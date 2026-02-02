<?php
/**
 * HEMIS Integration Test Script
 * Mavjud Univer kodlarini ishlatib hemis-back ni test qiladi.
 *
 * Usage:
 *   php test_hemis_integration.php [student_id] [--reset-uid]
 */

// =============================================================================
// 1. CONFIGURATION
// =============================================================================

$hemisEndpoint = getenv('HEMIS_ENDPOINT') ?: 'http://localhost:8081/app/rest/';
$testStudentId = $argv[1] ?? getenv('HEMIS_TEST_STUDENT_ID') ?: null;
$resetUid = in_array('--reset-uid', $argv);

// Colors
define('COLOR_GREEN', "\033[32m");
define('COLOR_RED', "\033[31m");
define('COLOR_YELLOW', "\033[33m");
define('COLOR_BLUE', "\033[34m");
define('COLOR_RESET', "\033[0m");

// =============================================================================
// 2. BOOTSTRAP YII2 APPLICATION
// =============================================================================

echo COLOR_BLUE . "\n=== HEMIS Integration Test ===" . COLOR_RESET . "\n\n";
echo "HEMIS Endpoint: {$hemisEndpoint}\n";

// Load composer autoload first (for Dotenv)
require __DIR__ . '/vendor/autoload.php';

// Load environment
require __DIR__ . '/common/config/env.php';

// Override HEMIS endpoint for testing
putenv("HEMIS_ENDPOINT={$hemisEndpoint}");
$_ENV['HEMIS_ENDPOINT'] = $hemisEndpoint;

// Include Yii
require __DIR__ . '/vendor/yiisoft/yii2/Yii.php';

// Bootstrap
require __DIR__ . '/common/config/bootstrap.php';
require __DIR__ . '/console/config/bootstrap.php';

// Create console application config
$config = yii\helpers\ArrayHelper::merge(
    require __DIR__ . '/common/config/main.php',
    require __DIR__ . '/common/config/main-local.php',
    require __DIR__ . '/console/config/main.php',
    require __DIR__ . '/console/config/main-local.php'
);

// Create application
$app = new yii\console\Application($config);

echo "Database: " . Yii::$app->db->dsn . "\n\n";

// =============================================================================
// 3. TEST FUNCTIONS (Mavjud Univer kodlarini chaqiradi)
// =============================================================================

use common\components\hemis\sync\StudentUpdater;
use common\components\hemis\HemisApiError;
use common\models\student\EStudent;

function printResult($name, $success, $message = '') {
    $status = $success ? COLOR_GREEN . "PASS" : COLOR_RED . "FAIL";
    echo "{$status}" . COLOR_RESET . " - {$name}";
    if ($message) echo ": {$message}";
    echo "\n";
    return $success;
}

function testStudentSync($student, $resetUid = false) {
    echo COLOR_YELLOW . "\n[1] Testing StudentUpdater::updateModel()..." . COLOR_RESET . "\n";

    echo "  Student: {$student->second_name} {$student->first_name}\n";
    echo "  PINFL: {$student->passport_pin}\n";
    echo "  UID (oldin): " . ($student->_uid ?: 'null') . "\n";
    echo "  Student ID (oldin): " . ($student->student_id_number ?: 'null') . "\n";

    if ($resetUid) {
        echo COLOR_YELLOW . "\n  --reset-uid: UID tozalanmoqda..." . COLOR_RESET . "\n";
        $student->updateAttributes(['_uid' => null, 'student_id_number' => null]);
        $student->refresh();
    }

    try {
        // Mavjud Univer kodi - StudentUpdater::updateModel()
        // Bu metod to'liq flowni bajaradi:
        // 1. generateStudentId() - agar _uid yo'q bo'lsa
        // 2. updateStudentUid() - student/update endpoint
        // 3. entity PUT - fallback
        $result = StudentUpdater::updateModel($student);

        $student->refresh();

        echo "\n  UID (keyin): " . ($student->_uid ?: 'null') . "\n";
        echo "  Student ID (keyin): " . ($student->student_id_number ?: 'null') . "\n";

        if ($result && $result->id) {
            printResult("StudentUpdater::updateModel()", true, "Muvaffaqiyatli");
            echo "    Response ID: {$result->id}\n";
            echo "    Response Code: {$result->code}\n";
            echo "    Verified: " . ($result->verified ? 'true' : 'false') . "\n";
            echo "    Points: {$result->points}\n";
            return true;
        } else {
            printResult("StudentUpdater::updateModel()", false, "Javob yo'q");
            return false;
        }
    } catch (HemisApiError $e) {
        printResult("StudentUpdater::updateModel()", false, "HEMIS Error: " . $e->getMessage());
        return false;
    } catch (Exception $e) {
        printResult("StudentUpdater::updateModel()", false, $e->getMessage());
        return false;
    }
}

function testStudentValidate($pinfl) {
    echo COLOR_YELLOW . "\n[2] Testing HemisApi::validateStudentData()..." . COLOR_RESET . "\n";

    try {
        // Mavjud Univer kodi
        $result = Yii::$app->hemisApi->validateStudentData($pinfl, false);

        if ($result) {
            printResult("validateStudentData()", true, "Javob olindi");
            echo "    Code: " . ($result->code ?: 'null') . "\n";
            if ($result->data) {
                echo "    Student: " . ($result->data['fullname'] ?? 'N/A') . "\n";
                echo "    University: " . ($result->data['university']['name'] ?? 'N/A') . "\n";
            }
            return true;
        } else {
            printResult("validateStudentData()", false, "Javob yo'q");
            return false;
        }
    } catch (HemisApiError $e) {
        printResult("validateStudentData()", false, "HEMIS Error: " . $e->getMessage());
        return false;
    } catch (Exception $e) {
        printResult("validateStudentData()", false, $e->getMessage());
        return false;
    }
}

function testStudentCheck($student) {
    echo COLOR_YELLOW . "\n[3] Testing StudentUpdater::checkModel()..." . COLOR_RESET . "\n";

    try {
        // Mavjud Univer kodi - HEMIS bilan sinxronizatsiyani tekshiradi
        $diff = StudentUpdater::checkModel($student, false);

        $student->refresh();

        if ($diff === false) {
            echo "    Sync status: " . $student->_sync_status . "\n";
            if ($student->_sync_status == 'not_found') {
                printResult("checkModel()", false, "Student HEMIS da topilmadi");
            } else {
                printResult("checkModel()", false, "Xatolik: " . $student->_sync_diff);
            }
            return false;
        } else {
            printResult("checkModel()", true, count($diff) . " ta farq topildi");
            if (count($diff) > 0) {
                echo "    Farqlar:\n";
                foreach ($diff as $field => $values) {
                    echo "      - {$field}: local='{$values['s']}' vs hemis='{$values['a']}'\n";
                }
            }
            return true;
        }
    } catch (HemisApiError $e) {
        printResult("checkModel()", false, "HEMIS Error: " . $e->getMessage());
        return false;
    } catch (Exception $e) {
        printResult("checkModel()", false, $e->getMessage());
        return false;
    }
}

// =============================================================================
// 4. RUN TESTS
// =============================================================================

$results = [];

if ($testStudentId) {
    $student = EStudent::findOne($testStudentId);
    if ($student) {
        if (!$student->meta) {
            echo COLOR_RED . "Student meta topilmadi!" . COLOR_RESET . "\n";
            exit(1);
        }

        $results['sync'] = testStudentSync($student, $resetUid);
        $results['validate'] = testStudentValidate($student->passport_pin);
        $results['check'] = testStudentCheck($student);
    } else {
        echo COLOR_RED . "Student not found: {$testStudentId}" . COLOR_RESET . "\n";
    }
} else {
    echo COLOR_YELLOW . "No student ID provided." . COLOR_RESET . "\n";
    echo "Usage: php test_hemis_integration.php <student_id> [--reset-uid]\n\n";

    // List some students
    echo "Available students with UID (first 10):\n";
    $students = EStudent::find()
        ->where(['not', ['_uid' => null]])
        ->andWhere(['not', ['_uid' => '']])
        ->limit(10)
        ->all();

    if (count($students) > 0) {
        foreach ($students as $s) {
            echo "  ID: {$s->id} | {$s->second_name} {$s->first_name} | PINFL: {$s->passport_pin} | UID: {$s->_uid}\n";
        }
    }

    echo "\nStudents without UID (first 5):\n";
    $newStudents = EStudent::find()
        ->where(['or', ['_uid' => null], ['_uid' => '']])
        ->andWhere(['not', ['passport_pin' => null]])
        ->limit(5)
        ->all();

    foreach ($newStudents as $s) {
        echo "  ID: {$s->id} | {$s->second_name} {$s->first_name} | PINFL: {$s->passport_pin}\n";
    }

    echo "\nRun: php test_hemis_integration.php <student_id>\n";
    echo "     php test_hemis_integration.php <student_id> --reset-uid  (yangi student yaratish testi)\n";
}

// =============================================================================
// 5. SUMMARY
// =============================================================================

if (!empty($results)) {
    echo COLOR_BLUE . "\n=== Test Summary ===" . COLOR_RESET . "\n";
    $passed = 0;
    $failed = 0;
    foreach ($results as $name => $success) {
        if ($success) {
            echo COLOR_GREEN . "PASS" . COLOR_RESET . " - {$name}\n";
            $passed++;
        } else {
            echo COLOR_RED . "FAIL" . COLOR_RESET . " - {$name}\n";
            $failed++;
        }
    }
    echo "\nTotal: {$passed} passed, {$failed} failed\n";
    exit($failed > 0 ? 1 : 0);
}
