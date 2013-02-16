package org.traeg.mobilejournal.task;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.traeg.mobilejournal.model.JournalEntry;
import org.traeg.mobilejournal.model.MongoTypeAdapter;

import android.util.Log;

import com.google.mygson.*;
import com.google.mygson.reflect.TypeToken;

public class GetJournalEntriesTask extends
		BaseTask<String, Void, ArrayList<JournalEntry>> {

	// Interface for Success / Failure
	public interface GetJournalEntriesResult {
		public void onAllEntriesLoaded(ArrayList<JournalEntry> entries);

		public void onAllEntriesLoadFailure(Exception e);
	}

	// Note that we are holding a WeakReference to the listener. This allows the activity that we are referencing
	// to be garbage collected. If this was not a weak reference the activity could be held in memory simply because
	// of this reference.  Remember that the activity is also pointing to an instance of this class and the 2-way reference
	// would prevent them from being garbage collected.
	protected final WeakReference<GetJournalEntriesResult> listener;

	// Constructor to pass success / failure listener
	public GetJournalEntriesTask(GetJournalEntriesResult listener) {
		this.listener = new WeakReference<GetJournalEntriesResult>(listener);
	}

	@Override
	protected ArrayList<JournalEntry> doInBackground(String... params) {

		try {

			// URL format -
			// https://api.mongolab.com/api/1/databases/mobilejournal/collections/entries?apiKey=xxxxxxxxx&s={"updatedDate": -1}
			String sortParam = "&s=" + URLEncoder.encode( "{'updatedDate': -1}", "UTF-8" );
			String urlString = String.format("%s/entries?apiKey=%s%s", BaseTask.BASE_URL, BaseTask.API_KEY, sortParam); 			
			URL url = new URL( urlString );
			
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

			try {
				InputStream in = new BufferedInputStream(
						urlConnection.getInputStream());

				// Note how we've namesspaced Gson as com.google.mygson.Gson to avoid namespace conflicts if the device 
				// already has Gson in the public classpath as we've observed on some HTC branded devices.
				Gson gson = MongoTypeAdapter.getGsonBuilder().create();
				InputStreamReader streamReader = new InputStreamReader(in);

				Type listType = new TypeToken<ArrayList<JournalEntry>>() {}.getType();
				ArrayList<JournalEntry> results = gson.fromJson(streamReader, listType);
				streamReader.close();
				in.close();

				return results;

			} catch (Exception ex) {
				Log.e("Stream failed - %s", ex.getMessage());

			} finally {
				urlConnection.disconnect();
			}

		} catch (Exception ex) {
			Log.e("Fetch failed - %s", ex.getMessage());
		}

		return null;
	}

	@Override
	protected void onPostExecute(ArrayList<JournalEntry> entries) {
		if (this.listener != null) {
			if (entries != null) {
				this.listener.get().onAllEntriesLoaded(entries);
			} else {
				this.listener.get().onAllEntriesLoadFailure(null);
			}
		}
	}

}
