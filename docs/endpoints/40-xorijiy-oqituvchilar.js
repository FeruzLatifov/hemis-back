// 40.OTMda xorijiy o'qituvchilar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// hemishe_RIAdministrativeEmployee3 - OTMda faoliyat olib borayotgan xorijiy o'qituvchilar

const endpoints_40 = [
    // ============================================
    // 40.OTMda xorijiy o'qituvchilar (7 endpoint)
    // ============================================
    {
        id: 1,
        category: "40.OTMda xorijiy o'qituvchilar",
        name: "Yangi administrative employee3 yaratish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3",
        requiresAuth: true,
        inputFields: {
            // Foreign Key maydonlari (CUBA format)
            _university: {
                label: "Universitet ID",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                cubaForeignKey: true
            },
            _educationYear: {
                label: "O'quv yili ID",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                cubaForeignKey: true
            },
            _country: {
                label: "Davlat ID",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
            },
            _employee: {
                label: "Xodim ID (UUID)",
                type: "text",
                placeholder: "Xodim UUID",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true,
                useStoredId: "employeeId"
            },
            _employeeForm: {
                label: "Xodim shakli ID",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "11",
                defaultOld: "11",
                required: false,
                cubaForeignKey: true
            },
            _condutionForm: {
                label: "O'tkazish shakli ID",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "11",
                defaultOld: "11",
                required: false,
                cubaForeignKey: true
            },
            // Oddiy maydonlar
            fullname: {
                label: "To'liq ism",
                type: "text",
                defaultNew: "John Smith",
                defaultOld: "David Johnson",
                required: false
            },
            workPlace: {
                label: "Ish joyi",
                type: "text",
                defaultNew: "Harvard University",
                defaultOld: "MIT",
                required: false
            },
            specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                defaultNew: "Computer Science",
                defaultOld: "Data Science",
                required: false
            },
            subject: {
                label: "Fan nomi",
                type: "text",
                defaultNew: "Programming",
                defaultOld: "Machine Learning",
                required: false
            },
            contractData: {
                label: "Shartnoma ma'lumotlari",
                type: "text",
                defaultNew: "Contract-2024-001",
                defaultOld: "Contract-2023-002",
                required: false
            },
            arrivalDate: {
                label: "Kelish sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2024-01-15",
                defaultOld: "2023-01-20",
                required: false
            },
            departureDate: {
                label: "Ketish sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2025-01-15",
                defaultOld: "2024-01-20",
                required: false
            },
            lessonTime: {
                label: "Dars soatlari",
                type: "number",
                defaultNew: "200",
                defaultOld: "150",
                required: false
            },
            year: {
                label: "Yil",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2023",
                required: false
            }
        },
        hasBody: true,
        bodyFields: ["_university", "_educationYear", "_country", "_employee", "_employeeForm", "_condutionForm", "fullname", "workPlace", "specialityName", "subject", "contractData", "arrivalDate", "departureDate", "lessonTime", "year"],
        description: `**Yangi administrative employee3 yozuvi yaratish**

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3

<b>Ma'lumot:</b> OTMda faoliyat olib borayotgan xorijiy o'qituvchilar

<b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
- _university: Universitet ID (required)
- _educationYear: O'quv yili ID (required)
- _country: Davlat ID
- _employee: Xodim UUID
- _employeeForm: Xodim shakli ID
- _condutionForm: O'tkazish shakli ID

<b>Oddiy maydonlar:</b>
- fullname: Xorijiy o'qituvchining to'liq ismi
- workPlace: Ish joyi (asosiy universitet)
- specialityName: Mutaxassislik nomi
- subject: O'tiladigan fan nomi
- contractData: Shartnoma ma'lumotlari
- arrivalDate, departureDate: Kelish va ketish sanalari
- lessonTime: Dars soatlari soni
- year: Yil

<b>Response:</b> 201 Created - Yaratilgan yozuv`,
        ported: true,
        storeResultId: "administrativeEmployee3Id"
    },
    {
        id: 2,
        category: "40.OTMda xorijiy o'qituvchilar",
        name: "Administrative employee3 ID bo'yicha olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}",
        requiresAuth: true,
        inputFields: {
            entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee3Id"
            },
            returnNulls: {
                label: "Null qiymatlarni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                defaultNew: "",
                defaultOld: "",
                required: false
            }
        },
        description: `**Administrative employee3 yozuvini ID bo'yicha olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}

<b>Workflow:</b> #1 POST dan yaratilgan ID avtomatik qo'yiladi.

<b>Response:</b> 200 OK - Topilgan yozuv yoki 404 Not Found`,
        ported: true
    },
    {
        id: 3,
        category: "40.OTMda xorijiy o'qituvchilar",
        name: "Administrative employee3 yangilash (PUT)",
        method: "PUT",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}",
        requiresAuth: true,
        inputFields: {
            entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee3Id"
            },
            // Foreign Key maydonlari
            _country: {
                label: "Davlat ID (yangi)",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
            },
            _employeeForm: {
                label: "Xodim shakli ID (yangi)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
            },
            _condutionForm: {
                label: "O'tkazish shakli ID (yangi)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
            },
            // Oddiy maydonlar
            fullname: {
                label: "To'liq ism (yangi qiymat)",
                type: "text",
                defaultNew: "Robert Brown",
                defaultOld: "Michael Davis",
                required: false
            },
            workPlace: {
                label: "Ish joyi (yangi qiymat)",
                type: "text",
                defaultNew: "Stanford University",
                defaultOld: "University of Cambridge",
                required: false
            },
            specialityName: {
                label: "Mutaxassislik nomi (yangi qiymat)",
                type: "text",
                defaultNew: "Artificial Intelligence",
                defaultOld: "Deep Learning",
                required: false
            },
            lessonTime: {
                label: "Dars soatlari (yangi qiymat)",
                type: "number",
                defaultNew: "250",
                defaultOld: "180",
                required: false
            }
        },
        hasBody: true,
        bodyFields: ["_country", "_employeeForm", "_condutionForm", "fullname", "workPlace", "specialityName", "lessonTime"],
        description: `**Administrative employee3 yozuvini yangilash**

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}

<b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
- _country: Davlat ID
- _employeeForm: Xodim shakli ID
- _condutionForm: O'tkazish shakli ID

<b>Oddiy maydonlar:</b>
- fullname: To'liq ism
- workPlace: Ish joyi
- specialityName: Mutaxassislik nomi
- lessonTime: Dars soatlari

<b>Response:</b> 200 OK - Yangilangan yozuv yoki 404 Not Found`,
        ported: true
    },
    {
        id: 4,
        category: "40.OTMda xorijiy o'qituvchilar",
        name: "Administrative employee3 o'chirish (DELETE)",
        method: "DELETE",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}",
        requiresAuth: true,
        inputFields: {
            entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee3Id"
            }
        },
        description: `**Administrative employee3 yozuvini o'chirish (soft delete)**

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}

<b>Response:</b> 200 OK yoki 404 Not Found

<b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
        ported: true
    },
    {
        id: 5,
        category: "40.OTMda xorijiy o'qituvchilar",
        name: "Barcha administrative employee3 olish (GET all)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3",
        requiresAuth: true,
        inputFields: {
            limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
                required: false
            },
            offset: {
                label: "Offset",
                type: "number",
                defaultNew: "0",
                defaultOld: "0",
                required: false
            },
            returnNulls: {
                label: "Null qiymatlarni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                defaultNew: "",
                defaultOld: "",
                required: false
            }
        },
        description: `**Barcha administrative employee3 yozuvlarini olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3

<b>Parameters:</b>
- limit: Sahifa hajmi (default: 50)
- offset: Boshlanish indeksi (default: 0)

<b>Response:</b> Sahifalangan ro'yxat`,
        ported: true
    },
    {
        id: 6,
        category: "40.OTMda xorijiy o'qituvchilar",
        name: "Administrative employee3 qidirish (GET /search)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search",
        requiresAuth: true,
        inputFields: {
            filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"fullname","operator":"contains","value":"John"}]}',
                defaultNew: '{"conditions":[{"property":"fullname","operator":"notEmpty"}]}',
                defaultOld: '{"conditions":[{"property":"fullname","operator":"notEmpty"}]}',
                required: false,
                rows: 3
            },
            limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
                required: false
            },
            offset: {
                label: "Offset",
                type: "number",
                defaultNew: "0",
                defaultOld: "0",
                required: false
            },
            returnNulls: {
                label: "Null qiymatlarni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                defaultNew: "",
                defaultOld: "",
                required: false
            }
        },
        description: `**Administrative employee3 yozuvlarini qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search

<b>Filter misollari:</b>
- {"conditions":[{"property":"fullname","operator":"contains","value":"John"}]}
- {"conditions":[{"property":"workPlace","operator":"notEmpty"}]}
- {"conditions":[{"property":"year","operator":"=","value":"2024"}]}

<b>Response:</b> Filter shartiga mos yozuvlar`,
        ported: true
    },
    {
        id: 7,
        category: "40.OTMda xorijiy o'qituvchilar",
        name: "Administrative employee3 qidirish (POST /search)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search",
        requiresAuth: true,
        inputFields: {
            filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"fullname","operator":"notEmpty"}]}',
                defaultNew: '{"conditions":[{"property":"year","operator":"=","value":"2024"}]}',
                defaultOld: '{"conditions":[{"property":"year","operator":"=","value":"2023"}]}',
                required: false,
                rows: 3
            },
            limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
                required: false
            },
            offset: {
                label: "Offset",
                type: "number",
                defaultNew: "0",
                defaultOld: "0",
                required: false
            },
            returnNulls: {
                label: "Null qiymatlarni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                defaultNew: "",
                defaultOld: "",
                required: false
            }
        },
        hasBody: true,
        bodyFields: ["filter", "limit", "offset"],
        description: `**Administrative employee3 yozuvlarini qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search

<b>Filter misollari:</b>
- {"conditions":[{"property":"year","operator":"=","value":"2024"}]}
- {"conditions":[{"property":"fullname","operator":"contains","value":"Smith"}]}
- {"conditions":[{"property":"lessonTime","operator":">=","value":"100"}]}

<b>Response:</b> Filter shartiga mos yozuvlar`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_40;
}
