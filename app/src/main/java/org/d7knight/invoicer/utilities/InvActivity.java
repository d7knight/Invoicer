package org.d7knight.invoicer.utilities;

import java.util.ArrayList;
import org.d7knight.invoicer.activities.R;


import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;

public abstract class InvActivity extends ListActivity implements ProductListener {
	public ArrayList<Product> productList;
	protected Context appContext;
	protected LayoutInflater inflater;
	public ArrayAdapter<Product> adapter;
	protected AlertDialog userInput;
	@Override
	public void onConfigurationChanged(Configuration c) {
		super.onConfigurationChanged(c);
		updateDialogLayout();
		adapter.notifyDataSetChanged();
	}

	public void updateDialogLayout() {
		Display display = getWindowManager().getDefaultDisplay();
		WindowManager.LayoutParams WMLP = userInput.getWindow().getAttributes();
		WMLP.y = 10;
		WMLP.height = display.getHeight() - 20;
		WMLP.width = display.getWidth();
		userInput.getWindow().setAttributes(WMLP);
	}
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		appContext = this;
		productList = new ArrayList<Product>();
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Product.listener = this;
		init();
		final ListView lv = getListView();

		lv.setTextFilterEnabled(true);
		adapter = new ArrayAdapter<Product>(
				appContext, R.layout.cell, this.productList);
		adapter.setNotifyOnChange(false);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				Product CurrentProduct = adapter.getItem(position);
				showProduct(CurrentProduct, true);

			}
		});
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						appContext);
				builder.setCancelable(true).setMessage(R.string.list_remove);
				final AlertDialog d = builder.create();

				d.setButton(appContext.getString(R.string.cancel),
						new AlertDialog.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								arg0.cancel();
							}
						});
				d.setButton2(appContext.getString(R.string.okay),
						new AlertDialog.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								adapter.remove(adapter.getItem(arg2));
								
								update(d);
							}
						});

				d.show();
				return false;
			}

		});
		}

	public void addProduct(View v) {
		Product p = new Product();
		this.adapter.add(p);
		showProduct(p, false);
	}
	public void update(AlertDialog d){
		this.adapter.notifyDataSetChanged();
		this.adapter.setNotifyOnChange(false);
		d.cancel();
	};
	public abstract void init();
	public abstract String getProductLabel(Product p);
	public abstract void showProduct(Product p, boolean isEditing);

}
