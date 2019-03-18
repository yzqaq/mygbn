package gbn;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import tools.Cs;
import tools.Timer;

public class GbnClient implements Cs {
    // 端口
    private final int serverPort = 12340;
    private final int clientPort = 12341;
    private DatagramSocket datagramSocket;
    InetAddress inetAddress;
    private final int windowSize = 4;
    /*
     * 当前数据包的 seq
     */
    private int curSeq;
    /*
     * 当前等待确认的 ack
     */
    private int curAck;
    /*
     * 收到的包的总数
     */
    private int totalSeq = 10;

    /*
     * 需要发送的包总数
     */
    private int nextSeq;

    public GbnClient() {

    }

    @Override
    public void timeouthandler() {
        System.out.println("ack:" + curAck + "等待超时");
        for (int i = curAck; i < curSeq; i++) {
            // 构造数据
            String clientData = i + "    这是数据";
            byte[] data = clientData.getBytes();
            // 打包成数据包
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, serverPort);
            // 发送数据包
            try {
                System.out.println("客户端正在重传数据：" + i);
                datagramSocket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        System.out.println();

    }

    @Override
    public int getCurAck() {
        return curAck;
    }

    /**
     * 客户端开始运行
     * 
     * @throws Exception
     */
    public void start() throws Exception {
        datagramSocket = new DatagramSocket(clientPort);
        inetAddress = InetAddress.getLocalHost();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("请输入数字：\n0.退出1.得到时间\n2.接收数据3.发送数据");
            switch (scanner.nextInt()) {
            case 0:
                System.out.println("客户端终止。");
                scanner.close();
                return;
            case 1:
                getServerTime();
                break;
            case 2:
                getData();
                break;
            case 3:
                sendData();
                break;
            }
        }
    }

    /**
     * 获取服务器时间
     * 
     * @throws IOException
     */
    public void getServerTime() throws IOException {
        byte[] byt = "time".getBytes();
        DatagramPacket packet = new DatagramPacket(byt, byt.length, inetAddress, serverPort);
        datagramSocket.send(packet);
        byt = new byte[1024];
        packet = new DatagramPacket(byt, byt.length);
        datagramSocket.receive(packet);
        System.out.println("获得当前服务器时间：" + new String(byt));
    }

    /**
     * 从服务器下文件
     * 
     * @throws IOException
     */
    public void getData() throws IOException {
        // 发送“send”指令，要求服务器发送数据
        byte[] bytout = "send".getBytes();
        // 打包
        DatagramPacket packetout = new DatagramPacket(bytout, bytout.length, inetAddress, serverPort);
        datagramSocket.send(packetout);
        // 接收数据的包
        byte[] bytin = new byte[2048];
        DatagramPacket packetin = new DatagramPacket(bytin, bytin.length);
        nextSeq = 0;
        while (true) {
            datagramSocket.receive(packetin);
            String recived = new String(bytin);
            // System.out.println(recived);
            int recivednum = 0;
            try {
                recivednum = Integer.parseInt(recived.trim().substring(0, 3).trim());
                System.out.println("客户端收到数据：" + recivednum);
                // 包装成ack
                String s = recivednum + "";
                // 判断是否所需的包,不是就舍弃掉

                if (recivednum == nextSeq) {
                    bytout = s.getBytes();
                    nextSeq++;
                } else {
                    s = (nextSeq - 1) + "";
                    bytout = s.getBytes();
                }
                packetout = new DatagramPacket(bytout, bytout.length, inetAddress, serverPort);
                datagramSocket.send(packetout);
                System.out.println("发送ack: " + s);
                System.out.println();
                // 如果数据发完
                if (recivednum == totalSeq - 1) {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("eroe");
            }

        }
    }

    /**
     * 向服务器发送数据
     * 
     * @throws IOException
     */
    public void sendData() throws IOException {
        // 发送指令，要求服务器接受数据
        byte[] bytout = "recive".getBytes();
        // 打包
        DatagramPacket packetout = new DatagramPacket(bytout, bytout.length, inetAddress, serverPort);
        datagramSocket.send(packetout);
        // 接收数据的包
        nextSeq = 0;
        curAck = 0;
        curSeq = 0;
        Thread timer = new Thread(new Timer(this));
        timer.start();
        while (true) {
            while (curSeq - curAck < windowSize && curSeq < totalSeq) {
                // 不发3
                if (curSeq != 3) {
                    // 构造数据
                    String clientData = curSeq + "    这是数据";
                    byte[] data = clientData.getBytes();
                    // 打包成数据包
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, serverPort);
                    // 发送数据包
                    datagramSocket.send(datagramPacket);
                    // 当前seq++
                }

                System.out.println("向服务器发送的数据:" + curSeq);
                curSeq++;
            }
            System.out.println();
            // 收ACK模块
            byte[] bytes = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
            datagramSocket.receive(datagramPacket);
            String ackdata = new String(bytes, 0, bytes.length).trim();
            try {
                int ack = Integer.parseInt(ackdata);
                curAck = ack >= curAck ? ack + 1 : curAck;
                System.out.println("客户端收到ack： " + ack);
            } catch (NumberFormatException e) {
                System.out.println("error");
            }
            // 包发完了退出
            if (curAck == totalSeq) {
                // 结束计时
                timer.interrupt();
                break;
            }
        }
        // System.out.println("服务器端收到ack：" + curAck);
        System.out.println("客户端发送文件完毕");
    }

    public static void main(String[] args) {
        try {
            new GbnClient().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
