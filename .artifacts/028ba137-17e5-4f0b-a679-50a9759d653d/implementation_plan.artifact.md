# Fix Native Crash on Rotation in PdfRendererCore

The app crashes with a `SIGSEGV` when the device is rotated. The crash happens in `PdfRendererCore.renderPage`. This is a race condition between `PdfRenderer.close()` (called when the activity is recreated or the view is removed) and an active `renderPage` operation on a background thread.

## Proposed Changes

### [Utils]

#### [MODIFY] [PdfRendererCore.kt](file:///D:/apps/scorda/app/src/main/java/com/example/scorda/util/PdfRendererCore.kt)
- **Synchronize `close()`**: Update `close()` to acquire the same `Mutex` used by `renderPage` and `getPageDimensions`. This ensures that the native `PdfRenderer` is not closed while it is actively being used to render a page.
- **Use `runBlocking` in `close()`**: Since `close()` is a non-suspend function (required by `AutoCloseable`), use `runBlocking` to wait for the mutex. This is safe in this context as it only blocks the thread calling `close` (usually the main thread during cleanup) until the current (already cancelled but still running) render operation finishes.
- **Cache `pageCount`**: Store the `pageCount` during initialization. This prevents calling the native `renderer.pageCount` method after the renderer might have been closed, which is another common source of native crashes.
- **Double-check `isClosed`**: Ensure `isClosed` is checked both before and after acquiring the lock in all methods.

## Verification Plan

### Manual Verification
- Deploy the app to a device or emulator.
- Open a score.
- Rapidly rotate the device multiple times.
- Verify that the app no longer crashes with a `SIGSEGV`.
- Verify that PDF pages still render correctly after rotation.
