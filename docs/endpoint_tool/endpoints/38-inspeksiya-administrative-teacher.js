// 38.Inspeksiya administrative teacher endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// hemishe_RIAdministrativeEmployee1 - Top-1000 universitetlaridan PhD/DSc darajali o'qituvchilar

const endpoints_38 = [
    // ============================================
    // 38.Inspeksiya administrative teacher (7 endpoint)
    // ============================================
    {
        id: 1,
        category: "38.Inspeksiya administrative teacher",
        name: "Yangi administrative employee1 yaratish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1",
        requiresAuth: true,
        inputFields: {
            // Foreign Key maydonlari (CUBA format)
            _university: {
                label: "Universitet ID",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "401",
                defaultOld: "351",
                required: false,
                cubaForeignKey: true
            },
            _educationYear: {
                label: "O'quv yili ID",
                type: "text",
                placeholder: "Masalan: 2024",
                defaultNew: "2024",
                defaultOld: "2024",
                required: false,
                cubaForeignKey: true
            },
            _employee: {
                label: "Xodim ID (UUID)",
                type: "text",
                placeholder: "Xodim UUID (Employee POST dan yoki bazadan)",
                defaultNew: "8a576cff-1de3-7b47-c3fb-87dd2796bea7",
                defaultOld: "8882879b-c9f2-defd-570c-02aed77faf71",
                required: false,
                cubaForeignKey: true,
                useStoredId: "employeeId"  // Employee POST endpoint bo'lsa shu ID ishlatiladi
            },
            _country: {
                label: "Davlat kodi",
                type: "text",
                placeholder: "Masalan: UZ, US",
                defaultNew: "US",
                defaultOld: "GB",
                required: false,
                cubaForeignKey: true
            },
            _degree: {
                label: "Ilmiy daraja ID",
                type: "text",
                placeholder: "Daraja kodi",
                defaultNew: "11",
                defaultOld: "12",
                required: false,
                cubaForeignKey: true
            },
            _rank: {
                label: "Ilmiy unvon ID",
                type: "text",
                placeholder: "Unvon kodi",
                defaultNew: "11",
                defaultOld: "12",
                required: false,
                cubaForeignKey: true
            },
            // Oddiy maydonlar
            foreignUniversity: {
                label: "Chet el universiteti nomi",
                type: "text",
                defaultNew: "Harvard University",
                defaultOld: "Massachusetts Institute of Technology",
                required: false
            },
            diplomaType: {
                label: "Diplom turi",
                type: "text",
                defaultNew: "PhD",
                defaultOld: "DSc",
                required: false
            },
            diplomaSerialNumber: {
                label: "Diplom seriya raqami",
                type: "text",
                defaultNew: "PHD-2024-001",
                defaultOld: "DSC-2023-002",
                required: false
            },
            diplomaDate: {
                label: "Diplom sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2024-06-15",
                defaultOld: "2023-05-20",
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
                defaultNew: "Amaliy matematika va informatika",
                defaultOld: "Nazariy fizika",
                required: false
            },
            councilDate: {
                label: "Kengash sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2024-05-20",
                defaultOld: "2023-04-15",
                required: false
            },
            councilNumber: {
                label: "Kengash raqami",
                type: "text",
                defaultNew: "DSc/PhD.03/30.12.2019.FM.02.01",
                defaultOld: "DSc/PhD.03/30.12.2019.Fiz.01.02",
                required: false
            }
        },
        hasBody: true,
        bodyFields: ["_university", "_educationYear", "_employee", "_country", "_degree", "_rank", "foreignUniversity", "diplomaType", "diplomaSerialNumber", "diplomaDate", "specialityCode", "specialityName", "councilDate", "councilNumber"],
        description: `**Yangi administrative employee1 yozuvi yaratish**

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1

<b>Ma'lumot:</b> Top-1000 universitetlaridan PhD/DSc darajali o'qituvchi

<b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
- _university: Universitet ID
- _educationYear: O'quv yili ID
- _employee: Xodim UUID
- _country: Davlat kodi (UZ, US, GB...)
- _degree: Ilmiy daraja ID
- _rank: Ilmiy unvon ID

<b>Oddiy maydonlar:</b>
- foreignUniversity: Chet el universiteti nomi
- diplomaType: PhD yoki DSc
- diplomaSerialNumber: Diplom seriya raqami
- diplomaDate: Diplom berilgan sana (YYYY-MM-DD)
- specialityCode: Mutaxassislik kodi
- specialityName: Mutaxassislik nomi
- councilDate: Kengash sanasi
- councilNumber: Kengash raqami

<b>Response:</b> 201 Created - Yaratilgan yozuv`,
        ported: true,
        storeResultId: "administrativeEmployee1Id"
    },
    {
        id: 2,
        category: "38.Inspeksiya administrative teacher",
        name: "Administrative employee1 ID bo'yicha olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}",
        requiresAuth: true,
        inputFields: {
            entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee1Id"
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
        description: `**Administrative employee1 yozuvini ID bo'yicha olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}

<b>Workflow:</b> #1 POST dan yaratilgan ID avtomatik qo'yiladi.

<b>Response:</b> 200 OK - Topilgan yozuv yoki 404 Not Found`,
        ported: true
    },
    {
        id: 3,
        category: "38.Inspeksiya administrative teacher",
        name: "Administrative employee1 yangilash (PUT)",
        method: "PUT",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}",
        requiresAuth: true,
        inputFields: {
            entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee1Id"
            },
            // Foreign Key maydonlari
            _university: {
                label: "Universitet ID (yangi)",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
            },
            _country: {
                label: "Davlat kodi (yangi)",
                type: "text",
                placeholder: "UZ, US, GB...",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
            },
            _degree: {
                label: "Ilmiy daraja ID (yangi)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
            },
            _rank: {
                label: "Ilmiy unvon ID (yangi)",
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
                defaultNew: "Stanford University",
                defaultOld: "University of Cambridge",
                required: false
            },
            diplomaType: {
                label: "Diplom turi (yangi qiymat)",
                type: "text",
                defaultNew: "DSc",
                defaultOld: "PhD",
                required: false
            },
            specialityName: {
                label: "Mutaxassislik nomi (yangi qiymat)",
                type: "text",
                defaultNew: "Kompyuter fanlari",
                defaultOld: "Kvant fizikasi",
                required: false
            }
        },
        hasBody: true,
        bodyFields: ["_university", "_country", "_degree", "_rank", "foreignUniversity", "diplomaType", "specialityName"],
        description: `**Administrative employee1 yozuvini yangilash**

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}

<b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
- _university: Universitet ID
- _country: Davlat kodi
- _degree: Ilmiy daraja ID
- _rank: Ilmiy unvon ID

<b>Oddiy maydonlar:</b>
- foreignUniversity: Chet el universiteti nomi
- diplomaType: PhD yoki DSc
- specialityName: Mutaxassislik nomi

<b>Response:</b> 200 OK - Yangilangan yozuv yoki 404 Not Found`,
        ported: true
    },
    {
        id: 4,
        category: "38.Inspeksiya administrative teacher",
        name: "Administrative employee1 o'chirish (DELETE)",
        method: "DELETE",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}",
        requiresAuth: true,
        inputFields: {
            entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee1Id"
            }
        },
        description: `**Administrative employee1 yozuvini o'chirish (soft delete)**

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}

<b>Response:</b> 200 OK yoki 404 Not Found

<b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
        ported: true
    },
    {
        id: 5,
        category: "38.Inspeksiya administrative teacher",
        name: "Barcha administrative employee1 olish (GET all)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1",
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
        description: `**Barcha administrative employee1 yozuvlarini olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1

<b>Parameters:</b>
- limit: Sahifa hajmi (default: 50)
- offset: Boshlanish indeksi (default: 0)

<b>Response:</b> Sahifalangan ro'yxat`,
        ported: true
    },
    {
        id: 6,
        category: "38.Inspeksiya administrative teacher",
        name: "Administrative employee1 qidirish (GET /search)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/search",
        requiresAuth: true,
        inputFields: {
            filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"diplomaType","operator":"=","value":"rank"}]}',
                defaultNew: '{"conditions":[{"property":"diplomaType","operator":"=","value":"rank"}]}',
                defaultOld: '{"conditions":[{"property":"diplomaType","operator":"=","value":"rank"}]}',
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
        description: `**Administrative employee1 yozuvlarini qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/search

<b>Filter misollari:</b>
- {"conditions":[{"property":"diplomaType","operator":"=","value":"rank"}]}
- {"conditions":[{"property":"foreignUniversity","operator":"contains","value":"davlat"}]}
- {"conditions":[{"property":"specialityName","operator":"notEmpty"}]}

<b>Response:</b> Filter shartiga mos yozuvlar`,
        ported: true
    },
    {
        id: 7,
        category: "38.Inspeksiya administrative teacher",
        name: "Administrative employee1 qidirish (POST /search)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/search",
        requiresAuth: true,
        inputFields: {
            filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"foreignUniversity","operator":"notEmpty"}]}',
                defaultNew: '{"conditions":[{"property":"foreignUniversity","operator":"contains","value":"davlat"}]}',
                defaultOld: '{"conditions":[{"property":"foreignUniversity","operator":"contains","value":"davlat"}]}',
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
        description: `**Administrative employee1 yozuvlarini qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/search

<b>Filter misollari:</b>
- {"conditions":[{"property":"foreignUniversity","operator":"contains","value":"davlat"}]}
- {"conditions":[{"property":"diplomaType","operator":"=","value":"rank"}]}
- {"conditions":[{"property":"specialityCode","operator":"startsWith","value":"01"}]}

<b>Response:</b> Filter shartiga mos yozuvlar`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_38;
}
