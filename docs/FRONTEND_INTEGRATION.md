# Frontend Integration - hemis-front

**Maqsad:** Frontend (React/Vue/Angular) ni Backend bilan ulash
**Backend:** http://localhost:8081
**Frontend:** http://localhost:3000 (hemis-front)
**Sana:** 2025-11-09

---

## 🎯 Umumiy ma'lumot

### Arxitektura

```
┌─────────────────┐         API Calls         ┌─────────────────┐
│                 │  ──────────────────────>   │                 │
│  HEMIS-FRONT    │         (HTTP/JSON)        │  HEMIS-BACK     │
│  Port: 3000     │  <──────────────────────   │  Port: 8081     │
│                 │         Responses          │                 │
└─────────────────┘                            └─────────────────┘
     React/Vue                                   Spring Boot
```

### Asosiy integration qismlari

1. **Authentication** - Login/Logout
2. **API Calls** - CRUD operations
3. **State Management** - Token, User data
4. **Error Handling** - 401, 403, 500
5. **CORS** - Cross-Origin Resource Sharing

---

## 🔐 Step 1: Authentication

### Backend endpoint

```
POST http://localhost:8081/app/rest/v2/oauth/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic Y2xpZW50OnNlY3JldA==

grant_type=password
username=admin
password=admin
```

### Frontend kod (React example)

#### `src/services/auth.service.js`

```javascript
const API_URL = 'http://localhost:8081/app/rest/v2/oauth';

class AuthService {
  async login(username, password) {
    const response = await fetch(`${API_URL}/token`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Basic Y2xpZW50OnNlY3JldA=='
      },
      body: new URLSearchParams({
        grant_type: 'password',
        username: username,
        password: password
      })
    });

    if (!response.ok) {
      throw new Error('Login failed');
    }

    const data = await response.json();

    // Token ni localStorage ga saqlash
    localStorage.setItem('access_token', data.access_token);
    localStorage.setItem('refresh_token', data.refresh_token);
    localStorage.setItem('token_expires_at', Date.now() + data.expires_in * 1000);

    return data;
  }

  logout() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('token_expires_at');
  }

  getToken() {
    return localStorage.getItem('access_token');
  }

  isAuthenticated() {
    const token = this.getToken();
    const expiresAt = localStorage.getItem('token_expires_at');

    if (!token || !expiresAt) {
      return false;
    }

    return Date.now() < parseInt(expiresAt);
  }
}

export default new AuthService();
```

#### `src/components/Login.jsx`

```javascript
import React, { useState } from 'react';
import authService from '../services/auth.service';
import { useNavigate } from 'react-router-dom';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await authService.login(username, password);
      navigate('/dashboard'); // Login muvaffaqiyatli
    } catch (err) {
      setError('Noto\'g\'ri foydalanuvchi nomi yoki parol');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <form onSubmit={handleSubmit}>
        <h2>HEMIS Login</h2>

        {error && <div className="error">{error}</div>}

        <input
          type="text"
          placeholder="Foydalanuvchi nomi"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          disabled={loading}
        />

        <input
          type="password"
          placeholder="Parol"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          disabled={loading}
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Yuklanmoqda...' : 'Kirish'}
        </button>
      </form>
    </div>
  );
}

export default Login;
```

---

## 📡 Step 2: API Service

### `src/services/api.service.js`

```javascript
import authService from './auth.service';

const API_BASE_URL = 'http://localhost:8081/api';

class ApiService {
  async request(endpoint, options = {}) {
    const token = authService.getToken();

    const config = {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
    };

    // Token qo'shish (agar mavjud bo'lsa)
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

      // 401 Unauthorized - login sahifasiga yo'naltirish
      if (response.status === 401) {
        authService.logout();
        window.location.href = '/login';
        throw new Error('Unauthorized');
      }

      // 403 Forbidden - ruxsat yo'q
      if (response.status === 403) {
        throw new Error('Sizda bu amalni bajarish uchun ruxsat yo'q');
      }

      // 404 Not Found
      if (response.status === 404) {
        throw new Error('Ma\'lumot topilmadi');
      }

      // 500 Server Error
      if (response.status >= 500) {
        throw new Error('Server xatosi. Keyinroq qayta urinib ko\'ring');
      }

      // JSON response
      if (response.headers.get('content-type')?.includes('application/json')) {
        return await response.json();
      }

      return response;
    } catch (error) {
      console.error('API Error:', error);
      throw error;
    }
  }

  // GET request
  get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  }

  // POST request
  post(endpoint, data) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  // PUT request
  put(endpoint, data) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  // DELETE request
  delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }
}

export default new ApiService();
```

---

## 👥 Step 3: User Service Example

### `src/services/user.service.js`

```javascript
import apiService from './api.service';

class UserService {
  // Current user ma'lumotlarini olish
  async getCurrentUser() {
    return apiService.get('/users/me');
  }

  // Barcha foydalanuvchilar (pagination)
  async getAllUsers(page = 0, size = 20) {
    return apiService.get(`/users?page=${page}&size=${size}`);
  }

  // Foydalanuvchi yaratish
  async createUser(userData) {
    return apiService.post('/users', userData);
  }

  // Foydalanuvchini yangilash
  async updateUser(id, userData) {
    return apiService.put(`/users/${id}`, userData);
  }

  // Foydalanuvchini o'chirish
  async deleteUser(id) {
    return apiService.delete(`/users/${id}`);
  }
}

export default new UserService();
```

### Usage in Component

```javascript
import React, { useEffect, useState } from 'react';
import userService from '../services/user.service';

function UserList() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const data = await userService.getAllUsers();
      setUsers(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Yuklanmoqda...</div>;
  if (error) return <div>Xato: {error}</div>;

  return (
    <div>
      <h2>Foydalanuvchilar</h2>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Username</th>
            <th>Email</th>
            <th>Rollar</th>
          </tr>
        </thead>
        <tbody>
          {users.map(user => (
            <tr key={user.id}>
              <td>{user.id}</td>
              <td>{user.username}</td>
              <td>{user.email}</td>
              <td>{user.roles?.join(', ')}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default UserList;
```

---

## 🔄 Step 4: Token Refresh

### Automatic token refresh

```javascript
// src/services/auth.service.js ga qo'shimcha

class AuthService {
  // ... existing code ...

  async refreshToken() {
    const refreshToken = localStorage.getItem('refresh_token');

    if (!refreshToken) {
      throw new Error('No refresh token');
    }

    const response = await fetch(`${API_URL}/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Basic Y2xpZW50OnNlY3JldA=='
      },
      body: new URLSearchParams({
        grant_type: 'refresh_token',
        refresh_token: refreshToken
      })
    });

    if (!response.ok) {
      // Refresh failed - logout
      this.logout();
      window.location.href = '/login';
      throw new Error('Token refresh failed');
    }

    const data = await response.json();

    // Update tokens
    localStorage.setItem('access_token', data.access_token);
    localStorage.setItem('refresh_token', data.refresh_token);
    localStorage.setItem('token_expires_at', Date.now() + data.expires_in * 1000);

    return data.access_token;
  }

  // Check if token is about to expire (5 minutes before)
  shouldRefresh() {
    const expiresAt = localStorage.getItem('token_expires_at');
    if (!expiresAt) return false;

    const expiryTime = parseInt(expiresAt);
    const now = Date.now();
    const fiveMinutes = 5 * 60 * 1000;

    return (expiryTime - now) < fiveMinutes;
  }
}
```

### Axios Interceptor (alternative)

```javascript
import axios from 'axios';
import authService from './services/auth.service';

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
});

// Request interceptor - add token
api.interceptors.request.use(
  async (config) => {
    const token = authService.getToken();

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Refresh if needed
    if (authService.shouldRefresh()) {
      const newToken = await authService.refreshToken();
      config.headers.Authorization = `Bearer ${newToken}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - handle errors
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 401 Unauthorized - try refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const newToken = await authService.refreshToken();
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        authService.logout();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
```

---

## 🛡️ Step 5: Protected Routes

### React Router example

```javascript
// src/components/ProtectedRoute.jsx
import React from 'react';
import { Navigate } from 'react-router-dom';
import authService from '../services/auth.service';

function ProtectedRoute({ children }) {
  if (!authService.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;
```

### App.jsx with routes

```javascript
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import UserList from './components/UserList';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/users"
          element={
            <ProtectedRoute>
              <UserList />
            </ProtectedRoute>
          }
        />

        <Route path="/" element={<Navigate to="/dashboard" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

---

## 🌐 Step 6: CORS Configuration

### Backend (Spring Boot)

Fayl: `app/src/main/java/uz/hemis/app/config/CorsConfig.java`

```java
package uz.hemis.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Frontend URL (development)
        config.addAllowedOrigin("http://localhost:3000");

        // Production URL
        config.addAllowedOrigin("https://hemis.uz");

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

---

## 📦 Step 7: Frontend Setup

### package.json dependencies

```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.20.0",
    "axios": "^1.6.0"
  }
}
```

### Environment variables

`.env.development`

```
REACT_APP_API_URL=http://localhost:8081
```

`.env.production`

```
REACT_APP_API_URL=https://api.hemis.uz
```

### Usage

```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL;
```

---

## 🎯 Complete Example: Student List

```javascript
// src/pages/Students.jsx
import React, { useState, useEffect } from 'react';
import apiService from '../services/api.service';

function Students() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadStudents();
  }, [page]);

  const loadStudents = async () => {
    try {
      setLoading(true);
      setError('');

      const response = await apiService.get(`/students?page=${page}&size=20`);

      setStudents(response.content || response);
      setTotalPages(response.totalPages || 1);
    } catch (err) {
      setError(err.message || 'Xatolik yuz berdi');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Rostdan ham o\'chirmoqchimisiz?')) {
      return;
    }

    try {
      await apiService.delete(`/students/${id}`);
      loadStudents(); // Reload list
    } catch (err) {
      alert('O\'chirishda xatolik: ' + err.message);
    }
  };

  if (loading) return <div className="loading">Yuklanmoqda...</div>;
  if (error) return <div className="error">Xato: {error}</div>;

  return (
    <div className="students-page">
      <h1>Talabalar ro'yxati</h1>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Ism</th>
            <th>Familiya</th>
            <th>Fakultet</th>
            <th>Harakatlar</th>
          </tr>
        </thead>
        <tbody>
          {students.map(student => (
            <tr key={student.id}>
              <td>{student.id}</td>
              <td>{student.firstName}</td>
              <td>{student.lastName}</td>
              <td>{student.faculty}</td>
              <td>
                <button onClick={() => handleDelete(student.id)}>
                  O'chirish
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Pagination */}
      <div className="pagination">
        <button
          disabled={page === 0}
          onClick={() => setPage(page - 1)}
        >
          Oldingi
        </button>

        <span>Sahifa {page + 1} / {totalPages}</span>

        <button
          disabled={page >= totalPages - 1}
          onClick={() => setPage(page + 1)}
        >
          Keyingi
        </button>
      </div>
    </div>
  );
}

export default Students;
```

---

## 🚀 Deployment

### Frontend build

```bash
cd /home/adm1n/startup/hemis-front
npm run build
```

### Nginx configuration

```nginx
server {
    listen 80;
    server_name hemis.uz;

    # Frontend static files
    root /var/www/hemis-front/build;
    index index.html;

    # Frontend routes
    location / {
        try_files $uri /index.html;
    }

    # Backend API proxy
    location /api {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 📚 Resources

- [React Documentation](https://react.dev/)
- [Axios Documentation](https://axios-http.com/)
- [React Router](https://reactrouter.com/)

---

**Status:** ✅ Integration guide tayyor
**Backend:** http://localhost:8081
**Frontend:** http://localhost:3000
**Last Updated:** 2025-11-09
