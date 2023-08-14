package com.eryansky.codegen;

import com.eryansky.codegen.util.FileType;

/**
 * 各类资源配置
 */
public class Resources {
	
	public static final String CATALOG = "";
	public static final String SCHEMA = "";

    /**
     * 模块名称 子模块可以设置为“package1.package2”
     */
    public static final String MODULE = "biz";
    public static final String AUTHOR = "eryan";
    public static final String PRODUCT_NAME = "XXX有限公司";
    public static final String PRODUCT_URL = "https://github.com/eryanwcp/ec";


	/************ 模板配置 ************/
	public static final String TEMPLATE_PATH = "app/src/test/java/com/eryansky/codegen/template";//Long bigint主键
//	public static final String TEMPLATE_PATH = "app/src/test/java/com/eryansky/codegen/template_uuid";//String UUID主鍵
	public static final String ENTITY_TEMPLATE = "java_entity.vm";
	public static final String DAO_TEMPLATE = "java_dao.vm";
	public static final String DAO_XML_TEMPLATE = "xml_dao.vm";
	public static final String SERVICE_TEMPLATE = "java_service.vm";
	public static final String CONTROLLER_TEMPLATE = "java_controller.vm";

	public static final String JSP_LIST_TEMPLATE = "jsp_list.vm";
	public static final String JSP_INPUT_TEMPLATE = "jsp_input.vm";

	/************
	 * Package 声明,
	 * 如果只声明了BASE_PACKAGE,未声明其它Package
	 * 那么以base_package为基础创建目录/com/**
	 * -entity
	 * -dao
	 * -service
	 * --impl
	 * -controller
	 **************/



	public static final String BASE_PACKAGE = "com.eryansky.modules";//基础包路径
	public static final String ENTITY_PACKAGE = BASE_PACKAGE+"."+MODULE+".mapper";
	public static final String DAO_PACKAGE = BASE_PACKAGE+"."+MODULE+".dao";
	public static final String SERVICE_PACKAGE = BASE_PACKAGE+"."+MODULE+".service";
	public static final String CONTROLLER_PACKAGE = BASE_PACKAGE+"."+MODULE+".web";

	/************ controller访问地址 : request_mapping/moudle ****************/
	public static final String REQUEST_MAPPING = "jsp/"+MODULE.replace(".","/");

	public static final String JSP_STORE_PATH =  "E:\\workspace\\wencp\\ec\\app\\src\\main\\webapp\\WEB-INF\\views\\";
	/************ 生成JAVA文件的根目录，系统根据package声明进行目录创建 **********/
	public static final String JAVA_STROE_PATH = "E:\\workspace\\wencp\\ec\\app\\src\\main\\java\\";

	public static String getClazzNameByTableName(String tableName) {
		return null;
	}

	/**
	 * 根据Java文件类型获取存储地址
	 * 
	 * @param type
	 * @return
	 */
	public static String getJavaStorePath(FileType type) {
		String packageDecl = getPackage(type);
		packageDecl = packageDecl.replaceAll("\\.", "/");
		return JAVA_STROE_PATH + "/" + packageDecl;
	}

	/**
	 * 根据Java文件类型获取Package声明
	 * 
	 * @param type
	 * @return
	 */
	public static String getPackage(FileType type) {
		if (type.getPakage() == null || "".equals(type.getPakage()))
			return BASE_PACKAGE + "." + type.getType();
		else
			return type.getPakage();
	}

}
