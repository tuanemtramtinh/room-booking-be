# Luồng Đăng Nhập Google (Google OAuth Login Flow)

## Tổng quan

Hệ thống sử dụng **Google ID Token** (không phải Authorization Code). Frontend tự lấy token từ Google, sau đó gửi lên backend để xác thực.

---

## Sequence Diagram

```
Frontend                  Backend                    Google API               Database
   │                         │                            │                       │
   │─── Google Sign-In ──────────────────────────────────▶│                       │
   │◀── idToken ─────────────────────────────────────────│                       │
   │                         │                            │                       │
   │── POST /api/auth/google ▶│                            │                       │
   │   { idToken }           │                            │                       │
   │                         │── GET tokeninfo?id_token ──▶│                       │
   │                         │◀── GoogleTokenPayload ─────│                       │
   │                         │   { sub, email, name,      │                       │
   │                         │     picture, aud }         │                       │
   │                         │                            │                       │
   │                         │── findByGoogleId(sub) ─────────────────────────────▶│
   │                         │◀── user / empty ───────────────────────────────────│
   │                         │                            │                       │
   │                         │   [nếu không tìm thấy]     │                       │
   │                         │── findByEmail(email) ───────────────────────────────▶│
   │                         │◀── user / empty ───────────────────────────────────│
   │                         │                            │                       │
   │                         │   [nếu tìm thấy theo email]│                       │
   │                         │── save(user + googleId) ────────────────────────────▶│
   │                         │                            │                       │
   │                         │   [nếu không tìm thấy]     │                       │
   │                         │── save(newUser) ────────────────────────────────────▶│
   │                         │                            │                       │
   │                         │── generateToken(user) ─────│                       │
   │                         │◀── JWT ────────────────────│                       │
   │                         │                            │                       │
   │◀── AuthResponse ────────│                            │                       │
   │   { accessToken,        │                            │                       │
   │     tokenType: "Bearer",│                            │                       │
   │     user }              │                            │                       │
```

---

## Chi tiết từng bước

### Bước 1 — Frontend lấy Google ID Token

Frontend tích hợp Google Sign-In SDK và nhận về một `idToken` (JWT do Google ký).

### Bước 2 — Gửi request lên backend

```http
POST /api/auth/google
Content-Type: application/json

{
  "idToken": "<Google ID Token>"
}
```

> Validation: `idToken` không được để trống (`@NotBlank`).

---

### Bước 3 — Xác minh token với Google (`verifyGoogleToken`)

Backend gọi Google tokeninfo endpoint:

```
GET https://oauth2.googleapis.com/tokeninfo?id_token={idToken}
```

Google trả về `GoogleTokenPayload`:

| Field           | Ý nghĩa                        |
|-----------------|-------------------------------|
| `sub`           | Google User ID (định danh duy nhất) |
| `aud`           | Client ID của ứng dụng        |
| `email`         | Email người dùng              |
| `name`          | Tên đầy đủ                    |
| `picture`       | URL ảnh đại diện              |
| `email_verified`| Trạng thái xác minh email     |

**Kiểm tra bảo mật:** `payload.aud` phải khớp với `google.client-id` trong config. Nếu không khớp → `401 Unauthorized`.

---

### Bước 4 — Tìm hoặc tạo User (Upsert)

```
Tìm theo googleId (payload.sub)
        │
        ├─ Tìm thấy → dùng user đó (không thay đổi gì)
        │
        └─ Không tìm thấy
                │
                ├─ Tìm theo email (payload.email)
                │       └─ Tìm thấy → gắn googleId + cập nhật avatarUrl → save
                │
                └─ Không tìm thấy → tạo User mới → save
```

**User mới được tạo với:**

| Field       | Giá trị             |
|-------------|---------------------|
| `email`     | Từ Google payload   |
| `fullName`  | Từ Google payload   |
| `avatarUrl` | Từ Google payload   |
| `googleId`  | `sub` từ Google     |
| `role`      | `STAFF` (mặc định)  |
| `status`    | `ACTIVE` (mặc định) |

---

### Bước 5 — Kiểm tra trạng thái tài khoản

Nếu `user.status == INACTIVE` → `403 Forbidden: Account is inactive`.

---

### Bước 6 — Phát JWT nội bộ

`JwtService.generateToken(user)` tạo JWT chứa:

| Claim     | Giá trị              |
|-----------|----------------------|
| `sub`     | `user.id` (Long)     |
| `email`   | Email người dùng     |
| `role`    | `ADMIN` hoặc `STAFF` |
| `iat`     | Thời điểm phát hành  |
| `exp`     | Thời điểm hết hạn    |

JWT được ký bằng HMAC-SHA với secret key từ `app.jwt.secret`.

---

### Bước 7 — Trả về response

```json
{
  "accessToken": "<JWT>",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyen Van A",
    "avatarUrl": "https://...",
    "role": "STAFF",
    "status": "ACTIVE"
  }
}
```

---

## Phân quyền Admin / Staff

Google chỉ xác định **danh tính** (identity), không xác định **role**. Role được lấy từ database.

| Tình huống | Role nhận được |
|---|---|
| Email chưa có trong DB | `STAFF` (mặc định) |
| Email đã có trong DB với role `ADMIN` | `ADMIN` (giữ nguyên) |
| Admin promote user sau khi đăng nhập | Cập nhật qua API |

**Cách tạo tài khoản Admin:** Insert trực tiếp vào DB trước khi người dùng đăng nhập lần đầu:

```sql
INSERT INTO users (email, full_name, role, status)
VALUES ('admin@example.com', 'Admin Name', 'ADMIN', 'ACTIVE');
```

Khi Admin đăng nhập Google bằng email đó, hệ thống sẽ tìm thấy theo email, gắn `googleId` và giữ nguyên role `ADMIN`.

---

## Xử lý lỗi

| Trường hợp | HTTP Status | Message |
|---|---|---|
| `idToken` bị thiếu / rỗng | `400 Bad Request` | Validation error |
| Google API trả lỗi | `401 Unauthorized` | `Invalid Google token` |
| `aud` không khớp client ID | `401 Unauthorized` | `Invalid Google token` |
| Tài khoản bị vô hiệu hóa | `403 Forbidden` | `Account is inactive` |

---

## Sử dụng JWT cho các request tiếp theo

Sau khi đăng nhập, client gửi JWT trong header:

```http
Authorization: Bearer <accessToken>
```

`JwtAuthFilter` sẽ:
1. Trích xuất và xác thực JWT
2. Lấy `userId` từ claim `sub`
3. Load `User` từ DB
4. Set `Authentication` vào `SecurityContext` với authority `ROLE_ADMIN` hoặc `ROLE_STAFF`
