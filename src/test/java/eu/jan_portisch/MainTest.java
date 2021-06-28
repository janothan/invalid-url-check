package eu.jan_portisch;

import org.apache.commons.io.FileUtils;
import org.javatuples.Triplet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {


    @Test
    @Disabled
    void checkLink(){
        assertEquals(Main.UrlStatus.OK, Main.isLinkOk("http://www.jan-portisch.eu"));
    }

    @Test
    void checkProblematicDirMainMd(){
        try {
            String[] args1 = new String[3];
            args1[0] = "-dir";
            args1[1] = loadFile("debug_ioobe").getAbsolutePath();
            args1[2] = "-md";
            int statusCode = catchSystemExit(() -> {
                Main.main(args1);
            });
            assertFalse(statusCode == 0);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void checkSingleFileMainMd(){
        try {
            String[] args1 = new String[3];
            args1[0] = "-dir";
            args1[1] = loadFile("file_no_link.md").getAbsolutePath();
            args1[2] = "-md";
            int statusCode = catchSystemExit(() -> {
                Main.main(args1);
            });
            assertEquals(0, statusCode);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void checkSingleFileMainMd2(){
        try {
            String[] args1 = new String[3];
            args1[0] = "-dir";
            args1[1] = loadFile("error_file.md").getAbsolutePath();
            args1[2] = "-md";
            int statusCode = catchSystemExit(() -> {
                Main.main(args1);
            });
            assertEquals(0, statusCode);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

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
            e.printStackTrace();
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
        Triplet<Boolean, Set<String>, Set<String>> result = Main.isLinkSetOk(linkSet2);
        assertFalse(result.getValue0());
        assertEquals(1, result.getValue1().size());
        assertTrue(result.getValue1().contains("https://www.jan-portisch.eu/asdfasfsadfasfasdfasfds"));
    }

    @Test
    void isLinkOk(){
        assertEquals(Main.UrlStatus.OK, Main.isLinkOk("https://sws.ifi.uio.no/oaei/phenotype/"));
        assertEquals(Main.UrlStatus.ERROR, Main.isLinkOk("https://www.jan-portisch.eu/DOES_NOT_EXIST"));
        assertEquals(Main.UrlStatus.OK, Main.isLinkOk("https://databus.dbpedia.org/dbpedia/collections/latest-core"));
    }

    /**
     * Helper function to load files in class path that contain spaces.
     *
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName) {
        try {
            URL fileUrl = this.getClass().getClassLoader().getResource(fileName);
            File result = FileUtils.toFile(fileUrl);
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (Exception exception) {
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }

}