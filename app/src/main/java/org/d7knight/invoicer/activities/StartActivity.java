package org.d7knight.invoicer.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;


public class StartActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }
    //MENU
    public void manageInvoices() {
        Intent myIntent = new Intent(this, ManageInvoices.class);
        startActivity(myIntent);
    }

    public void manageContacts() {
        Intent myIntent = new Intent(this, ManageProducts.class);
        startActivity(myIntent);
    }
    public void manageProducts() {
        Intent myIntent = new Intent(this, ManageProducts.class);
        startActivity(myIntent);
    }

}
