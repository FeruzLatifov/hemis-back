package uz.hemis.security.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.hemis.common.auth.AccessScope;
import uz.hemis.common.auth.ScopeResolver;
import uz.hemis.common.auth.SubjectInfo;
import uz.hemis.common.auth.UserScopeData;
import uz.hemis.common.enums.UserType;
import uz.hemis.common.port.security.UserScopeLoadingPort;

/**
 * Default {@link ScopeResolver} — derives the caller's OTM {@link AccessScope} server-side.
 *
 * <p>This is the keystone of the cross-OTM IDOR fix: the human web token carries no
 * {@code university_code} claim, so a human user's scope MUST be resolved from the DB
 * ({@code users.university_id} + {@code user_type}) rather than trusted from the token or a
 * request parameter. Machine (CLIENT) tokens keep their inline tenancy claim.</p>
 *
 * <p><strong>Fail-closed policy:</strong></p>
 * <table>
 *   <caption>tier → scope</caption>
 *   <tr><td>USER, UNIVERSITY, code present</td><td>{@code restrictedTo(code)}</td></tr>
 *   <tr><td>USER, UNIVERSITY, code null</td><td>{@code denyAll} (misconfigured OTM user)</td></tr>
 *   <tr><td>USER, MINISTRY / SYSTEM</td><td>{@code global}</td></tr>
 *   <tr><td>USER, ORGANIZATION</td><td>{@code denyAll} (org tenancy not modelled yet)</td></tr>
 *   <tr><td>USER not found / null type / anonymous</td><td>{@code denyAll}</td></tr>
 *   <tr><td>CLIENT with university_code</td><td>{@code restrictedTo(code)}</td></tr>
 *   <tr><td>CLIENT without university_code</td><td>{@code global} (central/internal machine)</td></tr>
 * </table>
 *
 * <p>The permission dimension ({@code @PreAuthorize(hasAuthority(...))}) is enforced separately; this
 * only answers "which OTM's rows". Effective access = permission AND scope.</p>
 *
 * <p>NOTE: {@link UserScopeLoadingPort#loadScope} is currently an uncached per-request projection read
 * (indexed by PK). Caching (Caffeine L1 + Redis, evict on user role/status/university change) is the
 * next optimisation — see task "ScopeResolver … (cached)". Correctness (fail-closed) ships first.</p>
 *
 * @since 2.2.0
 */
@Component("scopeResolver")
@RequiredArgsConstructor
@Slf4j
public class DefaultScopeResolver implements ScopeResolver {

    private final CurrentSubjectHelper currentSubjectHelper;
    private final UserScopeLoadingPort userScopeLoadingPort;

    @Override
    public AccessScope currentScope() {
        SubjectInfo subject = currentSubjectHelper.current().orElse(null);
        if (subject == null) {
            return AccessScope.denyAll();
        }
        if (subject.isClient()) {
            return scopeForClient(subject);
        }
        return scopeForUser(subject);
    }

    /**
     * Machine tokens carry their tenancy inline (issued and signed centrally). A UNIVERSITY_BACKEND
     * client is bound to one OTM; central/internal machines (MyGov, sync services) have no code and
     * are trusted broad. TODO(R7): tighten EXTERNAL_SYSTEM clients that legitimately need no cross-OTM read.
     */
    private AccessScope scopeForClient(SubjectInfo subject) {
        String code = subject.universityCode();
        return (code == null || code.isBlank())
                ? AccessScope.global()
                : AccessScope.restrictedTo(code);
    }

    /** Humans: resolve from the DB — the token has no reliable scope claim. */
    private AccessScope scopeForUser(SubjectInfo subject) {
        UserScopeData data = userScopeLoadingPort.loadScope(subject.id()).orElse(null);
        if (data == null || data.userType() == null) {
            log.debug("Scope deny-all: unresolved user {}", subject.id());
            return AccessScope.denyAll();
        }
        UserType type = data.userType();
        return switch (type) {
            case MINISTRY, SYSTEM -> AccessScope.global();
            case UNIVERSITY -> AccessScope.restrictedTo(data.universityCode());
            case ORGANIZATION -> AccessScope.denyAll();
        };
    }
}
