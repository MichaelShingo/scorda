# Enable Destructuring for Bitmap

The user wants to destructure a `Bitmap` object to retrieve its width and height (e.g., `val (width, height) = bitmap`). Since `Bitmap` does not natively support destructuring in Kotlin, we need to provide extension functions for `component1()` and `component2()`.

## Proposed Changes

### Utilities

#### [NEW] [BitmapExtensions.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/util/BitmapExtensions.kt)
- Add operator extension functions `component1()` and `component2()` to `android.graphics.Bitmap`.

## Verification Plan

### Manual Verification
- Verify that the error in `ZoomablePdfPage.kt` is resolved after importing the new extension functions.
- Build the project to ensure no regressions.
