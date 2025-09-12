package com.zygoo13.admin.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig{

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception  {
        httpSecurity.authorizeHttpRequests(req -> req
                .anyRequest().permitAll()
        );

    return httpSecurity.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

}


























//@Configuration
//@EnableWebSecurity
//public class WebSecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//            .authorizeHttpRequests(req -> req
//                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll() // public endpoints
//                .anyRequest().authenticated() // còn lại phải login
//            )
//            .formLogin(form -> form
//                .loginPage("/login")      // custom login page
//                .defaultSuccessUrl("/users", true) // sau khi login chuyển về
//                .permitAll()
//            )
//            .logout(logout -> logout
//                .logoutUrl("/logout")
//                .logoutSuccessUrl("/login?logout")
//                .permitAll()
//            )
//            .csrf(csrf -> csrf.disable()); // tùy bạn, nếu để test thì disable, còn prod thì nên bật
//
//        return http.build();
//    }
//}
