# Nhật ký prompt

Lưu nguyên văn các prompt yêu cầu viết code trong pha triển khai, và các prompt khác được yêu cầu lưu. Mục mới thêm ở cuối, mục cũ giữ nguyên.

## 2026-10-26: khung package cho hai dev

> Bắt đầu code được rồi, gỡ dòng đó đi nhé. Trước tiên, chưa code vội, nhưng hãy tạo khung cho các package dựa theo thiết kế của hệ thống. Hiện giờ cần khung cho cả 2 dev. Mục tiêu sẽ là push khung lên main. Sau khi tạo khung, tạo một commit message để tôi commit và push

## 2026-10-26: sửa tài liệu Dev 2 theo nhận xét (kéo theo sửa khung code)

> Trong @docs_review.txt là một số nhận xét của tôi sau khi đã xem docs dev 2. Validate các nhận xét đó trước khi thực hiện.

Nội dung `docs_review.txt` lúc đó:

> Chỉnh sửa:
> - Sửa lại indent cho đúng
> - Người chưa đăng nhập có thể chơi với máy
> - Chỉ cần tên đăng nhập, không cần điền tên hiển thị
> - Người chơi xem bảng xếp hạng: Không cần đăng nhập cũng xem được top, muốn xem tài khoản mình ở đâu thì cần đăng nhập, với cả cho phép xem vị trí của mình bằng một nút
> - Lúc code thì làm bot dễ chút, để tăng delay mỗi lần nó nhấn 1 cặp
> - Nhấn mạnh nếu không thì bỏ chọn lúc code
> - Đấu đối kháng: Từ màn hình chính vào màn hình đấu đối kháng chứ không chọn người chơi B từ màn hình chính, chỉnh nút thành Accept và Reject
> - Không phải muốn tiếp tục mà là tái đấu, để nhất quán lại sẽ là tái đấu
> - Chỉnh lại view ranking thành extend do yêu cầu ở trên
> - Xem lại modul thách đấu, có khi bị dính sang việc của dev 1 phần chơi online
> - Thêm khách vào làm thực thể trong hệ thống, chỉ cho chơi với bot
> - Trong match mà có bot thì có tính không hay chỉ đơn giản là thuật toán thôi, nếu mà thế thì không cần object bot là đúng.
> - Cho playerranking dưới trực tiếp rankingscreen
> - Cần chỉnh tay hình 2.5, 2.9 và các class diagram còn lại, sau đó chụp lại
> - Phần kịch bản v2, từ b nhảy sang d?
> - Các biểu đồ tuần tự chỉnh lại thành actor, các actor phải ở hết bên trái
> - Sao biểu đồ lớp pha thiết kế lại y hệt pha phân tích, thực sự không cần thêm lớp nào à?
> - Tách các màn hình riêng ra, kiểu đăng nhập 1 ảnh, bảng xếp hạng 1 ảnh, giao diện chính 1 ảnh
> - Tạo thử một bản biểu đồ tuần tự riêng chứa full các bước cho những cái cần nhiều hơn 1 phần
> - Unpin header rows hết cho tất cả các bảng

Prompt bổ sung trong cùng lượt:

> Mà actor phải dùng icon của actor nhé


## 2026-10-27: sửa thiết kế Dev 2 theo bản đánh giá (kéo theo sửa khung code)

> ok bắt đầu

Bối cảnh: trả lời cho kế hoạch sửa sau bản đánh giá thiết kế Dev 2 cùng ngày (thoát hoặc mất kết nối coi như dừng, mã phòng `roomId` trong bộ nhớ, lời mời có hàng đợi, hết hạn 30 giây và nút hủy, `PING`, bỏ include Login ở `Locate My Rank` và `Respond To Challenge`, bảng xếp hạng chỉ gồm người đã có ván, giữ `MatchController` chung và chỉ thêm thuộc tính).

## 2026-11-01: Dev 2 bắt đầu code, hạ tầng server và CSDL

> Bắt đầu code phần Dev 2. Đọc trước @plan.md đặc biệt là pha thiết kế. Bắt đầu bằng việc làm hạ tầng server và csdl trước. Chỉ làm các phần có liên quan đến dev 2 và code phải khớp thiết kế 100%. Trước khi implement, nếu có câu hỏi gì thì hỏi lại tôi để xác nhận.

Trả lời các câu hỏi xác nhận trong cùng lượt:

> - Nhánh: Nhánh mới từ main, để tên là dev2/code
> - Phạm vi: Server+CSDL+OnlinePlayer (Message, ServerConfig, GameServer, ClientHandler, DAO, 4 script SQL + phần OnlinePlayerManager mà hạ tầng/Dev 1 cần)
> - Controller: Xem lại các diagram xem có không, nếu không thì option 1 (tạo bằng new khi cần, inviteTimer static), sau đó giải thích lại cho tôi về việc nó có khớp thiết kế không.
> - Seed: Option 1 (khớp cả TB điểm đối thủ và TB thời gian thắng của bảng mẫu pha 2), giải thích nó giống thiết kế chỗ nào. Với cả nhớ lưu các prompt làm code vào prompt.md

## 2026-11-01: bước 2, bảng xếp hạng phía server

> Không, giữ giao diện tiếng Việt. Tiếp theo là bước 2: bảng xếp hạng phía server. Như trước, hỏi tôi để xác nhận, với cả thêm một dòng vào @CLAUDE.md là code phải khớp thiết kế 100%.

Trả lời các câu hỏi xác nhận trong cùng lượt:

> - Tên trường danh sách dòng trong RANKING: listRanking
> - Mỗi dòng: đúng 5 trường {rank, username, totalPoint, avgOpponentPoint, avgWinTime}
> - mine = true mà người hỏi chưa có trong bảng: trang rỗng (me rỗng, page rỗng, danh sách rỗng, pageCount vẫn đúng)
> - PlayerRankingDAO.getTotalPoints(): Làm khung.

## 2026-11-01: bước 3, tầng mạng Android và màn bảng xếp hạng

> Ok tiếp đến Bước 3: network phía Android và màn Bảng xếp hạng. Tương tự vậy. Thêm một dòng vào @CLAUDE.md trước khi code thì hỏi tôi để xác nhận. Không cần commit cái claude md làm một commit riêng

Trả lời các câu hỏi xác nhận trong cùng lượt:

> - MainActivity: Option 1 (layout đủ theo Hình 3.5, chỉ làm logic Dev 2, các nút của Dev 1 để TODO, thêm ClientSession.clear()), giải thích về các việc cần làm để có thể báo cho dev 1
> - Theme: Material mặc định, theme sau
> - Kết nối: Option 1 (ServerConnection giữ application Context, có isConnected(); lỗi socket thì hủy pingTimer, xóa phiên, mở MainActivity kèm thông báo; MainActivity.onStart() kết nối lại; connect() trên luồng nền, send() trên luồng ghi riêng), giải thích việc này sẽ ảnh hưởng đến thiết kế như thế nào.
> - Định dạng số trên bảng xếp hạng: Như bảng mẫu

## 2026-11-01: khóa màn hình ngang

> Trước hết là force xoay ngang cho game đã. Về việc lệch m2 thì chính xác là nó thiếu thôi hay nó lệch hẳn luôn?
