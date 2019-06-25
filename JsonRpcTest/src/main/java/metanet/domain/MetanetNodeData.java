package metanet.domain;

import java.util.List;

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
	private List<String> payloads;

	public String getTxid() {
		return txid;
	}

	public void setTxid(String txid) {
		this.txid = txid;
	}

	public List<String> getPayloads() {
		return payloads;
	}

	public void setPayloads(List<String> payloads) {
		this.payloads = payloads;
	}
}
