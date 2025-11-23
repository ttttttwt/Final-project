# Coding Standards - LEXIA

> **Last Updated**: November 23, 2025  
> **Version**: 1.0  
> **Applies To**: Backend (Spring Boot), Frontend Web (Next.js), Frontend Mobile (React Native)

---

## 📋 Table of Contents

1. [Backend Standards](#backend-standards)
2. [Frontend Web Standards](#frontend-web-standards)
3. [Mobile Standards](#mobile-standards)
4. [Testing Requirements](#testing-requirements)
5. [Security Standards](#security-standards)

---

## Backend Standards

### Package Structure

```
com.lexia.backend/
├── auth/              # JWT, Security filters
├── controller/        # REST API endpoints
├── service/           # Business logic
│   ├── impl/          # Service implementations
├── repository/        # JPA repositories
├── entity/            # JPA entities
├── dto/               # Data Transfer Objects
├── mapper/            # Entity-DTO mappers
├── config/            # Configuration classes
├── exception/         # Custom exceptions
├── validation/        # Custom validators
├── specification/     # JPA Specifications
├── util/              # Utility classes
├── converter/         # Custom converters
├── seeder/            # Database seeders
└── common/            # Shared utilities
```

### Security Rules (CRITICAL)

```java
// ✅ DO
@PostMapping("/register")
public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterDTO dto) {
    String hashed = passwordEncoder.encode(dto.getPassword()); // Bcrypt cost 12
    User user = userService.create(dto.getEmail(), hashed);
    LOG.info("User registered: {}", user.getEmail());
    return ResponseEntity.ok(UserMapper.toDTO(user));
}

// ❌ NEVER
- Plain-text passwords/tokens
- Log sensitive data
- Expose entities directly
- Hardcode secrets
```

### JWT Token Handling

- **Access Token**: 15 minutes expiry
- **Refresh Token**: 7 days expiry
- Return tokens in response body (stored in localStorage on frontend)
- Hash refresh tokens before DB storage (SHA-256)
- Never log tokens

### Gemini API Integration

```java
@Retryable(maxAttempts = 3)
public RolePlayDTO generateRolePlay(String context) {
    try {
        String response = geminiClient.generate(prompt);
        aiUsageLogRepository.save(new AIUsageLog(...)); // Track cost
        return parseResponse(response);
    } catch (TimeoutException e) {
        return getFallbackScenario(); // Always have fallback
    }
}
```

### Testing Requirements

- **Coverage**: 70%+ overall, Services ≥ 80%
- **Types**: Unit + Integration tests
- **Test Naming**: `should_DoSomething_When_Condition`
- **Mock External Services**: Always mock Gemini API, email services

---

## Frontend Web Standards

### Project Structure

```
lexia-web/
├── app/                    # Next.js App Router pages
├── components/            # React components
├── services/             # API service functions
├── store/                # Zustand stores
├── types/                # TypeScript types
├── lib/                  # Utilities, helpers
├── hooks/                # Custom React hooks
├── tests/                # Test files
└── public/               # Static assets
```

### Security Rules (CRITICAL)

```typescript
// ✅ DO: localStorage for tokens (temporary approach)
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Note: Will migrate to httpOnly cookies for better XSS protection later
```

### API Error Handling

```typescript
// ✅ Comprehensive error handling
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    // Network errors
    if (error.code === "ERR_NETWORK") {
      toast.error("No internet connection");
      return Promise.reject({ code: "NETWORK" });
    }

    // Timeout
    if (error.code === "ECONNABORTED") {
      toast.error("Request timeout");
      return retryRequest(error.config, 3); // Retry 3 times
    }

    // Server errors
    if (error.response?.status >= 500) {
      toast.error("Server error");
      return retryRequest(error.config, 3);
    }

    // 401 - Refresh token
    if (error.response?.status === 401) {
      return handleTokenRefresh(error);
    }

    return Promise.reject(error);
  }
);
```

### Component Standards

```typescript
// ✅ DO: Type-safe components with validation
interface LoginFormProps {
  onSuccess: () => void;
}

const loginSchema = z.object({
  email: z.string().email("Invalid email"),
  password: z.string().min(8, "Min 8 characters"),
});

export function LoginForm({ onSuccess }: LoginFormProps) {
  const form = useForm<z.infer<typeof loginSchema>>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: z.infer<typeof loginSchema>) => {
    try {
      await authService.login(data);
      toast.success("Login successful");
      onSuccess();
    } catch (error) {
      toast.error(error.message);
    }
  };

  return <form onSubmit={form.handleSubmit(onSubmit)}>...</form>;
}
```

### Responsive Design

```typescript
// ✅ Mobile-first, test all breakpoints
// 320px (Mobile S), 375px (Mobile M), 768px (Tablet)
// 1024px (Desktop S), 1280px (Desktop M), 1920px (Desktop L)

<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  {/* Mobile: 1 column, Tablet: 2 columns, Desktop: 3 columns */}
</div>
```

### Accessibility (WCAG AA)

```typescript
// ✅ ARIA labels, keyboard nav, contrast
<button aria-label="Close dialog" onClick={onClose}>
  <X className="h-4 w-4" aria-hidden="true" />
</button>

<input
  aria-describedby="email-error"
  aria-invalid={errors.email ? "true" : "false"}
/>
{errors.email && <span id="email-error">{errors.email.message}</span>}
```

### Testing Requirements

```typescript
// ✅ 60%+ coverage (global), 80%+ (services)
describe("LoginForm", () => {
  it("validates email format", async () => {
    render(<LoginForm onSuccess={jest.fn()} />);

    const emailInput = screen.getByLabelText(/email/i);
    await userEvent.type(emailInput, "invalid");

    const submitBtn = screen.getByRole("button", { name: /login/i });
    await userEvent.click(submitBtn);

    expect(screen.getByText(/invalid email/i)).toBeInTheDocument();
  });
});
```

---

## Mobile Standards

### Project Structure

```
lexia-mobile/
├── app/                    # Screen components
│   ├── auth/              # Auth screens
│   │   ├── LoginScreen.tsx
│   │   └── RegisterScreen.tsx
│   ├── tabs/              # Tab navigation screens
│   │   ├── HomeScreen.tsx
│   │   ├── CoursesScreen.tsx
│   │   ├── ProgressScreen.tsx
│   │   └── ProfileScreen.tsx
│   ├── courses/           # Course screens
│   │   └── CourseDetailScreen.tsx
│   └── lessons/           # Lesson screens
│       └── LessonViewerScreen.tsx
├── components/            # Reusable components
│   └── AuthProvider.tsx  # Auth context provider
├── services/             # API service functions
│   └── api.ts            # Axios instance & API calls
├── store/                # Zustand stores
│   └── authStore.ts      # Auth state management
├── types/                # TypeScript types
│   └── index.ts          # Type definitions
├── assets/               # Static assets (images, fonts)
├── App.tsx               # Root component
├── index.ts              # Entry point
└── app.json              # Expo configuration
```

### Security Rules (CRITICAL)

```typescript
// ✅ DO: AsyncStorage for tokens (mobile approach)
import AsyncStorage from "@react-native-async-storage/async-storage";
import axios from "axios";

const api = axios.create({
  baseURL: "http://your-api-url/api",
});

// Add token to requests
api.interceptors.request.use(async (config) => {
  const token = await AsyncStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 - Token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshToken = await AsyncStorage.getItem("refreshToken");
      if (refreshToken) {
        // Attempt token refresh
        try {
          const response = await axios.post("/api/auth/refresh", {
            refreshToken,
          });
          await AsyncStorage.setItem("accessToken", response.data.accessToken);
          // Retry original request
          error.config.headers.Authorization = `Bearer ${response.data.accessToken}`;
          return axios(error.config);
        } catch {
          // Refresh failed - logout
          await AsyncStorage.multiRemove(["accessToken", "refreshToken"]);
          // Navigate to login
        }
      }
    }
    return Promise.reject(error);
  }
);
```

### Navigation Structure

```typescript
// ✅ Type-safe navigation with React Navigation
import { NavigationContainer } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";

// Define navigation types
export type RootStackParamList = {
  Login: undefined;
  Register: undefined;
  MainTabs: undefined;
  CourseDetail: { courseId: string };
  LessonViewer: { lessonId: string };
};

export type TabParamList = {
  Home: undefined;
  Courses: undefined;
  Progress: undefined;
  Profile: undefined;
};

// Use typed navigation hooks
const navigation =
  useNavigation<NativeStackNavigationProp<RootStackParamList>>();
const route = useRoute<RouteProp<RootStackParamList, "CourseDetail">>();
```

### Component Standards

```typescript
// ✅ DO: React Native Paper for UI consistency
import { Button, TextInput, Card, Text } from "react-native-paper";
import { StyleSheet, View } from "react-native";

interface LoginScreenProps {
  navigation: NativeStackNavigationProp<RootStackParamList, "Login">;
}

export function LoginScreen({ navigation }: LoginScreenProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = async () => {
    if (!email || !password) {
      setError("Email and password are required");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await api.post("/auth/login", { email, password });
      await AsyncStorage.multiSet([
        ["accessToken", response.data.accessToken],
        ["refreshToken", response.data.refreshToken],
      ]);
      navigation.replace("MainTabs");
    } catch (err: any) {
      setError(err.response?.data?.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Card style={styles.card}>
        <Card.Content>
          <Text variant="headlineMedium">Login</Text>

          <TextInput
            label="Email"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
            error={!!error}
            style={styles.input}
          />

          <TextInput
            label="Password"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            error={!!error}
            style={styles.input}
          />

          {error && <Text style={styles.error}>{error}</Text>}

          <Button
            mode="contained"
            onPress={handleLogin}
            loading={loading}
            disabled={loading}
          >
            Login
          </Button>
        </Card.Content>
      </Card>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    padding: 16,
  },
  card: {
    padding: 16,
  },
  input: {
    marginVertical: 8,
  },
  error: {
    color: "red",
    marginBottom: 8,
  },
});
```

### State Management (Zustand)

```typescript
// ✅ Same Zustand pattern as web
import { create } from "zustand";
import AsyncStorage from "@react-native-async-storage/async-storage";

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  checkAuth: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,

  login: async (email, password) => {
    const response = await api.post("/auth/login", { email, password });
    await AsyncStorage.multiSet([
      ["accessToken", response.data.accessToken],
      ["refreshToken", response.data.refreshToken],
    ]);
    set({ user: response.data.user, isAuthenticated: true });
  },

  logout: async () => {
    await AsyncStorage.multiRemove(["accessToken", "refreshToken"]);
    set({ user: null, isAuthenticated: false });
  },

  checkAuth: async () => {
    const token = await AsyncStorage.getItem("accessToken");
    if (token) {
      try {
        const response = await api.get("/auth/me");
        set({ user: response.data, isAuthenticated: true });
      } catch {
        await AsyncStorage.multiRemove(["accessToken", "refreshToken"]);
        set({ user: null, isAuthenticated: false });
      }
    }
  },
}));
```

### Platform-Specific Code

```typescript
// ✅ Handle iOS/Android differences
import { Platform } from "react-native";

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
});
```

### Accessibility

```typescript
// ✅ ARIA equivalent in React Native
<TouchableOpacity
  accessible={true}
  accessibilityLabel="Close button"
  accessibilityHint="Closes the current screen"
  accessibilityRole="button"
  onPress={onClose}
>
  <Icon name="close" size={24} />
</TouchableOpacity>
```

### Testing Requirements

```typescript
// ✅ Test mobile components
import { render, fireEvent, waitFor } from "@testing-library/react-native";

describe("LoginScreen", () => {
  it("validates email format", async () => {
    const { getByLabelText, getByText } = render(<LoginScreen />);

    const emailInput = getByLabelText("Email");
    fireEvent.changeText(emailInput, "invalid");

    const loginBtn = getByText("Login");
    fireEvent.press(loginBtn);

    await waitFor(() => {
      expect(getByText(/invalid email/i)).toBeTruthy();
    });
  });
});
```

---

## Testing Requirements

### Backend

- **Coverage**: ≥ 70% overall, Services ≥ 80%
- **Types**: Unit + Integration
- **Framework**: JUnit 5, Mockito, MockMvc
- **Run Tests**: `./gradlew test`

### Frontend Web

- **Coverage**: ≥ 60% overall, Services ≥ 80%
- **Types**: Unit + Integration
- **Framework**: Jest, React Testing Library
- **Run Tests**: `npm test`

### Mobile

- **Coverage**: ≥ 60% overall, Services ≥ 80%
- **Types**: Unit + Integration
- **Framework**: Jest, React Native Testing Library
- **Run Tests**: `npm test`

---

## Security Standards

### Do's ✅

**Backend**:

- ✅ Bcrypt passwords (cost 12)
- ✅ Return JWT in response body
- ✅ Hash refresh tokens before DB (SHA-256)
- ✅ Validate all inputs
- ✅ Use DTOs for APIs

**Frontend (Web)**:

- ✅ localStorage for tokens (temporary, will migrate to httpOnly cookies)
- ✅ Validate forms (React Hook Form + Zod)
- ✅ Handle all errors (network, timeout, 500)
- ✅ Add retry logic (3 attempts)
- ✅ Test responsive (320px - 1920px)
- ✅ ARIA labels + keyboard nav
- ✅ TypeScript strict mode

**Mobile**:

- ✅ AsyncStorage for tokens with proper encryption consideration
- ✅ Validate all inputs (manual validation or libraries)
- ✅ Handle all errors (network, timeout, server)
- ✅ Add retry logic (3 attempts)
- ✅ Test on iOS & Android
- ✅ Accessibility labels (accessible, accessibilityLabel)
- ✅ TypeScript strict mode
- ✅ Use React Native Paper for consistent UI
- ✅ Type-safe navigation (React Navigation types)

### Don'ts ❌

**Backend**:

- ❌ Store plain-text passwords/refresh tokens
- ❌ Log sensitive data (passwords, tokens)
- ❌ Skip tests (70% minimum)
- ❌ Use generic `Exception` catches

**Frontend (Web)**:

- ❌ Store passwords or sensitive data in localStorage
- ❌ Skip form validation (use Zod)
- ❌ Ignore error states (network, timeout, server)
- ❌ Forget loading states/skeletons
- ❌ Skip accessibility (ARIA, keyboard nav)
- ❌ Use `<img>` (use Next.js `<Image>`)

**Mobile**:

- ❌ Store sensitive data in plain AsyncStorage (except tokens)
- ❌ Skip platform-specific handling (iOS/Android)
- ❌ Ignore navigation type safety
- ❌ Forget error boundaries
- ❌ Skip accessibility labels
- ❌ Use inline styles without StyleSheet

**Both**:

- ❌ Edit `docs/context/` without approval
- ❌ Commit failing tests
- ❌ Work on multiple tasks at once

---

## Code Review Checklist

### All Platforms

- [ ] TypeScript strict mode enabled
- [ ] No console.log in production code
- [ ] Error handling implemented
- [ ] Loading states present
- [ ] Tests written and passing
- [ ] Code follows project structure
- [ ] No hardcoded values
- [ ] Documentation updated

### Backend Specific

- [ ] DTOs used (never expose entities)
- [ ] Input validation (@Valid)
- [ ] Proper HTTP status codes
- [ ] Transaction management
- [ ] Exception handling
- [ ] Logging (no sensitive data)

### Frontend Specific

- [ ] Form validation (Zod)
- [ ] Responsive design tested
- [ ] Accessibility (ARIA, keyboard)
- [ ] Error boundaries
- [ ] API error handling
- [ ] Token management

### Mobile Specific

- [ ] Platform-specific code handled
- [ ] Navigation types defined
- [ ] AsyncStorage error handling
- [ ] iOS & Android tested
- [ ] Accessibility labels
- [ ] StyleSheet usage

---

**Document Version**: 1.0  
**Last Updated**: November 23, 2025  
**Maintained By**: LEXIA Development Team
