package fake_user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import domain.ChannelBidInfo;
import domain.UserInfo;
import domain.UserUTXO;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import utils.HDHierarchyKeyGenerator;
import utils.HttpRequestSender;
import utils.TransactionBuilder;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yaochao on 2019/07/19
 */
public class FakeUser {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

	private final static String USER_CENTER_ADDRESS = "1fiDhEkRR8MHq1AsUUKdsFDoy2LqHZU23";

	public static void register(String userAddress, long valueToUserCenter, String userName, ECKey userKey) {
		NetworkParameters params = MainNetParams.get();
		TransactionBuilder builder = new TransactionBuilder(params);
		List<String> payloads = new ArrayList<>();
		List<UserUTXO> userUTXOS = HttpRequestSender.getUtxoFromWhatOnChain(userAddress);
		payloads.add(USER_CENTER_ADDRESS);
		payloads.add(userAddress);
		payloads.add(userName);
		String txHex = builder.addOpReturnOutput(payloads)
				.addP2PKHOutput(USER_CENTER_ADDRESS, valueToUserCenter)
				.addP2PKHOutput(userAddress, valueToUserCenter)
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

	public static void bidForChannel(String userAddress, String childAddress, long paymentAmount
            , String channelAddress, ChannelBidInfo channelBidInfo, ECKey userKey) {
        NetworkParameters params = MainNetParams.get();
        TransactionBuilder builder = new TransactionBuilder(params);
        List<String> payloads = new ArrayList<>();
        List<UserUTXO> userUTXOS = HttpRequestSender.getUtxoFromWhatOnChain(userAddress);
        payloads.add("0");
        payloads.add(gson.toJson(channelBidInfo));
        String txHex = builder.addMetanetChildNodeOutput(childAddress, "65741127aa2cca061a1be2daf724041b7f3646a6be4e165f1b3991418ac355e9", payloads)
                .addP2PKHOutput(channelAddress, paymentAmount)
                .addP2PKHOutput(userAddress, paymentAmount)
                .addSignedInputs(userUTXOS, userKey)
                .buildRawTxHex();
        long totalInput = builder.getTotalInput();
        BitcoinJSONRPCClient.RawTransaction rawTx = HttpRequestSender.decodeRawTransaction(txHex);
        long txSize = rawTx.size();
        TransactionBuilder newBuilder = new TransactionBuilder(params);
        long change = totalInput - (txSize + 2) - paymentAmount;
        txHex = newBuilder.addMetanetChildNodeOutput(childAddress, "65741127aa2cca061a1be2daf724041b7f3646a6be4e165f1b3991418ac355e9", payloads)
                .addP2PKHOutput(channelAddress, paymentAmount)
                .addP2PKHOutput(userAddress, change)
                .addSignedInputs(userUTXOS, userKey)
                .buildRawTxHex();
        HttpRequestSender.broadcastRawTransaction(txHex);
    }

	public static void modifyUserInfo(String userAddress, String childAddress, long valueToUserCenter, UserInfo userInfo, ECKey userKey) {
        NetworkParameters params = MainNetParams.get();
        TransactionBuilder builder = new TransactionBuilder(params);
        List<String> payloads = new ArrayList<>();
        List<UserUTXO> userUTXOS = HttpRequestSender.getUtxoFromWhatOnChain(userAddress);
        payloads.add("1");
        payloads.add(gson.toJson(userInfo));
        String txHex = builder.addMetanetChildNodeOutput(childAddress, "65741127aa2cca061a1be2daf724041b7f3646a6be4e165f1b3991418ac355e9", payloads)
                .addP2PKHOutput(USER_CENTER_ADDRESS, valueToUserCenter)
                .addP2PKHOutput(userAddress, valueToUserCenter)
                .addSignedInputs(userUTXOS, userKey)
                .buildRawTxHex();
        long totalInput = builder.getTotalInput();
        BitcoinJSONRPCClient.RawTransaction rawTx = HttpRequestSender.decodeRawTransaction(txHex);
        long txSize = rawTx.size();
        TransactionBuilder newBuilder = new TransactionBuilder(params);
        long change = totalInput - (txSize + 2) - valueToUserCenter;
        txHex = newBuilder.addMetanetChildNodeOutput(childAddress, "65741127aa2cca061a1be2daf724041b7f3646a6be4e165f1b3991418ac355e9", payloads)
                .addP2PKHOutput(USER_CENTER_ADDRESS, valueToUserCenter)
                .addP2PKHOutput(userAddress, change)
                .addSignedInputs(userUTXOS, userKey)
                .buildRawTxHex();
        HttpRequestSender.broadcastRawTransaction(txHex);
    }

	public static void main(String[] args) throws Exception {
		NetworkParameters params = MainNetParams.get();
		List<String> mnemonics = Arrays.asList(new String[]{"various", "proud", "hover", "misery", "normal", "once", "melt", "woman", "vague", "crew", "umbrella", "forward"});
		String passphrase = "yaochao";
		DeterministicKey masterKey = HDHierarchyKeyGenerator.restoreMasterKeyFromMnemonicCode(mnemonics, passphrase);
		DeterministicKey userKey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(masterKey, "M/88H/88H/88H/88H");
		// 13Dm7Mb74hRMqZmcYoHxwWA2ZMPCiWG7Sv
		String userAddress = userKey.toAddress(params).toBase58();
		//register(userAddress, 3300L, "fake user 2", userKey);

//        UserInfo userInfo = new UserInfo();
//        userInfo.setName("edited fake user 2");
//        userInfo.setProfile("I am a fake user");
//        DeterministicKey modifyUserInfoKey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(masterKey, "M/88H/88H/88H/88H/0");
//        String modifyUserInfoAddress = modifyUserInfoKey.toAddress(params).toBase58();
//        modifyUserInfo(userAddress, modifyUserInfoAddress, 1200, userInfo, userKey);

        DeterministicKey channelBidKey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(masterKey, "M/88H/88H/88H/88H/2");
        String channelBidAddress = channelBidKey.toAddress(params).toBase58();
        String channelAddress = "1BFPF73S7bQWw6WbDYuuPqn1sTR5oFBVPr";
        ChannelBidInfo channelBidInfo = new ChannelBidInfo();
        channelBidInfo.setPlatform("huobi");
        channelBidInfo.setChannelName("bchusdt");
        channelBidInfo.setStartTime(simpleDateFormat.parse("2019-07-21 19:30:00").getTime());
        channelBidInfo.setEndTime(simpleDateFormat.parse("2019-07-21 21:30:00").getTime());
        channelBidInfo.setPaymentAmount(12000L);
        bidForChannel(userAddress, channelBidAddress, 18000, channelAddress, channelBidInfo, userKey);
    }
}
