package metanet.manager;

import metanet.domain.MetanetNode;
import org.bitcoinj.crypto.DeterministicKey;

/**
 * Created by yaochao on 2019/06/22
 */
public class MetanetTreeManager {

	private MetanetNode currentNode;

	private DeterministicKey masterKey;

	public MetanetTreeManager(MetanetNode currentNode, DeterministicKey masterKey) {
		this.currentNode = currentNode;
		this.masterKey = masterKey;
	}
}
