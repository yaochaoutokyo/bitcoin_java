package metanet.test;

import metanet.domain.MetanetNode;
import metanet.manager.MetanetNodeManager;
import metanet.manager.MetanetTreeManager;
import metanet.utils.HDHierarchyKeyGenerator;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.*;
import org.bitcoinj.params.MainNetParams;
import java.util.Arrays;
import java.util.List;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * Created by yaochao on 2019/06/19
 */
public class FileSystemGenerationTest {
	public static void main(String[] args) throws Exception {
		NetworkParameters params = MainNetParams.get();
		MetanetNodeManager nodeManager = new MetanetNodeManager(params);
		MetanetTreeManager treeManager = new MetanetTreeManager(params);

		List<String> newMnemonics = Arrays.asList(new String[] {"oak", "device", "pretty", "swift", "custom"
				, "cousin", "remove", "poet", "negative", "live", "reward", "hurdle"});
		String passphrase = "yc19931012";
		DeterministicKey originKey = HDHierarchyKeyGenerator.restoreMasterKeyFromMnemonicCode(newMnemonics, passphrase);
		DeterministicKey rootKey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(originKey, "M/1");
		MetanetNode rootNode = new MetanetNode(Base64.encode(rootKey.getPubKey()), rootKey, null);
		nodeManager.getMetanetTree(rootNode);

		DeterministicKey root1Key = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(originKey, "M/0");
		System.out.println(root1Key.serializePrivB58(params));
		MetanetNode root1Node = new MetanetNode(Base64.encode(root1Key.getPubKey()), root1Key, null);
		nodeManager.getMetanetNodeInfo(root1Node);
	}
}

