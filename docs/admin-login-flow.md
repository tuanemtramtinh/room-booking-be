# Luồng Đăng Nhập Admin (Admin Login Flow)

## Tổng quan

Admin đăng nhập bằng **email + password** (BCrypt), không qua Google OAuth. Tài khoản admin được tạo tự động khi app khởi động lần đầu thông qua `DataInitializer`.

---

## Sequence Diagram

```
Frontend                  Backend                    Database
   │                         │                           │
   │── POST /api/auth/admin ─▶│                           │
   │   { email, password }   │                           │
   │                         │── findByEmail(email) ─────▶│
   │                         │◀── user / empty ──────────│
   │                         │                           │
   │                         │   [Kiểm tra role]         │
   │                         │   [Kiểm tra password]     │
   │                         │   [Kiểm tra status]       │
   │                         │                           │
   │                         │── generateToken(user)     │
   │                         │                           │
   │◀── AuthResponse ────────│                           │
   │   { accessToken,        │                           │
   │     tokenType: "Bearer",│                           │
   │     user }              │                           │
```

---

## Chi tiết từng bước

### Bước 1 — Gửi request lên backend

```http
POST /api/auth/admin
Content-Type: application/json

{
  "email": "admin@hcmut.edu.vn",
  "password": "Admin@123456"
}
```

> Validation: `email` đúng định dạng (`@Email`), `password` không được để trống (`@NotBlank`).

---

### Bước 2 — Tìm user theo email

`AuthService` tìm user trong DB theo email.

- Không tìm thấy → `401 Unauthorized: Invalid credentials`

---

### Bước 3 — Kiểm tra role

User tìm thấy phải có `role = ADMIN`.

- `role != ADMIN` → `403 Forbidden: Access denied`

---

### Bước 4 — Kiểm tra password

So khớp password nhập vào với hash BCrypt đã lưu trong DB.

- `password == null` (chưa được set) → `401 Unauthorized: Invalid credentials`
- BCrypt không khớp → `401 Unauthorized: Invalid credentials`

> Dùng chung message `"Invalid credentials"` cho cả trường hợp không tìm thấy email lẫn sai password — tránh lộ thông tin user có tồn tại hay không.

---

### Bước 5 — Kiểm tra trạng thái tài khoản

- `status == INACTIVE` → `403 Forbidden: Account is inactive`

---

### Bước 6 — Phát JWT nội bộ

`JwtService.generateToken(user)` tạo JWT chứa:

| Claim   | Giá trị            |
|---------|--------------------|
| `sub`   | `user.id` (Long)   |
| `email` | Email admin        |
| `role`  | `ADMIN`            |
| `iat`   | Thời điểm phát hành|
| `exp`   | Thời điểm hết hạn  |

---

### Bước 7 — Trả về response

```json
{
  "accessToken": "<JWT>",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "email": "admin@hcmut.edu.vn",
    "fullName": "Administrator",
    "avatarUrl": null,
    "role": "ADMIN",
    "status": "ACTIVE"
  }
}
```

---

## Bảng xử lý lỗi

| Trường hợp                            | HTTP Status | Message               |
|---------------------------------------|-------------|-----------------------|
| `email` hoặc `password` để trống      | `400`       | Validation error      |
| Email không tồn tại trong DB          | `401`       | `Invalid credentials` |
| User tồn tại nhưng không phải ADMIN   | `403`       | `Access denied`       |
| Password chưa được set hoặc sai       | `401`       | `Invalid credentials` |
| Tài khoản bị vô hiệu hóa             | `403`       | `Account is inactive` |

---

## Khởi tạo tài khoản Admin (`DataInitializer`)

Khi app khởi động, `DataInitializer` chạy tự động và kiểm tra:

```
App khởi động
      │
      ▼
Tìm user theo app.admin.email trong DB
      │
      ├─ Không tìm thấy → Tạo user mới với role = ADMIN
      │       └─ Set password (BCrypt encode từ app.admin.password)
      │
      ├─ Tìm thấy, password == null → Chỉ update password
      │
      └─ Tìm thấy, đã có password → Không làm gì
```

Config trong `application.properties`:

```properties
app.admin.email=admin@hcmut.edu.vn
app.admin.full-name=Administrator
app.admin.password=Admin@123456
```

> Thay đổi `app.admin.password` trước khi deploy production.

---

## So sánh với Google Login

|                   | `POST /api/auth/google`       | `POST /api/auth/admin`   |
|-------------------|-------------------------------|--------------------------|
| **Đối tượng**     | STAFF (mọi người)             | ADMIN (chỉ 1 tài khoản)  |
| **Xác thực qua**  | Google API (ID Token)         | BCrypt password          |
| **Role**          | Lấy từ DB (mặc định `STAFF`)  | Bắt buộc phải là `ADMIN` |
| **Tạo user mới**  | Có (auto-register)            | Không                    |
