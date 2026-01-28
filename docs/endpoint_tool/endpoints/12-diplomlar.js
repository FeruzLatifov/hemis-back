// 12.Diplomlar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_12 = [
    // ============================================
    // 12.Diplomlar (6 endpoint)
    // ============================================
    {
                id: 1,
                category: "12.Diplomlar",
                name: "Yangi diploma yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    diplomaNumber: {
                        label: "Diploma raqami",
                        type: "text",
                        defaultNew: "NEW-" + Date.now(),
                        defaultOld: "OLD-" + Date.now(),
                        required: true,
                        placeholder: "AA-1234567"
                    },
                    university: {
                        label: "OTM kodi",
                        type: "text",
                        defaultNew: "401",
                        defaultOld: "351",
                        required: true,
                        placeholder: "OTM code (401 yoki 351)"
                    },
                    student: {
                        label: "Talaba UUID",
                        type: "text",
                        defaultNew: "0d738e89-a9a9-9b9c-22d7-19912228b063",
                        defaultOld: "ad0ee8cc-b5a6-3192-61a4-d4ee911a7912",
                        required: true,
                        placeholder: "Talaba entity UUID"
                    },
                    speciality: {
                        label: "Mutaxassislik UUID",
                        type: "text",
                        defaultNew: "4c991851-7287-4330-a003-0b8362542439",
                        defaultOld: "83b9d50f-b49d-d5b5-327c-adcef0a51e2d",
                        required: true,
                        placeholder: "Mutaxassislik UUID"
                    },
                    active: {
                        label: "Active",
                        type: "select",
                        options: [{value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "true",
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
                // OLD-HEMIS format: _entityName + nested refs with _entityName, speciality is plain string
                bodyGenerator: (inputs) => ({
                    _entityName: "hemishe_EStudentDiploma",
                    university: {_entityName: "hemishe_EUniversity", id: inputs.university},
                    student: {_entityName: "hemishe_EStudent", id: inputs.student},
                    speciality: inputs.speciality,
                    diplomaNumber: inputs.diplomaNumber,
                    active: inputs.active === 'true'
                }),
                description: "Yangi diploma yaratish. Yaratilgan ID avtomatik PUT va DELETE ga o'tadi. CUBA format.",
                ported: true,
                storeResultId: "createdDiplomaId"
            },
    {
                id: 2,
                category: "12.Diplomlar",
                name: "Diplomni yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Diploma ID (UUID)",
                        type: "text",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        placeholder: "POST ishga tushsa avtomatik to'ldiriladi",
                        useStoredId: "createdDiplomaId"
                    },
                    diplomaNumber: {
                        label: "Yangi diploma raqami",
                        type: "text",
                        defaultNew: "UPDATED-NEW-" + Date.now(),
                        defaultOld: "UPDATED-OLD-" + Date.now(),
                        required: false,
                        placeholder: "Yangilanadigan raqam"
                    },
                    registerNumber: {
                        label: "Registr raqami",
                        type: "text",
                        default: "99999",
                        required: false,
                        placeholder: "12345"
                    },
                    active: {
                        label: "Active",
                        type: "select",
                        options: [{value: "", label: "-- tanlang --"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "true",
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
                // OLD-HEMIS format: _entityName + update fields
                bodyGenerator: (inputs) => {
                    const body = {_entityName: "hemishe_EStudentDiploma"};
                    if (inputs.diplomaNumber) body.diplomaNumber = inputs.diplomaNumber;
                    if (inputs.registerNumber) body.registerNumber = inputs.registerNumber;
                    if (inputs.active === 'true' || inputs.active === 'false') body.active = inputs.active === 'true';
                    return body;
                },
                description: "2️⃣ POST da yaratilgan diplomni yangilash. entityId avtomatik to'ldiriladi.",
                ported: true
            },
    {
                id: 3,
                category: "12.Diplomlar",
                name: "Diplomni o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Diploma ID (o'chirish uchun)",
                        type: "text",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        placeholder: "POST ishga tushsa avtomatik to'ldiriladi",
                        useStoredId: "createdDiplomaId"
                    }
                },
                description: "3️⃣ POST da yaratilgan diplomni o'chirish. entityId avtomatik to'ldiriladi.",
                ported: true
            },
    {
                id: 4,
                category: "12.Diplomlar",
                name: "Diplomni olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Diploma ID (UUID)",
                        type: "text",
                        defaultNew: "d2adcec7-92eb-2936-2e97-4860c802ff03",
                        defaultOld: "de11c5a1-2943-4a9d-016f-f0a160e0af00",
                        required: true,
                        placeholder: "UUID formatda diploma ID (401: B00844218, 351: B00781880)"
                    },
                    view: {
                        label: "View",
                        type: "text",
                        default: "eStudentDiploma-view",
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
                description: "Bitta diplomni UUID bo'yicha olish. view parametri bilan bog'liq entity ma'lumotlari ham qaytariladi.",
                ported: true
            },
    {
                id: 5,
                category: "12.Diplomlar",
                name: "Barcha diplomlar (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma",
                requiresAuth: true,
                // ⚠️ ESLATMA: Old Hemis CUBA GET /entities da filter ishlamaydi!
                // Filterlash uchun POST /search (#7) ishlatilsin
                inputFields: {
                    view: {
                        label: "View",
                        type: "text",
                        default: "eStudentDiploma-view",
                        required: false
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 1,  // ⚠️ 1 ta diplom - solishtirish uchun yetarli
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
                description: "⚠️ Old Hemis da filter ishlamaydi! Filterlash uchun POST /search (#7) ishlating. Bu endpoint faqat tuzilmani solishtiradi.",
                ported: true,
                storeFirstId: "diplomaId"
            },
    {
                id: 6,
                category: "12.Diplomlar",
                name: "Diplomlarni qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma/search",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                    filter: {
                        label: "CUBA Filter (JSON) - Required",
                        type: "textarea",
                        default: '{"conditions":[{"property":"diplomaNumber","operator":"=","value":"B00844218"}]}',
                        defaultOld: '{"conditions":[{"property":"diplomaNumber","operator":"=","value":"B00781880"}]}',
                        required: true,
                        placeholder: '{"conditions":[{"property":"diplomaNumber","operator":"=","value":"..."}]}'
                    },
                    view: {
                        label: "View",
                        type: "text",
                        default: "eStudentDiploma-view",
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
                    returnCount: {
                        label: "Return Count (X-Total-Count header)",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
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
                description: "GET /search - filter query param sifatida (URL encoded JSON). Old Hemis bilan bir xil.",
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_12;
}
