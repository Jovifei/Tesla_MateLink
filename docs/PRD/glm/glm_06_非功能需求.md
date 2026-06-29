# glm_06 · 非功能需求

## 1. 性能

### 1.1 启动性能

| 指标 | 目标 | 测量方式 |
|---|---|---|
| 冷启动到首屏可交互 | ≤ 2s（iPhone 12 / Pixel 6 级别） | Xcode Instruments / Android Profiler |
| 冷启动到 Dashboard 数据加载完成 | ≤ 3s | 同上 |
| 热启动到首屏 | ≤ 0.5s | 同上 |

### 1.2 运行性能

| 场景 | 指标 |
|---|---|
| Dashboard 刷新（5s 轮询） | 网络请求 ≤ 800ms，UI 更新 ≤ 200ms |
| 列表滚动 | ≥ 55 fps（iOS）/ ≥ 50 fps（Android） |
| 行程详情打开 | ≤ 1s（含地图 + 图表） |
| 3D 车辆渲染 | ≥ 30 fps（低端）/ ≥ 60 fps（高端） |
| 地图缩放/平移 | ≥ 55 fps |

### 1.3 资源消耗

| 资源 | 限制 |
|---|---|
| App 安装包 | ≤ 50MB（iOS）/ ≤ 40MB（Android） |
| 内存占用 | ≤ 200MB（iOS 内存警告阈值） |
| 磁盘缓存 | ≤ 50MB（自动 LRU 清理） |
| 网络流量（5s 轮询 1h） | ≤ 1MB（开启 gzip） |
| 电量（1h 后台运行） | ≤ 2%（iOS）/ ≤ 3%（Android） |

### 1.4 后台性能

| 平台 | 限制 |
|---|---|
| iOS 后台 | 最多 30s 后台拉取，之后由系统接管 |
| Android 后台 | WorkManager 15min 一次 |
| 推送通知 | 不依赖 App 后台运行，由服务端推送 |

## 2. 安全

### 2.1 数据安全

| 数据 | 存储 | 加密 |
|---|---|---|
| Server URL | MMKV | 否 |
| API Token | expo-secure-store | ✅ iOS Keychain / Android Keystore |
| 用户偏好 | MMKV | 否 |
| 车辆数据缓存 | MMKV | 否 |

### 2.2 传输安全

- ✅ 强制 HTTPS（iOS ATS + Android cleartext 禁用）
- ✅ 例外：允许局域网 IP（10.x / 192.168.x / 172.16-31.x）
- ❌ 不支持自签名证书（用户应配 Traefik + Let's Encrypt）
- ✅ Certificate Pinning（v1.1 考虑，需权衡——TeslaMate 实例域名不固定，pinning 不可行）

### 2.3 应用安全

- ✅ 不在 App 中硬编码任何密钥
- ✅ 不在日志中输出 API Token / 车辆数据
- ✅ 不允许 WebView 加载外部 URL（仅加载本地资源）
- ✅ iOS：禁用 `UIWebView`，使用 `WKWebView`
- ✅ Android：`android:allowBackup="false"`，`android:fullBackupContent="false"`
- ✅ 代码混淆：iOS ProGuard / Android R8 启用

### 2.4 隐私

| 原则 | 实现 |
|---|---|
| 不收集车辆数据 | App 不上传任何车辆数据到我们服务器 |
| 不收集用户数据 | 不做用户账号体系 |
| 不追踪用户行为 | PostHog 自托管，仅记录匿名事件 |
| 崩溃报告可选 | 用户可在 Settings 关闭 Sentry |
| 符合 GDPR | 隐私政策明确说明"App 不收集任何用户数据" |
| 符合 CCPA | 同上 |
| 符合中国 PIPL | 同上 + 不出境（PostHog 自托管在国内服务器） |

## 3. 合规

### 3.1 商标合规

| 商标 | 处理 |
|---|---|
| Tesla / Tesla 徽标 | ❌ 不用作 App 主名或图标 |
| Model S/3/X/Y | 仅作为功能描述文字 |
| TeslaMate | ❌ 不作为 App 主名；可用 "for TeslaMate" 副标题 |
| Cybertruck | ❌ 不作为 App 主名 |

### 3.2 App Store 合规

| 要求 | 实现 |
|---|---|
| 免责声明 | 描述含 "Not affiliated with Tesla, Inc." |
| 数据收集声明 | 隐私政策明确"不收集"|
| 使用要求 | 描述含 "Requires self-hosted TeslaMate instance" |
| 车辆类 App 特殊审核 | 强调"只读"，不做车控 |
| 年龄评级 | 4+（无不当内容） |
| IAP | PRO 解锁用一次性 IAP（非订阅） |

### 3.3 Google Play 合规

| 要求 | 实现 |
|---|---|
| 数据安全表 | 填写"不收集数据" |
| 目标 API 级别 | latest（Android 14+） |
| 广告 | 不含广告 |
| 内购 | PRO 解锁用一次性内购 |

### 3.4 中国合规

| 要求 | 实现 |
|---|---|
| ICP 备案 | App 不在中国大陆上架（仅在 App Store / Google Play 全球区） |
| 高德地图 SDK 合规 | 使用高德 SDK 需注册 bundle id + package name |
| 数据本地化 | App 不上传数据，符合"数据不出境"原则 |
| 内容审核 | App 内不含 UGC，无需内容审核 |

### 3.5 开源合规

| 依赖 | 许可证 | 合规要求 |
|---|---|---|
| teslamate (AGPL-3.0) | 仅作为数据源，不修改源码 | 无传染 |
| teslamateapi (MIT) | 仅作为 API，不复制代码 | 无要求 |
| matedroid (需复核) | 借鉴算法思路，不复制代码 | 需在 NOTICE 提及 |
| react-native (MIT) | npm 依赖 | NOTICE |
| three.js (MIT) | npm 依赖 | NOTICE |
| 其他 npm 包 | 各自许可证 | 自动生成 NOTICE |

## 4. 本地化

### 4.1 语言支持

| 语言 | P0 | P1 | P2 |
|---|---|---|---|
| 英语 (en) | ✅ | | |
| 简体中文 (zh-CN) | ✅ | | |
| 繁体中文 (zh-TW) | | ✅ | |
| 日语 (ja) | | ✅ | |
| 德语 (de) | | ✅ | |
| 法语 (fr) | | ✅ | |
| 西班牙语 (es) | | | ✅ |
| 意大利语 (it) | | | ✅ |

### 4.2 单位本地化

| 单位 | 选项 | 默认 |
|---|---|---|
| 长度 | km / mi | 按地区 |
| 温度 | °C / °F | 按地区 |
| 气压 | bar / psi | bar |
| 能量 | kWh / Wh | kWh |

### 4.3 时区

- 默认跟随系统时区
- 用户可在 Settings 手动设置
- 与 TeslaMate 的 `TZ` 环境变量无关（App 端独立处理）

### 4.4 地图本地化

| 地区 | 地图源 |
|---|---|
| zh-CN | 高德地图（GCJ-02） |
| 其他 | Google Maps / Apple Maps（WGS-84） |

### 4.5 字体本地化

- 中文：系统默认（iOS PingFang SC / Android Source Han Sans）
- 英文：Inter 或系统默认
- 数字：JetBrains Mono（赛博朋克主题）/ 系统默认

## 5. 可用性

### 5.1 无障碍（Accessibility）

| 要素 | 要求 |
|---|---|
| 字体大小 | 支持系统 Dynamic Type / Font Scale |
| 颜色对比度 | WCAG AA（4.5:1 文字，3:1 大文字） |
| VoiceOver / TalkBack | 所有可交互元素有 `accessibilityLabel` |
| 触摸目标 | ≥ 44pt × 44pt（iOS）/ ≥ 48dp × 48dp（Android） |
| 减少动效 | 支持系统"Reduce Motion"开关 |
| 颜色不传递信息 | 状态徽章除了颜色还有图标/文字 |

### 5.2 错误恢复

| 场景 | 处理 |
|---|---|
| 网络断开 | Toast 提示 + 离线模式 |
| 服务器不可达 | 重试 3 次后显示全屏错误页 |
| 数据格式错误 | Sentry 上报 + Toast "Data error" |
| App 崩溃 | Sentry 上报 + 重启 App |

### 5.3 用户反馈

| 场景 | 实现 |
|---|---|
| 操作成功 | 轻量 Toast |
| 操作失败 | Toast + "Retry" 按钮 |
| 长耗时操作 | Loading 指示器 + 文字说明 |
| 数据加载 | 骨架屏 |
| 空状态 | 插画 + 引导文字 |

## 6. 可靠性

### 6.1 容错

| 场景 | 处理 |
|---|---|
| API 端点不存在 | 跳过该功能，不崩溃 |
| 字段缺失 | 显示 "—" 或默认值 |
| 字段类型错误 | Sentry 上报 + 默认值 |
| 时区错误 | 回退到 UTC |

### 6.2 数据一致性

| 场景 | 处理 |
|---|---|
| 缓存与服务器不一致 | React Query 自动 invalidate |
| 多车切换 | 清空当前车缓存，重新拉取 |
| 登出 | 清空所有本地数据 |

## 7. 可维护性

### 7.1 代码质量

| 指标 | 目标 |
|---|---|
| TypeScript 严格模式 | ✅ |
| ESLint 错误 | 0 |
| 单元测试覆盖率 | ≥ 60%（核心逻辑 ≥ 80%） |
| E2E 测试覆盖核心流程 | ≥ 5 个 |
| 代码重复率 | ≤ 3% |

### 7.2 文档

| 文档 | 状态 |
|---|---|
| README.md | ✅ 必备 |
| CONTRIBUTING.md | ✅ 必备 |
| ARCHITECTURE.md | ✅ 必备 |
| API.md（自封装的 API client） | ✅ 必备 |
| CHANGELOG.md | ✅ 必备 |

### 7.3 CI/CD

| 流程 | 自动化 |
|---|---|
| PR 检查 | Lint + Typecheck + Test |
| Main 合并 | 自动构建 Preview 版本 |
| Tag 发布 | 自动构建 Production 版本 + 提交商店 |
| 依赖更新 | Dependabot 周更 |

## 8. 兼容性

### 8.1 iOS

| 维度 | 要求 |
|---|---|
| 最低 iOS 版本 | iOS 15.0 |
| 推荐 iOS 版本 | iOS 17+（支持 Swift Charts 等） |
| 设备 | iPhone / iPad（Universal） |
| iPhone 适配 | iPhone SE ~ iPhone 15 Pro Max |
| iPad 适配 | 支持（v1.1） |
| 横屏 | 仅地图/图表页支持 |

### 8.2 Android

| 维度 | 要求 |
|---|---|
| 最低 API | 26（Android 8.0） |
| 目标 API | 34（Android 14） |
| 设备 | 手机 / 平板 |
| ChromeOS | 不主动支持 |
| 横屏 | 仅地图/图表页支持 |

### 8.3 TeslaMate 兼容性

| TeslaMate 版本 | 兼容性 |
|---|---|
| v1.28+ | ✅ 完全兼容 |
| v1.27 | ⚠️ 部分字段可能缺失 |
| < v1.27 | ❌ 不支持 |
| TeslaMateApi v1.21+ | ✅ 必需 |

## 9. 监控与运维

### 9.1 崩溃监控

| 指标 | 目标 |
|---|---|
| 崩溃率 | < 0.5% 会话 |
| ANR 率（Android） | < 0.1% |
| 主线程卡顿（iOS） | < 0.1% |
| 响应时间 | Sentry Issue 24h 内分类 |

### 9.2 用户行为分析

| 事件 | 记录 |
|---|---|
| App 启动 | ✅ |
| 页面浏览 | ✅ |
| 功能使用 | ✅（按功能 ID） |
| 设置变更 | ✅（仅键名，不含值） |
| 错误 | ✅ |
| 车辆数据 | ❌ 不记录 |
| 服务器 URL | ❌ 不记录 |
| API Token | ❌ 不记录 |

## 10. 下一步

- ✅ 本文档完成
- ⏭ 继续 `glm_07_项目计划.md`
