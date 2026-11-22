package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.core.PasswordHasher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Database(
    entities = [
        AccountEntity::class,
        CompanyEntity::class,
        ProjectBriefEntity::class,
        ProjectApplicationEntity::class,
        ProjectTaskEntity::class,
        WorkSubmissionEntity::class,
        NotificationEntity::class,
        PlatformActivityEntity::class,
        ModerationReportEntity::class,
        DeviceSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SkillBridgeDatabase : RoomDatabase() {
    abstract fun skillBridgeDao(): SkillBridgeDao

    companion object {
        @Volatile
        private var INSTANCE: SkillBridgeDatabase? = null
        private val seedMutex = Mutex()

        fun getInstance(context: Context): SkillBridgeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SkillBridgeDatabase::class.java,
                    "skillbridge_platform.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
        }

        suspend fun ensureSeeded(db: SkillBridgeDatabase) {
            seedMutex.withLock {
                val dao = db.skillBridgeDao()
                if (dao.countAccounts() > 0) return

                val now = System.currentTimeMillis()
                val day = 86_400_000L

                val (adminSalt, adminHash) = PasswordHasher.createSaltedHash("Admin@123")
                val (studentSalt, studentHash) = PasswordHasher.createSaltedHash("Student@123")
                val (companySalt, companyHash) = PasswordHasher.createSaltedHash("Company@123")
                val (directorSalt, directorHash) = PasswordHasher.createSaltedHash("Director@123")
                val (extraSalt, extraHash) = PasswordHasher.createSaltedHash("DemoPass@2026")

                val accounts = listOf(
                    AccountEntity(
                        id = "acc_admin_demo",
                        loginId = "admin_demo",
                        displayName = "SkillBridge Operations Admin",
                        email = "admin@skillbridge.io",
                        passwordSalt = adminSalt,
                        passwordHash = adminHash,
                        role = AccountRole.PLATFORM_ADMIN.key,
                        companyId = "",
                        isActive = true,
                        careerLevel = "Director / Leadership",
                        domain = "Platform Governance",
                        country = "UAE",
                        points = 0,
                        streakDays = 0,
                        verifiedCount = 0,
                        skillsCsv = "Trust & Safety,Rubric Audits,Governance",
                        targetSkillsCsv = "",
                        goalsSummary = "Maintain platform integrity, company verification, and fair brief approvals.",
                        weeklyHours = 40,
                        profileCompletion = 100,
                        createdAtEpochMs = now - 25 * day
                    ),
                    AccountEntity(
                        id = "acc_student_demo",
                        loginId = "student_demo",
                        displayName = "Lina Mansour",
                        email = "student@skillbridge.io",
                        passwordSalt = studentSalt,
                        passwordHash = studentHash,
                        role = AccountRole.STUDENT.key,
                        companyId = "",
                        isActive = true,
                        careerLevel = "Mid-level",
                        domain = "Data & AI",
                        country = "Egypt",
                        points = 640,
                        streakDays = 6,
                        verifiedCount = 2,
                        skillsCsv = "Kotlin,SQL,Python,REST APIs,Analytics",
                        targetSkillsCsv = "Time-Series Telemetry,Compose Canvas,System Design",
                        goalsSummary = "Complete Smart City Telemetry & Cloud Gateway projects to earn Senior verified status.",
                        weeklyHours = 14,
                        profileCompletion = 92,
                        createdAtEpochMs = now - 20 * day
                    ),
                    AccountEntity(
                        id = "acc_company_demo",
                        loginId = "company_demo",
                        displayName = "NileGrid Technologies",
                        email = "talent@nilegrid.io",
                        passwordSalt = companySalt,
                        passwordHash = companyHash,
                        role = AccountRole.COMPANY.key,
                        companyId = "comp_nilegrid",
                        isActive = true,
                        careerLevel = "Director / Leadership",
                        domain = "Data & AI",
                        country = "Egypt",
                        points = 0,
                        streakDays = 0,
                        verifiedCount = 0,
                        skillsCsv = "Telemetry,Smart Cities,Cloud Architecture",
                        targetSkillsCsv = "",
                        goalsSummary = "Publish real-world engineering briefs and identify top MENA talent.",
                        weeklyHours = 20,
                        profileCompletion = 100,
                        createdAtEpochMs = now - 22 * day
                    ),
                    AccountEntity(
                        id = "acc_director_demo",
                        loginId = "director_demo",
                        displayName = "Dr. Karim Hassan",
                        email = "karim.hassan@nilegrid.io",
                        passwordSalt = directorSalt,
                        passwordHash = directorHash,
                        role = AccountRole.COMPANY_DIRECTOR.key,
                        companyId = "comp_nilegrid",
                        isActive = true,
                        careerLevel = "Director / Leadership",
                        domain = "Data & AI",
                        country = "Egypt",
                        points = 0,
                        streakDays = 0,
                        verifiedCount = 0,
                        skillsCsv = "Architecture Review,Telemetry Rubric,Mentorship",
                        targetSkillsCsv = "",
                        goalsSummary = "Supervise assigned NileGrid project briefs and verify student deliverables.",
                        weeklyHours = 18,
                        profileCompletion = 100,
                        createdAtEpochMs = now - 21 * day
                    ),
                    // Additional realistic accounts for talent discovery, multi-company & director isolation verification
                    AccountEntity(
                        id = "acc_student_omar",
                        loginId = "omar_khaled",
                        displayName = "Omar Khaled",
                        email = "omar@skillbridge.io",
                        passwordSalt = extraSalt,
                        passwordHash = extraHash,
                        role = AccountRole.STUDENT.key,
                        companyId = "",
                        isActive = true,
                        careerLevel = "Senior",
                        domain = "Backend & Cloud",
                        country = "Saudi Arabia",
                        points = 1280,
                        streakDays = 14,
                        verifiedCount = 5,
                        skillsCsv = "Kotlin,Distributed Systems,Kubernetes,PostgreSQL",
                        targetSkillsCsv = "Service Mesh,Observability",
                        goalsSummary = "Lead backend architecture teams.",
                        weeklyHours = 16,
                        profileCompletion = 96,
                        createdAtEpochMs = now - 18 * day
                    ),
                    AccountEntity(
                        id = "acc_student_sara",
                        loginId = "sara_nabil",
                        displayName = "Sara Nabil",
                        email = "sara@skillbridge.io",
                        passwordSalt = extraSalt,
                        passwordHash = extraHash,
                        role = AccountRole.STUDENT.key,
                        companyId = "",
                        isActive = true,
                        careerLevel = "Junior",
                        domain = "Security & Reverse Eng",
                        country = "Jordan",
                        points = 290,
                        streakDays = 4,
                        verifiedCount = 1,
                        skillsCsv = "Python,Network Analysis,Unit Testing",
                        targetSkillsCsv = "Protocol Parsing,Binary Analysis",
                        goalsSummary = "Advance through Ethical Reverse Engineering labs.",
                        weeklyHours = 10,
                        profileCompletion = 78,
                        createdAtEpochMs = now - 12 * day
                    ),
                    AccountEntity(
                        id = "acc_company_oasisfin",
                        loginId = "oasisfin_corp",
                        displayName = "OasisFin Cloud",
                        email = "partner@oasisfin.ae",
                        passwordSalt = extraSalt,
                        passwordHash = extraHash,
                        role = AccountRole.COMPANY.key,
                        companyId = "comp_oasisfin",
                        isActive = true,
                        careerLevel = "Director / Leadership",
                        domain = "Backend & Cloud",
                        country = "UAE",
                        points = 0,
                        streakDays = 0,
                        verifiedCount = 0,
                        skillsCsv = "FinTech,Idempotent Ledgers",
                        targetSkillsCsv = "",
                        goalsSummary = "Pending verification for cloud payment engineering briefs.",
                        weeklyHours = 15,
                        profileCompletion = 90,
                        createdAtEpochMs = now - 5 * day
                    ),
                    AccountEntity(
                        id = "acc_director_tariq",
                        loginId = "director_tariq",
                        displayName = "Eng. Tariq Zayed",
                        email = "tariq@oasisfin.ae",
                        passwordSalt = extraSalt,
                        passwordHash = extraHash,
                        role = AccountRole.COMPANY_DIRECTOR.key,
                        companyId = "comp_oasisfin",
                        isActive = true,
                        careerLevel = "Director / Leadership",
                        domain = "Backend & Cloud",
                        country = "UAE",
                        points = 0,
                        streakDays = 0,
                        verifiedCount = 0,
                        skillsCsv = "Payments,Security Audits",
                        targetSkillsCsv = "",
                        goalsSummary = "Supervise OasisFin briefs.",
                        weeklyHours = 12,
                        profileCompletion = 95,
                        createdAtEpochMs = now - 5 * day
                    )
                )

                val companies = listOf(
                    CompanyEntity(
                        id = "comp_nilegrid",
                        ownerAccountId = "acc_company_demo",
                        name = "NileGrid Technologies",
                        city = "Cairo",
                        domain = "Data & AI",
                        verificationStatus = "VERIFIED",
                        verificationTier = "TRUSTED",
                        verificationNotes = "Commercial registration & escrowed mentorship verified by SkillBridge Trust Team.",
                        rewardPolicy = "Points + verified digital certificate + paid milestone stipend",
                        ipPolicy = "Student retains open-source portfolio showcase rights; challenge telemetry stays sandboxed.",
                        verifiedAtEpochMs = now - 20 * day,
                        createdAtEpochMs = now - 22 * day
                    ),
                    CompanyEntity(
                        id = "comp_oasisfin",
                        ownerAccountId = "acc_company_oasisfin",
                        name = "OasisFin Cloud",
                        city = "Dubai",
                        domain = "Backend & Cloud",
                        verificationStatus = "PENDING",
                        verificationTier = "BASIC",
                        verificationNotes = "Submitted trade license; awaiting Admin compliance sign-off for paid escrow.",
                        rewardPolicy = "Points + verified experience badge",
                        ipPolicy = "Non-production sandbox schemas only.",
                        verifiedAtEpochMs = 0L,
                        createdAtEpochMs = now - 5 * day
                    )
                )

                val briefs = listOf(
                    ProjectBriefEntity(
                        id = "brief_smart_city",
                        companyId = "comp_nilegrid",
                        companyName = "NileGrid Technologies",
                        createdByAccountId = "acc_company_demo",
                        assignedDirectorId = "acc_director_demo",
                        assignedDirectorName = "Dr. Karim Hassan",
                        title = "Smart City Analytics Dashboard",
                        summary = "Build an analytics dashboard & anomaly alerting pipeline to visualize live municipal mobility and grid telemetry.",
                        domain = "Data & AI",
                        level = "Mid-level",
                        skillsCsv = "Data Science,Analytics,AI,Kotlin,SQL",
                        rubricCsv = "Data Accuracy (35%),Anomaly Pipeline Latency (30%),UI Clarity & Accessibility (20%),Unit Tests (15%)",
                        deliverablesCsv = "Git Repository,Architecture Note,Unit Test Report,Understanding Summary",
                        deadline = "Dec 30, 2026",
                        pointsReward = 250,
                        prizePool = "$600 Stipend Pool",
                        approvalStatus = "ACTIVE",
                        adminNotes = "Approved: clear rubric, non-PII synthetic city telemetry dataset verified.",
                        isTeamAllowed = true,
                        createdAtEpochMs = now - 14 * day,
                        updatedAtEpochMs = now - 2 * day
                    ),
                    ProjectBriefEntity(
                        id = "brief_gateway_ledger",
                        companyId = "comp_nilegrid",
                        companyName = "NileGrid Technologies",
                        createdByAccountId = "acc_company_demo",
                        assignedDirectorId = "acc_director_demo",
                        assignedDirectorName = "Dr. Karim Hassan",
                        title = "Idempotent Grid Settlement API",
                        summary = "Design a fault-tolerant settlement microservice with deterministic transaction IDs and rate limiting.",
                        domain = "Backend & Cloud",
                        level = "Senior",
                        skillsCsv = "Kotlin,Distributed Systems,SQL,REST APIs",
                        rubricCsv = "Idempotency Guarantees (40%),Concurrency Tests (35%),Schema Design (25%)",
                        deliverablesCsv = "Service Code,Concurrency Stress Report,Understanding Summary",
                        deadline = "Jan 18, 2027",
                        pointsReward = 350,
                        prizePool = "$900 Stipend Pool",
                        approvalStatus = "APPROVED",
                        adminNotes = "Approved for public discovery.",
                        isTeamAllowed = true,
                        createdAtEpochMs = now - 8 * day,
                        updatedAtEpochMs = now - 3 * day
                    ),
                    ProjectBriefEntity(
                        id = "brief_pending_edge",
                        companyId = "comp_nilegrid",
                        companyName = "NileGrid Technologies",
                        createdByAccountId = "acc_company_demo",
                        assignedDirectorId = "acc_director_demo",
                        assignedDirectorName = "Dr. Karim Hassan",
                        title = "Solar Microgrid Forecasting Lab",
                        summary = "Train a lightweight rolling baseline forecaster for regional solar load balancing.",
                        domain = "Data & AI",
                        level = "Junior",
                        skillsCsv = "Python,Analytics,SQL",
                        rubricCsv = "MAE Benchmark (50%),Clean Feature Pipeline (30%),Documentation (20%)",
                        deliverablesCsv = "Notebook / Kotlin Inference Module,Evaluation Report",
                        deadline = "Feb 10, 2027",
                        pointsReward = 180,
                        prizePool = "$300 Stipend Pool",
                        approvalStatus = "PENDING_APPROVAL",
                        adminNotes = "",
                        isTeamAllowed = false,
                        createdAtEpochMs = now - 2 * day,
                        updatedAtEpochMs = now - 2 * day
                    ),
                    ProjectBriefEntity(
                        id = "brief_completed_telemetry",
                        companyId = "comp_nilegrid",
                        companyName = "NileGrid Technologies",
                        createdByAccountId = "acc_company_demo",
                        assignedDirectorId = "acc_director_demo",
                        assignedDirectorName = "Dr. Karim Hassan",
                        title = "Transit Stream Deduplicator",
                        summary = "Completed cohort brief implementing sliding-window GPS packet deduplication.",
                        domain = "Backend & Cloud",
                        level = "Mid-level",
                        skillsCsv = "Kotlin,SQL,Analytics",
                        rubricCsv = "Throughput (40%),Correctness Tests (40%),Docs (20%)",
                        deliverablesCsv = "Verified Repo,Test Suite",
                        deadline = "Completed",
                        pointsReward = 220,
                        prizePool = "$450 Stipend Pool",
                        approvalStatus = "COMPLETED",
                        adminNotes = "Cohort completed with 94% pass rate.",
                        isTeamAllowed = true,
                        createdAtEpochMs = now - 19 * day,
                        updatedAtEpochMs = now - 6 * day
                    ),
                    // Brief belonging to another company & another director (used to enforce & verify Director isolation!)
                    ProjectBriefEntity(
                        id = "brief_oasisfin_vault",
                        companyId = "comp_oasisfin",
                        companyName = "OasisFin Cloud",
                        createdByAccountId = "acc_company_oasisfin",
                        assignedDirectorId = "acc_director_tariq",
                        assignedDirectorName = "Eng. Tariq Zayed",
                        title = "FinTech Ledger Audit Explorer",
                        summary = "Build a read-only cryptographic audit verifier for double-entry ledger snapshots.",
                        domain = "Security & Reverse Eng",
                        level = "Senior",
                        skillsCsv = "Security,Cryptography,Kotlin",
                        rubricCsv = "Verification Accuracy (50%),Threat Model (30%),Tests (20%)",
                        deliverablesCsv = "Verifier Module,Threat Model",
                        deadline = "Jan 25, 2027",
                        pointsReward = 300,
                        prizePool = "$700 Stipend Pool",
                        approvalStatus = "APPROVED",
                        adminNotes = "Approved sandbox brief.",
                        isTeamAllowed = false,
                        createdAtEpochMs = now - 4 * day,
                        updatedAtEpochMs = now - 4 * day
                    )
                )

                val applications = listOf(
                    ProjectApplicationEntity(
                        id = "app_lina_smart_city",
                        briefId = "brief_smart_city",
                        studentAccountId = "acc_student_demo",
                        studentName = "Lina Mansour",
                        studentLevel = "Mid-level",
                        studentSkillsCsv = "Kotlin,SQL,Python,Analytics",
                        coverNote = "Built municipal KPI telemetry prototypes using Compose and SQL aggregation pipelines.",
                        roleRequested = "Data & Visualization Engineer",
                        status = "ACCEPTED",
                        decisionNote = "Strong telemetry background; assigned to Dr. Karim's cohort.",
                        appliedAtEpochMs = now - 10 * day,
                        updatedAtEpochMs = now - 8 * day
                    ),
                    ProjectApplicationEntity(
                        id = "app_omar_smart_city",
                        briefId = "brief_smart_city",
                        studentAccountId = "acc_student_omar",
                        studentName = "Omar Khaled",
                        studentLevel = "Senior",
                        studentSkillsCsv = "Kotlin,Distributed Systems,PostgreSQL",
                        coverNote = "Experienced with high-throughput telemetry ingestion.",
                        roleRequested = "Backend Lead",
                        status = "SHORTLISTED",
                        decisionNote = "Shortlisted for Cohort B.",
                        appliedAtEpochMs = now - 6 * day,
                        updatedAtEpochMs = now - 4 * day
                    ),
                    ProjectApplicationEntity(
                        id = "app_sara_gateway",
                        briefId = "brief_gateway_ledger",
                        studentAccountId = "acc_student_sara",
                        studentName = "Sara Nabil",
                        studentLevel = "Junior",
                        studentSkillsCsv = "Python,Unit Testing",
                        coverNote = "Eager to contribute stress test harnesses for idempotency checks.",
                        roleRequested = "QA & Verification",
                        status = "APPLIED",
                        decisionNote = "",
                        appliedAtEpochMs = now - 2 * day,
                        updatedAtEpochMs = now - 2 * day
                    ),
                    ProjectApplicationEntity(
                        id = "app_lina_completed",
                        briefId = "brief_completed_telemetry",
                        studentAccountId = "acc_student_demo",
                        studentName = "Lina Mansour",
                        studentLevel = "Mid-level",
                        studentSkillsCsv = "Kotlin,SQL,Analytics",
                        coverNote = "Applying with streaming deduplication experience.",
                        roleRequested = "Core Developer",
                        status = "ACCEPTED",
                        decisionNote = "Accepted into completed cohort.",
                        appliedAtEpochMs = now - 18 * day,
                        updatedAtEpochMs = now - 17 * day
                    )
                )

                val tasks = listOf(
                    ProjectTaskEntity(
                        id = "task_sc_1",
                        briefId = "brief_smart_city",
                        assignedStudentId = "acc_student_demo",
                        assignedStudentName = "Lina Mansour",
                        assignedDirectorId = "acc_director_demo",
                        title = "Design telemetry anomaly thresholds & rolling average query",
                        milestoneOrder = 1,
                        dueDateLabel = "Nov 26",
                        isOverdue = false,
                        status = "DONE",
                        createdAtEpochMs = now - 8 * day,
                        updatedAtEpochMs = now - 5 * day
                    ),
                    ProjectTaskEntity(
                        id = "task_sc_2",
                        briefId = "brief_smart_city",
                        assignedStudentId = "acc_student_demo",
                        assignedStudentName = "Lina Mansour",
                        assignedDirectorId = "acc_director_demo",
                        title = "Implement interactive grid health dashboard & latency tests",
                        milestoneOrder = 2,
                        dueDateLabel = "Dec 04",
                        isOverdue = false,
                        status = "IN_PROGRESS",
                        createdAtEpochMs = now - 5 * day,
                        updatedAtEpochMs = now - 1 * day
                    ),
                    ProjectTaskEntity(
                        id = "task_sc_3",
                        briefId = "brief_smart_city",
                        assignedStudentId = "acc_student_demo",
                        assignedStudentName = "Lina Mansour",
                        assignedDirectorId = "acc_director_demo",
                        title = "Submit final Understanding Report & benchmark trace",
                        milestoneOrder = 3,
                        dueDateLabel = "Dec 12",
                        isOverdue = false,
                        status = "TODO",
                        createdAtEpochMs = now - 5 * day,
                        updatedAtEpochMs = now - 5 * day
                    )
                )

                val submissions = listOf(
                    WorkSubmissionEntity(
                        id = "sub_lina_completed",
                        briefId = "brief_completed_telemetry",
                        briefTitle = "Transit Stream Deduplicator",
                        companyName = "NileGrid Technologies",
                        studentAccountId = "acc_student_demo",
                        studentName = "Lina Mansour",
                        directorAccountId = "acc_director_demo",
                        repoOrDemoUrl = "https://github.com/lina-mansour/transit-dedup-lab",
                        understandingReport = "Implemented sliding-window monotonic timestamp filter with deterministic idempotency keys, cutting duplicate telemetry writes by 98.4% in replay benchmarks.",
                        skillsAppliedCsv = "Kotlin,SQL,Analytics",
                        status = "VERIFIED",
                        score = 94,
                        pointsAwarded = 220,
                        feedback = "Excellent work! Strong analytical skills and clean edge-case test coverage.",
                        rejectionReason = "",
                        verificationCode = "SB-VER-9401",
                        isPublicOnPassport = true,
                        submittedAtEpochMs = now - 9 * day,
                        evaluatedAtEpochMs = now - 7 * day
                    ),
                    WorkSubmissionEntity(
                        id = "sub_lina_smart_city_pending",
                        briefId = "brief_smart_city",
                        briefTitle = "Smart City Analytics Dashboard",
                        companyName = "NileGrid Technologies",
                        studentAccountId = "acc_student_demo",
                        studentName = "Lina Mansour",
                        directorAccountId = "acc_director_demo",
                        repoOrDemoUrl = "https://github.com/lina-mansour/smart-city-telemetry",
                        understandingReport = "Built real-time sector grid load aggregator with rolling 15-minute z-score anomaly flagging and accessible KPI cards.",
                        skillsAppliedCsv = "Data Science,Analytics,AI,Kotlin",
                        status = "PENDING_REVIEW",
                        score = 0,
                        pointsAwarded = 0,
                        feedback = "",
                        rejectionReason = "",
                        verificationCode = "SB-PEND-7721",
                        isPublicOnPassport = true,
                        submittedAtEpochMs = now - 1 * day,
                        evaluatedAtEpochMs = 0L
                    ),
                    WorkSubmissionEntity(
                        id = "sub_omar_oasisfin_isolated",
                        briefId = "brief_oasisfin_vault",
                        briefTitle = "FinTech Ledger Audit Explorer",
                        companyName = "OasisFin Cloud",
                        studentAccountId = "acc_student_omar",
                        studentName = "Omar Khaled",
                        directorAccountId = "acc_director_tariq",
                        repoOrDemoUrl = "https://github.com/omar-khaled/ledger-verifier",
                        understandingReport = "Merkle snapshot verification CLI with double-entry invariant assertions.",
                        skillsAppliedCsv = "Security,Cryptography,Kotlin",
                        status = "PENDING_REVIEW",
                        score = 0,
                        pointsAwarded = 0,
                        feedback = "",
                        rejectionReason = "",
                        verificationCode = "SB-PEND-3310",
                        isPublicOnPassport = true,
                        submittedAtEpochMs = now - 2 * day,
                        evaluatedAtEpochMs = 0L
                    )
                )

                val notifications = listOf(
                    NotificationEntity(
                        id = "notif_admin_1",
                        recipientAccountId = "acc_admin_demo",
                        category = "APPROVAL",
                        titleEn = "1 Company & 1 Brief awaiting approval",
                        titleAr = "شركة ومشروع بانتظار المراجعة",
                        bodyEn = "OasisFin Cloud verification & Solar Microgrid Forecasting Lab need Admin sign-off.",
                        bodyAr = "شركة OasisFin ومشروع مختبر الطاقة الشمسية بانتظار اعتماد المشرف العام.",
                        isRead = false,
                        createdAtEpochMs = now - 2 * day
                    ),
                    NotificationEntity(
                        id = "notif_student_1",
                        recipientAccountId = "acc_student_demo",
                        category = "EVALUATION",
                        titleEn = "Verified Experience Earned (94%)",
                        titleAr = "تم توثيق خبرتك العملية (94%)",
                        bodyEn = "Dr. Karim Hassan verified Transit Stream Deduplicator (+220 pts).",
                        bodyAr = "قام د. كريم حسن بتوثيق مشروع مزيل تكرار بيانات النقل (+220 نقطة).",
                        isRead = false,
                        createdAtEpochMs = now - 7 * day
                    ),
                    NotificationEntity(
                        id = "notif_company_1",
                        recipientAccountId = "acc_company_demo",
                        category = "APPLICATION",
                        titleEn = "New applicants on Smart City & Settlement briefs",
                        titleAr = "متقدمون جدد على مشاريعك العملية",
                        bodyEn = "Review shortlisted & new student applications in your Applicants workspace.",
                        bodyAr = "راجع الطلبات الجديدة والمرشحين المختارين في مساحة المتقدمين.",
                        isRead = false,
                        createdAtEpochMs = now - 2 * day
                    ),
                    NotificationEntity(
                        id = "notif_director_1",
                        recipientAccountId = "acc_director_demo",
                        category = "EVALUATION",
                        titleEn = "Submission awaiting rubric review",
                        titleAr = "تسليم عملي بانتظار التقييم",
                        bodyEn = "Lina Mansour submitted Smart City Analytics Dashboard for evaluation.",
                        bodyAr = "رفعت لينا منصور تسليم لوحة تحليلات المدينة الذكية للمراجعة.",
                        isRead = false,
                        createdAtEpochMs = now - 1 * day
                    )
                )

                val activities = listOf(
                    PlatformActivityEntity("act_1", "acc_company_demo", AccountRole.COMPANY.key, "BRIEF_PUBLISHED", "NileGrid published Smart City Analytics Dashboard", "brief_smart_city", now - 14 * day),
                    PlatformActivityEntity("act_2", "acc_admin_demo", AccountRole.PLATFORM_ADMIN.key, "BRIEF_APPROVED", "Admin approved Smart City Analytics Dashboard", "brief_smart_city", now - 13 * day),
                    PlatformActivityEntity("act_3", "acc_student_demo", AccountRole.STUDENT.key, "APPLIED", "Lina Mansour applied to Smart City Analytics Dashboard", "app_lina_smart_city", now - 10 * day),
                    PlatformActivityEntity("act_4", "acc_company_demo", AccountRole.COMPANY.key, "APPLICATION_ACCEPTED", "NileGrid accepted Lina Mansour into Smart City cohort", "app_lina_smart_city", now - 8 * day),
                    PlatformActivityEntity("act_5", "acc_director_demo", AccountRole.COMPANY_DIRECTOR.key, "SUBMISSION_VERIFIED", "Dr. Karim Hassan verified Transit Stream Deduplicator (94%)", "sub_lina_completed", now - 7 * day),
                    PlatformActivityEntity("act_6", "acc_company_demo", AccountRole.COMPANY.key, "BRIEF_CREATED", "NileGrid submitted Solar Microgrid Forecasting Lab for approval", "brief_pending_edge", now - 2 * day),
                    PlatformActivityEntity("act_7", "acc_student_demo", AccountRole.STUDENT.key, "WORK_SUBMITTED", "Lina Mansour submitted Smart City Analytics Dashboard", "sub_lina_smart_city_pending", now - 1 * day)
                )

                val reports = listOf(
                    ModerationReportEntity(
                        id = "rep_seed_1",
                        reporterAccountId = "acc_student_sara",
                        targetType = "BRIEF",
                        targetId = "brief_oasisfin_vault",
                        reason = "Clarify sandbox test vector license before paid cohort starts.",
                        status = "OPEN",
                        resolutionNote = "",
                        createdAtEpochMs = now - 2 * day
                    )
                )

                dao.insertAccounts(accounts)
                dao.insertCompanies(companies)
                dao.insertBriefs(briefs)
                dao.insertApplications(applications)
                dao.insertTasks(tasks)
                dao.insertSubmissions(submissions)
                dao.insertNotifications(notifications)
                dao.insertActivities(activities)
                dao.insertReports(reports)
            }
        }
    }
}
