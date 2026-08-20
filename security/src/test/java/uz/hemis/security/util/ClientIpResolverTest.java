package uz.hemis.security.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ClientIpResolver — trusted-proxy qoidasi va X-Forwarded-For spoofing himoyasi.
 *
 * <p>Bu klass qaytargan manzil rate-limit hisoblagichi, OAuth-client IP allowlist va audit izining
 * kaliti — ya'ni bu yerdagi xato "cheklov chetlab o'tildi" degani. Shuning uchun testlar aynan
 * chegara holatlarini bosadi: ishonchsiz peer'ning soxta sarlavhasi, ko'p-hopli zanjir, prod'dagi
 * aniq ro'yxat va IPv4-mapped IPv6.</p>
 *
 * <p>{@code trustedProxiesConfig} — {@code @Value} maydoni (prod'da Spring to'ldiradi), shuning
 * uchun testda {@link ReflectionTestUtils} bilan o'rnatiladi. Bo'sh qoldirilsa dev-rejim amal
 * qiladi: loopback + RFC-1918 ishonchli.</p>
 */
@DisplayName("ClientIpResolver — trusted-proxy va XFF spoofing himoyasi")
class ClientIpResolverTest {

    private static final String CLIENT = "203.0.113.7";   // TEST-NET-3 — public, hech qachon proksi emas
    private static final String ATTACKER = "198.51.100.9"; // TEST-NET-2 — public

    private static ClientIpResolver resolver(String trustedProxies) {
        ClientIpResolver resolver = new ClientIpResolver();
        ReflectionTestUtils.setField(resolver, "trustedProxiesConfig", trustedProxies);
        return resolver;
    }

    private static MockHttpServletRequest request(String remoteAddr) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr(remoteAddr);
        return req;
    }

    @Nested
    @DisplayName("Ishonchsiz peer — forwarded sarlavhalar E'TIBORSIZ")
    class UntrustedPeer {

        @Test
        @DisplayName("Public peer X-Forwarded-For yuborsa — soxta manzil emas, socket peer qaytadi")
        void publicPeerSpoofingXff_isIgnored() {
            MockHttpServletRequest req = request(ATTACKER);
            req.addHeader("X-Forwarded-For", CLIENT); // "men aslida boshqaman" — ishonilmaydi

            assertThat(resolver("").resolve(req)).isEqualTo(ATTACKER);
        }

        @Test
        @DisplayName("Public peer X-Real-IP yuborsa ham — socket peer qaytadi")
        void publicPeerSpoofingXRealIp_isIgnored() {
            MockHttpServletRequest req = request(ATTACKER);
            req.addHeader("X-Real-IP", CLIENT);

            assertThat(resolver("").resolve(req)).isEqualTo(ATTACKER);
        }

        @Test
        @DisplayName("Sarlavhasiz oddiy so'rov — socket peer")
        void plainRequest_returnsSocketPeer() {
            assertThat(resolver("").resolve(request(CLIENT))).isEqualTo(CLIENT);
        }
    }

    @Nested
    @DisplayName("Ishonchli proksi (dev: loopback + RFC-1918)")
    class TrustedProxyDevDefault {

        @Test
        @DisplayName("Bitta hop — XFF dagi manzil client")
        void singleHop_returnsClient() {
            MockHttpServletRequest req = request("10.0.0.5");
            req.addHeader("X-Forwarded-For", CLIENT);

            assertThat(resolver("").resolve(req)).isEqualTo(CLIENT);
        }

        @Test
        @DisplayName("Ko'p hop — o'ngdan chapga, birinchi ISHONCHSIZ manzil client")
        void multiHop_scansRightToLeft() {
            MockHttpServletRequest req = request("10.0.0.5");
            // client -> ingress -> pod: o'ng tomondagi ikkita hop ishonchli proksi
            req.addHeader("X-Forwarded-For", CLIENT + ", 10.1.2.3, 192.168.5.5");

            assertThat(resolver("").resolve(req)).isEqualTo(CLIENT);
        }

        @Test
        @DisplayName("Zanjirga soxta hop qo'shilsa — eng o'ngdagi ishonchsiz g'olib (chapdagi soxta e'tiborsiz)")
        void appendedChain_ignoresLeftmostForgery() {
            MockHttpServletRequest req = request("10.0.0.5");
            // Hujumchi chapga o'z qiymatini yozadi; ingress uning haqiqiy manzilini o'ngga QO'SHADI.
            // Chapdan o'qish (keng tarqalgan xato) soxta qiymatni qaytarardi.
            req.addHeader("X-Forwarded-For", CLIENT + ", " + ATTACKER);

            assertThat(resolver("").resolve(req)).isEqualTo(ATTACKER);
        }

        @Test
        @DisplayName("Hamma hop ishonchli — eng chapdagi (client'ga eng yaqin) qaytadi")
        void allHopsTrusted_returnsLeftmost() {
            MockHttpServletRequest req = request("127.0.0.1");
            req.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1");

            assertThat(resolver("").resolve(req)).isEqualTo("10.0.0.1");
        }

        @Test
        @DisplayName("XFF yo'q, X-Real-IP bor — X-Real-IP qaytadi")
        void noXff_fallsBackToXRealIp() {
            MockHttpServletRequest req = request("10.0.0.5");
            req.addHeader("X-Real-IP", CLIENT);

            assertThat(resolver("").resolve(req)).isEqualTo(CLIENT);
        }

        @Test
        @DisplayName("Forwarded sarlavhalari umuman yo'q — proksining o'z manzili")
        void trustedProxyWithoutHeaders_returnsProxyItself() {
            assertThat(resolver("").resolve(request("10.0.0.5"))).isEqualTo("10.0.0.5");
        }

        @Test
        @DisplayName("Bo'sh hop'lar tashlab yuboriladi")
        void blankHops_areSkipped() {
            MockHttpServletRequest req = request("10.0.0.5");
            req.addHeader("X-Forwarded-For", CLIENT + ", ,  ");

            assertThat(resolver("").resolve(req)).isEqualTo(CLIENT);
        }
    }

    @Nested
    @DisplayName("Prod: aniq trusted-proxies ro'yxati (RFC-1918 blanket ishonch YO'Q)")
    class ExplicitTrustList {

        @Test
        @DisplayName("Ro'yxatdagi proksi — XFF qabul qilinadi")
        void listedProxy_honoursXff() {
            MockHttpServletRequest req = request("10.0.0.5");
            req.addHeader("X-Forwarded-For", CLIENT);

            assertThat(resolver("10.0.0.5, 10.0.0.6").resolve(req)).isEqualTo(CLIENT);
        }

        @Test
        @DisplayName("Ro'yxatda YO'Q private peer — endi ishonchsiz, XFF e'tiborsiz (dev'dan farqi)")
        void unlistedPrivatePeer_isNoLongerTrusted() {
            MockHttpServletRequest req = request("10.9.9.9"); // private, lekin ro'yxatda yo'q
            req.addHeader("X-Forwarded-For", CLIENT);

            assertThat(resolver("10.0.0.5").resolve(req)).isEqualTo("10.9.9.9");
        }

        @Test
        @DisplayName("Loopback ro'yxatdan qat'i nazar ishonchli")
        void loopbackAlwaysTrusted() {
            MockHttpServletRequest req = request("127.0.0.1");
            req.addHeader("X-Forwarded-For", CLIENT);

            assertThat(resolver("10.0.0.5").resolve(req)).isEqualTo(CLIENT);
        }

        @Test
        @DisplayName("Zanjirdagi ro'yxatda yo'q private hop — client sifatida qaytadi")
        void unlistedPrivateHopInChain_isTheClient() {
            MockHttpServletRequest req = request("10.0.0.5");
            // 10.9.9.9 prod ro'yxatida yo'q → ishonchsiz → o'ngdan birinchi topilgan client
            req.addHeader("X-Forwarded-For", CLIENT + ", 10.9.9.9");

            assertThat(resolver("10.0.0.5").resolve(req)).isEqualTo("10.9.9.9");
        }
    }

    @Nested
    @DisplayName("IPv4-mapped IPv6 normalizatsiyasi")
    class Ipv4MappedIpv6 {

        @Test
        @DisplayName("::ffff:10.0.0.5 peer private deb tanilib, XFF qabul qilinadi")
        void mappedPrivatePeer_isTrusted() {
            MockHttpServletRequest req = request("::ffff:10.0.0.5");
            req.addHeader("X-Forwarded-For", CLIENT);

            assertThat(resolver("").resolve(req)).isEqualTo(CLIENT);
        }

        @Test
        @DisplayName("::ffff:127.0.0.1 loopback deb tanilib, XFF qabul qilinadi")
        void mappedLoopback_isTrusted() {
            MockHttpServletRequest req = request("::ffff:127.0.0.1");
            req.addHeader("X-Forwarded-For", CLIENT);

            assertThat(resolver("").resolve(req)).isEqualTo(CLIENT);
        }

        @Test
        @DisplayName("::1 loopback — XFF qabul qilinadi")
        void ipv6Loopback_isTrusted() {
            MockHttpServletRequest req = request("::1");
            req.addHeader("X-Forwarded-For", CLIENT);

            assertThat(resolver("").resolve(req)).isEqualTo(CLIENT);
        }
    }

    @Nested
    @DisplayName("RFC-1918 chegaralari (dev-rejim)")
    class PrivateRangeBoundaries {

        @Test
        @DisplayName("172.16-31 ishonchli, 172.15 va 172.32 EMAS")
        void class172_onlyWithinRfc1918() {
            assertThat(resolveWithXff("172.16.0.1")).isEqualTo(CLIENT);
            assertThat(resolveWithXff("172.31.255.1")).isEqualTo(CLIENT);
            // Diapazondan tashqarida — ishonchsiz, XFF e'tiborsiz, socket peer qaytadi
            assertThat(resolveWithXff("172.15.0.1")).isEqualTo("172.15.0.1");
            assertThat(resolveWithXff("172.32.0.1")).isEqualTo("172.32.0.1");
        }

        @Test
        @DisplayName("10.x va 192.168.x ishonchli, 192.169.x EMAS")
        void class10And192_boundaries() {
            assertThat(resolveWithXff("10.255.255.255")).isEqualTo(CLIENT);
            assertThat(resolveWithXff("192.168.255.1")).isEqualTo(CLIENT);
            assertThat(resolveWithXff("192.169.0.1")).isEqualTo("192.169.0.1");
        }

        /** Peer'dan XFF={@link #CLIENT} bilan so'rov: ishonchli bo'lsa CLIENT, aks holda peer qaytadi. */
        private static String resolveWithXff(String peer) {
            MockHttpServletRequest req = request(peer);
            req.addHeader("X-Forwarded-For", CLIENT);
            return resolver("").resolve(req);
        }
    }
}
