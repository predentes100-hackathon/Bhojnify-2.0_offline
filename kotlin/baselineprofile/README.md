# Bhojnify Baseline Profile Module

This module generates baseline profiles for Bhojnify using AndroidX Benchmark Macro (`androidx.benchmark.macro.junit4.BaselineProfileRule`).

## Capabilities Covered in Generator
- Cold startup to Home screen
- Tab navigation across Home, Customers, Admin, and Profile
- LazyColumn scrolling through the Customer List
- Warm restart and backstack restoration

## How to Run (on connected device or emulator)
```bash
./gradlew :baselineprofile:generateBaselineProfile
```

Once generated, the profile is automatically packaged into the release APK to eliminate JIT compilation overhead during startup and critical user journeys.
