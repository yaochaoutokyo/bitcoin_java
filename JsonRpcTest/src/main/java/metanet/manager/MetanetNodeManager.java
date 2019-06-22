package metanet.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import metanet.domain.MetanetNode;
import metanet.domain.MetanetNodeData;
import metanet.domain.MetanetNodeUTXO;
import metanet.utils.PlanariaQueryUrlBuilder;
import metanet.utils.HttpRequestSender;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import java.io.IOException;
import java.util.*;

/**
 * @description: metanet format is the transaction with the inputs of Sig_parent and PubKey_Parent,
 * and the outputs of OP_RETURN, Metanet_Flag, PubKey_node, Txid_Parent, and payload.
 * @author YAO Chao
 * @date: 2019/06/22
 **/

public class MetanetNodeManager {

	private ObjectMapper objectMapper;

	private NetworkParameters params;

	private Gson gson;

	private static final String META = "meta";

	public MetanetNodeManager(NetworkParameters params) {
		objectMapper = new ObjectMapper();
		gson = new Gson();
		this.params = params;
	}

	/**
	 * @description: Build the tree of Metanet nodes from the root node by using Breadth-First
	 * Search (BFS) algorithm
	 * @param rootNode root of the tree
	 * @date: 2019/06/22
	 **/
	public MetanetNode getMetanetTree(MetanetNode rootNode) throws IOException {
		// using Breadth-First Search to buildRawTxHex Metanet Tree
		Queue<MetanetNode> nodeQueue = new LinkedList<>();
		if (rootNode != null) {
			nodeQueue.offer(rootNode);
		}
		while (! nodeQueue.isEmpty()) {
			MetanetNode currentNode = nodeQueue.poll();
			getMetanetNodeInfo(currentNode);
			for (MetanetNode child : currentNode.getChildren()) {
				nodeQueue.offer(child);
			}
		}
		return rootNode;
	}

	/**
	 * @description: Collect information of currentNode
	 * @date: 2019/06/22
	 **/
	public MetanetNode getMetanetNodeInfo(MetanetNode currentNode) throws IOException {
		getAndSetNodeDataList(currentNode);
		getAndSetUTXOList(currentNode);
		countAndSetBalance(currentNode);
		getAndSetChildrenNode(currentNode);
		return currentNode;
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
	public List<MetanetNodeData> getAndSetNodeDataList(MetanetNode currentNode)
			throws IOException {
		List<MetanetNodeData> dataList = new ArrayList<>();
		String json = getMetaTxSentToCurrentNode(currentNode);
		JsonNode uncomfirmedTxsNode = objectMapper.readTree(json).at("/u");
		parseTxJsonNodeIntoDataList(uncomfirmedTxsNode, dataList, currentNode);
		JsonNode comfirmedTxsNode = objectMapper.readTree(json).at("/c");
		parseTxJsonNodeIntoDataList(comfirmedTxsNode, dataList, currentNode);
		currentNode.setDataList(dataList);
		return dataList;
	}

	/**
	 * @description: A common method to add meta-data of current node into a List. Parent node can
	 * generate more than one outputs to more than one child nodes, but currently for each child node,
	 * it can only have a output
	 * @param txsNode jsonNode of uncomfirmed or comfired txs
	 * @param dataList resluting set of data
	 * @date: 2019/06/22
	 **/
	private void parseTxJsonNodeIntoDataList(JsonNode txsNode, List<MetanetNodeData> dataList, MetanetNode currentNode)
			throws JsonProcessingException {
		for (JsonNode txNode : txsNode) {
			MetanetNodeData data = new MetanetNodeData();
			String txid = objectMapper.treeToValue(txNode.at("/tx/h"), String.class);
			data.setTxid(txid);

			// find the only one metanet output which belong to current node
			JsonNode outputsNode = txNode.at("/out");
			for (JsonNode output : outputsNode) {
				JsonNode metaNode = output.at("/s1");
				String metaFlag = metaNode.toString().isEmpty() ? null : objectMapper.treeToValue(metaNode, String.class);
				if (metaFlag != null && metaFlag.equals(META)) {
					String childPubKey = objectMapper.treeToValue(output.at("/b2"), String.class);
					if (currentNode.getPubKey().equals(childPubKey)) {
						String payload = objectMapper.treeToValue(output.at("/s4"), String.class);
						data.setPayload(payload);
						// todo: support multi-output to one child key
						// the is only one meta-data output belong to current node, so once it has been found, break the loop
						break;
					}
				}
			}
			dataList.add(data);
		}
	}

	/**
	 * @description: Get UTXO list belong to current node
	 * @param currentNode current metanet node
	 * @date: 2019/06/22
	 **/
	public List<MetanetNodeUTXO> getAndSetUTXOList(MetanetNode currentNode) throws IOException {
		String json = HttpRequestSender.getUtxoForPubKey(currentNode.getPubKey(), params);
		List<MetanetNodeUTXO> utxoList = gson.fromJson(json, new TypeToken<List<MetanetNodeUTXO>>(){}.getType());
		currentNode.setUtxoList(utxoList);
		return utxoList;
	}

	/**
	 * @description: Count balance of current node
	 * @param currentNode current metanet node
	 * @date: 2019/06/22
	 **/
	public long countAndSetBalance(MetanetNode currentNode) throws IOException {
		if (currentNode.getUtxoList() == null) {
			getAndSetUTXOList(currentNode);
		}
		long balance = 0;
		for (MetanetNodeUTXO utxo : currentNode.getUtxoList()) {
			balance += utxo.getValue();
		}
		currentNode.setBalance(balance);
		return balance;
	}


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

	/**
	 * @description: Get pubKeys of children of current node
	 * @param currentNode current metanet node
	 * @date: 2019/06/22
	 **/
	public List<MetanetNode> getAndSetChildrenNode(MetanetNode currentNode) throws IOException {
		TreeSet<String> childrenPubKeySet = new TreeSet<>();
		String json = getMetaTxSentFromCurrentNode(currentNode);
		JsonNode uncomfirmedTxsNode = objectMapper.readTree(json).at("/u");
		parseTxJsonIntoChildrenPubKeySet(uncomfirmedTxsNode, childrenPubKeySet);
		JsonNode comfirmedTxsNode = objectMapper.readTree(json).at("/c");
		parseTxJsonIntoChildrenPubKeySet(comfirmedTxsNode, childrenPubKeySet);

		List<MetanetNode> children = new ArrayList<>();
		int indexOfChildPath = 0;
		for (String childPubKey : childrenPubKeySet) {
			String childPath = String.format("%s/%s", currentNode.getPath(), indexOfChildPath);
			MetanetNode child = new MetanetNode(childPubKey, childPath, currentNode);
			children.add(child);
			indexOfChildPath++;
		}
		currentNode.setChildren(children);
		return children;
	}

	/**
	 * @description: A common method to add pubKeys of children into a treeSet. It is able to buildRawTxHex
	 * a metanet transaction with more than one metanet outputs, but each child can only have one output
	 * @param txsNode jsonNode of uncomfirmed or comfired txs
	 * @param childrenSet resluting set of children pubKeys
	 * @date: 2019/06/22
	 **/
	private void parseTxJsonIntoChildrenPubKeySet(JsonNode txsNode, TreeSet<String> childrenSet) throws JsonProcessingException {
		for (JsonNode txNode : txsNode) {
			JsonNode outputsNode = txNode.at("/out");
			for (JsonNode output : outputsNode) {
				JsonNode metaNode = output.at("/s1");
				String metaFlag = metaNode.toString().isEmpty() ? null : objectMapper.treeToValue(metaNode, String.class);
				if (metaFlag != null && metaFlag.equals(META)) {
					String childPubKey = objectMapper.treeToValue(output.at("/b2"), String.class);
					childrenSet.add(childPubKey);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		NetworkParameters params = MainNetParams.get();
		MetanetNodeManager metanetNodeManager = new MetanetNodeManager(params);
		MetanetNode currentNode = new MetanetNode("A4QtyIYcWnnpK6D+4j0uNNi6m/buRjPhNnEMYl22E0gs", "M",null);
		String json = metanetNodeManager.getMetaTxSentFromCurrentNode(currentNode);
		metanetNodeManager.getMetanetTree(currentNode);
		System.out.println(json);
	}
}
