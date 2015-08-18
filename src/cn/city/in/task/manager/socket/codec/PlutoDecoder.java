package cn.city.in.task.manager.socket.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class PlutoDecoder extends CumulativeProtocolDecoder {
	private static ObjectMapper mapper = new ObjectMapper();

	public PlutoDecoder() {
	}

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		if (in.prefixedDataAvailable(4)) {
			int length = in.getInt();
			byte[] bytes = new byte[length];
			in.get(bytes);
			JsonNode data = mapper.readValue(bytes, JsonNode.class);
			out.write(data);
			return true;
		} else {
			// 数据不够
			return false;

		}
	}
}
