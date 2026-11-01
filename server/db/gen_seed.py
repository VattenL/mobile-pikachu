"""Sinh server/db/seed.sql và seed-tiebreak.sql. seed.sql: 12 người chơi và 88 ván khớp bảng xếp hạng mẫu của kịch bản xem bảng xếp hạng (pha 2).

Bảng mẫu cho tổng điểm, trung bình điểm đối thủ (làm tròn 1 chữ số) và trung bình thời gian thắng (mm:ss) của
từng người. Script giải một bài toán quy hoạch nguyên (scipy.optimize.milp) để chọn số ván thắng, thua, hòa giữa
từng cặp người chơi sao cho tổng điểm đúng và trung bình điểm đối thủ lệch giá trị trong bảng dưới 0.05 (làm tròn
một chữ số thì đúng bằng bảng; bằng tuyệt đối không có lời giải trong thời gian hợp lý), rồi chọn
thời gian hoàn thành của các ván thắng để trung bình đúng từng giây. Ràng buộc thêm theo pha 5, mục 2c: một ván
thắng do đối thủ bỏ cuộc (không có finishTime), một cặp gặp nhau ít nhất hai lần.

Chạy lại: python server/db/gen_seed.py [seed|tiebreak] (cần numpy, scipy). Bộ giải dừng sau 120 giây với lời giải tốt nhất đã tìm
được, nên chạy lại trên máy khác có thể ra bộ ván khác (vẫn khớp bảng mẫu); seed.sql đã sinh được commit kèm.
"""

import itertools
import random
import sys
from datetime import datetime, timedelta
from pathlib import Path

import numpy as np
from scipy.optimize import Bounds, LinearConstraint, milp

SAMPLE_PASSWORD = "123456"
TIME_LIMIT = 600  # giây

# (tên đăng nhập, tổng điểm, TB điểm đối thủ, TB thời gian thắng mm:ss) theo bảng mẫu pha 2
PLAYERS = [
    ("hoangnam", 3.5, 8.6, "06:58"),     # A
    ("minhanh", 12.5, 9.2, "05:48"),     # B
    ("nguoichoic", 10.5, 9.5, "06:31"),
    ("nguoichoid", 9.0, 10.4, "06:05"),
    ("nguoichoie", 8.5, 8.9, "07:12"),
    ("nguoichoig", 8.0, 9.1, "06:44"),
    ("nguoichoih", 7.5, 11.0, "07:02"),
    ("nguoichoii", 7.5, 8.4, "06:55"),
    ("nguoichoik", 6.0, 7.8, "07:30"),
    ("nguoichoil", 5.5, 9.0, "08:01"),
    ("nguoichoim", 5.0, 8.2, "06:20"),
    ("nguoichoin", 4.5, 7.5, "07:45"),
]
N = len(PLAYERS)
S10 = [round(p[1] * 10) for p in PLAYERS]        # tổng điểm x10
AOS10 = [round(p[2] * 10) for p in PLAYERS]      # TB điểm đối thủ x10
AWT = [int(p[3][:2]) * 60 + int(p[3][3:]) for p in PLAYERS]  # giây
PAIRS = list(itertools.combinations(range(N), 2))


def solve():
    """Biến cho mỗi cặp (p, q), p < q: p thắng q, q thắng p, hòa; thêm z = số ván tối đa của một cặp."""
    nv = 3 * len(PAIRS) + 1
    z = nv - 1

    def var(k, kind):
        return 3 * k + kind  # 0: p thắng, 1: q thắng, 2: hòa

    rows, lo, hi = [], [], []

    def add(coef, a, b):
        rows.append(coef)
        lo.append(a)
        hi.append(b)

    for p in range(N):
        pts = np.zeros(nv)   # 2 x điểm của p
        aos = np.zeros(nv)   # sum((10 S(đối thủ) - 10 AOS_p) * số ván), |.| <= 0.49 * số ván (lệch < 0.05)
        cnt = np.zeros(nv)
        wins = np.zeros(nv)
        for k, (a, b) in enumerate(PAIRS):
            if p not in (a, b):
                continue
            q = b if p == a else a
            mine, theirs = (0, 1) if p == a else (1, 0)
            pts[var(k, mine)] = 2
            pts[var(k, 2)] = 1
            wins[var(k, mine)] = 1
            for kind in range(3):
                aos[var(k, kind)] = S10[q] - AOS10[p]
                cnt[var(k, kind)] = 1
        add(pts, S10[p] / 5, S10[p] / 5)
        add(aos - 0.49 * cnt, -np.inf, 0)
        add(aos + 0.49 * cnt, 0, np.inf)
        # mỗi người ít nhất 2 ván thắng để có thời gian thắng (một ván thắng có thể là ván đối thủ bỏ cuộc)
        add(wins, 2, np.inf)
    for k in range(len(PAIRS)):
        c = np.zeros(nv)
        c[[var(k, 0), var(k, 1), var(k, 2)]] = 1
        c[z] = -1
        add(c, -np.inf, 0)
    total = np.zeros(nv)
    total[:z] = 1
    add(total, 88, 88)

    obj = np.zeros(nv)
    obj[z] = 100          # ưu tiên trải đều các cặp
    obj[2:z:3] = 1        # rồi ít ván hòa
    res = milp(obj, constraints=LinearConstraint(np.array(rows), lo, hi),
               integrality=np.ones(nv), bounds=Bounds(0, np.inf), options={"time_limit": 120})
    if res.x is None:  # hết giờ mà chưa có lời giải nào
        raise SystemExit("No set of matches fits the sample table: " + res.message)
    x = np.round(res.x).astype(int)
    games = []  # (người 1, người 2, kết quả của người 1)
    for k, (a, b) in enumerate(PAIRS):
        games += [(a, b, "WIN")] * x[var(k, 0)]
        games += [(b, a, "WIN")] * x[var(k, 1)]
        games += [(a, b, "DRAW")] * x[var(k, 2)]
    return games, x[z]


def finish_times(rng, k, avg):
    """k thời gian hoàn thành (giây) có trung bình đúng avg, trong khoảng 3 phút đến 9 phút 50."""
    while True:
        t = [avg + rng.randint(-75, 75) for _ in range(k - 1)]
        last = k * avg - sum(t)
        t.append(last)
        if all(180 <= v <= TIME_LIMIT - 10 for v in t):
            rng.shuffle(t)
            return t


def board_string(tiles):
    return ",".join(str(v) for v in tiles)


def remove_pairs(rng, tiles, remaining):
    """Xóa các cặp cùng hình cho đến khi còn đúng remaining ô."""
    tiles = list(tiles)
    while sum(1 for v in tiles if v) > remaining:
        v = rng.choice([v for v in tiles if v])
        idx = [i for i, t in enumerate(tiles) if t == v]
        for i in rng.sample(idx, 2):
            tiles[i] = 0
    return tiles


def render(header, usernames, matches):
    """Câu lệnh INSERT cho người chơi và các ván; matches: (bắt đầu, số giây, đề bài, [(người, kết quả, ms, bàn)])."""
    out = header + ["", "INSERT INTO tblPlayer (id, username, password) VALUES"]
    out.append(",\n".join(f"  ({p + 1}, '{name}', SHA2('{SAMPLE_PASSWORD}', 256))"
                          for p, name in enumerate(usernames)) + ";")
    match_rows, result_rows = [], []
    for mid, (begin, length, initial, rows) in enumerate(matches, start=1):
        end = begin + timedelta(seconds=length)
        match_rows.append(f"  ({mid}, '{begin:%Y-%m-%d %H:%M:%S}', '{end:%Y-%m-%d %H:%M:%S}', "
                          f"{TIME_LIMIT}, '{board_string(initial)}')")
        for player, result, ft, tiles in rows:
            result_rows.append(f"  ('{result}', {'NULL' if ft is None else ft}, '{board_string(tiles)}', "
                               f"{mid}, {player + 1})")
    out += ["", "INSERT INTO tblMatch (id, startTime, endTime, timeLimit, tiles) VALUES",
            ",\n".join(match_rows) + ";",
            "", "INSERT INTO tblMatchResult (result, finishTime, tiles, matchId, playerId) VALUES",
            ",\n".join(result_rows) + ";", ""]
    return "\n".join(out)


def new_board(rng):
    initial = [v for v in range(1, 26) for _ in range(4)]
    rng.shuffle(initial)
    return initial


def write_seed():
    games, max_pair = solve()
    assert max_pair >= 2, "need a pair that met at least twice"
    rng = random.Random(2026)
    rng.shuffle(games)

    # ván thắng do đối thủ bỏ cuộc: một ván thắng của người có nhiều ván thắng nhất
    win_count = [sum(1 for g in games if g[0] == p and g[2] == "WIN") for p in range(N)]
    forfeit_winner = max(range(N), key=lambda p: win_count[p])
    forfeit_idx = next(i for i, g in enumerate(games) if g[0] == forfeit_winner and g[2] == "WIN")

    timed = {p: [] for p in range(N)}
    for i, g in enumerate(games):
        if g[2] == "WIN" and i != forfeit_idx:
            timed[g[0]].append(i)
    finish = {}
    for p in range(N):
        for i, t in zip(timed[p], finish_times(rng, len(timed[p]), AWT[p])):
            finish[i] = t

    matches = []
    start = datetime(2026, 10, 1, 8, 0, 0)
    for i, (p, q, res) in enumerate(games):
        begin = start + timedelta(hours=8 * i, minutes=rng.randint(0, 300))
        initial = new_board(rng)
        if res == "DRAW":
            length = TIME_LIMIT
            rows = [(p, "DRAW", None, remove_pairs(rng, initial, 2 * rng.randint(1, 12))),
                    (q, "DRAW", None, remove_pairs(rng, initial, 2 * rng.randint(1, 12)))]
        elif i == forfeit_idx:
            length = rng.randint(120, 400)
            rows = [(p, "WIN", None, remove_pairs(rng, initial, 2 * rng.randint(15, 30))),
                    (q, "LOSE", None, remove_pairs(rng, initial, 2 * rng.randint(15, 30)))]
        else:
            length = finish[i]
            rows = [(p, "WIN", length * 1000, [0] * 100),
                    (q, "LOSE", None, remove_pairs(rng, initial, 2 * rng.randint(2, 20)))]
        matches.append((begin, length, initial, rows))

    header = [
        "-- Dữ liệu mẫu dùng chung cho kiểm thử đơn vị và kiểm thử chức năng (pha 5, mục 2c).",
        "-- Sinh bằng server/db/gen_seed.py, không sửa tay. Chạy sau schema.sql trên CSDL rỗng:",
        "--   mysql -u pikachu -p pikachu < server/db/seed.sql",
        f"-- 12 người chơi (mật khẩu mẫu chung: {SAMPLE_PASSWORD}) và 88 ván; bảng xếp hạng khớp bảng mẫu",
        "-- của kịch bản xem bảng xếp hạng (tổng điểm, TB điểm đối thủ, TB thời gian thắng).",
        f"-- Ván {forfeit_idx + 1}: {PLAYERS[forfeit_winner][0]} thắng do đối thủ bỏ cuộc (finishTime rỗng).",
        f"-- Một cặp gặp nhau nhiều nhất {max_pair} ván.",
    ]
    path = Path(__file__).with_name("seed.sql")
    path.write_text(render(header, [pl[0] for pl in PLAYERS], matches), encoding="utf-8", newline="\n")
    print(f"{path}: {len(games)} matches, at most {max_pair} between one pair, "
          f"forfeit in match {forfeit_idx + 1} (won by {PLAYERS[forfeit_winner][0]})")


def write_tiebreak():
    """VR_7: hoangnam và minhanh cùng tổng điểm (1), cùng TB điểm đối thủ (0.5), khác TB thời gian thắng.
    nguoichoic và nguoichoid trùng cả ba tiêu chí (0.5, 0.75, rỗng) nên cùng hạng: bảng 1, 2, 3, 3."""
    rng = random.Random(7)
    start = datetime(2026, 10, 20, 9, 0, 0)
    matches = []
    for i, (p, q, res, ft) in enumerate([(0, 2, "WIN", 300), (1, 3, "WIN", 420), (2, 3, "DRAW", None)]):
        initial = new_board(rng)
        if res == "WIN":
            rows = [(p, "WIN", ft * 1000, [0] * 100), (q, "LOSE", None, remove_pairs(rng, initial, 24))]
        else:
            rows = [(p, "DRAW", None, remove_pairs(rng, initial, 10)), (q, "DRAW", None, remove_pairs(rng, initial, 14))]
        matches.append((start + timedelta(hours=i), ft or TIME_LIMIT, initial, rows))
    header = [
        "-- Dữ liệu mẫu cho các trường hợp bằng điểm ở bảng xếp hạng (pha 5: VR_7, VR_10), nạp thay cho seed.sql.",
        "-- Sinh bằng server/db/gen_seed.py, không sửa tay. Chạy sau schema.sql trên CSDL rỗng:",
        "--   mysql -u pikachu -p pikachu < server/db/seed-tiebreak.sql",
        f"-- 4 người chơi (mật khẩu mẫu chung: {SAMPLE_PASSWORD}), 3 ván. Bảng xếp hạng mong đợi:",
        "--   1 hoangnam    1    0.5   05:00",
        "--   2 minhanh     1    0.5   07:00   (cùng tổng điểm, cùng TB điểm đối thủ, thời gian thắng lâu hơn)",
        "--   3 nguoichoic  0.5  0.75  rỗng",
        "--   3 nguoichoid  0.5  0.75  rỗng    (trùng cả ba tiêu chí với nguoichoic)",
    ]
    path = Path(__file__).with_name("seed-tiebreak.sql")
    path.write_text(render(header, ["hoangnam", "minhanh", "nguoichoic", "nguoichoid"], matches),
                    encoding="utf-8", newline="\n")
    print(f"{path}: 3 matches")


def main():
    """python gen_seed.py [seed|tiebreak]: không có tham số thì sinh cả hai tệp."""
    sys.stdout.reconfigure(encoding="utf-8")
    which = sys.argv[1:] or ["seed", "tiebreak"]
    if "seed" in which:
        write_seed()
    if "tiebreak" in which:
        write_tiebreak()


if __name__ == "__main__":
    main()
