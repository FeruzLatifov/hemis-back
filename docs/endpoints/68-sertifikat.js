// 68.Sertifikat endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_68 = [
    // ============================================
    // 68.Sertifikat (2 endpoint)
    // ============================================
    {
        id: 1,
        category: "68.Sertifikat",
        name: "Talaba sertifikati yaratish",
        method: "POST",
        url: "/entities/hemishe_EStudentCertificate",
        requiresAuth: true,
        hasBody: true,
        inputFields: {
            universityId: {
                label: "Universitet ID",
                type: "text",
                defaultNew: "11111111-1111-1111-1111-111111111111",
                defaultOld: "11111111-1111-1111-1111-111111111111",
                required: true
            },
            studentId: {
                label: "Talaba ID",
                type: "text",
                defaultNew: "22222222-2222-2222-2222-222222222222",
                defaultOld: "22222222-2222-2222-2222-222222222222",
                required: true
            },
            certificateType: {
                label: "Sertifikat turi (code)",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: true
            },
            serialNumber: {
                label: "Seriya raqami",
                type: "text",
                defaultNew: "AA1234567",
                defaultOld: "AA1234567",
                required: true
            },
            issueDate: {
                label: "Berilgan sana",
                type: "text",
                defaultNew: "2025-01-01",
                defaultOld: "2025-01-01",
                required: true
            },
            validDate: {
                label: "Amal qilish sanasi",
                type: "text",
                defaultNew: "2026-01-01",
                defaultOld: "2026-01-01",
                required: true
            }
        },
        urlBuilder: function(fields) {
            return this.url;
        },
        bodyBuilder: function(fields) {
            return JSON.stringify({
                university: fields.universityId,
                student: fields.studentId,
                certificateType: fields.certificateType,
                serialNumber: fields.serialNumber,
                issueDate: fields.issueDate,
                validDate: fields.validDate,
                active: true
            }, null, 2);
        },
        description: `**Talaba sertifikati yaratish**

<b>Endpoint:</b> POST /entities/hemishe_EStudentCertificate

<b>So'rov formati:</b>
<pre>
{
    "university": "uuid",
    "student": "uuid",
    "certificateType": "uuid",
    "serialNumber": "AA1234567",
    "issueDate": "2025-01-01",
    "validDate": "2026-01-01",
    "active": true
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "id": "generated-uuid",
    "_entityName": "hemishe_EStudentCertificate",
    "_instanceName": "AA1234567",
    ...
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "68.Sertifikat",
        name: "Xodim sertifikati yaratish",
        method: "POST",
        url: "/entities/hemishe_EEmpoyeeCertificate",
        requiresAuth: true,
        hasBody: true,
        inputFields: {
            universityCode: {
                label: "Universitet kodi",
                type: "text",
                defaultNew: "999",
                defaultOld: "999",
                required: true
            },
            employeeId: {
                label: "Xodim ID",
                type: "text",
                defaultNew: "f41a4336-a4f5-154d-cc28-eff76884fa99",
                defaultOld: "f41a4336-a4f5-154d-cc28-eff76884fa99",
                required: true
            },
            certificateTypeCode: {
                label: "Sertifikat turi (code)",
                type: "text",
                defaultNew: "2",
                defaultOld: "2",
                required: true
            },
            certificateNameCode: {
                label: "Sertifikat nomi (code)",
                type: "text",
                defaultNew: "30",
                defaultOld: "30",
                required: true
            },
            certificateGradeCode: {
                label: "Sertifikat darajasi (code)",
                type: "text",
                defaultNew: "2",
                defaultOld: "2",
                required: true
            },
            serialNumber: {
                label: "Seriya raqami",
                type: "text",
                defaultNew: "AA1112244",
                defaultOld: "AA1112244",
                required: true
            },
            issueDate: {
                label: "Berilgan sana",
                type: "text",
                defaultNew: "2025-03-05",
                defaultOld: "2025-03-05",
                required: true
            },
            validDate: {
                label: "Amal qilish sanasi",
                type: "text",
                defaultNew: "2026-03-11",
                defaultOld: "2026-03-11",
                required: true
            }
        },
        urlBuilder: function(fields) {
            return this.url;
        },
        bodyBuilder: function(fields) {
            // OLD-HEMIS format: ARRAY of objects with nested code/id references
            return JSON.stringify([{
                university: { code: fields.universityCode },
                employee: { id: fields.employeeId },
                certificateType: { code: fields.certificateTypeCode },
                certificateName: { code: fields.certificateNameCode },
                certificateGrade: { code: fields.certificateGradeCode },
                certificateSubject: { code: "1" },
                issueDate: fields.issueDate,
                validDate: fields.validDate,
                serialNumber: fields.serialNumber,
                active: true
            }], null, 2);
        },
        description: `**Xodim sertifikati yaratish**

<b>Endpoint:</b> POST /entities/hemishe_EEmpoyeeCertificate

<b>Eslatma:</b> Endpoint nomida "EEmpoyeeCertificate" yozuvi OLD-HEMIS formatiga moslik uchun saqlab qolindi (typo)

<b>So'rov formati (ARRAY!):</b>
<pre>
[
    {
        "university": { "code": "999" },
        "employee": { "id": "f41a4336-a4f5-154d-cc28-eff76884fa99" },
        "certificateType": { "code": "2" },
        "certificateName": { "code": "30" },
        "certificateGrade": { "code": "2" },
        "certificateSubject": { "code": "1" },
        "issueDate": "2025-03-05",
        "validDate": "2026-03-11",
        "serialNumber": "AA1112244",
        "active": true
    }
]
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
[
    {
        "_entityName": "hemishe_EEmpoyeeCertificate",
        "_instanceName": "AA1112244",
        "id": "generated-uuid"
    }
]
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_68;
}
