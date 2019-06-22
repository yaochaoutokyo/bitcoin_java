package practice;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import static org.bitcoinj.script.ScriptOpCodes.OP_CHECKSIG;
import static org.bitcoinj.script.ScriptOpCodes.OP_EQUAL;
import static org.bitcoinj.script.ScriptOpCodes.OP_HASH160;

/**
 * Created by yaochao on 2019/06/17
 */
public class P2SHTest {
	public static void main(String[] args) {
		NetworkParameters params = MainNetParams.get();
		ECKey key = new ECKey();

		// 见证脚本：<pubKey> CHECKSIG
		Script redeemScript = (new ScriptBuilder()).data(key.getPubKey()).op(OP_CHECKSIG).build();
		// 见证脚本哈希
		byte[] hash = Utils.sha256hash160(redeemScript.getProgram());
		// P2SH地址
		Address addr = Address.fromP2SHHash(params, hash);
		System.out.format("p2sh address => %s\n", addr.toString());

		// 锁定脚本: HASH160 <scriptHash> EQUAL
		Script lockingScript = new ScriptBuilder().op(OP_HASH160).data(redeemScript.getProgram()).op(OP_EQUAL).build();
		System.out.format("locking script of P2SH => %s\n", lockingScript.toString());

		// 解锁脚本: <Sig> <redeemScript>
		// 完整脚本: <Sig> < <pubKey> CHECKSIG > HASH160 <scriptHash> EQUAL
	}
}
