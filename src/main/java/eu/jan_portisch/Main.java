package eu.jan_portisch;

import org.javatuples.Pair;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Main {

    private static final boolean onlyMarkdownLinks = true;

    public static void main(String[] args) {
        // Sanity Check
        if (!isArgsOk(args)) {
            System.exit(1);
            return;
        }
        File root = new File(args[0]);

        // case 1: root is a file
        if (root.isFile()) {
            if (!isFileOk(root)) {
                System.exit(1);
                return;
            }
        }

        // case 2: root is a directory
        if (!isDirectoryOk(root)) {
            System.exit(1);
            return;
        }

        System.exit(0);
    }


    static boolean isDirectoryOk(File directoryOrFile) {
        boolean result = true;
        if (directoryOrFile.isDirectory()) {
            for (File file : directoryOrFile.listFiles()) {
                boolean intermediateResult = isDirectoryOk(file);
                if (!intermediateResult) {
                    result = false;
                }
            }
        } else {
            boolean intermediateResult = isFileOk(directoryOrFile);
            if (!intermediateResult) {
                result = false;
            }
        }
        return result;
    }

    static boolean isFileOk(File file) {
        try {
            Set<String> linkSet = new HashSet<>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            LinkExtractor linkExtractor = LinkExtractor.builder()
                    .linkTypes(EnumSet.of(LinkType.URL, LinkType.WWW))
                    .build();

            // let's crawl all URLs
            while ((line = reader.readLine()) != null) {
                Iterator<LinkSpan> iterator = linkExtractor.extractLinks(line).iterator();
                while (iterator.hasNext()) {
                    LinkSpan link = iterator.next();
                    if (onlyMarkdownLinks) {
                        if (line.substring(link.getBeginIndex() - 1, link.getBeginIndex()).equals("(") && line.substring(link.getEndIndex(), link.getEndIndex() + 1).equals(")")) {
                            linkSet.add(line.substring(link.getBeginIndex(), link.getEndIndex()));
                        }
                    } else {
                        linkSet.add(line.substring(link.getBeginIndex(), link.getEndIndex()));
                    }
                }

            }
            reader.close();
            Pair<Boolean, Set<String>> result = isLinkSetOk(linkSet);
            if (result.getValue0()) {
                System.out.println(file.getAbsolutePath() + "  ✅️");
            } else {
                System.out.println(file.getAbsolutePath() + "  ❌️");
                for (String s : result.getValue1()) {
                    System.out.println("\t" + s);
                }
            }
            return result.getValue0();
        } catch (IOException e) {
            System.out.println("Problem while processing file: " + file.getAbsolutePath());
            e.printStackTrace();
            return false;
        }
    }


    static Pair<Boolean, Set<String>> isLinkSetOk(Set<String> linkSet) {
        boolean result = true;
        Set<String> errorUrls = new HashSet<>();
        for (String link : linkSet) {
            try {
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK
                        && responseCode != HttpURLConnection.HTTP_MOVED_PERM
                        && responseCode != HttpURLConnection.HTTP_MOVED_TEMP
                        && responseCode != HttpURLConnection.HTTP_FORBIDDEN) {
                    errorUrls.add(link);
                    result = false;
                }
            } catch (IOException e) {
                System.out.println("[ERROR] Problem with URL: " + link);
                errorUrls.add(link);
            }
        }
        return new Pair(result, errorUrls);
    }


    /**
     * Just some sanity checks...
     *
     * @param args The main args.
     * @return False if args is not ok, else true.
     */
    private static boolean isArgsOk(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Please provide a directory/file.");
            return false;
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("The provided file/directory does not exist. Please provide an existing directory/file.");
            return false;
        }
        return true;
    }


}
