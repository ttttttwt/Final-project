# Sprint 4: Setup Tasks (A6-A8)

**Date**: November 23, 2025  
**Epic**: A - Project Initialization  
**Status**: 🔵 Ready to Execute

---

## A6: Install Missing Dependencies (1 pt)

### Objective

Install all required npm packages for Sprint 4 features (React Query, Zod, NetInfo, Charts, Markdown, etc.)

### Dependencies to Install

#### Core Dependencies (Production)

```json
{
  "@tanstack/react-query": "^5.56.0",
  "@tanstack/query-async-storage-persister": "^5.56.0",
  "zod": "^3.22.4",
  "react-hook-form": "^7.49.0",
  "@react-native-community/netinfo": "^11.3.0",
  "react-native-chart-kit": "^6.12.0",
  "react-native-svg": "^14.1.0",
  "react-native-markdown-display": "^7.0.0",
  "react-native-fast-image": "^8.6.3",
  "expo-av": "~14.0.7",
  "expo-notifications": "~0.28.18",
  "expo-local-authentication": "~14.0.3",
  "react-native-toast-message": "^2.2.0"
}
```

#### Dev Dependencies (Testing)

```json
{
  "@testing-library/react-native": "^12.4.0",
  "@testing-library/jest-native": "^5.4.3",
  "jest": "^29.7.0",
  "@types/jest": "^29.5.11"
}
```

### Installation Steps

#### Step 1: Core Dependencies

```bash
# Navigate to mobile project
cd e:\final-project\lexia-mobile

# Install core dependencies
npm install @tanstack/react-query@^5.56.0
npm install @tanstack/query-async-storage-persister@^5.56.0
npm install zod@^3.22.4
npm install react-hook-form@^7.49.0
npm install @react-native-community/netinfo@^11.3.0
npm install react-native-chart-kit@^6.12.0
npm install react-native-svg@^14.1.0
npm install react-native-markdown-display@^7.0.0
npm install react-native-fast-image@^8.6.3
npm install react-native-toast-message@^2.2.0
```

#### Step 2: Expo Modules

```bash
# Install Expo modules
npx expo install expo-av
npx expo install expo-notifications
npx expo install expo-local-authentication
```

#### Step 3: Dev Dependencies

```bash
# Install testing libraries
npm install --save-dev @testing-library/react-native@^12.4.0
npm install --save-dev @testing-library/jest-native@^5.4.3
npm install --save-dev jest@^29.7.0
npm install --save-dev @types/jest@^29.5.11
```

#### Step 4: Verify Installation

```bash
# Check package.json
cat package.json

# Run app to verify no errors
npm start
```

### Acceptance Criteria

- [ ] All packages installed successfully
- [ ] `package.json` contains all dependencies
- [ ] `package-lock.json` updated
- [ ] No version conflicts
- [ ] App compiles without errors
- [ ] `npm start` runs successfully

### Verification Script

```bash
# Run this to verify all packages are installed
node -e "const pkg = require('./package.json'); const deps = ['@tanstack/react-query', 'zod', 'react-hook-form', '@react-native-community/netinfo', 'react-native-chart-kit', 'react-native-svg', 'react-native-markdown-display', 'react-native-fast-image', 'expo-av', 'expo-notifications', 'expo-local-authentication']; deps.forEach(d => console.log(d, pkg.dependencies[d] || 'MISSING'));"
```

---

## A7: Configure Test Environment (1 pt)

### Objective

Setup Jest + React Native Testing Library with proper configuration for Sprint 4 testing

### Step 1: Create `jest.config.js`

```javascript
// e:\final-project\lexia-mobile\jest.config.js
module.exports = {
  preset: "jest-expo",
  setupFilesAfterEnv: [
    "@testing-library/jest-native/extend-expect",
    "<rootDir>/jest.setup.js",
  ],
  transformIgnorePatterns: [
    "node_modules/(?!((jest-)?react-native|@react-native(-community)?)|expo(nent)?|@expo(nent)?/.*|@expo-google-fonts/.*|react-navigation|@react-navigation/.*|@unimodules/.*|unimodules|sentry-expo|native-base|react-native-svg)",
  ],
  collectCoverageFrom: [
    "app/**/*.{ts,tsx}",
    "components/**/*.{ts,tsx}",
    "services/**/*.{ts,tsx}",
    "store/**/*.{ts,tsx}",
    "hooks/**/*.{ts,tsx}",
    "!**/*.d.ts",
    "!**/node_modules/**",
    "!**/coverage/**",
  ],
  coverageThresholds: {
    global: {
      branches: 60,
      functions: 60,
      lines: 60,
      statements: 60,
    },
    "./services/**/*.{ts,tsx}": {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80,
    },
    "./store/**/*.{ts,tsx}": {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80,
    },
  },
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/$1",
  },
};
```

### Step 2: Create `jest.setup.js`

```javascript
// e:\final-project\lexia-mobile\jest.setup.js
import "@testing-library/jest-native/extend-expect";

// Mock AsyncStorage
jest.mock("@react-native-async-storage/async-storage", () =>
  require("@react-native-async-storage/async-storage/jest/async-storage-mock")
);

// Mock React Native modules
jest.mock("react-native/Libraries/Animated/NativeAnimatedHelper");

// Mock Expo modules
jest.mock("expo-av", () => ({
  Audio: {
    Sound: jest.fn(),
  },
}));

jest.mock("expo-notifications", () => ({
  requestPermissionsAsync: jest.fn(),
  getPermissionsAsync: jest.fn(),
}));

jest.mock("expo-local-authentication", () => ({
  authenticateAsync: jest.fn(),
  hasHardwareAsync: jest.fn(),
}));

// Mock NetInfo
jest.mock("@react-native-community/netinfo", () => ({
  fetch: jest.fn(() => Promise.resolve({ isConnected: true })),
  addEventListener: jest.fn(),
}));

// Silence console errors in tests
global.console = {
  ...console,
  error: jest.fn(),
  warn: jest.fn(),
};
```

### Step 3: Update `package.json` Scripts

```json
{
  "scripts": {
    "start": "expo start",
    "android": "expo start --android",
    "ios": "expo start --ios",
    "web": "expo start --web",
    "test": "jest --passWithNoTests",
    "test:watch": "jest --watch",
    "test:coverage": "jest --coverage",
    "test:ci": "jest --ci --coverage --maxWorkers=2"
  }
}
```

### Step 4: Create Sample Test

```typescript
// e:\final-project\lexia-mobile\__tests__\App.test.tsx
import React from "react";
import { render } from "@testing-library/react-native";
import App from "../App";

describe("App", () => {
  it("renders without crashing", () => {
    const { getByText } = render(<App />);
    expect(getByText).toBeDefined();
  });
});
```

### Step 5: Create Test Utils

```typescript
// e:\final-project\lexia-mobile\tests\utils\testUtils.tsx
import React, { ReactElement } from "react";
import { render, RenderOptions } from "@testing-library/react-native";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { PaperProvider } from "react-native-paper";

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
      mutations: {
        retry: false,
      },
    },
  });

interface AllTheProvidersProps {
  children: React.ReactNode;
}

const AllTheProviders = ({ children }: AllTheProvidersProps) => {
  const queryClient = createTestQueryClient();

  return (
    <QueryClientProvider client={queryClient}>
      <PaperProvider>{children}</PaperProvider>
    </QueryClientProvider>
  );
};

const customRender = (
  ui: ReactElement,
  options?: Omit<RenderOptions, "wrapper">
) => render(ui, { wrapper: AllTheProviders, ...options });

export * from "@testing-library/react-native";
export { customRender as render };
```

### Acceptance Criteria

- [ ] `jest.config.js` created with proper configuration
- [ ] `jest.setup.js` created with all mocks
- [ ] Coverage thresholds defined (60% global, 80% services)
- [ ] Test scripts added to `package.json`
- [ ] Sample test passes: `npm test`
- [ ] Test utils created with QueryClientProvider wrapper

---

## A8: Setup ESLint Rules + Prettier (0.5 pt)

### Objective

Configure ESLint and Prettier to match web app standards

### Step 1: Install ESLint + Prettier

```bash
npm install --save-dev eslint@^8.56.0
npm install --save-dev prettier@^3.2.4
npm install --save-dev eslint-config-prettier@^9.1.0
npm install --save-dev eslint-plugin-react@^7.33.2
npm install --save-dev eslint-plugin-react-hooks@^4.6.0
npm install --save-dev @typescript-eslint/eslint-plugin@^6.19.0
npm install --save-dev @typescript-eslint/parser@^6.19.0
```

### Step 2: Create `.eslintrc.js`

```javascript
// e:\final-project\lexia-mobile\.eslintrc.js
module.exports = {
  root: true,
  extends: [
    "expo",
    "eslint:recommended",
    "plugin:react/recommended",
    "plugin:react-hooks/recommended",
    "plugin:@typescript-eslint/recommended",
    "prettier",
  ],
  parser: "@typescript-eslint/parser",
  plugins: ["react", "react-hooks", "@typescript-eslint"],
  rules: {
    "react/react-in-jsx-scope": "off",
    "react/prop-types": "off",
    "@typescript-eslint/no-unused-vars": ["error", { argsIgnorePattern: "^_" }],
    "@typescript-eslint/explicit-module-boundary-types": "off",
    "@typescript-eslint/no-explicit-any": "error",
    "no-console": ["warn", { allow: ["warn", "error"] }],
  },
  settings: {
    react: {
      version: "detect",
    },
  },
};
```

### Step 3: Create `.prettierrc.js`

```javascript
// e:\final-project\lexia-mobile\.prettierrc.js
module.exports = {
  semi: true,
  trailingComma: "es5",
  singleQuote: true,
  printWidth: 80,
  tabWidth: 2,
  arrowParens: "always",
  endOfLine: "lf",
};
```

### Step 4: Create `.prettierignore`

```
# e:\final-project\lexia-mobile\.prettierignore
node_modules/
coverage/
.expo/
dist/
build/
*.log
.git/
```

### Step 5: Update `package.json` Scripts

```json
{
  "scripts": {
    "lint": "eslint . --ext .ts,.tsx --max-warnings 0",
    "lint:fix": "eslint . --ext .ts,.tsx --fix",
    "format": "prettier --write \"**/*.{ts,tsx,json,md}\"",
    "format:check": "prettier --check \"**/*.{ts,tsx,json,md}\""
  }
}
```

### Step 6: Create `.vscode/settings.json`

```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "typescript.tsdk": "node_modules/typescript/lib"
}
```

### Acceptance Criteria

- [ ] ESLint and Prettier installed
- [ ] `.eslintrc.js` created with rules matching web app
- [ ] `.prettierrc.js` created
- [ ] Lint scripts added to `package.json`
- [ ] `npm run lint` passes with 0 warnings
- [ ] `npm run format` formats all files
- [ ] VSCode auto-formats on save

---

## Summary

**Total Points**: 2.5 pts  
**Estimated Time**: 4-6 hours  
**Dependencies**: None (must be completed first)

**Order of Execution**:

1. A6 → Install dependencies (1 hour)
2. A7 → Configure tests (2 hours)
3. A8 → Setup linting (1 hour)

**Verification Checklist**:

- [ ] All dependencies installed
- [ ] Tests run successfully
- [ ] Linting passes with 0 warnings
- [ ] App compiles and runs
- [ ] No console errors

**Next Steps After Completion**:

- Proceed to Epic B (Authentication)
- Document any issues in daily-log.md
- Commit changes with message: `feat(setup): complete project initialization (A6-A8)`
