import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Created by yaochao on 2019/06/17
 */
public class BitcoindJsonRpcTest {
	public static void main(String[] args) throws Exception {
		//创建HttpClient
		CloseableHttpClient client = HttpClients.createDefault();

//创建请求
		HttpPost req = new HttpPost("http://admin:huobijp@52.199.36.243:8332");
		req.setHeader("Content-Type","application/json");
		StringEntity payload = new StringEntity("{\"jsonrpc\":\"1.0\",\"method\":\"getnetworkinfo\",\"params\":[],\"id\":123}");
		req.setEntity(payload);

//获取响应
		CloseableHttpResponse rsp = client.execute(req);
		String ret = EntityUtils.toString(rsp.getEntity());
		client.close();

//显示结果
		System.out.format("rsp => \n%s\n",ret);
	}
}
