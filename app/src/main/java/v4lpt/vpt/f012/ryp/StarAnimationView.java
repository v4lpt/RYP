package v4lpt.vpt.f012.ryp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class StarAnimationView extends View {

    private Paint fillPaint;
    private Paint strokePaint;
    private Paint blackBorderPaint;
    private Path starPath;
    private int currentStep = 0;
    private ValueAnimator animator;
    private static final int STAR_COUNT = 5;
    private static final int TOTAL_STEPS = STAR_COUNT * 2;
    private int rating = -1; // -1 indicates animation mode, 0-5 indicates static rating display
    private boolean isOverlay = false;

    public StarAnimationView(Context context) {
        super(context);
        init();
    }

    public StarAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StarAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.rgb(255, 215, 0));
        fillPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2f);

        blackBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackBorderPaint.setColor(Color.BLACK);
        blackBorderPaint.setStyle(Paint.Style.STROKE);
        blackBorderPaint.setStrokeWidth(8f);

        starPath = new Path();

        animator = ValueAnimator.ofInt(0, TOTAL_STEPS - 1);
        animator.setDuration(TOTAL_STEPS * 300); // 300 milliseconds per step
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentStep = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (rating == -1 && !isOverlay) {
            animator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        animator.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerY = getHeight() / 2f;
        float radius = Math.min(getWidth() / (STAR_COUNT * 2.5f), getHeight() / 5f);
        float spacing = radius * 2.5f;

        for (int i = 0; i < STAR_COUNT; i++) {
            float centerX = (i - STAR_COUNT / 2f + 0.5f) * spacing + getWidth() / 2f;
            drawStar(canvas, centerX, centerY, radius, i);
        }
    }

    private void drawStar(Canvas canvas, float centerX, float centerY, float radius, int position) {
        createStarPath(centerX, centerY, radius);

        if (isOverlay) {
            canvas.drawPath(starPath, blackBorderPaint);
        }
        canvas.drawPath(starPath, strokePaint);

        if (rating == -1 && !isOverlay) {
            // Animation mode
            if (currentStep < STAR_COUNT) {
                // Filling stars
                if (position <= currentStep) {
                    canvas.drawPath(starPath, fillPaint);
                }
            } else {
                // Unfilling stars
                if (position > currentStep - STAR_COUNT) {
                    canvas.drawPath(starPath, fillPaint);
                }
            }
        } else {
            // Static rating display mode
            if (position < rating) {
                canvas.drawPath(starPath, fillPaint);
            }
        }
    }

    private void createStarPath(float centerX, float centerY, float radius) {
        starPath.reset();
        float innerRadius = radius * 0.4f;

        for (int i = 0; i < 10; i++) {
            float r = (i % 2 == 0) ? radius : innerRadius;
            double angle = Math.PI / 2 + i * Math.PI / 5;
            float x = (float) (centerX + r * Math.cos(angle));
            float y = (float) (centerY - r * Math.sin(angle));
            if (i == 0) starPath.moveTo(x, y);
            else starPath.lineTo(x, y);
        }
        starPath.close();
    }

    public void setRating(int rating) {
        this.rating = rating;
        animator.cancel();
        invalidate();
    }

    public void startAnimation() {
        this.rating = -1;
        animator.start();
    }

    public void setOverlay(boolean isOverlay) {
        this.isOverlay = isOverlay;
        invalidate();
    }
}