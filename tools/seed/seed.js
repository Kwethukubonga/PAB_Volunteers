/*
 * Adds sample data: one activity and one announcement for each Philisa programme, plus
 * activities today, tomorrow and in the past (for the Home planner, the Past tab, milestones
 * and "How did it go?") and one draft that only admins see.
 *
 *   cd tools/seed
 *   npm install
 *   node seed.js --dry-run                          shows what would be added, touches nothing
 *   node seed.js --key <path to key file>           adds the samples
 *   node seed.js --key <path to key file> --notify  adds them so phones get notified
 *   node seed.js --key <path to key file> --volunteer <email>
 *                                                   also signs that volunteer up to the planner
 *                                                   samples, rates one past activity and sends a
 *                                                   sample concern
 *   node seed.js --key <path to key file> --remove  takes every sample out again
 *
 * The key file comes from Firebase console > Project settings > Service accounts >
 * Generate new private key. It gives full access to the database, so keep it outside
 * this repo and never commit it.
 *
 * Every sample id starts with "seed-". Running it again skips samples that already exist,
 * so spots people have taken are never reset. Activity dates count forward from the day
 * it is run, so the samples are always upcoming.
 */

const path = require('path');

const args = process.argv.slice(2);
const has = (name) => args.includes(name);
const valueOf = (name) => {
  const i = args.indexOf(name);
  return i >= 0 ? args[i + 1] : undefined;
};

// South Africa has no daylight saving, so midnight is always 22:00 UTC the day before.
const SAST_OFFSET = 2 * 60 * 60 * 1000;

/** The date the app expects: "yyyy-MM-dd" text plus midnight in South African time. */
function daysFromToday(offset) {
  const nowInSa = new Date(Date.now() + SAST_OFFSET);
  const midnightUtc = Date.UTC(
    nowInSa.getUTCFullYear(), nowInSa.getUTCMonth(), nowInSa.getUTCDate() + offset
  );
  return {
    date: new Date(midnightUtc).toISOString().slice(0, 10),
    dateMillis: midnightUtc - SAST_OFFSET,
  };
}

// Programme names must match the app exactly, because they are matched as text.
const programmes = [
  {
    key: 'holiday',
    programme: 'Holiday Programme',
    activity: {
      title: 'Holiday Club Morning',
      days: 3, startTime: '09:00', endTime: '12:00',
      location: 'Philisa Centre, 55 - 57 Strauss Road, Steenberg',
      volunteerRole: 'Activity helper', totalSpots: 8,
      description: 'Help run games, reading and crafts for children during the school holidays. Energy matters more than experience.',
    },
    announcement: {
      title: 'Holiday helpers needed',
      messageBody: 'We are looking for volunteers who can give a few mornings during the school holidays. Join a Holiday Club Morning from the Activities tab.',
    },
  },
  {
    key: 'youth',
    programme: 'Youth Programme',
    activity: {
      title: 'Youth Life Skills Workshop',
      days: 5, startTime: '10:00', endTime: '13:00',
      location: 'Philisa Centre, 55 - 57 Strauss Road, Steenberg',
      volunteerRole: 'Workshop assistant', totalSpots: 6,
      description: 'Support a session for teenagers on setting goals, building confidence and staying safe online. You will help run the group activities.',
    },
    announcement: {
      title: 'New youth workshops this term',
      messageBody: 'The Youth Programme is running monthly life skills workshops for teenagers. Volunteers with youth work or coaching experience are especially welcome.',
    },
  },
  {
    key: 'womens-empowerment',
    programme: 'Women Empowerment',
    activity: {
      title: 'Sewing and Skills Circle',
      days: 6, startTime: '09:30', endTime: '12:30',
      location: 'Philisa Centre, 55 - 57 Strauss Road, Steenberg',
      volunteerRole: 'Skills helper', totalSpots: 6,
      description: 'Assist women learning sewing and small business skills. Help set up, hand out materials and encourage everyone taking part.',
    },
    announcement: {
      title: 'Sewing materials wanted',
      messageBody: 'The Sewing and Skills Circle is collecting fabric, thread and scissors. Please bring donations to the centre during office hours.',
    },
  },
  {
    key: 'noordkaap',
    programme: 'Noordkaap - Afterschool Programme',
    activity: {
      title: 'Homework Help Afternoon',
      days: 8, startTime: '14:00', endTime: '16:30',
      location: 'Philisa Centre, 55 - 57 Strauss Road, Steenberg',
      volunteerRole: 'Homework tutor', totalSpots: 8,
      description: 'Help primary school learners with reading, maths and homework after school. Patience matters more than being a teacher.',
    },
    announcement: {
      title: 'Afterschool tutors needed',
      messageBody: 'We are looking for volunteers who can give two afternoons a month to help learners with their homework. Join a Homework Help Afternoon from the Activities tab.',
    },
  },
  {
    key: 'ecd',
    programme: 'ECD Centre (Early Childhood Development)',
    activity: {
      title: 'ECD Classroom Helper',
      days: 10, startTime: '08:30', endTime: '12:00',
      location: 'Philisa Centre, 55 - 57 Strauss Road, Steenberg',
      volunteerRole: 'Classroom helper', totalSpots: 6,
      description: 'Help the ECD teachers with story time, play and snacks for young children. A teacher is always in the room.',
    },
    announcement: {
      title: 'Toys and books for the ECD Centre',
      messageBody: 'Clean toys, picture books and art supplies are always needed for the ECD Centre. Drop them off at the centre any weekday.',
    },
  },
  {
    key: 'seniors',
    programme: 'Senior Programme',
    activity: {
      title: 'Seniors Tea and Chat',
      days: 12, startTime: '10:00', endTime: '12:00',
      location: 'Philisa Centre, 55 - 57 Strauss Road, Steenberg',
      volunteerRole: 'Companion', totalSpots: 8,
      description: 'Spend a morning with older members of the community over tea, games and conversation. Help with serving and setting up.',
    },
    announcement: {
      title: 'Tea mornings are back',
      messageBody: 'Our tea mornings for seniors return this month. A friendly face and a good conversation go a long way.',
    },
  },
  {
    key: 'community-feeding',
    programme: 'Community Feeding',
    activity: {
      title: 'Community Soup Kitchen',
      days: 13, startTime: '09:00', endTime: '13:00',
      location: 'Philisa Centre, 55 - 57 Strauss Road, Steenberg',
      volunteerRole: 'Kitchen helper', totalSpots: 12,
      description: 'Help prepare, cook and serve a warm meal for families in the community. Closed shoes are required in the kitchen.',
    },
    announcement: {
      title: 'Kitchen safety reminder',
      messageBody: 'Please wear closed shoes and tie back long hair when you volunteer in the kitchen. Aprons and gloves are provided.',
    },
  },
  {
    key: 'social-work',
    programme: 'Social Work / Internship',
    activity: {
      title: 'Office and Intake Support',
      days: 20, startTime: '09:00', endTime: '13:00',
      location: 'Philisa Centre, 55 - 57 Strauss Road, Steenberg',
      volunteerRole: 'Admin assistant', totalSpots: 4,
      description: 'Help the social work team with filing, preparing information packs and welcoming visitors. Everything you see and hear stays confidential.',
    },
    announcement: {
      title: 'Confidentiality at Philisa',
      messageBody: 'Volunteers supporting Social Work / Internship must keep everything they see and hear private. Speak to the team if you have any questions.',
    },
  },
  {
    key: 'internship',
    programme: 'Internship',
    activity: {
      title: 'Intern Welcome Morning',
      days: 22, startTime: '09:00', endTime: '12:00',
      location: 'Philisa Centre, 55 - 57 Strauss Road, Steenberg',
      volunteerRole: 'Intern', totalSpots: 4,
      description: 'Meet the team, see how the centre runs and find out how an internship at Philisa works.',
    },
    announcement: {
      title: 'Internships at Philisa',
      messageBody: 'Students who need practical hours can intern at Philisa. Join the Intern Welcome Morning from the Activities tab to find out more.',
    },
  },
];

// Samples that show the planner and history screens. Negative days are in the past, so they
// count as finished: they fill the Past tab and milestones, and offer "How did it go?".
const plannerSamples = [
  {
    key: 'today', programme: 'Community Feeding', days: 0,
    title: 'Food Parcel Packing', startTime: '15:00', endTime: '17:00',
    volunteerRole: 'Packing volunteer', totalSpots: 10,
    description: 'Pack food parcels for families on the Community Feeding list. On the day, "I need help" appears on this activity for anyone signed up.',
  },
  {
    key: 'tomorrow', programme: 'Noordkaap - Afterschool Programme', days: 1,
    title: 'Reading Buddies', startTime: '14:30', endTime: '16:00',
    volunteerRole: 'Reading buddy', totalSpots: 6,
    description: 'Read one-on-one with a learner who is building confidence. Books are provided.',
  },
  {
    key: 'past-recent', programme: 'Social Work / Internship', days: -3,
    title: 'Centre Spring Clean', startTime: '09:00', endTime: '12:00',
    volunteerRole: 'Cleaning team', totalSpots: 8,
    description: 'Clean and tidy the rooms at the centre before the new term.',
  },
  {
    key: 'past-week', programme: 'Community Feeding', days: -10,
    title: 'Blanket Drive Sorting', startTime: '10:00', endTime: '13:00',
    volunteerRole: 'Sorting and packing', totalSpots: 8,
    description: 'Sort donated blankets for families in need. Packing happens at the centre.',
  },
  {
    key: 'past-month', programme: 'Youth Programme', days: -24,
    title: 'Youth Soccer Morning', startTime: '09:00', endTime: '12:00',
    volunteerRole: 'Coach assistant', totalSpots: 6,
    description: 'Help run a friendly soccer morning for teenagers, with water breaks and a team talk.',
  },
  {
    key: 'draft', programme: 'Holiday Programme', days: 25, status: 'draft',
    title: 'Holiday Club Planning', startTime: '10:00', endTime: '12:00',
    volunteerRole: 'Planner', totalSpots: 5,
    description: 'Still being planned. Only admins see drafts until they are published.',
  },
];

function buildDocs(notify) {
  const now = Date.now();
  // Only set when asked, because the app notifies volunteers about anything with a new publishedAt.
  const published = notify ? { publishedAt: now } : {};
  return programmes.flatMap((p, i) => {
    const { days, ...activity } = p.activity;
    return [
      {
        collection: 'activities',
        id: `seed-activity-${p.key}`,
        data: {
          ...activity,
          programme: p.programme,
          ...daysFromToday(days),
          filledSpots: 0,
          status: 'published',
          createdBy: 'seed',
          createdDate: now,
          ...published,
        },
      },
      {
        collection: 'announcements',
        id: `seed-announcement-${p.key}`,
        data: {
          ...p.announcement,
          // A minute apart, so they keep this order in the app.
          date: now - i * 60 * 1000,
          status: 'published',
          createdBy: 'seed',
          ...published,
        },
      },
    ];
  }).concat(plannerSamples.map(({ key, days, status = 'published', ...activity }) => ({
    collection: 'activities',
    id: `seed-activity-${key}`,
    data: {
      ...activity,
      location: 'Philisa Centre, 55 - 57 Strauss Road, Steenberg',
      ...daysFromToday(days),
      filledSpots: 0,
      status,
      createdBy: 'seed',
      createdDate: now,
      ...(status === 'published' ? published : {}),
    },
  })));
}

/**
 * A volunteer's own sample records, so the screens that need them have something to show:
 * places on the planner samples (today, tomorrow and the past three) and one upcoming
 * activity, feedback on one past activity (the other two still ask "How did it go?") and a
 * concern for admins. Ids start with "seed-" like everything else.
 */
function buildVolunteerDocs(docs, uid, name) {
  const now = Date.now();
  const activities = Object.fromEntries(docs.filter((d) => d.collection === 'activities').map((d) => [d.id, d.data]));
  const joined = ['today', 'tomorrow', 'past-recent', 'past-week', 'past-month']
    .map((key) => `seed-activity-${key}`)
    .concat('seed-activity-noordkaap');
  const signups = joined.map((activityId) => {
    const a = activities[activityId];
    return {
      collection: 'activitySignups',
      id: `${activityId}_${uid}`,
      activityId,
      data: {
        activityId, userId: uid, volunteerName: name,
        activityTitle: a.title, programme: a.programme, date: a.date, dateMillis: a.dateMillis,
        startTime: a.startTime, endTime: a.endTime, location: a.location, signedUpDate: now,
      },
    };
  });
  return [
    ...signups,
    {
      collection: 'activityFeedback',
      id: `seed-activity-past-week_${uid}`,
      data: {
        activityId: 'seed-activity-past-week', userId: uid, volunteerName: name, rating: 5,
        comment: 'Well organised, and the team made newcomers feel welcome.', createdAt: now,
      },
    },
    {
      collection: 'concerns',
      id: `seed-concern-${uid}`,
      data: {
        reportedBy: uid, reporterName: name,
        activityId: 'seed-activity-past-recent', activityTitle: activities['seed-activity-past-recent'].title,
        message: 'Sample concern for testing: the side gate was locked when we arrived, so we waited outside for 20 minutes.',
        createdAt: now, status: 'new',
      },
    },
  ];
}

async function add(db, docs) {
  let added = 0;
  let skipped = 0;
  for (const doc of docs) {
    try {
      await db.collection(doc.collection).doc(doc.id).create(doc.data);
      if (doc.collection === 'activitySignups') {
        const { FieldValue } = require('firebase-admin/firestore');
        await db.collection('activities').doc(doc.activityId).update({ filledSpots: FieldValue.increment(1) });
      }
      added++;
    } catch (e) {
      // 6 is ALREADY_EXISTS: left alone so spots already taken are kept.
      if (e.code === 6) skipped++;
      else throw e;
    }
  }
  console.log(`Added ${added}, skipped ${skipped} that already existed.`);
}

// Samples for programmes Philisa no longer runs, so --remove still clears them.
const retiredDocs = ['after-school', 'baby-saver', 'safe-houses', 'mens-cafe', 'search-rescue'].flatMap((key) => [
  { collection: 'activities', id: `seed-activity-${key}` },
  { collection: 'announcements', id: `seed-announcement-${key}` },
]);

async function remove(db, docs) {
  const batch = db.batch();
  let signups = 0;
  for (const doc of docs.concat(retiredDocs)) {
    if (doc.collection === 'activities') {
      const taken = await db.collection('activitySignups').where('activityId', '==', doc.id).get();
      taken.forEach((s) => batch.delete(s.ref));
      signups += taken.size;
    }
    batch.delete(db.collection(doc.collection).doc(doc.id));
  }
  // Feedback on the samples, sample concerns, and clearance requests from before that feature went.
  const feedback = await db.collection('activityFeedback').where('activityId', '>=', 'seed-').where('activityId', '<', 'seed.').get();
  feedback.forEach((d) => batch.delete(d.ref));
  const concerns = (await db.collection('concerns').get()).docs.filter((d) => d.id.startsWith('seed-'));
  concerns.forEach((d) => batch.delete(d.ref));
  const clearances = await db.collection('clearances').where('seeded', '==', true).get();
  clearances.forEach((d) => batch.delete(d.ref));
  await batch.commit();
  console.log(`Removed ${docs.length} samples, ${signups} sign-ups, ${feedback.size} feedback, ${concerns.length} concerns and ${clearances.size} clearance requests.`);
}

async function main() {
  const docs = buildDocs(has('--notify'));

  if (has('--dry-run')) {
    for (const doc of docs) {
      const d = doc.data;
      const when = doc.collection === 'activities' ? `${d.date} ${d.startTime}-${d.endTime}` : '';
      console.log(`${doc.collection.padEnd(13)} ${doc.id.padEnd(40)} ${d.title} ${when}`);
    }
    const own = valueOf('--volunteer') ? buildVolunteerDocs(docs, '<uid>', valueOf('--volunteer')) : [];
    for (const doc of own) console.log(`${doc.collection.padEnd(17)} ${doc.id}`);
    console.log(`\n${docs.length + own.length} documents. Nothing was written.`);
    return;
  }

  const keyPath = valueOf('--key');
  if (!keyPath) {
    console.error('Give the service account key file: node seed.js --key <path>');
    process.exit(1);
  }

  // Loaded here so --dry-run works before npm install.
  const { initializeApp, cert } = require('firebase-admin/app');
  const { getFirestore } = require('firebase-admin/firestore');
  initializeApp({ credential: cert(require(path.resolve(keyPath))) });
  const db = getFirestore();

  if (has('--remove')) {
    await remove(db, docs);
    return;
  }
  await add(db, docs);

  const email = valueOf('--volunteer');
  if (email) {
    const { getAuth } = require('firebase-admin/auth');
    const account = await getAuth().getUserByEmail(email);
    const profile = (await db.collection('users').doc(account.uid).get()).data() || {};
    const name = `${profile.firstName || ''} ${profile.lastName || ''}`.trim() || email;
    await add(db, buildVolunteerDocs(docs, account.uid, name));
    console.log(`Samples for ${name} (${email}) added.`);
  }
}

main().catch((e) => {
  console.error(e.message || e);
  process.exit(1);
});
