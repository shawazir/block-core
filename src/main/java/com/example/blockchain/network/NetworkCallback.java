package com.example.blockchain.network;

public interface NetworkCallback<T> {

	void onSuccess(T response);

	void onFailure(T response);
}
