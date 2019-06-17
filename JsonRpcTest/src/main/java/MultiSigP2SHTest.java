import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Created by yaochao on 2019/06/18
 */
public class MultiSigP2SHTest {

	public static void main(String[] args) {
		NetworkParameters params = MainNetParams.get();
		List<ECKey> ecKeys = Arrays.asList(new ECKey(), new ECKey(), new ECKey());

		// 输入钥匙list，和阀值，构建 阀值 - list.size() 的多重签名交易脚本
		Script redeemScript = ScriptBuilder.createMultiSigOutputScript(2, ecKeys);
		System.out.format("redeemScript => %s\n",redeemScript);

		// 通过多重签名交易脚本生成见证脚本
		Script p2shScript = ScriptBuilder.createP2SHOutputScript(redeemScript);
		System.out.format("p2shScript => %s\n",p2shScript);

		byte[] p2shHash = Utils.sha256hash160(redeemScript.getProgram());
		Address p2shAddr = Address.fromP2SHHash(params, p2shHash);
		System.out.format("p2shAddr => %s\n", p2shAddr.toBase58());
	}
}
