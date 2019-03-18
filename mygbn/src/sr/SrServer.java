package sr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import tools.Cs;
import tools.Timer;

public class SrServer implements Cs {
    private final int serverPort = 12340;
    private final int clientPort = 12341;
    private DatagramSocket datagramSocket;
    private InetAddress inetAddress;
    /*
     * 窗口大小
     */
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
    /*
     * 当前窗口 ack的收到情况
     */
    private boolean[] ackInWin = new boolean[windowSize];

    /**
     * @throws Exception
     * 
     */
    public void start() throws Exception {
        inetAddress = InetAddress.getLocalHost();
        datagramSocket = new DatagramSocket(serverPort);
        sendData();

    }

    /**
     * 向客户端发送数据
     * 
     * @throws IOException
     */
    public void sendData() throws IOException {
        curAck = 0;
        curSeq = 0;
        Thread timer = new Thread(new Timer(this));
        timer.start();
        while (true) {
            while (curSeq - curAck < windowSize && curSeq < totalSeq) {
                // 不发3
                if (curSeq != 3 && curSeq != 5) {
                    // 构造数据
                    String clientData = curSeq + "    这是数据";
                    byte[] data = clientData.getBytes();
                    // 打包成数据包
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, clientPort);
                    // 发送数据包
                    datagramSocket.send(datagramPacket);
                    // 当前seq++
                }

                System.out.println("向客户端发送的数据:" + curSeq);
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
                System.out.println("服务器端收到ack： " + ack);
                int step = ack - curAck;
                // 标记收到ack
                if (step < windowSize && step >= 0) {
                    ackInWin[step] = true;
                    // 窗口移动
                    moveAckInWin();
                }
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
    }

    /**
     * 移动ack窗口
     */
    private void moveAckInWin() {
        int i = 0;
        while (i < windowSize && ackInWin[i]) {
            i++;
        }
        if (i != 0) {
            curAck += i;
            // 将数组左移i位，后面添上false
            for (int j = 0; j < windowSize - i; j++) {
                ackInWin[j] = ackInWin[j + i];
            }
            for (int j = windowSize - i; j < windowSize; j++) {
                ackInWin[j] = false;
            }
        }
    }

    @Override
    public void timeouthandler() {
        System.out.println("ack:" + curAck + "等待超时");
        for (int i = curAck; i < curSeq; i++) {
            if (!ackInWin[i - curAck]) {
                // 构造数据
                String clientData = i + "    这是数据";
                byte[] data = clientData.getBytes();
                // 打包成数据包
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, clientPort);
                // 发送数据包
                try {
                    System.out.println("服务器正在重传数据：" + i);
                    datagramSocket.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println();

    }

    @Override
    public int getCurAck() {
        return curAck;
    }

    public static void main(String[] args) {
        try {
            new SrServer().start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
