#!/bin/bash

# Interactive PR check script for Toggles project
# Usage: ./pr_check.sh
# On startup, select which suites to run by toggling with 1/2/3/4 and Enter. Press Enter with no input to start.

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR"/.. || exit

run_static=true
run_unit=true
run_check=true
run_instrumentation=false

show_menu() {
  echo "Select test suites to run (toggle by entering number, press Enter to start):"
  echo "  1. Static checks         [$(if $run_static; then echo 'x'; else echo ' '; fi)]"
  echo "  2. Unit tests           [$(if $run_unit; then echo 'x'; else echo ' '; fi)]"
  echo "  3. Check                [$(if $run_check; then echo 'x'; else echo ' '; fi)]"
  echo "  4. Instrumentation tests [$(if $run_instrumentation; then echo 'x'; else echo ' '; fi)]"
}

while true; do
  show_menu
  read -r -p "Toggle (1/2/3/4) or Enter to start: " choice
  case "$choice" in
    "1")
      run_static=$([ "$run_static" = true ] && echo false || echo true)
      ;;
    "2")
      run_unit=$([ "$run_unit" = true ] && echo false || echo true)
      ;;
    "3")
      run_check=$([ "$run_check" = true ] && echo false || echo true)
      ;;
    "4")
      run_instrumentation=$([ "$run_instrumentation" = true ] && echo false || echo true)
      ;;
    "")
      break
      ;;
    *)
      echo "Invalid input. Enter 1, 2, 3, 4, or just Enter to start."
      ;;
  esac
  echo
done

if ! $run_static && ! $run_unit && ! $run_check && ! $run_instrumentation; then
  echo "No suites selected. Exiting."
  exit 0
fi

if $run_static; then
  echo "Running static checks..."
  ./gradlew assembleDebug
  ./gradlew assembleAndroidTest
  ./gradlew detekt
  ./gradlew versionCatalogFormat
  ./gradlew detektMain
fi

if $run_unit; then
  echo "Running unit tests..."
  ./gradlew test

fi

if $run_check; then
  echo "Running check suite..."
  ./gradlew check
  ./gradlew :toggles-core:check
  ./gradlew :toggles-flow:check
  ./gradlew :toggles-flow-noop:check
  ./gradlew :toggles-prefs:check
  ./gradlew :toggles-prefs-noop:check
fi

if $run_instrumentation; then
  echo "Running instrumentation tests..."
  ./gradlew pixel6api35googleDebugAndroidTest
fi
