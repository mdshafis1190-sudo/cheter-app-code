// src/components/GoogleSignInButton.jsx
import React, { useState, useEffect } from "react";
import { 
  loginWithGoogle, 
  logoutUser, 
  subscribeToAuthChanges, 
  handleRedirectResult 
} from "../firebase/authService";

export const GoogleSignInButton = ({ onUserChange }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    // 1. Resolve redirect sign-in if page returned from Google redirect
    handleRedirectResult().then((redirectUser) => {
      if (redirectUser) {
        setUser(redirectUser);
        onUserChange?.(redirectUser);
      }
    });

    // 2. Subscribe to persistent auth state
    const unsubscribe = subscribeToAuthChanges((currentUser) => {
      setUser(currentUser);
      onUserChange?.(currentUser);
    });

    return () => unsubscribe();
  }, [onUserChange]);

  const handleLogin = async () => {
    setLoading(true);
    setError(null);
    const { user: loggedInUser, error: authError } = await loginWithGoogle();
    setLoading(false);

    if (authError) {
      setError(authError);
    } else if (loggedInUser) {
      setUser(loggedInUser);
      onUserChange?.(loggedInUser);
    }
  };

  const handleLogout = async () => {
    await logoutUser();
    setUser(null);
    onUserChange?.(null);
  };

  // If user is logged in, display modern user badge with photo, name, email & UID
  if (user) {
    return (
      <div style={styles.profileCard}>
        {user.photoUrl ? (
          <img 
            src={user.photoUrl} 
            alt={user.name} 
            style={styles.avatar} 
            referrerPolicy="no-referrer"
          />
        ) : (
          <div style={styles.avatarPlaceholder}>
            {user.name.charAt(0).toUpperCase()}
          </div>
        )}
        <div style={styles.profileInfo}>
          <div style={styles.userName}>{user.name}</div>
          <div style={styles.userEmail}>{user.email}</div>
          <div style={styles.userUid}>UID: {user.uid}</div>
        </div>
        <button 
          onClick={handleLogout} 
          style={styles.logoutBtn}
          title="Sign Out"
        >
          Sign Out
        </button>
      </div>
    );
  }

  // Unauthenticated Login Button
  return (
    <div style={styles.container}>
      <button 
        onClick={handleLogin} 
        disabled={loading}
        style={{
          ...styles.googleBtn,
          opacity: loading ? 0.7 : 1,
          cursor: loading ? "wait" : "pointer"
        }}
      >
        <svg style={styles.googleIcon} viewBox="0 0 48 48">
          <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
          <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
          <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
          <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
        </svg>
        <span style={styles.btnText}>
          {loading ? "Connecting..." : "Continue with Google"}
        </span>
      </button>
      {error && <div style={styles.errorText}>{error}</div>}
    </div>
  );
};

const styles = {
  container: {
    display: "inline-flex",
    flexDirection: "column",
    alignItems: "flex-start",
    gap: "6px"
  },
  googleBtn: {
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "12px",
    backgroundColor: "#ffffff",
    color: "#3c4043",
    border: "1px solid #dadce0",
    borderRadius: "24px",
    padding: "10px 22px",
    fontSize: "15px",
    fontWeight: "500",
    boxShadow: "0 2px 4px rgba(0,0,0,0.06)",
    transition: "all 0.2s ease-in-out"
  },
  googleIcon: {
    width: "20px",
    height: "20px",
    flexShrink: 0
  },
  btnText: {
    fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif"
  },
  profileCard: {
    display: "flex",
    alignItems: "center",
    gap: "12px",
    padding: "12px 18px",
    borderRadius: "16px",
    backgroundColor: "#ffffff",
    border: "1px solid #e0e0e0",
    boxShadow: "0 2px 8px rgba(0,0,0,0.04)",
    maxWidth: "420px"
  },
  avatar: {
    width: "44px",
    height: "44px",
    borderRadius: "50%",
    objectFit: "cover",
    border: "2px solid #ea4335"
  },
  avatarPlaceholder: {
    width: "44px",
    height: "44px",
    borderRadius: "50%",
    backgroundColor: "#D32F2F",
    color: "#fff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontWeight: "bold",
    fontSize: "18px"
  },
  profileInfo: {
    display: "flex",
    flexDirection: "column",
    textAlign: "left",
    overflow: "hidden"
  },
  userName: {
    fontWeight: "600",
    fontSize: "14px",
    color: "#1a1a1a"
  },
  userEmail: {
    fontSize: "12px",
    color: "#5f6368",
    whiteSpace: "nowrap",
    textOverflow: "ellipsis",
    overflow: "hidden"
  },
  userUid: {
    fontSize: "10px",
    color: "#80868b",
    fontFamily: "monospace",
    marginTop: "2px"
  },
  logoutBtn: {
    marginLeft: "auto",
    padding: "6px 12px",
    borderRadius: "8px",
    border: "1px solid #e0e0e0",
    backgroundColor: "#f8f9fa",
    color: "#d93025",
    fontSize: "12px",
    fontWeight: "600",
    cursor: "pointer"
  },
  errorText: {
    color: "#d93025",
    fontSize: "12px",
    marginTop: "4px"
  }
};

export default GoogleSignInButton;
