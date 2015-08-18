package cn.city.in.task.execute.taskdeal;

import cn.city.in.task.execute.taskdeal.analyse.WebNewsCategoryTaskDealImpl;
import cn.city.in.task.execute.taskdeal.db.DBTaskDealImpl;
import cn.city.in.task.execute.taskdeal.file.FileTaskDealImpl;
import cn.city.in.task.execute.taskdeal.homepage.HomePageDataRankUpdate;
import cn.city.in.task.execute.taskdeal.log.DataBaseLogWriter;
import cn.city.in.task.execute.taskdeal.push.PushTaskDealImpl;
import cn.city.in.task.execute.taskdeal.solr.SolrTaskDealImpl;

/**
 * 定义所有的处理子类
 * 
 * @author 黄林 The Class TaskDealConstants.
 */
public class TaskDealConstants {
	public static final Class<? extends TaskDealInterface>[] allDeal = new Class[] {
			PushTaskDealImpl.class, DBTaskDealImpl.class,
			WebNewsCategoryTaskDealImpl.class,HomePageDataRankUpdate.class,DataBaseLogWriter.class,FileTaskDealImpl.class,SolrTaskDealImpl.class };
}
