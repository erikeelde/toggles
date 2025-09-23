# Toggles - Android Feature Switching Library

Toggles is a multi-module Android project providing feature switching capabilities through content providers and various client libraries.

**Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Working Effectively

### Prerequisites and Setup
**CRITICAL**: This project requires specific environment setup. Follow these steps exactly:

1. **Install Java 21** (required - project will not build with older versions):
   ```bash
   sudo apt update && sudo apt install -y openjdk-21-jdk
   sudo update-alternatives --config java  # Select Java 21
   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
   ```

2. **Install Android SDK** (required for Android project):
   ```bash
   # Method 1: Using Android Studio (recommended)
   # Download and install Android Studio, then use SDK Manager
   
   # Method 2: Command line tools (if Android Studio not available)
   wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
   unzip commandlinetools-linux-9477386_latest.zip
   mkdir -p ~/android-sdk/cmdline-tools/latest
   mv cmdline-tools/* ~/android-sdk/cmdline-tools/latest/
   export ANDROID_HOME=~/android-sdk
   export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
   
   # Accept licenses and install required components
   yes | sdkmanager --licenses
   sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
   
   # Required environment variables (add to ~/.bashrc or ~/.profile)
   export ANDROID_HOME=~/android-sdk
   export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
   ```

3. **Verify setup**:
   ```bash
   java -version  # Should show Java 21
   ./gradlew --version  # Should work without errors
   ```

4. **First Build Setup** (one-time only):
   The first build will download many dependencies and may take 15-25 minutes. This is normal.
   ```bash
   ./gradlew build --no-daemon  # First build - takes 15-25 minutes. NEVER CANCEL. Set timeout to 45+ minutes.
   ```
   After the first successful build, subsequent builds will be much faster (2-8 minutes).

### Build Commands and Timing

**CRITICAL TIMING INFORMATION - NEVER CANCEL THESE COMMANDS:**

1. **Initial setup** (first time only):
   ```bash
   ./gradlew --version  # Downloads Gradle wrapper - takes 2-3 minutes. NEVER CANCEL.
   ```

2. **Code quality checks**:
   ```bash
   ./gradlew detekt  # Static analysis - takes 2-5 minutes. NEVER CANCEL. Set timeout to 10+ minutes.
   ./gradlew versionCatalogFormat  # Version catalog formatting - takes 30 seconds
   ./gradlew detektMain --no-configuration-cache  # Main source detekt - takes 1-2 minutes
   ```

3. **Build and test**:
   ```bash
   ./gradlew test  # Unit tests - takes 3-7 minutes. NEVER CANCEL. Set timeout to 15+ minutes.
   ./gradlew assembleAndroidTest  # Android test APK - takes 5-10 minutes. NEVER CANCEL. Set timeout to 20+ minutes.
   ./gradlew check  # All checks - takes 8-15 minutes. NEVER CANCEL. Set timeout to 30+ minutes.
   ```

4. **Module-specific checks**:
   ```bash
   ./gradlew :toggles-core:check  # Core library - takes 1-2 minutes
   ./gradlew :toggles-flow:check --no-configuration-cache  # Flow library - takes 2-3 minutes
   ./gradlew :toggles-flow-noop:check  # Flow noop - takes 1 minute
   ./gradlew :toggles-prefs:check --no-configuration-cache  # Prefs library - takes 2-3 minutes
   ./gradlew :toggles-prefs-noop:check  # Prefs noop - takes 1 minute
   ```

5. **Android instrumentation tests**:
   ```bash
   ./gradlew pixel6api35googleDebugAndroidTest  # Emulator tests - takes 10-20 minutes. NEVER CANCEL. Set timeout to 45+ minutes.
   ```

6. **Complete PR check** (runs all validations):
   ```bash
   ./scripts/pr_check.sh  # Full PR validation - takes 15-30 minutes. NEVER CANCEL. Set timeout to 60+ minutes.
   ```

### Network Requirements
**CRITICAL**: This project requires internet access for initial setup and builds:

- **dl.google.com**: Required for Android build tools and Google services (AGP 8.12.1, Google Services 4.4.3)
- **services.gradle.org**: Required for Gradle wrapper download (Gradle 9.0.0)
- **repo1.maven.org**: Required for Maven Central dependencies
- **plugins.gradle.org**: Required for Gradle plugins

**Without internet access, the project cannot build.** If you see errors like:
- "dl.google.com: No address associated with hostname"
- "Could not resolve com.android.tools.build:gradle"
- "No cached version available for offline mode"

This indicates network connectivity issues. Ensure all the above domains are accessible.

## Validation

### Build Validation
After making changes, ALWAYS run these validation steps in order:

1. **Quick validation** (3-5 minutes):
   ```bash
   ./gradlew detekt versionCatalogFormat
   ```

2. **Medium validation** (10-15 minutes):
   ```bash
   ./gradlew test check
   ```

3. **Full validation** (20-45 minutes):
   ```bash
   ./scripts/pr_check.sh
   ```

### Manual Testing Scenarios
**CRITICAL**: After making any code changes, always validate with actual running applications:

1. **Toggles App** (`toggles-app`) - Main feature toggle management:
   ```bash
   # Build and install the main app
   ./gradlew :toggles-app:assembleDebug  # Takes 3-5 minutes
   adb install toggles-app/build/outputs/apk/debug/toggles-app-debug.apk
   
   # Manual validation steps:
   # - Launch app on device/emulator
   # - Verify app starts without crashes (should show "No applications found" initially)
   # - Navigate through main screens
   # - Test adding/removing toggles if possible
   # - Verify content provider is accessible by other apps
   ```

2. **Toggles Sample** (`toggles-sample`) - Library integration example:
   ```bash
   # Build and install the sample app
   ./gradlew :toggles-sample:assembleDebug  # Takes 2-3 minutes
   adb install toggles-sample/build/outputs/apk/debug/toggles-sample-debug.apk
   
   # Manual validation steps:
   # - Launch sample app on device/emulator
   # - Test toggle reading functionality
   # - Verify different library APIs work (flow, prefs)
   # - Check license information display
   # - Test integration with main toggles app
   ```

3. **Library API Testing** - Validate library interfaces:
   ```bash
   # Test that libraries build correctly
   ./gradlew :toggles-core:assembleDebug :toggles-flow:assembleDebug :toggles-prefs:assembleDebug
   
   # Verify library artifacts are created
   ls -la toggles-*/build/outputs/aar/
   
   # Test publishing to local repository
   ./gradlew publishToMavenLocal  # Takes 2-3 minutes
   ls ~/.m2/repository/se/eelde/toggles/
   ```

4. **End-to-End Scenario** (most comprehensive):
   ```bash
   # Install both apps
   ./gradlew assembleDebug  # Build all debug variants - takes 5-8 minutes
   adb install toggles-app/build/outputs/apk/debug/toggles-app-debug.apk
   adb install toggles-sample/build/outputs/apk/debug/toggles-sample-debug.apk
   
   # Test scenario:
   # 1. Open Toggles app, create a feature toggle
   # 2. Open Sample app, verify it can read the toggle
   # 3. Change toggle value in Toggles app
   # 4. Verify Sample app reflects the change (may require restart)
   ```

### CI/CD Integration
The project uses GitHub Actions for CI/CD:
- **Pull requests**: Run checks and emulator tests (`.github/workflows/pull-request.yml`)
- **Releases**: Automated app and library publishing

Always ensure your changes pass the same checks that CI runs:
```bash
./scripts/pr_check.sh  # This mirrors the CI checks
```

## Project Structure

### Repository Overview
```
toggles/
├── .github/                 # GitHub Actions, workflows, and copilot instructions
├── build-logic/            # Custom Gradle conventions and plugins
├── config/                 # Configuration files (detekt, etc.)
├── gradle/                 # Gradle wrapper and version catalog
├── modules/                # Internal feature modules
│   ├── applications/       # Application listing module
│   ├── booleanconfiguration/ # Boolean toggle configuration
│   ├── compose-theme/      # UI theming
│   ├── configurations/     # Configuration management
│   ├── database/          # Database API, implementation, wiring
│   ├── enumconfiguration/ # Enum toggle configuration
│   ├── help/              # Help and documentation
│   ├── integerconfiguration/ # Integer toggle configuration
│   ├── oss/               # Open source licenses
│   ├── provider/          # Content provider API, implementation, wiring
│   ├── routes/            # Navigation routing
│   └── stringconfiguration/ # String toggle configuration
├── scripts/               # Build and utility scripts
├── toggles-app/           # Main Android application
├── toggles-core/          # Core library (published to Maven Central)
├── toggles-flow/          # Kotlin Flow library (published to Maven Central)
├── toggles-flow-noop/     # No-op Flow implementation
├── toggles-prefs/         # SharedPreferences-like API (published to Maven Central)
├── toggles-prefs-noop/    # No-op Prefs implementation
└── toggles-sample/        # Sample application demonstrating library usage
```

### Key Modules
- **`toggles-app/`**: Main Android application for managing feature toggles
- **`toggles-sample/`**: Sample application demonstrating library usage
- **`toggles-core/`**: Core library with common functionality
- **`toggles-flow/`**: Kotlin Flow-based toggle access
- **`toggles-prefs/`**: SharedPreferences-like API for toggles
- **`toggles-*-noop/`**: No-op implementations for testing/staging
- **`modules/`**: Additional internal modules (database, provider, UI components)

### Important Files
- **`scripts/pr_check.sh`**: Complete validation script (mirrors CI)
- **`gradle/libs.versions.toml`**: Version catalog for dependencies
- **`build-logic/`**: Custom Gradle plugins and conventions
- **`.github/workflows/`**: CI/CD pipelines

### Configuration Files
- **`gradle.properties`**: Project-wide Gradle configuration
- **`config/detekt/detekt.yml`**: Static analysis rules
- **`.github/ci-gradle.properties`**: CI-specific Gradle settings

## Common Tasks

### Building APKs
```bash
# Debug APKs
./gradlew assembleDebug  # All debug APKs - takes 5-8 minutes. Set timeout to 15+ minutes.

# Release APKs (requires signing configuration)
./gradlew assembleRelease  # All release APKs - takes 7-12 minutes. Set timeout to 20+ minutes.
```

### Running Tests
```bash
# Unit tests only
./gradlew testDebugUnitTest  # Debug unit tests - takes 2-4 minutes

# Android instrumentation tests (requires emulator/device)
./gradlew connectedDebugAndroidTest  # Connected tests - takes 8-15 minutes. Set timeout to 30+ minutes.
```

### Code Quality
```bash
# Format code
./gradlew versionCatalogFormat  # Format version catalog

# Static analysis
./gradlew detekt  # Full detekt analysis - takes 2-5 minutes. Set timeout to 10+ minutes.
./gradlew detektMain  # Main source only
```

### Library Publishing
```bash
# Publish to local repository
./gradlew publishToMavenLocal  # Local publication - takes 3-5 minutes

# Check publication readiness
./gradlew :toggles-core:check :toggles-flow:check :toggles-prefs:check  # Library checks - takes 5-8 minutes
```

## Troubleshooting

### Common Issues

1. **"Dependency requires at least JVM runtime version 21"**
   - Ensure Java 21 is installed and active
   - Set `JAVA_HOME` environment variable correctly
   - Verify with: `java -version` and `echo $JAVA_HOME`

2. **"dl.google.com: No address associated with hostname"**
   - Network connectivity issue - project requires internet access
   - Ensure access to Google Maven repository (dl.google.com)
   - Cannot build in fully offline environments

3. **Android SDK not found**
   - Install Android SDK using command line tools or Android Studio
   - Set `ANDROID_HOME` environment variable: `export ANDROID_HOME=~/android-sdk`
   - Verify with: `echo $ANDROID_HOME` and `ls $ANDROID_HOME`

4. **Emulator tests failing**
   - Ensure Android emulator is available and running
   - Check that pixel2api35 emulator image is installed: `avdmanager list avd`
   - Start emulator manually if needed: `emulator -avd <avd_name>`

5. **Configuration cache issues**
   - Some tasks use `--no-configuration-cache` flag for compatibility
   - If cache corruption occurs: `./gradlew clean --no-configuration-cache`
   - Clear gradle cache: `rm -rf ~/.gradle/caches/`

6. **"Could not resolve" dependency errors**
   - Usually indicates network issues or missing repositories
   - Check internet connectivity to required domains
   - Try cleaning and rebuilding: `./gradlew clean build`

7. **OutOfMemoryError during build**
   - Gradle is configured for 12GB heap (see gradle.properties)
   - If running on smaller machines, reduce: `org.gradle.jvmargs=-Xmx8g -Xms500m`

### Performance Notes
- **First build**: Takes 15-25 minutes due to dependency downloads
- **Incremental builds**: Take 2-8 minutes depending on changes
- **Clean builds**: Take 10-18 minutes
- **Full test suite**: Takes 15-35 minutes including emulator tests

### Working in Restricted Networks
If you encounter network connectivity issues (cannot access dl.google.com, etc.):

1. **This project cannot build in fully offline environments** - it requires internet access
2. **Document the limitation**: Note that builds will fail with network restrictions
3. **Alternative approach**: If you must work offline, document the commands that would work once network is available
4. **Use mobile hotspot or VPN**: If corporate network blocks Google domains

Example documentation for restricted environments:
```markdown
## Network Restriction Note
This project requires internet access to:
- dl.google.com (Android build tools)
- repo1.maven.org (Maven Central)
- services.gradle.org (Gradle wrapper)

Without access to these domains, builds will fail with:
"dl.google.com: No address associated with hostname"

Commands that WOULD work with proper network access:
- ./gradlew build (first build: 15-25 minutes)
- ./gradlew test (unit tests: 3-7 minutes)
- ./scripts/pr_check.sh (full validation: 15-30 minutes)
```

**REMEMBER: NEVER CANCEL long-running builds or tests. Set generous timeouts and wait for completion.**

## Expected Command Outputs

### Successful Setup
```bash
$ java -version
openjdk version "21.0.8" 2025-07-15
OpenJDK Runtime Environment (build 21.0.8+9-Ubuntu-0ubuntu124.04.1)
OpenJDK 64-Bit Server VM (build 21.0.8+9-Ubuntu-0ubuntu124.04.1, mixed mode, sharing)

$ ./gradlew --version
------------------------------------------------------------
Gradle 9.0.0
------------------------------------------------------------
Build time:    2025-07-31 16:35:12 UTC
Kotlin:        2.2.0
Launcher JVM:  21.0.8 (Ubuntu 21.0.8+9-Ubuntu-0ubuntu124.04.1)
```

### Successful Build Output
```bash
$ ./gradlew build
...
BUILD SUCCESSFUL in 8m 23s
XXX actionable tasks: XXX executed, XXX up-to-date
```

### Successful Test Output
```bash
$ ./gradlew test
...
BUILD SUCCESSFUL in 4m 15s
XXX actionable tasks: XXX executed, XXX up-to-date
```

### Common Failure Indicators
- `BUILD FAILED` - indicates compilation or test failures
- `Could not resolve` - dependency resolution issues (usually network)
- `No address associated with hostname` - network connectivity problems
- `Dependency requires at least JVM runtime version 21` - wrong Java version