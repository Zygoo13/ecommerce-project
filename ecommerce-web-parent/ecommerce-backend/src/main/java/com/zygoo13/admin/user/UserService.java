package com.zygoo13.admin.user;


import com.zygoo13.common.entity.Role;
import com.zygoo13.common.entity.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public List<Role> getAllRoles(){
        return roleRepository.findAll();
    }

    public void saveUser(User user) {
        boolean isUpdatingUser = (user.getId() != null);

        if (isUpdatingUser) {
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Could not find any user with ID " + user.getId()));

            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(existingUser.getPassword()); // giữ nguyên pass cũ
            } else {
                encodePassword(user); // encode pass mới
            }
        } else {
            encodePassword(user); // user mới thì luôn encode
        }

        userRepository.save(user);
    }


    void encodePassword(User user){
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    }

    public boolean isEmailUnique(Integer id, String email) {
        User existingUser = userRepository.findByEmail(email);

        if (existingUser == null) {
            return true; // chưa có ai dùng email này
        }

        // Nếu đang tạo mới user mà email đã tồn tại
        if (id == null) {
            return false;
        }

        // Nếu đang edit user mà email thuộc về user khác
        return existingUser.getId().equals(id);
    }


    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Could not find any user with ID " + id));
    }



}
