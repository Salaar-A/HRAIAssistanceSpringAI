package com.ai.hraiassistance.repo;

import com.ai.hraiassistance.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepo extends JpaRepository<Employee, Integer> {
}