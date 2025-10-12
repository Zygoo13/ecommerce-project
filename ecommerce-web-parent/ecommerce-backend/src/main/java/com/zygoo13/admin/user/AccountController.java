package com.zygoo13.admin.user;

import com.zygoo13.admin.security.EcommerceUserDetails;
import com.zygoo13.common.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    @GetMapping
    public String viewDetails(Model model, @AuthenticationPrincipal EcommerceUserDetails loggedUser) {
        User user = userService.getUserById(loggedUser.getUser().getId());
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Account Details");
        return "account_form";
    }

    @PostMapping
    public String saveDetails(@AuthenticationPrincipal EcommerceUserDetails loggedUser,
                              @RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam(required = false) String password,
                              @RequestParam(name = "image", required = false) MultipartFile image,
                              RedirectAttributes ra) {

        User updated = userService.updateAccount(
                loggedUser.getUser().getId(),
                firstName, lastName, password, image
        );

        // Đồng bộ lại principal
        loggedUser.getUser().setFirstName(updated.getFirstName());
        loggedUser.getUser().setLastName(updated.getLastName());
        loggedUser.getUser().setPhotos(updated.getPhotos());

        ra.addFlashAttribute("message", "Tài khoản đã được cập nhật.");
        return "redirect:/account";
    }


}
