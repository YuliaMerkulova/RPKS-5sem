import java.io.*;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
public class MainClass {
    static class CommonObject{
        public File file = new File("out.txt");
        public ArrayList<String> commonBuffer = new ArrayList<>();
        public SortedSet<Integer> numStrings = new TreeSet<>();
        public String substr;

        int beforeFounded = 2;
        int afterFounded = 3;
        int counter = 0;
    }
//6, 14, 15, 16
    public void writeStrings(CommonObject commonObj) {
        try(FileWriter writer = new FileWriter(commonObj.file))
            {
                for(Integer i: commonObj.numStrings){
                    if (i < commonObj.commonBuffer.size()){
                        writer.write(commonObj.commonBuffer.get(i));
                        commonObj.numStrings.remove(i);
                    }
                }
                writer.flush();
            }
            catch(IOException ex){

                System.out.println(ex.getMessage());
            }
    }

    public static CommonObject obj = new CommonObject();

    public static void main(String[] args) {
        File file = new File("file.txt");
        try {
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);

            ArrayList<String> commonBuffer = new ArrayList<>();

            obj.numStrings = new TreeSet<>();
            obj.substr = "these";

            Thread firstThread;
            Thread secondThread;

            Worker firstHalfWorker = new Worker(obj);
            Worker secondHalfWorker = new Worker(obj);

            firstHalfWorker.setNeed();
            firstHalfWorker.unsetReady();
            firstHalfWorker.setIndexStart(0);
            firstHalfWorker.setIndexFinish(0);

            secondHalfWorker.setNeed();
            secondHalfWorker.unsetReady();
            secondHalfWorker.setIndexStart(0);
            secondHalfWorker.setIndexFinish(0);


            secondThread = new Thread(secondHalfWorker);
            firstThread = new Thread(firstHalfWorker);
            firstThread.setName("ONE");

            secondThread.setName("TWO");
            secondThread.start();
            firstThread.start();

            String line;
            firstHalfWorker.setReady();
            secondHalfWorker.setReady();
            obj.counter = 0;
            while (reader.ready()) {
                for(int i = 0; i < 3; i++) {
                    line = reader.readLine();
                    System.out.println(line);
                    if(line != null) {
                        commonBuffer.add(line);
                        //currStep++;
                        System.out.println("line add");
                    }
                    else {
                        break;
                    }
                }

                while(true) {
                    synchronized (obj) {
                        if ((firstHalfWorker.step != firstHalfWorker.indexFinish) ||
                        (secondHalfWorker.step != secondHalfWorker.indexFinish)){
                            continue;
                        }
                        firstHalfWorker.unsetReady();
                        secondHalfWorker.unsetReady();
                        System.out.println("I KILL UOI");
                        obj.commonBuffer.clear();
                        obj.commonBuffer.addAll(commonBuffer);
                        obj.counter += obj.commonBuffer.size();
                        commonBuffer.clear();
                        System.out.println("SIZE" + obj.commonBuffer.size());
                        int start1 = 0;
                        int finish1 = obj.commonBuffer.size() / 2 + 1;

                        int finish2 = obj.commonBuffer.size();
                        firstHalfWorker.setIndexStart(start1);
                        firstHalfWorker.setIndexFinish(finish1);
                        secondHalfWorker.setIndexStart(finish1);
                        secondHalfWorker.setIndexFinish(finish2);

                        System.out.println("buff update");

                        firstHalfWorker.setReady();
                        secondHalfWorker.setReady();
                        break;
                    }
                }

            }
            firstHalfWorker.unsetNeed();
            secondHalfWorker.unsetNeed();
            fr.close();
            firstThread.interrupt();
            secondThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String line;
            int counter = 0;
            System.out.println(obj.numStrings);
            while (reader.ready()) {
                System.out.println("SIZE" + obj.numStrings.size() + "counter" + counter);
                line = reader.readLine();
                if (obj.numStrings.contains(counter))
                {
                    System.out.println(line);
                }
                counter++;
                if(line == null) {
                        break;
                }
            }
            fr.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
