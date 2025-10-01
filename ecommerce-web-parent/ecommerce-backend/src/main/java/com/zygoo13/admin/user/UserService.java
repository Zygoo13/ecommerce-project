package com.zygoo13.admin.user;

import com.zygoo13.common.entity.Role;
import com.zygoo13.common.entity.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    public static final int USERS_PER_PAGE = 4;

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // UserService.java - Đã chỉnh sửa

    public Page<User> listByPage(int pageNum, String sortField, String sortDir) {
        // Bảo vệ khỏi lỗi: Dù đã fix ở Controller, code Service vẫn nên tự bảo vệ
        if (sortField == null || sortField.isEmpty()) {
            sortField = "id"; // Giá trị mặc định cuối cùng
        }

        // Tạo đối tượng Sort an toàn và gọn gàng hơn
        Sort sort = sortDir.equals("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        // Spring Data Paging (PageRequest) cần trang 0-index.
        PageRequest pageable = PageRequest.of(pageNum, USERS_PER_PAGE, sort);

        return userRepository.findAll(pageable);
    }

    /** Lưu user và trả về entity đã lưu */
    public User saveUser(User user) {
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

        return userRepository.save(user);
    }

    void encodePassword(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    }

    public boolean isEmailUnique(Integer id, String email) {
        User existingUser = userRepository.findByEmail(email);

        if (existingUser == null) {
            return true; // chưa có ai dùng email này
        }

        if (id == null) {
            return false; // đang tạo mới user mà email đã tồn tại
        }

        return existingUser.getId().equals(id);
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Could not find any user with ID " + id));
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public void updateUserEnabledStatus(Integer id, boolean enabled) {
        User user = getUserById(id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }


}
