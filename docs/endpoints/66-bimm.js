// 66.BIMM endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_66 = [
    // ============================================
    // 66.BIMM (5 endpoint)
    // ============================================
    {
        id: 1,
        category: "66.BIMM",
        name: "Nogironlik holatini tekshirish",
        method: "GET",
        url: "/services/bimm/disabilityCheck",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "12345678901234",
                required: true
            },
            document: {
                label: "Hujjat raqami",
                type: "text",
                defaultNew: "AB1234567",
                defaultOld: "AB1234567",
                required: false
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            if (fields.document) params.push("document=" + encodeURIComponent(fields.document));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Nogironlik holatini tekshirish (BIMM)**

<b>Endpoint:</b> GET /services/bimm/disabilityCheck

<b>Parametrlar:</b>
- pinfl: Fuqaro PINFL raqami (required)
- document: Hujjat raqami (optional)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "12345678901234",
        "document": "AB1234567",
        "hasDisability": false,
        "message": "Stub implementation - parameters returned"
    }
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "66.BIMM",
        name: "Kam ta'minlangan oilalar ro'yxati",
        method: "GET",
        url: "/services/bimm/provertyRegister",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "12345678901234",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Kam ta'minlangan oilalar ro'yxatini tekshirish (BIMM)**

<b>Endpoint:</b> GET /services/bimm/provertyRegister

<b>Parametrlar:</b>
- pinfl: Fuqaro PINFL raqami (required)

<b>Eslatma:</b> Endpoint nomida "proverty" OLD-HEMIS formatiga moslik uchun saqlab qolindi (to'g'ri yozilishi: "poverty")

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "12345678901234",
        "inRegister": false,
        "message": "Stub implementation - parameters returned"
    }
}
</pre>`,
        ported: true
    },
    {
        id: 3,
        category: "66.BIMM",
        name: "Sertifikat ma'lumotlari",
        method: "GET",
        url: "/services/bimm/certificate",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "12345678901234",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Sertifikat ma'lumotlarini olish (BIMM)**

<b>Endpoint:</b> GET /services/bimm/certificate

<b>Parametrlar:</b>
- pinfl: Fuqaro PINFL raqami (required)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "12345678901234",
        "certificates": [],
        "message": "Stub implementation - parameters returned"
    }
}
</pre>`,
        ported: true
    },
    {
        id: 4,
        category: "66.BIMM",
        name: "Ilmiy daraja ma'lumotlari",
        method: "GET",
        url: "/services/bimm/academicDegree",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "12345678901234",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Ilmiy daraja ma'lumotlarini olish (BIMM)**

<b>Endpoint:</b> GET /services/bimm/academicDegree

<b>Parametrlar:</b>
- pinfl: Fuqaro PINFL raqami (required)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "12345678901234",
        "degrees": [],
        "message": "Stub implementation - parameters returned"
    }
}
</pre>`,
        ported: true
    },
    {
        id: 5,
        category: "66.BIMM",
        name: "O'qituvchi malaka oshirish",
        method: "GET",
        url: "/services/bimm/teacherTraining",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "12345678901234",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**O'qituvchi malaka oshirish ma'lumotlari (BIMM)**

<b>Endpoint:</b> GET /services/bimm/teacherTraining

<b>Parametrlar:</b>
- pinfl: O'qituvchi PINFL raqami (required)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "12345678901234",
        "trainings": [],
        "message": "Stub implementation - parameters returned"
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_66;
}
