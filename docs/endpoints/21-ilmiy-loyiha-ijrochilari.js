// 21.Ilmiy loyiha ijrochilari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_21 = [
    // ============================================
    // 21.Ilmiy loyiha ijrochilari (3 endpoint)
    // ============================================
    {
                id: 1,
                category: "21.Ilmiy loyiha ijrochilari",
                name: "Loyiha ijrochisi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EProjectExecutor",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                    body_outsider: {
                        label: "outsider (Tashqi ijrochi nomi)",
                        type: "text",
                        defaultNew: "Test Ijrochi - Yangi HEMIS",
                        defaultOld: "Test Ijrochi - Eski HEMIS",
                        required: false,
                        bodyField: "outsider"
                    },
                    body_startDate: {
                        label: "startDate (Boshlanish sanasi)",
                        type: "text",
                        defaultNew: "2024-01-01",
                        defaultOld: "2024-01-01",
                        required: false,
                        bodyField: "startDate",
                        placeholder: "YYYY-MM-DD"
                    },
                    body_endDate: {
                        label: "endDate (Tugash sanasi)",
                        type: "text",
                        defaultNew: "2024-12-31",
                        defaultOld: "2024-12-31",
                        required: false,
                        bodyField: "endDate",
                        placeholder: "YYYY-MM-DD"
                    },
                    body_active: {
                        label: "active (Faol)",
                        type: "select",
                        options: [
                            { value: "true", label: "true (Ha)" },
                            { value: "false", label: "false (Yo'q)" }
                        ],
                        default: "true",
                        required: false,
                        bodyField: "active"
                    }
                },
                hasBody: true,
                bodyFields: ["body_outsider", "body_startDate", "body_endDate", "body_active"],
                bodyGenerator: (inputs) => ({
                    outsider: inputs.body_outsider || undefined,
                    startDate: inputs.body_startDate || undefined,
                    endDate: inputs.body_endDate || undefined,
                    active: inputs.body_active === 'true'
                }),
                description: `**Yangi loyiha ijrochisi yaratish** (POST create)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EProjectExecutor

<b>Request Body:</b>
- outsider - Tashqi ijrochi nomi (String)
- project - Loyiha {id: "uuid"} (UUID, optional)
- projectExecutorType - Ijrochi turi {id: "uuid"} (UUID, optional)
- startDate - Boshlanish sanasi (YYYY-MM-DD)
- endDate - Tugash sanasi (YYYY-MM-DD)
- active - Faol holati (Boolean)

<b>Response (OLD-HEMIS format):</b>
<pre>{
  "_entityName": "hemishe_EProjectExecutor",
  "_instanceName": "com.company.hemishe.entity.EProjectExecutor-uuid [detached]",
  "id": "uuid-here"
}</pre>`,
                ported: true,
                storeResultId: "createdProjectExecutorId"
            },
    {
                id: 2,
                category: "21.Ilmiy loyiha ijrochilari",
                name: "Loyiha ijrochisini olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EProjectExecutor/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        placeholder: "UUID yoki {createdProjectExecutorId}",
                        useStoredId: "createdProjectExecutorId"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "text",
                        defaultNew: "true",
                        defaultOld: "true",
                        required: false
                    }
                },
                queryParamsFromInputs: ["returnNulls"],
                description: `**Loyiha ijrochisini ID bo'yicha olish** (GET by ID)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProjectExecutor/{entityId}?returnNulls=true

<b>Path Parameters:</b>
- entityId - Loyiha ijrochisi UUID (required)

<b>Query Parameters:</b>
- returnNulls - null qiymatlarni qaytarish (default: true)

<b>Response:</b> Loyiha ijrochisi to'liq ma'lumotlari`,
                ported: true
            },
    {
                id: 4,
                category: "21.Ilmiy loyiha ijrochilari",
                name: "Loyiha ijrochisini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EProjectExecutor/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        placeholder: "UUID yoki {createdProjectExecutorId}",
                        useStoredId: "createdProjectExecutorId"
                    }
                },
                description: `**Loyiha ijrochisini o'chirish** (DELETE - Soft delete)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EProjectExecutor/{entityId}

<b>Path Parameters:</b>
- entityId - Loyiha ijrochisi UUID (required)

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_21;
}
