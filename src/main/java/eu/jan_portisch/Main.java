package eu.jan_portisch;

import org.apache.commons.cli.*;
import org.javatuples.Triplet;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;


import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;


public class Main {


    /**
     * If true only md files are parsed and only md links are checked.
     */
    private static boolean isOnlyMarkdownLinks = false;

    /**
     * The amount of time to wait before the link again (in seconds).
     */
    private static final int RETRY_TIME_SECONDS = 3;

    /**
     * The maximum number of retries.
     */
    private static final int MAX_RETRIES = 1;

    /**
     * If true, the console output will be more extensive.
     */
    private static boolean debugOutput = false;

    public static void main(String[] args) {
        Options options = new Options();
        Option dirOption = new Option("dir", "directory", true, "The directory or file that shall be checked.");
        dirOption.setOptionalArg(false);
        Option mdOption = new Option("md", "markdown", false, "Only markdown files will be parsed and only markdown links will be checked.");
        mdOption.setOptionalArg(true);
        Option excludeOption = new Option("ex", "exclude", true, "Exclude a specific directory.");
        excludeOption.setOptionalArg(true);
        options.addOption(dirOption);
        options.addOption(mdOption);
        options.addOption(excludeOption);

        String directoryPath;
        String exclusion = "";
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            directoryPath = cmd.getOptionValue("dir");

            if (cmd.hasOption("md")) {
                isOnlyMarkdownLinks = true;
            } else {
                // this is required for repeated unit testing
                isOnlyMarkdownLinks = false;
            }
            if (cmd.hasOption("ex")){
                exclusion = cmd.getOptionValue("ex");
            }

        } catch (ParseException | NullPointerException e) {
            System.out.println("Problem while parsing the arguments.");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("posix", options);
            System.exit(1);
            return;
        }

        File root = new File(directoryPath);

        // case 1: root is a file
        if (root.isFile()) {
            if (!isFileOk(root)) {
                System.exit(1);
                return;
            }
        } else {
            // case 2: root is a directory
            if (!isDirectoryOk(root, exclusion)) {
                System.exit(1);
                return;
            }
        }

        System.exit(0);
    }

    static boolean isDirectoryOk(File directoryOrFile){
        return isDirectoryOk(directoryOrFile, "");
    }

    /**
     * Recursively checks all files in the specified directory. The method will check the file, if a file is provided
     * instead of a directory.
     * @param directoryOrFile File or directory.
     * @param exclude Directory or file name that shall be excluded
     * @return True if file is ok, else false.
     */
    static boolean isDirectoryOk(File directoryOrFile, String exclude) {
        boolean result = true;
        if (directoryOrFile.isDirectory()) {
            for (File file : directoryOrFile.listFiles()) {
                if(file.getName().equals(exclude)){
                    continue;
                }
                boolean intermediateResult = isDirectoryOk(file, exclude);
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
            if (isOnlyMarkdownLinks && !file.getName().endsWith(".md")) {
                return true;
            }
            Set<String> linkSet = new HashSet<>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            LinkExtractor linkExtractor = LinkExtractor.builder()
                    .linkTypes(EnumSet.of(LinkType.URL, LinkType.WWW))
                    .build();

            ArrayList<String> warnings = new ArrayList<>();
            // let's crawl all URLs
            while ((line = reader.readLine()) != null) {
                for (LinkSpan link : linkExtractor.extractLinks(line)) {
                    if (isOnlyMarkdownLinks) {
                        try {
                            if (line.charAt(link.getBeginIndex() - 1) == '(' && line.charAt(link.getEndIndex()) == ')') {
                                linkSet.add(line.substring(link.getBeginIndex(), link.getEndIndex()));
                            }
                        } catch (StringIndexOutOfBoundsException e){
                            warnings.add("\tMissing brackets in line: '" + line + "' ⚠️");
                        }
                    } else {
                        linkSet.add(line.substring(link.getBeginIndex(), link.getEndIndex()));
                    }
                }

            }
            reader.close();
            Triplet<Boolean, Set<String>, Set<String>> result = isLinkSetOk(linkSet);
            if (result.getValue0()) {
                System.out.println(file.getAbsolutePath() + "  ✅️");
            } else {
                System.out.println(file.getAbsolutePath() + "  ❌️");
                for (String s : result.getValue1()) {
                    System.out.println("\t" + s + " ❌ [Invalid URL]");
                }
            }
            if(result.getValue2().size() > 0){
                for (String s : result.getValue2()) {
                    System.out.println("\t" + s + " ⚠️ [Problem with URL]");
                }
            }
            if(warnings.size() > 0){
                for(String s : warnings){
                    System.out.println(s);
                }
            }

            return result.getValue0();
        } catch (IOException e) {
            System.out.println("Problem while processing file: " + file.getAbsolutePath());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check a set of links.
     * @param linkSet The set of links to be checked.
     * @return Pair where
     * [0] bool indicating whether the link set is ok
     * [1] URLs that are not ok
     * [2] URLs that produce some warning.
     */
    static Triplet<Boolean, Set<String>, Set<String>> isLinkSetOk(Set<String> linkSet) {
        boolean result = true;
        Set<String> errorUrls = new HashSet<>();
        Set<String> warnUrls = new HashSet<>();
        for (String link : linkSet) {
           switch (isLinkOk(link)){
               case OK:
                   continue;
               case WARN:
                   warnUrls.add(link);
                   break;
               case ERROR:
                   errorUrls.add(link);
                   result = false;
                   break;
           }
        }
        return new Triplet(result, errorUrls, warnUrls);
    }

    enum UrlStatus {
        OK, WARN, ERROR;
    }

    static UrlStatus isLinkOk(String link){
        return isLinkOk(link, 0);
    }

    /**
     * Check whether the specified string URL is ok.
     * @param link The URL that shall be checked.
     * @return OK: Everything is fine.
     * WARN: Some exception occurred, the URL may be fine.
     * ERROR: The URL is not ok.
     */
    static UrlStatus isLinkOk(String link, int trial){
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            TrustModifier.relaxHostChecking(connection);
            connection.setDoOutput(true);
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK
                    && responseCode != HttpURLConnection.HTTP_MOVED_PERM
                    && responseCode != HttpURLConnection.HTTP_MOVED_TEMP
                    && responseCode != HttpURLConnection.HTTP_FORBIDDEN) {

                if(trial < MAX_RETRIES) {
                    if(debugOutput) {
                        System.out.printf("A problem occurred with link: %s\nSleep for %s seconds and retry.\n",
                                link,
                                RETRY_TIME_SECONDS);
                    }
                    try {
                        Thread.sleep(RETRY_TIME_SECONDS * 1000);
                        return isLinkOk(link, ++trial);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return UrlStatus.ERROR;
                    }
                } else {
                    return UrlStatus.ERROR;
                }
            }
        } catch (IOException e) {
            System.out.println("Problematic link: " + link);
            e.printStackTrace();
            return UrlStatus.WARN;
        }
        return UrlStatus.OK;
    }
}
