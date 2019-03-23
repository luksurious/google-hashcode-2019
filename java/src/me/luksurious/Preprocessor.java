package me.luksurious;

import java.util.*;

class Preprocessor {

    private boolean assembleVerticalSlidesOnline;
    private boolean shuffleSlides;
    private boolean useUnorderedSlides;
    private boolean shuffleVerticalPictures;

    Preprocessor(boolean assembleVerticalSlidesOnline, boolean shuffleSlides, boolean useUnorderedSlides, boolean shuffleVerticalPictures) {
        this.assembleVerticalSlidesOnline = assembleVerticalSlidesOnline;
        this.shuffleSlides = shuffleSlides;
        this.useUnorderedSlides = useUnorderedSlides;
        this.shuffleVerticalPictures = shuffleVerticalPictures;
    }

    List<Picture> generateSlides(ArrayList<Picture> pictures) {
        HashMap<String, TagInfo> tagDict = mapTagsToNumbers(pictures);
        removeObsoleteTags(pictures, tagDict);

        List<Picture> slides;

        if (assembleVerticalSlidesOnline) {
            slides = pictures;
        } else {
            slides = new ArrayList<>();
            List<Picture> verticalPics = new ArrayList<>();

            for (Picture picture : pictures) {
                if (picture.orientation.equals("H")) {
                    slides.add(picture);
                } else {
                    verticalPics.add(picture);
                }
            }

            slides.addAll(assembleVerticalSlides(verticalPics));
        }

        if (shuffleSlides) {
            Collections.shuffle(slides);
        }
        if (!useUnorderedSlides) {
            slides.sort(Comparator.comparingInt(o -> -o.tagCount));
        }

        // sort tags
        for (Picture slide : slides) {
            Arrays.sort(slide.tagIds);
            if (Main.DEBUG) {
                System.out.printf("Slide %d %d, tags %s%n", slide.id, slide.secondId, Arrays.toString(slide.tags));
                System.out.printf("Slide %d %d, tags %s%n", slide.id, slide.secondId, Arrays.toString(slide.tagIds));
            }
        }

        return slides;
    }

    private HashMap<String, TagInfo> mapTagsToNumbers(List<Picture> pictures) {
        // map tags to indices, to speed up comparison later
        HashMap<String, TagInfo> tagDict = new HashMap<>();

        int tagIdCounter = 0;
        for (int i = 0; i < pictures.size(); i++) {
            Picture picture = pictures.get(i);

            // initialize tag ids with the same size as the string tags
            picture.tagIds = new int[picture.tagCount];

            int tagIndex = 0;
            for (String tag : picture.tags) {
                TagInfo tagData;
                if (!tagDict.containsKey(tag)) {
                    // create a new taginfo object, for a new tag
                    tagData = new TagInfo();
                    tagData.tagId = tagIdCounter;
                    tagDict.put(tag, tagData);

                    tagIdCounter++;
                } else {
                    tagData = tagDict.get(tag);
                }
                // add current slide to the list of slides which contain the tag
                tagData.slideIds.add(i);

                // populate the tag index array
                picture.tagIds[tagIndex++] = tagData.tagId;
            }
        }
        return tagDict;
    }

    private void removeObsoleteTags(List<Picture> pictures, HashMap<String, TagInfo> tagDict) {
        int tagIdCounter;
        int removedTags = 0;
        tagIdCounter = 0;
        HashMap<Integer, Integer> tagMap = new HashMap<>();

        // search for tags which are only on one slide
        for (Map.Entry<String, TagInfo> tag : tagDict.entrySet()) {
            TagInfo tagInfo = tag.getValue();
            if (tagInfo.slideIds.size() < 2) {
                // only tag id and one slide - can be removed
                Integer pictureId = tagInfo.slideIds.get(0);
                int indexToRemove = -1;
                int[] tagIds = pictures.get(pictureId).tagIds;

                // find index in array of tagids which should be removed
                for (int i = 0; i < tagIds.length; i++) {
                    if (tagIds[i] == tagInfo.tagId) {
                        indexToRemove = i;
                        break;
                    }
                }
                if (indexToRemove == -1) {
                    System.out.printf("Error finding tag index for %s in slide %d (tag=%d)%n",
                            tag.getKey(), pictureId, tagInfo.tagId);
                    continue;
                }
                pictures.get(pictureId).tagIds = removeElement(tagIds, indexToRemove);
                removedTags++;
                tag.setValue(null);
            } else {
                // reset tag ids
                tagMap.put(tagInfo.tagId, tagIdCounter++);
            }
        }

        if (removedTags > 0) {
            // if tags were removed update the tag ids with the new tag order, to prevent holes
            for (Picture slide : pictures) {
                int length = slide.tagIds.length;
                for (int i = 0; i < length; i++) {
                    slide.tagIds[i] = tagMap.get(slide.tagIds[i]);
                }
            }
        }

        System.out.printf("Removed %,d obsolete tags%n", removedTags);
    }

    private int[] removeElement(int[] arr, int removedIdx) {
        System.arraycopy(arr, removedIdx + 1, arr, removedIdx, arr.length - 1 - removedIdx);
        return Arrays.copyOfRange(arr, 0, arr.length - 1);
    }

    private List<Picture> assembleVerticalSlides(List<Picture> verticalPics) {
        System.out.printf("Assembling vertical pictures into slides: %,d%n", verticalPics.size());
        List<Picture> verticalSlides = new ArrayList<>();

        if (shuffleVerticalPictures) {
            Collections.shuffle(verticalPics);
        }

        do {
            if (verticalPics.isEmpty()) {
                break;
            }
            // pick a first picture, and find first picture with few common tags
            Picture verticalPic = verticalPics.get(0);
            verticalPics.remove(0);

            Picture pairPicture = findVerticalMatch(verticalPic, verticalPics);
            if (pairPicture == null) {
                // cannot find any match - list empty??
                continue;
            }

            verticalSlides.add(Picture.combineVerticalPictures(verticalPic, pairPicture));

        } while (verticalPics.size() > 1);

        return verticalSlides;
    }

    private Picture findVerticalMatch(Picture verticalPic, List<Picture> verticalPics) {
        Picture pairPicture = null;

        // try to find a pair without common tags. if not possible take the one with the least common ones
        int maxCommon = 0;
        boolean hasMatch = false;

        // variables to hold the picture with the fewest common tags
        int secondBestTagCounts = -1;
        int secondBestTagIndex = -1;

        for (int vi = 0; vi < verticalPics.size(); vi++) {
            Picture currentPic = verticalPics.get(vi);
            int[] commonTags = getCommonTagIds(verticalPic.tagIds, currentPic.tagIds);

            int commonTagsCount = commonTags.length;
            if (commonTagsCount > maxCommon) {
                // save index if tag is less than the previously lowest number
                if (commonTagsCount < secondBestTagCounts || secondBestTagCounts == -1) {
                    secondBestTagCounts = commonTagsCount;
                    secondBestTagIndex = vi;
                }
                continue;
            }

            pairPicture = currentPic;
            verticalPics.remove(vi);
            hasMatch = true;
            break;
        }

        // check if we have a picture with the fewest tags to use instead
        if (!hasMatch) {
            if (secondBestTagIndex >= 0) {
                pairPicture = verticalPics.get(secondBestTagIndex);
                verticalPics.remove(secondBestTagIndex);
            }
        }

        return pairPicture;
    }

    private int[] getCommonTagIds(int[] a, int[] b) {
        int bLength = b.length;
        int[] commonTags = new int[Math.max(a.length, bLength)];

        int commonTagsCount = 0;
        for (int tag : a) {
            for (int i1 : b) {
                if (tag == i1) {
                    commonTags[commonTagsCount] = tag;
                    commonTagsCount++;
                    break;
                }
            }
        }

        return Arrays.copyOfRange(commonTags, 0, commonTagsCount);
    }
}
