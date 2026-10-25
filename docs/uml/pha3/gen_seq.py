"""Sinh kịch bản v3 và biểu đồ tuần tự pha thiết kế từ cùng một danh sách bước.

Mỗi bước là (kiểu, nguồn, đích, nhãn, câu kịch bản):
- "call": nguồn gọi đích (nét liền).
- "self": nguồn tự gọi phương thức của mình, mở một khung kích hoạt lồng nhau.
- "ret":  nguồn trả kết quả cho đích (nét đứt), đóng mọi khung kích hoạt của nguồn.
- "msg":  thông điệp một chiều: thao tác của người chơi, hiển thị, gửi qua TCP.
("loop", "điều kiện") mở khung lặp, ("end",) đóng khung lặp,
("deact", lớp) đóng khung kích hoạt của một phương thức không có bước trả về.

Dùng: python gen_seq.py  -> ghi seq-*.puml và thay các khối
<!-- BEGIN v3-xx --> ... <!-- END v3-xx --> trong docs/pha3-thiet-ke.md.
Sửa kịch bản v3 ở đây, không sửa tay trong tài liệu.
"""
import re
from pathlib import Path

HERE = Path(__file__).parent
DOC = HERE.parent.parent / "pha3-thiet-ke.md"

STYLE = """skinparam shadowing false
skinparam defaultFontSize 12
skinparam sequenceMessageAlign center
skinparam responseMessageBelowArrow true
skinparam ArrowColor #4A4A4A
skinparam sequence {
  LifeLineBorderColor #4A4A4A
  ParticipantBorderColor #4A4A4A
  ParticipantBackgroundColor #F7F7F7
  ActorBorderColor #4A4A4A
  ActorBackgroundColor #F7F7F7
}
autonumber "<b>0:"
"""

# ---------------------------------------------------------------- M2
M2_PARTS = [
    ("actor", "Player", "Người chơi"),
    ("boundary", "LoginActivity", None),
    ("boundary", "MainActivity", None),
    ("boundary", "RankingActivity", None),
    ("control", "ServerConnection", None),
    ("control", "ClientHandler", None),
    ("control", "LoginController", None),
    ("control", "OnlinePlayerManager", None),
    ("control", "RankingController", None),
    ("control", "PlayerDAO", None),
    ("control", "PlayerRankingDAO", None),
    ("entity", "PlayerRanking", "Player\\nRanking"),
    ("entity", "PlayerE", "Player"),
]
M2 = [
    ("msg", "Player", "LoginActivity", "nhập, click đăng nhập", "Tại giao diện `LoginActivity`, người chơi nhập tên đăng nhập, mật khẩu và click nút Đăng nhập."),
    ("call", "LoginActivity", "ServerConnection", "send(LOGIN)", "Lớp `LoginActivity` gọi lớp `ServerConnection` yêu cầu gửi message `LOGIN`."),
    ("msg", "ServerConnection", "ClientHandler", "LOGIN (TCP)", "Lớp `ServerConnection` gửi message `LOGIN` qua TCP tới lớp `ClientHandler` trên server."),
    ("call", "ClientHandler", "LoginController", "handleLogin()", "Lớp `ClientHandler` gọi hàm `handleLogin()` của lớp `LoginController`."),
    ("call", "LoginController", "PlayerDAO", "gọi", "Lớp `LoginController` gọi lớp `PlayerDAO` yêu cầu kiểm tra đăng nhập."),
    ("self", "PlayerDAO", None, "checkLogin()", "Lớp `PlayerDAO` gọi hàm `checkLogin()`."),
    ("call", "PlayerDAO", "PlayerE", "gọi", "Hàm `checkLogin()` gọi lớp `Player` để đóng gói thông tin."),
    ("self", "PlayerE", None, "Player()", "Lớp `Player` đóng gói thông tin thực thể."),
    ("ret", "PlayerE", "PlayerDAO", "trả về", "Lớp `Player` trả kết quả cho hàm `checkLogin()`."),
    ("ret", "PlayerDAO", "LoginController", "trả về", "Hàm `checkLogin()` trả kết quả cho lớp `LoginController`."),
    ("call", "LoginController", "OnlinePlayerManager", "gọi", "Lớp `LoginController` gọi lớp `OnlinePlayerManager` yêu cầu thêm người chơi vào danh sách online."),
    ("self", "OnlinePlayerManager", None, "addOnlinePlayer()", "Lớp `OnlinePlayerManager` gọi hàm `addOnlinePlayer()`."),
    ("ret", "OnlinePlayerManager", "LoginController", "trả về", "Hàm `addOnlinePlayer()` trả kết quả cho lớp `LoginController`."),
    ("call", "LoginController", "ClientHandler", "send(LOGIN_RESULT)", "Lớp `LoginController` gọi lớp `ClientHandler` gửi message `LOGIN_RESULT`."),
    ("msg", "ClientHandler", "ServerConnection", "LOGIN_RESULT (TCP)", "Lớp `ClientHandler` gửi message `LOGIN_RESULT` qua TCP tới lớp `ServerConnection`."),
    ("call", "ServerConnection", "LoginActivity", "onMessage()", "Lớp `ServerConnection` chuyển message cho lớp `LoginActivity`."),
    ("call", "LoginActivity", "MainActivity", "gọi", "Lớp `LoginActivity` lưu người chơi vào `ClientSession` và gọi lớp `MainActivity`."),
    ("msg", "MainActivity", "Player", "hiển thị", "Lớp `MainActivity` hiển thị giao diện chính cho người chơi."),
    ("msg", "Player", "MainActivity", "click xem bảng xếp hạng", "Người chơi click nút Xem bảng xếp hạng."),
    ("call", "MainActivity", "RankingActivity", "gọi", "Lớp `MainActivity` gọi lớp `RankingActivity`."),
    ("call", "RankingActivity", "ServerConnection", "send(GET_RANKING)", "Lớp `RankingActivity` gọi lớp `ServerConnection` yêu cầu gửi message `GET_RANKING`."),
    ("msg", "ServerConnection", "ClientHandler", "GET_RANKING (TCP)", "Lớp `ServerConnection` gửi message `GET_RANKING` qua TCP tới lớp `ClientHandler`."),
    ("call", "ClientHandler", "RankingController", "handleGetRanking()", "Lớp `ClientHandler` gọi hàm `handleGetRanking()` của lớp `RankingController`."),
    ("call", "RankingController", "PlayerRankingDAO", "gọi", "Lớp `RankingController` gọi lớp `PlayerRankingDAO` yêu cầu lấy bảng xếp hạng."),
    ("self", "PlayerRankingDAO", None, "getRanking()", "Lớp `PlayerRankingDAO` gọi hàm `getRanking()`."),
    ("call", "PlayerRankingDAO", "PlayerRanking", "gọi", "Hàm `getRanking()` gọi lớp `PlayerRanking` để đóng gói thông tin."),
    ("self", "PlayerRanking", None, "PlayerRanking()", "Lớp `PlayerRanking` đóng gói thông tin thực thể."),
    ("ret", "PlayerRanking", "PlayerRankingDAO", "trả về", "Lớp `PlayerRanking` trả kết quả cho hàm `getRanking()`."),
    ("ret", "PlayerRankingDAO", "RankingController", "trả về", "Hàm `getRanking()` trả danh sách đã sắp xếp cho lớp `RankingController`."),
    ("call", "RankingController", "ClientHandler", "send(RANKING)", "Lớp `RankingController` gọi lớp `ClientHandler` gửi message `RANKING`."),
    ("msg", "ClientHandler", "ServerConnection", "RANKING (TCP)", "Lớp `ClientHandler` gửi message `RANKING` qua TCP tới lớp `ServerConnection`."),
    ("call", "ServerConnection", "RankingActivity", "onMessage()", "Lớp `ServerConnection` chuyển message cho lớp `RankingActivity`."),
    ("msg", "RankingActivity", "Player", "hiển thị", "Lớp `RankingActivity` hiển thị bảng xếp hạng cho người chơi."),
    ("msg", "Player", "RankingActivity", "click quay lại", "Người chơi click nút Quay lại."),
    ("call", "RankingActivity", "MainActivity", "gọi", "Lớp `RankingActivity` gọi lại lớp `MainActivity`."),
    ("msg", "MainActivity", "Player", "hiển thị", "Lớp `MainActivity` hiển thị giao diện chính cho người chơi."),
]

# ---------------------------------------------------------------- M3
M3_PARTS = [
    ("actor", "Player", "Người chơi"),
    ("boundary", "MainActivity", None),
    ("boundary", "DifficultyActivity", "Difficulty\\nActivity"),
    ("boundary", "BotGameActivity", "BotGame\\nActivity"),
    ("boundary", "BoardView", None),
    ("boundary", "BotResultDialog", "BotResult\\nDialog"),
    ("control", "ServerConnection", "Server\\nConnection"),
    ("control", "BoardGenerator", None),
    ("control", "PathFinder", None),
    ("control", "Bot", None),
    ("entity", "Board", None),
]
M3 = [
    ("msg", "Player", "MainActivity", "click chơi với máy", "Tại giao diện chính, người chơi click nút Chơi với máy."),
    ("call", "MainActivity", "DifficultyActivity", "gọi", "Lớp `MainActivity` gọi lớp `DifficultyActivity`."),
    ("msg", "DifficultyActivity", "Player", "hiển thị", "Lớp `DifficultyActivity` hiển thị các lựa chọn độ khó cho người chơi."),
    ("msg", "Player", "DifficultyActivity", "chọn độ khó, click bắt đầu", "Người chơi chọn độ khó Trung bình và click nút Bắt đầu."),
    ("call", "DifficultyActivity", "BotGameActivity", "gọi", "Lớp `DifficultyActivity` gọi lớp `BotGameActivity`, kèm độ khó đã chọn."),
    ("call", "BotGameActivity", "BoardGenerator", "gọi", "Lớp `BotGameActivity` gọi lớp `BoardGenerator` yêu cầu sinh đề bài 10x10."),
    ("self", "BoardGenerator", None, "generateBoard()", "Lớp `BoardGenerator` gọi hàm `generateBoard()`."),
    ("call", "BoardGenerator", "Board", "gọi", "Hàm `generateBoard()` gọi lớp `Board` để đóng gói đề bài."),
    ("self", "Board", None, "Board()", "Lớp `Board` đóng gói thông tin thực thể."),
    ("ret", "Board", "BoardGenerator", "trả về", "Lớp `Board` trả kết quả cho hàm `generateBoard()`."),
    ("ret", "BoardGenerator", "BotGameActivity", "trả về", "Hàm `generateBoard()` trả đề bài cho lớp `BotGameActivity`."),
    ("call", "BotGameActivity", "Board", "gọi", "Lớp `BotGameActivity` gọi lớp `Board` yêu cầu tạo bản sao đề bài cho máy."),
    ("self", "Board", None, "copy()", "Lớp `Board` gọi hàm `copy()`."),
    ("ret", "Board", "BotGameActivity", "trả về", "Hàm `copy()` trả bàn của máy cho lớp `BotGameActivity`."),
    ("call", "BotGameActivity", "Bot", "gọi", "Lớp `BotGameActivity` gọi lớp `Bot` yêu cầu khởi tạo máy theo độ khó."),
    ("self", "Bot", None, "Bot()", "Lớp `Bot` gọi hàm khởi tạo `Bot()`."),
    ("ret", "Bot", "BotGameActivity", "trả về", "Lớp `Bot` trả đối tượng máy cho lớp `BotGameActivity`."),
    ("call", "BotGameActivity", "ServerConnection", "send(UPDATE_STATUS)", "Lớp `BotGameActivity` gọi lớp `ServerConnection` yêu cầu gửi message `UPDATE_STATUS` với trạng thái luyện tập."),
    ("call", "BotGameActivity", "BoardView", "setBoard()", "Lớp `BotGameActivity` gọi hàm `setBoard()` của lớp `BoardView` và bắt đầu đồng hồ."),
    ("msg", "BoardView", "Player", "hiển thị", "Lớp `BoardView` hiển thị bàn chơi, đồng hồ và số ô còn lại cho người chơi."),
    ("loop", "lặp đến khi một bên xóa hết ô hoặc hết giờ"),
    ("msg", "Player", "BoardView", "click hai ô", "Người chơi click lần lượt hai ô cùng hình."),
    ("call", "BoardView", "BotGameActivity", "onCellClick()", "Lớp `BoardView` gọi hàm `onCellClick()` của lớp `BotGameActivity`."),
    ("call", "BotGameActivity", "PathFinder", "gọi", "Lớp `BotGameActivity` gọi lớp `PathFinder` yêu cầu kiểm tra đường nối giữa hai ô."),
    ("self", "PathFinder", None, "checkPath()", "Lớp `PathFinder` gọi hàm `checkPath()`."),
    ("ret", "PathFinder", "BotGameActivity", "trả về", "Hàm `checkPath()` trả đường nối cho lớp `BotGameActivity`."),
    ("call", "BotGameActivity", "Board", "gọi", "Lớp `BotGameActivity` gọi lớp `Board` yêu cầu xóa hai ô."),
    ("self", "Board", None, "removePair()", "Lớp `Board` gọi hàm `removePair()`."),
    ("ret", "Board", "BotGameActivity", "trả về", "Hàm `removePair()` trả kết quả cho lớp `BotGameActivity`."),
    ("call", "BotGameActivity", "Board", "gọi", "Lớp `BotGameActivity` gọi lớp `Board` yêu cầu kiểm tra bàn đã hết ô chưa."),
    ("self", "Board", None, "isCleared()", "Lớp `Board` gọi hàm `isCleared()`."),
    ("ret", "Board", "BotGameActivity", "trả về", "Hàm `isCleared()` trả kết quả cho lớp `BotGameActivity`."),
    ("call", "BotGameActivity", "PathFinder", "gọi", "Lớp `BotGameActivity` gọi lớp `PathFinder` yêu cầu tìm một cặp còn nối được để kiểm tra bế tắc."),
    ("self", "PathFinder", None, "findAvailablePair()", "Lớp `PathFinder` gọi hàm `findAvailablePair()`."),
    ("ret", "PathFinder", "BotGameActivity", "trả về", "Hàm `findAvailablePair()` trả cặp ô cho lớp `BotGameActivity` (nếu trả về rỗng thì lớp `BotGameActivity` gọi `BoardGenerator.shuffle()` để xáo bàn)."),
    ("call", "BotGameActivity", "BoardView", "showPath()", "Lớp `BotGameActivity` gọi hàm `showPath()` của lớp `BoardView`."),
    ("msg", "BoardView", "Player", "hiển thị", "Lớp `BoardView` vẽ đường nối, hiển thị bàn chơi mới và số ô còn lại cho người chơi."),
    ("call", "BotGameActivity", "Bot", "gọi", "Sau mỗi khoảng `getDelayMs()`, lớp `BotGameActivity` gọi lớp `Bot` yêu cầu nước đi của máy."),
    ("self", "Bot", None, "nextMove()", "Lớp `Bot` gọi hàm `nextMove()`."),
    ("call", "Bot", "PathFinder", "gọi", "Hàm `nextMove()` gọi lớp `PathFinder` yêu cầu tìm một cặp nối được trên bàn của máy."),
    ("self", "PathFinder", None, "findAvailablePair()", "Lớp `PathFinder` gọi hàm `findAvailablePair()`."),
    ("ret", "PathFinder", "Bot", "trả về", "Hàm `findAvailablePair()` trả cặp ô cho hàm `nextMove()`."),
    ("ret", "Bot", "BotGameActivity", "trả về", "Hàm `nextMove()` trả nước đi cho lớp `BotGameActivity`."),
    ("call", "BotGameActivity", "Board", "gọi", "Lớp `BotGameActivity` gọi lớp `Board` của máy yêu cầu xóa cặp ô đó."),
    ("self", "Board", None, "removePair()", "Lớp `Board` gọi hàm `removePair()`."),
    ("ret", "Board", "BotGameActivity", "trả về", "Hàm `removePair()` trả kết quả cho lớp `BotGameActivity`."),
    ("msg", "BotGameActivity", "Player", "hiển thị", "Lớp `BotGameActivity` vẽ nước đi của máy lên bàn thu nhỏ của máy và hiển thị số ô còn lại của máy cho người chơi."),
    ("end",),
    ("call", "BotGameActivity", "ServerConnection", "send(UPDATE_STATUS)", "Lớp `BotGameActivity` dừng đồng hồ, xác định kết quả và gọi lớp `ServerConnection` yêu cầu gửi message `UPDATE_STATUS` với trạng thái rỗi."),
    ("call", "BotGameActivity", "BotResultDialog", "gọi", "Lớp `BotGameActivity` gọi lớp `BotResultDialog`."),
    ("msg", "BotResultDialog", "Player", "hiển thị", "Lớp `BotResultDialog` hiển thị kết quả ván cho người chơi."),
    ("msg", "Player", "BotResultDialog", "click OK", "Người chơi click nút OK."),
    ("call", "BotResultDialog", "MainActivity", "gọi", "Lớp `BotResultDialog` gọi lại lớp `MainActivity`."),
    ("msg", "MainActivity", "Player", "hiển thị", "Lớp `MainActivity` hiển thị giao diện chính cho người chơi."),
]
M3_LOOP_NOTE = "(Lặp lại các bước {a} đến {b} cho đến khi một bên xóa hết ô hoặc hết giờ.)"

# ---------------------------------------------------------------- M3
M4A_PARTS = [
    ("actor", "A", "Người chơi A"),
    ("boundary", "MainActivity", None),
    ("boundary", "ChallengeConfirmDialog", "ChallengeConfirm\\nDialog"),
    ("boundary", "InvitationDialog", "Invitation\\nDialog"),
    ("boundary", "MatchActivity", None),
    ("control", "ServerConnection", "Server\\nConnection"),
    ("control", "ClientHandler", "Client\\nHandler"),
    ("control", "ChallengeController", "Challenge\\nController"),
    ("control", "OnlinePlayerManager", "OnlinePlayer\\nManager"),
    ("control", "BoardGenerator", "Board\\nGenerator"),
    ("control", "MatchManager", "Match\\nManager"),
    ("entity", "Board", None),
    ("entity", "Match", None),
    ("actor", "B", "Người chơi B"),
]
M4A = [
    ("msg", "A", "MainActivity", "click tên B", "Tại giao diện chính, người chơi A click vào tên người chơi B (không bận: rỗi hoặc luyện tập)."),
    ("call", "MainActivity", "ChallengeConfirmDialog", "gọi", "Lớp `MainActivity` gọi lớp `ChallengeConfirmDialog`."),
    ("msg", "ChallengeConfirmDialog", "A", "hiển thị", "Lớp `ChallengeConfirmDialog` hiển thị dialog xác nhận thách đấu B cho A."),
    ("msg", "A", "ChallengeConfirmDialog", "click xác nhận", "A click nút Xác nhận."),
    ("ret", "ChallengeConfirmDialog", "MainActivity", "xác nhận", "Lớp `ChallengeConfirmDialog` trả kết quả xác nhận cho lớp `MainActivity`."),
    ("call", "MainActivity", "ServerConnection", "send(INVITE)", "Lớp `MainActivity` gọi lớp `ServerConnection` yêu cầu gửi message `INVITE`."),
    ("msg", "MainActivity", "A", "hiển thị", "Lớp `MainActivity` hiển thị thông báo đang chờ B trả lời cho A."),
    ("msg", "ServerConnection", "ClientHandler", "INVITE (TCP)", "Lớp `ServerConnection` gửi message `INVITE` qua TCP tới lớp `ClientHandler` của A trên server."),
    ("call", "ClientHandler", "ChallengeController", "handleInvite()", "Lớp `ClientHandler` gọi hàm `handleInvite()` của lớp `ChallengeController`."),
    ("call", "ChallengeController", "OnlinePlayerManager", "gọi", "Lớp `ChallengeController` gọi lớp `OnlinePlayerManager` yêu cầu gửi lời mời từ A tới B."),
    ("self", "OnlinePlayerManager", None, "sendInvitation()", "Lớp `OnlinePlayerManager` gọi hàm `sendInvitation()`, kiểm tra B đang online và không bận."),
    ("call", "OnlinePlayerManager", "ClientHandler", "send(INVITE_RECEIVED)", "Hàm `sendInvitation()` gọi lớp `ClientHandler` của B gửi message `INVITE_RECEIVED`."),
    ("ret", "OnlinePlayerManager", "ChallengeController", "trả về", "Hàm `sendInvitation()` trả kết quả cho lớp `ChallengeController`."),
    ("msg", "ClientHandler", "ServerConnection", "INVITE_RECEIVED (TCP)", "Lớp `ClientHandler` của B gửi message `INVITE_RECEIVED` qua TCP tới lớp `ServerConnection` trên máy B."),
    ("call", "ServerConnection", "MainActivity", "onMessage()", "Lớp `ServerConnection` chuyển message cho lớp `MainActivity` trên máy B."),
    ("call", "MainActivity", "InvitationDialog", "gọi", "Lớp `MainActivity` gọi lớp `InvitationDialog`."),
    ("msg", "InvitationDialog", "B", "hiển thị", "Lớp `InvitationDialog` hiển thị lời mời cho B."),
    ("msg", "B", "InvitationDialog", "click OK", "B click nút OK."),
    ("call", "InvitationDialog", "ServerConnection", "send(INVITE_REPLY)", "Lớp `InvitationDialog` gọi lớp `ServerConnection` yêu cầu gửi message `INVITE_REPLY` chấp nhận."),
    ("msg", "ServerConnection", "ClientHandler", "INVITE_REPLY (TCP)", "Lớp `ServerConnection` gửi message `INVITE_REPLY` qua TCP tới lớp `ClientHandler` của B."),
    ("call", "ClientHandler", "ChallengeController", "handleInviteReply()", "Lớp `ClientHandler` gọi hàm `handleInviteReply()` của lớp `ChallengeController`."),
    ("call", "ChallengeController", "BoardGenerator", "gọi", "Lớp `ChallengeController` gọi lớp `BoardGenerator` yêu cầu sinh đề bài 10x10."),
    ("self", "BoardGenerator", None, "generateBoard()", "Lớp `BoardGenerator` gọi hàm `generateBoard()`."),
    ("call", "BoardGenerator", "Board", "gọi", "Hàm `generateBoard()` gọi lớp `Board` để đóng gói đề bài."),
    ("self", "Board", None, "Board()", "Lớp `Board` đóng gói thông tin thực thể."),
    ("ret", "Board", "BoardGenerator", "trả về", "Lớp `Board` trả kết quả cho hàm `generateBoard()`."),
    ("ret", "BoardGenerator", "ChallengeController", "trả về", "Hàm `generateBoard()` trả đề bài cho lớp `ChallengeController`."),
    ("call", "ChallengeController", "MatchManager", "gọi", "Lớp `ChallengeController` gọi lớp `MatchManager` yêu cầu tạo ván đấu cho A và B với đề bài vừa sinh."),
    ("self", "MatchManager", None, "createMatch()", "Lớp `MatchManager` gọi hàm `createMatch()`, tạo phòng chơi giữ hai bản sao đề bài cho hai người."),
    ("call", "MatchManager", "Match", "gọi", "Hàm `createMatch()` gọi lớp `Match` để đóng gói ván đấu."),
    ("self", "Match", None, "Match()", "Lớp `Match` đóng gói thông tin thực thể."),
    ("ret", "Match", "MatchManager", "trả về", "Lớp `Match` trả kết quả cho hàm `createMatch()`."),
    ("ret", "MatchManager", "ChallengeController", "trả về", "Hàm `createMatch()` trả phòng chơi cho lớp `ChallengeController`."),
    ("call", "ChallengeController", "OnlinePlayerManager", "gọi", "Lớp `ChallengeController` gọi lớp `OnlinePlayerManager` yêu cầu chuyển A và B sang bận."),
    ("self", "OnlinePlayerManager", None, "updateStatus()", "Lớp `OnlinePlayerManager` gọi hàm `updateStatus()`."),
    ("ret", "OnlinePlayerManager", "ChallengeController", "trả về", "Hàm `updateStatus()` trả kết quả cho lớp `ChallengeController`."),
    ("call", "ChallengeController", "ClientHandler", "send(MATCH_START)", "Lớp `ChallengeController` gọi lớp `ClientHandler` của A và của B gửi message `MATCH_START`."),
    ("msg", "ClientHandler", "ServerConnection", "MATCH_START (TCP)", "Lớp `ClientHandler` gửi message `MATCH_START` qua TCP tới lớp `ServerConnection` trên cả hai máy."),
    ("call", "ServerConnection", "MainActivity", "onMessage()", "Lớp `ServerConnection` chuyển message cho lớp `MainActivity` trên cả hai máy."),
    ("call", "MainActivity", "MatchActivity", "gọi", "Lớp `MainActivity` gọi lớp `MatchActivity`, kèm đề bài và thông tin đối thủ."),
    ("msg", "MatchActivity", "A", "hiển thị", "Lớp `MatchActivity` hiển thị bàn chơi, đồng hồ và số ô còn lại của đối thủ cho A và B."),
]

M4BC_PARTS = [  # phần 2 và 3; mỗi phần chỉ giữ lớp có mặt trong các bước của nó
    ("actor", "A", "Người chơi A"),
    ("boundary", "MainActivity", None),
    ("boundary", "MatchActivity", None),
    ("boundary", "ResultDialog", None),
    ("control", "ServerConnection", "Server\\nConnection"),
    ("control", "ClientHandler", "Client\\nHandler"),
    ("control", "MatchController", "Match\\nController"),
    ("control", "ChallengeController", "Challenge\\nController"),
    ("control", "MatchManager", "Match\\nManager"),
    ("control", "OnlinePlayerManager", "OnlinePlayer\\nManager"),
    ("control", "PathFinder", None),
    ("control", "MatchDAO", None),
    ("control", "PlayerRankingDAO", "PlayerRanking\\nDAO"),
    ("entity", "Board", None),
    ("entity", "PlayerRanking", "Player\\nRanking"),
]
M4B = [
    ("loop", "lặp đến khi một người xóa hết ô"),
    ("msg", "A", "MatchActivity", "click hai ô", "A click lần lượt hai ô cùng hình."),
    ("call", "MatchActivity", "ServerConnection", "send(MOVE)", "Lớp `MatchActivity` gọi lớp `ServerConnection` yêu cầu gửi message `MOVE`."),
    ("msg", "ServerConnection", "ClientHandler", "MOVE (TCP)", "Lớp `ServerConnection` gửi message `MOVE` qua TCP tới lớp `ClientHandler` của A."),
    ("call", "ClientHandler", "MatchController", "handleMove()", "Lớp `ClientHandler` gọi hàm `handleMove()` của lớp `MatchController`."),
    ("call", "MatchController", "MatchManager", "gọi", "Lớp `MatchController` gọi lớp `MatchManager` yêu cầu lấy phòng chơi của ván."),
    ("self", "MatchManager", None, "getRoom()", "Lớp `MatchManager` gọi hàm `getRoom()`."),
    ("ret", "MatchManager", "MatchController", "trả về", "Hàm `getRoom()` trả phòng chơi cho lớp `MatchController`, từ đó lấy bàn của A."),
    ("call", "MatchController", "PathFinder", "gọi", "Lớp `MatchController` gọi lớp `PathFinder` yêu cầu kiểm tra đường nối trên bàn của A."),
    ("self", "PathFinder", None, "checkPath()", "Lớp `PathFinder` gọi hàm `checkPath()`."),
    ("ret", "PathFinder", "MatchController", "trả về", "Hàm `checkPath()` trả đường nối cho lớp `MatchController`."),
    ("call", "MatchController", "Board", "gọi", "Lớp `MatchController` gọi lớp `Board` của A yêu cầu xóa hai ô."),
    ("self", "Board", None, "removePair()", "Lớp `Board` gọi hàm `removePair()`."),
    ("ret", "Board", "MatchController", "trả về", "Hàm `removePair()` trả kết quả cho lớp `MatchController`."),
    ("call", "MatchController", "Board", "gọi", "Lớp `MatchController` gọi lớp `Board` yêu cầu kiểm tra bàn của A đã hết ô chưa."),
    ("self", "Board", None, "isCleared()", "Lớp `Board` gọi hàm `isCleared()`."),
    ("ret", "Board", "MatchController", "trả về", "Hàm `isCleared()` trả kết quả cho lớp `MatchController`."),
    ("call", "MatchController", "PathFinder", "gọi", "Lớp `MatchController` gọi lớp `PathFinder` yêu cầu tìm một cặp còn nối được trên bàn của A để kiểm tra bế tắc."),
    ("self", "PathFinder", None, "findAvailablePair()", "Lớp `PathFinder` gọi hàm `findAvailablePair()`."),
    ("ret", "PathFinder", "MatchController", "trả về", "Hàm `findAvailablePair()` trả cặp ô cho lớp `MatchController` (nếu trả về rỗng thì lớp `MatchController` gọi `BoardGenerator.shuffle()` xáo riêng bàn của A và gửi `BOARD_SHUFFLED` cho A)."),
    ("call", "MatchController", "ClientHandler", "send(MOVE_RESULT)", "Lớp `MatchController` gọi lớp `ClientHandler` của A gửi message `MOVE_RESULT` và lớp `ClientHandler` của B gửi message `OPPONENT_PROGRESS`."),
    ("msg", "ClientHandler", "ServerConnection", "MOVE_RESULT,\\nOPPONENT_PROGRESS (TCP)", "Lớp `ClientHandler` gửi các message qua TCP tới lớp `ServerConnection` trên hai máy."),
    ("call", "ServerConnection", "MatchActivity", "onMessage()", "Lớp `ServerConnection` chuyển message cho lớp `MatchActivity` trên hai máy."),
    ("msg", "MatchActivity", "A", "hiển thị", "Lớp `MatchActivity` vẽ đường nối và bàn chơi mới cho A; trên máy B, vẽ nước đi của A lên bàn thu nhỏ của đối thủ và cập nhật số ô còn lại của A."),
    ("end",),
]

# Phần 3 (Dev 2): kết thúc ván và tái đấu. Ảnh dùng trong tài liệu vẽ bằng Visual Paradigm (docs/vp/build_vp.py).
M4C = [
    ("self", "MatchController", None, "endMatch()", "Khi bàn của A đã hết ô, lớp `MatchController` gọi hàm `endMatch()`, tính kết quả và điểm của hai người."),
    ("call", "MatchController", "MatchDAO", "gọi", "Hàm `endMatch()` gọi lớp `MatchDAO` yêu cầu lưu ván đấu cùng hai kết quả."),
    ("self", "MatchDAO", None, "saveMatch()", "Lớp `MatchDAO` gọi hàm `saveMatch()`."),
    ("ret", "MatchDAO", "MatchController", "trả về", "Hàm `saveMatch()` trả kết quả cho hàm `endMatch()`."),
    ("call", "MatchController", "ClientHandler", "send(MATCH_END)", "Hàm `endMatch()` gọi lớp `ClientHandler` của A và của B gửi message `MATCH_END`."),
    ("deact", "MatchController"),
    ("msg", "ClientHandler", "ServerConnection", "MATCH_END (TCP)", "Lớp `ClientHandler` gửi message `MATCH_END` qua TCP tới lớp `ServerConnection` trên hai máy."),
    ("call", "ServerConnection", "MatchActivity", "onMessage()", "Lớp `ServerConnection` chuyển message cho lớp `MatchActivity` trên hai máy."),
    ("call", "MatchActivity", "ResultDialog", "gọi", "Lớp `MatchActivity` gọi lớp `ResultDialog`."),
    ("msg", "ResultDialog", "A", "hiển thị", "Lớp `ResultDialog` hiển thị kết quả và câu hỏi có muốn tiếp tục không cho A và B."),
    ("msg", "A", "ResultDialog", "click dừng", "A click nút Dừng."),
    ("call", "ResultDialog", "ServerConnection", "send(REMATCH_REPLY)", "Lớp `ResultDialog` gọi lớp `ServerConnection` yêu cầu gửi message `REMATCH_REPLY` từ chối."),
    ("call", "ResultDialog", "MainActivity", "gọi", "Lớp `ResultDialog` gọi lớp `MainActivity` trên máy A."),
    ("msg", "ServerConnection", "ClientHandler", "REMATCH_REPLY (TCP)", "Lớp `ServerConnection` gửi message `REMATCH_REPLY` qua TCP tới lớp `ClientHandler` của A."),
    ("call", "ClientHandler", "MatchController", "handleRematchReply()", "Lớp `ClientHandler` gọi hàm `handleRematchReply()` của lớp `MatchController`."),
    ("call", "MatchController", "OnlinePlayerManager", "gọi", "Lớp `MatchController` gọi lớp `OnlinePlayerManager` yêu cầu chuyển A và B về rỗi."),
    ("self", "OnlinePlayerManager", None, "updateStatus()", "Lớp `OnlinePlayerManager` gọi hàm `updateStatus()`."),
    ("ret", "OnlinePlayerManager", "MatchController", "trả về", "Hàm `updateStatus()` trả kết quả cho lớp `MatchController`."),
    ("call", "MatchController", "ClientHandler", "send(OPPONENT_LEFT)", "Lớp `MatchController` gọi lớp `ClientHandler` của B gửi message `OPPONENT_LEFT`."),
    ("call", "MatchController", "ChallengeController", "gọi", "Lớp `MatchController` gọi lớp `ChallengeController` yêu cầu gửi lại danh sách người chơi online."),
    ("self", "ChallengeController", None, "broadcastOnlineList()", "Lớp `ChallengeController` gọi hàm `broadcastOnlineList()`."),
    ("call", "ChallengeController", "OnlinePlayerManager", "gọi", "Hàm `broadcastOnlineList()` gọi lớp `OnlinePlayerManager` yêu cầu lấy danh sách người chơi online."),
    ("self", "OnlinePlayerManager", None, "getOnlinePlayers()", "Lớp `OnlinePlayerManager` gọi hàm `getOnlinePlayers()`."),
    ("ret", "OnlinePlayerManager", "ChallengeController", "trả về", "Hàm `getOnlinePlayers()` trả danh sách cho hàm `broadcastOnlineList()`."),
    ("call", "ChallengeController", "PlayerRankingDAO", "gọi", "Hàm `broadcastOnlineList()` gọi lớp `PlayerRankingDAO` yêu cầu lấy tổng điểm của các người chơi trong danh sách."),
    ("self", "PlayerRankingDAO", None, "getTotalPoints()", "Lớp `PlayerRankingDAO` gọi hàm `getTotalPoints()`."),
    ("call", "PlayerRankingDAO", "PlayerRanking", "gọi", "Hàm `getTotalPoints()` gọi lớp `PlayerRanking` để đóng gói thông tin."),
    ("self", "PlayerRanking", None, "PlayerRanking()", "Lớp `PlayerRanking` đóng gói thông tin thực thể."),
    ("ret", "PlayerRanking", "PlayerRankingDAO", "trả về", "Lớp `PlayerRanking` trả kết quả cho hàm `getTotalPoints()`."),
    ("ret", "PlayerRankingDAO", "ChallengeController", "trả về", "Hàm `getTotalPoints()` trả kết quả cho hàm `broadcastOnlineList()`."),
    ("call", "ChallengeController", "ClientHandler", "send(ONLINE_LIST)", "Hàm `broadcastOnlineList()` gọi lớp `ClientHandler` của mọi người chơi rỗi gửi message `ONLINE_LIST`."),
    ("deact", "ChallengeController"),
    ("msg", "ClientHandler", "ServerConnection", "OPPONENT_LEFT,\\nONLINE_LIST (TCP)", "Lớp `ClientHandler` gửi các message qua TCP tới lớp `ServerConnection` trên hai máy."),
    ("call", "ServerConnection", "ResultDialog", "onMessage()", "Lớp `ServerConnection` chuyển message `OPPONENT_LEFT` cho lớp `ResultDialog` trên máy B."),
    ("call", "ResultDialog", "MainActivity", "gọi", "Lớp `ResultDialog` gọi lớp `MainActivity` trên máy B."),
    ("call", "ServerConnection", "MainActivity", "onMessage()", "Lớp `ServerConnection` chuyển message `ONLINE_LIST` cho lớp `MainActivity` trên hai máy."),
    ("msg", "MainActivity", "A", "hiển thị", "Lớp `MainActivity` hiển thị danh sách người chơi online đã cập nhật điểm cho A và B, kèm thông báo \"A đã dừng chơi\" cho B."),
]

# ---------------------------------------------------------------- M1
M1_PARTS = [
    ("actor", "Player", "Người chơi"),
    ("boundary", "RegisterActivity", "Register\\nActivity"),
    ("boundary", "LoginActivity", None),
    ("boundary", "MainActivity", None),
    ("control", "ServerConnection", "Server\\nConnection"),
    ("control", "ClientHandler", "Client\\nHandler"),
    ("control", "LoginController", "Login\\nController"),
    ("control", "OnlinePlayerManager", "OnlinePlayer\\nManager"),
    ("control", "ChallengeController", "Challenge\\nController"),
    ("control", "PlayerDAO", None),
    ("entity", "PlayerE", "Player"),
]
M1 = [
    ("msg", "Player", "LoginActivity", "click đăng ký", "Tại giao diện `LoginActivity`, người chơi click nút Đăng ký."),
    ("call", "LoginActivity", "RegisterActivity", "gọi", "Lớp `LoginActivity` gọi lớp `RegisterActivity`."),
    ("msg", "RegisterActivity", "Player", "hiển thị", "Lớp `RegisterActivity` hiển thị giao diện đăng ký cho người chơi."),
    ("msg", "Player", "RegisterActivity", "nhập, click đăng ký", "Người chơi nhập tên đăng nhập, mật khẩu, nhập lại mật khẩu, tên hiển thị và click nút Đăng ký."),
    ("call", "RegisterActivity", "ServerConnection", "send(REGISTER)", "Lớp `RegisterActivity` kiểm tra không ô nào rỗng và mật khẩu nhập lại khớp, rồi gọi lớp `ServerConnection` yêu cầu gửi message `REGISTER`."),
    ("msg", "ServerConnection", "ClientHandler", "REGISTER (TCP)", "Lớp `ServerConnection` gửi message `REGISTER` qua TCP tới lớp `ClientHandler` trên server."),
    ("call", "ClientHandler", "LoginController", "handleRegister()", "Lớp `ClientHandler` gọi hàm `handleRegister()` của lớp `LoginController`."),
    ("call", "LoginController", "PlayerDAO", "gọi", "Lớp `LoginController` băm mật khẩu và gọi lớp `PlayerDAO` yêu cầu thêm người chơi mới."),
    ("self", "PlayerDAO", None, "addPlayer()", "Lớp `PlayerDAO` gọi hàm `addPlayer()`."),
    ("ret", "PlayerDAO", "LoginController", "trả về", "Hàm `addPlayer()` trả kết quả cho lớp `LoginController` (sai nếu tên đăng nhập đã tồn tại)."),
    ("call", "LoginController", "ClientHandler", "send(REGISTER_RESULT)", "Lớp `LoginController` gọi lớp `ClientHandler` gửi message `REGISTER_RESULT`."),
    ("msg", "ClientHandler", "ServerConnection", "REGISTER_RESULT (TCP)", "Lớp `ClientHandler` gửi message `REGISTER_RESULT` qua TCP tới lớp `ServerConnection`."),
    ("call", "ServerConnection", "RegisterActivity", "onMessage()", "Lớp `ServerConnection` chuyển message cho lớp `RegisterActivity`."),
    ("call", "RegisterActivity", "LoginActivity", "gọi", "Lớp `RegisterActivity` gọi lại lớp `LoginActivity`, kèm tên đăng nhập vừa đăng ký."),
    ("msg", "LoginActivity", "Player", "hiển thị", "Lớp `LoginActivity` hiển thị thông báo đăng ký thành công, ô tên đăng nhập đã điền sẵn."),
    ("msg", "Player", "LoginActivity", "nhập mật khẩu, click đăng nhập", "Người chơi nhập mật khẩu và click nút Đăng nhập."),
    ("call", "LoginActivity", "ServerConnection", "send(LOGIN)", "Lớp `LoginActivity` gọi lớp `ServerConnection` yêu cầu gửi message `LOGIN`."),
    ("msg", "ServerConnection", "ClientHandler", "LOGIN (TCP)", "Lớp `ServerConnection` gửi message `LOGIN` qua TCP tới lớp `ClientHandler` trên server."),
    ("call", "ClientHandler", "LoginController", "handleLogin()", "Lớp `ClientHandler` gọi hàm `handleLogin()` của lớp `LoginController`."),
    ("call", "LoginController", "PlayerDAO", "gọi", "Lớp `LoginController` gọi lớp `PlayerDAO` yêu cầu kiểm tra đăng nhập."),
    ("self", "PlayerDAO", None, "checkLogin()", "Lớp `PlayerDAO` gọi hàm `checkLogin()`."),
    ("call", "PlayerDAO", "PlayerE", "gọi", "Hàm `checkLogin()` gọi lớp `Player` để đóng gói thông tin."),
    ("self", "PlayerE", None, "Player()", "Lớp `Player` đóng gói thông tin thực thể."),
    ("ret", "PlayerE", "PlayerDAO", "trả về", "Lớp `Player` trả kết quả cho hàm `checkLogin()`."),
    ("ret", "PlayerDAO", "LoginController", "trả về", "Hàm `checkLogin()` trả kết quả cho lớp `LoginController`."),
    ("call", "LoginController", "OnlinePlayerManager", "gọi", "Lớp `LoginController` gọi lớp `OnlinePlayerManager` yêu cầu thêm người chơi vào danh sách online."),
    ("self", "OnlinePlayerManager", None, "addOnlinePlayer()", "Lớp `OnlinePlayerManager` gọi hàm `addOnlinePlayer()`."),
    ("ret", "OnlinePlayerManager", "LoginController", "trả về", "Hàm `addOnlinePlayer()` trả kết quả cho lớp `LoginController` (sai nếu tài khoản đang online ở máy khác)."),
    ("call", "LoginController", "ClientHandler", "send(LOGIN_RESULT)", "Lớp `LoginController` gọi lớp `ClientHandler` gửi message `LOGIN_RESULT`."),
    ("msg", "ClientHandler", "ServerConnection", "LOGIN_RESULT (TCP)", "Lớp `ClientHandler` gửi message `LOGIN_RESULT` qua TCP tới lớp `ServerConnection`."),
    ("call", "ServerConnection", "LoginActivity", "onMessage()", "Lớp `ServerConnection` chuyển message cho lớp `LoginActivity`."),
    ("call", "LoginActivity", "MainActivity", "gọi", "Lớp `LoginActivity` lưu người chơi vào `ClientSession` và gọi lớp `MainActivity`."),
    ("msg", "MainActivity", "Player", "hiển thị", "Lớp `MainActivity` hiển thị giao diện chính cho người chơi."),
    ("msg", "Player", "MainActivity", "click đăng xuất", "Người chơi click nút Đăng xuất."),
    ("call", "MainActivity", "ServerConnection", "send(LOGOUT)", "Lớp `MainActivity` gọi lớp `ServerConnection` yêu cầu gửi message `LOGOUT`."),
    ("call", "MainActivity", "LoginActivity", "gọi", "Lớp `MainActivity` xóa người chơi khỏi `ClientSession` và gọi lại lớp `LoginActivity`."),
    ("msg", "LoginActivity", "Player", "hiển thị", "Lớp `LoginActivity` hiển thị giao diện đăng nhập cho người chơi."),
    ("msg", "ServerConnection", "ClientHandler", "LOGOUT (TCP)", "Lớp `ServerConnection` gửi message `LOGOUT` qua TCP tới lớp `ClientHandler` trên server."),
    ("call", "ClientHandler", "LoginController", "handleLogout()", "Lớp `ClientHandler` gọi hàm `handleLogout()` của lớp `LoginController`."),
    ("call", "LoginController", "OnlinePlayerManager", "gọi", "Lớp `LoginController` gọi lớp `OnlinePlayerManager` yêu cầu xóa người chơi khỏi danh sách online."),
    ("self", "OnlinePlayerManager", None, "removeOnlinePlayer()", "Lớp `OnlinePlayerManager` gọi hàm `removeOnlinePlayer()`."),
    ("ret", "OnlinePlayerManager", "LoginController", "trả về", "Hàm `removeOnlinePlayer()` trả kết quả cho lớp `LoginController`."),
    ("call", "LoginController", "ChallengeController", "broadcastOnlineList()", "Lớp `LoginController` gọi hàm `broadcastOnlineList()` của lớp `ChallengeController` để gửi lại danh sách online cho những người chơi khác."),
]


def used_parts(parts, steps):
    names = {x for st in steps if st[0] not in ("loop", "end", "deact") for x in st[1:3] if x}
    return [p for p in parts if p[1] in names]


M4B_PARTS = used_parts(M4BC_PARTS, M4B)
M4C_PARTS = used_parts(M4BC_PARTS, M4C)


def puml(name, title, parts, steps, start=1):
    out = [f"@startuml {name}", f"title {title}", "", STYLE.rstrip()]
    if start != 1:
        out.append(f'autonumber {start} "<b>0:"')
    out.append("")
    for kind, alias, label in parts:
        if label:
            out.append(f'{kind} "{label}" as {alias}')
        else:
            out.append(f"{kind} {alias}")
    out.append("")
    depth = {}
    for st in steps:
        if st[0] == "loop":
            out.append(f"loop {st[1]}")
            continue
        if st[0] == "end":
            out.append("end")
            continue
        if st[0] == "deact":
            for _ in range(depth.pop(st[1], 0)):
                out.append(f"deactivate {st[1]}")
            continue
        kind, src, dst, label, _ = st
        if kind == "self":
            out.append(f"{src} -> {src} : {label}")
            out.append(f"activate {src}")
            depth[src] = depth.get(src, 0) + 1
        elif kind == "ret":
            out.append(f"{src} --> {dst} : {label}")
            for _ in range(depth.pop(src, 0)):
                out.append(f"deactivate {src}")
        else:
            out.append(f"{src} -> {dst} : {label}")
    out.append("@enduml")
    return "\n".join(out) + "\n"


def v3(steps, start=1, loop_note=None):
    lines, n, loop_start = [], start, None
    for st in steps:
        if st[0] == "deact":
            continue
        if st[0] == "loop":
            loop_start = n
            continue
        if st[0] == "end":
            note = loop_note or "(Lặp lại các bước {a} đến {b}.)"
            lines.append(note.format(a=loop_start, b=n - 1))
            continue
        lines.append(f"{n}. {st[4]}")
        n += 1
    return lines, n


def count(steps):
    return sum(1 for s in steps if s[0] not in ("loop", "end", "deact"))


def main():
    m4b_start = count(M4A) + 1
    m4c_start = m4b_start + count(M4B)
    diagrams = [
        ("seq-m2", "Biểu đồ tuần tự - Modul View Ranking", M2_PARTS, M2, 1),
        ("seq-m3", "Biểu đồ tuần tự - Modul Play With Bot", M3_PARTS, M3, 1),
        ("seq-m4-1", "Biểu đồ tuần tự - Modul Play Online Match (phần 1: thách đấu)", M4A_PARTS, M4A, 1),
        ("seq-m4-2", "Biểu đồ tuần tự - Modul Play Online Match (phần 2: chơi ván)", M4B_PARTS, M4B, m4b_start),
        ("seq-m4-3", "Biểu đồ tuần tự - Modul Play Online Match (phần 3: kết thúc ván và tái đấu)", M4C_PARTS, M4C,
         m4c_start),
        ("seq-m1", "Biểu đồ tuần tự - Modul Manage Account", M1_PARTS, M1, 1),
    ]
    for name, title, parts, steps, start in diagrams:
        (HERE / f"{name}.puml").write_text(puml(name, title, parts, steps, start), encoding="utf-8")

    real = [s for s in M4B if s[0] not in ("loop", "end", "deact")]
    cleared = m4b_start + next(i for i, s in enumerate(real) if s[0] == "ret" and "isCleared()" in s[4])
    m4_note = f"(Lặp lại các bước {{a}} đến {{b}} cho đến khi bước {cleared} trả về bàn của A đã hết ô; B chơi song song theo cùng các bước trên máy của mình.)"
    blocks = {
        "v3-m2": v3(M2)[0],
        "v3-m3": v3(M3, loop_note=M3_LOOP_NOTE)[0],
        "v3-m4": v3(M4A)[0] + v3(M4B, m4b_start, m4_note)[0] + v3(M4C, m4c_start)[0],
        "v3-m1": v3(M1)[0],
    }
    text = DOC.read_text(encoding="utf-8")
    for key, lines in blocks.items():
        body = "\n".join(lines)
        pattern = re.compile(rf"(<!-- BEGIN {key} -->\n)(?:.*?\n)?(<!-- END {key} -->)", re.S)
        text, n = pattern.subn(lambda m: m.group(1) + body + "\n" + m.group(2), text)
        if n != 1:
            raise SystemExit(f"thiếu khối {key} trong {DOC.name}")
    DOC.write_text(text, encoding="utf-8")
    print("ok:", ", ".join(f"{k} {len(v)} dòng" for k, v in blocks.items()))


if __name__ == "__main__":
    main()
