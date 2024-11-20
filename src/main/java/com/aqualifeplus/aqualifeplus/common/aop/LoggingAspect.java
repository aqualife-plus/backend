package com.aqualifeplus.aqualifeplus.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // 모든 서비스 메서드에 대해 실행
    @Around("execution(* com.aqualifeplus.aqualifeplus.controller..*(..)) && " +
            "!@within(com.aqualifeplus.aqualifeplus.common.aop.NoLogging)")
    public void logMethodName(ProceedingJoinPoint joinPoint) throws Throwable {
        // 로그 출력
        log.info("start : {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());

        try {
            joinPoint.proceed();
            log.info("proceed : {}.{}",
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        } catch (Throwable throwable) {
            throw throwable;
        }

        log.info("finish : {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
    }
}
