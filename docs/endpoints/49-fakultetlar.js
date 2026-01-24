// 49.Fakultetlar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_49 = [
    // ============================================
    // 49.Fakultetlar (1 endpoint)
    // OLD-HEMIS FORMAT BILAN 100% MOSLIK!
    // ============================================
    {
        id: 1,
        category: "49.Fakultetlar",
        name: "OTM fakultetlarini olish",
        method: "GET",
        url: "/services/faculty/get",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            university: {
                label: "OTM kodi",
                type: "text",
                defaultNew: "401",
                defaultOld: "301",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.university) params.push("university=" + encodeURIComponent(fields.university));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**OTM fakultetlarini olish**

<b>Endpoint:</b> GET /services/faculty/get

<b>Parametrlar:</b>
- university: OTM kodi (masalan: 301, 401)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": [
        {
            "_entityName": "hemishe_EUniversityDepartment",
            "id": "301-101",
            "code": "301-101",
            "version": 3,
            "nameUz": "Axborot texnologiyalari",
            "nameRu": "Информационные технологии"
        }
    ]
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_49;
}
