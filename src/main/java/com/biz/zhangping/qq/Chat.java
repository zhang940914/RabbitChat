package com.biz.zhangping.qq;
import java.io.IOException;
import java.util.Scanner;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
public class Chat {
	private final static String EXCHANGE_NAME = "ex_log";
	public static void main(String[] args) throws IOException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		final Connection connection = factory.newConnection();
		final Channel channel = connection.createChannel();
		// 声明转发器和类型
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		Thread thread=new Thread() {
			public void run() {
				QueueingConsumer consumer = new QueueingConsumer(channel);
				// 创建一个非持久的、唯一的且自动删除的队列
				String queueName = null;
				try {
					queueName= channel.queueDeclare().getQueue();
					channel.queueBind(queueName, EXCHANGE_NAME, "");
					channel.basicConsume(queueName, false, consumer);
					while (true) {
						QueueingConsumer.Delivery delivery = null;
						try {
							delivery = consumer.nextDelivery();
						} catch (ShutdownSignalException e) {
							return;
						} catch (ConsumerCancelledException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try{
							String message = new String(delivery.getBody());
							System.out.println(" [x] Received '" + message + "'");
						}catch(NullPointerException e){
							return;
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		// 创建连接和频道
		
		Scanner in = new Scanner(System.in);
		String nickname = null;
		System.out.println("Welcome to Rabbit ChatRoom .^_^.");
		System.out.println("Type q to exit...");
		System.out.println("Input your nickname first .^_^.");
		System.out.println("hello" + (nickname = in.next()) + ",you can chat from now,enjoy it");
		thread.start();
		System.out.println();
		while (true) {
			String message = in.next();
			if ("q".equals(message)) {
				System.out.println("Goodbye .^_^.");
				System.out.println("Process finished with exit code 0");
				channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
				in.close();
				channel.close();
				connection.close();
						
					return;
			}
			// 往转发器上发送消息
			channel.basicPublish(EXCHANGE_NAME, "", null, (nickname + " said " + message).getBytes());

			System.out.println(" [x] Sent '" + message + "'");
		}

	}

}