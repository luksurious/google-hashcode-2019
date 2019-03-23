package me.luksurious.obsolete;

import me.luksurious.Picture;

import java.util.*;

abstract public class Slide {
    private Set<String> tags = null;

    abstract List<Picture> getPictures();

    public Set<String> getTags() {
        if (tags == null) {
            getTagsOfSlide(getPictures());
        }

        return tags;
    }

    private Set<String> getTagsOfSlide(List<Picture> pictures) {
        tags = new TreeSet<>();
        for (Picture picture : pictures) {
            tags.addAll(Arrays.asList(picture.tags));
        }

        return tags;
    }
}
