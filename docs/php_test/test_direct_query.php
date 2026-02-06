<?php
// Direct JDBC query to verify
$pdo = new PDO("pgsql:host=localhost;dbname=test1_hemis", "postgres", "postgres");
$stmt = $pdo->prepare("SELECT id, _faculty, _university FROM hemishe_e_student WHERE pinfl = ? AND _student_status IN ('10', '11', '12', '13', '14', '15') ORDER BY create_ts DESC LIMIT 1");
$stmt->execute(['52503015440023']);
$row = $stmt->fetch(PDO::FETCH_ASSOC);

echo "Direct DB Query:\n";
echo "ID: " . $row['id'] . "\n";
echo "Faculty: " . $row['_faculty'] . "\n";
echo "University: " . $row['_university'] . "\n";
