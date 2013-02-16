package org.traeg.mobilejournal;

import java.util.Date;

import org.traeg.mobilejournal.model.JournalEntry;
import org.traeg.mobilejournal.model.JournalModel;
import org.traeg.mobilejournal.task.DeleteJournalEntryTask;
import org.traeg.mobilejournal.task.SaveJournalEntryTask;
import org.traeg.mobilejournal.task.DeleteJournalEntryTask.DeleteJournalEntryResult;
import org.traeg.mobilejournal.task.SaveJournalEntryTask.SaveJournalEntryResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class DetailActivity extends Activity implements SaveJournalEntryResult, DeleteJournalEntryResult {

	EditText titleEditText;
	EditText notesEditText;
	
	Button cancelButton;
	Button saveButton;
	Button deleteButton;
	ProgressBar progressIndicator;

	JournalEntry journalEntry;
	JournalModel journalModel;
	
	Boolean isAdding = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_view);
		
    	this.journalModel = JournalModel.getInstance();
		
		Bundle extras = getIntent().getExtras();
    	if (extras != null) {
//			journalEntry = (JournalEntry)extras.getSerializable("journalEntry");
			int entryPosition = extras.getInt("entryPosition");			
			if ( entryPosition >= 0 && entryPosition < journalModel.journalEntries.size() ) {
				journalEntry = journalModel.journalEntries.get(entryPosition);
			} else {
				// Make a new entry
				journalEntry = new JournalEntry();
				isAdding = true;
			}
		}
		
	}

	// When the activity starts
	@Override
	public void onStart() {
    	super.onStart();
    	
    	titleEditText = (EditText)this.findViewById(R.id.titleEditText);
    	notesEditText = (EditText)this.findViewById(R.id.notesEditText);
    	cancelButton = (Button)this.findViewById(R.id.cancelButton);
    	saveButton = (Button)this.findViewById(R.id.saveButton);
    	deleteButton = (Button)this.findViewById(R.id.deleteButton);
    	progressIndicator = (ProgressBar)this.findViewById(R.id.loadingProgress);
    	
    	if (journalEntry != null) {
    		titleEditText.setText(journalEntry.title);
    		notesEditText.setText(journalEntry.notes);    		
    	}
    	
    	cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// End the current activity - returning to the MainActivity (List View)
				finish();
			}
		});
    	
    	saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveEntry();
			}
		});
    	
    	if (isAdding) {
    		deleteButton.setVisibility(View.INVISIBLE);
    	} else {
        	deleteButton.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				onDeleteClicked();
    			}
    		});
    	}
    	
    	
	}
	
	private void saveEntry() {
		journalEntry.title = titleEditText.getText().toString();
		journalEntry.notes = notesEditText.getText().toString();
		journalEntry.updatedDate = new Date();
		progressIndicator.setVisibility(View.VISIBLE);
		SaveJournalEntryTask saveTask = new SaveJournalEntryTask(this);
		saveTask.execute(journalEntry);
	}

	private void onDeleteClicked() {
		AlertDialog dialog;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Really delete this entry?")
    			.setTitle("Confirm Delete")
    			.setCancelable(true)
    			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.dismiss();
    	                deleteEntry();
    	           }
    			})
    			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.dismiss();
    	           }
    			})
    			;
    	dialog = builder.create();
    	dialog.show();
	}
	
	private void deleteEntry() {
		progressIndicator.setVisibility(View.VISIBLE);
		DeleteJournalEntryTask deleteTask = new DeleteJournalEntryTask(this);
		deleteTask.execute(journalEntry);
	}


	// ------------------------------
	// SaveJournalEntryResult
	// ------------------------------
	@Override
	public void onEntrySaved( JournalEntry entry ) {
		progressIndicator.setVisibility(View.INVISIBLE);
		Toast.makeText(getApplicationContext(), "Saved entry", Toast.LENGTH_SHORT).show();
		finish();
	}

	@Override
	public void onEntrySaveFailure(Exception e) {
		progressIndicator.setVisibility(View.INVISIBLE);
		AlertDialog dialog;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(String.format("Failure: %s", e != null ? e.getMessage() : ""))
    			.setTitle("Entries Failed To Save")
    			.setCancelable(false)
    			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.dismiss();
    	           }
    	       });
    	dialog = builder.create();
    	dialog.show();
	}

	// ------------------------------
	// DeleteJournalEntryResult
	// ------------------------------
	@Override
	public void onEntryDeleted(JournalEntry deletedEntry) {
		progressIndicator.setVisibility(View.INVISIBLE);
		Toast.makeText(getApplicationContext(), "Deleted entry", Toast.LENGTH_SHORT).show();
		finish();
	}

	@Override
	public void onEntryDeleteFailure(Exception e) {
		progressIndicator.setVisibility(View.INVISIBLE);
		AlertDialog dialog;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(String.format("Failure: %s", e != null ? e.getMessage() : ""))
    			.setTitle("Entry Failed To Delete")
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
