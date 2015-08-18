package cn.city.in.task.execute.taskdeal.solr

import cn.city.in.task.execute.taskdeal.BaseTaskDeal
import org.codehaus.jackson.JsonNode
import scala.collection.JavaConverters._
import org.apache.solr.common.SolrInputDocument
import cn.city.in.api.tools.solr.SolrTool

class SolrTaskDealImpl() extends BaseTaskDeal {

  @Override def accept(task: String) = {
    task.equals("solr")
  }

  @Override def doTask(task: JsonNode) = {
    val data = task.get("data")
    val t = data.get("type").asText()
    val url = data.get("url").asText()
    val doc = new SolrInputDocument()
    if ("place".equals(t)) {
      "id" :: "placename" :: "address" :: "store" :: "rank" :: Nil foreach { name => if (data.get(name) != null) doc.addField(name, data.get(name).asText(), 1.0f) }
      SolrTool.addDoc(url, doc)
    } else if ("post".equals(t)) {
      "id" :: "username" :: "placename" :: "content" :: "store" :: "rank" :: Nil foreach { name => if (data.get(name) != null) doc.addField(name, data.get(name).asText(), 1.0f) }
      SolrTool.addDoc(url, doc)
    } else if ("optimize".equals(t)) {
      SolrTool.optimize(url)
    } else if ("del_post".equals(t)) {
      SolrTool.deleteByQuery(url, "id:" + data.get("id").asText())
    } else {
      throw new Exception("can't deal task:" + task);
    }
    true
  }

}