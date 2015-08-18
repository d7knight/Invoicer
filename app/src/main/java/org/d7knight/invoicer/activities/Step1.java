package org.d7knight.invoicer.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.d7knight.invoicer.utilities.Utilities;

public class Step1 extends AppCompatActivity {
    private Context appContext;
    private AutoCompleteTextView[] customerDetails;
    @SuppressWarnings("unchecked")
    private static ArrayList<String>[] autoFields = new ArrayList[3];
    private final String eol = System.getProperty("line.separator");
    private Bundle resumed;
    private DrawerLayout drawer;

    public void menu(View v) {
        this.openOptionsMenu();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.step1_activity);

        // Set a toolbar to replace the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIcon(R.drawable.ic_ab_drawer);
            drawer = (DrawerLayout) findViewById(R.id.drawer);
            drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        }


        appContext = this;
        File f = new File(Utilities.basePath);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                Toast.makeText(
                        this,
                        this.getString(R.string.tst_wrndb),
                        Toast.LENGTH_LONG).show();
            }
        }
        checkFirstTime();

        customerDetails = new AutoCompleteTextView[3];
        customerDetails[0] = (AutoCompleteTextView) findViewById(R.id.ci_tag);
        customerDetails[1] = (AutoCompleteTextView) findViewById(R.id.ci_address);
        customerDetails[2] = (AutoCompleteTextView) findViewById(R.id.ci_phone);

        try {
            initAutoComplete();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }


    }


    @Override
    public void onResume() {
        super.onResume();
        checkFirstTime();
    }

    public void initAutoComplete() throws IOException {
        for (int nFieldCounter = 0; nFieldCounter < autoFields.length; nFieldCounter++) {
            autoFields[nFieldCounter] = new ArrayList<String>();
            File f = this.getFileStreamPath("field" + nFieldCounter);
            if (!f.exists()) {
                f.createNewFile();
            } else {
                loadField(nFieldCounter);
            }

        }
    }


    public void checkFirstTime() {
        File a = new File(Utilities.priceListPath), b = this.getFileStreamPath(
                Utilities.userInfoPath);
        if (!(a.exists() && b.exists())) {
            try {
                if (!(new File(Utilities.basePath + "bcard.b64")).exists()) {
                    Utilities.writeLogoFile(this.getAssets().open("bcard.png"));
                }
            } catch (IOException e) {

            }
            askForInfo();
        } else {
            try {
                if (Utilities.loadUserInfo(this) != Utilities.nFields) {
                    askForInfo();
                }
                ;
            } catch (Exception e) {
                Utilities.sendErrorLog(e, this);
            }
        }
    }


    public void loadField(final int nFieldCounter) throws IOException {
        customerDetails[nFieldCounter]
                .setOnFocusChangeListener(new AdapterView.OnFocusChangeListener() {

                    public void onFocusChange(View arg0, boolean arg1) {
                        customerDetails[nFieldCounter].dismissDropDown();
                    }
                });

        customerDetails[nFieldCounter].setThreshold(2);
        try {
            InputStreamReader inputreader = new InputStreamReader(
                    openFileInput("field" + nFieldCounter));
            BufferedReader fin = new BufferedReader(inputreader);
            String sCurrentLine = fin.readLine();
            while (sCurrentLine != null) {
                if (!autoFields[nFieldCounter].contains(sCurrentLine)) {
                    autoFields[nFieldCounter].add(sCurrentLine);
                }
                sCurrentLine = fin.readLine();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(appContext,
                    android.R.layout.simple_dropdown_item_1line, autoFields[nFieldCounter]);


            customerDetails[nFieldCounter].setAdapter(adapter);
        } catch (FileNotFoundException e) {
            Utilities.sendErrorLog(e, this);
        }
    }

    public void writeFields() {
        try {
            for (int nCounter = 0; nCounter < 3; nCounter++) {

                FileOutputStream fos = openFileOutput("field" + nCounter,
                        Context.MODE_PRIVATE);
                String currentDetail = "";
                for (int nLineCounter = 0; nLineCounter < autoFields[nCounter]
                        .size(); nLineCounter++) {
                    currentDetail += autoFields[nCounter].get(nLineCounter)
                            .toString() + eol;
                }
                fos.write(currentDetail.getBytes());
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            Utilities.sendErrorLog(e, this);
        }
    }

    public void enterProduct(View arg0) {
        String[] customerInfo = new String[3];

        for (int nCounter = 0; nCounter < customerInfo.length; nCounter++) {

            String currentDetail = customerDetails[nCounter].getText()
                    .toString();
            if (nCounter == 0 && (currentDetail == null || currentDetail.equals(""))) {
                currentDetail = "";
            }
            if (currentDetail != null) {


                customerInfo[nCounter] = currentDetail;
                autoFields[nCounter].add(currentDetail);
            }
        }
        Utilities.customerInfo = customerInfo;
        writeFields();
        Intent myIntent = new Intent(this, Step2.class);
        if (resumed != null) {
            myIntent.putExtras(resumed);
        }
        startActivityForResult(myIntent, 1);

    }

    @Override
    protected void onActivityResult(int rqCode, int rsCode, Intent data) {
        switch (rsCode) {
            case 1:
                resumed = data.getExtras();
                break;
            case 2:
                finish();
                break;
            default:
                break;

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


}
