#!/bin/bash

# Script to wait for Kafka to be ready and create defined topics.

BOOTSTRAP_SERVER="kafka:9092"
MAX_RETRIES=10
SLEEP_INTERVAL=15

# --- Define Topics to Create --- 
# Format: "topic_name;partitions;replication_factor"
declare -a TOPICS_TO_CREATE=(
    "FOO_LOGGING_TOPIC;2;1"
    # Add more topics here following the same format
    # "ANOTHER_TOPIC;3;1"
    # "YET_ANOTHER;5;1"
)

# --- Wait for Kafka --- 
echo "Waiting for Kafka ($BOOTSTRAP_SERVER) to be ready..."

kafka_ready=false
retry_count=0
while [ $retry_count -lt $MAX_RETRIES ]; do
  # Try listing topics to check readiness
  if /usr/bin/kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --list > /dev/null 2>&1; then
    echo "Kafka is ready!"
    kafka_ready=true
    break
  else
    echo "Kafka not ready yet. Retrying in $SLEEP_INTERVAL seconds... ($((retry_count+1))/$MAX_RETRIES)"
    sleep $SLEEP_INTERVAL
    retry_count=$((retry_count+1))
  fi
done

if [ "$kafka_ready" = false ]; then
  echo "Kafka did not become ready in time after $MAX_RETRIES retries. Exiting."
  exit 1
fi

# --- Create Topics --- 
echo "Processing topics..."

# Get the list of existing topics ONCE
existing_topics=$(/usr/bin/kafka-topics --bootstrap-server $BOOTSTRAP_SERVER --list)
echo "Existing topics:"
# Print existing topics
echo "${existing_topics:-<none>}"

# Loop through the defined topics
for topic_def in "${TOPICS_TO_CREATE[@]}"; do
    # Parse the definition string
    IFS=';' read -r topic_name partitions replication_factor <<< "$topic_def"

    echo "Checking topic '$topic_name'..."

    # Check if the topic exists in the list retrieved earlier
    # Use grep -q with -x to match the whole line exactly
    if echo "$existing_topics" | grep -q -x "$topic_name"; then
        echo "  Topic '$topic_name' already exists. Skipping creation."
    else
        echo "  Topic '$topic_name' does not exist. Creating with P=$partitions, R=$replication_factor..."
        /usr/bin/kafka-topics --create \
          --topic "$topic_name" \
          --bootstrap-server $BOOTSTRAP_SERVER \
          --partitions "$partitions" \
          --replication-factor "$replication_factor"
        
        # Optional: Verify creation
        if [ $? -eq 0 ]; then
            echo "  Topic '$topic_name' created successfully."
        else
            echo "  ERROR: Failed to create topic '$topic_name'."
            # Consider adding 'exit 1' here if creation failure should stop the script
        fi
    fi
done

echo "Topic processing finished."
exit 0