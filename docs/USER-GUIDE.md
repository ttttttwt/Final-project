# LEXIA API - Hướng Dẫn Sử Dụng cho Người Dùng

## 📚 Giới Thiệu

LEXIA là nền tảng học tiếng Anh với AI. API này cung cấp các chức năng đăng ký, đăng nhập và quản lý phiên làm việc.

**Base URL**: `http://localhost:8080/api/v1`

---

## 🚀 Bắt Đầu Nhanh

### Bước 1: Đăng Ký Tài Khoản

**Endpoint**: `POST /auth/register`

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nguyen.van.a@example.com",
    "password": "MatKhau123!",
    "confirmPassword": "MatKhau123!",
    "fullName": "Nguyễn Văn A"
  }'
```

**Yêu cầu**:

- ✅ Email hợp lệ (vd: `user@example.com`)
- ✅ Mật khẩu 8-255 ký tự
- ✅ Mật khẩu xác nhận phải khớp
- ✅ Họ tên không được để trống

**Phản hồi thành công** (201 Created):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "nguyen.van.a@example.com",
  "isActive": true,
  "authProvider": "EMAIL",
  "createdAt": "2025-10-21T12:00:00Z",
  "profile": {
    "fullName": "Nguyễn Văn A"
  }
}
```

**Lỗi thường gặp**:

❌ **Email đã tồn tại** (409 Conflict):

```json
{
  "timestamp": "2025-10-21T12:00:00Z",
  "status": 409,
  "error": "User Already Exists",
  "message": "Email already registered",
  "path": "/api/v1/auth/register"
}
```

❌ **Mật khẩu không khớp** (400 Bad Request):

```json
{
  "timestamp": "2025-10-21T12:00:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed. Please check the validation errors.",
  "path": "/api/v1/auth/register",
  "validationErrors": [
    {
      "field": "confirmPassword",
      "message": "Passwords do not match"
    }
  ]
}
```

---

### Bước 2: Đăng Nhập

**Endpoint**: `POST /auth/login`

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nguyen.van.a@example.com",
    "password": "MatKhau123!"
  }'
```

**Phản hồi thành công** (200 OK):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpYXQiOjE3Mjk0OTY0MDAsImV4cCI6MTcyOTQ5NzMwMH0.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpYXQiOjE3Mjk0OTY0MDAsImV4cCI6MTczMDEwMTIwMH0.signature",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "nguyen.van.a@example.com",
    "fullName": "Nguyễn Văn A",
    "isActive": true
  }
}
```

**Thông tin quan trọng**:

- 🔑 **accessToken**: Dùng để truy cập các API bảo mật (có hiệu lực 15 phút)
- 🔄 **refreshToken**: Dùng để lấy accessToken mới khi hết hạn (có hiệu lực 7 ngày)
- ⏰ **expiresIn**: Thời gian sống của accessToken (miliseconds)

**Lỗi thường gặp**:

❌ **Sai email hoặc mật khẩu** (401 Unauthorized):

```json
{
  "timestamp": "2025-10-21T12:00:00Z",
  "status": 401,
  "error": "Authentication Failed",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login"
}
```

---

### Bước 3: Sử Dụng Access Token

Sau khi đăng nhập, bạn nhận được `accessToken`. Sử dụng token này để truy cập các API bảo mật:

```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Lưu ý**:

- Token có hiệu lực **15 phút**
- Phải thêm header: `Authorization: Bearer <accessToken>`

---

### Bước 4: Làm Mới Token (Refresh Token)

Khi `accessToken` hết hạn (sau 15 phút), sử dụng `refreshToken` để lấy token mới:

**Endpoint**: `POST /auth/refresh`

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

**Phản hồi thành công** (200 OK):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.NEW_TOKEN...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.NEW_REFRESH_TOKEN...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

**Lưu ý quan trọng**:

- ⚠️ Refresh token cũ sẽ **TỰ ĐỘNG BỊ VÔ HIỆU HÓA**
- ✅ Bạn nhận được cặp token mới (access + refresh)
- 🔄 Luôn lưu refresh token mới để sử dụng lần sau

**Lỗi thường gặp**:

❌ **Token không hợp lệ hoặc hết hạn** (401 Unauthorized):

```json
{
  "timestamp": "2025-10-21T12:00:00Z",
  "status": 401,
  "error": "Invalid Token",
  "message": "Refresh token is invalid or expired",
  "path": "/api/v1/auth/refresh"
}
```

---

### Bước 5: Đăng Xuất (Logout)

**Endpoint**: `POST /auth/logout`

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Phản hồi thành công** (200 OK):

```json
{
  "message": "Logged out successfully"
}
```

**Lưu ý**:

- ⚠️ Refresh token của bạn sẽ bị vô hiệu hóa
- ✅ Bạn cần đăng nhập lại để nhận token mới
- 🔒 Tính năng này giúp bảo vệ tài khoản khi sử dụng thiết bị công cộng

---

## 📋 Quản Lý Thông Tin Cá Nhân

Sau khi đăng nhập thành công, bạn có thể quản lý thông tin cá nhân của mình.

### Xem Thông Tin Cá Nhân

**Endpoint**: `GET /users/profile`

```bash
curl -X GET http://localhost:8080/api/v1/users/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Phản hồi thành công** (200 OK):

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "nguyen.van.a@example.com",
  "firstName": "Văn A",
  "lastName": "Nguyễn",
  "bio": "Tôi là một lập trình viên đang học tiếng Anh",
  "phoneNumber": "+84901234567",
  "avatarUrl": "https://example.com/avatar.jpg",
  "timezone": "Asia/Ho_Chi_Minh",
  "language": "vi",
  "currentLevel": "INTERMEDIATE",
  "learningGoal": "Business English for work",
  "createdAt": "2025-10-21T12:00:00Z",
  "updatedAt": "2025-10-21T15:30:00Z"
}
```

**Thông tin các trường**:

- 📧 **email**: Email đăng nhập (không thể thay đổi)
- 👤 **firstName, lastName**: Họ và tên
- 📝 **bio**: Giới thiệu bản thân (tối đa 500 ký tự)
- 📱 **phoneNumber**: Số điện thoại (tùy chọn)
- 🖼️ **avatarUrl**: Đường dẫn ảnh đại diện
- 🌍 **timezone**: Múi giờ (IANA timezone, vd: Asia/Ho_Chi_Minh)
- 🗣️ **language**: Ngôn ngữ giao diện (ISO 639-1, vd: vi, en)
- 📊 **currentLevel**: Trình độ hiện tại
- 🎯 **learningGoal**: Mục tiêu học tập

**Lỗi thường gặp**:

❌ **Chưa đăng nhập** (401 Unauthorized):

```json
{
  "timestamp": "2025-10-21T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "path": "/api/v1/users/profile"
}
```

---

### Cập Nhật Thông Tin Cá Nhân

**Endpoint**: `PUT /users/profile`

```bash
curl -X PUT http://localhost:8080/api/v1/auth/users/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Văn B",
    "lastName": "Nguyễn",
    "bio": "Tôi là một kỹ sư phần mềm học tiếng Anh để thăng tiến",
    "phoneNumber": "+84912345678",
    "timezone": "Asia/Ho_Chi_Minh",
    "language": "vi"
  }'
```

**Yêu cầu**:

- ✅ **firstName**: Tên (bắt buộc, tối đa 100 ký tự)
- ✅ **lastName**: Họ (bắt buộc, tối đa 100 ký tự)
- ⭕ **bio**: Giới thiệu (tùy chọn, tối đa 500 ký tự)
- ⭕ **phoneNumber**: Số điện thoại (tùy chọn, định dạng: +84xxxxxxxxx hoặc 10-20 chữ số)
- ✅ **timezone**: Múi giờ (bắt buộc, IANA timezone, vd: Asia/Ho_Chi_Minh, America/New_York)
- ✅ **language**: Mã ngôn ngữ (bắt buộc, ISO 639-1, vd: vi, en, ja, ko)

**Phản hồi thành công** (200 OK):

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "nguyen.van.a@example.com",
  "firstName": "Văn B",
  "lastName": "Nguyễn",
  "bio": "Tôi là một kỹ sư phần mềm học tiếng Anh để thăng tiến",
  "phoneNumber": "+84912345678",
  "avatarUrl": "https://example.com/avatar.jpg",
  "timezone": "Asia/Ho_Chi_Minh",
  "language": "vi",
  "currentLevel": "INTERMEDIATE",
  "learningGoal": "Business English for work",
  "createdAt": "2025-10-21T12:00:00Z",
  "updatedAt": "2025-10-21T16:45:00Z"
}
```

**Lỗi thường gặp**:

❌ **Thiếu trường bắt buộc** (400 Bad Request):

```json
{
  "timestamp": "2025-10-21T12:00:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed. Please check the validation errors.",
  "path": "/api/v1/users/profile",
  "validationErrors": [
    {
      "field": "firstName",
      "message": "First name is required"
    },
    {
      "field": "timezone",
      "message": "Timezone must be a valid IANA timezone (e.g., Asia/Ho_Chi_Minh)"
    }
  ]
}
```

❌ **Định dạng số điện thoại không hợp lệ** (400 Bad Request):

```json
{
  "timestamp": "2025-10-21T12:00:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed. Please check the validation errors.",
  "path": "/api/v1/users/profile",
  "validationErrors": [
    {
      "field": "phoneNumber",
      "message": "Phone number must be 10-20 digits with optional + prefix"
    }
  ]
}
```

❌ **Bio quá dài** (400 Bad Request):

```json
{
  "timestamp": "2025-10-21T12:00:00Z",
  "status": 400,
  "error": "Invalid Input",
  "message": "Bio must not exceed 500 characters",
  "path": "/api/v1/users/profile"
}
```

❌ **Múi giờ không hợp lệ** (400 Bad Request):

```json
{
  "timestamp": "2025-10-21T12:00:00Z",
  "status": 400,
  "error": "Invalid Input",
  "message": "Timezone must be a valid IANA timezone (e.g., Asia/Ho_Chi_Minh, America/New_York, UTC)",
  "path": "/api/v1/users/profile"
}
```

**Danh sách múi giờ phổ biến**:

- 🇻🇳 Vietnam: `Asia/Ho_Chi_Minh`
- 🇺🇸 US East: `America/New_York`
- 🇺🇸 US West: `America/Los_Angeles`
- 🇬🇧 UK: `Europe/London`
- 🇯🇵 Japan: `Asia/Tokyo`
- 🇰🇷 Korea: `Asia/Seoul`
- 🌍 UTC: `UTC`

**Danh sách ngôn ngữ hỗ trợ**:

- 🇻🇳 Tiếng Việt: `vi`
- 🇺🇸 English: `en`
- 🇯🇵 日本語: `ja`
- 🇰🇷 한국어: `ko`
- 🇨🇳 中文: `zh`
- 🇫🇷 Français: `fr`
- 🇩🇪 Deutsch: `de`
- 🇪🇸 Español: `es`

---

### Cập Nhật Ảnh Đại Diện

**Endpoint**: `POST /users/profile/avatar`

```bash
curl -X POST http://localhost:8080/api/v1/users/profile/avatar \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "avatarUrl": "https://example.com/my-new-avatar.jpg"
  }'
```

**Yêu cầu**:

- ✅ **avatarUrl**: URL ảnh đại diện (bắt buộc, phải là HTTP/HTTPS)

**Phản hồi thành công** (200 OK):

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "nguyen.van.a@example.com",
  "firstName": "Văn B",
  "lastName": "Nguyễn",
  "avatarUrl": "https://example.com/my-new-avatar.jpg",
  "updatedAt": "2025-10-21T17:00:00Z"
}
```

**Lưu ý**:

- 📌 Hiện tại API chỉ hỗ trợ URL ảnh (HTTP/HTTPS)
- 🔜 Tính năng upload file trực tiếp sẽ được bổ sung trong phiên bản sau
- ✅ Bạn có thể sử dụng dịch vụ lưu trữ ảnh như Cloudinary, AWS S3

**Lỗi thường gặp**:

❌ **URL không hợp lệ** (400 Bad Request):

```json
{
  "timestamp": "2025-10-21T12:00:00Z",
  "status": 400,
  "error": "Invalid Input",
  "message": "Avatar URL must be a valid HTTP or HTTPS URL",
  "path": "/api/v1/users/profile/avatar"
}
```

---

### Xóa Ảnh Đại Diện

**Endpoint**: `DELETE /users/profile/avatar`

```bash
curl -X DELETE http://localhost:8080/api/v1/users/profile/avatar \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Phản hồi thành công** (200 OK):

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "nguyen.van.a@example.com",
  "firstName": "Văn B",
  "lastName": "Nguyễn",
  "avatarUrl": null,
  "updatedAt": "2025-10-21T17:15:00Z"
}
```

**Lưu ý**:

- ✅ Ảnh đại diện sẽ được đặt về `null`
- 🖼️ Hệ thống sẽ hiển thị ảnh mặc định
- 🔄 Bạn có thể upload ảnh mới bất cứ lúc nào

---

## � Quản Lý Khóa Học (Sprint 2)

Sprint 2 bổ sung đầy đủ API quản lý khóa học cho cả người học và đội ngũ soạn nội dung. Mọi yêu cầu phải kèm header `Authorization: Bearer <accessToken>` hợp lệ.

### Xem danh sách khóa học đã xuất bản

**Endpoint**: `GET /api/v1/courses`

```bash
curl -X GET "http://localhost:8080/api/v1/courses?page=0&size=10&sort=createdAt,desc" \
  -H "Authorization: Bearer <accessToken>"
```

**Phản hồi mẫu** (200 OK):

```json
{
  "content": [
    {
      "id": 1,
      "title": "English Basics (A1)",
      "description": "Foundation course for beginners",
      "thumbnailUrl": "https://cdn.lexia.com/courses/a1-basics.jpg",
      "cefrLevel": "A1",
      "isPublished": true,
      "sectionCount": 3,
      "createdAt": "2025-10-30T10:15:30",
      "updatedAt": "2025-10-31T14:22:45"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "first": true
}
```

**Ghi chú**:

- `size` tối đa 100.
- `sort` nhận định dạng `field,asc|desc` (ví dụ `title,asc`).

### Tìm kiếm khóa học nâng cao

**Endpoint**: `GET /api/v1/courses/search`

```bash
curl -X GET "http://localhost:8080/api/v1/courses/search?title=english&cefrLevel=B1&isPublished=true&page=0&size=5&sort=title,asc" \
  -H "Authorization: Bearer <accessToken>"
```

**Tham số hỗ trợ**:

- `title`: tìm kiếm theo tiêu đề (không phân biệt hoa/thường, hỗ trợ chứa).
- `cefrLevel`: lọc theo trình độ (`A1` → `C2`).
- `isPublished`: `true/false`.
- `page`, `size`, `sort`: giống endpoint danh sách.

Phản hồi có cấu trúc phân trang giống `GET /courses`.

### Xem chi tiết khóa học

**Endpoint**: `GET /api/v1/courses/{id}`

```bash
curl -X GET http://localhost:8080/api/v1/courses/1 \
  -H "Authorization: Bearer <accessToken>"
```

**Phản hồi mẫu** (200 OK):

```json
{
  "id": 1,
  "title": "English Basics (A1)",
  "description": "Foundation course for beginners starting to learn English",
  "thumbnailUrl": "https://cdn.lexia.com/courses/a1-basics.jpg",
  "cefrLevel": "A1",
  "isPublished": true,
  "sectionCount": 3,
  "createdAt": "2025-10-30T10:15:30",
  "updatedAt": "2025-10-31T14:22:45"
}
```

### Tạo & quản lý khóa học (⚠️ ROLE_CONTENT_MANAGER)

Các endpoint bên dưới yêu cầu quyền `CONTENT_MANAGER`.

**Tạo mới** — `POST /api/v1/courses`

```bash
curl -X POST http://localhost:8080/api/v1/courses \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Business English for Professionals",
    "description": "Master business English for office communication",
    "thumbnailUrl": "https://cdn.lexia.com/courses/business-english.jpg",
    "cefrLevel": "B1"
  }'
```

**Phản hồi mẫu** (201 Created):

```json
{
  "id": 5,
  "title": "Business English for Professionals",
  "description": "Master business English for office communication",
  "thumbnailUrl": "https://cdn.lexia.com/courses/business-english.jpg",
  "cefrLevel": "B1",
  "isPublished": false,
  "sectionCount": 0,
  "createdAt": "2025-11-01T09:00:00",
  "updatedAt": "2025-11-01T09:00:00"
}
```

**Cập nhật** — `PUT /api/v1/courses/{id}` (chỉ cập nhật các trường gửi lên; tiêu đề phải duy nhất).

```bash
curl -X PUT http://localhost:8080/api/v1/courses/5 \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated syllabus with live workshops",
    "cefrLevel": "B2"
  }'
```

**Xuất bản / gỡ khỏi trang**:

```bash
curl -X POST http://localhost:8080/api/v1/courses/5/publish \
  -H "Authorization: Bearer <accessToken>"

curl -X POST http://localhost:8080/api/v1/courses/5/unpublish \
  -H "Authorization: Bearer <accessToken>"
```

**Xóa khóa học** — `DELETE /api/v1/courses/{id}` (chỉ thực hiện được khi khóa học đã ở trạng thái `isPublished = false`).

**Lỗi thường gặp**:

- 400: khóa học chưa có nội dung nên không thể publish hoặc dữ liệu đầu vào sai định dạng.
- 403: người dùng không có quyền `CONTENT_MANAGER`.
- 404: không tìm thấy khóa học theo ID.
- 409: tiêu đề bị trùng.

---

## 🧩 Quản Lý Bài Học (Lessons)

Người học có thể đọc dữ liệu bài học; quyền chỉnh sửa thuộc đội `CONTENT_MANAGER`. Tất cả endpoint yêu cầu JWT hợp lệ.

### Xem thông tin bài học

**Endpoint**: `GET /api/v1/lessons/{id}`

```bash
curl -X GET http://localhost:8080/api/v1/lessons/10 \
  -H "Authorization: Bearer <accessToken>"
```

**Phản hồi mẫu** (200 OK):

```json
{
  "id": 10,
  "sectionId": 3,
  "title": "Basic Greetings and Introductions",
  "lessonType": "READING",
  "content": "{\"passages\":[{\"title\":\"Meeting People\",\"text\":\"When you meet someone new...\"}],\"questions\":[{\"question\":\"What should you say first?\",\"type\":\"multiple_choice\",\"options\":[\"Hello\",\"Goodbye\",\"Thank you\"],\"correctAnswer\":0}]}",
  "orderIndex": 0,
  "durationMinutes": 15,
  "createdAt": "2025-10-31T08:00:00",
  "updatedAt": "2025-10-31T08:30:00"
}
```

**Ghi chú**: Trường `content` là chuỗi JSON, cấu trúc phụ thuộc `lessonType` (tham khảo `DATABASE-SCHEMA.md` mục 2.3).

### Danh sách bài học theo section hoặc khóa học

- `GET /api/v1/lessons/sections/{sectionId}` – trả về danh sách bài học theo thứ tự `orderIndex`.
- `GET /api/v1/lessons/courses/{courseId}` – tổng hợp bài học của cả khóa học.

```bash
curl -X GET http://localhost:8080/api/v1/lessons/sections/3 \
  -H "Authorization: Bearer <accessToken>"
```

Phản hồi: mảng `LessonDTO` đã sắp xếp theo thứ tự hiển thị.

### Tạo bài học mới (⚠️ ROLE_CONTENT_MANAGER)

```bash
curl -X POST http://localhost:8080/api/v1/lessons/sections/3/lessons \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Basic Greetings and Introductions",
    "lessonType": "READING",
    "content": "{\"passages\":[{\"title\":\"Meeting People\",\"text\":\"When you meet someone new...\"}],\"questions\":[{\"question\":\"What should you say first?\",\"type\":\"multiple_choice\",\"options\":[\"Hello\",\"Goodbye\",\"Thank you\"],\"correctAnswer\":0}]}",
    "orderIndex": 0,
    "durationMinutes": 15
  }'
```

**Phản hồi mẫu** (201 Created): giống dữ liệu ở endpoint GET.

**Ghi chú**:

- `content` phải là chuỗi JSON hợp lệ; hệ thống sẽ validate theo từng `lessonType`.
- Nếu JSON không đúng schema, API trả về 400 cùng thông báo chi tiết.

### Cập nhật, xóa & sắp xếp lại bài học (⚠️ ROLE_CONTENT_MANAGER)

- `PUT /api/v1/lessons/{id}` – cập nhật tiêu đề, nội dung, thời lượng,...
- `DELETE /api/v1/lessons/{id}` – xóa bài học (trả về 204 No Content).
- `PATCH /api/v1/lessons/{id}/reorder?newOrderIndex=2` – đổi vị trí trong section.

**Lỗi thường gặp**: 400 (dữ liệu không hợp lệ), 403 (thiếu quyền), 404 (không tìm thấy bài học).

---

## 🛣️ Lộ Trình Học Tập (Learning Paths)

Sprint 2 giới thiệu lộ trình học chuẩn CEFR. Người dùng phải đăng nhập để sử dụng các endpoint sau.

### Xem tất cả lộ trình

**Endpoint**: `GET /api/v1/learning-paths`

```bash
curl -X GET http://localhost:8080/api/v1/learning-paths \
  -H "Authorization: Bearer <accessToken>"
```

**Phản hồi mẫu** (200 OK):

```json
[
  {
    "id": 1,
    "name": "Beginner Path (A1)",
    "description": "A comprehensive path for complete beginners starting their English journey",
    "cefrLevel": "A1",
    "isDefault": true,
    "courses": [
      {
        "courseId": 1,
        "courseTitle": "English Basics (A1)",
        "orderIndex": 0,
        "sectionCount": 3
      }
    ],
    "totalCourses": 1,
    "estimatedHours": 15,
    "createdAt": "2025-11-03T10:00:00",
    "updatedAt": "2025-11-03T10:00:00"
  }
]
```

### Xem chi tiết một lộ trình

**Endpoint**: `GET /api/v1/learning-paths/{id}` – trả về danh sách khóa học thuộc lộ trình cùng thứ tự học.

### Nhận lộ trình được đề xuất

**Endpoint**: `GET /api/v1/learning-paths/recommend`

- Dựa trên `currentLevel` trong hồ sơ người dùng.
- Nếu chưa thiết lập, hệ thống mặc định trả về lộ trình A1.

### Bắt đầu một lộ trình

**Endpoint**: `POST /api/v1/learning-paths/{id}/start`

```bash
curl -X POST http://localhost:8080/api/v1/learning-paths/1/start \
  -H "Authorization: Bearer <accessToken>"
```

**Phản hồi mẫu** (201 Created):

```json
{
  "enrollmentId": 12,
  "pathId": 1,
  "pathName": "Beginner Path (A1)",
  "pathCefrLevel": "A1",
  "currentCourseId": 1,
  "currentCourseTitle": "English Basics (A1)",
  "coursesCompleted": 0,
  "totalCourses": 1,
  "progressPercentage": 0,
  "startedAt": "2025-11-03T15:30:00",
  "completedAt": null,
  "isCompleted": false
}
```

### Xem tiến độ các lộ trình của tôi

**Endpoint**: `GET /api/v1/learning-paths/my-progress`

- Trả về mảng `UserPathProgressDTO` (bao gồm lộ trình đang học và đã hoàn thành).
- 409 được trả về nếu cố gắng `start` một lộ trình đã tham gia.

**Lỗi thường gặp**: 404 (không tìm thấy lộ trình hoặc lộ trình chưa được cấu hình), 409 (đăng ký trùng lặp).

---

## 🎯 Đăng Ký Khóa Học & Theo Dõi Tiến Độ

Các API này hỗ trợ người học theo dõi tiến độ chi tiết và streak học tập hằng ngày.

### Đăng ký khóa học

**Endpoint**: `POST /api/v1/enrollments?courseId={id}`

```bash
curl -X POST "http://localhost:8080/api/v1/enrollments?courseId=1" \
  -H "Authorization: Bearer <accessToken>"
```

**Phản hồi mẫu** (201 Created):

```json
{
  "id": 20,
  "courseId": 1,
  "courseTitle": "English Basics (A1)",
  "thumbnailUrl": "https://cdn.lexia.com/courses/a1-basics.jpg",
  "cefrLevel": "A1",
  "enrolledAt": "2025-11-05T10:30:00",
  "progressPercentage": 0,
  "completedAt": null,
  "isCompleted": false
}
```

### Danh sách khóa học đã đăng ký

**Endpoint**: `GET /api/v1/enrollments` – trả về danh sách `EnrollmentDTO` của người dùng hiện tại.

### Xem tiến độ chi tiết của một khóa

**Endpoint**: `GET /api/v1/enrollments/{courseId}/progress`

```bash
curl -X GET http://localhost:8080/api/v1/enrollments/1/progress \
  -H "Authorization: Bearer <accessToken>"
```

**Phản hồi mẫu** (200 OK):

```json
{
  "courseId": 1,
  "courseTitle": "English Basics (A1)",
  "cefrLevel": "A1",
  "totalLessons": 18,
  "completedLessons": 8,
  "progressPercentage": 44,
  "lessonProgress": [
    {
      "lessonId": 1,
      "lessonTitle": "Introduction to English Alphabet",
      "lessonType": "READING",
      "sectionTitle": "Getting Started",
      "status": "COMPLETED",
      "score": 100,
      "attempts": 1
    }
  ]
}
```

### Ghi nhận hoàn thành bài học

**Endpoint**: `POST /api/v1/progress/lessons/{lessonId}/complete`

```bash
curl -X POST http://localhost:8080/api/v1/progress/lessons/1/complete \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "resultDetailsJson": "{\"score\":95,\"correctAnswers\":19,\"totalQuestions\":20,\"timeSpent\":780}"
  }'
```

**Phản hồi mẫu** (201 Created):

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "lessonId": 1,
  "lessonTitle": "Introduction to English Alphabet",
  "lessonType": "READING",
  "status": "COMPLETED",
  "score": 95,
  "attempts": 1,
  "startedAt": "2025-11-05T10:30:00",
  "completedAt": "2025-11-05T10:43:00",
  "resultDetails": {
    "score": 95,
    "correctAnswers": 19,
    "totalQuestions": 20,
    "timeSpent": 780
  },
  "updatedAt": "2025-11-05T10:43:00"
}
```

**Ghi chú**:

- `resultDetailsJson` bắt buộc là chuỗi JSON hợp lệ (dùng `{}` nếu không có dữ liệu bổ sung).
- API tự động đếm số lần thử, cập nhật điểm và đồng bộ tiến độ khóa học.

### Xem streak học tập

**Endpoint**: `GET /api/v1/progress/streak`

```bash
curl -X GET http://localhost:8080/api/v1/progress/streak \
  -H "Authorization: Bearer <accessToken>"
```

**Phản hồi mẫu** (200 OK):

```json
{
  "currentStreak": 7,
  "longestStreak": 15,
  "lastActivityDate": "2025-11-05",
  "isActiveToday": true,
  "totalActiveDays": 42
}
```

**Lỗi thường gặp**:

- 400: dữ liệu không hợp lệ (ví dụ courseId âm, JSON sai định dạng).
- 401: chưa đăng nhập hoặc token hết hạn.
- 404: không tìm thấy khóa học/bài học hoặc chưa đăng ký.
- 409: hành động trùng lặp (đã đăng ký khóa học hoặc đã bắt đầu lộ trình).

---

## �🔐 Bảo Mật & Quyền Riêng Tư

### Audit Logging (Nhật Ký Hoạt Động)

Tất cả các thay đổi thông tin cá nhân đều được ghi lại để đảm bảo bảo mật:

- ✅ **Cập nhật thông tin**: Ghi lại tất cả các trường đã thay đổi
- ✅ **Cập nhật ảnh đại diện**: Ghi lại URL ảnh mới
- ✅ **Xóa ảnh đại diện**: Ghi lại hành động xóa
- ✅ **Thông tin ghi nhận**: Người dùng, thời gian, hành động, thay đổi chi tiết

**Mục đích**:

- 🛡️ Phát hiện truy cập trái phép
- 📊 Tuân thủ quy định bảo mật dữ liệu
- 🔍 Hỗ trợ điều tra khi có sự cố

### Quyền Truy Cập

- ✅ Bạn chỉ có thể xem và chỉnh sửa thông tin của **chính mình**
- ✅ Thông tin người dùng được lấy từ JWT token (không cần truyền user ID)
- ✅ Mọi thay đổi đều yêu cầu xác thực (JWT token hợp lệ)

---

## 📖 Tài Nguyên Bổ Sung

### Swagger UI (API Documentation)

Xem tài liệu API đầy đủ và thử nghiệm trực tiếp:

```
http://localhost:8080/swagger-ui.html
```

**Tính năng Swagger UI**:

- 📚 Xem tất cả endpoints và schemas
- 🧪 Thử nghiệm API trực tiếp ("Try it out")
- 🔐 Xác thực JWT token (nút "Authorize")
- 📝 Xem request/response examples
- 📥 Export OpenAPI specification (JSON/YAML)

### OpenAPI Specification

Export API specification để import vào các công cụ khác:

- **JSON**: `http://localhost:8080/api-docs`
- **YAML**: `http://localhost:8080/api-docs.yaml`

**Sử dụng với**:

- Postman (import collection)
- Insomnia
- API testing tools

---

## ❓ Câu Hỏi Thường Gặp

### 1. Token hết hạn, tôi phải làm gì?

- Sử dụng **refresh token** để lấy token mới (xem Bước 4)
- Không cần đăng nhập lại

### 2. Tôi có thể đăng nhập trên nhiều thiết bị không?

- ✅ Có, hệ thống hỗ trợ đăng nhập đa thiết bị
- 🔐 Mỗi thiết bị có refresh token riêng
- ⚠️ Nếu phát hiện token bị đánh cắp, tất cả token của thiết bị đó sẽ bị vô hiệu hóa

### 3. Làm sao để bảo vệ token của tôi?

- 🔒 Không chia sẻ token với người khác
- 💾 Lưu token an toàn (localStorage/sessionStorage)
- 🚪 Đăng xuất khi sử dụng thiết bị công cộng
- 🔄 Refresh token định kỳ

### 4. Tôi quên mật khẩu, phải làm sao?

- 🔜 Tính năng "Quên mật khẩu" sẽ được bổ sung trong Sprint 2

### 5. Làm sao để thay đổi mật khẩu?

- 🔜 Tính năng "Đổi mật khẩu" sẽ được bổ sung trong Sprint 2

### 6. Tại sao không thể upload ảnh trực tiếp?

- 📌 Phiên bản hiện tại chỉ hỗ trợ URL ảnh
- 🔜 Tính năng upload file sẽ được bổ sung trong Sprint 2
- 💡 **Workaround**: Sử dụng dịch vụ hosting ảnh như Cloudinary, Imgur

### 7. Múi giờ và ngôn ngữ có ảnh hưởng gì?

- 🕐 **Timezone**: Hiển thị thời gian theo múi giờ của bạn
- 🗣️ **Language**: Giao diện và nội dung học tập sẽ theo ngôn ngữ này
- 🌍 Hỗ trợ đa ngôn ngữ: vi, en, ja, ko, zh, fr, de, es

### 8. Bio tối đa bao nhiêu ký tự?

- 📝 Tối đa **500 ký tự**
- ✏️ Đủ cho một đoạn giới thiệu ngắn gọn
- ✅ Có thể bỏ trống nếu không muốn giới thiệu

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề, vui lòng liên hệ:

- 📧 Email: support@lexia.com
- 📚 Documentation: http://localhost:8080/swagger-ui.html
- 🐛 Bug Report: GitHub Issues

---

## 🔄 Lịch Sử Cập Nhật

**v1.0.0** (Sprint 1 - October 28, 2025):

- ✅ Authentication API (register, login, refresh, logout)
- ✅ User Profile Management API (get, update, avatar)
- ✅ JWT authentication với access token (15 min) và refresh token (7 days)
- ✅ Audit logging cho tất cả thay đổi
- ✅ Swagger UI documentation
- ✅ 81% test coverage

**Sắp tới** (Sprint 2):

- 🔜 Forgot Password / Reset Password
- 🔜 Change Password
- 🔜 Avatar upload (multipart/form-data)
- 🔜 Course Management APIs
- 🔜 AI Features APIs

---
