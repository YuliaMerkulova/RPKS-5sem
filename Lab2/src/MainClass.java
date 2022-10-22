import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;

import static java.lang.Character.isDigit;
import static java.lang.Character.isSpaceChar;

public class MainClass {

    public static class IncorrectSymbolException extends Exception {
        public IncorrectSymbolException(String err){
            super(err);
        }
    };
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        try {
            System.out.println("Input sum of money");
            int sum;
            sum = in.nextInt();

            System.out.println("Input values in format: num num num");
            in.nextLine();
            String line = in.nextLine();

            String[] valuesStr = line.split(" ");
            HashSet<Integer> valuesSet = new HashSet<Integer>();

            for (String s : valuesStr) {
                valuesSet.add(Integer.parseInt(s));
            }

            Iterator<Integer> it = valuesSet.iterator();
            for (Integer i : valuesSet) {
                System.out.println(i);
            }
            
        }catch (NumberFormatException e){
            System.err.print(e);
        }catch (InputMismatchException er){
                System.err.println("Your input is wrong.");
        }
    }
}
