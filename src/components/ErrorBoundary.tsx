import React from 'react';

interface ErrorBoundaryState {
  hasError: boolean;
  errorMessage: string;
}

export class ErrorBoundary extends React.Component<{ children: React.ReactNode }, ErrorBoundaryState> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false, errorMessage: '' };
  }

  static getDerivedStateFromError(error: unknown): ErrorBoundaryState {
    const raw = error instanceof Error ? error.message : String(error);
    let friendly = raw;
    try {
      const parsed = JSON.parse(raw) as { error?: string; operationType?: string; path?: string };
      if (parsed.error) {
        friendly = `${parsed.error}${parsed.path ? ` (${parsed.operationType} on ${parsed.path})` : ''}`;
      }
    } catch {
      // Plain error string
    }
    return { hasError: true, errorMessage: friendly };
  }

  componentDidCatch(error: unknown, errorInfo: React.ErrorInfo) {
    console.error('SkillBridge ErrorBoundary caught:', error, errorInfo.componentStack);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-[#F7FAFC] dark:bg-[#061120] p-6">
          <div className="max-w-lg w-full rounded-2xl border border-[#D5E0EC] dark:border-[#1C395E] bg-white dark:bg-[#0D1E36] p-6 space-y-4 shadow-[0_20px_50px_-32px_rgba(11,31,56,0.22)]">
            <h1 className="font-display text-xl font-bold text-[#0B1F38] dark:text-[#F1F7FC]">
              Something went wrong in SkillBridge
            </h1>
            <p className="text-sm text-[#4C637C] dark:text-[#93ADC8] break-words">
              {this.state.errorMessage || 'An unexpected error occurred while synchronizing data.'}
            </p>
            <button
              type="button"
              onClick={() => {
                this.setState({ hasError: false, errorMessage: '' });
                window.location.reload();
              }}
              className="sb-btn-interactive px-4 py-2 rounded-xl bg-[#00B4DB] hover:bg-[#008CAE] text-white text-sm font-semibold transition"
            >
              Reload SkillBridge
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}
