import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

public class MainClass {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("У вас недостаточно или слишком много параметров!");
            System.exit(0);
        }
        Path path = null;
        try {
            path = Paths.get(args[0]);
        } catch (InvalidPathException | NullPointerException ex) {
            System.out.println("Кажется, путь к директории неверный....");
            System.exit(0);
        }
        long size = 0;
        try (Stream<Path> walker = Files.walk(path)) {
            size = walker.filter(Files::isRegularFile).mapToLong(p -> {
                try {
                    return Files.size(p);
                } catch (IOException e) {
                    System.out.printf("Невозможно получить размер файла %s%n%s", p, e);
                    return 0L;
                }
            }).sum();
        } catch (AccessDeniedException e) {
            System.out.printf("Ошибка доступа %s", e);
        } catch (IOException e) {
            System.out.printf("Ошибка при подсчёте размера директории %s", e);
        }
        System.out.println(path + " ---- " + size + " bytes / " + (size / 1024) / 1024 + " Mb / " +
                ((size / 1024) / 1024) / 1024 + " Gbsize");
    }
}
