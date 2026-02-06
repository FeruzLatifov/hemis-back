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

// Test university GET with returnNulls=true
$ch = curl_init($new . "v2/entities/hemishe_EUniversity/401?returnNulls=true");
curl_setopt_array($ch, [
    CURLOPT_HTTPHEADER => ["Authorization: Bearer $token"],
    CURLOPT_RETURNTRANSFER => true
]);
$resp = curl_exec($ch);
$json = json_decode($resp, true);
curl_close($ch);

echo "All keys in response:\n";
$keys = array_keys($json);
sort($keys);
echo implode(", ", $keys) . "\n\n";

// Check specific fields
$check = ['gradingSystem', 'accreditationInfo', 'deletedBy', 'deleteTs', 
          'uzbmbUrl', 'universityUrl', 'bankInfo', 'mailAddress', 'cadastre',
          'addForeignStudent', 'addTransferStudent'];
echo "Missing fields check:\n";
foreach ($check as $f) {
    $exists = array_key_exists($f, $json) ? "YES" : "NO";
    echo "  $f: $exists\n";
}
