package eu.jan_portisch;

import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {


    @Test
    void isFileOk() {
        assertTrue(Main.isFileOk(loadFile("root.md")));
    }

    @Test
    void help() {
        String[] args1 = new String[2];

        try {
            int statusCode = catchSystemExit(() -> {
                Main.main(args1);
            });
            assertEquals(1, statusCode);
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    void mainOk() {
        try {
            String[] args1 = new String[2];
            args1[0] = "-dir";
            args1[1] = loadFile("myOkRootDir").getAbsolutePath();
            int statusCode = catchSystemExit(() -> {
                Main.main(args1);
            });
            assertEquals(0, statusCode);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void mainNotOkAll() {
        String[] args1 = new String[2];
        args1[0] = "-dir";
        args1[1] = loadFile("myNotOkRootDir").getAbsolutePath();
        try {
            int statusCode = catchSystemExit(() -> {
                Main.main(args1);
            });
            assertEquals(1, statusCode);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void mainNotOkButMdOk() {
        String[] args1 = new String[3];
        args1[0] = "-dir";
        args1[1] = loadFile("myNotOkRootDir").getAbsolutePath();
        args1[2] = "-md";
        try {
            int statusCode = catchSystemExit(() -> {
                Main.main(args1);
            });
            assertEquals(0, statusCode);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void isLinkSetOk() {
        Set<String> linkSet1 = new HashSet<>();
        linkSet1.add("https://www.jan-portisch.eu/");
        linkSet1.add("https://www.wikidata.org/");
        assertTrue(Main.isLinkSetOk(linkSet1).getValue0());

        Set<String> linkSet2 = new HashSet<>();
        linkSet2.add("https://www.jan-portisch.eu/");
        linkSet2.add("https://www.jan-portisch.eu/asdfasfsadfasfasdfasfds");
        Pair<Boolean, Set<String>> result = Main.isLinkSetOk(linkSet2);
        assertFalse(result.getValue0());
        assertEquals(1, result.getValue1().size());
        assertTrue(result.getValue1().contains("https://www.jan-portisch.eu/asdfasfsadfasfasdfasfds"));
    }

    /**
     * Helper function to load files in class path that contain spaces.
     *
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName) {
        try {
            File result = FileUtils.toFile(this.getClass().getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception) {
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }

}