package com.anhlq.calendarwidgets.imageclass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by NguyenDuc on 09/01/2015.
 */
public class MultiTouchView extends ImageView implements View.OnTouchListener {
    // these matrices will be used to move and zoom image
    private Matrix mMatrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private final float[] mMatrixValues = new float[9];
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;

    private int mIntrinsicWidth;
    private int mIntrinsicHeight;
    // display width height.
    private int mWidth;
    private int mHeight;


    //scale
    private float mScale;
    private float mMinScale;
    private CommunicatorOnTouchMultiView communicator;

    public MultiTouchView(Context context, AttributeSet attr) {
        super(context, attr);
        initialize();
    }

    public MultiTouchView(Context context) {
        super(context);
        initialize();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        this.initialize();
    }

    private void initialize() {
        this.setScaleType(ScaleType.MATRIX);
        Drawable d = getDrawable();
        if (d != null) {
            mIntrinsicWidth = d.getIntrinsicWidth();
            mIntrinsicHeight = d.getIntrinsicHeight();

            setOnTouchListener(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // handle touch events here
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(mMatrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(mMatrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    mMatrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    mMatrix.postTranslate(dx, dy);
                    //maxMove(dx, dy, event);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        mMatrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        mMatrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null && event.getPointerCount() == 2) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        mMatrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (this.getWidth() / 2) * sx;
                        float yc = (this.getHeight() / 2) * sx;
                        mMatrix.postRotate(r, tx + xc, ty + yc);
                    }
                }
                break;
        }

        this.setImageMatrix(mMatrix);
        return true;
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    public void maxMove(float dx, float dy, MotionEvent event) {
      /*  mMatrix.getValues(mMatrixValues);
        float scaleX = mMatrixValues[0];
        float scaleY = mMatrixValues[4];
        float relativeX = ((event.getX() - mMatrixValues[2]) * scaleX) / mMatrixValues[0];
        float relativeY = ((event.getY() - mMatrixValues[5]) * scaleY) / mMatrixValues[4];
        if (relativeX < 0) {
            dx = 0;
        }
        Log.d("tag", relativeX + "");*/
        mMatrix.postTranslate(dx, dy);
    }

    public void cutting() {
        int width = (int) (mIntrinsicWidth * getScale());
        int height = (int) (mIntrinsicHeight * getScale());
        if (getTranslateX() < -(width - mWidth)) {
            mMatrix.postTranslate(-(getTranslateX() + width - mWidth), 0);
        }
        if (getTranslateX() > 0) {
            mMatrix.postTranslate(-getTranslateX(), 0);
        }
        if (getTranslateY() < -(height - mHeight)) {
            mMatrix.postTranslate(0, -(getTranslateY() + height - mHeight));
        }
        if (getTranslateY() > 0) {
            mMatrix.postTranslate(0, -getTranslateY());
        }
        if (width < mWidth) {
            mMatrix.postTranslate((mWidth - width) / 2, 0);
        }
        if (height < mHeight) {
            mMatrix.postTranslate(0, (mHeight - height) / 2);
        }
        setImageMatrix(mMatrix);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
    /*    mWidth = r - l;
        mHeight = b - t;

        mMatrix.reset();
        mScale = (float) r / (float) mIntrinsicWidth;
        int paddingHeight = 0;
        int paddingWidth = 0;
        // scaling vertical
        if (mScale * mIntrinsicHeight > mHeight) {
            mScale = (float) mHeight / (float) mIntrinsicHeight;
            mMatrix.postScale(mScale, mScale);
            paddingWidth = (r - mWidth) / 2;
            paddingHeight = 0;
            // scaling horizontal
        } else {
            mMatrix.postScale(mScale, mScale);
            paddingHeight = (b - mHeight) / 2;
            paddingWidth = 0;
        }
        mMatrix.postTranslate(paddingWidth, paddingHeight);

        setImageMatrix(mMatrix);
        mMinScale = mScale;
        //move to screen center
        cutting();*/
        return super.setFrame(l, t, r, b);
    }


    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    protected float getScale() {
        return getValue(mMatrix, Matrix.MSCALE_X);
    }

    protected float getTranslateX() {
        return getValue(mMatrix, Matrix.MTRANS_X);
    }

    protected float getTranslateY() {
        return getValue(mMatrix, Matrix.MTRANS_Y);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        communicator.slideDownFromMain();
        return super.onTouchEvent(event);
    }

    public interface CommunicatorOnTouchMultiView {
        public void slideDownFromMain();
    }

    public void setOntouchMultiView(CommunicatorOnTouchMultiView communicator) {
        this.communicator = communicator;
    }
}
