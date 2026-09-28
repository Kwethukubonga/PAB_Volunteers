/**
 * Runs the real firestore.rules file against the Firestore emulator.
 *
 * These are the checks that matter most, because a mistake here is not a bug on one
 * screen, it is anyone being able to read or change data that is not theirs. Nothing
 * touches the live database.
 */
const fs = require('fs');
const path = require('path');
const assert = require('assert');
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require('@firebase/rules-unit-testing');
const { doc, getDoc, setDoc, updateDoc, deleteDoc } = require('firebase/firestore');

const VOLUNTEER = 'volunteer_uid';
const OTHER = 'other_uid';
const ADMIN = 'admin_uid';

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: 'pab-volunteers-rules-test',
    firestore: {
      rules: fs.readFileSync(path.resolve(__dirname, '../firestore.rules'), 'utf8'),
      host: '127.0.0.1',
      port: 8080,
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

// Seeded with the rules switched off, so the starting data is not itself a test.
beforeEach(async () => {
  await testEnv.clearFirestore();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, 'users', VOLUNTEER), {
      uid: VOLUNTEER,
      firstName: 'Nomsa',
      email: 'nomsa@example.com',
      role: 'volunteer',
      volunteerId: 'VOL-2026-ABC123',
      joinedDate: 1,
    });
    await setDoc(doc(db, 'users', OTHER), {
      uid: OTHER,
      firstName: 'Thabo',
      email: 'thabo@example.com',
      role: 'volunteer',
    });
    await setDoc(doc(db, 'users', ADMIN), {
      uid: ADMIN,
      firstName: 'Admin',
      email: 'admin@example.com',
      role: 'admin',
    });
    await setDoc(doc(db, 'announcements', 'ann1'), {
      title: 'Afterschool tutors needed',
      messageBody: 'Two afternoons a month.',
      status: 'published',
      thumbsUpBy: [],
    });
  });
});

const asVolunteer = () => testEnv.authenticatedContext(VOLUNTEER).firestore();
const asOther = () => testEnv.authenticatedContext(OTHER).firestore();
const asAdmin = () => testEnv.authenticatedContext(ADMIN).firestore();
const asStranger = () => testEnv.unauthenticatedContext().firestore();

describe('users', () => {
  it('lets a volunteer read their own record', async () => {
    await assertSucceeds(getDoc(doc(asVolunteer(), 'users', VOLUNTEER)));
  });

  it('stops a volunteer reading someone else', async () => {
    await assertFails(getDoc(doc(asVolunteer(), 'users', OTHER)));
  });

  it('lets an admin read any volunteer, for the volunteers page', async () => {
    await assertSucceeds(getDoc(doc(asAdmin(), 'users', VOLUNTEER)));
  });

  it('stops a signed out person reading anything', async () => {
    await assertFails(getDoc(doc(asStranger(), 'users', VOLUNTEER)));
  });

  it('lets a volunteer edit their own profile', async () => {
    await assertSucceeds(
      updateDoc(doc(asVolunteer(), 'users', VOLUNTEER), { firstName: 'Nomsa B' })
    );
  });

  it('stops a volunteer making themselves an admin', async () => {
    await assertFails(updateDoc(doc(asVolunteer(), 'users', VOLUNTEER), { role: 'admin' }));
  });

  it('stops a volunteer changing their own email', async () => {
    await assertFails(
      updateDoc(doc(asVolunteer(), 'users', VOLUNTEER), { email: 'someone@else.com' })
    );
  });

  it('stops a volunteer changing their volunteer id or join date', async () => {
    await assertFails(
      updateDoc(doc(asVolunteer(), 'users', VOLUNTEER), { volunteerId: 'VOL-2026-ZZZZZZ' })
    );
    await assertFails(updateDoc(doc(asVolunteer(), 'users', VOLUNTEER), { joinedDate: 999 }));
  });

  it('stops a volunteer editing someone else', async () => {
    await assertFails(updateDoc(doc(asVolunteer(), 'users', OTHER), { firstName: 'Hacked' }));
  });

  it('lets someone delete their own account', async () => {
    await assertSucceeds(deleteDoc(doc(asVolunteer(), 'users', VOLUNTEER)));
  });

  it('stops an admin deleting a volunteer', async () => {
    await assertFails(deleteDoc(doc(asAdmin(), 'users', VOLUNTEER)));
  });

  it('stops a new account being created as an admin', async () => {
    const fresh = testEnv.authenticatedContext('fresh_uid').firestore();
    await assertFails(setDoc(doc(fresh, 'users', 'fresh_uid'), { role: 'admin' }));
    await assertSucceeds(setDoc(doc(fresh, 'users', 'fresh_uid'), { role: 'volunteer' }));
  });
});

describe('announcements', () => {
  it('lets any signed in volunteer read them', async () => {
    await assertSucceeds(getDoc(doc(asVolunteer(), 'announcements', 'ann1')));
  });

  it('stops a signed out person reading them', async () => {
    await assertFails(getDoc(doc(asStranger(), 'announcements', 'ann1')));
  });

  it('stops a volunteer writing an announcement', async () => {
    await assertFails(
      updateDoc(doc(asVolunteer(), 'announcements', 'ann1'), { title: 'Free money' })
    );
  });

  it('lets an admin edit an announcement', async () => {
    await assertSucceeds(
      updateDoc(doc(asAdmin(), 'announcements', 'ann1'), { title: 'Tutors still needed' })
    );
  });

  it('lets a volunteer add their own thumbs up', async () => {
    await assertSucceeds(
      updateDoc(doc(asVolunteer(), 'announcements', 'ann1'), { thumbsUpBy: [VOLUNTEER] })
    );
  });

  it('lets a volunteer take their own thumbs up away', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await updateDoc(doc(context.firestore(), 'announcements', 'ann1'), {
        thumbsUpBy: [VOLUNTEER],
      });
    });
    await assertSucceeds(
      updateDoc(doc(asVolunteer(), 'announcements', 'ann1'), { thumbsUpBy: [] })
    );
  });

  it('stops a volunteer thumbing up on someone else behalf', async () => {
    await assertFails(
      updateDoc(doc(asVolunteer(), 'announcements', 'ann1'), { thumbsUpBy: [OTHER] })
    );
  });

  it('stops a volunteer removing someone else thumbs up', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await updateDoc(doc(context.firestore(), 'announcements', 'ann1'), {
        thumbsUpBy: [VOLUNTEER, OTHER],
      });
    });
    await assertFails(
      updateDoc(doc(asVolunteer(), 'announcements', 'ann1'), { thumbsUpBy: [VOLUNTEER] })
    );
  });

  it('stops a volunteer stuffing the count', async () => {
    await assertFails(
      updateDoc(doc(asVolunteer(), 'announcements', 'ann1'), {
        thumbsUpBy: [VOLUNTEER, 'ghost1', 'ghost2'],
      })
    );
  });

  it('stops a volunteer clearing everyone thumbs up', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await updateDoc(doc(context.firestore(), 'announcements', 'ann1'), {
        thumbsUpBy: [OTHER, 'someone_else'],
      });
    });
    await assertFails(
      updateDoc(doc(asVolunteer(), 'announcements', 'ann1'), { thumbsUpBy: [] })
    );
  });

  it('stops a volunteer sneaking an edit in alongside a thumbs up', async () => {
    await assertFails(
      updateDoc(doc(asVolunteer(), 'announcements', 'ann1'), {
        thumbsUpBy: [VOLUNTEER],
        title: 'Changed while nobody looked',
      })
    );
  });

  it('stops a volunteer deleting an announcement', async () => {
    await assertFails(deleteDoc(doc(asVolunteer(), 'announcements', 'ann1')));
  });
});

describe('collections that are closed', () => {
  it('refuses anything not named in the rules', async () => {
    await assertFails(getDoc(doc(asAdmin(), 'impactStats', 'main')));
    await assertFails(setDoc(doc(asAdmin(), 'secrets', 'x'), { a: 1 }));
  });
});
