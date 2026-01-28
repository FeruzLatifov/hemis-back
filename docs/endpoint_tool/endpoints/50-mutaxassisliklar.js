// 50.Mutaxassisliklar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_50 = [
    // ============================================
    // 50.Mutaxassisliklar (2 endpoint)
    // OLD-HEMIS FORMAT BILAN 100% MOSLIK!
    // ============================================
    {
        id: 1,
        category: "50.Mutaxassisliklar",
        name: "OTM mutaxassisliklarini olish",
        method: "GET",
        url: "/services/speciality/get",
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
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.university) params.push("university=" + encodeURIComponent(fields.university));
            if (fields.type) params.push("type=" + encodeURIComponent(fields.type));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**OTM mutaxassisliklarini olish**

<b>Endpoint:</b> GET /services/speciality/get

<b>Parametrlar:</b>
- university: OTM kodi (masalan: 999, 401)
- type: Ta'lim turi kodi (11=Bakalavr, 12=Magistr, ixtiyoriy)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": [
        {
            "_entityName": "hemishe_EUniversitySpeciality",
            "id": "40abbb45-d332-c663-fa0b-70b076098c02",
            "active": true,
            "version": 1,
            "specialityCode": "11111122",
            "specialityName": "test"
        }
    ]
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "50.Mutaxassisliklar",
        name: "OTM mutaxassisliklarini yuborish",
        method: "POST",
        url: "/app/rest/v2/entities/hemishe_EUniversitySpeciality",
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
            facultyCode: {
                label: "Fakultet kodi",
                type: "text",
                defaultNew: "401-102",
                defaultOld: "999-102",
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
                label: "Ta'lim yili",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2021",
                required: true
            },
            specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                defaultNew: "60310900",
                defaultOld: "2223",
                required: true
            },
            specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                defaultNew: "Psixologiya",
                defaultOld: "test2223",
                required: true
            },
            active: {
                label: "Faol",
                type: "text",
                defaultNew: "true",
                defaultOld: "true",
                required: false
            }
        },
        // OLD-HEMIS: ARRAY formatida yuboradi!
        bodyGenerator: function(fields) {
            return [
                {
                    "university": {"code": fields.universityCode},
                    "faculty": {"code": fields.facultyCode},
                    "educationType": {"code": fields.educationTypeCode},
                    "educationYear": {"code": fields.educationYearCode},
                    "specialityCode": fields.specialityCode,
                    "specialityName": fields.specialityName,
                    "active": fields.active === "true"
                }
            ];
        },
        description: `**OTM mutaxassisliklarini yuborish**

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EUniversitySpeciality

<b>OLD-HEMIS Request formati (ARRAY!):</b>
<pre>
[
    {
        "university": {"code": "999"},
        "faculty": {"code": "999-102"},
        "educationType": {"code": "11"},
        "educationYear": {"code": "2021"},
        "specialityCode": "2223",
        "specialityName": "test2223",
        "active": true
    }
]
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
[
    {
        "_entityName": "hemishe_EUniversitySpeciality",
        "_instanceName": "2223 test2223",
        "id": "7f797721-93bf-0518-49eb-49ddf26dd124"
    }
]
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_50;
}
