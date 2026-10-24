# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Course project (PTIT): an online head-to-head Pikachu (Onet / connect-pair) game. The user communicates in Vietnamese; project docs are in Vietnamese.

- `de_bai.md`: the original assignment spec. It is the source of truth for game rules and scoring.
- `CREDITS.md`: sources and licences of every external resource (libraries, images, fonts, sounds, borrowed code, reference documents), kept in its own file (user request). When adding any such resource, add a row there in the same change.
- `plan.md`: Part I is the software specification outline, following the phases of the course textbook (requirements → analysis → design → implementation → testing) over 3 functional modules M1/M2/M3. Part II is the implementation plan: module architecture, the split of work between 2 devs, the `core` interface, the JSON message protocol, doc ownership (§7), and open questions still to be decided (§8). Read it before designing anything.
- Course textbook and a fully worked reference project (same method, different domain) live outside the repo at `D:\Stud Mats\Kỳ 1 năm 4\httt` (`tham-khao/BG HP TTTN 2 CNPM 2020 final.pdf`, extracts in `tham-khao/trich-bai-giang/`). Follow its step-by-step method and artifact formats when writing spec sections.

## Analysis & design documents

- `docs/pha1-thu-thap-yeu-cau.md` (requirements) and `docs/pha2-phan-tich.md` (analysis), one file per phase. Drafts (written before any code exists): `docs/pha3-thiet-ke.md` (design), `docs/pha4-cai-dat.md` (implementation: project layout, schema, per-class behaviour, unit test cases), `docs/pha5-ra-soat-kiem-thu.md` (review checklists, functional test cases). Modules: M1 View Ranking, M2 Play With Bot, M3 Play Online Match, M4 Manage Account (register, login, logout). Use case and class names are English; all prose is Vietnamese. Figures are numbered per phase (Hình 1.x, 2.x, 3.x).
- Phase 3 kịch bản v3 and the sequence diagrams `seq-*.puml` are generated from one step list by `docs/uml/pha3/gen_seq.py` (it rewrites the `<!-- BEGIN v3-xx -->` blocks in `pha3-thiet-ke.md`). Edit the generator, never the lists or `seq-*.puml` by hand. Every call in a sequence diagram must travel along a link in the matching `class-design-m*.puml`.
- UI wireframes (`ui-m*.puml`) embed salt with `rectangle X [ {{salt ... }} ]`; the `{{salt` form is required (a bare `{{` + `salt` line fails on the current server).
- Never write `.puml` content containing `\n` through a bash heredoc: a hook turns it into a real newline. Use the Write/Edit tools.
- PlantUML sources live in `docs/uml/pha<N>/`, one folder per phase, with the rendered `.png` next to each `.puml`. Render via the PlantUML server: `python "D:/Stud Mats/Kỳ 1 năm 4/httt/tools/render_puml.py" <file>.puml`. A `*` at the start of a label line triggers creole bullets, so write it as `<U+002A>`.
- The combined document lives in Google Docs. Original doc ("BTL Mobile", Pha 1, 2): https://docs.google.com/document/d/100DT2kuFF1eBq96pwHUJ5dyfTJcFGH4ZWvjk01PkTxU/edit (id `100DT2kuFF1eBq96pwHUJ5dyfTJcFGH4ZWvjk01PkTxU`). It is generated as HTML (Times New Roman, black text, 6pt paragraph spacing, images loaded from PlantUML server URLs) and uploaded with the Drive `create_file` tool. The Drive connector cannot edit an existing doc's content: `create_file` always makes a new doc. Read the Drive copy first, since the user may have edited it.
- Scoring is decided: win 1, draw 0.5, loss 0 (`de_bai.md`). The per-match point is derived from `result` and is not a DB column. Other open decisions and their proposed defaults are in `plan.md` §8; the phase 3+ drafts follow those defaults.

## Pending requirements

- M1 also includes viewing and editing the player's own profile (user decision). It is deliberately kept out of `plan.md` for now. Do not add it to the module table, use cases or screens until the user says to.

## Code layout

Code is on hold until the design (phase 3) is settled: the first implementation was written too early and was removed (user decision). The repo holds only the Android Studio setup (single `app` module with the template `MainActivity`). Do not start coding until the user says to.

Planned layout (`plan.md` Part II, `docs/pha4-cai-dat.md`): three Gradle modules, all Java 11.
- `core`: plain `java-library`, no Android or socket code. Board model (10x10 play area inside a 12x12 grid with an empty border), path finder (≤ 3 segments / ≤ 2 turns), seeded board generation and shuffle, bot, protocol message classes.
- `server`: plain Java (`ServerSocket`, one thread per client, JDBC to MySQL). Depends on `core`.
- `app`: Android client. Depends on `core`.
When adding `core` or `server`, register them in `settings.gradle.kts`.

Key design constraints:
- The server is the referee. It re-validates every client move with `core`'s path finder on that player's own board and never trusts a client-reported result.
- Both players start from the same initial board, but each removes pairs on their own copy, so the boards diverge after the first move. The server keeps one board per player, and a deadlock is per player: the server shuffles only that player's board and sends it only to them. The server also owns the 10-minute clock and the finish times.
- Accounts, match results and the leaderboard live in the server DB. There is no local client DB. Player status and pending invitations are in server memory only. Statuses (UI label / constant): Rỗi `IDLE`, Luyện tập `TRAINING` (playing the bot: still invitable), Bận `BUSY` (in a match; set only by the server). Docs that define the statuses also give the English word (idle, training, busy).
- The DB stores the initial puzzle (`tblMatch.tiles`) and each player's board at match end (`tblMatchResult.tiles`). Match facts and points are kept apart: the DB holds results, points and rankings are computed by query.
- Protocol: newline-delimited JSON with a `type` field (`plan.md` §5.2).
- Ownership is split by function (`plan.md` Part II §2), each dev with one easy, one medium and one hard function. Dev 1: register/login/logout, the bot game (M2) with the `core` engine, and online match play (`MatchActivity`, `MatchManager`, `MatchRoom`, `MatchController.handleMove()`). Dev 2: the shared server/network/DB infrastructure, ranking (M1), lobby and the challenge/invitation flow, and match end and rematch (`endMatch()`, timer, quit, disconnect, `MatchDAO`, `ResultDialog`, `handleRematchReply()`).

## UI decisions (for when coding starts)

- Game-style UI (user decision): landscape only, immersive full screen, Pikachu yellow/orange background with navy outlines, 3D buttons, a bold display font for titles. No night theme. With AppCompat themes, a plain `Button` becomes a `MaterialButton` that ignores custom backgrounds; use `androidx.appcompat.widget.AppCompatButton` for drawable-backed buttons.
- Tiles are Pokémon pixel sprites (user decision: coursework only, never published; record the source in `CREDITS.md` when added). Draw them without bitmap filtering so they stay crisp.
- Edit texts need `flagNoExtractUi`, otherwise the landscape keyboard covers the whole form.
- Android 17 (API 37) blocks sockets to LAN addresses (including the emulator's `10.0.2.2`) unless `ACCESS_LOCAL_NETWORK` is granted; it fails as a silent connect timeout.

## Build & test

Use the Gradle wrapper. From Bash run `./gradlew`; from PowerShell run `.\gradlew.bat`.

```bash
./gradlew :app:assembleDebug                 # build debug APK
./gradlew :app:installDebug                  # install on connected device/emulator
./gradlew :app:testDebugUnitTest             # JVM unit tests
./gradlew :app:lintDebug
```

- MySQL 8 is installed locally (`C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe`). The root password is the user's: never ask for it in chat; open a separate console (`Start-Process cmd`) for them to type it.
- Emulator: AVD `Medium_Phone` (API 37), SDK at `E:/Android/Sdk`.

## Tooling notes

- AGP 9.3 with Kotlin DSL and a version catalog (`gradle/libs.versions.toml`). Add new dependencies to the catalog, not inline.
- All modules are Java (source/target compatibility 11). compileSdk/targetSdk is 37, minSdk is 24.
- Package and namespace: `ptit.e23cnpm01.pikachu`.
