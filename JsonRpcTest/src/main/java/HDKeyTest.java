import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.*;
import org.bitcoinj.params.MainNetParams;

import java.security.SecureRandom;
import java.util.List;

import static org.bitcoinj.core.Utils.HEX;

/**
 * Created by yaochao on 2019/06/18
 */
public class HDKeyTest {
	public static void main(String[] args) throws Exception {
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

		// 助记词转换为种子
		// 助记词 + 种子（"mnemonic" + 可选的passphrase）--2048 * HMAC-SHA512--> seed
		String passphrase = "123456";
		byte[] seed = MnemonicCode.toSeed(mnemonics,passphrase);
		System.out.format("seed => %s\n", HEX.encode(seed));

		// 生成主密钥
		// seed --HMAC-SHA512--> master privkey(256bit) + master chaincode(256bit)
		DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed);
		System.out.format("master key => %s\n", masterKey.getPrivateKeyAsHex());

		// 普通衍生
		// 使用masterKey衍生子私钥
		DeterministicKey childPrivKey = HDKeyDerivation.deriveChildKey(masterKey, 12);
		System.out.println("derive with master privkey:");
		System.out.format("child privKey => %s\n", childPrivKey.getPrivateKeyAsHex());
		System.out.format("child pubKey => %s\n", childPrivKey.getPublicKeyAsHex());
		// 普通衍生的情况下，只需要父公钥和父链码就可以推导出指定编号的子公钥和子链码
		// 丢掉了私钥只有公钥和链码
		DeterministicKey masterPubKey = masterKey.dropPrivateBytes();
		DeterministicKey childPubKey = HDKeyDerivation.deriveChildKey(masterPubKey, 12);
		System.out.println("derive with master pubkey:");
//		System.out.format("child privKey => %s\n", childPubKey.getPrivateKeyAsHex()); // 没有私钥
		System.out.format("child pubKey => %s\n", childPubKey.getPublicKeyAsHex());

		// 将层级密钥输出成拓展密钥，需要网络参数主网前缀为xprv和xpub，测试网为tprv和tpub
		NetworkParameters params = MainNetParams.get();
		String xprv = childPrivKey.serializePrivB58(params);
		String xpub = childPrivKey.serializePubB58(params);
		System.out.format("xprv => %s\n", xprv);
		System.out.format("xpub => %s\n", xpub);
		// 还可以将拓展密钥输出成层级密钥,同样需要拓展密钥和网络参数
		DeterministicKey keyFromXprv = DeterministicKey.deserializeB58(xprv, params);
		System.out.format("prv => %s\n", keyFromXprv.getPrivateKeyAsHex());
		System.out.format("pub => %s\n", keyFromXprv.getPublicKeyAsHex());

		// 硬化衍生
		// 比特币根据子密钥序号来区分派生普通密钥还是强化密钥：当序号 小于0x80000000时，生成普通子密钥，否则生成强化子密钥。
		// 注意，你需要从一个包含私钥的层级密钥才能派生强化子密钥。
		int index = 12;
		DeterministicKey normalKey = HDKeyDerivation.deriveChildKey(childPrivKey,index);
		DeterministicKey hardenKey = HDKeyDerivation.deriveChildKey(childPrivKey,index | 0x80000000);
		System.out.format("normal child privkey => %s\n", normalKey.getPrivateKeyAsHex());
		System.out.format("harden child privkey => %s\n", hardenKey.getPrivateKeyAsHex());
		// 还可以使用childNumber来指定是否硬化
		DeterministicKey hardenKeyByChildNum = HDKeyDerivation.deriveChildKey(childPrivKey, new ChildNumber(index,true));
		System.out.format("harden child privkey (child num) => %s\n", hardenKeyByChildNum.getPrivateKeyAsHex());

		// 路径表示法
		DeterministicHierarchy hierarchy = new DeterministicHierarchy(masterKey);
		String path = "/1";
		List<ChildNumber> numbers = HDUtils.parsePath(path);
		DeterministicKey keyFromHDPath = hierarchy.get(numbers, true, true);
		System.out.format("harden child privkey (path) => %s\n", keyFromHDPath.getPrivateKeyAsHex());
		System.out.println("path =>" + keyFromHDPath.getPath());
	}
}
