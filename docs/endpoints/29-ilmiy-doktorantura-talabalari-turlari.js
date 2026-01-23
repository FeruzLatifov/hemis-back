// 29.Ilmiy doktorantura talabalari turlari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_29 = [
    // ============================================
    // 29.Ilmiy doktorantura talabalari turlari (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Barcha turlarni olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType",
                requiresAuth: true,
                inputFields: {
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
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [
                            { value: "false", label: "false (default)" },
                            { value: "true", label: "true" }
                        ],
                        defaultNew: "false",
                        defaultOld: "false",
                        required: false
                    }
                },
                queryParamsFromInputs: ["limit", "offset", "returnNulls"],
                description: `**Barcha doktorantura talabasi turlarini olish** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentType

<b>Parameters:</b>
- limit: Sahifa hajmi (default: 50)
- offset: Boshlang'ich pozitsiya (default: 0)
- returnNulls: Null qiymatlarni qaytarish (default: false)

<b>Response:</b> Tur ro'yxati CUBA formatda`,
                ported: true
            },
    {
                id: 2,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Tur olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Tur kodi",
                        type: "text",
                        placeholder: "Tur kodi (masalan: 11)",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: true,
                        useStoredId: "doctoralStudentTypeCode"
                    }
                },
                description: `**Tur ID bo'yicha olish** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}

<b>Parameters:</b>
- entityId: Tur kodi (code field - String)

<b>Response:</b> Tur CUBA formatda`,
                ported: true
            },
    {
                id: 3,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Yangi tur yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["code", "name", "nameRu", "nameEn"],
                inputFields: {
                    code: {
                        label: "Tur kodi",
                        type: "text",
                        placeholder: "Tur kodi (masalan: 98 yoki 99)",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        bodyField: "code"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        placeholder: "Tur nomi",
                        defaultNew: "Test Tur",
                        defaultOld: "Test Tur",
                        required: true,
                        bodyField: "name"
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        placeholder: "Tur nomi (ruscha)",
                        defaultNew: "Тестовый тип",
                        defaultOld: "Тестовый тип",
                        required: false,
                        bodyField: "nameRu"
                    },
                    nameEn: {
                        label: "Nomi (Inglizcha)",
                        type: "text",
                        placeholder: "Tur nomi (inglizcha)",
                        defaultNew: "Test Type",
                        defaultOld: "Test Type",
                        required: false,
                        bodyField: "nameEn"
                    }
                },
                storeResultId: "doctoralStudentTypeCode",
                storeIdField: "code",
                description: `**Yangi doktorantura talabasi turi yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HDoctoralStudentType

<b>Body:</b> JSON formatda tur ma'lumotlari

<b>Response:</b> Yaratilgan tur CUBA formatda`,
                ported: true
            },
    {
                id: 4,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Turni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["name", "nameRu", "nameEn", "active"],
                inputFields: {
                    entityId: {
                        label: "Tur kodi",
                        type: "text",
                        placeholder: "Tur kodi (98 yoki 99)",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        useStoredId: "doctoralStudentTypeCode"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        placeholder: "Yangi nom",
                        defaultNew: "Test Tur Updated",
                        defaultOld: "Test Tur Updated",
                        required: false,
                        bodyField: "name"
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        placeholder: "Yangi nom (ruscha)",
                        defaultNew: "Обновленный тип",
                        defaultOld: "Обновленный тип",
                        required: false,
                        bodyField: "nameRu"
                    },
                    nameEn: {
                        label: "Nomi (Inglizcha)",
                        type: "text",
                        placeholder: "Yangi nom (inglizcha)",
                        defaultNew: "",
                        defaultOld: "",
                        required: false,
                        bodyField: "nameEn"
                    },
                    active: {
                        label: "Faolmi?",
                        type: "select",
                        options: [
                            { value: "true", label: "Ha" },
                            { value: "false", label: "Yo'q" }
                        ],
                        defaultNew: "true",
                        defaultOld: "true",
                        required: false,
                        bodyField: "active"
                    }
                },
                description: `**Turni yangilash** (PUT)

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}

<b>Body:</b> O'zgartiriladigan maydonlar JSON formatda

<b>Response:</b> Yangilangan tur CUBA formatda`,
                ported: true
            },
    {
                id: 5,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Turni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Tur kodi",
                        type: "text",
                        placeholder: "Tur kodi (98 yoki 99)",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        useStoredId: "doctoralStudentTypeCode"
                    }
                },
                description: `**Turni o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            },
    {
                id: 6,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Turlarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter JSON (majburiy)",
                        type: "text",
                        placeholder: '{"conditions":[]}',
                        defaultNew: '{"conditions":[]}',
                        defaultOld: '{"conditions":[]}',
                        required: true
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
                    }
                },
                queryParamsFromInputs: ["filter", "limit", "offset"],
                description: `**Turlarni qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentType/search

<b>Parametrlar:</b>
- filter: JSON filter (majburiy) - {"conditions":[]}
- limit: Natijalar soni (default: 50)
- offset: Boshlang'ich pozitsiya

<b>Response:</b> Filter shartiga mos turlar`,
                ported: true
            },
    {
                id: 7,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Turlarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType/search",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
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
                    }
                },
                hasBody: true,
                bodyTemplate: {
                    "filter": {
                        "conditions": []
                    }
                },
                queryParamsFromInputs: ["limit", "offset"],
                description: `**Turlarni qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HDoctoralStudentType/search

<b>Body:</b> {"filter":{"conditions":[]}} formatida

<b>Parametrlar:</b>
- limit: Natijalar soni (default: 50)
- offset: Boshlang'ich pozitsiya

<b>Response:</b> Filter shartiga mos turlar`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_29;
}
