package cn.city.in.task.execute.log;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.WriterAppender;

/**
 * 固定字符大小的内存日志输出源
 * 
 * @author 黄林 The Class ExcuteMemAppender.
 */
public class ExcuteMemAppender extends WriterAppender {
	/**
	 * The Class LogWriter.
	 * 
	 * @author 黄林 The Class LogWriter.
	 */
	protected class LogWriter extends Writer {
		// 10k-20k 字符
		/** The size. @author 黄林 The size. */
		private int size = 10000;

		/**
		 * Instantiates a new log writer.
		 * 
		 * @param len
		 *            the len
		 */
		public LogWriter(Integer len) {
			super();
			if (len != null && len > 0) {
				size = len;
			}
			buf = new StringBuffer(size);
			lastBuf = new StringBuffer(size);
		}

		// append
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Writer#append(char)
		 */
		@Override
		public LogWriter append(char c) {
			try {
				write(c);
			} catch (IOException e) {
			}
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Writer#append(java.lang.CharSequence)
		 */
		@Override
		public LogWriter append(CharSequence csq) {
			if (csq == null)
				write("null");
			else
				write(csq.toString());
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Writer#append(java.lang.CharSequence, int, int)
		 */
		@Override
		public LogWriter append(CharSequence csq, int start, int end) {
			CharSequence cs = (csq == null ? "null" : csq);
			write(cs.subSequence(start, end).toString());
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Writer#close()
		 */
		@Override
		public void close() throws IOException {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Writer#flush()
		 */
		@Override
		public void flush() throws IOException {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return lastBuf.toString() + buf.toString();
		}

		// write
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Writer#write(char[], int, int)
		 */
		@Override
		public void write(char cbuf[], int off, int len) {
			if ((off < 0) || (off > cbuf.length) || (len < 0)
					|| ((off + len) > cbuf.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return;
			}
			synchronized (buf) {
				if (buf.length() >= size) {
					lastBuf = buf;
					buf = new StringBuffer(size);
				}
			}
			buf.append(cbuf, off, len);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Writer#write(java.lang.String)
		 */
		@Override
		public void write(String str) {
			synchronized (buf) {
				if (buf.length() >= size) {
					lastBuf = buf;
					buf = new StringBuffer(size);
				}
			}
			buf.append(str);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Writer#write(java.lang.String, int, int)
		 */
		@Override
		public void write(String str, int off, int len) {
			write(str.substring(off, off + len));
		}
	}

	/** The buf. @author 黄林 The buf. */
	protected static StringBuffer buf;
	/** The last buf. @author 黄林 The last buf. */
	protected static StringBuffer lastBuf;

	/**
	 * 输出日志
	 * 
	 * @return the log string
	 * @author 黄林
	 */
	public static String getLogString() {
		if (null != lastBuf && buf != null) {
			return lastBuf.toString() + buf.toString();
		}
		return null;
	}

	private Integer size;

	public ExcuteMemAppender() {
		super();
	}

	/**
	 * Instantiates a new excute mem log.
	 * 
	 * @param len
	 *            the len
	 */
	public ExcuteMemAppender(Integer size) {
		this.size = size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.log4j.WriterAppender#activateOptions()
	 */
	@Override
	public void activateOptions() {
		setWriter(new LogWriter(size));
		super.activateOptions();
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}
}
