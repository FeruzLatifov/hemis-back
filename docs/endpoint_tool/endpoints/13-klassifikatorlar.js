// 13.Klassifikatorlar endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_13 = [
    // ============================================
    // 13.Klassifikatorlar (3 endpoint)
    // ============================================
    {
                id: 1,
                category: "13.Klassifikatorlar",
                name: "Barcha klassifikatorlar ro'yxati (GET /info)",
                method: "GET",
                url: "/app/rest/v2/services/classifiers/info",
                requiresAuth: true,
                description: "Tizimda mavjud barcha klassifikatorlar ro'yxatini olish (title, version, count)",
                ported: true
            },
    {
                id: 2,
                category: "13.Klassifikatorlar",
                name: "Bitta klassifikatorni olish (GET /single)",
                method: "GET",
                url: "/app/rest/v2/services/classifiers/single",
                requiresAuth: true,
                inputFields: {
                    classifier: {
                        label: "Klassifikator nomi",
                        type: "text",
                        default: "h_university",
                        defaultNew: "h_university",
                        defaultOld: "h_university",
                        required: true,
                        placeholder: "h_gender, h_citizenship_type, h_education_form...",
                        description: "Klassifikator nomi (masalan: h_university, h_gender, h_citizenship_type, h_education_form, h_payment_form)"
                    }
                },
                description: "Bitta klassifikator ma'lumotlarini olish (CUBA formatida)",
                ported: true
            },
    {
                id: 3,
                category: "13.Klassifikatorlar",
                name: "Barcha klassifikatorlar (items bilan) (GET /allItems)",
                method: "GET",
                url: "/app/rest/v2/services/classifiers/allItems",
                requiresAuth: true,
                description: "Barcha klassifikatorlarni items bilan birga olish (CUBA formatida)",
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_13;
}
