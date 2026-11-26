# TruckTrack Checklist

## What the checklist is

Drivers answer a fixed set of questions for one truck and one trailer once per week. The server is the durable record of those answers, accepts at most one response per user per week, and reminds drivers who haven't filled it in yet.

## Weekly period and reminders

- **Period**: the calendar week Monday–Sunday in `Europe/Bratislava`. On submit the server stamps `checklist_response.period_start_date` with that week's Monday (`ChecklistPeriod.startDateOf`); the client never sends it. Rows submitted before this column existed keep `NULL` and are not backfilled.
- **One per week**: a second submit by the same user in the same week is rejected with `409` and the message "Už ste to tento týždeň vyplnili.". The use case locks the account row before checking, and the unique constraint `uk_checklist_response_answered_by_period (answered_by_id, period_start_date)` backs it up.
- **Who is reminded**: accounts with `account.role = DRIVER` that have no response for the current week. The role is stored from the verified JWT's realm roles whenever the account is synced (`CurrentUserService.currentUser()` — every login's push-device registration, every issue action and every checklist submit). Accounts that existed before the column was added get their role on their next such request; until then they are not reminded.
- **When**: `ChecklistReminderScheduler` runs Monday 08:00 (`WEEK_START`, a nudge) and Wednesday 08:00 (`TWO_DAYS_LEFT`) `Europe/Bratislava`, only when `app.checklist-reminder.enabled` (`CHECKLIST_REMINDER_ENABLED`) is `true`. A run missed while the server is down is not caught up.
- **At most one push per driver per stage and week**: `sendChecklistReminder` writes one `checklist_reminder` row (`account_id`, `period_start_date`, `stage`, `sent_at_utc`, unique on the first three) per due driver and skips drivers that already have one, so a restart or a repeated run never sends twice. Filling the checklist at any time silences the remaining reminders of that week.
- **Sending**: after the transaction commits, `ChecklistReminderDueListener` sends a `CHECKLIST_REMINDER` push through `PushNotificationService.sendToAccount` with `deepLink` `https://transroute.org/checklist`, which opens the checklist screen. Drivers without a registered push device are skipped silently; their reminder row is still written. The Slovak texts are `push.checklist-reminder.*` in `messages.properties`.

## Model

- **checklist_question**: bigint identity `id`; a unique short string identifier, which is the contract between questions and responses; `text` as a jsonb map of language to text; a `position` int for stable ordering; `created_at_utc`.
- **checklist_response**: bigint identity `id`; foreign keys `truck` and `trailer` to `vehicle(id)`, both required and their vehicle types verified on submit; `answered_by` to `account(id)`; `answered_at_utc`; nullable `period_start_date` (unique together with `answered_by_id`); `answers` as one jsonb document.
- **checklist_reminder**: bigint identity `id`; `account_id` to `account(id)`; `period_start_date`; `stage` (`WEEK_START`, `TWO_DAYS_LEFT`); `sent_at_utc`; unique `(account_id, period_start_date, stage)`.
- **Indexes**: composite btrees on `(truck_id, answered_at_utc)`, `(trailer_id, answered_at_utc)` and `(answered_by_id, answered_at_utc)` for later search, plus a GIN `jsonb_path_ops` index on `answers` for content search.

Seeded questions occupy positions 1-8 and are currently Slovak only:

| Identifier | Text |
| --- | --- |
| `OIL_1` | Kontrola hladiny oleja |
| `WHEELS_1` | Vizuálna kontrola pneumatík (poškodenie, opotrebenie) |
| `WHEELS_2` | Kontrola tlaku v pneumatikách |
| `WHEELS_3` | Kontrola dotiahnutia kolies |
| `LIGHTS_1` | Kontrola osvetlenia |
| `BODYWORK_1` | Vizuálna kontrola vozidla (škrabance, odreniny, schodíky, úniky kvapalín) |
| `LICENSE_1` | Kontrola dokladov (eurolicencia, osvedčenia, karty) |
| `TOLL_1` | Kontrola mýtnych jednotiek (umiestnenie a počet náprav) |

## Point-in-time record

A response is a self-contained historical document. The client never sends question text and never sends a display language — only answers as `{identifier, answer}` entries. At submit time the server snapshots into the response: the truck and trailer with `id` and `licensePlate`, and per item the identifier, a `texts` list of `language`/`text` pairs holding every language available at that moment, and the boolean answer. Because the snapshot stores every language available at submit time, the client's display language is not recorded. Later question edits can never corrupt history — an answer always shows what was actually asked.

The `answers` jsonb looks like this (illustrative):

```json
{
  "truck": { "id": 12, "licensePlate": "MA151E0" },
  "trailer": { "id": 34, "licensePlate": "MA777TR" },
  "items": [
    {
      "identifier": "OIL_1",
      "texts": [
        { "language": "sk", "text": "Kontrola hladiny oleja" }
      ],
      "answer": true
    }
  ]
}
```

## Endpoints

- **GET /api/v1/checklist-question**: optional `Accept-Language` header (for example `en-US,en;q=0.9,sk;q=0.8`); there is no language query parameter. Text resolution honors language preference weights and regional fallback, then falls back to `sk`, then to the first available entry. Missing or blank headers default to `sk`; malformed language preferences yield 422. Questions are ordered by `position`, then `id`. A question with no texts at all yields 422.
- **POST /api/v1/checklist-response/create**: the only response endpoint; the surface is submit-only by design. Body: `truckId`, `trailerId`, and `answers` as a list of `{identifier, answer}` entries. There is no client-supplied language.

Both endpoints require an authenticated user with the DRIVER or MECHANIC role.

Validation behaves as follows:

- **Empty answers list**: 422.
- **Null answer entry**: 422.
- **Duplicate identifiers**: 422, listing the duplicated identifiers with the sanitized rendering below.
- **Answer identifiers**: must exactly equal the current set of question identifiers, otherwise 422 listing the missing and unknown identifiers. The message is sanitized: single-line, truncated, at most 10 identifiers plus "(+N more)".
- **Unknown truck or trailer id**: 404.
- **Wrong vehicle type**: 422 when the truck is not a TRUCK or the trailer is not a TRAILER.
- **Already submitted this week**: 409, checked before the answers are validated.
- **answeredBy**: always taken from the JWT subject, never from the request.

## Multi-language

One language, `sk`, exists today. The jsonb text column is ready for more languages with no schema change, and the response snapshot preserves every language available at submit time.

The mobile app's shared API and image clients send the selected app language in `Accept-Language` on every request, reading the current selection so language changes apply to subsequent requests.

## Mobile app

Drivers open the checklist from the Checklist button stacked above the + button on the Issues screen (`feature:checklist`). The screen shows a vehicles card with separate truck and trailer pickers, preselected with the driver's remembered vehicles, followed by one card per question with an OK / Problem choice. A sticky bar shows progress and the Submit button. Submitting with a missing vehicle or unanswered check highlights what is missing and scrolls to it. A failed submit shows an error under the Submit button and keeps the answers. On success the app remembers the truck and trailer and shows a confirmation with both plates, the time, and the number of problems recorded. Leaving with unsubmitted answers asks for confirmation first. Problem answers are only recorded; they do not create issues.

## Known gaps and follow-ups

- **Question CRUD**: not implemented. Questions are currently immutable, seeded by migration only. The history safety described above is already designed in for when editing arrives.
- **Vehicle ownership gap**: vehicles are fleet-global, so any authenticated driver or mechanic can submit a checklist against any truck and trailer pair. Binding submissions to assigned vehicles is a pending product decision.
- **No driver↔truck assignment or per-driver timezones**: every driver is reminded on the same Bratislava schedule, regardless of vehicle.
- **"Thanks for being on time" feedback**: a possible later addition to the submit response; no design yet.
- **No read endpoints for responses yet**: by design for now. The database indexes exist for the future search surface.
- **Migration**: the Liquibase changesets should be applied once against a fresh database to verify them before deploy.

## Implementation pointers

- Server domain: `server/module-server/src/main/kotlin/sk/momosilabs/truckTrack/checklist/`
- Contract: `server/module-api/src/main/kotlin/sk/momosilabs/truckTrack/api/checklist/`
- DTO mirrors: `shared/src/commonMain/kotlin/com/momosi/trucktrack/shared/checklist/`
- Client domain: `app/core/checklist/`
- Client UI: `app/feature/checklist/`
- Migrations: `../server/module-server/src/main/resources/db/changelog/2026/11/11-23-checklist.yaml` and `11-26-checklist-reminder.yaml` (the `2026/11` folder nesting is real; some tools display the path flattened)
