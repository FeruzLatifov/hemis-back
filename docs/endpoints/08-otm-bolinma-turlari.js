// 08.OTM bo'linma turlari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_08 = [
    // ============================================
    // 08.OTM bo'linma turlari (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "08.OTM bo'linma turlari",
                name: "Bo'linma turini ID bo'yicha olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                inputFields: {
                    entityId: {
                        label: "Bo'linma turi kodi",
                        type: "text",
                        default: "11",
                        required: true,
                        placeholder: "11 = Fakultet, 12 = Kafedra"
                    },
                    dynamicAttributes: {
                        label: "Dynamic attributes",
                        type: "select",
                        options: ["", "true", "false"],
                        default: "",
                        required: false
                    },
                    returnNulls: {
                        label: "Return nulls",
                        type: "select",
                        options: ["", "true", "false"],
                        default: "",
                        required: false
                    }
                },
                description: "Bo'linma turini code bo'yicha olish. Masalan: 11=Fakultet, 12=Kafedra, 13=Bo'lim",
                ported: true
            },
    {
                id: 2,
                category: "08.OTM bo'linma turlari",
                name: "Barcha bo'linma turlarini olish (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType",
                requiresAuth: true,
                dependsOn: 1,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: "20",
                        required: false,
                        placeholder: "Nechta yozuv"
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: "0",
                        required: false,
                        placeholder: "Qayerdan boshlash"
                    },
                    returnCount: {
                        label: "Return count",
                        type: "select",
                        options: ["", "true", "false"],
                        default: "",
                        required: false
                    }
                },
                description: "Barcha bo'linma turlarini sahifalangan ro'yxat sifatida olish",
                ported: true
            },
    {
                id: 3,
                category: "08.OTM bo'linma turlari",
                name: "Bo'linma turlarini qidirish (GET search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType/search",
                requiresAuth: true,
                dependsOn: 1,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON format)",
                        type: "textarea",
                        rows: 3,
                        default: '{"conditions":[{"property":"name","operator":"contains","value":"Fakultet"}]}',
                        required: true,
                        placeholder: '{"conditions":[{"property":"name","operator":"contains","value":"..."}]}',
                        helpText: 'CUBA filter: property=name/code/active, operator=contains/=/startsWith'
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: "20",
                        required: false
                    },
                    returnCount: {
                        label: "Return count",
                        type: "select",
                        options: ["", "true", "false"],
                        default: "",
                        required: false
                    }
                },
                description: "Bo'linma turlarini CUBA JSON filter bilan qidirish",
                ported: true
            },
    {
                id: 4,
                category: "08.OTM bo'linma turlari",
                name: "Bo'linma turlarini qidirish (POST search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType/search",
                requiresAuth: true,
                dependsOn: 1,
                contentType: "json",
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: "20",
                        required: false
                    },
                    returnCount: {
                        label: "Return count",
                        type: "select",
                        options: ["", "true", "false"],
                        default: "",
                        required: false
                    }
                },
                requestBody: {
                    filter: {
                        conditions: [
                            { property: "name", operator: "contains", value: "Kafedra" }
                        ]
                    }
                },
                description: "Bo'linma turlarini CUBA JSON filter bilan qidirish (POST)",
                ported: true
            },
    {
                id: 5,
                category: "08.OTM bo'linma turlari",
                name: "Yangi bo'linma turi yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType",
                requiresAuth: true,
                dependsOn: 1,
                contentType: "json",
                inputFields: {
                    code: {
                        label: "Kod",
                        type: "text",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        placeholder: "NEW=98, OLD=99",
                        helpText: "🆕 Yangi Hemis: 98 | 🏛️ Eski Hemis: 99"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        defaultNew: "Test turi (Yangi Hemis)",
                        defaultOld: "Test turi (Eski Hemis)",
                        required: true
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        defaultNew: "Тестовый тип (Новый Hemis)",
                        defaultOld: "Тестовый тип (Старый Hemis)",
                        required: false
                    },
                    nameEn: {
                        label: "Nomi (Inglizcha)",
                        type: "text",
                        defaultNew: "Test type (New Hemis)",
                        defaultOld: "Test type (Old Hemis)",
                        required: false
                    }
                },
                bodyTemplate: {
                    code: "{code}",
                    name: "{name}",
                    nameRu: "{{nameRu}}",
                    nameEn: "{{nameEn}}"
                },
                description: "Yangi bo'linma turi yaratish. 🆕 NEW=98, 🏛️ OLD=99 - alohida test ma'lumotlari",
                ported: true
            },
    {
                id: 6,
                category: "08.OTM bo'linma turlari",
                name: "Bo'linma turini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                contentType: "json",
                inputFields: {
                    entityId: {
                        label: "Bo'linma turi kodi",
                        type: "text",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        placeholder: "NEW=98, OLD=99",
                        helpText: "🆕 Yangi Hemis: 98 | 🏛️ Eski Hemis: 99 (avval #5 da yaratilgan)"
                    },
                    name: {
                        label: "Yangi nom (O'zbekcha)",
                        type: "text",
                        defaultNew: "Yangilangan (Yangi Hemis)",
                        defaultOld: "Yangilangan (Eski Hemis)",
                        required: true
                    },
                    nameRu: {
                        label: "Yangi nom (Ruscha)",
                        type: "text",
                        defaultNew: "Обновлено (Новый Hemis)",
                        defaultOld: "Обновлено (Старый Hemis)",
                        required: false
                    }
                },
                bodyTemplate: {
                    name: "{name}",
                    nameRu: "{{nameRu}}"
                },
                description: "Mavjud bo'linma turini yangilash. Avval #5 da yaratilgan yozuvni yangilaydi",
                ported: true
            },
    {
                id: 7,
                category: "08.OTM bo'linma turlari",
                name: "Bo'linma turini o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                inputFields: {
                    entityId: {
                        label: "Bo'linma turi kodi",
                        type: "text",
                        defaultNew: "98",
                        defaultOld: "99",
                        required: true,
                        placeholder: "NEW=98, OLD=99",
                        helpText: "🆕 Yangi Hemis: 98 | 🏛️ Eski Hemis: 99 (avval #5 da yaratilgan)"
                    }
                },
                description: "Bo'linma turini soft delete qilish. Test ketma-ketligi: #5 yaratish → #6 yangilash → #7 o'chirish",
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_08;
}
