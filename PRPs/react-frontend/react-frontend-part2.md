# PRP Part 2: Authentication Module (To Be Expanded)

<!-- DEFERRED: Expand this outline into full implementation details when ready to execute -->

**Feature:** Authentication UI Components
**Scope:** ~6 files

---

## 1. Module Overview

This part implements the complete authentication user interface including:
- Login page with form validation
- Navigation bar with user info and logout
- Layout component enhancement

---

## 2. Files to Implement

| # | File Path | Purpose |
|---|-----------|---------|
| 1 | `frontend/src/pages/LoginPage.jsx` | Login form with email/password |
| 2 | `frontend/src/components/Layout.jsx` | Full layout with navigation |
| 3 | `frontend/src/components/Navbar.jsx` | Navigation bar component |
| 4 | `frontend/src/components/Button.jsx` | Reusable button component |
| 5 | `frontend/src/components/Input.jsx` | Reusable input component |
| 6 | `frontend/src/components/Alert.jsx` | Error/success message display |

---

## 3. High-Level Requirements

### 3.1 Login Page (`LoginPage.jsx`)
- Email input field with validation
- Password input field
- Submit button with loading state
- Error message display for failed login
- Redirect to `/products` on success
- Link to "use demo credentials" for testing

### 3.2 Layout Component (`Layout.jsx`)
- Navigation bar at top
- Main content area with `<Outlet />`
- Footer (optional, minimal)
- Responsive container

### 3.3 Navbar Component (`Navbar.jsx`)
- Logo/brand text "GPU Store"
- Navigation links: Products, My Orders
- User email display (from auth context)
- Logout button
- Mobile-responsive menu (hamburger)

### 3.4 Reusable Components
- **Button:** Primary/secondary variants, loading state, disabled state
- **Input:** Label, error message, focus states
- **Alert:** Success/error/warning variants, dismissible

---

## 4. API Interactions

```javascript
// Login
POST /api/auth/login
Body: { email: string, password: string }
Response: { token: string, type: "Bearer", expiresIn: number }
```

---

## 5. User Flow

```
[User visits /products]
  → [Not authenticated]
  → [Redirect to /login]
  → [User enters credentials]
  → [Submit form]
  → [Success: Store token, redirect to /products]
  → [Error: Show error message]
```

---

## 6. Styling Guidelines

- Use Tailwind CSS utility classes
- Color scheme: Blue primary, Gray neutrals
- Responsive breakpoints: sm (640px), md (768px), lg (1024px)
- Form inputs: Rounded borders, focus ring
- Buttons: Rounded, hover states, disabled opacity

---

## 7. Validation Gates

```bash
# Manual testing steps:
1. npm run dev
2. Navigate to /login
3. Enter invalid credentials → See error
4. Enter valid credentials → Redirect to /products
5. Check localStorage for token
6. Refresh page → Stay logged in
7. Click logout → Redirect to /login
```

---

## 8. Dependencies on Part 1

- `AuthContext` and `useAuth` hook
- `api/axios.js` configured instance
- `ProtectedRoute` component
- Tailwind CSS setup

---

## 9. Error Handling

| Scenario | UI Response |
|----------|-------------|
| Invalid credentials | Show "Invalid email or password" alert |
| Network error | Show "Unable to connect to server" alert |
| Empty fields | Show inline validation errors |
| Server error (500) | Show "Something went wrong" alert |
