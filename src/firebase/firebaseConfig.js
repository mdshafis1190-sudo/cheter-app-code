// src/firebase/firebaseConfig.js
import { initializeApp, getApps, getApp } from "firebase/app";
import { getAnalytics, isSupported } from "firebase/analytics";
import { 
  getAuth, 
  GoogleAuthProvider, 
  setPersistence, 
  browserLocalPersistence 
} from "firebase/auth";
import { getFirestore } from "firebase/firestore";

// Your web app's Firebase configuration (Project: cheter-7dda1)
const firebaseConfig = {
  apiKey: "AIzaSyAiYXMD-1HSyeoKxDFIZhFj-MZBPX9OY9I",
  authDomain: "cheter-7dda1.firebaseapp.com",
  projectId: "cheter-7dda1",
  storageBucket: "cheter-7dda1.firebasestorage.app",
  messagingSenderId: "142280541265",
  appId: "1:142280541265:web:cf7cb2b796ae37b700e470",
  measurementId: "G-2TPR2YGBQP"
};

// Initialize Firebase App (Singleton Pattern)
const app = !getApps().length ? initializeApp(firebaseConfig) : getApp();

// Optional Analytics (only runs in supported browser environments)
let analytics = null;
if (typeof window !== "undefined") {
  isSupported().then((supported) => {
    if (supported) {
      analytics = getAnalytics(app);
    }
  });
}

// Initialize Firebase Auth
const auth = getAuth(app);

// Configure Google Auth Provider
const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({
  prompt: "select_account" // Always prompt user to pick their Google account
});

// Configure Local Session Persistence (keeps user logged in across page refreshes & tab closes)
setPersistence(auth, browserLocalPersistence).catch((error) => {
  console.error("Firebase persistence setup error:", error);
});

// Initialize Firestore Database (for Menu & Multi-Tenant QR Data)
const db = getFirestore(app);

export { app, auth, googleProvider, db, analytics, firebaseConfig };
