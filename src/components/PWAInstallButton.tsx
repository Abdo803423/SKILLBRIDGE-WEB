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
      <span className="inline-flex items-center gap-1.5 text-xs font-medium text-[#008CAE] dark:text-[#7EE8F5] whitespace-nowrap shrink-0">
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
            ? 'sb-btn-interactive inline-flex items-center gap-2 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] px-4 py-2.5 text-sm font-semibold text-white transition whitespace-nowrap shrink-0'
            : 'sb-btn-interactive inline-flex items-center gap-1.5 rounded-lg border border-[#00B4DB]/40 bg-[#00B4DB]/10 hover:bg-[#00B4DB]/20 px-3 py-1.5 text-xs font-semibold text-[#008CAE] dark:text-[#7EE8F5] transition whitespace-nowrap shrink-0'
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
              ? 'sb-btn-interactive inline-flex items-center gap-2 rounded-xl bg-[#0B1F38] dark:bg-[#00B4DB] px-4 py-2.5 text-sm font-semibold text-white dark:text-[#061120] transition whitespace-nowrap shrink-0'
              : 'sb-btn-interactive inline-flex items-center gap-1.5 rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] px-3 py-1.5 text-xs font-medium text-[#0B1F38] dark:text-[#F1F7FC] hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F] transition whitespace-nowrap shrink-0'
          }
        >
          <Smartphone className="w-4 h-4" />
          {isArabic ? 'التثبيت على iOS' : 'Install on iOS'}
        </button>

        {showIOSGuide && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/55 backdrop-blur-xs p-4">
            <div className="sb-route-enter w-full max-w-sm rounded-2xl bg-white dark:bg-[#0D1E36] border border-[#D5E0EC] dark:border-[#1C395E] p-6 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="font-display text-base font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
                  {isArabic ? 'التثبيت على iPhone / iPad' : 'Install on iPhone / iPad'}
                </h3>
                <button
                  type="button"
                  onClick={() => setShowIOSGuide(false)}
                  className="p-1.5 rounded-lg text-[#4C637C] dark:text-[#93ADC8] hover:bg-[#F0F6FA] dark:hover:bg-[#132D4F]"
                  aria-label="Close"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
              <ol className="text-sm text-[#4C637C] dark:text-[#93ADC8] space-y-2 list-decimal list-inside">
                <li>{isArabic ? 'اضغط زر المشاركة (Share) في شريط Safari.' : 'Tap the Share button in Safari.'}</li>
                <li>{isArabic ? 'مرّر للأسفل واختر "إضافة إلى الشاشة الرئيسية".' : 'Scroll down and tap "Add to Home Screen".'}</li>
                <li>{isArabic ? 'افتح SkillBridge كتطبيق مستقل متزامن لحظيًا.' : 'Launch SkillBridge as a standalone synced app.'}</li>
              </ol>
              <button
                type="button"
                onClick={() => setShowIOSGuide(false)}
                className="sb-btn-interactive w-full rounded-xl bg-[#00B4DB] py-2.5 text-sm font-semibold text-white hover:bg-[#008CAE] transition"
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
        className={
          variant === 'primary'
            ? 'sb-btn-interactive inline-flex items-center gap-2 rounded-xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] px-4 py-2 text-xs font-semibold text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB] dark:hover:border-[#7EE8F5] transition whitespace-nowrap shrink-0'
            : 'sb-btn-interactive inline-flex items-center gap-1.5 rounded-lg border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] px-3 py-1.5 text-xs font-medium text-[#0B1F38] dark:text-[#F1F7FC] hover:border-[#00B4DB] transition whitespace-nowrap shrink-0'
        }
      >
        <Laptop className="w-3.5 h-3.5 text-[#00B4DB] dark:text-[#7EE8F5]" />
        {isArabic ? 'تثبيت المنصات' : 'Apps'}
      </button>
    );
  }

  return null;
};
