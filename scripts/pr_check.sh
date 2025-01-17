#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cd "$SCRIPT_DIR"/.. || exit

./gradlew detekt
./gradlew versionCatalogFormat
./gradlew test
./gradlew assembleAndroidTest
./gradlew check
./gradlew :toggles-core:check
./gradlew :toggles-flow:check --no-configuration-cache
./gradlew :toggles-flow-noop:check
./gradlew :toggles-prefs:check --no-configuration-cache
./gradlew :toggles-prefs-noop:check
