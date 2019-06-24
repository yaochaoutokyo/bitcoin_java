package metanet.utils;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDUtils;

import java.util.List;

/**
 * Created by yaochao on 2019/06/23
 */
public class HDHierarchyKeyGenerator {

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
