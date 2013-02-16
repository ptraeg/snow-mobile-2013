package org.traeg.mobilejournal.task;

import android.os.AsyncTask;

public abstract class BaseTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	//TODO: Hide API_KEY before distribution of code
	protected static final String API_KEY = "<your-api-key-here>";
	protected static final String BASE_URL = "https://api.mongolab.com/api/1/databases/mobilejournal/collections";

}
