# Thay đổi so với thiết kế khi cài đặt

Ghi lại những chỗ code của Dev 2 khác hoặc thêm so với thiết kế ban đầu. Tài liệu thiết kế trong repo (`docs/*.md`, nguồn `.puml`, `gen_seq.py`) được sửa theo code; Google Doc của Dev 2 thì không sửa (quyết định của người dùng 2026-11-01), nên tệp này là danh sách chỗ Google Doc đó khác với code. Mỗi bước cài đặt thêm một mục mới ở cuối.

## Trạng thái cập nhật tài liệu

Đã sửa (2026-11-01, sau bước 3):

- `plan.md` §5.2 và pha 3 mục 7: dòng `RANKING` (`listRanking`, `me` chỉ khi `mine`, trả lời `mine` rỗng).
- Pha 3 mục 3 (`ServerConnection`, kết nối lại), mục 4b (`tvEmpty`, định dạng số, kết nối mỗi khi giao diện chính hiện ra, quyền mạng cục bộ, "bước 1–15").
- Kịch bản v3 M2 trong `gen_seq.py`: giao diện chính hiện trước, thêm bước `MainActivity.onResume()` (xin quyền, kết nối trên luồng nền); M2 thành 47 bước. Đã chạy lại `gen_seq.py` (khối `v3-m2` trong pha 3, `seq-m2.puml`).
- `class-design-m2.puml`: thêm `RankingActivity.tvEmpty`, `MainActivity.onResume()`, `ServerConnection.appContext`, `setContext()`, `isConnected()`.
- Pha 4 mục 1c, 1d, 2a, 2c.
- Pha 5 mục 2c (dữ liệu mẫu) đã sửa ở bước 1.

Chưa làm:

- Hình 3.7, 3.15, 3.18 đã dựng lại trong Visual Paradigm ngày 2026-11-01 (bản "(01-11)" trong `pikachu.vpp`, ảnh `docs/vp/png/` đã xuất lại). Bố cục 3.7, 3.15 chép từ hai bản chỉnh tay "Thử: ... (dọc)"; 3.15 còn 3 đường cắt qua khối (`MatchController` tới `Board`, `BoardGenerator`, `MatchDAO`), cần chỉnh tay rồi `python docs/vp/build_vp.py --export class-design-m4`. Bản cũ vẫn còn trong `pikachu.vpp` cho tới khi xóa tay.
- Gạch chân thuộc tính tĩnh (`GameServer.serverSocket`, `ChallengeController.inviteTimer`): `build_vp.py` chưa đọc được `{static}` trong `.puml`, nên chưa vẽ.
- Hình 3.15 cần dựng lại sau bước 5–6: `MatchController.endMatch()` thêm tham số `player`, `MatchRoom` thêm `endReason` (nguồn `class-design-m4.puml` đã sửa, ảnh PlantUML đã vẽ lại; plugin Visual Paradigm không kết nối được lúc làm nên chưa dựng bản VP: `python docs/vp/build_vp.py --add class-design-m4` hoặc sửa tay hai dòng trong `pikachu.vpp`, rồi `--export class-design-m4`).
- Google Doc của Dev 2: không sửa (theo quyết định).
- Biểu đồ của Dev 1 (M1, M3) có vẽ `ServerConnection` nhưng chỉ các phương thức mà modul đó dùng, không cần thêm.

Phân loại:

- **Thêm**: thiết kế không có, code thêm vào; phần đã có trong thiết kế giữ nguyên.
- **Khác**: code làm khác điều tài liệu ghi.
- **Chốt**: thiết kế không nói rõ, đã hỏi và chốt với người dùng.

## Bước 1: hạ tầng server và CSDL (2026-11-01)

| Lớp, tệp | Loại | Nội dung | Tài liệu cần sửa |
|---|---|---|---|
| Các lớp điều khiển | Chốt | `ClientHandler` tạo `LoginController`, `RankingController`, `ChallengeController`, `MatchController` bằng `new` ở mỗi lần xử lý message (biểu đồ không có `getInstance()` cho các lớp này) | không cần |
| `ChallengeController` | Thêm | `inviteTimer` khai báo `static` để mọi lời mời dùng chung một bộ hẹn giờ | Hình 3.15: gạch chân `inviteTimer` |
| `GameServer` | Thêm | `serverSocket` khai báo `static` (vì `main()` là hàm tĩnh) | Hình 3.7: gạch chân `serverSocket` |
| `ClientHandler` | Thêm | get/set cho `player`, `roomId` | không cần (get/set không vẽ) |
| `ClientHandler` | Thêm | Khi mất kết nối, chỉ gọi `closeInvitations()` và `broadcastOnlineList()` nếu `removeOnlinePlayer()` trả đúng, để kết nối có lần đăng nhập bị từ chối (tài khoản đang online ở máy khác) không gỡ nhầm người đang online | Pha 4 mục 2a (mô tả `ClientHandler.run()`) |
| `ClientHandler` | Thêm | Một message xử lý lỗi (ngoại lệ) thì ghi log và bỏ qua, không đứt kết nối; dòng không phải JSON cũng bỏ qua | Pha 4 mục 2a |
| `OnlinePlayerManager` | Thêm | Hàm khởi tạo riêng tư, thuộc tính tĩnh `instance` cho `getInstance()`; `addOnlinePlayer()` đặt trạng thái `IDLE` | không cần |
| `Message` | Thêm | `put()`, `has()`, `getString()`, `getInt()`, `getBoolean()`, `getCell()`, `get(key, Class)`, `get(key, Type)` (pha 4 mục 2a có nhắc một phần) | Biểu đồ lớp nếu muốn vẽ đủ |
| `ServerConfig` | Thêm | Đọc `server.properties` ở thư mục chạy, đổi đường dẫn bằng `-Dpikachu.config=...` | Pha 4 mục 1c |
| Server | Khác | Thông báo console và nội dung ngoại lệ viết tiếng Anh (console Windows không hiện được tiếng Việt); giao diện app vẫn tiếng Việt | không cần |
| `create-db.sql`, `server.properties.example` | Chốt | Tài khoản MySQL `pikachu`, mật khẩu `pikachu` (máy chạy thử) | Pha 4 mục 1d |
| `seed.sql`, `seed-tiebreak.sql` | Thêm | Sinh bằng `server/db/gen_seed.py` (quy hoạch nguyên, NumPy/SciPy); mật khẩu mẫu chung `123456`; TB điểm đối thủ lệch bảng mẫu pha 2 dưới 0.05 (làm tròn một chữ số thì khớp); cặp `minhanh` – `nguoichoie` gặp nhau 13 ván; ván 13 là ván thắng do bỏ cuộc. `seed-tiebreak.sql` có thêm hai người trùng cả ba tiêu chí | Pha 5 mục 2c (đã ghi), pha 4 mục 1b (đã ghi) |

## Bước 2: bảng xếp hạng phía server (2026-11-01)

| Lớp, tệp | Loại | Nội dung | Tài liệu cần sửa |
|---|---|---|---|
| Message `RANKING` | Chốt | Danh sách dòng của trang nằm trong trường `listRanking`; mỗi dòng đúng 5 trường `rank`, `username`, `totalPoint`, `avgOpponentPoint`, `avgWinTime` (không có `id`) | Pha 3 mục 7 (bảng message), `plan.md` §5.2 |
| `RankingController.handleGetRanking()` | Chốt | `mine = true` mà người hỏi là khách hoặc chưa có trong bảng: gửi `RANKING` không có `me`, không có `page`, `listRanking` rỗng, `pageCount` vẫn đúng; client giữ nguyên trang | Pha 3 mục 7, pha 4 mục 2c |
| `RankingController.handleGetRanking()` | Chốt | `me` chỉ gửi khi `mine = true`; thiếu `page` thì coi như trang 1 | Pha 3 mục 7 |
| `RankingController.handleGetRanking()` | Thêm | Tìm dòng của người chơi theo tên đăng nhập | không cần |
| `PlayerRankingDAO.getRanking()` | Thêm | Lỗi CSDL thì in lỗi và trả danh sách rỗng (client sẽ hiện "Chưa có dữ liệu xếp hạng") | Pha 4 mục 2c |
| `PlayerRankingDAO.getTotalPoints()` | Chốt | Để khung, làm ở bước sảnh | không cần |
| Test | Thêm | `TestData` (chèn dữ liệu trong giao dịch rồi rollback), lớp con của `ClientHandler` giữ message gửi đi; `DAOTest` (kết nối, utf8mb4, tên tiếng Việt) | Pha 4 mục 3 nếu muốn liệt kê |

## Bước 3: tầng mạng Android và màn bảng xếp hạng (2026-11-01)

| Lớp, tệp | Loại | Nội dung | Tài liệu cần sửa |
|---|---|---|---|
| `ServerConnection` | Thêm | `appContext : Context` và `setContext(context)` (giao diện chính truyền vào): khi mất kết nối, lớp này xóa `ClientSession`, báo "Mất kết nối tới máy chủ, vui lòng đăng nhập lại" và mở lại màn hình khởi động của ứng dụng (xóa các màn hình khác). Mở qua intent khởi động chứ không gọi thẳng `MainActivity`, nên gói `network` không phụ thuộc gói `view` | Hình 3.7 (và các biểu đồ khác có `ServerConnection`), pha 4 mục 2a |
| `ServerConnection` | Thêm | `isConnected()`: màn hình kiểm tra trước khi gửi | Như trên |
| `ServerConnection` | Thêm | `send()` ghi trên một luồng ghi riêng (Android cấm dùng mạng trên luồng giao diện); luồng đọc riêng; `connect()` chờ tối đa 5 giây và phải gọi trên luồng nền | Pha 4 mục 2a |
| `MainActivity` | Khác | Kết nối mỗi lần giao diện chính hiện ra (`onResume()`), không chỉ khi mở ứng dụng; nhờ đó tự kết nối lại sau khi mất kết nối hoặc khi server bật lại. Lần bấm Xem bảng xếp hạng đầu tiên sau khi server bật lại vẫn báo lỗi, lần sau mới vào được | Pha 3 mục 4b, kịch bản v3 M2 bước 5 |
| `MainActivity` | Khác | Giao diện chính hiện ngay, kết nối chạy song song trên luồng nền (kịch bản v3 M2 bước 13–14 ghi kết nối xong rồi mới hiện) | Kịch bản v3 M2 bước 13–14 (`gen_seq.py`) |
| `MainActivity`, manifest | Thêm | Android 17 (API 37) trở lên: xin quyền `ACCESS_LOCAL_NETWORK` (quyền lúc chạy) trước khi kết nối, hỏi tối đa một lần mỗi lần mở ứng dụng; không cho phép thì không kết nối được | Kịch bản v3 M2 (bước xin quyền trước bước 5), pha 4 mục 1c |
| `MainActivity` | Thêm | Layout đủ theo Hình 3.5; nút dưới cùng id `btnLogout` hiện "Đăng nhập" khi là khách, "Đăng xuất" khi đã đăng nhập. Các nút của Dev 1 và nút Cài đặt, Đấu đối kháng để TODO | Biểu đồ lớp M1 (Dev 1): thêm nút Đăng nhập |
| `RankingActivity` | Thêm | `tvEmpty` hiện "Chưa có dữ liệu xếp hạng" (thiết kế yêu cầu hiện câu này nhưng biểu đồ lớp không có thành phần nào) | Hình 3.7 |
| `RankingActivity` | Thêm | Adapter và view holder cho `RecyclerView`; hàm định dạng số và hàm tìm dòng của mình | không cần |
| `RankingActivity` | Chốt | Định dạng như bảng mẫu: tổng điểm bỏ ".0" (12.5, 9), TB điểm đối thủ một chữ số lẻ, TB thời gian thắng mm:ss làm tròn tới giây, rỗng hiện "--"; dòng của mình nền vàng nhạt, chữ đậm | Pha 4 mục 2c |
| `RankingActivity` | Thêm | "Không kết nối được máy chủ", "Bạn chưa có trong bảng xếp hạng" hiện bằng Toast | không cần |
| `ClientSession` | Thêm | `clear()` (file của Dev 1) đặt `player = null` | không cần |
| Manifest | Thêm | Mọi màn hình khóa ngang `sensorLandscape` (theo quyết định giao diện kiểu game) | Pha 4 mục 1c |
| Giao diện | Chốt | Tạm dùng Material mặc định, làm theme kiểu game ở bước sau | không cần |

## Bước 4: sảnh, lời mời, tạo ván khi chấp nhận (2026-11-01)

| Lớp, tệp | Loại | Nội dung | Tài liệu |
|---|---|---|---|
| Message `ONLINE_LIST` | Chốt | Danh sách nằm trong trường `onlinePlayers`; mỗi dòng đúng `username`, `status`, `totalPoint`; sắp theo tổng điểm giảm dần rồi tên (như Hình 3.10) | Đã sửa: pha 3 mục 7, `plan.md` §5.2 |
| Message `MATCH_START` | Chốt | `tiles` là lưới 12x12 của đề (có viền), `timeLimitSec` = 600, `opponent` là đối tượng chỉ gồm `username` | Đã sửa: pha 3 mục 7, `plan.md` §5.2 |
| `BaseActivity` → `MatchActivity` | Chốt | Mở `MatchActivity` kèm extra `EXTRA_MATCH_START` là cả message `MATCH_START` dạng JSON | Đã sửa: pha 4 mục 2e; ghi cho Dev 1 ở `viec-dev-1.md` |
| `OnlinePlayerManager` | Thêm | `hasInvitation(from)`: `handleInvite()` cần biết người mời đã có lời mời đi chưa (biểu đồ không có hàm nào cho việc này) | Đã sửa: `class-design-m4.puml`, pha 4 mục 2e |
| `OnlinePlayerManager.updateStatus()` | Chốt | Đặt trạng thái theo tên đăng nhập; trả `false` nếu có người không online | Đã sửa: pha 4 mục 2e |
| `ChallengeController` | Khác | Bộ hẹn giờ của lời mời cũ (đã trả lời, hủy) vẫn chạy; nếu người đó mời lại cùng người kia trước khi nó chạy thì `onInviteTimeout()` gỡ nhầm lời mời mới (lỗi tìm thấy khi chạy thử: lời mời mới hết hạn sau 20 giây). Sửa: giữ tác vụ hẹn giờ hiện hành của mỗi người mời (`inviteTasks`, tĩnh), tác vụ cũ tự bỏ qua | Đã sửa: pha 4 mục 2e; `class-design-m4.puml` thêm `inviteTasks` |
| `ChallengeController.handleInviteReply()` | Khác | Kiểm tra lại hai người chưa bận, sinh đề, tạo phòng và chuyển sang bận trong cùng một khối đồng bộ (trên `OnlinePlayerManager`), để hai lời chấp nhận cùng lúc với cùng một người chỉ tạo một ván. Người mời vừa rời mạng hoặc đã bận thì người trả lời nhận `INVITE_CLOSED` (`OFFLINE` hoặc `BUSY`) | Đã sửa: pha 4 mục 2e |
| `ChallengeController` | Thêm | `handleInvite()`: người được mời rời mạng ngay sau khi ghi lời mời thì gỡ lời mời và báo `OFFLINE`. Các hàm riêng dựng message và gửi theo tên đăng nhập | không cần |
| `ChallengeController.broadcastOnlineList()` | Chốt | Chỉ gửi cho người đang rỗi (`IDLE`), theo pha 3 mục 4d | không cần |
| `ChallengeController.handleUpdateStatus()` | Thêm | Bỏ qua trạng thái khác `IDLE`, `TRAINING` (client không được tự đặt `BUSY`) | Đã sửa: pha 4 mục 2e |
| `MatchManager` | Thêm | Hàm khởi tạo riêng tư, `instance`; hằng `TIME_LIMIT_SEC` = 600; mỗi phòng một `Timer` riêng | không cần |
| `MatchRoom` (Dev 1) | Thêm | Hàm khởi tạo `MatchRoom(roomId, match, handlers, boards)` (đặt `startMillis`, `rematchReplies`, `ended`) và get/set; Dev 2 viết vì `createMatch()` cần (người dùng chốt) | Đã sửa: `class-design-m4.puml` |
| `PlayerRankingDAO.getTotalPoints()` | Chốt | Trả một `PlayerRanking` cho mỗi người trong danh sách, cùng thứ tự, giữ `id`, `username`, `status`; danh sách rỗng thì không truy vấn | Đã sửa: pha 4 mục 2e |
| `MainActivity.onMatchClick()` | Chốt | Khách: mở `LoginActivity` bằng `registerForActivityResult`, nhận `RESULT_OK` thì mở `LobbyActivity` (hợp đồng với Dev 1 ở `viec-dev-1.md`) | Đã sửa: pha 4 mục 2e |
| `LobbyActivity` | Thêm | Tên người chơi góc phải và dòng "(click tên để thách đấu)" như Hình 3.10 (biểu đồ lớp không có); `showMessage()` để `ChallengeConfirmDialog` hiện lý do; extra `EXTRA_MESSAGE` cho thông báo khi mở; chưa kết nối thì báo "Không kết nối được máy chủ" và đóng | Đã sửa: `class-design-m4.puml`, pha 4 mục 2e |
| `ChallengeConfirmDialog` | Thêm | Hàm khởi tạo nhận `LobbyActivity` và `opponent`; tiêu đề đổi từ "Xác nhận thách đấu" sang "Đang chờ trả lời" như Hình 3.11; đóng khi nhận `MATCH_START`; không đóng bằng Back | Đã sửa: pha 4 mục 2e |
| `InvitationDialog` | Thêm | Giữ message lời mời (`invitation`) để cất lại vào hàng đợi khi đổi màn hình; cửa sổ `FLAG_NOT_TOUCH_MODAL` và `FLAG_NOT_FOCUSABLE` (phím Back và chạm ra ngoài đi xuống màn hình bên dưới), không làm mờ nền | Đã sửa: `class-design-m4.puml`, pha 4 mục 2e |
| `BaseActivity` | Thêm | Chỉ màn hình đang ở trên cùng xử lý ba message chung (lúc chuyển màn hình có hai màn hình cùng đăng ký); popup lời mời chuyển sang màn hình mới khi đổi màn hình, màn hình dừng mà không có màn hình nào nhận thì cất lại vào hàng đợi; `onInvitationClosed()`; hạn của lời mời lưu trong message (trường `deadline` chỉ có trên máy) | Đã sửa: `class-design-m4.puml`, pha 4 mục 2e |
| `BaseActivity` | Khác | `MATCH_START` đóng màn hình hiện tại trừ màn hình gốc (giao diện chính), để sau ván vẫn quay về được | Đã sửa: pha 4 mục 2e |
| Test | Thêm | `FakeClient`; 3 test tự bỏ qua cho tới khi Dev 1 làm xong `generateBoard()`, `Board.copy()` (đã chạy đạt với bản vá tạm) | không cần |

Hình 3.15 `class-design-m4` đã dựng lại (xem mục trạng thái).

## Bước 5–6: kết thúc ván và tái đấu (2026-11-02)

| Lớp, tệp | Loại | Nội dung | Tài liệu |
|---|---|---|---|
| `MatchController.endMatch()` | Chốt | Thêm tham số `player : ClientHandler`: người xóa hết ô (`CLEARED`), người thoát hoặc mất kết nối (`QUIT`, `DISCONNECT`), rỗng khi hết giờ. Thiết kế chỉ có `endMatch(room, reason)` nên không biết ai thắng, ai thoát. `handleMove()` của Dev 1 gọi `endMatch(room, "CLEARED", handler)` | Đã sửa: `class-design-m4.puml`, pha 3 mục 4d, pha 4 mục 2e, `plan.md` Phần II §2. Hình 3.15 (VP) chưa dựng lại |
| `MatchRoom` (Dev 1) | Chốt | Thêm `endReason : String` (lý do kết thúc) và get/set: `stopRematch()` cần biết ván có hỏi tái đấu không (đối thủ đã thoát thì người còn lại không bị đẩy về sảnh) | Như trên |
| `MatchController.endMatch()` | Chốt | `finishTime` của người thắng `CLEARED` do `endMatch()` ghi (lúc gọi trừ `startMillis`), không do `handleMove()` ghi (pha 4 cũ ghi `handleMove()` ghi rồi mới gọi `endMatch()`); `endMatch()` được gọi ngay trong khối khóa phòng của nước đi cuối nên hai cách cho cùng thời điểm | Đã sửa: pha 4 mục 2e |
| Message `MATCH_END` | Chốt | Theo người nhận: `roomId`, `result`, `point`, `finishTime`, `remaining` của người nhận, `opponentFinishTime`, `opponentRemaining`, `reason`, `rematch`, `replySec` (30); `finishTime` tính bằng mili giây, không có trường này nếu người đó chưa xóa hết ô | Đã sửa: pha 3 mục 7, `plan.md` §5.2 |
| `MatchController.endMatch()` | Khác | Không "dùng lại" `timer` của phòng: `Timer` của Java đã `cancel()` thì không hẹn được nữa, nên hủy `Timer` 10 phút rồi gán một `Timer` mới hẹn 30 giây chờ trả lời tái đấu. Với `QUIT`, `DISCONNECT`, hẹn giờ trước rồi mới `stopRematch()` (pha 4 ghi hẹn giờ ở cuối) | Đã sửa: pha 3 mục 4d, pha 4 mục 2e |
| `MatchController.endMatch()` | Chốt | `saveMatch()` lỗi thì ghi log, ván vẫn kết thúc và vẫn gửi `MATCH_END` (điểm của ván đó không được tính) | Đã sửa: pha 4 mục 2e |
| `MatchController` | Chốt | "Còn ở phòng" nghĩa là `roomId` của `ClientHandler` bằng mã phòng; `stopRematch()` "bỏ khỏi phòng" bằng cách đặt `roomId` về rỗng. Message của người đã rời phòng bị bỏ qua | Đã sửa: pha 3 mục 4d, pha 4 mục 2e |
| `MatchController.stopRematch()` | Thêm | Ghi `false` vào `rematchReplies` của người dừng (cả khi thoát, hết giờ, mất kết nối) | Đã sửa: pha 4 mục 2e |
| `MatchController.handleRematchReply()` | Thêm | Bỏ qua khi ván chưa kết thúc, hoặc khi đồng ý mà ván không hỏi tái đấu (đối thủ đã thoát) | Đã sửa: pha 4 mục 2e |
| `MatchController.onRematchTimeout()` | Thêm | Phòng đã bị bỏ (đã tái đấu, mọi người đã dừng) thì không làm gì | Đã sửa: pha 4 mục 2e |
| `MatchController.handleDisconnect()` | Thêm | Trong lúc chờ khóa phòng cũ, hai người có thể vừa tái đấu (handler đã sang phòng mới); khi đó đọc lại `roomId` rồi khóa đúng phòng, để người mất kết nối bị xử thua ở ván mới | Đã sửa: pha 4 mục 2e |
| `MatchController` | Thêm | Hằng `REMATCH_REPLY_SEC` = 30; các hàm riêng tìm phòng theo message, kiểm tra còn ở phòng, tính điểm (thắng 1, hòa 0.5, thua 0), dựng `MATCH_END` | không cần |
| `ChallengeController` | Thêm | `ROWS`, `COLS`, `ICON_TYPES` và `matchStart()` đổi từ `private` sang phạm vi gói để `MatchController` dùng lại khi tái đấu (biểu đồ đã có liên kết `MatchController` – `ChallengeController`) | không cần |
| `MatchDAO.saveMatch()` | Chốt | Bàn lưu dạng chuỗi 100 mã hình vùng chơi (bỏ viền) theo từng hàng, như pha 3 mục 2b và `seed.sql`; gán `match.id` từ khóa sinh ra; trả `autoCommit` về như lúc được gọi | Đã sửa: pha 4 mục 2e |
| `ResultDialog` | Thêm | Hàm khởi tạo `ResultDialog(matchActivity, matchEnd, opponent)`: `MatchActivity` truyền message `MATCH_END` và tên đối thủ (để báo "B đã dừng chơi"; `OPPONENT_LEFT` không có dữ liệu) | Đã sửa: pha 3 mục 4d, pha 4 mục 2e; ghi cho Dev 1 ở `viec-dev-1.md` |
| `ResultDialog` | Chốt | Tiêu đề Chiến thắng! / Thất bại / Hòa!; nội dung theo kịch bản pha 2; `QUIT` và `DISCONNECT` cùng "Đối thủ đã thoát."; thời gian hoàn thành mm:ss | Đã sửa: pha 4 mục 2e |
| `ResultDialog` | Thêm | Bấm Tái đấu thì khóa nút Tái đấu; không đóng bằng Back hay chạm ra ngoài; cờ `closed` để không về sảnh hai lần (hết giờ đúng lúc nhận `OPPONENT_LEFT`); về sảnh thì mở `LobbyActivity` (extra `EXTRA_MESSAGE`) rồi đóng `MatchActivity` | Đã sửa: pha 4 mục 2e |
| `strings.xml` | Thêm | `quit_lost` "Bạn đã thoát nên bị xử thua (0 điểm)" cho `MatchActivity.onQuitClick()` của Dev 1 | không cần |
| Test | Thêm | `MatchDAOTest` (3 test, chạy được ngay); `MatchControllerTest` (13 test theo pha 4 mục 3b test 8, gọi `endMatch()` qua reflection, tự bỏ qua cho tới khi Dev 1 xong `generateBoard()`, `Board.copy()`, `countRemaining()`, `MatchRoom.getBoard()`, `getOpponent()`; đã chạy đạt cả 13 với bản vá tạm); `TestData.countResultsOfMatch()`, `countMatches()`; `FakeClient.matchReady()` | không cần |

Chạy thử (bản vá tạm, không commit: đăng nhập, sinh đề, `Board`, `MatchRoom.getBoard()`, `getOpponent()`, ván 25 giây): `./gradlew :server:test` 63/63 đạt; server thật với hai client qua socket đạt cả 5 tình huống (hết giờ rồi cả hai tái đấu, thoát giữa ván rồi Về sảnh, một người tái đấu một người dừng, mất kết nối giữa ván, hết 30 giây không trả lời); ván lưu đúng vào CSDL (đã xóa sau khi thử). `ResultDialog` chưa chạy trên máy ảo (máy ảo tự thoát lúc khởi động), mới build và lint.

## Lệch trong tài liệu của Dev 1 (chưa sửa)

- Biểu đồ lớp thiết kế M1 còn thuộc tính `Player.name` (đã bỏ: tên đăng nhập là tên hiển thị).
- Biểu đồ lớp M1 ghi `removeOnlinePlayer()` trả `void`; biểu đồ M4 và code trả `boolean`.
- Biểu đồ lớp M1 chưa có nút Đăng nhập trên giao diện chính (Hình 3.5 đã có).
