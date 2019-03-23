package me.luksurious;

import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

@CommandLine.Command(description = "Generate slideshow with optimized score",
        name = "slideshow-generator", mixinStandardHelpOptions = true)
public class Main implements Callable<Void> {

    final static boolean DEBUG = false;

    final static int ID_KEY = 0;
    final static int ORIENTATION_KEY = 1;
    final static int TAG_KEY = 2;

    @CommandLine.Option(names = {"-s", "--minscore"}, description = "The starting score to try to find matches")
    private int minScore = 3;

    @CommandLine.Parameters(index = "0", description = "The input file with the pictures.")
    private File file;
    private String inputFilename;

    @CommandLine.Option(names = {"-u", "--unordered"}, description = "Order slides by number of tags")
    private boolean useUnorderedSlides = false;

    @CommandLine.Option(names = {"-r", "--randomize"}, description = "Shuffle slides for randomization, before ordering")
    private boolean shuffleSlides = false;

    @CommandLine.Option(names = {"-v", "--randomize-vertical"}, description = "Shuffle vertical pictures before combining into slides")
    private boolean shuffleVerticalPictures = false;

    @CommandLine.Option(names = {"-t", "--two-sided"}, description = "Build the chain on both sides")
    private boolean buildTwoSided = false;

    @CommandLine.Option(names = {"-o", "--online"}, description = "Build vertical slides during slideshow generation, not before")
    private boolean assembleVerticalSlidesOnline = false;

    private static long startTime;
    private SlideshowGenerator slideshowGenerator;

    public static void main(String[] args) {
        CommandLine.call(new Main(), args);
    }

    @Override
    public Void call() {
        //System.out.printf("Given paramters: file = %s, minscore = %d", inputFilename, minScore);
        run();
        return null;
    }

    private void run() {
        startTime = System.currentTimeMillis();

        inputFilename = file.getName();

        FileConsumer action = new FileConsumer();
        try {
            Files.lines(file.toPath()).forEachOrdered(action);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.printf("Read file %s, found %,d pictures, expected %,d (%s)%n",
                inputFilename, action.pictures.size(), action.picCount, timeElapsed());

        if (action.picCount < 10) {
            action.pictures.forEach(picture -> {
                System.out.printf("Pic %d, orientation %s, tags %s%n",
                        picture.id, picture.orientation, String.join(", ", picture.tags));
            });
        }

        slideshowGenerator = new SlideshowGenerator(buildTwoSided, assembleVerticalSlidesOnline, minScore, useUnorderedSlides);

        List<Picture> slideshow = generateSlideshow(action.pictures);
        int totalScore = slideshowGenerator.getSlideshowScore(slideshow);

        System.out.printf("\nThe score is %,d (%s)", totalScore, timeElapsed());

        writeOutput(slideshow, totalScore);
    }

    private List<Picture> generateSlideshow(ArrayList<Picture> pictures) {
        Preprocessor preprocessor = new Preprocessor(assembleVerticalSlidesOnline, shuffleSlides, useUnorderedSlides, shuffleVerticalPictures);
        List<Picture> slides = preprocessor.generateSlides(pictures);

        if (!assembleVerticalSlidesOnline) {
            System.out.printf("Generated slides: %,d (%s)%n%n", slides.size(), timeElapsed());
        }

        List<Picture> slideshow = new ArrayList<>();

        slideshowGenerator.loopOverSlideBlocks(slideshow, slides);

        return slideshow;
    }

    private void writeOutput(List<Picture> slideshow, int totalScore) {
        List<String> output = new ArrayList<>();
        output.add(slideshow.size() + "");

        for (Picture slide : slideshow) {
            if (slide.secondId == -1) {
                output.add(slide.id + "");
            } else {
                output.add(slide.id + " " + slide.secondId);
            }
        }

        if (output.size() < 20) {
            System.out.println(output);
        }

        String baseName = inputFilename.substring(0, inputFilename.length() - 4)
                + "-slideshow_" + totalScore + "_" + timeElapsed().replaceAll(" ", "");
        try {
            Files.write(Paths.get(baseName + ".txt"), output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String timeElapsed() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;

        if (elapsed < 60) {
            return elapsed + "s";
        }
        long minutes = elapsed / 60;
        long seconds = elapsed % 60;
        return minutes + "m " + seconds + "s";
    }
}
