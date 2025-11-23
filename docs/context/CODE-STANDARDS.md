# LEXIA - Code Standards & Conventions

## Backend (Java/Spring Boot)

### Naming Conventions

- Classes: PascalCase (`UserService`, `JwtTokenProvider`)
- Methods: camelCase (`generateToken`, `validateUser`)
- Constants: UPPER_SNAKE_CASE (`JWT_EXPIRATION_MS`)
- Packages: lowercase with domain-driven structure
  ```
  com.lexia.api
  ├── auth
  ├── course
  ├── lesson
  ├── user
  └── ai
  ```

### Code Organization

```java
@Service
public class MyService {
    // 1. Constants
    private static final Logger LOG = LoggerFactory.getLogger(...);

    // 2. Autowired dependencies
    @Autowired
    private MyRepository repository;

    // 3. Lifecycle methods
    @PostConstruct
    public void init() { }

    // 4. Public methods
    public void publicMethod() { }

    // 5. Private helper methods
    private void privateHelper() { }
}
```

### Error Handling

✅ **DO:**

```java
try {
    // code
} catch (SpecificException e) {
    LOG.error("Specific error message", e);
    throw new CustomException("User-friendly message", e);
}
```

❌ **DON'T:**

```java
catch (Exception e) {
    e.printStackTrace();  // Never use this
}
```

### Security Practices

- ✅ Use Bcrypt for passwords
- ✅ Hash tokens before storing
- ✅ Validate all inputs
- ✅ Use HTTPS only
- ✅ Set JWT expiration
- ❌ Don't log sensitive data (tokens, passwords)
- ❌ Don't commit API keys to git

### Testing Requirements

- Minimum 70% code coverage
- Unit tests for services
- Integration tests for APIs
- Test naming: `test[Method][Scenario][Expected]`
  ```java
  @Test
  void testGenerateTokenWithValidUser_ReturnsValidToken() { }
  ```

### Documentation

- JavaDoc for public methods
- Meaningful variable names
- Comments for non-obvious logic
  ```java
  /**
   * Generates JWT access token for authenticated user.
   *
   * @param userId user ID
   * @return JWT token valid for 15 minutes
   * @throws InvalidUserException if user not found
   */
  public String generateAccessToken(UUID userId) { }
  ```

## Frontend Web (TypeScript/Next.js)

### Component Structure

```
components/
├── [FeatureName]/
│   ├── index.ts              # Barrel export
│   ├── [FeatureName].tsx     # Component
│   ├── [FeatureName].module.css
│   └── [FeatureName].test.tsx
```

### Naming Conventions

- Components: PascalCase (`UserProfile`, `RolePlayModal`, `CourseCard`)
- Files: kebab-case (`user-profile.tsx`, `course-card.tsx`)
- Functions/variables: camelCase (`handleLogin`, `isLoading`)
- Types/Interfaces: PascalCase with descriptive suffix (`User`, `LoginFormProps`, `ApiResponse`)
- Constants: UPPER_SNAKE_CASE (`API_BASE_URL`, `MAX_RETRY_ATTEMPTS`)

### Code Organization

```typescript
"use client"; // If client component

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

// 1. Type definitions
interface MyComponentProps {
  userId: string;
  onSuccess: () => void;
}

// 2. Component
export function MyComponent({ userId, onSuccess }: MyComponentProps) {
  // 3. Hooks
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 4. Effects
  useEffect(() => {
    // Effect logic
  }, []);

  // 5. Event handlers
  const handleSubmit = async () => {
    // Handler logic
  };

  // 6. Render helpers
  const renderContent = () => {
    // Render logic
  };

  // 7. JSX
  return <div>{/* Component markup */}</div>;
}
```

### Form Validation

✅ **DO:**

```typescript
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

const loginSchema = z.object({
  email: z.string().email("Invalid email address"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});

type LoginFormData = z.infer<typeof loginSchema>;

export function LoginForm() {
  const form = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      await authService.login(data);
      toast.success("Login successful");
    } catch (error) {
      toast.error(error.message);
    }
  };

  return (
    <form onSubmit={form.handleSubmit(onSubmit)}>{/* Form fields */}</form>
  );
}
```

❌ **DON'T:**

```typescript
// No validation
const handleSubmit = () => {
  login(email, password); // Unsafe!
};
```

### Error Handling

✅ **DO:**

```typescript
// API interceptor with retry
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.code === "ERR_NETWORK") {
      toast.error("No internet connection");
      return Promise.reject({ code: "NETWORK" });
    }

    if (error.code === "ECONNABORTED") {
      return retryRequest(error.config, 3);
    }

    if (error.response?.status >= 500) {
      return retryRequest(error.config, 3);
    }

    if (error.response?.status === 401) {
      return handleTokenRefresh(error);
    }

    return Promise.reject(error);
  }
);
```

### Responsive Design

✅ **DO:**

```typescript
// Tailwind CSS mobile-first approach
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  <div className="p-4 md:p-6 lg:p-8">
    <h2 className="text-xl md:text-2xl lg:text-3xl">Title</h2>
  </div>
</div>

// Breakpoints: 320px, 375px, 768px, 1024px, 1280px, 1920px
```

### Accessibility

✅ **DO:**

```typescript
<button
  aria-label="Close dialog"
  onClick={onClose}
>
  <X className="h-4 w-4" aria-hidden="true" />
</button>

<input
  aria-describedby="email-error"
  aria-invalid={errors.email ? 'true' : 'false'}
/>
{errors.email && (
  <span id="email-error" className="text-red-500">
    {errors.email.message}
  </span>
)}
```

### Testing

```typescript
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

describe("LoginForm", () => {
  it("should validate email format", async () => {
    render(<LoginForm onSuccess={jest.fn()} />);

    const emailInput = screen.getByLabelText(/email/i);
    await userEvent.type(emailInput, "invalid");

    const submitBtn = screen.getByRole("button", { name: /login/i });
    await userEvent.click(submitBtn);

    expect(screen.getByText(/invalid email/i)).toBeInTheDocument();
  });

  it("should call onSuccess after successful login", async () => {
    const onSuccess = jest.fn();
    render(<LoginForm onSuccess={onSuccess} />);

    // Fill form and submit
    await userEvent.type(screen.getByLabelText(/email/i), "user@test.com");
    await userEvent.type(screen.getByLabelText(/password/i), "password123");
    await userEvent.click(screen.getByRole("button", { name: /login/i }));

    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalled();
    });
  });
});
```

### Security Practices (Web)

- ✅ Store tokens in localStorage (temporary, will migrate to httpOnly cookies)
- ✅ Clear Authorization header format: `Bearer ${token}`
- ✅ Clear tokens on logout
- ✅ Validate all form inputs (Zod)
- ✅ Handle 401 with token refresh
- ✅ Add retry logic (3 attempts)
- ✅ Use environment variables for API URLs
- ❌ Don't store passwords in localStorage
- ❌ Don't skip form validation
- ❌ Don't ignore error states

### Code Style (Web)

- Use TypeScript strict mode (no `any`)
- Functional components with hooks
- Props interface for each component
- ESLint + Prettier configured
- Use Next.js `<Image>` instead of `<img>`
- Use `'use client'` directive when needed
- Prefer server components by default

---

## Mobile (TypeScript/React Native + Expo)

### Component Structure

```
app/
├── auth/
│   ├── LoginScreen.tsx
│   └── RegisterScreen.tsx
├── tabs/
│   ├── HomeScreen.tsx
│   ├── CoursesScreen.tsx
│   ├── ProgressScreen.tsx
│   └── ProfileScreen.tsx
components/
├── [FeatureName]/
│   ├── index.ts              # Barrel export
│   ├── [FeatureName].tsx     # Component
│   └── [FeatureName].test.tsx
```

### Naming Conventions

- Screens: PascalCase with suffix (`LoginScreen`, `CourseDetailScreen`)
- Components: PascalCase (`CourseCard`, `LessonItem`)
- Files: PascalCase matching component name
- Functions/variables: camelCase (`handlePress`, `isVisible`)
- Types/Interfaces: PascalCase (`User`, `NavigationProps`)
- Constants: UPPER_SNAKE_CASE (`API_BASE_URL`)

### Code Organization

```typescript
import React, { useState, useEffect } from "react";
import { View, Text, StyleSheet } from "react-native";
import { Button, TextInput } from "react-native-paper";
import { NativeStackNavigationProp } from "@react-navigation/native-stack";

// 1. Type definitions
interface MyScreenProps {
  navigation: NativeStackNavigationProp<RootStackParamList, "MyScreen">;
  route: RouteProp<RootStackParamList, "MyScreen">;
}

// 2. Component
export function MyScreen({ navigation, route }: MyScreenProps) {
  // 3. State
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // 4. Effects
  useEffect(() => {
    // Effect logic
  }, []);

  // 5. Event handlers
  const handlePress = async () => {
    // Handler logic
  };

  // 6. JSX
  return <View style={styles.container}>{/* Component markup */}</View>;
}

// 7. Styles (always use StyleSheet)
const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
});
```

### Navigation Type Safety

✅ **DO:**

```typescript
// types/navigation.ts
export type RootStackParamList = {
  Login: undefined;
  Register: undefined;
  MainTabs: undefined;
  CourseDetail: { courseId: string };
  LessonViewer: { lessonId: string; courseId: string };
};

export type TabParamList = {
  Home: undefined;
  Courses: undefined;
  Progress: undefined;
  Profile: undefined;
};

// In component
import { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { RouteProp } from "@react-navigation/native";

type Props = {
  navigation: NativeStackNavigationProp<RootStackParamList, "CourseDetail">;
  route: RouteProp<RootStackParamList, "CourseDetail">;
};

// Usage
navigation.navigate("LessonViewer", {
  lessonId: "123",
  courseId: "456",
});
```

### Input Validation

✅ **DO:**

```typescript
const handleLogin = async () => {
  // Manual validation (or use libraries like formik)
  if (!email || !email.includes("@")) {
    setError("Please enter a valid email");
    return;
  }

  if (!password || password.length < 8) {
    setError("Password must be at least 8 characters");
    return;
  }

  setLoading(true);
  setError("");

  try {
    await authService.login(email, password);
    navigation.replace("MainTabs");
  } catch (err: any) {
    setError(err.response?.data?.message || "Login failed");
  } finally {
    setLoading(false);
  }
};
```

### Platform-Specific Code

✅ **DO:**

```typescript
import { Platform, StyleSheet } from "react-native";

const styles = StyleSheet.create({
  container: {
    paddingTop: Platform.OS === "ios" ? 20 : 0,
    ...Platform.select({
      ios: {
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.25,
        shadowRadius: 3.84,
      },
      android: {
        elevation: 5,
      },
    }),
  },
  text: {
    fontFamily: Platform.select({
      ios: "System",
      android: "Roboto",
    }),
  },
});
```

### Error Handling (Mobile)

✅ **DO:**

```typescript
// API interceptor
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshToken = await AsyncStorage.getItem("refreshToken");
      if (refreshToken) {
        try {
          const response = await axios.post("/api/auth/refresh", {
            refreshToken,
          });
          await AsyncStorage.setItem("accessToken", response.data.accessToken);
          error.config.headers.Authorization = `Bearer ${response.data.accessToken}`;
          return axios(error.config);
        } catch {
          await AsyncStorage.multiRemove(["accessToken", "refreshToken"]);
          // Navigate to login
        }
      }
    }
    return Promise.reject(error);
  }
);
```

### Accessibility (Mobile)

✅ **DO:**

```typescript
import { TouchableOpacity, Text } from 'react-native';

<TouchableOpacity
  accessible={true}
  accessibilityRole="button"
  accessibilityLabel="Close button"
  accessibilityHint="Closes the current screen"
  onPress={onClose}
>
  <Text>Close</Text>
</TouchableOpacity>

<TextInput
  accessible={true}
  accessibilityLabel="Email input field"
  accessibilityHint="Enter your email address"
/>
```

### Testing (Mobile)

```typescript
import { render, fireEvent, waitFor } from "@testing-library/react-native";

describe("LoginScreen", () => {
  it("should validate email format", async () => {
    const { getByLabelText, getByText, findByText } = render(
      <LoginScreen navigation={mockNavigation} route={mockRoute} />
    );

    const emailInput = getByLabelText("Email");
    fireEvent.changeText(emailInput, "invalid");

    const loginBtn = getByText("Login");
    fireEvent.press(loginBtn);

    await waitFor(() => {
      expect(findByText(/valid email/i)).toBeTruthy();
    });
  });
});
```

### Security Practices (Mobile)

- ✅ Store tokens in AsyncStorage
- ✅ Clear Authorization header: `Bearer ${token}`
- ✅ Clear tokens on logout (AsyncStorage.multiRemove)
- ✅ Validate all inputs
- ✅ Handle 401 with token refresh
- ✅ Add retry logic (3 attempts)
- ✅ Use environment variables
- ❌ Don't store sensitive data in plain AsyncStorage (except tokens)
- ❌ Don't skip input validation
- ❌ Don't ignore navigation type safety

### Code Style (Mobile)

- Use TypeScript strict mode (no `any`)
- Functional components with hooks
- Props interface for each screen/component
- Always use StyleSheet (no inline styles)
- Use React Native Paper for UI consistency
- Type-safe navigation (React Navigation types)
- Handle both iOS & Android platforms
- Add accessibility labels to all interactive elements

## Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

Examples:

```
feat(auth): implement JWT refresh token rotation
fix(role-play): handle Gemini API timeout gracefully
docs(api): add role-play endpoints documentation
chore(deps): upgrade Spring Boot to 3.3.1
test(grammar): add unit tests for sandbox service
```

---

## Code Review Checklist

### All Platforms

- [ ] Follows coding standards
- [ ] Tests included (Backend: 70%+, Frontend: 60%+)
- [ ] No security vulnerabilities
- [ ] Performance considered
- [ ] Error handling present
- [ ] Documentation complete
- [ ] No hardcoded values/secrets
- [ ] TypeScript strict mode (Frontend)
- [ ] No `any` types without justification

### Backend Specific

- [ ] DTOs used (no entity exposure)
- [ ] Input validation (@Valid)
- [ ] Proper HTTP status codes
- [ ] Transaction management
- [ ] Exception handling
- [ ] No sensitive data in logs
- [ ] JavaDoc for public methods

### Frontend Web Specific

- [ ] Form validation (Zod + React Hook Form)
- [ ] Responsive design tested (320px - 1920px)
- [ ] Accessibility (ARIA, keyboard navigation)
- [ ] Error boundaries implemented
- [ ] API error handling with retry
- [ ] Token management (localStorage)
- [ ] Loading states/skeletons present
- [ ] Next.js Image component used

### Mobile Specific

- [ ] Platform-specific code handled (iOS/Android)
- [ ] Navigation types defined
- [ ] AsyncStorage error handling
- [ ] iOS & Android tested
- [ ] Accessibility labels added
- [ ] StyleSheet usage (no inline styles)
- [ ] React Native Paper components used
- [ ] Safe area insets handled
