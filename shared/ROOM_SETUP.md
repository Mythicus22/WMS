Room setup notes

This file documents Phase 0 configuration for Room (prepare only — no entities or database classes yet).

Recommended approach when implementing Room KMP or Room for Android:

- For Android-only Room (classic Room):
  - Add dependencies in the Android module or shared androidMain as needed:
    - implementation("androidx.room:room-runtime:2.6.1")
    - implementation("androidx.room:room-ktx:2.6.1")
    - kapt("androidx.room:room-compiler:2.6.1")
  - Use expect/actual or interfaces in shared module to abstract DAOs and database access so business logic remains in shared.

- For Multiplatform solutions:
  - Consider SQLDelight or other KMP-friendly libraries for cross-platform persistent storage.
  - Room KMP experimental ports may be used, but evaluate maturity.

Phase 0:
- Added version catalog entries for Room in `gradle/libs.versions.toml`.
- Created this document as guidance for future implementation.
- No entities or DAOs implemented at this phase.

