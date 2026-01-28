// 11.Fuqarolik holatlari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_11 = [
    // ============================================
    // 11.Fuqarolik holatlari (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "11.Fuqarolik holatlari",
                name: "Barcha fuqarolik holatlari (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HCitizenship",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 50,
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: 0,
                        required: false
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Fuqarolik holatlari klassifikatori: 11=O'zbekiston fuqarosi, 12=Xorijiy fuqaro, 13=Fuqaroligi yo'q",
                ported: true,
                storeFirstId: "citizenshipCode"
            },
    {
                id: 2,
                category: "11.Fuqarolik holatlari",
                name: "Fuqarolik holatini olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HCitizenship/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Fuqarolik kodi (11-13)",
                        type: "text",
                        default: "11",
                        required: true,
                        placeholder: "11"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Kod bo'yicha fuqarolik holatini olish. 11=O'zbekiston, 12=Xorijiy, 13=Fuqaroligi yo'q",
                dependsOn: 1,
                ported: true
            },
    {
                id: 3,
                category: "11.Fuqarolik holatlari",
                name: "Yangi fuqarolik holatini yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HCitizenship",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    code: {
                        label: "Kod (unique)",
                        type: "text",
                        default: "991",
                        defaultOld: "992",
                        required: true,
                        placeholder: "991"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        default: "Test fuqarolik (Yangi)",
                        defaultOld: "Test fuqarolik (Eski)",
                        required: true
                    },
                    nameEn: {
                        label: "Nomi (Inglizcha)",
                        type: "text",
                        default: "Test citizenship (New)",
                        defaultOld: "Test citizenship (Old)",
                        required: false
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        default: "Тестовое гражданство (Новый)",
                        defaultOld: "Тестовое гражданство (Старый)",
                        required: false
                    },
                    active: {
                        label: "Faol",
                        type: "select",
                        options: [{value: "true", label: "Ha"}, {value: "false", label: "Yo'q"}],
                        default: "true",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["code", "name", "nameEn", "nameRu", "active"],
                description: "Yangi fuqarolik holati yaratish. CUBA Platform kabi upsert qiladi.",
                dependsOn: 1,
                ported: true
            },
    {
                id: 4,
                category: "11.Fuqarolik holatlari",
                name: "Fuqarolik holatini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HCitizenship/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    entityId: {
                        label: "Fuqarolik kodi",
                        type: "text",
                        default: "991",
                        defaultOld: "992",
                        required: true,
                        placeholder: "991"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        default: "Test fuqarolik YANGILANGAN (Yangi)",
                        defaultOld: "Test fuqarolik YANGILANGAN (Eski)",
                        required: false
                    },
                    nameEn: {
                        label: "Nomi (Inglizcha)",
                        type: "text",
                        default: "Test citizenship UPDATED (New)",
                        defaultOld: "Test citizenship UPDATED (Old)",
                        required: false
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        default: "Тестовое гражданство ОБНОВЛЕНО (Новый)",
                        defaultOld: "Тестовое гражданство ОБНОВЛЕНО (Старый)",
                        required: false
                    },
                    active: {
                        label: "Faol",
                        type: "select",
                        options: [{value: "", label: "O'zgartirmaslik"}, {value: "true", label: "Ha"}, {value: "false", label: "Yo'q"}],
                        default: "",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["name", "nameEn", "nameRu", "active"],
                description: "Fuqarolik holatini yangilash. Faqat yuborilgan maydonlar yangilanadi.",
                dependsOn: 3,
                ported: true
            },
    {
                id: 5,
                category: "11.Fuqarolik holatlari",
                name: "Fuqarolik holatini o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HCitizenship/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    entityId: {
                        label: "Fuqarolik kodi",
                        type: "text",
                        default: "991",
                        defaultOld: "992",
                        required: true,
                        placeholder: "991"
                    }
                },
                description: "Fuqarolik holatini o'chirish (soft delete). Faqat test uchun yaratilgan yozuvlarni o'chiring!",
                dependsOn: 4,
                ported: true
            },
    {
                id: 6,
                category: "11.Fuqarolik holatlari",
                name: "Fuqarolik holatlarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HCitizenship/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "CUBA Filter (JSON)",
                        type: "textarea",
                        default: '{"conditions":[]}',
                        required: false,
                        placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 50,
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: 0,
                        required: false
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "CUBA filter formatida fuqarolik holatlarini qidirish.",
                dependsOn: 1,
                ported: true
            },
    {
                id: 7,
                category: "11.Fuqarolik holatlari",
                name: "Fuqarolik holatlarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HCitizenship/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "CUBA Filter (JSON)",
                        type: "textarea",
                        default: '{"conditions":[]}',
                        required: false,
                        placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 50,
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: 0,
                        required: false
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["filter"],
                description: "CUBA filter formatida fuqarolik holatlarini qidirish. Filter body da JSON.",
                dependsOn: 1,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_11;
}
