package com.kyobo.server.common;

/**
 * GoHomeSignal/RequireLoginSignal 중복 코드 제거용 공통 베이스.
 * catch는 항상 구체 타입으로 (베이스로 잡으면 다른 신호까지 같이 잡힘, 중복 방지 목적일 뿐).
 */
public abstract class ControlFlowSignal extends RuntimeException {
    protected ControlFlowSignal() {
        super(null, null, false, false);
    }
}
