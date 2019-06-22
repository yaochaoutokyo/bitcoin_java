package metanet.utils;

import metanet.RealTest;
import metanet.domain.MetanetNode;
import metanet.domain.MetanetNodeUTXO;
import metanet.manager.MetanetNodeManager;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bitcoinj.core.Utils.HEX;

/**
 * Created by yaochao on 2019/06/22
 */
public class BsvTransactionBuilder {

	private Transaction tx;

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
	 * @description: Add Metanet-format OP_RETURN output: the formula is
	 * OP_RETURN META_FLAG PubKey_childNode TxHash_parentNode payload1 payload2....
	 * @param base64ChildPubKey Base64 format of PubKey of child metanet node
	 * @param parentNodeTxid Transaction Hash of parent node, usually the txHash of first input
	 * @param payloads a List of String
	 * @date: 2019/06/22
	 **/
	public BsvTransactionBuilder addMetanetOpReturnOutput(String base64ChildPubKey,
														  String parentNodeTxid, List<String> payloads) {
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

	public BsvTransactionBuilder addInputs(List<MetanetNodeUTXO> utxoList, ECKey parentKey) {
		// add all input into transaction
		for (MetanetNodeUTXO utxo : utxoList) {
			Sha256Hash utxoHash = Sha256Hash.wrap(utxo.getTxid());
			tx.addInput(utxoHash, utxo.getVout(), new Script(new byte[]{}));
		}

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

	public String buildRawTxHex() {
		String txHex = HEX.encode(tx.bitcoinSerialize());
		return txHex;
	}

	public static void main(String[] args) throws Exception {
		NetworkParameters params = MainNetParams.get();
		MetanetNodeManager metanetNodeManager = new MetanetNodeManager(params);
		MetanetNode currentNode = new MetanetNode("Ah92lnR275QO2nvCHIrzFO4dGJbB1bY1RSEnTSCL5kzn", "M/1",null);
		metanetNodeManager.getMetanetTree(currentNode);

		List<String> mnemonics = Arrays.asList(new String[]{"forum", "rug", "slice", "snack", "width", "inside",
				"mad", "cotton", "noodle", "april", "dumb", "adapt"});
		String passphrase = "123456";
		DeterministicKey masterKey = RealTest.restoreMasterKeyFromMnemonicCode(mnemonics,passphrase);
		DeterministicKey currentKey = RealTest.deriveChildKeyByPath(masterKey, currentNode.getPath());
		System.out.println(Base64.encode(currentKey.getPubKey()));

		BsvTransactionBuilder txBuilder = new BsvTransactionBuilder(params);
		List<MetanetNodeUTXO> currentNodeUtxoList = currentNode.getUtxoList();
		Coin currentNodeBalance = Coin.SATOSHI.multiply(currentNode.getBalance());
		Coin valueSendToChildNode = Coin.SATOSHI.multiply(10000);
		Coin txFee = Coin.SATOSHI.multiply(326);
		List<String> payloads = new ArrayList<>();
		payloads.add("multi");
		payloads.add("part");
		payloads.add("output");
		DeterministicKey childkey2 = RealTest.deriveChildKeyByPath(masterKey, "M/1/1");
		String txHex = txBuilder
				.addMetanetOpReturnOutput(Base64.encode(childkey2.getPubKey()),currentNodeUtxoList.get(0).getTxid(), payloads)
				.addP2PKHOutput(childkey2.toAddress(params), valueSendToChildNode)
				.addP2PKHOutput(currentKey.toAddress(params), currentNodeBalance.subtract(valueSendToChildNode).subtract(txFee))
				.addInputs(currentNodeUtxoList, currentKey)
				.buildRawTxHex();
		RealTest.decode(txHex);
		RealTest.broadcast(txHex);
		System.out.println("....");

		// 8b3c5694c78f06d706f77ba1c442329e73559dc444cccb6c927bc0dee9caf266
		// 88fc0f8b84e5960f5f50fd55c61742da68eefed7dff0f423a1e49eb53a332cc0
	}
}
