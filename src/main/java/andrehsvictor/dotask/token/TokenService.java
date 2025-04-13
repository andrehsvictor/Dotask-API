package andrehsvictor.dotask.token;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import andrehsvictor.dotask.auth.AuthService;
import andrehsvictor.dotask.exception.InvalidJwtTypeException;
import andrehsvictor.dotask.jwt.JwtService;
import andrehsvictor.dotask.jwt.JwtType;
import andrehsvictor.dotask.revokedtoken.RevokedTokenService;
import andrehsvictor.dotask.token.dto.CredentialsDto;
import andrehsvictor.dotask.token.dto.GetTokenDto;
import andrehsvictor.dotask.token.dto.PostRefreshTokenDto;
import andrehsvictor.dotask.token.dto.RevokeTokenDto;
import andrehsvictor.dotask.user.User;
import andrehsvictor.dotask.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtService jwtService;
    private final AuthService authService;
    private final RevokedTokenService revokedTokenService;
    private final UserService userService;

    public GetTokenDto request(CredentialsDto credentials) {
        authService.authenticate(credentials.getEmail(), credentials.getPassword());
        User user = userService.findByEmail(credentials.getEmail());
        return generateTokenResponse(user.getId().toString());
    }

    public GetTokenDto refresh(PostRefreshTokenDto refreshTokenDto) {
        Jwt jwt = jwtService.decode(refreshTokenDto.getRefreshToken());
        if (jwtService.getTokenType(jwt) != JwtType.REFRESH) {
            throw new InvalidJwtTypeException("Token must be a refresh token");
        }
        revokedTokenService.revoke(jwt);
        return generateTokenResponse(jwt.getSubject());
    }

    public void revoke(RevokeTokenDto revokeTokenDto) {
        Jwt jwt = jwtService.decode(revokeTokenDto.getToken());
        revokedTokenService.revoke(jwt);
    }

    private GetTokenDto generateTokenResponse(String subject) {
        Jwt accessToken = jwtService.issue(subject, JwtType.ACCESS);
        Jwt refreshToken = jwtService.issue(subject, JwtType.REFRESH);

        long expiresIn = calculateExpirationTime(accessToken);

        return GetTokenDto.builder()
                .accessToken(accessToken.getTokenValue())
                .refreshToken(refreshToken.getTokenValue())
                .expiresIn(expiresIn)
                .build();
    }

    private long calculateExpirationTime(Jwt token) {
        return token.getExpiresAt().getEpochSecond() - token.getIssuedAt().getEpochSecond();
    }
}