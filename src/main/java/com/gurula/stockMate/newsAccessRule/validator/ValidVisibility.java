package com.gurula.stockMate.newsAccessRule.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VisibilityValidator.class)
@Documented
public @interface ValidVisibility {
    String message() default "權限範圍與對應 ID 條件不符合";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
