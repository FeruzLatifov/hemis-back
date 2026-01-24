// 65.Xo'jalik hisobot endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_65 = [
    // ============================================
    // 65.Xo'jalik hisobot (3 endpoint)
    // ============================================
    {
        id: 1,
        category: "65.Xo'jalik hisobot",
        name: "O'quv materiallari darajasi",
        method: "POST",
        url: "/entities/hemishe_REducationMaterials",
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
            educationYear: {
                label: "O'quv yili ID",
                type: "text",
                defaultNew: "2023",
                defaultOld: "2023",
                required: true
            },
            specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                defaultNew: "60310900",
                defaultOld: "60310900",
                required: true
            },
            specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                defaultNew: "Psixologiya (faoliyat turlari bo'yicha)",
                defaultOld: "Psixologiya (faoliyat turlari bo'yicha)",
                required: true
            },
            subjectCount: {
                label: "Fanlar soni",
                type: "text",
                defaultNew: "15",
                defaultOld: "15",
                required: true
            },
            textbooksCount: {
                label: "Darsliklar soni",
                type: "text",
                defaultNew: "5",
                defaultOld: "5",
                required: true
            },
            createdMaterialsGrade: {
                label: "Yaratilgan materiallar darajasi",
                type: "text",
                defaultNew: "4",
                defaultOld: "4",
                required: true
            }
        },
        urlBuilder: function(fields) {
            return this.url;
        },
        bodyBuilder: function(fields) {
            return JSON.stringify({
                university: { code: fields.universityCode },
                educationYear: { id: fields.educationYear },
                specialityId: "11111111-1111-1111-1111-111111111111",
                specialityCode: fields.specialityCode,
                specialityName: fields.specialityName,
                subjectCount: parseInt(fields.subjectCount) || 15,
                textbooksCount: parseInt(fields.textbooksCount) || 5,
                createdMaterialsGrade: parseInt(fields.createdMaterialsGrade) || 4
            }, null, 2);
        },
        description: `**O'quv materiallari darajasi**

<b>Endpoint:</b> POST /entities/hemishe_REducationMaterials

<b>So'rov formati:</b>
<pre>
{
    "university": { "code": "999" },
    "educationYear": { "id": "2023" },
    "specialityId": "uuid",
    "specialityCode": "60310900",
    "specialityName": "Psixologiya (faoliyat turlari bo'yicha)",
    "subjectCount": 15,
    "textbooksCount": 5,
    "createdMaterialsGrade": 4
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "_entityName": "hemishe_REducationMaterials",
    "_instanceName": "com.company.hemishe.entity.REducationMaterials-...",
    "id": "generated-uuid"
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "65.Xo'jalik hisobot",
        name: "Laboratoriyalar bilan ta'minlanganlik",
        method: "POST",
        url: "/entities/hemishe_RLaboratories",
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
            educationYear: {
                label: "O'quv yili ID",
                type: "text",
                defaultNew: "2023",
                defaultOld: "2023",
                required: true
            },
            specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                defaultNew: "60310900",
                defaultOld: "60310900",
                required: true
            },
            labCount: {
                label: "Laboratoriyalar soni",
                type: "text",
                defaultNew: "10",
                defaultOld: "10",
                required: true
            },
            equipmentLevel: {
                label: "Jihozlanganlik darajasi",
                type: "text",
                defaultNew: "4",
                defaultOld: "4",
                required: true
            }
        },
        urlBuilder: function(fields) {
            return this.url;
        },
        bodyBuilder: function(fields) {
            return JSON.stringify({
                university: { code: fields.universityCode },
                educationYear: { id: fields.educationYear },
                specialityCode: fields.specialityCode,
                labCount: parseInt(fields.labCount) || 10,
                equipmentLevel: parseInt(fields.equipmentLevel) || 4
            }, null, 2);
        },
        description: `**Laboratoriyalar bilan ta'minlanganlik**

<b>Endpoint:</b> POST /entities/hemishe_RLaboratories

<b>So'rov formati:</b>
<pre>
{
    "university": { "code": "999" },
    "educationYear": { "id": "2023" },
    "specialityCode": "60310900",
    "labCount": 10,
    "equipmentLevel": 4
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "_entityName": "hemishe_RLaboratories",
    "_instanceName": "com.company.hemishe.entity.RLaboratories-...",
    "id": "generated-uuid"
}
</pre>`,
        ported: true
    },
    {
        id: 3,
        category: "65.Xo'jalik hisobot",
        name: "AKT bilan jihozlanganlik",
        method: "POST",
        url: "/entities/hemishe_RIctEquipment",
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
            educationYear: {
                label: "O'quv yili ID",
                type: "text",
                defaultNew: "2023",
                defaultOld: "2023",
                required: true
            },
            computerCount: {
                label: "Kompyuterlar soni",
                type: "text",
                defaultNew: "100",
                defaultOld: "100",
                required: true
            },
            projectorCount: {
                label: "Proyektorlar soni",
                type: "text",
                defaultNew: "20",
                defaultOld: "20",
                required: true
            },
            internetSpeed: {
                label: "Internet tezligi (Mbps)",
                type: "text",
                defaultNew: "100",
                defaultOld: "100",
                required: true
            }
        },
        urlBuilder: function(fields) {
            return this.url;
        },
        bodyBuilder: function(fields) {
            return JSON.stringify({
                university: { code: fields.universityCode },
                educationYear: { id: fields.educationYear },
                computerCount: parseInt(fields.computerCount) || 100,
                projectorCount: parseInt(fields.projectorCount) || 20,
                internetSpeed: parseInt(fields.internetSpeed) || 100
            }, null, 2);
        },
        description: `**AKT bilan jihozlanganlik**

<b>Endpoint:</b> POST /entities/hemishe_RIctEquipment

<b>So'rov formati:</b>
<pre>
{
    "university": { "code": "999" },
    "educationYear": { "id": "2023" },
    "computerCount": 100,
    "projectorCount": 20,
    "internetSpeed": 100
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "_entityName": "hemishe_RIctEquipment",
    "_instanceName": "com.company.hemishe.entity.RIctEquipment-...",
    "id": "generated-uuid"
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_65;
}
