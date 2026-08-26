---
name: process-feedback
description: Turn raw customer feedback (an email, a support thread, a call transcript) into GitHub issues on piskula/TruckerTracker. Use when the user pastes customer/product feedback and wants it triaged into user stories, bugs, or tasks — with business analysis, app/server/shared impact, feasibility, difficulty, an implementation plan, a test plan, and dependency links to other issues. Not for turning a single already-scoped bug report into one issue — use this when there's real analysis to do across multiple points.
user-invocable: true
argument-hint: "[paste the customer email or feedback text]"
tools: Bash, Read, Grep, Glob
---

# Process Product Feedback

Converts unstructured customer feedback into well-specified GitHub issues that another agent (or a human) can pick up and implement without re-doing the analysis.

## Input

The feedback text is either passed as `$ARGUMENTS` or was just pasted by the user in the conversation. If neither is present, ask the user to paste the email/feedback text before doing anything else.

## Workflow

### 1. Break the feedback into discrete points

Read the whole email first, then split it into individual, atomic points. A single paragraph often contains multiple asks. For each point, classify it:

- **Bug** — something works incorrectly today.
- **Feature request / user story** — new capability, framed as what the user is trying to accomplish.
- **Enhancement** — existing capability, but the customer wants it improved (performance, UX, limits).
- **Question / not actionable** — praise, a question answerable without code changes, or a request out of scope. These do **not** get an issue — track them as "not actioned" with a one-line reason.

### 2. Technical analysis per actionable point

For each bug/feature/enhancement, before writing anything down, actually investigate — don't guess:

- Read `app/AGENTS.md`, `server/AGENTS.md`, `shared/AGENTS.md` (and the relevant module-level `AGENTS.md` files under them) to understand where this would live.
- Grep/Glob the codebase for the relevant screen, endpoint, entity, or DTO to check what already exists vs. needs to be built from scratch.
- Determine **affected areas**: `app`, `server`, `shared` — an item can hit more than one.
- **Feasibility study**: is this technically straightforward, does it need a new third-party integration, a schema/migration change, a platform-specific workaround (iOS vs Android), or design work before implementation can start? Note any open questions or blockers explicitly rather than hand-waving.
- **Difficulty rating** — pick one:
  - `difficulty:small` — isolated change in one module, no schema/API change, low risk, roughly under a day.
  - `difficulty:medium` — touches one layer end-to-end (e.g. a screen + its endpoint), may need a new DTO field or a simple migration, roughly 1–3 days.
  - `difficulty:large` — cross-cutting across app+server+shared, a new domain concept, or a schema/design decision that needs its own breakdown before implementation, multi-day.
- **Implementation plan**: concrete numbered steps — which files/modules are touched, new classes/endpoints/DTOs needed, migration required or not.
- **Test plan**: how this gets verified — unit tests, integration tests, manual steps (which screen, which flow). Be specific enough that someone else can execute it.

### 3. Cross-reference existing issues

Run `gh issue list --state open --json number,title,labels,url --limit 200` and read through titles (fetch bodies with `gh issue view <n>` for any that look related) to find:

- **Duplicates** — an open issue already covers this point. Don't create a new issue; note it in the tracking issue and skip.
- **Dependencies** — this point requires or is blocked by another open issue, or by another point from this same email.
- **Related work** — touches the same area but isn't blocking.

### 4. Ensure labels exist (idempotent)

Create/update the custom labels used below. `gh label create` upserts with `--force`, so this is safe to re-run:

```bash
gh label create "area:app" --color 1f77b4 --description "Affects the KMP client (app/)" --force
gh label create "area:server" --color 2ca02c --description "Affects the Spring Boot backend (server/)" --force
gh label create "area:shared" --color 9467bd --description "Affects the shared DTO/contract module (shared/)" --force
gh label create "difficulty:small" --color c2e0c6 --description "Isolated change, low risk, roughly under a day" --force
gh label create "difficulty:medium" --color fbca04 --description "One layer end-to-end, roughly 1-3 days" --force
gh label create "difficulty:large" --color d93f0b --description "Cross-cutting or new domain concept, multi-day, needs its own breakdown" --force
```

Use the existing default labels (`bug`, `enhancement`, `documentation`, `question`) for the type — don't invent new type labels.

### 5. Create the child issues first

For each actionable point, write the body to a temp file (use the scratchpad directory) and create the issue with `--body-file` (never inline `--body` with multi-line content — quoting breaks on Windows):

Body template:

```markdown
## Summary
<1-3 sentence description of the request/bug, in product terms>

## Customer feedback
> <quoted excerpt from the email this issue is based on>

## Business rationale
<why this matters, expected impact/who's affected>

## Affected areas
<app / server / shared — state which, and why>

## Feasibility
<straightforward vs. needs design/new integration/migration; open questions or blockers>

## Implementation plan
1. ...
2. ...

## Test plan
- Unit: ...
- Integration: ...
- Manual: ...

## Dependencies
- Depends on #N <if any — fill in during step 6>
- Related to #N <if any>
```

Create with:

```bash
gh issue create --title "<Bug: ...|Add ...|Improve ...>" --body-file <tmpfile> \
  --label bug --label "area:app" --label "difficulty:medium"
```

Pick the type label (`bug`/`enhancement`/`documentation`/`question`) and one or more `area:*` and exactly one `difficulty:*` label. Capture each created issue's number (parse it from the URL `gh issue create` prints).

### 6. Backfill dependency links

Now that all child issues have numbers, for any that depend on each other (from this batch, or from step 3's existing-issue cross-reference), run `gh issue edit <n> --body-file <updated-tmpfile>` to fill in the `## Dependencies` section with real `#N` references.

### 7. Create one tracking issue for the whole email

This is the baseline plan another agent builds on — it ties the batch together:

```markdown
## Source
Customer feedback received <date>. <sender/company if given>

## Summary
<2-4 sentence synthesis of what the customer is asking for and why it matters, from a business perspective>

## Items
- [ ] #<n> <title>
- [ ] #<n> <title>

## Not actioned
- <point> — <why: praise / question already answerable / out of scope>

## Dependency notes
- #<n> depends on #<n> because ...
```

```bash
gh issue create --title "Customer feedback: <short description> (<date>)" --body-file <tmpfile>
```

Then edit each child issue to add `Part of #<tracking-number>` under its `## Dependencies` section.

### 8. Report back

Print a short summary: the tracking issue link, each child issue link with its type/area/difficulty labels, and anything filed under "not actioned". Don't restate the full issue bodies — they're on GitHub.
