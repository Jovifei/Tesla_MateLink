---
name: using-superpowers
description: Use OMC superpowers (autopilot, ultrawork, ralph, team, ralplan, and more) for guided execution. Invoke when the user wants structured, autonomous, or intensive work modes.
license: MIT
metadata:
  author: omc
  version: "1.0"
---

Use OMC superpowers for guided execution. Each superpower is a distinct mode optimized for a different work style.

---

## Available Superpowers

| Superpower | Trigger | Best For |
|---|---|---|
| `autopilot` | "autopilot" | Autonomous end-to-end execution with minimal user input |
| `ultrawork` | "ulw" | Intensive focused work on a single task |
| `ralph` | "ralph" | Iterative problem-solving — push through blockers |
| `team` | `/team` | Multi-agent orchestration for complex tasks |
| `ralplan` | "ralplan" | Structured planning before execution |
| `deep-interview` | "deep interview" | Requirements gathering through dialogue |
| `ai-slop-cleaner` | "deslop" / "anti-slop" | Clean up low-quality AI-generated code |
| `tdd` | "tdd" | Test-driven development workflow |
| `deepsearch` | "deepsearch" | Thorough codebase exploration |
| `ultrathink` | "ultrathink" | Deep reasoning on complex problems |

---

## Choosing a Superpower

```
┌─────────────────────────────────────────────────┐
│           WHICH SUPERPOWER DO I NEED?           │
└─────────────────────────────────────────────────┘

  What's the task?
       │
       ├─ "Just do it end-to-end" ──────► autopilot
       │
       ├─ "Focus hard on this one thing" ► ultrawork
       │
       ├─ "I'm stuck, keep pushing" ────► ralph
       │
       ├─ "Complex, needs many agents" ─► team
       │
       ├─ "Plan before building" ────────► ralplan
       │
       ├─ "I'm not sure what I want" ───► deep-interview
       │
       ├─ "This AI code is messy" ──────► ai-slop-cleaner
       │
       ├─ "Write tests first" ──────────► tdd
       │
       ├─ "Find everything about X" ────► deepsearch
       │
       └─ "Think deeply about this" ────► ultrathink
```

---

## How Each Superpower Works

### autopilot

Fully autonomous execution. Claude plans, implements, verifies, and reports — with minimal user interaction.

**When to use:**
- Well-defined tasks with clear acceptance criteria
- Routine implementations (CRUD endpoints, boilerplate, migrations)
- When you want to hand off and check back later

**What happens:**
1. Claude explores the codebase
2. Creates a plan
3. Implements step by step
4. Verifies each step
5. Reports results

---

### ultrawork (ulw)

Intensive focused mode. Claude works through a single task with deep concentration, showing progress at each step.

**When to use:**
- A single complex task that needs full attention
- Debugging a tricky issue
- Refactoring a specific module

**What happens:**
1. Claude reads and understands the target area
2. Works through the task methodically
3. Shows progress after each significant change
4. Verifies the final result

---

### ralph

Iterative problem-solving mode. When Claude hits a blocker, it doesn't stop — it finds another way.

**When to use:**
- Tasks with unknown obstacles
- When you've already tried and got stuck
- Problems that require creative workarounds

**What happens:**
1. Claude attempts the direct approach
2. If blocked, analyzes why
3. Tries an alternative approach
4. Repeats until solved
5. Reports what worked and what didn't

---

### team

Multi-agent orchestration. Spawns specialized agents for different aspects of a complex task.

**When to use:**
- Tasks spanning multiple files or domains
- When parallel work would speed things up
- Complex changes needing different expertise (code, tests, docs)

**What happens:**
1. Claude decomposes the task
2. Assigns subtasks to specialized agents
3. Coordinates results
4. Integrates and verifies

---

### ralplan

Structured planning mode. Creates a detailed plan before any implementation.

**When to use:**
- Architectural decisions
- Large refactors
- When you want to review the approach before execution

**What happens:**
1. Claude explores the codebase
2. Identifies constraints and tradeoffs
3. Writes a detailed plan
4. Presents for review
5. Executes after approval

---

## Combining Superpowers

Superpowers can chain. Common patterns:

- **ralplan → ultrawork**: Plan first, then execute with focus
- **deep-interview → ralplan**: Gather requirements, then plan
- **team + tdd**: Parallel agents, each using TDD
- **ralph + ultrawork**: Push through blockers with intense focus

---

## Quick Reference

Say the trigger word or use `/oh-my-claudecode:<name>` to invoke.

```
"autopilot this feature"         → autonomous execution
"ulw fix the auth bug"           → focused intensive work
"ralph get this migration done"  → iterative push-through
"/team refactor the API layer"   → multi-agent orchestration
"ralplan the new dashboard"      → plan before building
"deep interview about the UX"    → requirements dialogue
"deslop this file"               → clean up AI code
"tdd the payment module"         → test-driven development
"deepsearch for all auth code"   → thorough codebase search
"ultrathink about this arch"     → deep reasoning
```

---

## Guardrails

- **Don't overuse team** — simple tasks don't need multiple agents
- **Don't skip planning for complex work** — use ralplan when 3+ files change
- **Don't autopilot critical changes** — stay engaged for security-sensitive code
- **Do verify** — every superpower includes verification; don't skip it
- **Do pick the lightest one** — ultrawork beats team for single-file changes
