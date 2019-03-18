package sr;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import tools.Cs;

public class SrClient implements Cs {
    private final int serverPort = 12340;
    private final int clientPort = 12341;
    private DatagramSocket datagramSocket;
    private InetAddress inetAddress;
    /*
     * 窗口大小
     */
    private final int windowSize = 4;
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
    private boolean[] SeqInWin = new boolean[windowSize];

    /**
     * 客户端开始
     * 
     * @throws Exception
     */
    public void start() throws Exception {
        inetAddress = InetAddress.getLocalHost();
        datagramSocket = new DatagramSocket(clientPort);
        reciveData();
    }

    /**
     * 收文件
     * 
     * @throws Exception
     */
    public void reciveData() throws Exception {
        byte[] bytout = null;
        // 打包
        DatagramPacket packetout = null;
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
                bytout = s.getBytes();
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

    @Override
    public void timeouthandler() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getCurAck() {
        // TODO Auto-generated method stub
        return 0;
    }

    public static void main(String[] args) {
        try {
            new SrClient().start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
