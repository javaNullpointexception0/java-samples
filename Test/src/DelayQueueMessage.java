import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayQueueMessage implements Delayed {
    private String body; // 消息内容
    private long executeTime; //执行的时间点

    public DelayQueueMessage(String body, long delay) {
        this.body =body;
        this.executeTime = System.currentTimeMillis() + delay;
    }

    //延迟任务是否到时就是按照这个方法判断如果返回的是负数则说明到期否则还没到期
    @Override
    public long getDelay(TimeUnit unit) {
        return executeTime - System.currentTimeMillis();
    }

    @Override
    public int compareTo(Delayed o) {
        return 0;
    }

    public String getBody() {
        return this.body;
    }
}
