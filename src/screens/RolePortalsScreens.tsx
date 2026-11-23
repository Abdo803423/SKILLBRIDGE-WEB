import React, { useMemo, useState } from 'react';
import {
  CheckCircle2,
  Eye,
  Plus,
  Search,
  UserCheck,
  XCircle,
} from 'lucide-react';
import { AnimatedNumber, SpatialCard } from '../components/InteractiveMotion';
import {
  DonutBreakdownChart,
  DualTrendLineChart,
  SectionHeader,
  StateWrapper,
  StatusLabel,
  VerticalColumnChart,
} from '../components/SkillBridgeUi';
import { useSkillBridge } from '../context/SkillBridgeContext';
import type {
  AccountRole,
  ProjectApplicationEntity,
  ProjectBriefEntity,
  VerificationTier,
} from '../types/skillbridge';

// ==========================================
// 1. PLATFORM ADMIN PORTAL
// ==========================================
export const RoleAdminDashboardScreen: React.FC<{ activeSubRoute: string }> = ({ activeSubRoute }) => {
  const {
    snapshot,
    isArabic,
    strings,
    verifyCompany,
    reviewBrief,
    setUserActive,
    resolveReport,
    actionBusy,
    setRoute,
  } = useSkillBridge();

  const companies = Array.isArray(snapshot?.companies) ? snapshot.companies : [];
  const briefs = Array.isArray(snapshot?.briefs) ? snapshot.briefs : [];
  const accounts = Array.isArray(snapshot?.accounts) ? snapshot.accounts : [];
  const applications = Array.isArray(snapshot?.applications) ? snapshot.applications : [];
  const reports = Array.isArray(snapshot?.reports) ? snapshot.reports : [];
  const activities = Array.isArray(snapshot?.activities) ? snapshot.activities : [];

  const [tierByCompany, setTierByCompany] = useState<Record<string, VerificationTier>>({});
  const [adminNotesById, setAdminNotesById] = useState<Record<string, string>>({});
  const [briefFilter, setBriefFilter] = useState<'ALL' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED'>('ALL');
  const [rejectBriefModal, setRejectBriefModal] = useState<ProjectBriefEntity | null>(null);
  const [rejectBriefReason, setRejectBriefReason] = useState('');
  const [rejectCompanyModal, setRejectCompanyModal] = useState<string | null>(null);
  const [rejectCompanyReason, setRejectCompanyReason] = useState('');
  const [userSearch, setUserSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState<'ALL' | AccountRole>('ALL');
  const [reportFilter, setReportFilter] = useState<'ALL' | 'OPEN' | 'RESOLVED' | 'DISMISSED'>('ALL');

  const pendingCompanies = companies.filter((c) => c.verificationStatus === 'PENDING');
  const pendingBriefs = briefs.filter((b) => b.approvalStatus === 'PENDING_APPROVAL');
  const openReports = reports.filter((r) => r.status === 'OPEN');

  const filteredBriefs = useMemo(() => {
    if (briefFilter === 'ALL') return briefs;
    if (briefFilter === 'APPROVED') {
      return briefs.filter((b) => ['APPROVED', 'ACTIVE', 'COMPLETED'].includes(b.approvalStatus));
    }
    return briefs.filter((b) => b.approvalStatus === briefFilter);
  }, [briefs, briefFilter]);

  const filteredAccounts = useMemo(() => {
    const q = userSearch.trim().toLowerCase();
    return accounts.filter((a) => {
      if (roleFilter !== 'ALL' && a.role !== roleFilter) return false;
      if (!q) return true;
      return `${a.displayName} ${a.loginId} ${a.email} ${a.domain} ${a.country}`.toLowerCase().includes(q);
    });
  }, [accounts, userSearch, roleFilter]);

  const filteredReports = useMemo(() => {
    if (reportFilter === 'ALL') return reports;
    return reports.filter((r) => r.status === reportFilter);
  }, [reports, reportFilter]);

  const roleDistData = useMemo(() => {
    const counts: Record<string, number> = {
      STUDENT: 0,
      COMPANY: 0,
      COMPANY_DIRECTOR: 0,
      PLATFORM_ADMIN: 0,
    };
    for (const acc of accounts) {
      counts[acc.role] = (counts[acc.role] ?? 0) + 1;
    }
    return [
      { name: 'Students', value: counts.STUDENT },
      { name: 'Companies', value: counts.COMPANY },
      { name: 'Directors', value: counts.COMPANY_DIRECTOR },
      { name: 'Admins', value: counts.PLATFORM_ADMIN },
    ].filter((d) => d.value > 0);
  }, [accounts]);

  const briefPipelineData = useMemo(
    () => [
      { stage: 'Pending', count: briefs.filter((b) => b.approvalStatus === 'PENDING_APPROVAL').length },
      { stage: 'Approved', count: briefs.filter((b) => b.approvalStatus === 'APPROVED').length },
      { stage: 'Active', count: briefs.filter((b) => b.approvalStatus === 'ACTIVE').length },
      { stage: 'Completed', count: briefs.filter((b) => b.approvalStatus === 'COMPLETED').length },
      { stage: 'Rejected', count: briefs.filter((b) => b.approvalStatus === 'REJECTED').length },
    ],
    [briefs],
  );

  const activityTrendData = useMemo(() => {
    const buckets: Record<string, { day: string; events: number; applications: number }> = {};
    const fmt = (ms: number) =>
      new Date(ms).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
    for (const act of activities) {
      const key = fmt(act.createdAtEpochMs);
      if (!buckets[key]) buckets[key] = { day: key, events: 0, applications: 0 };
      buckets[key].events += 1;
    }
    for (const app of applications) {
      const key = fmt(app.appliedAtEpochMs);
      if (!buckets[key]) buckets[key] = { day: key, events: 0, applications: 0 };
      buckets[key].applications += 1;
    }
    return Object.values(buckets).slice(-7);
  }, [activities, applications]);

  return (
    <StateWrapper>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SectionHeader
            title={isArabic ? 'مركز قيادة مشرف المنصة' : 'Platform Admin Command Center'}
            subtitle={
              isArabic
                ? 'اعتماد الشركات، مراجعة مشاريع الشركات، إدارة الصلاحيات، ومراقبة السجلات الحية'
                : 'Central governance for company KYC, brief moderation, user safety, and real-time audit logs'
            }
          />
          <div className="flex flex-wrap gap-1.5">
            {(
              [
                { id: 'adminHome', en: 'Overview', ar: 'نظرة عامة' },
                { id: 'adminQueues', en: 'Queues', ar: 'الطوابير' },
                { id: 'adminUsers', en: 'Users', ar: 'المستخدمون' },
                { id: 'adminLogs', en: 'Reports & Logs', ar: 'البلاغات والسجل' },
              ] as const
            ).map((t) => (
              <button
                key={t.id}
                type="button"
                onClick={() => setRoute(t.id)}
                className={`rounded-xl px-3 py-1.5 text-xs font-bold whitespace-nowrap active:scale-95 transition ${
                  activeSubRoute === t.id
                    ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-white'
                    : 'border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] text-[#4C637C] dark:text-[#93ADC8] hover:border-[#00B4DB]'
                }`}
              >
                {isArabic ? t.ar : t.en}
              </button>
            ))}
          </div>
        </div>

        {/* Clickable KPI Cards */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
          <button
            type="button"
            onClick={() => setRoute('adminUsers')}
            className="text-start rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 hover:border-[#00B4DB] active:scale-[0.99] transition"
          >
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'الحسابات المسجلة' : 'Total Accounts'}</p>
            <p className="font-display text-2xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums mt-1">
              <AnimatedNumber value={accounts.length} />
            </p>
          </button>
          <button
            type="button"
            onClick={() => setRoute('adminQueues')}
            className="text-start rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 hover:border-[#00B4DB] active:scale-[0.99] transition"
          >
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'شركات تنتظر التوثيق' : 'Pending Company KYC'}</p>
            <p className="font-display text-2xl font-bold text-[#008CAE] dark:text-[#7EE8F5] tabular-nums mt-1">
              <AnimatedNumber value={pendingCompanies.length} />
            </p>
          </button>
          <button
            type="button"
            onClick={() => setRoute('adminQueues')}
            className="text-start rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 hover:border-[#00B4DB] active:scale-[0.99] transition"
          >
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'مشاريع تنتظر الاعتماد' : 'Pending Briefs'}</p>
            <p className="font-display text-2xl font-bold text-amber-600 tabular-nums mt-1">
              <AnimatedNumber value={pendingBriefs.length} />
            </p>
          </button>
          <button
            type="button"
            onClick={() => setRoute('adminLogs')}
            className="text-start rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 hover:border-[#00B4DB] active:scale-[0.99] transition"
          >
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'بلاغات مفتوحة' : 'Open Reports'}</p>
            <p className="font-display text-2xl font-bold text-rose-600 tabular-nums mt-1">
              <AnimatedNumber value={openReports.length} />
            </p>
          </button>
        </div>

        {/* Overview Analytics Charts (Line, Doughnut, Bar) */}
        {activeSubRoute === 'adminHome' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
            <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 space-y-2">
              <p className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {isArabic ? 'نشاط المنصة والتقديمات' : 'Activity & Applications Trend'}
              </p>
              <div className="min-h-48 w-full">
                <DualTrendLineChart data={activityTrendData} />
              </div>
            </div>

            <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 space-y-2">
              <p className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {isArabic ? 'توزيع الأدوار' : 'User Role Distribution'}
              </p>
              <div className="min-h-48 w-full">
                <DonutBreakdownChart
                  data={roleDistData}
                  emptyLabel={isArabic ? 'لا توجد حسابات بعد' : 'No accounts yet.'}
                />
              </div>
            </div>

            <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 space-y-2">
              <p className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {isArabic ? 'حالة مشاريع الشركات' : 'Project Brief Pipeline'}
              </p>
              <div className="min-h-48 w-full">
                <VerticalColumnChart
                  data={briefPipelineData}
                  xKey="stage"
                  series={[{ key: 'count', label: 'Briefs', color: '#00B4DB' }]}
                />
              </div>
            </div>
          </div>
        )}

        {/* Queues Section */}
        {(activeSubRoute === 'adminHome' || activeSubRoute === 'adminQueues') && (
          <div className="space-y-5">
            {/* Project Brief Approval Queue */}
            <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <SectionHeader
                  title={isArabic ? 'طابور اعتماد مشاريع الشركات' : 'Project Brief Approval Queue'}
                  subtitle={`${briefs.length} total briefs · ${pendingBriefs.length} pending`}
                />
                <div className="flex flex-wrap gap-1.5">
                  {(['ALL', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED'] as const).map((f) => (
                    <button
                      key={f}
                      type="button"
                      onClick={() => setBriefFilter(f)}
                      className={`rounded-lg px-2.5 py-1 text-[11px] font-semibold border whitespace-nowrap active:scale-95 transition ${
                        briefFilter === f
                          ? 'bg-[#0B1F38] text-white border-[#0B1F38] dark:bg-[#00B4DB] dark:text-white dark:border-[#00B4DB]'
                          : 'border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]'
                      }`}
                    >
                      {f.replace('_', ' ')}
                    </button>
                  ))}
                </div>
              </div>

              {filteredBriefs.map((brief) => (
                <div
                  key={brief.id}
                  className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-4 space-y-2.5 hover:border-[#00B4DB]/70 transition"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <p className="text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5]">
                        {brief.companyName} · {brief.domain} · {brief.level}
                      </p>
                      <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{brief.title}</h3>
                    </div>
                    <StatusLabel status={brief.approvalStatus} />
                  </div>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{brief.summary}</p>
                  <div className="text-[11px] text-[#4C637C] dark:text-[#93ADC8] space-y-1">
                    <p>
                      <span className="font-semibold">Rubric:</span> {brief.rubricCsv}
                    </p>
                    <p>
                      <span className="font-semibold">Deliverables:</span> {brief.deliverablesCsv}
                    </p>
                    {brief.adminNotes && (
                      <p className="text-[#0B1F38] dark:text-[#F1F7FC]">
                        <span className="font-semibold">Admin Note:</span> {brief.adminNotes}
                      </p>
                    )}
                  </div>
                  {brief.approvalStatus === 'PENDING_APPROVAL' && (
                    <div className="flex flex-wrap items-center gap-2 pt-2 border-t border-[#D5E0EC]/80 dark:border-[#1C395E]">
                      <input
                        type="text"
                        placeholder={isArabic ? 'ملاحظة الاعتماد...' : 'Approval / moderation notes...'}
                        value={adminNotesById[brief.id] ?? ''}
                        onChange={(e) => setAdminNotesById((p) => ({ ...p, [brief.id]: e.target.value }))}
                        className="flex-1 min-w-[180px] rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-1.5 text-xs focus:outline-none focus:border-[#00B4DB]"
                      />
                      <button
                        type="button"
                        disabled={actionBusy}
                        onClick={() =>
                          reviewBrief(
                            brief.id,
                            true,
                            adminNotesById[brief.id] || 'Verified rubric, scope & IP policy.',
                            brief.updatedAtEpochMs,
                          )
                        }
                        className="rounded-xl bg-emerald-600 hover:bg-emerald-700 px-3.5 py-1.5 text-xs font-bold text-white flex items-center gap-1 whitespace-nowrap"
                      >
                        <CheckCircle2 className="w-3.5 h-3.5" />
                        {isArabic ? 'اعتماد ونشر' : 'Approve & Publish'}
                      </button>
                      <button
                        type="button"
                        disabled={actionBusy}
                        onClick={() => {
                          setRejectBriefModal(brief);
                          setRejectBriefReason(adminNotesById[brief.id] || '');
                        }}
                        className="rounded-xl border border-rose-300 text-rose-600 px-3.5 py-1.5 text-xs font-bold flex items-center gap-1 whitespace-nowrap"
                      >
                        <XCircle className="w-3.5 h-3.5" />
                        {isArabic ? 'رفض مع سبب' : 'Reject'}
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </section>

            {/* Company KYC Verification */}
            <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
              <SectionHeader title={isArabic ? 'توثيق الشركات الشريكة' : 'Company KYC Verification'} />
              <div className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E]">
                {companies.map((c) => {
                  const selectedTier = tierByCompany[c.id] ?? (c.verificationTier === 'UNVERIFIED' ? 'BUSINESS' : c.verificationTier);
                  return (
                    <div key={c.id} className="py-3 flex flex-col lg:flex-row lg:items-center justify-between gap-3">
                      <div className="space-y-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{c.name}</span>
                          <StatusLabel status={c.verificationStatus} />
                          <StatusLabel status={c.verificationTier} />
                        </div>
                        <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">
                          {c.city} · {c.domain} · {c.rewardPolicy}
                        </p>
                        {c.verificationNotes && (
                          <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{c.verificationNotes}</p>
                        )}
                      </div>
                      <div className="flex flex-wrap items-center gap-2 shrink-0">
                        <select
                          value={selectedTier}
                          onChange={(e) =>
                            setTierByCompany((p) => ({ ...p, [c.id]: e.target.value as VerificationTier }))
                          }
                          className="rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-2 py-1.5 text-xs text-[#0B1F38] dark:text-[#F1F7FC]"
                        >
                          <option value="BASIC">BASIC</option>
                          <option value="BUSINESS">BUSINESS</option>
                          <option value="TRUSTED">TRUSTED</option>
                        </select>
                        <button
                          type="button"
                          disabled={actionBusy}
                          onClick={() =>
                            verifyCompany(c.id, true, selectedTier, 'Verified domain & commercial profile.')
                          }
                          className="sb-btn-interactive rounded-lg bg-[#00B4DB] hover:bg-[#008CAE] px-3 py-1.5 text-xs font-bold text-white whitespace-nowrap"
                        >
                          {isArabic ? 'توثيق' : 'Verify'}
                        </button>
                        <button
                          type="button"
                          disabled={actionBusy}
                          onClick={() => {
                            setRejectCompanyModal(c.id);
                            setRejectCompanyReason('');
                          }}
                          className="rounded-lg border border-rose-300 text-rose-600 px-3 py-1.5 text-xs font-semibold whitespace-nowrap"
                        >
                          {isArabic ? 'رفض' : 'Reject'}
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </section>
          </div>
        )}

        {/* Users Management */}
        {(activeSubRoute === 'adminUsers' || activeSubRoute === 'adminHome') && (
          <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <SectionHeader
                title={isArabic ? 'إدارة الحسابات والصلاحيات' : 'User Accounts & RBAC Governance'}
                subtitle={`${filteredAccounts.length} / ${accounts.length} accounts`}
              />
              <div className="relative w-full sm:w-64">
                <Search className="w-3.5 h-3.5 text-[#4C637C] dark:text-[#93ADC8] absolute start-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={userSearch}
                  onChange={(e) => setUserSearch(e.target.value)}
                  placeholder={isArabic ? 'ابحث بالاسم أو البريد...' : 'Search name, email, domain...'}
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] ps-8 pe-3 py-1.5 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
                />
              </div>
            </div>

            <div className="flex flex-wrap gap-1.5">
              {(['ALL', 'STUDENT', 'COMPANY', 'COMPANY_DIRECTOR', 'PLATFORM_ADMIN'] as const).map((r) => (
                <button
                  key={r}
                  type="button"
                  onClick={() => setRoleFilter(r)}
                  className={`rounded-lg px-2.5 py-1 text-[11px] font-semibold border whitespace-nowrap active:scale-95 transition ${
                    roleFilter === r
                      ? 'bg-[#0B1F38] text-white border-[#0B1F38] dark:bg-[#00B4DB] dark:text-white dark:border-[#00B4DB]'
                      : 'border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]'
                  }`}
                >
                  {r.replace('_', ' ')}
                </button>
              ))}
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead className="border-b border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]">
                  <tr>
                    <th className="text-start py-2 pe-3 font-semibold">User</th>
                    <th className="text-start py-2 pe-3 font-semibold">Role</th>
                    <th className="text-start py-2 pe-3 font-semibold">Level / Points</th>
                    <th className="text-start py-2 pe-3 font-semibold">Status</th>
                    <th className="text-end py-2 font-semibold">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E]">
                  {filteredAccounts.map((acc) => (
                    <tr key={acc.id}>
                      <td className="py-2.5 pe-3">
                        <span className="font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{acc.displayName}</span>
                        <span className="block text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
                          {acc.loginId} · {acc.email}
                        </span>
                      </td>
                      <td className="py-2.5 pe-3 font-semibold text-[#008CAE] dark:text-[#7EE8F5]">{acc.role}</td>
                      <td className="py-2.5 pe-3 tabular-nums text-[#0B1F38] dark:text-[#F1F7FC]">
                        <span className="font-medium">{acc.careerLevel}</span>
                        <span className="block text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
                          {acc.points} pts · {acc.verifiedCount} verified
                        </span>
                      </td>
                      <td className="py-2.5 pe-3">
                        <StatusLabel status={acc.isActive ? 'ACTIVE' : 'SUSPENDED'} />
                      </td>
                      <td className="py-2.5 text-end">
                        {acc.role !== 'PLATFORM_ADMIN' && (
                          <button
                            type="button"
                            disabled={actionBusy}
                            onClick={() => setUserActive(acc.id, !acc.isActive)}
                            className="rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] px-2.5 py-1 text-[11px] font-semibold hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] whitespace-nowrap"
                          >
                            {acc.isActive
                              ? isArabic
                                ? 'تعليق'
                                : 'Suspend'
                              : isArabic
                                ? 'تفعيل'
                                : 'Activate'}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {/* Reports & Activity Logs Split */}
        {(activeSubRoute === 'adminLogs' || activeSubRoute === 'adminHome') && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <SectionHeader title={isArabic ? 'تقارير السلامة والمراجعة' : 'Moderation Reports'} />
                <div className="flex gap-1">
                  {(['ALL', 'OPEN', 'RESOLVED', 'DISMISSED'] as const).map((rf) => (
                    <button
                      key={rf}
                      type="button"
                      onClick={() => setReportFilter(rf)}
                      className={`rounded-md px-2 py-0.5 text-[10px] font-semibold border ${
                        reportFilter === rf
                          ? 'bg-[#0B1F38] text-white border-[#0B1F38] dark:bg-[#00B4DB] dark:text-white dark:border-[#00B4DB]'
                          : 'border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]'
                      }`}
                    >
                      {rf}
                    </button>
                  ))}
                </div>
              </div>
              {filteredReports.length === 0 ? (
                <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">No moderation reports matching filter.</p>
              ) : (
                filteredReports.map((rep) => (
                  <div key={rep.id} className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-3 flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{rep.targetType}</span>
                        <StatusLabel status={rep.status} />
                      </div>
                      <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] mt-0.5">{rep.reason}</p>
                      {rep.resolutionNote && (
                        <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8] mt-0.5">Note: {rep.resolutionNote}</p>
                      )}
                    </div>
                    {rep.status === 'OPEN' && (
                      <div className="flex gap-1.5">
                        <button
                          type="button"
                          disabled={actionBusy}
                          onClick={() => resolveReport(rep.id, 'RESOLVED', 'Action taken by Platform Admin.')}
                          className="rounded-lg bg-emerald-600 px-3 py-1 text-xs font-bold text-white"
                        >
                          Resolve
                        </button>
                        <button
                          type="button"
                          disabled={actionBusy}
                          onClick={() => resolveReport(rep.id, 'DISMISSED', 'Reviewed — no policy breach.')}
                          className="rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] px-3 py-1 text-xs font-bold"
                        >
                          Dismiss
                        </button>
                      </div>
                    )}
                  </div>
                ))
              )}
            </section>

            <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
              <SectionHeader title={strings.auditLogTitle} />
              <div className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E] max-h-80 overflow-y-auto">
                {activities.map((act) => (
                  <div key={act.id} className="py-2.5 flex items-center justify-between gap-2 text-xs">
                    <div>
                      <p className="font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">{act.summary}</p>
                      <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
                        {act.actionType} · {act.actorRole}
                      </p>
                    </div>
                    <span className="text-[11px] text-[#4C637C] dark:text-[#93ADC8] tabular-nums shrink-0">
                      {new Date(act.createdAtEpochMs).toLocaleTimeString()}
                    </span>
                  </div>
                ))}
              </div>
            </section>
          </div>
        )}

        {/* Reject Brief Reason Modal */}
        {rejectBriefModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#061120]/65 backdrop-blur-sm p-4">
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                await reviewBrief(
                  rejectBriefModal.id,
                  false,
                  rejectBriefReason || 'Clarify acceptance rubric and deliverables.',
                  rejectBriefModal.updatedAtEpochMs,
                );
                setRejectBriefModal(null);
              }}
              className="sb-route-enter w-full max-w-md rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-5 space-y-3 shadow-2xl"
            >
              <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {isArabic ? 'رفض مسودة المشروع' : `Reject Brief: ${rejectBriefModal.title}`}
              </h3>
              <textarea
                required
                rows={3}
                value={rejectBriefReason}
                onChange={(e) => setRejectBriefReason(e.target.value)}
                placeholder="Provide clear moderation feedback for the company..."
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setRejectBriefModal(null)} className="px-3 py-1.5 text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8]">
                  {strings.cancelLabel}
                </button>
                <button type="submit" disabled={actionBusy} className="rounded-xl bg-rose-600 px-4 py-2 text-xs font-bold text-white">
                  {strings.confirmLabel}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Reject Company Reason Modal */}
        {rejectCompanyModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#061120]/65 backdrop-blur-sm p-4">
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                await verifyCompany(
                  rejectCompanyModal,
                  false,
                  'UNVERIFIED',
                  rejectCompanyReason || 'Resubmit commercial verification documents.',
                );
                setRejectCompanyModal(null);
              }}
              className="sb-route-enter w-full max-w-md rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-5 space-y-3 shadow-2xl"
            >
              <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {isArabic ? 'رفض توثيق الشركة' : 'Reject Company Verification'}
              </h3>
              <textarea
                required
                rows={3}
                value={rejectCompanyReason}
                onChange={(e) => setRejectCompanyReason(e.target.value)}
                placeholder="Specify missing KYC or compliance requirements..."
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setRejectCompanyModal(null)} className="px-3 py-1.5 text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8]">
                  {strings.cancelLabel}
                </button>
                <button type="submit" disabled={actionBusy} className="rounded-xl bg-rose-600 px-4 py-2 text-xs font-bold text-white">
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

// ==========================================
// 2. COMPANY PORTAL
// ==========================================
export const RoleCompanyDashboardScreen: React.FC<{ activeSubRoute: string }> = ({ activeSubRoute }) => {
  const {
    snapshot,
    isArabic,
    strings,
    createBrief,
    assignDirector,
    decideApplication,
    actionBusy,
    setRoute,
  } = useSkillBridge();

  const companiesList = Array.isArray(snapshot?.companies) ? snapshot.companies : [];
  const company = companiesList.find((c) => c.id === snapshot?.account?.companyId) ?? companiesList[0];
  const briefs = Array.isArray(snapshot?.briefs) ? snapshot.briefs : [];
  const applications = Array.isArray(snapshot?.applications) ? snapshot.applications : [];
  const allAccounts = Array.isArray(snapshot?.accounts) ? snapshot.accounts : [];
  const directors = useMemo(
    () => allAccounts.filter((a) => a.role === 'COMPANY_DIRECTOR' && a.companyId === company?.id),
    [allAccounts, company?.id],
  );
  const students = useMemo(() => allAccounts.filter((a) => a.role === 'STUDENT'), [allAccounts]);

  const [createModal, setCreateModal] = useState(false);
  const [title, setTitle] = useState('');
  const [summary, setSummary] = useState('');
  const [domain, setDomain] = useState('Backend & Cloud');
  const [level, setLevel] = useState('Mid-level');
  const [skillsCsv, setSkillsCsv] = useState('TypeScript, PostgreSQL, API Security');
  const [rubricCsv, setRubricCsv] = useState('Architecture & Reliability (50%), Automated Tests & Docs (50%)');
  const [deliverablesCsv, setDeliverablesCsv] = useState('Source Repository, Architecture Understanding Report');
  const [deadline, setDeadline] = useState('21 days');
  const [pointsReward, setPointsReward] = useState(320);
  const [prizePool, setPrizePool] = useState('$600 Grant');
  const [isTeamAllowed, setIsTeamAllowed] = useState(true);
  const [assignedDirectorId, setAssignedDirectorId] = useState(directors[0]?.id ?? '');

  const [selectedBriefId, setSelectedBriefId] = useState<string | null>(null);
  const [appSearch, setAppSearch] = useState('');
  const [appStatusFilter, setAppStatusFilter] = useState<'ALL' | 'APPLIED' | 'SHORTLISTED' | 'ACCEPTED' | 'REJECTED'>('ALL');
  const [selectedApplicant, setSelectedApplicant] = useState<ProjectApplicationEntity | null>(null);
  const [talentSearch, setTalentSearch] = useState('');

  const filteredApplications = useMemo(() => {
    const q = appSearch.trim().toLowerCase();
    return applications.filter((a) => {
      if (appStatusFilter !== 'ALL' && a.status !== appStatusFilter) return false;
      if (!q) return true;
      return `${a.studentName} ${a.roleRequested} ${a.studentSkillsCsv}`.toLowerCase().includes(q);
    });
  }, [applications, appSearch, appStatusFilter]);

  const filteredTalent = useMemo(() => {
    const q = talentSearch.trim().toLowerCase();
    return students.filter(
      (s) => !q || `${s.displayName} ${s.domain} ${s.country} ${s.skillsCsv}`.toLowerCase().includes(q),
    );
  }, [students, talentSearch]);

  const appPipelineData = useMemo(
    () =>
      [
        { name: 'Applied', value: applications.filter((a) => a.status === 'APPLIED').length },
        { name: 'Shortlisted', value: applications.filter((a) => a.status === 'SHORTLISTED').length },
        { name: 'Accepted', value: applications.filter((a) => a.status === 'ACCEPTED').length },
        { name: 'Rejected', value: applications.filter((a) => a.status === 'REJECTED').length },
      ].filter((d) => d.value > 0),
    [applications],
  );

  const chartData = useMemo(
    () =>
      briefs.map((b) => ({
        name: (b.title ?? '').slice(0, 18),
        applicants: applications.filter((a) => a.briefId === b.id).length,
      })),
    [briefs, applications],
  );

  return (
    <StateWrapper>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SectionHeader
            title={company?.name || strings.companyDashTitle}
            subtitle={
              isArabic
                ? 'إدارة المشاريع، تعيين مديري الإشراف، استكشاف المواهب وفرز المتقدمين لحظيًا'
                : 'Publish verified project briefs, assign Company Directors, explore verified talent & shortlist candidates'
            }
          />
          <button
            type="button"
            onClick={() => setCreateModal(true)}
            className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white flex items-center gap-1.5 whitespace-nowrap"
          >
            <Plus className="w-4 h-4" />
            {isArabic ? 'إنشاء مشروع جديد' : 'Create Project Brief'}
          </button>
        </div>

        {/* Sub-nav */}
        <div className="flex flex-wrap gap-1.5">
          {(
            [
              { id: 'companyHome', en: 'Overview', ar: 'نظرة عامة' },
              { id: 'companyBriefs', en: 'Briefs', ar: 'المشاريع' },
              { id: 'companyApplicants', en: 'Applicants', ar: 'المتقدمون' },
              { id: 'companyTalent', en: 'Verified Talent', ar: 'المواهب الموثّقة' },
              { id: 'companyAnalytics', en: 'Analytics', ar: 'التحليلات' },
            ] as const
          ).map((t) => (
            <button
              key={t.id}
              type="button"
              onClick={() => setRoute(t.id)}
              className={`rounded-xl px-3 py-1.5 text-xs font-bold whitespace-nowrap active:scale-95 transition ${
                activeSubRoute === t.id
                  ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-white'
                  : 'border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] text-[#4C637C] dark:text-[#93ADC8] hover:border-[#00B4DB]'
              }`}
            >
              {isArabic ? t.ar : t.en}
            </button>
          ))}
        </div>

        {/* Metrics */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'حالة توثيق الشركة' : 'Company Tier'}</p>
            <div className="mt-1.5">
              <StatusLabel status={company?.verificationTier ?? 'BUSINESS'} />
            </div>
          </SpatialCard>
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'إجمالي المشاريع' : 'Published Briefs'}</p>
            <p className="font-display text-2xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums mt-1">
              <AnimatedNumber value={briefs.length} />
            </p>
          </SpatialCard>
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'طلبات التقديم' : 'Total Applicants'}</p>
            <p className="font-display text-2xl font-bold text-[#008CAE] dark:text-[#7EE8F5] tabular-nums mt-1">
              <AnimatedNumber value={applications.length} />
            </p>
          </SpatialCard>
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'المواهب الموثّقة' : 'Verified Talent Pool'}</p>
            <p className="font-display text-2xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums mt-1">
              <AnimatedNumber value={students.length} />
            </p>
          </SpatialCard>
        </div>

        {/* Analytics Charts (Doughnut + Bar) */}
        {(activeSubRoute === 'companyAnalytics' || activeSubRoute === 'companyHome') && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-2">
              <SectionHeader title={isArabic ? 'مسار المتقدمين' : 'Application Pipeline Breakdown'} />
              <div className="min-h-56 w-full">
                <DonutBreakdownChart
                  data={appPipelineData}
                  emptyLabel="No applications submitted yet."
                />
              </div>
            </section>

            <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-2">
              <SectionHeader title={isArabic ? 'تحليلات التقديم حسب المشروع' : 'Applicant Engagement by Brief'} />
              <div className="min-h-56 w-full">
                <VerticalColumnChart
                  data={chartData}
                  xKey="name"
                  series={[{ key: 'applicants', label: 'Applicants', color: '#00B4DB' }]}
                  emptyLabel="No published briefs yet."
                />
              </div>
            </section>
          </div>
        )}

        {/* Company Briefs + Director Assignment + Detail Inspector */}
        {(activeSubRoute === 'companyHome' || activeSubRoute === 'companyBriefs') && (
          <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <SectionHeader title={isArabic ? 'مشاريع الشركة وتعيين المشرفين' : 'Published Briefs & Director Assignment'} />
            {briefs.map((b) => {
              const expanded = selectedBriefId === b.id;
              return (
                <div key={b.id} className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-4 space-y-2 hover:border-[#00B4DB]/70 transition">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{b.title}</h3>
                      <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">
                        {b.domain} · {b.level} · {b.prizePool} · +{b.pointsReward} pts
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <StatusLabel status={b.approvalStatus} />
                      <button
                        type="button"
                        onClick={() => setSelectedBriefId(expanded ? null : b.id)}
                        className="rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] px-2.5 py-1 text-[11px] font-semibold flex items-center gap-1 hover:border-[#00B4DB] active:scale-95 transition"
                      >
                        <Eye className="w-3 h-3" />
                        {expanded ? 'Hide Details' : 'Details'}
                      </button>
                    </div>
                  </div>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{b.summary}</p>
                  {expanded && (
                    <div className="rounded-xl bg-[#F0F6FA] dark:bg-[#09172B] p-3 text-xs space-y-1 text-[#0B1F38] dark:text-[#F1F7FC]">
                      <p>
                        <span className="font-semibold">Skills:</span> {b.skillsCsv}
                      </p>
                      <p>
                        <span className="font-semibold">Rubric:</span> {b.rubricCsv}
                      </p>
                      <p>
                        <span className="font-semibold">Deliverables:</span> {b.deliverablesCsv}
                      </p>
                      {b.adminNotes && (
                        <p>
                          <span className="font-semibold">Moderation Note:</span> {b.adminNotes}
                        </p>
                      )}
                    </div>
                  )}
                  <div className="flex flex-wrap items-center gap-2 pt-2 border-t border-[#D5E0EC]/80 dark:border-[#1C395E]">
                    <span className="text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8]">
                      {isArabic ? 'المدير المشرف:' : 'Assigned Director:'}
                    </span>
                    <select
                      value={b.assignedDirectorId}
                      disabled={actionBusy}
                      onChange={(e) => assignDirector(b.id, e.target.value, b.updatedAtEpochMs)}
                      className="rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-2.5 py-1 text-xs text-[#0B1F38] dark:text-[#F1F7FC]"
                    >
                      <option value="">{isArabic ? 'اختر مدير إشراف...' : 'Select Director...'}</option>
                      {directors.map((d) => (
                        <option key={d.id} value={d.id}>
                          {d.displayName} ({d.loginId})
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              );
            })}
          </section>
        )}

        {/* Applicants Pipeline with Search & Status Filter */}
        {(activeSubRoute === 'companyHome' || activeSubRoute === 'companyApplicants') && (
          <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <SectionHeader title={isArabic ? 'المتقدمون للمشاريع' : 'Candidate Applications Pipeline'} />
              <div className="relative w-full sm:w-60">
                <Search className="w-3.5 h-3.5 text-[#4C637C] dark:text-[#93ADC8] absolute start-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={appSearch}
                  onChange={(e) => setAppSearch(e.target.value)}
                  placeholder="Search candidates or skills..."
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] ps-8 pe-3 py-1.5 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
                />
              </div>
            </div>

            <div className="flex flex-wrap gap-1.5">
              {(['ALL', 'APPLIED', 'SHORTLISTED', 'ACCEPTED', 'REJECTED'] as const).map((st) => (
                <button
                  key={st}
                  type="button"
                  onClick={() => setAppStatusFilter(st)}
                  className={`rounded-lg px-2.5 py-1 text-[11px] font-semibold border whitespace-nowrap active:scale-95 transition ${
                    appStatusFilter === st
                      ? 'bg-[#0B1F38] text-white border-[#0B1F38] dark:bg-[#00B4DB] dark:text-white dark:border-[#00B4DB]'
                      : 'border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]'
                  }`}
                >
                  {st}
                </button>
              ))}
            </div>

            {filteredApplications.length === 0 ? (
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'لا توجد طلبات مطابقة.' : 'No matching applicants.'}</p>
            ) : (
              filteredApplications.map((app) => {
                const briefTitle = briefs.find((b) => b.id === app.briefId)?.title ?? app.briefId;
                return (
                  <div key={app.id} className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-4 space-y-2 hover:border-[#00B4DB]/70 transition">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div>
                        <p className="text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                          {app.studentName} · {app.studentLevel}
                        </p>
                        <p className="text-xs text-[#008CAE] dark:text-[#7EE8F5] font-semibold">
                          {briefTitle} · {app.roleRequested}
                        </p>
                      </div>
                      <div className="flex items-center gap-2">
                        <StatusLabel status={app.status} />
                        <button
                          type="button"
                          onClick={() => setSelectedApplicant(app)}
                          className="rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] px-2 py-1 text-[11px] font-semibold hover:border-[#00B4DB]"
                        >
                          Inspect
                        </button>
                      </div>
                    </div>
                    <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{app.coverNote}</p>
                    <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{app.studentSkillsCsv}</p>
                    <div className="flex flex-wrap gap-2 pt-1">
                      <button
                        type="button"
                        disabled={actionBusy || app.status === 'SHORTLISTED'}
                        onClick={() => decideApplication(app.id, 'SHORTLISTED', 'Shortlisted for final review.', app.updatedAtEpochMs)}
                        className="rounded-lg border border-[#00B4DB] px-3 py-1 text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5]"
                      >
                        Shortlist
                      </button>
                      <button
                        type="button"
                        disabled={actionBusy || app.status === 'ACCEPTED'}
                        onClick={() => decideApplication(app.id, 'ACCEPTED', 'Accepted! Kickoff milestone provisioned.', app.updatedAtEpochMs)}
                        className="rounded-lg bg-emerald-600 hover:bg-emerald-700 px-3 py-1 text-xs font-bold text-white"
                      >
                        Accept Student
                      </button>
                      <button
                        type="button"
                        disabled={actionBusy || app.status === 'REJECTED'}
                        onClick={() => decideApplication(app.id, 'REJECTED', 'Cohort filled for this sprint.', app.updatedAtEpochMs)}
                        className="rounded-lg border border-rose-300 px-3 py-1 text-xs font-semibold text-rose-600"
                      >
                        Reject
                      </button>
                    </div>
                  </div>
                );
              })
            )}
          </section>
        )}

        {/* Verified Student Talent Explorer */}
        {(activeSubRoute === 'companyTalent' || activeSubRoute === 'companyHome') && (
          <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <SectionHeader
                title={isArabic ? 'مستكشف المواهب الموثّقة' : 'Verified Student Talent Explorer'}
                subtitle={isArabic ? 'جميع الطلاب المسجلين والمتزامنين عبر المنصة' : 'Search verified learners by domain, country, level & evidence points'}
              />
              <div className="relative w-full sm:w-64">
                <Search className="w-3.5 h-3.5 text-[#4C637C] dark:text-[#93ADC8] absolute start-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={talentSearch}
                  onChange={(e) => setTalentSearch(e.target.value)}
                  placeholder="Search skill, student, country..."
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] ps-8 pe-3 py-1.5 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
                />
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {filteredTalent.map((st) => (
                <div key={st.id} className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-3.5 space-y-1.5 hover:border-[#00B4DB] transition">
                  <div className="flex items-center justify-between gap-2">
                    <div>
                      <p className="text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC] flex items-center gap-1.5">
                        <UserCheck className="w-3.5 h-3.5 text-[#00B4DB] dark:text-[#7EE8F5]" />
                        {st.displayName}
                      </p>
                      <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
                        {st.domain} · {st.country} · {st.careerLevel}
                      </p>
                    </div>
                    <div className="text-end tabular-nums">
                      <p className="text-xs font-bold text-[#008CAE] dark:text-[#7EE8F5]">{st.points} pts</p>
                      <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{st.verifiedCount} verified</p>
                    </div>
                  </div>
                  {st.skillsCsv && <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{st.skillsCsv}</p>}
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Candidate Detail Modal */}
        {selectedApplicant && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#061120]/65 backdrop-blur-sm p-4">
            <div className="sb-route-enter w-full max-w-md rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-5 space-y-3 shadow-2xl">
              <div className="flex items-center justify-between">
                <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{selectedApplicant.studentName}</h3>
                <StatusLabel status={selectedApplicant.status} />
              </div>
              <p className="text-xs text-[#008CAE] dark:text-[#7EE8F5] font-semibold">
                {selectedApplicant.roleRequested} · {selectedApplicant.studentLevel}
              </p>
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{selectedApplicant.coverNote}</p>
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">
                <span className="font-semibold">Skills:</span> {selectedApplicant.studentSkillsCsv}
              </p>
              {selectedApplicant.decisionNote && (
                <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">
                  <span className="font-semibold">Decision Note:</span> {selectedApplicant.decisionNote}
                </p>
              )}
              <div className="flex justify-end pt-2">
                <button
                  type="button"
                  onClick={() => setSelectedApplicant(null)}
                  className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] px-4 py-1.5 text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC]"
                >
                  {strings.closeLabel}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Create Brief Modal */}
        {createModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#061120]/65 backdrop-blur-sm p-4 overflow-y-auto">
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                await createBrief({
                  title,
                  summary,
                  domain,
                  level,
                  skills: skillsCsv.split(',').map((s) => s.trim()).filter(Boolean),
                  rubric: rubricCsv.split(',').map((s) => s.trim()).filter(Boolean),
                  deliverables: deliverablesCsv.split(',').map((s) => s.trim()).filter(Boolean),
                  deadline,
                  pointsReward,
                  prizePool,
                  assignedDirectorId: assignedDirectorId || directors[0]?.id || '',
                  isTeamAllowed,
                });
                setCreateModal(false);
                setTitle('');
                setSummary('');
              }}
              className="sb-route-enter w-full max-w-lg rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-5 space-y-3 shadow-2xl"
            >
              <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {isArabic ? 'إرسال مشروع للاعتماد الإداري' : 'Submit New Project Brief'}
              </h3>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Brief Title"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
              />
              <textarea
                required
                rows={3}
                value={summary}
                onChange={(e) => setSummary(e.target.value)}
                placeholder="Engineering objectives and acceptance criteria..."
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
              />
              <div className="grid grid-cols-2 gap-2.5">
                <input
                  type="text"
                  value={domain}
                  onChange={(e) => setDomain(e.target.value)}
                  placeholder="Domain"
                  className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
                />
                <select
                  value={level}
                  onChange={(e) => setLevel(e.target.value)}
                  className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs"
                >
                  <option value="Junior">Junior</option>
                  <option value="Mid-level">Mid-level</option>
                  <option value="Senior">Senior</option>
                  <option value="Director / Leadership">Director / Leadership</option>
                </select>
                <input
                  type="text"
                  value={prizePool}
                  onChange={(e) => setPrizePool(e.target.value)}
                  placeholder="Reward / Grant"
                  className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
                />
                <input
                  type="number"
                  value={pointsReward}
                  onChange={(e) => setPointsReward(Number(e.target.value) || 250)}
                  placeholder="Points"
                  className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
                />
              </div>
              <div className="grid grid-cols-2 gap-2.5">
                <input
                  type="text"
                  value={deadline}
                  onChange={(e) => setDeadline(e.target.value)}
                  placeholder="Deadline"
                  className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
                />
                <label className="flex items-center gap-2 text-xs font-semibold px-2">
                  <input
                    type="checkbox"
                    checked={isTeamAllowed}
                    onChange={(e) => setIsTeamAllowed(e.target.checked)}
                    className="h-4 w-4 accent-[#00B4DB]"
                  />
                  {isArabic ? 'يسمح بعمل الفرق' : 'Allow Team Submissions'}
                </label>
              </div>
              <input
                type="text"
                value={skillsCsv}
                onChange={(e) => setSkillsCsv(e.target.value)}
                placeholder="Skills (comma-separated)"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
              />
              <input
                type="text"
                value={rubricCsv}
                onChange={(e) => setRubricCsv(e.target.value)}
                placeholder="Evaluation Rubric items (comma-separated)"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
              />
              <input
                type="text"
                value={deliverablesCsv}
                onChange={(e) => setDeliverablesCsv(e.target.value)}
                placeholder="Deliverables checklist (comma-separated)"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs focus:outline-none focus:border-[#00B4DB]"
              />
              <select
                value={assignedDirectorId}
                onChange={(e) => setAssignedDirectorId(e.target.value)}
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs"
              >
                {directors.map((d) => (
                  <option key={d.id} value={d.id}>
                    Assign Director: {d.displayName}
                  </option>
                ))}
              </select>
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setCreateModal(false)} className="px-3 py-1.5 text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8]">
                  {strings.cancelLabel}
                </button>
                <button type="submit" disabled={actionBusy} className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white">
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

// ==========================================
// 3. COMPANY DIRECTOR PORTAL (ISOLATED SCOPE)
// ==========================================
export const RoleDirectorDashboardScreen: React.FC<{ activeSubRoute: string }> = ({ activeSubRoute }) => {
  const {
    snapshot,
    isArabic,
    strings,
    createProjectTask,
    evaluateSubmission,
    completeBrief,
    actionBusy,
    setRoute,
  } = useSkillBridge();

  const briefs = Array.isArray(snapshot?.briefs) ? snapshot.briefs : [];
  const applications = Array.isArray(snapshot?.applications) ? snapshot.applications : [];
  const tasks = Array.isArray(snapshot?.tasks) ? snapshot.tasks : [];
  const submissions = Array.isArray(snapshot?.submissions) ? snapshot.submissions : [];

  const [taskModalBriefId, setTaskModalBriefId] = useState<string | null>(null);
  const [selectedStudentId, setSelectedStudentId] = useState<string>('');
  const [taskTitle, setTaskTitle] = useState('');
  const [taskDue, setTaskDue] = useState('In 4 days');

  const [scoresBySub, setScoresBySub] = useState<Record<string, number>>({});
  const [feedbackBySub, setFeedbackBySub] = useState<Record<string, string>>({});
  const [rejectSubTarget, setRejectSubTarget] = useState<string | null>(null);
  const [rejectSubMode, setRejectSubMode] = useState<'REVISION_REQUESTED' | 'REJECTED'>('REJECTED');
  const [rejectSubReason, setRejectSubReason] = useState('');

  const avgTurnaroundHours = useMemo(() => {
    const evaluated = submissions.filter((s) => s.evaluatedAtEpochMs > s.submittedAtEpochMs);
    if (evaluated.length === 0) return 0;
    const totalMs = evaluated.reduce((acc, s) => acc + (s.evaluatedAtEpochMs - s.submittedAtEpochMs), 0);
    return Math.max(1, Math.round(totalMs / evaluated.length / 3_600_000));
  }, [submissions]);

  const reviewBreakdownData = useMemo(
    () =>
      [
        { name: 'Pending', value: submissions.filter((s) => s.status === 'PENDING_REVIEW').length },
        { name: 'Verified', value: submissions.filter((s) => s.status === 'VERIFIED').length },
        { name: 'Revision', value: submissions.filter((s) => s.status === 'REVISION_REQUESTED').length },
        { name: 'Rejected', value: submissions.filter((s) => s.status === 'REJECTED').length },
      ].filter((d) => d.value > 0),
    [submissions],
  );

  const briefProgressData = useMemo(
    () =>
      briefs.map((b) => {
        const bTasks = tasks.filter((t) => t.briefId === b.id);
        const done = bTasks.filter((t) => t.status === 'DONE').length;
        return {
          name: (b.title ?? '').slice(0, 16),
          done,
          remaining: Math.max(0, bTasks.length - done),
        };
      }),
    [briefs, tasks],
  );

  const evaluatedSubmissions = useMemo(
    () => submissions.filter((s) => s.status !== 'PENDING_REVIEW'),
    [submissions],
  );

  const taskModalStudents = useMemo(
    () => applications.filter((a) => a.briefId === taskModalBriefId && a.status === 'ACCEPTED'),
    [applications, taskModalBriefId],
  );

  return (
    <StateWrapper>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SectionHeader
            title={isArabic ? 'استوديو مدير الإشراف' : 'Company Director Execution Studio'}
            subtitle={
              isArabic
                ? 'نطاق معزول حصريًا للمشاريع المسندة لك — إنشاء المهام وتقييم التسليمات'
                : 'Strictly scoped to your assigned company briefs — manage milestone tasks & grade deliverables'
            }
          />
          <div className="flex flex-wrap gap-1.5">
            {(
              [
                { id: 'directorHome', en: 'Overview', ar: 'نظرة عامة' },
                { id: 'directorProjects', en: 'Briefs', ar: 'المشاريع' },
                { id: 'directorStudents', en: 'Tasks', ar: 'المهام' },
                { id: 'directorReviews', en: 'Rubric Reviews', ar: 'التقييم' },
                { id: 'directorHistory', en: 'Evaluations History', ar: 'السجل' },
              ] as const
            ).map((t) => (
              <button
                key={t.id}
                type="button"
                onClick={() => setRoute(t.id)}
                className={`rounded-xl px-3 py-1.5 text-xs font-bold whitespace-nowrap active:scale-95 transition ${
                  activeSubRoute === t.id
                    ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-white'
                    : 'border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] text-[#4C637C] dark:text-[#93ADC8] hover:border-[#00B4DB]'
                }`}
              >
                {isArabic ? t.ar : t.en}
              </button>
            ))}
          </div>
        </div>

        {/* KPI Cards including Avg Turnaround */}
        <div className="grid grid-cols-2 lg:grid-cols-5 gap-3">
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'المشاريع المسندة' : 'Assigned Briefs'}</p>
            <p className="font-display text-2xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums mt-1">
              <AnimatedNumber value={briefs.length} />
            </p>
          </SpatialCard>
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'الطلاب المقبولون' : 'Accepted Students'}</p>
            <p className="font-display text-2xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums mt-1">
              <AnimatedNumber value={applications.filter((a) => a.status === 'ACCEPTED').length} />
            </p>
          </SpatialCard>
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'مهام الطلاب' : 'Tracked Tasks'}</p>
            <p className="font-display text-2xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums mt-1">
              <AnimatedNumber value={tasks.length} />
            </p>
          </SpatialCard>
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'بانتظار التقييم' : 'Pending Reviews'}</p>
            <p className="font-display text-2xl font-bold text-[#008CAE] dark:text-[#7EE8F5] tabular-nums mt-1">
              <AnimatedNumber value={submissions.filter((s) => s.status === 'PENDING_REVIEW').length} />
            </p>
          </SpatialCard>
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'متوسط سرعة التقييم' : 'Avg Turnaround'}</p>
            <p className="font-display text-2xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums mt-1">
              <AnimatedNumber value={avgTurnaroundHours} suffix="h" />
            </p>
          </SpatialCard>
        </div>

        {/* Director Overview Charts */}
        {activeSubRoute === 'directorHome' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-2">
              <SectionHeader title={isArabic ? 'توزيع حالات التقييم' : 'Submission Review Breakdown'} />
              <div className="min-h-52 w-full">
                <DonutBreakdownChart
                  data={reviewBreakdownData}
                  emptyLabel="No submissions yet."
                />
              </div>
            </section>

            <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-2">
              <SectionHeader title={isArabic ? 'تقدم المهام حسب المشروع' : 'Milestone Progress by Brief'} />
              <div className="min-h-52 w-full">
                <VerticalColumnChart
                  data={briefProgressData}
                  xKey="name"
                  stacked
                  series={[
                    { key: 'done', label: 'Completed', color: '#00B4DB' },
                    { key: 'remaining', label: 'Open', color: '#0B1F38' },
                  ]}
                  emptyLabel="No supervised briefs yet."
                />
              </div>
            </section>
          </div>
        )}

        {/* Supervised Briefs */}
        {(activeSubRoute === 'directorHome' || activeSubRoute === 'directorProjects') && (
          <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <SectionHeader title={isArabic ? 'المشاريع تحت إشرافك' : 'Your Supervised Briefs'} />
            {briefs.map((b) => {
              const accepted = applications.filter((a) => a.briefId === b.id && a.status === 'ACCEPTED');
              const bTasks = tasks.filter((t) => t.briefId === b.id);
              const doneTasks = bTasks.filter((t) => t.status === 'DONE').length;
              const pct = bTasks.length > 0 ? Math.round((doneTasks / bTasks.length) * 100) : 0;
              return (
                <div key={b.id} className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-4 space-y-2.5 hover:border-[#00B4DB]/70 transition">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <p className="text-xs font-bold text-[#008CAE] dark:text-[#7EE8F5]">{b.companyName}</p>
                      <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{b.title}</h3>
                    </div>
                    <StatusLabel status={b.approvalStatus} />
                  </div>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{b.summary}</p>
                  <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
                    <span className="font-semibold">Rubric:</span> {b.rubricCsv}
                  </p>
                  <div className="space-y-1">
                    <div className="flex justify-between text-[11px] text-[#4C637C] dark:text-[#93ADC8] tabular-nums">
                      <span>Milestone Progress ({doneTasks}/{bTasks.length})</span>
                      <span>{pct}%</span>
                    </div>
                    <div className="h-1.5 rounded-full bg-[#D5E0EC] dark:bg-[#1C395E] overflow-hidden">
                      <div className="h-full bg-[#00B4DB] transition-all duration-500" style={{ width: `${pct}%` }} />
                    </div>
                  </div>
                  <div className="flex flex-wrap gap-2 pt-1">
                    {accepted.length > 0 && (
                      <button
                        type="button"
                        onClick={() => {
                          setTaskModalBriefId(b.id);
                          setSelectedStudentId(accepted[0]?.studentAccountId ?? '');
                        }}
                        className="sb-btn-interactive rounded-lg bg-[#00B4DB] hover:bg-[#008CAE] px-3 py-1.5 text-xs font-bold text-white"
                      >
                        + {isArabic ? 'إسناد مهمة للطالب' : 'Assign Milestone Task'}
                      </button>
                    )}
                    {b.approvalStatus !== 'COMPLETED' && (
                      <button
                        type="button"
                        disabled={actionBusy}
                        onClick={() => completeBrief(b.id)}
                        className="rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] px-3 py-1.5 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB]"
                      >
                        {isArabic ? 'إنهاء المشروع' : 'Mark Completed'}
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </section>
        )}

        {/* Rubric Reviews */}
        {(activeSubRoute === 'directorHome' || activeSubRoute === 'directorReviews') && (
          <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <SectionHeader title={isArabic ? 'استوديو مراجعة وتوثيق التسليمات' : 'Submission Rubric Review Studio'} />
            {submissions.length === 0 ? (
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">
                {isArabic ? 'لا توجد تسليمات للطلاب في مشاريعك بعد.' : 'No student deliverables submitted yet.'}
              </p>
            ) : (
              submissions.map((sub) => {
                const score = scoresBySub[sub.id] ?? (sub.score > 0 ? sub.score : 92);
                const feedback = feedbackBySub[sub.id] ?? sub.feedback;
                const parentBrief = briefs.find((b) => b.id === sub.briefId);
                return (
                  <div key={sub.id} className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-4 space-y-3 hover:border-[#00B4DB]/70 transition">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div>
                        <p className="text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                          {sub.studentName} — {sub.briefTitle}
                        </p>
                        <p className="text-xs text-[#008CAE] dark:text-[#7EE8F5] break-all">{sub.repoOrDemoUrl}</p>
                      </div>
                      <StatusLabel status={sub.status} />
                    </div>
                    {parentBrief && (
                      <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8] bg-[#F0F6FA] dark:bg-[#09172B] rounded-lg px-2.5 py-1.5">
                        <span className="font-semibold">Evaluation Rubric:</span> {parentBrief.rubricCsv}
                      </p>
                    )}
                    <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{sub.understandingReport}</p>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-3 items-center pt-2 border-t border-[#D5E0EC]/80 dark:border-[#1C395E]">
                      <div>
                        <label className="block text-[11px] font-semibold mb-1">
                          Rubric Score: <span className="font-bold text-[#008CAE] dark:text-[#7EE8F5] tabular-nums">{score}%</span>
                        </label>
                        <input
                          type="range"
                          min={40}
                          max={100}
                          value={score}
                          onChange={(e) => setScoresBySub((p) => ({ ...p, [sub.id]: Number(e.target.value) }))}
                          className="w-full accent-[#00B4DB]"
                        />
                      </div>
                      <div className="md:col-span-2">
                        <input
                          type="text"
                          value={feedback}
                          onChange={(e) => setFeedbackBySub((p) => ({ ...p, [sub.id]: e.target.value }))}
                          placeholder={isArabic ? 'ملاحظات المقيّم المهنية...' : 'Mentor evaluation feedback...'}
                          className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
                        />
                      </div>
                    </div>

                    <div className="flex flex-wrap gap-2">
                      <button
                        type="button"
                        disabled={actionBusy}
                        onClick={() =>
                          evaluateSubmission(sub.id, {
                            score,
                            feedback: feedback || 'Strong technical architecture & verified deliverables.',
                            decision: 'VERIFIED',
                            rejectionReason: '',
                            expectedEvaluatedAtEpochMs: sub.evaluatedAtEpochMs,
                          })
                        }
                        className="rounded-lg bg-emerald-600 hover:bg-emerald-700 px-3.5 py-1.5 text-xs font-bold text-white"
                      >
                        Verify & Award Points
                      </button>
                      <button
                        type="button"
                        disabled={actionBusy}
                        onClick={() => {
                          setRejectSubTarget(sub.id);
                          setRejectSubMode('REVISION_REQUESTED');
                          setRejectSubReason(feedback || '');
                        }}
                        className="rounded-lg border border-amber-400 text-amber-700 dark:text-amber-300 px-3.5 py-1.5 text-xs font-bold"
                      >
                        Request Revision
                      </button>
                      <button
                        type="button"
                        disabled={actionBusy}
                        onClick={() => {
                          setRejectSubTarget(sub.id);
                          setRejectSubMode('REJECTED');
                          setRejectSubReason('');
                        }}
                        className="rounded-lg border border-rose-300 text-rose-600 px-3.5 py-1.5 text-xs font-bold"
                      >
                        Reject Submission
                      </button>
                    </div>
                  </div>
                );
              })
            )}
          </section>
        )}

        {/* Tracked Student Tasks */}
        {(activeSubRoute === 'directorStudents' || activeSubRoute === 'directorHome') && (
          <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <SectionHeader title={isArabic ? 'متابعة مهام الطلاب' : 'Student Task Progress'} />
            <div className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E]">
              {tasks.map((t) => (
                <div key={t.id} className="py-2.5 flex items-center justify-between gap-2 text-xs">
                  <div>
                    <p className="font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">{t.title}</p>
                    <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
                      {t.assignedStudentName} · {t.dueDateLabel}
                    </p>
                  </div>
                  <StatusLabel status={t.status} />
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Evaluations History */}
        {(activeSubRoute === 'directorHistory' || activeSubRoute === 'directorHome') && (
          <section className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <SectionHeader
              title={isArabic ? 'سجل التقييمات والأكواد الموثّقة' : 'Evaluations History & Verification Codes'}
            />
            {evaluatedSubmissions.length === 0 ? (
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">No evaluated deliverables yet.</p>
            ) : (
              <div className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E]">
                {evaluatedSubmissions.map((ev) => (
                  <div key={ev.id} className="py-3 flex flex-wrap items-center justify-between gap-2 text-xs">
                    <div>
                      <p className="font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                        {ev.studentName} — {ev.briefTitle}
                      </p>
                      <p className="text-[#4C637C] dark:text-[#93ADC8] mt-0.5">{ev.feedback || ev.rejectionReason}</p>
                      <p className="text-[11px] font-mono text-[#008CAE] dark:text-[#7EE8F5] mt-0.5">
                        Verification Code: {ev.verificationCode}
                      </p>
                    </div>
                    <div className="text-end tabular-nums space-y-1">
                      <StatusLabel status={ev.status} />
                      <p className="font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                        {ev.score}% · +{ev.pointsAwarded} pts
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {/* Create Task Modal (with Student Selector) */}
        {taskModalBriefId && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#061120]/65 backdrop-blur-sm p-4">
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                const targetId = selectedStudentId || taskModalStudents[0]?.studentAccountId;
                if (!targetId) return;
                await createProjectTask({
                  briefId: taskModalBriefId,
                  studentAccountId: targetId,
                  title: taskTitle,
                  dueDateLabel: taskDue,
                });
                setTaskModalBriefId(null);
                setTaskTitle('');
              }}
              className="sb-route-enter w-full max-w-md rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-5 space-y-3 shadow-2xl"
            >
              <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {isArabic ? 'إسناد مهمة متابعة جديدة' : 'Assign Student Milestone Task'}
              </h3>
              <div>
                <label className="block text-xs font-semibold mb-1">
                  {isArabic ? 'الطالب المقبول' : 'Select Accepted Student'}
                </label>
                <select
                  value={selectedStudentId || taskModalStudents[0]?.studentAccountId || ''}
                  onChange={(e) => setSelectedStudentId(e.target.value)}
                  className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC]"
                >
                  {taskModalStudents.map((st) => (
                    <option key={st.studentAccountId} value={st.studentAccountId}>
                      {st.studentName} ({st.roleRequested})
                    </option>
                  ))}
                </select>
              </div>
              <input
                type="text"
                required
                value={taskTitle}
                onChange={(e) => setTaskTitle(e.target.value)}
                placeholder="e.g. Implement integration tests & telemetry endpoint"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <input
                type="text"
                required
                value={taskDue}
                onChange={(e) => setTaskDue(e.target.value)}
                placeholder="Due label (e.g. In 5 days)"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setTaskModalBriefId(null)} className="px-3 py-1.5 text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8]">
                  {strings.cancelLabel}
                </button>
                <button type="submit" disabled={actionBusy} className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white">
                  {strings.confirmLabel}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Revision / Rejection Reason Modal */}
        {rejectSubTarget && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#061120]/65 backdrop-blur-sm p-4">
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                const targetSub = submissions.find((s) => s.id === rejectSubTarget);
                if (!targetSub) return;
                const currentScore = scoresBySub[targetSub.id] ?? (targetSub.score > 0 ? targetSub.score : 68);
                await evaluateSubmission(targetSub.id, {
                  score: rejectSubMode === 'REJECTED' ? Math.min(currentScore, 60) : currentScore,
                  feedback: rejectSubReason,
                  decision: rejectSubMode,
                  rejectionReason: rejectSubReason,
                  expectedEvaluatedAtEpochMs: targetSub.evaluatedAtEpochMs,
                });
                setRejectSubTarget(null);
              }}
              className="sb-route-enter w-full max-w-md rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-5 space-y-3 shadow-2xl"
            >
              <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {rejectSubMode === 'REJECTED' ? 'Reject Deliverable' : 'Request Rubric Revision'}
              </h3>
              <textarea
                required
                rows={3}
                value={rejectSubReason}
                onChange={(e) => setRejectSubReason(e.target.value)}
                placeholder="Explain required rubric fixes or technical gaps..."
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setRejectSubTarget(null)} className="px-3 py-1.5 text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8]">
                  {strings.cancelLabel}
                </button>
                <button
                  type="submit"
                  disabled={actionBusy}
                  className={`rounded-xl px-4 py-2 text-xs font-bold text-white ${
                    rejectSubMode === 'REJECTED' ? 'bg-rose-600' : 'bg-amber-600'
                  }`}
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
