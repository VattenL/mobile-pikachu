# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Course project (PTIT): an online head-to-head Pikachu (Onet / connect-pair) game. The user communicates in Vietnamese; project docs are in Vietnamese.

- `de_bai.md`: the original assignment spec. It is the source of truth for game rules and scoring.
- `CREDITS.md`: sources and licences of every external resource (libraries, images, fonts, sounds, borrowed code, reference documents), kept in its own file (user request). When adding any such resource, add a row there in the same change.
- `plan.md`: Part I is the software specification outline, following the phases of the course textbook (requirements → analysis → design → implementation → testing) over 4 functional modules M1–M4. Part II is the implementation plan: module architecture, the split of work between 2 devs, the `core` interface, the JSON message protocol, doc ownership (§7), and open questions still to be decided (§8). Read it before designing anything.
- Course textbook and a fully worked reference project (same method, different domain) live outside the repo at `D:\Stud Mats\Kỳ 1 năm 4\httt` (`tham-khao/BG HP TTTN 2 CNPM 2020 final.pdf`, extracts in `tham-khao/trich-bai-giang/`). Follow its step-by-step method and artifact formats when writing spec sections.

## Analysis & design documents

- `docs/pha1-thu-thap-yeu-cau.md` (requirements) and `docs/pha2-phan-tich.md` (analysis), one file per phase. Drafts (written before any code exists): `docs/pha3-thiet-ke.md` (design), `docs/pha4-cai-dat.md` (implementation: project layout, schema, per-class behaviour, unit test cases), `docs/pha5-ra-soat-kiem-thu.md` (review checklists, functional test cases). Modules: M1 Manage Account (register, login, logout), M2 View Ranking, M3 Play With Bot, M4 Play Online Match. Use case and class names are English; all prose is Vietnamese. Figures are numbered per phase (Hình 1.x, 2.x, 3.x).
- Phase 3 kịch bản v3 and the sequence diagrams `seq-*.puml` are generated from one step list by `docs/uml/pha3/gen_seq.py` (it rewrites the `<!-- BEGIN v3-xx -->` blocks in `pha3-thiet-ke.md`). Edit the generator, never the lists or `seq-*.puml` by hand. M4 has five step lists: challenge (`M4A`, Dev 2), match play (`M4B`, Dev 1), match end when a player stops (`M4C`, Dev 2), the rematch branch for exception 13a (`M4D`, Dev 2, numbered from 1) and the quit branch for exception 9a (`M4E`, Dev 2, numbered from 1). Every call in a sequence diagram must travel along a link in the matching `class-design-m*.puml`.
- Visual Paradigm (user decision, 2026-10): the diagrams of the shared parts and of Dev 2's modules (M2, M4 challenge and match end) are drawn in VP CE, not PlantUML. `docs/vp/build_vp.py` builds `docs/vp/pikachu.vpp` and `docs/vp/png/<key>.png` through the local VP MCP plugin (VP must be open): use case, class and state diagrams are read from the `.puml` files, design-phase sequences from `gen_seq.py`, and the rest (analysis sequences from kịch bản v2, ERD, package, deployment) is declared in the script. Design-phase M2 and M4 have no activity diagram (user decision 2026-10-27: the lecturer asks for either activity or sequence, Dev 2 keeps sequence); Dev 1's M1, M3 activity diagrams stay. Edit the source and rerun; a full run deletes and rebuilds the `.vpp`. Exception (user decision 2026-10-26): the user hand-adjusts the layout of the diagrams listed in `HAND_EDITED` (class diagrams 2.1, 2.3, 2.5, 3.1, 3.7, 3.15 and state 2.9) in VP after a build, so a full run refuses once `pikachu.vpp` was saved after the last automated build (`build-stamp.txt`, git-ignored) unless `--force` is given, which loses those hand edits. Content changes to a hand-edited diagram still go into its source first, then the user redoes the layout. `--only key,...` test-builds into the open project without saving (diagram names get a time suffix; the plugin cannot delete diagrams, so the user reopens `pikachu.vpp` without saving to drop them); `--add key,...` builds new diagrams into the open `pikachu.vpp` under their real names and saves, keeping the hand edits; `--export key,...` only re-exports PNGs from the open project. Every connector of a class, state or activity diagram has at most 3 right-angle segments (user request 2026-10-27): after building a key in `TIDY`, `build_vp.export()` calls `docs/vp/tidy.py`'s `route_lines()`, which computes straight / L / Z / U routes clear of the other shapes and places the labels, then draws them with the plugin's `setConnectorRoutes` (sixth local patch). `python docs/vp/tidy.py <key>...` does the same on an existing diagram, first copying the shape positions of the older hand-adjusted version (`TIDY_REF`) and spreading them to the new sizes. Diagrams rebuilt on 2026-10-27 carry the suffix "(27-10)" in VP (`--add` refuses an existing name); the older versions stay in `pikachu.vpp` until the user deletes them. `docs/vp/slice_seq.py <key> <n>` cuts a long sequence PNG into n images at blank rows between messages, repeating the lifeline heads (`<key>-1.png`...). M4 sequences are full-length (all steps in one diagram, sliced into pages: Hình 2.13–2.14, 3.21–3.23). The shared md shows the full version with the match-play loop; Dev 2's doc swaps in the `*-dev2` versions (user decision 2026-10-27), which skip Dev 1's match-play steps but keep the kịch bản step numbers (`("skip", k)` steps). Their lifeline spacing copies the user's hand-adjusted full diagrams (`SEQ_M4_X`, `SEQA_M4_X`) plus 10 px; `place_lifelines` shrinks the lifeline boxes first because the plugin pushes overlapping shapes apart, and boundary boxes stay 80 px wide or the icon loses its left bar. Lifelines added through the plugin's API draw an actor as a box; the user wants the stick-figure actor icon in sequence diagrams, with every actor on the left (`build_sequence` sorts actors first). The md files link those figures to `vp/png/`. Wireframes, `path-examples`, and Dev 1's modules (M1, M3, M4 match play) stay PlantUML. Analysis-phase interaction diagrams: sequence for M2 and M4, communication for M1 and M3. VP CE stamps a watermark at the bottom right of every image; the user accepts it (no padding notes).
- Dev 2's UI figures (main screen, ranking, lobby, challenge confirm, invitation, result dialog) are drawn in Excel in the textbook's style (user decision 2026-10-27): `docs/ui/build_ui.py` writes `docs/ui/giao-dien.xlsx` (one sheet per screen, openpyxl) and exports `docs/ui/png/<sheet>.png` through Excel (pywin32 COM, PDF) and PyMuPDF; edit the script and rerun. Dev 1's wireframes stay PlantUML.
- PlantUML UI wireframes (`ui-m*.puml`) embed salt with `rectangle X [ {{salt ... }} ]`; the `{{salt` form is required (a bare `{{` + `salt` line fails on the current server).
- Never write `.puml` content containing `\n` through a bash heredoc: a hook turns it into a real newline. Use the Write/Edit tools.
- PlantUML sources live in `docs/uml/pha<N>/`, one folder per phase, with the rendered `.png` next to each `.puml`. Render via the PlantUML server: `python "D:/Stud Mats/Kỳ 1 năm 4/httt/tools/render_puml.py" <file>.puml`. A `*` at the start of a label line triggers creole bullets, so write it as `<U+002A>`.
- Dev 2's Google Doc is built by `docs/dev2/build_gdoc.py`: it doubles the 2-space list indent (Python-Markdown needs 4), turns `<thead>` rows into plain bold rows (Docs pins header rows), renumbers the sub-section letters and `mục Xy` references, and folds the M4 match-play steps (Dev 1) into one line.
- The combined document lives in Google Docs. Original doc ("BTL Mobile", Pha 1, 2): https://docs.google.com/document/d/100DT2kuFF1eBq96pwHUJ5dyfTJcFGH4ZWvjk01PkTxU/edit (id `100DT2kuFF1eBq96pwHUJ5dyfTJcFGH4ZWvjk01PkTxU`). It is generated as HTML (Times New Roman, black text, 6pt paragraph spacing, images loaded from PlantUML server URLs) and uploaded with the Drive `create_file` tool. The Drive connector cannot edit an existing doc's content: `create_file` always makes a new doc. Read the Drive copy first, since the user may have edited it.
- Scoring is decided: win 1, draw 0.5, loss 0 (`de_bai.md`). The per-match point is derived from `result` and is not a DB column. Other open decisions and their proposed defaults are in `plan.md` §8; the phase 3+ drafts follow those defaults.

## Pending requirements

- M1 (Manage Account) also includes viewing and editing the player's own profile (user decision). It is deliberately kept out of `plan.md` for now. Do not add it to the module table, use cases or screens until the user says to.

## Prompt log

From now until the end of the implementation phase (user decision 2026-10-26), append every user prompt that asks for code to `docs/prompts.md`, verbatim, in the same turn the code is written. Also append any other prompt the user asks to save. Each entry gets a heading with the date and a short label; never edit or reorder earlier entries.

## Code layout

Layout (`plan.md` Part II, `docs/pha4-cai-dat.md`): three Gradle modules, all Java 11. The package skeleton was created on `main` on 2026-10-26: one file per class of the design class diagrams (fields and method signatures, bodies `// TODO` or `UnsupportedOperationException`, the owner dev in each class Javadoc), plus `MessageType`, `Difficulty`, `GameServer`, `ServerConfig`, `server/db/*.sql` stubs and `server/server.properties.example` (the real `server.properties` is git-ignored).
- `core`: plain `java-library`, no Android or socket code. Board model (10x10 play area inside a 12x12 grid with an empty border), path finder (≤ 3 segments / ≤ 2 turns), seeded board generation and shuffle, bot, protocol message classes.
- `server`: plain Java (`ServerSocket`, one thread per client, JDBC to MySQL). Depends on `core`.
- `app`: Android client. Depends on `core`.

Key design constraints:
- The server is the referee. It re-validates every client move with `core`'s path finder on that player's own board and never trusts a client-reported result.
- Both players start from the same initial board, but each removes pairs on their own copy, so the boards diverge after the first move. The server keeps one board per player, and a deadlock is per player: the server shuffles only that player's board and sends it only to them. The server also owns the 10-minute clock and the finish times.
- Accounts, match results and the leaderboard live in the server DB. There is no local client DB. Player status and pending invitations are in server memory only. Statuses (UI label / constant): Rỗi `IDLE`, Luyện tập `TRAINING` (playing the bot: still invitable), Bận `BUSY` (in a match; set only by the server). Docs that define the statuses also give the English word (idle, training, busy).
- The DB stores the initial puzzle (`tblMatch.tiles`) and each player's board at match end (`tblMatchResult.tiles`). Match facts and points are kept apart: the DB holds results, points and rankings are computed by query.
- Protocol: newline-delimited JSON with a `type` field (`plan.md` §5.2).
- Guests (user decision 2026-10-26): the app opens on the main screen (`MainActivity`, buttons Chơi với máy, Đấu đối kháng, Xem bảng xếp hạng, Cài đặt) for guests and players alike. Guests (actor `Guest`, `Player` inherits it) may only play the bot and view the ranking; Đấu đối kháng asks a guest to log in, then opens the lobby `LobbyActivity` (`LobbyScreen`), which holds the online list. Settings need no login and belong to no use case. A guest is not an entity class.
- The username is the display name: `Player` has no `name` attribute or column. The ranking is paged on the server, 10 rows per page (`GET_RANKING` with `page` or `mine`); `Locate My Rank` (Vị trí của tôi, logged-in only) extends `View Ranking`, which no longer includes `Login`.
- Wording: the invitation buttons are Accept / Reject, the post-match choice is "tái đấu" (Rematch, UC `Decide To Rematch`), never "tiếp tục". One wireframe image per screen.
- Ownership is split by function (`plan.md` Part II §2), each dev with one easy, one medium and one hard function. Dev 1: register/login/logout, the bot game (M3) with the `core` engine, and online match play (`MatchActivity`, `MatchRoom`, `MatchController.handleMove()`). Dev 2: the shared server/network/DB infrastructure, ranking (M2), lobby and the challenge/invitation flow including match setup on accept (`generateBoard()` call, `MatchManager.createMatch()`, `updateStatus()`; user decision 2026-10-26), and match end and rematch (`endMatch()`, timer, quit, disconnect, `MatchDAO`, `ResultDialog`, `handleRematchReply()`).

## UI decisions (for when coding starts)

- Game-style UI (user decision): landscape only, immersive full screen, Pikachu yellow/orange background with navy outlines, 3D buttons, a bold display font for titles. No night theme. With AppCompat themes, a plain `Button` becomes a `MaterialButton` that ignores custom backgrounds; use `androidx.appcompat.widget.AppCompatButton` for drawable-backed buttons.
- Tiles are Pokémon pixel sprites (user decision: coursework only, never published; record the source in `CREDITS.md` when added). Draw them without bitmap filtering so they stay crisp.
- Edit texts need `flagNoExtractUi`, otherwise the landscape keyboard covers the whole form.
- Android 17 (API 37) blocks sockets to LAN addresses (including the emulator's `10.0.2.2`) unless `ACCESS_LOCAL_NETWORK` is granted; it fails as a silent connect timeout.

## Build & test

Use the Gradle wrapper. From Bash run `./gradlew`; from PowerShell run `.\gradlew.bat`.
If a build fails with `jlink executable ... does not exist`, a Gradle daemon started under VS Code's bundled JRE is running: `./gradlew --stop` and build again (JDK 21 at `C:/Program Files/Eclipse Adoptium/jdk-21.0.10.7-hotspot`).

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
