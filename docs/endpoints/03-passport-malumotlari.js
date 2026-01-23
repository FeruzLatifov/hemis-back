// 03.Passport ma'lumotlari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_03 = [
    // ============================================
    // 03.Passport ma'lumotlari (4 endpoint)
    // ============================================
    {
                id: 1,
                category: "03.Passport ma'lumotlari",
                name: "Passport ma'lumotlarni olish (Pinfl va Seria nomer bilan)",
                method: "GET",
                url: "/app/rest/v2/services/passport-data/getDataBySN",
                requiresAuth: true,
                params: {
                    pinfl: "{pinfl}",
                    seriaNumber: "{seriaNumber}",
                    captchaId: "{captchaId}",
                    captchaValue: "{captchaValue}"
                },
                inputFields: {
                    pinfl: {
                        label: "PINFL",
                        type: "text",
                        placeholder: "14 raqamli PINFL",
                        default: "",  // Configdan olinadi (newPinfl/oldPinfl)
                        useConfigPinfl: true,
                        required: true
                    },
                    seriaNumber: {
                        label: "Passport Seria/Raqam",
                        type: "text",
                        placeholder: "AB1234567",
                        default: "",  // Configdan olinadi
                        useConfigSerial: true,
                        required: true
                    },
                    captchaId: {
                        label: "Captcha ID",
                        type: "text",
                        placeholder: "UUID format (avtomatik to'ldiriladi)",
                        default: "",
                        required: true,
                        hint: "⚠️ Yangi captcha olish uchun tugmani bosing",
                        readonly: true,
                        needsCaptcha: true  // Show get captcha button
                    },
                    captchaValue: {
                        label: "Captcha Qiymati",
                        type: "text",
                        placeholder: "Captcha rasmidagi raqamni kiriting",
                        default: "",
                        required: true,
                        hint: "⚠️ Yuqoridagi rasmda ko'rsatilgan raqamni kiriting",
                        needsCaptcha: true  // Depends on captcha
                    }
                },
                description: "PINFL va passport seria-raqam orqali GUVD bazasidan passport ma'lumotlarini olish (captcha talab qilinadi)",
                dependsOn: 1,  // Requires authentication (token)
                ported: true  // ✅ Controller ported, 100% old-hemis format {success, data, address}
            },
    {
                id: 2,
                category: "03.Passport ma'lumotlari",
                name: "Passport ma'lumotlarni olish (Seria nomer va tug'ilgan kun bilan)",
                method: "GET",
                url: "/app/rest/v2/services/passport-data/getDataBySNBirthdate",
                requiresAuth: true,
                params: {
                    seriaNumber: "{seriaNumber}",
                    birthdate: "{birthdate}",
                    captchaId: "{captchaId}",
                    captchaValue: "{captchaValue}"
                },
                inputFields: {
                    seriaNumber: {
                        label: "Passport Seria/Raqam",
                        type: "text",
                        placeholder: "AB1234567",
                        default: "",  // Configdan olinadi
                        useConfigSerial: true,
                        required: true
                    },
                    birthdate: {
                        label: "Tug'ilgan sana",
                        type: "text",
                        placeholder: "YYYY-MM-DD",
                        default: "",  // Configdan olinadi
                        useConfigBirthdate: true,
                        required: true
                    },
                    captchaId: {
                        label: "Captcha ID",
                        type: "text",
                        placeholder: "UUID format (avtomatik to'ldiriladi)",
                        default: "",
                        required: true,
                        hint: "⚠️ Yangi captcha olish uchun tugmani bosing",
                        readonly: true,
                        needsCaptcha: true  // Show get captcha button
                    },
                    captchaValue: {
                        label: "Captcha Qiymati",
                        type: "text",
                        placeholder: "Captcha rasmidagi raqamni kiriting",
                        default: "",
                        required: true,
                        hint: "⚠️ Yuqoridagi rasmda ko'rsatilgan raqamni kiriting",
                        needsCaptcha: true  // Depends on captcha
                    }
                },
                description: "Passport seria-raqam va tug'ilgan sana orqali GUVD bazasidan passport ma'lumotlarini olish (PINFL talab qilinmaydi, captcha talab qilinadi)",
                dependsOn: 1,  // Requires authentication (token)
                ported: true  // ✅ Controller ported, 100% old-hemis format {success, data, address}
            },
    {
                id: 3,
                category: "03.Passport ma'lumotlari",
                name: "Passport ma'lumotlarni olish (Pinfl va tug'ilgan kun bilan)",
                method: "GET",
                url: "/app/rest/v2/services/passport-data/getDataByPinflBirthdate",
                requiresAuth: true,
                params: {
                    pinfl: "{pinfl}",
                    birthdate: "{birthdate}",
                    captchaId: "{captchaId}",
                    captchaValue: "{captchaValue}"
                },
                inputFields: {
                    pinfl: {
                        label: "PINFL",
                        type: "text",
                        placeholder: "14 raqamli PINFL",
                        default: "",  // Configdan olinadi (newPinfl/oldPinfl)
                        useConfigPinfl: true,
                        required: true
                    },
                    birthdate: {
                        label: "Tug'ilgan sana",
                        type: "text",
                        placeholder: "YYYY-MM-DD",
                        default: "",  // Configdan olinadi
                        useConfigBirthdate: true,
                        required: true
                    },
                    captchaId: {
                        label: "Captcha ID",
                        type: "text",
                        placeholder: "UUID format (avtomatik to'ldiriladi)",
                        default: "",
                        required: true,
                        hint: "⚠️ Yangi captcha olish uchun tugmani bosing",
                        readonly: true,
                        needsCaptcha: true  // Show get captcha button
                    },
                    captchaValue: {
                        label: "Captcha Qiymati",
                        type: "text",
                        placeholder: "Captcha rasmidagi raqamni kiriting",
                        default: "",
                        required: true,
                        hint: "⚠️ Yuqoridagi rasmda ko'rsatilgan raqamni kiriting",
                        needsCaptcha: true  // Depends on captcha
                    }
                },
                description: "PINFL va tug'ilgan sana orqali GUVD bazasidan passport ma'lumotlarini olish (captcha talab qilinadi)",
                dependsOn: 1,  // Requires authentication (token)
                ported: true  // ✅ Controller ported, 100% old-hemis format {success, data, address}
            },
    {
                id: 4,
                category: "03.Passport ma'lumotlari",
                name: "Address",
                method: "GET",
                url: "/app/rest/v2/services/passport-data/getAddress",
                requiresAuth: true,
                params: {
                    pinfl: "{pinfl}"
                },
                inputFields: {
                    pinfl: {
                        label: "PINFL",
                        type: "text",
                        placeholder: "14 raqamli PINFL",
                        default: "",  // Configdan olinadi (newPinfl/oldPinfl)
                        useConfigPinfl: true,
                        required: true
                    }
                },
                description: "PINFL orqali GUVD bazasidan manzil ma'lumotlarini olish (captcha talab qilinmaydi)",
                dependsOn: 1,  // Requires authentication (token)
                ported: true  // ✅ Controller ported, 100% old-hemis format {success, data}
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_03;
}
