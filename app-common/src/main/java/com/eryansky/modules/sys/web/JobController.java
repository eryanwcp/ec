package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.excels.ExcelUtils;
import com.eryansky.core.excels.JsGridReportBase;
import com.eryansky.core.excels.TableData;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.modules.sys._enum.JobState;
import com.eryansky.modules.sys.mapper.QuartzJobDetail;
import com.eryansky.modules.sys.service.JobService;
import com.google.common.collect.Maps;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.rmi.ServerException;
import java.util.HashMap;
import java.util.Map;

/**
 * Quartz定时任务管理
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/job")
public class JobController extends SimpleController {
	@Autowired
	private JobService jobAndTriggerService;

	@Autowired
	private Scheduler scheduler;

	/**
	 * 任务列表
	 * @param export
	 * @param model
	 * @param uiModel
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("sys:job:view")
	@RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value={"list",""})
	public String getJobList(@RequestParam(value = "export",defaultValue = "false") Boolean export, QuartzJobDetail model,
                             Model uiModel, HttpServletRequest request, HttpServletResponse response) {
		Page<QuartzJobDetail> page = new Page<>(request, response);
		if(WebUtils.isAjaxRequest(request) || export){
			if(export){
				page.setPageSize(Page.PAGESIZE_ALL);
			}
			page = jobAndTriggerService.findJobList(page,model.getJobName(),model.getTriggerState());
			if(export){
				String title = "定时任务列表";
				String[] hearders = new String[] {"任务名", "任务状态", "执行实例","时间表达式","上一次执行时间","下一次执行时间"};//表头数组
				String[] fields = new String[] {"jobName", "triggerStateView", "instanceName", "cronExpression","prevFireTime","nextFireTime"};//对象属性数组
				TableData td = ExcelUtils.createTableData(page.getResult(), ExcelUtils.createTableHeader(hearders,0),fields);
				try {
					JsGridReportBase report = new JsGridReportBase(request, response);
					report.exportToExcel(title, SecurityUtils.getCurrentUserName(), td);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
				return null;
			}
			return renderString(response,page);
		}
		uiModel.addAttribute("page",page);
		uiModel.addAttribute("model",model);
		uiModel.addAttribute("states",JobState.values());
		return "modules/sys/jobList";

	}

	/**
	 * 立即执行定时任务
	 * @param request
	 * @param jobClassName
	 * @param jobGroupName
	 * @return
	 */
	@RequiresPermissions("sys:job:edit")
	@PostMapping(value = "triggerJob")
	@ResponseBody
	public Result triggerJob(String jobClassName, String jobGroupName, HttpServletRequest request, HttpServletResponse response) {
		try {
			//JobKey定义了job的名称和组别
			JobKey jobKey = JobKey.jobKey(jobClassName, jobGroupName);
			//执行一次任务
			scheduler.triggerJob(jobKey);
		} catch (SchedulerException e) {
			logger.error("执行定时任务失败", e);
			return Result.errorResult().setMsg("任务执行失败");
		}
		return Result.successResult().setMsg("任务执行成功");
	}


	/**
	 * 暂停定时任务
	 * @param request
	 * @param jobClassName
	 * @param jobGroupName
	 * @return
	 */
	@RequiresPermissions("sys:job:edit")
	@PostMapping(value = "pauseJob")
	@ResponseBody
	public Result pauseJob(String jobClassName, String jobGroupName, HttpServletRequest request, HttpServletResponse response) {
		try {
			//JobKey定义了job的名称和组别
			JobKey jobKey = JobKey.jobKey(jobClassName, jobGroupName);
			//暂停任务
			scheduler.pauseJob(jobKey);
		} catch (SchedulerException e) {
			logger.error("暂停调度任务异常", e);
			return Result.errorResult().setMsg("任务暂停失败");
		}
		return Result.successResult().setMsg("任务已经暂停");
	}

	/**
	 * 继续定时任务
	 * @param request
	 * @param jobClassName
	 * @param jobGroupName
	 * @return
	 */
	@RequiresPermissions("sys:job:edit")
	@PostMapping(value = "resumeJob")
	@ResponseBody
	public Result resumeJob(String jobClassName, String jobGroupName, HttpServletRequest request, HttpServletResponse response) {
		try {
			//JobKey定义了job的名称和组别
			JobKey jobKey = JobKey.jobKey(jobClassName, jobGroupName);
			//继续任务
			scheduler.resumeJob(jobKey);
		} catch (SchedulerException e) {
			logger.error("继续调度任务异常", e);
			return Result.errorResult().setMsg("任务继续失败");
		}
		return Result.successResult().setMsg("任务已经继续");
	}

	/**
	 * 添加定时任务
	 * @param jobClassName
	 * @param jobGroupName
	 * @param cronExpression
	 * @return
	 */
	@RequiresPermissions("sys:job:edit")
	@PostMapping(value = "addJob")
	@ResponseBody
	public Result addJob(String jobClassName, String jobGroupName, String cronExpression, HttpServletRequest request, HttpServletResponse response) {
		try {
			//构建JobDetail(表示一个具体的可执行的调度程序,Job是这个可执行程调度程序所要执行的内容)
			JobDetail jobDetail = JobBuilder.newJob(getClass(jobClassName).getClass())//工作项1：Job类
					.withIdentity(jobClassName, jobGroupName)//工作项2：job名以及所属组
					.build();//构建

			//构建触发器Trigger(调度参数的配置，代表何时触发该任务)
			//通过cron表达式构建CronScheduleBuilder
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
			//构建CronTrigger触发器
			CronTrigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(jobClassName, jobGroupName) //工作项1：job名以及所属组
					.withSchedule(scheduleBuilder) //工作项2：指定调度参数
					.build();//构建

			/**
			 *构建调度容器(当Trigger与JobDetail组合，就可以被Scheduler容器调度了)
			 * 一个调度容器中可以注册多个JobDetail和Trigger。
			 */
			//注册调度任务
			scheduler.scheduleJob(jobDetail, trigger);
			//启动任务
			scheduler.start();
		} catch (SchedulerException e) {
			logger.error("构建调度任务异常", e);
			return Result.errorResult().setMsg("添加定时任务失败");
		} catch (ServerException e) {
			logger.error("内部异常", e);
			return Result.errorResult().setMsg("添加定时任务失败");
		}
		return Result.successResult().setMsg("添加定时任务成功");
	}

	/**
	 * 更新定时任务
	 * @param request
	 * @param jobClassName
	 * @param jobGroupName
	 * @param cronExpression
	 * @return
	 */
	@RequiresPermissions("sys:job:edit")
	@PostMapping(value = "rescheduleJob")
	@ResponseBody
	public Result rescheduleJob(String jobClassName, String jobGroupName, String cronExpression, HttpServletRequest request, HttpServletResponse response) {
		try {
			//构建旧的TriggerKey
			TriggerKey triggerKey = TriggerKey.triggerKey(jobClassName, jobGroupName);
			//通过cron表达式构建CronScheduleBuilder
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
			//从调度容器中获取旧的CronTrigger
			CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
			//更新CronTrigger
			trigger = trigger.getTriggerBuilder()
					.withIdentity(triggerKey) //工作项1：job名以及所属组
					.withSchedule(scheduleBuilder) //工作项2：指定调度参数
					.build();//构建

			//更新定时任务
			scheduler.rescheduleJob(triggerKey, trigger);
		} catch (SchedulerException e) {
			logger.error("更新定时任务异常", e);
			return Result.errorResult().setMsg("更新定时任务失败");
		}
		return Result.successResult().setMsg("更新定时任务成功");
	}


	/**
	 * 删除定时任务
	 * @param request
	 * @param jobClassName
	 * @param jobGroupName
	 * @return
	 */
	@RequiresPermissions("sys:job:edit")
	@PostMapping(value = "removeJob")
	@ResponseBody
	public Result removeJob(String jobClassName, String jobGroupName, HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> returnData = Maps.newHashMap();
		try {
			//TriggerKey定义了trigger的名称和组别
			TriggerKey triggerKey = TriggerKey.triggerKey(jobClassName, jobGroupName);
			//暂停触发器
			scheduler.resumeTrigger(triggerKey);
			//暂停触发器
			scheduler.unscheduleJob(triggerKey);
			//移除任务
			scheduler.deleteJob(JobKey.jobKey(jobClassName, jobGroupName));
		} catch (SchedulerException e) {
			logger.error("删除定时任务异常", e);
			return Result.errorResult().setMsg("删除定时任务失败");
		}
		return Result.successResult().setMsg("删除定时任务成功");
	}

	/**
	 * 获得指定的类实例
	 * @param classname
	 * @return
	 * @throws ServerException
	 */
	private Job getClass(String classname) throws ServerException {
		Job baseJob = null;
		try {
			//加载参数指定的类
			Class<?> classTmp = Class.forName(classname);
			//实例化
			baseJob = (Job) classTmp.newInstance();
		} catch (ClassNotFoundException e) {
			logger.error("找不到指定的类", e);
			throw new ServerException("");
		} catch (InstantiationException e) {
			logger.error("实例化类失败", e);
			throw new ServerException("");
		} catch (IllegalAccessException e) {
			logger.error("实例化类失败", e);
			throw new ServerException("");
		}
		return baseJob;
	}
	
	
}
