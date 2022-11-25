import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class MainClass {

    public static void main(String[] args){
//        if (args.length != 1)
//        {
//            System.out.println("У вас недостаточно или слишком много параметров!");
//            System.exit(0);
//        }
        Path path = null;
        try{
            path = Paths.get("./src");
        }
        catch (InvalidPathException | NullPointerException ex)
        {
            System.out.println("Кажется, путь к директории неверный....");
            System.exit(0);
        }
        long size = 0;
        try (Stream<Path> walk = Files.walk(path)) {
            size = walk.filter(Files::isRegularFile).mapToLong(p -> {
                try {
                    return Files.size(p);
                } catch (IOException e) {
                    System.out.printf("Невозможно получить размер файла %s%n%s", p, e);
                    return 0L;
                }
            }).sum();
        } catch (IOException e) {
            System.out.printf("Ошибка при подсчёте размера директории %s", e);
        }
        System.out.println(path +  " ---- " + size +  " bytes / " + (size /1024)/1024 + " Mb / " +
                ((size /1024)/1024)/1024 +  " Gbsize");
    }
}
