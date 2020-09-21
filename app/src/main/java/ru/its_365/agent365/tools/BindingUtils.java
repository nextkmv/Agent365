package ru.its_365.agent365.tools;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.widget.TextView;

import ru.its_365.agent365.db.model.PriceType;

public class BindingUtils {

    @BindingAdapter("android:text")
    public static void setFloat(TextView view, float value) {
        if (Float.isNaN(value)) view.setText("");
        else view.setText(String.format("%.2f", value));
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static float getFloat(TextView view) {
        String num = view.getText().toString();
        if(num.isEmpty()) return 0.0F;
        try {
            return Float.parseFloat(num);
        } catch (NumberFormatException e) {
            return 0.0F;
        }
    }

    @BindingAdapter("android:text")
    public static void setPriceType(TextView view, PriceType value) {
        if (value == null) view.setText("");
        else view.setText(value.getName());
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static PriceType getPriceType(TextView view) {
        String num = view.getText().toString();
        return null;
    }
}
