package com.zygoo13.admin.user;


import com.zygoo13.common.entity.Role;
import com.zygoo13.common.entity.User;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    TestEntityManager testEntityManager;

    @Test
    public void testCreateUser() {
        Role roleAdmin = testEntityManager.find(Role.class, 1);
        User user = new User("anh01@gmail.com", "anh01", "Anh", "Hoang");
        user.addRole(roleAdmin);

        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isGreaterThan(0);
    }

    @Test
    public void testCreateNewUserWithTwoRoles() {
        User user = new User("anh02@gmail.com", "anh02", "Anh", "Tran");
        Role roleEditor = new Role(3);
        Role roleAssistant = new Role(5);

        user.addRole(roleEditor);
        user.addRole(roleAssistant);

        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isGreaterThan(0);
    }

    @Test
    public void testListAllUsers(){
        Iterable<User> listUsers = userRepository.findAll();
        listUsers.forEach(System.out::println);
    }

    @Test
    public void testUserById(){
        User user = userRepository.findById(1).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println(user);
        assertThat(user).isNotNull();
    }

    @Test
    public void testUpdateUserDetails() {
        User user = userRepository.findById(1).orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        user.setEmail("anh001@gmail.com");
        userRepository.save(user);
    }

    @Test
    public void testUpdateUserRoles() {
        User user = userRepository.findById((2)).orElseThrow(() -> new RuntimeException("User not found"));
        Role roleEditor = new Role(3);
        Role roleSalesperson = new Role(2);
        user.getRoles().remove(roleEditor);
        user.addRole(roleSalesperson);
    }

    @Test
    public void testDeleteUser() {
        Integer id = 2;
        userRepository.deleteById(id);
    }

    @Test
    public void testGetUserByEmail() {
        String email = "anh001@gmail.com";
        User user = userRepository.findByEmail(email);
        assertThat(user).isNotNull();
    }
}
