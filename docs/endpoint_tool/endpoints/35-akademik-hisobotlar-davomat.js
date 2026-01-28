// 35.Akademik hisobotlar davomat endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_35 = [
    // ============================================
    // 35.Akademik hisobotlar davomat (7 endpoint)
    // ============================================
    {
                id: 1,
                category: "35.Akademik hisobotlar davomat",
                name: "Yangi davomat yozuvi yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicAttendance",
                requiresAuth: true,
                inputFields: {
                    universityCode: {
                        label: "Universitet kodi",
                        type: "text",
                        defaultNew: "401",
                        defaultOld: "351",
                        required: false
                    },
                    universityName: {
                        label: "Universitet nomi",
                        type: "text",
                        defaultNew: "TATU",
                        defaultOld: "BuxDU",
                        required: false
                    },
                    facultyCode: {
                        label: "Fakultet kodi",
                        type: "text",
                        defaultNew: "401-01",
                        defaultOld: "351-01",
                        required: false
                    },
                    facultyName: {
                        label: "Fakultet nomi",
                        type: "text",
                        defaultNew: "Dasturiy injiniring",
                        defaultOld: "Fizika-matematika",
                        required: false
                    },
                    educationTypeCode: {
                        label: "Ta'lim turi kodi",
                        type: "text",
                        defaultNew: "11",
                        defaultOld: "11",
                        required: false
                    },
                    educationTypeName: {
                        label: "Ta'lim turi nomi",
                        type: "text",
                        defaultNew: "Bakalavr",
                        defaultOld: "Bakalavr",
                        required: false
                    },
                    educationYearCode: {
                        label: "O'quv yili kodi",
                        type: "text",
                        defaultNew: "2024",
                        defaultOld: "2023",
                        required: false
                    },
                    semesterTypeCode: {
                        label: "Semestr kodi",
                        type: "text",
                        defaultNew: "1",
                        defaultOld: "1",
                        required: false
                    },
                    courseCode: {
                        label: "Kurs kodi",
                        type: "text",
                        defaultNew: "1",
                        defaultOld: "1",
                        required: false
                    },
                    attendancePercent: {
                        label: "Davomat foizi",
                        type: "number",
                        defaultNew: "85.5",
                        defaultOld: "78.3",
                        required: false
                    },
                    badAttendanceStudentCount: {
                        label: "Yomon davomatli talabalar soni",
                        type: "number",
                        defaultNew: "10",
                        defaultOld: "15",
                        required: false
                    },
                    updateDate: {
                        label: "Yangilangan sana",
                        type: "text",
                        placeholder: "YYYY-MM-DD",
                        defaultNew: "2024-01-15",
                        defaultOld: "2023-01-15",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["universityCode", "universityName", "facultyCode", "facultyName", "educationTypeCode", "educationTypeName", "educationYearCode", "semesterTypeCode", "courseCode", "attendancePercent", "badAttendanceStudentCount", "updateDate"],
                description: `**Yangi davomat yozuvi yaratish**

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicAttendance

<b>Response:</b> 201 Created`,
                ported: true,
                storeResultId: "academicAttendanceId"
            },
    {
                id: 2,
                category: "35.Akademik hisobotlar davomat",
                name: "Davomat yozuvini ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicAttendance/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "academicAttendanceId"
                    },
                    returnNulls: {
                        label: "Null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        defaultNew: "",
                        defaultOld: "",
                        required: false
                    }
                },
                description: `**Davomat yozuvini ID bo'yicha olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicAttendance/{entityId}

<b>Response:</b> 200 OK yoki 404 Not Found`,
                ported: true
            },
    {
                id: 3,
                category: "35.Akademik hisobotlar davomat",
                name: "Davomat yozuvini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RAcademicAttendance/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "academicAttendanceId"
                    },
                    attendancePercent: {
                        label: "Davomat foizi (yangi)",
                        type: "number",
                        defaultNew: "92.0",
                        defaultOld: "85.0",
                        required: false
                    },
                    badAttendanceStudentCount: {
                        label: "Yomon davomatli talabalar (yangi)",
                        type: "number",
                        defaultNew: "5",
                        defaultOld: "10",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["attendancePercent", "badAttendanceStudentCount"],
                description: `**Davomat yozuvini yangilash**

<b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RAcademicAttendance/{entityId}

<b>Response:</b> 200 OK yoki 404 Not Found`,
                ported: true
            },
    {
                id: 4,
                category: "35.Akademik hisobotlar davomat",
                name: "Davomat yozuvini o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RAcademicAttendance/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik",
                        defaultNew: "",
                        defaultOld: "",
                        required: true,
                        useStoredId: "academicAttendanceId"
                    }
                },
                description: `**Davomat yozuvini o'chirish (soft delete)**

<b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RAcademicAttendance/{entityId}

<b>Response:</b> 200 OK yoki 404 Not Found`,
                ported: true
            },
    {
                id: 5,
                category: "35.Akademik hisobotlar davomat",
                name: "Barcha davomat yozuvlarini olish (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicAttendance",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        defaultNew: "50",
                        defaultOld: "50",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        defaultNew: "0",
                        defaultOld: "0",
                        required: false
                    },
                    returnNulls: {
                        label: "Null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        defaultNew: "",
                        defaultOld: "",
                        required: false
                    }
                },
                description: `**Barcha davomat yozuvlarini olish**

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicAttendance

<b>Response:</b> Sahifalangan davomat ro'yxati`,
                ported: true
            },
    {
                id: 6,
                category: "35.Akademik hisobotlar davomat",
                name: "Davomat yozuvlarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicAttendance/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON)",
                        type: "textarea",
                        placeholder: '{"conditions":[{"property":"universityCode","operator":"=","value":"351"}]}',
                        defaultNew: '{"conditions":[{"property":"universityCode","operator":"=","value":"351"}]}',
                        defaultOld: '{"conditions":[{"property":"universityCode","operator":"=","value":"351"}]}',
                        required: false,
                        rows: 3
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        defaultNew: "2",
                        defaultOld: "2",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        defaultNew: "0",
                        defaultOld: "0",
                        required: false
                    },
                    returnNulls: {
                        label: "Null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        defaultNew: "",
                        defaultOld: "",
                        required: false
                    }
                },
                description: `**Davomat yozuvlarini qidirish** (GET /search)

<b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicAttendance/search

<b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
            },
    {
                id: 7,
                category: "35.Akademik hisobotlar davomat",
                name: "Davomat yozuvlarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicAttendance/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (CUBA JSON)",
                        type: "textarea",
                        placeholder: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                        defaultNew: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                        defaultOld: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                        required: false,
                        rows: 3
                    },
                    limit: {
                        label: "Limit",
                        type: "number",
                        defaultNew: "50",
                        defaultOld: "50",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        defaultNew: "0",
                        defaultOld: "0",
                        required: false
                    },
                    returnNulls: {
                        label: "Null qiymatlarni qaytarish",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        defaultNew: "",
                        defaultOld: "",
                        required: false
                    }
                },
                hasBody: true,
                bodyFields: ["filter", "limit", "offset"],
                description: `**Davomat yozuvlarini qidirish** (POST /search)

<b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicAttendance/search

<b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_35;
}
