# iOS Verification

## Windows Evidence

Run the local file inventory checks, verify `project.yml` and `Podfile`, and record that `xcodebuild` is unavailable on Windows.

## Mac Simulator

```bash
cd app_mimo/ios
xcodegen generate
pod install
xcodebuild -workspace MateLink.xcworkspace -scheme MateLink -destination 'platform=iOS Simulator,name=iPhone 15' build
```

Expected first-pass acceptance: the app builds successfully and launches into Onboarding or the main tab shell with mock data.

## Connected iPhone

1. Connect the iPhone by USB.
2. Trust the computer on the iPhone.
3. Open `MateLink.xcworkspace`.
4. Set Team and Bundle Identifier under Signing & Capabilities.
5. Select the connected iPhone.
6. Run the `MateLink` scheme.

## Notes

- If CocoaPods fails, resolve dependency installation before treating the app target as broken.
- If signing fails, keep simulator verification as the first build proof and record the exact signing blocker.
