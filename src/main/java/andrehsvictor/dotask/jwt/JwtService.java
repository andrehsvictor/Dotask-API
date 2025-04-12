package andrehsvictor.dotask.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import andrehsvictor.dotask.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;

    @Value("${security.token.jwt.audience}")
    private String audience;

    @Value("${security.token.access.lifespan}")
    private Duration accessTokenLifespan;

    @Value("${security.token.refresh.lifespan}")
    private Duration refreshTokenLifespan;

    @Value("${security.token.jwt.issuer:#{T(org.springframework.web.servlet.support.ServletUriComponentsBuilder).fromCurrentContextPath().toUriString()}}")
    private String issuer;

    public Jwt issue(String subject, JwtType type) {
        return switch (type) {
            case ACCESS -> issueToken(subject, "access", accessTokenLifespan);
            case REFRESH -> issueToken(subject, "refresh", refreshTokenLifespan);
        };
    }

    public Jwt issue(Jwt existingToken, JwtType type) {
        return issue(existingToken.getSubject(), type);
    }

    private Jwt issueToken(String subject, String tokenType, Duration lifespan) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(lifespan);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .audience(List.of(audience))
                .issuedAt(now)
                .issuer(getIssuer())
                .claim("type", tokenType)
                .expiresAt(expiresAt)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims));
    }

    private String getIssuer() {
        if (issuer == null || issuer.isEmpty()) {
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .toUriString();
        }
        return issuer;
    }

    public JwtType getTokenType(Jwt token) {
        String type = token.getClaimAsString("type");
        return switch (type) {
            case "access" -> JwtType.ACCESS;
            case "refresh" -> JwtType.REFRESH;
            default -> throw new IllegalArgumentException("Invalid token type: " + type);
        };
    }

    public Jwt decode(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }
}