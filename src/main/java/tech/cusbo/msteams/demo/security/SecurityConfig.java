package tech.cusbo.msteams.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import tech.cusbo.msteams.demo.inboundevent.subscription.OnLoginSubscribeToEventsHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final OnLoginSubscribeToEventsHandler subscribeToEventsHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/graph/webhook/events",
                "/api/graph/webhook/lifecycle",
                "/subscriptions/ensure",
                "/subscriptions/app",
                "/subscriptions/app/**",
                "/", "/login**", "/error"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .logout(logout -> logout
            .logoutSuccessUrl("/")
        )
        .oauth2Login(oauth -> oauth
            .successHandler(subscribeToEventsHandler)
        );
    return http.build();
  }
}