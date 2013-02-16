package org.traeg.mobilejournal.task;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import org.traeg.mobilejournal.model.JournalEntry;
import org.traeg.mobilejournal.model.MongoTypeAdapter;

import com.google.mygson.Gson;

import android.util.Log;

public class DeleteJournalEntryTask extends BaseTask<JournalEntry, Void, JournalEntry> {

	private Exception failureException;

	// Interface for Success / Failure
	public interface DeleteJournalEntryResult
	{
		public void onEntryDeleted(JournalEntry deletedEntry );

		public void onEntryDeleteFailure(Exception e);
	}

	protected final WeakReference<DeleteJournalEntryResult> listener;

	// Constructor to pass success / failure listener
	public DeleteJournalEntryTask( DeleteJournalEntryResult listener )
	{
		this.listener = new WeakReference<DeleteJournalEntryResult>( listener );
	}
	
	@Override
	protected JournalEntry doInBackground(JournalEntry... entries) {
		
		try {
			JournalEntry entryToDelete = entries[0];
			
			URL url;
			url = new URL(String.format("%s/entries/%s?apiKey=%s", BaseTask.BASE_URL, entryToDelete.id.value,  BaseTask.API_KEY));
			
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			
			try {

				urlConnection.setRequestMethod("DELETE");
				
				Gson gson = MongoTypeAdapter.getGsonBuilder().create();
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				InputStreamReader streamReader = new InputStreamReader(in);
				JournalEntry deletedEntry = gson.fromJson(streamReader, JournalEntry.class);
				streamReader.close();
				in.close();

				return deletedEntry;
			     
			} catch (Exception ex) {
				Log.e("Delete stream failed - %s", ex.getMessage());
				failureException = ex;
			
			} finally {
			     urlConnection.disconnect();
			}

			
		} catch (Exception ex) {
			Log.e("Delete failed - %s", ex.getMessage());
			failureException = ex;
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(JournalEntry deletedEntry) {
		if (this.listener != null) {
			if (failureException != null) {
				this.listener.get().onEntryDeleteFailure(failureException);
			} else {
				this.listener.get().onEntryDeleted(deletedEntry);
			}
		}
	}
	
}
