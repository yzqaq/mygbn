package gbn;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * 服务端
 */
public class GServer {
    private final int port = 8080;
    private DatagramSocket datagramSocket = new DatagramSocket();
    private DatagramPacket datagramPacket;
    private InetAddress inetAddress;
    private Model model;
    private Timer timer;
    private int nextSeq = 1;
    private int base = 1;
    private int N = 5;

    public GServer() throws Exception {
        model = new Model();
        timer = new Timer(this, model);
        model.setTime(0);
        timer.start();
        while (true) { // 向客户端端发送数据
            sendData(); // 从客户端端接受ACK
            byte[] bytes = new byte[4096];
            datagramPacket = new DatagramPacket(bytes, bytes.length);
            datagramSocket.receive(datagramPacket);
            String fromServer = new String(bytes, 0, bytes.length);
            int ack = Integer.parseInt(fromServer.substring(fromServer.indexOf("ack:") + 4).trim());
            base = ack + 1;
            if (base == nextSeq) { // 停止计时器
                model.setTime(0);
            } else { // 开始计时器
                model.setTime(3);
            }
            System.out.println("从客户端获得的数据:" + fromServer);
            System.out.println("\n");
        }
    }

    public static void main(String[] args) throws Exception {

        new GServer();
    }

    /**
     * 向客户端发送数据
     *
     * @throws Exception
     */
    private void sendData() throws Exception {
        inetAddress = InetAddress.getLocalHost();
        while (nextSeq < base + N && nextSeq <= 10) { // 不发编号为3的数据，模拟数据丢失
            if (nextSeq == 6) {
                System.out.println("向客户端发送的数据:" + nextSeq);
                nextSeq++;
                continue;
            }
            String clientData = "服务端发送的数据编号:" + nextSeq;
            System.out.println("向客户端发送的数据:" + nextSeq);
            byte[] data = clientData.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, port);
            datagramSocket.send(datagramPacket);
            if (nextSeq == base) { // 开始计时
                model.setTime(3);
            }
            nextSeq++;
        }
    }

    /**
     * 超时数据重传
     */
    public void timeOut() throws Exception {
        for (int i = base; i < nextSeq; i++) {
            String clientData = "服务端重新发送的数据编号:" + i;
            // System.out.println("ack " + i + " 接收超时");
            System.out.println("向客户端重新发送的数据:" + i);
            byte[] data = clientData.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, port);
            datagramSocket.send(datagramPacket);
        }
    }
}
