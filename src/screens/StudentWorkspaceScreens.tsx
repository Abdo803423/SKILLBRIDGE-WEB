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

  const realVerifiedCount = submissions.filter((s) => !s.isDemoSample && s.status === 'VERIFIED').length;
  const computedLevel = computeLevelFromEvidence(profile.points, realVerifiedCount);
  const readiness = activeGoal?.readinessScore ?? Math.min(92, 54 + realVerifiedCount * 10);

  const skillBars = useMemo(
    () => [
      { name: strings.techSkills, score: Math.min(96, 62 + profile.currentSkills.length * 5) },
      { name: strings.softSkills, score: 78 },
      { name: strings.domainKnowledge, score: Math.min(94, 60 + realVerifiedCount * 8) },
      { name: strings.problemSolving, score: Math.min(95, 68 + milestones.filter((m) => m.completed).length * 6) },
      { name: strings.commCollab, score: 82 },
    ],
    [strings, profile.currentSkills.length, realVerifiedCount, milestones],
  );

  const studentTasks = snapshot?.tasks ?? [];
  const nextMilestone = milestones.find((m) => !m.completed) ?? milestones[0] ?? null;

  return (
    <StateWrapper>
      <div className="space-y-6">
        {/* Hero Readiness + Profile KPI Row */}
        <section className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 sm:p-6">
          <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
            <div className="space-y-3 min-w-0">
              <div className="flex flex-wrap items-center gap-2 text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5]">
                <span>{localizedDomain(profile.domain, isArabic ? 'AR' : 'EN')}</span>
                <span>·</span>
                <span>{localizedCareerLevel(computedLevel, isArabic ? 'AR' : 'EN')}</span>
                <span>·</span>
                <span className="text-slate-500 dark:text-slate-400">{profile.country}</span>
              </div>
              <h2 className="font-display text-2xl font-bold text-[#0B1F38] dark:text-white">
                {isArabic ? `مرحبًا، ${profile.displayName}` : `Welcome back, ${profile.displayName}`}
              </h2>
              <p className="text-xs text-slate-600 dark:text-slate-300 max-w-2xl leading-relaxed">
                {activeGoal?.executiveSummary || profile.goalsSummary}
              </p>

              <div className="flex flex-wrap gap-2 pt-1">
                <button
                  type="button"
                  onClick={() => setRoute('analysis')}
                  className="inline-flex items-center gap-1.5 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white transition"
                >
                  <Sparkles className="w-4 h-4" />
                  {strings.aiGapAnalyzer}
                </button>
                <button
                  type="button"
                  onClick={() => setRoute('briefs')}
                  className="inline-flex items-center gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-slate-700 px-4 py-2 text-xs font-bold text-[#0B1F38] dark:text-white hover:bg-slate-50 dark:hover:bg-slate-800 transition"
                >
                  <Briefcase className="w-4 h-4 text-[#008CAE]" />
                  {isArabic ? 'مشاريع الشركات المفتوحة' : 'Company Briefs'} ({publicBriefs.length})
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setAiAgentMode('SKILL_ADVISOR');
                    setAiDrawerOpen(true);
                  }}
                  className="inline-flex items-center gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-slate-700 px-4 py-2 text-xs font-bold text-[#0B1F38] dark:text-white hover:bg-slate-50 dark:hover:bg-slate-800 transition"
                >
                  <Bot className="w-4 h-4 text-[#008CAE]" />
                  {strings.aiAgentTitle}
                </button>
              </div>
            </div>

            <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5 shrink-0">
              <ReadinessRing score={readiness} label={strings.yourSkillMatch} />
              <div className="grid grid-cols-2 sm:grid-cols-1 gap-2.5">
                <div className="rounded-xl border border-slate-200 dark:border-slate-800 px-3.5 py-2.5 bg-slate-50 dark:bg-slate-950/40">
                  <p className="text-[11px] text-slate-500">{strings.pointsLabel}</p>
                  <p className="font-display text-lg font-bold text-[#0B1F38] dark:text-white tabular-nums">
                    {profile.points}
                  </p>
                </div>
                <div className="rounded-xl border border-slate-200 dark:border-slate-800 px-3.5 py-2.5 bg-slate-50 dark:bg-slate-950/40">
                  <p className="text-[11px] text-slate-500">{strings.streakLabel}</p>
                  <p className="font-display text-lg font-bold text-[#008CAE] dark:text-[#7EE8F5] flex items-center gap-1 tabular-nums">
                    <Flame className="w-4 h-4 text-amber-500" />
                    {profile.streakDays}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* 6-Step Verified Career Loop */}
        <section className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4 sm:p-5 space-y-3">
          <SectionHeader
            title={strings.loopTitle}
            subtitle={strings.levelDisclaimer}
          />
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2">
            {strings.loopSteps.map((step, idx) => (
              <div
                key={step}
                className="rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950/40 px-3 py-2.5"
              >
                <span className="text-[10px] font-bold text-[#008CAE] dark:text-[#7EE8F5] tabular-nums">
                  0{idx + 1}
                </span>
                <p className="text-xs font-bold text-[#0B1F38] dark:text-white mt-0.5">{step}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Analytics & Next Recommended Step */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          <div className="lg:col-span-7 rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
            <SectionHeader
              title={strings.skillOverview}
              action={
                <button
                  type="button"
                  onClick={() => setRoute('analysis')}
                  className="text-xs font-semibold text-[#008CAE] hover:underline"
                >
                  {strings.appealOrAdjust}
                </button>
              }
            />
            <SkillBarsChart data={skillBars} />
          </div>

          <div className="lg:col-span-5 rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 flex flex-col justify-between gap-4">
            <div className="space-y-3">
              <SectionHeader title={strings.recommendedNextStep} />
              {nextMilestone ? (
                <div className="rounded-xl border border-[#00B4DB]/30 bg-[#E0F9FD]/40 dark:bg-slate-800/70 p-4 space-y-1.5">
                  <span className="text-[11px] font-bold text-[#008CAE] dark:text-[#7EE8F5]">
                    {isArabic ? `المرحلة ${nextMilestone.phaseOrder}` : `Phase ${nextMilestone.phaseOrder}`} ·{' '}
                    {nextMilestone.estimatedHours}h
                  </span>
                  <h3 className="text-sm font-bold text-[#0B1F38] dark:text-white">{nextMilestone.title}</h3>
                  <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">{nextMilestone.description}</p>
                </div>
              ) : (
                <div className="rounded-xl border border-slate-200 dark:border-slate-800 p-4 space-y-1.5">
                  <p className="text-sm font-bold text-[#0B1F38] dark:text-white">
                    {gaps[0]?.skillName ?? (isArabic ? 'تشغيل محلل الفجوات الذكي' : 'Run AI Skill Gap Analyzer')}
                  </p>
                  <p className="text-xs text-slate-600 dark:text-slate-300">
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
              className="w-full rounded-xl bg-[#0B1F38] dark:bg-[#00B4DB] text-white dark:text-slate-950 py-2.5 text-xs font-bold flex items-center justify-center gap-1.5 hover:opacity-90"
            >
              {strings.proveThisSkill}
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Synchronized Company Brief Milestone Tasks */}
        {studentTasks.length > 0 && (
          <section className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
            <SectionHeader
              title={isArabic ? 'مهام مشاريع الشركات المتزامنة' : 'Synced Company Brief Tasks'}
              subtitle={
                isArabic
                  ? 'تحديث حالة المهمة يظهر فورًا لدى مدير الإشراف والشركة'
                  : 'Task status changes synchronize instantly with your Company Director'
              }
            />
            <div className="divide-y divide-slate-100 dark:divide-slate-800">
              {studentTasks.map((task) => (
                <div key={task.id} className="py-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <div className="min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-sm font-semibold text-[#0B1F38] dark:text-white">{task.title}</span>
                      <StatusLabel status={task.status} />
                    </div>
                    <p className="text-xs text-slate-500 mt-0.5 flex items-center gap-1.5">
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
                        className={`rounded-lg px-2.5 py-1 text-[11px] font-semibold border transition ${
                          task.status === st
                            ? 'bg-[#0B1F38] text-white border-[#0B1F38] dark:bg-[#00B4DB] dark:text-slate-950'
                            : 'border-slate-300 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800'
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
                className="inline-flex items-center gap-1 text-xs font-semibold text-[#008CAE] hover:underline"
              >
                <Compass className="w-3.5 h-3.5" />
                {strings.navDiscover}
              </button>
            }
          />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {CHALLENGES_CATALOG.slice(0, 3).map((ch) => (
              <div
                key={ch.id}
                className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4 flex flex-col justify-between gap-3"
              >
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between gap-2 text-[11px] text-slate-500">
                    <span className="font-semibold text-[#008CAE] dark:text-[#7EE8F5]">{ch.companyName}</span>
                    <span>{ch.rewardLabel}</span>
                  </div>
                  <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-white">
                    {lText(ch.title, isArabic ? 'AR' : 'EN')}
                  </h3>
                  <p className="text-xs text-slate-600 dark:text-slate-300 line-clamp-2">
                    {lText(ch.summary, isArabic ? 'AR' : 'EN')}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => {
                    setSelectedChallengeId(ch.id);
                    setRoute('challengeDetail');
                  }}
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 py-2 text-xs font-bold hover:bg-slate-50 dark:hover:bg-slate-800"
                >
                  {strings.projectDetails}
                </button>
              </div>
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

  const [targetRole, setTargetRole] = useState(activeGoal?.targetRole || 'Senior Backend & Distributed Systems Engineer');
  const [expLevel, setExpLevel] = useState(activeGoal?.experienceLevel || profile.careerLevel || 'Mid-level');
  const [weeklyHours, setWeeklyHours] = useState(activeGoal?.weeklyHours || profile.weeklyHours || 12);
  const [selectedSkills, setSelectedSkills] = useState<string[]>(profile.currentSkills);
  const [notes, setNotes] = useState(profile.goalsSummary);
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
          className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-4"
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
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-sm"
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
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-sm"
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
                    className={`rounded-lg px-2.5 py-1 text-xs font-semibold border transition ${
                      active
                        ? 'bg-[#0B1F38] text-white border-[#0B1F38] dark:bg-[#00B4DB] dark:text-slate-950'
                        : 'border-slate-300 dark:border-slate-700 text-slate-600 dark:text-slate-300'
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
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-sm"
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
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-sm"
              />
            </div>
          </div>

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={analyzing}
              className="rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] disabled:opacity-60 px-5 py-2.5 text-xs font-bold text-white flex items-center gap-2"
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
              <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
                <ReadinessRing score={activeGoal.readinessScore} label={activeGoal.targetRole} />
                <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">
                  {activeGoal.executiveSummary}
                </p>
              </div>

              <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
                <SectionHeader title={isArabic ? 'فجوات المهارات ذات الأولوية' : 'Prioritized Skill Gaps'} />
                <div className="space-y-2.5">
                  {gaps.map((gap) => (
                    <div key={gap.id} className="rounded-xl border border-slate-200 dark:border-slate-800 p-3 space-y-1">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-[#0B1F38] dark:text-white">{gap.skillName}</span>
                        <StatusLabel status={gap.priority} />
                      </div>
                      <p className="text-[11px] text-slate-500">
                        {gap.currentLevel} → {gap.targetLevel}
                      </p>
                      <p className="text-xs text-slate-600 dark:text-slate-300">{gap.whyItMatters}</p>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="lg:col-span-7 rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
              <SectionHeader title={isArabic ? 'معالم خارطة الطريق' : 'Execution Milestones'} />
              <div className="space-y-3">
                {milestones.map((ms) => (
                  <div key={ms.id} className="rounded-xl border border-slate-200 dark:border-slate-800 p-4 space-y-2">
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex items-start gap-2.5">
                        <input
                          type="checkbox"
                          checked={ms.completed}
                          onChange={(e) => toggleMilestone(ms.id, e.target.checked)}
                          className="mt-1 h-4 w-4 rounded accent-[#00B4DB]"
                        />
                        <div>
                          <p className="text-xs font-bold text-[#0B1F38] dark:text-white">
                            {isArabic ? `المرحلة ${ms.phaseOrder}` : `Phase ${ms.phaseOrder}`}: {ms.title}
                          </p>
                          <p className="text-xs text-slate-600 dark:text-slate-300 mt-1">{ms.description}</p>
                          <p className="text-[11px] text-[#008CAE] dark:text-[#7EE8F5] mt-1">
                            {isArabic ? 'التطبيق العملي:' : 'Practice:'} {ms.practiceProject} · {ms.estimatedHours}h
                          </p>
                        </div>
                      </div>
                      <button
                        type="button"
                        onClick={() => openMilestoneTutor(ms.title, ms.description, ms.practiceProject)}
                        className="rounded-lg border border-[#00B4DB]/40 px-2.5 py-1 text-[11px] font-semibold text-[#008CAE] dark:text-[#7EE8F5] shrink-0"
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
          <div className="rounded-2xl border border-[#00B4DB] bg-white dark:bg-slate-900 p-5 space-y-2">
            <div className="flex items-center justify-between">
              <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-white">
                {isArabic ? 'الدرس المكثف:' : 'AI Micro-Sprint Tutor:'} {tutorTitle}
              </h3>
              <button
                type="button"
                onClick={() => setTutorTitle(null)}
                className="text-xs text-slate-500 hover:text-slate-800"
              >
                {strings.closeLabel}
              </button>
            </div>
            <div className="text-xs leading-relaxed text-slate-700 dark:text-slate-200 whitespace-pre-wrap">
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
    const map = new Map(publicBriefs.map((b) => [b.id, b]));
    for (const b of snapshot?.briefs ?? []) map.set(b.id, b);
    return Array.from(map.values());
  }, [publicBriefs, snapshot?.briefs]);

  const myAppsMap = useMemo(() => {
    const map = new Map<string, NonNullable<typeof snapshot>['applications'][number]>();
    for (const app of snapshot?.applications ?? []) {
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
      return `${b.title} ${b.companyName} ${b.domain} ${b.skillsCsv}`.toLowerCase().includes(q);
    });
  }, [briefs, myAppsMap, filterTab, briefSearch]);

  const allTasks = snapshot?.tasks ?? [];

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
            className="w-full sm:w-64 rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-1.5 text-xs"
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
              className={`rounded-xl px-3 py-1.5 text-xs font-bold border whitespace-nowrap ${
                filterTab === tab.id
                  ? 'bg-[#0B1F38] text-white border-[#0B1F38] dark:bg-[#00B4DB] dark:text-slate-950'
                  : 'border-[#D5E0EC] dark:border-slate-700 bg-white dark:bg-slate-900'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredBriefs.map((brief) => {
            const myApp = myAppsMap.get(brief.id);
            const briefTasks = allTasks.filter((t) => t.briefId === brief.id);
            const expanded = expandedBriefId === brief.id;
            return (
              <div
                key={brief.id}
                className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 flex flex-col justify-between gap-4"
              >
                <div className="space-y-2.5">
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-xs font-bold text-[#008CAE] dark:text-[#7EE8F5]">
                      {brief.companyName} · {brief.domain}
                    </span>
                    <StatusLabel status={myApp ? myApp.status : brief.approvalStatus} />
                  </div>
                  <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-white">{brief.title}</h3>
                  <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">{brief.summary}</p>
                  <div className="text-[11px] text-slate-500 flex flex-wrap gap-3 tabular-nums">
                    <span>{brief.level}</span>
                    <span>·</span>
                    <span className="font-semibold text-[#0B1F38] dark:text-slate-200">{brief.prizePool}</span>
                    <span>·</span>
                    <span>+{brief.pointsReward} {strings.pointsLabel}</span>
                    <span>·</span>
                    <span>{brief.deadline}</span>
                  </div>
                  <div className="text-[11px] text-slate-600 dark:text-slate-400">
                    <span className="font-semibold">{isArabic ? 'المهارات:' : 'Skills:'}</span> {brief.skillsCsv}
                  </div>

                  {expanded && (
                    <div className="rounded-xl bg-slate-50 dark:bg-slate-800/70 p-3 text-[11px] space-y-1">
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
                    <div className="rounded-xl border border-slate-200 dark:border-slate-800 p-3 space-y-2">
                      <p className="text-[11px] font-bold text-[#008CAE]">
                        {isArabic ? 'مهام التنفيذ المسندة:' : 'Assigned Sprint Tasks:'}
                      </p>
                      {briefTasks.map((t) => (
                        <div key={t.id} className="flex items-center justify-between gap-2 text-[11px]">
                          <span className="truncate font-medium">{t.title}</span>
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
                            className="rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 px-2 py-0.5 text-[11px]"
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

                <div className="flex flex-wrap gap-2 pt-2 border-t border-slate-100 dark:border-slate-800">
                  <button
                    type="button"
                    onClick={() => setExpandedBriefId(expanded ? null : brief.id)}
                    className="rounded-xl border border-[#D5E0EC] dark:border-slate-700 px-3 py-2 text-xs font-semibold"
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
                      className="rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white"
                    >
                      {strings.applyNow}
                    </button>
                  )}
                  {myApp?.status === 'ACCEPTED' && (
                    <button
                      type="button"
                      onClick={() => setSubmitTargetId(brief.id)}
                      className="rounded-xl bg-[#0B1F38] dark:bg-[#00B4DB] text-white dark:text-slate-950 px-4 py-2 text-xs font-bold flex items-center gap-1.5"
                    >
                      <CheckCircle2 className="w-4 h-4" />
                      {strings.submitSolution}
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        {/* Apply Modal */}
        {applyTargetId && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                await applyToBrief(applyTargetId, coverNote, roleRequested);
                setApplyTargetId(null);
                setCoverNote('');
              }}
              className="w-full max-w-md rounded-2xl bg-white dark:bg-slate-900 border border-[#D5E0EC] dark:border-slate-800 p-5 space-y-3"
            >
              <h3 className="font-display text-base font-bold">{strings.applyNow}</h3>
              <div>
                <label className="block text-xs font-semibold mb-1">{isArabic ? 'الدور المقترح' : 'Requested Role'}</label>
                <input
                  type="text"
                  required
                  value={roleRequested}
                  onChange={(e) => setRoleRequested(e.target.value)}
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
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
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
                />
              </div>
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setApplyTargetId(null)} className="px-3 py-1.5 text-xs font-semibold">
                  {strings.cancelLabel}
                </button>
                <button
                  type="submit"
                  disabled={actionBusy}
                  className="rounded-xl bg-[#00B4DB] px-4 py-2 text-xs font-bold text-white"
                >
                  {strings.confirmLabel}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Submit Deliverable Modal */}
        {submitTargetId && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
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
              className="w-full max-w-lg rounded-2xl bg-white dark:bg-slate-900 border border-[#D5E0EC] dark:border-slate-800 p-5 space-y-3"
            >
              <h3 className="font-display text-base font-bold">{strings.submitSolution}</h3>
              <div>
                <label className="block text-xs font-semibold mb-1">Repository / Demo URL</label>
                <input
                  type="url"
                  required
                  value={repoUrl}
                  onChange={(e) => setRepoUrl(e.target.value)}
                  placeholder="https://github.com/..."
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
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
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold mb-1">{isArabic ? 'المهارات المطبقة' : 'Skills Applied (comma separated)'}</label>
                <input
                  type="text"
                  value={skillsInput}
                  onChange={(e) => setSkillsInput(e.target.value)}
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
                />
              </div>
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setSubmitTargetId(null)} className="px-3 py-1.5 text-xs font-semibold">
                  {strings.cancelLabel}
                </button>
                <button
                  type="submit"
                  disabled={actionBusy}
                  className="rounded-xl bg-[#00B4DB] px-4 py-2 text-xs font-bold text-white"
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
          matchedSkills: activeGoal?.matchedSkills ?? ['SQL', 'TypeScript'],
          topGaps: gaps.slice(0, 4).map((g) => g.skillName),
          history: nextHistory.slice(-6),
          userPrompt: query,
          isArabic,
        },
        (delta) => {
          assembled += delta;
          setMessages((prev) => {
            const copy = [...prev];
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
          <div className="flex gap-1.5 rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-white dark:bg-slate-900 p-1">
            {(['COACH', 'INTERVIEW'] as const).map((m) => (
              <button
                key={m}
                type="button"
                onClick={() => setMode(m)}
                className={`rounded-lg px-3 py-1.5 text-xs font-bold transition ${
                  mode === m
                    ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-slate-950'
                    : 'text-slate-600 dark:text-slate-300'
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

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 flex flex-col h-[520px]">
          <div className="flex-1 overflow-y-auto p-4 space-y-3">
            {messages.length === 0 && (
              <div className="h-full flex flex-col items-center justify-center text-center gap-3 px-4">
                <ShieldCheck className="w-8 h-8 text-[#00B4DB]" />
                <p className="text-sm font-semibold text-[#0B1F38] dark:text-white">
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
                      className="rounded-xl border border-[#D5E0EC] dark:border-slate-700 px-3 py-1.5 text-xs font-medium hover:bg-slate-50 dark:hover:bg-slate-800"
                    >
                      <TrendingUp className="w-3.5 h-3.5 inline me-1 text-[#00B4DB]" />
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
                      ? 'bg-[#0B1F38] text-white'
                      : 'bg-slate-100 dark:bg-slate-800 text-[#0B1F38] dark:text-slate-100'
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
            className="p-3 border-t border-[#D5E0EC] dark:border-slate-800 flex items-center gap-2"
          >
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder={strings.aiAgentAskHint}
              disabled={streaming}
              className="flex-1 rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
            />
            <button
              type="submit"
              disabled={streaming || !input.trim()}
              className="rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] disabled:opacity-50 px-4 py-2 text-xs font-bold text-white flex items-center gap-1.5"
            >
              <Send className="w-3.5 h-3.5" />
            </button>
          </form>
        </div>
      </div>
    </StateWrapper>
  );
};
