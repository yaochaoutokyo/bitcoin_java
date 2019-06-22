package practice;

import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import static org.bitcoinj.core.Utils.HEX;

/**
 * Created by yaochao on 2019/06/18
 */
public class SendRawTxTest {
	public static void main(String[] args) throws Exception {
		// 创建3个地址，分别对应发起者，接收者和找零地址
		ECKey senderKey = new ECKey();
		ECKey receiverKey = new ECKey();
		ECKey changeKey = new ECKey();

		// 创建网络参数，建立交易
		NetworkParameters params = MainNetParams.get();
		Transaction tx = new Transaction(params);

		// 添加交易输出
		// 第一个输出，接收者地址加BTC数量
		Coin txValue = Coin.parseCoin("5");
		Address receiverAddr = receiverKey.toAddress(params);
		tx.addOutput(txValue, receiverAddr);
		// 第二个输出，找零地址加找零BTC数量，输入与输出的差值为fee(0.0001BTC)
		Coin changeValue = Coin.parseCoin("4.9999");
		Address changeAddr = changeKey.toAddress(params);
		tx.addOutput(changeValue, changeAddr);

		// 使用addSingedInput添加输入，其参数是交易输出点（交易id和输出序号）、 公钥脚本和签名私钥
		// 必须先加入输出才能签名，因为签名使用的签名体是交易输出，否则会报交易无输出异常
		String utxoTxid = "78a37b87b03101a5ff28266f01365e61122bca00c6910f8d9c5c0f450cd05c62";
		int utxoVout = 0;
		TransactionOutPoint outPoint = new TransactionOutPoint(params, utxoVout, Sha256Hash.wrap(utxoTxid));
		Script utxoScript = ScriptBuilder.createOutputScript(senderKey.toAddress(params));
		tx.addSignedInput(outPoint, utxoScript, senderKey);

		// bitcoinSerialize()方法将签名的交易对象转化为字节数组，并进一步编码为16进制字符串;
		// 这就是可以直接提交给节点旳sendrawtransaction调用的裸交易
		String txHex = HEX.encode(tx.bitcoinSerialize());
		System.out.format("tx hex => %s\n",txHex);

		// 使用节点旳decoderawtransaction调用来检查一下裸交易的内容是否与 我们的预期一致
		BitcoinJSONRPCClient client = new BitcoinJSONRPCClient("http://admin:huobijp@52.199.36.243:8332");
		BitcoinJSONRPCClient.RawTransaction rawTx = client.decodeRawTransaction(txHex);
		System.out.format("raw tx => %s\n",rawTx);

		// 将交易送给节点处理,如果交易不合法会被拒绝
		String txid = client.sendRawTransaction(txHex);
		System.out.format("txid => %s\n", txid);
	}
}
