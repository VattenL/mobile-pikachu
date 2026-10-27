# Kế hoạch dự án: Game Pikachu đối kháng trực tuyến

> Đề bài gốc: [de_bai.md](de_bai.md)
>
> Công nghệ: Android (Java) cho client, Java thuần cho server, giao tiếp qua TCP Socket.

- **Phần I**: Khung tài liệu đặc tả phần mềm, theo các pha của bài giảng *BG HP TTTN 2 CNPM 2020* (chương 3 đến chương 6).
- **Phần II**: Kế hoạch triển khai, chia việc cho 2 dev theo 2 phase (Offline → Online).

---

# PHẦN I. KHUNG TÀI LIỆU ĐẶC TẢ PHẦN MỀM

**Đề tài:** Hệ thống trò chơi Pikachu đối kháng trực tuyến.

Tài liệu đi theo quy trình phát triển hướng đối tượng của bài giảng: **Thu thập yêu cầu → Phân tích → Thiết kế → Cài đặt → Kiểm thử**. Kết quả của pha trước là đầu vào bắt buộc của pha sau. Các mục đánh dấu *(bổ sung)* không có trong giáo trình, được thêm vì hệ thống là game client-server.

## Các modul của hệ thống

Lấy theo các chức năng của đề. Dòng Dễ của đề (quản lí thông tin người chơi, xem BXH) tách thành hai modul M1 và M2, để mỗi người trong nhóm có một chức năng dễ. Mã modul dùng xuyên suốt tài liệu.

| Mã | Modul | Mức | Nội dung |
| --- | --- | --- | --- |
| M1 | Quản lý tài khoản | Dễ | Đăng ký, đăng nhập, đăng xuất |
| M2 | Xem bảng xếp hạng | Dễ | Xem bảng xếp hạng theo ba tiêu chí (khách cũng xem được), xem vị trí của mình |
| M3 | Thi đấu với máy | Trung bình | Chọn độ khó, đua với bot trên cùng một đề, xem kết quả |
| M4 | Thách đấu và đấu đối kháng | Khó | Xem danh sách online, thách đấu, trả lời lời mời, chơi ván đối kháng, chọn tái đấu / dừng |

Phần dùng chung toàn hệ thống (từ khóa, mô tả hệ thống, use case tổng quan, lớp thực thể, CSDL) làm một lần. Từ kịch bản trở đi, mỗi modul làm riêng một bộ.

## Phần mở đầu

- Lời cảm ơn / Lời giới thiệu
- Danh mục thuật ngữ và viết tắt
- Danh mục hình ảnh và bảng biểu

## Chương 1. Thu thập yêu cầu (giáo trình §3.1)

### 1.1. Tìm hiểu lĩnh vực chuyên môn (§3.1.1)

- **Bước 1, 2:** liệt kê các từ khóa chuyên môn, chia thành 3 nhóm:

| Con người | Hoạt động của con người | Vật, đối tượng |
| --- | --- | --- |
| Người chơi, khách, đối thủ, máy (bot) | Đăng ký, đăng nhập, đăng xuất, thách đấu, chấp nhận, từ chối, chọn ô, nối cặp ô, thoát ván, tái đấu, xem bảng xếp hạng, xem vị trí của mình | Tài khoản, ván đấu, bàn chơi, ô, hình nhân vật, đường nối, đồng hồ, điểm, lời mời, kết quả, bảng xếp hạng |

- **Bước 3:** bảng giải thích ngữ nghĩa từng từ khóa (glossary).

### 1.2. Mô tả hệ thống bằng ngôn ngữ tự nhiên (§3.1.2)

- **Bước 1, mục đích hệ thống:** game Pikachu cho nhiều người chơi trên thiết bị Android, đấu đối kháng qua mạng với server làm trọng tài, hoặc đấu với máy.
- **Bước 2, phạm vi:** ai được dùng hệ thống và được làm những chức năng nào (người chơi chưa đăng nhập, người chơi đã đăng nhập).
- **Bước 3, hoạt động nghiệp vụ:** mô tả chi tiết từng chức năng ở bước 2 diễn ra thế nào. Lấy từ [de_bai.md](de_bai.md), bổ sung các điểm đã chốt ở [Phần II mục 8](#8-vấn-đề-cần-chốt).
- **Bước 4, đối tượng và thuộc tính:** người chơi, ván đấu, kết quả của từng người trong ván, lời mời, bảng xếp hạng...
- **Bước 5, quan hệ số lượng:** ví dụ mỗi ván có đúng 2 người chơi, mỗi người chơi có nhiều ván, mỗi người trong một ván có một kết quả.

### 1.3. Mô tả hệ thống bằng UML: use case (§3.1.3)

- **Use case tổng quan:**
  - Bước 1, actor: `Người chơi`; người bị thách đấu là actor gián tiếp của use case thách đấu.
  - Bước 2, mỗi chức năng ở §1.2 thành một use case.
  - Bước 3, mịn hóa: gộp các use case trùng nhau, dùng use case trừu tượng nếu cần.
- **Use case chi tiết cho từng modul M1 đến M4:** trích phần use case tương ứng từ biểu đồ tổng quan; mỗi giao diện tương tác thành một use case con; xác định quan hệ include / extend / generalization; kèm bảng mô tả từng use case.
- Ví dụ use case con: `Nối cặp ô` được cả M3 và M4 include; `Trả lời lời mời` (M4).
- Quy tắc: tên use case là động từ chỉ hành động của actor; mỗi use case có đường đi tới ít nhất một actor; mã use case (UC01, UC02...) giữ nguyên từ biểu đồ tổng quan sang biểu đồ chi tiết.

### 1.4. Yêu cầu phi chức năng *(bổ sung)*

- **Hiệu năng:** độ trễ phản hồi một nước đi < 200 ms; server xử lý đồng thời nhiều phòng chơi.
- **Tin cậy:** server là trọng tài duy nhất, không tin kết quả do client tự báo.
- **Mở rộng:** dễ bổ sung loại message và chức năng mới.

## Chương 2. Phân tích (giáo trình §3.2)

### 2.1. Viết kịch bản (§3.2.1)

- Mỗi modul một bảng kịch bản v1: **Use case / Actor / Tiền điều kiện / Hậu điều kiện / Kịch bản chính / Ngoại lệ**.
- Kịch bản chính đánh số bước, xen kẽ hành động người chơi và phản hồi hệ thống; bước hệ thống hiển thị dữ liệu thì kèm bảng dữ liệu mẫu (ví dụ danh sách online, bảng xếp hạng).
- Ngoại lệ đánh số theo bước phát sinh (ví dụ `4a. Đối thủ đang bận`).
- Dùng một bộ dữ liệu mẫu nhất quán cho cả tài liệu (tên người chơi, điểm, thời gian).

### 2.2. Trích lớp thực thể (§3.2.2)

- **Bước 1:** mô tả toàn hệ thống trong một đoạn văn.
- **Bước 2:** trích các danh từ, chia nhóm người / vật / thông tin.
- **Bước 3:** đánh giá, chọn danh từ thành lớp thực thể và thuộc tính. Chưa cần thuộc tính id ở pha này.
- **Bước 4:** xác định quan hệ số lượng giữa các lớp.
- **Bước 5:** vẽ biểu đồ lớp thực thể toàn hệ thống.
- Ứng viên dự kiến: `Player` (người chơi), `Match` (ván đấu), `MatchResult` (kết quả của một người trong một ván), lớp thống kê xếp hạng `PlayerRanking` kế thừa `Player` (theo cách giáo trình làm với lớp thống kê), các chỉ số tính từ `MatchResult`. Bàn chơi, lời mời, trạng thái online là dữ liệu tạm thời, cần cân nhắc có phải thực thể lưu trữ hay không.

### 2.3. Trích lớp biên và lớp điều khiển, biểu đồ lớp phân tích (§3.2.3)

- **Bước 1:** mỗi giao diện thành một lớp biên. Giao diện dự kiến:
  - M1: đăng nhập, đăng ký, trang chính.
  - M2: trang chính, bảng xếp hạng.
  - M3: chọn độ khó, bàn chơi, kết quả.
  - M4: màn hình đấu đối kháng (danh sách online, mở từ giao diện chính), dialog lời mời, bàn chơi, dialog kết quả và hỏi tái đấu.
- **Bước 2:** mỗi thành phần giao diện thành một thuộc tính, tiền tố `in` (nhận dữ liệu vào), `out` (hiện dữ liệu ra), `sub` (submit).
- **Bước 3:** mỗi xử lý phía dưới giao diện thành một phương thức: tên gợi nhớ, tham số vào/ra, gán cho lớp thực thể theo tham số ra, nếu không thì theo tham số vào.
- Vẽ một biểu đồ lớp phân tích cho mỗi modul. Quan hệ giữa các lớp thực thể giữ đúng như biểu đồ §2.2.

### 2.4. Phân tích hoạt động (§3.2.4)

- **Biểu đồ trạng thái** cho mỗi modul: mỗi trạng thái là một giao diện đang chờ người chơi; điều kiện chuyển là hành động của người chơi trên giao diện đó.
- **Kịch bản v2:** mỗi bước là hành động của một lớp trong biểu đồ lớp phân tích hoặc của actor ("Lớp A gọi lớp B yêu cầu...", "Lớp B trả kết quả cho lớp A", "Lớp A hiển thị cho người chơi").
- **Biểu đồ giao tiếp** cho kịch bản chuẩn, mỗi message ứng với một bước kịch bản v2, chỉ đi trên các liên kết có trong biểu đồ lớp.
- **Biểu đồ tuần tự pha phân tích**, chuyển từ biểu đồ giao tiếp.

## Chương 3. Thiết kế (giáo trình chương 4)

### 3.1. Thiết kế lớp thực thể (§4.1)

- **Bước 1:** thêm thuộc tính id cho các lớp không kế thừa.
- **Bước 2:** thêm kiểu dữ liệu Java cho thuộc tính.
- **Bước 3:** chuyển association sang aggregation / composition nếu có.
- **Bước 4:** thêm thuộc tính kiểu đối tượng cho lớp chứa (ví dụ `Match` chứa danh sách `MatchResult`).

### 3.2. Thiết kế cơ sở dữ liệu (§4.2)

- **Bước 1:** mỗi lớp thực thể một bảng `tblXxx`.
- **Bước 2:** bỏ thuộc tính kiểu đối tượng, đổi kiểu theo hệ quản trị CSDL.
- **Bước 3:** quan hệ 1-1 gộp, 1-n giữ nguyên, n-n thêm bảng trung gian.
- **Bước 4:** khóa chính `id`; khóa ngoại đặt tên `xxxId`.
- **Bước 5:** bỏ thuộc tính dư thừa (trùng lặp, dẫn xuất); bảng thống kê không lưu.
- Bảng dự kiến (CSDL đặt trên server):

| Bảng | Cột |
| --- | --- |
| `tblPlayer` | `id`, `username` (cũng là tên hiển thị), `password` (lưu dạng băm) |
| `tblMatch` | `id`, `startTime`, `endTime`, `timeLimit`, `tiles` (đề bài, gộp từ `Board` theo quan hệ 1-0..1) |
| `tblMatchResult` | `id`, `result`, `finishTime` (null nếu chưa xóa hết ô), `tiles` (bàn của người chơi lúc ván kết thúc, gộp từ `Board` theo quan hệ 1-0..1), `matchId`, `playerId` |

- Điểm mỗi ván (thắng 1, hòa 0.5, thua 0) suy ra từ `result` nên không lưu. Tổng điểm, trung bình điểm đối thủ, trung bình thời gian thắng là dữ liệu dẫn xuất, tính bằng truy vấn. Trạng thái người chơi chỉ giữ trong bộ nhớ server.
- Tên bảng và tên cột dùng thống nhất trong tài liệu và code (`tblXxx`, cột camelCase theo thuộc tính lớp thực thể).

### 3.3. Thiết kế chi tiết cho modul (§4.3)

#### 3.3.1. Thiết kế tĩnh (§4.3.1)

- **Bước 1:** thiết kế giao diện (wireframe) cho từng màn hình Android, các màn hình nối bằng mũi tên theo luồng.
- **Bước 2:** lớp giao diện theo nền tảng di động (Activity, Dialog): thuộc tính là thành phần giao diện (`EditText`, `RecyclerView`, `Button`...), thêm thuộc tính ẩn để giữ dữ liệu giữa các màn hình (người chơi đang đăng nhập, ván đang chơi).
- **Bước 3:** lớp truy xuất dữ liệu DAO (đặt trên server), kế thừa lớp `DAO` dùng chung kết nối; phương thức của lớp thực thể ở pha phân tích chuyển sang DAO tương ứng.
- **Bước 4:** thêm các lớp thực thể liên quan, giữ nguyên quan hệ.
- *(bổ sung)* Client không gọi DAO trực tiếp. Giữa giao diện và DAO có tầng mạng: lớp kết nối phía client và lớp xử lý từng loại message phía server (lớp điều khiển).
- Vẽ một biểu đồ lớp thiết kế cho mỗi modul.

#### 3.3.2. Thiết kế hoạt động (§4.3.2)

- **Bước 1:** biểu đồ hoạt động cho mỗi modul, mỗi hành động ứng với một phương thức trong biểu đồ lớp thiết kế.
- **Bước 2:** kịch bản v3 và biểu đồ tuần tự thiết kế; tên hành động là tên phương thức đã thiết kế, thể hiện phạm vi hoạt động của từng phương thức.

#### 3.3.3. Thiết kế triển khai (§4.3.3)

- Biểu đồ gói: client (`view`, `network`), server (`controller`, `dao`), dùng chung (`core`, `model`).
- Biểu đồ triển khai: thiết bị Android ⇄ TCP ⇄ máy chủ game ⇄ CSDL.

### 3.4. Thiết kế giao thức truyền thông *(bổ sung)*

- Chọn TCP thay vì UDP: game theo lượt click, không cần tốc độ khung hình cao, nhưng không được mất gói với nước đi và kết quả ván.
- Message JSON, mỗi message một dòng, trường `type` xác định loại. Bảng message ở [Phần II mục 5.2](#52-giao-thức-message-dev-b-chốt-trước-khi-vào-phase-2).
- Biểu đồ tuần tự cho các luồng mạng chính: đăng nhập và cập nhật danh sách online; thách đấu → chấp nhận → bắt đầu ván; gửi nước đi → server kiểm tra → kết thúc ván → hỏi tiếp tục.

### 3.5. Thuật toán *(bổ sung)*

Mỗi thuật toán có mã giả và hình minh họa.

- **Nối hai ô:** nối 2 ô cùng loại bằng tối đa 3 đoạn thẳng (tối đa 2 lần gấp khúc), chỉ đi qua ô trống. Bàn 10x10 đặt trong lưới 12x12 có viền trống để đường nối được vòng ra ngoài. Dùng BFS với trạng thái (ô, hướng, số lần rẽ), hoặc xét trực tiếp các dạng đường thẳng / chữ L / chữ Z, U. Hình minh họa tọa độ `(x1, y1) → (x2, y2)` với đường hợp lệ và không hợp lệ.
- **Sinh đề và kiểm tra bế tắc:** sinh 100 ô (50 cặp), mỗi loại hình xuất hiện số chẵn lần; phát hiện bế tắc (còn ô nhưng không còn cặp nối được) và xáo lại. Ở chế độ đối kháng, 2 người chỉ chung **đề ban đầu**: mỗi người xóa cặp trên bản riêng nên sau nước đi đầu hai bàn đã khác nhau. Bế tắc xảy ra riêng trên bàn của từng người, nên server xáo riêng bàn của người bị bế tắc và chỉ gửi bàn mới cho người đó. Sinh đề "luôn giải được" không bỏ được bước xáo, vì đề giải được vẫn bế tắc nếu xóa sai thứ tự.
- **Bot:** quét bàn tìm các cặp nối được; mức dễ chọn ngẫu nhiên sau thời gian chờ `T_delay` dài, mức khó `T_delay` ngắn hơn và ưu tiên cặp mở thêm đường cho ô khác.
- **Xếp hạng:**
  - Tổng điểm: `S = Σ point các ván` (`point` mỗi ván: thắng 1, hòa 0.5, thua 0, tính từ `result`).
  - Trung bình điểm đối thủ: `AOS = Σ S(đối thủ của ván i) / số ván`, với `S(đối thủ)` là tổng điểm **hiện tại** của đối thủ; gặp cùng một người 2 ván thì tính 2 lần.
  - Trung bình thời gian thắng: `AWT = Σ finishTime / số ván thắng có finishTime`. Ván thắng do đối thủ thoát hoặc mất kết nối không có `finishTime` nên không tính vào AWT (đề xuất, xem mục 8). Người chưa có ván thắng nào có AWT rỗng, xếp sau cùng ở tiêu chí 3.
  - Thứ tự: `ORDER BY totalPoint DESC, avgOpponentPoint DESC, avgWinTime ASC` (AWT rỗng xếp cuối).

## Chương 4. Cài đặt (giáo trình chương 5)

### 4.1. Tổ chức dự án (§5.1)

- 3 module Gradle `core`, `app`, `server` (xem [Phần II mục 1](#1-kiến-trúc-module)), các gói chia theo tầng như biểu đồ gói §3.3.3.
- Môi trường: Java, Android SDK / Android Studio, Java SE cho server, JDBC, MySQL hoặc SQLite trên server, JUnit.

### 4.2. Cài đặt các modul (§5.2)

- Mỗi modul M1 đến M4: trình bày code các lớp trong biểu đồ lớp thiết kế (lớp giao diện, lớp điều khiển phía server, DAO) và giải thích cách các phương thức hoạt động. Lớp thực thể không cần trình bày code.

### 4.3. Kiểm thử đơn vị (§5.3)

- Bảng test case: **TT / Chức năng / Lớp / Phương thức / Trường hợp test**.
- Chọn input phủ hết các nhánh; kết quả mong đợi lấy từ thiết kế, không lấy từ code.
- Viết bằng JUnit; test DAO phải chuẩn bị dữ liệu mẫu và rollback sau khi chạy.
- Trọng tâm: `PathFinder` (thẳng, L, Z, U, không nối được, đi vòng qua viền), `BoardGenerator` (số ô mỗi loại chẵn, phát hiện bế tắc), các DAO và truy vấn xếp hạng.

## Chương 5. Rà soát và kiểm thử hệ thống (giáo trình chương 6)

### 5.1. Rà soát (§6.1)

- Rà soát đặc tả, phân tích, thiết kế và code theo checklist của giáo trình; kiểm tra các sản phẩm thống nhất với nhau (use case ↔ kịch bản ↔ biểu đồ lớp ↔ biểu đồ tuần tự ↔ code).

### 5.2. Kiểm thử chức năng (§6.2)

Test case hộp đen cho từng use case, gồm cả kiểm tra giao diện và kiểm tra dữ liệu nhập. Các tình huống cần có:

- Đăng nhập đồng thời nhiều client; đăng nhập một tài khoản ở hai máy.
- Thách đấu: đồng ý, từ chối, đối thủ đang bận.
- Trọng tài: hai người hoàn thành gần như cùng lúc, tính điểm đúng.
- Một bên dừng sau ván; mất kết nối giữa ván.
- Bảng xếp hạng khi có người bằng điểm ở tiêu chí thứ nhất, thứ hai.

## Kết luận và hướng phát triển

- **Kết luận:** mức độ hoàn thành so với 3 chức năng của đề (Dễ, Trung bình, Khó); ưu điểm, hạn chế (ví dụ: mật khẩu đi qua TCP dạng rõ, chưa mã hoá kênh truyền).
- **Hướng phát triển:** chuyển giao thức sang WebSocket; thêm vật phẩm hỗ trợ, tự động ghép cặp; mở rộng sang web.

## Tài liệu tham khảo

1. Bài giảng *Thực tập tốt nghiệp 2: Công nghệ phần mềm* (BG HP TTTN 2 CNPM), 2020.
2. A. S. Tanenbaum, *Computer Networks*, 5th ed., Pearson.
3. R. Sedgewick, K. Wayne, *Algorithms*, 4th ed., Addison-Wesley.
4. Tài liệu Java Networking API và Android Developers.

---

# PHẦN II. KẾ HOẠCH TRIỂN KHAI

## 1. Kiến trúc module

Tách project Gradle thành 3 module:

```text
pikachu/
├── core/     Java thuần: Board, PathFinder, BoardGenerator, Bot, class message protocol
├── app/      Android client, phụ thuộc core
└── server/   Java server (Socket + JDBC), phụ thuộc core
```

- `core` không phụ thuộc Android hay Socket, nên test được bằng JUnit thuần.
- Server dùng chính `PathFinder` trong `core` để kiểm tra nước đi của client (chống gian lận), nên không phải viết thuật toán hai lần.

## 2. Nguyên tắc chia việc

Mỗi người làm một chức năng dễ, một trung bình, một khó. Chia theo chức năng (use case), không theo tầng, nên mỗi người viết cả client lẫn server cho phần của mình:

| Mức | Dev 1 | Dev 2 |
| --- | --- | --- |
| Dễ | Quản lý tài khoản: đăng ký, đăng nhập, đăng xuất (M1) | Xem bảng xếp hạng (M2) |
| Trung bình | Thi đấu với máy (M3), gồm engine `core` | Thách đấu và trả lời lời mời (thuộc M4) |
| Khó | Đấu đối kháng online: chơi ván và trọng tài (thuộc M4) | Kết thúc ván và tái đấu (thuộc M4) |

- **Hạ tầng chung do Dev 2 dựng trước:** `GameServer`, `ClientHandler`, `ServerConnection`, `MessageListener`, `Message`, lớp `DAO`, script tạo CSDL và dữ liệu mẫu. Dev 1 đã nhận phần thuật toán nặng nhất (`core`, bot, trọng tài) nên không nhận thêm hạ tầng.
- **Ranh giới trong `MatchController`** (hai người cùng làm lớp này):
  - Dev 1: `handleMove()`, `MatchRoom` (bàn riêng của hai người, đồng bộ), `MatchActivity`.
  - Dev 2: `endMatch()`, `onTimeout()` và bộ hẹn giờ 10 phút, `handleLeaveMatch()`, xử lý mất kết nối, `handleRematchReply()`, `stopRematch()`, `onRematchTimeout()`, `MatchDAO`, `ResultDialog`.
  - Điểm nối duy nhất: `handleMove()` thấy bàn của người gửi đã hết ô thì gọi `endMatch(room, "CLEARED")`. Quyết định 2026-10-27: không tách, giữ `MatchController` chung; phần của Dev 2 gồm thêm `stopRematch()`, `onRematchTimeout()`. `MatchRoom` (Dev 1) có thêm `roomId`, cờ `ended` và dùng lại `timer` để hẹn 30 giây chờ tái đấu; các thuộc tính này do phần của Dev 2 dùng.
- `OnlinePlayerManager` (danh sách online, trạng thái, lời mời) thuộc Dev 2; phần đăng nhập, đăng xuất của Dev 1 gọi `addOnlinePlayer()` và hàm gỡ người chơi khỏi danh sách.
- Mỗi người chỉ sửa code thuộc phần mình; thay đổi interface dùng chung (mục 5) phải báo người kia.
- Đề yêu cầu "server lưu toàn bộ thông tin", nên CSDL và tài khoản nằm trên server ngay từ Phase 1, không làm bản offline rồi bỏ.

## 3. Phase 1: Nền tảng (2 người làm song song)

### Dev 1: engine, chơi với máy, tài khoản

**Sản phẩm:** đấu máy hoàn chỉnh; đăng ký, đăng nhập, đăng xuất chạy được với server.

- `core`:
  - Mô hình bàn chơi lưới 12x12 (10x10 + viền).
  - `PathFinder`: kiểm tra 2 ô nối được bằng ≤ 3 đoạn, trả về đường đi để vẽ.
  - `BoardGenerator`: sinh đề, kiểm tra bế tắc, xáo bài (có seed).
  - `Bot`: tìm cặp hợp lệ, `T_delay` theo mức độ.
  - Unit test cho toàn bộ phần trên.
- `app`, chơi với máy:
  - `BoardView`: vẽ 10x10 ô ảnh, chọn 2 ô, vẽ đường nối, ẩn ô.
  - Màn chọn độ khó, màn đấu máy (người và bot đua trên cùng một đề), đồng hồ, nút Thoát, dialog kết quả.
  - Gửi `UPDATE_STATUS` (luyện tập / rỗi) khi bắt đầu và kết thúc ván với máy.
- Tài khoản (làm sau `core`, khi hạ tầng của Dev 2 đã chạy):
  - Màn Đăng nhập / Đăng ký, `ClientSession`.
  - Server: `LoginController` (đăng ký, đăng nhập, đăng xuất, chặn một tài khoản đăng nhập ở hai máy), `PlayerDAO` (`checkLogin()`, `addPlayer()`, băm mật khẩu).

### Dev 2: hạ tầng, sảnh, bảng xếp hạng

**Sản phẩm:** server chạy được, client kết nối được, xem được danh sách online và bảng xếp hạng.

- Hạ tầng (làm trước, Dev 1 cần cho phần tài khoản):
  - `ServerSocket` đa luồng, mỗi client một `ClientHandler`; `Message` và danh sách loại message.
  - Tầng network client: kết nối, gửi/nhận message JSON trên luồng nền, đẩy sự kiện về UI thread.
  - CSDL: các bảng `tblPlayer`, `tblMatch`, `tblMatchResult` (như Phần I §3.2), lớp `DAO`, dữ liệu mẫu.
- Sảnh: `OnlinePlayerManager` (danh sách online, trạng thái rỗi / luyện tập / bận, lời mời đang chờ), màn hình đấu đối kháng (`LobbyActivity`) với danh sách online, tự đẩy `ONLINE_LIST`.
- Bảng xếp hạng: `PlayerRankingDAO` (truy vấn 3 tiêu chí, đồng hạng; tổng điểm cho danh sách online), `RankingController`, màn Bảng xếp hạng. Có thể nhập sẵn dữ liệu trận giả để test.

### Việc chung ngày đầu

- Tạo cấu trúc 3 module (Dev 1 tạo `core`, Dev 2 tạo `server`).
- Thống nhất interface `core` (mục 5.1) và danh sách message (mục 5.2).

## 4. Phase 2: Đối kháng online

### Dev 1: chơi ván và trọng tài

- `MatchActivity`: nhận đề từ server thay vì tự sinh; gửi `MOVE`, cập nhật bàn theo `MOVE_RESULT`; thay bàn khi nhận `BOARD_SHUFFLED`; vẽ bàn thu nhỏ và số ô còn lại của đối thủ theo `OPPONENT_PROGRESS`; nút Thoát gửi `LEAVE_MATCH`.
- `MatchRoom`: giữ một bản bàn riêng cho mỗi người (cùng đề ban đầu), các thao tác trên phòng được đồng bộ. Phòng do `MatchManager.createMatch()` của Dev 2 tạo.
- `MatchController.handleMove()`: kiểm tra nước đi bằng `PathFinder` trên bàn của chính người gửi, ghi nhận thời điểm hoàn thành theo đồng hồ server; bàn của ai bế tắc thì xáo riêng bàn đó và gửi `BOARD_SHUFFLED` cho người đó; hết ô thì gọi `endMatch()` của Dev 2.

### Dev 2: thách đấu, kết thúc ván và tái đấu

- Thách đấu và trả lời lời mời: `ChallengeConfirmDialog` (kèm popup chờ có nút Hủy lời mời), `InvitationDialog` (popup không chặn), `BaseActivity` (nhận lời mời ở mọi màn hình, hàng đợi lời mời), `ChallengeController` (`INVITE`, `INVITE_CANCEL`, `INVITE_REPLY`, `UPDATE_STATUS`, lời mời hết hạn sau 30 giây, người được mời đang bận, hai người mời nhau cùng lúc, đóng các lời mời khác khi một người vào ván); khi đồng ý thì sinh đề bằng `generateBoard()` của `core`, tạo ván bằng `MatchManager.createMatch()` (Dev 2 cài `MatchManager`: tạo, tra cứu và bỏ phòng chơi), chuyển hai người sang bận bằng `updateStatus()`, gửi `MATCH_START`.
- Kết thúc ván: `endMatch()` xác định kết quả theo lý do (xóa hết ô, hết giờ, thoát, mất kết nối) và chỉ kết thúc một lần dù hai người về đích gần như cùng lúc; bộ hẹn giờ 10 phút và `onTimeout()`; `handleLeaveMatch()` (người thoát bị xử thua và được coi như đã dừng); mất kết nối giữa ván (lỗi đọc hoặc quá 15 giây không có `PING`) bị xử thua; `MatchDAO.saveMatch()` lưu ván và hai kết quả trong một giao dịch; gửi `MATCH_END`.
- Tái đấu: `ResultDialog` (đếm ngược 30 giây), `handleRematchReply()`, `stopRematch()`, `onRematchTimeout()`: cả hai tái đấu thì bắt đầu ván mới; một người dừng, hết 30 giây hoặc mất kết nối ở dialog kết quả thì gửi `OPPONENT_LEFT`, trả hai người về rỗi, bỏ phòng, gửi lại danh sách online.

## 5. Hợp đồng giao tiếp

### 5.1. Interface `core` (Dev 1 cung cấp)

Chữ ký dự kiến, chốt chi tiết ngày đầu. Tên phương thức giữ đúng tên ở biểu đồ lớp phân tích (`docs/pha2-phan-tich.md`) để truy vết được:

```java
// Toạ độ một ô. core tự định nghĩa, không dùng java.awt.Point (Android không có java.awt)
public final class Cell {
    public final int row, col;
}

// Bàn chơi: lưới 12x12 gồm vùng chơi 10x10 và viền trống, ô trống = 0
public final class Board {
    public Board(int rowCount, int columnCount, int[][] tiles);
    public int[][] getTiles();
    public int getTile(Cell c);
    public void removePair(Cell a, Cell b);
    public boolean isCleared();
    public int countRemaining();
    public Board copy();
}

public final class PathFinder {
    // Các điểm của đường nối (2 đầu mút + tối đa 2 góc), hoặc null nếu không nối được
    public static List<Cell> checkPath(Board board, Cell a, Cell b);
    // Một cặp còn nối được, hoặc null nếu bàn bế tắc
    public static Cell[] findAvailablePair(Board board);
}

public final class BoardGenerator {
    public static Board generateBoard(int rows, int cols, int iconTypes, long seed);
    public static Board shuffle(Board board, long seed);
}

public final class Bot {
    public Bot(Difficulty difficulty);
    public long getDelayMs();          // T_delay theo độ khó
    public Cell[] nextMove(Board board);
}
```

### 5.2. Giao thức message (Dev 2 chốt trước khi vào Phase 2)

JSON, mỗi message một dòng, trường `type` xác định loại:

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

Ván mới sau khi cả hai chọn Tái đấu bắt đầu bằng một `MATCH_START` mới.

## 6. Cân bằng khối lượng

| Tiêu chí | Dev 1 | Dev 2 |
| --- | --- | --- |
| Phase 1 | Nặng: thuật toán nối ô, sinh đề, bot, màn đấu máy; thêm tài khoản | Nặng: server đa luồng, network client, CSDL; sảnh và bảng xếp hạng nhẹ hơn |
| Phase 2 | Vừa: trọng tài và màn đấu online | Nặng: lời mời, mọi cách kết thúc ván, tái đấu, các tình huống biên |
| Phụ thuộc | Tài khoản cần hạ tầng của Dev 2; ván online cần `MATCH_START` của Dev 2 | Sinh đề cần `core` của Dev 1; kết thúc ván cần `MatchRoom` của Dev 1 |
| Rủi ro xung đột | Trung bình: `MatchController` làm chung (ranh giới ở mục 2) | Trung bình: như trên |

Dev 1 xong Phase 2 sớm hơn có thể nhận thêm: kiểm thử tích hợp, vẽ sơ đồ cho tài liệu.

## 7. Phân công tài liệu đặc tả

Tài liệu vẫn viết theo modul (Pha 2–5 mỗi modul một bộ); trong một modul, ai làm chức năng nào thì viết phần đó:

| Phần tài liệu | Người làm |
| --- | --- |
| Phần dùng chung: §1.1, §1.2, use case tổng quan §1.3, §1.4, lớp thực thể §2.2, §3.1, CSDL §3.2 | Cả hai, làm trước khi tách modul |
| M1 (quản lý tài khoản), từ use case chi tiết đến kiểm thử | Dev 1 |
| M2 (xem bảng xếp hạng), từ use case chi tiết đến kiểm thử | Dev 2 |
| M3 (thi đấu với máy), từ use case chi tiết đến kiểm thử; thuật toán §3.5 | Dev 1 |
| M4, phần thách đấu và trả lời lời mời (kịch bản bước 1–8 và các ngoại lệ 2a–7e) | Dev 2 |
| M4, phần chơi ván (kịch bản bước 9–11, ngoại lệ 10a, 10b) | Dev 1 |
| M4, phần kết thúc ván và tái đấu (kịch bản bước 12–14, ngoại lệ 9a, 12a–12c, 13a–13d) | Dev 2 |
| Giao thức §3.4 | Dev 2 |
| Thiết kế triển khai §3.3.3, tổ chức dự án §4.1, rà soát §5.1 | Cả hai |

## 8. Vấn đề cần chốt

Đề bài chưa nói rõ, cần thống nhất trước khi viết kịch bản và code phần liên quan:

- [x] **Điều kiện thắng/hòa:** ai xóa hết ô trước thắng; hết giờ mà cả 2 chưa xong thì hòa; thoát ngang hoặc mất kết nối thì thua.
- [x] **Thời gian giới hạn** mỗi ván: 10 phút.
- [x] **Trung bình điểm đối thủ:** trung bình tổng điểm hiện tại của đối thủ, tính theo từng ván.
- [x] **Trận đấu máy** không tính vào BXH và không lưu.
- [x] **Trạng thái người chơi:** ba trạng thái, nhãn hiển thị trên giao diện là Rỗi, Luyện tập, Bận: `IDLE` (rỗi), `TRAINING` (đang chơi với máy, vẫn tính là online và vẫn nhận lời mời; đồng ý thì bỏ ván với máy để vào trận), `BUSY` (trong trận với người khác, không nhận lời mời; chỉ server đặt).
- [x] **Xáo bài khi bế tắc:** server xáo riêng bàn của người bị bế tắc và chỉ gửi cho người đó (`BOARD_SHUFFLED`). Hai người chỉ chung đề ban đầu; sinh đề "luôn giải được" không thay được bước xáo.
- [x] **Đồng hồ:** server giữ đồng hồ 10 phút, tự kết thúc ván khi hết giờ và ghi `finishTime` theo giờ server; client chỉ hiển thị.
- [x] **Cách tính điểm:** theo đề, thắng 1, hòa 0.5, thua 0 (`de_bai.md` là nguồn quyết định). Điểm suy ra từ `result` nên CSDL không lưu cột điểm.
- [x] **Lưu bàn chơi:** CSDL lưu đề bài ban đầu (`tblMatch.tiles`) và bàn của từng người lúc ván kết thúc (`tblMatchResult.tiles`), theo quan hệ `Match` ◇ `Board` và `MatchResult` ◇ `Board` ở biểu đồ lớp thực thể.
- [ ] **AWT với ván thắng do đối thủ bỏ cuộc:** đề xuất không tính vào AWT (người thắng chưa xóa hết ô nên không có `finishTime`).
- [ ] **Một tài khoản đăng nhập ở 2 máy:** đề xuất từ chối lần đăng nhập sau (`LOGIN_RESULT` báo "tài khoản đang đăng nhập ở máy khác"), khớp ngoại lệ 6b của kịch bản M1.
- [x] **Mất kết nối ngoài ván:** đang có lời mời chờ thì mọi lời mời đi và đến người đó bị đóng với lý do `OFFLINE`; đang ở dialog kết quả thì coi như chọn Dừng (`OPPONENT_LEFT`).
- [x] **Trạng thái trong lúc ở dialog kết quả (hỏi tái đấu):** vẫn là bận cho đến khi người đó rời dialog; dialog có hạn 30 giây, hết hạn coi như chọn Dừng; đã chọn Tái đấu vẫn đổi sang Dừng được.
- [ ] **Số loại icon** trên bàn 10x10: đề xuất 25 loại, mỗi loại 4 ô.
- [ ] **CSDL server:** MySQL hay SQLite. Đề xuất MySQL (server đa luồng ghi đồng thời, giáo trình cũng dùng MySQL).
- [ ] **Phản hồi nước đi trong ván online:** hiện `MatchActivity` gửi `MOVE` rồi chờ `MOVE_RESULT` mới xóa ô, mạng chậm sẽ thấy giật. Đề xuất client kiểm tra ngay bằng `PathFinder` của `core` và xóa ô trên máy, server vẫn kiểm tra lại; server từ chối thì trả bàn về như cũ. Nếu chốt thì sửa pha 3 mục 4d, mục 7 và kịch bản v3 M4.
- [x] **Phát hiện mất kết nối (heartbeat):** client gửi `PING` mỗi 5 giây, server đặt thời gian chờ đọc 15 giây rồi xử lý như mất kết nối. Client mất kết nối thì xóa phiên, về giao diện chính chế độ khách và phải đăng nhập lại.
- [x] **Thoát hoặc mất kết nối giữa ván (2026-10-27):** người thoát bị xử thua (0 điểm, không trừ điểm) và được coi như đã chọn Dừng: về sảnh ngay với thông báo, không hỏi tái đấu; người còn lại thấy dialog kết quả chỉ có nút Về sảnh.
- [x] **Lời mời (2026-10-27):** lời mời hết hạn sau 30 giây tính từ lúc gửi; người mời thấy popup chờ chặn màn hình có nút Hủy lời mời, nên mỗi lúc chỉ có một lời mời đi; người được mời thấy popup không chặn (ván với máy vẫn chạy), nhiều lời mời thì xếp hàng trên máy và hiện lần lượt. Khi một người vào ván, mọi lời mời khác đi và đến người đó bị đóng.
- [x] **Mã ván trong lúc chơi (2026-10-27):** ván đang diễn ra dùng mã phòng `roomId` do `MatchManager` cấp trong bộ nhớ; `Match.id` của CSDL chỉ có khi lưu lúc kết thúc.
- [x] **Người chưa có ván trong bảng xếp hạng (2026-10-27):** không có trong bảng xếp hạng, vì không có trung bình điểm đối thủ để xếp theo tiêu chí 2; danh sách online vẫn hiện họ với 0 điểm.
- [ ] **Vòng đời Android:** app xuống nền có thể bị thu hồi làm mất kết nối, người chơi bị xử thua. Tối thiểu: giữ `ServerConnection` ở mức ứng dụng (đã là singleton) và gỡ `MessageListener` khi màn hình đóng; làm kỹ hơn thì giữ kết nối trong một Service. Với phạm vi đồ án đề xuất làm mức tối thiểu.
