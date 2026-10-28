# Handover: what's done and what's left

PAB Volunteers (Android) and the Help Centre (web). Updated 29 October 2026.

Task 2 got 176/270 (65%). Most marks were lost on the **web app** (APIs, Database, Data Flow, Security, CI/CD). Most of that is now fixed on web `develop`. Railway is gone: the site runs on Render's free plan with Postgres on Neon (Nuha, PRs #14 and #15), and CI deploys `main` there once the `RENDER_DEPLOY_HOOK` secret is set (W1).

**Repos:** Android `Kwethukubonga/PAB_Volunteers` · Web `Kwethukubonga/PAB_WebApp`. Clone both into the same folder.
**Full checklist:** [`IMPROVEMENT_PLAN.md`](IMPROVEMENT_PLAN.md) in this folder (108 items: 82 done, 11 part done; section 8 lists what's left on Android, in order). Each item says how to do it and how to know it's finished.

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
| Web | `main` | Old version (4 Oct). CI deploys `main` to Render (https://philisa-abafazi-bethu-web.onrender.com/) once `RENDER_DEPLOY_HOOK` is set. |
| Web | `develop` | All the new work, on Postgres (Neon), and the fixes from the 26 October edge-case test (issues #22–#27, PRs #29–#31) and README update (PR #32). 362 tests (six need Postgres and run in CI), coverage reported. Ready to go to `main` once W1 is done. |
| Web | `feature/ash/lighthouse`, `feature/ash/threat-model` | H4, PR #20, and J2, PR #21 (Ash). Open since 25 October; review and merge. |
| Web | Issue #28 | README out of date: fixed by Nuha's PR #32, which didn't link it. Close it. |
| Both | `changes_final_POE` | These notes and `changes_POE.md`. Never merge; delete before the final submission. |
| Web | `revert-7-feature/api` | Made with GitHub's Revert button on PR #7, never opened as a PR. **Don't merge it**: it would undo all the new web work. Delete it if nobody needs it. |
| Web | `feature/database-persistence`, `feature/update-programme-images`, `unathi` | Old, and they carry a stray `.claude/launch.json`. Delete if finished with. |
| Android | `develop` | Everything: all requirements and the requirements table, the pipeline, palette and Settings (PRs #17–#21), Tlhogi's Collective features, mockup screens, P1 work and Explore fingerprint (PRs #22–#24, #34), and Kwethu's P2 work (PRs #25–#33, #35). 189 unit tests and 68 rules tests. CI green. |
| Android | `master` | Old version (5 Oct). Merge `develop` in, then push `v1.1.0` (plan, section 8.2). |

### Done on web `develop`

- **Security:** server-side validation on both forms (SA phone rules, length limits), rate limiting (5 posts / 10 min per IP, API included), honeypot, strict security headers and CSP, 64 KB request limit on the forms, `Situation` and `Extra` encrypted at rest, retention clean-up, request logging with no personal data
- **Data:** unique `PAB-XXXXXX` references, schema constraints, staff notes, ready for Postgres (needs only a connection string)
- **Staff area** `/Staff/Requests`: PAB admins sign in with their Android admin account and move requests New → In progress → Closed
- **Shared API:** `/api/support-requests`, `/api/contact`, `/api/programmes`, `/api/resources`; OpenAPI at `/openapi/v1.json`; one error format (problem details)
- **Service layer:** no page touches the database directly
- **Design:** one palette in `design/tokens.json`, Tailwind built at compile time, self-hosted fonts, real icons instead of emoji, WebP images with lazy loading
- **Accessibility:** AA contrast, 44px touch targets, focus moves between form steps, step name shown on phones, `autocomplete`/`required` on fields
- **Safety/UX:** quick exit button (or Esc twice), loading state on Submit, confirmation page with "Copy reference", emergency numbers work offline
- **CI/CD:** tests, coverage, CSS-matches-tokens check, Docker build, deploy to Render from `main` (once its secret is set), `/health` and `/alive` checks, Dependabot
- **Docs:** README section 2 maps every requirement to its page, code and test; brought up to date by Nuha (PR #32)
- **Edge-case fixes** (issues #22–#27, Unathi, PRs #29–#31): the rate limit follows the page or endpoint reached, so a trailing slash can't get round it; Enter moves to the next step; the confirmation only shows after a real save; unknown programmes give a 404; the Privacy page is translated; more phone formats and landlines accepted, names need letters; full step 3 summary; South African time on the confirmation; Leave this site in the header on phones

### Done on Android

- All 17 requirements, 3 languages, light and dark themes, CI with unit tests, lint, rules tests and an APK build
- Rules tests for joining and leaving (the sign-up and the spot counter must change together; no overbooking, no signing others up)
- Requirements table in the README (PR #17); Settings as list rows, change password, two-step delete, offline bar (PR #21)
- Colours from the shared `design/tokens.json` (PR #20); download page live at https://pab-volunteers.web.app, deployed with the rules by the pipeline on a merge into `master`, Dependabot and a PR template, admin passwords out of the README (PR #19)
- Collective features, mockup screens and P1 work (PRs #22–#24, Tlhogi), fingerprint on the Explore page (PR #34, Tlhogi)
- Eyebrows gone, CI badge, release tag job (PR #25), edge-case tests (PR #26), version 1.1 (PR #27), contact PAB (PR #28), only my programmes (PR #29), privacy and download my data (PR #30), the introduction (PRs #31, #35), the web's fonts (PR #32) and Material 3 (PR #33), all Kwethu

---

## 2. Web team: Unathi, Nuha, Ash

### W1 · Go live: merge web `develop` → `main` · Owner: ______
Do the setup first:

| # | Setup step | Done |
|---|---|---|
| S1 | Firebase console → Project settings → add a **Web app** → put its API key in Render as `Firebase__ApiKey` | ☐ |
| S2 | Firebase console → Authentication → Settings → Authorised domains → add `philisa-abafazi-bethu-web.onrender.com` | ☐ |
| S3 | GitHub (web repo) → Settings → Secrets → `RENDER_DEPLOY_HOOK`; then turn off Render's own auto-deploy so only CI deploys | ☐ |
| S4 | Render variables: `TrustProxy=true` and `TrustProxyHops=3` (PR #14; the Dockerfile no longer sets them) | ☐ |
| S5 | Render variables: `ConnectionStrings__DefaultConnection` (Neon, direct connection), `DataProtection__Certificate`, `DataProtection__CertificatePassword`; Health Check Path `/alive` (PR #15) | ☐ |
| S6 | After deploy: `/health` says Healthy; submit a test request; sign in at `/Staff/SignIn` with an admin account and see it | ☐ |

Nuha set some of these up while testing PRs #14 and #15; tick what's already in place.

Staff sign-in has only been tested with fake tokens, never against the real Firebase project, so S6 matters. S1 and S2 need someone with Firebase access (ask the Android team).

### W2 · Translation check · Owner: ______
Check the isiXhosa and Afrikaans text on the new pages, form errors, the 429 page and the staff area (ideally a first-language speaker). The text is in the `.resx` files (PR #10) and the programmes in `Content/programmes.json` (PR #19).

### Web backlog

| ID | Pri | Task | Owner |
|---|---|---|---|
| C1 | P1 | ~~Provision Postgres, set the connection string, turn on backups, test a restore~~ done on Render and Neon (PRs #14, #15). Still: check Neon's server certificate (`Trust Server Certificate=true` today) | Nuha |
| W8 | P2 | Hosting configuration back in the repo: a `render.yaml` (`railway.json` went with Railway) | |
| D4 | P2 | Branch protection on web `develop` and `main` (CI must pass + 1 review). Dependabot and the badge are done | |
| Issues | P1 | ~~Edge-case test findings, issues #22–#27~~ fixed (PRs #29–#31) | Unathi |
| Issue #28 | P2 | ~~README out of date~~ fixed (PR #32); close the issue | Nuha |
| W12 | P2 | ~~Staff list: New / In progress / Closed tabs with counts, relative times, confirm before closing~~ done (PR #11) | Unathi |
| A8 | P2 | ~~Move programmes out of `Data/SiteData.cs` and UI text out of `s.Pick(...)` into content / `.resx` files~~ done (PRs #10, #19) | Unathi |
| C4 | P2 | Free uptime monitor (e.g. UptimeRobot) on `/alive`, so Neon can still sleep; put the uptime figure in the README | |
| D5 | P2 | Tag a release (`v1.1.0`) when `develop` goes to `main`, with notes from the merged PRs | |
| H4, J2 | P3 | Lighthouse CI (PR #20) and the threat model and diagram (PR #21): open since 25 October; review and merge | Ash |
| G7 | P2 | ~~Edge cases: very long names, emoji in every field, fast repeated Submit~~ done on web (PR #16) | Unathi |
| J4 | P2 | ~~PR template for the web repo (what changed, how it was tested, screenshots)~~ done (PR #17) | Unathi |

---

## 3. Android team: Tlhogi, Kwethu

### A1 · ~~Palette PR~~ · Owner: Kwethu
Merged as PR #20. Still to do on a phone: check the hand-written night colours (`values-night/colors.xml`) sit well next to the new light ones.

### A2 · Firebase housekeeping · Owner: ______
- Add the `FIREBASE_SERVICE_ACCOUNT` secret (D2), so a merge into `master` publishes the page, the APK and `firestore.rules`. Until then, publish the rules by hand after the collective-features PR
- Change both admin passwords in Firebase: they're out of the README (R2) but still in the repo's history
- Remove the duplicate test user in Firebase Auth (uid starting `ZQxVoCX6`)
- Firebase Support ticket about editing the password-reset email template (I8)
- Give the web team Firebase access for W1 (S1, S2)

### Android backlog

**P0, the marker's points:**

| ID | Task | Owner |
|---|---|---|
| D2 | ~~CI deploys the download page (`public/`) and `firestore.rules` on merge to `master`~~ built (PR #19). Still: the `FIREBASE_SERVICE_ACCOUNT` secret, then see the first merge into `master` deploy | Kwethu |
| C3 | ~~Put the APK download page live, link it in the README~~ live at https://pab-volunteers.web.app (PR #19). Still: install it from there on a phone once | Kwethu |
| J6 | ~~README: download page link, rules tests, anything else that changed~~ done (PR #19) | Kwethu |

**P1:**

| ID | Task | Owner |
|---|---|---|
| J1 | ~~Requirements table~~ merged into `develop` (PR #17). Still to do: demo video timestamps per requirement, and a slide | Tlhogi |
| R1 | ~~Hide draft activities in the rules~~ merged (PR #24), with 4 rules tests. Publish that rules file only once volunteers have the new APK: "live-hosting" first, then the rules (plan, section 8.2) | Tlhogi |
| R2 | ~~Take the admin passwords out of the README~~ done (PR #19): the details go to the marker privately. Still: change both passwords in Firebase, since they're in the history | Kwethu |
| F10 | ~~Navigation rail on tablets~~ done (PR #24) | Tlhogi |
| F12 | Check large text (1.3×) and dark mode in all 3 languages, fix clipping | |
| K1–K4 | ~~Settings rows, change password, two-step delete, offline banner~~ merged (PR #21). Still: check on a phone (3 languages, dark mode) | Tlhogi |
| K19–K26 | ~~Milestones, clearance, report a concern, search, "How did it go?", directions, animated splash, phone spacing~~ merged (PR #22). Still: **publish the new `firestore.rules`** with the release | Tlhogi |
| K7, K9–K14, E6, F9, I1, I3 | ~~Solid headers, Home planner, details with top bar, calendar and "I need help", Profile tabs and colour, fingerprint lock, emergency contact and reminders~~ merged (PR #23). Still: **confirm the crisis line 081 746 9889 with PAB**. Parts left: see the plan, section 8 | Tlhogi |
| K16 | ~~Google sign-in through Credential Manager~~ done (PR #24) | Tlhogi |
| P1 work | ~~Fingerprint sign-in, R1, F10, K6, K16, G1–G3, K5, F11, F14, K13, E7, simpler sign-in, Edit profile in Settings, Community months, milestone grid, admin Settings item, phone-first spacing, seed samples~~ merged (PR #24); fingerprint back on the Explore page (PR #34). Still: check sign-in on a phone under Material 3 | Tlhogi |
| Release | Merge `develop` → `master`, push `v1.1.0` (plan, section 8.2) | |
| Pictures | Announcement pictures need the Cloudinary details in `local.properties` and the CI secrets (plan, section 8.2) | Kwethu |

**P2:**

| ID | Task | Owner |
|---|---|---|
| E3, E4, E9 | ~~Same fonts as the web, eyebrow labels gone, Material 3~~ done (PRs #25, #32, #33) | Kwethu |
| K7 | Tinted neutrals (the solid header is done) | |
| G1, G2, K5, K6 | ~~Snackbars, buttons on empty screens, Retry, Save pinned at the bottom~~ done (PR #24) | Tlhogi |
| F13 | TalkBack pass (F11 predictive Back and F14 row updates are done, PR #24) | |
| I2, I4, I5, K8 | ~~"Only my programmes", contact PAB, privacy and download my data, intro screens~~ done (PRs #28–#31, #35) | Kwethu |
| K10 | Upcoming/Past tabs on Schedule, newest/oldest toggle | |
| D4 | Branch protection on Android `develop` and `master` (Dependabot, PR #19, and the badge, PR #25, are done) | |
| D5 | ~~Tag job: a `v*` tag builds the APK and makes a GitHub Release~~ built (PR #25). Still: push `v1.1.0` after the release merge | Kwethu |
| C4 | Uptime monitor on the download page | |
| G7 | ~~Edge-case unit tests~~ done (PR #26, Kwethu). Still, on a phone: airplane mode with Retry, fast repeated taps on Join and Save | |

**P3:** E10 (other sub-screens), I6, I7 (needs Firebase's paid Blaze plan), K11 (admin lists), K15, K17, K18. **J2** Android part is P1. See the plan, section 8.

---

## 4. Shared: each team does its part

| ID | Web team (Unathi, Nuha, Ash) | Android team (Tlhogi, Kwethu) |
|---|---|---|
| **B3** Android uses the web API | Keep `GET /api/support-requests` working with a Firebase ID token; help test | Admin screen that lists web requests through the API (send the ID token the app already has). Makes PAB-FR14 fully true |
| **W10** Alert staff about new requests | ~~Send a push when a request is saved~~ done by email instead (Unathi, PRs #12, #13): new requests and contact messages email staff with no personal details | Nothing needed now; FCM (I7) only if push is wanted later |
| **J2** Architecture and threat model | Threat model for the GBV data (who could misuse it, what stops them), web side of the diagram | Android side of the diagram (Firebase, rules, notifications) |
| **J5** Demo script | Validation error, 429 page, staff view, auto-deploy | Joining, notifications, 3 languages, dark mode |
| **E2** PRODUCT.md and DESIGN.md | Web components and states | Android components; tokens are shared from `design/tokens.json` |

The group chose email for W10, so staff hear about new requests and contact messages by email.

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
- **Render** web service and **Neon** database: variables, the deploy hook, the data-protection certificate (web team, Nuha)
- **Cloudinary**: the cloud name and unsigned preset for `local.properties` (Android team, announcement pictures)
- **GitHub**: write access to both repos, and admin for branch protection and secrets
- A **service account key** only if you run the seed script (`tools/seed`). Keep it outside the repo
