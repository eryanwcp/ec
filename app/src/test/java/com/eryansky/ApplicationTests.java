package com.eryansky;

import com.eryansky.common.utils.ThreadUtils;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.modules.sys.dao.VersionLogDao;
import com.eryansky.modules.sys.mapper.OrganExtend;
import com.eryansky.modules.sys.mapper.VersionLog;
import com.eryansky.modules.sys.service.*;
import com.eryansky.modules.sys.utils.OrganUtils;
import com.eryansky.modules.sys.utils.SystemSerialNumberUtils;
import com.eryansky.modules.sys.utils.UserUtils;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.eryansky.modules.sys.mapper.VersionLogDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;

@RunWith(value = SpringJUnit4ClassRunner.class)
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

    @Test
    public void contextLoads() {
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
