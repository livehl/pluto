package cn.city.in.task.execute.taskdeal.analyse;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.api.tools.common.Tool;
import cn.city.in.task.execute.db.DataBaseTool;
import cn.city.in.task.execute.taskdeal.BaseTaskDeal;
import cn.city.in.task.execute.taskdeal.db.dto.PlutoDataDTO;
import cn.city.in.task.execute.taskdeal.db.dto.PlutoPostDTO;

/**
 * 执行网站频道分析任务
 * 
 * @author 黄林 The Class DBTaskDealImpl.
 */
public class WebNewsCategoryTaskDealImpl extends BaseTaskDeal {

	/** The log. @author 黄林 The log. */
	protected static Log log = LogFactory
			.getLog(WebNewsCategoryTaskDealImpl.class);

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String... args) throws Exception {
		PropertyTool.init("classpath:serverconf/task.execute.properties");
		ObjectNode on = new ObjectNode(JsonNodeFactory.instance);
		on.put("id", 1354366);
		ArrayNode an = new ArrayNode(JsonNodeFactory.instance);
		an.add("汽车城");
		an.add("二手车");
		on.put("tags", an);
		ObjectNode out = new ObjectNode(JsonNodeFactory.instance);
		out.put("data", on);
		WebNewsCategoryTaskDealImpl wnctdi = new WebNewsCategoryTaskDealImpl();
		wnctdi.doTask(out);
		System.exit(0);
	}

	/** The conn. @author 黄林 The conn. */
	protected Connection conn;

	/** The idel count. @author 黄林 The idel count. */
	protected int idelCount = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.city.in.task.execute.taskdeal.BaseTaskDeal#accept(java.lang.String)
	 */
	@Override
	public boolean accept(String task) {
		return task.equals("indigo");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.city.in.task.execute.taskdeal.BaseTaskDeal#doTask(org.codehaus.jackson
	 * .JsonNode)
	 */
	@Override
	public boolean doTask(JsonNode task) throws Exception {
		JsonNode data = task.get("data");
		JsonNode tags = data.get("tags");
		Integer id = data.get("id").asInt();
		if (null == conn||conn.isClosed()) {
			init();
		}
		// 查询实体
		List<PlutoPostDTO> postList=DataBaseTool.queryDataBase(conn,
				PlutoPostDTO.class,
				"select id,uid,placeId from Post where id=" + id);
		if (Tool.isNull(postList)) {
			return true;
		}
		PlutoPostDTO post=postList.get(0);
		// 删除原有数据
		DataBaseTool.update(conn, "delete from WebNewsCategoryData where postId="
				+ id);
		DataBaseTool.update(conn, "delete from PostOwnTag where postId="
				+ id);
		List<PlutoDataDTO> queryInfo=new ArrayList<PlutoDataDTO>();
		StringBuilder sb;
		if (tags.size()>0) {
			// 查询所有的tagId
			sb = new StringBuilder(
					"select id,content from Tag where content in(");
			for (JsonNode tag : tags) {
				sb.append("'");
				sb.append(tag.asText());
				sb.append("',");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
			List<PlutoDataDTO> tagIds = DataBaseTool.queryDataBase(conn,
					PlutoDataDTO.class, sb.toString());
			//插入不存在的tag
			if(tagIds.size()!=tags.size())
			{
				for (JsonNode tag : tags) {
					String context=tag.asText();
					boolean hasTag=false;
					for (PlutoDataDTO newtag : tagIds) {
						if(newtag.getContent().equals(context))
						{
							hasTag=true;
							break;
						}
					}
					if (!hasTag) {
						//插入数据
						String sql="INSERT IGNORE  INTO Tag (content) VALUES ('"+context+"')";
						DataBaseTool.update(conn, sql);
						PlutoDataDTO insertTag=new PlutoDataDTO();
						insertTag.setId(DataBaseTool.queryDataBase(conn,PlutoDataDTO.class, "select id,content from Tag where content='"+context+"'").get(0).getId());
						insertTag.setContent(context);
						tagIds.add(insertTag);
					}
				}
			}
			if (Tool.isNotNull(tagIds)) {
				// 记录PostOwnTag
				sb = new StringBuilder(
						"INSERT IGNORE  INTO PostOwnTag (tagId,postId) VALUES ");
				for (PlutoDataDTO insert : tagIds) {
					sb.append(" (" + insert.getId() + "," + id + ")");
					sb.append(",");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(";");
				DataBaseTool.update(conn, sb.toString());
				// 查询tag的关联关系
				sb = new StringBuilder(
						"select channelId,catId,tagType,tagId from WebNewsCategoryOwnTag where tagType=0 and tagId in(");
				for (PlutoDataDTO tagId : tagIds) {
					sb.append("'");
					sb.append(tagId.getId());
					sb.append("',");
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append(")");
				queryInfo.addAll(DataBaseTool.queryDataBase(conn,
						PlutoDataDTO.class, sb.toString()));
			}
		}
		// 查询用户的关联关系
		sb = new StringBuilder(
				"select channelId,catId,tagType,tagId from WebNewsCategoryOwnTag where tagType=3 and tagId="+post.getUid());
		queryInfo.addAll(DataBaseTool.queryDataBase(conn,
				PlutoDataDTO.class, sb.toString()));
		// 查询地点的关联关系
		sb = new StringBuilder(
				"select channelId,catId,tagType,tagId from WebNewsCategoryOwnTag where tagType=2 and tagId="+post.getPlaceId());
		queryInfo.addAll(DataBaseTool.queryDataBase(conn,
				PlutoDataDTO.class, sb.toString()));
		//查询地点的分类关联关系
		sb=new StringBuilder(
				"select placeCategoryId from PlaceOwnCategory where placeId="+post.getPlaceId());
		List<PlutoDataDTO> placeCategoryList=DataBaseTool.queryDataBase(conn,
				PlutoDataDTO.class, sb.toString());
		Set<PlutoDataDTO> allPlaceCategoryList=new HashSet<PlutoDataDTO>(placeCategoryList);
		for (PlutoDataDTO placeCategory : placeCategoryList) {
			sb=new StringBuilder(
					"select parent as placeCategoryId  from PlaceCategoryShip where parent!=0 and child="+placeCategory.getPlaceCategoryId());
			allPlaceCategoryList.addAll(DataBaseTool.queryDataBase(conn,
					PlutoDataDTO.class, sb.toString()));
		}
		if (allPlaceCategoryList.size()>0) {
			sb = new StringBuilder(
					"select channelId,catId,tagType,tagId from WebNewsCategoryOwnTag where tagType=1 and tagId in(");
			for (PlutoDataDTO placeCategory : allPlaceCategoryList) {
				sb.append("'");
				sb.append(placeCategory.getPlaceCategoryId());
				sb.append("',");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
			queryInfo.addAll(DataBaseTool.queryDataBase(conn,
					PlutoDataDTO.class, sb.toString()));
		}
		// 分析数据\去重
		Set<String> unSet = new HashSet<String>();
		List<PlutoDataDTO> insertInfo = new ArrayList<PlutoDataDTO>();
		for (PlutoDataDTO query : queryInfo) {
			if (!unSet.contains(query.getCatId() + "_" + query.getChannelId())) {
				PlutoDataDTO insert = new PlutoDataDTO();
				insert.setCatId(query.getCatId());
				insert.setChannelId(query.getChannelId());
				insert.setPostId(id);
				insertInfo.add(insert);
				unSet.add(query.getCatId() + "_" + query.getChannelId());
			}
		}
		if (Tool.isNull(insertInfo)) {
			return true;
		}
		// 写入数据
		sb = new StringBuilder(
				"INSERT IGNORE  INTO WebNewsCategoryData (catId,channelId,postId) VALUES ");
		for (PlutoDataDTO insert : insertInfo) {
			sb.append(" (" + insert.getCatId() + "," + insert.getChannelId()
					+ "," + insert.getPostId() + ")");
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(";");
		DataBaseTool.update(conn, sb.toString());
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.city.in.task.execute.taskdeal.BaseTaskDeal#idle()
	 */
	@Override
	public boolean idle() {
		idelCount++;
		if (idelCount > 120) {
			idelCount = 0;
			return stop();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.city.in.task.execute.taskdeal.BaseTaskDeal#init()
	 */
	@Override
	public boolean init() {
		try {
			conn = DataBaseTool.getConnection();
		} catch (Exception e) {
			log.error("init connection fail", e);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.city.in.task.execute.taskdeal.BaseTaskDeal#stop()
	 */
	@Override
	public boolean stop() {
		try {
			if (null != conn) {
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			log.error("stop connection fail", e);
			return false;
		}
		return true;
	}
}
