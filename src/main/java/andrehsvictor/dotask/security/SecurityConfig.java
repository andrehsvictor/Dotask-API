package andrehsvictor.dotask.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import andrehsvictor.dotask.jwt.JwtTypeFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/token",
            "/api/v1/token/refresh",
            "/api/v1/token/revoke",
    };

    private static final String[] PUBLIC_POST_ENDPOINTS = {
            "/api/v1/users"
    };

    private static final String[] ACCOUNT_MANAGEMENT_ENDPOINTS = {
            "/api/v1/users/email/verify",
            "/api/v1/users/password/reset",
            "/api/v1/users/send-action-email",
    };

    private static final String[] DOCUMENTATION_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Value("${security.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${security.cors.allowed-methods}")
    private String[] allowedMethods;

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTypeFilter jwtTypeFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtTypeFilter jwtTypeFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtTypeFilter = jwtTypeFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                        .requestMatchers(ACCOUNT_MANAGEMENT_ENDPOINTS).permitAll()
                        .requestMatchers(DOCUMENTATION_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtAuthenticationConverter())))
                .addFilterBefore(jwtTypeFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (allowedOrigins != null && allowedOrigins.length > 0 && !allowedOrigins[0].equals("*")) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        } else {
            configuration.addAllowedOriginPattern("*");
        }

        if (allowedMethods != null && allowedMethods.length > 0 && !allowedMethods[0].equals("*")) {
            configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        } else {
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        }

        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}