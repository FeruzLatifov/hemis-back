<?php
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

$newToken = getToken($new, $auth, $username, $password);
if (!$newToken) { echo "Token olishda xatolik!\n"; exit(1); }

// Get student with faculty=308-102
$ch = curl_init($new . "v2/entities/hemishe_EStudent/ef99299b-9b3c-73b0-90ee-a5e93976e223?returnNulls=true");
curl_setopt_array($ch, [
    CURLOPT_HTTPHEADER => ["Authorization: Bearer $newToken"],
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_TIMEOUT => 30
]);
$resp = curl_exec($ch);
$code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

$json = json_decode($resp, true);
echo "HTTP: $code\n";
echo "Faculty field: " . json_encode($json['faculty'] ?? 'NOT FOUND') . "\n";
echo "_faculty field: " . json_encode($json['_faculty'] ?? 'NOT FOUND') . "\n";
