package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(UserService userService,
                           RoleService roleService,
                           PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.findAll());
        return "admin/user-form";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute User user,
                           @RequestParam("roles") List<Integer> roleIds) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = new HashSet<>(roleService.findAllByIds(roleIds));
        user.setRoles(roles);
        userService.saveUser(user);
        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Integer id, Model model) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.findAll());
        return "admin/user-form";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Integer id,
                             @ModelAttribute User user,
                             @RequestParam("roles") List<Integer> roleIds) {
        User existingUser = userService.findUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setAge(user.getAge());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        Set<Role> roles = roleService.findAllByIds(roleIds);
        existingUser.setRoles(roles);
        userService.saveUser(existingUser);
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}
