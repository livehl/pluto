package cn.city.in.task.manager.http.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cn.city.in.api.tools.common.FileTool;
import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.api.tools.common.StringTool;
import cn.city.in.task.manager.http.comment.HttpComment;

/**
 * 文件控制器,自动重载已经使用过的文件至内存
 * 
 * @author 黄林 The Class FileController.
 */
public class FileController extends BaseController {
	private static HashMap<String, File> fileCache = new HashMap<String, File>();

	/**
	 * 重载某个文件
	 * 
	 * @param fileUri
	 *            the file uri
	 * @author 黄林 Reload.
	 */
	public static void reload(String fileUri) {
		File file = FileTool.getClassPathFile(fileUri);
		if (file.exists()) {
			fileCache.put(fileUri, file);
		} else {
			fileCache.remove(fileUri);
		}
	}

	/**
	 * 文本内容
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "*.html,*.htm,*.css,*.js,*.xml")
	public String html() throws Exception {
		// Map m=getRequest().getHeaders();
		// System.out.println(((Object[])m.get("Accept-Language"))[0]);
		String fileUri = "html/" + getRequest().getAction();
		setContentType("text");
		File file = null;
		if (fileCache.containsKey(fileUri)) {
			file = fileCache.get(fileUri);
		} else {
			file = FileTool.getClassPathFile(fileUri);
			fileCache.put(fileUri, file);
		}
		if (!file.exists()) {
			getResponse().notFound();
			return null;
		}
		if (setCache(file)) {
			return null;
		}
		String value = PropertyTool.readFileAsString(file);
		return value;
	}

	/**
	 * 数据流
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "*.png,*.jpg,*.gif,*.swf")
	public byte[] image() throws Exception {
		String fileUri = "html/" + getRequest().getAction();
		setContentType("image");
		File file = null;
		if (fileCache.containsKey(fileUri)) {
			file = fileCache.get(fileUri);
		} else {
			file = FileTool.getClassPathFile(fileUri);
			fileCache.put(fileUri, file);
		}
		if (!file.exists()) {
			getResponse().notFound();
			return null;
		}
		if (setCache(file)) {
			return null;
		}
		byte[] value = PropertyTool.readFileAsByte(file);
		return value;
	}

	/**
	 * 设置缓存
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	protected boolean setCache(File file) throws Exception {
		// 判断是否需要返回缓存
		String[] modifiedTime = getRequest().getHeader("If-Modified-Since");
		if (StringTool.isNotNull(modifiedTime)) {
			String timeStr = modifiedTime[0];
			SimpleDateFormat sdf = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss zzz");
			Date date = sdf.parse(timeStr);
			if (date.getTime() / 1000 >= file.lastModified() / 1000) {
				// 304
				getResponse().setResponseCode(304);
				return true;
			} else {
				System.out.println(date.getTime() / 1000 + ":"
						+ file.lastModified() / 1000);
				getResponse().setLastModified(new Date(file.lastModified()));
			}
		}
		return false;
	}

	/**
	 * 设置ContentType
	 * 
	 * @param head
	 *            所属大类
	 * @author 黄林
	 */
	public void setContentType(String head) {
		String uri = getRequest().getUri();
		String ext = uri.substring(uri.lastIndexOf(".") + 1);
		if ("htm".equals(ext)) {
			ext = "html";
		}
		if ("jpg".equals(ext)) {
			ext = "jpeg";
		}
		if ("js".equals(ext)) {
			ext = "javascript";
		}
		getResponse().setContentType(head + "/" + ext + ";charset=utf-8");
	}
}
