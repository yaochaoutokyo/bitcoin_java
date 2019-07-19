package metanet.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import metanet.domain.MetanetNode;
import metanet.domain.MetanetNodeData;
import metanet.domain.MetnetNodeUTXO;
import metanet.utils.HDHierarchyKeyGenerator;
import metanet.utils.PlanariaQueryUrlBuilder;
import metanet.utils.HttpRequestSender;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;

import java.io.IOException;
import java.util.*;

/**
 * @description: metanet format is the transaction with the inputs of Sig_parent and PubKey_Parent,
 * and the outputs of OP_RETURN, Metanet_Flag, PubKey_node, Txid_Parent, and payload.
 * For Root node, it has OP_RETURN meta P_root and its own txide, without Txid_parent and payloads
 * @author YAO Chao
 * @date: 2019/06/22
 **/
// todo: fix the problem of data version controll and restore node location in HD key tree
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
	public MetanetNode getMetanetTree(MetanetNode rootNode) {
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
	 * @param currentNode current metanet node
	 * @date: 2019/06/22
	 **/
	public MetanetNode getMetanetNodeInfo(MetanetNode currentNode) {
		getAndSetNodeDataList(currentNode, null);
		getAndSetUTXOList(currentNode);
		countAndSetBalance(currentNode);
		getAndSetChildrenNode(currentNode, null);
		return currentNode;
	}

	/**
	 * @description: Get Txids of metanet data and parent of current node. For normal metanet node,
	 * it must have OP_RETURN meta PubKey_node Txid_prarent, the payloads of normal metanet node can be empty,
	 * but can't be null; However, for root metanet node, it must have OP_RETURN meta PubKey_root,
	 * the payloads and parent_txid of root node is null
	 * @param currentNode current metanet node
	 * @date: 2019/06/22
	 **/
	public List<MetanetNodeData> getAndSetNodeDataList(MetanetNode currentNode, Integer limit) {
		List<MetanetNodeData> dataHistoryList = new ArrayList<>();
		String json = getMetaTxSentToCurrentNode(currentNode, limit);
		// the newest data is current data, so start from unconfirmed tx to traverse from newest tx to oldest tx
		try {
			JsonNode unconfirmedTxsNode = objectMapper.readTree(json).at("/u");
			parseTxJsonNodeIntoDataList(unconfirmedTxsNode, dataHistoryList, currentNode);
			JsonNode confirmedTxsNode = objectMapper.readTree(json).at("/c");
			parseTxJsonNodeIntoDataList(confirmedTxsNode, dataHistoryList, currentNode);
		} catch (JsonProcessingException e) {
			System.out.println("fail to parse json");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("fail to read json node");
			e.printStackTrace();
		}
		currentNode.setDataHistoryList(dataHistoryList);
		if (! dataHistoryList.isEmpty()) {
			// todo: decide the version order of data by payload item, instead of confirmed time order
			MetanetNodeData latestData = dataHistoryList.get(0);
			currentNode.setVersion(latestData.getTxid());
			currentNode.setData(latestData.getPayloads());
		}
		return dataHistoryList;
	}

	/**
	 * @description: Get UTXO list belong to current node
	 * @param currentNode current metanet node
	 * @date: 2019/06/22
	 **/
	public List<MetnetNodeUTXO> getAndSetUTXOList(MetanetNode currentNode) {
		String json = HttpRequestSender.getUtxoForBase64PubKey(currentNode.getPubKey(), params);
		List<MetnetNodeUTXO> utxoList = gson.fromJson(json, new TypeToken<List<MetnetNodeUTXO>>(){}.getType());
		currentNode.setUtxoList(utxoList);
		return utxoList;
	}

	/**
	 * @description: Count balance of current node
	 * @param currentNode current metanet node
	 * @date: 2019/06/22
	 **/
	public long countAndSetBalance(MetanetNode currentNode) {
		if (currentNode.getUtxoList() == null) {
			getAndSetUTXOList(currentNode);
		}
		long balance = 0;
		for (MetnetNodeUTXO utxo : currentNode.getUtxoList()) {
			balance += utxo.getValue();
		}
		currentNode.setBalance(balance);
		return balance;
	}

	/**
	 * @description: Get pubKeys of children of current node
	 * @param currentNode current metanet node
	 * @date: 2019/06/22
	 **/
	public List<MetanetNode> getAndSetChildrenNode(MetanetNode currentNode, Integer limit) {
		LinkedHashSet<String> childrenAddressSet = new LinkedHashSet<>();
		String json = getMetaTxSentFromCurrentNode(currentNode, limit);
		// need to traverse from oldest tx to newest tx, so start from confirmed transaction
		try {
			JsonNode confirmedTxsNode = objectMapper.readTree(json).at("/c");
			parseTxJsonIntoChildrenPubKeySet(confirmedTxsNode, childrenAddressSet);
			JsonNode unconfirmedTxsNode = objectMapper.readTree(json).at("/u");
			parseTxJsonIntoChildrenPubKeySet(unconfirmedTxsNode, childrenAddressSet);
		} catch (JsonProcessingException e) {
			System.out.println("fail to parse json");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("fail to read json node");
			e.printStackTrace();
		}
		List<MetanetNode> children = new ArrayList<>();
		DeterministicKey parentKey = currentNode.getKey();
		int indexOfChild = 0;
		for (String childAddress : childrenAddressSet) {
			// todo: decide the order of child node by data payload, instead of the confirmed time order.
			DeterministicKey childKey = HDHierarchyKeyGenerator.deriveChildKeyFromParentKey(parentKey, indexOfChild);
			MetanetNode child = new MetanetNode(params, childKey, currentNode);
			children.add(child);
			indexOfChild++;
		}
		currentNode.setChildren(children);
		return children;
	}


	/**
	 * @description: Get json of metanet format transactions sent to current metanet node.
	 * @param currentNode current metanet node
	 * @return json
	 * @date: 2019/06/22
	 **/
	private String getMetaTxSentToCurrentNode(MetanetNode currentNode, Integer limit) {
		PlanariaQueryUrlBuilder builder = new PlanariaQueryUrlBuilder();
		String url = builder
				.addOpReturn()
				.addMetaFlag()
				.addChildNodeAddress(currentNode.getAddress())
				.setQueryLimit(limit)
				.buildUrl();
		String json = HttpRequestSender.sendHttpRequestToPlanaria(url);
		return json;
	}

	/**
	 * @description: Get json of metanet format transactions sent from current metanet node.
	 * @param currentNode current metanet node
	 * @return json
	 * @date: 2019/06/22
	 **/
	private String getMetaTxSentFromCurrentNode(MetanetNode currentNode, Integer limit) {
		PlanariaQueryUrlBuilder builder = new PlanariaQueryUrlBuilder();
		String url = builder
				.addOpReturn()
				.addMetaFlag()
				.addParentNodePubKey(currentNode.getPubKey())
				.setQueryLimit(limit)
				.buildUrl();
		String json = HttpRequestSender.sendHttpRequestToPlanaria(url);
		return json;
	}

	/**
	 * @description: A common method to add pubKeys of children into a treeSet. It is able to buildRawTxHex
	 * a metanet transaction with more than one metanet outputs, but each child can only have one output
	 * @param txsNode jsonNode of uncomfirmed or comfired txs
	 * @param childrenSet resluting set of children pubKeys
	 * @date: 2019/06/22
	 **/
	private void parseTxJsonIntoChildrenPubKeySet(JsonNode txsNode, LinkedHashSet<String> childrenSet)
			throws JsonProcessingException {
		int txNodeNum = txsNode.size();
		// reverse order
		for (int i = txNodeNum - 1; i >= 0; i--) {
			JsonNode txNode = txsNode.get(i);
			JsonNode outputsNode = txNode.at("/out");
			for (JsonNode output : outputsNode) {
				JsonNode metaNode = output.at("/s1");
				String metaFlag = metaNode.toString().isEmpty() ? null : objectMapper.treeToValue(metaNode, String.class);
				if (metaFlag != null && metaFlag.equals(META)) {
					String childAddress = objectMapper.treeToValue(output.at("/s2"), String.class);
					// todo: set s5 be the location of child node in HD Key tree
					if (! childrenSet.contains(childAddress)) {
						childrenSet.add(childAddress);
					}
				}
			}
		}
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
					String childAddress = objectMapper.treeToValue(output.at("/s2"), String.class);
					if (currentNode.getAddress().equals(childAddress)) {
						List<String> payloads = new ArrayList<>();
						JsonNode payLoadNode = output.at("/s3");
						int index = 3;
						while (! payLoadNode.toString().isEmpty()) {
							String payload = objectMapper.treeToValue(payLoadNode, String.class);
							payloads.add(payload);
							String nextPayloadNodePath = String.format("/s%s", ++index);
							payLoadNode = output.at(nextPayloadNodePath);
						}
						data.setPayloads(payloads);
						// todo: support multi-output to one child key
						// the is only one meta-data output belong to current node, so once it has been found, break the loop
						break;
					}
				}
			}
			dataList.add(data);
		}
	}
}
