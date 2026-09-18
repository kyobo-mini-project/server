package com.kyobo.server;

import java.util.Scanner;

import com.kyobo.server.config.FieldEncryptor;

/**
 * 관리자 계정 시딩용 1회성 도구.
 * 코드/비밀번호를 입력받아 BCrypt 해시를 출력한다 — admins 테이블 INSERT문에 그대로 사용.
 */
public class HashGenerator {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("관리자 코드와 비밀번호를 입력하면 BCrypt 해시를 출력합니다. (종료: 코드에 0 입력)");
            while (true) {
                System.out.print("관리자 코드 (0: 종료): ");
                String code = scanner.nextLine();
                if (code.equals("0")) {
                    break;
                }
                System.out.print("비밀번호: ");
                String password = scanner.nextLine();
                System.out.println(code + " -> " + FieldEncryptor.hashPassword(password));
            }
        }
    }
}
