package org.mangelok.invoicer.activities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.mangelok.invoicer.utilities.Utilities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Toast;

public class FirstTime extends Activity {
	private String[] sValues = {"","","0","Tax","bool","$","bool","Invoice","0"};
	private CheckedTextView[] checks = new CheckedTextView[2];
	private EditText[] fields = new EditText[sValues.length-checks.length];

	int REQUEST_CODE = 98123;
	
	public void pickImage(View View) {
		Toast.makeText(this, this.getString(R.string.tst_wrnpng), Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.setType("image/png");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE);
    }
 
	public void noLogo(View view) {
		checks[0].toggle();
	}
 
	public void currPos(View view) {
		checks[1].toggle();
	}
	public void resetLogo(View view) throws IOException{
		Utilities.writeLogoFile(this.getAssets().open("bcard.png"));
		Toast.makeText(this, this.getString(R.string.tst_nlogr), Toast.LENGTH_LONG).show();
	}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
            try {
                InputStream stream = getContentResolver().openInputStream(data.getData());
                Utilities.writeLogoFile(stream);  
        		Toast.makeText(this, this.getString(R.string.tst_nlogs), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, this.getString(R.string.tst_elio), Toast.LENGTH_LONG).show();
                
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

	private void loadInfo() {
		try {
			
			fields[0].setText(Utilities.userInfo[0]);
			fields[1].setText(Utilities.userInfo[1]);
			fields[2].setText(Utilities.userInfo[2]);
			fields[3].setText(Utilities.userInfo[3]);
			if (Utilities.userInfo[4].equals("false")){
				checks[0].setChecked(false);
			}
			fields[4].setText(Utilities.userInfo[5]);
			if (Utilities.userInfo[6].equals("false")){
				checks[1].setChecked(false);
			} 
			fields[5].setText(Utilities.userInfo[7]);
			fields[6].setText(Utilities.userInfo[8]);
		} catch (Exception e) {
			// Most likely null point exception. No action required.
			//TODO: remove
			fields[0].setText("DRAPER KNIGHT\n174 Strange Street\nKitchener, ON\nN2G 1R6\n519-572-0684");
			fields[1].setText("Thank you for your business!");
			fields[2].setText("13");
			fields[3].setText("HST");
			checks[0].setChecked(false);
		}

	}

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.startup);
		
		fields[0]= (EditText) this.findViewById(R.id.ft_cmp_name);
		fields[1]= (EditText) this.findViewById(R.id.ft_greeting);
		fields[2]= (EditText) this.findViewById(R.id.ft_tax);
		fields[3]= (EditText) this.findViewById(R.id.ft_txname);
		fields[4]= (EditText) this.findViewById(R.id.ft_currency);
		fields[5]= (EditText) this.findViewById(R.id.ft_invname);
		fields[6]= (EditText) this.findViewById(R.id.ft_invnum);
		checks[0] = (CheckedTextView) this.findViewById(R.id.ft_ctv);
		checks[1] = (CheckedTextView) this.findViewById(R.id.ft_currpos);
		loadInfo();
	}

	public void next(View v) {
		//String[] sValues = {"","","0","Tax","bool","$","bool","Invoice"};
		int j = 0,c=0;//j=arraypos,c=checkedarrpos
		for (int i = 0; i < fields.length; i++){
			while (sValues[j].equals("bool")){
				sValues[j]=checks[c].isChecked()+"";
				c++;
				j++;
				if (j == sValues.length){
					Toast.makeText(this, "Error: boolean at end of sValues", Toast.LENGTH_LONG).show();
					return;
				}
			}
			//Toast.makeText(this, "I: " + i + " J: " + j, Toast.LENGTH_SHORT).show();
			String currField = fields[i].getText().toString();
			if (currField==null){currField="";}
			if (!currField.equals("")){
				sValues[j] = currField;}
			j++;
		}
		
		if (sValues[0].equals("")) {
			Toast.makeText(this, this.getString(R.string.tst_ecmpn),
					Toast.LENGTH_SHORT).show();
		} else {
			try {
				Utilities.writeUserInfo(sValues, this);
			} catch (IOException e) {
				Utilities.sendErrorLog(e, this);
			}

			File b = new File(Utilities.priceListPath);
			if (!b.exists()) {
				Intent myIntent = new Intent(this, PriceEditor.class);
				startActivity(myIntent);
			} else {
				Intent myIntent = new Intent(this, Step1.class);
				startActivity(myIntent);
			}
			finish();
			finish();
		}

	}

	public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

	public void more(View v) {
		View moreOptions = findViewById(R.id.ft_more);
		switch (moreOptions.getVisibility()) {
		case View.GONE:
			moreOptions.setVisibility(View.VISIBLE);
			((Button) v).setText(this.getString(R.string.ft_less));
			break;
		case View.VISIBLE:
			moreOptions.setVisibility(View.GONE);

			((Button) v).setText(this.getString(R.string.ft_more));
			break;
		default:
			break;
		}
	}
}


/*
String sName, sGreet, sTaxAmount, sTaxName,sCurr,sInv;
sName = name.getText().toString();
sGreet = greeting.getText().toString();
sTaxAmount = tax.getText().toString();
sCurr = curr.getText().toString();
sInv = invname.getText().toString();

if (!txName.getText().equals("")) {
	// MORE Options
	sTaxName = txName.getText().toString();
} else {
	sTaxName = "Tax";
}
if (sTaxAmount == null || sTaxAmount.equals("")) {
	sTaxAmount = "" + 0;
}
if (sCurr == null || sCurr.equals("")){
	sCurr = "$";
}
if (sInv == null || sInv.equals("")){
	sInv = "Invoice";
}*/
