package tools;

public class Timer implements Runnable {
    private final int time = 3;
    Cs cs;

    /**
     * 计时器的简单实现，持续获取当前的的curAck，休眠一段时间后再次获取curAck，若两次相同，则调用timeHandler()
     * 
     * @param cs
     */
    public Timer(Cs cs) {
        this.cs = cs;
    }

    @Override
    public void run() {
        while (true) {
            int ack = cs.getCurAck();
            try {
                Thread.sleep(time * 1000);
            } catch (InterruptedException e) {
                break;
            }
            if (ack == cs.getCurAck()) {
                cs.timeouthandler();
            }
        }
    }

}
