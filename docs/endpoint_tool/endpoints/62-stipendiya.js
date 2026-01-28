// 62.Stipendiya endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_62 = [
    // ============================================
    // 62.Stipendiya (3 endpoint)
    // ============================================
    {
        id: 1,
        category: "62.Stipendiya",
        name: "Stipendiya yaratish (EStudentScholarshipFull)",
        method: "POST",
        url: "/entities/hemishe_EStudentScholarshipFull",
        requiresAuth: true,
        hasBody: true,
        inputFields: {
            scholarshipCode: {
                label: "Stipendiya kodi",
                type: "text",
                defaultNew: "STI-2024-001",
                defaultOld: "STI-2024-001",
                required: true
            },
            studentId: {
                label: "Talaba ID",
                type: "text",
                defaultNew: "11111111-1111-1111-1111-111111111111",
                defaultOld: "11111111-1111-1111-1111-111111111111",
                required: true
            },
            semester: {
                label: "Semestr",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: false
            },
            amount: {
                label: "Summa",
                type: "text",
                defaultNew: "500000",
                defaultOld: "500000",
                required: false
            }
        },
        urlBuilder: function(fields) {
            return this.url;
        },
        bodyBuilder: function(fields) {
            return JSON.stringify({
                scholarshipCode: fields.scholarshipCode || "STI-2024-001",
                _student: fields.studentId ? { id: fields.studentId } : null,
                semester: parseInt(fields.semester) || 1,
                amount: parseFloat(fields.amount) || 500000
            }, null, 2);
        },
        description: `**Stipendiya yaratish (EStudentScholarshipFull)**

<b>Endpoint:</b> POST /entities/hemishe_EStudentScholarshipFull

<b>So'rov formati:</b>
<pre>
{
    "scholarshipCode": "STI-2024-001",
    "_student": { "id": "..." },
    "semester": 1,
    "amount": 500000
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "_entityName": "hemishe_EStudentScholarshipFull",
    "_instanceName": "STI-2024-001",
    "id": "generated-uuid"
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "62.Stipendiya",
        name: "Stipendiya oylik tulov (EStudentScholarshipAmount)",
        method: "POST",
        url: "/entities/hemishe_EStudentScholarshipAmount",
        requiresAuth: true,
        hasBody: true,
        inputFields: {
            scholarshipId: {
                label: "Stipendiya ID",
                type: "text",
                defaultNew: "11111111-1111-1111-1111-111111111111",
                defaultOld: "7ad1eb02-fec1-1da3-2288-fc9a74371618",
                required: true
            },
            month: {
                label: "Oy",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: true
            },
            amount: {
                label: "Summa",
                type: "text",
                defaultNew: "500000",
                defaultOld: "500000",
                required: true
            }
        },
        urlBuilder: function(fields) {
            return this.url;
        },
        bodyBuilder: function(fields) {
            return JSON.stringify({
                _scholarship: { id: fields.scholarshipId },
                month: parseInt(fields.month) || 1,
                amount: parseFloat(fields.amount) || 500000
            }, null, 2);
        },
        description: `**Stipendiya oylik tulov (EStudentScholarshipAmount)**

<b>Endpoint:</b> POST /entities/hemishe_EStudentScholarshipAmount

<b>So'rov formati:</b>
<pre>
{
    "_scholarship": { "id": "scholarship-uuid" },
    "month": 1,
    "amount": 500000
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "_entityName": "hemishe_EStudentScholarshipAmount",
    "_instanceName": "...",
    "id": "generated-uuid"
}
</pre>`,
        ported: true
    },
    {
        id: 3,
        category: "62.Stipendiya",
        name: "Stipendiya summalarini o'chirish",
        method: "GET",
        url: "/services/scholarship/deleteAmounts",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            scholarshipId: {
                label: "Stipendiya ID (UUID)",
                type: "text",
                defaultNew: "11111111-1111-1111-1111-111111111111",
                defaultOld: "7ad1eb02-fec1-1da3-2288-fc9a74371618",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.scholarshipId) params.push("scholarshipId=" + encodeURIComponent(fields.scholarshipId));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Stipendiya summalarini o'chirish**

<b>Endpoint:</b> GET /services/scholarship/deleteAmounts

<b>Parametrlar:</b>
- scholarshipId: Stipendiya UUID (required)

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "scholarshipId": "7ad1eb02-fec1-1da3-2288-fc9a74371618",
        "deleted": true,
        "message": "Scholarship amounts deleted"
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_62;
}
