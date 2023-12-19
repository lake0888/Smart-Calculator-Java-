package calculator;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // put your code here
        executeCalculator();
        System.out.println("Bye!");
    }

    public static void executeCalculator() {
        String regex = "[+-]?\\s*\\S+\\s*([*\\/^]\\s*[a-zA-Z0-9()]+\\s*|[+-]+\\s*\\S+\\s*)*";
        Pattern expression = Pattern.compile(regex);

        Pattern assignment = Pattern.compile("\\s*\\S+\\s*=\\s*\\S+\\s*");

        Map<String, BigInteger> map = new HashMap<>();

        String line = "";
        while (!line.equals("/exit")) {
            line = scanner.nextLine();

            Matcher matcherExpression = expression.matcher(line);
            Matcher matcherAssignment = assignment.matcher(line);
            if (line.equals("/help")) {
                System.out.println("The program calculates the addition(+), subtraction(-), multiplication(*), division(/) and pow(^) of numbers");
            } else if (line.startsWith("/") && !line.equals("/exit")) {
                System.out.println("Unknown command");
            } else {
                if (!line.isEmpty() && !line.equals("/exit")) {
                    if (matcherAssignment.matches()) {
                        assignValue(map, line);
                    } else if (!matcherExpression.matches() || !areTheyBalanced(line)) {
                        System.out.println("Invalid expression");
                    } else {
                        line = line.replaceAll("\\s+", "")
                                .replaceAll("-{2}", "+")
                                .replaceAll("\\++", "+")
                                .replaceAll("(\\+-)|(-\\+)", "-");

                        BigInteger result = calculate(map, line);
                        System.out.printf("%d", result);
                    }
                }
            }
        }
    }

    public static BigInteger calculate(Map<String, BigInteger> map, String line) {
        Deque<String> dequeNumbers = new ArrayDeque<>();
        dequeNumbers.push("0");
        Deque<String> dequeOperators = new ArrayDeque<>();
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < line.length(); i++) {
            String c = String.valueOf(line.charAt(i));
            //CHECK IF IS AN OPERATOR
            if (c.matches("[\\(\\)^\\/*+-]")) {
                int currentPriority = getPriority(c);
                if ((dequeOperators.isEmpty() || c.equals("(") || dequeOperators.peek().equals("(") ||
                        currentPriority > getPriority(dequeOperators.peek())) && !c.equals(")")) {
                    dequeOperators.push(c);
                } else {
                    if (c.equals(")")) {
                        while (!dequeOperators.peek().equals("(")) {
                            result = calculate(map, dequeNumbers, dequeOperators);
                        }
                        //REMOVE "("
                        dequeOperators.pop();
                    } else {
                        result = calculate(map, dequeNumbers, dequeOperators);
                        dequeOperators.push(c);
                    }
                }
            } else { //OTHERWISE IS A NUMBER
                String value = c;
                int j = i + 1;
                while (j < line.length()) {
                    String ch = String.valueOf(line.charAt(j));
                    if (!ch.matches("[\\(\\)^\\/*+-]")) {
                        value += ch;
                        j++;
                    } else break;
                }
                i = j - 1;
                dequeNumbers.push(value);
            }
        }
        while (!dequeOperators.isEmpty()) {
            result = calculate(map, dequeNumbers, dequeOperators);
        }

        if (isNotValid(map, dequeNumbers.peek())) {
            System.out.println("Unknown variable");
            return BigInteger.ZERO;
        }
        return dequeOperators.isEmpty() && dequeNumbers.peek() != null
                ? map.containsKey(dequeNumbers.peek())
                    ? map.get(dequeNumbers.peek())
                    : new BigInteger(dequeNumbers.peek())
                : BigInteger.ZERO;
    }

    public static BigInteger calculate(Map<String, BigInteger> map, Deque<String> dequeNumbers, Deque<String> dequeOperators) {
        String operatorInHead = dequeOperators.pop();
        String b = dequeNumbers.pop();
        String a = dequeNumbers.pop();
        if (isNotValid(map, a) || isNotValid(map, b)) {
            System.out.println("Unknown variable");
            return BigInteger.ZERO;
        }
        BigInteger value_a = map.containsKey(a) ? map.get(a) : new BigInteger(a);
        BigInteger value_b = map.containsKey(b) ? map.get(b) : new BigInteger(b);
        BigInteger result = calculate(operatorInHead.charAt(0), value_a, value_b);
        dequeNumbers.push(String.valueOf(result));
        return result;
    }

    public static BigInteger calculate(char operator, BigInteger a, BigInteger b) {
        BigInteger result = BigInteger.ZERO;
        switch (operator) {
            case '+' -> result = addition(a, b);
            case '-' -> result = subtraction(a, b);
            case '*' -> result = multiplication(a, b);
            case '/' -> result = division(a, b);
            case '^' -> result = pow(a, b.intValue());
        }
        return result;
    }

    public static BigInteger addition(BigInteger a, BigInteger b) { return a.add(b); }

    public static BigInteger subtraction(BigInteger a, BigInteger b) { return a.subtract(b); }

    public static BigInteger multiplication(BigInteger a, BigInteger b) {
        return a.multiply(b);
    }

    public static BigInteger division(BigInteger a, BigInteger b) {
        if (b == BigInteger.ZERO) throw new ArithmeticException("B can not be 0");
        return a.divide(b);
    }

    public static BigInteger pow(BigInteger a, int b) {
        return (BigInteger) a.pow(b);
    }

    public static int getPriority(String operator) {
        if (operator.equals("+") || operator.equals("-")) return 0;
        else if (operator.equals("*") || operator.equals("/")) return 1;
        else if (operator.equals("^")) return 2;
        return 3;
    }

    public static void assignValue(Map<String, BigInteger> map, String line) {
        String[] assignExpression = line.replaceAll("\\s+", "").split("=");
        String identifier = assignExpression[0];
        String value = assignExpression[1];
        if (!isValidIdentifier(identifier)) {
            System.out.println("Invalid identifier");
        } else if (isValidIdentifier(value) && !map.containsKey(value)) {
            System.out.println("Unknown variable");
        } else if (!map.containsKey(value) && !isValueValid(value)) {
            System.out.println("Invalid assignment");
        } else {
            map.put(identifier, (map.containsKey(value)) ? map.get(value) : new BigInteger(value));
        }
    }

    public static boolean isValidIdentifier(String identifier) {
        return identifier.matches("[a-zA-Z]+");
    }

    public static boolean isValueValid(String value) {
        return value.matches("[+-]?\\d+");
    }

    public static boolean areTheyBalanced(String line) {
        line = line.replaceAll("\\s+", "");
        Deque<Character> stack = new ArrayDeque<>();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '{' || c == '(' || c == '[') {
                stack.offerLast(c);
            } else if (c == '}' || c == ')' || c == ']') {
                if (!stack.isEmpty()) {
                    char last = stack.peekLast();
                    char next = '-';
                    switch (last) {
                        case '{' -> next = '}';
                        case '(' -> next = ')';
                        case '[' -> next = ']';
                    }
                    if (c != next) return false;
                    stack.pollLast();
                } else return false;
            }
        }
        return stack.isEmpty();
    }

    public static boolean isNotValid(Map<String, BigInteger> map, String value) {
        return !isValueValid(value) && !map.containsKey(value);
    }
}
