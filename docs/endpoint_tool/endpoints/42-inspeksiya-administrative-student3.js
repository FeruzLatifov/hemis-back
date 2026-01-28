// 42.Inspeksiya administrative student3 endpoints
// Bitiruvchilar band bo'lishi - Bакалавр битирувчиларининг иш билан таъминланганлиги
// hemishe_RIAdministrativeStudent3

const endpoints_42 = [
    // ============================================
    // 42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi) - 7 endpoint
    // ============================================
    {
        id: 1,
        category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
        name: "Yangi yozuv yaratish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3",
        requiresAuth: true,
        dependsOn: 1,
        description: "Bitiruvchilar band bo'lishi - yangi yozuv yaratish",
        storeResultId: "administrativeStudent3EntityId",
        inputFields: {
            university: { label: "Universitet ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
            educationYear: { label: "O'quv yili ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
            student: { label: "Talaba ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
            company: { label: "Kompaniya nomi", type: "text", required: false, placeholder: "Kompaniya nomi", defaultNew: "Test Kompaniya NEW", defaultOld: "Test Kompaniya OLD" },
            position: { label: "Lavozim", type: "text", required: false, placeholder: "Lavozim", defaultNew: "Dasturchi", defaultOld: "Dasturchi" },
            mastersUniversityName: { label: "Magistratura OTM nomi", type: "text", required: false, placeholder: "Magistratura OTM", defaultNew: "", defaultOld: "" },
            educationType: { label: "Ta'lim turi ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.university) body.university = { id: fields.university };
            if (fields.educationYear) body.educationYear = { id: fields.educationYear };
            if (fields.student) body.student = { id: fields.student };
            if (fields.company) body.company = fields.company;
            if (fields.position) body.position = fields.position;
            if (fields.mastersUniversityName) body.mastersUniversityName = fields.mastersUniversityName;
            if (fields.educationType) body.educationType = { id: fields.educationType };
            return body;
        },
        ported: true
    },
    {
        id: 2,
        category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
        name: "Barcha yozuvlarni olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3",
        requiresAuth: true,
        dependsOn: 1,
        description: "Bitiruvchilar band bo'lishi - barcha yozuvlarni olish",
        storeFirstId: "administrativeStudent3EntityId",
        inputFields: {
            limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
            offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
            returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false }
        },
        ported: true
    },
    {
        id: 3,
        category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
        name: "ID bo'yicha olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Bitiruvchilar band bo'lishi - ID bo'yicha olish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent3EntityId" }
        },
        ported: true
    },
    {
        id: 4,
        category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
        name: "Yozuvni yangilash (PUT)",
        method: "PUT",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Bitiruvchilar band bo'lishi - yozuvni yangilash",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent3EntityId" },
            company: { label: "Kompaniya nomi", type: "text", required: false, placeholder: "Kompaniya nomi", defaultNew: "Yangilangan Kompaniya", defaultOld: "Yangilangan Kompaniya" },
            position: { label: "Lavozim", type: "text", required: false, placeholder: "Lavozim", defaultNew: "Senior Dasturchi", defaultOld: "Senior Dasturchi" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.company) body.company = fields.company;
            if (fields.position) body.position = fields.position;
            return body;
        },
        ported: true
    },
    {
        id: 5,
        category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
        name: "Yozuvni o'chirish (DELETE)",
        method: "DELETE",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Bitiruvchilar band bo'lishi - yozuvni o'chirish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent3EntityId" }
        },
        ported: true
    },
    {
        id: 6,
        category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
        name: "Qidirish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Bitiruvchilar band bo'lishi - qidirish (GET)",
        inputFields: {
            filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
            limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
            offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.filter && fields.filter.trim()) {
                params.push("filter=" + encodeURIComponent(fields.filter.trim()));
            } else {
                params.push("filter=" + encodeURIComponent('{"conditions":[]}'));
            }
            if (fields.limit) params.push("limit=" + encodeURIComponent(fields.limit));
            if (fields.offset) params.push("offset=" + encodeURIComponent(fields.offset));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        ported: true
    },
    {
        id: 7,
        category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
        name: "Qidirish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Bitiruvchilar band bo'lishi - qidirish (POST)",
        inputFields: {
            filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
            limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
            offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
        },
        bodyGenerator: (fields) => {
            const body = { limit: parseInt(fields.limit) || 50, offset: parseInt(fields.offset) || 0 };
            if (fields.filter && fields.filter.trim()) {
                try { body.filter = JSON.parse(fields.filter); } catch (e) { body.filter = { conditions: [] }; }
            } else {
                body.filter = { conditions: [] };
            }
            return body;
        },
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_42;
}
