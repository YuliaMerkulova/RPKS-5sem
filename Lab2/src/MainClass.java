import java.util.*;

public class MainClass {

    public static boolean exchangeMoney(int value, Integer[] valuesSet, int i, ArrayList<Integer> output)
    {
        if (value == 0)
            return true;
        if (value < 0)
            return false;
        for (int j = i; j < valuesSet.length; j++)
        {
            if (exchangeMoney(value - valuesSet[j], valuesSet, j, output))
            {
                output.add(valuesSet[j]);
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        try {
            System.out.println("Input sum of money");
            int sum;
            sum = in.nextInt();
            if (sum <= 0)
                throw new NumberFormatException("Number is 0 or negative, try again");

            System.out.println("Input values in format: num num num");
            in.nextLine();
            String line = in.nextLine();

            String[] valuesStr = line.split(" ");
            Integer[] valuesArr = new Integer[valuesStr.length];
            HashMap<Integer, Integer> resMap = new HashMap<>(valuesArr.length);

            for (int i = 0; i < valuesArr.length; i++) {
                valuesArr[i] = Integer.parseInt(valuesStr[i]);
                if (valuesArr[i] <= 0)
                    throw new NumberFormatException("Number is 0 or negative, try again");
                resMap.put(Integer.parseInt(valuesStr[i]), 0);
            }

            ArrayList<Integer> outArray = new ArrayList<>();
            if (exchangeMoney(sum, valuesArr, 0, outArray)) {
                for (Integer integer : outArray) {
                    resMap.put(integer, resMap.get(integer) + 1);
                }
                System.out.print(sum + "-> ");
                for (Integer integer : valuesArr) {
                    System.out.print(integer + "[" + resMap.get(integer) + "] ");
                }
            }
            else System.out.println("Can't do it");

        }catch (NumberFormatException e){
            System.err.print(e);
        }catch (InputMismatchException er){
                System.err.println("Your input is wrong.");
        }
    }
}
