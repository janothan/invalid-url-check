package eu.jan_portisch;

import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {


    @Test
    void isFileOk() {
        assertTrue(Main.isFileOk(loadFile("root.md")));
    }

    @Test
    void mainOk(){
        String[] args1 = new String[1];
        args1[0] = loadFile("myOkRootDir").getAbsolutePath();
        Main.main(args1);
    }

    @Test
    void mainNotOk(){
        String[] args1 = new String[1];
        args1[0] = loadFile("myOkRootDirNotOk").getAbsolutePath();
        Main.main(args1);
    }

    @Test
    void isLinkSetOk(){
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