// src/firebase/authService.js
import { 
  signInWithPopup, 
  signInWithRedirect, 
  getRedirectResult, 
  signOut, 
  onAuthStateChanged 
} from "firebase/auth";
import { auth, googleProvider } from "./firebaseConfig";

/**
 * Transforms Firebase User credential into a clean profile object
 */
export const formatUserProfile = (user) => {
  if (!user) return null;
  return {
    uid: user.uid,
    name: user.displayName || user.email?.split("@")[0] || "User",
    email: user.email || "",
    photoUrl: user.photoURL || "",
    emailVerified: user.emailVerified,
    provider: user.providerData?.[0]?.providerId === "google.com" ? "Google" : "Email"
  };
};

/**
 * Initiates Google Sign-In with safe Popup flow and fallback to Redirect
 * @returns {Promise<{user: object|null, error: string|null}>}
 */
export const loginWithGoogle = async () => {
  try {
    const result = await signInWithPopup(auth, googleProvider);
    const userProfile = formatUserProfile(result.user);
    return { user: userProfile, error: null };
  } catch (error) {
    // If popup was blocked by browser or closed before completion, fallback to redirect
    if (error.code === "auth/popup-blocked" || error.code === "auth/popup-closed-by-user") {
      console.warn("Popup blocked or closed by user, attempting redirect flow...");
      try {
        await signInWithRedirect(auth, googleProvider);
        return { user: null, error: null };
      } catch (redirectError) {
        return { user: null, error: redirectError.message };
      }
    }
    return { user: null, error: error.message };
  }
};

/**
 * Handles redirect authentication resolution when page loads
 */
export const handleRedirectResult = async () => {
  try {
    const result = await getRedirectResult(auth);
    if (result && result.user) {
      return formatUserProfile(result.user);
    }
    return null;
  } catch (error) {
    console.error("Redirect sign-in resolution error:", error);
    return null;
  }
};

/**
 * Listens for real-time authentication session state changes
 * @param {Function} callback Callback receiving user profile or null
 * @returns {Function} Unsubscribe function
 */
export const subscribeToAuthChanges = (callback) => {
  return onAuthStateChanged(auth, (user) => {
    callback(formatUserProfile(user));
  });
};

/**
 * Logs out the current user and clears session state
 */
export const logoutUser = async () => {
  try {
    await signOut(auth);
    return { success: true, error: null };
  } catch (error) {
    return { success: false, error: error.message };
  }
};
