// 61.Ijtimoiy himoya endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_61 = [
    // ============================================
    // 61.Ijtimoiy himoya (5 endpoint)
    // ============================================
    {
        id: 1,
        category: "61.Ijtimoiy himoya",
        name: "Yagona ijtimoiy reestr",
        method: "GET",
        url: "/services/social/singleRegister",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "41108842860015",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Yagona ijtimoiy reestr**

<b>Endpoint:</b> GET /services/social/singleRegister

<b>Parametrlar:</b>
- pinfl: Fuqaroning PINFL raqami

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "41108842860015",
        "registered": false,
        "message": "Social integration - implementation pending"
    }
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "61.Ijtimoiy himoya",
        name: "Temir daftar (to'liq)",
        method: "GET",
        url: "/services/social/daftarFull",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "41108842860015",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Temir daftar (to'liq ma'lumot)**

<b>Endpoint:</b> GET /services/social/daftarFull

<b>Parametrlar:</b>
- pinfl: Fuqaroning PINFL raqami

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "41108842860015",
        "daftar": {},
        "message": "Social integration - implementation pending"
    }
}
</pre>`,
        ported: true
    },
    {
        id: 3,
        category: "61.Ijtimoiy himoya",
        name: "Temir daftar (qisqa)",
        method: "GET",
        url: "/services/social/daftarShort",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "41108842860015",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Temir daftar (qisqa ma'lumot)**

<b>Endpoint:</b> GET /services/social/daftarShort

<b>Parametrlar:</b>
- pinfl: Fuqaroning PINFL raqami

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "41108842860015",
        "daftar": {},
        "message": "Social integration - implementation pending"
    }
}
</pre>`,
        ported: true
    },
    {
        id: 4,
        category: "61.Ijtimoiy himoya",
        name: "Ayollar daftari",
        method: "GET",
        url: "/services/social/women",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "42601793500108",
                required: true
            },
            sn: {
                label: "Seriya va raqam",
                type: "text",
                defaultNew: "AA1234567",
                defaultOld: "KA0773072",
                required: false
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            if (fields.sn) params.push("sn=" + encodeURIComponent(fields.sn));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Ayollar daftari**

<b>Endpoint:</b> GET /services/social/women

<b>Parametrlar:</b>
- pinfl: Fuqaroning PINFL raqami
- sn: Hujjat seriyasi va raqami (ixtiyoriy)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "42601793500108",
        "sn": "KA0773072",
        "support": [],
        "message": "Social integration - implementation pending"
    }
}
</pre>`,
        ported: true
    },
    {
        id: 5,
        category: "61.Ijtimoiy himoya",
        name: "Yoshlar daftari",
        method: "GET",
        url: "/services/social/young",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "42601793500108",
                required: true
            },
            seria: {
                label: "Seriya",
                type: "text",
                defaultNew: "AA",
                defaultOld: "KA",
                required: true
            },
            number: {
                label: "Raqam",
                type: "text",
                defaultNew: "1234567",
                defaultOld: "0773072",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            if (fields.seria) params.push("seria=" + encodeURIComponent(fields.seria));
            if (fields.number) params.push("number=" + encodeURIComponent(fields.number));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Yoshlar daftari**

<b>Endpoint:</b> GET /services/social/young

<b>Parametrlar:</b>
- pinfl: Fuqaroning PINFL raqami
- seria: Hujjat seriyasi (masalan: KA)
- number: Hujjat raqami (masalan: 0773072)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "42601793500108",
        "seria": "KA",
        "number": "0773072",
        "support": [],
        "message": "Social integration - implementation pending"
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_61;
}
