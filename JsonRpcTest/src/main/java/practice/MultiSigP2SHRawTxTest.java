package practice;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.bitcoinj.core.Utils.HEX;

/**
 * Created by yaochao on 2019/06/18
 */
public class MultiSigP2SHRawTxTest {
	public static void main(String[] args) throws Exception {

		// 2 - 2 多重签名地址
		ECKey senderKey1 = new ECKey();
		ECKey senderKey2 = new ECKey();
		ECKey receiverKey = new ECKey();

		// 生成见证脚本和锁定脚本
		Script redeemScript = ScriptBuilder.createRedeemScript(2, Arrays.asList(senderKey1, senderKey2));
		System.out.format("P2SH redeem script => %s\n", redeemScript);
		Script outputScript = ScriptBuilder.createP2SHOutputScript(redeemScript);
		System.out.format("P2SH output script => %s\n", outputScript);

		// 生成P2SH地址
		NetworkParameters params = MainNetParams.get();
		Address p2shAddress = outputScript.getToAddress(params);
		System.out.format("sender address => %s\n", p2shAddress);

		// 假设以上地址收到了一笔转账
		String utxoTxid = "78a37b87b03101a5ff28266f01365e61122bca00c6910f8d9c5c0f450cd05c62";
		int utxoVout = 0;
		double utxoValue = 10.0;

		// 新建交易
		Transaction tx = new Transaction(params);
		Coin amout = Coin.parseCoin("9.9999");
		Address receiverAddress = receiverKey.toAddress(params);

		// 加入output,由于input=10.0，output=9.9999，无需找零
		tx.addOutput(amout, receiverAddress);

		// 新建input
		TransactionInput txi = tx.addInput(Sha256Hash.wrap(utxoTxid), utxoVout, new Script(new byte[]{}));

		// 创建签名(Stream 形式操作)
		List<TransactionSignature> txSigs = Arrays.asList(senderKey1, senderKey2)
				.stream()
				.map(key -> tx.calculateSignature(0, key, redeemScript, Transaction.SigHash.ALL, false))
				.collect(Collectors.toList());

		// 创建input脚本
		Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(txSigs, redeemScript);
		System.out.format("P2SH input script => %s\n", inputScript);
		txi.setScriptSig(inputScript);

		// 输出RawTx
		String txHex = HEX.encode(tx.bitcoinSerialize());

		// 使用节点旳decoderawtransaction调用来检查一下裸交易的内容是否与 我们的预期一致
		BitcoinJSONRPCClient client = new BitcoinJSONRPCClient("http://admin:huobijp@52.199.36.243:8332");
		BitcoinJSONRPCClient.RawTransaction rawTx = client.decodeRawTransaction(txHex);
		System.out.format("raw tx => %s\n",rawTx);

		// 广播交易，将交易送给节点处理,如果交易不合法会被拒绝
		String txid = client.sendRawTransaction(txHex);
		System.out.format("txid => %s\n", txid);
	}
}
