package com.project.stephencao.puzzle.bean;

import android.graphics.Bitmap;

public class ImagePieceBean {
    private int index;
    private Bitmap bitmap;

    public ImagePieceBean(int index, Bitmap bitmap) {
        this.index = index;
        this.bitmap = bitmap;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "ImagePieceBean{" +
                "index=" + index +
                ", bitmap=" + bitmap +
                '}';
    }
}
