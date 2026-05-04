# Cập Nhật Thông Tin Người Dùng (User Update Flow)

> Tài liệu này mô tả luồng cập nhật thông tin người dùng.  
> Chỉ cho phép thay đổi trường `fullName`.  
> Tất cả request **phải** gửi kèm header: `Authorization: Bearer <accessToken>`

---

## API

### `PUT /api/users/{id}` — Cập nhật tên người dùng

### Request

```http
PUT /api/users/2
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "fullName": "Nguyễn Văn B"
}
```

**Path param:**

| Param | Kiểu   | Mô tả       |
|-------|--------|-------------|
| `id`  | `Long` | ID của user |

**Request body:**

| Field      | Kiểu     | Bắt buộc | Mô tả              |
|------------|----------|----------|--------------------|
| `fullName` | `String` | Có       | Tên mới (không được để trống) |

---

### Response — `200 OK`

```json
{
  "id": 2,
  "email": "nguyen.van.a@hcmut.edu.vn",
  "fullName": "Nguyễn Văn B",
  "avatarUrl": "https://lh3.googleusercontent.com/...",
  "role": "STAFF",
  "status": "ACTIVE"
}
```

Trả về toàn bộ thông tin user sau khi cập nhật.

---

## Sequence

```
Frontend                    Backend                    Database
   │                           │                           │
   │── PUT /api/users/{id} ───▶│                           │
   │   { fullName }            │                           │
   │                           │── findById(id) ───────────▶│
   │                           │◀── User / empty ──────────│
   │                           │   [404 nếu không tìm thấy]│
   │                           │   user.setFullName(...)   │
   │                           │── save(user) ─────────────▶│
   │                           │◀── User đã cập nhật ──────│
   │◀── 200 UserResponse ──────│                           │
```

---

## Xử lý lỗi

| Trường hợp                      | Status | Message                        |
|---------------------------------|--------|--------------------------------|
| `id` không tồn tại              | `404`  | `User not found with id: {id}` |
| `fullName` để trống hoặc null   | `400`  | Validation error               |
| Không gửi token / token hết hạn | `401`  | Unauthorized                   |

---

## Lưu ý cho Frontend

- Chỉ field `fullName` được phép thay đổi qua endpoint này. Các field khác (`email`, `role`, `status`, `avatarUrl`) **không thể** cập nhật qua đây.
- `fullName` không được là chuỗi rỗng hoặc chỉ có khoảng trắng (`@NotBlank`).
- Response trả về object user đầy đủ — có thể dùng trực tiếp để cập nhật state trên UI mà không cần gọi thêm `GET /api/users`.
