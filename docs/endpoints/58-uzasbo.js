// 58.UzASBO endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_58 = [
    // ============================================
    // 58.UzASBO (3 endpoint)
    // ============================================
    {
        id: 1,
        category: "58.UzASBO",
        name: "Stipendiya ma'lumotlarini olish",
        method: "GET",
        url: "/services/uzasbo/scholarship",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            inn: {
                label: "INN",
                type: "text",
                defaultNew: "123456789",
                defaultOld: "201354108",
                required: true
            },
            year: {
                label: "Yil",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2023",
                required: true
            },
            month: {
                label: "Oy",
                type: "text",
                defaultNew: "1",
                defaultOld: "9",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.inn) params.push("inn=" + encodeURIComponent(fields.inn));
            if (fields.year) params.push("year=" + encodeURIComponent(fields.year));
            if (fields.month) params.push("month=" + encodeURIComponent(fields.month));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**UzASBO Stipendiya ma'lumotlarini olish**

<b>Endpoint:</b> GET /services/uzasbo/scholarship

<b>Parametrlar:</b>
- inn: Tashkilot INN raqami
- year: Yil
- month: Oy (1-12)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "inn": "201354108",
        "year": 2023,
        "month": 9,
        "scholarships": [],
        "message": "UzASBO scholarship data"
    }
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "58.UzASBO",
        name: "Stipendiya tekshirish",
        method: "POST",
        url: "/services/student/checkScholarship2",
        requiresAuth: true,
        hasBody: true,
        inputFields: {
            tin: {
                label: "TIN",
                type: "text",
                defaultNew: "123456789",
                defaultOld: "207095330",
                required: true
            },
            docOn: {
                label: "Hujjat sanasi",
                type: "text",
                defaultNew: "2024-01-15",
                defaultOld: "2024-02-05",
                required: true
            },
            pinfl1: {
                label: "Talaba 1 PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "60209047160010",
                required: true
            },
            sum1: {
                label: "Talaba 1 summa",
                type: "text",
                defaultNew: "100000",
                defaultOld: "103576",
                required: false
            }
        },
        bodyGenerator: function(fields) {
            return {
                "tin": fields.tin,
                "docOn": fields.docOn,
                "students": [
                    {
                        "pinfl": fields.pinfl1,
                        "sum": fields.sum1 || ""
                    }
                ]
            };
        },
        description: `**Stipendiya tekshirish (Scholarship check)**

<b>Endpoint:</b> POST /services/student/checkScholarship2

<b>OLD-HEMIS Request formati:</b>
<pre>
{
    "tin": "207095330",
    "docOn": "2024-02-05",
    "students": [
        {"pinfl": "60209047160010", "sum": "103576"},
        {"pinfl": "52712015360046"},
        {"pinfl": "527120153600461", "sum": "103576"}
    ]
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "tin": "207095330",
        "docOn": "2024-02-05",
        "results": [
            {"pinfl": "60209047160010", "status": "OK"},
            {"pinfl": "52712015360046", "status": "OK"},
            {"pinfl": "527120153600461", "status": "INVALID_PINFL"}
        ]
    }
}
</pre>

<b>Izoh:</b> PINFL 14 raqamli bo'lishi kerak, aks holda INVALID_PINFL qaytariladi.`,
        ported: true
    },
    {
        id: 3,
        category: "58.UzASBO",
        name: "Type test",
        method: "GET",
        url: "/services/test/typetest",
        requiresAuth: true,
        hasBody: false,
        inputFields: {},
        urlBuilder: function(fields) {
            return this.url;
        },
        description: `**Type test endpoint**

<b>Endpoint:</b> GET /services/test/typetest

<b>Parametrlar:</b> Yo'q

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "type": "test",
        "message": "Type test endpoint"
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_58;
}
