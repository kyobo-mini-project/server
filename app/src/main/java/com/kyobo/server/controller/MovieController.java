package com.kyobo.server.controller;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import com.kyobo.server.entity.MovieListItem;
import com.kyobo.server.entity.User;
import com.kyobo.server.service.MovieService;

public class MovieController {
    private final Scanner scanner;
    private final MovieService movieService;
    private final MovieDetailController movieDetailController;

    public MovieController(Scanner scanner) {
        this.scanner = scanner;
        this.movieService = new MovieService();
        this.movieDetailController = new MovieDetailController(scanner, this::requestExit); //moviedetail추가
    }

    /** @return false면 프로그램을 종료하고, true면 홈 메뉴로 돌아간다. */
    public boolean runMovieList(User currentUser) {
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
                            boolean continueProgram =
                            runMovieDetail(currentUser);

                            if (continueProgram == false){
                                return false;
                            }
                            printMovieList(movies);
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
        if (movies.isEmpty()) {
            System.out.println("현재 상영 중인 영화가 없습니다.");
        } else {
            int titleWidth = Math.max(8, maxDisplayWidth(movies.stream().map(MovieListItem::getTitle).toList()));
            int genreWidth = Math.max(8, maxDisplayWidth(movies.stream().map(MovieListItem::getGenres).toList()));

            for (MovieListItem movie : movies) {
                System.out.printf("%s  제목: %s  장르: %s  (점유율: %5.1f%%)%n",
                        padDisplay(movie.getMovieId() + ".", 4),
                        padDisplay(nullToEmpty(movie.getTitle()), titleWidth),
                        padDisplay(nullToEmpty(movie.getGenres()), genreWidth),
                        movie.getOccupancyRate());
            }
        }
        System.out.println("================================================");
    }

    private int maxDisplayWidth(List<String> values) {
        int max = 0;
        for (String value : values) {
            max = Math.max(max, displayWidth(nullToEmpty(value)));
        }
        return max;
    }

    private String padDisplay(String text, int width) {
        int padding = width - displayWidth(text);
        if (padding <= 0) {
            return text;
        }
        return text + " ".repeat(padding);
    }

    /** 터미널 기준 표시 폭. 한글 등 전각은 2칸으로 계산한다. */
    private int displayWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            width += isWideChar(codePoint) ? 2 : 1;
        }
        return width;
    }

    private boolean isWideChar(int codePoint) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || (codePoint >= 0xFF01 && codePoint <= 0xFF60);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void printSubMenu(boolean isEmpty) {
        System.out.println(isEmpty
                ? "[2. 뒤로가기] [0. 종료]"
                : "[1. 영화 상세 조회] [2. 뒤로가기] [0. 종료]");
    }

    private boolean runMovieDetail(User currentUser) {
        return movieDetailController.runMovieDetail(currentUser);
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
