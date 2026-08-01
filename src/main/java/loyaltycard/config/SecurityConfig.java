package loyaltycard.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenAuthFilter tokenAuthFilter;

    /*
     * Custom UserDetailsService bean to disable Spring Boot's default in-memory user.
     *
     * <p>By throwing a UsernameNotFoundException for any username, this bean prevents Spring Security
     * from creating a default user with a generated password during development.
     */
    @Bean
    UserDetailsService emptyDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("No local users, only JWT tokens allowed!");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity
                .securityContext(
                        securityContext ->
                                securityContext
                                        .requireExplicitSave(true)
                                        .securityContextRepository(
                                                new DelegatingSecurityContextRepository(
                                                        new RequestAttributeSecurityContextRepository(),
                                                        new HttpSessionSecurityContextRepository())))
                .authorizeHttpRequests(
                        requests ->
                                requests
                                        .requestMatchers(
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**",
                                                "/login",
                                                "/sign-up",
                                                "/admins/login")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .cors(
                        cors ->
                                cors.configurationSource(
                                        _ -> {
                                            CorsConfiguration configuration = new CorsConfiguration();
                                            configuration.setAllowedOrigins(List.of("*"));
                                            configuration.setAllowedMethods(List.of("*"));
                                            configuration.setAllowedHeaders(List.of("*"));
                                            return configuration;
                                        }))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                        e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}