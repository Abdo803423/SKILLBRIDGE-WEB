import React, { useMemo, useState } from 'react';
import {
  ArrowLeft,
  ArrowRight,
  ArrowUpRight,
  CheckCircle2,
  Globe,
  Lock,
  Moon,
  RefreshCw,
  ShieldCheck,
  Sun,
  UserPlus,
} from 'lucide-react';
import {
  AnimatedPlatformEmblem,
  LivingBridgeCanvas,
  MotionReveal,
  SpatialCard,
} from '../components/InteractiveMotion';
import { SkillBridgeLogo, StatusLabel, ToastBanner } from '../components/SkillBridgeUi';
import { useSkillBridge } from '../context/SkillBridgeContext';
import { detectBrowserEnvironment } from '../hooks/useNetworkStatus';

const DEMO_ACCOUNTS = [
  { labelEn: 'Student (Demo)', labelAr: 'حساب طالب', loginId: 'student_demo', pass: 'Student@123' },
  { labelEn: 'Company Partner', labelAr: 'شركة شريكة', loginId: 'company_demo', pass: 'Company@123' },
  { labelEn: 'Company Director', labelAr: 'مدير الإشراف', loginId: 'director_demo', pass: 'Director@123' },
  { labelEn: 'Platform Admin', labelAr: 'مشرف المنصة', loginId: 'admin_demo', pass: 'Admin@123' },
];

export const WelcomeScreen: React.FC = () => {
  const {
    strings,
    isArabic,
    darkTheme,
    toggleLanguage,
    toggleTheme,
    loginWithCredentials,
    registerStudent,
    signInWithGoogle,
    enterGuestPreview,
    authBusy,
    authError,
    clearAuthError,
    setRoute,
  } = useSkillBridge();

  const browserEnv = useMemo(() => detectBrowserEnvironment(), []);
  const [authTab, setAuthTab] = useState<'LOGIN' | 'REGISTER'>('LOGIN');
  const [activeWorkflowIdx, setActiveWorkflowIdx] = useState(0);
  const [loginId, setLoginId] = useState('student_demo');
  const [password, setPassword] = useState('Student@123');
  const [regName, setRegName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regHandle, setRegHandle] = useState('');
  const [regPass, setRegPass] = useState('');

  const scrollToSection = (id: string) => {
    const el = document.getElementById(id);
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  };

  const experienceCards = [
    {
      id: 'WEB' as const,
      title: strings.useOnlineTitle,
      desc: strings.useOnlineDesc,
      meta: isArabic ? 'متاح مباشرة · بدون تثبيت · مزامنة فورية' : 'Zero Install · Modern Browsers · Live SSE Sync',
      recommended: browserEnv.viewportProfile === 'TABLET',
      ctaLabel: strings.getStarted,
      onClick: () => scrollToSection('sb-auth-card'),
      secondaryAction: () => setRoute('platform'),
      secondaryLabel: isArabic ? 'عرض تفاصيل المنصة' : 'View Platform Architecture',
    },
    {
      id: 'MOBILE' as const,
      title: strings.mobileWebTitle,
      desc: strings.mobileWebDesc,
      meta: isArabic ? 'واجهة متجاوبة للجوال · شريط تنقل سفلي' : 'Responsive Mobile Layout · Touch-Ready Navigation',
      recommended: browserEnv.viewportProfile === 'MOBILE',
      ctaLabel: isArabic ? 'استكشاف التحديات الآن' : 'Explore Challenges',
      onClick: () => {
        enterGuestPreview();
        setRoute('discover');
      },
      secondaryAction: () => setRoute('platform'),
      secondaryLabel: isArabic ? 'عرض تفاصيل المنصة' : 'View Platform Architecture',
    },
    {
      id: 'DESKTOP' as const,
      title: strings.desktopWebTitle,
      desc: strings.desktopWebDesc,
      meta: isArabic ? 'لوحات تحكم متعددة الأعمدة · رسوم بيانية حية' : 'Sidebar Workspace · Multi-Column Analytics',
      recommended: browserEnv.viewportProfile === 'DESKTOP',
      ctaLabel: strings.getStarted,
      onClick: () => scrollToSection('sb-auth-card'),
      secondaryAction: () => setRoute('platform'),
      secondaryLabel: isArabic ? 'عرض تفاصيل المنصة' : 'View Platform Architecture',
    },
  ];

  const orderedCards = [...experienceCards].sort((a, b) => Number(b.recommended) - Number(a.recommended));

  const workflowSteps = [
    {
      num: '01.',
      title: isArabic ? 'تشخيص فجوة المهارات وخارطة الطريق' : 'Diagnose Skill Gaps & Career Readiness',
      desc: isArabic
        ? 'اربط مهاراتك الحالية بالهدف الوظيفي واحصل على مقياس جاهزية لحظي ومسار أسبوعي دقيق.'
        : 'Map your current proficiency against target industry roles with a live readiness score and weekly plan.',
    },
    {
      num: '02.',
      title: isArabic ? 'مشاريع الشركات والهندسة العكسية' : 'Real Company Briefs & Reverse Engineering Labs',
      desc: isArabic
        ? 'قدّم حلولًا عملية لتحديات الشركات الحقيقية ومختبرات فحص الأنظمة ضمن فرق متعاونة.'
        : 'Execute scoped enterprise briefs, weekly sprint deliverables, and ethical architecture dissections.',
    },
    {
      num: '03.',
      title: isArabic ? 'تقييم المشرفين والاعتماد الرسمي' : 'Director Rubric Review & Verified Experience Passport',
      desc: isArabic
        ? 'يعتمد مدير الإشراف أعمالك وفق معايير واضحة لتُوثّق مباشرة في سجل خبرتك الرقمي.'
        : 'Company directors verify deliverables by rubric so completed work lands permanently in your verified passport.',
    },
    {
      num: '04.',
      title: isArabic ? 'وصول فوري من أي متصفح وفي أي وقت' : 'Seamless Browser Access With Live Central Sync',
      desc: isArabic
        ? 'جميع الحسابات والإشعارات والتقديمات تُحفظ وتُحدّث آنيًا في السحابة دون أي تثبيت.'
        : 'Sign in from Chrome, Edge, Safari, or Firefox and collaborate over one real-time shared database.',
    },
  ];

  const handleSubmitAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    clearAuthError();
    if (authTab === 'LOGIN') {
      await loginWithCredentials(loginId.trim(), password);
    } else {
      await registerStudent({
        loginId: regHandle.trim(),
        displayName: regName.trim(),
        email: regEmail.trim(),
        password: regPass,
      });
    }
  };

  return (
    <div className="relative min-h-screen bg-[#F7FAFC] dark:bg-[#061120] text-[#0B1F38] dark:text-[#F1F7FC] overflow-x-clip">
      {/* Interactive 3D-Projected Particle Constellation & Click Ripple Atmosphere */}
      <LivingBridgeCanvas
        density="hero"
        className="fixed inset-0 w-full h-full z-0 opacity-85 dark:opacity-90"
      />

      {/* Sticky 3-Zone Editorial Navigation Bar */}
      <header className="sticky top-0 z-20 bg-[#F7FAFC]/88 dark:bg-[#061120]/88 backdrop-blur-md border-b border-[#D5E0EC] dark:border-[#1C395E]">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-8">
          <button type="button" onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })} className="text-start">
            <SkillBridgeLogo size="md" />
          </button>

          <nav className="hidden md:flex items-center gap-7 text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8]">
            <button
              type="button"
              onClick={() => scrollToSection('sb-experiences')}
              className="sb-nav-underline hover:text-[#0B1F38] dark:hover:text-[#7EE8F5] transition whitespace-nowrap shrink-0"
            >
              {isArabic ? 'تجربة الويب' : 'Web Experience'}
            </button>
            <button
              type="button"
              onClick={() => scrollToSection('sb-workflow')}
              className="sb-nav-underline hover:text-[#0B1F38] dark:hover:text-[#7EE8F5] transition whitespace-nowrap shrink-0"
            >
              {isArabic ? 'آلية العمل' : 'Workflow'}
            </button>
            <button
              type="button"
              onClick={() => setRoute('platform')}
              className="sb-nav-underline hover:text-[#0B1F38] dark:hover:text-[#7EE8F5] transition whitespace-nowrap shrink-0"
            >
              {strings.platformOverviewTitle}
            </button>
          </nav>

          <div className="flex items-center gap-2 shrink-0">
            <button
              type="button"
              onClick={toggleLanguage}
              className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white/90 dark:bg-[#0D1E36]/90 px-3 py-1.5 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB] transition whitespace-nowrap shrink-0 active:scale-95"
            >
              {strings.languageToggleLabel}
            </button>
            <button
              type="button"
              onClick={toggleTheme}
              className="p-2 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white/90 dark:bg-[#0D1E36]/90 text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB] transition active:scale-95"
              aria-label="Theme"
            >
              {darkTheme ? <Sun className="w-4 h-4 text-[#7EE8F5]" /> : <Moon className="w-4 h-4 text-[#008CAE]" />}
            </button>
            <button
              type="button"
              onClick={() => scrollToSection('sb-auth-card')}
              className="sb-btn-interactive hidden sm:inline-flex items-center gap-1.5 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-1.5 text-xs font-semibold text-white whitespace-nowrap shrink-0 shadow-[0_8px_22px_-8px_rgba(0,180,219,0.65)]"
            >
              {strings.getStarted}
            </button>
          </div>
        </div>
      </header>

      <main className="relative z-10 max-w-6xl mx-auto px-4 sm:px-6 py-8 sm:py-12 space-y-14">
        {/* Ambient Blue Split Hero + Interactive 3D Stage & Auth Card */}
        <section className="sb-blue-atmosphere grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 items-stretch">
          <div className="relative z-10 lg:col-span-7 flex flex-col justify-between space-y-6">
            <div className="space-y-4">
              <p className="text-xs font-semibold tracking-wide text-[#008CAE] dark:text-[#7EE8F5]">
                {isArabic
                  ? `${browserEnv.sessionLabel} · موقع ويب تفاعلي متجاوب`
                  : `${browserEnv.sessionLabel} · Responsive Web Platform`}
              </p>

              <h1 className="font-display text-3xl sm:text-5xl font-bold tracking-tight text-[#0B1F38] dark:text-[#F1F7FC] leading-[1.08] [text-wrap:balance]">
                {strings.tagline}
              </h1>

              <p className="text-sm sm:text-base text-[#4C637C] dark:text-[#93ADC8] leading-relaxed max-w-2xl">
                {strings.welcomeSubtitle}
              </p>
            </div>

            {/* Interactive 3D Tilted Product Stage Card */}
            <SpatialCard
              tiltStrength={4}
              highlight
              className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white/95 dark:bg-[#0D1E36]/95 p-2 backdrop-blur-sm"
            >
              <figure>
                <div className="rounded-xl overflow-hidden border border-[#D5E0EC]/80 dark:border-[#1C395E] bg-[#0B1F38] relative">
                  <img
                    src={`${import.meta.env.BASE_URL}assets/images/img_hero_bridge_1794605869240.jpg`}
                    alt="SkillBridge Career Development Website"
                    referrerPolicy="no-referrer"
                    className="w-full h-52 sm:h-64 object-cover transition-transform duration-700 ease-out group-hover:scale-[1.03]"
                  />
                  <div className="pointer-events-none absolute inset-0 bg-gradient-to-tr from-[#0B1F38]/55 via-transparent to-[#00B4DB]/15" />
                </div>
                <figcaption className="px-3 pt-2.5 pb-1.5 flex flex-wrap items-center justify-between gap-2 text-xs">
                  <span className="font-medium text-[#4C637C] dark:text-[#93ADC8]">
                    {isArabic
                      ? 'منصة ويب موحدة · مزامنة فورية لـ ٤ أدوار'
                      : 'Unified Web Platform · Live 4-Role Sync'}
                  </span>
                  <button
                    type="button"
                    onClick={() => setRoute('platform')}
                    className="inline-flex items-center gap-1 font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline whitespace-nowrap transition-transform duration-200 group-hover:translate-x-0.5"
                  >
                    {strings.platformOverviewTitle}
                    <ArrowUpRight className="w-3.5 h-3.5" />
                  </button>
                </figcaption>
              </figure>
            </SpatialCard>
          </div>

          {/* Right Interactive Auth Console */}
          <SpatialCard
            id="sb-auth-card"
            tiltStrength={2.5}
            className="relative z-10 lg:col-span-5 rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white/95 dark:bg-[#0D1E36]/95 p-5 sm:p-6 backdrop-blur-sm"
          >
            <div className="flex flex-col justify-between h-full space-y-4">
              <div className="space-y-4">
                <div className="flex items-center justify-between border-b border-[#D5E0EC] dark:border-[#1C395E] pb-3">
                  <div className="inline-flex rounded-xl bg-[#F0F6FA] dark:bg-[#061120] p-1 border border-[#D5E0EC] dark:border-[#1C395E] gap-1">
                    <button
                      type="button"
                      onClick={() => {
                        clearAuthError();
                        setAuthTab('LOGIN');
                      }}
                      className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition-all whitespace-nowrap shrink-0 active:scale-95 ${
                        authTab === 'LOGIN'
                          ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-[#061120] shadow-sm'
                          : 'text-[#4C637C] dark:text-[#93ADC8] hover:text-[#0B1F38] dark:hover:text-[#F1F7FC]'
                      }`}
                    >
                      {isArabic ? 'تسجيل الدخول' : 'Sign In'}
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        clearAuthError();
                        setAuthTab('REGISTER');
                      }}
                      className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition-all whitespace-nowrap shrink-0 active:scale-95 ${
                        authTab === 'REGISTER'
                          ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-[#061120] shadow-sm'
                          : 'text-[#4C637C] dark:text-[#93ADC8] hover:text-[#0B1F38] dark:hover:text-[#F1F7FC]'
                      }`}
                    >
                      {isArabic ? 'حساب طالب جديد' : 'Register Student'}
                    </button>
                  </div>
                  <Lock className="w-4 h-4 text-[#00B4DB] dark:text-[#7EE8F5]" />
                </div>

                {authError && (
                  <div className="rounded-xl border border-rose-300 dark:border-rose-800 bg-rose-50 dark:bg-rose-950/40 px-3 py-2 text-xs text-rose-700 dark:text-rose-300">
                    {authError}
                  </div>
                )}

                <form onSubmit={handleSubmitAuth} className="space-y-3">
                  {authTab === 'LOGIN' ? (
                    <>
                      <div>
                        <label className="block text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8] mb-1">
                          {isArabic ? 'اسم المستخدم أو البريد' : 'Username or Email'}
                        </label>
                        <input
                          type="text"
                          required
                          value={loginId}
                          onChange={(e) => setLoginId(e.target.value)}
                          className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F7FAFC] dark:bg-[#061120] px-3 py-2 text-sm text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB] transition-colors"
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8] mb-1">
                          {isArabic ? 'كلمة المرور' : 'Password'}
                        </label>
                        <input
                          type="password"
                          required
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F7FAFC] dark:bg-[#061120] px-3 py-2 text-sm text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB] transition-colors"
                        />
                      </div>
                    </>
                  ) : (
                    <>
                      <div>
                        <label className="block text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8] mb-1">
                          {isArabic ? 'الاسم الكامل' : 'Full Name'}
                        </label>
                        <input
                          type="text"
                          required
                          value={regName}
                          onChange={(e) => setRegName(e.target.value)}
                          placeholder="e.g. Nadia Samir"
                          className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F7FAFC] dark:bg-[#061120] px-3 py-2 text-sm text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB] transition-colors"
                        />
                      </div>
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                        <div>
                          <label className="block text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8] mb-1">
                            {isArabic ? 'معرف الدخول' : 'Handle'}
                          </label>
                          <input
                            type="text"
                            required
                            value={regHandle}
                            onChange={(e) => setRegHandle(e.target.value)}
                            placeholder="nadia_dev"
                            className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F7FAFC] dark:bg-[#061120] px-3 py-2 text-sm text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB] transition-colors"
                          />
                        </div>
                        <div>
                          <label className="block text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8] mb-1">
                            {isArabic ? 'البريد الإلكتروني' : 'Email'}
                          </label>
                          <input
                            type="email"
                            required
                            value={regEmail}
                            onChange={(e) => setRegEmail(e.target.value)}
                            placeholder="nadia@example.com"
                            className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F7FAFC] dark:bg-[#061120] px-3 py-2 text-sm text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB] transition-colors"
                          />
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8] mb-1">
                          {isArabic ? 'كلمة المرور (٦ أحرف فأكثر)' : 'Password (6+ chars)'}
                        </label>
                        <input
                          type="password"
                          required
                          minLength={6}
                          value={regPass}
                          onChange={(e) => setRegPass(e.target.value)}
                          className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F7FAFC] dark:bg-[#061120] px-3 py-2 text-sm text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB] transition-colors"
                        />
                      </div>
                    </>
                  )}

                  <button
                    type="submit"
                    disabled={authBusy}
                    className="sb-btn-interactive w-full rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] disabled:opacity-60 py-2.5 text-sm font-bold text-white flex items-center justify-center gap-2 whitespace-nowrap shadow-[0_10px_24px_-10px_rgba(0,180,219,0.75)]"
                  >
                    {authTab === 'REGISTER' && <UserPlus className="w-4 h-4" />}
                    {authBusy
                      ? isArabic
                        ? 'جارٍ الاتصال...'
                        : 'Authenticating...'
                      : authTab === 'LOGIN'
                        ? strings.getStarted
                        : isArabic
                          ? 'إنشاء الحساب والدخول'
                          : 'Create Student Account'}
                  </button>
                </form>

                {authTab === 'LOGIN' && (
                  <div className="space-y-1.5 pt-1">
                    <p className="text-[11px] font-semibold text-[#4C637C] dark:text-[#93ADC8]">
                      {isArabic ? 'دخول سريع حسب الصلاحية (بيانات حية متزامنة):' : 'Quick Role Access (Central DB):'}
                    </p>
                    <div className="grid grid-cols-2 gap-1.5">
                      {DEMO_ACCOUNTS.map((acc) => (
                        <button
                          key={acc.loginId}
                          type="button"
                          disabled={authBusy}
                          onClick={() => {
                            setLoginId(acc.loginId);
                            setPassword(acc.pass);
                            loginWithCredentials(acc.loginId, acc.pass);
                          }}
                          className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F7FAFC] dark:bg-[#061120] px-2.5 py-2 text-[11px] font-semibold text-start hover:border-[#00B4DB] hover:bg-[#E0F9FD]/40 dark:hover:bg-[#0E2440] active:scale-[0.98] transition"
                        >
                          <span className="block text-[#0B1F38] dark:text-[#F1F7FC] truncate">
                            {isArabic ? acc.labelAr : acc.labelEn}
                          </span>
                          <span className="block font-mono text-[10px] text-[#008CAE] dark:text-[#7EE8F5] truncate">
                            {acc.loginId}
                          </span>
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              <div className="pt-3 border-t border-[#D5E0EC] dark:border-[#1C395E] space-y-2">
                <button
                  type="button"
                  disabled={authBusy}
                  onClick={signInWithGoogle}
                  className="sb-btn-interactive w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F7FAFC] dark:bg-[#061120] hover:border-[#00B4DB] py-2 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC] transition"
                >
                  {strings.signInGoogle}
                </button>
                <button
                  type="button"
                  onClick={enterGuestPreview}
                  className="w-full rounded-xl py-2 text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8] hover:text-[#008CAE] dark:hover:text-[#7EE8F5] transition active:scale-[0.99]"
                >
                  {strings.continueAsGuest}
                </button>
              </div>
            </div>
          </SpatialCard>
        </section>

        <div className="sb-gl-divider" />

        {/* Three Responsive Browser Experience Cards */}
        <section id="sb-experiences" className="space-y-6">
          <MotionReveal>
            <div className="flex flex-wrap items-end justify-between gap-3">
              <div className="space-y-1">
                <h2 className="font-display text-2xl sm:text-3xl font-bold tracking-tight text-[#0B1F38] dark:text-[#F1F7FC] [text-wrap:balance]">
                  {isArabic ? 'موقع واحد متجاوب لكل الشاشات والمتصفحات' : 'One Website Built for Every Browser & Viewport'}
                </h2>
                <p className="text-xs sm:text-sm text-[#4C637C] dark:text-[#93ADC8]">
                  {isArabic
                    ? 'افتح SkillBridge مباشرة عبر Chrome أو Edge أو Safari أو Firefox بدون أي تحميل أو تثبيت.'
                    : 'Access the complete SkillBridge ecosystem directly in any modern browser — desktop, tablet, or smartphone.'}
                </p>
              </div>
              <button
                type="button"
                onClick={() => setRoute('platform')}
                className="sb-btn-interactive inline-flex items-center gap-2 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] px-4 py-2 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB] transition whitespace-nowrap shrink-0"
              >
                <Globe className="w-3.5 h-3.5 text-[#00B4DB] dark:text-[#7EE8F5]" />
                {strings.platformOverviewTitle}
              </button>
            </div>
          </MotionReveal>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
            {orderedCards.map((card, idx) => (
              <MotionReveal key={card.id} delayMs={idx * 85} className="h-full">
                <SpatialCard
                  tiltStrength={5}
                  highlight={card.recommended}
                  className={`h-full rounded-2xl border p-6 bg-white/95 dark:bg-[#0D1E36]/95 backdrop-blur-sm ${
                    card.recommended
                      ? 'border-[#00B4DB] dark:border-[#00B4DB]'
                      : 'border-[#D5E0EC] dark:border-[#1C395E] hover:border-[#00B4DB]/70'
                  }`}
                >
                  <div className="space-y-4">
                    <div className="flex items-center justify-between gap-2">
                      <AnimatedPlatformEmblem kind={card.id} active={card.recommended} />
                      {card.recommended && (
                        <span className="text-[11px] font-semibold text-[#008CAE] dark:text-[#7EE8F5]">
                          {isArabic ? 'متوافق مع شاشتك الحالية' : 'Matched to your viewport'}
                        </span>
                      )}
                    </div>
                    <div className="space-y-1.5">
                      <h3 className="font-display text-lg font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                        {card.title}
                      </h3>
                      <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] leading-relaxed">{card.desc}</p>
                      <p className="font-mono text-[11px] text-[#008CAE] dark:text-[#7EE8F5] pt-1 tabular-nums">
                        {card.meta}
                      </p>
                    </div>
                  </div>

                  <div className="space-y-2 pt-4 mt-4 border-t border-[#D5E0EC]/80 dark:border-[#1C395E]">
                    <button
                      type="button"
                      onClick={card.onClick}
                      className={`sb-btn-interactive w-full rounded-xl py-2.5 px-4 text-xs font-bold transition whitespace-nowrap ${
                        card.recommended
                          ? 'bg-[#00B4DB] hover:bg-[#008CAE] text-white shadow-[0_8px_20px_-8px_rgba(0,180,219,0.7)]'
                          : 'border border-[#D5E0EC] dark:border-[#1C395E] text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB]'
                      }`}
                    >
                      {card.ctaLabel}
                    </button>

                    <button
                      type="button"
                      onClick={card.secondaryAction}
                      className="w-full text-[11px] font-semibold text-[#4C637C] dark:text-[#93ADC8] hover:text-[#008CAE] dark:hover:text-[#7EE8F5] py-1 transition"
                    >
                      {card.secondaryLabel}
                    </button>
                  </div>
                </SpatialCard>
              </MotionReveal>
            ))}
          </div>
        </section>

        <div className="sb-gl-divider" />

        {/* Interactive Numbered Architecture Spine (Works via Hover & Touch Tap) */}
        <section id="sb-workflow" className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          <MotionReveal className="lg:col-span-5 space-y-3">
            <p className="text-xs font-semibold uppercase tracking-wider text-[#008CAE] dark:text-[#7EE8F5]">
              {isArabic ? 'مسار العمل الموحد' : 'Ecosystem Workflow'}
            </p>
            <h2 className="font-display text-2xl sm:text-3xl font-bold tracking-tight text-[#0B1F38] dark:text-[#F1F7FC] [text-wrap:balance]">
              {isArabic
                ? 'من تشخيص الفجوة إلى خبرة معتمدة في مكان واحد'
                : 'From skill gap diagnosis to verified experience'}
            </h2>
            <p className="text-xs sm:text-sm text-[#4C637C] dark:text-[#93ADC8] leading-relaxed">
              {isArabic
                ? 'جميع الأدوار الأربعة — الطالب، الشركة، مدير الإشراف، والمشرف العام — تتعامل مع نفس السجلات الفورية.'
                : 'Students, Company Partners, Company Directors, and Platform Admins collaborate over the same live records.'}
            </p>
          </MotionReveal>

          <div className="lg:col-span-7 border-s border-[#D5E0EC] dark:border-[#1C395E] ps-5 sm:ps-6 space-y-3">
            {workflowSteps.map((step, idx) => {
              const selected = activeWorkflowIdx === idx;
              return (
                <MotionReveal key={step.num} delayMs={idx * 70}>
                  <button
                    type="button"
                    onClick={() => setActiveWorkflowIdx(idx)}
                    onMouseEnter={() => setActiveWorkflowIdx(idx)}
                    className={`w-full text-start rounded-2xl border p-4 transition-all duration-200 active:scale-[0.992] ${
                      selected
                        ? 'border-[#00B4DB] bg-white/95 dark:bg-[#0D1E36]/95 shadow-[0_14px_34px_-22px_rgba(0,180,219,0.55)]'
                        : 'border-transparent hover:border-[#D5E0EC] dark:hover:border-[#1C395E] hover:bg-white/60 dark:hover:bg-[#0D1E36]/60'
                    }`}
                  >
                    <div className="flex items-baseline gap-3">
                      <span
                        className={`font-mono text-xs font-bold tabular-nums transition-colors ${
                          selected ? 'text-[#00B4DB] dark:text-[#7EE8F5]' : 'text-[#4C637C] dark:text-[#93ADC8]'
                        }`}
                      >
                        {step.num}
                      </span>
                      <h3 className="font-display text-base sm:text-lg font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                        {step.title}
                      </h3>
                    </div>
                    <p className="text-xs sm:text-sm text-[#4C637C] dark:text-[#93ADC8] leading-relaxed ps-8 mt-1">
                      {step.desc}
                    </p>
                  </button>
                </MotionReveal>
              );
            })}
          </div>
        </section>

        {/* Quiet Editorial Footer */}
        <footer className="pt-8 border-t border-[#D5E0EC] dark:border-[#1C395E] flex flex-wrap items-center justify-between gap-3 text-xs text-[#4C637C] dark:text-[#93ADC8]">
          <span>SkillBridge · Career Web Platform</span>
          <div className="flex items-center gap-4">
            <button
              type="button"
              onClick={() => {
                enterGuestPreview();
                setRoute('discover');
              }}
              className="font-medium hover:text-[#008CAE] dark:hover:text-[#7EE8F5] transition"
            >
              {strings.navDiscover}
            </button>
            <button
              type="button"
              onClick={() => setRoute('platform')}
              className="font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline"
            >
              {strings.platformOverviewTitle}
            </button>
          </div>
        </footer>
      </main>
      <ToastBanner />
    </div>
  );
};

export const PlatformOverviewScreen: React.FC = () => {
  const { isArabic, strings, setRoute, session, sseConnected, lastSyncedMs } = useSkillBridge();
  const browserEnv = useMemo(() => detectBrowserEnvironment(), []);

  const webCapabilities = [
    {
      feature: isArabic ? 'الحسابات والصلاحيات (طالب / شركة / مدير إشراف / مشرف عام)' : 'Unified Auth & 4-Role RBAC Web Portals',
    },
    {
      feature: isArabic ? 'تحليل الفجوات بالذكاء الاصطناعي والمدرب التفاعلي' : 'AI Skill Gap Analyzer & Streaming Coach',
    },
    {
      feature: isArabic ? 'المشاريع الحية، تسليم الأعمال، ومراجعة المدير' : 'Live Company Briefs, Tasks & Rubric Verification',
    },
    {
      feature: isArabic ? 'المزامنة اللحظية وتعدد جلسات المتصفح' : 'Real-Time Browser Session Sync (Central REST + SSE)',
    },
  ];

  return (
    <div className="relative min-h-screen bg-[#F7FAFC] dark:bg-[#061120] text-[#0B1F38] dark:text-[#F1F7FC] overflow-x-clip">
      <LivingBridgeCanvas density="ambient" className="fixed inset-0 w-full h-full z-0 opacity-75" />

      <div className="relative z-10 max-w-5xl mx-auto px-4 sm:px-6 py-6 sm:py-10 space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <button
            type="button"
            onClick={() => setRoute(session ? 'home' : 'welcome')}
            className="sb-btn-interactive inline-flex items-center gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] px-3 py-2 text-xs font-semibold hover:border-[#00B4DB] whitespace-nowrap transition"
          >
            {isArabic ? <ArrowRight className="w-4 h-4" /> : <ArrowLeft className="w-4 h-4" />}
            {strings.backLabel}
          </button>
          <SkillBridgeLogo size="sm" subtitle />
        </div>

        {/* Browser Environment Banner */}
        <SpatialCard
          tiltStrength={2.5}
          highlight
          className="sb-blue-atmosphere rounded-2xl border border-[#00B4DB]/40 bg-[#0B1F38] text-white p-6 sm:p-7"
        >
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-5">
            <div className="relative z-10 space-y-1.5">
              <p className="text-xs font-semibold text-[#7EE8F5]">
                {isArabic ? `المتصفح النشط: ${browserEnv.sessionLabel}` : `Active Browser: ${browserEnv.sessionLabel}`}
              </p>
              <h1 className="font-display text-2xl sm:text-3xl font-bold text-white">
                {strings.platformOverviewTitle}
              </h1>
              <p className="text-xs sm:text-sm text-[#B7CCE0] max-w-xl leading-relaxed">
                {isArabic
                  ? 'موقع SkillBridge يعمل بالكامل داخل المتصفح عبر شاشات الكمبيوتر والأجهزة اللوحية والهواتف مع مزامنة لحظية دون حاجة لأي تثبيت.'
                  : 'SkillBridge runs entirely in your web browser across desktop, tablet, and mobile viewports with real-time cloud persistence and zero installation required.'}
              </p>
            </div>
            <div className="relative z-10 shrink-0">
              <button
                type="button"
                onClick={() => setRoute(session ? 'home' : 'welcome')}
                className="sb-btn-interactive inline-flex items-center gap-2 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2.5 text-xs font-bold text-white whitespace-nowrap shadow-[0_8px_22px_-8px_rgba(0,180,219,0.8)]"
              >
                <Globe className="w-4 h-4" />
                {strings.getStarted}
              </button>
            </div>
          </div>
        </SpatialCard>

        {/* Browser Workspace Profiles */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[
            {
              kind: 'DESKTOP' as const,
              num: '01.',
              title: strings.desktopWebTitle,
              desc: strings.desktopWebDesc,
              status: 'ACTIVE',
            },
            {
              kind: 'MOBILE' as const,
              num: '02.',
              title: strings.mobileWebTitle,
              desc: strings.mobileWebDesc,
              status: 'ACTIVE',
            },
            {
              kind: 'WEB' as const,
              num: '03.',
              title: strings.useOnlineTitle,
              desc: strings.useOnlineDesc,
              status: 'LIVE',
            },
          ].map((item, idx) => (
            <MotionReveal key={item.num} delayMs={idx * 70}>
              <SpatialCard
                tiltStrength={3}
                className="h-full rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white/95 dark:bg-[#0D1E36]/95 p-5 space-y-3"
              >
                <div className="flex items-center justify-between gap-2">
                  <AnimatedPlatformEmblem kind={item.kind} active />
                  <StatusLabel status={item.status} />
                </div>
                <h2 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                  <span className="font-mono text-xs text-[#00B4DB] me-2">{item.num}</span>
                  {item.title}
                </h2>
                <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] leading-relaxed">{item.desc}</p>
              </SpatialCard>
            </MotionReveal>
          ))}
        </div>

        {/* Real-Time Sync & Web Capabilities Matrix */}
        <MotionReveal delayMs={210}>
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white/95 dark:bg-[#0D1E36]/95 p-5 space-y-3">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <h2 className="font-display text-lg font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {isArabic ? 'قدرات الموقع والمزامنة الفورية' : 'Website Capabilities & Live Sync Verification'}
              </h2>
              <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5]">
                <RefreshCw className="w-3.5 h-3.5" />
                {sseConnected
                  ? isArabic
                    ? 'متصل بالمزامنة المباشرة'
                    : 'Real-Time Stream Ready'
                  : `${isArabic ? 'آخر مزامنة' : 'Last Sync'}: ${new Date(lastSyncedMs).toLocaleTimeString()}`}
              </span>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
              {webCapabilities.map((row) => (
                <div
                  key={row.feature}
                  className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F7FAFC] dark:bg-[#061120] px-3.5 py-2.5 flex items-center justify-between gap-2"
                >
                  <span className="text-xs font-medium text-[#0B1F38] dark:text-[#F1F7FC] flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-[#00B4DB] shrink-0" />
                    {row.feature}
                  </span>
                  <ShieldCheck className="w-4 h-4 text-[#008CAE] dark:text-[#7EE8F5] shrink-0" />
                </div>
              ))}
            </div>
          </div>
        </MotionReveal>
      </div>
    </div>
  );
};
