package com.github.kinetic.nixthing.demo;

import com.github.kinetic.nixthing.ast.*;
import com.github.kinetic.nixthing.core.enviornment.Environment;
import com.github.kinetic.nixthing.core.eval.Evaluator;
import com.github.kinetic.nixthing.core.lexer.Lexer;
import com.github.kinetic.nixthing.core.parser.Parser;
import com.github.kinetic.nixthing.lang.Lazy;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"DuplicatedCode", "CallToPrintStackTrace"})
public class Main {

    public static void main(String[] args) {
        runDemo();
        runConfigDemo("Basic Configuration", "/config_demo.nix");
        runConfigDemo("Advanced Configuration", "/advanced_config.nix");
    }

    private static void runDemo() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Complete Demo");
        System.out.println("=".repeat(50));

        try {
            String input = loadResource("/demo.nix");

            System.out.println("\nInput:");
            System.out.println(indent(input));

            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer.tokenize());
            NixExpression parsedAst = parser.parse();

            System.out.println("\nParsing successful");

            Evaluator evaluator = new Evaluator();
            Environment globalEnv = new Environment(null);
            NixExpression evaluatedResult = evaluator.eval(
                    parsedAst,
                    globalEnv
            );

            System.out.println("Evaluation successful");

            displayResult(evaluatedResult);
        } catch(Exception e) {
            System.out.println("\nError: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runConfigDemo(String title, String resourcePath) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(title);
        System.out.println("=".repeat(50));

        try {
            String input = loadResource(resourcePath);

            System.out.println("\nInput:");
            System.out.println(indent(input));

            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer.tokenize());
            NixExpression parsedAst = parser.parse();

            System.out.println("\nParsing successful");

            Evaluator evaluator = new Evaluator();
            Environment globalEnv = new Environment(null);
            NixExpression evaluatedResult = evaluator.eval(
                    parsedAst,
                    globalEnv
            );

            System.out.println("Evaluation successful");

            if(evaluatedResult instanceof NixSet config) {
                displayConfigValues(config);
            } else {
                displayResult(evaluatedResult);
            }
        } catch(Exception e) {
            System.out.println("\nError: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void displayConfigValues(NixSet config) {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("Configuration Values:");
        System.out.println("-".repeat(50));

        Optional<String> version = getString(config, "version");
        version.ifPresent(v -> System.out.println("Version: " + v));

        Optional<String> message = getString(config, "message");
        message.ifPresent(m -> System.out.println("Message: " + m));

        Optional<NixSet> server = getSet(config);
        server.ifPresent(s -> {
            System.out.println("\nServer Configuration:");
            Optional<String> host = getString(s, "host");
            host.ifPresent(h -> System.out.println("  Host: " + h));
            Optional<Integer> port = getInteger(s);
            port.ifPresent(p -> System.out.println("  Port: " + p));
        });

        System.out.println("-".repeat(50));
    }

    private static void displayResult(NixExpression result) {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("Evaluated Result:");
        System.out.println("-".repeat(50));

        switch(result) {
            case NixSet set -> displaySet(set, "");
            case NixString nixString -> System.out.println(
                    "String: \"" + nixString.getValue() + "\""
            );
            case NixInteger nixInteger -> System.out.println("Number: " + nixInteger.getValue());
            case NixBoolean nixBoolean -> System.out.println("Boolean: " + nixBoolean.getValue());
            case NixList list -> System.out.println("List: " + formatList(list));
            case null, default -> System.out.println(result);
        }

        System.out.println("-".repeat(50));
    }

    private static void displaySet(NixSet set, String indent) {
        if(set.getEnv() == null) {
            System.out.println(indent + "  (empty set)");
            return;
        }

        Map<String, Lazy> variables = set.getEnv().getVariables();

        variables.forEach((key, lazyValue) -> {
            NixExpression value = lazyValue.getValue();

            switch(value) {
                case NixString nixString -> System.out.println(
                        indent +
                                "  " +
                                key +
                                " = \"" +
                                nixString.getValue() +
                                "\""
                );
                case NixInteger nixInteger -> System.out.println(
                        indent +
                                "  " +
                                key +
                                " = " +
                                nixInteger.getValue()
                );
                case NixBoolean nixBoolean -> System.out.println(
                        indent +
                                "  " +
                                key +
                                " = " +
                                nixBoolean.getValue()
                );
                case NixList nixList -> System.out.println(
                        indent + "  " + key + " = " + formatList(nixList)
                );
                case NixSet nixSet -> {
                    System.out.println(indent + "  " + key + " = {");
                    displaySet(nixSet, indent + "  ");
                    System.out.println(indent + "  }");
                }
                case null, default -> System.out.println(indent + "  " + key + " = " + value);
            }
        });
    }

    private static String formatList(NixList list) {
        return list
                .getElements()
                .stream()
                .map(e -> {
                    if(e instanceof NixString) {
                        return "\"" + ((NixString) e).getValue() + "\"";
                    } else if(e instanceof NixInteger) {
                        return String.valueOf(((NixInteger) e).getValue());
                    } else if(e instanceof NixBoolean) {
                        return String.valueOf(((NixBoolean) e).getValue());
                    }
                    return e.toString();
                })
                .collect(Collectors.joining(", ", "[ ", " ]"));
    }

    private static String loadResource(String path) throws Exception {
        try(InputStream is = Main.class.getResourceAsStream(path)) {
            if(is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String indent(String text) {
        return text
                .lines()
                .map(line -> "  " + line)
                .collect(Collectors.joining("\n"));
    }

    private static Optional<NixExpression> getAttr(
            NixSet set,
            String attrName
    ) {
        if(set.getEnv() == null) {
            return Optional.empty();
        }
        return set.getEnv().lookup(attrName);
    }

    private static Optional<String> getString(NixSet root, String path) {
        return getAttr(root, path)
                .filter(val -> val instanceof NixString)
                .map(val -> ((NixString) val).getValue());
    }

    private static Optional<Integer> getInteger(NixSet root) {
        return getAttr(root, "port")
                .filter(val -> val instanceof NixInteger)
                .map(val -> ((NixInteger) val).getValue());
    }

    private static Optional<NixSet> getSet(NixSet root) {
        return getAttr(root, "server")
                .filter(val -> val instanceof NixSet)
                .map(val -> (NixSet) val);
    }
}
