# Stratum Rust Demo

A multi-screen Android app written **100% in Rust**, with no Java except a tiny `MainActivity`. It drives the real Android SDK (views, sensors, canvas, clipboard, files) through the **Stratum Rust SDK**.

## Built on

| Project | Role |
|---|---|
| [stratum-sys](https://github.com/a-s-l-a-h/stratum-sys) | The base Stratum project: reads `android.jar` and generates the C engine (`libstratum.so`), the metadata table and the `stratum-runtime` Android module. |
| [stratum-rust](https://github.com/a-s-l-a-h/stratum-rust) | The Rust target built on stratum-sys: generates the typed Rust crate (`stratum`) and packages the SDK that this demo uses. |

You do **not** need either repo to run this demo. The generated SDK files (the engine and the Rust crate/headers) are published as a ready-made zip, see below.

## Quick start (no setup)

The generated SDK is large, so it is not stored in git. Download the full project from the **[Releases page](https://github.com/a-s-l-a-h/stratumrustdemo/releases#release-v0_01-tag)**:

1. Download `stratumrustdemo-full.zip` (the demo project with the generated Rust crate and engine already included).
2. Unzip, open the folder in Android Studio, let Gradle sync, press **Run**.

Prefer to clone the repo? Then also download `stratum-sdk.zip` from the same release and unzip it into the project root, so these folders exist:

```
app/src/main/native/
app/src/main/rust/stratum/
```

## Requirements

- Android Studio (current stable) with **NDK** and **CMake 3.22.1** (SDK Manager -> SDK Tools)
- Rust via [rustup](https://rustup.rs), plus the Android targets:
  ```
  rustup target add aarch64-linux-android x86_64-linux-android
  ```
- Device (arm64) or emulator (x86_64), Android 7.0 (API 24) or newer

## Build and run

Press **Run** in Android Studio, or:
```bash
./gradlew assembleDebug
```
The first build is slow: CMake builds `libstratum.so`, then Cargo builds the Rust app for each ABI (`cargoBuildDebug` task). Rust compiler errors appear in that task's log.

## Screens

| Screen | Shows |
|---|---|
| Home | device info, live task/note/callback counters, quick launch grid |
| Tasks | persistent to-do list (file in app storage) |
| Calc | expression parser (parentheses, history) |
| Paint | custom `CanvasView`: touch drawing, palette, undo, brush size |
| Level & Motion | accelerometer through `Handlers` (multi-method listener), bubble level, shake counter |
| Timer | stopwatch with a background thread and `run_on_ui_thread` |
| Notes | persistent notes, save/clear |
| Bench | Rust -> JNI -> Java round-trip benchmark (static/instance calls, strings, arrays, bytes, maps) |
| Arrays | typed and multi-dimensional Java arrays built from Rust |
| Settings | dark/light theme, text size (SeekBar), toast, clipboard, reset data |

## Project layout

```
app/src/main/
├── java/.../MainActivity.java      loads the Rust library
├── native/                         CMake glue + engine/  (builds libstratum.so)   [from SDK zip]
└── rust/
    ├── Cargo.toml                  package + lib name = the library name
    ├── stratum-rust.gradle.kts     runs cargo from Gradle, installs .so into jniLibs/
    ├── src/lib.rs                  the whole app
    └── stratum/                    generated Rust crate (typed Android API, API_INDEX.txt)   [from SDK zip]
stratum-runtime/                    Android library: StratumActivity and helpers
```
`native/`, `rust/stratum/`, `jniLibs/` and the Cargo build output are not in git.

## How the Rust code is organised (`lib.rs`)

- global `AppState` (pages, nav buttons, themed views) behind a `Mutex`
- helpers: `vbox/hbox/label/button/edit/card/add_child`, theming registry
- one `build_*` function per screen, all called from `on_create`
- `guard(...)` wraps every callback so a panic becomes a toast instead of a crash
- `stratum_main!` registers `on_create`, `on_back_pressed` (back goes Home) and `on_destroy`

## The library name

The native library name must be the same in four places:

| Place | Value |
|---|---|
| `app/src/main/rust/Cargo.toml` | `[package] name` **and** `[lib] name` |
| `MainActivity.java` | `System.loadLibrary("...")` |
| `gradle.properties` | `stratum.rustLib=...` |

(The default inside `stratum-rust.gradle.kts` is only a fallback.)

## Renaming the project

1. Close Android Studio and rename the folder.
2. `settings.gradle.kts`: `rootProject.name = "yourname"`.
3. `Cargo.toml`: set `name` in both `[package]` and `[lib]`.
4. `MainActivity.java`: `System.loadLibrary("yourname")`.
5. `gradle.properties`: `stratum.rustLib=yourname`.
6. Optional app id: refactor-rename the package, then update `namespace` and `applicationId` in `app/build.gradle.kts` and `app_name` in `strings.xml`.
7. Delete `.gradle/`, `.idea/`, `build/`, `app/build/`, `app/.cxx/`, `app/src/main/jniLibs/`, then sync and rebuild.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `cargo build failed ... rustup target add` | install the target named in the message |
| `UnsatisfiedLinkError ... lib<name>.so` | library name mismatch (see table above) |
| `package ID specification ... did not match` | `[package] name` differs from `stratum.rustLib` |
| `No libstratum.so found` / missing `native/` | the SDK zip was not unzipped into the project root |
| `stratum::android::...` class not found | the SDK was generated with a narrow `--emit-prefix`; regenerate wider or use `reflect` |
| NDK not found | install NDK in SDK Manager or set `ndk.dir` in `local.properties` |

## Writing your own code

See `STRATUM_RUST_API.md` in the [stratum-rust](https://github.com/a-s-l-a-h/stratum-rust) repo. It documents the Java-to-Rust mapping and rules, and you can hand it to an LLM to generate app code.

## Status

Pre-1.0. Only API level 35 is generated by default.

## License

MIT, see `LICENSE`.g