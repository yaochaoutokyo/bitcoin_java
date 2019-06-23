package metanet.manager;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import metanet.RealTest;
import metanet.domain.MetanetNode;
import metanet.domain.MetanetNodeUTXO;
import metanet.utils.BsvTransactionBuilder;
import metanet.utils.HDHierarchyKeyGenerator;
import metanet.utils.HttpRequestSender;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRPCException;

import java.io.IOException;
import java.util.*;

/**
 * Created by yaochao on 2019/06/22
 */
public class MetanetEdgeManager {

	private NetworkParameters params;

	private DeterministicKey masterKey;

	private static final long BYTES_PER_INPUT = 148L;

	private static final long BYTES_PER_OUTPUT = 34L;

	private static final long BYTES_EMPTY_META_OUTPUT = 81L;

	private static final long BYTES_PER_CHAR = 1L;

	private static final long BYTES_PER_SPACE = 1L;

	private static long DEFALUT_FEE_PER_BYTE = 1L;

	private static final long INITIAL_LENGTH_BYTES = 10L;

	public MetanetEdgeManager(DeterministicKey masterKey, NetworkParameters params) {
		this.params = params;
		this.masterKey = masterKey;
	}

	public static void setDefalutFeePerByte(long feePerByte) {
		DEFALUT_FEE_PER_BYTE = feePerByte;
	}

	/**
	 * @description: Build Metanet-format Transaction from parent node to a single child node
	 * can't build transaction with more than 1 OP_RETURN output, the JSON RPC will give
	 * response: {"result":null,"error":{"code":-26,"message":"64: multi-op-return"},"id":"1"}
	 * @param parentNode Parent Metanet node
	 * @param childNode Child Metanet node
	 * @param payloads Metanet-Data send to child node
	 * @param valueSendToChild value send to the address child node
	 * @date: 2019/06/23
	 **/
	public String buildEdgeFromCurrentNodeToChild(MetanetNode parentNode, MetanetNode childNode, List<String> payloads, long valueSendToChild)
			throws InsufficientMoneyException {

		// estimate values of inputs and outputs
		List<MetanetNodeUTXO> parentNodeUtxoList = parentNode.getUtxoList();
		Coin parentNodeBalance = Coin.SATOSHI.multiply(parentNode.getBalance());
		Coin outputValueToChild = Coin.SATOSHI.multiply(valueSendToChild);
		long outputNum = valueSendToChild == 0 ? 1 : 2;
		long inputNum = parentNodeUtxoList.size();
		long metaOutput = payloads == null || payloads.isEmpty() ? 0 : 1;
		Coin transactionFee = estimateTransactionFee(inputNum, outputNum, metaOutput, payloads);
		Coin changeToParent = parentNodeBalance.subtract(outputValueToChild).subtract(transactionFee);
		if (changeToParent.isLessThan(Coin.ZERO)) {
			throw new InsufficientMoneyException(outputValueToChild.plus(transactionFee).subtract(parentNodeBalance));
		}

		// prepare necessary information for build a transaction
		DeterministicKey parentKey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(masterKey, parentNode.getPath());
		DeterministicKey childkey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(masterKey, childNode.getPath());
		childNode.setPubKey(Base64.encode(childkey.getPubKey()));
		String base64ChildPubKey = Base64.encode(childkey.getPubKey());
		String parentTxHash = parentNodeUtxoList.get(0).getTxid();
		Address parentAddress = parentKey.toAddress(params);
		Address childAddress = childkey.toAddress(params);

		// build a metanet-format transaction
		BsvTransactionBuilder txBuilder = new BsvTransactionBuilder(params);
		if (payloads != null && ! payloads.isEmpty()) {
			txBuilder.addMetanetOpReturnOutput(base64ChildPubKey, parentTxHash, payloads);
		}
		if (valueSendToChild != 0L) {
			txBuilder.addP2PKHOutput(childAddress, outputValueToChild);
		}
		String txHex = txBuilder
				.addP2PKHOutput(parentAddress, changeToParent)
				.addSignedInputs(parentNodeUtxoList, parentKey)
				.buildRawTxHex();

		HttpRequestSender.decodeRawTransaction(txHex);
		String txid = null;
		try {
			txid = HttpRequestSender.broadcastRawTransaction(txHex);
		} catch (BitcoinRPCException e) {
			System.out.println("transaction fee is insufficient!");
		}
		return txid;
	}

	/**
	 * @description: batch build Metanet-output
	 * @date: 2019/06/23
	 **/
	@Deprecated
	public String buildEdgeFromCurrentNodeToChildren(MetanetNode parentNode, List<MetanetNode> childNodeList
			, Map<MetanetNode, List<String>> payloadMap, Map<MetanetNode, Long> valueSendToChildMap)
		 	throws IOException, InsufficientMoneyException {

		List<MetanetNodeUTXO> parentNodeUtxoList = parentNode.getUtxoList();
		Coin parentNodeBalance = Coin.SATOSHI.multiply(parentNode.getBalance());
		DeterministicKey parentKey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(masterKey, parentNode.getPath());
		String parentTxHash = parentNodeUtxoList.get(0).getTxid();
		Address parentAddress = parentKey.toAddress(params);

		// estimate values of inputs and outputs
		long inputNum = parentNodeUtxoList.size();
		long outputNum = 1;
		long metaOutput = 0;
		List<String> payloads = new ArrayList<>();
		Coin totalOutputValue = Coin.ZERO;
		BsvTransactionBuilder txBuilder = new BsvTransactionBuilder(params);
		for (MetanetNode childNode : childNodeList) {
			DeterministicKey childkey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(masterKey, childNode.getPath());
			Address childAddress = childkey.toAddress(params);
			String base64ChildPubKey = Base64.encode(childkey.getPubKey());
			if (payloadMap.containsKey(childNode)) {
				List<String> childNodePayload = payloadMap.get(childNode);
				txBuilder.addMetanetOpReturnOutput(base64ChildPubKey, parentTxHash, childNodePayload);
				payloads.addAll(childNodePayload);
				metaOutput++;
			}
			if (! valueSendToChildMap.getOrDefault(childNode, 0L).equals(0L)) {
				Long valueSendToChild = valueSendToChildMap.get(childNode);
				Coin outputValueToChild = Coin.SATOSHI.multiply(valueSendToChild);
				totalOutputValue = totalOutputValue.add(outputValueToChild);
				txBuilder.addP2PKHOutput(childAddress, outputValueToChild);
				outputNum++;
			}
		}

		Coin transactionFee = estimateTransactionFee(inputNum, outputNum, metaOutput, payloads);
		Coin changeToParent = parentNodeBalance.subtract(totalOutputValue).subtract(transactionFee);
		if (changeToParent.isLessThan(Coin.ZERO)) {
			throw new InsufficientMoneyException(totalOutputValue.plus(transactionFee).subtract(parentNodeBalance));
		}

		String txHex = txBuilder
				.addP2PKHOutput(parentAddress, changeToParent)
				.addSignedInputs(parentNodeUtxoList, parentKey)
				.buildRawTxHex();
		HttpRequestSender.decodeRawTransaction(txHex);
		String txid = HttpRequestSender.broadcastRawTransaction(txHex);
		return txid;
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
	private Coin estimateTransactionFee(long inputNum, long outputNum, long metaOutputNum, List<String> payloads) {
		long inputFee = inputNum * BYTES_PER_INPUT;
		long outputFee = outputNum * BYTES_PER_OUTPUT;
		long metaOutputFee = metaOutputNum * BYTES_EMPTY_META_OUTPUT;
		long payloadFee = payloads.size() * BYTES_PER_SPACE;
		for (int i = 0; i < payloads.size(); i++) {
			String payload = payloads.get(i);
			payloadFee += BYTES_PER_CHAR * payload.length();
		}
		// 10 bytes is the initial length of tx
		long txFeeSum = INITIAL_LENGTH_BYTES + inputFee + outputFee + metaOutputFee + payloadFee;
		Coin txFee = Coin.SATOSHI.multiply(txFeeSum * DEFALUT_FEE_PER_BYTE);
		return txFee;
	}

	public static void main(String[] args) throws IOException, InsufficientMoneyException {
		NetworkParameters params = MainNetParams.get();
		List<String> mnemonics = Arrays.asList(new String[]{"forum", "rug", "slice", "snack", "width", "inside",
				"mad", "cotton", "noodle", "april", "dumb", "adapt"});
		String passphrase = "123456";
		DeterministicKey masterKey = RealTest.restoreMasterKeyFromMnemonicCode(mnemonics,passphrase);
		DeterministicKey parentKey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(masterKey, "M/1/0");
		MetanetNode parentNode = new MetanetNode(Base64.encode(parentKey.getPubKey()),"M/1/0", null);
		MetanetNodeManager metanetNodeManager = new MetanetNodeManager(params);
		metanetNodeManager.getMetanetTree(parentNode);
//		MetanetNode childNode1 = new MetanetNode("A5TdWv85gj0rX/KteTu2aJi04QtbSu6zNtfbZlJhfGZc","M/1/0/0", parentNode);
//		metanetNodeManager.getMetanetNodeInfo(childNode1);

		MetanetNode childNode2 = new MetanetNode("","M/1/0/1", parentNode);
		MetanetEdgeManager metanetEdgeManager = new MetanetEdgeManager(masterKey, params);
		List<String> payloads = Arrays.asList(new String[]{"M/1/0/1", "unable", "to", "batch"});
		metanetEdgeManager.buildEdgeFromCurrentNodeToChild(parentNode, childNode2, payloads, 0);
//		MetanetNode childNode3 = new MetanetNode("", "M/1/0/2", parentNode);
//		MetanetNode childNode4 = new MetanetNode("", "M/1/0/3", parentNode);

	}
}
