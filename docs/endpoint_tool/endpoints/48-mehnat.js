// 48.Mehnat endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_48 = [
    // ============================================
    // 48.Mehnat (1 endpoint)
    // OLD-HEMIS FORMAT BILAN 100% MOSLIK!
    // ============================================
    {
        id: 1,
        category: "48.Mehnat",
        name: "Mehnat daftarchasi ma'lumotlari",
        method: "GET",
        url: "/services/employment/workbook",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "31304931230067",
                defaultOld: "31304931230067",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Mehnat daftarchasi ma'lumotlari**

<b>Endpoint:</b> GET /services/employment/workbook

<b>Parametrlar:</b>
- pinfl: Fuqaroning PINFL raqami (14 raqamli)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "_entityName": "hemishe_Workbook",
    "id": "",
    "result": "1",
    "comments": "Ok",
    "data": {
        "_entityName": "hemishe_Data",
        "id": "",
        "jobs": [
            {
                "_entityName": "hemishe_EmployeeJob",
                "id": "",
                "orgName": "TOSHKENT DAVLAT UNIVERSITETI",
                "orgInn": "201678867",
                "positionName": "Bosh buhgalter",
                "dateBegin": "2005-08-17",
                "dateEnd": "",
                ...
            }
        ]
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_48;
}
