package com.yellowpineapple.wakup.sdk.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yellowpineapple.wakup.sdk.R;

/**
 * Created by agutierrez on 05/02/15.
 */
public class OfferActionButton extends FrameLayout {

    static final int[] SELECTED_STATE_SET = new int[] { android.R.attr.state_selected, android.R.attr.state_enabled };
    static final int[] PRESSED_STATE_SET = new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled };
    static final int[] DISABLED_STATE_SET = new int[] { - android.R.attr.state_enabled };
    static final int[] DEFAULT_STATE_SET = new int[] { android.R.attr.state_enabled };

    CharSequence text;
    Drawable icon;
    Float fontSize = 0f;

    /* Views */
    ViewGroup actionView;
    ImageView imgIcon;
    TextView txtAction;

    /* Constructors */
    public OfferActionButton(Context context) {
        this(context, null);
    }

    public OfferActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OfferActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Extract styleable attributes
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OfferActionButton);
            if (a.hasValue(R.styleable.OfferActionButton_buttonText)) {
                String text = a.getString(R.styleable.OfferActionButton_buttonText);
                setText(text);
            }
            if (a.hasValue(R.styleable.OfferActionButton_buttonIcon)) {
                Drawable icon = a.getDrawable(R.styleable.OfferActionButton_buttonIcon);
                setIcon(icon);
            }
            if (a.hasValue(R.styleable.OfferActionButton_buttonFontSize)) {
                fontSize = a.getDimension(R.styleable.OfferActionButton_buttonFontSize, 0f);
            }
            a.recycle();
        }
        init();
    }

    private void init() {
        injectViews();
        // Set text
        setText(text);
        // Set image
        setIcon(icon);
        // Set font size
        if (fontSize > 0) {
            txtAction.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }
    }

    private void injectViews() {
        inflate(getContext(), R.layout.view_action_button, this);
        imgIcon = (ImageView) findViewById(R.id.imgIcon);
        txtAction = (TextView) findViewById(R.id.txtAction);
        actionView = (ViewGroup) findViewById(R.id.actionView);
    }

    public void setText(int textResId) {
        setText(getResources().getText(textResId));
    }

    public void setText(CharSequence text) {
        this.text = text;
        if (txtAction != null) {
            txtAction.setText(text);
        }
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
        if (imgIcon != null) {
            if (!isInEditMode()) {
                Drawable defaultDrawable = icon.getCurrent();
                defaultDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.action_active), PorterDuff.Mode.MULTIPLY);
                defaultDrawable = drawableToBitmap(defaultDrawable);

                Drawable pressedDrawable = icon.getCurrent();
                pressedDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.action_pressed), PorterDuff.Mode.MULTIPLY);
                pressedDrawable = drawableToBitmap(pressedDrawable);

                Drawable disabledDrawable = icon.getCurrent();
                disabledDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.action_inactive), PorterDuff.Mode.MULTIPLY);
                disabledDrawable = drawableToBitmap(disabledDrawable);

                StateListDrawable listDrawable = new StateListDrawable();
                listDrawable.addState(DISABLED_STATE_SET, disabledDrawable);
                listDrawable.addState(SELECTED_STATE_SET, pressedDrawable);
                listDrawable.addState(PRESSED_STATE_SET, pressedDrawable);
                listDrawable.addState(DEFAULT_STATE_SET, defaultDrawable);
                imgIcon.setImageDrawable(listDrawable);
            } else {
                imgIcon.setColorFilter(getResources().getColor(R.color.action_active));
                imgIcon.setImageDrawable(icon);
            }
        }
    }

    public BitmapDrawable drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return new BitmapDrawable(getContext().getResources(), bitmap);
    }
}