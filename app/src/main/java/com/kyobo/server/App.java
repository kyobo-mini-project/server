package com.kyobo.server;

import java.util.Scanner;

import com.kyobo.server.controller.ConsoleController;
import com.kyobo.server.service.HealthService;

public class App {
    public static void main(String[] args) {
        mybatisHealthCheck();

        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleController(scanner).run();
        }
    }

    private static void mybatisHealthCheck() {
        int result = new HealthService().ping();
        System.out.println("MyBatis 연결 성공: " + result);
    }
}
