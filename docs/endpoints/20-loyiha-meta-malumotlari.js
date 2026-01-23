// 20.Loyiha meta ma'lumotlari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_20 = [
    // ============================================
    // 20.Loyiha meta ma'lumotlari (5 endpoint)
    // ============================================
    {
                id: 1,
                category: "20.Loyiha meta ma'lumotlari",
                name: "Loyiha meta yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EProjectMeta",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_fiscalYear", "body_budget", "body_quantityMembers", "body_active"],
                inputFields: {
                    body_fiscalYear: {
                        label: "fiscalYear (Moliyaviy yil)",
                        type: "text",
                        defaultNew: "2025",
                        defaultOld: "2024",
                        required: true,
                        bodyField: "fiscalYear",
                        placeholder: "2024"
                    },
                    body_budget: {
                        label: "budget (Byudjet)",
                        type: "text",
                        defaultNew: "50000000.0",
                        defaultOld: "30000000.0",
                        required: true,
                        bodyField: "budget",
                        placeholder: "50000000.0"
                    },
                    body_quantityMembers: {
                        label: "quantityMembers (A'zolar soni)",
                        type: "text",
                        defaultNew: "5",
                        defaultOld: "3",
                        required: true,
                        bodyField: "quantityMembers"
                    },
                    body_active: {
                        label: "active (Faol)",
                        type: "select",
                        options: [{value: "true", label: "true (Ha)"}, {value: "false", label: "false (Yo'q)"}],
                        default: "true",
                        required: false,
                        bodyField: "active"
                    }
                },
                bodyGenerator: (inputs) => ({
                    fiscalYear: parseInt(inputs.body_fiscalYear) || 2025,
                    budget: parseFloat(inputs.body_budget) || 50000000.0,
                    quantityMembers: parseInt(inputs.body_quantityMembers) || 5,
                    active: inputs.body_active === 'true'
                }),
                description: `**Loyiha meta yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EProjectMeta

<b>Request body:</b>
<pre>{
  "fiscalYear": 2025,
  "budget": 50000000.0,
  "quantityMembers": 5,
  "active": true
}</pre>

<b>Response:</b>
<pre>{
  "_entityName": "hemishe_EProjectMeta",
  "_instanceName": "com.company.hemishe.entity.EProjectMeta-{id} [detached]",
  "id": "yangi_uuid"
}</pre>`,
                ported: true,
                storeResultId: "projectMetaId"
            },
    {
                id: 2,
                category: "20.Loyiha meta ma'lumotlari",
                name: "Loyiha meta olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EProjectMeta/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Loyiha meta ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "projectMetaId"
                    }
                },
                description: `**Loyiha meta olish** (GET by ID)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProjectMeta/{entityId}

<b>Response:</b> Bitta loyiha meta ma'lumotlari`,
                ported: true
            },
    {
                id: 3,
                category: "20.Loyiha meta ma'lumotlari",
                name: "Loyiha meta ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EProjectMeta",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "text",
                        default: "10",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "text",
                        default: "0",
                        required: false
                    }
                },
                description: `**Loyiha meta ma'lumotlari ro'yxati** (GET all)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProjectMeta

<b>Response format:</b>
<pre>[
  {
    "_entityName": "hemishe_EProjectMeta",
    "_instanceName": "com.company.hemishe.entity.EProjectMeta-{id} [detached]",
    "id": "uuid",
    "quantityMembers": 4,
    "active": true,
    "version": 1,
    "fiscalYear": 2018,
    "budget": 10000000.0
  }
]</pre>`,
                ported: true
            },
    {
                id: 4,
                category: "20.Loyiha meta ma'lumotlari",
                name: "Loyiha meta yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EProjectMeta/{entityId}",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["budget", "quantityMembers"],
                inputFields: {
                    entityId: {
                        label: "Loyiha meta ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "projectMetaId"
                    },
                    body_budget: {
                        label: "budget (yangi qiymat)",
                        type: "text",
                        defaultNew: "75000000.0",
                        defaultOld: "60000000.0",
                        required: true,
                        bodyField: "budget"
                    },
                    body_quantityMembers: {
                        label: "quantityMembers (yangi qiymat)",
                        type: "text",
                        defaultNew: "10",
                        defaultOld: "8",
                        required: true,
                        bodyField: "quantityMembers"
                    }
                },
                description: `**Loyiha meta yangilash** (PUT)

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EProjectMeta/{entityId}

<b>Response:</b> Minimal response - _entityName, _instanceName, id`,
                ported: true
            },
    {
                id: 5,
                category: "20.Loyiha meta ma'lumotlari",
                name: "Loyiha meta o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EProjectMeta/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Loyiha meta ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "projectMetaId"
                    }
                },
                description: `**Loyiha meta o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EProjectMeta/{entityId}

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_20;
}
