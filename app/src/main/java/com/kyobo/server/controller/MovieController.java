package com.kyobo.server.controller;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import com.kyobo.server.entity.MovieListItem;
import com.kyobo.server.entity.User;
import com.kyobo.server.service.MovieService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.kyobo.server.common.ApiResponse;
import com.kyobo.server.entity.Movie;

import com.kyobo.server.entity.Genre;

public class MovieController {
    private enum SearchResultAction { SEARCH_AGAIN, BACK, EXIT }

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

    public boolean runSearchMovie() {
        return runSearchMovie(null);
    }

    public boolean runSearchMovie(User currentUser) {
        while (true) {
            System.out.println("[1. 제목 검색] [2. 장르 검색] [3. 뒤로가기] [0. 종료]");
            try {
                switch (readInt("기능 선택: ")) {
                    case 1 -> {
                        if (!runSearchByTitle(currentUser)) return false;
                    }
                    case 2 -> {
                        if (!runSearchByGenre(currentUser)) return false;
                    }
                    case 3 -> { return true; }
                    case 0 -> { return requestExit(); }
                    default -> printInvalidMenu();
                }
            } catch (InputMismatchException e) {
                printInvalidMenu();
            }
        }
    }

    private boolean runSearchByTitle(User currentUser) {
        while (true) {
            System.out.print("검색할 영화 제목(2글자 이상)을 입력해 주세요: ");
            String keyword = scanner.nextLine().trim();
            if (keyword.isEmpty()) {
                System.out.println("검색어를 입력해 주세요.");
                continue;
            }
            if (keyword.length() < 2) {
                System.out.println("제목 검색어는 2글자 이상 입력해 주세요.");
                continue;
            }

            SearchResultAction action = runSearchResults(
                    keyword, movieService.searchMoviesByTitle(keyword), currentUser);
            if (action == SearchResultAction.EXIT) return false;
            if (action == SearchResultAction.BACK) return true;
        }
    }

    private boolean runSearchByGenre(User currentUser) {
        ApiResponse<List<Genre>> response = movieService.getGenreList();
        if (!"00".equals(response.getStatusCode())) {
            System.out.println(response.getStatusMessage());
            return true;
        }

        List<Genre> genres = response.getData();
        if (genres.isEmpty()) {
            System.out.println("등록된 장르가 없습니다.");
            return true;
        }

        while (true) {
            printGenres(genres);
            System.out.print("검색할 장르 번호 또는 장르명을 입력해 주세요: ");
            Genre selectedGenre = findGenre(genres, scanner.nextLine().trim());
            if (selectedGenre == null) {
                System.out.println("올바른 장르를 선택해 주세요.");
                continue;
            }

            SearchResultAction action = runSearchResults(selectedGenre.getGenreName(),
                    movieService.searchMoviesByGenre(selectedGenre.getGenreName()), currentUser);
            if (action == SearchResultAction.EXIT) return false;
            if (action == SearchResultAction.BACK) return true;
        }
    }

    private SearchResultAction runSearchResults(String keyword, List<MovieListItem> movies, User currentUser) {
        if (movies.isEmpty()) {
            System.out.println("'" + keyword + "'에 대한 검색 결과가 없습니다.");
            while (true) {
                System.out.println("[1. 다시 검색] [2. 뒤로가기]");
                try {
                    switch (readInt("기능 선택: ")) {
                        case 1 -> { return SearchResultAction.SEARCH_AGAIN; }
                        case 2 -> { return SearchResultAction.BACK; }
                        default -> printInvalidMenu();
                    }
                } catch (InputMismatchException e) {
                    printInvalidMenu();
                }
            }
        }

        printSearchResults(keyword, movies);
        while (true) {
            System.out.println("[1. 영화 상세 조회] [2. 다시 검색] [3. 뒤로가기] [0. 종료]");
            try {
                switch (readInt("기능 선택: ")) {
                    case 1 -> {
                        if (!runMovieDetail(currentUser)) return SearchResultAction.EXIT;
                        printSearchResults(keyword, movies);
                    }
                    case 2 -> { return SearchResultAction.SEARCH_AGAIN; }
                    case 3 -> { return SearchResultAction.BACK; }
                    case 0 -> {
                        if (!requestExit()) return SearchResultAction.EXIT;
                    }
                    default -> printInvalidMenu();
                }
            } catch (InputMismatchException e) {
                printInvalidMenu();
            }
        }
    }

    private void printSearchResults(String keyword, List<MovieListItem> movies) {
        System.out.println("================================================");
        System.out.println("'" + keyword + "' 검색 결과 (총 " + movies.size() + "건)");
        System.out.println("------------------------------------------------");
        for (MovieListItem movie : movies) {
            System.out.println(movie.getMovieId() + ". 제목: " + nullToEmpty(movie.getTitle())
                    + "  장르: " + nullToEmpty(movie.getGenres()));
        }
        System.out.println("================================================");
    }

    private void printGenres(List<Genre> genres) {
        System.out.println("[장르 목록]");
        int columnWidth = 15;
        for (int i = 0; i < genres.size(); i++) {
            Genre genre = genres.get(i);
            String item = genre.getGenreId() + ". " + genre.getGenreName();
            System.out.print(padDisplay(item, columnWidth));
            if ((i + 1) % 5 == 0) {
                System.out.println();
            } else if (i == genres.size() - 1) {
                System.out.println();
            }
        }
    }

    private Genre findGenre(List<Genre> genres, String input) {
        for (Genre genre : genres) {
            if (genre.getGenreName().equals(input) || String.valueOf(genre.getGenreId()).equals(input)) {
                return genre;
            }
        }
        return null;
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

    public void runMovieRegistration() {
        System.out.println("===== 영화 등록 =====");

        String title = readRequiredText("영화 제목: ");
        int ageLimit = readRegistrationNumber(
                "관람 가능 나이 (전체 관람가: 0): ", 0);
        int runningTime = readRegistrationNumber(
                "상영 시간 (분): ", 1);
        LocalDate releaseDate = readReleaseDate();
        String content = readRequiredText("영화 소개: ");
        String director = readRequiredText("감독: ");
        // 장르 목록 조회
        ApiResponse<List<Genre>> genreResponse =
                movieService.getGenreList();

        if (!"00".equals(genreResponse.getStatusCode())) {
            System.out.println(genreResponse.getStatusMessage());
            return;
        }

        List<Genre> genres = genreResponse.getData();

        if (genres.isEmpty()) {
            System.out.println("등록된 장르가 없습니다.");
            return;
        }

// 장르번호와 이름 출력
        System.out.println("===== 장르 목록 =====");

        for (int i = 0; i < genres.size(); i++) {
            Genre genre = genres.get(i);

            String item = genre.getGenreId() + ". " + genre.getGenreName();
            System.out.printf("%-20s", item);

            // 5개를 출력했거나 마지막 장르라면 줄바꿈
            if ((i + 1) % 5 == 0 || i == genres.size() - 1) {
                System.out.println();
            }
        }

// 출력된 목록을 보고 입력
        List<Integer> genreIds = readGenreIds(genres);

        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setAgeLimit(ageLimit);
        movie.setRunningTime(runningTime);
        movie.setReleaseDate(releaseDate);
        movie.setContent(content);
        movie.setDirector(director);

        ApiResponse<Void> response = movieService.insertMovie(movie, genreIds);
        System.out.println(response.getStatusMessage());
    }

    // 빈 문자열이면 다시 입력받기
    private String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("내용을 입력해 주세요.");
                continue;
            }

            return input;
        }
    }

    // 정수인지, 최솟값 이상인지 검사하기
    private int readRegistrationNumber(String prompt, int min) {
        while (true) {
            String input = readRequiredText(prompt);

            try {
                int number = Integer.parseInt(input);

                if (number < min) {
                    System.out.println(min + " 이상의 숫자를 입력해 주세요.");
                    continue;
                }

                return number;
            } catch (NumberFormatException e) {
                System.out.println("정수를 입력해 주세요.");
            }
        }
    }

    // 실제로 존재하는 날짜인지 검사하기
    private LocalDate readReleaseDate() {
        while (true) {
            String input = readRequiredText("개봉일 (예: 2026-09-30): ");

            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println(
                        "올바른 날짜를 yyyy-MM-dd 형식으로 입력해 주세요.");
            }
        }
    }
    private List<Integer> readGenreIds(List<Genre> genres) {
        while (true) {
            String input = readRequiredText(
                    "장르 번호 (여러 개는 쉼표로 구분, 예: 1,3,5): ");

            List<Integer> genreIds = new java.util.ArrayList<>();
            boolean valid = true;

            for (String part : input.split(",", -1)) {
                int genreId;

                try {
                    genreId = Integer.parseInt(part.trim());
                } catch (NumberFormatException e) {
                    System.out.println(
                            "장르 번호를 쉼표로 구분해 입력해 주세요.");
                    valid = false;
                    break;
                }

                boolean exists = false;

                for (Genre genre : genres) {
                    if (Integer.valueOf(genreId).equals(genre.getGenreId())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    System.out.println(
                            "목록에 없는 장르 번호입니다: " + genreId);
                    valid = false;
                    break;
                }

                // 같은 번호를 여러 번 입력해도 한 번만 추가
                if (!genreIds.contains(genreId)) {
                    genreIds.add(genreId);
                }
            }

            if (valid) {
                return genreIds;
            }
        }
    }
}
