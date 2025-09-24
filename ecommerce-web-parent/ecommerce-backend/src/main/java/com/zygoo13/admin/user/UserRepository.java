package com.zygoo13.admin.user;

import com.zygoo13.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByEnabled(boolean enabled);
    boolean existsByEmail(String email);
    User findByEmail(String email);

}
