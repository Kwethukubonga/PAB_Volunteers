/*
 * Adds sample data: one activity and one announcement for each Philisa programme.
 *
 *   cd tools/seed
 *   npm install
 *   node seed.js --dry-run                          shows what would be added, touches nothing
 *   node seed.js --key <path to key file>           adds the samples
 *   node seed.js --key <path to key file> --notify  adds them so phones get notified
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
  });
}

async function add(db, docs) {
  let added = 0;
  let skipped = 0;
  for (const doc of docs) {
    try {
      await db.collection(doc.collection).doc(doc.id).create(doc.data);
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
  await batch.commit();
  console.log(`Removed ${docs.length} samples and ${signups} sign-ups for them.`);
}

async function main() {
  const docs = buildDocs(has('--notify'));

  if (has('--dry-run')) {
    for (const doc of docs) {
      const d = doc.data;
      const when = doc.collection === 'activities' ? `${d.date} ${d.startTime}-${d.endTime}` : '';
      console.log(`${doc.collection.padEnd(13)} ${doc.id.padEnd(40)} ${d.title} ${when}`);
    }
    console.log(`\n${docs.length} documents. Nothing was written.`);
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

  if (has('--remove')) await remove(db, docs);
  else await add(db, docs);
}

main().catch((e) => {
  console.error(e.message || e);
  process.exit(1);
});
