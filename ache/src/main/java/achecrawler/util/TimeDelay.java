package achecrawler.util;

public class TimeDelay {

    private long lastTimestamp = 0;
    private int minimumTimeInterval;
    
    public TimeDelay(int minimumTimeInterval) {
        this.minimumTimeInterval = minimumTimeInterval;
    }

    public void waitMinimumDelayIfNecesary() {
        if (lastTimestamp == 0) {
            lastTimestamp = System.currentTimeMillis();
            return;
        }

        long elapsedTime = System.currentTimeMillis() - lastTimestamp;
        if (elapsedTime < minimumTimeInterval) {
            System.out.println("Waiting minimum delay: " + elapsedTime);
            long waitTime = minimumTimeInterval - elapsedTime;
            if (waitTime < 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Tread interrupted while waiting.");
                }
            }
        }

        lastTimestamp = System.currentTimeMillis();
    }
}
