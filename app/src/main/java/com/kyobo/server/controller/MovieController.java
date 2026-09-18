package com.kyobo.server.controller;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import com.kyobo.server.entity.MovieListItem;
import com.kyobo.server.service.MovieService;

public class MovieController {
    private final Scanner scanner;
    private final MovieService movieService;

    public MovieController(Scanner scanner) {
        this.scanner = scanner;
        this.movieService = new MovieService();
    }

    /** @return false면 프로그램을 종료하고, true면 홈 메뉴로 돌아간다. */
    public boolean runMovieList() {
        List<MovieListItem> movies = movieService.getMovieList();
        printMovieList(movies);

        while (true) {
            printSubMenu(movies.isEmpty());
            try {
                switch (readInt("기능 선택: ")) {
                    case 1 -> {
                        if (movies.isEmpty()) {
                            printInvalidMenu();
                        } else {
                            runMovieDetail();
                        }
                    }
                    case 2 -> {
                        return true;
                    }
                    case 0 -> {
                        return requestExit();
                    }
                    default -> printInvalidMenu();
                }
            } catch (InputMismatchException e) {
                printInvalidMenu();
            }
        }
    }

    private void printMovieList(List<MovieListItem> movies) {
        System.out.println("================================================");
        System.out.println("상영 영화 목록");
        System.out.println("------------------------------------------------");
        int count = 0;
        if (movies.isEmpty()) {
            System.out.println("현재 상영 중인 영화가 없습니다.");
        } else {
            for (MovieListItem movie : movies) {
                count++;
                System.out.printf("%d. 제목: %s | 장르: %s%n", movie.getMovieId(), movie.getTitle(), movie.getGenres());
            }
        }
        System.out.println("================================================");
    }

    private void printSubMenu(boolean isEmpty) {
        System.out.println(isEmpty
                ? "[2. 뒤로가기] [0. 종료]"
                : "[1. 영화 상세 조회] [2. 뒤로가기] [0. 종료]");
    }

    private void runMovieDetail() {
        // 영화 상세 조회(F-05)가 구현되면 해당 컨트롤러로 위임한다.
        System.out.println("영화 상세 조회 기능은 아직 구현되지 않았습니다.");
    }

    private void printInvalidMenu() {
        System.out.println("잘못된 입력입니다. 메뉴 번호를 다시 입력해 주세요.");
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextInt()) {
            scanner.nextLine();
            throw new InputMismatchException();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    public boolean requestExit() {
        System.out.print("정말 종료하시겠습니까? (종료: 0, 취소: 1) ");
        int answer = readInt("");
        if (answer == 0) {
            System.out.println("================================================");
            System.out.println("교보시네마를 종료합니다. 안녕히 가세요 👋");
            System.out.println("================================================");
            return false;
        }
        return true;
    }
}
