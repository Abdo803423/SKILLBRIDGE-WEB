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

export const SkillBridgeLogo: React.FC<{ size?: 'sm' | 'md' | 'lg'; subtitle?: boolean }> = ({
  size = 'md',
  subtitle = false,
}) => {
  const dim = size === 'sm' ? 'w-8 h-8 rounded-lg' : size === 'lg' ? 'w-12 h-12 rounded-2xl' : 'w-9 h-9 rounded-xl';
  const titleCls =
    size === 'sm'
      ? 'text-base font-bold'
      : size === 'lg'
        ? 'text-2xl font-bold'
        : 'text-lg font-bold';

  return (
    <div className="flex items-center gap-2.5 shrink-0">
      <div className={`${dim} overflow-hidden bg-[#0B1F38] border border-[#00B4DB]/30 flex items-center justify-center shrink-0`}>
        <img
          src="/assets/images/skillbridge_app_icon_1794769666078.jpg"
          alt="SkillBridge"
          className="w-full h-full object-cover"
        />
      </div>
      <div className="leading-tight">
        <span className={`font-display tracking-tight text-[#0B1F38] dark:text-white block ${titleCls}`}>
          Skill<span className="text-[#00B4DB]">Bridge</span>
        </span>
        {subtitle && (
          <span className="block text-[10px] font-semibold tracking-wider uppercase text-slate-500 dark:text-slate-400">
            Web · Mobile · Desktop
          </span>
        )}
      </div>
    </div>
  );
};

export const StatusLabel: React.FC<{ status: string; className?: string }> = ({ status, className = '' }) => {
  const { language } = useSkillBridge();
  const s = status.toUpperCase();
  let tone = 'border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 bg-slate-100/70 dark:bg-slate-800/70';

  if (['VERIFIED', 'APPROVED', 'ACTIVE', 'ACCEPTED', 'DONE', 'LIVE', 'PAID', 'RESOLVED', 'TRUSTED'].includes(s)) {
    tone = 'border-emerald-300 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300 bg-emerald-50 dark:bg-emerald-950/50';
  } else if (['PENDING', 'PENDING_APPROVAL', 'PENDING_REVIEW', 'UNDER_REVIEW', 'IN_PROGRESS', 'SHORTLISTED', 'UPCOMING', 'APPLIED', 'BUSINESS'].includes(s)) {
    tone = 'border-sky-300 dark:border-sky-800 text-sky-700 dark:text-sky-300 bg-sky-50 dark:bg-sky-950/50';
  } else if (['REVISION_REQUESTED', 'DISPUTED', 'OPEN', 'BASIC'].includes(s)) {
    tone = 'border-amber-300 dark:border-amber-800 text-amber-700 dark:text-amber-300 bg-amber-50 dark:bg-amber-950/50';
  } else if (['REJECTED', 'SUSPENDED'].includes(s)) {
    tone = 'border-rose-300 dark:border-rose-800 text-rose-700 dark:text-rose-300 bg-rose-50 dark:bg-rose-950/50';
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
      <h2 className="font-display text-lg font-bold tracking-tight text-[#0B1F38] dark:text-white">{title}</h2>
      {subtitle && <p className="text-xs text-slate-600 dark:text-slate-400 mt-0.5">{subtitle}</p>}
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
    <div className="flex items-center gap-4 shrink-0">
      <div className="relative w-28 h-28 flex items-center justify-center">
        <svg className="w-28 h-28 -rotate-90" viewBox="0 0 100 100">
          <circle
            cx="50"
            cy="50"
            r={radius}
            fill="none"
            stroke="currentColor"
            strokeWidth="8"
            className="text-slate-200 dark:text-slate-800"
          />
          <circle
            cx="50"
            cy="50"
            r={radius}
            fill="none"
            stroke="#00B4DB"
            strokeWidth="8"
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            className="transition-all duration-500"
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
          <span className="font-display text-2xl font-bold text-[#0B1F38] dark:text-white tabular-nums">
            {clamped}%
          </span>
        </div>
      </div>
      <div className="min-w-0">
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">{label}</p>
        <p className="text-sm font-medium text-[#0B1F38] dark:text-slate-200 mt-1">
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
  const colors = ['#00B4DB', '#0B1F38', '#008CAE', '#558B00', '#0284C7'];
  return (
    <div className="h-56 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} layout="vertical" margin={{ top: 4, right: 16, left: 8, bottom: 4 }}>
          <XAxis type="number" domain={[0, 100]} hide />
          <YAxis
            type="category"
            dataKey="name"
            width={132}
            tick={{ fontSize: 11, fill: '#64748B' }}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip
            formatter={(val: number) => [`${val}%`, 'Score']}
            contentStyle={{
              borderRadius: '10px',
              border: '1px solid #D5E0EC',
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
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-8 flex flex-col items-center justify-center text-center gap-3">
          <RefreshCw className="w-7 h-7 text-[#00B4DB] animate-spin" />
          <p className="text-sm font-medium text-slate-700 dark:text-slate-300">{strings.loadingText}</p>
          <button
            type="button"
            onClick={() => setPreviewStateMode('NORMAL')}
            className="text-xs font-semibold text-[#008CAE] hover:underline"
          >
            {isArabic ? 'العودة للحالة الطبيعية' : 'Return to Normal State'}
          </button>
        </div>
      )}

      {!allowBypassPreview && previewStateMode === 'EMPTY' && (
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-8 flex flex-col items-center justify-center text-center gap-3">
          <Compass className="w-8 h-8 text-slate-400" />
          <h3 className="text-base font-bold text-[#0B1F38] dark:text-white">{strings.emptyTitle}</h3>
          <p className="text-xs text-slate-500 max-w-sm">
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
              className="rounded-xl bg-[#00B4DB] px-4 py-2 text-xs font-semibold text-white hover:bg-[#008CAE]"
            >
              {strings.emptyAction}
            </button>
            <button
              type="button"
              onClick={() => setPreviewStateMode('NORMAL')}
              className="rounded-xl border border-slate-300 dark:border-slate-700 px-4 py-2 text-xs font-semibold text-slate-700 dark:text-slate-200"
            >
              {isArabic ? 'استعادة' : 'Reset State'}
            </button>
          </div>
        </div>
      )}

      {!allowBypassPreview && previewStateMode === 'ERROR' && (
        <div className="rounded-2xl border border-rose-300 dark:border-rose-800 bg-rose-50 dark:bg-rose-950/30 p-8 flex flex-col items-center justify-center text-center gap-3">
          <AlertTriangle className="w-8 h-8 text-rose-600 dark:text-rose-400" />
          <h3 className="text-base font-bold text-rose-900 dark:text-rose-200">
            {isArabic ? 'تعذر مزامنة البيانات (معاينة)' : 'Synchronization Interrupted (Preview)'}
          </h3>
          <button
            type="button"
            onClick={() => setPreviewStateMode('NORMAL')}
            className="rounded-xl bg-rose-600 px-4 py-2 text-xs font-semibold text-white hover:bg-rose-700"
          >
            {strings.errorRetry}
          </button>
        </div>
      )}

      {!allowBypassPreview && previewStateMode === 'LOCKED' && (
        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-8 flex flex-col items-center justify-center text-center gap-3">
          <Lock className="w-8 h-8 text-amber-600" />
          <h3 className="text-base font-bold text-[#0B1F38] dark:text-white">{strings.lockedTitle}</h3>
          <p className="text-xs text-slate-600 dark:text-slate-400 max-w-md">{strings.guestLockedReason}</p>
          <div className="flex gap-2">
            {!session && !firebaseUser && (
              <button
                type="button"
                onClick={() => {
                  setPreviewStateMode('NORMAL');
                  setRoute('welcome');
                }}
                className="rounded-xl bg-[#00B4DB] px-4 py-2 text-xs font-semibold text-white"
              >
                {strings.getStarted}
              </button>
            )}
            <button
              type="button"
              onClick={() => setPreviewStateMode('NORMAL')}
              className="rounded-xl border border-slate-300 dark:border-slate-700 px-4 py-2 text-xs font-semibold text-slate-700 dark:text-slate-200"
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
        verified ? 'text-emerald-700 dark:text-emerald-400' : 'text-slate-500 dark:text-slate-400'
      }`}
    >
      {verified ? <ShieldCheck className="w-3.5 h-3.5" /> : <Sparkles className="w-3.5 h-3.5" />}
      {!compact && (verified ? strings.verifiedBadge : strings.selfClaimedBadge)}
    </span>
  );
};

export const ToastBanner: React.FC = () => {
  const { actionToast, clearActionToast } = useSkillBridge();
  if (!actionToast) return null;
  return (
    <div className="fixed bottom-20 md:bottom-6 end-4 z-50 max-w-sm rounded-xl border border-[#00B4DB]/40 bg-[#0B1F38] text-white px-4 py-3 shadow-lg flex items-center gap-3">
      <CheckCircle2 className="w-4 h-4 text-[#A6E22E] shrink-0" />
      <p className="text-xs font-medium flex-1">{actionToast}</p>
      <button type="button" onClick={clearActionToast} className="text-xs text-slate-300 hover:text-white shrink-0">
        ×
      </button>
    </div>
  );
};
