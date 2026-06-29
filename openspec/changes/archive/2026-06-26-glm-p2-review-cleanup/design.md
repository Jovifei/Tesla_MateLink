## Architecture Decisions

本次为 4 个独立的小优化，无架构变更。每个修复独立且无跨依赖。

## Approach

### 1. SettingsScreen 冗余保存

`saveSettings()` 已经持久化 `serverUrl` 和 `apiToken`。额外的 `saveServerUrl()` + `saveApiToken()` 是 no-op，删除即可。

### 2. Vehicle3dScreen epsilon 比较

将 `offsetX == 0f && offsetY == 0f && scale == 1f` 改为 `abs(offsetX) < 0.5f && abs(offsetY) < 0.5f && abs(scale - 1f) < 0.01f`。使用 `import kotlin.math.abs`。

### 3. DataExporter companion 函数

`createShareIntent()` 不依赖实例状态，从 `DataExporter` 实例方法移到 `companion object`。ExportScreen 调用点从 `DataExporter(context).createShareIntent(...)` 改为 `DataExporter.createShareIntent(context, ...)`。

### 4. PDF 缓存清理

在 `generatePdf()` 的 `Task.detached` 中，创建新 PDF 前删除同一年份的旧文件：
```swift
let fileName = "matelink_annual_report_\(year).pdf"
let tempURL = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)
if FileManager.default.fileExists(atPath: tempURL.path) {
    try? FileManager.default.removeItem(at: tempURL)
}
```

## Data Flow

无数据流变更。

## Risks

- 风险极低。所有修改均为删除冗余代码或添加防御性逻辑。
- iOS PDF 清理已在 commit 8700a47 中应用，本次仅验证 Android 侧对应逻辑。
