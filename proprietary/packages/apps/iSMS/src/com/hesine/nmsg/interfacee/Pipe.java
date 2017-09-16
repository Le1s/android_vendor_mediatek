package com.hesine.nmsg.interfacee;

public interface Pipe {
	public static final int MEMORY_SUCCESS 		= 2;
	public static final int LOCAL_SUCCESS 		= 1;
	public static final int NET_SUCCESS 		= 0;
	public static final int PARSE_FAIL 			= -1;	
	public static final int NET_FAIL 			= -2;
	public static final int NET_TIMEOUT 		= -3;
	public static final int SERVER_ERROR 		= -4;	

	public void complete(Object owner, Object data, int success);
}
