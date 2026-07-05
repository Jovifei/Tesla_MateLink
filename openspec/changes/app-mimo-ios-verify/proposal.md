## Why

`app_mimo/ios` contains a broad SwiftUI application source tree, but the repository does not currently expose a detectable Xcode project or workspace. This blocks the next practical milestone: verifying that the iOS app can be opened, built, and launched on a simulator or a connected iPhone.

## What Changes

- Add an iOS environment verification capability for `app_mimo`.
- Establish a reproducible iOS project/workspace entry point for the existing SwiftUI sources.
- Document the simulator and connected-device verification path, including signing and environment prerequisites.
- Define the minimum launch verification target as reaching Onboarding or the main tab shell without adding large product features.
- Record any machine-specific blockers, such as missing macOS/Xcode access, as verification evidence instead of treating them as completion.

## Capabilities

### New Capabilities

- `ios-environment-verification`: Covers the `app_mimo` iOS project/workspace entry point, dependency setup, simulator/device build path, and minimum app launch verification evidence.

### Modified Capabilities

- None.

## Impact

- Affected code and files: `app_mimo/ios`, iOS project/workspace metadata, iOS dependency configuration, and verification documentation.
- Affected systems: local iOS development environment, Xcode, CocoaPods, simulator, and connected iPhone verification flow.
- Not affected: Android implementation, Web implementation, `docs/git_ref` reference code, App Store release packaging, and full iOS feature completion.
