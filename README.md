<div align="center">
PHILISA VOLUNTEERS
Android Application – Project README
Team KANTU
Developed for Philisa Abafazi Bethu
</div>
---
Table of Contents
Introduction
About Philisa Abafazi Bethu
Background and Requirements Gathering
Functional Requirements
Non-Functional Requirements
User Roles and Permissions
Features
Technology Stack
System Architecture
Design Patterns
Data Model
Security and Privacy
Getting Started
Project Structure
DevOps and Development Workflow
Running Costs
Project Status and Future Work
The Team
---
1. Introduction
Philisa Volunteers is an Android application that brings the volunteer experience at Philisa Abafazi Bethu (PAB) into one place. Anyone who wants to get involved can create an account, fill in a short two-step profile, and start volunteering straight away. There is no waiting for approval. From there, volunteers can browse opportunities, join or leave activities, keep track of their schedule and hours, and stay up to date with announcements from the organisation.
Authorised PAB staff use the same app, but are taken to a separate admin area where they can manage volunteers, publish activities and post announcements.
The app is available in English, isiXhosa and Afrikaans, supports light and dark mode, and keeps working when the phone loses signal.
This README covers the background to the project, its requirements, how the app is built, and how to get it running on your own machine.
---
2. About Philisa Abafazi Bethu
Philisa Abafazi Bethu, which means "Heal Our Women" in isiXhosa, is a non-profit organisation founded in 2008. It began in Lavender Hill and is now based in Steenberg, Cape Town. Over the years, PAB has grown from a community initiative into an organisation that supports women, children and families in many different ways.
The app currently lists ten of PAB's programmes that volunteers can support:
		
Afterschool Programmes	Youth Programme	Women Empowerment
Baby Saver	Emergency Safe Houses	Senior Programme
Community Feeding	Men's Café	Search and Rescue Team
Social Work Services		
Volunteers are a big part of what makes this work possible, and this app was built with them in mind.
---
3. Background and Requirements Gathering
3.1 How the Requirements Were Gathered
The requirements for Philisa Volunteers came from working directly with PAB over several meetings and site visits, rather than from what we assumed would be useful.
Date	Engagement	Outcome
19 May 2026	Site visit to the PAB Women Centre, followed by an online introductory meeting with the director	The team saw PAB's work first-hand, including the safe houses, Baby Saver, children's programmes and the community feeding garden.
27 July 2026	In-person meeting with the director	Discussed where technology could improve existing processes. PAB shared its own wish list of digital solutions.
4 August 2026	Presentation of three project ideas	PAB selected the idea that included the Philisa Volunteers Android application.
We also looked at existing volunteer management platforms, such as Volunteero, Thina and Bloomerang Volunteer, to see how they handle sign-ups, opportunities, activity management and notifications. These were used as a reference only. The final requirements are based on PAB's specific needs.
3.2 The Problem
#	Current Situation	Impact
1	Volunteer applications are completed on PDF forms and sent by email.	Signing up is inconvenient, which can discourage people from getting involved.
2	There is no central place for upcoming programmes, events and opportunities.	Regular volunteers find it hard to stay informed and plan their involvement.
3	Staff have no single tool to keep track of volunteers, share opportunities and communicate with them.	Managing volunteers takes more time and effort than it should.
Philisa Volunteers solves these problems by giving volunteers and staff one app that supports the whole volunteer journey.
---
4. Functional Requirements
ID	Requirement	Role	Status
PV-FR01	Anyone must be able to create an account with email and password, or with their Google account.	Visitor	✔
PV-FR02	New users must complete a two-step profile (personal details, then programme interests), after which they become a volunteer immediately.	Visitor	✔
PV-FR03	Users must be able to sign in securely and reset a forgotten password by email.	All	✔
PV-FR04	Volunteers must be able to browse published opportunities and open one to see its full details.	Volunteer	✔
PV-FR05	Volunteers must be able to join an activity, as long as spots are still available.	Volunteer	✔
PV-FR06	Volunteers must be able to leave an activity they joined, which gives the spot back.	Volunteer	✔
PV-FR07	Volunteers must be able to see their activities as Upcoming and Completed, and view them on a schedule.	Volunteer	✔
PV-FR08	Volunteers must be able to save opportunities as favourites.	Volunteer	✔
PV-FR09	Volunteers must be able to read announcements from PAB and give them a thumbs up.	Volunteer	✔
PV-FR10	Volunteers must receive notifications about new activities and announcements.	Volunteer	✔
PV-FR11	Volunteers must be able to view and edit their profile, and delete their account.	Volunteer	✔
PV-FR12	Administrators must be taken to a separate admin area when they sign in.	Administrator	✔
PV-FR13	Administrators must be able to view all volunteers and their details.	Administrator	✔
PV-FR14	Administrators must be able to create, edit, publish, unpublish and delete activities.	Administrator	✔
PV-FR15	Administrators must be able to see who has signed up for each activity.	Administrator	✔
PV-FR16	Administrators must be able to create, publish and delete announcements, with an optional picture.	Administrator	✔
PV-FR17	Administrators must be notified when an activity becomes fully booked.	Administrator	✔
---
5. Non-Functional Requirements
Category	Requirement	How the App Meets It
Accessibility	The app should be usable by people with different levels of digital experience and language preferences.	Available in English, isiXhosa and Afrikaans. Clear labels, simple bottom navigation and readable layouts. Light and dark themes.
Usability	Users should be able to use the app without training.	A short two-step sign-up, friendly empty-state messages (for example, "You haven't joined an activity yet") and confirmation before actions such as leaving an activity or deleting an account.
Reliability	The app should keep working when the connection drops, without losing information.	Firestore's persistent offline cache lets volunteers see their data without signal. Joining an activity uses a database transaction, so two people can never take the last spot.
Performance	Screens and key actions should respond quickly.	Images are loaded and cached with Glide, and lists use RecyclerView. Pull-to-refresh is available on list screens.
Availability	The system should be available when volunteers need it.	Firebase is a managed Google Cloud service, with a target of 99.9% uptime.
Scalability	The app should grow with PAB without needing to be rebuilt.	A layered, modular code structure and a database that scales automatically.
Security	Personal information must be protected and access restricted by role.	Firebase Security Rules on every collection, tested automatically against the Firestore emulator.
---
6. User Roles and Permissions
6.1 User Roles
There is no approval step. As soon as a new user completes their profile, they become a volunteer.
Role	Description	How the Role Is Given
Visitor	Someone who has opened the app but not yet created an account.	–
Volunteer	Anyone who has created an account and completed their profile.	Automatically, on sign-up. Every new account must start as a volunteer.
Administrator	An authorised PAB staff member.	Set by hand in the Firebase console by changing the user's `role` to `admin`. It cannot be changed from inside the app.
6.2 Permission Matrix
These permissions are enforced by the Firebase Security Rules, not just by the app's screens.
Action	Visitor	Volunteer	Administrator
View the welcome and About PAB screens	✔	✔	✔
Create an account	✔	–	–
Read and edit own profile	–	✔	✔
Change own role, volunteer ID, email or join date	–	✘	✔ (via console)
Read other volunteers' profiles	–	✘	✔
Browse activities and announcements	–	✔	✔
Join or leave an activity	–	✔	–
Give an announcement a thumbs up	–	✔	✔
See who signed up for an activity	–	Own sign-ups only	✔
Create, edit, publish or delete activities	–	✘	✔
Create, publish or delete announcements	–	✘	✔
Delete own account	–	✔	✔
Delete another user's account	–	✘	✘
---
7. Features
7.1 Getting Started (Visitors)
Screen	What It Does
Splash	Checks whether the user is already signed in and sends them to the right place.
Welcome	Introduces PAB, with options to Continue with Google or Sign in with email.
About PAB	Explains PAB's work and programmes, with volunteer stories and contact details.
Email sign-in / sign-up	Create an account or sign in, with a Forgot password option. Passwords must be at least 8 characters and include a symbol.
Profile setup (step 1 of 2)	First name, last name, South African cellphone number and area or township.
Profile setup (step 2 of 2)	Choose one or more programmes the volunteer is interested in.
7.2 Volunteers
Volunteers use a bottom navigation bar with five tabs:
Tab	What It Does
Home	A greeting based on the time of day, today's activities, stats (today, completed and hours), and what's coming up.
Activities	Browse all published opportunities or just favourites. Open an activity to see its details and spots remaining, then Join or Leave. My Activities are split into Upcoming and Completed.
Schedule	A summary of upcoming and completed activities in date order.
Community	Announcements from PAB, with pictures, which volunteers can open and give a thumbs up.
Profile	The volunteer's details, volunteer ID, stats, and options to edit their profile or sign out.
7.3 Administrators
Administrators have their own bottom navigation bar:
Tab	What It Does
Overview	Total volunteers, total sign-ups, published activities, recent volunteers, and quick actions to create an activity or post an announcement.
Volunteers	A list of all registered volunteers, with a details screen for each one.
Activities	Create, edit, publish, unpublish and delete activities. See how many spots are filled and who has signed up.
Posts	Create, edit, publish and delete announcements, with an optional picture and a thumbs-up count.
7.4 Settings
Available to all signed-in users:
Setting	Options
Theme	Follow the phone, Light or Dark
Language	English, isiXhosa or Afrikaans. Notifications and password-reset emails also use the chosen language.
Notifications	New activities, announcements, and (for admins) activities that are full
Account	Delete my account. This removes the profile and gives back the volunteer's places on activities.
7.5 Notifications
A background worker checks Firestore for anything new roughly every 15 minutes while the phone is connected, and shows a notification in the right channel.
Channel	Who Receives It	When
New activities	Volunteers	A new activity is published
Announcements	Volunteers	PAB posts a new announcement
Full activities	Administrators	Every spot on an activity has been taken
---
8. Technology Stack
8.1 Technologies
Area	Technology
Platform	Native Android (minimum SDK 26 / Android 8.0; target and compile SDK 34)
Language	Kotlin 2.0.20 (JVM target 17)
Build system	Gradle (Kotlin DSL), Android Gradle Plugin 8.5.2, version catalog
User interface	XML layouts, Material Components, ConstraintLayout, RecyclerView, SwipeRefreshLayout, View Binding
Navigation	Jetpack Navigation Component (separate graphs for auth, volunteer and admin)
Architecture components	ViewModel, LiveData, Kotlin Coroutines
Authentication	Firebase Authentication (email/password and Google Sign-In)
Database	Cloud Firestore with persistent offline cache
Image hosting	Cloudinary (announcement pictures)
Image loading	Glide
Background work	WorkManager
Testing	JUnit unit tests; Firestore Security Rules tests with the Firebase emulator (Node.js)
CI	GitHub Actions
8.2 Why We Chose Firebase
Offline support. Many volunteers work in areas with unreliable signal, such as Lavender Hill, Steenberg and Khayelitsha. Firestore's persistent cache means the app still shows data offline.
Built-in security. Firebase Security Rules let us enforce who can read and change each record directly in the database, not just in the app.
Simple sign-in. Firebase Authentication handles email/password accounts, Google Sign-In and password-reset emails.
Low running costs. Firebase's free tier (50,000 reads, 20,000 writes and 20,000 deletes per day) comfortably covers PAB's expected usage.
8.3 Alternatives Considered
Option	Strengths	Why We Didn't Choose It
Azure SQL / PostgreSQL	Strong data integrity and structured schemas.	Fixed monthly hosting costs of around R280 to R450, no built-in offline syncing for mobile, and more database administration.
MongoDB Atlas	Flexible document model and powerful queries.	Higher entry-tier costs and more complex mobile syncing than Firebase's Android SDK.
---
9. System Architecture
9.1 Layered Architecture
The app is organised into layers, so each part has one job and changes in one place don't ripple through the whole app.
```
┌──────────────────────────────────────────────────────────────┐
│  UI LAYER                                          ui/        │
│  Activities & Fragments (auth, volunteer, admin, profile,    │
│  settings) · RecyclerView Adapters · View Binding            │
├──────────────────────────────────────────────────────────────┤
│  VIEWMODEL LAYER                                             │
│  AuthViewModel · ProfileSetupViewModel · VolunteerViewModel  │
│  · AdminViewModel                                            │
├──────────────────────────────────────────────────────────────┤
│  DATA LAYER                                        data/      │
│  Repositories: Auth · Account · User · Activity ·            │
│  Signup · Announcement                                       │
│  Models: User · Activity · ActivitySignup · Announcement     │
├──────────────────────────────────────────────────────────────┤
│  SERVICES                                                    │
│  FirebaseAuthManager · FirestoreManager · ImageUploader      │
│  (Cloudinary) · UpdatesWorker (WorkManager)                  │
└──────────────────────────────────────────────────────────────┘
```
Layer	Package	Responsibility
UI	`ui/`	Screens, lists and user input. Split into `auth`, `volunteer`, `admin`, `profile` and `settings`.
ViewModel	`ui/…`	Holds screen state and calls the repositories, so data survives screen rotation.
Data	`data/repository`, `data/model`	Repositories are the only classes that talk to Firebase. Models map directly to Firestore documents.
Services	`data/firebase`, `data/remote`, `notifications/`	Firebase set-up, picture uploads and background notifications.
Utilities	`utils/`	Language, theme, dates, phone number validation, error messages and network checks.
9.2 Navigation
Graph	Screens
`AuthNavGraph`	Splash, Welcome, About PAB, Email sign-in/sign-up, Profile setup
`VolunteerNavGraph`	Home, Activities, Schedule, Community, Profile
`AdminNavGraph`	Overview, Volunteers, Activities, Posts
`AppNavGraph`	Decides which graph to show, based on whether the user is signed in, has finished their profile, and is an admin
9.3 How Data Flows Through the App
```
        ┌──────────────────────────────┐
        │   Philisa Volunteers (App)   │
        │   UI ─► ViewModel ─► Repo    │
        └───┬──────────┬───────────┬───┘
            │          │           │
   sign-in  │  reads & │  picture  │
            ▼  writes  ▼  uploads  ▼
  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
  │   Firebase   │ │    Cloud     │ │  Cloudinary  │
  │     Auth     │ │  Firestore   │ │              │
  └──────────────┘ └──────▲───────┘ └──────────────┘
                          │ checks for new items
                          │ (about every 15 min)
                 ┌────────┴────────┐
                 │  UpdatesWorker  │──► Local notifications
                 │  (WorkManager)  │
                 └─────────────────┘
```
---
10. Design Patterns
Pattern	Where It Is Used	Why
Repository	`AuthRepository`, `UserRepository`, `ActivityRepository`, `SignupRepository`, `AnnouncementRepository`, `AccountRepository`	Keeps all Firebase code in one place. Screens never call Firestore directly, so the database could be changed without rewriting the UI.
MVVM (Model–View–ViewModel)	`VolunteerViewModel`, `AdminViewModel`, `AuthViewModel`, `ProfileSetupViewModel`	Separates what the screen shows from how the data is loaded, and keeps state when the phone is rotated.
Observer	LiveData in the ViewModels	Screens update automatically when the data they are watching changes.
Singleton	`FirestoreManager`, `FirebaseAuthManager`, `AppLanguage`, `ThemePreference`	One shared instance of the database connection and app settings.
Adapter	`ActivityAdapter`, `AnnouncementAdapter`, `VolunteerAdapter`, `SignupAdapter` and others	Converts lists of data into rows for RecyclerView.
---
11. Data Model
11.1 Firestore Collections
Collection	What It Stores	Key Fields
`users`	Each person's profile	`firstName`, `lastName`, `email`, `phone`, `area`, `programmeInterests`, `favouriteActivityIds`, `role`, `volunteerId`, `profileComplete`, `joinedDate`
`activities`	Volunteer opportunities created by admins	`title`, `programme`, `date`, `dateMillis`, `startTime`, `endTime`, `location`, `volunteerRole`, `totalSpots`, `filledSpots`, `description`, `status`, `createdBy`, `createdDate`, `publishedAt`
`activitySignups`	A volunteer's place on an activity	`activityId`, `userId`, `volunteerName`, `activityTitle`, `programme`, `date`, `dateMillis`, `startTime`, `endTime`, `location`, `signedUpDate`
`announcements`	Posts from PAB to volunteers	`title`, `messageBody`, `imageUrl`, `date`, `status`, `createdBy`, `publishedAt`, `thumbsUpBy`
11.2 Design Decisions
Decision	Reason
Sign-up IDs are `activityId_userId`.	One fixed ID per person per activity, so the same spot can never be taken twice.
Joining uses a Firestore transaction.	The activity is re-read from the server and the spot is taken in one step, so two people can't take the last spot at the same time.
Sign-ups copy the activity's title, date and location.	Volunteers keep their history and hours even if an admin later unpublishes or deletes the activity.
Leaving an activity deletes the sign-up.	There is no approval status, so there is nothing to keep. The spot goes straight back to the group.
Values such as `fullName`, `isAdmin` and `spotsRemaining` are calculated in the app.	They are never saved to Firestore, so they can't get out of sync.
11.3 Status Values
Field	Possible Values
`role`	`volunteer`, `admin`
Activity `status`	`draft`, `published`
Announcement `status`	`draft`, `published`
---
12. Security and Privacy
12.1 Firebase Security Rules
All access is controlled by `firestore.rules`:
Collection	Rules
`users`	Users can read their own record, and admins can read everyone. A new record must start with the role `volunteer`. Users can edit their own profile, but not their `role`, `volunteerId`, `email` or `joinedDate`. Only the user can delete their own account.
`activities`	Any signed-in user can read. Only admins can create, edit or delete. Volunteers may only move the spot counter up or down by one, only alongside their own sign-up, and never below 0 or above the total.
`announcements`	Any signed-in user can read. Only admins can create, edit or delete. Volunteers may only add or remove their own thumbs up.
`activitySignups`	Volunteers see their own places. Admins see who signed up for what.
12.2 Other Measures
Area	Measure
Rules testing	The rules are tested against the Firebase emulator on every push, to prove they refuse what they should.
Passwords	At least 8 characters, including a symbol.
Account deletion	Users can delete their own account and data at any time, in line with POPIA. Firebase requires a recent sign-in first.
Secrets	Cloudinary keys live in `local.properties`, and release signing details live in `keystore.properties`. Both are excluded from Git.
Offline data	Firestore's cache only holds data the signed-in user is allowed to read.
---
13. Getting Started
13.1 What You Will Need
Android Studio (latest stable version)
JDK 17
An emulator or Android device running Android 8.0 or later
Git
Node.js 20 (only needed to run the security rules tests or the seed script)
13.2 Setting Up the Project
Clone the repository and switch to the `develop` branch, which holds the latest working code:
```bash
   git clone https://github.com/Kwethukubonga/PAB_Volunteers.git
   cd PAB_Volunteers
   git checkout develop
   ```
Open the `PAB_Volunteers` folder in Android Studio.
Copy `local.properties.example` to `local.properties` and add the Cloudinary details (ask a team member):
```properties
   cloudinary.cloud.name=YOUR_CLOUD_NAME
   cloudinary.upload.preset=YOUR_UPLOAD_PRESET
   ```
The app still runs without these, but picture uploads on announcements won't work.
Wait for Gradle to sync, then choose a device and click Run.
The Firebase configuration (`google-services.json`) and a shared debug keystore are already included, so Google Sign-In works on every team member's debug build.
13.3 Creating an Admin Account
Sign up in the app as normal.
In the Firebase console, go to Firestore > users, find your record, and change `role` from `volunteer` to `admin`.
Sign out and sign back in to open the admin area.
13.4 Adding Sample Data
The seed script adds one sample activity and one announcement for each PAB programme. You'll need a Firebase service account key, which must never be committed.
```bash
cd tools/seed
npm install
node seed.js --dry-run                 # shows what would be added
node seed.js --key <path to key file>  # adds the samples
node seed.js --key <path to key file> --remove   # removes them again
```
13.5 Useful Commands
Task	Windows	macOS / Linux
Build a debug APK	`gradlew.bat assembleDebug`	`./gradlew assembleDebug`
Run unit tests	`gradlew.bat testDebugUnitTest`	`./gradlew testDebugUnitTest`
Run lint	`gradlew.bat lintDebug`	`./gradlew lintDebug`
Run security rules tests	`cd firestore-tests && npm install && npm test`	same
---
14. Project Structure
```
PAB_Volunteers/
├── .github/workflows/android.yml       # CI pipeline
├── app/
│   ├── build.gradle.kts                # App settings and dependencies
│   ├── google-services.json            # Firebase configuration
│   └── src/
│       ├── main/java/com/kantu/pab_volunteers/
│       │   ├── data/
│       │   │   ├── firebase/           # FirebaseAuthManager, FirestoreManager
│       │   │   ├── model/              # User, Activity, ActivitySignup, Announcement
│       │   │   ├── remote/             # ImageUploader (Cloudinary)
│       │   │   └── repository/         # Auth, Account, User, Activity, Signup, Announcement
│       │   ├── navigation/             # App, Auth, Volunteer and Admin nav graphs
│       │   ├── notifications/          # UpdatesWorker, channels, settings, permission prompt
│       │   ├── ui/
│       │   │   ├── auth/               # Splash, Welcome, About PAB, Email sign-in, Google sign-in
│       │   │   ├── profile/            # Two-step profile setup and programme list
│       │   │   ├── volunteer/          # Home, Activities, Schedule, Community, Profile
│       │   │   ├── admin/              # Overview, Volunteers, Activities, Announcements
│       │   │   └── settings/           # Theme, language, notifications, delete account
│       │   ├── utils/                  # Language, theme, dates, phone numbers, errors
│       │   └── PabApplication.kt
│       ├── main/res/                   # Layouts, drawables, strings (en, xh, af), themes
│       └── test/                       # Unit tests
├── firestore-tests/                    # Security rules tests (Firebase emulator)
├── tools/seed/                         # Sample data script
├── keystore/debug.keystore             # Shared debug key (release keys are never committed)
├── firestore.rules                     # Firestore security rules
├── local.properties.example            # Template for Cloudinary settings
├── keystore.properties.example         # Template for release signing
└── settings.gradle.kts
```
---
15. DevOps and Development Workflow
15.1 Branching Strategy
Branch	Purpose
`master`	The stable, release-ready version of the app.
`develop`	Where finished features are brought together and tested.
`feature/…`	Individual features, for example `feature/authentication`, `feature/database`, `feature/home` and `feature/tests`.
```
 feature/* ─► Pull Request ─► CI checks ─► Peer review ─► develop ─► master
```
15.2 Continuous Integration
GitHub Actions runs on every push and Pull Request to `master` and `develop`. A new push cancels any run still going for the same branch.
Job	What It Does
Unit tests	Runs `testDebugUnitTest` and saves the test report.
Lint	Runs `lintDebug`. Lint errors fail the build, including missing translations, so an untranslated string is caught before release.
Rules	Runs the Firestore security rules tests against the Firebase emulator.
Build APK	Only runs once the three checks above pass. Builds the debug APK and publishes it as a download for 30 days.
Cloudinary details are supplied to CI through GitHub Secrets, so they never appear in the code.
15.3 Testing
Type	What Is Tested
Unit tests	Data models, utilities and phone number validation.
Security rules tests	That users can only read and change what they should, for example that a volunteer can't make themselves an admin.
Device testing	The app is tested on the Android emulator and physical devices.
User Acceptance Testing	PAB representatives will test the app before release.
---
16. Running Costs
Item	Service	Estimated Cost
Android distribution	Google Play Console	US$25 once-off (approximately R460)
Authentication	Firebase Authentication	R0 within free limits
Database	Cloud Firestore	R0 within free limits
Picture hosting	Cloudinary	R0 on the free plan
Notifications	WorkManager (on the device)	R0
---
17. Project Status and Future Work
17.1 Current Status
Area	Status
Sign-up, sign-in, Google Sign-In and password reset	✔ Complete
Two-step profile setup with programme interests	✔ Complete
Volunteer Home, Activities, Schedule, Community and Profile	✔ Complete
Joining and leaving activities, favourites	✔ Complete
Admin Overview, Volunteers, Activities and Posts	✔ Complete
Notifications	✔ Complete
English, isiXhosa and Afrikaans	✔ Complete
Light and dark mode	✔ Complete
Firestore Security Rules and rules tests	✔ Complete
GitHub Actions pipeline	✔ Complete
User Acceptance Testing with PAB	☐ Planned
Release build and Google Play distribution	☐ Planned
17.2 Possible Future Improvements
Instant push notifications through Firebase Cloud Messaging, instead of checking every 15 minutes.
WhatsApp or SMS reminders for volunteers.
Attendance tracking, so admins can confirm who attended an activity.
---
18. The Team
Philisa Volunteers is being developed by Team KANTU:
Team Member
Kwethukubonga Kunene
Letlhogonolo Kgatshe
Nuha Grimwood
Unathi Mudzengi
Ash Kruger
We would like to thank Philisa Abafazi Bethu for welcoming us into their space, sharing their time and helping shape this project around the real needs of their volunteers and community.
---
<div align="center"><i>Built for Philisa Abafazi Bethu – "Heal Our Women"</i></div>
