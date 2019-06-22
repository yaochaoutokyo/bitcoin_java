package mywallet.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mywallet.domain.MetanetNode;
import mywallet.utils.PlanariaQueryUrlBuilder;
import mywallet.utils.HttpRequestSender;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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

	/**
	 * metanet format is the transaction with the inputs of Sig_parent and PubKey_Parent,
	 * and the outputs of OP_RETURN, Metanet_Flag, PubKey_node, Txid_Parent, and payload.
	 **/

	/**
	 * @description: Get json of metanet format transactions sent from current metanet node.
	 * @param currentNode current metanet node
	 * @return json
	 * @date: 2019/06/22
	 **/
	private String getMetaTxSentFromCurrentNode(MetanetNode currentNode) throws IOException {
		PlanariaQueryUrlBuilder builder = new PlanariaQueryUrlBuilder();
		String url = builder
				.addOpReturn()
				.addMetaFlag()
				.addParentNodePubKey(currentNode.getPubKey())
				.buildUrl();
		String json = HttpRequestSender.sendHttpRequestToPlanaria(url);
		return json;
	}

	public void getMetanetNodeInfo(MetanetNode currentNode) throws IOException {
		getNodeDataTxidsAndParent(currentNode);
		getTxidsOfUTXO(currentNode);
		getChildrenNode(currentNode);
	}

	/**
	 * @description: Get json of metanet format transactions sent to current metanet node.
	 * @param currentNode current metanet node
	 * @return json
	 * @date: 2019/06/22
	 **/
	private String getMetaTxSentToCurrentNode(MetanetNode currentNode) throws IOException {
		PlanariaQueryUrlBuilder builder = new PlanariaQueryUrlBuilder();
		String url = builder
				.addOpReturn()
				.addMetaFlag()
				.addChildNodePubKeyScript(currentNode.getPubKey())
				.buildUrl();
		String json = HttpRequestSender.sendHttpRequestToPlanaria(url);
		return json;
	}

	/**
	 * @description: Get Txids of metanet data and parent of current node, the PubKey of parent
	 * is the pubkey of the first input
	 * @param currentNode current metanet node
	 * @date: 2019/06/22
	 **/
	public void getNodeDataTxidsAndParent(MetanetNode currentNode) throws IOException {
		List<String> dataTxids = new ArrayList<>();
		String json = getMetaTxSentToCurrentNode(currentNode);
		JsonNode uncomfirmedTxsNode = objectMapper.readTree(json).at("/u");
		String uncomfirmedParent = addDataTxidIntoList(uncomfirmedTxsNode, dataTxids);
		JsonNode comfirmedTxsNode = objectMapper.readTree(json).at("/c");
		String comfirmedparent = addDataTxidIntoList(comfirmedTxsNode, dataTxids);
		currentNode.setDataTxids(dataTxids);
		String parent = uncomfirmedParent != null ? uncomfirmedParent : comfirmedparent;
		currentNode.setParent(parent);
	}

	/**
	 * @description: A common method to add data txids of current node into a List, and return parent
	 * of this node
	 * @param txsNode jsonNode of uncomfirmed or comfired txs
	 * @param dataTxids resluting set of data txids
	 * @return parent pubKey of current node
	 * @date: 2019/06/22
	 **/
	private String addDataTxidIntoList(JsonNode txsNode, List<String> dataTxids) throws JsonProcessingException {
		String parent = null;
		for (JsonNode node : txsNode) {
			if (parent == null) {
				JsonNode firstInputNode = node.at("/in").get(0);
				parent = objectMapper.treeToValue(firstInputNode.at("/b1"), String.class);
			}
			String txid = objectMapper.treeToValue(node.at("/tx/h"), String.class);
			dataTxids.add(txid);
		}
		return parent;
	}

	/**
	 * @description: Get UTXO ids belong to current node
	 * @param currentNode current metanet node
	 * @date: 2019/06/22
	 **/
	public void getTxidsOfUTXO(MetanetNode currentNode) throws IOException {
		List<String> utxoTxids = new ArrayList<>();
		String json = HttpRequestSender.getUtxoForPubKey(currentNode.getPubKey(), params);
		JsonNode utxosNode = objectMapper.readTree(json);
		for (JsonNode utxo : utxosNode) {
			String utxoTxid = objectMapper.treeToValue(utxo.at("/txid"), String.class);
			utxoTxids.add(utxoTxid);
		}
		currentNode.setUtxoTxids(utxoTxids);
	}


	/**
	 * @description: Get pubKeys of children of current node
	 * @param currentNode current metanet node
	 * @date: 2019/06/22
	 **/
	public void getChildrenNode(MetanetNode currentNode) throws IOException {
		TreeSet<String> childrenSet = new TreeSet<>();
		String json = getMetaTxSentFromCurrentNode(currentNode);
		JsonNode uncomfirmedTxsNode = objectMapper.readTree(json).at("/u");
		addChildrenPubKeyIntoSet(uncomfirmedTxsNode, childrenSet);
		JsonNode comfirmedTxsNode = objectMapper.readTree(json).at("/c");
		addChildrenPubKeyIntoSet(comfirmedTxsNode, childrenSet);

		List<String> children = new ArrayList<>();
		for (String child : childrenSet) {
			children.add(child);
		}
		currentNode.setChildren(children);
	}

	/**
	 * @description: A common method to add pubKeys of children into a treeSet
	 * @param txsNode jsonNode of uncomfirmed or comfired txs
	 * @param childrenSet resluting set of children pubKeys
	 * @date: 2019/06/22
	 **/
	private void addChildrenPubKeyIntoSet(JsonNode txsNode, TreeSet<String> childrenSet) throws JsonProcessingException {
		for (JsonNode txNode : txsNode) {
			JsonNode outputsNode = txNode.at("/out");
			for (JsonNode output : outputsNode) {
				Integer opCode = objectMapper.treeToValue(output.at("/b0/op"), Integer.class);
				String childPubKey = objectMapper.treeToValue(output.at("/b2"), String.class);
				if (opCode != null && opCode.equals(106)) {
					childrenSet.add(childPubKey);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		NetworkParameters params = MainNetParams.get();
		MetanetNodeManager metanetNodeManager = new MetanetNodeManager(params);
		MetanetNode currentNode = new MetanetNode("A4QtyIYcWnnpK6D+4j0uNNi6m/buRjPhNnEMYl22E0gs", "M");
		String json = metanetNodeManager.getMetaTxSentFromCurrentNode(currentNode);
		metanetNodeManager.getTxidsOfUTXO(currentNode);
		metanetNodeManager.getChildrenNode(currentNode);
		metanetNodeManager.getNodeDataTxidsAndParent(currentNode);
		MetanetNode currentNode2 = new MetanetNode("Ah92lnR275QO2nvCHIrzFO4dGJbB1bY1RSEnTSCL5kzn", "M/1");
		String json2 = metanetNodeManager.getMetaTxSentToCurrentNode(currentNode2);
		metanetNodeManager.getTxidsOfUTXO(currentNode2);
		metanetNodeManager.getChildrenNode(currentNode2);
		metanetNodeManager.getNodeDataTxidsAndParent(currentNode2);
		System.out.println(json);
		System.out.println(json2);
	}
}
