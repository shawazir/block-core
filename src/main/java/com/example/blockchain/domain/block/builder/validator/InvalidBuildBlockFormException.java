package com.example.blockchain.domain.block.builder.validator;

public class InvalidBuildBlockFormException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private BuildBlockFormValidationResult validationResult;

	public InvalidBuildBlockFormException(BuildBlockFormValidationResult validationResult) {
		this.validationResult = validationResult;
	}

	public BuildBlockFormValidationResult getValidationResult() {
		return validationResult;
	}

	public void setValidationResult(BuildBlockFormValidationResult validationResult) {
		this.validationResult = validationResult;
	}
}
