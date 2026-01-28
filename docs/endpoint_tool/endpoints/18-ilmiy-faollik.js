// 18.Ilmiy faollik endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_18 = [
    // ============================================
    // 18.Ilmiy faollik (5 endpoint)
    // ============================================
    {
                id: 1,
                category: "18.Ilmiy faollik",
                name: "Ilmiy faoliyatlar ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EResearchActivity",
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
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "false",
                        required: false
                    }
                },
                description: `**Ilmiy faoliyatlar ro'yxati** (GET all)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EResearchActivity

<b>Response format:</b>
<pre>[
  {
    "_entityName": "hemishe_EResearchActivity",
    "_instanceName": "com.company.hemishe.entity.EResearchActivity-{id} [detached]",
    "id": "uuid",
    "scientificWorkCount": "6",
    "link": "google scholar ...",
    "version": 1,
    "referenceCount": "22",
    "hIndex": "3"
  }
]</pre>`,
                ported: true,
                storeFirstId: "researchActivityId"
            },
    {
                id: 2,
                category: "18.Ilmiy faollik",
                name: "Ilmiy faoliyat yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EResearchActivity",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["hIndex", "scientificWorkCount", "referenceCount", "link"],
                inputFields: {
                    body_hIndex: {
                        label: "hIndex (H-indeks)",
                        type: "text",
                        defaultNew: "5",
                        defaultOld: "3",
                        required: true,
                        bodyField: "hIndex",
                        placeholder: "H-indeks raqami"
                    },
                    body_scientificWorkCount: {
                        label: "scientificWorkCount (Ilmiy ishlar soni)",
                        type: "text",
                        defaultNew: "15",
                        defaultOld: "10",
                        required: true,
                        bodyField: "scientificWorkCount"
                    },
                    body_referenceCount: {
                        label: "referenceCount (Iqtiboslar soni)",
                        type: "text",
                        defaultNew: "30",
                        defaultOld: "20",
                        required: true,
                        bodyField: "referenceCount"
                    },
                    body_link: {
                        label: "link (Scholar profil havolasi)",
                        type: "text",
                        defaultNew: "https://scholar.google.com/test-new",
                        defaultOld: "https://scholar.google.com/test-old",
                        required: false,
                        bodyField: "link",
                        placeholder: "https://scholar.google.com/..."
                    }
                },
                description: `**Ilmiy faoliyat yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EResearchActivity

<b>Request body:</b>
<pre>{
  "hIndex": "5",
  "scientificWorkCount": "15",
  "referenceCount": "30",
  "link": "https://scholar.google.com/..."
}</pre>

<b>Response:</b>
<pre>{
  "_entityName": "hemishe_EResearchActivity",
  "_instanceName": "com.company.hemishe.entity.EResearchActivity-{id} [detached]",
  "id": "yangi_uuid"
}</pre>`,
                ported: true,
                storeResultId: "researchActivityId"
            },
    {
                id: 3,
                category: "18.Ilmiy faollik",
                name: "Ilmiy faoliyatni olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EResearchActivity/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Ilmiy faoliyat ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "researchActivityId"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "false",
                        required: false
                    }
                },
                description: `**Ilmiy faoliyatni olish** (GET by ID)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EResearchActivity/{entityId}

<b>Response:</b> Bitta ilmiy faoliyat ma'lumotlari`,
                ported: true
            },
    {
                id: 4,
                category: "18.Ilmiy faollik",
                name: "Ilmiy faoliyatni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EResearchActivity/{entityId}",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["hIndex", "scientificWorkCount"],
                inputFields: {
                    entityId: {
                        label: "Ilmiy faoliyat ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "researchActivityId"
                    },
                    body_hIndex: {
                        label: "hIndex (yangi qiymat)",
                        type: "text",
                        defaultNew: "7",
                        defaultOld: "5",
                        required: true,
                        bodyField: "hIndex"
                    },
                    body_scientificWorkCount: {
                        label: "scientificWorkCount (yangi qiymat)",
                        type: "text",
                        defaultNew: "20",
                        defaultOld: "15",
                        required: true,
                        bodyField: "scientificWorkCount"
                    }
                },
                description: `**Ilmiy faoliyatni yangilash** (PUT)

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EResearchActivity/{entityId}

<b>Request body:</b>
<pre>{
  "hIndex": "7",
  "scientificWorkCount": "20"
}</pre>

<b>Response:</b> Minimal response - _entityName, _instanceName, id`,
                ported: true
            },
    {
                id: 5,
                category: "18.Ilmiy faollik",
                name: "Ilmiy faoliyatni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EResearchActivity/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Ilmiy faoliyat ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "researchActivityId"
                    }
                },
                description: `**Ilmiy faoliyatni o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EResearchActivity/{entityId}

<b>Response:</b>
- <b>200 OK</b> - Muvaffaqiyatli o'chirildi
- <b>404 Not Found</b> - Topilmadi

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_18;
}
