package com.eryansky;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.utils.ThreadUtils;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.rpc.client.TestConsumer;
import com.eryansky.modules.sys.mapper.OrganExtend;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.mapper.VersionLog;
import com.eryansky.modules.sys.service.*;
import com.eryansky.modules.sys.utils.OrganUtils;
import com.eryansky.modules.sys.utils.SystemSerialNumberUtils;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.client.common.vo.ExtendAttr;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static com.eryansky.modules.sys.mapper.VersionLogDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;

@SpringBootTest(classes = Application.class)
public class ApplicationTests {

    @Autowired
    private ConfigService configService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PostService postService;
    @Autowired
    private VersionLogService versionLogService;
    @Autowired
    private TestConsumer testConsumer;

    @Test
    public void contextLoads() {
        System.out.println(testConsumer.testRpc2("1"));

    }


    @Test
    public void extendAttr() {
        User user = userService.getUserByLoginName("admin");
        System.out.println(JsonMapper.toJsonString(user));
        ExtendAttr extendAttr = new ExtendAttr();
        extendAttr.put("key1","data1");
        extendAttr.put("key2", Arrays.asList("1","2"));
        user.setExtendAttr(extendAttr);
        userService.save(user);
        user = userService.getUserByLoginName("admin");
        System.out.println(JsonMapper.toJsonString(user));
        //SELECT * FROM t_sys_user a WHERE JSON_EXTRACT(a.extend_attr,'$.key1') = 'data1'
//        String sql = "WHERE JSON_EXTRACT(a.extend_attr,'$.key1') = 'data1'";
//        List<User> users = userService.findBySql(sql);

        String sql = "WHERE JSON_EXTRACT(a.extend_attr,'$.key1') = #{extendAttrKey1}";
        Parameter parameter = Parameter.newParameter();
        parameter.put("extendAttrKey1","data1");

        List<User> users = userService.findBySql(sql,parameter);
        System.out.println(JsonMapper.toJsonString(users));

    }


    @Test
    public void addUserRoleAndPost() {
        String userId = "1eb60440083945bb84394b7b1cc77414";
        String postCode = "post1";
        String roleCode = "role1";
        System.out.println(JsonMapper.toJsonString(postService.findPostsByUserId(userId)));
        UserUtils.addUserOrganPost(userId, postCode);
        System.out.println(JsonMapper.toJsonString(postService.findPostsByUserId(userId)));

        System.out.println(JsonMapper.toJsonString(roleService.findRolesByUserId(userId)));
        UserUtils.addUserRole(userId, roleCode);
        System.out.println(JsonMapper.toJsonString(roleService.findRolesByUserId(userId)));
    }


    @Test
    public void e() {
        System.out.println(Encrypt.e("1"));
    }


    @Test
    public void generateSerialNumberByModelCode() {
        String moduleCode = "A01";
        Map<String, String> params0 = Maps.newHashMap();
        params0.put("param1", "");
        params0.put("param2", "");
        String customCategory0 = null;
        for (int i = 1; i < 10; i++) {
            int finalI = i;
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " 0-" + finalI + " " + SystemSerialNumberUtils.generateSerialNumberByModelCode(moduleCode, customCategory0, params0));
            }).start();
        }

        Map<String, String> params1 = Maps.newHashMap();
        params1.put("param1", "A01");
        params1.put("param2", "A01");
        String customCategory1 = "A01";
        for (int i = 1; i < 10; i++) {
            int finalI = i;
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " 1-" + finalI + " " + SystemSerialNumberUtils.generateSerialNumberByModelCode(moduleCode, customCategory1, params1));
            }).start();
        }
        Map<String, String> params2 = Maps.newHashMap();
        params2.put("param1", "B02");
        params2.put("param2", "B02");
        String customCategory2 = "B02";
        for (int i = 1; i < 10; i++) {
            int finalI = i;
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " 2-" + finalI + " " + SystemSerialNumberUtils.generateSerialNumberByModelCode(moduleCode, customCategory2, params2));
            }).start();
        }

        ThreadUtils.sleep(30 * 1000);

    }


    @Test
    public void testSelectByExample() {
        SelectStatementProvider selectStatement = select(data.allColumns())
                .from(data, "a")
                .where(app, isEqualTo("1"))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        List<VersionLog> rows = versionLogService.selectMany(selectStatement);
        System.out.println(JsonMapper.toJsonString(rows));
    }



    @Test
    public void getOrganExtendByUserId() {
        Date d1 = Calendar.getInstance().getTime();
        for(int i =0;i<1000;i++){
            OrganExtend organExtend = OrganUtils.getOrganExtendByUserLoginName("admin");
            System.out.println(JsonMapper.toJsonString(organExtend));
        }
        Date d2 = Calendar.getInstance().getTime();
        System.out.println(d2.getTime() - d1.getTime());
    }
}
