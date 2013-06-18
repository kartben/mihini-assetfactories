package org.eclipse.mihini.coap.assetfactory;

import java.util.Map;

import org.apache.mina.api.IoSession;
import org.apache.mina.coap.CoapCode;
import org.apache.mina.coap.CoapMessage;
import org.apache.mina.coap.CoapOption;
import org.apache.mina.coap.CoapOptionType;
import org.apache.mina.coap.resource.AbstractResourceHandler;
import org.apache.mina.coap.resource.CoapResponse;
import org.eclipse.mihini.connector.Asset;

public class CoapAsset implements Asset {

	private String _assetId;
	private CoapServer _coapServer;

	public CoapAsset(String assetId, CoapServer coapServer) {
		_assetId = assetId;
		_coapServer = coapServer;

		_coapServer.reg.register(new AbstractResourceHandler() {

			@Override
			public String getPath() {
				return _assetId;
			}

			@Override
			public CoapResponse handle(CoapMessage request, IoSession session) {
				return new CoapResponse(CoapCode.CONTENT.getCode(),
						"niah niah niah niah niah\n niah niah niah\n"
								.getBytes(),
						new CoapOption(CoapOptionType.CONTENT_FORMAT,
								new byte[] { 0 }));
			}

			@Override
			public String getTittle() {
				return "Some demo resource";
			}

		});
	}

	@Override
	public String getAssetId() {
		return _assetId;
	}

	@Override
	public void unregister() {
		// _coapServer.reg.unregister(_assetId.....);
	}

	@Override
	public void pushData(String path, Map<String, Object> data, String queue) {
		// TODO Auto-generated method stub
	}

	private String pathify(String path) {
		return path.replace('.', '/');
	}

	@Override
	public void registerDataHandler(final String path, final DataHandler handler) {
		_coapServer.reg.register(new AbstractResourceHandler() {

			@Override
			public String getPath() {
				return _assetId + '/' + pathify(path);
			}

			@Override
			public CoapResponse handle(CoapMessage request, IoSession session) {
				return new CoapResponse(CoapCode.CONTENT.getCode(), handler
						.value().toString().getBytes(), new CoapOption(
						CoapOptionType.CONTENT_FORMAT, new byte[] { 0 }));
			}

			@Override
			public String getTittle() {
				return "TODO";
			}

		});
	}

}
