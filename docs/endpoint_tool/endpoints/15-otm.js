// 15.OTM endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_15 = [
    // ============================================
    // 15.OTM (3 endpoint)
    // ============================================
    {
                id: 1,
                category: "15.OTM",
                name: "OTM ro'yxati (GET /entities/hemishe_EUniversity)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EUniversity",
                requiresAuth: true,
                inputFields: {
                    limit: { label: "Limit", type: "number", defaultValue: "5", required: false },
                    view: { label: "View", type: "text", defaultValue: "eUniversity-view", required: false }
                },
                description: "Barcha OTM ro'yxatini OLD-HEMIS formatida olish (eUniversity-view: code, name, tin, address, ownership, universityType, soato)",
                ported: true
            },
    {
                id: 2,
                category: "15.OTM",
                name: "OTM yaratish/yangilash (POST /entities/hemishe_EUniversity)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EUniversity",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                    body_code: {
                        label: "code (OTM kodi)",
                        type: "text",
                        defaultNew: "999-NEW-TEST",   // 🆕 Yangi HEMIS uchun
                        defaultOld: "999-OLD-TEST",   // 🏛️ Eski HEMIS uchun
                        required: true,
                        placeholder: "Yangi: 999-NEW-TEST, Eski: 999-OLD-TEST",
                        bodyField: "code"
                    },
                    body_name: {
                        label: "name (OTM nomi)",
                        type: "text",
                        defaultNew: "Yangi Test Universiteti",   // 🆕 Yangi HEMIS uchun
                        defaultOld: "Eski Test Universiteti",    // 🏛️ Eski HEMIS uchun
                        required: true,
                        placeholder: "OTM nomi",
                        bodyField: "name"
                    },
                    body_tin: {
                        label: "tin (STIR)",
                        type: "text",
                        defaultNew: "111222333",   // 🆕 Yangi HEMIS uchun
                        defaultOld: "444555666",   // 🏛️ Eski HEMIS uchun
                        required: false,
                        placeholder: "STIR raqami",
                        bodyField: "tin"
                    },
                    body_address: {
                        label: "address (Manzil)",
                        type: "text",
                        defaultNew: "Yangi manzil, Toshkent",   // 🆕 Yangi HEMIS uchun
                        defaultOld: "Eski manzil, Toshkent",    // 🏛️ Eski HEMIS uchun
                        required: false,
                        placeholder: "Manzil",
                        bodyField: "address"
                    },
                    body_active: {
                        label: "active (Faol)",
                        type: "select",
                        options: [{value: "true", label: "true (faol)"}, {value: "false", label: "false (nofaol)"}],
                        default: "true",
                        required: false,
                        bodyField: "active",
                        parseAs: "boolean"
                    },
                    body_studentUrl: {
                        label: "studentUrl",
                        type: "text",
                        defaultNew: "student.new-test.uz",   // 🆕 Yangi HEMIS uchun
                        defaultOld: "student.old-test.uz",   // 🏛️ Eski HEMIS uchun
                        required: false,
                        placeholder: "Talaba portal URL",
                        bodyField: "studentUrl"
                    },
                    body_teacherUrl: {
                        label: "teacherUrl",
                        type: "text",
                        defaultNew: "teacher.new-test.uz",   // 🆕 Yangi HEMIS uchun
                        defaultOld: "teacher.old-test.uz",   // 🏛️ Eski HEMIS uchun
                        required: false,
                        placeholder: "O'qituvchi portal URL",
                        bodyField: "teacherUrl"
                    },
                    body_oneId: {
                        label: "oneId",
                        type: "select",
                        options: [{value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "true",
                        required: false,
                        bodyField: "oneId",
                        parseAs: "boolean"
                    },
                    returnNulls: {
                        label: "null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                bodyGenerator: (inputs) => ({
                    code: inputs.body_code,
                    name: inputs.body_name,
                    tin: inputs.body_tin || null,
                    address: inputs.body_address || null,
                    active: inputs.body_active === 'true',
                    studentUrl: inputs.body_studentUrl || null,
                    teacherUrl: inputs.body_teacherUrl || null,
                    oneId: inputs.body_oneId === 'true',
                    addStudent: true,
                    allowGrouping: true,
                    allowTransferOutside: true,
                    gpaEdit: false,
                    accreditationEdit: false,
                    gradingSystem: false
                }),
                description: `**OTM yaratish yoki yangilash** (CUBA Entity API pattern)

<b>🧪 Test uchun:</b>
- 🆕 <b>Yangi Hemis:</b> code=999-NEW-TEST, name="Yangi Test Universiteti"
- 🏛️ <b>Eski Hemis:</b> code=999-OLD-TEST, name="Eski Test Universiteti"

<b>Xususiyatlar:</b>
- Agar body da "code" mavjud va bazada bor → yangilash
- Agar body da "code" bazada mavjud emas → yaratish
- Response: Yaratilgan/yangilangan OTM + Location header

<b>Majburiy maydonlar:</b>
- code - OTM kodi (String, unique)
- name - OTM nomi

<b>Ixtiyoriy maydonlar:</b>
- tin - STIR raqami
- address - Manzil
- active - Faol holat (boolean)
- studentUrl, teacherUrl - Portal URL lari
- oneId, addStudent, allowGrouping - Boolean flaglar

<b>⚠️ Eslatma:</b> Bu endpoint faqat admin yoki tegishli huquqli foydalanuvchilar uchun!`,
                ported: true,
                storeFirstId: "createdUniversityCode"
            },
    {
                id: 3,
                category: "15.OTM",
                name: "OTM o'chirish (DELETE /entities/hemishe_EUniversity)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EUniversity/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "OTM kodi (o'chirish uchun)",
                        type: "text",
                        defaultNew: "999-NEW-TEST",   // 🆕 Yangi HEMIS uchun
                        defaultOld: "999-OLD-TEST",   // 🏛️ Eski HEMIS uchun
                        required: true,
                        placeholder: "Yangi: 999-NEW-TEST, Eski: 999-OLD-TEST",
                        useStoredId: "createdUniversityCode"
                    }
                },
                description: `**OTM o'chirish** (soft delete)

<b>🧪 Test uchun:</b>
- 🆕 <b>Yangi Hemis:</b> entityId=999-NEW-TEST
- 🏛️ <b>Eski Hemis:</b> entityId=999-OLD-TEST

<b>Xususiyatlar:</b>
- Soft delete: deleteTs va deletedBy maydonlari o'rnatiladi
- Response: 204 No Content (muvaffaqiyatli) yoki 404 Not Found

<b>⚠️ Eslatma:</b> Avval #2 orqali test OTM yarating, keyin shu endpoint orqali o'chiring!`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_15;
}
