import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length != 4) {
                System.err.println("Укажите необходимые файлы!");
                return;
        }

        List<File> inputFiles = new ArrayList<>();
        for (int i = 0; i < 4; i++)
            inputFiles.add(new File(args[i]));

        DBEngine dataBaseEngine = new DBEngine(inputFiles);
        System.out.println();
        dataBaseEngine.run("SELECT STUDENTS.FIO, MARKS.id, Marks.mark FROM MARKS, STUDENTS WHERE MARKS.mark = 555");
        }
    }

