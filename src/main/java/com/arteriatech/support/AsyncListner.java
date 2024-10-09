package com.arteriatech.support;

import java.io.IOException;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

public class AsyncListner implements AsyncListener {

	@Override
	public void onComplete(AsyncEvent asynchContext) throws IOException {
		//asynchContext.getAsyncContext().getResponse().getWriter().println("Sending email Completed");
		
	}

	@Override
	public void onError(AsyncEvent asynchContext) throws IOException {
		asynchContext.getAsyncContext().getResponse().getWriter().println("Error Occured while Sending the email");
		
	}

	@Override
	public void onStartAsync(AsyncEvent asyncContext) throws IOException {
		asyncContext.getAsyncContext().getResponse().getWriter().println("asynch Process Started");
		
	}

	@Override
	public void onTimeout(AsyncEvent asynchEvent) throws IOException {
		
		asynchEvent.getAsyncContext().getResponse().getWriter().println("Request has time-out");
		
	}

}
