#!/bin/bash

# Set the Gradle version you want to update to
GRADLE_VERSION="${1:-8.6}"

# Save the root directory
ROOT_DIR=$(pwd)

echo "🔍 Searching for Gradle projects to update to version $GRADLE_VERSION..."

# Find all gradlew files (symbolizing root of Gradle projects)
find . -type f -name "gradlew" | while read -r gradlew_path; do
  project_dir=$(dirname "$gradlew_path")
  echo "📁 Updating wrapper in: $project_dir"

  cd "$project_dir" || continue

  chmod +x ./gradlew

  ./gradlew wrapper --gradle-version "$GRADLE_VERSION"

  echo "✅ Updated in $project_dir"
  echo "---------------------------------------"

  cd "$ROOT_DIR" || exit
done

echo "🎉 All Gradle wrappers updated."