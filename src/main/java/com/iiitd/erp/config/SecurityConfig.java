package com.iiitd.erp.config;

import com.iiitd.erp.persistent.entity.auth_db.UsersAuth;
import com.iiitd.erp.persistent.persistence.repository.UsersAuthRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(12); }

  @Bean
  UserDetailsService userDetailsService(UsersAuthRepository repo){
    return username -> {
      UsersAuth u = repo.findByUsername(username)
          .orElseThrow(() -> new UsernameNotFoundException("No such user"));
      if (!"active".equalsIgnoreCase(u.getStatus())) throw new DisabledException("User inactive/locked");
      String role = u.getRole().toUpperCase();
      if (!role.equals("ADMIN") && !role.equals("INSTRUCTOR") && !role.equals("STUDENT")) {
        throw new UsernameNotFoundException("Wrong userRole: " + role);
      }
      var auths = List.of(new SimpleGrantedAuthority(u.getRole().toUpperCase()));
      return User.withUsername(u.getUsername())
          .password(u.getPasswordHash())
          .authorities(auths)
          .build();
    };
  }

  @Bean
  DaoAuthenticationProvider authenticationProvider(UserDetailsService uds, PasswordEncoder enc){
    DaoAuthenticationProvider p = new DaoAuthenticationProvider();
    p.setUserDetailsService(uds);
    p.setPasswordEncoder(enc);
    return p;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      // .requiresChannel(ch -> ch.anyRequest().requiresSecure()) // enable behind HTTPS
      .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/login", "/api/auth/logout"))
      .sessionManagement(sm -> sm.sessionFixation().migrateSession())
            .securityContext(sc -> sc.requireExplicitSave(false))
            .authorizeHttpRequests(reg -> reg
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/instructor/**").hasRole("INSTRUCTOR")
        .requestMatchers("/api/student/**").hasRole("STUDENT")
        .anyRequest().authenticated()
      )
            .exceptionHandling(eh -> eh
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                      response.setStatus(403);
                      response.setContentType("application/json");
                      response.getWriter().write("{\"error\": \"Wrong userRole\"}");
                    })
            );
    return http.build();
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
    return cfg.getAuthenticationManager();
  }
}
