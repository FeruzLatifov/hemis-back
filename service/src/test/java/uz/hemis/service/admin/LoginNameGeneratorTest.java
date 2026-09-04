package uz.hemis.service.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ConflictException;
import uz.hemis.domain.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginNameGenerator — ism+familiya → login slug")
class LoginNameGeneratorTest {

    @Mock private UserRepository userRepository;

    /** Hech bir login band emas — slug qoidasining o'zini tekshiruvchi testlar uchun. */
    private LoginNameGenerator freeGenerator() {
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        return new LoginNameGenerator(userRepository);
    }

    // =====================================================
    // Slug qoidasi
    // =====================================================

    @Test
    @DisplayName("lotin ism-familiya → ism_familiya (ISM BIRINCHI)")
    void latinName() {
        assertThat(freeGenerator().generate("UTKIR", "XAMDAMOV")).isEqualTo("utkir_xamdamov");
    }

    @Test
    @DisplayName("o'zbek kirill → lotin: Ў→o, Қ→q, Ғ→g, Ҳ→h")
    void uzbekCyrillic() {
        LoginNameGenerator gen = freeGenerator();
        assertThat(gen.generate("Ўткир", "Хамдамов")).isEqualTo("otkir_xamdamov");
        assertThat(gen.generate("Қобил", "Ғаффоров")).isEqualTo("qobil_gafforov");
        assertThat(gen.generate("Ҳасан", "Ҳусанов")).isEqualTo("hasan_husanov");
    }

    @Test
    @DisplayName("rus kirill: ё→yo, й→y, ц→ts, ч→ch, ш→sh, ъ/ь→'', ы→i, ю→yu, я→ya")
    void russianCyrillic() {
        LoginNameGenerator gen = freeGenerator();
        assertThat(gen.generate("Пётр", "Чайковский")).isEqualTo("pyotr_chaykovskiy");
        assertThat(gen.generate("Юлия", "Цыганова")).isEqualTo("yuliya_tsiganova");
        assertThat(gen.generate("Игорь", "Объедков")).isEqualTo("igor_obedkov");
    }

    @Test
    @DisplayName("qoraqalpoq/qozoq kirilli: Ә→a, Ө→o, Ү→u, Ұ→u, Ң→n, Һ→h, І→i")
    void karakalpakCyrillic() {
        LoginNameGenerator gen = freeGenerator();
        // Jadvalda bo'lmagan harf jimgina tashlab yuborilardi ("Гүлнара" → "glnara"),
        // ya'ni Nukus OTM xodimi tanib bo'lmaydigan login olardi.
        assertThat(gen.generate("Гүлнара", "Өтемисова")).isEqualTo("gulnara_otemisova");
        assertThat(gen.generate("Әлимбет", "Ниязов")).isEqualTo("alimbet_niyazov");
        assertThat(gen.generate("Мыңбай", "Сейтов")).isEqualTo("minbay_seytov");
    }

    @Test
    @DisplayName("bir shaxsning kirillcha va lotincha yozuvi bir xil loginga tushadi")
    void sameCyrillicAndLatinPerson() {
        LoginNameGenerator gen = freeGenerator();
        // Rus imlosi (Уткир) va lotin imlosi (Utkir) — ayni odam, ayni slug.
        assertThat(gen.generate("Уткир", "Хамдамов"))
                .isEqualTo(gen.generate("Utkir", "Xamdamov"))
                .isEqualTo("utkir_xamdamov");
    }

    @Test
    @DisplayName("o'zbek lotin apostroflari o'chiriladi: Sa'dulla→sadulla, O'ktam→oktam")
    void uzbekApostrophes() {
        LoginNameGenerator gen = freeGenerator();
        // Turli apostrof belgilari: to'g'ri tirnoq, o'ng bir tirnoq, modifier turned comma.
        assertThat(gen.generate("Sa'dulla", "O'ktamov")).isEqualTo("sadulla_oktamov");
        assertThat(gen.generate("Sa’dulla", "Oʻktamov")).isEqualTo("sadulla_oktamov");
        assertThat(gen.generate("Gʼayrat", "Toʼlqinov")).isEqualTo("gayrat_tolqinov");
    }

    @Test
    @DisplayName("defisli familiya — defis o'chiriladi, bo'sh joy ham")
    void hyphenatedSurname() {
        LoginNameGenerator gen = freeGenerator();
        assertThat(gen.generate("Anna", "Petrova-Ivanova")).isEqualTo("anna_petrovaivanova");
        assertThat(gen.generate("  Ali  ", "Abdulla Qodiriy")).isEqualTo("ali_abdullaqodiriy");
    }

    @Test
    @DisplayName("ism bo'sh — familiya yolg'iz ishlatiladi (pastki chiziqsiz)")
    void blankFirstName() {
        LoginNameGenerator gen = freeGenerator();
        assertThat(gen.generate("   ", "Xamdamov")).isEqualTo("xamdamov");
        assertThat(gen.generate(null, "Xamdamov")).isEqualTo("xamdamov");
        // Teskarisi ham: familiya bo'sh bo'lsa ism yolg'iz.
        assertThat(gen.generate("Utkir", null)).isEqualTo("utkir");
    }

    @Test
    @DisplayName("ikkala ism ham bo'sh → BadRequest 'First name and last name are required'")
    void bothBlank_throwsBadRequest() {
        // Slug bo'sh — repo'ga umuman yetib bormaydi (stub kerak emas).
        LoginNameGenerator gen = new LoginNameGenerator(userRepository);

        assertThatThrownBy(() -> gen.generate("  ", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("First name and last name are required");

        // Faqat tinish belgisi / apostrof — slug bo'sh chiqadi, ayni xato.
        assertThatThrownBy(() -> gen.generate("'''", "---"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("First name and last name are required");
    }

    @Test
    @DisplayName("3 belgidan qisqa slug → BadRequest (DTO @Size(min=3) bilan bir xil chegara)")
    void tooShort_throwsBadRequest() {
        LoginNameGenerator gen = new LoginNameGenerator(userRepository);

        assertThatThrownBy(() -> gen.generate("Li", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("First name and last name are required");
    }

    @Test
    @DisplayName("juda uzun ism — base 46 belgigacha qirqiladi (suffiks 50 chegarasiga sig'sin)")
    void veryLongName_truncatedTo46() {
        String login = freeGenerator().generate("A".repeat(40), "B".repeat(40));

        assertThat(login).hasSize(46);
        assertThat(login).isEqualTo("a".repeat(40) + "_" + "b".repeat(5));
        // base(46) + eng uzun suffiks("99") = 48 ≤ DTO @Size(max = 50)
        assertThat(login.length() + 2).isLessThanOrEqualTo(50);
    }

    // =====================================================
    // Kolliziya (band login)
    // =====================================================

    @Test
    @DisplayName("kolliziya: base band → base2, u ham band → base3")
    void collision_walksSuffixes() {
        when(userRepository.existsByUsernameIgnoreCase("utkir_xamdamov")).thenReturn(true);
        when(userRepository.existsByUsernameIgnoreCase("utkir_xamdamov2")).thenReturn(true);
        when(userRepository.existsByUsernameIgnoreCase("utkir_xamdamov3")).thenReturn(false);

        LoginNameGenerator gen = new LoginNameGenerator(userRepository);

        assertThat(gen.generate("Utkir", "Xamdamov")).isEqualTo("utkir_xamdamov3");
        verify(userRepository).existsByUsernameIgnoreCase("utkir_xamdamov");
        verify(userRepository).existsByUsernameIgnoreCase("utkir_xamdamov2");
        verify(userRepository).existsByUsernameIgnoreCase("utkir_xamdamov3");
    }

    @Test
    @DisplayName("band tekshiruvi REGISTRGA BOG'LIQ BO'LMAGAN repo metodi orqali ketadi")
    void usesCaseInsensitiveRepositoryCheck() {
        // users jadvalida uq_users_username_lower funksional unikal indeksi bor
        // (LOWER(username) WHERE deleted_at IS NULL): registrga bog'liq existsByUsername
        // "Utkir_Xamdamov" ni ko'rmay o'tib ketardi va saqlashda constraint xatosi chiqardi.
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);

        new LoginNameGenerator(userRepository).generate("Utkir", "Xamdamov");

        verify(userRepository).existsByUsernameIgnoreCase("utkir_xamdamov");
        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    @DisplayName("99 ta variantning hammasi band → Conflict")
    void allSuffixesTaken_throwsConflict() {
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(true);

        LoginNameGenerator gen = new LoginNameGenerator(userRepository);

        assertThatThrownBy(() -> gen.generate("Utkir", "Xamdamov"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Username already exists");
    }
}
