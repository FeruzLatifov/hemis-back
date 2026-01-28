// 10.Talaba holati endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_10 = [
    // ============================================
    // 10.Talaba holati (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "10.Talaba holati",
                name: "Barcha talaba holatlari (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType",
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
                description: "Talaba holatlari klassifikatori: 10=Boshqa, 11=O'qimoqda, 12=Chetlashgan, 13=Akademik ta'til, 14=Bitirgan",
                ported: true,
                storeFirstId: "studentStatusCode"
            },
    {
                id: 2,
                category: "10.Talaba holati",
                name: "Talaba holatini olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Holat kodi (10-14)",
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
                description: "Kod bo'yicha talaba holatini olish. Holatlar: 10=Boshqa, 11=O'qimoqda, 12=Chetlashgan, 13=Akademik ta'til, 14=Bitirgan",
                dependsOn: 1,
                ported: true
            },
    {
                id: 3,
                category: "10.Talaba holati",
                name: "Yangi talaba holatini yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType",
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
                        default: "Test holati (Yangi)",
                        defaultOld: "Test holati (Eski)",
                        required: true
                    },
                    nameEn: {
                        label: "Nomi (Inglizcha)",
                        type: "text",
                        default: "Test status (New)",
                        defaultOld: "Test status (Old)",
                        required: false
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        default: "Тестовый статус (Новый)",
                        defaultOld: "Тестовый статус (Старый)",
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
                description: "Yangi talaba holati yaratish. CUBA Platform kabi upsert qiladi - mavjud bo'lsa yangilaydi.",
                dependsOn: 1,
                ported: true
            },
    {
                id: 4,
                category: "10.Talaba holati",
                name: "Talaba holatini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    entityId: {
                        label: "Holat kodi",
                        type: "text",
                        default: "991",
                        defaultOld: "992",
                        required: true,
                        placeholder: "991"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        default: "Test holati YANGILANGAN (Yangi)",
                        defaultOld: "Test holati YANGILANGAN (Eski)",
                        required: false
                    },
                    nameEn: {
                        label: "Nomi (Inglizcha)",
                        type: "text",
                        default: "Test status UPDATED (New)",
                        defaultOld: "Test status UPDATED (Old)",
                        required: false
                    },
                    nameRu: {
                        label: "Nomi (Ruscha)",
                        type: "text",
                        default: "Тестовый статус ОБНОВЛЕНО (Новый)",
                        defaultOld: "Тестовый статус ОБНОВЛЕНО (Старый)",
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
                description: "Talaba holatini yangilash. Faqat yuborilgan maydonlar yangilanadi.",
                dependsOn: 3,
                ported: true
            },
    {
                id: 5,
                category: "10.Talaba holati",
                name: "Talaba holatini o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    entityId: {
                        label: "Holat kodi",
                        type: "text",
                        default: "991",
                        defaultOld: "992",
                        required: true,
                        placeholder: "991"
                    }
                },
                description: "Talaba holatini o'chirish (soft delete). Faqat test uchun yaratilgan yozuvlarni o'chiring!",
                dependsOn: 4,
                ported: true
            },
    {
                id: 6,
                category: "10.Talaba holati",
                name: "Talaba holatlarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType/search",
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
                description: "CUBA filter formatida talaba holatlarini qidirish. filter parametri URL encoded JSON formatida.",
                dependsOn: 1,
                ported: true
            },
    {
                id: 7,
                category: "10.Talaba holati",
                name: "Talaba holatlarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType/search",
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
                description: "CUBA filter formatida talaba holatlarini qidirish. Filter body da JSON formatida yuboriladi.",
                dependsOn: 1,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_10;
}
