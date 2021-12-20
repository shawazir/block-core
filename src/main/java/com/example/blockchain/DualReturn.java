package com.example.blockchain;

public class DualReturn<T1, T2> {

	private T1 value1;
	private T2 value2;

	public DualReturn(T1 value1, T2 value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	public T1 getValue1() {
		return value1;
	}

	public T2 getValue2() {
		return value2;
	}
}
