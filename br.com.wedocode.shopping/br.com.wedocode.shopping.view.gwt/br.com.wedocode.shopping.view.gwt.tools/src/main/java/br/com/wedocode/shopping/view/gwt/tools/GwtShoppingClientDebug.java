package br.com.wedocode.shopping.view.gwt.tools;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.dev.codeserver.CodeServer;
import com.google.gwt.dev.codeserver.Options;
import com.google.gwt.dev.codeserver.WebServer;

import br.com.wedocode.shopping.view.gwt.tools.utils.BuildUtils;

public class GwtShoppingClientDebug {

    private static Logger LOG;

    public static void main(String[] args) throws Exception {
        LOG = LoggerFactory.getLogger(GwtShoppingClientDebug.class);

        var errno = 0;
        WebServer gwtServer = null;
        org.eclipse.jetty.server.Server jettyServer = null;
        try {
            var basedir = new File(BuildUtils.getJarFileFromRepo(GwtShoppingClientDebug.class), "../..")
                    .getCanonicalFile();

            File workdir = new File(basedir, "target/work");
            if (!workdir.exists()) {
                workdir.mkdirs();
            }

            var bindAdress = "127.0.0.1";
            var bindPort = 9876;

        // @formatter:off
		 args = new String[] { 
			"-noincremental",
			"-strict",
			"-noallowMissingSrc",
			"-logLevel",    "INFO",
			"-sourceLevel", "1.11",
			"-workDir",     workdir.getCanonicalPath(), 
			"-bindAddress", bindAdress,
			"-port",        String.valueOf(bindPort),
			"-src",         new File(basedir, "src").getCanonicalPath(),

			"br.com.wedocode.shopping.WeDoCodeShopping"
		};
		// @formatter:on

            Options options = new Options();
            if (!options.parseArgs(args)) {
                errno = 1;
                return;
            }

            gwtServer = CodeServer.start(options);

            {
                final Field field = gwtServer.getClass().getDeclaredField("server");
                field.setAccessible(true);
                jettyServer = (org.eclipse.jetty.server.Server) field.get(gwtServer);
                jettyServer.setStopAtShutdown(true);
            }

            LOG.info("Code Server is ready.");
            LOG.info("Next, visit: http://" + bindAdress + ":" + bindPort + "/");
            LOG.info("Type \"stop\" and click ENTER to gracefully stop Code Server");

            try (var scanner = new Scanner(System.in)) {
                do {
                    final String line = scanner.next();
                    if ("stop".equals(line) || "close".equals(line) || "exit".equals(line)) {
                        break;
                    }
                } while (true);
            }
        } catch (final Throwable cause) {
            LOG.error("Running webserver", cause);
            errno = 2;
        } finally {
            if (gwtServer != null) {
                gwtServer.stop();
            }
            System.exit(errno);
        }
    }

}
