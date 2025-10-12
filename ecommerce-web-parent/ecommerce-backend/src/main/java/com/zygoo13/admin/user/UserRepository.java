package com.zygoo13.admin.user;

import com.zygoo13.common.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Tìm kiếm user theo trạng thái enabled
    List<User> findByEnabled(boolean enabled);

    // Kiểm tra sự tồn tại của email (dùng để validate email trùng lặp)
    boolean existsByEmail(String email);

    // Tìm user theo email (dùng để đăng nhập)
    User findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailOption(@Param("email") String email);


    // Tìm kiếm user theo từ khóa (tên, họ, email) với phân trang
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))") // JPQL
    Page<User> search(@Param("keyword") String keyword, Pageable pageable);



}
