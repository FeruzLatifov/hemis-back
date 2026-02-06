<?php
$new = "http://localhost:8081/app/rest/";
$auth = "Basic Y2xpZW50OnNlY3JldA==";
$username = "otm401";
$password = "XCZDAb7qvGTXxz";

$ch = curl_init($new . "v2/oauth/token");
curl_setopt_array($ch, [
    CURLOPT_POST => true,
    CURLOPT_POSTFIELDS => "grant_type=password&username=$username&password=$password",
    CURLOPT_HTTPHEADER => ["Authorization: $auth", "Content-Type: application/x-www-form-urlencoded"],
    CURLOPT_RETURNTRANSFER => true
]);
$token = json_decode(curl_exec($ch), true)['access_token'];
curl_close($ch);

// Test student/id
$ch = curl_init($new . "v2/services/student/id");
curl_setopt_array($ch, [
    CURLOPT_POST => true,
    CURLOPT_POSTFIELDS => json_encode([
        "data" => [
            "citizenship" => "11",
            "pinfl" => "52503015440023",
            "serial" => "AD8970877",
            "year" => "2024",
            "education_type" => "11"
        ]
    ]),
    CURLOPT_HTTPHEADER => ["Authorization: Bearer $token", "Content-Type: application/json"],
    CURLOPT_RETURNTRANSFER => true
]);
$resp = curl_exec($ch);
$json = json_decode($resp, true);
curl_close($ch);

// Check if faculty exists
$student = $json['student'] ?? [];
echo "Faculty in response: " . (isset($student['faculty']) ? json_encode($student['faculty']) : "NOT FOUND") . "\n";

// Print all student keys
echo "\nAll student keys:\n";
$keys = array_keys($student);
sort($keys);
echo implode(", ", $keys) . "\n";
