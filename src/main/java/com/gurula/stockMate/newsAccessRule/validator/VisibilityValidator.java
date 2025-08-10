package com.gurula.stockMate.newsAccessRule.validator;

import com.gurula.stockMate.newsAccessRule.dto.CreatedNewsAccessRuleDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.Set;

public class VisibilityValidator implements ConstraintValidator<ValidVisibility, Object> {
    @Override
    public void initialize(ValidVisibility constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Object visibility = getFieldValue(value, "visibility");

            if (visibility == null) {
                return true;
            }

            String visibilityName = visibility.toString();

            return switch (visibilityName) {
                case "RESTRICTED" -> hasNonEmptySet(value, "visibleToMemberIds");
                case "GROUP" -> hasNonEmptySet(value, "visibleToGroupIds");
                case "ROLE" -> hasNonEmptySet(value, "visibleToRoleIds");
                default -> true;
            };
        } catch (Exception e) {
            // 如果該 class 沒有對應欄位，直接通過
            return true;
        }
    }

    private Object getFieldValue(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private boolean hasNonEmptySet(Object obj, String fieldName) throws Exception {
        Object fieldValue = getFieldValue(obj, fieldName);
        return fieldValue instanceof Set && !((Set<?>) fieldValue).isEmpty();
    }
}
