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

```css
/* Light Mode (Default) */
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
// Subtle shadows, clean borders
<Card className="border border-border bg-card hover:shadow-lg transition-shadow">
  <CardHeader>
    <CardTitle className="text-2xl font-serif">Course Title</CardTitle>
    <CardDescription className="text-muted-foreground">
      Description
    </CardDescription>
  </CardHeader>
  <CardContent>{/* Content */}</CardContent>
</Card>
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

// Tailwind dark mode classes
<div className="bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100">
  {/* Content */}
</div>;
```

### Requirements

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
