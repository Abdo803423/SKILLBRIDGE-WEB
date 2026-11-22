import React, { useEffect, useMemo, useState } from 'react';
import {
  ArrowLeft,
  ArrowRight,
  ArrowUpRight,
  CheckCircle2,
  Download,
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
import { PWAInstallButton } from '../components/PWAInstallButton';
import { SkillBridgeLogo, StatusLabel, ToastBanner } from '../components/SkillBridgeUi';
import { useSkillBridge } from '../context/SkillBridgeContext';
import { detectDevicePlatform, usePWAInstall } from '../hooks/usePWAInstall';
import { fetchDownloadsStatusApi, type DownloadsStatusResponse } from '../services/ecosystemApi';

const DEMO_ACCOUNTS = [
  { labelEn: 'Student (Demo)', labelAr: 'حساب طالب', loginId: 'student_demo', pass: 'Student@123' },
  { labelEn: 'Company Partner', labelAr: 'شركة شريكة', loginId: 'company_demo', pass: 'Company@123' },
  { labelEn: 'Company Director', labelAr: 'مدير الإشراف', loginId: 'director_demo', pass: 'Director@123' },
  { labelEn: 'Platform Admin', labelAr: 'مشرف المنصة', loginId: 'admin_demo', pass: 'Admin@123' },
];

function formatMb(sizeBytes: number): string {
  if (!sizeBytes) return '';
  const mb = sizeBytes / (1024 * 1024);
  return `${mb.toFixed(1)} MB`;
}

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

  const platform = useMemo(() => detectDevicePlatform(), []);
  const [downloadsStatus, setDownloadsStatus] = useState<DownloadsStatusResponse | null>(null);
  const [authTab, setAuthTab] = useState<'LOGIN' | 'REGISTER'>('LOGIN');
  const [activeWorkflowIdx, setActiveWorkflowIdx] = useState(0);
  const [loginId, setLoginId] = useState('student_demo');
  const [password, setPassword] = useState('Student@123');
  const [regName, setRegName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regHandle, setRegHandle] = useState('');
  const [regPass, setRegPass] = useState('');

  useEffect(() => {
    fetchDownloadsStatusApi()
      .then(setDownloadsStatus)
      .catch(() => {});
  }, []);

  const scrollToSection = (id: string) => {
    const el = document.getElementById(id);
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  };

  const androidReady = downloadsStatus?.android.available ?? false;
  const windowsReady = downloadsStatus?.windows.available ?? false;

  const accessCards = [
    {
      id: 'WEB' as const,
      title: strings.useOnlineTitle,
      desc: strings.useOnlineDesc,
      meta: isArabic ? 'متاح مباشرة · بدون تثبيت · مزامنة SSE' : 'Browser App · Zero Install · Live SSE Sync',
      recommended: platform.recommendedTarget === 'WEB',
      ctaLabel: strings.getStarted,
      href: undefined as string | undefined,
      downloadName: undefined as string | undefined,
      onClick: () => scrollToSection('sb-auth-card'),
      secondaryAction: undefined as (() => void) | undefined,
      secondaryLabel: undefined as string | undefined,
    },
    {
      id: 'MOBILE' as const,
      title: strings.mobileAppTitle,
      desc: androidReady
        ? isArabic
          ? 'تطبيق Android أصلي بصيغة APK موقّعة متصل لحظيًا بقاعدة البيانات المركزية.'
          : 'Native Kotlin & Jetpack Compose APK wired to the same shared central database.'
        : strings.mobileAppDesc,
      meta: androidReady
        ? `${formatMb(downloadsStatus!.android.sizeBytes)} · SHA-256 ${downloadsStatus!.android.sha256.slice(0, 8)}`
        : isArabic
          ? 'حزمة الموبايل الموحدة'
          : 'Native Mobile Runtime',
      recommended: platform.recommendedTarget === 'MOBILE',
      ctaLabel: androidReady
        ? isArabic
          ? 'تحميل Android (.apk)'
          : 'Download Android (.apk)'
        : isArabic
          ? 'عرض خيارات الموبايل'
          : 'Open Mobile Download',
      href: androidReady ? downloadsStatus!.android.downloadUrl : undefined,
      downloadName: androidReady ? downloadsStatus!.android.filename : undefined,
      onClick: androidReady ? undefined : () => setRoute('downloads'),
      secondaryAction: () => setRoute('downloads'),
      secondaryLabel: isArabic ? 'التفاصيل والتحقق' : 'Checksums & Install Notes',
    },
    {
      id: 'DESKTOP' as const,
      title: strings.desktopAppTitle,
      desc: windowsReady
        ? isArabic
          ? 'مثبّت Windows مستقل (.exe) يطلق تطبيق SkillBridge بواجهة سطح مكتب متكاملة.'
          : 'Dedicated Windows NSIS installer launching SkillBridge as an independent desktop client.'
        : strings.desktopAppDesc,
      meta: windowsReady
        ? `${formatMb(downloadsStatus!.windows.sizeBytes)} · SHA-256 ${downloadsStatus!.windows.sha256.slice(0, 8)}`
        : isArabic
          ? 'حزمة سطح المكتب الموحدة'
          : 'Desktop Client Runtime',
      recommended: platform.recommendedTarget === 'DESKTOP',
      ctaLabel: windowsReady
        ? isArabic
          ? 'تحميل Windows (.exe)'
          : 'Download Windows (.exe)'
        : isArabic
          ? 'عرض خيارات سطح المكتب'
          : 'Open Desktop Download',
      href: windowsReady ? downloadsStatus!.windows.downloadUrl : undefined,
      downloadName: windowsReady ? downloadsStatus!.windows.filename : undefined,
      onClick: windowsReady ? undefined : () => setRoute('downloads'),
      secondaryAction: () => setRoute('downloads'),
      secondaryLabel: isArabic ? 'التفاصيل والتحقق' : 'Checksums & Install Notes',
    },
  ];

  const orderedAccessCards = [...accessCards].sort((a, b) => Number(b.recommended) - Number(a.recommended));

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
      title: isArabic ? 'حساب واحد عبر الويب والأندرويد وويندوز' : 'One Account Synced Across Web, Android & Windows',
      desc: isArabic
        ? 'جميع الحسابات والإشعارات والتقديمات تُحفظ وتُحدّث آنيًا في قاعدة البيانات المركزية.'
        : 'Sign in on any client and continue seamlessly from the exact same central database and live event stream.',
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
              onClick={() => scrollToSection('sb-platforms')}
              className="sb-nav-underline hover:text-[#0B1F38] dark:hover:text-[#7EE8F5] transition whitespace-nowrap shrink-0"
            >
              {isArabic ? 'المنصات' : 'Platforms'}
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
              onClick={() => setRoute('downloads')}
              className="sb-nav-underline hover:text-[#0B1F38] dark:hover:text-[#7EE8F5] transition whitespace-nowrap shrink-0"
            >
              {strings.downloadCenterTitle}
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
                  ? `${platform.osLabel} · ويب وأندرويد وويندوز`
                  : `${platform.osLabel} · Web, Android & Windows`}
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
                    src="/assets/images/img_hero_bridge_1794605869240.jpg"
                    alt="SkillBridge Unified Ecosystem"
                    referrerPolicy="no-referrer"
                    className="w-full h-52 sm:h-64 object-cover transition-transform duration-700 ease-out group-hover:scale-[1.03]"
                  />
                  <div className="pointer-events-none absolute inset-0 bg-gradient-to-tr from-[#0B1F38]/55 via-transparent to-[#00B4DB]/15" />
                </div>
                <figcaption className="px-3 pt-2.5 pb-1.5 flex flex-wrap items-center justify-between gap-2 text-xs">
                  <span className="font-medium text-[#4C637C] dark:text-[#93ADC8]">
                    {isArabic
                      ? 'ساحة عمل موحدة · قاعدة بيانات مركزية حية'
                      : 'Unified Workspace · Live Central Database Sync'}
                  </span>
                  <button
                    type="button"
                    onClick={() => setRoute('downloads')}
                    className="inline-flex items-center gap-1 font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline whitespace-nowrap transition-transform duration-200 group-hover:translate-x-0.5"
                  >
                    {strings.downloadCenterTitle}
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

        {/* Three Spatial Platform Destination Cards (Scroll-Triggered + Animated Emblems) */}
        <section id="sb-platforms" className="space-y-6">
          <MotionReveal>
            <div className="flex flex-wrap items-end justify-between gap-3">
              <div className="space-y-1">
                <h2 className="font-display text-2xl sm:text-3xl font-bold tracking-tight text-[#0B1F38] dark:text-[#F1F7FC] [text-wrap:balance]">
                  {isArabic ? 'اختر طريقة وصولك المفضلة إلى SkillBridge' : 'Choose Your SkillBridge Platform'}
                </h2>
                <p className="text-xs sm:text-sm text-[#4C637C] dark:text-[#93ADC8]">
                  {isArabic
                    ? 'الويب، تطبيق Android APK، وتطبيق Windows EXE — نفس الهوية والمزايا وقاعدة البيانات الموحدة.'
                    : 'Web App, Native Android APK, and Windows Desktop Setup — unified identity, workflow & central database.'}
                </p>
              </div>
              <PWAInstallButton variant="primary" onOpenDownloadCenter={() => setRoute('downloads')} />
            </div>
          </MotionReveal>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
            {orderedAccessCards.map((card, idx) => (
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
                          {isArabic ? 'موصى به لجهازك' : 'Recommended for your device'}
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
                    {card.href ? (
                      <a
                        href={card.href}
                        download={card.downloadName}
                        className={`sb-btn-interactive w-full rounded-xl py-2.5 px-4 text-xs font-bold transition flex items-center justify-center gap-1.5 whitespace-nowrap ${
                          card.recommended
                            ? 'bg-[#00B4DB] hover:bg-[#008CAE] text-white shadow-[0_8px_20px_-8px_rgba(0,180,219,0.7)]'
                            : 'bg-[#0B1F38] hover:bg-[#132D4F] dark:bg-[#00B4DB] dark:text-[#061120] dark:hover:bg-[#7EE8F5] text-white'
                        }`}
                      >
                        <Download className="w-3.5 h-3.5 shrink-0" />
                        {card.ctaLabel}
                      </a>
                    ) : (
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
                    )}

                    {card.secondaryAction && (
                      <button
                        type="button"
                        onClick={card.secondaryAction}
                        className="w-full text-[11px] font-semibold text-[#4C637C] dark:text-[#93ADC8] hover:text-[#008CAE] dark:hover:text-[#7EE8F5] py-1 transition"
                      >
                        {card.secondaryLabel}
                      </button>
                    )}
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
          <span>SkillBridge · Web · Android · Windows</span>
          <button
            type="button"
            onClick={() => setRoute('downloads')}
            className="font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline"
          >
            {strings.downloadCenterTitle}
          </button>
        </footer>
      </main>
      <ToastBanner />
    </div>
  );
};

export const DownloadCenterScreen: React.FC = () => {
  const { isArabic, strings, setRoute, session, sseConnected, lastSyncedMs } = useSkillBridge();
  const platform = useMemo(() => detectDevicePlatform(), []);
  const { isInstallable, isInstalled, isIOS } = usePWAInstall();
  const [downloadsStatus, setDownloadsStatus] = useState<DownloadsStatusResponse | null>(null);
  const [loadingStatus, setLoadingStatus] = useState(true);

  useEffect(() => {
    setLoadingStatus(true);
    fetchDownloadsStatusApi()
      .then(setDownloadsStatus)
      .catch(() => {})
      .finally(() => setLoadingStatus(false));
  }, []);

  const android = downloadsStatus?.android;
  const windows = downloadsStatus?.windows;

  const parityMatrix = [
    {
      feature: isArabic ? 'الحسابات والصلاحيات (طالب / شركة / مدير إشراف / مشرف عام)' : 'Unified Auth & 4-Role RBAC Portals',
    },
    {
      feature: isArabic ? 'تحليل الفجوات بالذكاء الاصطناعي والمدرب التفاعلي' : 'AI Skill Gap Analyzer & Streaming Coach',
    },
    {
      feature: isArabic ? 'المشاريع الحية، تسليم الأعمال، ومراجعة المدير' : 'Live Company Briefs, Tasks & Rubric Verification',
    },
    {
      feature: isArabic ? 'المزامنة اللحظية عبر الأجهزة وإدارة الجلسات' : 'Real-Time Cross-Device Sync (Central REST + SSE)',
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

        {/* Smart Detection Banner */}
        <SpatialCard
          tiltStrength={2.5}
          highlight
          className="sb-blue-atmosphere rounded-2xl border border-[#00B4DB]/40 bg-[#0B1F38] text-white p-6 sm:p-7"
        >
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-5">
            <div className="relative z-10 space-y-1.5">
              <p className="text-xs font-semibold text-[#7EE8F5]">
                {isArabic ? `تم اكتشاف المنصة: ${platform.osLabel}` : `Detected Platform: ${platform.osLabel}`}
              </p>
              <h1 className="font-display text-2xl sm:text-3xl font-bold text-white">
                {strings.downloadCenterTitle}
              </h1>
              <p className="text-xs sm:text-sm text-[#B7CCE0] max-w-xl leading-relaxed">
                {isArabic
                  ? 'اختر استخدام الويب مباشرة أو حمّل الحزمة الأصلية لتطبيق Android (.apk) أو برنامج سطح المكتب لـ Windows (.exe) — جميعها متصلة بقاعدة البيانات المركزية نفسها.'
                  : 'Use SkillBridge directly online, download the native Android APK build, or install the standalone Windows Desktop application (.exe) — all connected to the exact same central database.'}
              </p>
            </div>
            <div className="relative z-10 shrink-0">
              {platform.recommendedTarget === 'MOBILE' && android?.available ? (
                <a
                  href={android.downloadUrl}
                  download={android.filename}
                  className="sb-btn-interactive inline-flex items-center gap-2 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2.5 text-xs font-bold text-white whitespace-nowrap shadow-[0_8px_22px_-8px_rgba(0,180,219,0.8)]"
                >
                  <Download className="w-4 h-4" />
                  {isArabic ? 'تحميل تطبيق Android (.apk)' : 'Download for Android (.apk)'}
                </a>
              ) : windows?.available ? (
                <a
                  href={windows.downloadUrl}
                  download={windows.filename}
                  className="sb-btn-interactive inline-flex items-center gap-2 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2.5 text-xs font-bold text-white whitespace-nowrap shadow-[0_8px_22px_-8px_rgba(0,180,219,0.8)]"
                >
                  <Download className="w-4 h-4" />
                  {isArabic ? 'تحميل لنظام Windows (.exe)' : 'Download for Windows (.exe)'}
                </a>
              ) : (
                <PWAInstallButton variant="primary" />
              )}
            </div>
          </div>
        </SpatialCard>

        {/* Option 1: Web Application */}
        <MotionReveal delayMs={60}>
          <SpatialCard
            tiltStrength={2}
            className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white/95 dark:bg-[#0D1E36]/95 p-5"
          >
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="flex items-start gap-3.5">
                <AnimatedPlatformEmblem kind="WEB" />
                <div>
                  <h2 className="font-display text-lg font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                    <span className="font-mono text-xs text-[#00B4DB] me-2">01.</span>
                    {isArabic ? 'تطبيق الويب (بدون تثبيت)' : 'Web Application (Zero-Install Browser Access)'}
                  </h2>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] mt-1">
                    {isArabic
                      ? 'افتح SkillBridge فورًا من أي متصفح مع مزامنة لحظية كاملة لجميع الأدوار الأربعة.'
                      : 'Launch the full SkillBridge ecosystem instantly in Chrome, Edge, Safari, or Firefox with live SSE sync.'}
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setRoute(session ? 'home' : 'welcome')}
                className="sb-btn-interactive rounded-xl bg-[#0B1F38] dark:bg-[#00B4DB] text-white dark:text-[#061120] px-4 py-2 text-xs font-bold whitespace-nowrap shrink-0"
              >
                {strings.getStarted}
              </button>
            </div>
          </SpatialCard>
        </MotionReveal>

        {/* Option 2: Native Android Mobile Application */}
        <MotionReveal delayMs={120}>
          <SpatialCard
            tiltStrength={2}
            className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white/95 dark:bg-[#0D1E36]/95 p-5 space-y-4"
          >
            <div className="flex items-center justify-between gap-2">
              <div className="flex items-center gap-3">
                <AnimatedPlatformEmblem kind="MOBILE" active={Boolean(android?.available)} />
                <h2 className="font-display text-lg font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                  <span className="font-mono text-xs text-[#00B4DB] me-2">02.</span>
                  {isArabic ? 'تطبيق الموبايل (Android APK + Web App)' : 'Mobile Application (Android Native APK & iOS Web App)'}
                </h2>
              </div>
              {android?.available && <StatusLabel status="ACTIVE" />}
            </div>

            <div className="divide-y divide-[#D5E0EC] dark:divide-[#1C395E]">
              <div className="py-3 first:pt-1 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <p className="text-sm font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">
                    {isArabic ? 'تطبيق Android الأصلي (Kotlin / Jetpack Compose APK)' : 'Native Android Application (SkillBridge-Android.apk)'}
                  </p>
                  {android?.available ? (
                    <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] tabular-nums">
                      {formatMb(android.sizeBytes)} · SHA-256: <span className="font-mono text-[#008CAE] dark:text-[#7EE8F5]">{android.sha256.slice(0, 12)}</span> ·{' '}
                      {isArabic ? 'متصل بقاعدة البيانات المركزية والمزامنة الفورية' : 'Connected to shared central backend & Firestore'}
                    </p>
                  ) : (
                    <p className="text-xs text-amber-700 dark:text-amber-300">
                      {loadingStatus ? 'Checking build artifact...' : 'Android APK build not found in /downloads directory yet.'}
                    </p>
                  )}
                </div>
                <div className="shrink-0">
                  {android?.available ? (
                    <a
                      href={android.downloadUrl}
                      download={android.filename}
                      className="sb-btn-interactive inline-flex items-center gap-1.5 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white whitespace-nowrap"
                    >
                      <Download className="w-4 h-4" />
                      {isArabic ? 'تحميل لنظام Android (.apk)' : 'Download for Android'}
                    </a>
                  ) : null}
                </div>
              </div>

              <div className="py-3 last:pb-0 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div>
                  <p className="text-sm font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">
                    {isArabic ? 'تثبيت الموبايل عبر المتصفح (Android & iOS Safari PWA)' : 'Mobile Browser Install (Android Chrome & iOS Safari)'}
                  </p>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] mt-0.5">
                    {isIOS
                      ? isArabic
                        ? 'على iPhone/iPad: افتح قائمة المشاركة في Safari واختر "إضافة إلى الشاشة الرئيسية".'
                        : 'On iPhone/iPad: tap Safari Share -> "Add to Home Screen" for full mobile mode.'
                      : isArabic
                        ? 'شريط تنقل سفلي مريح للإبهام وتجربة تطبيق مستقلة من المتصفح.'
                        : 'Thumb-friendly bottom tab bar & standalone mobile web app mode.'}
                  </p>
                </div>
                <div className="shrink-0">
                  <PWAInstallButton variant="compact" />
                </div>
              </div>
            </div>
          </SpatialCard>
        </MotionReveal>

        {/* Option 3: Dedicated Windows Desktop Application */}
        <MotionReveal delayMs={180}>
          <SpatialCard
            tiltStrength={2}
            className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white/95 dark:bg-[#0D1E36]/95 p-5 space-y-4"
          >
            <div className="flex items-center justify-between gap-2">
              <div className="flex items-center gap-3">
                <AnimatedPlatformEmblem kind="DESKTOP" active={Boolean(windows?.available)} />
                <h2 className="font-display text-lg font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                  <span className="font-mono text-xs text-[#00B4DB] me-2">03.</span>
                  {isArabic ? 'تطبيق سطح المكتب (Windows .exe المستقل)' : 'Desktop Application (Windows .EXE Installer & Cross-OS App)'}
                </h2>
              </div>
              {windows?.available && <StatusLabel status="ACTIVE" />}
            </div>

            <div className="divide-y divide-[#D5E0EC] dark:divide-[#1C395E]">
              <div className="py-3 first:pt-1 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <p className="text-sm font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">
                    {isArabic ? 'برنامج SkillBridge لنظام Windows (مثبّت .exe مستقل)' : 'Windows Desktop Installer (SkillBridge-Windows-Setup.exe)'}
                  </p>
                  {windows?.available ? (
                    <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] tabular-nums">
                      {formatMb(windows.sizeBytes)} · NSIS 64-bit Installer · SHA-256:{' '}
                      <span className="font-mono text-[#008CAE] dark:text-[#7EE8F5]">{windows.sha256.slice(0, 12)}</span>
                    </p>
                  ) : (
                    <p className="text-xs text-amber-700 dark:text-amber-300">
                      {loadingStatus ? 'Checking build artifact...' : 'Windows installer binary not found in /downloads directory yet.'}
                    </p>
                  )}
                </div>
                <div className="shrink-0">
                  {windows?.available ? (
                    <a
                      href={windows.downloadUrl}
                      download={windows.filename}
                      className="sb-btn-interactive inline-flex items-center gap-1.5 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white whitespace-nowrap"
                    >
                      <Download className="w-4 h-4" />
                      {isArabic ? 'تحميل لنظام Windows (.exe)' : 'Download for Windows'}
                    </a>
                  ) : null}
                </div>
              </div>

              <div className="py-3 last:pb-0 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div>
                  <p className="text-sm font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">
                    {isArabic ? 'تثبيت سطح المكتب لـ macOS و Linux عبر المتصفح' : 'macOS & Linux Desktop Standalone Window (Chrome / Edge)'}
                  </p>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] mt-0.5">
                    {!isInstallable && !isInstalled && !isIOS
                      ? isArabic
                        ? 'استخدم أيقونة التثبيت في شريط عنوان المتصفح لفتح SkillBridge في نافذة مستقلة على macOS أو Linux.'
                        : 'Click the Install icon in Chrome/Edge address bar to run SkillBridge in a dedicated window on macOS or Linux.'
                      : isArabic
                        ? 'شريط جانبي موسّع ومساحة عمل متعددة الأعمدة مع مزامنة فورية.'
                        : 'Expanded navigation rail & multi-column analytics workspace.'}
                  </p>
                </div>
                <div className="shrink-0">
                  <PWAInstallButton variant="compact" />
                </div>
              </div>
            </div>
          </SpatialCard>
        </MotionReveal>

        {/* Cross-Platform Parity & Real-Time Sync Verification */}
        <MotionReveal delayMs={240}>
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white/95 dark:bg-[#0D1E36]/95 p-5 space-y-3">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <h2 className="font-display text-lg font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {isArabic ? 'التكافؤ والمزامنة عبر المنصات الثلاث' : 'Cross-Platform Parity & Central Sync Verification'}
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
              {parityMatrix.map((row) => (
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
