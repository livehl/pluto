package cn.city.in.task.manager.socket.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class PlutoEncoder extends ProtocolEncoderAdapter {
	private static ObjectMapper mapper = new ObjectMapper();

	public PlutoEncoder() {
	}

	@Override
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {
		JsonNode msg = (JsonNode) message;
		byte[] bytes = mapper.writeValueAsBytes(msg);
		int capacity = bytes.length + 4;
		IoBuffer buffer = IoBuffer.allocate(capacity, false);
		buffer.setAutoExpand(true);// 设置自动扩充
		buffer.putInt(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		out.write(buffer);
	}
}
