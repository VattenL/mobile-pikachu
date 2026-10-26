# Nguồn tài nguyên sử dụng

Mọi tài nguyên lấy từ bên ngoài (thư viện, hình ảnh, phông chữ, âm thanh, đoạn code tham khảo, tài liệu) được ghi ở đây, kèm nguồn và giấy phép. Thêm tài nguyên mới thì bổ sung vào bảng tương ứng.

## Thư viện phần mềm

| Thư viện | Phiên bản | Dùng ở | Nguồn | Giấy phép |
|---|---|---|---|---|
| AndroidX AppCompat, Activity, ConstraintLayout, RecyclerView | xem `gradle/libs.versions.toml` | `app` | https://developer.android.com/jetpack/androidx | Apache License 2.0 |
| Gson | 2.13.1 | `core` (message JSON) | https://github.com/google/gson | Apache License 2.0 |
| MySQL Connector/J | 9.3.0 | `server` (JDBC) | https://dev.mysql.com/downloads/connector/j/ | GPL 2.0 kèm Universal FOSS Exception |
| Material Components for Android | 1.14.0 | `app` (giao diện) | https://github.com/material-components/material-components-android | Apache License 2.0 |
| JUnit 4 | 4.13.2 | test | https://junit.org/junit4/ | Eclipse Public License 1.0 |
| Gradle Wrapper | 9.5.0 | build | https://gradle.org | Apache License 2.0 |

## Hình ảnh, phông chữ

| Tài nguyên | Dùng ở | Nguồn | Giấy phép, ghi chú |
|---|---|---|---|
| Biểu tượng ứng dụng | `app/src/main/res/mipmap-*` | Mẫu mặc định của Android Studio | Apache License 2.0 |

"Pikachu" và các nhân vật Pokémon là nhãn hiệu, tác phẩm thuộc Nintendo, Creatures Inc., GAME FREAK inc. và The Pokémon Company.

## Công cụ và tài liệu

| Tài nguyên | Dùng ở | Nguồn | Ghi chú |
|---|---|---|---|
| PlantUML (máy chủ plantuml.com) | Vẽ sơ đồ trong `docs/uml/` | https://plantuml.com | Mã nguồn sơ đồ do nhóm viết; ảnh PNG được máy chủ PlantUML sinh ra |
| Visual Paradigm Community Edition 17.3 | Vẽ sơ đồ trong `docs/vp/` (`pikachu.vpp`, ảnh `docs/vp/png/`) | https://www.visual-paradigm.com | Bản miễn phí cho mục đích phi thương mại; ảnh xuất ra có watermark "Powered By Visual Paradigm Community Edition" |
| Plugin MCP cho Visual Paradigm (fork `thanhtrnnn/vp-mcp` của plugin của Manoel Brunnen) | `docs/vp/build_vp.py` gọi plugin để dựng sơ đồ | https://github.com/thanhtrnnn/vp-mcp | Apache License 2.0; bản cài trên máy đã sửa để chỉ nhận kết nối từ localhost |
| Bài giảng *Thực tập tốt nghiệp 2: Công nghệ phần mềm* (BG HP TTTN 2 CNPM), PTIT, 2020 | Phương pháp, khuôn các pha trong `docs/`; checklist rà soát và kiểm thử ở `docs/pha5-ra-soat-kiem-thu.md` rút gọn từ Bảng 6.x của bài giảng | Tài liệu học phần | Tài liệu nội bộ của học phần |
| Đề bài | `de_bai.md` | Đề bài tập lớn của học phần | Luật chơi, cách tính điểm, tiêu chí xếp hạng |

## Dữ liệu mẫu

Tên người chơi trong các kịch bản (Hoàng Nam, Minh Anh, Quốc Bảo, Thu Hà, Đức Long, Lan Anh) là tên giả định, không phải người thật.
