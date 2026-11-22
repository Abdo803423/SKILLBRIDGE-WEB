import type { AppLanguage, LocalizedText } from '../types/skillbridge';

export interface AppStrings {
  appName: string;
  tagline: string;
  welcomeTitle: string;
  welcomeSubtitle: string;
  getStarted: string;
  signInGoogle: string;
  continueAsGuest: string;
  guestBannerTitle: string;
  guestLockedReason: string;
  languageToggleLabel: string;

  navHome: string;
  navDiscover: string;
  navCommunity: string;
  navTeams: string;
  navPassport: string;
  backLabel: string;
  closeLabel: string;
  cancelLabel: string;
  confirmLabel: string;
  saveLabel: string;
  offlineBannerText: string;
  demoCatalogLabel: string;

  roleLearner: string;
  roleMentor: string;
  roleDirector: string;
  roleCompany: string;
  roleAdmin: string;
  levelJunior: string;
  levelMid: string;
  levelSenior: string;
  levelDirector: string;
  levelDisclaimer: string;
  planStarterFree: string;
  planGrowthPaid: string;
  planProPaid: string;
  planEnterprise: string;

  loopTitle: string;
  loopSteps: string[];

  yourSkillMatch: string;
  skillAnalysisTitle: string;
  skillOverview: string;
  techSkills: string;
  softSkills: string;
  domainKnowledge: string;
  problemSolving: string;
  commCollab: string;
  aiGapAnalyzer: string;
  aiExplainTitle: string;
  appealOrAdjust: string;
  proveThisSkill: string;
  recommendedNextStep: string;
  activeChallenges: string;
  pointsLabel: string;
  streakLabel: string;

  badgesTitle: string;
  unlockedBadgesLabel: string;
  lockedBadgesLabel: string;
  auditLogTitle: string;

  aiAgentTitle: string;
  aiAgentAskHint: string;
  aiAgentDataUsedPrefix: string;
  aiAgentBoundariesPrefix: string;
  aiAgentUnavailable: string;
  aiAgentConfirmActionTitle: string;
  aiAgentReportLabel: string;
  aiAgentPersonalizationToggle: string;
  aiAgentContextToggle: string;

  discoverTitle: string;
  filterAll: string;
  filterChallenges: string;
  filterProjects: string;
  filterContests: string;
  filterReverseEng: string;
  filterMentors: string;
  filterTeams: string;
  filterOpportunities: string;
  projectDetails: string;
  applyNow: string;
  saveForLater: string;
  savedLabel: string;
  rubricBeforeStart: string;
  prizePoolDisclaimer: string;
  ipRightsTitle: string;
  aiPolicyTitle: string;
  launchWorkSim: string;
  submitSolution: string;

  contestsTitle: string;
  liveNow: string;
  upcoming: string;
  ended: string;
  editorialTab: string;
  standingsTab: string;
  problemsTab: string;
  reverseEngTitle: string;
  authorizedScopeBadge: string;
  allowedVsProhibited: string;
  understandingReportLabel: string;
  secretLeakWarning: string;

  communityTitle: string;
  sortTrending: string;
  sortNew: string;
  sortTop: string;
  sortUnanswered: string;
  newPost: string;
  bestAnswer: string;
  acceptAnswer: string;
  reportContent: string;
  verifiedBadge: string;
  selfClaimedBadge: string;
  replyLabel: string;
  helpfulLabel: string;

  teamsTitle: string;
  myTeams: string;
  recruitingTeams: string;
  createTeam: string;
  joinTeam: string;
  sprintBoard: string;
  rewardSplitTitle: string;
  teamChat: string;
  membersLabel: string;
  needRolePrefix: string;

  leaderboardTitle: string;
  top100Title: string;
  ratingFormulaTitle: string;
  passportTitle: string;
  verifiedExperience: string;
  mentorFeedback: string;
  sharePassport: string;
  skillEvidenceGraph: string;
  walletTitle: string;

  searchPlaceholder: string;
  notificationsTitle: string;
  companyDashTitle: string;
  mentorDashTitle: string;
  directorAdminTitle: string;
  privacySettingsTitle: string;
  accountSettingsTitle: string;
  deleteAccountTitle: string;
  deleteAccountBody: string;
  deleteAccountAction: string;
  blueprintTitle: string;
  statePreviewTitle: string;
  loadingText: string;
  emptyTitle: string;
  emptyAction: string;
  errorRetry: string;
  lockedTitle: string;
  signOut: string;

  // Cross-platform & Download Center
  downloadCenterTitle: string;
  useOnlineTitle: string;
  useOnlineDesc: string;
  mobileAppTitle: string;
  mobileAppDesc: string;
  desktopAppTitle: string;
  desktopAppDesc: string;
  installNowLabel: string;
  comingSoonLabel: string;
  liveSyncConnected: string;
  liveSyncReconnecting: string;
}

export const EnStrings: AppStrings = {
  appName: 'SkillBridge',
  tagline: 'BRIDGE YOUR SKILLS TO GREATER OPPORTUNITIES',
  welcomeTitle: 'Welcome to SkillBridge',
  welcomeSubtitle:
    'One unified career ecosystem across Web, Mobile, and Desktop — bridge skill gaps to real company briefs, team sprints, ethical reverse engineering, and verified experience.',
  getStarted: 'Use SkillBridge Online',
  signInGoogle: 'Continue with Google',
  continueAsGuest: 'Continue as Guest (Preview)',
  guestBannerTitle: 'Guest Preview Mode — Sign in with Google or your SkillBridge account to save progress & sync across devices.',
  guestLockedReason: 'This private action requires signing in so your verified evidence stays synchronized.',
  languageToggleLabel: 'عربي',

  navHome: 'Home',
  navDiscover: 'Discover',
  navCommunity: 'Community',
  navTeams: 'Teams',
  navPassport: 'Passport',
  backLabel: 'Back',
  closeLabel: 'Close',
  cancelLabel: 'Cancel',
  confirmLabel: 'Confirm',
  saveLabel: 'Save',
  offlineBannerText: 'Offline mode — showing cached data. Central sync resumes automatically when connected.',
  demoCatalogLabel: 'Demo Reference',

  roleLearner: 'Learner',
  roleMentor: 'Mentor',
  roleDirector: 'Director',
  roleCompany: 'Company',
  roleAdmin: 'Admin / Mod',
  levelJunior: 'Junior',
  levelMid: 'Mid-level',
  levelSenior: 'Senior',
  levelDirector: 'Director / Leadership',
  levelDisclaimer: 'Levels reflect verified practical evidence, never paid subscription tiers.',
  planStarterFree: 'Starter — Free',
  planGrowthPaid: 'Growth — Paid',
  planProPaid: 'Professional+ — Paid',
  planEnterprise: 'Company / Enterprise',

  loopTitle: 'Verified Career Loop',
  loopSteps: ['Skill Gap', 'Learning', 'Practice', 'Evaluation', 'Evidence', 'Opportunity'],

  yourSkillMatch: 'Your Skills Match Score',
  skillAnalysisTitle: 'Skill Analysis',
  skillOverview: 'Skill Overview',
  techSkills: 'Technical Skills',
  softSkills: 'Soft Skills',
  domainKnowledge: 'Domain Knowledge',
  problemSolving: 'Problem Solving',
  commCollab: 'Communication & Collab',
  aiGapAnalyzer: 'AI Skill Gap Analyzer',
  aiExplainTitle: 'Why SkillBridge AI Recommended This',
  appealOrAdjust: 'Appeal / Edit Skills',
  proveThisSkill: 'Prove This Skill',
  recommendedNextStep: 'Recommended Next Step',
  activeChallenges: 'Active Challenges & Deadlines',
  pointsLabel: 'Points',
  streakLabel: 'Day Streak',

  badgesTitle: 'SkillBridge Badges',
  unlockedBadgesLabel: 'Unlocked',
  lockedBadgesLabel: 'Locked',
  auditLogTitle: 'Verification & Activity Log',

  aiAgentTitle: 'SkillBridge AI Agent',
  aiAgentAskHint: 'Ask for hints, sprint breakdown, rubric checks, or gap review...',
  aiAgentDataUsedPrefix: 'Data Used:',
  aiAgentBoundariesPrefix: 'Boundaries:',
  aiAgentUnavailable: 'AI service is currently unavailable or offline. Verify connection and retry.',
  aiAgentConfirmActionTitle: 'Confirm Agent Action',
  aiAgentReportLabel: 'Report AI Response',
  aiAgentPersonalizationToggle: 'Personalized AI Guidance',
  aiAgentContextToggle: 'Share Active Screen Context with AI',

  discoverTitle: 'Discover Hub',
  filterAll: 'All',
  filterChallenges: 'Challenges',
  filterProjects: 'Projects',
  filterContests: 'Contests',
  filterReverseEng: 'Reverse Eng',
  filterMentors: 'Mentors',
  filterTeams: 'Teams',
  filterOpportunities: 'Roles',
  projectDetails: 'Project Details',
  applyNow: 'Apply Now',
  saveForLater: 'Save for Later',
  savedLabel: 'Saved',
  rubricBeforeStart: 'Evaluation Rubric (Disclosed Before Starting)',
  prizePoolDisclaimer: 'Rewards depend on published rubric rank & compliance verification.',
  ipRightsTitle: 'IP & Output Rights',
  aiPolicyTitle: 'AI Usage Policy',
  launchWorkSim: 'AI Work Simulation',
  submitSolution: 'Submit Solution',

  contestsTitle: 'SkillBridge Contests',
  liveNow: 'Live',
  upcoming: 'Upcoming',
  ended: 'Ended',
  editorialTab: 'Editorial',
  standingsTab: 'Standings',
  problemsTab: 'Problems',
  reverseEngTitle: 'Reverse Engineering Hub',
  authorizedScopeBadge: 'Ethical / Authorized Scope',
  allowedVsProhibited: 'Allowed vs Prohibited Rules',
  understandingReportLabel: 'Mandatory Understanding & Verification Report',
  secretLeakWarning: 'Secret/API key detected in submission (e.g., AIza, sk-, Bearer, PRIVATE KEY). Remove credentials before submitting.',

  communityTitle: 'Community',
  sortTrending: 'Trending',
  sortNew: 'Newest',
  sortTop: 'Most Helpful',
  sortUnanswered: 'Unanswered',
  newPost: 'Ask / Share',
  bestAnswer: 'Best Answer',
  acceptAnswer: 'Mark Best Answer',
  reportContent: 'Report',
  verifiedBadge: 'Verified Skill',
  selfClaimedBadge: 'Self-Claimed',
  replyLabel: 'Reply',
  helpfulLabel: 'helpful',

  teamsTitle: 'Teams & Sprints',
  myTeams: 'My Teams',
  recruitingTeams: 'Recruiting',
  createTeam: 'Create Team',
  joinTeam: 'Join Team',
  sprintBoard: 'Sprint Board',
  rewardSplitTitle: 'Transparent Points & Reward Split',
  teamChat: 'Team Feed',
  membersLabel: 'Members',
  needRolePrefix: 'Need:',

  leaderboardTitle: 'Leaderboards',
  top100Title: 'Top 100 Rankings',
  ratingFormulaTitle: 'Transparent Rating & Points Formula',
  passportTitle: 'Experience Passport',
  verifiedExperience: 'Verified Experience',
  mentorFeedback: 'Mentor Feedback',
  sharePassport: 'Share Passport',
  skillEvidenceGraph: 'Skill Evidence Graph',
  walletTitle: 'Points & Rewards Ledger',

  searchPlaceholder: 'Search skills, challenges, contests, teams, people...',
  notificationsTitle: 'Notifications',
  companyDashTitle: 'Company Partner Hub',
  mentorDashTitle: 'Mentor Review Studio',
  directorAdminTitle: 'Director & Moderation',
  privacySettingsTitle: 'Privacy & UI States',
  accountSettingsTitle: 'Account & Session Settings',
  deleteAccountTitle: 'Delete Account & Data',
  deleteAccountBody:
    'Permanently deletes your SkillBridge profile, goals, submissions, badges, and ledger records from Firestore. This cannot be undone.',
  deleteAccountAction: 'Delete My Account',
  blueprintTitle: 'Product Architecture & Ecosystem',
  statePreviewTitle: 'UI State Inspector',
  loadingText: 'Syncing verified SkillBridge data...',
  emptyTitle: 'Nothing here yet',
  emptyAction: 'Explore Challenges',
  errorRetry: 'Retry',
  lockedTitle: 'Milestone Locked',
  signOut: 'Sign Out',

  downloadCenterTitle: 'Download & Platform Access',
  useOnlineTitle: 'Use SkillBridge Online',
  useOnlineDesc: 'Instant browser access with full real-time synchronization and zero installation required.',
  mobileAppTitle: 'Download Mobile App',
  mobileAppDesc: 'Install SkillBridge on Android or iOS for thumb-friendly navigation and live notifications.',
  desktopAppTitle: 'Download Desktop App',
  desktopAppDesc: 'Install SkillBridge on Windows, macOS, or Linux for an expanded sidebar & multi-column workspace.',
  installNowLabel: 'Install App Now',
  comingSoonLabel: 'Coming Soon',
  liveSyncConnected: 'Live Sync Active',
  liveSyncReconnecting: 'Reconnecting Sync...',
};

export const ArStrings: AppStrings = {
  appName: 'SkillBridge',
  tagline: 'اربط مهاراتك بفرص مهنية حقيقية',
  welcomeTitle: 'مرحبًا بك في SkillBridge',
  welcomeSubtitle:
    'منظومة مهنية موحدة عبر الويب والموبايل وسطح المكتب — انتقل من تحليل الفجوات إلى مشاريع الشركات والتوثيق الفوري للخبرة.',
  getStarted: 'استخدم SkillBridge أونلاين',
  signInGoogle: 'المتابعة عبر Google',
  continueAsGuest: 'معاينة كزائر (بدون حفظ)',
  guestBannerTitle: 'وضع المعاينة للزائر — سجّل الدخول لحفظ تقدّمك ومزامنته عبر كل الأجهزة.',
  guestLockedReason: 'يتطلب هذا الإجراء تسجيل الدخول لحفظ ومزامنة أدلتك الموثّقة.',
  languageToggleLabel: 'EN',

  navHome: 'الرئيسية',
  navDiscover: 'استكشف',
  navCommunity: 'المجتمع',
  navTeams: 'الفرق',
  navPassport: 'السجل',
  backLabel: 'رجوع',
  closeLabel: 'إغلاق',
  cancelLabel: 'إلغاء',
  confirmLabel: 'تأكيد',
  saveLabel: 'حفظ',
  offlineBannerText: 'وضع عدم الاتصال — يتم عرض البيانات المحفوظة وستُزامن تلقائيًا عند عودة الشبكة.',
  demoCatalogLabel: 'نموذج مرجعي',

  roleLearner: 'متعلم / مطور',
  roleMentor: 'مرشد',
  roleDirector: 'مدير إشراف',
  roleCompany: 'شركة',
  roleAdmin: 'مشرف النظام',
  levelJunior: 'مبتدئ • Junior',
  levelMid: 'متوسط • Mid-level',
  levelSenior: 'خبير • Senior',
  levelDirector: 'قيادي • Director',
  levelDisclaimer: 'المستوى المهني يُقاس بالأدلة العملية والتقييمات الموثّقة وليس بالاشتراكات المدفوعة.',
  planStarterFree: 'Starter — مجانية',
  planGrowthPaid: 'Growth — مدفوعة',
  planProPaid: 'Professional+ — مدفوعة',
  planEnterprise: 'شركات ومؤسسات',

  loopTitle: 'حلقة الخبرة الموثّقة',
  loopSteps: ['فجوة المهارات', 'التعلم', 'التطبيق العملي', 'التقييم', 'دليل موثّق', 'الفرصة'],

  yourSkillMatch: 'نسبة تطابق مهاراتك',
  skillAnalysisTitle: 'تحليل المهارات',
  skillOverview: 'نظرة عامة على المهارات',
  techSkills: 'المهارات التقنية',
  softSkills: 'المهارات الشخصية',
  domainKnowledge: 'المعرفة بالمجال',
  problemSolving: 'حل المشكلات',
  commCollab: 'التواصل والعمل الجماعي',
  aiGapAnalyzer: 'محلل الفجوات الذكي',
  aiExplainTitle: 'لماذا أوصى الذكاء الاصطناعي بهذا المسار؟',
  appealOrAdjust: 'تعديل المهارات',
  proveThisSkill: 'أثبت هذه المهارة عمليًا',
  recommendedNextStep: 'الخطوة الموصى بها الآن',
  activeChallenges: 'التحديات والمشاريع النشطة',
  pointsLabel: 'نقطة',
  streakLabel: 'أيام متتالية',

  badgesTitle: 'شارات SkillBridge',
  unlockedBadgesLabel: 'مفتوحة',
  lockedBadgesLabel: 'مقفلة',
  auditLogTitle: 'سجل التوثيق والنشاط',

  aiAgentTitle: 'المساعد الذكي SkillBridge',
  aiAgentAskHint: 'اسأل عن تلميح تقني، تقسيم مهام الفريق، أو مراجعة معايير التقييم...',
  aiAgentDataUsedPrefix: 'البيانات المستخدمة:',
  aiAgentBoundariesPrefix: 'حدود المساعد:',
  aiAgentUnavailable: 'خدمة الذكاء الاصطناعي غير متاحة حاليًا أو لا يوجد اتصال. تحقّق من الشبكة وأعد المحاولة.',
  aiAgentConfirmActionTitle: 'تأكيد إجراء المساعد الذكي',
  aiAgentReportLabel: 'الإبلاغ عن رد AI',
  aiAgentPersonalizationToggle: 'تخصيص الذكاء الاصطناعي حسب مستواك',
  aiAgentContextToggle: 'مشاركة سياق الشاشة الحالية مع AI',

  discoverTitle: 'مركز الاستكشاف',
  filterAll: 'الكل',
  filterChallenges: 'تحديات',
  filterProjects: 'مشاريع',
  filterContests: 'مسابقات',
  filterReverseEng: 'هندسة عكسية',
  filterMentors: 'مرشدون',
  filterTeams: 'فرق',
  filterOpportunities: 'فرص',
  projectDetails: 'تفاصيل المشروع',
  applyNow: 'قدّم الآن',
  saveForLater: 'حفظ للاحقًا',
  savedLabel: 'تم الحفظ',
  rubricBeforeStart: 'معايير التقييم المعلنة قبل البدء',
  prizePoolDisclaimer: 'تُوزع المكافآت والنقاط حسب الترتيب ومعايير المراجعة الموثقة.',
  ipRightsTitle: 'حقوق الملكية الفكرية',
  aiPolicyTitle: 'سياسة استخدام الذكاء الاصطناعي',
  launchWorkSim: 'محاكاة العمل بالذكاء الاصطناعي',
  submitSolution: 'إرسال الحل والتوثيق',

  contestsTitle: 'مسابقات SkillBridge',
  liveNow: 'مباشر الآن',
  upcoming: 'قادمة',
  ended: 'منتهية',
  editorialTab: 'الشرح والحلول',
  standingsTab: 'لوحة الترتيب',
  problemsTab: 'المسائل',
  reverseEngTitle: 'مركز الهندسة العكسية الأخلاقية',
  authorizedScopeBadge: 'نطاق آمن ومصرّح به',
  allowedVsProhibited: 'المسموح مقابل المحظور',
  understandingReportLabel: 'تقرير الفهم والتحقق الإلزامي',
  secretLeakWarning: 'تم رصد مفتاح سري أو توكن في التسليم (مثل AIza أو sk- أو Bearer). أزل الأسرار قبل الإرسال.',

  communityTitle: 'مجتمع المطورين',
  sortTrending: 'الأكثر تفاعلًا',
  sortNew: 'الأحدث',
  sortTop: 'الأكثر إفادة',
  sortUnanswered: 'بلا إجابة',
  newPost: 'اسأل / شارك',
  bestAnswer: 'أفضل إجابة',
  acceptAnswer: 'اعتماد كأفضل إجابة',
  reportContent: 'إبلاغ',
  verifiedBadge: 'مهارة موثّقة',
  selfClaimedBadge: 'تصريح ذاتي',
  replyLabel: 'رد',
  helpfulLabel: 'مفيد',

  teamsTitle: 'الفرق وسباقات العمل',
  myTeams: 'فرقي',
  recruitingTeams: 'فرق تبحث عن أعضاء',
  createTeam: 'إنشاء فريق',
  joinTeam: 'انضمام للفريق',
  sprintBoard: 'لوحة المهام',
  rewardSplitTitle: 'توزيع النقاط والمكافآت بشفافية',
  teamChat: 'نشاط الفريق',
  membersLabel: 'الأعضاء',
  needRolePrefix: 'المطلوب:',

  leaderboardTitle: 'لوحة الصدارة',
  top100Title: 'تصنيف أفضل ١٠٠',
  ratingFormulaTitle: 'معادلة التقييم والنقاط الشفافة',
  passportTitle: 'سجل الخبرة الموثّق',
  verifiedExperience: 'خبرات عملية معتمدة',
  mentorFeedback: 'ملاحظات المقيّم',
  sharePassport: 'مشاركة السجل',
  skillEvidenceGraph: 'خريطة أدلة المهارات',
  walletTitle: 'سجل النقاط والمكافآت',

  searchPlaceholder: 'ابحث عن مهارات، مشاريع، مسابقات، فرق، أشخاص...',
  notificationsTitle: 'الإشعارات',
  companyDashTitle: 'بوابة الشركات الشريكة',
  mentorDashTitle: 'استوديو مراجعة المرشدين',
  directorAdminTitle: 'الإشراف والاعتماد',
  privacySettingsTitle: 'الخصوصية وحالات الواجهة',
  accountSettingsTitle: 'الحساب والجلسات المتصلة',
  deleteAccountTitle: 'حذف الحساب والبيانات',
  deleteAccountBody: 'يؤدي هذا لحذف ملفك وأهدافك وتسليماتك وشاراتك نهائيًا من السحابة.',
  deleteAccountAction: 'حذف حسابي نهائيًا',
  blueprintTitle: 'معمارية المنظومة الموحدة',
  statePreviewTitle: 'معاين حالات الواجهة',
  loadingText: 'جارٍ مزامنة بيانات SkillBridge الموثّقة...',
  emptyTitle: 'لا توجد عناصر بعد',
  emptyAction: 'استكشف التحديات',
  errorRetry: 'إعادة المحاولة',
  lockedTitle: 'مرحلة مقفلة',
  signOut: 'تسجيل الخروج',

  downloadCenterTitle: 'مركز التحميل والوصول عبر المنصات',
  useOnlineTitle: 'استخدم SkillBridge أونلاين',
  useOnlineDesc: 'وصول فوري من المتصفح مع مزامنة لحظية كاملة دون الحاجة إلى تثبيت.',
  mobileAppTitle: 'تطبيق الموبايل',
  mobileAppDesc: 'ثبّت تطبيق SkillBridge على Android أو iOS لواجهة لمسية وإشعارات فورية.',
  desktopAppTitle: 'تطبيق سطح المكتب',
  desktopAppDesc: 'ثبّت SkillBridge على Windows أو macOS أو Linux لشريط جانبي ومساحة عمل متكاملة.',
  installNowLabel: 'تثبيت التطبيق الآن',
  comingSoonLabel: 'قريبًا',
  liveSyncConnected: 'مزامنة مباشرة فعّالة',
  liveSyncReconnecting: 'إعادة الاتصال بالمزامنة...',
};

export function getStrings(lang: AppLanguage): AppStrings {
  return lang === 'AR' ? ArStrings : EnStrings;
}

export function lText(text: LocalizedText, lang: AppLanguage): string {
  return lang === 'AR' ? text.ar : text.en;
}

export function localizedDomain(domain: string, lang: AppLanguage): string {
  if (lang !== 'AR') return domain;
  const map: Record<string, string> = {
    'Backend & Cloud': 'الخلفية والسحابة',
    'Data Science & AI': 'علم البيانات والذكاء الاصطناعي',
    'Data & AI': 'البيانات والذكاء الاصطناعي',
    'Mobile & Android': 'تطبيقات الجوال و Android',
    Cybersecurity: 'الأمن السيبراني',
    'Product & UX': 'المنتج وتجربة المستخدم',
    'Platform Governance': 'حوكمة المنصة',
  };
  return map[domain] || domain;
}

export function localizedCareerLevel(level: string, lang: AppLanguage): string {
  if (lang !== 'AR') return level;
  const map: Record<string, string> = {
    Junior: 'مبتدئ • Junior',
    'Mid-level': 'متوسط • Mid-level',
    Senior: 'خبير • Senior',
    'Director / Leadership': 'قيادي • Director',
  };
  return map[level] || level;
}

export function localizedStatus(status: string, lang: AppLanguage): string {
  if (lang !== 'AR') return status.replace(/_/g, ' ');
  const map: Record<string, string> = {
    PENDING: 'قيد الانتظار',
    PENDING_APPROVAL: 'بانتظار الاعتماد',
    PENDING_REVIEW: 'بانتظار المراجعة',
    UNDER_REVIEW: 'قيد المراجعة',
    APPROVED: 'معتمد',
    ACTIVE: 'نشط',
    COMPLETED: 'مكتمل',
    VERIFIED: 'موثّق',
    REVISION_REQUESTED: 'مطلوب تعديل',
    REJECTED: 'مرفوض',
    APPLIED: 'تم التقديم',
    SHORTLISTED: 'قائمة مختصرة',
    ACCEPTED: 'مقبول',
    TODO: 'للتنفيذ',
    IN_PROGRESS: 'قيد التنفيذ',
    DONE: 'مكتمل',
    OPEN: 'مفتوح',
    RESOLVED: 'تمت المعالجة',
    DISMISSED: 'مغلق',
    PAID: 'مدفوع',
  };
  return map[status] || status;
}
