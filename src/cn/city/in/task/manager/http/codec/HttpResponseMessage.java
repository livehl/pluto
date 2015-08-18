/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package cn.city.in.task.manager.http.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;

import cn.city.in.api.tools.common.ExceptionTool;

/**
 * A HTTP response message.
 * 
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 555855 $, $Date: 2007-07-13 12:19:00 +0900 (Fri, 13 Jul 2007)
 *          $
 */
public class HttpResponseMessage {
	/** HTTP response codes */
	public static final int HTTP_STATUS_SUCCESS = 200;

	public static final int HTTP_STATUS_NOT_FOUND = 404;

	private static final String NOT_FOUND_STRING = "<h1>404</br>请求的地址不存在</h1>";

	public static final int HTTP_STATUS_ERROR = 500;

	private static final String ERROR_STRING = "<h1>500</br>服务器内部错误</h1>";

	private static final int HTTP_STATUS_FORBIDDEN = 403;
	private static final String FORBIDDEN_STRING = "<h1>403</br>禁止访问</h1>";

	/** Map<String, String> */
	private Map headers = new HashMap();

	/** Storage for body of HTTP response. */
	private ByteArrayOutputStream body = new ByteArrayOutputStream(1024);

	private int responseCode = HTTP_STATUS_SUCCESS;

	public HttpResponseMessage() {
		headers.put("Server", "Pluto v1.00");
		headers.put("Cache-Control", "private");
		headers.put("Content-Type", "text/html; charset=utf-8");
		headers.put("Connection", "keep-alive");
		headers.put("Keep-Alive", "200");
		headers.put("Date", new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date()));
		headers.put("Last-Modified", new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date()));
	}

	public void appendBody(byte[] b) {
		try {
			body.write(b);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void appendBody(String s) {
		try {
			body.write(s.getBytes());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 设置500
	 * 
	 * @return the string
	 * @author 黄林
	 */
	public String error(Throwable th) {
		setResponseCode(HTTP_STATUS_ERROR);
		appendBody(ERROR_STRING);
		try {
			appendBody("<pre>" + ExceptionTool.getStackTraceString(th)
					+ "</pre>");
		} catch (Throwable e) {
			appendBody("<pre>");
			th.printStackTrace(new PrintStream(body));
			appendBody("</pre>");
		}
		return ERROR_STRING;
	}

	/**
	 * 设置403
	 * 
	 * @return the string
	 * @author 黄林
	 */
	public String forbidden() {
		setResponseCode(HTTP_STATUS_FORBIDDEN);
		appendBody(FORBIDDEN_STRING);
		return FORBIDDEN_STRING;
	}

	public IoBuffer getBody() {
		return IoBuffer.wrap(body.toByteArray());
	}

	public int getBodyLength() {
		return body.size();
	}

	public Map getHeaders() {
		return headers;
	}

	public int getResponseCode() {
		return this.responseCode;
	}

	/**
	 * 设置404
	 * 
	 * @return the string
	 * @author 黄林
	 */
	public String notFound() {
		setResponseCode(HTTP_STATUS_NOT_FOUND);
		appendBody(NOT_FOUND_STRING);
		return NOT_FOUND_STRING;
	}

	public void setContentType(String contentType) {
		headers.put("Content-Type", contentType);
	}

	public void setLastModified(Date date) {
		headers.put("Last-Modified", new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss zzz").format(date));
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * 设置返回的数据为json
	 * 
	 * @author 黄林 Sets the return json.
	 */
	public void setReturnJson() {
		setContentType("text/json;charset=utf-8");
	}

	public void write(String str) {
		appendBody(str);
	}
}
