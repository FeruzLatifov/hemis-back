#!/bin/bash
# Test student/id endpoint on both systems (new-hemis vs old-hemis)
# Usage: ./.scripts/test_student_id.sh

TOKEN="eyJraWQiOiJoZW1pcy1qd3Qta2V5IiwiYWxnIjoiSFMyNTYifQ.eyJpc3MiOiJoZW1pcy1iYWNrZW5kIiwic3ViIjoiMmI2YWQwMDQtOGVjYy02NDRjLWNlNDktYmQ3OGJiNDA2YjZhIiwiZXhwIjoxODUwMjE2NjIxLCJpYXQiOjE3NjM4MTY2MjEsInNjb3BlIjoicmVzdC1hcGkiLCJ1c2VybmFtZSI6ImZlcnV6In0.SokTpdK-OLzaMcyM7RvNMAnK4hmuQU9vkStCzzo0t8Y"
OLD_TOKEN="EHQUTFuoXBRc8-yWc0i0RtbufPo"

DATA='{"data":{"citizenship":"11","pinfl":"31507976020031","serial":"AA6970877","year":"2024","education_type":"11","education_form":"11"}}'

echo "=== NEW-HEMIS: POST /app/rest/v2/services/student/id ==="
curl -s -X POST "http://localhost:8081/app/rest/v2/services/student/id" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$DATA" > /tmp/new_student_id.json

python3 -m json.tool /tmp/new_student_id.json 2>/dev/null || cat /tmp/new_student_id.json
echo ""

echo "=== OLD-HEMIS: POST /app/rest/v2/services/student/id ==="
curl -s -X POST "http://localhost:8082/app/rest/v2/services/student/id" \
  -H "Authorization: Bearer $OLD_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$DATA" > /tmp/old_student_id.json

python3 -m json.tool /tmp/old_student_id.json 2>/dev/null || cat /tmp/old_student_id.json
