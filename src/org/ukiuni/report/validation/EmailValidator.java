package org.ukiuni.report.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailValidator implements ConstraintValidator<Email, String> {

	public void initialize(Email email) {
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		return true;
	}
}
