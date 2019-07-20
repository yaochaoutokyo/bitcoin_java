package metanet.domain;

/**
 * @description: Metanet node utxo class
 * @author YAO Chao
 * @date: 2019/06/25
 **/
public class MetnetNodeUTXO {

	/**
	 * @description: Transaction id of the UTXO
	 * @date: 2019/06/21
	 **/
	private String txid;

	/**
	 * @description: Address of node
	 * @date: 2019/06/21
	 **/
	private String address;

	/**
	 * @description: Index of the UTXO
	 * @date: 2019/06/21
	 **/
	private long vout;

	/**
	 * @description: Amount of the UTXO (satoshi)
	 * @date: 2019/06/21
	 **/
	private long value;

	/**
	 * @description: The locking Script of UTXO
	 * @param null
	 * @date: 2019/06/21
	 **/
	private String scriptPubKey;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTxid() {
		return txid;
	}

	public void setTxid(String txid) {
		this.txid = txid;
	}

	public long getVout() {
		return vout;
	}

	public void setVout(long vout) {
		this.vout = vout;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public String getScriptPubKey() {
		return scriptPubKey;
	}

	public void setScriptPubKey(String scriptPubKey) {
		this.scriptPubKey = scriptPubKey;
	}
}
