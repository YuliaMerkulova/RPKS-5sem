import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MainClass {
    public static class Task implements Callable<Set<Integer>> {

        ArrayList<String> buffer = new ArrayList<>();
        String substr;
        int start;
        int before;
        int after;
        Set<Integer> result;

        public Task(ArrayList<String> buffer, String substr, int start, int before, int after) {
            this.buffer.addAll(buffer);
            this.substr = substr;
            this.start = start;
            this.before = before;
            this.after = after;
            result = new HashSet<>();
        }

        @Override
        public Set<Integer> call() {
            for (int i = 0; i < buffer.size(); i++) {
                if (this.buffer.get(i).toLowerCase().contains(this.substr.toLowerCase())) {
                    for (int j = start + i - before; j <= start + i + after; j++) {
                        result.add(j);
                    }
                }
            }
            return result;
        }
    }
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        ArrayList<Future<Set<Integer>>> futureArrayList = new ArrayList<>();
        File file = new File("file.txt");
        int before = 0;
        int after = 0;
        String substr = "";
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
            System.out.println("Input substr");
            in.nextLine();
            substr = in.nextLine();
            in.close();
        }
        catch (NumberFormatException | InputMismatchException e)
        {
            e.printStackTrace();
            System.exit(0);
        }
        try {
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            int start = 0;
            ArrayList<String> commonBuffer = new ArrayList<>();
            while (reader.ready()) {
                for(int i = 0; i < 10; i++) {
                    String line = reader.readLine();
                    if(line != null) {
                        commonBuffer.add(line);
                    }
                }
                futureArrayList.add(executorService.submit(new Task(commonBuffer, substr, start, before, after)));
                commonBuffer.clear();
                start += 10;
            }
            fr.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("outfile"));
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String line;
            int counter = 0;
            for (Future<Set<Integer>> setFuture : futureArrayList) {
                setFuture.get();
            }
            SortedSet<Integer> resultSet = new TreeSet<>();
            for (Future<Set<Integer>> setFuture : futureArrayList) {
                resultSet.addAll(setFuture.get());
            }
            while (reader.ready()) {
                line = reader.readLine();
                if (resultSet.contains(counter))
                {
                    writer.write(line + '\n');
                }
                counter++;
            }
            fr.close();
            writer.close();
            reader.close();
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();
    }
}