import React, { useMemo, useState } from 'react';
import {
  ArrowLeft,
  ArrowRight,
  Award,
  CheckCircle2,
  Coins,
  Flag,
  Lock,
  MessageCircle,
  Plus,
  Share2,
  ShieldCheck,
  ThumbsUp,
  Trophy,
  Users,
} from 'lucide-react';
import { SectionHeader, StateWrapper, StatusLabel, VerifiedIndicator } from '../components/SkillBridgeUi';
import { SEED_COMMENTS, useSkillBridge } from '../context/SkillBridgeContext';
import { EVIDENCE_NODES, LEADERBOARD_ENTRIES } from '../data/ecosystemData';
import { lText, localizedDomain } from '../i18n/localization';
import { evaluateBadgeProgress } from '../services/pointsAndBadgesEngine';

export const CommunityFeedScreen: React.FC = () => {
  const {
    strings,
    isArabic,
    communityPosts,
    setSelectedPostId,
    setRoute,
    createCommunityPost,
    showToast,
  } = useSkillBridge();

  const [sortBy, setSortBy] = useState<'TRENDING' | 'NEW' | 'TOP' | 'UNANSWERED'>('TRENDING');
  const [composerOpen, setComposerOpen] = useState(false);
  const [postType, setPostType] = useState('Technical Question');
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [tagsCsv, setTagsCsv] = useState('Backend, Architecture');

  const sorted = useMemo(() => {
    const arr = [...communityPosts];
    if (sortBy === 'NEW') return arr.sort((a, b) => b.createdAtMs - a.createdAtMs);
    if (sortBy === 'TOP' || sortBy === 'TRENDING') return arr.sort((a, b) => b.helpfulCount - a.helpfulCount);
    if (sortBy === 'UNANSWERED') return arr.filter((p) => !p.acceptedCommentId);
    return arr;
  }, [communityPosts, sortBy]);

  const handlePublish = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createCommunityPost({
        postType,
        title,
        body,
        tags: tagsCsv.split(',').map((t) => t.trim()).filter(Boolean),
      });
      setTitle('');
      setBody('');
      setComposerOpen(false);
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Unable to post');
    }
  };

  return (
    <StateWrapper>
      <div className="space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SectionHeader
            title={strings.communityTitle}
            subtitle={isArabic ? 'مناقشات تقنية بإجابات معتمدة وأدلة موثّقة' : 'Technical Q&A with Verified Skill distinction and Best Answer threads'}
          />
          <button
            type="button"
            onClick={() => setComposerOpen(true)}
            className="rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" />
            {strings.newPost}
          </button>
        </div>

        <div className="flex flex-wrap gap-1.5">
          {(
            [
              { id: 'TRENDING', label: strings.sortTrending },
              { id: 'NEW', label: strings.sortNew },
              { id: 'TOP', label: strings.sortTop },
              { id: 'UNANSWERED', label: strings.sortUnanswered },
            ] as const
          ).map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setSortBy(tab.id)}
              className={`rounded-xl px-3 py-1.5 text-xs font-semibold ${
                sortBy === tab.id
                  ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-slate-950'
                  : 'bg-white dark:bg-slate-900 border border-[#D5E0EC] dark:border-slate-700'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="space-y-3">
          {sorted.map((post) => (
            <div
              key={post.id}
              className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-2.5"
            >
              <div className="flex flex-wrap items-center justify-between gap-2 text-xs">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="font-bold text-[#0B1F38] dark:text-white">{post.authorName}</span>
                  <span className="text-slate-400">·</span>
                  <span className="text-slate-500">{post.authorLevel}</span>
                  <VerifiedIndicator verified={post.authorVerified} />
                </div>
                <span className="text-[11px] font-semibold text-[#008CAE] dark:text-[#7EE8F5]">{post.postType}</span>
              </div>

              <button
                type="button"
                onClick={() => {
                  setSelectedPostId(post.id);
                  setRoute('postDetail');
                }}
                className="text-start block w-full"
              >
                <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-white hover:text-[#008CAE] transition">
                  {post.title}
                </h3>
                <p className="text-xs text-slate-600 dark:text-slate-300 mt-1 line-clamp-2 leading-relaxed">
                  {post.body}
                </p>
              </button>

              <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-slate-100 dark:border-slate-800 text-xs text-slate-500">
                <span>{post.tags.map((t) => `#${t}`).join('  ')}</span>
                <div className="flex items-center gap-4">
                  <span className="inline-flex items-center gap-1 tabular-nums">
                    <ThumbsUp className="w-3.5 h-3.5" /> {post.helpfulCount} {strings.helpfulLabel}
                  </span>
                  <span className="inline-flex items-center gap-1 tabular-nums">
                    <MessageCircle className="w-3.5 h-3.5" /> {post.commentCount}
                  </span>
                  {post.acceptedCommentId && (
                    <span className="inline-flex items-center gap-1 text-emerald-700 dark:text-emerald-400 font-semibold">
                      <CheckCircle2 className="w-3.5 h-3.5" /> {strings.bestAnswer}
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {composerOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <form
              onSubmit={handlePublish}
              className="w-full max-w-lg rounded-2xl bg-white dark:bg-slate-900 border border-[#D5E0EC] dark:border-slate-800 p-5 space-y-3"
            >
              <h3 className="font-display text-base font-bold">{strings.newPost}</h3>
              <select
                value={postType}
                onChange={(e) => setPostType(e.target.value)}
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
              >
                <option value="Technical Question">Technical Question</option>
                <option value="Tutorial">Tutorial</option>
                <option value="Team Ad">Team Ad</option>
              </select>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Concise title..."
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
              />
              <textarea
                required
                rows={4}
                value={body}
                onChange={(e) => setBody(e.target.value)}
                placeholder="Include code context or constraints (secrets are auto-blocked)..."
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
              />
              <input
                type="text"
                value={tagsCsv}
                onChange={(e) => setTagsCsv(e.target.value)}
                placeholder="Tags (comma-separated)"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
              />
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setComposerOpen(false)} className="px-3 py-1.5 text-xs font-semibold">
                  {strings.cancelLabel}
                </button>
                <button type="submit" className="rounded-xl bg-[#00B4DB] px-4 py-2 text-xs font-bold text-white">
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

export const PostDetailScreen: React.FC = () => {
  const {
    selectedPostId,
    communityPosts,
    setRoute,
    strings,
    isArabic,
    addCommentToPost,
    submitModerationReport,
    session,
    showToast,
  } = useSkillBridge();

  const post = communityPosts.find((p) => p.id === selectedPostId) ?? communityPosts[0];
  const comments = SEED_COMMENTS[post.id] ?? [];
  const [replyBody, setReplyBody] = useState('');

  return (
    <StateWrapper>
      <div className="space-y-5">
        <button
          type="button"
          onClick={() => setRoute('community')}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#008CAE] hover:underline"
        >
          {isArabic ? <ArrowRight className="w-4 h-4" /> : <ArrowLeft className="w-4 h-4" />}
          {strings.backLabel}
        </button>

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 sm:p-6 space-y-3">
          <div className="flex items-center justify-between gap-2 text-xs">
            <div className="flex items-center gap-2">
              <span className="font-bold">{post.authorName}</span>
              <VerifiedIndicator verified={post.authorVerified} />
            </div>
            <button
              type="button"
              onClick={async () => {
                if (session) {
                  await submitModerationReport('COMMUNITY_POST', post.id, 'User flagged post for review.');
                } else {
                  showToast(strings.guestLockedReason);
                }
              }}
              className="text-xs text-slate-500 hover:text-rose-600 inline-flex items-center gap-1"
            >
              <Flag className="w-3.5 h-3.5" />
              {strings.reportContent}
            </button>
          </div>

          <h1 className="font-display text-xl font-bold text-[#0B1F38] dark:text-white">{post.title}</h1>
          <p className="text-sm text-slate-600 dark:text-slate-300 leading-relaxed whitespace-pre-wrap">{post.body}</p>
        </div>

        <div className="space-y-3">
          <SectionHeader title={`${isArabic ? 'الردود' : 'Answers'} (${comments.length})`} />
          {comments.map((c) => (
            <div
              key={c.id}
              className={`rounded-2xl border p-4 space-y-1.5 bg-white dark:bg-slate-900 ${
                c.isBestAnswer ? 'border-emerald-400 dark:border-emerald-700' : 'border-[#D5E0EC] dark:border-slate-800'
              }`}
            >
              <div className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  <span className="font-bold">{c.authorName}</span>
                  <VerifiedIndicator verified={c.authorVerified} />
                </div>
                {c.isBestAnswer && (
                  <span className="text-emerald-700 dark:text-emerald-400 font-bold flex items-center gap-1">
                    <CheckCircle2 className="w-3.5 h-3.5" /> {strings.bestAnswer}
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-700 dark:text-slate-200 leading-relaxed">{c.body}</p>
            </div>
          ))}
        </div>

        <form
          onSubmit={async (e) => {
            e.preventDefault();
            try {
              await addCommentToPost(post.id, replyBody);
              setReplyBody('');
            } catch (err) {
              showToast(err instanceof Error ? err.message : 'Sign in required');
            }
          }}
          className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4 flex gap-2"
        >
          <input
            type="text"
            required
            value={replyBody}
            onChange={(e) => setReplyBody(e.target.value)}
            placeholder={isArabic ? 'أضف إجابة تقنية أو ملاحظة...' : 'Write a helpful technical answer...'}
            className="flex-1 rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
          />
          <button type="submit" className="rounded-xl bg-[#00B4DB] px-4 py-2 text-xs font-bold text-white">
            {strings.replyLabel}
          </button>
        </form>
      </div>
    </StateWrapper>
  );
};

export const TeamsHubScreen: React.FC = () => {
  const {
    strings,
    isArabic,
    teams,
    firebaseUser,
    setSelectedTeamId,
    setRoute,
    createTeam,
    joinTeam,
    showToast,
  } = useSkillBridge();

  const [tab, setTab] = useState<'RECRUITING' | 'MINE'>('RECRUITING');
  const [modalOpen, setModalOpen] = useState(false);
  const [name, setName] = useState('');
  const [desc, setDesc] = useState('');
  const [challengeTitle, setChallengeTitle] = useState('Smart City Analytics Dashboard');
  const [rolesNeeded, setRolesNeeded] = useState('Backend API, QA Engineer');

  const visibleTeams = useMemo(() => {
    if (tab === 'MINE' && firebaseUser) {
      return teams.filter((t) => t.memberIds.includes(firebaseUser.uid));
    }
    return teams;
  }, [teams, tab, firebaseUser]);

  return (
    <StateWrapper>
      <div className="space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SectionHeader
            title={strings.teamsTitle}
            subtitle={isArabic ? 'شكّل فريقك ووزّع المهام والمكافآت بشفافية كاملة' : 'Form sprint squads with transparent role & reward distribution'}
          />
          <button
            type="button"
            onClick={() => setModalOpen(true)}
            className="rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" />
            {strings.createTeam}
          </button>
        </div>

        <div className="flex gap-1.5">
          <button
            type="button"
            onClick={() => setTab('RECRUITING')}
            className={`rounded-xl px-3.5 py-1.5 text-xs font-bold ${
              tab === 'RECRUITING'
                ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-slate-950'
                : 'bg-white dark:bg-slate-900 border border-[#D5E0EC] dark:border-slate-700'
            }`}
          >
            {strings.recruitingTeams}
          </button>
          <button
            type="button"
            onClick={() => setTab('MINE')}
            className={`rounded-xl px-3.5 py-1.5 text-xs font-bold ${
              tab === 'MINE'
                ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-slate-950'
                : 'bg-white dark:bg-slate-900 border border-[#D5E0EC] dark:border-slate-700'
            }`}
          >
            {strings.myTeams}
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {visibleTeams.map((team) => (
            <div
              key={team.id}
              className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3"
            >
              <div className="flex items-center justify-between gap-2">
                <span className="text-xs font-bold text-[#008CAE]">{localizedDomain(team.domain, isArabic ? 'AR' : 'EN')}</span>
                <span className="text-xs text-slate-500 tabular-nums">
                  {team.memberNames.length}/{team.maxMembers} {strings.membersLabel}
                </span>
              </div>
              <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-white">{team.name}</h3>
              <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">{team.description}</p>
              <p className="text-[11px] text-slate-500">
                <span className="font-semibold">{strings.needRolePrefix}</span> {team.rolesNeeded.join(', ')}
              </p>

              <div className="flex items-center gap-2 pt-2 border-t border-slate-100 dark:border-slate-800">
                <button
                  type="button"
                  onClick={() => {
                    setSelectedTeamId(team.id);
                    setRoute('teamWorkspace');
                  }}
                  className="flex-1 rounded-xl border border-[#D5E0EC] dark:border-slate-700 py-2 text-xs font-bold"
                >
                  {strings.sprintBoard}
                </button>
                <button
                  type="button"
                  onClick={() => joinTeam(team)}
                  className="rounded-xl bg-[#00B4DB] px-4 py-2 text-xs font-bold text-white"
                >
                  {strings.joinTeam}
                </button>
              </div>
            </div>
          ))}
        </div>

        {modalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                try {
                  await createTeam({
                    name,
                    description: desc,
                    domain: 'Backend & Cloud',
                    challengeTitle,
                    rolesNeeded: rolesNeeded.split(',').map((s) => s.trim()).filter(Boolean),
                    requiredSkills: ['TypeScript', 'SQL'],
                  });
                  setModalOpen(false);
                  setName('');
                  setDesc('');
                } catch (err) {
                  showToast(err instanceof Error ? err.message : 'Unable to create team');
                }
              }}
              className="w-full max-w-md rounded-2xl bg-white dark:bg-slate-900 border border-[#D5E0EC] dark:border-slate-800 p-5 space-y-3"
            >
              <h3 className="font-display text-base font-bold">{strings.createTeam}</h3>
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Team Name"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
              />
              <textarea
                required
                rows={3}
                value={desc}
                onChange={(e) => setDesc(e.target.value)}
                placeholder="Team goal & collaboration charter..."
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
              />
              <input
                type="text"
                value={challengeTitle}
                onChange={(e) => setChallengeTitle(e.target.value)}
                placeholder="Target Challenge"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
              />
              <input
                type="text"
                value={rolesNeeded}
                onChange={(e) => setRolesNeeded(e.target.value)}
                placeholder="Roles Needed"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-3 py-2 text-xs"
              />
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setModalOpen(false)} className="px-3 py-1.5 text-xs font-semibold">
                  {strings.cancelLabel}
                </button>
                <button type="submit" className="rounded-xl bg-[#00B4DB] px-4 py-2 text-xs font-bold text-white">
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

export const TeamWorkspaceScreen: React.FC = () => {
  const { selectedTeamId, teams, teamTasks, setRoute, strings, isArabic } = useSkillBridge();
  const team = teams.find((t) => t.id === selectedTeamId) ?? teams[0];
  const tasks = teamTasks[team.id] ?? [];

  return (
    <StateWrapper>
      <div className="space-y-5">
        <button
          type="button"
          onClick={() => setRoute('teams')}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#008CAE] hover:underline"
        >
          {isArabic ? <ArrowRight className="w-4 h-4" /> : <ArrowLeft className="w-4 h-4" />}
          {strings.backLabel}
        </button>

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h1 className="font-display text-xl font-bold text-[#0B1F38] dark:text-white">{team.name}</h1>
            <span className="text-xs font-bold text-[#008CAE] tabular-nums">{team.sprintProgress}% Sprint Progress</span>
          </div>
          <p className="text-xs text-slate-600 dark:text-slate-300">{team.challengeTitle} — {team.description}</p>
          <div className="rounded-xl border border-slate-200 dark:border-slate-800 p-3 text-xs">
            <p className="font-bold text-[#0B1F38] dark:text-white mb-1">{strings.rewardSplitTitle}</p>
            <p className="text-slate-600 dark:text-slate-300">
              {isArabic
                ? `توزيع متساوٍ بين الأعضاء النشطين (${Math.round(100 / Math.max(1, team.memberNames.length))}% لكل عضو موثّق).`
                : `Equal documented split (${Math.round(100 / Math.max(1, team.memberNames.length))}% per active verified contributor).`}
            </p>
          </div>
        </div>

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
          <SectionHeader title={strings.sprintBoard} />
          <div className="divide-y divide-slate-100 dark:divide-slate-800">
            {tasks.map((t) => (
              <div key={t.id} className="py-3 flex items-center justify-between gap-3">
                <div>
                  <p className="text-xs font-bold text-[#0B1F38] dark:text-white">{t.title}</p>
                  <p className="text-[11px] text-slate-500">
                    {t.assigneeName} · {t.role}
                  </p>
                </div>
                <StatusLabel status={t.status} />
              </div>
            ))}
          </div>
        </div>
      </div>
    </StateWrapper>
  );
};

export const ExperiencePassportScreen: React.FC = () => {
  const { strings, isArabic, profile, submissions, showToast, setRoute } = useSkillBridge();
  const [proofCodeModal, setProofCodeModal] = useState<string | null>(null);
  const lang = isArabic ? 'AR' : 'EN';

  const realSubs = submissions.filter((s) => !s.isDemoSample && !s.id.startsWith('seed_'));
  const demoSubs = submissions.filter((s) => s.isDemoSample || s.id.startsWith('seed_'));

  return (
    <StateWrapper>
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SectionHeader
            title={strings.passportTitle}
            subtitle={isArabic ? 'سجل عملي قابل للتحقق ومربوط برمز توثيق رسمي' : 'Employer-verifiable evidence record backed by mentor rubric reviews'}
          />
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setRoute('badges')}
              className="rounded-xl border border-[#D5E0EC] dark:border-slate-700 bg-white dark:bg-slate-900 px-3 py-2 text-xs font-bold flex items-center gap-1.5"
            >
              <Trophy className="w-4 h-4 text-[#00B4DB]" />
              {strings.badgesTitle}
            </button>
            <button
              type="button"
              onClick={async () => {
                await navigator.clipboard?.writeText(window.location.href).catch(() => {});
                showToast(isArabic ? 'تم نسخ رابط التحقق من السجل.' : 'Passport verification link copied.');
              }}
              className="rounded-xl bg-[#0B1F38] dark:bg-[#00B4DB] text-white dark:text-slate-950 px-4 py-2 text-xs font-bold flex items-center gap-1.5"
            >
              <Share2 className="w-3.5 h-3.5" />
              {strings.sharePassport}
            </button>
          </div>
        </div>

        {/* Summary Strip */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4">
            <p className="text-[11px] text-slate-500">{strings.verifiedExperience}</p>
            <p className="font-display text-2xl font-bold tabular-nums mt-1">
              {realSubs.filter((s) => s.status === 'VERIFIED').length}
            </p>
          </div>
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4">
            <p className="text-[11px] text-slate-500">{strings.pointsLabel}</p>
            <p className="font-display text-2xl font-bold tabular-nums mt-1">{profile.points}</p>
          </div>
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4">
            <p className="text-[11px] text-slate-500">{isArabic ? 'المستوى المهني' : 'Verified Tier'}</p>
            <p className="font-display text-base font-bold text-[#008CAE] mt-1">{profile.careerLevel}</p>
          </div>
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4">
            <p className="text-[11px] text-slate-500">{isArabic ? 'إظهار السجل' : 'Visibility'}</p>
            <p className="font-display text-base font-bold mt-1">
              {profile.passportPublic ? (isArabic ? 'عام للتحقق' : 'Public Verify') : isArabic ? 'خاص' : 'Private'}
            </p>
          </div>
        </div>

        {/* Verified & Synchronized Submissions */}
        <section className="space-y-3">
          <SectionHeader title={strings.verifiedExperience} />
          {realSubs.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-slate-300 dark:border-slate-700 p-6 text-center text-xs text-slate-500">
              {isArabic
                ? 'لا توجد تسليمات خاصة بك موثّقة بعد — قدّم في مشروع شركة أو تحدٍ ليظهر هنا مع رمز تحقق.'
                : 'No personal submissions verified yet — submit a company brief or challenge below to mint your first verification code.'}
            </div>
          ) : (
            <div className="space-y-3">
              {realSubs.map((sub) => (
                <div
                  key={sub.id}
                  className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-2"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-white">
                        {sub.challengeTitle}
                      </h3>
                      <p className="text-xs text-[#008CAE] font-semibold">
                        {sub.companyName} · {sub.skillsApplied.join(' · ')}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <StatusLabel status={sub.status} />
                      {sub.score > 0 && (
                        <span className="text-xs font-bold tabular-nums text-[#0B1F38] dark:text-white">
                          {sub.score}%
                        </span>
                      )}
                    </div>
                  </div>
                  {sub.mentorFeedback && (
                    <p className="text-xs text-slate-600 dark:text-slate-300">
                      <span className="font-semibold">{strings.mentorFeedback}:</span> {sub.mentorFeedback}
                    </p>
                  )}
                  <div className="flex items-center justify-between pt-2 border-t border-slate-100 dark:border-slate-800 text-xs">
                    <span className="font-mono text-[11px] text-slate-500">{sub.verificationCode}</span>
                    <button
                      type="button"
                      onClick={() => setProofCodeModal(sub.verificationCode)}
                      className="font-semibold text-[#008CAE] hover:underline"
                    >
                      {isArabic ? 'بطاقة التحقق' : 'Inspect Verification Proof'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Reference Sample Entries (Clearly Tagged Demo Reference) */}
        <section className="space-y-3">
          <SectionHeader title={`${strings.demoCatalogLabel} — ${strings.skillEvidenceGraph}`} />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            {EVIDENCE_NODES.map((node) => (
              <div
                key={node.verificationId}
                className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-4 space-y-1.5"
              >
                <div className="flex items-center justify-between gap-2 text-[11px]">
                  <span className="font-bold text-[#008CAE]">{node.level}</span>
                  <ShieldCheck className="w-4 h-4 text-emerald-600" />
                </div>
                <h4 className="font-display text-sm font-bold text-[#0B1F38] dark:text-white">{node.skill}</h4>
                <p className="text-xs text-slate-600 dark:text-slate-300">{lText(node.proofTitle, lang)}</p>
                <p className="text-[11px] text-slate-500">{node.evaluator}</p>
              </div>
            ))}
          </div>

          {demoSubs.map((sub) => (
            <div
              key={sub.id}
              className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/60 p-4 flex flex-wrap items-center justify-between gap-2"
            >
              <div>
                <p className="text-[10px] font-bold uppercase tracking-wider text-slate-500">
                  {strings.demoCatalogLabel}
                </p>
                <p className="text-sm font-bold text-[#0B1F38] dark:text-white">{sub.challengeTitle}</p>
                <p className="text-xs text-slate-500">{sub.mentorFeedback}</p>
              </div>
              <StatusLabel status={sub.status} />
            </div>
          ))}
        </section>

        {proofCodeModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div className="w-full max-w-sm rounded-2xl bg-white dark:bg-slate-900 border border-[#D5E0EC] dark:border-slate-800 p-5 space-y-3 text-center">
              <Award className="w-8 h-8 mx-auto text-[#00B4DB]" />
              <h3 className="font-display text-base font-bold">
                {isArabic ? 'شهادة توثيق خبرة SkillBridge' : 'SkillBridge Cryptographic Verification'}
              </h3>
              <p className="font-mono text-sm font-bold bg-slate-100 dark:bg-slate-800 py-2 rounded-xl">
                {proofCodeModal}
              </p>
              <button
                type="button"
                onClick={() => setProofCodeModal(null)}
                className="w-full rounded-xl bg-[#00B4DB] py-2 text-xs font-bold text-white"
              >
                {strings.closeLabel}
              </button>
            </div>
          </div>
        )}
      </div>
    </StateWrapper>
  );
};

export const BadgesGalleryScreen: React.FC = () => {
  const { strings, isArabic, profile, submissions, badges, communityPosts, teams, teamTasks, milestones } =
    useSkillBridge();
  const lang = isArabic ? 'AR' : 'EN';

  const progressList = useMemo(() => {
    const allTasks = Object.values(teamTasks).flat();
    return evaluateBadgeProgress({
      profile,
      submissions,
      earnedBadges: badges,
      authoredPosts: communityPosts.filter((p) => p.authorId === profile.userId),
      authoredComments: [],
      userTeams: teams.filter((t) => t.memberIds.includes(profile.userId)),
      userTeamTasks: allTasks,
      completedMilestones: milestones.filter((m) => m.completed),
    });
  }, [profile, submissions, badges, communityPosts, teams, teamTasks, milestones]);

  return (
    <StateWrapper>
      <div className="space-y-5">
        <SectionHeader
          title={strings.badgesTitle}
          subtitle={
            isArabic
              ? 'تُفتح الشارات تلقائيًا بناءً على تسليماتك الموثّقة ومساهماتك الحقيقية'
              : 'Badges unlock strictly from verified challenge evidence, teams, and community contributions'
          }
        />

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {progressList.map((item) => (
            <div
              key={item.definition.id}
              className={`rounded-2xl border p-4 space-y-2.5 bg-white dark:bg-slate-900 ${
                item.isUnlocked
                  ? 'border-[#00B4DB] ring-1 ring-[#00B4DB]/20'
                  : 'border-[#D5E0EC] dark:border-slate-800'
              }`}
            >
              <div className="flex items-center justify-between gap-2">
                {item.isUnlocked ? (
                  <Trophy className="w-5 h-5 text-[#00B4DB]" />
                ) : (
                  <Lock className="w-5 h-5 text-slate-400" />
                )}
                <span className="text-[11px] font-bold text-[#008CAE] tabular-nums">
                  +{item.definition.pointsAwarded} pts
                </span>
              </div>
              <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-white">
                {lText(item.definition.title, lang)}
              </h3>
              <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">
                {lText(item.definition.description, lang)}
              </p>
              <div className="space-y-1 pt-1">
                <div className="h-1.5 w-full rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
                  <div
                    className="h-full bg-[#00B4DB] rounded-full transition-all"
                    style={{ width: `${item.progressPercent}%` }}
                  />
                </div>
                <p className="text-[11px] text-slate-500 flex justify-between tabular-nums">
                  <span>{lText(item.remainingLabel, lang)}</span>
                  <span>
                    {item.currentCount}/{item.targetCount}
                  </span>
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </StateWrapper>
  );
};

export const PointsWalletScreen: React.FC = () => {
  const { strings, isArabic, profile, pointLedger, auditLogs } = useSkillBridge();

  return (
    <StateWrapper>
      <div className="space-y-6">
        <SectionHeader
          title={strings.walletTitle}
          subtitle={`${profile.points} ${strings.pointsLabel} · ${strings.auditLogTitle}`}
        />

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-500">{isArabic ? 'الرصيد الكلي الموثّق' : 'Total Verified Points'}</p>
            <p className="font-display text-3xl font-bold text-[#0B1F38] dark:text-white tabular-nums mt-1">
              {profile.points}
            </p>
          </div>
          <Coins className="w-9 h-9 text-[#00B4DB]" />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
            <SectionHeader title={strings.walletTitle} />
            {pointLedger.length === 0 ? (
              <p className="text-xs text-slate-500">
                {isArabic
                  ? 'ستظهر حركات النقاط والمكافآت هنا بعد أول توثيق عملي.'
                  : 'Reward transactions appear here once submissions or badges are verified.'}
              </p>
            ) : (
              <div className="divide-y divide-slate-100 dark:divide-slate-800">
                {pointLedger.map((tx) => (
                  <div key={tx.id} className="py-2.5 flex items-center justify-between text-xs">
                    <div>
                      <p className="font-semibold text-[#0B1F38] dark:text-white">{tx.title}</p>
                      <p className="text-[11px] text-slate-500">{tx.reason}</p>
                    </div>
                    <span className="font-bold text-emerald-600 tabular-nums">+{tx.points} pts</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 p-5 space-y-3">
            <SectionHeader title={strings.auditLogTitle} />
            {auditLogs.length === 0 ? (
              <p className="text-xs text-slate-500">
                {isArabic ? 'سجل الأمان والتحقق جاهز لتوثيق أفعالك.' : 'Account verification audit events record automatically here.'}
              </p>
            ) : (
              <div className="divide-y divide-slate-100 dark:divide-slate-800">
                {auditLogs.map((log) => (
                  <div key={log.id} className="py-2.5 text-xs">
                    <p className="font-semibold text-[#0B1F38] dark:text-white">{log.summary}</p>
                    <p className="text-[11px] text-slate-500">
                      {log.actionType} · {log.actorRole}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </StateWrapper>
  );
};

export const LeaderboardScreen: React.FC = () => {
  const { strings, isArabic } = useSkillBridge();
  const [filterTeamsOnly, setFilterTeamsOnly] = useState(false);

  const rows = useMemo(
    () => LEADERBOARD_ENTRIES.filter((r) => (filterTeamsOnly ? r.isTeam : !r.isTeam)),
    [filterTeamsOnly],
  );

  return (
    <StateWrapper>
      <div className="space-y-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SectionHeader
            title={strings.leaderboardTitle}
            subtitle={`${strings.ratingFormulaTitle}: 60% Verified Score + 25% Consistency + 15% Peer/Contest Impact`}
          />
          <div className="flex gap-1.5">
            <button
              type="button"
              onClick={() => setFilterTeamsOnly(false)}
              className={`rounded-xl px-3 py-1.5 text-xs font-bold ${
                !filterTeamsOnly ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-slate-950' : 'border border-[#D5E0EC] dark:border-slate-700'
              }`}
            >
              {isArabic ? 'الأفراد' : 'Individuals'}
            </button>
            <button
              type="button"
              onClick={() => setFilterTeamsOnly(true)}
              className={`rounded-xl px-3 py-1.5 text-xs font-bold ${
                filterTeamsOnly ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-slate-950' : 'border border-[#D5E0EC] dark:border-slate-700'
              }`}
            >
              <Users className="w-3.5 h-3.5 inline me-1" />
              {strings.navTeams}
            </button>
          </div>
        </div>

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-slate-800 bg-white dark:bg-slate-900 overflow-x-auto">
          <table className="w-full text-xs">
            <thead className="bg-slate-50 dark:bg-slate-950/60 border-b border-[#D5E0EC] dark:border-slate-800 text-slate-500">
              <tr>
                <th className="text-start px-4 py-3 font-semibold">#</th>
                <th className="text-start px-4 py-3 font-semibold">{isArabic ? 'الاسم' : 'Contributor'}</th>
                <th className="text-start px-4 py-3 font-semibold">{isArabic ? 'المجال' : 'Domain'}</th>
                <th className="text-start px-4 py-3 font-semibold">{isArabic ? 'التصنيف' : 'Rating'}</th>
                <th className="text-start px-4 py-3 font-semibold">{strings.pointsLabel}</th>
                <th className="text-start px-4 py-3 font-semibold">{isArabic ? 'أدلة موثّقة' : 'Verified'}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {rows.map((row) => (
                <tr key={row.name} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/40">
                  <td className="px-4 py-3 font-bold tabular-nums">{row.rank}</td>
                  <td className="px-4 py-3 font-semibold text-[#0B1F38] dark:text-white">
                    {row.name}
                    <span className="block text-[11px] font-normal text-slate-500">
                      {row.country} · {row.level}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-slate-600 dark:text-slate-300">{row.domain}</td>
                  <td className="px-4 py-3 font-bold text-[#008CAE] tabular-nums">{row.rating}</td>
                  <td className="px-4 py-3 font-semibold tabular-nums">{row.points}</td>
                  <td className="px-4 py-3 tabular-nums">{row.verifiedCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </StateWrapper>
  );
};
