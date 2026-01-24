// 53.Healthcheck endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// OLD-HEMIS FORMAT BILAN 100% MOSLIK!

const endpoints_53 = [
    // ============================================
    // 53.Healthcheck (1 endpoint)
    // ============================================
    {
        id: 1,
        category: "53.Healthcheck",
        name: "Server holatini tekshirish",
        method: "GET",
        url: "/actuator/health",
        requiresAuth: false,
        hasBody: false,
        inputFields: {},
        urlBuilder: function(fields) {
            return this.url;
        },
        description: `**Server holatini tekshirish**

<b>Endpoint:</b> GET /actuator/health

<b>Response formati:</b>
<pre>
{
    "status": "UP"
}
</pre>

<b>Izoh:</b> Bu endpoint serverning ishlash holatini tekshirish uchun ishlatiladi.`,
        ported: true
    }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_53;
}
