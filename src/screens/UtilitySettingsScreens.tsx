import React, { useMemo, useState } from 'react';
import {
  Bell,
  CheckCheck,
  Database,
  Globe,
  Laptop,
  LogOut,
  Search,
  Server,
  Shield,
  Smartphone,
  Trash2,
} from 'lucide-react';
import { SectionHeader, StateWrapper, StatusLabel } from '../components/SkillBridgeUi';
import {
  CHALLENGES_CATALOG,
  useSkillBridge,
} from '../context/SkillBridgeContext';
import { lText } from '../i18n/localization';
import type { PreviewStateMode } from '../types/skillbridge';

export const GlobalSearchScreen: React.FC = () => {
  const {
    strings,
    isArabic,
    publicBriefs,
    communityPosts,
    teams,
    setSelectedChallengeId,
    setSelectedPostId,
    setSelectedTeamId,
    setRoute,
  } = useSkillBridge();
  const [query, setQuery] = useState('');
  const q = query.trim().toLowerCase();
  const lang = isArabic ? 'AR' : 'EN';

  const matchedChallenges = useMemo(
    () =>
      CHALLENGES_CATALOG.filter(
        (c) => !q || `${c.title.en} ${c.title.ar} ${c.skills.join(' ')}`.toLowerCase().includes(q),
      ),
    [q],
  );
  const matchedBriefs = useMemo(
    () => publicBriefs.filter((b) => !q || `${b.title} ${b.companyName} ${b.skillsCsv}`.toLowerCase().includes(q)),
    [publicBriefs, q],
  );
  const matchedPosts = useMemo(
    () => communityPosts.filter((p) => !q || `${p.title} ${p.tags.join(' ')}`.toLowerCase().includes(q)),
    [communityPosts, q],
  );
  const matchedTeams = useMemo(
    () => teams.filter((t) => !q || `${t.name} ${t.domain}`.toLowerCase().includes(q)),
    [teams, q],
  );

  return (
    <StateWrapper>
      <div className="space-y-5">
        <SectionHeader title={isArabic ? 'البحث الشامل' : 'Global Ecosystem Search'} />
        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute start-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            autoFocus
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={strings.searchPlaceholder}
            className="w-full rounded-2xl border border-[#D5E0EC] dark:border-slate-700 bg-white dark:bg-slate-900 ps-10 pe-4 py-3 text-sm focus:outline-none focus:border-[#00B4DB]"
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4 space-y-2">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500">
              {strings.filterChallenges} & {strings.filterProjects} ({matchedChallenges.length + matchedBriefs.length})
            </h3>
            {matchedBriefs.map((b) => (
              <button
                key={b.id}
                type="button"
                onClick={() => setRoute('briefs')}
                className="block w-full text-start rounded-lg px-2.5 py-2 hover:bg-slate-50 dark:hover:bg-slate-800 text-xs"
              >
                <span className="font-bold text-[#0B1F38] dark:text-white">{b.title}</span>
                <span className="block text-[11px] text-[#008CAE]">{b.companyName}</span>
              </button>
            ))}
            {matchedChallenges.map((c) => (
              <button
                key={c.id}
                type="button"
                onClick={() => {
                  setSelectedChallengeId(c.id);
                  setRoute('challengeDetail');
                }}
                className="block w-full text-start rounded-lg px-2.5 py-2 hover:bg-slate-50 dark:hover:bg-slate-800 text-xs"
              >
                <span className="font-bold text-[#0B1F38] dark:text-white">{lText(c.title, lang)}</span>
                <span className="block text-[11px] text-slate-500">{c.companyName}</span>
              </button>
            ))}
          </div>

          <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4 space-y-2">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500">
              {strings.communityTitle} & {strings.navTeams} ({matchedPosts.length + matchedTeams.length})
            </h3>
            {matchedPosts.map((p) => (
              <button
                key={p.id}
                type="button"
                onClick={() => {
                  setSelectedPostId(p.id);
                  setRoute('postDetail');
                }}
                className="block w-full text-start rounded-lg px-2.5 py-2 hover:bg-slate-50 dark:hover:bg-slate-800 text-xs"
              >
                <span className="font-bold text-[#0B1F38] dark:text-white">{p.title}</span>
                <span className="block text-[11px] text-slate-500">{p.authorName}</span>
              </button>
            ))}
            {matchedTeams.map((t) => (
              <button
                key={t.id}
                type="button"
                onClick={() => {
                  setSelectedTeamId(t.id);
                  setRoute('teamWorkspace');
                }}
                className="block w-full text-start rounded-lg px-2.5 py-2 hover:bg-slate-50 dark:hover:bg-slate-800 text-xs"
              >
                <span className="font-bold text-[#0B1F38] dark:text-white">{t.name}</span>
                <span className="block text-[11px] text-slate-500">{t.challengeTitle}</span>
              </button>
            ))}
          </div>
        </div>
      </div>
    </StateWrapper>
  );
};

export const NotificationsScreen: React.FC = () => {
  const {
    strings,
    isArabic,
    snapshot,
    localNotifications,
    markAllNotificationsRead,
    markNotificationRead,
    session,
  } = useSkillBridge();
  const lang = isArabic ? 'AR' : 'EN';
  const centralNotifs = snapshot?.notifications ?? [];

  return (
    <StateWrapper>
      <div className="space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SectionHeader title={strings.notificationsTitle} />
          {session && centralNotifs.some((n) => !n.isRead) && (
            <button
              type="button"
              onClick={markAllNotificationsRead}
              className="inline-flex items-center gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-slate-700 px-3 py-1.5 text-xs font-bold hover:bg-slate-50 dark:hover:bg-slate-800"
            >
              <CheckCheck className="w-4 h-4 text-[#00B4DB]" />
              {isArabic ? 'تحديد الكل كمقروء' : 'Mark All Read'}
            </button>
          )}
        </div>

        <div className="space-y-2.5">
          {centralNotifs.map((n) => (
            <div
              key={n.id}
              className={`rounded-2xl border p-4 flex items-start justify-between gap-3 bg-white dark:bg-slate-900 ${
                !n.isRead ? 'border-[#00B4DB]' : 'border-[#D5E0EC] dark:border-slate-800'
              }`}
            >
              <div className="flex items-start gap-3">
                <Bell className={`w-4 h-4 mt-0.5 shrink-0 ${!n.isRead ? 'text-[#00B4DB]' : 'text-slate-400'}`} />
                <div>
                  <p className="text-xs font-bold text-[#0B1F38] dark:text-white">
                    {isArabic ? n.titleAr : n.titleEn}
                  </p>
                  <p className="text-xs text-slate-600 dark:text-slate-300 mt-0.5">
                    {isArabic ? n.bodyAr : n.bodyEn}
                  </p>
                </div>
              </div>
              {!n.isRead && (
                <button
                  type="button"
                  onClick={() => markNotificationRead(n.id)}
                  className="text-[11px] font-semibold text-[#008CAE] shrink-0"
                >
                  {isArabic ? 'قُرئ' : 'Read'}
                </button>
              )}
            </div>
          ))}

          {!session &&
            localNotifications.map((n) => (
              <div
                key={n.id}
                className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4 flex items-start justify-between gap-3"
              >
                <div>
                  <p className="text-xs font-bold text-[#0B1F38] dark:text-white">{lText(n.title, lang)}</p>
                  <p className="text-xs text-slate-600 dark:text-slate-300 mt-0.5">{lText(n.body, lang)}</p>
                </div>
                <span className="text-[11px] text-slate-400">{n.timeAgo}</span>
              </div>
            ))}
        </div>
      </div>
    </StateWrapper>
  );
};

export const ArchitectureBlueprintScreen: React.FC = () => {
  const { strings, isArabic, setRoute } = useSkillBridge();

  return (
    <StateWrapper>
      <div className="space-y-5">
        <SectionHeader
          title={strings.blueprintTitle}
          subtitle={
            isArabic
              ? 'منصة واحدة · ثلاث بوابات وصول (ويب، موبايل، سطح مكتب) · خلفية وقاعدة بيانات مركزية مشتركة'
              : 'One Product · Three Access Clients (Web, Mobile, Desktop) · One Shared Backend & Central Database'
          }
        />

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[
            {
              icon: Globe,
              title: isArabic ? '١. تطبيق الويب (Browser)' : '1. Web Client',
              desc: isArabic
                ? 'وصول فوري من المتصفح بتصميم SkillBridge المتجاوب.'
                : 'Zero-install responsive browser workspace.',
            },
            {
              icon: Smartphone,
              title: isArabic ? '٢. تطبيق الموبايل (PWA)' : '2. Mobile Client',
              desc: isArabic
                ? 'تنقل سفلي مريح للإبهام وتجربة تطبيق مستقل على الهاتف.'
                : 'Thumb-zone mobile navigation + installable PWA.',
            },
            {
              icon: Laptop,
              title: isArabic ? '٣. تطبيق سطح المكتب (PWA)' : '3. Desktop Client',
              desc: isArabic
                ? 'شريط جانبي ثابت ولوحات متعددة الأعمدة على Windows وmacOS وLinux.'
                : 'Fixed sidebar & multi-column desktop layouts.',
            },
          ].map((c) => {
            const Icon = c.icon;
            return (
              <div
                key={c.title}
                className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4 space-y-2"
              >
                <Icon className="w-5 h-5 text-[#00B4DB]" />
                <h3 className="font-display text-sm font-bold">{c.title}</h3>
                <p className="text-xs text-slate-600 dark:text-slate-300">{c.desc}</p>
              </div>
            );
          })}
        </div>

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
          <div className="flex items-center gap-2 text-sm font-bold text-[#0B1F38] dark:text-white">
            <Server className="w-4 h-4 text-[#00B4DB]" />
            <span>{isArabic ? 'واجهة API الموحدة + المزامنة اللحظية SSE' : 'Unified Express API + Real-Time SSE Stream'}</span>
          </div>
          <div className="flex items-center gap-2 text-sm font-bold text-[#0B1F38] dark:text-white">
            <Database className="w-4 h-4 text-[#00B4DB]" />
            <span>
              {isArabic
                ? 'قاعدة البيانات المركزية + Firestore السحابية لأهداف المتعلمين وتقارير الذكاء الاصطناعي'
                : 'Central Authoritative Store + Cloud Firestore for Learner Goals, Submissions, Badges & Community'}
            </span>
          </div>
          <button
            type="button"
            onClick={() => setRoute('downloads')}
            className="rounded-xl bg-[#00B4DB] px-4 py-2 text-xs font-bold text-white"
          >
            {strings.downloadCenterTitle}
          </button>
        </div>
      </div>
    </StateWrapper>
  );
};

export const AccountAndPrivacySettingsScreen: React.FC = () => {
  const {
    strings,
    isArabic,
    language,
    darkTheme,
    toggleLanguage,
    toggleTheme,
    session,
    snapshot,
    currentDeviceSessionId,
    revokeDeviceSession,
    previewStateMode,
    setPreviewStateMode,
    deleteAccountAndReset,
    signOutAll,
    actionBusy,
    setRoute,
  } = useSkillBridge();

  const [confirmDeleteStep, setConfirmDeleteStep] = useState(false);
  const deviceSessions = snapshot?.deviceSessions ?? [];

  return (
    <StateWrapper allowBypassPreview>
      <div className="space-y-6">
        <SectionHeader
          title={strings.accountSettingsTitle}
          subtitle={isArabic ? 'إدارة الجلسات المتزامنة، المظهر، الخصوصية، ومعاينة حالات الواجهة' : 'Manage connected devices, appearance, privacy & UI states'}
        />

        {/* Appearance & Download Center Shortcut */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="text-sm font-bold text-[#0B1F38] dark:text-white">
              {session ? session.displayName : 'SkillBridge Guest'}
            </p>
            <p className="text-xs text-slate-500">
              {session ? `${session.email} · ${session.role}` : 'Preview Mode'}
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={toggleLanguage}
              className="rounded-xl border border-[#D5E0EC] dark:border-slate-700 px-3 py-1.5 text-xs font-semibold"
            >
              <Globe className="w-3.5 h-3.5 inline me-1" />
              {language === 'AR' ? 'Switch to English' : 'التبديل إلى العربية'}
            </button>
            <button
              type="button"
              onClick={toggleTheme}
              className="rounded-xl border border-[#D5E0EC] dark:border-slate-700 px-3 py-1.5 text-xs font-semibold"
            >
              {darkTheme ? (isArabic ? 'الوضع النهاري' : 'Light Mode') : isArabic ? 'الوضع الداكن' : 'Dark Mode'}
            </button>
            <button
              type="button"
              onClick={() => setRoute('downloads')}
              className="rounded-xl bg-[#00B4DB] px-3 py-1.5 text-xs font-bold text-white"
            >
              {strings.downloadCenterTitle}
            </button>
          </div>
        </div>

        {/* Cross-Device Synchronized Sessions */}
        {session && (
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
            <SectionHeader
              title={isArabic ? 'الأجهزة والجلسات المتصلة لحظيًا' : 'Connected Cross-Platform Device Sessions'}
              subtitle={isArabic ? 'يمكنك إبطال أي جلسة عن بُعد عبر جميع المنصات' : 'Monitor & remotely revoke active Web, Mobile, and Desktop sessions'}
            />
            {deviceSessions.length === 0 ? (
              <p className="text-xs text-slate-500">{isArabic ? 'الجلسة الحالية نشطة.' : 'Current session active.'}</p>
            ) : (
              <div className="divide-y divide-slate-100 dark:divide-slate-800">
                {deviceSessions.map((ds) => (
                  <div key={ds.id} className="py-2.5 flex flex-wrap items-center justify-between gap-2 text-xs">
                    <div>
                      <p className="font-bold text-[#0B1F38] dark:text-white">
                        {ds.deviceLabel}
                        {ds.id === currentDeviceSessionId && (
                          <span className="ms-2 text-[11px] font-semibold text-[#008CAE]">
                            ({isArabic ? 'هذا الجهاز' : 'This Device'})
                          </span>
                        )}
                      </p>
                      <p className="text-[11px] text-slate-500">
                        {ds.windowWidthDp}px · {new Date(ds.lastActiveEpochMs).toLocaleTimeString()}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <StatusLabel status={ds.revoked ? 'REJECTED' : 'ACTIVE'} />
                      {!ds.revoked && ds.id !== currentDeviceSessionId && (
                        <button
                          type="button"
                          disabled={actionBusy}
                          onClick={() => revokeDeviceSession(ds.id)}
                          className="rounded-lg border border-rose-300 text-rose-600 px-2.5 py-1 text-[11px] font-semibold"
                        >
                          Revoke
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* UI State Inspector */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
          <SectionHeader
            title={strings.statePreviewTitle}
            subtitle={isArabic ? 'اختبر استجابة الواجهة لكل الحالات (طبيعي، تحميل، فراغ، خطأ، مقفل)' : 'Inspect Normal, Loading, Empty, Error & Locked UI states'}
          />
          <div className="flex flex-wrap gap-1.5">
            {(['NORMAL', 'LOADING', 'EMPTY', 'ERROR', 'LOCKED'] as PreviewStateMode[]).map((mode) => (
              <button
                key={mode}
                type="button"
                onClick={() => setPreviewStateMode(mode)}
                className={`rounded-xl px-3 py-1.5 text-xs font-bold transition ${
                  previewStateMode === mode
                    ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-slate-950'
                    : 'border border-[#D5E0EC] dark:border-slate-700'
                }`}
              >
                {mode}
              </button>
            ))}
          </div>
        </div>

        {/* Account Actions */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 flex flex-wrap items-center justify-between gap-3">
          <button
            type="button"
            onClick={signOutAll}
            className="inline-flex items-center gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-slate-700 px-4 py-2 text-xs font-bold hover:bg-slate-50 dark:hover:bg-slate-800"
          >
            <LogOut className="w-4 h-4" />
            {strings.signOut}
          </button>

          {!confirmDeleteStep ? (
            <button
              type="button"
              onClick={() => setConfirmDeleteStep(true)}
              className="inline-flex items-center gap-1.5 rounded-xl border border-rose-300 text-rose-600 px-4 py-2 text-xs font-bold hover:bg-rose-50 dark:hover:bg-rose-950/30"
            >
              <Trash2 className="w-4 h-4" />
              {strings.deleteAccountAction}
            </button>
          ) : (
            <div className="flex flex-wrap items-center gap-2">
              <span className="text-xs text-rose-600 font-semibold flex items-center gap-1">
                <Shield className="w-4 h-4" />
                {isArabic ? 'تأكيد الحذف النهائي؟' : 'Confirm permanent deletion?'}
              </span>
              <button
                type="button"
                onClick={() => setConfirmDeleteStep(false)}
                className="rounded-lg border border-slate-300 px-3 py-1 text-xs font-semibold"
              >
                {strings.cancelLabel}
              </button>
              <button
                type="button"
                onClick={deleteAccountAndReset}
                className="rounded-lg bg-rose-600 px-3 py-1 text-xs font-bold text-white"
              >
                {strings.confirmLabel}
              </button>
            </div>
          )}
        </div>
      </div>
    </StateWrapper>
  );
};
