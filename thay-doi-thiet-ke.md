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

- Dựng lại hình trong Visual Paradigm (cần mở VP): Hình 3.7 `class-design-m2` (biểu đồ chỉnh tay, dựng bằng `--add` rồi chỉnh lại bố cục) và Hình 3.18 `seq-m2`. Ảnh `docs/vp/png/` hiện vẫn là bản cũ.
- Gạch chân thuộc tính tĩnh (`GameServer.serverSocket`, `ChallengeController.inviteTimer`): `build_vp.py` chưa đọc được `{static}` trong `.puml`, nên chưa vẽ.
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

## Lệch trong tài liệu của Dev 1 (chưa sửa)

- Biểu đồ lớp thiết kế M1 còn thuộc tính `Player.name` (đã bỏ: tên đăng nhập là tên hiển thị).
- Biểu đồ lớp M1 ghi `removeOnlinePlayer()` trả `void`; biểu đồ M4 và code trả `boolean`.
- Biểu đồ lớp M1 chưa có nút Đăng nhập trên giao diện chính (Hình 3.5 đã có).
