## Context / Goals / Non-Goals

F-105~F-108，3 个独立功能并行实现。

### D-1: 效率评分公式
```
score = min(100, max(0, (estimatedConsumption / actualConsumption) × 100))
```
- estimatedConsumption: 车型 EPA/NEDC 基准值（hardcode = 150 Wh/km）
- actualConsumption: real consumption from drives data

### D-2: 多语言用 AI 翻译非母语
选择：对 ja/de/fr 用 AI 生成翻译（而非人工）。iOS 6 个 Localizable.strings、Android 3 个 strings.xml、Web 3 个 json。不需要专业翻译服务。

### D-3: 访问地区用 MapKit 区域
iOS 用 MKMapView delegate 检测 drive 结束经纬度所属区域。Android 用 Geocoder 反查。

## Tasks

- [ ] T-101 iOS EfficiencyView 评分公式 + UI
- [ ] T-102 Android EfficiencyScreen 评分公式 + UI  
- [ ] T-201 iOS DestinationsView 区域分组
- [ ] T-202 Android DestinationsScreen 区域分组
- [ ] T-301 iOS: ja.lproj + de.lproj + fr.lproj Localizable.strings
- [ ] T-302 Android: values-ja + values-de + values-fr strings.xml
- [ ] T-303 Web: ja.json + de.json + fr.json