import { useEffect, useState } from 'react';

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed'; platform: string }>;
}

export type PlatformKind = 'MOBILE_ANDROID' | 'MOBILE_IOS' | 'TABLET' | 'DESKTOP';

export interface DetectedPlatformInfo {
  kind: PlatformKind;
  osLabel: string;
  recommendedTarget: 'MOBILE' | 'DESKTOP' | 'WEB';
  deviceLabel: string;
  windowWidth: number;
}

export function detectDevicePlatform(): DetectedPlatformInfo {
  if (typeof window === 'undefined' || typeof navigator === 'undefined') {
    return {
      kind: 'DESKTOP',
      osLabel: 'Desktop Browser',
      recommendedTarget: 'DESKTOP',
      deviceLabel: 'Web Desktop Session',
      windowWidth: 1280,
    };
  }

  const ua = navigator.userAgent || '';
  const lowUa = ua.toLowerCase();
  const width = window.innerWidth || 1280;

  const isIPhone = /iphone|ipod/.test(lowUa);
  const isIPad = /ipad/.test(lowUa) || (lowUa.includes('macintosh') && navigator.maxTouchPoints > 1);
  const isAndroid = /android/.test(lowUa);
  const isAndroidMobile = isAndroid && /mobile/.test(lowUa);
  const isAndroidTablet = isAndroid && !/mobile/.test(lowUa);

  if (isIPhone) {
    return {
      kind: 'MOBILE_IOS',
      osLabel: 'iOS (iPhone)',
      recommendedTarget: 'MOBILE',
      deviceLabel: 'iPhone Mobile Client',
      windowWidth: width,
    };
  }
  if (isAndroidMobile || (!isIPad && !isAndroidTablet && width < 640 && 'ontouchstart' in window)) {
    return {
      kind: 'MOBILE_ANDROID',
      osLabel: isAndroid ? 'Android Phone' : 'Mobile Smartphone',
      recommendedTarget: 'MOBILE',
      deviceLabel: 'Android Mobile Client',
      windowWidth: width,
    };
  }
  if (isIPad || isAndroidTablet || (width >= 640 && width < 1024 && navigator.maxTouchPoints > 1)) {
    return {
      kind: 'TABLET',
      osLabel: isIPad ? 'iPadOS Tablet' : 'Tablet',
      recommendedTarget: 'WEB',
      deviceLabel: 'Tablet Client',
      windowWidth: width,
    };
  }

  let osLabel = 'Desktop / Laptop';
  if (lowUa.includes('win')) osLabel = 'Windows Desktop';
  else if (lowUa.includes('mac')) osLabel = 'macOS Desktop';
  else if (lowUa.includes('linux')) osLabel = 'Linux Desktop';

  return {
    kind: 'DESKTOP',
    osLabel,
    recommendedTarget: 'DESKTOP',
    deviceLabel: `${osLabel} Client`,
    windowWidth: width,
  };
}

export function usePWAInstall() {
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null);
  const [isInstalled, setIsInstalled] = useState(false);
  const [isIOS, setIsIOS] = useState(false);

  useEffect(() => {
    const isStandalone =
      window.matchMedia('(display-mode: standalone)').matches ||
      (window.navigator as unknown as { standalone?: boolean }).standalone === true;
    setIsInstalled(isStandalone);

    const ua = window.navigator.userAgent.toLowerCase();
    setIsIOS(/iphone|ipad|ipod/.test(ua));

    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault();
      setDeferredPrompt(e as BeforeInstallPromptEvent);
    };

    const handleAppInstalled = () => {
      setIsInstalled(true);
      setDeferredPrompt(null);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    window.addEventListener('appinstalled', handleAppInstalled);

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
      window.removeEventListener('appinstalled', handleAppInstalled);
    };
  }, []);

  const install = async (): Promise<boolean> => {
    if (!deferredPrompt) return false;
    await deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    if (outcome === 'accepted') {
      setIsInstalled(true);
      setDeferredPrompt(null);
      return true;
    }
    return false;
  };

  return {
    isInstallable: Boolean(deferredPrompt),
    isInstalled,
    isIOS,
    install,
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
