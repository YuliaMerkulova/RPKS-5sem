import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;

import java.util.HashMap;
import java.util.Stack;
//lallallala

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

    static class Symbol{
        public int position;
        public Character num;
        public Symbol(int pos, Character num){
            this.position = pos;
            this.num = num;
        }
    }

    public static void readMyJson(String jsonFile, ArrayList<Bracket> brackets) throws FileNotFoundException, JsonSyntaxException, MalformedJsonException {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Bracket>>(){}.getType();
        brackets.addAll(gson.fromJson(new FileReader(jsonFile), listType));
    }

    public static Symbol checkText(String textFile, HashMap<Character, Character> mapBrackets) throws IOException  {
        Stack<Symbol> checkCharStack = new Stack<>();
        FileReader reader = new FileReader(textFile);
        int c;
        int position = -1;
        Character myChar = '0';
        while((c = reader.read()) != -1){
            Character num = (char)c;
            position++;
            if (mapBrackets.containsValue(num))
            {
                checkCharStack.add(new Symbol(position, num));
            }
            if (mapBrackets.containsKey(num))
            {
                if (!checkCharStack.empty()) {
                    if (checkCharStack.peek().num.equals(mapBrackets.get(num))) {
                        checkCharStack.pop();
                    } else
                        return new Symbol(position, num);
                }
                else return new Symbol(position, num);
            }

        }
        if (!checkCharStack.empty()) {
            return new Symbol(position, myChar);
        }
        return new Symbol(-1, myChar);
    }

    public static void main(String[] args) throws IOException {
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
            System.out.println(bracket);
            mapBrackets.put(bracket.right, bracket.left);
        }

        try {
            Symbol res = checkText(args[1], mapBrackets);
            if (res.position != -1)
                System.out.println("Error at pose" + res.position);
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