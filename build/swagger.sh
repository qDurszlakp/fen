#!/bin/bash
# Dumps the OpenAPI spec from a running application.

set -e

HOST="${1:-http://localhost:8080}"
OUT="../swagger"

mkdir -p "$OUT"
curl -sS --fail "$HOST/v3/api-docs.yaml" -o "$OUT/openapi.yaml"

echo "Saved $OUT/openapi.yaml"
