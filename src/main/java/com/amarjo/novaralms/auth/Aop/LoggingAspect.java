package com.amarjo.novaralms.auth.Aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.amarjo.novaralms.auth.Services.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
    System.out.println("Calling method: " + joinPoint.getSignature().getName());
    }
    @After("execution(* com.amarjo.novaralms.auth.Services.*.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        System.out.println("Finished method: " + joinPoint.getSignature().getName());
    }
    @AfterReturning(pointcut = "execution(* com.amarjo.novaralms.auth.Services.*.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("Method"+ joinPoint.getSignature().getName() + "Returning result: " + result);
    }
}
