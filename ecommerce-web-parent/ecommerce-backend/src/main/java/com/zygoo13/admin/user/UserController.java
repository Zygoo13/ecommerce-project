package com.zygoo13.admin.user;

import com.zygoo13.admin.FileUploadUtil;
import com.zygoo13.common.entity.Role;
import com.zygoo13.common.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    // Mặc định hiển thị trang đầu tiên
    @GetMapping("/users")
    public String listFirstPage(Model model) {
        return showUserPage(1, "firstName", "asc", null, model);
    }


    // Hiển thị form tạo user mới
    @GetMapping("/users/new")
    public String newUser(Model model) {
        List<Role> listRoles = userService.getAllRoles();
        User user = new User();
        model.addAttribute("user", user);
        model.addAttribute("listRoles", listRoles);
        model.addAttribute("pageTitle", "Create New User");
        return "user_form";
    }


    // Xử lý lưu user
    @PostMapping("/users/save")
    public String saveUser(
            User user,
            // MultipartFile để nhận file upload
            RedirectAttributes redirectAttributes,
            // Tên "image" phải trùng với thẻ <input type="file" name="image">
            @RequestParam("image") MultipartFile multipartFile) throws IOException {

        // Xử lý ảnh upload
        if (!multipartFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(
                    Objects.requireNonNull(multipartFile.getOriginalFilename()));
            user.setPhotos(fileName);

            // Lưu user trước để lấy ID
            User savedUser = userService.saveUser(user);

            // Thư mục lưu file theo id user
            String uploadDir = System.getProperty("user.dir") + "/ecommerce-web-parent/ecommerce-backend/user-photos/" + savedUser.getId();

            FileUploadUtil.cleanDir(uploadDir);
            FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
        } else {
            // Nếu không upload file, giữ nguyên ảnh cũ (nếu có)
            if (user.getPhotos() != null && user.getPhotos().isEmpty()) {
                user.setPhotos(null);
            }
            userService.saveUser(user);
        }

        redirectAttributes.addFlashAttribute("message", "The user has been saved successfully.");
        return "redirect:/users";
    }


    // Hiển thị form chỉnh sửa user
    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable(name = "id") Integer id,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            model.addAttribute("user", user);
            List<Role> listRoles = userService.getAllRoles();
            model.addAttribute("listRoles", listRoles);
            model.addAttribute("pageTitle", "Edit User (ID: " + id + ")");
            return "user_form";
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "redirect:/users";
        }
    }


    // Xử lý xóa user
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable(name = "id") Integer id,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "The user ID " + id + " has been deleted successfully.");
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/users";
    }


    // Xử lý thay đổi trạng thái kích hoạt user
    @GetMapping("/users/{id}/enabled/{status}")
    public String updateUserEnabledStatus(@PathVariable("id") Integer id,
                                          @PathVariable("status") boolean enabled,
                                          RedirectAttributes redirectAttributes) {
        userService.updateUserEnabledStatus(id, enabled);
        String status = enabled ? "enabled" : "disabled";
        String message = "The user ID " + id + " has been " + status;
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/users";
    }


    // Hiển thị danh sách user với phân trang, sắp xếp, và tìm kiếm
    private String showUserPage(int pageNum, String sortField, String sortDir, String keyword, Model model) {
        Page<User> page = userService.listByPage(pageNum - 1, sortField, sortDir, keyword);
        List<User> listUsers = page.getContent();

        // Tính toán các thông tin phân trang
        long startCount = (long) (pageNum - 1) * UserService.USERS_PER_PAGE + 1;
        long endCount = startCount + UserService.USERS_PER_PAGE - 1;
        if (endCount > page.getTotalElements()) endCount = page.getTotalElements();

        // Đảo chiều sắp xếp
        String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";

        // Truyền dữ liệu đến view
        model.addAttribute("startCount", startCount);
        model.addAttribute("endCount", endCount);
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("listUsers", listUsers);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", reverseSortDir);
        model.addAttribute("keyword", keyword);

        return "users";
    }

    // Xử lý yêu cầu phân trang
    @GetMapping("/users/page/{pageNum}")
    public String listByPage(
            @PathVariable(name = "pageNum") int pageNum,
            @RequestParam(value = "sortField", required = false, defaultValue = "id") String sortField,
            @RequestParam(value = "sortDir", required = false, defaultValue = "asc") String sortDir,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        return showUserPage(pageNum, sortField, sortDir, keyword, model);
    }


    @GetMapping("/users/export/csv")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        // Thiết lập định dạng phản hồi
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "users_" + timestamp + ".csv";
        String headerValue = "attachment; filename=" + fileName;
        response.setHeader("Content-Disposition", headerValue);

        List<User> listUsers = userService.getAllUsers().stream()
                .sorted(Comparator.comparing(User::getFirstName))
                .toList();

        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                .setHeader("User ID", "Email", "First Name", "Last Name", "Roles", "Enabled")
                .get();

        try (PrintWriter writer = response.getWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {

            for (User user : listUsers) {
                csvPrinter.printRecord(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRoles().stream().map(Role::getName).toList(), // Format roles as a list of names
                    user.isEnabled()
                );
            }
        }
    }

    // Xuất danh sách user ra file Excel
    @GetMapping("/users/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<User> listUsers = userService.getAllUsers().stream()
                .sorted(Comparator.comparing(User::getFirstName))
                .toList();

        UserExcelExporter excelExporter = new UserExcelExporter(listUsers);
        excelExporter.export(response);
    }

    // Xuất danh sách user ra file PDF
    @GetMapping("/users/export/pdf")
    public void exportUsersToPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        response.setHeader("Content-Disposition", "attachment; filename=users_" + timestamp + ".pdf");

        List<User> list = userService.getAllUsers()
                .stream().sorted(Comparator.comparing(User::getFirstName)).toList();

        new UserPdfExporter(list).export(response);
    }



}
