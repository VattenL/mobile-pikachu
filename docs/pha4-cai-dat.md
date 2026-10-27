# Pha 4. Cài đặt

> Bản nháp, viết trước khi có code. Mục 1 và 3 dùng được ngay làm khung cho việc cài đặt; mục 2 mô tả các lớp cần cài và cách các phương thức hoạt động theo thiết kế ở pha 3, phần trích code sẽ thay bằng code thật khi cài đặt xong.

## 1. Tổ chức dự án

### a. Cấu trúc module

Dự án Gradle gồm ba module, khai báo trong `settings.gradle.kts`:

```kotlin
include(":app", ":core", ":server")
```

| Module | Loại | Phụ thuộc | Nội dung |
|---|---|---|---|
| `core` | `java-library`, Java 11, không phụ thuộc Android | Gson | gói `model`, `logic`, `protocol` |
| `app` | ứng dụng Android, minSdk 24 | `project(":core")`, AndroidX | gói `view`, `network` |
| `server` | ứng dụng Java (`application`, lớp chạy `GameServer`) | `project(":core")`, MySQL Connector/J | gói `controller`, `dao`; thư mục `db` chứa script CSDL |

Thư viện mới (Gson, MySQL Connector/J) được thêm vào `gradle/libs.versions.toml`, không khai báo trực tiếp trong file build. Kiểm thử đơn vị dùng JUnit 4 (đã có trong catalog).

### b. Cây thư mục và gói

```text
pikachu/
├── core/src/main/java/ptit/e23cnpm01/pikachu/core/
│   ├── model/      Player, PlayerRanking, Match, MatchResult, Board, Cell
│   ├── logic/      PathFinder, BoardGenerator, Bot, Difficulty
│   └── protocol/   Message, MessageType
├── core/src/test/java/...           PathFinderTest, BoardGeneratorTest, BoardTest, BotTest
├── app/src/main/java/ptit/e23cnpm01/pikachu/
│   ├── view/       LoginActivity, RegisterActivity, BaseActivity, MainActivity, LobbyActivity, RankingActivity,
│   │               DifficultyActivity, BotGameActivity, MatchActivity, ChallengeConfirmDialog,
│   │               InvitationDialog, ResultDialog, BotResultDialog, BoardView
│   └── network/    ServerConnection, MessageListener, ClientSession
├── server/src/main/java/ptit/e23cnpm01/pikachu/server/
│   ├── controller/ GameServer, ClientHandler, LoginController, RankingController,
│   │               ChallengeController, MatchController, OnlinePlayerManager,
│   │               MatchManager, MatchRoom
│   └── dao/        DAO, PlayerDAO, PlayerRankingDAO, MatchDAO
├── server/src/test/java/...         PlayerDAOTest, PlayerRankingDAOTest, MatchDAOTest,
│                                    OnlinePlayerManagerTest, MatchControllerTest
└── server/db/      create-db.sql, schema.sql, seed.sql, seed-tiebreak.sql
```

### c. Môi trường và cấu hình

- Client: Android Studio, Android SDK 37, thiết bị hoặc máy ảo Android 7.0 (API 24) trở lên. Địa chỉ và cổng server đặt trong tài nguyên chuỗi của `app` để đổi khi chạy thử (máy ảo truy cập máy chạy server qua `10.0.2.2`).
- Server: JDK 11 trở lên, MySQL 8. Thông tin kết nối CSDL và cổng (mặc định 9999) đọc từ `server.properties`, không ghi cứng trong code.
- Ứng dụng Android cần quyền `android.permission.INTERNET`. Kết nối socket chạy trên luồng nền, không chạy trên luồng giao diện.

### d. Script cơ sở dữ liệu

`server/db/create-db.sql` chạy một lần bằng tài khoản quản trị MySQL: tạo CSDL `pikachu` với bảng mã `utf8mb4` (để lưu tên tiếng Việt) và một tài khoản riêng cho server, chỉ có quyền trên CSDL này.

`server/db/schema.sql` tạo lại các bảng theo Hình 3.2 (xóa bảng cũ theo thứ tự ngược với khóa ngoại):

```sql
DROP TABLE IF EXISTS tblMatchResult;
DROP TABLE IF EXISTS tblMatch;
DROP TABLE IF EXISTS tblPlayer;

CREATE TABLE tblPlayer (
  id        INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username  VARCHAR(50)  NOT NULL UNIQUE,
  password  VARCHAR(64)  NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE tblMatch (
  id         INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
  startTime  DATETIME     NOT NULL,
  endTime    DATETIME     NOT NULL,
  timeLimit  INT          NOT NULL,
  tiles      VARCHAR(300) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE tblMatchResult (
  id          INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
  result      VARCHAR(10)  NOT NULL,
  finishTime  INT          NULL,
  tiles       VARCHAR(300) NOT NULL,
  matchId     INT          NOT NULL,
  playerId    INT          NOT NULL,
  UNIQUE (matchId, playerId),
  FOREIGN KEY (matchId)  REFERENCES tblMatch(id) ON DELETE CASCADE,
  FOREIGN KEY (playerId) REFERENCES tblPlayer(id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
```

Hình 3.2 ghi kiểu của MySQL (`int(10)`, `datetime`). Script ghi `INT` không kèm độ rộng vì MySQL 8 đã bỏ độ rộng hiển thị của kiểu số nguyên (`int(10)` và `INT` là cùng một kiểu), và dùng `DATETIME` chứ không dùng `TIMESTAMP` vì `TIMESTAMP` đổi múi giờ khi đọc ghi và chỉ đến năm 2038. Chương trình kết nối CSDL cũng dùng bảng mã `utf8mb4`.

`server/db/seed.sql` nhập dữ liệu mẫu dùng chung cho kiểm thử đơn vị và kiểm thử chức năng (mô tả ở pha 5, mục 2c).

## 2. Cài đặt các modul

Lớp thực thể không trình bày code (chỉ có thuộc tính, hàm khởi tạo, get, set). Với mỗi modul trình bày các lớp trong biểu đồ lớp thiết kế và cách các phương thức hoạt động.

### a. Phần dùng chung

- `DAO`: giữ một `Connection` tĩnh dùng chung, mở một lần trong hàm khởi tạo từ thông tin trong `server.properties`. Vì nhiều luồng `ClientHandler` cùng dùng một kết nối, mọi phương thức của DAO con, cả đọc lẫn ghi, đều khóa trên `DAO.class`; phương thức ghi dùng thêm giao dịch (`setAutoCommit(false)`, `commit`, lỗi thì `rollback`). Hàm đọc cũng phải khóa vì giao dịch gắn với kết nối chứ không gắn với luồng: nếu không, một hàm đọc chạy xen giữa các lệnh của `saveMatch()` sẽ thấy dữ liệu đang ghi dở (ván mới chỉ có một kết quả).

```java
public abstract class DAO {
    protected static Connection con;

    public DAO() {
        if (con == null) {
            Properties p = ServerConfig.load();
            try {
                con = DriverManager.getConnection(p.getProperty("db.url"),
                        p.getProperty("db.user"), p.getProperty("db.password"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```

- `Message`: bọc một `JsonObject` của Gson; `toJson()` trả chuỗi JSON một dòng, `fromJson(line)` dựng lại đối tượng; các hàm `put`, `getString`, `getInt`, `getCell`... đọc ghi trường dữ liệu. `MessageType` chứa các hằng chuỗi `LOGIN`, `MOVE`... theo bảng ở pha 3, mục 7.
- `ClientHandler.run()`: đặt `socket.setSoTimeout(15000)`, rồi vòng lặp đọc từng dòng từ socket, `Message.fromJson()`, chuyển theo `type` cho hàm xử lý tương ứng của lớp điều khiển (`PING` thì bỏ qua, chỉ để giữ kết nối). Khi đọc gặp lỗi, hết luồng hoặc quá 15 giây không có dòng nào (client mất kết nối), gọi lần lượt `MatchController.handleDisconnect()` (nếu `roomId` khác rỗng), `OnlinePlayerManager.removeOnlinePlayer()`, `ChallengeController.closeInvitations(username, "OFFLINE")`, `ChallengeController.broadcastOnlineList()`, rồi đóng socket. `send(msg)` ghi `msg.toJson()` kèm xuống dòng, được đồng bộ để hai luồng không ghi xen vào nhau.
- `ServerConnection`: một đối tượng duy nhất trong ứng dụng; `connect()` mở socket trên luồng nền; một luồng đọc riêng nhận từng dòng, dựng `Message` và dùng `Handler` của luồng chính để gọi `onMessage()` của các `MessageListener` đang đăng ký. Mỗi `Activity` đăng ký trong `onStart()` và hủy trong `onStop()`. `pingTimer` gửi `PING` mỗi 5 giây. Khi socket báo lỗi: hủy `pingTimer`, xóa `ClientSession`, đưa ứng dụng về `MainActivity` ở chế độ khách với thông báo "Mất kết nối tới máy chủ, vui lòng đăng nhập lại", rồi thử kết nối lại.

### b. Modul quản lý tài khoản

| Lớp | Phương thức | Cách hoạt động |
|---|---|---|
| `LoginActivity` | `onRegisterClick()` | mở `RegisterActivity`; khi quay về kèm tên đăng nhập vừa đăng ký thì điền sẵn và hiện "Đăng ký thành công, mời đăng nhập" |
| `RegisterActivity` | `onRegisterClick()` | kiểm tra bốn ô không rỗng và mật khẩu nhập lại khớp; hợp lệ thì gửi `REGISTER` |
| | `onMessage()` | `REGISTER_RESULT`: thành công thì trả tên đăng nhập về `LoginActivity` và đóng màn; thất bại thì hiện lỗi, xóa hai ô mật khẩu |
| | `onBackClick()` | đóng màn, về `LoginActivity`, không gửi gì |
| `LoginActivity` | `onLoginClick()` | kiểm tra hai ô không rỗng, gửi `LOGIN` qua `ServerConnection` |
| | `onMessage()` | nhận `LOGIN_RESULT`: thành công thì lưu người chơi vào `ClientSession` và mở `MainActivity`; thất bại thì hiện thông báo lỗi |
| `LoginController` | `handleLogin()` | băm mật khẩu, gọi `PlayerDAO.checkLogin()`; nếu đúng thì gọi `OnlinePlayerManager.addOnlinePlayer()`, sai hoặc đang online nơi khác thì trả lỗi; gửi `LOGIN_RESULT`, sau đó gửi lại danh sách online cho mọi người |
| `PlayerDAO` | `checkLogin()` | `SELECT id, username FROM tblPlayer WHERE username = ? AND password = ?` bằng `PreparedStatement` (chống SQL injection), có dòng thì đóng gói `Player`, không có thì trả `null` |
| `MainActivity` | `onLogoutClick()` | gửi `LOGOUT`, `ClientSession.clear()`, mở `LoginActivity` và đóng các màn hình phía sau |
| `LoginController` | `handleRegister()` | kiểm tra ba trường không rỗng, băm mật khẩu, gọi `PlayerDAO.addPlayer()`, gửi `REGISTER_RESULT` (`ok`, hoặc `error` "tên đăng nhập đã tồn tại") |
| | `handleLogout()` | `OnlinePlayerManager.removeOnlinePlayer()`, đặt người chơi của `ClientHandler` về rỗng, `ChallengeController.broadcastOnlineList()` |
| `PlayerDAO` | `addPlayer()` | `INSERT INTO tblPlayer (username, password) VALUES (?, ?)`; trùng `username` thì CSDL báo vi phạm ràng buộc `UNIQUE`, hàm bắt lỗi và trả `false` |
| `OnlinePlayerManager` | `removeOnlinePlayer()` | xóa ánh xạ tên đăng nhập → `ClientHandler`; `ClientHandler` cũng gọi hàm này khi client mất kết nối |

### c. Modul xem bảng xếp hạng

| Lớp | Phương thức | Cách hoạt động |
|---|---|---|
| `MainActivity` | `onRankingClick()` | mở `RankingActivity`; dùng được cả khi là khách |
| `RankingActivity` | `onCreate()`, `onMessage()` | ẩn nút Vị trí của tôi nếu `ClientSession` chưa có người chơi; gửi `GET_RANKING` với `page = 1`; nhận `RANKING` thì đổ các dòng của trang vào `RecyclerView`, hiện "Trang page/pageCount", lưu `me` vào `myRank`, tô màu dòng có `username` trùng người chơi trong `ClientSession` (không theo số hạng vì có thể đồng hạng); bảng rỗng thì hiện "Chưa có dữ liệu xếp hạng" và vô hiệu hai nút chuyển trang; `mine` mà `me` rỗng thì báo "Bạn chưa có trong bảng xếp hạng"; đổi `avgWinTime` từ mili giây sang mm:ss |
| | `onPrevPageClick()`, `onNextPageClick()`, `onMyRankClick()` | gửi `GET_RANKING` với `page - 1`, `page + 1` (vô hiệu ở trang đầu, trang cuối) hoặc `mine = true` |
| `RankingController` | `handleGetRanking()` | gọi `PlayerRankingDAO.getRanking()`, đánh số hạng theo thứ tự trả về vào `rank` của từng dòng (trùng cả ba tiêu chí thì cùng hạng, kiểu 1, 2, 2, 4); `mine = true` thì tìm dòng của `handler.player` (rỗng nếu chưa đăng nhập hoặc chưa có trong bảng) và lấy trang chứa dòng đó, ngược lại lấy trang `page` (ngoài khoảng thì kẹp về 1..pageCount); gửi `RANKING` với `page`, `pageCount`, `me` và `PAGE_SIZE` = 10 dòng của trang |
| `PlayerRankingDAO` | `getRanking()` | chạy truy vấn ở pha 3, mục 8d, mỗi dòng đóng gói một `PlayerRanking` (kế thừa `Player`: `id`, `username`; thêm ba chỉ số) |

### d. Modul chơi với máy

| Lớp | Phương thức | Cách hoạt động |
|---|---|---|
| `DifficultyActivity` | `onStartClick()` | lấy độ khó của thẻ đang chọn, mở `BotGameActivity` kèm độ khó |
| `BotGameActivity` | `onCreate()` | `BoardGenerator.generateBoard(10, 10, 25, seed)`, sao bàn cho máy, tạo `Bot`, gắn bàn vào `BoardView`, chạy `CountDownTimer` 10 phút, hẹn lượt máy đầu tiên bằng `handler.postDelayed(..., bot.getDelayMs())`; gửi `UPDATE_STATUS` với `TRAINING` |
| | `onCellClick()` | lần click đầu lưu `firstCell`; lần thứ hai gọi `PathFinder.checkPath()`; có đường thì `board.removePair()`, vẽ đường, kiểm tra `isCleared()` (xong thì `endGame("WIN")`), kiểm tra bế tắc bằng `findAvailablePair()` và `shuffle()` khi cần; không có đường thì bỏ chọn |
| | `onBotTurn()` | `bot.nextMove(botBoard)` trên luồng nền; rỗng thì xáo bàn của máy; có cặp thì xóa trên `botBoard` và vẽ nước đi lên bàn thu nhỏ (`botBoardView.showMatch()`), cập nhật số ô của máy; máy hết ô thì `endGame("LOSE")`, chưa thì hẹn lượt tiếp |
| | `onQuitClick()`, hết giờ | thoát: hủy hẹn giờ, về `MainActivity`; hết giờ: `endGame("DRAW")`; khi ván kết thúc hoặc thoát đều gửi `UPDATE_STATUS` với `IDLE` |
| `BoardView` | `onDraw()`, `onTouchEvent()` | chia khung vẽ thành lưới 12x12, vẽ ảnh nhân vật theo mã hình, viền ô đang chọn, đường nối trong khoảng 300 ms; đổi toạ độ chạm thành `Cell` |
| `PathFinder`, `BoardGenerator`, `Bot` | | theo thuật toán ở pha 3, mục 8 |

### e. Modul thách đấu và đấu đối kháng

| Lớp | Phương thức | Cách hoạt động |
|---|---|---|
| `MainActivity` | `onMatchClick()` | `ClientSession` chưa có người chơi thì mở `LoginActivity` (đăng nhập xong thì mở `LobbyActivity`); đã đăng nhập thì mở `LobbyActivity` |
| `LobbyActivity` | `onCreate()` | gửi `GET_ONLINE_LIST`; nếu được mở kèm thông báo (bị xử thua do thoát, đối thủ đã dừng) thì hiện trên `tvMessage` |
| | `onPlayerClick()` | bỏ qua nếu người được chọn đang bận; mở `ChallengeConfirmDialog` với người được chọn |
| | `onMessage()` | `ONLINE_LIST`: cập nhật danh sách; các message khác chuyển cho `BaseActivity.onMessage()` |
| `BaseActivity` | `onMessage()` | `INVITE_RECEIVED`: thêm `{from, hạn = lúc nhận + timeoutSec}` vào `invitationQueue` (tĩnh, dùng chung các màn hình), gọi `showNextInvitation()`; `INVITE_CLOSED`: bỏ lời mời của `from` khỏi hàng đợi, đóng popup nếu đang hiện lời mời đó rồi `showNextInvitation()`; `MATCH_START`: đóng popup, xóa hàng đợi, đóng màn hình hiện tại (`BotGameActivity` khi đóng thì hủy đồng hồ và lượt của máy) rồi mở `MatchActivity` |
| | `showNextInvitation()` | khi chưa có popup nào: bỏ các lời mời đã quá hạn ở đầu hàng, lấy lời mời kế tiếp và mở `InvitationDialog` không chặn (cửa sổ không modal, `FLAG_NOT_TOUCH_MODAL`) ở mép trên màn hình |
| `ChallengeConfirmDialog` | `onConfirmClick()` | gửi `INVITE`; đổi nội dung thành "Đang chờ ... trả lời" và chạy `countDown` 30 giây; nút Hủy thành Hủy lời mời; dialog không đóng được khi chạm ra ngoài |
| | `onCancelClick()` | trước khi xác nhận: đóng dialog, không gửi gì; khi đang chờ: gửi `INVITE_CANCEL`, đóng dialog |
| | `onMessage()` | `INVITE_RESULT`: đóng dialog, hiện lý do trên `LobbyActivity` ("đã từ chối", "đang bận", "không còn online", "không trả lời") |
| `InvitationDialog` | `onAcceptClick()`, `onRejectClick()` | gửi `INVITE_REPLY` với `accept` tương ứng, đóng popup; `countDown` về 0 thì chỉ đóng popup. Đóng popup thì `BaseActivity.showNextInvitation()` |
| `MatchActivity` | `onCellClick()` | hai lần click thì gửi `MOVE`; trong lúc chờ phản hồi không nhận thêm click |
| | `onMessage()` | `MOVE_RESULT`: hợp lệ thì xóa hai ô, vẽ đường; `BOARD_SHUFFLED`: thay bàn; `OPPONENT_PROGRESS`: vẽ nước đi của đối thủ lên bàn thu nhỏ, thay bằng `tiles` server gửi, cập nhật số ô đối thủ; `MATCH_END`: mở `ResultDialog` |
| | `onQuitClick()` | gửi `LEAVE_MATCH`, đóng màn chơi, mở `LobbyActivity` kèm thông báo "Bạn đã thoát nên bị xử thua (0 điểm)" |
| `ResultDialog` | `show()`, `onMessage()` | hiện kết quả, điểm nhận được, thời gian hoàn thành hoặc số ô còn lại; `rematch = false` thì ẩn nút Tái đấu, nút Dừng ghi Về sảnh; chạy `countDown` 30 giây, về 0 thì làm như bấm Dừng. `MATCH_START`: đóng dialog và màn chơi cũ, mở `MatchActivity` với ván mới; `OPPONENT_LEFT`: về `LobbyActivity` kèm thông báo đối thủ đã dừng chơi |
| | `onRematchClick()` | gửi `REMATCH_REPLY` đồng ý, hiện "Đang chờ đối thủ trả lời...", nút Dừng vẫn bấm được |
| | `onStopClick()` | gửi `REMATCH_REPLY` từ chối, về `LobbyActivity` |
| `ChallengeController` | `handleGetOnlineList()` | `OnlinePlayerManager.getOnlinePlayers()`, `PlayerRankingDAO.getTotalPoints()`, gửi `ONLINE_LIST` (không gồm người hỏi) cho riêng người hỏi |
| | `handleInvite()` | bỏ qua nếu người gửi chưa đăng nhập, đang bận, đã có lời mời đi hoặc tự mời mình; `sendInvitation()` trả `OFFLINE` hoặc `BUSY` thì gửi `INVITE_RESULT` với lý do đó; gửi được thì `inviteTimer.schedule(() -> onInviteTimeout(from, to), 30 s)` và gửi `INVITE_RECEIVED` (`from`, `timeoutSec = 30`) |
| | `handleInviteCancel()` | `removeInvitation(from, to)` đúng thì gửi `INVITE_CLOSED` (`CANCELLED`) cho người được mời |
| | `onInviteTimeout()` | `removeInvitation(from, to)` đúng (lời mời vẫn còn) thì gửi `INVITE_RESULT` (`EXPIRED`) cho người mời và `INVITE_CLOSED` (`EXPIRED`) cho người được mời; sai thì lời mời đã được xử lý, không làm gì |
| | `handleInviteReply()` | `removeInvitation(from, người gửi)` sai thì gửi `INVITE_CLOSED` (`EXPIRED`) cho người gửi; từ chối: gửi `INVITE_RESULT` (`REJECTED`); chấp nhận: kiểm tra lại cả hai chưa bận, `generateBoard()`, `MatchManager.createMatch()`, `updateStatus(..., BUSY)`, `closeInvitations()` cho hai người với lý do `BUSY`, gửi `MATCH_START` cho hai người, rồi `broadcastOnlineList()` |
| | `closeInvitations()` | `OnlinePlayerManager.removeInvitationsOf(username)`; lời mời người đó gửi đi: `INVITE_CLOSED` cho người được mời; lời mời tới người đó: `INVITE_RESULT` cho người mời; cùng lý do truyền vào |
| | `handleUpdateStatus()` | bỏ qua nếu người gửi đang `BUSY`; ngược lại `OnlinePlayerManager.updateStatus()` theo `status` trong message rồi `broadcastOnlineList()` |
| `OnlinePlayerManager` | `sendInvitation()` | `to` không online: trả `OFFLINE`; `to` đang `BUSY`: trả `BUSY`; ngược lại ghi `pendingInvites[from] = to`, trả `null` |
| | `removeInvitation()` | xóa `pendingInvites[from]` nếu đúng bằng `to`, trả đúng; ngược lại trả sai. Đồng bộ nên khi trả lời, hủy và hết hạn đến cùng lúc chỉ một lời gọi trả đúng |
| | `removeInvitationsOf()` | xóa và trả mọi cặp (người mời, người được mời) có chứa `username` |
| `MatchController` | `handleMove()` | khóa `MatchRoom`; từ chối nếu người gửi không thuộc phòng `roomId` hoặc ván đã kết thúc (kiểm tra này dùng chung cho `handleLeaveMatch()`; `handleRematchReply()` chỉ kiểm tra người gửi thuộc phòng); lấy bàn của người gửi; `checkPath()`; hợp lệ thì `removePair()`; `isCleared()` thì ghi `finishTime` theo đồng hồ server và `endMatch(room, "CLEARED")`; chưa thì kiểm tra bế tắc, xáo riêng bàn đó và gửi `BOARD_SHUFFLED`; gửi `MOVE_RESULT` cho người gửi, `OPPONENT_PROGRESS` cho đối thủ |
| | `endMatch()` | bỏ qua nếu cờ `ended` đã bật; bật cờ, hủy bộ hẹn giờ 10 phút, xác định kết quả theo bảng ở pha 3, mục 8d, tính điểm, `MatchDAO.saveMatch()`; `CLEARED`, `TIMEOUT`: gửi `MATCH_END` (`rematch = true`) cho hai người; `QUIT`, `DISCONNECT`: gửi `MATCH_END` (`rematch = false`) cho người còn lại rồi `stopRematch(room, người thoát)`; cuối cùng dùng lại `timer` hẹn `onRematchTimeout()` sau 30 giây |
| | `handleLeaveMatch()`, `onTimeout()` | `endMatch(room, "QUIT")`, `endMatch(room, "TIMEOUT")` |
| | `handleRematchReply()` | ghi lựa chọn vào `rematchReplies`; từ chối thì `stopRematch()`; cả hai đồng ý thì hủy hẹn giờ, `MatchManager.removeRoom()`, sinh đề mới, `createMatch()`, gửi `MATCH_START` |
| | `stopRematch()` | chuyển người dừng về `IDLE`, bỏ khỏi phòng; nếu ván kết thúc bình thường và người kia còn trong phòng thì người kia cũng về `IDLE`, nhận `OPPONENT_LEFT` và rời phòng; phòng rỗng thì `MatchManager.removeRoom()`; `broadcastOnlineList()` |
| | `onRematchTimeout()` | những người còn trong phòng mà chưa trả lời được coi là chọn dừng: `stopRematch()` cho từng người |
| | `handleDisconnect()` | lấy phòng theo `handler.roomId`; ván chưa kết thúc thì `endMatch(room, "DISCONNECT")`; đã kết thúc (đang ở dialog kết quả) thì `stopRematch(room, handler)` |
| `MatchManager` | `createMatch()` | cấp `roomId = nextRoomId++`, tạo `MatchRoom` với hai bản sao đề bài và hẹn giờ 10 phút gọi `onTimeout()`, gán `roomId` cho hai `ClientHandler` |
| | `removeRoom()` | xóa phòng khỏi `rooms`, hủy `timer`, đặt `roomId` của các `ClientHandler` còn giữ phòng đó về rỗng |
| `MatchDAO` | `saveMatch()` | trong một giao dịch: thêm dòng `tblMatch` (kèm đề bài), lấy khóa sinh ra, thêm hai dòng `tblMatchResult` (kết quả, thời gian hoàn thành, bàn lúc kết thúc của từng người; điểm không lưu); lỗi thì `rollback` và trả `false`. Hàm chỉ tự `commit` khi được gọi lúc `autoCommit` đang bật, nên test đơn vị tắt `autoCommit` trước khi gọi thì `rollback` được toàn bộ |
| `PlayerRankingDAO` | `getTotalPoints()` | `SELECT p.id, COALESCE(SUM(CASE r.result WHEN 'WIN' THEN 1 WHEN 'DRAW' THEN 0.5 ELSE 0 END), 0) FROM tblPlayer p LEFT JOIN tblMatchResult r ON r.playerId = p.id WHERE p.id IN (...) GROUP BY p.id` (như pha 3, mục 8d), người chưa có ván nào có tổng điểm 0 |

## 3. Kiểm thử đơn vị

### a. Quy trình

- Bước 1: với mỗi phương thức, chọn input phủ hết các nhánh; kết quả mong đợi lấy từ thiết kế (pha 3), không lấy từ code.
- Bước 2: viết test JUnit 4. Mỗi test khai báo input và kết quả mong đợi, gọi phương thức, so sánh kết quả; với DAO thì kiểm tra thêm dữ liệu trong CSDL.
- Bước 3: báo cáo số test đạt, không đạt và mô tả chi tiết các trường hợp không đạt.
- Lớp trong `core` là Java thuần nên chạy test bằng `./gradlew :core:test`, không cần thiết bị. Test DAO chạy bằng `./gradlew :server:test` trên CSDL đã nạp `schema.sql` và `seed.sql`; mỗi test ghi dữ liệu đặt `setAutoCommit(false)` và `rollback` ở cuối để không ảnh hưởng test khác.
- Bàn chơi dùng trong test được dựng từ ma trận viết sẵn trong code test, không sinh ngẫu nhiên, để kết quả lặp lại được.
- Các trường hợp bằng điểm của test 6 dùng dữ liệu riêng chèn ngay trong test rồi rollback, vì dữ liệu mẫu chung (pha 5, mục 2c) chỉ có cặp bằng tổng điểm.

### b. Bộ test case

| TT | Chức năng | Lớp | Phương thức | Trường hợp test |
|---|---|---|---|---|
| 1 | Nối cặp ô | `PathFinder` | `checkPath()` | Hai ô cùng hàng, kề nhau<br>Hai ô cùng hàng, giữa là ô trống (đường thẳng)<br>Hai ô cùng hàng, giữa có ô chặn và không có đường vòng<br>Hai ô cùng cột (đường thẳng dọc)<br>Đường chữ L (1 lần rẽ)<br>Đường chữ Z (2 lần rẽ)<br>Đường chữ U đi trên viền trên, dưới, trái, phải<br>Chỉ nối được bằng 3 lần rẽ: trả về null<br>Hai ô khác hình: trả về null<br>Chọn cùng một ô hai lần: trả về null<br>Một trong hai ô là ô trống: trả về null<br>Đường trả về chỉ gồm hai đầu mút và các điểm rẽ, đúng thứ tự |
| | | | `findAvailablePair()` | Bàn có đúng một cặp nối được: trả về đúng cặp đó<br>Bàn bế tắc (còn ô nhưng không cặp nào nối được): trả về null<br>Bàn đã hết ô: trả về null |
| 2 | Sinh đề, xáo bàn | `BoardGenerator` | `generateBoard()` | Vùng chơi đủ 100 ô có hình, viền toàn 0<br>Mỗi mã hình xuất hiện đúng 100 / iconTypes lần (số chẵn)<br>Cùng seed sinh cùng đề, khác seed sinh đề khác<br>Đề sinh ra không bế tắc |
| | | | `shuffle()` | Số ô và số lần xuất hiện của mỗi mã hình không đổi<br>Ô trống vẫn trống<br>Bàn sau khi xáo không bế tắc<br>Bàn còn đúng 2 ô cùng hình |
| 3 | Bàn chơi | `Board` | `removePair()`, `isCleared()`, `countRemaining()` | Xóa một cặp: hai ô thành 0, số ô giảm 2<br>Xóa cặp cuối: `isCleared()` đúng<br>Bàn còn ô: `isCleared()` sai |
| 4 | Máy | `Bot` | `nextMove()`, `getDelayMs()` | Bàn còn cặp: trả về một cặp mà `checkPath()` nối được<br>Bàn bế tắc: trả về null<br>Mức Khó: chọn cặp để lại nhiều cặp nối được nhất<br>Thời gian chờ giảm dần từ Dễ tới Khó |
| 5 | Đăng nhập | `PlayerDAO` | `checkLogin()` | Đúng tên đăng nhập và mật khẩu: trả về người chơi đúng id, tên<br>Sai mật khẩu: null<br>Tên đăng nhập không tồn tại: null<br>Chuỗi nhập có ký tự `'`, `--`, `OR 1=1`: null, không lỗi SQL |
| | | `OnlinePlayerManager` | `addOnlinePlayer()` | Người chưa online: trả về đúng, có trong danh sách<br>Người đang online ở máy khác: trả về sai |
| | Đăng xuất | `OnlinePlayerManager` | `removeOnlinePlayer()` | Người đang online: bị xóa khỏi danh sách<br>Người không có trong danh sách: không báo lỗi, danh sách không đổi |
| | Đăng ký | `PlayerDAO` | `addPlayer()` | Tên đăng nhập mới: trả về đúng, có thêm một dòng với mật khẩu đã băm<br>Tên đăng nhập đã có: trả về sai, không thêm dòng nào |
| 6 | Xem bảng xếp hạng | `PlayerRankingDAO` | `getRanking()` | Thứ tự theo tổng điểm giảm dần<br>Bằng tổng điểm: theo trung bình điểm đối thủ giảm dần<br>Bằng cả hai: theo trung bình thời gian thắng tăng dần<br>Trùng cả ba tiêu chí: hai người đứng liền nhau, theo thứ tự tên<br>Người chưa thắng ván nào đứng sau ở tiêu chí 3<br>Ván thắng do đối thủ bỏ cuộc không tính vào trung bình thời gian thắng<br>Gặp cùng đối thủ hai ván: điểm đối thủ tính hai lần<br>Người chưa có ván nào không có trong bảng<br>CSDL chưa có ván nào: danh sách rỗng |
| | | `RankingController` | `handleGetRanking()` | 12 người có ván: `page = 1` trả 10 dòng, `pageCount` 2; `page = 2` trả 2 dòng<br>Hai người trùng cả ba tiêu chí: cùng `rank`, người sau đó có `rank` nhảy (1, 2, 2, 4)<br>`mine` với người ở hạng 12: trả trang 2, `me` = 12<br>`mine` với người chưa có ván hoặc kết nối chưa đăng nhập: `me` rỗng<br>`page` ngoài khoảng: kẹp về trang đầu hoặc cuối<br>Bảng rỗng: `pageCount` 1, không có dòng |
| | | `PlayerRankingDAO` | `getTotalPoints()` | Danh sách người chơi có ván: đúng tổng điểm<br>Người chưa có ván: tổng điểm 0<br>Danh sách rỗng: kết quả rỗng |
| 7 | Thách đấu | `OnlinePlayerManager` | `sendInvitation()`, `removeInvitation()`, `removeInvitationsOf()`, `updateStatus()` | Người được mời rỗi hoặc luyện tập: trả `null`, có lời mời trong `pendingInvites`<br>Người được mời bận: trả `BUSY`<br>Người được mời không online: trả `OFFLINE`<br>Gỡ lời mời đang có: đúng; gỡ lần thứ hai (trả lời và hết hạn cùng lúc): sai<br>Gỡ mọi lời mời của một người: trả cả lời mời đi và đến, các lời mời khác giữ nguyên<br>Chuyển hai người sang bận rồi về rỗi: trạng thái đúng |
| | | `ChallengeController` | `handleInvite()`, `handleInviteReply()`, `onInviteTimeout()` | Mời khi đã có lời mời đi, hoặc tự mời mình: không gửi gì<br>Trả lời một lời mời đã hết hạn: người trả lời nhận `INVITE_CLOSED` (`EXPIRED`), không tạo phòng<br>Chấp nhận khi người mời đã vào ván khác: không tạo phòng<br>Chấp nhận hợp lệ: tạo đúng một phòng, hai người bận, lời mời khác tới hai người bị đóng với `BUSY`<br>Hết hạn: người mời nhận `EXPIRED`, người được mời nhận `INVITE_CLOSED` |
| 8 | Đấu đối kháng | `MatchController` | `endMatch()` | Một người xóa hết ô: người đó WIN có `finishTime`, người kia LOSE, hai người nhận `MATCH_END` với `rematch = true`<br>Hết giờ: hai người DRAW<br>Một người thoát hoặc mất kết nối: người đó LOSE và về rỗi ngay, người kia WIN không có `finishTime`, chỉ người kia nhận `MATCH_END` với `rematch = false`<br>Hai người hoàn thành gần như cùng lúc: chỉ người được xử lý trước thắng, ván chỉ kết thúc một lần<br>Hẹn giờ 10 phút đến sau khi ván đã kết thúc: không làm gì |
| | | `MatchController` | `handleRematchReply()`, `onRematchTimeout()`, `handleDisconnect()` | Cả hai đồng ý: phòng cũ bị bỏ, phòng mới có `roomId` khác, hai người vẫn bận<br>Một người dừng: hai người rỗi, người kia nhận `OPPONENT_LEFT`, phòng bị bỏ<br>Đồng ý rồi đổi sang dừng: xử lý như dừng<br>Hết 30 giây khi mới một người đồng ý: hai người rỗi, phòng bị bỏ<br>Đối thủ đã thoát, người còn lại bấm Về sảnh: người đó rỗi, phòng bị bỏ<br>Mất kết nối ở dialog kết quả: như chọn dừng |
| | | `MatchManager` | `createMatch()`, `removeRoom()` | Hai phòng tạo liên tiếp có `roomId` khác nhau, `ClientHandler` của hai người giữ đúng `roomId`<br>Bỏ phòng: `getRoom()` trả rỗng, `roomId` của hai `ClientHandler` về rỗng |
| | | `MatchDAO` | `saveMatch()` | Lưu ván hợp lệ: có một dòng `tblMatch` và hai dòng `tblMatchResult` đúng dữ liệu, gồm bàn lúc kết thúc của từng người<br>Xóa ván vừa lưu: hai dòng kết quả bị xóa theo (`ON DELETE CASCADE`)<br>Một kết quả tham chiếu người chơi không tồn tại: trả về sai, không còn dòng nào được lưu (rollback) |

### c. Mẫu cài đặt test

Test thuật toán dựng bàn từ ma trận viết sẵn (0 là ô trống; vùng chơi thu nhỏ để dễ đọc):

```java
public class PathFinderTest {
    @Test
    public void checkPath_chuU_quaVien() {
        // hai ô 1 ở hàng trên cùng của vùng chơi, giữa bị chặn, nối được qua viền trên
        Board board = TestBoards.of(
            "0 0 0 0 0 0",
            "0 1 2 3 1 0",
            "0 4 5 6 7 0",
            "0 0 0 0 0 0");
        List<Cell> path = PathFinder.checkPath(board, new Cell(1, 1), new Cell(1, 4));
        assertNotNull(path);
        assertEquals(Arrays.asList(new Cell(1, 1), new Cell(0, 1),
                                   new Cell(0, 4), new Cell(1, 4)), path);
    }

    @Test
    public void checkPath_canBaLanRe_khongNoiDuoc() {
        Board board = TestBoards.of(
            "0 0 0 0 0 0",
            "0 1 2 3 4 0",
            "0 0 0 0 5 0",
            "0 6 7 0 1 0",
            "0 8 9 2 3 0",
            "0 0 0 0 0 0");
        assertNull(PathFinder.checkPath(board, new Cell(1, 1), new Cell(3, 4)));
    }
}
```

Test DAO có ghi dữ liệu luôn rollback:

```java
public class MatchDAOTest {
    private final MatchDAO dao = new MatchDAO();

    @Test
    public void saveMatch_hopLe() throws SQLException {
        Match match = TestData.matchHoangNamThangMinhAnh();
        try {
            DAO.con.setAutoCommit(false);
            assertTrue(dao.saveMatch(match));
            assertEquals(2, TestData.countResultsOfMatch(match.getId()));
        } finally {
            DAO.con.rollback();
            DAO.con.setAutoCommit(true);
        }
    }
}
```
