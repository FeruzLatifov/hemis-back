package uz.hemis.domain.entity.security;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite primary key for {@link OAuthClientRole} junction entity.
 *
 * <p>Maps to {@code oauth_client_role (client_id, role_id)} composite PK (V003).</p>
 *
 * @since 2.1.0
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OAuthClientRoleId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;
}
