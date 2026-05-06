package com.ali.ems.controller;

import com.ali.ems.entity.User;
import com.ali.ems.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import com.ali.ems.entity.Employee;
import com.ali.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class EmployeeController {

    // Dependency Injection
    @Autowired
    private EmployeeRepository repo;
    @Autowired
    private UserRepository userRepo;


    // Method to check login
    private boolean isLoggedIn(HttpSession session){
        return session.getAttribute("user") == null;
    }

    //-------Public pages-------

    // show pages
    @GetMapping("/")
    public String welcome() {
        return "welcome";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // process authentication
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    //------------Authentication--------

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model, HttpSession session) {

        User user = userRepo.findByEmailAndPassword(email, password);

        if (user != null) {
            session.setAttribute("user", user); // store user
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute User user, Model model) {

        User existing = userRepo.findByEmail(user.getEmail());

        if (existing != null) {
            model.addAttribute("error", "Email already registered!");
            return "signup";
        }

        userRepo.save(user);
        return "redirect:/dashboard";
    }

    //logout
    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate(); // destroy session
        return "redirect:/login";
    }

    //--------Dashboard---------

    //dashboard page
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        if(isLoggedIn(session)){
            return "redirect:/login";
        }

        //total employee
        long totalEmployees = repo.count();

        // total department
        long departments = repo.findAll()
                .stream()
                .map(Employee::getDepartment)
                .distinct()
                .count();

        //active employees(for now same as total)
        long active = repo.count();

        model.addAttribute("total", totalEmployees);
        model.addAttribute("dept", departments);
        model.addAttribute("active", active);

        return "dashboard";
    }

    //----------Add page----------

    @GetMapping("/add")
    public String showForm(HttpSession session) {

        if(isLoggedIn(session)){
            return "redirect:/login";
        }

        return "add-emp";
    }

    // save employee
    @PostMapping("/save")
    public String saveEmployee(@ModelAttribute Employee emp) {
        repo.save(emp);
        return "redirect:/add?success";
    }

    //--------view employee--------

    @GetMapping("/employees")
    public String viewEmployees(@RequestParam(required = false) String keyword,
                                Model model,
                                HttpSession session) {

        if(isLoggedIn(session)){
            return "redirect:/login";
        }

        List<Employee> list;

        if (keyword != null && !keyword.isEmpty()) {

            try {
                int id = Integer.parseInt(keyword);
                Employee emp = repo.findById(id).orElse(null);
                list = new java.util.ArrayList<>();

                if (emp != null) {
                    list.add(emp);
                }
            } catch (Exception e) {
                list = repo.findByNameContainingIgnoreCase(keyword);
                list.addAll(repo.findByDepartmentContainingIgnoreCase(keyword));
            }

        } else {
            list = repo.findAll();
        }

        model.addAttribute("list", list);
        return "view-employees";
    }

    //--------Update page---------

    @GetMapping("/update-page")
    public String updatePage(HttpSession session){

        if(isLoggedIn(session)){
            return "redirect:/login";
        }

        return "update-emp";
    }

    @GetMapping("/find")
    public String findEmployee(@RequestParam int id,
                               Model model,
                               HttpSession session){

        if (isLoggedIn(session)) {
            return "redirect:/login";
        }

        Employee emp = repo.findById(id).orElse(null);

        if (emp == null){
            model.addAttribute("error", "Employee not found");
            return "update-emp";
        }
        model.addAttribute("emp", emp);
        return "update-form";
    }

    @PostMapping("/update")
    public String updateEmployee(@ModelAttribute Employee emp,
                                 HttpSession session) {

        if (isLoggedIn(session)) {
            return "redirect:/login";
        }

        repo.save(emp);
        return "redirect:/update-page?updated";
    }

    //--------Delete page---

    @GetMapping("/delete-page")
    public String deletePage0(HttpSession session){

        if(isLoggedIn(session)){
            return "redirect:/login";
        }

        return "delete-emp";
    }

    @GetMapping("/find-delete")
    public String findEmployeeForDelete(@RequestParam int id, Model model,
                                        HttpSession session) {

        if (isLoggedIn(session)) {
            return "redirect:/login";
        }

        Employee emp = repo.findById(id).orElse(null);

        if (emp == null){
           model.addAttribute("error", "Employee not found");
           return "delete-emp";
        }

        model.addAttribute("emp", emp);
        return "delete-form";
    }

    @GetMapping("/delete-confirm/{id}")
    public String deleteEmployee(@PathVariable int id) {
        repo.deleteById(id);
        return "redirect:/delete-page?deleted";
    }
}