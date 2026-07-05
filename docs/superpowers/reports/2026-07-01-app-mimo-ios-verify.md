# app_mimo iOS Verification Report

## Windows Checks

- Source inventory: pass.
  `Get-ChildItem -Force app_mimo\ios` shows `MateLink`, `IOS_SOURCE_INVENTORY.md`, `Podfile`, `project.yml`, `README.md`, and `VERIFY_IOS.md`.
- Inventory detail: pass.
  `Get-ChildItem -Recurse -File app_mimo\ios -Include *.swift,*.plist,*.json,*.strings,Podfile,project.yml` lists the expected SwiftUI sources, `Info.plist`, localized strings, mock JSON, `Podfile`, and `project.yml`.
- Missing Xcode entry before generation: pass.
  No `.xcodeproj`, `.xcworkspace`, or `project.pbxproj` exists yet under `app_mimo/ios`; generation remains a Mac-side step.
- Xcode project definition: pass.
  `app_mimo/ios/project.yml` defines the initial `MateLink` app target and keeps Widget setup deferred.
- Local Xcode availability: blocked on Windows.
  `where.exe xcodebuild` returned `INFO: Could not find files for the given pattern(s).`
- Local CocoaPods availability: blocked on Windows.
  `where.exe pod` returned `INFO: Could not find files for the given pattern(s).`

## Mac Commands To Run Later

```bash
cd app_mimo/ios
brew install xcodegen cocoapods
xcodegen generate
pod install
xcodebuild -workspace MateLink.xcworkspace -scheme MateLink -destination 'platform=iOS Simulator,name=iPhone 15' build
```

## Connected iPhone Status

Blocked until Mac/Xcode and signing are available.

## Review Mode Record

Automated code review was skipped because `review_mode=off` was selected for this Windows-side preparation pass, which only adds project metadata, documentation, and blocker evidence without changing iOS runtime behavior.

## Remaining Mac-Only Blockers

1. Install Xcode, Xcode command line tools, XcodeGen, and CocoaPods on a Mac.
2. Generate `MateLink.xcodeproj` and `MateLink.xcworkspace` from `project.yml` plus `Podfile`.
3. Resolve any CocoaPods or AMap SDK installation issues on macOS.
4. Set the final Apple Developer team and signing configuration in Xcode.
5. Run the simulator build, then run the app on the connected iPhone.

## Next Recommended Change After Environment Verification

After the first successful Mac launch proof, the next best follow-up is to decide whether `MateLink/Widget/MateLinkWidget.swift` should become a real extension target in this change or move into a dedicated follow-up change with signing and entitlements handled explicitly.
