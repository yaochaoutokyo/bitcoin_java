package utils;

import domain.UserUTXO;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

import java.util.List;

import static org.bitcoinj.core.Utils.HEX;

/**
 * @description: class for building bsv transaction
 * @author YAO Chao
 * @date: 2019/06/25
 **/
public class TransactionBuilder {

	private Transaction tx;

	private NetworkParameters params;

	private static final String META = "meta";

	/**
	 * extra byte to ensure txFee is enough
	 */
	private static final long EXTRA_BYTES = 3L;

	/**
	 * initial length of a transaction
	 */
	private static final long INITIAL_LENGTH_BYTES = 10L;

	/**
	 * bytes per input
	 */
	private static final long BYTES_PER_INPUT = 148L;

	/**
	 * bytes per output
	 */
	private static final long BYTES_PER_OUTPUT = 34L;

	/**
	 * bytes per empty metanet output
	 */
	private static final long BYTES_PER_EMPTY_META_OUTPUT = 114L;

	/**
	 * bytes per root meta output
	 */
	private static final long BYTES_ROOT_META_OUTPUT = 55L;

	/**
	 * 1 char is 1 byte
	 */
	private static final long BYTES_PER_CHAR = 1L;

	/**
	 * 1 space is 1 byte
	 */
	private static final long BYTES_PER_SPACE = 1L;

	/**
	 * fee rate for per byte
	 */
	private long feePerByte = 1L;

	private long totalInput = 0;


	public TransactionBuilder(NetworkParameters params) {
		this.params = params;
		tx = new Transaction(params);
	}

	/**
	 * @description: Add P2PSH output, like output for changing, and output for sending money
	 * @param address target address
	 * @param value value of BSV (satoshi)
	 * @date: 2019/06/22
	 **/
	public TransactionBuilder addP2PKHOutput(String address, Long value) {
		Address addressObj = Address.fromBase58(params, address);
		Coin valueCoin = Coin.SATOSHI.multiply(value);
		tx.addOutput(valueCoin, addressObj);
		return this;
	}

	/**
	 * @description: Add Metanet-format OP_RETURN output: the formula is
	 * OP_RETURN META_FLAG PubKey_childNode TxHash_parentNode payload1 payload2....
	 * @param childAddress Base58 format of address of child metanet node
	 * @param parentTxid Transaction Hash of parent node, usually the txHash of first input
	 * @param payloads a List of String
	 * @date: 2019/06/22
	 **/
	public TransactionBuilder addMetanetChildNodeOutput(String childAddress
			, String parentTxid, List<String> payloads) {
		// Build the head of Metanet output
		ScriptBuilder payloadBuilder = new ScriptBuilder()
				.op(ScriptOpCodes.OP_RETURN)
				.data(META.getBytes())
				.data(childAddress.getBytes())
				.data(parentTxid.getBytes());
		// put payloads into Metanet output
		for (String payload : payloads) {
			payloadBuilder.data(payload.getBytes());
		}
		Script metaNetOutputScript = payloadBuilder.build();
		tx.addOutput(Coin.ZERO, metaNetOutputScript);
		return this;
	}

	/**
	 * @description: Add OP_RETURN output
	 * @param payloads a List of String
	 * @date: 2019/06/22
	 **/
	public TransactionBuilder addOpReturnOutput(List<String> payloads) {
		// Build the head of Metanet output
		ScriptBuilder payloadBuilder = new ScriptBuilder()
				.op(ScriptOpCodes.OP_RETURN);
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
	 * @param userKey the ECKey of parent
	 * @date: 2019/06/24
	 **/
	public TransactionBuilder addSignedInputs(List<UserUTXO> utxoList, ECKey userKey) {
		this.addInputs(utxoList);
		addSignatures(utxoList, userKey);
		return this;
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
	private TransactionBuilder addInputs(List<UserUTXO> utxoList) {
		// add all input into transaction
		for (UserUTXO utxo : utxoList) {
			Sha256Hash utxoHash = Sha256Hash.wrap(utxo.getTxid());
			tx.addInput(utxoHash, utxo.getVout(), new Script(new byte[]{}));
			totalInput += utxo.getValue();
		}
		return this;
	}

	/**
	 * @description: Sign the inputs with the privKey of parent node
	 * @param utxoList utxos of parent node need to be signed
	 * @param parentKey the ECKey of parent
	 * @date: 2019/06/24
	 **/
	private TransactionBuilder addSignatures(List<UserUTXO> utxoList, ECKey parentKey) {
		for (int i = 0; i< utxoList.size(); i++) {
			// make signature with [ALL | FORK_ID]
			UserUTXO utxo = utxoList.get(i);
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

	/**
	 * @description: estimate transaction fee (1 input = 148 B, 1 P2PSK output = 34 B;
	 * 1 empty metanet output = 81; 1 char = 1 B; 1 space = 1 B)
	 * @param inputNum number of inputs
	 * @param outputNum number of outputs
	 * @param metaOutputNum number of empty meta-outputs
	 * @param payloads list of payload
	 * @date: 2019/06/23
	 **/
	private Coin estimateTransactionFee(long inputNum, long outputNum, long metaOutputNum
			, long rootMetaOutputNum, List<String> payloads) {
		long inputFee = inputNum * BYTES_PER_INPUT;
		long outputFee = outputNum * BYTES_PER_OUTPUT;
		long rootMetaOutputFee = rootMetaOutputNum * BYTES_ROOT_META_OUTPUT;
		long metaOutputFee = metaOutputNum * BYTES_PER_EMPTY_META_OUTPUT;
		long payloadFee = 0L;
		if (payloads != null) {
			payloadFee = payloads.size() * BYTES_PER_SPACE;
			for (int i = 0; i < payloads.size(); i++) {
				String payload = payloads.get(i);
				payloadFee += BYTES_PER_CHAR * payload.length();
			}
		}
		long txFeeSum = EXTRA_BYTES + INITIAL_LENGTH_BYTES + inputFee + outputFee
				+ metaOutputFee + rootMetaOutputFee + payloadFee;
        return Coin.SATOSHI.multiply(txFeeSum * feePerByte);
	}

	public long getTotalInput() {
		return totalInput;
	}
}
