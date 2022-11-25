import java.io.*;
import java.util.*;

public class MainClass {
    static class CommonObject{
        public ArrayList<String> commonBuffer = new ArrayList<>();
        public Set<Integer> numStrings = new TreeSet<>(); // почему treeSet?
        public String substr;

        int beforeFounded = 0;
        int afterFounded = 0;
        int counter = 0;
    }

    public static final CommonObject obj = new CommonObject();

    public static void main(String[] args) {
        File file = new File("file.txt");
        int before = 0;
        int after = 0;
        try {
            System.out.println("Input str before");
            Scanner in = new Scanner(System.in);
            before = in.nextInt();
            if (before < 0) {
                throw new NumberFormatException("Number is negative");
            }
            System.out.println("Input str after");
            after = in.nextInt();
            if (after < 0)
                throw new NumberFormatException("Number is or negative");
        }
        catch (NumberFormatException | InputMismatchException e)
        {
            e.printStackTrace();
            System.exit(0);
        }
        try {
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);

            ArrayList<String> commonBuffer = new ArrayList<>();

            obj.numStrings = new TreeSet<>();
            obj.substr = "these";
            obj.afterFounded = after;
            obj.beforeFounded = before;

            Worker firstHalfWorker = new Worker(obj);
            Worker secondHalfWorker = new Worker(obj);

            firstHalfWorker.unsetReady();
            firstHalfWorker.setIndexStart(0);
            firstHalfWorker.setIndexFinish(0);

            secondHalfWorker.unsetReady();
            secondHalfWorker.setIndexStart(0);
            secondHalfWorker.setIndexFinish(0);


            Thread firstThread = new Thread(firstHalfWorker);
            Thread secondThread = new Thread(secondHalfWorker);

            firstThread.setName("ONE");
            secondThread.setName("TWO");

            secondThread.start();
            firstThread.start();

            String line;
            firstHalfWorker.setReady();
            secondHalfWorker.setReady();
            obj.counter = 0;
            while (reader.ready()) {
                for(int i = 0; i < 10; i++) {
                    line = reader.readLine();
                    if(line != null) {
                        commonBuffer.add(line);
                    }
                    else {
                        break; // поменять
                    }
                }

                while(true) { //переделать цикл без continue4

                    synchronized (obj) {
                        if ((firstHalfWorker.step != firstHalfWorker.indexFinish) ||
                        (secondHalfWorker.step != secondHalfWorker.indexFinish)){
                            continue;
                        }else {
                            firstHalfWorker.unsetReady();
                            secondHalfWorker.unsetReady();

                            obj.commonBuffer.clear();
                            obj.commonBuffer.addAll(commonBuffer);
                            obj.counter += obj.commonBuffer.size();
                            commonBuffer.clear();

                            firstHalfWorker.setIndexStart(0);
                            firstHalfWorker.setIndexFinish(obj.commonBuffer.size() / 2 + 1);

                            secondHalfWorker.setIndexStart(obj.commonBuffer.size() / 2 + 1);
                            secondHalfWorker.setIndexFinish(obj.commonBuffer.size());

                            firstHalfWorker.setReady();
                            secondHalfWorker.setReady();
                            break;
                        }
                    }
                }

            }

            fr.close();
            firstThread.interrupt();
            secondThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("outfile"));
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String line;
            int counter = 0;
            System.out.println(obj.numStrings);
            while (reader.ready()) {
                line = reader.readLine();
                if (obj.numStrings.contains(counter))
                {
                    writer.write(line + '\n');
                }
                counter++;
                if(line == null) {
                    break;
                }
            }
            fr.close();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
