package com.zygoo13.admin.user;


import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserRestController {

    UserService userService;


    @PostMapping("/users/check_email")
    public String checkDuplicateEmail(@Param("email") String email) {
        return userService.isEmailUnique(email) ? "OK" : "Duplicated" ;
    }
}
