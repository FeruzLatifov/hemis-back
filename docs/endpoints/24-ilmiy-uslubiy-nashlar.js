// 24.Ilmiy uslubiy nashlar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_24 = [
    // ============================================
    // 24.Ilmiy uslubiy nashlar (5 endpoint)
    // ============================================
    {
                id: 1,
                category: "24.Ilmiy uslubiy nashlar",
                name: "Yangi uslubiy nashr yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EPublicationMethodical",
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
                    name: {
                        label: "Nomi",
                        type: "text",
                        placeholder: "Uslubiy nashr nomi",
                        defaultNew: "Test uslubiy nashr (Yangi Hemis)",
                        defaultOld: "Test uslubiy nashr (Eski Hemis)",
                        required: true
                    },
                    authors: {
                        label: "Mualliflar",
                        type: "text",
                        placeholder: "Familiya I.O., Familiya I.O.",
                        defaultNew: "Test A.B., Namuna C.D.",
                        defaultOld: "Test A.B., Namuna C.D.",
                        required: false
                    },
                    author_counts: {
                        label: "Mualliflar soni",
                        type: "number",
                        placeholder: "2",
                        defaultNew: "2",
                        defaultOld: "2",
                        required: false
                    },
                    publisher: {
                        label: "Nashriyot",
                        type: "text",
                        placeholder: "Nashriyot nomi",
                        defaultNew: "Test Nashriyot",
                        defaultOld: "Test Nashriyot",
                        required: false
                    },
                    issue_year: {
                        label: "Chiqish yili",
                        type: "number",
                        placeholder: "2024",
                        defaultNew: "2024",
                        defaultOld: "2024",
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
                    name: "{name}",
                    authors: "{authors}",
                    author_counts: "{author_counts}",
                    publisher: "{publisher}",
                    issue_year: "{issue_year}",
                    active: "{active}"
                },
                description: `**Yangi uslubiy nashr yaratish** (POST)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EPublicationMethodical

<b>Ma'lumotlar:</b>
- _university: OTM kodi (401=Yangi, 351=Eski)
- name: Uslubiy nashr nomi
- authors: Mualliflar ro'yxati
- author_counts: Mualliflar soni
- publisher: Nashriyot
- issue_year: Chiqish yili
- source_name: Manba nomi
- _methodical_publication_type: Nashr turi kodi
- _publication_database: Baza kodi
- _employee: Xodim UUID
- _education_year: O'quv yili kodi
- active: Faol holati

<b>Response:</b> Yaratilgan entity CUBA formatda`,
                ported: true,
                storeResultId: "publicationMethodicalId"
            },
    {
                id: 2,
                category: "24.Ilmiy uslubiy nashlar",
                name: "Uslubiy nashrlar ro'yxatini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationMethodical",
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
                params: {
                    limit: "{limit}",
                    offset: "{offset}"
                },
                description: `**Uslubiy nashrlar ro'yxati** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationMethodical

<b>Parametrlar:</b>
- limit: Natijalar soni (default: 50)
- offset: Boshlang'ich pozitsiya
- returnNulls: Null qiymatlarni qaytarish
- view: Ko'rinish nomi

<b>Response:</b> Uslubiy nashrlar massivi`,
                ported: true
            },
    {
                id: 3,
                category: "24.Ilmiy uslubiy nashlar",
                name: "Uslubiy nashrni ID bo'yicha olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Uslubiy nashr ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationMethodicalId"
                    }
                },
                description: `**Uslubiy nashrni ID bo'yicha olish** (GET)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}

<b>Response:</b> Uslubiy nashr ma'lumotlari CUBA formatda`,
                ported: true
            },
    {
                id: 4,
                category: "24.Ilmiy uslubiy nashlar",
                name: "Uslubiy nashrni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                    entityId: {
                        label: "Uslubiy nashr ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationMethodicalId"
                    },
                    name: {
                        label: "Yangi nom",
                        type: "text",
                        placeholder: "Uslubiy nashr nomi",
                        defaultNew: "Yangilangan uslubiy nashr",
                        defaultOld: "Yangilangan uslubiy nashr",
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
                    name: "{name}",
                    active: "{active}"
                },
                description: `**Uslubiy nashrni yangilash** (PUT)

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}

<b>Body:</b> O'zgartiriladigan maydonlar JSON formatda

<b>Response:</b> Yangilangan entity CUBA formatda`,
                ported: true
            },
    {
                id: 5,
                category: "24.Ilmiy uslubiy nashlar",
                name: "Uslubiy nashrni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Uslubiy nashr ID (UUID)",
                        type: "text",
                        placeholder: "UUID formatda ID",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "publicationMethodicalId"
                    }
                },
                description: `**Uslubiy nashrni o'chirish** (DELETE)

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}

<b>Response:</b> 200 OK (empty body)

<b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_24;
}
