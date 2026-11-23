import { useEffect, useState } from 'react';

export type ViewportProfile = 'MOBILE' | 'TABLET' | 'DESKTOP';

export interface BrowserEnvironmentInfo {
  viewportProfile: ViewportProfile;
  osLabel: string;
  browserLabel: string;
  sessionLabel: string;
  windowWidth: number;
}

export function detectBrowserEnvironment(): BrowserEnvironmentInfo {
  if (typeof window === 'undefined' || typeof navigator === 'undefined') {
    return {
      viewportProfile: 'DESKTOP',
      osLabel: 'Desktop',
      browserLabel: 'Modern Browser',
      sessionLabel: 'Desktop Web Browser',
      windowWidth: 1280,
    };
  }

  const ua = navigator.userAgent || '';
  const lowUa = ua.toLowerCase();
  const width = window.innerWidth || 1280;

  let browserLabel = 'Web Browser';
  if (lowUa.includes('edg/')) browserLabel = 'Microsoft Edge';
  else if (lowUa.includes('firefox/')) browserLabel = 'Mozilla Firefox';
  else if (lowUa.includes('chrome/') || lowUa.includes('crios/')) browserLabel = 'Google Chrome';
  else if (lowUa.includes('safari/')) browserLabel = 'Apple Safari';

  let osLabel = 'Desktop';
  if (/iphone|ipod/.test(lowUa)) osLabel = 'iOS';
  else if (/ipad/.test(lowUa) || (lowUa.includes('macintosh') && navigator.maxTouchPoints > 1)) osLabel = 'iPadOS';
  else if (lowUa.includes('android')) osLabel = 'Android';
  else if (lowUa.includes('win')) osLabel = 'Windows';
  else if (lowUa.includes('mac')) osLabel = 'macOS';
  else if (lowUa.includes('linux')) osLabel = 'Linux';

  const viewportProfile: ViewportProfile =
    width < 640 ? 'MOBILE' : width < 1024 ? 'TABLET' : 'DESKTOP';

  return {
    viewportProfile,
    osLabel,
    browserLabel,
    sessionLabel: `${browserLabel} · ${osLabel}`,
    windowWidth: width,
  };
}

export function useOnlineStatus(): boolean {
  const [isOnline, setIsOnline] = useState(
    typeof navigator !== 'undefined' ? navigator.onLine : true,
  );

  useEffect(() => {
    const onOnline = () => setIsOnline(true);
    const onOffline = () => setIsOnline(false);
    window.addEventListener('online', onOnline);
    window.addEventListener('offline', onOffline);
    return () => {
      window.removeEventListener('online', onOnline);
      window.removeEventListener('offline', onOffline);
    };
  }, []);

  return isOnline;
}
