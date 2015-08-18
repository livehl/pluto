package cn.city.in.task.manager.http.codec;

/**
 * http上传的文件
 * 
 * @author 黄林 The Class HttpFile.
 */
public class HttpFile {

	/** The name. @author 黄林 The name. */
	private String name;

	/** The file name. @author 黄林 The file name. */
	private String fileName;

	/** The type. @author 黄林 The type. */
	private String type;

	/** The data. @author 黄林 The data. */
	private byte[] data;

	/**
	 * Instantiates a new http file.
	 */
	public HttpFile() {
		super();
	}

	/**
	 * Instantiates a new http file.
	 * 
	 * @param name
	 *            the name
	 * @param fileName
	 *            the file name
	 * @param type
	 *            the type
	 * @param data
	 *            the data
	 */
	public HttpFile(String name, String fileName, String type, byte[] data) {
		super();
		this.name = name;
		this.fileName = fileName;
		this.type = type;
		this.data = data;
	}

	/**
	 * Gets the data.
	 * 
	 * @return the data
	 * @author 黄林
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Gets the file name.
	 * 
	 * @return the file name
	 * @author 黄林
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 * @author 黄林
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 * @author 黄林
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the data.
	 * 
	 * @param data
	 *            the new data
	 * @author 黄林
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * Sets the file name.
	 * 
	 * @param fileName
	 *            the new file name
	 * @author 黄林
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 * @author 黄林
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type
	 *            the new type
	 * @author 黄林
	 */
	public void setType(String type) {
		this.type = type;
	}

}
