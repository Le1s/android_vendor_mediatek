package com.hesine.nmsg.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.http.conn.ConnectTimeoutException;

import com.hesine.nmsg.interfacee.Pipe;

import android.os.AsyncTask;
import android.util.Log;

public class HttpCore extends AsyncTask<Object, Object, Object> {

    private static final int 	ConnectTimeout 	= 15000;
    private static final int 	ReadTimeout 	= 5000 * 10;
    public static final int 	BUFFER_SIZE 	= 2 * 1024;
    public static final byte[]  BUFFER 			= new byte[BUFFER_SIZE];
	private static final int	RETRY_TIME_LIMIT = 3;

    private HttpURLConnection 	connection 		= null;
	private InputStream 		inputStream 	= null;
	private OutputStream 		outputStream 	= null;
	private ByteArrayInputStream byteInputStream = null;
	private boolean 			httpRuning 		= false;
	private RequestTask 		task 			= null;
	private Pipe 				listener 		= null;
	private int					retryCount		= RETRY_TIME_LIMIT; 

	public static final int 	CODE_SUCCESS 	= 1;
    public static final int 	CODE_FAILED 	= 0;
    public static final int 	CODE_TIMEOUT 	= -1;
	private int 				code 			= CODE_SUCCESS;

	public HttpCore() {
	}

	public void start(RequestTask task, Pipe listener) {
		this.task 		= task;
		this.listener 	= listener;
		execute();
	}

	public RequestTask getTask() {
		return task;
	}
	
	void get(RequestTask task){
		try {
			retryCount --;
			code = CODE_SUCCESS;
	        URL url = new URL(task.getUrl());
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Accept", "*/*");
//			connection.setRequestProperty("Connection", "keep-alive");
			connection.setRequestProperty("Connection", "close");
			if(task.isUseGZip()) {
				connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			}
			connection.setConnectTimeout(ConnectTimeout);	        
			String contentEncoding = connection.getContentEncoding();
	        if(contentEncoding != null && contentEncoding.equals("gzip") ){
	        	inputStream 	= new GZIPInputStream(connection.getInputStream());
	        } else {
	            inputStream = new BufferedInputStream(connection.getInputStream());
	        }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int bytesRead = 0;
            while ((bytesRead = inputStream.read(BUFFER, 0, BUFFER_SIZE)) != -1) {
                byteArrayOutputStream.write(BUFFER, 0, bytesRead);
                byteArrayOutputStream.flush();
            }
            inputStream.close();
            inputStream = null;
            task.setParseData(byteArrayOutputStream.toByteArray());
		} catch (SocketTimeoutException e) {
            code = CODE_TIMEOUT;
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            code = CODE_TIMEOUT;
            e.printStackTrace();
        } catch (Exception e) {
            code = CODE_FAILED;
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (connection != null) {
                	connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        	if((code == CODE_FAILED || code == CODE_TIMEOUT) && retryCount > 0) {
        		get(task);
        	} else {
                retryCount = RETRY_TIME_LIMIT;
        	}
        }
	}
	
	void post(RequestTask task) {
		retryCount --;
		code = CODE_SUCCESS;
		inputStream 	= null;
		outputStream 	= null;
		byteInputStream = null;
		try {
			URL url = new URL(task.getUrl());
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Accept", "*/*");
//			connection.setRequestProperty("Connection", "keep-alive");
			connection.setRequestProperty("Connection", "close");
			if(task.isUseGZip()) {
				connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			}
			connection.setFixedLengthStreamingMode(task.getConstructData().length);
			connection.setConnectTimeout(ConnectTimeout);
			connection.setReadTimeout(ReadTimeout);
			connection.connect();
	        outputStream = connection.getOutputStream();

	        byteInputStream = new ByteArrayInputStream(task.getConstructData());

            int bytesRead = 0;
            while ((bytesRead = byteInputStream.read(BUFFER, 0, BUFFER_SIZE)) != -1) {
                outputStream.write(BUFFER, 0, bytesRead);
                outputStream.flush();
            }
            byteInputStream.close();
            byteInputStream = null;
            outputStream.close();
            outputStream = null;
            String contentEncoding = connection.getContentEncoding();
	        if(contentEncoding != null && contentEncoding.equals("gzip") ){
	        	inputStream = new GZIPInputStream(connection.getInputStream());
	        } else {
	            inputStream = new BufferedInputStream(connection.getInputStream());
	        }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            while ((bytesRead = inputStream.read(BUFFER, 0, BUFFER_SIZE)) != -1) {
                byteArrayOutputStream.write(BUFFER, 0, bytesRead);
                byteArrayOutputStream.flush();
            }
            inputStream.close();
            inputStream = null;
            task.setParseData(byteArrayOutputStream.toByteArray());
		} catch (SocketTimeoutException e) {
            code = CODE_TIMEOUT;
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            code = CODE_TIMEOUT;
            e.printStackTrace();
        } catch (Exception e) {
            code = CODE_FAILED;
            e.printStackTrace();
        } finally {
            try {
                if (byteInputStream != null) {
                    byteInputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (connection != null) {
                	connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        	if((code == CODE_FAILED || code == CODE_TIMEOUT) && retryCount > 0) {
        		post(task);
        	} else {
                retryCount = RETRY_TIME_LIMIT;
        	}
        }
	}

	protected void cancel() {
		if (httpRuning && byteInputStream != null && connection != null) {
			try {
                if (byteInputStream != null) {
                    byteInputStream.close();
                    byteInputStream = null;
                }

                if (outputStream != null) {
                    outputStream.close();
                    outputStream = null;
                }

                if (inputStream != null) {
                    inputStream.close();
    				inputStream = null;
                }

                if (connection != null) {
                	connection.disconnect();
    				connection = null;
                }
				this.cancel(true);
				this.onCancelled();
			} catch (Exception e) {
				Log.d("LOG", "[HTTP]Exception:" + e.getMessage());
			}
		}
	}

	protected void onCancelled(Object result) {
		super.onCancelled();
		httpRuning = false;
		listener.complete(task, this, 1);
	}

	@Override
	protected Object doInBackground(Object... params) {
		if (isCancelled()) {
			return null;
		}
		if (task.getMethod().equals("POST")) {
			post(task);
		} else if(task.getMethod().equals("GET")){ 
			get(task);
		} else {
		    // if need other method like "DELETE" etc. 
		}
		return null;
	}

	@Override
	protected void onPreExecute() {
		httpRuning = true;
	}

	@Override
	protected void onProgressUpdate(Object... values) {
	}

	@Override
	protected void onPostExecute(Object result) {
		cancel();
		httpRuning = false;
		int ret = Pipe.NET_SUCCESS;
		if(code == CODE_FAILED) {
			ret = Pipe.NET_FAIL;
		} else if (code == CODE_TIMEOUT) {
			ret = Pipe.NET_TIMEOUT;
		}
		listener.complete(task, this, ret);	
	}
}
