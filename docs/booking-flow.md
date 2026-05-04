# Booking Flow

> Tài liệu này mô tả toàn bộ luồng đặt phòng dành cho Frontend.  
> Base URL: `/api/bookings`  
> Tất cả request **phải** gửi kèm header: `Authorization: Bearer <accessToken>`

---

## Vòng đời trạng thái booking

```
                   ┌─────────┐
       Tạo mới ───▶│ PENDING │
                   └────┬────┘
                        │
              ┌─────────┼─────────┐
              ▼         ▼         ▼
         ┌────────┐ ┌──────────┐ ┌───────────┐
         │APPROVED│ │ REJECTED │ │ CANCELLED │
         └────────┘ └──────────┘ └───────────┘
```

| Trạng thái  | Ý nghĩa                               | Ai thực hiện |
|-------------|---------------------------------------|--------------|
| `PENDING`   | Vừa được tạo, chờ admin xét duyệt    | System       |
| `APPROVED`  | Admin đã duyệt                        | Admin        |
| `REJECTED`  | Admin từ chối (kèm lý do)             | Admin        |
| `CANCELLED` | User huỷ trước khi được duyệt         | User         |

---

## Danh sách API

| Method | Endpoint                    | Mô tả                         | Role  |
|--------|-----------------------------|-------------------------------|-------|
| `POST` | `/api/bookings`             | Tạo booking mới               | User  |
| `GET`  | `/api/bookings`             | Lấy danh sách booking         | Admin |
| `GET`  | `/api/bookings?status=PENDING` | Lọc booking theo trạng thái | Admin |
| `PUT`  | `/api/bookings/{id}/approve` | Duyệt booking                | Admin |
| `PUT`  | `/api/bookings/{id}/reject`  | Từ chối booking              | Admin |

---

## 1. Tạo booking — `POST /api/bookings`

### Request

```http
POST /api/bookings
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "roomId": 2,
  "title": "Họp nhóm dự án",
  "description": "Review sprint 3",
  "attendeeCount": 5,
  "startDate": "2026-05-10",
  "startHour": "09:00:00",
  "endHour": "11:00:00"
}
```

> `userId` **không cần gửi** — backend tự lấy từ JWT token.

### Mô tả các field request

| Field           | Kiểu        | Bắt buộc | Mô tả                                  |
|-----------------|-------------|----------|----------------------------------------|
| `roomId`        | `Long`      | Có       | ID của phòng muốn đặt                  |
| `title`         | `String`    | Có       | Tiêu đề buổi họp (không được để trống) |
| `description`   | `String`    | Không    | Mô tả thêm                             |
| `attendeeCount` | `Integer`   | Không    | Số người tham dự (tối thiểu 1 nếu có) |
| `startDate`     | `LocalDate` | Có       | Ngày đặt phòng. Format: `yyyy-MM-dd`   |
| `startHour`     | `LocalTime` | Có       | Giờ bắt đầu. Format: `HH:mm:ss`       |
| `endHour`       | `LocalTime` | Có       | Giờ kết thúc. Format: `HH:mm:ss`      |

> `startHour` phải nhỏ hơn `endHour`, ngược lại trả về `400 Bad Request`.

### Response — `201 Created`

```json
{
  "id": 10,
  "userId": 1,
  "roomId": 2,
  "title": "Họp nhóm dự án",
  "description": "Review sprint 3",
  "attendeeCount": 5,
  "startDate": "2026-05-10",
  "startHour": "09:00:00",
  "endHour": "11:00:00",
  "status": "PENDING",
  "rejectReason": null,
  "reviewedBy": null,
  "reviewedAt": null,
  "createdAt": "2026-05-04T08:00:00Z",
  "updatedAt": "2026-05-04T08:00:00Z"
}
```

### Sequence

```
Frontend                  Backend                    Database
   │                         │                           │
   │── POST /api/bookings ───▶│                           │
   │   { roomId, ... }       │                           │
   │                         │── Lấy user từ JWT         │
   │                         │── Validate request        │
   │                         │   startHour < endHour ?   │
   │                         │── findById(roomId) ───────▶│
   │                         │◀── Room / 404 ────────────│
   │                         │── save(booking)           │
   │                         │   status = PENDING ───────▶│
   │                         │── save(history)           │
   │                         │   null → PENDING ─────────▶│
   │◀── 201 BookingDTO ──────│                           │
```

### Xử lý lỗi

| Trường hợp                    | Status | Message                              |
|-------------------------------|--------|--------------------------------------|
| Field bắt buộc bị thiếu/null  | `400`  | Validation error                     |
| `attendeeCount` < 1           | `400`  | Validation error                     |
| `startHour` >= `endHour`      | `400`  | `startHour must be before endHour`   |
| `roomId` không tồn tại        | `404`  | `Room not found with id: {id}`       |
| Không gửi token / token hết hạn | `401` | Unauthorized                        |

---

## 2. Lấy danh sách booking — `GET /api/bookings`

### Request

```http
GET /api/bookings
Authorization: Bearer <admin-token>
```

Lọc theo trạng thái (tùy chọn):

```http
GET /api/bookings?status=PENDING
```

Các giá trị `status` hợp lệ: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`

### Response — `200 OK`

```json
[
  {
    "id": 10,
    "userId": 1,
    "roomId": 2,
    "title": "Họp nhóm dự án",
    "description": "Review sprint 3",
    "attendeeCount": 5,
    "startDate": "2026-05-10",
    "startHour": "09:00:00",
    "endHour": "11:00:00",
    "status": "PENDING",
    "rejectReason": null,
    "reviewedBy": null,
    "reviewedAt": null,
    "createdAt": "2026-05-04T08:00:00Z",
    "updatedAt": "2026-05-04T08:00:00Z"
  }
]
```

> Danh sách được sắp xếp theo `createdAt` mới nhất trước.

---

## 3. Duyệt booking — `PUT /api/bookings/{id}/approve`

### Request

```http
PUT /api/bookings/10/approve
Authorization: Bearer <admin-token>
```

> Không cần request body.  
> Chỉ duyệt được booking đang ở trạng thái `PENDING`.

### Response — `200 OK`

```json
{
  "id": 10,
  "userId": 1,
  "roomId": 2,
  "title": "Họp nhóm dự án",
  "description": "Review sprint 3",
  "attendeeCount": 5,
  "startDate": "2026-05-10",
  "startHour": "09:00:00",
  "endHour": "11:00:00",
  "status": "APPROVED",
  "rejectReason": null,
  "reviewedBy": 1,
  "reviewedAt": "2026-05-04T09:00:00Z",
  "createdAt": "2026-05-04T08:00:00Z",
  "updatedAt": "2026-05-04T09:00:00Z"
}
```

### Sequence

```
Frontend                  Backend                    Database
   │                         │                           │
   │── PUT /{id}/approve ────▶│                           │
   │                         │── Lấy admin từ JWT        │
   │                         │── findById(id) ───────────▶│
   │                         │◀── Booking / 404 ─────────│
   │                         │── status == PENDING ?     │
   │                         │   Không → 400             │
   │                         │── status = APPROVED       │
   │                         │── reviewedBy = admin      │
   │                         │── reviewedAt = now()      │
   │                         │── save(booking) ──────────▶│
   │                         │── save(history)           │
   │                         │   PENDING → APPROVED ─────▶│
   │◀── 200 BookingDTO ──────│                           │
```

### Xử lý lỗi

| Trường hợp                    | Status | Message                                  |
|-------------------------------|--------|------------------------------------------|
| `id` không tồn tại            | `404`  | `Booking not found with id: {id}`        |
| Booking không ở trạng thái `PENDING` | `400` | `Only PENDING bookings can be approved` |
| Không gửi token / token hết hạn | `401` | Unauthorized                            |

---

## 4. Từ chối booking — `PUT /api/bookings/{id}/reject`

### Request

```http
PUT /api/bookings/10/reject
Authorization: Bearer <admin-token>
Content-Type: application/json
```

```json
{
  "rejectReason": "Phòng đã được đặt trước trong khung giờ này"
}
```

> `rejectReason` không bắt buộc nhưng nên điền để user biết lý do.  
> Chỉ từ chối được booking đang ở trạng thái `PENDING`.

### Response — `200 OK`

```json
{
  "id": 10,
  "userId": 1,
  "roomId": 2,
  "title": "Họp nhóm dự án",
  "description": "Review sprint 3",
  "attendeeCount": 5,
  "startDate": "2026-05-10",
  "startHour": "09:00:00",
  "endHour": "11:00:00",
  "status": "REJECTED",
  "rejectReason": "Phòng đã được đặt trước trong khung giờ này",
  "reviewedBy": 1,
  "reviewedAt": "2026-05-04T09:00:00Z",
  "createdAt": "2026-05-04T08:00:00Z",
  "updatedAt": "2026-05-04T09:00:00Z"
}
```

### Sequence

```
Frontend                  Backend                    Database
   │                         │                           │
   │── PUT /{id}/reject ─────▶│                           │
   │   { rejectReason }      │                           │
   │                         │── Lấy admin từ JWT        │
   │                         │── findById(id) ───────────▶│
   │                         │◀── Booking / 404 ─────────│
   │                         │── status == PENDING ?     │
   │                         │   Không → 400             │
   │                         │── status = REJECTED       │
   │                         │── reviewedBy = admin      │
   │                         │── reviewedAt = now()      │
   │                         │── rejectReason = ...      │
   │                         │── save(booking) ──────────▶│
   │                         │── save(history)           │
   │                         │   PENDING → REJECTED ─────▶│
   │◀── 200 BookingDTO ──────│                           │
```

### Xử lý lỗi

| Trường hợp                    | Status | Message                                  |
|-------------------------------|--------|------------------------------------------|
| `id` không tồn tại            | `404`  | `Booking not found with id: {id}`        |
| Booking không ở trạng thái `PENDING` | `400` | `Only PENDING bookings can be rejected` |
| Không gửi token / token hết hạn | `401` | Unauthorized                            |

---

## Mô tả các field response (BookingDTO)

| Field          | Kiểu        | Mô tả                                           |
|----------------|-------------|-------------------------------------------------|
| `id`           | `Long`      | ID của booking                                  |
| `userId`       | `Long`      | ID user đặt phòng                               |
| `roomId`       | `Long`      | ID phòng được đặt                               |
| `title`        | `String`    | Tiêu đề buổi họp                                |
| `description`  | `String`    | Mô tả thêm                                      |
| `attendeeCount`| `Integer`   | Số người tham dự                                |
| `startDate`    | `String`    | Ngày đặt. Format: `yyyy-MM-dd`                  |
| `startHour`    | `String`    | Giờ bắt đầu. Format: `HH:mm:ss`                |
| `endHour`      | `String`    | Giờ kết thúc. Format: `HH:mm:ss`               |
| `status`       | `String`    | Trạng thái: `PENDING` / `APPROVED` / `REJECTED` / `CANCELLED` |
| `rejectReason` | `String`    | Lý do từ chối — `null` nếu chưa bị từ chối     |
| `reviewedBy`   | `Long`      | ID admin xét duyệt — `null` nếu chưa duyệt     |
| `reviewedAt`   | `Instant`   | Thời điểm xét duyệt (UTC) — `null` nếu chưa duyệt |
| `createdAt`    | `Instant`   | Thời điểm tạo (UTC)                             |
| `updatedAt`    | `Instant`   | Thời điểm cập nhật cuối (UTC)                   |

---

## Lưu ý cho Frontend

- `startDate`, `startHour`, `endHour` là 3 field **riêng biệt** — không dùng dạng timestamp gộp.
- Format ngày: `"2026-05-10"` (ISO 8601).
- Format giờ: `"09:00:00"` (HH:mm:ss).
- Sau khi tạo, booking luôn ở `PENDING` — hiển thị trạng thái chờ duyệt cho user.
- Admin dùng `GET /api/bookings?status=PENDING` để lấy danh sách cần xét duyệt.
- Approve không cần body, Reject nên gửi kèm `rejectReason` để user biết lý do.
- `reviewedBy`, `reviewedAt`, `rejectReason` đều `null` khi booking mới tạo, sẽ được điền sau khi admin xét duyệt.
