package com.github.kinetic.nixthing.demo;

import com.github.kinetic.nixthing.ast.NixExpression;
import com.github.kinetic.nixthing.ast.NixList;
import com.github.kinetic.nixthing.ast.NixSet;
import com.github.kinetic.nixthing.ast.NixString;
import com.github.kinetic.nixthing.core.Environment;
import com.github.kinetic.nixthing.core.Evaluator;
import com.github.kinetic.nixthing.core.Lexer;
import com.github.kinetic.nixthing.core.Parser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        try {
            String input = loadResource("/demo.nix");

            System.out.println("--- Parsing and Evaluating demo.nix ---");

            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer.tokenize());
            NixExpression parsedAst = parser.parse();

            Evaluator evaluator = new Evaluator();
            Environment globalEnv = new Environment(null);
            NixExpression evaluatedResult = evaluator.eval(parsedAst, globalEnv);

            System.out.println("\n--- Data Extraction ---");

            if(evaluatedResult instanceof NixSet) {
                NixSet config = (NixSet) evaluatedResult;

                getString(config, "message").ifPresent(val ->
                        System.out.println("message: " + val)
                );

                getString(config, "other").ifPresent(val ->
                        System.out.println("other: " + val)
                );

                getList(config, "packageList").ifPresent(val ->
                        System.out.println("packageList: " + val)
                );

            } else {
                System.out.println("Evaluation did not result in a set. Got: " + evaluatedResult);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static String loadResource(String path) throws Exception {
        try(InputStream is = Main.class.getResourceAsStream(path)) {
            if(is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Optional<NixExpression> getAttr(NixSet set, String attrName) {
        if(set.getEnv() == null) {
            return Optional.empty();
        }
        return set.getEnv().lookup(attrName);
    }

    private static Optional<String> getString(NixSet root, String path) {
        return getAttr(root, path)
                .map(val -> ((NixString) val).getValue());
    }

    private static Optional<List<String>> getList(NixSet root, String path) {
        return getAttr(root, path)
                .map(val -> ((NixList) val).getElements().stream()
                        .map(e -> ((NixString) e).getValue())
                        .collect(Collectors.toList()));
    }
}