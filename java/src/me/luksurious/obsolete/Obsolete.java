package me.luksurious.obsolete;

import me.luksurious.Picture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Obsolete {

    private boolean useUnorderedSlides = false;
    private int totalScore = 0;
    private int maxSlides;
    private StringBuffer inputFilename;

    private int[] doSlideAdding(int minScore, List<Picture> slideshow, List<Picture> possibleSlides) {
        if (possibleSlides.isEmpty()) {
            return null;
        }

        Picture lastSlide = slideshow.get(slideshow.size() - 1);
        int startIndex = 0;

        while (true) {
            int[] scores = new int[possibleSlides.size()];
            boolean addedSlide = false;
            for (int oi = startIndex, arraySize = possibleSlides.size(); oi < arraySize; oi++) {
                Picture thisSlide = possibleSlides.get(oi);

                int score = score(lastSlide, thisSlide, -1);
                if (score > (minScore - 1)) {
                    lastSlide = thisSlide;
                    slideshow.add(thisSlide);
                    possibleSlides.remove(oi);

                    totalScore += score;

                    int slideshowSize = slideshow.size();
                    if (slideshowSize % 50 == 0) {
                        System.out.print(".");
                    }
                    if (slideshowSize % 2000 == 0) {
                        System.out.printf(" #%,d %,dpts %d%% [min %dpt] (%s)\n", slideshowSize, totalScore, (int) (slideshowSize / maxSlides * 100.0), minScore, timeElapsed());
                    }
                    addedSlide = true;
                    if (useUnorderedSlides) {
                        startIndex = oi;
                    } else {
                        startIndex = 0;
                    }

                    break;
                } else {
                    scores[oi] = score;
                }
            }

            if (!addedSlide) {
                if (startIndex == 0) {
                    return scores;
                }
                startIndex = 0;
            }
        }

        //if (addedCount > 0)
        //   System.out.printf(" #%,d %,dpts %d%% [min %dpt] (%s)\n", slideshow.size(), totalScore, (int)(slideshow.size() /maxSlides*100.0), minScore, timeElapsed());
        //return null;
    }

    private void removeDeadSlides(List<Picture> slides) {
        // todo remove slides without common tags
        ArrayList<Integer> slidesToRemove = new ArrayList<>();
        int slideSize = slides.size();
        for (int i = 0; i < slideSize; i++) {
            if (i % 500 == 0 && i > 0) System.out.print(".");
            if (i % 5000 == 0 && i > 0)
                System.out.printf(" %d dead %d%% (%s)%n", slidesToRemove.size(), (i * 100 / slideSize), timeElapsed());
            Picture slideA = slides.get(i);

            boolean hasMatch = false;
            for (int j = 0; j < slideSize; j++) {
                if (i == j) {
                    continue;
                }
                if (slidesToRemove.contains(j)) continue;

                if (score(slideA, slides.get(j), -1) > 0) {
                    hasMatch = true;
                    break;
                }
            }

            if (!hasMatch) {
                slidesToRemove.add(i);
            }
        }

        Collections.reverse(slidesToRemove);
        for (Integer integer : slidesToRemove) {
            slides.remove(slides.get(integer));
        }
        System.out.printf("Removed %d dead slides, %d left (%s)%n", slidesToRemove.size(), slides.size(), timeElapsed());
    }

    private void writeSlidesOutput(List<Slide> slides) {
        List<String> output = new ArrayList<>();
        output.add(slides.size() + "");

        for (Slide slide : slides) {
            if (slide instanceof HSlide) {
                output.add(((HSlide) slide).picture.id + "");
            } else {
                output.add(((VSlide) slide).pictures.get(0).id + " " + ((VSlide) slide).pictures.get(1).id);
            }
        }

        if (output.size() < 20) {
            System.out.println(output);
        }

        String baseName = inputFilename.substring(0, inputFilename.length() - 4);
        try {
            Files.write(Paths.get(baseName + "-slides.txt"), output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String timeElapsed() {
        return "";
    }

    private int score(Picture slideA, Picture picture, int i) {
        return 0;
    }
}
