import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
  RulesTestEnvironment,
} from '@firebase/rules-unit-testing';
import * as fs from 'fs';
import { doc, getDoc, setDoc, updateDoc, collection, getDocs, serverTimestamp } from 'firebase/firestore';

let testEnv: RulesTestEnvironment;

export async function runDirtyDozenChecks() {
  testEnv = await initializeTestEnvironment({
    projectId: 'skillbridge-security-test',
    firestore: {
      rules: fs.readFileSync('firestore.rules', 'utf8'),
    },
  });

  const alice = testEnv.authenticatedContext('alice_uid', {
    email: 'alice@example.com',
    email_verified: true,
  });
  const unverified = testEnv.authenticatedContext('unv_uid', {
    email: 'unv@example.com',
    email_verified: false,
  });
  const bob = testEnv.authenticatedContext('bob_uid', {
    email: 'bob@example.com',
    email_verified: true,
  });

  // 1. Unverified email rejected on profile create
  await assertFails(
    setDoc(doc(unverified.firestore(), 'users/unv_uid'), {
      userId: 'unv_uid',
      displayName: 'Unverified',
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // 2. Shadow field injection rejected
  await assertFails(
    setDoc(doc(alice.firestore(), 'users/alice_uid'), {
      userId: 'alice_uid',
      displayName: 'Alice',
      isAdmin: true,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // 3. Valid owner profile creation succeeds
  await assertSucceeds(
    setDoc(doc(alice.firestore(), 'users/alice_uid'), {
      userId: 'alice_uid',
      displayName: 'Alice',
      roleMode: 'LEARNER',
      points: 0,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // 4. Cross-user PII read rejected
  await assertFails(getDoc(doc(bob.firestore(), 'users/alice_uid')));

  // 5. Self roleMode escalation to ADMIN rejected
  await assertFails(
    updateDoc(doc(alice.firestore(), 'users/alice_uid'), {
      roleMode: 'ADMIN',
      updatedAt: serverTimestamp(),
    })
  );

  // 6. Client timestamp forgery rejected
  await assertFails(
    setDoc(doc(alice.firestore(), 'users/alice_uid/goals/goal_1'), {
      userId: 'alice_uid',
      targetRole: 'Staff Engineer',
      experienceLevel: 'Senior',
      weeklyHours: 10,
      readinessScore: 70,
      executiveSummary: 'Summary',
      currentSkills: ['TypeScript'],
      matchedSkills: ['TypeScript'],
      isActive: true,
      createdAt: new Date(2020, 1, 1),
      updatedAt: serverTimestamp(),
    })
  );

  // 7. Orphaned gap rejected
  await assertFails(
    setDoc(doc(alice.firestore(), 'users/alice_uid/gaps/gap_orphan'), {
      userId: 'alice_uid',
      goalId: 'non_existent_goal',
      skillName: 'Go',
      priority: 'HIGH',
      currentLevel: 'Beginner',
      targetLevel: 'Proficient',
      whyItMatters: 'Concurrency',
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // 8. Ledger creation & immutable points check
  await assertSucceeds(
    setDoc(doc(alice.firestore(), 'users/alice_uid/pointLedger/tx_1'), {
      userId: 'alice_uid',
      txId: 'tx_1',
      sourceType: 'CHALLENGE',
      sourceId: 'ch_1',
      title: 'Challenge 1',
      points: 100,
      status: 'APPROVED',
      reason: 'Verified',
      createdAt: serverTimestamp(),
    })
  );
  await assertFails(
    updateDoc(doc(alice.firestore(), 'users/alice_uid/pointLedger/tx_1'), {
      points: 9999,
      status: 'PAID',
      reason: 'Tampered',
    })
  );

  // 9. Immutable audit log update rejected
  await assertSucceeds(
    setDoc(doc(alice.firestore(), 'users/alice_uid/auditLogs/log_1'), {
      userId: 'alice_uid',
      actionType: 'LOGIN',
      summary: 'Signed in',
      actorRole: 'LEARNER',
      createdAt: serverTimestamp(),
    })
  );
  await assertFails(
    updateDoc(doc(alice.firestore(), 'users/alice_uid/auditLogs/log_1'), {
      summary: 'Altered',
    })
  );

  // 10. Path ID poisoning rejected
  const longId = 'a'.repeat(130);
  await assertFails(
    setDoc(doc(alice.firestore(), `users/alice_uid/auditLogs/${longId}`), {
      userId: 'alice_uid',
      actionType: 'TEST',
      summary: 'Long id',
      actorRole: 'LEARNER',
      createdAt: serverTimestamp(),
    })
  );

  // 11. Blanket list on reports rejected for non-owner
  await assertFails(getDocs(collection(bob.firestore(), 'reports')));

  // 12. Valid report create with OPEN status only
  await assertSucceeds(
    setDoc(doc(alice.firestore(), 'reports/rep_1'), {
      reporterId: 'alice_uid',
      targetType: 'POST',
      targetId: 'post_1',
      reason: 'Leaked credential detected in snippet',
      status: 'OPEN',
      createdAt: serverTimestamp(),
    })
  );

  await testEnv.cleanup();
}
