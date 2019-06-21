package mywallet.domain;

/**
 * Created by yaochao on 2019/06/19
 */
public class MetanetNodeUTXO {

	/**
	 * @description: Address of node
	 * @date: 2019/06/21
	 **/
	private String address;

	/**
	 * @description: Transaction id of the UTXO
	 * @date: 2019/06/21
	 **/
	private String txid;

	/**
	 * @description: Index of the UTXO
	 * @date: 2019/06/21
	 **/
	private long vout;

	/**
	 * @description: Amount of the UTXO (satoshi)
	 * @date: 2019/06/21
	 **/
	private String amount;

	/**
	 * @description: The locking Script of UTXO
	 * @param null
	 * @date: 2019/06/21
	 **/
	private String scriptPubKey;

	/**
	 * @description: Block height of the UTXO
	 * @date: 2019/06/21
	 **/
	private int height;

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

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getScriptPubKey() {
		return scriptPubKey;
	}

	public void setScriptPubKey(String scriptPubKey) {
		this.scriptPubKey = scriptPubKey;
	}
}
