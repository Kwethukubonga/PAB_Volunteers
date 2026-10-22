# Improvement Plan: Philisa Volunteers (Android) and Help Centre (Web)

This plan turns three sources into one checklist:

1. **Task 2 feedback** (marked by Amber Bruil): 176/270, 65%. Section 1 Code and Implementation 127/210, Section 2 Presentation 49/60.
2. **An audit against [Impeccable](https://github.com/pbakaus/impeccable)** (v4.6): its craft floor, its web and Android audit criteria, and its harden, clarify, onboard and polish guidance.
3. **Suggestions raised while building the app**: settings features, email delivery, notifications.

Each item names the repo it belongs to, its priority, how to do it, and how to know it is finished. Items that came from the marker's feedback are tagged with the rubric criterion they address.

**Progress (22 October 2026):** 39 of 100 done, 10 part done (marked **Status**). Ticks reflect web `develop` and Android `develop` after the handover. Nothing on web `develop` is live until it is merged to `main`. The handover list is `handover/HANDOVER.md`.

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

- [ ] **A8 · P2 · Web: Move content out of C# classes**
  - **Status (22 Oct):** Part done. Resources moved to `Data/ResourceData.cs` (typed data shared by the page, the API and the offline copy). Programmes are still in `Data/SiteData.cs` and UI text still uses `s.Pick(...)`; no `.resx` yet.
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

- [ ] **C1 · P1 · Web: Move from SQLite to a managed database**
  - **Status (22 Oct):** Code ready: `Data/DatabaseProvider.cs` switches to Npgsql when the connection string points at Postgres. Still to do: provision the database on Railway, set the connection string, turn on backups and test a restore.
  - Use Railway's Postgres plugin, Neon or Supabase, with the Npgsql EF Core provider.
  - Put the connection string in an environment variable, run migrations on deploy, and turn on automatic backups.
  - Write down the restore steps and test them once.
  - **Done when:** the live site runs on Postgres, restarting or redeploying loses nothing, and a backup has been restored in a test.

- [x] **C2 · P0 · Web: Health check**
  - Add `AddHealthChecks()` with a database check, mapped to `/health`, and set it as Railway's health check path so broken deploys aren't promoted.
  - **Done when:** `/health` returns 200 when healthy and 503 when the database is unreachable.

- [ ] **C3 · P1 · Android: Put the download page live**
  - The README still says the Firebase Hosting download page is "in progress". Deploy it (see D2), add the link to the README, and test the APK download on a phone.

- [ ] **C4 · P2 · Both: Uptime monitoring**
  - Add a free uptime monitor (for example UptimeRobot) on `/health` and the download page, and put the uptime figure in the README and the presentation.

### D. CI/CD
*Rubric: GitHub CI/CD (11/20), Version Control*

- [x] **D1 · P0 · Web: Deploy automatically after the tests pass**
  - Add a `deploy` job to `dotnet-tests.yml` that runs after `test`, only on `main`, using the Railway CLI with a `RAILWAY_TOKEN` secret. Or connect Railway to GitHub with "Wait for CI" turned on.
  - **Done when:** merging to `main` updates the live site with no manual step, and a failing test blocks the deploy.

- [ ] **D2 · P0 · Android: Deploy hosting and rules from the pipeline**
  - After `build` passes on `master`, publish the APK and download page to Firebase Hosting (`FirebaseExtended/action-hosting-deploy` with a service account secret).
  - After the rules tests pass, run `firebase deploy --only firestore:rules`, so the tested rules are always the live ones.
  - **Done when:** a merge to `master` updates the download page and the live rules without anyone running `firebase deploy`.

- [x] **D3 · P1 · Web: More tests in CI**
  - **Status (22 Oct):** Done. `coverlet.collector` added; CI puts the coverage summary on the run page and keeps the report as an artifact.
  - Add the A1–A3 and B4 tests, collect coverage (`coverlet`), and keep the test and coverage reports as artifacts the way the Android pipeline does.

- [ ] **D4 · P2 · Both: Branch protection, badges and Dependabot**
  - **Status (22 Oct):** Part done (web): `.github/dependabot.yml` and a CI badge. Still to do: branch protection on both repos, Dependabot and badge on Android.
  - Require CI to pass and one review before merging into `develop` and `main`/`master`.
  - Add CI status badges to both READMEs.
  - Add `.github/dependabot.yml` for Gradle, NuGet, npm and GitHub Actions. Lint currently lists 63 outdated dependencies.

- [ ] **D5 · P2 · Both: Versioned releases**
  - Tag releases (`v1.1.0`), attach the APK to a GitHub Release, and generate release notes from merged pull requests.

### E. Design system and visual consistency
*Rubric: Visual Design & Branding (7/10). Impeccable: craft floor, document, typeset, quieter*

- [ ] **E1 · P1 · Both: Define the palette once**
  - **Status (22 Oct):** Web done: `design/tokens.json` and `tools/tokens/build.js` write the Tailwind theme and Android `colors.xml`. The Android side is on branch `feature/kwethu/design-tokens` (PAB_Volunteers) with no PR yet. Night colours are still hand-written.
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
  - **Status (22 Oct):** Web done (three headings). Android labels (`eyebrow_our_programmes`, `eyebrow_testimonials`, `admin_portal_label`) still there.
  - Impeccable bans the small caps label above a heading outright.
  - Android: `eyebrow_our_programmes`, `eyebrow_testimonials`, `admin_portal_label`.
  - Web: "What We Do", "Get Help" and similar labels in uppercase with wide letter spacing (about 18 across 8 pages).
  - Keep labels that carry information, such as "Step 1 of 2" and field labels.

- [x] **E5 · P2 · Web: Replace emoji with real icons**
  - Emoji stand in for icons on six pages (About 🤝💜❤✊⚖🌍, Contact 📞✉📍🚨, Index 📋💬, ProgrammeDetail 🚨, RequestSupport 📍, Resources 🏘🚨⚖👶🧠❓), next to the stroke SVG icons in the header.
  - Use one icon family. Material Symbols is a good fit because Android already uses Material icons.

- [ ] **E6 · P2 · Android: Review the header gradient**
  - Every header is a 135° purple gradient. Purple fits PAB's brand; the gradient on every screen is the generic part.
  - Try a solid `purple_900` band with lighter purple surfaces on the screens below, and show both versions to PAB.
  - Whatever stays, it must pass F9.

- [ ] **E7 · P3 · Android: Rethink the stat tiles**
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

- [ ] **F9 · P1 · Android: Fix the muted header text contrast**
  - **Status (22 Oct):** Fixed on branch `feature/kwethu/design-tokens` (white at 85%, plus darker success and error pill text). Needs a PR into Android `develop`.
  - The header gradient runs at 135°, so its lightest end (`purple_500`) sits at the top left, where "STEP 1 OF 2" and similar 12sp labels in `text_on_purple_muted` (#D8B4FE) appear. That's about 3.2:1; it needs 4.5:1.
  - Use white at 85% opacity, darken the light end of the gradient, or flip the angle.

- [ ] **F10 · P1 · Android: Navigation rail on tablets**
  - Impeccable's Android rule: "Never ship a phone bottom-bar untouched on a tablet."
  - Add `layout-sw600dp/activity_volunteer_main.xml` and `activity_admin_main.xml` using `NavigationRailView` with the same menu, and set it up the same way as the bottom bar.
  - **Done when:** a tablet shows the rail on the side and a phone still shows the bottom bar.

- [ ] **F11 · P2 · Android: Predictive Back**
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

- [ ] **F14 · P2 · Android: Update only the rows that changed**
  - Seven adapters call `notifyDataSetChanged()`, which lint flags.
  - Move them to `ListAdapter` with `DiffUtil`, so only changed rows redraw, and add item animations.

### G. UX writing and states
*Impeccable: clarify, onboard, harden. Rubric: UX & Feedback*

- [ ] **G1 · P2 · Android: Snackbars instead of Toasts**
  - Seven places use Toasts for "Activity saved", "Profile updated" and similar. Material uses Snackbars for short confirmations.
  - Show them on the screen the person returns to; the shared `AppMessage` helper already uses Snackbar.

- [ ] **G2 · P2 · Android: Give empty states a button**
  - Empty Schedule gets a "Browse activities" button, empty Favourites gets "Find opportunities", and an empty admin list gets "Create activity".

- [ ] **G3 · P3 · Android: Name the action in delete dialogs**
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
  - Run Lighthouse CI on pull requests, with minimum scores for Performance and Accessibility.

### I. Android features (from the settings research and earlier suggestions)

- [ ] **I1 · P2 · Android: Activity reminders**: "Remind me the day before" and "1 hour before" for joined activities, using WorkManager, with on/off switches in Settings.
- [ ] **I2 · P2 · Android: "Only my programmes"**: an option to be notified only about new activities in the programmes the volunteer chose.
- [ ] **I3 · P2 · Android: Add to calendar**: a button on activity details that opens the phone's calendar with the details filled in (`CalendarContract`).
- [ ] **I4 · P2 · Android: Contact Philisa**: call, WhatsApp, email and directions to 55 Strauss Road, in Settings.
- [ ] **I5 · P2 · Android: Privacy and POPIA**: a privacy page and "Download my data" (the profile and sign-ups as a file).
- [ ] **I6 · P3 · Android: About**: app version, PAB website and social links.
- [ ] **I7 · P3 · Android: Instant notifications**: Firebase Cloud Messaging through Cloud Functions. Needs the paid Blaze plan; until then the 15-minute background check stays.
- [ ] **I8 · P2 · Android: Reset email reaching inboxes**: once Firebase Support enables template editing, set the sender name, subject and wording, and a custom sending domain if PAB's domain is available.

### J. Process and presentation
*Rubric: Requirements Alignment (4/5), Architecture (11/15), Branching (12/15), Presentation*

- [ ] **J1 · P1 · Both: Requirements traceability table**
  - **Status (22 Oct):** Web done: README section 2 maps PAB-FR01–15 to page, code and test. Still to do: the same table in the Android README (PV-FR01–17), demo timestamps, and a slide.
  - For every requirement (PV-FR01–FR17 and PAB-FR01–FR10), list the screen, the code, the test that proves it, and the timestamp in the demo video.
  - Put it in both READMEs and on a slide. This is the "elaboration" the marker asked for.

- [ ] **J2 · P1 · Both: Architecture and threat model**
  - One diagram showing both clients, the shared API (B1), both databases, authentication, CI/CD and hosting.
  - A short threat model for the gender-based violence data: who could misuse it, and which of A1, A2, A5, A6 and G4 stops them.
  - This is what the 12–15 Architecture band ("expert-level technical defence") needs.

- [ ] **J3 · P1 · Team: Spread the work**
  - The marker noted that one member did far less on Android.
  - Give each of the five members whole workstreams across both repos, with their own branches and pull requests, reviewed by someone else.

- [ ] **J4 · P2 · Both: Commit and pull-request hygiene**
  - Use descriptive commit messages and a pull request template (what changed, how it was tested, screenshots).
  - Avoid stray template commits like the "Update fmt.Println message from 'Hello' to 'Goodbye'" one now on Android `master`.

- [ ] **J5 · P2 · Both: Demo script**
  - Show the pipeline deploying a change by itself, a validation error, the 429 rate-limit page, the staff view receiving a request, a notification arriving, and the app in all three languages and dark mode.

- [ ] **J6 · P0 · Both: Bring the READMEs up to date**
  - **Status (22 Oct):** Web README restored and current (staff area, API, Docker HTTPS setup). Android README still says the download page is in progress; update it when C3 lands.
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

- [ ] **K1 · P1 · Android: Rebuild Settings as list rows** (from `ui/profile/SettingsScreen.kt`)
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

- [ ] **K2 · P1 · Android: Change password from Settings** (from `SettingsScreen.kt`, `SettingsViewModel.sendPasswordReset`)
  - For email accounts only (hidden for Google accounts). Show the account's email as the supporting line, a small spinner while sending, and a Snackbar "Reset link sent to …".
  - Today the reset is only reachable from the sign-in screen.

- [ ] **K3 · P1 · Android: Delete account in two calm steps** (from `ui/profile/DeleteAccountScreen.kt`)
  - **Step 1:** a page listing what will be lost (hours, upcoming places, history), with "No, go back" as the big button and "Yes, continue" as the quiet one.
  - **Step 2:** confirm with the password (or Google, for Google accounts) and type DELETE.
  - **Why:** this replaces today's "sign in again" dead end. PAB currently signs the person out when their sign-in is older than 5 minutes; Collective asks them to confirm again on the spot instead.

- [ ] **K4 · P1 · Android: App-wide offline banner** (from `util/NetworkMonitor.kt`, `ui/navigation/ConnectivityViewModel.kt`, `CommonComponents.OfflineBanner`)
  - A slim bar under the header on every screen while there's no connection, so people know before they tap Join.
  - Writes queued offline show "Sending…".

- [ ] **K5 · P2 · Android: Error state with Retry** (from `CommonComponents.ErrorState`)
  - When a list fails to load, show the reason and a Retry button in place of the list, instead of a Snackbar over an empty screen.

- [ ] **K6 · P2 · Android: Main action pinned at the bottom** (from `CommonComponents.DetailScaffold`, `BottomActionBar`, `PrimaryActionButton`)
  - Join or Leave on activity details, and Save in the editors and Edit Profile, sit in a bar at the bottom within thumb reach that rises above the keyboard.
  - The button turns into a spinner while saving and is always at least 48dp tall.
  - Pairs with E10 (top app bar for sub-screens).

- [ ] **K7 · P2 · Android: Solid brand surfaces and tinted neutrals** (from `ui/theme/Color.kt`, `BrandComponents.HeroHeader`)
  - Replace the gradient headers with one solid brand panel, and tint the greys slightly towards PAB's lavender (Collective's light and dark neutrals are violet-tinted).
  - This resolves E6 and F9 and matches Impeccable's "tinted greys" advice.

- [ ] **K8 · P2 · Android: First-run introduction** (from `ui/onboarding/OnboardingScreen.kt`)
  - Three swipeable pages before sign-in on a new install: find an activity, join in one tap, track your hours. Include Skip, Next and "Get started", and a replay option in Settings, About.
  - Use PAB's own photos.

- [ ] **K9 · P2 · Android: Home as a planner** (from `ui/home/HomeScreen.kt`)
  - **Next activity card:** a large brand-colour card at the top showing the next activity ("Community Soup Kitchen · Sat 09:00 · Philisa Centre"), or a nudge to find one.
  - **Week strip:** the coming week, with a dot on days that have something booked. Tapping a day shows that day's plan.
  - Replaces the four stat tiles (E7); hours stay on Profile.

- [ ] **K10 · P2 · Android: Schedule like an order list** (from `ui/profile/HistoryScreen.kt`)
  - Active and Past tabs, status chips with counts ("Upcoming (3)"), a newest/oldest toggle, and relative times ("in 2 days", "3 hours ago").

- [ ] **K11 · P3 · Android: One colour per person** (from `Color.AvatarColors`)
  - Volunteer initials in admin lists get a colour picked from their ID, so the same person always looks the same, instead of every avatar being lavender.

- [ ] **K12 · P2 · Android: Volunteer safety on the day** (from `ui/safety/TripSafety.kt`)
  - **Share where I'm volunteering:** activity, place, time and current location, sent through the share sheet to WhatsApp or SMS. Nothing is stored by the app.
  - **On the day of a joined activity:** an "I need help" sheet with Call 112, Call 10111, Call PAB's crisis line, and a ready-written text to the emergency contact from K1.
  - The dialler and SMS app always open with everything filled in, so nothing is sent without the person tapping.
  - Most useful for Search and Rescue and community outreach activities.

- [ ] **K13 · P2 · Android: Fingerprint lock, especially for admins** (from `security/AppLock.kt`, `security/BiometricCredentialStore.kt`)
  - An optional "Lock the app when it reopens" using Android's fingerprint or face check.
  - Admin phones show every volunteer's phone number and area, so suggest it to admins on first sign-in (POPIA).

- [ ] **K14 · P2 · Android: Reminders with a chosen lead time** (from `notifications/DepartureReminders.kt`, `util/ReminderTimes.kt`)
  - Settings choice: Off / 1 hour / 1 day before a joined activity, scheduled with WorkManager so they arrive when the app is closed. This is how to build I1.

- [ ] **K15 · P3 · Android: Don't notify about what's on screen** (from `notifications/ActiveChat.kt`)
  - Skip the "new announcement" notification while Community is open, and "activity full" while that activity's sign-up list is open.

- [ ] **K16 · P1 · Android: Google sign-in through Credential Manager** (from `ui/auth/GoogleCredentialSignIn.kt`)
  - Switch to `GetSignInWithGoogleOption`. This removes the deprecated `GoogleSignIn` APIs behind the build warnings, and gives the modern account picker.

- [ ] **K17 · P3 · Android: Tagged logging without personal data** (from Collective's README Logging table)
  - One `Log` tag per class for sign-in, joins and leaves, notifications and connectivity changes. Log user IDs only, never emails or phone numbers. Document the tags in the README.

- [ ] **K18 · P3 · Android: ViewModel tests** (from Collective's `ui/**/ViewModelTest.kt`, `MainDispatcherRule.kt`)
  - PAB's 66 unit tests cover utilities and models. Add tests for `VolunteerViewModel` and `AdminViewModel` (join, leave, offline, error messages) with a test dispatcher. Turbine and MockK, as Collective uses, make this easier.

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

- [x] **W8 · P2 · Web: Hosting configuration in the repo** (from `render.yaml`)
  - Add `railway.json` (or `railway.toml`) with the build command, health check path (`/health`, from C2) and restart policy, so the hosting setup is reviewed and versioned like code.

- [x] **W9 · P2 · Web: Seed script with relative dates** (from `backend/src/seed/plan.ts`, `seedData.ts`)
  - A development seed with programmes, resources and example support requests in every status. Dates count from when it runs, and derived values (counts, statuses) are worked out rather than typed in.
  - Add a test that checks the seed is consistent. Collective's suite does this.

- [ ] **W10 · P1 · Web: Alert PAB's admin phones about new requests** (from Collective's API sending FCM pushes on new bookings)
  - When a support request is saved, the web server sends a push through the Firebase Admin SDK for .NET (`FirebaseAdmin` NuGet) to PAB admin devices: "New support request · Urgent · Food assistance". No personal details go in the notification.
  - Tapping it opens the request through B3.
  - This links the two apps and answers the marker's "notification so PAB staff can act" (A6). The Android app would need FCM added (I7).

- [x] **W11 · P2 · Web: Work offline for emergency numbers** (from Collective's offline banner and cache)
  - Make the Help Centre an installable web app with a service worker that caches the Resources page and emergency numbers, so they open with no data.
  - Show an offline notice on the form, and keep what the person typed until they're back online.

- [ ] **W12 · P2 · Web: Staff request list like Collective's History** (from `ui/profile/HistoryScreen.kt`, `BrandComponents.StatusPill`)
  - **Status (22 Oct):** Part done: longest-waiting / newest toggle and status pill colours from the tokens. Still to do: New / In progress / Closed tabs with counts, relative times, confirm before closing.
  - New / In progress / Closed tabs with counts, urgent requests first, colour-coded status pills with one colour per status everywhere, relative times ("2 hours ago"), and a newest/oldest toggle.
  - Closing or deleting a request asks for confirmation, the way Collective handles destructive actions.
