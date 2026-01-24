// 67.OTM Config endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_67 = [
    // ============================================
    // 67.OTM Config (1 endpoint)
    // ============================================
    {
        id: 1,
        category: "67.OTM Config",
        name: "Universitet konfiguratsiyasi",
        method: "GET",
        url: "/services/university/config",
        requiresAuth: true,
        hasBody: false,
        inputFields: {},
        urlBuilder: function(fields) {
            return this.url;
        },
        description: `**Universitet konfiguratsiyasi**

<b>Endpoint:</b> GET /services/university/config

<b>Parametrlar:</b> Yo'q

<b>Eslatma:</b> Bu endpoint tizim bo'ylab universitet sozlamalarini qaytaradi:
- Mavjud universitetlar ro'yxati
- Tizim sozlamalari
- Feature flaglar

<b>OLD-HEMIS Response formati:</b>
<pre>
{
    "universities": [...],
    "settings": {...},
    "features": {...}
}
</pre>`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_67;
}
