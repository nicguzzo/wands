#!/bin/bash

SETTINGS_FILE="settings.gradle.kts"

if [[ ! -f "$SETTINGS_FILE" ]]; then
    echo "Error: $SETTINGS_FILE not found in the current directory."
    exit 1
fi

targets=()

# Use a variable for the regex to prevent bash syntax errors with parentheses
regex='mc\("([^"]+)".*loaders[[:space:]]*=[[:space:]]*listOf\(([^)]+)\)'

while IFS= read -r line; do
    if [[ "$line" =~ $regex ]]; then
        version="${BASH_REMATCH[1]}"
        loaders_raw="${BASH_REMATCH[2]}"
        IFS=',' read -ra loaders_arr <<< "$loaders_raw"
        for loader in "${loaders_arr[@]}"; do
            # Clean up quotes and whitespace
            clean_loader=$(echo "$loader" | tr -d '"' | tr -d ' ' | tr -d '\r')
            if [[ -n "$clean_loader" ]]; then
                targets+=("$version-$clean_loader")
            fi
        done
    fi
done < "$SETTINGS_FILE"

if [ ${#targets[@]} -eq 0 ]; then
    echo "No targets found in $SETTINGS_FILE."
    exit 1
fi

echo "Available Modstitch/Stonecutter Targets:"
for i in "${!targets[@]}"; do
    echo "  $((i+1))) ${targets[$i]}"
done

echo ""
read -p "Select a target number (1-${#targets[@]}): " choice

if ! [[ "$choice" =~ ^[0-9]+$ ]] || [ "$choice" -lt 1 ] || [ "$choice" -gt "${#targets[@]}" ]; then
    echo "Invalid selection."
    exit 1
fi

selected_target="${targets[$((choice-1))]}"

echo ""
echo "Setting active project to $selected_target..."

output=$(./gradlew "Set active project to $selected_target" 2>&1)
status=$?

if [ $status -eq 0 ]; then
    echo "Changed to $selected_target"
else
    echo "Failed to change target. Output:"
    echo "$output"
    exit $status
fi
