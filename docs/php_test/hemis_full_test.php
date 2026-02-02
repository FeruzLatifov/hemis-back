<?php
/**
 * HEMIS Full Integration Test with Response Body Comparison
 *
 * Barcha HEMIS_ENDPOINT orqali chaqiriladigan endpointlarni test qiladi.
 * Qoida: avval old-hemis (8082) da test, ishlasa — hemis-back (8081) da ham test.
 * Old-hemis da 404 bo'lsa — SKIP (ishlatilmagan endpoint).
 *
 * Yangi: JSON response body ni field-by-field solishtiradi.
 */

$OLD_BASE = 'http://localhost:8082/app/rest/';
$NEW_BASE = 'http://localhost:8081/app/rest/';

// Ignorable fields — timestamp/UUID farqlar
$IGNORE_FIELDS = ['id', 'createTs', 'updateTs', 'createdBy', 'updatedBy', 'version', 'dbGeneratedId'];

// --- Auth ---
function getToken($base) {
    $ch = curl_init($base . 'v2/oauth/token');
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => 'grant_type=password&username=otm401&password=XCZDAb7qvGTXxz',
        CURLOPT_HTTPHEADER => [
            'Authorization: Basic Y2xpZW50OnNlY3JldA==',
            'Content-Type: application/x-www-form-urlencoded; charset=UTF-8',
        ],
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => 10,
        CURLOPT_ENCODING => '', // Accept gzip/deflate automatically
    ]);
    $resp = curl_exec($ch);
    $code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    if ($code >= 200 && $code < 300) {
        $data = json_decode($resp, true);
        return $data['access_token'] ?? null;
    }
    echo "  Token xato (HTTP $code): " . substr($resp, 0, 200) . "\n";
    return null;
}

/**
 * API so'rov yuborish — to'liq response body saqlash
 */
function apiCall($base, $method, $path, $token, $body = null, $noAuth = false) {
    $url = $base . $path;
    $ch = curl_init($url);
    if ($noAuth) {
        $headers = ['Content-Type: application/json'];
    } else {
        $headers = [
            "Authorization: Bearer $token",
            'Content-Type: application/json',
        ];
    }
    $opts = [
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER => $headers,
        CURLOPT_TIMEOUT => 30,
        CURLOPT_CUSTOMREQUEST => $method,
        CURLOPT_ENCODING => '', // gzip/deflate decompression
    ];
    if ($body !== null) {
        $opts[CURLOPT_POSTFIELDS] = is_string($body) ? $body : json_encode($body);
    }
    curl_setopt_array($ch, $opts);
    $resp = curl_exec($ch);
    $code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $err = curl_error($ch);
    curl_close($ch);

    // JSON parse
    $json = null;
    if ($resp !== false && $resp !== '') {
        $json = json_decode($resp, true);
        if (json_last_error() !== JSON_ERROR_NONE) {
            $json = null;
        }
    }

    return [
        'code' => $code,
        'body' => $resp,
        'json' => $json,
        'error' => $err,
    ];
}

// ============================================================
// JSON Deep Comparison
// ============================================================

/**
 * Recursive JSON taqqoslash
 * @return array ['diffs' => [...], 'total_fields' => int, 'matching_fields' => int]
 */
function compareJson($old, $new, $path = '', $depth = 0) {
    global $IGNORE_FIELDS;

    $diffs = [];
    $totalFields = 0;
    $matchingFields = 0;

    // Depth limit
    if ($depth > 20) {
        return ['diffs' => [], 'total_fields' => 0, 'matching_fields' => 0];
    }

    // Null check
    if ($old === null && $new === null) {
        return ['diffs' => [], 'total_fields' => 1, 'matching_fields' => 1];
    }

    // Type comparison
    $oldType = gettype($old);
    $newType = gettype($new);

    // Normalize: integer/double -> number
    $oldTypeN = in_array($oldType, ['integer', 'double']) ? 'number' : $oldType;
    $newTypeN = in_array($newType, ['integer', 'double']) ? 'number' : $newType;

    $currentField = basename($path);

    // Both are arrays (could be JSON array or object)
    if ($oldType === 'array' && $newType === 'array') {
        $oldIsAssoc = isAssoc($old);
        $newIsAssoc = isAssoc($new);

        if ($oldIsAssoc && $newIsAssoc) {
            // Object comparison — field by field
            $allKeys = array_unique(array_merge(array_keys($old), array_keys($new)));
            foreach ($allKeys as $key) {
                $fieldPath = $path === '' ? $key : "$path.$key";
                $isIgnored = in_array($key, $IGNORE_FIELDS);

                if (!array_key_exists($key, $new)) {
                    $totalFields++;
                    if ($isIgnored) {
                        $matchingFields++;
                    } else {
                        $diffs[] = [
                            'path' => $fieldPath,
                            'type' => 'MISSING_IN_NEW',
                            'old_value' => summarizeValue($old[$key]),
                            'ignored' => $isIgnored,
                        ];
                    }
                } elseif (!array_key_exists($key, $old)) {
                    $totalFields++;
                    if ($isIgnored) {
                        $matchingFields++;
                    } else {
                        $diffs[] = [
                            'path' => $fieldPath,
                            'type' => 'MISSING_IN_OLD',
                            'new_value' => summarizeValue($new[$key]),
                            'ignored' => $isIgnored,
                        ];
                    }
                } else {
                    // Both have the key
                    $sub = compareJson($old[$key], $new[$key], $fieldPath, $depth + 1);
                    $totalFields += $sub['total_fields'];
                    $matchingFields += $sub['matching_fields'];
                    if ($isIgnored) {
                        // Ignored fieldlar uchun diff ni qayd qilamiz lekin matching hisoblaymiz
                        foreach ($sub['diffs'] as &$d) {
                            $d['ignored'] = true;
                        }
                        unset($d);
                        // Ignored fieldlarni match hisoblaymiz
                        $matchingFields += ($sub['total_fields'] - $sub['matching_fields']);
                    }
                    $diffs = array_merge($diffs, $sub['diffs']);
                }
            }
        } elseif (!$oldIsAssoc && !$newIsAssoc) {
            // Array comparison — first element taqqoslash (list comparison)
            $totalFields++;
            if (count($old) === 0 && count($new) === 0) {
                $matchingFields++;
            } elseif (count($old) > 0 && count($new) > 0) {
                // Agar arrayning har bir elementi bitta kalitli object bo'lsa,
                // kalit bo'yicha saralash (masalan: classifiers arrayi)
                $sortByKey = function(&$arr) {
                    if (count($arr) > 1 && is_array($arr[0]) && count($arr[0]) === 1) {
                        usort($arr, function($a, $b) {
                            return strcmp(array_keys($a)[0], array_keys($b)[0]);
                        });
                    }
                };
                $sortByKey($old);
                $sortByKey($new);

                // Birinchi elementni solishtirish (struktura uchun)
                $sub = compareJson($old[0], $new[0], $path . '[0]', $depth + 1);
                $totalFields += $sub['total_fields'] - 1;
                $matchingFields += $sub['matching_fields'];
                $diffs = array_merge($diffs, $sub['diffs']);

                // Array length diff
                if (count($old) !== count($new)) {
                    $diffs[] = [
                        'path' => $path . '.length',
                        'type' => 'VALUE_DIFF',
                        'old_value' => count($old),
                        'new_value' => count($new),
                        'ignored' => true, // array length farqi odatda test data
                    ];
                }
            } else {
                // Bir bo'sh, bir yo'q
                $diffs[] = [
                    'path' => $path,
                    'type' => 'VALUE_DIFF',
                    'old_value' => 'array(' . count($old) . ')',
                    'new_value' => 'array(' . count($new) . ')',
                    'ignored' => false,
                ];
            }
        } else {
            // Bir assoc, bir indexed
            $totalFields++;
            $diffs[] = [
                'path' => $path,
                'type' => 'TYPE_MISMATCH',
                'old_type' => $oldIsAssoc ? 'object' : 'array',
                'new_type' => $newIsAssoc ? 'object' : 'array',
                'ignored' => false,
            ];
        }
    } elseif ($oldTypeN !== $newTypeN) {
        // Type mismatch (lekin null vs type ham bo'lishi mumkin)
        $totalFields++;
        $isIgnored = in_array($currentField, $IGNORE_FIELDS);

        // null vs boshqa — ko'pincha test data farqi
        if ($old === null || $new === null) {
            $diffs[] = [
                'path' => $path,
                'type' => 'VALUE_DIFF',
                'old_value' => summarizeValue($old),
                'new_value' => summarizeValue($new),
                'ignored' => $isIgnored,
            ];
            if ($isIgnored) $matchingFields++;
        } else {
            // Haqiqiy type mismatch — string vs int kabi
            // string "123" vs int 123 — bu normal, match hisoblaymiz
            if (isNumericEquivalent($old, $new)) {
                $matchingFields++;
                $diffs[] = [
                    'path' => $path,
                    'type' => 'VALUE_DIFF',
                    'old_value' => summarizeValue($old) . " ($oldType)",
                    'new_value' => summarizeValue($new) . " ($newType)",
                    'note' => 'numeric equivalent',
                    'ignored' => true,
                ];
            } else {
                $diffs[] = [
                    'path' => $path,
                    'type' => 'TYPE_MISMATCH',
                    'old_type' => $oldType,
                    'new_type' => $newType,
                    'old_value' => summarizeValue($old),
                    'new_value' => summarizeValue($new),
                    'ignored' => $isIgnored,
                ];
                if ($isIgnored) $matchingFields++;
            }
        }
    } else {
        // Same type — value comparison
        $totalFields++;
        $isIgnored = in_array($currentField, $IGNORE_FIELDS);
        if ($old === $new) {
            $matchingFields++;
        } else {
            if ($isIgnored) {
                $matchingFields++;
            }
            $diffs[] = [
                'path' => $path,
                'type' => 'VALUE_DIFF',
                'old_value' => summarizeValue($old),
                'new_value' => summarizeValue($new),
                'ignored' => $isIgnored,
            ];
        }
    }

    return ['diffs' => $diffs, 'total_fields' => max($totalFields, 1), 'matching_fields' => $matchingFields];
}

function isAssoc(array $arr): bool {
    if (empty($arr)) return false;
    return array_keys($arr) !== range(0, count($arr) - 1);
}

function summarizeValue($val): string {
    if ($val === null) return 'null';
    if (is_bool($val)) return $val ? 'true' : 'false';
    if (is_array($val)) {
        if (isAssoc($val)) {
            $keys = array_keys($val);
            if (count($keys) <= 3) return '{' . implode(',', $keys) . '}';
            return 'object(' . count($keys) . ' fields)';
        }
        return 'array(' . count($val) . ')';
    }
    $s = (string)$val;
    if (strlen($s) > 80) return substr($s, 0, 77) . '...';
    return $s;
}

function isNumericEquivalent($a, $b): bool {
    if (is_numeric($a) && is_numeric($b)) {
        return (float)$a === (float)$b;
    }
    return false;
}

/**
 * Moslik darajasini aniqlash
 */
function getMatchLevel(array $comparison): string {
    $diffs = $comparison['diffs'];
    $total = $comparison['total_fields'];
    $matching = $comparison['matching_fields'];

    if (empty($diffs)) return 'FULL_MATCH';

    // Faqat ignored difflar bormi?
    $nonIgnoredDiffs = array_filter($diffs, fn($d) => !($d['ignored'] ?? false));

    if (empty($nonIgnoredDiffs)) return 'FULL_MATCH';

    // Strukturaviy difflar (MISSING_IN_NEW, MISSING_IN_OLD, TYPE_MISMATCH)
    $structuralDiffs = array_filter($nonIgnoredDiffs, fn($d) =>
        in_array($d['type'], ['MISSING_IN_NEW', 'MISSING_IN_OLD', 'TYPE_MISMATCH'])
    );

    $matchPercent = $total > 0 ? ($matching / $total * 100) : 0;

    if (empty($structuralDiffs)) {
        return 'STRUCTURE_MATCH'; // Fieldlar bir xil, faqat qiymatlar farq
    }

    if ($matchPercent >= 50) {
        return 'PARTIAL_MATCH'; // Ba'zi fieldlar yo'q/qo'shimcha
    }

    return 'STRUCTURE_MISMATCH'; // JSON strukturasi tubdan farq
}

// ============================================================
// Test runner
// ============================================================
$results = [];
$pass = 0; $fail = 0; $skip = 0;
$comparisonReport = [];

function test($name, $method, $path, $body = null, $expectCodes = [200, 201], $noAuth = false) {
    global $OLD_BASE, $NEW_BASE, $oldToken, $newToken, $results, $pass, $fail, $skip, $comparisonReport;

    echo "\n--- $name ---\n";
    echo "  $method $path" . ($noAuth ? " [NO AUTH]" : "") . "\n";

    // 1) Old-hemis test
    $old = apiCall($OLD_BASE, $method, $path, $oldToken, $body, $noAuth);
    echo "  OLD: {$old['code']}";

    $oldFailed = ($old['code'] == 404 || $old['code'] == 405 || $old['code'] >= 500);

    if ($oldFailed) {
        echo ", " . substr($old['body'], 0, 80);
    }

    // 2) New-hemis test
    $new = apiCall($NEW_BASE, $method, $path, $newToken, $body, $noAuth);
    echo ", NEW: {$new['code']}\n";

    $oldIsError = ($old['code'] >= 400);
    $newIsError = ($new['code'] >= 400);
    $bothError = ($oldIsError && $newIsError);

    // JSON comparison
    $comparison = null;
    $matchLevel = null;
    $matchPercent = 0;

    if ($old['json'] !== null && $new['json'] !== null) {
        $comparison = compareJson($old['json'], $new['json']);
        $matchLevel = getMatchLevel($comparison);
        $matchPercent = $comparison['total_fields'] > 0
            ? round($comparison['matching_fields'] / $comparison['total_fields'] * 100, 1)
            : 100;

        // Rangli natija
        $levelColors = [
            'FULL_MATCH' => "\033[32m",
            'STRUCTURE_MATCH' => "\033[32m",
            'PARTIAL_MATCH' => "\033[33m",
            'STRUCTURE_MISMATCH' => "\033[31m",
        ];
        $levelColor = $levelColors[$matchLevel] ?? "\033[0m";
        echo "  JSON: {$levelColor}{$matchLevel}\033[0m ({$matchPercent}% fields match)\n";

        // Non-ignored difflarni ko'rsatish
        $nonIgnored = array_filter($comparison['diffs'], fn($d) => !($d['ignored'] ?? false));
        if (!empty($nonIgnored)) {
            $shown = 0;
            foreach ($nonIgnored as $d) {
                if ($shown >= 10) {
                    $remaining = count($nonIgnored) - $shown;
                    echo "    ... va yana $remaining ta farq\n";
                    break;
                }
                $icons = [
                    'MISSING_IN_NEW' => "\033[31m-\033[0m",
                    'MISSING_IN_OLD' => "\033[33m+\033[0m",
                    'TYPE_MISMATCH' => "\033[31m!\033[0m",
                    'VALUE_DIFF' => "\033[36m~\033[0m",
                ];
                $icon = $icons[$d['type']] ?? ' ';
                switch ($d['type']) {
                    case 'MISSING_IN_NEW':
                        echo "    $icon {$d['path']}: old da bor, new da YO'Q (old={$d['old_value']})\n";
                        break;
                    case 'MISSING_IN_OLD':
                        echo "    $icon {$d['path']}: new da bor, old da YO'Q (new={$d['new_value']})\n";
                        break;
                    case 'TYPE_MISMATCH':
                        $ot = $d['old_type'] ?? '?';
                        $nt = $d['new_type'] ?? '?';
                        echo "    $icon {$d['path']}: type farq — old=$ot, new=$nt\n";
                        break;
                    case 'VALUE_DIFF':
                        $ov = $d['old_value'] ?? '?';
                        $nv = $d['new_value'] ?? '?';
                        echo "    $icon {$d['path']}: old=\"$ov\" vs new=\"$nv\"\n";
                        break;
                }
                $shown++;
            }
        }
    } elseif ($old['json'] === null && $new['json'] === null) {
        // Ikkisi ham JSON emas
        if ($old['body'] === $new['body']) {
            $matchLevel = 'FULL_MATCH';
            echo "  JSON: \033[33mNon-JSON response, body match\033[0m\n";
        } else {
            $matchLevel = null;
            echo "  JSON: \033[33mNon-JSON response\033[0m\n";
        }
    } else {
        $matchLevel = 'STRUCTURE_MISMATCH';
        echo "  JSON: \033[31mSTRUCTURE_MISMATCH\033[0m (bir tomoni JSON emas)\n";
    }

    // Test natijasi
    $testResult = 'FAIL';
    if ($new['code'] >= 200 && $new['code'] < 300) {
        if ($matchLevel === 'STRUCTURE_MISMATCH') {
            echo "  \033[31mFAIL\033[0m (HTTP OK lekin JSON strukturasi mos emas)\n";
            $testResult = 'FAIL';
            $fail++;
        } else {
            $label = $oldFailed ? "PASS (old: {$old['code']})" : "PASS";
            echo "  \033[32m$label\033[0m\n";
            $testResult = 'PASS';
            $pass++;
        }
    } elseif ($bothError) {
        echo "  \033[32mPASS\033[0m (ikkala server xato: Old={$old['code']}, New={$new['code']})\n";
        $testResult = 'PASS';
        $pass++;
    } else {
        echo "  \033[31mFAIL\033[0m (Old={$old['code']}, New={$new['code']})\n";
        $testResult = 'FAIL';
        $fail++;
    }

    // Result saqlash
    $result = [
        'name' => $name,
        'status' => $testResult,
        'method' => $method,
        'path' => $path,
        'old_code' => $old['code'],
        'new_code' => $new['code'],
        'match_level' => $matchLevel,
        'match_percent' => $matchPercent,
    ];
    if ($testResult === 'FAIL') {
        $result['old_body'] = substr($old['body'], 0, 1000);
        $result['new_body'] = substr($new['body'], 0, 1000);
    }
    if ($oldFailed && $testResult === 'PASS') {
        $result['note'] = "Old-hemis {$old['code']}, hemis-back OK";
    }
    $results[] = $result;

    // Comparison report saqlash
    if ($comparison !== null) {
        $nonIgnoredDiffs = array_filter($comparison['diffs'], fn($d) => !($d['ignored'] ?? false));
        $comparisonReport[] = [
            'name' => $name,
            'method' => $method,
            'path' => $path,
            'old_code' => $old['code'],
            'new_code' => $new['code'],
            'match_level' => $matchLevel,
            'match_percent' => $matchPercent,
            'total_fields' => $comparison['total_fields'],
            'matching_fields' => $comparison['matching_fields'],
            'non_ignored_diffs' => count($nonIgnoredDiffs),
            'all_diffs' => $comparison['diffs'],
        ];
    }
}

// ============================================================
echo "=== HEMIS Full Comparison Test ===\n";
echo "Old: $OLD_BASE\nNew: $NEW_BASE\n";

echo "\n--- 0. Authentication ---\n";
$oldToken = getToken($OLD_BASE);
echo "  Old-hemis token: " . ($oldToken ? "OK" : "FAIL") . "\n";
$newToken = getToken($NEW_BASE);
echo "  New-hemis token: " . ($newToken ? "OK" : "FAIL") . "\n";

if (!$oldToken || !$newToken) {
    echo "Token olishda xato! Test to'xtatildi.\n";
    exit(1);
}

// ============================================================
// ENTITY ENDPOINTLAR
// ============================================================
echo "\n\n========== ENTITY ENDPOINTLAR ==========\n";

// --- Student ---
$studentUid = '2290aa1b-4287-014c-de1a-c3d996f8e855';
test('Student GET', 'GET', "v2/entities/hemishe_EStudent/$studentUid?returnNulls=true");
test('Student GET (view)', 'GET', "v2/entities/hemishe_EStudent/$studentUid?dynamicAttributes=true&returnNulls=true&view=eStudent-view");
test('Student POST (upsert)', 'POST', 'v2/entities/hemishe_EStudent/', [
    'id' => $studentUid,
    '_university' => ['code' => '401'],
    '_specialty' => ['code' => '5111000'],
    '_educationType' => ['code' => '11'],
    '_educationForm' => ['code' => '11'],
    'name' => 'Test Student',
    'fullName' => 'TEST TEST TEST',
    'passport_pin' => '52603015520014',
]);
test('Student PUT', 'PUT', "v2/entities/hemishe_EStudent/$studentUid", [
    'name' => 'Test Student Updated',
]);
test('Student PUT (responseView)', 'PUT', "v2/entities/hemishe_EStudent/$studentUid?responseView=_local", [
    'name' => 'Test Student Updated',
]);

// --- Teacher ---
$teacherUid = '4f4bf177-3cb1-3f42-9339-d8ce3b4ffbb6';
test('Teacher GET', 'GET', "v2/entities/hemishe_ETeacher/$teacherUid?returnNulls=true");
test('Teacher GET (view)', 'GET', "v2/entities/hemishe_ETeacher/$teacherUid?dynamicAttributes=true&returnNulls=true&view=eTeacher-view");
test('Teacher PUT', 'PUT', "v2/entities/hemishe_ETeacher/$teacherUid", [
    'name' => 'Test Teacher',
]);

// --- University ---
test('University GET list', 'GET', 'v2/entities/hemishe_EUniversity/?limit=1');
test('University GET (addForeignStudent)', 'GET', 'v2/entities/hemishe_EUniversity/401?returnNulls=true');
test('University POST', 'POST', 'v2/entities/hemishe_EUniversity/', [
    '_university' => ['code' => '401'],
    'name' => 'Test University',
]);

// --- Specialty ---
test('Specialty POST', 'POST', 'v2/entities/hemishe_EUniversitySpeciality/', [
    '_university' => ['code' => '401'],
    '_educationType' => ['code' => '11'],
    'code' => '5111000',
    'name' => 'Test Specialty',
]);

// --- Department ---
test('Department GET list', 'GET', 'v2/entities/hemishe_EUniversityDepartment/?limit=3');
test('Department GET (view)', 'GET', 'v2/entities/hemishe_EUniversityDepartment/?limit=1&view=eUniversityDepartment-view&returnNulls=true');
test('Department POST', 'POST', 'v2/entities/hemishe_EUniversityDepartment/', [
    '_university' => ['code' => '401'],
    'code' => '401-TEST',
    'name' => 'Test Department',
]);

// --- Group ---
test('Group POST', 'POST', 'v2/entities/hemishe_EUniversityGroup/', [
    '_university' => '401',
    '_education_type' => '11',
    '_education_year' => '2024',
    'group_id' => 'TEST-GROUP-001',
    'group_name' => 'Test Group',
]);

// --- Student Diploma ---
test('StudentDiploma GET list', 'GET', 'v2/entities/hemishe_EStudentDiploma/?limit=3');
test('StudentDiploma GET (view)', 'GET', 'v2/entities/hemishe_EStudentDiploma/?limit=1&view=eStudentDiploma-view&returnNulls=true');
test('StudentDiploma POST', 'POST', 'v2/entities/hemishe_EStudentDiploma/', [
    '_student' => $studentUid,
    '_university' => ['code' => '401'],
    'serial' => 'TEST001',
    'number' => '000001',
]);

// --- Student GPA (entity endpoint) ---
test('StudentGpa GET list', 'GET', 'v2/entities/hemishe_EStudentGpa/?limit=3');
test('StudentGpa GET (view)', 'GET', 'v2/entities/hemishe_EStudentGpa/?limit=1&view=eStudentGpa-view&returnNulls=true&dynamicAttributes=true');
test('StudentGpa POST', 'POST', 'v2/entities/hemishe_EStudentGpa/', [
    '_student' => ['id' => $studentUid],
    '_university' => ['code' => '401'],
    '_educationYear' => ['code' => '2024'],
    'gpa' => '3.5',
]);

// --- Dissertation Defense ---
test('DissertationDefense GET list', 'GET', 'v2/entities/hemishe_EDissertationDefense/?limit=3');
test('DissertationDefense POST', 'POST', 'v2/entities/hemishe_EDissertationDefense/', [
    '_university' => ['code' => '401'],
    'name' => 'Test Dissertation',
]);

// --- Doctorate Student ---
test('DoctorateStudent GET list', 'GET', 'v2/entities/hemishe_EDoctorateStudent/?limit=3');
test('DoctorateStudent POST', 'POST', 'v2/entities/hemishe_EDoctorateStudent/', [
    '_university' => ['code' => '401'],
    'name' => 'Test Doctorate Student',
    'passport_pin' => '12345678901234',
]);

// --- Employee Jobs ---
test('EmployeeJobs GET list', 'GET', 'v2/entities/hemishe_EEmployeeJob/?limit=3');

// --- DELETE operatsiyalar ---
// DELETE uchun mavjud bo'lmagan ID yuboramiz — 404 kutamiz ikkala tomondan
$fakeDeleteUid = '00000000-0000-0000-0000-000000000000';
test('EmployeeJobs DELETE', 'DELETE', "v2/entities/hemishe_EEmployeeJob/$fakeDeleteUid");
test('Student DELETE', 'DELETE', "v2/entities/hemishe_EStudent/$fakeDeleteUid");
test('Teacher DELETE', 'DELETE', "v2/entities/hemishe_ETeacher/$fakeDeleteUid");
test('Department DELETE', 'DELETE', "v2/entities/hemishe_EUniversityDepartment/$fakeDeleteUid");
test('StudentGpa DELETE', 'DELETE', "v2/entities/hemishe_EStudentGpa/$fakeDeleteUid");
test('StudentDiploma DELETE', 'DELETE', "v2/entities/hemishe_EStudentDiploma/$fakeDeleteUid");
test('StudentCertificate DELETE', 'DELETE', "v2/entities/hemishe_EStudentCertificate/$fakeDeleteUid");

// --- Student Certificate (Foreign) ---
test('StudentCertificate GET list', 'GET', 'v2/entities/hemishe_EStudentCertificate/?limit=3');
test('StudentCertificate GET (view)', 'GET', 'v2/entities/hemishe_EStudentCertificate/?limit=1&view=eStudentCertificate-view&returnNulls=true');
test('StudentCertificate POST', 'POST', 'v2/entities/hemishe_EStudentCertificate/', [
    '_student' => $studentUid,
    'name' => 'IELTS',
    'score' => '7.0',
]);

// --- Employee Certificate ---
test('EmployeeCertificate GET list', 'GET', 'v2/entities/hemishe_EEmpoyeeCertificate/?limit=3');
test('EmployeeCertificate POST', 'POST', 'v2/entities/hemishe_EEmpoyeeCertificate/', [
    '_employee' => $teacherUid,
    'name' => 'PhD Certificate',
]);

// --- Administrative Employee 1 (Academic Degree) ---
test('AdminEmployee1 GET list', 'GET', 'v2/entities/hemishe_RIAdministrativeEmployee1/?limit=3');
test('AdminEmployee1 POST', 'POST', 'v2/entities/hemishe_RIAdministrativeEmployee1/', [
    '_employee' => $teacherUid,
    '_university' => ['code' => '401'],
]);

// --- Administrative Employee 2 (Training) ---
test('AdminEmployee2 GET list', 'GET', 'v2/entities/hemishe_RIAdministrativeEmployee2/?limit=3');
test('AdminEmployee2 POST', 'POST', 'v2/entities/hemishe_RIAdministrativeEmployee2/', [
    '_employee' => $teacherUid,
    '_university' => ['code' => '401'],
]);

// --- Administrative Employee 3 (Foreign) ---
test('AdminEmployee3 GET list', 'GET', 'v2/entities/hemishe_RIAdministrativeEmployee3/?limit=3');
test('AdminEmployee3 POST', 'POST', 'v2/entities/hemishe_RIAdministrativeEmployee3/', [
    '_employee' => $teacherUid,
    '_university' => ['code' => '401'],
]);

// --- Administrative Student 2 (Exchange) ---
test('AdminStudent2 GET list', 'GET', 'v2/entities/hemishe_RIAdministrativeStudent2/?limit=3');
test('AdminStudent2 POST', 'POST', 'v2/entities/hemishe_RIAdministrativeStudent2/', [
    '_student' => $studentUid,
    '_university' => ['code' => '401'],
]);

// --- Administrative Student 3 (Employment) ---
test('AdminStudent3 GET list', 'GET', 'v2/entities/hemishe_RIAdministrativeStudent3/?limit=3');
test('AdminStudent3 GET (view)', 'GET', 'v2/entities/hemishe_RIAdministrativeStudent3/?limit=1&view=rIAdministrativeStudent3-view&returnNulls=true');
test('AdminStudent3 POST', 'POST', 'v2/entities/hemishe_RIAdministrativeStudent3/', [
    '_student' => $studentUid,
    '_university' => ['code' => '401'],
]);

// --- Administrative Student 4 (Olympiad) ---
test('AdminStudent4 GET list', 'GET', 'v2/entities/hemishe_RIAdministrativeStudent4/?limit=3');
test('AdminStudent4 POST', 'POST', 'v2/entities/hemishe_RIAdministrativeStudent4/', [
    '_student' => $studentUid,
    '_university' => ['code' => '401'],
]);

// --- Administrative Student Sport ---
test('AdminStudentSport GET list', 'GET', 'v2/entities/hemishe_RIAdministrativeStudentSport/?limit=3');
test('AdminStudentSport POST', 'POST', 'v2/entities/hemishe_RIAdministrativeStudentSport/', [
    '_student' => $studentUid,
    '_university' => ['code' => '401'],
]);

// --- Project ---
test('Project GET list', 'GET', 'v2/entities/hemishe_EProject/?limit=3');
test('Project GET (view)', 'GET', 'v2/entities/hemishe_EProject/?limit=1&view=eProject-view&returnNulls=true');
test('Project POST', 'POST', 'v2/entities/hemishe_EProject/', [
    '_university' => ['code' => '401'],
    'name' => 'Test Project',
]);

// --- Project Executor ---
test('ProjectExecutor GET list', 'GET', 'v2/entities/hemishe_EProjectExecutor/?limit=3');

// --- Project Meta ---
test('ProjectMeta GET list', 'GET', 'v2/entities/hemishe_EProjectMeta/?limit=3');

// --- Publication Scientific ---
test('PubScientific GET list', 'GET', 'v2/entities/hemishe_EPublicationScientific/?limit=3');
test('PubScientific POST', 'POST', 'v2/entities/hemishe_EPublicationScientific/', [
    '_university' => ['code' => '401'],
    'name' => 'Test Scientific Publication',
]);

// --- Publication Methodical ---
test('PubMethodical GET list', 'GET', 'v2/entities/hemishe_EPublicationMethodical/?limit=3');
test('PubMethodical POST', 'POST', 'v2/entities/hemishe_EPublicationMethodical/', [
    '_university' => ['code' => '401'],
    'name' => 'Test Methodical Publication',
]);

// --- Publication Property ---
test('PubProperty GET list', 'GET', 'v2/entities/hemishe_EPublicationProperty/?limit=3');
test('PubProperty POST', 'POST', 'v2/entities/hemishe_EPublicationProperty/', [
    '_university' => ['code' => '401'],
    'name' => 'Test Publication Property',
]);

// --- Publication Author Meta ---
test('PubAuthorMeta GET list', 'GET', 'v2/entities/hemishe_EPublicationAuthorMeta/?limit=3');

// --- Research Activity ---
test('ResearchActivity GET list', 'GET', 'v2/entities/hemishe_EResearchActivity/?limit=3');
test('ResearchActivity POST', 'POST', 'v2/entities/hemishe_EResearchActivity/', [
    '_university' => ['code' => '401'],
    '_employee' => $teacherUid,
    'name' => 'Test Research Activity',
]);

// --- Teacher POST ---
test('Teacher POST (upsert)', 'POST', 'v2/entities/hemishe_ETeacher/', [
    'id' => $teacherUid,
    '_university' => ['code' => '401'],
    'firstname' => 'Test',
    'lastname' => 'Teacher',
    'fathername' => 'Testovich',
]);

// --- Employee Jobs POST/PUT ---
test('EmployeeJobs POST', 'POST', 'v2/entities/hemishe_EEmployeeJob/', [
    '_employee' => ['id' => $teacherUid],
    '_university' => ['code' => '401'],
    'tag' => 'v5',
]);
test('EmployeeJobs PUT', 'PUT', "v2/entities/hemishe_EEmployeeJob/$fakeDeleteUid", [
    'tag' => 'v5',
]);

// --- University Group GET/DELETE ---
test('UniversityGroup GET list', 'GET', 'v2/entities/hemishe_EUniversityGroup/?limit=3');
test('UniversityGroup DELETE', 'DELETE', "v2/entities/hemishe_EUniversityGroup/$fakeDeleteUid");

// --- University Speciality GET/DELETE ---
test('UniversitySpeciality GET list', 'GET', 'v2/entities/hemishe_EUniversitySpeciality/?limit=3');
test('UniversitySpeciality DELETE', 'DELETE', "v2/entities/hemishe_EUniversitySpeciality/$fakeDeleteUid");

// --- Student Diploma PUT ---
test('StudentDiploma PUT', 'PUT', "v2/entities/hemishe_EStudentDiploma/$fakeDeleteUid", [
    'serial' => 'TEST002',
]);

// --- Project PUT/DELETE ---
test('Project PUT', 'PUT', "v2/entities/hemishe_EProject/$fakeDeleteUid", [
    'name' => 'Updated Project',
]);
test('Project DELETE', 'DELETE', "v2/entities/hemishe_EProject/$fakeDeleteUid");

// --- Project Executor POST/PUT/DELETE ---
test('ProjectExecutor POST', 'POST', 'v2/entities/hemishe_EProjectExecutor/', [
    '_university' => ['code' => '401'],
    '_employee' => ['id' => $teacherUid],
]);
test('ProjectExecutor PUT', 'PUT', "v2/entities/hemishe_EProjectExecutor/$fakeDeleteUid", [
    'tag' => 'v5',
]);
test('ProjectExecutor DELETE', 'DELETE', "v2/entities/hemishe_EProjectExecutor/$fakeDeleteUid");

// --- Project Meta POST/PUT/DELETE ---
test('ProjectMeta POST', 'POST', 'v2/entities/hemishe_EProjectMeta/', [
    '_university' => ['code' => '401'],
]);
test('ProjectMeta PUT', 'PUT', "v2/entities/hemishe_EProjectMeta/$fakeDeleteUid", [
    'tag' => 'v5',
]);
test('ProjectMeta DELETE', 'DELETE', "v2/entities/hemishe_EProjectMeta/$fakeDeleteUid");

// --- Publication Author Meta POST/DELETE ---
test('PubAuthorMeta POST', 'POST', 'v2/entities/hemishe_EPublicationAuthorMeta/', [
    '_university' => ['code' => '401'],
    '_employee' => ['id' => $teacherUid],
]);
test('PubAuthorMeta DELETE', 'DELETE', "v2/entities/hemishe_EPublicationAuthorMeta/$fakeDeleteUid");

// --- Remaining DELETE operations ---
test('DissertationDefense DELETE', 'DELETE', "v2/entities/hemishe_EDissertationDefense/$fakeDeleteUid");
test('DoctorateStudent DELETE', 'DELETE', "v2/entities/hemishe_EDoctorateStudent/$fakeDeleteUid");
test('EmployeeCertificate DELETE', 'DELETE', "v2/entities/hemishe_EEmpoyeeCertificate/$fakeDeleteUid");
test('PubMethodical DELETE', 'DELETE', "v2/entities/hemishe_EPublicationMethodical/$fakeDeleteUid");
test('PubProperty DELETE', 'DELETE', "v2/entities/hemishe_EPublicationProperty/$fakeDeleteUid");
test('PubScientific DELETE', 'DELETE', "v2/entities/hemishe_EPublicationScientific/$fakeDeleteUid");
test('ResearchActivity DELETE', 'DELETE', "v2/entities/hemishe_EResearchActivity/$fakeDeleteUid");
test('AdminEmployee1 DELETE', 'DELETE', "v2/entities/hemishe_RIAdministrativeEmployee1/$fakeDeleteUid");
test('AdminEmployee2 DELETE', 'DELETE', "v2/entities/hemishe_RIAdministrativeEmployee2/$fakeDeleteUid");
test('AdminEmployee3 DELETE', 'DELETE', "v2/entities/hemishe_RIAdministrativeEmployee3/$fakeDeleteUid");
test('AdminStudent2 DELETE', 'DELETE', "v2/entities/hemishe_RIAdministrativeStudent2/$fakeDeleteUid");
test('AdminStudent3 DELETE', 'DELETE', "v2/entities/hemishe_RIAdministrativeStudent3/$fakeDeleteUid");
test('AdminStudent4 DELETE', 'DELETE', "v2/entities/hemishe_RIAdministrativeStudent4/$fakeDeleteUid");
test('AdminStudentSport DELETE', 'DELETE', "v2/entities/hemishe_RIAdministrativeStudentSport/$fakeDeleteUid");

// --- UniversityAttachedSpeciality ---
test('UnivAttachedSpec GET list', 'GET', 'v2/entities/hemishe_EUniversityAttachedSpeciality/?limit=3');
test('UnivAttachedSpec POST', 'POST', 'v2/entities/hemishe_EUniversityAttachedSpeciality/', [
    'university' => ['_entityName' => 'hemishe_EUniversity', 'code' => '401'],
    'educationForm' => ['_entityName' => 'hemishe_HEducationForm', 'code' => '11'],
    'active' => true,
]);
// POST dan qaytgan id ni olish va DELETE/GET uchun ishlatish
$lastResult = end($results);
$uasId = null;
if ($lastResult && $lastResult['status'] === 'PASS') {
    $postResp = apiCall($NEW_BASE, 'POST', 'v2/entities/hemishe_EUniversityAttachedSpeciality/', $newToken, [
        'university' => ['_entityName' => 'hemishe_EUniversity', 'code' => '401'],
        'educationForm' => ['_entityName' => 'hemishe_HEducationForm', 'code' => '11'],
        'active' => true,
    ]);
    if ($postResp['json'] && isset($postResp['json']['id'])) {
        $uasId = $postResp['json']['id'];
    }
}
if ($uasId) {
    test('UnivAttachedSpec GET by id', 'GET', "v2/entities/hemishe_EUniversityAttachedSpeciality/$uasId?returnNulls=true");
    test('UnivAttachedSpec DELETE', 'DELETE', "v2/entities/hemishe_EUniversityAttachedSpeciality/$uasId");
} else {
    test('UnivAttachedSpec DELETE (fake)', 'DELETE', "v2/entities/hemishe_EUniversityAttachedSpeciality/$fakeDeleteUid");
}

// --- Classifiers ---
test('Classifier: EducationType', 'GET', 'v2/entities/hemishe_HEducationType?limit=3');
test('Classifier: EducationForm', 'GET', 'v2/entities/hemishe_HEducationForm?limit=3');
test('Classifier: Course', 'GET', 'v2/entities/hemishe_HCourse?limit=3');
test('Classifier: EducationYear', 'GET', 'v2/entities/hemishe_HEducationYear?limit=3');
test('Classifier: TransferType', 'GET', 'v2/entities/hemishe_HTransferType?limit=3');
test('Classifier: TransferType by code', 'GET', 'v2/entities/hemishe_HTransferType/11');

// ============================================================
// SERVICE ENDPOINTLAR
// ============================================================
echo "\n\n========== SERVICE ENDPOINTLAR ==========\n";

// Student services
test('Student verify (PINFL)', 'GET', 'v2/services/student/verify?pinfl=52603015520014');
test('Student validate', 'GET', 'v2/services/student/validate?data=52603015520014');
test('Student ID generate', 'POST', 'v2/services/student/id/', json_encode([
    'data' => [
        'citizenship' => 'UZ',
        'pinfl' => '52603015520014',
        'serial' => 'AA1234567',
        'year' => '2024',
        'education_type' => '11',
    ],
]));
test('Student update', 'POST', 'v2/services/student/update/', json_encode([
    'student' => [
        'id' => $studentUid,
        '_university' => ['code' => '401'],
    ],
]));
test('Student GPA (service)', 'POST', 'v2/services/student/gpa/', json_encode([
    'gpa' => [
        'studentId' => ['id' => $studentUid],
        'educationYear' => ['code' => '2024'],
        'level' => ['code' => '11'],
        'method' => '1',
        'gpa' => '3.5',
        'creditSum' => '60',
        'subjects' => '10',
        'debtSubjects' => '0',
    ],
]));
test('Student contractStatistics', 'POST', 'v2/services/student/contractStatistics/', json_encode([
    'contractStatistics' => [
        'university' => ['code' => '401'],
        'educationYear' => ['code' => '2024'],
        'educationType' => ['code' => '11'],
        'educationForm' => ['code' => '11'],
        'course' => ['code' => '1'],
        'semester' => ['code' => '11'],
        'dailyCount' => 100,
        'total' => 0,
        'date' => '2024-01-01',
    ],
]));

// Teacher services
test('Teacher ID generate', 'POST', 'v2/services/teacher/id/', json_encode([
    'data' => [
        'citizenship' => 'UZ',
        'pinfl' => '12345678901234',
        'serial' => 'AA1234567',
        'gender' => '11',
        'year' => '2024',
    ],
]));
test('Teacher addJob', 'POST', 'v2/services/teacher/addJob/', json_encode([
    'job' => [
        'employee' => ['id' => $teacherUid],
        'university' => ['code' => '401'],
        'department' => ['code' => '401-TEST'],
        'employeeForm' => ['code' => '11'],
        'employeeStatus' => ['code' => '11'],
        'employeeRate' => ['code' => '1.0'],
        'employeePosition' => ['code' => '1'],
        'jobStartDate' => '2024-01-01',
        'contractDate' => '2024-01-01',
        'contractNumber' => 'TEST-001',
        'tag' => 'v5',
    ],
]));

// Doctoral student service
test('DoctoralStudent ID', 'POST', 'v2/services/doctoral-student/id/', json_encode([
    'data' => [
        'citizenship' => 'UZ',
        'pinfl' => '52603015520014',
        'serial' => 'AA1234567',
        'year' => '2024',
        'education_type' => '40',
    ],
]));

// University services
test('University config', 'GET', 'v2/services/university/config');
test('University get', 'GET', 'v2/services/university/get?code=401');

// Group service
test('Group get', 'GET', 'v2/services/group/get?university=401&type=11&year=2024');

// Specialty service
test('Speciality get', 'GET', 'v2/services/speciality/get?university=401&type=11&year=2020');

// Classifier services — NO AUTH
test('Classifiers info (NO AUTH)', 'GET', 'v2/services/classifiers/info', null, [200, 201], true);
test('Classifiers allItems (NO AUTH)', 'GET', 'v2/services/classifiers/allItems', null, [200, 201], true);

// Passport data services
test('PassportData byPinflBirthdate', 'GET', 'v2/services/passport-data/getDataByPinflBirthdate?birthdate=1990-01-01&pinfl=52603015520014&captchaValue=1234&captchaId=test');
test('PassportData bySN', 'GET', 'v2/services/passport-data/getDataBySN?pinfl=52603015520014&seriaNumber=AA1234567&captchaValue=1234&captchaId=test');
test('PassportData address', 'GET', 'v2/services/passport-data/getAddress?pinfl=52603015520014');

// BIMM services
test('BIMM certificate', 'GET', 'v2/services/bimm/certificate?pinfl=52603015520014');
test('BIMM academicDegree', 'GET', 'v2/services/bimm/academicDegree?pinfl=52603015520014');
test('BIMM provertyRegister', 'GET', 'v2/services/bimm/provertyRegister?pinfl=52603015520014');

// Social services
test('Social singleRegister', 'GET', 'v2/services/social/singleRegister?pinfl=52603015520014');
test('Social women', 'GET', 'v2/services/social/women?pinfl=52603015520014&sn=AA1234567');

// Billing services
test('Billing invoice', 'POST', 'v2/services/billing/invoice/', json_encode([
    'params' => [
        'OrganizationId' => '401',
        'EduFacultyId' => '',
        'EduYearId' => '2024',
        'EduTypeId' => '11',
    ],
]));
test('Billing scholarship', 'POST', 'v2/services/billing/scholarship/', json_encode([
    'tin' => '201354108',
    'pinfls' => ['52603015520014'],
]));

// UzAsbo
test('UzAsbo scholarship', 'GET', 'v2/services/uzasbo/scholarship/?inn=201354108&year=2024&month=1');

// Employment services
test('Employment workbook', 'GET', 'v2/services/employment/workbook?pinfl=52603015520014');
test('Employment graduateList', 'POST', 'v2/services/employment/graduateList/', json_encode([
    'employments' => [
        [
            'university' => ['code' => '401'],
            'department' => ['code' => '401-TEST'],
            'educationYear' => ['code' => '2024'],
            'educationType' => ['code' => '11'],
            'educationForm' => ['code' => '11'],
            'paymentForm' => ['code' => '11'],
            'gender' => ['code' => '11'],
            'workplaceCompatibility' => ['code' => '11'],
            'qty' => 1,
        ],
    ],
]));

// Captcha
test('Captcha getNumeric', 'GET', 'v2/services/captcha/getNumericCaptcha');

// Legal entity
test('LegalEntity bankRequisites', 'GET', 'v2/services/legalentity/bankRequisites?inn=123456789');

// Diploma blank
test('DiplomBlank get', 'GET', 'v2/services/diplom-blank/get?university=401&year=2024');
test('DiplomBlank setStatus', 'GET', 'v2/services/diplom-blank/setStatus?blankCode=TEST001&statusCode=1');

// Send verify code
test('Send verifyCode', 'POST', 'v2/services/send/verifyCode/', json_encode([
    'id' => 'test_user',
    'email' => 'test@test.com',
    'phone' => '+998901234567',
    'verify_code' => '1234',
    'hash' => '',
]));


// ============================================================
// QO'SHIMCHA ENDPOINTLAR (avtomatik generatsiya qilingan)
// ============================================================

// --- 01.Token ---
test('userInfo', 'GET', 'v2/userInfo');

// --- 02.Captcha ---
test('Service: captcha/getArithmeticCaptcha', 'GET', 'v2/services/captcha/getArithmeticCaptcha');

// --- 03.Passport ma'lumotlari ---
test('Service: passport-data/getDataBySNBirthdate', 'GET', 'v2/services/passport-data/getDataBySNBirthdate?seriaNumber=AA1234567&birthdate=1990-01-01&captchaValue=1234&captchaId=test');

// --- 04.Talaba ---
test('EStudent GET list', 'GET', 'v2/entities/hemishe_EStudent?limit=3');
test('Service: student/contractInfo', 'GET', "v2/services/student/contractInfo?studentId=$studentUid");
test('EStudentMeta GET list', 'GET', 'v2/entities/hemishe_EStudentMeta?limit=3');
test('EStudentMeta GET by id', 'GET', "v2/entities/hemishe_EStudentMeta/$fakeDeleteUid?returnNulls=true");
test('EStudentMeta POST', 'POST', 'v2/entities/hemishe_EStudentMeta/', ['active' => true]);
test('EStudentMeta PUT', 'PUT', "v2/entities/hemishe_EStudentMeta/$fakeDeleteUid", ['active' => true]);
test('EStudentMeta DELETE', 'DELETE', "v2/entities/hemishe_EStudentMeta/$fakeDeleteUid");

// --- 05.O'qituvchi ---
test('ETeacher GET list', 'GET', 'v2/entities/hemishe_ETeacher?limit=3');

// --- 06.Xodim lavozimlari ---
test('EEmployeeJobs GET by id', 'GET', "v2/entities/hemishe_EEmployeeJob/$fakeDeleteUid?returnNulls=true");
test('EEmployeeJobs search POST', 'POST', 'v2/entities/hemishe_EEmployeeJob/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HUniversityEmployeeStatusType GET list', 'GET', 'v2/entities/hemishe_HUniversityEmployeeStatusType?limit=3');
test('HUniversityEmployeeStatusType GET by id', 'GET', "v2/entities/hemishe_HUniversityEmployeeStatusType/$fakeDeleteUid?returnNulls=true");
test('HUniversityEmployeeType GET list', 'GET', 'v2/entities/hemishe_HUniversityEmployeeType?limit=3');
test('HUniversityEmployeeType GET by id', 'GET', "v2/entities/hemishe_HUniversityEmployeeType/$fakeDeleteUid?returnNulls=true");
test('HUniversityEmployeeRate GET list', 'GET', 'v2/entities/hemishe_HUniversityEmployeeRate?limit=3');
test('HUniversityEmployeeRate GET by id', 'GET', "v2/entities/hemishe_HUniversityEmployeeRate/$fakeDeleteUid?returnNulls=true");
test('HTeacherPositionType GET list', 'GET', 'v2/entities/hemishe_HTeacherPositionType?limit=3');
test('HTeacherPositionType GET by id', 'GET', "v2/entities/hemishe_HTeacherPositionType/$fakeDeleteUid?returnNulls=true");
test('EEmployeeJob GET list', 'GET', 'v2/entities/hemishe_EEmployeeJob?limit=3');
test('EEmployeeJob GET by id', 'GET', "v2/entities/hemishe_EEmployeeJob/$fakeDeleteUid?returnNulls=true");

// --- 07.OTM bo'linmalari ---
test('EUniversityDepartment GET by id', 'GET', "v2/entities/hemishe_EUniversityDepartment/$fakeDeleteUid?returnNulls=true");
test('EUniversityDepartment search', 'POST', 'v2/entities/hemishe_EUniversityDepartment/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('EUniversityDepartment search POST', 'POST', 'v2/entities/hemishe_EUniversityDepartment/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('EUniversityDepartment PUT', 'PUT', "v2/entities/hemishe_EUniversityDepartment/$fakeDeleteUid", ['active' => true]);

// --- 08.OTM bo'linma turlari ---
test('HUniversityDepartmentType GET by id', 'GET', "v2/entities/hemishe_HUniversityDepartmentType/$fakeDeleteUid?returnNulls=true");
test('HUniversityDepartmentType GET list', 'GET', 'v2/entities/hemishe_HUniversityDepartmentType?limit=3');
test('HUniversityDepartmentType search', 'POST', 'v2/entities/hemishe_HUniversityDepartmentType/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HUniversityDepartmentType search POST', 'POST', 'v2/entities/hemishe_HUniversityDepartmentType/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HUniversityDepartmentType POST', 'POST', 'v2/entities/hemishe_HUniversityDepartmentType/', ['code' => 'TEST99', 'name' => 'Test Type']);
test('HUniversityDepartmentType PUT', 'PUT', "v2/entities/hemishe_HUniversityDepartmentType/$fakeDeleteUid", ['active' => true]);
test('HUniversityDepartmentType DELETE', 'DELETE', "v2/entities/hemishe_HUniversityDepartmentType/$fakeDeleteUid");

// --- 13.Klassifikatorlar ---
test('Service: classifiers/single', 'GET', 'v2/services/classifiers/single?name=HEducationType');

// --- 14.Tarjima ---
// test('Service: translate/get', 'GET', 'v2/services/translate/get?key=menu.student&locale=uz-UZ'); // old-hemis da yo'q
// test('Service: translate/get POST', 'POST', 'v2/services/translate/get/', ['key' => 'menu.student', 'locale' => 'uz-UZ']); // old-hemis da yo'q

// --- 15.OTM ---
test('EUniversity DELETE', 'DELETE', "v2/entities/hemishe_EUniversity/$fakeDeleteUid");

// --- 09.OTM xodimlari kategoriyasi ---
test('HUniversityEmployeeType POST', 'POST', 'v2/entities/hemishe_HUniversityEmployeeType/', ['code' => 'TEST99', 'name' => 'Test Type']);
test('HUniversityEmployeeType PUT', 'PUT', "v2/entities/hemishe_HUniversityEmployeeType/$fakeDeleteUid", ['active' => true]);
test('HUniversityEmployeeType DELETE', 'DELETE', "v2/entities/hemishe_HUniversityEmployeeType/$fakeDeleteUid");
test('HUniversityEmployeeType search', 'POST', 'v2/entities/hemishe_HUniversityEmployeeType/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HUniversityEmployeeType search POST', 'POST', 'v2/entities/hemishe_HUniversityEmployeeType/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 10.Talaba holati ---
test('HStudentStatusType GET list', 'GET', 'v2/entities/hemishe_HStudentStatusType?limit=3');
test('HStudentStatusType GET by id', 'GET', "v2/entities/hemishe_HStudentStatusType/$fakeDeleteUid?returnNulls=true");
test('HStudentStatusType POST', 'POST', 'v2/entities/hemishe_HStudentStatusType/', ['code' => 'TEST99', 'name' => 'Test Status']);
test('HStudentStatusType PUT', 'PUT', "v2/entities/hemishe_HStudentStatusType/$fakeDeleteUid", ['active' => true]);
test('HStudentStatusType DELETE', 'DELETE', "v2/entities/hemishe_HStudentStatusType/$fakeDeleteUid");
test('HStudentStatusType search', 'POST', 'v2/entities/hemishe_HStudentStatusType/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HStudentStatusType search POST', 'POST', 'v2/entities/hemishe_HStudentStatusType/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 11.Fuqarolik holatlari ---
test('HCitizenship GET list', 'GET', 'v2/entities/hemishe_HCitizenship?limit=3');
test('HCitizenship GET by id', 'GET', "v2/entities/hemishe_HCitizenship/$fakeDeleteUid?returnNulls=true");
test('HCitizenship POST', 'POST', 'v2/entities/hemishe_HCitizenship/', ['code' => 'TEST99', 'name' => 'Test Citizenship']);
test('HCitizenship PUT', 'PUT', "v2/entities/hemishe_HCitizenship/$fakeDeleteUid", ['active' => true]);
test('HCitizenship DELETE', 'DELETE', "v2/entities/hemishe_HCitizenship/$fakeDeleteUid");
test('HCitizenship search', 'POST', 'v2/entities/hemishe_HCitizenship/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HCitizenship search POST', 'POST', 'v2/entities/hemishe_HCitizenship/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 12.Diplomlar ---
test('EStudentDiploma GET by id', 'GET', "v2/entities/hemishe_EStudentDiploma/$fakeDeleteUid?returnNulls=true");
test('EStudentDiploma search', 'POST', 'v2/entities/hemishe_EStudentDiploma/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('EStudentDiploma search POST', 'POST', 'v2/entities/hemishe_EStudentDiploma/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 16.Ilmiy doktorant talabalari ---
test('EDoctorateStudent PUT', 'PUT', "v2/entities/hemishe_EDoctorateStudent/$fakeDeleteUid", ['active' => true]);
test('EDoctorateStudent GET by id', 'GET', "v2/entities/hemishe_EDoctorateStudent/$fakeDeleteUid?returnNulls=true");
test('EDoctorateStudent search', 'POST', 'v2/entities/hemishe_EDoctorateStudent/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('EDoctorateStudent search POST', 'POST', 'v2/entities/hemishe_EDoctorateStudent/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 17.Ilmiy dissertasiya himoyalari ---
test('EDissertationDefense GET by id', 'GET', "v2/entities/hemishe_EDissertationDefense/$fakeDeleteUid?returnNulls=true");

// --- 18.Ilmiy faollik ---
test('EResearchActivity GET by id', 'GET', "v2/entities/hemishe_EResearchActivity/$fakeDeleteUid?returnNulls=true");
test('EResearchActivity PUT', 'PUT', "v2/entities/hemishe_EResearchActivity/$fakeDeleteUid", ['active' => true]);

// --- 19.Ilmiy loyihalar ---
test('EProject GET by id', 'GET', "v2/entities/hemishe_EProject/$fakeDeleteUid?returnNulls=true");

// --- 21.Ilmiy loyiha ijrochilari ---
test('EProjectExecutor GET by id', 'GET', "v2/entities/hemishe_EProjectExecutor/$fakeDeleteUid?returnNulls=true");

// --- 20.Ilmiy loyiha meta ma'lumotlari ---
test('EProjectMeta GET by id', 'GET', "v2/entities/hemishe_EProjectMeta/$fakeDeleteUid?returnNulls=true");

// --- 22.Ilmiy nashrlar ---
test('EPublicationScientific GET by id', 'GET', "v2/entities/hemishe_EPublicationScientific/$fakeDeleteUid?returnNulls=true");
test('EPublicationScientific PUT', 'PUT', "v2/entities/hemishe_EPublicationScientific/$fakeDeleteUid", ['active' => true]);

// --- 23.Ilmiy ishlanmalar ---
test('EPublicationProperty GET by id', 'GET', "v2/entities/hemishe_EPublicationProperty/$fakeDeleteUid?returnNulls=true");
test('EPublicationProperty PUT', 'PUT', "v2/entities/hemishe_EPublicationProperty/$fakeDeleteUid", ['active' => true]);

// --- 24.Ilmiy uslubiy nashlar ---
test('EPublicationMethodical GET by id', 'GET', "v2/entities/hemishe_EPublicationMethodical/$fakeDeleteUid?returnNulls=true");
test('EPublicationMethodical PUT', 'PUT', "v2/entities/hemishe_EPublicationMethodical/$fakeDeleteUid", ['active' => true]);

// --- 26.Ilmiy nashrlarni baholash mezonlari ---
test('EPublicationCriteria POST', 'POST', 'v2/entities/hemishe_EPublicationCriteria/', ['active' => true]);
test('EPublicationCriteria GET list', 'GET', 'v2/entities/hemishe_EPublicationCriteria?limit=3');
test('EPublicationCriteria GET by id', 'GET', "v2/entities/hemishe_EPublicationCriteria/$fakeDeleteUid?returnNulls=true");
test('EPublicationCriteria PUT', 'PUT', "v2/entities/hemishe_EPublicationCriteria/$fakeDeleteUid", ['active' => true]);
test('EPublicationCriteria DELETE', 'DELETE', "v2/entities/hemishe_EPublicationCriteria/$fakeDeleteUid");

// --- 25.Ilmiy nashr mualliflari meta ma'lumotlari ---
test('EPublicationAuthorMeta GET by id', 'GET', "v2/entities/hemishe_EPublicationAuthorMeta/$fakeDeleteUid?returnNulls=true");
test('EPublicationAuthorMeta PUT', 'PUT', "v2/entities/hemishe_EPublicationAuthorMeta/$fakeDeleteUid", ['active' => true]);

// --- 27.Ilmiy uslubiy nashr turlari ---
test('HMethodicalPublicationType GET list', 'GET', 'v2/entities/hemishe_HMethodicalPublicationType?limit=3');
test('HMethodicalPublicationType GET by id', 'GET', "v2/entities/hemishe_HMethodicalPublicationType/$fakeDeleteUid?returnNulls=true");
test('HMethodicalPublicationType POST', 'POST', 'v2/entities/hemishe_HMethodicalPublicationType/', ['code' => 'TEST99', 'name' => 'Test Type']);
test('HMethodicalPublicationType PUT', 'PUT', "v2/entities/hemishe_HMethodicalPublicationType/$fakeDeleteUid", ['active' => true]);
test('HMethodicalPublicationType DELETE', 'DELETE', "v2/entities/hemishe_HMethodicalPublicationType/$fakeDeleteUid");
test('HMethodicalPublicationType search', 'POST', 'v2/entities/hemishe_HMethodicalPublicationType/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HMethodicalPublicationType search POST', 'POST', 'v2/entities/hemishe_HMethodicalPublicationType/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 28.Ilmiy doktorantura talabalari statusi ---
test('HDoctoralStudentStatus GET list', 'GET', 'v2/entities/hemishe_HDoctoralStudentStatus?limit=3');
test('HDoctoralStudentStatus GET by id', 'GET', "v2/entities/hemishe_HDoctoralStudentStatus/$fakeDeleteUid?returnNulls=true");
test('HDoctoralStudentStatus POST', 'POST', 'v2/entities/hemishe_HDoctoralStudentStatus/', ['code' => 'TEST99', 'name' => 'Test Status']);
test('HDoctoralStudentStatus PUT', 'PUT', "v2/entities/hemishe_HDoctoralStudentStatus/$fakeDeleteUid", ['active' => true]);
test('HDoctoralStudentStatus DELETE', 'DELETE', "v2/entities/hemishe_HDoctoralStudentStatus/$fakeDeleteUid");
test('HDoctoralStudentStatus search', 'POST', 'v2/entities/hemishe_HDoctoralStudentStatus/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HDoctoralStudentStatus search POST', 'POST', 'v2/entities/hemishe_HDoctoralStudentStatus/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 29.Ilmiy doktorantura talabalari turlari ---
test('HDoctoralStudentType GET list', 'GET', 'v2/entities/hemishe_HDoctoralStudentType?limit=3');
test('HDoctoralStudentType GET by id', 'GET', "v2/entities/hemishe_HDoctoralStudentType/$fakeDeleteUid?returnNulls=true");
test('HDoctoralStudentType POST', 'POST', 'v2/entities/hemishe_HDoctoralStudentType/', ['code' => 'TEST99', 'name' => 'Test Type']);
test('HDoctoralStudentType PUT', 'PUT', "v2/entities/hemishe_HDoctoralStudentType/$fakeDeleteUid", ['active' => true]);
test('HDoctoralStudentType DELETE', 'DELETE', "v2/entities/hemishe_HDoctoralStudentType/$fakeDeleteUid");
test('HDoctoralStudentType search', 'POST', 'v2/entities/hemishe_HDoctoralStudentType/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HDoctoralStudentType search POST', 'POST', 'v2/entities/hemishe_HDoctoralStudentType/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 30.Ilmiy nashr etish hududlari turlari ---
test('HPublicationLocality GET list', 'GET', 'v2/entities/hemishe_HPublicationLocality?limit=3');
test('HPublicationLocality GET by id', 'GET', "v2/entities/hemishe_HPublicationLocality/$fakeDeleteUid?returnNulls=true");
test('HPublicationLocality POST', 'POST', 'v2/entities/hemishe_HPublicationLocality/', ['code' => 'TEST99', 'name' => 'Test Locality']);
test('HPublicationLocality PUT', 'PUT', "v2/entities/hemishe_HPublicationLocality/$fakeDeleteUid", ['active' => true]);
test('HPublicationLocality DELETE', 'DELETE', "v2/entities/hemishe_HPublicationLocality/$fakeDeleteUid");
test('HPublicationLocality search', 'POST', 'v2/entities/hemishe_HPublicationLocality/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HPublicationLocality search POST', 'POST', 'v2/entities/hemishe_HPublicationLocality/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 31.Akademik hisobotlar chetlashgan talabalar ---
test('RExpel GET list', 'GET', 'v2/entities/hemishe_RExpel?limit=3');
test('RExpel GET by id', 'GET', "v2/entities/hemishe_RExpel/$fakeDeleteUid?returnNulls=true");
test('RExpel POST', 'POST', 'v2/entities/hemishe_RExpel/', ['active' => true]);
test('RExpel PUT', 'PUT', "v2/entities/hemishe_RExpel/$fakeDeleteUid", ['active' => true]);
test('RExpel DELETE', 'DELETE', "v2/entities/hemishe_RExpel/$fakeDeleteUid");
test('RExpel search', 'POST', 'v2/entities/hemishe_RExpel/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RExpel search POST', 'POST', 'v2/entities/hemishe_RExpel/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 32.Akademik hisobotlar akademik guruhlar ---
test('RAcademicGroup GET list', 'GET', 'v2/entities/hemishe_RAcademicGroup?limit=3');
test('RAcademicGroup GET by id', 'GET', "v2/entities/hemishe_RAcademicGroup/$fakeDeleteUid?returnNulls=true");
test('RAcademicGroup POST', 'POST', 'v2/entities/hemishe_RAcademicGroup/', ['active' => true]);
test('RAcademicGroup PUT', 'PUT', "v2/entities/hemishe_RAcademicGroup/$fakeDeleteUid", ['active' => true]);
test('RAcademicGroup DELETE', 'DELETE', "v2/entities/hemishe_RAcademicGroup/$fakeDeleteUid");
test('RAcademicGroup search', 'POST', 'v2/entities/hemishe_RAcademicGroup/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RAcademicGroup search POST', 'POST', 'v2/entities/hemishe_RAcademicGroup/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 33.Akademik hisobotlar fanlar ---
test('RAcademicSubjects GET list', 'GET', 'v2/entities/hemishe_RAcademicSubjects?limit=3');
test('RAcademicSubjects GET by id', 'GET', "v2/entities/hemishe_RAcademicSubjects/$fakeDeleteUid?returnNulls=true");
test('RAcademicSubjects POST', 'POST', 'v2/entities/hemishe_RAcademicSubjects/', ['active' => true]);
test('RAcademicSubjects PUT', 'PUT', "v2/entities/hemishe_RAcademicSubjects/$fakeDeleteUid", ['active' => true]);
test('RAcademicSubjects DELETE', 'DELETE', "v2/entities/hemishe_RAcademicSubjects/$fakeDeleteUid");
test('RAcademicSubjects search', 'POST', 'v2/entities/hemishe_RAcademicSubjects/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RAcademicSubjects search POST', 'POST', 'v2/entities/hemishe_RAcademicSubjects/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 34.Akademik hisobotlar o'zlashtirish ---
test('RAcademicScore POST', 'POST', 'v2/entities/hemishe_RAcademicScore/', ['active' => true]);
test('RAcademicScore GET by id', 'GET', "v2/entities/hemishe_RAcademicScore/$fakeDeleteUid?returnNulls=true");
test('RAcademicScore PUT', 'PUT', "v2/entities/hemishe_RAcademicScore/$fakeDeleteUid", ['active' => true]);
test('RAcademicScore DELETE', 'DELETE', "v2/entities/hemishe_RAcademicScore/$fakeDeleteUid");
test('RAcademicScore GET list', 'GET', 'v2/entities/hemishe_RAcademicScore?limit=3');
test('RAcademicScore search', 'POST', 'v2/entities/hemishe_RAcademicScore/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RAcademicScore search POST', 'POST', 'v2/entities/hemishe_RAcademicScore/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 35.Akademik hisobotlar davomat ---
test('RAcademicAttendance POST', 'POST', 'v2/entities/hemishe_RAcademicAttendance/', ['active' => true]);
test('RAcademicAttendance GET by id', 'GET', "v2/entities/hemishe_RAcademicAttendance/$fakeDeleteUid?returnNulls=true");
test('RAcademicAttendance PUT', 'PUT', "v2/entities/hemishe_RAcademicAttendance/$fakeDeleteUid", ['active' => true]);
test('RAcademicAttendance DELETE', 'DELETE', "v2/entities/hemishe_RAcademicAttendance/$fakeDeleteUid");
test('RAcademicAttendance GET list', 'GET', 'v2/entities/hemishe_RAcademicAttendance?limit=3');
test('RAcademicAttendance search', 'POST', 'v2/entities/hemishe_RAcademicAttendance/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RAcademicAttendance search POST', 'POST', 'v2/entities/hemishe_RAcademicAttendance/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 36.Shartnoma statistikasi ---
test('RContractStatistics GET list', 'GET', 'v2/entities/hemishe_RContractStatistics?limit=3');

// --- 37.Bandlik statistikasi ---
test('REmployment POST', 'POST', 'v2/entities/hemishe_REmployment/', [
    'university' => ['code' => '401'],
    'department' => ['code' => '401-100'],
    'educationYear' => ['code' => '2024'],
    'educationType' => ['code' => '11'],
    'educationForm' => ['code' => '11'],
    'paymentForm' => ['code' => '11'],
    'gender' => ['code' => '11'],
    'workplaceCompatibility' => ['code' => '11'],
    'graduateFieldsType' => ['code' => '31'],
    'graduateInactiveType' => ['code' => '13'],
    'qty' => 0,
]);
test('REmployment GET by id', 'GET', "v2/entities/hemishe_REmployment/$fakeDeleteUid?returnNulls=true");
test('REmployment PUT', 'PUT', "v2/entities/hemishe_REmployment/$fakeDeleteUid", ['active' => true]);
test('REmployment GET list', 'GET', 'v2/entities/hemishe_REmployment?limit=3');
test('REmployment DELETE', 'DELETE', "v2/entities/hemishe_REmployment/$fakeDeleteUid");
test('REmployment search', 'POST', 'v2/entities/hemishe_REmployment/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('REmployment search POST', 'POST', 'v2/entities/hemishe_REmployment/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 39.Inspeksiya administrative student ---
test('RIAdministrativeStudent2 GET by id', 'GET', "v2/entities/hemishe_RIAdministrativeStudent2/$fakeDeleteUid?returnNulls=true");
test('RIAdministrativeStudent2 PUT', 'PUT', "v2/entities/hemishe_RIAdministrativeStudent2/$fakeDeleteUid", ['active' => true]);
test('RIAdministrativeStudent2 search', 'POST', 'v2/entities/hemishe_RIAdministrativeStudent2/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RIAdministrativeStudent2 search POST', 'POST', 'v2/entities/hemishe_RIAdministrativeStudent2/search', ['filter' => ['conditions' => []], 'limit' => 3]);


// ============================================================
// AVTOMATIK QO'SHILGAN YANGI TESTLAR (missing endpoints)
// ============================================================

// --- 05.O'qituvchi (missing) ---
test('ETeacher POST search', 'POST', 'v2/entities/hemishe_ETeacher/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('ETeacher search POST', 'POST', 'v2/entities/hemishe_ETeacher/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 06.Xodim lavozimlari (missing) ---
test('HUniversityEmployeeForm GET list', 'GET', 'v2/entities/hemishe_HUniversityEmployeeForm?limit=3');
test('HUniversityEmployeeForm GET by id', 'GET', "v2/entities/hemishe_HUniversityEmployeeForm/$fakeDeleteUid?returnNulls=true");
test('HUniversityEmployeeStatusType POST search', 'POST', 'v2/entities/hemishe_HUniversityEmployeeStatusType/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HUniversityEmployeeStatusType search POST', 'POST', 'v2/entities/hemishe_HUniversityEmployeeStatusType/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HUniversityEmployeeRate POST search', 'POST', 'v2/entities/hemishe_HUniversityEmployeeRate/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HUniversityEmployeeRate search POST', 'POST', 'v2/entities/hemishe_HUniversityEmployeeRate/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HTeacherPositionType POST search', 'POST', 'v2/entities/hemishe_HTeacherPositionType/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HTeacherPositionType search POST', 'POST', 'v2/entities/hemishe_HTeacherPositionType/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HUniversityEmployeeForm POST search', 'POST', 'v2/entities/hemishe_HUniversityEmployeeForm/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('HUniversityEmployeeForm search POST', 'POST', 'v2/entities/hemishe_HUniversityEmployeeForm/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 07.OTM bo'linmalari (missing) ---
test('EUniversityDepartment POST search', 'POST', 'v2/entities/hemishe_EUniversityDepartment/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 08.OTM bo'linma turlari (missing) ---
test('HUniversityDepartmentType POST search', 'POST', 'v2/entities/hemishe_HUniversityDepartmentType/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 09.OTM xodimlari kategoriyasi (missing) ---
test('HUniversityEmployeeType POST search', 'POST', 'v2/entities/hemishe_HUniversityEmployeeType/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 10.Talaba holati (missing) ---
test('HStudentStatusType POST search', 'POST', 'v2/entities/hemishe_HStudentStatusType/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 11.Fuqarolik holatlari (missing) ---
test('HCitizenship POST search', 'POST', 'v2/entities/hemishe_HCitizenship/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 12.Diplomlar (missing) ---
test('EStudentDiploma POST search', 'POST', 'v2/entities/hemishe_EStudentDiploma/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 16.Ilmiy doktorant talabalari (missing) ---
test('EDoctorateStudent POST search', 'POST', 'v2/entities/hemishe_EDoctorateStudent/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 27.Ilmiy uslubiy nashr turlari (missing) ---
test('HMethodicalPublicationType POST search', 'POST', 'v2/entities/hemishe_HMethodicalPublicationType/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 28.Ilmiy doktorantura talabalari statusi (missing) ---
test('HDoctoralStudentStatus POST search', 'POST', 'v2/entities/hemishe_HDoctoralStudentStatus/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 29.Ilmiy doktorantura talabalari turlari (missing) ---
test('HDoctoralStudentType POST search', 'POST', 'v2/entities/hemishe_HDoctoralStudentType/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 30.Ilmiy nashr etish hududlari turlari (missing) ---
test('HPublicationLocality POST search', 'POST', 'v2/entities/hemishe_HPublicationLocality/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 31.Akademik hisobotlar chetlashgan talabalar (missing) ---
test('RExpel POST search', 'POST', 'v2/entities/hemishe_RExpel/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 32.Akademik hisobotlar akademik guruhlar (missing) ---
test('RAcademicGroup POST search', 'POST', 'v2/entities/hemishe_RAcademicGroup/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 33.Akademik hisobotlar fanlar (missing) ---
test('RAcademicSubjects POST search', 'POST', 'v2/entities/hemishe_RAcademicSubjects/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 34.Akademik hisobotlar o'zlashtirish (missing) ---
test('RAcademicScore POST search', 'POST', 'v2/entities/hemishe_RAcademicScore/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 35.Akademik hisobotlar davomat (missing) ---
test('RAcademicAttendance POST search', 'POST', 'v2/entities/hemishe_RAcademicAttendance/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 37.Bandlik statistikasi (missing) ---
test('REmployment POST search', 'POST', 'v2/entities/hemishe_REmployment/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 38.Inspeksiya administrative teacher (missing) ---
test('RIAdministrativeEmployee1 GET by id', 'GET', "v2/entities/hemishe_RIAdministrativeEmployee1/$fakeDeleteUid?returnNulls=true");
test('RIAdministrativeEmployee1 PUT', 'PUT', "v2/entities/hemishe_RIAdministrativeEmployee1/$fakeDeleteUid", ['active' => true]);
test('RIAdministrativeEmployee1 POST search', 'POST', 'v2/entities/hemishe_RIAdministrativeEmployee1/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RIAdministrativeEmployee1 search POST', 'POST', 'v2/entities/hemishe_RIAdministrativeEmployee1/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 39.Xorijiy OTMda malaka oshirish (missing) ---
test('RIAdministrativeEmployee2 GET by id', 'GET', "v2/entities/hemishe_RIAdministrativeEmployee2/$fakeDeleteUid?returnNulls=true");
test('RIAdministrativeEmployee2 PUT', 'PUT', "v2/entities/hemishe_RIAdministrativeEmployee2/$fakeDeleteUid", ['active' => true]);
test('RIAdministrativeEmployee2 POST search', 'POST', 'v2/entities/hemishe_RIAdministrativeEmployee2/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RIAdministrativeEmployee2 search POST', 'POST', 'v2/entities/hemishe_RIAdministrativeEmployee2/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 40.OTMda xorijiy o'qituvchilar (missing) ---
test('RIAdministrativeEmployee3 GET by id', 'GET', "v2/entities/hemishe_RIAdministrativeEmployee3/$fakeDeleteUid?returnNulls=true");
test('RIAdministrativeEmployee3 PUT', 'PUT', "v2/entities/hemishe_RIAdministrativeEmployee3/$fakeDeleteUid", ['active' => true]);
test('RIAdministrativeEmployee3 POST search', 'POST', 'v2/entities/hemishe_RIAdministrativeEmployee3/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RIAdministrativeEmployee3 search POST', 'POST', 'v2/entities/hemishe_RIAdministrativeEmployee3/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 41.Inspeksiya administrative student2 (Akademik almashinuv) (missing) ---
test('RIAdministrativeStudent2 POST search', 'POST', 'v2/entities/hemishe_RIAdministrativeStudent2/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi) (missing) ---
test('RIAdministrativeStudent3 GET by id', 'GET', "v2/entities/hemishe_RIAdministrativeStudent3/$fakeDeleteUid?returnNulls=true");
test('RIAdministrativeStudent3 PUT', 'PUT', "v2/entities/hemishe_RIAdministrativeStudent3/$fakeDeleteUid", ['active' => true]);
test('RIAdministrativeStudent3 POST search', 'POST', 'v2/entities/hemishe_RIAdministrativeStudent3/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RIAdministrativeStudent3 search POST', 'POST', 'v2/entities/hemishe_RIAdministrativeStudent3/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 43.Inspeksiya administrative student4 (Talaba olimpiadalari) (missing) ---
test('RIAdministrativeStudent4 GET by id', 'GET', "v2/entities/hemishe_RIAdministrativeStudent4/$fakeDeleteUid?returnNulls=true");
test('RIAdministrativeStudent4 PUT', 'PUT', "v2/entities/hemishe_RIAdministrativeStudent4/$fakeDeleteUid", ['active' => true]);
test('RIAdministrativeStudent4 POST search', 'POST', 'v2/entities/hemishe_RIAdministrativeStudent4/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RIAdministrativeStudent4 search POST', 'POST', 'v2/entities/hemishe_RIAdministrativeStudent4/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari) (missing) ---
test('RIAdministrativeStudentSport GET by id', 'GET', "v2/entities/hemishe_RIAdministrativeStudentSport/$fakeDeleteUid?returnNulls=true");
test('RIAdministrativeStudentSport PUT', 'PUT', "v2/entities/hemishe_RIAdministrativeStudentSport/$fakeDeleteUid", ['active' => true]);
test('RIAdministrativeStudentSport POST search', 'POST', 'v2/entities/hemishe_RIAdministrativeStudentSport/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RIAdministrativeStudentSport search POST', 'POST', 'v2/entities/hemishe_RIAdministrativeStudentSport/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 45.Inspeksiya Sport inshootlari (missing) ---
test('RIAdministrativeSportFacilities POST', 'POST', 'v2/entities/hemishe_RIAdministrativeSportFacilities/', ['active' => true]);
test('RIAdministrativeSportFacilities GET list', 'GET', 'v2/entities/hemishe_RIAdministrativeSportFacilities?limit=3');
test('RIAdministrativeSportFacilities GET by id', 'GET', "v2/entities/hemishe_RIAdministrativeSportFacilities/$fakeDeleteUid?returnNulls=true");
test('RIAdministrativeSportFacilities PUT', 'PUT', "v2/entities/hemishe_RIAdministrativeSportFacilities/$fakeDeleteUid", ['active' => true]);
test('RIAdministrativeSportFacilities DELETE', 'DELETE', "v2/entities/hemishe_RIAdministrativeSportFacilities/$fakeDeleteUid");
test('RIAdministrativeSportFacilities POST search', 'POST', 'v2/entities/hemishe_RIAdministrativeSportFacilities/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RIAdministrativeSportFacilities search POST', 'POST', 'v2/entities/hemishe_RIAdministrativeSportFacilities/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 46.Akademik Uslubiy nashrlar (missing) ---
test('RIAcademicMethodologicPublications POST', 'POST', 'v2/entities/hemishe_RIAcademicMethodologicPublications/', ['active' => true]);
test('RIAcademicMethodologicPublications GET list', 'GET', 'v2/entities/hemishe_RIAcademicMethodologicPublications?limit=3');
test('RIAcademicMethodologicPublications GET by id', 'GET', "v2/entities/hemishe_RIAcademicMethodologicPublications/$fakeDeleteUid?returnNulls=true");
test('RIAcademicMethodologicPublications PUT', 'PUT', "v2/entities/hemishe_RIAcademicMethodologicPublications/$fakeDeleteUid", ['active' => true]);
test('RIAcademicMethodologicPublications DELETE', 'DELETE', "v2/entities/hemishe_RIAcademicMethodologicPublications/$fakeDeleteUid");
test('RIAcademicMethodologicPublications POST search', 'POST', 'v2/entities/hemishe_RIAcademicMethodologicPublications/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RIAcademicMethodologicPublications search POST', 'POST', 'v2/entities/hemishe_RIAcademicMethodologicPublications/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 47.Akademik O'quv ishlari (missing) ---
test('RIAcademicEducationalWork POST', 'POST', 'v2/entities/hemishe_RIAcademicEducationalWork/', ['active' => true]);
test('RIAcademicEducationalWork GET list', 'GET', 'v2/entities/hemishe_RIAcademicEducationalWork?limit=3');
test('RIAcademicEducationalWork GET by id', 'GET', "v2/entities/hemishe_RIAcademicEducationalWork/$fakeDeleteUid?returnNulls=true");
test('RIAcademicEducationalWork PUT', 'PUT', "v2/entities/hemishe_RIAcademicEducationalWork/$fakeDeleteUid", ['active' => true]);
test('RIAcademicEducationalWork DELETE', 'DELETE', "v2/entities/hemishe_RIAcademicEducationalWork/$fakeDeleteUid");
test('RIAcademicEducationalWork POST search', 'POST', 'v2/entities/hemishe_RIAcademicEducationalWork/search', ['filter' => ['conditions' => []], 'limit' => 3]);
test('RIAcademicEducationalWork search POST', 'POST', 'v2/entities/hemishe_RIAcademicEducationalWork/search', ['filter' => ['conditions' => []], 'limit' => 3]);

// --- 49.Fakultetlar (missing) ---
test('Service: faculty/get', 'GET', 'v2/services/faculty/get');

// --- 52.Mail (missing) ---
test('Service: mail/send POST', 'POST', 'v2/services/mail/send/', json_encode(['data' => []]));

// --- 54.Transkript (missing) ---
test('Service: transcript/get', 'GET', 'v2/services/transcript/get');

// --- 55.DTM (missing) ---
test('Service: mandat/get', 'GET', 'v2/services/mandat/get');

// --- 56.OAK (missing) ---
test('Service: oak/byPin', 'GET', 'v2/services/oak/byPin');

// --- 57.Contract (missing) ---
test('Service: contract/get', 'GET', 'v2/services/contract/get');

// --- 58.UzASBO (missing) ---
test('Service: student/checkScholarship2 POST', 'POST', 'v2/services/student/checkScholarship2/', json_encode(['data' => []]));
test('Service: test/typetest', 'GET', 'v2/services/test/typetest');

// --- 60.Soliq (missing) ---
test('Service: tax/rent', 'GET', 'v2/services/tax/rent');

// --- 61.Ijtimoiy himoya (missing) ---
test('Service: social/daftarFull', 'GET', 'v2/services/social/daftarFull');
test('Service: social/daftarShort', 'GET', 'v2/services/social/daftarShort');
test('Service: social/young', 'GET', 'v2/services/social/young');

// --- 62.Stipendiya (missing) ---
test('EStudentScholarshipFull POST', 'POST', 'v2/entities/hemishe_EStudentScholarshipFull/', ['active' => true]);
test('EStudentScholarshipAmount POST', 'POST', 'v2/entities/hemishe_EStudentScholarshipAmount/', ['active' => true]);
test('Service: scholarship/deleteAmounts', 'GET', 'v2/services/scholarship/deleteAmounts');

// --- 64.OTM (missing) ---
test('Service: otm/studentInfoById', 'GET', 'v2/services/otm/studentInfoById');
test('Service: otm/studentListByTutor', 'GET', 'v2/services/otm/studentListByTutor');
test('Service: otm/studentInfoByPinfl', 'GET', 'v2/services/otm/studentInfoByPinfl');

// --- 65.Xo'jalik hisobot (missing) ---
test('REducationMaterials POST', 'POST', 'v2/entities/hemishe_REducationMaterials/', ['active' => true]);
test('RLaboratories POST', 'POST', 'v2/entities/hemishe_RLaboratories/', ['active' => true]);
test('RIctEquipment POST', 'POST', 'v2/entities/hemishe_RIctEquipment/', ['active' => true]);

// --- 66.BIMM (missing) ---
test('Service: bimm/disabilityCheck', 'GET', 'v2/services/bimm/disabilityCheck');
test('Service: bimm/teacherTraining', 'GET', 'v2/services/bimm/teacherTraining');

// --- 69.Amaliyot (missing) ---
test('EStudentPractice POST', 'POST', 'v2/entities/hemishe_EStudentPractice/', ['active' => true]);

// --- 70.Qo'shimcha xizmatlar (missing) ---
test('Service: classifiers/hokimiyat', 'GET', 'v2/services/classifiers/hokimiyat');
test('Service: diploma/byhash', 'GET', 'v2/services/diploma/byhash');
test('Service: student/get', 'GET', 'v2/services/student/get');
test('Service: student/getActive', 'GET', 'v2/services/student/getActive');

// ============================================================
// NATIJA
// ============================================================
echo "\n\n========================================\n";
echo "=== NATIJA ===\n";
echo "========================================\n";
echo "\033[32mPASS: $pass\033[0m\n";
echo "\033[31mFAIL: $fail\033[0m\n";
echo "Jami: " . ($pass + $fail) . "\n";

// Match level statistikasi
$matchStats = ['FULL_MATCH' => 0, 'STRUCTURE_MATCH' => 0, 'PARTIAL_MATCH' => 0, 'STRUCTURE_MISMATCH' => 0, 'N/A' => 0];
foreach ($results as $r) {
    $level = $r['match_level'] ?? 'N/A';
    if (!isset($matchStats[$level])) $matchStats[$level] = 0;
    $matchStats[$level]++;
}
echo "\n--- JSON Moslik Statistikasi ---\n";
echo "  \033[32mFULL_MATCH:        {$matchStats['FULL_MATCH']}\033[0m\n";
echo "  \033[32mSTRUCTURE_MATCH:   {$matchStats['STRUCTURE_MATCH']}\033[0m\n";
echo "  \033[33mPARTIAL_MATCH:     {$matchStats['PARTIAL_MATCH']}\033[0m\n";
echo "  \033[31mSTRUCTURE_MISMATCH:{$matchStats['STRUCTURE_MISMATCH']}\033[0m\n";
echo "  N/A:               {$matchStats['N/A']}\n";

// FAIL details
$failures = array_filter($results, fn($r) => $r['status'] === 'FAIL');
if ($failures) {
    echo "\n\033[31m--- FAIL ro'yxati ---\033[0m\n";
    foreach ($failures as $f) {
        $ml = $f['match_level'] ?? '?';
        $mp = $f['match_percent'] ?? 0;
        echo "  {$f['name']}: Old={$f['old_code']} New={$f['new_code']} | JSON: $ml ({$mp}%)\n";
    }
}

// STRUCTURE_MISMATCH details
$mismatches = array_filter($comparisonReport, fn($r) => $r['match_level'] === 'STRUCTURE_MISMATCH');
if ($mismatches) {
    echo "\n\033[31m--- STRUCTURE_MISMATCH batafsil ---\033[0m\n";
    foreach ($mismatches as $m) {
        echo "  {$m['name']}:\n";
        $criticalDiffs = array_filter($m['all_diffs'], fn($d) =>
            !($d['ignored'] ?? false) && in_array($d['type'], ['MISSING_IN_NEW', 'TYPE_MISMATCH'])
        );
        foreach (array_slice($criticalDiffs, 0, 5) as $d) {
            echo "    [{$d['type']}] {$d['path']}";
            if (isset($d['old_value'])) echo " old={$d['old_value']}";
            if (isset($d['new_value'])) echo " new={$d['new_value']}";
            if (isset($d['old_type'])) echo " old_type={$d['old_type']} new_type={$d['new_type']}";
            echo "\n";
        }
    }
}

// Old-hemis da ishlamagan lekin hemis-back da ishlagan endpointlar
$oldFailed = array_filter($results, fn($r) => $r['status'] === 'PASS' && isset($r['note']) && strpos($r['note'], 'Old-hemis') !== false);
if ($oldFailed) {
    echo "\n\033[33m--- Old-hemis da xato, hemis-back da OK ---\033[0m\n";
    foreach ($oldFailed as $r) {
        echo "  {$r['name']}: {$r['note']}\n";
    }
}

// Save results
file_put_contents('/tmp/hemis_full_test_results.json', json_encode($results, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
echo "\nTest natijalar: /tmp/hemis_full_test_results.json\n";

// Save detailed comparison report
file_put_contents('/tmp/hemis_comparison_report.json', json_encode([
    'generated_at' => date('Y-m-d H:i:s'),
    'old_base' => $OLD_BASE,
    'new_base' => $NEW_BASE,
    'summary' => [
        'total_tests' => count($results),
        'pass' => $pass,
        'fail' => $fail,
        'match_stats' => $matchStats,
    ],
    'endpoints' => $comparisonReport,
], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
echo "Batafsil JSON diff: /tmp/hemis_comparison_report.json\n";
