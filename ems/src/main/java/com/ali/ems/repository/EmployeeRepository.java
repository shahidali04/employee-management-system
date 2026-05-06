package com.ali.ems.repository;

import com.ali.ems.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    List<Employee> findByNameContainingIgnoreCase(String name);

    List<Employee> findByDepartmentContainingIgnoreCase(String department);

    List<Employee> findBySalary(double salary);
}
