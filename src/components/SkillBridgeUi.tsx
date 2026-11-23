import React from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  CloudOff,
  Compass,
  Lock,
  RefreshCw,
  ShieldCheck,
  Sparkles,
} from 'lucide-react';
import { Bar, BarChart, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { useSkillBridge } from '../context/SkillBridgeContext';
import { localizedStatus } from '../i18n/localization';
import { AnimatedNumber } from './InteractiveMotion';

export const SkillBridgeLogo: React.FC<{ size?: 'sm' | 'md' | 'lg'; subtitle?: boolean }> = ({
  size = 'md',
  subtitle = false,
}) => {
  const dim = size === 'sm' ? 'w-8 h-8 rounded-lg' : size === 'lg' ? 'w-11 h-11 rounded-2xl' : 'w-9 h-9 rounded-xl';
  const titleCls =
    size === 'sm'
      ? 'text-base font-bold'
      : size === 'lg'
        ? 'text-2xl font-bold'
        : 'text-lg font-bold';

  return (
    <div className="flex items-center gap-2.5 shrink-0 group">
      <div
        className={`${dim} overflow-hidden bg-[#0B1F38] border border-[#00B4DB]/40 shadow-[0_6px_18px_-8px_rgba(0,180,219,0.5)] flex items-center justify-center shrink-0 transition-transform duration-300 group-hover:scale-105 group-active:scale-95`}
      >
        <img
          src={`${import.meta.env.BASE_URL}assets/images/skillbridge_app_icon_1794769666078.jpg`}
          alt="SkillBridge"
          className="w-full h-full object-cover"
        />
      </div>
      <div className="leading-tight">
        <span className={`font-display tracking-tight text-[#0B1F38] dark:text-[#F1F7FC] block whitespace-nowrap ${titleCls}`}>
          Skill<span className="text-[#00B4DB]">Bridge</span>
        </span>
        {subtitle && (
          <span className="block text-[10px] font-semibold tracking-wider uppercase text-[#4C637C] dark:text-[#93ADC8]">
            Career Web Platform
          </span>
        )}
      </div>
    </div>
  );
};

export const StatusLabel: React.FC<{ status: string; className?: string }> = ({ status, className = '' }) => {
  const { language } = useSkillBridge();
  const s = status.toUpperCase();
  let tone =
    'border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8] bg-[#F0F6FA] dark:bg-[#091A30]';

  if (['VERIFIED', 'APPROVED', 'ACTIVE', 'ACCEPTED', 'DONE', 'LIVE', 'PAID', 'RESOLVED', 'TRUSTED'].includes(s)) {
    tone =
      'border-[#00B4DB]/45 dark:border-[#00B4DB]/50 text-[#008CAE] dark:text-[#7EE8F5] bg-[#E0F9FD]/80 dark:bg-[#08263D]';
  } else if (
    ['PENDING', 'PENDING_APPROVAL', 'PENDING_REVIEW', 'UNDER_REVIEW', 'IN_PROGRESS', 'SHORTLISTED', 'UPCOMING', 'APPLIED', 'BUSINESS'].includes(
      s,
    )
  ) {
    tone =
      'border-sky-300 dark:border-sky-800 text-sky-700 dark:text-sky-300 bg-sky-50 dark:bg-sky-950/45';
  } else if (['REVISION_REQUESTED', 'DISPUTED', 'OPEN', 'BASIC'].includes(s)) {
    tone =
      'border-amber-300 dark:border-amber-800 text-amber-700 dark:text-amber-300 bg-amber-50 dark:bg-amber-950/45';
  } else if (['REJECTED', 'SUSPENDED'].includes(s)) {
    tone =
      'border-rose-300 dark:border-rose-800 text-rose-700 dark:text-rose-300 bg-rose-50 dark:bg-rose-950/40';
  }

  return (
    <span
      className={`inline-flex items-center gap-1 rounded-md border px-2 py-0.5 text-[11px] font-semibold whitespace-nowrap shrink-0 ${tone} ${className}`}
    >
      {localizedStatus(s, language)}
    </span>
  );
};

export const SectionHeader: React.FC<{
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}> = ({ title, subtitle, action }) => (
  <div className="flex flex-wrap items-center justify-between gap-3">
    <div className="min-w-0">
      <h2 className="font-display text-lg font-bold tracking-tight text-[#0B1F38] dark:text-[#F1F7FC] [text-wrap:balance]">
        {title}
      </h2>
      {subtitle && <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] mt-0.5">{subtitle}</p>}
    </div>
    {action && <div className="shrink-0">{action}</div>}
  </div>
);

export const ReadinessRing: React.FC<{ score: number; label: string }> = ({ score, label }) => {
  const clamped = Math.max(0, Math.min(100, Math.round(score)));
  const radius = 44;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (clamped / 100) * circumference;

  return (
    <div className="flex items-center gap-4 shrink-0 group">
      <div className="relative w-28 h-28 flex items-center justify-center transition-transform duration-300 group-hover:scale-105">
        <svg className="w-28 h-28 -rotate-90 drop-shadow-[0_4px_14px_rgba(0,180,219,0.28)]" viewBox="0 0 100 100">
          <defs>
            <linearGradient id="sbReadinessGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#008CAE" />
              <stop offset="55%" stopColor="#00B4DB" />
              <stop offset="100%" stopColor="#7EE8F5" />
            </linearGradient>
          </defs>
          <circle
            cx="50"
            cy="50"
            r={radius}
            fill="none"
            stroke="currentColor"
            strokeWidth="8"
            className="text-[#D5E0EC] dark:text-[#1C395E]"
          />
          <circle
            cx="50"
            cy="50"
            r={radius}
            fill="none"
            stroke="url(#sbReadinessGrad)"
            strokeWidth="8"
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            className="transition-all duration-700 ease-out"
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
          <span className="font-display text-2xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums">
            <AnimatedNumber value={clamped} suffix="%" />
          </span>
        </div>
      </div>
      <div className="min-w-0">
        <p className="text-xs font-semibold uppercase tracking-wider text-[#4C637C] dark:text-[#93ADC8]">{label}</p>
        <p className="text-sm font-medium text-[#0B1F38] dark:text-[#F1F7FC] mt-1">
          {clamped >= 75 ? 'Ready for Verified Briefs' : clamped >= 50 ? 'Active Skill Progression' : 'Building Core Foundations'}
        </p>
      </div>
    </div>
  );
};

export interface SkillMetricDatum {
  name: string;
  score: number;
}

export const SkillBarsChart: React.FC<{ data: SkillMetricDatum[] }> = ({ data }) => {
  const colors = ['#00B4DB', '#0B1F38', '#008CAE', '#0284C7', '#38BDF8'];
  return (
    <div className="h-56 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} layout="vertical" margin={{ top: 4, right: 16, left: 8, bottom: 4 }}>
          <XAxis type="number" domain={[0, 100]} hide />
          <YAxis
            type="category"
            dataKey="name"
            width={132}
            tick={{ fontSize: 11, fill: '#4C637C' }}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip
            formatter={(val: number) => [`${val}%`, 'Score']}
            contentStyle={{
              borderRadius: '10px',
              border: '1px solid #D5E0EC',
              backgroundColor: '#FFFFFF',
              color: '#0B1F38',
              fontSize: '12px',
            }}
          />
          <Bar dataKey="score" radius={[0, 6, 6, 0]} barSize={18}>
            {data.map((_, index) => (
              <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

export const StateWrapper: React.FC<{
  children: React.ReactNode;
  allowBypassPreview?: boolean;
}> = ({ children, allowBypassPreview = false }) => {
  const {
    previewStateMode,
    setPreviewStateMode,
    isOnline,
    strings,
    setRoute,
    isArabic,
    session,
    firebaseUser,
  } = useSkillBridge();

  return (
    <div className="space-y-4">
      {!isOnline && (
        <div className="flex items-center gap-2.5 rounded-xl border border-amber-300 dark:border-amber-800 bg-amber-50 dark:bg-amber-950/40 px-4 py-2.5 text-xs font-medium text-amber-900 dark:text-amber-200">
          <CloudOff className="w-4 h-4 shrink-0" />
          <span>{strings.offlineBannerText}</span>
        </div>
      )}

      {!allowBypassPreview && previewStateMode === 'LOADING' && (
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-8 flex flex-col items-center justify-center text-center gap-3">
          <RefreshCw className="w-7 h-7 text-[#00B4DB] animate-spin" />
          <p className="text-sm font-medium text-[#4C637C] dark:text-[#93ADC8]">{strings.loadingText}</p>
          <button
            type="button"
            onClick={() => setPreviewStateMode('NORMAL')}
            className="text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline"
          >
            {isArabic ? 'العودة للحالة الطبيعية' : 'Return to Normal State'}
          </button>
        </div>
      )}

      {!allowBypassPreview && previewStateMode === 'EMPTY' && (
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-8 flex flex-col items-center justify-center text-center gap-3">
          <Compass className="w-8 h-8 text-[#00B4DB]" />
          <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{strings.emptyTitle}</h3>
          <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] max-w-sm">
            {isArabic
              ? 'هذه معاينة لحالة الفراغ — يمكنك استكشاف التحديات أو استعادة البيانات.'
              : 'UI Empty State Preview — explore challenges or restore active state.'}
          </p>
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => {
                setPreviewStateMode('NORMAL');
                setRoute('discover');
              }}
              className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-semibold text-white transition"
            >
              {strings.emptyAction}
            </button>
            <button
              type="button"
              onClick={() => setPreviewStateMode('NORMAL')}
              className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-4 py-2 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC] hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F]"
            >
              {isArabic ? 'استعادة' : 'Reset State'}
            </button>
          </div>
        </div>
      )}

      {!allowBypassPreview && previewStateMode === 'ERROR' && (
        <div className="rounded-2xl border border-rose-300 dark:border-rose-800 bg-rose-50/70 dark:bg-rose-950/30 p-8 flex flex-col items-center justify-center text-center gap-3">
          <AlertTriangle className="w-8 h-8 text-rose-700 dark:text-rose-400" />
          <h3 className="font-display text-base font-bold text-rose-900 dark:text-rose-200">
            {isArabic ? 'تعذر مزامنة البيانات (معاينة)' : 'Synchronization Interrupted (Preview)'}
          </h3>
          <button
            type="button"
            onClick={() => setPreviewStateMode('NORMAL')}
            className="sb-btn-interactive rounded-xl bg-rose-700 px-4 py-2 text-xs font-semibold text-white hover:bg-rose-800"
          >
            {strings.errorRetry}
          </button>
        </div>
      )}

      {!allowBypassPreview && previewStateMode === 'LOCKED' && (
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-8 flex flex-col items-center justify-center text-center gap-3">
          <Lock className="w-8 h-8 text-[#008CAE] dark:text-[#7EE8F5]" />
          <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{strings.lockedTitle}</h3>
          <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] max-w-md">{strings.guestLockedReason}</p>
          <div className="flex gap-2">
            {!session && !firebaseUser && (
              <button
                type="button"
                onClick={() => {
                  setPreviewStateMode('NORMAL');
                  setRoute('welcome');
                }}
                className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-semibold text-white"
              >
                {strings.getStarted}
              </button>
            )}
            <button
              type="button"
              onClick={() => setPreviewStateMode('NORMAL')}
              className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-4 py-2 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC]"
            >
              {isArabic ? 'إنهاء المعاينة' : 'Exit Locked Preview'}
            </button>
          </div>
        </div>
      )}

      {(allowBypassPreview || previewStateMode === 'NORMAL') && children}
    </div>
  );
};

export const VerifiedIndicator: React.FC<{ verified: boolean; compact?: boolean }> = ({ verified, compact = false }) => {
  const { strings } = useSkillBridge();
  return (
    <span
      className={`inline-flex items-center gap-1 text-[11px] font-semibold whitespace-nowrap shrink-0 ${
        verified ? 'text-[#008CAE] dark:text-[#7EE8F5]' : 'text-[#4C637C] dark:text-[#93ADC8]'
      }`}
    >
      {verified ? <ShieldCheck className="w-3.5 h-3.5 text-[#00B4DB]" /> : <Sparkles className="w-3.5 h-3.5" />}
      {!compact && (verified ? strings.verifiedBadge : strings.selfClaimedBadge)}
    </span>
  );
};

export const ToastBanner: React.FC = () => {
  const { actionToast, clearActionToast } = useSkillBridge();
  if (!actionToast) return null;
  return (
    <div className="sb-route-enter fixed bottom-20 md:bottom-6 end-4 z-50 max-w-sm rounded-xl border border-[#00B4DB]/50 bg-[#0B1F38] text-white px-4 py-3 shadow-[0_18px_40px_-16px_rgba(0,180,219,0.5)] flex items-center gap-3">
      <CheckCircle2 className="w-4 h-4 text-[#A6E22E] shrink-0" />
      <p className="text-xs font-medium flex-1">{actionToast}</p>
      <button type="button" onClick={clearActionToast} className="text-xs text-[#93ADC8] hover:text-white shrink-0">
        ×
      </button>
    </div>
  );
};
