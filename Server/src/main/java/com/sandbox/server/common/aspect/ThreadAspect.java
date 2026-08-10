package com.sandbox.server.common.aspect;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ThreadAspect {

    @Around("@annotation(com.sandbox.server.common.aspect.ThreadLog)")
    public Object logThreadInfo(ProceedingJoinPoint joinPoint) throws Throwable {

        val className = joinPoint.getTarget().getClass().getSimpleName();
        val message = String.format("[THREAD MONITOR]: %s: %s", className, Thread.currentThread().getName());
        log.info(message);

        return joinPoint.proceed();
    }

}
