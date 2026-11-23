export type ActiveRoute =
  | 'welcome'
  | 'platform'
  | 'home'
  | 'discover'
  | 'challengeDetail'
  | 'contests'
  | 'reverseEng'
  | 'community'
  | 'postDetail'
  | 'teams'
  | 'teamWorkspace'
  | 'passport'
  | 'analysis'
  | 'badges'
  | 'wallet'
  | 'leaderboard'
  | 'briefs'
  | 'coach'
  | 'search'
  | 'notifications'
  | 'blueprint'
  | 'settings'
  | 'adminHome'
  | 'adminQueues'
  | 'adminUsers'
  | 'adminLogs'
  | 'companyHome'
  | 'companyBriefs'
  | 'companyApplicants'
  | 'companyTalent'
  | 'companyAnalytics'
  | 'directorHome'
  | 'directorStudents'
  | 'directorReviews'
  | 'directorHistory'
  | 'directorProjects';

const ROUTE_TO_PATH: Record<ActiveRoute, string> = {
  welcome: '/',
  platform: '/platform',
  home: '/home',
  discover: '/discover',
  challengeDetail: '/challenge',
  contests: '/contests',
  reverseEng: '/labs',
  community: '/community',
  postDetail: '/post',
  teams: '/teams',
  teamWorkspace: '/team',
  passport: '/passport',
  analysis: '/analysis',
  badges: '/badges',
  wallet: '/wallet',
  leaderboard: '/leaderboard',
  briefs: '/briefs',
  coach: '/coach',
  search: '/search',
  notifications: '/notifications',
  blueprint: '/architecture',
  settings: '/settings',
  adminHome: '/admin',
  adminQueues: '/admin/queues',
  adminUsers: '/admin/users',
  adminLogs: '/admin/logs',
  companyHome: '/company',
  companyBriefs: '/company/briefs',
  companyApplicants: '/company/applicants',
  companyTalent: '/company/talent',
  companyAnalytics: '/company/analytics',
  directorHome: '/director',
  directorStudents: '/director/tasks',
  directorReviews: '/director/reviews',
  directorHistory: '/director/history',
  directorProjects: '/director/projects',
};

const PATH_TO_ROUTE: Record<string, ActiveRoute> = Object.entries(ROUTE_TO_PATH).reduce(
  (acc, [route, path]) => {
    acc[path] = route as ActiveRoute;
    return acc;
  },
  {} as Record<string, ActiveRoute>,
);

// Legacy aliases so old bookmarks remain graceful
PATH_TO_ROUTE['/downloads'] = 'platform';

function getNormalizedBase(): string {
  const base = import.meta.env.BASE_URL || '/';
  if (base === '/') return '';
  return base.endsWith('/') ? base.slice(0, -1) : base;
}

export function routeToUrl(route: ActiveRoute): string {
  const base = getNormalizedBase();
  const path = ROUTE_TO_PATH[route] || '/';
  return base ? `${base}${path === '/' ? '/' : path}` : path;
}

export function urlToRoute(pathname: string): ActiveRoute {
  const base = getNormalizedBase();
  let clean = pathname || '/';
  if (base && clean.startsWith(base)) {
    clean = clean.slice(base.length) || '/';
  }
  if (clean.length > 1 && clean.endsWith('/')) {
    clean = clean.slice(0, -1);
  }
  return PATH_TO_ROUTE[clean] ?? 'welcome';
}

export function isBrowseableRoute(route: ActiveRoute): boolean {
  return ![
    'welcome',
    'platform',
    'adminHome',
    'adminQueues',
    'adminUsers',
    'adminLogs',
    'companyHome',
    'companyBriefs',
    'companyApplicants',
    'companyTalent',
    'companyAnalytics',
    'directorHome',
    'directorStudents',
    'directorReviews',
    'directorHistory',
    'directorProjects',
  ].includes(route);
}
