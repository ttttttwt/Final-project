# LEXIA - Frontend Design Requirements

## Overview

Giao diện người dùng của LEXIA phải được thiết kế với phong cách **minimalist và hiện đại**, lấy cảm hứng từ **Medium**, tập trung vào trải nghiệm người dùng tối ưu và tính trực quan cao.

## Design Philosophy

### Core Principles

1. **Content-First**: Nội dung học tập là trung tâm, UI không làm phân tâm
2. **Minimalist**: Giao diện tối giản, loại bỏ yếu tố thừa
3. **Readable**: Typography rõ ràng, spacing hợp lý
4. **Intuitive**: Navigation dễ hiểu, không cần hướng dẫn
5. **Responsive**: Hoạt động mượt mà trên mọi thiết bị

### Medium-Inspired Elements

```
┌─────────────────────────────────────────────────┐
│  Header (Clean, minimal)                        │
│  Logo | Navigation | User Avatar                │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌────────────────────────────────────────┐    │
│  │                                        │    │
│  │   Large, Readable Title               │    │
│  │   (Serif font, generous spacing)      │    │
│  │                                        │    │
│  │   Metadata (Author, Date, Read time)   │    │
│  │                                        │    │
│  │   ────────────────────────────────     │    │
│  │                                        │    │
│  │   Clean body text                     │    │
│  │   (Sans-serif, optimal line-height)   │    │
│  │                                        │    │
│  │   Generous white space                │    │
│  │                                        │    │
│  └────────────────────────────────────────┘    │
│                                                 │
└─────────────────────────────────────────────────┘
```

## Technical Requirements

### Framework & Tools

- **Framework**: Next.js 14+ (App Router)
- **Language**: TypeScript (strict mode)
- **Styling**: Tailwind CSS + shadcn/ui components
- **State Management**: Zustand
- **Testing**: Jest + React Testing Library

### Component Architecture

```
components/
├── ui/              # shadcn/ui base components
│   ├── button.tsx
│   ├── card.tsx
│   ├── input.tsx
│   └── ...
├── layout/          # Layout components
│   ├── Header.tsx
│   ├── Sidebar.tsx
│   ├── Footer.tsx
│   └── MainLayout.tsx
├── auth/            # Authentication UI
│   ├── LoginForm.tsx
│   └── RegisterForm.tsx
├── courses/         # Course-related UI
│   ├── CourseCard.tsx
│   ├── CourseList.tsx
│   └── CourseDetail.tsx
└── lessons/         # Lesson-related UI
    ├── LessonViewer.tsx
    └── LessonProgress.tsx
```

## Design Specifications

### Typography

```css
/* Inspired by Medium's typography */

/* Headlines (Serif) */
font-family: "Georgia", "Times New Roman", serif;
font-size: 32px - 42px (Desktop), 28px - 36px (Mobile);
line-height: 1.2 - 1.3;
font-weight: 700;

/* Body Text (Sans-serif) */
font-family: system-ui, -apple-system, "Segoe UI", sans-serif;
font-size: 18px - 20px (Desktop), 16px - 18px (Mobile);
line-height: 1.6 - 1.8;
font-weight: 400;

/* UI Text */
font-family: "Inter", system-ui, sans-serif;
font-size: 14px - 16px;
line-height: 1.5;
```

### Color Palette

LEXIA offers two color scheme options. **Version B (Yellow Accent)** is recommended for its modern, professional appearance.

---

#### **Version A: Medium Green (Original)**

Classic Medium-inspired palette with green as primary color.

```css
/* Light Mode */
--background: #ffffff;
--foreground: #242424;
--card: #f9f9f9;
--border: #e6e6e6;
--primary: #1a8917; /* Medium green for primary actions */
--accent: #0066cc; /* Blue for links */
--muted: #757575; /* Gray for secondary text */

/* Dark Mode */
--background: #121212;
--foreground: #e4e4e4;
--card: #1e1e1e;
--border: #2e2e2e;
--primary: #1a8917;
--accent: #5799ff;
--muted: #9e9e9e;
```

---

#### **Version B: Yellow Accent (Recommended)** ⭐

**Design Philosophy**: Minimal & Modern, inspired by Medium's clean aesthetic.

**Overall Feel**: Refined – Readable – Professional

**Light Mode**:

| Role               | Color Name   | HEX Code | Usage                                      |
| ------------------ | ------------ | -------- | ------------------------------------------ |
| **Primary**        | Deep Blue    | #1A73E8  | Brand color (buttons, links, CTAs)         |
| **Accent**         | Warm Yellow  | #FFB300  | Highlights (icons, tags, badges, emphasis) |
| **Background**     | White        | #FFFFFF  | Main background                            |
| **Surface/Card**   | Light Gray   | #F8F9FA  | Cards, panels, secondary surfaces          |
| **Text Primary**   | Dark Gray    | #202124  | Primary text, headings                     |
| **Text Secondary** | Medium Gray  | #5F6368  | Secondary text, captions                   |
| **Border**         | Light Border | #E0E0E0  | Borders, dividers                          |
| **Error**          | Red          | #EA4335  | Error states, destructive actions          |

```css
/* Light Mode (Version B) */
--primary: #1a73e8; /* Deep Blue - brand color */
--accent: #ffb300; /* Warm Yellow - highlights */
--background: #ffffff; /* White */
--surface: #f8f9fa; /* Light Gray - cards */
--text-primary: #202124; /* Dark Gray - main text */
--text-secondary: #5f6368; /* Medium Gray - secondary text */
--border: #e0e0e0; /* Light Border */
--error: #ea4335; /* Red - errors */
```

**🌙 Dark Mode (Version B)**:

| Component          | Color Name   | HEX Code |
| ------------------ | ------------ | -------- |
| **Background**     | Dark Black   | #121212  |
| **Surface**        | Dark Gray    | #1E1E1E  |
| **Text Primary**   | Light Gray   | #E8EAED  |
| **Text Secondary** | Medium Gray  | #9AA0A6  |
| **Primary**        | Light Blue   | #8AB4F8  |
| **Accent**         | Light Yellow | #FDD663  |

```css
/* Dark Mode (Version B) */
--background: #121212; /* Dark Black */
--surface: #1e1e1e; /* Dark Gray */
--text-primary: #e8eaed; /* Light Gray */
--text-secondary: #9aa0a6; /* Medium Gray */
--primary: #8ab4f8; /* Light Blue - adjusted for dark mode */
--accent: #fdd663; /* Light Yellow - adjusted for dark mode */
--border: #2e2e2e; /* Dark Border */
--error: #f28b82; /* Light Red - adjusted for dark mode */
```

---

#### **Choosing a Color Scheme**

**Use Version A (Green)** if:

- You want a classic Medium-inspired look
- Brand recognition with green is important
- Calming, nature-inspired aesthetic desired

**Use Version B (Yellow Accent)** ⭐ **Recommended** if:

- Modern, professional appearance is priority
- Need strong visual hierarchy (blue primary + yellow accent)
- Better accessibility (blue/yellow have better contrast)
- Target audience: working professionals (corporate feel)

**Implementation Note**:

- Choose ONE version and apply consistently across entire app
- Version B is recommended for LEXIA's target audience (working professionals)
- All examples in this document will use Version B unless specified

### Spacing & Layout

```css
/* Container Max-Width */
max-width: 680px (Content), 1280px (Full-width pages);

/* Spacing Scale (Tailwind-based) */
xs: 0.5rem (8px)
sm: 1rem (16px)
md: 1.5rem (24px)
lg: 2rem (32px)
xl: 3rem (48px)
2xl: 4rem (64px)

/* Content Padding */
padding-x: 24px (Mobile), 48px (Tablet), 64px (Desktop);
padding-y: 32px (Mobile), 48px (Tablet), 64px (Desktop);
```

### Component Design Patterns

#### 1. Header

```tsx
// Clean, minimal header like Medium
<header className="border-b border-border bg-background/95 backdrop-blur">
  <div className="container flex h-16 items-center justify-between px-6">
    <Logo />
    <Navigation />
    <UserMenu />
  </div>
</header>
```

#### 2. Content Layout

```tsx
// Generous white space, readable width
<main className="container max-w-3xl py-12 px-6">
  <article className="prose prose-lg dark:prose-invert">
    <h1>Lesson Title</h1>
    <div className="text-muted-foreground text-sm mb-8">
      Level: Intermediate • 10 min read
    </div>
    <div className="space-y-6">{/* Content */}</div>
  </article>
</main>
```

#### 3. Card Design

```tsx
// Subtle shadows, clean borders (Version B colors)
<Card className="border border-[#E0E0E0] bg-[#F8F9FA] hover:shadow-lg transition-shadow">
  <CardHeader>
    <CardTitle className="text-2xl font-serif text-[#202124]">
      Course Title
    </CardTitle>
    <CardDescription className="text-[#5F6368]">
      Intermediate • 24 lessons • 8 hours
    </CardDescription>
  </CardHeader>
  <CardContent>
    <div className="flex items-center gap-2 mb-4">
      <Badge className="bg-[#FFB300] text-[#202124] hover:bg-[#FFB300]/90">
        Featured
      </Badge>
      <Badge variant="outline" className="border-[#E0E0E0] text-[#5F6368]">
        Business English
      </Badge>
    </div>
    <Button className="w-full bg-[#1A73E8] hover:bg-[#1557B0] text-white">
      Start Learning
    </Button>
  </CardContent>
</Card>
```

#### 4. Button Variants (Version B)

```tsx
// Primary Button (Deep Blue)
<Button className="bg-[#1A73E8] hover:bg-[#1557B0] text-white">
  Primary Action
</Button>

// Secondary Button (Outline)
<Button
  variant="outline"
  className="border-[#1A73E8] text-[#1A73E8] hover:bg-[#1A73E8]/10"
>
  Secondary Action
</Button>

// Accent Button (Yellow - for highlights)
<Button className="bg-[#FFB300] hover:bg-[#E09F00] text-[#202124]">
  Highlight Action
</Button>

// Destructive Button
<Button
  variant="destructive"
  className="bg-[#EA4335] hover:bg-[#D33426] text-white"
>
  Delete
</Button>
```

#### 5. Tag/Badge System (Version B)

```tsx
// Accent Tag (Yellow)
<Badge className="bg-[#FFB300] text-[#202124]">
  New
</Badge>

// Primary Tag (Blue)
<Badge className="bg-[#1A73E8] text-white">
  Premium
</Badge>

// Outline Tag
<Badge variant="outline" className="border-[#E0E0E0] text-[#5F6368]">
  Intermediate
</Badge>

// Success Tag
<Badge className="bg-[#34A853] text-white">
  Completed
</Badge>
```

#### 6. Alert/Notification (Version B)

```tsx
// Info Alert (Primary Blue)
<Alert className="border-[#1A73E8] bg-[#E8F0FE]">
  <InfoIcon className="h-4 w-4 text-[#1A73E8]" />
  <AlertTitle className="text-[#1A73E8]">Information</AlertTitle>
  <AlertDescription className="text-[#5F6368]">
    Your lesson progress has been saved.
  </AlertDescription>
</Alert>

// Warning Alert (Yellow)
<Alert className="border-[#FFB300] bg-[#FFF9E6]">
  <AlertTriangle className="h-4 w-4 text-[#FFB300]" />
  <AlertTitle className="text-[#202124]">Warning</AlertTitle>
  <AlertDescription className="text-[#5F6368]">
    You have 3 pending assignments due tomorrow.
  </AlertDescription>
</Alert>

// Error Alert
<Alert className="border-[#EA4335] bg-[#FCE8E6]">
  <XCircle className="h-4 w-4 text-[#EA4335]" />
  <AlertTitle className="text-[#EA4335]">Error</AlertTitle>
  <AlertDescription className="text-[#5F6368]">
    Failed to load lesson. Please try again.
  </AlertDescription>
</Alert>
```

## Responsive Design

### Breakpoints

```css
/* Mobile First Approach */
sm: 640px   /* Mobile landscape */
md: 768px   /* Tablet */
lg: 1024px  /* Desktop small */
xl: 1280px  /* Desktop medium */
2xl: 1536px /* Desktop large */
```

### Testing Requirements

- ✅ **320px** - iPhone SE (Mobile S)
- ✅ **375px** - iPhone 12/13 (Mobile M)
- ✅ **768px** - iPad (Tablet)
- ✅ **1024px** - Desktop Small
- ✅ **1280px** - MacBook (Desktop M)
- ✅ **1920px** - Full HD (Desktop L)

### Responsive Patterns

```tsx
// Example: Responsive grid
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
  {/* Content adapts to screen size */}
</div>

// Example: Responsive typography
<h1 className="text-3xl md:text-4xl lg:text-5xl font-serif">
  Headline
</h1>

// Example: Responsive padding
<section className="px-6 md:px-12 lg:px-16 py-8 md:py-12 lg:py-16">
  {/* Adaptive spacing */}
</section>
```

## Dark Mode Support

### Implementation Strategy

```tsx
// Use next-themes for dark mode
import { ThemeProvider } from "next-themes";

// Version B Dark Mode with Tailwind classes
<div className="bg-white dark:bg-[#121212] text-[#202124] dark:text-[#E8EAED]">
  {/* Content */}
</div>;
```

### Requirements

- ✅ System preference detection
- ✅ Manual toggle in UI
- ✅ Persistent user preference
- ✅ Smooth transitions
- ✅ All components support dark mode

### Dark Mode Examples (Version B)

#### Header (Dark Mode)

```tsx
<header className="border-b border-[#E0E0E0] dark:border-[#2E2E2E] bg-white/95 dark:bg-[#121212]/95 backdrop-blur">
  <div className="container flex h-16 items-center justify-between px-6">
    <Logo />
    <Navigation />
    <ThemeToggle /> {/* Light/Dark mode toggle */}
    <UserMenu />
  </div>
</header>
```

#### Card (Dark Mode)

```tsx
<Card className="border border-[#E0E0E0] dark:border-[#2E2E2E] bg-[#F8F9FA] dark:bg-[#1E1E1E] hover:shadow-lg transition-shadow">
  <CardHeader>
    <CardTitle className="text-2xl font-serif text-[#202124] dark:text-[#E8EAED]">
      Course Title
    </CardTitle>
    <CardDescription className="text-[#5F6368] dark:text-[#9AA0A6]">
      Intermediate • 24 lessons
    </CardDescription>
  </CardHeader>
  <CardContent>
    <Badge className="bg-[#FFB300] dark:bg-[#FDD663] text-[#202124] dark:text-[#121212]">
      Featured
    </Badge>
    <Button className="bg-[#1A73E8] dark:bg-[#8AB4F8] text-white dark:text-[#121212] hover:bg-[#1557B0] dark:hover:bg-[#A8C7FA]">
      Start Learning
    </Button>
  </CardContent>
</Card>
```

#### Alert (Dark Mode)

```tsx
// Info Alert in Dark Mode
<Alert className="border-[#1A73E8] dark:border-[#8AB4F8] bg-[#E8F0FE] dark:bg-[#1E1E1E]">
  <InfoIcon className="h-4 w-4 text-[#1A73E8] dark:text-[#8AB4F8]" />
  <AlertTitle className="text-[#1A73E8] dark:text-[#8AB4F8]">
    Information
  </AlertTitle>
  <AlertDescription className="text-[#5F6368] dark:text-[#9AA0A6]">
    Your lesson progress has been saved.
  </AlertDescription>
</Alert>

// Warning Alert in Dark Mode
<Alert className="border-[#FFB300] dark:border-[#FDD663] bg-[#FFF9E6] dark:bg-[#1E1E1E]">
  <AlertTriangle className="h-4 w-4 text-[#FFB300] dark:text-[#FDD663]" />
  <AlertTitle className="text-[#202124] dark:text-[#E8EAED]">
    Warning
  </AlertTitle>
  <AlertDescription className="text-[#5F6368] dark:text-[#9AA0A6]">
    You have 3 pending assignments due tomorrow.
  </AlertDescription>
</Alert>
```

#### Theme Toggle Component

```tsx
"use client";

import { Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";
import { Button } from "@/components/ui/button";

export function ThemeToggle() {
  const { theme, setTheme } = useTheme();

  return (
    <Button
      variant="ghost"
      size="icon"
      onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
      className="text-[#5F6368] dark:text-[#9AA0A6] hover:bg-[#F8F9FA] dark:hover:bg-[#1E1E1E]"
    >
      <Sun className="h-5 w-5 rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
      <Moon className="absolute h-5 w-5 rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
      <span className="sr-only">Toggle theme</span>
    </Button>
  );
}
```

### CSS Variables Setup (Version B)

```css
/* globals.css or app/globals.css */
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    /* Light Mode - Version B */
    --primary: #1a73e8; /* Deep Blue */
    --accent: #ffb300; /* Warm Yellow */
    --background: #ffffff;
    --surface: #f8f9fa;
    --text-primary: #202124;
    --text-secondary: #5f6368;
    --border: #e0e0e0;
    --error: #ea4335;
  }

  .dark {
    /* Dark Mode - Version B */
    --background: #121212;
    --surface: #1e1e1e;
    --text-primary: #e8eaed;
    --text-secondary: #9aa0a6;
    --primary: #8ab4f8; /* Light Blue for dark mode */
    --accent: #fdd663; /* Light Yellow for dark mode */
    --border: #2e2e2e;
    --error: #f28b82;
  }
}

/* Apply to body */
body {
  background-color: var(--background);
  color: var(--text-primary);
}
```

### Tailwind Config Extension (Version B)

```js
// tailwind.config.js
module.exports = {
  darkMode: ["class"],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: "#1A73E8", // Deep Blue
          light: "#8AB4F8", // Light Blue (dark mode)
          dark: "#1557B0", // Darker Blue (hover)
        },
        accent: {
          DEFAULT: "#FFB300", // Warm Yellow
          light: "#FDD663", // Light Yellow (dark mode)
          dark: "#E09F00", // Darker Yellow (hover)
        },
        background: {
          DEFAULT: "#FFFFFF",
          dark: "#121212",
        },
        surface: {
          DEFAULT: "#F8F9FA",
          dark: "#1E1E1E",
        },
        text: {
          primary: "#202124",
          "primary-dark": "#E8EAED",
          secondary: "#5F6368",
          "secondary-dark": "#9AA0A6",
        },
        border: {
          DEFAULT: "#E0E0E0",
          dark: "#2E2E2E",
        },
      },
    },
  },
};
```

- ✅ System preference detection
- ✅ Manual toggle in UI
- ✅ Persistent user preference
- ✅ Smooth transitions
- ✅ All components support dark mode

## Accessibility (WCAG AA)

### Requirements

- ✅ **Semantic HTML**: Use `<nav>`, `<main>`, `<article>`, `<aside>`
- ✅ **ARIA Labels**: All interactive elements labeled
- ✅ **Keyboard Navigation**: Full keyboard support (Tab, Enter, Escape)
- ✅ **Focus Indicators**: Visible focus states
- ✅ **Color Contrast**: ≥ 4.5:1 for normal text, ≥ 3:1 for large text
- ✅ **Screen Readers**: Proper alt text, ARIA attributes

### Example

```tsx
// Accessible button
<button
  aria-label="Close dialog"
  className="focus:outline-none focus:ring-2 focus:ring-primary"
  onClick={onClose}
>
  <X className="h-4 w-4" aria-hidden="true" />
</button>

// Accessible form
<form>
  <label htmlFor="email" className="sr-only">Email</label>
  <input
    id="email"
    type="email"
    aria-describedby="email-error"
    aria-invalid={errors.email ? "true" : "false"}
  />
  {errors.email && (
    <span id="email-error" role="alert" className="text-destructive">
      {errors.email.message}
    </span>
  )}
</form>
```

## Performance Requirements

### Metrics

- ✅ **First Contentful Paint (FCP)**: < 1.8s
- ✅ **Largest Contentful Paint (LCP)**: < 2.5s
- ✅ **Time to Interactive (TTI)**: < 3.8s
- ✅ **Cumulative Layout Shift (CLS)**: < 0.1

### Optimization Techniques

```tsx
// 1. Image optimization (Next.js Image)
import Image from "next/image";
<Image
  src="/hero.jpg"
  alt="Hero image"
  width={1200}
  height={600}
  priority={true}
  placeholder="blur"
/>;

// 2. Code splitting
const LazyComponent = dynamic(() => import("./HeavyComponent"), {
  loading: () => <Skeleton />,
  ssr: false,
});

// 3. Font optimization
import { Inter } from "next/font/google";
const inter = Inter({ subsets: ["latin"], display: "swap" });
```

## Animation & Interactions

### Principles

- **Subtle**: Animations should enhance, not distract
- **Fast**: Transitions < 300ms
- **Purposeful**: Every animation has a reason

### Examples

```css
/* Hover effects */
.card {
  transition: all 200ms ease-in-out;
}
.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

/* Page transitions */
.page-enter {
  opacity: 0;
  transform: translateY(10px);
}
.page-enter-active {
  opacity: 1;
  transform: translateY(0);
  transition: all 300ms ease-out;
}
```

## Reusable Component Guidelines

### 1. Component Structure

```tsx
// Good example: Reusable, typed, accessible
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "ghost";
  size?: "sm" | "md" | "lg";
  isLoading?: boolean;
}

export function Button({
  variant = "primary",
  size = "md",
  isLoading = false,
  children,
  className,
  disabled,
  ...props
}: ButtonProps) {
  return (
    <button
      className={cn(
        buttonVariants({ variant, size }),
        isLoading && "opacity-50 cursor-not-allowed",
        className
      )}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading && <Spinner className="mr-2" />}
      {children}
    </button>
  );
}
```

### 2. Naming Conventions

- **Components**: PascalCase (`CourseCard`, `LessonViewer`)
- **Props**: camelCase (`isLoading`, `onSubmit`)
- **Files**: kebab-case (`course-card.tsx`, `lesson-viewer.tsx`)
- **CSS Classes**: kebab-case or Tailwind utilities

### 3. Testing

```tsx
// Component test example
describe("Button", () => {
  it("renders children correctly", () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText("Click me")).toBeInTheDocument();
  });

  it("shows loading state", () => {
    render(<Button isLoading>Submit</Button>);
    expect(screen.getByRole("button")).toBeDisabled();
  });

  it("handles click events", async () => {
    const handleClick = jest.fn();
    render(<Button onClick={handleClick}>Click</Button>);

    await userEvent.click(screen.getByRole("button"));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });
});
```

## Integration with Backend

### API Communication

```tsx
// Using axios with httpOnly cookies (secure)
import { api } from "@/lib/api";

export const courseService = {
  async getCourses() {
    const response = await api.get("/api/courses");
    return response.data;
  },

  async enrollCourse(courseId: string) {
    const response = await api.post(`/api/courses/${courseId}/enroll`);
    return response.data;
  },
};

// Error handling
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Refresh token logic
      await authService.refreshToken();
      return api.request(error.config);
    }

    if (error.code === "ERR_NETWORK") {
      toast.error("No internet connection");
      return Promise.reject({ code: "NETWORK" });
    }

    return Promise.reject(error);
  }
);
```

## Quality Checklist

### Before Code Review

- [ ] Component is reusable and well-typed
- [ ] Responsive design tested (320px - 1920px)
- [ ] Dark mode works correctly
- [ ] Accessibility requirements met (ARIA, keyboard nav)
- [ ] Performance optimized (lazy loading, images)
- [ ] Error states handled
- [ ] Loading states implemented
- [ ] Form validation included (Zod schema)
- [ ] Tests written (≥60% coverage)
- [ ] No console errors/warnings

### Before Production

- [ ] All pages responsive
- [ ] Dark mode fully functional
- [ ] Lighthouse score > 90
- [ ] WCAG AA compliance verified
- [ ] Cross-browser tested (Chrome, Safari, Firefox)
- [ ] Mobile tested (iOS, Android)
- [ ] API integration working
- [ ] Error boundaries in place
- [ ] Analytics integrated
- [ ] SEO optimized (meta tags, sitemap)

## Expected Deliverables

### Phase 1: Core UI (Sprint 3)

- ✅ Next.js project setup with TypeScript
- ✅ Tailwind + shadcn/ui configured
- ✅ Layout components (Header, Sidebar, Footer)
- ✅ Authentication pages (Login, Register)
- ✅ Dashboard home page
- ✅ Course listing and detail pages
- ✅ Dark mode support
- ✅ Responsive design
- ✅ Basic animations

### Phase 2: Advanced Features (Sprint 4+)

- ✅ Lesson viewer with progress tracking
- ✅ Profile management
- ✅ Settings page
- ✅ AI role-play interface
- ✅ Grammar sandbox UI
- ✅ Gamification elements
- ✅ Accessibility audit
- ✅ Performance optimization

## Reference Links

### Design Inspiration

- [Medium Design System](https://medium.design/)
- [Vercel Design](https://vercel.com/design)
- [Tailwind UI](https://tailwindui.com/)
- [shadcn/ui](https://ui.shadcn.com/)

### Technical Documentation

- [Next.js Docs](https://nextjs.org/docs)
- [Tailwind CSS Docs](https://tailwindcss.com/docs)
- [React Hook Form](https://react-hook-form.com/)
- [Zod Validation](https://zod.dev/)

### Accessibility Resources

- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [WAI-ARIA Practices](https://www.w3.org/WAI/ARIA/apg/)
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)

---

**Last Updated**: November 12, 2025  
**Status**: 🎨 Active Design Requirements for Sprint 3+
