package com.project.mis;

import android.hardware.camera2.CameraAccessException;

interface OnSwipeListener {
    void onSwipe(Swipe swipe) throws CameraAccessException;
    void onTap(Tap tap);
}
