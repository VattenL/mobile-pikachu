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

