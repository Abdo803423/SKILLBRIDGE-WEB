import React from 'react';
import { AiAgentDrawer } from './components/AiAgentDrawer';
import { AppShell } from './components/AppShell';
import { useSkillBridge } from './context/SkillBridgeContext';
import {
  BadgesGalleryScreen,
  CommunityFeedScreen,
  ExperiencePassportScreen,
  LeaderboardScreen,
  PointsWalletScreen,
  PostDetailScreen,
  TeamsHubScreen,
  TeamWorkspaceScreen,
} from './screens/CommunityTeamsPassportScreens';
import {
  ChallengeDetailScreen,
  ContestsScreen,
  DiscoverScreen,
  ReverseEngHubScreen,
} from './screens/DiscoverContestsLabsScreens';
import {
  RoleAdminDashboardScreen,
  RoleCompanyDashboardScreen,
  RoleDirectorDashboardScreen,
} from './screens/RolePortalsScreens';
import {
  AiCoachScreen,
  SkillAnalysisScreen,
  StudentBriefsScreen,
  StudentHomeScreen,
} from './screens/StudentWorkspaceScreens';
import {
  AccountAndPrivacySettingsScreen,
  ArchitectureBlueprintScreen,
  GlobalSearchScreen,
  NotificationsScreen,
} from './screens/UtilitySettingsScreens';
import { DownloadCenterScreen, WelcomeScreen } from './screens/WelcomeAndDownloadsScreens';

export const App: React.FC = () => {
  const { route, session, isGuestPreview, authReady } = useSkillBridge();

  if (!authReady) {
    return (
      <div className="min-h-screen bg-[#F7FAFC] dark:bg-slate-950 flex items-center justify-center">
        <div className="text-center space-y-2">
          <div className="w-8 h-8 rounded-full border-2 border-[#00B4DB] border-t-transparent animate-spin mx-auto" />
          <p className="text-xs font-semibold text-slate-500">SkillBridge</p>
        </div>
      </div>
    );
  }

  if (route === 'welcome' || (!session && !isGuestPreview && route !== 'downloads')) {
    return <WelcomeScreen />;
  }

  if (route === 'downloads' && !session && !isGuestPreview) {
    return <DownloadCenterScreen />;
  }

  const role = session?.role ?? 'STUDENT';

  const renderRoleView = () => {
    if (route === 'downloads') return <DownloadCenterScreen />;
    if (route === 'notifications') return <NotificationsScreen />;
    if (route === 'settings') return <AccountAndPrivacySettingsScreen />;
    if (route === 'blueprint') return <ArchitectureBlueprintScreen />;

    // Isolated Role Portals (Strict Role Guard)
    if (role === 'PLATFORM_ADMIN') {
      return <RoleAdminDashboardScreen activeSubRoute={route} />;
    }
    if (role === 'COMPANY') {
      return <RoleCompanyDashboardScreen activeSubRoute={route} />;
    }
    if (role === 'COMPANY_DIRECTOR') {
      return <RoleDirectorDashboardScreen activeSubRoute={route} />;
    }

    // Student / Guest Routes
    switch (route) {
      case 'discover':
        return <DiscoverScreen />;
      case 'challengeDetail':
        return <ChallengeDetailScreen />;
      case 'contests':
        return <ContestsScreen />;
      case 'reverseEng':
        return <ReverseEngHubScreen />;
      case 'community':
        return <CommunityFeedScreen />;
      case 'postDetail':
        return <PostDetailScreen />;
      case 'teams':
        return <TeamsHubScreen />;
      case 'teamWorkspace':
        return <TeamWorkspaceScreen />;
      case 'passport':
        return <ExperiencePassportScreen />;
      case 'analysis':
        return <SkillAnalysisScreen />;
      case 'badges':
        return <BadgesGalleryScreen />;
      case 'wallet':
        return <PointsWalletScreen />;
      case 'leaderboard':
        return <LeaderboardScreen />;
      case 'briefs':
        return <StudentBriefsScreen />;
      case 'coach':
        return <AiCoachScreen />;
      case 'search':
        return <GlobalSearchScreen />;
      default:
        return <StudentHomeScreen />;
    }
  };

  return (
    <AppShell>
      {renderRoleView()}
      {role === 'STUDENT' && <AiAgentDrawer />}
    </AppShell>
  );
};

export default App;
