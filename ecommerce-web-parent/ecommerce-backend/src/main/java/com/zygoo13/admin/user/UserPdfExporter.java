package com.zygoo13.admin.user;

import com.zygoo13.common.entity.Role;
import com.zygoo13.common.entity.User;
import jakarta.servlet.http.HttpServletResponse;

import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;

import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Export Users -> PDF (OpenPDF 3.0.0).
 * - Font Unicode (NotoSans) được embed để hiển thị tiếng Việt.
 * - Tiêu đề, header bảng có nền, zebra rows, footer số trang.
 * - KHÔNG set Content-Type/Content-Disposition (controller kiểm soát).
 */
public class UserPdfExporter {

    private final List<User> users;

    // Fonts
    private Font fontTitle;
    private Font fontHeader;
    private Font fontBody;

    public UserPdfExporter(List<User> users) {
        this.users = users;
    }

    public void export(HttpServletResponse response) throws IOException {
        // A4 landscape, chừa top để header/tiêu đề thoáng
        Rectangle pageSize = PageSize.A4.rotate();
        Document document = new Document(pageSize, 36, 36, 54, 36);
        PdfWriter writer;
        try {
            writer = PdfWriter.getInstance(document, response.getOutputStream());
        } catch (DocumentException e) {
            throw new IOException("Cannot init PDF writer", e);
        }
        writer.setPageEvent(new FooterPageEvent());

        // init fonts trước khi open()
        initFonts();

        document.open();

        addTitle(document);
        addTable(document);

        document.close();
    }

    /* -------------------- UI helpers -------------------- */

    private void initFonts() throws IOException {
        // thử cả 2 chỗ: /static/fonts và /fonts
        InputStream is = getClass().getResourceAsStream("/static/fonts/NotoSans-Regular.ttf");
        if (is == null) {
            is = getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf");
        }

        String alias = "NotoSans";

        if (is != null) {
            // Ghi file tạm để FontFactory đọc được
            java.io.File tempFontFile = java.io.File.createTempFile("NotoSans", ".ttf");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFontFile)) {
                is.transferTo(fos);
            }

            // Đăng ký font theo đường dẫn file tạm
            FontFactory.register(tempFontFile.getAbsolutePath(), alias);

            fontTitle  = FontFactory.getFont(alias, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 16f, Font.BOLD, new Color(33,37,41));
            fontHeader = FontFactory.getFont(alias, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 11f, Font.BOLD, Color.WHITE);
            fontBody   = FontFactory.getFont(alias, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 11f, Font.NORMAL, new Color(33,37,41));

            // Xóa file tạm khi JVM tắt
            tempFontFile.deleteOnExit();
            return;
        }

        // fallback nếu không tìm thấy font
        fontTitle  = FontFactory.getFont("Helvetica", 16f, Font.BOLD, new Color(33,37,41));
        fontHeader = FontFactory.getFont("Helvetica", 11f, Font.BOLD, Color.WHITE);
        fontBody   = FontFactory.getFont("Helvetica", 11f, Font.NORMAL, new Color(33,37,41));
    }


    private void addTitle(Document document) throws IOException {
        Paragraph title = new Paragraph();
        title.setSpacingAfter(8f);
        title.add(new Chunk("Users Report", fontTitle));

        Paragraph subtitle = new Paragraph();
        subtitle.setSpacingAfter(10f);
        subtitle.add(new Chunk("Total: " + users.size() + " users", fontBody));

        try {
            document.add(title);
            document.add(subtitle);
        } catch (DocumentException e) {
            throw new IOException("Cannot add title", e);
        }
    }

    private void addTable(Document document) throws IOException {
        // 6 cột: ID | Email | First | Last | Roles | Enabled
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(4f);
        table.setHeaderRows(1);

        try {
            table.setWidths(new float[]{8f, 28f, 18f, 18f, 22f, 8f});
        } catch (DocumentException e) {
            throw new IOException("Cannot set table widths", e);
        }

        // Header cells
        addHeaderCell(table, "ID");
        addHeaderCell(table, "Email");
        addHeaderCell(table, "First Name");
        addHeaderCell(table, "Last Name");
        addHeaderCell(table, "Roles");
        addHeaderCell(table, "Enabled");

        // Body (zebra)
        boolean zebra = false;
        Color zebraBg = new Color(245, 247, 250);

        for (User u : users) {
            Color bg = zebra ? zebraBg : Color.WHITE;

            addBodyCell(table, toStr(u.getId()),    bg, Element.ALIGN_RIGHT);
            addBodyCell(table, toStr(u.getEmail()), bg, Element.ALIGN_LEFT);
            addBodyCell(table, toStr(u.getFirstName()), bg, Element.ALIGN_LEFT);
            addBodyCell(table, toStr(u.getLastName()),  bg, Element.ALIGN_LEFT);

            Collection<Role> roleCol = u.getRoles();
            String roles = (roleCol == null) ? "" :
                    roleCol.stream().map(Role::getName).collect(Collectors.joining(", "));
            addBodyCell(table, roles, bg, Element.ALIGN_LEFT);

            addBodyCell(table, u.isEnabled() ? "Yes" : "No", bg, Element.ALIGN_CENTER);

            zebra = !zebra;
        }

        try {
            document.add(table);
        } catch (DocumentException e) {
            throw new IOException("Cannot add table", e);
        }
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(52, 71, 103));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        cell.setBorderWidth(0.5f);

        // Tránh constructor Phrase(String, Font) => add Chunk trực tiếp
        cell.addElement(new Chunk(text, fontHeader));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Color bg, int align) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        cell.setBorderWidth(0.3f);

        cell.addElement(new Chunk(text, fontBody));
        table.addCell(cell);
    }

    private String toStr(Object o) { return (o == null) ? "" : String.valueOf(o); }

    /* -------------- Footer page numbers -------------- */
    private static class FooterPageEvent extends PdfPageEventHelper {
        private final Font footerFont =
                FontFactory.getFont("NotoSans", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9f, Font.NORMAL, new Color(120,120,120));

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Phrase footer = new Phrase();
            footer.add(new Chunk(String.format("Page %d", writer.getPageNumber()), footerFont));
            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    footer,
                    (document.right() + document.left()) / 2,
                    document.bottom() - 10,
                    0
            );
        }
    }
}
