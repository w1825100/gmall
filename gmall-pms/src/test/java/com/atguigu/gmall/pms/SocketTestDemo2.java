package com.atguigu.gmall.pms;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.*;

/**
 * @program: gmall
 * @description: 使用多线程(线程池)改造socket通信
 * @author: lgd
 * @create: 2021-02-22 12:02
 **/
public class SocketTestDemo2 {
    public static void main(String[] args) throws Exception {
        //1.初始化线程池 核心10,最大50,存活时间300s,任务队列10,默认线程工厂,超出直接抛异常
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                10, 20, 300,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(10),
                Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy()
        );
        //2.初始化serverSocket
        ServerSocket serverSocket = new ServerSocket(9999);

        //3.启动服务,阻塞式等待连接
        while (true) {
            Socket socket = serverSocket.accept();
            //异步执行任务
            threadPoolExecutor.execute(new Task(socket));
        }
    }

}

//任务对象,用于执行服务端取得连接之后的业务逻辑
class Task implements Runnable {
    private Socket socket;

    public Task(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //获得连接channel
            InputStream serverInputStream = socket.getInputStream();
            OutputStream serverOutputStream = socket.getOutputStream();
            int len;
            byte[] b1 = new byte[1024];
            while ((len = serverInputStream.read(b1)) != -1) {
                String request = new String(b1, 0, len);
                System.out.println(Thread.currentThread().getName() + ":接收到客户端请求了,内容为:" + request);
                if (request.startsWith("GET")) {
                    System.out.println("判断出请求来自浏览器:");
                    System.out.println("向客户端回复...");
                    String responseHeader = "http/1.1 200 ok\n\r\n";
                    String responseBody = "<!DOCTYPE html>\n" +
                            "<html lang=\"en\">\n" +
                            "<head>\n" +
                            "    <meta charset=\"UTF-8\">\n" +
                            "    <title>Title</title>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "    <h1><font color=\"red\">自定义html</font></h1>\n" +
                            "</body>\n" +
                            "</html>\n";
                    serverOutputStream.write((responseHeader + responseBody).getBytes());
                    serverOutputStream.flush();
                } else {
                    System.out.println("判断出请求来自java客户端");
                    System.out.println("向客户端回复...");
                    serverOutputStream.write(("你好,我是服务端" + Thread.currentThread().getName()).getBytes());
                }
            }
            socket.shutdownOutput();
            socket.close();
            serverInputStream.close();
            serverOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Client {
    @Test
    public void clientTest() throws Exception {
        Socket socket = new Socket("127.0.0.1", 9966);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        System.out.println("向服务端发起请求...");
        outputStream.write("你好,我是客户端".getBytes());
        socket.shutdownOutput();

        //获得连接channel
        int len;
        byte[] b1 = new byte[1024];
        while ((len = inputStream.read(b1)) != -1) {
            System.out.println("接受到服务端响应了,内容为:" + new String(b1, 0, len));
        }
        socket.shutdownInput();
        socket.close();
        inputStream.close();
        outputStream.close();
    }


}

