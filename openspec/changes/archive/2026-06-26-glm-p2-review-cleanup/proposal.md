## Why

17 轮交叉审核后，Android 侧 CRITICAL/HIGH 已清零。剩余 4 个 MEDIUM 级别优化项，均为代码质量改进，不影响功能正确性，但值得在合并前清理。

## What Changes

- **SettingsScreen**: 移除 `onSave` 中冗余的 `saveServerUrl()` + `saveApiToken()` 调用（已被 `saveSettings()` 覆盖）
- **Vehicle3dScreen**: 手势提示的浮点比较从 exact `== 0f` 改为 epsilon 比较，确保用户拖回中心后提示能重新显示
- **DataExporter**: `createShareIntent()` 从实例方法改为 companion object 函数，避免每次分享创建新实例
- **AnnualReportPDFScreen**: PDF 生成前清理同一年份的旧文件，防止 cacheDir 累积

## Capabilities

### New Capabilities

无新增能力。

### Modified Capabilities

无需求变更。本次仅为实现层优化。

## Impact

- `SettingsScreen.kt`: 1 行删除
- `Vehicle3dScreen.kt`: 3 行修改
- `DataExporter.kt` + `ExportScreen.kt`: 方法签名变更 + 调用点更新
- `AnnualReportPDFScreen.kt`: 3 行新增（文件清理）
