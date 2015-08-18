package cn.city.in.api.tools.image;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;

import cn.city.in.api.tools.common.FileTool;

public class ImageTool {

	private static Log log = LogFactory.getLog(ImageTool.class);

	/**
	 * 图片水印
	 * 
	 * @param srcFile
	 *            File 图片原文件
	 * @param destFile
	 *            File 新图片文件
	 * @param destWidth
	 *            Integer 新图片宽度
	 * @param destHeight
	 *            Integer 新图片高度
	 * @return true, if successful
	 */
	public static boolean addWatermark(File srcFile, File destFile,
			File watermarkFile, String format) {
		try {
			if (!srcFile.isFile() || !srcFile.exists())// 文件不存在
			{
				log.error("file no exists:" + srcFile.getAbsolutePath());
				return false;
			}
			// 创建路径
			FileTool.makeDirs(new File(destFile.getParent()));
			// 建立原图片对象
			BufferedImage src = ImageIO.read(srcFile);
			BufferedImage watemark = ImageIO.read(watermarkFile);
			Graphics g = src.getGraphics();
			g.drawImage(watemark, 0, src.getHeight() - watemark.getHeight(),
					null);
			g.dispose();
			ImageIO.write(src, format, destFile);
			return true;
		} catch (Exception e) {
			log.error("compress pic fail:", e);
			return false;
		}
	}

	/**
	 * 图片压缩
	 * 
	 * @param srcFile
	 *            File 图片原文件
	 * @param destFile
	 *            File 新图片文件
	 * @param destWidth
	 *            Integer 新图片宽度
	 * @param destHeight
	 *            Integer 新图片高度
	 * @return true, if successful
	 */
	public static boolean compressPic(BufferedImage src, File destFile,
			Integer destWidth, Integer destHeight, String format) {
		try {
			int picType = BufferedImage.TYPE_INT_RGB;
			if (format.equals("png") || format.equals("gif")) {
				picType = BufferedImage.TYPE_INT_ARGB;
			}
			// Edit by Liuw at 2012-2-10：创建路径
			FileTool.makeDirs(new File(destFile.getParent()));
			// 建立原图片对象
			// 获得原始图片宽高
			int srcWidth = src.getWidth();
			int srcHeight = src.getHeight();
			int mw = 0;
			int mh = 0;
			/** 是否等比,0=不等比，1=宽不定，2=高不定 */
			int proportion = destWidth == null ? 1 : destHeight == null ? 2 : 0;
			if (srcWidth <= -1) {// 不是图片
				log.error(" can't read,retry!");
				return false;
			}
			switch (proportion) {
			case 0:
				// 获得标准长度
				if (srcWidth <= destWidth && srcHeight <= destHeight) {// 原图小于目标图
					// 直接输出图片
					return ImageIO.write(src, format, destFile);
				} else {// 已最短边为基准，裁长边
					if (srcWidth < srcHeight) {
						mw = destWidth;
						mh = new Double(srcHeight * ((double) mw / srcWidth))
								.intValue();
					} else {
						mh = destHeight;
						mw = new Double(srcWidth * ((double) mh / srcHeight))
								.intValue();
					}
				}
				break;
			case 1:// 不定宽，定长
					// 获得标准长度
				if (srcHeight <= destHeight) {// 原图长度小于目标图
					// 直接输出图片
					return ImageIO.write(src, format, destFile);

				} else {
					mh = destHeight;
					mw = new Double(srcWidth * ((double) mh / srcHeight))
							.intValue();
				}
				break;
			case 2:// 不定长，定宽
				if (srcWidth <= destWidth) {// 原图宽度小于目标图
					// 直接输出图片
					return ImageIO.write(src, format, destFile);
				} else {
					mw = destWidth;
					mh = (int) (srcHeight * ((double) mw / srcWidth));
				}
				break;

			default:
				return false;
			}
			// 缩放图片
			BufferedImage tag = new BufferedImage(mw, mh, picType);
			Graphics g = tag.getGraphics();
			g.drawImage(src.getScaledInstance(mw, mh, Image.SCALE_SMOOTH), 0,
					0, mw, mh, null);
			ImageWriter iw = ImageIO.getImageWritersByFormatName(format).next();
			ImageWriteParam iwp = new javax.imageio.plugins.jpeg.JPEGImageWriteParam(
					null);
			//
			iw.setOutput(ImageIO.createImageOutputStream(destFile));
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			// iwp.setCompressionQuality((float) 0.8);
			iwp.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
			iw.write(null, new IIOImage(tag, null, null), iwp);
			g.dispose();
			// ImageIO.write(tag, format, destFile);
			// 裁切图片
			src = tag;
			srcWidth = src.getWidth();
			srcHeight = src.getHeight();
			if (proportion == 0
					&& (srcWidth > destWidth || srcHeight > destHeight)) {
				// 需要裁切才执行裁切操作
				int x = (srcWidth - destWidth) / 2;
				int y = (srcHeight - destHeight) / 2;
				// 裁切图片
				ImageFilter filter = new CropImageFilter(x, y, destWidth,
						destHeight);
				Image img = Toolkit.getDefaultToolkit().createImage(
						new FilteredImageSource(src.getSource(), filter));
				tag = new BufferedImage(destWidth, destHeight, picType);
				g = tag.getGraphics();
				g.drawImage(img.getScaledInstance(destWidth, destHeight,
						Image.SCALE_SMOOTH), 0, 0, destWidth, destHeight, null);
				iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				iwp.setCompressionQuality((float) 0.8);
				iwp.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
				ColorModel cm = ColorModel.getRGBdefault();
				iwp.setDestinationType(new ImageTypeSpecifier(cm, cm
						.createCompatibleSampleModel(destWidth, destHeight)));
				iw.setOutput(ImageIO.createImageOutputStream(destFile));
				iw.write(null, new IIOImage(tag, null, null), iwp);
				g.dispose();
			} else {
				ImageIO.write(tag, format, destFile);
			}
			src = null;
			tag = null;
			destFile = null;
			return true;
		} catch (Exception e) {
			log.error("compress pic fail:", e);
			return false;
		}
	}
	
	/**
	 * 调整图片到指定大小,并压缩质量
	 * 缩扩裁剪
	 * @param src the src
	 * @param x the x
	 * @param y the y
	 * @return the buffered image
	 * @author 黄林
	 */
	public static BufferedImage resize(BufferedImage src,Integer x,Integer y)
	{
		//扩
		if (src.getWidth()<x) {
			src=Scalr.resize(src,Method.QUALITY,Mode.FIT_TO_WIDTH,x);
		}
		if (src.getHeight()<y) {
			src=Scalr.resize(src,Method.QUALITY,Mode.FIT_TO_HEIGHT,y);
		}
		int x1=0,x2=0,y1=0,y2=0;
		//裁
		if (src.getWidth()/(float)x>src.getHeight()/(float)y) {
			//裁掉x
			x1=(src.getWidth()-(src.getHeight()*x)/y)/2;
			x2=src.getWidth()-x1;
			y1=0;
			y2=src.getHeight();
		}
		if (src.getWidth()/(float)x<src.getHeight()/(float)y) {
			//裁掉y
			y1=(src.getHeight()-(src.getWidth()*y)/x)/2;
			y2=src.getHeight()-y1;
			x1=0;
			x2=src.getWidth();
		}
		if (src.getWidth()/(float)x==src.getHeight()/(float)y) {
			//不裁剪
			x1=0;
			y1=0;
			x2=src.getWidth();
			y2=src.getHeight();
		}
		//裁剪
		src=src.getSubimage(x1, y1, x2-x1, y2-y1);
//		压缩
//		System.out.println("x:"+x+" \t y:"+y);
//		System.out.println("src x:"+src.getWidth()+" \t y:"+src.getHeight());
		return Scalr.resize(src,Method.QUALITY, x,y);
	}
}
