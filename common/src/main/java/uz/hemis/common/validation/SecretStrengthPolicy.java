package uz.hemis.common.validation;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mashina kredensiali (OAuth2 {@code client_secret}) uchun mustahkamlik siyosati.
 *
 * <p><strong>Nega server tomonda ham kerak:</strong> frontend'dagi baho
 * ({@code hemis-front/src/lib/secretStrength.ts}) foydalanuvchiga yordam beradi, lekin u
 * xavfsizlik nazorati EMAS — endpoint {@code curl} bilan to'g'ridan-to'g'ri ham chaqiriladi.
 * Shuning uchun qoidalar shu yerda majburlanadi.</p>
 *
 * <p><strong>Nega entropiya, tarkib qoidalari emas:</strong> "bitta bosh harf + bitta raqam"
 * talabi odamlarni {@code Parol123!} kabi taxmin qilinadigan namunalarga majburlaydi. NIST
 * SP 800-63B aynan shuning uchun tarkib qoidalaridan voz kechishni tavsiya qiladi: uzunlik va
 * tasodifiylik himoya beradi. Shu sabab uzun kichik-harfli satr bu yerda yaroqli hisoblanadi,
 * {@code admin1234567} esa — yo'q.</p>
 *
 * <p>⚠️ Bu klass qoidalari frontend'dagi {@code secretStrength.ts} bilan AYNAN mos bo'lishi
 * shart. Aks holda admin frontend'dan o'tib, serverdan 400 oladi.</p>
 */
public final class SecretStrengthPolicy {

    /** Qo'lda kiritilgan maxfiy kalit uchun eng kam uzunlik. */
    public static final int MIN_LENGTH = 12;

    /** Entropiyaning eng kam maqbul darajasi (bit). */
    public static final int MIN_ENTROPY_BITS = 50;

    /** Takrorlanish chegarasi: noyob belgilar ulushi shundan past bo'lsa — rad etiladi. */
    private static final double MIN_UNIQUE_RATIO = 0.35;

    /** Ketma-ket yurish uzunligi ({@code abcd}, {@code 4321}) shundan boshlab rad etiladi. */
    private static final int MAX_SEQUENTIAL_RUN = 4;

    /** Client ID moslashuvi shu uzunlikdan boshlab tekshiriladi (qisqasi soxta moslik beradi). */
    private static final int MIN_CLIENT_ID_MATCH_LENGTH = 3;

    /**
     * Taxmin qilinadigan negizlar. Bu "buzilgan parollar bazasi" emas — eng ko'p uchraydigan
     * tanlovlarni to'sadi. Haqiqiy himoya — avtomatik generatsiya (288 bit).
     * ⚠️ {@code secretStrength.ts} dagi {@code COMMON_WORDS} bilan bir xil tutilsin.
     */
    private static final List<String> COMMON_WORDS = List.of(
            "password", "passwd", "parol", "admin", "administrator", "root", "user", "login",
            "secret", "client", "qwerty", "asdf", "welcome", "letmein", "changeme", "default",
            "test", "demo", "sample", "example", "master", "system", "server",
            "hemis", "otm", "univer", "ministry", "vazirlik", "uzbek");

    private SecretStrengthPolicy() {
    }

    /** Rad etish sababi — foydalanuvchiga ko'rsatiladigan o'zbekcha xabar bilan. */
    public enum Violation {
        TOO_SHORT("Maxfiy kalit kamida " + MIN_LENGTH + " belgidan iborat bo'lishi kerak"),
        CONTAINS_CLIENT_ID("Maxfiy kalit client_id ni o'z ichiga olmasligi kerak"),
        COMMON_WORD("Maxfiy kalit oson taxmin qilinadigan so'z (admin, parol, test va h.k.) ishlatmasligi kerak"),
        LOW_ENTROPY("Maxfiy kalit yetarlicha murakkab emas — uzunroq yoki tasodifiyroq qiymat tanlang"),
        REPEATED("Maxfiy kalitda takrorlanuvchi belgilar juda ko'p"),
        SEQUENTIAL("Maxfiy kalitda abcd yoki 1234 kabi ketma-ketlik bo'lmasligi kerak");

        private final String message;

        Violation(String message) {
            this.message = message;
        }

        public String message() {
            return message;
        }
    }

    /**
     * Maxfiy kalitni tekshiradi.
     *
     * @param secret   ochiq maxfiy kalit
     * @param clientId shu hisobning {@code client_id} si (null bo'lishi mumkin)
     * @return buzilgan qoidalar; bo'sh ro'yxat — maxfiy kalit maqbul
     */
    public static List<Violation> validate(String secret, String clientId) {
        if (secret == null || secret.isBlank()) {
            return List.of(Violation.TOO_SHORT);
        }
        List<Violation> violations = new java.util.ArrayList<>();
        String lowered = secret.toLowerCase(Locale.ROOT);

        if (secret.length() < MIN_LENGTH) {
            violations.add(Violation.TOO_SHORT);
        }
        if (clientId != null) {
            String id = clientId.trim().toLowerCase(Locale.ROOT);
            if (id.length() >= MIN_CLIENT_ID_MATCH_LENGTH && lowered.contains(id)) {
                violations.add(Violation.CONTAINS_CLIENT_ID);
            }
        }
        if (COMMON_WORDS.stream().anyMatch(lowered::contains)) {
            violations.add(Violation.COMMON_WORD);
        }
        if (uniqueRatio(secret) < MIN_UNIQUE_RATIO) {
            violations.add(Violation.REPEATED);
        }
        if (hasSequentialRun(secret)) {
            violations.add(Violation.SEQUENTIAL);
        }
        if (entropyBits(secret) < MIN_ENTROPY_BITS) {
            violations.add(Violation.LOW_ENTROPY);
        }
        return violations;
    }

    /** Buzilgan qoidalarni bitta o'qiladigan xabarga yig'adi. */
    public static String describe(List<Violation> violations) {
        return violations.stream().map(Violation::message).collect(Collectors.joining("; "));
    }

    /**
     * Entropiya bahosi (bit).
     *
     * <p>Belgilar hovuzi ishlatilgan sinflardan yig'iladi, so'ng takrorlanish uchun jarima
     * qo'llanadi: {@code "aaaaaaaaaaaa"} xom hisobda 12 belgi bo'lsa-da, amalda bitta belgi.</p>
     */
    public static int entropyBits(String secret) {
        if (secret == null || secret.isEmpty()) {
            return 0;
        }
        boolean lower = secret.chars().anyMatch(Character::isLowerCase);
        boolean upper = secret.chars().anyMatch(Character::isUpperCase);
        boolean digit = secret.chars().anyMatch(Character::isDigit);
        boolean symbol = secret.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        int pool = (lower ? 26 : 0) + (upper ? 26 : 0) + (digit ? 10 : 0) + (symbol ? 33 : 0);
        if (pool == 0) {
            return 0;
        }
        double raw = secret.length() * (Math.log(pool) / Math.log(2));
        return (int) Math.round(raw * (0.4 + 0.6 * uniqueRatio(secret)));
    }

    private static double uniqueRatio(String secret) {
        Set<Integer> distinct = secret.chars().boxed().collect(Collectors.toSet());
        return (double) distinct.size() / secret.length();
    }

    /** {@code abcd} / {@code 4321} kabi 4+ uzunlikdagi ketma-ketlik. */
    private static boolean hasSequentialRun(String secret) {
        String lowered = secret.toLowerCase(Locale.ROOT);
        int run = 1;
        int direction = 0;
        for (int i = 1; i < lowered.length(); i++) {
            int delta = lowered.charAt(i) - lowered.charAt(i - 1);
            if (delta == 1 || delta == -1) {
                if (delta == direction) {
                    run++;
                } else {
                    direction = delta;
                    run = 2;
                }
                if (run >= MAX_SEQUENTIAL_RUN) {
                    return true;
                }
            } else {
                direction = 0;
                run = 1;
            }
        }
        return false;
    }
}
