package com.eryansky.fastweixin.company.message.req;
/**
 *  微信企业号异步任务类型
 *
 * @author Eryan
 * @date 2016-03-15
 */
public final class QYBatchJobType {

    private final String SYNCUSER     = "sync_user";// 增量更新成员
    private final String REPLACEUSER  = "replace_user";// 全量覆盖成员
    private final String INVITEUSER   = "invite_user";// 邀请成员关注
    private final String REPLACEPARTY = "replace_party";// 全量覆盖部门

    private QYBatchJobType() {
    }
}
