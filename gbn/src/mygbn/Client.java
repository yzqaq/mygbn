package mygbn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import gbn.GClient;

public class Client {
    private final int serverPort = 12340;
    private final String serverIp = "127.0.0.1";
    Socket socket = null;
    InputStream in = null;
    OutputStream out = null;

    public Client() {
        try {
            socket = new Socket(serverIp, serverPort);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到服务器时间
     */
    public void getServerTime() {
        try {
            out.write("time".getBytes());
            byte[] byt = new byte[1024];
            in.read(byt);
            System.out.println(new String(byt));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    public void getData() {

        try {
            out.write("start".getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // byte[] byt = new byte[1024];
        // while (true) {
        // try {
        // in.read(byt);
        // String s = new String(byt).trim();
        // Scanner scanner = new Scanner(s);
        // Integer ack = scanner.nextInt();
        // System.out.println("客户端收到：包" + ack);
        // out.write(ack.toString().getBytes());
        // out.flush();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        try {
            new GClient();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("请输入数字：\n0.退出1.得到时间\n2.接收数据3.发送数据");
            switch (scanner.nextInt()) {
            case 0:
                System.out.println("客户端终止。");
                scanner.close();
                return;
            case 1:
                client.getServerTime();
                break;
            case 2:
                client.getData();
                break;

            }

        }

    }

}
