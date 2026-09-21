package com.kyobo.server.common;

import java.util.Map;

/**
 * 콘솔 인트로에 "KYOBO CINEMA" 로고가 극장 커튼이 걷히듯 드러나는 걸 보여준다.
 * 로고 자체는 왜곡하지 않고 고정해두고, 그 위를 덮은 커튼(주름 무늬)만 중앙에서
 * 양옆으로 걷어내는 방식이라 글자가 깨지거나 찌그러질 일이 없다.
 */
public final class IntroAnimation {
    private static final String ANSI_CLEAR = "[H[2J[3J";
    private static final int FONT_HEIGHT = 5;
    private static final char[] CURTAIN_FOLD = {'▓', '▓', '▒'};
    private static final int CURTAIN_STEP = 3;
    private static final long FRAME_DELAY_MS = 150;
    private static final long HOLD_DELAY_MS = 700;

    private static final Map<Character, String[]> FONT = Map.ofEntries(
            Map.entry('K', new String[] {"█   █", "█  █ ", "███  ", "█  █ ", "█   █"}),
            Map.entry('Y', new String[] {"█   █", " █ █ ", "  █  ", "  █  ", "  █  "}),
            Map.entry('O', new String[] {" ███ ", "█   █", "█   █", "█   █", " ███ "}),
            Map.entry('B', new String[] {"████ ", "█   █", "████ ", "█   █", "████ "}),
            Map.entry('C', new String[] {" ████", "█    ", "█    ", "█    ", " ████"}),
            Map.entry('I', new String[] {"█████", "  █  ", "  █  ", "  █  ", "█████"}),
            Map.entry('N', new String[] {"█   █", "██  █", "█ █ █", "█  ██", "█   █"}),
            Map.entry('E', new String[] {"█████", "█    ", "████ ", "█    ", "█████"}),
            Map.entry('M', new String[] {"█   █", "██ ██", "█ █ █", "█   █", "█   █"}),
            Map.entry('A', new String[] {"  █  ", " █ █ ", "█████", "█   █", "█   █"}),
            Map.entry(' ', new String[] {"     ", "     ", "     ", "     ", "     "}));

    private IntroAnimation() {
    }

    public static void play() {
        String[] logo = buildLogo("KYOBO CINEMA");
        int fullWidth = logo[0].length();

        int startCoverWidth = (fullWidth + 1) / 2;
        for (int coverWidth = startCoverWidth; coverWidth > 0; coverWidth -= CURTAIN_STEP) {
            renderFrame(logo, fullWidth, coverWidth);
            sleep(FRAME_DELAY_MS);
        }
        renderFrame(logo, fullWidth, 0);
        sleep(HOLD_DELAY_MS);
    }

    private static String[] buildLogo(String text) {
        String[] lines = new String[FONT_HEIGHT];
        for (int row = 0; row < FONT_HEIGHT; row++) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                String[] glyph = FONT.getOrDefault(text.charAt(i), FONT.get(' '));
                line.append(glyph[row]);
                if (i < text.length() - 1) {
                    line.append(' ');
                }
            }
            lines[row] = line.toString();
        }
        return lines;
    }

    /** coverWidth만큼 양쪽 끝을 주름진 커튼으로 덮고, 가운데만 로고를 그대로 보여준다 (무대 액자 테두리 포함). */
    private static void renderFrame(String[] logo, int fullWidth, int coverWidth) {
        System.out.print(ANSI_CLEAR);
        String border = "─".repeat(fullWidth + 2);
        System.out.println("┌" + border + "┐");
        for (String line : logo) {
            StringBuilder frame = new StringBuilder(fullWidth);
            for (int x = 0; x < fullWidth; x++) {
                boolean covered = x < coverWidth || x >= fullWidth - coverWidth;
                frame.append(covered ? CURTAIN_FOLD[x % CURTAIN_FOLD.length] : line.charAt(x));
            }
            System.out.println("│ " + frame + " │");
        }
        System.out.println("└" + border + "┘");
        System.out.flush();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
