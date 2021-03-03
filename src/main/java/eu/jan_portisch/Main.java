package eu.jan_portisch;

import org.apache.commons.cli.*;
import org.javatuples.Pair;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;


public class Main {


    /**
     * If true only md files are parsed and only md links are checked.
     */
    private static boolean isOnlyMarkdownLinks = false;

    public static void main(String[] args) {
        Options options = new Options();
        Option dirOption = new Option("dir", "directory", true, "The directory or file that shall be checked.");
        dirOption.setOptionalArg(false);
        Option mdOption = new Option("md", "markdown", false, "Only markdown files will be parsed and only markdown links will be checked.");
        mdOption.setOptionalArg(true);
        options.addOption(dirOption);
        options.addOption(mdOption);

        String directoryPath;
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            directoryPath = cmd.getOptionValue("dir");

            if(cmd.hasOption("md")){
                isOnlyMarkdownLinks = true;
            }

        } catch (ParseException | NullPointerException e){
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
            if(isOnlyMarkdownLinks && !file.getName().endsWith(".md")){
                return true;
            }
            Set<String> linkSet = new HashSet<>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            LinkExtractor linkExtractor = LinkExtractor.builder()
                    .linkTypes(EnumSet.of(LinkType.URL, LinkType.WWW))
                    .build();

            // let's crawl all URLs
            while ((line = reader.readLine()) != null) {
                for (LinkSpan link : linkExtractor.extractLinks(line)) {
                    if (isOnlyMarkdownLinks) {
                        if (line.charAt(link.getBeginIndex() - 1) == '(' && line.charAt(link.getEndIndex()) == ')') {
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
}
