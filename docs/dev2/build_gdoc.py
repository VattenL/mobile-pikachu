"""Dựng Google Doc "thiết kế hệ thống và các modul của Dev 2" từ docs/pha1..3-*.md.

Lấy phần dùng chung và các mục của M2 (xem bảng xếp hạng), M4 (thách đấu và đấu đối kháng); bỏ các mục của
Dev 1 (M1, M3, thuật toán đường nối, sinh đề, bot, phần chơi ván của M4). Chữ cái các mục cấp 3 được đánh lại liên
tiếp (b, d thành a, b) và các tham chiếu "mục 4d" đổi theo; số hình giữ như tài liệu chung. Các bước chơi ván (phần
của Dev 1) trong kịch bản v2, v3 của M4 được gộp thành một dòng. Ảnh được nhúng thẳng vào HTML (data URI).

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
    "pha2-phan-tich.md": {"1": "bd", "2": None, "3": "bd", "4": "bd", "5": "bd"},
    "pha3-thiet-ke.md": {"1": None, "2": None, "3": None, "4": "bd", "5": "bd", "6": None, "7": None, "8": "d"},
}

INTRO = (
    "Tài liệu trích từ bản đặc tả chung của nhóm, gồm phần dùng chung của hệ thống và các modul do Dev 2 phụ "
    "trách: xem bảng xếp hạng (M2), thách đấu và trả lời lời mời, kết thúc ván và tái đấu (thuộc M4). Các mục "
    "của Dev 1 (quản lý tài khoản, chơi với máy, phần chơi ván của M4, thuật toán) được lược bỏ; số "
    "hình giữ như bản chung, chữ cái các mục được đánh lại liên tiếp."
)

PAGE_WIDTH_PX = 624  # 6.5 inch ở 96 dpi
PAGE_HEIGHT_PX = 853  # 640 pt: vừa một trang Letter lề 1 inch, chừa chỗ cho chú thích


def pick(md, select):
    """Giữ tiêu đề cấp 1, các mục cấp 2 và cấp 3 được chọn; chữ cái mục cấp 3 đánh lại liên tiếp trong mỗi mục cấp 2
    và các tham chiếu "mục 4d" trong cùng file đổi theo."""
    out, keep, h2, letters = [], True, None, {}
    for line in md.splitlines():
        if line.startswith("# "):
            keep = True
        elif m := re.match(r"## (\d+)\.", line):
            h2 = m[1]
            keep = h2 in select
        elif m := re.match(r"### ([a-z])\. ", line):
            keep = h2 in select and (select[h2] is None or m[1] in select[h2])
            if keep:
                new = "abcdefgh"[sum(1 for k in letters if k[0] == h2)]
                letters[h2, m[1]] = new
                line = f"### {new}. " + line[len(m[0]):]
        elif line.startswith("### "):  # "### Bước 1: ..." thuộc mục cấp 2 lấy cả
            keep = h2 in select and select[h2] is None
        if keep:
            out.append(line)

    def ref(m):
        if (m[1], m[2]) not in letters:
            raise SystemExit(f"tham chiếu tới mục không có trong tài liệu Dev 2: {m[0]}")
        return f"mục {m[1]}{letters[m[1], m[2]]}"
    return re.sub(r"mục (\d)([a-e])\b", ref, "\n".join(out))


def indent_lists(md):
    """Python-Markdown chỉ nhận danh sách con thụt 4 dấu cách; md thụt 2 nên nhân đôi (ngoài khối code)."""
    out, code = [], False
    for line in md.splitlines():
        if line.startswith("```"):
            code = not code
        elif not code:
            line = re.sub(r"^((?:  )+)(?=[-*] |\d+\. )", lambda m: m[1] * 2, line)
        out.append(line)
    return "\n".join(out)


def fold_play_steps(md):
    """Kịch bản v2, v3 của M4: các bước chơi ván trong vòng lặp (phần của Dev 1) gộp thành một dòng."""
    out, n = [], 0
    for line in md.split("\n"):
        m = re.match(r"\(Lặp lại các bước (\d+)(?:-| đến )(\d+) .*B chơi song song", line)
        if m:
            a, b = int(m[1]), int(m[2])
            while out and (k := re.match(r"(\d+)\. ", out[-1])) and a <= int(k[1]) <= b:
                out.pop()
            # dòng trống hai bên để danh sách sau bắt đầu lại đúng số bước
            out += ["", f"(Các bước {a} đến {b}: chơi ván, thuộc phần của Dev 1; lặp đến khi một người xóa hết ô.)", ""]
            n += 1
        else:
            out.append(line)
    md = "\n".join(out)
    if n != 2:
        raise SystemExit(f"gộp bước chơi ván: {n} chỗ, cần 2")
    return md


DEV1_FIGURES = ["uml/pha3/ui-m4-2.png"]  # giao diện phần chơi ván (Dev 1)
# hình dùng bản riêng cho tài liệu Dev 2 (docs/vp/build_vp.py): hoạt động và tuần tự M4 không có vòng lặp chơi ván
DEV2_FIGURES = {"vp/png/activity-m4.png": "vp/png/activity-m4-dev2.png",
                **{f"vp/png/seq-analysis-m4-full-{i}.png": f"vp/png/seq-analysis-m4-dev2-{i}.png" for i in (1, 2)},
                **{f"vp/png/seq-m4-full-{i}.png": f"vp/png/seq-m4-dev2-{i}.png" for i in (1, 2, 3)}}
# đoạn dẫn của các hình tuần tự M4 nói theo bản Dev 2: (mẫu, hàm thay); số bước lấy từ chính câu trong md
DEV2_TEXT = [
    (r"Biểu đồ vẽ đủ các bước 1–(\d+) của kịch bản phiên bản 2 trong một biểu đồ, kể cả phần chơi ván "
     r"\(bước (\d+)–(\d+), thuộc modul của Dev 1\); vì dài",
     lambda m: f"Biểu đồ vẽ các bước 1–{int(m[2]) - 1} (thách đấu) và {int(m[3]) + 1}–{m[1]} (kết thúc ván) của kịch "
               f"bản phiên bản 2 trong một biểu đồ; phần chơi ván (bước {m[2]}–{m[3]}) thuộc phần của Dev 1 nên không "
               f"vẽ, số bước giữ như kịch bản. Vì dài"),
    (r"Biểu đồ tuần tự vẽ đủ các bước 1–(\d+) ở trên trong một biểu đồ, kể cả vòng lặp chơi ván "
     r"\(bước (\d+)–(\d+), thuộc phần của Dev 1\); vì dài",
     lambda m: f"Biểu đồ tuần tự vẽ các bước 1–{int(m[2]) - 1} (thách đấu) và {int(m[3]) + 1}–{m[1]} (kết thúc ván) ở "
               f"trên trong một biểu đồ; vòng lặp chơi ván (bước {m[2]}–{m[3]}) thuộc phần của Dev 1 nên không vẽ, số "
               f"bước giữ như kịch bản. Vì dài"),
]


def adjust(md):
    md = re.sub(r"^(##+ .*?) (?:cho )?bốn modul", r"\1 cho các modul", md, flags=re.M)
    for src in DEV1_FIGURES:
        md = re.sub(rf"!\[[^\]]*\]\({re.escape(src)}\)\n\n\*Hình [^\n]*\*\n\n", "", md)
    for old, new in DEV2_FIGURES.items():
        md = md.replace(f"]({old})", f"]({new})")
    for pattern, new in DEV2_TEXT:
        md, n = re.subn(pattern, new, md)
        if n != 1:
            raise SystemExit(f"không khớp: {pattern}")
    # ngoại lệ 10a, 10b của kịch bản v1 M4 xảy ra trong lúc chơi ván (phần của Dev 1)
    md, n = re.subn(r"<br>10[ab]\. [^<|]*", "", md)
    if n != 2:
        raise SystemExit(f"bỏ ngoại lệ 10a, 10b: {n} chỗ, cần 2")
    for old, new in [
        (r"giao diện chơi \(Hình 3\.\d+\)", "giao diện chơi (thuộc phần của Dev 1)"),
        (r"^- `MatchScreen` → `MatchActivity`: .*$", "- `MatchScreen` → `MatchActivity`: thuộc phần của Dev 1."),
    ]:
        md, n = re.subn(old, new, md, flags=re.M)
        if n != 1:
            raise SystemExit(f"không khớp: {old}")
    return fold_play_steps(md)


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
    # dòng tiêu đề trong <thead> bị Google Docs ghim lại: chuyển thành dòng thường, chữ đậm
    html = re.sub(r"</?(?:thead|tbody)>\n?", "", html)
    html = re.sub(r"<th( [^>]*)?>(.*?)</th>", r"<td\1><b>\2</b></td>", html, flags=re.S)
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
    body = markdown.markdown(indent_lists(adjust("\n\n".join(parts))), extensions=["tables", "sane_lists"])
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
