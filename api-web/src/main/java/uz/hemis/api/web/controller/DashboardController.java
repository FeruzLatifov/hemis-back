package uz.hemis.api.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.dashboard.DashboardService;
import uz.hemis.service.dashboard.dto.DashboardResponse;

import java.util.concurrent.TimeUnit;

/**
 * Dashboard Statistics API Controller - BEST PRACTICE Implementation
 * 
 * Features:
 * - Read from REPLICA database (zero master load)
 * - Redis caching (30 minutes TTL)
 * - Sub-50ms response time (cached)
 * - Comprehensive Swagger documentation
 * - HTTP cache headers
 */
@RestController
@RequestMapping("/api/v1/web/dashboard")
@Tag(
    name = "📊 Dashboard Statistics",
    description = """
        ## Dashboard Statistics API - Production Ready
        
        **🚀 Performance Optimized:**
        - ⚡ 30-minute Redis cache
        - 📈 Aggregates 3M+ student records
        - 🎯 <50ms cached response time
        - 🔄 Automatic cache refresh
        
        **🛡️ Best Practices:**
        - Reads from READ REPLICA database (zero master load)
        - Distributed Redis cache (horizontal scaling)
        - Graceful degradation (if cache fails, uses DB)
        - JWT authentication required
        
        **📊 Statistics Included:**
        - Overview: Students, Teachers, Universities, Diplomas
        - Student Breakdown: By form, region, language
        - Education Types: Bachelor, Master, Doctorate
        - Top 10 Universities: Detailed rankings
        - Recent Activities: Real-time feed
        
        **💡 Usage:**
        ```bash
        curl -H "Authorization: Bearer {token}" \\
             -H "Accept-Language: uz-UZ" \\
             https://api.hemis.uz/api/v1/web/dashboard/stats
        ```
        """
)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(
        summary = "Get comprehensive dashboard statistics",
        description = """
            ## 📊 Dashboard Statistics Endpoint
            
            Returns comprehensive, aggregated statistics for the HEMIS dashboard.
            
            ### 🎯 Data Sources:
            - **Database:** READ REPLICA (zero master load)
            - **Cache:** Redis (30 minutes TTL)
            - **Records:** 3M+ students, 253 universities
            
            ### ⚡ Performance:
            - **First Request:** ~30-40 seconds (database aggregation)
            - **Cached Requests:** <50ms (Redis)
            - **Cache Duration:** 30 minutes
            - **Auto-refresh:** Every 30 minutes
            
            ### 📈 Statistics Included:
            
            **1. Overview (16 metrics):**
            
            **Total Counts:**
            - Total Students: 3.1M+ (barcha statuslar)
            - Total Teachers: 6K+
            - Total Universities: 257
            - Total Diplomas: 861K+
            - Total Projects: 2K+
            - Total Publications: 470K+
            
            **By Status (YANGI):**
            - Active Students (O'qimoqda): 1.68M ✅
            - Graduated Students (Bitirgan): 868K ✅
            - Expelled Students (Chetlashgan): 403K ❌
            - Academic Leave (Akademik ta'til): 5K ⏸️
            - Cancelled Students (Bekor qilingan): 31K ❌
            
            **Payment Type (faqat active):**
            - Grant Students: 173K
            - Contract Students: 1.5M
            
            **Gender (faqat active):**
            - Male: 780K
            - Female: 900K
            
            **2. Student Breakdowns (FAQAT O'QIMOQDA):**
            - By Education Form (Kunduzgi: 886K, Sirtqi: 648K, Kechki: 65K)
            - By Region (Toshkent, Samarqand, Farg'ona, etc.)
            - By Language (O'zbek, Rus, Ingliz, etc.)
            
            **3. Education Types (FAQAT O'QIMOQDA):**
            - Bachelor (~95%)
            - Master (~5%)
            - Doctorate (<1%)
            
            **4. Top 10 Universities (FAQAT O'QIMOQDA):**
            - Ranked by active student count
            - Male/Female breakdown
            - Grant/Contract ratio
            
            **5. Recent Activities:**
            - Latest 5 student registrations
            - Real-time activity feed
            
            ### ⚠️ MUHIM O'ZGARISHLAR:
            
            **Status Bo'yicha Filtrlash:**
            - Barcha statistikalar (education form, region, language, top universities) 
              faqat **O'QIMOQDA** (status_code='11') talabalar uchun
            - Grant/Contract faqat active talabalar
            - Gender faqat active talabalar
            - Bu mantiqiy, chunki bitirganlar/chetlashganlarni statistikaga qo'shish noto'g'ri
            
            ### 🔒 Security:
            - Requires JWT Bearer token
            - Permission: `dashboard.view`
            
            ### 🌐 Localization:
            - Supports: uz-UZ, oz-UZ, ru-RU, en-US
            - Header: `Accept-Language: uz-UZ`
            
            ### 💡 Example Response:
            ```json
            {
              "timestamp": "2025-11-13T04:58:34.756Z",
              "overview": {
                "totalStudents": 3109691,
                "totalTeachers": 6154,
                "totalUniversities": 257,
                "totalDiplomas": 861454,
                "totalProjects": 1973,
                "totalPublications": 469917,
                "activeStudents": 1680500,
                "graduatedStudents": 868424,
                "expelledStudents": 403437,
                "academicLeaveStudents": 4947,
                "cancelledStudents": 30795,
                "grantStudents": 172888,
                "contractStudents": 1507612,
                "maleCount": 780087,
                "femaleCount": 900413
              },
              "students": {
                "byEducationForm": [
                  {"name": "Kunduzgi", "count": 886193},
                  {"name": "Sirtqi", "count": 648046}
                ],
                "byRegion": [...],
                "byLanguage": [...]
              },
              "educationTypes": [
                {"code": "11", "name": "Bakalavr", "count": 1595000}
              ],
              "topUniversities": [
                {
                  "rank": 1,
                  "name": "...",
                  "studentCount": 50000,
                  "grantCount": 10000,
                  "contractCount": 40000
                }
              ],
              "recentActivities": [...]
            }
            ```
            
            ### ⚠️ Notes:
            - Data refreshes every 30 minutes
            - Large response (~15-20KB)
            - All data from REPLICA database
            - Graceful fallback if Redis unavailable
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Statistics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DashboardResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    summary = "Complete dashboard statistics",
                    value = """
                        {
                          "timestamp": "2025-11-13T04:04:52.302Z",
                          "overview": {
                            "totalStudents": 2957308,
                            "totalTeachers": 6154,
                            "totalUniversities": 253,
                            "totalDiplomas": 861454
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "🔒 Unauthorized - Invalid or missing JWT token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2025-11-13T04:04:52.302Z",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "Full authentication is required"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "🚫 Forbidden - User lacks 'dashboard.view' permission",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2025-11-13T04:04:52.302Z",
                          "status": 403,
                          "error": "Forbidden",
                          "message": "Access Denied"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "❌ Internal Server Error - Database or Redis issue",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2025-11-13T04:04:52.302Z",
                          "status": 500,
                          "error": "Internal Server Error",
                          "message": "An unexpected error occurred"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<DashboardResponse> getDashboardStats() {
        log.info("📊 Dashboard statistics requested");
        
        DashboardResponse stats = dashboardService.getDashboardStats();
        
        // Add HTTP cache headers (client-side caching)
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES)  // Browser cache: 30 min
                        .cachePublic()                                    // Shareable cache
                        .mustRevalidate())                                // Revalidate when stale
                .body(stats);
    }
}
