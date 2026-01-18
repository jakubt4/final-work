# PRP Part 1: React Frontend - Setup & Core Infrastructure (FULL DETAIL)

**Feature:** Minimalist React Frontend ("UI Client") for GPU E-commerce Platform
**Confidence Score:** 9/10

---

## 1. Context & Background

### 1.1 Project State
- **Backend:** Fully implemented Spring Boot 3.4.1 with JWT authentication
- **Frontend:** Vite + React 19 initialized (basic template only)
- **Goal:** Create a lightweight "Disposable UI" to test User Journeys (Auth → Browse → Order)

### 1.2 Technology Stack (Already Installed)
| Package | Version | Purpose |
|---------|---------|---------|
| react | ^19.2.0 | UI Framework |
| react-dom | ^19.2.0 | DOM Rendering |
| react-router-dom | ^7.12.0 | Client-side routing |
| axios | ^1.13.2 | HTTP Client |
| vite | ^7.2.4 | Build tool & dev server |

### 1.3 Documentation References
- [Tailwind CSS v4 + Vite Setup](https://tailwindcss.com/docs)
- [Axios Interceptors for JWT](https://mihai-andrei.com/blog/jwt-authentication-using-axios-interceptors/)
- [React Router v7 Protected Routes](https://blog.logrocket.com/authentication-react-router-v7)
- [React Router v7 Private Routes](https://www.robinwieruch.de/react-router-private-routes/)

### 1.4 Backend API Contracts (Reference)

**Auth Endpoint:**
```
POST /api/auth/login
Request:  { "email": "...", "password": "..." }
Response: { "token": "...", "type": "Bearer", "expiresIn": 86400000 }
```

**Product Endpoint:**
```
GET /api/products (Protected)
Response: [{ "id": 1, "name": "RTX 4090", "description": "...", "price": 1599.99, "stock": 15, "createdAt": "...", "updatedAt": "..." }]
```

**Order Endpoint:**
```
POST /api/orders (Protected)
Request:  { "items": [{ "productId": 1, "quantity": 1 }] }
Response: { "id": 1, "userId": 1, "total": 1599.99, "status": "PENDING", "items": [...], "createdAt": "...", "updatedAt": "..." }

GET /api/orders (Protected)
Response: [{ "id": 1, "userId": 1, "total": 1599.99, "status": "PENDING", "items": [...], ... }]
```

---

## 2. Implementation Blueprint - Part 1

### 2.1 Scope for Part 1
This part covers **7 files** focused on setup and core infrastructure:

| # | File Path | Purpose |
|---|-----------|---------|
| 1 | `frontend/vite.config.js` | Vite config with proxy & Tailwind |
| 2 | `frontend/src/index.css` | Tailwind CSS imports |
| 3 | `frontend/src/api/axios.js` | Axios instance with interceptors |
| 4 | `frontend/src/context/AuthContext.jsx` | Authentication state management |
| 5 | `frontend/src/components/ProtectedRoute.jsx` | Route guard component |
| 6 | `frontend/src/App.jsx` | Router setup & layout |
| 7 | `frontend/src/main.jsx` | App entry with providers |

---

## 3. Task-by-Task Implementation

### Task 1: Install Tailwind CSS v4

**Terminal Commands:**
```bash
cd frontend
npm install -D @tailwindcss/vite tailwindcss
```

**Validation:** `npm list @tailwindcss/vite` shows installed version

---

### Task 2: Configure Vite with Proxy & Tailwind

**File:** `frontend/vite.config.js`

**Pseudocode:**
```javascript
// 1. Import Vite defineConfig
// 2. Import React plugin
// 3. Import Tailwind CSS plugin
// 4. Export config with:
//    - plugins: [react(), tailwindcss()]
//    - server.proxy: '/api' -> 'http://localhost:8080'
```

**Full Implementation:**
```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
```

**Key Points:**
- Proxy all `/api/*` requests to backend on port 8080
- `changeOrigin: true` ensures correct Host header
- No need to configure CORS on frontend (proxy handles it)

---

### Task 3: Setup Tailwind CSS

**File:** `frontend/src/index.css`

**Full Implementation:**
```css
@import "tailwindcss";
```

**Note:** Tailwind CSS v4 uses the simplified `@import "tailwindcss"` syntax instead of the old `@tailwind base/components/utilities` directives.

---

### Task 4: Create Axios Instance with JWT Interceptors

**File:** `frontend/src/api/axios.js`

**Pseudocode:**
```javascript
// 1. Import axios
// 2. Create axios instance with baseURL '/api'
// 3. Request interceptor:
//    - Get token from localStorage
//    - If token exists, add Authorization header
// 4. Response interceptor:
//    - On 401 error, clear token and redirect to login
// 5. Export instance
```

**Full Implementation:**
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

**Key Points:**
- `baseURL: '/api'` works with Vite proxy
- Token stored in localStorage (acceptable for this "disposable UI")
- 401 response triggers automatic logout and redirect
- Do NOT use this axios instance for the token refresh endpoint (would cause infinite loop)

---

### Task 5: Create Authentication Context

**File:** `frontend/src/context/AuthContext.jsx`

**Pseudocode:**
```javascript
// 1. Create AuthContext with React.createContext()
// 2. Create AuthProvider component:
//    - State: user (null or object), token (null or string), loading (boolean)
//    - On mount: check localStorage for existing token
//    - login(email, password): POST to /auth/login, store token, set user
//    - logout(): clear localStorage, reset state
//    - isAuthenticated: derived from token existence
// 3. Export context and provider
```

**Full Implementation:**
```javascript
import { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  // Initialize auth state from localStorage
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');

    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    const { token: authToken } = response.data;

    // Store token
    localStorage.setItem('token', authToken);
    setToken(authToken);

    // Decode user info from token (basic extraction)
    // For this simple UI, we just store email
    const userData = { email };
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);

    return response.data;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  const value = {
    user,
    token,
    loading,
    isAuthenticated: !!token,
    login,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export default AuthContext;
```

**Key Points:**
- `loading` state prevents flash of login page on refresh
- Token persistence via localStorage
- Simple user object (just email) - sufficient for this UI
- Custom `useAuth` hook for convenient access

---

### Task 6: Create Protected Route Component

**File:** `frontend/src/components/ProtectedRoute.jsx`

**Pseudocode:**
```javascript
// 1. Import useAuth hook and Navigate from react-router-dom
// 2. Check loading state - show spinner if loading
// 3. Check isAuthenticated - redirect to /login if not
// 4. Render children (Outlet) if authenticated
```

**Full Implementation:**
```javascript
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute() {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
```

**Key Points:**
- Uses `<Outlet />` for nested route rendering (React Router v7 pattern)
- Loading spinner while checking auth state
- `replace` prop prevents login page from being in history

---

### Task 7: Setup Router in App.jsx

**File:** `frontend/src/App.jsx`

**Pseudocode:**
```javascript
// 1. Import BrowserRouter, Routes, Route from react-router-dom
// 2. Import page components (Login, Products, Orders)
// 3. Import ProtectedRoute
// 4. Define routes:
//    - /login (public)
//    - / (redirect to /products)
//    - /products (protected)
//    - /orders (protected)
// 5. Include basic layout (nav, etc.)
```

**Full Implementation:**
```javascript
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import ProductsPage from './pages/ProductsPage';
import OrdersPage from './pages/OrdersPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<LoginPage />} />

        {/* Protected routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            <Route path="/" element={<Navigate to="/products" replace />} />
            <Route path="/products" element={<ProductsPage />} />
            <Route path="/orders" element={<OrdersPage />} />
          </Route>
        </Route>

        {/* Catch-all redirect */}
        <Route path="*" element={<Navigate to="/products" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

**Key Points:**
- Nested route structure: ProtectedRoute wraps Layout which wraps pages
- Root `/` redirects to `/products`
- Catch-all route handles unknown paths

---

### Task 8: Update main.jsx with Providers

**File:** `frontend/src/main.jsx`

**Full Implementation:**
```javascript
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { AuthProvider } from './context/AuthContext';
import App from './App';
import './index.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </StrictMode>
);
```

**Key Points:**
- AuthProvider wraps entire app
- CSS import includes Tailwind

---

## 4. Placeholder Components (Stubs for Part 1)

These files need to exist for the app to compile. They will be fully implemented in Parts 2 & 3.

### `frontend/src/components/Layout.jsx` (Stub)
```javascript
import { Outlet } from 'react-router-dom';

export default function Layout() {
  return (
    <div className="min-h-screen bg-gray-100">
      <main className="container mx-auto px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
```

### `frontend/src/pages/LoginPage.jsx` (Stub)
```javascript
export default function LoginPage() {
  return <div>Login Page - To be implemented</div>;
}
```

### `frontend/src/pages/ProductsPage.jsx` (Stub)
```javascript
export default function ProductsPage() {
  return <div>Products Page - To be implemented</div>;
}
```

### `frontend/src/pages/OrdersPage.jsx` (Stub)
```javascript
export default function OrdersPage() {
  return <div>Orders Page - To be implemented</div>;
}
```

---

## 5. Folder Structure After Part 1

```
frontend/
├── src/
│   ├── api/
│   │   └── axios.js              # Axios instance with interceptors
│   ├── components/
│   │   ├── Layout.jsx            # App layout wrapper (stub)
│   │   └── ProtectedRoute.jsx    # Route guard
│   ├── context/
│   │   └── AuthContext.jsx       # Auth state management
│   ├── pages/
│   │   ├── LoginPage.jsx         # Login page (stub)
│   │   ├── ProductsPage.jsx      # Products page (stub)
│   │   └── OrdersPage.jsx        # Orders page (stub)
│   ├── App.jsx                   # Router configuration
│   ├── main.jsx                  # Entry point with providers
│   └── index.css                 # Tailwind imports
├── vite.config.js                # Vite + Tailwind + Proxy config
└── package.json                  # Dependencies
```

---

## 6. Validation Gates

### Gate 1: Tailwind Installation
```bash
cd frontend && npm list @tailwindcss/vite
```
**Expected:** Shows `@tailwindcss/vite@x.x.x`

### Gate 2: Development Server
```bash
cd frontend && npm run dev
```
**Expected:** Server starts on http://localhost:5173 without errors

### Gate 3: Build Check
```bash
cd frontend && npm run build
```
**Expected:** Build completes without errors

### Gate 4: Lint Check
```bash
cd frontend && npm run lint
```
**Expected:** No critical errors (warnings acceptable)

---

## 7. Error Handling Strategy

| Error Type | Handling |
|------------|----------|
| Network error | Axios interceptor logs error, shows toast |
| 401 Unauthorized | Auto-logout and redirect to /login |
| 400 Bad Request | Display validation errors from response |
| 500 Server Error | Display generic error message |

---

## 8. Implementation Order

1. Install Tailwind CSS (`npm install -D @tailwindcss/vite tailwindcss`)
2. Update `vite.config.js` (proxy + tailwind plugin)
3. Update `src/index.css` (tailwind import)
4. Create `src/api/axios.js`
5. Create `src/context/AuthContext.jsx`
6. Create `src/components/ProtectedRoute.jsx`
7. Create stub pages (`LoginPage`, `ProductsPage`, `OrdersPage`)
8. Create `src/components/Layout.jsx` (stub)
9. Update `src/App.jsx`
10. Update `src/main.jsx`
11. Run validation gates

---

## 9. Gotchas & Warnings

1. **Tailwind v4 Syntax:** Use `@import "tailwindcss"` NOT `@tailwind base/components/utilities`
2. **Proxy Path:** Must match `/api` exactly - backend endpoints are at `/api/*`
3. **React Router v7:** Uses `<Outlet />` for nested routes, not `children` prop
4. **localStorage Security:** Acceptable for this "disposable UI" but not production-ready
5. **Token Refresh:** Not implemented (24h expiry is sufficient for testing)
6. **CORS:** Handled by Vite proxy - no frontend config needed

---

## Next Parts Overview

- **Part 2:** Authentication Module (Login form, navigation, logout)
- **Part 3:** Product Module (Grid display, Buy button) & Order Module (Order creation, My Orders list)
