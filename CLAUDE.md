<!-- code-review-graph MCP tools -->
## MCP Tools: code-review-graph

**IMPORTANT: This project has a knowledge graph. ALWAYS use the
code-review-graph MCP tools BEFORE using Grep/Glob/Read to explore
the codebase.** The graph is faster, cheaper (fewer tokens), and gives
you structural context (callers, dependents, test coverage) that file
scanning cannot.

### When to use graph tools FIRST

- **Exploring code**: `semantic_search_nodes` or `query_graph` instead of Grep
- **Understanding impact**: `get_impact_radius` instead of manually tracing imports
- **Code review**: `detect_changes` + `get_review_context` instead of reading entire files
- **Finding relationships**: `query_graph` with callers_of/callees_of/imports_of/tests_for
- **Architecture questions**: `get_architecture_overview` + `list_communities`

Fall back to Grep/Glob/Read **only** when the graph doesn't cover what you need.

### Key Tools

| Tool | Use when |
| ------ | ---------- |
| `detect_changes` | Reviewing code changes — gives risk-scored analysis |
| `get_review_context` | Need source snippets for review — token-efficient |
| `get_impact_radius` | Understanding blast radius of a change |
| `get_affected_flows` | Finding which execution paths are impacted |
| `query_graph` | Tracing callers, callees, imports, tests, dependencies |
| `semantic_search_nodes` | Finding functions/classes by name or keyword |
| `get_architecture_overview` | Understanding high-level codebase structure |
| `refactor_tool` | Planning renames, finding dead code |

### Workflow

1. The graph auto-updates on file changes (via hooks).
2. Use `detect_changes` for code review.
3. Use `get_affected_flows` to understand impact.
4. Use `query_graph` pattern="tests_for" to check coverage.

<!-- project-init: project overview -->
## 项目概览：Tesla MateLink

Tesla 车辆数据伴侣应用，TeslaMate 自托管记录器的跨平台移动端。详见 `docs/README.md` 与 `docs/01-ARC-系统架构.md`。

### 仓库结构（多 AI 变体并存）

| 目录 | 平台 | 说明 |
|------|------|------|
| `app/` | Android | 主实现骨架 |
| `app_glm/` | Android + iOS(Watch/Widget) + Shared | GLM 变体 |
| `app_mimo/` | Android + iOS + Web + Shared | MIMO 变体 |
| `web_matelink/` | Web | Vite + React + Zustand |

### 技术栈

- Android: Kotlin · Jetpack Compose · Hilt · Room · Retrofit · WorkManager · Glance
- iOS: Swift · SwiftUI · WidgetKit · WatchConnectivity
- Web: TypeScript · React · Vite · Zustand

### 关键约定

- 数据源：Network-First + 缓存降级 + Mock 可切换（`DelegatingCarRepository`）。
- 中国本地化：高德地图 + GCJ-02 坐标转换（原始 WGS-84），测试向量见 `shared/gcj02_test_vectors.json`。
- 分时电价 TOU：成本统计按时段电价。

### 开发命令

- Android: `cd app_glm/android && ./gradlew assembleDebug` / `./gradlew test`
- Web: `cd web_matelink && npm run dev` / `npm run build`
- 探索代码优先用 codegraph / code-review-graph MCP 工具。
