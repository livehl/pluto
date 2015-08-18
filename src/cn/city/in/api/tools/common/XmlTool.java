package cn.city.in.api.tools.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;

/**
 * xml解析工具类
 * 
 * @author Johnny
 * 
 */
public class XmlTool {
	/**
	 * 
	 * 遍历全部节点，将节点放入Map返回
	 * 
	 * @param element
	 * 
	 * @return
	 */

	public static Map getResults(OMElement element) {

		if (element == null) {

			return null;

		}

		Iterator iter = element.getChildElements();

		Map map = new HashMap();

		while (iter.hasNext()) {

			OMNode omNode = (OMNode) iter.next();

			if (omNode.getType() == OMNode.ELEMENT_NODE) {

				OMElement omElement = (OMElement) omNode;

				String key = omElement.getLocalName().trim();

				// System.out.println("sta: " + key);

				String value = omElement.getText().trim();

				map.put(key, value);

			}

		}

		return map;

	}

}
