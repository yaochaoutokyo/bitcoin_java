package mywallet.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import mywallet.domain.MetanetNode;
import mywallet.utils.PlanariaRequestUrlBuilder;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import java.io.IOException;

import static mywallet.manager.RequestSender.sendHttpRequestToPlanaria;

/**
 * Created by yaochao on 2019/06/21
 */
public class MetanetNodeManager {

	private ObjectMapper objectMapper;



	private NetworkParameters params;

	public MetanetNodeManager(NetworkParameters params) {
		objectMapper = new ObjectMapper();
		this.params = params;
	}


	private String getMetaTxSendFromCurrentNode(MetanetNode currentNode) throws IOException {
		PlanariaRequestUrlBuilder builder = new PlanariaRequestUrlBuilder(params);
		String url = builder
				.addMetaFlag()
				.addInputAddress(currentNode.getAddress())
				.buildUrl();
		String json = RequestSender.sendHttpRequestToPlanaria(url);
		return json;
	}

	private String getMetaTxSentToCurrentNode(MetanetNode currentNode) throws IOException {
		PlanariaRequestUrlBuilder builder = new PlanariaRequestUrlBuilder(params);
		String url = builder
				.addMetaFlag()
				.addOutputPubKeyScript(currentNode.getAddress())
				.buildUrl();
		String json = RequestSender.sendHttpRequestToPlanaria(url);
		return json;
	}


	public void getNodeMetaTxids(MetanetNode currentNode) throws IOException {
		String json = getMetaTxSendFromCurrentNode(currentNode);
		JsonNode uncomfirmedTxNode = objectMapper.readTree(json).at("/u");
		for (JsonNode node : uncomfirmedTxNode) {

		}
		JsonNode comfirmedTxNode = objectMapper.readTree(json).at("/c");


	}

	public void getChildrenNode(MetanetNode currentNode) {

	}

	public static void main(String[] args) throws Exception {
		NetworkParameters params = MainNetParams.get();
		MetanetNodeManager metanetNodeManager = new MetanetNodeManager(params);
		MetanetNode currentNode = new MetanetNode("13FpbqJoYY3cCPfUomE1oxctXDWJQPoEvJ");
		String json = metanetNodeManager.getMetaTxSendFromCurrentNode(currentNode);
		MetanetNode currentNode2 = new MetanetNode("1HCZBF2cgyR6wVoqHUzL3W5Yx3KDAAyUHW");
		String json2 = metanetNodeManager.getMetaTxSentToCurrentNode(currentNode2);
		System.out.println(json);
		System.out.println(json2);
	}
}
