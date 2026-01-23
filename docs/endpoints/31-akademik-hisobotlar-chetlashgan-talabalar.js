// 31.Akademik hisobotlar chetlashgan talabalar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_31 = [
    // ============================================
    // 31.Akademik hisobotlar chetlashgan talabalar (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Barcha chetlashgan talabalar yozuvlarini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RExpel",
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
                    returnNulls: {
                        label: "Null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        defaultNew: "",
                        defaultOld: "",
                        required: false
                    }
                },
                description: `**Barcha chetlashgan talabalar yozuvlarini olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RExpel

<b>Parameters:</b>
- limit: Natija soni limiti (default: 50)
- offset: Sahifalash uchun offset

<b>Response:</b> Chetlashgan talabalar ro'yxati CUBA formatda`,
                ported: true
            },
    {
                id: 2,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Chetlashgan talaba yozuvini olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RExpel/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "#1 dan yoki #3 dan qaytgan ID ni kiriting",
                        defaultNew: "",
                        defaultOld: "",
                        required: true
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
                description: `**Chetlashgan talaba yozuvini ID bo'yicha olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RExpel/{entityId}

<b>Parameters:</b>
- entityId: Yozuv ID (UUID format)

<b>Response:</b> Yozuv CUBA formatda`,
                ported: true
            },
    {
                id: 3,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Yangi chetlashgan talaba yozuvi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RExpel",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                    universityCode: {
                        label: "Universitet kodi",
                        type: "text",
                        placeholder: "401",
                        defaultNew: "401",
                        defaultOld: "351",
                        required: false
                    },
                    universityName: {
                        label: "Universitet nomi",
                        type: "text",
                        placeholder: "O'zbekiston Milliy universiteti",
                        defaultNew: "O'zbekiston Milliy universiteti",
                        defaultOld: "Samarqand davlat universiteti",
                        required: false
                    },
                    facultyCode: {
                        label: "Fakultet kodi",
                        type: "text",
                        placeholder: "401-101",
                        defaultNew: "401-101",
                        defaultOld: "351-101",
                        required: false
                    },
                    facultyName: {
                        label: "Fakultet nomi",
                        type: "text",
                        placeholder: "Matematika fakulteti",
                        defaultNew: "Matematika fakulteti",
                        defaultOld: "Fizika fakulteti",
                        required: false
                    },
                    educationTypeCode: {
                        label: "Ta'lim turi kodi",
                        type: "text",
                        placeholder: "11",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: false
                    },
                    educationTypeName: {
                        label: "Ta'lim turi nomi",
                        type: "text",
                        placeholder: "Kunduzgi",
                        defaultNew: "Kunduzgi",
                        defaultOld: "Kunduzgi",
                        required: false
                    },
                    educationYearCode: {
                        label: "O'quv yili kodi",
                        type: "text",
                        placeholder: "2024",
                        defaultNew: "2024",
                        defaultOld: "2024",
                        required: false
                    },
                    educationYearName: {
                        label: "O'quv yili nomi",
                        type: "text",
                        placeholder: "2024-2025",
                        defaultNew: "2024-2025",
                        defaultOld: "2024-2025",
                        required: false
                    },
                    semesterTypeCode: {
                        label: "Semestr turi kodi",
                        type: "text",
                        placeholder: "1",
                        defaultNew: "1",
                        defaultOld: "1",
                        required: false
                    },
                    semesterTypeName: {
                        label: "Semestr turi nomi",
                        type: "text",
                        placeholder: "Kuz semestri",
                        defaultNew: "Kuz semestri",
                        defaultOld: "Kuz semestri",
                        required: false
                    },
                    courseCode: {
                        label: "Kurs kodi",
                        type: "text",
                        placeholder: "1",
                        defaultNew: "1",
                        defaultOld: "2",
                        required: false
                    },
                    courseName: {
                        label: "Kurs nomi",
                        type: "text",
                        placeholder: "1-kurs",
                        defaultNew: "1-kurs",
                        defaultOld: "2-kurs",
                        required: false
                    },
                    expelReasonCode: {
                        label: "Chetlashtirish sababi kodi",
                        type: "text",
                        placeholder: "11",
                        defaultNew: "11",
                        defaultOld: "12",
                        required: false
                    },
                    expelReasonName: {
                        label: "Chetlashtirish sababi nomi",
                        type: "text",
                        placeholder: "O'z xohishiga binoan",
                        defaultNew: "O'z xohishiga binoan",
                        defaultOld: "O'qishni ko'chirish sababli",
                        required: false
                    },
                    expelCount: {
                        label: "Chetlashgan talabalar soni",
                        type: "number",
                        placeholder: "5",
                        defaultNew: "3",
                        defaultOld: "2",
                        required: false
                    }
                },
                bodyFields: ["universityCode", "universityName", "facultyCode", "facultyName", "educationTypeCode", "educationTypeName", "educationYearCode", "educationYearName", "semesterTypeCode", "semesterTypeName", "courseCode", "courseName", "expelReasonCode", "expelReasonName", "expelCount"],
                description: `**Yangi chetlashgan talaba yozuvi yaratish**

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RExpel

<b>Body:</b> JSON formatda yozuv ma'lumotlari
<pre>{
  "universityCode": "401",
  "universityName": "O'zbekiston Milliy universiteti",
  "facultyCode": "FAKU001",
  "facultyName": "Matematika fakulteti",
  "educationTypeCode": "11",
  "educationTypeName": "Kunduzgi",
  "expelReasonCode": "01",
  "expelReasonName": "O'z ixtiyori bilan",
  "expelCount": 5
}</pre>

<b>Response:</b> Yaratilgan yozuv CUBA formatda`,
                ported: true
            },
    {
                id: 4,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Chetlashgan talaba yozuvini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RExpel/{entityId}",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "#3 dan qaytgan ID ni kiriting",
                        defaultNew: "",
                        defaultOld: "",
                        required: true
                    },
                    universityCode: {
                        label: "Universitet kodi",
                        type: "text",
                        placeholder: "401",
                        defaultNew: "401",
                        defaultOld: "351",
                        required: false
                    },
                    universityName: {
                        label: "Universitet nomi",
                        type: "text",
                        placeholder: "O'zbekiston Milliy universiteti",
                        defaultNew: "O'zbekiston Milliy universiteti (YANGILANGAN)",
                        defaultOld: "Samarqand davlat universiteti (YANGILANGAN)",
                        required: false
                    },
                    facultyCode: {
                        label: "Fakultet kodi",
                        type: "text",
                        placeholder: "401-102",
                        defaultNew: "401-102",
                        defaultOld: "351-102",
                        required: false
                    },
                    facultyName: {
                        label: "Fakultet nomi",
                        type: "text",
                        placeholder: "Informatika fakulteti",
                        defaultNew: "Informatika fakulteti",
                        defaultOld: "Kimyo fakulteti",
                        required: false
                    },
                    educationTypeCode: {
                        label: "Ta'lim turi kodi",
                        type: "text",
                        placeholder: "12",
                        defaultNew: "12",
                        defaultOld: "12",
                        required: false
                    },
                    educationTypeName: {
                        label: "Ta'lim turi nomi",
                        type: "text",
                        placeholder: "Sirtqi",
                        defaultNew: "Sirtqi",
                        defaultOld: "Sirtqi",
                        required: false
                    },
                    educationYearCode: {
                        label: "O'quv yili kodi",
                        type: "text",
                        placeholder: "2025",
                        defaultNew: "2025",
                        defaultOld: "2025",
                        required: false
                    },
                    educationYearName: {
                        label: "O'quv yili nomi",
                        type: "text",
                        placeholder: "2025-2026",
                        defaultNew: "2025-2026",
                        defaultOld: "2025-2026",
                        required: false
                    },
                    semesterTypeCode: {
                        label: "Semestr turi kodi",
                        type: "text",
                        placeholder: "2",
                        defaultNew: "2",
                        defaultOld: "2",
                        required: false
                    },
                    semesterTypeName: {
                        label: "Semestr turi nomi",
                        type: "text",
                        placeholder: "Bahor semestri",
                        defaultNew: "Bahor semestri",
                        defaultOld: "Bahor semestri",
                        required: false
                    },
                    courseCode: {
                        label: "Kurs kodi",
                        type: "text",
                        placeholder: "3",
                        defaultNew: "3",
                        defaultOld: "4",
                        required: false
                    },
                    courseName: {
                        label: "Kurs nomi",
                        type: "text",
                        placeholder: "3-kurs",
                        defaultNew: "3-kurs",
                        defaultOld: "4-kurs",
                        required: false
                    },
                    expelReasonCode: {
                        label: "Chetlashtirish sababi kodi",
                        type: "text",
                        placeholder: "13",
                        defaultNew: "13",
                        defaultOld: "14",
                        required: false
                    },
                    expelReasonName: {
                        label: "Chetlashtirish sababi nomi",
                        type: "text",
                        placeholder: "Akademik qarzdorlik",
                        defaultNew: "Akademik qarzdorlik",
                        defaultOld: "Intizom buzarlik",
                        required: false
                    },
                    expelCount: {
                        label: "Chetlashgan talabalar soni",
                        type: "number",
                        placeholder: "10",
                        defaultNew: "5",
                        defaultOld: "7",
                        required: false
                    }
                },
                bodyFields: ["universityCode", "universityName", "facultyCode", "facultyName", "educationTypeCode", "educationTypeName", "educationYearCode", "educationYearName", "semesterTypeCode", "semesterTypeName", "courseCode", "courseName", "expelReasonCode", "expelReasonName", "expelCount"],
                description: `**Chetlashgan talaba yozuvini yangilash**

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RExpel/{entityId}

<b>Parameters:</b>
- entityId: Yozuv ID (UUID format)

<b>Body:</b> JSON formatda yangilash ma'lumotlari

<b>Response:</b> Yangilangan yozuv CUBA formatda`,
                ported: true
            },
    {
                id: 5,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Chetlashgan talaba yozuvini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RExpel/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "#3 yoki #4 dan qaytgan ID ni kiriting",
                        defaultNew: "",
                        defaultOld: "",
                        required: true
                    }
                },
                description: `**Chetlashgan talaba yozuvini o'chirish**

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RExpel/{entityId}

<b>Parameters:</b>
- entityId: Yozuv ID (UUID format)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            },
    {
                id: 6,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Chetlashgan talabalarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RExpel/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON)",
                        type: "textarea",
                        placeholder: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                        defaultNew: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                        defaultOld: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                        required: true,
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
                description: `**Chetlashgan talabalarni qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RExpel/search?filter=...

<b>Parameters:</b>
- filter: CUBA format filter JSON (URL encoded)
- limit, offset: Sahifalash

<b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
            },
    {
                id: 7,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Chetlashgan talabalarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RExpel/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON)",
                        type: "textarea",
                        placeholder: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                        defaultNew: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                        defaultOld: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
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
                description: `**Chetlashgan talabalarni qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RExpel/search

<b>Body:</b> CUBA format filter JSON
<pre>{"filter":{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}}</pre>

<b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_31;
}
