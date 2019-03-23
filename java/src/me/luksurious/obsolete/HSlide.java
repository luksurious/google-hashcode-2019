package me.luksurious.obsolete;

import me.luksurious.Picture;
import me.luksurious.obsolete.Slide;

import java.util.ArrayList;
import java.util.List;

public class HSlide extends Slide {
    Picture picture;

    public HSlide(Picture picture) {
        this.picture = picture;
    }

    @Override
    List<Picture> getPictures() {
        ArrayList<Picture> pics = new ArrayList<>();
        pics.add(picture);

        return pics;
    }
}
