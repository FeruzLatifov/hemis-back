// 25.Ilmiy nashr mualliflari meta ma'lumotlari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_25 = [
    // ============================================
    // 25.Ilmiy nashr mualliflari meta ma'lumotlari (5 endpoint)
    // ============================================
    {
                id: 1,
                category: "25.Ilmiy nashr mualliflari meta ma'lumotlari",
                name: "Nashr muallifi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EPublicationAuthorMeta",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_employee", "body_university", "body_publicationTypeTable", "body_isMainAuthor", "body_isCheckedByAuthor", "body_active"],
                inputFields: {
                    body_employee: {
                        label: "Xodim ID (UUID)",
                        type: "text",
                        placeholder: "Teacher UUID",
                        defaultNew: "6b3c0dfc-e269-3df5-894e-85b8c2386e9d",
                        defaultOld: "1d2f4cda-79df-3de6-e15f-434a3f044b5f",
                        required: true,
                        bodyField: "employee",
                        cubaForeignKey: true
                    },
                    body_university: {
                        label: "Universitet kodi",
                        type: "text",
                        placeholder: "401",
                        defaultNew: "401",
                        defaultOld: "351",
                        required: true,
                        bodyField: "university",
                        cubaForeignKey: true,
                        cubaForeignKeyField: "code"
                    },
                    body_publicationTypeTable: {
                        label: "Nashr turi",
                        type: "select",
                        options: [
                            { value: "scientific", label: "Ilmiy (scientific)" },
                            { value: "property", label: "Ishlanma (property)" },
                            { value: "methodic", label: "Metodik (methodic)" }
                        ],
                        defaultNew: "scientific",
                        defaultOld: "scientific",
                        required: true,
                        bodyField: "publicationTypeTable"
                    },
                    body_isMainAuthor: {
                        label: "Asosiy muallif",
                        type: "select",
                        options: [
                            { value: "1", label: "Ha (1)" },
                            { value: "0", label: "Yo'q (0)" }
                        ],
                        defaultNew: "1",
                        defaultOld: "1",
                        required: false,
                        bodyField: "isMainAuthor"
                    },
                    body_isCheckedByAuthor: {
                        label: "Muallif tomonidan tekshirilgan",
                        type: "select",
                        options: [
                            { value: "true", label: "Ha" },
                            { value: "false", label: "Yo'q" }
                        ],
                        defaultNew: "true",
                        defaultOld: "true",
                        required: false,
                        bodyField: "isCheckedByAuthor"
                    },
                    body_active: {
                        label: "Faol",
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
                storeResultId: "publicationAuthorMetaId",
                description: `**Nashr muallifi yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EPublicationAuthorMeta

<b>Request Body:</b>
\`\`\`json
{
    "employee": "teacher-uuid",
    "university": "401",
    "publicationTypeTable": "scientific",
    "isMainAuthor": "1",
    "isCheckedByAuthor": "true",
    "active": "true"
}
\`\`\`

<b>Response:</b> Yaratilgan entity CUBA formatda - nested objectlar bilan`,
                ported: true
            },
    {
                id: 2,
                category: "25.Ilmiy nashr mualliflari meta ma'lumotlari",
                name: "Nashr mualliflari ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationAuthorMeta",
                requiresAuth: true,
                inputFields: {
                    offset: {
                        label: "Offset",
                        type: "text",
                        defaultNew: "0",
                        defaultOld: "0",
                        required: false
                    },
                    limit: {
                        label: "Limit",
                        type: "text",
                        defaultNew: "10",
                        defaultOld: "10",
                        required: false
                    }
                },
                queryParamsFromInputs: ["offset", "limit"],
                description: "Barcha nashr mualliflari meta ma'lumotlari ro'yxatini olish",
                ported: true
            },
    {
                id: 3,
                category: "25.Ilmiy nashr mualliflari meta ma'lumotlari",
                name: "Nashr muallifi olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Nashr muallifi ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationAuthorMetaId"
                    }
                },
                description: `**Nashr muallifi olish** (GET by ID)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}

<b>Response:</b> Bitta entity CUBA formatda`,
                ported: true
            },
    {
                id: 4,
                category: "25.Ilmiy nashr mualliflari meta ma'lumotlari",
                name: "Nashr muallifini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_isMainAuthor", "body_isCheckedByAuthor"],
                inputFields: {
                    entityId: {
                        label: "Nashr muallifi ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationAuthorMetaId"
                    },
                    body_isMainAuthor: {
                        label: "isMainAuthor (yangi qiymat)",
                        type: "select",
                        options: [
                            { value: "1", label: "Ha (1)" },
                            { value: "0", label: "Yo'q (0)" }
                        ],
                        defaultNew: "0",
                        defaultOld: "0",
                        required: true,
                        bodyField: "isMainAuthor"
                    },
                    body_isCheckedByAuthor: {
                        label: "isCheckedByAuthor (yangi qiymat)",
                        type: "select",
                        options: [
                            { value: "true", label: "Ha" },
                            { value: "false", label: "Yo'q" }
                        ],
                        defaultNew: "false",
                        defaultOld: "false",
                        required: true,
                        bodyField: "isCheckedByAuthor"
                    }
                },
                description: `**Nashr muallifini yangilash** (PUT)

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}

<b>Body:</b> O'zgartiriladigan maydonlar JSON formatda

<b>Response:</b> Yangilangan entity CUBA formatda`,
                ported: true
            },
    {
                id: 5,
                category: "25.Ilmiy nashr mualliflari meta ma'lumotlari",
                name: "Nashr muallifini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Nashr muallifi ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationAuthorMetaId"
                    }
                },
                description: `**Nashr muallifini o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_25;
}
