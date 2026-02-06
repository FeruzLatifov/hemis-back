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
    $url = $baseUrl . $path;
    if ($body && $method == 'GET') {
        $url .= '?' . http_build_query($body);
        $body = null;
    }
    $ch = curl_init($url);
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

echo "=== Student ID Endpoint Comparison ===\n\n";

$oldToken = getToken($old, $auth, $username, $password);
$newToken = getToken($new, $auth, $username, $password);

if (!$oldToken) { echo "OLD-HEMIS token olishda xatolik!\n"; exit(1); }
if (!$newToken) { echo "NEW-HEMIS token olishda xatolik!\n"; exit(1); }

echo "Tokens: OK\n\n";

// Test with existing student
$studentId = "2290aa1b-4287-014c-de1a-c3d996f8e855";

echo "--- POST /services/student/id (studentId=$studentId) ---\n";
$path = "v2/services/student/id";
$body = ["studentId" => $studentId];

$oldResp = callEndpoint($old, $oldToken, $path, 'POST', $body);
$newResp = callEndpoint($new, $newToken, $path, 'POST', $body);

echo "OLD-HEMIS: HTTP {$oldResp['code']}\n";
echo "NEW-HEMIS: HTTP {$newResp['code']}\n\n";

if ($oldResp['code'] == 200 && $newResp['code'] == 200) {
    echo "OLD Response keys: " . implode(", ", array_keys($oldResp['json'] ?? [])) . "\n";
    echo "NEW Response keys: " . implode(", ", array_keys($newResp['json'] ?? [])) . "\n\n";
    
    // Check main fields
    $fieldsToCheck = ['success', 'is_new', 'unique_id'];
    foreach ($fieldsToCheck as $f) {
        $oldVal = $oldResp['json'][$f] ?? 'MISSING';
        $newVal = $newResp['json'][$f] ?? 'MISSING';
        $match = $oldVal === $newVal ? "\033[32m✓\033[0m" : "\033[31m✗\033[0m";
        echo "$match $f: old=$oldVal, new=$newVal\n";
    }
    
    echo "\n--- Student object comparison ---\n";
    $oldStudent = $oldResp['json']['student'] ?? [];
    $newStudent = $newResp['json']['student'] ?? [];
    
    $allKeys = array_unique(array_merge(array_keys($oldStudent), array_keys($newStudent)));
    sort($allKeys);
    
    $matches = 0;
    $mismatches = [];
    
    foreach ($allKeys as $key) {
        $oldVal = $oldStudent[$key] ?? null;
        $newVal = $newStudent[$key] ?? null;
        
        if (is_array($oldVal) && is_array($newVal)) {
            // Compare nested objects
            $oldJson = json_encode($oldVal, JSON_UNESCAPED_UNICODE);
            $newJson = json_encode($newVal, JSON_UNESCAPED_UNICODE);
            if ($oldJson === $newJson) {
                $matches++;
            } else {
                $mismatches[] = "$key: DIFFERENT nested object";
            }
        } elseif ($oldVal === $newVal) {
            $matches++;
        } else {
            $oldStr = is_array($oldVal) ? json_encode($oldVal) : strval($oldVal);
            $newStr = is_array($newVal) ? json_encode($newVal) : strval($newVal);
            $mismatches[] = "$key: old=$oldStr vs new=$newStr";
        }
    }
    
    $total = count($allKeys);
    $percent = $total > 0 ? round($matches / $total * 100, 1) : 0;
    
    echo "Fields matched: $matches / $total ($percent%)\n";
    if (!empty($mismatches)) {
        echo "\nMismatches:\n";
        foreach (array_slice($mismatches, 0, 20) as $m) echo "  - $m\n";
        if (count($mismatches) > 20) echo "  ... va yana " . (count($mismatches) - 20) . " ta\n";
    }
} else {
    if ($oldResp['code'] != 200) {
        echo "OLD-HEMIS Response:\n";
        echo substr($oldResp['body'], 0, 500) . "\n";
    }
    if ($newResp['code'] != 200) {
        echo "NEW-HEMIS Response:\n";
        echo substr($newResp['body'], 0, 500) . "\n";
    }
}

echo "\n=== DONE ===\n";
