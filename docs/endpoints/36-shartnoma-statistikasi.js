// 36.Shartnoma statistikasi endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_36 = [
    // ============================================
    // 36.Shartnoma statistikasi (2 endpoint)
    // ============================================
    {
                id: 1,
                category: "36.Shartnoma statistikasi",
                name: "Shartnoma statistikasini yuborish",
                method: "POST",
                url: "/app/rest/v2/services/student/contractStatistics",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                    universityCode: {
                        label: "OTM kodi",
                        type: "text",
                        defaultNew: "401",
                        defaultOld: "999",
                        required: true
                    },
                    educationYearCode: {
                        label: "Ta'lim yili kodi",
                        type: "text",
                        defaultNew: "2024",
                        defaultOld: "2021",
                        required: true
                    },
                    educationTypeCode: {
                        label: "Ta'lim turi kodi",
                        type: "text",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: true
                    },
                    educationFormCode: {
                        label: "Ta'lim shakli kodi",
                        type: "text",
                        defaultNew: "13",
                        defaultOld: "13",
                        required: true
                    },
                    facultyCode: {
                        label: "Fakultet kodi",
                        type: "text",
                        defaultNew: "401-102",
                        defaultOld: "999-102",
                        required: true
                    },
                    courseCode: {
                        label: "Kurs kodi",
                        type: "text",
                        defaultNew: "12",
                        defaultOld: "12",
                        required: true
                    },
                    semesterCode: {
                        label: "Semestr kodi",
                        type: "text",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: true
                    },
                    date: {
                        label: "Sana (YYYY-MM-DD)",
                        type: "text",
                        defaultNew: "2024-09-11",
                        defaultOld: "2021-09-11",
                        required: true
                    },
                    dailyCount: {
                        label: "Kunlik soni",
                        type: "number",
                        defaultNew: "1",
                        defaultOld: "1",
                        required: true
                    },
                    total: {
                        label: "Jami soni",
                        type: "number",
                        defaultNew: "0",
                        defaultOld: "0",
                        required: true
                    }
                },
                bodyGenerator: function(fields) {
                    return {
                        "contractStatistics": {
                            "university": {"code": fields.universityCode},
                            "educationYear": {"code": fields.educationYearCode},
                            "educationType": {"code": fields.educationTypeCode},
                            "educationForm": {"code": fields.educationFormCode},
                            "faculty": {"code": fields.facultyCode},
                            "course": {"code": fields.courseCode},
                            "semester": {"code": fields.semesterCode},
                            "date": fields.date,
                            "dailyCount": parseInt(fields.dailyCount) || 0,
                            "total": parseInt(fields.total) || 0
                        }
                    };
                },
                description: `**Shartnoma statistikasini yuborish**

<b>Endpoint:</b> POST /app/rest/v2/services/student/contractStatistics

<b>Parametrlar:</b>
- universityCode: OTM kodi (NEW: 401, OLD: 999)
- educationYearCode: Ta'lim yili (NEW: 2024, OLD: 2021)
- educationTypeCode: Ta'lim turi (11=Bakalavr)
- educationFormCode: Ta'lim shakli (13=Sirtqi)
- facultyCode: Fakultet kodi (NEW: 401-102, OLD: 999-102)
- courseCode: Kurs kodi (12=2-kurs)
- semesterCode: Semestr kodi (11=Kuzgi)
- date: Sana (YYYY-MM-DD)
- dailyCount: Kunlik soni
- total: Jami soni

<b>Response:</b> Saqlangan statistika ma'lumotlari`,
                ported: true
            },
    {
                id: 2,
                category: "36.Shartnoma statistikasi",
                name: "Shartnoma statistikasi entity ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RContractStatistics",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                    view: {
                        label: "View nomi",
                        type: "text",
                        defaultNew: "rContractStatistics-view",
                        defaultOld: "rContractStatistics-view",
                        required: false
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        defaultNew: "2",
                        defaultOld: "2",
                        required: false
                    }
                },
                urlBuilder: function(fields) {
                    let params = [];
                    if (fields.view) params.push("view=" + encodeURIComponent(fields.view));
                    if (fields.limit) params.push("limit=" + encodeURIComponent(fields.limit));
                    return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Shartnoma statistikasi entity ro'yxati**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RContractStatistics

<b>Parametrlar:</b>
- view: CUBA view nomi (rContractStatistics-view)
- limit: Natijalar soni

<b>Response:</b> Shartnoma statistikasi ro'yxati (nested objectlar bilan)`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_36;
}
