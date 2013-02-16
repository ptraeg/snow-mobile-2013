package org.traeg.mobilejournal.model;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.util.Log;

import com.google.mygson.GsonBuilder;
import com.google.mygson.JsonDeserializationContext;
import com.google.mygson.JsonDeserializer;
import com.google.mygson.JsonElement;
import com.google.mygson.JsonObject;
import com.google.mygson.JsonParseException;
import com.google.mygson.JsonSerializationContext;
import com.google.mygson.JsonSerializer;

// Based on - http://craigsmusings.com/2011/04/09/deserializing-mongodb-ids-and-dates-with-gson/

public class MongoTypeAdapter {
	public static GsonBuilder getGsonBuilder()
	{		
		GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(MongoObjectId.class, new MongoTypeAdapter.MongoObjectIdDeserializer());
		gb.registerTypeAdapter(MongoObjectId.class, new MongoTypeAdapter.MongoObjectIdSerializer());
		gb.registerTypeAdapter(Date.class, new MongoTypeAdapter.MongoDateDeserializer());
		gb.registerTypeAdapter(Date.class, new MongoTypeAdapter.MongoDateSerializer());
		return gb;
	}
	
	public static class MongoObjectIdDeserializer implements JsonDeserializer<MongoObjectId> 
	{
		@Override
		public MongoObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			try
			{
				return new MongoObjectId(json.getAsJsonObject().get("$oid").getAsString());
			}
			catch (Exception e)
			{
				return null;
			}
		}
	}
	
	public static class MongoObjectIdSerializer implements JsonSerializer<MongoObjectId> 
	{
		@Override
		public JsonElement serialize(MongoObjectId id, Type typeOfT, JsonSerializationContext context)
		{
			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("$oid", id.value);
			return jsonObj;
		}
	}	

	public static class MongoDateDeserializer implements JsonDeserializer<Date> 
	{
		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException
			{
				Date date = null;
	        	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
	        	fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
				try 
				{
					date = fmt.parse(json.getAsJsonObject().get("$date").getAsString());
				}
				catch (ParseException e)	
				{
					Log.e("MJ", "Unable to deserialize date: " + e.toString());
					date = null;
				}
				return date;
			}
	}
	
	public static class MongoDateSerializer implements JsonSerializer<Date> 
	{
		@Override
		public JsonElement serialize(Date date, Type typeOfT, JsonSerializationContext context)
		{
        	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        	fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
	        JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("$date", fmt.format(date));
			return jsonObj;
		}
	}	

	
	
}
