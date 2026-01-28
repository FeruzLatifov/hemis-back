// 37.Bandlik statistikasi endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_37 = [
    // ============================================
    // 37.Bandlik statistikasi (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "37.Bandlik statistikasi",
                name: "Yangi bandlik yozuvi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_REmployment",
                requiresAuth: true,
                inputFields: {
                    uId: {
                        label: "Universitet ID (raqam)",
                        type: "text",
                        defaultNew: "401",
                        defaultOld: "351",
                        required: false
                    },
                    qty: {
                        label: "Miqdor (talabalar soni)",
                        type: "number",
                        defaultNew: "5",
                        defaultOld: "5",
                        required: false
                    },
                    university: {
                        label: "Universitet kodi",
                        type: "text",
                        defaultNew: "401",
                        defaultOld: "351",
                        required: false
                    },
                    department: {
                        label: "Bo'lim kodi",
                        type: "text",
                        defaultNew: "401-102-08",
                        defaultOld: "351-118",
                        placeholder: "Yangi: 401-102-08, Eski: 351-118",
                        required: false
                    },
                    educationYear: {
                        label: "Ta'lim yili kodi",
                        type: "text",
                        defaultNew: "2026",
                        defaultOld: "2021",
                        placeholder: "2026, 2027, 2028...",
                        required: false
                    },
                    educationType: {
                        label: "Ta'lim turi kodi",
                        type: "text",
                        defaultNew: "11",
                        defaultOld: "11",
                        placeholder: "11=Bakalavr, 12=Magistr...",
                        required: false
                    },
                    educationForm: {
                        label: "Ta'lim shakli kodi",
                        type: "text",
                        defaultNew: "11",
                        defaultOld: "11",
                        placeholder: "11=Kunduzgi, 22=Sirtqi...",
                        required: false
                    },
                    paymentForm: {
                        label: "To'lov shakli kodi",
                        type: "text",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: false
                    },
                    gender: {
                        label: "Jins kodi",
                        type: "text",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: false
                    },
                    workplaceCompatibility: {
                        label: "Ish joyi mosligi kodi",
                        type: "text",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: false
                    },
                    graduateInactiveType: {
                        label: "Bitiruvchi nofaol turi kodi",
                        type: "text",
                        defaultNew: "13",
                        defaultOld: "13",
                        required: false
                    },
                    graduateFieldsType: {
                        label: "Bitiruvchi soha turi kodi",
                        type: "text",
                        defaultNew: "31",
                        defaultOld: "31",
                        required: false
                    }
                },
                hasBody: true,
                storeResultId: "createdREmploymentId",
                bodyGenerator: (inputs) => {
                    // UNIFIED FORMAT: Ikkala tizim uchun underscore bilan (_university)
                    // NEW-HEMIS ham OLD-HEMIS formatini qabul qiladi
                    let item = {};
                    if (inputs.uId) item.uId = inputs.uId;  // String sifatida
                    if (inputs.qty) item.qty = parseInt(inputs.qty);

                    // FK maydonlar - underscore bilan (OLD-HEMIS format)
                    if (inputs.university) item._university = {code: inputs.university};
                    if (inputs.department) item._department = {code: inputs.department};
                    if (inputs.educationYear) item._educationYear = {code: inputs.educationYear};
                    if (inputs.educationType) item._educationType = {code: inputs.educationType};
                    if (inputs.educationForm) item._educationForm = {code: inputs.educationForm};
                    if (inputs.paymentForm) item._paymentForm = {code: inputs.paymentForm};
                    if (inputs.gender) item._gender = {code: inputs.gender};
                    if (inputs.workplaceCompatibility) item._workplaceCompatibility = {code: inputs.workplaceCompatibility};
                    if (inputs.graduateInactiveType) item._graduateInactiveType = {code: inputs.graduateInactiveType};
                    if (inputs.graduateFieldsType) item._graduateFieldsType = {code: inputs.graduateFieldsType};
                    // Massiv formatida yuboradi
                    return [item];
                },
                description: `**Yangi bandlik statistikasi yozuvi yaratish (UPSERT)** (POST /)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_REmployment

<b>UPSERT:</b> Agar yozuv mavjud bo'lsa - yangilanadi, yo'q bo'lsa - yaratiladi
<b>Unique Key:</b> department + educationYear + educationType + educationForm + paymentForm + gender + workplaceCompatibility + graduateFieldsType + graduateInactiveType

<b>Body:</b> UNIFIED FORMAT - MASSIV sifatida, underscore bilan yuboriladi
<pre>[
  {
    "uId": "401",
    "qty": 5,
    "_university": {"code": "401"},
    "_department": {"code": "401-102-08"},
    "_educationYear": {"code": "2026"},
    "_educationType": {"code": "11"},
    "_educationForm": {"code": "11"},
    "_paymentForm": {"code": "11"},
    "_gender": {"code": "11"},
    "_workplaceCompatibility": {"code": "11"},
    "_graduateInactiveType": {"code": "13"},
    "_graduateFieldsType": {"code": "31"}
  }
]</pre>

<b>Response:</b> Yaratilgan/Yangilangan bandlik yozuvi`,
                ported: true
            },
    {
                id: 2,
                category: "37.Bandlik statistikasi",
                name: "Bandlik yozuvini ID bo'yicha olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_REmployment/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik to'ldiriladi",
                        defaultNew: "",
                        defaultOld: "",
                        useStoredId: "createdREmploymentId",
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
                description: `**Bandlik yozuvini ID bo'yicha olish** (GET /{entityId})

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_REmployment/{entityId}

<b>Path param:</b> entityId - UUID formatida yozuv identifikatori

<b>Response:</b> Topilgan bandlik yozuvi yoki 404`,
                ported: true
            },
    {
                id: 3,
                category: "37.Bandlik statistikasi",
                name: "Bandlik yozuvini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_REmployment/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik to'ldiriladi",
                        defaultNew: "",
                        defaultOld: "",
                        useStoredId: "createdREmploymentId",
                        required: true
                    },
                    qty: {
                        label: "Miqdor (talabalar soni)",
                        type: "number",
                        defaultNew: "150",
                        defaultOld: "150",
                        required: false
                    }
                },
                hasBody: true,
                bodyGenerator: (inputs) => {
                    let body = {};
                    if (inputs.qty) body.qty = parseInt(inputs.qty);
                    return body;
                },
                description: `**Bandlik yozuvini yangilash** (PUT /{entityId})

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_REmployment/{entityId}

<b>Body:</b> JSON formatida yangilanadigan maydonlar
<pre>{
  "qty": 150
}</pre>

<b>Response:</b> Yangilangan bandlik yozuvi yoki 404`,
                ported: true
            },
    {
                id: 4,
                category: "37.Bandlik statistikasi",
                name: "Barcha bandlik yozuvlarini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_REmployment",
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
                description: `**Bandlik statistikasi hisobotlari** (GET /)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_REmployment

<b>Tavsif:</b> Bo'lim, ta'lim yili, ta'lim turi bo'yicha bandlik statistikasi

<b>Query params:</b>
- limit, offset: Sahifalash
- returnCount: X-Total-Count headerda umumiy sonni qaytarish
- returnNulls: Null qiymatlarni ham qaytarish

<b>Response:</b> Sahifalangan bandlik yozuvlari ro'yxati`,
                ported: true
            },
    {
                id: 5,
                category: "37.Bandlik statistikasi",
                name: "Bandlik yozuvini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_REmployment/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik to'ldiriladi",
                        defaultNew: "",
                        defaultOld: "",
                        useStoredId: "createdREmploymentId",
                        required: true
                    }
                },
                description: `**Bandlik yozuvini o'chirish** (DELETE /{entityId})

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_REmployment/{entityId}

<b>Path param:</b> entityId - UUID formatida yozuv identifikatori

<b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
                ported: true
            },
    {
                id: 6,
                category: "37.Bandlik statistikasi",
                name: "Bandlik yozuvlarini qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_REmployment/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON yoki text)",
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
                description: `**Bandlik yozuvlarini qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_REmployment/search?filter=...&limit=2

<b>Query params:</b>
- filter: CUBA JSON format (URL encoded)
- limit, offset: Sahifalash

<b>CUBA Filter misoli:</b>
<pre>{"conditions":[{"property":"qty","operator":"notEmpty"}]}</pre>

<b>Response:</b> Filter shartiga mos yozuvlar (limit ta)`,
                ported: true
            },
    {
                id: 7,
                category: "37.Bandlik statistikasi",
                name: "Bandlik yozuvlarini qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_REmployment/search",
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
                hasBody: true,
                bodyFields: ["filter", "limit", "offset"],
                queryParamsFromInputs: ["returnNulls"],
                description: `**Bandlik yozuvlarini qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_REmployment/search

<b>Body (OLD-HEMIS compatible):</b>
<pre>{"filter":{"conditions":[{"property":"qty","operator":"notEmpty"}]},"limit":2,"offset":0}</pre>

<b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_37;
}
