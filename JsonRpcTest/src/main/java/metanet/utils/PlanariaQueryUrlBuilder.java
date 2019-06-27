package metanet.utils;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.util.ArrayList;

/**
 * @description: Class for building planaria Query Url
 * @author YAO Chao
 * @date: 2019/06/22
 **/
public class PlanariaQueryUrlBuilder {

	private ArrayList<String> findConditions;

	private static final String PLANARIA_QUERY_URL = "http://52.199.36.243:3000/q/1KWqy2WbNpEPC7hwvfJbvXy2vekS2LwGim/";

	private String limitCondition = ",{\"limit\": %d}";

	public PlanariaQueryUrlBuilder() {
		findConditions = new ArrayList<>();
	}

	/**
	 * @description: add OP_RETURN code
	 * @date: 2019/06/22
	 **/
	public PlanariaQueryUrlBuilder addOpReturn() {
		findConditions.add("\"out.b0.op\":106");
		return this;
	}

	/**
	 * @description: add metenet flag into the first term about OP_RETURN "out.s1" condition
	 * @date: 2019/06/22
	 **/
	public PlanariaQueryUrlBuilder addMetaFlag() {
		findConditions.add("\"out.s1\":\"meta\"");
		return this;
	}

	/**
	 * @description: add base64 pubKey into the input pubKey "in.b1" condition
	 * @param parentPubKey base64 pubKey of parent node
	 * @date: 2019/06/22
	 **/
	public PlanariaQueryUrlBuilder addParentNodePubKey(String parentPubKey) {
		String condition = String.format("\"in.b1\":\"%s\"", parentPubKey);
		findConditions.add(condition);
		return this;
	}

	/**
	 * @description: add base64 pubKey into the second term after OP_RETURN which is "out.b2" condition
	 * @param childAddress base64 pubKey of child node
	 * @date: 2019/06/22
	 **/
	public PlanariaQueryUrlBuilder addChildNodeAddress(String childAddress) {
		String condition = String.format("\"out.s2\":\"%s\"", childAddress);
		findConditions.add(condition);
		return this;
	}

	/**
	 * @description: set query limit
	 * @param limit maximum number of transactions in once response
	 * @date: 2019/06/23
	 **/
	public PlanariaQueryUrlBuilder setQueryLimit(Integer limit) {
		if (limit != null) {
			limitCondition = String.format(limitCondition, limit);
		} else {
			limitCondition = "";
		}
		return this;
	}

	/**
	 * @description: buildRawTxHex planaria query url
	 * @date: 2019/06/22
	 **/
	public String buildUrl() {

		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < findConditions.size(); i++) {
			stringBuilder.append(findConditions.get(i));
			if (i != findConditions.size() - 1) {
				stringBuilder.append(",");
			}
		}
		String originalJson = String.format("{ \"v\": 3, \"q\": { \"find\": {%s} %s}}",
				stringBuilder.toString(), limitCondition);

		// apply base64 encode to the original json
		String queryCondition = Base64.encode(originalJson.getBytes());
		String url = PLANARIA_QUERY_URL + queryCondition;
		return url;
	}
}
