package org.mangelok.invoicer.activities;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.mangelok.invoicer.utilities.InvActivity;
import org.mangelok.invoicer.utilities.Product;
import org.mangelok.invoicer.utilities.Utilities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PriceEditor extends InvActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private int permissionsRequestCode;
    private boolean permissionGranted = false;

    @Override
    public void onPause() {
        super.onPause();
        if (permissionGranted) {
            save();
        }
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        askForPermissions();
    }

    private void askForPermissions() {
        permissionsRequestCode = (int) (Math.random() * 1000.0);

        int isWriteAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int isReadAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (isReadAllowed != PackageManager.PERMISSION_GRANTED && isWriteAllowed != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please grant permission to write to your files, or else creating a price list will fail", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, permissionsRequestCode);
        } else {
            onPermissionGranted();
        }
    }

    private void onPermissionGranted(){
        this.productList = Utilities.getPriceList(this);
        updateAdapter();
        permissionGranted = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != permissionsRequestCode) {
            Log.e("MAK", "Permission result code unexpected.. Weird.");
        }

        askForPermissions();
    }

    public void email(View v) {
        String eol = System.getProperty("line.separator");
        String output = "PRICE LIST" + eol + "----------" + eol;
        Iterator<Product> it = this.productList.iterator();

        while (it.hasNext()) {
            Product prodType = it.next();
            String unit = prodType.comments;
            Float price = prodType.amount;
            output += prodType.type + ": " + Utilities.formatCurrp(price);
            if (unit != null && !unit.equals("")) {
                output += "/" + unit;
            }
            output += eol + eol;
        }
        output += Utilities.userInfo[1];
        String subject = "Price list as of: ";

        Date today = new Date();
        subject += String.format("%tB %<te, %<tY", today);

        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, output);
        startActivity(emailIntent);
    }

    @Override
    public void init() {
        setContentView(R.layout.pe_list);
        initDialog();
    }

    @Override
    public String getProductLabel(Product currentProduct) {
        String label = currentProduct.type;
        label += " - " + Utilities.formatCurrp(currentProduct.amount);
        if (currentProduct.comments != null
                && !currentProduct.comments.equals("")) {
            label += "/" + currentProduct.comments;
        }
        return label;
    }

    public void save() {
        try {
            Utilities.writePriceList(productList);
        } catch (IOException e) {
            Utilities.sendErrorLog(e, this);
        }
    }

    public void saveAndClose(View v) {
        if (this.productList.isEmpty()) {
            Toast.makeText(this, "You must create at least one product!",
                    Toast.LENGTH_LONG).show();
        } else {
            save();
            finish();
            Intent myIntent = new Intent(this, Step1.class);
            startActivity(myIntent);
        }
    }

    private EditText name, price, units;
    private Button submit, cancel;

    public void initDialog() {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.pe_dialog, null);
        ViewGroup dialog = (ViewGroup) root.getChildAt(0);
        ViewGroup rLay = (ViewGroup) dialog.getChildAt(6);
        name = (EditText) dialog.getChildAt(1);
        price = (EditText) dialog.getChildAt(3);
        units = (EditText) dialog.getChildAt(5);
        submit = (Button) rLay.getChildAt(0);
        cancel = (Button) rLay.getChildAt(1);

        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setView(root).setCancelable(true);
        userInput = builder.create();

    }

    @Override
    public void showProduct(final Product ProductName, final boolean isEditing) {

        // index of comments is 6, of spinner is 4 and of number picker is 2
        if (isEditing) {
            price.setText("" + ProductName.amount);
        } else {
            price.setText("");
        }
        name.setText(ProductName.type);
        units.setText(ProductName.comments);
        submit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                String sName = name.getText().toString();
                if (price.getText() == null
                        || price.getText().toString().equals("")) {
                    price.setText("0");
                }
                ProductName.set(Float.parseFloat(price.getText().toString()),
                        sName, units.getText().toString());
                Utilities.nList.add(sName);
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
        userInput.show();

    }

    @Override
    public void update(AlertDialog d) {
        super.update(d);

    }
}
