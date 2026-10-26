"""Cắt một ảnh biểu đồ tuần tự dài thành vài ảnh để vừa trang, mỗi ảnh lặp lại hàng đầu lifeline.

Đường cắt rơi vào một khoảng trắng giữa hai message (bỏ qua các đường dọc: lifeline, thanh kích hoạt, cạnh khung
loop), gần các mốc chia đều chiều cao nhất. Biểu đồ vẫn là một, số bước liền mạch qua các ảnh.

    python slice_seq.py seq-analysis-m4-full 3     # png/seq-analysis-m4-full.png -> png/seq-analysis-m4-full-1..3.png
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image

PNG = Path(__file__).resolve().parent / "png"


def slice_png(key, parts):
    img = Image.open(PNG / f"{key}.png").convert("RGB")
    a = np.asarray(img)
    ink = a.min(axis=2) < 235  # mọi điểm không trắng
    h, w = ink.shape
    # cột của đường dọc (lifeline nét đứt, thanh kích hoạt, cạnh khung): không trắng ở nhiều hàng
    vertical = ink.mean(axis=0) > 0.12
    vertical = np.convolve(vertical, np.ones(7), "same") > 0
    busy = ink[:, ~vertical].sum(axis=1)
    # mũi tên message: hàng có một đoạn ngang dài (gạch chân biểu tượng entity ở hàng đầu chỉ ~80 px); hàng đầu
    # lifeline kết thúc ở hàng thưa nhất ngay trên nhãn của message đầu tiên
    arrow = np.array([longest_run(ink[y]) >= 150 for y in range(h)])
    first_arrow = int(np.argmax(arrow))
    lo = max(first_arrow - 24, 1)
    header = lo + int(np.argmin(busy[lo:first_arrow - 10]))
    blank = busy <= 8  # giữa hai message chỉ còn vài điểm lẻ của nét đứt
    cuts = [header]
    for k in range(1, parts):
        target = header + (h - header) * k // parts
        cuts.append(nearest_gap(blank, target, cuts[-1] + 40))
    cuts.append(h)
    head = img.crop((0, 0, w, header))
    for i in range(parts):
        body = img.crop((0, cuts[i], w, cuts[i + 1]))
        out = Image.new("RGB", (w, header + body.height), "white")
        out.paste(head, (0, 0))
        out.paste(body, (0, header))
        out.save(PNG / f"{key}-{i + 1}.png")
        print(f"{key}-{i + 1}.png  {w}x{out.height}  (hàng {cuts[i]}-{cuts[i + 1]})")


def longest_run(row):
    best = cur = 0
    for v in row:
        cur = cur + 1 if v else 0
        best = max(best, cur)
    return best


def nearest_gap(blank, target, lo):
    """Hàng giữa của một khoảng trắng dài ít nhất 8 hàng, gần target nhất (và sau lo)."""
    gaps, y, h = [], lo, len(blank)
    while y < h:
        if blank[y]:
            s = y
            while y < h and blank[y]:
                y += 1
            if y - s >= 8:
                gaps.append((s + y) // 2)
        y += 1
    if not gaps:
        raise SystemExit("không tìm được khoảng trắng để cắt")
    return min(gaps, key=lambda g: abs(g - target))


if __name__ == "__main__":
    slice_png(sys.argv[1], int(sys.argv[2]) if len(sys.argv) > 2 else 3)
