# Voice guide — plain-English analogies & essential theory

This is the style calibration for the two recurring content types in a guide:
`plain` explanations attached to `points`/`steps`, and `theorySteps`/`glossary`
entries. Read this before writing either. The goal both times is the same:
a non-technical student should walk away actually understanding the mechanism,
not just recognizing the vocabulary.

## Plain-English analogies

**The test for a good analogy: it should map onto the ACTUAL mechanism, not
just gesture at a vibe.** A bad analogy is decorative — it sounds nice but
you can't trace the parts of the analogy back to parts of the real thing. A
good analogy is one where, if you push on it, the mapping still holds.

Concretely: before writing a `plain` field, identify (a) what the two or
three moving parts of the real mechanism are, and (b) find an everyday
situation with the same number of moving parts in the same relationship.
Then write the analogy so each real part has an obvious everyday counterpart.

**Worked examples from Week 3** (COMP90018 — UI & WatchOS), for calibration:

| Real mechanism | Moving parts | Analogy | Why it maps |
|---|---|---|---|
| View vs. ViewGroup | a visible widget; an invisible container that arranges widgets | furniture vs. the room | furniture = visible/touchable (View); the room = invisible but decides placement (ViewGroup) |
| `onPause` (DialogActivity, transparent theme) vs. `onStop` (NormalActivity, opaque) | fully covered vs. partially covered | a full projector screen vs. a small note card held up | the note card genuinely doesn't fully block the view — same as the transparent theme |
| Fragment `replace()` | a container view; a swappable piece of content inside it | a picture frame and the photo inside it | the frame (Activity/container) stays; only the photo (Fragment) changes |
| Adapter | raw data; a widget on screen | a factory line stamping out a finished part from raw material | the Adapter doesn't draw anything itself, same as a stamping machine doesn't design the part |
| `convertView == null` vs. reuse (ListView recycling) | building new vs. reusing existing | building a picture frame from raw wood vs. swapping the photo in an existing frame | only ~8 frames ever get built (enough to fill the screen); every scroll after that just swaps photos |
| RecyclerView recycling at scale | many data items; few physical views | a theatre with 10 seats serving 1000 people in shifts | the *seats* (Views) are finite and reused; the *audience* (data) rotates through them |
| Resource qualifiers (`values` vs `values-round`) | one resource name; two possible values, chosen by device shape | a restaurant handing you a different menu insert depending on your table shape | the choice is automatic, made for you, based on a physical property (table shape / screen shape) |
| ListView's whole-list click listener vs. RecyclerView's per-row listener | one listener for many items vs. one listener per item | one guard at a building's front door vs. every room having its own doorbell | the guard can't tell rooms apart; a doorbell is inherently per-room |

**When NOT to add a `plain` field:** if a bullet is already procedural/plain
("Run the module, open Logcat") it doesn't need one — forcing an analogy onto
an instruction that isn't conceptually loaded just adds noise. Only attach
`plain` to bullets that name a mechanism, a callback, an API, or a pattern a
non-technical student wouldn't already have a mental model for.

**Avoid generic/decorative analogies.** "It's like a black box that does
stuff" or "think of it like magic" fail the mapping test — they don't have
enough structure to actually teach anything. If you can't identify the 2-3
moving parts of the real mechanism, you're not ready to write the analogy
yet — go back and make sure you understand the code first.

## Essential theory: `theorySteps` vs. `glossary`

Use **`theorySteps`** (numbered, ordered) when the content is a genuine
*procedure* — a sequence of calls that happen in order and where the order
itself matters (e.g. `beginTransaction().replace(...).addToBackStack(...).commit()`).
Each step's `title` should be the literal code (the method call), and `body`
explains what it does and why it's there — not just restating the method name.

Use **`glossary`** (term/definition pairs, unordered) when the content is a
*cluster* of related vocabulary that doesn't have a strict sequence (e.g.
Adapter, ViewHolder, LayoutManager, `getItemCount()` — these all matter but
there's no "step 1, step 2" relationship between them).

**Bold (`<b>`) only the load-bearing terms** — the ones a student needs to be
able to recall and use, not every noun in the sentence. A good density is
roughly 1-3 bolded terms per sentence, never a sentence where everything is
bold (that defeats the purpose of bolding at all).

**Comparison tables (`compareTable`)** are for when two named things are
being explicitly contrasted (ListView vs. RecyclerView, `add()` vs.
`replace()`, square vs. round). Always end with a "When to use" row if there's
a real practical answer — that's usually the most useful row in the table.

## A note on correctness over cleverness

If you're not sure whether a mechanism works the way you're about to describe
it (e.g. "does ListView's demo also have a per-row click listener, like
RecyclerView's does?"), **check the actual source file before writing the
explanation.** A wrong-but-plausible-sounding explanation is worse than no
explanation — it was a wrong claim about "both click-listener styles on the
ListView" that had to be corrected later in Week 3's guide. When in doubt,
grep the actual repo files rather than assuming symmetry between two similar
demos.
