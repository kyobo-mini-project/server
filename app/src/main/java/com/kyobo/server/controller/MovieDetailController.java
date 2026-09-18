package com.kyobo.server.controller;

import java.util.Scanner;
import java.util.function.BooleanSupplier;

import com.kyobo.server.entity.MovieDetail;
import com.kyobo.server.service.MovieDetailService;

public class MovieDetailController {

    private final Scanner scanner;
    private final MovieDetailService movieDetailService;
    private final BooleanSupplier requestExit;

    public MovieDetailController(
            Scanner scanner,
            MovieDetailService movieDetailService,
            BooleanSupplier requestExit
    ) {
        this.scanner = scanner;
        this.movieDetailService = movieDetailService;
        this.requestExit = requestExit;
    }

    /** @return false면 프로그램 종료, true면 영화 목록으로 복귀 */
    public boolean runMovieDetail() {
        MovieDetail movie;

        // 1. 상세 조회할 영화 번호 입력
        while (true) {
            System.out.print(
                    "상세 조회할 영화 번호를 입력하세요. (목록으로: 0): "
            );

            if (!scanner.hasNextLine()) {
                return false;
            }

            String input = scanner.nextLine().trim();
            int movieId;

            try {
                movieId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("영화 번호를 숫자로 입력해 주세요.");
                continue;
            }

            if (movieId == 0) {
                return true;
            }

            if (movieId < 0) {
                System.out.println("올바른 영화 번호를 입력해 주세요.");
                continue;
            }

            movie = movieDetailService.findMovieDetail(movieId);

            if (movie == null) {
                System.out.println("존재하지 않는 영화입니다.");
                continue;
            }

            break;
        }

        // 2. 상세 정보 출력
        printMovieDetail(movie);

        // 3. 메뉴 분기
        while (true) {
            printSubMenu();
            System.out.print("기능 선택: ");

            if (!scanner.hasNextLine()) {
                return false;
            }

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {
                    boolean continueProgram =
                            runBooking(movie.getMovieId());

                    if (!continueProgram) {
                        return false;
                    }

                    printMovieDetail(movie);
                }

                case "2" -> {
                    return true;
                }

                case "0" -> {
                    // 팀에서 이미 작성한 종료 메서드 실행
                    try {
                        if (!requestExit.getAsBoolean()) {
                            return false;
                        }
                    } catch (java.util.InputMismatchException e) {
                        // 앞서 보여준 requestExit()의 잘못된 입력 처리
                        printInvalidMenu();
                    }
                }

                default -> printInvalidMenu();
            }
        }
    }

    private void printMovieDetail(MovieDetail movie) {
        System.out.println("================================================");
        System.out.println("영화 상세 정보");
        System.out.println("------------------------------------------------");

        System.out.println("영화 번호: " + movie.getMovieId());
        System.out.println("제목: " + displayText(movie.getTitle()));
        System.out.println("장르: " + displayText(movie.getGenres()));
        System.out.println("감독: " + displayText(movie.getDirector()));

        System.out.println("상영 시간: "
                + (movie.getRunningTime() == null
                ? "미등록"
                : movie.getRunningTime() + "분"));

        System.out.println("개봉일: "
                + (movie.getReleaseDate() == null
                ? "미등록"
                : movie.getReleaseDate()
                .toLocalDateTime()
                .toLocalDate()));

        Integer ageLimit = movie.getAgeLimit();

        if (ageLimit == null) {
            System.out.println("관람 제한 나이: 미등록");
        } else {
            System.out.println("관람 제한 나이: " + ageLimit + "세");
        }

        System.out.println("줄거리: " + displayText(movie.getContent()));
        System.out.println("================================================");
    }

    private void printSubMenu() {
        System.out.println(
                "[1. 영화 예매하기] [2. 목록으로 돌아가기] [0. 종료]"
        );
    }

    /**
     * 예매 기능 연결 자리
     * @return false면 프로그램 종료, true면 상세 화면으로 복귀
     */
    private boolean runBooking(Integer movieId) {
        // 예매 기능 구현 후 해당 컨트롤러로 위임한다.
        // 현재 상세보기 중인 영화의 movieId를 전달한다.
        System.out.println("선택한 영화 번호: " + movieId);
        System.out.println("영화 예매 기능은 아직 연결되지 않았습니다.");

        return true;
    }

    private void printInvalidMenu() {
        System.out.println(
                "잘못된 입력입니다. 메뉴 번호를 다시 입력해 주세요."
        );
    }

    private String displayText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "미등록";
        }

        return value;
    }
}