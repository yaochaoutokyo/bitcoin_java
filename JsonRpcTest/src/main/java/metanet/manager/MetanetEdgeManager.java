package metanet.manager;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import metanet.domain.MetanetNode;
import metanet.domain.MetanetNodeUTXO;
import metanet.utils.BsvTransactionBuilder;
import metanet.utils.HDHierarchyKeyGenerator;
import metanet.utils.HttpRequestSender;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import java.io.IOException;
import java.util.List;

/**
 * Created by yaochao on 2019/06/22
 */
public class MetanetEdgeManager {

	private NetworkParameters params;

	private DeterministicKey masterKey;

	private MetanetNodeManager nodeManager;

	private static final long INPUT_FEE_UNIT = 148;

	private static final long OUTPUT_FEE_UNIT = 34;

	private static final long EMPTY_META_OUTPUT_FEE_UNIT = 81;

	private static final long CHAR_FEE_UNIT = 2;

	private static final long SPACE_FEE_UNIT = 1;

	public MetanetEdgeManager(DeterministicKey masterKey, NetworkParameters params) {
		this.params = params;
		this.masterKey = masterKey;
		nodeManager = new MetanetNodeManager(params);
	}

	/**
	 * @description: Build Metanet-format Transaction from parent node to a single child node
	 * @param parentNode Parent Metanet node
	 * @param childNode Child Metanet node
	 * @param payloads Metanet-Data send to child node
	 * @param valueSendToChild value send to the address child node
	 * @date: 2019/06/23
	 **/
	public String buildEdgeFromCurrentNodeToChild(MetanetNode parentNode, MetanetNode childNode,
												List<String> payloads, long valueSendToChild) throws IOException {
		// get metanet info of parentNode
		// todo: let caller of this function get metanet info of parent node
		nodeManager.getMetanetNodeInfo(parentNode);
		BsvTransactionBuilder txBuilder = new BsvTransactionBuilder(params);
		List<MetanetNodeUTXO> parentNodeUtxoList = parentNode.getUtxoList();

		// estimate values of inputs and outputs
		Coin parentNodeBalance = Coin.SATOSHI.multiply(parentNode.getBalance());
		Coin outputValueToChild = Coin.SATOSHI.multiply(valueSendToChild);
		Coin transactionFee = estimateTransactionFee(parentNodeUtxoList.size(), 2, 1, payloads);
		Coin changeToParent = parentNodeBalance.subtract(outputValueToChild).subtract(transactionFee);

		// prepare necessary information for build a transaction
		DeterministicKey parentKey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(masterKey, parentNode.getPath());
		DeterministicKey childkey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(masterKey, childNode.getPath());
		String base64ChildPubKey = Base64.encode(childkey.getPubKey());
		String parentTxHash = parentNodeUtxoList.get(0).getTxid();
		Address parentAddress = parentKey.toAddress(params);
		Address childAddress = childkey.toAddress(params);

		// build a metanet-format transaction
		txBuilder.addMetanetOpReturnOutput(base64ChildPubKey, parentTxHash, payloads);
		// if valueSendToChild = 0, remove the P2PKH output to child address
		if (valueSendToChild != 0) {
			txBuilder.addP2PKHOutput(childAddress, outputValueToChild);
		}
		String txHex = txBuilder
				.addP2PKHOutput(parentAddress, changeToParent)
				.addSignedInputs(parentNodeUtxoList, parentKey)
				.buildRawTxHex();
		String txid = HttpRequestSender.broadcastRawTransaction(txHex);
		return txid;
	}

	/**
	 * @description: estimate transaction fee (1 input = 148 B, 1 P2PSK output = 34 B;
	 * 1 empty metanet output = 81; 1 char = 2 B; 1 space = 1 B)
	 * @param inputNum number of inputs
	 * @param outputNum number of outputs
	 * @param metaOutputNum number of empty meta-outputs
	 * @param payloads list of payload
	 * @date: 2019/06/23
	 **/
	private Coin estimateTransactionFee(long inputNum, long outputNum, long metaOutputNum, List<String> payloads) {
		long inputFee = inputNum * INPUT_FEE_UNIT;
		long outputFee = outputNum * OUTPUT_FEE_UNIT;
		long metaOutputFee = metaOutputNum * EMPTY_META_OUTPUT_FEE_UNIT;
		long payloadFee = payloads.size() * SPACE_FEE_UNIT;
		for (int i = 0; i < payloads.size(); i++) {
			String payload = payloads.get(i);
			payloadFee += CHAR_FEE_UNIT * payload.length();
		}
		long txFeeSum = inputFee + outputFee + metaOutputFee + payloadFee;
		Coin txFee = Coin.SATOSHI.multiply(txFeeSum);
		return txFee;
	}
}
