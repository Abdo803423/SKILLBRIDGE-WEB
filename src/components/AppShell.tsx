import React from 'react';
import {
  Activity,
  Award,
  Bell,
  Bot,
  Briefcase,
  Building2,
  CheckCircle2,
  ClipboardCheck,
  Compass,
  Download,
  FolderGit2,
  Globe,
  Home,
  LayoutDashboard,
  LogOut,
  MessageSquare,
  Moon,
  Search,
  Settings,
  ShieldAlert,
  Sparkles,
  Sun,
  Trophy,
  UserCheck,
  Users,
  Wallet,
} from 'lucide-react';
import { type ActiveRoute, useSkillBridge } from '../context/SkillBridgeContext';
import { PWAInstallButton } from './PWAInstallButton';
import { SkillBridgeLogo, ToastBanner } from './SkillBridgeUi';

interface NavEntry {
  id: ActiveRoute;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
}

export const AppShell: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const {
    route,
    setRoute,
    session,
    isGuestPreview,
    strings,
    isArabic,
    darkTheme,
    toggleLanguage,
    toggleTheme,
    signOutAll,
    sseConnected,
    isOnline,
    snapshot,
    localNotifications,
    setAiDrawerOpen,
    profile,
  } = useSkillBridge();

  const role = session?.role ?? 'STUDENT';

  const unreadCentralCount = snapshot?.notifications.filter((n) => !n.isRead).length ?? 0;
  const unreadLocalCount = session ? 0 : localNotifications.filter((n) => n.isUnread).length;
  const unreadTotal = unreadCentralCount + unreadLocalCount;

  const navItems: NavEntry[] = (() => {
    if (role === 'PLATFORM_ADMIN') {
      return [
        { id: 'adminHome', label: isArabic ? 'نظرة عامة' : 'Admin Overview', icon: LayoutDashboard },
        { id: 'adminQueues', label: isArabic ? 'طوابير الاعتماد' : 'Verification Queues', icon: ClipboardCheck },
        { id: 'adminUsers', label: isArabic ? 'الحسابات والفرق' : 'Users & Access', icon: Users },
        { id: 'adminLogs', label: isArabic ? 'السجل والمراقبة' : 'Activity Ledger', icon: Activity },
        { id: 'downloads', label: isArabic ? 'التحميل والمنصات' : 'Download Center', icon: Download },
        { id: 'settings', label: strings.accountSettingsTitle, icon: Settings },
      ];
    }
    if (role === 'COMPANY') {
      return [
        { id: 'companyHome', label: isArabic ? 'لوحة الشركة' : 'Company Hub', icon: Building2 },
        { id: 'companyBriefs', label: isArabic ? 'مشاريعنا' : 'Our Briefs', icon: Briefcase },
        { id: 'companyApplicants', label: isArabic ? 'المتقدمون' : 'Applicants', icon: UserCheck },
        { id: 'companyTalent', label: isArabic ? 'المواهب الموثّقة' : 'Verified Talent', icon: Award },
        { id: 'companyAnalytics', label: isArabic ? 'الأثر والتحليلات' : 'Analytics', icon: Activity },
        { id: 'downloads', label: isArabic ? 'التحميل والمنصات' : 'Download Center', icon: Download },
        { id: 'settings', label: strings.accountSettingsTitle, icon: Settings },
      ];
    }
    if (role === 'COMPANY_DIRECTOR') {
      return [
        { id: 'directorHome', label: isArabic ? 'بوابة الإشراف' : 'Director Studio', icon: ShieldAlert },
        { id: 'directorProjects', label: isArabic ? 'المشاريع المسندة' : 'Supervised Briefs', icon: FolderGit2 },
        { id: 'directorStudents', label: isArabic ? 'مهام الطلاب' : 'Student Tasks', icon: Users },
        { id: 'directorReviews', label: isArabic ? 'مراجعة التسليمات' : 'Rubric Reviews', icon: CheckCircle2 },
        { id: 'directorHistory', label: isArabic ? 'سجل التقييمات' : 'Evaluations History', icon: Trophy },
        { id: 'downloads', label: isArabic ? 'التحميل والمنصات' : 'Download Center', icon: Download },
        { id: 'settings', label: strings.accountSettingsTitle, icon: Settings },
      ];
    }
    return [
      { id: 'home', label: strings.navHome, icon: Home },
      { id: 'discover', label: strings.navDiscover, icon: Compass },
      { id: 'briefs', label: isArabic ? 'مشاريع الشركات' : 'Company Briefs', icon: Briefcase },
      { id: 'community', label: strings.navCommunity, icon: MessageSquare },
      { id: 'teams', label: strings.navTeams, icon: Users },
      { id: 'passport', label: strings.navPassport, icon: Award },
      { id: 'analysis', label: strings.skillAnalysisTitle, icon: Sparkles },
      { id: 'badges', label: strings.badgesTitle, icon: Trophy },
      { id: 'wallet', label: strings.walletTitle, icon: Wallet },
      { id: 'leaderboard', label: strings.leaderboardTitle, icon: Activity },
      { id: 'coach', label: isArabic ? 'المدرب الذكي' : 'AI Coach', icon: Bot },
      { id: 'downloads', label: isArabic ? 'التحميل والمنصات' : 'Download Center', icon: Download },
      { id: 'blueprint', label: strings.blueprintTitle, icon: Globe },
      { id: 'settings', label: strings.accountSettingsTitle, icon: Settings },
    ];
  })();

  const mobileBottomItems: NavEntry[] = (() => {
    if (role === 'PLATFORM_ADMIN' || role === 'COMPANY' || role === 'COMPANY_DIRECTOR') {
      return navItems.slice(0, 4);
    }
    return [
      { id: 'home', label: strings.navHome, icon: Home },
      { id: 'discover', label: strings.navDiscover, icon: Compass },
      { id: 'community', label: strings.navCommunity, icon: MessageSquare },
      { id: 'teams', label: strings.navTeams, icon: Users },
      { id: 'passport', label: strings.navPassport, icon: Award },
    ];
  })();

  const activeTitle =
    navItems.find((n) => n.id === route)?.label ??
    (route === 'notifications'
      ? strings.notificationsTitle
      : route === 'search'
        ? strings.searchPlaceholder
        : strings.appName);

  const roleDisplayName =
    role === 'PLATFORM_ADMIN'
      ? strings.roleAdmin
      : role === 'COMPANY'
        ? strings.roleCompany
        : role === 'COMPANY_DIRECTOR'
          ? strings.roleDirector
          : strings.roleLearner;

  return (
    <div className="min-h-screen bg-[#F7FAFC] dark:bg-slate-950 text-[#0B1F38] dark:text-slate-100 flex">
      {/* Desktop Left Navigation Rail */}
      <aside className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0 md:start-0 z-30 bg-white dark:bg-slate-900 border-e border-[#D5E0EC] dark:border-slate-800">
        <div className="h-16 px-4 flex items-center justify-between border-b border-[#D5E0EC] dark:border-slate-800">
          <button type="button" onClick={() => setRoute(navItems[0].id)} className="text-start">
            <SkillBridgeLogo size="sm" subtitle />
          </button>
        </div>

        {/* Account & Role Identity Strip */}
        <div className="px-4 py-3 border-b border-[#D5E0EC] dark:border-slate-800 bg-slate-50/70 dark:bg-slate-950/40">
          <p className="text-xs font-bold text-[#0B1F38] dark:text-white truncate">
            {session ? session.displayName : profile.displayName}
          </p>
          <p className="text-[11px] text-[#008CAE] dark:text-[#7EE8F5] font-semibold mt-0.5">
            {isGuestPreview ? (isArabic ? 'زائر (معاينة)' : 'Guest Preview') : roleDisplayName}
          </p>
        </div>

        {/* Primary Nav List */}
        <nav className="flex-1 overflow-y-auto px-2.5 py-3 space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = route === item.id;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => setRoute(item.id)}
                className={`w-full flex items-center gap-2.5 rounded-xl px-3 py-2 text-xs font-semibold transition whitespace-nowrap ${
                  active
                    ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-slate-950'
                    : 'text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800'
                }`}
              >
                <Icon className="w-4 h-4 shrink-0" />
                <span className="truncate">{item.label}</span>
              </button>
            );
          })}
        </nav>

        {/* Bottom Utility Actions */}
        <div className="p-3 border-t border-[#D5E0EC] dark:border-slate-800 space-y-2">
          <div className="flex items-center justify-between gap-2">
            <button
              type="button"
              onClick={toggleLanguage}
              className="flex-1 rounded-lg border border-[#D5E0EC] dark:border-slate-700 py-1.5 text-xs font-semibold hover:bg-slate-100 dark:hover:bg-slate-800"
            >
              {strings.languageToggleLabel}
            </button>
            <button
              type="button"
              onClick={toggleTheme}
              className="p-2 rounded-lg border border-[#D5E0EC] dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800"
              aria-label="Toggle Theme"
            >
              {darkTheme ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
            </button>
            <button
              type="button"
              onClick={signOutAll}
              className="p-2 rounded-lg border border-[#D5E0EC] dark:border-slate-700 text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30"
              aria-label={strings.signOut}
              title={strings.signOut}
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
          <PWAInstallButton variant="compact" onOpenDownloadCenter={() => setRoute('downloads')} />
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 md:ps-64 flex flex-col min-w-0">
        {/* Shared Compact Top Header (<=56px on Mobile, SaaS Top Bar on Desktop) */}
        <header className="sticky top-0 z-20 h-14 bg-white/95 dark:bg-slate-900/95 backdrop-blur border-b border-[#D5E0EC] dark:border-slate-800 px-4 flex items-center justify-between gap-3">
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="md:hidden">
              <SkillBridgeLogo size="sm" />
            </div>
            <div className="hidden md:block min-w-0">
              <h1 className="font-display text-sm font-bold text-[#0B1F38] dark:text-white truncate">{activeTitle}</h1>
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            {/* Real-time Central Sync Indicator */}
            <span
              className={`hidden sm:inline-flex items-center gap-1.5 text-[11px] font-semibold px-2.5 py-1 rounded-lg border whitespace-nowrap shrink-0 ${
                !isOnline
                  ? 'border-amber-300 text-amber-700 bg-amber-50 dark:bg-amber-950/40 dark:text-amber-300'
                  : sseConnected || !session
                    ? 'border-emerald-300/70 text-emerald-700 dark:text-emerald-300 bg-emerald-50/70 dark:bg-emerald-950/30'
                    : 'border-sky-300 text-sky-700 dark:text-sky-300 bg-sky-50 dark:bg-sky-950/30'
              }`}
            >
              {!isOnline
                ? 'Offline'
                : session
                  ? sseConnected
                    ? strings.liveSyncConnected
                    : strings.liveSyncReconnecting
                  : isArabic
                    ? 'كتالوج الويب'
                    : 'Web Catalog'}
            </span>

            {role === 'STUDENT' && (
              <button
                type="button"
                onClick={() => setRoute('search')}
                className="p-2 rounded-lg border border-[#D5E0EC] dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300"
                aria-label="Search"
              >
                <Search className="w-4 h-4" />
              </button>
            )}

            <button
              type="button"
              onClick={() => setRoute('notifications')}
              className="relative p-2 rounded-lg border border-[#D5E0EC] dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300"
              aria-label={strings.notificationsTitle}
            >
              <Bell className="w-4 h-4" />
              {unreadTotal > 0 && (
                <span className="absolute -top-1 -end-1 min-w-[17px] h-[17px] rounded-full bg-[#00B4DB] text-white text-[10px] font-bold flex items-center justify-center px-1 tabular-nums">
                  {unreadTotal}
                </span>
              )}
            </button>

            {role === 'STUDENT' && (
              <button
                type="button"
                onClick={() => setAiDrawerOpen(true)}
                className="inline-flex items-center gap-1.5 rounded-xl bg-[#0B1F38] dark:bg-[#00B4DB] px-3 py-1.5 text-xs font-semibold text-white dark:text-slate-950 hover:opacity-90 transition whitespace-nowrap shrink-0"
              >
                <Bot className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">{isArabic ? 'المساعد الذكي' : 'AI Agent'}</span>
              </button>
            )}

            <button
              type="button"
              onClick={() => setRoute('settings')}
              className="md:hidden p-2 rounded-lg border border-[#D5E0EC] dark:border-slate-700 text-slate-600 dark:text-slate-300"
              aria-label={strings.accountSettingsTitle}
            >
              <Settings className="w-4 h-4" />
            </button>
          </div>
        </header>

        {/* Guest Preview Banner */}
        {isGuestPreview && (
          <div className="bg-[#E0F9FD] dark:bg-slate-800 border-b border-[#00B4DB]/30 px-4 py-2 flex flex-wrap items-center justify-between gap-2 text-xs">
            <span className="font-medium text-[#0B1F38] dark:text-[#7EE8F5]">{strings.guestBannerTitle}</span>
            <button
              type="button"
              onClick={() => setRoute('welcome')}
              className="rounded-lg bg-[#0B1F38] text-white px-3 py-1 font-semibold hover:bg-[#132D4F] whitespace-nowrap shrink-0"
            >
              {isArabic ? 'تسجيل الدخول الآن' : 'Sign In'}
            </button>
          </div>
        )}

        {/* Scrollable Workspace Content */}
        <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 py-5 pb-24 md:pb-8">
          {children}
        </main>
      </div>

      {/* Mobile Bottom Thumb Bar (< md only) */}
      <nav className="md:hidden fixed bottom-0 inset-x-0 z-30 h-16 bg-white/95 dark:bg-slate-900/95 backdrop-blur border-t border-[#D5E0EC] dark:border-slate-800 flex items-center justify-around px-1">
        {mobileBottomItems.map((item) => {
          const Icon = item.icon;
          const active = route === item.id;
          return (
            <button
              key={item.id}
              type="button"
              onClick={() => setRoute(item.id)}
              className={`flex flex-col items-center justify-center min-w-[56px] min-h-[44px] rounded-xl px-2 py-1 text-[10px] font-semibold transition ${
                active ? 'text-[#008CAE] dark:text-[#7EE8F5]' : 'text-slate-500 dark:text-slate-400'
              }`}
            >
              <Icon className="w-4 h-4 mb-0.5" />
              <span className="truncate max-w-[68px]">{item.label}</span>
            </button>
          );
        })}
      </nav>

      <ToastBanner />
    </div>
  );
};
