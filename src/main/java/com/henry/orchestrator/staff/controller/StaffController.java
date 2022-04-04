package com.henry.orchestrator.staff.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.henry.orchestrator.kafka.service.KafkaProducerService;
import com.henry.orchestrator.staff.model.Staff;
import com.henry.orchestrator.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;

@Controller
public class StaffController {
    @Autowired
    RestTemplate restTemplate;

    private final KafkaProducerService kafkaProducerService;
    ObjectMapper mapper = new ObjectMapper();

    public StaffController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @GetMapping("/signup")
    public String showSignUpForm(Staff staff) {
        return "add-staff";
    }

    @GetMapping(value= {"", "/", "/index"})
    public String showStaffList(Model model) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Staff[]> responseEntityStaff =
                restTemplate.exchange("http://localhost:8082/staffs",
                        HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
                        });

        model.addAttribute("staffs", Arrays.asList(responseEntityStaff.getBody()));
        return "index";
    }

    @PostMapping("/addstaff")
    public String addStaff(@Valid Staff staff, BindingResult result, Model model) throws JsonProcessingException {
        if (result.hasErrors()) {
            return "add-staff";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<Staff> entity = new HttpEntity<>(staff,headers);

        HttpStatus responseStatus = restTemplate.exchange(
                "http://localhost:8082/createStaff", HttpMethod.POST, entity, String.class).getStatusCode();

        if (responseStatus.equals(HttpStatus.OK)){
            sendLoggingToKafka(staff);
            return "redirect:/index";
        }

        return "add-staff";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Staff> staff = restTemplate.exchange(
                "http://localhost:8082/getStaffById?id="+id, HttpMethod.GET, entity, Staff.class);

        model.addAttribute("staff", staff.getBody());
        return "update-staff";
    }

    @PostMapping("/update/{id}")
    public String updateStaff(@PathVariable("id") long id, @Valid Staff staff,
                             BindingResult result, Model model) throws JsonProcessingException {
        if (result.hasErrors()) {
            staff.setId(id);
            return "update-staff";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<Staff> entity = new HttpEntity<>(staff,headers);

        HttpStatus responseStatus = restTemplate.exchange(
                "http://localhost:8082/updateStaff", HttpMethod.PUT, entity, String.class).getStatusCode();

        if (responseStatus.equals(HttpStatus.OK)){
            sendLoggingToKafka(staff);
            return "redirect:/index";
        }

        return "update-staff";
    }

    @GetMapping("/delete/{id}")
    public String deleteStaff(@PathVariable("id") long id, Model model) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Staff> staff = restTemplate.exchange(
                "http://localhost:8082/getStaffById?id="+id, HttpMethod.GET, entity, Staff.class);

        if (staff.getBody() == null){
            throw new IllegalArgumentException("Invalid staff Id:" + id);
        }

        HttpStatus responseStatus = restTemplate.exchange(
                "http://localhost:8082/deleteStaff?id="+id, HttpMethod.DELETE, entity, String.class).getStatusCode();

        if (responseStatus.equals(HttpStatus.OK)){
            sendLoggingToKafka(staff.getBody());
            return "redirect:/index";
        }

        return "redirect:/index";
    }

    private void sendLoggingToKafka(Staff staff) throws JsonProcessingException {
        kafkaProducerService.sendMessage(JsonUtil.generateJson(staff));
    }
}
