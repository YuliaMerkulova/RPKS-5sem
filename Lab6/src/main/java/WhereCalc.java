
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WhereCalc {
    private final Map<String, Integer> signsAndValues = new HashMap<>();
    public WhereCalc() {
        signsAndValues.put("AND", 2);
        signsAndValues.put("OR", 1);
    }
    private List<String> polishNotation = null;
    private String expression;
    public void setExpression(String expression) {
        if (expression == null)
            return;
        this.expression = expression;

        String[] separatedWithSpaces = expression.split("((?<=^)|(?<=(\\s)))(?=(\\w+\\.\\w+\\s*=\\s*(('[^']" +
                "+')|(\\d+))))|(?<=('|\\d))(?=\\s+(AND|OR)\\s+(\\w+\\.\\w+\\s*=\\s*(('[^']+')|(\\d+))))|(?<=\\s)(?=(AND|" +
                "OR)\\s+(\\w+\\.\\w+\\s*=\\s*(('[^']+')|(\\d+))))|((?<=AND)|(?<=OR))(?=\\s+(\\w+\\.\\w+\\s*=\\s*(('[^']" +
                "+')|(\\d+))))");
//        for (String str : separatedWithSpaces)
//        {
//            System.out.println(str);
//        }
        //System.out.println(Arrays.toString(separatedWithSpaces));
        polishNotation = toPolishNotation(deleteSpaces(separatedWithSpaces));
//        for (String str : polishNotation)
//        {
//            System.out.println("HELLO" + str);
//        }
        deleteSpacesInPolishNotation();
    }
    private List<String> deleteSpaces(String[] separatedExpression) {
        //System.out.println(Arrays.stream(separatedExpression).filter(string -> string.charAt(0) != ' ').collect(Collectors.toList()));
        return Arrays.stream(separatedExpression).filter(string -> string.charAt(0) != ' ').collect(Collectors.toList());
    }
    private List<String> toPolishNotation(List<String> expression) {
        ArrayDeque<String> stackForSigns = new ArrayDeque<>();
        List<String> polishNotation = new ArrayList<>();
        for (String part : expression)
        {
            part = part.toUpperCase();
            if (isSign(part)) {
                while (!stackForSigns.isEmpty() && firstSignIsNotSmaller(stackForSigns.peek(), part))
                    polishNotation.add(stackForSigns.pop());
                stackForSigns.push(part);
            } else
                polishNotation.add(part);
        }

        while (!stackForSigns.isEmpty())
            polishNotation.add(stackForSigns.pop());
        return polishNotation;
    }
    private boolean isSign(String expression) {
        return signsAndValues.containsKey(expression);
    }
    private boolean firstSignIsNotSmaller(String peekSign, String currentSign) {
        return signsAndValues.get(peekSign) >= signsAndValues.get(currentSign);
    }
    private void deleteSpacesInPolishNotation() {
        polishNotation = polishNotation.stream().map((polishNotationElement) -> {
            Matcher matcher = Pattern.compile("\\s*(\\w+\\.\\w+)\\s*=\\s*(('[^']+')|(\\d+))\\s*").matcher(polishNotationElement);
            return matcher.replaceAll("$1=$2");
        }).collect(Collectors.toList());
    }
    public int countExpression(Map<String, Integer> variablesValues) {
        ArrayDeque<Integer> values = new ArrayDeque<>();
        for (String part : polishNotation)
        {
            part = part.toUpperCase();
            if (isSign(part))
            {
                if (values.size() < 2)
                    throw new RuntimeException("Некорректное выражение: " + expression);
                Integer secondDigit = values.pop();
                Integer firstDigit = values.pop();
                values.push(countOperation(firstDigit, secondDigit, part));
            } else {
                if (!variablesValues.containsKey(part))
                    throw new RuntimeException("Неизвестное значение: " + part);
                Integer variableValue = variablesValues.get(part);
                values.push(variableValue);
            }
        }
        return values.pop();
    }
    private Integer countOperation(Integer firstDigit, Integer secondDigit, String sign) {
        Integer answer = null;
        switch (sign) {
            case "AND":
                answer = firstDigit * secondDigit;
                break;
            case "OR":
                answer = firstDigit + secondDigit;
                break;
        }
        return answer;
    }
}