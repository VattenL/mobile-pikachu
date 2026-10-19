# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Course project (PTIT): an online head-to-head Pikachu (Onet / connect-pair) game. The user communicates in Vietnamese; project docs are in Vietnamese.

- `de_bai.md`: the original assignment spec. It is the source of truth for game rules and scoring.
- `plan.md`: Part I is the software specification outline, following the phases of the course textbook (requirements → analysis → design → implementation → testing) over 3 functional modules M1/M2/M3. Part II is the implementation plan: module architecture, the split of work between 2 devs, the `core` interface, the JSON message protocol, doc ownership (§7), and open questions still to be decided (§8). Read it before designing anything.
- Course textbook and a fully worked reference project (same method, different domain) live outside the repo at `D:\Stud Mats\Kỳ 1 năm 4\httt` (`tham-khao/BG HP TTTN 2 CNPM 2020 final.pdf`, extracts in `tham-khao/trich-bai-giang/`). Follow its step-by-step method and artifact formats when writing spec sections.

## Pending requirements

- M1 also includes viewing and editing the player's own profile (user decision). It is deliberately kept out of `plan.md` for now. Do not add it to the module table, use cases or screens until the user says to.

## Current state vs. planned architecture

The repo is currently only the Android Studio template: a single `:app` module with an empty `MainActivity`. `plan.md` targets 3 Gradle modules:

- `core`: pure Java with no Android or socket dependencies. Holds the board model (10x10 play area inside a 12x12 grid with an empty border), the path finder (≤ 3 segments / ≤ 2 turns), board generation, deadlock check and shuffle (seeded), the bot, and the protocol message classes.
- `app`: Android client. Depends on `core`.
- `server`: plain Java (`ServerSocket`, one thread per client, JDBC). Depends on `core`.

Key design constraints from the plan:
- The server is the referee. It re-validates every client move with `core`'s path finder and never trusts a client-reported result.
- Both players must always see an identical board, so the server performs any shuffle (or seeds it).
- Accounts, match results and the leaderboard live in the server DB from Phase 1 onward. There is no local client DB. Online/busy status is in server memory only.
- Protocol: newline-delimited JSON with a `type` field (see the table in `plan.md` §5.2).
- Ownership: Dev A owns `core` and the game screen; Dev B owns `server`, the network client layer, and the login/lobby/ranking screens.

When adding the `core` or `server` modules, register them in `settings.gradle.kts`. Keep `core` a plain `java-library` with no Android dependencies.

## Build & test

Use the Gradle wrapper. From Bash run `./gradlew`; from PowerShell run `.\gradlew.bat`.

```bash
./gradlew :app:assembleDebug                 # build debug APK
./gradlew :app:installDebug                  # install on connected device/emulator
./gradlew :app:testDebugUnitTest             # JVM unit tests
./gradlew :app:testDebugUnitTest --tests "ptit.e23cnpm01.pikachu.ExampleUnitTest"   # single test class (append .method for one method)
./gradlew :app:connectedDebugAndroidTest     # instrumented tests (needs device)
./gradlew :app:lint
```

## Tooling notes

- AGP 9.3 with Kotlin DSL and a version catalog (`gradle/libs.versions.toml`). Add new dependencies to the catalog, not inline.
- The app is written in Java (source/target compatibility 11). compileSdk/targetSdk is 37, minSdk is 24.
- Package and namespace: `ptit.e23cnpm01.pikachu`.
