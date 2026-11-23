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
import { AnimatedNumber, MotionReveal, SpatialCard } from '../components/InteractiveMotion';
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
    const arr = Array.isArray(communityPosts) ? [...communityPosts] : [];
    if (sortBy === 'NEW') return arr.sort((a, b) => (b.createdAtMs ?? 0) - (a.createdAtMs ?? 0));
    if (sortBy === 'TOP' || sortBy === 'TRENDING') return arr.sort((a, b) => (b.helpfulCount ?? 0) - (a.helpfulCount ?? 0));
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
            className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white flex items-center gap-1.5 whitespace-nowrap"
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
              className={`rounded-xl px-3 py-1.5 text-xs font-semibold transition active:scale-95 whitespace-nowrap ${
                sortBy === tab.id
                  ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-white'
                  : 'bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="space-y-3">
          {sorted.map((post, idx) => (
            <MotionReveal key={post.id} delayMs={Math.min(idx, 5) * 45}>
              <SpatialCard
                tiltStrength={2.5}
                className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-2.5"
              >
                <div className="flex flex-wrap items-center justify-between gap-2 text-xs">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{post.authorName}</span>
                    <span className="text-[#4C637C] dark:text-[#93ADC8]">·</span>
                    <span className="text-[#4C637C] dark:text-[#93ADC8]">{post.authorLevel}</span>
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
                  <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC] hover:text-[#008CAE] dark:hover:text-[#7EE8F5] transition">
                    {post.title}
                  </h3>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] mt-1 line-clamp-2 leading-relaxed">
                    {post.body}
                  </p>
                </button>

                <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-[#D5E0EC]/80 dark:border-[#1C395E] text-xs text-[#4C637C] dark:text-[#93ADC8]">
                  <span>{(post.tags ?? []).map((t) => `#${t}`).join('  ')}</span>
                  <div className="flex items-center gap-4">
                    <span className="inline-flex items-center gap-1 tabular-nums">
                      <ThumbsUp className="w-3.5 h-3.5 text-[#00B4DB]" /> {post.helpfulCount} {strings.helpfulLabel}
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
              </SpatialCard>
            </MotionReveal>
          ))}
        </div>

        {composerOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#061120]/65 backdrop-blur-sm p-4">
            <form
              onSubmit={handlePublish}
              className="sb-route-enter w-full max-w-lg rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-5 space-y-3 shadow-2xl"
            >
              <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{strings.newPost}</h3>
              <select
                value={postType}
                onChange={(e) => setPostType(e.target.value)}
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC]"
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
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <textarea
                required
                rows={4}
                value={body}
                onChange={(e) => setBody(e.target.value)}
                placeholder="Include code context or constraints (secrets are auto-blocked)..."
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <input
                type="text"
                value={tagsCsv}
                onChange={(e) => setTagsCsv(e.target.value)}
                placeholder="Tags (comma-separated)"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setComposerOpen(false)} className="px-3 py-1.5 text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8]">
                  {strings.cancelLabel}
                </button>
                <button type="submit" className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white">
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

  const safePosts = Array.isArray(communityPosts) ? communityPosts : [];
  const post = safePosts.find((p) => p.id === selectedPostId) ?? safePosts[0];
  const [replyBody, setReplyBody] = useState('');
  if (!post) return null;
  const comments = SEED_COMMENTS[post.id] ?? [];

  return (
    <StateWrapper>
      <div className="space-y-5">
        <button
          type="button"
          onClick={() => setRoute('community')}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline"
        >
          {isArabic ? <ArrowRight className="w-4 h-4" /> : <ArrowLeft className="w-4 h-4" />}
          {strings.backLabel}
        </button>

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 sm:p-6 space-y-3">
          <div className="flex items-center justify-between gap-2 text-xs">
            <div className="flex items-center gap-2">
              <span className="font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{post.authorName}</span>
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
              className="text-xs text-[#4C637C] dark:text-[#93ADC8] hover:text-rose-600 inline-flex items-center gap-1"
            >
              <Flag className="w-3.5 h-3.5" />
              {strings.reportContent}
            </button>
          </div>

          <h1 className="font-display text-xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] [text-wrap:balance]">{post.title}</h1>
          <p className="text-sm text-[#4C637C] dark:text-[#93ADC8] leading-relaxed whitespace-pre-wrap">{post.body}</p>
        </div>

        <div className="space-y-3">
          <SectionHeader title={`${isArabic ? 'الردود' : 'Answers'} (${comments.length})`} />
          {comments.map((c) => (
            <div
              key={c.id}
              className={`rounded-2xl border p-4 space-y-1.5 bg-white dark:bg-[#0D1E36] ${
                c.isBestAnswer ? 'border-emerald-400 dark:border-emerald-700' : 'border-[#D5E0EC] dark:border-[#1C395E]'
              }`}
            >
              <div className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  <span className="font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{c.authorName}</span>
                  <VerifiedIndicator verified={c.authorVerified} />
                </div>
                {c.isBestAnswer && (
                  <span className="text-emerald-700 dark:text-emerald-400 font-bold flex items-center gap-1">
                    <CheckCircle2 className="w-3.5 h-3.5" /> {strings.bestAnswer}
                  </span>
                )}
              </div>
              <p className="text-xs text-[#0B1F38] dark:text-[#F1F7FC] leading-relaxed">{c.body}</p>
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
          className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 flex gap-2"
        >
          <input
            type="text"
            required
            value={replyBody}
            onChange={(e) => setReplyBody(e.target.value)}
            placeholder={isArabic ? 'أضف إجابة تقنية أو ملاحظة...' : 'Write a helpful technical answer...'}
            className="flex-1 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
          />
          <button type="submit" className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white">
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

  const safeTeams = Array.isArray(teams) ? teams : [];
  const visibleTeams = useMemo(() => {
    if (tab === 'MINE' && firebaseUser) {
      return safeTeams.filter((t) => (t.memberIds ?? []).includes(firebaseUser.uid));
    }
    return safeTeams;
  }, [safeTeams, tab, firebaseUser]);

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
            className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white flex items-center gap-1.5 whitespace-nowrap"
          >
            <Plus className="w-4 h-4" />
            {strings.createTeam}
          </button>
        </div>

        <div className="flex gap-1.5">
          <button
            type="button"
            onClick={() => setTab('RECRUITING')}
            className={`rounded-xl px-3.5 py-1.5 text-xs font-bold active:scale-95 transition whitespace-nowrap ${
              tab === 'RECRUITING'
                ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-white'
                : 'bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]'
            }`}
          >
            {strings.recruitingTeams}
          </button>
          <button
            type="button"
            onClick={() => setTab('MINE')}
            className={`rounded-xl px-3.5 py-1.5 text-xs font-bold active:scale-95 transition whitespace-nowrap ${
              tab === 'MINE'
                ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-white'
                : 'bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]'
            }`}
          >
            {strings.myTeams}
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {visibleTeams.map((team, idx) => (
            <MotionReveal key={team.id} delayMs={Math.min(idx, 5) * 55}>
              <SpatialCard
                tiltStrength={4}
                className="h-full rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3"
              >
                <div className="space-y-2.5">
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-xs font-bold text-[#008CAE] dark:text-[#7EE8F5]">
                      {localizedDomain(team.domain, isArabic ? 'AR' : 'EN')}
                    </span>
                    <span className="text-xs text-[#4C637C] dark:text-[#93ADC8] tabular-nums">
                      {(team.memberNames ?? []).length}/{team.maxMembers} {strings.membersLabel}
                    </span>
                  </div>
                  <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{team.name}</h3>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] leading-relaxed">{team.description}</p>
                  <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
                    <span className="font-semibold">{strings.needRolePrefix}</span> {(team.rolesNeeded ?? []).join(', ')}
                  </p>
                </div>

                <div className="flex items-center gap-2 pt-2 border-t border-[#D5E0EC]/80 dark:border-[#1C395E]">
                  <button
                    type="button"
                    onClick={() => {
                      setSelectedTeamId(team.id);
                      setRoute('teamWorkspace');
                    }}
                    className="flex-1 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] py-2 text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB] active:scale-95 transition"
                  >
                    {strings.sprintBoard}
                  </button>
                  <button
                    type="button"
                    onClick={() => joinTeam(team)}
                    className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white"
                  >
                    {strings.joinTeam}
                  </button>
                </div>
              </SpatialCard>
            </MotionReveal>
          ))}
        </div>

        {modalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#061120]/65 backdrop-blur-sm p-4">
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
              className="sb-route-enter w-full max-w-md rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-5 space-y-3 shadow-2xl"
            >
              <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{strings.createTeam}</h3>
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Team Name"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <textarea
                required
                rows={3}
                value={desc}
                onChange={(e) => setDesc(e.target.value)}
                placeholder="Team goal & collaboration charter..."
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <input
                type="text"
                value={challengeTitle}
                onChange={(e) => setChallengeTitle(e.target.value)}
                placeholder="Target Challenge"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <input
                type="text"
                value={rolesNeeded}
                onChange={(e) => setRolesNeeded(e.target.value)}
                placeholder="Roles Needed"
                className="w-full rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] px-3 py-2 text-xs text-[#0B1F38] dark:text-[#F1F7FC] focus:outline-none focus:border-[#00B4DB]"
              />
              <div className="flex justify-end gap-2">
                <button type="button" onClick={() => setModalOpen(false)} className="px-3 py-1.5 text-xs font-semibold text-[#4C637C] dark:text-[#93ADC8]">
                  {strings.cancelLabel}
                </button>
                <button type="submit" className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2 text-xs font-bold text-white">
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
  const safeTeams = Array.isArray(teams) ? teams : [];
  const team = safeTeams.find((t) => t.id === selectedTeamId) ?? safeTeams[0];
  if (!team) return null;
  const tasks = Array.isArray(teamTasks?.[team.id]) ? teamTasks[team.id] : [];

  return (
    <StateWrapper>
      <div className="space-y-5">
        <button
          type="button"
          onClick={() => setRoute('teams')}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline"
        >
          {isArabic ? <ArrowRight className="w-4 h-4" /> : <ArrowLeft className="w-4 h-4" />}
          {strings.backLabel}
        </button>

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h1 className="font-display text-xl font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{team.name}</h1>
            <span className="text-xs font-bold text-[#008CAE] dark:text-[#7EE8F5] tabular-nums">{team.sprintProgress}% Sprint Progress</span>
          </div>
          <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{team.challengeTitle} — {team.description}</p>
          <div className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] p-3 text-xs">
            <p className="font-bold text-[#0B1F38] dark:text-[#F1F7FC] mb-1">{strings.rewardSplitTitle}</p>
            <p className="text-[#4C637C] dark:text-[#93ADC8]">
              {isArabic
                ? `توزيع متساوٍ بين الأعضاء النشطين (${Math.round(100 / Math.max(1, (team.memberNames ?? []).length))}% لكل عضو موثّق).`
                : `Equal documented split (${Math.round(100 / Math.max(1, (team.memberNames ?? []).length))}% per active verified contributor).`}
            </p>
          </div>
        </div>

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
          <SectionHeader title={strings.sprintBoard} />
          <div className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E]">
            {tasks.map((t) => (
              <div key={t.id} className="py-3 flex items-center justify-between gap-3">
                <div>
                  <p className="text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{t.title}</p>
                  <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
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

  const safeSubmissions = Array.isArray(submissions) ? submissions : [];
  const realSubs = safeSubmissions.filter((s) => !s.isDemoSample && !String(s.id ?? '').startsWith('seed_'));
  const demoSubs = safeSubmissions.filter((s) => s.isDemoSample || String(s.id ?? '').startsWith('seed_'));

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
              className="rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] px-3 py-2 text-xs font-bold text-[#0B1F38] dark:text-[#F1F7FC] flex items-center gap-1.5 hover:border-[#00B4DB] active:scale-95 transition whitespace-nowrap"
            >
              <Trophy className="w-4 h-4 text-[#00B4DB] dark:text-[#7EE8F5]" />
              {strings.badgesTitle}
            </button>
            <button
              type="button"
              onClick={async () => {
                await navigator.clipboard?.writeText(window.location.href).catch(() => {});
                showToast(isArabic ? 'تم نسخ رابط التحقق من السجل.' : 'Passport verification link copied.');
              }}
              className="sb-btn-interactive rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] text-white px-4 py-2 text-xs font-bold flex items-center gap-1.5 whitespace-nowrap"
            >
              <Share2 className="w-3.5 h-3.5" />
              {strings.sharePassport}
            </button>
          </div>
        </div>

        {/* Summary Strip */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{strings.verifiedExperience}</p>
            <p className="font-display text-2xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums mt-1">
              <AnimatedNumber value={realSubs.filter((s) => s.status === 'VERIFIED').length} />
            </p>
          </SpatialCard>
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{strings.pointsLabel}</p>
            <p className="font-display text-2xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums mt-1">
              <AnimatedNumber value={profile.points} />
            </p>
          </SpatialCard>
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'المستوى المهني' : 'Verified Tier'}</p>
            <p className="font-display text-base font-bold text-[#008CAE] dark:text-[#7EE8F5] mt-1">{profile.careerLevel}</p>
          </SpatialCard>
          <SpatialCard tiltStrength={4} className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4">
            <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'إظهار السجل' : 'Visibility'}</p>
            <p className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC] mt-1">
              {profile.passportPublic ? (isArabic ? 'عام للتحقق' : 'Public Verify') : isArabic ? 'خاص' : 'Private'}
            </p>
          </SpatialCard>
        </div>

        {/* Verified & Synchronized Submissions */}
        <section className="space-y-3">
          <SectionHeader title={strings.verifiedExperience} />
          {realSubs.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-[#D5E0EC] dark:border-[#1C395E] p-6 text-center text-xs text-[#4C637C] dark:text-[#93ADC8]">
              {isArabic
                ? 'لا توجد تسليمات خاصة بك موثّقة بعد — قدّم في مشروع شركة أو تحدٍ ليظهر هنا مع رمز تحقق.'
                : 'No personal submissions verified yet — submit a company brief or challenge below to mint your first verification code.'}
            </div>
          ) : (
            <div className="space-y-3">
              {realSubs.map((sub) => (
                <div
                  key={sub.id}
                  className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-2 hover:border-[#00B4DB] transition"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                        {sub.challengeTitle}
                      </h3>
                      <p className="text-xs text-[#008CAE] dark:text-[#7EE8F5] font-semibold">
                        {sub.companyName} · {(sub.skillsApplied ?? []).join(' · ')}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <StatusLabel status={sub.status} />
                      {sub.score > 0 && (
                        <span className="text-xs font-bold tabular-nums text-[#0B1F38] dark:text-[#F1F7FC]">
                          {sub.score}%
                        </span>
                      )}
                    </div>
                  </div>
                  {sub.mentorFeedback && (
                    <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">
                      <span className="font-semibold">{strings.mentorFeedback}:</span> {sub.mentorFeedback}
                    </p>
                  )}
                  <div className="flex items-center justify-between pt-2 border-t border-[#D5E0EC]/80 dark:border-[#1C395E] text-xs">
                    <span className="font-mono text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{sub.verificationCode}</span>
                    <button
                      type="button"
                      onClick={() => setProofCodeModal(sub.verificationCode)}
                      className="font-semibold text-[#008CAE] dark:text-[#7EE8F5] hover:underline"
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
              <SpatialCard
                key={node.verificationId}
                tiltStrength={4}
                className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-4 space-y-1.5"
              >
                <div className="flex items-center justify-between gap-2 text-[11px]">
                  <span className="font-bold text-[#008CAE] dark:text-[#7EE8F5]">{node.level}</span>
                  <ShieldCheck className="w-4 h-4 text-[#00B4DB]" />
                </div>
                <h4 className="font-display text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{node.skill}</h4>
                <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{lText(node.proofTitle, lang)}</p>
                <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{node.evaluator}</p>
              </SpatialCard>
            ))}
          </div>

          {demoSubs.map((sub) => (
            <div
              key={sub.id}
              className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-[#F0F6FA] dark:bg-[#09172B] p-4 flex flex-wrap items-center justify-between gap-2"
            >
              <div>
                <p className="text-[10px] font-bold uppercase tracking-wider text-[#4C637C] dark:text-[#93ADC8]">
                  {strings.demoCatalogLabel}
                </p>
                <p className="text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">{sub.challengeTitle}</p>
                <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{sub.mentorFeedback}</p>
              </div>
              <StatusLabel status={sub.status} />
            </div>
          ))}
        </section>

        {proofCodeModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#061120]/65 backdrop-blur-sm p-4">
            <div className="sb-route-enter w-full max-w-sm rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-5 space-y-3 text-center shadow-2xl">
              <Award className="w-8 h-8 mx-auto text-[#00B4DB] dark:text-[#7EE8F5]" />
              <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                {isArabic ? 'شهادة توثيق خبرة SkillBridge' : 'SkillBridge Cryptographic Verification'}
              </h3>
              <p className="font-mono text-sm font-bold bg-[#F0F6FA] dark:bg-[#09172B] text-[#0B1F38] dark:text-[#7EE8F5] py-2 rounded-xl">
                {proofCodeModal}
              </p>
              <button
                type="button"
                onClick={() => setProofCodeModal(null)}
                className="sb-btn-interactive w-full rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] py-2 text-xs font-bold text-white"
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
    const allTasks = Object.values(teamTasks ?? {}).flat();
    const safePosts = Array.isArray(communityPosts) ? communityPosts : [];
    const safeTeams = Array.isArray(teams) ? teams : [];
    const safeMilestones = Array.isArray(milestones) ? milestones : [];
    return evaluateBadgeProgress({
      profile,
      submissions: Array.isArray(submissions) ? submissions : [],
      earnedBadges: Array.isArray(badges) ? badges : [],
      authoredPosts: safePosts.filter((p) => p.authorId === profile?.userId),
      authoredComments: [],
      userTeams: safeTeams.filter((t) => (t.memberIds ?? []).includes(profile?.userId)),
      userTeamTasks: allTasks,
      completedMilestones: safeMilestones.filter((m) => m.completed),
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
          {progressList.map((item, idx) => (
            <MotionReveal key={item.definition.id} delayMs={Math.min(idx, 5) * 45}>
              <SpatialCard
                tiltStrength={5}
                highlight={item.isUnlocked}
                className={`h-full rounded-2xl border p-4 space-y-2.5 bg-white dark:bg-[#0D1E36] ${
                  item.isUnlocked
                    ? 'border-[#00B4DB] ring-1 ring-[#00B4DB]/25'
                    : 'border-[#D5E0EC] dark:border-[#1C395E]'
                }`}
              >
                <div className="space-y-2">
                  <div className="flex items-center justify-between gap-2">
                    {item.isUnlocked ? (
                      <Trophy className="w-5 h-5 text-[#00B4DB] dark:text-[#7EE8F5]" />
                    ) : (
                      <Lock className="w-5 h-5 text-[#4C637C] dark:text-[#93ADC8]" />
                    )}
                    <span className="text-[11px] font-bold text-[#008CAE] dark:text-[#7EE8F5] tabular-nums">
                      +{item.definition.pointsAwarded} pts
                    </span>
                  </div>
                  <h3 className="font-display text-sm font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                    {lText(item.definition.title, lang)}
                  </h3>
                  <p className="text-xs text-[#4C637C] dark:text-[#93ADC8] leading-relaxed">
                    {lText(item.definition.description, lang)}
                  </p>
                </div>
                <div className="space-y-1 pt-1">
                  <div className="h-1.5 w-full rounded-full bg-[#D5E0EC] dark:bg-[#1C395E] overflow-hidden">
                    <div
                      className="h-full bg-[#00B4DB] rounded-full transition-all duration-500"
                      style={{ width: `${item.progressPercent}%` }}
                    />
                  </div>
                  <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8] flex justify-between tabular-nums">
                    <span>{lText(item.remainingLabel, lang)}</span>
                    <span>
                      {item.currentCount}/{item.targetCount}
                    </span>
                  </p>
                </div>
              </SpatialCard>
            </MotionReveal>
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

        <SpatialCard
          tiltStrength={3}
          highlight
          className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">{isArabic ? 'الرصيد الكلي الموثّق' : 'Total Verified Points'}</p>
              <p className="font-display text-3xl font-bold text-[#0B1F38] dark:text-[#F1F7FC] tabular-nums mt-1">
                <AnimatedNumber value={profile.points} />
              </p>
            </div>
            <Coins className="w-9 h-9 text-[#00B4DB] dark:text-[#7EE8F5]" />
          </div>
        </SpatialCard>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <SectionHeader title={strings.walletTitle} />
            {pointLedger.length === 0 ? (
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">
                {isArabic
                  ? 'ستظهر حركات النقاط والمكافآت هنا بعد أول توثيق عملي.'
                  : 'Reward transactions appear here once submissions or badges are verified.'}
              </p>
            ) : (
              <div className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E]">
                {pointLedger.map((tx) => (
                  <div key={tx.id} className="py-2.5 flex items-center justify-between text-xs">
                    <div>
                      <p className="font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">{tx.title}</p>
                      <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">{tx.reason}</p>
                    </div>
                    <span className="font-bold text-[#008CAE] dark:text-[#7EE8F5] tabular-nums">+{tx.points} pts</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-5 space-y-3">
            <SectionHeader title={strings.auditLogTitle} />
            {auditLogs.length === 0 ? (
              <p className="text-xs text-[#4C637C] dark:text-[#93ADC8]">
                {isArabic ? 'سجل الأمان والتحقق جاهز لتوثيق أفعالك.' : 'Account verification audit events record automatically here.'}
              </p>
            ) : (
              <div className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E]">
                {auditLogs.map((log) => (
                  <div key={log.id} className="py-2.5 text-xs">
                    <p className="font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">{log.summary}</p>
                    <p className="text-[11px] text-[#4C637C] dark:text-[#93ADC8]">
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
              className={`rounded-xl px-3 py-1.5 text-xs font-bold active:scale-95 transition whitespace-nowrap ${
                !filterTeamsOnly ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-white' : 'border border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]'
              }`}
            >
              {isArabic ? 'الأفراد' : 'Individuals'}
            </button>
            <button
              type="button"
              onClick={() => setFilterTeamsOnly(true)}
              className={`rounded-xl px-3 py-1.5 text-xs font-bold active:scale-95 transition whitespace-nowrap ${
                filterTeamsOnly ? 'bg-[#0B1F38] text-white dark:bg-[#00B4DB] dark:text-white' : 'border border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]'
              }`}
            >
              <Users className="w-3.5 h-3.5 inline me-1" />
              {strings.navTeams}
            </button>
          </div>
        </div>

        <div className="rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] overflow-x-auto">
          <table className="w-full text-xs">
            <thead className="bg-[#F0F6FA] dark:bg-[#09172B] border-b border-[#D5E0EC] dark:border-[#1C395E] text-[#4C637C] dark:text-[#93ADC8]">
              <tr>
                <th className="text-start px-4 py-3 font-semibold">#</th>
                <th className="text-start px-4 py-3 font-semibold">{isArabic ? 'الاسم' : 'Contributor'}</th>
                <th className="text-start px-4 py-3 font-semibold">{isArabic ? 'المجال' : 'Domain'}</th>
                <th className="text-start px-4 py-3 font-semibold">{isArabic ? 'التصنيف' : 'Rating'}</th>
                <th className="text-start px-4 py-3 font-semibold">{strings.pointsLabel}</th>
                <th className="text-start px-4 py-3 font-semibold">{isArabic ? 'أدلة موثّقة' : 'Verified'}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#D5E0EC]/80 dark:divide-[#1C395E]">
              {rows.map((row) => (
                <tr key={row.name} className="hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F]/70 transition">
                  <td className="px-4 py-3 font-bold tabular-nums text-[#0B1F38] dark:text-[#F1F7FC]">{row.rank}</td>
                  <td className="px-4 py-3 font-semibold text-[#0B1F38] dark:text-[#F1F7FC]">
                    {row.name}
                    <span className="block text-[11px] font-normal text-[#4C637C] dark:text-[#93ADC8]">
                      {row.country} · {row.level}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-[#4C637C] dark:text-[#93ADC8]">{row.domain}</td>
                  <td className="px-4 py-3 font-bold text-[#008CAE] dark:text-[#7EE8F5] tabular-nums">{row.rating}</td>
                  <td className="px-4 py-3 font-semibold tabular-nums text-[#0B1F38] dark:text-[#F1F7FC]">{row.points}</td>
                  <td className="px-4 py-3 tabular-nums text-[#0B1F38] dark:text-[#F1F7FC]">{row.verifiedCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </StateWrapper>
  );
};
