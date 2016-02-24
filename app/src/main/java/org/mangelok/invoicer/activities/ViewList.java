/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mangelok.invoicer.activities;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;

import org.mangelok.invoicer.utilities.Utilities;

/**
 *
 * @author michael
 */
public class ViewList extends ListActivity {

    static Context appContext;    
    String[] invoicelist;

    public void create(View v){
        Intent i = new Intent(this,Step1.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void initInvoices() {
        ArrayList<String> ivcs = new ArrayList<String>();
        File rDir = new File(Utilities.basePath);
        rDir.mkdirs();
        File[] rDirList = rDir.listFiles();
        for (int nCounter = 0; nCounter < rDirList.length; nCounter++) {
            String sFileName = rDirList[nCounter].getName();
            if (sFileName.contains(".htm")) {
                ivcs.add(sFileName);
            }  
        }
        invoicelist = new String[ivcs.size()];
        for (int nCounter = 0; nCounter < invoicelist.length; nCounter++){
            invoicelist[nCounter] = ivcs.get(nCounter).substring(0, ivcs.get(nCounter).indexOf(".htm"));
        }
        }

       
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        appContext=this;
        setContentView(R.layout.viewerlist);
        

        final ListView lv = getListView();
        initInvoices();
        this.setListAdapter(new ArrayAdapter<String>(this, R.layout.cell, invoicelist));
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
//OnItemClick	
				Intent i = new Intent(appContext,ViewHTM.class);
				i.putExtra("HTML", invoicelist[pos]);
				startActivity(i); 
				
			}
        });
    }
}
