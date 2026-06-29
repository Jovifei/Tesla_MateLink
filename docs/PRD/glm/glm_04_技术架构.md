# glm_04 · 技术架构

## 1. 技术选型

### 1.1 总体方案：React Native + Expo

**选型理由**：

| 方案 | 优势 | 劣势 | 评分 |
|---|---|---|---|
| **React Native + Expo** ✅ | 跨平台一致 + Expo Build 简化发布 + 主流方案 | iOS 需 Mac 构建；Widget/Watch 需原生桥接 | 9/10 |
| Flutter | 性能优 + 跨平台一致 | 国内 Flutter 生态较弱 + 与原生集成稍复杂 | 8/10 |
| KMP + Compose Multiplatform | 复用 matedroid Kotlin 代码 | iOS Compose 仍在演进 + 学习曲线高 | 7/10 |
| 原生双端（Swift + Kotlin） | 体验最优 + Widget/Watch 原生 | 单人团队工作量翻倍 + iOS 端无开源参考 | 5/10 |

**最终决策**：React Native + Expo，理由：
1. `gwesseling/Tesla-app` 已验证 RN + Tesla UI 可行
2. Expo EAS Build 简化 iOS / Android 双端发布
3. 单人团队可维护，工作量与原生单端相当
4. Widget/Watch 用原生桥接（Swift WidgetKit / Kotlin AppWidgetProvider），主体仍用 RN

**关于 mimo 反驳的回应**：mimo 认为 RN 有性能天花板 + 包体积大 + Widget/Watch 仍要写原生，主张双端原生。
- 部分有理：Widget/Watch 确实需要原生代码（见 §4.6 原生桥接）
- 但双端原生 = 工作量翻倍，对单人团队不可行
- matedroid (Kotlin) 和 t-buddy (Swift) 各自只验证了**单平台**可行，未验证**双端原生**可行
- 现代 RN 0.76+ 新架构性能已大幅改善，对数据展示类 App 足够

#### 1.1.1 双方案对照表（吸收 mimo 的呈现方式）

> Jovi 决策点：根据团队规模选择方案。

| 维度 | **方案 A · 原生双端**<br>(Kotlin + Swift) | **方案 B · RN + Expo**<br>(主推) |
|---|---|---|
| 适用团队规模 | ≥ 2 人（iOS + Android 各 1） | 单人 / 小团队 |
| 主体开发工时 | 双倍（双端各写一遍） | 单份 |
| Widget / Watch | 直接原生写 | 原生模块桥接（见 §4.6） |
| 性能上限 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 包体积 | 15-25MB | 35-50MB |
| 上手难度 | 高（两套技术栈） | 中（TS + 桥接概念） |
| 借鉴现成代码 | matedroid (Kotlin) + Tesla_Clone_Swiftui | gwesseling/Tesla-app (RN) |
| iOS 端开源参考 | 缺乏（仅 UI 克隆，无 TeslaMate 对接） | gwesseling 已验证 RN 可行 |
| 切换/转换成本 | → B 难（重写） | → A 易（核心逻辑保留，UI 重写） |
| **本 PRD 推荐** | 备选 | ✅ **主推** |

**默认采用方案 B**，若 Jovi 决策需要原生双端，本 PRD 的功能需求、信息架构、数据模型部分均通用（仅技术架构和工时估算需要调整）。

### 1.2 技术栈清单

| 层 | 技术 | 版本 | 用途 |
|---|---|---|---|
| **框架** | React Native | 0.76+ | 跨平台 UI |
| **SDK** | Expo | SDK 52+ | 构建/部署/原生模块 |
| **语言** | TypeScript | 5.5+ | 类型安全 |
| **导航** | react-navigation | 6.x | Stack + Tab 导航 |
| **状态** | React Query (TanStack Query) | 5.x | 服务器状态 + 缓存 |
| **本地状态** | Zustand | 4.x | 全局 UI 状态 |
| **存储** | MMKV | latest | 高性能 KV 存储 |
| **安全存储** | expo-secure-store | latest | Token 加密存储 |
| **网络** | fetch + 自封装 client | — | HTTP 请求 |
| **图表** | victory-native | 40.x | 基于 D3 的图表 |
| **3D**（v1.2+） | @react-three/fiber + three | 8.x / 0.169 | 3D 车辆渲染（F-205，MVP 用 2D） |
| **地图** | react-native-maps + 高德 SDK | latest | 国内外地图切换 |
| **图表手势** | react-native-gesture-handler | 2.x | 手势识别 |
| **动画** | react-native-reanimated | 3.x | 60fps 动画 |
| **国际化** | i18next + react-i18next | latest | 多语言 |
| **测试** | Jest + React Native Testing Library | latest | 单测 |
| **E2E** | Detox | latest | 端到端测试 |
| **Lint** | ESLint + Prettier | latest | 代码规范 |
| **CI/CD** | GitHub Actions + EAS Build | — | 自动构建发布 |
| **崩溃监控** | Sentry | latest | 线上错误监控 |
| **分析**（MVP） | Sentry + App Store Connect + Google Play Console | latest | 崩溃监控 + 平台内置分析 |
| **分析**（v1.2+） | PostHog 自托管（可选） | latest | 深度用户行为分析（MVP 阶段不引入） |

### 1.3 禁用技术

- ❌ Redux（过重，React Query + Zustand 足够）
- ❌ Redux Saga（同上）
- ❌ MobX（学习成本高）
- ❌ NativeBase / react-native-elements（已过时）
- ❌ 任何需要 Mac 以外构建 iOS 的方案

### 1.4 未来可扩展（非 v1.0）

| 方案 | 用途 | 何时考虑 |
|---|---|---|
| **Web PWA** | 零安装覆盖"不想装 App 的临时用户"+ 桌面浏览器访问 | v1.2 之后，用 RN Web 或独立 React + Vite 实现 |
| **KMP 共享业务逻辑** | 若未来决定转原生（Swift + Kotlin），可用 KMP 共享数据层 | v2.0 之后再评估 |
| **MQTT 直连** | 替代 5s 轮询，实时性更好 | v1.2 之后，用户反馈有实时性需求时 |
| **CarPlay / Android Auto** | 驾驶中简化界面 | v2.0+，有足够用户请求时 |

## 2. 系统架构

### 2.1 整体架构图

```
┌──────────────────────────────────────────────────────────┐
│                    Mobile App (RN + Expo)                 │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │                   UI Layer (Presentation)              │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │ │
│  │  │Dashboard │ │ Drives   │ │ Charges  │ │ More     │ │ │
│  │  │  Page    │ │  Page    │ │  Page    │ │  Page    │ │ │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ │ │
│  │       │            │            │            │       │ │
│  │  ┌────┴────────────┴────────────┴────────────┴────┐  │ │
│  │  │              Shared Components                  │  │ │
│  │  │  [CarImage] [Map] [Chart] [StatCard] [Skeleton] │  │ │
│  │  └────────────────────────────────────────────────┘  │ │
│  └──────────────────────────────────────────────────────┘ │
│                              │                             │
│                              ▼                             │
│  ┌──────────────────────────────────────────────────────┐ │
│  │              Feature Hooks (Business Logic)            │ │
│  │  useCarStatus / useDrives / useCharges / useBatteryHealth│ │
│  │  useCarList / useDriveDetail / useChargeDetail         │ │
│  └──────────────────────────────────────────────────────┘ │
│                              │                             │
│                              ▼                             │
│  ┌──────────────────────────────────────────────────────┐ │
│  │              Data Layer (State + API)                 │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐            │ │
│  │  │ReactQuery│  │ MMKV     │  │ API Client│           │ │
│  │  │ (Cache)  │  │ (Local)  │  │ (HTTP)   │            │ │
│  │  └──────────┘  └──────────┘  └────┬─────┘            │ │
│  └──────────────────────────────────┼───────────────────┘ │
│                                      │                     │
└──────────────────────────────────────┼─────────────────────┘
                                       │ HTTPS
                                       ▼
              ┌─────────────────────────────────┐
              │   TeslaMateApi (用户自托管)      │
              │   /api/v1/cars/...               │
              │   tobiasehlert/teslamateapi      │
              └──────────┬──────────────┬───────┘
                         │              │
                 ┌───────▼───┐    ┌─────▼─────┐
                 │ PostgreSQL │    │  MQTT     │
                 │ (TeslaMate │    │ Broker    │
                 │  写入)      │    │ (实时)    │
                 └───────┬─────┘    └────┬─────┘
                         │               │
                   ┌─────▼───────────────▼─────┐
                   │   TeslaMate (Elixir)      │
                   │   采集车辆数据 → 写库 + 发布 │
                   └───────────────────────────┘
                                │
                                ▼
                        Tesla Fleet API / 车辆
```

### 2.2 分层职责

| 层 | 职责 | 不做 |
|---|---|---|
| **UI Layer** | 渲染组件、用户交互、动画 | 业务逻辑、数据获取 |
| **Feature Hooks** | 业务逻辑、数据转换、状态管理 | UI 渲染、HTTP 调用 |
| **Data Layer** | API 调用、缓存、持久化 | 业务逻辑 |
| **API Client** | HTTP 请求、认证、错误处理 | 业务逻辑 |

## 3. 项目目录结构

```
tesla_master/
├── src/
│   ├── api/                       # API 客户端
│   │   ├── client.ts              # HTTP 客户端封装
│   │   ├── teslamate.ts           # TeslaMateApi 接口封装
│   │   └── types.ts               # API 响应类型
│   ├── components/                # 通用组件
│   │   ├── CarImage/              # 2D 车辆图（MVP）
│   │   ├── Car3D/                 # 3D 车辆组件（v1.2+，F-205）
│   │   ├── Map/                   # 地图组件
│   │   ├── Chart/                 # 图表组件
│   │   ├── StatCard/              # 统计卡片
│   │   └── Skeleton/              # 骨架屏
│   ├── features/                  # 功能模块
│   │   ├── dashboard/
│   │   │   ├── DashboardPage.tsx
│   │   │   ├── useCarStatus.ts
│   │   │   └── components/
│   │   ├── drives/
│   │   │   ├── DriveListPage.tsx
│   │   │   ├── DriveDetailPage.tsx
│   │   │   ├── useDrives.ts
│   │   │   └── useDriveDetail.ts
│   │   ├── charges/
│   │   │   ├── ChargeListPage.tsx
│   │   │   ├── ChargeDetailPage.tsx
│   │   │   └── useCharges.ts
│   │   ├── battery/
│   │   │   ├── BatteryHealthPage.tsx
│   │   │   └── useBatteryHealth.ts
│   │   ├── updates/
│   │   ├── settings/
│   │   └── onboarding/
│   ├── hooks/                     # 通用 hooks
│   ├── store/                     # Zustand 全局状态
│   ├── i18n/                      # 国际化
│   │   ├── en.json
│   │   ├── zh-CN.json
│   │   └── ...
│   ├── theme/                     # 主题（颜色/字号/spacing）
│   ├── utils/                     # 工具函数
│   │   ├── routeSimplifier.ts     # ★ 借鉴 matedroid
│   │   ├── tripAggregator.ts      # ★ 借鉴 matedroid
│   │   ├── tariffCalculator.ts    # 分时电价
│   │   └── coordinateFixer.ts     # GCJ-02 纠偏
│   ├── mock/                      # Mock 数据
│   │   └── data.json
│   ├── navigation/                # 导航配置
│   └── App.tsx                    # 入口
├── assets/                        # 静态资源
│   ├── 3d/                        # GLTF 车辆模型
│   ├── images/                    # 图片
│   └── fonts/                     # 字体
├── docs/                          # 文档
│   ├── PRD/glm/                   # 本套 PRD
│   └── git_ref/                   # 参考仓库
├── e2e/                           # E2E 测试
├── .github/workflows/             # CI/CD
├── app.json                       # Expo 配置
├── eas.json                       # EAS Build 配置
├── package.json
├── tsconfig.json
└── README.md
```

## 4. 关键模块设计

### 4.1 API Client

```typescript
// src/api/client.ts
import * as SecureStore from 'expo-secure-store';

export class TeslaMateClient {
  private baseUrl: string;
  private token: string | null;

  constructor(baseUrl: string, token?: string) {
    this.baseUrl = baseUrl.replace(/\/$/, '');
    this.token = token ?? null;
  }

  private async request<T>(path: string, init?: RequestInit): Promise<T> {
    const url = `${this.baseUrl}${path}`;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...init?.headers,
    };
    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`;
    }

    const response = await fetch(url, {
      ...init,
      headers,
      signal: AbortSignal.timeout(10_000),
    });

    if (!response.ok) {
      throw new TeslaMateApiError(response.status, await response.text());
    }
    return response.json();
  }

  async getCars(): Promise<Car[]> {
    return this.request('/cars');
  }
  async getCarStatus(carId: number): Promise<CarStatus> {
    return this.request(`/cars/${carId}/status`);
  }
  async getDrives(carId: number, page = 1, limit = 20): Promise<Drive[]> {
    return this.request(`/cars/${carId}/drives?page=${page}&limit=${limit}`);
  }
  async getDriveDetail(carId: number, driveId: number): Promise<DriveDetail> {
    return this.request(`/cars/${carId}/drives/${driveId}`);
  }
  // ... 其他端点
}

export class TeslaMateApiError extends Error {
  constructor(public status: number, public body: string) {
    super(`TeslaMate API error ${status}: ${body}`);
  }
}
```

### 4.2 React Query 集成

```typescript
// src/features/dashboard/useCarStatus.ts
import { useQuery } from '@tanstack/react-query';

export function useCarStatus(carId: number) {
  return useQuery({
    queryKey: ['carStatus', carId],
    queryFn: () => apiClient.getCarStatus(carId),
    refetchInterval: 5_000,        // 5s 轮询
    refetchOnWindowFocus: true,
    staleTime: 3_000,
    retry: 2,
  });
}
```

```typescript
// src/features/drives/useDrives.ts
export function useDrives(carId: number) {
  return useInfiniteQuery({
    queryKey: ['drives', carId],
    queryFn: ({ pageParam = 1 }) => apiClient.getDrives(carId, pageParam),
    getNextPageParam: (lastPage, allPages) =>
      lastPage.length === 20 ? allPages.length + 1 : undefined,
    initialPageParam: 1,
  });
}
```

### 4.3 2D 车辆图（MVP）

> **变更说明**：3D 车辆展示原列 P0，经评估降级为 P2（F-205）。MVP 用 2D 图片方案，借鉴 matedroid 的 `drawable-nodpi/` 实现。

```typescript
// src/components/CarImage/index.tsx
import FastImage from 'react-native-fast-image';

interface CarImageProps {
  modelType: 'model3' | 'modelY' | 'modelS' | 'modelX' | 'cybertruck';
  exteriorColor: string;
  wheelType: string;
  angle?: 'front' | 'side' | 'rear';
}

const colorMap: Record<string, string> = {
  'DeepBlue': '#1E3A8A',
  'RedMultiCoat': '#B91C1C',
  'PearlWhite': '#E5E7EB',
  'MidnightSilver': '#4B5563',
  'SolidBlack': '#18181B',
  'StealthGrey': '#374151',
};

export function CarImage({ modelType, exteriorColor, wheelType, angle = 'side' }: CarImageProps) {
  const imageKey = `${modelType}_${wheelType}_${angle}`;
  const tintColor = colorMap[exteriorColor];

  return (
    <FastImage
      source={images[imageKey]}
      tintColor={tintColor}
      resizeMode={FastImage.resizeMode.contain}
      style={{ width: 300, height: 150 }}
    />
  );
}
```

**资源**：
- 车型轮廓 PNG（透明背景）× 5 车型 × 3 角度 = 15 张
- 用 `tintColor` 动态着色，避免每色一张图
- 总资源大小 < 2MB

**3D 升级路径**（v1.2，F-205）：
- 若用户反馈希望 3D，启用 Three.js + R3F
- 用户在 Settings 切换 2D/3D
- 低端机默认 2D，高端机可选 3D
- 3D 实现见原方案（GLTF + 动态材质）

### 4.4 地图组件（国内外切换）

```typescript
// src/components/Map/MapView.tsx
import { useLocale } from '@/hooks/useLocale';

export function MapView({ coordinates, ...props }: MapViewProps) {
  const locale = useLocale();
  const isChina = locale.startsWith('zh-CN');

  // 国内地图需要 GCJ-02 纠偏
  const fixedCoords = isChina
    ? coordinates.map(c => wgs84ToGcj02(c.lat, c.lng))
    : coordinates;

  if (isChina) {
    return <AmapView coordinates={fixedCoords} {...props} />;
  }
  return <ReactNativeMapsView coordinates={fixedCoords} {...props} />;
}
```

### 4.5 离线缓存

```typescript
// src/api/cache.ts
import MMKV from 'react-native-mmkv';

const storage = new MMKV({ id: 'teslamate_cache' });

export function cacheSet<T>(key: string, value: T, ttlMs: number = 24 * 3600_000): void {
  storage.set(key, JSON.stringify({
    value,
    timestamp: Date.now(),
    ttlMs,
  }));
}

export function cacheGet<T>(key: string): T | null {
  const raw = storage.getString(key);
  if (!raw) return null;
  const { value, timestamp, ttlMs } = JSON.parse(raw);
  return value;  // 即使 TTL 过期，仍返回数据（标记 stale）
}

export function isStale(key: string): boolean {
  const raw = storage.getString(key);
  if (!raw) return true;
  const { timestamp, ttlMs } = JSON.parse(raw);
  return Date.now() - timestamp > ttlMs;
}
```

### 4.6 原生模块桥接（Widget / Watch）

> **回应 mimo 反驳**：mimo 指出 Widget/Watch 仍需写原生代码，这点正确。但 RN 主代码 + 原生桥接是主流模式（Instagram/Discord/Shopify 均如此），不需要全盘原生。

| 功能 | iOS 原生 | Android 原生 | RN 桥接方式 |
|---|---|---|---|
| **桌面 Widget**（F-103, P1） | Swift + WidgetKit | Kotlin + AppWidgetProvider | App Groups（iOS）/ SharedPreferences（Android）共享数据 |
| **Apple Watch**（F-308, P3） | SwiftUI + WatchConnectivity | N/A | WatchConnectivity 从 RN 主 App 推送数据 |
| **Live Activity**（iOS 16+） | Swift + ActivityKit | N/A | 通过 Native Module 触发 |
| **推送通知**（F-104, P1） | APNs（原生） | FCM（原生） | 通过 Expo Push Notifications 或自建原生模块 |

**数据共享机制**：

```typescript
// iOS: App Group 共享存储
import * as SecureStore from 'expo-secure-store';

// RN 端写入
await SecureStore.setItemAsync(
  'widget_car_status',
  JSON.stringify({ battery: 78, range: 312, state: 'charging' }),
  { keychainAccessible: SecureStore.WHEN_UNLOCKED }
);

// iOS Widget 端读取（Swift）
let sharedDefaults = UserDefaults(suiteName: "group.com.yourcompany.teslamate")
if let data = sharedDefaults?.data(forKey: "widget_car_status") {
    let status = try JSONDecoder().decode(CarStatus.self, from: data)
}
```

```typescript
// Android: SharedPreferences 共享
// RN 端写入（通过 Native Module）
NativeModules.WidgetBridge.updateStatus({
  battery: 78,
  range: 312,
  state: 'charging',
});

// Android Widget 端读取（Kotlin）
val sharedPref = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
val battery = sharedPref.getInt("battery", 0)
```

**工作流**：
1. RN 主 App 定时拉取 status（5s 轮询）
2. RN 写入共享存储
3. iOS WidgetKit 每 15min 读取共享存储刷新 UI
4. Android WorkManager 每 15min 触发 Widget 更新

**工作量估算**：
| 模块 | 工时 |
|---|---|
| iOS Widget（Small + Medium） | 3d |
| Android Widget（2x2 + 4x2） | 2d |
| RN 共享存储桥接 | 1d |
| Apple Watch（若做） | 5d |

## 5. 状态管理

### 5.1 全局状态（Zustand）

```typescript
// src/store/settings.ts
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { MMKVStorage } from '@/utils/mmkvStorage';

interface SettingsState {
  serverUrl: string | null;
  apiToken: string | null;
  currentCarId: number | null;
  theme: 'system' | 'light' | 'dark';
  units: 'km' | 'mile';
  timezone: string;
  language: string;
  mockMode: boolean;
  // P1
  mapProvider: 'auto' | 'amap' | 'google';
  tariffEnabled: boolean;

  setServer: (url: string, token?: string) => void;
  setCurrentCar: (carId: number) => void;
  // ...
}

export const useSettings = create<SettingsState>()(
  persist(
    (set) => ({
      serverUrl: null,
      apiToken: null,
      // ...
    }),
    {
      name: 'settings',
      storage: createJSONStorage(() => MMKVStorage),
    }
  )
);
```

### 5.2 服务器状态（React Query）

React Query 自动管理：
- 缓存
- 重新拉取（`refetchOnWindowFocus`, `refetchInterval`）
- 加载/错误状态
- 分页（`useInfiniteQuery`）
- 乐观更新（如需要）

## 6. 构建与发布

### 6.1 Expo EAS Build

```json
// eas.json
{
  "cli": { "version": ">= 5.0.0" },
  "build": {
    "development": {
      "developmentClient": true,
      "distribution": "internal"
    },
    "preview": {
      "distribution": "internal",
      "env": { "APP_ENV": "preview" }
    },
    "production": {
      "env": { "APP_ENV": "production" },
      "autoIncrement": true
    }
  },
  "submit": {
    "production": {
      "ios": {
        "appleId": "jovi@example.com",
        "ascAppId": "YOUR_APP_ID",
        "appleTeamId": "YOUR_TEAM_ID"
      },
      "android": {
        "serviceAccountKeyPath": "./google-service-account.json",
        "package": "com.yourcompany.teslamate"
      }
    }
  }
}
```

### 6.2 CI/CD（GitHub Actions）

```yaml
# .github/workflows/build.yml
name: Build & Test
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20' }
      - uses: expo/expo-github-action@v8
        with: { expo-version: latest }
      - run: npm ci
      - run: npm run lint
      - run: npm run typecheck
      - run: npm test -- --coverage

  build:
    needs: test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: npx eas-cli build --platform all --profile production --non-interactive
        env:
          EXPO_TOKEN: ${{ secrets.EXPO_TOKEN }}
```

## 7. 性能优化

### 7.1 启动性能

| 优化点 | 目标 |
|---|---|
| 首屏可交互 | < 2s（中端机型） |
| Bundle 体积 | < 10MB（JS bundle） |
| 原生模块懒加载 | Maps / Three.js（v1.2+） 动态 import |

### 7.2 运行性能

| 优化点 | 实现 |
|---|---|
| 列表虚拟化 | `FlashList` 替代 `FlatList` |
| 图片缓存 | `expo-image` 自动缓存 |
| 3D 渲染 | 低端机降级为 2D |
| 轨迹抽稀 | 借鉴 matedroid 的 RouteSimplifier |
| React Query staleTime | 避免重复请求 |

### 7.3 内存管理

| 场景 | 限制 |
|---|---|
| 2D 车辆图 | ≤ 5MB |
| 3D 车辆（v1.2+） | ≤ 30MB |
| 地图 | ≤ 50MB |
| 图片缓存 | ≤ 100MB |
| 总内存 | ≤ 200MB（iOS 内存警告阈值） |

## 8. 监控与可观测性

### 8.1 崩溃监控（Sentry）

```typescript
// src/utils/sentry.ts
import * as Sentry from '@sentry/react-native';

Sentry.init({
  dsn: 'YOUR_SENTRY_DSN',
  environment: __DEV__ ? 'development' : 'production',
  tracesSampleRate: 0.1,  // 10% 性能采样
});
```

### 8.2 用户行为分析

**MVP 阶段**（简化方案，回应 mimo 反驳）：
- **Sentry**：崩溃 + Session Replay（用户可选）
- **App Store Connect**：内置下载量/留存/崩溃率
- **Google Play Console**：内置下载量/留存/ANR/崩溃率
- 不引入 PostHog，避免自托管服务器成本

**v1.2+ 可选**（当用户量 > 5000 时再考虑）：
- PostHog 自托管，做深度漏斗分析
- 仍坚持"不收集车辆数据"原则
- 收集的事件：
  - 页面浏览
  - 按钮点击（仅功能 ID）
  - 设置变更（仅键名）
  - 错误
- **永不收集**：车辆数据、TeslaMate URL、API Token

### 8.3 日志

- 开发期：`react-native-log-box` 显示完整日志
- 生产期：写入本地文件 + Sentry breadcrumb
- 用户可主动导出日志（用于 Bug 报告）

## 9. 下一步

- ✅ 本文档完成
- ⏭ 继续 `glm_05_数据模型与API.md`
