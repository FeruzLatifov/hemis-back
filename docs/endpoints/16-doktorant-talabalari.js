// 16.Doktorant talabalari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_16 = [
    // ============================================
    // 16.Doktorant talabalari (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "16.Doktorant talabalari",
                name: "Doktorant yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    code: {
                        label: "Doktorant kodi",
                        type: "text",
                        default: "401-DOC-TEST-NEW",
                        defaultOld: "351-DOC-TEST-OLD",
                        required: true,
                        placeholder: "401-DOC-TEST"
                    },
                    firstname: {
                        label: "Ism",
                        type: "text",
                        default: "Test",
                        required: true
                    },
                    lastname: {
                        label: "Familiya",
                        type: "text",
                        default: "Doktorant",
                        required: true
                    },
                    fathername: {
                        label: "Otasining ismi",
                        type: "text",
                        default: "Testovich",
                        required: false
                    },
                    university: {
                        label: "OTM kodi",
                        type: "text",
                        default: "401",
                        defaultOld: "351",
                        required: true
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
                bodyGenerator: (inputs) => ({
                    code: inputs.code,
                    firstname: inputs.firstname,
                    lastname: inputs.lastname,
                    fathername: inputs.fathername,
                    university: {code: inputs.university},
                    active: inputs.active === 'true'
                }),
                description: "Yangi doktorant talaba yaratish. CUBA format: university: {code: '401'}",
                ported: true,
                storeResultId: "doctoralStudentId"  // POST single object qaytaradi, array emas
            },
    {
                id: 2,
                category: "16.Doktorant talabalari",
                name: "Doktorantni yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    entityId: {
                        label: "Doktorant ID (UUID)",
                        type: "text",
                        default: "",
                        defaultOld: "",
                        required: true,
                        placeholder: "POST dan olingan UUID",
                        useStoredId: "doctoralStudentId"
                    },
                    firstname: {
                        label: "Yangi ism",
                        type: "text",
                        default: "TestUpdated",
                        defaultOld: "TestUpdatedOld",
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
                bodyFields: ["firstname", "active"],
                description: "Doktorant ma'lumotlarini yangilash. Faqat yuborilgan maydonlar yangilanadi.",
                ported: true
            },
    {
                id: 3,
                category: "16.Doktorant talabalari",
                name: "Doktorantni o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    entityId: {
                        label: "Doktorant ID (o'chirish uchun)",
                        type: "text",
                        default: "",
                        defaultOld: "",
                        required: true,
                        placeholder: "POST dan olingan UUID",
                        useStoredId: "doctoralStudentId"
                    }
                },
                description: "Doktorantni o'chirish (soft delete). 200 OK qaytaradi.",
                ported: true
            },
    {
                id: 4,
                category: "16.Doktorant talabalari",
                name: "Doktorant olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    entityId: {
                        label: "Doktorant ID (UUID)",
                        type: "text",
                        default: "",
                        defaultOld: "",
                        required: true,
                        placeholder: "Mavjud doktorant UUID"
                    },
                    view: {
                        label: "View",
                        type: "text",
                        default: "_local",
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
                description: "Bitta doktorantni UUID bo'yicha olish.",
                ported: true
            },
    {
                id: 5,
                category: "16.Doktorant talabalari",
                name: "Barcha doktorantlar (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent",
                requiresAuth: true,
                inputFields: {
                    view: {
                        label: "View",
                        type: "text",
                        default: "_local",
                        required: false
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 10,
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
                description: "Barcha doktorantlarni olish (pagination bilan).",
                ported: true
            },
    {
                id: 6,
                category: "16.Doktorant talabalari",
                name: "Doktorantlarni qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent/search",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    filter: {
                        label: "CUBA Filter (JSON)",
                        type: "textarea",
                        default: '{"conditions":[{"property":"active","operator":"=","value":true}]}',
                        defaultOld: '{"conditions":[{"property":"active","operator":"=","value":true}]}',
                        required: true,
                        placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                    },
                    view: {
                        label: "View",
                        type: "text",
                        default: "_local",
                        required: false
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 10,
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: 0,
                        required: false
                    }
                },
                description: "CUBA JSON filter bilan doktorantlarni qidirish (GET).",
                ported: true
            },
    {
                id: 7,
                category: "16.Doktorant talabalari",
                name: "Doktorantlarni qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent/search",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    filter: {
                        label: "CUBA Filter (JSON)",
                        type: "textarea",
                        default: '{"conditions":[{"property":"active","operator":"=","value":true}]}',
                        defaultOld: '{"conditions":[{"property":"active","operator":"=","value":true}]}',
                        required: false,
                        placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 10,
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: 0,
                        required: false
                    }
                },
                hasBody: true,
                bodyGenerator: (inputs) => {
                    try {
                        const filterObj = JSON.parse(inputs.filter || '{"conditions":[]}');
                        return { filter: filterObj };
                    } catch (e) {
                        return { filter: { conditions: [] } };
                    }
                },
                queryParamsFromInputs: ["limit", "offset"],
                description: "CUBA JSON filter bilan doktorantlarni qidirish (POST). Body={filter:{...}}",
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_16;
}
