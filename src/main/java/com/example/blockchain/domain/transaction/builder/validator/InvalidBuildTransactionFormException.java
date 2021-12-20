package com.example.blockchain.domain.transaction.builder.validator;

public class InvalidBuildTransactionFormException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private BuildTransactionFormValidationResult validationResult;

	public InvalidBuildTransactionFormException(BuildTransactionFormValidationResult validationResult) {
		this.validationResult = validationResult;
	}

	public BuildTransactionFormValidationResult getValidationResult() {
		return validationResult;
	}

	public void setValidationResult(BuildTransactionFormValidationResult validationResult) {
		this.validationResult = validationResult;
	}
}
