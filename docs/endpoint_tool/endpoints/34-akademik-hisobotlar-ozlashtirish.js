// 34.Akademik hisobotlar o'zlashtirish endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_34 = [
    // ============================================
    // 34.Akademik hisobotlar o'zlashtirish (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "Yangi o'zlashtirish yozuvi yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore",
                requiresAuth: true,
                inputFields: {
                    universityCode: {
                        label: "Universitet kodi",
                        type: "text",
                        defaultNew: "401",
                        defaultOld: "351",
                        required: false
                    },
                    universityName: {
                        label: "Universitet nomi",
                        type: "text",
                        defaultNew: "TATU",
                        defaultOld: "BuxDU",
                        required: false
                    },
                    facultyCode: {
                        label: "Fakultet kodi",
                        type: "text",
                        defaultNew: "401-01",
                        defaultOld: "351-01",
                        required: false
                    },
                    facultyName: {
                        label: "Fakultet nomi",
                        type: "text",
                        defaultNew: "Dasturiy injiniring",
                        defaultOld: "Fizika-matematika",
                        required: false
                    },
                    educationTypeCode: {
                        label: "Ta'lim turi kodi",
                        type: "text",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: false
                    },
                    educationTypeName: {
                        label: "Ta'lim turi nomi",
                        type: "text",
                        defaultNew: "Bakalavr",
                        defaultOld: "Bakalavr",
                        required: false
                    },
                    educationYearCode: {
                        label: "O'quv yili kodi",
                        type: "text",
                        defaultNew: "2024",
                        defaultOld: "2023",
                        required: false
                    },
                    educationYearName: {
                        label: "O'quv yili nomi",
                        type: "text",
                        defaultNew: "2024-2025",
                        defaultOld: "2023-2024",
                        required: false
                    },
                    semesterTypeCode: {
                        label: "Semestr kodi",
                        type: "text",
                        defaultNew: "1",
                        defaultOld: "1",
                        required: false
                    },
                    semesterTypeName: {
                        label: "Semestr nomi",
                        type: "text",
                        defaultNew: "1-semestr",
                        defaultOld: "1-semestr",
                        required: false
                    },
                    courseCode: {
                        label: "Kurs kodi",
                        type: "text",
                        defaultNew: "1",
                        defaultOld: "1",
                        required: false
                    },
                    courseName: {
                        label: "Kurs nomi",
                        type: "text",
                        defaultNew: "1-kurs",
                        defaultOld: "1-kurs",
                        required: false
                    },
                    tableType: {
                        label: "Jadval turi",
                        type: "text",
                        defaultNew: "o'zlashtirish ko'rsatkichlari",
                        defaultOld: "o'zlashtirish ko'rsatkichlari",
                        required: false
                    },
                    scorePercent: {
                        label: "O'zlashtirish foizi",
                        type: "number",
                        defaultNew: "85.5",
                        defaultOld: "78.3",
                        required: false
                    },
                    scoreType: {
                        label: "Baho turi",
                        type: "text",
                        defaultNew: "yaxshi",
                        defaultOld: "qoniqarli",
                        required: false
                    },
                    debitorCount: {
                        label: "Qarzdorlar soni",
                        type: "number",
                        defaultNew: "5",
                        defaultOld: "10",
                        required: false
                    },
                    updateDate: {
                        label: "Yangilangan sana",
                        type: "text",
                        placeholder: "YYYY-MM-DD",
                        defaultNew: "2024-01-15",
                        defaultOld: "2023-01-15",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["universityCode", "universityName", "facultyCode", "facultyName", "educationTypeCode", "educationTypeName", "educationYearCode", "educationYearName", "semesterTypeCode", "semesterTypeName", "courseCode", "courseName", "tableType", "scorePercent", "scoreType", "debitorCount", "updateDate"],
                description: `**Yangi o'zlashtirish yozuvi yaratish**

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicScore

<b>Response:</b> 201 Created - Yaratilgan yozuv (ID avtomatik keyingi testlarga o'tadi)`,
                ported: true,
                storeResultId: "academicScoreId"
            },
    {
                id: 2,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "O'zlashtirish yozuvini ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik to'ldiriladi",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "academicScoreId"
                    },
                    returnNulls: {
                        label: "Null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        defaultNew: "",
                        defaultOld: "",
                        required: false
                    }
                },
                description: `**O'zlashtirish yozuvini ID bo'yicha olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicScore/{entityId}

<b>Workflow:</b> #1 POST dan yaratilgan ID avtomatik qo'yiladi.

<b>Parameters:</b>
- entityId: #1 POST dan olingan UUID

<b>Response:</b> 200 OK - Topilgan yozuv yoki 404 Not Found`,
                ported: true
            },
    {
                id: 3,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "O'zlashtirish yozuvini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik to'ldiriladi",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "academicScoreId"
                    },
                    scorePercent: {
                        label: "O'zlashtirish foizi (yangi qiymat)",
                        type: "number",
                        defaultNew: "95.0",
                        defaultOld: "88.5",
                        required: false
                    },
                    debitorCount: {
                        label: "Qarzdorlar soni (yangi qiymat)",
                        type: "number",
                        defaultNew: "3",
                        defaultOld: "8",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["scorePercent", "debitorCount"],
                description: `**O'zlashtirish yozuvini yangilash**

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RAcademicScore/{entityId}

<b>Response:</b> 200 OK - Yangilangan yozuv yoki 404 Not Found`,
                ported: true
            },
    {
                id: 4,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "O'zlashtirish yozuvini o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik to'ldiriladi",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "academicScoreId"
                    }
                },
                description: `**O'zlashtirish yozuvini o'chirish (soft delete)**

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RAcademicScore/{entityId}

<b>Response:</b> 200 OK yoki 404 Not Found

<b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
                ported: true
            },
    {
                id: 5,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "Barcha o'zlashtirish yozuvlarini olish (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        defaultNew: "50",
                        defaultOld: "50",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        defaultNew: "0",
                        defaultOld: "0",
                        required: false
                    },
                    returnCount: {
                        label: "Umumiy sonni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        defaultNew: "",
                        defaultOld: "",
                        required: false
                    },
                    returnNulls: {
                        label: "Null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        defaultNew: "",
                        defaultOld: "",
                        required: false
                    }
                },
                description: `**Barcha o'zlashtirish yozuvlarini olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicScore

<b>Parameters:</b>
- limit: Sahifa hajmi (default: 50)
- offset: Boshlanish indeksi (default: 0)
- returnCount: Umumiy sonni X-Total-Count headerda qaytarish
- returnNulls: Null qiymatlarni ham qaytarish

<b>Response:</b> Sahifalangan o'zlashtirish hisobotlari ro'yxati`,
                ported: true
            },
    {
                id: 6,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "O'zlashtirish yozuvlarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON)",
                        type: "textarea",
                        placeholder: '{"conditions":[{"property":"universityCode","operator":"=","value":"351"}]}',
                        defaultNew: '{"conditions":[{"property":"universityCode","operator":"=","value":"351"}]}',
                        defaultOld: '{"conditions":[{"property":"universityCode","operator":"=","value":"351"}]}',
                        required: false,
                        rows: 3
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        defaultNew: "2",
                        defaultOld: "2",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        defaultNew: "0",
                        defaultOld: "0",
                        required: false
                    },
                    returnNulls: {
                        label: "Null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        defaultNew: "",
                        defaultOld: "",
                        required: false
                    }
                },
                description: `**O'zlashtirish yozuvlarini qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicScore/search

<b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
            },
    {
                id: 7,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "O'zlashtirish yozuvlarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON)",
                        type: "textarea",
                        placeholder: '{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}',
                        defaultNew: '{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}',
                        defaultOld: '{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}',
                        required: false,
                        rows: 3
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        defaultNew: "50",
                        defaultOld: "50",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        defaultNew: "0",
                        defaultOld: "0",
                        required: false
                    },
                    returnNulls: {
                        label: "Null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        defaultNew: "",
                        defaultOld: "",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["filter", "limit", "offset"],
                description: `**O'zlashtirish yozuvlarini qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicScore/search

<b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_34;
}
