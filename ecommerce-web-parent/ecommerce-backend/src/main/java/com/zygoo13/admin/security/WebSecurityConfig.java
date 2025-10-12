package com.zygoo13.admin.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final EcommerceUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/webjars/**", "/images/**", "/css/**", "/js/**", "/fonts/**",
                                "/login", "/error", "/register"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")     // dùng email để đăng nhập
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true) // đăng nhập thành công chuyển về trang chủ
                        .failureUrl("/login?error=true") // đăng nhập thất bại
                        .permitAll() // cho phép tất cả mọi người truy cập trang login
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecret")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                )
                .authenticationProvider(authenticationProvider()); // Cấu hình AuthenticationProvider

        return http.build();
    }

    // Cấu hình AuthenticationProvider sử dụng UserDetailsService và PasswordEncoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); // Sử dụng DaoAuthenticationProvider
        provider.setUserDetailsService(userDetailsService); // Cung cấp UserDetailsService để tải thông tin người dùng
        provider.setPasswordEncoder(passwordEncoder()); // Cung cấp PasswordEncoder để mã hóa và so sánh mật khẩu
        return provider; // Trả về AuthenticationProvider đã cấu hình
    }

    // Cấu hình PasswordEncoder sử dụng BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
