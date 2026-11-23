import React, { useMemo, useState } from 'react';
import {
  ArrowLeft,
  ArrowRight,
  Bookmark,
  Bot,
  CheckCircle2,
  Cpu,
  Lock,
  Play,
  Search,
  ShieldAlert,
  ShieldCheck,
  Terminal,
  Trophy,
} from 'lucide-react';
import { MotionReveal, SpatialCard } from '../components/InteractiveMotion';
import { SectionHeader, StateWrapper, StatusLabel } from '../components/SkillBridgeUi';
import {
  CHALLENGES_CATALOG,
  CONTESTS_CATALOG,
  REVERSE_ENG_LABS,
  SEED_COMPANIES,
  useSkillBridge,
} from '../context/SkillBridgeContext';
import { DOMAINS } from '../data/ecosystemData';
import { lText, localizedDomain } from '../i18n/localization';
import { streamWorkSimulationApi } from '../services/aiClient';
import { containsSecretLeak } from '../services/pointsAndBadgesEngine';

export const DiscoverScreen: React.FC = () => {
  const {
    strings,
    isArabic,
    profile,
    publicBriefs,
    saveChallengeToggle,
    setSelectedChallengeId,
    setRoute,
  } = useSkillBridge();

  const [categoryTab, setCategoryTab] = useState<'ALL' | 'CHALLENGE' | 'CONTEST' | 'REVERSE_ENG'>('ALL');
  const [selectedDomain, setSelectedDomain] = useState<string>('ALL');
  const [query, setQuery] = useState('');

  const safePublicBriefs = Array.isArray(publicBriefs) ? publicBriefs : [];
  const safeSavedIds = Array.isArray(profile?.savedChallengeIds) ? profile.savedChallengeIds : [];

  const filteredChallenges = useMemo(() => {
    const q = query.trim().toLowerCase();
    return CHALLENGES_CATALOG.filter((ch) => {
      if (categoryTab !== 'ALL' && ch.category !== categoryTab) return false;
      if (selectedDomain !== 'ALL' && ch.domain !== selectedDomain) return false;
      if (q) {
        const hay = `${ch.title.en} ${ch.title.ar} ${ch.companyName} ${(ch.skills ?? []).join(' ')}`.toLowerCase();
        if (!hay.includes(q)) return false;
      }
      return true;
    });
  }, [categoryTab, selectedDomain, query]);

  return (
    <StateWrapper>
      <div className="space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SectionHeader
            title={strings.discoverTitle}
            subtitle={
              isArabic
                ? 'استكشف مشاريع الشركات والتحديات والمسابقات ومختبرات الهندسة العكسية'
                : 'Explore verified company challenges, coding contests, and authorized reverse engineering labs'
            }
          />
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => setRoute('contests')}
              className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] px-3 py-2 text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] flex items-center gap-1.5 hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] hover:border-[#00B4DB] active:scale-95 transition whitespace-nowrap"
            >
              <Trophy className="w-4 h-4 text-[#00B4DB] dark:text-[#7EE8F5]" />
              {strings.contestsTitle}
            </button>
            <button
              type="button"
              onClick={() => setRoute('reverseEng')}
              className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] px-3 py-2 text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] flex items-center gap-1.5 hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] hover:border-[#00B4DB] active:scale-95 transition whitespace-nowrap"
            >
              <Terminal className="w-4 h-4 text-[#00B4DB] dark:text-[#7EE8F5]" />
              {strings.reverseEngTitle}
            </button>
          </div>
        </div>

        {/* Search & Filters */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 space-y-3">
          <div className="relative">
            <Search className="w-4 h-4 text-[#4C637C] dark:text-[#93ADC8] absolute start-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder={strings.searchPlaceholder}
              className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] ps-9 pe-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
            />
          </div>

          <div className="flex flex-wrap items-center gap-1.5">
            {(
              [
                { id: 'ALL', label: strings.filterAll },
                { id: 'CHALLENGE', label: strings.filterChallenges },
                { id: 'CONTEST', label: strings.filterContests },
                { id: 'REVERSE_ENG', label: strings.filterReverseEng },
              ] as const
            ).map((tab) => (
              <button
                key={tab.id}
                type="button"
                onClick={() => setCategoryTab(tab.id)}
                className={`rounded-lg px-3 py-1.5 text-xs font-semibold transition active:scale-95 whitespace-nowrap ${
                  categoryTab === tab.id
                    ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-white'
                    : 'bg-[#F0F6FA] dark:bg-[#09172B] text-[#4C637C] dark:text-[#93ADC8] hover:text-[#0B1F38] dark:hover:text-[#F1F7FC]'
                }`}
              >
                {tab.label}
              </button>
            ))}

            <select
              value={selectedDomain}
              onChange={(e) => setSelectedDomain(e.target.value)}
              className="ms-auto rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-2.5 py-1.5 text-xs font-medium text-[#0B1F38] dark:text-[#F1F7FC]"
            >
              <option value="ALL">{isArabic ? 'كل المجالات' : 'All Domains'}</option>
              {DOMAINS.map((d) => (
                <option key={d} value={d}>
                  {localizedDomain(d, isArabic ? 'AR' : 'EN')}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Synchronized Live Company Briefs Highlight */}
        {safePublicBriefs.length > 0 && (
          <SpatialCard
            tiltStrength={2}
            highlight
            className="rounded-2xl border border-[#00B4DB]/45 dark:border-[#1C4E78] bg-[#E0F9FD]/65 dark:bg-[#08243B] p-4"
          >
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p className="text-xs font-bold text-[#008CAE] dark:text-[#7EE8F5]">
                  {isArabic
                    ? `${safePublicBriefs.length} مشاريع شركات حية منشورة حاليًا`
                    : `${safePublicBriefs.length} Live Synchronized Company Briefs Open Now`}
                </p>
                <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">
                  {isArabic
                    ? 'قدّم مباشرة وتابع حالة القبول والمهام في الوقت الفعلي عبر كل الأجهزة'
                    : 'Apply live and sync milestone reviews directly with Company Directors'}
                </p>
              </div>
              <button
                type="button"
                onClick={() => setRoute('briefs')}
                className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] text-white px-4 py-2 text-xs font-bold whitespace-nowrap"
              >
                {isArabic ? 'فتح مشاريع الشركات' : 'Open Company Briefs'}
              </button>
            </div>
          </SpatialCard>
        )}

        {/* Challenges Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredChallenges.map((ch, idx) => {
            const isSaved = safeSavedIds.includes(ch.id);
            return (
              <MotionReveal key={ch.id} delayMs={Math.min(idx, 5) * 55}>
                <SpatialCard
                  tiltStrength={5}
                  className="h-full rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 flex flex-col justify-between gap-4"
                >
                  <div className="space-y-2.5">
                    <div className="flex items-center justify-between gap-2 text-[11px]">
                      <span className="font-bold text-[#008CAE] dark:text-[#7EE8F5] truncate">{ch.companyName}</span>
                      <StatusLabel status={ch.companyTier} />
                    </div>
                    <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                      {lText(ch.title, isArabic ? 'AR' : 'EN')}
                    </h3>
                    <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] line-clamp-3 leading-relaxed">
                      {lText(ch.summary, isArabic ? 'AR' : 'EN')}
                    </p>
                    <div className="text-[11px] text-[#4C637C] dark:text-[#93ADC8] flex flex-wrap gap-2 tabular-nums">
                      <span>{ch.level}</span>
                      <span>·</span>
                      <span className="font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">{ch.rewardLabel}</span>
                      <span>·</span>
                      <span>+{ch.pointsReward} {strings.pointsLabel}</span>
                    </div>
                    <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{(ch.skills ?? []).join(' · ')}</p>
                  </div>

                  <div className="flex items-center gap-2 pt-2 border-t border-[#D5E0EC]/80 dark:border-[#1C395E]">
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedChallengeId(ch.id);
                        setRoute('challengeDetail');
                      }}
                      className="sb-btn-interactive flex-1 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] py-2 text-xs font-bold text-white whitespace-nowrap"
                    >
                      {strings.projectDetails}
                    </button>
                    <button
                      type="button"
                      onClick={() => saveChallengeToggle(ch.id)}
                      className={`p-2 rounded-xl border transition active:scale-95 ${
                        isSaved
                          ? 'border-[#00B4DB] bg-[#E0F9FD] dark:bg-[#08243B] text-[#008CAE] dark:text-[#7EE8F5]'
                          : 'border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8] hover:border-[#00B4DB]'
                      }`}
                      aria-label={strings.saveForLater}
                    >
                      <Bookmark className="w-4 h-4" />
                    </button>
                  </div>
                </SpatialCard>
              </MotionReveal>
            );
          })}
        </div>

        {/* Verified Partner Companies Directory */}
        <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
          <SectionHeader title={isArabic ? 'الشركات الموثّقة الشريكة' : 'Verified Partner Companies'} />
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
            {SEED_COMPANIES.map((c) => (
              <div key={c.id} className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-3 space-y-1 hover:border-[#00B4DB] transition">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] truncate">{c.name}</span>
                  <StatusLabel status={c.tier} />
                </div>
                <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{c.city} · {c.domain}</p>
                <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8] tabular-nums">
                  ★ {c.avgMentorRating} · {c.completionRate}% completion
                </p>
              </div>
            ))}
          </div>
        </section>
      </div>
    </StateWrapper>
  );
};

export const ChallengeDetailScreen: React.FC = () => {
  const {
    selectedChallengeId,
    setRoute,
    isArabic,
    strings,
    submitCatalogChallenge,
    showToast,
    setAiDrawerOpen,
    setAiAgentMode,
  } = useSkillBridge();

  const challenge = CHALLENGES_CATALOG.find((c) => c.id === selectedChallengeId) ?? CHALLENGES_CATALOG[0];
  const lang = isArabic ? 'AR' : 'EN';

  const [simDecision, setSimDecision] = useState('');
  const [simOutput, setSimOutput] = useState('');
  const [simBusy, setSimBusy] = useState(false);

  const [repoUrl, setRepoUrl] = useState('');
  const [report, setReport] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleRunSim = async () => {
    if (!simDecision.trim() || simBusy) return;
    setSimBusy(true);
    setSimOutput('');
    try {
      await streamWorkSimulationApi(
        {
          challengeTitle: lText(challenge.title, lang),
          userDecision: simDecision,
          isArabic,
        },
        (delta) => setSimOutput((prev) => prev + delta),
      );
    } catch (err) {
      setSimOutput(`⚠️ ${err instanceof Error ? err.message : strings.aiAgentUnavailable}`);
    } finally {
      setSimBusy(false);
    }
  };

  const handleSubmitSolution = async (e: React.FormEvent) => {
    e.preventDefault();
    if (containsSecretLeak(`${repoUrl}\n${report}`)) {
      showToast(strings.secretLeakWarning);
      return;
    }
    setSubmitting(true);
    try {
      await submitCatalogChallenge({
        challengeId: challenge.id,
        challengeTitle: lText(challenge.title, lang),
        companyName: challenge.companyName,
        category: challenge.category,
        roleInTeam: 'Solo Contributor',
        skillsApplied: challenge.skills,
        repoOrDemoUrl: repoUrl,
        understandingReport: report,
      });
      setRepoUrl('');
      setReport('');
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Submission blocked');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <StateWrapper>
      <div className="space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <button
            type="button"
            onClick={() => setRoute('discover')}
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline"
          >
            {isArabic ? <ArrowRight className="w-4 h-4" /> : <ArrowLeft className="w-4 h-4" />}
            {strings.backLabel}
          </button>
          <button
            type="button"
            onClick={() => {
              setAiAgentMode('CHALLENGE_MENTOR');
              setAiDrawerOpen(true);
            }}
            className="inline-flex items-center gap-1.5 rounded-xl border border-[#00B4DB]/45 dark:border-[#1C4E78] bg-[#E0F9FD]/60 dark:bg-[#08243B] px-3 py-1.5 text-xs font-bold text-[#008CAE] dark:text-[#7EE8F5] hover:border-[#00B4DB] active:scale-95 transition whitespace-nowrap"
          >
            <Bot className="w-3.5 h-3.5" />
            {isArabic ? 'اسأل مرشد التحدي' : 'Ask Challenge Mentor'}
          </button>
        </div>

        {/* Overview Card */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 sm:p-6 space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <span className="text-xs font-bold text-[#008CAE] dark:text-[#7EE8F5]">
              {challenge.companyName} · {challenge.domain}
            </span>
            <StatusLabel status={challenge.companyTier} />
          </div>
          <h1 className="font-display text-2xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] [text-wrap:balance]">
            {lText(challenge.title, lang)}
          </h1>
          <p className="text-sm text-[#4C637C] dark:text-[#93ADC8] leading-relaxed">
            {lText(challenge.summary, lang)}
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
            <div className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-4 space-y-2">
              <h3 className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] flex items-center gap-1.5">
                <ShieldCheck className="w-4 h-4 text-[#00B4DB] dark:text-[#7EE8F5]" />
                {strings.rubricBeforeStart}
              </h3>
              <ul className="space-y-1.5 text-xs text-[#4C637C] dark:text-[#93ADC8] list-disc list-inside">
                {(challenge.rubric ?? []).map((r, i) => (
                  <li key={i}>{lText(r, lang)}</li>
                ))}
              </ul>
            </div>

            <div className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-4 space-y-2">
              <h3 className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{strings.ipRightsTitle}</h3>
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{lText(challenge.ipPolicy, lang)}</p>
              <h3 className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] pt-1">{strings.aiPolicyTitle}</h3>
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{lText(challenge.aiPolicy, lang)}</p>
            </div>
          </div>
        </div>

        {/* Interactive AI Work Simulation */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
          <SectionHeader
            title={strings.launchWorkSim}
            subtitle={lText(challenge.simulationPrompt, lang)}
          />
          <div className="flex flex-col sm:flex-row gap-2">
            <input
              type="text"
              value={simDecision}
              onChange={(e) => setSimDecision(e.target.value)}
              placeholder={
                isArabic
                  ? 'اكتب قرارك الهندسي للتحدي (مثال: استخدام TimescaleDB مع فهرس مركب)...'
                  : 'Propose your engineering decision (e.g., partition telemetry by hour & use idempotency key)...'
              }
              className="flex-1 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
            />
            <button
              type="button"
              disabled={simBusy || !simDecision.trim()}
              onClick={handleRunSim}
              className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] text-white px-4 py-2 text-xs font-bold flex items-center justify-center gap-1.5 disabled:opacity-50 whitespace-nowrap"
            >
              <Play className="w-3.5 h-3.5" />
              {simBusy ? '...' : isArabic ? 'اختبر القرار' : 'Run Simulation'}
            </button>
          </div>
          {simOutput && (
            <div className="rounded-xl border border-[#00B4DB]/40 dark:border-[#1C4E78] bg-[#F0F6FA] dark:bg-[#09172B] p-3.5 text-xs leading-relaxed whitespace-pre-wrap text-[#0B1F38] dark:text-[#F1F7FC]">
              {simOutput}
            </div>
          )}
        </div>

        {/* Submit Catalog Solution */}
        <form
          onSubmit={handleSubmitSolution}
          className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3"
        >
          <SectionHeader
            title={strings.submitSolution}
            subtitle={isArabic ? 'يتم فحص التسليم تلقائيًا لمنع تسريب أي مفاتيح أو توكنات سرية.' : 'Protected by automated secret-leak scanner before submission.'}
          />
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold mb-1">Repository / Sandbox URL</label>
              <input
                type="url"
                required
                value={repoUrl}
                onChange={(e) => setRepoUrl(e.target.value)}
                placeholder="https://github.com/your-handle/solution"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1">{strings.understandingReportLabel}</label>
              <input
                type="text"
                required
                value={report}
                onChange={(e) => setReport(e.target.value)}
                placeholder="Summarize design trade-offs & test assertions..."
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
            </div>
          </div>
          <div className="flex justify-end">
            <button
              type="submit"
              disabled={submitting}
              className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-5 py-2 text-xs font-bold text-white"
            >
              {submitting ? '...' : strings.submitSolution}
            </button>
          </div>
        </form>
      </div>
    </StateWrapper>
  );
};

export const ContestsScreen: React.FC = () => {
  const { strings, isArabic, setRoute, showToast } = useSkillBridge();
  const [statusFilter, setStatusFilter] = useState<'LIVE' | 'UPCOMING' | 'ENDED'>('LIVE');
  const [selectedContestId, setSelectedContestId] = useState(CONTESTS_CATALOG[0].id);
  const [subTab, setSubTab] = useState<'PROBLEMS' | 'STANDINGS' | 'EDITORIAL'>('PROBLEMS');

  const lang = isArabic ? 'AR' : 'EN';
  const filtered = CONTESTS_CATALOG.filter((c) => c.status === statusFilter);
  const active = filtered.find((c) => c.id === selectedContestId) ?? filtered[0] ?? CONTESTS_CATALOG[0];

  return (
    <StateWrapper>
      <div className="space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <SectionHeader title={strings.contestsTitle} subtitle={strings.prizePoolDisclaimer} />
          <button type="button" onClick={() => setRoute('discover')} className="text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline">
            {strings.backLabel}
          </button>
        </div>

        <div className="flex gap-1.5">
          {(
            [
              { id: 'LIVE', label: strings.liveNow },
              { id: 'UPCOMING', label: strings.upcoming },
              { id: 'ENDED', label: strings.ended },
            ] as const
          ).map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setStatusFilter(tab.id)}
              className={`rounded-xl px-3.5 py-1.5 text-xs font-bold transition active:scale-95 whitespace-nowrap ${
                statusFilter === tab.id
                  ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-white'
                  : 'bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {filtered.map((contest) => (
            <button
              key={contest.id}
              type="button"
              onClick={() => setSelectedContestId(contest.id)}
              className={`text-start rounded-2xl border p-4 space-y-1.5 transition active:scale-[0.99] ${
                active.id === contest.id
                  ? 'border-[#00B4DB] bg-white dark:bg-[#0D1E36] ring-1 ring-[#00B4DB]/30 shadow-[0_12px_28px_-18px_rgba(0,180,219,0.5)]'
                  : 'border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] hover:border-[#00B4DB]/60'
              }`}
            >
              <div className="flex items-center justify-between gap-2">
                <span className="text-[11px] font-bold text-[#008CAE] dark:text-[#7EE8F5]">{contest.organizer}</span>
                <StatusLabel status={contest.status} />
              </div>
              <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {lText(contest.title, lang)}
              </h3>
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] tabular-nums">
                {contest.prizePool} · {contest.participantsCount} {isArabic ? 'متسابق' : 'entrants'}
              </p>
            </button>
          ))}
        </div>

        {/* Contest Arena */}
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="font-display text-lg font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {lText(active.title, lang)}
              </h2>
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{lText(active.startsOrEndsIn, lang)}</p>
            </div>
            <div className="flex gap-1.5">
              {(
                [
                  { id: 'PROBLEMS', label: strings.problemsTab },
                  { id: 'STANDINGS', label: strings.standingsTab },
                  { id: 'EDITORIAL', label: strings.editorialTab },
                ] as const
              ).map((t) => (
                <button
                  key={t.id}
                  type="button"
                  onClick={() => setSubTab(t.id)}
                  className={`rounded-lg px-3 py-1.5 text-xs font-bold active:scale-95 transition whitespace-nowrap ${
                    subTab === t.id
                      ? 'bg-[#00B4DB] text-white'
                      : 'border border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]'
                  }`}
                >
                  {t.label}
                </button>
              ))}
            </div>
          </div>

          {subTab === 'PROBLEMS' && (
            <div className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E]">
              {active.problems.map((p) => (
                <div key={p.code} className="py-3 flex flex-wrap items-center justify-between gap-2">
                  <div>
                    <span className="font-mono text-xs font-bold text-[#008CAE] dark:text-[#7EE8F5] me-2">[{p.code}]</span>
                    <span className="text-sm font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">{lText(p.title, lang)}</span>
                    <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8] mt-0.5 tabular-nums">
                      {p.difficulty} · {p.points} pts · {p.solvedPercent}% AC
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => showToast(isArabic ? `تم فتح بيئة المسألة ${p.code}` : `Opened Problem ${p.code} sandbox.`)}
                    className="rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] px-3 py-1.5 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB] active:scale-95 transition"
                  >
                    {isArabic ? 'فتح المسألة' : 'Open Problem'}
                  </button>
                </div>
              ))}
            </div>
          )}

          {subTab === 'STANDINGS' && (
            <div className="text-xs space-y-2">
              {[
                { rank: 1, name: 'Nile Distributed Squad', score: 1550, penalty: '42m' },
                { rank: 2, name: 'Omar Al-Harbi', score: 1200, penalty: '58m' },
                { rank: 3, name: 'Hana Benali', score: 1100, penalty: '64m' },
              ].map((row) => (
                <div
                  key={row.rank}
                  className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-3.5 py-2.5 flex items-center justify-between"
                >
                  <span className="font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">
                    #{row.rank} · {row.name}
                  </span>
                  <span className="tabular-nums text-[#008CAE] dark:text-[#7EE8F5] font-bold">
                    {row.score} pts ({row.penalty})
                  </span>
                </div>
              ))}
            </div>
          )}

          {subTab === 'EDITORIAL' && (
            <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] leading-relaxed">
              {lText(active.editorialSummary, lang)}
            </p>
          )}
        </div>
      </div>
    </StateWrapper>
  );
};

export const ReverseEngHubScreen: React.FC = () => {
  const { strings, isArabic, setRoute, submitCatalogChallenge, showToast, setAiDrawerOpen, setAiAgentMode } =
    useSkillBridge();
  const lang = isArabic ? 'AR' : 'EN';
  const [selectedLabId, setSelectedLabId] = useState(REVERSE_ENG_LABS[0].id);
  const [reportText, setReportText] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const lab = REVERSE_ENG_LABS.find((l) => l.id === selectedLabId) ?? REVERSE_ENG_LABS[0];

  const handleSubmitLab = async (e: React.FormEvent) => {
    e.preventDefault();
    if (containsSecretLeak(reportText)) {
      showToast(strings.secretLeakWarning);
      return;
    }
    setSubmitting(true);
    try {
      await submitCatalogChallenge({
        challengeId: lab.id,
        challengeTitle: lText(lab.title, lang),
        companyName: lab.authorizedOrg,
        category: 'REVERSE_ENG',
        roleInTeam: 'Security Analyst',
        skillsApplied: ['Reverse Engineering', 'Protocol Audit', 'Threat Modeling'],
        repoOrDemoUrl: `https://skillbridge.dev/sandbox/${lab.id}`,
        understandingReport: reportText,
      });
      setReportText('');
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Unable to submit');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <StateWrapper>
      <div className="space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <SectionHeader
            title={strings.reverseEngTitle}
            subtitle={strings.authorizedScopeBadge}
          />
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => {
                setAiAgentMode('REVERSE_ENG_GUIDE');
                setAiDrawerOpen(true);
              }}
              className="rounded-xl border border-[#00B4DB]/45 dark:border-[#1C4E78] bg-[#E0F9FD]/60 dark:bg-[#08243B] px-3 py-1.5 text-xs font-bold text-[#008CAE] dark:text-[#7EE8F5] hover:border-[#00B4DB] active:scale-95 transition whitespace-nowrap"
            >
              <Bot className="w-3.5 h-3.5 inline me-1" />
              {isArabic ? 'مرشد الهندسة العكسية' : 'Reverse Eng Guide'}
            </button>
            <button type="button" onClick={() => setRoute('discover')} className="text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline">
              {strings.backLabel}
            </button>
          </div>
        </div>

        {/* Lab Selector */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {REVERSE_ENG_LABS.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => setSelectedLabId(item.id)}
              className={`text-start rounded-2xl border p-4 space-y-1 transition active:scale-[0.99] ${
                lab.id === item.id
                  ? 'border-[#00B4DB] bg-white dark:bg-[#0D1E36] ring-1 ring-[#00B4DB]/30 shadow-[0_12px_28px_-18px_rgba(0,180,219,0.5)]'
                  : 'border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] hover:border-[#00B4DB]/60'
              }`}
            >
              <div className="flex items-center justify-between gap-2 text-[11px]">
                <span className="font-bold text-[#008CAE] dark:text-[#7EE8F5]">{item.authorizedOrg}</span>
                <span className="text-[#4C637C] dark:text-[#93ADC8] tabular-nums">+{item.pointsReward} pts</span>
              </div>
              <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {lText(item.title, lang)}
              </h3>
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{item.targetSystem}</p>
            </button>
          ))}
        </div>

        {/* Allowed vs Prohibited Guardrails */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="rounded-2xl border border-emerald-300 dark:border-emerald-800 bg-emerald-50/60 dark:bg-emerald-950/20 p-4 space-y-2">
            <h3 className="text-xs font-bold text-emerald-800 dark:text-emerald-300 flex items-center gap-1.5">
              <CheckCircle2 className="w-4 h-4" />
              {isArabic ? 'الإجراءات المسموحة داخل النطاق' : 'Authorized Actions'}
            </h3>
            <ul className="space-y-1 text-xs text-emerald-900 dark:text-emerald-200 list-disc list-inside">
              {(lab.allowedActions ?? []).map((a, idx) => (
                <li key={idx}>{lText(a, lang)}</li>
              ))}
            </ul>
          </div>

          <div className="rounded-2xl border border-rose-300 dark:border-rose-800 bg-rose-50/60 dark:bg-rose-950/20 p-4 space-y-2">
            <h3 className="text-xs font-bold text-rose-800 dark:text-rose-300 flex items-center gap-1.5">
              <ShieldAlert className="w-4 h-4" />
              {isArabic ? 'الإجراءات المحظورة' : 'Strictly Prohibited'}
            </h3>
            <ul className="space-y-1 text-xs text-rose-900 dark:text-rose-200 list-disc list-inside">
              {(lab.prohibitedActions ?? []).map((a, idx) => (
                <li key={idx}>{lText(a, lang)}</li>
              ))}
            </ul>
          </div>
        </div>

        {/* Sandbox Trace Inspector */}
        <div className="rounded-2xl border border-[#1C395E] bg-[#061120] text-[#F1F7FC] p-4 space-y-2 shadow-[0_14px_34px_-18px_rgba(0,180,219,0.35)]">
          <div className="flex items-center justify-between text-xs text-[#7EE8F5]">
            <span className="font-bold flex items-center gap-1.5">
              <Cpu className="w-4 h-4 text-[#00B4DB]" />
              Sandbox Artifact Trace
            </span>
            <span className="flex items-center gap-1">
              <Lock className="w-3.5 h-3.5" /> Read-Only Authorized Fixture
            </span>
          </div>
          <pre className="text-xs font-mono overflow-x-auto leading-relaxed whitespace-pre-wrap text-[#D5E6F5]">
            {lab.sampleArtifactTrace}
          </pre>
        </div>

        {/* Submit Understanding Report */}
        <form
          onSubmit={handleSubmitLab}
          className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3"
        >
          <SectionHeader title={strings.understandingReportLabel} />
          <ul className="text-xs text-[#4C637C] dark:text-[#93ADC8] space-y-1 list-disc list-inside">
            {(lab.understandingChecklist ?? []).map((c, idx) => (
              <li key={idx}>{lText(c, lang)}</li>
            ))}
          </ul>
          <textarea
            required
            rows={4}
            value={reportText}
            onChange={(e) => setReportText(e.target.value)}
            placeholder={
              isArabic
                ? 'اشرح الحالة الأمنية قبل وبعد الإصلاح مع ذكر حدود الحالات الحرجة...'
                : 'Document root cause, state transition proof, and safe boundary assertions...'
            }
            className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
          />
          <div className="flex justify-end">
            <button
              type="submit"
              disabled={submitting}
              className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-5 py-2 text-xs font-bold text-white"
            >
              {submitting ? '...' : strings.submitSolution}
            </button>
          </div>
        </form>
      </div>
    </StateWrapper>
  );
};
