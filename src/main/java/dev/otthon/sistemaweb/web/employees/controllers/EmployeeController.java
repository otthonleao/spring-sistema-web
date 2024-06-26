package dev.otthon.sistemaweb.web.employees.controllers;

import dev.otthon.sistemaweb.core.exceptions.EmployeeNotFoundException;
import dev.otthon.sistemaweb.core.repositories.EmployeeRepository;
import dev.otthon.sistemaweb.core.repositories.PositionRepository;
import dev.otthon.sistemaweb.web.employees.dtos.EmployeeForm;
import dev.otthon.sistemaweb.web.employees.mappers.EmployeeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ModelAndView index() {
        var employees = employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toEmployeeListItem)
                .toList();
        var model = Map.of("employees", employees);
        return new ModelAndView("employees/index", model);
    }

    @GetMapping("/{id}")
    public ModelAndView details(@PathVariable Long id) {
        var employee = employeeRepository.findById(id)
                .map(employeeMapper::toEmployeeDetails)
                .orElseThrow(EmployeeNotFoundException::new);
        var model = Map.of("employee", employee);
        return new ModelAndView("employees/details", model);
    }

    /* RESPONSÁVEL POR EXIBIR O FORMULÁRIO DE CADASTRO DE NOVO FUNCIONÁRIO */
    @GetMapping("/create")
    public ModelAndView create() {
        var positions = positionRepository.findAll();
        var model = Map.of(
                "pageTitle", "Cadastro de Funcionário",
                "employeeForm", new EmployeeForm(),
                "positions", positions
        );
        return new ModelAndView("employees/form", model);
    }

    @PostMapping("/create")
    public String create(@Valid EmployeeForm employeeForm, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Cadastro de Funcionário");
            model.addAttribute("positions", positionRepository.findAll());
            return "employees/form";
        }

        var employee = employeeMapper.toEmployee(employeeForm);

        // Usando senha criptografada em forma de hash para ser salva no banco
        var passwordHash = passwordEncoder.encode("changeMe"); // Senha padrão ao criar um novo usuário
        employee.setPassword(passwordHash);

        employeeRepository.save(employee);
        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable Long id) {
        var employeeForm = employeeRepository.findById(id)
                .map(employeeMapper::toEmployeeForm)
                .orElseThrow(EmployeeNotFoundException::new);
        var positions = positionRepository.findAll();
        var model = Map.of(
                "pageTitle", "Edição de Funcionário",
                "employeeForm", employeeForm,
                "positions", positions
        );
        return new ModelAndView("employees/form", model);
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @Valid EmployeeForm employeeForm, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Cadastro de Funcionário");
            model.addAttribute("positions", positionRepository.findAll());
            return "employees/form";
        }

        var employee = employeeRepository.findById(id)
                .orElseThrow(EmployeeNotFoundException::new);
        var employeeData = employeeMapper.toEmployee(employeeForm);
        BeanUtils.copyProperties(employeeData, employee, "id", "password"); // Ignora esses campos quando for editar funcionário
        employeeRepository.save(employee);
        return "redirect:/employees";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        var employee = employeeRepository.findById(id)
                .orElseThrow(EmployeeNotFoundException::new);
        employeeRepository.delete(employee);
        return "redirect:/employees";
    }
}
