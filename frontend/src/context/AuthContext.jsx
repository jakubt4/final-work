import { createContext, useContext, useState } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

// Initialize auth state from localStorage
function getInitialAuthState() {
  const storedToken = localStorage.getItem('token');
  const storedUser = localStorage.getItem('user');

  if (storedToken && storedUser) {
    return {
      token: storedToken,
      user: JSON.parse(storedUser),
      loading: false,
    };
  }

  return {
    token: null,
    user: null,
    loading: false,
  };
}

export function AuthProvider({ children }) {
  const [authState, setAuthState] = useState(getInitialAuthState);

  const login = async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    const { token: authToken } = response.data;

    // Store token
    localStorage.setItem('token', authToken);

    // For this simple UI, we just store email
    const userData = { email };
    localStorage.setItem('user', JSON.stringify(userData));

    setAuthState({
      token: authToken,
      user: userData,
      loading: false,
    });

    return response.data;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setAuthState({
      token: null,
      user: null,
      loading: false,
    });
  };

  const value = {
    user: authState.user,
    token: authState.token,
    loading: authState.loading,
    isAuthenticated: !!authState.token,
    login,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export default AuthContext;
