package cn.city.in.api.tools.solr;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import cn.city.in.api.entity.Place;
import cn.city.in.api.entity.Post;
import cn.city.in.api.entity.dto.PlaceDTO;
import cn.city.in.api.entity.dto.UserDTO;
import cn.city.in.api.tools.cache.DataBaseCacheTool;
import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.api.tools.common.Tool;

public class ApiSolrTool {
	private static String postUrl;
	private static String placeUrl;

	private static String getPostUrl() {
		if (null == postUrl) {
			postUrl = PropertyTool.getProperties("solr_post_url");
		}
		return postUrl;
	}

	private static String getPlaceUrl() {
		if (null == placeUrl) {
			placeUrl = PropertyTool.getProperties("solr_place_url");
		}
		return placeUrl;
	}

	/**
	 * 普通查询
	 * 
	 * @param query
	 *            the query
	 * @return the solr document list
	 * @author 黄林
	 */
	public static SolrDocumentList queryPost(String query) {
		String url = getPostUrl();
		return SolrTool.query(url, query);
	}

	/**
	 * 查询带分页和排序
	 * 
	 * @param query
	 *            the query
	 * @param pageNumber
	 *            the page number
	 * @param pageSize
	 *            the page size
	 * @return the solr document list
	 * @author 黄林
	 */
	public static SolrDocumentList queryPost(String query, int pageNumber,
			int pageSize, String sort, boolean isDesc) {
		String url = getPostUrl();
		SolrQuery pagequery = new SolrQuery(query);
		pageNumber = pageNumber < 1 ? 1 : pageNumber;
		pageSize = pageSize < 1 ? 1 : pageSize;
		pagequery.setStart((pageNumber - 1) * pageSize);
		pagequery.setRows(pageSize);
		if (null != sort) {
			pagequery.setSort(sort, isDesc ? ORDER.desc : ORDER.asc);
		}
		return SolrTool.fullQuery(url, pagequery);
	}
	/**
	 * 查询话题带分页和排序
	 * 
	 * @param query
	 *            the query
	 * @param pageNumber
	 *            the page number
	 * @param pageSize
	 *            the page size
	 * @return the solr document list
	 * @author 黄林
	 */
	public static SolrDocumentList queryPostSubject(String query, int pageNumber,
			int pageSize, String sort, boolean isDesc) {
		return queryPost(query+" tags:"+query, pageNumber,pageSize,sort,isDesc);
	}

	/**
	 * 查询带分页
	 * 
	 * @param query
	 *            the query
	 * @param pageNumber
	 *            the page number
	 * @param pageSize
	 *            the page size
	 * @return the solr document list
	 * @author 黄林
	 */
	public static SolrDocumentList queryPost(String query, int pageNumber,
			int pageSize) {
		return queryPost(query, pageNumber, pageSize, null, false);
	}

	/**
	 * 优化solr 索引
	 * 
	 * @author 黄林 Optimize.
	 */
	public static void optimize() {
		SolrTool.optimize(getPlaceUrl());
		SolrTool.optimize(getPostUrl());
	}

	/**
	 * 删除post
	 * 
	 * @param id
	 *            the id
	 * @author 黄林 Del post.
	 */
	public static void delPost(int id) {
		SolrTool.deleteByQuery(getPostUrl(), "id:" + id);
	}

	/**
	 * 添加一条post
	 * 
	 * @param post
	 *            the post
	 * @return the update response
	 * @author 黄林
	 */
	public static void addPost(Post post) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", post.getId());
		if(null==post.getCreateDate()){
			post.setCreateDate(new Timestamp(System.currentTimeMillis()));
		}
		doc.addField("createTime", post.getCreateDate().getTime());
		if (Tool.isValid(post.getPlaceId())) {
			doc.addField(
					"placename",
					DataBaseCacheTool.getCache(PlaceDTO.class,
							post.getPlaceId()).getPlacename());
		}
		UserDTO user=DataBaseCacheTool.getCache(UserDTO.class, post.getUid());
		if(null!=user){
			doc.addField("username",
					user.getUsername());
		}
		if (Tool.isNotNull(post.getContent())) {
			doc.addField("content", post.getContent());
			List<String> tags=SolrTool.getTags(post.getContent(),"#");
			for (String tag : tags) {
				doc.addField("tags",tag);
			}
		}
		if (Tool.isNotNull(post.getLat())) {
			doc.addField("store", post.getLat() + "," + post.getLng());
		}
		SolrTool.addDoc(getPostUrl(), doc);
	}

	/**
	 * 删除post
	 * 
	 * @param id
	 *            the id
	 * @author 黄林 Del post.
	 */
	public static void delPlace(int id) {
		SolrTool.deleteByQuery(getPlaceUrl(), "id:" + id);
	}

	/**
	 * 添加一条post
	 * 
	 * @param post
	 *            the post
	 * @return the update response
	 * @author 黄林
	 */
	public static void addPlace(Place place) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", place.getId());
		doc.addField("placename", place.getPlacename());
		if (Tool.isNotNull(place.getAddress())) {
			doc.addField("address", place.getAddress());
		}
		doc.addField("store", place.getLatitude() + "," + place.getLongitude());
		SolrTool.addDoc(getPlaceUrl(), doc);
	}

	public static void updatePlace(Place place) {
		delPlace(place.getId());
		addPlace(place);
	}

}
