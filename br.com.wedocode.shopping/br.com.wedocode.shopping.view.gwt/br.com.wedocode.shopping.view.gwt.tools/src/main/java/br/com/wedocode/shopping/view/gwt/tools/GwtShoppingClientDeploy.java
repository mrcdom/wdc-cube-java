package br.com.wedocode.shopping.view.gwt.tools;

import java.io.File;
import java.io.IOException;

import com.google.gwt.dev.CompileTaskRunner;
import com.google.gwt.dev.CompileTaskRunner.CompileTask;
import com.google.gwt.dev.Compiler;
import com.google.gwt.dev.CompilerOptionsImpl;
import com.google.gwt.dev.javac.UnitCacheSingleton;
import com.google.gwt.dev.util.Memory;
import com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger;

import br.com.wedocode.shopping.view.gwt.tools.utils.BuildUtils;

public class GwtShoppingClientDeploy {

    public static void main_(String[] args) {
        com.google.gwt.dev.Compiler.main(args);
    }

    public static void main(String[] args) throws Exception {
        var errno = 0;
        try {
            errno = run();
        } finally {
            System.exit(errno);
        }
    }

    @SuppressWarnings("deprecation")
    public static int run() throws Exception, IOException {
        var errno = 0;
        var basedir = new File(BuildUtils.getJarFileFromRepo(GwtShoppingClientDeploy.class), "../..")
                .getCanonicalFile();
        var biServerDir = BuildUtils.getServerWarFolder();
        var webAppDir = new File(biServerDir, "src/main/webapp");

        Memory.initialize();
        if (System.getProperty("gwt.jjs.dumpAst") != null) {
            System.out.println("Will dump AST to: " + System.getProperty("gwt.jjs.dumpAst"));
        }

        SpeedTracerLogger.init();

        var moduleName = "br.com.wedocode.shopping.WeDoCodeShopping";

        final var options = new CompilerOptionsImpl();
        {
            options.setWorkDir(new File(basedir, "target/gwt-work"));
            options.setGenDir(new File(basedir, "target/gwt-generated"));
            options.setLogLevel(com.google.gwt.core.ext.TreeLogger.Type.INFO);
            options.setOutput(com.google.gwt.dev.jjs.JsOutputOption.OBFUSCATED);
            options.setSourceLevel(com.google.gwt.dev.util.arg.SourceLevel.JAVA11);
            options.setOptimizationLevel(9);
            options.setWarDir(webAppDir);

            options.addModuleName(moduleName);
        }

        if (options.getWorkDir() != null && !options.getWorkDir().exists()) {
            options.getWorkDir().mkdirs();
        }

        if (options.getGenDir() != null && !options.getGenDir().exists()) {
            options.getGenDir().mkdirs();
        }

        if (options.getWarDir() != null && !options.getWarDir().exists()) {
            options.getWarDir().mkdirs();
        }

        var task = (CompileTask) (logger) -> {
            var persistentUnitCacheDir = new File(options.getGenDir(), "../");
            UnitCacheSingleton.get(logger, null, persistentUnitCacheDir, options);
            return Compiler.compile(logger, options);
        };

        if (CompileTaskRunner.runWithAppropriateLogger(options, task)) {
            System.out.println(moduleName + " compiled succefully");
            errno = 0;
        } else {
            System.out.println(moduleName + " compilation failed");
            errno = 1;
        }
        return errno;
    }

}
