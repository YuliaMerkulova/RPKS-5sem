import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainClass {

    public static void main(String[] args)
    {
        String filename;
        String param;
        String regex = "\\d*[smhd]";
        Long timeParam = 0L;
        try(Scanner in = new Scanner(System.in)){
            System.out.println("Введите название файла");
            filename = in.nextLine();
            System.out.println("Введите отклонение в формате число+(s/m/h/d) или, если не хотите введите no");
            param = in.nextLine();

            if (param.matches(regex)) {
                System.out.println("Окей, ваш параметр принят");
                if (param.endsWith("s")){
                    timeParam = Long.parseLong(param.substring(0, param.lastIndexOf("s")));
                } else if (param.endsWith("m")) {
                    timeParam = Long.parseLong(param.substring(0, param.lastIndexOf("m"))) * 60;
                } else if (param.endsWith("h")) {
                    timeParam = Long.parseLong(param.substring(0, param.lastIndexOf("h"))) * 3600;
                } else if (param.endsWith("d")) {
                    timeParam = Long.parseLong(param.substring(0, param.lastIndexOf("d"))) * 24 * 3600;
                }
            }
            else if (param.equals("no")) {
                System.out.println("Будет задан стандартный параметр 3s");
                timeParam = 3L;
            } else {
                System.out.println("Ваш параметр задан неверно");
                return;
            }
        }
        catch (Exception ex){
            System.err.println("Упс....");
            ex.printStackTrace();
            return;
        }
        File file = new File(filename);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            int start = 0;
            HashMap<Integer, LocalDateTime>  startDateMap = new HashMap<>();
            HashMap<Integer, Long> deltaQueryTime = new HashMap<>();
            Long countLines = 0L;
            try(FileReader fr_ = new FileReader(file);
                BufferedReader reader_ = new BufferedReader(fr_);) {
                while (reader_.ready()) {
                    String line = reader_.readLine();
                    countLines++;
                }
            }
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            Monitor myMonitor = new Monitor(countLines);
            Thread MonitorThread = new Thread(myMonitor);
            MonitorThread.start();
            while (reader.ready()) {
                String line = reader.readLine();
                myMonitor.doneLinesCount.incrementAndGet();
                String dateLine = line.substring(0, 19);
                String idLine = line.substring(line.lastIndexOf('=') + 2);
                String resOrQuery = line.substring(line.lastIndexOf('-') + 2, line.lastIndexOf('-') + 3);
                if (resOrQuery.equals("Q")) {
                    startDateMap.put(Integer.parseInt(idLine), LocalDateTime.parse(dateLine, dtf));
                }
                if (resOrQuery.equals("R"))
                {
                    deltaQueryTime.put(Integer.parseInt(idLine),
                            ChronoUnit.SECONDS.between(startDateMap.get(Integer.parseInt(idLine)), LocalDateTime.parse(dateLine,dtf)));
                }
            }
            MonitorThread.interrupt();
            fr.close();
            reader.close();
//            System.out.println(startDateMap);
            System.out.println(deltaQueryTime);
            ArrayList<Long> deltaValues = new ArrayList<>(deltaQueryTime.values());
            Collections.sort(deltaValues);
//            System.out.println(deltaValues);
            Long median = 0L;
            if (deltaValues.size() % 2 == 0){
                median = (deltaValues.get(deltaValues.size() / 2 - 1) + deltaValues.get(deltaValues.size() / 2)) / 2;
            }
            else {
                median = deltaValues.get(deltaValues.size() / 2);
            }
//            System.out.println(median);
            for(Map.Entry<Integer, Long> entry : deltaQueryTime.entrySet()) {
                Integer key = entry.getKey();
                Long value = entry.getValue();
                if (value >= median + timeParam){
                    System.out.println("ANOMALY QUERY FOR ID = " + key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
