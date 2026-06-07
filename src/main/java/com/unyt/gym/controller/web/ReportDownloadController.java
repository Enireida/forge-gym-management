package com.unyt.gym.controller.web;

import com.unyt.gym.entity.Role;
import com.unyt.gym.entity.User;
import com.unyt.gym.exception.GymException;
import com.unyt.gym.service.ReportDocxService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Streams the executive Word report back to the browser as a download.
 */
@RestController
@RequestMapping("/admin/reports")
public class ReportDownloadController {

    private final ReportDocxService docxService;

    public ReportDownloadController(ReportDocxService docxService) {
        this.docxService = docxService;
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(HttpSession session) throws IOException {
        User u = (User) session.getAttribute("currentUser");
        if (u == null || (u.getRole() != Role.ADMIN && u.getRole() != Role.MANAGER)) {
            throw new GymException("Admins only", 401);
        }
        byte[] body = docxService.buildExecutiveReport();
        String filename = "Forge_Report_" + LocalDate.now() + ".docx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(body);
    }
}
