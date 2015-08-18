package cn.city.in.api.tools.solr
import org.apache.solr.client.solrj.impl.HttpSolrServer
import org.apache.solr.client.solrj.SolrQuery
import scala.collection.JavaConverters._
import cn.city.in.api.tools.common.Tool._
import org.apache.solr.common.SolrInputDocument
import scala.actors.Actor.actor
import java.util.concurrent.atomic.AtomicInteger
import java.util.{ List => jList }
import java.util.ArrayList
object SolrTool {
  private def getServer(url: String) = {
    val cache_key = "solr_server_cache_" + url
    if (getLocCache(cache_key) == null) {
      addLocCache(cache_key, new HttpSolrServer(url), "5m")
      getLocCache(cache_key).asInstanceOf[HttpSolrServer]
    } else {
      getLocCache(cache_key).asInstanceOf[HttpSolrServer]
    }
  }

  def getTags(context: String, separate: String): java.util.List[String] = {
    if (null == context || context.trim.length == 0 || context.indexOf(separate) == -1) {
      new ArrayList(0)
    } else {
      val start = context.indexOf(separate);
      if (start == -1) {
        new ArrayList(0)
      } else {
        val end = context.indexOf(separate, start + 1);
        if (end == -1) {
          new ArrayList(0)
        } else {
          context.substring(start, end + 1) :: ((getTags(context.substring(end + 1), separate) asScala)toList) asJava
        }
      }
    }

  }

  /**
   * 默认查询
   *
   * @param url the url
   * @param queryStr the query str
   * @return the org.apache.solr.common. solr document list
   * @author 黄林
   */
  def query(url: String, queryStr: String) = {
    fullQuery(url, new SolrQuery(queryStr))
  }

  /**
   * 限制距离带分页查询
   *
   * @param url the url
   * @param queryStr the query str
   * @param lat the lat
   * @param lng the lng
   * @param dis the dis
   * @param field the field
   * @return the org.apache.solr.common. solr document list
   * @author 黄林
   */
  def queryWithDis(url: String, queryStr: String, lat: Double, lng: Double, dis: Double, field: String, pageNumber: Int,
    pageSize: Int, sort: String, isDesc: Boolean) = {
    val query = new SolrQuery(queryStr)
    query.setFilterQueries(s"{!geofilt pt=$lat,$lng sfield=$field d=$dis}")
    val rows = if (pageSize < 1) 1 else pageSize
    val start = (if (pageNumber < 1) 1 else pageNumber) * rows
    query.setStart(start)
    query.setRows(rows)
    println(query)
    fullQuery(url, query)
  }

  /**
   * 默认查询
   *
   * @param url the url
   * @param query the query
   * @return the org.apache.solr.common. solr document list
   * @author 黄林
   */
  def fullQuery(url: String, query: SolrQuery) = {
    getServer(url).query(query).getResults
  }

  /**
   * 异步添加文档
   *
   * @param url the url
   * @param docs the docs
   * @return the org.apache.solr.client.solrj.response. update response
   * @author 黄林
   */
  def addDoc(url: String, doc: SolrInputDocument) = {
    actor {
      val server = getServer(url)
      server.add(doc)
      server.commit()
    }
  }
  /**
   * 添加超大量的文件，以1000个文档为单位批量添加
   * 异步模式，立即返回
   *
   * @param url the url
   * @param docs the docs
   * @author 黄林
   * Adds the many docs.
   */
  def addManyDocs(url: String, docs: java.util.ArrayList[SolrInputDocument]) {
    val server = getServer(url)
    docs.asScala.grouped(1000) foreach { list =>
      val server = getServer(url)
      actor { server.add(list.asJava) }
      server.commit()
    }
  }

  /**
   * 添加超大量的文件，以1000个文档为单位批量添加,并且等待提交完
   *
   * @param url the url
   * @param docs the docs
   * @author 黄林
   * Adds the many docs wait.
   */
  def addManyDocsWait(url: String, docs: java.util.ArrayList[SolrInputDocument]) {
    val server = getServer(url)
    val num = new AtomicInteger()
    docs.asScala.grouped(1000) foreach { list =>
      num.addAndGet(1)
      val server = getServer(url)
      try {
        actor { server.add(list.asJava) }
        server.commit()
      } finally {
        num.addAndGet(-1)
      }
    }
    while (num.get() != 0) {
      Thread.sleep(1)
    }
  }

  /**
   * Delete by query.
   *
   * @param url the url
   * @param query the query
   * @return the org.apache.solr.client.solrj.response. update response
   * @author 黄林
   */
  def deleteByQuery(url: String, query: String) = {
    val server = getServer(url)
    server.deleteByQuery(query)
    server.commit()
  }
  /**
   * 优化索引
   *
   * @param url the url
   * @param query the query
   * @return the org.apache.solr.client.solrj.response. update response
   * @author 黄林
   */
  def optimize(url: String) = {
    getServer(url).optimize()
  }
}