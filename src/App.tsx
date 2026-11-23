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
import { PlatformOverviewScreen, WelcomeScreen } from './screens/WelcomeAndPlatformScreens';

export const App: React.FC = () => {
  const { route, session, isGuestPreview, authReady } = useSkillBridge();

  if (route === 'welcome') {
    return <WelcomeScreen />;
  }

  if (route === 'platform' && !session && !isGuestPreview) {
    return <PlatformOverviewScreen />;
  }

  if (!authReady && !session && !isGuestPreview) {
    return (
      <div className="min-h-screen bg-[#F7FAFC] dark:bg-[#061120] flex items-center justify-center">
        <div className="text-center space-y-3">
          <div className="relative w-10 h-10 mx-auto">
            <div className="absolute inset-0 rounded-full border-2 border-[#00B4DB]/25" />
            <div className="absolute inset-0 rounded-full border-2 border-[#00B4DB] border-t-transparent animate-spin" />
          </div>
          <p className="font-display text-sm font-semibold tracking-tight text-[#0B1F38] dark:text-[#7EE8F5]">
            SkillBridge
          </p>
        </div>
      </div>
    );
  }

  if (!session && !isGuestPreview) {
    return <WelcomeScreen />;
  }

  const role = session?.role ?? 'STUDENT';

  const renderRoleView = () => {
    if (route === 'platform') return <PlatformOverviewScreen />;
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
      <div key={`${role}_${route}`} className="sb-route-enter">
        {renderRoleView()}
      </div>
      {role === 'STUDENT' && <AiAgentDrawer />}
    </AppShell>
  );
};

export default App;
