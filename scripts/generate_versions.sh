#!/bin/bash

# Overwrite versions.properties at the beginning
: > versions.properties

# Define the prefixes
prefixes=("v")

# Get the latest commit hash
hash=$(git rev-parse --short HEAD)

# Check if the working directory is dirty
git diff --quiet || dirty=true
dirty=${dirty:-false}

# Determine if we're in release mode
release_mode=false
if [[ $1 == "--release" ]]; then
    release_mode=true
fi

# Loop over the prefixes
for prefix in "${prefixes[@]}"; do

    # Get the latest git tag from current branch that matches the current prefix
    tag=$(git describe --tags --match "${prefix}[0-9]*" --abbrev=0)

    # Trim the prefix from the start of the tag name
    version="${tag:1}"

    # Split the version into major, minor, and patch parts
    IFS='.' read -r -a version_parts <<< "$version"
    major="${version_parts[0]}"
    minor="${version_parts[1]}"
    patch="${version_parts[2]}"

    # Calculate the version code using arithmetic expansion
    version_code=$((major * 1000000 + minor * 1000 + patch * 10))

    # Get the number of commits since the tag
    commit_count=$(git rev-list --count "${tag}"..HEAD)

    # Create the debug version string and append -dirty if necessary
    debug_version="${version}-${commit_count}-g${hash}"
    debug_version_suffix="${commit_count}-g${hash}"
    [[ $dirty == true ]] && debug_version+="-dirty"
    [[ $dirty == true ]] && debug_version_suffix+="-dirty"

    # Convert prefix to uppercase for property names
    prefix_upper=$(echo "$prefix" | tr '[:lower:]' '[:upper:]')

    # Derive library version from lib/* tags
    lib_tag=$(git describe --tags --match "lib/*" --abbrev=0 2>/dev/null || echo "lib/0.0.0")
    lib_describe=$(git describe --tags --match "lib/*" 2>/dev/null || echo "lib/0.0.0-0-g0000000")
    library_version="${lib_tag#lib/}"

    # Split library version into parts
    IFS='.' read -r -a lib_version_parts <<< "$library_version"
    lib_major="${lib_version_parts[0]}"
    lib_minor="${lib_version_parts[1]}"
    lib_patch="${lib_version_parts[2]}"

    # Determine commit distance from last lib tag
    if [[ "$lib_describe" == "$lib_tag" ]]; then
        lib_commit_distance=0
    else
        lib_commit_distance=$(echo "$lib_describe" | sed -E 's/^lib\/[0-9]+\.[0-9]+\.[0-9]+-([0-9]+)-g.*/\1/')
    fi

    if [[ $release_mode == true ]]; then
        : # Use version as-is from tag
    elif [[ $lib_commit_distance -gt 0 ]]; then
        # Ahead of last release — bump patch for snapshot
        lib_patch=$((lib_patch + 1))
        library_version="${lib_major}.${lib_minor}.${lib_patch}-SNAPSHOT"
    else
        library_version="${library_version}-SNAPSHOT"
    fi

    # Print the result
    printf "%s_VERSION=%s\n" "$prefix_upper" "$version"
    printf "%s_DEBUG_VERSION=%s\n" "$prefix_upper" "$debug_version"
    printf "%s_DEBUG_VERSION_SUFFIX=%s\n" "$prefix_upper" "$debug_version_suffix"
    printf "%s_VERSION_CODE=%d\n" "$prefix_upper" "$version_code"
    printf "%s_LIBRARY_VERSION=%s\n" "$prefix_upper" "$library_version"

    # Write to versions.properties
    {
        printf "%s_VERSION=%s\n" "$prefix_upper" "$version"
        printf "%s_VERSION_CODE=%d\n" "$prefix_upper" "$version_code"
        printf "%s_DEBUG_VERSION_SUFFIX=%s\n" "$prefix_upper" "$debug_version_suffix"
        printf "%s_DEBUG_VERSION=%s\n" "$prefix_upper" "$debug_version"
        printf "%s_LIBRARY_VERSION=%s\n" "$prefix_upper" "$library_version"
    } >> versions.properties

done

# Print more result
printf "HASH=%s\n" "$hash"
printf "DIRTY=%s\n" "$dirty"

# Write the hash to versions.properties after the loop
printf "HASH=%s\n" "$hash" >> versions.properties
printf "DIRTY=%s\n" "$dirty" >> versions.properties
