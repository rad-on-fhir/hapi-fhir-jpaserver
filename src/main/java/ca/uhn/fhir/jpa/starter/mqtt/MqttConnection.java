package ca.uhn.fhir.jpa.starter.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MqttConnection implements MqttCallback, DisposableBean {
	private static final AtomicInteger THREAD_COUNT = new AtomicInteger();
	private final String brokerUrl;
	final private String clientId = UUID.randomUUID().toString();
	final int qos;

	private MqttClient mqttClient = null;
	private final MqttConnectOptions connectionOptions;
	private final MemoryPersistence persistence;

	private final Queue<MqttFhirMessage> messageQueue = new LinkedBlockingQueue<>();

	private final Logger logger = LoggerFactory.getLogger(MqttConnection.class);
	private volatile boolean run = true;

	public MqttConnection(MqttConfig config) {
		this.brokerUrl = (config.useSSL ? config.SSL : config.TCP) + config.host + ":" + config.port;
		this.persistence = new MemoryPersistence();
		this.qos = config.qos;
		this.connectionOptions = new MqttConnectOptions();
		this.connectionOptions.setCleanSession(true);
		if (!config.userName.isEmpty() || !config.password.isEmpty()) {
			this.connectionOptions.setPassword(config.password.toCharArray());
			this.connectionOptions.setUserName(config.userName);
		}
		Thread backgroundThread = new Thread(this::backgroundTask);
		backgroundThread.setName("MqttConnection-Thread-" + THREAD_COUNT.incrementAndGet());
		backgroundThread.setDaemon(true);
		backgroundThread.start();
		// Finally connect to backgroundTask
		connect();
	}

	private void backgroundTask() {
		long delay = 20;
		while (run) {
			try {
				MqttFhirMessage msg = this.messageQueue.poll();
				if (msg != null) {
					try {
						this.mqttClient.publish(msg.getTopic(), msg.getPayload(), msg.getQos(), msg.isRetained());
					} catch (Exception e) {
						this.messageQueue.add(msg);
					}
				} else {
					//noinspection BusyWait
					Thread.sleep(delay);
				}
			} catch (InterruptedException ie) {
				run = false;
			} catch (Exception e) {
				this.logger.info("Error in Background-Thread " + e.getMessage(), e);
				}
		}
	}

	private void connect() {
		try {
			if (this.mqttClient != null) {
				this.mqttClient.close(true);
			}
			this.mqttClient = new MqttClient(brokerUrl, clientId, persistence);
			this.mqttClient.connect(this.connectionOptions);
			this.mqttClient.setCallback(this);
		} catch (Exception e) {
			throw new RuntimeException("Could not Connect to Mqtt " + brokerUrl, e);
		}
	}

	public void send(String topic, byte[] payload) {
		this.send(topic, payload, qos, false);
	}

	public void send(String topic, byte[] payload,int qos, boolean retained) {
		this.messageQueue.add(new MqttFhirMessage(topic, payload, qos, retained));
	}

	@Override
	public void connectionLost(Throwable throwable) {
		this.logger.error("Lost Connection to Mqtt at " + brokerUrl, throwable);
		if (run) {
			this.connect();
		} else {
			this.logger.info("Won't reconnect - shutting down");
		}
	}

	@Override
	public void messageArrived(String s, MqttMessage mqttMessage) {
		this.logger.info("Got a Message '" + s + "' : " + mqttMessage);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
		this.logger.debug("Delivery Completed " + iMqttDeliveryToken);
	}

	@Override
	public void destroy() throws Exception {
		this.run = false;
		this.mqttClient.close(true);
		this.mqttClient = null;
	}
}
