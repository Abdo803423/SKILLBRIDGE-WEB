import React, { useMemo, useRef, useState } from 'react';
import {
  ArrowRight,
  Bot,
  Briefcase,
  CheckCircle2,
  Clock,
  Compass,
  Flame,
  Send,
  ShieldCheck,
  Sparkles,
  Target,
  TrendingUp,
} from 'lucide-react';
import { AnimatedNumber, MotionReveal, SpatialCard } from '../components/InteractiveMotion';
import {
  ReadinessRing,
  SectionHeader,
  SkillBarsChart,
  StateWrapper,
  StatusLabel,
} from '../components/SkillBridgeUi';
import { useSkillBridge, CHALLENGES_CATALOG } from '../context/SkillBridgeContext';
import { SKILL_PRESETS } from '../data/ecosystemData';
import { lText, localizedCareerLevel, localizedDomain } from '../i18n/localization';
import { analyzeSkillGapApi, streamCoachReplyApi, streamMilestoneTutorApi } from '../services/aiClient';
import { computeLevelFromEvidence, containsSecretLeak } from '../services/pointsAndBadgesEngine';

export const StudentHomeScreen: React.FC = () => {
  const {
    strings,
    isArabic,
    profile,
    activeGoal,
    gaps,
    milestones,
    submissions,
    snapshot,
    publicBriefs,
    setRoute,
    setSelectedChallengeId,
    setAiDrawerOpen,
    setAiAgentMode,
    updateStudentTaskStatus,
    actionBusy,
  } = useSkillBridge();

  const safeSubs = Array.isArray(submissions) ? submissions : [];
  const safeSkills = Array.isArray(profile?.currentSkills) ? profile.currentSkills : [];
  const safeMilestones = Array.isArray(milestones) ? milestones : [];
  const safeGaps = Array.isArray(gaps) ? gaps : [];
  const safePublicBriefs = Array.isArray(publicBriefs) ? publicBriefs : [];

  const realVerifiedCount = safeSubs.filter((s) => !s.isDemoSample && s.status === 'VERIFIED').length;
  const computedLevel = computeLevelFromEvidence(profile?.points ?? 0, realVerifiedCount);
  const readiness = activeGoal?.readinessScore ?? Math.min(92, 54 + realVerifiedCount * 10);

  const skillBars = useMemo(
    () => [
      { name: strings.techSkills, score: Math.min(96, 62 + safeSkills.length * 5) },
      { name: strings.softSkills, score: 78 },
      { name: strings.domainKnowledge, score: Math.min(94, 60 + realVerifiedCount * 8) },
      { name: strings.problemSolving, score: Math.min(95, 68 + safeMilestones.filter((m) => m.completed).length * 6) },
      { name: strings.commCollab, score: 82 },
    ],
    [strings, safeSkills.length, realVerifiedCount, safeMilestones],
  );

  const studentTasks = Array.isArray(snapshot?.tasks) ? snapshot.tasks : [];
  const nextMilestone = safeMilestones.find((m) => !m.completed) ?? safeMilestones[0] ?? null;

  return (
    <StateWrapper>
      <div className="space-y-6">
        {/* Hero Readiness + Profile KPI Row */}
        <MotionReveal immediate>
          <SpatialCard
            tiltStrength={2.5}
            highlight
            className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 sm:p-6"
          >
            <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
              <div className="space-y-3 min-w-0">
                <div className="flex flex-wrap items-center gap-2 text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5]">
                  <span>{localizedDomain(profile.domain, isArabic ? 'AR' : 'EN')}</span>
                  <span>·</span>
                  <span>{localizedCareerLevel(computedLevel, isArabic ? 'AR' : 'EN')}</span>
                  <span>·</span>
                  <span className="text-[#4C637C] dark:text-[#93ADC8]">{profile.country}</span>
                </div>
                <h2 className="font-display text-2xl font-bold tracking-tight text-[#0B1F38] dark:text-[#F1F7FC] [text-wrap:balance]">
                  {isArabic ? `مرحبًا، ${profile.displayName}` : `Welcome back, ${profile.displayName}`}
                </h2>
                <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] max-w-2xl leading-relaxed">
                  {activeGoal?.executiveSummary || profile.goalsSummary}
                </p>

                <div className="flex flex-wrap gap-2 pt-1">
                  <button
                    type="button"
                    onClick={() => setRoute('analysis')}
                    className="sb-btn-interactive inline-flex items-center gap-1.5 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white transition whitespace-nowrap"
                  >
                    <Sparkles className="w-4 h-4" />
                    {strings.aiGapAnalyzer}
                  </button>
                  <button
                    type="button"
                    onClick={() => setRoute('briefs')}
                    className="inline-flex items-center gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-4 py-2 text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] hover:border-[#00B4DB] active:scale-[0.98] transition whitespace-nowrap"
                  >
                    <Briefcase className="w-4 h-4 text-[#00B4DB] dark:text-[#7EE8F5]" />
                    {isArabic ? 'مشاريع الشركات المفتوحة' : 'Company Briefs'} ({safePublicBriefs.length})
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setAiAgentMode('SKILL_ADVISOR');
                      setAiDrawerOpen(true);
                    }}
                    className="inline-flex items-center gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-4 py-2 text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] hover:border-[#00B4DB] active:scale-[0.98] transition whitespace-nowrap"
                  >
                    <Bot className="w-4 h-4 text-[#00B4DB] dark:text-[#7EE8F5]" />
                    {strings.aiAgentTitle}
                  </button>
                </div>
              </div>

              <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5 shrink-0">
                <ReadinessRing score={readiness} label={strings.yourSkillMatch} />
                <div className="grid grid-cols-2 sm:grid-cols-1 gap-2.5">
                  <div className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-3.5 py-2.5 bg-[#F0F6FA] dark:bg-[#09172B]">
                    <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{strings.pointsLabel}</p>
                    <p className="font-display text-lg font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums">
                      <AnimatedNumber value={profile.points} />
                    </p>
                  </div>
                  <div className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-3.5 py-2.5 bg-[#F0F6FA] dark:bg-[#09172B]">
                    <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{strings.streakLabel}</p>
                    <p className="font-display text-lg font-bold text-[#008CAE] dark:text-[#7EE8F5] flex items-center gap-1 tabular-nums">
                      <Flame className="w-4 h-4 text-amber-500" />
                      <AnimatedNumber value={profile.streakDays} />
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </SpatialCard>
        </MotionReveal>

        {/* 6-Step Verified Career Loop */}
        <MotionReveal delayMs={60}>
          <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 sm:p-5 space-y-3">
            <SectionHeader
              title={strings.loopTitle}
              subtitle={strings.levelDisclaimer}
            />
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2">
              {strings.loopSteps.map((step, idx) => (
                <div
                  key={step}
                  className="group rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] hover:border-[#00B4DB] hover:-translate-y-0.5 transition-all duration-200 px-3 py-2.5"
                >
                  <span className="font-mono text-[10px] font-bold text-[#008CAE] dark:text-[#7EE8F5] tabular-nums">
                    0{idx + 1}
                  </span>
                  <p className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] mt-0.5">{step}</p>
                </div>
              ))}
            </div>
          </section>
        </MotionReveal>

        {/* Analytics & Next Recommended Step */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          <div className="lg:col-span-7 rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <SectionHeader
              title={strings.skillOverview}
              action={
                <button
                  type="button"
                  onClick={() => setRoute('analysis')}
                  className="text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline"
                >
                  {strings.appealOrAdjust}
                </button>
              }
            />
            <SkillBarsChart data={skillBars} />
          </div>

          <SpatialCard
            tiltStrength={3.5}
            className="lg:col-span-5 rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 flex flex-col justify-between gap-4"
          >
            <div className="space-y-3">
              <SectionHeader title={strings.recommendedNextStep} />
              {nextMilestone ? (
                <div className="rounded-xl border border-[#00B4DB]/40 dark:border-[#1C4E78] bg-[#E0F9FD]/60 dark:bg-[#08243B] p-4 space-y-1.5">
                  <span className="text-[11px] font-bold text-[#008CAE] dark:text-[#7EE8F5]">
                    {isArabic ? `المرحلة ${nextMilestone.phaseOrder}` : `Phase ${nextMilestone.phaseOrder}`} ·{' '}
                    {nextMilestone.estimatedHours}h
                  </span>
                  <h3 className="text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{nextMilestone.title}</h3>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] leading-relaxed">{nextMilestone.description}</p>
                </div>
              ) : (
                <div className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-4 space-y-1.5">
                  <p className="text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                    {safeGaps[0]?.skillName ?? (isArabic ? 'تشغيل محلل الفجوات الذكي' : 'Run AI Skill Gap Analyzer')}
                  </p>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">
                    {isArabic
                      ? 'حدّد وظيفتك المستهدفة لبناء خارطة مهام مخصصة والبدء بأقرب مشروع شركة.'
                      : 'Define your target engineering role to unlock a tailored roadmap & matching company briefs.'}
                  </p>
                </div>
              )}
            </div>

            <button
              type="button"
              onClick={() => setRoute('analysis')}
              className="sb-btn-interactive w-full rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] text-white py-2.5 text-xs font-bold flex items-center justify-center gap-1.5 transition"
            >
              {strings.proveThisSkill}
              <ArrowRight className="w-4 h-4" />
            </button>
          </SpatialCard>
        </div>

        {/* Synchronized Company Brief Milestone Tasks */}
        {studentTasks.length > 0 && (
          <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <SectionHeader
              title={isArabic ? 'مهام مشاريع الشركات المتزامنة' : 'Synced Company Brief Tasks'}
              subtitle={
                isArabic
                  ? 'تحديث حالة المهمة يظهر فورًا لدى مدير الإشراف والشركة'
                  : 'Task status changes synchronize instantly with your Company Director'
              }
            />
            <div className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E]">
              {studentTasks.map((task) => (
                <div key={task.id} className="py-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <div className="min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-sm font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">{task.title}</span>
                      <StatusLabel status={task.status} />
                    </div>
                    <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] mt-0.5 flex items-center gap-1.5">
                      <Clock className="w-3.5 h-3.5" />
                      {task.dueDateLabel}
                    </p>
                  </div>
                  <div className="flex items-center gap-1.5 shrink-0">
                    {(['TODO', 'IN_PROGRESS', 'DONE'] as const).map((st) => (
                      <button
                        key={st}
                        type="button"
                        disabled={actionBusy || task.status === st}
                        onClick={() => updateStudentTaskStatus(task.id, st, task.updatedAtEpochMs)}
                        className={`rounded-lg px-2.5 py-1 text-[11px] font-semibold border transition active:scale-95 whitespace-nowrap ${
                          task.status === st
                            ? 'bg-[#0B1F38] text-white border-[#0B1F38] dark:bg-[#00B4DB] dark:text-white dark:border-[#00B4DB]'
                            : 'border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8] hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F]'
                        }`}
                      >
                        {st.replace('_', ' ')}
                      </button>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Featured Challenges Preview */}
        <section className="space-y-3">
          <SectionHeader
            title={strings.activeChallenges}
            action={
              <button
                type="button"
                onClick={() => setRoute('discover')}
                className="inline-flex items-center gap-1 text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline"
              >
                <Compass className="w-3.5 h-3.5" />
                {strings.navDiscover}
              </button>
            }
          />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {CHALLENGES_CATALOG.slice(0, 3).map((ch, idx) => (
              <MotionReveal key={ch.id} delayMs={idx * 65}>
                <SpatialCard
                  tiltStrength={5}
                  className="h-full rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 flex flex-col justify-between gap-3"
                >
                  <div className="space-y-1.5">
                    <div className="flex items-center justify-between gap-2 text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
                      <span className="font-semibold text-[#008CAE] dark:text-[#7EE8F5]">{ch.companyName}</span>
                      <span>{ch.rewardLabel}</span>
                    </div>
                    <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                      {lText(ch.title, isArabic ? 'AR' : 'EN')}
                    </h3>
                    <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] line-clamp-2">
                      {lText(ch.summary, isArabic ? 'AR' : 'EN')}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      setSelectedChallengeId(ch.id);
                      setRoute('challengeDetail');
                    }}
                    className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] py-2 text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] hover:border-[#00B4DB] active:scale-[0.98] transition"
                  >
                    {strings.projectDetails}
                  </button>
                </SpatialCard>
              </MotionReveal>
            ))}
          </div>
        </section>
      </div>
    </StateWrapper>
  );
};

export const SkillAnalysisScreen: React.FC = () => {
  const {
    strings,
    isArabic,
    profile,
    activeGoal,
    gaps,
    milestones,
    saveAnalysisPlan,
    toggleMilestone,
    showToast,
  } = useSkillBridge();

  const safeGaps = Array.isArray(gaps) ? gaps : [];
  const safeMilestones = Array.isArray(milestones) ? milestones : [];
  const [targetRole, setTargetRole] = useState(activeGoal?.targetRole || 'Senior Backend & Distributed Systems Engineer');
  const [expLevel, setExpLevel] = useState(activeGoal?.experienceLevel || profile?.careerLevel || 'Mid-level');
  const [weeklyHours, setWeeklyHours] = useState(activeGoal?.weeklyHours || profile?.weeklyHours || 12);
  const [selectedSkills, setSelectedSkills] = useState<string[]>(() =>
    Array.isArray(profile?.currentSkills) ? profile.currentSkills : [],
  );
  const [notes, setNotes] = useState(profile?.goalsSummary ?? '');
  const [analyzing, setAnalyzing] = useState(false);
  const [aiError, setAiError] = useState<string | null>(null);

  const [tutorTitle, setTutorTitle] = useState<string | null>(null);
  const [tutorOutput, setTutorOutput] = useState('');
  const [tutorStreaming, setTutorStreaming] = useState(false);

  const toggleSkill = (sk: string) => {
    setSelectedSkills((prev) => (prev.includes(sk) ? prev.filter((x) => x !== sk) : [...prev, sk]));
  };

  const handleAnalyze = async (e: React.FormEvent) => {
    e.preventDefault();
    setAnalyzing(true);
    setAiError(null);
    try {
      const result = await analyzeSkillGapApi({
        targetRole,
        experienceLevel: expLevel,
        currentSkills: selectedSkills,
        backgroundNotes: notes,
        weeklyHours,
        isArabic,
      });
      await saveAnalysisPlan({
        targetRole,
        experienceLevel: expLevel,
        weeklyHours,
        currentSkills: selectedSkills,
        analysis: result,
      });
      showToast(isArabic ? 'تم تحليل الفجوات وتحديث خارطة التعلم.' : 'AI Skill Gap analysis saved.');
    } catch (err) {
      setAiError(err instanceof Error ? err.message : strings.aiAgentUnavailable);
    } finally {
      setAnalyzing(false);
    }
  };

  const openMilestoneTutor = async (title: string, desc: string, project: string) => {
    setTutorTitle(title);
    setTutorOutput('');
    setTutorStreaming(true);
    try {
      await streamMilestoneTutorApi(
        {
          targetRole,
          milestoneTitle: title,
          milestoneDescription: desc,
          practiceProject: project,
          isArabic,
        },
        (delta) => setTutorOutput((prev) => prev + delta),
      );
    } catch (err) {
      setTutorOutput(`⚠️ ${err instanceof Error ? err.message : strings.aiAgentUnavailable}`);
    } finally {
      setTutorStreaming(false);
    }
  };

  return (
    <StateWrapper>
      <div className="space-y-6">
        <SectionHeader
          title={strings.aiGapAnalyzer}
          subtitle={isArabic ? 'تحليل فوري لفجوات المهارات وبناء خارطة طريق عملية بالذكاء الاصطناعي' : 'Evaluate skill readiness and generate an actionable learning roadmap powered by server-side Gemini AI'}
        />

        <form
          onSubmit={handleAnalyze}
          className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-4"
        >
          {aiError && (
            <div className="rounded-xl border border-rose-300 bg-rose-50 dark:bg-rose-950/30 px-3 py-2 text-xs text-rose-700 dark:text-rose-300">
              {aiError}
            </div>
          )}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="md:col-span-2">
              <label className="block text-xs font-semibold mb-1">{isArabic ? 'المسمى الوظيفي المستهدف' : 'Target Role'}</label>
              <input
                type="text"
                required
                value={targetRole}
                onChange={(e) => setTargetRole(e.target.value)}
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-sm text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold mb-1">{isArabic ? 'الساعات الأسبوعية' : 'Weekly Study Hours'}</label>
              <input
                type="number"
                min={2}
                max={60}
                value={weeklyHours}
                onChange={(e) => setWeeklyHours(Number(e.target.value) || 10)}
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-sm text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold mb-1.5">{isArabic ? 'مهاراتك الحالية' : 'Your Current Skills'}</label>
            <div className="flex flex-wrap gap-1.5">
              {SKILL_PRESETS.map((sk) => {
                const active = selectedSkills.includes(sk);
                return (
                  <button
                    key={sk}
                    type="button"
                    onClick={() => toggleSkill(sk)}
                    className={`rounded-lg px-2.5 py-1 text-xs font-semibold border transition active:scale-95 ${
                      active
                        ? 'bg-[#00B4DB] text-white border-[#00B4DB] shadow-[0_6px_16px_-8px_rgba(0,180,219,0.65)]'
                        : 'border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8] hover:border-[#00B4DB]'
                    }`}
                  >
                    {sk}
                  </button>
                );
              })}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
            <div>
              <label className="block text-xs font-semibold mb-1">{isArabic ? 'المستوى الحالي' : 'Experience Level'}</label>
              <select
                value={expLevel}
                onChange={(e) => setExpLevel(e.target.value)}
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-sm text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              >
                <option value="Junior">Junior</option>
                <option value="Mid-level">Mid-level</option>
                <option value="Senior">Senior</option>
              </select>
            </div>
            <div className="md:col-span-2">
              <label className="block text-xs font-semibold mb-1">{isArabic ? 'ملخص الخبرة الحالية' : 'Background & Goals'}</label>
              <input
                type="text"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-sm text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
            </div>
          </div>

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={analyzing}
              className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] disabled:opacity-60 px-5 py-2.5 text-xs font-bold text-white flex items-center gap-2 whitespace-nowrap"
            >
              <Target className="w-4 h-4" />
              {analyzing
                ? isArabic
                  ? 'جارٍ التحليل الذكي...'
                  : 'Analyzing with AI...'
                : isArabic
                  ? 'تحليل الفجوات وبناء الخارطة'
                  : 'Generate Skill Gap & Roadmap'}
            </button>
          </div>
        </form>

        {/* Active Goal Output */}
        {activeGoal && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
            <div className="lg:col-span-5 space-y-4">
              <SpatialCard tiltStrength={3.5} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
                <ReadinessRing score={activeGoal.readinessScore} label={activeGoal.targetRole} />
                <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] leading-relaxed">
                  {activeGoal.executiveSummary}
                </p>
              </SpatialCard>

              <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
                <SectionHeader title={isArabic ? 'فجوات المهارات ذات الأولوية' : 'Prioritized Skill Gaps'} />
                <div className="space-y-2.5">
                  {safeGaps.map((gap) => (
                    <div key={gap.id} className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-3 space-y-1 hover:border-[#00B4DB] transition">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{gap.skillName}</span>
                        <StatusLabel status={gap.priority} />
                      </div>
                      <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
                        {gap.currentLevel} → {gap.targetLevel}
                      </p>
                      <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{gap.whyItMatters}</p>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="lg:col-span-7 rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
              <SectionHeader title={isArabic ? 'معالم خارطة الطريق' : 'Execution Milestones'} />
              <div className="space-y-3">
                {safeMilestones.map((ms) => (
                  <div key={ms.id} className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-4 space-y-2 hover:border-[#00B4DB]/70 transition">
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex items-start gap-2.5">
                        <input
                          type="checkbox"
                          checked={ms.completed}
                          onChange={(e) => toggleMilestone(ms.id, e.target.checked)}
                          className="mt-1 h-4 w-4 rounded accent-[#00B4DB]"
                        />
                        <div>
                          <p className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                            {isArabic ? `المرحلة ${ms.phaseOrder}` : `Phase ${ms.phaseOrder}`}: {ms.title}
                          </p>
                          <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] mt-1">{ms.description}</p>
                          <p className="text-[11px] text-[#008CAE] dark:text-[#7EE8F5] mt-1">
                            {isArabic ? 'التطبيق العملي:' : 'Practice:'} {ms.practiceProject} · {ms.estimatedHours}h
                          </p>
                        </div>
                      </div>
                      <button
                        type="button"
                        onClick={() => openMilestoneTutor(ms.title, ms.description, ms.practiceProject)}
                        className="rounded-lg border border-[#00B4DB]/45 dark:border-[#1C4E78] bg-[#E0F9FD]/60 dark:bg-[#08243B] px-2.5 py-1 text-[11px] font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:border-[#00B4DB] active:scale-95 transition shrink-0 whitespace-nowrap"
                      >
                        {isArabic ? 'شرح ذكي' : 'Micro-Tutor'}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Micro-Sprint Tutor Drawer/Card */}
        {tutorTitle && (
          <div className="sb-route-enter rounded-2xl border border-[#00B4DB] bg-white dark:bg-[#0D1E36] p-5 space-y-2 shadow-[0_14px_34px_-18px_rgba(0,180,219,0.45)]">
            <div className="flex items-center justify-between">
              <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {isArabic ? 'الدرس المكثف:' : 'AI Micro-Sprint Tutor:'} {tutorTitle}
              </h3>
              <button
                type="button"
                onClick={() => setTutorTitle(null)}
                className="text-xs text-[#4C637C] dark:text-[#93ADC8] hover:text-[#0B1F38] dark:hover:text-[#F1F7FC]"
              >
                {strings.closeLabel}
              </button>
            </div>
            <div className="text-xs leading-relaxed text-[#0B1F38] dark:text-[#F1F7FC] whitespace-pre-wrap">
              {tutorOutput || (tutorStreaming ? '...' : '')}
            </div>
          </div>
        )}
      </div>
    </StateWrapper>
  );
};

export const StudentBriefsScreen: React.FC = () => {
  const {
    isArabic,
    strings,
    publicBriefs,
    snapshot,
    applyToBrief,
    submitProjectWork,
    updateStudentTaskStatus,
    actionBusy,
    session,
    showToast,
  } = useSkillBridge();

  const [applyTargetId, setApplyTargetId] = useState<string | null>(null);
  const [coverNote, setCoverNote] = useState('');
  const [roleRequested, setRoleRequested] = useState('Full-Stack / Backend Contributor');
  const [filterTab, setFilterTab] = useState<'ALL' | 'APPLIED' | 'ACCEPTED'>('ALL');
  const [briefSearch, setBriefSearch] = useState('');
  const [expandedBriefId, setExpandedBriefId] = useState<string | null>(null);

  const [submitTargetId, setSubmitTargetId] = useState<string | null>(null);
  const [repoUrl, setRepoUrl] = useState('');
  const [understanding, setUnderstanding] = useState('');
  const [skillsInput, setSkillsInput] = useState('TypeScript, SQL, System Design');

  const briefs = useMemo(() => {
    const safePublic = Array.isArray(publicBriefs) ? publicBriefs : [];
    const safeSnapBriefs = Array.isArray(snapshot?.briefs) ? snapshot.briefs : [];
    const map = new Map(safePublic.map((b) => [b.id, b]));
    for (const b of safeSnapBriefs) map.set(b.id, b);
    return Array.from(map.values());
  }, [publicBriefs, snapshot?.briefs]);

  const myAppsMap = useMemo(() => {
    const map = new Map<string, NonNullable<typeof snapshot>['applications'][number]>();
    const safeApps = Array.isArray(snapshot?.applications) ? snapshot.applications : [];
    for (const app of safeApps) {
      map.set(app.briefId, app);
    }
    return map;
  }, [snapshot?.applications]);

  const filteredBriefs = useMemo(() => {
    const q = briefSearch.trim().toLowerCase();
    return briefs.filter((b) => {
      const app = myAppsMap.get(b.id);
      if (filterTab === 'APPLIED' && !app) return false;
      if (filterTab === 'ACCEPTED' && app?.status !== 'ACCEPTED') return false;
      if (!q) return true;
      return `${b.title ?? ''} ${b.companyName ?? ''} ${b.domain ?? ''} ${b.skillsCsv ?? ''}`.toLowerCase().includes(q);
    });
  }, [briefs, myAppsMap, filterTab, briefSearch]);

  const allTasks = Array.isArray(snapshot?.tasks) ? snapshot.tasks : [];

  return (
    <StateWrapper>
      <div className="space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SectionHeader
            title={isArabic ? 'مشاريع الشركات المعتمدة' : 'Live Company Briefs Marketplace'}
            subtitle={
              isArabic
                ? 'تقديم مباشر، مهام متابعة، وتوثيق من مدير الإشراف'
                : 'Apply to verified briefs, complete tasks, and submit deliverables for Director grading'
            }
          />
          <input
            type="text"
            value={briefSearch}
            onChange={(e) => setBriefSearch(e.target.value)}
            placeholder={isArabic ? 'ابحث في مشاريع الشركات...' : 'Filter by title, company, skill...'}
            className="w-full sm:w-64 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] px-3 py-1.5 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
          />
        </div>

        <div className="flex flex-wrap gap-1.5">
          {(
            [
              { id: 'ALL', label: isArabic ? 'الكل' : 'All Open Briefs' },
              { id: 'APPLIED', label: isArabic ? 'طلباتي' : 'My Applications' },
              { id: 'ACCEPTED', label: isArabic ? 'المقبول بها' : 'Accepted Cohorts' },
            ] as const
          ).map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setFilterTab(tab.id)}
              className={`rounded-xl px-3 py-1.5 text-xs font-bold border whitespace-nowrap active:scale-95 transition ${
                filterTab === tab.id
                  ? 'bg-[#0B1F38] text-white border-[#0B1F38] dark:bg-[#00B4DB] dark:text-white dark:border-[#00B4DB]'
                  : 'border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] text-[#4C637C] dark:text-[#93ADC8] hover:border-[#00B4DB]'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredBriefs.map((brief, idx) => {
            const myApp = myAppsMap.get(brief.id);
            const briefTasks = allTasks.filter((t) => t.briefId === brief.id);
            const expanded = expandedBriefId === brief.id;
            return (
              <MotionReveal key={brief.id} delayMs={Math.min(idx, 5) * 55}>
                <SpatialCard
                  tiltStrength={4}
                  highlight={myApp?.status === 'ACCEPTED'}
                  className="h-full rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 flex flex-col justify-between gap-4"
                >
                  <div className="space-y-2.5">
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-xs font-bold text-[#008CAE] dark:text-[#7EE8F5]">
                        {brief.companyName} · {brief.domain}
                      </span>
                      <StatusLabel status={myApp ? myApp.status : brief.approvalStatus} />
                    </div>
                    <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{brief.title}</h3>
                    <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] leading-relaxed">{brief.summary}</p>
                    <div className="text-[11px] text-[#4C637C] dark:text-[#93ADC8] flex flex-wrap gap-3 tabular-nums">
                      <span>{brief.level}</span>
                      <span>·</span>
                      <span className="font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">{brief.prizePool}</span>
                      <span>·</span>
                      <span>+{brief.pointsReward} {strings.pointsLabel}</span>
                      <span>·</span>
                      <span>{brief.deadline}</span>
                    </div>
                    <div className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
                      <span className="font-semibold">{isArabic ? 'المهارات:' : 'Skills:'}</span> {brief.skillsCsv}
                    </div>

                    {expanded && (
                      <div className="rounded-xl bg-[#F0F6FA] dark:bg-[#09172B] p-3 text-[11px] space-y-1 text-[#0B1F38] dark:text-[#F1F7FC]">
                        <p>
                          <span className="font-semibold">{strings.rubricBeforeStart}:</span> {brief.rubricCsv}
                        </p>
                        <p>
                          <span className="font-semibold">Deliverables:</span> {brief.deliverablesCsv}
                        </p>
                        {brief.assignedDirectorName && (
                          <p>
                            <span className="font-semibold">Supervising Director:</span> {brief.assignedDirectorName}
                          </p>
                        )}
                      </div>
                    )}

                    {myApp?.status === 'ACCEPTED' && briefTasks.length > 0 && (
                      <div className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-3 space-y-2">
                        <p className="text-[11px] font-bold text-[#008CAE] dark:text-[#7EE8F5]">
                          {isArabic ? 'مهام التنفيذ المسندة:' : 'Assigned Sprint Tasks:'}
                        </p>
                        {briefTasks.map((t) => (
                          <div key={t.id} className="flex items-center justify-between gap-2 text-[11px]">
                            <span className="truncate font-medium text-[#0B1F38] dark:text-[#F1F7FC]">{t.title}</span>
                            <select
                              value={t.status}
                              disabled={actionBusy}
                              onChange={(e) =>
                                updateStudentTaskStatus(
                                  t.id,
                                  e.target.value as 'TODO' | 'IN_PROGRESS' | 'DONE',
                                  t.updatedAtEpochMs,
                                )
                              }
                              className="rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] px-2 py-0.5 text-[11px] text-[#0B1F38] dark:text-[#F1F7FC]"
                            >
                              <option value="TODO">TODO</option>
                              <option value="IN_PROGRESS">IN PROGRESS</option>
                              <option value="DONE">DONE</option>
                            </select>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>

                  <div className="flex flex-wrap gap-2 pt-2 border-t border-[#D5E0EC]/80 dark:border-[#1C395E]">
                    <button
                      type="button"
                      onClick={() => setExpandedBriefId(expanded ? null : brief.id)}
                      className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-3 py-2 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB] active:scale-95 transition whitespace-nowrap"
                    >
                      {expanded ? strings.closeLabel : strings.projectDetails}
                    </button>
                    {!myApp && (
                      <button
                        type="button"
                        onClick={() => {
                          if (!session) {
                            showToast(strings.guestLockedReason);
                            return;
                          }
                          setApplyTargetId(brief.id);
                        }}
                        className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white whitespace-nowrap"
                      >
                        {strings.applyNow}
                      </button>
                    )}
                    {myApp?.status === 'ACCEPTED' && (
                      <button
                        type="button"
                        onClick={() => setSubmitTargetId(brief.id)}
                        className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] text-white px-4 py-2 text-xs font-bold flex items-center gap-1.5 whitespace-nowrap"
                      >
                        <CheckCircle2 className="w-4 h-4" />
                        {strings.submitSolution}
                      </button>
                    )}
                  </div>
                </SpatialCard>
              </MotionReveal>
            );
          })}
        </div>

        {/* Apply Modal */}
        {applyTargetId && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#061120]/65 backdrop-blur-sm p-4">
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                await applyToBrief(applyTargetId, coverNote, roleRequested);
                setApplyTargetId(null);
                setCoverNote('');
              }}
              className="sb-route-enter w-full max-w-md rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-5 space-y-3 shadow-2xl"
            >
              <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{strings.applyNow}</h3>
              <div>
                <label className="block text-xs font-semibold mb-1">{isArabic ? 'الدور المقترح' : 'Requested Role'}</label>
                <input
                  type="text"
                  required
                  value={roleRequested}
                  onChange={(e) => setRoleRequested(e.target.value)}
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold mb-1">{isArabic ? 'رسالة التقديم' : 'Cover Note & Relevant Proof'}</label>
                <textarea
                  required
                  rows={3}
                  value={coverNote}
                  onChange={(e) => setCoverNote(e.target.value)}
                  placeholder="Highlight verified skills or prior systems you built..."
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
                />
              </div>
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setApplyTargetId(null)} className="px-3 py-1.5 text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8]">
                  {strings.cancelLabel}
                </button>
                <button
                  type="submit"
                  disabled={actionBusy}
                  className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white"
                >
                  {strings.confirmLabel}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Submit Deliverable Modal */}
        {submitTargetId && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#061120]/65 backdrop-blur-sm p-4">
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                if (containsSecretLeak(`${repoUrl}\n${understanding}`)) {
                  showToast(strings.secretLeakWarning);
                  return;
                }
                await submitProjectWork({
                  briefId: submitTargetId,
                  repoOrDemoUrl: repoUrl,
                  understandingReport: understanding,
                  skillsApplied: skillsInput.split(',').map((s) => s.trim()).filter(Boolean),
                });
                setSubmitTargetId(null);
                setRepoUrl('');
                setUnderstanding('');
              }}
              className="sb-route-enter w-full max-w-lg rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-5 space-y-3 shadow-2xl"
            >
              <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{strings.submitSolution}</h3>
              <div>
                <label className="block text-xs font-semibold mb-1">Repository / Demo URL</label>
                <input
                  type="url"
                  required
                  value={repoUrl}
                  onChange={(e) => setRepoUrl(e.target.value)}
                  placeholder="https://github.com/..."
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold mb-1">{strings.understandingReportLabel}</label>
                <textarea
                  required
                  rows={4}
                  value={understanding}
                  onChange={(e) => setUnderstanding(e.target.value)}
                  placeholder="Describe architecture choices, test proof, edge cases..."
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold mb-1">{isArabic ? 'المهارات المطبقة' : 'Skills Applied (comma separated)'}</label>
                <input
                  type="text"
                  value={skillsInput}
                  onChange={(e) => setSkillsInput(e.target.value)}
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
                />
              </div>
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setSubmitTargetId(null)} className="px-3 py-1.5 text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8]">
                  {strings.cancelLabel}
                </button>
                <button
                  type="submit"
                  disabled={actionBusy}
                  className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white"
                >
                  {strings.confirmLabel}
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </StateWrapper>
  );
};

export const AiCoachScreen: React.FC = () => {
  const { isArabic, strings, activeGoal, gaps, saveCoachMessage } = useSkillBridge();
  const [mode, setMode] = useState<'COACH' | 'INTERVIEW'>('COACH');
  const [messages, setMessages] = useState<Array<{ sender: 'USER' | 'AI'; content: string }>>([]);
  const [input, setInput] = useState('');
  const [streaming, setStreaming] = useState(false);
  const abortRef = useRef<AbortController | null>(null);

  const sendMessage = async (textOverride?: string) => {
    const query = (textOverride ?? input).trim();
    if (!query || streaming) return;
    if (!textOverride) setInput('');

    const nextHistory = [...messages, { sender: 'USER' as const, content: query }];
    setMessages([...nextHistory, { sender: 'AI', content: '' }]);
    setStreaming(true);
    saveCoachMessage(mode, 'USER', query).catch(() => {});

    const controller = new AbortController();
    abortRef.current = controller;

    let assembled = '';
    try {
      await streamCoachReplyApi(
        {
          mode,
          targetRole: activeGoal?.targetRole ?? 'Software Engineer',
          matchedSkills: Array.isArray(activeGoal?.matchedSkills) ? activeGoal.matchedSkills : ['SQL', 'TypeScript'],
          topGaps: (Array.isArray(gaps) ? gaps : []).slice(0, 4).map((g) => g.skillName),
          history: nextHistory.slice(-6),
          userPrompt: query,
          isArabic,
        },
        (delta) => {
          assembled += delta;
          setMessages((prev) => {
            const copy = [...prev];
            if (copy.length === 0) return [{ sender: 'AI', content: assembled }];
            copy[copy.length - 1] = { sender: 'AI', content: assembled };
            return copy;
          });
        },
        controller.signal,
      );
      if (assembled) {
        saveCoachMessage(mode, 'AI', assembled).catch(() => {});
      }
    } catch (err) {
      const msg = err instanceof Error ? err.message : strings.aiAgentUnavailable;
      setMessages((prev) => {
        const copy = [...prev];
        if (copy.length === 0) return [{ sender: 'AI', content: `⚠️ ${msg}` }];
        copy[copy.length - 1] = { sender: 'AI', content: `⚠️ ${msg}` };
        return copy;
      });
    } finally {
      setStreaming(false);
    }
  };

  return (
    <StateWrapper>
      <div className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SectionHeader
            title={isArabic ? 'المدرب المهني ومحاكاة المقابلات' : 'AI Career Coach & Mock Interviewer'}
            subtitle={isArabic ? 'محادثة متخصصة بالبث المباشر مبنية على أهدافك' : 'Streaming real-time coaching & technical interview practice'}
          />
          <div className="flex gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-1">
            {(['COACH', 'INTERVIEW'] as const).map((m) => (
              <button
                key={m}
                type="button"
                onClick={() => setMode(m)}
                className={`rounded-lg px-3 py-1.5 text-xs font-bold transition active:scale-95 whitespace-nowrap ${
                  mode === m
                    ? 'bg-[#00B4DB] text-white'
                    : 'text-[#4C637C] dark:text-[#93ADC8] hover:text-[#0B1F38] dark:hover:text-[#F1F7FC]'
                }`}
              >
                {m === 'COACH'
                  ? isArabic
                    ? 'مدرب المسار'
                    : 'Career Coach'
                  : isArabic
                    ? 'مقابلة تقنية'
                    : 'Mock Interview'}
              </button>
            ))}
          </div>
        </div>

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] flex flex-col h-[520px]">
          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {messages.length === 0 && (
              <div className="h-full flex flex-col items-center justify-center text-center gap-3 px-4">
                <ShieldCheck className="w-8 h-8 text-[#00B4DB] dark:text-[#7EE8F5]" />
                <p className="text-sm font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">
                  {mode === 'INTERVIEW'
                    ? isArabic
                      ? 'ابدأ مقابلة تقنية واقعية مع تقييم فوري'
                      : 'Start a realistic technical interview drill'
                    : isArabic
                      ? 'اسأل مدرب SkillBridge عن مسارك أو مشاريعك'
                      : 'Ask your AI Career Coach about projects, gaps, or readiness'}
                </p>
                <div className="flex flex-wrap justify-center gap-2">
                  {[
                    mode === 'INTERVIEW'
                      ? isArabic
                        ? 'اسألني سؤال تصميم نظام Backend صعب'
                        : 'Ask me a mid-level distributed systems interview question'
                      : isArabic
                        ? 'اقترح خطة مذاكرة لآخر ٣٠ يومًا قبل مشروع الشركة'
                        : 'Give me a 2-week sprint plan to close my highest priority skill gap',
                  ].map((q) => (
                    <button
                      key={q}
                      type="button"
                      onClick={() => sendMessage(q)}
                      className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-3 py-1.5 text-xs font-medium text-[#0B1F38] dark:text-[#F1F7FC] hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] hover:border-[#00B4DB] active:scale-95 transition"
                    >
                      <TrendingUp className="w-3.5 h-3.5 inline me-1 text-[#00B4DB] dark:text-[#7EE8F5]" />
                      {q}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {messages.map((m, idx) => (
              <div key={idx} className={`flex ${m.sender === 'USER' ? 'justify-end' : 'justify-start'}`}>
                <div
                  className={`max-w-[85%] rounded-2xl px-4 py-2.5 text-xs leading-relaxed whitespace-pre-wrap ${
                    m.sender === 'USER'
                      ? 'bg-[#0B1F38] dark:bg-[#00B4DB] text-white'
                      : 'bg-[#F0F6FA] dark:bg-[#132D4F] text-[#0B1F38] dark:text-[#F1F7FC]'
                  }`}
                >
                  {m.content || (streaming ? '...' : '')}
                </div>
              </div>
            ))}
          </div>

          <form
            onSubmit={(e) => {
              e.preventDefault();
              sendMessage();
            }}
            className="p-3 border-t border-[#D5E0EC] dark:border-[#1C395E] flex items-center gap-2"
          >
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder={strings.aiAgentAskHint}
              disabled={streaming}
              className="flex-1 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
            />
            <button
              type="submit"
              disabled={streaming || !input.trim()}
              className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] disabled:opacity-50 px-4 py-2 text-xs font-bold text-white flex items-center gap-1.5"
            >
              <Send className="w-3.5 h-3.5" />
            </button>
          </form>
        </div>
      </div>
    </StateWrapper>
  );
};
