package org.traeg.mobilejournal.model;

import java.util.Date;

import com.google.mygson.annotations.SerializedName;


public class JournalEntry extends BaseModel {
	
	@SerializedName("_id")
	public MongoObjectId id;
	
	public int categoryId;
	
	public String title;
	
	public String notes;
	
	public Date updatedDate;
	
	public JournalEntry() {
		this.title = "";
		this.notes = "";
	}
	
}
