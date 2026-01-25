#!/bin/bash

# Script to delete all S3 assets from Devy Board
# WARNING: This will permanently delete all player headshot images!

echo "⚠️  WARNING: This will delete ALL player headshots from S3 and database!"
echo ""
read -p "Are you sure you want to continue? (type 'yes' to confirm): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Aborted."
    exit 0
fi

echo ""
echo "Sending DELETE request to https://devyboard.com/S3-delete..."
echo ""

curl -X DELETE https://devyboard.com/S3-delete \
  -H "Content-Type: application/json" \
  -w "\n\nHTTP Status: %{http_code}\n" \
  | jq '.' 2>/dev/null || cat

echo ""
echo "Done!"
