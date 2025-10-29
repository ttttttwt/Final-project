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

## 🔐 Bảo Mật & Quyền Riêng Tư

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
