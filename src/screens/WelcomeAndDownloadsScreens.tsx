import React, { useEffect, useMemo, useState } from 'react';
import {
  ArrowLeft,
  ArrowRight,
  CheckCircle2,
  Download,
  Globe,
  Laptop,
  Lock,
  Moon,
  RefreshCw,
  ShieldCheck,
  Smartphone,
  Sparkles,
  Sun,
  UserPlus,
} from 'lucide-react';
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

  const scrollToAuth = () => {
    const el = document.getElementById('sb-auth-card');
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  };

  const androidReady = downloadsStatus?.android.available ?? false;
  const windowsReady = downloadsStatus?.windows.available ?? false;

  const accessCards = [
    {
      id: 'WEB',
      title: strings.useOnlineTitle,
      desc: strings.useOnlineDesc,
      badge: isArabic ? 'متاح مباشرة' : 'Instant Browser Access',
      recommended: platform.recommendedTarget === 'WEB',
      icon: Globe,
      ctaLabel: strings.getStarted,
      href: undefined as string | undefined,
      downloadName: undefined as string | undefined,
      onClick: scrollToAuth,
      secondaryAction: undefined as (() => void) | undefined,
      secondaryLabel: undefined as string | undefined,
    },
    {
      id: 'MOBILE',
      title: strings.mobileAppTitle,
      desc: androidReady
        ? isArabic
          ? `ملف تثبيت Android APK حقيقي (${formatMb(downloadsStatus!.android.sizeBytes)}) متزامن مع قاعدة البيانات المركزية.`
          : `Official signed Android APK build (${formatMb(downloadsStatus!.android.sizeBytes)}) wired to the same live central backend.`
        : strings.mobileAppDesc,
      badge: androidReady ? (isArabic ? 'حزمة Android APK جاهزة' : 'Android APK Ready') : isArabic ? 'تحميل الموبايل' : 'Mobile Access',
      recommended: platform.recommendedTarget === 'MOBILE',
      icon: Smartphone,
      ctaLabel: androidReady
        ? isArabic
          ? 'تحميل تطبيق Android (.apk)'
          : 'Download for Android (.apk)'
        : isArabic
          ? 'عرض خيارات الموبايل'
          : 'Open Mobile Download',
      href: androidReady ? downloadsStatus!.android.downloadUrl : undefined,
      downloadName: androidReady ? downloadsStatus!.android.filename : undefined,
      onClick: androidReady ? undefined : () => setRoute('downloads'),
      secondaryAction: () => setRoute('downloads'),
      secondaryLabel: isArabic ? 'التفاصيل والتحقق' : 'All Mobile Options',
    },
    {
      id: 'DESKTOP',
      title: strings.desktopAppTitle,
      desc: windowsReady
        ? isArabic
          ? `مثبت Windows الرسمي (${formatMb(downloadsStatus!.windows.sizeBytes)}) بواجهة سطح مكتب متكاملة ومزامنة فورية.`
          : `Dedicated Windows installer (${formatMb(downloadsStatus!.windows.sizeBytes)}) launching as an independent SkillBridge desktop app.`
        : strings.desktopAppDesc,
      badge: windowsReady ? (isArabic ? 'مثبت Windows (.exe) جاهز' : 'Windows .EXE Ready') : isArabic ? 'تطبيق سطح المكتب' : 'Desktop Access',
      recommended: platform.recommendedTarget === 'DESKTOP',
      icon: Laptop,
      ctaLabel: windowsReady
        ? isArabic
          ? 'تحميل لنظام Windows (.exe)'
          : 'Download for Windows (.exe)'
        : isArabic
          ? 'عرض خيارات سطح المكتب'
          : 'Open Desktop Download',
      href: windowsReady ? downloadsStatus!.windows.downloadUrl : undefined,
      downloadName: windowsReady ? downloadsStatus!.windows.filename : undefined,
      onClick: windowsReady ? undefined : () => setRoute('downloads'),
      secondaryAction: () => setRoute('downloads'),
      secondaryLabel: isArabic ? 'التفاصيل والتحقق' : 'All Desktop Options',
    },
  ];

  // Smart ordering prioritizes detected platform without restricting user choice
  const orderedAccessCards = [...accessCards].sort((a, b) => Number(b.recommended) - Number(a.recommended));

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
    <div className="min-h-screen bg-[#F7FAFC] dark:bg-slate-950 text-[#0B1F38] dark:text-slate-100">
      {/* Top Minimal Header */}
      <header className="sticky top-0 z-20 bg-white/95 dark:bg-slate-900/95 backdrop-blur border-b border-[#D5E0EC] dark:border-slate-800">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-3">
          <SkillBridgeLogo size="md" subtitle />
          <div className="flex items-center gap-2 shrink-0">
            <button
              type="button"
              onClick={() => setRoute('downloads')}
              className="hidden sm:inline-flex items-center gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-slate-700 px-3 py-1.5 text-xs font-semibold hover:bg-slate-100 dark:hover:bg-slate-800 whitespace-nowrap shrink-0"
            >
              <Download className="w-3.5 h-3.5 text-[#00B4DB]" />
              {strings.downloadCenterTitle}
            </button>
            <button
              type="button"
              onClick={toggleLanguage}
              className="rounded-xl border border-[#D5E0EC] dark:border-slate-700 px-3 py-1.5 text-xs font-semibold hover:bg-slate-100 dark:hover:bg-slate-800 whitespace-nowrap shrink-0"
            >
              {strings.languageToggleLabel}
            </button>
            <button
              type="button"
              onClick={toggleTheme}
              className="p-2 rounded-xl border border-[#D5E0EC] dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800"
              aria-label="Theme"
            >
              {darkTheme ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-8 sm:py-12 space-y-10">
        {/* Hero + Unified Auth Split */}
        <section className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-stretch">
          <div className="lg:col-span-7 flex flex-col justify-between space-y-5">
            <div className="space-y-4">
              <div className="inline-flex items-center gap-2 text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5]">
                <Sparkles className="w-4 h-4 shrink-0" />
                <span>
                  {isArabic
                    ? `تم التعرف على جهازك: ${platform.osLabel} · منتج واحد عبر الويب والأندرويد وسطح المكتب`
                    : `Detected Device: ${platform.osLabel} · One Product Across Web, Android & Windows Desktop`}
                </span>
              </div>

              <h1 className="font-display text-3xl sm:text-5xl font-bold tracking-tight text-[#0B1F38] dark:text-white leading-[1.12] [text-wrap:balance]">
                {strings.tagline}
              </h1>

              <p className="text-sm sm:text-base text-slate-600 dark:text-slate-300 leading-relaxed max-w-2xl">
                {strings.welcomeSubtitle}
              </p>
            </div>

            <div className="rounded-2xl overflow-hidden border border-[#D5E0EC] dark:border-slate-800 bg-[#0B1F38] relative">
              <img
                src="/assets/images/img_hero_bridge_1794605869240.jpg"
                alt="SkillBridge Unified Ecosystem"
                referrerPolicy="no-referrer"
                className="w-full h-52 sm:h-64 object-cover opacity-90"
              />
              <div className="bg-gradient-to-t from-[#0B1F38] via-[#0B1F38]/80 to-transparent p-4 sm:p-5 text-white">
                <p className="text-xs font-semibold text-[#7EE8F5]">
                  {isArabic
                    ? 'حساب واحد · قاعدة بيانات مركزية واحدة · مزامنة فورية عبر كل المنصات'
                    : 'One Account · One Authoritative Central Database · Real-Time Cross-Platform Sync'}
                </p>
              </div>
            </div>
          </div>

          {/* Right Auth Card */}
          <div
            id="sb-auth-card"
            className="lg:col-span-5 rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 sm:p-6 space-y-4 flex flex-col justify-between"
          >
            <div className="space-y-4">
              <div className="flex items-center justify-between border-b border-[#D5E0EC] dark:border-slate-800 pb-3">
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => {
                      clearAuthError();
                      setAuthTab('LOGIN');
                    }}
                    className={`rounded-lg px-3 py-1.5 text-xs font-bold transition whitespace-nowrap shrink-0 ${
                      authTab === 'LOGIN'
                        ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-slate-950'
                        : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
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
                    className={`rounded-lg px-3 py-1.5 text-xs font-bold transition whitespace-nowrap shrink-0 ${
                      authTab === 'REGISTER'
                        ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-slate-950'
                        : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
                    }`}
                  >
                    {isArabic ? 'حساب طالب جديد' : 'Register Student'}
                  </button>
                </div>
                <Lock className="w-4 h-4 text-[#008CAE]" />
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
                      <label className="block text-xs font-semibold text-slate-600 dark:text-slate-300 mb-1">
                        {isArabic ? 'اسم المستخدم أو البريد' : 'Username or Email'}
                      </label>
                      <input
                        type="text"
                        required
                        value={loginId}
                        onChange={(e) => setLoginId(e.target.value)}
                        className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-sm focus:outline-none focus:border-[#00B4DB]"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-slate-600 dark:text-slate-300 mb-1">
                        {isArabic ? 'كلمة المرور' : 'Password'}
                      </label>
                      <input
                        type="password"
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-sm focus:outline-none focus:border-[#00B4DB]"
                      />
                    </div>
                  </>
                ) : (
                  <>
                    <div>
                      <label className="block text-xs font-semibold text-slate-600 dark:text-slate-300 mb-1">
                        {isArabic ? 'الاسم الكامل' : 'Full Name'}
                      </label>
                      <input
                        type="text"
                        required
                        value={regName}
                        onChange={(e) => setRegName(e.target.value)}
                        placeholder="e.g. Nadia Samir"
                        className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-sm"
                      />
                    </div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                      <div>
                        <label className="block text-xs font-semibold text-slate-600 dark:text-slate-300 mb-1">
                          {isArabic ? 'معرف الدخول' : 'Handle'}
                        </label>
                        <input
                          type="text"
                          required
                          value={regHandle}
                          onChange={(e) => setRegHandle(e.target.value)}
                          placeholder="nadia_dev"
                          className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-sm"
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-semibold text-slate-600 dark:text-slate-300 mb-1">
                          {isArabic ? 'البريد الإلكتروني' : 'Email'}
                        </label>
                        <input
                          type="email"
                          required
                          value={regEmail}
                          onChange={(e) => setRegEmail(e.target.value)}
                          placeholder="nadia@example.com"
                          className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-sm"
                        />
                      </div>
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-slate-600 dark:text-slate-300 mb-1">
                        {isArabic ? 'كلمة المرور (٦ أحرف فأكثر)' : 'Password (6+ chars)'}
                      </label>
                      <input
                        type="password"
                        required
                        minLength={6}
                        value={regPass}
                        onChange={(e) => setRegPass(e.target.value)}
                        className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-sm"
                      />
                    </div>
                  </>
                )}

                <button
                  type="submit"
                  disabled={authBusy}
                  className="w-full rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] disabled:opacity-60 py-2.5 text-sm font-bold text-white transition flex items-center justify-center gap-2 whitespace-nowrap"
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
                  <p className="text-[11px] font-semibold text-slate-500 dark:text-slate-400">
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
                        className="rounded-lg border border-[#D5E0EC] dark:border-slate-700 px-2.5 py-1.5 text-[11px] font-semibold text-start hover:bg-slate-50 dark:hover:bg-slate-800 transition"
                      >
                        <span className="block text-[#0B1F38] dark:text-white truncate">
                          {isArabic ? acc.labelAr : acc.labelEn}
                        </span>
                        <span className="block text-[10px] text-slate-500 truncate">{acc.loginId}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <div className="pt-2 border-t border-[#D5E0EC] dark:border-slate-800 space-y-2">
              <button
                type="button"
                disabled={authBusy}
                onClick={signInWithGoogle}
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700 py-2 text-xs font-semibold text-[#0B1F38] dark:text-white transition"
              >
                {strings.signInGoogle}
              </button>
              <button
                type="button"
                onClick={enterGuestPreview}
                className="w-full rounded-xl py-2 text-xs font-semibold text-slate-600 dark:text-slate-400 hover:text-[#0B1F38] dark:hover:text-white transition"
              >
                {strings.continueAsGuest}
              </button>
            </div>
          </div>
        </section>

        {/* Three Platform Access Options (Smart Prioritized Order, Direct Downloads, Zero Lock-In) */}
        <section className="space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <h2 className="font-display text-xl font-bold text-[#0B1F38] dark:text-white">
                {isArabic ? 'اختر طريقة وصولك المفضلة إلى SkillBridge' : 'Choose Your SkillBridge Platform'}
              </h2>
              <p className="text-xs text-slate-600 dark:text-slate-400 mt-0.5">
                {isArabic
                  ? 'الويب، تطبيق Android APK، وتطبيق Windows EXE — نفس الهوية والمزايا وقاعدة البيانات الموحدة.'
                  : 'Web App, Native Android APK, and Windows Desktop Setup — identical design, workflow & central database.'}
              </p>
            </div>
            <PWAInstallButton variant="primary" onOpenDownloadCenter={() => setRoute('downloads')} />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {orderedAccessCards.map((card) => {
              const Icon = card.icon;
              return (
                <div
                  key={card.id}
                  className={`rounded-2xl border p-5 flex flex-col justify-between gap-4 bg-white dark:bg-slate-900 ${
                    card.recommended
                      ? 'border-[#00B4DB] ring-1 ring-[#00B4DB]/30'
                      : 'border-[#D5E0EC] dark:border-slate-800'
                  }`}
                >
                  <div className="space-y-3">
                    <div className="flex items-center justify-between gap-2">
                      <div className="w-10 h-10 rounded-xl bg-[#E0F9FD] dark:bg-slate-800 flex items-center justify-center text-[#008CAE] dark:text-[#7EE8F5]">
                        <Icon className="w-5 h-5" />
                      </div>
                      <span className="text-[11px] font-semibold text-[#008CAE] dark:text-[#7EE8F5]">
                        {card.recommended
                          ? isArabic
                            ? 'موصى به لجهازك'
                            : 'Recommended for your device'
                          : card.badge}
                      </span>
                    </div>
                    <div>
                      <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-white">{card.title}</h3>
                      <p className="text-xs text-slate-600 dark:text-slate-300 mt-1 leading-relaxed">{card.desc}</p>
                    </div>
                  </div>

                  <div className="space-y-2">
                    {card.href ? (
                      <a
                        href={card.href}
                        download={card.downloadName}
                        className={`w-full rounded-xl py-2.5 px-4 text-xs font-bold transition flex items-center justify-center gap-1.5 whitespace-nowrap ${
                          card.recommended
                            ? 'bg-[#0B1F38] dark:bg-[#00B4DB] text-white dark:text-slate-950 hover:opacity-90'
                            : 'bg-[#00B4DB] hover:bg-[#008CAE] text-white'
                        }`}
                      >
                        <Download className="w-3.5 h-3.5 shrink-0" />
                        {card.ctaLabel}
                      </a>
                    ) : (
                      <button
                        type="button"
                        onClick={card.onClick}
                        className={`w-full rounded-xl py-2.5 px-4 text-xs font-bold transition whitespace-nowrap ${
                          card.recommended
                            ? 'bg-[#0B1F38] dark:bg-[#00B4DB] text-white dark:text-slate-950 hover:opacity-90'
                            : 'border border-[#D5E0EC] dark:border-slate-700 text-[#0B1F38] dark:text-white hover:bg-slate-50 dark:hover:bg-slate-800'
                        }`}
                      >
                        {card.ctaLabel}
                      </button>
                    )}

                    {card.secondaryAction && (
                      <button
                        type="button"
                        onClick={card.secondaryAction}
                        className="w-full text-[11px] font-semibold text-slate-500 hover:text-[#008CAE] py-1"
                      >
                        {card.secondaryLabel}
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </section>
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
    <div className="min-h-screen bg-[#F7FAFC] dark:bg-slate-950 text-[#0B1F38] dark:text-slate-100">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 py-6 sm:py-8 space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <button
            type="button"
            onClick={() => setRoute(session ? 'home' : 'welcome')}
            className="inline-flex items-center gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2 text-xs font-semibold hover:bg-slate-50 dark:hover:bg-slate-800 whitespace-nowrap"
          >
            {isArabic ? <ArrowRight className="w-4 h-4" /> : <ArrowLeft className="w-4 h-4" />}
            {strings.backLabel}
          </button>
          <SkillBridgeLogo size="sm" subtitle />
        </div>

        {/* Smart Detection Banner */}
        <div className="rounded-2xl border border-[#00B4DB]/40 bg-[#0B1F38] text-white p-5 sm:p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1.5">
            <p className="text-xs font-semibold text-[#7EE8F5]">
              {isArabic ? `تم اكتشاف المنصة: ${platform.osLabel}` : `Detected Platform: ${platform.osLabel}`}
            </p>
            <h1 className="font-display text-xl sm:text-2xl font-bold">{strings.downloadCenterTitle}</h1>
            <p className="text-xs text-slate-300 max-w-xl">
              {isArabic
                ? 'اختر استخدام الويب مباشرة أو حمّل الحزمة الأصلية لتطبيق Android (.apk) أو برنامج سطح المكتب لـ Windows (.exe) — جميعها متصلة بقاعدة البيانات المركزية نفسها.'
                : 'Use SkillBridge directly online, download the native Android APK build, or install the standalone Windows Desktop application (.exe) — all connected to the exact same central database.'}
            </p>
          </div>
          <div className="shrink-0">
            {platform.recommendedTarget === 'MOBILE' && android?.available ? (
              <a
                href={android.downloadUrl}
                download={android.filename}
                className="inline-flex items-center gap-2 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2.5 text-xs font-bold text-white whitespace-nowrap"
              >
                <Download className="w-4 h-4" />
                {isArabic ? 'تحميل تطبيق Android (.apk)' : 'Download for Android (.apk)'}
              </a>
            ) : windows?.available ? (
              <a
                href={windows.downloadUrl}
                download={windows.filename}
                className="inline-flex items-center gap-2 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2.5 text-xs font-bold text-white whitespace-nowrap"
              >
                <Download className="w-4 h-4" />
                {isArabic ? 'تحميل لنظام Windows (.exe)' : 'Download for Windows (.exe)'}
              </a>
            ) : (
              <PWAInstallButton variant="primary" />
            )}
          </div>
        </div>

        {/* Option 1: Web Application */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <h2 className="font-display text-base font-bold text-[#0B1F38] dark:text-white">
                {isArabic ? '١. تطبيق الويب (بدون تثبيت)' : '1. Web Application (Zero-Install Browser Access)'}
              </h2>
              <p className="text-xs text-slate-600 dark:text-slate-400 mt-1">
                {isArabic
                  ? 'افتح SkillBridge فورًا من أي متصفح مع مزامنة لحظية كاملة لجميع الأدوار الأربعة.'
                  : 'Launch the full SkillBridge ecosystem instantly in Chrome, Edge, Safari, or Firefox with live SSE sync.'}
              </p>
            </div>
            <button
              type="button"
              onClick={() => setRoute(session ? 'home' : 'welcome')}
              className="rounded-xl bg-[#0B1F38] dark:bg-[#00B4DB] text-white dark:text-slate-950 px-4 py-2 text-xs font-bold whitespace-nowrap shrink-0"
            >
              {strings.getStarted}
            </button>
          </div>
        </div>

        {/* Option 2: Native Android Mobile Application */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-4">
          <div className="flex items-center justify-between gap-2">
            <h2 className="font-display text-base font-bold text-[#0B1F38] dark:text-white">
              {isArabic ? '٢. تطبيق الموبايل (Android APK + Web App)' : '2. Mobile Application (Android Native APK & iOS Web App)'}
            </h2>
            {android?.available && <StatusLabel status="ACTIVE" />}
          </div>

          <div className="divide-y divide-slate-100 dark:divide-slate-800">
            <div className="py-3 first:pt-1 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="space-y-1">
                <p className="text-sm font-semibold text-[#0B1F38] dark:text-white">
                  {isArabic ? 'تطبيق Android الأصلي (Kotlin / Jetpack Compose APK)' : 'Native Android Application (SkillBridge-Android.apk)'}
                </p>
                {android?.available ? (
                  <p className="text-xs text-slate-600 dark:text-slate-400 tabular-nums">
                    {formatMb(android.sizeBytes)} · SHA-256: <span className="font-mono">{android.sha256.slice(0, 12)}</span> ·{' '}
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
                    className="inline-flex items-center gap-1.5 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white whitespace-nowrap"
                  >
                    <Download className="w-4 h-4" />
                    {isArabic ? 'تحميل لنظام Android (.apk)' : 'Download for Android'}
                  </a>
                ) : null}
              </div>
            </div>

            <div className="py-3 last:pb-0 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <p className="text-sm font-semibold text-[#0B1F38] dark:text-white">
                  {isArabic ? 'تثبيت الموبايل عبر المتصفح (Android & iOS Safari PWA)' : 'Mobile Browser Install (Android Chrome & iOS Safari)'}
                </p>
                <p className="text-xs text-slate-600 dark:text-slate-400 mt-0.5">
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
        </div>

        {/* Option 3: Dedicated Windows Desktop Application */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-4">
          <div className="flex items-center justify-between gap-2">
            <h2 className="font-display text-base font-bold text-[#0B1F38] dark:text-white">
              {isArabic ? '٣. تطبيق سطح المكتب (Windows .exe المستقل)' : '3. Desktop Application (Windows .EXE Installer & Cross-OS App)'}
            </h2>
            {windows?.available && <StatusLabel status="ACTIVE" />}
          </div>

          <div className="divide-y divide-slate-100 dark:divide-slate-800">
            <div className="py-3 first:pt-1 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div className="space-y-1">
                <p className="text-sm font-semibold text-[#0B1F38] dark:text-white">
                  {isArabic ? 'برنامج SkillBridge لنظام Windows (مثبّت .exe مستقل)' : 'Windows Desktop Installer (SkillBridge-Windows-Setup.exe)'}
                </p>
                {windows?.available ? (
                  <p className="text-xs text-slate-600 dark:text-slate-400 tabular-nums">
                    {formatMb(windows.sizeBytes)} · NSIS 64-bit Installer · SHA-256:{' '}
                    <span className="font-mono">{windows.sha256.slice(0, 12)}</span>
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
                    className="inline-flex items-center gap-1.5 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white whitespace-nowrap"
                  >
                    <Download className="w-4 h-4" />
                    {isArabic ? 'تحميل لنظام Windows (.exe)' : 'Download for Windows'}
                  </a>
                ) : null}
              </div>
            </div>

            <div className="py-3 last:pb-0 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <p className="text-sm font-semibold text-[#0B1F38] dark:text-white">
                  {isArabic ? 'تثبيت سطح المكتب لـ macOS و Linux عبر المتصفح' : 'macOS & Linux Desktop Standalone Window (Chrome / Edge)'}
                </p>
                <p className="text-xs text-slate-600 dark:text-slate-400 mt-0.5">
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
        </div>

        {/* Cross-Platform Parity & Real-Time Sync Verification */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h2 className="font-display text-base font-bold text-[#0B1F38] dark:text-white">
              {isArabic ? 'التكافؤ والمزامنة عبر المنصات الثلاث' : 'Cross-Platform Parity & Central Sync Verification'}
            </h2>
            <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-emerald-700 dark:text-emerald-400">
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
                className="rounded-xl border border-slate-200 dark:border-slate-800 px-3.5 py-2.5 flex items-center justify-between gap-2"
              >
                <span className="text-xs font-medium text-slate-700 dark:text-slate-200 flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-[#00B4DB] shrink-0" />
                  {row.feature}
                </span>
                <ShieldCheck className="w-4 h-4 text-emerald-600 shrink-0" />
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
