#!/usr/bin/env python3
"""
Old-Hemis Proxy Server
CORS muammosini hal qilish uchun proxy server
"""

from http.server import HTTPServer, SimpleHTTPRequestHandler
import json
import urllib.request
import urllib.error
from urllib.parse import urlparse, parse_qs

OLD_HEMIS_BASE_URL = "http://localhost:8082"
OLD_HEMIS_TOKEN = "hIvYdo6xPjRIm1fjJRbOlxphxF4"

class ProxyHTTPRequestHandler(SimpleHTTPRequestHandler):
    def do_GET(self):
        # Agar /proxy/ bilan boshlansa - proxy qil
        if self.path.startswith('/proxy/old-hemis/'):
            self.proxy_old_hemis('GET', None)
        else:
            # Oddiy static file serve qil
            super().do_GET()

    def do_POST(self):
        # Agar /proxy/ bilan boshlansa - proxy qil
        if self.path.startswith('/proxy/old-hemis/'):
            # Read request body
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length) if content_length > 0 else None
            self.proxy_old_hemis('POST', post_data)
        else:
            # Static file server doesn't handle POST
            self.send_error(405, "Method Not Allowed")

    def do_PUT(self):
        # Agar /proxy/ bilan boshlansa - proxy qil
        if self.path.startswith('/proxy/old-hemis/'):
            # Read request body
            content_length = int(self.headers.get('Content-Length', 0))
            put_data = self.rfile.read(content_length) if content_length > 0 else None
            self.proxy_old_hemis('PUT', put_data)
        else:
            self.send_error(405, "Method Not Allowed")

    def do_DELETE(self):
        # Agar /proxy/ bilan boshlansa - proxy qil
        if self.path.startswith('/proxy/old-hemis/'):
            self.proxy_old_hemis('DELETE', None)
        else:
            self.send_error(405, "Method Not Allowed")

    def proxy_old_hemis(self, method, body=None):
        """Old-hemis ga proxy so'rov yuborish (GET, POST, PUT, DELETE)"""
        # URL ni parse qilish: /proxy/old-hemis/app/rest/v2/... -> /app/rest/v2/...
        path = self.path.replace('/proxy/old-hemis', '')
        old_hemis_url = OLD_HEMIS_BASE_URL + path

        print(f"🔄 Proxying {method} to old-hemis: {old_hemis_url}")

        try:
            # Old-hemis ga so'rov
            req = urllib.request.Request(old_hemis_url, method=method)

            # Copy headers from original request (except Host, Content-Length)
            has_auth = False
            for header, value in self.headers.items():
                if header.lower() not in ['host', 'content-length', 'connection']:
                    req.add_header(header, value)
                    if header.lower() == 'authorization':
                        has_auth = True
                        print(f"📋 Using client Authorization: {value[:50]}...")

            # Special handling for OAuth token endpoint - needs Basic auth
            if '/oauth/token' in path:
                # OAuth endpoint requires Basic auth: client:secret -> base64
                import base64
                basic_auth = base64.b64encode(b'client:secret').decode('utf-8')
                req.add_header('Authorization', f'Basic {basic_auth}')
                print(f"🔐 Added Basic auth for OAuth token endpoint")
            elif not has_auth:
                # Add Bearer token for other endpoints only if no auth provided
                req.add_header('Authorization', f'Bearer {OLD_HEMIS_TOKEN}')
                print(f"⚠️ No client auth, using default token")

            # Add body for POST/PUT
            if method in ['POST', 'PUT'] and body:
                req.data = body
                print(f"📦 {method} body: {len(body)} bytes")

            with urllib.request.urlopen(req, timeout=10) as response:
                data = response.read()

                # CORS headerlarini qo'shish
                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.send_header('Access-Control-Allow-Origin', '*')
                self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
                self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
                self.end_headers()

                self.wfile.write(data)
                print(f"✅ Old-hemis response: {len(data)} bytes")

        except urllib.error.HTTPError as e:
            # Read the actual error response from OLD-HEMIS
            error_body = e.read() if e.fp else b'{}'
            print(f"❌ Old-hemis HTTP error: {e.code} {e.reason}")
            print(f"   Response body: {error_body.decode('utf-8', errors='replace')[:200]}")

            # Return actual OLD-HEMIS response (not proxy wrapper)
            self.send_response(e.code)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
            self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
            self.end_headers()
            self.wfile.write(error_body)
        except urllib.error.URLError as e:
            print(f"❌ Old-hemis connection error: {e.reason}")
            self.send_error_response(502, f"Old-hemis ga ulanishda xatolik: {e.reason}")
        except Exception as e:
            print(f"❌ Proxy error: {e}")
            self.send_error_response(500, str(e))

    def send_error_response(self, code, message):
        """Xato javobini yuborish"""
        error_data = {
            "error": "Old-hemis proxy error",
            "message": message,
            "hint": "Old-hemis server ishlab turganini tekshiring (port 8082)"
        }

        response = json.dumps(error_data).encode('utf-8')

        self.send_response(code)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()
        self.wfile.write(response)

    def do_OPTIONS(self):
        """CORS preflight so'rovlar uchun"""
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()

if __name__ == '__main__':
    PORT = 9000
    print(f"""
╔══════════════════════════════════════════════════════════════╗
║  🚀 HEMIS Endpoint Tester - Proxy Server                    ║
╚══════════════════════════════════════════════════════════════╝

📡 Server: http://localhost:{PORT}
📄 HTML:   http://localhost:{PORT}/endpoint_tester.html
🔄 Proxy:  http://localhost:{PORT}/proxy/old-hemis/app/rest/v2/...

✅ Proxy old-hemis ga so'rov yuboradi (CORS muammosini hal qiladi)
✅ Token avtomatik qo'shiladi: {OLD_HEMIS_TOKEN[:20]}...

🛑 To'xtatish: Ctrl+C
    """)

    server = HTTPServer(('0.0.0.0', PORT), ProxyHTTPRequestHandler)

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n\n✅ Server to'xtatildi")
        server.shutdown()
