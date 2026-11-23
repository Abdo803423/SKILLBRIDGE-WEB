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
import { MotionReveal, SpatialCard } from '../components/InteractiveMotion';
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
          <Search className="w-4 h-4 text-[#4C637C] dark:text-[#93ADC8] absolute start-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            autoFocus
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={strings.searchPlaceholder}
            className="w-full rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] ps-10 pe-4 py-3 text-sm text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 space-y-2">
            <h3 className="text-xs font-bold uppercase tracking-wider text-[#4C637C] dark:text-[#93ADC8]">
              {strings.filterChallenges} & {strings.filterProjects} ({matchedChallenges.length + matchedBriefs.length})
            </h3>
            {matchedBriefs.map((b) => (
              <button
                key={b.id}
                type="button"
                onClick={() => setRoute('briefs')}
                className="block w-full text-start rounded-lg px-2.5 py-2 hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] text-xs transition"
              >
                <span className="font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{b.title}</span>
                <span className="block text-[11px] text-[#008CAE] dark:text-[#7EE8F5]">{b.companyName}</span>
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
                className="block w-full text-start rounded-lg px-2.5 py-2 hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] text-xs transition"
              >
                <span className="font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{lText(c.title, lang)}</span>
                <span className="block text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{c.companyName}</span>
              </button>
            ))}
          </div>

          <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 space-y-2">
            <h3 className="text-xs font-bold uppercase tracking-wider text-[#4C637C] dark:text-[#93ADC8]">
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
                className="block w-full text-start rounded-lg px-2.5 py-2 hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] text-xs transition"
              >
                <span className="font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{p.title}</span>
                <span className="block text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{p.authorName}</span>
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
                className="block w-full text-start rounded-lg px-2.5 py-2 hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] text-xs transition"
              >
                <span className="font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{t.name}</span>
                <span className="block text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{t.challengeTitle}</span>
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
              className="inline-flex items-center gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-3 py-1.5 text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] hover:border-[#00B4DB] active:scale-95 transition"
            >
              <CheckCheck className="w-4 h-4 text-[#00B4DB] dark:text-[#7EE8F5]" />
              {isArabic ? 'تحديد الكل كمقروء' : 'Mark All Read'}
            </button>
          )}
        </div>

        <div className="space-y-2.5">
          {centralNotifs.map((n) => (
            <div
              key={n.id}
              className={`rounded-2xl border p-4 flex items-start justify-between gap-3 bg-white dark:bg-[#0D1E36] transition ${
                !n.isRead ? 'border-[#00B4DB]' : 'border-[#D5E0EC] dark:border-[#1C395E]'
              }`}
            >
              <div className="flex items-start gap-3">
                <Bell className={`w-4 h-4 mt-0.5 shrink-0 ${!n.isRead ? 'text-[#00B4DB] dark:text-[#7EE8F5]' : 'text-[#4C637C] dark:text-[#93ADC8]'}`} />
                <div>
                  <p className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                    {isArabic ? n.titleAr : n.titleEn}
                  </p>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] mt-0.5">
                    {isArabic ? n.bodyAr : n.bodyEn}
                  </p>
                </div>
              </div>
              {!n.isRead && (
                <button
                  type="button"
                  onClick={() => markNotificationRead(n.id)}
                  className="text-[11px] font-semibold text-[#008CAE] dark:text-[#7EE8F5] shrink-0"
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
                className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 flex items-start justify-between gap-3"
              >
                <div>
                  <p className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{lText(n.title, lang)}</p>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] mt-0.5">{lText(n.body, lang)}</p>
                </div>
                <span className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{n.timeAgo}</span>
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
              ? 'موقع ويب متكامل · تصميم متجاوب لكل الشاشات · خلفية وقاعدة بيانات مركزية حية'
              : 'Standard Browser Website · Responsive Across Every Screen · Shared API & Cloud Firestore'
          }
        />

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[
            {
              icon: Globe,
              title: isArabic ? '١. متصفح سطح المكتب' : '1. Desktop Web Workspace',
              desc: isArabic
                ? 'شريط تنقل جانبي ولوحات مراجعة وتحليلات متعددة الأعمدة في المتصفح.'
                : 'Fixed navigation rail, multi-column review tables, and analytics charts in Chrome, Edge, Safari, or Firefox.',
            },
            {
              icon: Smartphone,
              title: isArabic ? '٢. متصفح الموبايل والأجهزة اللوحية' : '2. Responsive Mobile Web',
              desc: isArabic
                ? 'تنقل سفلي مريح للإبهام بدون الحاجة لتثبيت أي تطبيق.'
                : 'Thumb-friendly bottom bar and responsive layouts on iOS and Android browsers with zero installation.',
            },
            {
              icon: Laptop,
              title: isArabic ? '٣. ٤ بوابات صلاحيات معزولة' : '3. Four Isolated Role Portals',
              desc: isArabic
                ? 'صلاحيات الطالب والشركة ومدير الإشراف والمشرف العام متصلة آنيًا.'
                : 'Student, Company, Director, and Platform Admin portals synchronized over one live backend.',
            },
          ].map((c, idx) => {
            const Icon = c.icon;
            return (
              <MotionReveal key={c.title} delayMs={idx * 60}>
                <SpatialCard
                  tiltStrength={5}
                  className="h-full rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 space-y-2"
                >
                  <Icon className="w-5 h-5 text-[#00B4DB] dark:text-[#7EE8F5]" />
                  <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{c.title}</h3>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{c.desc}</p>
                </SpatialCard>
              </MotionReveal>
            );
          })}
        </div>

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
          <div className="flex items-center gap-2 text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
            <Server className="w-4 h-4 text-[#00B4DB] dark:text-[#7EE8F5]" />
            <span>{isArabic ? 'واجهة API الموحدة + المزامنة اللحظية SSE' : 'Unified Express API + Real-Time SSE Stream'}</span>
          </div>
          <div className="flex items-center gap-2 text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
            <Database className="w-4 h-4 text-[#00B4DB] dark:text-[#7EE8F5]" />
            <span>
              {isArabic
                ? 'قاعدة البيانات المركزية + Firestore السحابية لأهداف المتعلمين وتقارير الذكاء الاصطناعي'
                : 'Central Authoritative Store + Cloud Firestore for Learner Goals, Submissions, Badges & Community'}
            </span>
          </div>
          <button
            type="button"
            onClick={() => setRoute('platform')}
            className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white"
          >
            {strings.platformOverviewTitle}
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
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
              {session ? session.displayName : 'SkillBridge Guest'}
            </p>
            <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">
              {session ? `${session.email} · ${session.role}` : 'Preview Mode'}
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={toggleLanguage}
              className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-3 py-1.5 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB] active:scale-95 transition"
            >
              <Globe className="w-3.5 h-3.5 inline me-1 text-[#00B4DB]" />
              {language === 'AR' ? 'Switch to English' : 'التبديل إلى العربية'}
            </button>
            <button
              type="button"
              onClick={toggleTheme}
              className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-3 py-1.5 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB] active:scale-95 transition"
            >
              {darkTheme ? (isArabic ? 'الوضع النهاري' : 'Light Mode') : isArabic ? 'الوضع الداكن' : 'Dark Mode'}
            </button>
            <button
              type="button"
              onClick={() => setRoute('platform')}
              className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-3 py-1.5 text-xs font-bold text-white"
            >
              {strings.platformOverviewTitle}
            </button>
          </div>
        </div>

        {/* Synchronized Browser Sessions */}
        {session && (
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <SectionHeader
              title={isArabic ? 'جلسات المتصفح المتصلة لحظيًا' : 'Connected Browser Sessions'}
              subtitle={isArabic ? 'يمكنك متابعة أو إبطال أي جلسة متصفح عن بُعد' : 'Monitor & remotely revoke active browser sessions'}
            />
            {deviceSessions.length === 0 ? (
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'الجلسة الحالية نشطة.' : 'Current session active.'}</p>
            ) : (
              <div className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E]">
                {deviceSessions.map((ds) => (
                  <div key={ds.id} className="py-2.5 flex flex-wrap items-center justify-between gap-2 text-xs">
                    <div>
                      <p className="font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                        {ds.deviceLabel}
                        {ds.id === currentDeviceSessionId && (
                          <span className="ms-2 text-[11px] font-semibold text-[#008CAE] dark:text-[#7EE8F5]">
                            ({isArabic ? 'هذا الجهاز' : 'This Device'})
                          </span>
                        )}
                      </p>
                      <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
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
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
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
                className={`rounded-xl px-3 py-1.5 text-xs font-bold transition active:scale-95 ${
                  previewStateMode === mode
                    ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-white'
                    : 'border border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8] hover:border-[#00B4DB]'
                }`}
              >
                {mode}
              </button>
            ))}
          </div>
        </div>

        {/* Account Actions */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 flex flex-wrap items-center justify-between gap-3">
          <button
            type="button"
            onClick={signOutAll}
            className="inline-flex items-center gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-4 py-2 text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] active:scale-95 transition"
          >
            <LogOut className="w-4 h-4" />
            {strings.signOut}
          </button>

          {!confirmDeleteStep ? (
            <button
              type="button"
              onClick={() => setConfirmDeleteStep(true)}
              className="inline-flex items-center gap-1.5 rounded-xl border border-rose-300 text-rose-600 px-4 py-2 text-xs font-bold hover:bg-rose-50 dark:hover:bg-rose-950/30 active:scale-95 transition"
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
                className="rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] px-3 py-1 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC]"
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
