// 51.Guruhlar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_51 = [
    // ============================================
    // 51.Guruhlar (2 endpoint)
    // ============================================
    {
        id: 1,
        category: "51.Guruhlar",
        name: "Guruhlar ro'yxatini olish",
        method: "GET",
        url: "/services/group/get",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            university: {
                label: "OTM kodi",
                type: "text",
                defaultNew: "401",
                defaultOld: "999",
                required: true
            },
            type: {
                label: "Ta'lim turi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: false
            },
            year: {
                label: "O'quv yili",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2021",
                required: false
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.university) params.push("university=" + encodeURIComponent(fields.university));
            if (fields.type) params.push("type=" + encodeURIComponent(fields.type));
            if (fields.year) params.push("year=" + encodeURIComponent(fields.year));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Guruhlar ro'yxatini olish**

<b>Endpoint:</b> GET /services/group/get

<b>Parametrlar:</b>
- university: OTM kodi (masalan: 999, 401)
- type: Ta'lim turi kodi (11=Bakalavr, ixtiyoriy)
- year: O'quv yili (masalan: 2021, 2024)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": [
        {
            "_entityName": "hemishe_EUniversityGroup",
            "id": "88d77617-eb47-a99c-d46b-5dcaa57347cc",
            "groupId": "1",
            "active": true,
            "version": 1,
            "groupName": "125-21"
        }
    ]
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "51.Guruhlar",
        name: "Guruhlarni yuborish",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_EUniversityGroup",
        requiresAuth: true,
        hasBody: true,
        inputFields: {
            universityCode: {
                label: "OTM kodi",
                type: "text",
                defaultNew: "401",
                defaultOld: "999",
                required: true
            },
            educationTypeCode: {
                label: "Ta'lim turi kodi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: true
            },
            educationYearCode: {
                label: "O'quv yili",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2021",
                required: true
            },
            groupId: {
                label: "Guruh ID",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: true
            },
            groupName: {
                label: "Guruh nomi",
                type: "text",
                defaultNew: "200-24",
                defaultOld: "200-21",
                required: true
            }
        },
        // OLD-HEMIS: ARRAY formatida yuboradi!
        bodyGenerator: function(fields) {
            return [
                {
                    "university": {"code": fields.universityCode},
                    "educationType": {"code": fields.educationTypeCode},
                    "educationYear": {"code": fields.educationYearCode},
                    "groupId": fields.groupId,
                    "groupName": fields.groupName
                }
            ];
        },
        description: `**Guruhlarni yuborish**

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EUniversityGroup

<b>OLD-HEMIS Request formati (ARRAY!):</b>
<pre>
[
    {
        "university": {"code": "999"},
        "educationType": {"code": "11"},
        "educationYear": {"code": "2021"},
        "groupId": "1",
        "groupName": "200-21"
    }
]
</pre>

<b>OLD-HEMIS Response formati (201 Created):</b>
<pre>
[
    {
        "_entityName": "hemishe_EUniversityGroup",
        "_instanceName": "200-21",
        "id": "dca5bd9f-3ff5-7957-bfaa-ed61fb62b2ce"
    }
]
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_51;
}
