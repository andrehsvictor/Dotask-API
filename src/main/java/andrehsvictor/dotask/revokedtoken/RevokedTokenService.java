package andrehsvictor.dotask.revokedtoken;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RevokedTokenService {

    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public void revoke(Jwt jwt) {
        UUID jti = UUID.fromString(jwt.getId());
        
        if (revokedTokenRepository.existsByJti(jti)) {
            return;
        }
        
        RevokedToken revokedToken = RevokedToken.builder()
                .jti(jti)
                .expiresAt(toLocalDateTime(jwt.getExpiresAt()))
                .revokedAt(LocalDateTime.now())
                .build();
                
        revokedTokenRepository.save(revokedToken);
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(Jwt jwt) {
        return revokedTokenRepository.existsByJti(UUID.fromString(jwt.getId()));
    }
    
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        revokedTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
    
    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}