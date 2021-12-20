package com.example.blockchain;

public interface Callback<T> {

	void onSuccess(T response);

	void onFailure(T response);

	public static Callback<Void> createEmptyTypeVoid() {
		return new Callback<Void>() {
			@Override
			public void onSuccess(Void response) {

			}

			@Override
			public void onFailure(Void response) {

			}
		};
	}
}
