package com.example.blockchain.domain.clock;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.blockchain.Callback;
import com.example.blockchain.network.NetworkCallback;
import com.example.blockchain.network.NetworkInterface;
import com.example.blockchain.network.NodeAvailabilitySubscriber;
import com.example.blockchain.network.handler.GetResponse;

public class NetworkClock implements NodeAvailabilitySubscriber {

	private static final Logger log = LoggerFactory.getLogger(NetworkClock.class);

	private static final int NETWORK_TIME_UPDATE_INTERVAL = 1000 * 60 * 60 * 24; // 24 hours
	private static final int RETRIEVAL_DELAY_AFTER_FAILURE = 1000 * 60 * 5; // Five minutes
	private static final int ALLOWED_DIFFERENCE_FROM_SYSTEM_TIME = 1000 * 60 * 70; // 70 minutes
	private static final int TARGET_NUMBER_OF_NODES = 10;
	private static final int MIN_NUMBER_OF_NODES = 5;

	private NetworkInterface networkInterface;

	private Date networkTime = new Date();
	private Timer timer = new Timer();
	private boolean started = false;
	private boolean needsToUpdateWithEnoughNodes = false;

	public NetworkClock() {

	}

	public boolean isStarted() {
		return started;
	}

	/**
	 * Gets the network time.
	 * 
	 */
	public Date getNetworkTime() {
		return networkTime;
	}

	public void start(NetworkInterface networkInterface, Callback<Void> callback) {
		if (this.networkInterface != null) {
			throw new RuntimeException("Network clock already started");
		}
		this.networkInterface = networkInterface;
		this.networkInterface.subscribeNodeAvailabilitySubscriber(this, MIN_NUMBER_OF_NODES);
		started = true;
		updateNetworkTime(callback);
	}

	/**
	 * Starts the network time update process.
	 * 
	 */
	public void updateNetworkTime() {
		updateNetworkTime(Callback.createEmptyTypeVoid());
	}

	/**
	 * Starts the network time update process.
	 * 
	 */
	public void updateNetworkTime(Callback<Void> callback) {
		if (this.networkInterface == null) {
			throw new RuntimeException("Network clock should be started first");
		}
		startUpdatingNetworkTime(callback);
	}

	private void startUpdatingNetworkTime(Callback<Void> callback) {
		log.debug("Started updating network time");
		NetworkCallback<GetResponse<Date>> networkCallback = new NetworkCallback<GetResponse<Date>>() {
			@Override
			public void onSuccess(GetResponse<Date> response) {
				finishUpdatingNetworkTime(response.getValues(), callback);
			}

			@Override
			public void onFailure(GetResponse<Date> response) {
				scheduleRetrievalProcess(RETRIEVAL_DELAY_AFTER_FAILURE);
				log.info("Failed to update time from network due to no replies");
				callback.onFailure(null);
			}
		};
		boolean enoughNodes = networkInterface.getNetworkTime(TARGET_NUMBER_OF_NODES, MIN_NUMBER_OF_NODES, networkCallback);
		if (!enoughNodes) {
			scheduleRetrievalProcess(RETRIEVAL_DELAY_AFTER_FAILURE);
			needsToUpdateWithEnoughNodes = true;
			log.info("Failed to update time from network due to not enough nodes");
			callback.onFailure(null);
		}
	}

	private void finishUpdatingNetworkTime(List<Date> retrievedNetworkTimes, Callback<Void> callback) {
		boolean returnedValuesNotEnough = retrievedNetworkTimes.size() < MIN_NUMBER_OF_NODES;
		if (returnedValuesNotEnough) {
			scheduleRetrievalProcess(RETRIEVAL_DELAY_AFTER_FAILURE);
			log.info("Failed to update time from network due to no replies");
			callback.onFailure(null);
			return;
		}

		Date updatedNetworkTime = computeNetworkTime(retrievedNetworkTimes);
		boolean invalidMedian = (updatedNetworkTime == null);
		if (invalidMedian) {
			scheduleRetrievalProcess(RETRIEVAL_DELAY_AFTER_FAILURE);
			log.info("Failed to update time from network due to computed value being invalid");
			callback.onFailure(null);
		} else {
			this.networkTime = updatedNetworkTime;
			scheduleRetrievalProcess(NETWORK_TIME_UPDATE_INTERVAL);
			log.info("Updated network time successfully");
			callback.onSuccess(null);
		}
	}

	private Date computeNetworkTime(List<Date> retrievedNetworkTimes) {
		Date networkTime = computeMedian(retrievedNetworkTimes);
		long differenceFromSystemTime = Math.abs(new Date().getTime() - networkTime.getTime());
		if (differenceFromSystemTime > ALLOWED_DIFFERENCE_FROM_SYSTEM_TIME) {
			return null;
		} else {
			return networkTime;
		}
	}

	private Date computeMedian(List<Date> retrievedNetworkTimes) {
		Collections.sort(retrievedNetworkTimes);
		if (retrievedNetworkTimes.size() % 2 == 0) {
			int index1 = (retrievedNetworkTimes.size() / 2) - 1;
			int index2 = retrievedNetworkTimes.size() / 2;
			Date date = new Date((retrievedNetworkTimes.get(index1).getTime() + retrievedNetworkTimes.get(index2).getTime()) / 2);
			return date;
		} else {
			int index = retrievedNetworkTimes.size() / 2;
			return retrievedNetworkTimes.get(index);
		}
	}

	private void scheduleRetrievalProcess(long delay) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				startUpdatingNetworkTime(Callback.createEmptyTypeVoid());
			}
		}, delay);
	}

	@Override
	public void onAvailability() {
		if (needsToUpdateWithEnoughNodes) {
			needsToUpdateWithEnoughNodes = false;
			updateNetworkTime();
		}
	}
}
