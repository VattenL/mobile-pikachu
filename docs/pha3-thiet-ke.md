# Pha 3. Thiết kế

## 1. Thiết kế lớp thực thể

Đầu vào là biểu đồ lớp thực thể pha phân tích (Hình 2.1). Ngôn ngữ cài đặt là Java cho cả client Android, server và thư viện dùng chung `core`; các lớp thực thể đặt trong `core` để client và server dùng chung một định nghĩa.

### a. Bổ sung thuộc tính id

- `Player`, `Match`, `MatchResult`, `Board` không kế thừa lớp nào nên được bổ sung thuộc tính `id`. Ở mục 2c, bảng bàn chơi được gộp vào bảng ván đấu và bảng kết quả, nên `id` của `Board` không còn trong CSDL.
- `PlayerRanking` kế thừa `Player` nên dùng `id` của `Player`, không bổ sung `id` riêng (giống các lớp thống kê kế thừa lớp thực thể trong giáo trình).

### b. Bổ sung kiểu dữ liệu

- `Player`: `id : int`, `username : String`, `password : String` (giá trị băm SHA-256 của mật khẩu, không lưu mật khẩu gốc; khai báo `transient` để đối tượng `Player` gửi qua mạng, ví dụ trong danh sách online, không mang theo giá trị này), `status : String` (`IDLE`: rỗi, `TRAINING`: luyện tập, `BUSY`: bận).
- `Match`: `id : int`, `startTime : Date`, `endTime : Date`, `timeLimit : int` (số giây, mặc định 600).
- `MatchResult`: `id : int`, `result : String` (WIN, DRAW, LOSE), `point : float` (điểm của ván theo đề: thắng 1, hòa 0.5, thua 0; suy ra từ `result`), `finishTime : Integer` (số mili giây tính từ lúc bắt đầu ván, rỗng nếu người chơi chưa xóa hết ô). Dùng kiểu bọc `Integer` để biểu diễn được giá trị rỗng.
- `Board`: `id : int`, `rowCount : int`, `columnCount : int`, `tiles : int[][]`. `rowCount`, `columnCount` là kích thước cả lưới (12 x 12): vùng chơi 10x10 ở giữa và một vòng viền trống để đường nối có thể đi vòng ra ngoài. Ô trống có giá trị 0, ô có hình mang mã hình từ 1 trở đi.
- `PlayerRanking`: kế thừa các thuộc tính của `Player`, thêm `rank : int` (thứ hạng, do server đánh số khi lấy bảng xếp hạng; bổ sung ở pha này vì client nhận từng trang và cần hạng của mỗi dòng), `totalPoint : float`, `avgOpponentPoint : float`, `avgWinTime : Float` (rỗng nếu người chơi chưa thắng ván nào có thời gian hoàn thành).
- Đơn vị thời gian: `timeLimit` là thời lượng cấu hình nên tính bằng giây (khớp trường `timeLimitSec` của giao thức); `finishTime`, `avgWinTime` là thời lượng đo được nên tính bằng mili giây, khi hiển thị đổi sang mm:ss.

### c. Chuyển quan hệ association sang aggregation, composition

Biểu đồ lớp thực thể pha phân tích đã xác định quan hệ đối tượng ở bước 5, nên giữ nguyên:

- `Match` ◆ `MatchResult` (1 - 2): kết quả sinh ra và mất đi cùng ván đấu.
- `MatchResult` ◇ `Player` (0..* - 1), `Match` ◇ `Board` (0..1 - 1) và `MatchResult` ◇ `Board` (0..1 - 1). Cùng lớp `Board` đóng hai vai: đề bài ban đầu của ván và bàn riêng của từng người, hai bàn này khác nhau ngay sau nước đi đầu tiên.
- `PlayerRanking` ▷ `Player` (kế thừa): mỗi dòng xếp hạng là một người chơi kèm các chỉ số thống kê, nên có sẵn tên người chơi để hiển thị mà không cần thuộc tính đối tượng riêng.

### d. Bổ sung thuộc tính đối tượng

- `Match` chứa `board : Board` và `results : MatchResult[]` (mảng hai phần tử).
- `MatchResult` chứa `player : Player` và `board : Board` (bàn riêng của người chơi trong ván).
- `PlayerRanking` không cần thuộc tính đối tượng vì đã kế thừa `Player`.

Ngoài các lớp thực thể, `core` có lớp giá trị `Cell` (`row : int`, `col : int`) biểu diễn toạ độ một ô, dùng làm tham số cho các phương thức xử lý bàn chơi. `Cell` không phải lớp thực thể vì không được quản lý hay lưu trữ.

Kết quả thu được biểu đồ lớp thực thể pha thiết kế như Hình 3.1.

![Hình 3.1: Biểu đồ lớp thực thể pha thiết kế](vp/png/class-design-entity.png)

*Hình 3.1: Biểu đồ lớp thực thể pha thiết kế*

## 2. Thiết kế cơ sở dữ liệu

Cơ sở dữ liệu đặt trên server, hệ quản trị đề xuất là MySQL vì server nhiều luồng cần ghi đồng thời (giáo trình cũng dùng MySQL).

### a. Mỗi lớp thực thể một bảng

`Player` → `tblPlayer`, `Match` → `tblMatch`, `MatchResult` → `tblMatchResult`, `Board` → `tblBoard`, `PlayerRanking` → `tblPlayerRanking`. `PlayerRanking` kế thừa `Player` nên `tblPlayerRanking` không có `id` riêng mà dùng khóa `playerId` tham chiếu `tblPlayer` (giống `tblSinhvien` dùng `tblThanhvienid` trong giáo trình); bảng này bị loại ở bước e.

### b. Bỏ thuộc tính kiểu đối tượng, đổi kiểu dữ liệu

- Bỏ `Match.board`, `Match.results`, `MatchResult.player`, `MatchResult.board`.
- `int`, `Integer` → `int(10)`, `String` → `varchar`, `Date` → `datetime` (các thuộc tính `float` chỉ có ở `point` và `PlayerRanking`, đều là dẫn xuất nên bị loại ở bước e). `Board.tiles` là mảng hai chiều nên được lưu dưới dạng chuỗi: mã hình của 100 ô vùng chơi, đọc theo từng hàng, cách nhau bởi dấu phẩy (tối đa 299 ký tự) → `varchar(300)`.

### c. Chuyển quan hệ số lượng thành quan hệ giữa các bảng

- `tblPlayer` - `tblMatchResult` là 1-n, giữ nguyên.
- `tblMatch` - `tblMatchResult` là 1-n (đúng 2), giữ nguyên.
- `tblBoard` - `tblMatch` là 1-0..1, nên gộp: cột `tiles` chuyển vào `tblMatch` (đề bài ban đầu của ván).
- `tblBoard` - `tblMatchResult` là 1-0..1, nên gộp tương tự: cột `tiles` chuyển vào `tblMatchResult` (bàn của người chơi lúc ván kết thúc).
- Sau hai lần gộp, bảng `tblBoard` bị loại cùng cột `id` của nó: đề bài và bàn riêng được xác định qua ván đấu và kết quả chứa chúng. `rowCount`, `columnCount` không cần lưu vì mọi ván đều dùng lưới 12x12 (vùng chơi 10x10); hai giá trị này là hằng số trong code, chuỗi `tiles` chỉ lưu 100 ô của vùng chơi.

### d. Khóa chính, khóa ngoại

- Khóa chính của mỗi bảng là cột `id`, tự tăng.
- `tblMatchResult` có hai khóa ngoại `matchId` (tham chiếu `tblMatch.id`) và `playerId` (tham chiếu `tblPlayer.id`). Cặp (`matchId`, `playerId`) là duy nhất: một người chơi chỉ có một kết quả trong một ván.
- Khóa ngoại thể hiện quan hệ đối tượng ở mục 1c: `matchId` đặt `ON DELETE CASCADE` vì `Match` ◆ `MatchResult` là gắn chặt (xóa ván thì xóa luôn hai kết quả); `playerId` giữ mặc định (không cho xóa người chơi còn kết quả) vì `MatchResult` ◇ `Player` chỉ là hợp thành.
- Ràng buộc "mỗi ván có đúng hai kết quả" không biểu diễn được bằng khóa; `MatchDAO.saveMatch()` bảo đảm bằng cách thêm ván và hai kết quả trong cùng một giao dịch.

### e. Loại bỏ thuộc tính dư thừa

- `Player.status` chỉ có ý nghĩa khi người chơi online và được giữ trong bộ nhớ server, không lưu vào `tblPlayer`.
- `tblPlayerRanking` bị loại vì toàn bộ thuộc tính là dẫn xuất, tính bằng truy vấn trên `tblMatchResult` (truy vấn ở mục 8d).
- `MatchResult.point` không lưu vì là thuộc tính dẫn xuất: điểm mỗi ván suy ra được từ `result` theo luật của đề (thắng 1, hòa 0.5, thua 0). Truy vấn tính điểm từ `result` (mục 8d). Nhờ vậy CSDL không thể chứa một kết quả trái luật (ví dụ hòa mà được 1 điểm), và tổng điểm của mọi người luôn bằng số ván đã đấu.
- `tblMatchResult.tiles` giữ lại dù cùng ván đã có `tblMatch.tiles`: bàn của một người lúc kết thúc không suy ra được từ đề bài, vì phụ thuộc thứ tự xóa và các lần xáo khi bàn người đó bế tắc.
- `tblMatch.endTime` giữ lại vì không suy ra được từ các cột khác (ván có thể kết thúc do hết giờ, do người chơi xóa hết ô hoặc do bỏ cuộc).

Kết quả thu được thiết kế cơ sở dữ liệu như Hình 3.2. Công cụ vẽ chỉ đánh dấu được ràng buộc duy nhất trên từng cột, nên ràng buộc duy nhất của cặp (`matchId`, `playerId`) và `ON DELETE CASCADE` của `matchId` được ghi ở bảng dưới hình.

![Hình 3.2: Biểu đồ thiết kế cơ sở dữ liệu](vp/png/erd.png)

*Hình 3.2: Biểu đồ thiết kế cơ sở dữ liệu*

| Bảng | Cột | Ghi chú |
|---|---|---|
| `tblPlayer` | `id`, `username`, `password` | `username` duy nhất; `password` là chuỗi băm SHA-256 dạng hex (64 ký tự) |
| `tblMatch` | `id`, `startTime`, `endTime`, `timeLimit`, `tiles` | `tiles` là đề bài ban đầu của ván |
| `tblMatchResult` | `id`, `result`, `finishTime`, `tiles`, `matchId`, `playerId` | `finishTime` rỗng nếu người chơi chưa xóa hết ô; `tiles` là bàn của người chơi lúc ván kết thúc (toàn ô trống nếu đã xóa hết); (`matchId`, `playerId`) duy nhất; `matchId` có `ON DELETE CASCADE` (xóa ván thì xóa kết quả) |

## 3. Kiến trúc và nguyên tắc chuyển từ phân tích sang thiết kế

Hệ thống gồm ba thành phần (module Gradle):

- `app`: client Android. Tầng giao diện là các `Activity`, `Dialog` và lớp vẽ bàn chơi `BoardView`; tầng mạng gồm `ServerConnection` (giữ kết nối TCP, gửi message, nhận message trên một luồng nền rồi chuyển về luồng giao diện cho các `MessageListener`) và `ClientSession` (giữ người chơi đang đăng nhập, tương tự phiên làm việc).
- `server`: Java thuần. `GameServer` mở `ServerSocket`; mỗi client có một `ClientHandler` chạy trên một luồng riêng, đọc từng dòng JSON và chuyển cho lớp điều khiển theo loại message. Lớp điều khiển gọi DAO để đọc ghi CSDL.
- Phát hiện mất kết nối: client gửi `PING` mỗi 5 giây (bộ hẹn giờ `pingTimer` của `ServerConnection`); `ClientHandler` đặt thời gian chờ đọc 15 giây cho socket, quá thời gian đó mà không nhận được dòng nào thì xử lý như mất kết nối. Phía client, khi socket báo lỗi, `ServerConnection` xóa người chơi khỏi `ClientSession`, đưa ứng dụng về giao diện chính ở chế độ khách với thông báo "Mất kết nối tới máy chủ, vui lòng đăng nhập lại" rồi kết nối lại; server đã gỡ người chơi khỏi danh sách online nên phải đăng nhập lại.
- `core`: Java thuần, dùng chung cho cả hai bên: lớp thực thể, các lớp xử lý bàn chơi (`PathFinder`, `BoardGenerator`, `Bot`) và lớp message `Message`.

Client không bao giờ gọi DAO trực tiếp: giữa giao diện và DAO luôn có tầng mạng và lớp điều khiển trên server. Server là trọng tài: mọi nước đi trong ván đối kháng được server kiểm tra lại bằng `PathFinder` trên bản bàn chơi của chính người gửi.

Các phương thức của lớp thực thể ở pha phân tích được chuyển sang thiết kế như sau:

| Phương thức pha phân tích | Lớp thiết kế | Lý do |
|---|---|---|
| `Player.checkLogin()` | `PlayerDAO.checkLogin()` | đọc CSDL |
| `Player.addPlayer()` | `PlayerDAO.addPlayer()` | ghi CSDL |
| `PlayerRanking.getRanking()`, `getTotalPoints()` | `PlayerRankingDAO` | truy vấn thống kê trên CSDL |
| `PlayerRanking.getPlayerPage()` và phần lấy một trang của `getRanking()` | `RankingController.handleGetRanking()` (server) | cắt trang và tìm dòng của người chơi trên kết quả truy vấn, để mỗi lần chỉ gửi một trang cho client |
| `Match.saveMatch()` | `MatchDAO.saveMatch()` | ghi CSDL |
| `Player.getOnlinePlayers()`, `addOnlinePlayer()`, `removeOnlinePlayer()`, `sendInvitation()`, `updateStatus()` | `OnlinePlayerManager` (server) | danh sách online, lời mời, trạng thái chỉ nằm trong bộ nhớ server, không có trong CSDL, nên giao cho một lớp quản lý trong bộ nhớ thay cho DAO |
| `Match.createMatch()` | `MatchManager.createMatch()` (server) | ván đang diễn ra chỉ nằm trong bộ nhớ server cho tới khi kết thúc mới được lưu |
| `Board.checkPath()`, `findAvailablePair()` | `PathFinder` (core) | thuật toán dùng chung cho client (chơi với máy) và server (trọng tài); tách khỏi lớp dữ liệu `Board` |
| `Board.generateBoard()`, `shuffle()` | `BoardGenerator` (core) | như trên |
| `Board.removePair()`, `isCleared()` | giữ ở `Board` | chỉ thao tác trên dữ liệu của chính bàn chơi |
| `Player.cancelInvitation()`, `replyInvitation()` | `OnlinePlayerManager.removeInvitation()`, gọi từ `ChallengeController.handleInviteCancel()`, `handleInviteReply()` | lời mời chỉ nằm trong bộ nhớ server; lớp điều khiển kiểm tra lời mời còn hiệu lực rồi báo cho hai bên |
| `Match.endMatch()` | `MatchController.endMatch()` | ván đang diễn ra nằm trong bộ nhớ server (`MatchRoom`); kết quả do server quyết định |
| `Match.replyRematch()` | `MatchController.handleRematchReply()` | lựa chọn tái đấu của hai người được ghi trên phòng chơi của server |

Tên phương thức được giữ nguyên như pha phân tích để truy vết được từ biểu đồ lớp phân tích tới biểu đồ lớp thiết kế. Các phương thức chuyển lên lớp điều khiển của server được đặt tên theo message mà chúng xử lý (`handle...`); bảng trên ghi lại tên mới của từng phương thức, ví dụ `getPlayerPage()` thành nhánh `mine` của `handleGetRanking()`.

## 4. Thiết kế tĩnh cho bốn modul

Màu trong các biểu đồ lớp thiết kế: xanh dương là tầng giao diện client, tím là tầng mạng client, xanh lá là tầng điều khiển (trên server, hoặc lớp xử lý của `core`), vàng là tầng DAO, cam là lớp thực thể và lớp message của `core`. Thuộc tính của lớp giao diện là các thành phần giao diện Android (`EditText`, `TextView`, `Button`, `RecyclerView`...) và các thuộc tính ẩn giữ dữ liệu cần cho xử lý.

### a. Modul quản lý tài khoản

Giao diện như Hình 3.3.

![Hình 3.3: Thiết kế giao diện modul quản lý tài khoản](uml/pha3/ui-m1.png)

*Hình 3.3: Thiết kế giao diện modul quản lý tài khoản*

- `RegisterScreen` → `RegisterActivity`: bốn `EditText` (tên đăng nhập, mật khẩu, nhập lại mật khẩu, tên hiển thị), `TextView` báo lỗi, nút Đăng ký, nút Quay lại. Ô rỗng và mật khẩu nhập lại được kiểm tra ngay trên client trước khi gửi `REGISTER`. Khi nhận `REGISTER_RESULT` thành công thì quay lại `LoginActivity` kèm tên đăng nhập.
- `LoginScreen` → `LoginActivity`: hai `EditText` nhập tên đăng nhập và mật khẩu, `TextView` báo lỗi, nút Đăng nhập, nút Đăng ký mở `RegisterActivity`; khi quay về sau khi đăng ký thì điền sẵn tên đăng nhập. `LoginActivity` cài đặt `MessageListener` để nhận `LOGIN_RESULT`.
- `MainScreen` → `MainActivity`: nút Đăng xuất. `onLogoutClick()` gửi `LOGOUT`, xóa người chơi khỏi `ClientSession` và về `LoginActivity` ngay, không chờ server trả lời.
- Trên server, `LoginController` gồm ba hàm:
  - `handleRegister()`: kiểm tra ba trường không rỗng, băm mật khẩu SHA-256, gọi `PlayerDAO.addPlayer()`; tên đăng nhập trùng thì CSDL báo vi phạm ràng buộc duy nhất và hàm trả về sai.
  - `handleLogin()`: gọi `PlayerDAO.checkLogin()` rồi `OnlinePlayerManager.addOnlinePlayer()`; hàm này trả về sai nếu tài khoản đang online ở máy khác.
  - `handleLogout()`: gọi `OnlinePlayerManager.removeOnlinePlayer()` rồi `ChallengeController.broadcastOnlineList()` để những người chơi khác thấy danh sách mới. Khi client mất kết nối, `ClientHandler` cũng gọi `removeOnlinePlayer()`.
- `PlayerDAO` kế thừa lớp `DAO`; `addPlayer()` dùng chung kết nối với `checkLogin()`.

![Hình 3.4: Biểu đồ lớp thiết kế modul quản lý tài khoản](uml/pha3/class-design-m1.png)

*Hình 3.4: Biểu đồ lớp thiết kế modul quản lý tài khoản*

### b. Modul xem bảng xếp hạng

Giao diện gồm hai màn hình: giao diện chính như Hình 3.5 và giao diện bảng xếp hạng như Hình 3.6.

![Hình 3.5: Thiết kế giao diện chính](uml/pha3/ui-m2-main.png)

*Hình 3.5: Thiết kế giao diện chính*

![Hình 3.6: Thiết kế giao diện bảng xếp hạng](uml/pha3/ui-m2-ranking.png)

*Hình 3.6: Thiết kế giao diện bảng xếp hạng*

- `MainScreen` → `MainActivity`: giao diện chính, hiện ngay khi mở ứng dụng, dùng chung cho khách và người chơi: `TextView` tên người chơi (hoặc "Khách"), bốn nút Chơi với máy, Đấu đối kháng, Xem bảng xếp hạng, Cài đặt; thuộc tính ẩn `player` lấy từ `ClientSession`, rỗng khi là khách. Màn hình cài đặt không thuộc modul nào nên không thiết kế ở đây.
- `RankingScreen` → `RankingActivity`: `RecyclerView` các dòng của trang đang xem, `TextView` số trang ("Trang 1/2"), nút Trang trước, Trang sau, Vị trí của tôi, Quay lại; thuộc tính ẩn `page`, `pageCount`, `listRanking`, `myRank`. `RankingActivity` kế thừa `BaseActivity` (mục 4d) để người chơi đã đăng nhập vẫn nhận được lời mời khi đang xem bảng xếp hạng. Nút Vị trí của tôi chỉ hiện khi `ClientSession` có người chơi đã đăng nhập. Mỗi lần chuyển trang, `RankingActivity` gửi `GET_RANKING` với số trang; nút Vị trí của tôi gửi `GET_RANKING` với `mine = true`. Khi nhận `RANKING` thì hiện trang và tô màu dòng có tên đăng nhập trùng người chơi trong `ClientSession` (không tô theo thứ hạng, vì hai người cùng hạng có cùng số hạng); `me` (thứ hạng của người hỏi, giữ ở `myRank`) rỗng thì báo "Bạn chưa có trong bảng xếp hạng". Không kết nối được server thì báo "Không kết nối được máy chủ" và quay về giao diện chính.
- Khách không cần đăng nhập vẫn xem được: `ServerConnection` kết nối tới server ngay khi mở ứng dụng, và server trả lời `GET_RANKING` trên mọi kết nối.
- Trên server, `RankingController.handleGetRanking()` gọi `PlayerRankingDAO.getRanking()` lấy cả bảng đã sắp xếp, đánh số hạng (mục 8d) rồi cắt ra trang được yêu cầu, mỗi trang `PAGE_SIZE` = 10 dòng, và gửi `RANKING`. Với `mine = true`, hàm tìm dòng của người chơi đang đăng nhập trên kết nối đó (`ClientHandler.player`) và trả trang chứa dòng đó, kèm `me` là thứ hạng của người chơi. Cả bảng được truy vấn lại mỗi lần vì số người chơi nhỏ; khi cần có thể chuyển sang `LIMIT`/`OFFSET` ngay trong truy vấn.
- `PlayerRankingDAO` kế thừa lớp `DAO` để dùng chung một kết nối CSDL.

![Hình 3.7: Biểu đồ lớp thiết kế modul xem bảng xếp hạng](vp/png/class-design-m2.png)

*Hình 3.7: Biểu đồ lớp thiết kế modul xem bảng xếp hạng*

### c. Modul chơi với máy

Ván với máy chạy hoàn toàn trên client và không lưu kết quả; client chỉ báo server trạng thái luyện tập để người khác vẫn thấy mình online và vẫn mời được: khi bắt đầu ván, `BotGameActivity` gửi `UPDATE_STATUS` với trạng thái luyện tập qua `ServerConnection`; khi ván kết thúc hoặc người chơi thoát thì gửi trạng thái rỗi. Phía server, `ChallengeController.handleUpdateStatus()` cập nhật trạng thái trong `OnlinePlayerManager` rồi gửi lại danh sách online (mục 4d). Giao diện như Hình 3.8 (màn hình ngang).

![Hình 3.8: Thiết kế giao diện modul chơi với máy](uml/pha3/ui-m3.png)

*Hình 3.8: Thiết kế giao diện modul chơi với máy*

- `DifficultyScreen` → `DifficultyActivity`: nhóm ba thẻ chọn độ khó `rgDifficulty` (mỗi thẻ có hình và tên độ khó, chỉ chọn được một), nút Bắt đầu, nút Quay lại.
- `BotGameScreen` → `BotGameActivity` và lớp vẽ `BoardView`. `BoardView` là một `View` tự vẽ: vẽ các ô hình, ô đang chọn và đường nối, nhận sự kiện chạm rồi gọi `BotGameActivity.onCellClick()`. `BotGameActivity` giữ các thuộc tính ẩn: bàn của người chơi `board`, bàn của máy `botBoard` (bản sao cùng đề), đối tượng `bot`, ô đã chọn trước `firstCell`, đồng hồ `timer` và `handler` để hẹn lượt của máy. Màn chơi có hai `BoardView`: bàn của người chơi ở giữa, bàn của máy thu nhỏ ở bên phải (`botBoardView`, chỉ để xem, `setInteractive(false)`); mỗi nước của máy được vẽ lên bàn thu nhỏ cùng hiệu ứng nối cặp.
- `BotResultDialog`: `TextView` kết quả, nút OK.
- Lớp xử lý trong `core`: `BoardGenerator` (sinh đề, xáo bàn), `PathFinder` (kiểm tra đường nối, tìm cặp còn nối được), `Bot` (chọn nước đi, thời gian chờ theo độ khó).

![Hình 3.9: Biểu đồ lớp thiết kế modul chơi với máy](uml/pha3/class-design-m3.png)

*Hình 3.9: Biểu đồ lớp thiết kế modul chơi với máy*

### d. Modul thách đấu và đấu đối kháng

Mỗi màn hình của modul một hình: màn hình đấu đối kháng (Hình 3.10), dialog xác nhận thách đấu và popup chờ trên máy người thách đấu (Hình 3.11), popup lời mời trên máy người được mời (Hình 3.12), giao diện chơi (Hình 3.13) và dialog kết quả (Hình 3.14, gồm cả trường hợp đối thủ đã thoát). Giao diện chính như Hình 3.5.

![Hình 3.10: Thiết kế màn hình đấu đối kháng](uml/pha3/ui-m4-lobby.png)

*Hình 3.10: Thiết kế màn hình đấu đối kháng*

![Hình 3.11: Thiết kế dialog xác nhận thách đấu](uml/pha3/ui-m4-confirm.png)

*Hình 3.11: Thiết kế dialog xác nhận thách đấu*

![Hình 3.12: Thiết kế dialog lời mời](uml/pha3/ui-m4-invitation.png)

*Hình 3.12: Thiết kế dialog lời mời*

![Hình 3.13: Thiết kế giao diện chơi ván đối kháng](uml/pha3/ui-m4-2.png)

*Hình 3.13: Thiết kế giao diện chơi ván đối kháng*

![Hình 3.14: Thiết kế dialog kết quả](uml/pha3/ui-m4-result.png)

*Hình 3.14: Thiết kế dialog kết quả*

- `MainScreen` → `MainActivity`: như modul xem bảng xếp hạng. Nút Đấu đối kháng: nếu `ClientSession` chưa có người chơi thì mở `LoginActivity` (modul quản lý tài khoản), đăng nhập xong thì mở `LobbyActivity`; nếu đã đăng nhập thì mở thẳng `LobbyActivity`.
- `LobbyScreen` → `LobbyActivity`: màn hình đấu đối kháng, gồm `RecyclerView` danh sách người chơi online (không gồm chính mình), `TextView` thông báo (bị từ chối, đối thủ bận, không trả lời, đã dừng chơi, bị xử thua do thoát), nút Quay lại; thuộc tính ẩn `onlinePlayers`. Khi mở, `LobbyActivity` gửi `GET_ONLINE_LIST` để lấy danh sách hiện tại, sau đó cập nhật theo `ONLINE_LIST` mà server tự đẩy. Click vào một người chơi không bận thì mở `ChallengeConfirmDialog`; click người đang bận thì không làm gì. `LobbyActivity` nhận `ONLINE_LIST`; `INVITE_RECEIVED`, `INVITE_CLOSED`, `MATCH_START` do lớp cha `BaseActivity` xử lý.
- `BaseActivity`: lớp cha trừu tượng của các Activity mở được khi đã đăng nhập (`MainActivity`, `LobbyActivity`, `RankingActivity`, `DifficultyActivity`, `BotGameActivity`; trừ `MatchActivity` vì người đang đấu không nhận lời mời), cài đặt `MessageListener`. Thuộc tính ẩn `invitationQueue` (hàng đợi lời mời, tĩnh nên dùng chung cho mọi màn hình) và `invitationDialog` (popup đang hiện). `onMessage()` xử lý chung ba message: `INVITE_RECEIVED` thì đưa lời mời vào hàng đợi và gọi `showNextInvitation()` (hiện popup nếu chưa có popup nào), nên người được mời nhận lời mời ở bất kỳ màn hình nào, kể cả khi đang chơi với máy; `INVITE_CLOSED` (người mời hủy, hết hạn, rời mạng hoặc đã vào ván khác) thì bỏ lời mời đó khỏi hàng đợi và đóng popup nếu đang hiện lời mời đó; `MATCH_START` thì đóng popup, xóa hàng đợi, đóng màn hình hiện tại và mở `MatchActivity`. Nếu màn hình đó là `BotGameActivity` thì ván với máy dừng (hủy đồng hồ và lượt của máy, không lưu). Khách không nhận các message này vì server chỉ gửi cho người đã đăng nhập.
- `ChallengeConfirmDialog`: `TextView` thông báo, nút Xác nhận, nút Hủy; thuộc tính ẩn `opponent`, `countDown`; cài đặt `MessageListener`. Dialog có hai trạng thái. Lúc đầu hỏi "Bạn muốn thách đấu B?": Hủy thì chỉ đóng dialog, không gửi gì; Xác nhận thì dialog gửi `INVITE` và chuyển thành popup chờ chặn màn hình "Đang chờ B trả lời... 30" đếm ngược, nút Hủy đổi thành Hủy lời mời (gửi `INVITE_CANCEL`). Vì popup chặn màn hình nên mỗi lúc một người chỉ có một lời mời đi. Nhận `INVITE_RESULT` thì đóng popup và hiện lý do trên `LobbyActivity`; nhận `MATCH_START` thì `BaseActivity` đóng popup và mở ván.
- `InvitationDialog`: popup không chặn (non-modal), hiện ở mép trên màn hình như một thông báo đẩy; màn hình bên dưới vẫn thao tác được, nên ván với máy vẫn chạy. Gồm `TextView` tên người mời, `TextView` đếm ngược, nút Accept, nút Reject; thuộc tính ẩn `challenger`, `countDown`. Thời gian còn lại tính từ `timeoutSec` trong `INVITE_RECEIVED`. Accept, Reject thì gửi `INVITE_REPLY` rồi đóng; hết giờ thì chỉ đóng (server tự hủy lời mời). Khi popup đóng, `BaseActivity` hiện lời mời kế tiếp còn hạn trong hàng đợi.
- `MatchScreen` → `MatchActivity`: bàn của mình (`BoardView`), bàn thu nhỏ của đối thủ (`opponentBoardView`, chỉ để xem), đồng hồ, số ô còn lại của hai bên, nút Thoát; thuộc tính ẩn `board`, `opponentBoard`, `roomId`, `opponent`, `firstCell`. Khác với ván với máy, `MatchActivity` không tự kiểm tra đường nối mà gửi `MOVE` lên server rồi cập nhật bàn theo `MOVE_RESULT`, thay bàn khi nhận `BOARD_SHUFFLED`; bàn của đối thủ được cập nhật theo `OPPONENT_PROGRESS` (cặp vừa xóa, đường nối và cả bàn hiện tại của đối thủ, gồm cả khi bàn đó vừa được xáo).
- Thoát ván: nút Thoát của `MatchActivity` (`onQuitClick()`) gửi `LEAVE_MATCH`, đóng màn chơi và mở `LobbyActivity` kèm thông báo "Bạn đã thoát nên bị xử thua (0 điểm)", không hiện dialog kết quả.
- `ResultDialog`: `TextView` kết quả (kết quả, điểm nhận được, thời gian hoàn thành hoặc số ô còn lại, lý do kết thúc), `TextView` đếm ngược 30 giây, nút Tái đấu, nút Dừng; thuộc tính ẩn `roomId`, `countDown`. Khi `MATCH_END` có `rematch = false` (đối thủ thoát hoặc mất kết nối) thì ẩn nút Tái đấu và nút Dừng mang chữ Về sảnh. Bấm Tái đấu thì gửi `REMATCH_REPLY` đồng ý và hiện "Đang chờ đối thủ trả lời...", vẫn bấm Dừng được; nhận `MATCH_START` (cả hai đã đồng ý) thì đóng dialog và mở `MatchActivity` với ván mới; nhận `OPPONENT_LEFT` thì về `LobbyActivity` kèm thông báo đối thủ đã dừng chơi. Bấm Dừng (Về sảnh) hoặc hết 30 giây thì gửi `REMATCH_REPLY` từ chối và về `LobbyActivity`.
- Trên server:
  - `ChallengeController` xử lý `GET_ONLINE_LIST` (`handleGetOnlineList()` gửi danh sách online hiện tại cho riêng người hỏi), `INVITE`, `INVITE_CANCEL`, `INVITE_REPLY`, `UPDATE_STATUS` và gửi lại danh sách online cho mọi người rỗi khi có thay đổi (`broadcastOnlineList()`). Quy tắc lời mời:
    - `handleInvite()`: bỏ qua nếu người mời chưa đăng nhập, đang bận, đã có một lời mời đi đang chờ, hoặc tự mời mình. `OnlinePlayerManager.sendInvitation()` ghi lời mời, hoặc trả lý do nếu người được mời không online (`OFFLINE`) hay đang bận (`BUSY`), khi đó gửi `INVITE_RESULT` cho người mời. Gửi được thì hẹn giờ 30 giây (`inviteTimer`) và gửi `INVITE_RECEIVED` kèm `timeoutSec = 30` cho người được mời.
    - `handleInviteCancel()`: gỡ lời mời đang chờ bằng `OnlinePlayerManager.removeInvitation()`, gửi `INVITE_CLOSED` (`CANCELLED`) cho người được mời.
    - `onInviteTimeout()`: hết 30 giây mà lời mời còn chờ thì gỡ lời mời, gửi `INVITE_RESULT` (`EXPIRED`) cho người mời và `INVITE_CLOSED` (`EXPIRED`) cho người được mời.
    - `handleInviteReply()`: gỡ lời mời bằng `removeInvitation()`; lời mời không còn (đã hủy, hết hạn) thì chỉ gửi `INVITE_CLOSED` (`EXPIRED`) cho người trả lời. Từ chối thì gửi `INVITE_RESULT` (`REJECTED`) cho người mời. Chấp nhận thì kiểm tra lại cả hai chưa bận, sinh đề bằng `generateBoard()`, tạo phòng qua `MatchManager.createMatch()`, chuyển hai người sang bận bằng `updateStatus()`, đóng mọi lời mời khác đi và đến hai người bằng `closeInvitations()`, gửi `MATCH_START` cho hai người rồi `broadcastOnlineList()`.
    - `closeInvitations(username, reason)`: gỡ mọi lời mời đi và đến một người (`OnlinePlayerManager.removeInvitationsOf()`); lời mời người đó gửi đi thì báo `INVITE_CLOSED` cho người được mời, lời mời gửi tới người đó thì báo `INVITE_RESULT` cho người mời, cùng lý do `BUSY` (vào ván) hoặc `OFFLINE` (đăng xuất, mất kết nối).
  - `MatchController` xử lý `MOVE`, `LEAVE_MATCH`, `REMATCH_REPLY`, hết giờ (`onTimeout()`), hết thời gian trả lời tái đấu (`onRematchTimeout()`), mất kết nối (`handleDisconnect()`) và kết thúc ván (`endMatch()`). Mọi hàm xử lý message đều khóa `MatchRoom` và bỏ qua message nếu người gửi không thuộc phòng `roomId`, hoặc ván đã kết thúc (với `MOVE`, `LEAVE_MATCH`).
    - `endMatch(room, reason)`: chỉ chạy một lần cho mỗi phòng (cờ `ended`), hủy hẹn giờ 10 phút, xác định kết quả theo bảng ở mục 8d, tính điểm, `MatchDAO.saveMatch()`, gửi `MATCH_END` rồi hẹn giờ 30 giây chờ trả lời tái đấu. Với `CLEARED`, `TIMEOUT`: gửi cho hai người, `rematch = true`. Với `QUIT`, `DISCONNECT`: chỉ gửi cho người còn lại, `rematch = false`; người thoát được coi như đã chọn Dừng (`stopRematch()`).
    - `handleRematchReply()`: ghi câu trả lời vào `rematchReplies` của phòng. Có người dừng thì gọi `stopRematch()`; cả hai đồng ý thì bỏ phòng cũ (`MatchManager.removeRoom()`), sinh đề mới, tạo phòng mới qua `MatchManager.createMatch()` và gửi `MATCH_START` cho cả hai, trạng thái vẫn là bận.
    - `stopRematch(room, stopper)`: chuyển người dừng về rỗi và bỏ khỏi phòng. Nếu ván kết thúc bình thường (`rematch = true`) và người kia còn ở dialog kết quả thì người kia cũng về rỗi, nhận `OPPONENT_LEFT` và rời phòng; nếu đối thủ đã thoát thì người còn lại vẫn bận cho đến khi bấm Về sảnh (gửi `REMATCH_REPLY` từ chối). Khi không còn ai trong phòng thì `MatchManager.removeRoom()`; cuối cùng `broadcastOnlineList()`.
    - `onRematchTimeout()`: hết 30 giây sau `MATCH_END` mà chưa đủ hai người đồng ý thì coi những người chưa trả lời là chọn Dừng.
  - Mất kết nối: khi đọc socket báo lỗi hoặc quá 15 giây không nhận được dòng nào, `ClientHandler.run()` gọi `MatchController.handleDisconnect()`, rồi `OnlinePlayerManager.removeOnlinePlayer()`, `ChallengeController.closeInvitations(username, "OFFLINE")` và `ChallengeController.broadcastOnlineList()`. `handleDisconnect()` tìm phòng theo `roomId` của `ClientHandler`: đang trong ván thì gọi `endMatch(room, "DISCONNECT")`, đang ở dialog kết quả thì coi như chọn Dừng (`stopRematch()`).
  - `MatchManager` giữ các ván đang diễn ra theo mã phòng `roomId`, là số tăng dần do `MatchManager` cấp (`nextRoomId`) lúc tạo phòng. Ván chỉ có `Match.id` của CSDL khi được lưu lúc kết thúc, nên các message trong ván dùng `roomId`. `createMatch()` cấp `roomId`, tạo `MatchRoom` và gán `roomId` cho `ClientHandler` của hai người; `getRoom()` tra phòng; `removeRoom()` bỏ phòng và xóa `roomId` khỏi các `ClientHandler` còn giữ nó.
  - `MatchRoom` (thuộc phần chơi ván) gồm `roomId`, `Match`, hai `ClientHandler`, **hai bản bàn chơi riêng** cho hai người (cùng đề ban đầu), thời điểm bắt đầu theo đồng hồ server, bộ hẹn giờ `timer` (10 phút của ván; sau khi ván kết thúc dùng lại để hẹn 30 giây chờ trả lời tái đấu), `rematchReplies` và cờ `ended`. Hai thuộc tính sau và cách dùng lại `timer` thuộc phần kết thúc ván và tái đấu.
  - `OnlinePlayerManager` giữ ánh xạ tên đăng nhập → `ClientHandler` của người đang online, trạng thái (rỗi, luyện tập, bận) của từng người, và các lời mời đang chờ `pendingInvites` (người mời → người được mời; mỗi người mời chỉ có một lời mời đi). Lớp này chỉ giữ dữ liệu, không gửi message; việc báo cho người chơi do `ChallengeController` làm.
- Các thao tác trên `MatchRoom` và trên `OnlinePlayerManager` được đồng bộ (`synchronized`) vì nhiều luồng `ClientHandler` và các bộ hẹn giờ cùng truy cập; ai hoàn thành trước được xác định theo thứ tự server xử lý message.

![Hình 3.15: Biểu đồ lớp thiết kế modul thách đấu và đấu đối kháng](vp/png/class-design-m4.png)

*Hình 3.15: Biểu đồ lớp thiết kế modul thách đấu và đấu đối kháng*

## 5. Thiết kế hoạt động cho bốn modul

Mỗi hành động trong biểu đồ hoạt động ứng với một phương thức đã thiết kế ở mục 4. Phần xử lý tại mỗi lớp là một khung (M1, M3) hoặc một làn (M2, M4); phương thức của lớp DAO, lớp quản lý và lớp xử lý được ghi ở ghi chú bên cạnh (M1, M3) hoặc ở một nút riêng nối hai chiều với hành động gọi nó (M2, M4). Trong kịch bản phiên bản 3 và biểu đồ tuần tự, các bước gán từng thuộc tính khi đóng gói lớp thực thể được lược bỏ cho gọn. Tên hành động là tên phương thức đã thiết kế; khung kích hoạt thể hiện phạm vi hoạt động của từng phương thức.

### a. Modul quản lý tài khoản

![Hình 3.16: Biểu đồ hoạt động modul quản lý tài khoản](uml/pha3/activity-m1.png)

*Hình 3.16: Biểu đồ hoạt động modul quản lý tài khoản*

Kịch bản phiên bản 3:

<!-- BEGIN v3-m1 -->
1. Tại giao diện `LoginActivity`, người chơi click nút Đăng ký.
2. Lớp `LoginActivity` gọi lớp `RegisterActivity`.
3. Lớp `RegisterActivity` hiển thị giao diện đăng ký cho người chơi.
4. Người chơi nhập tên đăng nhập, mật khẩu, nhập lại mật khẩu, tên hiển thị và click nút Đăng ký.
5. Lớp `RegisterActivity` kiểm tra không ô nào rỗng và mật khẩu nhập lại khớp, rồi gọi lớp `ServerConnection` yêu cầu gửi message `REGISTER`.
6. Lớp `ServerConnection` gửi message `REGISTER` qua TCP tới lớp `ClientHandler` trên server.
7. Lớp `ClientHandler` gọi hàm `handleRegister()` của lớp `LoginController`.
8. Lớp `LoginController` băm mật khẩu và gọi lớp `PlayerDAO` yêu cầu thêm người chơi mới.
9. Lớp `PlayerDAO` gọi hàm `addPlayer()`.
10. Hàm `addPlayer()` trả kết quả cho lớp `LoginController` (sai nếu tên đăng nhập đã tồn tại).
11. Lớp `LoginController` gọi lớp `ClientHandler` gửi message `REGISTER_RESULT`.
12. Lớp `ClientHandler` gửi message `REGISTER_RESULT` qua TCP tới lớp `ServerConnection`.
13. Lớp `ServerConnection` chuyển message cho lớp `RegisterActivity`.
14. Lớp `RegisterActivity` gọi lại lớp `LoginActivity`, kèm tên đăng nhập vừa đăng ký.
15. Lớp `LoginActivity` hiển thị thông báo đăng ký thành công, ô tên đăng nhập đã điền sẵn.
16. Người chơi nhập mật khẩu và click nút Đăng nhập.
17. Lớp `LoginActivity` gọi lớp `ServerConnection` yêu cầu gửi message `LOGIN`.
18. Lớp `ServerConnection` gửi message `LOGIN` qua TCP tới lớp `ClientHandler` trên server.
19. Lớp `ClientHandler` gọi hàm `handleLogin()` của lớp `LoginController`.
20. Lớp `LoginController` gọi lớp `PlayerDAO` yêu cầu kiểm tra đăng nhập.
21. Lớp `PlayerDAO` gọi hàm `checkLogin()`.
22. Hàm `checkLogin()` gọi lớp `Player` để đóng gói thông tin.
23. Lớp `Player` đóng gói thông tin thực thể.
24. Lớp `Player` trả kết quả cho hàm `checkLogin()`.
25. Hàm `checkLogin()` trả kết quả cho lớp `LoginController`.
26. Lớp `LoginController` gọi lớp `OnlinePlayerManager` yêu cầu thêm người chơi vào danh sách online.
27. Lớp `OnlinePlayerManager` gọi hàm `addOnlinePlayer()`.
28. Hàm `addOnlinePlayer()` trả kết quả cho lớp `LoginController` (sai nếu tài khoản đang online ở máy khác).
29. Lớp `LoginController` gọi lớp `ClientHandler` gửi message `LOGIN_RESULT`.
30. Lớp `ClientHandler` gửi message `LOGIN_RESULT` qua TCP tới lớp `ServerConnection`.
31. Lớp `ServerConnection` chuyển message cho lớp `LoginActivity`.
32. Lớp `LoginActivity` lưu người chơi vào `ClientSession` và gọi lớp `MainActivity`.
33. Lớp `MainActivity` hiển thị giao diện chính cho người chơi.
34. Người chơi click nút Đăng xuất.
35. Lớp `MainActivity` gọi lớp `ServerConnection` yêu cầu gửi message `LOGOUT`.
36. Lớp `MainActivity` xóa người chơi khỏi `ClientSession` và gọi lại lớp `LoginActivity`.
37. Lớp `LoginActivity` hiển thị giao diện đăng nhập cho người chơi.
38. Lớp `ServerConnection` gửi message `LOGOUT` qua TCP tới lớp `ClientHandler` trên server.
39. Lớp `ClientHandler` gọi hàm `handleLogout()` của lớp `LoginController`.
40. Lớp `LoginController` gọi lớp `OnlinePlayerManager` yêu cầu xóa người chơi khỏi danh sách online.
41. Lớp `OnlinePlayerManager` gọi hàm `removeOnlinePlayer()`.
42. Hàm `removeOnlinePlayer()` trả kết quả cho lớp `LoginController`.
43. Lớp `LoginController` gọi hàm `broadcastOnlineList()` của lớp `ChallengeController` để gửi lại danh sách online cho những người chơi khác.
<!-- END v3-m1 -->

![Hình 3.17: Biểu đồ tuần tự modul quản lý tài khoản](uml/pha3/seq-m1.png)

*Hình 3.17: Biểu đồ tuần tự modul quản lý tài khoản*

### b. Modul xem bảng xếp hạng

![Hình 3.18: Biểu đồ hoạt động modul xem bảng xếp hạng](vp/png/activity-m2.png)

*Hình 3.18: Biểu đồ hoạt động modul xem bảng xếp hạng*

Kịch bản phiên bản 3:

<!-- BEGIN v3-m2 -->
1. Tại giao diện `MainActivity`, người chơi click nút Xem bảng xếp hạng.
2. Lớp `MainActivity` gọi lớp `RankingActivity`.
3. Lớp `RankingActivity` gọi lớp `ServerConnection` yêu cầu gửi message `GET_RANKING` với trang 1.
4. Lớp `ServerConnection` gửi message `GET_RANKING` qua TCP tới lớp `ClientHandler` trên server.
5. Lớp `ClientHandler` gọi hàm `handleGetRanking()` của lớp `RankingController`.
6. Lớp `RankingController` gọi lớp `PlayerRankingDAO` yêu cầu lấy bảng xếp hạng.
7. Lớp `PlayerRankingDAO` gọi hàm `getRanking()`.
8. Hàm `getRanking()` gọi lớp `PlayerRanking` để đóng gói thông tin.
9. Lớp `PlayerRanking` đóng gói thông tin thực thể.
10. Lớp `PlayerRanking` trả kết quả cho hàm `getRanking()`.
11. Hàm `getRanking()` trả danh sách đã sắp xếp cho lớp `RankingController`.
12. Lớp `RankingController` đánh số hạng, lấy các dòng của trang 1 và gọi lớp `ClientHandler` gửi message `RANKING`.
13. Lớp `ClientHandler` gửi message `RANKING` qua TCP tới lớp `ServerConnection`.
14. Lớp `ServerConnection` chuyển message cho lớp `RankingActivity`.
15. Lớp `RankingActivity` hiển thị trang 1 của bảng xếp hạng và số trang cho người chơi.
16. Người chơi click nút Vị trí của tôi.
17. Lớp `RankingActivity` gọi lớp `ServerConnection` yêu cầu gửi message `GET_RANKING` với `mine = true`.
18. Lớp `ServerConnection` gửi message `GET_RANKING` qua TCP tới lớp `ClientHandler`.
19. Lớp `ClientHandler` gọi hàm `handleGetRanking()` của lớp `RankingController`.
20. Lớp `RankingController` gọi lớp `PlayerRankingDAO` yêu cầu lấy bảng xếp hạng.
21. Lớp `PlayerRankingDAO` gọi hàm `getRanking()`.
22. Hàm `getRanking()` gọi lớp `PlayerRanking` để đóng gói thông tin.
23. Lớp `PlayerRanking` đóng gói thông tin thực thể.
24. Lớp `PlayerRanking` trả kết quả cho hàm `getRanking()`.
25. Hàm `getRanking()` trả danh sách đã sắp xếp cho lớp `RankingController`.
26. Lớp `RankingController` tìm dòng của người chơi đang đăng nhập trên `ClientHandler`, lấy các dòng của trang chứa dòng đó và gọi lớp `ClientHandler` gửi message `RANKING` kèm thứ hạng `me`.
27. Lớp `ClientHandler` gửi message `RANKING` qua TCP tới lớp `ServerConnection`.
28. Lớp `ServerConnection` chuyển message cho lớp `RankingActivity`.
29. Lớp `RankingActivity` hiển thị trang chứa người chơi, tô màu dòng có tên đăng nhập của người chơi.
30. Người chơi click nút Quay lại.
31. Lớp `RankingActivity` gọi lại lớp `MainActivity`.
32. Lớp `MainActivity` hiển thị giao diện chính cho người chơi.
<!-- END v3-m2 -->

![Hình 3.19: Biểu đồ tuần tự modul xem bảng xếp hạng](vp/png/seq-m2.png)

*Hình 3.19: Biểu đồ tuần tự modul xem bảng xếp hạng*

### c. Modul chơi với máy

![Hình 3.20: Biểu đồ hoạt động modul chơi với máy](uml/pha3/activity-m3.png)

*Hình 3.20: Biểu đồ hoạt động modul chơi với máy*

Kịch bản phiên bản 3:

<!-- BEGIN v3-m3 -->
1. Tại giao diện chính, người chơi click nút Chơi với máy.
2. Lớp `MainActivity` gọi lớp `DifficultyActivity`.
3. Lớp `DifficultyActivity` hiển thị các lựa chọn độ khó cho người chơi.
4. Người chơi chọn độ khó Trung bình và click nút Bắt đầu.
5. Lớp `DifficultyActivity` gọi lớp `BotGameActivity`, kèm độ khó đã chọn.
6. Lớp `BotGameActivity` gọi lớp `BoardGenerator` yêu cầu sinh đề bài 10x10.
7. Lớp `BoardGenerator` gọi hàm `generateBoard()`.
8. Hàm `generateBoard()` gọi lớp `Board` để đóng gói đề bài.
9. Lớp `Board` đóng gói thông tin thực thể.
10. Lớp `Board` trả kết quả cho hàm `generateBoard()`.
11. Hàm `generateBoard()` trả đề bài cho lớp `BotGameActivity`.
12. Lớp `BotGameActivity` gọi lớp `Board` yêu cầu tạo bản sao đề bài cho máy.
13. Lớp `Board` gọi hàm `copy()`.
14. Hàm `copy()` trả bàn của máy cho lớp `BotGameActivity`.
15. Lớp `BotGameActivity` gọi lớp `Bot` yêu cầu khởi tạo máy theo độ khó.
16. Lớp `Bot` gọi hàm khởi tạo `Bot()`.
17. Lớp `Bot` trả đối tượng máy cho lớp `BotGameActivity`.
18. Lớp `BotGameActivity` gọi lớp `ServerConnection` yêu cầu gửi message `UPDATE_STATUS` với trạng thái luyện tập.
19. Lớp `BotGameActivity` gọi hàm `setBoard()` của lớp `BoardView` và bắt đầu đồng hồ.
20. Lớp `BoardView` hiển thị bàn chơi, đồng hồ và số ô còn lại cho người chơi.
21. Người chơi click lần lượt hai ô cùng hình.
22. Lớp `BoardView` gọi hàm `onCellClick()` của lớp `BotGameActivity`.
23. Lớp `BotGameActivity` gọi lớp `PathFinder` yêu cầu kiểm tra đường nối giữa hai ô.
24. Lớp `PathFinder` gọi hàm `checkPath()`.
25. Hàm `checkPath()` trả đường nối cho lớp `BotGameActivity`.
26. Lớp `BotGameActivity` gọi lớp `Board` yêu cầu xóa hai ô.
27. Lớp `Board` gọi hàm `removePair()`.
28. Hàm `removePair()` trả kết quả cho lớp `BotGameActivity`.
29. Lớp `BotGameActivity` gọi lớp `Board` yêu cầu kiểm tra bàn đã hết ô chưa.
30. Lớp `Board` gọi hàm `isCleared()`.
31. Hàm `isCleared()` trả kết quả cho lớp `BotGameActivity`.
32. Lớp `BotGameActivity` gọi lớp `PathFinder` yêu cầu tìm một cặp còn nối được để kiểm tra bế tắc.
33. Lớp `PathFinder` gọi hàm `findAvailablePair()`.
34. Hàm `findAvailablePair()` trả cặp ô cho lớp `BotGameActivity` (nếu trả về rỗng thì lớp `BotGameActivity` gọi `BoardGenerator.shuffle()` để xáo bàn).
35. Lớp `BotGameActivity` gọi hàm `showPath()` của lớp `BoardView`.
36. Lớp `BoardView` vẽ đường nối, hiển thị bàn chơi mới và số ô còn lại cho người chơi.
37. Sau mỗi khoảng `getDelayMs()`, lớp `BotGameActivity` gọi lớp `Bot` yêu cầu nước đi của máy.
38. Lớp `Bot` gọi hàm `nextMove()`.
39. Hàm `nextMove()` gọi lớp `PathFinder` yêu cầu tìm một cặp nối được trên bàn của máy.
40. Lớp `PathFinder` gọi hàm `findAvailablePair()`.
41. Hàm `findAvailablePair()` trả cặp ô cho hàm `nextMove()`.
42. Hàm `nextMove()` trả nước đi cho lớp `BotGameActivity`.
43. Lớp `BotGameActivity` gọi lớp `Board` của máy yêu cầu xóa cặp ô đó.
44. Lớp `Board` gọi hàm `removePair()`.
45. Hàm `removePair()` trả kết quả cho lớp `BotGameActivity`.
46. Lớp `BotGameActivity` vẽ nước đi của máy lên bàn thu nhỏ của máy và hiển thị số ô còn lại của máy cho người chơi.
(Lặp lại các bước 21 đến 46 cho đến khi một bên xóa hết ô hoặc hết giờ.)
47. Lớp `BotGameActivity` dừng đồng hồ, xác định kết quả và gọi lớp `ServerConnection` yêu cầu gửi message `UPDATE_STATUS` với trạng thái rỗi.
48. Lớp `BotGameActivity` gọi lớp `BotResultDialog`.
49. Lớp `BotResultDialog` hiển thị kết quả ván cho người chơi.
50. Người chơi click nút OK.
51. Lớp `BotResultDialog` gọi lại lớp `MainActivity`.
52. Lớp `MainActivity` hiển thị giao diện chính cho người chơi.
<!-- END v3-m3 -->

![Hình 3.21: Biểu đồ tuần tự modul chơi với máy](uml/pha3/seq-m3.png)

*Hình 3.21: Biểu đồ tuần tự modul chơi với máy*

### d. Modul thách đấu và đấu đối kháng

![Hình 3.22: Biểu đồ hoạt động modul thách đấu và đấu đối kháng](vp/png/activity-m4.png)

*Hình 3.22: Biểu đồ hoạt động modul thách đấu và đấu đối kháng*

Kịch bản phiên bản 3 (người chơi A thách đấu người chơi B; mỗi lớp giao diện và `ServerConnection` có một bản trên mỗi máy, mỗi người chơi có một `ClientHandler` riêng trên server):

<!-- BEGIN v3-m4 -->
1. Tại giao diện chính, người chơi A (đã đăng nhập) click nút Đấu đối kháng.
2. Lớp `MainActivity` gọi lớp `LobbyActivity`.
3. Lớp `LobbyActivity` gọi lớp `ServerConnection` yêu cầu gửi message `GET_ONLINE_LIST`.
4. Lớp `ServerConnection` gửi message `GET_ONLINE_LIST` qua TCP tới lớp `ClientHandler` của A trên server.
5. Lớp `ClientHandler` gọi hàm `handleGetOnlineList()` của lớp `ChallengeController`.
6. Lớp `ChallengeController` gọi lớp `OnlinePlayerManager` yêu cầu lấy danh sách người chơi online.
7. Lớp `OnlinePlayerManager` gọi hàm `getOnlinePlayers()`.
8. Hàm `getOnlinePlayers()` trả danh sách cho lớp `ChallengeController`.
9. Lớp `ChallengeController` gọi lớp `PlayerRankingDAO` yêu cầu lấy tổng điểm của các người chơi trong danh sách.
10. Lớp `PlayerRankingDAO` gọi hàm `getTotalPoints()`.
11. Hàm `getTotalPoints()` gọi lớp `PlayerRanking` để đóng gói thông tin.
12. Lớp `PlayerRanking` đóng gói thông tin thực thể.
13. Lớp `PlayerRanking` trả kết quả cho hàm `getTotalPoints()`.
14. Hàm `getTotalPoints()` trả kết quả cho lớp `ChallengeController`.
15. Lớp `ChallengeController` gọi lớp `ClientHandler` của A gửi message `ONLINE_LIST`.
16. Lớp `ClientHandler` gửi message `ONLINE_LIST` qua TCP tới lớp `ServerConnection` trên máy A.
17. Lớp `ServerConnection` chuyển message cho lớp `LobbyActivity`.
18. Lớp `LobbyActivity` hiển thị danh sách người chơi online cho A.
19. A click vào tên người chơi B (không bận: rỗi hoặc luyện tập).
20. Lớp `LobbyActivity` gọi lớp `ChallengeConfirmDialog`.
21. Lớp `ChallengeConfirmDialog` hiển thị dialog xác nhận thách đấu B cho A.
22. A click nút Xác nhận.
23. Lớp `ChallengeConfirmDialog` gọi lớp `ServerConnection` yêu cầu gửi message `INVITE`.
24. Lớp `ChallengeConfirmDialog` chuyển thành popup chờ, hiển thị "Đang chờ B trả lời..." đếm ngược 30 giây và nút Hủy lời mời cho A.
25. Lớp `ServerConnection` gửi message `INVITE` qua TCP tới lớp `ClientHandler` của A trên server.
26. Lớp `ClientHandler` gọi hàm `handleInvite()` của lớp `ChallengeController`.
27. Lớp `ChallengeController` gọi lớp `OnlinePlayerManager` yêu cầu ghi nhận lời mời từ A tới B.
28. Lớp `OnlinePlayerManager` gọi hàm `sendInvitation()`: B đang online và không bận nên ghi lời mời A → B.
29. Hàm `sendInvitation()` trả kết quả cho lớp `ChallengeController` (không có lý do từ chối).
30. Lớp `ChallengeController` hẹn giờ 30 giây cho lời mời và gọi lớp `ClientHandler` của B gửi message `INVITE_RECEIVED`.
31. Lớp `ClientHandler` của B gửi message `INVITE_RECEIVED` qua TCP tới lớp `ServerConnection` trên máy B.
32. Lớp `ServerConnection` chuyển message cho lớp `LobbyActivity` trên máy B (B đang ở màn hình đấu đối kháng); hàm `onMessage()` của lớp cha `BaseActivity` đưa lời mời vào hàng đợi.
33. Lớp `LobbyActivity` gọi lớp `InvitationDialog` (hàm `showNextInvitation()` của lớp cha).
34. Lớp `InvitationDialog` hiển thị popup lời mời, đếm ngược 30 giây, cho B.
35. B click nút Accept.
36. Lớp `InvitationDialog` gọi lớp `ServerConnection` yêu cầu gửi message `INVITE_REPLY` chấp nhận.
37. Lớp `ServerConnection` gửi message `INVITE_REPLY` qua TCP tới lớp `ClientHandler` của B.
38. Lớp `ClientHandler` gọi hàm `handleInviteReply()` của lớp `ChallengeController`.
39. Lớp `ChallengeController` gọi lớp `OnlinePlayerManager` yêu cầu gỡ lời mời A → B.
40. Lớp `OnlinePlayerManager` gọi hàm `removeInvitation()`.
41. Hàm `removeInvitation()` trả kết quả cho lớp `ChallengeController`: lời mời còn hiệu lực.
42. Lớp `ChallengeController` gọi lớp `BoardGenerator` yêu cầu sinh đề bài 10x10.
43. Lớp `BoardGenerator` gọi hàm `generateBoard()`.
44. Hàm `generateBoard()` gọi lớp `Board` để đóng gói đề bài.
45. Lớp `Board` đóng gói thông tin thực thể.
46. Lớp `Board` trả kết quả cho hàm `generateBoard()`.
47. Hàm `generateBoard()` trả đề bài cho lớp `ChallengeController`.
48. Lớp `ChallengeController` gọi lớp `MatchManager` yêu cầu tạo ván đấu cho A và B với đề bài vừa sinh.
49. Lớp `MatchManager` gọi hàm `createMatch()`: cấp `roomId` mới, tạo phòng chơi giữ hai bản sao đề bài và gán `roomId` cho `ClientHandler` của A và B.
50. Hàm `createMatch()` gọi lớp `Match` để đóng gói ván đấu.
51. Lớp `Match` đóng gói thông tin thực thể.
52. Lớp `Match` trả kết quả cho hàm `createMatch()`.
53. Hàm `createMatch()` trả phòng chơi cho lớp `ChallengeController`.
54. Lớp `ChallengeController` gọi lớp `OnlinePlayerManager` yêu cầu chuyển A và B sang bận.
55. Lớp `OnlinePlayerManager` gọi hàm `updateStatus()`.
56. Hàm `updateStatus()` trả kết quả cho lớp `ChallengeController`.
57. Lớp `ChallengeController` gọi hàm `closeInvitations()` cho A và cho B: gỡ các lời mời khác đi và đến hai người, báo cho những người liên quan.
58. Lớp `ChallengeController` gọi lớp `ClientHandler` của A và của B gửi message `MATCH_START` kèm `roomId`.
59. Lớp `ChallengeController` gọi hàm `broadcastOnlineList()` để những người chơi khác thấy A và B đã bận.
60. Hàm `broadcastOnlineList()` gọi lớp `OnlinePlayerManager` yêu cầu lấy danh sách người chơi online.
61. Lớp `OnlinePlayerManager` gọi hàm `getOnlinePlayers()`.
62. Hàm `getOnlinePlayers()` trả danh sách cho hàm `broadcastOnlineList()`.
63. Hàm `broadcastOnlineList()` gọi lớp `PlayerRankingDAO` yêu cầu lấy tổng điểm của các người chơi trong danh sách.
64. Lớp `PlayerRankingDAO` gọi hàm `getTotalPoints()`.
65. Hàm `getTotalPoints()` gọi lớp `PlayerRanking` để đóng gói thông tin.
66. Lớp `PlayerRanking` đóng gói thông tin thực thể.
67. Lớp `PlayerRanking` trả kết quả cho hàm `getTotalPoints()`.
68. Hàm `getTotalPoints()` trả kết quả cho hàm `broadcastOnlineList()`.
69. Hàm `broadcastOnlineList()` gọi lớp `ClientHandler` của mọi người chơi rỗi gửi message `ONLINE_LIST`.
70. Lớp `ClientHandler` gửi message `MATCH_START` qua TCP tới lớp `ServerConnection` trên cả hai máy (và `ONLINE_LIST` tới máy những người chơi rỗi khác).
71. Lớp `ServerConnection` chuyển message `MATCH_START` cho lớp `LobbyActivity` trên cả hai máy.
72. Lớp `LobbyActivity` (hàm `onMessage()` của lớp cha `BaseActivity`) đóng popup chờ trên máy A, gọi lớp `MatchActivity`, kèm đề bài, `roomId` và thông tin đối thủ.
73. Lớp `MatchActivity` hiển thị bàn chơi, đồng hồ và số ô còn lại của đối thủ cho A và B.
74. A click lần lượt hai ô cùng hình.
75. Lớp `MatchActivity` gọi lớp `ServerConnection` yêu cầu gửi message `MOVE`.
76. Lớp `ServerConnection` gửi message `MOVE` qua TCP tới lớp `ClientHandler` của A.
77. Lớp `ClientHandler` gọi hàm `handleMove()` của lớp `MatchController`.
78. Lớp `MatchController` gọi lớp `MatchManager` yêu cầu lấy phòng chơi của ván.
79. Lớp `MatchManager` gọi hàm `getRoom()`.
80. Hàm `getRoom()` trả phòng chơi cho lớp `MatchController`, từ đó lấy bàn của A.
81. Lớp `MatchController` gọi lớp `PathFinder` yêu cầu kiểm tra đường nối trên bàn của A.
82. Lớp `PathFinder` gọi hàm `checkPath()`.
83. Hàm `checkPath()` trả đường nối cho lớp `MatchController`.
84. Lớp `MatchController` gọi lớp `Board` của A yêu cầu xóa hai ô.
85. Lớp `Board` gọi hàm `removePair()`.
86. Hàm `removePair()` trả kết quả cho lớp `MatchController`.
87. Lớp `MatchController` gọi lớp `Board` yêu cầu kiểm tra bàn của A đã hết ô chưa.
88. Lớp `Board` gọi hàm `isCleared()`.
89. Hàm `isCleared()` trả kết quả cho lớp `MatchController`.
90. Lớp `MatchController` gọi lớp `PathFinder` yêu cầu tìm một cặp còn nối được trên bàn của A để kiểm tra bế tắc.
91. Lớp `PathFinder` gọi hàm `findAvailablePair()`.
92. Hàm `findAvailablePair()` trả cặp ô cho lớp `MatchController` (nếu trả về rỗng thì lớp `MatchController` gọi `BoardGenerator.shuffle()` xáo riêng bàn của A và gửi `BOARD_SHUFFLED` cho A).
93. Lớp `MatchController` gọi lớp `ClientHandler` của A gửi message `MOVE_RESULT` và lớp `ClientHandler` của B gửi message `OPPONENT_PROGRESS`.
94. Lớp `ClientHandler` gửi các message qua TCP tới lớp `ServerConnection` trên hai máy.
95. Lớp `ServerConnection` chuyển message cho lớp `MatchActivity` trên hai máy.
96. Lớp `MatchActivity` vẽ đường nối và bàn chơi mới cho A; trên máy B, vẽ nước đi của A lên bàn thu nhỏ của đối thủ và cập nhật số ô còn lại của A.
(Lặp lại các bước 74 đến 96 cho đến khi bước 89 trả về bàn của A đã hết ô; B chơi song song theo cùng các bước trên máy của mình.)
97. Khi bàn của A đã hết ô, lớp `MatchController` gọi hàm `endMatch()`: đánh dấu phòng đã kết thúc, hủy hẹn giờ 10 phút, tính kết quả và điểm của hai người.
98. Hàm `endMatch()` gọi lớp `MatchDAO` yêu cầu lưu ván đấu cùng hai kết quả.
99. Lớp `MatchDAO` gọi hàm `saveMatch()`.
100. Hàm `saveMatch()` trả kết quả cho hàm `endMatch()`.
101. Hàm `endMatch()` gọi lớp `ClientHandler` của A và của B gửi message `MATCH_END` (`rematch = true`) và hẹn giờ 30 giây chờ trả lời tái đấu.
102. Lớp `ClientHandler` gửi message `MATCH_END` qua TCP tới lớp `ServerConnection` trên hai máy.
103. Lớp `ServerConnection` chuyển message cho lớp `MatchActivity` trên hai máy.
104. Lớp `MatchActivity` gọi lớp `ResultDialog`.
105. Lớp `ResultDialog` hiển thị kết quả, điểm nhận được và câu hỏi có muốn tái đấu không, đếm ngược 30 giây, cho A và B.
106. A click nút Dừng.
107. Lớp `ResultDialog` gọi lớp `ServerConnection` yêu cầu gửi message `REMATCH_REPLY` từ chối.
108. Lớp `ResultDialog` gọi lớp `LobbyActivity` trên máy A.
109. Lớp `ServerConnection` gửi message `REMATCH_REPLY` qua TCP tới lớp `ClientHandler` của A.
110. Lớp `ClientHandler` gọi hàm `handleRematchReply()` của lớp `MatchController`.
111. Lớp `MatchController` gọi lớp `MatchManager` yêu cầu lấy phòng chơi theo `roomId`.
112. Lớp `MatchManager` gọi hàm `getRoom()`.
113. Hàm `getRoom()` trả phòng chơi cho lớp `MatchController`; lớp `MatchController` ghi nhận A dừng vào `rematchReplies` của phòng.
114. Lớp `MatchController` gọi hàm `stopRematch()` vì A chọn dừng.
115. Hàm `stopRematch()` gọi lớp `OnlinePlayerManager` yêu cầu chuyển A và B về rỗi.
116. Lớp `OnlinePlayerManager` gọi hàm `updateStatus()`.
117. Hàm `updateStatus()` trả kết quả cho hàm `stopRematch()`.
118. Hàm `stopRematch()` gọi lớp `ClientHandler` của B gửi message `OPPONENT_LEFT`.
119. Hàm `stopRematch()` gọi lớp `MatchManager` yêu cầu bỏ phòng chơi vì không còn ai trong phòng.
120. Lớp `MatchManager` gọi hàm `removeRoom()`.
121. Hàm `removeRoom()` trả kết quả cho hàm `stopRematch()`.
122. Hàm `stopRematch()` gọi lớp `ChallengeController` yêu cầu gửi lại danh sách người chơi online.
123. Lớp `ChallengeController` gọi hàm `broadcastOnlineList()`.
124. Hàm `broadcastOnlineList()` gọi lớp `OnlinePlayerManager` yêu cầu lấy danh sách người chơi online.
125. Lớp `OnlinePlayerManager` gọi hàm `getOnlinePlayers()`.
126. Hàm `getOnlinePlayers()` trả danh sách cho hàm `broadcastOnlineList()`.
127. Hàm `broadcastOnlineList()` gọi lớp `PlayerRankingDAO` yêu cầu lấy tổng điểm của các người chơi trong danh sách.
128. Lớp `PlayerRankingDAO` gọi hàm `getTotalPoints()`.
129. Hàm `getTotalPoints()` gọi lớp `PlayerRanking` để đóng gói thông tin.
130. Lớp `PlayerRanking` đóng gói thông tin thực thể.
131. Lớp `PlayerRanking` trả kết quả cho hàm `getTotalPoints()`.
132. Hàm `getTotalPoints()` trả kết quả cho hàm `broadcastOnlineList()`.
133. Hàm `broadcastOnlineList()` gọi lớp `ClientHandler` của mọi người chơi rỗi gửi message `ONLINE_LIST`.
134. Lớp `ClientHandler` gửi các message qua TCP tới lớp `ServerConnection` trên hai máy.
135. Lớp `ServerConnection` chuyển message `OPPONENT_LEFT` cho lớp `ResultDialog` trên máy B.
136. Lớp `ResultDialog` gọi lớp `LobbyActivity` trên máy B.
137. Lớp `ServerConnection` chuyển message `ONLINE_LIST` cho lớp `LobbyActivity` trên hai máy.
138. Lớp `LobbyActivity` hiển thị danh sách người chơi online đã cập nhật điểm cho A và B, kèm thông báo "A đã dừng chơi" cho B.
<!-- END v3-m4 -->

Biểu đồ tuần tự vẽ đủ các bước 1–138 ở trên trong một biểu đồ, kể cả vòng lặp chơi ván (bước 74–96, thuộc phần của Dev 1); vì dài nên ảnh được cắt thành ba phần, mỗi phần lặp lại hàng đầu lifeline (Hình 3.23, 3.24, 3.25). Nhánh tái đấu và nhánh thoát ván ở cuối mục.

![Hình 3.23: Biểu đồ tuần tự modul thách đấu, phần 1/3](vp/png/seq-m4-full-1.png)

*Hình 3.23: Biểu đồ tuần tự modul thách đấu và đấu đối kháng (phần 1/3)*

![Hình 3.24: Biểu đồ tuần tự modul thách đấu, phần 2/3](vp/png/seq-m4-full-2.png)

*Hình 3.24: Biểu đồ tuần tự modul thách đấu và đấu đối kháng (phần 2/3)*

![Hình 3.25: Biểu đồ tuần tự modul thách đấu, phần 3/3](vp/png/seq-m4-full-3.png)

*Hình 3.25: Biểu đồ tuần tự modul thách đấu và đấu đối kháng (phần 3/3)*

Ngoại lệ 13a của kịch bản v1 (cả hai người chơi chọn tái đấu) có kịch bản phiên bản 3 riêng, thay cho phần 3 từ bước 106 (sau khi `ResultDialog` hiện lên ở bước 105). Kịch bản đánh số lại từ 1:

<!-- BEGIN v3-m4-rematch -->
1. Tại dialog kết quả, A click nút Tái đấu.
2. Lớp `ResultDialog` gọi lớp `ServerConnection` yêu cầu gửi message `REMATCH_REPLY` đồng ý.
3. Lớp `ResultDialog` hiển thị thông báo đang chờ đối thủ trả lời cho A; nút Dừng vẫn bấm được.
4. Lớp `ServerConnection` gửi message `REMATCH_REPLY` qua TCP tới lớp `ClientHandler` của A trên server.
5. Lớp `ClientHandler` gọi hàm `handleRematchReply()` của lớp `MatchController`.
6. Lớp `MatchController` gọi lớp `MatchManager` yêu cầu lấy phòng chơi của ván vừa kết thúc theo `roomId`.
7. Lớp `MatchManager` gọi hàm `getRoom()`.
8. Hàm `getRoom()` trả phòng chơi cho lớp `MatchController`; lớp `MatchController` ghi nhận A đồng ý vào `rematchReplies` của phòng và chờ vì B chưa trả lời.
9. Trên máy của mình, B click nút Tái đấu.
10. Lớp `ResultDialog` trên máy B gọi lớp `ServerConnection` yêu cầu gửi message `REMATCH_REPLY` đồng ý.
11. Lớp `ServerConnection` gửi message `REMATCH_REPLY` qua TCP tới lớp `ClientHandler` của B.
12. Lớp `ClientHandler` gọi hàm `handleRematchReply()` của lớp `MatchController`.
13. Lớp `MatchController` gọi lớp `MatchManager` yêu cầu lấy phòng chơi của ván vừa kết thúc theo `roomId`.
14. Lớp `MatchManager` gọi hàm `getRoom()`.
15. Hàm `getRoom()` trả phòng chơi cho lớp `MatchController`; lớp `MatchController` ghi nhận B đồng ý, thấy cả hai đã đồng ý nên hủy hẹn giờ chờ trả lời và tạo ván mới.
16. Lớp `MatchController` gọi lớp `MatchManager` yêu cầu bỏ phòng của ván cũ.
17. Lớp `MatchManager` gọi hàm `removeRoom()`.
18. Hàm `removeRoom()` trả kết quả cho lớp `MatchController`.
19. Lớp `MatchController` gọi lớp `BoardGenerator` yêu cầu sinh đề bài 10x10 mới.
20. Lớp `BoardGenerator` gọi hàm `generateBoard()`.
21. Hàm `generateBoard()` gọi lớp `Board` để đóng gói đề bài.
22. Lớp `Board` đóng gói thông tin thực thể.
23. Lớp `Board` trả kết quả cho hàm `generateBoard()`.
24. Hàm `generateBoard()` trả đề bài cho lớp `MatchController`.
25. Lớp `MatchController` gọi lớp `MatchManager` yêu cầu tạo ván mới cho A và B với đề bài vừa sinh.
26. Lớp `MatchManager` gọi hàm `createMatch()`: cấp `roomId` mới, tạo phòng chơi mới giữ hai bản sao đề bài và gán `roomId` cho `ClientHandler` của A và B.
27. Hàm `createMatch()` gọi lớp `Match` để đóng gói ván đấu.
28. Lớp `Match` đóng gói thông tin thực thể.
29. Lớp `Match` trả kết quả cho hàm `createMatch()`.
30. Hàm `createMatch()` trả phòng chơi mới cho lớp `MatchController`.
31. Lớp `MatchController` gọi lớp `ClientHandler` của A và của B gửi message `MATCH_START` kèm `roomId` mới; trạng thái hai người vẫn là bận.
32. Lớp `ClientHandler` gửi message `MATCH_START` qua TCP tới lớp `ServerConnection` trên cả hai máy.
33. Lớp `ServerConnection` chuyển message cho lớp `ResultDialog` trên cả hai máy.
34. Lớp `ResultDialog` đóng và gọi lớp `MatchActivity`, kèm đề bài mới, `roomId` và thông tin đối thủ.
35. Lớp `MatchActivity` hiển thị bàn chơi mới, đồng hồ và số ô còn lại của đối thủ cho A và B.
<!-- END v3-m4-rematch -->

Biểu đồ tuần tự của nhánh tái đấu như Hình 3.26.

![Hình 3.26: Biểu đồ tuần tự modul thách đấu, nhánh tái đấu](vp/png/seq-m4-4.png)

*Hình 3.26: Biểu đồ tuần tự modul thách đấu và đấu đối kháng, nhánh tái đấu*

Ngoại lệ 9a của kịch bản v1 (A bấm Thoát giữa ván) có kịch bản phiên bản 3 riêng, thay cho vòng lặp chơi ván và phần 3: người thoát được coi như đã chọn dừng nên không được hỏi tái đấu, còn người thắng chỉ có nút Về sảnh. Kịch bản đánh số lại từ 1:

<!-- BEGIN v3-m4-quit -->
1. Trong lúc ván đang diễn ra, A click nút Thoát.
2. Lớp `MatchActivity` gọi lớp `ServerConnection` yêu cầu gửi message `LEAVE_MATCH` kèm `roomId`.
3. Lớp `MatchActivity` đóng màn chơi và gọi lớp `LobbyActivity`, kèm thông báo bị xử thua.
4. Lớp `LobbyActivity` hiển thị thông báo "Bạn đã thoát nên bị xử thua (0 điểm)" cho A.
5. Lớp `ServerConnection` gửi message `LEAVE_MATCH` qua TCP tới lớp `ClientHandler` của A trên server.
6. Lớp `ClientHandler` gọi hàm `handleLeaveMatch()` của lớp `MatchController`.
7. Lớp `MatchController` gọi lớp `MatchManager` yêu cầu lấy phòng chơi theo `roomId`.
8. Lớp `MatchManager` gọi hàm `getRoom()`.
9. Hàm `getRoom()` trả phòng chơi cho lớp `MatchController`; A thuộc phòng và ván chưa kết thúc.
10. Lớp `MatchController` gọi hàm `endMatch()` với lý do `QUIT`: đánh dấu phòng đã kết thúc, hủy hẹn giờ 10 phút, xử A thua, B thắng không có thời gian hoàn thành, tính điểm.
11. Hàm `endMatch()` gọi lớp `MatchDAO` yêu cầu lưu ván đấu cùng hai kết quả.
12. Lớp `MatchDAO` gọi hàm `saveMatch()`.
13. Hàm `saveMatch()` trả kết quả cho hàm `endMatch()`.
14. Hàm `endMatch()` gọi lớp `ClientHandler` của B gửi message `MATCH_END` (lý do `QUIT`, `rematch = false`) và hẹn giờ 30 giây.
15. Hàm `endMatch()` gọi hàm `stopRematch()` vì A thoát được coi như đã chọn dừng.
16. Hàm `stopRematch()` gọi lớp `OnlinePlayerManager` yêu cầu chuyển A về rỗi; A được bỏ khỏi phòng, B vẫn bận vì còn ở dialog kết quả.
17. Lớp `OnlinePlayerManager` gọi hàm `updateStatus()`.
18. Hàm `updateStatus()` trả kết quả cho hàm `stopRematch()`.
19. Hàm `stopRematch()` gọi lớp `ChallengeController` yêu cầu gửi lại danh sách người chơi online.
20. Lớp `ChallengeController` gọi hàm `broadcastOnlineList()`.
21. Hàm `broadcastOnlineList()` gọi lớp `OnlinePlayerManager` yêu cầu lấy danh sách người chơi online.
22. Lớp `OnlinePlayerManager` gọi hàm `getOnlinePlayers()`.
23. Hàm `getOnlinePlayers()` trả danh sách cho hàm `broadcastOnlineList()`.
24. Hàm `broadcastOnlineList()` gọi lớp `PlayerRankingDAO` yêu cầu lấy tổng điểm của các người chơi trong danh sách.
25. Lớp `PlayerRankingDAO` gọi hàm `getTotalPoints()`.
26. Hàm `getTotalPoints()` gọi lớp `PlayerRanking` để đóng gói thông tin.
27. Lớp `PlayerRanking` đóng gói thông tin thực thể.
28. Lớp `PlayerRanking` trả kết quả cho hàm `getTotalPoints()`.
29. Hàm `getTotalPoints()` trả kết quả cho hàm `broadcastOnlineList()`.
30. Hàm `broadcastOnlineList()` gọi lớp `ClientHandler` của mọi người chơi rỗi gửi message `ONLINE_LIST`.
31. Lớp `ClientHandler` gửi message `MATCH_END` tới máy B và `ONLINE_LIST` tới máy những người chơi rỗi, trong đó có máy A.
32. Lớp `ServerConnection` chuyển message `ONLINE_LIST` cho lớp `LobbyActivity` trên máy A.
33. Lớp `LobbyActivity` hiển thị danh sách người chơi online đã cập nhật điểm cho A.
34. Lớp `ServerConnection` chuyển message `MATCH_END` cho lớp `MatchActivity` trên máy B.
35. Lớp `MatchActivity` gọi lớp `ResultDialog`.
36. Lớp `ResultDialog` hiển thị cho B "Bạn thắng (+1 điểm). Đối thủ đã thoát.", ẩn nút Tái đấu, chỉ còn nút Về sảnh, đếm ngược 30 giây.
37. B click nút Về sảnh.
38. Lớp `ResultDialog` gọi lớp `ServerConnection` yêu cầu gửi message `REMATCH_REPLY` từ chối.
39. Lớp `ResultDialog` gọi lớp `LobbyActivity` trên máy B.
40. Lớp `ServerConnection` gửi message `REMATCH_REPLY` qua TCP tới lớp `ClientHandler` của B.
41. Lớp `ClientHandler` gọi hàm `handleRematchReply()` của lớp `MatchController`.
42. Lớp `MatchController` gọi lớp `MatchManager` yêu cầu lấy phòng chơi theo `roomId`.
43. Lớp `MatchManager` gọi hàm `getRoom()`.
44. Hàm `getRoom()` trả phòng chơi cho lớp `MatchController`; lớp `MatchController` ghi nhận B dừng.
45. Lớp `MatchController` gọi hàm `stopRematch()` vì B chọn về sảnh.
46. Hàm `stopRematch()` gọi lớp `OnlinePlayerManager` yêu cầu chuyển B về rỗi.
47. Lớp `OnlinePlayerManager` gọi hàm `updateStatus()`.
48. Hàm `updateStatus()` trả kết quả cho hàm `stopRematch()`.
49. Hàm `stopRematch()` gọi lớp `MatchManager` yêu cầu bỏ phòng chơi vì không còn ai trong phòng.
50. Lớp `MatchManager` gọi hàm `removeRoom()`.
51. Hàm `removeRoom()` trả kết quả cho hàm `stopRematch()`.
52. Hàm `stopRematch()` gọi lớp `ChallengeController` yêu cầu gửi lại danh sách người chơi online.
53. Lớp `ChallengeController` gọi hàm `broadcastOnlineList()`.
54. Hàm `broadcastOnlineList()` gọi lớp `OnlinePlayerManager` yêu cầu lấy danh sách người chơi online.
55. Lớp `OnlinePlayerManager` gọi hàm `getOnlinePlayers()`.
56. Hàm `getOnlinePlayers()` trả danh sách cho hàm `broadcastOnlineList()`.
57. Hàm `broadcastOnlineList()` gọi lớp `PlayerRankingDAO` yêu cầu lấy tổng điểm của các người chơi trong danh sách.
58. Lớp `PlayerRankingDAO` gọi hàm `getTotalPoints()`.
59. Hàm `getTotalPoints()` gọi lớp `PlayerRanking` để đóng gói thông tin.
60. Lớp `PlayerRanking` đóng gói thông tin thực thể.
61. Lớp `PlayerRanking` trả kết quả cho hàm `getTotalPoints()`.
62. Hàm `getTotalPoints()` trả kết quả cho hàm `broadcastOnlineList()`.
63. Hàm `broadcastOnlineList()` gọi lớp `ClientHandler` của mọi người chơi rỗi gửi message `ONLINE_LIST`.
64. Lớp `ClientHandler` gửi message `ONLINE_LIST` qua TCP tới lớp `ServerConnection` trên máy của những người chơi rỗi, gồm A và B.
65. Lớp `ServerConnection` chuyển message cho lớp `LobbyActivity` trên hai máy.
66. Lớp `LobbyActivity` hiển thị danh sách người chơi online đã cập nhật cho A và B.
<!-- END v3-m4-quit -->

Biểu đồ tuần tự của nhánh thoát ván như Hình 3.27.

![Hình 3.27: Biểu đồ tuần tự modul thách đấu, nhánh thoát ván](vp/png/seq-m4-5.png)

*Hình 3.27: Biểu đồ tuần tự modul thách đấu và đấu đối kháng, nhánh thoát ván*

Các cách kết thúc ván còn lại:

- Mất kết nối giữa ván: `ClientHandler` gặp lỗi đọc hoặc quá 15 giây không nhận được dòng nào thì gọi `MatchController.handleDisconnect()`; hàm này gọi `endMatch()` với lý do `DISCONNECT` và phần sau giống nhánh thoát ván kể từ hàm `endMatch()`.
- Hết giờ: bộ hẹn giờ 10 phút của phòng gọi `MatchController.onTimeout()`, hàm này gọi `endMatch()` với lý do `TIMEOUT`; phần sau giống phần 3 kể từ bước 97, hai người được hỏi tái đấu.
- Hết 30 giây ở dialog kết quả mà chưa đủ hai người đồng ý: bộ hẹn giờ của phòng gọi `MatchController.onRematchTimeout()`, những người chưa trả lời được coi là chọn dừng (`stopRematch()`).

## 6. Thiết kế triển khai

Các lớp được chia vào gói theo tầng: phía client là `view` và `network`; phía server là `controller` và `dao`; phần dùng chung `core` gồm `model` (lớp thực thể), `logic` (xử lý bàn chơi) và `protocol` (message). Gói gốc là `ptit.e23cnpm01.pikachu`. Biểu đồ gói như Hình 3.28.

![Hình 3.28: Biểu đồ gói](vp/png/package.png)

*Hình 3.28: Biểu đồ gói*

Mỗi người chơi cài ứng dụng trên một thiết bị Android; các thiết bị kết nối tới máy chủ game qua TCP; máy chủ game truy cập CSDL qua JDBC. Biểu đồ triển khai như Hình 3.29.

![Hình 3.29: Biểu đồ triển khai](vp/png/deployment.png)

*Hình 3.29: Biểu đồ triển khai*

## 7. Thiết kế giao thức truyền thông

- Dùng TCP thay vì UDP: game theo lượt click, không cần tốc độ khung hình cao, nhưng không được mất hay đảo thứ tự nước đi và kết quả ván. TCP bảo đảm cả hai điều này.
- Mỗi message là một đối tượng JSON nằm trên một dòng (kết thúc bằng ký tự xuống dòng), trường `type` cho biết loại message, các trường còn lại là dữ liệu. Ví dụ: `{"type":"MOVE","roomId":12,"a":{"row":3,"col":5},"b":{"row":7,"col":2}}`. Lớp `Message` trong `core` chuyển đổi giữa đối tượng và chuỗi JSON (thư viện Gson) để client và server dùng chung một định dạng. Đối tượng `Player` gửi qua mạng không mang mật khẩu vì thuộc tính `password` là `transient` (mục 1b).
- Client kết nối tới server ngay khi mở ứng dụng. Kết nối chưa đăng nhập (khách) chỉ được gửi `REGISTER`, `LOGIN`, `GET_RANKING`, `PING`; server bỏ qua các message khác trên kết nối đó.
- Client gửi `PING` mỗi 5 giây; server quá 15 giây không nhận được dòng nào trên một kết nối thì xử lý như mất kết nối (mục 3).
- Khi mở màn hình đấu đối kháng, client gửi `GET_ONLINE_LIST` một lần để lấy danh sách hiện tại; sau đó server tự đẩy `ONLINE_LIST` cho mọi client đang rỗi mỗi khi có thay đổi (đăng nhập, đăng xuất, đổi trạng thái, đổi điểm), nên client không phải hỏi định kỳ. Danh sách gửi cho mỗi người không gồm chính người đó.
- Ván đang diễn ra được gọi bằng mã phòng `roomId` do server cấp (mục 4d), không phải `id` của ván trong CSDL vì ván chỉ được lưu khi kết thúc.
- Toạ độ ô trong message là toạ độ trên lưới 12x12 có viền.

| Chiều | `type` | Nội dung chính |
|---|---|---|
| C → S | `REGISTER` | `username`, `password` |
| S → C | `REGISTER_RESULT` | `ok`, `error` |
| C → S | `LOGIN` | `username`, `password` |
| S → C | `LOGIN_RESULT` | `ok`, `error`, `player` |
| C → S | `LOGOUT` | không có dữ liệu; server gỡ người chơi khỏi danh sách online, không trả lời |
| C → S | `PING` | không có dữ liệu, gửi mỗi 5 giây; server không trả lời |
| C → S | `UPDATE_STATUS` | `status`: `TRAINING` khi bắt đầu ván với máy, `IDLE` khi ván kết thúc hoặc thoát; server bỏ qua nếu đang `BUSY` |
| C → S | `GET_ONLINE_LIST` | không có dữ liệu; server trả `ONLINE_LIST` cho riêng người hỏi |
| S → C | `ONLINE_LIST` | danh sách `PlayerRanking` (thuộc tính kế thừa từ `Player`: `username`, `status` là `IDLE`, `TRAINING` hoặc `BUSY`; thêm `totalPoint`), không gồm người nhận |
| C → S | `GET_RANKING` | `page` (số trang, bắt đầu từ 1) hoặc `mine` (`true`: trang chứa người chơi đang đăng nhập) |
| S → C | `RANKING` | `page`, `pageCount`, `me` (thứ hạng của người hỏi khi `mine`, rỗng nếu chưa có trong bảng), danh sách `{rank, username, totalPoint, avgOpponentPoint, avgWinTime}` của trang |
| C → S | `INVITE` | `to` |
| S → C | `INVITE_RECEIVED` | `from`, `timeoutSec` (30): gửi cho người được mời |
| C → S | `INVITE_CANCEL` | `to`: người mời hủy lời mời đang chờ |
| C → S | `INVITE_REPLY` | `from`, `accept` |
| S → C | `INVITE_RESULT` | gửi cho người mời: `to`, `accepted`, `reason` (`REJECTED`: bị từ chối; `BUSY`: người được mời đang bận hoặc vừa vào ván khác; `OFFLINE`: người được mời không online hoặc vừa rời mạng; `EXPIRED`: hết 30 giây chưa được trả lời) |
| S → C | `INVITE_CLOSED` | gửi cho người được mời khi lời mời không còn hiệu lực: `from`, `reason` (`CANCELLED`: người mời hủy; `EXPIRED`: hết 30 giây, hoặc trả lời một lời mời đã hết hiệu lực; `BUSY`: người mời đã vào ván khác; `OFFLINE`: người mời đăng xuất hoặc mất kết nối) |
| S → C | `MATCH_START` | `roomId`, `tiles`, `timeLimitSec`, `opponent` |
| C → S | `MOVE` | `roomId`, `a`, `b` |
| S → C | `MOVE_RESULT` | `ok`, `path`, `remaining` |
| S → C | `BOARD_SHUFFLED` | `tiles` (chỉ gửi cho người có bàn bị bế tắc) |
| S → C | `OPPONENT_PROGRESS` | `remaining`, `a`, `b`, `path` (nước đối thủ vừa đi), `tiles` (bàn hiện tại của đối thủ), `shuffled` (bàn đối thủ vừa được xáo) |
| C → S | `LEAVE_MATCH` | `roomId` |
| S → C | `MATCH_END` | `result`, `point`, `finishTime` và `remaining` của hai bên, `reason` (`CLEARED`, `TIMEOUT`, `QUIT`, `DISCONNECT`), `rematch` (`true` thì hỏi tái đấu; `false` khi đối thủ thoát hoặc mất kết nối), `replySec` (30); với `QUIT`, `DISCONNECT` chỉ gửi cho người còn lại |
| C → S | `REMATCH_REPLY` | `roomId`, `accept`; gửi lại được để đổi từ đồng ý sang từ chối |
| S → C | `OPPONENT_LEFT` | không có dữ liệu; gửi cho người còn ở dialog kết quả khi đối thủ chọn Dừng, hết thời gian trả lời hoặc mất kết nối |

Luồng message chính giữa hai client và server như Hình 3.30.

![Hình 3.30: Luồng message giữa hai client và server](vp/png/protocol.png)

*Hình 3.30: Luồng message giữa hai client và server*

Xử lý các tình huống bất thường:

- Người được mời không trả lời: sau 30 giây server hủy lời mời, gửi `INVITE_RESULT` (`EXPIRED`) cho người mời và `INVITE_CLOSED` (`EXPIRED`) cho người được mời.
- Người mời hủy lời mời: `INVITE_CANCEL`, server gửi `INVITE_CLOSED` (`CANCELLED`) cho người được mời.
- Một người vào ván (chấp nhận một lời mời): mọi lời mời khác đi và đến người đó bị đóng với lý do `BUSY`.
- Mất kết nối hoặc đăng xuất khi đang có lời mời chờ: mọi lời mời đi và đến người đó bị đóng với lý do `OFFLINE`.
- Mất kết nối giữa ván (lỗi đọc socket hoặc quá 15 giây không có `PING`): server coi như người đó thoát, kết thúc ván với lý do `DISCONNECT`, người còn lại thắng và chỉ được hỏi Về sảnh.
- Thoát giữa ván (`LEAVE_MATCH`): người thoát thua, được coi như đã chọn Dừng và về rỗi ngay; người còn lại thắng, nhận `MATCH_END` với `rematch = false`.
- Mất kết nối khi đang ở dialog kết quả, hoặc hết 30 giây mà chưa trả lời: coi như chọn Dừng, gửi `OPPONENT_LEFT` cho người còn lại.
- Hết giờ: bộ hẹn giờ của `MatchRoom` gọi `MatchController.onTimeout()`, kết thúc ván hòa với lý do `TIMEOUT`.
- Hai người cùng xóa hết ô gần như cùng lúc, hoặc hết giờ đúng lúc có nước đi cuối: các thao tác trên phòng được đồng bộ và `endMatch()` chỉ chạy một lần (cờ `ended`), nên chỉ thông điệp được xử lý trước có hiệu lực.

## 8. Thuật toán

### a. Kiểm tra đường nối hai ô

Hai ô nối được khi cùng hình và có một đường gồm tối đa 3 đoạn thẳng (tối đa 2 lần rẽ) đi theo hàng, cột, chỉ qua các ô trống; đường được phép đi trên vòng viền trống của lưới 12x12. Các dạng đường hợp lệ và một ví dụ không hợp lệ như Hình 3.31.

![Hình 3.31: Các dạng đường nối hai ô](uml/pha3/path-examples.png)

*Hình 3.31: Các dạng đường nối hai ô*

`PathFinder.checkPath()` dùng tìm kiếm theo chiều rộng, mỗi trạng thái là một ô kèm hướng đi và số đoạn đã dùng. Từ mỗi trạng thái, thuật toán đi thẳng theo từng hướng qua các ô trống; đổi hướng thì tăng số đoạn; dừng nhánh khi số đoạn vượt 3, gặp ô có hình, ra khỏi lưới, hoặc ô đó đã được tới bằng số đoạn không lớn hơn.

```text
checkPath(board, a, b):
    nếu a = b hoặc tile(a) = 0 hoặc tile(a) ≠ tile(b): trả về null
    Q ← hàng đợi, thêm (a, hướng = rỗng, số đoạn = 0, đường = [a])
    best[ô][hướng] ← vô cùng
    trong khi Q khác rỗng:
        (c, d, k, path) ← lấy đầu Q
        với mỗi hướng d' trong {lên, xuống, trái, phải}:
            k' ← k nếu d' = d, ngược lại k + 1
            nếu k' > 3: bỏ qua
            n ← c + d'
            trong khi n nằm trong lưới 12x12:
                // rút gọn: chỉ giữ hai đầu mút và các điểm rẽ
                nếu n = b: trả về rút gọn(path + [n])
                nếu tile(n) ≠ 0 hoặc best[n][d'] ≤ k': dừng
                best[n][d'] ← k'
                thêm (n, d', k', path + [n]) vào Q
                n ← n + d'
    trả về null
```

Số trạng thái tối đa là 12 x 12 x 4, nên mỗi lần kiểm tra chạy rất nhanh, không làm chậm phản hồi của server cho mỗi nước đi.

### b. Sinh đề, phát hiện bế tắc và xáo bàn

```text
generateBoard(rows = 10, cols = 10, iconTypes, seed):
    n ← rows x cols / iconTypes                // số ô mỗi loại, phải là số chẵn
    values ← mỗi mã hình 1..iconTypes lặp n lần
    rng ← Random(seed)                         // tạo một lần trước vòng lặp
    lặp:
        xáo values bằng rng
        đặt values vào vùng chơi (hàng 1..10, cột 1..10), viền để 0
    đến khi findAvailablePair(board) ≠ null
    trả về board

findAvailablePair(board):
    với mỗi mã hình t, với mỗi cặp ô (p, q) cùng mang hình t:
        nếu checkPath(board, p, q) ≠ null: trả về [p, q]
    trả về null                                // bàn bế tắc

shuffle(board, seed):
    cells ← các ô còn hình; values ← mã hình của các ô đó
    rng ← Random(seed)
    lặp:
        xáo values bằng rng, gán lại vào cells
    đến khi findAvailablePair(board) ≠ null
    trả về board
```

- Với 25 loại hình, mỗi loại có 4 ô, nên `findAvailablePair()` xét tối đa 25 x 6 = 150 cặp.
- Sau mỗi lần xóa cặp, bên xử lý (client trong ván với máy, server trong ván đối kháng) gọi `findAvailablePair()`; nếu trả về rỗng mà bàn chưa hết ô thì gọi `shuffle()`.
- Trong ván đối kháng, server sinh đề một lần, sao thành hai bàn riêng cho hai người. Bế tắc xảy ra trên bàn của từng người, nên server chỉ xáo bàn của người bị bế tắc và chỉ gửi `BOARD_SHUFFLED` cho người đó. Seed được server sinh nên client không đoán trước được kết quả xáo.

### c. Máy (bot)

- `getDelayMs()`: thời gian chờ giữa hai nước đi của máy, Dễ 6 giây, Trung bình 4 giây, Khó 2 giây (giá trị ban đầu, chỉnh khi chơi thử).
- `nextMove(board)`: lấy danh sách các cặp còn nối được trên bàn của máy. Mức Dễ và Trung bình chọn ngẫu nhiên một cặp. Mức Khó thử xóa từng cặp trên một bản sao và chọn cặp để lại nhiều cặp nối được nhất, tránh tự làm bàn bế tắc. Nếu không còn cặp nào, `BotGameActivity` xáo bàn của máy rồi đi tiếp.

### d. Kết quả ván và bảng xếp hạng

Kết quả ván do `MatchController.endMatch()` quyết định theo lý do kết thúc:

| Lý do | Người A | Người B |
|---|---|---|
| A xóa hết ô trước (`CLEARED`) | WIN, `finishTime` = thời điểm server nhận nước đi cuối của A trừ thời điểm bắt đầu ván | LOSE, `finishTime` rỗng |
| Hết 10 phút (`TIMEOUT`) | DRAW | DRAW |
| A bấm Thoát (`QUIT`) hoặc mất kết nối (`DISCONNECT`) | LOSE | WIN, `finishTime` rỗng |

Điểm mỗi ván theo đề: thắng 1, hòa 0.5, thua 0. Điểm không lưu trong CSDL mà tính từ `result` ngay trong truy vấn (bảng tạm `r`). Bảng xếp hạng chỉ gồm người chơi đã có ít nhất một ván đối kháng (khớp ngoại lệ 2a và 4a của kịch bản xem bảng xếp hạng), được tính bằng một truy vấn trong `PlayerRankingDAO.getRanking()`:

```sql
WITH r AS (SELECT matchId, playerId, result, finishTime,
                  CASE result WHEN 'WIN' THEN 1 WHEN 'DRAW' THEN 0.5
                              ELSE 0 END AS point
           FROM tblMatchResult),
     t AS (SELECT playerId, SUM(point) AS totalPoint FROM r GROUP BY playerId)
SELECT p.id, p.username,
       t.totalPoint,
       o.avgOpponentPoint,
       w.avgWinTime
FROM tblPlayer p
JOIN t ON t.playerId = p.id
JOIN (SELECT me.playerId, AVG(ot.totalPoint) AS avgOpponentPoint
      FROM r me
      JOIN r opp ON opp.matchId = me.matchId AND opp.playerId <> me.playerId
      JOIN t ot ON ot.playerId = opp.playerId
      GROUP BY me.playerId) o ON o.playerId = p.id
LEFT JOIN (SELECT playerId, AVG(finishTime) AS avgWinTime
           FROM r
           WHERE result = 'WIN' AND finishTime IS NOT NULL
           GROUP BY playerId) w ON w.playerId = p.id
ORDER BY t.totalPoint DESC, o.avgOpponentPoint DESC,
         w.avgWinTime IS NULL, w.avgWinTime ASC, p.username ASC;
```

- Trung bình điểm đối thủ lấy tổng điểm **hiện tại** của đối thủ ở mỗi ván, gặp cùng một người nhiều ván thì tính nhiều lần.
- Trung bình thời gian thắng chỉ tính các ván thắng có `finishTime`; ván thắng do đối thủ bỏ cuộc không tính (đề xuất). Người chưa có ván thắng nào có giá trị rỗng và đứng sau ở tiêu chí thứ ba (`avgWinTime IS NULL` sắp xếp sai trước đúng).
- Số hạng do `RankingController` đánh theo thứ tự truy vấn trả về và ghi vào thuộc tính `rank` của từng `PlayerRanking`. Hai người trùng cả ba tiêu chí thì cùng hạng (đánh số kiểu 1, 2, 2, 4) và được liệt kê theo tên đăng nhập; `p.username` ở cuối `ORDER BY` chỉ để thứ tự hiển thị cố định. Vì có thể cùng hạng, client tô dòng của người chơi theo tên đăng nhập chứ không theo số hạng.
- Phân trang: dòng thứ i (đếm từ 1 theo thứ tự truy vấn) nằm ở trang ⌈i / 10⌉; số trang là ⌈số dòng / 10⌉, tối thiểu 1. Trang chứa người chơi tính theo vị trí dòng của người đó, nên hai người cùng hạng ở ranh giới trang vẫn mỗi người nằm đúng một trang.
- `getTotalPoints(listPlayer)` tính tổng điểm như bảng tạm `t` nhưng chỉ cho các người chơi trong danh sách online. Truy vấn đi từ `tblPlayer` và `LEFT JOIN` sang `tblMatchResult`, tổng điểm lấy `COALESCE(..., 0)`, nên người chưa có ván nào vẫn hiện trong danh sách với 0 điểm.
- Bảng xếp hạng không gồm người chưa có ván đối kháng nào (quyết định ghi ở `plan.md` §8): người đó không có trung bình điểm đối thủ nên không xếp được theo tiêu chí thứ hai.
