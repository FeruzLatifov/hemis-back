// 55.DTM endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_55 = [
    // ============================================
    // 55.DTM Mandat (1 endpoint)
    // ============================================
    {
        id: 1,
        category: "55.DTM",
        name: "DTM Mandat ma'lumotlari",
        method: "GET",
        url: "/services/mandat/get",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "42704911920029",
                required: true
            },
            passport: {
                label: "Passport",
                type: "text",
                defaultNew: "AB1234567",
                defaultOld: "AB4454415",
                required: true
            },
            year: {
                label: "Yil",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2022",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            if (fields.passport) params.push("passport=" + encodeURIComponent(fields.passport));
            if (fields.year) params.push("year=" + encodeURIComponent(fields.year));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**DTM Mandat ma'lumotlari**

<b>Endpoint:</b> GET /services/mandat/get

<b>Parametrlar:</b>
- pinfl: Abituriyentning PINFL raqami
- passport: Passport seriya va raqami
- year: Imtihon yili

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "status": 200,
        "entrantId": 1400811,
        "fullName": "SATTOROVA Z. F.",
        "result": 33.4,
        "ustuv": 1,
        "tumanName": "Bandixon tumani",
        "regionName": "Surxondaryo viloyati",
        "nBall1": 1.1,
        "nBall2": 1.1,
        "s1Name": "Ona tili",
        "s2Name": "Matematika",
        ...
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_55;
}
