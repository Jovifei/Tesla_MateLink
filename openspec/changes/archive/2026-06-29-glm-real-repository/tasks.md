## 1. 测试基础设施搭建

- [x] Task 1: 添加测试依赖 + TestUtils (commit 2fafe52)

## 2. 数据层映射

- [x] T-001 核对 API Response 与 Entity 字段对齐，实现 toEntity/toDomain 映射 (commit b1f8e85)

## 3. RealCarRepository 实现

- [x] T-002 新建 RealCarRepository.kt (commit 7fbd4e1)
- [x] T-003 Network-First 缓存策略 (commit 7fbd4e1)
- [x] T-004 实现全部 7 个接口方法 (commit 7fbd4e1)

## 4. DI 与运行时切换

- [x] T-005 @RealImpl 绑定 (commit f082371)
- [x] T-006 DelegatingCarRepository (commit f082371)
- [x] T-007 SettingsDataStore useRealDataSource (commit be3638b, fix 8eff618)

## 5. UI 开关

- [x] T-008 SettingsScreen 数据源 Switch (commit 047a26d)

## 6. 测试

- [x] T-009 RealCarRepository API + 缓存测试 (commit b9e669e, 8 tests)
- [x] T-010 API 失败降级测试 (commit b9e669e)
- [x] T-011 DelegatingCarRepository 转发测试 (commit 6d2fe08, 8 tests)
