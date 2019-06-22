package practice;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

import static org.bitcoinj.core.Utils.HEX;

/**
 * Created by yaochao on 2019/06/17
 */
public class P2PKHTest {
	public static void main(String[] args) {

		NetworkParameters params = MainNetParams.get();
		ECKey key = new ECKey();
		Address addr = key.toAddress(params);

		// 构造锁定脚本
		ScriptBuilder sb = new ScriptBuilder();
		Script pubScript = sb.op(ScriptOpCodes.OP_DUP)
				.op(ScriptOpCodes.OP_HASH160)
				.data(addr.getHash160())
				.op(ScriptOpCodes.OP_EQUALVERIFY)
				.op(ScriptOpCodes.OP_CHECKSIG)
				.build();

		System.out.format("pubHash => %s\n", HEX.encode(addr.getHash160()));
		System.out.format("pubScript => %s\n", pubScript.toString());
	}
}
