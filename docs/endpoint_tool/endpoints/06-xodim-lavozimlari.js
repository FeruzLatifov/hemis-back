// 06.Xodim lavozimlari endpoints
// Auto-generated - DO NOT EDIT DIRECTLY
// Bu faylni o'zgartirganingizda endpoint_tester.html ni yangilang

const endpoints_06 = [
    // ============================================
    // 06.Xodim lavozimlari (29 endpoint)
    // ============================================
    {
                id: 1,
                category: "06.Xodim lavozimlari",
                name: "Barcha xodim lavozimlari ro'yxati (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EEmployeeJobs",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        placeholder: "10",
                        default: "5",
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
                        label: "View (nested objects uchun)",
                        type: "text",
                        placeholder: "eEmployeeJob-view",
                        default: "",
                        required: false
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "false",
                        required: false
                    }
                },
                description: "Barcha xodim lavozimlari ro'yxatini olish. view=eEmployeeJob-view qo'shsangiz nested objectlar qaytadi. Birinchi element ID si keyingi endpointlar uchun avtomatik to'ldiriladi.",
                dependsOn: 1,
                ported: true,
                storeFirstId: "employeeJobId"  // Birinchi elementdan ID olish
            },
    {
                id: 2,
                category: "06.Xodim lavozimlari",
                name: "Xodim lavozimi olish (Entity API)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EEmployeeJobs/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (avval #1 ni ishga tushiring)",
                        type: "text",
                        placeholder: "UUID - #1 dan olinadi",
                        default: "",
                        required: true,
                        useStoredId: "employeeJobId"
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
                description: "Xodim lavozimi ma'lumotlarini ID bo'yicha olish. ⚠️ Avval #1 ni ishga tushiring - ID avtomatik to'ldiriladi.",
                dependsOn: 1,
                ported: true
            },
    {
                id: 3,
                category: "06.Xodim lavozimlari",
                name: "Xodim lavozimini yangilash (Entity API)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EEmployeeJobs/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Entity ID (avval #1 ni ishga tushiring)",
                        type: "text",
                        placeholder: "UUID - #1 dan olinadi",
                        default: "",
                        required: true,
                        useStoredId: "employeeJobId"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "false",
                        required: false
                    }
                },
                bodyGenerator: () => ({
                    "_employeeStatus": "12",
                    "contractNumber": "UPD-" + String(Math.floor(Math.random() * 9000) + 1000),
                    "tag": "yangilangan-" + Date.now()
                }),
                description: "Xodim lavozimi ma'lumotlarini yangilash. ⚠️ Avval #1 ni ishga tushiring - ID avtomatik to'ldiriladi.",
                dependsOn: 1,
                ported: true
            },
    {
                id: 5,
                category: "06.Xodim lavozimlari",
                name: "Xodim kodi orqali ish joylarini olish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EEmployeeJobs/search",
                requiresAuth: true,
                inputFields: {
                    employeeCode: {
                        label: "Xodim kodi (employee.code)",
                        type: "text",
                        placeholder: "3011911003",
                        default: "3011911003",
                        required: true
                    },
                    view: {
                        label: "View",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "eEmployeeJob-view", label: "eEmployeeJob-view (nested)"}],
                        default: "eEmployeeJob-view",
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
                bodyTemplate: {
                    "filter": {
                        "conditions": [
                            {
                                "property": "employee.code",
                                "operator": "=",
                                "value": "{employeeCode}"
                            }
                        ]
                    }
                },
                description: "Xodim kodi bo'yicha barcha ish joylarini olish.",
                ported: true
            },
    {
                id: 6,
                category: "06.Xodim lavozimlari",
                name: "Barcha xodim holatlari (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 50,
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: 0,
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
                description: "Xodim holatlari klassifikatori: 11=Ishlamoqda, 12=Ta'tilda, 13=Xizmat safarida, 14=Bo'shagan",
                ported: true,
                storeFirstId: "employeeStatusTypeCode"
            },
    {
                id: 7,
                category: "06.Xodim lavozimlari",
                name: "Xodim holati olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Holat kodi (11, 12, 13, 14)",
                        type: "text",
                        default: "11",
                        required: true,
                        useStoredId: "employeeStatusTypeCode"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Bitta xodim holatini kod bo'yicha olish. Kodlar: 11=Ishlamoqda, 12=Ta'tilda, 13=Xizmat safarida, 14=Bo'shagan",
                dependsOn: 6,
                ported: true
            },
    {
                id: 8,
                category: "06.Xodim lavozimlari",
                name: "Barcha xodim turlari (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 50,
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: 0,
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
                description: "Xodim turlari klassifikatori: 10=Boshqa, 11=Administrativ-boshqaruv, 12=Professor-o'qituvchi, 13=O'quv-yordamchi va texnik, 14=Xizmat ko'rsatuvchi",
                ported: true,
                storeFirstId: "employeeTypeCode"
            },
    {
                id: 9,
                category: "06.Xodim lavozimlari",
                name: "Xodim turini olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Tur kodi (10, 11, 12, 13, 14)",
                        type: "text",
                        default: "12",
                        required: true,
                        useStoredId: "employeeTypeCode"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Bitta xodim turini kod bo'yicha olish. Kodlar: 10=Boshqa, 11=Administrativ, 12=Professor, 13=Texnik, 14=Xizmat",
                dependsOn: 8,
                ported: true
            },
    {
                id: 10,
                category: "06.Xodim lavozimlari",
                name: "Barcha xodim stavkalari (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeRate",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 50,
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: 0,
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
                description: "Xodim mehnat stavkalari klassifikatori: 11=1,00, 12=0,75, 13=0,50, 14=0,25, 15=0,30, 16=0,20, 17=0,15, 18=0,10, 19=0,05",
                ported: true,
                storeFirstId: "employeeRateCode"
            },
    {
                id: 11,
                category: "06.Xodim lavozimlari",
                name: "Xodim stavkasini olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeRate/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Stavka kodi (11-19)",
                        type: "text",
                        default: "11",
                        required: true,
                        useStoredId: "employeeRateCode"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Bitta stavkani kod bo'yicha olish. Kodlar: 11=1,00, 12=0,75, 13=0,50, 14=0,25, 15=0,30, 16=0,20, 17=0,15, 18=0,10, 19=0,05",
                dependsOn: 10,
                ported: true
            },
    {
                id: 12,
                category: "06.Xodim lavozimlari",
                name: "Barcha xodim lavozimlari (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HTeacherPositionType",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 50,
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: 0,
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
                description: "Xodim lavozimlari klassifikatori (227 ta): 11=Stajer-o'qituvchi, 12=O'qituvchi, 13=Katta o'qituvchi, 14=Dotsent, 15=Professor va boshqalar",
                ported: true,
                storeFirstId: "positionTypeCode"
            },
    {
                id: 13,
                category: "06.Xodim lavozimlari",
                name: "Xodim lavozimini olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HTeacherPositionType/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Lavozim kodi",
                        type: "text",
                        default: "11",
                        required: true,
                        useStoredId: "positionTypeCode"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Bitta lavozimni kod bo'yicha olish. Masalan: 11=Stajer-o'qituvchi, 14=Dotsent, 15=Professor",
                dependsOn: 12,
                ported: true
            },
    {
                id: 14,
                category: "06.Xodim lavozimlari",
                name: "Barcha xodim ish joylari (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EEmployeeJob",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 50,
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: 0,
                        required: false
                    },
                    view: {
                        label: "View",
                        type: "text",
                        default: "eEmployeeJob-view",
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
                description: "Xodimlarning ish joylari (lavozim, OTM, bo'lim, stavka, shakl, holat). UUID asosida.",
                ported: true,
                storeFirstId: "employeeJobId"
            },
    {
                id: 15,
                category: "06.Xodim lavozimlari",
                name: "Xodim ish joyini olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EEmployeeJob/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Ish joyi UUID",
                        type: "text",
                        default: "",
                        required: true,
                        useStoredId: "employeeJobId"
                    },
                    view: {
                        label: "View",
                        type: "text",
                        default: "eEmployeeJob-view",
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
                description: "Bitta xodim ish joyini UUID bo'yicha olish. Ma'lumotlar: lavozim, OTM, bo'lim, stavka, shartnoma.",
                dependsOn: 14,
                ported: true
            },
    {
                id: 16,
                category: "06.Xodim lavozimlari",
                name: "Xodim ish joyini yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EEmployeeJobs",
                requiresAuth: true,
                inputFields: {
                    _employee: {
                        label: "Xodim UUID (Employee #2)",
                        type: "text",
                        default: "bf9ac27a-4b88-aa2a-ec12-1983afb59a8b",
                        required: true
                    },
                    _university: {
                        label: "OTM kodi",
                        type: "text",
                        default: "305",
                        required: true
                    },
                    _department: {
                        label: "Bo'lim kodi",
                        type: "text",
                        default: "305-212",
                        required: false
                    },
                    _employeeType: {
                        label: "Xodim turi kodi (12=Professor-o'qituvchi)",
                        type: "text",
                        default: "12",
                        required: false
                    },
                    _employeePosition: {
                        label: "Lavozim kodi (14=Dotsent)",
                        type: "text",
                        default: "14",
                        required: false
                    },
                    _employeeRate: {
                        label: "Stavka kodi (11=1.00 stavka)",
                        type: "text",
                        default: "11",
                        required: false
                    },
                    _employeeForm: {
                        label: "Ish shakli kodi",
                        type: "text",
                        default: "11",
                        required: false
                    },
                    _employeeStatus: {
                        label: "Holat kodi (11=Ishlamoqda)",
                        type: "text",
                        default: "11",
                        required: false
                    },
                    jobStartDate: {
                        label: "Ish boshlash sanasi",
                        type: "text",
                        default: "2025-01-01",
                        required: false
                    },
                    contractNumber: {
                        label: "Shartnoma raqami",
                        type: "text",
                        default: "TEST-001",
                        required: false
                    },
                    decreeNumber: {
                        label: "Buyruq raqami",
                        type: "text",
                        default: "TEST-D001",
                        required: false
                    }
                },
                description: "Yangi xodim ish joyini yaratish. _employee (UUID), OTM, bo'lim, lavozim, stavka va boshqa ma'lumotlar.",
                ported: true,
                storeFirstId: "createdEmployeeJobId",
                bodyTemplate: {
                    "_employee": {"id": "{_employee}"},
                    "_university": {"code": "{_university}"},
                    "_department": {"code": "{{_department}}"},
                    "_employeeType": {"code": "{{_employeeType}}"},
                    "_teacherPositionType": {"code": "{{_employeePosition}}"},
                    "_employeeRate": {"code": "{{_employeeRate}}"},
                    "_employmentForm": {"code": "{{_employeeForm}}"},
                    "_employeeStatus": {"code": "{{_employeeStatus}}"},
                    "jobStartDate": "{{jobStartDate}}",
                    "contractNumber": "{{contractNumber}}",
                    "decreeNumber": "{{decreeNumber}}"
                }
            },
    {
                id: 17,
                category: "06.Xodim lavozimlari",
                name: "Xodim ish joyini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EEmployeeJobs/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Ish joyi UUID (Employee #1 - boshqa xodim)",
                        type: "text",
                        default: "bd5f40d5-771e-62eb-db48-c67d5317e429",
                        required: true,
                        useStoredId: "createdEmployeeJobId"
                    },
                    _employeeStatus: {
                        label: "Xodim holati kodi (11=Ishlayapti, 12=Bo'shatilgan)",
                        type: "text",
                        default: "11",
                        required: false
                    },
                    _employeeRate: {
                        label: "Stavka kodi (11=1.0, 12=0.75, 13=0.5, 14=0.25)",
                        type: "text",
                        default: "",
                        required: false
                    },
                    jobEndDate: {
                        label: "Ish tugash sanasi (YYYY-MM-DD)",
                        type: "text",
                        default: "2025-12-31",
                        required: false
                    },
                    contractNumber: {
                        label: "Shartnoma raqami",
                        type: "text",
                        default: "UPD-001",
                        required: false
                    }
                },
                description: "Mavjud xodim ish joyini yangilash (partial update). entityId #16 POST dan avtomatik olinadi. Faqat o'zgartirmoqchi bo'lgan maydonlarni yuboring.",
                dependsOn: 16,
                ported: true,
                bodyTemplate: {
                    "_employeeStatus": {"code": "{{_employeeStatus}}"},
                    "_employeeRate": {"code": "{{_employeeRate}}"},
                    "jobEndDate": "{{jobEndDate}}",
                    "contractNumber": "{{contractNumber}}"
                }
            },
    {
                id: 18,
                category: "06.Xodim lavozimlari",
                name: "Xodim ish joyini yaratish - YANGI (Service API)",
                method: "POST",
                url: "/app/rest/v2/services/teacher/addJob",
                requiresAuth: true,
                inputFields: {
                    employeeId: {
                        label: "Xodim UUID",
                        type: "text",
                        default: "b2a51826-9870-4b2b-e633-94c6d9958fa9",
                        required: true,
                        placeholder: "Xodim (ETeacher) UUID si"
                    },
                    universityCode: {
                        label: "Universitet kodi",
                        type: "text",
                        default: "401",
                        required: true,
                        placeholder: "401"
                    },
                    departmentCode: {
                        label: "Bo'lim kodi",
                        type: "text",
                        default: "401-102",
                        required: true,
                        placeholder: "401-102"
                    },
                    employeeFormCode: {
                        label: "Shtat shakli kodi (11=asosiy, 12=ichki, 13=tashqi, 14=soatbay)",
                        type: "text",
                        default: "11",
                        required: true
                    },
                    employeeStatusCode: {
                        label: "Ish holati kodi (11=ishlamoqda, 12=ta'tilda, 13=bo'shatilgan)",
                        type: "text",
                        default: "11",
                        required: true
                    },
                    employeeTypeCode: {
                        label: "Xodim turi kodi (11=administrativ, 12=professor, 13=texnik)",
                        type: "text",
                        default: "11",
                        required: true
                    },
                    employeeRateCode: {
                        label: "Stavka kodi (11=1.0, 12=0.75, 13=0.5, 14=0.25)",
                        type: "text",
                        default: "13",
                        required: true
                    },
                    employeePositionCode: {
                        label: "Lavozim kodi (11=Stajer-o'qituvchi, 12=O'qituvchi, 14=Dotsent, 15=Professor)",
                        type: "text",
                        default: "11",
                        required: true
                    },
                    jobStartDate: {
                        label: "Ish boshlash sanasi (YYYY-MM-DD)",
                        type: "text",
                        default: "2020-01-06",
                        required: true
                    },
                    jobEndDate: {
                        label: "Ish tugash sanasi (YYYY-MM-DD)",
                        type: "text",
                        default: "2025-12-31",
                        required: false
                    }
                },
                description: "Xodimga yangi lavozim qo'shish (Service API). Agar xodim asosiy shtatda ishlayotgan bo'lsa va yangi job ham asosiy shtat bo'lsa - xato qaytadi.",
                ported: true,
                bodyTemplate: {
                    "job": {
                        "employee": {"id": "{{employeeId}}"},
                        "university": {"code": "{{universityCode}}"},
                        "department": {"code": "{{departmentCode}}"},
                        "employeeForm": {"code": "{{employeeFormCode}}"},
                        "employeeStatus": {"code": "{{employeeStatusCode}}"},
                        "employeeType": {"code": "{{employeeTypeCode}}"},
                        "employeeRate": {"code": "{{employeeRateCode}}"},
                        "employeePosition": {"code": "{{employeePositionCode}}"},
                        "jobStartDate": "{{jobStartDate}}",
                        "jobEndDate": "{{jobEndDate}}"
                    }
                }
            },
    {
                id: 19,
                category: "06.Xodim lavozimlari",
                name: "Xodim ish joyini o'chirish - YANGI (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EEmployeeJobs/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Ish joyi UUID (o'chirish uchun)",
                        type: "text",
                        default: "",
                        required: true,
                        placeholder: "UUID - avval #1 dan oling",
                        useStoredId: "employeeJobId"
                    }
                },
                description: "Xodim ish joyini o'chirish (soft delete). O'chirilgan yozuv bazadan o'chirilmaydi, faqat delete_ts belgilanadi. 200=muvaffaqiyatli (bo'sh response), 404=topilmadi.",
                ported: true
            },
    // ============================================
    // employeeForm - Xodim mehnat shakllari (2 endpoint)
    // ============================================
    {
                id: 20,
                category: "06.Xodim lavozimlari",
                name: "Barcha xodim mehnat shakllari (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeForm",
                requiresAuth: true,
                inputFields: {
                    limit: {
                        label: "Limit",
                        type: "number",
                        default: 50,
                        required: false
                    },
                    offset: {
                        label: "Offset",
                        type: "number",
                        default: 0,
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
                description: "Xodim mehnat shakllari klassifikatori: 11=Asosiy shtat, 12=Ichki o'rindoshlik, 13=Tashqi o'rindoshlik, 14=Soatbay",
                ported: true,
                storeFirstId: "employeeFormCode"
            },
    {
                id: 21,
                category: "06.Xodim lavozimlari",
                name: "Xodim mehnat shaklini olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeForm/{entityId}",
                requiresAuth: true,
                inputFields: {
                    entityId: {
                        label: "Shakl kodi (11, 12, 13, 14)",
                        type: "text",
                        default: "11",
                        required: true,
                        useStoredId: "employeeFormCode"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Bitta mehnat shaklini kod bo'yicha olish. Kodlar: 11=Asosiy shtat, 12=Ichki o'rindoshlik, 13=Tashqi o'rindoshlik, 14=Soatbay",
                dependsOn: 20,
                ported: true
            },
    // ============================================
    // /search endpointlar - CUBA Platform compatible
    // ============================================
    {
                id: 22,
                category: "06.Xodim lavozimlari",
                name: "Xodim holatlarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (JSON string)",
                        type: "text",
                        default: "",
                        required: false,
                        placeholder: "{\"conditions\":[...]}"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Xodim holatlari klassifikatorini qidirish (CUBA /search endpoint). GET parametrlari orqali filter yuborish mumkin.",
                ported: true
            },
    {
                id: 23,
                category: "06.Xodim lavozimlari",
                name: "Xodim holatlarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType/search",
                requiresAuth: true,
                inputFields: {
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                bodyTemplate: {
                    "filter": {
                        "conditions": []
                    }
                },
                description: "Xodim holatlari klassifikatorini JSON filter orqali qidirish. Bo'sh filter barcha yozuvlarni qaytaradi.",
                ported: true
            },
    {
                id: 24,
                category: "06.Xodim lavozimlari",
                name: "Xodim stavkalarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeRate/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (JSON string)",
                        type: "text",
                        default: "",
                        required: false,
                        placeholder: "{\"conditions\":[...]}"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Xodim stavkalari klassifikatorini qidirish (CUBA /search endpoint). GET parametrlari orqali filter yuborish mumkin.",
                ported: true
            },
    {
                id: 25,
                category: "06.Xodim lavozimlari",
                name: "Xodim stavkalarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeRate/search",
                requiresAuth: true,
                inputFields: {
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                bodyTemplate: {
                    "filter": {
                        "conditions": []
                    }
                },
                description: "Xodim stavkalari klassifikatorini JSON filter orqali qidirish. Bo'sh filter barcha yozuvlarni qaytaradi.",
                ported: true
            },
    {
                id: 26,
                category: "06.Xodim lavozimlari",
                name: "Xodim lavozimlarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HTeacherPositionType/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (JSON string)",
                        type: "text",
                        default: "",
                        required: false,
                        placeholder: "{\"conditions\":[...]}"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Xodim lavozimlari klassifikatorini qidirish (CUBA /search endpoint). 227 ta lavozim mavjud.",
                ported: true
            },
    {
                id: 27,
                category: "06.Xodim lavozimlari",
                name: "Xodim lavozimlarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HTeacherPositionType/search",
                requiresAuth: true,
                inputFields: {
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                bodyTemplate: {
                    "filter": {
                        "conditions": []
                    }
                },
                description: "Xodim lavozimlari klassifikatorini JSON filter orqali qidirish. Bo'sh filter barcha 227 ta lavozimni qaytaradi.",
                ported: true
            },
    {
                id: 28,
                category: "06.Xodim lavozimlari",
                name: "Mehnat shakllarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeForm/search",
                requiresAuth: true,
                inputFields: {
                    filter: {
                        label: "Filter (JSON string)",
                        type: "text",
                        default: "",
                        required: false,
                        placeholder: "{\"conditions\":[...]}"
                    },
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                description: "Mehnat shakllari klassifikatorini qidirish (CUBA /search endpoint). 11=Asosiy, 12=Ichki, 13=Tashqi, 14=Soatbay.",
                ported: true
            },
    {
                id: 29,
                category: "06.Xodim lavozimlari",
                name: "Mehnat shakllarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeForm/search",
                requiresAuth: true,
                inputFields: {
                    returnNulls: {
                        label: "Return Nulls",
                        type: "select",
                        options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                        default: "",
                        required: false
                    }
                },
                bodyTemplate: {
                    "filter": {
                        "conditions": []
                    }
                },
                description: "Mehnat shakllari klassifikatorini JSON filter orqali qidirish. Bo'sh filter 4 ta shaklni qaytaradi.",
                ported: true
            }
];

// Export for module bundler (optional)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_06;
}
