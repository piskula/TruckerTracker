# TruckTrack Checklist

## What the checklist is

Drivers answer a fixed set of questions for one truck and one trailer once per week. The server is the durable record of those answers, accepts at most one response per user per week, and reminds drivers who haven't filled it in yet.

## Temporary daily testing mode

The backend currently uses a **daily** checklist while the client stays unchanged. `ChecklistConfig`
binds `DailyChecklistPeriod` and `DailyChecklistReminderScheduler` in code, with no runtime
daily/weekly selector. Submission, status and reminder eligibility all use the current calendar
day in `Europe/Bratislava`; a completed checklist stops today's reminder, but not tomorrow's.
A second submission on the same day returns `409` with "Už ste to dnes vyplnili.".

The scheduler runs **every day at 08:00 Europe/Bratislava**, including weekends, using the `DAILY`
stage and daily Slovak notification text. The existing `CHECKLIST_REMINDER_ENABLED` off switch
still applies. Reminder rows are unique per driver, day and stage, so repeated runs on the same
day do not send twice. A missed run is not caught up.

The existing `period_start_date` columns store the local day in this mode; no migration or API
change is needed. Existing weekly rows are not rewritten. Restoring weekly behaviour requires
changing both bean bindings in `ChecklistConfig` to `WeeklyChecklistPeriod` and
`ChecklistReminderScheduler`, then redeploying. Historical testing responses retain their daily
dates; only a Monday-dated response matches the restored weekly period.

Client labels and cached status behaviour remain weekly as described below. The following sections
document the retained weekly implementation, not the temporarily active daily cadence.

## Weekly period and reminders

- **Period**: the calendar week Monday–Sunday in `Europe/Bratislava`. On submit the server stamps `checklist_response.period_start_date` with that week's Monday (`WeeklyChecklistPeriod.startDateOf`); the client never sends it. Rows submitted before this column existed keep `NULL` and are not backfilled.
- **One per week**: a second submit by the same user in the same week is rejected with `409` and the message "Už ste to tento týždeň vyplnili.". The use case locks the account row before checking, and the unique constraint `uk_checklist_response_answered_by_period (answered_by_id, period_start_date)` backs it up.
- **Who is reminded**: accounts with a `DRIVER` row and no `MECHANIC` row in `account_role` that have no response for the current week, so a user who is both driver and mechanic is not reminded. An account stores every realm role of its verified JWT (one row per role); the rows are replaced whenever the account is synced (`CurrentUserService.currentUser()` — every login's push-device registration, every issue action and every checklist submit). Accounts that existed before the table was added get their roles on their next such request; until then they are not reminded.
- **When**: `ChecklistReminderScheduler` runs Monday 08:00 (`WEEK_START`, a nudge) and Wednesday 08:00 (`TWO_DAYS_LEFT`) `Europe/Bratislava`, unless `app.checklist-reminder.enabled` (`CHECKLIST_REMINDER_ENABLED`, default `true`) is `false`. A run missed while the server is down is not caught up.
- **At most one push per driver per stage and week**: `sendChecklistReminder` writes one `checklist_reminder` row (`account_id`, `period_start_date`, `stage`, `sent_at_utc`, unique on the first three) per due driver and skips drivers that already have one, so a restart or a repeated run never sends twice. Filling the checklist at any time silences the remaining reminders of that week.
- **Sending**: after the transaction commits, `ChecklistReminderDueListener` sends a `CHECKLIST_REMINDER` push through `PushNotificationService.sendToAccount` with `deepLink` `https://transroute.org/checklist`, which opens the checklist screen. Drivers without a registered push device are skipped silently; their reminder row is still written. The Slovak texts are `push.checklist-reminder.*` in `messages.properties`.

## Model

- **checklist_question**: bigint identity `id`; a unique short string identifier, which is the contract between questions and responses; `text` as a jsonb map of language to text; a `position` int for stable ordering; `created_at_utc`.
- **checklist_response**: bigint identity `id`; foreign keys `truck` and `trailer` to `vehicle(id)`, both required and their vehicle types verified on submit; `answered_by` to `account(id)`; `answered_at_utc`; nullable `period_start_date` (unique together with `answered_by_id`); `answers` as one jsonb document.
- **checklist_reminder**: bigint identity `id`; `account_id` to `account(id)`; `period_start_date`; `stage` (`WEEK_START`, `TWO_DAYS_LEFT`, `DAILY`); `sent_at_utc`; unique `(account_id, period_start_date, stage)`.
- **account_role**: bigint identity `id`; `account_id` to `account(id)`; `role` (`DRIVER`, `MECHANIC`); unique `(account_id, role)`, indexed by `role`.
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
- **POST /api/v1/checklist-response/create**: body: `truckId`, `trailerId`, and `answers` as a list of `{identifier, answer}` entries. There is no client-supplied language.
- **GET /api/v1/checklist-response/status**: the current user's checklist for the current week (`ChecklistPeriod`, so the week boundary follows `Europe/Bratislava` and the client never computes it). Returns `state` and `response`:
  - `COMPLETED` when the user already has a response this week, which is returned in `response` (same shape as the create response). This wins over the role rule, so a mechanic who filled it in also sees it.
  - `PENDING` when the user has no response this week and must fill it in. The rule matches the reminders: the verified JWT has `DRIVER` and not `MECHANIC`.
  - `NOT_REQUIRED` otherwise. `response` is `null` for both of these.

All endpoints require an authenticated user with the DRIVER or MECHANIC role.

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

Drivers open the checklist from the Checklist button stacked above the + button on the Issues screen (`feature:checklist`). The screen shows a vehicles card with separate truck and trailer pickers, preselected with the driver's remembered vehicles, followed by one card per question with an OK / Problem choice. A sticky bar shows progress and the Submit button. Submitting with a missing vehicle or unanswered check highlights what is missing and scrolls to it. A failed submit shows an error under the Submit button and keeps the answers; when the server rejects it because the driver already filled the checklist this week, the message says so and points to Monday instead of suggesting a retry. On success the app remembers the truck and trailer and shows a confirmation with both plates, the time, and the number of problems recorded. Leaving with unsubmitted answers asks for confirmation first. Problem answers are only recorded; they do not create issues.

### Weekly status, button and automatic redirect

`core:checklist`'s `ChecklistRepository.status` holds the current week's `ChecklistStatus` (`Unknown`, `Unavailable`, `NotRequired`, `Pending`, `Completed(response)`), loaded from the status endpoint. A successful submit sets it to `Completed`, and a `409` on submit refreshes it. A failed check sets `Unavailable` (the last known value is not kept, so last week's completion is never assumed). A status response that was in flight while the user signed out or submitted is dropped.

- **When it is loaded**: `TruckTrackViewModel` checks it once when access becomes `Ready` (app start, login, passing the notification gate); the checklist screen checks it again whenever it opens. There is no refresh when the app returns to the foreground, so if the week changes while the app stays in memory, the button catches up on the next start or login — the Monday reminder push opens the checklist screen, which checks for itself. Nothing waits for it: the UI is never blocked.
- **Button**: the Issues screen shows the Checklist button only while the status is `Pending`. If the last check failed (`Unavailable`), it falls back to showing it to drivers, since the server still rejects a second submit with `409`. It is hidden while loading, when `NotRequired`, and when `Completed`.
- **Automatic redirect**: only the check made when access becomes `Ready` can open the checklist, and only if it is `Pending` and the user stayed on the issues list while it ran. If that check fails, nothing opens and the button remains for drivers. Signing out clears the status.
- **Never twice, never over a deep link**: if a push opened the checklist or an issue, or the user tapped the button, before the check answered, the automatic open is skipped — even if the user already went back to the issues list. It also only navigates while the issues list is the current screen. If the automatic open happens first and the `/checklist` link arrives afterwards, `Navigator.navigate(root, destination)` ignores it because the checklist is already on top. Both are delivered only while the root is resumed and access is `Ready`.
- **Already completed**: opening the checklist (button, push, or deep link) also refreshes the status. Whenever the status is `Completed` — on opening, or learned later such as after a `409` — the screen shows a read-only summary of this week's submission (plates, time, problem count) with "Checklist already completed for this week", instead of the form. If the status check fails, the form opens as before.

## Known gaps and follow-ups

- **Question CRUD**: not implemented. Questions are currently immutable, seeded by migration only. The history safety described above is already designed in for when editing arrives.
- **Vehicle ownership gap**: vehicles are fleet-global, so any authenticated driver or mechanic can submit a checklist against any truck and trailer pair. Binding submissions to assigned vehicles is a pending product decision.
- **No driver↔truck assignment or per-driver timezones**: every driver is reminded on the same Bratislava schedule, regardless of vehicle.
- **"Thanks for being on time" feedback**: a possible later addition to the submit response; no design yet.
- **No read endpoints for responses beyond the current user's own week**: the status endpoint returns only that. The database indexes exist for a future search surface.
- **Migration**: the Liquibase changesets should be applied once against a fresh database to verify them before deploy.

## Implementation pointers

- Server domain: `server/module-server/src/main/kotlin/sk/momosilabs/truckTrack/checklist/`
- Contract: `server/module-api/src/main/kotlin/sk/momosilabs/truckTrack/api/checklist/`
- DTO mirrors: `shared/src/commonMain/kotlin/com/momosi/trucktrack/shared/checklist/`
- Client domain: `app/core/checklist/`
- Client UI: `app/feature/checklist/`
- Migrations: `../server/module-server/src/main/resources/db/changelog/2026/11/11-23-checklist.yaml` and `11-26-checklist-reminder.yaml` (the `2026/11` folder nesting is real; some tools display the path flattened)
