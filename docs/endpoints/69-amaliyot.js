// 69.Amaliyot endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_69 = [
    // ============================================
    // 69.Amaliyot (1 endpoint)
    // ============================================
    {
        id: 1,
        category: "69.Amaliyot",
        name: "Amaliyot yaratish",
        method: "POST",
        url: "/entities/hemishe_EStudentPractice",
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
            studentId: {
                label: "Talaba ID",
                type: "text",
                defaultNew: "11111111-1111-1111-1111-111111111111",
                defaultOld: "11111111-1111-1111-1111-111111111111",
                required: true
            },
            practiceType: {
                label: "Amaliyot turi (code)",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: true
            },
            startDate: {
                label: "Boshlanish sanasi",
                type: "text",
                defaultNew: "2025-01-15",
                defaultOld: "2025-01-15",
                required: true
            },
            endDate: {
                label: "Tugash sanasi",
                type: "text",
                defaultNew: "2025-02-15",
                defaultOld: "2025-02-15",
                required: true
            },
            organization: {
                label: "Tashkilot nomi",
                type: "text",
                defaultNew: "Test Tashkilot",
                defaultOld: "Test Tashkilot",
                required: true
            }
        },
        urlBuilder: function(fields) {
            return this.url;
        },
        bodyBuilder: function(fields) {
            return JSON.stringify({
                university: { code: fields.universityCode },
                student: { id: fields.studentId },
                practiceType: { code: fields.practiceType },
                startDate: fields.startDate,
                endDate: fields.endDate,
                organization: fields.organization,
                active: true
            }, null, 2);
        },
        description: `**Amaliyot yaratish**

<b>Endpoint:</b> POST /entities/hemishe_EStudentPractice

<b>Eslatma:</b> Bu endpoint old_hemis.json da topilmagan.
Ammo OLD-HEMIS tizimida mavjud bo'lgan pattern asosida qo'shildi.

<b>So'rov formati:</b>
<pre>
{
    "university": { "code": "999" },
    "student": { "id": "uuid" },
    "practiceType": { "code": "1" },
    "startDate": "2025-01-15",
    "endDate": "2025-02-15",
    "organization": "Test Tashkilot",
    "active": true
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "_entityName": "hemishe_EStudentPractice",
    "_instanceName": "...",
    "id": "generated-uuid"
}
</pre>`,
        ported: false
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_69;
}
