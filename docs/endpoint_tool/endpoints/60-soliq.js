// 60.Soliq endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_60 = [
    // ============================================
    // 60.Soliq (1 endpoint)
    // ============================================
    {
        id: 1,
        category: "60.Soliq",
        name: "Ijara shartnomasi",
        method: "GET",
        url: "/services/tax/rent",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "51805035330018",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Ijara shartnomasi (Soliq ma'lumotlari)**

<b>Endpoint:</b> GET /services/tax/rent

<b>Parametrlar:</b>
- pinfl: Fuqaroning PINFL raqami (14 raqamli)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "51805035330018",
        "rentContracts": [],
        "message": "Tax rent data"
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_60;
}
