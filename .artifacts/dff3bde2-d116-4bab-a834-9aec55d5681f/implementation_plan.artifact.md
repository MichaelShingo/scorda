# Implementation Plan: Integrating AndroidX Ink API (`ink-geometry`, `ink-strokes`, `ink-rendering`, etc.)

Upgrade Scorda's annotation system from basic Compose `Path` drawing to Google's official **AndroidX Ink API**. This will enable **stylus pressure sensitivity**, realistic brush engines (Pressure Pen, Marker, Highlighter), hardware-accelerated smooth rendering, high-precision geometric intersection testing for erasers, and compact protobuf-based binary stroke storage.

---

## User Review Required

> [!IMPORTANT]
> **Dependency Additions**: We will add the `androidx.ink` suite (`ink-geometry`, `ink-strokes`, `ink-brush`, `ink-rendering`, `ink-authoring`, `ink-storage`) to `libs.versions.toml` and `app/build.gradle.kts`.

> [!NOTE]
> **Database Entity Update**: `Stroke` will be updated to store `inputs` (`ByteArray` representing encoded `StrokeInputBatch` from `ink-storage`) along with brush metadata (color, size, stock brush type). This natively supports pressure, speed, tilt, and high-resolution input coordinates.

---

## Proposed Architectural Changes

```
+-------------------------------------------------------------------------+
|                              DrawingCanvas                              |
|  +-------------------------------+   +-------------------------------+  |
|  |     Dry Ink Canvas            |   |     InProgressStrokes (Wet)   |  |
|  |  (CanvasStrokeRenderer.draw)  |   |  (Real-time stylus/touch input)  |  |
|  +-------------------------------+   +-------------------------------+  |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                          AnnotationViewModel                            |
|             (Manages Brushes, Active Layer, Target Pages)               |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                  Room Database / Storage (`ink-storage`)                 |
|   `StrokeInputBatch.encode()` / `decode()` <--> `ByteArray` in SQLite   |
+-------------------------------------------------------------------------+
```

### 1. Dependencies & Version Catalog

#### [MODIFY] [libs.versions.toml](file:///D:/apps/scorda/gradle/libs.versions.toml)
- Add version `androidx-ink = "1.0.0"` (or `1.1.0-alpha07`).
- Define libraries:
  - `androidx-ink-geometry = { group = "androidx.ink", name = "ink-geometry", version.ref = "androidx-ink" }`
  - `androidx-ink-strokes = { group = "androidx.ink", name = "ink-strokes", version.ref = "androidx-ink" }`
  - `androidx-ink-brush = { group = "androidx.ink", name = "ink-brush", version.ref = "androidx-ink" }`
  - `androidx-ink-rendering = { group = "androidx.ink", name = "ink-rendering", version.ref = "androidx-ink" }`
  - `androidx-ink-authoring = { group = "androidx.ink", name = "ink-authoring-compose", version.ref = "androidx-ink" }`
  - `androidx-ink-storage = { group = "androidx.ink", name = "ink-storage", version.ref = "androidx-ink" }`

#### [MODIFY] [build.gradle.kts](file:///D:/apps/scorda/app/build.gradle.kts)
- Include the new `androidx.ink` dependencies.

---

### 2. Data Models & Database Migration

#### [MODIFY] [Stroke.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/database/entities/Stroke.kt)
- Update `Stroke` to store `inputs: ByteArray` (encoded `StrokeInputBatch`) instead of `List<AnnotationPoint>`.
- Add brush metadata fields: `brushFamily` (e.g. `PRESSURE_PEN`, `MARKER`, `HIGHLIGHTER`), `color: Long`, `size: Float`, `epsilon: Float`.
- Retain `id`, `scoreId`, `layerId`, `pageIndex`, `createdAt`.

#### [MODIFY] [Converters.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/database/Converters.kt)
- Remove manual `AnnotationPoint` byte buffer serialization.
- Add serialization helpers using `ink-storage` (`StrokeInputBatch.encode()` / `StrokeInputBatch.decode()`) and `Brush` mapping.

#### [NEW] [InkConverters.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/database/InkConverters.kt)
- Provide utility extensions to convert between Ink API `Stroke` / `Brush` objects and Room `Stroke` entity DTOs.

---

### 3. Rendering & Drawing Component Integration

#### [MODIFY] [DrawingCanvas.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/components/organisms/scoreView/DrawingCanvas.kt)
- **Dry Ink Layer**: Use `CanvasStrokeRenderer.create()` to render completed `androidx.ink.strokes.Stroke` objects onto the Compose `Canvas` with high visual fidelity and antialiasing.
- **Wet Ink Layer**: Integrate `InProgressStrokes` for real-time stroke authoring with ultra-low latency and full pressure/stylus support.
- **Coordinate Transformation**: Use `pointerEventToWorldTransform` matrix to map screen touch/stylus inputs to normalized PDF page coordinates.
- **Eraser Logic using `ink-geometry`**:
  - Replace point distance threshold loops with `ink-geometry` intersection testing (`stroke.shape.intersects(parallelogram, AffineTransform.IDENTITY)` or `Segment`).

#### [MODIFY] [Brush.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/database/entities/Brush.kt)
- Map internal `Brush` domain model to `androidx.ink.brush.StockBrushes` (`pressurePen()`, `marker()`, `highlighter()`, `dashedLine()`).

---

### 4. ViewModel & Repository Updates

#### [MODIFY] [AnnotationRepository.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/repository/AnnotationRepository.kt)
- Adapt repository methods to pass `StrokeInputBatch` or Ink `Stroke` objects seamlessly.

#### [MODIFY] [AnnotationViewModel.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/viewmodel/AnnotationViewModel.kt)
- Expose `BrushFamily` options (Pressure Pen, Marker, Highlighter) to the UI toolbar.
- Maintain cached/deserialized `androidx.ink.strokes.Stroke` objects for active pages to keep rendering ultra-fast.

---

## Verification Plan

### Automated Build & Compilation
- Run `./gradlew assembleDebug` to verify dependency resolution and Kotlin build success.

### Manual & Visual Verification
1. **Stylus Pressure Testing**: Test drawing with varying pressure on a stylus (or simulated input) to verify line width changes dynamically.
2. **Brush Types**: Verify Pressure Pen, Marker, and Highlighter rendering styles.
3. **Eraser Accuracy**: Test eraser stroke removal using `ink-geometry` intersection testing.
4. **Persistence Test**: Draw strokes, change scores, reopen the score, and verify strokes render identically with exact pressure profiles restored from SQLite (`ink-storage`).
