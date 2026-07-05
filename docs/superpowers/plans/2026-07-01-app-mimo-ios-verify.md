---
change: app-mimo-ios-verify
design-doc: docs/superpowers/specs/2026-07-01-app-mimo-ios-verify-design.md
base-ref: f53d652825d9f753236bcecdaefcb57680c5731a
---

# app_mimo iOS Environment Verification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prepare `app_mimo/ios` so its existing SwiftUI app can be opened and verified later on Mac/Xcode, while recording that Windows + iPhone alone cannot perform native iOS deployment.

**Architecture:** Add a reproducible XcodeGen project definition instead of hand-writing fragile `.pbxproj` metadata on Windows. Document the Mac generation/build/device workflow and create a local verification record that distinguishes Windows checks from Mac/Xcode blockers.

**Tech Stack:** SwiftUI, Xcode 15+, iOS 16.0+, CocoaPods, XcodeGen, PowerShell file inventory checks, OpenSpec.

---

### Task 1: iOS Source Inventory

**Files:**
- Create: `app_mimo/ios/IOS_SOURCE_INVENTORY.md`
- Read: `app_mimo/ios/MateLink/**/*.swift`
- Read: `app_mimo/ios/MateLink/Resources/**`
- Read: `app_mimo/ios/MateLink/Info.plist`
- Read: `app_mimo/ios/Podfile`

- [ ] **Step 1: Generate source inventory**

Run:
```powershell
Get-ChildItem -Recurse -File app_mimo\ios |
  Where-Object { $_.FullName -notmatch '\\Pods\\|\\build\\|\\.xcodeproj\\|\\.xcworkspace\\' } |
  Select-Object FullName, Length |
  Sort-Object FullName
```

Expected: lists Swift sources, resources, Info.plist, and Podfile.

- [ ] **Step 2: Write inventory document**

Create `app_mimo/ios/IOS_SOURCE_INVENTORY.md` with sections:
```markdown
# app_mimo iOS Source Inventory

## App Target Inputs
- `MateLink/App/MateLinkApp.swift`
- `MateLink/App/ContentView.swift`
- `MateLink/App/AppState.swift`

## Core Inputs
- `MateLink/Core/API/ApiClient.swift`
- `MateLink/Core/Models/Car.swift`
- `MateLink/Core/Models/CarStatus.swift`
- `MateLink/Core/Map/AmapView.swift`
- `MateLink/Core/Map/MapUtils.swift`
- `MateLink/Core/Theme/AppTheme.swift`
- `MateLink/Core/Utils/GCJ02Converter.swift`
- `MateLink/Core/Utils/RouteSimplifier.swift`

## Feature Inputs
- Include every `MateLink/Features/**/*.swift` file.

## Resources
- `MateLink/Info.plist`
- `MateLink/Resources/mock_data.json`
- `MateLink/Resources/*.lproj/Localizable.strings`

## Deferred Target Inputs
- `MateLink/Widget/MateLinkWidget.swift` is inventoried but may be deferred until the app target launches.
```

- [ ] **Step 3: Verify no Xcode entry exists yet**

Run:
```powershell
Get-ChildItem -Recurse -Force app_mimo\ios -Include *.xcodeproj,*.xcworkspace,project.pbxproj
```

Expected before implementation: no project/workspace files.

### Task 2: Reproducible Xcode Project Definition

**Files:**
- Create: `app_mimo/ios/project.yml`
- Modify: `app_mimo/ios/Podfile`

- [ ] **Step 1: Add XcodeGen project definition**

Create `app_mimo/ios/project.yml`:
```yaml
name: MateLink
options:
  bundleIdPrefix: com.jovif
  deploymentTarget:
    iOS: "16.0"
settings:
  base:
    SWIFT_VERSION: "5.9"
    DEVELOPMENT_TEAM: ""
targets:
  MateLink:
    type: application
    platform: iOS
    deploymentTarget: "16.0"
    sources:
      - path: MateLink/App
      - path: MateLink/Core
      - path: MateLink/Features
      - path: MateLink/Resources
    resources:
      - path: MateLink/Resources/mock_data.json
      - path: MateLink/Resources/en.lproj
      - path: MateLink/Resources/zh-Hans.lproj
      - path: MateLink/Resources/de.lproj
      - path: MateLink/Resources/fr.lproj
      - path: MateLink/Resources/ja.lproj
    info:
      path: MateLink/Info.plist
    settings:
      PRODUCT_BUNDLE_IDENTIFIER: com.jovif.matelink
      INFOPLIST_FILE: MateLink/Info.plist
```

- [ ] **Step 2: Ensure Podfile uses generated project**

Modify `app_mimo/ios/Podfile` to include:
```ruby
project 'MateLink.xcodeproj'
```

directly after `platform :ios, '16.0'`.

- [ ] **Step 3: Document Widget deferral in the inventory**

Update `IOS_SOURCE_INVENTORY.md` to state the initial XcodeGen config verifies the app target first; Widget target setup is a follow-up unless Mac verification proves it is trivial.

### Task 3: iOS Verification Documentation

**Files:**
- Create: `app_mimo/ios/README.md`
- Create: `app_mimo/ios/VERIFY_IOS.md`

- [ ] **Step 1: Write iOS README**

Create `app_mimo/ios/README.md` with:
```markdown
# MateLink iOS

This directory contains the native SwiftUI implementation for app_mimo.

## Current Verification Strategy

Windows + iPhone cannot directly build and deploy this native iOS app. Use Windows to maintain sources and project metadata, then use Mac + Xcode for simulator or connected-device verification.

## Generate Project On Mac

```bash
cd app_mimo/ios
brew install xcodegen
xcodegen generate
pod install
open MateLink.xcworkspace
```

## Minimum Launch Target

The first verification succeeds when the app reaches Onboarding or the main tab shell without requiring a live TeslaMate server.
```

- [ ] **Step 2: Write verification guide**

Create `app_mimo/ios/VERIFY_IOS.md` with simulator and device commands:
```markdown
# iOS Verification

## Windows Evidence

Run file inventory checks and record that Xcode is unavailable locally.

## Mac Simulator

```bash
cd app_mimo/ios
xcodegen generate
pod install
xcodebuild -workspace MateLink.xcworkspace -scheme MateLink -destination 'platform=iOS Simulator,name=iPhone 15' build
```

## Connected iPhone

1. Connect iPhone by USB.
2. Trust the computer on the iPhone.
3. Open `MateLink.xcworkspace`.
4. Set Team and Bundle Identifier under Signing & Capabilities.
5. Select the connected iPhone.
6. Run the `MateLink` scheme.
```

- [ ] **Step 3: Link verification docs from source inventory**

Add a final line to `IOS_SOURCE_INVENTORY.md`:
```markdown
Verification instructions live in `README.md` and `VERIFY_IOS.md`.
```

### Task 4: Verification Record

**Files:**
- Create: `docs/superpowers/reports/2026-07-01-app-mimo-ios-verify.md`
- Modify: `openspec/changes/app-mimo-ios-verify/tasks.md`

- [ ] **Step 1: Run local Windows checks**

Run:
```powershell
Get-ChildItem -Force app_mimo\ios | Select-Object Mode,Length,Name
Get-ChildItem -Recurse -File app_mimo\ios -Include *.swift,*.plist,*.json,*.strings,Podfile,project.yml | Select-Object FullName,Length
where.exe xcodebuild
where.exe pod
```

Expected: source/project metadata exists; `xcodebuild` is not available on Windows.

- [ ] **Step 2: Write verification report**

Create `docs/superpowers/reports/2026-07-01-app-mimo-ios-verify.md`:
```markdown
# app_mimo iOS Verification Report

## Windows Checks

- Source inventory: pass/fail with command evidence.
- Xcode project definition: pass/fail with `project.yml` evidence.
- Local Xcode availability: blocked on Windows.

## Mac Commands To Run Later

```bash
cd app_mimo/ios
xcodegen generate
pod install
xcodebuild -workspace MateLink.xcworkspace -scheme MateLink -destination 'platform=iOS Simulator,name=iPhone 15' build
```

## Connected iPhone Status

Blocked until Mac/Xcode and signing are available.
```

- [ ] **Step 3: Validate OpenSpec**

Run:
```powershell
openspec validate app-mimo-ios-verify --strict
```

Expected: `Change 'app-mimo-ios-verify' is valid`.

- [ ] **Step 4: Mark OpenSpec tasks complete as implementation evidence is produced**

Update `openspec/changes/app-mimo-ios-verify/tasks.md` only after each implemented item has evidence in the report or files.

### Task 5: Final Review

**Files:**
- Read: `app_mimo/ios/README.md`
- Read: `app_mimo/ios/VERIFY_IOS.md`
- Read: `docs/superpowers/reports/2026-07-01-app-mimo-ios-verify.md`
- Read: `openspec/changes/app-mimo-ios-verify/tasks.md`

- [ ] **Step 1: Check no placeholders remain**

Run:
```powershell
rg -n "TBD|TODO|fill in|placeholder" app_mimo\ios\README.md app_mimo\ios\VERIFY_IOS.md docs\superpowers\reports\2026-07-01-app-mimo-ios-verify.md
```

Expected: no placeholder text that weakens verification.

- [ ] **Step 2: Confirm project metadata references required launch inputs**

Run:
```powershell
Select-String -Path app_mimo\ios\project.yml -Pattern "MateLink/App|MateLink/Core|MateLink/Features|MateLink/Resources|Info.plist|PRODUCT_BUNDLE_IDENTIFIER"
```

Expected: every pattern appears.

- [ ] **Step 3: Summarize remaining Mac-only blockers**

Add a final section to the verification report listing exactly what must be done after Jovi gets Mac access.
