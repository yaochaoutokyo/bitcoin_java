package metanet.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yaochao on 2019/06/23
 */
public class WhatsOnChainUTXOResp {

	@SerializedName("tx_hash")
	private String txid;

	@SerializedName("tx_pos")
	private long vout;

	private long value;

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
}
