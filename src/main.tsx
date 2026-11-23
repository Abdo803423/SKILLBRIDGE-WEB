import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { ErrorBoundary } from './components/ErrorBoundary';
import { SkillBridgeProvider } from './context/SkillBridgeContext';
import './index.css';

// Clean up any legacy service worker from earlier PWA builds so browsers always load fresh website assets.
if (typeof window !== 'undefined' && 'serviceWorker' in navigator) {
  navigator.serviceWorker
    .getRegistrations()
    .then((registrations) => Promise.all(registrations.map((reg) => reg.unregister())))
    .catch(() => {});
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
