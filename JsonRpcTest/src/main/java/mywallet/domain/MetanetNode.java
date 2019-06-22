package mywallet.domain;

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
	 * @description: Transaction ids which contain the OP_RETURN data, the first one in the list
	 * is the latest version of the data, we can edit data through changing the version of data
	 * @date: 2019/06/21
	 **/
	private List<String> dataTxids;

	/**
	 * @description: Transaction ids of UTXO belong to the address of current metanet node
	 * @date: 2019/06/21
	 **/
	private List<String> utxoTxids;

	/**
	 * @description: base64 format of pubKey of parent node
	 * @date: 2019/06/21
	 **/
	private String parent;

	/**
	 * @description: base64 format of pubKey of child nodes
	 * @date: 2019/06/21
	 **/
	private List<String> children;

	public MetanetNode(String pubKey, String path) {
		this.pubKey = pubKey;
		this.path = path;
	}

	public MetanetNode(String pubKey, String path, String parent) {
		this.pubKey = pubKey;
		this.path = path;
		this.parent = parent;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPubKey() {
		return pubKey;
	}

	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}

	public List<String> getDataTxids() {
		return dataTxids;
	}

	public void setDataTxids(List<String> dataTxids) {
		this.dataTxids = dataTxids;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public List<String> getChildren() {
		return children;
	}

	public void setChildren(List<String> children) {
		this.children = children;
	}

	public List<String> getUtxoTxids() {
		return utxoTxids;
	}

	public void setUtxoTxids(List<String> utxoTxids) {
		this.utxoTxids = utxoTxids;
	}
}
