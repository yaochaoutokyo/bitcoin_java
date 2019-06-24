package metanet.utils;

import metanet.domain.MetanetNodeUTXO;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.util.List;

import static org.bitcoinj.core.Utils.HEX;

/**
 * Created by yaochao on 2019/06/22
 */
public class BsvTransactionBuilder {

	private Transaction tx;

	private TransactionOutput changeOutput;

	private NetworkParameters params;

	private static final byte[] METANET_FLAG = HEX.decode("6d657461");

	public BsvTransactionBuilder(NetworkParameters params) {
		this.params = params;
		tx = new Transaction(params);
	}

	/**
	 * @description: Add P2PSH output, like output for changing, and output for sending money
	 * @param targetAddress target address
	 * @param value value of BSV (satoshi)
	 * @date: 2019/06/22
	 **/
	public BsvTransactionBuilder addP2PKHOutput(Address targetAddress, Coin value) {
		tx.addOutput(value, targetAddress);
		return this;
	}

	/**
	 * @description: Add metanet-format root node output: the formula is
	 * OP_RETURN META_FLAG PubKey_root
	 * @param base64RootPubKey Base64 format of PubKey of root metanet node
	 * @date: 2019/06/23
	 **/
	public BsvTransactionBuilder addMetanetRootNodeOutput(String base64RootPubKey) {
		byte[] childNodePubKey = Base64.decode(base64RootPubKey);
		Script metanetRootOutputScript = new ScriptBuilder()
				.op(ScriptOpCodes.OP_RETURN)
				.data(METANET_FLAG)
				.data(childNodePubKey)
				.build();
		tx.addOutput(Coin.ZERO, metanetRootOutputScript);
		return this;
	}

	/**
	 * @description: Add Metanet-format OP_RETURN output: the formula is
	 * OP_RETURN META_FLAG PubKey_childNode TxHash_parentNode payload1 payload2....
	 * @param base64ChildPubKey Base64 format of PubKey of child metanet node
	 * @param parentNodeTxid Transaction Hash of parent node, usually the txHash of first input
	 * @param payloads a List of String
	 * @date: 2019/06/22
	 **/
	public BsvTransactionBuilder addMetanetChildNodeOutput(String base64ChildPubKey
			, String parentNodeTxid, List<String> payloads) {
		Sha256Hash parentNodeTxidHash = Sha256Hash.wrap(parentNodeTxid);
		byte[] childNodePubKey = Base64.decode(base64ChildPubKey);
		// Build the head of Metanet output
		ScriptBuilder payloadBuilder = new ScriptBuilder()
				.op(ScriptOpCodes.OP_RETURN)
				.data(METANET_FLAG)
				.data(childNodePubKey)
				.data(parentNodeTxidHash.getBytes());
		// put payloads into Metanet output
		for (String payload : payloads) {
			payloadBuilder.data(payload.getBytes());
		}
		Script metaNetOutputScript = payloadBuilder.build();
		tx.addOutput(Coin.ZERO, metaNetOutputScript);
		return this;
	}

	/**
	 * @description: Add all UTXOs of parent node into transaction and sign them
	 * @param utxoList utxos of parent node
	 * @param parentKey the ECKey of parent
	 * @date: 2019/06/24
	 **/
	public BsvTransactionBuilder addSignedInputs(List<MetanetNodeUTXO> utxoList, ECKey parentKey) {
		this.addInputs(utxoList);
		addSignatures(utxoList, parentKey);
		return this;
	}

	/**
	 * @description: Return the transaction object
	 * @return transaction object
	 * @date: 2019/06/24
	 **/
	public Transaction buildTx() {
		return tx;
	}

	/**
	 * @description: Return the hex of raw transaction
	 * @return hex of raw transaction
	 * @date: 2019/06/24
	 **/
	public String buildRawTxHex() {
		String txHex = HEX.encode(tx.bitcoinSerialize());
		return txHex;
	}

	/**
	 * @description: add all of the UTXOs of parent node into the transaction inputs
	 * @param utxoList utxos of parent node
	 * @date: 2019/06/24
	 **/
	private BsvTransactionBuilder addInputs(List<MetanetNodeUTXO> utxoList) {
		// add all input into transaction
		for (MetanetNodeUTXO utxo : utxoList) {
			Sha256Hash utxoHash = Sha256Hash.wrap(utxo.getTxid());
			tx.addInput(utxoHash, utxo.getVout(), new Script(new byte[]{}));
		}
		return this;
	}

	/**
	 * @description: Sign the inputs with the privKey of parent node
	 * @param utxoList utxos of parent node need to be signed
	 * @param parentKey the ECKey of parent
	 * @date: 2019/06/24
	 **/
	private BsvTransactionBuilder addSignatures(List<MetanetNodeUTXO> utxoList, ECKey parentKey) {
		for (int i = 0; i< utxoList.size(); i++) {
			// make signature with [ALL | FORK_ID]
			MetanetNodeUTXO utxo = utxoList.get(i);
			Script pubKeyScript = new Script(HEX.decode(utxo.getScriptPubKey()));
			Sha256Hash hash = tx.hashForSignatureWitness(i, pubKeyScript, Coin.SATOSHI.multiply(utxo.getValue()),
					Transaction.SigHash.ALL, false);
			ECKey.ECDSASignature ecSig = parentKey.sign(hash);
			TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false, true);

			// make sigScript with txSig
			Script sigScript = ScriptBuilder.createInputScript(txSig, parentKey);
			TransactionInput input = tx.getInputs().get(i);
			input.setScriptSig(sigScript);
		}
		return this;
	}
}
