package com.example.wgumobile;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * DateWatcher class is an implementation of TextWatcher,
 * which is used to enforce date format dd/mm/yy
 */
public class DateWatcher implements TextWatcher {
    public static final String DATE_FORMAT = "^\\d{1,2}\\/\\d{1,2}\\/\\d{2}$";
    private EditText editText;

    public DateWatcher(EditText editText) {
        this.editText = editText;
    }

    //Unused
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int textLength = editText.getText().toString().length();
        //Suspend textChangedListener momentarily to enforce format
        editText.removeTextChangedListener(this);
        //Add / between month and day only if adding letters to text
        if ((textLength == 2 || textLength == 5) && count > 0) {
            editText.setText(editText.getText().toString() + "/");
            editText.setSelection(s.length() + 1);
        }
        //Don't allow typing to continue past year
        if (textLength > 8) {
            editText.setText(editText.getText().toString().substring(0, 8));
            editText.setSelection(s.length() - 1);
        }
        editText.addTextChangedListener(this);
    }

    //Unused
    @Override
    public void afterTextChanged(Editable s) {
    }
}
