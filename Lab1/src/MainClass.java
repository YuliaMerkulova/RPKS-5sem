import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;

public class MainClass {

    static class Bracket{
        public Character left;
        public Character right;
        public Bracket(Character left, Character right){
            this.left = left;
            this.right = right;
        }
        public String toString() {
            return "Bracket [ left: " + left + ", right: " + right + " ]";
        }
    }

    public static void readMyJson(String jsonFile, ArrayList<Bracket> brackets) throws FileNotFoundException, JsonSyntaxException, MalformedJsonException {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Bracket>>(){}.getType();
        //brackets.addAll(gson.fromJson(new FileReader(jsonFile), listType)); // как закрыть json

        try (FileReader fr = new FileReader(jsonFile))
        {
            brackets.addAll(gson.fromJson(fr, listType));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static int checkText(String textFile, HashMap<Character, Character> mapBrackets) throws IOException  {
        Deque<Character> checkCharStack = new ArrayDeque<>();
        //Stack<Character> checkCharStack = new Stack<>(); // какой класс вместо стека?
        FileReader reader = new FileReader(textFile);
        int c;
        int position = -1;
        while((c = reader.read()) != -1){
            Character num = (char)c;
            position++;
            if (mapBrackets.containsValue(num)) //если открывающая
            {
                checkCharStack.addLast(num);
            }
            if (mapBrackets.containsKey(num)) //если закрывающая
            {
                if (!checkCharStack.isEmpty()) {
                    if (checkCharStack.peekLast() == mapBrackets.get(num)) { //парная
                        checkCharStack.removeLast();
                    } else
                        return position;
                }
                else return position;
            }

        }
        reader.close();
        if (!checkCharStack.isEmpty()) {
            return position;
        }
        return -1;
    }

    public static void main(String[] args){

        if (args.length != 2)
        {
            System.out.println("Not two arguments");
            System.exit(0);
        }

        ArrayList<Bracket> brackets = new ArrayList<>();
        try{
            readMyJson(args[0], brackets);
        } catch (FileNotFoundException e) {
            System.out.println("Can't found json file");
        }
        catch (JsonSyntaxException | MalformedJsonException e)
        {
            e.printStackTrace();
        }

        HashMap<Character, Character> mapBrackets = new HashMap<>();
        for (Bracket bracket : brackets) {
            mapBrackets.put(bracket.right, bracket.left);
        }

        try {
            int res = checkText(args[1], mapBrackets);
            if (res != -1)
                System.out.println("Error at pose" + res);
            else
                System.out.println("No errors");
        } catch (FileNotFoundException e)
        {
            System.out.println("File not found");
        }
        catch (IOException e) {
            System.out.println("Reading file error");
        }
    }
}