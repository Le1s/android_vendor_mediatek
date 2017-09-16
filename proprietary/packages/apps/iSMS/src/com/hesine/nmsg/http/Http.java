package com.hesine.nmsg.http;

import java.util.Vector;

import com.hesine.nmsg.interfacee.Pipe;

public class Http implements Pipe {
	
	public RequestTask post( String url, byte[] constructData, Pipe listener ){
		RequestTask task = new RequestTask( "POST", url, constructData, listener );
		newHttpThread( task );
		return task;
	}
	
	public RequestTask get( String url, Pipe listener ){
		RequestTask task = new RequestTask( "GET", url, null, listener );
		newHttpThread( task );
		return task;
	}
	
	public boolean cancel( RequestTask task ){
		tasks.removeElement( task );
		for( int i = 0; i < https.size(); i++ ){
			HttpCore http = https.elementAt( i );
			if( http.getTask() == task ){
				http.cancel();
				return true;
			}
		}
		return false;
	}
	public void cancelAll(){
		tasks.removeAllElements();
		for( int i = 0; i < https.size(); i++ ){
			HttpCore http = https.elementAt( i );
			http.cancel();
		}
	}
	
	public static Http instance() {
        if( httpengine == null) {
            synchronized ( Http.class) {
                if( httpengine == null) {
                	httpengine = new Http();
                }
            }
        }
        return httpengine;
    }	
	
	private boolean newHttpThread( RequestTask task  ){
		if( httpThreadCount >= maxThreadNum ){
			tasks.addElement( task );
			return false;            
		}else{
			HttpCore _http = new HttpCore();
			_http.start( task, this );
			https.addElement( _http );
			httpThreadCount++;
			return true;
		}
	}
	
	private RequestTask scanTaskQueue(){
		if( tasks.size() > 0 ){
			return tasks.elementAt( 0 );
		}
		return null;
	}
	
	@Override
	public void complete( Object owner, Object data, int success ) {
		RequestTask task = (RequestTask) owner;
		HttpCore http = (HttpCore) data;

		if(task.getListener() != null){
			task.getListener().complete( task, http, success );
		}
		task.setParseData(null);
		
		https.removeElement( http );
		http		= null;
		
		httpThreadCount--;
		RequestTask _task = scanTaskQueue(); 
		if( _task != null && httpThreadCount < maxThreadNum ){
			tasks.removeElement( _task );
			newHttpThread( _task );
		}
	}

	private Vector<RequestTask>   	tasks 			= new Vector<RequestTask>();
	private Vector<HttpCore>   		https 			= new Vector<HttpCore>();
	private int				httpThreadCount 	= 0;
	private int				maxThreadNum		= 3;
	private static Http httpengine		= null;
}
