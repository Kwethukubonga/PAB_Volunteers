# Handover: what's done and what's left

PAB Volunteers (Android) and the Help Centre (web). Handed over by Tlhogi on 22 October 2026.

Task 2 got 176/270 (65%). Most marks were lost on the **web app** (APIs, Database, Data Flow, Security, CI/CD). Most of that is now fixed on web `develop`, but **none of it is live yet** because Railway only deploys `main`.

**Repos:** Android `Kwethukubonga/PAB_Volunteers` · Web `Kwethukubonga/PAB_WebApp`. Clone both into the same folder.
**Full checklist:** [`IMPROVEMENT_PLAN.md`](IMPROVEMENT_PLAN.md) in this folder (100 items: 39 done, 10 part done). Each item says how to do it and how to know it's finished.

## Picking this up with Claude Code

Each repo has a `CLAUDE.md` that Claude Code reads automatically: what the project is, the stack, commands, and the team's rules. To carry on:

1. Pull `develop` in both repos.
2. Open Claude Code in either repo and say something like *"Read handover/HANDOVER.md and pick up job 2.1"* (from the web repo, the path is `../PAB_Volunteers/handover/HANDOVER.md`).
3. Put your name in the **Owner** column here, work on `feature/<name>/<topic>`, PR into `develop`, and have someone else review.

> **Merging PRs:** use **"Create a merge commit"**, not "Squash and merge". Squashing already rewrote one person's commits under another name; the marker grades individual contribution from the history.
>
> **No AI signatures:** no `Co-Authored-By: Claude`/Copilot lines in commits or PRs. `CLAUDE.md` tells Claude this, but also turn off commit attribution in your own Claude Code settings (the `attribution` setting, or `"includeCoAuthoredBy": false` in older versions) and read each commit message before you push. AI use is declared once, in each README's AI Usage Declaration.

---

## 1. Where things stand

| Repo | Branch | State |
|---|---|---|
| Web | `main` | Old version (4 Oct). This is what Railway serves. |
| Web | `develop` | All the new work. 185 tests pass, coverage reported, README restored. Ready to go to `main` once section 2.2 is done. |
| Web | `revert-7-feature/api` | Made with GitHub's Revert button on PR #7, never opened as a PR. **Don't merge it**: it would undo all the new web work. Delete it if nobody needs it. |
| Web | `feature/database-persistence`, `feature/update-programme-images`, `unathi` | Old, and they carry a stray `.claude/launch.json`. Delete if finished with. |
| Android | `develop` | Same as `master` plus this handover and new rules tests (40 pass). All 17 requirements (PV-FR01–17) implemented. |
| Android | `feature/kwethu/design-tokens` | Shared palette + header contrast fix (E1, F9). **No PR yet.** Its commit message has a `Co-Authored-By: Claude` line; reword it before the PR (Kwethu). |

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

### Done in this handover (22 Oct)

- Web CI fixed (`coverlet.collector` added), so the coverage step and the Docker build run again
- Web README restored from Kwethu's full version (it had been overwritten with the old one), plus the Docker HTTPS setup and an honest PAB-FR14 row
- Android: rules tests for joining and leaving (the sign-up and the spot counter must change together; no overbooking, no signing others up), 40 tests passing
- Android `develop` brought level with `master`
- `CLAUDE.md` in both repos, and this folder

---

## 2. Do these next (in order)

### 2.1 Android palette PR · Owner: ______
Open a PR from `feature/kwethu/design-tokens` into Android `develop` (reword the commit to drop the Claude line first). It changes only `values/colors.xml`. Check the night colours (`values-night/colors.xml`) still sit well next to the new light ones, and that Android CI passes.

### 2.2 Go live: merge web `develop` → `main` · Owner: ______
Do the setup first:

| # | Setup step | Done |
|---|---|---|
| S1 | Firebase console → Project settings → add a **Web app** → put its API key in Railway as `Firebase__ApiKey` | ☐ |
| S2 | Firebase console → Authentication → Settings → Authorised domains → add the Railway domain | ☐ |
| S3 | GitHub (web repo) → Settings → Secrets → `RAILWAY_TOKEN` and `RAILWAY_SERVICE`; then turn off Railway's own auto-deploy so only CI deploys | ☐ |
| S4 | Railway variables: check `TrustProxy=true` (the Dockerfile sets it) | ☐ |
| S5 | Railway volume: the app now runs as a non-root user (UID 1654). If it can't write `/data/philisa.db` after deploy, make the volume writable by that user | ☐ |
| S6 | After deploy: `/health` says Healthy; submit a test request; sign in at `/Staff/SignIn` with an admin account and see it | ☐ |

Staff sign-in has only been tested with fake tokens, never against the real Firebase project, so S6 matters.

### 2.3 Quick jobs · Owner: ______
- Check the isiXhosa and Afrikaans text on the new web pages, errors and staff area (ideally a first-language speaker)
- Publish the latest `firestore.rules` (Android repo) to the live project
- Remove the duplicate test user in Firebase Auth (uid starting `ZQxVoCX6`)
- Decide how staff hear about new requests: email, or a push to admin phones (W10)
- Firebase Support ticket about editing the password-reset email template (I8)

---

## 3. Still open from Phase 1 (P0)

| ID | Repo | Task | Owner |
|---|---|---|---|
| D2 | Android | CI deploys the download page (`public/`) and `firestore.rules` on merge to `master`, using a service account secret in GitHub (never in the repo) | |
| C3 | Android | Put the APK download page live, link it in the README, test on a phone | |
| J6 | Android | README: download page link, rules tests, anything else that changed | |

---

## 4. Phase 2: Architecture

| ID | Repo | Task | Owner |
|---|---|---|---|
| B3 | Both | Android uses the web API: admins see web requests through `GET /api/support-requests` (send the Firebase ID token they already have), or a "Get help" entry that posts to it. This makes PAB-FR14 fully true | |
| C1 | Web | Provision Postgres on Railway (code is ready), set the connection string, turn on backups, test a restore | |
| A8 | Web | Move programmes out of `Data/SiteData.cs` and UI text out of `s.Pick(...)` into content / `.resx` files | |
| D4 | Both | Branch protection on `develop` and `main`/`master` (CI must pass + 1 review); Dependabot and badge on Android | |
| C4 | Both | Free uptime monitor on `/health` and the download page | |
| D5 | Both | Tag releases, attach the APK to a GitHub Release | |

---

## 5. Phase 3: Design and accessibility (mostly Android now)

| ID | Repo | Task | Owner |
|---|---|---|---|
| F10 | Android | Navigation rail on tablets instead of the bottom bar | |
| F12 | Android | Check large text (1.3×) and dark mode in all 3 languages, fix clipping | |
| K1–K4 | Android | Settings as list rows, change password from Settings, two-step delete account, offline banner | |
| E3, E4 | Android | Same fonts as the web (Lora + Source Sans 3), remove eyebrow labels | |
| E9, E10, K5–K7 | Android | Material 3, top app bar, Retry on errors, main action pinned at the bottom, solid header instead of gradient | |
| F11, F13, F14 | Android | Predictive Back, TalkBack pass, `ListAdapter` + `DiffUtil` | |
| E2 | Both | Write PRODUCT.md and DESIGN.md | |
| H4 | Web | Lighthouse in CI | |

---

## 6. Phase 4: Features and presentation

| ID | Repo | Task | Owner |
|---|---|---|---|
| J1 | Android | Requirements traceability table like the web README's section 2 (PV-FR01–17 → screen → code → test), then demo timestamps and a slide | |
| J2 | Both | Architecture diagram (both apps, API, databases, auth, CI/CD, hosting) + short threat model for the GBV data | |
| J5 | Both | Demo script: auto-deploy, validation error, 429 page, staff view, notification, 3 languages, dark mode | |
| J4 | Both | PR template (what changed, how it was tested, screenshots) | |
| W10 | Web + Android | Push alert to admin phones on a new request (needs FCM in Android, I7) | |
| W12 | Web | Staff list: New / In progress / Closed tabs with counts, relative times, confirm before closing | |
| I1–I8, K8–K18 | Android | Reminders, add to calendar, contact PAB, privacy/POPIA, intro screens, home planner, safety sheet, fingerprint lock, Credential Manager sign-in, ViewModel tests | |
| G1–G3, G7 | Android | Snackbars, empty-state buttons, delete dialog wording, edge-case testing | |

---

## 7. Accounts and access you may need

Nothing secret is in this repo. Ask whoever owns each one:

- **Firebase** project `pab-volunteers`: console access (Editor) for the setup steps, rules and Auth
- **Railway** web service: variables, volume, deploy token
- **Cloudinary**: the cloud name and unsigned preset for `local.properties` (announcement pictures)
- **GitHub**: write access to both repos, and admin for branch protection and secrets
- A **service account key** only if you run the seed script (`tools/seed`). Keep it outside the repo
