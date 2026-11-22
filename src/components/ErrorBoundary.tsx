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

  componentDidCatch(error: unknown) {
    console.error('SkillBridge ErrorBoundary caught:', error);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-[#F7FAFC] dark:bg-slate-950 p-6">
          <div className="max-w-lg w-full rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-6 space-y-4">
            <h1 className="text-xl font-bold text-[#0B1F38] dark:text-white">
              Something went wrong in SkillBridge
            </h1>
            <p className="text-sm text-slate-600 dark:text-slate-300 break-words">
              {this.state.errorMessage || 'An unexpected error occurred while synchronizing data.'}
            </p>
            <button
              type="button"
              onClick={() => {
                this.setState({ hasError: false, errorMessage: '' });
                window.location.reload();
              }}
              className="px-4 py-2 rounded-lg bg-[#00B4DB] text-white text-sm font-semibold hover:bg-[#008CAE] transition"
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
