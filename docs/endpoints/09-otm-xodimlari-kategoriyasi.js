// 09.OTM xodimlari kategoriyasi endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_09 = [
    // ============================================
    // 09.OTM xodimlari kategoriyasi (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "09.OTM xodimlari kategoriyasi",
                name: "Barcha OTM xodim kategoriyalari (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType",
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
                description: "OTM xodimlari kategoriyalari klassifikatori: 10=Boshqa, 11=Administrativ-boshqaruv, 12=Professor-o'qituvchi, 13=O'quv-yordamchi va texnik, 14=Xizmat ko'rsatuvchi",
                ported: true,
                storeFirstId: "employeeTypeCode"
            },
    {
                id: 2,
                category: "09.OTM xodimlari kategoriyasi",
                name: "OTM xodim kategoriyasini olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Tur kodi (10, 11, 12, 13, 14)",
                        type: "text",
                        default: "12",
                        required: true,
                        useStoredId: "employeeTypeCode"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Bitta OTM xodim kategoriyasini kod bo'yicha olish. Kodlar: 10=Boshqa, 11=Administrativ, 12=Professor, 13=Texnik, 14=Xizmat",
                dependsOn: 1,
                ported: true
            },
    {
                id: 3,
                category: "09.OTM xodimlari kategoriyasi",
                name: "Yangi OTM xodim kategoriyasini yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    id: {
                        label: "Tur kodi (unique)",
                        type: "number",
                        default: 991,
                        defaultOld: 992,
                        required: true,
                        hint: "Yangi: 991, Eski: 992 (turli kodlar - bitta baza)"
                    },
                    name: {
                        label: "Nomi (O'zbekcha)",
                        type: "text",
                        default: "Test xodim turi (NEW)",
                        defaultOld: "Test xodim turi (OLD)",
                        required: true
                    },
                    active: {
                        label: "Faol",
                        type: "select",
                        options: [{value: true, label: "Ha"}, {value: false, label: "Yo'q"}],
                        default: true,
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["id", "name", "active"],
                description: "Yangi OTM xodim kategoriyasini yaratish. id (code) va name majburiy. ⚠️ Yangi va Eski uchun turli kodlar kiriting!",
                storeFirstId: "newEmployeeTypeId",
                ported: true
            },
    {
                id: 4,
                category: "09.OTM xodimlari kategoriyasi",
                name: "OTM xodim kategoriyasini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    entityId: {
                        label: "Tur kodi",
                        type: "number",
                        default: 991,
                        defaultOld: 992,
                        required: true,
                        hint: "#3 da yaratilgan kodni kiriting"
                    },
                    name: {
                        label: "Yangi nomi (O'zbekcha)",
                        type: "text",
                        default: "Yangilangan nom (NEW)",
                        defaultOld: "Yangilangan nom (OLD)",
                        required: false
                    },
                    active: {
                        label: "Faol",
                        type: "select",
                        options: [{value: "", label: "O'zgartirmaslik"}, {value: true, label: "Ha"}, {value: false, label: "Yo'q"}],
                        default: "",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["name", "active"],
                description: "OTM xodim kategoriyasini yangilash. Avval #3 orqali yarating, keyin shu yerda yangilang.",
                dependsOn: 3,
                ported: true
            },
    {
                id: 5,
                category: "09.OTM xodimlari kategoriyasi",
                name: "OTM xodim kategoriyasini o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    entityId: {
                        label: "Tur kodi",
                        type: "number",
                        default: 991,
                        defaultOld: 992,
                        required: true,
                        hint: "#3 da yaratilgan kodni kiriting"
                    }
                },
                description: "OTM xodim kategoriyasini o'chirish (soft delete). Avval #3 da yarating → #4 da yangilang → #5 da o'chiring.",
                dependsOn: 3,
                ported: true
            },
    {
                id: 6,
                category: "09.OTM xodimlari kategoriyasi",
                name: "OTM xodim kategoriyalarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType/search",
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
                description: "CUBA filter formatida OTM xodim kategoriyalarini qidirish. filter parametri URL encoded JSON formatida.",
                dependsOn: 1,
                ported: true
            },
    {
                id: 7,
                category: "09.OTM xodimlari kategoriyasi",
                name: "OTM xodim kategoriyalarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType/search",
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
                bodyFields: ["filter", "limit", "offset"],
                description: "CUBA filter formatida OTM xodim kategoriyalarini qidirish. Filter body da JSON formatida yuboriladi.",
                dependsOn: 1,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_09;
}
