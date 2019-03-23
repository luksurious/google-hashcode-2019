package me.luksurious;

import java.util.ArrayList;
import java.util.List;

class SlideshowGenerator {

    private boolean buildTwoSided;
    private boolean assembleVerticalSlidesOnline;
    private boolean useUnorderedSlides;
    private int minScore;

    private int maxSlides;

    private int totalCount = 0;

    private int maxScoreCount = 0;
    private int totalScore = 0;

    SlideshowGenerator(boolean buildTwoSided, boolean assembleVerticalSlidesOnline, int minScore, boolean useUnorderedSlides) {
        this.buildTwoSided = buildTwoSided;
        this.assembleVerticalSlidesOnline = assembleVerticalSlidesOnline;
        this.minScore = minScore;
        this.useUnorderedSlides = useUnorderedSlides;
    }

    void loopOverSlideBlocks(List<Picture> slideshow, List<Picture> slides) {
        maxSlides = slides.size();

        int separateChains = 0;
        ArrayList<Integer> chainStarts = new ArrayList<>();

        do {
            if (Main.DEBUG) {
                System.out.print("|");
            }

            chainStarts.add(slideshow.size());

            Picture slide = generateStartSlide(slides);
            if (slide == null) {
                continue;
            }
            slideshow.add(slide);

            separateChains++;
            if (separateChains > 1) {
                totalCount++;
            }
            printProgress(slideshow, false);

            constructSlideChain(slides, slideshow, false, 0);
            // test adding in front
            if (buildTwoSided && !slides.isEmpty()) {
                // auto optimization by intellij
                // check starting points of all "chains"
                // remove from list if no match exists, to not check again next time
                chainStarts.removeIf(chainStart -> !constructSlideChain(slides, slideshow, true, chainStart));
            }
        } while (!slides.isEmpty());

        printProgress(slideshow, true);
        System.out.printf("%n%n-- Created %,d separate chains %n", separateChains);
    }

    private Picture generateStartSlide(List<Picture> slides) {
        Picture slide = slides.get(0);
        slides.remove(0);

        // if vertical slides should be combined while looping, find a match for the start slide
        if (assembleVerticalSlidesOnline && slide.orientation.equals("V")) {
            // find another one to combine with
            boolean found = false;
            for (int i = 0; i < slides.size(); i++) {
                Picture possSlide = slides.get(i);
                if (possSlide.orientation.equals("V")) {
                    // TODO find good pair
                    slide = Picture.combineVerticalPictures(slide, possSlide);
                    found = true;
                    slides.remove(i);
                    break;
                }
            }
            if (!found) {
                // cannot combine, ignore picture
                System.out.print(",");
                return null;
            }
        }

        return slide;
    }

    private boolean constructSlideChain(List<Picture> slides, List<Picture> slideshow, boolean reverse, int reverseStart) {
        Picture prevSlide;
        if (reverse) {
            prevSlide = slideshow.get(reverseStart);
        } else {
            prevSlide = slideshow.get(slideshow.size() - 1);
        }

        boolean addedSomething = false;

        while (!slides.isEmpty()) {
            checkMinScore(slideshow, prevSlide);

            Picture nextSlide = findBestSuccessor(slides, prevSlide, false);
            if (nextSlide != null) {
                if (assembleVerticalSlidesOnline) {
                    nextSlide = combineVerticalSlideOnline(slides, prevSlide, nextSlide);
                    if (nextSlide == null) {
                        continue;
                    }
                }
                if (reverse) {
                    slideshow.add(reverseStart, nextSlide);
                } else {
                    slideshow.add(nextSlide);
                }
                slides.remove(nextSlide.matchIndex);

                totalScore += nextSlide.matchScore;

                if (nextSlide.matchScore >= minScore) {
                    maxScoreCount++;
                }
                totalCount++;

                printProgress(slideshow, false);

                prevSlide = nextSlide;
                addedSomething = true;
            } else {
                break;
            }
        }

        return addedSomething;
    }

    private Picture combineVerticalSlideOnline(List<Picture> slides, Picture prevSlide, Picture nextSlide) {
        if (nextSlide.orientation.equals("V")) {
            // add the next best vertical picture
            slides.remove(nextSlide.matchIndex);
            Picture addPicture = findBestSuccessor(slides, prevSlide, true);

            if (addPicture == null) {
                // find vertical pic with least amount of tags
                int minCountTags = 1000;
                for (int i = 0; i < slides.size(); i++) {
                    Picture picture = slides.get(i);
                    if (picture.orientation.equals("V") && picture.tagCount < minCountTags) {
                        minCountTags = picture.tagCount;
                        addPicture = picture;
                        addPicture.matchIndex = i;
                    }
                }

                if (addPicture == null) {
                    // if cannot find any, skip this picture completely
                    System.out.print("-");
                    return null;
                }
            }

            nextSlide = Picture.combineVerticalPictures(nextSlide, addPicture);
            nextSlide.matchIndex = addPicture.matchIndex;
            nextSlide.matchScore = score(prevSlide, nextSlide);
        }
        return nextSlide;
    }

    /**
     * If the slide list is ordered, check if the number of tags allows to reduce the min score to speed up the run
     * @param slideshow
     * @param prevSlide
     */
    private void checkMinScore(List<Picture> slideshow, Picture prevSlide) {
        if (!useUnorderedSlides && slideshow.size() % 10 == 0) {
            double slideMaxScore = prevSlide.tagCount;
            if (!assembleVerticalSlidesOnline || prevSlide.orientation.equals("H")) {
                slideMaxScore = Math.floor(slideMaxScore / 2.0);
            }
            if (slideMaxScore < minScore) {
                minScore = (int) slideMaxScore;
            }
        }
    }

    private void printProgress(List<Picture> slideshow, boolean force) {
        int slideshowSize = slideshow.size();
        if (slideshowSize % 50 == 0) {
            System.out.print(".");
        }
        if (slideshowSize % 2000 == 0 || force) {
            System.out.printf(" #%,d %,dpts %d%% [%d%% %dpts+] (%s)\n",
                    slideshowSize, totalScore, (slideshowSize * 100 / maxSlides), (maxScoreCount * 100 / totalCount),
                    minScore, Main.timeElapsed());
        }
    }

    private Picture findBestSuccessor(List<Picture> possibleSlides, Picture previousSlide, boolean requireVertical) {
        int highestScore = 0;
        int highestIndex = -1;
        for (int oi = 0, arraySize = possibleSlides.size(); oi < arraySize; oi++) {
            Picture thisSlide = possibleSlides.get(oi);
            if (requireVertical && !thisSlide.orientation.equals("V")) {
                continue;
            }

            int score = score(previousSlide, thisSlide);
            if (score >= minScore) {
                thisSlide.matchIndex = oi;
                thisSlide.matchScore = score;
                return thisSlide;
            } else if (score > highestScore) {
                highestScore = score;
                highestIndex = oi;
            }
        }

        if (highestScore > 0) {
            Picture nextSlide = possibleSlides.get(highestIndex);
            nextSlide.matchIndex = highestIndex;
            nextSlide.matchScore = highestScore;

            return nextSlide;
        }

        return null;
    }

    private int score(Picture a, Picture b) {
        int commonCount = getCommonTagCount(a.tagIds, b.tagIds);

        if (commonCount == 0) {
            return 0;
        }
        int onlyACount = a.tagCount - commonCount;
        int onlyBCount = b.tagCount - commonCount;

        return Math.min(commonCount, Math.min(onlyACount, onlyBCount));
    }
    private int getCommonTagCount(int[] a, int[] b) {
        int commonTagsCount = 0;
        for (int i2 : a) {
            for (int i1 : b) {
                if (i2 == i1) {
                    commonTagsCount++;
                    break;
                }
            }
        }

        return commonTagsCount;
    }

    int getSlideshowScore(List<Picture> slideshow) {
        int totalScore = 0;
        Picture prevSlide = slideshow.get(0);
        int maxScore = 0;
        for (int i = 1; i < slideshow.size(); i++) {

            Picture thisSlide = slideshow.get(i);
            int score = score(prevSlide, thisSlide);

            if (score > maxScore) {
                maxScore = score;
            }

            totalScore += score;

            prevSlide = thisSlide;
        }

        System.out.printf("%n%n-- Max score: %d%n", maxScore);

        return totalScore;
    }

}
