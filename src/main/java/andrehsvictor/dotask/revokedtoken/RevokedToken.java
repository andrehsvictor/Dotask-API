package andrehsvictor.dotask.revokedtoken;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "revoked_tokens")
public class RevokedToken implements Serializable {

    private static final long serialVersionUID = 842414051409323194L;

    @Id
    private UUID jti;

    private LocalDateTime revokedAt;
    private LocalDateTime expiresAt;

    @PrePersist
    public void prePersist() {
        this.revokedAt = LocalDateTime.now();
    }

}
