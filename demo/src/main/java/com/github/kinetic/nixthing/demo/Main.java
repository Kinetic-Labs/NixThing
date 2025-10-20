package com.github.kinetic.nixthing.demo;

import com.github.kinetic.nixthing.ast.NixBoolean;
import com.github.kinetic.nixthing.ast.NixExpression;
import com.github.kinetic.nixthing.ast.NixInteger;
import com.github.kinetic.nixthing.ast.NixList;
import com.github.kinetic.nixthing.ast.NixSet;
import com.github.kinetic.nixthing.ast.NixString;
import com.github.kinetic.nixthing.core.Environment;
import com.github.kinetic.nixthing.core.Evaluator;
import com.github.kinetic.nixthing.core.Lexer;
import com.github.kinetic.nixthing.core.Parser;
import com.github.kinetic.nixthing.lang.Lazy;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        runDemo("Complete Demo", "/demo.nix");
        runConfigDemo("Basic Configuration", "/config_demo.nix");
        runConfigDemo("Advanced Configuration", "/advanced_config.nix");
    }

    private static void runDemo(String title, String resourcePath) {
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

            displayResult(evaluatedResult);
        } catch (Exception e) {
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

            if (evaluatedResult instanceof NixSet) {
                NixSet config = (NixSet) evaluatedResult;
                displayConfigValues(config);
            } else {
                displayResult(evaluatedResult);
            }
        } catch (Exception e) {
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

        Optional<NixSet> server = getSet(config, "server");
        server.ifPresent(s -> {
            System.out.println("\nServer Configuration:");
            Optional<String> host = getString(s, "host");
            host.ifPresent(h -> System.out.println("  Host: " + h));
            Optional<Integer> port = getInteger(s, "port");
            port.ifPresent(p -> System.out.println("  Port: " + p));
        });

        System.out.println("-".repeat(50));
    }

    private static void displayResult(NixExpression result) {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("Evaluated Result:");
        System.out.println("-".repeat(50));

        if (result instanceof NixSet) {
            NixSet set = (NixSet) result;
            displaySet(set, "");
        } else if (result instanceof NixString) {
            System.out.println(
                "String: \"" + ((NixString) result).getValue() + "\""
            );
        } else if (result instanceof NixInteger) {
            System.out.println("Number: " + ((NixInteger) result).getValue());
        } else if (result instanceof NixBoolean) {
            System.out.println("Boolean: " + ((NixBoolean) result).getValue());
        } else if (result instanceof NixList) {
            NixList list = (NixList) result;
            System.out.println("List: " + formatList(list));
        } else {
            System.out.println(result);
        }

        System.out.println("-".repeat(50));
    }

    private static void displaySet(NixSet set, String indent) {
        if (set.getEnv() == null) {
            System.out.println(indent + "  (empty set)");
            return;
        }

        Map<String, Lazy> variables = set.getEnv().getVariables();

        variables.forEach((key, lazyValue) -> {
            NixExpression value = lazyValue.getValue();

            if (value instanceof NixString) {
                System.out.println(
                    indent +
                        "  " +
                        key +
                        " = \"" +
                        ((NixString) value).getValue() +
                        "\""
                );
            } else if (value instanceof NixInteger) {
                System.out.println(
                    indent +
                        "  " +
                        key +
                        " = " +
                        ((NixInteger) value).getValue()
                );
            } else if (value instanceof NixBoolean) {
                System.out.println(
                    indent +
                        "  " +
                        key +
                        " = " +
                        ((NixBoolean) value).getValue()
                );
            } else if (value instanceof NixList) {
                System.out.println(
                    indent + "  " + key + " = " + formatList((NixList) value)
                );
            } else if (value instanceof NixSet) {
                System.out.println(indent + "  " + key + " = {");
                displaySet((NixSet) value, indent + "  ");
                System.out.println(indent + "  }");
            } else {
                System.out.println(indent + "  " + key + " = " + value);
            }
        });
    }

    private static String formatList(NixList list) {
        return list
            .getElements()
            .stream()
            .map(e -> {
                if (e instanceof NixString) {
                    return "\"" + ((NixString) e).getValue() + "\"";
                } else if (e instanceof NixInteger) {
                    return String.valueOf(((NixInteger) e).getValue());
                } else if (e instanceof NixBoolean) {
                    return String.valueOf(((NixBoolean) e).getValue());
                }
                return e.toString();
            })
            .collect(Collectors.joining(", ", "[ ", " ]"));
    }

    private static String loadResource(String path) throws Exception {
        try (InputStream is = Main.class.getResourceAsStream(path)) {
            if (is == null) {
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
        if (set.getEnv() == null) {
            return Optional.empty();
        }
        return set.getEnv().lookup(attrName);
    }

    private static Optional<String> getString(NixSet root, String path) {
        return getAttr(root, path)
            .filter(val -> val instanceof NixString)
            .map(val -> ((NixString) val).getValue());
    }

    private static Optional<Integer> getInteger(NixSet root, String path) {
        return getAttr(root, path)
            .filter(val -> val instanceof NixInteger)
            .map(val -> ((NixInteger) val).getValue());
    }

    private static Optional<NixSet> getSet(NixSet root, String path) {
        return getAttr(root, path)
            .filter(val -> val instanceof NixSet)
            .map(val -> (NixSet) val);
    }

    private static Optional<List<String>> getList(NixSet root, String path) {
        return getAttr(root, path)
            .filter(val -> val instanceof NixList)
            .map(val ->
                ((NixList) val).getElements()
                    .stream()
                    .filter(e -> e instanceof NixString)
                    .map(e -> ((NixString) e).getValue())
                    .collect(Collectors.toList())
            );
    }
}
