// 26.Nashrlarni baholash mezonlari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_26 = [
    // ============================================
    // 26.Nashrlarni baholash mezonlari (5 endpoint)
    // ============================================
    {
                id: 1,
                category: "26.Nashrlarni baholash mezonlari",
                name: "Yangi baholash mezoni yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EPublicationCriteria",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                    _university: {
                        label: "Universitet kodi",
                        type: "text",
                        placeholder: "401 yoki 351",
                        defaultNew: "401",
                        defaultOld: "351",
                        required: true
                    },
                    _education_year: {
                        label: "O'quv yili",
                        type: "text",
                        placeholder: "2024",
                        defaultNew: "2024",
                        defaultOld: "2024",
                        required: false
                    },
                    _publication_type_table: {
                        label: "Nashr turi jadvali",
                        type: "text",
                        placeholder: "hemishe_EPublicationScientific",
                        defaultNew: "hemishe_EPublicationScientific",
                        defaultOld: "hemishe_EPublicationScientific",
                        required: false
                    },
                    markValue: {
                        label: "Ball qiymati",
                        type: "number",
                        placeholder: "10",
                        defaultNew: "10",
                        defaultOld: "10",
                        required: false
                    },
                    active: {
                        label: "Faol",
                        type: "checkbox",
                        defaultNew: true,
                        defaultOld: true,
                        required: false
                    }
                },
                body: {
                    _university: "{_university}",
                    _education_year: "{_education_year}",
                    _publication_type_table: "{_publication_type_table}",
                    markValue: "{markValue}",
                    active: "{active}"
                },
                description: `**Yangi baholash mezoni yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EPublicationCriteria

<b>Ma'lumotlar:</b>
- _university: OTM kodi (401=Yangi, 351=Eski)
- _education_year: O'quv yili
- _publication_type_table: Nashr turi jadvali nomi
- _publication_methodical_type: Uslubiy nashr turi
- _publication_scientific_type: Ilmiy nashr turi
- _publication_property_type: Intellektual mulk turi
- inPublicationDatabase: Bazada mavjudligi (1/0)
- markValue: Ball qiymati
- position: Tartib raqami
- active: Faol holati

<b>Response:</b> Yaratilgan entity CUBA formatda`,
                ported: true,
                storeResultId: "publicationCriteriaId"
            },
    {
                id: 2,
                category: "26.Nashrlarni baholash mezonlari",
                name: "Baholash mezonlari ro'yxatini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationCriteria",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        placeholder: "50",
                        defaultNew: "10",
                        defaultOld: "10",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        placeholder: "0",
                        defaultNew: "0",
                        defaultOld: "0",
                        required: false
                    }
                },
                queryParamsFromInputs: ["limit", "offset"],
                description: `**Baholash mezonlari ro'yxati** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationCriteria

<b>Parametrlar:</b>
- limit: Natijalar soni (default: 50)
- offset: Boshlang'ich pozitsiya
- returnNulls: Null qiymatlarni qaytarish
- view: Ko'rinish nomi

<b>Response:</b> Baholash mezonlari massivi`,
                ported: true,
                storeFirstId: "publicationCriteriaId"
            },
    {
                id: 3,
                category: "26.Nashrlarni baholash mezonlari",
                name: "Baholash mezonini ID bo'yicha olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Baholash mezoni ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationCriteriaId"
                    }
                },
                description: `**Baholash mezonini ID bo'yicha olish** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}

<b>Response:</b> Baholash mezoni ma'lumotlari CUBA formatda`,
                ported: true
            },
    {
                id: 4,
                category: "26.Nashrlarni baholash mezonlari",
                name: "Baholash mezonini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_markValue", "body_active"],
                inputFields: {
                    entityId: {
                        label: "Baholash mezoni ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationCriteriaId"
                    },
                    body_markValue: {
                        label: "Yangi ball qiymati",
                        type: "number",
                        placeholder: "15",
                        defaultNew: "15",
                        defaultOld: "15",
                        required: false,
                        bodyField: "markValue"
                    },
                    body_active: {
                        label: "Faol",
                        type: "checkbox",
                        defaultNew: true,
                        defaultOld: true,
                        required: false,
                        bodyField: "active"
                    }
                },
                description: `**Baholash mezonini yangilash** (PUT)

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}

<b>Body:</b> O'zgartiriladigan maydonlar JSON formatda

<b>Response:</b> Yangilangan entity CUBA formatda`,
                ported: true
            },
    {
                id: 5,
                category: "26.Nashrlarni baholash mezonlari",
                name: "Baholash mezonini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Baholash mezoni ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationCriteriaId"
                    }
                },
                description: `**Baholash mezonini o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_26;
}
