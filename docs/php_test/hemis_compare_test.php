<?php
/**
 * OLD-HEMIS (8082) vs NEW HEMIS-BACK (8081) taqqoslash testi
 *
 * Logika:
 * 1. Old-hemis da ishlasa → hemis-back da ham ishlashi kerak
 * 2. Old-hemis da ishlamasa → endpoint ishlatilmagan, SKIP
 */
require '/home/adm1n/startup/univer/vendor/autoload.php';
require '/home/adm1n/startup/univer/common/config/env.php';
require '/home/adm1n/startup/univer/vendor/yiisoft/yii2/Yii.php';
require '/home/adm1n/startup/univer/common/config/bootstrap.php';
require '/home/adm1n/startup/univer/console/config/bootstrap.php';
$config = yii\helpers\ArrayHelper::merge(
    require '/home/adm1n/startup/univer/common/config/main.php',
    require '/home/adm1n/startup/univer/common/config/main-local.php',
    require '/home/adm1n/startup/univer/console/config/main.php',
    require '/home/adm1n/startup/univer/console/config/main-local.php'
);
new yii\console\Application($config);

$OLD_HEMIS = 'http://localhost:8082/app/rest/';
$NEW_HEMIS = 'http://localhost:8081/app/rest/';

$results = [];
$pass = 0; $fail = 0; $skip = 0; $total = 0;

function getToken($baseUrl) {
    $ch = curl_init($baseUrl . 'v2/oauth/token');
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, 'grant_type=password&username=otm401&password=XCZDAb7qvGTXxz');
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Authorization: Basic Y2xpZW50OnNlY3JldA==',
        'Content-Type: application/x-www-form-urlencoded',
    ]);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    $resp = curl_exec($ch);
    $code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    if ($code == 200) {
        $data = json_decode($resp, true);
        return $data['access_token'] ?? null;
    }
    return null;
}

function apiCall($baseUrl, $token, $method, $path, $data = null) {
    $url = $baseUrl . $path;
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
    if ($data !== null) {
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    }
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'Authorization: Bearer ' . $token,
    ]);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 15);
    $resp = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    return ['code' => $httpCode, 'body' => $resp, 'data' => json_decode($resp, true)];
}

function testEndpoint($name, $method, $path, $data, $oldToken, $newToken, &$pass, &$fail, &$skip, &$results) {
    global $OLD_HEMIS, $NEW_HEMIS;

    echo "\n--- {$name} ---\n";
    echo "  {$method} {$path}\n";

    // 1. Old-hemis da sinash
    $oldResult = apiCall($OLD_HEMIS, $oldToken, $method, $path, $data);
    echo "  OLD-HEMIS: HTTP {$oldResult['code']}";

    if ($oldResult['code'] >= 400) {
        // Old-hemis da ishlamaydi — bu endpoint ishlatilmagan
        $errMsg = '';
        if (isset($oldResult['data']['error'])) {
            $errMsg = $oldResult['data']['error'];
        } elseif (isset($oldResult['data']['message'])) {
            $errMsg = $oldResult['data']['message'];
        }
        echo " — {$errMsg}\n";
        echo "  \033[33mSKIP\033[0m — Old-hemis da ham ishlamaydi\n";
        $skip++;
        $results[] = ['name' => $name, 'status' => 'SKIP', 'reason' => "Old-hemis: HTTP {$oldResult['code']} {$errMsg}"];
        return;
    }

    echo " — OK\n";

    // 2. New hemis-back da sinash
    $newResult = apiCall($NEW_HEMIS, $newToken, $method, $path, $data);
    echo "  NEW-HEMIS: HTTP {$newResult['code']}";

    if ($newResult['code'] >= 400) {
        $errMsg = '';
        if (isset($newResult['data']['error'])) {
            $errMsg = $newResult['data']['error'];
            if (isset($newResult['data']['details'])) {
                $errMsg .= ' — ' . substr($newResult['data']['details'], 0, 100);
            }
        } elseif (isset($newResult['data']['message'])) {
            $errMsg = $newResult['data']['message'];
        }
        echo " — {$errMsg}\n";
        echo "  \033[31mFAIL\033[0m — Old-hemis da ishlaydi, hemis-back da ishlamaydi!\n";
        $fail++;
        $results[] = ['name' => $name, 'status' => 'FAIL', 'old_http' => $oldResult['code'], 'new_http' => $newResult['code'], 'error' => $errMsg];
        return;
    }

    echo " — OK\n";
    echo "  \033[32mPASS\033[0m\n";
    $pass++;
    $results[] = ['name' => $name, 'status' => 'PASS', 'old_http' => $oldResult['code'], 'new_http' => $newResult['code']];
}

// ============================================
echo "=== OLD-HEMIS vs NEW HEMIS-BACK Taqqoslash ===\n";
echo "Old: {$OLD_HEMIS}\n";
echo "New: {$NEW_HEMIS}\n\n";

// Token olish
echo "--- 0. Authentication ---\n";
$oldToken = getToken($OLD_HEMIS);
$newToken = getToken($NEW_HEMIS);
echo "  Old-hemis token: " . ($oldToken ? 'OK' : 'FAIL') . "\n";
echo "  New-hemis token: " . ($newToken ? 'OK' : 'FAIL') . "\n";

if (!$oldToken) { echo "\nOld-hemis (8082) ga ulanib bo'lmadi!\n"; exit(1); }
if (!$newToken) { echo "\nNew hemis-back (8081) ga ulanib bo'lmadi!\n"; exit(1); }

// ============================================
// Ma'lumotlarni olish
// ============================================
$student = \common\models\student\EStudent::find()
    ->where(['not', ['_uid' => null]])
    ->andWhere(['not', ['_uid' => '']])
    ->limit(1)->one();

$department = \common\models\structure\EDepartment::find()
    ->where(['not', ['_qid' => null]])
    ->limit(1)->one();

$specialty = \common\models\student\ESpecialty::find()->limit(1)->one();

$employee = \common\models\employee\EEmployee::find()
    ->where(['not', ['_uid' => null]])
    ->andWhere(['not', ['_uid' => '']])
    ->limit(1)->one();

$gpaStudents = \common\models\student\EStudent::find()
    ->where(['not', ['_uid' => null]])
    ->andWhere(['not', ['_uid' => '']])
    ->select(['id', '_uid'])
    ->limit(20)
    ->asArray()
    ->all();

$gpa = null;
foreach ($gpaStudents as $gs) {
    $gpa = \common\models\performance\EStudentGpa::find()
        ->where(['_student' => $gs['id']])
        ->limit(1)->one();
    if ($gpa) break;
}

// ============================================
// TESTLAR
// ============================================

// 1. Student update
if ($student) {
    $studentData = \common\components\hemis\sync\StudentUpdater::getSyncData($student);
    testEndpoint(
        'StudentUpdater::updateModel',
        'POST', 'v2/entities/hemishe_EStudent/',
        $studentData, $oldToken, $newToken,
        $pass, $fail, $skip, $results
    );
}

// 2. Student GET (validate)
if ($student && $student->_uid) {
    testEndpoint(
        'Student GET by UUID',
        'GET', 'v2/entities/hemishe_EStudent/' . $student->_uid . '?returnNulls=true',
        null, $oldToken, $newToken,
        $pass, $fail, $skip, $results
    );
}

// 3. Student validate
if ($student) {
    testEndpoint(
        'HemisApi::validateStudentData',
        'GET', 'v2/services/student/verify?pinfl=' . $student->passport_pin,
        null, $oldToken, $newToken,
        $pass, $fail, $skip, $results
    );
}

// 4. Department update
if ($department) {
    try {
        $deptData = \common\components\hemis\sync\DepartmentUpdater::getSyncData($department);
        testEndpoint(
            'DepartmentUpdater::updateModel',
            'POST', 'v2/entities/hemishe_EUniversityDepartment/',
            $deptData, $oldToken, $newToken,
            $pass, $fail, $skip, $results
        );
    } catch (\Exception $e) {
        echo "\n--- DepartmentUpdater ---\n  ERROR: " . $e->getMessage() . "\n";
    }
}

// 5. Department GET
if ($department && $department->_qid) {
    testEndpoint(
        'Department GET by UUID',
        'GET', 'v2/entities/hemishe_EUniversityDepartment/' . $department->_qid . '?returnNulls=true',
        null, $oldToken, $newToken,
        $pass, $fail, $skip, $results
    );
}

// 6. Specialty update
if ($specialty) {
    $specData = \common\components\hemis\sync\SpecialtyUpdater::getSyncData($specialty);
    testEndpoint(
        'SpecialtyUpdater::updateModel',
        'POST', 'v2/entities/hemishe_EUniversitySpeciality/',
        $specData, $oldToken, $newToken,
        $pass, $fail, $skip, $results
    );
}

// 7. Specialty GET (service endpoint)
testEndpoint(
    'Specialty GET /services/speciality/get',
    'GET', 'v2/services/speciality/get?university=' . \common\components\hemis\sync\BaseApiUpdater::getUniversity() . '&type=11&year=2020',
    null, $oldToken, $newToken,
    $pass, $fail, $skip, $results
);

// 8. Employee GET
if ($employee && $employee->_uid) {
    testEndpoint(
        'Employee GET by UUID',
        'GET', 'v2/entities/hemishe_ETeacher/' . $employee->_uid . '?returnNulls=true',
        null, $oldToken, $newToken,
        $pass, $fail, $skip, $results
    );
}

// 9. Employee update
if ($employee) {
    $empData = \common\components\hemis\sync\EmployeeUpdater::getSyncData($employee);
    testEndpoint(
        'EmployeeUpdater::updateModel',
        'PUT', 'v2/entities/hemishe_ETeacher/' . $employee->_uid,
        $empData, $oldToken, $newToken,
        $pass, $fail, $skip, $results
    );
}

// 10. University update - getSyncData mavjud emas, manual data yaratamiz
$univ = \common\models\structure\EUniversity::findOne(1);
if ($univ) {
    $univData = [
        'code' => $univ->code,
        'name' => $univ->name,
    ];
    testEndpoint(
        'UniversityUpdater::updateModel',
        'POST', 'v2/entities/hemishe_EUniversity/',
        $univData, $oldToken, $newToken,
        $pass, $fail, $skip, $results
    );
}

// 11. University GET
testEndpoint(
    'University GET',
    'GET', 'v2/entities/hemishe_EUniversity/?limit=1',
    null, $oldToken, $newToken,
    $pass, $fail, $skip, $results
);

// 12. GPA update
if ($gpa) {
    $gpaData = \common\components\hemis\sync\StudentGpaUpdater::getSyncData($gpa);
    testEndpoint(
        'StudentGpaUpdater::updateModel',
        'POST', 'v2/services/student/gpa/',
        $gpaData, $oldToken, $newToken,
        $pass, $fail, $skip, $results
    );
}

// 13. Student ID generate
if ($student) {
    testEndpoint(
        'Student ID generate/check',
        'POST', 'v2/services/student/id/',
        ['pinfl' => $student->passport_pin, 'university' => ['code' => \common\components\hemis\sync\BaseApiUpdater::getUniversity()]],
        $oldToken, $newToken,
        $pass, $fail, $skip, $results
    );
}

// 14. Group entity
try {
    $group = \common\models\student\EGroup::find()->limit(1)->one();
    if ($group) {
        $groupData = \common\components\hemis\sync\GroupUpdater::getSyncData($group);
        testEndpoint(
            'GroupUpdater::updateModel',
            'POST', 'v2/entities/hemishe_EUniversityGroup/',
            $groupData, $oldToken, $newToken,
            $pass, $fail, $skip, $results
        );
    }
} catch (\Exception $e) {
    echo "\n--- GroupUpdater ---\n  ERROR: " . $e->getMessage() . "\n";
}

// 15. Student update (PUT - mavjud studentni yangilash)
if ($student && $student->_uid) {
    testEndpoint(
        'Student PUT (update)',
        'PUT', 'v2/entities/hemishe_EStudent/' . $student->_uid,
        \common\components\hemis\sync\StudentUpdater::getSyncData($student),
        $oldToken, $newToken,
        $pass, $fail, $skip, $results
    );
}

// 16. Classifier endpoints
testEndpoint(
    'Classifier: educationType',
    'GET', 'v2/entities/hemishe_HEducationType?limit=3',
    null, $oldToken, $newToken,
    $pass, $fail, $skip, $results
);

testEndpoint(
    'Classifier: educationForm',
    'GET', 'v2/entities/hemishe_HEducationForm?limit=3',
    null, $oldToken, $newToken,
    $pass, $fail, $skip, $results
);

testEndpoint(
    'Classifier: course (level)',
    'GET', 'v2/entities/hemishe_HCourse?limit=3',
    null, $oldToken, $newToken,
    $pass, $fail, $skip, $results
);

testEndpoint(
    'Classifier: educationYear',
    'GET', 'v2/entities/hemishe_HEducationYear?limit=3',
    null, $oldToken, $newToken,
    $pass, $fail, $skip, $results
);

// ============================================
// NATIJA
// ============================================
echo "\n\n=== NATIJA ===\n";
echo "\033[32mPASS: {$pass}\033[0m\n";
echo "\033[31mFAIL: {$fail}\033[0m\n";
echo "\033[33mSKIP: {$skip}\033[0m (old-hemis da ham ishlamaydi)\n";
echo "Jami: " . ($pass + $fail + $skip) . "\n";

if ($fail > 0) {
    echo "\n\033[31m--- FAIL bo'lganlar (old-hemis da ishlaydi, hemis-back da ishlamaydi) ---\033[0m\n";
    foreach ($results as $r) {
        if ($r['status'] === 'FAIL') {
            echo "  {$r['name']}: old={$r['old_http']}, new={$r['new_http']} — {$r['error']}\n";
        }
    }
}

if ($skip > 0) {
    echo "\n\033[33m--- SKIP (old-hemis da ham ishlamaydi — ishlatilmagan) ---\033[0m\n";
    foreach ($results as $r) {
        if ($r['status'] === 'SKIP') {
            echo "  {$r['name']}: {$r['reason']}\n";
        }
    }
}

// JSON natija saqlash
file_put_contents('/tmp/hemis_compare_results.json', json_encode($results, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
echo "\nBatafsil natija: /tmp/hemis_compare_results.json\n";
