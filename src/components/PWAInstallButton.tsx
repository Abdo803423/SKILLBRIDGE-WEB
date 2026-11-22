import React, { useState } from 'react';
import { Download, Smartphone, Laptop, CheckCircle2, X } from 'lucide-react';
import { usePWAInstall } from '../hooks/usePWAInstall';
import { useSkillBridge } from '../context/SkillBridgeContext';

export const PWAInstallButton: React.FC<{ variant?: 'compact' | 'primary'; onOpenDownloadCenter?: () => void }> = ({
  variant = 'compact',
  onOpenDownloadCenter,
}) => {
  const { isInstallable, isInstalled, isIOS, install } = usePWAInstall();
  const { isArabic, strings, showToast } = useSkillBridge();
  const [showIOSGuide, setShowIOSGuide] = useState(false);

  if (isInstalled) {
    return (
      <span className="inline-flex items-center gap-1.5 text-xs font-medium text-emerald-700 dark:text-emerald-400 whitespace-nowrap shrink-0">
        <CheckCircle2 className="w-3.5 h-3.5" />
        {isArabic ? 'التطبيق مثبّت' : 'App Installed'}
      </span>
    );
  }

  if (isInstallable) {
    return (
      <button
        type="button"
        onClick={async () => {
          const accepted = await install();
          if (accepted) {
            showToast(isArabic ? 'تم تثبيت SkillBridge بنجاح!' : 'SkillBridge installed to your device.');
          }
        }}
        className={
          variant === 'primary'
            ? 'inline-flex items-center gap-2 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2.5 text-sm font-semibold text-white transition whitespace-nowrap shrink-0'
            : 'inline-flex items-center gap-1.5 rounded-lg border border-[#00B4DB]/40 bg-[#00B4DB]/10 hover:bg-[#00B4DB]/20 px-3 py-1.5 text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5] transition whitespace-nowrap shrink-0'
        }
      >
        <Download className="w-4 h-4" />
        {strings.installNowLabel}
      </button>
    );
  }

  if (isIOS) {
    return (
      <>
        <button
          type="button"
          onClick={() => setShowIOSGuide(true)}
          className={
            variant === 'primary'
              ? 'inline-flex items-center gap-2 rounded-xl bg-[#0B1F38] dark:bg-[#00B4DB] px-4 py-2.5 text-sm font-semibold text-white transition whitespace-nowrap shrink-0'
              : 'inline-flex items-center gap-1.5 rounded-lg border border-slate-300 dark:border-slate-700 px-3 py-1.5 text-xs font-medium text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition whitespace-nowrap shrink-0'
          }
        >
          <Smartphone className="w-4 h-4" />
          {isArabic ? 'التثبيت على iOS' : 'Install on iOS'}
        </button>

        {showIOSGuide && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div className="w-full max-w-sm rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 p-6 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-base font-bold text-[#0B1F38] dark:text-white">
                  {isArabic ? 'التثبيت على iPhone / iPad' : 'Install on iPhone / iPad'}
                </h3>
                <button
                  type="button"
                  onClick={() => setShowIOSGuide(false)}
                  className="p-1.5 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800"
                  aria-label="Close"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
              <ol className="text-sm text-slate-600 dark:text-slate-300 space-y-2 list-decimal list-inside">
                <li>{isArabic ? 'اضغط زر المشاركة (Share) في شريط Safari.' : 'Tap the Share button in Safari.'}</li>
                <li>{isArabic ? 'مرّر للأسفل واختر "إضافة إلى الشاشة الرئيسية".' : 'Scroll down and tap "Add to Home Screen".'}</li>
                <li>{isArabic ? 'افتح SkillBridge كتطبيق مستقل متزامن لحظيًا.' : 'Launch SkillBridge as a standalone synced app.'}</li>
              </ol>
              <button
                type="button"
                onClick={() => setShowIOSGuide(false)}
                className="w-full rounded-xl bg-[#00B4DB] py-2.5 text-sm font-semibold text-white hover:bg-[#008CAE] transition"
              >
                {strings.closeLabel}
              </button>
            </div>
          </div>
        )}
      </>
    );
  }

  if (onOpenDownloadCenter) {
    return (
      <button
        type="button"
        onClick={onOpenDownloadCenter}
        className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 dark:border-slate-700 px-3 py-1.5 text-xs font-medium text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition whitespace-nowrap shrink-0"
      >
        <Laptop className="w-3.5 h-3.5" />
        {isArabic ? 'تثبيت المنصات' : 'Apps'}
      </button>
    );
  }

  return null;
};
