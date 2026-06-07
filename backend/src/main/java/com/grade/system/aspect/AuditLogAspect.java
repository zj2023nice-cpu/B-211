package com.grade.system.aspect;

import com.grade.system.context.UserContext;
import com.grade.system.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogService auditLogService;

    @Pointcut("@annotation(com.grade.system.annotation.AuditLog)")
    public void auditLogPointcut() {
    }

    @Around("auditLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        String userRole = UserContext.getUserRole();
        
        Object result = null;
        Throwable exception = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            auditLogService.createAuditLogFromJoinPoint(
                    joinPoint,
                    result,
                    exception,
                    userId,
                    username,
                    userRole
            );
        }
    }
}
