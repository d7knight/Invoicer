package org.d7knight.invoicer.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.d7knight.invoicer.utilities.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;

public class ViewHTM extends AppCompatActivity {

	static Context appContext;
	String invoiceNumber;


    public void create(View v){
        Intent i = new Intent(this,Step1.class);
        startActivity(i);
        finish();
    }
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		appContext = this;
		setContentView(R.layout.viewerhtml);
		// Set a toolbar to replace the action bar.
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		invoiceNumber = this.getIntent().getStringExtra("HTML");
		String file = "Error loading invoice.";
		try {
			file = readFile(Utilities.basePath
					+ invoiceNumber + ".htm");
		} catch (IOException e1) {
		}
		WebView wv = (WebView) findViewById(R.id.viewer_html);
		wv.loadData(file, "text/html", "utf-8");
	}

	private static String readFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}
	@Override
	public void onPause() {
		super.onPause();
	}

	public void send(View v) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Resending invoice #" + invoiceNumber);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				"Hello,\nPlease find invoice #" + invoiceNumber
						+ " attached to this email.\n\n"
						+ Utilities.userInfo[1]);
		emailIntent.putExtra(
				android.content.Intent.EXTRA_STREAM,
				Uri.fromFile(new File(Utilities.basePath + invoiceNumber
						+ ".htm")));
		startActivity(emailIntent);
	}

}
