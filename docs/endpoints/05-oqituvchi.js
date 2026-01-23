// 05.O'qituvchi endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_05 = [
    // ============================================
    // 05.O'qituvchi (5 endpoint)
    // ============================================
    {
                id: 1,
                category: "05.O'qituvchi",
                name: "Bitta o'qituvchi ma'lumotlarini olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_ETeacher/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Teacher ID (UUID)",
                        type: "text",
                        placeholder: "O'qituvchi UUID",
                        default: "0ae4c868-ba37-d51c-357e-a720957d3064",
                        required: true
                    },
                    dynamicAttributes: {
                        label: "Dynamic Attributes",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    },
                    view: {
                        label: "View",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "_local", label: "_local"}, {value: "_minimal", label: "_minimal"}],
                        default: "_local",
                        required: false
                    }
                },
                description: "ID bo'yicha bitta o'qituvchi ma'lumotlarini qaytaradi. view=_local - faqat asosiy fieldlar (pinfl, birthday, firstname, code, tag, serialNumber, address, lastname, fathername, phone, employeeYear).",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - TeacherEntityController.java
            },
    {
                id: 2,
                category: "05.O'qituvchi",
                name: "Barcha o'qituvchilar ro'yxati (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_ETeacher",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        placeholder: "50",
                        default: "3",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        placeholder: "0",
                        default: "0",
                        required: false
                    },
                    view: {
                        label: "View",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "_local", label: "_local"}, {value: "_minimal", label: "_minimal"}],
                        default: "_local",
                        required: false
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Barcha o'qituvchilar ro'yxatini sahifalangan holda qaytaradi. University filter avtomatik qo'llaniladi.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - TeacherEntityController.java
            },
    {
                id: 3,
                category: "05.O'qituvchi",
                name: "O'qituvchi ma'lumotlarini o'zgartirish (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_ETeacher/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Teacher ID (UUID)",
                        type: "text",
                        placeholder: "O'qituvchi UUID",
                        default: "0ae4c868-ba37-d51c-357e-a720957d3064",
                        required: true
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "false",
                        required: false
                    },
                    view: {
                        label: "View",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "_local", label: "_local"}, {value: "_minimal", label: "_minimal"}],
                        default: "_local",
                        required: false
                    }
                },
                requestBody: {
                    phone: "+998901234567",
                    address: "Toshkent shahar, Mirzo Ulugbek tumani"
                },
                description: "O'qituvchi ma'lumotlarini yangilaydi (partial update). Faqat yuborilgan fieldlar o'zgaradi: phone, address, firstname, lastname, fathername, serialNumber, birthday, employeeYear, code, tag, _citizenship, _gender, _university, _academic_degree, _academic_rank.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - TeacherEntityController.java
            },
    {
                id: 4,
                category: "05.O'qituvchi",
                name: "O'qituvchi ID sini olish (Service API)",
                method: "POST",
                url: "/app/rest/v2/services/teacher/id",
                requiresAuth: true,
                inputFields: {},
                requestBody: {
                    data: {
                        citizenship: "11",
                        pinfl: "42103714310024",
                        serial: "AD3391507",
                        year: "2022",
                        gender: "12"
                    }
                },
                description: "O'qituvchi Universal ID sini olish yoki yangi yaratish. citizenship=11 O'zbekiston fuqarosi. Mavjud o'qituvchi topilsa is_new=false, topilmasa yangi yaratiladi is_new=true. ID format: {universityCode}{YY}{gender}{sequence}.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - CubaTeacherServiceController.java
            },
    {
                id: 5,
                category: "05.O'qituvchi",
                name: "Yangi o'qituvchi yaratish (Entity API)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_ETeacher",
                requiresAuth: true,
                inputFields: {
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "false",
                        required: false
                    },
                    view: {
                        label: "View",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "_local", label: "_local"}, {value: "_minimal", label: "_minimal"}],
                        default: "_local",
                        required: false
                    }
                },
                bodyGenerator: () => ({
                    code: "401" + new Date().getFullYear().toString().slice(-2) + "11" + String(Math.floor(Math.random() * 900) + 100),
                    firstname: "Test",
                    lastname: "Oqituvchi",
                    fathername: "Testovich",
                    pinfl: String(Math.floor(Math.random() * 90000000000000) + 10000000000000),
                    birthday: "1985-03-15",
                    serialNumber: "ZZ" + String(Math.floor(Math.random() * 9000000) + 1000000),
                    _gender: "11",
                    _citizenship: "11",
                    _university: "401",
                    phone: "+998901234567",
                    address: "Toshkent sh."
                }),
                description: "Yangi o'qituvchi yaratish (Entity API). Shaxsiy ma'lumotlar: firstname, lastname, fathername, pinfl (14 raqam), birthday (YYYY-MM-DD), serialNumber. Reference kodlar: _gender (11=erkak, 12=ayol), _citizenship (11=O'zbekiston), _university (OTM kodi), _academic_degree, _academic_rank. Qo'shimcha: phone, address, employeeYear.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - TeacherEntityController.java
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_05;
}
