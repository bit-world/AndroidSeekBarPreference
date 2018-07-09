package ru.bit_world.androidseekbarpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.IllegalFormatException;

/**
 * Created by PDA on 01.11.2017.
 */

public class SeekBarPreference extends Preference {

        private View mContainer;

        private int mDefaultValue;
        private int mMinValue;
        private int mMaxValue;
        private int mStepValue;

        private String mFormat;

        private int mStepMinValue;
        private int mStepMaxValue;

        private TextView mTitle;
        private TextView mSummary;
        private TextView mValue;
        private SeekBar mSeekBar;

        private SeekBar.OnSeekBarChangeListener mListener;

	public SeekBarPreference(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);

            mContainer = null;
            mListener = null;

            if (attrs != null) {
                TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);

                mMinValue = a.getInt(R.styleable.SeekBarPreference_minValue, 0);
                mDefaultValue = a.getInt(R.styleable.SeekBarPreference_android_defaultValue, 50);
                mMaxValue = a.getInt(R.styleable.SeekBarPreference_maxValue, 100);
                mStepValue = a.getInt(R.styleable.SeekBarPreference_stepValue, 1);

                if (mMinValue < 0) {
                    mMinValue = 0;
                }

                if (mMaxValue <= mMinValue) {
                    mMaxValue = mMinValue + 1;
                }

                if (mDefaultValue < mMinValue) {
                    mDefaultValue = mMinValue;
                } else if (mDefaultValue > mMaxValue) {
                    mDefaultValue = mMaxValue;
                }

                if (mStepValue <= 0) {
                    mStepValue = 1;
                }

                mFormat = a.getString(R.styleable.SeekBarPreference_format);

                a.recycle();
            } else {
                mMinValue = 0;
                mDefaultValue = 50;
                mMaxValue = 100;
                mStepValue = 1;
            }

            mStepMinValue = Math.round(mMinValue / mStepValue);
            mStepMaxValue = Math.round(mMaxValue / mStepValue);
        }

        @Override
        protected View onCreateView(ViewGroup parent) {
            super.onCreateView(parent);

            if (mContainer == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                mContainer = inflater.inflate(R.layout.seekbar_preference, null);

                mTitle = mContainer.findViewById(R.id.SeekBarPreferenceTitle);
                mTitle.setText(getTitle());

                mSummary = mContainer.findViewById(R.id.SeekBarPreferenceSummary);
                if (!TextUtils.isEmpty(getSummary())) {
                    mSummary.setText(getSummary());
                } else {
                    mSummary.setVisibility(View.GONE);
                }

                mValue = mContainer.findViewById(R.id.SeekBarPreferenceValue);

                mSeekBar = mContainer.findViewById(R.id.SeekBarPreferenceSeekBar);
                mSeekBar.setMax(mStepMaxValue - mStepMinValue);

                setValue(PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getKey(), mDefaultValue));

                mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (mListener != null) {
                            mListener.onStopTrackingTouch(seekBar);
                        }

                        saveValue();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        if (mListener != null) {
                            mListener.onStartTrackingTouch(seekBar);
                        }
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (mListener != null) {
                            mListener.onProgressChanged(seekBar, getValue(), fromUser);
                        }

                        updateView(progress);
                    }
                });
            }

            return mContainer;
        }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        mListener = listener;
    }


    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int value) {
        mMinValue = value;
        updateValues();
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int value) {
        mMaxValue = value;
        updateValues();
    }

    public int getStepValue() {
        return mStepValue;
    }

    public void setStepValue(int value) {
        mStepValue = value;
        updateValues();
    }

    public String getFormat() {
        return mFormat;
    }


    public void setFormat(String format) {
        mFormat = format;
        updateView();
    }

    public void setFormat(int formatResId) {
        setFormat(getContext().getResources().getString(formatResId));
    }

    public int getValue() {
        return (mSeekBar.getProgress() + mStepMinValue) * mStepValue;
    }

    public void setValue(int value) {
        value = getBoundedValue(value) - mStepMinValue;
        mSeekBar.setProgress(value);
        updateView(value);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTitle.setText(title);
    }

    @Override
    public void setTitle(int titleResId) {
        super.setTitle(titleResId);
        mTitle.setText(titleResId);
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        mSummary.setText(summary);
    }

    @Override
    public void setSummary(int summaryResId) {
        super.setSummary(summaryResId);
        mSummary.setText(summaryResId);
    }

    private void updateValues() {
        int currentValue = getValue();

        if (mMaxValue <= mMinValue) {
            mMaxValue = mMinValue + 1;
        }

        mStepMinValue = Math.round(mMinValue / mStepValue);
        mStepMaxValue = Math.round(mMaxValue / mStepValue);

        mSeekBar.setMax(mStepMaxValue - mStepMinValue);

        currentValue = getBoundedValue(currentValue) - mStepMinValue;

        mSeekBar.setProgress(currentValue);
        updateView(currentValue);
    }

    private int getBoundedValue(int value) {

        value = Math.round(value / mStepValue);

        if (value < mStepMinValue) {
            value = mStepMinValue;
        }

        if (value > mStepMaxValue) {
            value = mStepMaxValue;
        }

        return value;
    }

    private void updateView() {
        updateView(mSeekBar.getProgress());
    }

    private void updateView(int value) {

        if (!TextUtils.isEmpty(mFormat)) {
            mValue.setVisibility(View.VISIBLE);

            value = (value + mStepMinValue) * mStepValue;

            String text;

            try {
                text = String.format(mFormat, value);
            } catch (IllegalFormatException e) {
                text = Integer.toString(value);
            }

            mValue.setText(text);
        } else {
            mValue.setVisibility(View.GONE);
        }
    }

    private void saveValue() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putInt(getKey(), getValue())
                .apply();
    }

}
