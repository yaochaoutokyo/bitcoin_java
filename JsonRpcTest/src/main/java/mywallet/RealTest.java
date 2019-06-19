package mywallet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import mywallet.domain.UtxoResponse;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bitcoinj.core.Utils.HEX;

/**
 * Created by yaochao on 2019/06/19
 */
public class RealTest {

	private static BitcoinJSONRPCClient jsonRpcClient;
	private static NetworkParameters params;
	private static OkHttpClient okHttpClient;
	private static Gson gson;

	static {
		params = MainNetParams.get();
		okHttpClient = new OkHttpClient();
		gson = new Gson();
		try {
			jsonRpcClient = new BitcoinJSONRPCClient("http://admin:huobijp@52.199.36.243:8332");
		}catch(Exception e){
			System.out.format("static init error");
		}
	}

	public static List<String> genMnemonicCode() throws MnemonicException.MnemonicLengthException, IOException {
		// 生成安全随机熵源(128 bit)
		SecureRandom random = new SecureRandom();
		byte[] entropy = new byte[16];
		random.nextBytes(entropy);

		// 熵转换为助记词
		// 熵（128位） + 校验和（熵的sha256，取前4位）= 12个助记词（132/11 = 12）
		MnemonicCode mnemonicCode = new MnemonicCode();
		List<String> mnemonics = mnemonicCode.toMnemonic(entropy);
		return mnemonics;
	}

	public static DeterministicKey restoreMasterKeyFromMnemonicCode(List<String> mnemonics, String passphrase) {
		// 助记词转换为种子
		// 助记词 + 种子（"mnemonic" + 可选的passphrase）--2048 * HMAC-SHA512--> seed
		byte[] seed = MnemonicCode.toSeed(mnemonics,passphrase);

		// 生成主密钥
		// seed --HMAC-SHA512--> master privkey(256bit) + master chaincode(256bit)
		DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed);
		return masterKey;
	}

	public static List<UTXO> getUtxoByAddress(Address address) throws IOException {
		// 调用bitindex api获取地址的UTXO集
		String url = String.format("https://api.bitindex.network/api/v3/main/addr/%s/utxo", address);
		Request request = new Request.Builder().url(url).build();
		Response response = okHttpClient.newCall(request).execute();
		List<UtxoResponse> utxoResponses = new ArrayList<>();
		if (response.isSuccessful()) {
			String json = response.body().string();
			utxoResponses = gson.fromJson(json,new TypeToken<List<UtxoResponse>>(){}.getType());
		}
		// 将响应存入UTXO
		List<UTXO> utxos = new ArrayList<>();
		for (UtxoResponse utxoResp : utxoResponses) {
			UTXO utxo = new UTXO(Sha256Hash.wrap(utxoResp.getTxid()), utxoResp.getVout(),
					Coin.parseCoin(utxoResp.getAmount()), utxoResp.getHeight(),false,
					new Script(HEX.decode(utxoResp.getScriptPubKey())), address.toBase58());
			utxos.add(utxo);
		}
		return utxos;
	}

	public static String buildRawOpreturnTx(DeterministicKey masterKey, String data, List<UTXO> utxos) {

		Address myAddress = masterKey.toAddress(params);
		Transaction tx = new Transaction(params);

		// 选择UTXO作为Input
		UTXO inputUtxo = utxos.get(0);

		// 根据数据长度计算交易费
		Integer dataLength = data.getBytes().length;
		Coin txFee = Coin.SATOSHI.multiply(210 + dataLength);

		// OP_RETURN 携带数据
		Script opReturnScript = ScriptBuilder.createOpReturnScript(data.getBytes());
		tx.addOutput(Coin.ZERO, opReturnScript);
		// 找零的输出，找零会原来的地址
		Coin changeCoin = inputUtxo.getValue().minus(txFee);
		tx.addOutput(changeCoin, myAddress);

		// 先将input加入交易中
		TransactionInput input = tx.addInput(inputUtxo.getHash(), inputUtxo.getIndex(), new Script(new byte[]{}));
		// 签名交易，使用Hash类型[ALL | FORK_ID]
		Sha256Hash hash = tx.hashForSignatureWitness(0, inputUtxo.getScript(),
				inputUtxo.getValue(), Transaction.SigHash.ALL, false);
		ECKey.ECDSASignature ecSig = masterKey.sign(hash);
		TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false, true);

		// 叫签名放入输入中
		input.setScriptSig(ScriptBuilder.createInputScript(txSig, masterKey));

		String txHex = HEX.encode(tx.bitcoinSerialize());
		return txHex;
	}

	public static void decode(String txHex) {
		// 使用节点旳decoderawtransaction调用来检查一下裸交易的内容是否与我们的预期一致
		BitcoinJSONRPCClient.RawTransaction rawTx = jsonRpcClient.decodeRawTransaction(txHex);
		System.out.format("raw tx => %s\n",rawTx);
	}

	public static void broadcast(String txHex) {
		// 广播交易，将交易送给节点处理,如果交易不合法会被拒绝
		String txid = jsonRpcClient.sendRawTransaction(txHex);
		System.out.format("txid => %s\n", txid);
	}

	public static void main(String[] args) throws Exception {

		List<String> mnemonics = Arrays.asList(new String[]{"forum", "rug", "slice", "snack", "width", "inside",
				"mad", "cotton", "noodle", "april", "dumb", "adapt"});
		String passphrase = "123456";
		DeterministicKey masterKey = RealTest.restoreMasterKeyFromMnemonicCode(mnemonics,passphrase);

		Address address = masterKey.toAddress(params);
		List<UTXO> utxos = getUtxoByAddress(address);
		String txHex = buildRawOpreturnTx(masterKey, "YAO Chao Loves WANG Shuci", utxos);
		decode(txHex);
		broadcast(txHex);
	}
}
