package mywallet.domain;

import java.util.List;

/**
 * @description: Class for Metanet node, which is used to store data about current node
 * @author YAO Chao
 * @date: 2019/06/21
 **/
public class MetanetNode {

	/**
	 * @description: Address of current node
	 * @date: 2019/06/21
	 **/
	private String address;

	/**
	 * @description: Path of the address of current node in the HD hierarchy, which is used to
	 * retrieve private key and public key information from masterKey
	 * @date: 2019/06/21
	 **/
	private String path;

	/**
	 * @description: Transaction ids which contain the OP_RETURN data, the first one in the list
	 * is the latest version of the data, we can edit data through changing the version of data
	 * @date: 2019/06/21
	 **/
	private List<String> txids;

	/**
	 * @description: Reference to parent node
	 * @date: 2019/06/21
	 **/
	private String parent;

	/**
	 * @description: Addresses of child nodes
	 * @date: 2019/06/21
	 **/
	private List<String> children;

	public MetanetNode(String address) {
		this.address = address;
	}

	public MetanetNode(String address, String parent) {
		this.address = address;
		this.parent = parent;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<String> getTxids() {
		return txids;
	}

	public void setTxids(List<String> txids) {
		this.txids = txids;
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
}
