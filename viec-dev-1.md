# Việc Dev 1 cần làm để Dev 2 không bị chặn

Chỉ ghi những việc của Dev 1 mà phần của Dev 2 đang phải chờ (cập nhật 2026-11-01, sau bước 4 của Dev 2). Các việc khác của Dev 1 theo `plan.md` và `docs/pha4-cai-dat.md` như cũ. Chi tiết thay đổi của Dev 2 so với thiết kế xem `thay-doi-thiet-ke.md`.

## 1. Sinh đề và sao bàn (core) — chặn việc tạo ván khi chấp nhận lời mời

- `BoardGenerator.generateBoard(rows, cols, iconTypes, seed)`: server gọi `generateBoard(10, 10, 25, seed)` khi người được mời chấp nhận (`ChallengeController.handleInviteReply()`), cần trả `Board` cả lưới 12x12 (vùng chơi 10x10 và viền trống), vì `MATCH_START` gửi `tiles` là mảng 12x12 của bàn này.
- `Board.copy()`: sao sâu (cả mảng `tiles`); `MatchManager.createMatch()` gọi hai lần để mỗi người một bàn riêng.
- Đang chờ: 3 test của Dev 2 tự bỏ qua cho tới khi hai hàm này chạy được (`ChallengeControllerTest.chapNhanHopLe_...`, `MatchManagerTest`). Làm xong thì chạy `./gradlew :server:test`, ba test này phải đạt.

## 2. Đăng nhập, đăng xuất — chặn sảnh, lời mời, Vị trí của tôi

Server chỉ nhận `GET_ONLINE_LIST`, `INVITE`... từ kết nối đã đăng nhập, nên chưa có đăng nhập thì không vào được sảnh.

- `LoginController.handleLogin()`: `PlayerDAO.checkLogin()` đúng thì `handler.setPlayer(player)` **trước** khi gọi `OnlinePlayerManager.getInstance().addOnlinePlayer(handler)` (hàm này đọc người chơi từ handler và đặt trạng thái `IDLE`). Hàm trả `false` (tài khoản đang online ở máy khác) thì `handler.setPlayer(null)` rồi trả lỗi. Thành công thì gửi `LOGIN_RESULT` (`ok`, `player`) rồi `new ChallengeController().broadcastOnlineList()`.
- `PlayerDAO.checkLogin()` trả `Player` có cả `id` (tổng điểm trong danh sách online tính theo `id`) và `username`.
- `LoginController.handleLogout()`: trước khi `handler.setPlayer(null)`, gọi `OnlinePlayerManager.getInstance().removeOnlinePlayer(handler)` và `new ChallengeController().closeInvitations(username, "OFFLINE")` (pha 3 mục 7: đăng xuất khi có lời mời chờ thì đóng các lời mời đó), cuối cùng `broadcastOnlineList()`.
- `LoginActivity`: đăng ký `MessageListener` trong `onStart()`, hủy trong `onStop()`. Nhận `LOGIN_RESULT` thành công thì `ClientSession.player = msg.get("player", Player.class)`, `setResult(RESULT_OK)` rồi `finish()`. `MainActivity` mở `LoginActivity` từ nút Đấu đối kháng bằng `registerForActivityResult`, nhận `RESULT_OK` thì mở `LobbyActivity`; mở từ nút Đăng nhập thì cũng chỉ cần `finish()` để về giao diện chính.
- `MainActivity.onLogoutClick()` (nút `btnLogout`, chữ "Đăng nhập" khi là khách): khách thì mở `LoginActivity`; đã đăng nhập thì gửi `LOGOUT`, `ClientSession.clear()`.

## 3. Nhận ván ở MatchActivity — cần cho bước kết thúc ván của Dev 2

- `BaseActivity` mở `MatchActivity` khi nhận `MATCH_START`, kèm extra `BaseActivity.EXTRA_MATCH_START` là cả message dạng JSON. Đọc bằng `Message.fromJson(getIntent().getStringExtra(BaseActivity.EXTRA_MATCH_START))`: `roomId` (`getInt`), `tiles` (`get("tiles", int[][].class)`, lưới 12x12), `timeLimitSec`, `opponent` (`get("opponent", Player.class)`, chỉ có `username`).
- `MatchActivity` không kế thừa `BaseActivity`, nên tự đăng ký `MessageListener` trong `onStart()` và hủy trong `onStop()`.
