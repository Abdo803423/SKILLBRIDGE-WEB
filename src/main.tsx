import React from 'react';
import ReactDOM from 'react-dom/client';
import { registerSW } from 'virtual:pwa-register';
import App from './App';
import { ErrorBoundary } from './components/ErrorBoundary';
import { SkillBridgeProvider } from './context/SkillBridgeContext';
import './index.css';

if (typeof window !== 'undefined' && 'serviceWorker' in navigator) {
  registerSW({ immediate: true });
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ErrorBoundary>
      <SkillBridgeProvider>
        <App />
      </SkillBridgeProvider>
    </ErrorBoundary>
  </React.StrictMode>,
);
