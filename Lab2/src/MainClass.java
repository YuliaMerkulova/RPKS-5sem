import java.util.*;

public class MainClass {

    public static boolean exchangeMoney(int value, Integer[] valuesSet, int i, ArrayList<Integer> output)
    {
        int tmpVal = value;
        if (i != -1) {
            tmpVal -= valuesSet[i];
            if (tmpVal == 0) {
                return true;
            }
            if (tmpVal < 0) {
                return false;
            }
        }
        else {
            i += 1;
        }

        for (int j = i; j < valuesSet.length; j++) {
            if(exchangeMoney(tmpVal, valuesSet, j, output)) {
                output.add(valuesSet[j]);
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Integer setVal[] = {2, 1};
        ArrayList<Integer> outArray = new ArrayList<>();
        System.out.println("test: " + exchangeMoney(10, setVal, -1, setVal.length, outArray));
        for (Integer value : outArray) {
            System.out.println(value);
        }
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
