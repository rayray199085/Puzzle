package com.project.stephencao.puzzle.util;

import android.graphics.Bitmap;
import com.project.stephencao.puzzle.bean.ImagePieceBean;

import java.util.ArrayList;
import java.util.List;

public class ImageSplitter {
    /**
     * cut an image into pieceCount * pieceCount pieces
     *
     * @param bitmap
     * @param pieceCount
     * @return
     */
    public static List<ImagePieceBean> splitImage(Bitmap bitmap, int pieceCount) {
        List<ImagePieceBean> pieceBeans = new ArrayList<>();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // the image shape should be a square
        int pieceLength = Math.min(width, height) / pieceCount;
        for (int i = 0; i < pieceCount; i++) {
            for (int j = 0; j < pieceCount; j++) {
                Bitmap childBitmap = Bitmap.createBitmap(bitmap, j * pieceLength, i * pieceLength, pieceLength, pieceLength);
                ImagePieceBean imagePieceBean = new ImagePieceBean(j + i * pieceCount,childBitmap);
                pieceBeans.add(imagePieceBean);
            }
        }
        return pieceBeans;
    }
}
