package tools;

public interface Cs {
    /**
     * 处理timeout
     */
    public void timeouthandler();

    /**
     * 获取当前ack，用于计时器
     * 
     * @return
     */
    public int getCurAck();

}
