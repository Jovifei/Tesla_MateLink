# Lessons

## 2026-07-01 - Treat PRD as plan source, git_ref as reference

Pattern: I initially described `docs/git_ref` alongside planning documents in a way that could blur source-of-truth boundaries.

Prevention rule: For Tesla MateLink planning and implementation, treat `docs/PRD` and `docs/PLAN` as the plan/spec source, `openspec` as the change/spec workflow, and `docs/git_ref` only as external reference material. Do not infer project completion or scope from `docs/git_ref` implementation files.

## 2026-07-05 - Triage external review findings against the live worktree

Pattern: An external review bundled still-valid blockers together with findings that had already been fixed in the current worktree, which makes it easy to overreact and re-open solved issues.

Prevention rule: When a user brings a correction or external audit, first cross-check each headline claim against the live worktree and current task ledger. Split the findings into `still valid`, `already fixed`, and `cannot verify here` before dispatching implementation work.

## 2026-07-08 - Pull before committing

Pattern: I prepared and pushed Git changes without explicitly pulling from the remote immediately before the commit step.

Prevention rule: Before every future commit in `tesla_master`, run a pull step for the target repository first, preferably `git pull --rebase --autostash`, then re-check status and only commit once the local branch is based on the latest remote `main`.
