/*
 * To change this template, choose Tools | Templates

 * and open the template in the editor.
 */
//TODO: Make all dialogs STATIC/FINAL.
package org.d7knight.invoicer.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

import org.d7knight.invoicer.utilities.InvActivity;
import org.d7knight.invoicer.utilities.NumberPicker;
import org.d7knight.invoicer.utilities.Product;
import org.d7knight.invoicer.utilities.Utilities;

public class Step2 extends InvActivity {
	// private TextView preview;
	private WebView preview;
	private String previewHTML;
	// DIALOG
	private ArrayAdapter<String> spinner_adapter;
	private Spinner type;
	private NumberPicker numpick;
	private EditText comments;
	private Button submit, cancel;
	// preview
	private ArrayList<Product> pTypeList;

	@Override
	public void init() {
		setContentView(R.layout.pl_list);
		this.preview = (WebView) findViewById(R.id.pl_preview);
		this.preview.setVerticalFadingEdgeEnabled(false);
		pTypeList = Utilities.getPriceList(this);
		initDialog();
		loadData(this.getIntent().getExtras());
	}
	
	@Override
	public void onConfigurationChanged(Configuration c){
		super.onConfigurationChanged(c);
		
		ViewTreeObserver vto = submit.getViewTreeObserver(); 
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 

			@Override
			public void onGlobalLayout() { 
		        submit.getViewTreeObserver().removeGlobalOnLayoutListener(this); 
				scroot.scrollTo(submit.getRight(), submit.getBottom());
		    } 
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		try {
			updatePreview();
		} catch (Exception e) {
			//Ignorance is bliss!
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void loadData(Bundle b) {
		if (b == null) {
			return;
		}
		@SuppressWarnings("unchecked")
		ArrayList<Product> p = (ArrayList<Product>) b
				.getSerializable("PRODUCT");
		
		if (p.size() > 0 && !p.get(0).type.equals("0")){this.productList = p;}
	}

	public void back(View v) {
		Intent i = getIntent();
		i.putExtra("PRODUCT", productList);
		setResult(1, i);
		finish();
	}

	public void initSpinner() {
		@SuppressWarnings("unchecked")
		ArrayList<String> spinnerList = (ArrayList<String>) Utilities.nList.clone(); 
		Collections.sort(spinnerList);
		spinner_adapter = new ArrayAdapter<String>(this, R.layout.spinnercell,
				spinnerList) {
			@Override
			public View getDropDownView(int p, View v, ViewGroup vg) {
				TextView convertView = (TextView) super.getDropDownView(p, v,
						vg);
				convertView.setTextColor(Color.BLACK);
				return (View) convertView;
			}
		};
		type.setAdapter(spinner_adapter);
	}
	ScrollView scroot;

	public void initDialog() {

		scroot = (ScrollView) inflater.inflate(R.layout.pl_dialog, null);
		
		ViewGroup root =  (ViewGroup) ((ViewGroup)scroot).getChildAt(0);
		ViewGroup dialog = (ViewGroup) root.getChildAt(0);
		ViewGroup rLay = (ViewGroup) root.getChildAt(1);

		AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
		builder.setView(scroot);

		comments = (EditText) dialog.getChildAt(5);
		numpick = (NumberPicker) dialog.getChildAt(1);
		userInput = builder.create();
		submit = (Button) rLay.getChildAt(0);
		cancel = (Button) rLay.getChildAt(1);
		type = (Spinner) dialog.getChildAt(3);
		initSpinner();
		
		ViewTreeObserver vto = submit.getViewTreeObserver(); 
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 

			@Override
			public void onGlobalLayout() { 
		        submit.getViewTreeObserver().removeGlobalOnLayoutListener(this); 
				scroot.scrollTo(submit.getRight(), submit.getBottom());
		    } 
		});
	}

	@Override
	public void showProduct(final Product ProductName, final boolean isEditing) {
		// index of comments is 6, of spinner is 4 and of number picker is 2
		if (isEditing) {
			numpick.setValue((int) ProductName.amount);
			type.setPrompt(ProductName.type);
			comments.setText(ProductName.comments);
		} else {
			numpick.setValue(1);
			type.setSelection(0);
			comments.setText("");
		}
		userInput.show();
		submit.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				ProductName.set(numpick.getValue(), type.getSelectedItem()
						.toString(), comments.getText().toString());
				update(userInput);
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				if (!isEditing) {
					adapter.remove(ProductName);
				}
				userInput.cancel();
			}
		});
		updateDialogLayout();
	
	}

	public void updatePreview() {
		if (productList.isEmpty()) {
			preview.setVisibility(View.INVISIBLE);
			return;
		}
		previewHTML = Step3.getPreview(productList, pTypeList, this);
		preview.setVisibility(View.VISIBLE);
		try {
			preview.loadData(URLEncoder.encode(previewHTML,"utf-8").replaceAll("\\+"," "), "text/html", "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
	}

	@Override
	public String getProductLabel(Product p) {
		String label = ((int) p.amount) + " " + p.type + " " + p.comments;
		return label;
	}

	public void send(View v) throws IOException {
		if (!productList.isEmpty()) {
			Intent previewIntent = new Intent(this, Step3.class);
			previewIntent.putExtra("HTML", previewHTML);
			startActivity(previewIntent);
		} else {
			Toast toast = Toast.makeText(appContext,
					this.getString(R.string.tst_eprod), Toast.LENGTH_LONG);
			toast.show();
		}
	}


	@Override
	public void update(AlertDialog d) {
		super.update(d);
		updatePreview();
	}

	//MENU
	public void viewOld() {
		Intent myIntent = new Intent(this, ViewList.class);
		startActivity(myIntent);
	}

	public void editPrices() {
		Intent myIntent = new Intent(this, PriceEditor.class);
		startActivity(myIntent);
	}
	public void askForInfo() {
		Intent myIntent = new Intent(this, FirstTime.class);
		startActivity(myIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.editpricelist:
			editPrices();
			break;
		case R.id.viewpastivcs:
			viewOld();
			break;
		case R.id.changecmpinfo:
			askForInfo();
			break;
		}
		return true;
	}

	public void menu(View v){
		this.openOptionsMenu();
	}
}