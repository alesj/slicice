package org.openblend.slicice.tools;

import java.io.File;

import org.openblend.slicice.common.Table;
import org.openblend.slicice.common.TableParser;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Main {
    private static final String URL = "http://slicice.net/albumi/24/fifa-world-cup-brasil-2014.html";

    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("Missing args.");
        }

        final Table me;
        final TableParser parser = new TableParser();

        parser.parse(URL);
        if (args.length > 2) {
            int pages = Integer.parseInt(args[2]);
            for (int i = 1; i <= pages; i++) {
                parser.parse(URL + "?position=" + i);
            }
        }

        final String type = args[0];
        switch (type) {
            case "-u":
                String username = args[1];
                me = parser.popTable(username);
                if (me == null) {
                    throw new IllegalArgumentException("No such user: " + username);
                }
                break;
            case "-f":
                File input = new File(args[1]);
                if (input.exists() == false) {
                    throw new IllegalArgumentException("No such input file: " + input);
                }

                me = TableParser.parse(input);
                break;
            case "-m":
                for (String user : args[1].split(",")) {
                    Table other = parser.popTable(user);
                    if (other == null) continue;
                    execute(parser, other);
                }
                return;
            default:
                throw new IllegalArgumentException("No such type: " + type);
        }

        execute(parser, me);
    }

    private static void execute(TableParser parser, Table other) {
        System.out.println("\n -- Username: " + other.getUsername() + "\n");
        parser.setOther(other);
        parser.top(20);
    }
}
