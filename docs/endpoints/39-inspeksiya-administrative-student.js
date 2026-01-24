// 39.Inspeksiya administrative student endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Xorij OTMlari bilan akademik almashinuv dasturlari - talabalar

const endpoints_39 = [
    // ============================================
    // 39.Inspeksiya administrative student (7 endpoint)
    // ============================================
    {
        id: 1,
        category: "39.Inspeksiya administrative student",
        name: "Yangi yozuv yaratish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2",
        requiresAuth: true,
        dependsOn: 1,
        description: "Xorij OTMlari bilan akademik almashinuv - yangi talaba yozuvi yaratish (OLD-HEMIS CUBA format)",
        storeResultId: "administrativeStudent2EntityId",
        inputFields: {
            university: {
                label: "Universitet kodi",
                type: "text",
                required: false,
                placeholder: "301",
                defaultNew: "301",
                defaultOld: "301",
                helpText: "OTM kodi (masalan: 301). Request body: {\"code\": \"301\"}"
            },
            educationYear: {
                label: "O'quv yili",
                type: "text",
                required: false,
                placeholder: "2024",
                defaultNew: "2024",
                defaultOld: "2024",
                helpText: "O'quv yili kodi (masalan: 2024). Request body: {\"code\": \"2024\"}"
            },
            country: {
                label: "Davlat kodi",
                type: "text",
                required: false,
                placeholder: "US",
                defaultNew: "US",
                defaultOld: "US",
                helpText: "ISO country code (masalan: US, GB, DE). Request body: {\"code\": \"US\"}"
            },
            educationType: {
                label: "Ta'lim turi kodi",
                type: "text",
                required: false,
                placeholder: "11",
                defaultNew: "11",
                defaultOld: "11",
                helpText: "Ta'lim turi kodi. Request body: {\"code\": \"11\"}"
            },
            exchangeDocument: {
                label: "Shartnoma/Hujjat",
                type: "text",
                required: false,
                placeholder: "Shartnoma raqami",
                defaultNew: "SH-2024-NEW",
                defaultOld: "SH-2024-OLD"
            },
            exchangeType: {
                label: "Almashinuv turi",
                type: "text",
                required: false,
                placeholder: "outcome / income",
                defaultNew: "outcome",
                defaultOld: "outcome"
            },
            studentFullname: {
                label: "Talaba FIO",
                type: "text",
                required: true,
                placeholder: "Familiya Ism Otasining ismi",
                defaultNew: "Yangi Talaba Testovich",
                defaultOld: "Eski Talaba Testovich"
            },
            exchangeUniversityName: {
                label: "Xorij OTM nomi",
                type: "text",
                required: false,
                placeholder: "Harvard University",
                defaultNew: "Harvard University",
                defaultOld: "Harvard University"
            },
            specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                required: false,
                placeholder: "Informatika va AT",
                defaultNew: "Informatika va AT (NEW)",
                defaultOld: "Informatika va AT (OLD)"
            },
            specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                required: false,
                placeholder: "5110100",
                defaultNew: "5110100",
                defaultOld: "5110100"
            }
        },
        bodyGenerator: (fields) => {
            // OLD-HEMIS CUBA format: FK fields use {"code": "..."} format
            const body = {};

            // FK fields - {"code": "..."} format
            if (fields.university) body.university = { code: fields.university };
            if (fields.educationYear) body.educationYear = { code: fields.educationYear };
            if (fields.country) body.country = { code: fields.country };
            if (fields.educationType) body.educationType = { code: fields.educationType };

            // Simple string fields
            if (fields.exchangeDocument) body.exchangeDocument = fields.exchangeDocument;
            if (fields.exchangeType) body.exchangeType = fields.exchangeType;
            if (fields.studentFullname) body.studentFullname = fields.studentFullname;
            if (fields.exchangeUniversityName) body.exchangeUniversityName = fields.exchangeUniversityName;
            if (fields.specialityName) body.specialityName = fields.specialityName;
            if (fields.specialityCode) body.specialityCode = fields.specialityCode;

            return body;
        },
        ported: true
    },
    {
        id: 2,
        category: "39.Inspeksiya administrative student",
        name: "Barcha yozuvlarni olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2",
        requiresAuth: true,
        dependsOn: 1,
        description: "Barcha akademik almashinuv yozuvlarini olish (paginated). Qaytgan javobdan random ID tanlab GET/PUT/DELETE inputlariga qo'yadi.",
        storeFirstId: "administrativeStudent2EntityId",
        inputFields: {
            limit: {
                label: "Limit (nechta yozuv)",
                type: "number",
                required: false,
                placeholder: "50",
                default: "50"
            },
            offset: {
                label: "Offset (qayerdan boshlash)",
                type: "number",
                required: false,
                placeholder: "0",
                default: "0"
            },
            returnCount: {
                label: "Jami sonni qaytarish",
                type: "select",
                options: [
                    { value: "true", label: "Ha (true)" },
                    { value: "false", label: "Yo'q (false)" }
                ],
                default: "true",
                required: false
            },
            returnNulls: {
                label: "Null qiymatlarni qaytarish",
                type: "select",
                options: [
                    { value: "false", label: "Yo'q (false)" },
                    { value: "true", label: "Ha (true)" }
                ],
                default: "false",
                required: false
            }
        },
        ported: true
    },
    {
        id: 3,
        category: "39.Inspeksiya administrative student",
        name: "ID bo'yicha olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Akademik almashinuv yozuvini UUID bo'yicha olish",
        inputFields: {
            entityId: {
                label: "Entity UUID",
                type: "text",
                required: true,
                placeholder: "Avval GET All ishlatib ID oling",
                useStoredId: "administrativeStudent2EntityId"
            }
        },
        queryParams: [
            { name: "returnNulls", label: "Null qiymatlarni qaytarish", defaultValue: "false" }
        ],
        ported: true
    },
    {
        id: 4,
        category: "39.Inspeksiya administrative student",
        name: "Yozuvni yangilash (PUT)",
        method: "PUT",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Mavjud akademik almashinuv yozuvini yangilash (OLD-HEMIS CUBA format)",
        inputFields: {
            entityId: {
                label: "Entity UUID",
                type: "text",
                required: true,
                placeholder: "Avval GET All ishlatib ID oling",
                useStoredId: "administrativeStudent2EntityId"
            },
            university: {
                label: "Universitet kodi",
                type: "text",
                required: false,
                placeholder: "301",
                defaultNew: "",
                defaultOld: "",
                helpText: "OTM kodi (masalan: 301). Request body: {\"code\": \"301\"}"
            },
            educationYear: {
                label: "O'quv yili",
                type: "text",
                required: false,
                placeholder: "2024",
                defaultNew: "",
                defaultOld: "",
                helpText: "O'quv yili kodi (masalan: 2024). Request body: {\"code\": \"2024\"}"
            },
            country: {
                label: "Davlat kodi",
                type: "text",
                required: false,
                placeholder: "US",
                defaultNew: "",
                defaultOld: "",
                helpText: "ISO country code (masalan: US, GB, DE). Request body: {\"code\": \"US\"}"
            },
            educationType: {
                label: "Ta'lim turi kodi",
                type: "text",
                required: false,
                placeholder: "11",
                defaultNew: "",
                defaultOld: "",
                helpText: "Ta'lim turi kodi. Request body: {\"code\": \"11\"}"
            },
            studentFullname: {
                label: "Talaba FIO",
                type: "text",
                required: false,
                placeholder: "Familiya Ism Otasining ismi",
                defaultNew: "Yangilangan NEW Talaba",
                defaultOld: "Yangilangan OLD Talaba"
            },
            exchangeUniversityName: {
                label: "Xorij OTM nomi",
                type: "text",
                required: false,
                placeholder: "Harvard University",
                defaultNew: "MIT",
                defaultOld: "MIT"
            },
            exchangeDocument: {
                label: "Shartnoma/Hujjat",
                type: "text",
                required: false,
                placeholder: "Shartnoma raqami",
                defaultNew: "SH-2024-UPD-NEW",
                defaultOld: "SH-2024-UPD-OLD"
            },
            specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                required: false,
                placeholder: "5110100",
                defaultNew: "5110200",
                defaultOld: "5110200"
            },
            specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                required: false,
                placeholder: "Informatika va AT",
                defaultNew: "Kompyuter muhandisligi (NEW)",
                defaultOld: "Kompyuter muhandisligi (OLD)"
            },
            exchangeType: {
                label: "Almashinuv turi",
                type: "text",
                required: false,
                placeholder: "outcome / income",
                defaultNew: "income",
                defaultOld: "income"
            }
        },
        bodyGenerator: (fields) => {
            // OLD-HEMIS CUBA format: FK fields use {"code": "..."} format
            const body = {};

            // FK fields - {"code": "..."} format
            if (fields.university) body.university = { code: fields.university };
            if (fields.educationYear) body.educationYear = { code: fields.educationYear };
            if (fields.country) body.country = { code: fields.country };
            if (fields.educationType) body.educationType = { code: fields.educationType };

            // Simple string fields
            if (fields.studentFullname) body.studentFullname = fields.studentFullname;
            if (fields.exchangeUniversityName) body.exchangeUniversityName = fields.exchangeUniversityName;
            if (fields.exchangeDocument) body.exchangeDocument = fields.exchangeDocument;
            if (fields.specialityCode) body.specialityCode = fields.specialityCode;
            if (fields.specialityName) body.specialityName = fields.specialityName;
            if (fields.exchangeType) body.exchangeType = fields.exchangeType;

            return body;
        },
        ported: true
    },
    {
        id: 5,
        category: "39.Inspeksiya administrative student",
        name: "Yozuvni o'chirish (DELETE)",
        method: "DELETE",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Akademik almashinuv yozuvini o'chirish (soft delete)",
        inputFields: {
            entityId: {
                label: "Entity UUID",
                type: "text",
                required: true,
                placeholder: "Avval GET All ishlatib ID oling",
                useStoredId: "administrativeStudent2EntityId"
            }
        },
        ported: true
    },
    {
        id: 6,
        category: "39.Inspeksiya administrative student",
        name: "Qidirish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/search",
        requiresAuth: true,
        dependsOn: 1,
        inputFields: {
            filter: {
                label: "CUBA JSON filter (ixtiyoriy)",
                type: "textarea",
                rows: 3,
                placeholder: 'Bo\'sh qoldiring yoki: {"conditions":[...]}',
                defaultNew: '',
                defaultOld: '',
                required: false,
                helpText: 'Bo\'sh qoldiring - barcha yozuvlar qaytadi. Yoki CUBA filter: {"conditions":[{"property":"exchangeType","operator":"=","value":"outcome"}]}'
            },
            limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "10",
                defaultOld: "10",
                required: false
            },
            offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
            },
            returnNulls: {
                label: "Null qiymatlarni qaytarish",
                type: "select",
                options: [
                    { value: "", label: "default" },
                    { value: "true", label: "true" },
                    { value: "false", label: "false" }
                ],
                defaultNew: "",
                defaultOld: "",
                required: false
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            // Filter: CUBA format - {"conditions":[...]}
            // Bo'sh bo'lsa {"conditions":[]} yuborish kerak (aks holda 500 xato)
            if (fields.filter && fields.filter.trim()) {
                params.push("filter=" + encodeURIComponent(fields.filter.trim()));
            } else {
                // Bo'sh filter = {"conditions":[]} yuborish kerak
                params.push("filter=" + encodeURIComponent('{"conditions":[]}'));
            }
            if (fields.limit) params.push("limit=" + encodeURIComponent(fields.limit));
            if (fields.offset) params.push("offset=" + encodeURIComponent(fields.offset));
            if (fields.returnNulls) params.push("returnNulls=" + encodeURIComponent(fields.returnNulls));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Akademik almashinuv yozuvlarini qidirish (GET)**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeStudent2/search

<b>Parametrlar:</b>
- filter: CUBA JSON filter (masalan: {"conditions":[{"property":"exchangeType","operator":"=","value":"outcome"}]})
- limit: Natijalar soni (default: 50)
- offset: Boshlanish indeksi (default: 0)
- returnNulls: Null qiymatlarni qaytarish

<b>Response:</b> Filtrlangan yozuvlar ro'yxati`,
        ported: true
    },
    {
        id: 7,
        category: "39.Inspeksiya administrative student",
        name: "Qidirish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/search",
        requiresAuth: true,
        dependsOn: 1,
        inputFields: {
            filter: {
                label: "CUBA JSON filter",
                type: "textarea",
                rows: 3,
                placeholder: '{"conditions":[{"property":"exchangeType","operator":"=","value":"outcome"}]}',
                defaultNew: '',
                defaultOld: '',
                required: false,
                helpText: 'CUBA filter formati: {"conditions":[{"property":"field","operator":"=","value":"qiymat"}]}'
            },
            limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "50",
                defaultOld: "50",
                required: false
            },
            offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
            }
        },
        bodyGenerator: (fields) => {
            // CUBA POST search formati: {"filter": {...}, "limit": N, "offset": N}
            // Hammasi BODY da bo'lishi kerak!
            const body = {
                limit: parseInt(fields.limit) || 50,
                offset: parseInt(fields.offset) || 0
            };
            if (fields.filter && fields.filter.trim()) {
                try {
                    body.filter = JSON.parse(fields.filter);
                } catch (e) {
                    body.filter = { conditions: [] };
                }
            } else {
                // Bo'sh filter = {"conditions":[]} (CUBA talabi)
                body.filter = { conditions: [] };
            }
            return body;
        },
        description: `**Akademik almashinuv yozuvlarini qidirish (POST)**

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeStudent2/search

<b>CUBA Format (hammasi body da):</b>
{
  "filter": {"conditions": [...]},
  "limit": 50,
  "offset": 0
}

<b>Response:</b> Filtrlangan yozuvlar ro'yxati`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_39;
}
