// 64.OTM endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_64 = [
    // ============================================
    // 64.OTM (3 endpoint)
    // ============================================
    {
        id: 1,
        category: "64.OTM",
        name: "Talaba ma'lumotlari (ID bo'yicha)",
        method: "GET",
        url: "/services/otm/studentInfoById",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            studentId: {
                label: "Talaba ID (string format)",
                type: "text",
                defaultNew: "999221100044",
                defaultOld: "999221100044",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.studentId) params.push("studentId=" + encodeURIComponent(fields.studentId));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Talaba ma'lumotlari (ID bo'yicha)**

<b>Endpoint:</b> GET /services/otm/studentInfoById

<b>Parametrlar:</b>
- studentId: Talaba ID (string format, masalan: 999221100044)

<b>Eslatma:</b> OLD-HEMIS da studentId UUID emas, string formatda (999221100044)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "studentId": "999221100044",
        "fullName": "...",
        "faculty": "...",
        "speciality": "...",
        "course": 1
    }
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "64.OTM",
        name: "Tutor talabalari ro'yxati",
        method: "GET",
        url: "/services/otm/studentListByTutor",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            university: {
                label: "Universitet kodi",
                type: "text",
                defaultNew: "999",
                defaultOld: "999",
                required: true
            },
            tutorPinfl: {
                label: "Tutor PINFL",
                type: "text",
                defaultNew: "31503776560016",
                defaultOld: "31503776560016",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.university) params.push("university=" + encodeURIComponent(fields.university));
            if (fields.tutorPinfl) params.push("tutorPinfl=" + encodeURIComponent(fields.tutorPinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Tutor talabalari ro'yxati**

<b>Endpoint:</b> GET /services/otm/studentListByTutor

<b>Parametrlar:</b>
- university: Universitet kodi (masalan: 999)
- tutorPinfl: Tutor PINFL raqami

<b>Eslatma:</b> OLD-HEMIS formatida university va tutorPinfl string parameter sifatida yuboriladi

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "university": "999",
        "tutorPinfl": "31503776560016",
        "students": []
    }
}
</pre>`,
        ported: true
    },
    {
        id: 3,
        category: "64.OTM",
        name: "Talaba ma'lumotlari (PINFL bo'yicha)",
        method: "GET",
        url: "/services/otm/studentInfoByPinfl",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "Talaba PINFL",
                type: "text",
                defaultNew: "31503776560016",
                defaultOld: "31503776560016",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Talaba ma'lumotlari (PINFL bo'yicha)**

<b>Endpoint:</b> GET /services/otm/studentInfoByPinfl

<b>Parametrlar:</b>
- pinfl: Talaba PINFL raqami

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "pinfl": "31503776560016",
        "fullName": "...",
        "faculty": "...",
        "speciality": "...",
        "course": 1
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_64;
}
