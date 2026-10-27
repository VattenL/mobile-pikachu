"""Vẽ lại đường nối của biểu đồ VP sao cho mỗi đường tối đa 3 đoạn thẳng vuông góc.

    python tidy.py <khoá> [<khoá>...]      # khoá như trong build_vp.DIAGRAMS

Với mỗi đường (trừ đường tự nối một shape), thử các dạng: thẳng (1 đoạn), chữ L (2 đoạn), chữ Z và chữ U (3 đoạn),
với đầu mút ở giữa cạnh hoặc lệch một phần tư cạnh; bỏ dạng nào cắt qua shape khác, chọn dạng ngắn nhất, ít gấp
nhất và ít chồng lên các đường đã vẽ. Nhãn đặt cạnh đoạn dài nhất. Đường không tìm được dạng nào thì để nguyên và báo.
Với biểu đồ có bản cũ đã chỉnh tay (TIDY_REF), vị trí các shape trùng tên được chép từ bản cũ trước.
Cần Visual Paradigm đang mở pikachu.vpp và lệnh setConnectorRoutes của plugin (bản vá cục bộ 2026-10-27).
"""
import re
import sys

import build_vp as b

# khoá -> tên biểu đồ bản cũ (đã chỉnh tay) trong pikachu.vpp
TIDY_REF = {
    "class-analysis-m4": "2.5 Lớp phân tích M4",
    "state-m4": "2.9 Trạng thái M4",
    "class-design-entity": "3.1 Lớp thực thể pha thiết kế",
    "class-design-m2": "3.7 Lớp thiết kế M2",
    "class-design-m4": "3.16 Lớp thiết kế M4",
}
# shape mới (không có trong bản cũ): (shape tham chiếu, dx, dy) so với vị trí shape tham chiếu ở bản cũ
NEW_POS = {
    "state-m4": {"Hiển thị ChallengeConfirmDialog (chờ trả lời)": ("Hiển thị InvitationDialog", 0, -200)},
    "class-design-m2": {"BaseActivity": ("RankingActivity", 420, 0)},
}

GAP = 14      # khoảng cách tối thiểu giữa đường và shape khác
STUB = 24     # đoạn ra khỏi shape tối thiểu trước khi rẽ (dạng U)
SHAPE = re.compile(r"^(\s*)[^:]+: (.+?) (-?\d+),(-?\d+) (\d+)x(\d+)(?: caption .*)?$")
POINT = re.compile(r"\((-?\d+),(-?\d+)\)")
NEEDS = re.compile(r"TRUNCATED needs (\d+)x(\d+)")
CAP = re.compile(r" caption (-?\d+),(-?\d+) (\d+)x(\d+)")


def geometry(name):
    """Shape lá (không chứa shape khác) {tên: (x, y, w, h)}, shape chứa, và danh sách đường theo đúng thứ tự
    của getDiagramGeometry: (điểm, (rộng, cao) nhãn)."""
    lines = b.vp("getDiagramGeometry", diagramName=name).splitlines()[1:]
    shapes, depth, conns = [], [], []
    for line in lines:
        if " points" in line and " -> " in line:
            pts = [tuple(map(int, p)) for p in POINT.findall(line.split(" points", 1)[1].split(" caption")[0])]
            cap = box = None
            if (m := CAP.search(line)) and int(m[3]) > 0:
                cap = (int(m[3]), int(m[4]))
                box = tuple(int(v) for v in m.groups())
                if n := NEEDS.search(line):
                    cap = (max(cap[0], int(n[1])), max(cap[1], int(n[2])))
            conns.append((pts, cap, box))
        elif m := SHAPE.match(line):
            shapes.append((m[2], tuple(int(v) for v in m.groups()[2:])))
            depth.append(len(m[1]))
    leaves, containers = {}, {}
    for i, (s, box) in enumerate(shapes):
        has_child = i + 1 < len(shapes) and depth[i + 1] > depth[i]
        (containers if has_child else leaves)[s] = box
    return leaves, containers, conns


def owner(boxes, p):
    """Shape lá có cạnh chứa điểm p (đầu mút đường), gần nhất."""
    best, dist = None, 1e9
    for s, (x, y, w, h) in boxes.items():
        dx = max(x - p[0], 0, p[0] - (x + w))
        dy = max(y - p[1], 0, p[1] - (y + h))
        if dx + dy < dist:
            best, dist = s, dx + dy
    return best if dist <= 3 else None


def seg_hits(a, c, box, gap=GAP):
    """Đoạn thẳng đứng hoặc ngang a-c có đi vào box (nới gap) không."""
    x, y, w, h = box
    x0, x1 = sorted((a[0], c[0]))
    y0, y1 = sorted((a[1], c[1]))
    return x0 < x + w + gap and x1 > x - gap and y0 < y + h + gap and y1 > y - gap


def anchors(box, side):
    """Các điểm có thể làm đầu mút trên một cạnh: giữa cạnh và lệch một phần tư."""
    x, y, w, h = box
    if side in "NS":
        yy = y if side == "N" else y + h
        return [(x + w // 2, yy), (x + w // 4, yy), (x + 3 * w // 4, yy)]
    xx = x if side == "W" else x + w
    return [(xx, y + h // 2), (xx, y + h // 4), (xx, y + 3 * h // 4)]


def out(p, side, d):
    return {"N": (p[0], p[1] - d), "S": (p[0], p[1] + d), "W": (p[0] - d, p[1]), "E": (p[0] + d, p[1])}[side]


def candidates(A, B):
    """Các route ≤ 3 đoạn từ shape A tới shape B."""
    ax, ay, aw, ah = A
    bx, by, bw, bh = B
    res = []
    # thẳng: hai shape chồng nhau theo một trục
    lo, hi = max(ay, by) + 8, min(ay + ah, by + bh) - 8
    if lo <= hi:
        yy = (lo + hi) // 2
        if bx >= ax + aw:
            res.append([(ax + aw, yy), (bx, yy)])
        elif ax >= bx + bw:
            res.append([(ax, yy), (bx + bw, yy)])
    lo, hi = max(ax, bx) + 8, min(ax + aw, bx + bw) - 8
    if lo <= hi:
        xx = (lo + hi) // 2
        if by >= ay + ah:
            res.append([(xx, ay + ah), (xx, by)])
        elif ay >= by + bh:
            res.append([(xx, ay), (xx, by + bh)])
    for sa in "NSWE":
        for pa in anchors(A, sa):
            for sb in "NSWE":
                for pb in anchors(B, sb):
                    horiz_a, horiz_b = sa in "WE", sb in "WE"
                    if horiz_a != horiz_b:  # chữ L: rẽ một lần
                        corner = (pb[0], pa[1]) if horiz_a else (pa[0], pb[1])
                        res.append([pa, corner, pb])
                    else:  # chữ Z (hai cạnh đối diện) hoặc chữ U (cùng phía)
                        for mid in mids(A, B, pa, pb, sa, sb):
                            if horiz_a:
                                res.append([pa, (mid, pa[1]), (mid, pb[1]), pb])
                            else:
                                res.append([pa, (pa[0], mid), (pb[0], mid), pb])
    return [r for r in res if valid(r, A, B)]


def mids(A, B, pa, pb, sa, sb):
    """Toạ độ của đoạn giữa (x nếu hai đầu ra ngang, y nếu ra dọc)."""
    i = 0 if sa in "WE" else 1
    lo_a, hi_a = (A[0], A[0] + A[2]) if i == 0 else (A[1], A[1] + A[3])
    lo_b, hi_b = (B[0], B[0] + B[2]) if i == 0 else (B[1], B[1] + B[3])
    out_ = []
    if sa != sb:  # Z: đoạn giữa nằm trong khe giữa hai shape (giữa khe hoặc lệch về hai phía)
        for lo, hi, ok in ((hi_a, lo_b, sa in "ES"), (hi_b, lo_a, sa in "WN")):
            if ok and lo + 2 * GAP <= hi:
                out_ += sorted({(lo + hi) // 2, lo + (hi - lo) // 4, lo + 3 * (hi - lo) // 4},
                               key=lambda m: abs(m - (lo + hi) // 2))
    else:  # U: vòng ra ngoài cả hai shape, càng xa càng dễ né shape khác
        for k in range(10):
            d = STUB + GAP + 40 * k
            out_.append(max(hi_a, hi_b) + d if sa in "ES" else min(lo_a, lo_b) - d)
    return out_


def valid(route, A, B):
    """Đoạn đầu phải đi ra khỏi A, đoạn cuối đi vào B (không đâm xuyên chính hai shape đó)."""
    for (p, q) in zip(route, route[1:]):
        if p[0] != q[0] and p[1] != q[1]:
            return False
    # đoạn đầu phải đi ra ngoài A từ cạnh chứa đầu mút, đoạn cuối đi vào B từ phía ngoài cạnh của nó
    for box, p, q in ((A, route[0], route[1]), (B, route[-1], route[-2])):
        x, y, w, h = box
        if (p[0] == x and q[0] >= x) or (p[0] == x + w and q[0] <= x + w) or                 (p[1] == y and q[1] >= y) or (p[1] == y + h and q[1] <= y + h):
            return False
    inner = route[1:-1]
    for pt in inner:
        for (x, y, w, h) in (A, B):
            if x < pt[0] < x + w and y < pt[1] < y + h:
                return False
    if len(route) >= 3:
        # đoạn giữa không được cắt qua A hoặc B
        for p, q in zip(route[1:-1], route[2:-1]):
            if seg_hits(p, q, A, 2) or seg_hits(p, q, B, 2):
                return False
    return True


def cost(route, obstacles, used, ends=()):
    if any(seg_hits(p, q, box) for p, q in zip(route, route[1:]) for box in obstacles):
        return None
    length = sum(abs(p[0] - q[0]) + abs(p[1] - q[1]) for p, q in zip(route, route[1:]))
    c = length + 120 * (len(route) - 2)
    for e in (route[0], route[-1]):  # đầu mút trùng chỗ đầu mút đường khác: nhãn bội số đè nhau
        if any(abs(e[0] - f[0]) + abs(e[1] - f[1]) < 16 for f in ends):
            c += 180
    # đoạn đầu (cuối) đi ra xa đầu kia: đường vòng
    for p, q, far in ((route[0], route[1], route[-1]), (route[-1], route[-2], route[0])):
        if (q[0] - p[0]) * (far[0] - p[0]) < 0 or (q[1] - p[1]) * (far[1] - p[1]) < 0:
            c += 250
    for p, q in zip(route, route[1:]):  # chồng lên đoạn đã vẽ (cùng phương, gần nhau)
        for u, v in used:
            if p[0] == q[0] == u[0] == v[0] or abs(p[0] - u[0]) < 6 and p[0] == q[0] and u[0] == v[0]:
                if min(max(p[1], q[1]), max(u[1], v[1])) > max(min(p[1], q[1]), min(u[1], v[1])):
                    c += 400
            if p[1] == q[1] and u[1] == v[1] and abs(p[1] - u[1]) < 6:
                if min(max(p[0], q[0]), max(u[0], v[0])) > max(min(p[0], q[0]), min(u[0], v[0])):
                    c += 400
    return c


def overlap(r1, r2):
    (x, y, w, h), (u, v, p, q) = r1, r2
    return max(0, min(x + w, u + p) - max(x, u)) * max(0, min(y + h, v + q) - max(y, v))


def caption_at(route, size, shapes, labels, segments):
    """Góc trên trái của nhãn: thử hai bên của từng đoạn (đoạn dài trước), chọn chỗ ít đè lên shape, nhãn đã
    đặt và các đường nhất."""
    w, h = size
    best = None
    segs = sorted(zip(route, route[1:]), key=lambda s: -(abs(s[0][0] - s[1][0]) + abs(s[0][1] - s[1][1])))
    for rank, (p, q) in enumerate(segs):
        spots = []
        if p[1] == q[1]:
            lo, hi = sorted((p[0], q[0]))
            for x in range(lo - w // 2, hi - w // 2 + 1, 12):
                for k in range(3):
                    spots.append((x, p[1] - h - 2 - k * (h + 4), k, abs(x + w // 2 - (lo + hi) // 2)))
                    spots.append((x, p[1] + 3 + k * (h + 4), k, abs(x + w // 2 - (lo + hi) // 2)))
        else:
            lo, hi = sorted((p[1], q[1]))
            for y in range(lo - h // 2, hi - h // 2 + 1, 8):
                for k in range(3):
                    spots.append((p[0] + 4 + k * 20, y, k, abs(y + h // 2 - (lo + hi) // 2)))
                    spots.append((p[0] - w - 4 - k * 20, y, k, abs(y + h // 2 - (lo + hi) // 2)))
        for x, y, far, off in spots:
            box = (x, y, w, h)
            k = 10 * sum(overlap(box, s) for s in shapes) + 10 * sum(overlap(box, l) for l in labels)
            for u, v in segments:
                seg = (min(u[0], v[0]) - 2, min(u[1], v[1]) - 2, abs(u[0] - v[0]) + 4, abs(u[1] - v[1]) + 4)
                k += 4 * overlap(box, seg)
            k += 30 * rank + 40 * far + off // 2
            if best is None or k < best[0]:
                best = (k, (x, y))
    return best[1]


def spread(old, size, gap=60):
    """Giữ thứ tự và vị trí tương đối của bản cũ nhưng giãn ra cho đủ chỗ với kích thước mới: lần lượt theo x rồi
    theo y, shape nào chồng lên shape đứng trước nó (cùng hàng, cùng cột) thì bị đẩy sang phải, xuống dưới."""
    pos = {s: list(xy) for s, xy in old.items()}
    for axis in (0, 1):
        other = 1 - axis
        for s in sorted(pos, key=lambda s: old[s][axis]):
            for t in pos:
                if t == s or old[t][axis] > old[s][axis] or (old[t][axis] == old[s][axis] and t >= s):
                    continue
                # t đứng trước s theo trục này; nếu hai shape chồng nhau theo trục kia thì đẩy s
                lo, hi = pos[t][other], pos[t][other] + size[t][other]
                if lo < pos[s][other] + size[s][other] and pos[s][other] < hi:
                    pos[s][axis] = max(pos[s][axis], pos[t][axis] + size[t][axis] + gap)
    return {s: tuple(xy) for s, xy in pos.items()}


def copy_layout(key, name):
    ref, _, _ = geometry(TIDY_REF[key])
    cur, _, _ = geometry(name)
    old = {s: ref[s][:2] for s in cur if s in ref}
    for s, (anchor, dx, dy) in NEW_POS.get(key, {}).items():
        old[s] = (ref[anchor][0] + dx, ref[anchor][1] + dy)
    pos = spread(old, {s: cur[s][2:] for s in old})
    for i, s in enumerate(cur):  # dời ra xa trước: plugin giữ khoảng cách 48 px giữa các shape
        b.vp("setElementBounds", diagramName=name, elementName=s, x=3000 + (i % 5) * 500, y=3000 + (i // 5) * 400,
             width=0, height=0)
    b.place(name, pos)


def tidy(key, copy=True):
    name = {k: n for k, n, _ in b.DIAGRAMS}[key]
    if copy and key in TIDY_REF:
        copy_layout(key, name)
    route_lines(name, key)
    b.export(name, key)


def route_lines(name, key=None):
    """Vẽ lại các đường của biểu đồ name (đã dựng xong, shape đã ở chỗ) thành dạng ≤ 3 đoạn, đặt lại nhãn."""
    leaves, _, conns = geometry(name)
    lines, used, failed, routes, ends = [], [], [], {}, []
    # đường dài trước: chúng khó tìm chỗ hơn
    order = sorted(range(len(conns)), key=lambda i: -abs(conns[i][0][0][0] - conns[i][0][-1][0])
                   - abs(conns[i][0][0][1] - conns[i][0][-1][1]))
    for i in order:
        pts, cap = conns[i][:2]
        a, c = owner(leaves, pts[0]), owner(leaves, pts[-1])
        if not a or not c or a == c:
            continue
        if len(pts) <= 4 and all(p[0] == q[0] or p[1] == q[1] for p, q in zip(pts, pts[1:])):
            if not any(seg_hits(p, q, box) for p, q in zip(pts, pts[1:]) for s, box in leaves.items()
                       if s not in (a, c)) and not any(abs(e[0] - f[0]) + abs(e[1] - f[1]) < 16
                                                       for e in (pts[0], pts[-1]) for f in ends):
                used += list(zip(pts, pts[1:]))
                routes[i] = pts  # đã đủ đẹp, giữ đường, chỉ đặt lại nhãn
                ends += [pts[0], pts[-1]]
                continue
        obstacles = [box for s, box in leaves.items() if s not in (a, c)]
        best = None
        for r in candidates(leaves[a], leaves[c]):
            k = cost(r, obstacles, used, ends)
            if k is not None and (best is None or k < best[0]):
                best = (k, r)
        if not best:
            # không có dạng nào né hết shape: lấy dạng ≤ 3 đoạn cắt qua ít shape nhất
            for r in candidates(leaves[a], leaves[c]):
                hits = sum(1 for box in obstacles if any(seg_hits(p, q, box, 2) for p, q in zip(r, r[1:])))
                k = hits * 5000 + cost(r, [], used, ends)
                if best is None or k < best[0]:
                    best = (k, r)
            if not best:
                failed.append(f"{a} -> {c}")
                continue
            failed.append(f"{a} -> {c} (cắt qua {best[0] // 5000} shape)")
        used += list(zip(best[1], best[1][1:]))
        routes[i] = best[1]
        ends += [best[1][0], best[1][-1]]
    # nhãn của các đường không vẽ lại (tự nối một shape) giữ chỗ cũ, nhãn mới tránh chúng
    labels = [conns[i][2] for i in range(len(conns)) if i not in routes and conns[i][2]]
    for i in sorted(routes, key=lambda i: conns[i][1] is None):
        r, cap = routes[i], conns[i][1]
        line = f"{i}: " + " ".join(f"{x},{y}" for x, y in r)
        if cap:
            x, y = caption_at(r, cap, list(leaves.values()), labels, used)
            labels.append((x, y, cap[0], cap[1]))
            line += f" | {x},{y}"
        lines.append(line)
    if lines:
        print(b.vp("setConnectorRoutes", diagramName=name, routes="\n".join(lines)))
    print(key or name, "vẽ lại:", len(lines), "chưa né được hết:", failed)


if __name__ == "__main__":
    sys.stdout.reconfigure(encoding="utf-8")
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    for k in args:
        tidy(k, copy="--no-copy" not in sys.argv)
