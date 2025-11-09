# 🚀 Sprint 3 - Day 1 Quick Start Guide

**Date**: November 8, 2025  
**Focus**: Project Setup & Initial Configuration  
**Epic**: A - Project Setup & Configuration (4 pts)

---

## 🎯 Today's Goals

- [ ] Initialize Next.js 14+ project
- [ ] Install all dependencies
- [ ] Configure Tailwind CSS + shadcn/ui
- [ ] Setup Zustand state management
- [ ] Configure Axios API client
- [ ] Setup environment variables
- [ ] Create basic folder structure
- [ ] Test dev server runs

**Expected Time**: 4-6 hours  
**Story Points**: 4 pts

---

## ⚡ Step-by-Step Instructions

### Step 1: Create Next.js Project (15 min)

```bash
# Navigate to your project root (where backend folder is)
cd e:\final-project

# Create Next.js app
npx create-next-app@latest lexia-web --typescript --tailwind --app

# When prompted, select:
✓ TypeScript: Yes
✓ ESLint: Yes
✓ Tailwind CSS: Yes
✓ src/ directory: Yes
✓ App Router: Yes
✓ Import alias (@/*): Yes

# Navigate into project
cd lexia-web

# Open in VS Code
code .
```

**Verify**: App runs on http://localhost:3000

```bash
npm run dev
```

---

### Step 2: Install Dependencies (10 min)

```bash
# Core dependencies
npm install zustand axios react-hook-form zod lucide-react react-hot-toast recharts

# Date handling
npm install date-fns

# Dev dependencies
npm install -D @testing-library/react @testing-library/jest-dom @testing-library/user-event jest jest-environment-jsdom

# Optional: for better DX
npm install -D @types/node
```

**Verify**: Check package.json has all dependencies

---

### Step 3: Setup shadcn/ui (20 min)

```bash
# Initialize shadcn/ui
npx shadcn-ui@latest init

# When prompted:
✓ Style: Default
✓ Base color: Slate
✓ CSS variables: Yes

# Install commonly used components
npx shadcn-ui@latest add button
npx shadcn-ui@latest add input
npx shadcn-ui@latest add card
npx shadcn-ui@latest add form
npx shadcn-ui@latest add toast
npx shadcn-ui@latest add dialog
npx shadcn-ui@latest add dropdown-menu
npx shadcn-ui@latest add avatar
npx shadcn-ui@latest add badge
npx shadcn-ui@latest add progress
npx shadcn-ui@latest add skeleton
npx shadcn-ui@latest add tabs
```

**Verify**: Check `src/components/ui/` folder exists with components

---

### Step 4: Environment Variables (5 min)

Create `.env.local` in project root:

```env
# API Configuration
NEXT_PUBLIC_API_URL=http://localhost:8088/api/v1
NEXT_PUBLIC_APP_NAME=LEXIA
NEXT_PUBLIC_API_TIMEOUT=30000

# Optional: for development
NEXT_PUBLIC_ENV=development
```

Create `.env.production`:

```env
# API Configuration
NEXT_PUBLIC_API_URL=https://api.lexia.com/api/v1
NEXT_PUBLIC_APP_NAME=LEXIA
NEXT_PUBLIC_API_TIMEOUT=30000
```

**Verify**: Can access `process.env.NEXT_PUBLIC_API_URL` in code

---

### Step 5: Create Folder Structure (15 min)

```bash
# Create folders
mkdir -p src/lib
mkdir -p src/services
mkdir -p src/store
mkdir -p src/types
mkdir -p src/hooks
mkdir -p src/components/layout
mkdir -p src/components/auth
mkdir -p src/components/courses
mkdir -p src/components/progress
mkdir -p src/components/lessons
mkdir -p src/components/profile
mkdir -p src/app/(auth)/login
mkdir -p src/app/(auth)/register
mkdir -p src/app/dashboard
mkdir -p src/app/courses
mkdir -p src/app/progress
mkdir -p src/app/profile
mkdir -p src/app/settings
mkdir -p tests/components
mkdir -p tests/services
```

**Verify**: Folder structure matches plan

---

### Step 6: Setup Axios API Client (30 min)

Create `src/lib/api.ts`:

```typescript
import axios from "axios";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: parseInt(process.env.NEXT_PUBLIC_API_TIMEOUT || "30000"),
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor - add JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handle errors and refresh token
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // If 401 and not already retrying, try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem("refreshToken");
        if (refreshToken) {
          const response = await axios.post(
            `${process.env.NEXT_PUBLIC_API_URL}/auth/refresh`,
            { refreshToken }
          );

          const { accessToken, refreshToken: newRefreshToken } = response.data;
          localStorage.setItem("accessToken", accessToken);
          localStorage.setItem("refreshToken", newRefreshToken);

          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, redirect to login
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
```

**Verify**: Import works: `import api from '@/lib/api'`

---

### Step 7: Create Type Definitions (20 min)

Create `src/types/auth.ts`:

```typescript
export interface User {
  userId: string;
  email: string;
  firstName?: string;
  lastName?: string;
  avatarUrl?: string;
  currentLevel?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
  tokenType: string;
  expiresIn: number;
}

export interface RegisterRequest {
  email: string;
  password: string;
  confirmPassword: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}
```

Create `src/types/course.ts`, `src/types/progress.ts`, etc. (similar pattern)

---

### Step 8: Create Auth Store (30 min)

Create `src/store/authStore.ts`:

```typescript
import { create } from "zustand";
import { User, LoginRequest, RegisterRequest } from "@/types/auth";
import api from "@/lib/api";

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  loadUser: () => Promise<void>;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  login: async (credentials) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.post("/auth/login", credentials);
      const { accessToken, refreshToken, user } = response.data;

      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);

      set({
        user,
        accessToken,
        refreshToken,
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Login failed",
        isLoading: false,
      });
      throw error;
    }
  },

  register: async (data) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.post("/auth/register", data);
      const { accessToken, refreshToken, user } = response.data;

      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);

      set({
        user,
        accessToken,
        refreshToken,
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (error: any) {
      set({
        error: error.response?.data?.message || "Registration failed",
        isLoading: false,
      });
      throw error;
    }
  },

  logout: () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    set({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
    });
  },

  loadUser: async () => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      return;
    }

    set({ isLoading: true });
    try {
      const response = await api.get("/users/profile");
      set({
        user: response.data,
        accessToken: token,
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (error) {
      set({ isLoading: false });
      // Token invalid, clear storage
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
    }
  },

  clearError: () => set({ error: null }),
}));
```

---

### Step 9: Update Tailwind Config (10 min)

Update `tailwind.config.ts`:

```typescript
import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: ["class"],
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: "#f0f9ff",
          100: "#e0f2fe",
          500: "#3b82f6",
          600: "#2563eb",
          700: "#1d4ed8",
          900: "#1e3a8a",
        },
        accent: {
          500: "#f59e0b",
          600: "#d97706",
        },
        success: "#10b981",
        error: "#ef4444",
        warning: "#f59e0b",
      },
      fontFamily: {
        sans: ["Inter", "sans-serif"],
      },
    },
  },
  plugins: [require("tailwindcss-animate")],
};

export default config;
```

Add Inter font to `src/app/layout.tsx`:

```typescript
import { Inter } from "next/font/google";

const inter = Inter({ subsets: ["latin"] });

// In metadata and body
```

---

### Step 10: Test Basic Setup (15 min)

Create test page `src/app/test/page.tsx`:

```typescript
"use client";

import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import api from "@/lib/api";

export default function TestPage() {
  useEffect(() => {
    // Test API connection
    api
      .get("/health")
      .then(() => console.log("✅ API Connected"))
      .catch((err) => console.error("❌ API Error:", err));
  }, []);

  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold text-primary-600">
        LEXIA - Setup Complete! 🎉
      </h1>
      <p className="mt-4">Check console for API connection status.</p>
      <Button className="mt-4">Test Button (shadcn/ui)</Button>
    </div>
  );
}
```

**Visit**: http://localhost:3000/test

**Verify**:

- ✅ Page loads without errors
- ✅ Button styled correctly
- ✅ Console shows API connection attempt
- ✅ Tailwind styles applied

---

## ✅ Day 1 Checklist

- [ ] Next.js project created and runs
- [ ] All dependencies installed
- [ ] shadcn/ui configured with components
- [ ] Folder structure matches plan
- [ ] Environment variables setup
- [ ] Axios API client configured
- [ ] Auth store created (Zustand)
- [ ] Type definitions created
- [ ] Tailwind config customized
- [ ] Test page works
- [ ] Git repository initialized
- [ ] Initial commit made

---

## 📝 Git Commands

```bash
# Initialize git (if not already)
git init

# Add all files
git add .

# Initial commit
git commit -m "feat: initial Next.js project setup

- Next.js 14+ with TypeScript and App Router
- Tailwind CSS + shadcn/ui configured
- Zustand state management
- Axios API client with interceptors
- Auth store implementation
- Basic folder structure
- Environment variables setup

Story Points: 4/28 (14% complete)"

# Push to remote (if connected)
git push origin dev
```

---

## 🎯 End of Day 1 Status

**Completed**:

- ✅ Epic A (Project Setup) - 4 story points

**Progress**:

- **Day 1**: 4/28 points (14%)
- **On Track**: ✅ YES

**Next Up (Day 2)**:

- Start Epic B: Authentication Pages
- Create Login page
- Create Register page
- Setup form validation

---

## 🐛 Troubleshooting

### Issue: npm install fails

```bash
# Clear cache and retry
npm cache clean --force
npm install
```

### Issue: shadcn/ui init fails

```bash
# Make sure you're in project root
cd lexia-web
npx shadcn-ui@latest init --force
```

### Issue: CORS errors when calling API

- Check backend is running: http://localhost:8088
- Verify CORS config in backend allows localhost:3000
- Check .env.local has correct API URL

### Issue: TypeScript errors

```bash
# Restart TypeScript server in VS Code
Ctrl+Shift+P → "TypeScript: Restart TS Server"
```

---

## 📚 Resources

- [Next.js App Router Docs](https://nextjs.org/docs/app)
- [shadcn/ui Components](https://ui.shadcn.com/)
- [Zustand Quick Start](https://github.com/pmndrs/zustand)
- [Axios Docs](https://axios-http.com/)
- [Tailwind CSS](https://tailwindcss.com/)

---

**Good luck with Day 1! 🚀**

_Remember: Commit frequently, test often, ask for help when needed!_
