// 22.Ilmiy nashrlar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_22 = [
    // ============================================
    // 22.Ilmiy nashrlar (5 endpoint)
    // ============================================
    {
                id: 1,
                category: "22.Ilmiy nashrlar",
                name: "Ilmiy nashr yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EPublicationScientific",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_name", "body_authors", "body_authorCounts", "body_issueYear", "body_doi", "body_active"],
                inputFields: {
                    body_name: {
                        label: "name (Nashr nomi)",
                        type: "text",
                        defaultNew: "Quantum Computing in Medicine - NEW",
                        defaultOld: "Quantum Computing in Medicine - OLD",
                        required: true,
                        bodyField: "name",
                        placeholder: "Ilmiy nashr nomi"
                    },
                    body_authors: {
                        label: "authors (Mualliflar)",
                        type: "text",
                        defaultNew: "Aliyev A., Karimov B.",
                        defaultOld: "Aliyev A., Karimov B.",
                        required: true,
                        bodyField: "authors",
                        placeholder: "Aliyev A., Karimov B."
                    },
                    body_authorCounts: {
                        label: "authorCounts (Mualliflar soni)",
                        type: "text",
                        defaultNew: "2",
                        defaultOld: "2",
                        required: true,
                        bodyField: "authorCounts"
                    },
                    body_issueYear: {
                        label: "issueYear (Nashr yili)",
                        type: "text",
                        defaultNew: "2025",
                        defaultOld: "2024",
                        required: true,
                        bodyField: "issueYear"
                    },
                    body_doi: {
                        label: "doi (DOI)",
                        type: "text",
                        defaultNew: "10.1234/example.2025",
                        defaultOld: "10.1234/example.2024",
                        required: false,
                        bodyField: "doi",
                        placeholder: "10.1234/example.2024"
                    },
                    body_active: {
                        label: "active (Faol)",
                        type: "select",
                        options: [{value: "true", label: "true"}, {value: "false", label: "false"}],
                        defaultNew: "true",
                        defaultOld: "true",
                        required: true,
                        bodyField: "active"
                    }
                },
                description: `**Ilmiy nashr yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EPublicationScientific

<b>Request body:</b>
<pre>{
  "name": "Quantum Computing in Medicine",
  "authors": "Aliyev A., Karimov B.",
  "authorCounts": 2,
  "issueYear": 2024,
  "doi": "10.1234/example.2024",
  "active": true
}</pre>

<b>Response:</b>
<pre>{
  "_entityName": "hemishe_EPublicationScientific",
  "_instanceName": "com.company.hemishe.entity.EPublicationScientific-{id} [detached]",
  "id": "yangi_uuid"
}</pre>`,
                ported: true,
                storeResultId: "publicationScientificId"
            },
    {
                id: 2,
                category: "22.Ilmiy nashrlar",
                name: "Ilmiy nashr olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Ilmiy nashr ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationScientificId"
                    }
                },
                description: `**Ilmiy nashr olish** (GET by ID)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}

<b>Response:</b> Bitta ilmiy nashr ma'lumotlari`,
                ported: true
            },
    {
                id: 3,
                category: "22.Ilmiy nashrlar",
                name: "Ilmiy nashrlar ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationScientific",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "text",
                        defaultNew: "10",
                        defaultOld: "10",
                        default: "10",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "text",
                        defaultNew: "0",
                        defaultOld: "0",
                        required: false
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "text",
                        defaultNew: "true",
                        defaultOld: "true",
                        required: false
                    }
                },
                queryParamsFromInputs: ["limit", "offset", "returnNulls"],
                description: `**Loyiha ijrochilari ro'yxati** (GET list)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProjectExecutor?limit=10&offset=0

<b>Query Parameters:</b>
- limit - Qaytariladigan yozuvlar soni (default: 10)
- offset - O'tkazib yuboriladigan yozuvlar (default: 0)
- returnNulls - null qiymatlarni qaytarish (default: true)

<b>Response:</b> Massiv formatida loyiha ijrochilari ro'yxati`,
                ported: true
            },
    {
                id: 4,
                category: "22.Ilmiy nashrlar",
                name: "Ilmiy nashr yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_name", "body_authorCounts"],
                inputFields: {
                    entityId: {
                        label: "Ilmiy nashr ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationScientificId"
                    },
                    body_name: {
                        label: "name (yangi qiymat)",
                        type: "text",
                        defaultNew: "Updated Scientific Publication - NEW",
                        defaultOld: "Updated Scientific Publication - OLD",
                        required: true,
                        bodyField: "name"
                    },
                    body_authorCounts: {
                        label: "authorCounts (yangi qiymat)",
                        type: "text",
                        defaultNew: "5",
                        defaultOld: "4",
                        required: true,
                        bodyField: "authorCounts"
                    }
                },
                description: `**Ilmiy nashr yangilash** (PUT)

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}

<b>Response:</b> Minimal response - _entityName, _instanceName, id`,
                ported: true
            },
    {
                id: 5,
                category: "22.Ilmiy nashrlar",
                name: "Ilmiy nashr o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Ilmiy nashr ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationScientificId"
                    }
                },
                description: `**Ilmiy nashr o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_22;
}
