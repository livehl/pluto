package cn.city.in.api.exception;

import cn.city.in.api.tools.APIPropertyTool;

/**
 * 功能:核心异常类.
 * 
 * @author 黄林 2011-7-7
 * @version
 */
public class APIException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5660818308508496895L;

	/** 错误信息. */
	private String message;

	/** 错误码. */
	private int errorCode;

	/** 是否被禁止访问 @author 黄林 The is forbidden. */
	private boolean isForbidden = false;

	/**
	 * 功能:以错误码创建异常 创建者： 黄林 2011-9-13.
	 * 
	 * @param errorCode
	 *            the error code
	 */
	public APIException(int errorCode) {
		this.errorCode = errorCode;
		this.message = APIPropertyTool.getError(errorCode);
	}

	/**
	 * 功能:以错误码、是否为禁止访问 创建异常 创建者： 黄林 2011-9-13.
	 * 
	 * @param errorCode
	 *            the error code
	 * @param forbidden
	 *            the forbidden
	 */
	public APIException(int errorCode, boolean forbidden) {
		this.errorCode = errorCode;
		this.message = APIPropertyTool.getError(errorCode);
		this.isForbidden = forbidden;
	}

	/**
	 * 功能:以错误码、简洁消息创建异常 创建者： 黄林 2011-7-7.
	 * 
	 * @param errorCode
	 *            the erro code
	 * @param message
	 *            the message
	 */
	public APIException(int errorCode, String message) {
		this.message = message + APIPropertyTool.getError(errorCode);
		this.errorCode = errorCode;
	}

	/**
	 * 只有错误消息的构造方法
	 * 
	 * @author Johnny
	 * @param message
	 */
	public APIException(String message) {
		this.message = message;
		this.errorCode = -1;
	}

	/**
	 * Gets the 错误码.
	 * 
	 * @return the 错误码
	 */
	public int getErroCode() {
		return errorCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * Checks if is forbidden.
	 * 
	 * @return true, if is forbidden
	 */
	public boolean isForbidden() {
		return isForbidden;
	}

	/**
	 * Sets the 错误码.
	 * 
	 * @param erroCode
	 *            the new 错误码
	 */
	public void setErroCode(int erroCode) {
		this.errorCode = erroCode;
		this.message = APIPropertyTool.getError(errorCode);
	}

	/**
	 * Sets the forbidden.
	 * 
	 * @param isForbidden
	 *            the new forbidden
	 * @author 黄林
	 */
	public void setForbidden(boolean isForbidden) {
		this.isForbidden = isForbidden;
	}

	/**
	 * Sets the 错误信息.
	 * 
	 * @param message
	 *            the new 错误信息
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
