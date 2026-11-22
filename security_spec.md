# SkillBridge Firestore Security Specification (Phase 0)

## 1. Data Invariants
1. **User Identity & PII Isolation (`/users/{userId}`)**:
   - Every user document and its subcollections (`goals`, `gaps`, `milestones`, `coachMessages`, `submissions`, `badges`, `pointLedger`, `auditLogs`) are readable and writable strictly by the verified owner (`request.auth.uid == userId && request.auth.token.email_verified == true`) or a verified Platform Admin (`isAdmin()`).
   - `userId` and `createdAt` are immutable after creation; timestamps (`createdAt`, `updatedAt`) must equal `request.time`.
   - New profiles cannot self-escalate `roleMode` to `ADMIN` or arbitrarily inflate `points` above `0` at creation unless performed by `isAdmin()`.
2. **Relational Hierarchy & Atomicity**:
   - `SkillGap` and `RoadmapMilestone` documents require an existing parent `CareerGoal` in the same batch/transaction (`existsAfter(/databases/$(database)/documents/users/$(userId)/goals/$(incoming().goalId))`).
   - `TeamTask` documents under `/teams/{teamId}/tasks/{taskId}` verify parent team membership (`request.auth.uid in get(.../teams/$(teamId)).data.memberIds`) on `get`, `create`, `update`, and `delete`, while `list` enforces `request.auth.uid in resource.data.memberIds` with zero `get()` calls.
3. **Immutable Ledgers & Audit Logs**:
   - `PointLedgerEntry` amounts (`points`, `txId`, `userId`, `createdAt`) are immutable after creation; `AuditLogEntry` and `CoachMessage` are append-only (`allow update: if false`).
   - `UserBadge` documents cannot be altered except toggling `isShareable`.
4. **Community & Moderation**:
   - `CommunityPost` `list` queries enforce `resource.data.isHidden == false || resource.data.authorId == request.auth.uid || isAdmin()`.
   - `CommunityComment` enforces `incoming().postId == postId` and `exists(/databases/$(database)/documents/communityPosts/$(postId))`; only the post author (or admin) can set `isBestAnswer`.
   - `ContentReport` enforces `reporterId == request.auth.uid` on create, `status == 'OPEN'` on create, and restricts reads to the reporter or `isAdmin()`.

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unverified Email Spoof**: Write `/users/uid1` with `email_verified: false` -> `PERMISSION_DENIED`.
2. **Shadow / Ghost Field Injection**: Create `/users/uid1` with extra key `isAdmin: true` -> `PERMISSION_DENIED` (`hasOnly` rejection).
3. **Cross-User Profile Read**: `uid2` attempts `get` on `/users/uid1` -> `PERMISSION_DENIED`.
4. **Self-Role Escalation**: `uid1` creates `/users/uid1` with `roleMode: "ADMIN"` or `points: 99999` -> `PERMISSION_DENIED`.
5. **Client Timestamp Forgery**: Create `/users/uid1/goals/g1` with backdated `createdAt` (`!= request.time`) -> `PERMISSION_DENIED`.
6. **Orphaned SkillGap**: Create `/users/uid1/gaps/gap1` referencing non-existent `goalId: "missing"` -> `PERMISSION_DENIED` (`existsAfter` check).
7. **Ledger Amount Tampering**: Update `/users/uid1/pointLedger/tx1` changing `points` -> `PERMISSION_DENIED`.
8. **Immutable Audit Log Mutation**: Update `/users/uid1/auditLogs/log1` -> `PERMISSION_DENIED`.
9. **Path ID Poisoning**: Create document with ID longer than 128 chars or invalid chars (`"../evil"`) -> `PERMISSION_DENIED` (`isValidId`).
10. **Non-Author Best Answer Hijack**: Non-author `uid2` attempts to set `isBestAnswer: true` on `/communityPosts/p1/comments/c1` -> `PERMISSION_DENIED`.
11. **Non-Member Team Task Access**: Non-member `uid3` attempts `create` or `get` on `/teams/t1/tasks/tk1` -> `PERMISSION_DENIED`.
12. **Blanket List Scraping**: Unauthenticated or unauthorized user attempts `list` on `/reports` or `/users/uid1/pointLedger` -> `PERMISSION_DENIED`.
