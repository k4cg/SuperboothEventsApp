package nerd.tuxmobil.fahrplan.congress.schedule;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import info.metadude.android.eventfahrplan.commons.logging.Logging;
import nerd.tuxmobil.fahrplan.congress.R;


public class HorizontalSnapScrollView extends HorizontalScrollView {

    private static final String LOG_TAG = "HorizontalSnapScrollView";

    @NonNull
    private final Logging logging = Logging.get();

    private final GestureDetector gestureDetector;

    @NonNull
    private HorizontalSnapScrollState horizontalSnapScrollState = new HorizontalSnapScrollState(logging);

    private HorizontalScrollView roomNames = null;

    private static final int SWIPE_MIN_DISTANCE = 5;

    private static final int SWIPE_THRESHOLD_VELOCITY = 2800;

    /**
     * get currently displayed column index
     *
     * @return index (0..n)
     */
    public int getColumnIndex() {
        return horizontalSnapScrollState.getActiveColumnIndex();
    }

    class YScrollDetector extends SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            int xStart = (int) e.getX();
//            logging.d(LOG_TAG, "onDown xStart: " + xStart + " getMeasuredWidth: " + getMeasuredWidth());
            float ofs = (float) (getScrollX() * horizontalSnapScrollState.getDisplayColumnCount()) / getMeasuredWidth();
            int activeColumnIndex = Math.round(ofs);
            horizontalSnapScrollState = horizontalSnapScrollState.copy(
                    horizontalSnapScrollState.getLogging(),
                    xStart,
                    horizontalSnapScrollState.getDisplayColumnCount(),
                    horizontalSnapScrollState.getRoomsCount(),
                    horizontalSnapScrollState.getColumnWidth(),
                    activeColumnIndex
            );
            return super.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float scale = getResources().getDisplayMetrics().density;
            float normalizedAbsoluteVelocityX = Math.abs(velocityX / scale);
            int columns = (int) Math.ceil((normalizedAbsoluteVelocityX / SWIPE_THRESHOLD_VELOCITY) * 3);
//	    	logging.d(LOG_TAG, "onFling " + velocityX + "/" + velocityY + " " + velocityX/scale + " " + columns);
            boolean exceedsVelocityThreshold = normalizedAbsoluteVelocityX > SWIPE_THRESHOLD_VELOCITY;
            try {
                //right to left
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && exceedsVelocityThreshold) {
                    scrollToColumn(horizontalSnapScrollState.getActiveColumnIndex() + columns, false);
                    return true;
                }
                //left to right
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && exceedsVelocityThreshold) {
                    scrollToColumn(horizontalSnapScrollState.getActiveColumnIndex() - columns, false);
                    return true;
                }
            } catch (Exception e) {
                logging.e(LOG_TAG, "There was an error processing the Fling event:" + e.getMessage());
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);
        return result;
    }

    /**
     * Sets the rooms count so it can be used to calculate
     * the room column dimensions in the next layout phase.
     */
    public void setRoomsCount(@IntRange(from = 1) int roomsCount) {
        horizontalSnapScrollState = horizontalSnapScrollState.copy(
                horizontalSnapScrollState.getLogging(),
                horizontalSnapScrollState.getXStart(),
                horizontalSnapScrollState.getDisplayColumnCount(),
                roomsCount,
                horizontalSnapScrollState.getColumnWidth(),
                horizontalSnapScrollState.getActiveColumnIndex()
        );
    }

    public void scrollToColumn(int col, boolean fast) {
        final int scrollTo = horizontalSnapScrollState.calculateScrollToXPosition(getChildAt(0).getMeasuredWidth(), col);
        logging.d(LOG_TAG, "scroll to col " + col + "/" + scrollTo + " " + getChildAt(0).getMeasuredWidth());
        if (!fast) {
            this.post(() -> smoothScrollTo(scrollTo, 0));
        } else {
            scrollTo(scrollTo, 0);
            if (roomNames != null) {
                roomNames.scrollTo(scrollTo, 0);
            }
        }
        horizontalSnapScrollState = horizontalSnapScrollState.copy(
                horizontalSnapScrollState.getLogging(),
                horizontalSnapScrollState.getXStart(),
                horizontalSnapScrollState.getDisplayColumnCount(),
                horizontalSnapScrollState.getRoomsCount(),
                horizontalSnapScrollState.getColumnWidth(),
                col
        );
    }

    public HorizontalSnapScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        horizontalSnapScrollState = horizontalSnapScrollState.copy(
                horizontalSnapScrollState.getLogging(),
                horizontalSnapScrollState.getXStart(),
                getResources().getInteger(R.integer.max_cols),
                horizontalSnapScrollState.getRoomsCount(),
                0,
                horizontalSnapScrollState.getActiveColumnIndex()
        );
        gestureDetector = new GestureDetector(new YScrollDetector());
        setOnTouchListener(new OnTouchListener());
    }

    private class OnTouchListener implements View.OnTouchListener {

        public boolean onTouch(View v, MotionEvent event) {
            logging.d(LOG_TAG, "onTouch");
            if (gestureDetector.onTouchEvent(event)) {
                // logging.d(LOG_TAG, "gesture detector consumed event");
                return false;
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                int scrollX = (int) event.getX();
                int columnIndex = horizontalSnapScrollState.calculateOnTouchColumnIndex(scrollX);
                scrollToColumn(columnIndex, false);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Calculates the number of columns to display at a time based on the physical dimensions and
     * the screen density of the device and the column count of the schedule. Further limiting
     * factors are a maximum column count to be displayed and a minimum column width.
     */
    @VisibleForTesting
    public static int calculateDisplayColumnCount(
            int availablePixels,
            int columnsCount,
            int maxColumnCountForLayout,
            float densityScaleFactor,
            int minColumnWidthDip
    ) {
        int columnCountLimit = Math.min(columnsCount, maxColumnCountForLayout);
        if (columnCountLimit == 1) {
            return 1;
        }
        float availableDips = ((float) availablePixels) / densityScaleFactor;
        int minWidthColumnCount = (int) Math.floor(availableDips / minColumnWidthDip);
        int columnCount = Math.min(minWidthColumnCount, columnCountLimit);
        return Math.max(1, columnCount);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        logging.d(LOG_TAG, "onSizeChanged " + oldWidth + ", " + oldHeight + ", " + width + ", " + height + " getMW:" + getMeasuredWidth());
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        Resources resources = getResources();
        int displayColumnCount;
        if (horizontalSnapScrollState.isRoomsCountInitialized()) {
            displayColumnCount = calculateDisplayColumnCount(
                    getMeasuredWidth(),
                    horizontalSnapScrollState.getRoomsCount(),
                    resources.getInteger(R.integer.max_cols),
                    resources.getDisplayMetrics().density,
                    resources.getInteger(R.integer.min_width_dip));
        } else {
            displayColumnCount = 1;
        }
        horizontalSnapScrollState = horizontalSnapScrollState.copy(
                horizontalSnapScrollState.getLogging(),
                horizontalSnapScrollState.getXStart(),
                displayColumnCount,
                horizontalSnapScrollState.getRoomsCount(),
                horizontalSnapScrollState.getColumnWidth(),
                horizontalSnapScrollState.getActiveColumnIndex()
        );
        int newItemWidth = horizontalSnapScrollState.calculateColumnWidth(getMeasuredWidth());
        float scale = getResources().getDisplayMetrics().density;

        logging.d(LOG_TAG, "item width: " + newItemWidth + " " + ((float) newItemWidth) / scale + "dp");
        setColumnWidth(newItemWidth);
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);
//		logging.d(LOG_TAG, "scrolled from " + oldScrollX + " to " + scrollX);
        if (roomNames != null) {
            roomNames.scrollTo(scrollX, 0);
        }
    }

    public void setChildScroller(HorizontalScrollView h) {
        roomNames = h;
    }

    public int getColumnWidth() {
        if (!horizontalSnapScrollState.isRoomsCountInitialized()) {
            throw new IllegalStateException("The \"roomsCount\" field must be initialized before invoking \"getColumnWidth\".");
        }
        return horizontalSnapScrollState.getColumnWidth();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        scrollToColumn(horizontalSnapScrollState.getActiveColumnIndex(), true);
    }

    private void setColumnWidth(int pixels) {
        logging.d(LOG_TAG, "setColumnWidth " + pixels);
        horizontalSnapScrollState = horizontalSnapScrollState.copy(
                horizontalSnapScrollState.getLogging(),
                horizontalSnapScrollState.getXStart(),
                horizontalSnapScrollState.getDisplayColumnCount(),
                horizontalSnapScrollState.getRoomsCount(),
                pixels,
                horizontalSnapScrollState.getActiveColumnIndex()
        );
        if (pixels == 0) {
            return;
        }

        ViewGroup container = (ViewGroup) getChildAt(0);
        int childCount = container.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewGroup c = (ViewGroup) container.getChildAt(i);
            ViewGroup.LayoutParams p = c.getLayoutParams();
            p.width = horizontalSnapScrollState.getColumnWidth();
            c.setLayoutParams(p);
        }
        container.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        if (roomNames != null) {
            View v;
            int firstRoomChildCount = ((ViewGroup) roomNames.getChildAt(0)).getChildCount();

            for (int i = 0; i < firstRoomChildCount; i++) {
                v = ((ViewGroup) roomNames.getChildAt(0)).getChildAt(i);
                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) v.getLayoutParams();
                p.width = horizontalSnapScrollState.getColumnWidth();
                v.setLayoutParams(p);
            }
        }
        scrollToColumn(horizontalSnapScrollState.getActiveColumnIndex(), true);
    }
}
