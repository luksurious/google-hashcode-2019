# Java implemenetation

## Prerequisites
- Java 8

## Run
With gradle: `gradlew.bat run --args="-h"` (Windows) or `./gradlew run --args="-h"` (Linux)

```
Usage: slideshow-generator [-hortuvV] [-s=<minScore>] <file>
Generate slideshow with optimized score
      <file>                 The input file with the pictures.
  -h, --help                 Show this help message and exit.
  -o, --online               Build vertical slides during slideshow generation, not
                               before
  -r, --randomize            Shuffle slides for randomization, before ordering
  -s, --minscore=<minScore>  The starting score to try to find matches
  -t, --two-sided            Build the chain on both sides
  -u, --unordered            Order slides by number of tags
  -v, --randomize-vertical   Shuffle vertical pictures before combining into slides
  -V, --version              Print version information and exit.
```

The input files must be in the same (root) folder.

Example runs:

`gradlew.bat run --args="-s 10 -uvrot c_memorable_moments.txt"`

`gradlew.bat run --args="-s 9 -t e_shiny_selfies.txt"`

Sample output:
```
λ gradlew.bat run --args="-s 10 -uvrot c_memorable_moments.txt"

> Task :run
Read file c_memorable_moments.txt, found 1.000 pictures, expected 1.000 (0s)
Removed 871 obsolete tags
................ #750 1.567pts 75% [0% 10pts+] (0s)


-- Created 12 separate chains


-- Max score: 6

The score is 1.567 (0s)
BUILD SUCCESSFUL in 2s
2 actionable tasks: 1 executed, 1 up-to-date
```

## Results
- a: 2 points in <1s
- b: 210.840 points in 3m 40s
- c: 1.567 points in <1s
- d: 434.330 points in 8m 50s
  - slightly less points but significantly faster: 432.252 points in 20s
- e: 406.513 points in 9m 11s
  - less points but faster: 400.920 in 6m 4s

## Used libraries
- Gradle
- Picocli
- Guava (finally not needed)
