// 23.Ilmiy ishlanmalar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_23 = [
    // ============================================
    // 23.Ilmiy ishlanmalar (5 endpoint)
    // ============================================
    {
                id: 1,
                category: "23.Ilmiy ishlanmalar",
                name: "Ilmiy ishlanma yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EPublicationProperty",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                    name: {
                        label: "Nomi",
                        type: "text",
                        placeholder: "Ilmiy ishlanma nomi",
                        defaultNew: "Test ilmiy ishlanma (Yangi)",
                        defaultOld: "Test ilmiy ishlanma (Eski)",
                        required: true
                    },
                    numbers: {
                        label: "Raqami",
                        type: "text",
                        placeholder: "FAP 00123",
                        defaultNew: "FAP 00123",
                        defaultOld: "FAP 00123",
                        required: false
                    },
                    authors: {
                        label: "Mualliflar",
                        type: "text",
                        placeholder: "Aliyev A., Karimov B.",
                        defaultNew: "Test Author",
                        defaultOld: "Test Author",
                        required: false
                    },
                    authorCounts: {
                        label: "Mualliflar soni",
                        type: "number",
                        placeholder: "2",
                        defaultNew: "1",
                        defaultOld: "1",
                        required: false
                    },
                    active: {
                        label: "Faol",
                        type: "select",
                        options: [
                            { value: "true", label: "Ha" },
                            { value: "false", label: "Yo'q" }
                        ],
                        defaultNew: "true",
                        defaultOld: "true",
                        required: false
                    }
                },
                body: {
                    name: "{name}",
                    numbers: "{numbers}",
                    authors: "{authors}",
                    authorCounts: "{authorCounts}",
                    active: "{active}"
                },
                storeResultId: "publicationPropertyId",
                description: `**Ilmiy ishlanma yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EPublicationProperty

<b>Request Body:</b>
\`\`\`json
{
    "name": "Yangi ixtiro nomi",
    "numbers": "FAP 00123",
    "authors": "Aliyev A., Karimov B.",
    "authorCounts": 2,
    "active": true
}
\`\`\`

<b>Response:</b> Minimal response - _entityName, _instanceName, id`,
                ported: true
            },
    {
                id: 2,
                category: "23.Ilmiy ishlanmalar",
                name: "Barcha ilmiy ishlanmalar",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationProperty",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        placeholder: "10",
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
                params: {
                    limit: "{limit}",
                    offset: "{offset}"
                },
                storeFirstId: "publicationPropertyId",
                description: `**Barcha ilmiy ishlanmalar ro'yxati** (GET list)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationProperty?offset=0&limit=10

<b>Response:</b> Ro'yxat (array of entities)`,
                ported: true
            },
    {
                id: 3,
                category: "23.Ilmiy ishlanmalar",
                name: "Ilmiy ishlanmani olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Ilmiy ishlanma ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationPropertyId"
                    }
                },
                description: `**Ilmiy ishlanmani olish** (GET by ID)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}

<b>Response:</b> To'liq entity ma'lumotlari`,
                ported: true
            },
    {
                id: 4,
                category: "23.Ilmiy ishlanmalar",
                name: "Ilmiy ishlanmani yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                    entityId: {
                        label: "Ilmiy ishlanma ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationPropertyId"
                    },
                    name: {
                        label: "Yangi nom",
                        type: "text",
                        placeholder: "Yangilangan nom",
                        defaultNew: "Yangilangan test ishlanma",
                        defaultOld: "Yangilangan test ishlanma",
                        required: false
                    },
                    active: {
                        label: "Faol",
                        type: "select",
                        options: [
                            { value: "true", label: "Ha" },
                            { value: "false", label: "Yo'q" }
                        ],
                        defaultNew: "true",
                        defaultOld: "true",
                        required: false
                    }
                },
                body: {
                    name: "{name}",
                    active: "{active}"
                },
                description: `**Ilmiy ishlanmani yangilash** (PUT)

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}

<b>Request Body:</b>
\`\`\`json
{
    "name": "Yangilangan nom",
    "active": true
}
\`\`\`

<b>Response:</b> Minimal response - _entityName, _instanceName, id`,
                ported: true
            },
    {
                id: 5,
                category: "23.Ilmiy ishlanmalar",
                name: "Ilmiy ishlanma o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Ilmiy ishlanma ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationPropertyId"
                    }
                },
                description: `**Ilmiy ishlanma o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_23;
}
