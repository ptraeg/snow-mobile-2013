package org.traeg.mobilejournal.adapter;

import java.util.List;
import java.text.DateFormat;

import org.traeg.mobilejournal.model.JournalEntry;

import org.traeg.mobilejournal.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class JournalEntryListAdapter extends BaseAdapter {

	private final LayoutInflater layoutInflater;
	private List<JournalEntry> entryList;
	private DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
	
	public JournalEntryListAdapter(Context context, List<JournalEntry> entryList) {
		this.entryList = entryList;
    	layoutInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
	}
	
	@Override
	public int getCount() {
		return this.entryList.size();
	}

	@Override
	public Object getItem(int index) {
		return this.entryList.get(index);
	}

	@Override
	public long getItemId(int index) {
//		return this.entryList.get(index).id;
		//TODO: Fix return value
		return index;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {

		JournalEntry journalEntry = (JournalEntry) getItem(position);

		// A ViewHolder keeps references to children views to avoid calls
		// to findViewById() on each row.  This speeds up getView calls when 
		// we are recycling a View
		ViewHolder viewHolder;

		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.journal_list_row,	null);

			// Creates a ViewHolder and store references to the child views
			// we want to send data to.
			viewHolder = new ViewHolder();
			viewHolder.entryTitle = (TextView) convertView.findViewById(R.id.entryTitle);
			viewHolder.updatedDate = (TextView) convertView.findViewById(R.id.updatedDate);
			convertView.setTag(viewHolder);

		} else {

			// Get the ViewHolder back to get fast access to the children
			viewHolder = (ViewHolder) convertView.getTag();

		}

		// Pass data through efficiently via the holder.
		viewHolder.entryTitle.setText(journalEntry.title);

		if (journalEntry.updatedDate != null) {
			
			viewHolder.updatedDate.setText("Updated: " + dateFormatter.format(journalEntry.updatedDate));

		} else {
		
			viewHolder.updatedDate.setText("");
		}

		return convertView;		
		
	}

	final static class ViewHolder {
    	TextView entryTitle;
   	 	TextView updatedDate;
	}
}
