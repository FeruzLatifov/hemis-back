// 56.OAK endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_56 = [
    // ============================================
    // 56.OAK - Oliy Attestatsiya Komissiyasi (1 endpoint)
    // ============================================
    {
        id: 1,
        category: "56.OAK",
        name: "OAK ma'lumotlari (PINFL bo'yicha)",
        method: "GET",
        url: "/services/oak/byPin",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "32707860270013",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**OAK ma'lumotlari (PINFL bo'yicha)**

<b>Endpoint:</b> GET /services/oak/byPin

<b>Parametrlar:</b>
- pinfl: Xodimning PINFL raqami

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "id": 123456,
        "jsonrpc": "2.0",
        "result": {
            "message": "Citizen retrieved successfully",
            "result": [
                {
                    "birth_date": "1986-07-27",
                    "f_name": "САНЖАР",
                    "gender": 1,
                    "m_name": "ИЗЗАТУЛЛАЕВИЧ",
                    "passport": "AB0916887",
                    "pin": "32707860270013",
                    "s_name": "ХИКМАТУЛЛАЕВ",
                    "title_details": {
                        "title": "Доцент",
                        "diploma_number": "01№013365",
                        "confirmed_date": "22.11.1992"
                    }
                }
            ],
            "success": true
        }
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_56;
}
