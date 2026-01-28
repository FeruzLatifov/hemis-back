// Barcha testlarni birlashtiruvchi fayl
// Bu fayl barcha test kategoriyalarini bitta massivga jamlaydi
//
// Har bir test fayli o'z kategoriyasi uchun tests_XX massivini e'lon qiladi.
// Ushbu fayl ularni tartib bilan birlashtiradi.
//
// Foydalanish: HTML sahifada barcha test fayllarni <script> bilan yuklang,
// so'ngra bu faylni oxirida yuklang.

const allIntegrationTests = [
    ...tests_00,
    ...tests_01,
    ...tests_02,
    ...tests_03,
    ...tests_04,
    ...tests_05,
    ...tests_06,
    ...tests_07,
    ...tests_08,
    ...tests_09,
    ...tests_10,
    ...tests_11,
    ...tests_12,
    ...tests_13,
];

console.log('Integration tests loaded:', allIntegrationTests.length, 'tests from 14 categories');
