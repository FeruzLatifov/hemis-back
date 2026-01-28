// 59.Test endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_59 = [
    // ============================================
    // 59.Test (1 endpoint)
    // ============================================
    {
        id: 1,
        category: "59.Test",
        name: "Minfin Social Provider (Tashqi API)",
        method: "GET",
        url: "/minfin/services/socialprov/v1",
        requiresAuth: true,
        hasBody: false,
        inputFields: {
            pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "302211100112",
                required: true
            }
        },
        urlBuilder: function(fields) {
            // Bu tashqi API - https://apimgw.egov.uz:8243/minfin/services/socialprov/v1
            return this.url;
        },
        description: `**Minfin Social Provider (Tashqi API)**

<b>Endpoint:</b> GET /minfin/services/socialprov/v1

<b>Izoh:</b> Bu tashqi API - https://apimgw.egov.uz:8243/minfin/services/socialprov/v1
OLD-HEMIS da tashqi tizimga murojaat qiladi.

<b>Parametrlar:</b>
- pinfl: PINFL raqami

<b>Eslatma:</b> Bu endpoint tashqi API bo'lgani uchun,
internal HEMIS serverda ishlamasligi mumkin.`,
        ported: false
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_59;
}
