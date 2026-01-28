// DataBootstrap — hemis-back API dan test uchun ma'lumotlar yuklash
// Token olgandan keyin entity list endpointlardan random data oladi

class DataBootstrap {
    /**
     * @param {string} baseUrl - hemis-back URL (masalan: http://localhost:8081)
     * @param {string} token - Bearer access token
     */
    constructor(baseUrl, token) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.log = [];
    }

    /**
     * Barcha kerakli ma'lumotlarni parallel yuklash
     * @returns {Object} testData
     */
    async loadAll() {
        this.log = [];
        this.addLog('Bootstrap boshlandi...');

        const fetches = [
            // Asosiy entitylar
            { key: 'students', url: '/app/rest/v2/entities/hemishe_EStudent', params: { limit: 5, view: 'eStudent-view' } },
            { key: 'teachers', url: '/app/rest/v2/entities/hemishe_ETeacher', params: { limit: 3, view: 'eTeacher-view' } },
            { key: 'departments', url: '/app/rest/v2/entities/hemishe_EUniversityDepartment', params: { limit: 5, view: '_local' } },
            { key: 'groups', url: '/app/rest/v2/entities/hemishe_EUniversityGroup', params: { limit: 3, view: '_local' } },
            { key: 'specialties', url: '/app/rest/v2/entities/hemishe_EUniversitySpeciality', params: { limit: 3, view: '_local' } },
            { key: 'universities', url: '/app/rest/v2/entities/hemishe_EUniversity', params: { limit: 2, view: '_local' } },
            // Diplom va sertifikat
            { key: 'diplomas', url: '/app/rest/v2/entities/hemishe_EStudentDiploma', params: { limit: 2, view: '_local' } },
            { key: 'gpa', url: '/app/rest/v2/entities/hemishe_EStudentGpa', params: { limit: 2, view: '_local' } },
            { key: 'certificates', url: '/app/rest/v2/entities/hemishe_EStudentCertificate', params: { limit: 2, view: '_local' } },
            { key: 'empCertificates', url: '/app/rest/v2/entities/hemishe_EEmpoyeeCertificate', params: { limit: 2, view: '_local' } },
            // Xodim lavozim
            { key: 'employeeJobs', url: '/app/rest/v2/entities/hemishe_EEmployeeJobs', params: { limit: 2, view: 'eEmployeeJobs-view' } },
            // Ilmiy faoliyat
            { key: 'doctoralStudents', url: '/app/rest/v2/entities/hemishe_EDoctorateStudent', params: { limit: 2, view: '_local' } },
            { key: 'dissertationDefenses', url: '/app/rest/v2/entities/hemishe_EDissertationDefense', params: { limit: 2, view: '_local' } },
            { key: 'projects', url: '/app/rest/v2/entities/hemishe_EProject', params: { limit: 2, view: '_local' } },
            { key: 'projectMetas', url: '/app/rest/v2/entities/hemishe_EProjectMeta', params: { limit: 2, view: '_local' } },
            { key: 'projectExecutors', url: '/app/rest/v2/entities/hemishe_EProjectExecutor', params: { limit: 2, view: '_local' } },
            // Nashrlar (3 turi)
            { key: 'publicationsScientific', url: '/app/rest/v2/entities/hemishe_EPublicationScientific', params: { limit: 2, view: '_local' } },
            { key: 'publicationsMethodical', url: '/app/rest/v2/entities/hemishe_EPublicationMethodical', params: { limit: 2, view: '_local' } },
            { key: 'publicationsProperty', url: '/app/rest/v2/entities/hemishe_EPublicationProperty', params: { limit: 2, view: '_local' } },
            { key: 'publicationAuthorMetas', url: '/app/rest/v2/entities/hemishe_EPublicationAuthorMeta', params: { limit: 2, view: '_local' } },
            { key: 'researchActivities', url: '/app/rest/v2/entities/hemishe_EResearchActivity', params: { limit: 2, view: '_local' } },
            // Admin entitylar — Student
            { key: 'adminStudent2', url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent2', params: { limit: 2, view: '_local' } },
            { key: 'adminStudent3', url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent3', params: { limit: 2, view: '_local' } },
            { key: 'adminStudent4', url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudent4', params: { limit: 2, view: '_local' } },
            { key: 'adminStudentSport', url: '/app/rest/v2/entities/hemishe_RIAdministrativeStudentSport', params: { limit: 2, view: '_local' } },
            // Admin entitylar — Employee
            { key: 'adminEmployee1', url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1', params: { limit: 2, view: '_local' } },
            { key: 'adminEmployee2', url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2', params: { limit: 2, view: '_local' } },
            { key: 'adminEmployee3', url: '/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3', params: { limit: 2, view: '_local' } },
            // Statistik entitylar (GenericStat)
            { key: 'ictEquipment', url: '/app/rest/v2/entities/hemishe_RIctEquipment', params: { limit: 2, view: '_local' } },
            { key: 'laboratories', url: '/app/rest/v2/entities/hemishe_RLaboratories', params: { limit: 2, view: '_local' } },
            { key: 'educationMaterials', url: '/app/rest/v2/entities/hemishe_REducationMaterials', params: { limit: 2, view: '_local' } },
        ];

        const results = await Promise.allSettled(
            fetches.map(f => this.fetchList(f.url, f.params).then(data => ({ key: f.key, data })))
        );

        const testData = {};
        for (const result of results) {
            if (result.status === 'fulfilled') {
                testData[result.value.key] = result.value.data;
                this.addLog(`${result.value.key}: ${result.value.data.length} ta yozuv yuklandi`);
            } else {
                this.addLog(`Xatolik: ${result.reason?.message || 'Noma\'lum'}`);
            }
        }

        // Fallback: bo'sh arraylar
        for (const f of fetches) {
            if (!testData[f.key]) {
                testData[f.key] = [];
                this.addLog(`${f.key}: bo'sh (yuklash xatosi)`);
            }
        }

        // University kodi (birinchi studentdan)
        testData.university = {
            code: this.extractUniversityCode(testData.students)
        };

        this.addLog(`Bootstrap tugadi. University kodi: ${testData.university.code}`);
        return testData;
    }

    /**
     * Bitta entity list endpointni chaqirish
     */
    async fetchList(url, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = this.baseUrl + url + (queryString ? '?' + queryString : '');

        try {
            const response = await fetch(fullUrl, {
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + this.token,
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                this.addLog(`GET ${url} → ${response.status}`);
                return [];
            }

            const data = await response.json();
            return Array.isArray(data) ? data : [];
        } catch (err) {
            this.addLog(`GET ${url} → Xatolik: ${err.message}`);
            return [];
        }
    }

    /**
     * University kodini studentlar yoki boshqa manbadan aniqlash
     */
    extractUniversityCode(students) {
        if (students && students.length > 0) {
            const first = students[0];
            if (first.university && first.university.code) return first.university.code;
            if (first.code) {
                const match = first.code.match(/^(\d{3})/);
                if (match) return match[1];
            }
        }
        return '401'; // default
    }

    addLog(msg) {
        const timestamp = new Date().toLocaleTimeString();
        this.log.push(`[${timestamp}] ${msg}`);
    }

    /**
     * Random element tanlash
     */
    static random(arr) {
        if (!arr || arr.length === 0) return null;
        return arr[Math.floor(Math.random() * arr.length)];
    }

    /**
     * Ma'lumotlar yetarlimi tekshirish
     */
    static checkAvailability(testData) {
        const report = {};
        const keys = ['students', 'teachers', 'departments', 'groups', 'specialties'];
        for (const key of keys) {
            report[key] = {
                available: (testData[key] || []).length > 0,
                count: (testData[key] || []).length
            };
        }
        return report;
    }
}
