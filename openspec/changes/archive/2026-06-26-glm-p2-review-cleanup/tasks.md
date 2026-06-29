## 1. SettingsScreen 冗余保存

- [x] T-001 移除 SettingsScreen.kt onSave 中冗余的 saveServerUrl() + saveApiToken() 调用

## 2. Vehicle3dScreen 手势提示

- [x] T-002 将手势提示浮点比较改为 epsilon 比较（abs + 阈值）

## 3. DataExporter companion 函数

- [x] T-003 将 createShareIntent() 移到 DataExporter companion object
- [x] T-004 更新 ExportScreen.kt 调用点

## 4. PDF 缓存清理

- [x] T-005 AnnualReportPDFScreen 生成 PDF 前清理同一年份旧文件
