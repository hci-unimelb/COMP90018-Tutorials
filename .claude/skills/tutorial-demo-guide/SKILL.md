---
name: tutorial-demo-guide
description: Builds an interactive, timed HTML "live demo guide" for a COMP90018 tutorial week — a run-sheet with a pace timer, real code snippets pulled from that week's numbered module folder in this repo, plain-English analogies for non-technical students, essential-theory panels (bolded key terms), comparison tables, and hand-built interactive previews where they genuinely fit — then publishes it as a Claude Artifact. Use this whenever the user gives a week number plus that week's slide deck (PDF or otherwise) and wants a demo guide, teaching guide, live demo guide, run sheet, or lesson plan for the tutorial — even if they don't say "skill," "artifact," or reference this by name. Also trigger on "make the Week N guide," "same as last week/Week 3," "turn these slides into a guide," or "I have the Week N slides, can you do the thing again." Do NOT trigger for requests to just summarize or explain a slide deck's content in plain chat with no page/artifact implied.
---

# Tutorial Demo Guide

## What this produces

A single self-contained HTML page: a sticky pace bar with a running timer synced to
per-block time budgets, a series of talk/demo blocks each with talking points,
real code snippets, plain-English explanations, essential-theory panels, and
(sometimes) a hand-built interactive preview simulating what the code produces
on screen — so the presenter can rehearse or teach directly from the page
without switching to Android Studio or an emulator.

This was built once already for Week 3 (User Interface & WatchOS) over several
rounds of real feedback — the engine, the visual design, and the voice are
already validated. **Don't redesign from scratch.** Reuse the bundled engine
template and voice conventions; only the per-week *content* changes.

## Inputs needed

1. **The week number** (e.g. "4"). If not given, ask.
2. **That week's slide deck** — a PDF is typical, but any file/description of
   the taught content works. If it's a large PDF, read it in page-range
   chunks (e.g. `pages: "1-15"`) rather than trying to read it all at once.
3. Everything else — the matching repo module(s), the time budget, the block
   breakdown — you work out yourself per the steps below. Only stop to ask
   the user if something is genuinely ambiguous (e.g. two different modules
   could plausibly match the week number, or the slides describe something
   that isn't in this repo at all).

**Defaults** (established with the user, don't re-ask unless they want to
change them for a specific week):
- Session length: **45 minutes** unless the user specifies otherwise.
- Interactive previews: build one **only when a block's content naturally has
  something worth simulating** — a state machine, a toggle demonstrating a
  resolution mechanism, a layout mock. Don't force one onto a block that's
  purely conceptual; a glossary/theory panel serves those better.
- Always **browser-test before publishing** (see Step 6) — this has already
  caught a real charset bug once, so don't skip it to save time.

## Step 1 — Find the matching module(s)

This repo's tutorial modules are top-level folders named `<week>-<seq>-<topic>`
(e.g. `3-1-activitylifecycle`, `3-2-layoutdemo`, `3-3-watch` for Week 3). List
the repo root and find every folder starting with `"<week>-"`. A week can have
one module or several — Week 3 had three, each covering a different chunk of
the slide deck. If nothing matches, or more than one plausible reading exists,
ask rather than guessing.

## Step 2 — Read the slides, then verify against the real code

Read the deck to understand the topics and their order. Then, for each
concept the deck covers, **read the actual source files in the matching
module** — don't transcribe the slides verbatim. Two things commonly go wrong
if you skip this:

- **Slides show a stale pattern.** Week 3's slides showed `WearableActivity` +
  `setAmbientEnabled()`, but the repo's actual code uses a plain `Activity` —
  a genuinely useful thing to flag as a `callout`, not something to silently
  paper over.
- **A slide's claim doesn't hold for THIS repo's specific implementation.**
  A slide showing "two ways to attach a click listener" doesn't mean both
  ways exist on the same widget in the code — it might mean one demo uses
  approach A and a different demo uses approach B. Grep the actual files
  (`setOnClickListener`, `setOnItemClickListener`, etc.) before writing a
  bullet that claims both exist somewhere specific.

Every code snippet you put in the guide must be verbatim from the real file —
never invented, never paraphrased into pseudo-code.

## Step 3 — Build the block breakdown

Decide the ordered list of blocks (talk vs. live-demo) and give each an
explicit minute allocation. The running totals (`cumEnd`) must sum exactly to
the session length — no unaccounted slack. Reserve the **last block** for
wrap-up/Q&A and fold any timing buffer into it, rather than leaving it as a
separate untracked cushion.

A reasonable split mirrors Week 3's: a short roadmap opener, alternating
talk/demo blocks matching the deck's sections, weighted so the blocks with
the richest hands-on content (adapters, a live state-machine demo) get the
most minutes, ending in a wrap-up that recaps via discussion questions rather
than re-lecturing.

## Step 4 — Write the content

Read **`references/voice-guide.md`** now if you haven't already this session —
it has the calibration for `plain` analogies and the `theorySteps`/`glossary`
distinction, with worked examples. Follow its guidance, don't just imitate
Week 3's specific analogies (a new week's concepts need their own analogies
grounded in what THAT code actually does).

For each block, decide which fields it actually needs — not every block needs
every field:

- `points` (talk) or `steps` (demo) — always.
- `plain` companion on any bullet naming a mechanism/API/pattern a
  non-technical student wouldn't already have a model for. Skip it on purely
  procedural bullets ("open this file").
- `theorySteps` for a genuine ordered procedure; `glossary` for a cluster of
  related vocabulary; `compareTable` when two named things are explicitly
  being contrasted. A block can have zero, one, or (rarely) more than one of
  these — don't force all three onto every block.
- `codes` — real snippets, with a one-line `note`. Reserve `highlight: true`
  for the one or two snippets per guide that are the actual root-cause/
  mechanism behind a "why does X happen" question (Week 3 used it for the
  manifest line explaining why a dialog theme changes lifecycle behavior).
- `callout` only for a genuine gotcha/mismatch — don't manufacture one.
- `ask` only when there's a concrete, checkable answer worth pausing for.

## Step 5 — Interactive previews (when they fit)

Skip this step entirely for blocks where nothing concrete would be gained by
simulating it — a glossary or theory panel is the right amount of interactivity
for a purely conceptual block. Build one when the real demo genuinely involves
clicking through states/screens that can be faithfully approximated in
HTML/CSS/JS: a lifecycle state machine with a fake Logcat, a layout mock
showing what an XML file produces, a toggle demonstrating a resource-qualifier
resolution, a tappable list/grid wired to the real data. Faithful means: if a
teacher clicked through the fake version, nothing about the underlying
mechanism should be misrepresented.

Each preview is bespoke — write a new `renderXxx(id)` function in the
"NEW INTERACTIVE PREVIEWS" section at the bottom of the script (see
`assets/engine-template.html`), scope its state to closures (don't leak
globals if the page ends up with more than one preview), and give it its own
CSS above the "ADD ANY NEW PREVIEW-SPECIFIC CSS" marker.

## Step 6 — Assemble, encode, and test

1. Copy `assets/engine-template.html` to a working path (e.g. the session
   scratchpad). Replace the header text, the `DATA` array, the
   `STORE_KEY` (bump the week number so one week's checked-off progress
   doesn't bleed into another's localStorage), and add any preview functions.
2. Run the bundled entity-safety script as the last edit before testing:
   `python3 <skill-dir>/scripts/entity_safe.py <path-to-html>` — converts
   every non-ASCII character (em-dashes, curly quotes, emoji) to a numeric
   HTML entity. This guards against mojibake if the page is ever served
   without an explicit UTF-8 charset header (a bare `python3 -m http.server`
   does exactly this, which is also what you'll use for local testing next).
3. **Always browser-test before publishing.** Create a temporary
   `.claude/launch.json` pointing a `python3 -m http.server` at the file's
   directory, `preview_start` it, `navigate` there with a cache-busting query
   string (`?v=N`) if you're re-testing after an edit, check
   `read_console_messages` for JS errors, and click through every new
   interactive widget to confirm it actually works (state transitions, toggles,
   tab switches). **Delete the temporary `launch.json` once done** — it's
   scaffolding, not part of the deliverable.

## Step 7 — Publish

Load the `artifact-design` skill (required before any Artifact publish) —
since this repo already has an established design system (the engine
template's tokens), it should confirm "honor what's already there" and let
you proceed without redesigning. Then publish via the `Artifact` tool:

- **New week → new artifact.** Use a fresh file path so it mints a new URL —
  don't overwrite a previous week's guide unless the user is explicitly asking
  to revise that specific week.
- **Iterating on the same week within the same conversation** → republish the
  same file path to update it in place (same URL).
- Title: `"Week N — Live Demo Guide"`. Description: one sentence naming the
  week's actual topic and what the page offers (timer, snippets, previews).
- Favicon: one or two emoji fitting **that week's topic** specifically — don't
  reuse a previous week's favicon; each week is a genuinely new artifact, and
  the emoji is how the user's tab picker tells them apart at a glance.

Tell the user what's in it, and flag anything from Step 2 that turned out to
be a stale-slide/current-code mismatch worth knowing about before they teach
from it.
