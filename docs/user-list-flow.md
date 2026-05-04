# Danh Sách Người Dùng (User List Flow)

> Tài liệu này mô tả luồng lấy danh sách người dùng trong hệ thống — dành cho admin.  
> Tất cả request **phải** gửi kèm header: `Authorization: Bearer <accessToken>`

---

## API

### `GET /api/users` — Lấy danh sách người dùng

Trả về danh sách tất cả user, **không bao gồm user đang đăng nhập**. Hỗ trợ lọc theo `role`, `status`, và tìm kiếm theo tên/email qua `keyword`.

### Request

```http
GET /api/users
Authorization: Bearer <accessToken>
```

**Query params (tất cả đều optional):**

| Param     | Kiểu     | Mô tả                                              | Ví dụ          |
|-----------|----------|----------------------------------------------------|----------------|
| `role`    | `String` | Lọc theo vai trò: `ADMIN` hoặc `STAFF`             | `?role=STAFF`  |
| `status`  | `String` | Lọc theo trạng thái: `ACTIVE` hoặc `INACTIVE`      | `?status=ACTIVE` |
| `keyword` | `String` | Tìm theo `fullName` hoặc `email` (case-insensitive)| `?keyword=nguyen` |

Các param có thể kết hợp:

```http
GET /api/users?role=STAFF&status=ACTIVE&keyword=nguyen
```

---

### Response — `200 OK`

```json
[
  {
    "id": 1,
    "email": "admin@hcmut.edu.vn",
    "fullName": "Administrator",
    "avatarUrl": null,
    "role": "ADMIN",
    "status": "ACTIVE"
  },
  {
    "id": 2,
    "email": "nguyen.van.a@hcmut.edu.vn",
    "fullName": "Nguyễn Văn A",
    "avatarUrl": "https://lh3.googleusercontent.com/...",
    "role": "STAFF",
    "status": "ACTIVE"
  }
]
```

> Trả về mảng rỗng `[]` nếu không có user nào khớp điều kiện lọc.

---

## Sequence

```
Frontend                    Backend                    Database
   │                           │                           │
   │── GET /api/users?... ─────▶│                           │
   │                           │   lấy currentUser.id      │
   │                           │   từ SecurityContext       │
   │                           │── findUsersWithFilters()  │
   │                           │   (excludeId, role,       │
   │                           │    status, keyword) ──────▶│
   │                           │◀── List<User> ────────────│
   │                           │   map → UserResponse      │
   │◀── 200 List<UserResponse> ─│                           │
```

---

## Mô tả các field response

| Field       | Kiểu     | Mô tả                                   |
|-------------|----------|-----------------------------------------|
| `id`        | `Long`   | ID của user                             |
| `email`     | `String` | Địa chỉ email (unique)                  |
| `fullName`  | `String` | Tên đầy đủ — có thể `null` với admin   |
| `avatarUrl` | `String` | URL ảnh đại diện — `null` nếu chưa có  |
| `role`      | `String` | `ADMIN` hoặc `STAFF`                    |
| `status`    | `String` | `ACTIVE` hoặc `INACTIVE`               |

---

## Xử lý lỗi

| Trường hợp                          | Status | Message                    |
|-------------------------------------|--------|----------------------------|
| `role` không hợp lệ (vd: `MANAGER`) | `400`  | `Invalid role: MANAGER`    |
| `status` không hợp lệ (vd: `BANNED`)| `400`  | `Invalid status: BANNED`   |
| Không gửi token / token hết hạn     | `401`  | Unauthorized               |

---

## Lưu ý cho Frontend

- Không truyền param nào → trả về **toàn bộ** user trong hệ thống, trừ chính người đang đăng nhập.
- `role` và `status` **không phân biệt hoa thường**: `staff`, `STAFF`, `Staff` đều hợp lệ.
- `keyword` tìm kiếm **partial match** trên cả `fullName` và `email`, không phân biệt hoa thường.
- Dùng `status=INACTIVE` để hiển thị danh sách tài khoản đã bị vô hiệu hóa.
- Thứ tự trả về theo ID tăng dần (mặc định của JPA).
