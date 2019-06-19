import org.bitcoinj.core.*;
import org.bitcoinj.crypto.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import static org.bitcoinj.core.Utils.HEX;

/**
 * Created by yaochao on 2019/06/19
 */
public class RealTest {

	public static List<String> genMnemonicCode() throws MnemonicException.MnemonicLengthException, IOException {
		// 生成安全随机熵源(128 bit)
		SecureRandom random = new SecureRandom();
		byte[] entropy = new byte[16];
		random.nextBytes(entropy);
		System.out.format("entropy => %s\n", HEX.encode(entropy));

		// 熵转换为助记词
		// 熵（128位） + 校验和（熵的sha256，取前4位）= 12个助记词（132/11 = 12）
		MnemonicCode mnemonicCode = new MnemonicCode();
		List<String> mnemonics = mnemonicCode.toMnemonic(entropy);
		System.out.format("mnemonics => %s\n", mnemonics);
		return mnemonics;
	}

	public static DeterministicKey restoreMasterKeyFromMnemonicCode(List<String> mnemonics, String passphrase) {
		// 助记词转换为种子
		// 助记词 + 种子（"mnemonic" + 可选的passphrase）--2048 * HMAC-SHA512--> seed
		byte[] seed = MnemonicCode.toSeed(mnemonics,passphrase);
		System.out.format("seed => %s\n", HEX.encode(seed));

		// 生成主密钥
		// seed --HMAC-SHA512--> master privkey(256bit) + master chaincode(256bit)
		DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed);
		System.out.format("master key => %s\n", masterKey.getPrivateKeyAsHex());
		return masterKey;
	}

	public static void main(String[] args) throws Exception {
//		List<String> mnemonics = RealTest.genMnemonicCode();
//		System.out.println(mnemonics);
		List<String> mnemonics = Arrays.asList(new String[]{"forum", "rug", "slice", "snack", "width", "inside",
				"mad", "cotton", "noodle", "april", "dumb", "adapt"});
		String passphrase = "123456";
		// 2be021ffd018b7c6ad9857c4ef45f04f495ee5b0bd8a093f05bb7ac17a8f328f
		DeterministicKey masterKey = RealTest.restoreMasterKeyFromMnemonicCode(mnemonics,passphrase);
		NetworkParameters params = MainNetParams.get();
		// 13FpbqJoYY3cCPfUomE1oxctXDWJQPoEvJ
		System.out.println(masterKey.toAddress(params));

		Address myAddress = masterKey.toAddress(params);
		Transaction tx = new Transaction(params);

		Coin changeCoin = Coin.parseCoin("0.00044500");

		// OP_RETURN
		Script opReturnScript = ScriptBuilder.createOpReturnScript("Hello World".getBytes());
		System.out.println(opReturnScript);
		tx.addOutput(Coin.ZERO, opReturnScript);
		// change Output
		TransactionOutput txChangeOutput = tx.addOutput(changeCoin, myAddress);

		TransactionOutPoint utxo = new TransactionOutPoint(params,0,
				Sha256Hash.wrap("05f65e1c32e4a20c621f2d8bc412edb181019ff079e25725385cc48f5bde6a94"));
		// 5e436fedb0b15aaabbc96479efcdf76ddbdf5d4d58ac8ffa56dd6920be71c4b9
		Script utxoLockingScript = ScriptBuilder.createOutputScript(masterKey.toAddress(params));
		System.out.println(utxoLockingScript);
		tx.addSignedInput(utxo, utxoLockingScript, masterKey, Transaction.SigHash.ALL, true);

		String txHex = HEX.encode(tx.bitcoinSerialize());
		System.out.println(txHex);

		// 使用节点旳decoderawtransaction调用来检查一下裸交易的内容是否与我们的预期一致
		BitcoinJSONRPCClient client = new BitcoinJSONRPCClient("http://admin:huobijp@52.199.36.243:8332");
		BitcoinJSONRPCClient.RawTransaction rawTx = client.decodeRawTransaction(txHex);
		System.out.format("raw tx => %s\n",rawTx);

		// 广播交易，将交易送给节点处理,如果交易不合法会被拒绝
		String txid = client.sendRawTransaction(txHex);
		System.out.format("txid => %s\n", txid);
	}
}
