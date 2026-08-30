# Performance Optimization for Annotations

This plan aims to optimize the annotation system (strokes) to handle large datasets (10M+ total strokes, 1000+ per score) efficiently. The current implementation suffers from CPU-intensive JSON serialization and memory pressure due to eager loading of all strokes in a score.

## User Review Required

> [!IMPORTANT]
> This optimization involves a database schema change. While we will aim for a clean migration, it's a significant change to the persistence layer.

> [!NOTE]
> We will switch from JSON serialization to a compact binary format for stroke points. This will break compatibility with existing data unless a migration script is implemented to convert old JSON strings to the new binary format.

## Open Questions

1. Do we need to preserve existing annotations during this migration? If so, I will include a data migration script. Given "production quality", I assume YES.

## Proposed Changes

### Data Layer

#### [MODIFY] [Stroke.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/database/entities/Stroke.kt)
- Add `scoreId: Long` field for direct querying.
- Update indices to include `(scoreId, pageIndex)` for optimized per-page lookups.

#### [NEW] [PointConverter.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/database/PointConverter.kt)
- Implement a binary `TypeConverter` for `List<AnnotationPoint>`.
- Use `ByteBuffer` to store coordinates as floats, significantly reducing parsing time and string overhead.

#### [MODIFY] [Converters.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/database/Converters.kt)
- Remove `fromPoints` and `toPoints` JSON converters (or keep them as fallback if migrating).

#### [MODIFY] [AnnotationDao.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/database/dao/AnnotationDao.kt)
- Update queries to use the new `scoreId` column.
- Optimize `getVisibleStrokesForScore` and `getVisibleStrokesForPage` to use direct joins instead of subqueries with `IN`.

### Repository & ViewModel Layer

#### [MODIFY] [AnnotationRepository.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/data/repository/AnnotationRepository.kt)
- Update `insertStroke` and observation methods to support `scoreId`.

#### [MODIFY] [AnnotationViewModel.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/ui/viewmodel/AnnotationViewModel.kt)
- **Lazy Loading**: Refactor `uiState` to observe only the currently visible pages rather than the entire score.
- **Grouping Optimization**: Move `groupBy { it.pageIndex }` logic to a background thread or optimize it to run only on the relevant "window" of pages.

---

## Verification Plan

### Automated Tests
- **Benchmark**: Create a test that compares JSON vs Binary serialization for 10,000 points.
- **Migration Test**: Verify that existing JSON-based strokes are correctly converted to the new format.
- **DAO Test**: Verify that per-page stroke retrieval is fast even with 100k total rows.

### Manual Verification
- Stress test with 1,000 strokes on a single page.
- Monitor memory usage and "jank" using the Android Studio Profiler during drawing and page flipping.
- Verify that "Undo" and "Clear Layer" still work correctly with the new schema.
