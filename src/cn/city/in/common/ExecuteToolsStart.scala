package cn.city.in.common

import org.apache.log4j.PropertyConfigurator
import cn.city.in.api.tools.common.PropertyTool
import cn.city.in.api.tools.common.FileTool
import java.sql.DriverManager

object ExecuteToolsStart {
  var init = false
  /*
	 * 工具类启动主入口
	 * 
	 */
  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
      println("请输入参数")
    }
    // 初始化日志及配置文件
    PropertyConfigurator.configureAndWatch(FileTool.getClassPathFile(
      "log4j.properties").getAbsolutePath())
    PropertyTool
      .init("classpath:config/*.conf")
    //读取命令列表
    val commonds = PropertyTool.readPropertyFileAsMap("commandfile")

  }

  def getConnection(driver: String, username: String, password: String, url: String) = {
    if (!init) {
      Class.forName(driver)
      init = true
    }
    DriverManager.getConnection(url,username,password)
  }

}