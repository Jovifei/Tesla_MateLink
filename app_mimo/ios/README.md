# MateLink iOS

This directory contains the native SwiftUI implementation for `app_mimo`.

## Current Verification Strategy

Windows plus an iPhone cannot directly build and deploy this native iOS app. Use Windows to maintain sources and project metadata, then use Mac plus Xcode for simulator or connected-device verification.

## Generate Project On Mac

```bash
cd app_mimo/ios
brew install xcodegen cocoapods
xcodegen generate
pod install
open MateLink.xcworkspace
```

## Minimum Launch Target

The first verification succeeds when the app reaches Onboarding or the main tab shell without requiring a live TeslaMate server.
