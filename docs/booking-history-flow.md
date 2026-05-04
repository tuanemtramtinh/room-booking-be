# Lấy Lịch Sử Đặt Phòng (Booking History Flow)

> Tài liệu này mô tả luồng lấy lịch sử đặt phòng của user đang đăng nhập.  
> Tất cả request **phải** gửi kèm header: `Authorization: Bearer <accessToken>`

---

## API

### `GET /api/bookings/history` — Lấy lịch sử đặt phòng

Trả về toàn bộ booking của user đang đăng nhập, sắp xếp theo thời gian tạo mới nhất trước.

### Request

```http
GET /api/bookings/history
Authorization: Bearer <accessToken>
```

> Không cần truyền `userId` — backend tự lấy từ JWT token.

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
    "status": "APPROVED",
    "rejectReason": null,
    "reviewedBy": 1,
    "reviewedAt": "2026-05-04T09:00:00Z",
    "createdAt": "2026-05-04T08:00:00Z",
    "updatedAt": "2026-05-04T09:00:00Z"
  },
  {
    "id": 9,
    "userId": 1,
    "roomId": 3,
    "title": "Seminar kỹ thuật",
    "description": null,
    "attendeeCount": 10,
    "startDate": "2026-05-08",
    "startHour": "14:00:00",
    "endHour": "16:00:00",
    "status": "REJECTED",
    "rejectReason": "Phòng đã được đặt trước trong khung giờ này",
    "reviewedBy": 1,
    "reviewedAt": "2026-05-03T10:00:00Z",
    "createdAt": "2026-05-03T08:00:00Z",
    "updatedAt": "2026-05-03T10:00:00Z"
  }
]
```

> Trả về mảng rỗng `[]` nếu user chưa có booking nào.

---

## Sequence

```
Frontend                  Backend                    Database
   │                         │                           │
   │── GET /api/bookings/history                         │
   │   Authorization: Bearer ▶│                           │
   │                         │── Lấy userId từ JWT       │
   │                         │── findByUserId() ─────────▶│
   │                         │◀── List<Booking> ─────────│
   │◀── 200 List<BookingDTO> ─│                           │
```

---

## Mô tả các field response

| Field          | Kiểu        | Mô tả                                                              |
|----------------|-------------|--------------------------------------------------------------------|
| `id`           | `Long`      | ID của booking                                                     |
| `userId`       | `Long`      | ID user đặt phòng                                                  |
| `roomId`       | `Long`      | ID phòng được đặt                                                  |
| `title`        | `String`    | Tiêu đề buổi họp                                                   |
| `description`  | `String`    | Mô tả thêm — có thể `null`                                         |
| `attendeeCount`| `Integer`   | Số người tham dự — có thể `null`                                   |
| `startDate`    | `String`    | Ngày đặt. Format: `yyyy-MM-dd`                                     |
| `startHour`    | `String`    | Giờ bắt đầu. Format: `HH:mm:ss`                                   |
| `endHour`      | `String`    | Giờ kết thúc. Format: `HH:mm:ss`                                  |
| `status`       | `String`    | Trạng thái: `PENDING` / `APPROVED` / `REJECTED` / `CANCELLED`     |
| `rejectReason` | `String`    | Lý do từ chối — `null` nếu không bị từ chối                       |
| `reviewedBy`   | `Long`      | ID admin đã xét duyệt — `null` nếu chưa được duyệt                |
| `reviewedAt`   | `Instant`   | Thời điểm xét duyệt (UTC) — `null` nếu chưa được duyệt            |
| `createdAt`    | `Instant`   | Thời điểm tạo booking (UTC)                                        |
| `updatedAt`    | `Instant`   | Thời điểm cập nhật cuối (UTC)                                      |

---

## Xử lý lỗi

| Trường hợp                      | Status | Message       |
|---------------------------------|--------|---------------|
| Không gửi token / token hết hạn | `401`  | Unauthorized  |

---

## Lưu ý cho Frontend

- Dùng `status` để hiển thị badge trạng thái tương ứng cho từng booking.
- Khi `status = REJECTED`, hiển thị thêm `rejectReason` để user biết lý do.
- `reviewedBy` và `reviewedAt` đều `null` khi booking đang ở trạng thái `PENDING`.
- Danh sách sắp xếp theo `createdAt` mới nhất trước — không cần sort lại ở frontend.
