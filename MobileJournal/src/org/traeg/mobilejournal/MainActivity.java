package org.traeg.mobilejournal;

import java.util.ArrayList;

import org.traeg.mobilejournal.R;
import org.traeg.mobilejournal.adapter.JournalEntryListAdapter;
import org.traeg.mobilejournal.model.JournalEntry;
import org.traeg.mobilejournal.model.JournalModel;
import org.traeg.mobilejournal.task.GetJournalEntriesTask;
import org.traeg.mobilejournal.task.GetJournalEntriesTask.GetJournalEntriesResult;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

/*
 * Peter Traeg - Universal Mind - 2013
 * For Snow*Mobile 2013 - Consuming REST Services in Android
 */

public class MainActivity extends Activity implements GetJournalEntriesResult {

	ProgressBar loadingProgress;
	ListView journalListView;
	Button addNewButton;
	
	JournalModel journalModel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	// Load menu options
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	// Respond to menu selections
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_refresh:
	            this.loadEntries();
	            return true;
	        case R.id.menu_add:
				showDetails(-1);  // Passing -1 signifies that we want to add an entry
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	// When the activity starts
	@Override
	public void onStart() {
    	super.onStart();
    	
    	// The following turns off connection pooling - Problems have been observed on some devices 
    	// like the Galaxy Nexus running Android 4.1 if pooling is enabled.  
    	// This must be called before your first HTTP request.
    	System.setProperty("http.keepAlive", "false");
    	
    	journalModel = JournalModel.getInstance();
    	loadingProgress = (ProgressBar)this.findViewById(R.id.loadingProgress);
    	
    	journalListView = (ListView)this.findViewById(R.id.journalListView);
		journalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	onListItemClick(parent, view, position, id);
            }
        });
		
		addNewButton = (Button)this.findViewById(R.id.addNew);
		addNewButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDetails(-1);  // Passing -1 signifies that we want to add an entry
			}
		});

		this.loadEntries();
	}

	// When the activity resumes - after pausing
	@Override
	public void onResume() {
    	super.onResume();
		Log.d(getPackageName(), "Resuming...");
	}
	
	
	// ------------------------------
	// Private members
	// ------------------------------
	private void loadEntries() {
		Log.d(getPackageName(), "Fetching Entries...");
		this.loadingProgress.setVisibility(View.VISIBLE);		
		GetJournalEntriesTask getEntries = new GetJournalEntriesTask(this);
		getEntries.execute("");
	}
	
	private void displayEntries() {
		JournalEntryListAdapter journalEntryAdapter = new JournalEntryListAdapter(this, this.journalModel.journalEntries);
		this.journalListView.setAdapter(journalEntryAdapter);
	}
	
	private void showDetails(int position) {
		Intent detailIntent = new Intent();
		detailIntent.setClass(this, DetailActivity.class);
		detailIntent.putExtra("entryPosition", position);
		startActivity(detailIntent);		
	}

	
	// ------------------------------
	// ListView Delegates
	// ------------------------------
	public void onListItemClick( AdapterView<?>  parent, View view, int position, long id )
	{
		showDetails( position );
	}

	
	// ------------------------------
	// GetJournalEntriesResult
	// ------------------------------
	@Override
	public void onAllEntriesLoaded(ArrayList<JournalEntry> entries) {
		this.journalModel.journalEntries = entries;
		this.loadingProgress.setVisibility(View.INVISIBLE);	
		this.displayEntries();
	}

	@Override
	public void onAllEntriesLoadFailure(Exception e) {
		this.loadingProgress.setVisibility(View.INVISIBLE);		
		AlertDialog dialog;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(String.format("Failure: %s", e != null ? e.getMessage() : ""))
    			.setTitle("Entries Failed To Load")
    			.setCancelable(false)
    			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.dismiss();
    	           }
    	       });
    	dialog = builder.create();
    	dialog.show();
	}

}
