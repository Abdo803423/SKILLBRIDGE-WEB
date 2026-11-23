import { initializeApp, getApps, getApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider, signInWithPopup, signOut as fbSignOut } from 'firebase/auth';
import {
  getFirestore,
  initializeFirestore,
  doc,
  getDocFromServer,
  type Firestore,
  type FirestoreSettings,
} from 'firebase/firestore';
import bundledFirebaseConfig from '../../firebase-applet-config.json';

const firebaseConfig = {
  ...bundledFirebaseConfig,
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || bundledFirebaseConfig.apiKey,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || bundledFirebaseConfig.authDomain,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || bundledFirebaseConfig.projectId,
  appId: import.meta.env.VITE_FIREBASE_APP_ID || bundledFirebaseConfig.appId,
  firestoreDatabaseId:
    import.meta.env.VITE_FIREBASE_DATABASE_ID || bundledFirebaseConfig.firestoreDatabaseId,
};

const app = getApps().length > 0 ? getApp() : initializeApp(firebaseConfig);

// CRITICAL: Pass the named firestoreDatabaseId from firebase-applet-config.json
// Use XHR long-polling (useFetchStreams: false + experimentalForceLongPolling: true) so
// WebChannel requests stay responsive across proxies and strict browser environments.
export const db: Firestore = (() => {
  try {
    const settings: FirestoreSettings & { useFetchStreams?: boolean } = {
      experimentalForceLongPolling: true,
      useFetchStreams: false,
    };
    return initializeFirestore(app, settings, firebaseConfig.firestoreDatabaseId);
  } catch {
    return getFirestore(app, firebaseConfig.firestoreDatabaseId);
  }
})();
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({ prompt: 'select_account' });

export enum OperationType {
  CREATE = 'create',
  UPDATE = 'update',
  DELETE = 'delete',
  LIST = 'list',
  GET = 'get',
  WRITE = 'write',
}

export interface FirestoreErrorInfo {
  error: string;
  operationType: OperationType;
  path: string | null;
  authInfo: {
    userId?: string | null;
    email?: string | null;
    emailVerified?: boolean | null;
    isAnonymous?: boolean | null;
    tenantId?: string | null;
    providerInfo?: Array<{
      providerId?: string | null;
      email?: string | null;
    }>;
  };
}

export function handleFirestoreError(
  error: unknown,
  operationType: OperationType,
  path: string | null
): never {
  const errInfo: FirestoreErrorInfo = {
    error: error instanceof Error ? error.message : String(error),
    authInfo: {
      userId: auth.currentUser?.uid ?? null,
      email: auth.currentUser?.email ?? null,
      emailVerified: auth.currentUser?.emailVerified ?? null,
      isAnonymous: auth.currentUser?.isAnonymous ?? null,
      tenantId: auth.currentUser?.tenantId ?? null,
      providerInfo:
        auth.currentUser?.providerData?.map((provider) => ({
          providerId: provider.providerId,
          email: provider.email,
        })) ?? [],
    },
    operationType,
    path,
  };
  console.error('Firestore Error: ', JSON.stringify(errInfo));
  throw new Error(JSON.stringify(errInfo));
}

export async function testConnection(): Promise<boolean> {
  try {
    await getDocFromServer(doc(db, 'test', 'connection'));
    return true;
  } catch (error) {
    if (error instanceof Error && error.message.includes('the client is offline')) {
      console.error('Please check your Firebase configuration.');
      return false;
    }
    return true;
  }
}

export async function signInWithGooglePopup() {
  return signInWithPopup(auth, googleProvider);
}

export async function signOutFirebase() {
  return fbSignOut(auth);
}
