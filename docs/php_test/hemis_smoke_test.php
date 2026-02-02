<?php
/**
 * HEMIS Integration Smoke Test
 * Barcha integratsiya funksiyalarini test qiladi (READ-ONLY)
 */

require __DIR__ . '/vendor/autoload.php';
require __DIR__ . '/common/config/env.php';
require __DIR__ . '/vendor/yiisoft/yii2/Yii.php';
require __DIR__ . '/common/config/bootstrap.php';
require __DIR__ . '/console/config/bootstrap.php';

$config = yii\helpers\ArrayHelper::merge(
    require __DIR__ . '/common/config/main.php',
    require __DIR__ . '/common/config/main-local.php',
    require __DIR__ . '/console/config/main.php',
    require __DIR__ . '/console/config/main-local.php'
);

new yii\console\Application($config);

// Results storage
$results = [
    'passed' => [],
    'failed' => [],
    'skipped' => []
];

function testResult($name, $status, $message = '') {
    global $results;
    $icon = $status === 'PASS' ? '✓' : ($status === 'FAIL' ? '✗' : '○');
    echo "[$icon] $name: $status" . ($message ? " - $message" : "") . "\n";

    if ($status === 'PASS') {
        $results['passed'][] = ['name' => $name, 'message' => $message];
    } elseif ($status === 'FAIL') {
        $results['failed'][] = ['name' => $name, 'message' => $message];
    } else {
        $results['skipped'][] = ['name' => $name, 'message' => $message];
    }
}

echo "=== HEMIS Integration Smoke Test ===\n";
echo "Date: " . date('Y-m-d H:i:s') . "\n";
$endpoint = getenv('HEMIS_ENDPOINT') ?: 'http://localhost:8081/app/rest/';
echo "Endpoint: " . $endpoint . "\n\n";

// 1. Token/Login Test
echo "--- 1. Authentication ---\n";
try {
    Yii::$app->hemisApi->apiLogin('otm401', 'XCZDAb7qvGTXxz');
    testResult('HemisApi::apiLogin', 'PASS', 'Token received');
} catch (Exception $e) {
    testResult('HemisApi::apiLogin', 'FAIL', $e->getMessage());
    echo "Cannot continue without authentication.\n";
    exit(1);
}

// 2. Student Tests
echo "\n--- 2. Student Integration ---\n";
$student = \common\models\student\EStudent::find()
    ->where(['not', ['_uid' => null]])
    ->andWhere(['not', ['_uid' => '']])
    ->limit(1)->one();

if ($student) {
    // 2.1 StudentUpdater::updateModel (READ test - just validate data)
    try {
        $result = \common\components\hemis\sync\StudentUpdater::updateModel($student);
        testResult('StudentUpdater::updateModel', $result ? 'PASS' : 'FAIL', $student->second_name);
    } catch (Exception $e) {
        testResult('StudentUpdater::updateModel', 'FAIL', $e->getMessage());
    }

    // 2.2 StudentUpdater::checkModel
    try {
        $diff = \common\components\hemis\sync\StudentUpdater::checkModel($student, false);
        testResult('StudentUpdater::checkModel', $diff !== false ? 'PASS' : 'FAIL',
            is_array($diff) ? count($diff) . ' differences' : 'error');
    } catch (Exception $e) {
        testResult('StudentUpdater::checkModel', 'FAIL', $e->getMessage());
    }

    // 2.3 validateStudentData
    try {
        $result = Yii::$app->hemisApi->validateStudentData($student->passport_pin, false);
        testResult('HemisApi::validateStudentData', $result ? 'PASS' : 'FAIL');
    } catch (Exception $e) {
        testResult('HemisApi::validateStudentData', 'FAIL', $e->getMessage());
    }
} else {
    testResult('Student Tests', 'SKIP', 'No student with UID found');
}

// 3. Department Tests
echo "\n--- 3. Department Integration ---\n";
$dept = \common\models\structure\EDepartment::find()->limit(1)->one();
if ($dept) {
    try {
        $result = \common\components\hemis\sync\DepartmentUpdater::updateModel($dept);
        testResult('DepartmentUpdater::updateModel', 'PASS', $dept->name);
    } catch (Exception $e) {
        testResult('DepartmentUpdater::updateModel', 'FAIL', $e->getMessage());
    }
} else {
    testResult('DepartmentUpdater', 'SKIP', 'No department found');
}

// 4. Specialty Tests
echo "\n--- 4. Specialty Integration ---\n";
try {
    $spec = \common\models\student\ESpecialty::find()->limit(1)->one();
    if ($spec) {
        $result = \common\components\hemis\sync\SpecialtyUpdater::updateModel($spec);
        testResult('SpecialtyUpdater::updateModel', 'PASS', $spec->name);
    } else {
        testResult('SpecialtyUpdater', 'SKIP', 'No specialty found in DB');
    }
} catch (Exception $e) {
    testResult('SpecialtyUpdater::updateModel', 'FAIL', $e->getMessage());
} catch (Error $e) {
    testResult('SpecialtyUpdater', 'FAIL', $e->getMessage());
}

// 5. Employee Tests
echo "\n--- 5. Employee Integration ---\n";
$employee = \common\models\employee\EEmployee::find()
    ->where(['not', ['_uid' => null]])
    ->andWhere(['not', ['_uid' => '']])
    ->limit(1)->one();

if ($employee) {
    // 5.1 EmployeeUpdater::checkModel (READ-ONLY)
    try {
        $diff = \common\components\hemis\sync\EmployeeUpdater::checkModel($employee, false);
        testResult('EmployeeUpdater::checkModel', $diff !== false ? 'PASS' : 'FAIL',
            is_array($diff) ? count($diff) . ' differences' : 'checked');
    } catch (Exception $e) {
        testResult('EmployeeUpdater::checkModel', 'FAIL', $e->getMessage());
    }

    // 5.2 EmployeeUpdater::updateModel (entity PUT - DB write talab qiladi)
    testResult('EmployeeUpdater::updateModel', 'SKIP', 'DB write talab qiladi');
} else {
    testResult('EmployeeUpdater', 'SKIP', 'No employee with UID found');
}

// 6. University Tests
echo "\n--- 6. University Integration ---\n";
try {
    $uni = \common\models\structure\EUniversity::findCurrentUniversity();
    if ($uni) {
        $result = \common\components\hemis\sync\UniversityUpdater::updateModel($uni);
        testResult('UniversityUpdater::updateModel', 'PASS', $uni->code);
    }
} catch (Exception $e) {
    testResult('UniversityUpdater::updateModel', 'FAIL', $e->getMessage());
}

// 7. GPA Tests (if model exists)
echo "\n--- 7. Student GPA Integration ---\n";
$gpa = null;
try {
    $gpa = \common\models\performance\EStudentGpa::find()->limit(1)->one();
} catch (Exception $e) {
    testResult('StudentGpaUpdater', 'FAIL', 'Model error: ' . $e->getMessage());
}

if ($gpa) {
    try {
        $result = \common\components\hemis\sync\StudentGpaUpdater::updateModel($gpa);
        testResult('StudentGpaUpdater::updateModel', $result ? 'PASS' : 'FAIL');
    } catch (Exception $e) {
        testResult('StudentGpaUpdater::updateModel', 'FAIL', $e->getMessage());
    }
} else if ($gpa === null) {
    testResult('StudentGpaUpdater', 'SKIP', 'No GPA record found in DB');
}

// 8. Scholarship Tests
echo "\n--- 8. Scholarship Integration ---\n";
try {
    $scholarship = \common\models\finance\EStudentScholarship::find()->limit(1)->one();
    if ($scholarship) {
        // Test getSyncData - faqat ma'lumot tuzilmasini tekshiradi
        $syncData = \common\components\hemis\sync\StudentScholarshipUpdater::getSyncData($scholarship);
        $hasRequiredFields = isset($syncData['stipendId']) && isset($syncData['pinfl']);
        testResult('StudentScholarshipUpdater::getSyncData', $hasRequiredFields ? 'PASS' : 'FAIL',
            $hasRequiredFields ? 'syncData fields valid' : 'Missing required fields');
    } else {
        testResult('Scholarship Test', 'SKIP', 'No scholarship record in DB');
    }
} catch (Exception $e) {
    testResult('Scholarship Test', 'FAIL', $e->getMessage());
}

// Summary
echo "\n=== SUMMARY ===\n";
echo "Passed: " . count($results['passed']) . "\n";
echo "Failed: " . count($results['failed']) . "\n";
echo "Skipped: " . count($results['skipped']) . "\n";

// Return results as JSON for parsing
file_put_contents('/tmp/hemis_test_results.json', json_encode($results, JSON_PRETTY_PRINT));
echo "\nResults saved to /tmp/hemis_test_results.json\n";

exit(count($results['failed']) > 0 ? 1 : 0);
