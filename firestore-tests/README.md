# Security rules tests

These run the real `firestore.rules` file against the Firestore emulator, so we can prove
the rules refuse what they should without touching the live database.

## Running them

You need Node 20 and a JDK on your PATH. The emulator is a Java program.

```bash
cd firestore-tests
npm install
npm test
```

The emulator starts, the tests run, and it shuts down again. The `PERMISSION_DENIED` lines
in the output are the point: those are the writes the rules correctly refused.

## What is covered

**Users**

- A volunteer reads only their own record, an admin reads everyone.
- A volunteer cannot make themselves an admin.
- A volunteer cannot change their own email, volunteer ID or join date.
- A volunteer cannot edit anyone else.
- Someone can delete their own account, and an admin cannot delete them.
- A new account cannot be created with the admin role.

**Announcements**

- Only an admin can write one.
- A volunteer can add and remove their own thumbs up.
- A volunteer cannot thumbs up as someone else, remove someone else's, clear the list,
  pad the count with made up ids, or slip an edit in alongside a thumbs up.

**Everything else**

- Collections not named in the rules are closed, including the old `impactStats`.

## Why this lives outside the Android project

The rules are enforced by Firestore, not by the app, so testing them through the app would
only prove the app asks nicely. These tests talk to the emulator directly and behave like
someone who is not using our app at all, which is the case that actually matters.
