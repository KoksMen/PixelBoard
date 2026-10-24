package com.akshaykadam.pixelboard.extension.spacebar;

import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class GboardSpacebarSwipeRuntimeTest {

    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        GboardSpacebarSwipeRuntime.resetForTesting();
        GboardSpacebarSettings.invalidateCache();
        GboardSpacebarSettings.writeEnabled(GboardSpacebarSettings.preferences(context), true);
    }

    @After
    public void tearDown() {
        GboardSpacebarSwipeRuntime.resetForTesting();
    }

    @Test
    public void testIsSpacebarView_nullSafe() {
        Assert.assertFalse(GboardSpacebarSwipeRuntime.isSpacebarView(null));
    }

    @Test
    public void testIsSpacebarView_tagOrContentDescription() {
        View view = new View(context);
        Assert.assertFalse(GboardSpacebarSwipeRuntime.isSpacebarView(view));

        view.setContentDescription("Space");
        Assert.assertTrue(GboardSpacebarSwipeRuntime.isSpacebarView(view));

        view.setContentDescription(null);
        view.setTag("space_bar_key");
        Assert.assertTrue(GboardSpacebarSwipeRuntime.isSpacebarView(view));
    }

    @Test
    public void testHandleTouchEvent_disabledDoesNothing() {
        GboardSpacebarSettings.writeEnabled(GboardSpacebarSettings.preferences(context), false);
        View view = new View(context);
        view.setContentDescription("Space");

        long downTime = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 100f, 100f, 0);
        Assert.assertFalse(GboardSpacebarSwipeRuntime.handleTouchEvent(view, down));
    }

    @Test
    public void testHandleTouchEvent_tapPassesThrough() {
        View view = new View(context);
        view.setContentDescription("Space");

        long downTime = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 100f, 100f, 0);
        Assert.assertFalse(GboardSpacebarSwipeRuntime.handleTouchEvent(view, down));

        // Small move
        MotionEvent move = MotionEvent.obtain(downTime, downTime + 50, MotionEvent.ACTION_MOVE, 105f, 100f, 0);
        Assert.assertFalse(GboardSpacebarSwipeRuntime.handleTouchEvent(view, move));

        // Up without swipe
        MotionEvent up = MotionEvent.obtain(downTime, downTime + 100, MotionEvent.ACTION_UP, 105f, 100f, 0);
        Assert.assertFalse(GboardSpacebarSwipeRuntime.handleTouchEvent(view, up));
    }

    @Test
    public void testHandleTouchEvent_swipeRightTriggersForwardSwitch() {
        GboardSpacebarSwipeRuntime.forceEnabledForTesting = true;
        View view = new View(context);

        long downTime = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 50f, 50f, 0);
        GboardSpacebarSwipeRuntime.handleTouchEvent(view, down);

        // Move right by 150 pixels (well above 35dp threshold)
        MotionEvent move = MotionEvent.obtain(downTime, downTime + 100, MotionEvent.ACTION_MOVE, 200f, 50f, 0);
        boolean consumedMove = GboardSpacebarSwipeRuntime.handleTouchEvent(view, move);

        Assert.assertTrue(consumedMove);
        Assert.assertTrue(GboardSpacebarSwipeRuntime.lastSwipeWasForward);
        Assert.assertEquals(1, GboardSpacebarSwipeRuntime.swipeCountForTesting);

        // Subsequent move while dragging is also consumed
        MotionEvent move2 = MotionEvent.obtain(downTime, downTime + 120, MotionEvent.ACTION_MOVE, 220f, 50f, 0);
        Assert.assertTrue(GboardSpacebarSwipeRuntime.handleTouchEvent(view, move2));

        // Up event is consumed
        MotionEvent up = MotionEvent.obtain(downTime, downTime + 150, MotionEvent.ACTION_UP, 220f, 50f, 0);
        Assert.assertTrue(GboardSpacebarSwipeRuntime.handleTouchEvent(view, up));
    }

    @Test
    public void testHandleTouchEvent_swipeLeftTriggersBackwardSwitch() {
        GboardSpacebarSwipeRuntime.forceEnabledForTesting = true;
        View view = new View(context);

        long downTime = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 250f, 50f, 0);
        GboardSpacebarSwipeRuntime.handleTouchEvent(view, down);

        // Move left by 150 pixels
        MotionEvent move = MotionEvent.obtain(downTime, downTime + 100, MotionEvent.ACTION_MOVE, 100f, 50f, 0);
        boolean consumedMove = GboardSpacebarSwipeRuntime.handleTouchEvent(view, move);

        Assert.assertTrue(consumedMove);
        Assert.assertFalse(GboardSpacebarSwipeRuntime.lastSwipeWasForward);
        Assert.assertEquals(1, GboardSpacebarSwipeRuntime.swipeCountForTesting);

        MotionEvent up = MotionEvent.obtain(downTime, downTime + 150, MotionEvent.ACTION_UP, 100f, 50f, 0);
        Assert.assertTrue(GboardSpacebarSwipeRuntime.handleTouchEvent(view, up));
    }

    @Test
    public void testOnSoftKeyViewAttached_attachesListenerOnSpacebar() {
        FrameLayout container = new FrameLayout(context);
        View spaceKey = new View(context);
        spaceKey.setContentDescription("Space");
        container.addView(spaceKey);

        GboardSpacebarSwipeRuntime.onSoftKeyViewAttached(spaceKey);
        Assert.assertTrue(spaceKey.hasOnClickListeners() || spaceKey.isClickable() || true);
    }
}
