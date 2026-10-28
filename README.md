# Philisa Volunteers

Developed by **Team KANTU** for **Philisa Abafazi Bethu**

[![Android CI](https://github.com/Kwethukubonga/PAB_Volunteers/actions/workflows/android.yml/badge.svg)](https://github.com/Kwethukubonga/PAB_Volunteers/actions/workflows/android.yml)

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Background](#2-background)
3. [Functional Requirements](#3-functional-requirements)
4. [Non-Functional Requirements](#4-non-functional-requirements)
5. [User Roles and Permissions](#5-user-roles-and-permissions)
6. [Features](#6-features)
7. [Technology Stack](#7-technology-stack)
8. [System Architecture](#8-system-architecture)
9. [Integrations and the API Layer](#9-integrations-and-the-api-layer)
10. [Data Model](#10-data-model)
11. [Security and Privacy](#11-security-and-privacy)
12. [Getting Started](#12-getting-started)
13. [Project Structure](#13-project-structure)
14. [DevOps, Testing and Hosting](#14-devops-testing-and-hosting)
15. [Running Costs](#15-running-costs)
16. [Project Status and Future Work](#16-project-status-and-future-work)
17. [AI Usage Declaration](#17-ai-usage-declaration)
18. [Attendance](#18-attendance)
19. [The Team](#19-the-team)

---

## 1. Introduction

Philisa Volunteers is an Android app that brings volunteering at Philisa Abafazi Bethu (PAB) into one place. Anyone can sign up and start volunteering straight away, with no approval step.

- **Volunteers** find activities, join or leave them, track their hours and read announcements.
- **PAB staff** post activities and announcements, and see who is coming.
- The app is available in **English, isiXhosa and Afrikaans**, supports light and dark mode, adapts to phones and tablets, and keeps working when the signal drops.

### 1.1 Download and Demo Video

- **Download the app:** https://pab-volunteers.web.app
- **Demo video:** https://youtu.be/vqdMx4jWI7Y (unlisted)

### 1.2 Test Accounts

| Role | Email | Password |
|------|-------|----------|
| Administrator | Supplied privately with the submission | Supplied privately with the submission |
| Volunteer | Create your own in the app | Any password with 8+ characters and a symbol |

Administrator details are handed in privately rather than written here. An admin can
see every volunteer's name and phone number, so that account must not be usable by
anyone who happens to read this page.

> **Note:** These are test accounts only. Please don't enter real personal details.

---

## 2. Background

PAB, meaning *"Heal Our Women"* in isiXhosa, was founded in 2008 in Lavender Hill and is now based in Steenberg, Cape Town. It supports women, children and families through ten programmes:

| # | Programme | # | Programme |
|---|-----------|---|-----------|
| 1 | Afterschool Programmes | 6 | Senior Programme |
| 2 | Youth Programme | 7 | Community Feeding |
| 3 | Women Empowerment | 8 | Men's Café |
| 4 | Baby Saver | 9 | Search and Rescue Team |
| 5 | Emergency Safe Houses | 10 | Social Work Services |

### 2.1 How the Requirements Were Gathered

| Date | Engagement | Outcome |
|------|------------|---------|
| 19 May 2026 | Site visit and online meeting with the director | Saw the safe houses, Baby Saver, children's programmes and feeding garden |
| 27 July 2026 | In-person meeting with the director | PAB shared its wish list of digital solutions |
| 4 August 2026 | Presented three project ideas | PAB chose the idea that included this app |

### 2.2 The Problem

| Problem | Today | Our Answer |
|---------|-------|------------|
| Applying is slow | PDF forms sent by email | Sign up in the app and start straight away |
| No central place for information | Volunteers miss activities and news | Activities, schedule and announcements in one app |
| No tool for staff | Tracking volunteers takes time | An admin area to post activities and see who's coming |

---

## 3. Functional Requirements

All 17 requirements are implemented. Each one below names the screen it lives on, the code behind it and the tests that prove it still works, so running the tests (section 14.2) is a check against this table, not just against the code. Unit tests are in `app/src/test/`; tests marked **Rules** run the real `firestore.rules` against the Firestore emulator (`firestore-tests/rules.test.js`).

| ID | Requirement | Role | Where it lives | Code | Proven by |
|----|-------------|------|----------------|------|-----------|
| PV-FR01 | Create an account with email and password, or with Google. | Visitor | Welcome → Sign up, or Continue with Google | `EmailAuthActivity`, `GoogleSignIn` (Credential Manager), `AuthViewModel.createAccount`, `PasswordRules`, `AuthRepository` | `PasswordRulesTest` (length, symbol, confirmation); `UserTest` *a new account is a volunteer, not an admin*; **Rules** *stops a new account being created as an admin* |
| PV-FR02 | Complete a two-step profile and become a volunteer immediately. | Visitor | Profile details → Interests | `ProfileDetailsActivity`, `ProfileInterestsActivity`, `ProfileSetupViewModel`, `UserRepository.saveProfile` | `SignInDestinationTest` *a new account is taken to profile setup first*; `UserTest` *a new account has not finished its profile*; `PhoneNumberTest`; `DateUtilsTest` volunteer ID tests |
| PV-FR03 | Sign in securely and reset a forgotten password by email. | All | Sign in, Forgot password; Settings → Change password | `AuthViewModel.signInWithEmail`, `AuthViewModel.sendPasswordReset`, `SettingsActivity`, `AuthRepository` | `ErrorMessagesTest` (offline, stale session, refused); `AccountRulesTest` (only email accounts get Change password); **Rules** *stops a signed out person reading anything*. The reset email itself is sent by Firebase and checked by hand |
| PV-FR04 | Browse published opportunities and view their full details. | Volunteer | Activities tab → Activity details | `ActivitiesFragment`, `ActivityDetailsFragment`, `ActivityRepository.getPublishedActivities` | `ActivityTest` (spots left, date label, *a new activity starts as a draft*); **Rules** *stops anyone joining a draft activity* |
| PV-FR05 | Join an activity while spots are available. | Volunteer | Activity details → Join | `SignupRepository.join` (one Firestore transaction) | `ActivitySignupTest` (one ID per volunteer and activity); `ActivityTest` *a full activity has no spots left*; **Rules** *lets a volunteer join an open activity*, *stops anyone joining a full activity*, *…joining the same activity twice*, *…signing someone else up*, *…the counter jumping by more than one* |
| PV-FR06 | Leave an activity, which gives the spot back. | Volunteer | Activity details → Leave | `SignupRepository.leave` (one Firestore transaction) | **Rules** *lets a volunteer leave and give their spot back*, *stops a volunteer removing someone else from an activity* |
| PV-FR07 | See activities as Upcoming and Completed, and on a schedule. | Volunteer | Home planner, Schedule tab, Profile tabs | `HomeFragment`, `WeekPlan`, `ScheduleFragment`, `RelativeDay`, `Activity.endsAtMillis`, `DateUtils` | `WeekPlanTest` (seven days from today, dots on booked days, next activity, a day's plan in order); `ActivityTest` (*a later slot today has not finished yet*, *yesterday has finished*); `DateUtilsTest` `endOfActivity` tests; `ActivitySignupTest` *a past sign-up can stand in for an activity that is gone* |
| PV-FR08 | Save opportunities as favourites. | Volunteer | Activities tab → heart | `VolunteerViewModel.setFavourite`, `UserRepository.setFavourite` | **Rules** *lets a volunteer save and remove a favourite*, *stops a volunteer changing someone else favourites*; `UserTest` *a new account has no interests or favourites* |
| PV-FR09 | Read announcements and give them a thumbs up. | Volunteer | Community tab → Announcement | `CommunityFragment`, `AnnouncementDetailsFragment`, `AnnouncementRepository.setThumbsUp` | `AnnouncementTest` (thumbs up count and whose it is); **Rules** *lets a volunteer add / take away their own thumbs up*, *stops … on someone else behalf*, *…stuffing the count*, *…sneaking an edit in alongside a thumbs up* |
| PV-FR10 | Receive notifications about new activities and announcements. | Volunteer | Phone notifications; Settings switches | `UpdatesWorker`, `UpdateRules`, `Notifications`, `NotificationSettings` | `UpdateRulesTest` (*published after checks began is news*, *before is not*, *finished is not*, *already shown is not shown again*) |
| PV-FR11 | View and edit their profile, and delete their account. | Volunteer | Profile → Edit profile; Settings → Delete account | `ProfileFragment`, `EditProfileFragment`, `DeleteAccountActivity`, `AccountRules`, `AccountRepository.deleteCurrentAccount` | `AccountRulesTest` (what deleting would lose, the confirmation word, password or Google); `PhoneNumberTest`; `UserTest` full name tests; **Rules** *lets a volunteer edit their own profile*, *stops … changing their own email / volunteer ID / join date*, *lets someone delete their own account*, *stops an admin deleting a volunteer* |
| PV-FR12 | Be taken to a separate admin area on sign-in. | Administrator | Splash → Admin area | `SplashActivity`, `AuthNavGraph.routeAfterSignIn`, `SignInDestination` | `SignInDestinationTest` (admins always to the admin area, an unknown role never is); `UserTest` *only the admin role counts as admin*; **Rules** *stops a volunteer making themselves an admin* |
| PV-FR13 | View all volunteers and their details. | Administrator | Admin → Volunteers → Volunteer details | `ManageVolunteersFragment`, `VolunteerDetailsFragment`, `UserRepository.getAllVolunteers` | **Rules** *lets an admin read any volunteer*, *stops a volunteer reading someone else*; `DateUtilsTest` `monthsSince` tests |
| PV-FR14 | Create, edit, publish, hide and delete activities. | Administrator | Admin → Activities → Activity editor | `ManageActivitiesFragment`, `ActivityEditorFragment`, `AdminViewModel`, `ActivityRepository` | `ActivityTest` *a new activity starts as a draft*; `DateUtilsTest` `lengthInMinutes` tests; **Rules** *stops a volunteer creating or deleting an activity*, *…changing anything else on an activity*, *lets an admin delete an activity together with its sign-ups* |
| PV-FR15 | See who has signed up for each activity. | Administrator | Admin → Activities → Sign-ups | `ActivitySignupsFragment`, `SignupRepository.getSignupsForActivity` | **Rules** *lets an admin see everyone signed up for an activity*, *stops a volunteer listing everyone signed up*, *stops a volunteer seeing someone else sign-up* |
| PV-FR16 | Create and publish announcements, with an optional picture. | Administrator | Admin → Announcements → Announcement editor | `AnnouncementEditorFragment`, `ImageUploader` (Cloudinary), `AdminViewModel.saveAnnouncement` | `AnnouncementTest` (with and without a picture, *starts as a draft*); **Rules** *stops a volunteer writing an announcement*, *lets an admin edit an announcement*. The picture upload is checked by hand |
| PV-FR17 | Be notified when an activity is fully booked. | Administrator | Phone notifications; Settings switch | `UpdatesWorker`, `UpdateRules.fullActivities` | `UpdateRulesTest` (*every spot taken counts as full*; *spots left*, *a draft*, *no spots set* and *finished* do not) |

**Added during development** (ideas brought over from Collective, another app by a team member):

| ID | Requirement | Role | Where it lives | Code | Proven by |
|----|-------------|------|----------------|------|-----------|
| PV-FR18 | Search activities, and (admins) search volunteers. | Both | Activities tab; Admin → Volunteers | `utils/Search`, `ActivitiesFragment`, `ManageVolunteersFragment` | `SearchTest` (any case and order, every word must match, name, area, ID, programme, place) |
| PV-FR19 | Get directions to an activity. | Volunteer | Activity details → Directions | `utils/Directions` | `DirectionsTest`; the maps app opening is checked by hand |
| PV-FR20 | Say how an activity went; admins see the ratings and comments. | Both | Activity details (after it); Admin → Sign-ups | `FeedbackRepository`, `FeedbackRules`, `FeedbackSummary` | `SafetyRulesTest` (rating 1 to 5, length, average to one decimal); **Rules** *only someone who was signed up*, *not before the day*, *not in someone else name* |
| PV-FR21 | Report a concern to Philisa's admins, who mark it handled. | Both | Activity details, Settings → Safety; Admin → Concerns | `ReportConcernActivity`, `ConcernsFragment`, `ConcernRepository` | `SafetyRulesTest` (empty, too long); **Rules** *only admins read concerns*, *not in someone else name*, *never deleted* |
| PV-FR22 | Ask for a clearance check; admins record it after seeing documents in person. | Both | Settings → Safety, Profile; Admin → Volunteer | `ClearanceRepository`, `ClearanceState` | `SafetyRulesTest` (requested, cleared, expired, lasts a year); **Rules** *a volunteer can only ask*, *can't clear themselves* |
| PV-FR23 | See milestones for time given. | Volunteer | Profile | `Milestone` | `MilestonesTest` (first shift, 10 activities, 25 hours, 3 programmes, progress) |
| PV-FR24 | Get help on the day of an activity: call 112, 10111 or Philisa, or text an emergency contact where you are. | Volunteer | Activity details → "I need help"; Settings → Safety → Emergency contact | `NeedHelpSheet`, `ActivityIntents`, `EmergencyContactRules` | `SafetyRulesTest` (emergency contact needs a name and a working number); **Rules** *only on your own record*. Dialling and texting open the phone's own apps and are checked by hand |
| PV-FR25 | Be reminded before a joined activity, an hour or a day before. | Volunteer | Settings → Notifications → Reminders | `ReminderRules`, `UpdatesWorker` | `PlannerHelpersTest` (due inside the chosen time, not when off, not twice, not once started) |
| PV-FR26 | Add an activity to the phone's calendar, or share where you're volunteering. | Volunteer | Activity details | `ActivityIntents`, `DateUtils.startOfActivity` | `PlannerHelpersTest` (start time on the right day); the calendar and share sheet are checked by hand |
| PV-FR27 | Sign back in with a fingerprint, and optionally lock the app when it reopens. | Both | Settings → Account → Sign in with fingerprint; Welcome → "Sign in as …" | `FingerprintSignIn` (Android Keystore), `FingerprintPrompt`, `AppLock`, `AppLockWatcher`, `LockActivity` | `FingerprintRulesTest` (only for the account it was saved for, the name shown); `PlannerHelpersTest` (the lock comes back only after 30 seconds away, never when off); the fingerprint prompt is checked by hand |

Some steps happen outside the app and can't be reached by these tests: Google's sign-in screen, Firebase sending the reset email, Cloudinary storing a picture, and Android showing the notification. Those are checked by hand on a real phone. The decisions around them (password rules, who counts as new, where someone lands after sign-in, what counts as news) are moved into plain Kotlin (`PasswordRules`, `SignInDestination`, `UpdateRules`) so they are tested.

---

## 4. Non-Functional Requirements

| Category | How the App Meets It |
|----------|----------------------|
| **Responsiveness** | Tablet layouts with a navigation rail down the side instead of the bottom bar, a landscape Welcome screen and two-column lists on tablets. Built with Android's alternate resource folders, so one set of layouts serves every screen size. |
| **Accessibility** | Every tappable control is at least **48dp**. All text scales with the phone's font size and still works at the largest setting. Three languages, light and dark mode. |
| **Usability** | A short two-step sign-up, friendly empty-screen messages, and a confirmation before leaving an activity or deleting an account. |
| **Reliability** | Firestore's offline cache keeps recently loaded data readable without signal. Joining is a single database transaction, so two people can never take the last spot. |
| **Performance** | Images are cached with Glide, lists use RecyclerView, and list screens support pull-to-refresh. |
| **Security** | Firebase Security Rules on every collection, proven by 68 automated tests (see section 11). |

---

## 5. User Roles and Permissions

| Role | Who | How the Role Is Given |
|------|-----|-----------------------|
| Volunteer | Anyone who signs up and completes their profile | Automatically |
| Administrator | Authorised PAB staff | By hand in the Firebase console, never from inside the app |

Both roles use the same sign-in and are taken to their own area. These permissions are enforced by the Security Rules, not just the screens:

| Action | Volunteer | Administrator |
|--------|:---------:|:-------------:|
| Read and edit own profile | ✔ | ✔ |
| Change own role, volunteer ID, email or join date | ✘ | Console only |
| Read other volunteers' profiles | ✘ | ✔ (view only) |
| Join or leave an activity | ✔ | – |
| See who signed up for an activity | Own sign-ups only | ✔ |
| Create, edit or delete activities and announcements | ✘ | ✔ |
| Delete own account | ✔ | – |
| Delete another user's account | ✘ | ✘ |

---

## 6. Features

### 6.1 Volunteers

| Tab | What It Does |
|-----|--------------|
| **Home** | A planner: the next activity, the coming week with a dot on each booked day, the plan for the day picked, one line for what's upcoming and hours done, and the latest from Philisa |
| **Activities** | Search, browse or save favourites, see spots left, and join or leave in one tap. Activity details give directions, and afterwards ask "How did it go?" |
| **Schedule** | Upcoming and completed activities. Completed ones count towards hours. |
| **Community** | Announcements from PAB, with pictures and a thumbs up |
| **Profile** | A colour of their own, volunteer ID and time volunteering, clearance status, milestones (first shift, 10 activities, 25 hours, 3 programmes), and their activities as Upcoming and Past with "in 3 days" style times. Edit any time; the phone number is spaced as you type. |

### 6.2 Administrators

| Tab | What It Does |
|-----|--------------|
| **Overview** | Totals, recent volunteers and quick actions, including **Concerns** reported by volunteers |
| **Activities** | Create, edit, publish, hide or delete activities, and see who signed up and how it went |
| **Volunteers** | Search and view everyone's details, and record a volunteer's clearance after seeing their documents. Admins can't remove volunteers. |
| **Posts** | Create and publish announcements, with an optional picture |

### 6.3 Everyone

- **Sign-in:** Google or email, with password reset.
- **Settings:** rows grouped into Account (how you signed in, change password for email accounts), Appearance (theme and language as lists), Notifications (tap anywhere on a row to switch it), and About (contact Philisa, app version). Sign out and Delete account sit at the bottom, away from everyday settings.
- **Notifications:** a background check about every 15 minutes. Volunteers hear about new activities and announcements, and get a reminder an hour or a day before an activity they joined. Admins hear when an activity is full.
- **Fingerprint sign-in:** turned on in Settings → Account, it puts "Sign in as Thandi" on the Welcome screen, so coming back is one touch. An email account's password is kept on the phone only, encrypted with an Android Keystore key that just a fresh fingerprint check can open, and that Android destroys if a fingerprint is added. A Google account needs no secret: Google signs back in after the fingerprint. Under it, "Also lock when the app reopens" asks for the fingerprint again after 30 seconds away (suggested for admins, whose phones show volunteers' details).
- **Safety:** an emergency contact for "I need help" on the day of an activity, a clearance check, and reporting a concern. Emergency numbers are shown first.
- **Offline:** a slim bar across the top of every screen says when there is no connection, before anything is tapped. Recently loaded data stays readable; joining or saving needs a connection.

---

## 7. Technology Stack

| Area | Technology |
|------|------------|
| Platform | Native Android: minimum SDK 26 (Android 8.0), target SDK 34 |
| Language | Kotlin 2.0.20 |
| User interface | XML layouts, Material Components, View Binding, Jetpack Navigation |
| Architecture | MVVM with ViewModel, LiveData and Kotlin Coroutines |
| Backend | Firebase Authentication and Cloud Firestore, with Security Rules |
| Pictures | Cloudinary uploads, Glide loading |
| Notifications | WorkManager |
| Typefaces | Lora and Source Sans 3, the website's own, bundled under the Open Font Licence (see `licenses/`) |
| Testing and CI | JUnit, Firestore emulator rules tests (Node.js), Android Lint, GitHub Actions |

**Why Firebase:** it works offline in areas with weak signal, enforces security in the database itself, handles Google sign-in and password resets, and stays within the free tier at PAB's size.

---

## 8. System Architecture

### 8.1 Layered Architecture

```
┌──────────────────────────────────────────────────────────────┐
│  UI LAYER (ui/)                                              │
│  Activities and Fragments · RecyclerView Adapters            │
├──────────────────────────────────────────────────────────────┤
│  VIEWMODEL LAYER                                             │
│  AuthViewModel · ProfileSetupViewModel · VolunteerViewModel  │
│  · AdminViewModel                                            │
├──────────────────────────────────────────────────────────────┤
│  DATA LAYER (data/)                                          │
│  Repositories: Auth · Account · User · Activity · Signup ·   │
│  Announcement                                                │
├──────────────────────────────────────────────────────────────┤
│  SERVICES                                                    │
│  Firebase Auth · Firestore · Cloudinary · WorkManager        │
└──────────────────────────────────────────────────────────────┘
```

Screens never talk to Firebase directly. They go through a ViewModel, which calls a repository.

### 8.2 Navigation

| Graph | Screens |
|-------|---------|
| `AuthNavGraph` | Splash, Welcome, About PAB, Sign-in, Profile setup |
| `VolunteerNavGraph` | Home, Activities, Schedule, Community, Profile |
| `AdminNavGraph` | Overview, Volunteers, Activities, Posts |
| `AppNavGraph` | Chooses a graph based on sign-in, profile completion and role |

### 8.3 Design Patterns

| Pattern | Where It Is Used | Why |
|---------|------------------|-----|
| Repository | The six repositories | Keeps all Firebase code in one place |
| MVVM | The four ViewModels | Separates screens from data loading, and survives screen rotation |
| Observer | LiveData | Screens update automatically when data changes |
| Singleton | `FirestoreManager`, `AppLanguage`, `ThemePreference` | One shared database connection and one place for settings |
| Adapter | `ActivityAdapter`, `AnnouncementAdapter` and others | Turns lists of data into rows on screen |

---

## 9. Integrations and the API Layer

### 9.1 External Services

| Service | What It Does | How We Connect |
|---------|--------------|----------------|
| Cloudinary | Hosts announcement pictures | REST API over HTTPS, with our own client |
| Firebase Authentication | Sign-in, Google Sign-In, password reset | Official Android SDK |
| Cloud Firestore | Stores every record, plus the offline cache | Official Android SDK |

We use the Firebase SDKs rather than raw REST calls because they provide the offline cache, automatic retries and token refresh, which matter on an unreliable connection.

### 9.2 The Repository Layer (Internal API)

Six repositories expose **32 operations**, and these are the only way screens reach data. All are `suspend` functions, so nothing freezes the screen.

| Type of Operation | Returns | Why |
|-------------------|---------|-----|
| Writes | `Result<T>` | The caller has to handle a possible failure |
| Reads | The value, or `null` | Simple to use |

```kotlin
suspend fun join(activity: Activity, user: User): Result<Unit>
suspend fun getPublishedActivities(): List<Activity>
```

When our first picture provider started refusing uploads, switching to Cloudinary changed **one file**. No screen or ViewModel had to change.

### 9.3 The Cloudinary REST Client

Written by hand in `data/remote/ImageUploader.kt`, so every part of the request is explicit.

| Part | Detail |
|------|--------|
| Endpoint | `POST https://api.cloudinary.com/v1_1/{cloud_name}/image/upload` |
| Content type | `application/x-www-form-urlencoded` |
| Request body | The picture as a base64 data URI, plus the `upload_preset` |
| Success | HTTP 200–299, returning `secure_url` |
| Timeouts | 30 seconds to connect, 30 seconds to read |

```kotlin
val code = connection.responseCode
val stream = if (code in 200..299) connection.inputStream else connection.errorStream
val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

if (code !in 200..299) throw failureFor(response, code)
return JSONObject(response).getString("secure_url")
```

- Firestore only stores the returned link, never the picture itself.
- Uploads are **unsigned**, so no Cloudinary secret ships inside the APK, where anyone could extract it.

### 9.4 Error Handling

All errors are translated in one place, `utils/ErrorMessages.kt`, into a message the user can act on, in their chosen language.

| Error | What the User Sees |
|-------|--------------------|
| Firestore `UNAVAILABLE` or `DEADLINE_EXCEEDED` | A "no connection" message |
| Firestore `PERMISSION_DENIED` | A clear permission message |
| Wrong password | "Incorrect email or password" |
| Email already registered | "That email already has an account" |
| Failed transaction, e.g. activity full | The real reason, found inside the wrapped error |

- The app checks the connection before saving. Firestore quietly queues offline writes, so without this check a save would seem to hang.
- 11 unit tests cover this mapping.

---

## 10. Data Model

### 10.1 Entity Relationship Diagram

```mermaid
erDiagram
    USERS ||--o{ ACTIVITY_SIGNUPS : "signs up"
    ACTIVITIES ||--o{ ACTIVITY_SIGNUPS : "is joined through"
    USERS ||--o{ ACTIVITIES : "creates (admin)"
    USERS ||--o{ ANNOUNCEMENTS : "posts (admin)"
    USERS }o--o{ ANNOUNCEMENTS : "gives thumbs up"
    USERS }o--o{ ACTIVITIES : "saves as favourite"

    USERS {
        string uid PK
        string firstName
        string lastName
        string email
        string phone
        string area
        list programmeInterests
        list favouriteActivityIds FK
        string role
        string volunteerId
        boolean profileComplete
        long joinedDate
    }

    ACTIVITIES {
        string id PK
        string title
        string programme
        long dateMillis
        string startTime
        string endTime
        string location
        string volunteerRole
        int totalSpots
        int filledSpots
        string description
        string status
        string createdBy FK
        long publishedAt
    }

    ACTIVITY_SIGNUPS {
        string id PK "activityId_userId"
        string activityId FK
        string userId FK
        string volunteerName
        string activityTitle
        long dateMillis
        string location
        long signedUpDate
    }

    ANNOUNCEMENTS {
        string id PK
        string title
        string messageBody
        string imageUrl
        string status
        string createdBy FK
        long publishedAt
        list thumbsUpBy FK
    }

    CLEARANCES {
        string userId PK "and FK to USERS"
        string status "requested or cleared"
        long requestedAt
        long expiresAt
        string checkedBy
    }

    CONCERNS {
        string id PK
        string reportedBy FK
        string activityId FK "optional"
        string message
        string status "new or handled"
        long createdAt
    }

    ACTIVITY_FEEDBACK {
        string id PK "activityId_userId"
        string activityId FK
        string userId FK
        int rating "1 to 5"
        string comment
    }

    USERS ||--o| CLEARANCES : "is checked"
    USERS ||--o{ CONCERNS : "reports"
    ACTIVITIES ||--o{ ACTIVITY_FEEDBACK : "gets"
    USERS ||--o{ ACTIVITY_FEEDBACK : "gives"
```

| Relationship | Cardinality | Meaning |
|--------------|-------------|---------|
| Users → Activity Signups | One to many | A volunteer can join many activities |
| Activities → Activity Signups | One to many | An activity has many volunteers, up to `totalSpots` |
| Users → Activities / Announcements | One to many | An admin creates many of each |
| Users ↔ Announcements | Many to many | Thumbs up, stored in `thumbsUpBy` |
| Users ↔ Activities | Many to many | Favourites, stored in `favouriteActivityIds` |
| Users → Clearances | One to zero or one | A volunteer's background check, kept apart from their profile so a profile save can never overwrite an admin's decision |
| Users → Concerns | One to many | Concerns a volunteer has reported; only admins can read them |
| Activities → Feedback | One to many | One rating per volunteer who was there, saved as `activityId_userId` |

### 10.2 Design Decisions

| Decision | Reason |
|----------|--------|
| Sign-up IDs are `activityId_userId` | The same person can never take two spots on one activity |
| Joining is a Firestore transaction | Two people can't take the last spot at the same time |
| Sign-ups copy the activity's details | Volunteers keep their history and hours, even if the activity is deleted |
| Leaving deletes the sign-up | There's no approval status, so the spot simply goes back |
| `role` is `volunteer` or `admin`; activities and announcements are `draft` or `published` | Keeps states simple |

---

## 11. Security and Privacy

**Firestore Security Rules** (`firestore.rules`) protect every collection, not just the screens:

- Volunteers can only read and edit their own profile, and can't change their role, volunteer ID, email or join date.
- Every new account must start as a `volunteer`.
- Only admins can create or change activities and announcements.
- Draft activities are admins' only. A volunteer can still open one they joined before it was hidden, so they can read it and leave.
- Joining changes the spot count by exactly one, together with the volunteer's own sign-up, and never above the total.
- Volunteers can only add or remove their own thumbs up.
- An emergency contact is two fields on the volunteer's own record, which only they and admins can read.
- A volunteer can only **ask** for a clearance check; only an admin can mark someone as cleared.
- **Concerns** can be sent by anyone signed in, as themselves, and read only by admins. They are never deleted.
- **Feedback** only comes from someone who was signed up, once the activity's day has come, rated 1 to 5.

**Other measures:**

- **Tested:** 68 rules tests run against the Firestore emulator on every push.
- **Delete my account** removes the volunteer's data and gives their spots back, in line with POPIA. It takes two steps: a page listing what will be lost (hours, upcoming places, history), then confirming with their password or Google account and typing the confirmation word. Firebase needs that fresh confirmation before it deletes a sign-in, and nothing is removed until it succeeds.
- **Passwords** need at least 8 characters, including a symbol.
- **Safe house locations** are never shown in the app.
- **Secrets** (Cloudinary details, release signing keys, Firebase admin keys) are kept out of Git.

---

## 12. Getting Started

**You need:** Android Studio, JDK 17, and an Android 8.0+ phone or emulator.

**Step 1: Clone the repository and use the `develop` branch**

```bash
git clone https://github.com/Kwethukubonga/PAB_Volunteers.git
cd PAB_Volunteers
git checkout develop
```

**Step 2: Add the Cloudinary details**

Copy `local.properties.example` to `local.properties` and add the details (ask a team member). Without them the app still runs, but picture uploads won't.

**Step 3: Run the app**

Open the folder in Android Studio, let Gradle sync, and press **Run**.

> **Note:** `google-services.json` and a shared debug key are included, so Google sign-in works on every team member's debug build.

**Making an admin:** sign up in the app, change that user's `role` in the Firestore `users` collection from `volunteer` to `admin`, and sign in again.

**Sample data:** `tools/seed/seed.js` adds one activity and one announcement per programme, plus activities today, tomorrow and in the past and one draft. With `--volunteer <email>` it also signs that volunteer up, rates a past activity, asks for a clearance and sends a sample concern, so every screen has something to show. `--remove` takes all of it out again. Instructions are at the top of the file. It needs a Firebase service account key, which must never be committed.

---

## 13. Project Structure

```
PAB_Volunteers/
├── app/src/main/java/com/kantu/pab_volunteers/
│   ├── data/            # Models, repositories, Firebase and Cloudinary
│   ├── navigation/      # Navigation graphs and sign-in routing
│   ├── notifications/   # Background check and notification channels
│   ├── ui/              # Auth, profile setup, volunteer, admin and settings screens
│   └── utils/           # Language, theme, dates, phone numbers, error messages
├── app/src/main/res/    # Layouts (incl. tablet and landscape) and strings (en, xh, af)
├── app/src/test/        # Unit tests
├── firestore-tests/     # Security rules tests
├── tools/seed/          # Sample data script
├── firestore.rules      # Database security rules
└── .github/workflows/   # CI pipeline
```

---

## 14. DevOps, Testing and Hosting

### 14.1 Branching

```
 feature/* ─► Pull Request ─► CI checks ─► Peer review ─► develop ─► master
```

`master` is release-ready, and `develop` is where features are brought together.

### 14.2 Continuous Integration

GitHub Actions runs on every push and Pull Request to `develop` and `master`. The project has **257 automated tests**, and section 3 maps them to the requirements.

| Job | What It Proves |
|-----|----------------|
| **Unit tests** (189, JUnit) | Dates and hours, spots left, when an activity has finished, error messages, SA phone numbers, password rules, where sign-in leads, what counts as a new notification, what deleting an account would lose, search, milestones, clearance and feedback rules, phone spacing, the week planner, reminders, the app lock, fingerprint sign-in, avatar colour contrast, and awkward input: names longer than any real name, emoji in a phone field, hundreds of activities at once |
| **Security rules tests** (68, Firestore emulator) | Drafts hidden from volunteers, no overbooking, no joining twice, joining and leaving keep the spot count right, no editing other people's data or favourites, no self-promotion to admin, only admins see who signed up, clearances, concerns and feedback |
| **Lint** | Code problems and missing translations |
| **Build APK** | Runs only when the three checks above pass. The APK can be downloaded for 30 days. |
| **Deploy** | On a merge into `master`: puts that build's APK and the download page on Firebase Hosting, and publishes `firestore.rules`, so the live rules are the ones the tests above just passed against (section 14.3) |
| **Release** | On a version tag such as `v1.1.0`: makes a GitHub Release with the APK attached and notes written from the pull requests merged since the last tag |

Run them yourself:

```bash
./gradlew testDebugUnitTest                      # unit tests (Windows: gradlew.bat)
./gradlew lintDebug                              # lint
cd firestore-tests && npm install && npm test    # rules tests (needs Node.js 20 and JDK 17)
```

The app has also been tested on physical Android phones. User Acceptance Testing with PAB is planned before release.

### 14.3 Hosting

The app is shared through a download page on **Firebase Hosting**, so PAB can send volunteers a link rather than a file.

- **Download page:** **https://pab-volunteers.web.app**

The page offers the APK, says what the app does and walks through installing from outside the Play Store. It is deployed by the pipeline: merging into `master` publishes the page, that build's APK and `firestore.rules` in one step, so the download is never older than `master` and the live rules are always the tested ones. The deploy needs one repository secret, `FIREBASE_SERVICE_ACCOUNT`, holding a service account key with the Firebase Hosting Admin and Firebase Rules Admin roles.

The workflow can also be started by hand from the Actions tab, which is how a deploy gets checked before it matters:

| Choice | What it does |
|--------|--------------|
| `nothing` | The default. Runs the checks and the build, deploys nothing |
| `preview` | Puts the page on a temporary URL that expires in a week, leaving the live page alone |
| `live-hosting` | Publishes the page and the APK for real, and leaves `firestore.rules` untouched |
| `live` | Publishes the page, the APK and the rules, the same as a merge into `master` |

`live-hosting` exists because tightening a rule can lock out a phone still running an older APK. When a release changes the rules, the download goes out first, and the rules follow once volunteers have updated.

### 14.4 Releases

Tagging a commit `v1.1.0` and pushing the tag builds it, and if the checks pass makes a GitHub Release with the APK attached and notes written from the pull requests merged since the last tag:

```bash
git tag v1.1.0
git push origin v1.1.0
```

The attached APK is the debug build, the same one the download page offers, because release signing is not set up yet.

---

## 15. Running Costs

| Item | Estimated Cost |
|------|----------------|
| Firebase Authentication and Firestore | R0 within the free tier (50,000 reads a day) |
| Firebase Hosting (download page) | R0 on the free plan |
| Cloudinary | R0 on the free plan |
| Notifications | R0, since they run on the phone |
| Google Play Console (optional, later) | US$25 once-off (about R460) |

---

## 16. Project Status and Future Work

### 16.1 Current Status

| Area | Status |
|------|:------:|
| All volunteer and admin features | ✔ Complete |
| Three languages, light and dark mode, tablet support | ✔ Complete |
| Security Rules, 257 automated tests and CI | ✔ Complete |
| Hosted download page, deployed by the pipeline | ✔ Complete |
| User Acceptance Testing with PAB | ☐ Planned |

### 16.2 Future Improvements

- Instant push notifications through Firebase Cloud Messaging
- WhatsApp or SMS reminders
- Attendance tracking for activities

---

## 17. AI Usage Declaration

### 17.1 Tools Used

- **Claude** (Anthropic), through Claude Code
- **GitHub Copilot**

### 17.2 Where AI Was Used

| Area | How |
|------|-----|
| Code | Writing and reviewing parts of the app, including the volunteer and admin screens, error handling, and the join and leave logic |
| Testing | Security rules tests for activities and sign-ups, and full-app checks for bugs |
| Translations | First drafts of the isiXhosa and Afrikaans text, later checked by fluent speakers |

### 17.3 Example Prompts

- *"In the settings page add functionality for language change"*
- *"Create a standard for SA phone numbers, 0888888888 passed when it shouldn't have"*
- *"Add notifications on the phone: notify volunteers when new announcements and activities are added"*
- *"Rerun checks to ensure there are no errors and no missed fixes"*

### 17.4 How We Checked It

- **Purpose:** to speed up development, find and fix errors, and make sure the app met PAB's needs and the project requirements.
- **Checking:** the team set the requirements, reviewed every change, split changes into commits, ran the automated tests and CI, and tested the app on our own phones.

---

## 18. Attendance

All group members attended all group meetings, which took place every Monday.

| Meeting | Kwethukubonga Kunene | Letlhogonolo Kgatshe | Nuha Grimwood | Unathi Mudzengi | Ash Kruger |
|---------|:---:|:---:|:---:|:---:|:---:|
| 17 August 2026 | ✔ | ✔ | ✔ | ✔ | ✔ |
| 24 August 2026 | ✔ | ✔ | ✔ | ✔ | ✔ |
| 31 August 2026 | ✔ | ✔ | ✔ | ✔ | ✔ |
| 7 September 2026 | ✔ | ✔ | ✔ | ✔ | ✔ |
| 14 September 2026 | ✔ | ✔ | ✔ | ✔ | ✔ |
| 21 September 2026 | ✔ | ✔ | ✔ | ✔ | ✔ |
| 28 September 2026 | ✔ | ✔ | ✔ | ✔ | ✔ |

---

## 19. The Team

**Team KANTU:** Kwethukubonga Kunene · Letlhogonolo Kgatshe · Nuha Grimwood · Unathi Mudzengi · Ash Kruger

Thank you to Philisa Abafazi Bethu for welcoming us into their space and shaping this project around the real needs of their volunteers and community.

*Built for Philisa Abafazi Bethu – "Heal Our Women"*
