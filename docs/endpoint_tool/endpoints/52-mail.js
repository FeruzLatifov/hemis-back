// 52.Mail endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_52 = [
    // ============================================
    // 52.Mail (2 endpoint)
    // ============================================
    {
        id: 1,
        category: "52.Mail",
        name: "Email yuborish",
        method: "POST",
        url: "/services/mail/send",
        requiresAuth: true,
        hasBody: true,
        inputFields: {
            id: {
                label: "ID",
                type: "text",
                defaultNew: "999999",
                defaultOld: "999999",
                required: true
            },
            resetLink: {
                label: "Reset havolasi",
                type: "text",
                defaultNew: "https://hemis.uz/reset_url",
                defaultOld: "https://hemis.uz/reset_url",
                required: true
            },
            to: {
                label: "Email manzil",
                type: "text",
                defaultNew: "no-reply@hemis.uz",
                defaultOld: "no-reply@hemis.uz",
                required: true
            }
        },
        bodyGenerator: function(fields) {
            return {
                "id": fields.id,
                "resetLink": fields.resetLink,
                "to": fields.to
            };
        },
        description: `**Email yuborish**

<b>Endpoint:</b> POST /services/mail/send

<b>OLD-HEMIS Request formati:</b>
<pre>
{
    "id": "999999",
    "resetLink": "https://hemis.uz/reset_url",
    "to": "no-reply@hemis.uz"
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "success": true,
    "id": "999999",
    "reset_link": "https://hemis.uz/reset_url",
    "to": "no-reply@hemis.uz"
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "52.Mail",
        name: "Tasdiqlash kodini yuborish",
        method: "POST",
        url: "/services/send/verifyCode",
        requiresAuth: true,
        hasBody: true,
        inputFields: {
            id: {
                label: "ID",
                type: "text",
                defaultNew: "999999",
                defaultOld: "999999",
                required: true
            },
            phone: {
                label: "Telefon",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false
            },
            email: {
                label: "Email",
                type: "text",
                defaultNew: "test@example.com",
                defaultOld: "kanet4u@gmail.com",
                required: true
            },
            verify_code: {
                label: "Tasdiqlash kodi",
                type: "text",
                defaultNew: "123456",
                defaultOld: "123456",
                required: true
            }
        },
        bodyGenerator: function(fields) {
            return {
                "id": fields.id,
                "phone": fields.phone || "",
                "email": fields.email,
                "verify_code": fields.verify_code
            };
        },
        description: `**Tasdiqlash kodini yuborish**

<b>Endpoint:</b> POST /services/send/verifyCode

<b>OLD-HEMIS Request formati:</b>
<pre>
{
    "id": "999999",
    "phone": "",
    "email": "kanet4u@gmail.com",
    "verify_code": "123456"
}
</pre>

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "email": {
        "success": true,
        "verify_code": "123456",
        "email": "kanet4u@gmail.com"
    }
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_52;
}
