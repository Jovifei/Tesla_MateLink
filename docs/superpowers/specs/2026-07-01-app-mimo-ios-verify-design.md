---
comet_change: app-mimo-ios-verify
role: technical-design
canonical_spec: openspec
---

# app_mimo iOS Environment Verification Design

## Objective

Prepare `app_mimo/ios` for iOS environment verification before adding more iOS features. The current workspace has SwiftUI sources, resources, Widget source, `Info.plist`, and a `Podfile`, but no detectable `.xcodeproj` or `.xcworkspace`. The immediate goal is to make the iOS app reproducibly openable and verifiable once a Mac/Xcode environment is available.

## Confirmed Path

Jovi currently has an iPhone but no confirmed Mac/Xcode environment. Native SwiftUI iOS apps cannot be developed and deployed directly from Windows to iPhone in the same way Android apps can be deployed from Windows to Android devices.

This change will therefore use path 3:

1. Prepare project metadata and verification documentation from Windows.
2. Record local blocker evidence for missing Mac/Xcode.
3. Leave exact Mac/Xcode simulator and connected-iPhone commands ready for later execution.

## Scope

In scope:

- Add or restore a reproducible Xcode project/workspace entry point for `app_mimo/ios`.
- Include the existing MateLink iOS app sources, resources, and launch-required files.
- Document simulator verification and connected-iPhone verification prerequisites.
- Record local verification evidence and blockers.

Out of scope:

- Full iOS notification implementation.
- Full AMap SDK rollout across all map screens.
- Apple Watch target creation.
- App Store packaging, screenshots, or release signing.
- Copying implementation code from `docs/git_ref`.

## Technical Approach

### 1. Inventory First

Build a source inventory for `app_mimo/ios`:

- App entry: `MateLink/App/MateLinkApp.swift`
- Root shell: `MateLink/App/ContentView.swift`
- State: `MateLink/App/AppState.swift`
- Models, API, map utilities, theme, resources, feature views, Widget source, and `Info.plist`
- Existing dependency declaration: `Podfile`

This inventory becomes the checklist for the Xcode project entry point.

### 2. App Target Before Widget Target

The first build target should prioritize the MateLink iOS app target. Widget source should be inventoried and documented, but the Widget target can be deferred if including it blocks the first app launch proof.

Rationale: the fastest useful proof is that the app itself reaches Onboarding or the main tab shell. Widget target setup often adds signing, entitlements, bundle identifier, and extension configuration complexity.

### 3. Prefer AMap Documentation, Allow First Launch Without AMap Runtime Proof

The existing `Podfile` declares AMap dependencies. The Xcode entry point should be compatible with CocoaPods, but first launch verification may document AMap as a Mac-side dependency step instead of proving SDK runtime behavior on Windows.

Rationale: Windows cannot run `pod install` or Xcode builds directly. The verification chain should not pretend AMap has been proven locally.

### 4. Evidence-Oriented Verification

Verification outputs must distinguish:

- What was checked on Windows.
- What requires Mac/Xcode.
- What exact command should be run later on Mac.
- Whether simulator or connected-iPhone verification passed, failed, or is blocked.

The expected verification report should include command lines, environment assumptions, and next fix targets.

## Verification Plan

Windows-side verification:

- Confirm the Xcode project/workspace metadata exists.
- Confirm project metadata references the required source/resource inventory.
- Run OpenSpec validation.
- Record that `xcodebuild`, simulator, and connected-iPhone deployment are blocked locally by missing Mac/Xcode.

Mac-side simulator verification:

```bash
cd app_mimo/ios
pod install
xcodebuild -workspace MateLink.xcworkspace -scheme MateLink -destination 'platform=iOS Simulator,name=iPhone 15' build
```

Mac-side connected-iPhone verification:

1. Connect iPhone by USB.
2. Trust the computer on the device.
3. Set signing team and bundle identifier in Xcode.
4. Select the connected iPhone as destination.
5. Run the MateLink target.

The minimum launch acceptance target is reaching Onboarding or the main tab shell without a live TeslaMate server.

## Risks

- Xcode project metadata may omit Swift files or resources.
  Mitigation: use the source inventory as a verification checklist.

- Widget target inclusion may block app target verification.
  Mitigation: defer Widget target if needed and record it as a follow-up.

- AMap/CocoaPods may fail on the first Mac run.
  Mitigation: keep the `pod install` step explicit and categorize dependency failures separately from project metadata failures.

- Physical-device signing may block iPhone deployment.
  Mitigation: make simulator build the first proof, and document signing/team requirements for later device verification.

## Open Items

- Final Apple Developer Team ID and bundle identifier for physical-device signing.
- Whether the later Mac verification should use a local Mac, borrowed Mac, or cloud Mac.
- Whether Widget target verification belongs in this change after the app target launches, or in a follow-up change.
