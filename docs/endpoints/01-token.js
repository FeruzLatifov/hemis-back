// 01.Token endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_01 = [
    // ============================================
    // 01.Token (3 endpoint)
    // ============================================
    {
                id: 1,
                category: "01.Token",
                name: "Token olish (Password Grant)",
                method: "POST",
                url: "/app/rest/v2/oauth/token",  // Same URL for both (backward compatibility)
                requiresAuth: false,
                auth: "basic",
                contentType: "multipart",
                params: {},
                body: {
                    grant_type: "password",
                    username: "{username}",
                    password: "{password}"
                },
                description: "Username va password bilan OAuth2 token olish",
                ported: true  // ✅ Controller ported
            },
    {
                id: 2,
                category: "01.Token",
                name: "Token yangilash (Refresh Grant)",
                method: "POST",
                url: "/app/rest/v2/oauth/token",  // Same URL for both
                requiresAuth: false,
                auth: "basic",
                contentType: "multipart",
                params: {},
                body: {
                    grant_type: "refresh_token",
                    refresh_token: "{refresh_token}"
                },
                description: "Refresh token bilan yangi access token olish",
                dependsOn: 1,
                ported: true  // ✅ Controller ported
            },
    {
                id: 3,
                category: "01.Token",
                name: "Foydalanuvchi ma'lumotlari",
                method: "GET",
                url: "/app/rest/v2/userInfo",  // ✅ Old-hemis URL (backward compatible)
                requiresAuth: true,
                params: {},
                description: "Joriy authenticated user ma'lumotlari (old-hemis format)",
                dependsOn: 1,
                ported: true  // ✅ Controller ported (supports both URLs)
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_01;
}
