"""Dựng Google Doc "thiết kế hệ thống và các modul của Dev 2" từ docs/pha1..3-*.md.

Lấy phần dùng chung và các mục của M2 (xem bảng xếp hạng), M4 (thách đấu và đấu đối kháng); bỏ các mục của
Dev 1 (M1, M3, thuật toán đường nối, sinh đề, bot, sơ đồ tuần tự phần chơi ván). Số mục và số hình giữ như tài
liệu chung để các tham chiếu "mục 4d", "Hình 2.1" vẫn đúng. Ảnh được nhúng thẳng vào HTML (data URI).

    python build_gdoc.py --html out.html          # chỉ ghi HTML để xem trước
    python build_gdoc.py                          # tạo Google Doc mới, in link
    python build_gdoc.py --doc <id>               # ghi đè nội dung Doc đã có (bản cũ vẫn trong lịch sử phiên bản)

Cần token OAuth của bộ công cụ gdoc.py (xem GDOC_TOOLS).
"""
import argparse
import base64
import io
import re
import sys
from pathlib import Path

import markdown
from PIL import Image

DOCS = Path(__file__).resolve().parent.parent
GDOC_TOOLS = Path("D:/Stud Mats/Kỳ 1 năm 4/qldupm/tools")
TITLE = "BTL Mobile - Thiết kế hệ thống và các modul của Dev 2"

# file -> {số mục cấp 2: None (lấy cả mục) hoặc các chữ cái mục cấp 3 được lấy (kèm đoạn mở đầu của mục cấp 2)}
SELECT = {
    "pha1-thu-thap-yeu-cau.md": {"1": None, "2": None, "3": "ace"},
    "pha2-phan-tich.md": {"1": "bd", "2": None, "3": "bd", "4": "bd", "5": "bd", "6": "bd"},
    "pha3-thiet-ke.md": {"1": None, "2": None, "3": None, "4": "bd", "5": "bd", "6": None, "7": None, "8": "d"},
}

INTRO = (
    "Tài liệu trích từ bản đặc tả chung của nhóm, gồm phần dùng chung của hệ thống và các modul do Dev 2 phụ "
    "trách: xem bảng xếp hạng (M2), thách đấu và trả lời lời mời, kết thúc ván và tái đấu (thuộc M4). Các mục "
    "của Dev 1 (quản lý tài khoản, chơi với máy, phần chơi ván của M4, thuật toán) được lược bỏ; số mục và số "
    "hình giữ như bản chung."
)

PAGE_WIDTH_PX = 624  # 6.5 inch ở 96 dpi
PAGE_HEIGHT_PX = 853  # 640 pt: vừa một trang Letter lề 1 inch, chừa chỗ cho chú thích


def pick(md, select):
    """Giữ tiêu đề cấp 1, các mục cấp 2 và cấp 3 được chọn."""
    out, keep, h2 = [], True, None
    for line in md.splitlines():
        if line.startswith("# "):
            keep = True
        elif m := re.match(r"## (\d+)\.", line):
            h2 = m[1]
            keep = h2 in select
        elif m := re.match(r"### ([a-z])\.", line):
            keep = h2 in select and (select[h2] is None or m[1] in select[h2])
        elif line.startswith("### "):  # "### Bước 1: ..." thuộc mục cấp 2 lấy cả
            keep = h2 in select and select[h2] is None
        if keep:
            out.append(line)
    return "\n".join(out)


DEV1_FIGURES = ["uml/pha3/ui-m4-2.png", "uml/pha3/seq-m4-2.png"]  # giao diện và tuần tự phần chơi ván (Dev 1)


def adjust(md):
    md = re.sub(r"^(##+ .*?) (?:cho )?bốn modul", r"\1 cho các modul", md, flags=re.M)
    for src in DEV1_FIGURES:
        md = re.sub(rf"!\[[^\]]*\]\({re.escape(src)}\)\n\n\*Hình [^\n]*\*\n\n", "", md)
    for old, new in [
        (r"Chơi ván như Hình 3\.\d+\.", "Chơi ván thuộc phần của Dev 1."),
        (r"phần 2 là vòng lặp chơi ván \(Hình 3\.\d+\)", "phần 2 là vòng lặp chơi ván (thuộc phần của Dev 1)"),
        (r"thuộc modul của Dev 1, chưa vẽ\.", "thuộc phần của Dev 1."),
    ]:
        md, n = re.subn(old, new, md)
        if n != 1:
            raise SystemExit(f"không khớp: {old}")
    return md


def embed_images(html):
    def repl(m):
        alt, src = m[1], m[2]
        path = DOCS / src
        data = path.read_bytes()
        w, h = Image.open(io.BytesIO(data)).size
        scale = min(1, PAGE_WIDTH_PX / w, PAGE_HEIGHT_PX / h)
        width, height = round(w * scale), round(h * scale)
        b64 = base64.b64encode(data).decode()
        return f'<img alt="{alt}" width="{width}" height="{height}" src="data:image/png;base64,{b64}">'
    return re.sub(r'<img alt="([^"]*)" src="([^"]+)"\s*/?>', repl, html)


FONT = "font-family:'Times New Roman';color:#000000;"


def style(html):
    html = re.sub(r"<p>(<img [^>]*>)</p>", r'<p style="text-align:center;margin:6pt 0">\1</p>', html)
    html = re.sub(r"<p><em>(Hình [^<]*)</em></p>",
                  rf'<p style="{FONT}font-size:13pt;text-align:center;font-style:italic;margin:6pt 0">\1</p>', html)
    html = re.sub(r"<p>", f'<p style="{FONT}font-size:13pt;margin:6pt 0;text-align:justify">', html)
    html = re.sub(r"<li>", f'<li style="{FONT}font-size:13pt;margin:3pt 0">', html)
    for tag, size in (("h1", 16), ("h2", 14), ("h3", 13), ("h4", 13)):
        html = html.replace(f"<{tag}>", f'<{tag} style="{FONT}font-size:{size}pt;font-weight:bold;margin:12pt 0 6pt">')
    html = html.replace("<table>", '<table style="border-collapse:collapse;width:100%">')
    for tag in ("td", "th"):
        html = re.sub(rf"<{tag}( [^>]*)?>",
                      f'<{tag} style="{FONT}font-size:12pt;border:1px solid #000000;padding:3pt 5pt;vertical-align:top">',
                      html)
    html = html.replace("<code>", "<code style=\"font-family:'Courier New';font-size:11pt\">")
    return html


def build_html():
    parts = [f"# {TITLE}", INTRO]
    for name, select in SELECT.items():
        md = (DOCS / name).read_text(encoding="utf-8")
        parts.append(pick(md, select))
    body = markdown.markdown(adjust("\n\n".join(parts)), extensions=["tables", "sane_lists"])
    body = re.sub(r"<!--.*?-->", "", body, flags=re.S)
    body = style(embed_images(body))
    body = re.sub(r"(<h1 [^>]*>Pha)", r'<p style="page-break-before:always"></p>\1', body)
    return (f'<!DOCTYPE html><html><head><meta charset="utf-8"><title>{TITLE}</title></head>'
            f'<body style="{FONT}font-size:13pt">{body}</body></html>')


def upload(html, doc_id):
    sys.path.insert(0, str(GDOC_TOOLS))
    import gdoc
    from googleapiclient.http import MediaIoBaseUpload

    creds = gdoc.load_creds(Path(gdoc.DEFAULT_DIR) / "token.json")
    media = MediaIoBaseUpload(io.BytesIO(html.encode("utf-8")), mimetype="text/html", resumable=True)
    files = gdoc.drive(creds).files()
    if doc_id:
        f = files.update(fileId=doc_id, media_body=media, fields="id,webViewLink").execute()
    else:
        body = {"name": TITLE, "mimeType": "application/vnd.google-apps.document"}
        f = files.create(body=body, media_body=media, fields="id,webViewLink").execute()
    print(f["webViewLink"])


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    ap = argparse.ArgumentParser()
    ap.add_argument("--html", help="ghi HTML ra file, không tải lên")
    ap.add_argument("--doc", help="id Google Doc cần ghi đè")
    a = ap.parse_args()
    html = build_html()
    if a.html:
        Path(a.html).write_text(html, encoding="utf-8")
        print(f"wrote {a.html} ({len(html) // 1024} KB)")
    else:
        upload(html, a.doc)


if __name__ == "__main__":
    main()
