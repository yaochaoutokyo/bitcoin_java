package metanet.domain;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;

import java.util.Base64;
import java.util.List;

/**
 * @description: Fundamental class for Metanet node, which is used to store data about current node
 * @author YAO Chao
 * @date: 2019/06/21
 **/
public class MetanetNode {

	/**
	 * @description: address of current node
	 * @date: 2019/06/27
	 **/
	private String address;

	/**
	 * @description: pubKey of current node
	 **/
	private String pubKey;

	/**
	 * @description: the latest txid of meta-output
	 * @date: 2019/06/24
	 **/
	private String version;


	/**
	 * @description: the latest content of data
	 * @date: 2019/06/24
	 **/
	private List<String> data;

	/**
	 * @description: Balance of current address
	 * @date: 2019/06/22
	 **/
	private long balance;

	/**
	 * @description: privKey, pubKey and pathNum
	 * @date: 2019/06/24
	 **/
	private DeterministicKey key;

	/**
	 * @description: Transaction data which contain the OP_RETURN data, we can edit data
	 * through changing the version of data
	 * @date: 2019/06/21
	 **/
	private List<MetanetNodeData> dataHistoryList;

	/**
	 * @description: UTXOs belong to the address of current metanet node
	 * @date: 2019/06/21
	 **/
	private List<MetnetNodeUTXO> utxoList;

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

	public MetanetNode(NetworkParameters params, DeterministicKey key) {
		this(params, key, null);
	}

	public MetanetNode(NetworkParameters params, DeterministicKey key, MetanetNode parent) {
		this.key = key;
		this.parent = parent;
		address = key.toAddress(params).toBase58();
		pubKey = Base64.getEncoder().encodeToString(key.getPubKey());
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

	public List<MetanetNodeData> getDataHistoryList() {
		return dataHistoryList;
	}

	public void setDataHistoryList(List<MetanetNodeData> dataHistoryList) {
		this.dataHistoryList = dataHistoryList;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}

	public List<MetnetNodeUTXO> getUtxoList() {
		return utxoList;
	}

	public void setUtxoList(List<MetnetNodeUTXO> utxoList) {
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPubKey() {
		return pubKey;
	}

	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}
}
