# Implementation Plan - Replace Hardcoded Dependency with Version Catalog Syntax

The user wants to replace the hardcoded dependency `implementation("androidx.compose.foundation:foundation:1.11.4")` in `app/build.gradle.kts` with the Gradle Version Catalog (`libs.versions.toml`) syntax.

## Proposed Changes

### Version Catalog

#### [MODIFY] [libs.versions.toml](file:///D:/apps/scorda/gradle/libs.versions.toml)
- Add a new version entry for `composeFoundation` (if we want to keep the specific version 1.11.4) or use the Compose BOM.
- Add `androidx-compose-foundation` to the `[libraries]` section.

Since the project already uses a Compose BOM (`2026.02.01`), I will add the library without a version to let the BOM manage it, which is the recommended practice for Compose libraries. If a specific version is required to override the BOM, I will add it accordingly.

### App Module

#### [MODIFY] [build.gradle.kts](file:///D:/apps/scorda/app/build.gradle.kts)
- Replace `implementation("androidx.compose.foundation:foundation:1.11.4")` with `implementation(libs.androidx.compose.foundation)`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project still builds correctly with the new dependency syntax.
