package metanet.manager;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import metanet.domain.MetanetNode;
import metanet.domain.MetanetNodeData;
import metanet.domain.MetanetNodeUTXO;
import metanet.utils.BsvTransactionBuilder;
import metanet.utils.HttpRequestSender;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import java.util.*;

/**
 * @description: class for build edge from parent node to child node
 * @author YAO Chao
 * @date: 2019/06/25
 **/
public class MetanetEdgeManager {

	private NetworkParameters params;

	/**
	 * minimum amount of output value
	 */
	private static final long DUST_VALUE_SATOSHI = 600L;

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
	private static final long BYTES_PER_EMPTY_META_OUTPUT = 124L;

	/**
	 * bytes per root meta output
	 */
	private static final long BYTES_ROOT_META_OUTPUT = 59L;

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


	public MetanetEdgeManager(NetworkParameters params) {
		this.params = params;
	}

	/**
	 * @description: Set the fee for per byte
	 * @param feePerByte self-defined fee/byte
	 * @date: 2019/06/23
	 **/
	public void setFeePerByte(long feePerByte) {
		this.feePerByte = feePerByte;
	}

	/**
	 * @description: build Edge from parent node to child node without any value transforming
	 * @param parentNode parent node
	 * @param childNode child node
	 * @param payloads data store in child node
	 * @return Txid
	 * @date: 2019/06/23
	 **/
	public String buildEdgeToMetanetNodeWithoutValue(MetanetNode parentNode, MetanetNode childNode
			, List<String> payloads) {
		return buildEdgeFromCurrentNodeToChild(parentNode, childNode, payloads, 0, false);
	}

	/**
	 * @description: build Edge from parent node to child node with certain amount of value transforming
	 * @param parentNode parent node
	 * @param childNode child node
	 * @param payloads data store in child node
	 * @param valueSendToChild value send to child node
	 * @return Txid
	 * @date: 2019/06/23
	 **/
	public String buildEdgeToMetanetNodeWithValue(MetanetNode parentNode, MetanetNode childNode
			, List<String> payloads, long valueSendToChild) {
		return buildEdgeFromCurrentNodeToChild(parentNode, childNode, payloads, valueSendToChild, false);
	}

	/**
	 * @description: create a root node from a pseudo-parent node without any value transforming
	 * @param pseudoParentNode Imaginary node which send a matenet-root format transaction to root node
	 * @param rootNode root node
	 * @return Txid
	 * @date: 2019/06/23
	 **/
	public String buildMetanetRootNodeWithoutValue(MetanetNode pseudoParentNode, MetanetNode rootNode) {
		return buildEdgeFromCurrentNodeToChild(pseudoParentNode, rootNode, null, 0, true);
	}

	/**
	 * @description: create a root node from a pseudo-parent node with certain amount of value transforming
	 * @param pseudoParentNode Imaginary node which send a matenet-root format transaction to root node
	 * @param rootNode root node
	 * @param valueSendToRoot value send to root node
	 * @return Txid
	 * @date: 2019/06/23
	 **/
	public String buildMetanetRootNodeWithValue(MetanetNode pseudoParentNode, MetanetNode rootNode
			, long valueSendToRoot) {
		return buildEdgeFromCurrentNodeToChild(pseudoParentNode, rootNode, null, valueSendToRoot, true);
	}

	public String sendMoneyFromNodeAToNodeB(MetanetNode nodeA, MetanetNode nodeB, long value) {
		return buildEdgeFromCurrentNodeToChild(nodeA, nodeB, null, value, false);
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
	private String buildEdgeFromCurrentNodeToChild(MetanetNode parentNode, MetanetNode childNode, List<String> payloads
			, long valueSendToChild, boolean isRoot) {

		if (valueSendToChild != 0L && valueSendToChild <= DUST_VALUE_SATOSHI) {
			System.out.println("Dust output");
		}
		// prepare necessary information for build a transaction
		DeterministicKey parentKey = parentNode.getKey();
		List<MetanetNodeUTXO> parentNodeUtxoList = parentNode.getUtxoList();
		List<MetanetNodeData> parentNodeDataList = parentNode.getDataList();
		// the newest data in the data list is the data tx of parent node
		// for non-metanet node, the parentNodeDataList is null, when create root node from a non-metanet node
		// it should be careful for this feature
		Address parentAddress = parentKey.toAddress(params);
		DeterministicKey childKey = childNode.getKey();
		String base64ChildPubKey = Base64.encode(childKey.getPubKey());
		Address childAddress = childKey.toAddress(params);

		// add metanet-format OP_RETURN output
		BsvTransactionBuilder txBuilder = new BsvTransactionBuilder(params);
		if (isRoot) {
			txBuilder.addMetanetRootNodeOutput(base64ChildPubKey);
		} else if (payloads != null) {
			String currentParentNodeDataHash = parentNode.getCurrentVersion();
			txBuilder.addMetanetChildNodeOutput(base64ChildPubKey, currentParentNodeDataHash, payloads);
		}

		// add P2PKH output to child node
		Coin outputValueToChild = Coin.SATOSHI.multiply(valueSendToChild);
		if (valueSendToChild != 0L) {
			txBuilder.addP2PKHOutput(childAddress, outputValueToChild);
		}

		// estimate txFee
		long outputNum = valueSendToChild == 0 ? 1 : 2;
		long inputNum = parentNodeUtxoList.size();
		long metaOutputNum = payloads != null ? 1 : 0;
		long rootMetaOutputNum = isRoot ? 1 : 0;
		Coin transactionFee = estimateTransactionFee(inputNum, outputNum, metaOutputNum, rootMetaOutputNum, payloads);

		// calculate change value
		Coin parentNodeBalance = Coin.SATOSHI.multiply(parentNode.getBalance());
		Coin changeToParent = parentNodeBalance.subtract(outputValueToChild).subtract(transactionFee);
		Coin dustOutput = Coin.SATOSHI.multiply(DUST_VALUE_SATOSHI);
		if (changeToParent.isLessThan(dustOutput)) {
			Coin lackValue = outputValueToChild.add(transactionFee).add(dustOutput).subtract(parentNodeBalance);
			System.out.println(String.format( "Balance is insufficient, lack of %d satoshi(s)", lackValue.value));
		}

		// add change, inputs into Tx, and sign it
		String txHex = txBuilder
				.addP2PKHOutput(parentAddress, changeToParent)
				.addSignedInputs(parentNodeUtxoList, parentKey)
				.buildRawTxHex();

		// decode the raw transaction
		HttpRequestSender.decodeRawTransaction(txHex);
		// broadcast the raw transaction
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
		Coin txFee = Coin.SATOSHI.multiply(txFeeSum * feePerByte);
		return txFee;
	}
}
