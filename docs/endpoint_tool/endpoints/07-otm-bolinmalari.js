// 07.OTM bo'linmalari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_07 = [
    // ============================================
    // 07.OTM bo'linmalari (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "07.OTM bo'linmalari",
                name: "Bo'linmani ID (code) bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Bo'linma kodi",
                        type: "text",
                        default: "401-102-08",
                        required: true,
                        placeholder: "Yangi:401-102-08, Eski:351-118"
                    },
                    view: {
                        label: "View nomi",
                        type: "text",
                        default: "",
                        required: false,
                        placeholder: "eUniversityDepartment-view (ixtiyoriy)"
                    },
                    returnNulls: {
                        label: "null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "OTM bo'linmasi (fakultet, kafedra, bo'lim) ma'lumotlarini kod bo'yicha olish. ID = code (String), UUID emas! Masalan: '351-118' (fakultet), '401-102-08' (kafedra). Response: _entityName, _instanceName, code, nameUz, nameRu, university, deparmentType, parent, status.",
                ported: true,
                storeFirstId: "departmentCode"
            },
    {
                id: 2,
                category: "07.OTM bo'linmalari",
                name: "Barcha bo'linmalarni olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment",
                requiresAuth: true,
                inputFields: {
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: "0",
                        required: false
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: "10",
                        required: false
                    },
                    sort: {
                        label: "Sort",
                        type: "text",
                        default: "",
                        required: false,
                        placeholder: "code-asc yoki nameUz-desc"
                    },
                    view: {
                        label: "View nomi",
                        type: "text",
                        default: "",
                        required: false
                    },
                    returnNulls: {
                        label: "null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Barcha OTM bo'linmalarini pagination bilan olish. Foydalanuvchi faqat o'z OTM bo'linmalarini ko'radi. Sort formati: field-direction (masalan: code-asc, nameUz-desc).",
                ported: true
            },
    {
                id: 3,
                category: "07.OTM bo'linmalari",
                name: "Bo'linmalarni qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA format JSON)",
                        type: "textarea",
                        // 🎯 Ikkala tizim uchun ham university.code = 401 (solishtirish uchun)
                        default: JSON.stringify({
                            filter: {
                                conditions: [
                                    { property: "university.code", operator: "=", value: "401" },
                                    { property: "status", operator: "=", value: true }
                                ]
                            }
                        }),
                        required: false,
                        placeholder: '{"filter":{"conditions":[{"property":"university.code","operator":"=","value":"401"}]}}',
                        rows: 4,
                        helpText: `<b>🎯 Test uchun avtomatik filter:</b><br>
- university.code = "401" (solishtirish uchun bir xil)<br>
- status = true (faqat faol bo'linmalar)<br><br>
<b>CUBA Filter formati:</b><br>
<pre>{"filter":{"conditions":[
  {"property":"university.code", "operator":"=", "value":"401"},
  {"property":"status", "operator":"=", "value":true}
]}}</pre>
<b>Qo'llab-quvvatlanadigan operatorlar:</b> =, <>, like, startsWith, endsWith, in, isNull, notNull<br>
<b>Filtrlash mumkin maydonlar:</b> code, nameUz, nameRu, university.code, deparmentType.code, status`
                    },
                    view: {
                        label: "View nomi",
                        type: "text",
                        default: "",
                        required: false
                    },
                    returnNulls: {
                        label: "null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Bo'linmalarni URL parametrlari orqali qidirish. 🎯 Filter: university.code=401, status=true",
                ported: true
            },
    {
                id: 4,
                category: "07.OTM bo'linmalari",
                name: "Bo'linmalarni qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment/search",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                    returnNulls: {
                        label: "null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                // 🎯 Ikkala tizim uchun ham university.code = 401 (solishtirish uchun)
                bodyGenerator: (inputs) => ({
                    filter: {
                        conditions: [
                            { property: "university.code", operator: "=", value: "401" },
                            { property: "status", operator: "=", value: true }
                        ]
                    },
                    view: "eUniversityDepartment-view"
                }),
                description: `Bo'linmalarni JSON filter orqali qidirish.

<b>🎯 Test uchun avtomatik filter (ikkala tizim uchun):</b>
- <b>university.code = "401"</b> (solishtirish uchun bir xil)
- <b>status = true</b> (faqat faol bo'linmalar)

<b>CUBA Filter formati:</b>
<pre>{"filter":{"conditions":[
  {"property":"university.code", "operator":"=", "value":"401"},
  {"property":"status", "operator":"=", "value":true}
]}, "view":"..."}</pre>

<b>Qo'llab-quvvatlanadigan operatorlar:</b> =, <>, like, startsWith, endsWith, in, isNull, notNull
<b>Filtrlash mumkin maydonlar:</b> code, nameUz, nameRu, university.code, deparmentType.code, status`,
                ported: true
            },
    {
                id: 5,
                category: "07.OTM bo'linmalari",
                name: "Yangi bo'linma yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                    body_id: {
                        label: "id (code)",
                        type: "text",
                        defaultNew: "401-99-TEST",  // 🎯 Yangi HEMIS (otm401)
                        defaultOld: "351-99-TEST",  // 🏛️ Eski HEMIS (otm351)
                        required: true,
                        placeholder: "Yangi: 401-xx-xx, Eski: 351-xx-xx",
                        bodyField: "id"
                    },
                    body_nameUz: {
                        label: "nameUz",
                        type: "text",
                        default: "Test bo'lim",
                        required: true,
                        placeholder: "O'zbekcha nomi",
                        bodyField: "nameUz"
                    },
                    body_nameRu: {
                        label: "nameRu",
                        type: "text",
                        default: "Тестовый отдел",
                        required: false,
                        placeholder: "Ruscha nomi",
                        bodyField: "nameRu"
                    },
                    body_status: {
                        label: "status",
                        type: "select",
                        options: [{value: "true", label: "true (faol)"}, {value: "false", label: "false (nofaol)"}],
                        default: "true",
                        required: false,
                        bodyField: "status",
                        parseAs: "boolean"
                    },
                    body_university_code: {
                        label: "university.code",
                        type: "text",
                        defaultNew: "401",  // 🎯 Yangi HEMIS (otm401)
                        defaultOld: "351",  // 🏛️ Eski HEMIS (otm351)
                        required: true,
                        placeholder: "Yangi Hemis: 401, Eski Hemis: 351",
                        bodyField: "university.code"
                    },
                    body_deparmentType_code: {
                        label: "deparmentType.code",
                        type: "text",
                        default: "12",
                        required: true,
                        placeholder: "10=fakultet, 11=kafedra, 12=bo'lim",
                        bodyField: "deparmentType.code"
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
                    id: inputs.body_id,
                    nameUz: inputs.body_nameUz,
                    nameRu: inputs.body_nameRu || null,
                    status: inputs.body_status === 'true',
                    university: {code: inputs.body_university_code},
                    deparmentType: {code: inputs.body_deparmentType_code}
                }),
                description: `Yangi bo'linma yaratish yoki mavjudini yangilash (UPSERT).

**🧪 Test uchun:**
- 🆕 **Yangi Hemis:** id=401-99-TEST, university.code=401
- 🏛️ **Eski Hemis:** id=351-99-TEST, university.code=351

**CUBA UPSERT Behavior:**
- Agar entity mavjud bo'lsa → YANGILASH
- Agar entity mavjud bo'lmasa → YARATISH

**Maydonlar:**
- id: Bo'linma kodi (String, unique, required)
- nameUz: O'zbekcha nomi (required)
- nameRu: Ruscha nomi (optional)
- status: true/false
- university.code: Universitet kodi
- deparmentType.code: Bo'linma turi (10=fakultet, 11=kafedra, 12=bo'lim)

**Eslatma:** Server xavfsizlik uchun university.code ni foydalanuvchi kontekstidan oladi.`,
                ported: true,
                storeFirstId: "createdDepartmentCode"
            },
    {
                id: 6,
                category: "07.OTM bo'linmalari",
                name: "Bo'linmani yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment/{entityId}",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                    entityId: {
                        label: "Bo'linma kodi",
                        type: "text",
                        defaultNew: "401-99-TEST",  // 🎯 Yangi HEMIS (otm401)
                        defaultOld: "351-99-TEST",  // 🏛️ Eski HEMIS (otm351)
                        required: true,
                        placeholder: "Yangi: 401-xx-xx, Eski: 351-xx-xx",
                        useStoredId: "departmentCode"
                    },
                    returnNulls: {
                        label: "null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                requestBody: {
                    nameUz: "Yangilangan nom",
                    nameRu: "Обновленное название",
                    status: true
                },
                description: `Bo'linmani yangilash.

**🧪 Test uchun:**
- 🆕 **Yangi Hemis (otm401):** entityId = 401-99-TEST
- 🏛️ **Eski Hemis (otm351):** entityId = 351-99-TEST

Avval #5 orqali bo'linma yarating, keyin shu endpoint orqali yangilang.`,
                ported: true
            },
    {
                id: 7,
                category: "07.OTM bo'linmalari",
                name: "Bo'linmani o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Bo'linma kodi (o'chirish uchun)",
                        type: "text",
                        defaultNew: "401-99-TEST",  // 🎯 Yangi HEMIS (otm401)
                        defaultOld: "351-99-TEST",  // 🏛️ Eski HEMIS (otm351)
                        required: true,
                        placeholder: "Yangi: 401-xx-xx, Eski: 351-xx-xx",
                        useStoredId: "createdDepartmentCode"
                    }
                },
                description: `Bo'linmani soft delete qilish (delete_ts belgilanadi, bazadan o'chirilmaydi).

**🧪 Test uchun:**
- 🆕 **Yangi Hemis (otm401):** entityId = 401-99-TEST
- 🏛️ **Eski Hemis (otm351):** entityId = 351-99-TEST

Avval #5 orqali bo'linma yarating, keyin shu endpoint orqali o'chiring.`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_07;
}
