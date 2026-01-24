// 54.Transkript endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_54 = [
    // ============================================
    // 54.Transkript (1 endpoint)
    // ============================================
    {
        id: 1,
        category: "54.Transkript",
        name: "Transkript ariza berish",
        method: "GET",
        url: "/services/transcript/get",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "999211100039",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Transkript ariza berish**

<b>Endpoint:</b> GET /services/transcript/get

<b>Parametrlar:</b>
- pinfl: Talabaning PINFL raqami (14 raqamli)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "999211100039",
        "message": "Transcript data"
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_54;
}
