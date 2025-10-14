package tech.cusbo.msteams.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final OnLoginSubscribeToEventsHandler subscribeToEventsHandler;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf()
        .disable()
        .authorizeRequests()
        .antMatchers("/api/graph/webhook/events", "/api/graph/webhook/lifecycle").permitAll()
        .antMatchers("/", "/login**", "/error").permitAll()
        .anyRequest().authenticated()
        .and()
        .oauth2Login()
        .defaultSuccessUrl("/auth/logged-in", true)
        .and()
        .logout()
        .logoutSuccessUrl("/")
        .and()
        .oauth2Login(oauth -> oauth
            .successHandler(subscribeToEventsHandler)
        );
  }
}