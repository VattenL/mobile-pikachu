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

Lấy theo 3 chức năng của đề. Mã modul dùng xuyên suốt tài liệu.

| Mã | Modul | Mức | Nội dung |
| --- | --- | --- | --- |
| M1 | Quản lí thông tin người chơi, xem BXH | Dễ | Đăng ký, đăng nhập, đăng xuất, xem bảng xếp hạng |
| M2 | Thi đấu với máy | Trung bình | Chọn độ khó, đua với bot trên cùng một đề, xem kết quả |
| M3 | Thách đấu và đấu đối kháng | Khó | Xem danh sách online, thách đấu, trả lời lời mời, chơi ván đối kháng, chọn tiếp tục / dừng |

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
| Người chơi, đối thủ, máy (bot) | Đăng ký, đăng nhập, đăng xuất, thách đấu, chấp nhận, từ chối, chọn ô, nối cặp ô, thoát ván, tiếp tục, xem bảng xếp hạng | Tài khoản, ván đấu, bàn chơi, ô, hình nhân vật, đường nối, đồng hồ, điểm, lời mời, kết quả, bảng xếp hạng |

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
- **Use case chi tiết cho từng modul M1, M2, M3:** trích phần use case tương ứng từ biểu đồ tổng quan; mỗi giao diện tương tác thành một use case con; xác định quan hệ include / extend / generalization; kèm bảng mô tả từng use case.
- Ví dụ use case con: `Nối cặp ô` được cả M2 và M3 include; `Trả lời lời mời` (M3).
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
- Ứng viên dự kiến: `Player` (người chơi), `Match` (ván đấu), `MatchResult` (kết quả của một người trong một ván), lớp thống kê xếp hạng là lớp dẫn xuất từ `MatchResult`. Bàn chơi, lời mời, trạng thái online là dữ liệu tạm thời, cần cân nhắc có phải thực thể lưu trữ hay không.

### 2.3. Trích lớp biên và lớp điều khiển, biểu đồ lớp phân tích (§3.2.3)

- **Bước 1:** mỗi giao diện thành một lớp biên. Giao diện dự kiến:
  - M1: đăng nhập, đăng ký, trang chính, bảng xếp hạng.
  - M2: chọn độ khó, bàn chơi, kết quả.
  - M3: sảnh (danh sách online), dialog lời mời, bàn chơi, dialog kết quả và hỏi tiếp tục.
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
| `tblPlayer` | `id`, `username`, `passwordHash`, `createdAt` |
| `tblMatch` | `id`, `startedAt`, `endedAt` |
| `tblMatchResult` | `id`, `points` (1 / 0.5 / 0), `finishTimeMs` (null nếu chưa hoàn thành), `matchId`, `playerId` |

- Tổng điểm, trung bình điểm đối thủ, trung bình thời gian thắng là dữ liệu dẫn xuất, tính bằng truy vấn. Trạng thái online / bận chỉ giữ trong bộ nhớ server.

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
- **Sinh đề và kiểm tra bế tắc:** sinh 100 ô (50 cặp), mỗi loại hình xuất hiện số chẵn lần; phát hiện bế tắc (còn ô nhưng không còn cặp nối được) và xáo lại. Ở chế độ đối kháng, server xáo để 2 người luôn cùng một bàn.
- **Bot:** quét bàn tìm các cặp nối được; mức dễ chọn ngẫu nhiên sau thời gian chờ `T_delay` dài, mức khó `T_delay` ngắn hơn và ưu tiên cặp mở thêm đường cho ô khác.
- **Xếp hạng:**
  - Tổng điểm: `S = Σ điểm các ván` (thắng 1, hòa 0.5, thua 0).
  - Trung bình điểm đối thủ: `AOS = Σ điểm của đối thủ / số ván` (cách tính chính xác cần chốt, xem [Phần II mục 8](#8-vấn-đề-cần-chốt)).
  - Trung bình thời gian thắng: `AWT = Σ thời gian hoàn thành các ván thắng / số ván thắng`.
  - Thứ tự: `ORDER BY totalScore DESC, avgOpponentScore DESC, avgWinTime ASC`.

## Chương 4. Cài đặt (giáo trình chương 5)

### 4.1. Tổ chức dự án (§5.1)

- 3 module Gradle `core`, `app`, `server` (xem [Phần II mục 1](#1-kiến-trúc-module)), các gói chia theo tầng như biểu đồ gói §3.3.3.
- Môi trường: Java, Android SDK / Android Studio, Java SE cho server, JDBC, MySQL hoặc SQLite trên server, JUnit.

### 4.2. Cài đặt các modul (§5.2)

- Mỗi modul M1, M2, M3: trình bày code các lớp trong biểu đồ lớp thiết kế (lớp giao diện, lớp điều khiển phía server, DAO) và giải thích cách các phương thức hoạt động. Lớp thực thể không cần trình bày code.

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

- **Kết luận:** mức độ hoàn thành so với 3 chức năng của đề (Dễ, Trung bình, Khó); ưu điểm, hạn chế.
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

- **Dev A** sở hữu `core` và màn chơi trên `app`.
- **Dev B** sở hữu `server` và các màn liên quan mạng trên `app` (đăng nhập, lobby, BXH) cùng tầng network client.
- Mỗi người chỉ sửa code thuộc phần mình sở hữu; thay đổi interface dùng chung phải báo người kia.
- Đề yêu cầu "server lưu toàn bộ thông tin", nên CSDL và tài khoản nằm trên server ngay từ Phase 1, không làm bản offline rồi bỏ.

## 3. Phase 1: Nền tảng (2 người làm song song)

### Dev A: game engine và chơi với máy

**Sản phẩm:** app chơi đơn và đấu máy hoàn chỉnh, chạy offline.

- `core`:
  - Mô hình bàn chơi lưới 12x12 (10x10 + viền).
  - `PathFinder`: kiểm tra 2 ô nối được bằng ≤ 3 đoạn, trả về đường đi để vẽ.
  - `BoardGenerator`: sinh đề, kiểm tra bế tắc, xáo bài (có seed).
  - `Bot`: tìm cặp hợp lệ, `T_delay` theo mức độ.
  - Unit test cho toàn bộ phần trên.
- `app`:
  - Màn bàn chơi: vẽ 10x10 ô ảnh, chọn 2 ô, vẽ đường nối, ẩn ô.
  - Đồng hồ, nút Thoát, màn kết quả.
  - Chế độ đấu máy: người và bot đua trên cùng một đề.

### Dev B: server, tài khoản, bảng xếp hạng

**Sản phẩm:** server chạy được, client đăng nhập được, xem được danh sách online và BXH.

- `server`:
  - `ServerSocket` đa luồng, mỗi client một `ClientHandler`.
  - Quản lý session, danh sách online, trạng thái rỗi / bận.
  - CSDL: các bảng `users`, `matches`, `match_results`; đăng ký, đăng nhập (hash mật khẩu).
  - Truy vấn BXH 3 tiêu chí. Có thể nhập sẵn dữ liệu trận giả để test.
- `app`:
  - Tầng network client: kết nối, gửi/nhận message JSON trên luồng nền, đẩy sự kiện về UI thread.
  - Màn Đăng nhập / Đăng ký, Lobby (danh sách online), Bảng xếp hạng.

### Việc chung ngày đầu

- Tạo cấu trúc 3 module (Dev A tạo `core`, Dev B tạo `server`).
- Thống nhất interface `core` (mục 5.1) và danh sách message (mục 5.2).

## 4. Phase 2: Đối kháng online

### Dev A: luồng trận đấu phía client

- Màn bàn chơi nhận đề từ server thay vì tự sinh.
- Gửi nước đi lên server, cập nhật bàn theo phản hồi.
- Dialog: nhận lời thách đấu (OK / Reject), kết quả ván (thắng / hòa / thua), hỏi tiếp tục, thông báo đối thủ đã thoát.

### Dev B: phòng chơi và trọng tài

- Luồng thách đấu: chuyển lời mời, phản hồi, đối thủ đang bận.
- Tạo phòng, sinh đề bằng `core`, gửi cùng đề cho 2 client.
- Kiểm tra nước đi bằng `PathFinder`, ghi nhận thời điểm hoàn thành của mỗi bên.
- Xác định kết quả, tính điểm, lưu CSDL, gửi kết quả cho 2 bên.
- Xử lý tái đấu, một bên dừng, mất kết nối giữa trận (xử thua).

## 5. Hợp đồng giao tiếp

### 5.1. Interface `core` (Dev A cung cấp)

Chữ ký dự kiến, chốt chi tiết ngày đầu:

```java
// Bàn chơi: lưới có viền, ô trống = 0
public final class Board {
    public static Board fromMatrix(int[][] cells);
    public int[][] toMatrix();
    public int get(int row, int col);
    public void remove(Point a, Point b);
    public boolean isCleared();
}

public final class PathFinder {
    // Trả về các điểm trên đường nối (tối đa 2 góc), hoặc null nếu không nối được
    public static List<Point> findPath(Board board, Point a, Point b);
}

public final class BoardGenerator {
    public static Board generate(int rows, int cols, int iconTypes, long seed);
    public static boolean hasAnyMove(Board board);
    public static Board shuffle(Board board, long seed);
}

public final class Bot {
    public Bot(Difficulty difficulty);
    public Move nextMove(Board board);
}
```

### 5.2. Giao thức message (Dev B chốt trước khi vào Phase 2)

JSON, mỗi message một dòng, trường `type` xác định loại:

| Chiều | `type` | Nội dung chính |
| --- | --- | --- |
| C → S | `LOGIN` / `REGISTER` | `username`, `password` |
| S → C | `LOGIN_RESULT` | `ok`, `error` |
| S → C | `ONLINE_LIST` | danh sách `{username, totalScore, status}` |
| C → S | `GET_RANKING` | |
| S → C | `RANKING` | danh sách đã sắp xếp |
| C → S | `INVITE` | `to` |
| S → C | `INVITE_RECEIVED` | `from` |
| C → S | `INVITE_REPLY` | `from`, `accept` |
| S → C | `MATCH_START` | `matchId`, `matrix`, `timeLimitSec` |
| C → S | `MOVE` | `matchId`, `p1`, `p2` |
| S → C | `MOVE_RESULT` | `ok`, `path`, hoặc bàn mới nếu đã xáo |
| S → C | `MATCH_END` | `result` (WIN / DRAW / LOSE), `points`, thời gian 2 bên |
| C → S | `REMATCH_REPLY` | `matchId`, `accept` |
| S → C | `OPPONENT_LEFT` | |

## 6. Cân bằng khối lượng

| Tiêu chí | Dev A | Dev B |
| --- | --- | --- |
| Phase 1 | Khó nhất: thuật toán nối ô, bot | Khó nhất: server đa luồng, network client |
| Phase 2 | Nhẹ hơn: gắn UI vào luồng mạng | Nặng hơn: phòng chơi, trọng tài, đồng bộ |
| Phụ thuộc | Phase 2 cần message từ Dev B | Phase 2 cần `core` của Dev A |
| Rủi ro xung đột | Thấp: chủ yếu sửa `core` và màn chơi | Thấp: chủ yếu sửa `server` và màn lobby |

Ở Phase 2, Dev A xong sớm hơn có thể nhận thêm: kiểm thử tích hợp, vẽ sơ đồ cho tài liệu.

## 7. Phân công tài liệu đặc tả

Chia theo modul, khớp với phần code mỗi người sở hữu:

| Phần tài liệu | Người làm |
| --- | --- |
| Phần dùng chung: §1.1, §1.2, use case tổng quan §1.3, §1.4, lớp thực thể §2.2, §3.1, CSDL §3.2 | Cả hai, làm trước khi tách modul |
| Modul M2 (thi đấu với máy), từ use case chi tiết đến kiểm thử; thuật toán §3.5 | Dev A |
| Modul M1 (người chơi, BXH), từ use case chi tiết đến kiểm thử; giao thức §3.4 | Dev B |
| Modul M3 (đối kháng): Dev B chủ trì; Dev A làm phần giao diện (§2.3, §3.3.1 bước 1-2) | Dev B + Dev A |
| Thiết kế triển khai §3.3.3, tổ chức dự án §4.1, rà soát §5.1 | Cả hai |

## 8. Vấn đề cần chốt

Đề bài chưa nói rõ, cần thống nhất trước khi viết kịch bản và code phần liên quan:

- [x] **Điều kiện thắng/hòa:** ai xóa hết ô trước thắng; hết giờ mà cả 2 chưa xong thì hòa; thoát ngang hoặc mất kết nối thì thua.
- [x] **Thời gian giới hạn** mỗi ván: 10 phút.
- [x] **Trung bình điểm đối thủ:** trung bình tổng điểm hiện tại của đối thủ, tính theo từng ván.
- [x] **Trận đấu máy** không tính vào BXH và không lưu.
- [ ] **Cách tính điểm:** theo đề (thắng 1, hòa 0.5, thua 0) hay Elo? Bàn sau; tài liệu pha 1, 2 chỉ ghi "điểm theo kết quả ván".
- [ ] **Xáo bài khi bế tắc:** server xáo rồi gửi cho cả 2, hay sinh đề luôn giải được để không cần xáo?
- [ ] **Số loại icon** trên bàn 10x10.
- [ ] **CSDL server:** MySQL hay SQLite.
