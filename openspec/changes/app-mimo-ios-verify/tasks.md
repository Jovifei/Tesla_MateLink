## 1. iOS Project Inventory

- [x] 1.1 Inventory `app_mimo/ios` Swift sources, resources, Info.plist, Podfile, and currently missing project/workspace metadata.
- [x] 1.2 Identify the minimum MateLink app target inputs required to launch Onboarding or the main tab shell.
- [x] 1.3 Decide whether the first build target includes the Widget source or defers it behind a separate target note.

## 2. Xcode Entry Point

- [x] 2.1 Add or restore a reproducible Xcode project/workspace entry point for `app_mimo/ios`.
- [x] 2.2 Include MateLink app sources, resources, Info.plist, and launch-required feature files in the project entry point.
- [x] 2.3 Configure bundle identifier, deployment target, Swift settings, and CocoaPods integration consistently with the existing Podfile.

## 3. Verification Documentation

- [x] 3.1 Document macOS/Xcode prerequisites and simulator build commands for the `app_mimo` iOS app.
- [x] 3.2 Document connected-iPhone verification prerequisites, including USB trust, signing, bundle identifier, and developer team setup.
- [x] 3.3 Document the minimum launch acceptance target: Onboarding or main tab shell without requiring a live TeslaMate server.

## 4. Verification Execution

- [x] 4.1 Run all possible local checks in the current environment and record command output.
- [x] 4.2 If macOS/Xcode is unavailable locally, create a verification record with exact Mac commands and the local blocker evidence.
- [ ] 4.3 If a Mac/Xcode environment is available, run simulator build/launch verification and record pass/fail evidence.
- [ ] 4.4 If signing and a connected iPhone are available, run physical-device verification and record pass/fail evidence.

## 5. Review And Handoff

- [x] 5.1 Update project documentation or review notes with the chosen iOS verification path.
- [x] 5.2 Run OpenSpec validation for `app-mimo-ios-verify`.
- [x] 5.3 Summarize remaining blockers and next recommended iOS feature change after environment verification.
