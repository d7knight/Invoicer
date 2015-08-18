package org.d7knight.invoicer.utilities;

import java.util.ArrayList;

import org.d7knight.invoicer.activities.FirstTime;
import org.d7knight.invoicer.activities.ManageProducts;
import org.d7knight.invoicer.activities.R;
import org.d7knight.invoicer.activities.ManageInvoices;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;

public abstract class BaseActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private Toolbar toolbar;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(this.getLayoutResource());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIcon(R.drawable.ic_ab_drawer);
            drawer = (DrawerLayout) findViewById(R.id.drawer);
            drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        }

    }

    //MENU
    public void viewOld() {
        Intent myIntent = new Intent(this, ManageInvoices.class);
        startActivity(myIntent);
    }

    public void editPrices() {
        Intent myIntent = new Intent(this, ManageProducts.class);
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
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                break;
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
    protected abstract int getLayoutResource();

    protected void setActionBarIcon(int iconRes) {
        toolbar.setNavigationIcon(iconRes);
    }

}
