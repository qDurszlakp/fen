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

cecho "Stopping and removing containers defined in docker-compose.yml..."
docker-compose down --remove-orphans -v

cecho "Containers removed." 