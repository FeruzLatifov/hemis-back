// 02.Captcha endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_02 = [
    // ============================================
    // 02.Captcha (2 endpoint)
    // ============================================
    {
                id: 1,
                category: "02.Captcha",
                name: "getNumericCaptcha (Raqamli captcha)",
                method: "GET",
                url: "/app/rest/v2/services/captcha/getNumericCaptcha",
                requiresAuth: true,  // Old-hemis uchun auth kerak
                dependsOn: 1,
                params: {},
                description: "5 xonali numeric captcha generatsiya qilish (PNG base64)",
                ported: true  // ✅ Controller ported, 100% old-hemis format {id, image}
            },
    {
                id: 2,
                category: "02.Captcha",
                name: "getArithmeticCaptcha (Arifmetik captcha)",
                method: "GET",
                url: "/app/rest/v2/services/captcha/getArithmeticCaptcha",
                requiresAuth: true,  // Old-hemis uchun auth kerak
                dependsOn: 1,
                params: {},
                description: "Arifmetik ifoda captcha (masalan: '5 + 3 = ?') - PUBLIC endpoint",
                ported: true  // ✅ Controller ported, 100% old-hemis format {id, image}
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_02;
}
