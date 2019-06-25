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
//		List<String> mnemonics = Arrays.asList(new String[]{"forum", "rug", "slice", "snack", "width", "inside",
//				"mad", "cotton", "noodle", "april", "dumb", "adapt"});
//		String passphrase = "123456";
		MetanetNodeManager nodeManager = new MetanetNodeManager(params);
		MetanetTreeManager treeManager = new MetanetTreeManager(params);

		List<String> newMnemonics = Arrays.asList(new String[] {"oak", "device", "pretty", "swift", "custom"
				, "cousin", "remove", "poet", "negative", "live", "reward", "hurdle"});
		String passphrase = "yc19931012";
		DeterministicKey originKey = HDHierarchyKeyGenerator.restoreMasterKeyFromMnemonicCode(newMnemonics, passphrase);
		DeterministicKey rootKey = HDHierarchyKeyGenerator.deriveChildKeyByAbsolutePath(originKey, "M/1");
		MetanetNode rootNode = new MetanetNode(Base64.encode(rootKey.getPubKey()), originKey, null);
		nodeManager.getMetanetTree(rootNode);

		System.out.println("...");
//		MetanetNode passwdDirNode = treeManager.createDirNode(rootNode,"passwd", 3000);
//		MetanetNode userDirNode = treeManager.createDirNode(rootNode, "user", 3000);
//		MetanetNode photoDirNode = treeManager.createDirNode(rootNode, "photo", 3000);
//		MetanetNode photoDirNode = new MetanetNode(Base64.encode(photoDirKey.getPubKey()), photoDirKey, rootNode);
//		MetanetNode passwdFileNode1 = treeManager.createFileNode(passwdDirNode, "huobi", Arrays.asList(new String[]{"yaochao@huobi.com", "huobijp"}));
//		MetanetNode passwdFileNode2 = treeManager.createFileNode(passwdDirNode, "okex", Arrays.asList(new String[]{"yaochao@okex.com", "okex"}));
//		MetanetNode userfileNode = treeManager.createFileNode(userDirNode, "yaochao", Arrays.asList(new String[]{"super admin", "root user"}));
//		MetanetNode userfileNode2 = treeManager.createFileNode(userDirNode, "guest", Arrays.asList(new String[]{"guest", "read only"}));
//		MetanetNode photoFileNode = treeManager.createFileNode(photoDirNode, "myPhoto.jpg", Arrays.asList(new String[]{"jpg", "zip", "data of my photo"}));
	}
}

