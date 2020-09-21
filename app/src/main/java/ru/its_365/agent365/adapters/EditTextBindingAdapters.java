package ru.its_365.agent365.adapters;

import android.databinding.BindingAdapter;
import android.text.TextWatcher;
import android.widget.EditText;

public class EditTextBindingAdapters {
    @BindingAdapter("textChangedListener")
    public static void bindTextWatcher(EditText editText, TextWatcher textWatcher) {
        editText.addTextChangedListener(textWatcher);
    }
}
