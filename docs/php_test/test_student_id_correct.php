<?php
$old = "http://localhost:8082/app/rest/";
$new = "http://localhost:8081/app/rest/";
$auth = "Basic Y2xpZW50OnNlY3JldA==";
$username = "otm401";
$password = "XCZDAb7qvGTXxz";

function getToken($baseUrl, $auth, $username, $password) {
    $ch = curl_init($baseUrl . "v2/oauth/token");
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => "grant_type=password&username=$username&password=$password",
        CURLOPT_HTTPHEADER => ["Authorization: $auth", "Content-Type: application/x-www-form-urlencoded"],
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => 10
    ]);
    $resp = curl_exec($ch);
    curl_close($ch);
    $json = json_decode($resp, true);
    return $json['access_token'] ?? null;
}

function callEndpoint($baseUrl, $token, $path, $method = 'POST', $body = null) {
    $ch = curl_init($baseUrl . $path);
    $headers = ["Authorization: Bearer $token"];
    if ($body) {
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($body));
        $headers[] = "Content-Type: application/json";
    }
    curl_setopt_array($ch, [
        CURLOPT_HTTPHEADER => $headers,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_CUSTOMREQUEST => $method,
        CURLOPT_TIMEOUT => 30
    ]);
    $resp = curl_exec($ch);
    $code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    return ['code' => $code, 'body' => $resp, 'json' => json_decode($resp, true)];
}

echo "=== Student ID Endpoint Comparison (Correct Format) ===\n\n";

$oldToken = getToken($old, $auth, $username, $password);
$newToken = getToken($new, $auth, $username, $password);

if (!$oldToken || !$newToken) { echo "Token olishda xatolik!\n"; exit(1); }

echo "Tokens: OK\n\n";

// OLD-HEMIS format: POST /services/student/id with {"data": {...}}
$testData = [
    "data" => [
        "citizenship" => "11",
        "pinfl" => "52503015440023",
        "serial" => "AD8970877",
        "year" => "2024",
        "education_type" => "11",
        "education_form" => "11"
    ]
];

echo "--- POST /services/student/id (OLD-HEMIS format) ---\n";
echo "Request: " . json_encode($testData, JSON_PRETTY_PRINT) . "\n\n";

$path = "v2/services/student/id";

$oldResp = callEndpoint($old, $oldToken, $path, 'POST', $testData);
$newResp = callEndpoint($new, $newToken, $path, 'POST', $testData);

echo "OLD-HEMIS: HTTP {$oldResp['code']}\n";
echo "NEW-HEMIS: HTTP {$newResp['code']}\n\n";

echo "OLD-HEMIS Response:\n";
echo json_encode($oldResp['json'], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE) . "\n\n";

echo "NEW-HEMIS Response:\n";
echo json_encode($newResp['json'], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE) . "\n";

echo "\n=== DONE ===\n";
