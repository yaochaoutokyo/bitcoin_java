package metanet;

import com.google.common.base.Joiner;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by yaochao on 2019/06/16
 */
public class WalletController {

	public static Wallet createWallet(NetworkParameters params) {
		Wallet wallet = new Wallet(params);
		Address a = wallet.currentReceiveAddress();
		ECKey b = wallet.currentReceiveKey();
		System.out.println(a);
		System.out.println(b);
		return wallet;
	}

	public static String getMenmoics(Wallet wallet) {
		DeterministicSeed seed = wallet.getKeyChainSeed();
		String menmoics = Joiner.on(" ").join(seed.getMnemonicCode());
		System.out.println("Seed words are: " + Joiner.on(" ").join(seed.getMnemonicCode()));
		System.out.println("Seed birthday is: " + seed.getCreationTimeSeconds());
		return menmoics;
	}

	public static Wallet restoreWallet(String menmoics, String passphrase, Long creationtime,
									   NetworkParameters params) throws UnreadableWalletException {

		DeterministicSeed seed = new DeterministicSeed(menmoics, null, passphrase, creationtime);
		Wallet wallet = Wallet.fromSeed(params, seed);
		System.out.println(wallet.toString());
		return wallet;
	}

	public static byte[] buildRawOpreturnTx(String data, Wallet wallet) {

		NetworkParameters params = wallet.getNetworkParameters();
		Address changeAddress = wallet.currentReceiveAddress();
		ECKey ecKey = wallet.currentReceiveKey();

		Transaction tx = new Transaction(params);
		Script script = new ScriptBuilder().op(ScriptOpCodes.OP_RETURN).data("hello".getBytes()).build();
		tx.addOutput(Coin.ZERO, script);
		// 修改这里的交易费配置，输入减去找零的差值就是交易费
		tx.addOutput(Coin.SATOSHI.multiply(10000), changeAddress);

		// 修改这里的input index和交易hash
		TransactionOutPoint outPoint = new TransactionOutPoint(params, 0L, Sha256Hash.wrap(Hex.decode("txHashOfUTXO")));
		// 修改这里的input的解锁脚本
		Script utxoScript = new Script(Hex.decode("utxoScript"));
		tx.addSignedInput(outPoint, utxoScript, ecKey, Transaction.SigHash.FORKID, true);
		return Hex.encode(tx.bitcoinSerialize());
	}
}
