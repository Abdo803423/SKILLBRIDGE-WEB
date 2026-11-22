import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import type {
  AccountEntity,
  AccountRole,
  CompanyEntity,
  DeviceSessionEntity,
  ModerationReportEntity,
  PlatformActivityEntity,
  PlatformNotificationEntity,
  ProjectApplicationEntity,
  ProjectBriefEntity,
  ProjectTaskEntity,
  SessionPrincipal,
  WorkSubmissionEntity,
} from '../src/types/skillbridge.ts';

export interface CentralEcosystemState {
  revision: number;
  updatedAtEpochMs: number;
  accounts: AccountEntity[];
  companies: CompanyEntity[];
  briefs: ProjectBriefEntity[];
  applications: ProjectApplicationEntity[];
  tasks: ProjectTaskEntity[];
  submissions: WorkSubmissionEntity[];
  notifications: PlatformNotificationEntity[];
  activities: PlatformActivityEntity[];
  reports: ModerationReportEntity[];
  deviceSessions: DeviceSessionEntity[];
}

export class BackendHttpError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

const SECRET_PATTERNS = ['AIza', 'sk-', 'Bearer ', '-----BEGIN', 'ghp_', 'AKIA'];

export function containsSecretLeak(text: string): boolean {
  return SECRET_PATTERNS.some((p) => text.includes(p));
}

export function hashPassword(password: string, existingSalt?: string): { salt: string; hash: string } {
  const salt = existingSalt || crypto.randomBytes(16).toString('hex');
  const hash = crypto.scryptSync(password, salt, 64).toString('hex');
  return { salt, hash };
}

export function verifyPassword(password: string, salt: string, expectedHash: string): boolean {
  const { hash } = hashPassword(password, salt);
  const a = Buffer.from(hash, 'hex');
  const b = Buffer.from(expectedHash, 'hex');
  if (a.length !== b.length) return false;
  return crypto.timingSafeEqual(a, b);
}

export function computeLevelFromEvidence(totalPoints: number, verifiedSubmissionsCount: number): string {
  if (totalPoints >= 3500 && verifiedSubmissionsCount >= 10) return 'Director / Leadership';
  if (totalPoints >= 1500 && verifiedSubmissionsCount >= 5) return 'Senior';
  if (totalPoints >= 500 && verifiedSubmissionsCount >= 2) return 'Mid-level';
  return 'Junior';
}

function toPrincipal(acc: AccountEntity, isGoogleLinked = false): SessionPrincipal {
  return {
    accountId: acc.id,
    loginId: acc.loginId,
    displayName: acc.displayName,
    email: acc.email,
    role: acc.role,
    companyId: acc.companyId,
    isGoogleLinked,
  };
}

function sanitizeAccountForClient(acc: AccountEntity): AccountEntity {
  return {
    ...acc,
    passwordSalt: '',
    passwordHash: '',
  };
}

function createSeedState(): CentralEcosystemState {
  const now = Date.now();
  const day = 86_400_000;

  const adminPass = hashPassword('Admin@123');
  const studentPass = hashPassword('Student@123');
  const companyPass = hashPassword('Company@123');
  const directorPass = hashPassword('Director@123');
  const extraPass = hashPassword('DemoPass@2026');

  const accounts: AccountEntity[] = [
    {
      id: 'acc_admin_demo',
      loginId: 'admin_demo',
      displayName: 'SkillBridge Operations Admin',
      email: 'admin@skillbridge.io',
      passwordSalt: adminPass.salt,
      passwordHash: adminPass.hash,
      role: 'PLATFORM_ADMIN',
      companyId: '',
      isActive: true,
      careerLevel: 'Director / Leadership',
      domain: 'Platform Governance',
      country: 'UAE',
      points: 0,
      streakDays: 0,
      verifiedCount: 0,
      skillsCsv: 'Trust & Safety,Rubric Audits,Governance',
      targetSkillsCsv: '',
      goalsSummary: 'Maintain platform integrity, company verification, and fair brief approvals.',
      weeklyHours: 40,
      profileCompletion: 100,
      createdAtEpochMs: now - 25 * day,
    },
    {
      id: 'acc_student_demo',
      loginId: 'student_demo',
      displayName: 'Lina Mansour',
      email: 'student@skillbridge.io',
      passwordSalt: studentPass.salt,
      passwordHash: studentPass.hash,
      role: 'STUDENT',
      companyId: '',
      isActive: true,
      careerLevel: 'Mid-level',
      domain: 'Data Science & AI',
      country: 'Egypt',
      points: 640,
      streakDays: 6,
      verifiedCount: 2,
      skillsCsv: 'Kotlin,SQL,Python,REST APIs,Analytics',
      targetSkillsCsv: 'Time-Series Telemetry,System Design,React TypeScript',
      goalsSummary: 'Complete Smart City Telemetry & Cloud Gateway projects to earn Senior verified status.',
      weeklyHours: 14,
      profileCompletion: 92,
      createdAtEpochMs: now - 20 * day,
    },
    {
      id: 'acc_company_demo',
      loginId: 'company_demo',
      displayName: 'NileGrid Technologies',
      email: 'talent@nilegrid.io',
      passwordSalt: companyPass.salt,
      passwordHash: companyPass.hash,
      role: 'COMPANY',
      companyId: 'comp_nilegrid',
      isActive: true,
      careerLevel: 'Director / Leadership',
      domain: 'Data Science & AI',
      country: 'Egypt',
      points: 0,
      streakDays: 0,
      verifiedCount: 0,
      skillsCsv: 'Telemetry,Smart Cities,Cloud Architecture',
      targetSkillsCsv: '',
      goalsSummary: 'Publish real-world engineering briefs and identify top MENA talent.',
      weeklyHours: 20,
      profileCompletion: 100,
      createdAtEpochMs: now - 22 * day,
    },
    {
      id: 'acc_director_demo',
      loginId: 'director_demo',
      displayName: 'Dr. Karim Hassan',
      email: 'karim.hassan@nilegrid.io',
      passwordSalt: directorPass.salt,
      passwordHash: directorPass.hash,
      role: 'COMPANY_DIRECTOR',
      companyId: 'comp_nilegrid',
      isActive: true,
      careerLevel: 'Director / Leadership',
      domain: 'Data Science & AI',
      country: 'Egypt',
      points: 0,
      streakDays: 0,
      verifiedCount: 0,
      skillsCsv: 'Architecture Review,Telemetry Rubric,Mentorship',
      targetSkillsCsv: '',
      goalsSummary: 'Supervise assigned NileGrid project briefs and verify student deliverables.',
      weeklyHours: 18,
      profileCompletion: 100,
      createdAtEpochMs: now - 21 * day,
    },
    {
      id: 'acc_student_omar',
      loginId: 'omar_khaled',
      displayName: 'Omar Khaled',
      email: 'omar@skillbridge.io',
      passwordSalt: extraPass.salt,
      passwordHash: extraPass.hash,
      role: 'STUDENT',
      companyId: '',
      isActive: true,
      careerLevel: 'Senior',
      domain: 'Backend & Cloud',
      country: 'Saudi Arabia',
      points: 1280,
      streakDays: 14,
      verifiedCount: 5,
      skillsCsv: 'Kotlin,Distributed Systems,Kubernetes,PostgreSQL',
      targetSkillsCsv: 'Service Mesh,Observability',
      goalsSummary: 'Lead backend architecture teams.',
      weeklyHours: 16,
      profileCompletion: 96,
      createdAtEpochMs: now - 18 * day,
    },
    {
      id: 'acc_student_sara',
      loginId: 'sara_nabil',
      displayName: 'Sara Nabil',
      email: 'sara@skillbridge.io',
      passwordSalt: extraPass.salt,
      passwordHash: extraPass.hash,
      role: 'STUDENT',
      companyId: '',
      isActive: true,
      careerLevel: 'Junior',
      domain: 'Cybersecurity',
      country: 'Jordan',
      points: 290,
      streakDays: 4,
      verifiedCount: 1,
      skillsCsv: 'Python,Network Analysis,Unit Testing',
      targetSkillsCsv: 'Protocol Parsing,Binary Analysis',
      goalsSummary: 'Advance through Ethical Reverse Engineering labs.',
      weeklyHours: 10,
      profileCompletion: 78,
      createdAtEpochMs: now - 12 * day,
    },
    {
      id: 'acc_company_oasisfin',
      loginId: 'oasisfin_corp',
      displayName: 'OasisFin Cloud',
      email: 'partner@oasisfin.ae',
      passwordSalt: extraPass.salt,
      passwordHash: extraPass.hash,
      role: 'COMPANY',
      companyId: 'comp_oasisfin',
      isActive: true,
      careerLevel: 'Director / Leadership',
      domain: 'Backend & Cloud',
      country: 'UAE',
      points: 0,
      streakDays: 0,
      verifiedCount: 0,
      skillsCsv: 'FinTech,Idempotent Ledgers',
      targetSkillsCsv: '',
      goalsSummary: 'Pending verification for cloud payment engineering briefs.',
      weeklyHours: 15,
      profileCompletion: 90,
      createdAtEpochMs: now - 5 * day,
    },
    {
      id: 'acc_director_tariq',
      loginId: 'director_tariq',
      displayName: 'Eng. Tariq Zayed',
      email: 'tariq@oasisfin.ae',
      passwordSalt: extraPass.salt,
      passwordHash: extraPass.hash,
      role: 'COMPANY_DIRECTOR',
      companyId: 'comp_oasisfin',
      isActive: true,
      careerLevel: 'Director / Leadership',
      domain: 'Backend & Cloud',
      country: 'UAE',
      points: 0,
      streakDays: 0,
      verifiedCount: 0,
      skillsCsv: 'Payments,Security Audits',
      targetSkillsCsv: '',
      goalsSummary: 'Supervise OasisFin briefs.',
      weeklyHours: 12,
      profileCompletion: 95,
      createdAtEpochMs: now - 5 * day,
    },
  ];

  const companies: CompanyEntity[] = [
    {
      id: 'comp_nilegrid',
      ownerAccountId: 'acc_company_demo',
      name: 'NileGrid Technologies',
      city: 'Cairo',
      domain: 'Data Science & AI',
      verificationStatus: 'VERIFIED',
      verificationTier: 'TRUSTED',
      verificationNotes: 'Commercial registration & escrowed mentorship verified by SkillBridge Trust Team.',
      rewardPolicy: 'Points + verified digital certificate + paid milestone stipend',
      ipPolicy: 'Student retains open-source portfolio showcase rights; challenge telemetry stays sandboxed.',
      verifiedAtEpochMs: now - 20 * day,
      createdAtEpochMs: now - 22 * day,
    },
    {
      id: 'comp_oasisfin',
      ownerAccountId: 'acc_company_oasisfin',
      name: 'OasisFin Cloud',
      city: 'Dubai',
      domain: 'Backend & Cloud',
      verificationStatus: 'PENDING',
      verificationTier: 'BASIC',
      verificationNotes: 'Submitted trade license; awaiting Admin compliance sign-off for paid escrow.',
      rewardPolicy: 'Points + verified experience badge',
      ipPolicy: 'Non-production sandbox schemas only.',
      verifiedAtEpochMs: 0,
      createdAtEpochMs: now - 5 * day,
    },
  ];

  const briefs: ProjectBriefEntity[] = [
    {
      id: 'brief_smart_city',
      companyId: 'comp_nilegrid',
      companyName: 'NileGrid Technologies',
      createdByAccountId: 'acc_company_demo',
      assignedDirectorId: 'acc_director_demo',
      assignedDirectorName: 'Dr. Karim Hassan',
      title: 'Smart City Analytics Dashboard',
      summary: 'Build an analytics dashboard & anomaly alerting pipeline to visualize live municipal mobility and grid telemetry.',
      domain: 'Data Science & AI',
      level: 'Mid-level',
      skillsCsv: 'Data Science,Analytics,AI,Kotlin,SQL',
      rubricCsv: 'Data Accuracy (35%),Anomaly Pipeline Latency (30%),UI Clarity & Accessibility (20%),Unit Tests (15%)',
      deliverablesCsv: 'Git Repository,Architecture Note,Unit Test Report,Understanding Summary',
      deadline: 'Dec 30, 2026',
      pointsReward: 250,
      prizePool: '$600 Stipend Pool',
      approvalStatus: 'ACTIVE',
      adminNotes: 'Approved: clear rubric, non-PII synthetic city telemetry dataset verified.',
      isTeamAllowed: true,
      createdAtEpochMs: now - 14 * day,
      updatedAtEpochMs: now - 2 * day,
    },
    {
      id: 'brief_gateway_ledger',
      companyId: 'comp_nilegrid',
      companyName: 'NileGrid Technologies',
      createdByAccountId: 'acc_company_demo',
      assignedDirectorId: 'acc_director_demo',
      assignedDirectorName: 'Dr. Karim Hassan',
      title: 'Idempotent Grid Settlement API',
      summary: 'Design a fault-tolerant settlement microservice with deterministic transaction IDs and rate limiting.',
      domain: 'Backend & Cloud',
      level: 'Senior',
      skillsCsv: 'Kotlin,Distributed Systems,SQL,REST APIs',
      rubricCsv: 'Idempotency Guarantees (40%),Concurrency Tests (35%),Schema Design (25%)',
      deliverablesCsv: 'Service Code,Concurrency Stress Report,Understanding Summary',
      deadline: 'Jan 18, 2027',
      pointsReward: 350,
      prizePool: '$900 Stipend Pool',
      approvalStatus: 'APPROVED',
      adminNotes: 'Approved for public discovery.',
      isTeamAllowed: true,
      createdAtEpochMs: now - 8 * day,
      updatedAtEpochMs: now - 3 * day,
    },
    {
      id: 'brief_pending_edge',
      companyId: 'comp_nilegrid',
      companyName: 'NileGrid Technologies',
      createdByAccountId: 'acc_company_demo',
      assignedDirectorId: 'acc_director_demo',
      assignedDirectorName: 'Dr. Karim Hassan',
      title: 'Solar Microgrid Forecasting Lab',
      summary: 'Train a lightweight rolling baseline forecaster for regional solar load balancing.',
      domain: 'Data Science & AI',
      level: 'Junior',
      skillsCsv: 'Python,Analytics,SQL',
      rubricCsv: 'MAE Benchmark (50%),Clean Feature Pipeline (30%),Documentation (20%)',
      deliverablesCsv: 'Inference Module,Evaluation Report',
      deadline: 'Feb 10, 2027',
      pointsReward: 180,
      prizePool: '$300 Stipend Pool',
      approvalStatus: 'PENDING_APPROVAL',
      adminNotes: '',
      isTeamAllowed: false,
      createdAtEpochMs: now - 2 * day,
      updatedAtEpochMs: now - 2 * day,
    },
    {
      id: 'brief_completed_telemetry',
      companyId: 'comp_nilegrid',
      companyName: 'NileGrid Technologies',
      createdByAccountId: 'acc_company_demo',
      assignedDirectorId: 'acc_director_demo',
      assignedDirectorName: 'Dr. Karim Hassan',
      title: 'Transit Stream Deduplicator',
      summary: 'Completed cohort brief implementing sliding-window GPS packet deduplication.',
      domain: 'Backend & Cloud',
      level: 'Mid-level',
      skillsCsv: 'Kotlin,SQL,Analytics',
      rubricCsv: 'Throughput (40%),Correctness Tests (40%),Docs (20%)',
      deliverablesCsv: 'Verified Repo,Test Suite',
      deadline: 'Completed',
      pointsReward: 220,
      prizePool: '$450 Stipend Pool',
      approvalStatus: 'COMPLETED',
      adminNotes: 'Cohort completed with 94% pass rate.',
      isTeamAllowed: true,
      createdAtEpochMs: now - 19 * day,
      updatedAtEpochMs: now - 6 * day,
    },
    {
      id: 'brief_oasisfin_vault',
      companyId: 'comp_oasisfin',
      companyName: 'OasisFin Cloud',
      createdByAccountId: 'acc_company_oasisfin',
      assignedDirectorId: 'acc_director_tariq',
      assignedDirectorName: 'Eng. Tariq Zayed',
      title: 'FinTech Ledger Audit Explorer',
      summary: 'Build a read-only cryptographic audit verifier for double-entry ledger snapshots.',
      domain: 'Cybersecurity',
      level: 'Senior',
      skillsCsv: 'Security,Cryptography,Kotlin',
      rubricCsv: 'Verification Accuracy (50%),Threat Model (30%),Tests (20%)',
      deliverablesCsv: 'Verifier Module,Threat Model',
      deadline: 'Jan 25, 2027',
      pointsReward: 300,
      prizePool: '$700 Stipend Pool',
      approvalStatus: 'APPROVED',
      adminNotes: 'Approved sandbox brief.',
      isTeamAllowed: false,
      createdAtEpochMs: now - 4 * day,
      updatedAtEpochMs: now - 4 * day,
    },
  ];

  const applications: ProjectApplicationEntity[] = [
    {
      id: 'app_lina_smart_city',
      briefId: 'brief_smart_city',
      studentAccountId: 'acc_student_demo',
      studentName: 'Lina Mansour',
      studentLevel: 'Mid-level',
      studentSkillsCsv: 'Kotlin,SQL,Python,Analytics',
      coverNote: 'Built municipal KPI telemetry prototypes and SQL aggregation pipelines.',
      roleRequested: 'Data & Visualization Engineer',
      status: 'ACCEPTED',
      decisionNote: "Strong telemetry background; assigned to Dr. Karim's cohort.",
      appliedAtEpochMs: now - 10 * day,
      updatedAtEpochMs: now - 8 * day,
    },
    {
      id: 'app_omar_smart_city',
      briefId: 'brief_smart_city',
      studentAccountId: 'acc_student_omar',
      studentName: 'Omar Khaled',
      studentLevel: 'Senior',
      studentSkillsCsv: 'Kotlin,Distributed Systems,PostgreSQL',
      coverNote: 'Experienced with high-throughput telemetry ingestion.',
      roleRequested: 'Backend Lead',
      status: 'SHORTLISTED',
      decisionNote: 'Shortlisted for Cohort B.',
      appliedAtEpochMs: now - 6 * day,
      updatedAtEpochMs: now - 4 * day,
    },
    {
      id: 'app_sara_gateway',
      briefId: 'brief_gateway_ledger',
      studentAccountId: 'acc_student_sara',
      studentName: 'Sara Nabil',
      studentLevel: 'Junior',
      studentSkillsCsv: 'Python,Unit Testing',
      coverNote: 'Eager to contribute stress test harnesses for idempotency checks.',
      roleRequested: 'QA & Verification',
      status: 'APPLIED',
      decisionNote: '',
      appliedAtEpochMs: now - 2 * day,
      updatedAtEpochMs: now - 2 * day,
    },
    {
      id: 'app_lina_completed',
      briefId: 'brief_completed_telemetry',
      studentAccountId: 'acc_student_demo',
      studentName: 'Lina Mansour',
      studentLevel: 'Mid-level',
      studentSkillsCsv: 'Kotlin,SQL,Analytics',
      coverNote: 'Applying with streaming deduplication experience.',
      roleRequested: 'Core Developer',
      status: 'ACCEPTED',
      decisionNote: 'Accepted into completed cohort.',
      appliedAtEpochMs: now - 18 * day,
      updatedAtEpochMs: now - 17 * day,
    },
  ];

  const tasks: ProjectTaskEntity[] = [
    {
      id: 'task_sc_1',
      briefId: 'brief_smart_city',
      assignedStudentId: 'acc_student_demo',
      assignedStudentName: 'Lina Mansour',
      assignedDirectorId: 'acc_director_demo',
      title: 'Design telemetry anomaly thresholds & rolling average query',
      milestoneOrder: 1,
      dueDateLabel: 'Nov 26',
      isOverdue: false,
      status: 'DONE',
      createdAtEpochMs: now - 8 * day,
      updatedAtEpochMs: now - 5 * day,
    },
    {
      id: 'task_sc_2',
      briefId: 'brief_smart_city',
      assignedStudentId: 'acc_student_demo',
      assignedStudentName: 'Lina Mansour',
      assignedDirectorId: 'acc_director_demo',
      title: 'Implement interactive grid health dashboard & latency tests',
      milestoneOrder: 2,
      dueDateLabel: 'Dec 04',
      isOverdue: false,
      status: 'IN_PROGRESS',
      createdAtEpochMs: now - 5 * day,
      updatedAtEpochMs: now - 1 * day,
    },
    {
      id: 'task_sc_3',
      briefId: 'brief_smart_city',
      assignedStudentId: 'acc_student_demo',
      assignedStudentName: 'Lina Mansour',
      assignedDirectorId: 'acc_director_demo',
      title: 'Submit final Understanding Report & benchmark trace',
      milestoneOrder: 3,
      dueDateLabel: 'Dec 12',
      isOverdue: false,
      status: 'TODO',
      createdAtEpochMs: now - 5 * day,
      updatedAtEpochMs: now - 5 * day,
    },
  ];

  const submissions: WorkSubmissionEntity[] = [
    {
      id: 'sub_lina_completed',
      briefId: 'brief_completed_telemetry',
      briefTitle: 'Transit Stream Deduplicator',
      companyName: 'NileGrid Technologies',
      studentAccountId: 'acc_student_demo',
      studentName: 'Lina Mansour',
      directorAccountId: 'acc_director_demo',
      repoOrDemoUrl: 'https://github.com/lina-mansour/transit-dedup-lab',
      understandingReport:
        'Implemented sliding-window monotonic timestamp filter with deterministic idempotency keys, cutting duplicate telemetry writes by 98.4% in replay benchmarks.',
      skillsAppliedCsv: 'Kotlin,SQL,Analytics',
      status: 'VERIFIED',
      score: 94,
      pointsAwarded: 220,
      feedback: 'Excellent work! Strong analytical skills and clean edge-case test coverage.',
      rejectionReason: '',
      verificationCode: 'SB-VER-9401',
      isPublicOnPassport: true,
      submittedAtEpochMs: now - 9 * day,
      evaluatedAtEpochMs: now - 7 * day,
    },
    {
      id: 'sub_lina_smart_city_pending',
      briefId: 'brief_smart_city',
      briefTitle: 'Smart City Analytics Dashboard',
      companyName: 'NileGrid Technologies',
      studentAccountId: 'acc_student_demo',
      studentName: 'Lina Mansour',
      directorAccountId: 'acc_director_demo',
      repoOrDemoUrl: 'https://github.com/lina-mansour/smart-city-telemetry',
      understandingReport:
        'Built real-time sector grid load aggregator with rolling 15-minute z-score anomaly flagging and accessible KPI cards.',
      skillsAppliedCsv: 'Data Science,Analytics,AI,Kotlin',
      status: 'PENDING_REVIEW',
      score: 0,
      pointsAwarded: 0,
      feedback: '',
      rejectionReason: '',
      verificationCode: 'SB-PEND-7721',
      isPublicOnPassport: true,
      submittedAtEpochMs: now - 1 * day,
      evaluatedAtEpochMs: 0,
    },
    {
      id: 'sub_omar_oasisfin_isolated',
      briefId: 'brief_oasisfin_vault',
      briefTitle: 'FinTech Ledger Audit Explorer',
      companyName: 'OasisFin Cloud',
      studentAccountId: 'acc_student_omar',
      studentName: 'Omar Khaled',
      directorAccountId: 'acc_director_tariq',
      repoOrDemoUrl: 'https://github.com/omar-khaled/ledger-verifier',
      understandingReport: 'Merkle snapshot verification CLI with double-entry invariant assertions.',
      skillsAppliedCsv: 'Security,Cryptography,Kotlin',
      status: 'PENDING_REVIEW',
      score: 0,
      pointsAwarded: 0,
      feedback: '',
      rejectionReason: '',
      verificationCode: 'SB-PEND-3310',
      isPublicOnPassport: true,
      submittedAtEpochMs: now - 2 * day,
      evaluatedAtEpochMs: 0,
    },
  ];

  const notifications: PlatformNotificationEntity[] = [
    {
      id: 'notif_admin_1',
      recipientAccountId: 'acc_admin_demo',
      category: 'APPROVAL',
      titleEn: '1 Company & 1 Brief awaiting approval',
      titleAr: 'شركة ومشروع بانتظار المراجعة',
      bodyEn: 'OasisFin Cloud verification & Solar Microgrid Forecasting Lab need Admin sign-off.',
      bodyAr: 'شركة OasisFin ومشروع مختبر الطاقة الشمسية بانتظار اعتماد المشرف العام.',
      isRead: false,
      createdAtEpochMs: now - 2 * day,
    },
    {
      id: 'notif_student_1',
      recipientAccountId: 'acc_student_demo',
      category: 'EVALUATION',
      titleEn: 'Verified Experience Earned (94%)',
      titleAr: 'تم توثيق خبرتك العملية (94%)',
      bodyEn: 'Dr. Karim Hassan verified Transit Stream Deduplicator (+220 pts).',
      bodyAr: 'قام د. كريم حسن بتوثيق مشروع مزيل تكرار بيانات النقل (+220 نقطة).',
      isRead: false,
      createdAtEpochMs: now - 7 * day,
    },
    {
      id: 'notif_company_1',
      recipientAccountId: 'acc_company_demo',
      category: 'APPLICATION',
      titleEn: 'New applicants on Smart City & Settlement briefs',
      titleAr: 'متقدمون جدد على مشاريعك العملية',
      bodyEn: 'Review shortlisted & new student applications in your Applicants workspace.',
      bodyAr: 'راجع الطلبات الجديدة والمرشحين المختارين في مساحة المتقدمين.',
      isRead: false,
      createdAtEpochMs: now - 2 * day,
    },
    {
      id: 'notif_director_1',
      recipientAccountId: 'acc_director_demo',
      category: 'EVALUATION',
      titleEn: 'Submission awaiting rubric review',
      titleAr: 'تسليم عملي بانتظار التقييم',
      bodyEn: 'Lina Mansour submitted Smart City Analytics Dashboard for evaluation.',
      bodyAr: 'رفعت لينا منصور تسليم لوحة تحليلات المدينة الذكية للمراجعة.',
      isRead: false,
      createdAtEpochMs: now - 1 * day,
    },
  ];

  const activities: PlatformActivityEntity[] = [
    { id: 'act_1', actorAccountId: 'acc_company_demo', actorRole: 'COMPANY', actionType: 'BRIEF_PUBLISHED', summary: 'NileGrid published Smart City Analytics Dashboard', targetId: 'brief_smart_city', createdAtEpochMs: now - 14 * day },
    { id: 'act_2', actorAccountId: 'acc_admin_demo', actorRole: 'PLATFORM_ADMIN', actionType: 'BRIEF_APPROVED', summary: 'Admin approved Smart City Analytics Dashboard', targetId: 'brief_smart_city', createdAtEpochMs: now - 13 * day },
    { id: 'act_3', actorAccountId: 'acc_student_demo', actorRole: 'STUDENT', actionType: 'APPLIED', summary: 'Lina Mansour applied to Smart City Analytics Dashboard', targetId: 'app_lina_smart_city', createdAtEpochMs: now - 10 * day },
    { id: 'act_4', actorAccountId: 'acc_company_demo', actorRole: 'COMPANY', actionType: 'APPLICATION_ACCEPTED', summary: 'NileGrid accepted Lina Mansour into Smart City cohort', targetId: 'app_lina_smart_city', createdAtEpochMs: now - 8 * day },
    { id: 'act_5', actorAccountId: 'acc_director_demo', actorRole: 'COMPANY_DIRECTOR', actionType: 'SUBMISSION_VERIFIED', summary: 'Dr. Karim Hassan verified Transit Stream Deduplicator (94%)', targetId: 'sub_lina_completed', createdAtEpochMs: now - 7 * day },
    { id: 'act_6', actorAccountId: 'acc_company_demo', actorRole: 'COMPANY', actionType: 'BRIEF_CREATED', summary: 'NileGrid submitted Solar Microgrid Forecasting Lab for approval', targetId: 'brief_pending_edge', createdAtEpochMs: now - 2 * day },
    { id: 'act_7', actorAccountId: 'acc_student_demo', actorRole: 'STUDENT', actionType: 'WORK_SUBMITTED', summary: 'Lina Mansour submitted Smart City Analytics Dashboard', targetId: 'sub_lina_smart_city_pending', createdAtEpochMs: now - 1 * day },
  ];

  const reports: ModerationReportEntity[] = [
    {
      id: 'rep_seed_1',
      reporterAccountId: 'acc_student_sara',
      targetType: 'BRIEF',
      targetId: 'brief_oasisfin_vault',
      reason: 'Clarify sandbox test vector license before paid cohort starts.',
      status: 'OPEN',
      resolutionNote: '',
      createdAtEpochMs: now - 2 * day,
    },
  ];

  return {
    revision: 1,
    updatedAtEpochMs: now,
    accounts,
    companies,
    briefs,
    applications,
    tasks,
    submissions,
    notifications,
    activities,
    reports,
    deviceSessions: [],
  };
}

export class CentralEcosystemStore {
  private readonly storeFile: string;
  private state: CentralEcosystemState;
  private readonly sessionSecret: string;
  private readonly listeners: Set<(event: { revision: number; kind: string; summary: string }) => void>;

  constructor(dataDir: string) {
    this.storeFile = path.join(dataDir, 'skillbridge-central.json');
    this.sessionSecret = process.env.SESSION_SECRET || crypto.randomBytes(32).toString('hex');
    this.listeners = new Set();
    if (!fs.existsSync(dataDir)) {
      fs.mkdirSync(dataDir, { recursive: true });
    }
    if (fs.existsSync(this.storeFile)) {
      try {
        const parsed = JSON.parse(fs.readFileSync(this.storeFile, 'utf8')) as CentralEcosystemState;
        if (parsed && Array.isArray(parsed.accounts) && parsed.accounts.length > 0) {
          this.state = parsed;
        } else {
          this.state = createSeedState();
          this.persist();
        }
      } catch {
        this.state = createSeedState();
        this.persist();
      }
    } else {
      this.state = createSeedState();
      this.persist();
    }
  }

  private persist() {
    const tmpFile = `${this.storeFile}.tmp`;
    fs.writeFileSync(tmpFile, JSON.stringify(this.state, null, 2), 'utf8');
    fs.renameSync(tmpFile, this.storeFile);
  }

  private bumpRevision(kind: string, summary: string) {
    this.state.revision += 1;
    this.state.updatedAtEpochMs = Date.now();
    this.persist();
    const payload = { revision: this.state.revision, kind, summary };
    for (const listener of this.listeners) {
      try {
        listener(payload);
      } catch {
        // ignore disconnected subscriber
      }
    }
  }

  subscribe(listener: (event: { revision: number; kind: string; summary: string }) => void): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  issueToken(principal: SessionPrincipal): string {
    const payloadObj = {
      accountId: principal.accountId,
      role: principal.role,
      exp: Date.now() + 7 * 86_400_000,
    };
    const encoded = Buffer.from(JSON.stringify(payloadObj)).toString('base64url');
    const sig = crypto.createHmac('sha256', this.sessionSecret).update(encoded).digest('base64url');
    return `${encoded}.${sig}`;
  }

  verifyToken(token: string): SessionPrincipal {
    const parts = token.split('.');
    if (parts.length !== 2) throw new BackendHttpError(401, 'Invalid authorization token.');
    const [encoded, sig] = parts;
    const expectedSig = crypto.createHmac('sha256', this.sessionSecret).update(encoded).digest('base64url');
    const a = Buffer.from(sig);
    const b = Buffer.from(expectedSig);
    if (a.length !== b.length || !crypto.timingSafeEqual(a, b)) {
      throw new BackendHttpError(401, 'Token signature verification failed.');
    }
    const payload = JSON.parse(Buffer.from(encoded, 'base64url').toString('utf8')) as {
      accountId: string;
      exp: number;
    };
    if (!payload.accountId || payload.exp < Date.now()) {
      throw new BackendHttpError(401, 'Session expired. Please sign in again.');
    }
    const account = this.state.accounts.find((acc) => acc.id === payload.accountId);
    if (!account) throw new BackendHttpError(401, 'Account no longer exists.');
    if (!account.isActive) throw new BackendHttpError(403, 'This account has been suspended by Platform Admin.');
    return toPrincipal(account, account.loginId.startsWith('google_'));
  }

  authenticateWithCredentials(loginId: string, password: string): { token: string; principal: SessionPrincipal } {
    const cleanLogin = (loginId || '').trim();
    if (!cleanLogin || !password) {
      throw new BackendHttpError(400, 'Login ID and password are required.');
    }
    const account = this.state.accounts.find(
      (a) => a.loginId.toLowerCase() === cleanLogin.toLowerCase() || a.email.toLowerCase() === cleanLogin.toLowerCase(),
    );
    if (!account || !verifyPassword(password, account.passwordSalt, account.passwordHash)) {
      throw new BackendHttpError(401, 'Invalid Login ID or password.');
    }
    if (!account.isActive) {
      throw new BackendHttpError(403, 'This account has been suspended by Platform Admin.');
    }
    const principal = toPrincipal(account, false);
    this.appendActivity(account.id, principal.role, 'AUTH_LOGIN', `${account.displayName} (${principal.role}) signed in.`, account.id);
    this.bumpRevision('auth:login', `${account.displayName} signed in`);
    return { token: this.issueToken(principal), principal };
  }

  registerStudentAccount(input: {
    loginId: string;
    displayName: string;
    email: string;
    password: string;
  }): { token: string; principal: SessionPrincipal } {
    const cleanLogin = (input.loginId || '').trim().toLowerCase().replace(/[^a-z0-9_.-]/g, '');
    const cleanName = (input.displayName || '').trim();
    if (cleanLogin.length < 3) throw new BackendHttpError(400, 'Login ID must be at least 3 characters.');
    if (cleanName.length < 2) throw new BackendHttpError(400, 'Display name is required.');
    if (!input.password || input.password.length < 6) throw new BackendHttpError(400, 'Password must be at least 6 characters.');
    if (this.state.accounts.some((a) => a.loginId.toLowerCase() === cleanLogin)) {
      throw new BackendHttpError(409, `Login ID '${cleanLogin}' is already taken.`);
    }
    const { salt, hash } = hashPassword(input.password);
    const now = Date.now();
    const account: AccountEntity = {
      id: `acc_${crypto.randomUUID().slice(0, 12)}`,
      loginId: cleanLogin,
      displayName: cleanName,
      email: (input.email || '').trim() || `${cleanLogin}@skillbridge.io`,
      passwordSalt: salt,
      passwordHash: hash,
      role: 'STUDENT',
      companyId: '',
      isActive: true,
      careerLevel: 'Junior',
      domain: 'Backend & Cloud',
      country: 'Egypt',
      points: 0,
      streakDays: 0,
      verifiedCount: 0,
      skillsCsv: '',
      targetSkillsCsv: '',
      goalsSummary: '',
      weeklyHours: 8,
      profileCompletion: 35,
      createdAtEpochMs: now,
    };
    this.state.accounts.push(account);
    this.appendActivity(account.id, 'STUDENT', 'USER_REGISTERED', `New student account registered: ${cleanName}`, account.id);
    this.bumpRevision('auth:register', `Registered student ${cleanName}`);
    const principal = toPrincipal(account, false);
    return { token: this.issueToken(principal), principal };
  }

  resolveGoogleStudentSession(input: {
    firebaseUid: string;
    displayName: string;
    email: string;
  }): { token: string; principal: SessionPrincipal } {
    const uid = (input.firebaseUid || '').trim();
    const cleanEmail = (input.email || '').trim().toLowerCase();
    if (!uid && !cleanEmail) throw new BackendHttpError(400, 'Firebase UID or email is required.');
    const existing = this.state.accounts.find(
      (a) => (uid && a.id === uid) || (cleanEmail && a.email.trim().toLowerCase() === cleanEmail),
    );
    if (existing) {
      if (!existing.isActive) {
        throw new BackendHttpError(403, 'Account suspended by Platform Admin.');
      }
      if (input.displayName?.trim() && existing.displayName === 'SkillBridge Learner') {
        existing.displayName = input.displayName.trim();
        this.persist();
      }
      const principal = toPrincipal(existing, true);
      return { token: this.issueToken(principal), principal };
    }
    const { salt, hash } = hashPassword(crypto.randomUUID());
    const effectiveUid = uid || `uid_${crypto.randomUUID().slice(0, 12)}`;
    const cleanName = (input.displayName || '').trim() || (input.email || '').split('@')[0] || 'SkillBridge Learner';
    const account: AccountEntity = {
      id: effectiveUid,
      loginId: `google_${effectiveUid.slice(0, 10)}`,
      displayName: cleanName,
      email: (input.email || '').trim(),
      passwordSalt: salt,
      passwordHash: hash,
      role: 'STUDENT',
      companyId: '',
      isActive: true,
      careerLevel: 'Junior',
      domain: 'Backend & Cloud',
      country: 'Egypt',
      points: 0,
      streakDays: 0,
      verifiedCount: 0,
      skillsCsv: 'Kotlin,SQL',
      targetSkillsCsv: 'Distributed Systems,React TypeScript',
      goalsSummary: '',
      weeklyHours: 10,
      profileCompletion: 50,
      createdAtEpochMs: Date.now(),
    };
    this.state.accounts.push(account);
    this.appendActivity(account.id, 'STUDENT', 'GOOGLE_LINKED', `Student linked via Google Sign-In: ${cleanName}`, account.id);
    this.bumpRevision('auth:google', `${cleanName} linked Google session`);
    const principal = toPrincipal(account, true);
    return { token: this.issueToken(principal), principal };
  }

  getSnapshotForSession(session: SessionPrincipal) {
    const ownAccount = this.state.accounts.find((a) => a.id === session.accountId);
    const safeAccounts = this.state.accounts.map(sanitizeAccountForClient);
    const ownNotifications = this.state.notifications
      .filter((n) => n.recipientAccountId === session.accountId)
      .sort((a, b) => b.createdAtEpochMs - a.createdAtEpochMs);

    const ownSessions = this.state.deviceSessions
      .filter((s) => s.accountId === session.accountId)
      .sort((a, b) => b.lastActiveEpochMs - a.lastActiveEpochMs);

    let visibleBriefs = this.state.briefs;
    let visibleApplications = this.state.applications;
    let visibleTasks = this.state.tasks;
    let visibleSubmissions = this.state.submissions;
    let visibleReports = this.state.reports;
    let visibleActivities = this.state.activities;

    if (session.role === 'STUDENT') {
      visibleBriefs = this.state.briefs.filter((b) =>
        ['APPROVED', 'ACTIVE', 'COMPLETED'].includes(b.approvalStatus),
      );
      visibleApplications = this.state.applications.filter((a) => a.studentAccountId === session.accountId);
      visibleTasks = this.state.tasks.filter((t) => t.assignedStudentId === session.accountId);
      visibleSubmissions = this.state.submissions.filter((s) => s.studentAccountId === session.accountId);
      visibleReports = this.state.reports.filter((r) => r.reporterAccountId === session.accountId);
      visibleActivities = this.state.activities.slice(0, 20);
    } else if (session.role === 'COMPANY') {
      visibleBriefs = this.state.briefs.filter((b) => b.companyId === session.companyId);
      const ownedIds = new Set(visibleBriefs.map((b) => b.id));
      visibleApplications = this.state.applications.filter((a) => ownedIds.has(a.briefId));
      visibleTasks = this.state.tasks.filter((t) => ownedIds.has(t.briefId));
      visibleSubmissions = this.state.submissions.filter((s) => ownedIds.has(s.briefId));
      visibleReports = this.state.reports.filter((r) => r.reporterAccountId === session.accountId);
      visibleActivities = this.state.activities.filter(
        (act) => act.actorAccountId === session.accountId || ownedIds.has(act.targetId),
      );
    } else if (session.role === 'COMPANY_DIRECTOR') {
      visibleBriefs = this.state.briefs.filter((b) => b.assignedDirectorId === session.accountId);
      const assignedIds = new Set(visibleBriefs.map((b) => b.id));
      visibleApplications = this.state.applications.filter((a) => assignedIds.has(a.briefId));
      visibleTasks = this.state.tasks.filter((t) => t.assignedDirectorId === session.accountId);
      visibleSubmissions = this.state.submissions.filter((s) => s.directorAccountId === session.accountId);
      visibleReports = this.state.reports.filter((r) => r.reporterAccountId === session.accountId);
      visibleActivities = this.state.activities.filter(
        (act) => act.actorAccountId === session.accountId || assignedIds.has(act.targetId),
      );
    }

    return {
      revision: this.state.revision,
      updatedAtEpochMs: this.state.updatedAtEpochMs,
      account: ownAccount ? sanitizeAccountForClient(ownAccount) : null,
      accounts: safeAccounts,
      companies: this.state.companies,
      briefs: visibleBriefs,
      applications: visibleApplications,
      tasks: visibleTasks,
      submissions: visibleSubmissions,
      notifications: ownNotifications,
      activities: visibleActivities.slice().sort((a, b) => b.createdAtEpochMs - a.createdAtEpochMs),
      reports: visibleReports.slice().sort((a, b) => b.createdAtEpochMs - a.createdAtEpochMs),
      deviceSessions: ownSessions,
    };
  }

  getPublicCatalogSummary() {
    const publishedBriefs = this.state.briefs.filter((b) =>
      ['APPROVED', 'ACTIVE', 'COMPLETED'].includes(b.approvalStatus),
    );
    return {
      revision: this.state.revision,
      updatedAtEpochMs: this.state.updatedAtEpochMs,
      companies: this.state.companies,
      publishedBriefs,
    };
  }

  private requireRole(session: SessionPrincipal, expected: AccountRole) {
    if (session.role !== expected) {
      throw new BackendHttpError(403, `Forbidden: requires ${expected} role (current: ${session.role}).`);
    }
  }

  private pushNotification(
    recipientAccountId: string,
    category: string,
    titleEn: string,
    titleAr: string,
    bodyEn: string,
    bodyAr: string,
  ) {
    if (!recipientAccountId) return;
    this.state.notifications.unshift({
      id: `notif_${crypto.randomUUID().slice(0, 10)}`,
      recipientAccountId,
      category,
      titleEn,
      titleAr,
      bodyEn,
      bodyAr,
      isRead: false,
      createdAtEpochMs: Date.now(),
    });
  }

  private notifyAdmins(category: string, titleEn: string, titleAr: string, bodyEn: string, bodyAr: string) {
    for (const admin of this.state.accounts.filter((a) => a.role === 'PLATFORM_ADMIN')) {
      this.pushNotification(admin.id, category, titleEn, titleAr, bodyEn, bodyAr);
    }
  }

  private appendActivity(actorAccountId: string, actorRole: string, actionType: string, summary: string, targetId: string) {
    this.state.activities.unshift({
      id: `act_${crypto.randomUUID().slice(0, 10)}`,
      actorAccountId,
      actorRole,
      actionType,
      summary: summary.slice(0, 280),
      targetId,
      createdAtEpochMs: Date.now(),
    });
  }

  registerDeviceSession(
    session: SessionPrincipal,
    input: { sessionId: string; deviceType: 'MOBILE' | 'DESKTOP_LAPTOP'; deviceLabel: string; windowWidthDp: number },
  ): DeviceSessionEntity {
    const safeType = input.deviceType === 'DESKTOP_LAPTOP' ? 'DESKTOP_LAPTOP' : 'MOBILE';
    const existing = this.state.deviceSessions.find((d) => d.id === input.sessionId);
    const entity: DeviceSessionEntity = {
      id: input.sessionId,
      accountId: session.accountId,
      deviceType: safeType,
      deviceLabel: (input.deviceLabel || 'SkillBridge Client').slice(0, 80),
      windowWidthDp: Number(input.windowWidthDp) || 1280,
      lastActiveEpochMs: Date.now(),
      revoked: existing?.revoked ?? false,
    };
    if (existing) {
      Object.assign(existing, entity);
    } else {
      this.state.deviceSessions.unshift(entity);
    }
    this.bumpRevision('device:heartbeat', `Synced ${entity.deviceLabel}`);
    return entity;
  }

  revokeDeviceSession(session: SessionPrincipal, targetSessionId: string) {
    const target = this.state.deviceSessions.find((d) => d.id === targetSessionId);
    if (!target) throw new BackendHttpError(404, 'Device session not found.');
    if (target.accountId !== session.accountId) {
      throw new BackendHttpError(403, 'Cannot revoke a session belonging to another account.');
    }
    target.revoked = true;
    target.lastActiveEpochMs = Date.now();
    this.bumpRevision('device:revoke', `Revoked ${target.deviceLabel}`);
  }

  createProjectBrief(
    session: SessionPrincipal,
    input: {
      title: string;
      summary: string;
      domain: string;
      level: string;
      skills: string[];
      rubric: string[];
      deliverables: string[];
      deadline: string;
      pointsReward: number;
      prizePool: string;
      assignedDirectorId: string;
      isTeamAllowed: boolean;
    },
  ): ProjectBriefEntity {
    this.requireRole(session, 'COMPANY');
    const company = this.state.companies.find((c) => c.id === session.companyId);
    if (!company) throw new BackendHttpError(403, 'No company entity linked to this account.');
    if ((input.title || '').trim().length < 4) throw new BackendHttpError(400, 'Project brief title must be at least 4 characters.');
    if ((input.summary || '').trim().length < 10) throw new BackendHttpError(400, 'Please provide a clear project brief summary.');

    let resolvedDirectorId = '';
    let resolvedDirectorName = '';
    if (input.assignedDirectorId?.trim()) {
      const director = this.state.accounts.find((a) => a.id === input.assignedDirectorId.trim());
      if (!director) throw new BackendHttpError(400, 'Selected director does not exist.');
      if (director.role !== 'COMPANY_DIRECTOR' || director.companyId !== company.id) {
        throw new BackendHttpError(403, `Director must belong to ${company.name}.`);
      }
      resolvedDirectorId = director.id;
      resolvedDirectorName = director.displayName;
    }

    const now = Date.now();
    const brief: ProjectBriefEntity = {
      id: `brief_${crypto.randomUUID().slice(0, 10)}`,
      companyId: company.id,
      companyName: company.name,
      createdByAccountId: session.accountId,
      assignedDirectorId: resolvedDirectorId,
      assignedDirectorName: resolvedDirectorName,
      title: input.title.trim().slice(0, 140),
      summary: input.summary.trim().slice(0, 1500),
      domain: (input.domain || company.domain).trim(),
      level: ['Junior', 'Mid-level', 'Senior', 'Director / Leadership'].includes(input.level) ? input.level : 'Mid-level',
      skillsCsv: (input.skills || []).map((s) => s.trim()).filter(Boolean).join(',') || 'Architecture,Testing',
      rubricCsv: (input.rubric || []).map((s) => s.trim()).filter(Boolean).join(',') || 'Architecture (40%),Tests (30%),Docs (30%)',
      deliverablesCsv: (input.deliverables || []).map((s) => s.trim()).filter(Boolean).join(',') || 'Git Repo,Understanding Summary',
      deadline: (input.deadline || '').trim() || 'Mar 15, 2027',
      pointsReward: Math.max(50, Math.min(2000, Number(input.pointsReward) || 250)),
      prizePool: (input.prizePool || '').trim() || 'Points + Certificate',
      approvalStatus: 'PENDING_APPROVAL',
      adminNotes: '',
      isTeamAllowed: Boolean(input.isTeamAllowed),
      createdAtEpochMs: now,
      updatedAtEpochMs: now,
    };

    this.state.briefs.unshift(brief);
    this.appendActivity(session.accountId, session.role, 'BRIEF_CREATED', `${company.name} created brief '${brief.title}'`, brief.id);
    this.notifyAdmins(
      'APPROVAL',
      'New Brief Pending Approval',
      'مشروع جديد بانتظار الاعتماد',
      `${company.name} submitted '${brief.title}' for Admin review.`,
      `قدّمت شركة ${company.name} مشروع '${brief.title}' لاعتماده.`,
    );
    this.bumpRevision('brief:created', brief.title);
    return brief;
  }

  assignDirectorToBrief(session: SessionPrincipal, briefId: string, directorAccountId: string, expectedUpdatedAtEpochMs?: number) {
    this.requireRole(session, 'COMPANY');
    const brief = this.state.briefs.find((b) => b.id === briefId);
    if (!brief) throw new BackendHttpError(404, 'Brief not found.');
    if (brief.companyId !== session.companyId) {
      throw new BackendHttpError(403, 'You can only manage briefs belonging to your company.');
    }
    if (expectedUpdatedAtEpochMs && brief.updatedAtEpochMs !== expectedUpdatedAtEpochMs) {
      throw new BackendHttpError(409, 'Conflict: this brief was modified on another device. Synced latest state.');
    }
    const director = this.state.accounts.find((a) => a.id === directorAccountId);
    if (!director) throw new BackendHttpError(404, 'Director account not found.');
    if (director.role !== 'COMPANY_DIRECTOR' || director.companyId !== session.companyId) {
      throw new BackendHttpError(403, 'Director does not belong to your company.');
    }
    brief.assignedDirectorId = director.id;
    brief.assignedDirectorName = director.displayName;
    brief.updatedAtEpochMs = Date.now();
    this.pushNotification(
      director.id,
      'TASK',
      'Assigned to Project Brief',
      'تم تعيينك مشرفًا على مشروع',
      `You are now supervising '${brief.title}' for ${brief.companyName}.`,
      `تم تعيينك للإشراف على مشروع '${brief.title}'.`,
    );
    this.appendActivity(session.accountId, session.role, 'DIRECTOR_ASSIGNED', `Assigned ${director.displayName} to '${brief.title}'`, briefId);
    this.bumpRevision('brief:director_assigned', brief.title);
  }

  reviewCompanyVerification(
    session: SessionPrincipal,
    companyId: string,
    approve: boolean,
    tier: CompanyEntity['verificationTier'],
    notes: string,
  ) {
    this.requireRole(session, 'PLATFORM_ADMIN');
    const company = this.state.companies.find((c) => c.id === companyId);
    if (!company) throw new BackendHttpError(404, 'Company not found.');
    const status = approve ? 'VERIFIED' : 'REJECTED';
    const nextTier: CompanyEntity['verificationTier'] = approve
      ? (['BASIC', 'BUSINESS', 'TRUSTED'].includes(tier) ? tier : 'BUSINESS')
      : 'UNVERIFIED';
    company.verificationStatus = status;
    company.verificationTier = nextTier;
    company.verificationNotes =
      notes.trim() || (approve ? 'Verified by SkillBridge Platform Admin.' : 'Verification rejected; resubmit compliance docs.');
    company.verifiedAtEpochMs = approve ? Date.now() : 0;

    this.pushNotification(
      company.ownerAccountId,
      'APPROVAL',
      approve ? `Company Verified (${nextTier})` : 'Company Verification Rejected',
      approve ? `تم توثيق الشركة (${nextTier})` : 'تم رفض طلب توثيق الشركة',
      notes.trim() || `Status updated to ${status} by Platform Admin.`,
      notes.trim() || `تم تحديث حالة التوثيق إلى ${status}.`,
    );
    this.appendActivity(session.accountId, session.role, `COMPANY_${status}`, `Admin set ${company.name} verification to ${status} (${nextTier})`, companyId);
    this.bumpRevision('company:reviewed', `${company.name}: ${status}`);
  }

  reviewProjectBrief(
    session: SessionPrincipal,
    briefId: string,
    approve: boolean,
    adminNotes: string,
    expectedUpdatedAtEpochMs?: number,
  ) {
    this.requireRole(session, 'PLATFORM_ADMIN');
    const brief = this.state.briefs.find((b) => b.id === briefId);
    if (!brief) throw new BackendHttpError(404, 'Project brief not found.');
    if (expectedUpdatedAtEpochMs && brief.updatedAtEpochMs !== expectedUpdatedAtEpochMs) {
      throw new BackendHttpError(409, 'Conflict: brief was updated on another device. Refreshing.');
    }
    const nextStatus = approve ? 'APPROVED' : 'REJECTED';
    brief.approvalStatus = nextStatus;
    brief.adminNotes =
      adminNotes.trim() || (approve ? 'Approved by Platform Admin.' : 'Rejected: tighten rubric and deliverables.');
    brief.updatedAtEpochMs = Date.now();

    this.pushNotification(
      brief.createdByAccountId,
      'APPROVAL',
      approve ? 'Brief Approved & Published' : 'Brief Needs Revision',
      approve ? 'تم اعتماد ونشر المشروع' : 'تم رفض مسودة المشروع',
      `'${brief.title}' is now ${nextStatus}.`,
      `أصبحت حالة مشروع '${brief.title}' الآن ${nextStatus}.`,
    );
    if (approve && brief.assignedDirectorId) {
      this.pushNotification(
        brief.assignedDirectorId,
        'APPROVAL',
        'Supervised Brief Approved',
        'تم اعتماد مشروع تحت إشرافك',
        `'${brief.title}' is live for student applications.`,
        `مشروع '${brief.title}' متاح الآن لتقديم الطلاب.`,
      );
    }
    this.appendActivity(session.accountId, session.role, `BRIEF_${nextStatus}`, `Admin marked '${brief.title}' as ${nextStatus}`, briefId);
    this.bumpRevision('brief:reviewed', `${brief.title}: ${nextStatus}`);
  }

  setUserActiveStatus(session: SessionPrincipal, targetAccountId: string, active: boolean) {
    this.requireRole(session, 'PLATFORM_ADMIN');
    if (targetAccountId === session.accountId) {
      throw new BackendHttpError(400, 'Cannot suspend your own active admin session.');
    }
    const account = this.state.accounts.find((a) => a.id === targetAccountId);
    if (!account) throw new BackendHttpError(404, 'User not found.');
    account.isActive = active;
    this.appendActivity(
      session.accountId,
      session.role,
      active ? 'USER_ACTIVATED' : 'USER_SUSPENDED',
      `Admin ${active ? 'activated' : 'suspended'} ${account.loginId}`,
      targetAccountId,
    );
    this.bumpRevision('user:status', `${account.loginId}: ${active ? 'active' : 'suspended'}`);
  }

  resolveModerationReport(session: SessionPrincipal, reportId: string, status: 'RESOLVED' | 'DISMISSED', resolutionNote: string) {
    this.requireRole(session, 'PLATFORM_ADMIN');
    const report = this.state.reports.find((r) => r.id === reportId);
    if (!report) throw new BackendHttpError(404, 'Report not found.');
    const safeStatus = status === 'DISMISSED' ? 'DISMISSED' : 'RESOLVED';
    report.status = safeStatus;
    report.resolutionNote = resolutionNote.trim() || 'Reviewed by Platform Admin.';
    this.appendActivity(session.accountId, session.role, `REPORT_${safeStatus}`, `Moderation report ${reportId} marked ${safeStatus}`, reportId);
    this.bumpRevision('report:resolved', `${reportId}: ${safeStatus}`);
  }

  applyToProjectBrief(session: SessionPrincipal, briefId: string, coverNote: string, roleRequested: string): ProjectApplicationEntity {
    this.requireRole(session, 'STUDENT');
    const brief = this.state.briefs.find((b) => b.id === briefId);
    if (!brief) throw new BackendHttpError(404, 'Project brief not found.');
    if (!['APPROVED', 'ACTIVE'].includes(brief.approvalStatus)) {
      throw new BackendHttpError(403, 'Cannot apply to a brief that is not approved/active.');
    }
    const duplicate = this.state.applications.find((a) => a.briefId === briefId && a.studentAccountId === session.accountId);
    if (duplicate) {
      throw new BackendHttpError(409, `You already applied to '${brief.title}'.`);
    }
    const student = this.state.accounts.find((a) => a.id === session.accountId);
    if (!student) throw new BackendHttpError(404, 'Student profile missing.');
    const now = Date.now();
    const app: ProjectApplicationEntity = {
      id: `app_${crypto.randomUUID().slice(0, 10)}`,
      briefId,
      studentAccountId: student.id,
      studentName: student.displayName,
      studentLevel: student.careerLevel,
      studentSkillsCsv: student.skillsCsv,
      coverNote: (coverNote || '').trim().slice(0, 600) || 'Ready to execute deliverables and verify skills.',
      roleRequested: (roleRequested || '').trim().slice(0, 80) || 'Project Builder',
      status: 'APPLIED',
      decisionNote: '',
      appliedAtEpochMs: now,
      updatedAtEpochMs: now,
    };
    this.state.applications.unshift(app);
    this.pushNotification(
      brief.createdByAccountId,
      'APPLICATION',
      `New Application: ${student.displayName}`,
      `طلب التحاق جديد: ${student.displayName}`,
      `Applied for '${brief.title}' (${app.roleRequested}).`,
      `تقدّم إلى مشروع '${brief.title}' (${app.roleRequested}).`,
    );
    this.appendActivity(session.accountId, session.role, 'APPLIED', `${student.displayName} applied to '${brief.title}'`, app.id);
    this.bumpRevision('application:created', `${student.displayName} -> ${brief.title}`);
    return app;
  }

  decideApplication(
    session: SessionPrincipal,
    applicationId: string,
    newStatus: 'SHORTLISTED' | 'ACCEPTED' | 'REJECTED',
    decisionNote: string,
    expectedUpdatedAtEpochMs?: number,
  ) {
    this.requireRole(session, 'COMPANY');
    if (!['SHORTLISTED', 'ACCEPTED', 'REJECTED'].includes(newStatus)) {
      throw new BackendHttpError(400, `Invalid application status: ${newStatus}`);
    }
    const app = this.state.applications.find((a) => a.id === applicationId);
    if (!app) throw new BackendHttpError(404, 'Application not found.');
    if (expectedUpdatedAtEpochMs && app.updatedAtEpochMs !== expectedUpdatedAtEpochMs) {
      throw new BackendHttpError(409, 'Conflict: application was updated concurrently on another device.');
    }
    const brief = this.state.briefs.find((b) => b.id === app.briefId);
    if (!brief) throw new BackendHttpError(404, 'Associated brief not found.');
    if (brief.companyId !== session.companyId) {
      throw new BackendHttpError(403, "You cannot review applications for another company's brief.");
    }
    const now = Date.now();
    app.status = newStatus;
    app.decisionNote = decisionNote.trim();
    app.updatedAtEpochMs = now;

    if (newStatus === 'ACCEPTED') {
      if (brief.approvalStatus === 'APPROVED') {
        brief.approvalStatus = 'ACTIVE';
        brief.updatedAtEpochMs = now;
      }
      const kickoffTask: ProjectTaskEntity = {
        id: `task_${crypto.randomUUID().slice(0, 10)}`,
        briefId: brief.id,
        assignedStudentId: app.studentAccountId,
        assignedStudentName: app.studentName,
        assignedDirectorId: brief.assignedDirectorId,
        title: `Kickoff milestone & architecture plan for ${brief.title}`,
        milestoneOrder: 1,
        dueDateLabel: brief.deadline,
        isOverdue: false,
        status: 'TODO',
        createdAtEpochMs: now,
        updatedAtEpochMs: now,
      };
      this.state.tasks.unshift(kickoffTask);
      if (brief.assignedDirectorId) {
        this.pushNotification(
          brief.assignedDirectorId,
          'TASK',
          'Student Accepted to Supervised Brief',
          'تم قبول طالب في مشروع تحت إشرافك',
          `${app.studentName} joined '${brief.title}'.`,
          `انضم ${app.studentName} إلى مشروع '${brief.title}'.`,
        );
      }
    }

    this.pushNotification(
      app.studentAccountId,
      'APPLICATION',
      `Application ${newStatus}: ${brief.title}`,
      `تحديث طلبك في ${brief.title}: ${newStatus}`,
      decisionNote.trim() || `Company updated your application status to ${newStatus}.`,
      decisionNote.trim() || `قامت الشركة بتحديث حالة طلبك إلى ${newStatus}.`,
    );
    this.appendActivity(
      session.accountId,
      session.role,
      `APPLICATION_${newStatus}`,
      `${brief.companyName} marked ${app.studentName} as ${newStatus} for '${brief.title}'`,
      applicationId,
    );
    this.bumpRevision('application:decided', `${app.studentName}: ${newStatus}`);
  }

  submitProjectWork(
    session: SessionPrincipal,
    input: { briefId: string; repoOrDemoUrl: string; understandingReport: string; skillsApplied: string[] },
  ): WorkSubmissionEntity {
    this.requireRole(session, 'STUDENT');
    const brief = this.state.briefs.find((b) => b.id === input.briefId);
    if (!brief) throw new BackendHttpError(404, 'Project brief not found.');
    const app = this.state.applications.find((a) => a.briefId === input.briefId && a.studentAccountId === session.accountId);
    if (!app || app.status !== 'ACCEPTED') {
      throw new BackendHttpError(403, `You must be ACCEPTED into '${brief.title}' before submitting deliverables.`);
    }
    const report = (input.understandingReport || '').trim();
    const repoUrl = (input.repoOrDemoUrl || '').trim();
    if (report.length < 15) {
      throw new BackendHttpError(400, 'Please include an Understanding Report (at least 15 characters).');
    }
    if (containsSecretLeak(`${repoUrl}\n${report}`)) {
      throw new BackendHttpError(400, 'Secret/API key detected in submission. Remove credentials before submitting.');
    }
    const subId = `sub_${crypto.randomUUID().slice(0, 10)}`;
    const submission: WorkSubmissionEntity = {
      id: subId,
      briefId: brief.id,
      briefTitle: brief.title,
      companyName: brief.companyName,
      studentAccountId: session.accountId,
      studentName: session.displayName,
      directorAccountId: brief.assignedDirectorId,
      repoOrDemoUrl: repoUrl.slice(0, 300),
      understandingReport: report.slice(0, 2500),
      skillsAppliedCsv:
        (input.skillsApplied || []).map((s) => s.trim()).filter(Boolean).join(',') || brief.skillsCsv,
      status: 'PENDING_REVIEW',
      score: 0,
      pointsAwarded: 0,
      feedback: '',
      rejectionReason: '',
      verificationCode: `SB-PEND-${subId.slice(-4).toUpperCase()}`,
      isPublicOnPassport: true,
      submittedAtEpochMs: Date.now(),
      evaluatedAtEpochMs: 0,
    };
    this.state.submissions.unshift(submission);
    if (brief.assignedDirectorId) {
      this.pushNotification(
        brief.assignedDirectorId,
        'EVALUATION',
        'New Submission Ready for Review',
        'تسليم جديد بانتظار التقييم',
        `${session.displayName} submitted work for '${brief.title}'.`,
        `رفع ${session.displayName} تسليمًا لمشروع '${brief.title}'.`,
      );
    }
    this.appendActivity(session.accountId, session.role, 'WORK_SUBMITTED', `${session.displayName} submitted deliverable for '${brief.title}'`, subId);
    this.bumpRevision('submission:created', `${session.displayName} -> ${brief.title}`);
    return submission;
  }

  updateStudentTaskStatus(session: SessionPrincipal, taskId: string, newStatus: 'TODO' | 'IN_PROGRESS' | 'DONE', expectedUpdatedAtEpochMs?: number) {
    this.requireRole(session, 'STUDENT');
    if (!['TODO', 'IN_PROGRESS', 'DONE'].includes(newStatus)) {
      throw new BackendHttpError(400, 'Invalid task status.');
    }
    const task = this.state.tasks.find((t) => t.id === taskId);
    if (!task) throw new BackendHttpError(404, 'Task not found.');
    if (task.assignedStudentId !== session.accountId) {
      throw new BackendHttpError(403, 'You can only update your own assigned project tasks.');
    }
    if (expectedUpdatedAtEpochMs && task.updatedAtEpochMs !== expectedUpdatedAtEpochMs) {
      throw new BackendHttpError(409, 'Conflict: task updated on another device. Synced latest.');
    }
    task.status = newStatus;
    task.updatedAtEpochMs = Date.now();
    this.bumpRevision('task:updated', `${task.title}: ${newStatus}`);
  }

  createProjectTask(
    session: SessionPrincipal,
    input: { briefId: string; studentAccountId: string; title: string; dueDateLabel: string },
  ): ProjectTaskEntity {
    this.requireRole(session, 'COMPANY_DIRECTOR');
    const brief = this.state.briefs.find((b) => b.id === input.briefId);
    if (!brief) throw new BackendHttpError(404, 'Project brief not found.');
    if (brief.assignedDirectorId !== session.accountId) {
      throw new BackendHttpError(403, `Access denied: you are not the assigned Director for '${brief.title}'.`);
    }
    const student = this.state.accounts.find((a) => a.id === input.studentAccountId);
    if (!student) throw new BackendHttpError(404, 'Student not found.');
    const app = this.state.applications.find((a) => a.briefId === input.briefId && a.studentAccountId === input.studentAccountId);
    if (!app || app.status !== 'ACCEPTED') {
      throw new BackendHttpError(400, 'Student must be accepted on this brief before assigning tasks.');
    }
    const title = (input.title || '').trim();
    if (title.length < 3) throw new BackendHttpError(400, 'Task title is required.');
    const now = Date.now();
    const task: ProjectTaskEntity = {
      id: `task_${crypto.randomUUID().slice(0, 10)}`,
      briefId: input.briefId,
      assignedStudentId: student.id,
      assignedStudentName: student.displayName,
      assignedDirectorId: session.accountId,
      title: title.slice(0, 160),
      milestoneOrder: 2,
      dueDateLabel: (input.dueDateLabel || '').trim() || brief.deadline,
      isOverdue: false,
      status: 'TODO',
      createdAtEpochMs: now,
      updatedAtEpochMs: now,
    };
    this.state.tasks.unshift(task);
    this.pushNotification(
      student.id,
      'TASK',
      'New Milestone Task Assigned',
      'تم إسناد مهمة جديدة لك',
      `${session.displayName} added '${title}' in '${brief.title}'.`,
      `أضاف ${session.displayName} مهمة '${title}' في '${brief.title}'.`,
    );
    this.bumpRevision('task:created', title);
    return task;
  }

  evaluateSubmission(
    session: SessionPrincipal,
    input: {
      submissionId: string;
      score: number;
      feedback: string;
      decision: 'VERIFIED' | 'REVISION_REQUESTED' | 'REJECTED';
      rejectionReason: string;
      expectedEvaluatedAtEpochMs?: number;
    },
  ) {
    this.requireRole(session, 'COMPANY_DIRECTOR');
    if (!['VERIFIED', 'REVISION_REQUESTED', 'REJECTED'].includes(input.decision)) {
      throw new BackendHttpError(400, 'Invalid evaluation decision.');
    }
    const sub = this.state.submissions.find((s) => s.id === input.submissionId);
    if (!sub) throw new BackendHttpError(404, 'Submission not found.');
    if (typeof input.expectedEvaluatedAtEpochMs === 'number' && sub.evaluatedAtEpochMs !== input.expectedEvaluatedAtEpochMs) {
      throw new BackendHttpError(409, 'Conflict: this submission was already evaluated on another device.');
    }
    const brief = this.state.briefs.find((b) => b.id === sub.briefId);
    if (!brief) throw new BackendHttpError(404, 'Project brief not found.');
    if (brief.assignedDirectorId !== session.accountId) {
      throw new BackendHttpError(403, `Access denied: you are not assigned to supervise '${brief.title}'.`);
    }

    const safeScore = Math.max(0, Math.min(100, Math.round(Number(input.score) || 0)));
    const now = Date.now();
    const previouslyVerified = sub.status === 'VERIFIED';
    const awardedPoints = input.decision === 'VERIFIED' ? (previouslyVerified ? sub.pointsAwarded : brief.pointsReward) : 0;
    const verificationCode =
      input.decision === 'VERIFIED' ? `SB-VER-${sub.id.slice(-4).toUpperCase()}-${safeScore}` : sub.verificationCode;

    sub.status = input.decision;
    sub.score = safeScore;
    sub.pointsAwarded = awardedPoints;
    sub.feedback =
      (input.feedback || '').trim() ||
      (input.decision === 'VERIFIED' ? 'Verified implementation meeting rubric standards.' : 'Please review director comments.');
    sub.rejectionReason =
      input.decision === 'VERIFIED' ? '' : (input.rejectionReason || '').trim() || 'Needs rubric refinement.';
    sub.verificationCode = verificationCode;
    sub.evaluatedAtEpochMs = now;

    if (input.decision === 'VERIFIED' && !previouslyVerified) {
      const student = this.state.accounts.find((a) => a.id === sub.studentAccountId);
      if (student) {
        student.points += awardedPoints;
        student.verifiedCount += 1;
        student.streakDays += 1;
        student.careerLevel = computeLevelFromEvidence(student.points, student.verifiedCount);
        const merged = Array.from(
          new Set(
            [...student.skillsCsv.split(','), ...sub.skillsAppliedCsv.split(',')]
              .map((s) => s.trim())
              .filter(Boolean),
          ),
        );
        student.skillsCsv = merged.join(',');
      }
    }

    this.pushNotification(
      sub.studentAccountId,
      'EVALUATION',
      input.decision === 'VERIFIED' ? `Passport Evidence Verified (${safeScore}%)` : `Submission ${input.decision} (${safeScore}%)`,
      input.decision === 'VERIFIED' ? `تم توثيق دليلك العملي (${safeScore}%)` : `تحديث مراجعة التسليم: ${input.decision}`,
      `${session.displayName} evaluated '${sub.briefTitle}': ${sub.feedback}`,
      `قيّم ${session.displayName} مشروع '${sub.briefTitle}': ${sub.feedback}`,
    );
    this.pushNotification(
      brief.createdByAccountId,
      'EVALUATION',
      `Director Evaluated ${sub.studentName}`,
      `قيّم المشرف تسليم ${sub.studentName}`,
      `${sub.briefTitle}: ${input.decision} (${safeScore}%).`,
      `${sub.briefTitle}: ${input.decision} (${safeScore}%).`,
    );
    this.appendActivity(
      session.accountId,
      session.role,
      `SUBMISSION_${input.decision}`,
      `Director scored ${sub.studentName}'s '${sub.briefTitle}' at ${safeScore}% (${input.decision})`,
      sub.id,
    );
    this.bumpRevision('submission:evaluated', `${sub.studentName}: ${input.decision}`);
  }

  markBriefCompleted(session: SessionPrincipal, briefId: string) {
    this.requireRole(session, 'COMPANY_DIRECTOR');
    const brief = this.state.briefs.find((b) => b.id === briefId);
    if (!brief) throw new BackendHttpError(404, 'Brief not found.');
    if (brief.assignedDirectorId !== session.accountId) {
      throw new BackendHttpError(403, 'Access denied: you are not assigned to this project.');
    }
    brief.approvalStatus = 'COMPLETED';
    brief.updatedAtEpochMs = Date.now();
    this.pushNotification(
      brief.createdByAccountId,
      'SYSTEM',
      `Project Completed: ${brief.title}`,
      `اكتمل المشروع: ${brief.title}`,
      `Director ${session.displayName} marked '${brief.title}' completed.`,
      `أنهى المشرف ${session.displayName} مشروع '${brief.title}'.`,
    );
    this.appendActivity(session.accountId, session.role, 'BRIEF_COMPLETED', `Director completed '${brief.title}'`, briefId);
    this.bumpRevision('brief:completed', brief.title);
  }

  updateStudentProfile(
    session: SessionPrincipal,
    input: {
      displayName: string;
      domain: string;
      country: string;
      skills: string[];
      targetSkills: string[];
      goalsSummary: string;
      weeklyHours: number;
    },
  ) {
    this.requireRole(session, 'STUDENT');
    const account = this.state.accounts.find((a) => a.id === session.accountId);
    if (!account) throw new BackendHttpError(404, 'Account not found.');
    const cleanName = (input.displayName || '').trim() || account.displayName;
    const cleanDomain = (input.domain || '').trim() || account.domain;
    const cleanCountry = (input.country || '').trim() || account.country;
    const skills = (input.skills || []).map((s) => s.trim()).filter(Boolean);
    const targetSkills = (input.targetSkills || []).map((s) => s.trim()).filter(Boolean);
    const goalsSummary = (input.goalsSummary || '').trim();
    const checks = [
      Boolean(cleanName),
      Boolean(cleanDomain),
      Boolean(cleanCountry),
      skills.length > 0,
      targetSkills.length > 0,
      Boolean(goalsSummary),
    ];
    const completion = Math.max(30, Math.round((checks.filter(Boolean).length * 100) / 6));

    account.displayName = cleanName;
    account.domain = cleanDomain;
    account.country = cleanCountry;
    account.skillsCsv = skills.join(',');
    account.targetSkillsCsv = targetSkills.join(',');
    account.goalsSummary = goalsSummary;
    account.weeklyHours = Math.max(1, Math.min(60, Number(input.weeklyHours) || account.weeklyHours));
    account.profileCompletion = completion;

    this.bumpRevision('profile:updated', cleanName);
  }

  submitModerationReport(session: SessionPrincipal, targetType: string, targetId: string, reason: string) {
    const cleanReason = (reason || '').trim();
    if (cleanReason.length < 3) {
      throw new BackendHttpError(400, 'Please provide a clear report reason.');
    }
    const report: ModerationReportEntity = {
      id: `rep_${crypto.randomUUID().slice(0, 10)}`,
      reporterAccountId: session.accountId,
      targetType: (targetType || 'BRIEF').trim(),
      targetId: (targetId || '').trim(),
      reason: cleanReason.slice(0, 600),
      status: 'OPEN',
      resolutionNote: '',
      createdAtEpochMs: Date.now(),
    };
    this.state.reports.unshift(report);
    this.notifyAdmins(
      'SYSTEM',
      'New Moderation Report',
      'بلاغ إشراف جديد',
      `${session.displayName} flagged ${report.targetType} (${report.targetId}).`,
      `أبلغ ${session.displayName} عن ${report.targetType} (${report.targetId}).`,
    );
    this.bumpRevision('report:created', report.targetType);
  }

  markAllNotificationsRead(session: SessionPrincipal) {
    for (const notif of this.state.notifications) {
      if (notif.recipientAccountId === session.accountId) {
        notif.isRead = true;
      }
    }
    this.bumpRevision('notifications:read_all', session.loginId);
  }

  markNotificationRead(session: SessionPrincipal, notificationId: string) {
    const notif = this.state.notifications.find(
      (n) => n.id === notificationId && n.recipientAccountId === session.accountId,
    );
    if (notif) {
      notif.isRead = true;
      this.bumpRevision('notifications:read', notificationId);
    }
  }
}
