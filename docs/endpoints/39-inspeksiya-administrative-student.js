// 39.Inspeksiya administrative student endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Xorij OTMlari bilan akademik almashinuv dasturlari - talabalar

const endpoints_39 = [
    // ============================================
    // 39.Inspeksiya administrative student (7 endpoint)
    // ============================================
    {
        id: 1,
        category: "39.Inspeksiya administrative student",
        name: "Yangi yozuv yaratish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2",
        requiresAuth: true,
        dependsOn: 1,
        description: "Xorij OTMlari bilan akademik almashinuv - yangi talaba yozuvi yaratish",
        inputFields: [
            { name: "_university", label: "OTM (UUID)", type: "text", required: true, placeholder: "OTM UUID" },
            { name: "_education_year", label: "O'quv yili (UUID)", type: "text", required: true, placeholder: "O'quv yili UUID" },
            { name: "student_fullname", label: "Talaba FIO", type: "text", required: true, placeholder: "Familiya Ism Otasining ismi" },
            { name: "_country", label: "Davlat (UUID)", type: "text", required: false, placeholder: "Davlat UUID" },
            { name: "exchange_university_name", label: "Xorij OTM nomi", type: "text", required: false, placeholder: "Harvard University" },
            { name: "exchange_document", label: "Shartnoma/Hujjat", type: "text", required: false, placeholder: "Shartnoma raqami" },
            { name: "education_type", label: "Ta'lim turi (UUID)", type: "text", required: false, placeholder: "Ta'lim turi UUID" },
            { name: "speciality_code", label: "Mutaxassislik kodi", type: "text", required: false, placeholder: "5110100" },
            { name: "speciality_name", label: "Mutaxassislik nomi", type: "text", required: false, placeholder: "Informatika va AT" },
            { name: "exchange_type", label: "Almashinuv turi", type: "text", required: false, placeholder: "Bir semestr / To'liq kurs" }
        ],
        bodyGenerator: (fields) => ({
            "_university": fields._university ? { "id": fields._university } : null,
            "_education_year": fields._education_year ? { "id": fields._education_year } : null,
            "student_fullname": fields.student_fullname || null,
            "_country": fields._country ? { "id": fields._country } : null,
            "exchange_university_name": fields.exchange_university_name || null,
            "exchange_document": fields.exchange_document || null,
            "education_type": fields.education_type ? { "id": fields.education_type } : null,
            "speciality_code": fields.speciality_code || null,
            "speciality_name": fields.speciality_name || null,
            "exchange_type": fields.exchange_type || null
        }),
        ported: true
    },
    {
        id: 2,
        category: "39.Inspeksiya administrative student",
        name: "Barcha yozuvlarni olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2",
        requiresAuth: true,
        dependsOn: 1,
        description: "Barcha akademik almashinuv yozuvlarini olish (paginated)",
        queryParams: [
            { name: "limit", label: "Limit", defaultValue: "50" },
            { name: "offset", label: "Offset", defaultValue: "0" },
            { name: "returnCount", label: "Jami sonni qaytarish", defaultValue: "true" },
            { name: "returnNulls", label: "Null qiymatlarni qaytarish", defaultValue: "false" }
        ],
        ported: true
    },
    {
        id: 3,
        category: "39.Inspeksiya administrative student",
        name: "ID bo'yicha olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Akademik almashinuv yozuvini UUID bo'yicha olish",
        inputFields: [
            { name: "entityId", label: "Entity UUID", type: "text", required: true, placeholder: "00000000-0000-0000-0000-000000000000" }
        ],
        queryParams: [
            { name: "returnNulls", label: "Null qiymatlarni qaytarish", defaultValue: "false" }
        ],
        ported: true
    },
    {
        id: 4,
        category: "39.Inspeksiya administrative student",
        name: "Yozuvni yangilash (PUT)",
        method: "PUT",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Mavjud akademik almashinuv yozuvini yangilash",
        inputFields: [
            { name: "entityId", label: "Entity UUID", type: "text", required: true, placeholder: "00000000-0000-0000-0000-000000000000" },
            { name: "student_fullname", label: "Talaba FIO", type: "text", required: false, placeholder: "Familiya Ism Otasining ismi" },
            { name: "exchange_university_name", label: "Xorij OTM nomi", type: "text", required: false, placeholder: "Harvard University" },
            { name: "exchange_document", label: "Shartnoma/Hujjat", type: "text", required: false, placeholder: "Shartnoma raqami" },
            { name: "speciality_code", label: "Mutaxassislik kodi", type: "text", required: false, placeholder: "5110100" },
            { name: "speciality_name", label: "Mutaxassislik nomi", type: "text", required: false, placeholder: "Informatika va AT" },
            { name: "exchange_type", label: "Almashinuv turi", type: "text", required: false, placeholder: "Bir semestr / To'liq kurs" }
        ],
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.student_fullname) body.student_fullname = fields.student_fullname;
            if (fields.exchange_university_name) body.exchange_university_name = fields.exchange_university_name;
            if (fields.exchange_document) body.exchange_document = fields.exchange_document;
            if (fields.speciality_code) body.speciality_code = fields.speciality_code;
            if (fields.speciality_name) body.speciality_name = fields.speciality_name;
            if (fields.exchange_type) body.exchange_type = fields.exchange_type;
            return body;
        },
        ported: true
    },
    {
        id: 5,
        category: "39.Inspeksiya administrative student",
        name: "Yozuvni o'chirish (DELETE)",
        method: "DELETE",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Akademik almashinuv yozuvini o'chirish (soft delete)",
        inputFields: [
            { name: "entityId", label: "Entity UUID", type: "text", required: true, placeholder: "00000000-0000-0000-0000-000000000000" }
        ],
        ported: true
    },
    {
        id: 6,
        category: "39.Inspeksiya administrative student",
        name: "Qidirish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Akademik almashinuv yozuvlarini qidirish (GET parametrlar bilan)",
        queryParams: [
            { name: "filter", label: "CUBA JSON filter", defaultValue: "" },
            { name: "limit", label: "Limit", defaultValue: "50" },
            { name: "offset", label: "Offset", defaultValue: "0" },
            { name: "returnNulls", label: "Null qiymatlarni qaytarish", defaultValue: "false" }
        ],
        ported: true
    },
    {
        id: 7,
        category: "39.Inspeksiya administrative student",
        name: "Qidirish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Akademik almashinuv yozuvlarini qidirish (POST body bilan)",
        inputFields: [
            { name: "limit", label: "Limit", type: "number", required: false, placeholder: "50" },
            { name: "offset", label: "Offset", type: "number", required: false, placeholder: "0" }
        ],
        bodyFields: ["filter", "limit", "offset"],
        bodyGenerator: (fields) => ({
            "filter": {},
            "limit": parseInt(fields.limit) || 50,
            "offset": parseInt(fields.offset) || 0
        }),
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_39;
}
