package me.luksurious.obsolete;

import me.luksurious.Picture;

import java.util.ArrayList;
import java.util.List;

public class VSlide extends Slide {
    List<Picture> pictures = new ArrayList<>();

    public VSlide(Picture left, Picture right) {
        this.pictures.add(left);
        this.pictures.add(right);
    }

    @Override
    List<Picture> getPictures() {
        return pictures;
    }
}
