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
        CURLOPT_RETURNTRANSFER => true
    ]);
    $resp = curl_exec($ch);
    curl_close($ch);
    return json_decode($resp, true)['access_token'] ?? null;
}

function deepCompare($old, $new, $path = '') {
    $diffs = [];
    if (is_array($old) && is_array($new)) {
        $allKeys = array_unique(array_merge(array_keys($old), array_keys($new)));
        foreach ($allKeys as $key) {
            $newPath = $path ? "$path.$key" : $key;
            if (!array_key_exists($key, $old)) {
                $diffs[] = "+ $newPath: new da bor, old da YO'Q";
            } elseif (!array_key_exists($key, $new)) {
                $diffs[] = "- $newPath: old da bor, new da YO'Q";
            } else {
                $diffs = array_merge($diffs, deepCompare($old[$key], $new[$key], $newPath));
            }
        }
    } elseif ($old !== $new) {
        // Ignore minor differences
        $oldStr = is_bool($old) ? ($old ? "true" : "false") : strval($old);
        $newStr = is_bool($new) ? ($new ? "true" : "false") : strval($new);
        // Ignore date format differences (space vs T)
        $oldNorm = str_replace(" ", "T", $oldStr);
        $newNorm = str_replace(" ", "T", $newStr);
        if ($oldNorm !== $newNorm) {
            $diffs[] = "~ $path: " . json_encode($old) . " vs " . json_encode($new);
        }
    }
    return $diffs;
}

$oldToken = getToken($old, $auth, $username, $password);
$newToken = getToken($new, $auth, $username, $password);

$testData = ["data" => ["citizenship" => "11", "pinfl" => "52503015440023", "serial" => "AD8970877", "year" => "2024", "education_type" => "11"]];

$ch = curl_init($old . "v2/services/student/id");
curl_setopt_array($ch, [CURLOPT_POST => true, CURLOPT_POSTFIELDS => json_encode($testData), CURLOPT_HTTPHEADER => ["Authorization: Bearer $oldToken", "Content-Type: application/json"], CURLOPT_RETURNTRANSFER => true]);
$oldResp = json_decode(curl_exec($ch), true);
curl_close($ch);

$ch = curl_init($new . "v2/services/student/id");
curl_setopt_array($ch, [CURLOPT_POST => true, CURLOPT_POSTFIELDS => json_encode($testData), CURLOPT_HTTPHEADER => ["Authorization: Bearer $newToken", "Content-Type: application/json"], CURLOPT_RETURNTRANSFER => true]);
$newResp = json_decode(curl_exec($ch), true);
curl_close($ch);

echo "=== student/id Taqqoslash ===\n\n";
echo "Top-level: success={$oldResp['success']}, is_active={$oldResp['is_active']}\n\n";

$diffs = deepCompare($oldResp['student'], $newResp['student']);
$total = count($diffs);

if ($total == 0) {
    echo "100% MATCH - Hech qanday farq yo'q!\n";
} else {
    // Filter important diffs
    $important = [];
    $minor = [];
    foreach ($diffs as $d) {
        if (strpos($d, '_instanceName') !== false || strpos($d, '.active') !== false) {
            $minor[] = $d;
        } else {
            $important[] = $d;
        }
    }
    
    echo "Muhim farqlar (" . count($important) . " ta):\n";
    foreach ($important as $d) echo "  $d\n";
    
    echo "\nKichik farqlar (_instanceName, active) (" . count($minor) . " ta)\n";
}
