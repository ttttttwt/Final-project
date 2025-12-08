# 🌙 Lesson Detail Dark Mode Fix - Implementation Guide

## ✅ Đã Hoàn Thành

### 1. **Navigation FABs** (Floating Action Buttons)
✅ Đã thay thế navigation footer bằng FAB buttons:
- **Left FAB**: Previous lesson (chỉ hiện khi có previous)
- **Right FAB**: Next lesson hoặc Back to Course
- **Ưu điểm**: 
  - Không chiếm không gian footer
  - Luôn hiển thị trên màn hình khi scroll
  - Professional hơn (giống YouTube, Medium)
  - Dễ chạm (56x56px)

### 2. **Dark Mode Issues** trong ReadingLesson

## ❌ Vấn Đề Còn Tồn Tại

File `ReadingLesson.tsx` đang dùng **hardcoded colors** thay vì theme colors:

```tsx
// ❌ BEFORE - Hardcoded
backgroundColor: "#fff"
color: "#333"
borderColor: "#ddd"
```

Cần update thành:

```tsx
// ✅ AFTER - Theme-aware
backgroundColor: theme.colors.surface
color: theme.colors.text
borderColor: theme.colors.outline
```

## 🔧 Cách Fix Nhanh

### Option A: Update toàn bộ ReadingLesson.tsx

Do file này dùng `react-native-paper` với cấu trúc khác, cần:

1. **Import useTheme từ @/lib/theme**:
```tsx
import { useTheme as useAppTheme } from "@/lib/theme";
```

2. **Sử dụng theme colors**:
```tsx
const { colors, isDark } = useAppTheme();

// Apply to styles
<View style={[styles.container, { backgroundColor: colors.background }]}>
<Card style={[styles.card, { backgroundColor: colors.surface }]}>
<Text style={[styles.text, { color: colors.text.primary }]}>
```

3. **Dynamic colors cho correct/incorrect**:
```tsx
const correctBg = isDark ? "#1B5E20" : "#E8F5E9"; // Dark green : Light green
const incorrectBg = isDark ? "#B71C1C" : "#FFEBEE"; // Dark red : Light red
```

### Option B: Chuyển sang Custom Components

Thay `react-native-paper` bằng custom components đã có dark mode:
- `Card` → `@/components/ui/Card`
- `Text` → `@/components/ui/Text`
- `Button` → `@/components/ui/Button`

## 📝 Files Cần Sửa

1. ✅ `app/lesson/[id].tsx` - **DONE** (FABs implemented)
2. ❌ `components/lessons/ReadingLesson.tsx` - **TODO**
3. ❌ `components/lessons/ListeningLesson.tsx` - **TODO**
4. ❌ `components/lessons/QuizLesson.tsx` - **TODO**
5. ❌ `components/lessons/SpeakingLesson.tsx` - **TODO**

## 🎯 Quick Win Solution

Sử dụng **Custom Hook** để tạo dynamic styles:

```tsx
// hooks/useLessonStyles.ts
import { useTheme } from "@/lib/theme";
import { useMemo } from "react";

export function useLessonStyles() {
  const { colors, isDark } = useTheme();

  return useMemo(() => ({
    container: { backgroundColor: colors.background },
    card: { backgroundColor: colors.surface },
    text: { color: colors.text.primary },
    secondaryText: { color: colors.text.secondary },
    border: { borderColor: colors.border },
    correctOption: {
      backgroundColor: isDark ? "#1B5E20" : "#E8F5E9",
      borderColor: "#4CAF50",
    },
    incorrectOption: {
      backgroundColor: isDark ? "#B71C1C" : "#FFEBEE",
      borderColor: "#F44336",
    },
    explanation: {
      backgroundColor: isDark ? "#3E2723" : "#FFF8E1",
      borderLeftColor: colors.primary,
    },
  }), [colors, isDark]);
}
```

Sau đó sử dụng trong component:

```tsx
const lessonStyles = useLessonStyles();

<View style={[styles.container, lessonStyles.container]}>
<Card style={[styles.card, lessonStyles.card]}>
```

## 🚀 Recommended Approach

**Tạo PR riêng cho Dark Mode Fix:**

1. Create hook: `useLessonStyles.ts`
2. Update ReadingLesson first (test thoroughly)
3. Apply same pattern to other lesson types
4. Test trên cả Light và Dark mode
5. Verify accessibility (contrast ratios)

## ✨ Expected Result

- ✅ Dark mode hoạt động đúng cho tất cả lesson types
- ✅ Colors tự động switch khi toggle theme
- ✅ Contrast đủ cho WCAG AA
- ✅ FAB navigation smooth và professional
- ✅ Không còn flash/flicker khi chuyển theme

---

**Status**: 
- Navigation: ✅ Complete
- Dark Mode: ⏳ In Progress (needs ReadingLesson fix)

**ETA**: ~2-3 hours để fix toàn bộ lesson components
