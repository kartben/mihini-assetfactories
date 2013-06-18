package org.eclipse.mihini.mqtt.assetfactory.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.mihini.connector.Asset;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class MqttAsset implements Asset {

	private String _assetId;
	private MqttAssetFactory _assetFactory;

	public MqttAsset(String id, MqttAssetFactory assetFactory) {
		_assetId = id;
		_assetFactory = assetFactory;

	}

	@Override
	public String getAssetId() {
		return _assetId;
	}

	@Override
	public void unregister() {
		// nothing to do for now. Later on it is likely that there will be a
		// pending subscription on some kind of /blabla/_assetId/commands that
		// we may want to unsubscribe
	}

	@Override
	public void pushData(String path, Map<String, Object> data, String queue) {
		for (Entry<String, Object> entry : data.entrySet()) {

			MqttMessage message = new MqttMessage(entry.getValue().toString()
					.getBytes());

			try {
				_assetFactory.getMqttClient().publish(
						'/' + _assetId + '/' + pathify(path) + '/'
								+ pathify(entry.getKey()), message);
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String pathify(String path) {
		return path.replace('.', '/');
	}

	@Override
	public void registerDataHandler(String path, DataHandler handler) {
		try {
			String topic = "/" + _assetId + "/__READ__/" + pathify(path);
			_assetFactory.getMqttClient().subscribe(topic);
			_assetFactory.getDataHandlers().put(topic, handler);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
