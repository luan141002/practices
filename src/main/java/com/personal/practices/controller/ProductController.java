package com.personal.practices.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @GetMapping()
    public ResponseEntity<?> getAll() {
        return new ResponseEntity(HttpEntity.EMPTY, HttpStatus.OK);
    }
}
