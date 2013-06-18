package org.eclipse.mihini.coap.assetfactory;

import org.eclipse.mihini.connector.Asset;
import org.eclipse.mihini.connector.AssetFactory;

public class CoapAssetFactory implements AssetFactory {

	private CoapServer _coapServer;

	public CoapAssetFactory() {
		_coapServer = new CoapServer();
		Runnable r = new Runnable() {
			public void run() {

				_coapServer.start();
			}
		};
		new Thread(r).start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Asset createAsset(String assetId) {
		CoapAsset asset = new CoapAsset(assetId, _coapServer);
		return asset;
	}

}
