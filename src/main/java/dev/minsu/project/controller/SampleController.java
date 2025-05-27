package dev.minsu.project.controller;

import dev.minsu.project.dto.SampleDto;
import dev.minsu.project.entity.SampleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/sample")
public class SampleController {

    @GetMapping("")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello");
    }
}
