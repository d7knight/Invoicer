package org.d7knight.invoicer.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class NumberPicker extends LinearLayout {
	EditText input;

	public NumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		Button plus = new Button(context), minus = new Button(context);
		input = new EditText(context);

		minus.setTextSize(24);
		plus.setTextSize(24);
		minus.setText("-");
		plus.setText("+");

		this.setOrientation(LinearLayout.HORIZONTAL);
		input.setText("1");
		input.setTextSize(24);
		this.addView(minus);
		this.addView(input);
		this.addView(plus);

		minus.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				float f = Float.parseFloat(input.getText().toString());
				f--;
				input.setText(f + " ");
			}

		});

		plus.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				float f = Float.parseFloat(input.getText().toString());
				f++;
				input.setText(f + " ");
			}

		});
	}


	public void setValue(float a) {
		input.setText(a + " ");
	}

	public float getValue() {
		return Float.parseFloat(input.getText().toString());
	}

}