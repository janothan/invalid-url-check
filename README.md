# Invalid URL Check

**What it does:**
- Parses URLS in all files of the given directory.
- Checks whether they are valid.
- Prints a log to the console.
- Will finish with exit code 1 if at least one URL is invalid, else with exit code 0.

**An URL is invalid if it does not return one of the following HTTP codes:**
- 200
- 301
- 302
- 403

**Purpose**
- Automated check of URLs in documents (e.g. documentation).
- Can be integrated in GitHub action pipelines.

**How to run?**

For any URL:
```bash
java -jar <program> -dir <directory/file to be checked>
```

If only markdown files (ending with `.md`) shall be checked:
```
java -jar -<program> <directory/file to be checked> -md
```