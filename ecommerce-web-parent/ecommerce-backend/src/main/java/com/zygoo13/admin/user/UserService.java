package com.zygoo13.admin.user;

import com.zygoo13.common.entity.Role;
import com.zygoo13.common.entity.User;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

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

    @Transactional
    public User updateAccount(Integer userId,
                              String firstName,
                              String lastName,
                              String rawPassword,
                              MultipartFile image) {

        User existing = getUserById(userId); // throw nếu không tồn tại

        // --- chỉ cập nhật field được phép ---
        existing.setFirstName(firstName);
        existing.setLastName(lastName);

        if (rawPassword != null && !rawPassword.isBlank()) {
            existing.setPassword(passwordEncoder.encode(rawPassword));
        }

        // Ảnh đại diện: chỉ set khi có upload mới
        if (image != null && !image.isEmpty()) {
            String original = org.springframework.util.StringUtils
                    .cleanPath(Objects.requireNonNull(image.getOriginalFilename()));

            if (original.contains("..")) {
                throw new IllegalArgumentException("Invalid file name.");
            }

            // Đặt fileName đơn giản (giữ lại tên) để đường dẫn hiển thị nhất quán
            // Nếu muốn tránh trùng, có thể thêm tiền tố id/timestamp, nhưng vì ta cleanDir trước nên an toàn.
            String fileName = original;

            // Dùng cùng baseDir với luồng Edit User
            String uploadDir = System.getProperty("user.dir")
                    + "/ecommerce-web-parent/ecommerce-backend/user-photos/" + existing.getId();

            try {
                // Chỉ giữ 1 ảnh duy nhất
                com.zygoo13.admin.FileUploadUtil.cleanDir(uploadDir);
                com.zygoo13.admin.FileUploadUtil.saveFile(uploadDir, fileName, image);
            } catch (Exception e) {
                throw new RuntimeException("Could not store image file. Error: " + e.getMessage(), e);
            }

            existing.setPhotos(fileName);
        }
        // else: giữ nguyên existing.getPhotos()

        return userRepository.save(existing);
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
