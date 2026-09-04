package uz.hemis.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ConflictException;
import uz.hemis.domain.repository.UserRepository;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

/**
 * PERSON akkaunt uchun login (username) generatori — ism + familiyadan.
 *
 * <p><strong>Nega kerak:</strong> ilgari PERSON akkauntda login = 14 xonali PINFL edi.
 * PINFL — milliy identifikator (PII): uni login sifatida ishlatish har bir kirish ekranida,
 * har bir audit qatorida va har bir "kim onlayn" ro'yxatida shaxsni oshkor qiladi. Endi login
 * o'qiladigan lotin slug: {@code ISM FAMILIYA → ism_familiya}. PINFL o'zining
 * {@code users.pinfl} ustunida qoladi (o'zgarishsiz).</p>
 *
 * <p><strong>Algoritm markazda (backend) turadi</strong> — frontend uni qayta yozmaydi.
 * Frontend faqat {@code POST /api/v1/web/admin/users/login-suggestion} ni chaqiradi, shunda
 * taklif qilingan login va yakuniy saqlangan login har doim bir xil qoidadan chiqadi.</p>
 *
 * <p><strong>Forward-only:</strong> allaqachon {@code username = pinfl} bilan yaratilgan
 * akkauntlar o'z loginini saqlab qoladi va ishlayveradi — hech qanday data migration yo'q.</p>
 *
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class LoginNameGenerator {

    /**
     * Slug uzunligi chegarasi. DTO {@code @Size(max = 50)} — 46 belgi bazaga, qolgan 4 ta
     * raqamli suffiks uchun ({@code base + "99"} = 48 ≤ 50).
     */
    static final int MAX_BASE_LENGTH = 46;

    /** DTO {@code @Size(min = 3)} bilan bir xil — 3 belgidan qisqa slug login bo'la olmaydi. */
    static final int MIN_BASE_LENGTH = 3;

    /** Kolliziya suffiksining yuqori chegarasi: base, base2, base3, …, base99. */
    static final int MAX_SUFFIX = 99;

    /**
     * Kirill → lotin jadvali (o'zbek + rus + qoraqalpoq), KICHIK harflar bo'yicha kalitlangan.
     *
     * <p>O'zbek kirilliga xos harflar ham qamrab olingan: {@code ў → o}, {@code қ → q},
     * {@code ғ → g}, {@code ҳ → h}. Rus {@code х} — {@code x}, o'zbek {@code ҳ} — {@code h}
     * (ikkalasi ham "h" tovushi, lekin lotin o'zbekchada ular ajratiladi).</p>
     *
     * <p>{@code ъ} va {@code ь} — belgisiz (o'chiriladi).</p>
     */
    private static final Map<Character, String> CYRILLIC_TO_LATIN = Map.ofEntries(
            Map.entry('а', "a"),
            Map.entry('б', "b"),
            Map.entry('в', "v"),
            Map.entry('г', "g"),
            Map.entry('ғ', "g"),   // U+0493 — o'zbek "g'"
            Map.entry('д', "d"),
            Map.entry('е', "e"),
            Map.entry('ё', "yo"),
            Map.entry('ж', "j"),
            Map.entry('з', "z"),
            Map.entry('и', "i"),
            Map.entry('й', "y"),
            Map.entry('к', "k"),
            Map.entry('қ', "q"),   // U+049B — o'zbek "q"
            Map.entry('л', "l"),
            Map.entry('м', "m"),
            Map.entry('н', "n"),
            Map.entry('о', "o"),
            Map.entry('п', "p"),
            Map.entry('р', "r"),
            Map.entry('с', "s"),
            Map.entry('т', "t"),
            Map.entry('у', "u"),
            Map.entry('ў', "o"),   // U+045E — o'zbek "o'"
            Map.entry('ф', "f"),
            Map.entry('х', "x"),
            Map.entry('ҳ', "h"),   // U+04B3 — o'zbek "h"
            Map.entry('ц', "ts"),
            Map.entry('ч', "ch"),
            Map.entry('ш', "sh"),
            Map.entry('щ', "sh"),
            Map.entry('ъ', ""),
            Map.entry('ы', "i"),
            Map.entry('ь', ""),
            Map.entry('э', "e"),
            Map.entry('ю', "yu"),
            Map.entry('я', "ya"),
            // Qoraqalpoq/qozoq kirillchasi. Bularsiz harflar jimgina TASHLAB YUBORILARDI
            // (filtr faqat [a-z0-9] qoldiradi): "Гүлнара Өтемисова" → glnara_temisova.
            Map.entry('ә', "a"),   // U+04D9
            Map.entry('ө', "o"),   // U+04E9
            Map.entry('ү', "u"),   // U+04AF
            Map.entry('ұ', "u"),   // U+04B1
            Map.entry('ң', "n"),   // U+04A3
            Map.entry('һ', "h"),   // U+04BB
            Map.entry('і', "i")    // U+0456
    );

    private final UserRepository userRepository;

    /**
     * Ism + familiyadan bo'sh (band bo'lmagan) login hosil qiladi.
     *
     * <p>Avval {@code base} sinaladi, band bo'lsa {@code base2}, {@code base3}, …,
     * {@code base99}. Tekshiruv REGISTRGA BOG'LIQ EMAS, chunki {@code users} jadvalida
     * {@code uq_users_username_lower} funksional unikal indeksi bor
     * ({@code LOWER(username) WHERE deleted_at IS NULL}) — registrga bog'liq tekshiruv
     * saqlash paytida DB xatosiga olib kelardi.</p>
     *
     * @param firstName ism (xom holda — qo'lda kiritilgan yoki GUVD gateway'dan autofill)
     * @param lastName  familiya (xom holda)
     * @return band bo'lmagan login, masalan {@code ism_familiya} yoki {@code ism_familiya2}
     * @throws BadRequestException slug 3 belgidan qisqa bo'lsa (ikkala ism ham bo'sh/yaroqsiz)
     * @throws ConflictException   99 ta variantning hammasi band bo'lsa
     */
    public String generate(String firstName, String lastName) {
        String base = buildBase(firstName, lastName);

        if (!userRepository.existsByUsernameIgnoreCase(base)) {
            return base;
        }
        for (int suffix = 2; suffix <= MAX_SUFFIX; suffix++) {
            String candidate = base + suffix;
            if (!userRepository.existsByUsernameIgnoreCase(candidate)) {
                return candidate;
            }
        }
        throw new ConflictException("Username already exists");
    }

    /**
     * Slug "poydevori": {@code ism_familiya} (ISM BIRINCHI, familiya ikkinchi).
     *
     * <p>Bitta qism bo'sh bo'lsa — ikkinchisi yolg'iz ishlatiladi (pastki chiziqsiz).
     * Natija {@value #MAX_BASE_LENGTH} belgigacha qirqiladi.</p>
     *
     * @throws BadRequestException natija {@value #MIN_BASE_LENGTH} belgidan qisqa bo'lsa
     */
    static String buildBase(String firstName, String lastName) {
        String first = slugify(firstName);
        String last = slugify(lastName);

        String base;
        if (!first.isEmpty() && !last.isEmpty()) {
            base = first + "_" + last;
        } else if (!first.isEmpty()) {
            base = first;
        } else {
            base = last;
        }

        if (base.length() > MAX_BASE_LENGTH) {
            base = base.substring(0, MAX_BASE_LENGTH);
        }
        if (base.length() < MIN_BASE_LENGTH) {
            throw new BadRequestException("First name and last name are required");
        }
        return base;
    }

    /**
     * Bitta ism qismini normallashtiradi: trim → NFC → kichik harf → kirill translit →
     * NFD + diakritikani olib tashlash → {@code [a-z0-9]} dan tashqari hamma narsani o'chirish.
     *
     * <p><strong>Qadamlar tartibi muhim:</strong> kirill translit NFD dan OLDIN turadi.
     * NFD {@code ў}(U+045E), {@code й}(U+0439), {@code ё}(U+0451) ni "harf + qo'shimcha belgi"
     * ga ajratadi; belgini olib tashlasak {@code ў → у}, ya'ni {@code o} o'rniga {@code u}
     * chiqib ketardi. Shuning uchun avval jadval qo'llanadi, NFD esa faqat qolgan lotin
     * diakritikasi uchun ishlaydi ({@code é → e}). Boshlang'ich NFC — kirituvchi allaqachon
     * ajratilgan shaklda yozgan bo'lsa, uni qayta yig'ish uchun.</p>
     *
     * <p>O'zbek lotin apostroflari ({@code '}, {@code ’}, {@code ʻ}, {@code ʼ}, {@code `}) va
     * defis oxirgi filtrda shunchaki o'chiriladi: {@code O'ktam → oktam},
     * {@code Sa'dulla → sadulla}, {@code Petrova-Ivanova → petrovaivanova}.</p>
     */
    static String slugify(String raw) {
        if (raw == null) {
            return "";
        }
        String lowered = Normalizer.normalize(raw.trim(), Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT);

        StringBuilder transliterated = new StringBuilder(lowered.length() + 8);
        for (int i = 0; i < lowered.length(); i++) {
            char c = lowered.charAt(i);
            String latin = CYRILLIC_TO_LATIN.get(c);
            if (latin != null) {
                transliterated.append(latin);
            } else {
                transliterated.append(c);
            }
        }

        return Normalizer.normalize(transliterated, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }
}
