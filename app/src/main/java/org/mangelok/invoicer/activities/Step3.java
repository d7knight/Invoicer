package org.mangelok.invoicer.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.mangelok.invoicer.utilities.Product;
import org.mangelok.invoicer.utilities.Utilities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;

public class Step3 extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static String invoiceTemplate;
    private static String eol = System.getProperty("line.separator");
    private static Context appContext;
    private static String receipt;
    private int permissionsRequestCode;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        appContext = this;
        setContentView(R.layout.preview);
        WebView wv = (WebView) findViewById(R.id.pv_wv);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setUseWideViewPort(true);

        String html = getIntent().getStringExtra("HTML");
        if (html != null) {
            try {
                wv.loadData(
                        URLEncoder.encode(getFullReceipt(html), "utf-8").replaceAll("\\+", " "),
                        "text/html", "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        askForPermissions();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode != permissionsRequestCode){
            Log.e("MAK", "Permission result code unexpected.. Weird.");
        }

        askForPermissions();
    }

    private void askForPermissions() {
        permissionsRequestCode = (int) (Math.random() * 1000.0);

        int isWriteAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int isReadAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (isReadAllowed != PackageManager.PERMISSION_GRANTED && isWriteAllowed != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please grant permission to write to your files, or sending invoices will fail", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, permissionsRequestCode);
        }
    }

    public static void loadTemplate() throws IOException {
        invoiceTemplate = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(appContext
                .getAssets().open("invoice.htm")));
        String line = br.readLine();
        while (line != null) {
            invoiceTemplate += line + eol;
            line = br.readLine();
        }
    }

    public String getFullReceipt(String previewText) {
        String style = getPart("<!--STARTSTYLE-->", "<!--ENDSTYLE-->");

        String header = getHeader(Utilities.userInfo[0]);
        String custInfo = getCustInfo();
        int indexStart = "<html><body>".length();
        int indexEnd = "</body></html>".length();
        String contents = previewText.substring(indexStart, previewText.length() - indexEnd);
        String greeting = "<p>" + Utilities.userInfo[1].replace("\n", "<br/>")
                + "</p>";
        receipt = "<html><body>" + style + header + custInfo + contents
                + greeting + getLogo() + "</body></html>";
        receipt = receipt.replace("INVTITLE",
                Utilities.userInfo[7].toUpperCase()).replace("Invtitle",
                Utilities.userInfo[7]);
        return receipt;
    }

    /*
    public static String getFullReceipt(String[] cmp, ArrayList<Product> pList,
            ArrayList<Product> pType, Context c) {
        appContext = c;
        String style = getPart("<!--STARTSTYLE-->", "<!--ENDSTYLE-->");
        String header = getHeader(Utilities.userInfo[0],c);
        String custInfo = getCustInfo();
        String contents = getContents(pList, pType);
        String greeting = "<p>" + Utilities.userInfo[1].replace("\n", "<br/>")
                + "</p>";
        receipt = "<html><body>" + style + header + custInfo + contents
                + greeting + getLogo() + "</body></html>";
        receipt = receipt.replace("INVTITLE",
                Utilities.userInfo[7].toUpperCase()).replace("Invtitle",
                Utilities.userInfo[7]);
        return receipt;
    }
*/
    public static String getLogo() {
        if (Utilities.userInfo[4].equals("false")) {
            return "";
        }

        File file = new File(Utilities.basePath + "bcard.b64");
        byte[] data = new byte[(int) file.length()];
        try {
            new FileInputStream(file).read(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("Invoicer", "Logo load complete.");
        return "<img alt=\"Business Card\" src=\"data:image/png;base64,"
                + new String(data) + "\" />";
    }

    private String getHeader(String cmp) {
        Date today = new Date();
        String header = getPart("<!--STARTHEADER-->", "<!--ENDHEADER-->");
        header = header.replace("NUMBER", getID());
        header = header.replace("DATE", String.format("%tB %<te, %<tY", today));
        header = returnAlignedText(header, cmp);

        return header;
    }

    private static String getCustInfo() {
        String header = getPart("<!--STARTCUSTINFO-->", "<!--ENDCUSTINFO-->");
        header = header.replace("Customer", Utilities.customerInfo[0]);
        header = header.replace("Address",
                Utilities.customerInfo[2].replace(eol, "<br/>"));
        header = header.replace("Phone", Utilities.customerInfo[1]);
        return header;
    }

    private static String returnAlignedText(String header, String companyName) {
        int nLineLimit, nLinesInput = 0, nLinesRequired = 0, nC = 0;
        String cmp = companyName.replace(eol, "<br/>");
        header = header.replace("CMP_NAME", cmp);
        int nIndexLLChar = header.indexOf("LINES") - 1;
        try {
            nLineLimit = Integer.parseInt(Character.toString(header
                    .charAt(nIndexLLChar)));
        } catch (NumberFormatException f) {
            Log.e("Invoicer",
                    "No line limit character! "
                            + header.substring(nIndexLLChar - 5, nIndexLLChar));
            return header;
        }
        int index = companyName.indexOf(eol);
        while (index != -1) {
            companyName = companyName.substring(index + 1);
            index = companyName.indexOf(eol);
            nLinesInput += 1;
        }
        nLinesRequired = nLineLimit - nLinesInput;
        String beginning = header.substring(0, nIndexLLChar), end = header
                .substring(nIndexLLChar + "LINES".length() + 1);

        for (nC = 0; nC < nLinesRequired; nC++) {
            beginning += "<br/>";
        }

        return beginning + end;
    }


    //getPreview and getContents used by Step2, must remain static
    public static String getPreview(ArrayList<Product> pList,
                                    ArrayList<Product> pType, Context c) {
        appContext = c;
        return "<html><body>" + getContents(pList, pType) + "</body></html>";
    }

    private static String getContents(ArrayList<Product> pList,
                                      ArrayList<Product> pType) {
        String beginning = getPart("<!--STARTPRODUCTS-->",
                "<!--STARTCONTENTS-->");
        String ending = getPart("<!--ENDCONTENTS-->", "<!--ENDPRODUCTS-->");
        String template = getPart("<!--STARTCONTENTS-->", "<!--ENDCONTENTS-->");
        String contents = "";
        float subtotal = 0, taxRate = Float.parseFloat(Utilities.userInfo[2]);
        for (Product p : pList) {
            // pType is productType, checking if productType is empty
            Product type = pType.get(Utilities.nList.indexOf(p.type));
            String productType = type.comments;
            if (!productType.equals("")) {
                productType = "/" + productType;
            }
            // check if productCost is int or not
            float productCost = type.amount;
            String productAmount = p.amount + "";
            if (p.amount - ((int) p.amount) == 0) {
                productAmount = ((int) p.amount) + "";
            }

            String productHTML = template;
            productHTML = productHTML.replace("INAME", p.type);
            productHTML = productHTML.replace("IPRICE",
                    Utilities.formatCurr(productCost) + productType);
            productHTML = productHTML.replace("IQTY", productAmount);
            productHTML = productHTML.replace("LPRICE",
                    Utilities.formatCurr(p.amount * productCost));
            productHTML = productHTML.replace("ICMT", p.comments);
            contents += productHTML;
            subtotal += p.amount * productCost;
        }
        float taxDue = 0;
        if (taxDue == 0) {
            taxDue = subtotal * (taxRate / 100);
        } else {
            taxDue = subtotal;// times taxable
            // ending = ending.replace("TXBL", money.format(numerics[3]));
        }
        Log.e("ENDING", ending);
        ending = ending.replace("SBTTL", Utilities.formatCurr(subtotal));

        Log.e("ENDING", ending);
        ending = ending.replace("TXRT", "" + taxRate);


        Log.e("ENDING", ending);
        ending = ending.replace("TXDU", Utilities.formatCurr(taxDue));

        Log.e("ENDING", ending);
        ending = ending.replace("TTL", Utilities.formatCurr(subtotal + taxDue));

        Log.e("ENDING", ending);
        ending = ending.replace("TXNM", Utilities.userInfo[3]);

        Log.e("ENDING", ending);
        return beginning + contents + ending;
    }

    private static String getPart(String a, String b) {
        if (invoiceTemplate == null) {
            try {
                loadTemplate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return invoiceTemplate.substring(
                invoiceTemplate.indexOf(a) + a.length(),
                invoiceTemplate.indexOf(b) + b.length());
    }

    public String getID() {
        int offset = Integer.parseInt(Utilities.userInfo[8]);
        int ID = Utilities.getNextID() + offset;
        DecimalFormat IDForm = new DecimalFormat("#0000");
        return IDForm.format(ID);
    }


    public void back(View v) {
        finish();
    }

    private File writeReceiptToHtml() {
        File f = new File(Utilities.basePath + fileId + ".htm");
        try {
            if (f.exists()) {
                // won't ever happen due to getID...
                Toast.makeText(
                        this,
                        "Invoice #" + fileId + " already exists! Overwriting..",
                        Toast.LENGTH_LONG).show();
            } else {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f);
            fw.write(receipt);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            Log.e("MAK", "Writing file failed: " + e.getMessage());
        }
        return f;
    }

    private File writeReceiptToPdf() {
        File receipt_pdf = new File(Utilities.basePath + fileId + ".pdf");
        try {
            if (!receipt_pdf.exists()) {
                receipt_pdf.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(receipt_pdf);
            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, out);

            document.addAuthor("");
            document.addCreator("");
            document.addSubject("");
            document.addCreationDate();
            document.addTitle("Invoicer");

            //open document
            document.open();


            StringReader in = new StringReader(receipt);
            //get the XMLWorkerHelper Instance
            XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
            //convert to PDF
            worker.parseXHtml(pdfWriter, document, in);
            //close the document
            document.close();
            //close the writer
            pdfWriter.close();

        } catch (IOException e) {
            Log.e("MAK", "File write failed: " + e.toString());

        } catch (DocumentException e) {
            Log.e("MAK", "Pdf creation failed: " + e.toString());

        } catch (RuntimeWorkerException e) {
            Log.e("MAK", "Pdf creation failed: " + e.toString());
        }
        return receipt_pdf;
    }

    private final String fileId = getID();

    public void send(View v) {
        writeReceiptToHtml();
        Uri uri = Uri.fromFile(writeReceiptToPdf());

        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        String subject, body;
        subject = Utilities.userInfo[7].toUpperCase().charAt(0)
                + Utilities.userInfo[7].substring(1) + " #" + fileId
                + " for ";
        body = this.getString(R.string.pv_body)
                .replace("GREETING", Utilities.userInfo[1])
                .replace("invoice", Utilities.userInfo[7].toLowerCase());
        String associatedInfo = "";
        if (!Utilities.customerInfo[0].equals("")) {
            associatedInfo = Utilities.customerInfo[0];
        } else if (!Utilities.customerInfo[1].equals("")) {
            associatedInfo = Utilities.customerInfo[1];
        } else if (!Utilities.customerInfo[2].equals("")) {
            associatedInfo = Utilities.customerInfo[2];
        }

        if (!associatedInfo.equals("")) {
            subject += associatedInfo;
            body = body.replace("the customer", associatedInfo);
        } else {
            subject += "customer";
        }
        body += "\n" + Utilities.userInfo[0];

        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);

        this.startActivity(emailIntent);
        createNew("Invoice completed! ");

    }

    AlertDialog d;
    boolean isCompleted = false;

    @Override
    public void onResume() {
        super.onResume();
        if (isCompleted) {
            d.show();
        }
    }

    public void clear(View v) {
        createNew("");
        d.show();
    }

    public void createNewHelper() {
        Intent i = new Intent(this, Step1.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(i);
        finish();
    }

    public void createNew(String appendBeginning) {
        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                createNewHelper();
            }
        });

        builder.setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        });
        d = builder.create();
        d.setMessage(appendBeginning
                + "Would you like to create a new invoice?\nNote: You will lose any unsaved/unsent data if you choose YES.");
        isCompleted = true;

    }
}
