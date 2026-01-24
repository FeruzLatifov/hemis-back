// 39.Xorijiy OTMda malaka oshirish endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// hemishe_RIAdministrativeEmployee2 - Xorijiy OTMlarda malaka oshirgan va stajirovka o'tgan o'qituvchilar

const endpoints_39 = [
    // ============================================
    // 39.Xorijiy OTMda malaka oshirish (7 endpoint)
    // ============================================
    {
        id: 1,
        category: "39.Xorijiy OTMda malaka oshirish",
        name: "Yangi administrative employee2 yaratish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2",
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
            _country: {
                label: "Davlat ID",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
            },
            _internshipForm: {
                label: "Stajirovka shakli ID",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "11",
                defaultOld: "11",
                required: false,
                cubaForeignKey: true
            },
            _internshipType: {
                label: "Stajirovka turi ID",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "11",
                defaultOld: "11",
                required: false,
                cubaForeignKey: true
            },
            // Oddiy maydonlar
            foreignUniversity: {
                label: "Chet el universiteti nomi",
                type: "text",
                defaultNew: "Massachusetts Institute of Technology",
                defaultOld: "Stanford University",
                required: false
            },
            specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                defaultNew: "01.01.01",
                defaultOld: "02.02.02",
                required: false
            },
            specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                defaultNew: "Texnologik mashinalar. Robotlar, mexatronika va robototexnika tizimlari",
                defaultOld: "Amaliy matematika va informatika",
                required: false
            },
            trainingTypeName: {
                label: "Ta'lim turi nomi",
                type: "text",
                defaultNew: "Programming",
                defaultOld: "Data Science",
                required: false
            },
            trainingContract: {
                label: "Shartnoma raqami",
                type: "text",
                defaultNew: "123",
                defaultOld: "456",
                required: false
            },
            trainingDateStart: {
                label: "Boshlash sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2024-09-01",
                defaultOld: "2023-09-01",
                required: false
            },
            trainingDateEnd: {
                label: "Tugash sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2024-12-30",
                defaultOld: "2023-12-30",
                required: false
            },
            year: {
                label: "Yil",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2023",
                required: false
            },
            subject: {
                label: "Fanlar",
                type: "text",
                defaultNew: "Subject1, Subject2",
                defaultOld: "Subject3, Subject4",
                required: false
            }
        },
        hasBody: true,
        bodyFields: ["_university", "_educationYear", "_employee", "_country", "_internshipForm", "_internshipType", "foreignUniversity", "specialityCode", "specialityName", "trainingTypeName", "trainingContract", "trainingDateStart", "trainingDateEnd", "year", "subject"],
        description: `**Yangi administrative employee2 yozuvi yaratish**

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2

<b>Ma'lumot:</b> Xorijiy OTMlarda malaka oshirgan va stajirovka o'tgan o'qituvchilar

<b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
- _university: Universitet ID (required)
- _educationYear: O'quv yili ID (required)
- _employee: Xodim UUID
- _country: Davlat ID
- _internshipForm: Stajirovka shakli ID
- _internshipType: Stajirovka turi ID

<b>Oddiy maydonlar:</b>
- foreignUniversity: Chet el universiteti nomi
- specialityCode, specialityName: Mutaxassislik kodi va nomi
- trainingTypeName: Ta'lim turi nomi
- trainingContract: Shartnoma raqami
- trainingDateStart, trainingDateEnd: Boshlash va tugash sanalari
- year: Yil
- subject: Fanlar ro'yxati

<b>Response:</b> 201 Created - Yaratilgan yozuv`,
        ported: true,
        storeResultId: "administrativeEmployee2Id"
    },
    {
        id: 2,
        category: "39.Xorijiy OTMda malaka oshirish",
        name: "Administrative employee2 ID bo'yicha olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}",
        requiresAuth: true,
        inputFields: {
            entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee2Id"
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
        description: `**Administrative employee2 yozuvini ID bo'yicha olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}

<b>Workflow:</b> #1 POST dan yaratilgan ID avtomatik qo'yiladi.

<b>Response:</b> 200 OK - Topilgan yozuv yoki 404 Not Found`,
        ported: true
    },
    {
        id: 3,
        category: "39.Xorijiy OTMda malaka oshirish",
        name: "Administrative employee2 yangilash (PUT)",
        method: "PUT",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}",
        requiresAuth: true,
        inputFields: {
            entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee2Id"
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
            _internshipForm: {
                label: "Stajirovka shakli ID (yangi)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
            },
            _internshipType: {
                label: "Stajirovka turi ID (yangi)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
            },
            // Oddiy maydonlar
            foreignUniversity: {
                label: "Chet el universiteti (yangi qiymat)",
                type: "text",
                defaultNew: "Harvard University",
                defaultOld: "University of Cambridge",
                required: false
            },
            trainingTypeName: {
                label: "Ta'lim turi nomi (yangi qiymat)",
                type: "text",
                defaultNew: "Artificial Intelligence",
                defaultOld: "Machine Learning",
                required: false
            },
            specialityName: {
                label: "Mutaxassislik nomi (yangi qiymat)",
                type: "text",
                defaultNew: "Kompyuter fanlari",
                defaultOld: "Ma'lumotlar fani",
                required: false
            }
        },
        hasBody: true,
        bodyFields: ["_country", "_internshipForm", "_internshipType", "foreignUniversity", "trainingTypeName", "specialityName"],
        description: `**Administrative employee2 yozuvini yangilash**

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}

<b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
- _country: Davlat ID
- _internshipForm: Stajirovka shakli ID
- _internshipType: Stajirovka turi ID

<b>Oddiy maydonlar:</b>
- foreignUniversity: Chet el universiteti nomi
- trainingTypeName: Ta'lim turi nomi
- specialityName: Mutaxassislik nomi

<b>Response:</b> 200 OK - Yangilangan yozuv yoki 404 Not Found`,
        ported: true
    },
    {
        id: 4,
        category: "39.Xorijiy OTMda malaka oshirish",
        name: "Administrative employee2 o'chirish (DELETE)",
        method: "DELETE",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}",
        requiresAuth: true,
        inputFields: {
            entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee2Id"
            }
        },
        description: `**Administrative employee2 yozuvini o'chirish (soft delete)**

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}

<b>Response:</b> 200 OK yoki 404 Not Found

<b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
        ported: true
    },
    {
        id: 5,
        category: "39.Xorijiy OTMda malaka oshirish",
        name: "Barcha administrative employee2 olish (GET all)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2",
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
        description: `**Barcha administrative employee2 yozuvlarini olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2

<b>Parameters:</b>
- limit: Sahifa hajmi (default: 50)
- offset: Boshlanish indeksi (default: 0)

<b>Response:</b> Sahifalangan ro'yxat`,
        ported: true
    },
    {
        id: 6,
        category: "39.Xorijiy OTMda malaka oshirish",
        name: "Administrative employee2 qidirish (GET /search)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/search",
        requiresAuth: true,
        inputFields: {
            filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"foreignUniversity","operator":"contains","value":"MIT"}]}',
                defaultNew: '{"conditions":[{"property":"foreignUniversity","operator":"notEmpty"}]}',
                defaultOld: '{"conditions":[{"property":"foreignUniversity","operator":"notEmpty"}]}',
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
        description: `**Administrative employee2 yozuvlarini qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/search

<b>Filter misollari:</b>
- {"conditions":[{"property":"foreignUniversity","operator":"contains","value":"MIT"}]}
- {"conditions":[{"property":"trainingTypeName","operator":"notEmpty"}]}
- {"conditions":[{"property":"year","operator":"=","value":"2024"}]}

<b>Response:</b> Filter shartiga mos yozuvlar`,
        ported: true
    },
    {
        id: 7,
        category: "39.Xorijiy OTMda malaka oshirish",
        name: "Administrative employee2 qidirish (POST /search)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/search",
        requiresAuth: true,
        inputFields: {
            filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"foreignUniversity","operator":"notEmpty"}]}',
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
        description: `**Administrative employee2 yozuvlarini qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/search

<b>Filter misollari:</b>
- {"conditions":[{"property":"year","operator":"=","value":"2024"}]}
- {"conditions":[{"property":"foreignUniversity","operator":"contains","value":"Harvard"}]}
- {"conditions":[{"property":"trainingTypeName","operator":"startsWith","value":"Programming"}]}

<b>Response:</b> Filter shartiga mos yozuvlar`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_39;
}
