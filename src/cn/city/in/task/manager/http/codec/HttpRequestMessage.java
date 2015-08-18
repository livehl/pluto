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

import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A HTTP request message.
 * 
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 555855 $, $Date: 2007-07-13 12:19:00 +0900 (Fri, 13 Jul 2007)
 *          $
 */
public class HttpRequestMessage {
	public static String arrayToString(String[] s, char sep) {
		if (s == null || s.length == 0)
			return "";
		StringBuffer buf = new StringBuffer();
		if (s != null) {
			for (int i = 0; i < s.length; i++) {
				if (i > 0)
					buf.append(sep);
				buf.append(s[i]);
			}
		}
		return buf.toString();
	}

	/** Map<String, String[]> */
	private Map headers = null;

	public String getAction() {
		String uri = getUri();
		if (uri.contains("?")) {
			return uri.substring(0, uri.indexOf("?"));
		} else {
			return uri;
		}
	}

	public String getContext() {
		String[] context = (String[]) headers.get("Context");
		return context == null ? null : context[0];
	}

	/**
	 * 获取请求数据
	 * 
	 * @param name
	 *            the name
	 * @return the data
	 * @author 黄林
	 */
	public Object getData(String name) {
		Object param = getDecoderParameter(name);
		if (null == param) {
			param = headers.get(name);
		}
		return param;
	}

	public String getDecoderParameter(String name) {
		String result = getParameter(name);
		if (null != result) {
			result = URLDecoder.decode(result);
		}
		return result;
	}

	public String[] getHeader(String name) {
		return (String[]) headers.get(name);
	}

	public Map getHeaders() {
		return headers;
	}

	public String getMethod() {
		String uri = getHeader("URI")[0];
		return uri.split(" ")[0];
	}

	public String getParameter(String name) {
		String[] param = (String[]) headers.get("@".concat(name));
		return param == null ? null : param[0];
	}

	public String[] getParameters(String name) {
		String[] param = (String[]) headers.get("@".concat(name));
		return param == null ? new String[] {} : param;
	}

	public String getRemoteAddr() {
		return (String) headers.get("X-Manager-remoteAddr");
	}

	public String getUri() {
		String uri = getHeader("URI")[0];
		return uri.split(" ")[1];
	}

	public void setHeaders(Map headers) {
		this.headers = headers;
	}

	public void setRemoteAddr(String ip) {
		headers.put("X-Manager-remoteAddr", ip);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		Iterator it = headers.entrySet().iterator();
		while (it.hasNext()) {
			Entry e = (Entry) it.next();
			str.append(e.getKey() + " : "
					+ arrayToString((String[]) e.getValue(), ',') + "\n");
		}
		return str.toString();
	}
}
