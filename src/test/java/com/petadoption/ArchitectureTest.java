package com.petadoption;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/**
 * Guards the layered architecture:  JSP / Servlet  →  Service  →  DAO  →  Database.
 * The tests read the source files and fail if a layer reaches past its neighbour.
 */
public class ArchitectureTest {
    private static final Path JAVA = Paths.get("src/main/java/com/petadoption");
    private static final Path VIEWS = Paths.get("src/main/webapp/WEB-INF/views");

    private static List<Path> files(Path dir, String suffix) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(p -> p.toString().endsWith(suffix)).collect(Collectors.toList());
        }
    }

    private static String read(Path p) throws IOException {
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    /** Returns the files in the package folder whose source matches the regex. */
    private static List<String> offenders(String pkg, String suffix, String regex) throws IOException {
        Pattern pattern = Pattern.compile(regex);
        List<String> bad = new ArrayList<>();
        for (Path p : files(pkg.isEmpty() ? VIEWS : JAVA.resolve(pkg), suffix)) {
            if (pattern.matcher(read(p)).find()) {
                bad.add(p.getFileName().toString());
            }
        }
        return bad;
    }

    @Test
    public void servlets_doNotImportDaos() throws IOException {
        List<String> bad = offenders("servlet", ".java", "import\\s+com\\.petadoption\\.dao\\.");
        assertTrue("Servlets must call Services, not DAOs: " + bad, bad.isEmpty());
    }

    @Test
    public void servlets_containNoSqlOrJdbc() throws IOException {
        List<String> bad = offenders("servlet", ".java", "java\\.sql|PreparedStatement|DBConnection|executeQuery");
        assertTrue("No JDBC code allowed in Servlets: " + bad, bad.isEmpty());
    }

    @Test
    public void servlets_doNotValidateInput_servicesDo() throws IOException {
        List<String> bad = offenders("servlet", ".java", "import\\s+com\\.petadoption\\.util\\.Validator");
        assertTrue("Validation belongs in the Service layer: " + bad, bad.isEmpty());
    }

    @Test
    public void services_doNotDependOnServletsOrWebApi() throws IOException {
        List<String> bad = offenders("service", ".java", "com\\.petadoption\\.servlet|javax\\.servlet");
        assertTrue("Services must not know about the web layer: " + bad, bad.isEmpty());
    }

    @Test
    public void daos_doNotDependOnServicesOrServlets() throws IOException {
        List<String> bad = offenders("dao", ".java", "com\\.petadoption\\.service|com\\.petadoption\\.servlet|javax\\.servlet");
        assertTrue("DAOs must only talk to the database: " + bad, bad.isEmpty());
    }

    @Test
    public void jsps_haveNoScriptletsOrSqlTags() throws IOException {
        // directives such as <%@ page %> are fine; <% code %> and <sql:...> are not
        List<String> bad = offenders("", ".jsp", "<%(?!@|--)|<sql:");
        assertTrue("JSPs must only render data (JSTL/EL): " + bad, bad.isEmpty());
    }
}
