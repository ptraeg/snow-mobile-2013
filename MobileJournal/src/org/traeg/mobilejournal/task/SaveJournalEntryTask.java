package org.traeg.mobilejournal.task;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import org.traeg.mobilejournal.model.JournalEntry;
import org.traeg.mobilejournal.model.MongoTypeAdapter;
import android.util.Log;

import com.google.mygson.Gson;

public class SaveJournalEntryTask extends BaseTask<JournalEntry, Void, JournalEntry> {
	
	private Exception failureException;

	// Interface for Success / Failure
	public interface SaveJournalEntryResult
	{
		public void onEntrySaved( JournalEntry entry );

		public void onEntrySaveFailure(Exception e);
	}

	protected final WeakReference<SaveJournalEntryResult> listener;

	// Constructor to pass success / failure listener
	public SaveJournalEntryTask( SaveJournalEntryResult listener )
	{
		this.listener = new WeakReference<SaveJournalEntryResult>( listener );
	}
	
	@Override
	protected JournalEntry doInBackground(JournalEntry... entries) {
		
		try {
			Boolean addingEntry = false;
			JournalEntry entryToWrite = entries[0];
			if (entryToWrite.id == null) {
				addingEntry = true;
			}
			
			URL url;
			if (addingEntry) {
				url = new URL(String.format("%s/entries?apiKey=%s", BaseTask.BASE_URL, BaseTask.API_KEY));
			} else {
				url = new URL(String.format("%s/entries/%s?apiKey=%s", BaseTask.BASE_URL, entryToWrite.id.value,  BaseTask.API_KEY));
			}
			
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			// If you have not disabled the connection pool on an application wide basis via System.setProperty("http.keepAlive", "false") 
			// you can do so for a given request by using a line such as the following.  Note that this will close the 
			// connection _after_ the request is made, thus preventing it from going back to the connection pool for later reuse.
			// urlConnection.setRequestProperty("Connection", "close"); //disables connection reuse
			
			try {

				Gson gson = MongoTypeAdapter.getGsonBuilder().create();
				String json = gson.toJson(entryToWrite);
				
				urlConnection.setUseCaches(false);
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod(addingEntry ? "POST" : "PUT");
				urlConnection.setFixedLengthStreamingMode(json.getBytes().length);
				urlConnection.setRequestProperty("Content-Type", "application/json");
//				urlConnection.setChunkedStreamingMode(0);
//				urlConnection.setRequestProperty("Content-Length", "" + 
//			               Integer.toString(json.getBytes().length));
				
				OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
				
				byte[] jsonBytes  = json.getBytes();
				out.write(jsonBytes);
				out.flush();
				
				int http_status = urlConnection.getResponseCode();
			    if (http_status != HttpURLConnection.HTTP_OK) {
			      throw new Exception("Failed - response code: " + http_status);
			    }
				
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				InputStreamReader streamReader = new InputStreamReader(in);

				JournalEntry updatedEntry = gson.fromJson(streamReader, JournalEntry.class);
				out.close();
				streamReader.close();
				in.close();
				
				
				return updatedEntry;
			     
			} catch (Exception ex) {
				Log.e("MJ", String.format( "Stream failed - %s", ex.toString() ) );
				failureException = ex;
			
			} finally {
			     urlConnection.disconnect();
			}

			
		} catch (Exception ex) {
			Log.e("MJ", String.format( "Save failed - %s", ex.toString() ) );
			failureException = ex;
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(JournalEntry entry) {
		if (this.listener != null) {
			if (entry != null) {
				this.listener.get().onEntrySaved(entry);
			} else {
				this.listener.get().onEntrySaveFailure(failureException);
			}
		}
	}
	

}
