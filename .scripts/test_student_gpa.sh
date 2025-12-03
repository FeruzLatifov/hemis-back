#!/bin/bash
# Test script for hemishe_EStudentGpa entity endpoint
# OLD-HEMIS vs NEW-HEMIS comparison

# Configuration
OLD_HEMIS="http://localhost:8082"
NEW_HEMIS="http://localhost:8081"
OLD_TOKEN="${OLD_TOKEN:-hIvYdo6xPjRIm1fjJRbOlxphxF4}"
NEW_TOKEN="${NEW_TOKEN:-eyJraWQiOiJoZW1pcy1qd3Qta2V5IiwiYWxnIjoiSFMyNTYifQ.eyJpc3MiOiJoZW1pcy1iYWNrZW5kIiwic3ViIjoiMzdlMzI3ZTAtYzE1NC04MWFiLTU1MmUtZDIwY2ViOTg5NGIzIiwiZXhwIjoxODUwNzAxNzIwLCJpYXQiOjE3NjQzMDE3MjAsInNjb3BlIjoicmVzdC1hcGkiLCJ1c2VybmFtZSI6Im90bTQwMSJ9.YQnuN1s8wCiVlfqLNEQuR2q8w2MBZGUodGLFMfD4kVg}"

ENDPOINT="/app/rest/v2/entities/hemishe_EStudentGpa"
PARAMS="?limit=1&view=eStudentGpa-view"

echo "=========================================="
echo "TEST: hemishe_EStudentGpa Entity Endpoint"
echo "=========================================="
echo ""

# Test OLD-HEMIS
echo "=== OLD-HEMIS Response ==="
OLD_RESPONSE=$(curl -s "${OLD_HEMIS}${ENDPOINT}${PARAMS}" \
  -H "Authorization: Bearer $OLD_TOKEN")
echo "$OLD_RESPONSE" | jq . 2>/dev/null || echo "$OLD_RESPONSE"
echo ""

# Test NEW-HEMIS
echo "=== NEW-HEMIS Response ==="
NEW_RESPONSE=$(curl -s "${NEW_HEMIS}${ENDPOINT}${PARAMS}" \
  -H "Authorization: Bearer $NEW_TOKEN")
echo "$NEW_RESPONSE" | jq . 2>/dev/null || echo "$NEW_RESPONSE"
echo ""

# Compare
echo "=== Comparison ==="
if [ -z "$OLD_RESPONSE" ] && [ -z "$NEW_RESPONSE" ]; then
    echo "Both responses are empty"
elif echo "$OLD_RESPONSE" | jq -e '.[0]' > /dev/null 2>&1 && echo "$NEW_RESPONSE" | jq -e '.[0]' > /dev/null 2>&1; then
    # Both have data - compare structure
    OLD_FIELDS=$(echo "$OLD_RESPONSE" | jq -r '.[0] | keys | .[]' 2>/dev/null | sort)
    NEW_FIELDS=$(echo "$NEW_RESPONSE" | jq -r '.[0] | keys | .[]' 2>/dev/null | sort)

    echo "OLD-HEMIS fields: $(echo $OLD_FIELDS | tr '\n' ' ')"
    echo "NEW-HEMIS fields: $(echo $NEW_FIELDS | tr '\n' ' ')"

    if [ "$OLD_FIELDS" = "$NEW_FIELDS" ]; then
        echo "✅ Field structure matches!"
    else
        echo "❌ Field structure differs!"
        echo "Missing in NEW: $(comm -23 <(echo "$OLD_FIELDS") <(echo "$NEW_FIELDS") | tr '\n' ' ')"
        echo "Extra in NEW: $(comm -13 <(echo "$OLD_FIELDS") <(echo "$NEW_FIELDS") | tr '\n' ' ')"
    fi
elif echo "$NEW_RESPONSE" | jq -e '.status' > /dev/null 2>&1; then
    echo "❌ NEW-HEMIS returned error: $(echo "$NEW_RESPONSE" | jq -r '.message // .error')"
else
    echo "⚠️ Unable to compare - response format differs"
fi

echo ""
echo "=========================================="
echo "Test completed at $(date)"
echo "=========================================="
