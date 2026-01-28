#!/usr/bin/env python3
"""
HEMIS Integration Tester — Proxy Server
Port 9001 da ishlaydi, CORS muammosini hal qiladi.
DB Bootstrap: hemis_401 bazasidan test ma'lumotlarini oladi.

Ishlatish:
    cd hemis-back/docs
    python3 integration-proxy.py
    python3 integration-proxy.py http://localhost:8082        # old-hemis
    python3 integration-proxy.py http://localhost:8081 hemis_401 postgres postgres  # DB bilan

Brauzerda:
    http://localhost:9001/integration_tester.html
"""

from http.server import HTTPServer, SimpleHTTPRequestHandler
import gzip
import json
import sys
import urllib.request
import urllib.error
from urllib.parse import urlparse, parse_qs
from datetime import date, datetime
from decimal import Decimal

# ─── KONFIGURATSIYA ─────────────────────────────────────
HEMIS_BACK_URL = sys.argv[1].rstrip('/') if len(sys.argv) > 1 else "http://localhost:8081"
PORT = 9001

# Default DB sozlamalari (argument bilan yoki /db/bootstrap?... bilan o'zgartiriladi)
DEFAULT_DB = {
    'host': 'localhost',
    'port': 5432,
    'dbname': sys.argv[2] if len(sys.argv) > 2 else 'hemis_401',
    'user': sys.argv[3] if len(sys.argv) > 3 else 'postgres',
    'password': sys.argv[4] if len(sys.argv) > 4 else 'postgres',
}


# ─── JSON SERIALIZER ────────────────────────────────────
class CustomEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, (date, datetime)):
            return obj.isoformat()
        if isinstance(obj, Decimal):
            return float(obj)
        if isinstance(obj, bytes):
            return obj.decode('utf-8', errors='replace')
        return super().default(obj)


# ─── DB BOOTSTRAP ────────────────────────────────────────
def db_bootstrap(db_config):
    """hemis_401 bazasidan test ma'lumotlarini olish"""
    try:
        import psycopg2
        import psycopg2.extras
    except ImportError:
        return {"error": "psycopg2 o'rnatilmagan. pip3 install psycopg2-binary"}

    try:
        conn = psycopg2.connect(**db_config)
        cur = conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor)
        result = {}

        # ─── STUDENTS ───
        # Faqat noyob PINFL ga ega talabalarni olish
        # (dublikat PINFL lar backend da IncorrectResultSizeDataAccessException chiqaradi)
        cur.execute("""
            SELECT s._uid AS id, s.first_name, s.second_name, s.third_name,
                   s.passport_pin AS pinfl, s.passport_number AS "serialNumber",
                   s.birth_date AS birthday, s._gender AS gender_code,
                   s._citizenship AS citizenship_code, s.year_of_enter,
                   s.student_id_number AS "studentIdNumber",
                   s.phone, s.email
            FROM e_student s
            WHERE s.active = true
              AND s.passport_pin IS NOT NULL AND s.passport_pin != ''
              AND s.passport_pin NOT IN (
                  SELECT ss.passport_pin FROM e_student ss
                  WHERE ss.active = true AND ss.passport_pin IS NOT NULL
                  GROUP BY ss.passport_pin HAVING COUNT(*) > 1
              )
              AND s.passport_pin NOT IN (
                  SELECT s2.passport_pin FROM e_student s2
                  JOIN e_student_meta m ON m._student = s2.id
                  WHERE s2.active = true AND m.active = true
                    AND m._student_status IN ('10','11','12','13','14','15')
                    AND s2.passport_pin IS NOT NULL
                  GROUP BY s2.passport_pin HAVING COUNT(m.id) > 1
              )
            ORDER BY s.id DESC LIMIT 5
        """)
        students = cur.fetchall()
        # Ma'lumotlarni PHP format ga moslashtirish
        for s in students:
            s['firstName'] = s.pop('first_name', '')
            s['lastName'] = s.pop('second_name', '')
            s['fatherName'] = s.pop('third_name', '')
            s['gender'] = {'code': s.pop('gender_code', '11')}
            s['citizenship'] = {'code': s.pop('citizenship_code', '120')}
            s['educationYear'] = {'code': str(s.pop('year_of_enter', 2020))}
        result['students'] = students

        # ─── UNIVERSITY ───
        cur.execute("SELECT _qid AS id, code, tin, name, address FROM e_university LIMIT 1")
        unis = cur.fetchall()
        result['universities'] = unis
        result['university'] = {'code': unis[0]['code'] if unis else '401',
                                'tin': unis[0].get('tin', '') if unis else ''}

        # ─── EMPLOYEES (teachers) ───
        cur.execute("""
            SELECT _uid AS id, first_name, second_name, third_name,
                   passport_pin AS pinfl, passport_number AS "serialNumber",
                   birth_date AS birthday, _gender AS gender_code,
                   _citizenship AS citizenship_code, year_of_enter,
                   employee_id_number AS "employeeIdNumber",
                   telephone AS phone, email
            FROM e_employee
            WHERE active = true AND passport_pin IS NOT NULL AND passport_pin != ''
            ORDER BY id DESC LIMIT 3
        """)
        teachers = cur.fetchall()
        for t in teachers:
            t['firstName'] = t.pop('first_name', '')
            t['lastName'] = t.pop('second_name', '')
            t['fatherName'] = t.pop('third_name', '')
            t['gender'] = {'code': t.pop('gender_code', '11')}
            t['citizenship'] = {'code': t.pop('citizenship_code', '120')}
            t['educationYear'] = {'code': str(t.pop('year_of_enter', 2020))}
        result['teachers'] = teachers

        # ─── DEPARTMENTS ───
        cur.execute("""
            SELECT _qid AS id, code, name AS "nameUz", _structure_type AS "structureType",
                   _type AS "deparmentType"
            FROM e_department WHERE active = true
            ORDER BY id LIMIT 5
        """)
        result['departments'] = cur.fetchall()

        # ─── GROUPS ───
        cur.execute("""
            SELECT _uid AS id, _uid AS "groupId", name AS "groupName",
                   _education_type AS "educationType",
                   _education_form AS "educationForm"
            FROM e_group WHERE active = true
            ORDER BY id DESC LIMIT 3
        """)
        result['groups'] = cur.fetchall()

        # ─── EDUCATION YEARS ───
        cur.execute("SELECT code, name FROM e_education_year WHERE active = true ORDER BY code DESC LIMIT 3")
        result['educationYears'] = cur.fetchall()

        # ─── STUDENT GPA ───
        cur.execute("""
            SELECT g._uid AS id, es._uid AS student_id, g._education_year AS education_year_code,
                   g._level AS level_code, g.gpa, g.credit_sum AS "creditSum",
                   g.subjects, g.debt_subjects AS "debtSubjects"
            FROM e_student_gpa g
            LEFT JOIN e_student es ON es.id = g._student
            ORDER BY g.id DESC LIMIT 3
        """)
        gpa = cur.fetchall()
        for g in gpa:
            g['studentId'] = {'id': g.pop('student_id', None)}
            g['educationYear'] = {'code': g.pop('education_year_code', '2020')}
            g['level'] = {'code': g.pop('level_code', '10')}
        result['gpa'] = gpa

        # ─── STUDENT DIPLOMAS ───
        cur.execute("""
            SELECT d._uid AS id, es._uid AS student_id, d.diploma_number AS "diplomaNumber",
                   d.register_number AS "registerNumber", d.diploma_status AS status,
                   d.specialty_name AS "specialtyName", d.student_name AS "studentName"
            FROM e_student_diploma d
            LEFT JOIN e_student es ON es.id = d._student
            ORDER BY d.id DESC LIMIT 3
        """)
        diplomas = cur.fetchall()
        for d in diplomas:
            d['student'] = {'id': d.pop('student_id', None)}
        result['diplomas'] = diplomas

        # ─── DIPLOMA BLANKS ───
        cur.execute("""
            SELECT _uid AS id, number, type, category, status, year,
                   number AS "blankNumber"
            FROM e_diploma_blank
            ORDER BY id DESC LIMIT 3
        """)
        result['diplomaBlanks'] = cur.fetchall()

        # ─── STUDENT CERTIFICATES ───
        cur.execute("""
            SELECT c.id, es._uid AS student_id
            FROM e_student_certificate c
            LEFT JOIN e_student es ON es.id = c._student
            ORDER BY c.id DESC LIMIT 2
        """)
        certs = cur.fetchall()
        for c in certs:
            c['student'] = {'id': c.pop('student_id', None)}
        result['certificates'] = certs

        # ─── DOCTORATE STUDENTS ───
        cur.execute("SELECT _uid AS id FROM e_doctorate_student ORDER BY id DESC LIMIT 2")
        result['doctoralStudents'] = cur.fetchall()

        # ─── PROJECTS ───
        cur.execute("SELECT _uid AS id FROM e_project ORDER BY id DESC LIMIT 2")
        result['projects'] = cur.fetchall()
        cur.execute("SELECT _uid AS id FROM e_project_meta ORDER BY id DESC LIMIT 2")
        result['projectMetas'] = cur.fetchall()
        cur.execute("SELECT _uid AS id FROM e_project_executor ORDER BY id DESC LIMIT 2")
        result['projectExecutors'] = cur.fetchall()

        # ─── PUBLICATIONS ───
        cur.execute("SELECT _uid AS id FROM e_publication_scientific ORDER BY id DESC LIMIT 2")
        result['publicationsScientific'] = cur.fetchall()
        cur.execute("SELECT _uid AS id FROM e_publication_methodical ORDER BY id DESC LIMIT 2")
        result['publicationsMethodical'] = cur.fetchall()
        cur.execute("SELECT _uid AS id FROM e_publication_property ORDER BY id DESC LIMIT 2")
        result['publicationsProperty'] = cur.fetchall()
        cur.execute("SELECT _uid AS id FROM e_publication_author_meta ORDER BY id DESC LIMIT 2")
        result['publicationAuthorMetas'] = cur.fetchall()

        # ─── STATISTIKA ───
        result['_meta'] = {
            'source': 'PostgreSQL',
            'database': db_config['dbname'],
            'host': db_config['host'],
            'studentCount': len(students),
            'teacherCount': len(teachers),
            'departmentCount': len(result['departments']),
            'groupCount': len(result['groups']),
            'universityCode': result['university']['code'],
            'universityTin': result['university'].get('tin', ''),
        }

        cur.close()
        conn.close()
        return result

    except Exception as e:
        return {"error": f"DB ulanish xatosi: {str(e)}"}


# ─── HANDLER ─────────────────────────────────────────────
class IntegrationProxyHandler(SimpleHTTPRequestHandler):
    """
    Statik fayllarni xizmat qiladi +
    /app/rest/v2/* so'rovlarini backend ga proxy qiladi (CORS headerlar bilan) +
    /db/bootstrap — PostgreSQL dan test ma'lumotlarini oladi
    """

    def do_GET(self):
        if self.path.startswith('/db/bootstrap'):
            self.handle_db_bootstrap()
        elif self.path.startswith('/app/rest/'):
            self.proxy_to_hemis('GET')
        else:
            super().do_GET()

    def do_POST(self):
        if self.path.startswith('/db/bootstrap'):
            # POST bilan ham qabul qilish (body da DB config)
            content_length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(content_length) if content_length > 0 else b'{}'
            self.handle_db_bootstrap(body)
        elif self.path.startswith('/app/rest/'):
            content_length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(content_length) if content_length > 0 else None
            self.proxy_to_hemis('POST', body)
        else:
            self.send_error(405, "Method Not Allowed")

    def do_PUT(self):
        if self.path.startswith('/app/rest/'):
            content_length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(content_length) if content_length > 0 else None
            self.proxy_to_hemis('PUT', body)
        else:
            self.send_error(405, "Method Not Allowed")

    def do_DELETE(self):
        if self.path.startswith('/app/rest/'):
            self.proxy_to_hemis('DELETE')
        else:
            self.send_error(405, "Method Not Allowed")

    def do_OPTIONS(self):
        """CORS preflight"""
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Accept')
        self.send_header('Access-Control-Max-Age', '3600')
        self.end_headers()

    def handle_db_bootstrap(self, body=None):
        """PostgreSQL dan test ma'lumotlarini olish"""
        # DB config: query params yoki POST body dan olish
        db_config = dict(DEFAULT_DB)

        # Query parametrlardan olish
        parsed = urlparse(self.path)
        params = parse_qs(parsed.query)
        if 'host' in params:
            db_config['host'] = params['host'][0]
        if 'port' in params:
            db_config['port'] = int(params['port'][0])
        if 'dbname' in params:
            db_config['dbname'] = params['dbname'][0]
        if 'user' in params:
            db_config['user'] = params['user'][0]
        if 'password' in params:
            db_config['password'] = params['password'][0]

        # POST body dan olish
        if body:
            try:
                body_config = json.loads(body)
                for key in ['host', 'port', 'dbname', 'user', 'password']:
                    if key in body_config:
                        db_config[key] = body_config[key]
            except Exception:
                pass

        print(f"  [DB] Bootstrap: {db_config['dbname']}@{db_config['host']}:{db_config['port']}")

        data = db_bootstrap(db_config)
        response = json.dumps(data, cls=CustomEncoder, ensure_ascii=False).encode('utf-8')

        self.send_response(200)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Accept')
        self.end_headers()
        self.wfile.write(response)

    def proxy_to_hemis(self, method, body=None):
        """hemis-back ga proxy so'rov"""
        target_url = HEMIS_BACK_URL + self.path
        print(f"  -> {method} {target_url}")

        try:
            req = urllib.request.Request(target_url, method=method)

            # Headerlarni ko'chirish
            for header, value in self.headers.items():
                if header.lower() not in ('host', 'content-length', 'connection', 'accept-encoding'):
                    req.add_header(header, value)

            # Backend ga gzip yubormaslikni aytish
            req.add_header('Accept-Encoding', 'identity')

            if body:
                req.data = body

            with urllib.request.urlopen(req, timeout=60) as response:
                data = response.read()
                content_type = response.headers.get('Content-Type', 'application/json')
                content_encoding = response.headers.get('Content-Encoding', '')

                # Agar gzip bo'lsa — decompress qilish
                if content_encoding == 'gzip' or (data[:2] == b'\x1f\x8b'):
                    try:
                        data = gzip.decompress(data)
                    except Exception:
                        pass

                self.send_response(response.status)
                self.send_header('Content-Type', content_type)
                self.send_header('Access-Control-Allow-Origin', '*')
                self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Accept')
                self.end_headers()
                self.wfile.write(data)

        except urllib.error.HTTPError as e:
            error_body = e.read() if e.fp else b'{}'
            print(f"  <- {e.code} {e.reason}")

            content_encoding = e.headers.get('Content-Encoding', '') if e.headers else ''
            if content_encoding == 'gzip' or (len(error_body) >= 2 and error_body[:2] == b'\x1f\x8b'):
                try:
                    error_body = gzip.decompress(error_body)
                except Exception:
                    pass

            content_type = e.headers.get('Content-Type', 'application/json') if e.headers else 'application/json'
            self.send_response(e.code)
            self.send_header('Content-Type', content_type)
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Accept')
            self.end_headers()
            self.wfile.write(error_body)

        except urllib.error.URLError as e:
            print(f"  <- Connection error: {e.reason}")
            error = json.dumps({"error": f"hemis-back ga ulanib bo'lmadi: {e.reason}"}).encode()
            self.send_response(502)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(error)

        except Exception as e:
            print(f"  <- Error: {e}")
            error = json.dumps({"error": str(e)}).encode()
            self.send_response(500)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(error)

    def log_message(self, format, *args):
        """Chiroyli log"""
        if args and len(args) >= 2:
            status = args[1]
            path = args[0] if args else ''
            if '/app/rest/' in str(path) or '/db/' in str(path):
                return
            icon = '.' if str(status).startswith('2') else 'x'
            print(f"  [{icon}] {' '.join(str(a) for a in args)}")


if __name__ == '__main__':
    is_old = '8081' not in HEMIS_BACK_URL
    backend_label = "old-hemis" if is_old else "hemis-back"
    print(f"""
 HEMIS Integration Tester — Proxy Server
 ========================================
 Server:    http://localhost:{PORT}
 Sahifa:    http://localhost:{PORT}/integration_tester.html
 Backend:   {backend_label} ({HEMIS_BACK_URL})
 Proxy:     /app/rest/v2/* -> {HEMIS_BACK_URL}
 DB:        /db/bootstrap   -> PostgreSQL {DEFAULT_DB['dbname']}@{DEFAULT_DB['host']}

 Ishlatish:
   python3 integration-proxy.py                                          # default
   python3 integration-proxy.py http://localhost:8082                     # old-hemis
   python3 integration-proxy.py http://localhost:8081 hemis_401 postgres postgres  # DB bilan
    """)

    server = HTTPServer(('0.0.0.0', PORT), IntegrationProxyHandler)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nServer to'xtatildi")
        server.shutdown()
