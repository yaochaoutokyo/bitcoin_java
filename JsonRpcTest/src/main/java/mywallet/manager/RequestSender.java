package mywallet.manager;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by yaochao on 2019/06/21
 */
public class RequestSender {

	private static OkHttpClient okHttpClient;

	static {
		okHttpClient = new OkHttpClient();
	}

	public static String sendHttpRequestToPlanaria(String url) throws IOException {
		Request request = new Request.Builder().header("key","1KWqy2WbNpEPC7hwvfJbvXy2vekS2LwGim").url(url).build();
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
