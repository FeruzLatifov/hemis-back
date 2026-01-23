// 32.Akademik guruhlar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_32 = [
    // ============================================
    // 32.Akademik guruhlar (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "32.Akademik guruhlar",
                name: "Barcha akademik guruhlar yozuvlarini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup",
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
                description: `**Barcha akademik guruhlar yozuvlarini olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicGroup

<b>Parameters:</b>
- limit: Sahifa hajmi (default: 50)
- offset: Boshlanish indeksi (default: 0)
- returnCount: Umumiy sonni X-Total-Count headerda qaytarish
- returnNulls: Null qiymatlarni ham qaytarish

<b>Response:</b> Sahifalangan akademik guruhlar ro'yxati`,
                ported: true
            },
    {
                id: 2,
                category: "32.Akademik guruhlar",
                name: "Akademik guruh yozuvini ID bo'yicha olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
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
                description: `**Akademik guruh yozuvini ID bo'yicha olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}

<b>Parameters:</b>
- entityId: Yozuv UUID si (path parameter)
- returnNulls: Null qiymatlarni ham qaytarish

<b>Response:</b> Topilgan akademik guruh yozuvi yoki 404`,
                ported: true
            },
    {
                id: 3,
                category: "32.Akademik guruhlar",
                name: "Yangi akademik guruh yozuvi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup",
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
                        defaultNew: "Test Universiteti (NEW)",
                        defaultOld: "Test Universiteti (OLD)",
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
                    educationFormCode: {
                        label: "Ta'lim shakli kodi",
                        type: "text",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: false
                    },
                    educationFormName: {
                        label: "Ta'lim shakli nomi",
                        type: "text",
                        defaultNew: "Kunduzgi",
                        defaultOld: "Kunduzgi",
                        required: false
                    },
                    educationYearCode: {
                        label: "O'quv yili kodi",
                        type: "text",
                        defaultNew: "2024",
                        defaultOld: "2024",
                        required: false
                    },
                    educationYearName: {
                        label: "O'quv yili nomi",
                        type: "text",
                        defaultNew: "2024-2025",
                        defaultOld: "2024-2025",
                        required: false
                    },
                    groupCount: {
                        label: "Guruhlar soni",
                        type: "number",
                        defaultNew: "10",
                        defaultOld: "10",
                        required: false
                    },
                    updateDate: {
                        label: "Yangilangan sana",
                        type: "date",
                        defaultNew: new Date().toISOString().split('T')[0],
                        defaultOld: new Date().toISOString().split('T')[0],
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["universityCode", "universityName", "educationTypeCode", "educationTypeName", "educationFormCode", "educationFormName", "educationYearCode", "educationYearName", "groupCount", "updateDate"],
                description: `**Yangi akademik guruh yozuvi yaratish**

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicGroup

<b>Body:</b>
<pre>{
  "universityCode": "401",
  "universityName": "Test Universiteti",
  "educationTypeCode": "11",
  "educationTypeName": "Bakalavr",
  "educationFormCode": "11",
  "educationFormName": "Kunduzgi",
  "educationYearCode": "2024",
  "educationYearName": "2024-2025",
  "groupCount": 10,
  "updateDate": "2024-01-01"
}</pre>

<b>Response:</b> Yaratilgan akademik guruh yozuvi`,
                ported: true
            },
    {
                id: 4,
                category: "32.Akademik guruhlar",
                name: "Akademik guruh yozuvini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                        defaultNew: "",
                        defaultOld: "",
                        required: true
                    },
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
                        defaultNew: "Yangilangan Universitet (NEW)",
                        defaultOld: "Yangilangan Universitet (OLD)",
                        required: false
                    },
                    groupCount: {
                        label: "Guruhlar soni",
                        type: "number",
                        defaultNew: "15",
                        defaultOld: "15",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["universityCode", "universityName", "groupCount"],
                description: `**Akademik guruh yozuvini yangilash**

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}

<b>Parameters:</b>
- entityId: Yozuv UUID si (path parameter)

<b>Body:</b>
<pre>{
  "universityCode": "401",
  "universityName": "Yangilangan Universitet",
  "groupCount": 15
}</pre>

<b>Response:</b> Yangilangan akademik guruh yozuvi yoki 404`,
                ported: true
            },
    {
                id: 5,
                category: "32.Akademik guruhlar",
                name: "Akademik guruh yozuvini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                        defaultNew: "",
                        defaultOld: "",
                        required: true
                    }
                },
                description: `**Akademik guruh yozuvini o'chirish (soft delete)**

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}

<b>Parameters:</b>
- entityId: Yozuv UUID si (path parameter)

<b>Response:</b> 200 OK yoki 404 Not Found

<b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
                ported: true
            },
    {
                id: 6,
                category: "32.Akademik guruhlar",
                name: "Akademik guruhlarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON)",
                        type: "textarea",
                        placeholder: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                        defaultNew: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                        defaultOld: '{"conditions":[{"property":"universityCode","operator":"=","value":"351"}]}',
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
                description: `**Akademik guruhlarni qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicGroup/search

<b>Parameters:</b>
- filter: CUBA format filter JSON (URL encoded)
- limit, offset: Sahifalash

<b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
            },
    {
                id: 7,
                category: "32.Akademik guruhlar",
                name: "Akademik guruhlarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup/search",
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
                description: `**Akademik guruhlarni qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicGroup/search

<b>Body:</b> CUBA format filter JSON
<pre>{"filter":{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}}</pre>

<b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_32;
}
