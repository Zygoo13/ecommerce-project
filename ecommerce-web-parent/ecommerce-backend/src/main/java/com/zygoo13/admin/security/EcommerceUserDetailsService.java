package com.zygoo13.admin.security;

import com.zygoo13.admin.user.UserRepository;
import com.zygoo13.common.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EcommerceUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOption(email)
                .orElseThrow(() -> new UsernameNotFoundException("Could not find user with email: " + email));
        return new EcommerceUserDetails(user);
    }
}
