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
const {
  doc, getDoc, setDoc, updateDoc, deleteDoc, writeBatch, arrayUnion, arrayRemove,
  collection, query, where, getDocs,
} = require('firebase/firestore');

const VOLUNTEER = 'volunteer_uid';
const OTHER = 'other_uid';
const ADMIN = 'admin_uid';

let testEnv;

// A plain function, not an arrow, so it can have its own time limit. Loading the rules
// into a freshly started emulator sometimes takes longer than a single test is allowed.
before(async function () {
  this.timeout(60000);
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

  // Favourites are a list on the volunteer's own record, saved the way the app saves them.
  it('lets a volunteer save and remove a favourite', async () => {
    const me = doc(asVolunteer(), 'users', VOLUNTEER);
    await assertSucceeds(updateDoc(me, { favouriteActivityIds: arrayUnion('act_1') }));
    await assertSucceeds(updateDoc(me, { favouriteActivityIds: arrayRemove('act_1') }));
  });

  it('stops a volunteer changing someone else favourites', async () => {
    await assertFails(
      updateDoc(doc(asVolunteer(), 'users', OTHER), { favouriteActivityIds: arrayUnion('act_1') })
    );
  });

  // The emergency contact "I need help" texts, saved as two fields on the volunteer's own record.
  it('lets a volunteer save their own emergency contact, and nobody else', async () => {
    const contact = { emergencyContactName: 'Thandi', emergencyContactPhone: '082 123 4567' };
    await assertSucceeds(updateDoc(doc(asVolunteer(), 'users', VOLUNTEER), contact));
    await assertFails(updateDoc(doc(asVolunteer(), 'users', OTHER), contact));
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

// Joining and leaving are saved as one batch in the app: the sign-up and the spot counter
// change together. These prove the rules only accept that exact pair, so nobody can
// overbook an activity, sign someone else up, or move the counter on its own.
describe('activities and sign-ups', () => {
  const OPEN = 'act_open';
  const FULL = 'act_full';
  const DRAFT = 'act_draft';
  const signupId = (activityId, uid) => `${activityId}_${uid}`;

  beforeEach(async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      const db = context.firestore();
      await setDoc(doc(db, 'activities', OPEN), {
        title: 'Community Soup Kitchen', status: 'published', totalSpots: 3, filledSpots: 1,
      });
      await setDoc(doc(db, 'activities', FULL), {
        title: 'Baby Pack Assembly', status: 'published', totalSpots: 1, filledSpots: 1,
      });
      await setDoc(doc(db, 'activities', DRAFT), {
        title: 'Not ready yet', status: 'draft', totalSpots: 5, filledSpots: 0,
      });
      // Someone else already has a place on the open activity.
      await setDoc(doc(db, 'activitySignups', signupId(OPEN, OTHER)), {
        activityId: OPEN, userId: OTHER,
      });
    });
  });

  /** What the app does when a volunteer taps Join. */
  function join(db, activityId, uid, newCount) {
    const batch = writeBatch(db);
    batch.set(doc(db, 'activitySignups', signupId(activityId, uid)), { activityId, userId: uid });
    batch.update(doc(db, 'activities', activityId), { filledSpots: newCount });
    return batch.commit();
  }

  it('lets a volunteer join an open activity', async () => {
    await assertSucceeds(join(asVolunteer(), OPEN, VOLUNTEER, 2));
  });

  it('stops anyone joining a full activity', async () => {
    await assertFails(join(asVolunteer(), FULL, VOLUNTEER, 2));
  });

  it('stops anyone joining a draft activity', async () => {
    await assertFails(join(asVolunteer(), DRAFT, VOLUNTEER, 1));
  });

  it('lets a volunteer open a published activity but not a draft', async () => {
    await assertSucceeds(getDoc(doc(asVolunteer(), 'activities', OPEN)));
    await assertFails(getDoc(doc(asVolunteer(), 'activities', DRAFT)));
  });

  it('lets a volunteer still open an activity they joined after it was hidden', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), 'activitySignups', signupId(DRAFT, VOLUNTEER)), {
        activityId: DRAFT, userId: VOLUNTEER,
      });
    });
    await assertSucceeds(getDoc(doc(asVolunteer(), 'activities', DRAFT)));
  });

  it('lets a volunteer list published activities, but not every activity', async () => {
    const activities = collection(asVolunteer(), 'activities');
    await assertSucceeds(getDocs(query(activities, where('status', '==', 'published'))));
    await assertFails(getDocs(activities));
  });

  it('lets an admin list every activity, drafts included', async () => {
    const all = await assertSucceeds(getDocs(collection(asAdmin(), 'activities')));
    assert.strictEqual(all.size, 3);
  });

  it('stops a volunteer joining the same activity twice', async () => {
    await assertSucceeds(join(asVolunteer(), OPEN, VOLUNTEER, 2));
    await assertFails(join(asVolunteer(), OPEN, VOLUNTEER, 3));
  });

  it('stops a volunteer signing someone else up', async () => {
    const db = asVolunteer();
    const batch = writeBatch(db);
    batch.set(doc(db, 'activitySignups', signupId(OPEN, 'third_uid')), {
      activityId: OPEN, userId: 'third_uid',
    });
    batch.update(doc(db, 'activities', OPEN), { filledSpots: 2 });
    await assertFails(batch.commit());
  });

  it('stops the counter going up without a sign-up', async () => {
    await assertFails(updateDoc(doc(asVolunteer(), 'activities', OPEN), { filledSpots: 2 }));
  });

  it('stops a sign-up being saved without the counter going up', async () => {
    await assertFails(setDoc(doc(asVolunteer(), 'activitySignups', signupId(OPEN, VOLUNTEER)), {
      activityId: OPEN, userId: VOLUNTEER,
    }));
  });

  it('stops the counter jumping by more than one', async () => {
    await assertFails(join(asVolunteer(), OPEN, VOLUNTEER, 3));
  });

  it('stops a volunteer changing anything else on an activity', async () => {
    await assertFails(updateDoc(doc(asVolunteer(), 'activities', OPEN), { title: 'Mine now' }));
  });

  it('lets a volunteer leave and give their spot back', async () => {
    const db = asOther();
    const batch = writeBatch(db);
    batch.delete(doc(db, 'activitySignups', signupId(OPEN, OTHER)));
    batch.update(doc(db, 'activities', OPEN), { filledSpots: 0 });
    await assertSucceeds(batch.commit());
  });

  it('stops a volunteer removing someone else from an activity', async () => {
    await assertFails(deleteDoc(doc(asVolunteer(), 'activitySignups', signupId(OPEN, OTHER))));
  });

  it('lets a volunteer check for their own place before joining', async () => {
    await assertSucceeds(getDoc(doc(asVolunteer(), 'activitySignups', signupId(OPEN, VOLUNTEER))));
  });

  it('stops a volunteer seeing someone else sign-up', async () => {
    await assertFails(getDoc(doc(asVolunteer(), 'activitySignups', signupId(OPEN, OTHER))));
  });

  // The same query the admin's sign-ups screen runs.
  const signupsFor = (db, activityId) =>
    getDocs(query(collection(db, 'activitySignups'), where('activityId', '==', activityId)));

  it('lets an admin see everyone signed up for an activity', async () => {
    const result = await assertSucceeds(signupsFor(asAdmin(), OPEN));
    assert.strictEqual(result.size, 1);
  });

  it('stops a volunteer listing everyone signed up for an activity', async () => {
    await assertFails(signupsFor(asVolunteer(), OPEN));
  });

  it('stops a volunteer creating or deleting an activity', async () => {
    await assertFails(setDoc(doc(asVolunteer(), 'activities', 'new'), {
      title: 'Fake', status: 'published', totalSpots: 99, filledSpots: 0,
    }));
    await assertFails(deleteDoc(doc(asVolunteer(), 'activities', OPEN)));
  });

  it('lets an admin delete an activity together with its sign-ups', async () => {
    const db = asAdmin();
    const batch = writeBatch(db);
    batch.delete(doc(db, 'activitySignups', signupId(OPEN, OTHER)));
    batch.delete(doc(db, 'activities', OPEN));
    await assertSucceeds(batch.commit());
  });
});

// Background checks: a volunteer can only ask, and only an admin can clear someone.
describe('clearances', () => {
  const request = { volunteerName: 'Nomsa', status: 'requested', requestedAt: 1 };

  it('lets a volunteer ask to be checked', async () => {
    await assertSucceeds(setDoc(doc(asVolunteer(), 'clearances', VOLUNTEER), request));
  });

  it('stops a volunteer clearing themselves', async () => {
    await assertFails(setDoc(doc(asVolunteer(), 'clearances', VOLUNTEER), {
      volunteerName: 'Nomsa', status: 'cleared', expiresAt: 9999999999999,
    }));
  });

  it('stops a volunteer sneaking an expiry date into a request', async () => {
    await assertFails(setDoc(doc(asVolunteer(), 'clearances', VOLUNTEER), { ...request, expiresAt: 9999999999999 }));
  });

  it('stops a volunteer asking on someone else behalf', async () => {
    await assertFails(setDoc(doc(asVolunteer(), 'clearances', OTHER), request));
  });

  it('lets an admin mark a volunteer as cleared', async () => {
    await assertSucceeds(setDoc(doc(asAdmin(), 'clearances', VOLUNTEER), {
      volunteerName: 'Nomsa', status: 'cleared', clearedAt: 1, expiresAt: 2, checkedBy: 'Admin',
    }));
  });

  it('lets a volunteer see their own record but not anyone else', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), 'clearances', OTHER), request);
    });
    await assertSucceeds(getDoc(doc(asVolunteer(), 'clearances', VOLUNTEER)));
    await assertFails(getDoc(doc(asVolunteer(), 'clearances', OTHER)));
    await assertSucceeds(getDoc(doc(asAdmin(), 'clearances', OTHER)));
  });

  it('lets a volunteer take their own record away, for example when deleting their account', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), 'clearances', VOLUNTEER), request);
    });
    await assertSucceeds(deleteDoc(doc(asVolunteer(), 'clearances', VOLUNTEER)));
  });
});

// Concerns go to the admins only, and nobody can pretend to be someone else.
describe('concerns', () => {
  const concern = (overrides = {}) => ({
    reportedBy: VOLUNTEER, reporterName: 'Nomsa', activityId: '', activityTitle: '',
    message: 'The gate at the centre does not lock.', createdAt: 1, status: 'new', ...overrides,
  });

  it('lets a volunteer report a concern as themselves', async () => {
    await assertSucceeds(setDoc(doc(asVolunteer(), 'concerns', 'c1'), concern()));
  });

  it('stops a volunteer reporting in someone else name', async () => {
    await assertFails(setDoc(doc(asVolunteer(), 'concerns', 'c1'), concern({ reportedBy: OTHER })));
  });

  it('stops an empty or overlong concern', async () => {
    await assertFails(setDoc(doc(asVolunteer(), 'concerns', 'c1'), concern({ message: '' })));
    await assertFails(setDoc(doc(asVolunteer(), 'concerns', 'c2'), concern({ message: 'a'.repeat(2001) })));
  });

  it('stops a concern arriving already marked as handled', async () => {
    await assertFails(setDoc(doc(asVolunteer(), 'concerns', 'c1'), concern({ status: 'handled' })));
  });

  it('lets only admins read concerns, not even the person who sent one', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), 'concerns', 'c1'), concern());
    });
    await assertFails(getDoc(doc(asVolunteer(), 'concerns', 'c1')));
    await assertSucceeds(getDoc(doc(asAdmin(), 'concerns', 'c1')));
  });

  it('lets an admin mark a concern as handled but never change or delete it', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), 'concerns', 'c1'), concern());
    });
    await assertSucceeds(updateDoc(doc(asAdmin(), 'concerns', 'c1'), { status: 'handled', handledBy: 'Admin', handledAt: 2 }));
    await assertFails(updateDoc(doc(asAdmin(), 'concerns', 'c1'), { message: 'Changed' }));
    await assertFails(deleteDoc(doc(asAdmin(), 'concerns', 'c1')));
  });
});

// "How did it go?" only from someone who was on the activity, once its day has come.
describe('activity feedback', () => {
  const PAST = 'act_past';
  const FUTURE = 'act_future';
  const feedbackId = (activityId, uid) => `${activityId}_${uid}`;
  const feedback = (activityId, overrides = {}) => ({
    activityId, userId: VOLUNTEER, volunteerName: 'Nomsa', rating: 4, comment: 'Well organised', createdAt: 1,
    ...overrides,
  });

  beforeEach(async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      const db = context.firestore();
      await setDoc(doc(db, 'activities', PAST), {
        title: 'Soup Kitchen', status: 'published', dateMillis: 1, totalSpots: 5, filledSpots: 1,
      });
      await setDoc(doc(db, 'activities', FUTURE), {
        title: 'Baby Packs', status: 'published', dateMillis: 4102444800000, totalSpots: 5, filledSpots: 1,
      });
      await setDoc(doc(db, 'activitySignups', feedbackId(PAST, VOLUNTEER)), { activityId: PAST, userId: VOLUNTEER });
      await setDoc(doc(db, 'activitySignups', feedbackId(FUTURE, VOLUNTEER)), { activityId: FUTURE, userId: VOLUNTEER });
    });
  });

  it('lets a volunteer who was there say how it went', async () => {
    await assertSucceeds(setDoc(doc(asVolunteer(), 'activityFeedback', feedbackId(PAST, VOLUNTEER)), feedback(PAST)));
  });

  it('stops feedback from someone who was not signed up', async () => {
    await assertFails(setDoc(
      doc(asOther(), 'activityFeedback', feedbackId(PAST, OTHER)), feedback(PAST, { userId: OTHER })
    ));
  });

  it('stops feedback before the activity day has come', async () => {
    await assertFails(setDoc(doc(asVolunteer(), 'activityFeedback', feedbackId(FUTURE, VOLUNTEER)), feedback(FUTURE)));
  });

  it('stops a rating outside one to five, or a very long comment', async () => {
    const mine = doc(asVolunteer(), 'activityFeedback', feedbackId(PAST, VOLUNTEER));
    await assertFails(setDoc(mine, feedback(PAST, { rating: 6 })));
    await assertFails(setDoc(mine, feedback(PAST, { rating: 0 })));
    await assertFails(setDoc(mine, feedback(PAST, { comment: 'a'.repeat(501) })));
  });

  it('stops a volunteer writing feedback in someone else name', async () => {
    await assertFails(setDoc(
      doc(asVolunteer(), 'activityFeedback', feedbackId(PAST, OTHER)), feedback(PAST, { userId: OTHER })
    ));
  });

  it('lets a volunteer read their own feedback, and admins read all of it', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), 'activityFeedback', feedbackId(PAST, OTHER)), feedback(PAST, { userId: OTHER }));
    });
    await assertSucceeds(getDoc(doc(asVolunteer(), 'activityFeedback', feedbackId(PAST, VOLUNTEER))));
    await assertFails(getDoc(doc(asVolunteer(), 'activityFeedback', feedbackId(PAST, OTHER))));
    await assertSucceeds(getDocs(query(collection(asAdmin(), 'activityFeedback'), where('activityId', '==', PAST))));
  });
});

describe('collections that are closed', () => {
  it('refuses anything not named in the rules', async () => {
    await assertFails(getDoc(doc(asAdmin(), 'impactStats', 'main')));
    await assertFails(setDoc(doc(asAdmin(), 'secrets', 'x'), { a: 1 }));
  });
});
