import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

public class MainClass {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        try {
            System.out.println("Input sum of money");
            Integer sum = Integer.valueOf(in.next());
            System.out.println("Input values in format: num num num");
            String values = in.nextLine();
            HashSet<Integer> valuesSet = new HashSet<Integer>();
        }catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
    }
}
