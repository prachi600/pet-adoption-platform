package com.petadoption;

import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/** Starts an embedded Tomcat so the project runs with a single command: mvn compile exec:exec */
public class Main {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        File baseDir = new File(System.getProperty("java.io.tmpdir"), "pet-adoption-tomcat");
        baseDir.mkdirs();

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        tomcat.setPort(port);
        tomcat.getConnector();

        Context ctx = tomcat.addWebapp("", new File("src/main/webapp").getAbsolutePath());
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                new File("target/classes").getAbsolutePath(), "/"));
        ctx.setParentClassLoader(Main.class.getClassLoader());
        ctx.setResources(resources);

        tomcat.start();
        System.out.println("Pet Adoption Platform is running at http://localhost:" + port + "/");
        tomcat.getServer().await();
    }
}
