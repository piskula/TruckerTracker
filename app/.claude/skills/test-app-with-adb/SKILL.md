---
name: test-app-with-adb
description: Use when driving the running Android app via adb to verify a UI change — navigating screens, tapping specific elements, filling text fields, or confirming a screen looks/behaves correctly after an edit. Triggered by phrases like "test this in the app", "navigate to the issue detail screen", "check this looks right on device", "tap the resolve button", "verify the UI change works".
---

# Skill: Drive the App via adb Using Compose Test Tags

> Every interactive element in `app/` carries a `Modifier.testTag(...)`, and the app root sets
> `testTagsAsResourceId = true` — so `adb shell uiautomator dump` exposes each tag as a
> **`resource-id`** you can grep for and tap by bounds, instead of guessing coordinates from a
> screenshot or fuzzy-matching visible text (which breaks under localization, dynamic content, or
> layout shifts).

## Why this matters

Compose semantics nodes have **no stable identifier** in a `uiautomator` dump by default —
`Modifier.testTag()` alone does not surface in `resource-id`; it only becomes visible there once
`testTagsAsResourceId = true` is set on an ancestor semantics node. That's already done once, for
the whole app, in `app/shared/src/commonMain/kotlin/com/momosi/trucktrack/app/TruckTrackApp.kt` on
the root `Scaffold`. You never need to set it again — just make sure any new interactive element
gets a `testTag`.

## Step 1 — Build, install, launch

```bash
./gradlew :app:android:assembleDebug          # from app/, or :app:app:android:assembleDebug from repo root
adb install -r app/android/build/outputs/apk/debug/android-debug.apk   # path relative to app/
adb shell monkey -p com.momosi.trucktrack -c android.intent.category.LAUNCHER 1
```

If the device is locked: `adb shell input keyevent 82 && adb shell wm dismiss-keyguard`.

**Windows/Git Bash gotcha:** Git Bash rewrites leading `/sdcard/...` paths to a Windows path before
they reach `adb`, breaking `uiautomator dump /sdcard/window_dump.xml`. Either export
`MSYS_NO_PATHCONV=1` for the session, or double the leading slash (`//sdcard/window_dump.xml`).

## Step 2 — Dump the hierarchy and read tags

```bash
adb shell uiautomator dump //sdcard/window_dump.xml
adb pull //sdcard/window_dump.xml ./window_dump.xml
grep -o 'resource-id="[a-zA-Z][^"]*"' window_dump.xml   # lists every tagged element on screen
```

Each match is a `testTag` value from the table below (or one you just added). To tap one, grab its
`bounds="[x1,y1][x2,y2]"` from the same node and tap the center:

```bash
grep -o 'resource-id="issue_card_18"[^/]*bounds="[^"]*"' window_dump.xml
adb shell input tap <center_x> <center_y>
```

To type into a focused text field: `adb shell input text "hello"` (tap the field's tag first to
focus it — `issue_detail_comment_field`, `create_issue_title_field`, etc.).

For a quick visual check instead of/alongside the hierarchy dump:
`adb exec-out screencap -p > screen.png`, then view it.

## Naming convention (follow this for any new tag)

`snake_case`, scoped by screen: `<screen>_<element>`, e.g. `issue_detail_resolve_button`. List
items and dynamic collections append a stable id, not an index into a mutable list:
`issue_card_${issue.id}`, `create_issue_vehicle_option_${vehicle.id}`. Reusable
`:core:ui-library` components that are structurally singular (the toolbar back arrow, a
confirmation dialog's buttons) get one fixed tag baked into the component itself, so every screen
that uses them gets the tag for free — don't re-tag these per screen.

## Tag catalog

**Global, from `:core:ui-library` (present on every screen that uses the component):**

| Tag | Component | Notes |
|---|---|---|
| `toolbar_back_button` | `Toolbar` | Present on any screen with a back toolbar (Issue Detail, Create Issue, Profile) |
| `confirmation_dialog_confirm_button` / `confirmation_dialog_dismiss_button` | `ConfirmationDialog` | Only one can be on screen at a time |
| `info_dialog_dismiss_button` | `InfoDialog` | Used by Profile's version dialog |
| `filter_chip_<value>` | `FilterChipRow` | `<value>` is the filtered item's `toString()`, e.g. `filter_chip_MyIssues` |
| `nav_bar_item_<NavKeySimpleName>` | `NavigationBar` | Component exists but isn't wired into the app yet — no screen uses bottom nav today |

**Sign In:** `sign_in_button`

**Issues List:** `issues_profile_button`, `filter_chip_MyIssues` / `_MyResolved` / `_MyWork` /
`_MyCompleted` / `_Open` / `_All`, `issue_card_<issueId>`, `issues_create_fab`,
`issues_retry_button`

**Issue Detail:** `toolbar_back_button`, `issue_detail_retry_button`,
`issue_detail_comment_field`, `issue_detail_send_comment_button`, `issue_detail_photo_<index>`,
`issue_detail_add_photo_button`, `issue_detail_reassign_button`,
`issue_detail_start_working_button`, `issue_detail_resolve_button`, plus
`confirmation_dialog_confirm_button` / `_dismiss_button` for the resolve confirmation

**Create Issue:** `toolbar_back_button`, `create_issue_vehicle_selector`,
`create_issue_vehicle_option_<vehicleId>`, `create_issue_title_field`,
`create_issue_description_field`, `create_issue_priority_Low` / `_Medium` / `_High`,
`create_issue_add_photo_button`, `create_issue_photo_<index>`,
`create_issue_remove_photo_<index>`, `create_issue_submit_button`

**Profile:** `toolbar_back_button`, `profile_version_info_button`, `profile_sign_out_button`,
`profile_app_version_row`, `info_dialog_dismiss_button`

**Full Screen Photo:** `full_screen_photo` (tap-to-dismiss background), `full_screen_photo_close_button`

## Warnings

- **Don't tap `profile_app_version_row` repeatedly.** `TestCrashManager`
  (`feature/profile/impl/TestCrashManagerImpl.kt`) throws a real, uncaught exception after 3 taps
  within 1 second — it's intentional (verifies Crashlytics end-to-end) but only in debug builds, and
  it will kill the app if you're just trying to open the version dialog.
- The tag catalog above reflects what exists as of this skill's writing. If a grep for an expected
  tag comes up empty, don't assume the convention broke — re-check the source file first, since
  screens do get restructured.

## Adding a tag to a new interactive element

Add `Modifier.testTag("<screen>_<element>")` to the element's own modifier chain (buttons, text
fields, clickable rows/boxes — not the static text/icon inside them). No other wiring needed; the
root-level `testTagsAsResourceId = true` already covers the whole tree. Update the catalog above
in the same change.
