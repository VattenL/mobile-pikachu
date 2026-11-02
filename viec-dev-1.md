# Việc Dev 1 cần làm để Dev 2 không bị chặn

Chỉ ghi những việc của Dev 1 mà phần của Dev 2 đang phải chờ (cập nhật 2026-11-02, sau bước 5–6 của Dev 2). Các việc khác của Dev 1 theo `plan.md` và `docs/pha4-cai-dat.md` như cũ. Chi tiết thay đổi của Dev 2 so với thiết kế xem `thay-doi-thiet-ke.md`.

## 1. Sinh đề, sao bàn, đếm ô (core) — chặn việc tạo ván, kết thúc ván, tái đấu

- `BoardGenerator.generateBoard(rows, cols, iconTypes, seed)`: server gọi `generateBoard(10, 10, 25, seed)` khi người được mời chấp nhận (`ChallengeController.handleInviteReply()`), cần trả `Board` cả lưới 12x12 (vùng chơi 10x10 và viền trống), vì `MATCH_START` gửi `tiles` là mảng 12x12 của bàn này.
- `Board.copy()`: sao sâu (cả mảng `tiles`); `MatchManager.createMatch()` gọi hai lần để mỗi người một bàn riêng.
- `Board.countRemaining()`: số ô còn hình trên cả lưới; `MatchController.endMatch()` dùng cho trường `remaining`, `opponentRemaining` của `MATCH_END`. `Board.isCleared()`: `handleMove()` dùng.
- Tái đấu cũng gọi `generateBoard(10, 10, 25, seed)` (trong `MatchController.handleRematchReply()`).

## 2. MatchRoom.getBoard(), getOpponent() — chặn kết thúc ván và tái đấu

- `getBoard(handler)`: bàn của người đó (`handlers[i]` chơi trên `boards[i]`). `getOpponent(handler)`: `ClientHandler` còn lại của phòng. `endMatch()`, `stopRematch()` gọi hai hàm này, nên trước khi có chúng mọi cách kết thúc ván đều ném `UnsupportedOperationException`.
- Dev 2 đã thêm `endReason : String` và get/set vào `MatchRoom` (lý do kết thúc, do `endMatch()` ghi); không cần làm gì với thuộc tính này.

## 3. Chạy lại test của Dev 2 khi xong mục 1, 2

Chạy `./gradlew :server:test`. Các test đang tự bỏ qua phải đạt hết: `ChallengeControllerTest.chapNhanHopLe_...`, `MatchManagerTest` (2 test), `MatchControllerTest` (13 test). Test đọc ghi CSDL nên cần MySQL đã nạp `schema.sql`, `seed.sql`.

## 4. Đăng nhập, đăng xuất — chặn sảnh, lời mời, Vị trí của tôi

Server chỉ nhận `GET_ONLINE_LIST`, `INVITE`... từ kết nối đã đăng nhập, nên chưa có đăng nhập thì không vào được sảnh.

- `LoginController.handleLogin()`: `PlayerDAO.checkLogin()` đúng thì `handler.setPlayer(player)` **trước** khi gọi `OnlinePlayerManager.getInstance().addOnlinePlayer(handler)` (hàm này đọc người chơi từ handler và đặt trạng thái `IDLE`). Hàm trả `false` (tài khoản đang online ở máy khác) thì `handler.setPlayer(null)` rồi trả lỗi. Thành công thì gửi `LOGIN_RESULT` (`ok`, `player`) rồi `new ChallengeController().broadcastOnlineList()`.
- `PlayerDAO.checkLogin()` trả `Player` có cả `id` (tổng điểm trong danh sách online tính theo `id`) và `username`.
- `LoginController.handleLogout()`: trước khi `handler.setPlayer(null)`, gọi `OnlinePlayerManager.getInstance().removeOnlinePlayer(handler)` và `new ChallengeController().closeInvitations(username, "OFFLINE")` (pha 3 mục 7: đăng xuất khi có lời mời chờ thì đóng các lời mời đó), cuối cùng `broadcastOnlineList()`.
- `LoginActivity`: đăng ký `MessageListener` trong `onStart()`, hủy trong `onStop()`. Nhận `LOGIN_RESULT` thành công thì `ClientSession.player = msg.get("player", Player.class)`, `setResult(RESULT_OK)` rồi `finish()`. `MainActivity` mở `LoginActivity` từ nút Đấu đối kháng bằng `registerForActivityResult`, nhận `RESULT_OK` thì mở `LobbyActivity`; mở từ nút Đăng nhập thì cũng chỉ cần `finish()` để về giao diện chính.
- `MainActivity.onLogoutClick()` (nút `btnLogout`, chữ "Đăng nhập" khi là khách): khách thì mở `LoginActivity`; đã đăng nhập thì gửi `LOGOUT`, `ClientSession.clear()`.

## 5. MatchActivity — chặn kết thúc ván, tái đấu, thoát ván phía client

- `BaseActivity` mở `MatchActivity` khi nhận `MATCH_START`, kèm extra `BaseActivity.EXTRA_MATCH_START` là cả message dạng JSON. Đọc bằng `Message.fromJson(getIntent().getStringExtra(BaseActivity.EXTRA_MATCH_START))`: `roomId` (`getInt`), `tiles` (`get("tiles", int[][].class)`, lưới 12x12), `timeLimitSec`, `opponent` (`get("opponent", Player.class)`, chỉ có `username`).
- `MatchActivity` không kế thừa `BaseActivity`, nên tự đăng ký `MessageListener` trong `onStart()` và hủy trong `onStop()`.
- `MATCH_END`: mở dialog kết quả bằng `new ResultDialog(this, msg, opponent.getUsername()).show()` (đối thủ lấy từ `MATCH_START`). Không cần đọc các trường của `MATCH_END`; nếu muốn dừng đồng hồ, khóa bàn thì cứ làm.
- Bỏ qua `MATCH_START` và `OPPONENT_LEFT` trong `MatchActivity.onMessage()`: `ResultDialog` tự đăng ký nhận hai message này (mở ván mới khi tái đấu, về sảnh khi đối thủ dừng) và tự đóng `MatchActivity`.
- `onQuitClick()`: gửi `new Message(MessageType.LEAVE_MATCH).put("roomId", roomId)`, mở `LobbyActivity` kèm `putExtra(LobbyActivity.EXTRA_MESSAGE, getString(R.string.quit_lost))` ("Bạn đã thoát nên bị xử thua (0 điểm)", chuỗi đã có), rồi `finish()`. Không hiện `ResultDialog` (server không gửi `MATCH_END` cho người thoát).

## 6. MatchController.handleMove() — chặn kết thúc ván khi xóa hết ô

- Khóa phòng (`synchronized (room)`), bỏ qua nếu `handler.getRoomId()` khác `roomId` của message hoặc `room.isEnded()`.
- Bàn của người gửi `isCleared()` thì gửi `MOVE_RESULT`, `OPPONENT_PROGRESS` của nước cuối như mọi nước khác, rồi gọi `endMatch(room, "CLEARED", handler)` vẫn trong khối khóa đó (chữ ký mới có tham số thứ ba là người xóa hết ô), để client vẽ xong cặp cuối rồi mới nhận `MATCH_END`. Không cần tự ghi `finishTime`: `endMatch()` ghi theo đồng hồ server.
