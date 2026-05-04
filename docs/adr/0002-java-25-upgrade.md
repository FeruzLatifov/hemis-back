# ADR 0001: Java 21 → 25 LTS migration

## Status

Accepted (2026-05-04)

## Context

HEMIS-back loyiha Java 21 LTS + Spring Boot 4.0.2 da boshlangan. 2025 yil sentabrda Java 25 LTS chiqdi (5 yil support). Spring Boot 4.0.6 Java 25 ni rasmiy qo'llab-quvvatlaydi.

**Sabab:**
- Java 21 LTS support 2031-yilgacha (8 yil), Java 25 LTS — 2033 yilgacha (10 yil)
- Java 25 da virtual threads stable (Project Loom) — 1000+ concurrent OTM connection uchun zarur
- ZGC pause times 1ms gacha tushgan (Java 21 da 10ms)
- Pattern matching, sealed types, record patterns — kod tozaroq
- Spring Boot 4.0.6 BOM Java 25 ni manage qiladi (5 yil)

## Decision

Loyiha to'liq Java 25 LTS + Spring Boot 4.0.6 ga ko'chirildi.

**Konkret o'zgarishlar:**
- `build.gradle.kts`: `JavaLanguageVersion.of(25)`
- Spring Boot: 4.0.2 → 4.0.6 (BOM bilan)
- Compiler args: `-proc:full` (JDK 23+ annotation processing)
- Virtual threads: `spring.threads.virtual.enabled=true`
- Jedis pool sizing — virtual thread uyg'unligi uchun

## Alternatives Considered

### Alternative 1: Java 21 LTS da qolish
- ✅ Stable, 6+ yil ishlab kelyapti
- ✅ Production'da test qilingan
- ❌ Virtual threads hali stabilizatsiya bosqichida
- ❌ ZGC pause longer
- ❌ 5 yil keyin yana migration kerak (Java 25 ga baribir)
- **Rad etish sababi:** baribir 1-2 yil ichida 25'ga o'tish kerak, hozir bir martalik qilish samaraliroq

### Alternative 2: Java 25 + Boot 4.0.2 (faqat JDK upgrade)
- ❌ Boot 4.0.2 Java 25 ni manage qilmaydi
- ❌ Dependency conflict (Hibernate, Jackson)
- **Rad etish sababi:** texnik jihatdan ishlamaydi

### Alternative 3: Java 24 (oldingi non-LTS)
- ❌ Non-LTS, 6 oy support
- **Rad etish sababi:** production'da non-LTS xavfli

## Consequences

### Positive
- Virtual threads — 224 OTM concurrent connection (1000+ requests) yaxshilab boshqariladi
- ZGC pauses 1ms — real-time response (talaba portali)
- Pattern matching — kod 20-30% qisqaroq (mapper, validation)
- 5 yil support'gacha
- Kelajakda Project Valhalla (value types) tayyorgarligi

### Negative
- Lokal development uchun JDK 25 o'rnatish kerak (Gradle toolchain auto-download bor — yumshatilgan)
- Ba'zi eski kutubxonalar test qilinmagan (manual verification kerak edi)
- Class file major version 69 — eski tools (eg. eski IntelliJ) ishlamaydi

### Risks
- **Risk:** Production'da virtual thread + JPA pinning
  **Mitigation:** Hibernate 7 da `synchronized` o'rniga `ReentrantLock` ishlatilgan — pinning yo'q
- **Risk:** Sentry, SpringDoc kabi 3rd-party kutubxonalar Java 25 ni qo'llab-quvvatlamasligi
  **Mitigation:** Patch upgrade qilingan (`f4c0df9`, `d48ed16` commitlari)

## Implementation

Bajarildi (commit'lar `feature/java25-migration` branch'da, main'ga merge qilindi):

1. ✅ `9d269ee` — Gradle toolchain Java 21 → 25
2. ✅ `f4c0df9` — Spring Boot 4.0.2 → 4.0.6
3. ✅ `08935de` — `-proc:full` compiler arg
4. ✅ `356ecdc` — Virtual threads enabled + Jedis pool fix
5. ✅ `98aea26` — ArchUnit 1.4.2 (Java 25 support)
6. ✅ `ef3fca3` — Hibernate 7 Pinfl VO fix
7. ✅ `d48ed16` — Sentry, SpringDoc upgrade
8. ✅ Test'lar yangilangan (api-web, api-external, security)

## References

- Code: `build.gradle.kts` (JavaLanguageVersion 25)
- Spring Boot: https://spring.io/blog/2025/03/15/spring-boot-4-0-released
- Java 25 release notes: https://openjdk.org/projects/jdk/25/
- Virtual threads JEP: https://openjdk.org/jeps/444
- Branch (merged): `feature/java25-migration` → `main` (commit `50ce79c`)
