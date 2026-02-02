<?php
/**
 * Test script for Quiz public key
 * Run: php test_quiz_key.php
 */

// Composer
require __DIR__ . '/vendor/autoload.php';

// Environment (loads .env)
require __DIR__ . '/common/config/env.php';

// Yii
require __DIR__ . '/vendor/yiisoft/yii2/Yii.php';

// Bootstrap application
require __DIR__ . '/common/config/bootstrap.php';
require __DIR__ . '/console/config/bootstrap.php';

$config = yii\helpers\ArrayHelper::merge(
    require __DIR__ . '/common/config/main.php',
    require __DIR__ . '/common/config/main-local.php',
    require __DIR__ . '/console/config/main.php',
    require __DIR__ . '/console/config/main-local.php'
);

$application = new yii\console\Application($config);

use common\components\Config;
use common\components\QuizHelper;

echo "=== Quiz Key Test ===\n\n";

// 1. Check if key exists
$publicKey = Config::get(Config::CONFIG_SYS_SURVEY_QUIZ_PUBLIC_KEY);
$apiUrl = Config::get(Config::CONFIG_SYS_SURVEY_QUIZ_API_URL);

echo "API URL: " . ($apiUrl ?: 'NOT SET') . "\n";
echo "Public Key set: " . ($publicKey ? 'YES' : 'NO') . "\n";

if (!$publicKey) {
    echo "\nERROR: Public key is not configured!\n";
    exit(1);
}

echo "Key length: " . strlen($publicKey) . " chars\n";
echo "Key preview: " . substr($publicKey, 0, 50) . "...\n\n";

// 2. Validate public key
$pubKeyResource = openssl_pkey_get_public($publicKey);
if (!$pubKeyResource) {
    echo "ERROR: Invalid public key format!\n";
    echo "OpenSSL error: " . openssl_error_string() . "\n";
    exit(1);
}

$details = openssl_pkey_get_details($pubKeyResource);
echo "Key type: RSA\n";
echo "Key bits: " . $details['bits'] . "\n";
echo "Max encrypt size (PKCS1): " . ($details['bits']/8 - 11) . " bytes\n\n";

// 3. Test encryption
$testValue = "12345678|TEACHER";
echo "Test value: '$testValue' (" . strlen($testValue) . " bytes)\n";

try {
    $encrypted = '';
    $success = openssl_public_encrypt(
        $testValue,
        $encrypted,
        $pubKeyResource,
        OPENSSL_PKCS1_PADDING
    );

    if ($success) {
        $base64 = base64_encode($encrypted);
        echo "Encryption: SUCCESS\n";
        echo "Encrypted length: " . strlen($encrypted) . " bytes\n";
        echo "Base64 length: " . strlen($base64) . " chars\n";
        echo "Base64 preview: " . substr($base64, 0, 50) . "...\n";
    } else {
        echo "Encryption: FAILED\n";
        echo "Error: " . openssl_error_string() . "\n";
    }
} catch (Exception $e) {
    echo "Exception: " . $e->getMessage() . "\n";
}

echo "\n=== Test Complete ===\n";
