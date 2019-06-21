package mywallet.domain;

/**
 * @description: Class for storing metenet data
 * @author YAO Chao
 * @date: 2019/06/21
 **/
public class MetanetNodeData {
	/**
	 * @description: Id of transaction which contains metanet_flag output
	 * @date: 2019/06/21
	 **/
	private String txid;

	/**
	 * @description: The data of metanet output
	 * @date: 2019/06/21
	 **/
	private String data;

	public String getTxid() {
		return txid;
	}

	public void setTxid(String txid) {
		this.txid = txid;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
