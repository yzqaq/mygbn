package mygbn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import gbn.GServer;

public class Server {
    private final int serverPort = 12340;
    private final String serverIp = "0.0.0.0";
    private final int bufLength = 1026;
    // 发送窗口大小为10，GBN中应满足W + 1 <= N（W为发送窗口大小，N为序列号个数）
    // 若设为1,则是停等协议
    private final int sendWindowSize = 3;
    // 序列号个数
    private final int seqSize = 20;
    /*
     * 收到的ack情况
     */
    private boolean[] ack = new boolean[seqSize];
    private long[] time = new long[seqSize];
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
    private int totalSeq;
    /*
     * 需要发送的包总数
     */
    private int totalPacket;
    ServerSocket serverSocket = null;
    Socket socket = null;
    InputStream in = null;
    OutputStream out = null;

    public Server() {
        try {
            serverSocket = new ServerSocket(serverPort);
            socket = serverSocket.accept();
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("服务器已连接客户端");
        byte[] byt = new byte[1024];
        while (true) {
            try {
                in.read(byt);
                String s = new String(byt).trim();
                if (s.equals("time")) {
                    out.write(new Date().toString().getBytes());
                    out.flush();
                } else if (s.equals("start")) {
                    try {
                        new GServer();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 当前序列好curSeg是否可用
     * 
     * @return
     */
    public boolean seqIsAvailable() {
        int step;
        step = curSeq - curAck;
        step = step >= 0 ? step : step + seqSize;
        // 序列号是否在当前发送窗口之内
        if (step >= sendWindowSize) {
            return false;
        }
        if (ack[curSeq]) {
            return true;
        }
        return true;

    }

    /**
     * 超时重传处理函数，滑动窗口内的数据帧都要重传
     */
    public void timeoutHandler() {
        System.out.println("Timer out error");
        int index;
        for (int i = 0; i < sendWindowSize; ++i) {
            index = (i + curAck) % seqSize;
            ack[index] = true;
        }
        totalSeq -= sendWindowSize;
        curSeq = curAck;
    }

    /**
     * 收到 ack ，累积确认，取数据帧的第一个字节 由于发送数据时，第一个字节（序列号）为 0 （ ASCII ）时发送失败，因此加一 了，此处需要减一还原
     * 
     * @param c
     */
    public void ackHandler(char c) {

    }

    public void output(boolean loss) {

        while (true) {
            byte[] bytes = new byte[1024];
            try {
                if (curSeq - curAck > sendWindowSize) {
                    // 表示丢包
                    if (false) {
                        String temp = curSeq + "   这是数据" + curSeq;
                        out.write(temp.getBytes());

                    }
                    time[curSeq] = System.currentTimeMillis();
                    System.out.println("向客户端发送数据：" + curSeq);
                    curSeq++;
                } else {
                    System.out.println(" 正在等待ack" + curAck);
                }
                if (in.available() != 0) {
                    in.read(bytes);
                    String s = new String(bytes, 0, bytes.length).trim();
                    if (s.equals("stop")) {
                        break;
                    }
                    int ac = Integer.parseInt(s);
                    if (ac >= curAck) {
                        System.out.println("获得ack:" + ac);
                        for (int i = 0; i <= ac; i++) {
                            time[ac] = 0;
                        }
                        ack[ac] = true;
                        curAck = ac;
                    }
                }
                for (int i = 0; i < curSeq; i++) {
                    if (time[i] != 0 && System.currentTimeMillis() - time[i] > 3000) {
                        System.out.println("ack" + i + "超时");
                        timeoutHandler();
                    }
                }
                if (curAck == 19) {
                    System.out.println("数据传输完毕");
                    break;
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        new Server();
    }
}
