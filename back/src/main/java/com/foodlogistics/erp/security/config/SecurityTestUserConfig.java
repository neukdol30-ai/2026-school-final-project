package com.foodlogistics.erp.security.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@Profile("in-memory-security-test")
public class SecurityTestUserConfig {

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder
    ) {

        UserDetails owner = User.builder()
                .username("owner")
                .password(passwordEncoder.encode("owner1234!"))
                .authorities(
                        "ROLE_OWNER",
                        "SYSTEM_ACCESS"
                )
                .build();

        return new InMemoryUserDetailsManager(owner);

    }
}
