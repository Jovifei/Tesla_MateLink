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
