# Comet Design Handoff

- Change: app-mimo-ios-verify
- Phase: design
- Mode: compact
- Context hash: 3791915167d2660c73df5a054647ed4ecbddbd72806bf55832eba8a57455d183

Generated-by: comet-handoff.sh

OpenSpec remains the canonical capability spec. This handoff is a deterministic, source-traceable context pack, not an agent-authored summary.

## openspec/changes/app-mimo-ios-verify/proposal.md

- Source: openspec/changes/app-mimo-ios-verify/proposal.md
- Lines: 1-27
- SHA256: 35c1943b6d5fa805f4c428a569ee474611ccfc9ca4f1869a62771cace84f5dbb

```md
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
```

## openspec/changes/app-mimo-ios-verify/design.md

- Source: openspec/changes/app-mimo-ios-verify/design.md
- Lines: 1-71
- SHA256: 4cf33bba39b86a38a777c191b0878ad51fb2c0ecb45c46238028290dd1bbc0bf

```md
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
```

## openspec/changes/app-mimo-ios-verify/tasks.md

- Source: openspec/changes/app-mimo-ios-verify/tasks.md
- Lines: 1-30
- SHA256: 9f9e8bdd9ab1f2f41da3e03cceb971a0232f184e7424762c831f9193556aefa3

```md
## 1. iOS Project Inventory

- [ ] 1.1 Inventory `app_mimo/ios` Swift sources, resources, Info.plist, Podfile, and currently missing project/workspace metadata.
- [ ] 1.2 Identify the minimum MateLink app target inputs required to launch Onboarding or the main tab shell.
- [ ] 1.3 Decide whether the first build target includes the Widget source or defers it behind a separate target note.

## 2. Xcode Entry Point

- [ ] 2.1 Add or restore a reproducible Xcode project/workspace entry point for `app_mimo/ios`.
- [ ] 2.2 Include MateLink app sources, resources, Info.plist, and launch-required feature files in the project entry point.
- [ ] 2.3 Configure bundle identifier, deployment target, Swift settings, and CocoaPods integration consistently with the existing Podfile.

## 3. Verification Documentation

- [ ] 3.1 Document macOS/Xcode prerequisites and simulator build commands for the `app_mimo` iOS app.
- [ ] 3.2 Document connected-iPhone verification prerequisites, including USB trust, signing, bundle identifier, and developer team setup.
- [ ] 3.3 Document the minimum launch acceptance target: Onboarding or main tab shell without requiring a live TeslaMate server.

## 4. Verification Execution

- [ ] 4.1 Run all possible local checks in the current environment and record command output.
- [ ] 4.2 If macOS/Xcode is unavailable locally, create a verification record with exact Mac commands and the local blocker evidence.
- [ ] 4.3 If a Mac/Xcode environment is available, run simulator build/launch verification and record pass/fail evidence.
- [ ] 4.4 If signing and a connected iPhone are available, run physical-device verification and record pass/fail evidence.

## 5. Review And Handoff

- [ ] 5.1 Update project documentation or review notes with the chosen iOS verification path.
- [ ] 5.2 Run OpenSpec validation for `app-mimo-ios-verify`.
- [ ] 5.3 Summarize remaining blockers and next recommended iOS feature change after environment verification.
```

## openspec/changes/app-mimo-ios-verify/specs/ios-environment-verification/spec.md

- Source: openspec/changes/app-mimo-ios-verify/specs/ios-environment-verification/spec.md
- Lines: 1-45
- SHA256: 098c933d1685043b19489f41725b4683a20c4383963b41d2e0fea4fdf6f6d83d

```md
## ADDED Requirements

### Requirement: iOS project entry point
The system SHALL provide a reproducible Xcode project or workspace entry point for `app_mimo/ios` that includes the existing MateLink SwiftUI app sources, resources, Info.plist, and widget sources required for environment verification.

#### Scenario: Developer opens the iOS project
- **WHEN** a developer follows the documented iOS setup path from the repository
- **THEN** Xcode can open the declared project or workspace entry point for `app_mimo/ios`

#### Scenario: Existing sources are included
- **WHEN** the iOS project entry point is inspected
- **THEN** it references the MateLink app entry, app state, tab shell, feature views, resources, and Info.plist needed for a minimum app launch

### Requirement: Simulator build path
The system SHALL define a simulator build path for the `app_mimo` iOS app that can prove whether the app reaches a minimum launchable state.

#### Scenario: Simulator build is available
- **WHEN** a macOS developer runs the documented simulator build command with the required Xcode version installed
- **THEN** the command builds the MateLink iOS target or reports concrete compile, dependency, or project configuration errors

#### Scenario: Minimum launch target is reached
- **WHEN** the simulator build and launch succeed
- **THEN** the app reaches either Onboarding or the main tab shell without requiring TeslaMate server access

### Requirement: Connected-device verification path
The system SHALL document the connected-iPhone verification path, including prerequisites for USB trust, signing, bundle identifier, and developer team configuration.

#### Scenario: Physical device prerequisites are visible
- **WHEN** a developer prepares to run the app on a connected iPhone
- **THEN** the documentation lists the signing and device trust prerequisites before the run command or Xcode action

#### Scenario: Signing blocks deployment
- **WHEN** physical-device deployment fails because signing configuration is missing
- **THEN** the verification record identifies signing as the blocker and preserves simulator verification as the minimum local proof

### Requirement: Verification evidence
The system SHALL record verification evidence for the iOS environment bring-up, including commands attempted, pass/fail status, and blockers that prevent local validation.

#### Scenario: Local machine cannot run Xcode
- **WHEN** the current environment lacks macOS or Xcode
- **THEN** the verification record states that limitation and includes the exact commands to run on a Mac

#### Scenario: Build fails
- **WHEN** simulator or device build verification fails
- **THEN** the verification record includes the failing command, error category, and next fix target
```

