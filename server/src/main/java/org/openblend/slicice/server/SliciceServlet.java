package org.openblend.slicice.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import org.openblend.slicice.common.Table;
import org.openblend.slicice.common.TableParser;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SliciceServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getParameter("url");
        String username = req.getParameter("username");
        String loc = req.getParameter("loc");
        String top = req.getParameter("top");

        OutputStream out = resp.getOutputStream();

        TableParser parser = new GaeTableParser(new PrintStream(out));
        parser.parse(url);
        if (loc != null) {
            int pages = Integer.parseInt(loc);
            for (int i = 1; i <= pages; i++) {
                parser.parse(url + "?position=" + i);
            }
        }

        out.write("<html>".getBytes());
        out.write(String.format("<title>%s comparison</title>", username).getBytes());
        out.write("<body>".getBytes());
        out.write(String.format("<a href=\"/\">[[HOME]]</a><p/><p/>").getBytes());

        Table user = parser.popTable(username);
        if (user == null) {
            out.write(String.format("No such user <b>%s</b>!!<p/>", username).getBytes());
        } else {
            parser.setOther(user);
            parser.top(top != null ? Integer.parseInt(top) : 20);
        }

        out.write("</body></html>".getBytes());
    }

    private static class GaeTable extends Table {
        public void print(Table other) {
            out.println(String.format("Username: %s<p/>", getUsername()));

            Set<Integer> t_own = new TreeSet<>(other.getOwn());
            t_own.retainAll(getMiss());
            out.println(String.format("I can offer [%s]: %s<p/>", t_own.size(), t_own));

            Set<Integer> t_miss = new TreeSet<>(getOwn());
            t_miss.retainAll(other.getMiss());
            out.println(String.format("I can get [%s]: %s<p/>", t_miss.size(), t_miss));

            out.println("------------------------------------<p/>");
        }
    }

    private static class GaeTableParser extends TableParser {
        private PrintStream rout;

        private GaeTableParser(PrintStream rout) {
            this.rout = rout;
        }

        @Override
        protected InputStream getInputStream(URL url) throws IOException {
            URLFetchService service = URLFetchServiceFactory.getURLFetchService();
            return new ByteArrayInputStream(service.fetch(url).getContent());
        }

        @Override
        protected Table createTable() {
            Table table = new GaeTable();
            table.setOut(rout);
            return table;
        }
    }
}
