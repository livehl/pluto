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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderAdapter;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

/**
 * A {@link MessageDecoder} that decodes {@link HttpRequest}.
 * 
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 555855 $, $Date: 2007-07-13 12:19:00 +0900 (Fri, 13 Jul 2007)
 *          $
 */
public class HttpRequestDecoder extends MessageDecoderAdapter {
	private static final byte[] CONTENT_LENGTH = new String("Content-Length:")
			.getBytes();

	private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();

	private HttpRequestMessage request = null;

	public HttpRequestDecoder() {
	}

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		// Return NEED_DATA if the whole header is not read yet.
		try {
			return messageComplete(in) ? MessageDecoderResult.OK
					: MessageDecoderResult.NEED_DATA;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return MessageDecoderResult.NOT_OK;
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		// Try to decode body
		HttpRequestMessage m = decodeBody(in);

		// Return NEED_DATA if the body is not fully read.
		if (m == null)
			return MessageDecoderResult.NEED_DATA;

		out.write(m);

		return MessageDecoderResult.OK;
	}

	private HttpRequestMessage decodeBody(IoBuffer in) {
		request = new HttpRequestMessage();
		try {
			// request.setHeaders(parseRequest(new StringReader(in
			// .getString(decoder))));
			byte[] data = new byte[in.remaining()];
			in.get(data);
			request.setHeaders(parseRequest(data));
			return request;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	/**
	 * 解码文件
	 * 
	 * @param data
	 *            the data
	 * @return the map
	 * @author 黄林
	 */
	private Map decodeFile(byte[] data) {
		Map map = new HashMap();
		int fileStart = findEmpityLine(data, 3) + 4;
		while (fileStart < data.length) {
			int infoStart = fileStart;
			int infoEnd = findEmpityLine(data, infoStart + 1) - 1 + 4;
			String info = new String(data, infoStart, infoEnd - infoStart);
			// System.out.println(info);
			String[] line = info.split("\r\n");
			String identy = line[0];
			int dataEnd = findByte(data, identy.getBytes(), infoEnd + 1);
			byte[] uploadData = Arrays.copyOfRange(data, infoEnd + 1,
					dataEnd - 2);
			String name = line[1];
			name = name.substring(name.indexOf("name") + 6);
			name = name.substring(0, name.indexOf("\""));
			Object upload = null;
			if (line.length == 4) {
				// 文件
				String fileName = line[1];
				fileName = fileName.substring(fileName.indexOf("fileName") + 6);
				fileName = fileName.substring(0, fileName.indexOf("\""));
				String type = line[2].split(":")[1];
				upload = new HttpFile(name, fileName, type, uploadData);
			} else {
				// 表单
				upload = new String(uploadData);

			}
			if (map.containsKey(name)) {
				Object o = map.get(name);
				if (o instanceof Object[]) {
					Object[] array = Arrays.copyOf((Object[]) o,
							((Object[]) o).length + 1);
					array[array.length] = upload;
					map.put(name, array);
				} else {
					Object[] array = new Object[2];
					array[0] = o;
					array[1] = upload;
					map.put(name, array);
				}
			} else {
				map.put(name, upload);
			}
			fileStart = dataEnd;
			if (data.length < (identy.getBytes().length + 10 + dataEnd)) {
				// 一般意味着结束了
				fileStart = data.length;
			}
		}
		return map;
	}

	/**
	 * 数组搜索
	 * 
	 * @param source
	 *            the source
	 * @param find
	 *            the find
	 * @return the int
	 * @author 黄林
	 */
	private int findByte(byte[] source, byte[] find, int start) {
		int index = -1;
		for (int i = start; i < source.length; i++) {
			if (source[i] == find[0]) {
				boolean allEqual = true;
				for (int j = 0; j < find.length; j++) {
					if (source[i + j] != find[j]) {
						allEqual = false;
					}
				}
				if (allEqual) {
					return i;
				}
			}
		}
		return index;
	}

	/**
	 * 查找空行
	 * 
	 * @param data
	 *            the data
	 * @param start
	 *            the start
	 * @return the int
	 * @author 黄林
	 */
	private int findEmpityLine(byte[] data, int start) {
		return findByte(data, new byte[] { (byte) 0x0D, (byte) 0x0A,
				(byte) 0x0D, (byte) 0x0A }, start);
	}

	private boolean messageComplete(IoBuffer in) throws Exception {
		int last = in.remaining() - 1;
		if (in.remaining() < 4)
			return false;

		// to speed up things we check if the Http request is a GET or POST
		if (in.get(0) == (byte) 'G' && in.get(1) == (byte) 'E'
				&& in.get(2) == (byte) 'T') {
			// Http GET request therefore the last 4 bytes should be 0x0D 0x0A
			// 0x0D 0x0A
			return (in.get(last) == (byte) 0x0A
					&& in.get(last - 1) == (byte) 0x0D
					&& in.get(last - 2) == (byte) 0x0A && in.get(last - 3) == (byte) 0x0D);
		} else if (in.get(0) == (byte) 'P' && in.get(1) == (byte) 'O'
				&& in.get(2) == (byte) 'S' && in.get(3) == (byte) 'T') {
			// Http POST request
			// first the position of the 0x0D 0x0A 0x0D 0x0A bytes
			// 我了个艹!你妹的原来这里是倒数的,post multipart/form-data 你妹的就不能用了!!
			int eoh = -1;
			for (int i = 3; i <= last; i++) {
				if (in.get(i) == (byte) 0x0D && in.get(i + 1) == (byte) 0x0A
						&& in.get(i + 2) == (byte) 0x0D
						&& in.get(i + 3) == (byte) 0x0A) {
					eoh = i + 4;
					break;
				}
			}
			if (eoh == -1)
				return false;
			for (int i = 0; i < last; i++) {
				boolean found = false;
				for (int j = 0; j < CONTENT_LENGTH.length; j++) {
					if (in.get(i + j) != CONTENT_LENGTH[j]) {
						found = false;
						break;
					}
					found = true;
				}
				if (found) {
					// retrieve value from this position till next 0x0D 0x0A
					StringBuilder contentLength = new StringBuilder();
					for (int j = i + CONTENT_LENGTH.length; j < last; j++) {
						if (in.get(j) == 0x0D)
							break;
						contentLength.append(new String(
								new byte[] { in.get(j) }));
					}
					// if content-length worth of data has been received then
					// the message is complete
					return (Integer.parseInt(contentLength.toString().trim())
							+ eoh == in.remaining());
				}
			}
		}

		// the message is not complete and we need more data
		return false;
	}

	private Map parseRequest(byte[] data) throws Exception {
		Map map = new HashMap();
		// String StringData=new String(data);
		// System.out.println(StringData);
		BufferedReader rdr = new BufferedReader(new StringReader(new String(
				data)));

		try {
			// Get request URL.
			String line = rdr.readLine();
			String[] url = line.split(" ");
			if (url.length < 3)
				return map;

			map.put("URI", new String[] { line });
			map.put("Method", new String[] { url[0].toUpperCase() });
			map.put("Context", new String[] { url[1].substring(1) });
			map.put("Protocol", new String[] { url[2] });
			// Read header
			while ((line = rdr.readLine()) != null && line.length() > 0) {
				String[] tokens = line.split(": ");
				map.put(tokens[0], new String[] { tokens[1] });
			}
			// Read Body
			// If method 'POST' then read Content-Length worth of data
			if (url[0].equalsIgnoreCase("POST")) {
				int len = Integer.parseInt(((String[]) map
						.get("Content-Length"))[0]);
				try {
					if ("multipart/form-data".equals(((String[]) map
							.get("Content-Type"))[0].split(";")[0])) {
						// Read Data Body
						map.putAll(decodeFile(data));
						return map;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				char[] buf = new char[len];
				if (rdr.read(buf) == len) {
					line = String.copyValueOf(buf);
				}
			} else if (url[0].equalsIgnoreCase("GET")) {
				int idx = url[1].indexOf('?');
				if (idx != -1) {
					map.put("Context",
							new String[] { url[1].substring(1, idx) });
					line = url[1].substring(idx + 1);
				} else {
					line = null;
				}
			}
			if (line != null) {
				String[] match = line.split("\\&");
				for (int i = 0; i < match.length; i++) {
					String[] params = new String[1];
					String[] tokens = match[i].split("=");
					switch (tokens.length) {
					case 0:
						map.put("@".concat(match[i]), new String[] {});
						break;
					case 1:
						map.put("@".concat(tokens[0]), new String[] {});
						break;
					default:
						String name = "@".concat(tokens[0]);
						if (map.containsKey(name)) {
							params = (String[]) map.get(name);
							String[] tmp = new String[params.length + 1];
							for (int j = 0; j < params.length; j++)
								tmp[j] = params[j];
							params = null;
							params = tmp;
						}
						params[params.length - 1] = tokens[1].trim();
						map.put(name, params);
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return map;
	}

	private Map parseRequest(Reader is) {
		Map map = new HashMap();
		BufferedReader rdr = new BufferedReader(is);

		try {
			// Get request URL.
			String line = rdr.readLine();
			String[] url = line.split(" ");
			if (url.length < 3)
				return map;

			map.put("URI", new String[] { line });
			map.put("Method", new String[] { url[0].toUpperCase() });
			map.put("Context", new String[] { url[1].substring(1) });
			map.put("Protocol", new String[] { url[2] });
			// Read header
			while ((line = rdr.readLine()) != null && line.length() > 0) {
				String[] tokens = line.split(": ");
				map.put(tokens[0], new String[] { tokens[1] });
			}

			// If method 'POST' then read Content-Length worth of data
			if (url[0].equalsIgnoreCase("POST")) {
				int len = Integer.parseInt(((String[]) map
						.get("Content-Length"))[0]);
				char[] buf = new char[len];
				if (rdr.read(buf) == len) {
					line = String.copyValueOf(buf);
				}
			} else if (url[0].equalsIgnoreCase("GET")) {
				int idx = url[1].indexOf('?');
				if (idx != -1) {
					map.put("Context",
							new String[] { url[1].substring(1, idx) });
					line = url[1].substring(idx + 1);
				} else {
					line = null;
				}
			}
			if (line != null) {
				String[] match = line.split("\\&");
				for (int i = 0; i < match.length; i++) {
					String[] params = new String[1];
					String[] tokens = match[i].split("=");
					switch (tokens.length) {
					case 0:
						map.put("@".concat(match[i]), new String[] {});
						break;
					case 1:
						map.put("@".concat(tokens[0]), new String[] {});
						break;
					default:
						String name = "@".concat(tokens[0]);
						if (map.containsKey(name)) {
							params = (String[]) map.get(name);
							String[] tmp = new String[params.length + 1];
							for (int j = 0; j < params.length; j++)
								tmp[j] = params[j];
							params = null;
							params = tmp;
						}
						params[params.length - 1] = tokens[1].trim();
						map.put(name, params);
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return map;
	}
}
