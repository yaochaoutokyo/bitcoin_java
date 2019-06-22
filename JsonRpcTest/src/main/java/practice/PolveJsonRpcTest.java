package practice;

import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

/**
 * Created by yaochao on 2019/06/17
 */
public class PolveJsonRpcTest {
	public static void main(String[] args) throws Exception {
		String nodeUrl = "http://admin:huobijp@52.199.36.243:8332";
		BitcoinJSONRPCClient client = new BitcoinJSONRPCClient(nodeUrl);
		System.out.println("network info => " + client.getNetworkInfo());
		System.out.println("validate address => " + client.validateAddress("13FpbqJoYY3cCPfUomE1oxctXDWJQPoEvJ"));
	}
}
