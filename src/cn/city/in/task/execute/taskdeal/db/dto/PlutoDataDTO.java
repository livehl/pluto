package cn.city.in.task.execute.taskdeal.db.dto;

public class PlutoDataDTO {
	Integer id;
	Integer uid;
	Integer placeId;
	Integer tagId;
	Integer channelId;
	Integer catId;
	Integer tagType;
	Integer postId;
	String content;
	
	Integer placeCategoryId;

	public Integer getPlaceCategoryId() {
		return placeCategoryId;
	}

	public void setPlaceCategoryId(Integer placeCategoryId) {
		this.placeCategoryId = placeCategoryId;
	}

	public Integer getCatId() {
		return catId;
	}

	public Integer getChannelId() {
		return channelId;
	}

	public Integer getId() {
		return id;
	}

	public Integer getPlaceId() {
		return placeId;
	}

	public Integer getPostId() {
		return postId;
	}

	public Integer getTagId() {
		return tagId;
	}

	public Integer getTagType() {
		return tagType;
	}

	public Integer getUid() {
		return uid;
	}

	public void setCatId(Integer catId) {
		this.catId = catId;
	}

	public void setChannelId(Integer channelId) {
		this.channelId = channelId;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setPlaceId(Integer placeId) {
		this.placeId = placeId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}

	public void setTagType(Integer tagType) {
		this.tagType = tagType;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
