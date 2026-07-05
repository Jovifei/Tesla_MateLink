# app_mimo Package 3 Reconciliation

Worktree: `C:\Users\Admin\.config\superpowers\worktrees\tesla_master\codex-app-mimo-stitch-1to1`

Date: `2026-07-05`

## Fixed

- iOS tab labels are now wired to existing `Localizable.strings` keys (`nav.dashboard`, `nav.drives`, `nav.charges`, `nav.more`) through the live app shell.
- `SettingsView` now uses existing localization keys for the most obvious entry copy (`connection`, `server_url`, `api_token`, `test_connection`, `preferences`, `mock_mode`, `version`, `settings.title`).
- `MoreView` now uses existing localization keys for the screen title and the clearest reused entries (`nav.more`, `battery_health.title`, `settings.title`, `about`).
- `app_mimo/README.md`, `app_mimo/ios/README.md`, `app_mimo/ios/VERIFY_IOS.md`, and `app_mimo/ios/IOS_SOURCE_INVENTORY.md` now agree on the iOS build entry: `project.yml` -> XcodeGen -> CocoaPods -> `MateLink.xcworkspace`.
- The same docs now consistently describe ATS and local networking as already present in `MateLink/Info.plist` for Windows-prep verification.
- Widget wording is now aligned to `deferred / source exists but target not wired`, with the missing proof called out explicitly: no widget target in `project.yml`, no `.entitlements` file, and no project-level App Group wiring proof.

## Still Valid

- Native iOS build proof is still missing on this Windows machine. Mac plus Xcode are still required for `xcodegen`, `pod install`, simulator build, and device signing verification.
- iOS widget runtime support is still not ready to claim. Source files reference `group.com.matelink`, but there is still no target or entitlement wiring evidence in the checked-in project metadata.
- iOS localization is still partial. This pass only wires the tab labels plus the most obvious `Settings` and `More` entry text to existing keys; many deeper feature screens still contain hardcoded copy.

## Outdated

- Any statement that the iOS app should be opened directly as a finished checked-in Xcode project is outdated. The repo entry is generated from `project.yml` and then opened via `MateLink.xcworkspace` after CocoaPods.
- Any statement that iOS Widget support is already active or verified is outdated. The current repository only proves source presence, not project integration.
- Any statement that ATS or local-networking setup is still absent from iOS is outdated. `MateLink/Info.plist` already contains `NSAppTransportSecurity` with `NSAllowsLocalNetworking` plus `NSLocalNetworkUsageDescription`.
