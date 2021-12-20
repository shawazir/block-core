package com.example.blockchain.domain.block.builder.validator;

public enum BuildBlockFormValidationResult {

	SUCCESS,
	GENERAL_ERROR,
	INVALID_PREVIOUS_BLOCK,
	INVALID_TRANSACTIONS,
	INVALID_TIME,
	INVALID_TARGET;
}
