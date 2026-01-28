// 27.Ilmiy uslubiy nashr turlari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_27 = [
    // ============================================
    // 27.Ilmiy uslubiy nashr turlari (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Barcha turlarni olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType",
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
                description: `**Barcha uslubiy nashr turlarini olish** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HMethodicalPublicationType

<b>Parameters:</b>
- limit: Sahifa hajmi (default: 50)
- offset: Boshlang'ich pozitsiya (default: 0)
- returnNulls: Null qiymatlarni qaytarish (default: false)

<b>Response:</b> Uslubiy nashr turlari ro'yxati CUBA formatda`,
                ported: true
            },
    {
                id: 2,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Tur ma'lumotini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Tur kodi",
                        type: "text",
                        placeholder: "98 yoki 99",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true
                    }
                },
                description: `**Uslubiy nashr turi ma'lumotini olish** (GET by ID)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}

<b>Response:</b> Tur ma'lumotlari CUBA formatda`,
                ported: true
            },
    {
                id: 3,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Yangi tur yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["code", "name", "nameRu", "nameEn"],
                inputFields: {
                    code: {
                        label: "Kod (unique)",
                        type: "text",
                        placeholder: "98 yoki 99",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        bodyField: "code"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        placeholder: "Tur nomi",
                        defaultNew: "Test Turi NEW",
                        defaultOld: "Test Turi OLD",
                        required: true,
                        bodyField: "name"
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        placeholder: "Tur nomi (ruscha)",
                        defaultNew: "Тестовый тип NEW",
                        defaultOld: "Тестовый тип OLD",
                        required: false,
                        bodyField: "nameRu"
                    },
                    nameEn: {
                        label: "Nomi (Inglizcha)",
                        type: "text",
                        placeholder: "Tur nomi (inglizcha)",
                        defaultNew: "Test Type NEW",
                        defaultOld: "Test Type OLD",
                        required: false,
                        bodyField: "nameEn"
                    }
                },
                description: `**Yangi uslubiy nashr turi yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HMethodicalPublicationType

<b>Body:</b> JSON formatda tur ma'lumotlari

<b>Response:</b> Yaratilgan tur CUBA formatda`,
                ported: true
            },
    {
                id: 4,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Turni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["name", "nameRu", "nameEn"],
                inputFields: {
                    entityId: {
                        label: "Tur kodi",
                        type: "text",
                        placeholder: "98 yoki 99",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        placeholder: "Yangi nom",
                        defaultNew: "Test Turi Updated NEW",
                        defaultOld: "Test Turi Updated OLD",
                        required: false,
                        bodyField: "name"
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        placeholder: "Yangi nom (ruscha)",
                        defaultNew: "Обновленный тип NEW",
                        defaultOld: "Обновленный тип OLD",
                        required: false,
                        bodyField: "nameRu"
                    },
                    nameEn: {
                        label: "Nomi (Inglizcha)",
                        type: "text",
                        placeholder: "Yangi nom (inglizcha)",
                        defaultNew: "Updated Type NEW",
                        defaultOld: "Updated Type OLD",
                        required: false,
                        bodyField: "nameEn"
                    }
                },
                description: `**Uslubiy nashr turini yangilash** (PUT)

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}

<b>Body:</b> O'zgartiriladigan maydonlar JSON formatda

<b>Response:</b> Yangilangan tur CUBA formatda`,
                ported: true
            },
    {
                id: 5,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Turni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Tur kodi",
                        type: "text",
                        placeholder: "98 yoki 99",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true
                    }
                },
                description: `**Uslubiy nashr turini o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            },
    {
                id: 6,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Filter bo'yicha qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/search",
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
                description: `**Filter bo'yicha qidirish** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HMethodicalPublicationType/search

<b>Parametrlar:</b>
- filter: JSON filter (majburiy) - {"conditions":[]}
- limit: Natijalar soni (default: 50)
- offset: Boshlang'ich pozitsiya

<b>Response:</b> Filter shartlariga mos entitylar massivi`,
                ported: true
            },
    {
                id: 7,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Filter bo'yicha qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/search",
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
                description: `**Filter bo'yicha qidirish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HMethodicalPublicationType/search

<b>Body:</b> {"filter":{"conditions":[]}} formatida

<b>Parametrlar:</b>
- limit: Natijalar soni (default: 50)
- offset: Boshlang'ich pozitsiya

<b>Response:</b> Filter shartlariga mos entitylar massivi`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_27;
}
