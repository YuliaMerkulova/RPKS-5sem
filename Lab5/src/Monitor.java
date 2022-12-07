import java.util.concurrent.atomic.AtomicLong;

public class Monitor implements Runnable {
    private final long length;
    public AtomicLong doneLinesCount = new AtomicLong(0);

    public Monitor (Long fileLength) {
        length = fileLength;
    }

    public void run() {
        double startTime = (double)System.nanoTime() / 1000000;
        long localLinesCount = 0;
        while (localLinesCount != length) {
            if ((localLinesCount != doneLinesCount.get())) {
                localLinesCount = doneLinesCount.get();
                System.out.format("Работает [мс]: %f. Проверено записей: %d. Осталось: %d\n",
                        (double)System.nanoTime() / 1000000 - startTime, localLinesCount, length - localLinesCount);
            }
        }
    }
}