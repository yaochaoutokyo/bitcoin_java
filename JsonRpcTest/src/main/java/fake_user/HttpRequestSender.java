package fake_user;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRPCException;

import java.io.IOException;
import java.util.List;

/**
 * @description: Util for dealing with Http request
 * @author YAO Chao
 * @date: 2019/06/25
 **/
public class HttpRequestSender {

	private static OkHttpClient okHttpClient;

	private static BitcoinJSONRPCClient jsonRpcClient;

	private static Gson gson = new Gson();

	private static final String JSON_RPC_URL = "http://admin:huobijp@52.199.36.243:8332";

	private static final String BITINDEX_GET_UTXO_WITH_ADDRESS = "https://api.bitindex.network/api/v3/%s/addr/%s/utxo";

	static {
		okHttpClient = new OkHttpClient();
		try {
			jsonRpcClient = new BitcoinJSONRPCClient(JSON_RPC_URL);
		}catch(Exception e){
			System.out.format("static init error");
		}
	}

	/**
	 * @description: Query UTXOs of current pubKey by using Bitindex API
	 * @date: 2019/06/23
	 **/
	public static List<UserUTXO> getUtxoByAddress(String address) {
		String url = String.format(BITINDEX_GET_UTXO_WITH_ADDRESS, "main", address);
		String json = sendHttpRequest(url);
		return gson.fromJson(json, new TypeToken<List<UserUTXO>>(){}.getType());
	}

	/**
	 * @description: Fundamental method for sending http request
	 * @param url url of request
	 * @date: 2019/06/23
	 **/
	public static String sendHttpRequest(String url) {
		Request request = new Request.Builder().url(url).build();
		return sendHttpRequest(request);
	}

	/**
	 * @description: Fundamental method for sending http request
	 * @param request
	 * @date: 2019/06/23
	 **/
	public static String sendHttpRequest(Request request) {
		Response response = null;
		String json = null;
		try {
			response = okHttpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				json = response.body().string();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("fail to build connection...");
		} finally {
			if (response != null) {
				try {
					response.body().close();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("fail to close response body");
				}
			}
		}
		return json;
	}

	/**
	 * @description: decode raw transaction by JSON RPC
	 * @param txHex Hex format of raw transaction
	 * @date: 2019/06/23
	 **/
	public static BitcoinJSONRPCClient.RawTransaction decodeRawTransaction(String txHex) {
		BitcoinJSONRPCClient.RawTransaction rawTx = jsonRpcClient.decodeRawTransaction(txHex);
		System.out.format("raw tx => %s\n",rawTx);
		return rawTx;
	}

	/**
	 * @description: broadcast raw transaction by JSON RPC
	 * @param txHex Hex format of raw transaction
	 * @date: 2019/06/23
	 **/
	public static String broadcastRawTransaction(String txHex) {
		String txid = null;
		try {
			txid = jsonRpcClient.sendRawTransaction(txHex);
		} catch (BitcoinRPCException e) {
			e.printStackTrace();
			System.out.println("illegal transaction...");
		}
		System.out.format("txid => %s\n", txid);
		return txid;
	}
}
