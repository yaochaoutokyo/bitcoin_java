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
		MetanetNode originNode = new MetanetNode(params, originKey, null);

		// todo: the tx order in planaria and whatsonchain could be different, it is better to add the transaction version and child location in to the payloads
		DeterministicKey rootKey2 = HDHierarchyKeyGenerator.deriveChildKeyByRelativePath(originKey, "/2");
		MetanetNode rootNode2 = new MetanetNode(params, rootKey2, null);
		nodeManager.getMetanetTree(rootNode2);
		System.out.println("...");
	}
}

