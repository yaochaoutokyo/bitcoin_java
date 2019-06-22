package practice;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;

/**
 * Created by yaochao on 2019/06/17
 */
public class AddressGenerationTest {
	public static void main(String[] args) {

		NetworkParameters main = MainNetParams.get();
		NetworkParameters test = TestNet3Params.get();
		NetworkParameters reg = RegTestParams.get();

		// 各个网络的地址prefix不一样
		System.out.format("main-net address header => %s\n",Integer.toHexString(main.getAddressHeader()));
		System.out.format("test-net address header => %s\n",Integer.toHexString(test.getAddressHeader()));
		System.out.format("reg-net address header => %s\n",Integer.toHexString(reg.getAddressHeader()));

		ECKey key = new ECKey();

		// 网络前缀 + 公钥哈希 + 校验和 --base58--> 地址
		// 网络前缀 + 公钥哈希 --2 * sha256，取前4字节--> 校验和
		// 因此确定一个地址需要2个参数：网络前缀，公钥哈希
		Address mainAddr = new Address(main, key.getPubKeyHash());
		System.out.format("main-net Address => %s\n", mainAddr.toBase58());

		Address testAddr = new Address(test, key.getPubKeyHash());
		System.out.format("test-net Address => %s\n", testAddr.toBase58());

		Address regAddr = new Address(reg, key.getPubKeyHash());
		System.out.format("reg-net Address => %s\n", regAddr.toBase58());

		// 由于key和地址是一一对应的，所以 key 加上 网络参数 也可以得到地址；
		Address addr = key.toAddress(main);
		System.out.format("main-net Address => %s\n", addr.toBase58());
	}
}
