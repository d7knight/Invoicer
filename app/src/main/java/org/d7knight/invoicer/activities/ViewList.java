/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.d7knight.invoicer.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;

import org.d7knight.invoicer.utilities.Utilities;

/**
 *
 * @author michael
 */
public class ViewList extends AppCompatActivity {

    static Context appContext;    
    String[] invoicelist;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private ListView lv;
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
        setContentView(R.layout.viewlist_activity);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIcon(R.drawable.ic_ab_drawer);
            drawer = (DrawerLayout) findViewById(R.id.drawer);
            drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        }
         lv=new ListView(this);
        initInvoices();
        lv.setAdapter(new ArrayAdapter<String>(this, R.layout.price_cell, invoicelist));
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
//OnItemClick	
				Intent i = new Intent(appContext,ViewerHtml.class);
				i.putExtra("HTML", invoicelist[pos]);
				startActivity(i); 
				
			}
        });
    }
}
