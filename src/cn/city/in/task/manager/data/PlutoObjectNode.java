package cn.city.in.task.manager.data;

import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 专为Kryo 优化的ObjectNode
 * 
 * @author 黄林 The Class PlutoObjectNode.
 */
public class PlutoObjectNode extends ObjectNode {

	/**
	 * Instantiates a new pluto object node.
	 */
	public PlutoObjectNode() {
		super(JsonNodeFactory.instance);
	}

	/**
	 * Instantiates a new pluto object node.
	 * 
	 * @param nc
	 *            the nc
	 */
	public PlutoObjectNode(JsonNodeFactory nc) {
		super(nc);
	}

}
