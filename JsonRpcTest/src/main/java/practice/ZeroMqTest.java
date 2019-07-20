package practice;

import org.zeromq.ZMQ;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.bitcoinj.core.Utils.HEX;

/**
 * Created by yaochao on 2019/06/26
 */
public class ZeroMqTest {
	private static final String url = "tcp://52.199.36.243:28332";

	ZeroMqTest() {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Runnable runner = this::connectZmqServer;
		executor.execute(runner);
	}

	private void connectZmqServer() {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.SUB);

		socket.connect(url);
		// socket.subscribe("rawblock");
		// socket.subscribe("hashblock");
		socket.subscribe("hashtx");
		// socket.subscribe("hashtx");
		System.out.println("connect to " + url);

		while (!Thread.currentThread().isInterrupted()) {
			String topic = socket.recvStr();
			System.out.println("client - topic:" + topic);
			if (socket.hasReceiveMore()) {
				byte[] bodyByte = socket.recv();
				String body = HEX.encode(bodyByte);
				switch (topic) {
					case "hashtx":
						System.out.println("client - body:" + body);
						break;
					default:
						break;
				}
			}
			if (socket.hasReceiveMore()) {
				String sequence = HEX.encode(socket.recv());
				System.out.println("client - sequence:" + sequence);
			}
		}
		socket.close();
		context.term();
	}

	public static void main(String[] args) {
		ZeroMqTest zeroMqTest = new ZeroMqTest();
	}
}

