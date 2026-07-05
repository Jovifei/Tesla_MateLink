# 开发指南 — Tesla MateLink

## 环境要求

| 平台 | 工具链 |
|------|--------|
| Android | JDK 17+ · Android Studio · Gradle |
| iOS | Xcode 15+ · Swift 5.9+ |
| Web | Node.js 18+ · npm/pnpm |

## 各变体构建

### Android（app_mimo）

```bash
cd app_mimo/android
# Windows 需指定 Android Studio 自带的 JDK
JAVA_HOME="D:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
# 或直接在 Android Studio 中点击 ▶ Run
```

APK 输出: `app_mimo/android/app/build/outputs/apk/debug/app-debug.apk`

> **注意**: 高德地图功能需要手动下载 SDK（见 TODO-mimo.md A-7）。

### Android（app_glm）

```bash
cd app_glm/android
./gradlew assembleDebug          # 构建 Debug APK
./gradlew test                   # 运行单元测试
./gradlew installDebug           # 安装到连接的设备
```

### iOS

```bash
cd app_glm/ios
# 用 Xcode 打开 MateLink 工程，或：
xcodebuild -scheme MateLink -destination 'platform=iOS Simulator,name=iPhone 15' build
```

### Web（web_matelink）

```bash
cd web_matelink
npm install
npm run dev                      # 本地开发服务器
npm run build                    # 生产构建
```

## 后端依赖

应用依赖自托管 [TeslaMate](https://github.com/adriankumpf/teslamate) 实例提供的 API。开发时可使用各变体 `shared/mock_data.json` 的 Mock 数据，通过设置页的数据源开关切换 Mock/Real。

## 常用工作流

### 代码探索（优先用知识图谱）

```bash
codegraph status                 # 查看 CodeGraph 索引状态
codegraph index                  # 重建索引（编辑大量文件后）
code-review-graph status         # 查看审查图谱统计
code-review-graph build          # 重建审查图谱
```

MCP 工具（在 Claude Code 中）：
- 结构性查询（谁调用谁、定义在哪、改动影响）→ `codegraph_*` / `code-review-graph` MCP 工具
- 字面文本查询（字符串、注释）→ grep/read

### 规格驱动开发

```bash
comet status                     # 查看活跃 change
openspec list                    # 查看 openspec 变更
```

## 坐标与本地化注意事项

- 中国境内地图使用**高德地图 + GCJ-02 坐标系**，TeslaMate 原始数据为 WGS-84，需经 `GCJ02` 工具转换。
- 转换测试向量见各变体 `shared/gcj02_test_vectors.json`。
- 分时电价（TOU）配置见 Android 的 `TariffConfigScreen`。

## 测试

```bash
# Android 单元测试（数据层覆盖：Mappers / Repository / UrlSecurity）
cd app_glm/android && ./gradlew test

# Web
cd web_matelink && npm test
```
