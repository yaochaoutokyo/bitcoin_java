package metanet.utils;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;

/**
 * @description: Address format transformer
 * @author YAO Chao
 * @date: 2019/06/22
 **/
public class AddressFormatTransformer {

	/**
	 * @description: Transform base64 format pubKeyHash to base58 format of address
	 * @param base64PubKeyHash base64 format of pubKeyHash
	 * @param params Network type
	 * @date: 2019/06/22
	 **/
	public static String base64PubKeyHashToBase58Address(NetworkParameters params, String base64PubKeyHash) {
		Address address = new Address(params, Base64.decode(base64PubKeyHash));
		return address.toBase58();
	}

	/**
	 * @description: Transform base58 format of address to base64 format pubKeyHash
	 * @param base58Address base58 format of address
	 * @param params Network type
	 * @date: 2019/06/22
	 **/
	public static String base58AddressToBase64PubKeyHash(NetworkParameters params, String base58Address) {
		Address address = Address.fromBase58(params,base58Address);
		byte[] pubKeyScript = address.getHash160();
		return Base64.encode(pubKeyScript);
	}

	/**
	 * @description: Transform base64 format pubKey to base58 format of address
	 * @param base64PubKey base64 format of pubKeyHash
	 * @param params Network type
	 * @date: 2019/06/22
	 **/
	public static String base64PubKeyToBase58Address(NetworkParameters params, String base64PubKey) {
		byte[] pubKey = Base64.decode(base64PubKey);
		byte[] pubKeyHash = Utils.sha256hash160(pubKey);
		Address address = new Address(params, pubKeyHash);
		return address.toBase58();
	}
}
