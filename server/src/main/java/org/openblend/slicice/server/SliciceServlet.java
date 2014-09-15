package org.openblend.slicice.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

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

        TableParser parser = new GaeTableParser(new PrintStream(resp.getOutputStream()));
        parser.parse(url);
        if (loc != null) {
            int pages = Integer.parseInt(loc);
            for (int i = 1; i <= pages; i++) {
                parser.parse(url + "?position=" + i);
            }
        }

        Table user = parser.popTable(username);
        parser.setOther(user);

        parser.top(top != null ? Integer.parseInt(top) : 20);
    }

    private static class GaeTable extends Table {

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
            GaeTable table = new GaeTable();
            table.setOut(rout);
            return table;
        }
    }
}
