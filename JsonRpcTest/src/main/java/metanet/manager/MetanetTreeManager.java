package metanet.manager;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import metanet.domain.MetanetNode;
import metanet.utils.HDHierarchyKeyGenerator;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: Class for managing metanet tree
 * @author YAO Chao
 * @date: 2019/06/25
 **/
public class MetanetTreeManager {

	private MetanetNodeManager nodeManager;

	private MetanetEdgeManager edgeManager;

	private static final long DUST_VALUE_SATOSHI = 600L;

	private static final String DIR = "dir";

	private static final String FILE = "file";

	public MetanetTreeManager(NetworkParameters params) {
		nodeManager = new MetanetNodeManager(params);
		edgeManager = new MetanetEdgeManager(params);
	}

	/**
	 * @description: Create a root node with a pesudoParentNode
	 * @param pesudoParentNode imaginary parent node
	 * @param value value send to root node, if value equals to zero, means only create root node
	 *              if value doesn't equal to zero, it should at least 600 satoshi, otherwise it will
	 *              be regarded as dust output
	 * @date: 2019/06/24
	 **/
	public MetanetNode createRootNode(MetanetNode pesudoParentNode, long value) {
		nodeManager.getMetanetNodeInfo(pesudoParentNode);
		MetanetNode rootNode = getNewAvailableChildNode(pesudoParentNode);
		if (value >= DUST_VALUE_SATOSHI) {
			edgeManager.buildMetanetRootNodeWithValue(pesudoParentNode, rootNode, value);
		} else if (value == 0L) {
			edgeManager.buildMetanetRootNodeWithoutValue(pesudoParentNode, rootNode);
		} else {
			System.out.println("value = 0 or value > 600");
		}
		return rootNode;
	}

	/**
	 * @description: Create a directory node with certain amount of value
	 * @param parentNode parent node of the new directory node
	 * @param dirName the name of the directory
	 * @param value value send to the directory node, at least should be 600 satoshi
	 * @date: 2019/06/24
	 **/
	public MetanetNode createDirNode(MetanetNode parentNode, String dirName, long value) {
		nodeManager.getMetanetNodeInfo(parentNode);
		MetanetNode dirNode = getNewAvailableChildNode(parentNode);
		// prepare payloads
		List<String> payloads = new ArrayList<>();
		payloads.add(DIR);
		payloads.add(dirName);
		// create edge from parent node to the new dir
		if (value >= DUST_VALUE_SATOSHI) {
			edgeManager.buildEdgeToMetanetNodeWithValue(parentNode, dirNode, payloads, value);
		} else {
			System.out.println("value = 0 or value > 600");
		}
		return dirNode;
	}

	/**
	 * @description: Create a file node with contents, a file node doesn't contain value
	 * @param parentNode parent node of the new file node
	 * @param fileName file name
	 * @param fileContent contents store in the file node
	 * @date: 2019/06/24
	 **/
	public MetanetNode createFileNode(MetanetNode parentNode, String fileName, List<String> fileContent) {
		nodeManager.getMetanetNodeInfo(parentNode);
		MetanetNode fileNode = getNewAvailableChildNode(parentNode);
		// prepare payloads
		List<String> payloads = new ArrayList<>();
		payloads.add(FILE);
		payloads.add(fileName);
		payloads.addAll(fileContent);
		edgeManager.buildEdgeToMetanetNodeWithoutValue(parentNode, fileNode, payloads);
		return fileNode;
	}

	/**
	 * @description: Edit a existing directory node
	 * @param parentNode parent node of the directory node, only the parent node can modify its child node
	 * @param dirNode the directory node which are going to be edited
	 * @param NewDirName new directory name
	 * @param value if value = 0, means don't send value, if value != 0, it should at least 600 satoshi
	 * @date: 2019/06/24
	 **/
	public MetanetNode editDirNode(MetanetNode parentNode, MetanetNode dirNode, String NewDirName, long value) {
		nodeManager.getMetanetNodeInfo(parentNode);
		List<String> payloads = new ArrayList<>();
		payloads.add(DIR);
		payloads.add(NewDirName);
		// create edge from parent node to the new dir
		if (value >= DUST_VALUE_SATOSHI) {
			edgeManager.buildEdgeToMetanetNodeWithValue(parentNode, dirNode, payloads, value);
		} else if (value == 0L) {
			edgeManager.buildEdgeToMetanetNodeWithoutValue(parentNode, dirNode, payloads);
		} else {
			System.out.println("value = 0 or value > 600");
		}
		// get info of the new dir
		return dirNode;
	}

	/**
	 * @description: Edit a existing file node
	 * @param parentNode parent node of the directory node, only the parent node can modify its child node
	 * @param fileNode the file node which are going to be edited
	 * @param newFileName new file name
	 * @param newFileContent new file contents
	 * @date: 2019/06/24
	 **/
	public MetanetNode editFileNode(MetanetNode parentNode, MetanetNode fileNode, String newFileName
			, List<String> newFileContent) {
		nodeManager.getMetanetNodeInfo(parentNode);
		// prepare payloads
		List<String> payloads = new ArrayList<>();
		payloads.add(FILE);
		payloads.add(newFileName);
		payloads.addAll(newFileContent);
		edgeManager.buildEdgeToMetanetNodeWithoutValue(parentNode, fileNode, payloads);
		return fileNode;
	}

	/**
	 * @description: Change value to a existing directory node from other metanet-node or outer source
	 * @param senderNode could be a meta-dir-node, or other source
	 * @param receiverNode the target directory node
	 * @param value value send to the directory node
	 * @date: 2019/06/24
	 **/
	public MetanetNode sendValueToNode(MetanetNode senderNode, MetanetNode receiverNode, long value) {
		edgeManager.sendMoneyFromNodeAToNodeB(senderNode, receiverNode, value);
		nodeManager.getMetanetNodeInfo(senderNode);
		return receiverNode;
	}

	/**
	 * @description: Get the new available child metanet-node of a existing metanet-node, the index
	 * of the new child node is the sum of number of its siblings plus one
	 * @param parentNode parent node
	 * @date: 2019/06/24
	 **/
	private MetanetNode getNewAvailableChildNode(MetanetNode parentNode) {
		// get the available child index and child key
		nodeManager.getMetanetNodeInfo(parentNode);
		List<MetanetNode> children = parentNode.getChildren();
		int indexOfDirNode = children == null ? 0 : children.size();
		String relativePathOfDirNode = String.format("/%d",indexOfDirNode);
		DeterministicKey parentNodeKey = parentNode.getKey();
		DeterministicKey dirNodeKey = HDHierarchyKeyGenerator.deriveChildKeyByRelativePath(parentNodeKey, relativePathOfDirNode);
		// create new metanet node for the dir
		String base64PubKeyOfDirNode = Base64.encode(dirNodeKey.getPubKey());
		MetanetNode dirNode = new MetanetNode(base64PubKeyOfDirNode, dirNodeKey, parentNode);
		return dirNode;
	}
}
