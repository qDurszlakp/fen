#!/bin/bash

################## HELPERS ##################
function cecho() {
    # Default color is green
    local color=32

    # Check if a color code is provided as the first argument
    if [[ "$1" =~ ^[0-9]+$ ]]; then
        color=$1
        shift
    fi

    echo -e "\e[${color}m$*\e[0m"
}

################## SCRIPT ##################

# Change directory to the location of the docker scripts
cd "$(dirname "$0")"

cecho "Stopping and removing containers defined in podman compose.yml..."
podman compose stop -t 0 2>/dev/null || true
podman compose down --remove-orphans -v 2>/dev/null || true

# Force-remove any leftover containers from the project network
LEFTOVER=$(podman ps -a --filter network=build_fen_network --format '{{.ID}}' 2>/dev/null)
if [ -n "$LEFTOVER" ]; then
    cecho 33 "Force-removing leftover containers..."
    echo "$LEFTOVER" | xargs podman rm -f 2>/dev/null || true
fi
podman network rm build_fen_network 2>/dev/null || true

cecho "Containers removed."