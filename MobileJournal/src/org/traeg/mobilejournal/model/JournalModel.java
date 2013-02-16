package org.traeg.mobilejournal.model;

import java.util.ArrayList;

public class JournalModel extends BaseModel {
	
	public ArrayList<JournalEntry> journalEntries;
	
	private static JournalModel journalModel;
	
	public static JournalModel getInstance() {
		if (journalModel == null) {
			journalModel = new JournalModel();
		}
		return journalModel;		
	}
	
}
