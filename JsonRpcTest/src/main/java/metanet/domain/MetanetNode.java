package metanet.domain;

import org.bitcoinj.crypto.DeterministicKey;

import java.util.List;

/**
 * @description: Fundamental class for Metanet node, which is used to store data about current node
 * @author YAO Chao
 * @date: 2019/06/21
 **/
public class MetanetNode {

	/**
	 * @description: base64 format of pubKey of current node
	 * @date: 2019/06/21
	 **/
	private String pubKey;

	/**
	 * @description: privKey, pubKey and pathNum
	 * @date: 2019/06/24
	 **/
	private DeterministicKey key;

	/**
	 * @description: Balance of current address
	 * @date: 2019/06/22
	 **/
	private long balance;

	/**
	 * @description: the latest txid of meta-output
	 * @date: 2019/06/24
	 **/
	private String currentVersion;

	/**
	 * @description: the latest content of data
	 * @date: 2019/06/24
	 **/
	private List<String> currentData;

	/**
	 * @description: Transaction data which contain the OP_RETURN data, the first one in the list
	 * is the latest version of the data, we can edit data through changing the version of data
	 * @date: 2019/06/21
	 **/
	private List<MetanetNodeData> dataList;

	/**
	 * @description: UTXOs belong to the address of current metanet node
	 * @date: 2019/06/21
	 **/
	private List<MetanetNodeUTXO> utxoList;

	/**
	 * @description: Parent node
	 * @date: 2019/06/21
	 **/
	private MetanetNode parent;

	/**
	 * @description: Child nodes
	 * @date: 2019/06/21
	 **/
	private List<MetanetNode> children;

	public MetanetNode(String pubKey, DeterministicKey key, MetanetNode parent) {
		this.pubKey = pubKey;
		this.key = key;
		this.parent = parent;
	}

	public String getPubKey() {
		return pubKey;
	}

	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}

	public long getBalance() {
		return balance;
	}

	public DeterministicKey getKey() {
		return key;
	}

	public void setKey(DeterministicKey key) {
		this.key = key;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	public List<MetanetNodeData> getDataList() {
		return dataList;
	}

	public void setDataList(List<MetanetNodeData> dataList) {
		this.dataList = dataList;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}

	public List<String> getCurrentData() {
		return currentData;
	}

	public void setCurrentData(List<String> currentData) {
		this.currentData = currentData;
	}

	public List<MetanetNodeUTXO> getUtxoList() {
		return utxoList;
	}

	public void setUtxoList(List<MetanetNodeUTXO> utxoList) {
		this.utxoList = utxoList;
	}

	public MetanetNode getParent() {
		return parent;
	}

	public void setParent(MetanetNode parent) {
		this.parent = parent;
	}

	public List<MetanetNode> getChildren() {
		return children;
	}

	public void setChildren(List<MetanetNode> children) {
		this.children = children;
	}
}
