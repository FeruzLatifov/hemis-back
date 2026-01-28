// 57.Contract endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_57 = [
    // ============================================
    // 57.Contract (1 endpoint)
    // ============================================
    {
        id: 1,
        category: "57.Contract",
        name: "Shartnoma ma'lumotlarini olish",
        method: "GET",
        url: "/services/contract/get",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "30503941620012",
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
            if (fields.year) params.push("year=" + encodeURIComponent(fields.year));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Shartnoma ma'lumotlarini olish**

<b>Endpoint:</b> GET /services/contract/get

<b>Parametrlar:</b>
- pinfl: Talabaning PINFL raqami (14 raqamli)
- year: O'quv yili (masalan: 2022, 2024)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "30503941620012",
        "year": 2022,
        "contracts": [],
        "message": "Contract data"
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_57;
}
