package com.processout.payment.gateway.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Documented
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreditCardDateValidator.class)
public @interface ValidCardDate {

    String message() default "Credit card has expired";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}