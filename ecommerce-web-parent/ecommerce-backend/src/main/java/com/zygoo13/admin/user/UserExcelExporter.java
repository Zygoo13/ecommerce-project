package com.zygoo13.admin.user;

import com.zygoo13.common.entity.Role;
import com.zygoo13.common.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Export danh sách User ra Excel (.xlsx)
 * Yêu cầu dependency:
 *   <dependency>
 *     <groupId>org.apache.poi</groupId>
 *     <artifactId>poi-ooxml</artifactId>
 *     <version>5.2.5</version>
 *   </dependency>
 */
public class UserExcelExporter {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<User> users;

    // Styles dùng lại
    private CellStyle headerStyle;
    private CellStyle bodyStyle;
    private CellStyle bodyStyleAlt;
    private CellStyle wrapStyle;

    public UserExcelExporter(List<User> users) {
        this.users = users;
        workbook = new XSSFWorkbook();
    }

    /** Tạo sheet + styles */
    private void initSheetAndStyles() {
        sheet = workbook.createSheet("Users");

        // Header style
        headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderAll(headerStyle, BorderStyle.THIN);

        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);

        // Body style
        bodyStyle = workbook.createCellStyle();
        setBorderAll(bodyStyle, BorderStyle.HAIR);
        bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Body style alternate (zebra)
        bodyStyleAlt = workbook.createCellStyle();
        setBorderAll(bodyStyleAlt, BorderStyle.HAIR);
        bodyStyleAlt.setVerticalAlignment(VerticalAlignment.CENTER);
        bodyStyleAlt.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        bodyStyleAlt.setFillPattern(FillPatternType.LESS_DOTS); // nhẹ, không quá đậm

        // Wrap text (nếu cần)
        wrapStyle = workbook.createCellStyle();
        setBorderAll(wrapStyle, BorderStyle.HAIR);
        wrapStyle.setWrapText(true);
        wrapStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    private void setBorderAll(CellStyle style, BorderStyle bs) {
        style.setBorderTop(bs);
        style.setBorderRight(bs);
        style.setBorderBottom(bs);
        style.setBorderLeft(bs);
    }

    private void writeHeaderRow() {
        Row row = sheet.createRow(0);
        row.setHeightInPoints(22);

        int col = 0;
        createCell(row, col++, "ID", headerStyle);
        createCell(row, col++, "Email", headerStyle);
        createCell(row, col++, "First Name", headerStyle);
        createCell(row, col++, "Last Name", headerStyle);
        createCell(row, col++, "Roles", headerStyle);
        createCell(row, col++, "Enabled", headerStyle);

        // Freeze header
        sheet.createFreezePane(0, 1);
    }

    private void writeDataRows() {
        int rowIdx = 1;
        for (User u : users) {
            Row row = sheet.createRow(rowIdx);
            row.setHeightInPoints(20);

            // zebra style: hàng chẵn dùng alt
            CellStyle rowStyle = (rowIdx % 2 == 0) ? bodyStyleAlt : bodyStyle;

            int col = 0;
            createCell(row, col++, u.getId(), rowStyle);
            createCell(row, col++, safe(u.getEmail()), rowStyle);
            createCell(row, col++, safe(u.getFirstName()), rowStyle);
            createCell(row, col++, safe(u.getLastName()), rowStyle);

            // roles: join theo tên role
            String rolesJoined = (u.getRoles() == null) ? "" :
                    u.getRoles().stream()
                            .map(Role::getName) // chỉnh theo field thật của bạn
                            .collect(Collectors.joining(", "));
            createCell(row, col++, rolesJoined, wrapStyle);

            createCell(row, col++, (u.isEnabled() ? "Yes" : "No"), rowStyle);

            rowIdx++;
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

    /** Helper tạo cell: tự set type cho Number/Boolean/String */
    private void createCell(Row row, int colIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(colIndex);

        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(String.valueOf(value));
        }

        if (style != null) cell.setCellStyle(style);
    }

    /** Gọi hàm này trong Controller để ghi file xuống response */
    public void export(HttpServletResponse response) throws IOException {
        initSheetAndStyles();
        writeHeaderRow();
        writeDataRows();

        for (int c = 0; c < sheet.getRow(0).getLastCellNum(); c++) {
            sheet.autoSizeColumn(c);
            if (sheet.getColumnWidth(c) > 256 * 60) sheet.setColumnWidth(c, 256 * 60);
        }

        // Thêm timestamp ở đây
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = "users_" + timestamp + ".xlsx";

        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);

        try {
            workbook.write(response.getOutputStream());
        } finally {
            workbook.close();
        }
    }
}
