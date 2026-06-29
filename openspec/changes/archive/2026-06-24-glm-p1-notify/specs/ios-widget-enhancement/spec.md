## ADDED Requirements

### Requirement: Widget 显示车辆图像
iOS Widget SHALL 在 medium size 布局中显示车辆图像背景，默认显示缓存的 car image，无图像时显示车型名占位。

#### Scenario: 有缓存车辆图像
- **WHEN** AppGroup UserDefaults 中有有效的 carImage 缓存
- **THEN** Widget 渲染 Image(uiImage:) 作为背景

#### Scenario: 无缓存车辆图像
- **WHEN** AppGroup UserDefaults 中无 carImage 缓存
- **THEN** Widget 显示 "Tesla" 文字占位，不渲染图像

### Requirement: Widget 显示状态图标行
iOS Widget SHALL 在 medium size 布局中显示锁车/sentry/插电/车内温度四个状态图标。

#### Scenario: 正常显示
- **WHEN** 有状态数据
- **THEN** Widget 显示 lock/unlock 🔒、sentry armed/off 🛡️、plug ⚡、temp 🌡️ 图标行

### Requirement: Widget 显示充电详情
iOS Widget SHALL 在 charging 状态下显示电压/电流/相位/进度条。

#### Scenario: 充电中
- **WHEN** 车辆状态 charging 且 carImage 数据包含 chargerVoltage/chargerActualCurrent/chargePhases/chargeLimitSoc
- **THEN** Widget 显示 "N V / N A / N相" + 充电进度条

### Requirement: Widget Lock Screen 变体
iOS Widget SHALL 支持 lock screen 显示电池百分比 + 续航 + 状态。

#### Scenario: Lock screen
- **WHEN** 添加到 lock screen
- **THEN** Widget 显示简洁布局：电池圆圈 + "%" + "NNN km" + 状态文字