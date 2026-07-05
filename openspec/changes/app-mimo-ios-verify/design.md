## Context

`app_mimo` is the preferred implementation line for iOS work because its PRD declares a native Kotlin + Swift product direction and its Android implementation is the strongest reference slice. The iOS tree already contains SwiftUI app sources, feature views, Widget code, resources, and a CocoaPods `Podfile`, but no `.xcodeproj` or `.xcworkspace` is currently detectable in `app_mimo/ios`.

This change treats iOS environment verification as the next milestone before feature expansion. The immediate problem is not whether every iOS feature is complete; it is that the current repository cannot be proven buildable or launchable on simulator or a connected iPhone.

## Goals / Non-Goals

**Goals:**

- Make `app_mimo/ios` expose a reproducible Xcode entry point for the existing SwiftUI sources.
- Preserve the current source tree and avoid rewriting product features during environment bring-up.
- Define a verification path for simulator and connected-device development.
- Capture blocker evidence when the current machine cannot run macOS/Xcode verification.
- Keep documentation aligned with the actual iOS verification path.

**Non-Goals:**

- Implement the full missing iOS notification system.
- Complete AMap SDK map rendering across all map screens.
- Add Apple Watch as a new target.
- Prepare App Store release metadata, screenshots, or store signing.
- Copy implementation code from `docs/git_ref`; reference repositories remain research material only.

## Decisions

### Decision 1: Use `app_mimo` as the iOS continuation target

`app_mimo` has the clearest alignment between PRD and implementation: native Android, native iOS, and Web variants share the same product framing. `app_glm` remains useful for comparison, but its current TODOs and split implementation history make it less attractive for the first iOS verification milestone.

Alternative considered: continue `app_glm`. This would keep work near some archived GLM changes, but it would not resolve the stronger product direction in the MIMO PRD.

### Decision 2: Prioritize project/workspace verification before feature work

The first implementation slice will establish the Xcode project or workspace, dependency setup, and minimum launch path. This is more valuable than adding another feature to an iOS tree that cannot yet be opened or built reproducibly.

Alternative considered: implement one missing feature such as notifications or AMap first. That would increase source volume without proving that the app can run on a real iOS development setup.

### Decision 3: Keep verification evidence explicit and environment-aware

The local workspace is Windows-based, so Xcode and iPhone deployment may not be directly executable here. The implementation must still provide scripts or documentation that a Mac/Xcode environment can run, and must record any local blocker as evidence rather than claiming success.

Alternative considered: treat missing Mac/Xcode access as outside scope. That would leave the change without a credible completion gate.

### Decision 4: Avoid broad capability changes

This change introduces `ios-environment-verification` instead of modifying user-facing capabilities such as dashboard, settings, maps, or notifications. The behavior contract is about development verification, not end-user feature semantics.

## Risks / Trade-offs

- Missing macOS/Xcode access -> provide deterministic verification commands and record local blocker evidence.
- Generated or recreated Xcode project could omit source/resources -> require a source inventory check and at least one launch-target validation step.
- CocoaPods dependency setup may require macOS tooling -> document `pod install` expectations and fallback evidence.
- Signing can block physical iPhone deployment -> define simulator verification as the minimum local launch proof and physical-device verification as an additional gate when signing is available.
- Existing Swift files may contain compile errors unrelated to project metadata -> treat compile failures as build-stage findings to fix or record, not as reason to skip project creation.

## Migration Plan

1. Inventory `app_mimo/ios` sources, resources, Info.plist, Podfile, and target needs.
2. Add or restore an Xcode project/workspace entry point that includes the existing app sources and resources.
3. Document dependency setup and expected Xcode version, simulator, and connected-device steps.
4. Run the strongest available local checks in the current environment.
5. If Mac/Xcode is unavailable locally, produce a verification report with exact commands for a Mac and the local blocker evidence.

Rollback is straightforward: remove the newly added project/workspace metadata and verification documentation if the chosen project structure proves unsuitable.

## Open Questions

- Which Mac/Xcode environment will be used for the first live simulator or connected-iPhone verification?
- What Apple Developer Team ID, bundle identifier, and signing style should be used for physical-device deployment?
- Should AMap pods be enabled during first build verification, or should the initial project tolerate MapKit fallback first?
