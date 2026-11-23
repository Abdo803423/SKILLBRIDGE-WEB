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
  const s = String(status ?? '').toUpperCase();
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
  const safeScore = Number.isFinite(score) ? score : 0;
  const clamped = Math.max(0, Math.min(100, Math.round(safeScore)));
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

const SB_BAR_COLORS = ['#00B4DB', '#0B1F38', '#008CAE', '#0284C7', '#38BDF8'];

export const SkillBarsChart: React.FC<{ data: SkillMetricDatum[] }> = ({ data }) => {
  const safeData = Array.isArray(data) ? data : [];
  const [hoveredIdx, setHoveredIdx] = React.useState<number | null>(null);

  if (safeData.length === 0) {
    return (
      <div className="h-56 w-full flex items-center justify-center text-xs text-[#4C637C] dark:text-[#93ADC8]">
        No skill metrics available yet.
      </div>
    );
  }

  return (
    <div className="min-h-56 w-full flex flex-col justify-center gap-3 py-1" role="list" aria-label="Skill readiness breakdown">
      {safeData.map((item, index) => {
        const clamped = Math.max(0, Math.min(100, Math.round(Number(item?.score ?? 0))));
        const color = SB_BAR_COLORS[index % SB_BAR_COLORS.length];
        const isHovered = hoveredIdx === index;

        return (
          <div
            key={`${item?.name ?? 'skill'}-${index}`}
            role="listitem"
            onMouseEnter={() => setHoveredIdx(index)}
            onMouseLeave={() => setHoveredIdx(null)}
            className="group space-y-1 rounded-lg px-2 py-1 transition-colors hover:bg-[#F0F6FA] dark:hover:bg-[#091A30]"
          >
            <div className="flex items-center justify-between gap-3 text-xs">
              <span className="font-medium text-[#4C637C] dark:text-[#93ADC8] truncate group-hover:text-[#0B1F38] dark:group-hover:text-[#F1F7FC] transition-colors">
                {item?.name ?? ''}
              </span>
              <span className="font-mono text-[11px] font-bold text-[#0B1F38] dark:text-[#7EE8F5] tabular-nums shrink-0">
                {clamped}%
              </span>
            </div>
            <div className="h-2.5 w-full rounded-full bg-[#E6EFF7] dark:bg-[#132D4F] overflow-hidden">
              <div
                className="h-full rounded-full transition-all duration-500 ease-out"
                style={{
                  width: `${clamped}%`,
                  backgroundColor: color,
                  opacity: hoveredIdx !== null && !isHovered ? 0.65 : 1,
                }}
              />
            </div>
          </div>
        );
      })}
    </div>
  );
};

export interface DonutSegmentDatum {
  name: string;
  value: number;
}

const SB_CHART_PALETTE = ['#00B4DB', '#0B1F38', '#0284C7', '#38BDF8', '#008CAE'];

export const DonutBreakdownChart: React.FC<{
  data: DonutSegmentDatum[];
  emptyLabel?: string;
}> = ({ data, emptyLabel = 'No data available yet.' }) => {
  const safeData = (Array.isArray(data) ? data : []).filter((d) => Number(d?.value) > 0);
  const [activeIndex, setActiveIndex] = React.useState<number | null>(null);
  const total = safeData.reduce((acc, d) => acc + Number(d.value || 0), 0);

  if (safeData.length === 0 || total <= 0) {
    return (
      <div className="h-48 w-full flex items-center justify-center text-xs text-[#4C637C] dark:text-[#93ADC8]">
        {emptyLabel}
      </div>
    );
  }

  const radius = 50;
  const circumference = 2 * Math.PI * radius;
  let runningOffset = 0;

  return (
    <div className="min-h-48 w-full flex flex-col sm:flex-row items-center justify-center gap-4 py-1">
      <div className="relative w-36 h-36 shrink-0">
        <svg viewBox="0 0 132 132" className="w-full h-full -rotate-90" role="img" aria-label="Distribution chart">
          <circle
            cx="66"
            cy="66"
            r={radius}
            fill="none"
            stroke="currentColor"
            strokeWidth="16"
            className="text-[#E6EFF7] dark:text-[#132D4F]"
          />
          {safeData.map((seg, idx) => {
            const pct = seg.value / total;
            const dash = Math.max(2, pct * circumference - 2);
            const dashOffset = -runningOffset;
            runningOffset += pct * circumference;
            const color = SB_CHART_PALETTE[idx % SB_CHART_PALETTE.length];
            const dimmed = activeIndex !== null && activeIndex !== idx;

            return (
              <circle
                key={`${seg.name}-${idx}`}
                cx="66"
                cy="66"
                r={radius}
                fill="none"
                stroke={color}
                strokeWidth={activeIndex === idx ? 19 : 16}
                strokeDasharray={`${dash} ${circumference}`}
                strokeDashoffset={dashOffset}
                onMouseEnter={() => setActiveIndex(idx)}
                onMouseLeave={() => setActiveIndex(null)}
                className="transition-all duration-300 cursor-pointer"
                style={{ opacity: dimmed ? 0.45 : 1 }}
              />
            );
          })}
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center pointer-events-none">
          <span className="font-display text-lg font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums">
            {activeIndex !== null && safeData[activeIndex] ? safeData[activeIndex].value : total}
          </span>
          <span className="text-[10px] font-semibold text-[#4C637C] dark:text-[#93ADC8] max-w-[74px] truncate">
            {activeIndex !== null && safeData[activeIndex] ? safeData[activeIndex].name : 'Total'}
          </span>
        </div>
      </div>

      <div className="flex flex-wrap sm:flex-col gap-1.5 justify-center min-w-0">
        {safeData.map((seg, idx) => {
          const color = SB_CHART_PALETTE[idx % SB_CHART_PALETTE.length];
          const pct = Math.round((seg.value / total) * 100);
          return (
            <button
              key={`${seg.name}-${idx}`}
              type="button"
              onMouseEnter={() => setActiveIndex(idx)}
              onMouseLeave={() => setActiveIndex(null)}
              onFocus={() => setActiveIndex(idx)}
              onBlur={() => setActiveIndex(null)}
              className="flex items-center gap-2 rounded-lg px-2 py-1 text-left text-[11px] hover:bg-[#F0F6FA] dark:hover:bg-[#091A30] transition"
            >
              <span
                className="w-2.5 h-2.5 rounded-full shrink-0"
                style={{ backgroundColor: color }}
              />
              <span className="font-medium text-[#0B1F38] dark:text-[#F1F7FC] truncate">{seg.name}</span>
              <span className="font-mono text-[10px] text-[#4C637C] dark:text-[#93ADC8] tabular-nums ms-auto">
                {seg.value} ({pct}%)
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
};

export interface VerticalColumnSeries {
  key: string;
  label: string;
  color: string;
}

export const VerticalColumnChart: React.FC<{
  data: Array<Record<string, string | number>>;
  xKey: string;
  series: VerticalColumnSeries[];
  stacked?: boolean;
  emptyLabel?: string;
}> = ({ data, xKey, series, stacked = false, emptyLabel = 'No metrics recorded yet.' }) => {
  const safeData = Array.isArray(data) ? data : [];
  const safeSeries = Array.isArray(series) ? series : [];
  const [hoverCol, setHoverCol] = React.useState<number | null>(null);

  if (safeData.length === 0 || safeSeries.length === 0) {
    return (
      <div className="h-48 w-full flex items-center justify-center text-xs text-[#4C637C] dark:text-[#93ADC8]">
        {emptyLabel}
      </div>
    );
  }

  const maxVal = Math.max(
    1,
    ...safeData.map((row) =>
      stacked
        ? safeSeries.reduce((sum, s) => sum + Math.max(0, Number(row?.[s.key] ?? 0)), 0)
        : Math.max(...safeSeries.map((s) => Math.max(0, Number(row?.[s.key] ?? 0)))),
    ),
  );

  return (
    <div className="min-h-48 w-full flex flex-col justify-between gap-2 pt-2">
      <div className="flex-1 flex items-end gap-2 sm:gap-3 min-h-[136px] border-b border-[#D5E0EC] dark:border-[#1C395E] pb-1 px-1">
        {safeData.map((row, idx) => {
          const xLabel = String(row?.[xKey] ?? '');
          const isHovered = hoverCol === idx;
          return (
            <div
              key={`${xLabel}-${idx}`}
              onMouseEnter={() => setHoverCol(idx)}
              onMouseLeave={() => setHoverCol(null)}
              className="flex-1 min-w-0 h-full flex flex-col items-center justify-end gap-1 group"
            >
              <div className="text-[10px] font-mono font-bold text-[#008CAE] dark:text-[#7EE8F5] tabular-nums h-4 flex items-center">
                {isHovered
                  ? safeSeries.map((s) => Number(row?.[s.key] ?? 0)).join('/')
                  : safeSeries.reduce((sum, s) => sum + Number(row?.[s.key] ?? 0), 0)}
              </div>
              <div
                className={`w-full max-w-[36px] h-[108px] flex ${
                  stacked ? 'flex-col-reverse justify-start' : 'items-end justify-center gap-1'
                }`}
              >
                {safeSeries.map((s) => {
                  const val = Math.max(0, Number(row?.[s.key] ?? 0));
                  const pct = Math.max(val > 0 ? 6 : 0, Math.round((val / maxVal) * 100));
                  return (
                    <div
                      key={s.key}
                      className={`w-full transition-all duration-300 ${
                        stacked ? 'first:rounded-b-sm last:rounded-t-md' : 'rounded-t-md'
                      }`}
                      style={{
                        height: `${pct}%`,
                        backgroundColor: s.color,
                        opacity: hoverCol !== null && !isHovered ? 0.6 : 1,
                      }}
                      title={`${xLabel} — ${s.label}: ${val}`}
                    />
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>

      <div className="flex items-center gap-2 sm:gap-3 px-1">
        {safeData.map((row, idx) => (
          <div key={`lbl-${idx}`} className="flex-1 min-w-0 text-center">
            <span className="block text-[10px] text-[#4C637C] dark:text-[#93ADC8] truncate">
              {String(row?.[xKey] ?? '')}
            </span>
          </div>
        ))}
      </div>

      {safeSeries.length > 1 && (
        <div className="flex flex-wrap items-center justify-center gap-3 pt-1">
          {safeSeries.map((s) => (
            <span key={s.key} className="inline-flex items-center gap-1.5 text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
              <span className="w-2.5 h-2.5 rounded-sm shrink-0" style={{ backgroundColor: s.color }} />
              {s.label}
            </span>
          ))}
        </div>
      )}
    </div>
  );
};

export interface TrendLineDatum {
  day: string;
  events: number;
  applications: number;
}

export const DualTrendLineChart: React.FC<{ data: TrendLineDatum[] }> = ({ data }) => {
  const safeData = Array.isArray(data) ? data : [];
  const [hoverIdx, setHoverIdx] = React.useState<number | null>(null);

  if (safeData.length === 0) {
    return (
      <div className="h-48 w-full flex items-center justify-center text-xs text-[#4C637C] dark:text-[#93ADC8]">
        No trend activity recorded yet.
      </div>
    );
  }

  const width = 320;
  const height = 148;
  const padX = 20;
  const padY = 16;
  const plotW = width - padX * 2;
  const plotH = height - padY * 2;
  const maxVal = Math.max(1, ...safeData.flatMap((d) => [Number(d.events || 0), Number(d.applications || 0)]));

  const getX = (i: number) =>
    safeData.length <= 1 ? padX + plotW / 2 : padX + (i / (safeData.length - 1)) * plotW;
  const getY = (val: number) => padY + plotH - (Math.max(0, val) / maxVal) * plotH;

  const eventsPoints = safeData.map((d, i) => `${getX(i).toFixed(1)},${getY(d.events).toFixed(1)}`).join(' ');
  const appsPoints = safeData.map((d, i) => `${getX(i).toFixed(1)},${getY(d.applications).toFixed(1)}`).join(' ');
  const activePoint = hoverIdx !== null ? safeData[hoverIdx] : safeData[safeData.length - 1];

  return (
    <div className="min-h-48 w-full flex flex-col justify-between gap-1.5">
      <div className="flex items-center justify-between text-[11px] text-[#4C637C] dark:text-[#93ADC8] px-1">
        <div className="flex items-center gap-3">
          <span className="inline-flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-[#00B4DB]" /> Events
          </span>
          <span className="inline-flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-[#0B1F38] dark:bg-[#7EE8F5]" /> Apps
          </span>
        </div>
        {activePoint && (
          <span className="font-mono text-[10px] tabular-nums text-[#0B1F38] dark:text-[#F1F7FC]">
            {activePoint.day}: {activePoint.events}ev / {activePoint.applications}ap
          </span>
        )}
      </div>

      <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-36 overflow-visible" role="img" aria-label="Activity and applications trend">
        {[0, 0.5, 1].map((t) => {
          const y = padY + t * plotH;
          return (
            <line
              key={t}
              x1={padX}
              x2={width - padX}
              y1={y}
              y2={y}
              stroke="currentColor"
              strokeDasharray="3 3"
              className="text-[#D5E0EC] dark:text-[#1C395E]"
            />
          );
        })}

        {safeData.length > 1 && (
          <>
            <polyline fill="none" stroke="#00B4DB" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" points={eventsPoints} />
            <polyline
              fill="none"
              stroke="currentColor"
              strokeWidth="2.2"
              strokeLinecap="round"
              strokeLinejoin="round"
              points={appsPoints}
              className="text-[#0B1F38] dark:text-[#7EE8F5]"
            />
          </>
        )}

        {safeData.map((d, idx) => {
          const cx = getX(idx);
          const cyEv = getY(d.events);
          const cyAp = getY(d.applications);
          const selected = hoverIdx === idx;
          return (
            <g
              key={`${d.day}-${idx}`}
              onMouseEnter={() => setHoverIdx(idx)}
              onMouseLeave={() => setHoverIdx(null)}
              className="cursor-pointer"
            >
              <circle cx={cx} cy={cyAp} r={selected ? 4.5 : 3} className="fill-[#0B1F38] dark:fill-[#7EE8F5]" />
              <circle cx={cx} cy={cyEv} r={selected ? 5 : 3.4} fill="#00B4DB" />
              <text
                x={cx}
                y={height - 2}
                textAnchor="middle"
                fontSize="9"
                className="fill-[#4C637C] dark:fill-[#93ADC8]"
              >
                {d.day}
              </text>
            </g>
          );
        })}
      </svg>
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
