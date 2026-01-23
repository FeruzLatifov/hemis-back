// 44.Inspeksiya administrative StudentSport endpoints
// Talaba sport yutuqlari - Талабаларнинг спорт ютуқлари ва спорт разрядлари
// hemishe_RIAdministrativeStudentSport

const endpoints_44 = [
    // ============================================
    // 44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari) - 7 endpoint
    // ============================================
    {
        id: 1,
        category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
        name: "Yangi yozuv yaratish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba sport yutuqlari - yangi yozuv yaratish",
        storeResultId: "administrativeStudentSportEntityId",
        inputFields: {
            university: { label: "Universitet ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
            educationYear: { label: "O'quv yili ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
            student: { label: "Talaba ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
            sportType: { label: "Sport turi ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
            sportDate: { label: "Sana", type: "text", required: false, placeholder: "2024-01-01", defaultNew: "2024-01-15", defaultOld: "2024-01-15" },
            sportTypeRank: { label: "Sport razryadi", type: "text", required: false, placeholder: "1/2/3", defaultNew: "1", defaultOld: "1" },
            sportTypeRankDocument: { label: "Hujjat raqami", type: "text", required: false, placeholder: "AB123456", defaultNew: "AB123456", defaultOld: "AB123456" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.university) body.university = { id: fields.university };
            if (fields.educationYear) body.educationYear = { id: fields.educationYear };
            if (fields.student) body.student = { id: fields.student };
            if (fields.sportType) body.sportType = { id: fields.sportType };
            if (fields.sportDate) body.sportDate = fields.sportDate;
            if (fields.sportTypeRank) body.sportTypeRank = fields.sportTypeRank;
            if (fields.sportTypeRankDocument) body.sportTypeRankDocument = fields.sportTypeRankDocument;
            return body;
        },
        ported: true
    },
    {
        id: 2,
        category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
        name: "Barcha yozuvlarni olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba sport yutuqlari - barcha yozuvlarni olish",
        storeFirstId: "administrativeStudentSportEntityId",
        inputFields: {
            limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
            offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
            returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false }
        },
        ported: true
    },
    {
        id: 3,
        category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
        name: "ID bo'yicha olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba sport yutuqlari - ID bo'yicha olish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudentSportEntityId" }
        },
        ported: true
    },
    {
        id: 4,
        category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
        name: "Yozuvni yangilash (PUT)",
        method: "PUT",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba sport yutuqlari - yozuvni yangilash",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudentSportEntityId" },
            sportTypeRank: { label: "Sport razryadi", type: "text", required: false, placeholder: "1/2/3", defaultNew: "2", defaultOld: "2" },
            sportTypeRankDocument: { label: "Hujjat raqami", type: "text", required: false, placeholder: "AB123456", defaultNew: "CD789012", defaultOld: "CD789012" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.sportTypeRank) body.sportTypeRank = fields.sportTypeRank;
            if (fields.sportTypeRankDocument) body.sportTypeRankDocument = fields.sportTypeRankDocument;
            return body;
        },
        ported: true
    },
    {
        id: 5,
        category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
        name: "Yozuvni o'chirish (DELETE)",
        method: "DELETE",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba sport yutuqlari - yozuvni o'chirish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudentSportEntityId" }
        },
        ported: true
    },
    {
        id: 6,
        category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
        name: "Qidirish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba sport yutuqlari - qidirish (GET)",
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
        category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
        name: "Qidirish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba sport yutuqlari - qidirish (POST)",
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
    module.exports = endpoints_44;
}
