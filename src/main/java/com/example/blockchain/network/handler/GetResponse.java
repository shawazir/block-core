package com.example.blockchain.network.handler;

import java.util.List;

public class GetResponse<T> {

	private int targetedNumberOfReturns;
	private List<T> values;

	public GetResponse(int targetNumberOfReturns, List<T> values) {
		this.targetedNumberOfReturns = targetNumberOfReturns;
		this.values = values;
	}

	public boolean isFullySuccessful() {
		return values.size() == targetedNumberOfReturns;
	}

	public boolean isFullyFailed() {
		return values.size() == 0;
	}

	public int getTargetedNumberOfReturns() {
		return targetedNumberOfReturns;
	}

	public List<T> getValues() {
		return values;
	}
}
