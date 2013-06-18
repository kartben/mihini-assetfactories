package org.eclipse.mihini.mqtt.assetfactory.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.eclipse.mihini.connector.Asset;
import org.eclipse.mihini.connector.Asset.DataHandler;
import org.eclipse.mihini.connector.AssetFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class MqttAssetFactory implements AssetFactory, MqttCallback {

	private MqttClient _mqttClient;
	private Map<String, DataHandler> _dataHandlers;

	public MqttAssetFactory() {
		_dataHandlers = new HashMap<String, Asset.DataHandler>();
		try {
			_mqttClient = new MqttClient("tcp://m2m.eclipse.org:1883",
					"mihini-mqtt-" + new Random().nextInt(1000000));
			_mqttClient.connect();
			_mqttClient.setCallback(this);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Asset createAsset(String assetId) {
		return new MqttAsset(assetId, this);
	}

	public MqttClient getMqttClient() {
		return _mqttClient;
	}

	public Map<String, DataHandler> getDataHandlers() {
		return _dataHandlers;
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(final String topic, MqttMessage message)
			throws Exception {
		if (_dataHandlers.containsKey(topic)) {
			new Thread(new Runnable() {
				public void run() {
					try {
						_mqttClient.publish(
								topic.replaceFirst("__READ__/", ""),
								new MqttMessage(_dataHandlers.get(topic)
										.value().toString().getBytes()));
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}).start();
		}

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}

}
