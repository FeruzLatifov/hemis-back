// 43.Inspeksiya administrative student4 endpoints
// Talaba olimpiadalari - Халқаро олимпиадалар ва мусобақаларда мукофотга сазовор бўлган талабалар
// hemishe_RIAdministrativeStudent4

const endpoints_43 = [
    // ============================================
    // 43.Inspeksiya administrative student4 (Talaba olimpiadalari) - 7 endpoint
    // ============================================
    {
        id: 1,
        category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
        name: "Yangi yozuv yaratish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba olimpiadalari - yangi yozuv yaratish",
        storeResultId: "administrativeStudent4EntityId",
        inputFields: {
            university: { label: "Universitet ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
            educationYear: { label: "O'quv yili ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
            country: { label: "Davlat ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
            student: { label: "Talaba ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
            olimpiadaType: { label: "Olimpiada turi", type: "text", required: false, placeholder: "international/republican", defaultNew: "international", defaultOld: "international" },
            olimpiadaName: { label: "Olimpiada nomi", type: "text", required: false, placeholder: "Olimpiada nomi", defaultNew: "Xalqaro Matematika Olimpiadasi", defaultOld: "Xalqaro Matematika Olimpiadasi" },
            olimpiadaSectionName: { label: "Bo'lim nomi", type: "text", required: false, placeholder: "Bo'lim", defaultNew: "Matematika", defaultOld: "Matematika" },
            olimpiadaPlace: { label: "O'tkazilgan joy", type: "text", required: false, placeholder: "Shahar/Davlat", defaultNew: "Toshkent", defaultOld: "Toshkent" },
            olimpiadaPlaceDate: { label: "Sana", type: "text", required: false, placeholder: "2024 yanvar", defaultNew: "2024 yanvar", defaultOld: "2024 yanvar" },
            olimpiadaSubject: { label: "Fan", type: "text", required: false, placeholder: "Fan nomi", defaultNew: "Matematika", defaultOld: "Matematika" },
            takenPosition: { label: "Olingan o'rin", type: "text", required: false, placeholder: "1/2/3", defaultNew: "1", defaultOld: "1" },
            diplomaSerial: { label: "Diplom seriyasi", type: "text", required: false, placeholder: "AB", defaultNew: "AB", defaultOld: "AB" },
            diplomaNumber: { label: "Diplom raqami", type: "text", required: false, placeholder: "123456", defaultNew: "123456", defaultOld: "123456" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.university) body.university = { id: fields.university };
            if (fields.educationYear) body.educationYear = { id: fields.educationYear };
            if (fields.country) body.country = { id: fields.country };
            if (fields.student) body.student = { id: fields.student };
            if (fields.olimpiadaType) body.olimpiadaType = fields.olimpiadaType;
            if (fields.olimpiadaName) body.olimpiadaName = fields.olimpiadaName;
            if (fields.olimpiadaSectionName) body.olimpiadaSectionName = fields.olimpiadaSectionName;
            if (fields.olimpiadaPlace) body.olimpiadaPlace = fields.olimpiadaPlace;
            if (fields.olimpiadaPlaceDate) body.olimpiadaPlaceDate = fields.olimpiadaPlaceDate;
            if (fields.olimpiadaSubject) body.olimpiadaSubject = fields.olimpiadaSubject;
            if (fields.takenPosition) body.takenPosition = fields.takenPosition;
            if (fields.diplomaSerial) body.diplomaSerial = fields.diplomaSerial;
            if (fields.diplomaNumber) body.diplomaNumber = fields.diplomaNumber;
            return body;
        },
        ported: true
    },
    {
        id: 2,
        category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
        name: "Barcha yozuvlarni olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba olimpiadalari - barcha yozuvlarni olish",
        storeFirstId: "administrativeStudent4EntityId",
        inputFields: {
            limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
            offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
            returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false }
        },
        ported: true
    },
    {
        id: 3,
        category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
        name: "ID bo'yicha olish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba olimpiadalari - ID bo'yicha olish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent4EntityId" }
        },
        ported: true
    },
    {
        id: 4,
        category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
        name: "Yozuvni yangilash (PUT)",
        method: "PUT",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba olimpiadalari - yozuvni yangilash",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent4EntityId" },
            olimpiadaName: { label: "Olimpiada nomi", type: "text", required: false, placeholder: "Olimpiada nomi", defaultNew: "Yangilangan Olimpiada", defaultOld: "Yangilangan Olimpiada" },
            takenPosition: { label: "Olingan o'rin", type: "text", required: false, placeholder: "1/2/3", defaultNew: "2", defaultOld: "2" }
        },
        bodyGenerator: (fields) => {
            const body = {};
            if (fields.olimpiadaName) body.olimpiadaName = fields.olimpiadaName;
            if (fields.takenPosition) body.takenPosition = fields.takenPosition;
            return body;
        },
        ported: true
    },
    {
        id: 5,
        category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
        name: "Yozuvni o'chirish (DELETE)",
        method: "DELETE",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/{entityId}",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba olimpiadalari - yozuvni o'chirish",
        inputFields: {
            entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent4EntityId" }
        },
        ported: true
    },
    {
        id: 6,
        category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
        name: "Qidirish (GET)",
        method: "GET",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba olimpiadalari - qidirish (GET)",
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
        category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
        name: "Qidirish (POST)",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/search",
        requiresAuth: true,
        dependsOn: 1,
        description: "Talaba olimpiadalari - qidirish (POST)",
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
    module.exports = endpoints_43;
}
