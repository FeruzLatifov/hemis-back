// 04.Talaba endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_04 = [
    // ============================================
    // 04.Talaba (18 endpoint)
    // ============================================
    {
                id: 1,
                category: "04.Talaba",
                name: "Talaba yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EStudent",
                requiresAuth: true,
                params: {
                    returnNulls: "false"
                },
                inputFields: {
                    code: {
                        label: "Talaba kodi (unique ID)",
                        type: "text",
                        placeholder: "Avtomatik generatsiya qilinadi",
                        defaultNew: "401" + Date.now().toString().slice(-10),
                        defaultOld: "351" + Date.now().toString().slice(-10),
                        required: true
                    },
                    pinfl: {
                        label: "PINFL",
                        type: "text",
                        placeholder: "14 xonali JSHSHIR",
                        defaultNew: "31507976020099",
                        defaultOld: "31507976020099",
                        required: true
                    },
                    serialNumber: {
                        label: "Passport seriya/raqami",
                        type: "text",
                        placeholder: "Masalan: AA1234567",
                        defaultNew: "AA" + Date.now().toString().slice(-7),
                        defaultOld: "AA" + Date.now().toString().slice(-7),
                        required: true
                    },
                    firstname: {
                        label: "Ism",
                        type: "text",
                        placeholder: "Talaba ismi",
                        defaultNew: "TestNew",
                        defaultOld: "TestOld",
                        required: true
                    },
                    lastname: {
                        label: "Familiya",
                        type: "text",
                        placeholder: "Talaba familiyasi",
                        defaultNew: "Testov",
                        defaultOld: "Testov",
                        required: true
                    },
                    fathername: {
                        label: "Otasining ismi",
                        type: "text",
                        placeholder: "Otasining ismi",
                        defaultNew: "Testovich",
                        defaultOld: "Testovich",
                        required: false
                    },
                    university_code: {
                        label: "Universitet kodi",
                        type: "text",
                        placeholder: "401=Yangi, 351=Eski",
                        defaultNew: "401",
                        defaultOld: "351",
                        required: true
                    },
                    education_type: {
                        label: "Ta'lim turi",
                        type: "select",
                        options: [
                            {value: "11", label: "11 - Bakalavr"},
                            {value: "12", label: "12 - Magistr"},
                            {value: "13", label: "13 - Doktorant"}
                        ],
                        default: "11",
                        required: true
                    },
                    education_form: {
                        label: "Ta'lim shakli",
                        type: "select",
                        options: [
                            {value: "11", label: "11 - Kunduzgi"},
                            {value: "12", label: "12 - Sirtqi"},
                            {value: "13", label: "13 - Kechki"}
                        ],
                        default: "11",
                        required: true
                    },
                    education_year: {
                        label: "Ta'lim yili",
                        type: "select",
                        options: [
                            {value: "2024", label: "2024-2025"},
                            {value: "2023", label: "2023-2024"},
                            {value: "2022", label: "2022-2023"}
                        ],
                        default: "2024",
                        required: true
                    },
                    student_status: {
                        label: "Talaba holati",
                        type: "select",
                        options: [
                            {value: "11", label: "11 - Faol"},
                            {value: "12", label: "12 - Chetlashgan"},
                            {value: "10", label: "10 - Boshqa"}
                        ],
                        default: "11",
                        required: true
                    }
                },
                bodyTemplate: {
                    code: "{code}",
                    pinfl: "{pinfl}",
                    serialNumber: "{serialNumber}",
                    firstname: "{firstname}",
                    lastname: "{lastname}",
                    fathername: "{fathername}",
                    _university: "{university_code}",
                    _education_type: "{education_type}",
                    _education_form: "{education_form}",
                    _education_year: "{education_year}",
                    _student_status: "{student_status}"
                },
                description: "Yangi talaba yaratish. Field nomlari underscore bilan: _university, _education_type, _student_status",
                dependsOn: 1,
                ported: true,
                storeResultId: "createdStudentId"
            },
    {
                id: 2,
                category: "04.Talaba",
                name: "Talabani olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EStudent/{entityId}",
                requiresAuth: true,
                params: {
                    dynamicAttributes: "false",
                    returnNulls: "false",
                    view: "_local"
                },
                inputFields: {
                    entityId: {
                        label: "Talaba ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik to'ldiriladi",
                        default: "",
                        useStoredId: "createdStudentId",
                        required: true
                    },
                    view: {
                        label: "View",
                        type: "select",
                        options: [
                            {value: "_local", label: "_local (faqat lokal maydonlar)"},
                            {value: "_minimal", label: "_minimal"},
                            {value: "", label: "To'liq (barcha maydonlar)"}
                        ],
                        default: "_local",
                        required: false
                    },
                    returnNulls: {
                        label: "Null qiymatlarni qaytarish",
                        type: "select",
                        options: [
                            {value: "false", label: "Yo'q"},
                            {value: "true", label: "Ha"}
                        ],
                        default: "false",
                        required: false
                    }
                },
                description: "POST da yaratilgan talabani ID bo'yicha olish. view=_local bilan OLD-HEMIS compatible. ⚠️ MUHIM: Solishtirishda OLD va NEW HEMIS uchun bir xil studentId ishlatilishi kerak!",
                dependsOn: 1,
                ported: true
            },
    {
                id: 3,
                category: "04.Talaba",
                name: "Talabani yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EStudent/{entityId}",
                requiresAuth: true,
                params: {
                    returnNulls: "false"
                },
                inputFields: {
                    entityId: {
                        label: "Talaba ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik to'ldiriladi",
                        default: "",
                        useStoredId: "createdStudentId",
                        required: true
                    },
                    phone: {
                        label: "Telefon raqami",
                        type: "text",
                        placeholder: "+998901234567",
                        defaultNew: "+998901112233",
                        defaultOld: "+998901112233",
                        required: false
                    },
                    email: {
                        label: "Email",
                        type: "text",
                        placeholder: "student@example.com",
                        defaultNew: "updated@hemis.uz",
                        defaultOld: "updated@hemis.uz",
                        required: false
                    },
                    address: {
                        label: "Manzil",
                        type: "text",
                        placeholder: "Toshkent shahar...",
                        defaultNew: "Yangilangan manzil - Toshkent",
                        defaultOld: "Yangilangan manzil - Toshkent",
                        required: false
                    }
                },
                bodyTemplate: {
                    phone: "{phone}",
                    email: "{email}",
                    address: "{address}"
                },
                description: "POST da yaratilgan talaba ma'lumotlarini qisman yangilash. Faqat yuborilgan fieldlar o'zgaradi.",
                dependsOn: 1,
                ported: true
            },
    {
                id: 4,
                category: "04.Talaba",
                name: "Barcha talabalar ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EStudent",
                requiresAuth: true,
                params: {
                    limit: "5",
                    offset: "0",
                    returnCount: "true",
                    view: "_local"
                },
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "text",
                        default: "5",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "text",
                        default: "0",
                        required: false
                    },
                    view: {
                        label: "View",
                        type: "text",
                        default: "_local",
                        required: false
                    },
                    returnCount: {
                        label: "Jami sonni qaytarish",
                        type: "select",
                        options: [{value: "true", label: "Ha"}, {value: "false", label: "Yo'q"}],
                        default: "true",
                        required: false
                    }
                },
                description: "Sahifalangan talabalar ro'yxatini olish",
                dependsOn: 1,
                ported: true
            },
    {
                id: 5,
                category: "04.Talaba",
                name: "Talaba ID sini olish (Service)",
                method: "POST",
                url: "/app/rest/v2/services/student/id",
                requiresAuth: true,
                params: {},
                inputFields: {
                    citizenship: {
                        label: "Fuqarolik kodi",
                        type: "select",
                        options: [
                            {value: "11", label: "11 - O'zbekiston"},
                            {value: "12", label: "12 - Chet el"}
                        ],
                        default: "11",
                        required: true
                    },
                    pinfl: {
                        label: "PINFL (JSHSHIR)",
                        type: "text",
                        placeholder: "14 xonali JSHSHIR raqami",
                        defaultNew: "31507976020031",
                        defaultOld: "31507976020031",
                        required: true
                    },
                    serial: {
                        label: "Passport seriya/raqami",
                        type: "text",
                        placeholder: "Masalan: AA6970877",
                        defaultNew: "A0939758",
                        defaultOld: "A0939758",
                        required: true
                    },
                    year: {
                        label: "Ta'lim yili",
                        type: "select",
                        options: [
                            {value: "2024", label: "2024-2025"},
                            {value: "2023", label: "2023-2024"}
                        ],
                        default: "2024",
                        required: true
                    },
                    education_type: {
                        label: "Ta'lim turi",
                        type: "select",
                        options: [
                            {value: "11", label: "11 - Bakalavr"},
                            {value: "12", label: "12 - Magistr"}
                        ],
                        default: "11",
                        required: true
                    },
                    education_form: {
                        label: "Ta'lim shakli",
                        type: "select",
                        options: [
                            {value: "11", label: "11 - Kunduzgi"},
                            {value: "12", label: "12 - Sirtqi"}
                        ],
                        default: "11",
                        required: true
                    }
                },
                bodyTemplate: {
                    data: {
                        citizenship: "{citizenship}",
                        pinfl: "{pinfl}",
                        serial: "{serial}",
                        year: "{year}",
                        education_type: "{education_type}",
                        education_form: "{education_form}"
                    }
                },
                description: "Talaba uchun unique ID olish yoki yangi ID yaratish",
                dependsOn: 1,
                ported: true
            },
    {
                id: 6,
                category: "04.Talaba",
                name: "Talabani transfer qilish (Service)",
                method: "POST",
                url: "/app/rest/v2/services/student/update",
                requiresAuth: true,
                params: {},
                inputFields: {
                    student_id: {
                        label: "Talaba ID (UUID)",
                        type: "text",
                        placeholder: "POST dan olingan ID",
                        default: "",
                        useStoredId: "createdStudentId",
                        required: true
                    },
                    university_code: {
                        label: "Yangi universitet kodi",
                        type: "text",
                        defaultNew: "999",
                        defaultOld: "999",
                        required: true
                    },
                    student_status: {
                        label: "Yangi status",
                        type: "select",
                        options: [
                            {value: "11", label: "11 - Faol"},
                            {value: "12", label: "12 - Chetlashgan"},
                            {value: "16", label: "16 - Bitirgan"}
                        ],
                        default: "12",
                        required: true
                    }
                },
                bodyTemplate: {
                    student: {
                        id: "{student_id}",
                        university: {code: "{university_code}"},
                        studentStatus: {code: "{student_status}"}
                    }
                },
                description: "Talabani boshqa universitetga o'tkazish (transfer). OLD HEMIS nested format: university.code, studentStatus.code",
                dependsOn: 1,
                ported: true
            },
    {
                id: 7,
                category: "04.Talaba",
                name: "Talaba statusini tekshirish (Passport seriya)",
                method: "GET",
                url: "/app/rest/v2/services/student/validate",
                requiresAuth: true,
                params: {},
                inputFields: {
                    data: {
                        label: "PINFL yoki Passport seriya",
                        type: "text",
                        placeholder: "Masalan: A0939758 yoki 31507976020031",
                        default: "",
                        useConfigSerial: true,  // Config dan olinadi (passport seriya)
                        required: true
                    }
                },
                description: "Talabaning joriy statusini tekshirish. PINFL (14 xonali) yoki Passport seriya/raqami (masalan: A0939758) orqali. Response: not_active (topilmadi), active (faol), graduated (bitirgan)",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - StudentServiceController.java
            },
    {
                id: 8,
                category: "04.Talaba",
                name: "Talaba tasdiqlash (DTM verification)",
                method: "GET",
                url: "/app/rest/v2/services/student/verify",
                requiresAuth: true,
                params: {},
                inputFields: {
                    pinfl: {
                        label: "PINFL (JSHSHIR)",
                        type: "text",
                        placeholder: "14 xonali JSHSHIR raqami",
                        default: "",
                        useConfigPinfl: true,  // Config dan olinadi (newPinfl/oldPinfl)
                        required: true
                    }
                },
                description: "Talabaning DTM verification ballarini olish (hemishe_e_verification jadvalidan). Response: success, count, records (verification yozuvlari ro'yxati). Har bir record: points, paymentForm, educationType, university, educationYear, category.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - StudentServiceController.java + VerificationService.java
            },
    {
                id: 9,
                category: "04.Talaba",
                name: "Talaba GPA (O'rtacha baho)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EStudentGpa",
                requiresAuth: true,
                params: {
                    limit: "5",
                    offset: "0",
                    view: "eStudentGpa-view"
                },
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "text",
                        placeholder: "Nechta yozuv olish",
                        default: "5",
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "text",
                        placeholder: "Qayerdan boshlash",
                        default: "0",
                        required: false
                    },
                    view: {
                        label: "View nomi",
                        type: "text",
                        placeholder: "eStudentGpa-view",
                        default: "eStudentGpa-view",
                        required: false
                    }
                },
                description: "Talabalarning GPA (o'rtacha baho) ma'lumotlarini olish. Response: id, gpa, creditSum, subjects, debtSubjects, method, level (kurs), educationYear, studentId (talaba)",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - StudentGpaEntityController.java
            },
    {
                id: 10,
                category: "04.Talaba",
                name: "Talaba GPA yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EStudentGpa",
                requiresAuth: true,
                params: {},
                inputFields: {
                    studentId: {
                        label: "Talaba ID (UUID)",
                        type: "text",
                        placeholder: "Talaba UUID (Talaba yaratish dan olinadi)",
                        default: "fb08e7eb-49ef-4240-a2d9-99d09f02ac6a",
                        useStoredId: "createdStudentId",
                        required: true
                    },
                    educationYearCode: {
                        label: "Ta'lim yili kodi",
                        type: "text",
                        placeholder: "2024",
                        default: "2024",
                        required: false
                    },
                    levelCode: {
                        label: "Kurs darajasi kodi",
                        type: "text",
                        placeholder: "12 = 2-kurs",
                        default: "12",
                        required: false
                    },
                    gpa: {
                        label: "GPA qiymati",
                        type: "text",
                        placeholder: "4.0",
                        default: "3.85",
                        required: false
                    },
                    method: {
                        label: "Hisoblash usuli",
                        type: "text",
                        placeholder: "one_year yoki all_year",
                        default: "one_year",
                        required: false
                    },
                    creditSum: {
                        label: "Jami kredit",
                        type: "text",
                        placeholder: "47.0",
                        default: "50.0",
                        required: false
                    },
                    subjects: {
                        label: "Fan soni",
                        type: "text",
                        placeholder: "11",
                        default: "12",
                        required: false
                    },
                    debtSubjects: {
                        label: "Qarzdor fan soni",
                        type: "text",
                        placeholder: "0",
                        default: "0",
                        required: false
                    }
                },
                bodyGenerator: function(inputs) {
                    // CUBA REST v2 format: nested objects with "id" for ManyToOne references
                    // HEducationYear va HCourse - BaseCodeNameEntity: code = id (primary key)
                    // EStudent - StandardEntity: UUID = id
                    return {
                        studentId: { id: inputs.studentId },
                        educationYear: { id: inputs.educationYearCode },
                        level: { id: inputs.levelCode },
                        gpa: inputs.gpa,
                        method: inputs.method,
                        creditSum: inputs.creditSum,
                        subjects: parseInt(inputs.subjects) || 0,
                        debtSubjects: parseInt(inputs.debtSubjects) || 0
                    };
                },
                description: "Yangi GPA yozuvini yaratish. studentId #1 Talaba yaratish dan avtomatik olinadi. Request: studentId, educationYear, level, gpa, method, creditSum, subjects, debtSubjects.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - StudentGpaEntityController.java
            },
    {
                id: 11,
                category: "04.Talaba",
                name: "Talaba GPA servis (UPSERT)",
                method: "POST",
                url: "/app/rest/v2/services/student/gpa",
                requiresAuth: true,
                params: {},
                inputFields: {
                    studentId: {
                        label: "Talaba ID (UUID)",
                        type: "text",
                        placeholder: "Talaba UUID (#1 dan olinadi)",
                        default: "fb08e7eb-49ef-4240-a2d9-99d09f02ac6a",
                        useStoredId: "createdStudentId",
                        required: true
                    },
                    educationYearCode: {
                        label: "Ta'lim yili kodi",
                        type: "text",
                        placeholder: "2024",
                        default: "2024",
                        required: false
                    },
                    levelCode: {
                        label: "Kurs darajasi kodi",
                        type: "text",
                        placeholder: "12 = 2-kurs",
                        default: "12",
                        required: false
                    },
                    gpa: {
                        label: "GPA qiymati",
                        type: "text",
                        placeholder: "4.0",
                        default: "3.85",
                        required: false
                    },
                    method: {
                        label: "Hisoblash usuli",
                        type: "text",
                        placeholder: "one_year yoki all_year",
                        default: "one_year",
                        required: false
                    },
                    creditSum: {
                        label: "Jami kredit",
                        type: "text",
                        placeholder: "47.0",
                        default: "50.0",
                        required: false
                    },
                    subjects: {
                        label: "Fan soni",
                        type: "text",
                        placeholder: "11",
                        default: "12",
                        required: false
                    },
                    debtSubjects: {
                        label: "Qarzdor fan soni",
                        type: "text",
                        placeholder: "0",
                        default: "0",
                        required: false
                    }
                },
                bodyGenerator: function(inputs) {
                    // Service format: {"gpa": {...}} wrapper bilan (Entity POST dan farqli)
                    // CUBA REST v2: ManyToOne references uchun "id" ishlatiladi
                    return {
                        gpa: {
                            studentId: { id: inputs.studentId },
                            educationYear: { id: inputs.educationYearCode },
                            level: { id: inputs.levelCode },
                            gpa: inputs.gpa,
                            method: inputs.method,
                            creditSum: inputs.creditSum,
                            subjects: parseInt(inputs.subjects) || 0,
                            debtSubjects: parseInt(inputs.debtSubjects) || 0
                        }
                    };
                },
                description: "Talaba GPA servis (UPSERT). studentId #1 dan olinadi. Mavjud bo'lsa yangilaydi, bo'lmasa yaratadi. Request: {gpa: {...}} wrapper bilan.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - StudentServiceController.java
            },
    {
                id: 12,
                category: "04.Talaba",
                name: "Talaba shartnoma ma'lumotlari (Contract Info)",
                method: "GET",
                url: "/app/rest/v2/services/student/contractInfo",
                requiresAuth: true,
                params: {
                    pinfl: "{pinfl}"
                },
                inputFields: {
                    pinfl: {
                        label: "PINFL",
                        type: "text",
                        placeholder: "Talaba PINFL",
                        default: "61111065190052",
                        required: true,
                        useConfigPinfl: true
                    }
                },
                description: "Talabaning shartnoma ma'lumotlarini api.hemis.uz dan olish. PINFL bo'yicha talabaning to'lov shartnomasi, GPA, qarzdorligi va boshqa ma'lumotlarini qaytaradi. Bu endpoint api.hemis.uz ga proxy so'rov yuboradi.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - StudentServiceController.java via HemisApiService.java
            },
    {
                id: 13,
                category: "04.Talaba",
                name: "Talaba Meta - Ro'yxat (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EStudentMeta",
                requiresAuth: true,
                params: {
                    limit: "10",
                    offset: "0",
                    returnNulls: "false"
                },
                description: "Barcha talaba meta ma'lumotlarini ro'yxatini olish (pagination bilan). CUBA Platform REST API compatible endpoint. limit va offset parametrlari bilan sahifalash mumkin.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - StudentMetaEntityController.java
            },
    {
                id: 14,
                category: "04.Talaba",
                name: "Talaba Meta - Bitta olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EStudentMeta/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik to'ldiriladi",
                        defaultNew: "4c453de6-5386-9d8e-a298-4bd3166a7ded",
                        defaultOld: "4c453de6-5386-9d8e-a298-4bd3166a7ded",
                        useStoredId: "createdStudentMetaId",
                        required: true
                    }
                },
                description: "ID bo'yicha bitta talaba meta ma'lumotlarini olish. CUBA Platform REST API compatible endpoint.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - StudentMetaEntityController.java
            },
    {
                id: 15,
                category: "04.Talaba",
                name: "Talaba Meta - Yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EStudentMeta",
                requiresAuth: true,
                inputFields: {
                    university: {
                        label: "Universitet kodi",
                        type: "text",
                        placeholder: "401=Yangi, 351=Eski",
                        defaultNew: "401",
                        defaultOld: "351",
                        required: true
                    },
                    studentId: {
                        label: "Talaba ID (UUID)",
                        type: "text",
                        placeholder: "#1 POST dan avtomatik to'ldiriladi",
                        default: "",
                        useStoredId: "createdStudentId",
                        required: false
                    },
                    education_type: {
                        label: "Ta'lim turi",
                        type: "select",
                        options: [
                            {value: "11", label: "11 - Bakalavr"},
                            {value: "12", label: "12 - Magistr"},
                            {value: "13", label: "13 - Doktorant"}
                        ],
                        default: "11",
                        required: true
                    },
                    education_form: {
                        label: "Ta'lim shakli",
                        type: "select",
                        options: [
                            {value: "11", label: "11 - Kunduzgi"},
                            {value: "12", label: "12 - Sirtqi"},
                            {value: "13", label: "13 - Kechki"}
                        ],
                        default: "11",
                        required: true
                    },
                    education_year: {
                        label: "Ta'lim yili",
                        type: "select",
                        options: [
                            {value: "2024", label: "2024-2025"},
                            {value: "2023", label: "2023-2024"},
                            {value: "2022", label: "2022-2023"}
                        ],
                        default: "2024",
                        required: true
                    },
                    student_status: {
                        label: "Talaba holati",
                        type: "select",
                        options: [
                            {value: "11", label: "11 - Faol"},
                            {value: "12", label: "12 - Chetlashgan"},
                            {value: "10", label: "10 - Boshqa"}
                        ],
                        default: "11",
                        required: true
                    },
                    groupName: {
                        label: "Guruh nomi",
                        type: "text",
                        placeholder: "Test guruh",
                        default: "Test guruh",
                        required: false
                    },
                    active: {
                        label: "Faol",
                        type: "select",
                        options: [
                            {value: "true", label: "Ha"},
                            {value: "false", label: "Yo'q"}
                        ],
                        default: "true",
                        required: false
                    }
                },
                bodyTemplate: {
                    _university: "{university}",
                    _student: "{studentId}",
                    _education_type: "{education_type}",
                    _education_form: "{education_form}",
                    _education_year: "{education_year}",
                    _student_status: "{student_status}",
                    groupName: "{groupName}",
                    active: true
                },
                description: "Yangi talaba meta yaratish. CUBA Platform REST API compatible endpoint. uId avtomatik generatsiya qilinadi.",
                dependsOn: 1,
                ported: true,  // ✅ Controller ported - StudentMetaEntityController.java
                storeResultId: "createdStudentMetaId"
            },
    {
                id: 16,
                category: "04.Talaba",
                name: "Talaba Meta - Yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EStudentMeta/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik to'ldiriladi",
                        defaultNew: "4c453de6-5386-9d8e-a298-4bd3166a7ded",
                        defaultOld: "4c453de6-5386-9d8e-a298-4bd3166a7ded",
                        useStoredId: "createdStudentMetaId",
                        required: true
                    },
                    groupName: {
                        label: "Guruh nomi",
                        type: "text",
                        placeholder: "Yangilangan guruh",
                        default: "Yangilangan guruh",
                        required: false
                    },
                    active: {
                        label: "Faol",
                        type: "select",
                        options: [
                            {value: "true", label: "Ha"},
                            {value: "false", label: "Yo'q"}
                        ],
                        default: "true",
                        required: false
                    }
                },
                bodyTemplate: {
                    groupName: "{groupName}",
                    active: true
                },
                description: "Talaba meta ma'lumotlarini yangilash (partial update). CUBA Platform REST API compatible endpoint.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - StudentMetaEntityController.java
            },
    {
                id: 17,
                category: "04.Talaba",
                name: "Talaba Meta - O'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EStudentMeta/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (UUID)",
                        type: "text",
                        placeholder: "POST dan avtomatik to'ldiriladi",
                        defaultNew: "4c453de6-5386-9d8e-a298-4bd3166a7ded",
                        defaultOld: "4c453de6-5386-9d8e-a298-4bd3166a7ded",
                        useStoredId: "createdStudentMetaId",
                        required: true
                    }
                },
                description: "Talaba meta ma'lumotlarini o'chirish (soft delete). CUBA Platform REST API compatible endpoint. Faqat deleteTs o'rnatiladi, fizik o'chirish bo'lmaydi.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - StudentMetaEntityController.java
            },
    {
                id: 18,
                category: "04.Talaba",
                name: "🗑️ TEST YAKUNLASH - Talabani o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EStudent/{entityId}",
                requiresAuth: true,
                params: {},
                inputFields: {
                    entityId: {
                        label: "Talaba ID (UUID)",
                        type: "text",
                        placeholder: "POST da yaratilgan talaba ID",
                        default: "",
                        useStoredId: "createdStudentId",
                        required: true
                    }
                },
                description: "⚠️ DIQQAT: Bu oxirgi qadam! POST (#1) da yaratilgan test talabani o'chirish. Soft delete - faqat delete_ts belgilanadi, ma'lumot bazadan fizik o'chirilmaydi. Barcha testlar tugagandan keyin ishlating!",
                dependsOn: 1,
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_04;
}
