package uz.hemis.service.audit;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.hemis.domain.repository.UserRepository;

/**
 * Turns audit stamps ({@code deleted_by} / {@code created_by} / {@code updated_by}) into something a
 * person can read.
 *
 * <p>The stamp written by the application is {@code Authentication#getName()}, and the JWT principal
 * claim is {@code sub} ({@code SecurityConfig}) which carries the user's <strong>UUID</strong> — so a
 * column rendered straight from the stamp shows a raw id (that is exactly what the "Kim o'chirdi"
 * column of the recycle bins used to show). One batch lookup maps those UUIDs to a name; anything
 * that is not a UUID ({@code "system"}, a machine client id) and any id whose {@code users} row is
 * gone stays as it is, because a raw stamp is still better than an empty cell.</p>
 *
 * <p>Shared on purpose: the university bin and the speciality bin resolve the identical stamp, and
 * two private copies of this logic had already drifted apart once.</p>
 *
 * @since 2.2.0
 */
@Component
@RequiredArgsConstructor
public class ActorNameResolver {

    private final UserRepository userRepository;

    /**
     * Batch-resolve audit stamps to display labels.
     *
     * @param stamps raw stamps, nulls and duplicates tolerated
     * @return stamp → label, containing only the stamps that resolved to a live user; callers render
     *         an unresolved stamp verbatim ({@code map.getOrDefault(stamp, stamp)}, guarding null)
     */
    public Map<String, String> resolve(Collection<String> stamps) {
        if (stamps == null || stamps.isEmpty()) {
            return Map.of();
        }
        var ids = stamps.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(ActorNameResolver::asUuidOrNull)
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<String, String> byId = new LinkedHashMap<>();
        for (UserRepository.DisplayName u : userRepository.findDisplayNamesByIds(ids)) {
            String label = u.getFullName() != null && !u.getFullName().isBlank()
                    ? u.getFullName()
                    : u.getUsername();
            byId.put(u.getId().toString(), label);
        }
        return byId;
    }

    /** Label for one stamp; a null stamp stays null, an unresolvable stamp is shown verbatim. */
    public static String label(String stamp, Map<String, String> resolved) {
        // getOrDefault would NPE on a null stamp: Map.of() rejects null keys.
        return stamp == null ? null : resolved.getOrDefault(stamp, stamp);
    }

    private static UUID asUuidOrNull(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;   // "system", a client id, or anything else non-UUID — shown verbatim
        }
    }
}
