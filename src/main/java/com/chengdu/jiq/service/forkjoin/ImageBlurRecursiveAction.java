package com.chengdu.jiq.service.forkjoin;

import java.util.concurrent.RecursiveAction;

/**
 * Created by jiyiqin on 2017/11/19.
 */
public class ImageBlurRecursiveAction extends RecursiveAction {
    private int[] mSource;
    private int mStart;
    private int mLength;
    private int[] mDestination;
    private int mBlurWidth = 15; // Processing window size, should be odd.
    protected static int sThreshold = 10000;

    public ImageBlurRecursiveAction(int[] src, int start, int length, int[] dst) {
        this.mSource = src;
        this.mStart = start;
        this.mLength = length;
        this.mDestination = dst;
    }

    @Override
    protected void compute() {
        if (mLength < sThreshold) {
            computeDirectly();
            return;
        }

        int split = mLength / 2;
        ImageBlurRecursiveAction leftAction = new ImageBlurRecursiveAction(mSource, mStart, split, mDestination);
        ImageBlurRecursiveAction rightAction = new ImageBlurRecursiveAction(mSource, mStart + split, mLength - split, mDestination);
        invokeAll(leftAction, rightAction);
    }

    // Average pixels from source, write results into destination.
    private void computeDirectly() {
        int sidePixels = (mBlurWidth - 1) / 2;
        for (int index = mStart; index < mStart + mLength; index++) {
            // Calculate average.
            float rt = 0, gt = 0, bt = 0;
            for (int mi = -sidePixels; mi <= sidePixels; mi++) {
                int mindex = Math.min(Math.max(mi + index, 0), mSource.length - 1);
                int pixel = mSource[mindex];
                rt += (float) ((pixel & 0x00ff0000) >> 16) / mBlurWidth;
                gt += (float) ((pixel & 0x0000ff00) >> 8) / mBlurWidth;
                bt += (float) ((pixel & 0x000000ff) >> 0) / mBlurWidth;
            }

            // Re-assemble destination pixel.
            int dpixel = (0xff000000)
                    | (((int) rt) << 16)
                    | (((int) gt) << 8)
                    | (((int) bt) << 0);
            mDestination[index] = dpixel;
        }
    }
}
