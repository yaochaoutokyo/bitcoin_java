package fake_user;

import metanet.utils.HDHierarchyKeyGenerator;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yaochao on 2019/07/19
 */
public class FakeUser {

	private final static String USER_CENTER_ADDRESS = "1fiDhEkRR8MHq1AsUUKdsFDoy2LqHZU23";

	public static void register(String userAddress, long valueToUserCenter, String userName, ECKey userKey) {
		NetworkParameters params = MainNetParams.get();
		TransactionBuilder builder = new TransactionBuilder(params);
		List<String> payloads = new ArrayList<>();
		List<UserUTXO> userUTXOS = HttpRequestSender.getUtxoByAddress(userAddress);
		payloads.add(USER_CENTER_ADDRESS);
		payloads.add(userAddress);
		payloads.add(userName);
		String txHex = builder.addOpReturnOutput(payloads)
				.addP2PKHOutput(USER_CENTER_ADDRESS, valueToUserCenter)
				.addP2PKHOutput(userAddress, 4000L)
				.addSignedInputs(userUTXOS, userKey)
				.buildRawTxHex();
		long totalInput = builder.getTotalInput();
		BitcoinJSONRPCClient.RawTransaction rawTx = HttpRequestSender.decodeRawTransaction(txHex);
		long txSize = rawTx.size();
		TransactionBuilder newBuilder = new TransactionBuilder(params);
		long change = totalInput - (txSize + 2) - valueToUserCenter;
		txHex = newBuilder.addOpReturnOutput(payloads)
				.addP2PKHOutput(USER_CENTER_ADDRESS, valueToUserCenter)
				.addP2PKHOutput(userAddress, change)
				.addSignedInputs(userUTXOS, userKey)
				.buildRawTxHex();
		HttpRequestSender.broadcastRawTransaction(txHex);
	}

	public static void main(String[] args) {
		NetworkParameters params = MainNetParams.get();
		List<String> mnemonics = Arrays.asList(new String[]{"various", "proud", "hover", "misery", "normal", "once", "melt", "woman", "vague", "crew", "umbrella", "forward"});
		String passphrase = "yaochao";
		DeterministicKey masterKey = HDHierarchyKeyGenerator.restoreMasterKeyFromMnemonicCode(mnemonics, passphrase);
		DeterministicKey userKey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(masterKey, "M/88H/88H/88H/88H");
		// 13Dm7Mb74hRMqZmcYoHxwWA2ZMPCiWG7Sv
		String userAddress = userKey.toAddress(params).toBase58();
		register(userAddress, 3000L, "fake user 2", userKey);
	}
}
