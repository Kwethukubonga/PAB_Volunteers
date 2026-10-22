# Handover: what's done and what's left

PAB Volunteers (Android) and the Help Centre (web). Updated 22 October 2026.

Task 2 got 176/270 (65%). Most marks were lost on the **web app** (APIs, Database, Data Flow, Security, CI/CD). Most of that is now fixed on web `develop`, but **none of it is live yet** because Railway only deploys `main`.

**Repos:** Android `Kwethukubonga/PAB_Volunteers` · Web `Kwethukubonga/PAB_WebApp`. Clone both into the same folder.
**Full checklist:** [`IMPROVEMENT_PLAN.md`](IMPROVEMENT_PLAN.md) in this folder (100 items: 39 done, 10 part done). Each item says how to do it and how to know it's finished.

> ⚠️ **These notes live only on the `changes_final_POE` branch**, which exists in both repos with the same `handover/` folder and a `changes_POE.md` project guide. They are for the team only, never go into `develop`, `main` or `master`, and **both `changes_final_POE` branches are deleted before the final submission** (section 5).

## Teams

| Team | People | Repo | Sections |
|---|---|---|---|
| **Web** | Unathi, Nuha, Ash | `PAB_WebApp` | 2 and 4 |
| **Android** | Tlhogi, Kwethu | `PAB_Volunteers` | 3 and 4 |

Within your team, put **your own name** in the Owner column for each item you take, so every person has whole items of their own (the marker grades individual contribution). Items marked "Both" in the plan are split in section 4: each team does its own app's part.

**Who to ask:** most of what's on web `develop` was built by **Kwethu** (API, service layer, design tokens, built CSS, images, Docker) and **Tlhogi** (forms and validation, rate limiting, security headers, staff area, references, CI deploy). Both are on the Android team now but can explain their web code.

## Picking this up with Claude Code

Each repo's `changes_final_POE` branch has `changes_POE.md`: what the project is, the stack, commands, and the team's rules. Claude Code doesn't load it by itself, so you point Claude at it. To carry on:

1. Clone both repos side by side and run `git fetch`.
2. Start your work from `develop`: `git checkout develop`, `git pull`, `git checkout -b feature/<name>/<topic>`. **Never branch from or merge `changes_final_POE`.**
3. Open Claude Code in your team's repo and say something like *"Read changes_POE.md and handover/HANDOVER.md from the changes_final_POE branch (use git show origin/changes_final_POE:<file>), then pick up job W1"*.
4. PR into `develop`, and have someone else review.
5. To put your name in an Owner column or tick an item: `git checkout changes_final_POE`, edit, commit, push. Make the same change in the other repo's copy so both stay identical.

> **Merging PRs:** use **"Create a merge commit"**, not "Squash and merge". Squashing already rewrote one person's commits under another name; the marker grades individual contribution from the history.
>
> **No AI signatures:** no `Co-Authored-By: Claude`/Copilot lines in commits or PRs. `changes_POE.md` tells Claude this, but also turn off commit attribution in your own Claude Code settings (the `attribution` setting, or `"includeCoAuthoredBy": false` in older versions) and read each commit message before you push. AI use is declared once, in each README's AI Usage Declaration.

---

## 1. Where things stand

| Repo | Branch | State |
|---|---|---|
| Web | `main` | Old version (4 Oct). This is what Railway serves. |
| Web | `develop` | All the new work. 185 tests pass, coverage reported, README restored. Ready to go to `main` once W1 is done. |
| Both | `changes_final_POE` | These notes and `changes_POE.md`. Never merge; delete before the final submission. |
| Web | `revert-7-feature/api` | Made with GitHub's Revert button on PR #7, never opened as a PR. **Don't merge it**: it would undo all the new web work. Delete it if nobody needs it. |
| Web | `feature/database-persistence`, `feature/update-programme-images`, `unathi` | Old, and they carry a stray `.claude/launch.json`. Delete if finished with. |
| Android | `develop` | Same as `master` plus new rules tests (40 pass). All 17 requirements (PV-FR01–17) implemented. |
| Android | `feature/kwethu/design-tokens` | Shared palette + header contrast fix (E1, F9). **No PR yet.** Its commit message has a `Co-Authored-By: Claude` line; reword it before the PR. |

### Done on web `develop`

- **Security:** server-side validation on both forms (SA phone rules, length limits), rate limiting (5 posts / 10 min per IP, API included), honeypot, strict security headers and CSP, 64 KB request limit on the forms, `Situation` and `Extra` encrypted at rest, retention clean-up, request logging with no personal data
- **Data:** unique `PAB-XXXXXX` references, schema constraints, staff notes, ready for Postgres (needs only a connection string)
- **Staff area** `/Staff/Requests`: PAB admins sign in with their Android admin account and move requests New → In progress → Closed
- **Shared API:** `/api/support-requests`, `/api/contact`, `/api/programmes`, `/api/resources`; OpenAPI at `/openapi/v1.json`; one error format (problem details)
- **Service layer:** no page touches the database directly
- **Design:** one palette in `design/tokens.json`, Tailwind built at compile time, self-hosted fonts, real icons instead of emoji, WebP images with lazy loading
- **Accessibility:** AA contrast, 44px touch targets, focus moves between form steps, step name shown on phones, `autocomplete`/`required` on fields
- **Safety/UX:** quick exit button (or Esc twice), loading state on Submit, confirmation page with "Copy reference", emergency numbers work offline
- **CI/CD:** tests, coverage, CSS-matches-tokens check, Docker build, deploy to Railway from `main`, `/health` check, Dependabot
- **Docs:** README section 2 maps every requirement to its page, code and test

### Done on Android

- All 17 requirements, 3 languages, light and dark themes, CI with unit tests, lint, rules tests and an APK build
- Rules tests for joining and leaving (the sign-up and the spot counter must change together; no overbooking, no signing others up)

---

## 2. Web team: Unathi, Nuha, Ash

### W1 · Go live: merge web `develop` → `main` · Owner: ______
Do the setup first:

| # | Setup step | Done |
|---|---|---|
| S1 | Firebase console → Project settings → add a **Web app** → put its API key in Railway as `Firebase__ApiKey` | ☐ |
| S2 | Firebase console → Authentication → Settings → Authorised domains → add the Railway domain | ☐ |
| S3 | GitHub (web repo) → Settings → Secrets → `RAILWAY_TOKEN` and `RAILWAY_SERVICE`; then turn off Railway's own auto-deploy so only CI deploys | ☐ |
| S4 | Railway variables: check `TrustProxy=true` (the Dockerfile sets it) | ☐ |
| S5 | Railway volume: the app now runs as a non-root user (UID 1654). If it can't write `/data/philisa.db` after deploy, make the volume writable by that user | ☐ |
| S6 | After deploy: `/health` says Healthy; submit a test request; sign in at `/Staff/SignIn` with an admin account and see it | ☐ |

Staff sign-in has only been tested with fake tokens, never against the real Firebase project, so S6 matters. S1 and S2 need someone with Firebase access (ask the Android team).

### W2 · Translation check · Owner: ______
Check the isiXhosa and Afrikaans text on the new pages, form errors, the 429 page and the staff area (ideally a first-language speaker). Every string is in `s.Pick(en, xh, af)` calls.

### Web backlog

| ID | Pri | Task | Owner |
|---|---|---|---|
| C1 | P1 | Provision Postgres on Railway (code is ready: `Data/DatabaseProvider.cs`), set the connection string, turn on backups, test a restore | |
| D4 | P2 | Branch protection on web `develop` and `main` (CI must pass + 1 review). Dependabot and the badge are done | |
| W12 | P2 | Staff list: New / In progress / Closed tabs with counts, relative times, confirm before closing | |
| A8 | P2 | Move programmes out of `Data/SiteData.cs` and UI text out of `s.Pick(...)` into content / `.resx` files | |
| C4 | P2 | Free uptime monitor (e.g. UptimeRobot) on `/health`; put the uptime figure in the README | |
| D5 | P2 | Tag a release (`v1.1.0`) when `develop` goes to `main`, with notes from the merged PRs | |
| H4 | P3 | Lighthouse CI on PRs, with minimum Performance and Accessibility scores | |
| G7 | P2 | Edge cases: very long names, emoji in every field, fast repeated Submit | |
| J4 | P2 | PR template for the web repo (what changed, how it was tested, screenshots) | |

---

## 3. Android team: Tlhogi, Kwethu

### A1 · Palette PR · Owner: ______
Open a PR from `feature/kwethu/design-tokens` into Android `develop` (reword the commit to drop the Claude line first). It changes only `values/colors.xml` and fixes the header contrast (F9). Check the night colours (`values-night/colors.xml`) still sit well next to the new light ones, and that Android CI passes.

### A2 · Firebase housekeeping · Owner: ______
- Publish the latest `firestore.rules` to the live project (the rules tests pass)
- Remove the duplicate test user in Firebase Auth (uid starting `ZQxVoCX6`)
- Firebase Support ticket about editing the password-reset email template (I8)
- Give the web team Firebase access for W1 (S1, S2)

### Android backlog

**P0, the marker's points:**

| ID | Task | Owner |
|---|---|---|
| D2 | CI deploys the download page (`public/`) and `firestore.rules` on merge to `master`, using a service account secret in GitHub (never in the repo) | |
| C3 | Put the APK download page live, link it in the README, test on a phone | |
| J6 | README: download page link, rules tests, anything else that changed | |

**P1:**

| ID | Task | Owner |
|---|---|---|
| J1 | Requirements table like the web README's section 2 (PV-FR01–17 → screen → code → test) | |
| F10 | Navigation rail on tablets instead of the bottom bar | |
| F12 | Check large text (1.3×) and dark mode in all 3 languages, fix clipping | |
| K1–K4 | Settings as list rows, change password from Settings, two-step delete account, offline banner | |
| K16 | Google sign-in through Credential Manager (removes the deprecated API) | |

**P2:**

| ID | Task | Owner |
|---|---|---|
| E3, E4 | Same fonts as the web (Lora + Source Sans 3 in `res/font`), remove eyebrow labels | |
| E9, E6, K7 | Material 3, solid header instead of gradient, tinted neutrals | |
| G1, G2, K5, K6 | Snackbars instead of Toasts, buttons on empty screens, Retry on errors, main action pinned at the bottom | |
| F11, F13, F14 | Predictive Back, TalkBack pass, `ListAdapter` + `DiffUtil` | |
| I1–I5, K8–K10, K12–K14 | Reminders, "only my programmes", add to calendar, contact PAB, privacy/POPIA, intro screens, home planner, schedule tabs, safety sheet, fingerprint lock | |
| D4 | Branch protection on Android `develop` and `master`, Dependabot, CI badge | |
| D5 | Tag releases and attach the APK to a GitHub Release | |
| C4 | Uptime monitor on the download page | |
| G7, J4 | Edge-case testing, PR template for the Android repo | |

**P3:** E7, E10, G3, I6, I7 (needs Firebase's paid Blaze plan), K11, K15, K17, K18. See the plan for details.

---

## 4. Shared: each team does its part

| ID | Web team (Unathi, Nuha, Ash) | Android team (Tlhogi, Kwethu) |
|---|---|---|
| **B3** Android uses the web API | Keep `GET /api/support-requests` working with a Firebase ID token; help test | Admin screen that lists web requests through the API (send the ID token the app already has). Makes PAB-FR14 fully true |
| **W10** Alert admin phones about new requests | Send a push through the Firebase Admin SDK when a request is saved (no personal details in it) | Add FCM to the app to receive it (I7) |
| **J2** Architecture and threat model | Threat model for the GBV data (who could misuse it, what stops them), web side of the diagram | Android side of the diagram (Firebase, rules, notifications) |
| **J5** Demo script | Validation error, 429 page, staff view, auto-deploy | Joining, notifications, 3 languages, dark mode |
| **E2** PRODUCT.md and DESIGN.md | Web components and states | Android components; tokens are shared from `design/tokens.json` |

Agree as a whole group how staff hear about new requests (email or push, W10) before starting on it.

---

## 5. Final submission checklist

Do this once, after the last feature PR is merged and before submitting. Everyone gets reminded in the group chat a few days before the deadline.

| # | Step | Repo | Done |
|---|---|---|---|
| F1 | Delete the `changes_final_POE` branch in **both** repos (GitHub → repo → Branches → bin icon). That removes `handover/` and `changes_POE.md` | Both | ☐ |
| F2 | Check `develop`, `main` and `master` contain no `handover/`, `changes_POE.md` or `CLAUDE.md` | Both | ☐ |
| F3 | READMEs are current: requirements tables, links, test counts, live URLs | Both | ☐ |
| F4 | No AI signatures in any commit or PR on the branches being submitted | Both | ☐ |
| F5 | Merge `develop` → `main` (web) and `develop` → `master` (Android) with a merge commit; CI green; live site and download page work | Both | ☐ |

---

## 6. Accounts and access you may need

Nothing secret is in either repo. Ask whoever owns each one:

- **Firebase** project `pab-volunteers`: console access (Editor). Web team needs it for W1; Android team for rules and Auth
- **Railway** web service: variables, volume, deploy token (web team)
- **Cloudinary**: the cloud name and unsigned preset for `local.properties` (Android team, announcement pictures)
- **GitHub**: write access to both repos, and admin for branch protection and secrets
- A **service account key** only if you run the seed script (`tools/seed`). Keep it outside the repo
