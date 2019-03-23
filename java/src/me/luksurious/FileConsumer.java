package me.luksurious;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

class FileConsumer implements Consumer<String> {
    private int lineNum = 0;

    int picCount;

    ArrayList<Picture> pictures = new ArrayList<>();

    @Override
    public void accept(String s) {
        if (lineNum == 0) {
            picCount = Integer.parseInt(s);
        } else if (!s.isEmpty()) {
            String[] res = s.split(" ");

            Picture newPic = new Picture();
            newPic.id = lineNum - 1;
            newPic.orientation = res[0];
            newPic.tags = Arrays.<String>copyOfRange(res, 2, res.length);
            newPic.tagCount = newPic.tags.length;
            Arrays.sort(newPic.tags);

            pictures.add(newPic);
        }

        lineNum++;
    }
}
