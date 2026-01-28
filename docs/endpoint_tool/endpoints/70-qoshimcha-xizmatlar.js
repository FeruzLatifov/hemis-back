// 70.Qo'shimcha xizmatlar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_70 = [
    // ============================================
    // 70.Qo'shimcha xizmatlar (4 endpoint)
    // ============================================
    {
        id: 1,
        category: "70.Qo'shimcha xizmatlar",
        name: "Hokimiyat klassifikatorlari",
        method: "GET",
        url: "/services/classifiers/hokimiyat",
        requiresAuth: true,
        hasBody: false,
        inputFields: {},
        urlBuilder: function(fields) {
            return this.url;
        },
        description: `**Hokimiyat klassifikatorlari**

<b>Endpoint:</b> GET /services/classifiers/hokimiyat

<b>Parametrlar:</b> Yo'q

<b>Tavsif:</b> Viloyat va tumanlar ierarxik strukturasini qaytaradi.

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "regions": [
        {
            "code": "1703",
            "name": "Toshkent shahri",
            "districts": [
                {
                    "code": "1703201",
                    "name": "Bektemir tumani"
                },
                ...
            ]
        },
        ...
    ]
}
</pre>`,
        ported: true
    },
    {
        id: 2,
        category: "70.Qo'shimcha xizmatlar",
        name: "Diplom tekshirish (hash bo'yicha)",
        method: "GET",
        url: "/services/diploma/byhash",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            hash: {
                label: "Diplom hash (QR koddan)",
                type: "text",
                defaultNew: "71d6a9e0436cfb3aaa9fee3f88844b42",
                defaultOld: "71d6a9e0436cfb3aaa9fee3f88844b42",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.hash) params.push("hash=" + encodeURIComponent(fields.hash));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Diplom tekshirish (hash bo'yicha)**

<b>Endpoint:</b> GET /services/diploma/byhash

<b>Parametrlar:</b>
- hash: Diplom QR kodidan olingan hash

<b>Tavsif:</b> Ish beruvchilar va davlat organlari tomonidan diplom haqiqiyligini tekshirish uchun ishlatiladi.

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "id": "uuid",
    "diplomaNumber": "12345678",
    "serialNumber": "AB",
    "student": {...},
    "university": {...},
    "specialty": {...},
    "diplomaType": {...},
    "issueDate": "2024-06-15",
    "diplomaHash": "71d6a9e0436cfb3aaa9fee3f88844b42",
    "status": "ACTIVE",
    "verified": true,
    "verificationDate": "2025-01-24T..."
}
</pre>`,
        ported: true
    },
    {
        id: 3,
        category: "70.Qo'shimcha xizmatlar",
        name: "Talaba olish (PINFL bo'yicha)",
        method: "GET",
        url: "/services/student/get",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "Talaba PINFL",
                type: "text",
                defaultNew: "61009076610088",
                defaultOld: "61009076610088",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Talaba olish (PINFL bo'yicha)**

<b>Endpoint:</b> GET /services/student/get

<b>Parametrlar:</b>
- pinfl: Talaba PINFL raqami

<b>Tavsif:</b> PINFL raqami bo'yicha talaba ma'lumotlarini olish.

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "id": "uuid",
    "pinfl": "61009076610088",
    "firstName": "...",
    "lastName": "...",
    "middleName": "...",
    "university": {...},
    "faculty": {...},
    "specialty": {...},
    "course": 1,
    "group": {...},
    "status": {...}
}
</pre>`,
        ported: true
    },
    {
        id: 4,
        category: "70.Qo'shimcha xizmatlar",
        name: "Faol talaba olish (PINFL bo'yicha)",
        method: "GET",
        url: "/services/student/getActive",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "Talaba PINFL",
                type: "text",
                defaultNew: "52305046740024",
                defaultOld: "52305046740024",
                required: true
            }
        },
        urlBuilder: function(fields) {
            let params = [];
            if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
            return this.url + (params.length > 0 ? "?" + params.join("&") : "");
        },
        description: `**Faol talaba olish (PINFL bo'yicha)**

<b>Endpoint:</b> GET /services/student/getActive

<b>Parametrlar:</b>
- pinfl: Talaba PINFL raqami

<b>Tavsif:</b> Faqat faol holatdagi (o'qiyotgan, bitirib ketmagan/chiqarib yuborilmagan) talaba ma'lumotlarini qaytaradi.

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "id": "uuid",
    "pinfl": "52305046740024",
    "firstName": "...",
    "lastName": "...",
    "middleName": "...",
    "university": {...},
    "faculty": {...},
    "specialty": {...},
    "course": 1,
    "group": {...},
    "status": "ACTIVE"
}
</pre>

<b>Eslatma:</b> Agar talaba faol bo'lmasa (bitirib ketgan, chiqarib yuborilgan), null qaytariladi.`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_70;
}
