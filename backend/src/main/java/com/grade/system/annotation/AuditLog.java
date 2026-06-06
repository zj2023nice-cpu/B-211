package com.grade.system.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    
    String module() default "";
    
    String action() default "";
    
    String description() default "";
    
    boolean saveParams() default true;
    
    boolean saveResult() default false;
}
