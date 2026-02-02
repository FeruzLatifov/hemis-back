        const endpoints = [

            // ============================================
            // 01.Token (3 endpoint)
            // ============================================
            {
                id: 1,
                category: "01.Token",
                name: "Token olish (Password Grant)",
                method: "POST",
                url: "/app/rest/v2/oauth/token",  // Same URL for both (backward compatibility)
                requiresAuth: false,
                auth: "basic",
                contentType: "multipart",
                params: {},
                body: {
                grant_type: "password",
                username: "{username}",
                password: "{password}"
                },
                description: "Username va password bilan OAuth2 token olish",
                ported: true  // ✅ Controller ported
                }
            {
                id: 2,
                category: "01.Token",
                name: "Token yangilash (Refresh Grant)",
                method: "POST",
                url: "/app/rest/v2/oauth/token",  // Same URL for both
                requiresAuth: false,
                auth: "basic",
                contentType: "multipart",
                params: {},
                body: {
                grant_type: "refresh_token",
                refresh_token: "{refresh_token}"
                },
                description: "Refresh token bilan yangi access token olish",
                dependsOn: 1,
                ported: true  // ✅ Controller ported
                }
            {
                id: 3,
                category: "01.Token",
                name: "Foydalanuvchi ma'lumotlari",
                method: "GET",
                url: "/app/rest/v2/userInfo",  // ✅ Old-hemis URL (backward compatible)
                requiresAuth: true,
                params: {},
                description: "Joriy authenticated user ma'lumotlari (old-hemis format)",
                dependsOn: 1,
                ported: true  // ✅ Controller ported (supports both URLs)
                }

            // ============================================
            // 02.Captcha (2 endpoint)
            // ============================================
            {
                id: 4,
                category: "02.Captcha",
                name: "getNumericCaptcha (Raqamli captcha)",
                method: "GET",
                url: "/app/rest/v2/services/captcha/getNumericCaptcha",
                requiresAuth: true,  // Old-hemis uchun auth kerak
                dependsOn: 1,
                params: {},
                description: "5 xonali numeric captcha generatsiya qilish (PNG base64)",
                ported: true  // ✅ Controller ported, 100% old-hemis format {id, image}
                }
            {
                id: 5,
                category: "02.Captcha",
                name: "getArithmeticCaptcha (Arifmetik captcha)",
                method: "GET",
                url: "/app/rest/v2/services/captcha/getArithmeticCaptcha",
                requiresAuth: true,  // Old-hemis uchun auth kerak
                dependsOn: 1,
                params: {},
                description: "Arifmetik ifoda captcha (masalan: '5 + 3 = ?') - PUBLIC endpoint",
                ported: true  // ✅ Controller ported, 100% old-hemis format {id, image}
                }

            // ============================================
            // 03.Passport ma'lumotlari (4 endpoint)
            // ============================================
            {
                id: 6,
                category: "03.Passport ma'lumotlari",
                name: "Passport ma'lumotlarni olish (Pinfl va Seria nomer bilan)",
                method: "GET",
                url: "/app/rest/v2/services/passport-data/getDataBySN",
                requiresAuth: true,
                params: {
                pinfl: "{pinfl}",
                seriaNumber: "{seriaNumber}",
                captchaId: "{captchaId}",
                captchaValue: "{captchaValue}"
                },
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                placeholder: "14 raqamli PINFL",
                default: "",  // Configdan olinadi (newPinfl/oldPinfl)
                useConfigPinfl: true,
                required: true
                },
                seriaNumber: {
                label: "Passport Seria/Raqam",
                type: "text",
                placeholder: "AB1234567",
                default: "",  // Configdan olinadi
                useConfigSerial: true,
                required: true
                },
                captchaId: {
                label: "Captcha ID",
                type: "text",
                placeholder: "UUID format (avtomatik to'ldiriladi)",
                default: "",
                required: true,
                hint: "⚠️ Yangi captcha olish uchun tugmani bosing",
                readonly: true,
                needsCaptcha: true  // Show get captcha button
                },
                captchaValue: {
                label: "Captcha Qiymati",
                type: "text",
                placeholder: "Captcha rasmidagi raqamni kiriting",
                default: "",
                required: true,
                hint: "⚠️ Yuqoridagi rasmda ko'rsatilgan raqamni kiriting",
                needsCaptcha: true  // Depends on captcha
                }
                },
                description: "PINFL va passport seria-raqam orqali GUVD bazasidan passport ma'lumotlarini olish (captcha talab qilinadi)",
                dependsOn: 1,  // Requires authentication (token)
                ported: true  // ✅ Controller ported, 100% old-hemis format {success, data, address}
                }
            {
                id: 7,
                category: "03.Passport ma'lumotlari",
                name: "Passport ma'lumotlarni olish (Seria nomer va tug'ilgan kun bilan)",
                method: "GET",
                url: "/app/rest/v2/services/passport-data/getDataBySNBirthdate",
                requiresAuth: true,
                params: {
                seriaNumber: "{seriaNumber}",
                birthdate: "{birthdate}",
                captchaId: "{captchaId}",
                captchaValue: "{captchaValue}"
                },
                inputFields: {
                seriaNumber: {
                label: "Passport Seria/Raqam",
                type: "text",
                placeholder: "AB1234567",
                default: "",  // Configdan olinadi
                useConfigSerial: true,
                required: true
                },
                birthdate: {
                label: "Tug'ilgan sana",
                type: "text",
                placeholder: "YYYY-MM-DD",
                default: "",  // Configdan olinadi
                useConfigBirthdate: true,
                required: true
                },
                captchaId: {
                label: "Captcha ID",
                type: "text",
                placeholder: "UUID format (avtomatik to'ldiriladi)",
                default: "",
                required: true,
                hint: "⚠️ Yangi captcha olish uchun tugmani bosing",
                readonly: true,
                needsCaptcha: true  // Show get captcha button
                },
                captchaValue: {
                label: "Captcha Qiymati",
                type: "text",
                placeholder: "Captcha rasmidagi raqamni kiriting",
                default: "",
                required: true,
                hint: "⚠️ Yuqoridagi rasmda ko'rsatilgan raqamni kiriting",
                needsCaptcha: true  // Depends on captcha
                }
                },
                description: "Passport seria-raqam va tug'ilgan sana orqali GUVD bazasidan passport ma'lumotlarini olish (PINFL talab qilinmaydi, captcha talab qilinadi)",
                dependsOn: 1,  // Requires authentication (token)
                ported: true  // ✅ Controller ported, 100% old-hemis format {success, data, address}
                }
            {
                id: 8,
                category: "03.Passport ma'lumotlari",
                name: "Passport ma'lumotlarni olish (Pinfl va tug'ilgan kun bilan)",
                method: "GET",
                url: "/app/rest/v2/services/passport-data/getDataByPinflBirthdate",
                requiresAuth: true,
                params: {
                pinfl: "{pinfl}",
                birthdate: "{birthdate}",
                captchaId: "{captchaId}",
                captchaValue: "{captchaValue}"
                },
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                placeholder: "14 raqamli PINFL",
                default: "",  // Configdan olinadi (newPinfl/oldPinfl)
                useConfigPinfl: true,
                required: true
                },
                birthdate: {
                label: "Tug'ilgan sana",
                type: "text",
                placeholder: "YYYY-MM-DD",
                default: "",  // Configdan olinadi
                useConfigBirthdate: true,
                required: true
                },
                captchaId: {
                label: "Captcha ID",
                type: "text",
                placeholder: "UUID format (avtomatik to'ldiriladi)",
                default: "",
                required: true,
                hint: "⚠️ Yangi captcha olish uchun tugmani bosing",
                readonly: true,
                needsCaptcha: true  // Show get captcha button
                },
                captchaValue: {
                label: "Captcha Qiymati",
                type: "text",
                placeholder: "Captcha rasmidagi raqamni kiriting",
                default: "",
                required: true,
                hint: "⚠️ Yuqoridagi rasmda ko'rsatilgan raqamni kiriting",
                needsCaptcha: true  // Depends on captcha
                }
                },
                description: "PINFL va tug'ilgan sana orqali GUVD bazasidan passport ma'lumotlarini olish (captcha talab qilinadi)",
                dependsOn: 1,  // Requires authentication (token)
                ported: true  // ✅ Controller ported, 100% old-hemis format {success, data, address}
                }
            {
                id: 9,
                category: "03.Passport ma'lumotlari",
                name: "Address",
                method: "GET",
                url: "/app/rest/v2/services/passport-data/getAddress",
                requiresAuth: true,
                params: {
                pinfl: "{pinfl}"
                },
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                placeholder: "14 raqamli PINFL",
                default: "",  // Configdan olinadi (newPinfl/oldPinfl)
                useConfigPinfl: true,
                required: true
                }
                },
                description: "PINFL orqali GUVD bazasidan manzil ma'lumotlarini olish (captcha talab qilinmaydi)",
                dependsOn: 1,  // Requires authentication (token)
                ported: true  // ✅ Controller ported, 100% old-hemis format {success, data}
                }

            // ============================================
            // 04.Talaba (18 endpoint)
            // ============================================
            {
                id: 10,
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
                }
            {
                id: 11,
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
                }
            {
                id: 12,
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
                }
            {
                id: 13,
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
                }
            {
                id: 14,
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
                }
            {
                id: 15,
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
                }
            {
                id: 16,
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
                }
            {
                id: 17,
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
                }
            {
                id: 18,
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
                }
            {
                id: 19,
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
                }
            {
                id: 20,
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
                }
            {
                id: 21,
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
                }
            {
                id: 22,
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
                }
            {
                id: 23,
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
                }
            {
                id: 24,
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
                }
            {
                id: 25,
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
                }
            {
                id: 26,
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
                }
            {
                id: 27,
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

            // ============================================
            // 05.O'qituvchi (8 endpoint)
            // ============================================
            {
                id: 28,
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
                }
            {
                id: 29,
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
                }
            {
                id: 30,
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
                }
            {
                id: 31,
                category: "05.O'qituvchi",
                name: "O'qituvchi ID sini olish (Service API)",
                method: "POST",
                url: "/app/rest/v2/services/teacher/id",
                requiresAuth: true,
                inputFields: {
                citizenship: {
                label: "Citizenship (fuqarolik)",
                type: "text",
                placeholder: "11 = O'zbekiston",
                default: "11",
                required: true
                },
                pinfl: {
                label: "PINFL (14 raqam)",
                type: "text",
                placeholder: "42103714310024",
                default: "42103714310024",
                required: true
                },
                serial: {
                label: "Passport seriya/raqam",
                type: "text",
                placeholder: "AD3391507",
                default: "AD3391507",
                required: true
                },
                year: {
                label: "Yil",
                type: "text",
                placeholder: "2022",
                default: "2022",
                required: true
                },
                gender: {
                label: "Jinsi (11=erkak, 12=ayol)",
                type: "text",
                placeholder: "12",
                default: "12",
                required: true
                }
                },
                bodyTemplate: {
                data: {
                citizenship: "{citizenship}",
                pinfl: "{pinfl}",
                serial: "{serial}",
                year: "{year}",
                gender: "{gender}"
                }
                },
                description: "O'qituvchi Universal ID sini olish yoki yangi yaratish. citizenship=11 O'zbekiston fuqarosi. Mavjud o'qituvchi topilsa is_new=false, topilmasa yangi yaratiladi is_new=true. ID format: {universityCode}{YY}{gender}{sequence}.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - CubaTeacherServiceController.java
                }
            {
                id: 32,
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
            {
                id: 33,
                category: "05.O'qituvchi",
                name: "O'qituvchilarni qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_ETeacher/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (JSON)",
                type: "text",
                placeholder: '{"conditions":[{"property":"pinfl","operator":"=","value":"32305967340015"}]}',
                default: '{"conditions":[{"property":"pinfl","operator":"=","value":"32305967340015"}]}',
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                default: "0",
                required: false
                },
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                default: "10",
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
                description: "O'qituvchilarni URL parametrlari orqali qidirish. Filter JSON formatida: {\"conditions\":[{\"property\":\"pinfl\",\"operator\":\"=\",\"value\":\"...\"}]}",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - TeacherEntityController.java
                }
            {
                id: 34,
                category: "05.O'qituvchi",
                name: "O'qituvchilarni qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_ETeacher/search",
                requiresAuth: true,
                inputFields: {
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                default: "0",
                required: false
                },
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                default: "10",
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
                requestBody: {
                filter: {
                conditions: [
                {
                property: "pinfl",
                operator: "=",
                value: "32305967340015"
                }
                ]
                }
                },
                description: "O'qituvchilarni JSON filter orqali qidirish. Request body da filter, offset, limit parametrlari bo'lishi mumkin.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - TeacherEntityController.java
                }
            {
                id: 35,
                category: "05.O'qituvchi",
                name: "Xodim lavozimini qo'shish (addJob)",
                method: "POST",
                url: "/app/rest/v2/services/teacher/addJob",
                requiresAuth: true,
                inputFields: {
                employeeId: {
                label: "O'qituvchi ID (UUID)",
                type: "text",
                placeholder: "O'qituvchi UUID",
                default: "00000000-0000-0000-0000-000000000000",
                required: true
                },
                universityCode: {
                label: "OTM kodi",
                type: "text",
                placeholder: "401",
                default: "401",
                required: true
                },
                departmentCode: {
                label: "Kafedra kodi",
                type: "text",
                placeholder: "401-102",
                default: "401-102",
                required: true
                },
                employeeForm: {
                label: "Shtat shakli (11=Asosiy, 12=Ichki o'rindosh, 13=Tashqi o'rindosh, 14=Soatbay)",
                type: "text",
                placeholder: "11",
                default: "11",
                required: true
                },
                employeeStatus: {
                label: "Holati (11=Ishlamoqda, 12=Ta'tilda, 13=Bo'shatilgan)",
                type: "text",
                placeholder: "11",
                default: "11",
                required: true
                },
                employeeType: {
                label: "Xodim turi",
                type: "text",
                placeholder: "11",
                default: "11",
                required: true
                },
                employeeRate: {
                label: "Stavka (11=1.0, 12=0.75, 13=0.5, 14=0.25)",
                type: "text",
                placeholder: "13",
                default: "13",
                required: true
                },
                employeePosition: {
                label: "Lavozim kodi",
                type: "text",
                placeholder: "11",
                default: "11",
                required: true
                },
                jobStartDate: {
                label: "Ish boshlagan sana",
                type: "text",
                placeholder: "YYYY-MM-DD",
                default: "2020-01-06",
                required: true
                },
                jobEndDate: {
                label: "Shartnoma tugash sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                default: "2025-08-01",
                required: true
                }
                },
                bodyTemplate: {
                job: {
                employee: {id: "{employeeId}"},
                university: {code: "{universityCode}"},
                department: {code: "{departmentCode}"},
                employeeForm: {code: "{employeeForm}"},
                employeeStatus: {code: "{employeeStatus}"},
                employeeType: {code: "{employeeType}"},
                employeeRate: {code: "{employeeRate}"},
                employeePosition: {code: "{employeePosition}"},
                jobStartDate: "{jobStartDate}",
                jobEndDate: "{jobEndDate}"
                }
                },
                description: "Xodimga yangi lavozim qo'shish. employeeForm: 11=Asosiy shtat, 12=O'rindoshlik (ichki), 13=O'rindoshlik (tashqi), 14=Soatbay. employeeStatus: 11=Ishlamoqda, 12=Ta'tilda, 13=Ishdan bo'shatilgan.",
                dependsOn: 1,
                ported: true  // ✅ Controller ported - CubaTeacherServiceController.java
                }

            // ============================================
            // 06.Xodim lavozimlari (29 endpoint)
            // ============================================
            {
                id: 36,
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
                }
            {
                id: 37,
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
                }
            {
                id: 38,
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
                }
            {
                id: 39,
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
                }
            {
                id: 40,
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
                }
            {
                id: 41,
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
                }
            {
                id: 42,
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
                }
            {
                id: 43,
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
                }
            {
                id: 44,
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
                }
            {
                id: 45,
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
                }
            {
                id: 46,
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
                }
            {
                id: 47,
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
                }
            {
                id: 48,
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
                }
            {
                id: 49,
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
                }
            {
                id: 50,
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
                }
            {
                id: 51,
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
                }
            {
                id: 52,
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
                }
            {
                id: 53,
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
                }
            {
                id: 54,
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
                }
            {
                id: 55,
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
                }
            {
                id: 56,
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
                }
            {
                id: 57,
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
                }
            {
                id: 58,
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
                }
            {
                id: 59,
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
                }
            {
                id: 60,
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
                }
            {
                id: 61,
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
                }
            {
                id: 62,
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
                }
            {
                id: 63,
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

            // ============================================
            // 07.OTM bo'linmalari (7 endpoint)
            // ============================================
            {
                id: 64,
                category: "07.OTM bo'linmalari",
                name: "Bo'linmani ID (code) bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Bo'linma kodi",
                type: "text",
                default: "401-102-08",
                required: true,
                placeholder: "Yangi:401-102-08, Eski:351-118"
                },
                view: {
                label: "View nomi",
                type: "text",
                default: "",
                required: false,
                placeholder: "eUniversityDepartment-view (ixtiyoriy)"
                },
                returnNulls: {
                label: "null qiymatlarni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "",
                required: false
                }
                },
                description: "OTM bo'linmasi (fakultet, kafedra, bo'lim) ma'lumotlarini kod bo'yicha olish. ID = code (String), UUID emas! Masalan: '351-118' (fakultet), '401-102-08' (kafedra). Response: _entityName, _instanceName, code, nameUz, nameRu, university, deparmentType, parent, status.",
                ported: true,
                storeFirstId: "departmentCode"
                }
            {
                id: 65,
                category: "07.OTM bo'linmalari",
                name: "Barcha bo'linmalarni olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment",
                requiresAuth: true,
                inputFields: {
                offset: {
                label: "Offset",
                type: "number",
                default: "0",
                required: false
                },
                limit: {
                label: "Limit",
                type: "number",
                default: "10",
                required: false
                },
                sort: {
                label: "Sort",
                type: "text",
                default: "",
                required: false,
                placeholder: "code-asc yoki nameUz-desc"
                },
                view: {
                label: "View nomi",
                type: "text",
                default: "",
                required: false
                },
                returnNulls: {
                label: "null qiymatlarni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "",
                required: false
                }
                },
                description: "Barcha OTM bo'linmalarini pagination bilan olish. Foydalanuvchi faqat o'z OTM bo'linmalarini ko'radi. Sort formati: field-direction (masalan: code-asc, nameUz-desc).",
                ported: true
                }
            {
                id: 66,
                category: "07.OTM bo'linmalari",
                name: "Bo'linmalarni qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA format JSON)",
                type: "textarea",
                // 🎯 Ikkala tizim uchun ham university.code = 401 (solishtirish uchun)
                default: JSON.stringify({
                filter: {
                conditions: [
                { property: "university.code", operator: "=", value: "401" },
                { property: "status", operator: "=", value: true }
                ]
                }
                }),
                required: false,
                placeholder: '{"filter":{"conditions":[{"property":"university.code","operator":"=","value":"401"}]}}',
                rows: 4,
                helpText: `<b>🎯 Test uchun avtomatik filter:</b><br>
                - university.code = "401" (solishtirish uchun bir xil)<br>
                - status = true (faqat faol bo'linmalar)<br><br>
                <b>CUBA Filter formati:</b><br>
                <pre>{"filter":{"conditions":[
                {"property":"university.code", "operator":"=", "value":"401"},
                {"property":"status", "operator":"=", "value":true}
                ]}}</pre>
                <b>Qo'llab-quvvatlanadigan operatorlar:</b> =, <>, like, startsWith, endsWith, in, isNull, notNull<br>
                <b>Filtrlash mumkin maydonlar:</b> code, nameUz, nameRu, university.code, deparmentType.code, status`
                },
                view: {
                label: "View nomi",
                type: "text",
                default: "",
                required: false
                },
                returnNulls: {
                label: "null qiymatlarni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "",
                required: false
                }
                },
                description: "Bo'linmalarni URL parametrlari orqali qidirish. 🎯 Filter: university.code=401, status=true",
                ported: true
                }
            {
                id: 67,
                category: "07.OTM bo'linmalari",
                name: "Bo'linmalarni qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment/search",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                returnNulls: {
                label: "null qiymatlarni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "",
                required: false
                }
                },
                // 🎯 Ikkala tizim uchun ham university.code = 401 (solishtirish uchun)
                bodyGenerator: (inputs) => ({
                filter: {
                conditions: [
                { property: "university.code", operator: "=", value: "401" },
                { property: "status", operator: "=", value: true }
                ]
                },
                view: "eUniversityDepartment-view"
                }),
                description: `Bo'linmalarni JSON filter orqali qidirish.
                <b>🎯 Test uchun avtomatik filter (ikkala tizim uchun):</b>
                - <b>university.code = "401"</b> (solishtirish uchun bir xil)
                - <b>status = true</b> (faqat faol bo'linmalar)
                <b>CUBA Filter formati:</b>
                <pre>{"filter":{"conditions":[
                {"property":"university.code", "operator":"=", "value":"401"},
                {"property":"status", "operator":"=", "value":true}
                ]}, "view":"..."}</pre>
                <b>Qo'llab-quvvatlanadigan operatorlar:</b> =, <>, like, startsWith, endsWith, in, isNull, notNull
                <b>Filtrlash mumkin maydonlar:</b> code, nameUz, nameRu, university.code, deparmentType.code, status`,
                ported: true
                }
            {
                id: 68,
                category: "07.OTM bo'linmalari",
                name: "Yangi bo'linma yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                body_id: {
                label: "id (code)",
                type: "text",
                defaultNew: "401-99-TEST",  // 🎯 Yangi HEMIS (otm401)
                defaultOld: "351-99-TEST",  // 🏛️ Eski HEMIS (otm351)
                required: true,
                placeholder: "Yangi: 401-xx-xx, Eski: 351-xx-xx",
                bodyField: "id"
                },
                body_nameUz: {
                label: "nameUz",
                type: "text",
                default: "Test bo'lim",
                required: true,
                placeholder: "O'zbekcha nomi",
                bodyField: "nameUz"
                },
                body_nameRu: {
                label: "nameRu",
                type: "text",
                default: "Тестовый отдел",
                required: false,
                placeholder: "Ruscha nomi",
                bodyField: "nameRu"
                },
                body_status: {
                label: "status",
                type: "select",
                options: [{value: "true", label: "true (faol)"}, {value: "false", label: "false (nofaol)"}],
                default: "true",
                required: false,
                bodyField: "status",
                parseAs: "boolean"
                },
                body_university_code: {
                label: "university.code",
                type: "text",
                defaultNew: "401",  // 🎯 Yangi HEMIS (otm401)
                defaultOld: "351",  // 🏛️ Eski HEMIS (otm351)
                required: true,
                placeholder: "Yangi Hemis: 401, Eski Hemis: 351",
                bodyField: "university.code"
                },
                body_deparmentType_code: {
                label: "deparmentType.code",
                type: "text",
                default: "12",
                required: true,
                placeholder: "10=fakultet, 11=kafedra, 12=bo'lim",
                bodyField: "deparmentType.code"
                },
                returnNulls: {
                label: "null qiymatlarni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "",
                required: false
                }
                },
                bodyGenerator: (inputs) => ({
                id: inputs.body_id,
                nameUz: inputs.body_nameUz,
                nameRu: inputs.body_nameRu || null,
                status: inputs.body_status === 'true',
                university: {code: inputs.body_university_code},
                deparmentType: {code: inputs.body_deparmentType_code}
                }),
                description: `Yangi bo'linma yaratish yoki mavjudini yangilash (UPSERT).
                **🧪 Test uchun:**
                - 🆕 **Yangi Hemis:** id=401-99-TEST, university.code=401
                - 🏛️ **Eski Hemis:** id=351-99-TEST, university.code=351
                **CUBA UPSERT Behavior:**
                - Agar entity mavjud bo'lsa → YANGILASH
                - Agar entity mavjud bo'lmasa → YARATISH
                **Maydonlar:**
                - id: Bo'linma kodi (String, unique, required)
                - nameUz: O'zbekcha nomi (required)
                - nameRu: Ruscha nomi (optional)
                - status: true/false
                - university.code: Universitet kodi
                - deparmentType.code: Bo'linma turi (10=fakultet, 11=kafedra, 12=bo'lim)
                **Eslatma:** Server xavfsizlik uchun university.code ni foydalanuvchi kontekstidan oladi.`,
                ported: true,
                storeFirstId: "createdDepartmentCode"
                }
            {
                id: 69,
                category: "07.OTM bo'linmalari",
                name: "Bo'linmani yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment/{entityId}",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                entityId: {
                label: "Bo'linma kodi",
                type: "text",
                defaultNew: "401-99-TEST",  // 🎯 Yangi HEMIS (otm401)
                defaultOld: "351-99-TEST",  // 🏛️ Eski HEMIS (otm351)
                required: true,
                placeholder: "Yangi: 401-xx-xx, Eski: 351-xx-xx",
                useStoredId: "departmentCode"
                },
                returnNulls: {
                label: "null qiymatlarni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "",
                required: false
                }
                },
                requestBody: {
                nameUz: "Yangilangan nom",
                nameRu: "Обновленное название",
                status: true
                },
                description: `Bo'linmani yangilash.
                **🧪 Test uchun:**
                - 🆕 **Yangi Hemis (otm401):** entityId = 401-99-TEST
                - 🏛️ **Eski Hemis (otm351):** entityId = 351-99-TEST
                Avval #5 orqali bo'linma yarating, keyin shu endpoint orqali yangilang.`,
                ported: true
                }
            {
                id: 70,
                category: "07.OTM bo'linmalari",
                name: "Bo'linmani o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EUniversityDepartment/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Bo'linma kodi (o'chirish uchun)",
                type: "text",
                defaultNew: "401-99-TEST",  // 🎯 Yangi HEMIS (otm401)
                defaultOld: "351-99-TEST",  // 🏛️ Eski HEMIS (otm351)
                required: true,
                placeholder: "Yangi: 401-xx-xx, Eski: 351-xx-xx",
                useStoredId: "createdDepartmentCode"
                }
                },
                description: `Bo'linmani soft delete qilish (delete_ts belgilanadi, bazadan o'chirilmaydi).
                **🧪 Test uchun:**
                - 🆕 **Yangi Hemis (otm401):** entityId = 401-99-TEST
                - 🏛️ **Eski Hemis (otm351):** entityId = 351-99-TEST
                Avval #5 orqali bo'linma yarating, keyin shu endpoint orqali o'chiring.`,
                ported: true
                }

            // ============================================
            // 08.OTM bo'linma turlari (7 endpoint)
            // ============================================
            {
                id: 71,
                category: "08.OTM bo'linma turlari",
                name: "Bo'linma turini ID bo'yicha olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                inputFields: {
                entityId: {
                label: "Bo'linma turi kodi",
                type: "text",
                default: "11",
                required: true,
                placeholder: "11 = Fakultet, 12 = Kafedra"
                },
                dynamicAttributes: {
                label: "Dynamic attributes",
                type: "select",
                options: ["", "true", "false"],
                default: "",
                required: false
                },
                returnNulls: {
                label: "Return nulls",
                type: "select",
                options: ["", "true", "false"],
                default: "",
                required: false
                }
                },
                description: "Bo'linma turini code bo'yicha olish. Masalan: 11=Fakultet, 12=Kafedra, 13=Bo'lim",
                ported: true
                }
            {
                id: 72,
                category: "08.OTM bo'linma turlari",
                name: "Barcha bo'linma turlarini olish (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType",
                requiresAuth: true,
                dependsOn: 1,
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                default: "20",
                required: false,
                placeholder: "Nechta yozuv"
                },
                offset: {
                label: "Offset",
                type: "number",
                default: "0",
                required: false,
                placeholder: "Qayerdan boshlash"
                },
                returnCount: {
                label: "Return count",
                type: "select",
                options: ["", "true", "false"],
                default: "",
                required: false
                }
                },
                description: "Barcha bo'linma turlarini sahifalangan ro'yxat sifatida olish",
                ported: true
                }
            {
                id: 73,
                category: "08.OTM bo'linma turlari",
                name: "Bo'linma turlarini qidirish (GET search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType/search",
                requiresAuth: true,
                dependsOn: 1,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON format)",
                type: "textarea",
                rows: 3,
                default: '{"conditions":[{"property":"name","operator":"contains","value":"Fakultet"}]}',
                required: true,
                placeholder: '{"conditions":[{"property":"name","operator":"contains","value":"..."}]}',
                helpText: 'CUBA filter: property=name/code/active, operator=contains/=/startsWith'
                },
                limit: {
                label: "Limit",
                type: "number",
                default: "20",
                required: false
                },
                returnCount: {
                label: "Return count",
                type: "select",
                options: ["", "true", "false"],
                default: "",
                required: false
                }
                },
                description: "Bo'linma turlarini CUBA JSON filter bilan qidirish",
                ported: true
                }
            {
                id: 74,
                category: "08.OTM bo'linma turlari",
                name: "Bo'linma turlarini qidirish (POST search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType/search",
                requiresAuth: true,
                dependsOn: 1,
                contentType: "json",
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                default: "20",
                required: false
                },
                returnCount: {
                label: "Return count",
                type: "select",
                options: ["", "true", "false"],
                default: "",
                required: false
                }
                },
                requestBody: {
                filter: {
                conditions: [
                { property: "name", operator: "contains", value: "Kafedra" }
                ]
                }
                },
                description: "Bo'linma turlarini CUBA JSON filter bilan qidirish (POST)",
                ported: true
                }
            {
                id: 75,
                category: "08.OTM bo'linma turlari",
                name: "Yangi bo'linma turi yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType",
                requiresAuth: true,
                dependsOn: 1,
                contentType: "json",
                inputFields: {
                code: {
                label: "Kod",
                type: "text",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                placeholder: "NEW=98, OLD=99",
                helpText: "🆕 Yangi Hemis: 98 | 🏛️ Eski Hemis: 99"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                defaultNew: "Test turi (Yangi Hemis)",
                defaultOld: "Test turi (Eski Hemis)",
                required: true
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                defaultNew: "Тестовый тип (Новый Hemis)",
                defaultOld: "Тестовый тип (Старый Hemis)",
                required: false
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                defaultNew: "Test type (New Hemis)",
                defaultOld: "Test type (Old Hemis)",
                required: false
                }
                },
                bodyTemplate: {
                code: "{code}",
                name: "{name}",
                nameRu: "{{nameRu}}",
                nameEn: "{{nameEn}}"
                },
                description: "Yangi bo'linma turi yaratish. 🆕 NEW=98, 🏛️ OLD=99 - alohida test ma'lumotlari",
                ported: true
                }
            {
                id: 76,
                category: "08.OTM bo'linma turlari",
                name: "Bo'linma turini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                contentType: "json",
                inputFields: {
                entityId: {
                label: "Bo'linma turi kodi",
                type: "text",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                placeholder: "NEW=98, OLD=99",
                helpText: "🆕 Yangi Hemis: 98 | 🏛️ Eski Hemis: 99 (avval #5 da yaratilgan)"
                },
                name: {
                label: "Yangi nom (O'zbekcha)",
                type: "text",
                defaultNew: "Yangilangan (Yangi Hemis)",
                defaultOld: "Yangilangan (Eski Hemis)",
                required: true
                },
                nameRu: {
                label: "Yangi nom (Ruscha)",
                type: "text",
                defaultNew: "Обновлено (Новый Hemis)",
                defaultOld: "Обновлено (Старый Hemis)",
                required: false
                }
                },
                bodyTemplate: {
                name: "{name}",
                nameRu: "{{nameRu}}"
                },
                description: "Mavjud bo'linma turini yangilash. Avval #5 da yaratilgan yozuvni yangilaydi",
                ported: true
                }
            {
                id: 77,
                category: "08.OTM bo'linma turlari",
                name: "Bo'linma turini o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HUniversityDepartmentType/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                inputFields: {
                entityId: {
                label: "Bo'linma turi kodi",
                type: "text",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                placeholder: "NEW=98, OLD=99",
                helpText: "🆕 Yangi Hemis: 98 | 🏛️ Eski Hemis: 99 (avval #5 da yaratilgan)"
                }
                },
                description: "Bo'linma turini soft delete qilish. Test ketma-ketligi: #5 yaratish → #6 yangilash → #7 o'chirish",
                ported: true
                }

            // ============================================
            // 09.OTM xodimlari kategoriyasi (7 endpoint)
            // ============================================
            {
                id: 78,
                category: "09.OTM xodimlari kategoriyasi",
                name: "Barcha OTM xodim kategoriyalari (GET all)",
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
                description: "OTM xodimlari kategoriyalari klassifikatori: 10=Boshqa, 11=Administrativ-boshqaruv, 12=Professor-o'qituvchi, 13=O'quv-yordamchi va texnik, 14=Xizmat ko'rsatuvchi",
                ported: true,
                storeFirstId: "employeeTypeCode"
                }
            {
                id: 79,
                category: "09.OTM xodimlari kategoriyasi",
                name: "OTM xodim kategoriyasini olish (GET by ID)",
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
                description: "Bitta OTM xodim kategoriyasini kod bo'yicha olish. Kodlar: 10=Boshqa, 11=Administrativ, 12=Professor, 13=Texnik, 14=Xizmat",
                dependsOn: 1,
                ported: true
                }
            {
                id: 80,
                category: "09.OTM xodimlari kategoriyasi",
                name: "Yangi OTM xodim kategoriyasini yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                id: {
                label: "Tur kodi (unique)",
                type: "number",
                default: 991,
                defaultOld: 992,
                required: true,
                hint: "Yangi: 991, Eski: 992 (turli kodlar - bitta baza)"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                default: "Test xodim turi (NEW)",
                defaultOld: "Test xodim turi (OLD)",
                required: true
                },
                active: {
                label: "Faol",
                type: "select",
                options: [{value: true, label: "Ha"}, {value: false, label: "Yo'q"}],
                default: true,
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["id", "name", "active"],
                description: "Yangi OTM xodim kategoriyasini yaratish. id (code) va name majburiy. ⚠️ Yangi va Eski uchun turli kodlar kiriting!",
                storeFirstId: "newEmployeeTypeId",
                ported: true
                }
            {
                id: 81,
                category: "09.OTM xodimlari kategoriyasi",
                name: "OTM xodim kategoriyasini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                entityId: {
                label: "Tur kodi",
                type: "number",
                default: 991,
                defaultOld: 992,
                required: true,
                hint: "#3 da yaratilgan kodni kiriting"
                },
                name: {
                label: "Yangi nomi (O'zbekcha)",
                type: "text",
                default: "Yangilangan nom (NEW)",
                defaultOld: "Yangilangan nom (OLD)",
                required: false
                },
                active: {
                label: "Faol",
                type: "select",
                options: [{value: "", label: "O'zgartirmaslik"}, {value: true, label: "Ha"}, {value: false, label: "Yo'q"}],
                default: "",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["name", "active"],
                description: "OTM xodim kategoriyasini yangilash. Avval #3 orqali yarating, keyin shu yerda yangilang.",
                dependsOn: 3,
                ported: true
                }
            {
                id: 82,
                category: "09.OTM xodimlari kategoriyasi",
                name: "OTM xodim kategoriyasini o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                entityId: {
                label: "Tur kodi",
                type: "number",
                default: 991,
                defaultOld: 992,
                required: true,
                hint: "#3 da yaratilgan kodni kiriting"
                }
                },
                description: "OTM xodim kategoriyasini o'chirish (soft delete). Avval #3 da yarating → #4 da yangilang → #5 da o'chiring.",
                dependsOn: 3,
                ported: true
                }
            {
                id: 83,
                category: "09.OTM xodimlari kategoriyasi",
                name: "OTM xodim kategoriyalarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "CUBA Filter (JSON)",
                type: "textarea",
                default: '{"conditions":[]}',
                required: false,
                placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                },
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
                description: "CUBA filter formatida OTM xodim kategoriyalarini qidirish. filter parametri URL encoded JSON formatida.",
                dependsOn: 1,
                ported: true
                }
            {
                id: 84,
                category: "09.OTM xodimlari kategoriyasi",
                name: "OTM xodim kategoriyalarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HUniversityEmployeeType/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "CUBA Filter (JSON)",
                type: "textarea",
                default: '{"conditions":[]}',
                required: false,
                placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                },
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
                hasBody: true,
                bodyFields: ["filter", "limit", "offset"],
                description: "CUBA filter formatida OTM xodim kategoriyalarini qidirish. Filter body da JSON formatida yuboriladi.",
                dependsOn: 1,
                ported: true
                }

            // ============================================
            // 10.Talaba holati (7 endpoint)
            // ============================================
            {
                id: 85,
                category: "10.Talaba holati",
                name: "Barcha talaba holatlari (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType",
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
                description: "Talaba holatlari klassifikatori: 10=Boshqa, 11=O'qimoqda, 12=Chetlashgan, 13=Akademik ta'til, 14=Bitirgan",
                ported: true,
                storeFirstId: "studentStatusCode"
                }
            {
                id: 86,
                category: "10.Talaba holati",
                name: "Talaba holatini olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Holat kodi (10-14)",
                type: "text",
                default: "11",
                required: true,
                placeholder: "11"
                },
                returnNulls: {
                label: "Return Nulls",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "",
                required: false
                }
                },
                description: "Kod bo'yicha talaba holatini olish. Holatlar: 10=Boshqa, 11=O'qimoqda, 12=Chetlashgan, 13=Akademik ta'til, 14=Bitirgan",
                dependsOn: 1,
                ported: true
                }
            {
                id: 87,
                category: "10.Talaba holati",
                name: "Yangi talaba holatini yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                code: {
                label: "Kod (unique)",
                type: "text",
                default: "991",
                defaultOld: "992",
                required: true,
                placeholder: "991"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                default: "Test holati (Yangi)",
                defaultOld: "Test holati (Eski)",
                required: true
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                default: "Test status (New)",
                defaultOld: "Test status (Old)",
                required: false
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                default: "Тестовый статус (Новый)",
                defaultOld: "Тестовый статус (Старый)",
                required: false
                },
                active: {
                label: "Faol",
                type: "select",
                options: [{value: "true", label: "Ha"}, {value: "false", label: "Yo'q"}],
                default: "true",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["code", "name", "nameEn", "nameRu", "active"],
                description: "Yangi talaba holati yaratish. CUBA Platform kabi upsert qiladi - mavjud bo'lsa yangilaydi.",
                dependsOn: 1,
                ported: true
                }
            {
                id: 88,
                category: "10.Talaba holati",
                name: "Talaba holatini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                entityId: {
                label: "Holat kodi",
                type: "text",
                default: "991",
                defaultOld: "992",
                required: true,
                placeholder: "991"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                default: "Test holati YANGILANGAN (Yangi)",
                defaultOld: "Test holati YANGILANGAN (Eski)",
                required: false
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                default: "Test status UPDATED (New)",
                defaultOld: "Test status UPDATED (Old)",
                required: false
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                default: "Тестовый статус ОБНОВЛЕНО (Новый)",
                defaultOld: "Тестовый статус ОБНОВЛЕНО (Старый)",
                required: false
                },
                active: {
                label: "Faol",
                type: "select",
                options: [{value: "", label: "O'zgartirmaslik"}, {value: "true", label: "Ha"}, {value: "false", label: "Yo'q"}],
                default: "",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["name", "nameEn", "nameRu", "active"],
                description: "Talaba holatini yangilash. Faqat yuborilgan maydonlar yangilanadi.",
                dependsOn: 3,
                ported: true
                }
            {
                id: 89,
                category: "10.Talaba holati",
                name: "Talaba holatini o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                entityId: {
                label: "Holat kodi",
                type: "text",
                default: "991",
                defaultOld: "992",
                required: true,
                placeholder: "991"
                }
                },
                description: "Talaba holatini o'chirish (soft delete). Faqat test uchun yaratilgan yozuvlarni o'chiring!",
                dependsOn: 4,
                ported: true
                }
            {
                id: 90,
                category: "10.Talaba holati",
                name: "Talaba holatlarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "CUBA Filter (JSON)",
                type: "textarea",
                default: '{"conditions":[]}',
                required: false,
                placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                },
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
                description: "CUBA filter formatida talaba holatlarini qidirish. filter parametri URL encoded JSON formatida.",
                dependsOn: 1,
                ported: true
                }
            {
                id: 91,
                category: "10.Talaba holati",
                name: "Talaba holatlarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HStudentStatusType/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "CUBA Filter (JSON)",
                type: "textarea",
                default: '{"conditions":[]}',
                required: false,
                placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                },
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
                hasBody: true,
                bodyFields: ["filter"],
                description: "CUBA filter formatida talaba holatlarini qidirish. Filter body da JSON formatida yuboriladi.",
                dependsOn: 1,
                ported: true
                }

            // ============================================
            // 11.Fuqarolik holatlari (7 endpoint)
            // ============================================
            {
                id: 92,
                category: "11.Fuqarolik holatlari",
                name: "Barcha fuqarolik holatlari (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HCitizenship",
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
                description: "Fuqarolik holatlari klassifikatori: 11=O'zbekiston fuqarosi, 12=Xorijiy fuqaro, 13=Fuqaroligi yo'q",
                ported: true,
                storeFirstId: "citizenshipCode"
                }
            {
                id: 93,
                category: "11.Fuqarolik holatlari",
                name: "Fuqarolik holatini olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HCitizenship/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Fuqarolik kodi (11-13)",
                type: "text",
                default: "11",
                required: true,
                placeholder: "11"
                },
                returnNulls: {
                label: "Return Nulls",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "",
                required: false
                }
                },
                description: "Kod bo'yicha fuqarolik holatini olish. 11=O'zbekiston, 12=Xorijiy, 13=Fuqaroligi yo'q",
                dependsOn: 1,
                ported: true
                }
            {
                id: 94,
                category: "11.Fuqarolik holatlari",
                name: "Yangi fuqarolik holatini yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HCitizenship",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                code: {
                label: "Kod (unique)",
                type: "text",
                default: "991",
                defaultOld: "992",
                required: true,
                placeholder: "991"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                default: "Test fuqarolik (Yangi)",
                defaultOld: "Test fuqarolik (Eski)",
                required: true
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                default: "Test citizenship (New)",
                defaultOld: "Test citizenship (Old)",
                required: false
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                default: "Тестовое гражданство (Новый)",
                defaultOld: "Тестовое гражданство (Старый)",
                required: false
                },
                active: {
                label: "Faol",
                type: "select",
                options: [{value: "true", label: "Ha"}, {value: "false", label: "Yo'q"}],
                default: "true",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["code", "name", "nameEn", "nameRu", "active"],
                description: "Yangi fuqarolik holati yaratish. CUBA Platform kabi upsert qiladi.",
                dependsOn: 1,
                ported: true
                }
            {
                id: 95,
                category: "11.Fuqarolik holatlari",
                name: "Fuqarolik holatini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HCitizenship/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                entityId: {
                label: "Fuqarolik kodi",
                type: "text",
                default: "991",
                defaultOld: "992",
                required: true,
                placeholder: "991"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                default: "Test fuqarolik YANGILANGAN (Yangi)",
                defaultOld: "Test fuqarolik YANGILANGAN (Eski)",
                required: false
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                default: "Test citizenship UPDATED (New)",
                defaultOld: "Test citizenship UPDATED (Old)",
                required: false
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                default: "Тестовое гражданство ОБНОВЛЕНО (Новый)",
                defaultOld: "Тестовое гражданство ОБНОВЛЕНО (Старый)",
                required: false
                },
                active: {
                label: "Faol",
                type: "select",
                options: [{value: "", label: "O'zgartirmaslik"}, {value: "true", label: "Ha"}, {value: "false", label: "Yo'q"}],
                default: "",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["name", "nameEn", "nameRu", "active"],
                description: "Fuqarolik holatini yangilash. Faqat yuborilgan maydonlar yangilanadi.",
                dependsOn: 3,
                ported: true
                }
            {
                id: 96,
                category: "11.Fuqarolik holatlari",
                name: "Fuqarolik holatini o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HCitizenship/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                entityId: {
                label: "Fuqarolik kodi",
                type: "text",
                default: "991",
                defaultOld: "992",
                required: true,
                placeholder: "991"
                }
                },
                description: "Fuqarolik holatini o'chirish (soft delete). Faqat test uchun yaratilgan yozuvlarni o'chiring!",
                dependsOn: 4,
                ported: true
                }
            {
                id: 97,
                category: "11.Fuqarolik holatlari",
                name: "Fuqarolik holatlarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HCitizenship/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "CUBA Filter (JSON)",
                type: "textarea",
                default: '{"conditions":[]}',
                required: false,
                placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                },
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
                description: "CUBA filter formatida fuqarolik holatlarini qidirish.",
                dependsOn: 1,
                ported: true
                }
            {
                id: 98,
                category: "11.Fuqarolik holatlari",
                name: "Fuqarolik holatlarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HCitizenship/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "CUBA Filter (JSON)",
                type: "textarea",
                default: '{"conditions":[]}',
                required: false,
                placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                },
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
                hasBody: true,
                bodyFields: ["filter"],
                description: "CUBA filter formatida fuqarolik holatlarini qidirish. Filter body da JSON.",
                dependsOn: 1,
                ported: true
                }

            // ============================================
            // 12.Diplomlar (6 endpoint)
            // ============================================
            {
                id: 99,
                category: "12.Diplomlar",
                name: "Yangi diploma yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                diplomaNumber: {
                label: "Diploma raqami",
                type: "text",
                defaultNew: "NEW-" + Date.now(),
                defaultOld: "OLD-" + Date.now(),
                required: true,
                placeholder: "AA-1234567"
                },
                university: {
                label: "OTM kodi",
                type: "text",
                defaultNew: "401",
                defaultOld: "351",
                required: true,
                placeholder: "OTM code (401 yoki 351)"
                },
                student: {
                label: "Talaba UUID",
                type: "text",
                defaultNew: "0d738e89-a9a9-9b9c-22d7-19912228b063",
                defaultOld: "ad0ee8cc-b5a6-3192-61a4-d4ee911a7912",
                required: true,
                placeholder: "Talaba entity UUID"
                },
                speciality: {
                label: "Mutaxassislik UUID",
                type: "text",
                defaultNew: "4c991851-7287-4330-a003-0b8362542439",
                defaultOld: "83b9d50f-b49d-d5b5-327c-adcef0a51e2d",
                required: true,
                placeholder: "Mutaxassislik UUID"
                },
                active: {
                label: "Active",
                type: "select",
                options: [{value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "true",
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
                hasBody: true,
                // OLD-HEMIS format: _entityName + nested refs with _entityName, speciality is plain string
                bodyGenerator: (inputs) => ({
                _entityName: "hemishe_EStudentDiploma",
                university: {_entityName: "hemishe_EUniversity", id: inputs.university},
                student: {_entityName: "hemishe_EStudent", id: inputs.student},
                speciality: inputs.speciality,
                diplomaNumber: inputs.diplomaNumber,
                active: inputs.active === 'true'
                }),
                description: "Yangi diploma yaratish. Yaratilgan ID avtomatik PUT va DELETE ga o'tadi. CUBA format.",
                ported: true,
                storeResultId: "createdDiplomaId"
                }
            {
                id: 100,
                category: "12.Diplomlar",
                name: "Diplomni yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Diploma ID (UUID)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: true,
                placeholder: "POST ishga tushsa avtomatik to'ldiriladi",
                useStoredId: "createdDiplomaId"
                },
                diplomaNumber: {
                label: "Yangi diploma raqami",
                type: "text",
                defaultNew: "UPDATED-NEW-" + Date.now(),
                defaultOld: "UPDATED-OLD-" + Date.now(),
                required: false,
                placeholder: "Yangilanadigan raqam"
                },
                registerNumber: {
                label: "Registr raqami",
                type: "text",
                default: "99999",
                required: false,
                placeholder: "12345"
                },
                active: {
                label: "Active",
                type: "select",
                options: [{value: "", label: "-- tanlang --"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "true",
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
                hasBody: true,
                // OLD-HEMIS format: _entityName + update fields
                bodyGenerator: (inputs) => {
                const body = {_entityName: "hemishe_EStudentDiploma"};
                if (inputs.diplomaNumber) body.diplomaNumber = inputs.diplomaNumber;
                if (inputs.registerNumber) body.registerNumber = inputs.registerNumber;
                if (inputs.active === 'true' || inputs.active === 'false') body.active = inputs.active === 'true';
                return body;
                },
                description: "2️⃣ POST da yaratilgan diplomni yangilash. entityId avtomatik to'ldiriladi.",
                ported: true
                }
            {
                id: 101,
                category: "12.Diplomlar",
                name: "Diplomni o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Diploma ID (o'chirish uchun)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: true,
                placeholder: "POST ishga tushsa avtomatik to'ldiriladi",
                useStoredId: "createdDiplomaId"
                }
                },
                description: "3️⃣ POST da yaratilgan diplomni o'chirish. entityId avtomatik to'ldiriladi.",
                ported: true
                }
            {
                id: 102,
                category: "12.Diplomlar",
                name: "Diplomni olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Diploma ID (UUID)",
                type: "text",
                defaultNew: "d2adcec7-92eb-2936-2e97-4860c802ff03",
                defaultOld: "de11c5a1-2943-4a9d-016f-f0a160e0af00",
                required: true,
                placeholder: "UUID formatda diploma ID (401: B00844218, 351: B00781880)"
                },
                view: {
                label: "View",
                type: "text",
                default: "eStudentDiploma-view",
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
                description: "Bitta diplomni UUID bo'yicha olish. view parametri bilan bog'liq entity ma'lumotlari ham qaytariladi.",
                ported: true
                }
            {
                id: 103,
                category: "12.Diplomlar",
                name: "Barcha diplomlar (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma",
                requiresAuth: true,
                // ⚠️ ESLATMA: Old Hemis CUBA GET /entities da filter ishlamaydi!
                // Filterlash uchun POST /search (#7) ishlatilsin
                inputFields: {
                view: {
                label: "View",
                type: "text",
                default: "eStudentDiploma-view",
                required: false
                },
                limit: {
                label: "Limit",
                type: "number",
                default: 1,  // ⚠️ 1 ta diplom - solishtirish uchun yetarli
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
                description: "⚠️ Old Hemis da filter ishlamaydi! Filterlash uchun POST /search (#7) ishlating. Bu endpoint faqat tuzilmani solishtiradi.",
                ported: true,
                storeFirstId: "diplomaId"
                }
            {
                id: 104,
                category: "12.Diplomlar",
                name: "Diplomlarni qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EStudentDiploma/search",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                filter: {
                label: "CUBA Filter (JSON) - Required",
                type: "textarea",
                default: '{"conditions":[{"property":"diplomaNumber","operator":"=","value":"B00844218"}]}',
                defaultOld: '{"conditions":[{"property":"diplomaNumber","operator":"=","value":"B00781880"}]}',
                required: true,
                placeholder: '{"conditions":[{"property":"diplomaNumber","operator":"=","value":"..."}]}'
                },
                view: {
                label: "View",
                type: "text",
                default: "eStudentDiploma-view",
                required: false
                },
                limit: {
                label: "Limit",
                type: "number",
                default: 10,
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                default: 0,
                required: false
                },
                returnCount: {
                label: "Return Count (X-Total-Count header)",
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
                }
                },
                description: "GET /search - filter query param sifatida (URL encoded JSON). Old Hemis bilan bir xil.",
                ported: true
                }

            // ============================================
            // 13.Klassifikatorlar (3 endpoint)
            // ============================================
            {
                id: 105,
                category: "13.Klassifikatorlar",
                name: "Barcha klassifikatorlar ro'yxati (GET /info)",
                method: "GET",
                url: "/app/rest/v2/services/classifiers/info",
                requiresAuth: true,
                description: "Tizimda mavjud barcha klassifikatorlar ro'yxatini olish (title, version, count)",
                ported: true
                }
            {
                id: 106,
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
                }
            {
                id: 107,
                category: "13.Klassifikatorlar",
                name: "Barcha klassifikatorlar (items bilan) (GET /allItems)",
                method: "GET",
                url: "/app/rest/v2/services/classifiers/allItems",
                requiresAuth: true,
                description: "Barcha klassifikatorlarni items bilan birga olish (CUBA formatida)",
                ported: true
                }

            // ============================================
            // 14.Tarjima (2 endpoint)
            // ============================================
            {
                id: 108,
                category: "14.Tarjima",
                name: "Barcha tarjimalar (GET /translate/get)",
                method: "GET",
                url: "/app/rest/v2/services/translate/get",
                requiresAuth: true,
                description: "Barcha tarjimalarni OLD-HEMIS formatida olish (_entityName, message, uz_Uz, ru_Ru, oz_Uz, en_Us, kk_Uz, category, version)",
                ported: true
                }
            {
                id: 109,
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

            // ============================================
            // 15.OTM (3 endpoint)
            // ============================================
            {
                id: 110,
                category: "15.OTM",
                name: "OTM ro'yxati (GET /entities/hemishe_EUniversity)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EUniversity",
                requiresAuth: true,
                inputFields: {
                limit: { label: "Limit", type: "number", defaultValue: "5", required: false },
                view: { label: "View", type: "text", defaultValue: "eUniversity-view", required: false }
                },
                description: "Barcha OTM ro'yxatini OLD-HEMIS formatida olish (eUniversity-view: code, name, tin, address, ownership, universityType, soato)",
                ported: true
                }
            {
                id: 111,
                category: "15.OTM",
                name: "OTM yaratish/yangilash (POST /entities/hemishe_EUniversity)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EUniversity",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                body_code: {
                label: "code (OTM kodi)",
                type: "text",
                defaultNew: "999-NEW-TEST",   // 🆕 Yangi HEMIS uchun
                defaultOld: "999-OLD-TEST",   // 🏛️ Eski HEMIS uchun
                required: true,
                placeholder: "Yangi: 999-NEW-TEST, Eski: 999-OLD-TEST",
                bodyField: "code"
                },
                body_name: {
                label: "name (OTM nomi)",
                type: "text",
                defaultNew: "Yangi Test Universiteti",   // 🆕 Yangi HEMIS uchun
                defaultOld: "Eski Test Universiteti",    // 🏛️ Eski HEMIS uchun
                required: true,
                placeholder: "OTM nomi",
                bodyField: "name"
                },
                body_tin: {
                label: "tin (STIR)",
                type: "text",
                defaultNew: "111222333",   // 🆕 Yangi HEMIS uchun
                defaultOld: "444555666",   // 🏛️ Eski HEMIS uchun
                required: false,
                placeholder: "STIR raqami",
                bodyField: "tin"
                },
                body_address: {
                label: "address (Manzil)",
                type: "text",
                defaultNew: "Yangi manzil, Toshkent",   // 🆕 Yangi HEMIS uchun
                defaultOld: "Eski manzil, Toshkent",    // 🏛️ Eski HEMIS uchun
                required: false,
                placeholder: "Manzil",
                bodyField: "address"
                },
                body_active: {
                label: "active (Faol)",
                type: "select",
                options: [{value: "true", label: "true (faol)"}, {value: "false", label: "false (nofaol)"}],
                default: "true",
                required: false,
                bodyField: "active",
                parseAs: "boolean"
                },
                body_studentUrl: {
                label: "studentUrl",
                type: "text",
                defaultNew: "student.new-test.uz",   // 🆕 Yangi HEMIS uchun
                defaultOld: "student.old-test.uz",   // 🏛️ Eski HEMIS uchun
                required: false,
                placeholder: "Talaba portal URL",
                bodyField: "studentUrl"
                },
                body_teacherUrl: {
                label: "teacherUrl",
                type: "text",
                defaultNew: "teacher.new-test.uz",   // 🆕 Yangi HEMIS uchun
                defaultOld: "teacher.old-test.uz",   // 🏛️ Eski HEMIS uchun
                required: false,
                placeholder: "O'qituvchi portal URL",
                bodyField: "teacherUrl"
                },
                body_oneId: {
                label: "oneId",
                type: "select",
                options: [{value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "true",
                required: false,
                bodyField: "oneId",
                parseAs: "boolean"
                },
                returnNulls: {
                label: "null qiymatlarni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "",
                required: false
                }
                },
                bodyGenerator: (inputs) => ({
                code: inputs.body_code,
                name: inputs.body_name,
                tin: inputs.body_tin || null,
                address: inputs.body_address || null,
                active: inputs.body_active === 'true',
                studentUrl: inputs.body_studentUrl || null,
                teacherUrl: inputs.body_teacherUrl || null,
                oneId: inputs.body_oneId === 'true',
                addStudent: true,
                allowGrouping: true,
                allowTransferOutside: true,
                gpaEdit: false,
                accreditationEdit: false,
                gradingSystem: false
                }),
                description: `**OTM yaratish yoki yangilash** (CUBA Entity API pattern)
                <b>🧪 Test uchun:</b>
                - 🆕 <b>Yangi Hemis:</b> code=999-NEW-TEST, name="Yangi Test Universiteti"
                - 🏛️ <b>Eski Hemis:</b> code=999-OLD-TEST, name="Eski Test Universiteti"
                <b>Xususiyatlar:</b>
                - Agar body da "code" mavjud va bazada bor → yangilash
                - Agar body da "code" bazada mavjud emas → yaratish
                - Response: Yaratilgan/yangilangan OTM + Location header
                <b>Majburiy maydonlar:</b>
                - code - OTM kodi (String, unique)
                - name - OTM nomi
                <b>Ixtiyoriy maydonlar:</b>
                - tin - STIR raqami
                - address - Manzil
                - active - Faol holat (boolean)
                - studentUrl, teacherUrl - Portal URL lari
                - oneId, addStudent, allowGrouping - Boolean flaglar
                <b>⚠️ Eslatma:</b> Bu endpoint faqat admin yoki tegishli huquqli foydalanuvchilar uchun!`,
                ported: true,
                storeFirstId: "createdUniversityCode"
                }
            {
                id: 112,
                category: "15.OTM",
                name: "OTM o'chirish (DELETE /entities/hemishe_EUniversity)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EUniversity/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "OTM kodi (o'chirish uchun)",
                type: "text",
                defaultNew: "999-NEW-TEST",   // 🆕 Yangi HEMIS uchun
                defaultOld: "999-OLD-TEST",   // 🏛️ Eski HEMIS uchun
                required: true,
                placeholder: "Yangi: 999-NEW-TEST, Eski: 999-OLD-TEST",
                useStoredId: "createdUniversityCode"
                }
                },
                description: `**OTM o'chirish** (soft delete)
                <b>🧪 Test uchun:</b>
                - 🆕 <b>Yangi Hemis:</b> entityId=999-NEW-TEST
                - 🏛️ <b>Eski Hemis:</b> entityId=999-OLD-TEST
                <b>Xususiyatlar:</b>
                - Soft delete: deleteTs va deletedBy maydonlari o'rnatiladi
                - Response: 204 No Content (muvaffaqiyatli) yoki 404 Not Found
                <b>⚠️ Eslatma:</b> Avval #2 orqali test OTM yarating, keyin shu endpoint orqali o'chiring!`,
                ported: true
                }

            // ============================================
            // 16.Ilmiy doktorant talabalari (7 endpoint)
            // ============================================
            {
                id: 113,
                category: "16.Ilmiy doktorant talabalari",
                name: "Doktorant yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                code: {
                label: "Doktorant kodi",
                type: "text",
                default: "401-DOC-TEST-NEW",
                defaultOld: "351-DOC-TEST-OLD",
                required: true,
                placeholder: "401-DOC-TEST"
                },
                firstname: {
                label: "Ism",
                type: "text",
                default: "Test",
                required: true
                },
                lastname: {
                label: "Familiya",
                type: "text",
                default: "Doktorant",
                required: true
                },
                fathername: {
                label: "Otasining ismi",
                type: "text",
                default: "Testovich",
                required: false
                },
                university: {
                label: "OTM kodi",
                type: "text",
                default: "401",
                defaultOld: "351",
                required: true
                },
                active: {
                label: "Faol",
                type: "select",
                options: [{value: "true", label: "Ha"}, {value: "false", label: "Yo'q"}],
                default: "true",
                required: false
                }
                },
                hasBody: true,
                bodyGenerator: (inputs) => ({
                code: inputs.code,
                firstname: inputs.firstname,
                lastname: inputs.lastname,
                fathername: inputs.fathername,
                university: {code: inputs.university},
                active: inputs.active === 'true'
                }),
                description: "Yangi doktorant talaba yaratish. CUBA format: university: {code: '401'}",
                ported: true,
                storeResultId: "doctoralStudentId"  // POST single object qaytaradi, array emas
                }
            {
                id: 114,
                category: "16.Ilmiy doktorant talabalari",
                name: "Doktorantni yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                entityId: {
                label: "Doktorant ID (UUID)",
                type: "text",
                default: "",
                defaultOld: "",
                required: true,
                placeholder: "POST dan olingan UUID",
                useStoredId: "doctoralStudentId"
                },
                firstname: {
                label: "Yangi ism",
                type: "text",
                default: "TestUpdated",
                defaultOld: "TestUpdatedOld",
                required: false
                },
                active: {
                label: "Faol",
                type: "select",
                options: [{value: "", label: "O'zgartirmaslik"}, {value: "true", label: "Ha"}, {value: "false", label: "Yo'q"}],
                default: "",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["firstname", "active"],
                description: "Doktorant ma'lumotlarini yangilash. Faqat yuborilgan maydonlar yangilanadi.",
                ported: true
                }
            {
                id: 115,
                category: "16.Ilmiy doktorant talabalari",
                name: "Doktorantni o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                entityId: {
                label: "Doktorant ID (o'chirish uchun)",
                type: "text",
                default: "",
                defaultOld: "",
                required: true,
                placeholder: "POST dan olingan UUID",
                useStoredId: "doctoralStudentId"
                }
                },
                description: "Doktorantni o'chirish (soft delete). 200 OK qaytaradi.",
                ported: true
                }
            {
                id: 116,
                category: "16.Ilmiy doktorant talabalari",
                name: "Doktorant olish (GET by ID)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent/{entityId}",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                entityId: {
                label: "Doktorant ID (UUID)",
                type: "text",
                default: "",
                defaultOld: "",
                required: true,
                placeholder: "Mavjud doktorant UUID"
                },
                view: {
                label: "View",
                type: "text",
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
                description: "Bitta doktorantni UUID bo'yicha olish.",
                ported: true
                }
            {
                id: 117,
                category: "16.Ilmiy doktorant talabalari",
                name: "Barcha doktorantlar (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent",
                requiresAuth: true,
                inputFields: {
                view: {
                label: "View",
                type: "text",
                default: "_local",
                required: false
                },
                limit: {
                label: "Limit",
                type: "number",
                default: 10,
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
                description: "Barcha doktorantlarni olish (pagination bilan).",
                ported: true
                }
            {
                id: 118,
                category: "16.Ilmiy doktorant talabalari",
                name: "Doktorantlarni qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent/search",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                filter: {
                label: "CUBA Filter (JSON)",
                type: "textarea",
                default: '{"conditions":[{"property":"active","operator":"=","value":true}]}',
                defaultOld: '{"conditions":[{"property":"active","operator":"=","value":true}]}',
                required: true,
                placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                },
                view: {
                label: "View",
                type: "text",
                default: "_local",
                required: false
                },
                limit: {
                label: "Limit",
                type: "number",
                default: 10,
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                default: 0,
                required: false
                }
                },
                description: "CUBA JSON filter bilan doktorantlarni qidirish (GET).",
                ported: true
                }
            {
                id: 119,
                category: "16.Ilmiy doktorant talabalari",
                name: "Doktorantlarni qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EDoctorateStudent/search",
                requiresAuth: true,
                separateInputs: true,
                inputFields: {
                filter: {
                label: "CUBA Filter (JSON)",
                type: "textarea",
                default: '{"conditions":[{"property":"active","operator":"=","value":true}]}',
                defaultOld: '{"conditions":[{"property":"active","operator":"=","value":true}]}',
                required: false,
                placeholder: '{"conditions":[{"property":"active","operator":"=","value":true}]}'
                },
                limit: {
                label: "Limit",
                type: "number",
                default: 10,
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                default: 0,
                required: false
                }
                },
                hasBody: true,
                bodyGenerator: (inputs) => {
                try {
                const filterObj = JSON.parse(inputs.filter || '{"conditions":[]}');
                return { filter: filterObj };
                } catch (e) {
                return { filter: { conditions: [] } };
                }
                },
                queryParamsFromInputs: ["limit", "offset"],
                description: "CUBA JSON filter bilan doktorantlarni qidirish (POST). Body={filter:{...}}",
                ported: true
                }

            // ============================================
            // 17.Ilmiy dissertasiya himoyalari (4 endpoint)
            // ============================================
            {
                id: 120,
                category: "17.Ilmiy dissertasiya himoyalari",
                name: "Dissertasiya himoyasi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EDissertationDefense",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                body_defenseDate: {
                label: "defenseDate (Himoya sanasi)",
                type: "text",
                defaultNew: "2024-06-15",
                defaultOld: "2021-02-10",
                required: true,
                bodyField: "defenseDate",
                placeholder: "YYYY-MM-DD"
                },
                body_defense_place: {
                label: "defense_place (Himoya joyi)",
                type: "text",
                defaultNew: "Toshkent Davlat Texnika Universiteti",
                defaultOld: "Toshkent",
                required: true,
                bodyField: "defense_place"
                },
                body_approvedDate: {
                label: "approvedDate (Tasdiqlangan sana)",
                type: "text",
                defaultNew: "2024-07-01",
                defaultOld: "2021-02-26",
                required: false,
                bodyField: "approvedDate",
                placeholder: "YYYY-MM-DD"
                },
                body_diplomaNumber: {
                label: "diplomaNumber (Diplom raqami)",
                type: "text",
                defaultNew: "01 № 123456",
                defaultOld: "01 № 232321",
                required: false,
                bodyField: "diplomaNumber"
                },
                body_active: {
                label: "active (Faol)",
                type: "select",
                options: [
                { value: "true", label: "true (Ha)" },
                { value: "false", label: "false (Yo'q)" }
                ],
                default: "true",
                required: false,
                bodyField: "active"
                }
                },
                bodyGenerator: (inputs) => ({
                defenseDate: inputs.body_defenseDate,
                defense_place: inputs.body_defense_place,
                approvedDate: inputs.body_approvedDate || undefined,
                diplomaNumber: inputs.body_diplomaNumber || undefined,
                active: inputs.body_active === 'true'
                }),
                description: `**Dissertasiya himoyasi yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EDissertationDefense
                <b>Response (OLD-HEMIS format):</b>
                <pre>{
                "_entityName": "hemishe_EDissertationDefense",
                "_instanceName": "com.company.hemishe.entity.EDissertationDefense-uuid [detached]",
                "id": "uuid-here"
                }</pre>`,
                ported: true,
                storeFirstId: "createdDissertationDefenseId"
                }
            {
                id: 121,
                category: "17.Ilmiy dissertasiya himoyalari",
                name: "Dissertasiya himoyasini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EDissertationDefense/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Dissertasiya himoyasi ID (UUID)",
                type: "text",
                placeholder: "Dissertasiya himoyasi UUID",
                defaultNew: "d5480757-8405-4aa4-60ed-15a07cf7dd46",
                defaultOld: "d5480757-8405-4aa4-60ed-15a07cf7dd46",
                required: true
                }
                },
                description: `**Dissertasiya himoyasini o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EDissertationDefense/{entityId}
                <b>Response:</b>
                - <b>200 OK</b> - Muvaffaqiyatli o'chirildi
                - <b>404 Not Found</b> - Topilmadi
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }
            {
                id: 122,
                category: "17.Ilmiy dissertasiya himoyalari",
                name: "Dissertasiya himoyasini olish (view=eDissertationDefense-view)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EDissertationDefense/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Dissertasiya himoyasi ID (UUID)",
                type: "text",
                placeholder: "Dissertasiya himoyasi UUID",
                defaultNew: "74649688-aef2-0171-da92-6eac77730bd2",
                defaultOld: "74649688-aef2-0171-da92-6eac77730bd2",
                required: true
                },
                view: {
                label: "View nomi",
                type: "text",
                default: "eDissertationDefense-view",
                required: true
                }
                },
                description: `**Dissertasiya himoyasini olish** (GET by ID with view)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EDissertationDefense/{entityId}?view=eDissertationDefense-view
                <b>Response (to'liq nested objectlar):</b>
                <pre>{
                "_entityName": "hemishe_EDissertationDefense",
                "id": "74649688-aef2-0171-da92-6eac77730bd2",
                "defenseDate": "2021-02-10",
                "doctorateStudent": {
                "_entityName": "hemishe_EDoctorateStudent",
                "_instanceName": "XAMDAMOVA DILFUZA",
                "id": "...",
                "firstName": "DILFUZA",
                "secondName": "XAMDAMOVA"
                },
                "speciality": {
                "_entityName": "hemishe_HSpecialityDoctoral",
                "id": "...",
                "name": "Hisoblash mashinalari..."
                }
                }</pre>`,
                ported: true
                }
            {
                id: 123,
                category: "17.Ilmiy dissertasiya himoyalari",
                name: "Dissertasiya himoyalari ro'yxati (view=eDissertationDefense-view)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EDissertationDefense",
                requiresAuth: true,
                inputFields: {
                view: {
                label: "View nomi",
                type: "text",
                default: "eDissertationDefense-view",
                required: true
                },
                limit: {
                label: "Limit",
                type: "text",
                default: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "text",
                default: "0",
                required: false
                }
                },
                description: `**Dissertasiya himoyalari ro'yxati** (GET list with view)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EDissertationDefense?view=eDissertationDefense-view&limit=10
                <b>Response:</b> Massiv formatida, to'liq nested objectlar bilan`,
                ported: true
                }

            // ============================================
            // 18.Ilmiy faollik (5 endpoint)
            // ============================================
            {
                id: 124,
                category: "18.Ilmiy faollik",
                name: "Ilmiy faoliyatlar ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EResearchActivity",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "text",
                default: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "text",
                default: "0",
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
                description: `**Ilmiy faoliyatlar ro'yxati** (GET all)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EResearchActivity
                <b>Response format:</b>
                <pre>[
                {
                "_entityName": "hemishe_EResearchActivity",
                "_instanceName": "com.company.hemishe.entity.EResearchActivity-{id} [detached]",
                "id": "uuid",
                "scientificWorkCount": "6",
                "link": "google scholar ...",
                "version": 1,
                "referenceCount": "22",
                "hIndex": "3"
                }
                ]</pre>`,
                ported: true,
                storeFirstId: "researchActivityId"
                }
            {
                id: 125,
                category: "18.Ilmiy faollik",
                name: "Ilmiy faoliyat yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EResearchActivity",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["hIndex", "scientificWorkCount", "referenceCount", "link"],
                inputFields: {
                body_hIndex: {
                label: "hIndex (H-indeks)",
                type: "text",
                defaultNew: "5",
                defaultOld: "3",
                required: true,
                bodyField: "hIndex",
                placeholder: "H-indeks raqami"
                },
                body_scientificWorkCount: {
                label: "scientificWorkCount (Ilmiy ishlar soni)",
                type: "text",
                defaultNew: "15",
                defaultOld: "10",
                required: true,
                bodyField: "scientificWorkCount"
                },
                body_referenceCount: {
                label: "referenceCount (Iqtiboslar soni)",
                type: "text",
                defaultNew: "30",
                defaultOld: "20",
                required: true,
                bodyField: "referenceCount"
                },
                body_link: {
                label: "link (Scholar profil havolasi)",
                type: "text",
                defaultNew: "https://scholar.google.com/test-new",
                defaultOld: "https://scholar.google.com/test-old",
                required: false,
                bodyField: "link",
                placeholder: "https://scholar.google.com/..."
                }
                },
                description: `**Ilmiy faoliyat yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EResearchActivity
                <b>Request body:</b>
                <pre>{
                "hIndex": "5",
                "scientificWorkCount": "15",
                "referenceCount": "30",
                "link": "https://scholar.google.com/..."
                }</pre>
                <b>Response:</b>
                <pre>{
                "_entityName": "hemishe_EResearchActivity",
                "_instanceName": "com.company.hemishe.entity.EResearchActivity-{id} [detached]",
                "id": "yangi_uuid"
                }</pre>`,
                ported: true,
                storeResultId: "researchActivityId"
                }
            {
                id: 126,
                category: "18.Ilmiy faollik",
                name: "Ilmiy faoliyatni olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EResearchActivity/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Ilmiy faoliyat ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "researchActivityId"
                },
                returnNulls: {
                label: "Return Nulls",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                default: "false",
                required: false
                }
                },
                description: `**Ilmiy faoliyatni olish** (GET by ID)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EResearchActivity/{entityId}
                <b>Response:</b> Bitta ilmiy faoliyat ma'lumotlari`,
                ported: true
                }
            {
                id: 127,
                category: "18.Ilmiy faollik",
                name: "Ilmiy faoliyatni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EResearchActivity/{entityId}",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["hIndex", "scientificWorkCount"],
                inputFields: {
                entityId: {
                label: "Ilmiy faoliyat ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "researchActivityId"
                },
                body_hIndex: {
                label: "hIndex (yangi qiymat)",
                type: "text",
                defaultNew: "7",
                defaultOld: "5",
                required: true,
                bodyField: "hIndex"
                },
                body_scientificWorkCount: {
                label: "scientificWorkCount (yangi qiymat)",
                type: "text",
                defaultNew: "20",
                defaultOld: "15",
                required: true,
                bodyField: "scientificWorkCount"
                }
                },
                description: `**Ilmiy faoliyatni yangilash** (PUT)
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EResearchActivity/{entityId}
                <b>Request body:</b>
                <pre>{
                "hIndex": "7",
                "scientificWorkCount": "20"
                }</pre>
                <b>Response:</b> Minimal response - _entityName, _instanceName, id`,
                ported: true
                }
            {
                id: 128,
                category: "18.Ilmiy faollik",
                name: "Ilmiy faoliyatni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EResearchActivity/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Ilmiy faoliyat ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "researchActivityId"
                }
                },
                description: `**Ilmiy faoliyatni o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EResearchActivity/{entityId}
                <b>Response:</b>
                - <b>200 OK</b> - Muvaffaqiyatli o'chirildi
                - <b>404 Not Found</b> - Topilmadi
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }

            // ============================================
            // 19.Ilmiy loyihalar (4 endpoint)
            // ============================================
            {
                id: 129,
                category: "19.Ilmiy loyihalar",
                name: "Loyiha yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EProject",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                body_name: {
                label: "name (Loyiha nomi)",
                type: "text",
                defaultNew: "Test loyiha - Yangi HEMIS",
                defaultOld: "Test loyiha - Eski HEMIS",
                required: true,
                bodyField: "name"
                },
                body_projectNumber: {
                label: "projectNumber (Loyiha raqami)",
                type: "text",
                defaultNew: "PRJ-NEW-001",
                defaultOld: "PRJ-OLD-001",
                required: false,
                bodyField: "projectNumber"
                },
                body_contractNumber: {
                label: "contractNumber (Shartnoma raqami)",
                type: "text",
                defaultNew: "CNT-2024-NEW-001",
                defaultOld: "CNT-2024-OLD-001",
                required: false,
                bodyField: "contractNumber"
                },
                body_contractDate: {
                label: "contractDate (Shartnoma sanasi)",
                type: "text",
                defaultNew: "2024-01-15",
                defaultOld: "2023-06-01",
                required: false,
                bodyField: "contractDate",
                placeholder: "YYYY-MM-DD"
                },
                body_startDate: {
                label: "startDate (Boshlanish sanasi)",
                type: "text",
                defaultNew: "2024-02-01",
                defaultOld: "2023-07-01",
                required: false,
                bodyField: "startDate",
                placeholder: "YYYY-MM-DD"
                },
                body_endDate: {
                label: "endDate (Tugash sanasi)",
                type: "text",
                defaultNew: "2024-12-31",
                defaultOld: "2024-06-30",
                required: false,
                bodyField: "endDate",
                placeholder: "YYYY-MM-DD"
                },
                body_position: {
                label: "position (Pozitsiya)",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: false,
                bodyField: "position"
                },
                body_active: {
                label: "active (Faol)",
                type: "select",
                options: [
                { value: "true", label: "true (Ha)" },
                { value: "false", label: "false (Yo'q)" }
                ],
                default: "true",
                required: false,
                bodyField: "active"
                }
                },
                bodyGenerator: (inputs) => ({
                name: inputs.body_name,
                projectNumber: inputs.body_projectNumber || undefined,
                contractNumber: inputs.body_contractNumber || undefined,
                contractDate: inputs.body_contractDate || undefined,
                startDate: inputs.body_startDate || undefined,
                endDate: inputs.body_endDate || undefined,
                position: inputs.body_position ? parseInt(inputs.body_position) : undefined,
                active: inputs.body_active === 'true'
                }),
                description: `**Yangi loyiha yaratish** (POST create)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EProject
                <b>Request Body:</b>
                - name - Loyiha nomi (String, required)
                - projectNumber - Loyiha raqami (String)
                - contractNumber - Shartnoma raqami (String)
                - contractDate - Shartnoma sanasi (YYYY-MM-DD)
                - startDate - Boshlanish sanasi (YYYY-MM-DD)
                - endDate - Tugash sanasi (YYYY-MM-DD)
                - position - Pozitsiya (Integer)
                - active - Faol holati (Boolean)
                <b>Response (OLD-HEMIS format):</b>
                <pre>{
                "_entityName": "hemishe_EProject",
                "_instanceName": "...",
                "id": "uuid-here"
                }</pre>`,
                ported: true,
                storeResultId: "createdProjectId"
                }
            {
                id: 130,
                category: "19.Ilmiy loyihalar",
                name: "Loyihani olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EProject/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: true,
                placeholder: "UUID yoki {createdProjectId}",
                useStoredId: "createdProjectId"
                },
                returnNulls: {
                label: "Return Nulls",
                type: "text",
                defaultNew: "true",
                defaultOld: "true",
                required: false
                }
                },
                queryParamsFromInputs: ["returnNulls"],
                description: `**Loyihani ID bo'yicha olish** (GET by ID)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProject/{entityId}?returnNulls=true
                <b>Path Parameters:</b>
                - entityId - Loyiha UUID (required)
                <b>Query Parameters:</b>
                - returnNulls - null qiymatlarni qaytarish (default: true)
                <b>Response:</b> Loyiha to'liq ma'lumotlari`,
                ported: true
                }
            {
                id: 131,
                category: "19.Ilmiy loyihalar",
                name: "Loyihalar ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EProject",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "text",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "text",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                },
                returnNulls: {
                label: "Return Nulls",
                type: "text",
                defaultNew: "true",
                defaultOld: "true",
                required: false
                }
                },
                queryParamsFromInputs: ["limit", "offset", "returnNulls"],
                description: `**Loyihalar ro'yxati** (GET list)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProject?limit=10&offset=0
                <b>Query Parameters:</b>
                - limit - Qaytariladigan yozuvlar soni (default: 10)
                - offset - O'tkazib yuboriladigan yozuvlar (default: 0)
                - returnNulls - null qiymatlarni qaytarish (default: true)
                <b>Response:</b> Massiv formatida loyihalar ro'yxati`,
                ported: true
                }
            {
                id: 132,
                category: "19.Ilmiy loyihalar",
                name: "Loyihani o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EProject/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: true,
                placeholder: "UUID yoki {createdProjectId}",
                useStoredId: "createdProjectId"
                }
                },
                description: `**Loyihani o'chirish** (DELETE - Soft delete)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EProject/{entityId}
                <b>Path Parameters:</b>
                - entityId - Loyiha UUID (required)
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }

            // ============================================
            // 20.Ilmiy loyiha meta ma'lumotlari (5 endpoint)
            // ============================================
            {
                id: 133,
                category: "20.Ilmiy loyiha meta ma'lumotlari",
                name: "Loyiha meta yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EProjectMeta",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_fiscalYear", "body_budget", "body_quantityMembers", "body_active"],
                inputFields: {
                body_fiscalYear: {
                label: "fiscalYear (Moliyaviy yil)",
                type: "text",
                defaultNew: "2025",
                defaultOld: "2024",
                required: true,
                bodyField: "fiscalYear",
                placeholder: "2024"
                },
                body_budget: {
                label: "budget (Byudjet)",
                type: "text",
                defaultNew: "50000000.0",
                defaultOld: "30000000.0",
                required: true,
                bodyField: "budget",
                placeholder: "50000000.0"
                },
                body_quantityMembers: {
                label: "quantityMembers (A'zolar soni)",
                type: "text",
                defaultNew: "5",
                defaultOld: "3",
                required: true,
                bodyField: "quantityMembers"
                },
                body_active: {
                label: "active (Faol)",
                type: "select",
                options: [{value: "true", label: "true (Ha)"}, {value: "false", label: "false (Yo'q)"}],
                default: "true",
                required: false,
                bodyField: "active"
                }
                },
                bodyGenerator: (inputs) => ({
                fiscalYear: parseInt(inputs.body_fiscalYear) || 2025,
                budget: parseFloat(inputs.body_budget) || 50000000.0,
                quantityMembers: parseInt(inputs.body_quantityMembers) || 5,
                active: inputs.body_active === 'true'
                }),
                description: `**Loyiha meta yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EProjectMeta
                <b>Request body:</b>
                <pre>{
                "fiscalYear": 2025,
                "budget": 50000000.0,
                "quantityMembers": 5,
                "active": true
                }</pre>
                <b>Response:</b>
                <pre>{
                "_entityName": "hemishe_EProjectMeta",
                "_instanceName": "com.company.hemishe.entity.EProjectMeta-{id} [detached]",
                "id": "yangi_uuid"
                }</pre>`,
                ported: true,
                storeResultId: "projectMetaId"
                }
            {
                id: 134,
                category: "20.Ilmiy loyiha meta ma'lumotlari",
                name: "Loyiha meta olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EProjectMeta/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Loyiha meta ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "projectMetaId"
                }
                },
                description: `**Loyiha meta olish** (GET by ID)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProjectMeta/{entityId}
                <b>Response:</b> Bitta loyiha meta ma'lumotlari`,
                ported: true
                }
            {
                id: 135,
                category: "20.Ilmiy loyiha meta ma'lumotlari",
                name: "Loyiha meta ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EProjectMeta",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "text",
                default: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "text",
                default: "0",
                required: false
                }
                },
                description: `**Loyiha meta ma'lumotlari ro'yxati** (GET all)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProjectMeta
                <b>Response format:</b>
                <pre>[
                {
                "_entityName": "hemishe_EProjectMeta",
                "_instanceName": "com.company.hemishe.entity.EProjectMeta-{id} [detached]",
                "id": "uuid",
                "quantityMembers": 4,
                "active": true,
                "version": 1,
                "fiscalYear": 2018,
                "budget": 10000000.0
                }
                ]</pre>`,
                ported: true
                }
            {
                id: 136,
                category: "20.Ilmiy loyiha meta ma'lumotlari",
                name: "Loyiha meta yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EProjectMeta/{entityId}",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["budget", "quantityMembers"],
                inputFields: {
                entityId: {
                label: "Loyiha meta ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "projectMetaId"
                },
                body_budget: {
                label: "budget (yangi qiymat)",
                type: "text",
                defaultNew: "75000000.0",
                defaultOld: "60000000.0",
                required: true,
                bodyField: "budget"
                },
                body_quantityMembers: {
                label: "quantityMembers (yangi qiymat)",
                type: "text",
                defaultNew: "10",
                defaultOld: "8",
                required: true,
                bodyField: "quantityMembers"
                }
                },
                description: `**Loyiha meta yangilash** (PUT)
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EProjectMeta/{entityId}
                <b>Response:</b> Minimal response - _entityName, _instanceName, id`,
                ported: true
                }
            {
                id: 137,
                category: "20.Ilmiy loyiha meta ma'lumotlari",
                name: "Loyiha meta o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EProjectMeta/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Loyiha meta ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "projectMetaId"
                }
                },
                description: `**Loyiha meta o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EProjectMeta/{entityId}
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }

            // ============================================
            // 21.Ilmiy loyiha ijrochilari (3 endpoint)
            // ============================================
            {
                id: 138,
                category: "21.Ilmiy loyiha ijrochilari",
                name: "Loyiha ijrochisi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EProjectExecutor",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                body_outsider: {
                label: "outsider (Tashqi ijrochi nomi)",
                type: "text",
                defaultNew: "Test Ijrochi - Yangi HEMIS",
                defaultOld: "Test Ijrochi - Eski HEMIS",
                required: false,
                bodyField: "outsider"
                },
                body_startDate: {
                label: "startDate (Boshlanish sanasi)",
                type: "text",
                defaultNew: "2024-01-01",
                defaultOld: "2024-01-01",
                required: false,
                bodyField: "startDate",
                placeholder: "YYYY-MM-DD"
                },
                body_endDate: {
                label: "endDate (Tugash sanasi)",
                type: "text",
                defaultNew: "2024-12-31",
                defaultOld: "2024-12-31",
                required: false,
                bodyField: "endDate",
                placeholder: "YYYY-MM-DD"
                },
                body_active: {
                label: "active (Faol)",
                type: "select",
                options: [
                { value: "true", label: "true (Ha)" },
                { value: "false", label: "false (Yo'q)" }
                ],
                default: "true",
                required: false,
                bodyField: "active"
                }
                },
                hasBody: true,
                bodyFields: ["body_outsider", "body_startDate", "body_endDate", "body_active"],
                bodyGenerator: (inputs) => ({
                outsider: inputs.body_outsider || undefined,
                startDate: inputs.body_startDate || undefined,
                endDate: inputs.body_endDate || undefined,
                active: inputs.body_active === 'true'
                }),
                description: `**Yangi loyiha ijrochisi yaratish** (POST create)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EProjectExecutor
                <b>Request Body:</b>
                - outsider - Tashqi ijrochi nomi (String)
                - project - Loyiha {id: "uuid"} (UUID, optional)
                - projectExecutorType - Ijrochi turi {id: "uuid"} (UUID, optional)
                - startDate - Boshlanish sanasi (YYYY-MM-DD)
                - endDate - Tugash sanasi (YYYY-MM-DD)
                - active - Faol holati (Boolean)
                <b>Response (OLD-HEMIS format):</b>
                <pre>{
                "_entityName": "hemishe_EProjectExecutor",
                "_instanceName": "com.company.hemishe.entity.EProjectExecutor-uuid [detached]",
                "id": "uuid-here"
                }</pre>`,
                ported: true,
                storeResultId: "createdProjectExecutorId"
                }
            {
                id: 139,
                category: "21.Ilmiy loyiha ijrochilari",
                name: "Loyiha ijrochisini olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EProjectExecutor/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: true,
                placeholder: "UUID yoki {createdProjectExecutorId}",
                useStoredId: "createdProjectExecutorId"
                },
                returnNulls: {
                label: "Return Nulls",
                type: "text",
                defaultNew: "true",
                defaultOld: "true",
                required: false
                }
                },
                queryParamsFromInputs: ["returnNulls"],
                description: `**Loyiha ijrochisini ID bo'yicha olish** (GET by ID)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProjectExecutor/{entityId}?returnNulls=true
                <b>Path Parameters:</b>
                - entityId - Loyiha ijrochisi UUID (required)
                <b>Query Parameters:</b>
                - returnNulls - null qiymatlarni qaytarish (default: true)
                <b>Response:</b> Loyiha ijrochisi to'liq ma'lumotlari`,
                ported: true
                }
            {
                id: 140,
                category: "21.Ilmiy loyiha ijrochilari",
                name: "Loyiha ijrochisini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EProjectExecutor/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: true,
                placeholder: "UUID yoki {createdProjectExecutorId}",
                useStoredId: "createdProjectExecutorId"
                }
                },
                description: `**Loyiha ijrochisini o'chirish** (DELETE - Soft delete)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EProjectExecutor/{entityId}
                <b>Path Parameters:</b>
                - entityId - Loyiha ijrochisi UUID (required)
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }

            // ============================================
            // 22.Ilmiy nashrlar (5 endpoint)
            // ============================================
            {
                id: 141,
                category: "22.Ilmiy nashrlar",
                name: "Ilmiy nashr yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EPublicationScientific",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_name", "body_authors", "body_authorCounts", "body_issueYear", "body_doi", "body_active"],
                inputFields: {
                body_name: {
                label: "name (Nashr nomi)",
                type: "text",
                defaultNew: "Quantum Computing in Medicine - NEW",
                defaultOld: "Quantum Computing in Medicine - OLD",
                required: true,
                bodyField: "name",
                placeholder: "Ilmiy nashr nomi"
                },
                body_authors: {
                label: "authors (Mualliflar)",
                type: "text",
                defaultNew: "Aliyev A., Karimov B.",
                defaultOld: "Aliyev A., Karimov B.",
                required: true,
                bodyField: "authors",
                placeholder: "Aliyev A., Karimov B."
                },
                body_authorCounts: {
                label: "authorCounts (Mualliflar soni)",
                type: "text",
                defaultNew: "2",
                defaultOld: "2",
                required: true,
                bodyField: "authorCounts"
                },
                body_issueYear: {
                label: "issueYear (Nashr yili)",
                type: "text",
                defaultNew: "2025",
                defaultOld: "2024",
                required: true,
                bodyField: "issueYear"
                },
                body_doi: {
                label: "doi (DOI)",
                type: "text",
                defaultNew: "10.1234/example.2025",
                defaultOld: "10.1234/example.2024",
                required: false,
                bodyField: "doi",
                placeholder: "10.1234/example.2024"
                },
                body_active: {
                label: "active (Faol)",
                type: "select",
                options: [{value: "true", label: "true"}, {value: "false", label: "false"}],
                defaultNew: "true",
                defaultOld: "true",
                required: true,
                bodyField: "active"
                }
                },
                description: `**Ilmiy nashr yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EPublicationScientific
                <b>Request body:</b>
                <pre>{
                "name": "Quantum Computing in Medicine",
                "authors": "Aliyev A., Karimov B.",
                "authorCounts": 2,
                "issueYear": 2024,
                "doi": "10.1234/example.2024",
                "active": true
                }</pre>
                <b>Response:</b>
                <pre>{
                "_entityName": "hemishe_EPublicationScientific",
                "_instanceName": "com.company.hemishe.entity.EPublicationScientific-{id} [detached]",
                "id": "yangi_uuid"
                }</pre>`,
                ported: true,
                storeResultId: "publicationScientificId"
                }
            {
                id: 142,
                category: "22.Ilmiy nashrlar",
                name: "Ilmiy nashr olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Ilmiy nashr ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationScientificId"
                }
                },
                description: `**Ilmiy nashr olish** (GET by ID)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}
                <b>Response:</b> Bitta ilmiy nashr ma'lumotlari`,
                ported: true
                }
            {
                id: 143,
                category: "22.Ilmiy nashrlar",
                name: "Ilmiy nashrlar ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationScientific",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "text",
                defaultNew: "10",
                defaultOld: "10",
                default: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "text",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                },
                returnNulls: {
                label: "Return Nulls",
                type: "text",
                defaultNew: "true",
                defaultOld: "true",
                required: false
                }
                },
                queryParamsFromInputs: ["limit", "offset", "returnNulls"],
                description: `**Loyiha ijrochilari ro'yxati** (GET list)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EProjectExecutor?limit=10&offset=0
                <b>Query Parameters:</b>
                - limit - Qaytariladigan yozuvlar soni (default: 10)
                - offset - O'tkazib yuboriladigan yozuvlar (default: 0)
                - returnNulls - null qiymatlarni qaytarish (default: true)
                <b>Response:</b> Massiv formatida loyiha ijrochilari ro'yxati`,
                ported: true
                }
            {
                id: 144,
                category: "22.Ilmiy nashrlar",
                name: "Ilmiy nashr yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_name", "body_authorCounts"],
                inputFields: {
                entityId: {
                label: "Ilmiy nashr ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationScientificId"
                },
                body_name: {
                label: "name (yangi qiymat)",
                type: "text",
                defaultNew: "Updated Scientific Publication - NEW",
                defaultOld: "Updated Scientific Publication - OLD",
                required: true,
                bodyField: "name"
                },
                body_authorCounts: {
                label: "authorCounts (yangi qiymat)",
                type: "text",
                defaultNew: "5",
                defaultOld: "4",
                required: true,
                bodyField: "authorCounts"
                }
                },
                description: `**Ilmiy nashr yangilash** (PUT)
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}
                <b>Response:</b> Minimal response - _entityName, _instanceName, id`,
                ported: true
                }
            {
                id: 145,
                category: "22.Ilmiy nashrlar",
                name: "Ilmiy nashr o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Ilmiy nashr ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationScientificId"
                }
                },
                description: `**Ilmiy nashr o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EPublicationScientific/{entityId}
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }

            // ============================================
            // 23.Ilmiy ishlanmalar (5 endpoint)
            // ============================================
            {
                id: 146,
                category: "23.Ilmiy ishlanmalar",
                name: "Ilmiy ishlanma yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EPublicationProperty",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                name: {
                label: "Nomi",
                type: "text",
                placeholder: "Ilmiy ishlanma nomi",
                defaultNew: "Test ilmiy ishlanma (Yangi)",
                defaultOld: "Test ilmiy ishlanma (Eski)",
                required: true
                },
                numbers: {
                label: "Raqami",
                type: "text",
                placeholder: "FAP 00123",
                defaultNew: "FAP 00123",
                defaultOld: "FAP 00123",
                required: false
                },
                authors: {
                label: "Mualliflar",
                type: "text",
                placeholder: "Aliyev A., Karimov B.",
                defaultNew: "Test Author",
                defaultOld: "Test Author",
                required: false
                },
                authorCounts: {
                label: "Mualliflar soni",
                type: "number",
                placeholder: "2",
                defaultNew: "1",
                defaultOld: "1",
                required: false
                },
                active: {
                label: "Faol",
                type: "select",
                options: [
                { value: "true", label: "Ha" },
                { value: "false", label: "Yo'q" }
                ],
                defaultNew: "true",
                defaultOld: "true",
                required: false
                }
                },
                body: {
                name: "{name}",
                numbers: "{numbers}",
                authors: "{authors}",
                authorCounts: "{authorCounts}",
                active: "{active}"
                },
                storeResultId: "publicationPropertyId",
                description: `**Ilmiy ishlanma yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EPublicationProperty
                <b>Request Body:</b>
                \`\`\`json
                {
                "name": "Yangi ixtiro nomi",
                "numbers": "FAP 00123",
                "authors": "Aliyev A., Karimov B.",
                "authorCounts": 2,
                "active": true
                }
                \`\`\`
                <b>Response:</b> Minimal response - _entityName, _instanceName, id`,
                ported: true
                }
            {
                id: 147,
                category: "23.Ilmiy ishlanmalar",
                name: "Barcha ilmiy ishlanmalar",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationProperty",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "10",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                }
                },
                params: {
                limit: "{limit}",
                offset: "{offset}"
                },
                storeFirstId: "publicationPropertyId",
                description: `**Barcha ilmiy ishlanmalar ro'yxati** (GET list)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationProperty?offset=0&limit=10
                <b>Response:</b> Ro'yxat (array of entities)`,
                ported: true
                }
            {
                id: 148,
                category: "23.Ilmiy ishlanmalar",
                name: "Ilmiy ishlanmani olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Ilmiy ishlanma ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationPropertyId"
                }
                },
                description: `**Ilmiy ishlanmani olish** (GET by ID)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}
                <b>Response:</b> To'liq entity ma'lumotlari`,
                ported: true
                }
            {
                id: 149,
                category: "23.Ilmiy ishlanmalar",
                name: "Ilmiy ishlanmani yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                entityId: {
                label: "Ilmiy ishlanma ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationPropertyId"
                },
                name: {
                label: "Yangi nom",
                type: "text",
                placeholder: "Yangilangan nom",
                defaultNew: "Yangilangan test ishlanma",
                defaultOld: "Yangilangan test ishlanma",
                required: false
                },
                active: {
                label: "Faol",
                type: "select",
                options: [
                { value: "true", label: "Ha" },
                { value: "false", label: "Yo'q" }
                ],
                defaultNew: "true",
                defaultOld: "true",
                required: false
                }
                },
                body: {
                name: "{name}",
                active: "{active}"
                },
                description: `**Ilmiy ishlanmani yangilash** (PUT)
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}
                <b>Request Body:</b>
                \`\`\`json
                {
                "name": "Yangilangan nom",
                "active": true
                }
                \`\`\`
                <b>Response:</b> Minimal response - _entityName, _instanceName, id`,
                ported: true
                }
            {
                id: 150,
                category: "23.Ilmiy ishlanmalar",
                name: "Ilmiy ishlanma o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Ilmiy ishlanma ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationPropertyId"
                }
                },
                description: `**Ilmiy ishlanma o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EPublicationProperty/{entityId}
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }

            // ============================================
            // 24.Ilmiy uslubiy nashlar (5 endpoint)
            // ============================================
            {
                id: 151,
                category: "24.Ilmiy uslubiy nashlar",
                name: "Yangi uslubiy nashr yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EPublicationMethodical",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                _university: {
                label: "Universitet kodi",
                type: "text",
                placeholder: "401 yoki 351",
                defaultNew: "401",
                defaultOld: "351",
                required: true
                },
                name: {
                label: "Nomi",
                type: "text",
                placeholder: "Uslubiy nashr nomi",
                defaultNew: "Test uslubiy nashr (Yangi Hemis)",
                defaultOld: "Test uslubiy nashr (Eski Hemis)",
                required: true
                },
                authors: {
                label: "Mualliflar",
                type: "text",
                placeholder: "Familiya I.O., Familiya I.O.",
                defaultNew: "Test A.B., Namuna C.D.",
                defaultOld: "Test A.B., Namuna C.D.",
                required: false
                },
                author_counts: {
                label: "Mualliflar soni",
                type: "number",
                placeholder: "2",
                defaultNew: "2",
                defaultOld: "2",
                required: false
                },
                publisher: {
                label: "Nashriyot",
                type: "text",
                placeholder: "Nashriyot nomi",
                defaultNew: "Test Nashriyot",
                defaultOld: "Test Nashriyot",
                required: false
                },
                issue_year: {
                label: "Chiqish yili",
                type: "number",
                placeholder: "2024",
                defaultNew: "2024",
                defaultOld: "2024",
                required: false
                },
                active: {
                label: "Faol",
                type: "checkbox",
                defaultNew: true,
                defaultOld: true,
                required: false
                }
                },
                body: {
                _university: "{_university}",
                name: "{name}",
                authors: "{authors}",
                author_counts: "{author_counts}",
                publisher: "{publisher}",
                issue_year: "{issue_year}",
                active: "{active}"
                },
                description: `**Yangi uslubiy nashr yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EPublicationMethodical
                <b>Ma'lumotlar:</b>
                - _university: OTM kodi (401=Yangi, 351=Eski)
                - name: Uslubiy nashr nomi
                - authors: Mualliflar ro'yxati
                - author_counts: Mualliflar soni
                - publisher: Nashriyot
                - issue_year: Chiqish yili
                - source_name: Manba nomi
                - _methodical_publication_type: Nashr turi kodi
                - _publication_database: Baza kodi
                - _employee: Xodim UUID
                - _education_year: O'quv yili kodi
                - active: Faol holati
                <b>Response:</b> Yaratilgan entity CUBA formatda`,
                ported: true,
                storeResultId: "publicationMethodicalId"
                }
            {
                id: 152,
                category: "24.Ilmiy uslubiy nashlar",
                name: "Uslubiy nashrlar ro'yxatini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationMethodical",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                }
                },
                params: {
                limit: "{limit}",
                offset: "{offset}"
                },
                description: `**Uslubiy nashrlar ro'yxati** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationMethodical
                <b>Parametrlar:</b>
                - limit: Natijalar soni (default: 50)
                - offset: Boshlang'ich pozitsiya
                - returnNulls: Null qiymatlarni qaytarish
                - view: Ko'rinish nomi
                <b>Response:</b> Uslubiy nashrlar massivi`,
                ported: true
                }
            {
                id: 153,
                category: "24.Ilmiy uslubiy nashlar",
                name: "Uslubiy nashrni ID bo'yicha olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Uslubiy nashr ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationMethodicalId"
                }
                },
                description: `**Uslubiy nashrni ID bo'yicha olish** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}
                <b>Response:</b> Uslubiy nashr ma'lumotlari CUBA formatda`,
                ported: true
                }
            {
                id: 154,
                category: "24.Ilmiy uslubiy nashlar",
                name: "Uslubiy nashrni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                entityId: {
                label: "Uslubiy nashr ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationMethodicalId"
                },
                name: {
                label: "Yangi nom",
                type: "text",
                placeholder: "Uslubiy nashr nomi",
                defaultNew: "Yangilangan uslubiy nashr",
                defaultOld: "Yangilangan uslubiy nashr",
                required: false
                },
                active: {
                label: "Faol",
                type: "checkbox",
                defaultNew: true,
                defaultOld: true,
                required: false
                }
                },
                body: {
                name: "{name}",
                active: "{active}"
                },
                description: `**Uslubiy nashrni yangilash** (PUT)
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}
                <b>Body:</b> O'zgartiriladigan maydonlar JSON formatda
                <b>Response:</b> Yangilangan entity CUBA formatda`,
                ported: true
                }
            {
                id: 155,
                category: "24.Ilmiy uslubiy nashlar",
                name: "Uslubiy nashrni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Uslubiy nashr ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationMethodicalId"
                }
                },
                description: `**Uslubiy nashrni o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EPublicationMethodical/{entityId}
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }

            // ============================================
            // 25.Ilmiy nashr mualliflari meta ma'lumotlari (5 endpoint)
            // ============================================
            {
                id: 156,
                category: "25.Ilmiy nashr mualliflari meta ma'lumotlari",
                name: "Nashr muallifi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EPublicationAuthorMeta",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_employee", "body_university", "body_publicationTypeTable", "body_isMainAuthor", "body_isCheckedByAuthor", "body_active"],
                inputFields: {
                body_employee: {
                label: "Xodim ID (UUID)",
                type: "text",
                placeholder: "Teacher UUID",
                defaultNew: "6b3c0dfc-e269-3df5-894e-85b8c2386e9d",
                defaultOld: "1d2f4cda-79df-3de6-e15f-434a3f044b5f",
                required: true,
                bodyField: "employee",
                cubaForeignKey: true
                },
                body_university: {
                label: "Universitet kodi",
                type: "text",
                placeholder: "401",
                defaultNew: "401",
                defaultOld: "351",
                required: true,
                bodyField: "university",
                cubaForeignKey: true,
                cubaForeignKeyField: "code"
                },
                body_publicationTypeTable: {
                label: "Nashr turi",
                type: "select",
                options: [
                { value: "scientific", label: "Ilmiy (scientific)" },
                { value: "property", label: "Ishlanma (property)" },
                { value: "methodic", label: "Metodik (methodic)" }
                ],
                defaultNew: "scientific",
                defaultOld: "scientific",
                required: true,
                bodyField: "publicationTypeTable"
                },
                body_isMainAuthor: {
                label: "Asosiy muallif",
                type: "select",
                options: [
                { value: "1", label: "Ha (1)" },
                { value: "0", label: "Yo'q (0)" }
                ],
                defaultNew: "1",
                defaultOld: "1",
                required: false,
                bodyField: "isMainAuthor"
                },
                body_isCheckedByAuthor: {
                label: "Muallif tomonidan tekshirilgan",
                type: "select",
                options: [
                { value: "true", label: "Ha" },
                { value: "false", label: "Yo'q" }
                ],
                defaultNew: "true",
                defaultOld: "true",
                required: false,
                bodyField: "isCheckedByAuthor"
                },
                body_active: {
                label: "Faol",
                type: "select",
                options: [
                { value: "true", label: "Ha" },
                { value: "false", label: "Yo'q" }
                ],
                defaultNew: "true",
                defaultOld: "true",
                required: false,
                bodyField: "active"
                }
                },
                storeResultId: "publicationAuthorMetaId",
                description: `**Nashr muallifi yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EPublicationAuthorMeta
                <b>Request Body:</b>
                \`\`\`json
                {
                "employee": "teacher-uuid",
                "university": "401",
                "publicationTypeTable": "scientific",
                "isMainAuthor": "1",
                "isCheckedByAuthor": "true",
                "active": "true"
                }
                \`\`\`
                <b>Response:</b> Yaratilgan entity CUBA formatda - nested objectlar bilan`,
                ported: true
                }
            {
                id: 157,
                category: "25.Ilmiy nashr mualliflari meta ma'lumotlari",
                name: "Nashr mualliflari ro'yxati",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationAuthorMeta",
                requiresAuth: true,
                inputFields: {
                offset: {
                label: "Offset",
                type: "text",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                },
                limit: {
                label: "Limit",
                type: "text",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                }
                },
                queryParamsFromInputs: ["offset", "limit"],
                description: "Barcha nashr mualliflari meta ma'lumotlari ro'yxatini olish",
                ported: true
                }
            {
                id: 158,
                category: "25.Ilmiy nashr mualliflari meta ma'lumotlari",
                name: "Nashr muallifi olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Nashr muallifi ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationAuthorMetaId"
                }
                },
                description: `**Nashr muallifi olish** (GET by ID)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}
                <b>Response:</b> Bitta entity CUBA formatda`,
                ported: true
                }
            {
                id: 159,
                category: "25.Ilmiy nashr mualliflari meta ma'lumotlari",
                name: "Nashr muallifini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_isMainAuthor", "body_isCheckedByAuthor"],
                inputFields: {
                entityId: {
                label: "Nashr muallifi ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationAuthorMetaId"
                },
                body_isMainAuthor: {
                label: "isMainAuthor (yangi qiymat)",
                type: "select",
                options: [
                { value: "1", label: "Ha (1)" },
                { value: "0", label: "Yo'q (0)" }
                ],
                defaultNew: "0",
                defaultOld: "0",
                required: true,
                bodyField: "isMainAuthor"
                },
                body_isCheckedByAuthor: {
                label: "isCheckedByAuthor (yangi qiymat)",
                type: "select",
                options: [
                { value: "true", label: "Ha" },
                { value: "false", label: "Yo'q" }
                ],
                defaultNew: "false",
                defaultOld: "false",
                required: true,
                bodyField: "isCheckedByAuthor"
                }
                },
                description: `**Nashr muallifini yangilash** (PUT)
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}
                <b>Body:</b> O'zgartiriladigan maydonlar JSON formatda
                <b>Response:</b> Yangilangan entity CUBA formatda`,
                ported: true
                }
            {
                id: 160,
                category: "25.Ilmiy nashr mualliflari meta ma'lumotlari",
                name: "Nashr muallifini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Nashr muallifi ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationAuthorMetaId"
                }
                },
                description: `**Nashr muallifini o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EPublicationAuthorMeta/{entityId}
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }

            // ============================================
            // 26.Ilmiy nashrlarni baholash mezonlari (5 endpoint)
            // ============================================
            {
                id: 161,
                category: "26.Ilmiy nashrlarni baholash mezonlari",
                name: "Yangi baholash mezoni yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EPublicationCriteria",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                _university: {
                label: "Universitet kodi",
                type: "text",
                placeholder: "401 yoki 351",
                defaultNew: "401",
                defaultOld: "351",
                required: true
                },
                _education_year: {
                label: "O'quv yili",
                type: "text",
                placeholder: "2024",
                defaultNew: "2024",
                defaultOld: "2024",
                required: false
                },
                _publication_type_table: {
                label: "Nashr turi jadvali",
                type: "text",
                placeholder: "hemishe_EPublicationScientific",
                defaultNew: "hemishe_EPublicationScientific",
                defaultOld: "hemishe_EPublicationScientific",
                required: false
                },
                markValue: {
                label: "Ball qiymati",
                type: "number",
                placeholder: "10",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                active: {
                label: "Faol",
                type: "checkbox",
                defaultNew: true,
                defaultOld: true,
                required: false
                }
                },
                body: {
                _university: "{_university}",
                _education_year: "{_education_year}",
                _publication_type_table: "{_publication_type_table}",
                markValue: "{markValue}",
                active: "{active}"
                },
                description: `**Yangi baholash mezoni yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EPublicationCriteria
                <b>Ma'lumotlar:</b>
                - _university: OTM kodi (401=Yangi, 351=Eski)
                - _education_year: O'quv yili
                - _publication_type_table: Nashr turi jadvali nomi
                - _publication_methodical_type: Uslubiy nashr turi
                - _publication_scientific_type: Ilmiy nashr turi
                - _publication_property_type: Intellektual mulk turi
                - inPublicationDatabase: Bazada mavjudligi (1/0)
                - markValue: Ball qiymati
                - position: Tartib raqami
                - active: Faol holati
                <b>Response:</b> Yaratilgan entity CUBA formatda`,
                ported: true,
                storeResultId: "publicationCriteriaId"
                }
            {
                id: 162,
                category: "26.Ilmiy nashrlarni baholash mezonlari",
                name: "Baholash mezonlari ro'yxatini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationCriteria",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                }
                },
                queryParamsFromInputs: ["limit", "offset"],
                description: `**Baholash mezonlari ro'yxati** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationCriteria
                <b>Parametrlar:</b>
                - limit: Natijalar soni (default: 50)
                - offset: Boshlang'ich pozitsiya
                - returnNulls: Null qiymatlarni qaytarish
                - view: Ko'rinish nomi
                <b>Response:</b> Baholash mezonlari massivi`,
                ported: true,
                storeFirstId: "publicationCriteriaId"
                }
            {
                id: 163,
                category: "26.Ilmiy nashrlarni baholash mezonlari",
                name: "Baholash mezonini ID bo'yicha olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Baholash mezoni ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationCriteriaId"
                }
                },
                description: `**Baholash mezonini ID bo'yicha olish** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}
                <b>Response:</b> Baholash mezoni ma'lumotlari CUBA formatda`,
                ported: true
                }
            {
                id: 164,
                category: "26.Ilmiy nashrlarni baholash mezonlari",
                name: "Baholash mezonini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}",
                requiresAuth: true,
                contentType: "json",
                hasBody: true,
                bodyFields: ["body_markValue", "body_active"],
                inputFields: {
                entityId: {
                label: "Baholash mezoni ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationCriteriaId"
                },
                body_markValue: {
                label: "Yangi ball qiymati",
                type: "number",
                placeholder: "15",
                defaultNew: "15",
                defaultOld: "15",
                required: false,
                bodyField: "markValue"
                },
                body_active: {
                label: "Faol",
                type: "checkbox",
                defaultNew: true,
                defaultOld: true,
                required: false,
                bodyField: "active"
                }
                },
                description: `**Baholash mezonini yangilash** (PUT)
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}
                <b>Body:</b> O'zgartiriladigan maydonlar JSON formatda
                <b>Response:</b> Yangilangan entity CUBA formatda`,
                ported: true
                }
            {
                id: 165,
                category: "26.Ilmiy nashrlarni baholash mezonlari",
                name: "Baholash mezonini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Baholash mezoni ID (UUID)",
                type: "text",
                placeholder: "UUID formatda ID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "publicationCriteriaId"
                }
                },
                description: `**Baholash mezonini o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_EPublicationCriteria/{entityId}
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }

            // ============================================
            // 27.Ilmiy uslubiy nashr turlari (7 endpoint)
            // ============================================
            {
                id: 166,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Barcha turlarni olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "50",
                defaultOld: "50",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                },
                returnNulls: {
                label: "Return Nulls",
                type: "select",
                options: [
                { value: "false", label: "false (default)" },
                { value: "true", label: "true" }
                ],
                defaultNew: "false",
                defaultOld: "false",
                required: false
                }
                },
                queryParamsFromInputs: ["limit", "offset", "returnNulls"],
                description: `**Barcha uslubiy nashr turlarini olish** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HMethodicalPublicationType
                <b>Parameters:</b>
                - limit: Sahifa hajmi (default: 50)
                - offset: Boshlang'ich pozitsiya (default: 0)
                - returnNulls: Null qiymatlarni qaytarish (default: false)
                <b>Response:</b> Uslubiy nashr turlari ro'yxati CUBA formatda`,
                ported: true
                }
            {
                id: 167,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Tur ma'lumotini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Tur kodi",
                type: "text",
                placeholder: "98 yoki 99",
                defaultNew: "98",
                defaultOld: "99",
                required: true
                }
                },
                description: `**Uslubiy nashr turi ma'lumotini olish** (GET by ID)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}
                <b>Response:</b> Tur ma'lumotlari CUBA formatda`,
                ported: true
                }
            {
                id: 168,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Yangi tur yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["code", "name", "nameRu", "nameEn"],
                inputFields: {
                code: {
                label: "Kod (unique)",
                type: "text",
                placeholder: "98 yoki 99",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                bodyField: "code"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                placeholder: "Tur nomi",
                defaultNew: "Test Turi NEW",
                defaultOld: "Test Turi OLD",
                required: true,
                bodyField: "name"
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                placeholder: "Tur nomi (ruscha)",
                defaultNew: "Тестовый тип NEW",
                defaultOld: "Тестовый тип OLD",
                required: false,
                bodyField: "nameRu"
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                placeholder: "Tur nomi (inglizcha)",
                defaultNew: "Test Type NEW",
                defaultOld: "Test Type OLD",
                required: false,
                bodyField: "nameEn"
                }
                },
                description: `**Yangi uslubiy nashr turi yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HMethodicalPublicationType
                <b>Body:</b> JSON formatda tur ma'lumotlari
                <b>Response:</b> Yaratilgan tur CUBA formatda`,
                ported: true
                }
            {
                id: 169,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Turni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["name", "nameRu", "nameEn"],
                inputFields: {
                entityId: {
                label: "Tur kodi",
                type: "text",
                placeholder: "98 yoki 99",
                defaultNew: "98",
                defaultOld: "99",
                required: true
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                placeholder: "Yangi nom",
                defaultNew: "Test Turi Updated NEW",
                defaultOld: "Test Turi Updated OLD",
                required: false,
                bodyField: "name"
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                placeholder: "Yangi nom (ruscha)",
                defaultNew: "Обновленный тип NEW",
                defaultOld: "Обновленный тип OLD",
                required: false,
                bodyField: "nameRu"
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                placeholder: "Yangi nom (inglizcha)",
                defaultNew: "Updated Type NEW",
                defaultOld: "Updated Type OLD",
                required: false,
                bodyField: "nameEn"
                }
                },
                description: `**Uslubiy nashr turini yangilash** (PUT)
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}
                <b>Body:</b> O'zgartiriladigan maydonlar JSON formatda
                <b>Response:</b> Yangilangan tur CUBA formatda`,
                ported: true
                }
            {
                id: 170,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Turni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Tur kodi",
                type: "text",
                placeholder: "98 yoki 99",
                defaultNew: "98",
                defaultOld: "99",
                required: true
                }
                },
                description: `**Uslubiy nashr turini o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_HMethodicalPublicationType/{entityId}
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }
            {
                id: 171,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Filter bo'yicha qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter JSON (majburiy)",
                type: "text",
                placeholder: '{"conditions":[]}',
                defaultNew: '{"conditions":[]}',
                defaultOld: '{"conditions":[]}',
                required: true
                },
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                }
                },
                queryParamsFromInputs: ["filter", "limit", "offset"],
                description: `**Filter bo'yicha qidirish** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HMethodicalPublicationType/search
                <b>Parametrlar:</b>
                - filter: JSON filter (majburiy) - {"conditions":[]}
                - limit: Natijalar soni (default: 50)
                - offset: Boshlang'ich pozitsiya
                <b>Response:</b> Filter shartlariga mos entitylar massivi`,
                ported: true
                }
            {
                id: 172,
                category: "27.Ilmiy uslubiy nashr turlari",
                name: "Filter bo'yicha qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/search",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                }
                },
                hasBody: true,
                bodyTemplate: {
                "filter": {
                "conditions": []
                }
                },
                queryParamsFromInputs: ["limit", "offset"],
                description: `**Filter bo'yicha qidirish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HMethodicalPublicationType/search
                <b>Body:</b> {"filter":{"conditions":[]}} formatida
                <b>Parametrlar:</b>
                - limit: Natijalar soni (default: 50)
                - offset: Boshlang'ich pozitsiya
                <b>Response:</b> Filter shartlariga mos entitylar massivi`,
                ported: true
                }

            // ============================================
            // 28.Ilmiy doktorantura talabalari statusi (7 endpoint)
            // ============================================
            {
                id: 173,
                category: "28.Ilmiy doktorantura talabalari statusi",
                name: "Barcha statuslarni olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "50",
                defaultOld: "50",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                },
                returnNulls: {
                label: "Return Nulls",
                type: "select",
                options: [
                { value: "false", label: "false (default)" },
                { value: "true", label: "true" }
                ],
                defaultNew: "false",
                defaultOld: "false",
                required: false
                }
                },
                queryParamsFromInputs: ["limit", "offset", "returnNulls"],
                description: `**Barcha doktorantura talabasi statuslarini olish** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentStatus
                <b>Parameters:</b>
                - limit: Sahifa hajmi (default: 50)
                - offset: Boshlang'ich pozitsiya (default: 0)
                - returnNulls: Null qiymatlarni qaytarish (default: false)
                <b>Response:</b> Status ro'yxati CUBA formatda`,
                ported: true
                }
            {
                id: 174,
                category: "28.Ilmiy doktorantura talabalari statusi",
                name: "Status olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Status kodi",
                type: "text",
                placeholder: "Status kodi (masalan: 11)",
                defaultNew: "11",
                defaultOld: "11",
                required: true,
                useStoredId: "doctoralStudentStatusCode"
                }
                },
                description: `**Status ID bo'yicha olish** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}
                <b>Parameters:</b>
                - entityId: Status kodi (code field - String)
                <b>Response:</b> Status CUBA formatda`,
                ported: true
                }
            {
                id: 175,
                category: "28.Ilmiy doktorantura talabalari statusi",
                name: "Yangi status yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["code", "name", "nameRu", "nameEn"],
                inputFields: {
                code: {
                label: "Status kodi",
                type: "text",
                placeholder: "Status kodi (masalan: 98 yoki 99)",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                bodyField: "code"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                placeholder: "Status nomi",
                defaultNew: "Test Status",
                defaultOld: "Test Status",
                required: true,
                bodyField: "name"
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                placeholder: "Status nomi (ruscha)",
                defaultNew: "Тестовый статус",
                defaultOld: "Тестовый статус",
                required: false,
                bodyField: "nameRu"
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                placeholder: "Status nomi (inglizcha)",
                defaultNew: "Test Status",
                defaultOld: "Test Status",
                required: false,
                bodyField: "nameEn"
                }
                },
                storeResultId: "doctoralStudentStatusCode",
                storeIdField: "code",
                description: `**Yangi doktorantura talabasi statusi yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HDoctoralStudentStatus
                <b>Body:</b> JSON formatda status ma'lumotlari
                <b>Response:</b> Yaratilgan status CUBA formatda`,
                ported: true
                }
            {
                id: 176,
                category: "28.Ilmiy doktorantura talabalari statusi",
                name: "Statusni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["name", "nameRu", "nameEn", "active"],
                inputFields: {
                entityId: {
                label: "Status kodi",
                type: "text",
                placeholder: "Status kodi (98 yoki 99)",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                useStoredId: "doctoralStudentStatusCode"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                placeholder: "Yangi nom",
                defaultNew: "Test Status Updated",
                defaultOld: "Test Status Updated",
                required: false,
                bodyField: "name"
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                placeholder: "Yangi nom (ruscha)",
                defaultNew: "Обновленный статус",
                defaultOld: "Обновленный статус",
                required: false,
                bodyField: "nameRu"
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                placeholder: "Yangi nom (inglizcha)",
                defaultNew: "",
                defaultOld: "",
                required: false,
                bodyField: "nameEn"
                },
                active: {
                label: "Faolmi?",
                type: "select",
                options: [
                { value: "true", label: "Ha" },
                { value: "false", label: "Yo'q" }
                ],
                defaultNew: "true",
                defaultOld: "true",
                required: false,
                bodyField: "active"
                }
                },
                description: `**Statusni yangilash** (PUT)
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}
                <b>Body:</b> O'zgartiriladigan maydonlar JSON formatda
                <b>Response:</b> Yangilangan status CUBA formatda`,
                ported: true
                }
            {
                id: 177,
                category: "28.Ilmiy doktorantura talabalari statusi",
                name: "Statusni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Status kodi",
                type: "text",
                placeholder: "Status kodi (98 yoki 99)",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                useStoredId: "doctoralStudentStatusCode"
                }
                },
                description: `**Statusni o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{entityId}
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }
            {
                id: 178,
                category: "28.Ilmiy doktorantura talabalari statusi",
                name: "Statuslarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter JSON (majburiy)",
                type: "text",
                placeholder: '{"conditions":[]}',
                defaultNew: '{"conditions":[]}',
                defaultOld: '{"conditions":[]}',
                required: true
                },
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                }
                },
                queryParamsFromInputs: ["filter", "limit", "offset"],
                description: `**Statuslarni qidirish** (GET /search)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentStatus/search
                <b>Parametrlar:</b>
                - filter: JSON filter (majburiy) - {"conditions":[]}
                - limit: Natijalar soni (default: 50)
                - offset: Boshlang'ich pozitsiya
                <b>Response:</b> Filter shartiga mos statuslar`,
                ported: true
                }
            {
                id: 179,
                category: "28.Ilmiy doktorantura talabalari statusi",
                name: "Statuslarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/search",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                }
                },
                hasBody: true,
                bodyTemplate: {
                "filter": {
                "conditions": []
                }
                },
                queryParamsFromInputs: ["limit", "offset"],
                description: `**Statuslarni qidirish** (POST /search)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HDoctoralStudentStatus/search
                <b>Body:</b> {"filter":{"conditions":[]}} formatida
                <b>Parametrlar:</b>
                - limit: Natijalar soni (default: 50)
                - offset: Boshlang'ich pozitsiya
                <b>Response:</b> Filter shartiga mos statuslar`,
                ported: true
                }

            // ============================================
            // 29.Ilmiy doktorantura talabalari turlari (7 endpoint)
            // ============================================
            {
                id: 180,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Barcha turlarni olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "50",
                defaultOld: "50",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                },
                returnNulls: {
                label: "Return Nulls",
                type: "select",
                options: [
                { value: "false", label: "false (default)" },
                { value: "true", label: "true" }
                ],
                defaultNew: "false",
                defaultOld: "false",
                required: false
                }
                },
                queryParamsFromInputs: ["limit", "offset", "returnNulls"],
                description: `**Barcha doktorantura talabasi turlarini olish** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentType
                <b>Parameters:</b>
                - limit: Sahifa hajmi (default: 50)
                - offset: Boshlang'ich pozitsiya (default: 0)
                - returnNulls: Null qiymatlarni qaytarish (default: false)
                <b>Response:</b> Tur ro'yxati CUBA formatda`,
                ported: true
                }
            {
                id: 181,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Tur olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Tur kodi",
                type: "text",
                placeholder: "Tur kodi (masalan: 11)",
                defaultNew: "11",
                defaultOld: "11",
                required: true,
                useStoredId: "doctoralStudentTypeCode"
                }
                },
                description: `**Tur ID bo'yicha olish** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}
                <b>Parameters:</b>
                - entityId: Tur kodi (code field - String)
                <b>Response:</b> Tur CUBA formatda`,
                ported: true
                }
            {
                id: 182,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Yangi tur yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["code", "name", "nameRu", "nameEn"],
                inputFields: {
                code: {
                label: "Tur kodi",
                type: "text",
                placeholder: "Tur kodi (masalan: 98 yoki 99)",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                bodyField: "code"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                placeholder: "Tur nomi",
                defaultNew: "Test Tur",
                defaultOld: "Test Tur",
                required: true,
                bodyField: "name"
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                placeholder: "Tur nomi (ruscha)",
                defaultNew: "Тестовый тип",
                defaultOld: "Тестовый тип",
                required: false,
                bodyField: "nameRu"
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                placeholder: "Tur nomi (inglizcha)",
                defaultNew: "Test Type",
                defaultOld: "Test Type",
                required: false,
                bodyField: "nameEn"
                }
                },
                storeResultId: "doctoralStudentTypeCode",
                storeIdField: "code",
                description: `**Yangi doktorantura talabasi turi yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HDoctoralStudentType
                <b>Body:</b> JSON formatda tur ma'lumotlari
                <b>Response:</b> Yaratilgan tur CUBA formatda`,
                ported: true
                }
            {
                id: 183,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Turni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["name", "nameRu", "nameEn", "active"],
                inputFields: {
                entityId: {
                label: "Tur kodi",
                type: "text",
                placeholder: "Tur kodi (98 yoki 99)",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                useStoredId: "doctoralStudentTypeCode"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                placeholder: "Yangi nom",
                defaultNew: "Test Tur Updated",
                defaultOld: "Test Tur Updated",
                required: false,
                bodyField: "name"
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                placeholder: "Yangi nom (ruscha)",
                defaultNew: "Обновленный тип",
                defaultOld: "Обновленный тип",
                required: false,
                bodyField: "nameRu"
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                placeholder: "Yangi nom (inglizcha)",
                defaultNew: "",
                defaultOld: "",
                required: false,
                bodyField: "nameEn"
                },
                active: {
                label: "Faolmi?",
                type: "select",
                options: [
                { value: "true", label: "Ha" },
                { value: "false", label: "Yo'q" }
                ],
                defaultNew: "true",
                defaultOld: "true",
                required: false,
                bodyField: "active"
                }
                },
                description: `**Turni yangilash** (PUT)
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}
                <b>Body:</b> O'zgartiriladigan maydonlar JSON formatda
                <b>Response:</b> Yangilangan tur CUBA formatda`,
                ported: true
                }
            {
                id: 184,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Turni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Tur kodi",
                type: "text",
                placeholder: "Tur kodi (98 yoki 99)",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                useStoredId: "doctoralStudentTypeCode"
                }
                },
                description: `**Turni o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_HDoctoralStudentType/{entityId}
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }
            {
                id: 185,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Turlarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter JSON (majburiy)",
                type: "text",
                placeholder: '{"conditions":[]}',
                defaultNew: '{"conditions":[]}',
                defaultOld: '{"conditions":[]}',
                required: true
                },
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                }
                },
                queryParamsFromInputs: ["filter", "limit", "offset"],
                description: `**Turlarni qidirish** (GET /search)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HDoctoralStudentType/search
                <b>Parametrlar:</b>
                - filter: JSON filter (majburiy) - {"conditions":[]}
                - limit: Natijalar soni (default: 50)
                - offset: Boshlang'ich pozitsiya
                <b>Response:</b> Filter shartiga mos turlar`,
                ported: true
                }
            {
                id: 186,
                category: "29.Ilmiy doktorantura talabalari turlari",
                name: "Turlarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HDoctoralStudentType/search",
                requiresAuth: true,
                contentType: "json",
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                placeholder: "50",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                offset: {
                label: "Offset",
                type: "number",
                placeholder: "0",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                }
                },
                hasBody: true,
                bodyTemplate: {
                "filter": {
                "conditions": []
                }
                },
                queryParamsFromInputs: ["limit", "offset"],
                description: `**Turlarni qidirish** (POST /search)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HDoctoralStudentType/search
                <b>Body:</b> {"filter":{"conditions":[]}} formatida
                <b>Parametrlar:</b>
                - limit: Natijalar soni (default: 50)
                - offset: Boshlang'ich pozitsiya
                <b>Response:</b> Filter shartiga mos turlar`,
                ported: true
                }

            // ============================================
            // 30.Ilmiy nashr etish hududlari turlari (7 endpoint)
            // ============================================
            {
                id: 187,
                category: "30.Ilmiy nashr etish hududlari turlari",
                name: "Barcha hududlarni olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality",
                requiresAuth: true,
                inputFields: {
                offset: {
                label: "Offset",
                type: "number",
                defaultNew: "0",
                defaultOld: "0",
                required: false
                },
                limit: {
                label: "Limit",
                type: "number",
                defaultNew: "50",
                defaultOld: "50",
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
                description: `**Barcha nashr etish hududlarini olish** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HPublicationLocality
                <b>Parameters:</b>
                - limit: Sahifa hajmi (default: 50)
                - offset: Boshlang'ich pozitsiya (default: 0)
                - returnNulls: Null qiymatlarni qaytarish (default: false)
                <b>Response:</b> Hudud ro'yxati CUBA formatda`,
                ported: true
                }
            {
                id: 188,
                category: "30.Ilmiy nashr etish hududlari turlari",
                name: "Hudud olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Hudud kodi",
                type: "text",
                placeholder: "Hudud kodi (masalan: 11)",
                defaultNew: "11",
                defaultOld: "11",
                required: true,
                useStoredId: "publicationLocalityCode"
                }
                },
                description: `**Hudud ID bo'yicha olish** (GET)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}
                <b>Parameters:</b>
                - entityId: Hudud kodi (code field - String)
                <b>Response:</b> Hudud CUBA formatda`,
                ported: true
                }
            {
                id: 189,
                category: "30.Ilmiy nashr etish hududlari turlari",
                name: "Yangi hudud yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["code", "name", "nameRu", "nameEn"],
                inputFields: {
                code: {
                label: "Hudud kodi",
                type: "text",
                placeholder: "Hudud kodi (masalan: 98 yoki 99)",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                bodyField: "code"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                placeholder: "Hudud nomi",
                defaultNew: "Test Hudud",
                defaultOld: "Test Hudud",
                required: true,
                bodyField: "name"
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                placeholder: "Hudud nomi (ruscha)",
                defaultNew: "Тестовая местность",
                defaultOld: "Тестовая местность",
                required: false,
                bodyField: "nameRu"
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                placeholder: "Hudud nomi (inglizcha)",
                defaultNew: "Test Locality",
                defaultOld: "Test Locality",
                required: false,
                bodyField: "nameEn"
                }
                },
                storeResultId: "publicationLocalityCode",
                storeIdField: "code",
                description: `**Yangi nashr etish hududi yaratish** (POST)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HPublicationLocality
                <b>Body:</b> JSON formatda hudud ma'lumotlari
                <b>Response:</b> Yaratilgan hudud CUBA formatda`,
                ported: true
                }
            {
                id: 190,
                category: "30.Ilmiy nashr etish hududlari turlari",
                name: "Hududni yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}",
                requiresAuth: true,
                hasBody: true,
                bodyFields: ["name", "nameRu", "nameEn", "active"],
                inputFields: {
                entityId: {
                label: "Hudud kodi",
                type: "text",
                placeholder: "Hudud kodi (98 yoki 99)",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                useStoredId: "publicationLocalityCode"
                },
                name: {
                label: "Nomi (O'zbekcha)",
                type: "text",
                placeholder: "Yangi nom",
                defaultNew: "Test Hudud Updated",
                defaultOld: "Test Hudud Updated",
                required: false,
                bodyField: "name"
                },
                nameRu: {
                label: "Nomi (Ruscha)",
                type: "text",
                placeholder: "Yangi nom (ruscha)",
                defaultNew: "Обновленная местность",
                defaultOld: "Обновленная местность",
                required: false,
                bodyField: "nameRu"
                },
                nameEn: {
                label: "Nomi (Inglizcha)",
                type: "text",
                placeholder: "Yangi nom (inglizcha)",
                defaultNew: "",
                defaultOld: "",
                required: false,
                bodyField: "nameEn"
                },
                active: {
                label: "Faolmi?",
                type: "select",
                options: [
                { value: "true", label: "Ha" },
                { value: "false", label: "Yo'q" }
                ],
                defaultNew: "true",
                defaultOld: "true",
                required: false,
                bodyField: "active"
                }
                },
                description: `**Hududni yangilash** (PUT)
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}
                <b>Body:</b> O'zgartiriladigan maydonlar JSON formatda
                <b>Response:</b> Yangilangan hudud CUBA formatda`,
                ported: true
                }
            {
                id: 191,
                category: "30.Ilmiy nashr etish hududlari turlari",
                name: "Hududni o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Hudud kodi",
                type: "text",
                placeholder: "Hudud kodi (98 yoki 99)",
                defaultNew: "98",
                defaultOld: "99",
                required: true,
                useStoredId: "publicationLocalityCode"
                }
                },
                description: `**Hududni o'chirish** (DELETE)
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_HPublicationLocality/{entityId}
                <b>Response:</b> 200 OK (empty body)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }
            {
                id: 192,
                category: "30.Ilmiy nashr etish hududlari turlari",
                name: "Hududlarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "text",
                placeholder: '{"conditions":[{"property":"name","operator":"contains","value":"a"}]}',
                defaultNew: '{"conditions":[{"property":"name","operator":"contains","value":"a"}]}',
                defaultOld: '{"conditions":[{"property":"name","operator":"contains","value":"a"}]}',
                required: true
                }
                },
                description: `**Hududlarni qidirish** (GET /search)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_HPublicationLocality/search
                <b>Parameters:</b>
                - filter: CUBA JSON format (ikkala tizim uchun bir xil)
                <b>Response:</b> Filter shartiga mos hududlar`,
                ported: true
                }
            {
                id: 193,
                category: "30.Ilmiy nashr etish hududlari turlari",
                name: "Hududlarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_HPublicationLocality/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"code","operator":"notEmpty"}]}',
                defaultNew: '{"conditions":[{"property":"code","operator":"notEmpty"}]}',
                defaultOld: '{"conditions":[{"property":"code","operator":"notEmpty"}]}',
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
                description: `**Hududlarni qidirish** (POST /search)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_HPublicationLocality/search
                <b>Body:</b> CUBA format filter JSON
                <pre>{"filter":{"conditions":[{"property":"code","operator":"notEmpty"}]}}</pre>
                <b>Response:</b> Filter shartiga mos hududlar`,
                ported: true
                }

            // ============================================
            // 31.Akademik hisobotlar chetlashgan talabalar (7 endpoint)
            // ============================================
            {
                id: 194,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Barcha chetlashgan talabalar yozuvlarini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RExpel",
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
                description: `**Barcha chetlashgan talabalar yozuvlarini olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RExpel
                <b>Parameters:</b>
                - limit: Natija soni limiti (default: 50)
                - offset: Sahifalash uchun offset
                <b>Response:</b> Chetlashgan talabalar ro'yxati CUBA formatda`,
                ported: true
                }
            {
                id: 195,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Chetlashgan talaba yozuvini olish (ID bo'yicha)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RExpel/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "#1 dan yoki #3 dan qaytgan ID ni kiriting",
                defaultNew: "",
                defaultOld: "",
                required: true
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
                description: `**Chetlashgan talaba yozuvini ID bo'yicha olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RExpel/{entityId}
                <b>Parameters:</b>
                - entityId: Yozuv ID (UUID format)
                <b>Response:</b> Yozuv CUBA formatda`,
                ported: true
                }
            {
                id: 196,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Yangi chetlashgan talaba yozuvi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RExpel",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                universityCode: {
                label: "Universitet kodi",
                type: "text",
                placeholder: "401",
                defaultNew: "401",
                defaultOld: "351",
                required: false
                },
                universityName: {
                label: "Universitet nomi",
                type: "text",
                placeholder: "O'zbekiston Milliy universiteti",
                defaultNew: "O'zbekiston Milliy universiteti",
                defaultOld: "Samarqand davlat universiteti",
                required: false
                },
                facultyCode: {
                label: "Fakultet kodi",
                type: "text",
                placeholder: "401-101",
                defaultNew: "401-101",
                defaultOld: "351-101",
                required: false
                },
                facultyName: {
                label: "Fakultet nomi",
                type: "text",
                placeholder: "Matematika fakulteti",
                defaultNew: "Matematika fakulteti",
                defaultOld: "Fizika fakulteti",
                required: false
                },
                educationTypeCode: {
                label: "Ta'lim turi kodi",
                type: "text",
                placeholder: "11",
                defaultNew: "11",
                defaultOld: "11",
                required: false
                },
                educationTypeName: {
                label: "Ta'lim turi nomi",
                type: "text",
                placeholder: "Kunduzgi",
                defaultNew: "Kunduzgi",
                defaultOld: "Kunduzgi",
                required: false
                },
                educationYearCode: {
                label: "O'quv yili kodi",
                type: "text",
                placeholder: "2024",
                defaultNew: "2024",
                defaultOld: "2024",
                required: false
                },
                educationYearName: {
                label: "O'quv yili nomi",
                type: "text",
                placeholder: "2024-2025",
                defaultNew: "2024-2025",
                defaultOld: "2024-2025",
                required: false
                },
                semesterTypeCode: {
                label: "Semestr turi kodi",
                type: "text",
                placeholder: "1",
                defaultNew: "1",
                defaultOld: "1",
                required: false
                },
                semesterTypeName: {
                label: "Semestr turi nomi",
                type: "text",
                placeholder: "Kuz semestri",
                defaultNew: "Kuz semestri",
                defaultOld: "Kuz semestri",
                required: false
                },
                courseCode: {
                label: "Kurs kodi",
                type: "text",
                placeholder: "1",
                defaultNew: "1",
                defaultOld: "2",
                required: false
                },
                courseName: {
                label: "Kurs nomi",
                type: "text",
                placeholder: "1-kurs",
                defaultNew: "1-kurs",
                defaultOld: "2-kurs",
                required: false
                },
                expelReasonCode: {
                label: "Chetlashtirish sababi kodi",
                type: "text",
                placeholder: "11",
                defaultNew: "11",
                defaultOld: "12",
                required: false
                },
                expelReasonName: {
                label: "Chetlashtirish sababi nomi",
                type: "text",
                placeholder: "O'z xohishiga binoan",
                defaultNew: "O'z xohishiga binoan",
                defaultOld: "O'qishni ko'chirish sababli",
                required: false
                },
                expelCount: {
                label: "Chetlashgan talabalar soni",
                type: "number",
                placeholder: "5",
                defaultNew: "3",
                defaultOld: "2",
                required: false
                }
                },
                bodyFields: ["universityCode", "universityName", "facultyCode", "facultyName", "educationTypeCode", "educationTypeName", "educationYearCode", "educationYearName", "semesterTypeCode", "semesterTypeName", "courseCode", "courseName", "expelReasonCode", "expelReasonName", "expelCount"],
                description: `**Yangi chetlashgan talaba yozuvi yaratish**
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RExpel
                <b>Body:</b> JSON formatda yozuv ma'lumotlari
                <pre>{
                "universityCode": "401",
                "universityName": "O'zbekiston Milliy universiteti",
                "facultyCode": "FAKU001",
                "facultyName": "Matematika fakulteti",
                "educationTypeCode": "11",
                "educationTypeName": "Kunduzgi",
                "expelReasonCode": "01",
                "expelReasonName": "O'z ixtiyori bilan",
                "expelCount": 5
                }</pre>
                <b>Response:</b> Yaratilgan yozuv CUBA formatda`,
                ported: true
                }
            {
                id: 197,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Chetlashgan talaba yozuvini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RExpel/{entityId}",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "#3 dan qaytgan ID ni kiriting",
                defaultNew: "",
                defaultOld: "",
                required: true
                },
                universityCode: {
                label: "Universitet kodi",
                type: "text",
                placeholder: "401",
                defaultNew: "401",
                defaultOld: "351",
                required: false
                },
                universityName: {
                label: "Universitet nomi",
                type: "text",
                placeholder: "O'zbekiston Milliy universiteti",
                defaultNew: "O'zbekiston Milliy universiteti (YANGILANGAN)",
                defaultOld: "Samarqand davlat universiteti (YANGILANGAN)",
                required: false
                },
                facultyCode: {
                label: "Fakultet kodi",
                type: "text",
                placeholder: "401-102",
                defaultNew: "401-102",
                defaultOld: "351-102",
                required: false
                },
                facultyName: {
                label: "Fakultet nomi",
                type: "text",
                placeholder: "Informatika fakulteti",
                defaultNew: "Informatika fakulteti",
                defaultOld: "Kimyo fakulteti",
                required: false
                },
                educationTypeCode: {
                label: "Ta'lim turi kodi",
                type: "text",
                placeholder: "12",
                defaultNew: "12",
                defaultOld: "12",
                required: false
                },
                educationTypeName: {
                label: "Ta'lim turi nomi",
                type: "text",
                placeholder: "Sirtqi",
                defaultNew: "Sirtqi",
                defaultOld: "Sirtqi",
                required: false
                },
                educationYearCode: {
                label: "O'quv yili kodi",
                type: "text",
                placeholder: "2025",
                defaultNew: "2025",
                defaultOld: "2025",
                required: false
                },
                educationYearName: {
                label: "O'quv yili nomi",
                type: "text",
                placeholder: "2025-2026",
                defaultNew: "2025-2026",
                defaultOld: "2025-2026",
                required: false
                },
                semesterTypeCode: {
                label: "Semestr turi kodi",
                type: "text",
                placeholder: "2",
                defaultNew: "2",
                defaultOld: "2",
                required: false
                },
                semesterTypeName: {
                label: "Semestr turi nomi",
                type: "text",
                placeholder: "Bahor semestri",
                defaultNew: "Bahor semestri",
                defaultOld: "Bahor semestri",
                required: false
                },
                courseCode: {
                label: "Kurs kodi",
                type: "text",
                placeholder: "3",
                defaultNew: "3",
                defaultOld: "4",
                required: false
                },
                courseName: {
                label: "Kurs nomi",
                type: "text",
                placeholder: "3-kurs",
                defaultNew: "3-kurs",
                defaultOld: "4-kurs",
                required: false
                },
                expelReasonCode: {
                label: "Chetlashtirish sababi kodi",
                type: "text",
                placeholder: "13",
                defaultNew: "13",
                defaultOld: "14",
                required: false
                },
                expelReasonName: {
                label: "Chetlashtirish sababi nomi",
                type: "text",
                placeholder: "Akademik qarzdorlik",
                defaultNew: "Akademik qarzdorlik",
                defaultOld: "Intizom buzarlik",
                required: false
                },
                expelCount: {
                label: "Chetlashgan talabalar soni",
                type: "number",
                placeholder: "10",
                defaultNew: "5",
                defaultOld: "7",
                required: false
                }
                },
                bodyFields: ["universityCode", "universityName", "facultyCode", "facultyName", "educationTypeCode", "educationTypeName", "educationYearCode", "educationYearName", "semesterTypeCode", "semesterTypeName", "courseCode", "courseName", "expelReasonCode", "expelReasonName", "expelCount"],
                description: `**Chetlashgan talaba yozuvini yangilash**
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RExpel/{entityId}
                <b>Parameters:</b>
                - entityId: Yozuv ID (UUID format)
                <b>Body:</b> JSON formatda yangilash ma'lumotlari
                <b>Response:</b> Yangilangan yozuv CUBA formatda`,
                ported: true
                }
            {
                id: 198,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Chetlashgan talaba yozuvini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RExpel/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "#3 yoki #4 dan qaytgan ID ni kiriting",
                defaultNew: "",
                defaultOld: "",
                required: true
                }
                },
                description: `**Chetlashgan talaba yozuvini o'chirish**
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RExpel/{entityId}
                <b>Parameters:</b>
                - entityId: Yozuv ID (UUID format)
                <b>⚠️ Eslatma:</b> Soft delete - delete_ts ustuni belgilanadi!`,
                ported: true
                }
            {
                id: 199,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Chetlashgan talabalarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RExpel/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                defaultNew: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                defaultOld: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                required: true,
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
                description: `**Chetlashgan talabalarni qidirish** (GET /search)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RExpel/search?filter=...
                <b>Parameters:</b>
                - filter: CUBA format filter JSON (URL encoded)
                - limit, offset: Sahifalash
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }
            {
                id: 200,
                category: "31.Akademik hisobotlar chetlashgan talabalar",
                name: "Chetlashgan talabalarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RExpel/search",
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
                description: `**Chetlashgan talabalarni qidirish** (POST /search)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RExpel/search
                <b>Body:</b> CUBA format filter JSON
                <pre>{"filter":{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}}</pre>
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }

            // ============================================
            // 32.Akademik hisobotlar akademik guruhlar (7 endpoint)
            // ============================================
            {
                id: 201,
                category: "32.Akademik hisobotlar akademik guruhlar",
                name: "Barcha akademik guruhlar yozuvlarini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup",
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
                returnCount: {
                label: "Umumiy sonni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                defaultNew: "",
                defaultOld: "",
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
                description: `**Barcha akademik guruhlar yozuvlarini olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicGroup
                <b>Parameters:</b>
                - limit: Sahifa hajmi (default: 50)
                - offset: Boshlanish indeksi (default: 0)
                - returnCount: Umumiy sonni X-Total-Count headerda qaytarish
                - returnNulls: Null qiymatlarni ham qaytarish
                <b>Response:</b> Sahifalangan akademik guruhlar ro'yxati`,
                ported: true
                }
            {
                id: 202,
                category: "32.Akademik hisobotlar akademik guruhlar",
                name: "Akademik guruh yozuvini ID bo'yicha olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                defaultNew: "",
                defaultOld: "",
                required: true
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
                description: `**Akademik guruh yozuvini ID bo'yicha olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}
                <b>Parameters:</b>
                - entityId: Yozuv UUID si (path parameter)
                - returnNulls: Null qiymatlarni ham qaytarish
                <b>Response:</b> Topilgan akademik guruh yozuvi yoki 404`,
                ported: true
                }
            {
                id: 203,
                category: "32.Akademik hisobotlar akademik guruhlar",
                name: "Yangi akademik guruh yozuvi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup",
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
                defaultNew: "Test Universiteti (NEW)",
                defaultOld: "Test Universiteti (OLD)",
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
                educationFormCode: {
                label: "Ta'lim shakli kodi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: false
                },
                educationFormName: {
                label: "Ta'lim shakli nomi",
                type: "text",
                defaultNew: "Kunduzgi",
                defaultOld: "Kunduzgi",
                required: false
                },
                educationYearCode: {
                label: "O'quv yili kodi",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2024",
                required: false
                },
                educationYearName: {
                label: "O'quv yili nomi",
                type: "text",
                defaultNew: "2024-2025",
                defaultOld: "2024-2025",
                required: false
                },
                groupCount: {
                label: "Guruhlar soni",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
                required: false
                },
                updateDate: {
                label: "Yangilangan sana",
                type: "date",
                defaultNew: new Date().toISOString().split('T')[0],
                defaultOld: new Date().toISOString().split('T')[0],
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["universityCode", "universityName", "educationTypeCode", "educationTypeName", "educationFormCode", "educationFormName", "educationYearCode", "educationYearName", "groupCount", "updateDate"],
                description: `**Yangi akademik guruh yozuvi yaratish**
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicGroup
                <b>Body:</b>
                <pre>{
                "universityCode": "401",
                "universityName": "Test Universiteti",
                "educationTypeCode": "11",
                "educationTypeName": "Bakalavr",
                "educationFormCode": "11",
                "educationFormName": "Kunduzgi",
                "educationYearCode": "2024",
                "educationYearName": "2024-2025",
                "groupCount": 10,
                "updateDate": "2024-01-01"
                }</pre>
                <b>Response:</b> Yaratilgan akademik guruh yozuvi`,
                ported: true
                }
            {
                id: 204,
                category: "32.Akademik hisobotlar akademik guruhlar",
                name: "Akademik guruh yozuvini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                defaultNew: "",
                defaultOld: "",
                required: true
                },
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
                defaultNew: "Yangilangan Universitet (NEW)",
                defaultOld: "Yangilangan Universitet (OLD)",
                required: false
                },
                groupCount: {
                label: "Guruhlar soni",
                type: "number",
                defaultNew: "15",
                defaultOld: "15",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["universityCode", "universityName", "groupCount"],
                description: `**Akademik guruh yozuvini yangilash**
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}
                <b>Parameters:</b>
                - entityId: Yozuv UUID si (path parameter)
                <b>Body:</b>
                <pre>{
                "universityCode": "401",
                "universityName": "Yangilangan Universitet",
                "groupCount": 15
                }</pre>
                <b>Response:</b> Yangilangan akademik guruh yozuvi yoki 404`,
                ported: true
                }
            {
                id: 205,
                category: "32.Akademik hisobotlar akademik guruhlar",
                name: "Akademik guruh yozuvini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                defaultNew: "",
                defaultOld: "",
                required: true
                }
                },
                description: `**Akademik guruh yozuvini o'chirish (soft delete)**
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RAcademicGroup/{entityId}
                <b>Parameters:</b>
                - entityId: Yozuv UUID si (path parameter)
                <b>Response:</b> 200 OK yoki 404 Not Found
                <b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
                ported: true
                }
            {
                id: 206,
                category: "32.Akademik hisobotlar akademik guruhlar",
                name: "Akademik guruhlarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                defaultNew: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                defaultOld: '{"conditions":[{"property":"universityCode","operator":"=","value":"351"}]}',
                required: true,
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
                returnCount: {
                label: "Umumiy sonni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                defaultNew: "",
                defaultOld: "",
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
                description: `**Akademik guruhlarni qidirish** (GET /search)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicGroup/search
                <b>Parameters:</b>
                - filter: CUBA format filter JSON (URL encoded)
                - limit, offset: Sahifalash
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }
            {
                id: 207,
                category: "32.Akademik hisobotlar akademik guruhlar",
                name: "Akademik guruhlarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicGroup/search",
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
                description: `**Akademik guruhlarni qidirish** (POST /search)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicGroup/search
                <b>Body:</b> CUBA format filter JSON
                <pre>{"filter":{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}}</pre>
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }

            // ============================================
            // 33.Akademik hisobotlar fanlar (7 endpoint)
            // ============================================
            {
                id: 208,
                category: "33.Akademik hisobotlar fanlar",
                name: "Barcha fanlar yozuvlarini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects",
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
                returnCount: {
                label: "Umumiy sonni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                defaultNew: "",
                defaultOld: "",
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
                description: `**Barcha fanlar yozuvlarini olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicSubjects
                <b>Parameters:</b>
                - limit: Sahifa hajmi (default: 50)
                - offset: Boshlanish indeksi (default: 0)
                - returnCount: Umumiy sonni X-Total-Count headerda qaytarish
                - returnNulls: Null qiymatlarni ham qaytarish
                <b>Response:</b> Sahifalangan fanlar ro'yxati`,
                ported: true
                }
            {
                id: 209,
                category: "33.Akademik hisobotlar fanlar",
                name: "Fan yozuvini ID bo'yicha olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                defaultNew: "",
                defaultOld: "",
                required: true
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
                description: `**Fan yozuvini ID bo'yicha olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}
                <b>Parameters:</b>
                - entityId: Yozuv UUID si (path parameter)
                - returnNulls: Null qiymatlarni ham qaytarish
                <b>Response:</b> Topilgan fan yozuvi yoki 404`,
                ported: true
                }
            {
                id: 210,
                category: "33.Akademik hisobotlar fanlar",
                name: "Yangi fan yozuvi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects",
                requiresAuth: true,
                inputFields: {
                universityCode: {
                label: "Universitet kodi",
                type: "text",
                defaultNew: "311",
                defaultOld: "313",
                required: false
                },
                universityName: {
                label: "Universitet nomi",
                type: "text",
                defaultNew: "Namangan muhandislik-texnologiya instituti",
                defaultOld: "Samarqand davlat arxitektura-qurilish universiteti",
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
                defaultNew: "2023",
                defaultOld: "2023",
                required: false
                },
                educationYearName: {
                label: "O'quv yili nomi",
                type: "text",
                defaultNew: "2023",
                defaultOld: "2023",
                required: false
                },
                curriculumCode: {
                label: "O'quv rejasi kodi",
                type: "text",
                defaultNew: "6",
                defaultOld: "4",
                required: false
                },
                curriculumName: {
                label: "O'quv rejasi nomi",
                type: "text",
                defaultNew: "240",
                defaultOld: "337",
                required: false
                },
                blockCode: {
                label: "Blok kodi",
                type: "text",
                defaultNew: "4",
                defaultOld: "4",
                required: false
                },
                blockName: {
                label: "Blok nomi",
                type: "text",
                defaultNew: "8",
                defaultOld: "4",
                required: false
                },
                subjectCount: {
                label: "Fanlar soni",
                type: "number",
                defaultNew: "1135",
                defaultOld: "1846",
                required: false
                },
                updateDate: {
                label: "Yangilangan sana",
                type: "date",
                defaultNew: new Date().toISOString().split('T')[0],
                defaultOld: new Date().toISOString().split('T')[0],
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["universityCode", "universityName", "educationTypeCode", "educationTypeName", "educationYearCode", "educationYearName", "curriculumCode", "curriculumName", "blockCode", "blockName", "subjectCount", "updateDate"],
                description: `**Yangi fan yozuvi yaratish**
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicSubjects
                <b>Body:</b>
                <pre>{
                "universityCode": "311",
                "universityName": "Namangan muhandislik-texnologiya instituti",
                "educationTypeCode": "11",
                "educationTypeName": "Bakalavr",
                "educationYearCode": "2024",
                "educationYearName": "2024-2025",
                "curriculumCode": "CS-2024",
                "curriculumName": "Kompyuter fanlari",
                "blockCode": "B1",
                "blockName": "Asosiy fanlar",
                "subjectCount": 15,
                "updateDate": "2024-01-01"
                }</pre>
                <b>Response:</b> Yaratilgan fan yozuvi`,
                ported: true
                }
            {
                id: 211,
                category: "33.Akademik hisobotlar fanlar",
                name: "Fan yozuvini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                defaultNew: "",
                defaultOld: "",
                required: true
                },
                universityCode: {
                label: "Universitet kodi",
                type: "text",
                defaultNew: "311",
                defaultOld: "313",
                required: false
                },
                universityName: {
                label: "Universitet nomi",
                type: "text",
                defaultNew: "Yangilangan Universitet (NEW)",
                defaultOld: "Yangilangan Universitet (OLD)",
                required: false
                },
                subjectCount: {
                label: "Fanlar soni",
                type: "number",
                defaultNew: "1200",
                defaultOld: "1900",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["universityCode", "universityName", "subjectCount"],
                description: `**Fan yozuvini yangilash**
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}
                <b>Parameters:</b>
                - entityId: Yozuv UUID si (path parameter)
                <b>Body:</b>
                <pre>{
                "universityCode": "311",
                "universityName": "Yangilangan Universitet",
                "subjectCount": 1200
                }</pre>
                <b>Response:</b> Yangilangan fan yozuvi yoki 404`,
                ported: true
                }
            {
                id: 212,
                category: "33.Akademik hisobotlar fanlar",
                name: "Fan yozuvini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                defaultNew: "",
                defaultOld: "",
                required: true
                }
                },
                description: `**Fan yozuvini o'chirish (soft delete)**
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RAcademicSubjects/{entityId}
                <b>Parameters:</b>
                - entityId: Yozuv UUID si (path parameter)
                <b>Response:</b> 200 OK yoki 404 Not Found
                <b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
                ported: true
                }
            {
                id: 213,
                category: "33.Akademik hisobotlar fanlar",
                name: "Fanlarni qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"universityCode","operator":"=","value":"311"}]}',
                defaultNew: '{"conditions":[{"property":"universityCode","operator":"=","value":"311"}]}',
                defaultOld: '{"conditions":[{"property":"universityCode","operator":"=","value":"313"}]}',
                required: true,
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
                returnCount: {
                label: "Umumiy sonni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                defaultNew: "",
                defaultOld: "",
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
                description: `**Fanlarni qidirish** (GET /search)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicSubjects/search
                <b>Parameters:</b>
                - filter: CUBA format filter JSON (URL encoded)
                - limit, offset: Sahifalash
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }
            {
                id: 214,
                category: "33.Akademik hisobotlar fanlar",
                name: "Fanlarni qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicSubjects/search",
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
                description: `**Fanlarni qidirish** (POST /search)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicSubjects/search
                <b>Body:</b> CUBA format filter JSON
                <pre>{"filter":{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}}</pre>
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }

            // ============================================
            // 34.Akademik hisobotlar o'zlashtirish (7 endpoint)
            // ============================================
            {
                id: 215,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "Yangi o'zlashtirish yozuvi yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore",
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
                educationYearName: {
                label: "O'quv yili nomi",
                type: "text",
                defaultNew: "2024-2025",
                defaultOld: "2023-2024",
                required: false
                },
                semesterTypeCode: {
                label: "Semestr kodi",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: false
                },
                semesterTypeName: {
                label: "Semestr nomi",
                type: "text",
                defaultNew: "1-semestr",
                defaultOld: "1-semestr",
                required: false
                },
                courseCode: {
                label: "Kurs kodi",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: false
                },
                courseName: {
                label: "Kurs nomi",
                type: "text",
                defaultNew: "1-kurs",
                defaultOld: "1-kurs",
                required: false
                },
                tableType: {
                label: "Jadval turi",
                type: "text",
                defaultNew: "o'zlashtirish ko'rsatkichlari",
                defaultOld: "o'zlashtirish ko'rsatkichlari",
                required: false
                },
                scorePercent: {
                label: "O'zlashtirish foizi",
                type: "number",
                defaultNew: "85.5",
                defaultOld: "78.3",
                required: false
                },
                scoreType: {
                label: "Baho turi",
                type: "text",
                defaultNew: "yaxshi",
                defaultOld: "qoniqarli",
                required: false
                },
                debitorCount: {
                label: "Qarzdorlar soni",
                type: "number",
                defaultNew: "5",
                defaultOld: "10",
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
                bodyFields: ["universityCode", "universityName", "facultyCode", "facultyName", "educationTypeCode", "educationTypeName", "educationYearCode", "educationYearName", "semesterTypeCode", "semesterTypeName", "courseCode", "courseName", "tableType", "scorePercent", "scoreType", "debitorCount", "updateDate"],
                description: `**Yangi o'zlashtirish yozuvi yaratish**
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicScore
                <b>Response:</b> 201 Created - Yaratilgan yozuv (ID avtomatik keyingi testlarga o'tadi)`,
                ported: true,
                storeResultId: "academicScoreId"
                }
            {
                id: 216,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "O'zlashtirish yozuvini ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "academicScoreId"
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
                description: `**O'zlashtirish yozuvini ID bo'yicha olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicScore/{entityId}
                <b>Workflow:</b> #1 POST dan yaratilgan ID avtomatik qo'yiladi.
                <b>Parameters:</b>
                - entityId: #1 POST dan olingan UUID
                <b>Response:</b> 200 OK - Topilgan yozuv yoki 404 Not Found`,
                ported: true
                }
            {
                id: 217,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "O'zlashtirish yozuvini yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "academicScoreId"
                },
                scorePercent: {
                label: "O'zlashtirish foizi (yangi qiymat)",
                type: "number",
                defaultNew: "95.0",
                defaultOld: "88.5",
                required: false
                },
                debitorCount: {
                label: "Qarzdorlar soni (yangi qiymat)",
                type: "number",
                defaultNew: "3",
                defaultOld: "8",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["scorePercent", "debitorCount"],
                description: `**O'zlashtirish yozuvini yangilash**
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RAcademicScore/{entityId}
                <b>Response:</b> 200 OK - Yangilangan yozuv yoki 404 Not Found`,
                ported: true
                }
            {
                id: 218,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "O'zlashtirish yozuvini o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "academicScoreId"
                }
                },
                description: `**O'zlashtirish yozuvini o'chirish (soft delete)**
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RAcademicScore/{entityId}
                <b>Response:</b> 200 OK yoki 404 Not Found
                <b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
                ported: true
                }
            {
                id: 219,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "Barcha o'zlashtirish yozuvlarini olish (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore",
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
                returnCount: {
                label: "Umumiy sonni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                defaultNew: "",
                defaultOld: "",
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
                description: `**Barcha o'zlashtirish yozuvlarini olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicScore
                <b>Parameters:</b>
                - limit: Sahifa hajmi (default: 50)
                - offset: Boshlanish indeksi (default: 0)
                - returnCount: Umumiy sonni X-Total-Count headerda qaytarish
                - returnNulls: Null qiymatlarni ham qaytarish
                <b>Response:</b> Sahifalangan o'zlashtirish hisobotlari ro'yxati`,
                ported: true
                }
            {
                id: 220,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "O'zlashtirish yozuvlarini qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore/search",
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
                description: `**O'zlashtirish yozuvlarini qidirish** (GET /search)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RAcademicScore/search
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }
            {
                id: 221,
                category: "34.Akademik hisobotlar o'zlashtirish",
                name: "O'zlashtirish yozuvlarini qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RAcademicScore/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}',
                defaultNew: '{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}',
                defaultOld: '{"conditions":[{"property":"universityCode","operator":"notEmpty"}]}',
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
                description: `**O'zlashtirish yozuvlarini qidirish** (POST /search)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RAcademicScore/search
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }

            // ============================================
            // 35.Akademik hisobotlar davomat (7 endpoint)
            // ============================================
            {
                id: 222,
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
                }
            {
                id: 223,
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
                }
            {
                id: 224,
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
                }
            {
                id: 225,
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
                }
            {
                id: 226,
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
                }
            {
                id: 227,
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
                }
            {
                id: 228,
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

            // ============================================
            // 36.Shartnoma statistikasi (2 endpoint)
            // ============================================
            {
                id: 229,
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
                }
            {
                id: 230,
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

            // ============================================
            // 37.Bandlik statistikasi (7 endpoint)
            // ============================================
            {
                id: 231,
                category: "37.Bandlik statistikasi",
                name: "Yangi bandlik yozuvi yaratish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_REmployment",
                requiresAuth: true,
                inputFields: {
                uId: {
                label: "Universitet ID (raqam)",
                type: "text",
                defaultNew: "401",
                defaultOld: "351",
                required: false
                },
                qty: {
                label: "Miqdor (talabalar soni)",
                type: "number",
                defaultNew: "5",
                defaultOld: "5",
                required: false
                },
                university: {
                label: "Universitet kodi",
                type: "text",
                defaultNew: "401",
                defaultOld: "351",
                required: false
                },
                department: {
                label: "Bo'lim kodi",
                type: "text",
                defaultNew: "401-102-08",
                defaultOld: "351-118",
                placeholder: "Yangi: 401-102-08, Eski: 351-118",
                required: false
                },
                educationYear: {
                label: "Ta'lim yili kodi",
                type: "text",
                defaultNew: "2026",
                defaultOld: "2021",
                placeholder: "2026, 2027, 2028...",
                required: false
                },
                educationType: {
                label: "Ta'lim turi kodi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                placeholder: "11=Bakalavr, 12=Magistr...",
                required: false
                },
                educationForm: {
                label: "Ta'lim shakli kodi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                placeholder: "11=Kunduzgi, 22=Sirtqi...",
                required: false
                },
                paymentForm: {
                label: "To'lov shakli kodi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: false
                },
                gender: {
                label: "Jins kodi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: false
                },
                workplaceCompatibility: {
                label: "Ish joyi mosligi kodi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: false
                },
                graduateInactiveType: {
                label: "Bitiruvchi nofaol turi kodi",
                type: "text",
                defaultNew: "13",
                defaultOld: "13",
                required: false
                },
                graduateFieldsType: {
                label: "Bitiruvchi soha turi kodi",
                type: "text",
                defaultNew: "31",
                defaultOld: "31",
                required: false
                }
                },
                hasBody: true,
                storeResultId: "createdREmploymentId",
                bodyGenerator: (inputs) => {
                // UNIFIED FORMAT: Ikkala tizim uchun underscore bilan (_university)
                // NEW-HEMIS ham OLD-HEMIS formatini qabul qiladi
                let item = {};
                if (inputs.uId) item.uId = inputs.uId;  // String sifatida
                if (inputs.qty) item.qty = parseInt(inputs.qty);
                // FK maydonlar - underscore bilan (OLD-HEMIS format)
                if (inputs.university) item._university = {code: inputs.university};
                if (inputs.department) item._department = {code: inputs.department};
                if (inputs.educationYear) item._educationYear = {code: inputs.educationYear};
                if (inputs.educationType) item._educationType = {code: inputs.educationType};
                if (inputs.educationForm) item._educationForm = {code: inputs.educationForm};
                if (inputs.paymentForm) item._paymentForm = {code: inputs.paymentForm};
                if (inputs.gender) item._gender = {code: inputs.gender};
                if (inputs.workplaceCompatibility) item._workplaceCompatibility = {code: inputs.workplaceCompatibility};
                if (inputs.graduateInactiveType) item._graduateInactiveType = {code: inputs.graduateInactiveType};
                if (inputs.graduateFieldsType) item._graduateFieldsType = {code: inputs.graduateFieldsType};
                // Massiv formatida yuboradi
                return [item];
                },
                description: `**Yangi bandlik statistikasi yozuvi yaratish (UPSERT)** (POST /)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_REmployment
                <b>UPSERT:</b> Agar yozuv mavjud bo'lsa - yangilanadi, yo'q bo'lsa - yaratiladi
                <b>Unique Key:</b> department + educationYear + educationType + educationForm + paymentForm + gender + workplaceCompatibility + graduateFieldsType + graduateInactiveType
                <b>Body:</b> UNIFIED FORMAT - MASSIV sifatida, underscore bilan yuboriladi
                <pre>[
                {
                "uId": "401",
                "qty": 5,
                "_university": {"code": "401"},
                "_department": {"code": "401-102-08"},
                "_educationYear": {"code": "2026"},
                "_educationType": {"code": "11"},
                "_educationForm": {"code": "11"},
                "_paymentForm": {"code": "11"},
                "_gender": {"code": "11"},
                "_workplaceCompatibility": {"code": "11"},
                "_graduateInactiveType": {"code": "13"},
                "_graduateFieldsType": {"code": "31"}
                }
                ]</pre>
                <b>Response:</b> Yaratilgan/Yangilangan bandlik yozuvi`,
                ported: true
                }
            {
                id: 232,
                category: "37.Bandlik statistikasi",
                name: "Bandlik yozuvini ID bo'yicha olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_REmployment/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                useStoredId: "createdREmploymentId",
                required: true
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
                description: `**Bandlik yozuvini ID bo'yicha olish** (GET /{entityId})
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_REmployment/{entityId}
                <b>Path param:</b> entityId - UUID formatida yozuv identifikatori
                <b>Response:</b> Topilgan bandlik yozuvi yoki 404`,
                ported: true
                }
            {
                id: 233,
                category: "37.Bandlik statistikasi",
                name: "Bandlik yozuvini yangilash",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_REmployment/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                useStoredId: "createdREmploymentId",
                required: true
                },
                qty: {
                label: "Miqdor (talabalar soni)",
                type: "number",
                defaultNew: "150",
                defaultOld: "150",
                required: false
                }
                },
                hasBody: true,
                bodyGenerator: (inputs) => {
                let body = {};
                if (inputs.qty) body.qty = parseInt(inputs.qty);
                return body;
                },
                description: `**Bandlik yozuvini yangilash** (PUT /{entityId})
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_REmployment/{entityId}
                <b>Body:</b> JSON formatida yangilanadigan maydonlar
                <pre>{
                "qty": 150
                }</pre>
                <b>Response:</b> Yangilangan bandlik yozuvi yoki 404`,
                ported: true
                }
            {
                id: 234,
                category: "37.Bandlik statistikasi",
                name: "Barcha bandlik yozuvlarini olish",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_REmployment",
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
                returnCount: {
                label: "Umumiy sonni qaytarish",
                type: "select",
                options: [{value: "", label: "default"}, {value: "true", label: "true"}, {value: "false", label: "false"}],
                defaultNew: "",
                defaultOld: "",
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
                description: `**Bandlik statistikasi hisobotlari** (GET /)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_REmployment
                <b>Tavsif:</b> Bo'lim, ta'lim yili, ta'lim turi bo'yicha bandlik statistikasi
                <b>Query params:</b>
                - limit, offset: Sahifalash
                - returnCount: X-Total-Count headerda umumiy sonni qaytarish
                - returnNulls: Null qiymatlarni ham qaytarish
                <b>Response:</b> Sahifalangan bandlik yozuvlari ro'yxati`,
                ported: true
                }
            {
                id: 235,
                category: "37.Bandlik statistikasi",
                name: "Bandlik yozuvini o'chirish",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_REmployment/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                useStoredId: "createdREmploymentId",
                required: true
                }
                },
                description: `**Bandlik yozuvini o'chirish** (DELETE /{entityId})
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_REmployment/{entityId}
                <b>Path param:</b> entityId - UUID formatida yozuv identifikatori
                <b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
                ported: true
                }
            {
                id: 236,
                category: "37.Bandlik statistikasi",
                name: "Bandlik yozuvlarini qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_REmployment/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON yoki text)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                defaultNew: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                defaultOld: '{"conditions":[{"property":"qty","operator":"notEmpty"}]}',
                required: true,
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
                description: `**Bandlik yozuvlarini qidirish** (GET /search)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_REmployment/search?filter=...&limit=2
                <b>Query params:</b>
                - filter: CUBA JSON format (URL encoded)
                - limit, offset: Sahifalash
                <b>CUBA Filter misoli:</b>
                <pre>{"conditions":[{"property":"qty","operator":"notEmpty"}]}</pre>
                <b>Response:</b> Filter shartiga mos yozuvlar (limit ta)`,
                ported: true
                }
            {
                id: 237,
                category: "37.Bandlik statistikasi",
                name: "Bandlik yozuvlarini qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_REmployment/search",
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
                hasBody: true,
                bodyFields: ["filter", "limit", "offset"],
                queryParamsFromInputs: ["returnNulls"],
                description: `**Bandlik yozuvlarini qidirish** (POST /search)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_REmployment/search
                <b>Body (OLD-HEMIS compatible):</b>
                <pre>{"filter":{"conditions":[{"property":"qty","operator":"notEmpty"}]},"limit":2,"offset":0}</pre>
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }

            // ============================================
            // 38.Inspeksiya administrative teacher (7 endpoint)
            // ============================================
            {
                id: 238,
                category: "38.Inspeksiya administrative teacher",
                name: "Yangi administrative employee1 yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1",
                requiresAuth: true,
                inputFields: {
                // Foreign Key maydonlari (CUBA format)
                _university: {
                label: "Universitet ID",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "401",
                defaultOld: "351",
                required: false,
                cubaForeignKey: true
                },
                _educationYear: {
                label: "O'quv yili ID",
                type: "text",
                placeholder: "Masalan: 2024",
                defaultNew: "2024",
                defaultOld: "2024",
                required: false,
                cubaForeignKey: true
                },
                _employee: {
                label: "Xodim ID (UUID)",
                type: "text",
                placeholder: "Xodim UUID (Employee POST dan yoki bazadan)",
                defaultNew: "8a576cff-1de3-7b47-c3fb-87dd2796bea7",
                defaultOld: "8882879b-c9f2-defd-570c-02aed77faf71",
                required: false,
                cubaForeignKey: true,
                useStoredId: "employeeId"  // Employee POST endpoint bo'lsa shu ID ishlatiladi
                },
                _country: {
                label: "Davlat kodi",
                type: "text",
                placeholder: "Masalan: UZ, US",
                defaultNew: "US",
                defaultOld: "GB",
                required: false,
                cubaForeignKey: true
                },
                _degree: {
                label: "Ilmiy daraja ID",
                type: "text",
                placeholder: "Daraja kodi",
                defaultNew: "11",
                defaultOld: "12",
                required: false,
                cubaForeignKey: true
                },
                _rank: {
                label: "Ilmiy unvon ID",
                type: "text",
                placeholder: "Unvon kodi",
                defaultNew: "11",
                defaultOld: "12",
                required: false,
                cubaForeignKey: true
                },
                // Oddiy maydonlar
                foreignUniversity: {
                label: "Chet el universiteti nomi",
                type: "text",
                defaultNew: "Harvard University",
                defaultOld: "Massachusetts Institute of Technology",
                required: false
                },
                diplomaType: {
                label: "Diplom turi",
                type: "text",
                defaultNew: "PhD",
                defaultOld: "DSc",
                required: false
                },
                diplomaSerialNumber: {
                label: "Diplom seriya raqami",
                type: "text",
                defaultNew: "PHD-2024-001",
                defaultOld: "DSC-2023-002",
                required: false
                },
                diplomaDate: {
                label: "Diplom sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2024-06-15",
                defaultOld: "2023-05-20",
                required: false
                },
                specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                defaultNew: "01.01.01",
                defaultOld: "02.02.02",
                required: false
                },
                specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                defaultNew: "Amaliy matematika va informatika",
                defaultOld: "Nazariy fizika",
                required: false
                },
                councilDate: {
                label: "Kengash sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2024-05-20",
                defaultOld: "2023-04-15",
                required: false
                },
                councilNumber: {
                label: "Kengash raqami",
                type: "text",
                defaultNew: "DSc/PhD.03/30.12.2019.FM.02.01",
                defaultOld: "DSc/PhD.03/30.12.2019.Fiz.01.02",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["_university", "_educationYear", "_employee", "_country", "_degree", "_rank", "foreignUniversity", "diplomaType", "diplomaSerialNumber", "diplomaDate", "specialityCode", "specialityName", "councilDate", "councilNumber"],
                description: `**Yangi administrative employee1 yozuvi yaratish**
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1
                <b>Ma'lumot:</b> Top-1000 universitetlaridan PhD/DSc darajali o'qituvchi
                <b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
                - _university: Universitet ID
                - _educationYear: O'quv yili ID
                - _employee: Xodim UUID
                - _country: Davlat kodi (UZ, US, GB...)
                - _degree: Ilmiy daraja ID
                - _rank: Ilmiy unvon ID
                <b>Oddiy maydonlar:</b>
                - foreignUniversity: Chet el universiteti nomi
                - diplomaType: PhD yoki DSc
                - diplomaSerialNumber: Diplom seriya raqami
                - diplomaDate: Diplom berilgan sana (YYYY-MM-DD)
                - specialityCode: Mutaxassislik kodi
                - specialityName: Mutaxassislik nomi
                - councilDate: Kengash sanasi
                - councilNumber: Kengash raqami
                <b>Response:</b> 201 Created - Yaratilgan yozuv`,
                ported: true,
                storeResultId: "administrativeEmployee1Id"
                }
            {
                id: 239,
                category: "38.Inspeksiya administrative teacher",
                name: "Administrative employee1 ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee1Id"
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
                description: `**Administrative employee1 yozuvini ID bo'yicha olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}
                <b>Workflow:</b> #1 POST dan yaratilgan ID avtomatik qo'yiladi.
                <b>Response:</b> 200 OK - Topilgan yozuv yoki 404 Not Found`,
                ported: true
                }
            {
                id: 240,
                category: "38.Inspeksiya administrative teacher",
                name: "Administrative employee1 yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee1Id"
                },
                // Foreign Key maydonlari
                _university: {
                label: "Universitet ID (yangi)",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                _country: {
                label: "Davlat kodi (yangi)",
                type: "text",
                placeholder: "UZ, US, GB...",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                _degree: {
                label: "Ilmiy daraja ID (yangi)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                _rank: {
                label: "Ilmiy unvon ID (yangi)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                // Oddiy maydonlar
                foreignUniversity: {
                label: "Chet el universiteti (yangi qiymat)",
                type: "text",
                defaultNew: "Stanford University",
                defaultOld: "University of Cambridge",
                required: false
                },
                diplomaType: {
                label: "Diplom turi (yangi qiymat)",
                type: "text",
                defaultNew: "DSc",
                defaultOld: "PhD",
                required: false
                },
                specialityName: {
                label: "Mutaxassislik nomi (yangi qiymat)",
                type: "text",
                defaultNew: "Kompyuter fanlari",
                defaultOld: "Kvant fizikasi",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["_university", "_country", "_degree", "_rank", "foreignUniversity", "diplomaType", "specialityName"],
                description: `**Administrative employee1 yozuvini yangilash**
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}
                <b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
                - _university: Universitet ID
                - _country: Davlat kodi
                - _degree: Ilmiy daraja ID
                - _rank: Ilmiy unvon ID
                <b>Oddiy maydonlar:</b>
                - foreignUniversity: Chet el universiteti nomi
                - diplomaType: PhD yoki DSc
                - specialityName: Mutaxassislik nomi
                <b>Response:</b> 200 OK - Yangilangan yozuv yoki 404 Not Found`,
                ported: true
                }
            {
                id: 241,
                category: "38.Inspeksiya administrative teacher",
                name: "Administrative employee1 o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee1Id"
                }
                },
                description: `**Administrative employee1 yozuvini o'chirish (soft delete)**
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{entityId}
                <b>Response:</b> 200 OK yoki 404 Not Found
                <b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
                ported: true
                }
            {
                id: 242,
                category: "38.Inspeksiya administrative teacher",
                name: "Barcha administrative employee1 olish (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
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
                description: `**Barcha administrative employee1 yozuvlarini olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1
                <b>Parameters:</b>
                - limit: Sahifa hajmi (default: 50)
                - offset: Boshlanish indeksi (default: 0)
                <b>Response:</b> Sahifalangan ro'yxat`,
                ported: true
                }
            {
                id: 243,
                category: "38.Inspeksiya administrative teacher",
                name: "Administrative employee1 qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"diplomaType","operator":"=","value":"rank"}]}',
                defaultNew: '{"conditions":[{"property":"diplomaType","operator":"=","value":"rank"}]}',
                defaultOld: '{"conditions":[{"property":"diplomaType","operator":"=","value":"rank"}]}',
                required: false,
                rows: 3
                },
                limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
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
                description: `**Administrative employee1 yozuvlarini qidirish** (GET /search)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/search
                <b>Filter misollari:</b>
                - {"conditions":[{"property":"diplomaType","operator":"=","value":"rank"}]}
                - {"conditions":[{"property":"foreignUniversity","operator":"contains","value":"davlat"}]}
                - {"conditions":[{"property":"specialityName","operator":"notEmpty"}]}
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }
            {
                id: 244,
                category: "38.Inspeksiya administrative teacher",
                name: "Administrative employee1 qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"foreignUniversity","operator":"notEmpty"}]}',
                defaultNew: '{"conditions":[{"property":"foreignUniversity","operator":"contains","value":"davlat"}]}',
                defaultOld: '{"conditions":[{"property":"foreignUniversity","operator":"contains","value":"davlat"}]}',
                required: false,
                rows: 3
                },
                limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
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
                description: `**Administrative employee1 yozuvlarini qidirish** (POST /search)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/search
                <b>Filter misollari:</b>
                - {"conditions":[{"property":"foreignUniversity","operator":"contains","value":"davlat"}]}
                - {"conditions":[{"property":"diplomaType","operator":"=","value":"rank"}]}
                - {"conditions":[{"property":"specialityCode","operator":"startsWith","value":"01"}]}
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }

            // ============================================
            // 39.Xorijiy OTMda malaka oshirish (7 endpoint)
            // ============================================
            {
                id: 245,
                category: "39.Xorijiy OTMda malaka oshirish",
                name: "Yangi administrative employee2 yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2",
                requiresAuth: true,
                inputFields: {
                // Foreign Key maydonlari (CUBA format)
                _university: {
                label: "Universitet ID",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                cubaForeignKey: true
                },
                _educationYear: {
                label: "O'quv yili ID",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                cubaForeignKey: true
                },
                _employee: {
                label: "Xodim ID (UUID)",
                type: "text",
                placeholder: "Xodim UUID",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true,
                useStoredId: "employeeId"
                },
                _country: {
                label: "Davlat ID",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                _internshipForm: {
                label: "Stajirovka shakli ID",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "11",
                defaultOld: "11",
                required: false,
                cubaForeignKey: true
                },
                _internshipType: {
                label: "Stajirovka turi ID",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "11",
                defaultOld: "11",
                required: false,
                cubaForeignKey: true
                },
                // Oddiy maydonlar
                foreignUniversity: {
                label: "Chet el universiteti nomi",
                type: "text",
                defaultNew: "Massachusetts Institute of Technology",
                defaultOld: "Stanford University",
                required: false
                },
                specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                defaultNew: "01.01.01",
                defaultOld: "02.02.02",
                required: false
                },
                specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                defaultNew: "Texnologik mashinalar. Robotlar, mexatronika va robototexnika tizimlari",
                defaultOld: "Amaliy matematika va informatika",
                required: false
                },
                trainingTypeName: {
                label: "Ta'lim turi nomi",
                type: "text",
                defaultNew: "Programming",
                defaultOld: "Data Science",
                required: false
                },
                trainingContract: {
                label: "Shartnoma raqami",
                type: "text",
                defaultNew: "123",
                defaultOld: "456",
                required: false
                },
                trainingDateStart: {
                label: "Boshlash sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2024-09-01",
                defaultOld: "2023-09-01",
                required: false
                },
                trainingDateEnd: {
                label: "Tugash sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2024-12-30",
                defaultOld: "2023-12-30",
                required: false
                },
                year: {
                label: "Yil",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2023",
                required: false
                },
                subject: {
                label: "Fanlar",
                type: "text",
                defaultNew: "Subject1, Subject2",
                defaultOld: "Subject3, Subject4",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["_university", "_educationYear", "_employee", "_country", "_internshipForm", "_internshipType", "foreignUniversity", "specialityCode", "specialityName", "trainingTypeName", "trainingContract", "trainingDateStart", "trainingDateEnd", "year", "subject"],
                description: `**Yangi administrative employee2 yozuvi yaratish**
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2
                <b>Ma'lumot:</b> Xorijiy OTMlarda malaka oshirgan va stajirovka o'tgan o'qituvchilar
                <b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
                - _university: Universitet ID (required)
                - _educationYear: O'quv yili ID (required)
                - _employee: Xodim UUID
                - _country: Davlat ID
                - _internshipForm: Stajirovka shakli ID
                - _internshipType: Stajirovka turi ID
                <b>Oddiy maydonlar:</b>
                - foreignUniversity: Chet el universiteti nomi
                - specialityCode, specialityName: Mutaxassislik kodi va nomi
                - trainingTypeName: Ta'lim turi nomi
                - trainingContract: Shartnoma raqami
                - trainingDateStart, trainingDateEnd: Boshlash va tugash sanalari
                - year: Yil
                - subject: Fanlar ro'yxati
                <b>Response:</b> 201 Created - Yaratilgan yozuv`,
                ported: true,
                storeResultId: "administrativeEmployee2Id"
                }
            {
                id: 246,
                category: "39.Xorijiy OTMda malaka oshirish",
                name: "Administrative employee2 ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee2Id"
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
                description: `**Administrative employee2 yozuvini ID bo'yicha olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}
                <b>Workflow:</b> #1 POST dan yaratilgan ID avtomatik qo'yiladi.
                <b>Response:</b> 200 OK - Topilgan yozuv yoki 404 Not Found`,
                ported: true
                }
            {
                id: 247,
                category: "39.Xorijiy OTMda malaka oshirish",
                name: "Administrative employee2 yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee2Id"
                },
                // Foreign Key maydonlari
                _country: {
                label: "Davlat ID (yangi)",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                _internshipForm: {
                label: "Stajirovka shakli ID (yangi)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                _internshipType: {
                label: "Stajirovka turi ID (yangi)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                // Oddiy maydonlar
                foreignUniversity: {
                label: "Chet el universiteti (yangi qiymat)",
                type: "text",
                defaultNew: "Harvard University",
                defaultOld: "University of Cambridge",
                required: false
                },
                trainingTypeName: {
                label: "Ta'lim turi nomi (yangi qiymat)",
                type: "text",
                defaultNew: "Artificial Intelligence",
                defaultOld: "Machine Learning",
                required: false
                },
                specialityName: {
                label: "Mutaxassislik nomi (yangi qiymat)",
                type: "text",
                defaultNew: "Kompyuter fanlari",
                defaultOld: "Ma'lumotlar fani",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["_country", "_internshipForm", "_internshipType", "foreignUniversity", "trainingTypeName", "specialityName"],
                description: `**Administrative employee2 yozuvini yangilash**
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}
                <b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
                - _country: Davlat ID
                - _internshipForm: Stajirovka shakli ID
                - _internshipType: Stajirovka turi ID
                <b>Oddiy maydonlar:</b>
                - foreignUniversity: Chet el universiteti nomi
                - trainingTypeName: Ta'lim turi nomi
                - specialityName: Mutaxassislik nomi
                <b>Response:</b> 200 OK - Yangilangan yozuv yoki 404 Not Found`,
                ported: true
                }
            {
                id: 248,
                category: "39.Xorijiy OTMda malaka oshirish",
                name: "Administrative employee2 o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee2Id"
                }
                },
                description: `**Administrative employee2 yozuvini o'chirish (soft delete)**
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{entityId}
                <b>Response:</b> 200 OK yoki 404 Not Found
                <b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
                ported: true
                }
            {
                id: 249,
                category: "39.Xorijiy OTMda malaka oshirish",
                name: "Barcha administrative employee2 olish (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
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
                description: `**Barcha administrative employee2 yozuvlarini olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2
                <b>Parameters:</b>
                - limit: Sahifa hajmi (default: 50)
                - offset: Boshlanish indeksi (default: 0)
                <b>Response:</b> Sahifalangan ro'yxat`,
                ported: true
                }
            {
                id: 250,
                category: "39.Xorijiy OTMda malaka oshirish",
                name: "Administrative employee2 qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"foreignUniversity","operator":"contains","value":"MIT"}]}',
                defaultNew: '{"conditions":[{"property":"foreignUniversity","operator":"notEmpty"}]}',
                defaultOld: '{"conditions":[{"property":"foreignUniversity","operator":"notEmpty"}]}',
                required: false,
                rows: 3
                },
                limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
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
                description: `**Administrative employee2 yozuvlarini qidirish** (GET /search)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/search
                <b>Filter misollari:</b>
                - {"conditions":[{"property":"foreignUniversity","operator":"contains","value":"MIT"}]}
                - {"conditions":[{"property":"trainingTypeName","operator":"notEmpty"}]}
                - {"conditions":[{"property":"year","operator":"=","value":"2024"}]}
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }
            {
                id: 251,
                category: "39.Xorijiy OTMda malaka oshirish",
                name: "Administrative employee2 qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"foreignUniversity","operator":"notEmpty"}]}',
                defaultNew: '{"conditions":[{"property":"year","operator":"=","value":"2024"}]}',
                defaultOld: '{"conditions":[{"property":"year","operator":"=","value":"2023"}]}',
                required: false,
                rows: 3
                },
                limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
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
                description: `**Administrative employee2 yozuvlarini qidirish** (POST /search)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/search
                <b>Filter misollari:</b>
                - {"conditions":[{"property":"year","operator":"=","value":"2024"}]}
                - {"conditions":[{"property":"foreignUniversity","operator":"contains","value":"Harvard"}]}
                - {"conditions":[{"property":"trainingTypeName","operator":"startsWith","value":"Programming"}]}
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }

            // ============================================
            // 40.OTMda xorijiy o'qituvchilar (7 endpoint)
            // ============================================
            {
                id: 252,
                category: "40.OTMda xorijiy o'qituvchilar",
                name: "Yangi administrative employee3 yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3",
                requiresAuth: true,
                inputFields: {
                // Foreign Key maydonlari (CUBA format)
                _university: {
                label: "Universitet ID",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                cubaForeignKey: true
                },
                _educationYear: {
                label: "O'quv yili ID",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: true,
                cubaForeignKey: true
                },
                _country: {
                label: "Davlat ID",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                _employee: {
                label: "Xodim ID (UUID)",
                type: "text",
                placeholder: "Xodim UUID",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true,
                useStoredId: "employeeId"
                },
                _employeeForm: {
                label: "Xodim shakli ID",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "11",
                defaultOld: "11",
                required: false,
                cubaForeignKey: true
                },
                _condutionForm: {
                label: "O'tkazish shakli ID",
                type: "text",
                placeholder: "UUID yoki kod",
                defaultNew: "11",
                defaultOld: "11",
                required: false,
                cubaForeignKey: true
                },
                // Oddiy maydonlar
                fullname: {
                label: "To'liq ism",
                type: "text",
                defaultNew: "John Smith",
                defaultOld: "David Johnson",
                required: false
                },
                workPlace: {
                label: "Ish joyi",
                type: "text",
                defaultNew: "Harvard University",
                defaultOld: "MIT",
                required: false
                },
                specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                defaultNew: "Computer Science",
                defaultOld: "Data Science",
                required: false
                },
                subject: {
                label: "Fan nomi",
                type: "text",
                defaultNew: "Programming",
                defaultOld: "Machine Learning",
                required: false
                },
                contractData: {
                label: "Shartnoma ma'lumotlari",
                type: "text",
                defaultNew: "Contract-2024-001",
                defaultOld: "Contract-2023-002",
                required: false
                },
                arrivalDate: {
                label: "Kelish sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2024-01-15",
                defaultOld: "2023-01-20",
                required: false
                },
                departureDate: {
                label: "Ketish sanasi",
                type: "text",
                placeholder: "YYYY-MM-DD",
                defaultNew: "2025-01-15",
                defaultOld: "2024-01-20",
                required: false
                },
                lessonTime: {
                label: "Dars soatlari",
                type: "number",
                defaultNew: "200",
                defaultOld: "150",
                required: false
                },
                year: {
                label: "Yil",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2023",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["_university", "_educationYear", "_country", "_employee", "_employeeForm", "_condutionForm", "fullname", "workPlace", "specialityName", "subject", "contractData", "arrivalDate", "departureDate", "lessonTime", "year"],
                description: `**Yangi administrative employee3 yozuvi yaratish**
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3
                <b>Ma'lumot:</b> OTMda faoliyat olib borayotgan xorijiy o'qituvchilar
                <b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
                - _university: Universitet ID (required)
                - _educationYear: O'quv yili ID (required)
                - _country: Davlat ID
                - _employee: Xodim UUID
                - _employeeForm: Xodim shakli ID
                - _condutionForm: O'tkazish shakli ID
                <b>Oddiy maydonlar:</b>
                - fullname: Xorijiy o'qituvchining to'liq ismi
                - workPlace: Ish joyi (asosiy universitet)
                - specialityName: Mutaxassislik nomi
                - subject: O'tiladigan fan nomi
                - contractData: Shartnoma ma'lumotlari
                - arrivalDate, departureDate: Kelish va ketish sanalari
                - lessonTime: Dars soatlari soni
                - year: Yil
                <b>Response:</b> 201 Created - Yaratilgan yozuv`,
                ported: true,
                storeResultId: "administrativeEmployee3Id"
                }
            {
                id: 253,
                category: "40.OTMda xorijiy o'qituvchilar",
                name: "Administrative employee3 ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee3Id"
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
                description: `**Administrative employee3 yozuvini ID bo'yicha olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}
                <b>Workflow:</b> #1 POST dan yaratilgan ID avtomatik qo'yiladi.
                <b>Response:</b> 200 OK - Topilgan yozuv yoki 404 Not Found`,
                ported: true
                }
            {
                id: 254,
                category: "40.OTMda xorijiy o'qituvchilar",
                name: "Administrative employee3 yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee3Id"
                },
                // Foreign Key maydonlari
                _country: {
                label: "Davlat ID (yangi)",
                type: "text",
                placeholder: "UUID",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                _employeeForm: {
                label: "Xodim shakli ID (yangi)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                _condutionForm: {
                label: "O'tkazish shakli ID (yangi)",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false,
                cubaForeignKey: true
                },
                // Oddiy maydonlar
                fullname: {
                label: "To'liq ism (yangi qiymat)",
                type: "text",
                defaultNew: "Robert Brown",
                defaultOld: "Michael Davis",
                required: false
                },
                workPlace: {
                label: "Ish joyi (yangi qiymat)",
                type: "text",
                defaultNew: "Stanford University",
                defaultOld: "University of Cambridge",
                required: false
                },
                specialityName: {
                label: "Mutaxassislik nomi (yangi qiymat)",
                type: "text",
                defaultNew: "Artificial Intelligence",
                defaultOld: "Deep Learning",
                required: false
                },
                lessonTime: {
                label: "Dars soatlari (yangi qiymat)",
                type: "number",
                defaultNew: "250",
                defaultOld: "180",
                required: false
                }
                },
                hasBody: true,
                bodyFields: ["_country", "_employeeForm", "_condutionForm", "fullname", "workPlace", "specialityName", "lessonTime"],
                description: `**Administrative employee3 yozuvini yangilash**
                <b>Endpoint:</b> PUT /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}
                <b>Foreign Key maydonlari (CUBA format {"id": "value"}):</b>
                - _country: Davlat ID
                - _employeeForm: Xodim shakli ID
                - _condutionForm: O'tkazish shakli ID
                <b>Oddiy maydonlar:</b>
                - fullname: To'liq ism
                - workPlace: Ish joyi
                - specialityName: Mutaxassislik nomi
                - lessonTime: Dars soatlari
                <b>Response:</b> 200 OK - Yangilangan yozuv yoki 404 Not Found`,
                ported: true
                }
            {
                id: 255,
                category: "40.OTMda xorijiy o'qituvchilar",
                name: "Administrative employee3 o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}",
                requiresAuth: true,
                inputFields: {
                entityId: {
                label: "Entity ID (UUID)",
                type: "text",
                placeholder: "POST dan avtomatik to'ldiriladi",
                defaultNew: "",
                defaultOld: "",
                required: true,
                useStoredId: "administrativeEmployee3Id"
                }
                },
                description: `**Administrative employee3 yozuvini o'chirish (soft delete)**
                <b>Endpoint:</b> DELETE /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{entityId}
                <b>Response:</b> 200 OK yoki 404 Not Found
                <b>Note:</b> Soft delete - yozuv o'chirilmaydi, faqat delete_ts qo'yiladi`,
                ported: true
                }
            {
                id: 256,
                category: "40.OTMda xorijiy o'qituvchilar",
                name: "Barcha administrative employee3 olish (GET all)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3",
                requiresAuth: true,
                inputFields: {
                limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
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
                description: `**Barcha administrative employee3 yozuvlarini olish**
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3
                <b>Parameters:</b>
                - limit: Sahifa hajmi (default: 50)
                - offset: Boshlanish indeksi (default: 0)
                <b>Response:</b> Sahifalangan ro'yxat`,
                ported: true
                }
            {
                id: 257,
                category: "40.OTMda xorijiy o'qituvchilar",
                name: "Administrative employee3 qidirish (GET /search)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"fullname","operator":"contains","value":"John"}]}',
                defaultNew: '{"conditions":[{"property":"fullname","operator":"notEmpty"}]}',
                defaultOld: '{"conditions":[{"property":"fullname","operator":"notEmpty"}]}',
                required: false,
                rows: 3
                },
                limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
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
                description: `**Administrative employee3 yozuvlarini qidirish** (GET /search)
                <b>Endpoint:</b> GET /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search
                <b>Filter misollari:</b>
                - {"conditions":[{"property":"fullname","operator":"contains","value":"John"}]}
                - {"conditions":[{"property":"workPlace","operator":"notEmpty"}]}
                - {"conditions":[{"property":"year","operator":"=","value":"2024"}]}
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }
            {
                id: 258,
                category: "40.OTMda xorijiy o'qituvchilar",
                name: "Administrative employee3 qidirish (POST /search)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search",
                requiresAuth: true,
                inputFields: {
                filter: {
                label: "Filter (CUBA JSON)",
                type: "textarea",
                placeholder: '{"conditions":[{"property":"fullname","operator":"notEmpty"}]}',
                defaultNew: '{"conditions":[{"property":"year","operator":"=","value":"2024"}]}',
                defaultOld: '{"conditions":[{"property":"year","operator":"=","value":"2023"}]}',
                required: false,
                rows: 3
                },
                limit: {
                label: "Limit",
                type: "number",
                defaultNew: "10",
                defaultOld: "10",
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
                description: `**Administrative employee3 yozuvlarini qidirish** (POST /search)
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search
                <b>Filter misollari:</b>
                - {"conditions":[{"property":"year","operator":"=","value":"2024"}]}
                - {"conditions":[{"property":"fullname","operator":"contains","value":"Smith"}]}
                - {"conditions":[{"property":"lessonTime","operator":">=","value":"100"}]}
                <b>Response:</b> Filter shartiga mos yozuvlar`,
                ported: true
                }

            // ============================================
            // 41.Inspeksiya administrative student2 (Akademik almashinuv) - 7 endpoint
            // ============================================
            {
                id: 259,
                category: "41.Inspeksiya administrative student2 (Akademik almashinuv)",
                name: "Yangi yozuv yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2",
                requiresAuth: true,
                dependsOn: 1,
                description: "Xorij OTMlari bilan akademik almashinuv - yangi talaba yozuvi yaratish (OLD-HEMIS CUBA format)",
                storeResultId: "administrativeStudent2EntityId",
                inputFields: {
                university: {
                label: "Universitet kodi",
                type: "text",
                required: false,
                placeholder: "301",
                defaultNew: "301",
                defaultOld: "301",
                helpText: "OTM kodi (masalan: 301). Request body: {\"code\": \"301\"}"
                },
                educationYear: {
                label: "O'quv yili",
                type: "text",
                required: false,
                placeholder: "2024",
                defaultNew: "2024",
                defaultOld: "2024",
                helpText: "O'quv yili kodi (masalan: 2024). Request body: {\"code\": \"2024\"}"
                },
                country: {
                label: "Davlat kodi",
                type: "text",
                required: false,
                placeholder: "US",
                defaultNew: "US",
                defaultOld: "US",
                helpText: "ISO country code (masalan: US, GB, DE). Request body: {\"code\": \"US\"}"
                },
                educationType: {
                label: "Ta'lim turi kodi",
                type: "text",
                required: false,
                placeholder: "11",
                defaultNew: "11",
                defaultOld: "11",
                helpText: "Ta'lim turi kodi. Request body: {\"code\": \"11\"}"
                },
                exchangeDocument: {
                label: "Shartnoma/Hujjat",
                type: "text",
                required: false,
                placeholder: "Shartnoma raqami",
                defaultNew: "SH-2024-NEW",
                defaultOld: "SH-2024-OLD"
                },
                exchangeType: {
                label: "Almashinuv turi",
                type: "text",
                required: false,
                placeholder: "outcome / income",
                defaultNew: "outcome",
                defaultOld: "outcome"
                },
                studentFullname: {
                label: "Talaba FIO",
                type: "text",
                required: true,
                placeholder: "Familiya Ism Otasining ismi",
                defaultNew: "Yangi Talaba Testovich",
                defaultOld: "Eski Talaba Testovich"
                },
                exchangeUniversityName: {
                label: "Xorij OTM nomi",
                type: "text",
                required: false,
                placeholder: "Harvard University",
                defaultNew: "Harvard University",
                defaultOld: "Harvard University"
                },
                specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                required: false,
                placeholder: "Informatika va AT",
                defaultNew: "Informatika va AT (NEW)",
                defaultOld: "Informatika va AT (OLD)"
                },
                specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                required: false,
                placeholder: "5110100",
                defaultNew: "5110100",
                defaultOld: "5110100"
                }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.university) body.university = { code: fields.university };
                if (fields.educationYear) body.educationYear = { code: fields.educationYear };
                if (fields.country) body.country = { code: fields.country };
                if (fields.educationType) body.educationType = { code: fields.educationType };
                if (fields.exchangeDocument) body.exchangeDocument = fields.exchangeDocument;
                if (fields.exchangeType) body.exchangeType = fields.exchangeType;
                if (fields.studentFullname) body.studentFullname = fields.studentFullname;
                if (fields.exchangeUniversityName) body.exchangeUniversityName = fields.exchangeUniversityName;
                if (fields.specialityName) body.specialityName = fields.specialityName;
                if (fields.specialityCode) body.specialityCode = fields.specialityCode;
                return body;
                },
                ported: true
                }
            {
                id: 260,
                category: "41.Inspeksiya administrative student2 (Akademik almashinuv)",
                name: "Barcha yozuvlarni olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2",
                requiresAuth: true,
                dependsOn: 1,
                description: "Barcha akademik almashinuv yozuvlarini olish (paginated)",
                storeFirstId: "administrativeStudent2EntityId",
                inputFields: {
                limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
                offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
                returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false },
                returnNulls: { label: "Null qiymatlarni qaytarish", type: "select", options: [{ value: "false", label: "Yo'q" }, { value: "true", label: "Ha" }], default: "false", required: false }
                },
                ported: true
                }
            {
                id: 261,
                category: "41.Inspeksiya administrative student2 (Akademik almashinuv)",
                name: "ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Akademik almashinuv yozuvini UUID bo'yicha olish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent2EntityId" }
                },
                ported: true
                }
            {
                id: 262,
                category: "41.Inspeksiya administrative student2 (Akademik almashinuv)",
                name: "Yozuvni yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Mavjud akademik almashinuv yozuvini yangilash",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent2EntityId" },
                university: { label: "Universitet kodi", type: "text", required: false, placeholder: "301", defaultNew: "", defaultOld: "" },
                educationYear: { label: "O'quv yili", type: "text", required: false, placeholder: "2024", defaultNew: "", defaultOld: "" },
                country: { label: "Davlat kodi", type: "text", required: false, placeholder: "US", defaultNew: "", defaultOld: "" },
                educationType: { label: "Ta'lim turi kodi", type: "text", required: false, placeholder: "11", defaultNew: "", defaultOld: "" },
                studentFullname: { label: "Talaba FIO", type: "text", required: false, placeholder: "FIO", defaultNew: "Yangilangan NEW Talaba", defaultOld: "Yangilangan OLD Talaba" },
                exchangeUniversityName: { label: "Xorij OTM nomi", type: "text", required: false, placeholder: "OTM", defaultNew: "MIT", defaultOld: "MIT" },
                exchangeDocument: { label: "Shartnoma", type: "text", required: false, placeholder: "Shartnoma", defaultNew: "SH-2024-UPD-NEW", defaultOld: "SH-2024-UPD-OLD" },
                exchangeType: { label: "Almashinuv turi", type: "text", required: false, placeholder: "income/outcome", defaultNew: "income", defaultOld: "income" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.university) body.university = { code: fields.university };
                if (fields.educationYear) body.educationYear = { code: fields.educationYear };
                if (fields.country) body.country = { code: fields.country };
                if (fields.educationType) body.educationType = { code: fields.educationType };
                if (fields.studentFullname) body.studentFullname = fields.studentFullname;
                if (fields.exchangeUniversityName) body.exchangeUniversityName = fields.exchangeUniversityName;
                if (fields.exchangeDocument) body.exchangeDocument = fields.exchangeDocument;
                if (fields.exchangeType) body.exchangeType = fields.exchangeType;
                return body;
                },
                ported: true
                }
            {
                id: 263,
                category: "41.Inspeksiya administrative student2 (Akademik almashinuv)",
                name: "Yozuvni o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Akademik almashinuv yozuvini o'chirish (soft delete)",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent2EntityId" }
                },
                ported: true
                }
            {
                id: 264,
                category: "41.Inspeksiya administrative student2 (Akademik almashinuv)",
                name: "Qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Akademik almashinuv yozuvlarini qidirish (GET)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false, helpText: 'CUBA filter: {"conditions":[{"property":"exchangeType","operator":"=","value":"outcome"}]}' },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false },
                returnNulls: { label: "Null qaytarish", type: "select", options: [{ value: "", label: "default" }, { value: "true", label: "true" }, { value: "false", label: "false" }], defaultNew: "", defaultOld: "", required: false }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.filter && fields.filter.trim()) {
                params.push("filter=" + encodeURIComponent(fields.filter.trim()));
                } else {
                params.push("filter=" + encodeURIComponent('{"conditions":[]}'));
                }
                if (fields.limit) params.push("limit=" + encodeURIComponent(fields.limit));
                if (fields.offset) params.push("offset=" + encodeURIComponent(fields.offset));
                if (fields.returnNulls) params.push("returnNulls=" + encodeURIComponent(fields.returnNulls));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                ported: true
                }
            {
                id: 265,
                category: "41.Inspeksiya administrative student2 (Akademik almashinuv)",
                name: "Qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent2/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Akademik almashinuv yozuvlarini qidirish (POST)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                bodyGenerator: (fields) => {
                const body = { limit: parseInt(fields.limit) || 50, offset: parseInt(fields.offset) || 0 };
                if (fields.filter && fields.filter.trim()) {
                try { body.filter = JSON.parse(fields.filter); } catch (e) { body.filter = { conditions: [] }; }
                } else {
                body.filter = { conditions: [] };
                }
                return body;
                },
                ported: true
                }

            // ============================================
            // 42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi) - 7 endpoint
            // ============================================
            {
                id: 266,
                category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
                name: "Yangi yozuv yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3",
                requiresAuth: true,
                dependsOn: 1,
                description: "Bitiruvchilar band bo'lishi - yangi yozuv yaratish",
                storeResultId: "administrativeStudent3EntityId",
                inputFields: {
                university: { label: "Universitet ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
                educationYear: { label: "O'quv yili ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
                student: { label: "Talaba ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
                company: { label: "Kompaniya nomi", type: "text", required: false, placeholder: "Kompaniya nomi", defaultNew: "Test Kompaniya NEW", defaultOld: "Test Kompaniya OLD" },
                position: { label: "Lavozim", type: "text", required: false, placeholder: "Lavozim", defaultNew: "Dasturchi", defaultOld: "Dasturchi" },
                mastersUniversityName: { label: "Magistratura OTM nomi", type: "text", required: false, placeholder: "Magistratura OTM", defaultNew: "", defaultOld: "" },
                educationType: { label: "Ta'lim turi ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.university) body.university = { id: fields.university };
                if (fields.educationYear) body.educationYear = { id: fields.educationYear };
                if (fields.student) body.student = { id: fields.student };
                if (fields.company) body.company = fields.company;
                if (fields.position) body.position = fields.position;
                if (fields.mastersUniversityName) body.mastersUniversityName = fields.mastersUniversityName;
                if (fields.educationType) body.educationType = { id: fields.educationType };
                return body;
                },
                ported: true
                }
            {
                id: 267,
                category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
                name: "Barcha yozuvlarni olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3",
                requiresAuth: true,
                dependsOn: 1,
                description: "Bitiruvchilar band bo'lishi - barcha yozuvlarni olish",
                storeFirstId: "administrativeStudent3EntityId",
                inputFields: {
                limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
                offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
                returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false }
                },
                ported: true
                }
            {
                id: 268,
                category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
                name: "ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Bitiruvchilar band bo'lishi - ID bo'yicha olish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent3EntityId" }
                },
                ported: true
                }
            {
                id: 269,
                category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
                name: "Yozuvni yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Bitiruvchilar band bo'lishi - yozuvni yangilash",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent3EntityId" },
                company: { label: "Kompaniya nomi", type: "text", required: false, placeholder: "Kompaniya nomi", defaultNew: "Yangilangan Kompaniya", defaultOld: "Yangilangan Kompaniya" },
                position: { label: "Lavozim", type: "text", required: false, placeholder: "Lavozim", defaultNew: "Senior Dasturchi", defaultOld: "Senior Dasturchi" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.company) body.company = fields.company;
                if (fields.position) body.position = fields.position;
                return body;
                },
                ported: true
                }
            {
                id: 270,
                category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
                name: "Yozuvni o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Bitiruvchilar band bo'lishi - yozuvni o'chirish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent3EntityId" }
                },
                ported: true
                }
            {
                id: 271,
                category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
                name: "Qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Bitiruvchilar band bo'lishi - qidirish (GET)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.filter && fields.filter.trim()) {
                params.push("filter=" + encodeURIComponent(fields.filter.trim()));
                } else {
                params.push("filter=" + encodeURIComponent('{"conditions":[]}'));
                }
                if (fields.limit) params.push("limit=" + encodeURIComponent(fields.limit));
                if (fields.offset) params.push("offset=" + encodeURIComponent(fields.offset));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                ported: true
                }
            {
                id: 272,
                category: "42.Inspeksiya administrative student3 (Bitiruvchilar band bo'lishi)",
                name: "Qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent3/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Bitiruvchilar band bo'lishi - qidirish (POST)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                bodyGenerator: (fields) => {
                const body = { limit: parseInt(fields.limit) || 50, offset: parseInt(fields.offset) || 0 };
                if (fields.filter && fields.filter.trim()) {
                try { body.filter = JSON.parse(fields.filter); } catch (e) { body.filter = { conditions: [] }; }
                } else {
                body.filter = { conditions: [] };
                }
                return body;
                },
                ported: true
                }

            // ============================================
            // 43.Inspeksiya administrative student4 (Talaba olimpiadalari) - 7 endpoint
            // ============================================
            {
                id: 273,
                category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
                name: "Yangi yozuv yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba olimpiadalari - yangi yozuv yaratish",
                storeResultId: "administrativeStudent4EntityId",
                inputFields: {
                university: { label: "Universitet ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
                educationYear: { label: "O'quv yili ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
                country: { label: "Davlat ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
                student: { label: "Talaba ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
                olimpiadaType: { label: "Olimpiada turi", type: "text", required: false, placeholder: "international/republican", defaultNew: "international", defaultOld: "international" },
                olimpiadaName: { label: "Olimpiada nomi", type: "text", required: false, placeholder: "Olimpiada nomi", defaultNew: "Xalqaro Matematika Olimpiadasi", defaultOld: "Xalqaro Matematika Olimpiadasi" },
                olimpiadaSectionName: { label: "Bo'lim nomi", type: "text", required: false, placeholder: "Bo'lim", defaultNew: "Matematika", defaultOld: "Matematika" },
                olimpiadaPlace: { label: "O'tkazilgan joy", type: "text", required: false, placeholder: "Shahar/Davlat", defaultNew: "Toshkent", defaultOld: "Toshkent" },
                olimpiadaPlaceDate: { label: "Sana", type: "text", required: false, placeholder: "2024 yanvar", defaultNew: "2024 yanvar", defaultOld: "2024 yanvar" },
                olimpiadaSubject: { label: "Fan", type: "text", required: false, placeholder: "Fan nomi", defaultNew: "Matematika", defaultOld: "Matematika" },
                takenPosition: { label: "Olingan o'rin", type: "text", required: false, placeholder: "1/2/3", defaultNew: "1", defaultOld: "1" },
                diplomaSerial: { label: "Diplom seriyasi", type: "text", required: false, placeholder: "AB", defaultNew: "AB", defaultOld: "AB" },
                diplomaNumber: { label: "Diplom raqami", type: "text", required: false, placeholder: "123456", defaultNew: "123456", defaultOld: "123456" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.university) body.university = { id: fields.university };
                if (fields.educationYear) body.educationYear = { id: fields.educationYear };
                if (fields.country) body.country = { id: fields.country };
                if (fields.student) body.student = { id: fields.student };
                if (fields.olimpiadaType) body.olimpiadaType = fields.olimpiadaType;
                if (fields.olimpiadaName) body.olimpiadaName = fields.olimpiadaName;
                if (fields.olimpiadaSectionName) body.olimpiadaSectionName = fields.olimpiadaSectionName;
                if (fields.olimpiadaPlace) body.olimpiadaPlace = fields.olimpiadaPlace;
                if (fields.olimpiadaPlaceDate) body.olimpiadaPlaceDate = fields.olimpiadaPlaceDate;
                if (fields.olimpiadaSubject) body.olimpiadaSubject = fields.olimpiadaSubject;
                if (fields.takenPosition) body.takenPosition = fields.takenPosition;
                if (fields.diplomaSerial) body.diplomaSerial = fields.diplomaSerial;
                if (fields.diplomaNumber) body.diplomaNumber = fields.diplomaNumber;
                return body;
                },
                ported: true
                }
            {
                id: 274,
                category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
                name: "Barcha yozuvlarni olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba olimpiadalari - barcha yozuvlarni olish",
                storeFirstId: "administrativeStudent4EntityId",
                inputFields: {
                limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
                offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
                returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false }
                },
                ported: true
                }
            {
                id: 275,
                category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
                name: "ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba olimpiadalari - ID bo'yicha olish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent4EntityId" }
                },
                ported: true
                }
            {
                id: 276,
                category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
                name: "Yozuvni yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba olimpiadalari - yozuvni yangilash",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent4EntityId" },
                olimpiadaName: { label: "Olimpiada nomi", type: "text", required: false, placeholder: "Olimpiada nomi", defaultNew: "Yangilangan Olimpiada", defaultOld: "Yangilangan Olimpiada" },
                takenPosition: { label: "Olingan o'rin", type: "text", required: false, placeholder: "1/2/3", defaultNew: "2", defaultOld: "2" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.olimpiadaName) body.olimpiadaName = fields.olimpiadaName;
                if (fields.takenPosition) body.takenPosition = fields.takenPosition;
                return body;
                },
                ported: true
                }
            {
                id: 277,
                category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
                name: "Yozuvni o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba olimpiadalari - yozuvni o'chirish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudent4EntityId" }
                },
                ported: true
                }
            {
                id: 278,
                category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
                name: "Qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba olimpiadalari - qidirish (GET)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.filter && fields.filter.trim()) {
                params.push("filter=" + encodeURIComponent(fields.filter.trim()));
                } else {
                params.push("filter=" + encodeURIComponent('{"conditions":[]}'));
                }
                if (fields.limit) params.push("limit=" + encodeURIComponent(fields.limit));
                if (fields.offset) params.push("offset=" + encodeURIComponent(fields.offset));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                ported: true
                }
            {
                id: 279,
                category: "43.Inspeksiya administrative student4 (Talaba olimpiadalari)",
                name: "Qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudent4/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba olimpiadalari - qidirish (POST)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                bodyGenerator: (fields) => {
                const body = { limit: parseInt(fields.limit) || 50, offset: parseInt(fields.offset) || 0 };
                if (fields.filter && fields.filter.trim()) {
                try { body.filter = JSON.parse(fields.filter); } catch (e) { body.filter = { conditions: [] }; }
                } else {
                body.filter = { conditions: [] };
                }
                return body;
                },
                ported: true
                }

            // ============================================
            // 44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari) - 7 endpoint
            // ============================================
            {
                id: 280,
                category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
                name: "Yangi yozuv yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba sport yutuqlari - yangi yozuv yaratish",
                storeResultId: "administrativeStudentSportEntityId",
                inputFields: {
                university: { label: "Universitet ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
                educationYear: { label: "O'quv yili ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
                student: { label: "Talaba ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
                sportType: { label: "Sport turi ID", type: "text", required: false, placeholder: "UUID", defaultNew: "", defaultOld: "" },
                sportDate: { label: "Sana", type: "text", required: false, placeholder: "2024-01-01", defaultNew: "2024-01-15", defaultOld: "2024-01-15" },
                sportTypeRank: { label: "Sport razryadi", type: "text", required: false, placeholder: "1/2/3", defaultNew: "1", defaultOld: "1" },
                sportTypeRankDocument: { label: "Hujjat raqami", type: "text", required: false, placeholder: "AB123456", defaultNew: "AB123456", defaultOld: "AB123456" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.university) body.university = { id: fields.university };
                if (fields.educationYear) body.educationYear = { id: fields.educationYear };
                if (fields.student) body.student = { id: fields.student };
                if (fields.sportType) body.sportType = { id: fields.sportType };
                if (fields.sportDate) body.sportDate = fields.sportDate;
                if (fields.sportTypeRank) body.sportTypeRank = fields.sportTypeRank;
                if (fields.sportTypeRankDocument) body.sportTypeRankDocument = fields.sportTypeRankDocument;
                return body;
                },
                ported: true
                }
            {
                id: 281,
                category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
                name: "Barcha yozuvlarni olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba sport yutuqlari - barcha yozuvlarni olish",
                storeFirstId: "administrativeStudentSportEntityId",
                inputFields: {
                limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
                offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
                returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false }
                },
                ported: true
                }
            {
                id: 282,
                category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
                name: "ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba sport yutuqlari - ID bo'yicha olish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudentSportEntityId" }
                },
                ported: true
                }
            {
                id: 283,
                category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
                name: "Yozuvni yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba sport yutuqlari - yozuvni yangilash",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudentSportEntityId" },
                sportTypeRank: { label: "Sport razryadi", type: "text", required: false, placeholder: "1/2/3", defaultNew: "2", defaultOld: "2" },
                sportTypeRankDocument: { label: "Hujjat raqami", type: "text", required: false, placeholder: "AB123456", defaultNew: "CD789012", defaultOld: "CD789012" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.sportTypeRank) body.sportTypeRank = fields.sportTypeRank;
                if (fields.sportTypeRankDocument) body.sportTypeRankDocument = fields.sportTypeRankDocument;
                return body;
                },
                ported: true
                }
            {
                id: 284,
                category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
                name: "Yozuvni o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba sport yutuqlari - yozuvni o'chirish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeStudentSportEntityId" }
                },
                ported: true
                }
            {
                id: 285,
                category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
                name: "Qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba sport yutuqlari - qidirish (GET)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.filter && fields.filter.trim()) {
                params.push("filter=" + encodeURIComponent(fields.filter.trim()));
                } else {
                params.push("filter=" + encodeURIComponent('{"conditions":[]}'));
                }
                if (fields.limit) params.push("limit=" + encodeURIComponent(fields.limit));
                if (fields.offset) params.push("offset=" + encodeURIComponent(fields.offset));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                ported: true
                }
            {
                id: 286,
                category: "44.Inspeksiya administrative StudentSport (Talaba sport yutuqlari)",
                name: "Qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Talaba sport yutuqlari - qidirish (POST)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                bodyGenerator: (fields) => {
                const body = { limit: parseInt(fields.limit) || 50, offset: parseInt(fields.offset) || 0 };
                if (fields.filter && fields.filter.trim()) {
                try { body.filter = JSON.parse(fields.filter); } catch (e) { body.filter = { conditions: [] }; }
                } else {
                body.filter = { conditions: [] };
                }
                return body;
                },
                ported: true
                }

            // ============================================
            // 45.Inspeksiya administrative SportFacilities (Sport inshootlari) - 7 endpoint
            // ============================================
            {
                id: 287,
                category: "45.Inspeksiya Sport inshootlari",
                name: "Yangi yozuv yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities",
                requiresAuth: true,
                dependsOn: 1,
                description: "Sport inshootlari - yangi yozuv yaratish",
                storeResultId: "administrativeSportFacilitiesEntityId",
                inputFields: {
                university: { label: "Universitet kodi", type: "text", required: false, placeholder: "999", defaultNew: "999", defaultOld: "999" },
                educationYear: { label: "O'quv yili kodi", type: "text", required: false, placeholder: "2021", defaultNew: "2021", defaultOld: "2021" },
                square: { label: "Maydon (kv.m)", type: "number", required: false, placeholder: "1500.5", defaultNew: "1500.5", defaultOld: "1500.5" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.university) body.university = { id: fields.university };
                if (fields.educationYear) body.educationYear = { id: fields.educationYear };
                if (fields.square) body.square = parseFloat(fields.square);
                return body;
                },
                ported: true
                }
            {
                id: 288,
                category: "45.Inspeksiya Sport inshootlari",
                name: "Barcha yozuvlarni olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities",
                requiresAuth: true,
                dependsOn: 1,
                description: "Sport inshootlari - barcha yozuvlarni olish",
                storeFirstId: "administrativeSportFacilitiesEntityId",
                inputFields: {
                limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
                offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
                returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false },
                view: { label: "View", type: "text", required: false, placeholder: "rIAdministrativeSportFacilities-view", defaultNew: "rIAdministrativeSportFacilities-view", defaultOld: "rIAdministrativeSportFacilities-view" }
                },
                ported: true
                }
            {
                id: 289,
                category: "45.Inspeksiya Sport inshootlari",
                name: "ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Sport inshootlari - ID bo'yicha olish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeSportFacilitiesEntityId" }
                },
                ported: true
                }
            {
                id: 290,
                category: "45.Inspeksiya Sport inshootlari",
                name: "Yozuvni yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Sport inshootlari - yozuvni yangilash",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeSportFacilitiesEntityId" },
                square: { label: "Maydon (kv.m)", type: "number", required: false, placeholder: "2000.0", defaultNew: "2500.0", defaultOld: "2500.0" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.square) body.square = parseFloat(fields.square);
                return body;
                },
                ported: true
                }
            {
                id: 291,
                category: "45.Inspeksiya Sport inshootlari",
                name: "Yozuvni o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Sport inshootlari - yozuvni o'chirish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "administrativeSportFacilitiesEntityId" }
                },
                ported: true
                }
            {
                id: 292,
                category: "45.Inspeksiya Sport inshootlari",
                name: "Qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Sport inshootlari - qidirish (GET)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.filter && fields.filter.trim()) {
                params.push("filter=" + encodeURIComponent(fields.filter.trim()));
                } else {
                params.push("filter=" + encodeURIComponent('{"conditions":[]}'));
                }
                if (fields.limit) params.push("limit=" + encodeURIComponent(fields.limit));
                if (fields.offset) params.push("offset=" + encodeURIComponent(fields.offset));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                ported: true
                }
            {
                id: 293,
                category: "45.Inspeksiya Sport inshootlari",
                name: "Qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Sport inshootlari - qidirish (POST)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                bodyGenerator: (fields) => {
                const body = { limit: parseInt(fields.limit) || 50, offset: parseInt(fields.offset) || 0 };
                if (fields.filter && fields.filter.trim()) {
                try { body.filter = JSON.parse(fields.filter); } catch (e) { body.filter = { conditions: [] }; }
                } else {
                body.filter = { conditions: [] };
                }
                return body;
                },
                ported: true
                }

            // ============================================
            // 46.Akademik Uslubiy nashrlar - 7 endpoint
            // ============================================
            {
                id: 294,
                category: "46.Akademik Uslubiy nashrlar",
                name: "Yangi yozuv yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications",
                requiresAuth: true,
                dependsOn: 1,
                description: "Uslubiy nashrlar - yangi yozuv yaratish",
                storeResultId: "academicMethodologicPublicationsEntityId",
                inputFields: {
                university: { label: "Universitet kodi", type: "text", required: false, placeholder: "999", defaultNew: "999", defaultOld: "999" },
                educationYear: { label: "O'quv yili kodi", type: "text", required: false, placeholder: "2021", defaultNew: "2021", defaultOld: "2021" },
                authorFullname: { label: "Muallif ismi", type: "text", required: true, placeholder: "Ism Familiya", defaultNew: "Test Muallif", defaultOld: "Test Muallif" },
                specialityCode: { label: "Mutaxassislik kodi", type: "text", required: false, placeholder: "123456", defaultNew: "101010", defaultOld: "101010" },
                specialityName: { label: "Mutaxassislik nomi", type: "text", required: false, placeholder: "Informatika", defaultNew: "Informatika", defaultOld: "Informatika" },
                bookType: { label: "Kitob turi", type: "text", required: false, placeholder: "textbook/manual", defaultNew: "textbook", defaultOld: "textbook" },
                bookName: { label: "Kitob nomi", type: "text", required: false, placeholder: "Kitob nomi", defaultNew: "Test darslik", defaultOld: "Test darslik" },
                certificateDate: { label: "Sertifikat sanasi", type: "text", required: false, placeholder: "2024-01-15", defaultNew: "2024-01-15", defaultOld: "2024-01-15" },
                certificateNumber: { label: "Sertifikat raqami", type: "text", required: false, placeholder: "123/1", defaultNew: "123/1", defaultOld: "123/1" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.university) body.university = { id: fields.university };
                if (fields.educationYear) body.educationYear = { id: fields.educationYear };
                if (fields.authorFullname) body.authorFullname = fields.authorFullname;
                if (fields.specialityCode) body.specialityCode = fields.specialityCode;
                if (fields.specialityName) body.specialityName = fields.specialityName;
                if (fields.bookType) body.bookType = fields.bookType;
                if (fields.bookName) body.bookName = fields.bookName;
                if (fields.certificateDate) body.certificateDate = fields.certificateDate;
                if (fields.certificateNumber) body.certificateNumber = fields.certificateNumber;
                return body;
                },
                ported: true
                }
            {
                id: 295,
                category: "46.Akademik Uslubiy nashrlar",
                name: "Barcha yozuvlarni olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications",
                requiresAuth: true,
                dependsOn: 1,
                description: "Uslubiy nashrlar - barcha yozuvlarni olish",
                storeFirstId: "academicMethodologicPublicationsEntityId",
                inputFields: {
                limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
                offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
                returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false },
                view: { label: "View", type: "text", required: false, placeholder: "rIAcademicMethodologicPublications-view", defaultNew: "rIAcademicMethodologicPublications-view", defaultOld: "rIAcademicMethodologicPublications-view" }
                },
                ported: true
                }
            {
                id: 296,
                category: "46.Akademik Uslubiy nashrlar",
                name: "ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Uslubiy nashrlar - ID bo'yicha olish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicMethodologicPublicationsEntityId" }
                },
                ported: true
                }
            {
                id: 297,
                category: "46.Akademik Uslubiy nashrlar",
                name: "Yozuvni yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Uslubiy nashrlar - yozuvni yangilash",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicMethodologicPublicationsEntityId" },
                authorFullname: { label: "Muallif ismi", type: "text", required: false, placeholder: "Ism Familiya", defaultNew: "Yangilangan Muallif", defaultOld: "Yangilangan Muallif" },
                bookName: { label: "Kitob nomi", type: "text", required: false, placeholder: "Kitob nomi", defaultNew: "Yangilangan darslik", defaultOld: "Yangilangan darslik" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.authorFullname) body.authorFullname = fields.authorFullname;
                if (fields.bookName) body.bookName = fields.bookName;
                return body;
                },
                ported: true
                }
            {
                id: 298,
                category: "46.Akademik Uslubiy nashrlar",
                name: "Yozuvni o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "Uslubiy nashrlar - yozuvni o'chirish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicMethodologicPublicationsEntityId" }
                },
                ported: true
                }
            {
                id: 299,
                category: "46.Akademik Uslubiy nashrlar",
                name: "Qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Uslubiy nashrlar - qidirish (GET)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.filter && fields.filter.trim()) {
                params.push("filter=" + encodeURIComponent(fields.filter.trim()));
                } else {
                params.push("filter=" + encodeURIComponent('{"conditions":[]}'));
                }
                if (fields.limit) params.push("limit=" + encodeURIComponent(fields.limit));
                if (fields.offset) params.push("offset=" + encodeURIComponent(fields.offset));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                ported: true
                }
            {
                id: 300,
                category: "46.Akademik Uslubiy nashrlar",
                name: "Qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "Uslubiy nashrlar - qidirish (POST)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                bodyGenerator: (fields) => {
                const body = { limit: parseInt(fields.limit) || 50, offset: parseInt(fields.offset) || 0 };
                if (fields.filter && fields.filter.trim()) {
                try { body.filter = JSON.parse(fields.filter); } catch (e) { body.filter = { conditions: [] }; }
                } else {
                body.filter = { conditions: [] };
                }
                return body;
                },
                ported: true
                }

            // ============================================
            // 47.Akademik O'quv ishlari - 7 endpoint
            // ============================================
            {
                id: 301,
                category: "47.Akademik O'quv ishlari",
                name: "Yangi yozuv yaratish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork",
                requiresAuth: true,
                dependsOn: 1,
                description: "O'quv ishlari - yangi yozuv yaratish",
                storeResultId: "academicEducationalWorkEntityId",
                inputFields: {
                university: { label: "Universitet kodi", type: "text", required: false, placeholder: "999", defaultNew: "999", defaultOld: "999" },
                educationYear: { label: "O'quv yili kodi", type: "text", required: false, placeholder: "2021", defaultNew: "2021", defaultOld: "2021" },
                specialityCode: { label: "Mutaxassislik kodi", type: "text", required: false, placeholder: "123456", defaultNew: "123456", defaultOld: "123456" },
                specialityName: { label: "Mutaxassislik nomi", type: "text", required: false, placeholder: "Mutaxassislik", defaultNew: "Informatika", defaultOld: "Informatika" },
                document: { label: "Hujjat", type: "text", required: false, placeholder: "12/5 01.01.2020", defaultNew: "12/5 01.01.2024", defaultOld: "12/5 01.01.2024" },
                subjects: { label: "Fanlar", type: "text", required: false, placeholder: "Fan1, Fan2", defaultNew: "Dasturlash, Ma'lumotlar bazasi", defaultOld: "Dasturlash, Ma'lumotlar bazasi" },
                languageName: { label: "Til", type: "text", required: false, placeholder: "Ingliz tili", defaultNew: "Ingliz tili", defaultOld: "Ingliz tili" },
                course: { label: "Kurs kodi", type: "text", required: false, placeholder: "11", defaultNew: "11", defaultOld: "11" },
                studentCount: { label: "Talabalar soni", type: "number", required: false, placeholder: "80", defaultNew: "50", defaultOld: "50" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.university) body.university = { id: fields.university };
                if (fields.educationYear) body.educationYear = { id: fields.educationYear };
                if (fields.specialityCode) body.specialityCode = fields.specialityCode;
                if (fields.specialityName) body.specialityName = fields.specialityName;
                if (fields.document) body.document = fields.document;
                if (fields.subjects) body.subjects = fields.subjects;
                if (fields.languageName) body.languageName = fields.languageName;
                if (fields.course) body.course = { id: fields.course };
                if (fields.studentCount) body.studentCount = parseInt(fields.studentCount);
                return body;
                },
                ported: true
                }
            {
                id: 302,
                category: "47.Akademik O'quv ishlari",
                name: "Barcha yozuvlarni olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork",
                requiresAuth: true,
                dependsOn: 1,
                description: "O'quv ishlari - barcha yozuvlarni olish",
                storeFirstId: "academicEducationalWorkEntityId",
                inputFields: {
                limit: { label: "Limit", type: "number", required: false, placeholder: "50", default: "50" },
                offset: { label: "Offset", type: "number", required: false, placeholder: "0", default: "0" },
                returnCount: { label: "Jami sonni qaytarish", type: "select", options: [{ value: "true", label: "Ha" }, { value: "false", label: "Yo'q" }], default: "true", required: false },
                view: { label: "View", type: "text", required: false, placeholder: "rIAcademicEducationalWork-view", defaultNew: "rIAcademicEducationalWork-view", defaultOld: "rIAcademicEducationalWork-view" }
                },
                ported: true
                }
            {
                id: 303,
                category: "47.Akademik O'quv ishlari",
                name: "ID bo'yicha olish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "O'quv ishlari - ID bo'yicha olish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicEducationalWorkEntityId" }
                },
                ported: true
                }
            {
                id: 304,
                category: "47.Akademik O'quv ishlari",
                name: "Yozuvni yangilash (PUT)",
                method: "PUT",
                url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "O'quv ishlari - yozuvni yangilash",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicEducationalWorkEntityId" },
                subjects: { label: "Fanlar", type: "text", required: false, placeholder: "Fan1, Fan2", defaultNew: "Yangilangan fanlar", defaultOld: "Yangilangan fanlar" },
                studentCount: { label: "Talabalar soni", type: "number", required: false, placeholder: "100", defaultNew: "100", defaultOld: "100" }
                },
                bodyGenerator: (fields) => {
                const body = {};
                if (fields.subjects) body.subjects = fields.subjects;
                if (fields.studentCount) body.studentCount = parseInt(fields.studentCount);
                return body;
                },
                ported: true
                }
            {
                id: 305,
                category: "47.Akademik O'quv ishlari",
                name: "Yozuvni o'chirish (DELETE)",
                method: "DELETE",
                url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork/{entityId}",
                requiresAuth: true,
                dependsOn: 1,
                description: "O'quv ishlari - yozuvni o'chirish",
                inputFields: {
                entityId: { label: "Entity UUID", type: "text", required: true, placeholder: "UUID", useStoredId: "academicEducationalWorkEntityId" }
                },
                ported: true
                }
            {
                id: 306,
                category: "47.Akademik O'quv ishlari",
                name: "Qidirish (GET)",
                method: "GET",
                url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "O'quv ishlari - qidirish (GET)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.filter && fields.filter.trim()) {
                params.push("filter=" + encodeURIComponent(fields.filter.trim()));
                } else {
                params.push("filter=" + encodeURIComponent('{"conditions":[]}'));
                }
                if (fields.limit) params.push("limit=" + encodeURIComponent(fields.limit));
                if (fields.offset) params.push("offset=" + encodeURIComponent(fields.offset));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                ported: true
                }
            {
                id: 307,
                category: "47.Akademik O'quv ishlari",
                name: "Qidirish (POST)",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_RIAcademicEducationalWork/search",
                requiresAuth: true,
                dependsOn: 1,
                description: "O'quv ishlari - qidirish (POST)",
                inputFields: {
                filter: { label: "CUBA JSON filter", type: "textarea", rows: 3, placeholder: '{"conditions":[]}', defaultNew: '', defaultOld: '', required: false },
                limit: { label: "Limit", type: "number", placeholder: "50", defaultNew: "10", defaultOld: "10", required: false },
                offset: { label: "Offset", type: "number", placeholder: "0", defaultNew: "0", defaultOld: "0", required: false }
                },
                bodyGenerator: (fields) => {
                const body = { limit: parseInt(fields.limit) || 50, offset: parseInt(fields.offset) || 0 };
                if (fields.filter && fields.filter.trim()) {
                try { body.filter = JSON.parse(fields.filter); } catch (e) { body.filter = { conditions: [] }; }
                } else {
                body.filter = { conditions: [] };
                }
                return body;
                },
                ported: true
                }

            // ============================================
            // 48.Mehnat (1 endpoint)
            // OLD-HEMIS FORMAT BILAN 100% MOSLIK!
            // ============================================
            {
                id: 308,
                category: "48.Mehnat",
                name: "Mehnat daftarchasi ma'lumotlari",
                method: "GET",
                url: "/services/employment/workbook",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "31304931230067",
                defaultOld: "31304931230067",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Mehnat daftarchasi ma'lumotlari**
                <b>Endpoint:</b> GET /services/employment/workbook
                <b>Parametrlar:</b>
                - pinfl: Fuqaroning PINFL raqami (14 raqamli)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "_entityName": "hemishe_Workbook",
                "id": "",
                "result": "1",
                "comments": "Ok",
                "data": {
                "_entityName": "hemishe_Data",
                "id": "",
                "jobs": [
                {
                "_entityName": "hemishe_EmployeeJob",
                "id": "",
                "orgName": "TOSHKENT DAVLAT UNIVERSITETI",
                "orgInn": "201678867",
                "positionName": "Bosh buhgalter",
                "dateBegin": "2005-08-17",
                "dateEnd": "",
                ...
                }
                ]
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 49.Fakultetlar (1 endpoint)
            // OLD-HEMIS FORMAT BILAN 100% MOSLIK!
            // ============================================
            {
                id: 309,
                category: "49.Fakultetlar",
                name: "OTM fakultetlarini olish",
                method: "GET",
                url: "/services/faculty/get",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                university: {
                label: "OTM kodi",
                type: "text",
                defaultNew: "401",
                defaultOld: "301",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.university) params.push("university=" + encodeURIComponent(fields.university));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**OTM fakultetlarini olish**
                <b>Endpoint:</b> GET /services/faculty/get
                <b>Parametrlar:</b>
                - university: OTM kodi (masalan: 301, 401)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": [
                {
                "_entityName": "hemishe_EUniversityDepartment",
                "id": "301-101",
                "code": "301-101",
                "version": 3,
                "nameUz": "Axborot texnologiyalari",
                "nameRu": "Информационные технологии"
                }
                ]
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 50.Mutaxassisliklar (2 endpoint)
            // OLD-HEMIS FORMAT BILAN 100% MOSLIK!
            // ============================================
            {
                id: 310,
                category: "50.Mutaxassisliklar",
                name: "OTM mutaxassisliklarini olish",
                method: "GET",
                url: "/services/speciality/get",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                university: {
                label: "OTM kodi",
                type: "text",
                defaultNew: "401",
                defaultOld: "999",
                required: true
                },
                type: {
                label: "Ta'lim turi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: false
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.university) params.push("university=" + encodeURIComponent(fields.university));
                if (fields.type) params.push("type=" + encodeURIComponent(fields.type));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**OTM mutaxassisliklarini olish**
                <b>Endpoint:</b> GET /services/speciality/get
                <b>Parametrlar:</b>
                - university: OTM kodi (masalan: 999, 401)
                - type: Ta'lim turi kodi (11=Bakalavr, 12=Magistr, ixtiyoriy)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": [
                {
                "_entityName": "hemishe_EUniversitySpeciality",
                "id": "40abbb45-d332-c663-fa0b-70b076098c02",
                "active": true,
                "version": 1,
                "specialityCode": "11111122",
                "specialityName": "test"
                }
                ]
                }
                </pre>`,
                ported: true
                }
            {
                id: 311,
                category: "50.Mutaxassisliklar",
                name: "OTM mutaxassisliklarini yuborish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EUniversitySpeciality",
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
                facultyCode: {
                label: "Fakultet kodi",
                type: "text",
                defaultNew: "401-102",
                defaultOld: "999-102",
                required: true
                },
                educationTypeCode: {
                label: "Ta'lim turi kodi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: true
                },
                educationYearCode: {
                label: "Ta'lim yili",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2021",
                required: true
                },
                specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                defaultNew: "60310900",
                defaultOld: "2223",
                required: true
                },
                specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                defaultNew: "Psixologiya",
                defaultOld: "test2223",
                required: true
                },
                active: {
                label: "Faol",
                type: "text",
                defaultNew: "true",
                defaultOld: "true",
                required: false
                }
                },
                // OLD-HEMIS: ARRAY formatida yuboradi!
                bodyGenerator: function(fields) {
                return [
                {
                "university": {"code": fields.universityCode},
                "faculty": {"code": fields.facultyCode},
                "educationType": {"code": fields.educationTypeCode},
                "educationYear": {"code": fields.educationYearCode},
                "specialityCode": fields.specialityCode,
                "specialityName": fields.specialityName,
                "active": fields.active === "true"
                }
                ];
                },
                description: `**OTM mutaxassisliklarini yuborish**
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EUniversitySpeciality
                <b>OLD-HEMIS Request formati (ARRAY!):</b>
                <pre>
                [
                {
                "university": {"code": "999"},
                "faculty": {"code": "999-102"},
                "educationType": {"code": "11"},
                "educationYear": {"code": "2021"},
                "specialityCode": "2223",
                "specialityName": "test2223",
                "active": true
                }
                ]
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                [
                {
                "_entityName": "hemishe_EUniversitySpeciality",
                "_instanceName": "2223 test2223",
                "id": "7f797721-93bf-0518-49eb-49ddf26dd124"
                }
                ]
                </pre>`,
                ported: true
                }

            // ============================================
            // 51.Guruhlar (2 endpoint)
            // ============================================
            {
                id: 312,
                category: "51.Guruhlar",
                name: "Guruhlar ro'yxatini olish",
                method: "GET",
                url: "/services/group/get",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                university: {
                label: "OTM kodi",
                type: "text",
                defaultNew: "401",
                defaultOld: "999",
                required: true
                },
                type: {
                label: "Ta'lim turi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: false
                },
                year: {
                label: "O'quv yili",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2021",
                required: false
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.university) params.push("university=" + encodeURIComponent(fields.university));
                if (fields.type) params.push("type=" + encodeURIComponent(fields.type));
                if (fields.year) params.push("year=" + encodeURIComponent(fields.year));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Guruhlar ro'yxatini olish**
                <b>Endpoint:</b> GET /services/group/get
                <b>Parametrlar:</b>
                - university: OTM kodi (masalan: 999, 401)
                - type: Ta'lim turi kodi (11=Bakalavr, ixtiyoriy)
                - year: O'quv yili (masalan: 2021, 2024)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": [
                {
                "_entityName": "hemishe_EUniversityGroup",
                "id": "88d77617-eb47-a99c-d46b-5dcaa57347cc",
                "groupId": "1",
                "active": true,
                "version": 1,
                "groupName": "125-21"
                }
                ]
                }
                </pre>`,
                ported: true
                }
            {
                id: 313,
                category: "51.Guruhlar",
                name: "Guruhlarni yuborish",
                method: "POST",
                url: "/app/rest/v2/entities/hemishe_EUniversityGroup",
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
                educationTypeCode: {
                label: "Ta'lim turi kodi",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: true
                },
                educationYearCode: {
                label: "O'quv yili",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2021",
                required: true
                },
                groupId: {
                label: "Guruh ID",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: true
                },
                groupName: {
                label: "Guruh nomi",
                type: "text",
                defaultNew: "200-24",
                defaultOld: "200-21",
                required: true
                }
                },
                // OLD-HEMIS: ARRAY formatida yuboradi!
                bodyGenerator: function(fields) {
                return [
                {
                "university": {"code": fields.universityCode},
                "educationType": {"code": fields.educationTypeCode},
                "educationYear": {"code": fields.educationYearCode},
                "groupId": fields.groupId,
                "groupName": fields.groupName
                }
                ];
                },
                description: `**Guruhlarni yuborish**
                <b>Endpoint:</b> POST /app/rest/v2/entities/hemishe_EUniversityGroup
                <b>OLD-HEMIS Request formati (ARRAY!):</b>
                <pre>
                [
                {
                "university": {"code": "999"},
                "educationType": {"code": "11"},
                "educationYear": {"code": "2021"},
                "groupId": "1",
                "groupName": "200-21"
                }
                ]
                </pre>
                <b>OLD-HEMIS Response formati (201 Created):</b>
                <pre>
                [
                {
                "_entityName": "hemishe_EUniversityGroup",
                "_instanceName": "200-21",
                "id": "dca5bd9f-3ff5-7957-bfaa-ed61fb62b2ce"
                }
                ]
                </pre>`,
                ported: true
                }

            // ============================================
            // 52.Mail (2 endpoint)
            // ============================================
            {
                id: 314,
                category: "52.Mail",
                name: "Email yuborish",
                method: "POST",
                url: "/services/mail/send",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                id: {
                label: "ID",
                type: "text",
                defaultNew: "999999",
                defaultOld: "999999",
                required: true
                },
                resetLink: {
                label: "Reset havolasi",
                type: "text",
                defaultNew: "https://hemis.uz/reset_url",
                defaultOld: "https://hemis.uz/reset_url",
                required: true
                },
                to: {
                label: "Email manzil",
                type: "text",
                defaultNew: "no-reply@hemis.uz",
                defaultOld: "no-reply@hemis.uz",
                required: true
                }
                },
                bodyGenerator: function(fields) {
                return {
                "id": fields.id,
                "resetLink": fields.resetLink,
                "to": fields.to
                };
                },
                description: `**Email yuborish**
                <b>Endpoint:</b> POST /services/mail/send
                <b>OLD-HEMIS Request formati:</b>
                <pre>
                {
                "id": "999999",
                "resetLink": "https://hemis.uz/reset_url",
                "to": "no-reply@hemis.uz"
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "id": "999999",
                "reset_link": "https://hemis.uz/reset_url",
                "to": "no-reply@hemis.uz"
                }
                </pre>`,
                ported: true
                }
            {
                id: 315,
                category: "52.Mail",
                name: "Tasdiqlash kodini yuborish",
                method: "POST",
                url: "/services/send/verifyCode",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                id: {
                label: "ID",
                type: "text",
                defaultNew: "999999",
                defaultOld: "999999",
                required: true
                },
                phone: {
                label: "Telefon",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false
                },
                email: {
                label: "Email",
                type: "text",
                defaultNew: "test@example.com",
                defaultOld: "kanet4u@gmail.com",
                required: true
                },
                verify_code: {
                label: "Tasdiqlash kodi",
                type: "text",
                defaultNew: "123456",
                defaultOld: "123456",
                required: true
                }
                },
                bodyGenerator: function(fields) {
                return {
                "id": fields.id,
                "phone": fields.phone || "",
                "email": fields.email,
                "verify_code": fields.verify_code
                };
                },
                description: `**Tasdiqlash kodini yuborish**
                <b>Endpoint:</b> POST /services/send/verifyCode
                <b>OLD-HEMIS Request formati:</b>
                <pre>
                {
                "id": "999999",
                "phone": "",
                "email": "kanet4u@gmail.com",
                "verify_code": "123456"
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "email": {
                "success": true,
                "verify_code": "123456",
                "email": "kanet4u@gmail.com"
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 53.Healthcheck (1 endpoint)
            // ============================================
            {
                id: 316,
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

            // ============================================
            // 54.Transkript (1 endpoint)
            // ============================================
            {
                id: 317,
                category: "54.Transkript",
                name: "Transkript ariza berish",
                method: "GET",
                url: "/services/transcript/get",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "999211100039",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Transkript ariza berish**
                <b>Endpoint:</b> GET /services/transcript/get
                <b>Parametrlar:</b>
                - pinfl: Talabaning PINFL raqami (14 raqamli)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "999211100039",
                "message": "Transcript data"
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 55.DTM Mandat (1 endpoint)
            // ============================================
            {
                id: 318,
                category: "55.DTM",
                name: "DTM Mandat ma'lumotlari",
                method: "GET",
                url: "/services/mandat/get",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "42704911920029",
                required: true
                },
                passport: {
                label: "Passport",
                type: "text",
                defaultNew: "AB1234567",
                defaultOld: "AB4454415",
                required: true
                },
                year: {
                label: "Yil",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2022",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                if (fields.passport) params.push("passport=" + encodeURIComponent(fields.passport));
                if (fields.year) params.push("year=" + encodeURIComponent(fields.year));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**DTM Mandat ma'lumotlari**
                <b>Endpoint:</b> GET /services/mandat/get
                <b>Parametrlar:</b>
                - pinfl: Abituriyentning PINFL raqami
                - passport: Passport seriya va raqami
                - year: Imtihon yili
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "status": 200,
                "entrantId": 1400811,
                "fullName": "SATTOROVA Z. F.",
                "result": 33.4,
                "ustuv": 1,
                "tumanName": "Bandixon tumani",
                "regionName": "Surxondaryo viloyati",
                "nBall1": 1.1,
                "nBall2": 1.1,
                "s1Name": "Ona tili",
                "s2Name": "Matematika",
                ...
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 56.OAK - Oliy Attestatsiya Komissiyasi (1 endpoint)
            // ============================================
            {
                id: 319,
                category: "56.OAK",
                name: "OAK ma'lumotlari (PINFL bo'yicha)",
                method: "GET",
                url: "/services/oak/byPin",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "32707860270013",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**OAK ma'lumotlari (PINFL bo'yicha)**
                <b>Endpoint:</b> GET /services/oak/byPin
                <b>Parametrlar:</b>
                - pinfl: Xodimning PINFL raqami
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "id": 123456,
                "jsonrpc": "2.0",
                "result": {
                "message": "Citizen retrieved successfully",
                "result": [
                {
                "birth_date": "1986-07-27",
                "f_name": "САНЖАР",
                "gender": 1,
                "m_name": "ИЗЗАТУЛЛАЕВИЧ",
                "passport": "AB0916887",
                "pin": "32707860270013",
                "s_name": "ХИКМАТУЛЛАЕВ",
                "title_details": {
                "title": "Доцент",
                "diploma_number": "01№013365",
                "confirmed_date": "22.11.1992"
                }
                }
                ],
                "success": true
                }
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 57.Contract (1 endpoint)
            // ============================================
            {
                id: 320,
                category: "57.Contract",
                name: "Shartnoma ma'lumotlarini olish",
                method: "GET",
                url: "/services/contract/get",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "30503941620012",
                required: true
                },
                year: {
                label: "Yil",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2022",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                if (fields.year) params.push("year=" + encodeURIComponent(fields.year));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Shartnoma ma'lumotlarini olish**
                <b>Endpoint:</b> GET /services/contract/get
                <b>Parametrlar:</b>
                - pinfl: Talabaning PINFL raqami (14 raqamli)
                - year: O'quv yili (masalan: 2022, 2024)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "30503941620012",
                "year": 2022,
                "contracts": [],
                "message": "Contract data"
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 58.UzASBO (3 endpoint)
            // ============================================
            {
                id: 321,
                category: "58.UzASBO",
                name: "Stipendiya ma'lumotlarini olish",
                method: "GET",
                url: "/services/uzasbo/scholarship",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                inn: {
                label: "INN",
                type: "text",
                defaultNew: "123456789",
                defaultOld: "201354108",
                required: true
                },
                year: {
                label: "Yil",
                type: "text",
                defaultNew: "2024",
                defaultOld: "2023",
                required: true
                },
                month: {
                label: "Oy",
                type: "text",
                defaultNew: "1",
                defaultOld: "9",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.inn) params.push("inn=" + encodeURIComponent(fields.inn));
                if (fields.year) params.push("year=" + encodeURIComponent(fields.year));
                if (fields.month) params.push("month=" + encodeURIComponent(fields.month));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**UzASBO Stipendiya ma'lumotlarini olish**
                <b>Endpoint:</b> GET /services/uzasbo/scholarship
                <b>Parametrlar:</b>
                - inn: Tashkilot INN raqami
                - year: Yil
                - month: Oy (1-12)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "inn": "201354108",
                "year": 2023,
                "month": 9,
                "scholarships": [],
                "message": "UzASBO scholarship data"
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 322,
                category: "58.UzASBO",
                name: "Stipendiya tekshirish",
                method: "POST",
                url: "/services/student/checkScholarship2",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                tin: {
                label: "TIN",
                type: "text",
                defaultNew: "123456789",
                defaultOld: "207095330",
                required: true
                },
                docOn: {
                label: "Hujjat sanasi",
                type: "text",
                defaultNew: "2024-01-15",
                defaultOld: "2024-02-05",
                required: true
                },
                pinfl1: {
                label: "Talaba 1 PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "60209047160010",
                required: true
                },
                sum1: {
                label: "Talaba 1 summa",
                type: "text",
                defaultNew: "100000",
                defaultOld: "103576",
                required: false
                }
                },
                bodyGenerator: function(fields) {
                return {
                "tin": fields.tin,
                "docOn": fields.docOn,
                "students": [
                {
                "pinfl": fields.pinfl1,
                "sum": fields.sum1 || ""
                }
                ]
                };
                },
                description: `**Stipendiya tekshirish (Scholarship check)**
                <b>Endpoint:</b> POST /services/student/checkScholarship2
                <b>OLD-HEMIS Request formati:</b>
                <pre>
                {
                "tin": "207095330",
                "docOn": "2024-02-05",
                "students": [
                {"pinfl": "60209047160010", "sum": "103576"},
                {"pinfl": "52712015360046"},
                {"pinfl": "527120153600461", "sum": "103576"}
                ]
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "tin": "207095330",
                "docOn": "2024-02-05",
                "results": [
                {"pinfl": "60209047160010", "status": "OK"},
                {"pinfl": "52712015360046", "status": "OK"},
                {"pinfl": "527120153600461", "status": "INVALID_PINFL"}
                ]
                }
                }
                </pre>
                <b>Izoh:</b> PINFL 14 raqamli bo'lishi kerak, aks holda INVALID_PINFL qaytariladi.`,
                ported: true
                }
            {
                id: 323,
                category: "58.UzASBO",
                name: "Type test",
                method: "GET",
                url: "/services/test/typetest",
                requiresAuth: true,
                hasBody: false,
                inputFields: {},
                urlBuilder: function(fields) {
                return this.url;
                },
                description: `**Type test endpoint**
                <b>Endpoint:</b> GET /services/test/typetest
                <b>Parametrlar:</b> Yo'q
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "type": "test",
                "message": "Type test endpoint"
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 59.Test (1 endpoint)
            // ============================================
            {
                id: 324,
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

            // ============================================
            // 60.Soliq (1 endpoint)
            // ============================================
            {
                id: 325,
                category: "60.Soliq",
                name: "Ijara shartnomasi",
                method: "GET",
                url: "/services/tax/rent",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "51805035330018",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Ijara shartnomasi (Soliq ma'lumotlari)**
                <b>Endpoint:</b> GET /services/tax/rent
                <b>Parametrlar:</b>
                - pinfl: Fuqaroning PINFL raqami (14 raqamli)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "51805035330018",
                "rentContracts": [],
                "message": "Tax rent data"
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 61.Ijtimoiy himoya (5 endpoint)
            // ============================================
            {
                id: 326,
                category: "61.Ijtimoiy himoya",
                name: "Yagona ijtimoiy reestr",
                method: "GET",
                url: "/services/social/singleRegister",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "41108842860015",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Yagona ijtimoiy reestr**
                <b>Endpoint:</b> GET /services/social/singleRegister
                <b>Parametrlar:</b>
                - pinfl: Fuqaroning PINFL raqami
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "41108842860015",
                "registered": false,
                "message": "Social integration - implementation pending"
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 327,
                category: "61.Ijtimoiy himoya",
                name: "Temir daftar (to'liq)",
                method: "GET",
                url: "/services/social/daftarFull",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "41108842860015",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Temir daftar (to'liq ma'lumot)**
                <b>Endpoint:</b> GET /services/social/daftarFull
                <b>Parametrlar:</b>
                - pinfl: Fuqaroning PINFL raqami
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "41108842860015",
                "daftar": {},
                "message": "Social integration - implementation pending"
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 328,
                category: "61.Ijtimoiy himoya",
                name: "Temir daftar (qisqa)",
                method: "GET",
                url: "/services/social/daftarShort",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "41108842860015",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Temir daftar (qisqa ma'lumot)**
                <b>Endpoint:</b> GET /services/social/daftarShort
                <b>Parametrlar:</b>
                - pinfl: Fuqaroning PINFL raqami
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "41108842860015",
                "daftar": {},
                "message": "Social integration - implementation pending"
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 329,
                category: "61.Ijtimoiy himoya",
                name: "Ayollar daftari",
                method: "GET",
                url: "/services/social/women",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "42601793500108",
                required: true
                },
                sn: {
                label: "Seriya va raqam",
                type: "text",
                defaultNew: "AA1234567",
                defaultOld: "KA0773072",
                required: false
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                if (fields.sn) params.push("sn=" + encodeURIComponent(fields.sn));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Ayollar daftari**
                <b>Endpoint:</b> GET /services/social/women
                <b>Parametrlar:</b>
                - pinfl: Fuqaroning PINFL raqami
                - sn: Hujjat seriyasi va raqami (ixtiyoriy)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "42601793500108",
                "sn": "KA0773072",
                "support": [],
                "message": "Social integration - implementation pending"
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 330,
                category: "61.Ijtimoiy himoya",
                name: "Yoshlar daftari",
                method: "GET",
                url: "/services/social/young",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "42601793500108",
                required: true
                },
                seria: {
                label: "Seriya",
                type: "text",
                defaultNew: "AA",
                defaultOld: "KA",
                required: true
                },
                number: {
                label: "Raqam",
                type: "text",
                defaultNew: "1234567",
                defaultOld: "0773072",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                if (fields.seria) params.push("seria=" + encodeURIComponent(fields.seria));
                if (fields.number) params.push("number=" + encodeURIComponent(fields.number));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Yoshlar daftari**
                <b>Endpoint:</b> GET /services/social/young
                <b>Parametrlar:</b>
                - pinfl: Fuqaroning PINFL raqami
                - seria: Hujjat seriyasi (masalan: KA)
                - number: Hujjat raqami (masalan: 0773072)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "42601793500108",
                "seria": "KA",
                "number": "0773072",
                "support": [],
                "message": "Social integration - implementation pending"
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 62.Stipendiya (3 endpoint)
            // ============================================
            {
                id: 331,
                category: "62.Stipendiya",
                name: "Stipendiya yaratish (EStudentScholarshipFull)",
                method: "POST",
                url: "/entities/hemishe_EStudentScholarshipFull",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                scholarshipCode: {
                label: "Stipendiya kodi",
                type: "text",
                defaultNew: "STI-2024-001",
                defaultOld: "STI-2024-001",
                required: true
                },
                studentId: {
                label: "Talaba ID",
                type: "text",
                defaultNew: "11111111-1111-1111-1111-111111111111",
                defaultOld: "11111111-1111-1111-1111-111111111111",
                required: true
                },
                semester: {
                label: "Semestr",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: false
                },
                amount: {
                label: "Summa",
                type: "text",
                defaultNew: "500000",
                defaultOld: "500000",
                required: false
                }
                },
                urlBuilder: function(fields) {
                return this.url;
                },
                bodyBuilder: function(fields) {
                return JSON.stringify({
                scholarshipCode: fields.scholarshipCode || "STI-2024-001",
                _student: fields.studentId ? { id: fields.studentId } : null,
                semester: parseInt(fields.semester) || 1,
                amount: parseFloat(fields.amount) || 500000
                }, null, 2);
                },
                description: `**Stipendiya yaratish (EStudentScholarshipFull)**
                <b>Endpoint:</b> POST /entities/hemishe_EStudentScholarshipFull
                <b>So'rov formati:</b>
                <pre>
                {
                "scholarshipCode": "STI-2024-001",
                "_student": { "id": "..." },
                "semester": 1,
                "amount": 500000
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "_entityName": "hemishe_EStudentScholarshipFull",
                "_instanceName": "STI-2024-001",
                "id": "generated-uuid"
                }
                </pre>`,
                ported: true
                }
            {
                id: 332,
                category: "62.Stipendiya",
                name: "Stipendiya oylik tulov (EStudentScholarshipAmount)",
                method: "POST",
                url: "/entities/hemishe_EStudentScholarshipAmount",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                scholarshipId: {
                label: "Stipendiya ID",
                type: "text",
                defaultNew: "11111111-1111-1111-1111-111111111111",
                defaultOld: "7ad1eb02-fec1-1da3-2288-fc9a74371618",
                required: true
                },
                month: {
                label: "Oy",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: true
                },
                amount: {
                label: "Summa",
                type: "text",
                defaultNew: "500000",
                defaultOld: "500000",
                required: true
                }
                },
                urlBuilder: function(fields) {
                return this.url;
                },
                bodyBuilder: function(fields) {
                return JSON.stringify({
                _scholarship: { id: fields.scholarshipId },
                month: parseInt(fields.month) || 1,
                amount: parseFloat(fields.amount) || 500000
                }, null, 2);
                },
                description: `**Stipendiya oylik tulov (EStudentScholarshipAmount)**
                <b>Endpoint:</b> POST /entities/hemishe_EStudentScholarshipAmount
                <b>So'rov formati:</b>
                <pre>
                {
                "_scholarship": { "id": "scholarship-uuid" },
                "month": 1,
                "amount": 500000
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "_entityName": "hemishe_EStudentScholarshipAmount",
                "_instanceName": "...",
                "id": "generated-uuid"
                }
                </pre>`,
                ported: true
                }
            {
                id: 333,
                category: "62.Stipendiya",
                name: "Stipendiya summalarini o'chirish",
                method: "GET",
                url: "/services/scholarship/deleteAmounts",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                scholarshipId: {
                label: "Stipendiya ID (UUID)",
                type: "text",
                defaultNew: "11111111-1111-1111-1111-111111111111",
                defaultOld: "7ad1eb02-fec1-1da3-2288-fc9a74371618",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.scholarshipId) params.push("scholarshipId=" + encodeURIComponent(fields.scholarshipId));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Stipendiya summalarini o'chirish**
                <b>Endpoint:</b> GET /services/scholarship/deleteAmounts
                <b>Parametrlar:</b>
                - scholarshipId: Stipendiya UUID (required)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "scholarshipId": "7ad1eb02-fec1-1da3-2288-fc9a74371618",
                "deleted": true,
                "message": "Scholarship amounts deleted"
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 63.Billing (2 endpoint)
            // ============================================
            {
                id: 334,
                category: "63.Billing",
                name: "Stipendiya to'lovi (UzASBO)",
                method: "POST",
                url: "/services/billing/scholarship",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                tin: {
                label: "Tashkilot INN",
                type: "text",
                defaultNew: "205771544",
                defaultOld: "205771544",
                required: true
                },
                pinfl1: {
                label: "Talaba 1 PINFL",
                type: "text",
                defaultNew: "62708005690041",
                defaultOld: "62708005690041",
                required: true
                },
                pinfl2: {
                label: "Talaba 2 PINFL",
                type: "text",
                defaultNew: "62708005690043",
                defaultOld: "62708005690043",
                required: false
                },
                pinfl3: {
                label: "Talaba 3 PINFL",
                type: "text",
                defaultNew: "40211905590019",
                defaultOld: "40211905590019",
                required: false
                }
                },
                urlBuilder: function(fields) {
                return this.url;
                },
                bodyBuilder: function(fields) {
                let pinflList = [fields.pinfl1];
                if (fields.pinfl2) pinflList.push(fields.pinfl2);
                if (fields.pinfl3) pinflList.push(fields.pinfl3);
                return JSON.stringify({
                tin: fields.tin,
                pinfl: pinflList
                }, null, 2);
                },
                description: `**Stipendiya to'lovi (UzASBO)**
                <b>Endpoint:</b> POST /services/billing/scholarship
                <b>So'rov formati:</b>
                <pre>
                {
                "tin": "205771544",
                "pinfl": [
                "62708005690041",
                "62708005690043",
                "40211905590019"
                ]
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "processedCount": 3,
                "tin": "205771544",
                "students": [],
                "message": "Scholarship query processed"
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 335,
                category: "63.Billing",
                name: "Hisob-faktura yaratish",
                method: "POST",
                url: "/services/billing/invoice",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                OrganizationId: {
                label: "Tashkilot ID",
                type: "text",
                defaultNew: "303",
                defaultOld: "303",
                required: true
                },
                EduFacultyId: {
                label: "Fakultet ID",
                type: "text",
                defaultNew: "",
                defaultOld: "",
                required: false
                },
                EduYearId: {
                label: "O'quv yili ID",
                type: "text",
                defaultNew: "3",
                defaultOld: "3",
                required: true
                },
                EduTypeId: {
                label: "Ta'lim turi ID",
                type: "text",
                defaultNew: "11",
                defaultOld: "11",
                required: true
                }
                },
                urlBuilder: function(fields) {
                return this.url;
                },
                bodyBuilder: function(fields) {
                return JSON.stringify({
                params: {
                OrganizationId: parseInt(fields.OrganizationId) || 303,
                EduFacultyId: fields.EduFacultyId || "",
                EduYearId: parseInt(fields.EduYearId) || 3,
                EduTypeId: fields.EduTypeId || "11"
                }
                }, null, 2);
                },
                description: `**Hisob-faktura yaratish**
                <b>Endpoint:</b> POST /services/billing/invoice
                <b>So'rov formati:</b>
                <pre>
                {
                "params": {
                "OrganizationId": 303,
                "EduFacultyId": "",
                "EduYearId": 3,
                "EduTypeId": "11"
                }
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "invoices": [],
                "message": "Invoice query processed",
                "organizationId": 303,
                "eduYearId": 3,
                "eduTypeId": "11"
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 64.OTM (3 endpoint)
            // ============================================
            {
                id: 336,
                category: "64.OTM",
                name: "Talaba ma'lumotlari (ID bo'yicha)",
                method: "GET",
                url: "/services/otm/studentInfoById",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                studentId: {
                label: "Talaba ID (string format)",
                type: "text",
                defaultNew: "999221100044",
                defaultOld: "999221100044",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.studentId) params.push("studentId=" + encodeURIComponent(fields.studentId));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Talaba ma'lumotlari (ID bo'yicha)**
                <b>Endpoint:</b> GET /services/otm/studentInfoById
                <b>Parametrlar:</b>
                - studentId: Talaba ID (string format, masalan: 999221100044)
                <b>Eslatma:</b> OLD-HEMIS da studentId UUID emas, string formatda (999221100044)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "studentId": "999221100044",
                "fullName": "...",
                "faculty": "...",
                "speciality": "...",
                "course": 1
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 337,
                category: "64.OTM",
                name: "Tutor talabalari ro'yxati",
                method: "GET",
                url: "/services/otm/studentListByTutor",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                university: {
                label: "Universitet kodi",
                type: "text",
                defaultNew: "999",
                defaultOld: "999",
                required: true
                },
                tutorPinfl: {
                label: "Tutor PINFL",
                type: "text",
                defaultNew: "31503776560016",
                defaultOld: "31503776560016",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.university) params.push("university=" + encodeURIComponent(fields.university));
                if (fields.tutorPinfl) params.push("tutorPinfl=" + encodeURIComponent(fields.tutorPinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Tutor talabalari ro'yxati**
                <b>Endpoint:</b> GET /services/otm/studentListByTutor
                <b>Parametrlar:</b>
                - university: Universitet kodi (masalan: 999)
                - tutorPinfl: Tutor PINFL raqami
                <b>Eslatma:</b> OLD-HEMIS formatida university va tutorPinfl string parameter sifatida yuboriladi
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "university": "999",
                "tutorPinfl": "31503776560016",
                "students": []
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 338,
                category: "64.OTM",
                name: "Talaba ma'lumotlari (PINFL bo'yicha)",
                method: "GET",
                url: "/services/otm/studentInfoByPinfl",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "Talaba PINFL",
                type: "text",
                defaultNew: "31503776560016",
                defaultOld: "31503776560016",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Talaba ma'lumotlari (PINFL bo'yicha)**
                <b>Endpoint:</b> GET /services/otm/studentInfoByPinfl
                <b>Parametrlar:</b>
                - pinfl: Talaba PINFL raqami
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "31503776560016",
                "fullName": "...",
                "faculty": "...",
                "speciality": "...",
                "course": 1
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 65.Xo'jalik hisobot (3 endpoint)
            // ============================================
            {
                id: 339,
                category: "65.Xo'jalik hisobot",
                name: "O'quv materiallari darajasi",
                method: "POST",
                url: "/entities/hemishe_REducationMaterials",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                universityCode: {
                label: "Universitet kodi",
                type: "text",
                defaultNew: "999",
                defaultOld: "999",
                required: true
                },
                educationYear: {
                label: "O'quv yili ID",
                type: "text",
                defaultNew: "2023",
                defaultOld: "2023",
                required: true
                },
                specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                defaultNew: "60310900",
                defaultOld: "60310900",
                required: true
                },
                specialityName: {
                label: "Mutaxassislik nomi",
                type: "text",
                defaultNew: "Psixologiya (faoliyat turlari bo'yicha)",
                defaultOld: "Psixologiya (faoliyat turlari bo'yicha)",
                required: true
                },
                subjectCount: {
                label: "Fanlar soni",
                type: "text",
                defaultNew: "15",
                defaultOld: "15",
                required: true
                },
                textbooksCount: {
                label: "Darsliklar soni",
                type: "text",
                defaultNew: "5",
                defaultOld: "5",
                required: true
                },
                createdMaterialsGrade: {
                label: "Yaratilgan materiallar darajasi",
                type: "text",
                defaultNew: "4",
                defaultOld: "4",
                required: true
                }
                },
                urlBuilder: function(fields) {
                return this.url;
                },
                bodyBuilder: function(fields) {
                return JSON.stringify({
                university: { code: fields.universityCode },
                educationYear: { id: fields.educationYear },
                specialityId: "11111111-1111-1111-1111-111111111111",
                specialityCode: fields.specialityCode,
                specialityName: fields.specialityName,
                subjectCount: parseInt(fields.subjectCount) || 15,
                textbooksCount: parseInt(fields.textbooksCount) || 5,
                createdMaterialsGrade: parseInt(fields.createdMaterialsGrade) || 4
                }, null, 2);
                },
                description: `**O'quv materiallari darajasi**
                <b>Endpoint:</b> POST /entities/hemishe_REducationMaterials
                <b>So'rov formati:</b>
                <pre>
                {
                "university": { "code": "999" },
                "educationYear": { "id": "2023" },
                "specialityId": "uuid",
                "specialityCode": "60310900",
                "specialityName": "Psixologiya (faoliyat turlari bo'yicha)",
                "subjectCount": 15,
                "textbooksCount": 5,
                "createdMaterialsGrade": 4
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "_entityName": "hemishe_REducationMaterials",
                "_instanceName": "com.company.hemishe.entity.REducationMaterials-...",
                "id": "generated-uuid"
                }
                </pre>`,
                ported: true
                }
            {
                id: 340,
                category: "65.Xo'jalik hisobot",
                name: "Laboratoriyalar bilan ta'minlanganlik",
                method: "POST",
                url: "/entities/hemishe_RLaboratories",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                universityCode: {
                label: "Universitet kodi",
                type: "text",
                defaultNew: "999",
                defaultOld: "999",
                required: true
                },
                educationYear: {
                label: "O'quv yili ID",
                type: "text",
                defaultNew: "2023",
                defaultOld: "2023",
                required: true
                },
                specialityCode: {
                label: "Mutaxassislik kodi",
                type: "text",
                defaultNew: "60310900",
                defaultOld: "60310900",
                required: true
                },
                labCount: {
                label: "Laboratoriyalar soni",
                type: "text",
                defaultNew: "10",
                defaultOld: "10",
                required: true
                },
                equipmentLevel: {
                label: "Jihozlanganlik darajasi",
                type: "text",
                defaultNew: "4",
                defaultOld: "4",
                required: true
                }
                },
                urlBuilder: function(fields) {
                return this.url;
                },
                bodyBuilder: function(fields) {
                return JSON.stringify({
                university: { code: fields.universityCode },
                educationYear: { id: fields.educationYear },
                specialityCode: fields.specialityCode,
                labCount: parseInt(fields.labCount) || 10,
                equipmentLevel: parseInt(fields.equipmentLevel) || 4
                }, null, 2);
                },
                description: `**Laboratoriyalar bilan ta'minlanganlik**
                <b>Endpoint:</b> POST /entities/hemishe_RLaboratories
                <b>So'rov formati:</b>
                <pre>
                {
                "university": { "code": "999" },
                "educationYear": { "id": "2023" },
                "specialityCode": "60310900",
                "labCount": 10,
                "equipmentLevel": 4
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "_entityName": "hemishe_RLaboratories",
                "_instanceName": "com.company.hemishe.entity.RLaboratories-...",
                "id": "generated-uuid"
                }
                </pre>`,
                ported: true
                }
            {
                id: 341,
                category: "65.Xo'jalik hisobot",
                name: "AKT bilan jihozlanganlik",
                method: "POST",
                url: "/entities/hemishe_RIctEquipment",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                universityCode: {
                label: "Universitet kodi",
                type: "text",
                defaultNew: "999",
                defaultOld: "999",
                required: true
                },
                educationYear: {
                label: "O'quv yili ID",
                type: "text",
                defaultNew: "2023",
                defaultOld: "2023",
                required: true
                },
                computerCount: {
                label: "Kompyuterlar soni",
                type: "text",
                defaultNew: "100",
                defaultOld: "100",
                required: true
                },
                projectorCount: {
                label: "Proyektorlar soni",
                type: "text",
                defaultNew: "20",
                defaultOld: "20",
                required: true
                },
                internetSpeed: {
                label: "Internet tezligi (Mbps)",
                type: "text",
                defaultNew: "100",
                defaultOld: "100",
                required: true
                }
                },
                urlBuilder: function(fields) {
                return this.url;
                },
                bodyBuilder: function(fields) {
                return JSON.stringify({
                university: { code: fields.universityCode },
                educationYear: { id: fields.educationYear },
                computerCount: parseInt(fields.computerCount) || 100,
                projectorCount: parseInt(fields.projectorCount) || 20,
                internetSpeed: parseInt(fields.internetSpeed) || 100
                }, null, 2);
                },
                description: `**AKT bilan jihozlanganlik**
                <b>Endpoint:</b> POST /entities/hemishe_RIctEquipment
                <b>So'rov formati:</b>
                <pre>
                {
                "university": { "code": "999" },
                "educationYear": { "id": "2023" },
                "computerCount": 100,
                "projectorCount": 20,
                "internetSpeed": 100
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "_entityName": "hemishe_RIctEquipment",
                "_instanceName": "com.company.hemishe.entity.RIctEquipment-...",
                "id": "generated-uuid"
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 66.BIMM (5 endpoint)
            // ============================================
            {
                id: 342,
                category: "66.BIMM",
                name: "Nogironlik holatini tekshirish",
                method: "GET",
                url: "/services/bimm/disabilityCheck",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "12345678901234",
                required: true
                },
                document: {
                label: "Hujjat raqami",
                type: "text",
                defaultNew: "AB1234567",
                defaultOld: "AB1234567",
                required: false
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                if (fields.document) params.push("document=" + encodeURIComponent(fields.document));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Nogironlik holatini tekshirish (BIMM)**
                <b>Endpoint:</b> GET /services/bimm/disabilityCheck
                <b>Parametrlar:</b>
                - pinfl: Fuqaro PINFL raqami (required)
                - document: Hujjat raqami (optional)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "12345678901234",
                "document": "AB1234567",
                "hasDisability": false,
                "message": "Stub implementation - parameters returned"
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 343,
                category: "66.BIMM",
                name: "Kam ta'minlangan oilalar ro'yxati",
                method: "GET",
                url: "/services/bimm/provertyRegister",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "12345678901234",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Kam ta'minlangan oilalar ro'yxatini tekshirish (BIMM)**
                <b>Endpoint:</b> GET /services/bimm/provertyRegister
                <b>Parametrlar:</b>
                - pinfl: Fuqaro PINFL raqami (required)
                <b>Eslatma:</b> Endpoint nomida "proverty" OLD-HEMIS formatiga moslik uchun saqlab qolindi (to'g'ri yozilishi: "poverty")
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "12345678901234",
                "inRegister": false,
                "message": "Stub implementation - parameters returned"
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 344,
                category: "66.BIMM",
                name: "Sertifikat ma'lumotlari",
                method: "GET",
                url: "/services/bimm/certificate",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "12345678901234",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Sertifikat ma'lumotlarini olish (BIMM)**
                <b>Endpoint:</b> GET /services/bimm/certificate
                <b>Parametrlar:</b>
                - pinfl: Fuqaro PINFL raqami (required)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "12345678901234",
                "certificates": [],
                "message": "Stub implementation - parameters returned"
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 345,
                category: "66.BIMM",
                name: "Ilmiy daraja ma'lumotlari",
                method: "GET",
                url: "/services/bimm/academicDegree",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "12345678901234",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Ilmiy daraja ma'lumotlarini olish (BIMM)**
                <b>Endpoint:</b> GET /services/bimm/academicDegree
                <b>Parametrlar:</b>
                - pinfl: Fuqaro PINFL raqami (required)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "12345678901234",
                "degrees": [],
                "message": "Stub implementation - parameters returned"
                }
                }
                </pre>`,
                ported: true
                }
            {
                id: 346,
                category: "66.BIMM",
                name: "O'qituvchi malaka oshirish",
                method: "GET",
                url: "/services/bimm/teacherTraining",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "PINFL",
                type: "text",
                defaultNew: "12345678901234",
                defaultOld: "12345678901234",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**O'qituvchi malaka oshirish ma'lumotlari (BIMM)**
                <b>Endpoint:</b> GET /services/bimm/teacherTraining
                <b>Parametrlar:</b>
                - pinfl: O'qituvchi PINFL raqami (required)
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "success": true,
                "data": {
                "pinfl": "12345678901234",
                "trainings": [],
                "message": "Stub implementation - parameters returned"
                }
                }
                </pre>`,
                ported: true
                }

            // ============================================
            // 67.OTM Config (1 endpoint)
            // ============================================
            {
                id: 347,
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

            // ============================================
            // 68.Sertifikat (2 endpoint)
            // ============================================
            {
                id: 348,
                category: "68.Sertifikat",
                name: "Talaba sertifikati yaratish",
                method: "POST",
                url: "/entities/hemishe_EStudentCertificate",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                universityId: {
                label: "Universitet ID",
                type: "text",
                defaultNew: "11111111-1111-1111-1111-111111111111",
                defaultOld: "11111111-1111-1111-1111-111111111111",
                required: true
                },
                studentId: {
                label: "Talaba ID",
                type: "text",
                defaultNew: "22222222-2222-2222-2222-222222222222",
                defaultOld: "22222222-2222-2222-2222-222222222222",
                required: true
                },
                certificateType: {
                label: "Sertifikat turi (code)",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: true
                },
                serialNumber: {
                label: "Seriya raqami",
                type: "text",
                defaultNew: "AA1234567",
                defaultOld: "AA1234567",
                required: true
                },
                issueDate: {
                label: "Berilgan sana",
                type: "text",
                defaultNew: "2025-01-01",
                defaultOld: "2025-01-01",
                required: true
                },
                validDate: {
                label: "Amal qilish sanasi",
                type: "text",
                defaultNew: "2026-01-01",
                defaultOld: "2026-01-01",
                required: true
                }
                },
                urlBuilder: function(fields) {
                return this.url;
                },
                bodyBuilder: function(fields) {
                return JSON.stringify({
                university: fields.universityId,
                student: fields.studentId,
                certificateType: fields.certificateType,
                serialNumber: fields.serialNumber,
                issueDate: fields.issueDate,
                validDate: fields.validDate,
                active: true
                }, null, 2);
                },
                description: `**Talaba sertifikati yaratish**
                <b>Endpoint:</b> POST /entities/hemishe_EStudentCertificate
                <b>So'rov formati:</b>
                <pre>
                {
                "university": "uuid",
                "student": "uuid",
                "certificateType": "uuid",
                "serialNumber": "AA1234567",
                "issueDate": "2025-01-01",
                "validDate": "2026-01-01",
                "active": true
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "id": "generated-uuid",
                "_entityName": "hemishe_EStudentCertificate",
                "_instanceName": "AA1234567",
                ...
                }
                </pre>`,
                ported: true
                }
            {
                id: 349,
                category: "68.Sertifikat",
                name: "Xodim sertifikati yaratish",
                method: "POST",
                url: "/entities/hemishe_EEmpoyeeCertificate",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                universityCode: {
                label: "Universitet kodi",
                type: "text",
                defaultNew: "999",
                defaultOld: "999",
                required: true
                },
                employeeId: {
                label: "Xodim ID",
                type: "text",
                defaultNew: "f41a4336-a4f5-154d-cc28-eff76884fa99",
                defaultOld: "f41a4336-a4f5-154d-cc28-eff76884fa99",
                required: true
                },
                certificateTypeCode: {
                label: "Sertifikat turi (code)",
                type: "text",
                defaultNew: "2",
                defaultOld: "2",
                required: true
                },
                certificateNameCode: {
                label: "Sertifikat nomi (code)",
                type: "text",
                defaultNew: "30",
                defaultOld: "30",
                required: true
                },
                certificateGradeCode: {
                label: "Sertifikat darajasi (code)",
                type: "text",
                defaultNew: "2",
                defaultOld: "2",
                required: true
                },
                serialNumber: {
                label: "Seriya raqami",
                type: "text",
                defaultNew: "AA1112244",
                defaultOld: "AA1112244",
                required: true
                },
                issueDate: {
                label: "Berilgan sana",
                type: "text",
                defaultNew: "2025-03-05",
                defaultOld: "2025-03-05",
                required: true
                },
                validDate: {
                label: "Amal qilish sanasi",
                type: "text",
                defaultNew: "2026-03-11",
                defaultOld: "2026-03-11",
                required: true
                }
                },
                urlBuilder: function(fields) {
                return this.url;
                },
                bodyBuilder: function(fields) {
                // OLD-HEMIS format: ARRAY of objects with nested code/id references
                return JSON.stringify([{
                university: { code: fields.universityCode },
                employee: { id: fields.employeeId },
                certificateType: { code: fields.certificateTypeCode },
                certificateName: { code: fields.certificateNameCode },
                certificateGrade: { code: fields.certificateGradeCode },
                certificateSubject: { code: "1" },
                issueDate: fields.issueDate,
                validDate: fields.validDate,
                serialNumber: fields.serialNumber,
                active: true
                }], null, 2);
                },
                description: `**Xodim sertifikati yaratish**
                <b>Endpoint:</b> POST /entities/hemishe_EEmpoyeeCertificate
                <b>Eslatma:</b> Endpoint nomida "EEmpoyeeCertificate" yozuvi OLD-HEMIS formatiga moslik uchun saqlab qolindi (typo)
                <b>So'rov formati (ARRAY!):</b>
                <pre>
                [
                {
                "university": { "code": "999" },
                "employee": { "id": "f41a4336-a4f5-154d-cc28-eff76884fa99" },
                "certificateType": { "code": "2" },
                "certificateName": { "code": "30" },
                "certificateGrade": { "code": "2" },
                "certificateSubject": { "code": "1" },
                "issueDate": "2025-03-05",
                "validDate": "2026-03-11",
                "serialNumber": "AA1112244",
                "active": true
                }
                ]
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                [
                {
                "_entityName": "hemishe_EEmpoyeeCertificate",
                "_instanceName": "AA1112244",
                "id": "generated-uuid"
                }
                ]
                </pre>`,
                ported: true
                }

            // ============================================
            // 69.Amaliyot (1 endpoint)
            // ============================================
            {
                id: 350,
                category: "69.Amaliyot",
                name: "Amaliyot yaratish",
                method: "POST",
                url: "/entities/hemishe_EStudentPractice",
                requiresAuth: true,
                hasBody: true,
                inputFields: {
                universityCode: {
                label: "Universitet kodi",
                type: "text",
                defaultNew: "999",
                defaultOld: "999",
                required: true
                },
                studentId: {
                label: "Talaba ID",
                type: "text",
                defaultNew: "11111111-1111-1111-1111-111111111111",
                defaultOld: "11111111-1111-1111-1111-111111111111",
                required: true
                },
                practiceType: {
                label: "Amaliyot turi (code)",
                type: "text",
                defaultNew: "1",
                defaultOld: "1",
                required: true
                },
                startDate: {
                label: "Boshlanish sanasi",
                type: "text",
                defaultNew: "2025-01-15",
                defaultOld: "2025-01-15",
                required: true
                },
                endDate: {
                label: "Tugash sanasi",
                type: "text",
                defaultNew: "2025-02-15",
                defaultOld: "2025-02-15",
                required: true
                },
                organization: {
                label: "Tashkilot nomi",
                type: "text",
                defaultNew: "Test Tashkilot",
                defaultOld: "Test Tashkilot",
                required: true
                }
                },
                urlBuilder: function(fields) {
                return this.url;
                },
                bodyBuilder: function(fields) {
                return JSON.stringify({
                university: { code: fields.universityCode },
                student: { id: fields.studentId },
                practiceType: { code: fields.practiceType },
                startDate: fields.startDate,
                endDate: fields.endDate,
                organization: fields.organization,
                active: true
                }, null, 2);
                },
                description: `**Amaliyot yaratish**
                <b>Endpoint:</b> POST /entities/hemishe_EStudentPractice
                <b>Eslatma:</b> Bu endpoint old_hemis.json da topilmagan.
                Ammo OLD-HEMIS tizimida mavjud bo'lgan pattern asosida qo'shildi.
                <b>So'rov formati:</b>
                <pre>
                {
                "university": { "code": "999" },
                "student": { "id": "uuid" },
                "practiceType": { "code": "1" },
                "startDate": "2025-01-15",
                "endDate": "2025-02-15",
                "organization": "Test Tashkilot",
                "active": true
                }
                </pre>
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "_entityName": "hemishe_EStudentPractice",
                "_instanceName": "...",
                "id": "generated-uuid"
                }
                </pre>`,
                ported: false
                }

            // ============================================
            // 70.Qo'shimcha xizmatlar (4 endpoint)
            // ============================================
            {
                id: 351,
                category: "70.Qo'shimcha xizmatlar",
                name: "Hokimiyat klassifikatorlari",
                method: "GET",
                url: "/services/classifiers/hokimiyat",
                requiresAuth: true,
                hasBody: false,
                inputFields: {},
                urlBuilder: function(fields) {
                return this.url;
                },
                description: `**Hokimiyat klassifikatorlari**
                <b>Endpoint:</b> GET /services/classifiers/hokimiyat
                <b>Parametrlar:</b> Yo'q
                <b>Tavsif:</b> Viloyat va tumanlar ierarxik strukturasini qaytaradi.
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "regions": [
                {
                "code": "1703",
                "name": "Toshkent shahri",
                "districts": [
                {
                "code": "1703201",
                "name": "Bektemir tumani"
                },
                ...
                ]
                },
                ...
                ]
                }
                </pre>`,
                ported: true
                }
            {
                id: 352,
                category: "70.Qo'shimcha xizmatlar",
                name: "Diplom tekshirish (hash bo'yicha)",
                method: "GET",
                url: "/services/diploma/byhash",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                hash: {
                label: "Diplom hash (QR koddan)",
                type: "text",
                defaultNew: "71d6a9e0436cfb3aaa9fee3f88844b42",
                defaultOld: "71d6a9e0436cfb3aaa9fee3f88844b42",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.hash) params.push("hash=" + encodeURIComponent(fields.hash));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Diplom tekshirish (hash bo'yicha)**
                <b>Endpoint:</b> GET /services/diploma/byhash
                <b>Parametrlar:</b>
                - hash: Diplom QR kodidan olingan hash
                <b>Tavsif:</b> Ish beruvchilar va davlat organlari tomonidan diplom haqiqiyligini tekshirish uchun ishlatiladi.
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "id": "uuid",
                "diplomaNumber": "12345678",
                "serialNumber": "AB",
                "student": {...},
                "university": {...},
                "specialty": {...},
                "diplomaType": {...},
                "issueDate": "2024-06-15",
                "diplomaHash": "71d6a9e0436cfb3aaa9fee3f88844b42",
                "status": "ACTIVE",
                "verified": true,
                "verificationDate": "2025-01-24T..."
                }
                </pre>`,
                ported: true
                }
            {
                id: 353,
                category: "70.Qo'shimcha xizmatlar",
                name: "Talaba olish (PINFL bo'yicha)",
                method: "GET",
                url: "/services/student/get",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "Talaba PINFL",
                type: "text",
                defaultNew: "61009076610088",
                defaultOld: "61009076610088",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Talaba olish (PINFL bo'yicha)**
                <b>Endpoint:</b> GET /services/student/get
                <b>Parametrlar:</b>
                - pinfl: Talaba PINFL raqami
                <b>Tavsif:</b> PINFL raqami bo'yicha talaba ma'lumotlarini olish.
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "id": "uuid",
                "pinfl": "61009076610088",
                "firstName": "...",
                "lastName": "...",
                "middleName": "...",
                "university": {...},
                "faculty": {...},
                "specialty": {...},
                "course": 1,
                "group": {...},
                "status": {...}
                }
                </pre>`,
                ported: true
                }
            {
                id: 354,
                category: "70.Qo'shimcha xizmatlar",
                name: "Faol talaba olish (PINFL bo'yicha)",
                method: "GET",
                url: "/services/student/getActive",
                requiresAuth: true,
                hasBody: false,
                inputFields: {
                pinfl: {
                label: "Talaba PINFL",
                type: "text",
                defaultNew: "52305046740024",
                defaultOld: "52305046740024",
                required: true
                }
                },
                urlBuilder: function(fields) {
                let params = [];
                if (fields.pinfl) params.push("pinfl=" + encodeURIComponent(fields.pinfl));
                return this.url + (params.length > 0 ? "?" + params.join("&") : "");
                },
                description: `**Faol talaba olish (PINFL bo'yicha)**
                <b>Endpoint:</b> GET /services/student/getActive
                <b>Parametrlar:</b>
                - pinfl: Talaba PINFL raqami
                <b>Tavsif:</b> Faqat faol holatdagi (o'qiyotgan, bitirib ketmagan/chiqarib yuborilmagan) talaba ma'lumotlarini qaytaradi.
                <b>OLD-HEMIS Response formati:</b>
                <pre>
                {
                "id": "uuid",
                "pinfl": "52305046740024",
                "firstName": "...",
                "lastName": "...",
                "middleName": "...",
                "university": {...},
                "faculty": {...},
                "specialty": {...},
                "course": 1,
                "group": {...},
                "status": "ACTIVE"
                }
                </pre>
                <b>Eslatma:</b> Agar talaba faol bo'lmasa (bitirib ketgan, chiqarib yuborilgan), null qaytariladi.`,
                ported: true
                }
        ];
