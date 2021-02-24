package com.atguigu.gmall.pms;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @program: gmall
 * @description: 复习socket编程
 * @author: lgd
 * @create: 2021-02-22 11:38
 **/
public class SocketTestDemo {
    @Test
    public void Client() throws Exception {
//    获得套接字对象  服务器ip
        InetAddress serverIp = InetAddress.getByName("127.0.0.1");
        //建立连接
        Socket socket = new Socket(serverIp, 9999);

        //获得连接channel
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();

        outputStream.write("你好,我是客户端".getBytes());
        socket.shutdownOutput();
        int len1;
        byte[] b1 = new byte[1024];
        while ((len1 = inputStream.read(b1)) != -1) {
            System.out.println("接收到服务器回复了:" + new String(b1, 0, len1));
        }
//    关闭资源
        outputStream.close();
        socket.close();
        inputStream.close();
    }

    public static void main(String[] args) throws Exception {
        //    接收端,绑定端口
        ServerSocket serverSocket = new ServerSocket(9999);
        //等待接受请求,接收后使用socket处理
        Socket socket= serverSocket.accept();
        System.out.println("开始监听端口...");

        //获得连接channel
        InputStream serverInputStream = socket.getInputStream();
        OutputStream serverOutputStream = socket.getOutputStream();

        int len;
        byte[] b1 = new byte[1024];
        while ((len = serverInputStream.read(b1)) != -1) {
            System.out.println("接收到客户端请求了:"+new String(b1,0,len));
            System.out.println("向客户端回复...");
            serverOutputStream.write("你好,我是服务端".getBytes());
            serverOutputStream.flush();
        }
        socket.shutdownOutput();

        socket.close();
        serverInputStream.close();
        serverOutputStream.close();

    }

}


