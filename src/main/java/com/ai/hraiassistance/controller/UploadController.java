package com.ai.hraiassistance.controller;

import com.ai.hraiassistance.entity.Employee;
import com.ai.hraiassistance.repo.EmployeeRepo;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UploadController {

    private static final String EMPLOYEE_HEADER = "id,name,designation,employmenttype";

    private final EmployeeRepo employeeRepository;
    private final VectorStore vectorStore;

    public UploadController(EmployeeRepo employeeRepository, VectorStore vectorStore) {
        this.employeeRepository = employeeRepository;
        this.vectorStore = vectorStore;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (file.isEmpty() || fileName == null || !fileName.contains(".")) {
            return ResponseEntity.badRequest().body("Please upload a valid file");
        }

        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        return switch (ext) {
            case "csv" -> saveEmployees(file);
            case "pdf" -> saveToVectorStore(file, fileName);
            default -> ResponseEntity.badRequest().body("Unsupported file type: " + ext);
        };
    }

    // CSV to employee table
    private ResponseEntity<String> saveEmployees(MultipartFile file) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String header = reader.readLine();
            if (header == null || !header.trim().equalsIgnoreCase(EMPLOYEE_HEADER)) {
                return ResponseEntity.badRequest()
                        .body("Not an employee CSV. Expected columns: " + EMPLOYEE_HEADER);
            }

            List<Employee> employees = new ArrayList<>();
            int skipped = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length != 4) { skipped++; continue; }
                try {
                    employees.add(new Employee(Integer.parseInt(p[0].trim()),
                            p[1].trim(), p[2].trim(), p[3].trim()));
                } catch (NumberFormatException e) {
                    skipped++;
                }
            }

            employeeRepository.saveAll(employees);
            return ResponseEntity.ok("Saved " + employees.size() + " employees, skipped " + skipped + " bad rows");
        }
    }

    // PDF to vector store
    private ResponseEntity<String> saveToVectorStore(MultipartFile file, String fileName) {
        vectorStore.delete("source == '" + fileName + "'");   // remove old version of this file

        List<Document> pages = new PagePdfDocumentReader(file.getResource()).read();
        List<Document> chunks = new TokenTextSplitter().split(pages);
        chunks.forEach(c -> c.getMetadata().put("source", fileName));

        vectorStore.add(chunks);
        return ResponseEntity.ok("Stored " + chunks.size() + " chunks from " + fileName);
    }
}