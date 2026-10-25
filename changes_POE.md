# Philisa Volunteers (Android): project guide

> This file and `handover/` live **only on the `changes_final_POE` branch**. They never go into `develop` or `master`, and the branch is deleted before the final submission. Claude Code does not load this file by itself, so start a session with: *"Read changes_POE.md on the changes_final_POE branch (`git show origin/changes_final_POE:changes_POE.md`)."*

The volunteer app for Philisa Abafazi Bethu (PAB), a gender-based violence and community support NPO in Lavender Hill, Cape Town. Volunteers sign up, join activities and read announcements; PAB admins manage activities, announcements and volunteers. Team KANTU, varsity group project (Task 3 follows Task 2, which got 176/270).

Sister repo: the public website, `Kwethukubonga/PAB_WebApp`. Clone both side by side (`PAB_Volunteers` and `PAB_WebApp` in the same folder).

**Team:** this app is owned by Tlhogi and Kwethu. The website is owned by Unathi, Nuha and Ash. Shared items (B3, W10, J2, J5, E2) are split per `handover/HANDOVER.md` section 4.

## Where to start

1. `handover/HANDOVER.md` (on this branch): what's done, what's left in order, the setup steps, and an Owner column. Put your name on what you take.
2. `handover/IMPROVEMENT_PLAN.md` (on this branch): the full checklist (A1–K26 and W1–W12, both repos; section 8 is the Android to-do in order). Each item says how to do it and how to know it's finished.
3. `README.md` lists the requirements PV-FR01–17. All are implemented; keep the README true when behaviour changes.

Read the notes without leaving your work branch: `git show origin/changes_final_POE:handover/HANDOVER.md`.

## Stack

- Kotlin, XML layouts with ViewBinding, MVVM (`ui/**/…ViewModel.kt`, repositories in `data/repository/`), Navigation component (`res/navigation/`), minSdk 26, target 34.
- Firebase Auth (email + Google) and Firestore, project `pab-volunteers`. Rules in `firestore.rules`, tested in `firestore-tests/` against the emulator.
- Notifications: WorkManager (`notifications/UpdatesWorker.kt`) checks every 15 minutes; no FCM yet (needs the Blaze plan).
- Announcement pictures go to Cloudinary (`data/remote/ImageUploader.kt`); details come from `local.properties` (see `local.properties.example`).
- Three languages (English, isiXhosa, Afrikaans) with per-app locales; light and dark palettes. Colours in `values/colors.xml` are generated from `PAB_WebApp/design/tokens.json` (`node tools/tokens/build.js --android ../PAB_Volunteers`, run from the web repo); `values-night/colors.xml` is hand-written.

## Commands

```bash
./gradlew testDebugUnitTest      # unit tests
./gradlew lintDebug
./gradlew assembleDebug
cd firestore-tests && npm ci && npm test   # rules tests (needs Java 17+ for the emulator)
```

CI (`.github/workflows/`) runs unit tests, lint and the rules tests on every push and PR, then builds the APK. On a merge into `master` it deploys the download page (https://pab-volunteers.web.app), that APK and `firestore.rules`, using the `FIREBASE_SERVICE_ACCOUNT` secret.

## Rules for this project

- **Keep the notes out of the app's history:** branch your work from `develop`, never from `changes_final_POE`, and never merge `changes_final_POE` into anything. Ticking items or adding your name is a commit on `changes_final_POE` only.
- **Final submission:** delete the `changes_final_POE` branch in both repos before the final submission (`handover/HANDOVER.md` section 5). Whenever someone mentions the final submission, the deadline, merging to `master` for submission, or a release, remind them of that checklist before doing anything else.
- **No AI attribution anywhere**: no `Co-Authored-By: Claude`/Copilot trailers, no "Generated with" lines, in commits, PRs, code or comments. The README's AI Usage Declaration section is the one place AI use is described.
- **Merge PRs with "Create a merge commit"**, never squash: the marker grades individual contribution from the history.
- Work on `feature/<name>/<topic>` branches, PR into `develop`, then `develop` → `master`.
- Every user-facing string goes in `strings.xml` in all three languages (`values`, `values-xh`, `values-af`). Tap targets 48dp, text in `sp`.
- Never commit secrets: Firebase service account keys (the seed script in `tools/seed` needs one; keep it outside the repo), `local.properties`, release keystores.
- Never delete data in the live Firebase project. Test rule changes in `firestore-tests` first, then publish `firestore.rules`.
- Starting the emulator is slow on the team's machines; prefer unit tests and the rules tests, and run the app on a real phone when you need to see it.
