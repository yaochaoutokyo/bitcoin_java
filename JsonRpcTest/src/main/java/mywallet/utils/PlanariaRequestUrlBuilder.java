package mywallet.utils;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;

import java.util.ArrayList;

/**
 * Created by yaochao on 2019/06/21
 */
public class PlanariaRequestUrlBuilder {

	private ArrayList<String> conditions;

	private NetworkParameters params;

	private static final String PLANARIA_QUERY_URL = "http://52.199.36.243:3000/q/1KWqy2WbNpEPC7hwvfJbvXy2vekS2LwGim/";

	public PlanariaRequestUrlBuilder(NetworkParameters params) {
		conditions = new ArrayList<>();
		this.params = params;
	}

	public PlanariaRequestUrlBuilder addMetaFlag() {
		conditions.add("\"out.s1\":\"meta\"");
		return this;
	}

	public PlanariaRequestUrlBuilder addInputAddress(String base58Address) {
		String condition = String.format("\"in.e.a\":\"%s\"", base58Address);
		conditions.add(condition);
		return this;
	}

	public PlanariaRequestUrlBuilder addOutputPubKeyScript(String base58Address) {
		// get base58 format of address, transfer it into base64 format of pubkeyScript
		Address address = Address.fromBase58(params,base58Address);
		byte[] pubKeyScript = address.getHash160();
		String base64PubKeyScript = Base64.encode(pubKeyScript);

		String condition = String.format("\"out.b2\":\"%s\"", base64PubKeyScript);
		conditions.add(condition);
		return this;
	}

	public String buildUrl() {

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < conditions.size(); i++) {
			stringBuilder.append(conditions.get(i));
			if (i != conditions.size() - 1) {
				stringBuilder.append(",");
			}
		}

		String originalJson = String.format("{\n" +
				"  \"v\": 3,\n" +
				"  \"q\": {\n" +
				"    \"find\": {%s},\n" +
				"    \"limit\": 10\n" +
				"  }\n" +
				"}", stringBuilder.toString());

		// apply base64 encode to the original json
		String queryCondition = Base64.encode(originalJson.getBytes());
		String url = PLANARIA_QUERY_URL + queryCondition;
		return url;
	}
}
