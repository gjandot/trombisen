package fr.gjandot.trombisen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;


public class SenList extends ListActivity
	//ListActivity: pour bénéficier du "empty view"
	//(@android:id/empty)
{
	static final String PREF_VUEH = "VH";
	static final String PREF_VUEF = "VF";
	static final String PREF_FILTRE = "f";
	static final String PREF_NUMGROUPE = "g";
	static final String PREF_DATEMILLIS = "d";
	static final long H4  = 4*3600*1000; /* 4 heures, en ms */

	final List<Senateur> listat = new ArrayList<Senateur>();
	
	private ListeAdapter adapter = null;
	private LinearLayout layout, liste = null;
	private ListView mListView = null;
	private EditText filterText = null;
	private Spinner spinner = null;
	private ImageButton imgbtnH=null, imgbtnF=null, imgclrFilt=null;
	public static boolean vueH = true, vueF = true;
	public static String filtreGroupe;
	private static ArrayList<String> listeGroupes;
	private static Senateur mSenateur = new Senateur();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.listat);

        mListView = (ListView) findViewById(android.R.id.list);
		//mListView.setEmptyView(findViewById(android.R.id.message));

		layout = (LinearLayout) findViewById(R.id.progress);
		liste = (LinearLayout) findViewById(R.id.liste);

        filterText = (EditText) findViewById(R.id.search_box);
        filterText.addTextChangedListener(filterTextWatcher);

		imgclrFilt = (ImageButton) findViewById(R.id.clearfilter);
		imgclrFilt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				filterText.setText("");
				adapter.getFilter().filter(filterText.getText());
			}
		});

		imgbtnH = (ImageButton) findViewById(R.id.btnH);
		imgbtnH.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				vueH = !vueH;
				//ne pas avoir ni hommes ni femmes
				if (!vueH && !vueF)
				{
					vueF = true;
				}
				adapter.getFilter().filter(filterText.getText());
				maj_btns();
			}
		});
		imgbtnF = (ImageButton) findViewById(R.id.btnF);
		imgbtnF.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				vueF = !vueF;
				//ne pas avoir ni hommes ni femmes
				if (!vueF && !vueH)
				{
					vueH = true;
				}
				adapter.getFilter().filter(filterText.getText());
				maj_btns();
			}
		});

		listeGroupes = new ArrayList<String>();
		listeGroupes.add(getResources().getString(R.string.tous));


        adapter = new ListeAdapter(this, listat);
        mListView.setTextFilterEnabled(true);
		mListView.setOnItemClickListener(new OnItemClickListener()
        {
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(view.getTag().toString()));
				startActivity(intent);
				//finish();
			}

        });
		mListView.setAdapter(adapter);


		spinner = (Spinner) findViewById(R.id.spinner);

		ArrayAdapter<String> adp = new ArrayAdapter<String> (this, R.layout.spinner, listeGroupes);
		spinner.setAdapter(adp);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
			{
				filtreGroupe = parentView.getItemAtPosition(position).toString();
				adapter.getFilter().filter(filterText.getText());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				//rien...
			}
		});

		new Task().execute();
    }


	public void charge_donnees()
	{
		try {
			if (android.os.Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
			getsenlist();

		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
	}


	public void getsenlist()
			throws XmlPullParserException, IOException
	{
		String element = new String();
		String SenNom = new String();
		String SenPrenom = new String();
		String SenGrp = new String();
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();

		//xpp.setInput(downloadUrl(), null);
		if (is_date_old()) {
			downloadUrl();
			save_date();
		}
		FileInputStream fis = new FileInputStream(new File(getCacheDir(), "/senateurs.xml"));
		xpp.setInput(fis, null);
		// GESTION d'ERREUR !!!
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			//if(eventType == XmlPullParser.START_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				element = xpp.getName();
			} else if(eventType == XmlPullParser.END_TAG) {
				if (element.equals("twitter"))//dernier élément
				{
					mSenateur.setNom(SenNom + " " + SenPrenom);
					listat.add(new Senateur(mSenateur));
				}
				element = "";
			} else if(eventType == XmlPullParser.TEXT) {
				if (element.equals("nom_de_famille") )
				{
					SenNom = xpp.getText();
				}
				if (element.equals("groupe_sigle") )
				{
					SenGrp= xpp.getText();
					mSenateur.setGrp(xpp.getText());
					if (!(listeGroupes.contains(SenGrp)))
					{
						listeGroupes.add(SenGrp);
					}
				}
				if (element.equals("prenom") )
				{
					SenPrenom = xpp.getText();
				}
				if (element.equals("sexe"))
				{
					mSenateur.setSexe_H(xpp.getText().equals("H"));
				}
				if (element.equals("nom_circo"))
				{
					mSenateur.setCirco(xpp.getText());
				}
				if (element.equals("url_institution")) {
					mSenateur.setSenUrl(xpp.getText());
				}
				if (element.equals("slug")) {
					//SenImgUrl = "http://www.nossenateurs.fr/senateur/photo/" + xpp.getText() + "/64";
					mSenateur.setImgUrl(xpp.getText());
				}
			}
			eventType = xpp.next();
		}
		Collections.sort(listat, new Senateur.SenComparateur());
	}


	private void downloadUrl() throws XmlPullParserException, IOException {

		URL url = new URL(getResources().getString(R.string.url_data));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.connect();
		//GESTION D'ERREUR !!!

		/* sauvegarde du flux xml en cache */
		File cacheDirectory = getBaseContext().getCacheDir();
		File tmpFile = new File(cacheDirectory.getPath() + "/senateurs.xml");
		FileOutputStream out = new FileOutputStream(tmpFile);
		InputStream in = conn.getInputStream();
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len != -1) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
		out.flush();
		out.close();
		in.close();
	}
	
  private TextWatcher filterTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			adapter.getFilter().filter(s);
		}
	};


  @Override
  protected void onDestroy() {
	  super.onDestroy();
	  filterText.removeTextChangedListener(filterTextWatcher);
  }


	/** AsyncTask to download and load an image in ListView */
	private class ImageLoaderTask extends AsyncTask<HashMap<String, Object>, Void, HashMap<String, Object>> {

		@Override
		protected HashMap<String, Object> doInBackground(HashMap<String, Object>... hm) {
			InputStream iStream=null;
			String imgUrl = (String) "";
			int position = (Integer) hm[0].get("position");

			URL url;
			try {
				url = new URL(imgUrl);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.connect();
				iStream = urlConnection.getInputStream();
				File cacheDirectory = getBaseContext().getCacheDir();
				File tmpFile = new File(cacheDirectory.getPath() + "/wpta_"+position+".png");
				FileOutputStream fOutStream = new FileOutputStream(tmpFile);
				Bitmap b = BitmapFactory.decodeStream(iStream);
				b.compress(Bitmap.CompressFormat.PNG, 100, fOutStream);
				fOutStream.flush();
				fOutStream.close();

				HashMap<String, Object> hmBitmap = new HashMap<String, Object>();
				hmBitmap.put("flag", tmpFile.getPath());
				hmBitmap.put("position", position);
				return hmBitmap;

			}catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(HashMap<String, Object> result) {
			String path = (String) result.get("flag");
			int position = (Integer) result.get("position");
			SimpleAdapter adapter = (SimpleAdapter) mListView.getAdapter();
			HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(position);
			hm.put("flag",path);
			adapter.notifyDataSetChanged();
		}
	}

	class Task extends AsyncTask<String, Integer, Boolean> {
		@Override
		protected void onPreExecute() {
			layout.setVisibility(View.VISIBLE);
			liste.setVisibility(View.GONE);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			layout.setVisibility(View.GONE);
			liste.setVisibility(View.VISIBLE);

			/* notification à l'UI de la fin du chargement */
			msg.arg1=1;
			handler.sendMessage(msg);

			super.onPostExecute(result);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			charge_donnees();
			return null;
		}
	}


	/* récepteur de la notification de la fin du chargement des données */
	Message msg = new Message();
	Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if(msg.arg1==1)
			{
				read_prefs();
				adapter.getFilter().filter(filterText.getText());
				adapter.notifyDataSetChanged();
				mListView.invalidate();
			}
			return false;
		}
	});



	void maj_btns() {
		if (vueF) {
			imgbtnF.setImageResource(R.drawable.sexe_f);
		}
		else
		{
			imgbtnF.setImageResource(R.drawable.sexe_nof);
		}
		if (vueH) {
			imgbtnH.setImageResource(R.drawable.sexe_h);
		}
		else
		{
			imgbtnH.setImageResource(R.drawable.sexe_noh);
		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
		read_prefs();
	}

	@Override
	public void onStop() {
		save_prefs();
		super.onStop();
	}

/*	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		save_prefs();
		super.onSaveInstanceState(savedInstanceState);
	}


	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		read_prefs();
}*/


	private void save_prefs()
	{
		SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putBoolean(PREF_VUEH, vueH);
		ed.putBoolean(PREF_VUEF, vueF);
		ed.putString(PREF_FILTRE, filterText.getText().toString());
		ed.putInt(PREF_NUMGROUPE, spinner.getSelectedItemPosition());
		ed.commit();
	}

	private void save_date()
	{
		SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putLong(PREF_DATEMILLIS, System.currentTimeMillis());
		ed.commit();
	}

	private boolean is_date_old()
	{
		SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
		long now = System.currentTimeMillis();
		return (now - mPrefs.getLong(PREF_DATEMILLIS, 0) > H4);
		/*
		if (now- mPrefs.getLong(PREF_DATEMILLIS, 0) > H4) {
			return true;
		}
		return false;*/
	}

	private void read_prefs()
	{
		SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
		vueH = mPrefs.getBoolean(PREF_VUEH, true);
		vueF = mPrefs.getBoolean(PREF_VUEF, true);
		maj_btns();
		filterText.setText(mPrefs.getString(PREF_FILTRE,""));
		spinner.setSelection(mPrefs.getInt(PREF_NUMGROUPE, 0));
		spinner.invalidate();
		adapter.getFilter().filter(filterText.getText());
	}

} //ACTIVITY