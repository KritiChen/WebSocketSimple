package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 硬编码验证（实际项目应使用数据库）
        if (("赵敏".equals(username) && "123456".equals(password)) ||
                ("张无忌".equals(username) && "654321".equals(password))) {

            session.setAttribute("username", username);
            response.put("success", true);
            return ResponseEntity.ok(response);
        }

        response.put("success", false);
        response.put("message", "江湖名号或武功秘钥错误！");
        return ResponseEntity.status(401).body(response);
    }
}
