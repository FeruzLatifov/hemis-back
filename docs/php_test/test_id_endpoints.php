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

function callEndpoint($baseUrl, $token, $path, $method = 'GET', $body = null) {
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

function deepCompare($old, $new, $path = '') {
    $diffs = [];
    if (is_array($old) && is_array($new)) {
        $allKeys = array_unique(array_merge(array_keys($old), array_keys($new)));
        foreach ($allKeys as $key) {
            $newPath = $path ? "$path.$key" : $key;
            if (!array_key_exists($key, $old)) {
                $diffs[] = "+ $newPath: new da bor (new=" . json_encode($new[$key]) . ")";
            } elseif (!array_key_exists($key, $new)) {
                $diffs[] = "- $newPath: old da bor (old=" . json_encode($old[$key]) . ")";
            } else {
                $diffs = array_merge($diffs, deepCompare($old[$key], $new[$key], $newPath));
            }
        }
    } elseif ($old !== $new) {
        $diffs[] = "~ $path: old=" . json_encode($old) . " vs new=" . json_encode($new);
    }
    return $diffs;
}

echo "=== ID Endpoint Test ===\n\n";

$oldToken = getToken($old, $auth, $username, $password);
$newToken = getToken($new, $auth, $username, $password);

if (!$oldToken || !$newToken) {
    echo "Token olishda xatolik!\n";
    exit(1);
}

// Test student/id
echo "--- 1. Student ID Generation (/services/student/id) ---\n";
$studentId = "2290aa1b-4287-014c-de1a-c3d996f8e855";
$path = "v2/services/student/id?studentId=$studentId";

$oldResp = callEndpoint($old, $oldToken, $path);
$newResp = callEndpoint($new, $newToken, $path);

echo "OLD-HEMIS: HTTP {$oldResp['code']}\n";
echo "NEW-HEMIS: HTTP {$newResp['code']}\n";

if ($oldResp['code'] == 200 && $newResp['code'] == 200) {
    $diffs = deepCompare($oldResp['json'], $newResp['json']);
    if (empty($diffs)) {
        echo "\033[32m100% MATCH!\033[0m\n";
    } else {
        $totalFields = count(array_keys(json_decode(json_encode($oldResp['json']), true)));
        echo "\033[33mFARQLAR (" . count($diffs) . " ta):\033[0m\n";
        foreach (array_slice($diffs, 0, 20) as $d) echo "  $d\n";
        if (count($diffs) > 20) echo "  ... va yana " . (count($diffs) - 20) . " ta\n";
    }
}

// Test teacher/id
echo "\n--- 2. Teacher ID Generation (/services/teacher/id) ---\n";
$teacherId = "4f4bf177-3cb1-3f42-9339-d8ce3b4ffbb6";
$path = "v2/services/teacher/id?teacherId=$teacherId";

$oldResp = callEndpoint($old, $oldToken, $path);
$newResp = callEndpoint($new, $newToken, $path);

echo "OLD-HEMIS: HTTP {$oldResp['code']}\n";
echo "NEW-HEMIS: HTTP {$newResp['code']}\n";

if ($oldResp['code'] == 200 && $newResp['code'] == 200) {
    $diffs = deepCompare($oldResp['json'], $newResp['json']);
    if (empty($diffs)) {
        echo "\033[32m100% MATCH!\033[0m\n";
    } else {
        echo "\033[33mFARQLAR (" . count($diffs) . " ta):\033[0m\n";
        foreach (array_slice($diffs, 0, 20) as $d) echo "  $d\n";
        if (count($diffs) > 20) echo "  ... va yana " . (count($diffs) - 20) . " ta\n";
    }
} elseif ($oldResp['code'] == 404) {
    echo "OLD-HEMIS: 404 - endpoint mavjud emas\n";
    echo "NEW-HEMIS: Bu endpoint faqat hemis-back da\n";
}

echo "\n=== DONE ===\n";
