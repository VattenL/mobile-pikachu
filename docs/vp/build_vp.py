"""Sinh project Visual Paradigm docs/vp/pikachu.vpp và ảnh docs/vp/png/*.png.

Nguồn nội dung, không chép tay:
- use case, lớp, trạng thái: đọc từ các file .puml trong docs/uml/pha<N>/;
- biểu đồ tuần tự pha thiết kế: danh sách bước trong docs/uml/pha3/gen_seq.py;
- biểu đồ tuần tự pha phân tích (kịch bản v2), hoạt động, gói, triển khai, CSDL: khai báo ở cuối file này.

Cần Visual Paradigm đang mở với plugin MCP (http://127.0.0.1:2026/mcp).

    python build_vp.py               # dựng lại toàn bộ project, lưu đè pikachu.vpp, xuất mọi ảnh
    python build_vp.py --only erd,seq-m2   # dựng thử vài biểu đồ vào project trống, chỉ xuất ảnh
    python build_vp.py --export class-entity,state-m4   # chỉ xuất lại ảnh từ project đang mở (sau khi chỉnh tay)

Lần dựng lại toàn bộ sẽ xoá pikachu.vpp cũ. Các biểu đồ trong HAND_EDITED được chỉnh bố cục bằng tay trong VP
sau lần dựng tự động (quyết định của người dùng, 2026-10-26); vì vậy dựng lại toàn bộ bị từ chối khi pikachu.vpp đã
được lưu sau lần dựng tự động gần nhất (so với build-stamp.txt), trừ khi chạy với --force (mất phần chỉnh tay).
"""
import argparse
import importlib.util
import json
import math
import re
import sys
import time
import urllib.request
from pathlib import Path

HERE = Path(__file__).resolve().parent
UML = HERE.parent / "uml"
PNG = HERE / "png"
VPP = HERE / "pikachu.vpp"
URL = "http://127.0.0.1:2026/mcp"

_spec = importlib.util.spec_from_file_location("gen_seq", UML / "pha3" / "gen_seq.py")
gen_seq = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(gen_seq)

# ---------------------------------------------------------------- VP client

_rpc_id = 0


def vp(tool, **args):
    global _rpc_id
    _rpc_id += 1
    body = {"jsonrpc": "2.0", "id": _rpc_id, "method": "tools/call", "params": {"name": tool, "arguments": args}}
    req = urllib.request.Request(URL, data=json.dumps(body).encode(), headers={
        "Content-Type": "application/json", "Accept": "application/json, text/event-stream"})
    res = json.load(urllib.request.urlopen(req, timeout=600))
    if "error" in res:
        raise RuntimeError(f"{tool}: {res['error']}")
    text = "".join(c.get("text", "") for c in res["result"].get("content", []))
    if res["result"].get("isError") or text.startswith("Error"):
        raise RuntimeError(f"{tool}({args}): {text}")
    return text


RUN = ""  # hậu tố tên khi dựng thử (--only)
STAMP = HERE / "build-stamp.txt"  # mtime của pikachu.vpp ngay sau lần dựng tự động toàn bộ gần nhất (không commit)
# biểu đồ người dùng chỉnh bố cục bằng tay trong VP sau khi dựng; nội dung vẫn lấy từ nguồn ở đây
HAND_EDITED = ["class-entity", "class-analysis-m2", "class-analysis-m4", "state-m4", "class-design-entity",
               "class-design-m2", "class-design-m4"]

VIS = {"-": "private", "+": "public", "#": "protected", "~": "package", "": "private"}


def label(s):
    """Nhãn PlantUML -> nhãn VP: bỏ xuống dòng cứng."""
    return s.replace("\\n", " ").replace("<U+002A>", "*").strip()


# ---------------------------------------------------------------- use case (.puml)

def build_usecase(name, src, straight=None):
    """Đường thẳng (xiên) thay cho vuông góc, như bản Dev 1 chỉnh Hình 1.1 (tool setConnectorStyle là bản vá
    cục bộ của plugin). straight="fan": xếp lại kiểu Hình 1.1 (chỉ có actor - use case); straight="lines": giữ
    vị trí của layout, đổi mọi đường (actor - use case, include, extend) thành đường thẳng; plugin dời nhãn
    stereotype về cạnh đoạn giữa của đường mới."""
    text = (UML / src).read_text(encoding="utf-8")
    ids, system = {}, None
    vp("createUseCaseDiagram", diagramName=name)
    for line in text.splitlines():
        line = line.strip()
        if m := re.match(r'actor "([^"]+)" as (\w+)', line):
            ids[m[2]] = m[1]
            vp("addActor", diagramName=name, actorName=m[1], x=0, y=0)
        elif m := re.match(r'usecase "([^"]+)" as (\w+)', line):
            ids[m[2]] = m[1]
            vp("addUseCase", diagramName=name, useCaseName=m[1], x=0, y=0)
        elif m := re.match(r'rectangle "([^"]+)"', line):
            system = m[1]
    for line in text.splitlines():
        line = line.strip()
        if m := re.match(r"(\w+) -+\|> (\w+)$", line):
            vp("addRelationship", diagramName=name, sourceName=ids[m[1]], targetName=ids[m[2]], relationshipType="Generalization")
        elif m := re.match(r"(\w+) -+> (\w+)$", line):
            vp("addRelationship", diagramName=name, sourceName=ids[m[1]], targetName=ids[m[2]], relationshipType="Association")
        elif m := re.match(r"(\w+) \.+> (\w+) : <<include>>", line):
            vp("addRelationship", diagramName=name, sourceName=ids[m[1]], targetName=ids[m[2]], relationshipType="Include")
        elif m := re.match(r"(\w+) <\.+ (\w+) : <<extend>>", line):
            vp("addRelationship", diagramName=name, sourceName=ids[m[2]], targetName=ids[m[1]], relationshipType="Extend")
    if system:
        vp("addSystemBoundary", diagramName=name, systemName=system)
    if straight == "lines":
        vp("setConnectorStyle", diagramName=name, style="oblique", relationshipType="", pairs="")
    elif straight == "fan":
        # như bản Dev 1: hộp system rộng, use case xếp theo vòng cung, actor đứng giữa theo chiều dọc
        # để các đường toả ra hai phía. Bước dọc 88 = cao 40 + khoảng cách tối thiểu 48 của plugin
        # giữa hai shape ghim tay; ghim thẳng vào chỗ mới sẽ đụng shape chưa dời, nên dời tạm xuống dưới trước.
        ucs = list(shape_bounds(name, "UseCase").items())
        bx, by, _, _ = shape_bounds(name, "System")[system]
        k, step = len(ucs), 88
        for i, (uc, _) in enumerate(ucs):
            vp("setElementBounds", diagramName=name, elementName=uc, x=0, y=by + 60 + (k + i) * 100, width=0, height=0)
        for i, (uc, (_, _, w, _)) in enumerate(ucs):
            cx = bx + 160 + int(130 * math.sin(math.pi * i / (k - 1)))
            vp("setElementBounds", diagramName=name, elementName=uc, x=cx - w // 2, y=by + 40 + i * step,
               width=0, height=0)
        box_h = 40 + (k - 1) * step + 40 + 40
        vp("setElementBounds", diagramName=name, elementName=system, x=0, y=0, width=460, height=box_h)
        # nhiều actor (Guest, Player): chia đều theo chiều dọc, theo thứ tự khai báo, mỗi actor ngang nhóm use case của nó
        actors = shape_bounds(name, "Actor")
        order = [a for a in ids.values() if a in actors]
        for i, actor in enumerate(order):
            h = actors[actor][3]
            vp("setElementBounds", diagramName=name, elementName=actor, x=max(bx - 150, 20),
               y=by + box_h * (i + 1) // (len(order) + 1) - h // 2, width=0, height=0)
        vp("setConnectorStyle", diagramName=name, style="oblique", relationshipType="", pairs="")


# ---------------------------------------------------------------- class (.puml)

CLASS_HEAD = re.compile(r"^(abstract class|class|interface) (\w+)\s*(?:#\w+)?\s*\{")
REL = re.compile(r'^(\w+)\s+(?:"([^"]*)"\s+)?([-.<|*o]+(?:\[[^\]]*\]|left|right|up|down)?[-.<|*o>]*)\s+(?:"([^"]*)"\s+)?(\w+)(?:\s*:\s*(.+))?$')


def parse_member(line):
    vis = line[0] if line[0] in "-+#~" else ""
    body = line[1:].strip() if vis else line
    if m := re.match(r"(\w+)\((.*)\)\s*(?::\s*(.+))?$", body):
        params = ", ".join(re.sub(r"\s*:\s*", ": ", p.strip()) for p in m[2].split(",") if p.strip())
        return ("op", VIS[vis], m[1], params, (m[3] or "").strip())
    if m := re.match(r"(\w+)\s*(?::\s*(.+))?$", body):
        return ("attr", VIS[vis], m[1], (m[2] or "").strip())
    raise ValueError(line)


def shape_bounds(name, kind):
    """{tên: (x, y, w, h)} của các shape loại kind ("Class", "State"...) trên biểu đồ."""
    out = {}
    for line in vp("getDiagramGeometry", diagramName=name).splitlines():
        if m := re.match(rf"\s*{kind}: (.+?) (-?\d+),(-?\d+) (\d+)x(\d+)", line):
            out[m[1]] = tuple(int(v) for v in m.groups()[1:])
    return out


def layout_tiers(name, tiers, max_width=1500, gap_x=60, gap_y=90):
    """Xếp mỗi tầng (lớp biên, mạng, điều khiển, DAO, thực thể) thành một hàng, quá rộng thì xuống dòng."""
    for c in shape_bounds(name, "Class"):  # co giãn theo nội dung trước khi đo
        vp("setElementBounds", diagramName=name, elementName=c, x=0, y=0, width=0, height=0)
    size = shape_bounds(name, "Class")
    y, pos = 40, {}
    for tier in tiers:
        rows, row, width = [], [], 0
        for c in tier:
            w = size[c][2]
            if row and width + w > max_width:
                rows.append(row)
                row, width = [], 0
            row.append(c)
            width += w + gap_x
        rows.append(row)
        for row in rows:
            x = 40
            for c in row:
                pos[c] = (x, y)
                x += size[c][2] + gap_x
            y += max(size[c][3] for c in row) + gap_y
    place(name, pos)


def line_blocked(boxes, a, b, margin=6):
    """Đoạn thẳng nối tâm hai shape a, b có cắt qua shape nào khác không (Liang-Barsky với từng hình chữ nhật)."""
    (ax, ay, aw, ah), (bx, by, bw, bh) = boxes[a], boxes[b]
    x0, y0, x1, y1 = ax + aw / 2, ay + ah / 2, bx + bw / 2, by + bh / 2
    for name, (x, y, w, h) in boxes.items():
        if name in (a, b):
            continue
        lo, hi = 0.0, 1.0
        for p, q in ((-(x1 - x0), x0 - (x - margin)), (x1 - x0, x + w + margin - x0),
                     (-(y1 - y0), y0 - (y - margin)), (y1 - y0, y + h + margin - y0)):
            if p == 0:
                if q < 0:
                    break
            elif p < 0:
                lo = max(lo, q / p)
            else:
                hi = min(hi, q / p)
        else:
            if lo <= hi:
                return True
    return False


def place(name, pos):
    """Ghim shape vào vị trí cho trước. Hai lượt: ở lượt đầu VP còn đẩy shape tránh các shape chưa ghim."""
    for _ in range(2):
        for shape, (x, y) in pos.items():
            vp("setElementBounds", diagramName=name, elementName=shape, x=x, y=y, width=0, height=0)
    vp("rerouteConnectors", diagramName=name)


def place_rows(name, rows, gap_x=60):
    """rows: [(y, x đầu hàng, [lớp...])]: ghim các lớp thành hàng, trái sang phải theo bề rộng thật.
    Dời tạm các lớp xuống dưới trước để chỗ mới không bị shape cũ chiếm (plugin giữ khoảng cách 48 px)."""
    size = shape_bounds(name, "Class")
    moved = [c for _, _, row in rows for c in row]
    for i, c in enumerate(moved):
        vp("setElementBounds", diagramName=name, elementName=c, x=40 + (i % 4) * 450, y=3000 + (i // 4) * 300,
           width=0, height=0)
    for y, x, row in rows:
        for c in row:
            vp("setElementBounds", diagramName=name, elementName=c, x=x, y=y, width=0, height=0)
            x += size[c][2] + gap_x
    vp("rerouteConnectors", diagramName=name)


def build_class(name, src, package, pos=None, oblique_up=False, rows=None):
    """pos: {tên lớp: (x, y)} để ghim vị trí thay cho layout theo tầng hoặc tự động.
    rows: xếp lại một số hàng sau layout theo tầng (xem place_rows).
    oblique_up: đường nối hai lớp ở hai hàng khác nhau vẽ thẳng (xiên) nếu đoạn thẳng đó không cắt qua lớp nào
    khác; đường trong cùng hàng và đường sẽ cắt qua lớp khác giữ vuông góc."""
    package += RUN
    text = (UML / src).read_text(encoding="utf-8")
    classes, rels, cur, tiers = [], [], None, []
    for raw in text.splitlines():
        line = raw.strip()
        if line.startswith("' ====="):
            tiers.append([])
        if not line or line.startswith("'"):
            continue
        if cur is not None:
            if line == "}":
                classes.append(cur)
                cur = None
            else:
                cur["members"].append(parse_member(line))
            continue
        if m := CLASS_HEAD.match(line):
            cur = {"kind": m[1], "name": m[2], "members": []}
            if tiers:
                tiers[-1].append(m[2])
        elif (m := REL.match(line)) and not line.startswith(("skinparam", "hide", "title")):
            rels.append(m.groups())
    vp("createClassDiagram", diagramName=name)
    for c in classes:
        vp("addClass", diagramName=name, className=c["name"], packageName="", packageColor="",
           stereotype="interface" if c["kind"] == "interface" else "", isAbstract=c["kind"] == "abstract class",
           extendsClass="", implementsInterfaces="", x=0, y=0, modelPackage=package)
        for mem in c["members"]:
            if mem[0] == "attr":
                vp("addAttribute", diagramName=name, className=c["name"], attributeName=mem[2],
                   attributeType=mem[3], visibility=mem[1])
            else:
                vp("addOperation", diagramName=name, className=c["name"], operationName=mem[2], returnType=mem[4],
                   params=mem[3], visibility=mem[1], isAbstract=False)
    for left, lmul, arrow, rmul, right, text_ in rels:
        core = re.sub(r"\[[^\]]*\]|left|right|up|down", "", arrow)
        if "hidden" in arrow:
            continue
        lmul, rmul, text_ = lmul or "", rmul or "", (text_ or "").strip()
        if core in ("-|>", "--|>"):
            vp("addGeneralization", diagramName=name, fromClass=left, toClass=right)
        elif core in ("<|-", "<|--"):
            vp("addGeneralization", diagramName=name, fromClass=right, toClass=left)
        elif core == "..|>":
            vp("addRealization", diagramName=name, fromClass=left, toClass=right)
        elif core in ("..>",):
            vp("addDependency", diagramName=name, fromClass=left, toClass=right)
        elif core in ("--o", "-o"):
            vp("addAggregation", diagramName=name, fromClass=right, toClass=left, fromMultiplicity=rmul, toMultiplicity=lmul)
        elif core in ("o--", "o-"):
            vp("addAggregation", diagramName=name, fromClass=left, toClass=right, fromMultiplicity=lmul, toMultiplicity=rmul)
        elif core in ("*--", "*-"):
            vp("addComposition", diagramName=name, fromClass=left, toClass=right, fromMultiplicity=lmul, toMultiplicity=rmul)
        elif core in ("--*", "-*"):
            vp("addComposition", diagramName=name, fromClass=right, toClass=left, fromMultiplicity=rmul, toMultiplicity=lmul)
        elif core in ("--", "-"):
            vp("addAssociation", diagramName=name, fromClass=left, toClass=right, fromMultiplicity=lmul,
               toMultiplicity=rmul, name=text_)
        else:
            raise ValueError(f"{src}: mũi tên lạ {arrow}")
    if pos:
        place(name, pos)
    elif tiers:
        layout_tiers(name, tiers)
        if rows:
            place_rows(name, rows)
    else:
        vp("autoLayoutDiagram", diagramName=name)
    vp("layoutConnectorLabels", diagramName=name)
    if oblique_up:
        boxes = shape_bounds(name, "Class")

        def other_row(a, b):  # hai lớp không cùng hàng (layout_tiers cho tầng quá rộng xuống dòng)
            (_, ay, _, ah), (_, by, _, bh) = boxes[a], boxes[b]
            return ay + ah < by or by + bh < ay

        pairs = ";".join(f"{left}|{right}" for left, _, arrow, _, right, _ in rels
                         if "hidden" not in arrow and other_row(left, right) and not line_blocked(boxes, left, right))
        vp("setConnectorStyle", diagramName=name, style="oblique", relationshipType="", pairs=pairs)


# ---------------------------------------------------------------- state (.puml)

def build_state(name, src, pos=None):
    """pos: {nhãn trạng thái: (x, y)} để ghim vị trí khi layout tự động bị rối."""
    text = (UML / src).read_text(encoding="utf-8")
    ids = {"[*]": "Bắt đầu"}
    vp("createStateDiagram", diagramName=name)
    vp("addInitialState", diagramName=name, stateName="Bắt đầu", x=0, y=0)
    for line in text.splitlines():
        if m := re.match(r'\s*state "([^"]+)" as (\w+)', line):
            ids[m[2]] = m[1]
            vp("addState", diagramName=name, stateName=m[1], entryAction="", doActivity="", exitAction="", x=0, y=0)
    for line in text.splitlines():
        if m := re.match(r"\s*(\S+) --> (\w+)(?: : (.+))?$", line):
            vp("addTransition", diagramName=name, fromState=ids[m[1]], toState=ids[m[2]], label=label(m[3] or ""))
    if pos:
        place(name, pos)
    else:
        vp("autoLayoutDiagram", diagramName=name)


# ---------------------------------------------------------------- sequence

def build_sequence(name, parts, steps, start=1, xs=None):
    """parts: (kiểu, tên lifeline). steps theo định dạng của gen_seq.py; ("msg2", nguồn, [đích...], nhãn) vẽ
    một bước kịch bản thành nhiều mũi tên (23a, 23b) khi một lớp hiển thị cho cả hai người chơi; ("skip", k) bỏ
    qua k số bước (phần của dev khác không vẽ). xs: {lifeline: x} của bản mẫu (place_lifelines)."""
    vp("createSequenceDiagram", diagramName=name)
    parts = sorted(parts, key=lambda p: p[0] != "actor")  # mọi actor (người chơi A, B) đứng bên trái
    used = {x for st in steps if st[0] not in ("loop", "end", "deact", "skip")
            for x in [st[1], *(st[2] if isinstance(st[2], list) else [st[2]])] if x}
    for kind, lifeline in parts:
        if lifeline not in used:
            continue
        vp("addLifeline", diagramName=name, lifelineName=lifeline, className="",
           lifelineType=kind, alias="")
    n, loops = start, []
    for st in steps:
        if st[0] == "skip":
            n += st[1]
            continue
        if st[0] == "loop":
            loops.append([st[1], n])
            continue
        if st[0] == "end":
            guard, first = loops.pop()
            vp("addCombinedFragment", diagramName=name, operator="loop", guard=guard, coveredLifelines="",
               fromSeq=first, toSeq=n - 1, operandStarts="")
            continue
        if st[0] == "deact":
            continue
        kind, src, dst, text_ = st[:4]
        text_ = label(text_)
        if kind == "msg2":
            for i, d in enumerate(dst):
                vp("addMessage", diagramName=name, fromLifeline=src, toLifeline=d, messageName=text_,
                   sequenceNumber=f"{n}{'ab'[i]}", messageType="asynch")
        elif kind == "ret":
            vp("addReturnMessage", diagramName=name, fromLifeline=src, toLifeline=dst, messageName=text_,
               sequenceNumber=str(n))
        else:
            vp("addMessage", diagramName=name, fromLifeline=src, toLifeline=src if kind == "self" else dst,
               messageName=text_, sequenceNumber=str(n), messageType="asynch" if kind == "msg" else "synch")
        n += 1
    if xs:
        place_lifelines(name, xs, {lifeline: kind for kind, lifeline in parts if lifeline in used})


def seq_parts(parts):
    """PARTS của gen_seq.py -> (kiểu, tên hiển thị); tên hiển thị là nhãn bỏ xuống dòng, hoặc alias."""
    return [(kind, seq_name(alias, lab)) for kind, alias, lab in parts]


def seq_name(alias, lab):
    """Nhãn chỉ ngắt dòng tên lớp ("Server\nConnection") thì dùng tên lớp; nhãn khác hẳn ("Người chơi",
    "Player" của PlayerE) thì dùng nhãn."""
    if not lab or lab.replace("\\n", "") == alias:
        return alias
    return label(lab).replace(" ", "") if "\\n" in lab else lab


def seq_steps(parts, steps):
    names = {alias: seq_name(alias, lab) for _, alias, lab in parts}
    out = []
    for st in steps:
        if st[0] in ("loop", "end", "deact", "skip"):
            out.append(st)
        else:
            kind, src, dst, text_, _ = st
            out.append((kind, names[src], names.get(dst), text_))
    return out


# khung hẹp: biểu tượng và tên vẫn vẽ đủ (tên tràn ra hai bên khung); biểu tượng boundary cần khung rộng hơn,
# hẹp quá thì mất vạch đứng bên trái
LIFELINE_W = {"boundary": 80}
LIFELINE_W_DEFAULT = 60
LIFELINE_EXTRA_GAP = 10  # thêm vào mỗi bước giữa hai lifeline so với bản mẫu


def place_lifelines(name, ref, kinds):
    """Đặt các lifeline theo khoảng cách của bản mẫu ref {lifeline: x tâm}, lấy từ bản đầy đủ người dùng chỉnh tay
    (lifeline sát nhau hơn khe 60 px của layout tự động), nới thêm LIFELINE_EXTRA_GAP. Lifeline vắng mặt thì dồn
    lại: mỗi lifeline cách lifeline đứng trước nó đúng bằng bước từ lifeline đó tới lifeline kế tiếp trong bản mẫu.
    kinds: {lifeline có trên biểu đồ: kiểu}.
    setElementBounds đẩy khung đè lên khung khác ra xa, mà khung tự dựng rộng hơn tên (khung chỉnh tay chồng lên
    nhau), nên trước hết co mọi khung lại rồi mới đặt từ trái sang phải."""
    height = {m[1]: int(m[2]) for m in re.finditer(
        r"^\s*(?:Lifeline|InteractionActor): (.+?) -?\d+,-?\d+ \d+x(\d+)", vp("getDiagramGeometry", diagramName=name), re.M)}
    order = [lifeline for lifeline in sorted(ref, key=ref.get) if lifeline in kinds]
    width = {lifeline: LIFELINE_W.get(kinds[lifeline], LIFELINE_W_DEFAULT) for lifeline in order}
    for lifeline in order:
        vp("setElementBounds", diagramName=name, elementName=lifeline, x=0, y=0, width=width[lifeline],
           height=height[lifeline])
    full = sorted(ref, key=ref.get)
    step = {a: ref[b] - ref[a] + LIFELINE_EXTRA_GAP for a, b in zip(full, full[1:])}
    c = None
    for prev, lifeline in zip([None] + order, order):
        c = ref[lifeline] if prev is None else c + step[prev]
        vp("setElementBounds", diagramName=name, elementName=lifeline, x=c - width[lifeline] // 2, y=0,
           width=width[lifeline], height=height[lifeline])


# ---------------------------------------------------------------- activity

def lane_of(title):
    """"Xử lí tại server: MatchController.endMatch()" -> làn "server: MatchController";
    "Xử lí tại MainActivity (máy A)" -> làn "MainActivity"."""
    rest = title.removeprefix("Xử lí tại ")
    return re.sub(r"\.\w+\(\)|\s*\(.*\)$", "", rest).strip()


def build_activity(name, frames, flows):
    """frames: [(tiêu đề "Xử lí tại ...", [(kiểu, tên)...])]; kiểu: action | initial | final | decision | merge | dao.
    "dao" là nút "XxxDAO: method()" nối hai chiều với nút đứng trước nó. flows: (từ, tới, điều kiện).
    Mỗi lớp xử lí là một làn (swimlane); khung không tiêu đề (nút bắt đầu, kết thúc) theo làn kề nó."""
    lanes = [lane_of(t) if t else None for t, _ in frames]
    for i in range(len(lanes)):
        if lanes[i] is None:
            lanes[i] = lanes[i + 1] if i + 1 < len(lanes) and lanes[i + 1] else lanes[i - 1]
    vp("createActivityDiagram", diagramName=name)
    for lane in dict.fromkeys(lanes):
        vp("addActivityPartition", diagramName=name, partitionName=lane, x=0, y=0, width=0, height=0)
    for lane, (_, nodes) in zip(lanes, frames):
        for kind, node in nodes:
            if kind in ("action", "dao"):
                vp("addActivityAction", diagramName=name, actionName=node, partitionName=lane, x=0, y=0)
            else:
                vp("addActivityControlNode", diagramName=name, kind=kind, nodeName=node, partitionName=lane,
                   x=0, y=0)
    for a, b, guard in flows:
        vp("addActivityFlow", diagramName=name, fromNode=a, toNode=b, guard=guard, twoWay=False)
    prev = None
    for _, nodes in frames:
        for kind, node in nodes:
            if kind == "dao":
                vp("addActivityFlow", diagramName=name, fromNode=prev, toNode=node, guard="", twoWay=True)
            else:
                prev = node
    vp("autoLayoutDiagram", diagramName=name)


# ================================================================ nội dung

# ---- Pha 2: biểu đồ tuần tự pha phân tích, theo kịch bản v2 (pha2-phan-tich.md mục 5)

SEQA_M2_PARTS = [("actor", "Người chơi"), ("boundary", "MainScreen"), ("boundary", "RankingScreen"),
                 ("entity", "PlayerRanking")]
SEQA_M2 = [
    ("msg", "Người chơi", "MainScreen", "click xem bảng xếp hạng"),
    ("call", "MainScreen", "RankingScreen", "gọi"),
    ("call", "RankingScreen", "PlayerRanking", "gọi"),
    ("self", "PlayerRanking", None, "getRanking()"),
    ("ret", "PlayerRanking", "RankingScreen", "trả về"),
    ("msg", "RankingScreen", "Người chơi", "hiển thị"),
    ("msg", "Người chơi", "RankingScreen", "click vị trí của tôi"),
    ("call", "RankingScreen", "PlayerRanking", "gọi"),
    ("self", "PlayerRanking", None, "getPlayerPage()"),
    ("ret", "PlayerRanking", "RankingScreen", "trả về"),
    ("msg", "RankingScreen", "Người chơi", "hiển thị"),
    ("msg", "Người chơi", "RankingScreen", "click quay lại"),
    ("call", "RankingScreen", "MainScreen", "gọi"),
    ("msg", "MainScreen", "Người chơi", "hiển thị"),
]

SEQA_M4_PARTS = [("actor", "Người chơi A"), ("actor", "Người chơi B"), ("boundary", "MainScreen"),
                 ("boundary", "LobbyScreen"), ("boundary", "ChallengeConfirmDialog"), ("boundary", "InvitationDialog"),
                 ("boundary", "MatchScreen"), ("boundary", "ResultDialog"), ("entity", "Player"),
                 ("entity", "PlayerRanking"), ("entity", "Board"), ("entity", "Match")]
SEQA_M4_1 = [  # kịch bản v2 M4, bước 1-34: thách đấu và trả lời lời mời
    ("msg", "Người chơi A", "MainScreen", "click đấu đối kháng"),
    ("call", "MainScreen", "LobbyScreen", "gọi"),
    ("call", "LobbyScreen", "Player", "gọi"),
    ("self", "Player", None, "getOnlinePlayers()"),
    ("ret", "Player", "LobbyScreen", "trả về"),
    ("call", "LobbyScreen", "PlayerRanking", "gọi"),
    ("self", "PlayerRanking", None, "getTotalPoints()"),
    ("ret", "PlayerRanking", "LobbyScreen", "trả về"),
    ("msg", "LobbyScreen", "Người chơi A", "hiển thị"),
    ("msg", "Người chơi A", "LobbyScreen", "click tên đối thủ"),
    ("call", "LobbyScreen", "ChallengeConfirmDialog", "gọi"),
    ("msg", "ChallengeConfirmDialog", "Người chơi A", "hiển thị"),
    ("msg", "Người chơi A", "ChallengeConfirmDialog", "click xác nhận"),
    ("call", "ChallengeConfirmDialog", "Player", "gọi"),
    ("self", "Player", None, "sendInvitation()"),
    ("ret", "Player", "ChallengeConfirmDialog", "trả về"),
    ("msg", "ChallengeConfirmDialog", "Người chơi A", "hiển thị popup chờ"),
    ("call", "LobbyScreen", "InvitationDialog", "gọi"),
    ("msg", "InvitationDialog", "Người chơi B", "hiển thị"),
    ("msg", "Người chơi B", "InvitationDialog", "click accept"),
    ("call", "InvitationDialog", "Player", "gọi"),
    ("self", "Player", None, "replyInvitation()"),
    ("ret", "Player", "InvitationDialog", "trả về"),
    ("call", "InvitationDialog", "Board", "gọi"),
    ("self", "Board", None, "generateBoard()"),
    ("ret", "Board", "InvitationDialog", "trả về"),
    ("call", "InvitationDialog", "Match", "gọi"),
    ("self", "Match", None, "createMatch()"),
    ("ret", "Match", "InvitationDialog", "trả về"),
    ("call", "InvitationDialog", "Player", "gọi"),
    ("self", "Player", None, "updateStatus()"),
    ("ret", "Player", "InvitationDialog", "trả về"),
    ("call", "InvitationDialog", "MatchScreen", "gọi"),
    ("msg2", "MatchScreen", ["Người chơi A", "Người chơi B"], "hiển thị"),
]
SEQA_M4_PLAY = [  # kịch bản v2 M4, bước 35-45: chơi ván (phần của Dev 1), chỉ dùng trong bản đầy đủ
    ("loop", "lặp đến khi một người xóa hết ô"),
    ("msg", "Người chơi A", "MatchScreen", "click hai ô"),
    ("call", "MatchScreen", "Board", "gọi"),
    ("self", "Board", None, "checkPath()"),
    ("ret", "Board", "MatchScreen", "trả về"),
    ("call", "MatchScreen", "Board", "gọi"),
    ("self", "Board", None, "removePair()"),
    ("ret", "Board", "MatchScreen", "trả về"),
    ("call", "MatchScreen", "Board", "gọi"),
    ("self", "Board", None, "isCleared()"),
    ("ret", "Board", "MatchScreen", "trả về"),
    ("msg2", "MatchScreen", ["Người chơi A", "Người chơi B"], "hiển thị"),
    ("end",),
]
SEQA_M4_2 = [  # kịch bản v2 M4, bước 46-66: kết thúc ván, A chọn dừng
    ("call", "MatchScreen", "Match", "gọi"),
    ("self", "Match", None, "endMatch()"),
    ("self", "Match", None, "saveMatch()"),
    ("ret", "Match", "MatchScreen", "trả về"),
    ("call", "MatchScreen", "ResultDialog", "gọi"),
    ("msg2", "ResultDialog", ["Người chơi A", "Người chơi B"], "hiển thị"),
    ("msg", "Người chơi A", "ResultDialog", "click dừng"),
    ("call", "ResultDialog", "Match", "gọi"),
    ("self", "Match", None, "replyRematch()"),
    ("ret", "Match", "ResultDialog", "trả về"),
    ("call", "ResultDialog", "Player", "gọi"),
    ("self", "Player", None, "updateStatus()"),
    ("ret", "Player", "ResultDialog", "trả về"),
    ("call", "ResultDialog", "LobbyScreen", "gọi"),
    ("call", "LobbyScreen", "Player", "gọi"),
    ("self", "Player", None, "getOnlinePlayers()"),
    ("ret", "Player", "LobbyScreen", "trả về"),
    ("call", "LobbyScreen", "PlayerRanking", "gọi"),
    ("self", "PlayerRanking", None, "getTotalPoints()"),
    ("ret", "PlayerRanking", "LobbyScreen", "trả về"),
    ("msg2", "LobbyScreen", ["Người chơi A", "Người chơi B"], "hiển thị"),
]
SEQA_M4_3 = [  # nhánh 13a (cả hai chọn tái đấu), đánh số riêng từ 1
    ("msg", "Người chơi A", "ResultDialog", "click tái đấu"),
    ("call", "ResultDialog", "Match", "gọi"),
    ("self", "Match", None, "replyRematch()"),
    ("ret", "Match", "ResultDialog", "trả về"),
    ("msg", "ResultDialog", "Người chơi A", "hiển thị thông báo chờ"),
    ("msg", "Người chơi B", "ResultDialog", "click tái đấu"),
    ("call", "ResultDialog", "Match", "gọi"),
    ("self", "Match", None, "replyRematch()"),
    ("ret", "Match", "ResultDialog", "trả về"),
    ("call", "ResultDialog", "Board", "gọi"),
    ("self", "Board", None, "generateBoard()"),
    ("ret", "Board", "ResultDialog", "trả về"),
    ("call", "ResultDialog", "Match", "gọi"),
    ("self", "Match", None, "createMatch()"),
    ("ret", "Match", "ResultDialog", "trả về"),
    ("call", "ResultDialog", "MatchScreen", "gọi"),
    ("msg2", "MatchScreen", ["Người chơi A", "Người chơi B"], "hiển thị"),
]

# ---- Pha 2: vị trí các trạng thái M4 (layout tự động làm rối nhãn)

STATE_M4_POS = {
    "Bắt đầu": (60, 30),
    "Hiển thị MainScreen": (20, 140),
    "Hiển thị LoginScreen": (20, 320),
    "Hiển thị LobbyScreen": (420, 320),
    "Hiển thị ChallengeConfirmDialog": (420, 120),
    "Hiển thị ChallengeConfirmDialog (chờ trả lời)": (820, 120),
    "Hiển thị InvitationDialog": (820, 320),
    "Hiển thị MatchScreen": (420, 560),
    "Hiển thị ResultDialog": (420, 800),
}

# ---- Pha 3: biểu đồ tuần tự M4 đầy đủ (Hình 3.23-3.25): mọi lifeline của các phần, actor đứng trước

M4_FULL_PARTS = sorted(gen_seq.M4A_PARTS + [p for p in gen_seq.M4BC_PARTS if p[1] not in {q[1] for q in gen_seq.M4A_PARTS}],
                       key=lambda p: ["actor", "boundary", "control", "entity"].index(p[0]))

# x tâm của lifeline trong hai bản đầy đủ sau khi người dùng chỉnh tay trong VP (2026-10-26); dùng cho chính chúng khi
# dựng lại và cho bản của Dev 2 (bỏ vòng lặp chơi ván, dồn lifeline lại theo cùng khoảng cách)
SEQA_M4_X = {"Người chơi A": 94, "Người chơi B": 196, "MainScreen": 293, "LobbyScreen": 396,
             "ChallengeConfirmDialog": 512, "InvitationDialog": 641, "MatchScreen": 755, "ResultDialog": 872,
             "Player": 995, "PlayerRanking": 1107, "Board": 1220, "Match": 1320}
SEQ_M4_X = {"Người chơi A": 72, "Người chơi B": 158, "MainActivity": 263, "LobbyActivity": 371,
            "ChallengeConfirmDialog": 489, "InvitationDialog": 613, "MatchActivity": 718, "ResultDialog": 831,
            "ServerConnection": 955, "ClientHandler": 1063, "ChallengeController": 1188, "OnlinePlayerManager": 1351,
            "BoardGenerator": 1471, "MatchManager": 1582, "PlayerRankingDAO": 1696, "MatchController": 1824,
            "PathFinder": 1930, "MatchDAO": 2022, "Board": 2110, "Match": 2200, "PlayerRanking": 2300}

# bản của Dev 2: thách đấu rồi kết thúc ván, bỏ các bước chơi ván (Dev 1) nhưng giữ số bước của kịch bản
SEQA_M4_DEV2 = SEQA_M4_1 + [("skip", gen_seq.count(SEQA_M4_PLAY))] + SEQA_M4_2
SEQ_M4_DEV2 = gen_seq.M4A + [("skip", gen_seq.count(gen_seq.M4B))] + gen_seq.M4C

# ---- Pha 3: biểu đồ hoạt động

ACT_M2 = [
    ("", [("initial", "Bắt đầu")]),
    ("Xử lí tại MainActivity", [("action", "Hiển thị giao diện chính")]),
    ("Xử lí tại RankingActivity", [("action", "Gửi message GET_RANKING")]),
    ("Xử lí tại server: RankingController.handleGetRanking()", [
        ("action", "Lấy bảng xếp hạng"), ("dao", "PlayerRankingDAO: getRanking()"),
        ("decision", "Yêu cầu trang của người chơi?"),
        ("action", "Tìm dòng của người chơi, lấy trang chứa dòng đó"),
        ("action", "Lấy các dòng của trang được yêu cầu"), ("merge", "#1"),
        ("action", "Gửi message RANKING")]),
    ("Xử lí tại RankingActivity (hiển thị)", [
        ("decision", "Hỏi vị trí mà chưa có trong bảng?"), ("action", "Báo chưa có trong bảng xếp hạng"),
        ("action", "Hiển thị trang, tô dòng của người chơi"), ("merge", "#3"), ("decision", "Người chơi chọn gì?")]),
    ("Xử lí tại MainActivity (quay lại)", [("action", "Hiển thị giao diện chính #2")]),
    ("", [("final", "Kết thúc")]),
]
ACT_M2_FLOWS = [
    ("Bắt đầu", "Hiển thị giao diện chính", ""),
    ("Hiển thị giao diện chính", "Gửi message GET_RANKING", "click xem bảng xếp hạng"),
    ("Gửi message GET_RANKING", "Lấy bảng xếp hạng", ""),
    ("Lấy bảng xếp hạng", "Yêu cầu trang của người chơi?", ""),
    ("Yêu cầu trang của người chơi?", "Tìm dòng của người chơi, lấy trang chứa dòng đó", "mine"),
    ("Yêu cầu trang của người chơi?", "Lấy các dòng của trang được yêu cầu", "số trang"),
    ("Tìm dòng của người chơi, lấy trang chứa dòng đó", "#1", ""),
    ("Lấy các dòng của trang được yêu cầu", "#1", ""),
    ("#1", "Gửi message RANKING", ""),
    ("Gửi message RANKING", "Hỏi vị trí mà chưa có trong bảng?", ""),
    ("Hỏi vị trí mà chưa có trong bảng?", "Báo chưa có trong bảng xếp hạng", "mine, me rỗng"),
    ("Hỏi vị trí mà chưa có trong bảng?", "Hiển thị trang, tô dòng của người chơi", "còn lại"),
    ("Báo chưa có trong bảng xếp hạng", "#3", ""),
    ("Hiển thị trang, tô dòng của người chơi", "#3", ""),
    ("#3", "Người chơi chọn gì?", ""),
    ("Người chơi chọn gì?", "Gửi message GET_RANKING", "click chuyển trang hoặc vị trí của tôi"),
    ("Người chơi chọn gì?", "Hiển thị giao diện chính #2", "click quay lại"),
    ("Hiển thị giao diện chính #2", "Kết thúc", ""),
]

ACT_M4 = [
    ("", [("initial", "Bắt đầu")]),
    ("Xử lí tại MainActivity (máy A)", [("action", "Hiển thị giao diện chính")]),
    ("Xử lí tại LobbyActivity (máy A)", [("action", "Gửi GET_ONLINE_LIST"), ("merge", "#4")]),
    ("Xử lí tại server: ChallengeController.handleGetOnlineList()", [
        ("action", "Lấy danh sách online"), ("dao", "OnlinePlayerManager: getOnlinePlayers()"),
        ("dao", "PlayerRankingDAO: getTotalPoints()"), ("action", "Gửi ONLINE_LIST cho A")]),
    ("Xử lí tại LobbyActivity (hiển thị)", [("action", "Hiển thị danh sách người chơi online")]),
    ("Xử lí tại ChallengeConfirmDialog (máy A)", [
        ("action", "Hiển thị dialog xác nhận thách đấu B"), ("decision", "A chọn gì?"),
        ("action", "Gửi INVITE, hiện popup chờ 30 giây"), ("action", "Đóng popup, báo lý do")]),
    ("Xử lí tại server: ChallengeController.handleInvite()", [
        ("action", "Ghi lời mời, hẹn giờ 30 giây"), ("dao", "OnlinePlayerManager: sendInvitation()"),
        ("decision", "B online và không bận?"), ("action", "Gửi INVITE_RECEIVED cho B"),
        ("action", "Gửi INVITE_RESULT cho A")]),
    ("Xử lí tại InvitationDialog (máy B)", [("action", "Hiển thị popup lời mời"), ("decision", "B trả lời?")]),
    ("Xử lí tại server: ChallengeController.handleInviteReply()", [
        ("action", "Gỡ lời mời"), ("dao", "OnlinePlayerManager: removeInvitation()"),
        ("action", "Sinh đề bài"), ("dao", "BoardGenerator: generateBoard()"),
        ("action", "Tạo phòng chơi"), ("dao", "MatchManager: createMatch()"),
        ("action", "Chuyển A, B sang bận, đóng các lời mời khác"), ("dao", "OnlinePlayerManager: updateStatus()"),
        ("action", "Gửi MATCH_START, gửi lại danh sách online")]),
    ("Xử lí tại MatchActivity (A, B)", [
        ("action", "Hiển thị bàn chơi, đồng hồ"), ("action", "Nhận hai ô được click, gửi MOVE")]),
    ("Xử lí tại server: MatchController.handleMove()", [
        ("action", "Kiểm tra đường nối trên bàn của người gửi"), ("dao", "PathFinder: checkPath()"),
        ("action", "Xóa hai ô, kiểm tra hết ô"), ("dao", "Board: removePair(), isCleared()"),
        ("action", "Kiểm tra bế tắc, xáo riêng bàn đó nếu cần"),
        ("dao", "PathFinder: findAvailablePair(), BoardGenerator: shuffle()"),
        ("action", "Gửi MOVE_RESULT, OPPONENT_PROGRESS"),
        ("decision", "Có người xóa hết ô hoặc hết giờ?")]),
    ("Xử lí tại server: MatchController.endMatch()", [
        ("action", "Tính kết quả và điểm"), ("action", "Lưu ván đấu và hai kết quả"),
        ("dao", "MatchDAO: saveMatch()"), ("action", "Gửi MATCH_END cho A và B")]),
    ("Xử lí tại ResultDialog (A, B)", [("action", "Hiển thị kết quả, hỏi tái đấu trong 30 giây")]),
    ("Xử lí tại server: MatchController.handleRematchReply()", [
        ("decision", "Hai người chọn gì?"),
        ("action", "Bỏ phòng cũ, sinh đề mới, tạo phòng mới"), ("dao", "MatchManager: removeRoom(), createMatch()"),
        ("action", "Chuyển A, B về rỗi, bỏ phòng"), ("dao", "OnlinePlayerManager: updateStatus() #2"),
        ("action", "Gửi OPPONENT_LEFT cho B"), ("action", "Gửi danh sách online mới"),
        ("dao", "OnlinePlayerManager: getOnlinePlayers() #2"), ("dao", "PlayerRankingDAO: getTotalPoints() #2")]),
    ("Xử lí tại LobbyActivity (A, B)", [("action", "Hiển thị danh sách online đã cập nhật")]),
    ("", [("final", "Kết thúc")]),
]
ACT_M4_FLOWS = [
    ("Bắt đầu", "Hiển thị giao diện chính", ""),
    ("Hiển thị giao diện chính", "Gửi GET_ONLINE_LIST", "A click đấu đối kháng"),
    ("Gửi GET_ONLINE_LIST", "Lấy danh sách online", ""),
    ("Lấy danh sách online", "Gửi ONLINE_LIST cho A", ""),
    ("Gửi ONLINE_LIST cho A", "#4", ""),
    ("#4", "Hiển thị danh sách người chơi online", ""),
    ("Hiển thị danh sách người chơi online", "Hiển thị dialog xác nhận thách đấu B", "click tên B không bận"),
    ("Hiển thị dialog xác nhận thách đấu B", "A chọn gì?", ""),
    ("A chọn gì?", "#4", "click hủy"),
    ("A chọn gì?", "Gửi INVITE, hiện popup chờ 30 giây", "click xác nhận"),
    ("Gửi INVITE, hiện popup chờ 30 giây", "Ghi lời mời, hẹn giờ 30 giây", ""),
    ("Ghi lời mời, hẹn giờ 30 giây", "B online và không bận?", ""),
    ("B online và không bận?", "Gửi INVITE_RECEIVED cho B", "có"),
    ("B online và không bận?", "Gửi INVITE_RESULT cho A", "không"),
    ("Gửi INVITE_RECEIVED cho B", "Hiển thị popup lời mời", ""),
    ("Hiển thị popup lời mời", "B trả lời?", ""),
    ("B trả lời?", "Gửi INVITE_RESULT cho A", "reject, hết 30 giây hoặc A hủy"),
    ("Gửi INVITE_RESULT cho A", "Đóng popup, báo lý do", ""),
    ("Đóng popup, báo lý do", "#4", ""),
    ("B trả lời?", "Gỡ lời mời", "accept"),
    ("Gỡ lời mời", "Sinh đề bài", ""),
    ("Sinh đề bài", "Tạo phòng chơi", ""),
    ("Tạo phòng chơi", "Chuyển A, B sang bận, đóng các lời mời khác", ""),
    ("Chuyển A, B sang bận, đóng các lời mời khác", "Gửi MATCH_START, gửi lại danh sách online", ""),
    ("Gửi MATCH_START, gửi lại danh sách online", "Hiển thị bàn chơi, đồng hồ", ""),
    ("Hiển thị bàn chơi, đồng hồ", "Nhận hai ô được click, gửi MOVE", ""),
    ("Nhận hai ô được click, gửi MOVE", "Kiểm tra đường nối trên bàn của người gửi", ""),
    ("Kiểm tra đường nối trên bàn của người gửi", "Xóa hai ô, kiểm tra hết ô", ""),
    ("Xóa hai ô, kiểm tra hết ô", "Kiểm tra bế tắc, xáo riêng bàn đó nếu cần", ""),
    ("Kiểm tra bế tắc, xáo riêng bàn đó nếu cần", "Gửi MOVE_RESULT, OPPONENT_PROGRESS", ""),
    ("Gửi MOVE_RESULT, OPPONENT_PROGRESS", "Có người xóa hết ô hoặc hết giờ?", ""),
    ("Có người xóa hết ô hoặc hết giờ?", "Hiển thị bàn chơi, đồng hồ", "chưa"),
    ("Có người xóa hết ô hoặc hết giờ?", "Tính kết quả và điểm", "rồi"),
    ("Tính kết quả và điểm", "Lưu ván đấu và hai kết quả", ""),
    ("Lưu ván đấu và hai kết quả", "Gửi MATCH_END cho A và B", ""),
    ("Gửi MATCH_END cho A và B", "Hiển thị kết quả, hỏi tái đấu trong 30 giây", ""),
    ("Hiển thị kết quả, hỏi tái đấu trong 30 giây", "Hai người chọn gì?", ""),
    ("Hai người chọn gì?", "Bỏ phòng cũ, sinh đề mới, tạo phòng mới", "cả hai tái đấu"),
    ("Bỏ phòng cũ, sinh đề mới, tạo phòng mới", "Hiển thị bàn chơi, đồng hồ", ""),
    ("Hai người chọn gì?", "Chuyển A, B về rỗi, bỏ phòng", "A dừng hoặc hết 30 giây"),
    ("Chuyển A, B về rỗi, bỏ phòng", "Gửi OPPONENT_LEFT cho B", ""),
    ("Gửi OPPONENT_LEFT cho B", "Gửi danh sách online mới", ""),
    ("Gửi danh sách online mới", "Hiển thị danh sách online đã cập nhật", ""),
    ("Hiển thị danh sách online đã cập nhật", "Kết thúc", ""),
]

# Bản cho tài liệu của Dev 2: vòng lặp chơi ván (MatchActivity, MatchController.handleMove(), phần của Dev 1)
# thu lại thành một hành động
ACT_M4_PLAY = "Chơi ván (phần của Dev 1)"
ACT_M4_DEV2 = [f if not f[0].startswith(("Xử lí tại MatchActivity", "Xử lí tại server: MatchController.handleMove()"))
               else None for f in ACT_M4]
ACT_M4_DEV2[ACT_M4_DEV2.index(None)] = ("Xử lí tại MatchActivity (A, B)", [
    ("action", "Hiển thị bàn chơi, đồng hồ"), ("action", ACT_M4_PLAY)])
ACT_M4_DEV2 = [f for f in ACT_M4_DEV2 if f]
_PLAY_NODES = {"Nhận hai ô được click, gửi MOVE", "Kiểm tra đường nối trên bàn của người gửi", "Xóa hai ô, kiểm tra hết ô",
               "Kiểm tra bế tắc, xáo riêng bàn đó nếu cần", "Gửi MOVE_RESULT, OPPONENT_PROGRESS",
               "Có người xóa hết ô hoặc hết giờ?"}
_FIRST_PLAY = next(i for i, f in enumerate(ACT_M4_FLOWS) if f[0] in _PLAY_NODES or f[1] in _PLAY_NODES)
_KEPT = [f for f in ACT_M4_FLOWS if f[0] not in _PLAY_NODES and f[1] not in _PLAY_NODES]
ACT_M4_DEV2_FLOWS = (_KEPT[:_FIRST_PLAY]
                     + [("Hiển thị bàn chơi, đồng hồ", ACT_M4_PLAY, ""),
                        (ACT_M4_PLAY, "Tính kết quả và điểm", "một người xóa hết ô hoặc hết giờ")]
                     + _KEPT[_FIRST_PLAY:])

# ---- Pha 3: luồng message giữa hai client và server (protocol.puml)

PROTOCOL_PARTS = [("participant", "Client A"), ("participant", "Server"), ("participant", "Client B")]
PROTOCOL = [
    # đăng nhập
    ("msg", "Client A", "Server", "LOGIN {username, password}"),
    ("msg", "Server", "Client A", "LOGIN_RESULT {ok, player}"),
    ("msg", "Client A", "Server", "PING (mỗi 5 giây, suốt phiên)"),
    ("msg", "Client A", "Server", "GET_ONLINE_LIST"),
    ("msg", "Server", "Client A", "ONLINE_LIST"),
    ("msg", "Server", "Client B", "ONLINE_LIST (A vừa online)"),
    # thách đấu
    ("msg", "Client A", "Server", "INVITE {to: B}"),
    ("msg", "Server", "Client B", "INVITE_RECEIVED {from: A, timeoutSec: 30}"),
    ("msg", "Client B", "Server", "INVITE_REPLY {from: A, accept: true}"),
    ("msg", "Server", "Client A", "MATCH_START {roomId, tiles, timeLimitSec, opponent}"),
    ("msg", "Server", "Client B", "MATCH_START {roomId, tiles, timeLimitSec, opponent}"),
    ("msg", "Client B", "Server", "INVITE_REPLY {from: A, accept: false}"),
    ("msg", "Server", "Client A", "INVITE_RESULT {to: B, accepted: false, reason: REJECTED}"),
    ("msg", "Client A", "Server", "INVITE_CANCEL {to: B}"),
    ("msg", "Server", "Client B", "INVITE_CLOSED {from: A, reason: CANCELLED}"),
    ("msg", "Server", "Client A", "INVITE_RESULT {to: B, accepted: false, reason: EXPIRED}"),
    ("msg", "Server", "Client B", "INVITE_CLOSED {from: A, reason: EXPIRED}"),
    # chơi ván
    ("msg", "Client A", "Server", "MOVE {roomId, a, b}"),
    ("msg", "Server", "Client A", "MOVE_RESULT {ok, path, remaining}"),
    ("msg", "Server", "Client B", "OPPONENT_PROGRESS {remaining, a, b, path, tiles, shuffled}"),
    ("msg", "Server", "Client A", "BOARD_SHUFFLED {tiles}"),
    ("msg", "Server", "Client A", "MATCH_END {result, point, finishTime, remaining, reason, rematch}"),
    ("msg", "Server", "Client B", "MATCH_END {result, point, finishTime, remaining, reason, rematch}"),
    # sau ván
    ("msg", "Client A", "Server", "REMATCH_REPLY {roomId, accept}"),
    ("msg", "Client B", "Server", "REMATCH_REPLY {roomId, accept}"),
    ("msg", "Server", "Client A", "MATCH_START (ván mới)"),
    ("msg", "Server", "Client B", "MATCH_START (ván mới)"),
    ("msg", "Server", "Client B", "OPPONENT_LEFT"),
    ("msg", "Server", "Client A", "ONLINE_LIST"),
    ("msg", "Server", "Client B", "ONLINE_LIST"),
]
# (toán tử, điều kiện các nhánh, thông điệp đầu, thông điệp cuối, thông điệp đầu của các nhánh sau), theo số thứ tự ở trên
PROTOCOL_FRAGMENTS = [
    ("alt", "B chấp nhận|B từ chối|A hủy lời mời|hết 30 giây", 9, 17, "12,14,16"),
    ("loop", "đến khi ván kết thúc", 18, 21, ""),
    ("opt", "bàn của A bế tắc", 21, 21, ""),
    ("alt", "cả hai tái đấu|một người dừng (ví dụ A)", 26, 30, "28"),
]


def build_protocol(name):
    build_sequence(name, PROTOCOL_PARTS, PROTOCOL)
    for op, guard, first, last, starts in PROTOCOL_FRAGMENTS:
        vp("addCombinedFragment", diagramName=name, operator=op, guard=guard, coveredLifelines="",
           fromSeq=first, toSeq=last, operandStarts=starts)


# ---- lớp thực thể (Hình 2.1 và 3.1), xếp ngang theo bản Dev 1 chỉnh Hình 3.1 trên Google Doc

ENTITY_POS = {"Board": (40, 40), "MatchResult": (300, 80), "Match": (40, 260), "Player": (330, 270),
              "PlayerRanking": (560, 250)}  # PlayerRanking cao hơn Player để watermark của VP không đè lên


# ---- Pha 3: lớp thiết kế M4 (Hình 3.16): thứ tự các hàng dưới chọn bằng cách thử mọi hoán vị, lấy phương án
# có ít đường thẳng bị lớp khác chắn nhất (các đường đó phải đi vòng vuông góc)

M4_ROWS = [(367, 200, ["ServerConnection"]), (367, 500, ["MessageListener"]),
           (766, 40, ["BoardGenerator", "MatchManager", "MatchRoom"]),
           (990, 240, ["PlayerRankingDAO", "DAO", "MatchDAO", "PathFinder"]),
           (1130, 340, ["PlayerRanking", "Player", "MatchResult", "Match", "Board"])]


# ---- Pha 3: CSDL (erd.puml)

def build_erd(name):
    """Kiểu cột theo MySQL (int(10), datetime): main() đặt CSDL mặc định của project là MySQL trước,
    nếu không VP đổi sang kiểu chung integer, timestamp."""
    vp("createErd", diagramName=name)
    tables = {
        "tblPlayer": [("id", "int(10)", True, False, True), ("username", "varchar(50)", False, False, True),
                      ("password", "varchar(64)", False, False, False)],
        "tblMatch": [("id", "int(10)", True, False, True), ("startTime", "datetime", False, False, False),
                     ("endTime", "datetime", False, False, False), ("timeLimit", "int(10)", False, False, False),
                     ("tiles", "varchar(300)", False, False, False)],
        "tblMatchResult": [("id", "int(10)", True, False, True), ("result", "varchar(10)", False, False, False),
                           ("finishTime", "int(10)", False, True, False), ("tiles", "varchar(300)", False, False, False)],
    }
    for table, cols in tables.items():
        vp("addTable", diagramName=name, tableName=table)
        for col, typ, pk, nullable, unique in cols:
            vp("addColumn", diagramName=name, tableName=table, columnName=col, columnType=typ, length=0, scale=0,
               isPrimaryKey=pk, isNullable=nullable, isUnique=unique)
    vp("addForeignKey", diagramName=name, fromTable="tblMatchResult", toTable="tblMatch", fromColumn="matchId",
       toColumn="", relationshipName="")
    vp("addForeignKey", diagramName=name, fromTable="tblMatchResult", toTable="tblPlayer", fromColumn="playerId",
       toColumn="", relationshipName="")
    vp("autoLayoutDiagram", diagramName=name)


# ---- Pha 3: biểu đồ gói (package.puml)

PACKAGES = {
    "app (Android client)": {
        "view": ["LoginActivity", "RegisterActivity", "BaseActivity", "MainActivity", "LobbyActivity", "RankingActivity",
                 "DifficultyActivity", "BotGameActivity", "MatchActivity", "ChallengeConfirmDialog",
                 "InvitationDialog", "ResultDialog", "BotResultDialog", "BoardView"],
        "network": ["ServerConnection", "MessageListener", "ClientSession"]},
    "server (Java)": {
        "controller": ["GameServer", "ClientHandler", "LoginController", "RankingController", "ChallengeController",
                       "MatchController", "OnlinePlayerManager", "MatchManager", "MatchRoom"],
        "dao": ["DAO", "PlayerDAO", "PlayerRankingDAO", "MatchDAO"]},
    "core (Java thuần, dùng chung)": {
        "model": ["Player", "PlayerRanking", "Match", "MatchResult", "Board", "Cell"],
        "logic": ["PathFinder", "BoardGenerator", "Bot", "Difficulty"],
        "protocol": ["Message", "MessageType"]},
}
PACKAGE_DEPS = [("view", "network"), ("view", "logic"), ("view", "model"), ("network", "protocol"),
                ("controller", "dao"), ("controller", "logic"), ("controller", "protocol"), ("dao", "model"),
                ("logic", "model"), ("network", "model"), ("controller", "model")]


def build_package(name):
    vp("createPackageDiagram", diagramName=name)
    for top, subs in PACKAGES.items():
        vp("addPackageElement", diagramName=name, packageName=top, parentPackage="", x=0, y=0, width=0, height=0,
           backgroundColor="")
        for sub, classes in subs.items():
            vp("addPackageElement", diagramName=name, packageName=sub, parentPackage=top, x=0, y=0, width=0,
               height=0, backgroundColor="")
            for c in classes:
                vp("addClassInPackage", diagramName=name, packageName=sub, className=c)
    for a, b in PACKAGE_DEPS:
        vp("addPackageDependency", diagramName=name, fromPackage=a, toPackage=b, kind="use")
    vp("autoLayoutDiagram", diagramName=name)


# ---- Pha 3: biểu đồ triển khai (deployment.puml)

def build_deployment(name):
    vp("createDeploymentDiagram", diagramName=name)
    nodes = [("Thiết bị Android (người chơi A)", "device", "pikachu.apk (app + core), máy A"),
             ("Thiết bị Android (người chơi B)", "device", "pikachu.apk (app + core), máy B"),
             ("Máy chủ game", "device", None),
             ("Máy chủ CSDL (MySQL 8)", "device", "CSDL pikachu (tblPlayer, tblMatch, tblMatchResult)")]
    pos = [(40, 40), (440, 40), (220, 260), (240, 520)]  # ghim: layout tự động đặt máy B bên trái máy A
    for (node, kind, artifact), (x, y) in zip(nodes, pos):
        vp("addDeploymentNode", diagramName=name, nodeName=node, kind=kind, parentNode="", x=x, y=y, width=0, height=0)
        if artifact:
            vp("addDeploymentArtifact", diagramName=name, artifactName=artifact, nodeName=node, x=0, y=0)
    # máy chủ game là thiết bị; JVM là môi trường thực thi nằm trong thiết bị đó
    vp("addDeploymentNode", diagramName=name, nodeName="JVM (Java 11)", kind="executionEnvironment",
       parentNode="Máy chủ game", x=0, y=0, width=0, height=0)
    vp("addDeploymentArtifact", diagramName=name, artifactName="pikachu-server.jar (server + core)",
       nodeName="JVM (Java 11)", x=0, y=0)
    for dev in nodes[:2]:
        vp("addCommunicationPath", diagramName=name, fromNode=dev[0], toNode=nodes[2][0],
           label="TCP, cổng 9999, JSON mỗi dòng một message")
    vp("addCommunicationPath", diagramName=name, fromNode=nodes[2][0], toNode=nodes[3][0], label="JDBC")
    vp("rerouteConnectors", diagramName=name)


# ================================================================ danh sách biểu đồ
# (khoá = tên file ảnh, tên biểu đồ trong VP, hàm dựng)
# Hậu tố "(27-10)": bản dựng lại sau đợt sửa thiết kế ngày 27-10 bằng --add (không dựng lại toàn bộ để giữ bố cục
# chỉnh tay của các hình không đổi); bản cũ cùng khoá còn trong pikachu.vpp cho tới khi xóa tay. Gói mô hình của các
# biểu đồ lớp dựng lại mang hậu tố _2710 để không trùng lớp với bản cũ.

DIAGRAMS = [
    ("uc-overview", "1.1 Use case tổng quan", lambda n: build_usecase(n, "pha1/uc-overview.puml", "fan")),
    ("uc-m2-view-ranking", "1.3 Use case M2 View Ranking (27-10)", lambda n: build_usecase(n, "pha1/uc-m2-view-ranking.puml", "lines")),
    ("uc-m4-play-online-match", "1.5 Use case M4 Play Online Match (27-10)",
     lambda n: build_usecase(n, "pha1/uc-m4-play-online-match.puml", "lines")),
    ("class-entity", "2.1 Lớp thực thể pha phân tích", lambda n: build_class(n, "pha2/class-entity.puml", "pha2_entity", ENTITY_POS)),
    ("class-analysis-m2", "2.3 Lớp phân tích M2", lambda n: build_class(n, "pha2/class-analysis-m2.puml", "pha2_m2", oblique_up=True)),
    ("class-analysis-m4", "2.5 Lớp phân tích M4 (27-10)", lambda n: build_class(n, "pha2/class-analysis-m4.puml", "pha2_m4_2710", oblique_up=True)),
    ("state-m2", "2.7 Trạng thái M2", lambda n: build_state(n, "pha2/state-m2.puml")),
    ("state-m4", "2.9 Trạng thái M4 (27-10)", lambda n: build_state(n, "pha2/state-m4.puml", STATE_M4_POS)),
    ("seq-analysis-m2", "2.11 Tuần tự phân tích M2", lambda n: build_sequence(n, SEQA_M2_PARTS, SEQA_M2)),
    ("seq-analysis-m4-3", "2.15 Tuần tự phân tích M4 (tái đấu) (27-10)",
     lambda n: build_sequence(n, SEQA_M4_PARTS, SEQA_M4_3)),
    ("class-design-entity", "3.1 Lớp thực thể pha thiết kế (27-10)",
     lambda n: build_class(n, "pha3/class-design-entity.puml", "pha3_entity_2710", ENTITY_POS)),
    ("erd", "3.2 Cơ sở dữ liệu", build_erd),
    ("class-design-m2", "3.7 Lớp thiết kế M2 (27-10)", lambda n: build_class(n, "pha3/class-design-m2.puml", "pha3_m2_2710", oblique_up=True)),
    ("class-design-m4", "3.16 Lớp thiết kế M4 (27-10)", lambda n: build_class(n, "pha3/class-design-m4.puml", "pha3_m4_2710", oblique_up=True, rows=M4_ROWS)),
    ("activity-m2", "3.18 Hoạt động M2 (27-10)", lambda n: build_activity(n, ACT_M2, ACT_M2_FLOWS)),
    ("seq-m2", "3.19 Tuần tự thiết kế M2",
     lambda n: build_sequence(n, seq_parts(gen_seq.M2_PARTS), seq_steps(gen_seq.M2_PARTS, gen_seq.M2))),
    ("activity-m4", "3.22 Hoạt động M4 (27-10)", lambda n: build_activity(n, ACT_M4, ACT_M4_FLOWS)),
    ("seq-m4-4", "3.26 Tuần tự thiết kế M4 (tái đấu) (27-10)",
     lambda n: build_sequence(n, seq_parts(gen_seq.M4D_PARTS), seq_steps(gen_seq.M4D_PARTS, gen_seq.M4D))),
    ("seq-m4-5", "3.27 Tuần tự thiết kế M4 (thoát ván)",
     lambda n: build_sequence(n, seq_parts(gen_seq.M4E_PARTS), seq_steps(gen_seq.M4E_PARTS, gen_seq.M4E))),
    ("package", "3.27 Biểu đồ gói (27-10)", build_package),
    ("deployment", "3.28 Biểu đồ triển khai (27-10)", build_deployment),
    ("protocol", "3.29 Luồng message (27-10)", build_protocol),
    # bản hoạt động M4 cho tài liệu của Dev 2 (docs/dev2/build_gdoc.py, không có trong md); hai biểu đồ tuần tự M4
    # vẽ đủ mọi bước trong một hình, ảnh được cắt thành nhiều phần theo SLICES (Hình 2.13-2.14, 3.23-3.25).
    # Tên biểu đồ trong VP còn chữ "Thử:" từ lần dựng đầu; giữ nguyên để --export tìm được trong pikachu.vpp đã chỉnh tay
    ("activity-m4-dev2", "Hoạt động M4 (bản Dev 2) (27-10)", lambda n: build_activity(n, ACT_M4_DEV2, ACT_M4_DEV2_FLOWS)),
    ("seq-analysis-m4-full", "Thử: tuần tự phân tích M4 đầy đủ (27-10)",
     lambda n: build_sequence(n, SEQA_M4_PARTS, SEQA_M4_1 + SEQA_M4_PLAY + SEQA_M4_2, xs=SEQA_M4_X)),
    ("seq-m4-full", "Thử: tuần tự thiết kế M4 đầy đủ (27-10)",
     lambda n: build_sequence(n, seq_parts(M4_FULL_PARTS), seq_steps(M4_FULL_PARTS, gen_seq.M4A + gen_seq.M4B + gen_seq.M4C),
                              xs=SEQ_M4_X)),
    # bản của Dev 2 thay cho hai bản đầy đủ trong tài liệu Dev 2 (docs/dev2/build_gdoc.py): bỏ vòng lặp chơi ván
    ("seq-analysis-m4-dev2", "Tuần tự phân tích M4 (bản Dev 2) (27-10)",
     lambda n: build_sequence(n, SEQA_M4_PARTS, SEQA_M4_DEV2, xs=SEQA_M4_X)),
    ("seq-m4-dev2", "Tuần tự thiết kế M4 (bản Dev 2) (27-10)",
     lambda n: build_sequence(n, seq_parts(M4_FULL_PARTS), seq_steps(M4_FULL_PARTS, SEQ_M4_DEV2), xs=SEQ_M4_X)),
]


# ảnh tuần tự dài: số phần cắt (docs/vp/slice_seq.py), mỗi phần lặp lại hàng đầu lifeline; bản Dev 2 cắt cùng số
# phần với bản đầy đủ để số hình trong tài liệu Dev 2 khớp tài liệu chung
SLICES = {"seq-analysis-m4-full": 2, "seq-m4-full": 3, "seq-analysis-m4-dev2": 2, "seq-m4-dev2": 3}


def export(name, key):
    vp("exportDiagramImage", diagramName=name, filePath=str(PNG / f"{key}.png"))
    if key in SLICES:
        import slice_seq
        slice_seq.slice_png(key, SLICES[key])


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    ap = argparse.ArgumentParser()
    ap.add_argument("--only", help="danh sách khoá, phân cách bằng dấu phẩy; dựng thử vào project đang mở, không lưu")
    ap.add_argument("--export", help="danh sách khoá: chỉ xuất ảnh các biểu đồ có sẵn trong project đang mở")
    ap.add_argument("--add", help="danh sách khoá: dựng các biểu đồ chưa có vào pikachu.vpp đang mở, đúng tên, rồi lưu")
    ap.add_argument("--force", action="store_true", help="dựng lại toàn bộ dù pikachu.vpp đã được chỉnh tay")
    a = ap.parse_args()
    names = {k: n for k, n, _ in DIAGRAMS}
    if a.export:
        for key in a.export.split(","):
            export(names[key], key)
            print("exported", key)
        return
    if a.add:
        # thêm biểu đồ mới mà không dựng lại toàn bộ (giữ các bố cục chỉnh tay); project đang mở phải là pikachu.vpp
        info = json.loads(re.search(r"\{.*\}", vp("getProjectInfo"))[0])
        if not info.get("file") or Path(info["file"]).resolve() != VPP:
            raise SystemExit(f"project đang mở không phải {VPP}")
        existing = vp("listDiagrams")
        builds = {k: b for k, _, b in DIAGRAMS}
        for key in a.add.split(","):
            if f"- {names[key]} (" in existing:
                raise SystemExit(f"đã có biểu đồ {names[key]}")
            builds[key](names[key])
            export(names[key], key)
            print("added", key)
        vp("saveProject")
        print("saved", VPP)
        return
    only = set(a.only.split(",")) if a.only else None
    if only and only - {k for k, _, _ in DIAGRAMS}:
        raise SystemExit(f"khoá lạ: {only - {k for k, _, _ in DIAGRAMS}}")
    global RUN
    if only:
        # dựng thử: thêm vào project đang mở, không newProject (VP sẽ hỏi lưu project chưa lưu), không lưu;
        # tên biểu đồ và gói mô hình mang hậu tố của lần chạy để không đụng các lần thử trước
        RUN = time.strftime("_%H%M%S")
    else:
        edited = VPP.exists() and (not STAMP.exists() or STAMP.read_text().strip() != str(VPP.stat().st_mtime_ns))
        if edited and not a.force:
            raise SystemExit("pikachu.vpp đã được lưu sau lần dựng tự động gần nhất, có thể có bố cục chỉnh tay ("
                             + ", ".join(HAND_EDITED) + "). Dùng --only để dựng thử, --export để xuất ảnh, "
                             "hoặc --force để xoá và dựng lại toàn bộ.")
        # pikachu.vpp đang mở mà còn thay đổi chưa lưu thì newProject làm VP hỏi "Save changes?"; file này
        # sắp bị xoá và dựng lại nên lưu luôn cho khỏi bị hỏi
        info = json.loads(re.search(r"\{.*\}", vp("getProjectInfo"))[0])
        if info.get("file") and Path(info["file"]).resolve() == VPP:
            vp("saveProject")
        vp("newProject")
        if VPP.exists():
            VPP.unlink()
    # kiểu cột MySQL (int(10), datetime) cho Hình 3.2; project mới chưa có CSDL mặc định (tool vá cục bộ)
    vp("setDefaultDatabase", databaseType="MySQL")
    PNG.mkdir(exist_ok=True)
    for key, name, build in DIAGRAMS:
        if only and key not in only:
            continue
        name += RUN
        build(name)
        export(name, key)
        print("ok", key)
    if not only:
        vp("saveProjectAs", filePath=str(VPP))
        STAMP.write_text(str(VPP.stat().st_mtime_ns))
        print("saved", VPP)


if __name__ == "__main__":
    main()
