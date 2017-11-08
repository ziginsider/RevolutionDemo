package io.github.ziginsider.revolutiondemo;

import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Created by zigin on 06.11.2017.
 */

public class RevolutionAnimationView extends View {

    private static class Star {
        private float x;
        private float y;
        private float scale;
        private float alpha;
        private float speed;
    }

    private float mBaseSpeed;
    private float mBaseSize;
    private long mCurrentPlayTime;

    public static final int SEED = 1337;
    public static final int COUNT = 32;
    public static final int BASE_SPEED_DP_PER_S = 200;


    /** The minimum scale of a Star */
    public static final float SCALE_MIN_PART = 0.45f;
    /** How much of the scale that's based on randomness */
    public static final float SCALE_RANDOM_PART = 0.55f;
    /** How much of the alpha that's based on the scale of the star */
    private static final float ALPHA_SCALE_PART = 0.5f;
    /** How much of the alpha that's based on randomness */
    private static final float ALPHA_RANDOM_PART = 0.5f;

    private final Random mRnd = new Random(SEED);
    private final Star[] mStars = new Star[COUNT];

    private TimeAnimator mTimeAnimator;
    private Drawable mDrawable;


    /** @see View#View(Context) */
    public RevolutionAnimationView(Context context) {
        super(context);
        init();
    }

    /** @see View#View(Context, AttributeSet) */
    public RevolutionAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /** @see View#View(Context, AttributeSet, int) */
    public RevolutionAnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_hammer);
        mBaseSize = Math.max(mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight()) / 2f;
        mBaseSpeed = BASE_SPEED_DP_PER_S * getResources().getDisplayMetrics().density;
    }

    private void initializeStar(Star star, int viewWidth, int viewHeight) {
        // Set the scale based on a min value and a random multiplier
        star.scale = SCALE_MIN_PART + SCALE_RANDOM_PART * mRnd.nextFloat();

        // Set X to a random value within the width of the view
        star.x = viewWidth * mRnd.nextFloat();

        // Set Y - start at the bottom of the view
        star.y = viewHeight;

        // The value Y is in the center of the star, add the size
        // to make sure it starts outside of the view bound
        star.y += star.scale * mBaseSize;

        //Add a random offset to create a small delay before the star appears again
        star.y += viewHeight * mRnd.nextFloat() / 4f;

        // The alpha is determined by the scale of the star and a random multiplier
        star.alpha = ALPHA_SCALE_PART * star.scale + ALPHA_RANDOM_PART * mRnd.nextFloat();

        // The bigger and the brighter star is faster
        star.speed = mBaseSpeed * star.alpha * star.scale;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int viewHight = getHeight();
        for( final Star star : mStars) {
            // Ignore the star if it's outside of the view bounds
            final float starSize = star.scale * mBaseSize;
            if (star.y + starSize < 0 || star.y - starSize > viewHight) {
                continue;
            }

            //Save the current canvas state
            final int save = canvas.save();

            // Move the canvas to the center of the star
            canvas.translate(star.x, star.y);

            //Rotate the canvas based on how far the star has moved
            final float progress = (star.y + starSize) / viewHight;
            canvas.rotate(360 * progress);

            //Prepare the size and alpha of the drawable
            final int size = Math.round(starSize);
            mDrawable.setBounds(-size, -size, size, size);
            mDrawable.setAlpha(Math.round(255 * star.alpha));

            // Draw the star to the canvas
            mDrawable.draw(canvas);

            // Restore the canvas to it's previous position and rotation
            canvas.restoreToCount(save);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // The starting position is dependent on the size of the view,
        // which is why the model is initialized here, when the view is measured.
        for (int i = 0; i < mStars.length; i++) {
            final Star star = new Star();
            initializeStar(star, w, h);
            mStars[i] = star;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mTimeAnimator = new TimeAnimator();
        mTimeAnimator.setTimeListener(new TimeAnimator.TimeListener() {
            @Override
            public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (!isLaidOut()) {
                        // Ignore all calls before the view has been measured and laid out.
                        return;
                    }
                }
                updateState(deltaTime);
                invalidate();
            }
        });
        mTimeAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mTimeAnimator.cancel();
        mTimeAnimator.setTimeListener(null);
        mTimeAnimator.removeAllListeners();
        mTimeAnimator = null;
    }

    /**
     * Progress the animation by moving the stars based on the elapsed time
     * @param deltaMs time delta since the last frame, in millis
     */
    private void updateState(float deltaMs) {
        // Converting to seconds since PX/S constants are easier to understand
        final float deltaSeconds = deltaMs / 1000f;
        final int viewWidth = getWidth();
        final int viewHeight = getHeight();

        for (final Star star : mStars) {
            // Move the star based on the elapsed time and it's speed
            star.y -= star.speed * deltaSeconds;

            // If the star is completely outside of the view bounds after
            // updating it's position, recycle it.
            final float size = star.scale * mBaseSize;
            if (star.y + size < 0) {
                initializeStar(star, viewWidth, viewHeight);
            }
        }
    }

    /**
     * Pause the animation if it's running
     */
    public void pause() {
        if (mTimeAnimator != null && mTimeAnimator.isRunning()) {
            // Store the current play time for later.
            mCurrentPlayTime = mTimeAnimator.getCurrentPlayTime();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mTimeAnimator.pause();
            }
        }
    }

    /**
     * Resume the animation if not already running
     */
    public void resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mTimeAnimator != null && mTimeAnimator.isPaused()) {
                mTimeAnimator.start();
                // Why set the current play time?
                // TimeAnimator uses timestamps internally to determine the delta given
                // in the TimeListener. When resumed, the next delta received will the whole
                // pause duration, which might cause a huge jank in the animation.
                // By setting the current play time, it will pick of where it left off.
                mTimeAnimator.setCurrentPlayTime(mCurrentPlayTime);
            }
        }
    }
}
