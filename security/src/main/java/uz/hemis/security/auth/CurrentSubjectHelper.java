package uz.hemis.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.hemis.common.auth.ClientType;
import uz.hemis.common.auth.SubjectInfo;
import uz.hemis.common.auth.SubjectType;

import java.util.Optional;
import java.util.UUID;

/**
 * SpEL helper bean — exposed as {@code @subject} for use in
 * {@code @PreAuthorize} / {@code @PostAuthorize} expressions.
 *
 * <p>Usage:</p>
 * <pre>
 * &#064;PreAuthorize("@subject.hasAuthority('students.view')")
 * public StudentDto getStudent(UUID id) { … }
 *
 * &#064;PreAuthorize("@subject.isClient() and @subject.ofType('UNIVERSITY_BACKEND')")
 * public List&lt;StudentDto&gt; bulkSync() { … }
 * </pre>
 *
 * <p>Works for both USER and CLIENT subjects transparently. Returns {@code false}
 * (not null / exception) when no authentication is present, so expressions behave
 * consistently for unauthenticated requests.</p>
 *
 * @since 2.1.0
 */
@Component("subject")
@RequiredArgsConstructor
public class CurrentSubjectHelper {

    private final SubjectResolver subjectResolver;

    /**
     * @return current {@link SubjectInfo}, or {@code Optional.empty()} when anonymous
     */
    public Optional<SubjectInfo> current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return subjectResolver.resolve(authentication);
    }

    public boolean hasAuthority(String authority) {
        return current().map(s -> s.hasAuthority(authority)).orElse(false);
    }

    public boolean hasAnyAuthority(String... authorities) {
        return current().map(s -> s.hasAnyAuthority(authorities)).orElse(false);
    }

    public boolean isUser() {
        return current().map(SubjectInfo::isUser).orElse(false);
    }

    public boolean isClient() {
        return current().map(SubjectInfo::isClient).orElse(false);
    }

    /**
     * Discriminator check for machine subjects: {@code #subject.ofType('UNIVERSITY_BACKEND')}.
     */
    public boolean ofType(String clientType) {
        if (clientType == null) return false;
        return current()
                .filter(SubjectInfo::isClient)
                .map(SubjectInfo::clientType)
                .map(ClientType::name)
                .map(clientType::equals)
                .orElse(false);
    }

    /**
     * Tenant-scoped check: {@code #subject.ownsUniversity(#universityCode)}.
     * Returns {@code false} for SYSTEM / ministry users (they aren't tenant-scoped).
     */
    public boolean ownsUniversity(String universityCode) {
        if (universityCode == null) return false;
        return current()
                .map(SubjectInfo::universityCode)
                .map(universityCode::equals)
                .orElse(false);
    }

    /** @return current subject UUID, or {@code null} when anonymous */
    public UUID id() {
        return current().map(SubjectInfo::id).orElse(null);
    }

    /** @return current subject type, or {@code null} when anonymous */
    public SubjectType type() {
        return current().map(SubjectInfo::type).orElse(null);
    }
}
