# Improvement Plan: Philisa Volunteers (Android) and Help Centre (Web)

This plan turns three sources into one checklist:

1. **Task 2 feedback** (marked by Amber Bruil): 176/270, 65%. Section 1 Code and Implementation 127/210, Section 2 Presentation 49/60.
2. **An audit against [Impeccable](https://github.com/pbakaus/impeccable)** (v4.6): its craft floor, its web and Android audit criteria, and its harden, clarify, onboard and polish guidance.
3. **Suggestions raised while building the app**: settings features, email delivery, notifications.

Each item names the repo it belongs to, its priority, how to do it, and how to know it is finished. Items that came from the marker's feedback are tagged with the rubric criterion they address.

**Progress (25 October 2026):** 75 of 108 done, 12 part done (marked **Status**). Android `develop` now has the Settings rebuild (K1–K4, PR #21), the shared palette (E1, PR #20) and the deploy pipeline (D2, PR #19). K19–K26, the mockup screens and the P1 work are on `feature/tlhogi/collective-features`, `feature/tlhogi/mockup-screens` and `feature/tlhogi/p1-and-spacing`, which go into `develop` next, in that order. Tlhogi checked the three branches on a phone on 25 October. The web site moved from Railway to Render, with Postgres on Neon (PRs #14, #15). Section 8 lists what's left on Android. Ticks reflect web `develop` and Android `develop` after the handover. Nothing on web `develop` is live until it is merged to `main`. The handover list is `handover/HANDOVER.md`.

> ⚠️ **Delete before the final submission.** This plan and `HANDOVER.md` are for the team only and live only on the `changes_final_POE` branch of each repo. Delete both branches before submitting (`HANDOVER.md` section 5, step F1).

**Repos and teams**
- **Android**: `Kwethukubonga/PAB_Volunteers`. **Android team: Tlhogi, Kwethu.** Every item labelled "Android".
- **Web**: `Kwethukubonga/PAB_WebApp`. **Web team: Unathi, Nuha, Ash.** Every item labelled "Web".
- Items labelled "Both": each team does its own app's part. `HANDOVER.md` section 4 says who does what for B3, W10, J2, J5 and E2.

**Priorities** (Impeccable's scale)
- **P0** Blocks a task, loses data or leaves sensitive data unprotected. Do first.
- **P1** Major problem or a rubric gap the marker named. Do before the next submission.
- **P2** Real but smaller. Next pass.
- **P3** Polish. If time allows.

---

## 1. Where the marks were lost

| Criterion | Mark | Lost | Marker's reason | Workstreams |
|---|---|---|---|---|
| Visual Design & Branding | 7/10 | 3 | Palette not shared between Tailwind and Android | E |
| UX & Feedback | 8/10 | 2 | No loading state on web submit | F, G |
| Responsiveness & Accessibility | 12/15 | 3 | Web pages not as accessible as Android | F |
| Programming Skills | 15/25 | 10 | Web needs a service layer; content hard-coded in C# classes | A, B |
| Database | 10/20 | 10 | Two flat SQLite tables; reference numbers can collide | A, C |
| APIs | 9/20 | 11 | No shared API; Razor writes straight to the database | B |
| Security | 11/20 | 9 | GBV form needs validation and rate limiting | A |
| Data Flow & Logic | 10/20 | 10 | PAB staff can't see or act on web support requests | A |
| Hosting: Deployment | 13/20 | 7 | Move off a single-container SQLite file | C |
| Hosting: Stability | 9/15 | 6 | Add a health check and a managed database | C |
| GitHub: Branching | 12/15 | 3 | Uneven contribution on Android | J |
| GitHub: CI/CD | 11/20 | 9 | Releases aren't deployed by the pipeline | D |
| Architecture & Design | 11/15 | 4 | Next band needs expert defence of API, security and DevOps | B, J |
| Requirements Alignment | 4/5 | 1 | Functional requirements need more elaboration | J |
| Demo, Version Control, Presentation | 34/40 | 6 | No specific criticism | J |

The biggest losses (APIs, Database, Data Flow, Programming, Security, CI/CD) all come from the web app. Workstreams A to D are where most marks can be recovered.

---

## 2. Impeccable audit scores (today)

These are estimates from reading the source. Impeccable's detector and rendered checks were not run, and nothing was captured on a device.

### Android: native audit, 14/20 (Good)

| Dimension | Score | Key finding |
|---|---|---|
| Accessibility | 3/4 | 48dp targets, sp text, labels and three languages are in place. Small muted text in headers is below contrast (F9). |
| Performance | 3/4 | RecyclerView and Glide caching. Seven adapters redraw whole lists with `notifyDataSetChanged` (F14). |
| Appearance & Theming | 3/4 | Colour tokens with a full dark palette. Built on Material 2 rather than Material 3 colour roles (E9). |
| Platform Conformance | 2/4 | Phone bottom bar on tablets, predictive Back not enabled, Toasts instead of Snackbars, custom header bands instead of a top app bar (F10, F11, G1, E10). |
| Adaptivity | 3/4 | Tablet spacing, two-column lists and landscape Welcome screen. Navigation doesn't change shape on tablets (F10). |

### Web: technical audit, 10/20 (Acceptable)

| Dimension | Score | Key finding |
|---|---|---|
| Accessibility | 2/4 | Good base: `lang`, skip link, labelled fields, labelled icon links, focus rings, reduced-motion CSS. Light grey text fails contrast in 15 places, no `autocomplete`, focus isn't moved between form steps (F1 to F3). |
| Performance | 1/4 | Tailwind loaded from its development CDN, none of the 9 images lazy-load, full-size JPGs, Google Fonts (H1 to H3). |
| Theming | 2/4 | Tailwind's default purples, plus one-off hex values and inline font styles, instead of shared tokens (E1, E8). |
| Responsive Design | 3/4 | Mobile-first with a collapsing menu. Language buttons, social icons and the menu button are below 44px (F5). |
| Implementation Integrity | 2/4 | Emoji used as icons next to SVG icons, eyebrow labels on most sections, placeholder "NPO 123-456" in the footer, Inter as the body font (E3 to E5, G5). |

### What already works (keep it)

- **Android:** every tappable control uses one 48dp size; all text is in `sp`; strings are complete in three languages with plurals; the dark palette is a real second scheme, not an inverted one; errors are translated into actionable messages; and 91 automated tests run in CI.
- **Web:** the three-step form with progress and a summary before submitting; the crisis line and 10111 shown at the right moment; no account needed, which suits someone in an unsafe situation; the page `lang` matches the chosen language; one consistent stroke-icon style in the header.

---

## 3. Phases

| Phase | Focus | Items |
|---|---|---|
| **1. Close the gaps the marker named** | Security, data reaching staff, deploys, quick UX fixes | A1–A3, A6, C2, D1, D2, F4, G4, G5, J6, W1–W4 |
| **2. Architecture** | Shared API, service layer, managed database, tests | A4, A5, A7, A8, B1–B4, C1, C3, D3–D5, W5–W9 |
| **3. Design system and accessibility** | One palette and type system, contrast, platform fit | E1–E10, F1–F3, F5–F14, H1–H4, K1–K7 |
| **4. Features and presentation** | Settings features, requirements traceability, demo | G1–G3, G6, G7, I1–I8, J1–J5, K8–K16, W10–W12 |

Section 7 lists the K (Android) and W (web) items, taken from Collective, Tlhogi's PROG7314 app.

---

## 4. Checklist

### A. Web back end: security and data
*Rubric: Security, Database, Data Flow, Programming Skills*

- [x] **A1 · P0 · Web: Check every submission on the server**
  - Today the support request model has no validation rules, and the browser is the only check. Without JavaScript, every field is optional.
  - Add rules to a request model: `[Required]` and `[StringLength]` on names, area and situation; `[EmailAddress]` on email; South African phone rules (the same standard as Android's `PhoneNumber.kt`: `0` plus a mobile network code, or `+27`); allowed-value lists for `ContactMethod`, `SupportType` and `Urgent`; and `Declaration` must be true.
  - Show errors next to each field with `asp-validation-for`, and keep what the person typed.
  - Do the same for the Contact form.
  - **Done when:** a POST with empty or invalid fields returns the form with field errors and saves nothing, and unit tests cover each rule.

- [x] **A2 · P0 · Web: Rate-limit the public forms**
  - Use ASP.NET Core's built-in rate limiter (`AddRateLimiter`) with a per-IP limit on POSTs to Request Support and Contact, for example 5 per 10 minutes.
  - Over the limit, return HTTP 429 with a friendly page that still shows the crisis line.
  - Add a hidden honeypot field that bots fill in and people don't.
  - **Done when:** the sixth quick submission gets 429 in an integration test, and the honeypot rejects bot posts.

- [x] **A3 · P0 · Web: Make reference numbers unique**
  - Today it's `"PAB" + Random(100–999)`: only 900 possible values, so collisions are certain once there are a few dozen requests.
  - Generate something like `PAB-2610-7KQ3` (date plus four random letters and digits, skipping look-alikes such as 0/O and 1/I). Add a unique index in a migration, and retry on the rare clash.
  - **Done when:** the unique index exists, existing duplicates are renamed by the migration, and a test shows a clash leads to a new reference rather than a failed save. (Random references can repeat now and then: about a 6% chance somewhere in 10,000. The index and retry are what stop duplicates.)

- [x] **A4 · P1 · Web: Constrain the database schema**
  - Configure the tables in `OnModelCreating`: required columns, maximum lengths, enums stored as strings, a unique index on `Reference`, and an index on `SubmittedAt`.
  - Add the columns staff need: `Status` (Received, In progress, Closed), `UpdatedAt`, `HandledBy` and `StaffNotes`.
  - Store times in UTC (`DateTime.UtcNow`, not `DateTime.Now`).
  - **Done when:** a migration applies the constraints, and the README's entity diagram matches it.

- [x] **A5 · P1 · Web: Protect sensitive data at rest and set a retention period**
  - Situation and Extra can describe abuse. Encrypt those two columns with ASP.NET Data Protection value converters, or rely on a managed database with encryption at rest (C1).
  - Agree a retention period with PAB, for example delete closed requests after 12 months. Implement it as a scheduled cleanup and state it on the Privacy page (POPIA).
  - Never log personal fields.
  - **Done when:** the database file or table can't be read in plain text, and the cleanup job has a test.

- [x] **A6 · P0 · Web: Let PAB staff see and act on requests**
  - This was the marker's main Data Flow point. Requests currently go into a database nobody can view.
  - **Minimum:** email each new request to PAB's intake address, with the reference, support type and urgency only. Leave the situation text out of the email.
  - **Full:** a login-protected staff area (`/Staff/Requests`) with a list sorted by urgency then date, a detail page, and status updates. Use ASP.NET Core Identity with staff accounts created by hand, `[Authorize]` on the pages, and an audit log of who opened what.
  - **Done when:** a test request shows up for staff within a minute, and a signed-out visitor gets redirected to sign in.

- [x] **A7 · P1 · Web: Add a service layer**
  - Page models currently use `AppDbContext` directly.
  - Add `ISupportRequestService`, `IContactService` and `IReferenceGenerator`, registered with dependency injection. Pages and API endpoints (B1) both call the services.
  - **Done when:** no page model references `AppDbContext`, and the services have unit tests with an in-memory or SQLite test database.

- [x] **A8 · P2 · Web: Move content out of C# classes**
  - **Status (25 Oct):** Done (Unathi). UI text is in `.resx` files for English, isiXhosa and Afrikaans (PR #10), and the programmes are in `Content/programmes.json`, read by `Data/ProgrammeContent.cs` (PR #19). Resources are in `Data/ResourceData.cs`.
  - Programme content lives in `Data/SiteData.cs`, and page text uses inline `s.Pick("English", "isiXhosa", "Afrikaans")` calls in the views.
  - Move programme content to a JSON file or a seeded table, and UI text to `.resx` files per language with `IStringLocalizer`.
  - **Done when:** changing a programme description or a label means editing a content file, not C# or Razor code.

### B. Shared API
*Rubric: APIs, Architecture, Programming Skills*

- [x] **B1 · P1 · Web: Add REST endpoints alongside the pages**
  - `POST /api/support-requests`: returns 201 with a `Location` header and the reference, 400 with `ProblemDetails` for validation errors, or 429 when rate-limited.
  - `GET /api/support-requests?status=&urgent=`: staff only, 401 or 403 otherwise, paginated.
  - `PATCH /api/support-requests/{id}`: staff only, updates the status.
  - `GET /api/programmes`: public, cacheable.
  - `GET /health`: see C2.
  - Publish an OpenAPI description (`AddOpenApi`) and link it from the README.
  - **Done when:** each endpoint returns the right status code for success, invalid input, unauthorised and not found, proven by B4 tests.

- [x] **B2 · P1 · Web: Send the Razor pages through the same services**
  - The form post and the API both call `ISupportRequestService` (A7), so there is one path for validation and saving.

- [ ] **B3 · P2 · Both: Connect the Android app to the API (stretch)**
  - Option 1: a "Get help" entry in the Android app that opens the web form, or posts to `POST /api/support-requests`.
  - Option 2: Android admins see web requests through `GET /api/support-requests`. The API checks the Firebase ID token, and the Firestore `role` must be `admin`.
  - Either gives the "shared API" the marker asked for and shows both clients using one back end.
  - **Done when:** the demo shows a request made on one client and seen on the other.

- [x] **B4 · P1 · Web: API integration tests**
  - Use `WebApplicationFactory` to cover each endpoint's status codes, validation, rate limiting (429), and staff authorisation (401 and 403). Run them in CI (D3).

### C. Data and hosting
*Rubric: Hosting Deployment, Hosting Stability, Database*

- [x] **C1 · P1 · Web: Move from SQLite to a managed database**
  - **Status (25 Oct):** Done (Nuha, PRs #14, #15). Railway is out, so the site runs on Render's free plan with Postgres on Neon (Frankfurt), its own migrations in `Migrations/Postgres`, and the encryption keys stored in the database, locked with a certificate held only in Render's settings. `tools/database/Backup-Database.ps1` and `Test-Restore.ps1` back up and restore; the live database was backed up and restored in a test. Known gap: the connection doesn't check Neon's server certificate yet (`Trust Server Certificate=true`).
  - Use Railway's Postgres plugin, Neon or Supabase, with the Npgsql EF Core provider.
  - Put the connection string in an environment variable, run migrations on deploy, and turn on automatic backups.
  - Write down the restore steps and test them once.
  - **Done when:** the live site runs on Postgres, restarting or redeploying loses nothing, and a backup has been restored in a test.

- [x] **C2 · P0 · Web: Health check**
  - **Status (25 Oct):** `/health` still checks the database. `/alive` (PR #15) answers without it, for Render's health check and an uptime monitor, so Neon can sleep between visitors.
  - Add `AddHealthChecks()` with a database check, mapped to `/health`, and set it as Railway's health check path so broken deploys aren't promoted.
  - **Done when:** `/health` returns 200 when healthy and 503 when the database is unreachable.

- [x] **C3 · P1 · Android: Put the download page live**
  - **Status (25 Oct):** Done (Kwethu, PR #19): live at https://pab-volunteers.web.app and linked in the README; the deploy checks the APK still downloads as an APK. Still worth doing once: download and install it on a phone.
  - The README still says the Firebase Hosting download page is "in progress". Deploy it (see D2), add the link to the README, and test the APK download on a phone.

- [ ] **C4 · P2 · Both: Uptime monitoring**
  - Add a free uptime monitor (for example UptimeRobot) on `/health` and the download page, and put the uptime figure in the README and the presentation.

### D. CI/CD
*Rubric: GitHub CI/CD (11/20), Version Control*

- [ ] **D1 · P0 · Web: Deploy automatically after the tests pass**
  - **Status (25 Oct):** Part done. Worked on Railway; with the move to Render (PR #14) the deploy job calls Render's deploy hook for the commit that passed the tests, only on `main`. It's skipped with a note until the `RENDER_DEPLOY_HOOK` secret is set (HANDOVER W1, S3).
  - Add a `deploy` job to `dotnet-tests.yml` that runs after `test`, only on `main`, using the Railway CLI with a `RAILWAY_TOKEN` secret. Or connect Railway to GitHub with "Wait for CI" turned on.
  - **Done when:** merging to `main` updates the live site with no manual step, and a failing test blocks the deploy.

- [ ] **D2 · P0 · Android: Deploy hosting and rules from the pipeline**
  - **Status (25 Oct):** Part done. The deploy job is built (Kwethu, PR #19): after build, unit tests, lint and the rules tests pass on a merge into `master`, one `firebase deploy` publishes the page, that build's APK and `firestore.rules`, then checks the page. Still to do: add the `FIREBASE_SERVICE_ACCOUNT` secret (Firebase Hosting Admin and Firebase Rules Admin roles) and see the first merge into `master` deploy.
  - After `build` passes on `master`, publish the APK and download page to Firebase Hosting (`FirebaseExtended/action-hosting-deploy` with a service account secret).
  - After the rules tests pass, run `firebase deploy --only firestore:rules`, so the tested rules are always the live ones.
  - **Done when:** a merge to `master` updates the download page and the live rules without anyone running `firebase deploy`.

- [x] **D3 · P1 · Web: More tests in CI**
  - **Status (22 Oct):** Done. `coverlet.collector` added; CI puts the coverage summary on the run page and keeps the report as an artifact.
  - Add the A1–A3 and B4 tests, collect coverage (`coverlet`), and keep the test and coverage reports as artifacts the way the Android pipeline does.

- [ ] **D4 · P2 · Both: Branch protection, badges and Dependabot**
  - **Status (25 Oct):** Part done. Dependabot on both (Android: Kwethu, PR #19, for Gradle, the workflow's actions and both npm folders) and a CI badge on the web README. Still to do: branch protection on both repos, and a CI badge on the Android README.
  - Require CI to pass and one review before merging into `develop` and `main`/`master`.
  - Add CI status badges to both READMEs.
  - Add `.github/dependabot.yml` for Gradle, NuGet, npm and GitHub Actions. Lint currently lists 63 outdated dependencies.

- [ ] **D5 · P2 · Both: Versioned releases**
  - Tag releases (`v1.1.0`), attach the APK to a GitHub Release, and generate release notes from merged pull requests.

### E. Design system and visual consistency
*Rubric: Visual Design & Branding (7/10). Impeccable: craft floor, document, typeset, quieter*

- [x] **E1 · P1 · Both: Define the palette once**
  - **Status (25 Oct):** Done. `design/tokens.json` and `tools/tokens/build.js` (web repo) write the Tailwind theme and Android's `values/colors.xml`, merged on Android in PR #20 (Kwethu). Android's `values-night/colors.xml` is still written by hand.
  - Today Android uses `#5B21B6`, `#4C1D95`, `#3B0764`, `#7C3AED`, `#EDE9FE` and cream `#FDFBF5`. The web uses Tailwind's default `purple-700` (#7E22CE), `purple-900`, `stone-*` and an arbitrary `#fffbf5`. They don't match.
  - Create `design/tokens.json` with brand purples, neutrals, status colours, type scale, spacing and corner radii.
  - Add a small script (for example `tools/tokens/build.js`) that writes Android's `colors.xml`, `values-night/colors.xml` and `dimens.xml`, and the web's Tailwind theme.
  - Base the brand colour on PAB's lavender ("Bringing the Lavender Back to the Hills").
  - **Done when:** changing one token changes both apps, and no raw hex values remain in web templates.

- [ ] **E2 · P2 · Both: Write PRODUCT.md and DESIGN.md**
  - Impeccable's `init` and `document` steps.
  - **PRODUCT.md:** who uses each app (volunteers; community members who may be in danger; PAB staff), where (low-cost Android phones, expensive mobile data, shared or monitored phones), the voice (warm, plain, never jokey around abuse), and the three languages.
  - **DESIGN.md:** the tokens from E1, the type scale, components and states.

- [ ] **E3 · P2 · Both: One type system for both apps**
  - **Status (22 Oct):** Web done: Lora and Source Sans 3 self-hosted. Android still uses the phone's `serif`; bundle the same fonts in `res/font`.
  - The web uses Inter for body text, which Impeccable lists as the most common sign of generic AI design. Android uses the phone's built-in `serif` for headings.
  - Pick one display face (Lora already gives the web its character) and one body face that renders isiXhosa and Afrikaans well.
  - Self-host them on the web (`woff2`, `font-display: swap`) and bundle them in Android's `res/font`, mapped through the Material type scale.

- [ ] **E4 · P3 · Both: Remove eyebrow labels above headings**
  - **Status (25 Oct):** Part done. Web done (three headings); Android's `admin_portal_label` is gone (`feature/tlhogi/p1-and-spacing`). Still to do: `eyebrow_our_programmes` and `eyebrow_testimonials` on the Explore page.
  - Impeccable bans the small caps label above a heading outright.
  - Android: `eyebrow_our_programmes`, `eyebrow_testimonials`, `admin_portal_label`.
  - Web: "What We Do", "Get Help" and similar labels in uppercase with wide letter spacing (about 18 across 8 pages).
  - Keep labels that carry information, such as "Step 1 of 2" and field labels.

- [x] **E5 · P2 · Web: Replace emoji with real icons**
  - Emoji stand in for icons on six pages (About 🤝💜❤✊⚖🌍, Contact 📞✉📍🚨, Index 📋💬, ProgrammeDetail 🚨, RequestSupport 📍, Resources 🏘🚨⚖👶🧠❓), next to the stroke SVG icons in the header.
  - Use one icon family. Material Symbols is a good fit because Android already uses Material icons.

- [x] **E6 · P2 · Android: Review the header gradient**
  - **Status (24 Oct):** Done on `feature/tlhogi/mockup-screens`: the one drawable every header shares is now solid `purple_700`. Show PAB the before and after when the PRs go in.
  - Every header is a 135° purple gradient. Purple fits PAB's brand; the gradient on every screen is the generic part.
  - Try a solid `purple_900` band with lighter purple surfaces on the screens below, and show both versions to PAB.
  - Whatever stays, it must pass F9.

- [x] **E7 · P3 · Android: Rethink the stat tiles**
  - **Status (25 Oct):** Done. Home's tiles became one summary line (`feature/tlhogi/mockup-screens`), and the admin Overview's three tiles an "At a glance" card whose rows open their tabs (`feature/tlhogi/p1-and-spacing`).
  - Home's four tiles (Today, Upcoming, Completed, Hours) and Admin's three follow the "big number, small label" pattern Impeccable flags.
  - Make each tile open its list (Upcoming opens Schedule), or merge them into one summary line ("3 upcoming · 12 hours completed").

- [x] **E8 · P2 · Web: Replace one-off styles with tokens**
  - Swap the repeated `style="font-family: Lora, serif;"` for the `font-serif` class, `bg-[#fffbf5]` for a named token, and build button and input classes once.

- [ ] **E9 · P2 · Android: Move to Material 3**
  - Change `Theme.MaterialComponents.DayNight` to `Theme.Material3.DayNight`.
  - Map the tokens to colour roles: primary, onPrimary, primaryContainer, surface, surfaceContainer, outline, error.
  - Use Material 3 buttons (filled, tonal, outlined, text) and tonal elevation.
  - Retest dark mode and contrast afterwards.

- [ ] **E10 · P3 · Android: Consider a top app bar**
  - **Status (24 Oct):** Part done. Activity details has a standard top bar with Back, Share and a favourite heart (`feature/tlhogi/mockup-screens`). Still to do: the other 15 screens with a header band and an image-button Back.
  - Screens use custom header bands with an image-button Back. A `MaterialToolbar` or `CollapsingToolbarLayout` gives the standard title, Back and action placement users expect.

### F. Accessibility and adaptivity
*Rubric: Responsiveness & Accessibility (12/15), UX & Feedback (8/10)*

**Web**

- [x] **F1 · P1 · Web: Fix low-contrast text**
  - `text-stone-400` (#A8A29E) appears 15 times, including the form summary labels, the "Women Centre SA" subtitle and placeholders (`placeholder-stone-400`). It measures about 2.4:1 on stone-50 and 2.5:1 on white.
  - Body text and placeholders need 4.5:1. Use `stone-500` (4.8:1 on white) or darker.

- [x] **F2 · P1 · Web: Mark required fields and add autocomplete**
  - Render `required` and `aria-required="true"` on the server, not only through JavaScript.
  - Add `autocomplete` values (`given-name`, `family-name`, `tel`, `email`, `address-level2`). This is WCAG 1.3.5, level AA.

- [x] **F3 · P1 · Web: Manage focus in the form steps**
  - On Next and Back, move focus to the new step's heading and announce "Step 2 of 3" in an `aria-live` region.
  - Set `aria-current="step"` on the current marker.
  - Give the completed-step tick hidden text ("completed").
  - Today focus stays on a button that has just been hidden.

- [x] **F4 · P0 · Web: Loading state on Submit**
  - This is the marker's UX point. On submit, disable the button, show a spinner and "Sending…", and ignore further clicks. Do the same on the Contact form.
  - **Done when:** clicking Submit ten times quickly saves one request.

- [x] **F5 · P1 · Web: Touch targets of at least 44px**
  - Language buttons are about 28px tall, social icons 32 to 36px, and the menu button 40px.
  - Language switching is essential for isiXhosa and Afrikaans speakers, so do those first.

- [x] **F6 · P2 · Web: Show which option is selected**
  - Radio cards only change the small radio button. Add `has-[:checked]:border-purple-700 has-[:checked]:bg-purple-50` and a `focus-within` ring.

- [x] **F7 · P2 · Web: Show the step name on phones**
  - Step labels are hidden below the `sm` breakpoint. Show "Step 2 of 3: Support needed" above the form on small screens.

- [x] **F8 · P3 · Web: Respect reduced motion when scrolling**
  - The form scrolls smoothly in JavaScript even when the person has asked for reduced motion. Check `matchMedia('(prefers-reduced-motion: reduce)')` first.

**Android**

- [x] **F9 · P1 · Android: Fix the muted header text contrast**
  - **Status (24 Oct):** Done on `feature/tlhogi/mockup-screens`: with the gradient gone (E6), `text_on_purple_muted` sits on solid `purple_700` at about 5.1:1. `feature/kwethu/design-tokens` also fixes it (white at 85%, plus darker success and error pill text); keep whichever PR goes in second consistent with the first.
  - The header gradient runs at 135°, so its lightest end (`purple_500`) sits at the top left, where "STEP 1 OF 2" and similar 12sp labels in `text_on_purple_muted` (#D8B4FE) appear. That's about 3.2:1; it needs 4.5:1.
  - Use white at 85% opacity, darken the light end of the gradient, or flip the angle.

- [x] **F10 · P1 · Android: Navigation rail on tablets**
  - **Status (25 Oct):** Done on `feature/tlhogi/p1-and-spacing`: tablets show the tabs as a navigation rail down the side; phones keep the bottom bar.
  - Impeccable's Android rule: "Never ship a phone bottom-bar untouched on a tablet."
  - Add `layout-sw600dp/activity_volunteer_main.xml` and `activity_admin_main.xml` using `NavigationRailView` with the same menu, and set it up the same way as the bottom bar.
  - **Done when:** a tablet shows the rail on the side and a phone still shows the bottom bar.

- [x] **F11 · P2 · Android: Predictive Back**
  - **Status (25 Oct):** Done on `feature/tlhogi/p1-and-spacing`: `enableOnBackInvokedCallback` is on; every screen already used OnBackPressedDispatcher.
  - Add `android:enableOnBackInvokedCallback="true"` to the manifest. The app already uses `OnBackPressedDispatcher`, which supports it.
  - Test Back from every tab and detail screen on Android 14 or later.

- [ ] **F12 · P1 · Android: Large-text and dark-mode check**
  - Run `adb shell settings put system font_scale 1.3` and `adb shell cmd uimode night yes`.
  - Capture every screen in English, isiXhosa and Afrikaans, and fix any clipping. Stat tiles, card buttons and settings toggles are the likeliest problems.
  - Restore `font_scale 1.0` afterwards.

- [ ] **F13 · P2 · Android: TalkBack check**
  - Walk through joining, leaving, settings and the admin editors with TalkBack.
  - The favourite heart should announce its state (`stateDescription`, "Saved" or "Not saved").
  - Status pills ("Joined", "Full") should be read with the activity title.

- [x] **F14 · P2 · Android: Update only the rows that changed**
  - **Status (25 Oct):** Done on `feature/tlhogi/p1-and-spacing`: the seven lists update only the rows that changed (DiffUtil), so nothing flashes on refresh.
  - Seven adapters call `notifyDataSetChanged()`, which lint flags.
  - Move them to `ListAdapter` with `DiffUtil`, so only changed rows redraw, and add item animations.

### G. UX writing and states
*Impeccable: clarify, onboard, harden. Rubric: UX & Feedback*

- [x] **G1 · P2 · Android: Snackbars instead of Toasts**
  - **Status (25 Oct):** Done on `feature/tlhogi/p1-and-spacing`: every Toast is a Snackbar, and a form that closes as it saves leaves its message on the screen it returns to.
  - Seven places use Toasts for "Activity saved", "Profile updated" and similar. Material uses Snackbars for short confirmations.
  - Show them on the screen the person returns to; the shared `AppMessage` helper already uses Snackbar.

- [x] **G2 · P2 · Android: Give empty states a button**
  - **Status (25 Oct):** Done on `feature/tlhogi/p1-and-spacing`: one shared card for every empty list, with Browse activities, Find opportunities, Create activity or announcement, or Clear search.
  - Empty Schedule gets a "Browse activities" button, empty Favourites gets "Find opportunities", and an empty admin list gets "Create activity".

- [x] **G3 · P3 · Android: Name the action in delete dialogs**
  - **Status (25 Oct):** Done on `feature/tlhogi/p1-and-spacing`: "Delete activity" and "Delete announcement", on the dialog buttons and the delete icons.
  - "Delete this activity?" currently confirms with "Remove". Use "Delete activity" and "Delete announcement" on the button.

- [x] **G4 · P1 · Web: Quick exit button**
  - A standard safety feature on gender-based violence support sites: someone may need to leave the page instantly if the person harming them walks in.
  - Add a fixed "Leave this site" button on Request Support, Resources and the programme pages. It opens a neutral site with `location.replace` (so Back doesn't return) and also triggers on the Esc key.
  - Add a short tip about clearing browser history to the Resources FAQ.

- [x] **G5 · P0 · Web: Remove the placeholder NPO number**
  - The footer says "NPO 123-456", which looks like a real registration number but isn't. Get PAB's real number or remove the line.

- [x] **G6 · P2 · Web: Confirmation page**
  - Say when PAB will respond (agree the timing with PAB), how they'll make contact (the method the person chose), and what to do in an emergency.
  - Add a "Copy reference" button.

- [ ] **G7 · P2 · Both: Test edge cases**
  - **Status (25 Oct):** Web done (Unathi, PR #16): long names and emoji counted the way PostgreSQL counts them, invisible characters removed, the same request sent twice in 10 minutes saved once, expired forms keep what was typed, phone numbers stored one way, clearer API errors (`EdgeCaseTests.cs`). Still to do: Android (200+ activities and volunteers, airplane mode with Retry, fast repeated taps on Join and Save).
  - Try names over 100 characters, emoji in every field, 200 or more activities and volunteers (admin lists may need paging), airplane mode with Retry on every list, and fast repeated taps on Join, Save and Submit.

### H. Web performance
*Impeccable audit: Performance 1/4. Data is expensive for PAB's community.*

- [x] **H1 · P1 · Web: Build Tailwind properly**
  - `cdn.tailwindcss.com` compiles CSS in the visitor's browser and isn't meant for production.
  - Add the Tailwind CLI to the Dockerfile build, so only the classes in use are output into one small CSS file.
  - **Done when:** no request goes to `cdn.tailwindcss.com`, and the CSS file is under 30KB.

- [x] **H2 · P1 · Web: Optimise images**
  - None of the 9 images use `loading="lazy"`, and the gallery and hero photos are full-size JPGs.
  - Convert them to WebP or AVIF, add responsive sizes (`srcset` and `sizes`), set `width` and `height` so the page doesn't jump, and lazy-load everything below the first screen.
  - **Done when:** the home page loads under 1MB on mobile.

- [x] **H3 · P2 · Web: Self-host fonts**
  - Fonts currently come from Google Fonts. Self-hosting is faster and sends nothing to a third party, which also helps with POPIA.

- [ ] **H4 · P3 · Web: Lighthouse in CI**
  - **Status (25 Oct):** Part done. Ash's PR #20 runs Lighthouse on pull requests with minimum scores; its checks pass. Next: review and merge it.
  - Run Lighthouse CI on pull requests, with minimum scores for Performance and Accessibility.

### I. Android features (from the settings research and earlier suggestions)

- [x] **I1 · P2 · Android: Activity reminders** (built as K14): "Remind me the day before" and "1 hour before" for joined activities, using WorkManager, with on/off switches in Settings.
- [ ] **I2 · P2 · Android: "Only my programmes"**: an option to be notified only about new activities in the programmes the volunteer chose.
- [x] **I3 · P2 · Android: Add to calendar**: a button on activity details that opens the phone's calendar with the details filled in (`CalendarContract`). Built on `feature/tlhogi/mockup-screens`.
- [ ] **I4 · P2 · Android: Contact Philisa**: call, WhatsApp, email and directions to 55 Strauss Road, in Settings.
- [ ] **I5 · P2 · Android: Privacy and POPIA**: a privacy page and "Download my data" (the profile and sign-ups as a file).
- [ ] **I6 · P3 · Android: About**: app version, PAB website and social links.
- [ ] **I7 · P3 · Android: Instant notifications**: Firebase Cloud Messaging through Cloud Functions. Needs the paid Blaze plan; until then the 15-minute background check stays.
- [ ] **I8 · P2 · Android: Reset email reaching inboxes**: once Firebase Support enables template editing, set the sender name, subject and wording, and a custom sending domain if PAB's domain is available.

### J. Process and presentation
*Rubric: Requirements Alignment (4/5), Architecture (11/15), Branching (12/15), Presentation*

- [ ] **J1 · P1 · Both: Requirements traceability table**
  - **Status (24 Oct):** Both READMEs done. Web: section 2 maps PAB-FR01–15. Android: section 3 maps PV-FR01–17 to screen, code and tests (merged into `develop` in PR #17; PV-FR18–27 are added by the feature branches); the gaps found were closed with 25 new tests (`UpdateRules`, `SignInDestination`, `PasswordRules`, favourites and sign-up list rules). Still to do: demo video timestamps, and a slide.
  - For every requirement (PV-FR01–FR17 and PAB-FR01–FR10), list the screen, the code, the test that proves it, and the timestamp in the demo video.
  - Put it in both READMEs and on a slide. This is the "elaboration" the marker asked for.

- [ ] **J2 · P1 · Both: Architecture and threat model**
  - One diagram showing both clients, the shared API (B1), both databases, authentication, CI/CD and hosting.
  - A short threat model for the gender-based violence data: who could misuse it, and which of A1, A2, A5, A6 and G4 stops them.
  - This is what the 12–15 Architecture band ("expert-level technical defence") needs.

- [ ] **J3 · P1 · Team: Spread the work**
  - The marker noted that one member did far less on Android.
  - Give each of the five members whole workstreams across both repos, with their own branches and pull requests, reviewed by someone else.

- [x] **J4 · P2 · Both: Commit and pull-request hygiene**
  - **Status (25 Oct):** Done. Pull request templates in both repos (web: Unathi, PR #17; Android: Kwethu, PR #19). The stray template commit stays in Android `master`'s history; rewriting `master` isn't worth it.
  - Use descriptive commit messages and a pull request template (what changed, how it was tested, screenshots).
  - Avoid stray template commits like the "Update fmt.Println message from 'Hello' to 'Goodbye'" one now on Android `master`.

- [ ] **J5 · P2 · Both: Demo script**
  - Show the pipeline deploying a change by itself, a validation error, the 429 rate-limit page, the staff view receiving a request, a notification arriving, and the app in all three languages and dark mode.

- [x] **J6 · P0 · Both: Bring the READMEs up to date**
  - **Status (25 Oct):** Done. Web README current, including Render and Postgres (PRs #14, #15). Android README has the download link, the deploy job and no admin passwords (Kwethu, PR #19). Keep both current as each item lands (HANDOVER F3).
  - The web README lists "Automated tests and CI" as future work, but `dotnet-tests.yml` and seven test files exist. The Android README still says the download page is in progress.
  - Update both as each item above lands.

---

## 5. Using Impeccable on this project (optional)

- **Install:** `npx impeccable install` from each repo's root, then run `/impeccable init` in Claude Code to write PRODUCT.md (item E2).
  - It adds skill files and an editor hook to the repo. Follow its README's ignore rules so `.impeccable/` runtime files stay out of Git.
- **Web:** `/impeccable audit` and `/impeccable critique` run its detector on the live pages. Re-run after phases 1 and 3 to track the score.
- **Android:** `/impeccable audit` audits from source (no detector), scored against its Android reference. Screenshots must come from a device or emulator, per its guidance.
- **Treat findings as advice, not rules.** Impeccable's own guidance says the brief wins: PAB's purple brand stays, even though purple gradients are on its list of common tells.

---

## 6. Expected effect on marks

These are rough estimates for planning, not promises. Only the marker decides the mark.

| Criterion | Now | Target | Main items |
|---|---|---|---|
| APIs | 9 | 14–16 | B1, B2, B4 (B3 for the top band) |
| Security | 11 | 15–17 | A1, A2, A5, A6, G4 |
| Data Flow & Logic | 10 | 14–16 | A6, A7, B2 |
| Database | 10 | 14–16 | A3, A4, C1 |
| Programming Skills | 15 | 19–21 | A7, A8, F14 |
| CI/CD | 11 | 15–17 | D1, D2, D3 |
| Hosting (both) | 22 | 28–31 | C1, C2, C3, C4 |
| Visual Design | 7 | 8–9 | E1, E3, E5 |
| UX & Accessibility | 20 | 22–24 | F1–F5, F9, F10, G4 |
| Architecture & Requirements | 15 | 17–19 | J1, J2 |

---

## 7. Ideas from Collective (Tlhogi's PROG7314 app)

Collective (`EMWCCN/prog7314-g2-2026-formative-2-part-2-tlhogivc`, a private repo, read at commit `a920076`; ask Tlhogi for access if you need to see the code, but each item below says what to build) already solves several things PAB is missing.

| Collective has | PAB today |
|---|---|
| Material 3 | Material 2 |
| Solid brand colour, no gradients | A purple gradient on every header |
| Neutral colours tinted towards the brand | Plain greys |
| A standard top app bar on sub-screens | Custom header bands with an image-button Back |
| An app-wide offline banner | Offline is only reported after an action fails |
| Error states with Retry | No Retry |
| The main action pinned in a bottom bar | Actions scroll with the page |
| A settings screen built from list rows | Cards with separate controls |
| A tested REST API with Firebase sign-in checks | No API on the web |

The items below say what to copy, and which Collective file shows how. Paths are relative to `android/app/src/main/java/com/collective/app/` or `backend/src/`.

### K. Android: settings, layout and finishing touches

- [x] **K1 · P1 · Android: Rebuild Settings as list rows** (from `ui/profile/SettingsScreen.kt`)
  - **Status (25 Oct):** Merged into `develop` (PR #21) with K2–K4. Still needs a check on a real phone, in all three languages and dark mode.
  - **Sections in this order:**
    - **Account:** edit profile, change password, fingerprint lock.
    - **Appearance:** theme, language.
    - **Notifications:** one switch per type, plus reminder timing.
    - **Safety:** emergency contact.
    - **About:** version, "Show the introduction again", PAB's website and contact.
  - **Each row:** a leading icon, a title, and a supporting line that shows the current value (for example "Change password · nomsa@email.com", or "Emergency contact · Thandi · 082 123 4567"), with a chevron for rows that open something.
  - **The whole row is the touch target.** Switch rows toggle when tapped anywhere (`role = Switch`), not only on the switch.
  - **Language as a radio list** with 56dp rows, instead of three buttons squeezed into one row. Longer isiXhosa and Afrikaans labels then never shrink.
  - **Sign out and Delete account at the very bottom:** a red filled button and a red outlined one, apart from everyday settings so they aren't tapped by accident. Sign out moves here from Profile.
  - **Section titles** in the brand colour above a lightly tinted card (`surfaceContainer` in Material 3).

- [x] **K2 · P1 · Android: Change password from Settings** (from `SettingsScreen.kt`, `SettingsViewModel.sendPasswordReset`)
  - For email accounts only (hidden for Google accounts). Show the account's email as the supporting line, a small spinner while sending, and a Snackbar "Reset link sent to …".
  - Today the reset is only reachable from the sign-in screen.

- [x] **K3 · P1 · Android: Delete account in two calm steps** (from `ui/profile/DeleteAccountScreen.kt`)
  - **Step 1:** a page listing what will be lost (hours, upcoming places, history), with "No, go back" as the big button and "Yes, continue" as the quiet one.
  - **Step 2:** confirm with the password (or Google, for Google accounts) and type DELETE.
  - **Why:** this replaces today's "sign in again" dead end. PAB currently signs the person out when their sign-in is older than 5 minutes; Collective asks them to confirm again on the spot instead.

- [x] **K4 · P1 · Android: App-wide offline banner** (from `util/NetworkMonitor.kt`, `ui/navigation/ConnectivityViewModel.kt`, `CommonComponents.OfflineBanner`)
  - A slim bar under the header on every screen while there's no connection, so people know before they tap Join.
  - Writes queued offline show "Sending…".

- [x] **K5 · P2 · Android: Error state with Retry** (from `CommonComponents.ErrorState`)
  - **Status (25 Oct):** Done on `feature/tlhogi/p1-and-spacing`: a list that fails to load with nothing to show says why and offers Try again.
  - When a list fails to load, show the reason and a Retry button in place of the list, instead of a Snackbar over an empty screen.

- [x] **K6 · P2 · Android: Main action pinned at the bottom** (from `CommonComponents.DetailScaffold`, `BottomActionBar`, `PrimaryActionButton`)
  - **Status (25 Oct):** Done. Join and Leave on activity details (`feature/tlhogi/mockup-screens`), and Save in both editors and Edit Profile, sit in a bar at the bottom that rises above the keyboard (`feature/tlhogi/p1-and-spacing`).
  - Join or Leave on activity details, and Save in the editors and Edit Profile, sit in a bar at the bottom within thumb reach that rises above the keyboard.
  - The button turns into a spinner while saving and is always at least 48dp tall.
  - Pairs with E10 (top app bar for sub-screens).

- [ ] **K7 · P2 · Android: Solid brand surfaces and tinted neutrals** (from `ui/theme/Color.kt`, `BrandComponents.HeroHeader`)
  - **Status (24 Oct):** Part done. The headers are one solid brand panel (E6). Still to do: tint the greys (`text_secondary` #6B7280 and the other neutrals, light and dark) slightly towards lavender.
  - Replace the gradient headers with one solid brand panel, and tint the greys slightly towards PAB's lavender (Collective's light and dark neutrals are violet-tinted).
  - This resolves E6 and F9 and matches Impeccable's "tinted greys" advice.

- [ ] **K8 · P2 · Android: First-run introduction** (from `ui/onboarding/OnboardingScreen.kt`)
  - Three swipeable pages before sign-in on a new install: find an activity, join in one tap, track your hours. Include Skip, Next and "Get started", and a replay option in Settings, About.
  - Use PAB's own photos.

- [x] **K9 · P2 · Android: Home as a planner** (from `ui/home/HomeScreen.kt`)
  - **Status (24 Oct):** Done on `feature/tlhogi/mockup-screens`: next activity card, the coming seven days with a dot on booked days, the plan for the day tapped, a summary line, and the latest announcement with how long ago it was posted. `WeekPlan` has 5 tests.
  - **Next activity card:** a large brand-colour card at the top showing the next activity ("Community Soup Kitchen · Sat 09:00 · Philisa Centre"), or a nudge to find one.
  - **Week strip:** the coming week, with a dot on days that have something booked. Tapping a day shows that day's plan.
  - Replaces the four stat tiles (E7); hours stay on Profile.

- [ ] **K10 · P2 · Android: Schedule like an order list** (from `ui/profile/HistoryScreen.kt`)
  - **Status (24 Oct):** Part done. Profile has Upcoming and Past tabs with counts and "today" / "in 3 days" times (`feature/tlhogi/mockup-screens`). Still to do: the same tabs on the Schedule tab itself, and a newest/oldest toggle.
  - Active and Past tabs, status chips with counts ("Upcoming (3)"), a newest/oldest toggle, and relative times ("in 2 days", "3 hours ago").

- [ ] **K11 · P3 · Android: One colour per person** (from `Color.AvatarColors`)
  - **Status (24 Oct):** Part done. `AvatarColors` (8 colours, each tested for 4.5:1 with white) colours the volunteer's own initial on Profile (`feature/tlhogi/mockup-screens`). Still to do: the admin's Volunteers list and sign-up lists.
  - Volunteer initials in admin lists get a colour picked from their ID, so the same person always looks the same, instead of every avatar being lavender.

- [x] **K12 · P2 · Android: Volunteer safety on the day** (from `ui/safety/TripSafety.kt`)
  - **Status (24 Oct):** Done on `feature/tlhogi/mockup-screens`: Share on activity details, and "I need help" on the day of a joined activity (112, 10111, Philisa's crisis line, a ready-written text to the emergency contact from Settings). The share has the activity, place and time but not the phone's current location, which would need the location permission. **Confirm the crisis line number (081 746 9889, taken from the web app) with PAB.**
  - **Share where I'm volunteering:** activity, place, time and current location, sent through the share sheet to WhatsApp or SMS. Nothing is stored by the app.
  - **On the day of a joined activity:** an "I need help" sheet with Call 112, Call 10111, Call PAB's crisis line, and a ready-written text to the emergency contact from K1.
  - The dialler and SMS app always open with everything filled in, so nothing is sent without the person tapping.
  - Most useful for Search and Rescue and community outreach activities.

- [x] **K13 · P2 · Android: Fingerprint lock, especially for admins** (from `security/AppLock.kt`, `security/BiometricCredentialStore.kt`)
  - **Status (25 Oct):** Done. Settings → Account → Sign in with fingerprint, with "Also lock when the app reopens" under it; admins are asked once to set it up (`feature/tlhogi/p1-and-spacing`). The sign-in itself is on the sign-in screen only.
  - An optional "Lock the app when it reopens" using Android's fingerprint or face check.
  - Admin phones show every volunteer's phone number and area, so suggest it to admins on first sign-in (POPIA).

- [x] **K14 · P2 · Android: Reminders with a chosen lead time** (from `notifications/DepartureReminders.kt`, `util/ReminderTimes.kt`)
  - **Status (24 Oct):** Done on `feature/tlhogi/mockup-screens`: Off, 1 hour or 1 day before, sent once each by the 15-minute background check on their own channel. Arrives within about 15 minutes of the chosen time, since WorkManager can't run more often.
  - Settings choice: Off / 1 hour / 1 day before a joined activity, scheduled with WorkManager so they arrive when the app is closed. This is how to build I1.

- [ ] **K15 · P3 · Android: Don't notify about what's on screen** (from `notifications/ActiveChat.kt`)
  - Skip the "new announcement" notification while Community is open, and "activity full" while that activity's sign-up list is open.

- [x] **K16 · P1 · Android: Google sign-in through Credential Manager** (from `ui/auth/GoogleCredentialSignIn.kt`)
  - **Status (25 Oct):** Done on `feature/tlhogi/p1-and-spacing`: Google sign-in uses Credential Manager; the deprecated GoogleSignIn library is gone.
  - Switch to `GetSignInWithGoogleOption`. This removes the deprecated `GoogleSignIn` APIs behind the build warnings, and gives the modern account picker.

- [ ] **K17 · P3 · Android: Tagged logging without personal data** (from Collective's README Logging table)
  - One `Log` tag per class for sign-in, joins and leaves, notifications and connectivity changes. Log user IDs only, never emails or phone numbers. Document the tags in the README.

- [ ] **K18 · P3 · Android: ViewModel tests** (from Collective's `ui/**/ViewModelTest.kt`, `MainDispatcherRule.kt`)
  - PAB's 66 unit tests cover utilities and models. Add tests for `VolunteerViewModel` and `AdminViewModel` (join, leave, offline, error messages) with a test dispatcher. Turbine and MockK, as Collective uses, make this easier.

**Added on 24 October from a second look at Collective.** All built on `feature/tlhogi/collective-features` (on top of the Settings branch, now in `develop`; this PR is next), with 23 unit tests and 19 rules tests. Each still needs a check on a real phone. **The new rules must be published** (`firestore.rules`) before clearances, concerns and feedback work in the live app (the D2 pipeline does it on a merge into `master` once its secret is set, or publish by hand); until then those parts are quietly refused and the rest of each screen still loads.

- [x] **K19 · P2 · Android: Volunteer milestones** (from `util/GamificationEngine.kt`): first shift, 10 activities, 25 hours, 3 programmes on Profile, worked out from finished activities, never stored, so they can't be faked.
- [x] **K20 · P2 · Android: Volunteer clearance** (from `ui/profile/DriverVerificationScreen.kt`): volunteer asks in Settings → Safety and brings documents to the centre; admin marks them cleared for 12 months from their profile. Own collection `clearances`; rules let a volunteer only ask.
- [x] **K21 · P2 · Android: Report a concern** (from `SafetyRepository` reports): from an activity or Settings, emergency numbers shown first; admins see them under Overview → Concerns and mark them handled. Never deleted.
- [x] **K22 · P2 · Android: Search** (from `util/SearchTokens.kt`): Activities tab and the admin's Volunteers list; every word must match.
- [x] **K23 · P2 · Android: "How did it go?"** (from `ui/reviews/`): on a finished activity the volunteer was on; average and comments on the admin's sign-ups screen.
- [x] **K24 · P2 · Android: Directions** (from `LocationComponents`): opens the phone's maps app at the activity's place, or the browser.
- [x] **K25 · P3 · Android: Animated splash** (from `HandsAnimation.kt`): logo eases in, name rises; off when animations are off.
- [x] **K26 · P2 · Android: Phone field spaced as typed** (from `PhoneNumberField.kt`): 082 123 4567 and +27 82 123 4567 in profile setup and Edit profile.

**Built on 24 October from the mockup** (`feature/tlhogi/mockup-screens`, on top of `feature/tlhogi/collective-features`, six "UI:" commits): E6, F9, I1, I3, K9, K12, K14 and parts of E7, E10, K6, K7, K10, K11, K13. 16 new unit tests and one rules test; the README adds PV-FR24–27. No rules change is needed: the emergency contact is two fields on the volunteer's own record, which the current rules already allow. Each screen still needs a check on a real phone, in all three languages and dark mode, and the new isiXhosa and Afrikaans text needs a first-language check.

### W. Web: making the weakest link stronger

Collective's `backend/` is an Express and TypeScript REST API with Firebase sign-in checks, an admin guard, one error format, security headers and an offline integration test suite. Those are the pieces the web app is missing, and each maps directly to ASP.NET Core.

- [x] **W1 · P0 · Web: Staff sign in with their PAB Firebase accounts** (from `middleware/auth.ts`, `middleware/requireAdmin.ts`)
  - Collective's API checks the Firebase ID token on every request. In ASP.NET Core this is `AddJwtBearer` with Authority `https://securetoken.google.com/pab-volunteers` and the audience set to the project ID.
  - Staff then use the same admin accounts they already have in the Android app; an `[Authorize(Policy = "Staff")]` policy checks the admin role.
  - This is the simplest way to build A6's staff area and B1's staff endpoints, with no second set of passwords to manage.

- [x] **W2 · P0 · Web: One error format** (from `middleware/errorHandler.ts`)
  - Collective has `ApiError(statusCode, message)`, a 404 handler and one error handler that logs 5xx errors as errors and 4xx as warnings.
  - In ASP.NET: `AddProblemDetails()`, `UseExceptionHandler()`, `UseStatusCodePages()`, plus a small `ApiException` mapped to its status code. Every API error comes back in the same JSON shape.

- [x] **W3 · P0 · Web: Security headers** (from `helmet` in `app.ts`)
  - Add the headers `helmet` sets by default: `Content-Security-Policy`, `X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer`, `Permissions-Policy`, and `frame-ancestors 'none'`.
  - `no-referrer` matters most here: it stops the address of a help page leaking to other sites someone visits next.

- [x] **W4 · P1 · Web: Validation with clear messages** (from `middleware/validate.ts`)
  - Collective rejects missing fields with a 400 that names them. Do the same with DataAnnotations or FluentValidation, as part of A1.
  - **Port Collective's South African phone standard** (`util/SaPhoneNumbers.kt` and its 7 tests) to C#, so the web, PAB Android and Collective all accept and refuse the same numbers. Reuse the same test cases.

- [x] **W5 · P1 · Web: Integration tests without a real database** (from `backend/tests/`, Jest and Supertest with a fake Firestore)
  - The ASP.NET equivalent is `WebApplicationFactory` with an in-memory SQLite or Testcontainers database.
  - Cover each endpoint's status codes (200, 201, 400, 401, 403, 404, 429) and the staff policy. This is B4.

- [x] **W6 · P1 · Web: Routes split by resource** (from `routes/` and `controllers/`)
  - Mirror Collective's one-router-per-resource layout with ASP.NET endpoint groups: `/api/support-requests`, `/api/contact`, `/api/programmes`, `/api/resources`, with the logic in the services from A7.

- [x] **W7 · P1 · Web: Request logging without personal data** (from `morgan` and `utils/logger`)
  - Log method, path, status and time for every request, with no form contents and no IP addresses stored beyond the rate limiter.

- [ ] **W8 · P2 · Web: Hosting configuration in the repo** (from `render.yaml`)
  - **Status (25 Oct):** Undone by the move to Render: `railway.json` was removed (PR #14) and Render's settings live only on its dashboard. A `render.yaml` with the Docker build, `/alive` health check and environment variable names (no values) would put it back in the repo.
  - Add `railway.json` (or `railway.toml`) with the build command, health check path (`/health`, from C2) and restart policy, so the hosting setup is reviewed and versioned like code.

- [x] **W9 · P2 · Web: Seed script with relative dates** (from `backend/src/seed/plan.ts`, `seedData.ts`)
  - A development seed with programmes, resources and example support requests in every status. Dates count from when it runs, and derived values (counts, statuses) are worked out rather than typed in.
  - Add a test that checks the seed is consistent. Collective's suite does this.

- [x] **W10 · P1 · Web: Alert PAB's admin phones about new requests** (from Collective's API sending FCM pushes on new bookings)
  - **Status (25 Oct):** Done by email instead of push, as the group agreed (Unathi). New support requests (PR #12) and contact messages (PR #13) email PAB's staff through Resend with no personal details, and staff read contact messages on the new Staff → Messages page. Push to the Android app (I7) is not needed for this.
  - When a support request is saved, the web server sends a push through the Firebase Admin SDK for .NET (`FirebaseAdmin` NuGet) to PAB admin devices: "New support request · Urgent · Food assistance". No personal details go in the notification.
  - Tapping it opens the request through B3.
  - This links the two apps and answers the marker's "notification so PAB staff can act" (A6). The Android app would need FCM added (I7).

- [x] **W11 · P2 · Web: Work offline for emergency numbers** (from Collective's offline banner and cache)
  - Make the Help Centre an installable web app with a service worker that caches the Resources page and emergency numbers, so they open with no data.
  - Show an offline notice on the form, and keep what the person typed until they're back online.

- [x] **W12 · P2 · Web: Staff request list like Collective's History** (from `ui/profile/HistoryScreen.kt`, `BrandComponents.StatusPill`)
  - **Status (25 Oct):** Done. Status tabs with counts, relative times ("2 hours ago"), the longest-waiting / newest toggle and token colours were already in; confirm before closing added by Unathi (PR #11).
  - New / In progress / Closed tabs with counts, urgent requests first, colour-coded status pills with one colour per status everywhere, relative times ("2 hours ago"), and a newest/oldest toggle.
  - Closing or deleting a request asks for confirmation, the way Collective handles destructive actions.

---

## 8. Android: what's left (25 October, after `feature/tlhogi/p1-and-spacing`)

Everything not ticked above, in the order to build it. Items marked part done list only the missing part. Build on a new branch from `feature/tlhogi/p1-and-spacing` until it is in `develop`, then from `develop`.

**Also on `feature/tlhogi/p1-and-spacing`, from Tlhogi's phone checks:** sign-in laid out as in Collective (main button, then Google and fingerprint side by side), fingerprint sign-in on the sign-in screen only, Edit profile and programme interests moved into Settings, Community announcements grouped by month and foldable, milestone badges as an even 2×2 grid, a Settings item in the admin bar, and phone-first spacing (16dp screen edge and card padding on phones; tablets unchanged).

### 8.1 Code (no phone or account needed)

**P2**
1. **K7** rest: lavender-tinted neutrals, light and dark (the light ones through `design/tokens.json`, since `colors.xml` is generated).
2. **K10** rest: Upcoming and Past tabs on the Schedule tab, and a newest/oldest toggle.
3. **K8** First-run introduction (three pages, Skip / Next / Get started), with "Show the introduction again" in Settings → About.
4. **I4** Contact Philisa: call, WhatsApp and directions to 55 Strauss Road beside today's email row.
5. **I2** "Only my programmes" switch for new-activity notifications.
6. **I5** Privacy page and "Download my data" (profile and sign-ups as a file).
7. **E4** rest: the two eyebrow labels on the Explore page.
8. **E3** Lora and Source Sans 3 in `res/font`, mapped through the type styles.
9. **E9** Material 3 theme and colour roles (largest change; retest dark mode and contrast after).
10. **D4** Android part: a CI badge in the README.
11. **J2** Android part: architecture diagram and threat model in the README.

**P3**
12. **E10** rest: a standard top bar on the other sub-screens.
13. **K11** rest: a colour per person in the admin's Volunteers and sign-up lists.
14. **I6** About: PAB's website and social links beside the version.
15. **K15** No "new announcement" notification while Community is open, nor "activity full" while that sign-up list is open.
16. **K17** One log tag per class, user IDs only, tags listed in the README.
17. **K18** `VolunteerViewModel` and `AdminViewModel` tests with a test dispatcher.
18. **B3** (stretch) A "Get help" entry that opens the web Help Centre form.

### 8.2 Needs a phone, an account or the team

- **PRs in order, merge commits only:** `feature/tlhogi/collective-features` → `feature/tlhogi/mockup-screens` → `feature/tlhogi/p1-and-spacing`. Each already includes the one before.
- **Pictures on announcements:** "No picture service key is set on this build" means the build has no Cloudinary details. Add `cloudinary.cloud.name` and `cloudinary.upload.preset` (an unsigned preset) to your own `local.properties`, from Kwethu's Cloudinary dashboard, and check the `CLOUDINARY_CLOUD_NAME` and `CLOUDINARY_UPLOAD_PRESET` secrets are set in the Android repo, so CI and download-page builds have them too. Never commit them.
- **Rules:** publish the collective-features `firestore.rules` now. Publish the `feature/tlhogi/p1-and-spacing` version (drafts hidden from volunteers, R1) only once volunteers have the new APK, because the old app's background check uses a query it refuses. The D2 pipeline does both on a merge into `master` once `FIREBASE_SERVICE_ACCOUNT` is set.
- **R2:** change both admin passwords in Firebase. They're out of the README but still in the repo's history.
- **Confirm Philisa's crisis line** (081 746 9889) with PAB before the "I need help" sheet ships.
- **Phone checks still to do:** 1.3× text in all three languages (F12), a TalkBack pass (F13), the edge cases (G7), and installing the APK from https://pab-volunteers.web.app (C3). The three branches were checked on a phone on 25 October.
- **First-language check** of the new isiXhosa and Afrikaans text.
- **C4** uptime monitor on the download page. **D5** tag `v1.1.0` with the APK. **D4** branch protection on `develop` and `master`.
- **J1** demo video timestamps and a slide; **J3** spread the work; **J5** demo script.
- **I7** needs the Blaze plan; **I8** needs Firebase Support.
