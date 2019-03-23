package me.luksurious;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Picture {
    public int id;
    public int secondId = -1;
    public String orientation;
    public int tagCount;
    public String[] tags;
    public int[] tagIds;

    public int matchIndex;
    public int matchScore;

    static Picture combineVerticalPictures(Picture verticalPic, Picture currentPic) {
        Picture slide = new Picture();

        slide.id = verticalPic.id;
        slide.orientation = "VV";
        slide.secondId = currentPic.id;
        slide.tags = Stream.concat(Stream.of(verticalPic.tags), Stream.of(currentPic.tags))
                .distinct().toArray(String[]::new);
        slide.tagIds = IntStream.concat(IntStream.of(verticalPic.tagIds), IntStream.of(currentPic.tagIds))
                .distinct().toArray();
        slide.tagCount = verticalPic.tagIds.length;

        return slide;
    }
}
