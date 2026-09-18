package com.kyobo.server.common;

/**
 * 비로그인 상태로 로그인 필요 기능(예: 예매)에 진입했을 때, 영화 목록/상세조회처럼
 * 중간에 낀 화면들을 건너뛰고 게스트 홈의 로그인 화면으로 바로 이동하기 위한 제어 신호.
 * 진단용이 아니라 흐름 제어용이라 스택트레이스는 채우지 않는다.
 */
public class RequireLoginSignal extends ControlFlowSignal {
}
