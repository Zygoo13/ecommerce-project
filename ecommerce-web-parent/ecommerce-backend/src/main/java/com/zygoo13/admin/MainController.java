package com.zygoo13.admin;


import com.zygoo13.admin.security.EcommerceUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String viewHomePage(Model model,
                               @AuthenticationPrincipal EcommerceUserDetails loggedUser) {
        if (loggedUser != null) {
            model.addAttribute("fullName", loggedUser.getFullName());
        }
        return "index"; // hoặc homepage của bạn
    }

    @GetMapping("/login")
    public String viewLoginPage() {
        return "login";
    }




}
