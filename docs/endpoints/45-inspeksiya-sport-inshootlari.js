// 45.Inspeksiya administrative SportFacilities endpoints
// Sport inshootlari - Sport inshootlari maydoni haqida ma'lumot
// hemishe_RIAdministrativeSportFacilities

const endpoints_45 = [
    // ============================================
    // 45.Inspeksiya administrative SportFacilities (Sport inshootlari) - 7 endpoint
    // ============================================
    {
        id: 1,
        category: "45.Inspeksiya Sport inshootlari",
        name: "Yangi yozuv yaratish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities",
        requiresAuth: true,
        dependsOn: 1,
        description: "Sport inshootlari - yangi yozuv yaratish",
        storeResultId: "administrativeSportFacilitiesEntityId",
        inputFields: {
            university: { label: "Universitet kodi", type: "text", required: false, placeholder: "999", defaultNew: "999", defaultOld: "999" },
            educationYear: { label: "O'quv yili kodi", type: "text", required: false, placeholder: "2021", defaultNew: "2021", defaultOld: "2021" },
            square: { label: "Maydon (kv.m)", type: "number", required: false, placeholder: "1500.5", defaultNew: "1500.5", defaultOld: "1500.5" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.university) body.university = { id: fields.university };
            if (fields.educationYear) body.educationYear = { id: fields.educationYear };
            if (fields.square) body.square = parseFloat(fields.square);
            return body;
        },
        ported: true
    },
    {
        id: 2,
        category: "45.Inspeksiya Sport inshootlari",
        name: "Barcha yozuvlarni olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities",
        requiresAuth: true,
        dependsOn: 1,
        description: "Sport inshootlari - barcha yozuvlarni olish",
        storeFirstId: "administrativeSportFacilitiesEntityId",
        inputFields: {
            limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
            offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
            returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false },
            view: { label: "View", type: "text", required: false, placeholder: "rIAdministrativeSportFacilities-view", defaultNew: "rIAdministrativeSportFacilities-view", defaultOld: "rIAdministrativeSportFacilities-view" }
        },
        ported: true
    },
    {
        id: 3,
        category: "45.Inspeksiya Sport inshootlari",
        name: "ID bo'yicha olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Sport inshootlari - ID bo'yicha olish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeSportFacilitiesEntityId" }
        },
        ported: true
    },
    {
        id: 4,
        category: "45.Inspeksiya Sport inshootlari",
        name: "Yozuvni yangilash (PUT)",
        method: "PUT",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Sport inshootlari - yozuvni yangilash",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeSportFacilitiesEntityId" },
            square: { label: "Maydon (kv.m)", type: "number", required: false, placeholder: "2000.0", defaultNew: "2500.0", defaultOld: "2500.0" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.square) body.square = parseFloat(fields.square);
            return body;
        },
        ported: true
    },
    {
        id: 5,
        category: "45.Inspeksiya Sport inshootlari",
        name: "Yozuvni o'chirish (DELETE)",
        method: "DELETE",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Sport inshootlari - yozuvni o'chirish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeSportFacilitiesEntityId" }
        },
        ported: true
    },
    {
        id: 6,
        category: "45.Inspeksiya Sport inshootlari",
        name: "Qidirish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Sport inshootlari - qidirish (GET)",
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
        category: "45.Inspeksiya Sport inshootlari",
        name: "Qidirish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Sport inshootlari - qidirish (POST)",
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
    module.exports = endpoints_45;
}
