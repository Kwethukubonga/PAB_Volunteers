/*
 * Adds sample data: one activity and one announcement for each Philisa programme, plus
 * activities today, tomorrow and in the past (for the Home planner, the Past tab, milestones
 * and "How did it go?") and one draft that only admins see.
 *
 * It also adds ten sample volunteers with their places on those activities, the feedback they
 * left and the clearances an admin would be working through, so the admin's Volunteers list
 * and an activity's sign-up sheet have people in them. They are database records only: there
 * are no sign-in accounts behind them, and nothing can be sent to them.
 *
 *   cd tools/seed
 *   npm install
 *   node seed.js --dry-run                          shows what would be added, touches nothing
 *   node seed.js --key <path to key file>           adds the samples
 *   node seed.js --key <path to key file> --notify  adds them so phones get notified
 *   node seed.js --key <path to key file> --volunteer <email>
 *                                                   also signs that volunteer up to the planner
 *                                                   samples, rates one past activity, asks for a
 *                                                   clearance and sends a sample concern
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
    key: 'after-school',
    programme: 'Afterschool Programmes',
    activity: {
      title: 'Homework Help Afternoon',
      days: 3, startTime: '14:00', endTime: '16:30',
      location: 'Philisa Centre, 55 Strauss Road, Steenberg',
      volunteerRole: 'Homework tutor', totalSpots: 8,
      description: 'Help primary school learners with reading, maths and homework after school. Patience matters more than being a teacher.',
    },
    announcement: {
      title: 'Afterschool tutors needed',
      messageBody: 'We are looking for volunteers who can give two afternoons a month to help learners with their homework. Join a Homework Help Afternoon from the Activities tab.',
    },
  },
  {
    key: 'youth',
    programme: 'Youth Programme',
    activity: {
      title: 'Youth Life Skills Workshop',
      days: 5, startTime: '10:00', endTime: '13:00',
      location: 'Philisa Centre, 55 Strauss Road, Steenberg',
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
      location: 'Philisa Centre, 55 Strauss Road, Steenberg',
      volunteerRole: 'Skills helper', totalSpots: 6,
      description: 'Assist women learning sewing and small business skills. Help set up, hand out materials and encourage everyone taking part.',
    },
    announcement: {
      title: 'Sewing materials wanted',
      messageBody: 'The Sewing and Skills Circle is collecting fabric, thread and scissors. Please bring donations to the centre during office hours.',
    },
  },
  {
    key: 'baby-saver',
    programme: 'Baby Saver',
    activity: {
      title: 'Baby Pack Assembly',
      days: 8, startTime: '10:00', endTime: '13:00',
      location: 'Philisa Centre, 55 Strauss Road, Steenberg',
      volunteerRole: 'Packing volunteer', totalSpots: 10,
      description: 'Sort donated clothing, nappies and blankets into care packs for newborns supported through the Baby Saver programme.',
    },
    announcement: {
      title: 'Baby pack donations',
      messageBody: 'We are collecting newborn nappies, baby clothes and warm blankets for the Baby Saver care packs. Drop them off at the centre any weekday.',
    },
  },
  {
    key: 'safe-houses',
    programme: 'Emergency Safe Houses',
    activity: {
      title: 'Safe House Supplies Drive',
      days: 10, startTime: '09:00', endTime: '12:00',
      // Packing happens at the centre. Safe house addresses are never shown in the app.
      location: 'Philisa Centre, 55 Strauss Road, Steenberg',
      volunteerRole: 'Sorting and packing', totalSpots: 8,
      description: 'Sort and pack toiletries, bedding and food for the emergency safe houses. Packing happens at the centre, and safe house locations are kept private.',
    },
    announcement: {
      title: 'Supplies for the safe houses',
      messageBody: 'Toiletries, bedding and non-perishable food are always needed. Thank you for keeping safe house locations private whenever you help.',
    },
  },
  {
    key: 'seniors',
    programme: 'Senior Programme',
    activity: {
      title: 'Seniors Tea and Chat',
      days: 12, startTime: '10:00', endTime: '12:00',
      location: 'Philisa Centre, 55 Strauss Road, Steenberg',
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
      location: 'Philisa Centre, 55 Strauss Road, Steenberg',
      volunteerRole: 'Kitchen helper', totalSpots: 12,
      description: 'Help prepare, cook and serve a warm meal for families in the community. Closed shoes are required in the kitchen.',
    },
    announcement: {
      title: 'Kitchen safety reminder',
      messageBody: 'Please wear closed shoes and tie back long hair when you volunteer in the kitchen. Aprons and gloves are provided.',
    },
  },
  {
    key: 'mens-cafe',
    programme: 'Men\'s Café',
    activity: {
      title: 'Men\'s Café Evening',
      days: 15, startTime: '17:30', endTime: '19:30',
      location: 'Philisa Centre, 55 Strauss Road, Steenberg',
      volunteerRole: 'Host and set-up', totalSpots: 6,
      description: 'Help set up and host a relaxed evening where men meet to talk and support each other. Includes set-up, making tea and cleaning up.',
    },
    announcement: {
      title: 'Men\'s Café meets every month',
      messageBody: 'The Men\'s Café is a safe space for men to talk and support each other. Volunteers help with set-up, tea and a warm welcome.',
    },
  },
  {
    key: 'search-rescue',
    programme: 'Search and Rescue Team',
    activity: {
      title: 'Search and Rescue Awareness Day',
      days: 17, startTime: '09:00', endTime: '13:00',
      location: 'Lavender Hill',
      volunteerRole: 'Community outreach', totalSpots: 10,
      description: 'Hand out safety information and help the team run an awareness stall on missing persons and child safety.',
    },
    announcement: {
      title: 'Awareness day in Lavender Hill',
      messageBody: 'Help us share safety information with families in Lavender Hill. Sign up for the Search and Rescue Awareness Day from the Activities tab.',
    },
  },
  {
    key: 'social-work',
    programme: 'Social Work Services',
    activity: {
      title: 'Office and Intake Support',
      days: 20, startTime: '09:00', endTime: '13:00',
      location: 'Philisa Centre, 55 Strauss Road, Steenberg',
      volunteerRole: 'Admin assistant', totalSpots: 4,
      description: 'Help the social work team with filing, preparing information packs and welcoming visitors. Everything you see and hear stays confidential.',
    },
    announcement: {
      title: 'Confidentiality at Philisa',
      messageBody: 'Volunteers supporting Social Work Services must keep everything they see and hear private. Speak to the team if you have any questions.',
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
    key: 'tomorrow', programme: 'Afterschool Programmes', days: 1,
    title: 'Reading Buddies', startTime: '14:30', endTime: '16:00',
    volunteerRole: 'Reading buddy', totalSpots: 6,
    description: 'Read one-on-one with a learner who is building confidence. Books are provided.',
  },
  {
    key: 'past-recent', programme: 'Social Work Services', days: -3,
    title: 'Centre Spring Clean', startTime: '09:00', endTime: '12:00',
    volunteerRole: 'Cleaning team', totalSpots: 8,
    description: 'Clean and tidy the rooms at the centre before the new term.',
  },
  {
    key: 'past-week', programme: 'Emergency Safe Houses', days: -10,
    title: 'Blanket Drive Sorting', startTime: '10:00', endTime: '13:00',
    volunteerRole: 'Sorting and packing', totalSpots: 8,
    description: 'Sort donated blankets for the safe houses. Packing happens at the centre.',
  },
  {
    key: 'past-month', programme: 'Youth Programme', days: -24,
    title: 'Youth Soccer Morning', startTime: '09:00', endTime: '12:00',
    volunteerRole: 'Coach assistant', totalSpots: 6,
    description: 'Help run a friendly soccer morning for teenagers, with water breaks and a team talk.',
  },
  {
    key: 'draft', programme: 'Afterschool Programmes', days: 25, status: 'draft',
    title: 'Holiday Club Planning', startTime: '10:00', endTime: '12:00',
    volunteerRole: 'Planner', totalSpots: 5,
    description: 'Still being planned. Only admins see drafts until they are published.',
  },
];

/*
 * Sample volunteers, so the admin's Volunteers list and an activity's sign-ups have people in
 * them rather than one row. These are database records only: there are no sign-in accounts
 * behind them, the addresses are on example.com, which exists to be used in writing like this
 * and reaches nobody, and the telephone numbers are a counting sequence for the same reason.
 *
 * `joins` are activity keys from the two lists above. `rated` leaves feedback on one of them,
 * which is what the admin's "How did it go?" summary reads. Between them they fill the
 * Community Soup Kitchen, the Seniors tea and the homework afternoon enough to look like a
 * real sign-up sheet without taking the last place on any of them.
 */
const sampleVolunteers = [
  {
    first: 'Nosipho', last: 'Mabaso', area: 'Lavender Hill', joinedDaysAgo: 412,
    interests: ['Community Feeding', 'Afterschool Programmes'],
    joins: ['community-feeding', 'today', 'past-week'],
    rated: { key: 'past-week', rating: 5, comment: 'Busy morning, but we got through every box before lunch.' },
    clearance: 'cleared',
    emergency: { name: 'Thembisa Mabaso', phone: '071 000 0030' },
  },
  {
    first: 'Charmaine', last: 'Fortuin', area: 'Steenberg', joinedDaysAgo: 365,
    interests: ['Senior Programme', 'Women Empowerment'],
    joins: ['seniors', 'womens-empowerment'],
    clearance: 'cleared',
    emergency: { name: 'Elton Fortuin', phone: '071 000 0031' },
  },
  {
    first: 'Sipho', last: 'Ngcobo', area: 'Retreat', joinedDaysAgo: 280,
    interests: ["Men's Café", 'Youth Programme'],
    joins: ['mens-cafe', 'past-month'],
    rated: { key: 'past-month', rating: 4, comment: 'Good turnout. We could do with one more coach next time.' },
  },
  {
    first: 'Fagrieyah', last: 'Isaacs', area: 'Grassy Park', joinedDaysAgo: 240,
    interests: ['Baby Saver', 'Women Empowerment'],
    joins: ['baby-saver', 'womens-empowerment', 'today'],
    clearance: 'requested',
  },
  {
    first: 'Lerato', last: 'Molefe', area: 'Seawinds', joinedDaysAgo: 190,
    interests: ['Youth Programme', 'Afterschool Programmes'],
    joins: ['after-school', 'youth', 'tomorrow'],
    emergency: { name: 'Palesa Molefe', phone: '071 000 0032' },
  },
  {
    first: 'Ashwin', last: 'Petersen', area: 'Muizenberg', joinedDaysAgo: 150,
    interests: ['Community Feeding', 'Emergency Safe Houses'],
    joins: ['community-feeding', 'safe-houses', 'past-week'],
    rated: { key: 'past-week', rating: 5, comment: 'Well run. Everyone knew what they were doing by the second hour.' },
  },
  {
    first: 'Zanele', last: 'Khumalo', area: 'Lavender Hill', joinedDaysAgo: 120,
    interests: ['Women Empowerment', 'Senior Programme'],
    joins: ['seniors', 'womens-empowerment', 'tomorrow'],
    clearance: 'cleared',
  },
  {
    first: 'Riyaad', last: 'Davids', area: 'Capricorn Park', joinedDaysAgo: 95,
    interests: ["Men's Café", 'Community Feeding'],
    joins: ['mens-cafe', 'community-feeding'],
    emergency: { name: 'Shanaaz Davids', phone: '071 000 0033' },
  },
  {
    first: 'Thandiwe', last: 'Mkhize', area: 'Steenberg', joinedDaysAgo: 60,
    interests: ['Baby Saver', 'Afterschool Programmes'],
    joins: ['after-school', 'baby-saver'],
    clearance: 'requested',
  },
  {
    first: 'Denise', last: 'Arendse', area: 'Lavender Hill', joinedDaysAgo: 21,
    interests: ['Senior Programme', 'Community Feeding'],
    joins: ['seniors', 'community-feeding', 'after-school'],
  },
];

const DAY = 24 * 60 * 60 * 1000;

/**
 * The sample volunteers, their places on the sample activities, the feedback they left and
 * the clearances an admin would be looking at. Keyed off the activities already built, so a
 * volunteer can never be signed up to something that is not there.
 */
function buildPeopleDocs(docs) {
  const now = Date.now();
  const activities = Object.fromEntries(
    docs.filter((d) => d.collection === 'activities').map((d) => [d.id, d.data])
  );

  return sampleVolunteers.flatMap((person, i) => {
    const uid = `seed-volunteer-${String(i + 1).padStart(2, '0')}`;
    const name = `${person.first} ${person.last}`;
    const out = [{
      collection: 'users',
      id: uid,
      data: {
        firstName: person.first,
        lastName: person.last,
        email: `${person.first}.${person.last}@example.com`.toLowerCase(),
        phone: `071 000 00${String(i + 10)}`,
        area: person.area,
        programmeInterests: person.interests,
        favouriteActivityIds: [],
        role: 'volunteer',
        volunteerId: `VOL-2026-${101 + i}`,
        profileComplete: true,
        joinedDate: now - person.joinedDaysAgo * DAY,
        emergencyContactName: person.emergency ? person.emergency.name : '',
        emergencyContactPhone: person.emergency ? person.emergency.phone : '',
      },
    }];

    person.joins.forEach((key, n) => {
      const activityId = `seed-activity-${key}`;
      const a = activities[activityId];
      if (!a) return;
      out.push({
        collection: 'activitySignups',
        id: `${activityId}_${uid}`,
        activityId,
        data: {
          activityId, userId: uid, volunteerName: name,
          activityTitle: a.title, programme: a.programme, date: a.date, dateMillis: a.dateMillis,
          startTime: a.startTime, endTime: a.endTime, location: a.location,
          // Spread out, so the sign-up sheet does not read as ten people at the same second.
          signedUpDate: now - (i + n + 2) * DAY,
        },
      });
    });

    if (person.rated) {
      const activityId = `seed-activity-${person.rated.key}`;
      out.push({
        collection: 'activityFeedback',
        id: `${activityId}_${uid}`,
        data: {
          activityId, userId: uid, volunteerName: name,
          rating: person.rated.rating, comment: person.rated.comment,
          createdAt: now - (i + 1) * DAY,
        },
      });
    }

    if (person.clearance === 'requested') {
      out.push({
        collection: 'clearances',
        id: uid,
        // Marked, so --remove takes away only what the seed put there.
        data: { volunteerName: name, status: 'requested', requestedAt: now - (i + 1) * DAY, seeded: true },
      });
    } else if (person.clearance === 'cleared') {
      const clearedAt = now - (i + 30) * DAY;
      out.push({
        collection: 'clearances',
        id: uid,
        data: {
          volunteerName: name, status: 'cleared',
          requestedAt: clearedAt - 7 * DAY, clearedAt,
          expiresAt: clearedAt + 365 * DAY, checkedBy: 'Philisa office', seeded: true,
        },
      });
    }

    return out;
  });
}

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
      location: 'Philisa Centre, 55 Strauss Road, Steenberg',
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
 * activity, feedback on one past activity (the other two still ask "How did it go?"), a
 * clearance request and a concern for admins. Ids start with "seed-" like everything else.
 */
function buildVolunteerDocs(docs, uid, name) {
  const now = Date.now();
  const activities = Object.fromEntries(docs.filter((d) => d.collection === 'activities').map((d) => [d.id, d.data]));
  const joined = ['today', 'tomorrow', 'past-recent', 'past-week', 'past-month']
    .map((key) => `seed-activity-${key}`)
    .concat('seed-activity-after-school');
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
      collection: 'clearances',
      id: uid,
      // Marked, so --remove takes away only a request the seed made.
      data: { volunteerName: name, status: 'requested', requestedAt: now, seeded: true },
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

async function remove(db, docs) {
  const batch = db.batch();
  let signups = 0;
  for (const doc of docs) {
    if (doc.collection === 'activities') {
      const taken = await db.collection('activitySignups').where('activityId', '==', doc.id).get();
      taken.forEach((s) => batch.delete(s.ref));
      signups += taken.size;
    }
    batch.delete(db.collection(doc.collection).doc(doc.id));
  }
  // Feedback on the samples, sample concerns and clearance requests the seed made.
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
  // After the activities, because every sample volunteer is signed up to one of them.
  docs.push(...buildPeopleDocs(docs));

  if (has('--dry-run')) {
    for (const doc of docs) {
      const d = doc.data;
      // Each kind keeps something different worth reading back before anything is written.
      const says = {
        activities: () => `${d.title} ${d.date} ${d.startTime}-${d.endTime}`,
        users: () => `${d.firstName} ${d.lastName} · ${d.area} · ${d.volunteerId}`,
        activitySignups: () => `${d.volunteerName} → ${d.activityTitle}`,
        activityFeedback: () => `${d.volunteerName} rated ${d.activityId.replace('seed-activity-', '')} ${d.rating}/5`,
        clearances: () => `${d.volunteerName} · ${d.status}`,
        concerns: () => `${d.reporterName} · ${d.activityTitle}`,
      }[doc.collection];
      console.log(`${doc.collection.padEnd(17)} ${doc.id.padEnd(42)} ${says ? says() : d.title}`);
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
    // A clearance the volunteer already has is left alone.
    const own = buildVolunteerDocs(docs, account.uid, name);
    const existing = await db.collection('clearances').doc(account.uid).get();
    await add(db, own.filter((d) => !(d.collection === 'clearances' && existing.exists)));
    console.log(`Samples for ${name} (${email}) added.`);
  }
}

main().catch((e) => {
  console.error(e.message || e);
  process.exit(1);
});
