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

# Derive library version from lib/* tags (done once, outside the app version loop)
lib_tag_found=false
if lib_tag=$(git describe --tags --match "lib/*" --abbrev=0 2>/dev/null); then
    lib_tag_found=true
fi
library_version="${lib_tag#lib/}"

if [[ $lib_tag_found == true ]]; then
    # Validate library version is strict semver (x.y.z)
    if [[ ! "$library_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        echo "Error: lib tag '$lib_tag' is not valid semver (expected lib/x.y.z)" >&2
        exit 1
    fi

    # Split library version into parts
    old_ifs="$IFS"
    IFS='.' read -r -a lib_version_parts <<< "$library_version"
    IFS="$old_ifs"
    lib_major="${lib_version_parts[0]}"
    lib_minor="${lib_version_parts[1]}"
    lib_patch="${lib_version_parts[2]}"

    lib_commit_distance=$(git rev-list --count "${lib_tag}"..HEAD)
else
    library_version="0.0.0"
    lib_major=0
    lib_minor=0
    lib_patch=0
    lib_commit_distance=0
fi

if [[ $release_mode == true ]]; then
    if [[ $lib_tag_found == false ]]; then
        echo "Error: --release requires a lib/* tag but none was found" >&2
        exit 1
    fi
    if [[ $lib_commit_distance -ne 0 ]]; then
        echo "Error: --release but HEAD is $lib_commit_distance commit(s) ahead of $lib_tag — tag HEAD first" >&2
        exit 1
    fi
elif [[ $lib_commit_distance -gt 0 ]]; then
    # Ahead of last release — bump patch for snapshot
    lib_patch=$((lib_patch + 1))
    library_version="${lib_major}.${lib_minor}.${lib_patch}-SNAPSHOT"
else
    library_version="${library_version}-SNAPSHOT"
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
