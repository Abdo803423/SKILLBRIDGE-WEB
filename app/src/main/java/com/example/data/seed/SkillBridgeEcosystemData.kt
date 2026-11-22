package com.example.data.seed

import com.example.core.LocalizedText
import com.example.data.model.BadgeDefinition
import com.example.data.model.ChallengeItem
import com.example.data.model.ChallengeSubmission
import com.example.data.model.CommunityComment
import com.example.data.model.CommunityPost
import com.example.data.model.CompanyProfile
import com.example.data.model.ContestItem
import com.example.data.model.ContestProblem
import com.example.data.model.ContestStatus
import com.example.data.model.DiscoverCategory
import com.example.data.model.EcosystemNotification
import com.example.data.model.EvidenceGraphNode
import com.example.data.model.LeaderboardEntry
import com.example.data.model.RewardTransaction
import com.example.data.model.Team
import com.example.data.model.TeamTask
import com.example.data.model.VerificationTier

object SystemThresholds {
    const val MID_LEVEL_MIN_POINTS = 500L
    const val MID_LEVEL_MIN_VERIFIED = 2
    const val SENIOR_LEVEL_MIN_POINTS = 1500L
    const val SENIOR_LEVEL_MIN_VERIFIED = 5
    const val DIRECTOR_LEVEL_MIN_POINTS = 3500L
    const val DIRECTOR_LEVEL_MIN_VERIFIED = 10
    const val HIGH_SCORE_THRESHOLD = 85L
    const val TOP_PERFORMER_MIN_POINTS = 1000L
}

object SkillBridgeEcosystemData {

    val domains = listOf("Backend & Cloud", "Data Science & AI", "Mobile & Android", "Cybersecurity", "Product & UX")
    val careerLevels = listOf("Junior", "Mid-level", "Senior", "Director / Leadership")
    val planTiers = listOf("Starter — Free", "Growth — Paid", "Professional+ — Paid", "Company / Enterprise")
    val countries = listOf("Egypt", "Saudi Arabia", "UAE", "Jordan", "Morocco", "Global")

    val badgeDefinitions: List<BadgeDefinition> = listOf(
        BadgeDefinition(
            id = "first_challenge",
            title = LocalizedText("First Challenge", "التحدي الأول"),
            description = LocalizedText(
                "Earn your very first mentor-verified challenge or project submission.",
                "احصل على أول توثيق عملي معتمد لتحدٍ أو مشروع."
            ),
            criteria = LocalizedText("1 verified challenge submission", "توثيق تسليم تحدٍ واحد بنجاح"),
            pointsAwarded = 150,
            targetCount = 1,
            category = "CHALLENGE"
        ),
        BadgeDefinition(
            id = "problem_solver",
            title = LocalizedText("Problem Solver", "حلّال المشكلات"),
            description = LocalizedText(
                "Demonstrate strong engineering accuracy across multiple verified challenges.",
                "أثبت دقة هندسية عالية عبر تحديين موثقين بنسبة ٨٥٪ فأكثر."
            ),
            criteria = LocalizedText("2 verified submissions scoring ≥ 85%", "تسليمان موثقان بدرجة ٨٥٪ فأكثر"),
            pointsAwarded = 250,
            targetCount = 2,
            category = "CHALLENGE"
        ),
        BadgeDefinition(
            id = "team_player",
            title = LocalizedText("Team Player", "روح الفريق"),
            description = LocalizedText(
                "Collaborate in a SkillBridge team and ship sprint deliverables together.",
                "شارك في فريق عمل وأنجز مهام سباق العمل الجماعي."
            ),
            criteria = LocalizedText("Join or create 1 team & complete 2 sprint tasks", "الانضمام لفريق وإنجاز مهمتين"),
            pointsAwarded = 200,
            targetCount = 3,
            category = "TEAM"
        ),
        BadgeDefinition(
            id = "community_helper",
            title = LocalizedText("Community Helper", "مساعد المجتمع"),
            description = LocalizedText(
                "Support peers with clear questions, answers, and helpful community posts.",
                "ساعد المطورين الآخرين بنشر أسئلة تقنية أو إجابات مفيدة."
            ),
            criteria = LocalizedText("2 authored community posts or replies", "مشاركتان أو إجابتان مفيدتان في المجتمع"),
            pointsAwarded = 150,
            targetCount = 2,
            category = "COMMUNITY"
        ),
        BadgeDefinition(
            id = "reverse_eng_explorer",
            title = LocalizedText("Reverse Engineering Explorer", "مستكشف الهندسة العكسية"),
            description = LocalizedText(
                "Complete an authorized sandbox Reverse Engineering lab with an Understanding Report.",
                "أكمل مختبر هندسة عكسية مصرّحًا به مع تقرير فهم موثّق."
            ),
            criteria = LocalizedText("1 verified Reverse Engineering lab", "توثيق مختبر هندسة عكسية واحد"),
            pointsAwarded = 300,
            targetCount = 1,
            category = "REVERSE_ENG"
        ),
        BadgeDefinition(
            id = "consistent_builder",
            title = LocalizedText("Consistent Builder", "البنّاء المستمر"),
            description = LocalizedText(
                "Complete learning roadmap milestones and verified practice deliverables consistently.",
                "أكمل معالم خارطة التعلم والتسليمات العملية باستمرار."
            ),
            criteria = LocalizedText("3 completed roadmap milestones or submissions", "٣ معالم خارطة طريق أو تسليمات مكتملة"),
            pointsAwarded = 180,
            targetCount = 3,
            category = "CONSISTENCY"
        ),
        BadgeDefinition(
            id = "verified_skill",
            title = LocalizedText("Verified Skill", "مهارة موثّقة"),
            description = LocalizedText(
                "Accumulate distinct verified skills backed by rubric evidence on your Passport.",
                "اجمع ٣ مهارات متميزة موثّقة بأدلة عملية في سجل الخبرة."
            ),
            criteria = LocalizedText("3 unique verified skills on Passport", "٣ مهارات موثّقة مختلفة في سجلك"),
            pointsAwarded = 250,
            targetCount = 3,
            category = "PASSPORT"
        ),
        BadgeDefinition(
            id = "mentor_contributor",
            title = LocalizedText("Mentor Contributor", "مساهم إرشادي"),
            description = LocalizedText(
                "Contribute constructive evaluations or accepted best-answer guidance to peers.",
                "قدّم مراجعات بنّاءة أو إجابات معتمدة لمساعدة الزملاء."
            ),
            criteria = LocalizedText("2 mentor evaluations or accepted answers", "تقييمان إرشاديان أو إجابتان معتمدتان"),
            pointsAwarded = 220,
            targetCount = 2,
            category = "MENTOR"
        ),
        BadgeDefinition(
            id = "top_mena_performer",
            title = LocalizedText("Top MENA Performer", "نخبة الشرق الأوسط"),
            description = LocalizedText(
                "Reach 1,000+ verified SkillBridge points through real challenges & contributions.",
                "تجاوز ١٠٠٠ نقطة موثّقة عبر التحديات والمساهمات الحقيقية."
            ),
            criteria = LocalizedText("1,000 verified points accumulated", "جمع ١٠٠٠ نقطة موثّقة"),
            pointsAwarded = 400,
            targetCount = 1000,
            category = "RANK"
        )
    )

    val challenges: List<ChallengeItem> = listOf(
        ChallengeItem(
            id = "ch_smart_city",
            category = DiscoverCategory.PROJECT,
            title = LocalizedText("Smart City Analytics Dashboard", "لوحة تحليلات المدينة الذكية"),
            subtitle = LocalizedText(
                "Build a real-time transit & energy telemetry dashboard using anonymized municipal feeds.",
                "ابنِ لوحة قياس لحظية لبيانات المرور والطاقة باستخدام تدفقات بيانات بلدية مجهولة الهوية."
            ),
            companyName = "Oasis Smart Mobility",
            companyTier = VerificationTier.TRUSTED,
            domain = "Data Science & AI",
            level = "Mid-level",
            isTeamAllowed = true,
            soloOrTeamLabel = LocalizedText("Solo or Team (2-4)", "فردي أو فريق (٢-٤)"),
            deadline = "2026-12-30",
            pointsReward = 350,
            prizePool = "$1,500 Pool",
            mentorName = "Dr. Tariq Al-Mansoor",
            mentorRole = "Principal Data Architect",
            skills = listOf("Data Science", "Analytics", "Kotlin", "Time-Series SQL"),
            rubric = listOf(
                LocalizedText("40% Accuracy & latency under 50k simulated events/min", "٤٠٪ الدقة وزمن الاستجابة تحت ٥٠ ألف حدث/دقيقة"),
                LocalizedText("30% Clean architecture & automated unit tests", "٣٠٪ جودة البنية البرمجية والاختبارات الآلية"),
                LocalizedText("30% Explainable UI insights & accessibility", "٣٠٪ وضوح الرسوم البيانية وسهولة الوصول")
            ),
            deliverables = listOf(
                LocalizedText("Git repository with reproducible README + benchmark", "مستودع Git مع توثيق التشغيل ونتائج الأداء"),
                LocalizedText("2-minute architecture walkthrough & trade-offs note", "ملخص معماري يوضح القرارات التقنية والبدائل")
            ),
            aiPolicy = LocalizedText(
                "AI assistants allowed for boilerplate; you must explain every schema and metric choice in your verification report.",
                "يُسمح بأدوات الذكاء الاصطناعي للمساعدة الأولية بشرط شرح كل قرار تصميمي ومؤشر في تقرير الفهم."
            ),
            ipPolicy = LocalizedText(
                "You retain full portfolio rights to your solution code; company receives evaluation review access only.",
                "تحتفظ بحقوق ملكية الكود لعرضه في ملفك، وتحصل الشركة على صلاحية التقييم فقط."
            ),
            faq = listOf(
                LocalizedText("Can a 3-person team split frontend and backend?", "هل يمكن لفريق من ٣ أشخاص تقسيم الواجهة والخلفية؟") to
                    LocalizedText("Yes! Each member specifies their role and receives individual verified skill credit.", "نعم، يحدد كل عضو دوره ويحصل على توثيق مهارة فردي وموثّق.")
            )
        ),
        ChallengeItem(
            id = "ch_nilepay_guard",
            category = DiscoverCategory.CHALLENGE,
            title = LocalizedText("Payment Idempotency & Fraud Guardrail", "حماية المدفوعات ومنع التكرار"),
            subtitle = LocalizedText(
                "Design an idempotent ledger API handling concurrent retries and anomaly rules.",
                "صمّم واجهة برمجية مقاومة لإعادة الإرسال المتزامن مع قواعد رصد الشذوذ المالي."
            ),
            companyName = "NilePay Fintech",
            companyTier = VerificationTier.BUSINESS,
            domain = "Backend & Cloud",
            level = "Senior",
            isTeamAllowed = false,
            soloOrTeamLabel = LocalizedText("Solo Challenge", "تحدٍ فردي"),
            deadline = "2026-12-18",
            pointsReward = 420,
            prizePool = "$2,000 Top 3",
            mentorName = "Nadia Haddad",
            mentorRole = "Director of Platform Engineering",
            skills = listOf("Distributed Systems", "PostgreSQL", "Idempotency", "API Security"),
            rubric = listOf(
                LocalizedText("50% Zero double-charge under 100 concurrent retry tests", "٥٠٪ عدم حدوث خصم مكرر تحت ١٠٠ محاولة متزامنة"),
                LocalizedText("30% Audit trail schema & deterministic state transitions", "٣٠٪ سجل التدقيق وتدرّج الحالات بدقة"),
                LocalizedText("20% Error budget & structured failure handling", "٢٠٪ معالجة الأخطاء بوضوح")
            ),
            deliverables = listOf(
                LocalizedText("API implementation + concurrency stress test suite", "تنفيذ الواجهة البرمجية مع اختبارات التزامن"),
                LocalizedText("State machine diagram & failure recovery notes", "مخطط الحالات وخطة التعافي من الأعطال")
            ),
            aiPolicy = LocalizedText(
                "Strict understanding review: submissions without clear lock/transaction reasoning fail verification.",
                "مراجعة فهم صارمة: تُرفض الحلول التي لا تشرح آلية الأقفال والمعاملات."
            ),
            ipPolicy = LocalizedText(
                "Open-source MIT portfolio submission; winners may opt into paid pilot extension.",
                "تسليم مفتوح المصدر لملفك المهني مع إمكانية التعاقد الاختياري للفائزين."
            ),
            faq = listOf(
                LocalizedText("Which languages are accepted?", "ما اللغات البرمجية المقبولة؟") to
                    LocalizedText("Kotlin, Go, Java, Python, or TypeScript with containerized tests.", "Kotlin أو Go أو Java أو Python أو TypeScript مع اختبارات قابلة للتشغيل.")
            )
        ),
        ChallengeItem(
            id = "ch_re_jwt_lab",
            category = DiscoverCategory.REVERSE_ENG,
            title = LocalizedText("Auth Token Rotation Protocol Audit", "تحليل وإعادة بناء بروتوكول تدوير التوكن"),
            subtitle = LocalizedText(
                "Inspect an authorized sandbox auth server trace to reconstruct refresh-token replay protection.",
                "حلّل آثار خادم مصادقة تجريبي مصرّح به لإعادة بناء آلية الحماية من إعادة استخدام التوكن."
            ),
            companyName = "Cedars Cloud Labs",
            companyTier = VerificationTier.TRUSTED,
            domain = "Cybersecurity",
            level = "Mid-level",
            isTeamAllowed = true,
            soloOrTeamLabel = LocalizedText("Solo or Pair (1-2)", "فردي أو ثنائي"),
            deadline = "2026-12-25",
            pointsReward = 380,
            prizePool = "$900 Lab Bounty",
            mentorName = "Karim Darwish",
            mentorRole = "Staff AppSec Engineer",
            skills = listOf("Reverse Engineering", "JWT Security", "Threat Modeling", "Clean Architecture"),
            rubric = listOf(
                LocalizedText("45% Accurate protocol state reconstruction in sandbox", "٤٥٪ دقة إعادة بناء تدفق البروتوكول في البيئة الآمنة"),
                LocalizedText("35% Understanding & Threat Report clarity", "٣٥٪ جودة تقرير الفهم وتحليل المخاطر"),
                LocalizedText("20% Passing all 4 defensive unit assertions", "٢٠٪ اجتياز اختبارات التحقق الأربعة")
            ),
            deliverables = listOf(
                LocalizedText("Clean-room Kotlin/Python validator passing sandbox tests", "وحدة تحقق نظيفة تجتاز اختبارات البيئة المعزولة"),
                LocalizedText("Mandatory Understanding Report (no raw exploit payloads)", "تقرير فهم إلزامي يشرح الثغرة وإصلاحها بدون استغلال ضار")
            ),
            aiPolicy = LocalizedText(
                "AI may summarize trace logs; never paste real credentials or private keys.",
                "يمكن استخدام AI لتلخيص السجلات، ويُمنع لصق أي مفاتيح سرية حقيقية."
            ),
            ipPolicy = LocalizedText(
                "Sandbox artifacts remain SkillBridge educational property; your report is verified in your Passport.",
                "تبقى ملفات المختبر تعليمية، ويُوثق تقريرك كدليل مهارة في سجلك."
            ),
            faq = listOf(
                LocalizedText("Is external network scanning allowed?", "هل يُسمح بفحص نطاقات خارجية؟") to
                    LocalizedText("Strictly forbidden. Only inspect the provided sandbox trace & OpenAPI mock.", "ممنوع تمامًا. التحليل محصور داخل الملفات التجريبية المصرّح بها فقط.")
            ),
            scopeNote = LocalizedText(
                "Ethical / Authorized Scope: Provided mock OAuth2 refresh trace v1.4 inside isolated SkillBridge sandbox.",
                "نطاق آمن ومصرّح به: آثار تدفق مصادقة تجريبي v1.4 داخل بيئة SkillBridge المعزولة فقط."
            ),
            allowedRules = listOf(
                LocalizedText("Analyze provided trace JSON, headers, and mock validator source", "تحليل ملفات JSON والترويسات وكود المختبر المرفق"),
                LocalizedText("Reconstruct clean-room token family invalidation logic", "إعادة بناء منطق إبطال سلسلة التوكن بأسلوب نظيف"),
                LocalizedText("Document remediation steps in your Understanding Report", "توثيق خطوات التحصين في تقرير الفهم")
            ),
            prohibitedRules = listOf(
                LocalizedText("No scanning, probing, or attacking live external domains/APIs", "يُمنع فحص أو اختبار أي خوادم أو نطاقات حقيقية خارجية"),
                LocalizedText("No uploading real API keys, PII, or proprietary client binaries", "يُمنع رفع أي مفاتيح API حقيقية أو بيانات شخصية أو ملفات سرية"),
                LocalizedText("No automated brute-force or denial-of-service scripts", "يُمنع تشغيل أدوات تخمين أو إغراق للخدمات")
            ),
            sandboxSnippet = """
                // Authorized Sandbox Trace v1.4 (Cedars Cloud Labs)
                POST /sandbox/v1/token/refresh
                Headers: { "X-Device-Id": "dev_94a", "X-Token-Family": "fam_771" }
                Trace Event #1: token_gen=2 rotated -> token_gen=3 (200 OK)
                Trace Event #2: REPLAY DETECTED token_gen=2 -> Expected: REVOKE_FAMILY(fam_771) + 401
                Bug Observed: Server returned 200 OK when X-Device-Id matched!
            """.trimIndent(),
            testCases = listOf(
                "TC-01: Valid latest generation rotates smoothly (200 OK)",
                "TC-02: Reused previous generation revokes entire token family (401)",
                "TC-03: Expired family returns deterministic error code TOKEN_FAMILY_REVOKED",
                "TC-04: Secret scanner verifies zero leaked credentials in submission"
            )
        ),
        ChallengeItem(
            id = "ch_re_telemetry",
            category = DiscoverCategory.REVERSE_ENG,
            title = LocalizedText("Legacy Binary Frame Parser Reconstruction", "إعادة بناء محلل حزم القياس القديمة"),
            subtitle = LocalizedText(
                "Decode an authorized IoT sensor packet spec from sample hex frames and rebuild a safe Kotlin parser.",
                "فكّ بنية حزم مستشعر إنترنت أشياء مصرّح به من عينات Hex وأعد بناءه بأمان."
            ),
            companyName = "Amman Grid Telemetry",
            companyTier = VerificationTier.BUSINESS,
            domain = "Backend & Cloud",
            level = "Junior",
            isTeamAllowed = false,
            soloOrTeamLabel = LocalizedText("Solo Lab", "مختبر فردي"),
            deadline = "2026-12-28",
            pointsReward = 260,
            prizePool = "260 Verified Pts",
            mentorName = "Yasmine Al-Khatib",
            mentorRole = "Embedded Systems Lead",
            skills = listOf("Binary Parsing", "Reverse Engineering", "Input Validation", "Unit Testing"),
            rubric = listOf(
                LocalizedText("50% Byte offset & CRC-16 validation accuracy", "٥٠٪ دقة قراءة الإزاحات والتحقق من CRC-16"),
                LocalizedText("30% Safe bounds checks against malformed frames", "٣٠٪ حماية الذاكرة والحدود ضد الحزم التالفة"),
                LocalizedText("20% Clear Understanding Report", "٢٠٪ وضوح تقرير الفهم")
            ),
            deliverables = listOf(
                LocalizedText("Clean parser function + malformed input tests", "دالة تحليل آمنة مع اختبارات الحزم الخاطئة"),
                LocalizedText("Packet layout table in Understanding Report", "جدول بنية الحزمة داخل تقرير الفهم")
            ),
            aiPolicy = LocalizedText(
                "Use AI only to review edge-case bounds; write your offset table yourself.",
                "استعن بالذكاء الاصطناعي لمراجعة الحالات الحدية فقط واكتب جدول البتّات بنفسك."
            ),
            ipPolicy = LocalizedText(
                "Educational authorized sample; verified badge added to Experience Passport.",
                "عينة تعليمية مصرّح بها تُضاف كشهادة موثقة في سجلك."
            ),
            faq = listOf(),
            scopeNote = LocalizedText(
                "Ethical / Authorized Scope: Synthetic sensor frames published under Apache-2.0 lab license.",
                "نطاق آمن ومصرّح به: حزم بيانات تركيبية منشورة برخصة مختبر تعليمي مفتوحة."
            ),
            allowedRules = listOf(
                LocalizedText("Inspect hex dump bytes 0..15 from the lab specification", "تحليل عينات Hex المرفقة من البايت 0 إلى 15"),
                LocalizedText("Test malformed lengths locally in unit tests", "اختبار الأطوال التالفة محليًا ضمن اختبارات الوحدة")
            ),
            prohibitedRules = listOf(
                LocalizedText("Do not attempt firmware extraction from commercial hardware", "يُمنع سحب برامج ثابتة من أجهزة تجارية غير مصرّح بها"),
                LocalizedText("Do not include third-party proprietary SDK dumps", "يُمنع رفع حزم برمجية تجارية مغلقة المصدر")
            ),
            sandboxSnippet = """
                // Sample Authorized Hex Frames (Little-Endian)
                Frame A: AA 55 01 00 1A 04 B8 0B 3F 91  -> version=1, temp=26.0C, battery=3000mV, crc=OK
                Frame B: AA 55 01 00 FF 7F 00 00 12 00  -> malformed battery=0mV -> Expect ParseError.INVALID_RANGE
            """.trimIndent(),
            testCases = listOf(
                "TC-01: Header magic bytes 0xAA 0x55 validated",
                "TC-02: Truncated frame < 10 bytes throws safe error",
                "TC-03: CRC mismatch rejected cleanly"
            )
        ),
        ChallengeItem(
            id = "ch_mentor_sara",
            category = DiscoverCategory.MENTOR,
            title = LocalizedText("1:1 Architecture & Portfolio Review", "جلسة إرشاد معماري ومراجعة السجل"),
            subtitle = LocalizedText(
                "Book a 30-min rubric-aligned code & career evidence review with Dr. Salma El-Sayed.",
                "احجز جلسة ٣٠ دقيقة لمراجعة الكود وأدلة المهارات مع د. سلمى السيد."
            ),
            companyName = "SkillBridge Mentor Guild",
            companyTier = VerificationTier.TRUSTED,
            domain = "Mobile & Android",
            level = "Mid-level",
            isTeamAllowed = false,
            soloOrTeamLabel = LocalizedText("1:1 Mentorship", "إرشاد فردي"),
            deadline = "Weekly Slots",
            pointsReward = 120,
            prizePool = "Verified Endorsement",
            mentorName = "Dr. Salma El-Sayed",
            mentorRole = "Staff Mobile Architect • Cairo",
            skills = listOf("Jetpack Compose", "Clean Architecture", "System Design"),
            rubric = listOf(
                LocalizedText("Bring 1 completed SkillBridge challenge or PR", "إحضار تسليم تحدٍ واحد مكتمل على الأقل"),
                LocalizedText("Actionable feedback logged directly on your Passport", "توثيق الملاحظات مباشرة في سجل الخبرة")
            ),
            deliverables = listOf(
                LocalizedText("Updated Skill Gap milestone plan", "خطة محدثة لإغلاق فجوات المهارات")
            ),
            aiPolicy = LocalizedText("Human mentor review with AI pre-check summary.", "مراجعة مرشد بشري مدعومة بملخص فحص أولي."),
            ipPolicy = LocalizedText("Confidential 1:1 session notes visible only per your privacy toggles.", "ملاحظات خاصة لا تظهر للعامة إلا بإذنك."),
            faq = listOf()
        ),
        ChallengeItem(
            id = "ch_opp_careem",
            category = DiscoverCategory.OPPORTUNITY,
            title = LocalizedText("Junior-to-Mid Backend Engineer (Evidence Fast-Track)", "مهندس خلفية — مسار تخطي الفرز عبر الأدلة"),
            subtitle = LocalizedText(
                "Matched to your 92% Skill Match & verified NilePay/Smart City submissions.",
                "فرصة مطابقة لنسبة ٩٢٪ من مهاراتك وأدلتك الموثقة في التحديات العملية."
            ),
            companyName = "Riyadh Mobility Cloud",
            companyTier = VerificationTier.TRUSTED,
            domain = "Backend & Cloud",
            level = "Mid-level",
            isTeamAllowed = false,
            soloOrTeamLabel = LocalizedText("Verified Evidence Track", "مسار الأدلة الموثقة"),
            deadline = "2027-01-15",
            pointsReward = 0,
            prizePool = "Full-Time / Remote MENA",
            mentorName = "Faisal Al-Qahtani",
            mentorRole = "Engineering Director",
            skills = listOf("Kotlin", "PostgreSQL", "Distributed Systems", "Clean Architecture"),
            rubric = listOf(
                LocalizedText("Requires >= 2 Verified Challenges in Backend or Data", "يتطلب توثيق تحديين على الأقل في الخلفية أو البيانات"),
                LocalizedText("No guaranteed hire — skips initial CV screening straight to technical round", "لا وعود توظيف مضمونة — يمنحك عبورًا مباشرًا للمقابلة التقنية")
            ),
            deliverables = listOf(
                LocalizedText("Share verified Experience Passport link", "مشاركة رابط سجل الخبرة الموثّق")
            ),
            aiPolicy = LocalizedText("Explainable AI match based purely on verified skills & rubric scores.", "ترشيح ذكي شفاف مبني على المهارات الموثقة فقط."),
            ipPolicy = LocalizedText("You control which Passport items the hiring team can inspect.", "أنت تتحكم في المشاريع والروابط الظاهرة للشركة."),
            faq = listOf()
        )
    )

    val contests: List<ContestItem> = listOf(
        ContestItem(
            id = "cnt_mena_14",
            title = LocalizedText("MENA Distributed Systems Cup #14", "كأس الشرق الأوسط للأنظمة الموزعة #١٤"),
            organizer = "NilePay Fintech • Director Faisal",
            status = ContestStatus.LIVE,
            countdownLabel = LocalizedText("Ends in 02h 18m", "تنتهي خلال ٠٢ س ١٨ د"),
            isTeamContest = true,
            prizePool = "$3,500 + 1,200 Pts",
            rulesSummary = LocalizedText(
                "4 practical system & algorithmic tasks. ICPC-style wrong-submission penalty (-10 min), transparent live scoreboard, and post-contest editorial.",
                "٤ مسائل عملية وخوارزمية. خصم ١٠ دقائق لكل محاولة خاطئة بعد القبول، لوحة ترتيب لحظية، وشرح حلول كامل بعد المسابقة."
            ),
            problems = listOf(
                ContestProblem("A", LocalizedText("Idempotency Key Deduplicator", "كاشف تكرار مفاتيح الدفع"), "Easy • 800", 150, 412, "AC"),
                ContestProblem("B", LocalizedText("Sliding Window Rate Limiter", "مُحدّد الطلبات بالنافذة المنزلقة"), "Medium • 1400", 300, 209, "AC"),
                ContestProblem("C", LocalizedText("Multi-Region Shard Rebalancer", "موزّع الأجزاء متعدد المناطق"), "Hard • 1900", 450, 64, "PENDING"),
                ContestProblem("D", LocalizedText("Zero-Downtime Schema Migration", "ترحيل قواعد البيانات دون توقف"), "Expert • 2300", 600, 19, "UNATTEMPTED")
            ),
            editorialSummary = LocalizedText(
                "Editorial unlocks when timer hits 00:00: includes lock-free ring buffer proof, PostgreSQL advisory lock trade-offs, and community Q&A.",
                "يُفتح الشرح التفصيلي فور انتهاء الوقت: يتضمن إثبات الأداء ومقارنة الحلول ونقاش المجتمع."
            )
        ),
        ContestItem(
            id = "cnt_ai_sprint_09",
            title = LocalizedText("Arabic NLP & RAG Reliability Sprint #9", "سباق موثوقية الذكاء الاصطناعي والعربية #٩"),
            organizer = "Cedars Cloud Labs",
            status = ContestStatus.UPCOMING,
            countdownLabel = LocalizedText("Starts in 2d 06h", "تبدأ بعد يومين و٦ ساعات"),
            isTeamContest = true,
            prizePool = "$2,000 + 900 Pts",
            rulesSummary = LocalizedText(
                "Teams of 2-3 build retrieval evaluation harnesses against bilingual Arabic/English hallucination benchmarks.",
                "فرق من ٢-٣ تبني أدوات تقييم دقة الاسترجاع ثنائي اللغة مع كشف الهلوسة."
            ),
            problems = listOf(
                ContestProblem("A", LocalizedText("Arabic Diacritics Normalizer", "توحيد التشكيل والنصوص العربية"), "Easy • 900", 200, 0, "LOCKED"),
                ContestProblem("B", LocalizedText("Citation Faithfulness Scorer", "مقياس مطابقة الإجابات للمصادر"), "Hard • 1800", 500, 0, "LOCKED")
            ),
            editorialSummary = LocalizedText("Editorial available after contest concludes.", "يتوفر الشرح بعد ختام المسابقة.")
        ),
        ContestItem(
            id = "cnt_mobile_perf_08",
            title = LocalizedText("Jetpack Compose 120Hz Optimization Round #8", "جولة أداء Compose ١٢٠ هرتز #٨"),
            organizer = "SkillBridge Directors",
            status = ContestStatus.ENDED,
            countdownLabel = LocalizedText("Ended • Editorial Open", "انتهت • الشرح متاح"),
            isTeamContest = false,
            prizePool = "$1,200 Distributed",
            rulesSummary = LocalizedText("Solo optimization contest focused on stability annotations, baseline profiles, and lazy list keys.", "مسابقة فردية للتحسين وتقليل إعادة التركيب."),
            problems = listOf(
                ContestProblem("A", LocalizedText("Eliminate Unstable Lambdas", "إزالة الدوال غير المستقرة"), "Easy • 900", 150, 588, "AC"),
                ContestProblem("B", LocalizedText("Canvas Telemetry Ring", "رسم مؤشرات الأداء بالـ Canvas"), "Medium • 1500", 350, 271, "AC")
            ),
            editorialSummary = LocalizedText(
                "Winner solution used immutable UI state wrappers + derivedStateOf for scroll headers, reducing recompositions by 91%.",
                "الحل الفائز اعتمد على نماذج حالة غير قابلة للتغيير وderivedStateOf مما خفّض إعادة الرسم بنسبة ٩١٪."
            )
        )
    )

    val seedSubmissions: List<ChallengeSubmission> = listOf(
        ChallengeSubmission(
            id = "seed_sub_1",
            userId = "seed",
            challengeId = "ch_smart_city",
            challengeTitle = "Smart City Analytics Dashboard",
            companyName = "Oasis Smart Mobility",
            category = "CHALLENGE",
            roleInTeam = "Data & Backend Lead",
            skillsApplied = listOf("Data Science", "Analytics", "SQL", "Kotlin"),
            repoOrDemoUrl = "https://github.com/skillbridge-demo/smart-city-telemetry",
            understandingReport = "Built sliding-window aggregation over anonymized traffic feeds with 94% test pass rate.",
            status = "VERIFIED",
            mentorFeedback = "Excellent work! Strong analytical skills and clean SQL indexing.",
            score = 94L,
            pointsAwarded = 350L,
            rewardAmount = "$450 Team Split",
            rewardStatus = "PAID",
            isPublicOnPassport = true,
            verificationCode = "SB-VER-2026-8412",
            isDemoSample = true
        ),
        ChallengeSubmission(
            id = "seed_sub_2",
            userId = "seed",
            challengeId = "ch_re_jwt_lab",
            challengeTitle = "Auth Token Rotation Protocol Audit",
            companyName = "Cedars Cloud Labs",
            category = "REVERSE_ENG",
            roleInTeam = "Security Analyst",
            skillsApplied = listOf("Reverse Engineering", "JWT Security", "Threat Modeling"),
            repoOrDemoUrl = "https://skillbridge.dev/sandbox/audit/fam_771",
            understandingReport = "Identified missing generation-counter invalidation when X-Device-Id matched; documented full token family revocation.",
            status = "VERIFIED",
            mentorFeedback = "Crisp threat report and zero false positives across all 4 sandbox test cases.",
            score = 91L,
            pointsAwarded = 380L,
            rewardAmount = "$300 Bounty",
            rewardStatus = "APPROVED",
            isPublicOnPassport = true,
            verificationCode = "SB-REV-2026-3905",
            isDemoSample = true
        )
    )

    val seedCommunityPosts: List<CommunityPost> = listOf(
        CommunityPost(
            id = "seed_post_1",
            authorId = "mentor_tariq",
            authorName = "Dr. Tariq Al-Mansoor",
            authorLevel = "Director / Leadership",
            authorVerified = true,
            postType = "Tutorial",
            title = "How to structure your Understanding Report for Reverse Engineering labs",
            body = "Reviewers look for 3 clear proofs: (1) exact state transition before/after fix, (2) boundary edge cases tested safely in sandbox, and (3) why your design prevents replay or injection.",
            tags = listOf("ReverseEng", "Security", "Rubric"),
            helpfulCount = 84L,
            commentCount = 2L,
            acceptedCommentId = "seed_c_1"
        ),
        CommunityPost(
            id = "seed_post_2",
            authorId = "user_omar",
            authorName = "Omar Al-Harbi",
            authorLevel = "Mid-level",
            authorVerified = true,
            postType = "Technical Question",
            title = "Best strategy for Idempotency Keys under PostgreSQL read-committed isolation?",
            body = "Working on NilePay Challenge #2. Should I rely on INSERT ... ON CONFLICT DO NOTHING with a unique constraint or explicit advisory locks?",
            tags = listOf("PostgreSQL", "Backend", "NilePay"),
            helpfulCount = 47L,
            commentCount = 2L,
            acceptedCommentId = "seed_c_2"
        ),
        CommunityPost(
            id = "seed_post_3",
            authorId = "user_yara",
            authorName = "Yara Khoury",
            authorLevel = "Junior",
            authorVerified = false,
            postType = "Team Ad",
            title = "Looking for 1 Data/ML & 1 QA Engineer for Smart City Analytics team",
            body = "We already have Backend & Android covered. Need someone comfortable with time-series SQL & test assertions. Fair 25% points/prize split!",
            tags = listOf("TeamUp", "DataScience", "SmartCity"),
            helpfulCount = 23L,
            commentCount = 1L
        )
    )

    val seedComments: Map<String, List<CommunityComment>> = mapOf(
        "seed_post_1" to listOf(
            CommunityComment(
                id = "seed_c_1",
                postId = "seed_post_1",
                parentCommentId = "",
                authorId = "mentor_karim",
                authorName = "Karim Darwish",
                authorLevel = "Senior",
                authorVerified = true,
                body = "Also remember: never paste live keys or production URLs—our automated secret guard will block the submission.",
                helpfulCount = 31L,
                isBestAnswer = true
            ),
            CommunityComment(
                id = "seed_c_1b",
                postId = "seed_post_1",
                parentCommentId = "seed_c_1",
                authorId = "user_nour",
                authorName = "Nour El-Din",
                authorLevel = "Junior",
                authorVerified = false,
                body = "That checklist saved my Token Rotation lab submission—passed on first review!",
                helpfulCount = 9L,
                isBestAnswer = false
            )
        ),
        "seed_post_2" to listOf(
            CommunityComment(
                id = "seed_c_2",
                postId = "seed_post_2",
                parentCommentId = "",
                authorId = "director_nadia",
                authorName = "Nadia Haddad",
                authorLevel = "Director / Leadership",
                authorVerified = true,
                body = "Use a UNIQUE(merchant_id, idempotency_key) table with status=IN_PROGRESS first, then return the cached response once COMMITTED.",
                helpfulCount = 39L,
                isBestAnswer = true
            ),
            CommunityComment(
                id = "seed_c_2b",
                postId = "seed_post_2",
                parentCommentId = "seed_c_2",
                authorId = "user_omar",
                authorName = "Omar Al-Harbi",
                authorLevel = "Mid-level",
                authorVerified = true,
                body = "Clear! Handling crash recovery via stale lease timeout makes total sense now.",
                helpfulCount = 12L,
                isBestAnswer = false
            )
        ),
        "seed_post_3" to listOf(
            CommunityComment(
                id = "seed_c_3",
                postId = "seed_post_3",
                parentCommentId = "",
                authorId = "user_ziad",
                authorName = "Ziad Mansour",
                authorLevel = "Mid-level",
                authorVerified = true,
                body = "Sent a join request in Teams Hub — I can own the QA harness and SQL benchmarks.",
                helpfulCount = 6L,
                isBestAnswer = false
            )
        )
    )

    val seedTeams: List<Team> = listOf(
        Team(
            id = "seed_team_1",
            captainId = "user_yara",
            captainName = "Yara Khoury",
            name = "Cedar Bridge Builders",
            description = "Tackling Oasis Smart City Analytics Dashboard with equal 4-way contribution split.",
            domain = "Data Science & AI",
            level = "Mid-level",
            maxMembers = 4L,
            memberIds = listOf("user_yara", "user_ziad"),
            memberNames = listOf("Yara Khoury (Captain • Android)", "Ziad Mansour (QA & Benchmarks)"),
            requiredSkills = listOf("Time-Series SQL", "Data Visualization", "Kotlin"),
            rolesNeeded = listOf("Data Scientist", "Backend API"),
            challengeTitle = "Smart City Analytics Dashboard",
            lookingForTeammates = true,
            sprintProgress = 58L
        ),
        Team(
            id = "seed_team_2",
            captainId = "user_omar",
            captainName = "Omar Al-Harbi",
            name = "Nile Distributed Squad",
            description = "Competing in MENA Distributed Systems Cup #14 and FinTech resilience challenges.",
            domain = "Backend & Cloud",
            level = "Senior",
            maxMembers = 3L,
            memberIds = listOf("user_omar", "user_salma"),
            memberNames = listOf("Omar Al-Harbi (Captain • Go/Kotlin)", "Salma Farouk (Database & Locks)"),
            requiredSkills = listOf("PostgreSQL", "Rate Limiting", "System Design"),
            rolesNeeded = listOf("Docs & Presentation", "Load Testing"),
            challengeTitle = "MENA Distributed Systems Cup #14",
            lookingForTeammates = true,
            sprintProgress = 75L
        )
    )

    val seedTeamTasks: Map<String, List<TeamTask>> = mapOf(
        "seed_team_1" to listOf(
            TeamTask(id = "t1", teamId = "seed_team_1", title = "Ingest anonymized transit stream schema", assigneeName = "Yara Khoury", role = "Captain", status = "DONE"),
            TeamTask(id = "t2", teamId = "seed_team_1", title = "Write 50k events/min load verification tests", assigneeName = "Ziad Mansour", role = "QA", status = "IN_PROGRESS"),
            TeamTask(id = "t3", teamId = "seed_team_1", title = "Build anomaly chart & accessibility labels", assigneeName = "Open Role", role = "Frontend / Data", status = "TODO")
        ),
        "seed_team_2" to listOf(
            TeamTask(id = "t4", teamId = "seed_team_2", title = "Problem A: Idempotency Deduplicator AC", assigneeName = "Omar Al-Harbi", role = "Backend", status = "DONE"),
            TeamTask(id = "t5", teamId = "seed_team_2", title = "Problem B: Sliding Window Redis Lua proof", assigneeName = "Salma Farouk", role = "Database", status = "DONE"),
            TeamTask(id = "t6", teamId = "seed_team_2", title = "Problem C: Shard Rebalancer dry-run", assigneeName = "Omar Al-Harbi", role = "Backend", status = "IN_PROGRESS")
        )
    )

    val leaderboardEntries: List<LeaderboardEntry> = listOf(
        LeaderboardEntry(1, "Laila Mansour", "Egypt", "MENA", "Data Science & AI", "Senior", 2240, 9840, +3, 18),
        LeaderboardEntry(2, "Omar Al-Harbi", "Saudi Arabia", "MENA", "Backend & Cloud", "Senior", 2195, 9520, +1, 16),
        LeaderboardEntry(3, "Hana Benali", "Morocco", "MENA", "Cybersecurity", "Mid-level", 2140, 9180, +5, 14),
        LeaderboardEntry(4, "Yousef Al-Zahrani", "Saudi Arabia", "MENA", "Backend & Cloud", "Director / Leadership", 2090, 8860, -1, 21),
        LeaderboardEntry(5, "Kareem El-Gamal", "Egypt", "MENA", "Mobile & Android", "Mid-level", 2045, 8430, +2, 12),
        LeaderboardEntry(6, "Rana Al-Suwaidi", "UAE", "MENA", "Data Science & AI", "Senior", 1998, 8190, 0, 15),
        LeaderboardEntry(7, "Ziad Mansour", "Jordan", "MENA", "Backend & Cloud", "Junior", 1940, 7720, +6, 9),
        LeaderboardEntry(8, "Elena Vasquez", "Global", "Global", "Cybersecurity", "Senior", 1925, 7610, -2, 17),
        LeaderboardEntry(9, "Nour Al-Qasem", "Jordan", "MENA", "Mobile & Android", "Junior", 1870, 7250, +4, 7),
        LeaderboardEntry(10, "Amina Haddad", "UAE", "MENA", "Product & UX", "Mid-level", 1835, 6990, +1, 11),
        LeaderboardEntry(11, "Cedar Bridge Builders", "MENA", "MENA", "Data Science & AI", "Mid-level", 2110, 8950, +4, 10, isTeam = true),
        LeaderboardEntry(12, "Nile Distributed Squad", "Egypt", "MENA", "Backend & Cloud", "Senior", 2180, 9340, +2, 13, isTeam = true)
    )

    val companies: List<CompanyProfile> = listOf(
        CompanyProfile(
            id = "comp_oasis",
            name = "Oasis Smart Mobility",
            city = "Dubai, UAE",
            domain = "Data Science & AI",
            tier = VerificationTier.TRUSTED,
            verificationReason = LocalizedText(
                "Verified trade license, audited escrow prize pool, and 18 mentor-verified challenge rounds.",
                "سجل تجاري موثّق، ميزانية مكافآت محجوزة مسبقًا، و١٨ جولة تحديات معتمدة."
            ),
            verifiedDate = "2026-09-12",
            publishedChallenges = 14,
            completionRate = 89,
            avgMentorRating = 4.9,
            rewardPolicy = LocalizedText("Escrow released within 7 days of rubric sign-off.", "تُصرف المكافآت خلال ٧ أيام من اعتماد التقييم."),
            ipPolicy = LocalizedText("Learners keep full portfolio showcase rights.", "يحتفظ المتنافس بحق عرض مشروعه في السجل.")
        ),
        CompanyProfile(
            id = "comp_nilepay",
            name = "NilePay Fintech",
            city = "Cairo, Egypt",
            domain = "Backend & Cloud",
            tier = VerificationTier.BUSINESS,
            verificationReason = LocalizedText(
                "Corporate domain & legal entity verified; active engineering director supervision.",
                "شركة تقنية مالية موثّقة بإشراف مباشر من مدراء الهندسة."
            ),
            verifiedDate = "2026-10-03",
            publishedChallenges = 9,
            completionRate = 84,
            avgMentorRating = 4.8,
            rewardPolicy = LocalizedText("Top 3 cash split + participation points for all passing tests.", "جوائز مالية لأول ٣ مراكز ونقاط مشاركة لكل مجتاز."),
            ipPolicy = LocalizedText("MIT open showcase unless explicit paid extension signed.", "عرض مفتوح المصدر ما لم يُوقَّع ملحق تعاقدي مدفوع.")
        ),
        CompanyProfile(
            id = "comp_startup_sandbox",
            name = "Maghreb Logistics Beta",
            city = "Casablanca, Morocco",
            domain = "Mobile & Android",
            tier = VerificationTier.BASIC,
            verificationReason = LocalizedText(
                "Domain email verified; eligible for unpaid practice challenges only until business audit.",
                "توثيق بريد إلكتروني أساسي فقط — مسموح بنشر تحديات تدريبية بدون جوائز مالية قبل التوثيق التجاري."
            ),
            verifiedDate = "2026-11-01",
            publishedChallenges = 2,
            completionRate = 76,
            avgMentorRating = 4.5,
            rewardPolicy = LocalizedText("Verified Points & Mentor Feedback only (no cash pool).", "نقاط موثّقة وتقييم مرشد فقط بدون مكافأة مالية."),
            ipPolicy = LocalizedText("Full IP remains with contributor.", "الملكية الفكرية كاملة للمشارك.")
        )
    )

    val notifications: List<EcosystemNotification> = listOf(
        EcosystemNotification(
            id = "n1",
            category = "Evaluation",
            title = LocalizedText("Verified Experience Approved (+350 pts)", "اعتماد خبرة عملية موثّقة (+٣٥٠ نقطة)"),
            body = LocalizedText("Dr. Tariq verified Smart City Analytics Dashboard (Score 94%).", "اعتمد د. طارق تسليم لوحة المدينة الذكية بنسبة ٩٤٪."),
            timeAgo = "12m",
            unread = true
        ),
        EcosystemNotification(
            id = "n2",
            category = "Contests",
            title = LocalizedText("MENA Distributed Cup #14 is Live!", "كأس الأنظمة الموزعة #١٤ مباشر الآن!"),
            body = LocalizedText("Problem C unlocked — 2h 18m remaining.", "المسألة C متاحة الآن — متبقي ساعتان و١٨ دقيقة."),
            timeAgo = "38m",
            unread = true
        ),
        EcosystemNotification(
            id = "n3",
            category = "Teams",
            title = LocalizedText("New Teammate Match for Cedar Bridge Builders", "مطابقة عضو جديد لفريق Cedar Bridge"),
            body = LocalizedText("Ziad Mansour matched 91% with your QA & SQL criteria.", "زياد منصور حقق تطابق ٩١٪ مع مهارات فريقك."),
            timeAgo = "2h",
            unread = false
        ),
        EcosystemNotification(
            id = "n4",
            category = "Rewards",
            title = LocalizedText("Bounty Approved: Auth Token Rotation Lab", "اعتماد مكافأة مختبر تدوير التوكن"),
            body = LocalizedText("$300 bounty moved to Approved status.", "تمت ترقية المكافأة ٣٠٠$ إلى حالة معتمد."),
            timeAgo = "1d",
            unread = false
        )
    )

    val walletTransactions: List<RewardTransaction> = listOf(
        RewardTransaction(
            id = "tx1",
            title = LocalizedText("Smart City Analytics Dashboard (Team Split)", "لوحة المدينة الذكية (حصة الفريق)"),
            points = 350,
            cashAmount = "$450",
            status = "PAID",
            dateLabel = "Nov 10, 2026"
        ),
        RewardTransaction(
            id = "tx2",
            title = LocalizedText("Auth Token Rotation Protocol Audit", "تحليل بروتوكول تدوير التوكن"),
            points = 380,
            cashAmount = "$300",
            status = "APPROVED",
            dateLabel = "Nov 12, 2026"
        ),
        RewardTransaction(
            id = "tx3",
            title = LocalizedText("MENA Distributed Cup #14 — Live Standing", "كأس الأنظمة الموزعة #١٤"),
            points = 450,
            cashAmount = "$250 est.",
            status = "UNDER_REVIEW",
            dateLabel = "Nov 15, 2026"
        ),
        RewardTransaction(
            id = "tx4",
            title = LocalizedText("Community Best Answer Streak Bonus", "مكافأة أفضل إجابات المجتمع"),
            points = 100,
            cashAmount = "—",
            status = "PAID",
            dateLabel = "Nov 08, 2026"
        )
    )

    val evidenceNodes: List<EvidenceGraphNode> = listOf(
        EvidenceGraphNode(
            skill = "Time-Series SQL & Analytics",
            level = "Senior • 95%",
            proofTitle = LocalizedText("Smart City Analytics Dashboard", "لوحة المدينة الذكية"),
            evaluator = "Dr. Tariq Al-Mansoor (Oasis Mobility)",
            verificationId = "SB-VER-2026-8412",
            isVerified = true
        ),
        EvidenceGraphNode(
            skill = "Reverse Engineering & JWT Security",
            level = "Mid-level • 91%",
            proofTitle = LocalizedText("Auth Token Rotation Protocol Audit", "تحليل بروتوكول تدوير التوكن"),
            evaluator = "Karim Darwish (Cedars Cloud Labs)",
            verificationId = "SB-REV-2026-3905",
            isVerified = true
        ),
        EvidenceGraphNode(
            skill = "Distributed Rate Limiting",
            level = "Developing • 74%",
            proofTitle = LocalizedText("MENA Cup #14 — Problem B Accepted", "كأس الأنظمة الموزعة — المسألة B"),
            evaluator = "Automated Contest Judge + Rubric",
            verificationId = "SB-CNT-2026-1402",
            isVerified = true
        ),
        EvidenceGraphNode(
            skill = "Zero-Downtime Migrations",
            level = "Target Gap • 40%",
            proofTitle = LocalizedText("Self-Claimed • Pending Challenge Proof", "مُصرّح ذاتيًا • بانتظار دليل عملي"),
            evaluator = "Unverified until Challenge C or Lab completed",
            verificationId = "SELF-CLAIMED",
            isVerified = false
        )
    )
}
