package com.kyobo.server.common;

/**
 * 예매 완료처럼, 아직 구현되지 않은 호출자(영화 상세조회)를 건너뛰고
 * 로그인 후 메인 메뉴까지 한 번에 돌아가야 하는 지점에서 쓰는 제어 신호.
 * 단순히 이전 단계로 돌아가는 경우는 각 메서드가 sentinel 값을 반환하는 것으로 충분하며,
 * 이 예외는 "현재 화면의 호출자까지 건너뛰어야 하는" 경우에만 던진다.
 * 진단용이 아니라 흐름 제어용이라 스택트레이스는 채우지 않는다.
 */
public class GoHomeSignal extends RuntimeException {
    public GoHomeSignal() {
        super(null, null, false, false);
    }
}
