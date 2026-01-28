// 63.Billing endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_63 = [
    // ============================================
    // 63.Billing (2 endpoint)
    // ============================================
    {
        id: 1,
        category: "63.Billing",
        name: "Stipendiya to'lovi (UzASBO)",
        method: "POST",
        url: "/services/billing/scholarship",
        requiresAuth: true,
        hasBody: true,
        inputFields: {
            tin: {
                label: "Tashkilot INN",
                type: "text",
                defaultNew: "205771544",
                defaultOld: "205771544",
                required: true
            },
            pinfl1: {
                label: "Talaba 1 PINFL",
                type: "text",
                defaultNew: "62708005690041",
                defaultOld: "62708005690041",
                required: true
            },
            pinfl2: {
                label: "Talaba 2 PINFL",
                type: "text",
                defaultNew: "62708005690043",
                defaultOld: "62708005690043",
                required: false
            },
            pinfl3: {
                label: "Talaba 3 PINFL",
                type: "text",
                defaultNew: "40211905590019",
                defaultOld: "40211905590019",
                required: false
            }
        },
        urlBuilder: function(fields) {
            return this.url;
        },
        bodyBuilder: function(fields) {
            let pinflList = [fields.pinfl1];
            if (fields.pinfl2) pinflList.push(fields.pinfl2);
            if (fields.pinfl3) pinflList.push(fields.pinfl3);
            return JSON.stringify({
                tin: fields.tin,
                pinfl: pinflList
            }, null, 2);
        },
        description: `**Stipendiya to'lovi (UzASBO)**

<b>Endpoint:</b> POST /services/billing/scholarship

<b>So'rov formati:</b>
<pre>
{
    "tin": "205771544",
    "pinfl": [
        "62708005690041",
        "62708005690043",
        "40211905590019"
    ]
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "processedCount": 3,
        "tin": "205771544",
        "students": [],
        "message": "Scholarship query processed"
    }
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "63.Billing",
        name: "Hisob-faktura yaratish",
        method: "POST",
        url: "/services/billing/invoice",
        requiresAuth: true,
        hasBody: true,
        inputFields: {
            OrganizationId: {
                label: "Tashkilot ID",
                type: "text",
                defaultNew: "303",
                defaultOld: "303",
                required: true
            },
            EduFacultyId: {
                label: "Fakultet ID",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false
            },
            EduYearId: {
                label: "O'quv yili ID",
                type: "text",
                defaultNew: "3",
                defaultOld: "3",
                required: true
            },
            EduTypeId: {
                label: "Ta'lim turi ID",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: true
            }
        },
        urlBuilder: function(fields) {
            return this.url;
        },
        bodyBuilder: function(fields) {
            return JSON.stringify({
                params: {
                    OrganizationId: parseInt(fields.OrganizationId) || 303,
                    EduFacultyId: fields.EduFacultyId || "",
                    EduYearId: parseInt(fields.EduYearId) || 3,
                    EduTypeId: fields.EduTypeId || "11"
                }
            }, null, 2);
        },
        description: `**Hisob-faktura yaratish**

<b>Endpoint:</b> POST /services/billing/invoice

<b>So'rov formati:</b>
<pre>
{
    "params": {
        "OrganizationId": 303,
        "EduFacultyId": "",
        "EduYearId": 3,
        "EduTypeId": "11"
    }
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "data": {
        "invoices": [],
        "message": "Invoice query processed",
        "organizationId": 303,
        "eduYearId": 3,
        "eduTypeId": "11"
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_63;
}
