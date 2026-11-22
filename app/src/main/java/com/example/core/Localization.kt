package com.example.core

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage { EN, AR }

enum class AppRoleMode(val id: String) {
    LEARNER("LEARNER"),
    MENTOR("MENTOR"),
    DIRECTOR("DIRECTOR"),
    COMPANY("COMPANY"),
    ADMIN("ADMIN")
}

data class LocalizedText(
    val en: String,
    val ar: String
) {
    fun get(lang: AppLanguage): String = if (lang == AppLanguage.AR) ar else en
}

data class AppStrings(
    val appName: String,
    val tagline: String,
    val welcomeTitle: String,
    val welcomeSubtitle: String,
    val getStarted: String,
    val signInGoogle: String,
    val continueAsGuest: String,
    val guestBannerTitle: String,
    val guestLockedReason: String,
    val languageToggleLabel: String,

    // Bottom Nav & Common Actions
    val navHome: String,
    val navDiscover: String,
    val navCommunity: String,
    val navTeams: String,
    val navPassport: String,
    val backLabel: String,
    val closeLabel: String,
    val cancelLabel: String,
    val confirmLabel: String,
    val saveLabel: String,
    val offlineBannerText: String,
    val demoCatalogLabel: String,

    // Roles & Levels
    val roleLearner: String,
    val roleMentor: String,
    val roleDirector: String,
    val roleCompany: String,
    val roleAdmin: String,
    val levelJunior: String,
    val levelMid: String,
    val levelSenior: String,
    val levelDirector: String,
    val levelDisclaimer: String,
    val planStarterFree: String,
    val planGrowthPaid: String,
    val planProPaid: String,
    val planEnterprise: String,

    // Core Loop
    val loopTitle: String,
    val loopSteps: List<String>,

    // Home & Analysis
    val yourSkillMatch: String,
    val skillAnalysisTitle: String,
    val skillOverview: String,
    val techSkills: String,
    val softSkills: String,
    val domainKnowledge: String,
    val problemSolving: String,
    val commCollab: String,
    val aiGapAnalyzer: String,
    val aiExplainTitle: String,
    val appealOrAdjust: String,
    val proveThisSkill: String,
    val recommendedNextStep: String,
    val activeChallenges: String,
    val pointsLabel: String,
    val streakLabel: String,

    // Badges & Progression
    val badgesTitle: String,
    val unlockedBadgesLabel: String,
    val lockedBadgesLabel: String,
    val auditLogTitle: String,

    // AI Agent
    val aiAgentTitle: String,
    val aiAgentAskHint: String,
    val aiAgentDataUsedPrefix: String,
    val aiAgentBoundariesPrefix: String,
    val aiAgentUnavailable: String,
    val aiAgentConfirmActionTitle: String,
    val aiAgentReportLabel: String,
    val aiAgentPersonalizationToggle: String,
    val aiAgentContextToggle: String,

    // Discover & Challenges
    val discoverTitle: String,
    val filterAll: String,
    val filterChallenges: String,
    val filterProjects: String,
    val filterContests: String,
    val filterReverseEng: String,
    val filterMentors: String,
    val filterTeams: String,
    val filterOpportunities: String,
    val projectDetails: String,
    val applyNow: String,
    val saveForLater: String,
    val savedLabel: String,
    val rubricBeforeStart: String,
    val prizePoolDisclaimer: String,
    val ipRightsTitle: String,
    val aiPolicyTitle: String,
    val launchWorkSim: String,
    val submitSolution: String,

    // Contests & Reverse Engineering
    val contestsTitle: String,
    val liveNow: String,
    val upcoming: String,
    val ended: String,
    val editorialTab: String,
    val standingsTab: String,
    val problemsTab: String,
    val reverseEngTitle: String,
    val authorizedScopeBadge: String,
    val allowedVsProhibited: String,
    val understandingReportLabel: String,
    val secretLeakWarning: String,

    // Community
    val communityTitle: String,
    val sortTrending: String,
    val sortNew: String,
    val sortTop: String,
    val sortUnanswered: String,
    val newPost: String,
    val bestAnswer: String,
    val acceptAnswer: String,
    val reportContent: String,
    val verifiedBadge: String,
    val selfClaimedBadge: String,
    val replyLabel: String,
    val helpfulLabel: String,

    // Teams
    val teamsTitle: String,
    val myTeams: String,
    val recruitingTeams: String,
    val createTeam: String,
    val joinTeam: String,
    val sprintBoard: String,
    val rewardSplitTitle: String,
    val teamChat: String,
    val membersLabel: String,
    val needRolePrefix: String,

    // Leaderboards & Passport
    val leaderboardTitle: String,
    val top100Title: String,
    val ratingFormulaTitle: String,
    val passportTitle: String,
    val verifiedExperience: String,
    val mentorFeedback: String,
    val sharePassport: String,
    val skillEvidenceGraph: String,
    val walletTitle: String,

    // Utilities, Account Settings & Governance
    val searchPlaceholder: String,
    val notificationsTitle: String,
    val companyDashTitle: String,
    val mentorDashTitle: String,
    val directorAdminTitle: String,
    val privacySettingsTitle: String,
    val accountSettingsTitle: String,
    val deleteAccountTitle: String,
    val deleteAccountBody: String,
    val deleteAccountAction: String,
    val blueprintTitle: String,
    val statePreviewTitle: String,
    val loadingText: String,
    val emptyTitle: String,
    val emptyAction: String,
    val errorRetry: String,
    val lockedTitle: String,
    val signOut: String
)

val EnStrings = AppStrings(
    appName = "SkillBridge",
    tagline = "BRIDGE YOUR SKILLS TO GREATER OPPORTUNITIES",
    welcomeTitle = "Welcome to SkillBridge",
    welcomeSubtitle = "Bridge your skills to real-world challenges, team sprints, ethical reverse engineering, and verified experience.",
    getStarted = "Get Started",
    signInGoogle = "Continue with Google",
    continueAsGuest = "Continue as Guest (Preview)",
    guestBannerTitle = "Guest Preview Mode — Sign in with Google to save progress, earn badges & submit solutions.",
    guestLockedReason = "This private action requires signing in with Google so your verified evidence stays safe.",
    languageToggleLabel = "عربي",

    navHome = "Home",
    navDiscover = "Discover",
    navCommunity = "Community",
    navTeams = "Teams",
    navPassport = "Passport",
    backLabel = "Back",
    closeLabel = "Close",
    cancelLabel = "Cancel",
    confirmLabel = "Confirm",
    saveLabel = "Save",
    offlineBannerText = "Offline mode — showing cached data. Cloud sync resumes when connected.",
    demoCatalogLabel = "Demo Reference",

    roleLearner = "Learner",
    roleMentor = "Mentor",
    roleDirector = "Director",
    roleCompany = "Company",
    roleAdmin = "Admin / Mod",
    levelJunior = "Junior",
    levelMid = "Mid-level",
    levelSenior = "Senior",
    levelDirector = "Director / Leadership",
    levelDisclaimer = "Levels reflect verified practical evidence, never paid subscription tiers.",
    planStarterFree = "Starter — Free",
    planGrowthPaid = "Growth — Paid",
    planProPaid = "Professional+ — Paid",
    planEnterprise = "Company / Enterprise",

    loopTitle = "Verified Career Loop",
    loopSteps = listOf("Skill Gap", "Learning", "Practice", "Evaluation", "Evidence", "Opportunity"),

    yourSkillMatch = "Your Skills Match Score",
    skillAnalysisTitle = "Skill Analysis",
    skillOverview = "Skill Overview",
    techSkills = "Technical Skills",
    softSkills = "Soft Skills",
    domainKnowledge = "Domain Knowledge",
    problemSolving = "Problem Solving",
    commCollab = "Communication & Collab",
    aiGapAnalyzer = "AI Skill Gap Analyzer",
    aiExplainTitle = "Why SkillBridge AI Recommended This",
    appealOrAdjust = "Appeal / Edit Skills",
    proveThisSkill = "Prove This Skill",
    recommendedNextStep = "Recommended Next Step",
    activeChallenges = "Active Challenges & Deadlines",
    pointsLabel = "Points",
    streakLabel = "Day Streak",

    badgesTitle = "SkillBridge Badges",
    unlockedBadgesLabel = "Unlocked",
    lockedBadgesLabel = "Locked",
    auditLogTitle = "Verification & Activity Log",

    aiAgentTitle = "SkillBridge AI Agent",
    aiAgentAskHint = "Ask for hints, sprint breakdown, rubric checks, or gap review...",
    aiAgentDataUsedPrefix = "Data Used:",
    aiAgentBoundariesPrefix = "Boundaries:",
    aiAgentUnavailable = "AI service is currently unavailable or offline. Verify connection and retry.",
    aiAgentConfirmActionTitle = "Confirm Agent Action",
    aiAgentReportLabel = "Report AI Response",
    aiAgentPersonalizationToggle = "Personalized AI Guidance",
    aiAgentContextToggle = "Share Active Screen Context with AI",

    discoverTitle = "Discover Hub",
    filterAll = "All",
    filterChallenges = "Challenges",
    filterProjects = "Projects",
    filterContests = "Contests",
    filterReverseEng = "Reverse Eng",
    filterMentors = "Mentors",
    filterTeams = "Teams",
    filterOpportunities = "Roles",
    projectDetails = "Project Details",
    applyNow = "Apply Now",
    saveForLater = "Save for Later",
    savedLabel = "Saved",
    rubricBeforeStart = "Evaluation Rubric (Disclosed Before Starting)",
    prizePoolDisclaimer = "Rewards depend on published rubric rank & compliance verification.",
    ipRightsTitle = "IP & Output Rights",
    aiPolicyTitle = "AI Usage Policy",
    launchWorkSim = "AI Work Simulation",
    submitSolution = "Submit Solution",

    contestsTitle = "SkillBridge Contests",
    liveNow = "Live",
    upcoming = "Upcoming",
    ended = "Ended",
    editorialTab = "Editorial",
    standingsTab = "Standings",
    problemsTab = "Problems",
    reverseEngTitle = "Reverse Engineering Hub",
    authorizedScopeBadge = "Ethical / Authorized Scope",
    allowedVsProhibited = "Allowed vs Prohibited Rules",
    understandingReportLabel = "Mandatory Understanding & Verification Report",
    secretLeakWarning = "Secret/API key detected in submission (e.g., AIza, sk-, Bearer, PRIVATE KEY). Remove credentials before submitting.",

    communityTitle = "Community",
    sortTrending = "Trending",
    sortNew = "Newest",
    sortTop = "Most Helpful",
    sortUnanswered = "Unanswered",
    newPost = "Ask / Share",
    bestAnswer = "Best Answer",
    acceptAnswer = "Mark Best Answer",
    reportContent = "Report",
    verifiedBadge = "Verified Skill",
    selfClaimedBadge = "Self-Claimed",
    replyLabel = "Reply",
    helpfulLabel = "helpful",

    teamsTitle = "Teams & Sprints",
    myTeams = "My Teams",
    recruitingTeams = "Recruiting",
    createTeam = "Create Team",
    joinTeam = "Join Team",
    sprintBoard = "Sprint Board",
    rewardSplitTitle = "Transparent Points & Reward Split",
    teamChat = "Team Feed",
    membersLabel = "Members",
    needRolePrefix = "Need:",

    leaderboardTitle = "Leaderboards",
    top100Title = "Top 100 Rankings",
    ratingFormulaTitle = "Transparent Rating & Points Formula",
    passportTitle = "Experience Passport",
    verifiedExperience = "Verified Experience",
    mentorFeedback = "Mentor Feedback",
    sharePassport = "Share Passport",
    skillEvidenceGraph = "Skill Evidence Graph",
    walletTitle = "Points & Rewards Ledger",

    searchPlaceholder = "Search skills, challenges, contests, teams, people...",
    notificationsTitle = "Notifications",
    companyDashTitle = "Company Partner Hub",
    mentorDashTitle = "Mentor Review Studio",
    directorAdminTitle = "Director & Moderation",
    privacySettingsTitle = "Privacy & UI States",
    accountSettingsTitle = "Account & Session Settings",
    deleteAccountTitle = "Delete Account & Data",
    deleteAccountBody = "Permanently deletes your SkillBridge profile, goals, submissions, badges, and ledger records from Firestore. This cannot be undone.",
    deleteAccountAction = "Delete My Account",
    blueprintTitle = "Product Architecture & MVP",
    statePreviewTitle = "UI State Inspector",
    loadingText = "Syncing verified SkillBridge data...",
    emptyTitle = "Nothing here yet",
    emptyAction = "Explore Challenges",
    errorRetry = "Retry",
    lockedTitle = "Milestone Locked",
    signOut = "Sign Out"
)

val ArStrings = AppStrings(
    appName = "SkillBridge",
    tagline = "اربط مهاراتك بفرص مهنية حقيقية",
    welcomeTitle = "مرحبًا بك في SkillBridge",
    welcomeSubtitle = "انتقل من التعلم إلى التطبيق العملي، العمل الجماعي، الهندسة العكسية الأخلاقية، وسجل الخبرة الموثّق.",
    getStarted = "ابدأ الآن",
    signInGoogle = "المتابعة عبر Google",
    continueAsGuest = "معاينة كزائر (بدون حفظ)",
    guestBannerTitle = "وضع المعاينة للزائر — سجّل بحساب Google لحفظ تقدّمك وكسب الشارات ورفع الحلول.",
    guestLockedReason = "يتطلب هذا الإجراء الخاص تسجيل الدخول بحساب Google لحفظ أدلتك الموثّقة بأمان.",
    languageToggleLabel = "EN",

    navHome = "الرئيسية",
    navDiscover = "استكشف",
    navCommunity = "المجتمع",
    navTeams = "الفرق",
    navPassport = "السجل",
    backLabel = "رجوع",
    closeLabel = "إغلاق",
    cancelLabel = "إلغاء",
    confirmLabel = "تأكيد",
    saveLabel = "حفظ",
    offlineBannerText = "أنت غير متصل حاليًا — يتم عرض البيانات المحفوظة، وستُزامن التغييرات عند عودة الاتصال.",
    demoCatalogLabel = "نموذج مرجعي",

    roleLearner = "متعلم / مطور",
    roleMentor = "مرشد",
    roleDirector = "قائد / Director",
    roleCompany = "شركة",
    roleAdmin = "مشرف / إدارة",
    levelJunior = "Junior • مبتدئ",
    levelMid = "Mid-level • متوسط",
    levelSenior = "Senior • خبير",
    levelDirector = "Director • قيادي",
    levelDisclaimer = "المستوى المهني يُقاس بالأدلة العملية والتقييمات الموثّقة وليس باقات مدفوعة.",
    planStarterFree = "Starter — مجانية",
    planGrowthPaid = "Growth — مدفوعة",
    planProPaid = "Professional+ — مدفوعة",
    planEnterprise = "شركات ومؤسسات",

    loopTitle = "حلقة الخبرة الموثّقة",
    loopSteps = listOf("فجوة المهارات", "التعلم", "التطبيق العملي", "التقييم", "دليل موثّق", "الفرصة"),

    yourSkillMatch = "نسبة تطابق مهاراتك",
    skillAnalysisTitle = "تحليل المهارات",
    skillOverview = "نظرة عامة على المهارات",
    techSkills = "المهارات التقنية",
    softSkills = "المهارات الشخصية",
    domainKnowledge = "المعرفة بالمجال",
    problemSolving = "حل المشكلات",
    commCollab = "التواصل والعمل الجماعي",
    aiGapAnalyzer = "محلل الفجوات الذكي",
    aiExplainTitle = "لماذا أوصى الذكاء الاصطناعي بهذا المسار؟",
    appealOrAdjust = "اعتراض / تعديل المهارات",
    proveThisSkill = "أثبت هذه المهارة عمليًا",
    recommendedNextStep = "الخطوة الموصى بها الآن",
    activeChallenges = "التحديات والمواعيد النشطة",
    pointsLabel = "نقطة",
    streakLabel = "أيام متتالية",

    badgesTitle = "شارات SkillBridge",
    unlockedBadgesLabel = "مفتوحة",
    lockedBadgesLabel = "مقفلة",
    auditLogTitle = "سجل التوثيق والنشاط",

    aiAgentTitle = "المساعد الذكي SkillBridge",
    aiAgentAskHint = "اسأل عن تلميح تقني، تقسيم مهام الفريق، أو مراجعة معايير التقييم...",
    aiAgentDataUsedPrefix = "البيانات المستخدمة:",
    aiAgentBoundariesPrefix = "حدود المساعد:",
    aiAgentUnavailable = "خدمة الذكاء الاصطناعي غير متاحة حاليًا أو لا يوجد اتصال. تحقّق من الشبكة وأعد المحاولة.",
    aiAgentConfirmActionTitle = "تأكيد إجراء المساعد الذكي",
    aiAgentReportLabel = "الإبلاغ عن رد AI",
    aiAgentPersonalizationToggle = "تخصيص الذكاء الاصطناعي حسب مستواك",
    aiAgentContextToggle = "مشاركة سياق الشاشة الحالية مع AI",

    discoverTitle = "مركز الاستكشاف",
    filterAll = "الكل",
    filterChallenges = "تحديات",
    filterProjects = "مشاريع",
    filterContests = "مسابقات",
    filterReverseEng = "هندسة عكسية",
    filterMentors = "مرشدون",
    filterTeams = "فرق",
    filterOpportunities = "فرص",
    projectDetails = "تفاصيل المشروع",
    applyNow = "قدّم الآن",
    saveForLater = "حفظ للاحقًا",
    savedLabel = "تم الحفظ",
    rubricBeforeStart = "معايير التقييم المعلنة قبل البدء",
    prizePoolDisclaimer = "تُوزع المكافآت والنقاط حسب الترتيب ومعايير المراجعة الموثقة.",
    ipRightsTitle = "حقوق الملكية الفكرية",
    aiPolicyTitle = "سياسة استخدام الذكاء الاصطناعي",
    launchWorkSim = "محاكاة بيئة العمل AI",
    submitSolution = "رفع التسليم",

    contestsTitle = "مسابقات SkillBridge",
    liveNow = "مباشرة الآن",
    upcoming = "قادمة",
    ended = "منتهية",
    editorialTab = "الشرح والحلول",
    standingsTab = "الترتيب المباشر",
    problemsTab = "المسائل",
    reverseEngTitle = "مركز الهندسة العكسية الأخلاقية",
    authorizedScopeBadge = "نطاق آمن ومصرّح به",
    allowedVsProhibited = "المسموح والممنوع أخلاقيًا وقانونيًا",
    understandingReportLabel = "تقرير الفهم والتحقق الإلزامي",
    secretLeakWarning = "تم رصد مفتاح سري أو Token في التسليم (مثل AIza أو sk- أو Bearer). احذفه قبل الإرسال.",

    communityTitle = "المجتمع",
    sortTrending = "الرائج",
    sortNew = "الأحدث",
    sortTop = "الأكثر فائدة",
    sortUnanswered = "بلا إجابة",
    newPost = "اسأل / شارك",
    bestAnswer = "أفضل إجابة",
    acceptAnswer = "تعيين كأفضل إجابة",
    reportContent = "إبلاغ",
    verifiedBadge = "مهارة موثّقة",
    selfClaimedBadge = "مُصرّح بها ذاتيًا",
    replyLabel = "رد",
    helpfulLabel = "مفيد",

    teamsTitle = "الفرق والسباقات",
    myTeams = "فرقي",
    recruitingTeams = "تبحث عن أعضاء",
    createTeam = "إنشاء فريق",
    joinTeam = "انضمام للفريق",
    sprintBoard = "لوحة المهام",
    rewardSplitTitle = "تقسيم النقاط والمكافآت بشفافية",
    teamChat = "ساحة الفريق",
    membersLabel = "أعضاء",
    needRolePrefix = "مطلوب:",

    leaderboardTitle = "لوحات التصنيف",
    top100Title = "أفضل 100 متنافس",
    ratingFormulaTitle = "معادلة احتساب التقييم والنقاط",
    passportTitle = "سجل الخبرة الموثّق",
    verifiedExperience = "خبرة عملية موثّقة",
    mentorFeedback = "تقييم المرشد",
    sharePassport = "مشاركة السجل",
    skillEvidenceGraph = "خريطة أدلة المهارات",
    walletTitle = "سجل النقاط والمكافآت",

    searchPlaceholder = "ابحث عن مهارة، تحدٍ، مسابقة، فريق، شركة...",
    notificationsTitle = "الإشعارات",
    companyDashTitle = "بوابة الشركات الموثّقة",
    mentorDashTitle = "استوديو تقييم المرشد",
    directorAdminTitle = "إدارة القادة والإشراف",
    privacySettingsTitle = "الخصوصية وحالات الواجهة",
    accountSettingsTitle = "إعدادات الحساب والجلسة",
    deleteAccountTitle = "حذف الحساب والبيانات",
    deleteAccountBody = "سيتم حذف ملفك المهني، أهدافك، تسليماتك، شاراتك، وسجل النقاط من Firestore بشكل نهائي ولا يمكن التراجع.",
    deleteAccountAction = "حذف حسابي نهائيًا",
    blueprintTitle = "خارطة المنتج والـ MVP",
    statePreviewTitle = "معاينة حالات الشاشة",
    loadingText = "جارٍ مزامنة بياناتك الموثّقة...",
    emptyTitle = "لا توجد عناصر هنا بعد",
    emptyAction = "استكشف التحديات",
    errorRetry = "إعادة المحاولة",
    lockedTitle = "عنصر مقفل حتى استيفاء المتطلب",
    signOut = "تسجيل الخروج"
)

val LocalAppStrings = staticCompositionLocalOf { EnStrings }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.EN }

fun AppLanguage.layoutDirection(): LayoutDirection =
    if (this == AppLanguage.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

fun localizedDomain(domain: String, lang: AppLanguage): String {
    if (lang == AppLanguage.EN) return domain
    return when (domain) {
        "All" -> "الكل"
        "Backend & Cloud" -> "الأنظمة الخلفية والسحابة"
        "Data Science & AI" -> "علوم البيانات والذكاء الاصطناعي"
        "Mobile & Android" -> "تطبيقات الموبايل والأندرويد"
        "Cybersecurity" -> "الأمن السيبراني"
        "Product & UX" -> "المنتجات وتجربة المستخدم"
        else -> domain
    }
}

fun localizedCareerLevel(level: String, lang: AppLanguage): String {
    if (lang == AppLanguage.EN) return level
    return when (level) {
        "Junior" -> "مبتدئ (Junior)"
        "Mid-level" -> "متوسط (Mid-level)"
        "Senior" -> "خبير (Senior)"
        "Director / Leadership" -> "قيادي (Director)"
        else -> level
    }
}

fun localizedSubmissionStatus(status: String, lang: AppLanguage): String {
    if (lang == AppLanguage.EN) return status.replace("_", " ")
    return when (status) {
        "PENDING" -> "قيد الانتظار"
        "UNDER_REVIEW" -> "قيد التقييم"
        "VERIFIED" -> "موثّق"
        "REJECTED" -> "مرفوض"
        "DISPUTED" -> "قيد المراجعة / اعتراض"
        "APPROVED" -> "معتمد"
        "PAID" -> "تم الصرف"
        else -> status
    }
}

fun localizedPostType(postType: String, lang: AppLanguage): String {
    if (lang == AppLanguage.EN) return postType
    return when (postType) {
        "Technical Question" -> "سؤال تقني"
        "Help Request" -> "طلب مساعدة"
        "Tutorial" -> "شرح تعليمي"
        "Team Ad" -> "إعلان فريق"
        "Code Review" -> "مراجعة كود"
        "Post-Contest Solution" -> "حل بعد المسابقة"
        "Suggest Challenge" -> "اقتراح تحدٍ"
        else -> postType
    }
}

fun localizedLeaderboardScope(scope: String, lang: AppLanguage): String {
    if (lang == AppLanguage.EN) return scope
    return when (scope) {
        "Global" -> "العالمي"
        "MENA" -> "الشرق الأوسط وشمال أفريقيا"
        "Egypt" -> "مصر"
        "Saudi Arabia" -> "السعودية"
        "UAE" -> "الإمارات"
        "Jordan" -> "الأردن"
        "Morocco" -> "المغرب"
        "Junior" -> "مبتدئ"
        "Mid-level" -> "متوسط"
        "Senior" -> "خبير"
        "Teams" -> "الفرق"
        "Contests" -> "المسابقات"
        "Community" -> "المجتمع"
        else -> scope
    }
}

fun localizedNotificationCategory(category: String, lang: AppLanguage): String {
    if (lang == AppLanguage.EN) return category
    return when (category) {
        "All" -> "الكل"
        "Evaluation" -> "التقييمات"
        "Contests" -> "المسابقات"
        "Teams" -> "الفرق"
        "Rewards" -> "المكافآت"
        else -> category
    }
}

fun localizedChannelName(channel: String, lang: AppLanguage): String {
    if (lang == AppLanguage.EN) return channel
    return when (channel) {
        "In-App" -> "تنبيهات داخل التطبيق"
        "Push" -> "الإشعارات الفورية"
        "Email Digest" -> "ملخص البريد الإلكتروني"
        "Team Mentions" -> "إشارات الفريق"
        else -> channel
    }
}

fun localizedTaskStatus(status: String, lang: AppLanguage): String {
    if (lang == AppLanguage.EN) return status.replace("_", " ")
    return when (status) {
        "TODO" -> "قيد الانتظار"
        "IN_PROGRESS" -> "قيد التنفيذ"
        "DONE" -> "مكتمل"
        else -> status
    }
}
