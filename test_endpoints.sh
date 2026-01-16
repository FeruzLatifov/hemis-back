#!/bin/bash
NEW_TOKEN="eyJraWQiOiJoZW1pcy1qd3Qta2V5IiwiYWxnIjoiSFMyNTYifQ.eyJpc3MiOiJoZW1pcy1iYWNrZW5kIiwic3ViIjoiMzdlMzI3ZTAtYzE1NC04MWFiLTU1MmUtZDIwY2ViOTg5NGIzIiwiZXhwIjoxODUxNDIxOTU1LCJpYXQiOjE3NjUwMjE5NTUsInNjb3BlIjoicmVzdC1hcGkiLCJ1c2VybmFtZSI6Im90bTQwMSJ9.gRef6eNzCesCg055mtg9T3UFK5NVtGNmp_fXPTLQdsc"

echo "=== #15 POST: Yangi xodim ish joyi yaratish ==="
RESPONSE=$(curl -s -X POST "http://localhost:8081/app/rest/v2/entities/hemishe_EEmployeeJobs" \
  -H "Authorization: Bearer $NEW_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "_employee": {"id": "b8c3b384-c048-f060-87b6-2ea3201c8325"},
    "_university": {"code": "305"},
    "_department": {"code": "305-212"},
    "_employeeType": {"code": "12"},
    "_teacherPositionType": {"code": "11"},
    "_employeeRate": {"code": "11"},
    "_employmentForm": {"code": "11"},
    "_employeeStatus": {"code": "11"},
    "jobStartDate": "2024-01-01",
    "contractNumber": "TEST-001"
  }')

echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"

# Extract created ID
CREATED_ID=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null)
echo ""
echo "Yaratilgan ish joyi ID: $CREATED_ID"

if [ -n "$CREATED_ID" ]; then
  echo ""
  echo "=== #16 PUT: Xodim ish joyini yangilash ==="
  PUT_RESPONSE=$(curl -s -X PUT "http://localhost:8081/app/rest/v2/entities/hemishe_EEmployeeJobs/$CREATED_ID" \
    -H "Authorization: Bearer $NEW_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "_employeeStatus": {"code": "11"},
      "jobEndDate": "2025-12-31",
      "contractNumber": "UPD-001"
    }')

  echo "$PUT_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$PUT_RESPONSE"

  echo ""
  echo "=== O'chirish (cleanup) ==="
  DELETE_RESPONSE=$(curl -s -X DELETE "http://localhost:8081/app/rest/v2/entities/hemishe_EEmployeeJobs/$CREATED_ID" \
    -H "Authorization: Bearer $NEW_TOKEN" \
    -w "HTTP Status: %{http_code}")
  echo "$DELETE_RESPONSE"
fi
