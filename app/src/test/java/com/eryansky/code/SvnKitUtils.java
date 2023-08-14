package com.eryansky.code;

import com.eryansky.common.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class SvnKitUtils {

    // svn各种客户端的管理工具，使用svnkit-hl接口进行开发
    private final SVNClientManager ourClientManager;
    // 仓库的统计开始版本，默认为1
    private static long defaultPreVersion = 1;
    private static final String USER_NAME = "";//SVN账号
    private static final String USER_PASSWORD = "";//SVN密码
    // 仓库地址
    public static String repositoryURL ="https://github.com/eryanwcp/ec.git";
    // 本地工作目录
    public static String rootDir = "E:\\workspace\\wencp\\ec\\";
    public static  String[] modules = new String[]{"sys","notice","disk"};//模块 module目录下 多个之间以”//“分割

    public static void main(final String[] args) throws Exception {
        // 初始化统计工具
        SvnKitUtils test = new SvnKitUtils(USER_NAME, USER_PASSWORD);
        //test.setDefaultPreVersion();  //设置统计版本起点， 默认从第1版开始
        test.setDefaultPreVersion(defaultPreVersion);  //设置统计版本起点， 默认从第1版开始
        List<String> dirs = Lists.newArrayList();
        dirs.add("common\\src\\main\\java");
        dirs.add("fastweixin\\src\\main\\java");
        dirs.add("j2cache\\src\\main\\java");
        dirs.add("j2cache-spring-boot-starter\\src\\main\\java");
        dirs.add("app\\src\\main\\java");
        dirs.add("app\\src\\main\\resources\\templates");
        dirs.add("app-common\\src\\main\\resources\\mappings\\extend");
        for(int i =0;i<modules.length;i++){
            String module = modules[i];
            dirs.add("app-common\\src\\main\\java\\com\\eryansky\\modules\\"+module);
            dirs.add("app-common\\src\\main\\resources\\mappings\\modules\\"+module);
            dirs.add("app-common\\src\\main\\webapp\\WEB-INF\\views\\modules\\"+module);
            dirs.add("app-common\\src\\main\\webapp\\WEB-INF\\views\\mobile\\modules\\"+module);
            dirs.add("app-common\\src\\main\\webapp\\static\\app\\modules\\"+module);
            dirs.add("app-common\\src\\main\\webapp\\static\\app\\mobile\\modules\\"+module);
        }

        dirs.add("src\\test\\java\\test\\utils");

        // 需要统计的文件格式
        String[] filters = new String[]{".java", ".css", ".js",".xml", ".jsp", ".properties"};
        Map<String, LineNumber> authors = Maps.newHashMap();
        // 执行统计
        test.computeCodeLineForEveryone(repositoryURL, rootDir,dirs, filters,authors);
        Collection<LineNumber> lineNumbers = authors.values();
        // 输出所有作者的贡献指标
        for (LineNumber lineNumber : lineNumbers) {
            System.out.println(lineNumber);
        }
    }


    static {
        // 开启SVN的 http和https的访问方式
        DAVRepositoryFactory.setup();
    }



    /**
     * 初始化工具类，为工具类连接仓库设置认证信息
     * @param username 连接用户名
     * @param password 连接密码
     */
    public SvnKitUtils(String username, String password) {
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        ourClientManager =SVNClientManager.newInstance((DefaultSVNOptions) options, username, password);
    }

    // getter 和 setter
    public long getDefaultPreVersion() {
        return defaultPreVersion;
    }

    public void setDefaultPreVersion(long defaultPreVersion) {
        SvnKitUtils.defaultPreVersion = defaultPreVersion;
    }

    // 其他实现，如checkout等
    /**
     * 从仓库检出代码到工作目录下
     * @param repositoryUrl 仓库地址
     * @param workCopyPath 本地工作路径
     * @return 当前最新版本号
     */
    public long checkOut(String repositoryUrl, String workCopyPath) throws SVNException {
        SVNURL svnurl = SVNURL.parseURIEncoded(repositoryUrl);
        File wcDir = new File(workCopyPath);
        wcDir.mkdirs();
        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        return updateClient.doCheckout(svnurl, wcDir, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
    }

    /**
     * 递归遍历文件夹下指定格式的文件，并返回
     * @param rootFolder 遍历的文件根目录
     * @param filters 指定的文件类型，如：new String[]{".java", ".html", ".css", ".js", ".jsp", ".properties"}
     * @return 目录下所有为指定类型的文件列表
     */
    private List<File> scanFolder(File rootFolder, String[] filters){
        List<File> paths = new ArrayList<File>();
        File[] childFiles = rootFolder.listFiles();
        if(childFiles == null){
            return Collections.EMPTY_LIST;
        }
        for (File childFile : childFiles) {
            if(childFile.isDirectory()){
                paths.addAll(scanFolder(childFile, filters));
            }else {
                for (String filter : filters) {
                    if(childFile.getName().endsWith(filter)){
                        paths.add(childFile);
                    }
                }
            }
        }
        return paths;
    }

    /**
     * 获取文件的所有更新版本，并遍历
     * @param file 需要遍历的文件
     * @param authors 用于保存项目作者和其指标的键值对
     */
    private void computeDiff(final File file, final Map<String, LineNumber> authors) throws SVNException, IOException {
        long preVersion = defaultPreVersion;
        final Map<Long, String> versions = new HashMap<Long, String>();
        ourClientManager.getLogClient().doLog(new File[]{file}, SVNRevision.create(defaultPreVersion), SVNRevision.HEAD, true, true, 1000000l, new ISVNLogEntryHandler() {
            public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
                Map<String, SVNPropertyValue> meta = logEntry.getRevisionProperties().asMap();
                String value = meta.get("svn:author").getString();
                if(value != null){
                    if(!authors.containsKey(value)){
                        authors.put(value, new LineNumber(value));
                    }
                }
                versions.put(logEntry.getRevision(), value);
            }
        });

        // 遍历计算
        compute(file, versions, authors);
    }

    /**
     * 遍历文件修改版本<br/>
     * 遍历修正版本，生成diff文件
     * @param file 本次统计文件
     * @param versions 文件的修订版本与修订作者的键值对
     * @param authors 作者与作者贡献指标键值对
     */
    private void compute(File file, Map<Long, String> versions, Map<String, LineNumber> authors) throws IOException, SVNException {
        File out = new File("temp");
        List<String> changeList = new ArrayList<String>();

        long preVersion = defaultPreVersion;
        for (Map.Entry<Long, String> longStringEntry : versions.entrySet()) {
            ourClientManager.getDiffClient().doDiff(file,SVNRevision.HEAD, SVNRevision.create(preVersion),SVNRevision.create(longStringEntry.getKey()),SVNDepth.INFINITY,true, new FileOutputStream(out), changeList);
            computeFileLineNum(out, authors.get(longStringEntry.getValue()));
            preVersion = longStringEntry.getKey();
        }
    }

    /**
     * 统计该修改版本的各项指标。<br/>
     * 使用BufferedReader遍历文件的每一行数据，
     * 然后判断该行代码属于指标属性，
     * 最后对该作者对应的指标积进行相关操作
     * @param file 版本修改文件（svn的diff文件）
     * @param lineNumber 该作者的指标对象
     */
    private void computeFileLineNum(File file, LineNumber lineNumber) throws IOException {
        int addCount = -1;//需要减去首行‘++++’开始的一行
        int removeCount = -1;//需要减去首行‘----’开始的一行
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        // 遍历diff文件，计算指标
        while((line = reader.readLine())!=null) {
            if(line.startsWith("+")){
                addCount++;
            }else if(line.startsWith("-")) {
                removeCount ++;
            }
        }
        reader.close();

        // 设置作者指标
        lineNumber.setModify(removeCount);
        lineNumber.setAdd(addCount-removeCount);
    }

    /**
     * 某一个仓库的代码贡献量的统计故居
     * @param repository 仓库地址
     * @param workCopyPath 本地工作路径
     * @param filters 文件类型过滤字符串数组
     * @return 该仓库的代码贡献量的统计结果
     */
    public Collection<LineNumber> computeCodeLineForEveryone(String repository, String workCopyPath, String[] filters) throws IOException, SVNException {
        // 仓库检出
        checkOut(repository, workCopyPath);

        // 获取需要统计的文件
        List<File> files = scanFolder(new File(workCopyPath), filters );

        // 遍历文件，并计算文件各个更新版本中的代码共享量
        Map<String, LineNumber> authors = new HashMap<String, LineNumber>();
        for (File file : files) {
            System.out.println("compute:"+file.getAbsolutePath());
            computeDiff(file, authors);
        }

        // 返回计算记过
        return authors.values();
    }


    /**
     * 某一个仓库的代码贡献量的统计故居
     * @param repository 仓库地址
     * @param workCopyPath 本地工作路径
     * @param filters 文件类型过滤字符串数组
     * @return 该仓库的代码贡献量的统计结果
     */
    /**
     * 某一个仓库的代码贡献量的统计故居
     * @param repository 仓库地址
     * @param workCopyPath 本地工作路径
     * @param filters 文件类型过滤字符串数组
     * @return 该仓库的代码贡献量的统计结果
     */
    public void computeCodeLineForEveryone(String repository, String workCopyPath, Collection<String> dirs, String[] filters, final Map<String, LineNumber> authors) throws IOException, SVNException, ExecutionException, InterruptedException {
        // 仓库检出
        checkOut(repository, workCopyPath);

        // 获取需要统计的文件
        List<File> files = Lists.newArrayList();
        for(String dir:dirs){
            files.addAll(scanFolder(new File(workCopyPath+dir), filters ));
        }

        // 遍历文件，并计算文件各个更新版本中的代码共享量
        for (final File file : files) {
            System.out.println(StringUtils.substringAfter(file.getAbsolutePath(),workCopyPath));
            computeDiff(file, authors);
        }
    }


}