package domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by intern-yao on 2019/07/20
 */
public class WhatsOnChainUTXO {

    private Integer height;

    @SerializedName("tx_pos")
    private Integer index;

    @SerializedName("tx_hash")
    private String txid;

    private Long value;

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
