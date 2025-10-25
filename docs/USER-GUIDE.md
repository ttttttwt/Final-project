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
## 🔐 Bảo Mật

### ✅ Những Điều NÊN Làm:

1. **Lưu token an toàn**:

   - ✅ Sử dụng `localStorage` hoặc `sessionStorage`
   - ✅ KHÔNG lưu trong cookie nếu không cấu hình HttpOnly

2. **Xử lý token hết hạn**:

   - ✅ Tự động refresh khi gặp lỗi 401
   - ✅ Đăng xuất người dùng khi refresh token hết hạn

3. **Bảo vệ thông tin nhạy cảm**:

   - ✅ KHÔNG log token ra console trong production
   - ✅ KHÔNG gửi token qua URL parameters

4. **Sử dụng HTTPS**:
   - ✅ Production PHẢI dùng HTTPS
   - ✅ Development có thể dùng HTTP

### ❌ Những Điều KHÔNG NÊN Làm:

- ❌ KHÔNG chia sẻ token với người khác
- ❌ KHÔNG lưu token trong code
- ❌ KHÔNG sử dụng token sau khi đăng xuất
- ❌ KHÔNG bỏ qua lỗi 401 (Unauthorized)

---

## 🐛 Xử Lý Lỗi

### Bảng Mã Lỗi HTTP:

| Mã Lỗi | Ý Nghĩa      | Nguyên Nhân                | Giải Pháp                        |
| ------ | ------------ | -------------------------- | -------------------------------- |
| 400    | Bad Request  | Dữ liệu không hợp lệ       | Kiểm tra lại email, mật khẩu     |
| 401    | Unauthorized | Token không hợp lệ/hết hạn | Refresh token hoặc đăng nhập lại |
| 403    | Forbidden    | Không có quyền truy cập    | Liên hệ admin                    |
| 404    | Not Found    | Không tìm thấy tài nguyên  | Kiểm tra URL                     |
| 409    | Conflict     | Email đã tồn tại           | Dùng email khác                  |
| 500    | Server Error | Lỗi server                 | Thử lại sau hoặc liên hệ support |

---