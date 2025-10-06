package com.zygoo13.admin.user;

import com.zygoo13.common.entity.Role;
import com.zygoo13.common.entity.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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


    // Phân trang và sắp xếp
    public Page<User> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
        // Mặc định sắp xếp theo id
        if (sortField == null || sortField.isEmpty()) {
            sortField = "id";
        }
        // Mặc định sắp xếp tăng dần
        Sort sort = sortDir.equals("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        // Tạo đối tượng Pageable với số trang, số phần tử mỗi trang và thông tin sắp xếp
        PageRequest pageable = PageRequest.of(pageNum, USERS_PER_PAGE, sort);
        // Nếu có từ khóa tìm kiếm, gọi phương thức tìm kiếm
        if(keyword != null && !keyword.isEmpty()) {
            return userRepository.search(keyword, pageable);
        }

        return userRepository.findAll(pageable);
    }

    // Lưu user (tạo mới hoặc cập nhật)
    public User saveUser(User user) {
        boolean isUpdatingUser = (user.getId() != null);

        if (isUpdatingUser) {
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new NoSuchElementException("Could not find any user with ID " + user.getId()));

            // giữ password cũ nếu không nhập mới
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(existingUser.getPassword());
            } else {
                encodePassword(user);
            }

            // 🔒 GIỮ ẢNH CŨ NẾU KHÔNG CÓ ẢNH MỚI/HIDDEN RỖNG
            if (user.getPhotos() == null || user.getPhotos().isBlank()) {
                user.setPhotos(existingUser.getPhotos());
            }
        } else {
            encodePassword(user);
        }

        return userRepository.save(user);
    }


    // Mã hóa mật khẩu
    void encodePassword(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    }

    // Kiểm tra tính duy nhất của email
    public boolean isEmailUnique(Integer id, String email) {
        User existingUser = userRepository.findByEmail(email);
        // Nếu không tìm thấy user nào với email này, tức là email duy nhất
        if (existingUser == null) {
            return true;
        }
        // Nếu tìm thấy user với email này, kiểm tra xem có phải là chính user đang cập nhật không
        if (id == null) {
            return false;
        }

        return existingUser.getId().equals(id);
    }

    // Lấy thông tin user theo ID
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Could not find any user with ID " + id));
    }

    // Xóa user theo ID
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    // Cập nhật trạng thái kích hoạt user
    public void updateUserEnabledStatus(Integer id, boolean enabled) {
        User user = getUserById(id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }


}
