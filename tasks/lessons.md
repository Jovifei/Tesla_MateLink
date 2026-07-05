# Lessons

## 2026-07-01 - Treat PRD as plan source, git_ref as reference

Pattern: I initially described `docs/git_ref` alongside planning documents in a way that could blur source-of-truth boundaries.

Prevention rule: For Tesla MateLink planning and implementation, treat `docs/PRD` and `docs/PLAN` as the plan/spec source, `openspec` as the change/spec workflow, and `docs/git_ref` only as external reference material. Do not infer project completion or scope from `docs/git_ref` implementation files.
