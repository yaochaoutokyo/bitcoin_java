package metanet.utils;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import java.io.IOException;

/**
 * Created by yaochao on 2019/06/21
 */
public class HttpRequestSender {

	private static OkHttpClient okHttpClient;

	private static final String PLANARIA_KEY = "1KWqy2WbNpEPC7hwvfJbvXy2vekS2LwGim";

	private static final String BITINDEX_GET_UTXO_WITH_ADDRESS = "https://api.bitindex.network/api/v3/%s/addr/%s/utxo";

	static {
		okHttpClient = new OkHttpClient();
	}

	public static String sendHttpRequestToPlanaria(String url) throws IOException {
		Request request = new Request.Builder().header("key",PLANARIA_KEY).url(url).build();
		return sendHttpRequest(request);
	}

	public static String getUtxoForPubKey(String base64PubKey, NetworkParameters params) throws IOException {
		if (! params.equals(MainNetParams.get())) {
			throw new IllegalArgumentException("currently Bitindex only support main net");
		}
		String base58Address = AddressFormatTransformer.base64PubKeyToBase58Address(params, base64PubKey);
		String url = String.format(BITINDEX_GET_UTXO_WITH_ADDRESS, "main", base58Address);
		return sendHttpRequest(url);
	}

	public static String sendHttpRequest(String url) throws IOException {
		Request request = new Request.Builder().url(url).build();
		return sendHttpRequest(request);
	}

	public static String sendHttpRequest(Request request) throws IOException {
		Response response = null;
		String json = null;
		try {
			response = okHttpClient.newCall(request).execute();
			if (response.isSuccessful()) {
				json = response.body().string();
			}
		} finally {
			if (response != null) {
				response.body().close();
			}
		}
		return json;
	}
}
