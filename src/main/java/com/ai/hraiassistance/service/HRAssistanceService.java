package com.ai.hraiassistance.service;

import com.ai.hraiassistance.entity.Employee;
import com.ai.hraiassistance.repo.EmployeeRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HRAssistanceService {

    private final VectorStore vectorStore;

    private final List<Employee> employees;

    public HRAssistanceService(VectorStore vectorStore, EmployeeRepo employeeRepo) throws IOException {
        this.vectorStore = vectorStore;
        this.employees = employeeRepo.findAll();
    }



    @Tool(description = "Use this ONLY when the user ask the question about the employee data.")
    public String checkEmployeeDetails(String query){
        log.info("Triggered the employee details..{}",query);
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(3)
                .build());

        return  results.stream().map(Document::getText)
                .collect(Collectors.joining("\n---\n"));
    }

    @Tool(description = "Count employees by employment type, such as Full-time, Contract or Intern")
    public long checkEmployeeTypeCount(String employeeType){
        log.info("Triggered the employee type count..{}",employeeType);
        return employees.stream().filter(e->e.getEmploymentType().equalsIgnoreCase(employeeType)).count();
    }

    @Tool(description = "Count employees with a given designation ONLY NOT EMPLOYMENT TYPE. Valid values: Software Engineer, Senior Software Engineer, QA Engineer, Team Lead, DevOps Engineer, HR Executive, Business Analyst, Accountant, Manager")
    public long countByDesignation(String designationType){
        log.info("Triggered the designation type..{}",designationType);
        return employees.stream().filter(e->e.getDesignation().equalsIgnoreCase(designationType)).count();
    }

    @Tool(description = "Count employees by BOTH designation and employment type. Use this when the question mentions a designation AND an employment type, e.g. 'Team Leads on contract'.")
    public long CountByEmployeeDesignationAndEmploymentType(@ToolParam(description = "Employment type: Full-time, Contract or Intern") String employeeType, @ToolParam(description = "Designation, e.g. Team Lead, QA Engineer") String designationType){
        log.info("Triggered the designation type..{}, {}",designationType, employeeType);
        return employees.stream().filter(e->e.getDesignation().equalsIgnoreCase(designationType) && e.getEmploymentType().equalsIgnoreCase(employeeType)).count();
    }

    // Tool annotation used to mention filter or the scenario that AI can use to call when matching query is asked by user.
    // ToolParam annotation used to mention the description for each query element individually so that the AI can't hallucinate

    @Tool(description = "Count employees. Filter by designation, employment type, or both. Leave a filter empty if the question does not mention it.")
    public long countEmployees(
            @ToolParam(description = "Designation, e.g. Team Lead, QA Engineer. Empty if not mentioned.", required = false)
            String designation,
            @ToolParam(description = "Employment type: Full-time, Contract or Intern. Empty if not mentioned.", required = false)
            String employmentType) {

        log.info("countEmployees -> designation={}, type={}", designation, employmentType);

        return employees.stream()
                .filter(e -> isEmpty(designation) || e.getDesignation().equalsIgnoreCase(designation.trim()))
                .filter(e -> isEmpty(employmentType) || e.getEmploymentType().equalsIgnoreCase(employmentType.trim()))
                .count();
    }

    private boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }

    @Tool(description = "Search the company HR policy documents. Use for questions about company rules such as leave policy, work from home, learning budget, notice period, working hours.")
    public String searchPolicy(
            @ToolParam(description = "What to search for, e.g. 'paid leave days' or 'work from home rules'") String query) {

        log.info("searchPolicy -> {}", query);
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(3).build());

        if (results.isEmpty()) {
            return "No matching policy found.";
        }
        return results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));
    }
}
