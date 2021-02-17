import java.util.concurrent.DelayQueue;

public class Main {

    private static DelayQueue<DelayQueueMessage> delayQueue = new DelayQueue<DelayQueueMessage>();

    public static void main(String[] args) throws Exception{
        delayQueue.add(new DelayQueueMessage("我延时两秒", 5000));
        delayQueue.add(new DelayQueueMessage("我延时一秒", 1000));
        new Runnable(){
            @Override
            public void run() {
                int i = 0;
                while(i < 2) {
                    try {
                        i++;
                        DelayQueueMessage take = delayQueue.take();
                        System.out.println(take.getBody());
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }.run();
        Thread.sleep(3000);
    }
}
