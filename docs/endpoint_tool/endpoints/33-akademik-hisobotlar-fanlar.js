// 33.Akademik hisobotlar fanlar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_33 = [
    // ============================================
    // 33.Akademik hisobotlar fanlar (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "33.Akademik hisobotlar fanlar",
                name: "Barcha fanlar yozuvlarini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects",
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
                description: `**Barcha fanlar yozuvlarini olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicSubjects

<b>Parameters:</b>
- limit: Sahifa hajmi (default: 50)
- offset: Boshlanish indeksi (default: 0)
- returnCount: Umumiy sonni X-Total-Count headerda qaytarish
- returnNulls: Null qiymatlarni ham qaytarish

<b>Response:</b> Sahifalangan fanlar ro'yxati`,
                ported: true
            },
    {
                id: 2,
                category: "33.Akademik hisobotlar fanlar",
                name: "Fan yozuvini ID bo'yicha olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}",
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
                description: `**Fan yozuvini ID bo'yicha olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}

<b>Parameters:</b>
- entityId: Yozuv UUID si (path parameter)
- returnNulls: Null qiymatlarni ham qaytarish

<b>Response:</b> Topilgan fan yozuvi yoki 404`,
                ported: true
            },
    {
                id: 3,
                category: "33.Akademik hisobotlar fanlar",
                name: "Yangi fan yozuvi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects",
                requiresAuth: true,
                inputFields: {
                    universityCode: {
                        label: "Universitet kodi",
                        type: "text",
                        defaultNew: "311",
                        defaultOld: "313",
                        required: false
                    },
                    universityName: {
                        label: "Universitet nomi",
                        type: "text",
                        defaultNew: "Namangan muhandislik-texnologiya instituti",
                        defaultOld: "Samarqand davlat arxitektura-qurilish universiteti",
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
                        defaultNew: "2023",
                        defaultOld: "2023",
                        required: false
                    },
                    educationYearName: {
                        label: "O'quv yili nomi",
                        type: "text",
                        defaultNew: "2023",
                        defaultOld: "2023",
                        required: false
                    },
                    curriculumCode: {
                        label: "O'quv rejasi kodi",
                        type: "text",
                        defaultNew: "6",
                        defaultOld: "4",
                        required: false
                    },
                    curriculumName: {
                        label: "O'quv rejasi nomi",
                        type: "text",
                        defaultNew: "240",
                        defaultOld: "337",
                        required: false
                    },
                    blockCode: {
                        label: "Blok kodi",
                        type: "text",
                        defaultNew: "4",
                        defaultOld: "4",
                        required: false
                    },
                    blockName: {
                        label: "Blok nomi",
                        type: "text",
                        defaultNew: "8",
                        defaultOld: "4",
                        required: false
                    },
                    subjectCount: {
                        label: "Fanlar soni",
                        type: "number",
                        defaultNew: "1135",
                        defaultOld: "1846",
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
                bodyFields: ["universityCode", "universityName", "educationTypeCode", "educationTypeName", "educationYearCode", "educationYearName", "curriculumCode", "curriculumName", "blockCode", "blockName", "subjectCount", "updateDate"],
                description: `**Yangi fan yozuvi yaratish**

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicSubjects

<b>Body:</b>
<pre>{
  "universityCode": "311",
  "universityName": "Namangan muhandislik-texnologiya instituti",
  "educationTypeCode": "11",
  "educationTypeName": "Bakalavr",
  "educationYearCode": "2024",
  "educationYearName": "2024-2025",
  "curriculumCode": "CS-2024",
  "curriculumName": "Kompyuter fanlari",
  "blockCode": "B1",
  "blockName": "Asosiy fanlar",
  "subjectCount": 15,
  "updateDate": "2024-01-01"
}</pre>

<b>Response:</b> Yaratilgan fan yozuvi`,
                ported: true
            },
    {
                id: 4,
                category: "33.Akademik hisobotlar fanlar",
                name: "Fan yozuvini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}",
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
                        defaultNew: "311",
                        defaultOld: "313",
                        required: false
                    },
                    universityName: {
                        label: "Universitet nomi",
                        type: "text",
                        defaultNew: "Yangilangan Universitet (NEW)",
                        defaultOld: "Yangilangan Universitet (OLD)",
                        required: false
                    },
                    subjectCount: {
                        label: "Fanlar soni",
                        type: "number",
                        defaultNew: "1200",
                        defaultOld: "1900",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["universityCode", "universityName", "subjectCount"],
                description: `**Fan yozuvini yangilash**

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}

<b>Parameters:</b>
- entityId: Yozuv UUID si (path parameter)

<b>Body:</b>
<pre>{
  "universityCode": "311",
  "universityName": "Yangilangan Universitet",
  "subjectCount": 1200
}</pre>

<b>Response:</b> Yangilangan fan yozuvi yoki 404`,
                ported: true
            },
    {
                id: 5,
                category: "33.Akademik hisobotlar fanlar",
                name: "Fan yozuvini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}",
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
                description: `**Fan yozuvini o'chirish (soft delete)**

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}

<b>Parameters:</b>
- entityId: Yozuv UUID si (path parameter)

<b>Response:</b> 200 OK yoki 404 Not Found

<b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
                ported: true
            },
    {
                id: 6,
                category: "33.Akademik hisobotlar fanlar",
                name: "Fanlarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON)",
                        type: "textarea",
                        placeholder: '{"conditions":[{"property":"universityCode","operator":"=","value":"311"}]}',
                        defaultNew: '{"conditions":[{"property":"universityCode","operator":"=","value":"311"}]}',
                        defaultOld: '{"conditions":[{"property":"universityCode","operator":"=","value":"313"}]}',
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
                description: `**Fanlarni qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicSubjects/search

<b>Parameters:</b>
- filter: CUBA format filter JSON (URL encoded)
- limit, offset: Sahifalash

<b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
            },
    {
                id: 7,
                category: "33.Akademik hisobotlar fanlar",
                name: "Fanlarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects/search",
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
                description: `**Fanlarni qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicSubjects/search

<b>Body:</b> CUBA format filter JSON
<pre>{"filter":{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}}</pre>

<b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_33;
}
