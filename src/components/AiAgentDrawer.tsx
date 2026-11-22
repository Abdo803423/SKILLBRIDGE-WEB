import React, { useMemo, useRef, useState } from 'react';
import {
  AlertCircle,
  Bot,
  Check,
  Flag,
  Send,
  Shield,
  Sparkles,
  ThumbsDown,
  ThumbsUp,
  X,
} from 'lucide-react';
import { useSkillBridge } from '../context/SkillBridgeContext';
import { streamAgentReplyApi } from '../services/aiClient';
import type { AiAgentContextMode, AiAgentMessage, AiAgentPendingAction } from '../types/skillbridge';

const AGENT_MODES: Array<{ id: AiAgentContextMode; en: string; ar: string }> = [
  { id: 'SKILL_ADVISOR', en: 'Skill Advisor', ar: 'مستشار المهارات' },
  { id: 'CHALLENGE_MENTOR', en: 'Challenge Mentor', ar: 'مرشد التحديات' },
  { id: 'TEAM_SPRINT_PLANNER', en: 'Team Planner', ar: 'مخطط الفريق' },
  { id: 'COMMUNITY_COACH', en: 'Community Coach', ar: 'مدرب المجتمع' },
  { id: 'REVERSE_ENG_GUIDE', en: 'Reverse Eng Guide', ar: 'مرشد الهندسة العكسية' },
  { id: 'PASSPORT_REVIEWER', en: 'Passport Review', ar: 'مراجع السجل' },
];

export const AiAgentDrawer: React.FC = () => {
  const {
    aiDrawerOpen,
    setAiDrawerOpen,
    aiAgentMode,
    setAiAgentMode,
    aiMessages,
    setAiMessages,
    profile,
    activeGoal,
    gaps,
    route,
    isArabic,
    strings,
    submitModerationReport,
    updateStudentProfile,
    session,
    showToast,
  } = useSkillBridge();

  const [promptInput, setPromptInput] = useState('');
  const [streaming, setStreaming] = useState(false);
  const [confirmAction, setConfirmAction] = useState<AiAgentPendingAction | null>(null);
  const abortRef = useRef<AbortController | null>(null);

  const contextSummary = useMemo(() => {
    const base = `Route=${route}; DisplayName=${profile.displayName}; Domain=${profile.domain}; Level=${profile.careerLevel}`;
    const goalPart = activeGoal ? `; TargetRole=${activeGoal.targetRole}; Readiness=${activeGoal.readinessScore}%` : '';
    const gapsPart = gaps.length > 0 ? `; TopGaps=${gaps.slice(0, 3).map((g) => g.skillName).join(', ')}` : '';
    return `${base}${goalPart}${gapsPart}`;
  }, [route, profile.displayName, profile.domain, profile.careerLevel, activeGoal, gaps]);

  if (!aiDrawerOpen) return null;

  const handleSend = async (overrideText?: string) => {
    const query = (overrideText ?? promptInput).trim();
    if (!query || streaming) return;
    if (!overrideText) setPromptInput('');

    const userMsg: AiAgentMessage = {
      id: `u_${Date.now()}`,
      sender: 'USER',
      content: query,
    };
    const aiMsgId = `a_${Date.now() + 1}`;
    const initialAiMsg: AiAgentMessage = {
      id: aiMsgId,
      sender: 'AI',
      content: '',
      dataUsedLabel: profile.aiContextSharingEnabled
        ? `${profile.domain}, ${profile.careerLevel}`
        : isArabic
          ? 'وضع عام بدون بيانات شخصية'
          : 'Generic mode (context sharing off)',
      boundariesLabel: isArabic
        ? 'إرشاد تدريبي فقط؛ لا يتجاوز قواعد التحقق أو ينشر تلقائيًا'
        : 'Guidance only — never bypasses rubric evaluation or publishes without confirmation',
      feedbackPositive: null,
    };

    setAiMessages((prev) => [...prev, userMsg, initialAiMsg]);
    setStreaming(true);

    const controller = new AbortController();
    abortRef.current = controller;

    let assembled = '';
    try {
      await streamAgentReplyApi(
        {
          mode: aiAgentMode,
          isArabic,
          contextSummary,
          allowPersonalization: profile.aiPersonalizationEnabled,
          allowContextSharing: profile.aiContextSharingEnabled,
          history: aiMessages.slice(-6).map((m) => ({ sender: m.sender, content: m.content })),
          userPrompt: query,
        },
        (delta) => {
          assembled += delta;
          setAiMessages((prev) =>
            prev.map((m) => (m.id === aiMsgId ? { ...m, content: assembled } : m)),
          );
        },
        controller.signal,
      );

      // Offer structured action confirmation if recommending a skill addition
      if (aiAgentMode === 'SKILL_ADVISOR' && profile.targetSkills.length < 6 && !profile.targetSkills.includes('System Design')) {
        setAiMessages((prev) =>
          prev.map((m) =>
            m.id === aiMsgId
              ? {
                  ...m,
                  pendingAction: {
                    type: 'UpdateTargetSkills',
                    skillsToAdd: ['System Design'],
                    description: {
                      en: 'Add "System Design" to your target roadmap skills',
                      ar: 'إضافة "تصميم الأنظمة" إلى مهارات خارطة الطريق المستهدفة',
                    },
                  },
                }
              : m,
          ),
        );
      }
    } catch (err) {
      const msg = err instanceof Error ? err.message : strings.aiAgentUnavailable;
      setAiMessages((prev) =>
        prev.map((m) => (m.id === aiMsgId ? { ...m, content: `⚠️ ${msg}` } : m)),
      );
    } finally {
      setStreaming(false);
      abortRef.current = null;
    }
  };

  const handleConfirmPendingAction = async () => {
    if (!confirmAction) return;
    if (confirmAction.type === 'UpdateTargetSkills') {
      const merged = Array.from(new Set([...profile.targetSkills, ...confirmAction.skillsToAdd]));
      if (session) {
        await updateStudentProfile({
          displayName: profile.displayName,
          domain: profile.domain,
          country: profile.country,
          skills: profile.currentSkills,
          targetSkills: merged,
          goalsSummary: profile.goalsSummary,
          weeklyHours: profile.weeklyHours,
        });
      } else {
        showToast(isArabic ? 'سجّل الدخول لحفظ تحديث المهارات المستهدفة.' : 'Sign in to persist target skill updates.');
      }
    }
    setConfirmAction(null);
  };

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/40 backdrop-blur-[1px]">
      <div className="w-full max-w-md h-full bg-white dark:bg-slate-900 border-s border-[#D5E0EC] dark:border-slate-800 flex flex-col">
        {/* Header */}
        <div className="px-4 py-3 border-b border-[#D5E0EC] dark:border-slate-800 flex items-center justify-between gap-2 bg-[#0B1F38] text-white">
          <div className="flex items-center gap-2.5 min-w-0">
            <img
              src="/assets/images/img_coach_orbit_1794605869758.jpg"
              alt="AI Coach"
              className="w-8 h-8 rounded-lg object-cover border border-[#00B4DB]/50 shrink-0"
            />
            <div className="min-w-0">
              <h3 className="text-sm font-bold truncate">{strings.aiAgentTitle}</h3>
              <p className="text-[11px] text-[#7EE8F5] truncate">
                {AGENT_MODES.find((m) => m.id === aiAgentMode)?.[isArabic ? 'ar' : 'en']}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => {
              abortRef.current?.abort();
              setAiDrawerOpen(false);
            }}
            className="p-1.5 rounded-lg hover:bg-white/10 text-slate-200"
            aria-label={strings.closeLabel}
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Mode Switcher */}
        <div className="px-3 py-2 border-b border-[#D5E0EC] dark:border-slate-800 flex gap-1.5 overflow-x-auto bg-slate-50 dark:bg-slate-950/60">
          {AGENT_MODES.map((m) => {
            const active = m.id === aiAgentMode;
            return (
              <button
                key={m.id}
                type="button"
                onClick={() => setAiAgentMode(m.id)}
                className={`rounded-lg px-2.5 py-1 text-[11px] font-semibold whitespace-nowrap shrink-0 transition ${
                  active
                    ? 'bg-[#00B4DB] text-white'
                    : 'bg-white dark:bg-slate-800 text-slate-600 dark:text-slate-300 border border-slate-200 dark:border-slate-700'
                }`}
              >
                {isArabic ? m.ar : m.en}
              </button>
            );
          })}
        </div>

        {/* Transparency & Boundaries Card */}
        <div className="mx-3 mt-3 rounded-xl border border-[#00B4DB]/30 bg-[#E0F9FD]/50 dark:bg-slate-800/70 px-3 py-2 text-[11px] text-slate-700 dark:text-slate-300 space-y-1">
          <div className="flex items-center gap-1.5 font-semibold text-[#0B1F38] dark:text-[#7EE8F5]">
            <Shield className="w-3.5 h-3.5" />
            <span>{isArabic ? 'الشفافية وضوابط المساعد' : 'Transparency & Safe Guardrails'}</span>
          </div>
          <p className="text-[11px] leading-relaxed">
            {isArabic
              ? 'يعتمد الذكاء الاصطناعي على أهدافك ومهاراتك الحالية دون أي تعديل تلقائي على درجات التقييم أو التسليمات.'
              : 'Uses your active screen context & target role. Rubric grading & credential verification remain strictly human/system verified.'}
          </p>
        </div>

        {/* Messages */}
        <div className="flex-1 overflow-y-auto p-3 space-y-3">
          {aiMessages.length === 0 && (
            <div className="rounded-xl border border-dashed border-slate-300 dark:border-slate-700 p-4 text-center space-y-2">
              <Bot className="w-6 h-6 mx-auto text-[#00B4DB]" />
              <p className="text-xs font-medium text-slate-600 dark:text-slate-300">
                {isArabic
                  ? 'اسأل المساعد عن فجوات المهارات، خطة أسبوعية، أو مراجعة تقرير الهندسة العكسية.'
                  : 'Ask for next learning steps, rubric readiness tips, or architecture trade-offs.'}
              </p>
              <div className="flex flex-wrap justify-center gap-1.5 pt-1">
                {[
                  isArabic ? 'ما أهم مهارة أركز عليها الآن؟' : 'What skill should I prove next?',
                  isArabic ? 'راجع خطة العمل لمشروع الشركة' : 'How should I approach a company brief?',
                ].map((q) => (
                  <button
                    key={q}
                    type="button"
                    onClick={() => handleSend(q)}
                    className="rounded-lg border border-slate-200 dark:border-slate-700 px-2.5 py-1 text-[11px] font-medium text-[#0B1F38] dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800"
                  >
                    {q}
                  </button>
                ))}
              </div>
            </div>
          )}

          {aiMessages.map((msg) => (
            <div
              key={msg.id}
              className={`flex flex-col ${msg.sender === 'USER' ? 'items-end' : 'items-start'}`}
            >
              <div
                className={`max-w-[88%] rounded-2xl px-3.5 py-2.5 text-xs leading-relaxed whitespace-pre-wrap ${
                  msg.sender === 'USER'
                    ? 'bg-[#0B1F38] text-white'
                    : 'bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-100 border border-slate-200 dark:border-slate-700'
                }`}
              >
                {msg.content || (streaming ? '...' : '')}
              </div>

              {msg.sender === 'AI' && msg.content && (
                <div className="mt-1.5 space-y-1.5 max-w-[88%]">
                  {msg.pendingAction && (
                    <button
                      type="button"
                      onClick={() => setConfirmAction(msg.pendingAction!)}
                      className="inline-flex items-center gap-1.5 rounded-lg bg-[#00B4DB]/15 border border-[#00B4DB]/40 px-2.5 py-1 text-[11px] font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:bg-[#00B4DB]/25"
                    >
                      <Sparkles className="w-3 h-3" />
                      {msg.pendingAction.description[isArabic ? 'ar' : 'en']}
                    </button>
                  )}

                  <div className="flex items-center gap-2 text-[11px] text-slate-500">
                    <button
                      type="button"
                      onClick={() =>
                        setAiMessages((prev) =>
                          prev.map((m) => (m.id === msg.id ? { ...m, feedbackPositive: true } : m)),
                        )
                      }
                      className={`p-1 rounded hover:bg-slate-100 dark:hover:bg-slate-800 ${
                        msg.feedbackPositive === true ? 'text-emerald-600' : ''
                      }`}
                      aria-label="Helpful"
                    >
                      <ThumbsUp className="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      onClick={() =>
                        setAiMessages((prev) =>
                          prev.map((m) => (m.id === msg.id ? { ...m, feedbackPositive: false } : m)),
                        )
                      }
                      className={`p-1 rounded hover:bg-slate-100 dark:hover:bg-slate-800 ${
                        msg.feedbackPositive === false ? 'text-rose-600' : ''
                      }`}
                      aria-label="Not helpful"
                    >
                      <ThumbsDown className="w-3.5 h-3.5" />
                    </button>
                    {!msg.isReported && (
                      <button
                        type="button"
                        onClick={async () => {
                          setAiMessages((prev) =>
                            prev.map((m) => (m.id === msg.id ? { ...m, isReported: true } : m)),
                          );
                          if (session) {
                            await submitModerationReport('AI_RESPONSE', msg.id, 'User flagged AI suggestion for audit.');
                          } else {
                            showToast(isArabic ? 'تم تسجيل البلاغ للمراجعة.' : 'Response flagged for safety review.');
                          }
                        }}
                        className="inline-flex items-center gap-1 hover:text-rose-600"
                      >
                        <Flag className="w-3 h-3" />
                        {strings.aiAgentReportLabel}
                      </button>
                    )}
                    {msg.isReported && (
                      <span className="inline-flex items-center gap-1 text-emerald-600">
                        <Check className="w-3 h-3" /> {isArabic ? 'مُبلّغ' : 'Flagged'}
                      </span>
                    )}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>

        {/* Confirm Action Modal inside drawer */}
        {confirmAction && (
          <div className="mx-3 mb-2 rounded-xl border border-amber-300 dark:border-amber-700 bg-amber-50 dark:bg-amber-950/50 p-3 space-y-2">
            <div className="flex items-center gap-1.5 text-xs font-bold text-[#0B1F38] dark:text-white">
              <AlertCircle className="w-4 h-4 text-amber-600" />
              {strings.aiAgentConfirmActionTitle}
            </div>
            <p className="text-xs text-slate-700 dark:text-slate-300">
              {confirmAction.description[isArabic ? 'ar' : 'en']}
            </p>
            <div className="flex gap-2 justify-end">
              <button
                type="button"
                onClick={() => setConfirmAction(null)}
                className="rounded-lg border border-slate-300 dark:border-slate-700 px-3 py-1 text-xs font-medium"
              >
                {strings.cancelLabel}
              </button>
              <button
                type="button"
                onClick={handleConfirmPendingAction}
                className="rounded-lg bg-[#00B4DB] px-3 py-1 text-xs font-semibold text-white"
              >
                {strings.confirmLabel}
              </button>
            </div>
          </div>
        )}

        {/* Composer */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSend();
          }}
          className="p-3 border-t border-[#D5E0EC] dark:border-slate-800 flex items-center gap-2"
        >
          <input
            type="text"
            value={promptInput}
            onChange={(e) => setPromptInput(e.target.value)}
            placeholder={strings.aiAgentAskHint}
            disabled={streaming}
            className="flex-1 rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs text-[#0B1F38] dark:text-white focus:outline-none focus:border-[#00B4DB]"
          />
          <button
            type="submit"
            disabled={streaming || !promptInput.trim()}
            className="rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] disabled:opacity-50 p-2.5 text-white transition shrink-0"
            aria-label="Send"
          >
            <Send className="w-4 h-4" />
          </button>
        </form>
      </div>
    </div>
  );
};
