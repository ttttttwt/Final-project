# LEXIA Admin Site - Specification Document

**Version**: 1.2.0  
**Last Updated**: November 27, 2025  
**Technology Stack**: React 18+ | TypeScript | shadcn/ui | Vite  
**Target Users**: ADMIN, CONTENT_MANAGER

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [User Roles & Permissions](#user-roles--permissions)
3. [Technical Architecture](#technical-architecture)
4. [Feature Requirements](#feature-requirements)
5. [UI/UX Design System](#uiux-design-system)
6. [API Integration](#api-integration)
7. [Security Requirements](#security-requirements)
8. [Development Guidelines](#development-guidelines)

---

## 1. Overview

### 1.1 Purpose

LEXIA Admin Site is a dedicated web application for administrators and content managers to manage the LEXIA learning platform. This includes user management, course/lesson creation, content moderation, and system monitoring.

### 1.2 Goals

- **Efficiency**: Streamline content creation and management workflows
- **Security**: Role-based access control with JWT authentication
- **Usability**: Intuitive interface with shadcn/ui components
- **Scalability**: Support growing content library and user base
- **Maintainability**: Clean React architecture with TypeScript

### 1.3 Tech Stack Rationale

| Technology          | Why Chosen                                             |
| ------------------- | ------------------------------------------------------ |
| **React 18+**       | Modern, component-based architecture with hooks        |
| **TypeScript**      | Type safety, better IDE support, fewer runtime errors  |
| **Vite**            | Fast dev server, optimized builds, modern tooling      |
| **shadcn/ui**       | Accessible, customizable components built on Radix UI  |
| **React Hook Form** | Performant form handling with minimal re-renders       |
| **Zod**             | Schema validation for forms and API responses          |
| **TanStack Query**  | Server state management, caching, automatic refetching |
| **Zustand**         | Lightweight global state management                    |
| **React Router v6** | Client-side routing with nested layouts                |

---

## 2. User Roles & Permissions

### 2.1 Role Definitions

#### ADMIN (Full Access)

- **User Management**: Create, update, delete users; assign roles
- **Content Management**: Full CRUD on courses, sections, lessons
- **System Monitoring**: View logs, metrics, health status via Actuator
- **Settings**: Configure system settings, API keys, integrations

#### CONTENT_MANAGER (Content Only)

- **Course Management**: Create, update, delete courses
- **Lesson Management**: Create, update, delete lessons
- **Content Publishing**: Publish/unpublish courses
- **Content Preview**: Preview courses as learner would see them
- **Analytics**: View course enrollment and completion stats

### 2.2 Permission Matrix

| Feature            | ADMIN | CONTENT_MANAGER |
| ------------------ | ----- | --------------- |
| Dashboard Overview | ✅    | ✅              |
| User Management    | ✅    | ❌              |
| Course Management  | ✅    | ✅              |
| Lesson Management  | ✅    | ✅              |
| Publish/Unpublish  | ✅    | ✅              |
| System Monitoring  | ✅    | ❌              |
| Settings & Config  | ✅    | ❌              |
| AI Usage Logs      | ✅    | ❌              |
| Actuator Endpoints | ✅    | ❌              |

---

## 3. Technical Architecture

### 3.1 Project Structure

```
lexia-admin/
├── public/                      # Static assets
│   ├── favicon.ico
│   └── images/
├── src/
│   ├── main.tsx                 # App entry point
│   ├── App.tsx                  # Root component
│   ├── router.tsx               # Route configuration
│   ├── components/              # Reusable UI components
│   │   ├── ui/                  # shadcn/ui components
│   │   │   ├── button.tsx
│   │   │   ├── input.tsx
│   │   │   ├── select.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── table.tsx
│   │   │   ├── form.tsx
│   │   │   └── ...
│   │   ├── layout/              # Layout components
│   │   │   ├── Sidebar.tsx
│   │   │   ├── Header.tsx
│   │   │   ├── Breadcrumb.tsx
│   │   │   └── MainLayout.tsx
│   │   ├── auth/                # Auth-related components
│   │   │   ├── ProtectedRoute.tsx
│   │   │   └── RoleGuard.tsx
│   │   └── shared/              # Shared components
│   │       ├── DataTable.tsx
│   │       ├── ConfirmDialog.tsx
│   │       ├── LoadingSpinner.tsx
│   │       └── ErrorBoundary.tsx
│   ├── features/                # Feature-based modules
│   │   ├── auth/
│   │   │   ├── pages/
│   │   │   │   ├── LoginPage.tsx
│   │   │   │   └── ForgotPasswordPage.tsx
│   │   │   ├── hooks/
│   │   │   │   └── useAuth.ts
│   │   │   └── api/
│   │   │       └── authApi.ts
│   │   ├── dashboard/
│   │   │   └── pages/
│   │   │       └── DashboardPage.tsx
│   │   ├── users/
│   │   │   ├── pages/
│   │   │   │   ├── UserListPage.tsx
│   │   │   │   ├── UserCreatePage.tsx
│   │   │   │   └── UserEditPage.tsx
│   │   │   ├── components/
│   │   │   │   ├── UserTable.tsx
│   │   │   │   └── UserForm.tsx
│   │   │   ├── hooks/
│   │   │   │   └── useUsers.ts
│   │   │   └── api/
│   │   │       └── usersApi.ts
│   │   ├── courses/
│   │   │   ├── pages/
│   │   │   │   ├── CourseListPage.tsx
│   │   │   │   ├── CourseCreatePage.tsx
│   │   │   │   ├── CourseEditPage.tsx
│   │   │   │   └── CoursePreviewPage.tsx
│   │   │   ├── components/
│   │   │   │   ├── CourseTable.tsx
│   │   │   │   ├── CourseForm.tsx
│   │   │   │   └── SectionManager.tsx
│   │   │   ├── hooks/
│   │   │   │   └── useCourses.ts
│   │   │   └── api/
│   │   │       └── coursesApi.ts
│   │   ├── lessons/
│   │   │   ├── pages/
│   │   │   │   ├── LessonCreatePage.tsx
│   │   │   │   └── LessonEditPage.tsx
│   │   │   ├── components/
│   │   │   │   ├── LessonForm.tsx
│   │   │   │   ├── ReadingLessonEditor.tsx
│   │   │   │   ├── ListeningLessonEditor.tsx
│   │   │   │   ├── QuizLessonEditor.tsx
│   │   │   │   └── SpeakingLessonEditor.tsx
│   │   │   ├── hooks/
│   │   │   │   └── useLessons.ts
│   │   │   └── api/
│   │   │       └── lessonsApi.ts
│   │   └── monitoring/
│   │       ├── pages/
│   │       │   ├── SystemHealthPage.tsx
│   │       │   └── AIUsageLogsPage.tsx
│   │       └── api/
│   │           └── monitoringApi.ts
│   ├── lib/                     # Utility libraries
│   │   ├── api.ts               # Axios instance + interceptors
│   │   ├── utils.ts             # Helper functions
│   │   └── constants.ts         # App constants
│   ├── hooks/                   # Global custom hooks
│   │   ├── useDebounce.ts
│   │   ├── useLocalStorage.ts
│   │   └── useMediaQuery.ts
│   ├── store/                   # Zustand stores
│   │   ├── authStore.ts         # Auth state
│   │   └── uiStore.ts           # UI state (sidebar, theme)
│   ├── types/                   # TypeScript types
│   │   ├── auth.types.ts
│   │   ├── course.types.ts
│   │   ├── lesson.types.ts
│   │   ├── user.types.ts
│   │   └── api.types.ts
│   ├── styles/                  # Global styles
│   │   └── globals.css
│   └── config/                  # Configuration files
│       └── env.ts
├── .env.development
├── .env.production
├── components.json              # shadcn/ui config
├── tsconfig.json
├── vite.config.ts
└── package.json
```

### 3.2 State Management Strategy

| State Type       | Solution            | Use Case                      |
| ---------------- | ------------------- | ----------------------------- |
| **Server State** | TanStack Query      | API data, caching, refetching |
| **Global State** | Zustand             | Auth state, UI preferences    |
| **Form State**   | React Hook Form     | Form inputs, validation       |
| **URL State**    | React Router        | Pagination, filters, search   |
| **Local State**  | useState/useReducer | Component-specific state      |

### 3.3 Routing Structure

```typescript
// src/router.tsx
import { createBrowserRouter } from "react-router-dom";

const router = createBrowserRouter([
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <MainLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <DashboardPage />,
      },
      {
        path: "users",
        element: (
          <RoleGuard roles={["ADMIN"]}>
            <UserListPage />
          </RoleGuard>
        ),
      },
      {
        path: "users/create",
        element: (
          <RoleGuard roles={["ADMIN"]}>
            <UserCreatePage />
          </RoleGuard>
        ),
      },
      {
        path: "users/:id/edit",
        element: (
          <RoleGuard roles={["ADMIN"]}>
            <UserEditPage />
          </RoleGuard>
        ),
      },
      {
        path: "courses",
        element: <CourseListPage />,
      },
      {
        path: "courses/create",
        element: <CourseCreatePage />,
      },
      {
        path: "courses/:id/edit",
        element: <CourseEditPage />,
      },
      {
        path: "courses/:id/preview",
        element: <CoursePreviewPage />,
      },
      {
        path: "lessons/create",
        element: <LessonCreatePage />,
      },
      {
        path: "lessons/:id/edit",
        element: <LessonEditPage />,
      },
      {
        path: "monitoring/health",
        element: (
          <RoleGuard roles={["ADMIN"]}>
            <SystemHealthPage />
          </RoleGuard>
        ),
      },
      {
        path: "monitoring/ai-usage",
        element: (
          <RoleGuard roles={["ADMIN"]}>
            <AIUsageLogsPage />
          </RoleGuard>
        ),
      },
    ],
  },
]);

export default router;
```

---

## 4. Feature Requirements

### 4.1 Authentication

#### 4.1.1 Login Page

- **URL**: `/login`
- **Access**: Public
- **Features**:
  - Email + password login form
  - "Remember me" checkbox (7-day token expiry)
  - Form validation (Zod schema)
  - Error handling (invalid credentials, network errors)
  - Loading state during authentication
  - Redirect to dashboard on success
  - Show role in success message (e.g., "Welcome, Admin!")

#### 4.1.2 Protected Routes

- **Implementation**: `<ProtectedRoute>` wrapper component
- **Behavior**:
  - Check for valid JWT token in localStorage
  - Redirect to `/login` if unauthenticated
  - Auto-refresh expired tokens using refresh token
  - Show loading spinner during token validation

#### 4.1.3 Role Guards

- **Implementation**: `<RoleGuard roles={['ADMIN']}>` wrapper
- **Behavior**:
  - Check user role from decoded JWT
  - Render children if role matches
  - Show 403 error page if unauthorized
  - Log unauthorized access attempts

### 4.2 Dashboard

#### 4.2.1 Admin Dashboard

- **URL**: `/`
- **Access**: ADMIN, CONTENT_MANAGER
- **Widgets**:
  1. **Stats Cards**:
     - Total Users
     - Total Courses (Published/Unpublished)
     - Total Lessons
     - Active Enrollments (last 7 days)
  2. **Recent Activity Timeline**:
     - Recently created courses
     - Recently published content
     - New user registrations (ADMIN only)
  3. **System Health** (ADMIN only):
     - API status (from `/actuator/health`)
     - Database status
     - AI API usage quota
  4. **Quick Actions**:
     - Create New Course
     - Create New Lesson
     - View All Courses

### 4.3 User Management (ADMIN Only) - (Implemented)

#### 4.3.1 User List Page

- **URL**: `/users`
- **Features**:
  - **Data Table** (shadcn/ui Table + TanStack Table):
    - Columns: ID, Email, Name, Role, Status, Created At, Actions
    - Sortable columns
    - Filterable by role (USER, CONTENT_MANAGER, ADMIN)
    - Searchable by email/name (debounced)
  - **Pagination**: Server-side, 10/25/50 per page
  - **Actions**:
    - Edit user (redirect to `/users/:id/edit`)
    - Delete user (with confirmation dialog)
    - Activate/Deactivate user

#### 4.3.2 User Create Page

- **URL**: `/users/create`
- **Form Fields**:
  - Email (required, validated)
  - Password (required, min 8 chars, 1 uppercase, 1 number)
  - Confirm Password (must match)
  - First Name (required)
  - Last Name (required)
  - Role (select: USER, CONTENT_MANAGER, ADMIN)
- **Validation**: Zod schema
- **Error Handling**: Display API errors (e.g., "Email already exists")
- **Success**: Redirect to `/users` with success toast

#### 4.3.3 User Edit Page

- **URL**: `/users/:id/edit`
- **Form Fields**: Same as create, except:
  - Password (optional, only if changing)
  - Current status (active/inactive toggle)
- **Pre-fill**: Load existing user data
- **Success**: Redirect to `/users` with success toast

### 4.4 Course Management

#### 4.4.1 Course List Page

- **URL**: `/courses`
- **Features**:
  - **Data Table**:
    - Columns: Thumbnail, Title, CEFR Level, Published Status, Sections, Lessons, Actions
    - Filter by CEFR level (A1-C2)
    - Filter by published status
    - Search by title (debounced)
  - **Pagination**: Server-side
  - **Actions**:
    - Edit (redirect to `/courses/:id/edit`)
    - Preview (redirect to `/courses/:id/preview`)
    - Publish/Unpublish (toggle button with confirmation)
    - Delete (with confirmation, only if unpublished)

#### 4.4.2 Course Create Page

- **URL**: `/courses/create`
- **Form Sections**:
  1. **Basic Info**:
     - Title (required, max 255 chars)
     - Description (textarea, max 1000 chars)
     - CEFR Level (select: A1-C2)
     - Thumbnail URL (optional, validated URL)
  2. **Sections** (initially empty):
     - "Add Section" button opens dialog
     - Section form: Title, Order Index
     - Drag-and-drop to reorder sections
- **Validation**: Zod schema
- **Save Behavior**:
  - Create course first (status: unpublished)
  - Then add sections if any
  - Show success toast with "Edit Course" link

#### 4.4.3 Course Edit Page

- **URL**: `/courses/:id/edit`
- **Features**:
  - Same form as create, pre-filled
  - **Section Management**:
    - List existing sections (collapsible cards)
    - Each section shows lesson count
    - "Add Lesson" button for each section (redirect to `/lessons/create?sectionId=X`)
    - Edit section (inline or dialog)
    - Delete section (with confirmation, cascade delete lessons)
    - Reorder sections (drag-and-drop)
  - **Lesson Management** (nested under sections):
    - List lessons in table (Title, Type, Duration, Order)
    - Edit lesson (redirect to `/lessons/:id/edit`)
    - Delete lesson (with confirmation)
    - Reorder lessons (drag-and-drop within section)
- **Publish Button**:
  - Disabled if course has 0 sections or 0 lessons
  - Show tooltip: "Course must have at least 1 section and 1 lesson"
  - Confirmation dialog before publishing
- **Save & Continue**: Button to save changes and stay on page

#### 4.4.4 Course Preview Page

- **URL**: `/courses/:id/preview`
- **Purpose**: View course as learner would see it
- **Features**:
  - Show course details (title, description, thumbnail)
  - Show sections in order
  - Show lessons in each section
  - Click lesson to preview content (read-only)
  - "Back to Edit" button
  - "Publish Course" button (if unpublished)

### 4.5 Lesson Management

#### 4.5.1 Lesson Create Page

- **URL**: `/lessons/create?sectionId=X`
- **Query Param**: `sectionId` (required, validates section exists)
- **Form Flow**:
  1. **Step 1: Lesson Type Selection**:
     - Radio group: READING, LISTENING, QUIZ, SPEAKING
     - Show description for each type
  2. **Step 2: Basic Info**:
     - Title (required)
     - Duration (minutes, number input)
     - Order Index (auto-filled, editable)
  3. **Step 3: Content Editor** (type-specific):
     - **READING**: See 4.5.2.1
     - **LISTENING**: See 4.5.2.2
     - **QUIZ**: See 4.5.2.3
     - **SPEAKING**: See 4.5.2.4
  4. **Step 4: Preview**:
     - Show lesson as learner would see it
     - "Edit" button to go back
     - "Save Lesson" button
- **Validation**: Zod schema for each lesson type (matches backend schemas)
- **Error Handling**: Show field-level errors
- **Success**: Redirect to course edit page with success toast

#### 4.5.2 Lesson Content Editors

##### 4.5.2.1 Reading Lesson Editor

**JSONB Schema**: See `DATABASE-SCHEMA.md` section 2.3.1

**Form Sections**:

1. **Passages** (array):
   - "Add Passage" button
   - Each passage:
     - Title (optional)
     - Text (textarea, rich text editor optional)
     - Delete button
2. **Questions** (array):
   - "Add Question" button
   - Each question:
     - Question text (required)
     - Question type (select: multiple_choice, true_false, short_answer)
     - Options (if multiple_choice, dynamic input list)
     - Correct Answer (select from options or free text)
     - Explanation (optional, textarea)
     - Delete button
3. **Vocabulary** (array, optional):
   - "Add Vocabulary" button
   - Each vocabulary:
     - Word (required)
     - Definition (required)
     - Example (optional)
     - Part of Speech (optional, select: noun, verb, adjective, adverb)
     - Delete button

**Validation**:

- Minimum 1 passage
- Minimum 1 question
- If multiple_choice: minimum 2 options, maximum 6
- Correct answer index must be valid

##### 4.5.2.2 Listening Lesson Editor

**JSONB Schema**: See `DATABASE-SCHEMA.md` section 2.3.2

**Form Sections**:

1. **Audio**:
   - Audio URL (text input, validated URL)
   - OR File Upload (upload to backend, get URL)
   - Duration (seconds, number input)
   - Transcript (textarea, required for accessibility)
   - Show Transcript Initially (checkbox)
2. **Questions** (array):
   - Same as Reading questions, plus:
     - Timestamp (optional, number input, seconds)
3. **Vocabulary** (array, optional):
   - Same as Reading vocabulary, plus:
     - Timestamp (optional, number input)

**Validation**:

- Audio URL required and valid
- Duration > 0
- Transcript required
- Timestamp (if provided) <= duration

##### 4.5.2.3 Quiz Lesson Editor

**JSONB Schema**: See `DATABASE-SCHEMA.md` section 2.3.3

**Form Sections**:

1. **Quiz Settings**:
   - Title (optional)
   - Instructions (optional, textarea)
   - Time Limit (optional, number input, seconds)
   - Passing Score (required, 0-100, default 70)
2. **Questions** (array):
   - "Add Question" button
   - Each question:
     - Question text (required)
     - Question type (select: multiple_choice, true_false, fill_blank, matching)
     - Options (if multiple_choice)
     - Correct Answer (depends on type)
     - Points (number, default 1)
     - Explanation (optional)
     - Hint (optional)
     - Delete button

**Validation**:

- Minimum 1 question
- Passing score 0-100
- Time limit (if provided) > 0
- Points > 0

##### 4.5.2.4 Speaking Lesson Editor

**JSONB Schema**: See `DATABASE-SCHEMA.md` section 2.3.4

**Form Sections**:

1. **Scenario**:
   - Scenario description (textarea, required)
   - Difficulty (select: beginner, intermediate, advanced)
2. **Prompts** (array):
   - "Add Prompt" button
   - Each prompt:
     - Prompt text (required)
     - Context (optional, textarea)
     - Sample Answers (array, text inputs)
     - Target Grammar (array, chips input)
     - Target Vocabulary (array, chips input)
     - Delete button
3. **Role-Play Settings** (optional):
   - AI Persona (text input, e.g., "hotel receptionist")
   - Turns (number, 1-20, default 5)
   - Enable Feedback (checkbox, default true)

**Validation**:

- Scenario required
- Difficulty required
- Minimum 1 prompt
- Turns (if provided) 1-20

#### 4.5.3 Lesson Edit Page

- **URL**: `/lessons/:id/edit`
- **Features**: Same as create page, pre-filled with existing data
- **Additional**:
  - "Preview Lesson" button (open in dialog)
  - "Duplicate Lesson" button (create copy in same section)
  - "Move to Another Section" button (select section dialog)
- **Save Behavior**: PATCH request to update lesson
- **Success**: Redirect to course edit page with success toast

### 4.6 System Monitoring (ADMIN Only)

#### 4.6.1 System Health Page

- **URL**: `/monitoring/health`
- **Features**:
  - **Health Status Cards**:
    - API Health (from `/actuator/health`)
    - Database Status (UP/DOWN)
    - Disk Space (available/total)
  - **Metrics** (from `/actuator/metrics`):
    - HTTP Request Count (by endpoint)
    - Average Response Time
    - JVM Memory Usage
    - Active Threads
  - **Refresh Button**: Manual refresh
  - **Auto-refresh**: Toggle, every 30s

#### 4.6.2 AI Usage Logs Page (Future Sprint)

- **URL**: `/monitoring/ai-usage`
- **Features**:
  - **Data Table**:
    - Columns: User, Feature, Input Tokens, Output Tokens, Cost, Timestamp
    - Filter by feature (MAGIC_FLASHCARD, ROLEPLAY, GRAMMAR_SANDBOX)
    - Date range picker
  - **Summary Stats**:
    - Total API Calls (today/week/month)
    - Total Cost (today/week/month)
    - Average Cost per Call
  - **Export**: CSV export button

---

## 5. UI/UX Design System

### 5.1 shadcn/ui Theme Configuration

```typescript
// components.json
{
  "style": "default",
  "rsc": false,
  "tsx": true,
  "tailwind": {
    "config": "tailwind.config.js",
    "css": "src/styles/globals.css",
    "baseColor": "zinc",
    "cssVariables": true
  },
  "aliases": {
    "components": "@/components",
    "utils": "@/lib/utils"
  }
}
```

### 5.2 Color Palette

```css
/* src/styles/globals.css */
@layer base {
  :root {
    --background: 0 0% 100%;
    --foreground: 240 10% 3.9%;
    --card: 0 0% 100%;
    --card-foreground: 240 10% 3.9%;
    --popover: 0 0% 100%;
    --popover-foreground: 240 10% 3.9%;
    --primary: 217 91% 60%; /* Blue */
    --primary-foreground: 0 0% 98%;
    --secondary: 240 4.8% 95.9%;
    --secondary-foreground: 240 5.9% 10%;
    --muted: 240 4.8% 95.9%;
    --muted-foreground: 240 3.8% 46.1%;
    --accent: 240 4.8% 95.9%;
    --accent-foreground: 240 5.9% 10%;
    --destructive: 0 84.2% 60.2%; /* Red */
    --destructive-foreground: 0 0% 98%;
    --border: 240 5.9% 90%;
    --input: 240 5.9% 90%;
    --ring: 217 91% 60%;
    --radius: 0.5rem;
  }

  .dark {
    --background: 240 10% 3.9%;
    --foreground: 0 0% 98%;
    /* ... dark mode colors */
  }
}
```

### 5.3 Typography

```typescript
// Tailwind Typography Plugin
export const typography = {
  h1: "scroll-m-20 text-4xl font-extrabold tracking-tight lg:text-5xl",
  h2: "scroll-m-20 border-b pb-2 text-3xl font-semibold tracking-tight first:mt-0",
  h3: "scroll-m-20 text-2xl font-semibold tracking-tight",
  h4: "scroll-m-20 text-xl font-semibold tracking-tight",
  p: "leading-7 [&:not(:first-child)]:mt-6",
  lead: "text-xl text-muted-foreground",
  large: "text-lg font-semibold",
  small: "text-sm font-medium leading-none",
  muted: "text-sm text-muted-foreground",
};
```

### 5.4 Component Examples

#### 5.4.1 Data Table with Pagination

```typescript
// components/shared/DataTable.tsx
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/select";

interface DataTableProps<T> {
  columns: ColumnDef<T>[];
  data: T[];
  pagination: {
    pageIndex: number;
    pageSize: number;
    totalPages: number;
  };
  onPaginationChange: (page: number, size: number) => void;
  isLoading?: boolean;
}

export function DataTable<T>({
  columns,
  data,
  pagination,
  onPaginationChange,
  isLoading,
}: DataTableProps<T>) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border">
        <Table>
          <TableHeader>{/* Render headers */}</TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={columns.length} className="text-center">
                  Loading...
                </TableCell>
              </TableRow>
            ) : data.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columns.length} className="text-center">
                  No results.
                </TableCell>
              </TableRow>
            ) : (
              data.map((row, i) => (
                <TableRow key={i}>{/* Render cells */}</TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Pagination Controls */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <p className="text-sm font-medium">Rows per page</p>
          <Select
            value={pagination.pageSize.toString()}
            onValueChange={(value) => onPaginationChange(0, parseInt(value))}
          >
            {/* Options: 10, 25, 50 */}
          </Select>
        </div>

        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() =>
              onPaginationChange(pagination.pageIndex - 1, pagination.pageSize)
            }
            disabled={pagination.pageIndex === 0}
          >
            Previous
          </Button>

          <span className="text-sm">
            Page {pagination.pageIndex + 1} of {pagination.totalPages}
          </span>

          <Button
            variant="outline"
            size="sm"
            onClick={() =>
              onPaginationChange(pagination.pageIndex + 1, pagination.pageSize)
            }
            disabled={pagination.pageIndex >= pagination.totalPages - 1}
          >
            Next
          </Button>
        </div>
      </div>
    </div>
  );
}
```

#### 5.4.2 Confirmation Dialog

```typescript
// components/shared/ConfirmDialog.tsx
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

interface ConfirmDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  variant?: "default" | "destructive";
}

export function ConfirmDialog({
  open,
  onOpenChange,
  title,
  description,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  onConfirm,
  variant = "default",
}: ConfirmDialogProps) {
  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{title}</AlertDialogTitle>
          <AlertDialogDescription>{description}</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>{cancelLabel}</AlertDialogCancel>
          <AlertDialogAction
            onClick={onConfirm}
            className={
              variant === "destructive"
                ? "bg-destructive text-destructive-foreground"
                : ""
            }
          >
            {confirmLabel}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
```

### 5.5 Layout Components

#### 5.5.1 Sidebar

```typescript
// components/layout/Sidebar.tsx
import { NavLink } from "react-router-dom";
import { cn } from "@/lib/utils";
import {
  LayoutDashboard,
  Users,
  BookOpen,
  FileText,
  Activity,
  Settings,
} from "lucide-react";

const navigation = [
  {
    name: "Dashboard",
    href: "/",
    icon: LayoutDashboard,
    roles: ["ADMIN", "CONTENT_MANAGER"],
  },
  { name: "Users", href: "/users", icon: Users, roles: ["ADMIN"] },
  {
    name: "Courses",
    href: "/courses",
    icon: BookOpen,
    roles: ["ADMIN", "CONTENT_MANAGER"],
  },
  {
    name: "Lessons",
    href: "/lessons",
    icon: FileText,
    roles: ["ADMIN", "CONTENT_MANAGER"],
  },
  {
    name: "Monitoring",
    href: "/monitoring/health",
    icon: Activity,
    roles: ["ADMIN"],
  },
  { name: "Settings", href: "/settings", icon: Settings, roles: ["ADMIN"] },
];

export function Sidebar() {
  const { user } = useAuthStore();

  const filteredNav = navigation.filter((item) =>
    item.roles.includes(user.role)
  );

  return (
    <aside className="w-64 border-r bg-card">
      <div className="p-6">
        <h1 className="text-2xl font-bold text-primary">LEXIA Admin</h1>
      </div>

      <nav className="space-y-1 px-3">
        {filteredNav.map((item) => (
          <NavLink
            key={item.name}
            to={item.href}
            className={({ isActive }) =>
              cn(
                "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
                isActive
                  ? "bg-primary text-primary-foreground"
                  : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
              )
            }
          >
            <item.icon className="h-5 w-5" />
            {item.name}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
```

#### 5.5.2 Header

```typescript
// components/layout/Header.tsx
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { User, LogOut } from "lucide-react";
import { useAuthStore } from "@/store/authStore";

export function Header() {
  const { user, logout } = useAuthStore();

  const initials = user.firstName[0] + user.lastName[0];

  return (
    <header className="border-b bg-card">
      <div className="flex h-16 items-center justify-between px-6">
        <div>{/* Breadcrumb will go here */}</div>

        <div className="flex items-center gap-4">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                className="relative h-10 w-10 rounded-full"
              >
                <Avatar>
                  <AvatarFallback>{initials}</AvatarFallback>
                </Avatar>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuLabel>
                <div className="flex flex-col space-y-1">
                  <p className="text-sm font-medium">
                    {user.firstName} {user.lastName}
                  </p>
                  <p className="text-xs text-muted-foreground">{user.email}</p>
                  <p className="text-xs text-muted-foreground">
                    Role: {user.role}
                  </p>
                </div>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem>
                <User className="mr-2 h-4 w-4" />
                Profile
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={logout}>
                <LogOut className="mr-2 h-4 w-4" />
                Logout
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </header>
  );
}
```

### 5.6 Responsive Design

- **Breakpoints** (Tailwind defaults):

  - `sm`: 640px
  - `md`: 768px
  - `lg`: 1024px
  - `xl`: 1280px
  - `2xl`: 1536px

- **Responsive Behavior**:
  - **Mobile (<768px)**: Sidebar collapsed to hamburger menu
  - **Tablet (768px-1024px)**: Sidebar visible, content adjusts
  - **Desktop (>1024px)**: Full layout with sidebar and content

---

## 6. API Integration

### 6.1 Axios Configuration

```typescript
// src/lib/api.ts
import axios, { AxiosError } from "axios";
import { useAuthStore } from "@/store/authStore";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8088/api/v1";

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor: Add JWT token
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: Handle errors and token refresh
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config;

    // If 401 and not already retried, try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const { refreshToken } = useAuthStore.getState();
        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken,
        });

        const { accessToken } = response.data;
        useAuthStore.getState().setToken(accessToken);

        // Retry original request with new token
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        // Refresh failed, logout user
        useAuthStore.getState().logout();
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
```

### 6.2 API Services

#### 6.2.1 Auth API

```typescript
// src/features/auth/api/authApi.ts
import { api } from "@/lib/api";
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from "@/types/auth.types";

export const authApi = {
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>("/auth/login", data);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<RegisterResponse> => {
    const response = await api.post<RegisterResponse>("/auth/register", data);
    return response.data;
  },

  logout: async (refreshToken: string): Promise<void> => {
    await api.post("/auth/logout", { refreshToken });
  },

  refreshToken: async (
    refreshToken: string
  ): Promise<{ accessToken: string }> => {
    const response = await api.post("/auth/refresh", { refreshToken });
    return response.data;
  },
};
```

#### 6.2.2 Courses API

```typescript
// src/features/courses/api/coursesApi.ts
import { api } from "@/lib/api";
import type {
  Course,
  CreateCourseRequest,
  UpdateCourseRequest,
  CourseListResponse,
  CourseSearchParams,
} from "@/types/course.types";

export const coursesApi = {
  list: async (params: CourseSearchParams): Promise<CourseListResponse> => {
    const response = await api.get<CourseListResponse>("/courses", { params });
    return response.data;
  },

  getById: async (id: number): Promise<Course> => {
    const response = await api.get<Course>(`/courses/${id}`);
    return response.data;
  },

  create: async (data: CreateCourseRequest): Promise<Course> => {
    const response = await api.post<Course>("/courses", data);
    return response.data;
  },

  update: async (id: number, data: UpdateCourseRequest): Promise<Course> => {
    const response = await api.put<Course>(`/courses/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/courses/${id}`);
  },

  publish: async (id: number): Promise<Course> => {
    const response = await api.post<Course>(`/courses/${id}/publish`);
    return response.data;
  },

  unpublish: async (id: number): Promise<Course> => {
    const response = await api.post<Course>(`/courses/${id}/unpublish`);
    return response.data;
  },
};
```

### 6.3 TanStack Query Hooks

```typescript
// src/features/courses/hooks/useCourses.ts
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { coursesApi } from "../api/coursesApi";
import type {
  CourseSearchParams,
  CreateCourseRequest,
} from "@/types/course.types";
import { toast } from "@/hooks/use-toast";

export const useCourses = (params: CourseSearchParams) => {
  return useQuery({
    queryKey: ["courses", params],
    queryFn: () => coursesApi.list(params),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

export const useCourse = (id: number) => {
  return useQuery({
    queryKey: ["courses", id],
    queryFn: () => coursesApi.getById(id),
    enabled: !!id,
  });
};

export const useCreateCourse = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateCourseRequest) => coursesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["courses"] });
      toast({
        title: "Success",
        description: "Course created successfully",
      });
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description: error.response?.data?.message || "Failed to create course",
        variant: "destructive",
      });
    },
  });
};

export const usePublishCourse = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => coursesApi.publish(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ["courses"] });
      queryClient.invalidateQueries({ queryKey: ["courses", id] });
      toast({
        title: "Success",
        description: "Course published successfully",
      });
    },
    onError: (error: any) => {
      toast({
        title: "Error",
        description:
          error.response?.data?.message || "Failed to publish course",
        variant: "destructive",
      });
    },
  });
};
```

---

## 7. Security Requirements

### 7.1 Authentication

- **Token Storage**: localStorage (same as lexia-web)
- **Token Format**: JWT Bearer token
- **Token Expiry**: 15 minutes (access), 7 days (refresh)
- **Refresh Strategy**: Automatic via Axios interceptor
- **Logout**: Clear tokens + revoke refresh token on backend

### 7.2 Authorization

- **Role Check**: Client-side via `<RoleGuard>` component
- **API Enforcement**: Backend validates JWT role claim
- **Protected Routes**: All routes except `/login` require auth
- **Role-based UI**: Hide/show features based on user role

### 7.3 Input Validation

- **Client-side**: Zod schemas for all forms
- **Server-side**: Backend validates all inputs (redundant but secure)
- **XSS Prevention**: React escapes by default
- **SQL Injection**: Backend uses JPA (parameterized queries)

### 7.4 HTTPS

- **Production**: Enforce HTTPS only
- **Development**: HTTP allowed on localhost

### 7.5 CORS

- **Backend Configuration**: Allow admin site origin
- **Credentials**: Include credentials in requests (`withCredentials: true`)

---

## 8. Development Guidelines

### 8.1 Code Standards

- **TypeScript**: Strict mode enabled
- **ESLint**: Enforce rules (Airbnb style guide)
- **Prettier**: Auto-format on save
- **Naming Conventions**:
  - Components: PascalCase (e.g., `UserTable.tsx`)
  - Hooks: camelCase with `use` prefix (e.g., `useCourses.ts`)
  - Files: kebab-case for non-components (e.g., `auth-api.ts`)
  - Constants: UPPER_SNAKE_CASE (e.g., `API_BASE_URL`)

### 8.2 Component Structure

```typescript
// Example: CourseTable.tsx
import { useState } from "react";
import { DataTable } from "@/components/shared/DataTable";
import { useCourses } from "../hooks/useCourses";
import type { CourseSearchParams } from "@/types/course.types";

interface CourseTableProps {
  initialFilters?: CourseSearchParams;
}

export function CourseTable({ initialFilters = {} }: CourseTableProps) {
  const [filters, setFilters] = useState<CourseSearchParams>(initialFilters);

  const { data, isLoading, error } = useCourses(filters);

  if (error) {
    return <div>Error loading courses</div>;
  }

  return (
    <DataTable
      columns={columns}
      data={data?.content || []}
      pagination={{
        pageIndex: data?.pageable.pageNumber || 0,
        pageSize: data?.pageable.pageSize || 10,
        totalPages: data?.totalPages || 0,
      }}
      onPaginationChange={(page, size) => {
        setFilters({ ...filters, page, size });
      }}
      isLoading={isLoading}
    />
  );
}
```

### 8.3 Error Handling

```typescript
// Global error handler
import { AxiosError } from "axios";

export const handleApiError = (error: unknown): string => {
  if (error instanceof AxiosError) {
    // RFC 7807 Problem Details
    const problem = error.response?.data;
    if (problem?.message) {
      return problem.message;
    }

    // Validation errors
    if (problem?.errors && Array.isArray(problem.errors)) {
      return problem.errors.map((e: any) => e.message).join(", ");
    }

    // HTTP status message
    return error.response?.statusText || "An error occurred";
  }

  return "An unexpected error occurred";
};
```

### 8.4 Testing Strategy

#### Unit Tests

- **Framework**: Vitest
- **Coverage**: 70% minimum
- **Focus**: Utility functions, hooks, validation logic

#### Integration Tests

- **Framework**: React Testing Library
- **Coverage**: 60% minimum
- **Focus**: Form submissions, API interactions

#### E2E Tests (Future)

- **Framework**: Playwright
- **Coverage**: Critical flows only
- **Focus**: Login, create course, publish course

### 8.5 Performance Optimization

- **Code Splitting**: Lazy load routes with `React.lazy()`
- **Memoization**: Use `useMemo` and `useCallback` for expensive computations
- **Virtual Scrolling**: For large data tables (react-window)
- **Image Optimization**: Use `<img loading="lazy">` for thumbnails
- **Bundle Size**: Monitor with `vite-plugin-bundle-visualizer`

### 8.6 Accessibility

- **WCAG 2.1 AA Compliance**
- **Keyboard Navigation**: All interactive elements accessible via Tab
- **Screen Reader Support**: ARIA labels on all icons and buttons
- **Focus Indicators**: Visible focus rings (default shadcn/ui behavior)
- **Color Contrast**: Minimum 4.5:1 for normal text, 3:1 for large text

---

## 9. Deployment

### 9.1 Build Configuration

```typescript
// vite.config.ts
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  build: {
    outDir: "dist",
    sourcemap: false, // Disable in production
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ["react", "react-dom", "react-router-dom"],
          ui: ["@radix-ui/react-dialog", "@radix-ui/react-dropdown-menu"],
        },
      },
    },
  },
});
```

### 9.2 Environment Variables

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8088/api/v1

# .env.production
VITE_API_BASE_URL=https://api.lexia.app/api/v1
```

### 9.3 Docker Deployment

```dockerfile
# Dockerfile
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

---

## 10. Project Milestones

### Phase 1: Foundation (Week 1-2)

- [x] Project setup (Vite + React + TypeScript)
- [x] shadcn/ui installation and theme configuration
- [x] Authentication (login, JWT handling, protected routes)
- [x] Layout components (Sidebar, Header, MainLayout)
- [x] Routing structure
- [x] Zustand auth store
- [x] TanStack Query setup

### Phase 2: Core Features (Week 3-5)

- [x] Dashboard page (stats cards, recent activity)
- [x] User management (list, create, edit, delete) - ADMIN only
- [x] Course management (list, create, edit, delete)
- [x] Section management (CRUD within course edit page)
- [x] Publish/unpublish functionality
- [x] Course preview page (with section list display)

### Phase 3: Lesson Management (Week 6-8)

- [x] Lesson type selection
- [x] Reading lesson editor
- [ ] Listening lesson editor
- [ ] Quiz lesson editor
- [ ] Speaking lesson editor
- [ ] Lesson preview
- [ ] Lesson reordering (drag-and-drop)

### Phase 4: Advanced Features (Week 9-10)

- [ ] System health monitoring (ADMIN only)
- [ ] AI usage logs (ADMIN only)
- [ ] Settings page
- [ ] User profile management
- [ ] Dark mode toggle

### Phase 5: Polish & Testing (Week 11-12)

- [ ] Comprehensive testing (unit + integration)
- [ ] Accessibility audit
- [ ] Performance optimization
- [ ] Documentation
- [ ] Deployment setup

---

## 11. Success Criteria

### Functional Requirements

- ✅ ADMIN can manage users (CRUD)
- ✅ CONTENT_MANAGER can manage courses and lessons (CRUD)
- ✅ Both roles can publish/unpublish courses
- ✅ All 4 lesson types (READING, LISTENING, QUIZ, SPEAKING) fully supported
- ✅ JSONB content validated against schemas
- ✅ Pagination, sorting, filtering on all list pages
- ✅ Real-time feedback (loading states, error messages, success toasts)
- ✅ ADMIN can monitor system health via Actuator

### Non-Functional Requirements

- ✅ **Performance**: Page load < 2s, API response < 500ms
- ✅ **Security**: JWT auth, role-based access control, input validation
- ✅ **Usability**: Intuitive UI, clear error messages, responsive design
- ✅ **Accessibility**: WCAG 2.1 AA compliant
- ✅ **Maintainability**: TypeScript, ESLint, Prettier, clean architecture
- ✅ **Test Coverage**: 70% unit, 60% integration

---

## 12. Future Enhancements

- **Bulk Operations**: Bulk publish/unpublish courses
- **Content Versioning**: Track lesson content changes over time
- **Collaboration**: Multiple content managers working on same course
- **AI Integration**: AI-assisted lesson content generation
- **Analytics Dashboard**: Detailed course performance metrics
- **Media Library**: Centralized media management for images/audio
- **Translation**: Multi-language support for admin UI
- **Mobile Admin App**: React Native admin app for on-the-go management

---

**Document Version**: 1.2.0  
**Status**: In Progress (Phase 3)  
**Next Review**: After Phase 3 completion  
**Approved By**: [To be filled]
