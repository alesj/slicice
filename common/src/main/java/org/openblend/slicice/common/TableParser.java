package org.openblend.slicice.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TableParser {
    private final static String USERNAME_TAG = "<p class=\"userName\">";
    private final static String TABLE_TAG = "<div class=\"offerTable\">";
    private final static String CELL_TAG = "<span class=\"offerCell  ([a-z]*)\">([0-9]+)</span>";
    private final static Pattern CELL_REGEXP = Pattern.compile(CELL_TAG);

    private Table other;
    private final Map<String, Table> tables = new HashMap<>();

    protected Table createTable() {
        return new Table();
    }

    public Table popTable(String username) {
        return tables.remove(username);
    }

    public void setOther(Table other) {
        this.other = other;
    }

    public void top(int n) {
        if (other == null) {
            throw new IllegalStateException("Missingh other table to compare!");
        }

        List<Table> values = new ArrayList<>(tables.values());
        for (Table t : values) {
            t.calculate(other);
            t.reset();
            t.doSwitch();
        }

        Collections.sort(values);
        Iterator<Table> iter = values.iterator();
        while (n > 0) {
            if (iter.hasNext()) {
                Table next = iter.next();
                if (next.match()) {
                    next.print(other);
                    n--;
                }
            } else {
                break;
            }
        }
    }

    public static Table parse(File file) throws IOException {
        Table table = new Table();
        try (FileInputStream in = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(in);
            String own = properties.getProperty("own");
            table.getOwn().addAll(parseLine(own));
            String miss = properties.getProperty("miss");
            table.getMiss().addAll(parseLine(miss));
        }
        return table;
    }

    protected InputStream getInputStream(URL url) throws IOException {
        return url.openStream();
    }

    public void parse(String url) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream inputStream = getInputStream(new URL(url))) {
            while (true) {
                int ch = inputStream.read();
                if (ch < 0) break;
                baos.write(ch);
            }
        }
        String content = new String(baos.toByteArray());

        int p = -1;
        while (true) {
            p = content.indexOf(USERNAME_TAG, p + 1);
            if (p < 0) break;

            Table table = createTable();

            p += USERNAME_TAG.length() + 1;

            while (content.charAt(p) != '>') p++;
            int start = p + 1;
            p = start;
            while (content.charAt(p) != '<') p++;
            table.setUsername(content.substring(start, p));

            p = content.indexOf(TABLE_TAG, p);
            int end = content.indexOf("</div>", p);

            parse(content.substring(p + TABLE_TAG.length(), end), table);
            tables.put(table.getUsername(), table);
        }
    }

    private void parse(String html, Table table) {
        Matcher matcher = CELL_REGEXP.matcher(html);
        while (matcher.find()) {
            int number = Integer.parseInt(matcher.group(2));
            String type = matcher.group(1);
            if ("missing".equals(type)) {
                table.getMiss().add(number);
            } else if ("duplicate".equals(type)) {
                table.getOwn().add(number);
            }
        }
    }

    private static Set<Integer> parseLine(String line) {
        String[] split = line.split(",");
        Set<Integer> set = new TreeSet<>();
        for (String s : split) {
            set.add(Integer.parseInt(s));
        }
        return set;
    }
}
