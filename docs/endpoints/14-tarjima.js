// 14.Tarjima endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_14 = [
    // ============================================
    // 14.Tarjima (2 endpoint)
    // ============================================
    {
                id: 1,
                category: "14.Tarjima",
                name: "Barcha tarjimalar (GET /translate/get)",
                method: "GET",
                url: "/app/rest/v2/services/translate/get",
                requiresAuth: true,
                description: "Barcha tarjimalarni OLD-HEMIS formatida olish (_entityName, message, uz_Uz, ru_Ru, oz_Uz, en_Us, kk_Uz, category, version)",
                ported: true
            },
    {
                id: 2,
                category: "14.Tarjima",
                name: "Tarjimalarni filtrlab olish (POST /translate/get)",
                method: "POST",
                url: "/app/rest/v2/services/translate/get",
                requiresAuth: true,
                inputFields: {
                    category: { label: "Kategoriya", type: "text", defaultValue: "app", required: false }
                },
                description: "Tarjimalarni kategoriya bo'yicha filtrlab olish",
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_14;
}
