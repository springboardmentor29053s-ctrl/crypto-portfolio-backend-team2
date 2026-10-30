package com.crypto_portfolio_tracker.controller;

import com.crypto_portfolio_tracker.dto.TaxReportDTO;
import com.crypto_portfolio_tracker.model.UserEntity;
import com.crypto_portfolio_tracker.repository.UserRepository;
import com.crypto_portfolio_tracker.service.ExcelGenerator;
import com.crypto_portfolio_tracker.service.TaxReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api")
@CrossOrigin("http://localhost:3000")
public class TaxController {
    private final UserRepository userRepo;
    private final TaxReportService taxReportService;

    public TaxController(UserRepository userRepo, TaxReportService taxReportService) {
        this.userRepo = userRepo;
        this.taxReportService = taxReportService;

    }

    @GetMapping("/tax-report")
    public List<TaxReportDTO> generateTaxReport(@RequestParam String email) {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return taxReportService.generateTaxReport(user);

    }

//    @GetMapping("/tax-report/pdf")
//    public ResponseEntity<byte[]> downloadTaxReport(@RequestParam String email) {
//
//        UserEntity user = userRepo.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        List<TaxReportDTO> report = taxReportService.generateTaxReport(user);
//
//        byte[] pdf = pdfGenerator.generateTaxReportPdf(report);
//
//        return ResponseEntity.ok()
//                .header("Content-Disposition", "attachment; filename=tax-report.pdf")
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(pdf);
//    }

    @GetMapping("/tax-report/excel")
    public ResponseEntity<byte[]> downloadExcel(@RequestParam String email) {
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<TaxReportDTO> reports = taxReportService.generateTaxReport(user);

        ByteArrayInputStream in = ExcelGenerator.generateTaxExcel(reports);
        log.info("Excel generated for user: {}", email);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=tax-report.xlsx")
                .body(in.readAllBytes());
    }
}



















