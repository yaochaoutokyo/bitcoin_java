package metanet.domain;

import java.util.List;

/**
 * @description: Class for Metanet node, which is used to store data about current node
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
	 * @description: Path of current node in the HD hierarchy, which is used to retrieve private
	 * key information from masterKey
	 * @date: 2019/06/21
	 **/
	private String path;

	/**
	 * @description: Balance of current address
	 * @param null
	 * @date: 2019/06/22
	 **/
	private long balance;

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

	public MetanetNode(String pubKey, String path, MetanetNode parent) {
		this.pubKey = pubKey;
		this.path = path;
		this.parent = parent;
	}

	public String getPubKey() {
		return pubKey;
	}

	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getBalance() {
		return balance;
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
