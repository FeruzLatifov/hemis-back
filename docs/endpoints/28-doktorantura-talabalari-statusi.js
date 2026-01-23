// 28.Doktorantura talabalari statusi endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_28 = [
    // ============================================
    // 28.Doktorantura talabalari statusi (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "28.Doktorantura talabalari statusi",
                name: "Barcha statuslarni olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus",
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
                description: `**Barcha doktorantura talabasi statuslarini olish** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentStatus

<b>Parameters:</b>
- limit: Sahifa hajmi (default: 50)
- offset: Boshlang'ich pozitsiya (default: 0)
- returnNulls: Null qiymatlarni qaytarish (default: false)

<b>Response:</b> Status ro'yxati CUBA formatda`,
                ported: true
            },
    {
                id: 2,
                category: "28.Doktorantura talabalari statusi",
                name: "Status olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Status kodi",
                        type: "text",
                        placeholder: "Status kodi (masalan: 11)",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: true,
                        useStoredId: "doctoralStudentStatusCode"
                    }
                },
                description: `**Status ID bo'yicha olish** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}

<b>Parameters:</b>
- entityId: Status kodi (code field - String)

<b>Response:</b> Status CUBA formatda`,
                ported: true
            },
    {
                id: 3,
                category: "28.Doktorantura talabalari statusi",
                name: "Yangi status yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["code", "name", "nameRu", "nameEn"],
                inputFields: {
                    code: {
                        label: "Status kodi",
                        type: "text",
                        placeholder: "Status kodi (masalan: 98 yoki 99)",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        bodyField: "code"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        placeholder: "Status nomi",
                        defaultNew: "Test Status",
                        defaultOld: "Test Status",
                        required: true,
                        bodyField: "name"
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        placeholder: "Status nomi (ruscha)",
                        defaultNew: "Тестовый статус",
                        defaultOld: "Тестовый статус",
                        required: false,
                        bodyField: "nameRu"
                    },
                    nameEn: {
                        label: "Nomi (Inglizcha)",
                        type: "text",
                        placeholder: "Status nomi (inglizcha)",
                        defaultNew: "Test Status",
                        defaultOld: "Test Status",
                        required: false,
                        bodyField: "nameEn"
                    }
                },
                storeResultId: "doctoralStudentStatusCode",
                storeIdField: "code",
                description: `**Yangi doktorantura talabasi statusi yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HDoctoralStudentStatus

<b>Body:</b> JSON formatda status ma'lumotlari

<b>Response:</b> Yaratilgan status CUBA formatda`,
                ported: true
            },
    {
                id: 4,
                category: "28.Doktorantura talabalari statusi",
                name: "Statusni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["name", "nameRu", "nameEn", "active"],
                inputFields: {
                    entityId: {
                        label: "Status kodi",
                        type: "text",
                        placeholder: "Status kodi (98 yoki 99)",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        useStoredId: "doctoralStudentStatusCode"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        placeholder: "Yangi nom",
                        defaultNew: "Test Status Updated",
                        defaultOld: "Test Status Updated",
                        required: false,
                        bodyField: "name"
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        placeholder: "Yangi nom (ruscha)",
                        defaultNew: "Обновленный статус",
                        defaultOld: "Обновленный статус",
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
                description: `**Statusni yangilash** (PUT)

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}

<b>Body:</b> O'zgartiriladigan maydonlar JSON formatda

<b>Response:</b> Yangilangan status CUBA formatda`,
                ported: true
            },
    {
                id: 5,
                category: "28.Doktorantura talabalari statusi",
                name: "Statusni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Status kodi",
                        type: "text",
                        placeholder: "Status kodi (98 yoki 99)",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        useStoredId: "doctoralStudentStatusCode"
                    }
                },
                description: `**Statusni o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            },
    {
                id: 6,
                category: "28.Doktorantura talabalari statusi",
                name: "Statuslarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/search",
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
                description: `**Statuslarni qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentStatus/search

<b>Parametrlar:</b>
- filter: JSON filter (majburiy) - {"conditions":[]}
- limit: Natijalar soni (default: 50)
- offset: Boshlang'ich pozitsiya

<b>Response:</b> Filter shartiga mos statuslar`,
                ported: true
            },
    {
                id: 7,
                category: "28.Doktorantura talabalari statusi",
                name: "Statuslarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/search",
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
                description: `**Statuslarni qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HDoctoralStudentStatus/search

<b>Body:</b> {"filter":{"conditions":[]}} formatida

<b>Parametrlar:</b>
- limit: Natijalar soni (default: 50)
- offset: Boshlang'ich pozitsiya

<b>Response:</b> Filter shartiga mos statuslar`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_28;
}
