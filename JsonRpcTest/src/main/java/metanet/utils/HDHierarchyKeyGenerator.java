package metanet.utils;

import org.bitcoinj.crypto.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yaochao on 2019/06/23
 */
public class HDHierarchyKeyGenerator {

	/**
	 * @description: Create new HD wallet, and return mnemonic codes
	 * @return 12 mnemonic codes
	 * @date: 2019/06/24
	 **/
	public static List<String> generateNewMnemonicCode() {
		// generate random entropy using secure random generator
		SecureRandom random = new SecureRandom();
		byte[] entropy = new byte[16];
		random.nextBytes(entropy);
		// generate mnemonics
		// entropy（128 bits） + CheckSum（first 4 bits of Sha256(entropy)）= 12 mnemonic words（132/11 = 12）
		List<String> mnemonics = new ArrayList<>();
		try {
			MnemonicCode mnemonicCode = new MnemonicCode();
			mnemonics = mnemonicCode.toMnemonic(entropy);
		} catch (IOException e) {
			System.out.println("Exception happened when create mnemonic code...");
			e.printStackTrace();
		} catch (MnemonicException.MnemonicLengthException e) {
			System.out.println("Invalid entropy length...");
			e.printStackTrace();
		}

		return mnemonics;
	}

	/**
	 * @description: Restore masterkey of HD wallet with mnemonic code and optional passphrase
	 * @param mnemonics 12 mnemonic codes
	 * @param passphrase optional passphrase
	 * @date: 2019/06/24
	 **/
	public static DeterministicKey restoreMasterKeyFromMnemonicCode(List<String> mnemonics, String passphrase) {
		// calculate seed with mnemonics
		// mnemonics + salt（"mnemonic" + optional passphrase）--2048 * HMAC-SHA512--> seed
		byte[] seed = MnemonicCode.toSeed(mnemonics, passphrase);
		// generate master key
		// seed --HMAC-SHA512--> master privkey(256bit) + master chaincode(256bit)
		DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey(seed);
		return masterKey;
	}

	/**
	 * @description: Derive child key by absolute path
	 * @param masterKey root key
	 * @param absolutePath absolute path of child key
	 * @date: 2019/06/23
	 **/
	public static DeterministicKey deriveChildKeyByAbsolutePath(DeterministicKey masterKey, String absolutePath) {
		DeterministicHierarchy hierarchy = new DeterministicHierarchy(masterKey);
		List<ChildNumber> numbers = HDUtils.parsePath(absolutePath);
		DeterministicKey childKey = hierarchy.get(numbers, false, true);
		return childKey;
	}

	/**
	 * @description: Derive child key by relative path with parent key
	 * @param parentKey parent key
	 * @param relativePath relative path of child key
	 * @date: 2019/06/23
	 **/
	public static DeterministicKey deriveChildKeyByRelativePath(DeterministicKey parentKey, String relativePath) {
		DeterministicHierarchy hierarchy = new DeterministicHierarchy(parentKey);
		List<ChildNumber> numbers = HDUtils.parsePath(relativePath);
		DeterministicKey childKey = hierarchy.get(numbers, true, true);
		return childKey;
	}
}
