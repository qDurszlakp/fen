#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e
# Treat pipe errors
set -o pipefail

################## CONFIGURATION ##################

DOCKER_REGISTRY="qwerty2137"
SERVER_MODULE_DIR="Server"
FOO_MODULE_DIR="Foo"
SERVER_IMAGE_NAME="server"
FOO_IMAGE_NAME="foo"

################## HELPERS ##################

cecho() {
    local color=32
    # Print all arguments passed to the function
    printf '\e[%sm%s\e[0m\n' "$color" "$*"
}

# Help message function
show_help() {
    echo "Usage: ./run.sh [options]"
    echo ""
    echo "Options:"
    echo "  --run-tests         Run tests during Maven build."
    echo "  --push-images       Tag and push built images to Docker registry ($DOCKER_REGISTRY)."
    echo "  --help, -h          Show this help message and exit."
}

################## ARGUMENT PARSING ##################

# Default values
RUN_TESTS=false
PUSH_IMAGES=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        --run-tests)
            RUN_TESTS=true
            shift # past argument
            ;;
        --push-images)
            PUSH_IMAGES=true
            shift # past argument
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            # Unknown option
            cecho 31 "Error: Unknown option $1"
            show_help
            exit 1
            ;;
    esac
done

################## FUNCTIONS FOR STEPS ##################

step_1_build() {
    cecho "[1/4] Building Maven project and Docker images..."
    cd .. # Go to project root

    local mvn_cmd="mvn -q clean package"
    if [ "$RUN_TESTS" == "false" ]; then
        cecho "Skipping tests..."
        mvn_cmd="$mvn_cmd -DskipTests"
    else
        cecho "Running tests..."
    fi

    cecho "Running Maven build..."
    $mvn_cmd

    cecho "Building Docker image for $SERVER_MODULE_DIR..."
    (cd "$SERVER_MODULE_DIR" && docker build -q -t $SERVER_IMAGE_NAME .)

    cecho "Building Docker image for $FOO_MODULE_DIR..."
    (cd "$FOO_MODULE_DIR" && docker build -q -t $FOO_IMAGE_NAME .)

    cd build # Return to build directory
    cecho "Build step finished."
}

step_2_push_images() {
    cecho "[2/4] Tagging and pushing images to registry..."
    if [ "$PUSH_IMAGES" == "true" ]; then
        cecho "Tagging $SERVER_IMAGE_NAME -> $DOCKER_REGISTRY/$SERVER_IMAGE_NAME:latest"
        docker tag $SERVER_IMAGE_NAME "$DOCKER_REGISTRY/$SERVER_IMAGE_NAME:latest"

        cecho "Tagging $FOO_IMAGE_NAME -> $DOCKER_REGISTRY/$FOO_IMAGE_NAME:latest"
        docker tag $FOO_IMAGE_NAME "$DOCKER_REGISTRY/$FOO_IMAGE_NAME:latest"

        cecho "Pushing $DOCKER_REGISTRY/$SERVER_IMAGE_NAME:latest..."
        docker push -q "$DOCKER_REGISTRY/$SERVER_IMAGE_NAME:latest"

        cecho "Pushing $DOCKER_REGISTRY/$FOO_IMAGE_NAME:latest..."
        docker push -q "$DOCKER_REGISTRY/$FOO_IMAGE_NAME:latest"
        cecho "Image push finished."
    else
        cecho "Skipping image push (use --push-images to enable)."
    fi
}

step_3_run_containers() {
    cecho "[3/4] Running Docker Compose environment..."
    cecho "Stopping and removing existing environment (if any)..."
    docker-compose down --remove-orphans -v > /dev/null 2>&1
    cecho "Starting new containers (rebuilding services if necessary)..."
    docker-compose up --build --scale server=2 -d
    cecho "Docker Compose environment started."
}

step_4_cleanup() {
    cecho "[4/4] Cleaning up dangling Docker images..."
    # Use xargs --no-run-if-empty to avoid error if no images found
    docker images -f dangling=true -q | xargs --no-run-if-empty docker rmi
    cecho "Cleanup finished."
}

################## MAIN EXECUTION ##################

cecho "Starting deployment script..."

step_1_build
step_2_push_images
step_3_run_containers
step_4_cleanup

cecho "Script finished successfully!"