import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;

import static org.bitcoinj.core.Utils.HEX;
import static org.bitcoinj.core.Utils.sha256hash160;

/**
 * Created by yaochao on 2019/06/17
 */
public class ECKeyTest {
	public static void main(String[] args) {
		ECKey key = new ECKey();
		System.out.format("compressed? => %s", key.isCompressed());

		String priv = key.getPrivateKeyAsHex();
		System.out.format("priv key => %s\n", priv);

		String pub = key.getPublicKeyAsHex();
		System.out.format("pub key => %s\n", pub);

		System.out.format("wrong address => %s\n", Base58.encode(sha256hash160(key.getPubKey())));

		String pubKeyHash = HEX.encode(key.getPubKeyHash());
		System.out.format("pubKey Hash => %s\n", pubKeyHash);
	}
}
