package com.zygoo13.admin.user;

import com.zygoo13.common.entity.Role;
import com.zygoo13.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    List<Role> findByName(String name);
}
