# Room Availability — Kiểm Tra Trùng Lịch

> Phòng không có trạng thái cố định để xác định availability.  
> Thay vào đó, khi tạo booking, backend tự động kiểm tra xem phòng đó có bị đặt trùng giờ không.

---

## Nguyên tắc

- Một phòng bị coi là **không available** nếu đã có booking `APPROVED` trùng **cùng ngày** và **giao thoa khung giờ**.
- Booking ở trạng thái `PENDING` / `REJECTED` / `CANCELLED` **không** chặn việc đặt phòng.
- Không có field `status` riêng để đánh dấu phòng là bận hay rảnh — availability được tính động từ bảng `bookings`.

---

## Điều kiện trùng lịch

Hai booking được coi là trùng nếu thỏa **cả 3 điều kiện**:

```
1. Cùng roomId
2. Cùng startDate
3. startHour_cũ < endHour_mới  AND  endHour_cũ > startHour_mới
```

### Ví dụ minh họa

```
Booking đã APPROVED:   [09:00 ─────────── 11:00]

Đặt mới trùng giữa:           [10:00 ──── 12:00]   ✗ 409 Conflict
Đặt mới bao trùm:      [08:00 ───────────────── 12:00]   ✗ 409 Conflict
Đặt mới nằm trong:         [09:30 ──── 10:30]   ✗ 409 Conflict
Đặt mới sát cuối:                       [11:00 ── 13:00]   ✓ OK
Đặt mới sát đầu:   [07:00 ── 09:00]   ✓ OK
```

---

## Luồng tạo booking có kiểm tra conflict

```
POST /api/bookings
{ roomId, startDate, startHour, endHour, ... }
        │
        ├─ [1] Validate: startHour < endHour ?
        │       └─ Không → 400 "startHour must be before endHour"
        │
        ├─ [2] Tìm room theo roomId
        │       └─ Không thấy → 404 "Room not found"
        │
        ├─ [3] Kiểm tra conflict
        │       Có booking APPROVED nào trùng roomId + startDate + giờ không?
        │       └─ Có → 409 "Room is not available in this time slot"
        │
        └─ [4] OK → Tạo booking với status = PENDING
```

---

## Response khi trùng lịch

```http
HTTP/1.1 409 Conflict
```

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room is not available in this time slot"
}
```

---

## Xử lý lỗi liên quan

| Trường hợp                          | Status | Message                                  |
|-------------------------------------|--------|------------------------------------------|
| `startHour` >= `endHour`            | `400`  | `startHour must be before endHour`       |
| `roomId` không tồn tại              | `404`  | `Room not found with id: {id}`           |
| Phòng đã có booking APPROVED trùng giờ | `409` | `Room is not available in this time slot` |

---

## Lưu ý cho Frontend

- Khi nhận `409`, hiển thị thông báo **"Phòng không còn trống trong khung giờ này"** để user chọn giờ hoặc phòng khác.
- Booking `PENDING` (chờ duyệt) **không** chặn user khác đặt cùng phòng — chỉ booking đã `APPROVED` mới chặn.
- Frontend nên cho phép user chọn phòng khác hoặc đổi khung giờ khi nhận `409`.
