package com.example.blockchain.domain.block.builder.validator;

import java.math.BigInteger;
import java.util.Date;

import com.example.blockchain.domain.block.Block;
import com.example.blockchain.domain.block.builder.BuildBlockForm;
import com.example.blockchain.domain.transaction.Transaction;

public class BlockBuilderV1Validator {

	public BuildBlockFormValidationResult validateBuildBlockForm(BuildBlockForm buildBlockForm) {
		BuildBlockFormValidationResult result = validatePreviousBlock(buildBlockForm.getPreviousBlock());
		if (result != BuildBlockFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateTransactions(buildBlockForm.getTransactions());
		if (result != BuildBlockFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateTime(buildBlockForm.getTime());
		if (result != BuildBlockFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateTarget(buildBlockForm.getTarget());
		if (result != BuildBlockFormValidationResult.SUCCESS) {
			return result;
		}

		return BuildBlockFormValidationResult.SUCCESS;
	}

	private BuildBlockFormValidationResult validatePreviousBlock(Block previousBlock) {
		// Validates previousBlock is not null
		if (previousBlock == null) {
			return BuildBlockFormValidationResult.INVALID_PREVIOUS_BLOCK;
		}

		return BuildBlockFormValidationResult.SUCCESS;
	}

	private BuildBlockFormValidationResult validateTransactions(Transaction[] transactions) {
		// Validates transactions is not null or empty
		if (transactions == null || transactions.length == 0) {
			return BuildBlockFormValidationResult.INVALID_TRANSACTIONS;
		}

		// Validates the values in transactions are not null
		for (Transaction transaction : transactions) {
			if (transaction == null) {
				return BuildBlockFormValidationResult.INVALID_TRANSACTIONS;
			}
		}

		return BuildBlockFormValidationResult.SUCCESS;
	}

	private BuildBlockFormValidationResult validateTime(Date time) {
		// Validates time is not null
		if (time == null) {
			return BuildBlockFormValidationResult.INVALID_TIME;
		}

		return BuildBlockFormValidationResult.SUCCESS;
	}

	private BuildBlockFormValidationResult validateTarget(BigInteger target) {
		// Validates target is not null
		if (target == null) {
			return BuildBlockFormValidationResult.INVALID_TARGET;
		}

		return BuildBlockFormValidationResult.SUCCESS;
	}
}
