package com.mediatek.dialer.plugin.dialpad;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.mediatek.op09.plugin.R;


public class OP09DialpadAdditionalButtons extends FrameLayout {

    private static final String TAG = "OP09DialpadAdditionalButtons";

    private Context mHostContext;

    private int mEdgeButtonWidth;
    private int mDialButtonWidth;
    private int mButtonHeight;
    private int mDividerHeight;
    private int mDividerWidth;

    private Drawable mDividerVertical;

    private boolean mLayouted = false;

    public OP09DialpadAdditionalButtons(Context pluginContext, Context hostContext) {
        //super(pluginContext, attrs);
        super(pluginContext);

        mHostContext = hostContext;

        Resources hostResource = hostContext.getResources();
        String hostPackageName = hostContext.getPackageName();

        mEdgeButtonWidth = pluginContext.getResources().getDimensionPixelSize(R.dimen.dialpad_additional_edge_button_width);
        mDialButtonWidth = pluginContext.getResources().getDimensionPixelSize(R.dimen.dialpad_additional_dial_button_width);
        mButtonHeight = hostResource.getDimensionPixelSize(hostResource.getIdentifier("dialpad_additional_button_height",
                                                           "dimen", hostPackageName));
        mDividerHeight = hostResource.getDimensionPixelSize(hostResource.getIdentifier("dialpad_divider_height",
                                                            "dimen", hostPackageName));
        mDividerWidth = hostResource.getDimensionPixelSize(hostResource.getIdentifier("dialpad_divider_width",
                                                            "dimen", hostPackageName));

        init();
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
    }

    protected void init() {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(new int[] {
            android.R.attr.selectableItemBackground
        });
        Drawable itemBackground = typedArray.getDrawable(0);

        typedArray = getContext().getTheme().obtainStyledAttributes(new int[] {android.R.attr.dividerVertical});
        mDividerVertical = typedArray.getDrawable(0);

        Resources resource = mHostContext.getResources();
        String packageName = mHostContext.getPackageName();

        ImageButton button = new ImageButton(getContext());
        button.setImageDrawable(resource.getDrawable(resource.getIdentifier("ic_dialpad_holo_dark", "drawable", packageName)));
        button.setBackgroundDrawable(itemBackground);
        button.setId(resource.getIdentifier("dialpadButton", "id", packageName));
        addView(button);

        View divider = new View(getContext());
        divider.setBackgroundDrawable(mDividerVertical);
        addView(divider);

        button = new ImageButton(getContext());
        button.setImageResource(R.drawable.ic_dial_action_call);
        button.setBackgroundResource(R.drawable.btn_call);
        button.setId(R.id.dialButtonLeft);
        addView(button);

        divider = new View(getContext());
        divider.setBackgroundDrawable(mDividerVertical);
        addView(divider);

        button = new ImageButton(getContext());
        button.setImageResource(R.drawable.ic_dial_action_call);
        button.setBackgroundResource(R.drawable.btn_call);
        button.setId(R.id.dialButtonRight);
        addView(button);

        divider = new View(getContext());
        divider.setBackgroundDrawable(mDividerVertical);
        addView(divider);

        button = new ImageButton(getContext());
        button.setBackgroundDrawable(itemBackground.getConstantState().newDrawable());
        if (ViewConfiguration.get(getContext()).hasPermanentMenuKey()) {
            button.setId(R.id.sendSMSButton);
            button.setImageResource(R.drawable.badge_action_sms);
        } else {
            button.setId(resource.getIdentifier("overflow_menu", "id", packageName));
            button.setImageDrawable(resource.getDrawable(resource.getIdentifier("ic_menu_overflow", "drawable", packageName)));
        }
        addView(button);

        setBackgroundDrawable(resource.getDrawable(resource.getIdentifier("dialpad_background", "drawable", packageName)));
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mLayouted) {
            return;
        }

        mLayouted = true;

        int dividerTop = (mButtonHeight - mDividerHeight) >> 1;

        // dialpad button
        View child = getChildAt(0);
        child.layout(0, 0, mEdgeButtonWidth, mButtonHeight);

        // divider
        child = getChildAt(1);
        child.layout(mEdgeButtonWidth, dividerTop, mEdgeButtonWidth + mDividerWidth, dividerTop + mDividerHeight);

        // left dial button
        View leftDialButton = getChildAt(2);
        View rightDialButton = getChildAt(4);
        if (View.VISIBLE == rightDialButton.getVisibility()) {
            leftDialButton.layout(mEdgeButtonWidth + mDividerWidth, 0,
                    mEdgeButtonWidth + mDialButtonWidth + mDividerWidth, mButtonHeight);
        } else {
            leftDialButton.layout(mEdgeButtonWidth + mDividerWidth, 0,
                    mEdgeButtonWidth + (mDialButtonWidth << 1) + (mDividerWidth << 1), mButtonHeight);
        }

        // divider
        child = getChildAt(3);
        child.layout(mEdgeButtonWidth + mDialButtonWidth + mDividerWidth, dividerTop,
                     mEdgeButtonWidth + mDialButtonWidth + (mDividerWidth << 1), dividerTop + mDividerHeight);

        // right dial button
        //child = getChildAt(4);
        if (View.VISIBLE == leftDialButton.getVisibility()) {
            rightDialButton.layout(mEdgeButtonWidth + mDialButtonWidth + (mDividerWidth << 1), 0,
                         mEdgeButtonWidth + (mDialButtonWidth << 1) + (mDividerWidth << 1), mButtonHeight);
        } else {
            rightDialButton.layout(mEdgeButtonWidth + mDividerWidth, 0,
                    mEdgeButtonWidth + (mDialButtonWidth << 1) + (mDividerWidth << 1), mButtonHeight);
        }

        // divider
        child = getChildAt(5);
        child.layout(mEdgeButtonWidth + (mDialButtonWidth << 1) + (mDividerWidth << 1), dividerTop,
                     mEdgeButtonWidth + (mDialButtonWidth << 1) + mDividerWidth * 3,
                     dividerTop + mDividerHeight);

        // sms or flow menu button
        child = getChildAt(6);
        child.layout(mEdgeButtonWidth + (mDialButtonWidth << 1) + mDividerWidth * 3, 0,
                     (mEdgeButtonWidth << 1) + (mDialButtonWidth << 1) + mDividerWidth * 3, mButtonHeight);
    }

    public void hideLeftShowRightDialButton() {
        //boolean isNeedRequestLayout = false;
        View leftDialButton = getChildAt(2);
        //if (View.INVISIBLE != leftDialButton.getVisibility()) {
            leftDialButton.setVisibility(View.GONE);
            //isNeedRequestLayout = true;
        //}
        View dividerBetweenDialButtons = getChildAt(3);
        dividerBetweenDialButtons.setVisibility(View.GONE);
        View rightDialButton = getChildAt(4);
        //if (View.VISIBLE != rightDialButton.getVisibility()) {
            rightDialButton.setVisibility(View.VISIBLE);
            //isNeedRequestLayout = true;
        //}
        //if (isNeedRequestLayout) {
        //    requestLayout();
        //}
    }

    public void hideRightShowLeftDialButton() {
        //boolean isNeedRequestLayout = false;
        View rightDialButton = getChildAt(4);
        //if (View.INVISIBLE != leftDialButton.getVisibility()) {
            rightDialButton.setVisibility(View.GONE);
        //}
        View dividerBetweenDialButtons = getChildAt(3);
        dividerBetweenDialButtons.setVisibility(View.GONE);
        View leftDialButton = getChildAt(2);
        leftDialButton.setVisibility(View.VISIBLE);
        //requestLayout();
    }

    public void showLeftRightDialButton() {

        View leftDialButton = getChildAt(2);
        leftDialButton.setVisibility(View.VISIBLE);

        View dividerBetweenDialButtons = getChildAt(3);
        dividerBetweenDialButtons.setVisibility(View.VISIBLE);

        View rightDialButton = getChildAt(4);
        rightDialButton.setVisibility(View.VISIBLE);

        //requestLayout();
    }
}
