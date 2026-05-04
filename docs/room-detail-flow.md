# Chi Tiết Phòng (Room Detail Flow)

> Tài liệu này mô tả luồng lấy chi tiết một phòng kèm toàn bộ danh sách booking.  
> Tất cả request **phải** gửi kèm header: `Authorization: Bearer <accessToken>`

---

## API

### `GET /api/rooms/{id}` — Lấy chi tiết phòng

Trả về thông tin phòng kèm toàn bộ bookings của phòng đó, sắp xếp theo ngày và giờ bắt đầu tăng dần.

### Request

```http
GET /api/rooms/2
Authorization: Bearer <accessToken>
```

### Response — `200 OK`

```json
{
  "id": 2,
  "name": "Phòng họp A",
  "location": "Tầng 3",
  "capacity": 10,
  "description": "Phòng họp lớn có máy chiếu",
  "status": "AVAILABLE",
  "bookings": [
    {
      "id": 5,
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
    },
    {
      "id": 8,
      "userId": 3,
      "roomId": 2,
      "title": "Seminar kỹ thuật",
      "description": null,
      "attendeeCount": 8,
      "startDate": "2026-05-10",
      "startHour": "14:00:00",
      "endHour": "16:00:00",
      "status": "PENDING",
      "rejectReason": null,
      "reviewedBy": null,
      "reviewedAt": null,
      "createdAt": "2026-05-04T10:00:00Z",
      "updatedAt": "2026-05-04T10:00:00Z"
    }
  ]
}
```

> `bookings` trả về **tất cả trạng thái** (PENDING, APPROVED, REJECTED, CANCELLED).  
> Sắp xếp theo `startDate` → `startHour` tăng dần.  
> Trả về mảng rỗng `[]` nếu phòng chưa có booking nào.

---

## Sequence

```
Frontend                  Backend                    Database
   │                         │                           │
   │── GET /api/rooms/{id} ──▶│                           │
   │                         │── findById(id) ───────────▶│
   │                         │◀── Room / 404 ────────────│
   │                         │── findByRoomId()          │
   │                         │   ORDER BY startDate,     │
   │                         │   startHour ASC ──────────▶│
   │                         │◀── List<Booking> ─────────│
   │◀── 200 RoomDetailDTO ───│                           │
```

---

## Mô tả các field response

### Room

| Field         | Kiểu        | Mô tả                                              |
|---------------|-------------|----------------------------------------------------|
| `id`          | `Long`      | ID của phòng                                       |
| `name`        | `String`    | Tên phòng                                          |
| `location`    | `String`    | Vị trí / tầng                                      |
| `capacity`    | `Integer`   | Sức chứa tối đa                                    |
| `description` | `String`    | Mô tả thêm về phòng                                |
| `status`      | `String`    | `AVAILABLE` / `MAINTENANCE` / `INACTIVE`           |
| `bookings`    | `Array`     | Danh sách toàn bộ booking của phòng                |

### Booking (trong mảng `bookings`)

| Field          | Kiểu      | Mô tả                                                          |
|----------------|-----------|----------------------------------------------------------------|
| `id`           | `Long`    | ID booking                                                     |
| `userId`       | `Long`    | ID user đặt phòng                                              |
| `title`        | `String`  | Tiêu đề buổi họp                                               |
| `startDate`    | `String`  | Ngày đặt. Format: `yyyy-MM-dd`                                 |
| `startHour`    | `String`  | Giờ bắt đầu. Format: `HH:mm:ss`                               |
| `endHour`      | `String`  | Giờ kết thúc. Format: `HH:mm:ss`                              |
| `status`       | `String`  | `PENDING` / `APPROVED` / `REJECTED` / `CANCELLED`             |
| `rejectReason` | `String`  | Lý do từ chối — `null` nếu không bị từ chối                   |
| `reviewedBy`   | `Long`    | ID admin xét duyệt — `null` nếu chưa duyệt                    |
| `reviewedAt`   | `Instant` | Thời điểm xét duyệt (UTC) — `null` nếu chưa duyệt             |

---

## Xử lý lỗi

| Trường hợp                      | Status | Message                        |
|---------------------------------|--------|--------------------------------|
| `id` không tồn tại              | `404`  | `Room not found with id: {id}` |
| Không gửi token / token hết hạn | `401`  | Unauthorized                   |

---

## So sánh với `GET /api/rooms`

| | `GET /api/rooms` | `GET /api/rooms/{id}` |
|---|---|---|
| **Mục đích** | Hiển thị danh sách phòng | Xem chi tiết 1 phòng |
| **Bookings** | Không có | Có đầy đủ |
| **Dùng khi** | Trang chọn phòng | Bấm vào 1 phòng cụ thể |

---

## Lưu ý cho Frontend

- Dùng `status` của từng booking để render màu sắc khác nhau trên timeline (ví dụ: `APPROVED` → đỏ/bận, `PENDING` → vàng/chờ, `CANCELLED`/`REJECTED` → xám).
- Chỉ booking `APPROVED` mới thực sự chặn khung giờ — `PENDING` chỉ hiển thị tham khảo.
- Danh sách đã được sort sẵn theo ngày và giờ tăng dần, không cần sort lại ở frontend.
