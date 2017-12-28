package de.hb_dhbw_stuttgart.tutorscout24_android.Model.Communication;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;


/*
  Created by Patrick Woehnl on 26.11.2017.
 */

/**
 * Die ChatLayout Klasse.
 * <p>
 * Das Chatlayout Managed das Layout des Chats.
 */
public class ChatLayout extends RelativeLayout {


    public ChatLayout(Context context) {
        super(context);
    }

    public ChatLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ChatLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Setzt die größe der Chat Bubbles.
     *
     * @param widthMeasureSpec  Die widthMeasureSpec.
     * @param heightMeasureSpec Die heightMeasureSpec.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final float adjustVal = (float) 12.667;

        if (getChildCount() < 3)
            return;

        int imageViewWidth = getChildAt(0).getMeasuredWidth();
        int timeWidth = getChildAt(1).getMeasuredWidth();
        int messageHeight = getChildAt(2).getMeasuredHeight();
        int messageWidth = getChildAt(2).getMeasuredWidth();

        int layoutWidth = (int) (imageViewWidth + timeWidth + messageWidth + convertDpToPixel(adjustVal, getContext()));

        setMeasuredDimension(layoutWidth, messageHeight);
    }

    /**
     * Wandelt dp in pixel um, abhängig vom Gerät.
     *
     * @param dp      Eine größe in dp.
     * @param context Der context.
     * @return Eine float Zahl welche die Pixel angibt.
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

}