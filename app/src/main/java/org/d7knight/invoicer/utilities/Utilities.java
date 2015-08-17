package org.d7knight.invoicer.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Utilities {
	final public static int nFields = 9;
	private final static String eol = System.getProperty("line.separator");
	final public static String basePath = Environment
			.getExternalStorageDirectory() + "/siminvoices/";
	final public static String priceListPath = basePath + "pricelist.txt";
	final public static String userInfoPath = "companyinfo.txt";
	public static String[] userInfo;
	public static String[] customerInfo;
	private static ArrayList<Product> pList;
	public static ArrayList<String> nList = new ArrayList<String>();// for
	final private static String separator = "!@#$%^&*()_+"; // spinner,

	public static void writeLogoFile(InputStream in) {
		FileOutputStream out;
		File f = new File(Utilities.basePath + "bcard.b64");
		if (f.exists())
			f.delete();

		try {
			f.createNewFile();
			out = new FileOutputStream(f);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = in.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();

			byte[] converted = android.util.Base64.encode(buffer.toByteArray(),
					android.util.Base64.DEFAULT);

			out.write(converted);
			out.flush();
			out.close();
			buffer.close();
		} catch (IOException e) {
			Log.e("Invoicer", "Error creating file.");
		}
	}

	public static int loadUserInfo(Context c) throws Exception {
		ArrayList<String> fileContents = new ArrayList<String>();
		BufferedReader br;

		br = new BufferedReader(new InputStreamReader(
				c.openFileInput(userInfoPath)));

		String nextLine = br.readLine();
		while (nextLine != null) {
			fileContents.add(nextLine);
			nextLine = br.readLine();
		}
		br.close();

		String[] info = new String[nFields];
		for (int i = 0; i < nFields; i++) {
			info[i] = "";
		}

		int nC = 0;
		for (String f : fileContents) {
			if (!f.equals(separator)) {
				info[nC] += f + eol;
			} else {
				info[nC] = info[nC].trim();
				nC += 1;
			}
		}
		userInfo = info;
		return nC;
	}

	public static int getNextID() {
		int nNewID = 0;

		File rDir = new File(Utilities.basePath);
		rDir.mkdirs();
		File[] rDirList = rDir.listFiles();
		for (int nCounter = 0; nCounter < rDirList.length; nCounter++) {
			String sFileName = rDirList[nCounter].getName();
			if (sFileName.contains(".htm")){
				int id = -1;
				try{
					id = Integer.parseInt(sFileName.substring(0, sFileName.indexOf(".htm")));
				} catch (NumberFormatException nfe){}
				if (id > nNewID){nNewID = id; }
			}
		}
		
		return nNewID+1;
	}

	public static String formatCurr(float f) {
		DecimalFormat df = new DecimalFormat("#0.00");
		int currSym = userInfo[5].charAt(0);
		if (userInfo[6].equals("false")) {
			return df.format(f) + "&#" + currSym + ";";
		} else {
			return "&#" + currSym + ";" + df.format(f);
		}
	}

	public static String formatCurrp(float f) {
		DecimalFormat df = new DecimalFormat("#0.00");
		String currSym = userInfo[5];
		if (userInfo[6].equals("false")) {
			return df.format(f) + currSym;
		} else {
			return currSym + df.format(f);
		}
	}

	public static void writeUserInfo(String[] info, Context c)
			throws IOException {
		File f = c.getFileStreamPath(userInfoPath);
		if (!f.exists()) {
			f.createNewFile();
		}

		String output = "";
		for (int n = 0; n < info.length; n++) {
			output += info[n] + eol + separator + eol;
		}
		BufferedWriter out = null;
		out = new BufferedWriter(new OutputStreamWriter(c.openFileOutput(
				userInfoPath, Context.MODE_PRIVATE)));

		out.write(output);
		out.flush();
		out.close();
		userInfo = info;
	}

	public static ArrayList<Product> getPriceList(Context c) {
		if (pList == null) {
			BufferedReader br = null;
			pList = new ArrayList<Product>();
			try {
				br = new BufferedReader(new FileReader(priceListPath));
			} catch (FileNotFoundException e) {
				return pList;
			}

			String sNextLine = "";
			try {
				sNextLine = br.readLine();
			} catch (Exception e) {
				sendErrorLog(e, c);
			}

			while (sNextLine != null) {
				Product p = new Product();

				// name
				p.type = sNextLine.substring(sNextLine.indexOf('"') + 1,
						sNextLine.lastIndexOf('"'));
				sNextLine.replace(p.type, "");

				// unit
				if (!(sNextLine.indexOf('\'') == -1 || sNextLine
						.lastIndexOf('\'') == -1)) {
					p.comments = sNextLine.substring(
							sNextLine.indexOf('\'') + 1,
							sNextLine.lastIndexOf('\''));
					sNextLine.replace(p.comments, "");
				}
				// price
				try {
					p.amount = Float.parseFloat(sNextLine.substring(
							sNextLine.indexOf('(') + 1,
							sNextLine.lastIndexOf(')')));
				} catch (Exception e) {
					sendErrorLog(e, c);
				}

				if (!p.type.equals("")) {
					pList.add(p);
					nList.add(p.type);

				}
				try {
					sNextLine = br.readLine();
				} catch (IOException ex) {
					sendErrorLog(ex, c);
				}
			}

			try {
				br.close();
			} catch (IOException e) {
				sendErrorLog(e, c);
			}

		}
		return pList;
	}

	public static void writePriceList(ArrayList<Product> plist)
			throws IOException {
		String output = "";
		nList = new ArrayList<String>();
		Iterator<Product> i = plist.iterator();
		while (i.hasNext()) {
			Product p = i.next();
			nList.add(p.type);
			output += '"' + p.type + '"' + " - ";
			String unit = p.comments;
			if (unit != null && !unit.equals("")) {
				output += "'" + unit + "'" + " - ";
			}
			output += "(" + p.amount + ")" + eol;
		}
		File f = new File(priceListPath);
		if (!f.exists()) {
			f.createNewFile();
		}
		BufferedWriter out = null;
		out = new BufferedWriter(new FileWriter(f, false));
		out.write(output);
		out.flush();
		out.close();

	}

	public static void sendErrorLog(Exception e, Context context) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		String error = baos.toString();
		Toast.makeText(
				context,
				"An error has occurred. Preparing to send error log..." + error,
				Toast.LENGTH_LONG).show();
		AlertDialog.Builder build = new Builder(context);
		AlertDialog d = build.create();

		d.setMessage(error);
		d.setButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();

			}
		});
		d.show();

		return;/*
				 * Intent emailIntent = new
				 * Intent(android.content.Intent.ACTION_SEND);
				 * emailIntent.setType("plain/text");
				 * emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new
				 * String[] { "makbusiness123@gmail.com" });
				 * emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				 * "ERROR LOG FOR " + context.getString(R.string.app_name));
				 * emailIntent .putExtra(android.content.Intent.EXTRA_TEXT,
				 * baos.toString()); context.startActivity(emailIntent);
				 */
	}

}
