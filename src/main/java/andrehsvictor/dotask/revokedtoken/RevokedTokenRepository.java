package andrehsvictor.dotask.revokedtoken;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {

    boolean existsByJti(UUID jti);

    @Modifying
    int deleteByExpiresAtBefore(LocalDateTime expiresAt);

}
