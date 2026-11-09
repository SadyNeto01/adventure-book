# How I used AI in this project

I built this project with an AI coding assistant (Claude Code). I used it to deliver faster, but every decision, every review and every commit was mine. This file describes the workflow I followed, and why it kept the code small and readable.

## 1. Context first, code second

Before writing any code I wrote a decision log with:

- the challenge and its six objectives, in order;
- the rules for an invalid book;
- the stack, the data model and the decisions already made (for example: invalid books are rejected and never stored, categories are free text, `gotoId` has no FK and the validator protects it).

I also set the conventions the assistant had to follow:

- package by feature;
- controller → service → repository layers, with the controller only doing HTTP;
- DTOs as records with a static `from(...)`, no MapStruct;
- no Lombok `@Builder` on entities;
- SLF4J logging levels;

With the context written down once, I didn't need long prompts. Each request could be short ("implement the next card") because the rules were already agreed.

## 2. Small, reviewable tasks

I split the work into 14 small GitHub issues on a project board (Todo → In Progress → Done), in the challenge's objective order. Each issue has a description, acceptance criteria and dependencies. I discussed the content of each card with the assistant before creating it, for example:

- paging first, filtering as its own card;
- choosing an option by index instead of `gotoId`;
- one progress per player and book;
- health with no upper cap.

Then the loop for every issue was the same:

1. The assistant implements one issue, and only that issue.
2. The full test suite runs against a real PostgreSQL (Testcontainers).
3. I review the diff and ask questions.
4. I commit only when I'm satisfied. The assistant never commits or pushes without my explicit approval.

One issue per commit kept every change small enough to review properly. It also gives the history a commit message per step, like `filter books by title, author, category and difficulty` or `apply choice consequences`.

## 3. Reviewing instead of accepting

I treated every suggestion as a proposal. Some examples where I changed or rejected what was proposed:

- **Keep it simple.**
  - `POST /books` first returned `201` with a `Location` header built by string concatenation. I removed the header, so it returns `201` with the body, like the other `POST` endpoints.
  - I removed tests that added nothing, such as a "context loads" test.
  - I removed explanatory comments the code didn't need.
- **Better behaviour.** Adding a duplicate category or removing a missing one first returned silent success. I changed this so the client is told: `409` for a duplicate and `404` for a missing category.
- **Naming.** The validator's result list was renamed from `reasons` to `errors`, so it matches the `errors` field in the API response.
- **Domain model.** I defined the whole data model myself:
  - books, with sections keyed by `(book_id, section_id)`;
  - options and consequences stored as JSON;
  - categories as free text;
  - players, and their progress per book.

  The assistant only implemented that model
- **Objective order.** I kept the challenge's objective order even when the assistant suggested doing objective 6 before 5.
- **Deliberate non-features.** The assistant suggested optimistic locking (`@Version`) for concurrent choices. I decided not to implement it, because the challenge didn't require it and it would have added unecessary complexity.

## 4. Understanding everything that ships

I did not accept code I couldn't explain. Whenever something was new or non-obvious, I asked the assistant to explain it until I understood it:

- RFC 9457 `ProblemDetail`;
- Spring Data `Pageable` and `Specification` (`root`, `query`, `cb`);
- `Locale.ROOT`;
- LIKE wildcard escaping;
- JPA dirty checking, i.e. why `@ElementCollection` changes are saved without calling `save()`;
- HTTP status families;
- optimistic locking.


## 5. My own agent orchestrator

For planning and review I used **engineering-team**, a Claude Code plugin I built myself. It works like a small software team. Each role is a separate agent with its own instructions, tools and model, and the main session acts as the orchestrator that runs them in order.

| Agent | Model | Role |
|---|---|---|
| Explorer | Haiku | Maps the repository: stack, commands, packages, conventions. It is cheap, so the expensive models don't spend tokens exploring. |
| Researcher | Haiku | Answers specific API or framework questions from official docs, matched to the project's versions, with citations. |
| Solution architect | Opus | Plans the work as small tasks with acceptance criteria, handles escalations, and does the final architectural check. Read-only. |
| Developer | Sonnet | Implements one approved task, following the plan and the repository's conventions. |
| Tester | Sonnet | Runs the real tests, build and checks. It may only add test files, never production code. |
| Code reviewer | Sonnet | Reviews the diff for correctness, design and over-engineering. Read-only. |
| Security reviewer | Sonnet, or Opus for security-sensitive tasks | Static security review. It blocks critical and high findings. |

How a run works:

1. **Plan.** The explorer maps the repository. The architect turns the request into a plan, and asks me questions when a decision is mine to make. Nothing is built until I approve the plan.
2. **Execute each task** through a fixed chain: develop → test → code review → security review.
   - A failure or a blocking finding sends the task back to the developer, with the report attached.
   - After three correction cycles the task is escalated to the architect, who proposes options. I decide.
   - For each blocking finding I choose: fix it, or accept the risk. An accepted risk is recorded with my reason.
3. **Verify.** A final test run covers the whole change, and the architect checks the result against the plan.

The design keeps the agents cheap and under control:

- **Small context.** Agents pass each other file paths (plan, reports), not pasted text.
- **Resumable runs.** Every report is saved word for word, and the run state lives in a `state.json`, so a run can be resumed after a crash.
- **Guard hook.** A hook runs before every tool call. While a run is active, it:
  - blocks `git push` and opening or merging PRs;
  - blocks destructive commands from the agents;
  - stops read-only agents from writing files, and the tester from changing production code;
  - rejects any commit that carries AI attribution.

How I used it in this project:

- **Breaking down the backlog.** The architect read the decision log and helped turn it into the issues on the board. I aligned each card's decisions with it before the issues were created.
- **An independent review halfway through.** The explorer, tester, code reviewer, security reviewer and architect each reviewed the whole repository, with no changes and no commits.
  - The architect's cross-check mattered. Some reviewer claims were wrong: line numbers that don't exist, and gaps that existing tests already covered. Those were discarded instead of "fixed".
  - The valid findings were folded into the planned work, not added as extra scope. For example, the status guard (`409` when a game is over) and the rule that death wins over reaching an `END` section both went into the consequences issue.

## 6. Guardrails that kept quality high

- **Real tests.** Integration tests run against PostgreSQL 17. They cover:
  - the seed;
  - filters, including LIKE escaping and combined category filters;
  - every 4xx case;
  - full play-throughs, including dying and finishing;
  - pause and resume;
  - all five invalid-book rules.

  Tests that change data put it back, so they don't depend on each other.
- **Constraints in the database.** The schema is owned by Flyway, and Hibernate only validates it. `CHECK`, `UNIQUE` and a composite foreign key protect the data even if the code has a bug.
- **No over-engineering.**
- **Human ownership.**
  - Commits are only allowed by me after revision.

## Result

All six objectives were delivered in 15 focused commits (the initial setup plus one per issue), with 75 integration and unit tests. Each part of the codebase stays small, and the AI handled the typing and the first drafts. Scope, design, review and the final call stayed with me.
