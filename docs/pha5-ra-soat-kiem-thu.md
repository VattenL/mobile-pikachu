# Pha 5. Rà soát và kiểm thử hệ thống

> Bản nháp. Mục 1a đến 1c đã rà soát trên tài liệu pha 1 đến pha 3 hiện có; mục 1d và cột kết quả ở mục 2 điền khi có code và hệ thống chạy được.

## 1. Rà soát

Mỗi phần dùng một checklist rút từ giáo trình (Bảng 6.x), chỉ giữ các mục áp dụng cho hệ thống này. Cột Y/N/NA: Y là đạt, N là chưa đạt, NA là không áp dụng. Các mục N được ghi thành vấn đề cần trao đổi ở cuối mỗi phần.

### a. Rà soát đặc tả (pha 1)

| Mã | Mục | Y/N/NA | Ghi chú |
|---|---|---|---|
| 1 | Mỗi yêu cầu trong đề bài đều có chức năng tương ứng? | Y | Bốn modul phủ các chức năng của đề: M1 (quản lý tài khoản) và M2 (xem bảng xếp hạng) cho dòng Dễ, M3 và M4 cho hai dòng còn lại |
| 2 | Các yêu cầu giao diện người dùng đã hoàn thiện? | Y | Mô tả ở bước 3 của mục 2, chi tiết hóa ở pha 3 |
| 3 | Các yêu cầu có thể kiểm thử được? | Y | Mỗi hoạt động nghiệp vụ có kết quả quan sát được |
| 4 | Yêu cầu hiệu năng đầy đủ và khả thi? | Y | Phản hồi một nước đi dưới 200 ms (`plan.md` Phần I §1.4) |
| 5 | Yêu cầu bảo mật đã được xác định? | N | Mới có băm mật khẩu khi lưu; kênh truyền chưa mã hóa |
| 6 | Không có yêu cầu xung đột hoặc trùng lặp? | Y | |
| 7 | Mỗi yêu cầu rõ ràng, không mơ hồ? | N | Còn các điểm mở ở `plan.md` §8 |
| 8 | Các chức năng phụ thuộc thời gian đã có tiêu chí thời gian? | Y | Ván 10 phút; thời gian hoàn thành tính theo đồng hồ server |
| 9 | Thuật toán của các yêu cầu chức năng đã được định nghĩa? | Y | Nối ô, xếp hạng (định nghĩa chi tiết ở pha 3, mục 8) |
| 10 | Mỗi yêu cầu đều trong phạm vi dự án? | Y | Các chức năng ngoài phạm vi đã liệt kê ở bước 2 |

Vấn đề cần trao đổi:

1. Cách tính điểm mỗi ván: đã chốt theo đề (thắng 1, hòa 0.5, thua 0).
2. Ván thắng do đối thủ thoát hoặc mất kết nối có tính vào trung bình thời gian thắng không? Đề chỉ nói "thời gian kết thúc trong các trận thắng".
3. Hết giờ mà chưa ai xong thì hòa, không so số ô còn lại. Cần xác nhận lại với giảng viên vì đề chỉ nói "ai hoàn thành nhanh hơn thì thắng".
4. Mật khẩu đi qua mạng dạng rõ. Có cần mã hóa kênh truyền trong phạm vi đồ án không?

### b. Rà soát phân tích (pha 2)

| Mã | Mục | Y/N/NA | Ghi chú |
|---|---|---|---|
| 1 | Tài liệu phân tích tuân theo phương pháp của giáo trình? | Y | Kịch bản v1, lớp thực thể, lớp phân tích, trạng thái, v2, giao tiếp |
| 2 | Các màn hình đã được xác định? | Y | Mỗi lớp biên một màn hình |
| 3 | Thông báo lỗi, cảnh báo, thông báo thông tin đầy đủ? | N | Thiếu thông báo khi đối thủ mất kết nối lúc đang mời và lúc ở dialog tiếp tục |
| 4 | Các giao tiếp giữa các thành phần đã được xác định? | Y | Biểu đồ giao tiếp khớp kịch bản v2 và biểu đồ lớp phân tích |
| 5 | Thứ tự sắp xếp của báo cáo (bảng xếp hạng) đã được chỉ định? | Y | Ba tiêu chí theo đề |
| 6 | Các trường của báo cáo tồn tại trong CSDL hoặc tính được? | Y | Ba chỉ số tính từ `MatchResult` |
| 7 | Hành vi cho mọi điều kiện lỗi đã được ghi lại? | N | Xem vấn đề 1, 2 dưới đây |
| 8 | Tài liệu đủ để tiến hành thiết kế? | Y | |
| 9 | Không có chức năng vượt phạm vi đề? | Y | |

Vấn đề cần trao đổi:

1. Kịch bản modul thách đấu chưa có ngoại lệ: người được mời mất kết nối khi lời mời đang chờ; một người mất kết nối khi đang ở dialog hỏi tiếp tục. Thiết kế (pha 3, mục 7) đang xử lý theo phương án đề xuất ở `plan.md` §8.
2. Ngoại lệ 2b của kịch bản xem bảng xếp hạng (tài khoản đang đăng nhập ở máy khác) chưa nói hệ thống xử lý thế nào. Thiết kế đang từ chối lần đăng nhập sau.
3. Ở pha phân tích, `MatchScreen` gọi trực tiếp `Board` để kiểm tra đường nối. Đây là mức trừu tượng của pha phân tích; ở thiết kế việc kiểm tra chuyển lên server (trọng tài). Không cần sửa pha 2 nhưng cần ghi nhận để người đọc không hiểu nhầm là client tự kiểm tra trong ván đối kháng.
4. Ngoại lệ 4a của kịch bản xem bảng xếp hạng (bảng rỗng khi chưa có ván nào) ngầm định bảng xếp hạng chỉ gồm người đã có ván. Thiết kế theo cách hiểu này; cần xác nhận người chưa chơi ván nào có cần hiện với 0 điểm không.

### c. Rà soát thiết kế (pha 3)

| Mã | Mục | Y/N/NA | Ghi chú |
|---|---|---|---|
| 1 | Mỗi lớp thực thể chuyển thành một bảng (hoặc gộp có lý do)? | Y | `Board` gộp vào `tblMatch` (đề bài) và `tblMatchResult` (bàn riêng của từng người); `PlayerRanking` kế thừa `Player`, toàn thuộc tính dẫn xuất nên không có bảng |
| 2 | Mỗi thuộc tính ánh xạ tới một cột (hoặc loại có lý do)? | Y | `status` giữ trong bộ nhớ; `rowCount`, `columnCount` là hằng số; `point` suy ra từ `result` nên không lưu |
| 3 | Mỗi khóa có chỉ mục? | Y | Khóa chính, `username` duy nhất, (`matchId`, `playerId`) duy nhất |
| 4 | Mọi khóa ngoại NOT NULL? | Y | `matchId`, `playerId`; `matchId` có `ON DELETE CASCADE` theo quan hệ gắn chặt `Match` ◆ `MatchResult` |
| 5 | Các cột dùng thường xuyên trong WHERE, GROUP BY, ORDER BY đã được xét chỉ mục? | Y | Truy vấn xếp hạng nhóm theo `playerId`, `matchId`; MySQL (InnoDB) tự tạo chỉ mục cho hai cột khóa ngoại này |
| 6 | Đáp ứng yêu cầu hiệu năng? | Y | Kiểm tra đường nối tối đa 12 x 12 x 4 trạng thái |
| 7 | Truy cập đồng thời đã được xử lý? | Y | Đồng bộ trên `MatchRoom`, `ClientHandler.send()` và mọi hàm của DAO (cả đọc, vì các luồng dùng chung một kết nối) |
| 8 | Mỗi phương thức trong kịch bản v3 có trong biểu đồ lớp thiết kế? | Y | Kịch bản v3 và biểu đồ tuần tự sinh từ cùng một danh sách bước (`docs/uml/pha3/gen_seq.py`) |
| 9 | Mỗi lời gọi trong biểu đồ tuần tự đi trên một liên kết của biểu đồ lớp thiết kế? | Y | |
| 10 | Phân quyền, kiểm soát truy cập dữ liệu đã được thực hiện? | Y | Lần rà soát đầu phát hiện server chưa kiểm tra người gửi `MOVE`, `LEAVE_MATCH`, `REMATCH_REPLY` có thuộc ván `matchId` không; đã bổ sung vào các hàm xử lý của `MatchController` (pha 4, mục 2e) |

Vấn đề cần trao đổi:

1. Một kết nối CSDL dùng chung cho mọi luồng đơn giản nhưng làm các truy vấn chạy tuần tự; với số người chơi của đồ án là đủ, nếu cần mở rộng thì dùng bộ kết nối (connection pool).

### d. Rà soát code (pha 4)

Thực hiện khi có code, theo các mục chính của checklist giáo trình (Bảng 6.5): biến toàn cục, tĩnh có thực sự cần; không ghi cứng hằng số (cổng, chuỗi kết nối, thời gian ván); mọi lỗi I/O socket và SQL được bắt và xử lý; tài nguyên (socket, `ResultSet`, `PreparedStatement`) được đóng; chia cho 0 khi tính trung bình; thông báo lỗi rõ ràng; thời gian phản hồi.

## 2. Kiểm thử chức năng

### a. Checklist chung

Rút từ Bảng 6.6 của giáo trình, áp dụng cho ứng dụng Android:

- Giao diện đúng với thiết kế (pha 3, Hình 3.3, 3.5, 3.6, 3.8, 3.10–3.14); không lỗi chính tả; định dạng số và thời gian (mm:ss) thống nhất.
- Các màn hình gọi từ nút bấm hiển thị đúng; nút Quay lại của hệ thống Android cho kết quả giống nút Quay lại trên màn hình.
- Xoay màn hình, ẩn ứng dụng rồi mở lại không làm mất trạng thái ván.
- Trường bắt buộc (tên đăng nhập, mật khẩu) không cho phép rỗng; khoảng trắng đầu cuối được cắt bỏ.
- Thông báo lỗi bằng ngôn ngữ tự nhiên, không đổ lỗi cho người dùng.
- Dữ liệu hiển thị (tổng điểm, bảng xếp hạng) khớp với dữ liệu trong CSDL.
- Thao tác ghi dữ liệu không thành công thì không có dữ liệu nào được lưu (rollback).

### b. Test case

Cột Kết quả để trống, điền khi thực hiện (Đạt / Không đạt kèm mô tả lỗi).

**Modul quản lý tài khoản.** Tiền điều kiện chung: server đang chạy, CSDL đã nạp dữ liệu mẫu (chưa có tài khoản `thanhtung`). Đăng nhập sai, bỏ trống trường và một tài khoản ở hai máy đã có ở VR_3 đến VR_5.

| Mã | Mục đích kiểm thử | Các bước thực hiện | Kết quả mong muốn | Kết quả |
|---|---|---|---|---|
| AC_1 | Giao diện đăng ký | Ở màn đăng nhập bấm Đăng ký | Có bốn ô tên đăng nhập, mật khẩu, nhập lại mật khẩu (ẩn ký tự), tên hiển thị, nút Đăng ký, nút Quay lại | |
| AC_2 | Đăng ký thành công | Nhập `thanhtung`, mật khẩu, nhập lại đúng, tên "Thanh Tùng", bấm Đăng ký | Về màn đăng nhập, báo "Đăng ký thành công, mời đăng nhập", ô tên đăng nhập điền sẵn `thanhtung`; `tblPlayer` có thêm một dòng, cột mật khẩu là chuỗi băm 64 ký tự | |
| AC_3 | Tên đăng nhập đã tồn tại | Đăng ký với `hoangnam` | Ở lại màn đăng ký, báo "Tên đăng nhập đã tồn tại"; `tblPlayer` không thêm dòng nào | |
| AC_4 | Thiếu thông tin hoặc mật khẩu không khớp | Bỏ trống tên hiển thị; hoặc nhập lại mật khẩu khác | Báo lỗi ngay trên màn đăng ký, không gửi lên server | |
| AC_5 | Quay lại | Ở màn đăng ký bấm Quay lại | Về màn đăng nhập, không có tài khoản mới | |
| AC_6 | Đăng nhập tài khoản mới | Sau AC_2, nhập mật khẩu và bấm Đăng nhập | Vào giao diện chính, tiêu đề "Thanh Tùng", "0 điểm"; máy khác thấy Thanh Tùng trong danh sách online với trạng thái Rỗi | |
| AC_7 | Đăng xuất | Thanh Tùng bấm Đăng xuất | Về màn đăng nhập; trên máy khác, Thanh Tùng biến khỏi danh sách online | |
| AC_8 | Đăng nhập lại sau khi đăng xuất | Sau AC_7, đăng nhập lại `thanhtung` trên máy khác | Đăng nhập được (không bị báo đang đăng nhập ở máy khác) | |
| AC_9 | Mất kết nối | Thanh Tùng đang online thì tắt mạng máy đó | Trên máy khác, Thanh Tùng biến khỏi danh sách online; bật mạng rồi đăng nhập lại được | |
| AC_10 | Giao diện đăng nhập | Mở ứng dụng, bấm Đấu đối kháng | Có ô tên đăng nhập, ô mật khẩu (ẩn ký tự), nút Đăng nhập, nút Đăng ký; con trỏ ở ô tên đăng nhập | |
| AC_11 | Đăng nhập đúng | Nhập `hoangnam` và mật khẩu đúng, bấm Đăng nhập | Vào màn hình đấu đối kháng, danh sách online không có chính mình | |
| AC_12 | Đăng nhập sai mật khẩu | Nhập `hoangnam`, mật khẩu sai | Ở lại màn đăng nhập, báo "Sai tên đăng nhập hoặc mật khẩu" | |
| AC_13 | Bỏ trống trường | Bỏ trống tên đăng nhập hoặc mật khẩu, bấm Đăng nhập | Báo trường bắt buộc, không gửi lên server | |
| AC_14 | Một tài khoản đăng nhập ở hai máy | Đăng nhập `hoangnam` ở máy 1, rồi ở máy 2 | Máy 2 bị từ chối, báo tài khoản đang đăng nhập ở máy khác; máy 1 không bị ảnh hưởng | |
| AC_15 | Nhiều client đăng nhập đồng thời | Đăng nhập 3 tài khoản khác nhau trên 3 máy gần như cùng lúc | Cả 3 vào được; danh sách online trên màn hình đấu đối kháng của mỗi máy có 2 người còn lại | |

**Modul xem bảng xếp hạng.** Tiền điều kiện chung: server đang chạy, CSDL đã nạp dữ liệu mẫu.

| Mã | Mục đích kiểm thử | Các bước thực hiện | Kết quả mong muốn | Kết quả |
|---|---|---|---|---|
| VR_1 | Giao diện chính khi là khách | Mở ứng dụng, chưa đăng nhập | Giao diện chính hiện "Khách" và bốn nút Chơi với máy, Đấu đối kháng, Xem bảng xếp hạng, Cài đặt | |
| VR_2 | Khách xem bảng xếp hạng | Chưa đăng nhập, bấm Xem bảng xếp hạng | Hiện trang 1 với 5 cột Hạng, Tên, Tổng điểm, TB điểm đối thủ, TB thời gian thắng, đúng dữ liệu mẫu, "Trang 1/2"; không có nút Vị trí của tôi | |
| VR_3 | Chuyển trang | Bấm Trang sau, rồi Trang trước | Hiện trang 2 (hạng 11, 12) rồi về trang 1; ở trang 1 nút Trang trước bị vô hiệu, ở trang cuối nút Trang sau bị vô hiệu | |
| VR_4 | Vị trí của tôi | Đăng nhập `hoangnam` (hạng 12), vào bảng xếp hạng, bấm Vị trí của tôi | Chuyển sang trang 2, dòng của `hoangnam` được tô màu | |
| VR_5 | Vị trí của tôi khi chưa có ván | Đăng nhập tài khoản chưa đấu ván nào, bấm Vị trí của tôi | Báo "Bạn chưa có trong bảng xếp hạng", giữ nguyên trang | |
| VR_6 | Bằng điểm ở tiêu chí 1 | Dữ liệu mẫu có hai người cùng tổng điểm | Người có TB điểm đối thủ cao hơn đứng trên | |
| VR_7 | Bằng điểm ở tiêu chí 1 và 2 | Nạp `seed-tiebreak.sql` thay cho dữ liệu mẫu chung: có hai người cùng tổng điểm và cùng TB điểm đối thủ | Người có TB thời gian thắng nhỏ hơn đứng trên | |
| VR_8 | Bảng xếp hạng rỗng | Dùng CSDL chưa có ván nào | Hiện thông báo chưa có dữ liệu xếp hạng | |
| VR_9 | Quay lại | Bấm Quay lại ở bảng xếp hạng | Về giao diện chính | |

**Modul chơi với máy.** Tiền điều kiện: đã đăng nhập, đang ở giao diện chính.

| Mã | Mục đích kiểm thử | Các bước thực hiện | Kết quả mong muốn | Kết quả |
|---|---|---|---|---|
| PB_1 | Giao diện chọn độ khó | Bấm Chơi với máy | Có ba lựa chọn, mặc định một lựa chọn đã chọn, nút Bắt đầu, nút Quay lại | |
| PB_2 | Bắt đầu ván | Chọn Trung bình, bấm Bắt đầu | Bàn 10x10 đủ ô, đồng hồ 10:00 đếm ngược, "Bạn: 100", "Máy: 100" | |
| PB_3 | Nối hợp lệ | Click hai ô cùng hình nối được | Hiện đường nối, hai ô biến mất, "Bạn" giảm 2 | |
| PB_4 | Nối không hợp lệ | Click hai ô khác hình, hoặc cùng hình nhưng cần hơn 3 đoạn | Không xóa ô, bỏ chọn | |
| PB_5 | Đường đi qua viền | Click hai ô ở mép bàn chỉ nối được qua viền | Nối được, đường vẽ ra ngoài vùng chơi | |
| PB_6 | Máy chơi theo độ khó | Quan sát số ô của máy ở ba độ khó | Máy xóa một cặp sau khoảng 6 s, 4 s, 2 s tương ứng | |
| PB_6b | Thấy bàn của máy | Quan sát cột phải trong lúc chơi | Bàn thu nhỏ của máy mất dần đúng các cặp máy vừa xóa, có hiệu ứng nối; chạm vào bàn này không có tác dụng | |
| PB_7 | Bế tắc | Dùng đề thử dẫn tới bế tắc | Bàn được xáo, vẫn đủ số ô mỗi hình, có cặp nối được | |
| PB_8 | Người thắng | Xóa hết ô trước máy | Dialog "Bạn thắng" kèm thời gian hoàn thành và số ô còn lại của máy | |
| PB_9 | Máy thắng | Để máy xóa hết trước | Dialog "Bạn thua" | |
| PB_10 | Hết giờ | Chờ hết 10 phút | Dialog "Hòa" | |
| PB_11 | Thoát giữa ván | Bấm Thoát | Về giao diện chính, máy dừng chơi | |
| PB_12 | Không lưu kết quả | Chơi xong một ván với máy, xem bảng xếp hạng | Tổng điểm và bảng xếp hạng không đổi | |
| PB_13 | Trạng thái khi chơi với máy | Người chơi khác xem danh sách online | Người đang chơi với máy hiện trạng thái Luyện tập, vẫn mời được | |

**Modul thách đấu và đấu đối kháng.** Tiền điều kiện: A (`hoangnam`) và B (`minhanh`) đăng nhập trên hai máy, đều rỗi và đang ở màn hình đấu đối kháng.

| Mã | Mục đích kiểm thử | Các bước thực hiện | Kết quả mong muốn | Kết quả |
|---|---|---|---|---|
| CP_0 | Khách vào đấu đối kháng | Chưa đăng nhập, bấm Đấu đối kháng ở giao diện chính | Hiện màn đăng nhập; đăng nhập xong thì vào màn hình đấu đối kháng | |
| CP_1 | Danh sách online | Quan sát màn hình đấu đối kháng hai máy | Mỗi máy thấy người kia với tổng điểm và trạng thái Rỗi | |
| CP_2 | Gửi lời mời | A click tên B, bấm Xác nhận | Sau khi click tên, máy A hiện dialog xác nhận thách đấu `minhanh`; sau khi xác nhận, máy A hiện "Đang chờ minhanh trả lời..."; máy B hiện dialog lời mời | |
| CP_3 | Hủy thách đấu | A click tên B, bấm Hủy | Dialog xác nhận đóng; máy A vẫn ở màn hình đấu đối kháng, không hiện "Đang chờ"; máy B không nhận lời mời | |
| CP_4 | Từ chối | B bấm Reject | Máy A báo bị từ chối; hai người vẫn rỗi | |
| CP_5 | Mời người đang bận | C đang trong ván với D; A click tên C, bấm Xác nhận | A được báo C đang bận; C không nhận lời mời | |
| CP_6 | Chấp nhận | B bấm Accept | Hai máy vào màn chơi với cùng đề, đồng hồ 10:00, "Đối thủ: 100"; máy thứ ba thấy A, B Bận | |
| CP_7 | Nối hợp lệ | A nối một cặp | Máy A xóa hai ô; trên máy B, bàn thu nhỏ của A mất đúng hai ô đó (có hiệu ứng nối) và hiện "98 ô"; bàn của B không đổi | |
| CP_8 | Server là trọng tài | Gửi `MOVE` cho cặp không nối được bằng một client thử | Server trả `ok = false`, bàn trên server không đổi | |
| CP_9 | Bế tắc riêng một người | Dùng đề thử để bàn của A bế tắc | Chỉ máy A nhận bàn xáo mới; bàn của B giữ nguyên; bàn thu nhỏ của A trên máy B cũng đổi theo bàn mới | |
| CP_10 | Thắng | A xóa hết ô trước | A: "Bạn thắng" kèm thời gian; B: "Bạn thua" kèm số ô còn lại; kết quả được lưu, tổng điểm cập nhật | |
| CP_11 | Hoàn thành gần như cùng lúc | A và B cùng còn một cặp, bấm gần như cùng lúc | Chỉ một người thắng (người được server xử lý trước), ván chỉ kết thúc và lưu một lần | |
| CP_12 | Hết giờ | Để hết 10 phút | Hai bên "Hòa"; lưu hai kết quả DRAW | |
| CP_13 | Thoát giữa ván | A bấm Thoát | A thua, B thắng; B nhận dialog kết quả | |
| CP_14 | Mất kết nối giữa ván | Tắt mạng máy A giữa ván | Server xử A thua, B thắng; A không còn trong danh sách online | |
| CP_15 | Cả hai tái đấu | Hai người bấm Tái đấu | Bắt đầu ván mới với đề mới | |
| CP_16 | Một bên dừng | A bấm Dừng | Hai máy về màn hình đấu đối kháng; B được báo "hoangnam đã dừng chơi"; hai người Rỗi, điểm cập nhật | |
| CP_17 | Hai người mời nhau cùng lúc | A và B mời nhau gần như cùng lúc | Chỉ tạo tối đa một ván; không ai rơi vào hai ván | |
| CP_18 | Mời người đang luyện tập | C đang chơi với máy; A click tên C, bấm Xác nhận; C bấm Accept | C nhận dialog lời mời ngay trên màn chơi với máy; sau khi đồng ý, ván với máy dừng (không lưu) và hai máy vào ván đối kháng | |

### c. Dữ liệu kiểm thử

`server/db/seed.sql` tạo dữ liệu dùng chung, khớp bảng xếp hạng mẫu của kịch bản (pha 2):

- Người chơi: 12 tài khoản ứng với người chơi A đến N của kịch bản xem bảng xếp hạng: `hoangnam` là A, `minhanh` là B, các người còn lại là `nguoichoic` đến `nguoichoin`; mật khẩu mẫu chung. Tên đăng nhập cũng là tên hiển thị.
- Các ván đối kháng đã kết thúc, chọn sao cho:
  - tổng điểm đúng như kịch bản: B 12.5, C 10.5, D 9, E 8.5, G 8, H 7.5, I 7.5, K 6, L 5.5, M 5, N 4.5, A 3.5. Mỗi ván phát ra đúng 1 điểm (1 + 0 hoặc 0.5 + 0.5), nên tổng điểm của mọi người (88) bằng đúng số ván; 12 người nên bảng xếp hạng có 2 trang và A nằm ở trang 2 (VR_3, VR_4);
  - thứ tự xếp hạng đúng như kịch bản; H và I cùng 7.5 nhưng khác TB điểm đối thủ để kiểm tra tiêu chí 2 (VR_6);
  - có một ván thắng do đối thủ bỏ cuộc (không có `finishTime`);
  - có một người gặp cùng một đối thủ hai lần.
- `server/db/seed-tiebreak.sql`: bộ dữ liệu nhỏ riêng cho VR_7, có hai người cùng tổng điểm và cùng TB điểm đối thủ. Dữ liệu này không trộn vào `seed.sql` để bảng xếp hạng mẫu vẫn khớp kịch bản.
- Một CSDL rỗng (chỉ chạy `schema.sql`) cho VR_8.
- Đề thử cố định cho PB_7, CP_9 (bàn gần bế tắc) được nạp bằng seed cố định trong chế độ thử của server.

### d. Thực hiện và ghi nhận

Thực hiện khi hệ thống chạy được: chạy lần lượt các test case, ghi Đạt / Không đạt; trường hợp không đạt mô tả bước, dữ liệu, kết quả thực tế và chuyển cho người phụ trách phần đó (Dev A: `core` và màn chơi; Dev B: server, mạng, màn đăng nhập, sảnh, xếp hạng).
