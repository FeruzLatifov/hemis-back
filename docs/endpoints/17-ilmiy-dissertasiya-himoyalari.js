// 17.Ilmiy dissertasiya himoyalari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_17 = [
    // ============================================
    // 17.Ilmiy dissertasiya himoyalari (4 endpoint)
    // ============================================
    {
                id: 1,
                category: "17.Ilmiy dissertasiya himoyalari",
                name: "Dissertasiya himoyasi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EDissertationDefense",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                    body_defenseDate: {
                        label: "defenseDate (Himoya sanasi)",
                        type: "text",
                        defaultNew: "2024-06-15",
                        defaultOld: "2021-02-10",
                        required: true,
                        bodyField: "defenseDate",
                        placeholder: "YYYY-MM-DD"
                    },
                    body_defense_place: {
                        label: "defense_place (Himoya joyi)",
                        type: "text",
                        defaultNew: "Toshkent Davlat Texnika Universiteti",
                        defaultOld: "Toshkent",
                        required: true,
                        bodyField: "defense_place"
                    },
                    body_approvedDate: {
                        label: "approvedDate (Tasdiqlangan sana)",
                        type: "text",
                        defaultNew: "2024-07-01",
                        defaultOld: "2021-02-26",
                        required: false,
                        bodyField: "approvedDate",
                        placeholder: "YYYY-MM-DD"
                    },
                    body_diplomaNumber: {
                        label: "diplomaNumber (Diplom raqami)",
                        type: "text",
                        defaultNew: "01 № 123456",
                        defaultOld: "01 № 232321",
                        required: false,
                        bodyField: "diplomaNumber"
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
                    defenseDate: inputs.body_defenseDate,
                    defense_place: inputs.body_defense_place,
                    approvedDate: inputs.body_approvedDate || undefined,
                    diplomaNumber: inputs.body_diplomaNumber || undefined,
                    active: inputs.body_active === 'true'
                }),
                description: `**Dissertasiya himoyasi yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EDissertationDefense

<b>Response (OLD-HEMIS format):</b>
<pre>{
  "_entityName": "hemishe_EDissertationDefense",
  "_instanceName": "com.company.hemishe.entity.EDissertationDefense-uuid [detached]",
  "id": "uuid-here"
}</pre>`,
                ported: true,
                storeFirstId: "createdDissertationDefenseId"
            },
    {
                id: 2,
                category: "17.Ilmiy dissertasiya himoyalari",
                name: "Dissertasiya himoyasini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EDissertationDefense/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Dissertasiya himoyasi ID (UUID)",
                        type: "text",
                        placeholder: "Dissertasiya himoyasi UUID",
                        defaultNew: "d5480757-8405-4aa4-60ed-15a07cf7dd46",
                        defaultOld: "d5480757-8405-4aa4-60ed-15a07cf7dd46",
                        required: true
                    }
                },
                description: `**Dissertasiya himoyasini o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EDissertationDefense/{entityId}

<b>Response:</b>
- <b>200 OK</b> - Muvaffaqiyatli o'chirildi
- <b>404 Not Found</b> - Topilmadi

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            },
    {
                id: 3,
                category: "17.Ilmiy dissertasiya himoyalari",
                name: "Dissertasiya himoyasini olish (view=eDissertationDefense-view)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EDissertationDefense/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Dissertasiya himoyasi ID (UUID)",
                        type: "text",
                        placeholder: "Dissertasiya himoyasi UUID",
                        defaultNew: "74649688-aef2-0171-da92-6eac77730bd2",
                        defaultOld: "74649688-aef2-0171-da92-6eac77730bd2",
                        required: true
                    },
                    view: {
                        label: "View nomi",
                        type: "text",
                        default: "eDissertationDefense-view",
                        required: true
                    }
                },
                description: `**Dissertasiya himoyasini olish** (GET by ID with view)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EDissertationDefense/{entityId}?view=eDissertationDefense-view

<b>Response (to'liq nested objectlar):</b>
<pre>{
  "_entityName": "hemishe_EDissertationDefense",
  "id": "74649688-aef2-0171-da92-6eac77730bd2",
  "defenseDate": "2021-02-10",
  "doctorateStudent": {
    "_entityName": "hemishe_EDoctorateStudent",
    "_instanceName": "XAMDAMOVA DILFUZA",
    "id": "...",
    "firstName": "DILFUZA",
    "secondName": "XAMDAMOVA"
  },
  "speciality": {
    "_entityName": "hemishe_HSpecialityDoctoral",
    "id": "...",
    "name": "Hisoblash mashinalari..."
  }
}</pre>`,
                ported: true
            },
    {
                id: 4,
                category: "17.Ilmiy dissertasiya himoyalari",
                name: "Dissertasiya himoyalari ro'yxati (view=eDissertationDefense-view)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EDissertationDefense",
                requiresAuth: true,
                inputFields: {
                    view: {
                        label: "View nomi",
                        type: "text",
                        default: "eDissertationDefense-view",
                        required: true
                    },
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
                description: `**Dissertasiya himoyalari ro'yxati** (GET list with view)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EDissertationDefense?view=eDissertationDefense-view&limit=10

<b>Response:</b> Massiv formatida, to'liq nested objectlar bilan`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_17;
}
