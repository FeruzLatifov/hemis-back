// 19.Ilmiy loyihalar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_19 = [
    // ============================================
    // 19.Ilmiy loyihalar (4 endpoint)
    // ============================================
    {
                id: 1,
                category: "19.Ilmiy loyihalar",
                name: "Loyiha yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EProject",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                    body_name: {
                        label: "name (Loyiha nomi)",
                        type: "text",
                        defaultNew: "Test loyiha - Yangi HEMIS",
                        defaultOld: "Test loyiha - Eski HEMIS",
                        required: true,
                        bodyField: "name"
                    },
                    body_projectNumber: {
                        label: "projectNumber (Loyiha raqami)",
                        type: "text",
                        defaultNew: "PRJ-NEW-001",
                        defaultOld: "PRJ-OLD-001",
                        required: false,
                        bodyField: "projectNumber"
                    },
                    body_contractNumber: {
                        label: "contractNumber (Shartnoma raqami)",
                        type: "text",
                        defaultNew: "CNT-2024-NEW-001",
                        defaultOld: "CNT-2024-OLD-001",
                        required: false,
                        bodyField: "contractNumber"
                    },
                    body_contractDate: {
                        label: "contractDate (Shartnoma sanasi)",
                        type: "text",
                        defaultNew: "2024-01-15",
                        defaultOld: "2023-06-01",
                        required: false,
                        bodyField: "contractDate",
                        placeholder: "YYYY-MM-DD"
                    },
                    body_startDate: {
                        label: "startDate (Boshlanish sanasi)",
                        type: "text",
                        defaultNew: "2024-02-01",
                        defaultOld: "2023-07-01",
                        required: false,
                        bodyField: "startDate",
                        placeholder: "YYYY-MM-DD"
                    },
                    body_endDate: {
                        label: "endDate (Tugash sanasi)",
                        type: "text",
                        defaultNew: "2024-12-31",
                        defaultOld: "2024-06-30",
                        required: false,
                        bodyField: "endDate",
                        placeholder: "YYYY-MM-DD"
                    },
                    body_position: {
                        label: "position (Pozitsiya)",
                        type: "text",
                        defaultNew: "1",
                        defaultOld: "1",
                        required: false,
                        bodyField: "position"
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
                bodyGenerator: (inputs) => ({
                    name: inputs.body_name,
                    projectNumber: inputs.body_projectNumber || undefined,
                    contractNumber: inputs.body_contractNumber || undefined,
                    contractDate: inputs.body_contractDate || undefined,
                    startDate: inputs.body_startDate || undefined,
                    endDate: inputs.body_endDate || undefined,
                    position: inputs.body_position ? parseInt(inputs.body_position) : undefined,
                    active: inputs.body_active === 'true'
                }),
                description: `**Yangi loyiha yaratish** (POST create)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EProject

<b>Request Body:</b>
- name - Loyiha nomi (String, required)
- projectNumber - Loyiha raqami (String)
- contractNumber - Shartnoma raqami (String)
- contractDate - Shartnoma sanasi (YYYY-MM-DD)
- startDate - Boshlanish sanasi (YYYY-MM-DD)
- endDate - Tugash sanasi (YYYY-MM-DD)
- position - Pozitsiya (Integer)
- active - Faol holati (Boolean)

<b>Response (OLD-HEMIS format):</b>
<pre>{
  "_entityName": "hemishe_EProject",
  "_instanceName": "...",
  "id": "uuid-here"
}</pre>`,
                ported: true,
                storeResultId: "createdProjectId"
            },
    {
                id: 2,
                category: "19.Ilmiy loyihalar",
                name: "Loyihani olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EProject/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        placeholder: "UUID yoki {createdProjectId}",
                        useStoredId: "createdProjectId"
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
                description: `**Loyihani ID bo'yicha olish** (GET by ID)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProject/{entityId}?returnNulls=true

<b>Path Parameters:</b>
- entityId - Loyiha UUID (required)

<b>Query Parameters:</b>
- returnNulls - null qiymatlarni qaytarish (default: true)

<b>Response:</b> Loyiha to'liq ma'lumotlari`,
                ported: true
            },
    {
                id: 3,
                category: "19.Ilmiy loyihalar",
                name: "Loyihalar ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EProject",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "text",
                        defaultNew: "10",
                        defaultOld: "10",
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
                description: `**Loyihalar ro'yxati** (GET list)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProject?limit=10&offset=0

<b>Query Parameters:</b>
- limit - Qaytariladigan yozuvlar soni (default: 10)
- offset - O'tkazib yuboriladigan yozuvlar (default: 0)
- returnNulls - null qiymatlarni qaytarish (default: true)

<b>Response:</b> Massiv formatida loyihalar ro'yxati`,
                ported: true
            },
    {
                id: 4,
                category: "19.Ilmiy loyihalar",
                name: "Loyihani o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EProject/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        placeholder: "UUID yoki {createdProjectId}",
                        useStoredId: "createdProjectId"
                    }
                },
                description: `**Loyihani o'chirish** (DELETE - Soft delete)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EProject/{entityId}

<b>Path Parameters:</b>
- entityId - Loyiha UUID (required)

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_19;
}
