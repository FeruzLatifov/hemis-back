// 30.Nashr etish hududlari turlari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_30 = [
    // ============================================
    // 30.Nashr etish hududlari turlari (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "30.Nashr etish hududlari turlari",
                name: "Barcha hududlarni olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality",
                requiresAuth: true,
                inputFields: {
                    offset: {
                        label: "Offset",
                        type: "number",
                        defaultNew: "0",
                        defaultOld: "0",
                        required: false
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        defaultNew: "50",
                        defaultOld: "50",
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
                description: `**Barcha nashr etish hududlarini olish** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HPublicationLocality

<b>Parameters:</b>
- limit: Sahifa hajmi (default: 50)
- offset: Boshlang'ich pozitsiya (default: 0)
- returnNulls: Null qiymatlarni qaytarish (default: false)

<b>Response:</b> Hudud ro'yxati CUBA formatda`,
                ported: true
            },
    {
                id: 2,
                category: "30.Nashr etish hududlari turlari",
                name: "Hudud olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Hudud kodi",
                        type: "text",
                        placeholder: "Hudud kodi (masalan: 11)",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: true,
                        useStoredId: "publicationLocalityCode"
                    }
                },
                description: `**Hudud ID bo'yicha olish** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}

<b>Parameters:</b>
- entityId: Hudud kodi (code field - String)

<b>Response:</b> Hudud CUBA formatda`,
                ported: true
            },
    {
                id: 3,
                category: "30.Nashr etish hududlari turlari",
                name: "Yangi hudud yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["code", "name", "nameRu", "nameEn"],
                inputFields: {
                    code: {
                        label: "Hudud kodi",
                        type: "text",
                        placeholder: "Hudud kodi (masalan: 98 yoki 99)",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        bodyField: "code"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        placeholder: "Hudud nomi",
                        defaultNew: "Test Hudud",
                        defaultOld: "Test Hudud",
                        required: true,
                        bodyField: "name"
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        placeholder: "Hudud nomi (ruscha)",
                        defaultNew: "Тестовая местность",
                        defaultOld: "Тестовая местность",
                        required: false,
                        bodyField: "nameRu"
                    },
                    nameEn: {
                        label: "Nomi (Inglizcha)",
                        type: "text",
                        placeholder: "Hudud nomi (inglizcha)",
                        defaultNew: "Test Locality",
                        defaultOld: "Test Locality",
                        required: false,
                        bodyField: "nameEn"
                    }
                },
                storeResultId: "publicationLocalityCode",
                storeIdField: "code",
                description: `**Yangi nashr etish hududi yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HPublicationLocality

<b>Body:</b> JSON formatda hudud ma'lumotlari

<b>Response:</b> Yaratilgan hudud CUBA formatda`,
                ported: true
            },
    {
                id: 4,
                category: "30.Nashr etish hududlari turlari",
                name: "Hududni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["name", "nameRu", "nameEn", "active"],
                inputFields: {
                    entityId: {
                        label: "Hudud kodi",
                        type: "text",
                        placeholder: "Hudud kodi (98 yoki 99)",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        useStoredId: "publicationLocalityCode"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        placeholder: "Yangi nom",
                        defaultNew: "Test Hudud Updated",
                        defaultOld: "Test Hudud Updated",
                        required: false,
                        bodyField: "name"
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        placeholder: "Yangi nom (ruscha)",
                        defaultNew: "Обновленная местность",
                        defaultOld: "Обновленная местность",
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
                description: `**Hududni yangilash** (PUT)

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}

<b>Body:</b> O'zgartiriladigan maydonlar JSON formatda

<b>Response:</b> Yangilangan hudud CUBA formatda`,
                ported: true
            },
    {
                id: 5,
                category: "30.Nashr etish hududlari turlari",
                name: "Hududni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Hudud kodi",
                        type: "text",
                        placeholder: "Hudud kodi (98 yoki 99)",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        useStoredId: "publicationLocalityCode"
                    }
                },
                description: `**Hududni o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            },
    {
                id: 6,
                category: "30.Nashr etish hududlari turlari",
                name: "Hududlarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON)",
                        type: "text",
                        placeholder: '{"conditions":[{"property":"name","operator":"contains","value":"a"}]}',
                        defaultNew: '{"conditions":[{"property":"name","operator":"contains","value":"a"}]}',
                        defaultOld: '{"conditions":[{"property":"name","operator":"contains","value":"a"}]}',
                        required: true
                    }
                },
                description: `**Hududlarni qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HPublicationLocality/search

<b>Parameters:</b>
- filter: CUBA JSON format (ikkala tizim uchun bir xil)

<b>Response:</b> Filter shartiga mos hududlar`,
                ported: true
            },
    {
                id: 7,
                category: "30.Nashr etish hududlari turlari",
                name: "Hududlarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON)",
                        type: "textarea",
                        placeholder: '{"conditions":[{"property":"code","operator":"notEmpty"}]}',
                        defaultNew: '{"conditions":[{"property":"code","operator":"notEmpty"}]}',
                        defaultOld: '{"conditions":[{"property":"code","operator":"notEmpty"}]}',
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
                description: `**Hududlarni qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HPublicationLocality/search

<b>Body:</b> CUBA format filter JSON
<pre>{"filter":{"conditions":[{"property":"code","operator":"notEmpty"}]}}</pre>

<b>Response:</b> Filter shartiga mos hududlar`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_30;
}
