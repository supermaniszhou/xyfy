package com.seeyon.v3x.edoc.util;


import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class ExEdocOpinionDisplayUtil extends EdocOpinionDisplayUtil {
    private static final Log LOGGER = LogFactory.getLog(ExEdocOpinionDisplayUtil.class);

    public static Map<String, Object> convertOpinionToString(Map<String, EdocOpinionModel> map, FormOpinionConfig displayConfig, CtpAffair currentAffair, boolean isFromPending, List<V3xHtmDocumentSignature> signatuers) {
        Map<String, Object> jsMap = _convertOpinionToString(map, displayConfig, currentAffair, isFromPending, signatuers, false, true);
        return jsMap;
    }


    public static Map<String, Object> _convertOpinionToString(Map<String, EdocOpinionModel> map,
                                                              FormOpinionConfig displayConfig, CtpAffair currentAffair, boolean isFromPending,
                                                              List<V3xHtmDocumentSignature> signatuers, boolean canSeeMyselfOpinion, boolean pcStyle) {
        Map<Long, StringBuilder> senderAttMap = new HashMap<Long, StringBuilder>();
        List<EdocOpinion> senderOpinions = new ArrayList<EdocOpinion>();

        Map<String, Object> jsMap = new HashMap<String, Object>();
        StringBuilder fileUrlStr = new StringBuilder();
        for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); ) {
            //公文单上元素位置
            String element = it.next();
            EdocOpinionModel model = map.get(element);
            List<EdocOpinion> opinions = model.getOpinions();
            for (EdocOpinion opinion : opinions) {
                //取回或者暂存待办的意见回写到意见框中，所以要跳过；其他情况下显示到意见区域
                if (opinion.getOpinionType().intValue() == EdocOpinion.OpinionType.provisionalOpinoin.ordinal()
                        || opinion.getOpinionType().intValue() == EdocOpinion.OpinionType.draftOpinion.ordinal()) {
                    if (currentAffair != null && canSeeMyselfOpinion) {
                        if (opinion.getAffairId() != currentAffair.getId()) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }


                //公文单不显示暂存待办意见
                StringBuilder sb = new StringBuilder();
                String value = (String) jsMap.get(element);
                if (value != null) {
                    sb.append(value);
                }
                //BUG_OA-69755_普通_V5_V5.1sp1_南宁明和信息技术有限公司_公文单中，意见之间的间隔较大_20141105004371_2014-11-11
                //if(sb.length()>0){
                //	sb.append("<br>");
                //}
                boolean hasSignature = false;
                if (signatuers != null && signatuers.size() > 0) {
                    for (V3xHtmDocumentSignature signature : signatuers) {
                        if (signature != null) {
                            if (null != signature.getAffairId() && signature.getAffairId().equals(opinion.getAffairId())) {
                                hasSignature = true;
                                break;
                            }
                        }
                    }
                }
//                if("pc".equals(user.getUserAgentFrom()) || pcStyle){
                sb.append(displayOpinionContent(displayConfig, opinion, hasSignature, true));
//                }else{
//                    sb.append(displayOpinionContentM3(displayConfig, opinion, hasSignature, true));
//                }
                //附件显示
                List<Attachment> tempAtts = null;
                if (null != opinion.getPolicy() && opinion.getPolicy().equals(EdocOpinion.FEED_BACK)) {
                    Long subOpinionId = opinion.getSubOpinionId();
                    if (subOpinionId != null) {
                        tempAtts = EdocHelper.getOpinionAttachmentsNotRelationDoc(opinion.getSubEdocId(), subOpinionId);
                    }
                } else {
                    tempAtts = opinion.getOpinionAttachments();
                }
                if (tempAtts != null) {
                    StringBuilder attSb = new StringBuilder();
                    attSb.append("<div style='clear:both;'>");
                    for (Attachment att : tempAtts) {
                        // 不管文件名有多长，显示整体的文件名。yangzd
                        //sb.append("<br>");//前端附件使用的是DIV，会自动换行
                        fileUrlStr.append(att.getFileUrl());
                        fileUrlStr.append(",");
                        String s = com.seeyon.ctp.common.filemanager.manager.Util.AttachmentToHtmlWithShowAllFileName(att, true, false);
                        sb.append(s);
                        attSb.append(s);
                    }
                    attSb.append("</div>");

                    if ("senderOpinion".equals(element)) {
                        senderAttMap.put(opinion.getId(), attSb);
                    }
                }

                //发起人附言如果没有绑定不向前台显示。前台页面通过下面的对象，有代码+标签的形式展示。
                if ("senderOpinion".equals(element)) {
                    senderOpinions.add(opinion);
                    continue;
                }

                jsMap.put(element, Strings.replaceNbspLO(sb.toString()));
            }
        }
        if (fileUrlStr.length() > 0) {
            fileUrlStr.deleteCharAt(fileUrlStr.length() - 1);
        }
        try {
            AppContext.putRequestContext("fileUrlStr", fileUrlStr);
        } catch (Exception e) {
            //TODO REST接口调用进来没有注入request
        }

        jsMap.put("senderOpinionAttStr", senderAttMap);
        jsMap.put("senderOpinionList", senderOpinions);
        return jsMap;
    }

    private static String getAttitude(Integer opinionType, int attitude) {

        String attitudeStr = null;
        String attitudeI18nLabel = "";

        //查找国际化标签。
        if (attitude > 0) {
            if (EdocOpinion.OpinionType.backOpinion.ordinal() == opinionType.intValue()) {
                attitudeI18nLabel = "stepBack.label";
            }
            //OA-18228 待办中进行终止操作，终止后到已办理查看，态度显示仍然是普通的，不是终止
            else if (EdocOpinion.OpinionType.stopOpinion.ordinal() == opinionType.intValue()) {
                attitudeI18nLabel = "stepStop.label";
            }
            //OA-19935  客户bug验证：流程是gw1，gw11，m1，串发，m1撤销，gw1在待发直接查看（不是编辑态），文单上丢失了撤销的意见
            else if (EdocOpinion.OpinionType.repealOpinion.ordinal() == opinionType.intValue()) {
                attitudeI18nLabel = "edoc.repeal.2.label";
            } else if (EdocOpinion.OpinionType.transferOpinion.ordinal() == opinionType.intValue()) {//移交
                attitudeI18nLabel = "edoc.transfer.label";
            } else {
                EnumManager enumManager = (EnumManager) AppContext.getBean("enumManagerNew");
                attitudeI18nLabel = enumManager.getEnumItemLabel(EnumNameEnum.collaboration_attitude,
                        Integer.toString(attitude));
            }
        }

        //查找用于显示的前台态度字符串
        if (Strings.isNotBlank(attitudeI18nLabel)) {

            attitudeStr = ResourceUtil.getString(attitudeI18nLabel);
        } else if (attitude == com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL) {
            attitudeStr = null;
        }

        if (opinionType == EdocOpinion.OpinionType.senderOpinion.ordinal()) attitudeStr = null;

        return attitudeStr;
    }

    /**
     * @param displayConfig
     * @param opinion
     * @param hasSignature
     * @param popUserInfo   是否新增用户信息选项卡连接 true 添加， false 不添加
     * @return
     * @Date : 2015年5月19日下午5:48:53
     */
    private static String displayOpinionContent(FormOpinionConfig displayConfig, EdocOpinion opinion, boolean hasSignature, boolean popUserInfo) {

        OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");

        StringBuilder sb = new StringBuilder();

        //显示内容：态度，用户名，意见类型，意见
        String attribute = getAttitude(opinion.getOpinionType(), opinion.getAttribute());
        String content = opinion.getContent();
        sb.append("<div id='" + opinion.getAffairId() + "' style='clear:both;'>");

        boolean newLine = displayConfig.isInscriberNewLine();

        if (newLine) {//设置落款换行显示
            sb.append("<div>");
        }

        //上报意见不显示态度
        String attrStr = null;
        if (attribute != null && (null != opinion.getPolicy() && !opinion.getPolicy().equals(EdocOpinion.REPORT))) {
            attrStr = "【" + attribute + "】";
            sb.append(attrStr);
            // 意见排序 ：【态度】 意见 部门 姓名 时间
            sb.append("&nbsp;").append(Strings.toHTML(content));
        } else {
            //没有态度的时候首行不要缩进
            sb.append(Strings.toHTML(content));
        }
        if (newLine) {//设置落款换行显示
            sb.append("</div>");
        }

        String defualt = ResourceUtil.getString("govdoc.space.quan2");//默认两个全角空格
        attrStr = replaceStr2Blank(attrStr, defualt);
        attrStr = Strings.toHTML(attrStr);
        if (newLine) {//设置落款换行显示, 跳过态度，与意见对齐,没有态度则两个汉字宽度，兼容国际化
            sb.append("<div>");
            if (!displayConfig.isNameAndDateNotInline()) {
                sb.append(attrStr);
            }
        } else {
            if (Strings.isNotBlank(content)) {
                //意见内容和后面的部门或者人员名称隔开几个空格
                sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            }
        }

        //设置了【文单签批后不显示系统落款 】，如果没有签批内容，则也需要显示系统落款。也就是，系统落款和签批内容至少要有一个
        //下面是追加显示单位名称-------魏俊标--2011-10-12
        StringBuilder orgStr = new StringBuilder();
        if (displayConfig.isShowUnit() && !(displayConfig.isHideInscriber() && hasSignature)) {
            orgStr.append("&nbsp;").append(opinion.getAccountName());
        }
        if (displayConfig.isShowDept() && !(displayConfig.isHideInscriber() && hasSignature)) {
            orgStr.append("&nbsp;").append(opinion.getDepartmentName());
        }
        if (displayConfig.isNameAndDateNotInline() && orgStr.length() > 0) {
            sb.append(attrStr);
        }
        sb.append(orgStr);


        // 如果是管理员终止，不显示管理员名字及时间
        V3xOrgMember member = getMember(opinion.getCreateUserId(), orgManager);

        String userName = getOpinionUserName(opinion.getCreateUserId(), opinion.getProxyName(), orgManager, displayConfig, opinion, popUserInfo, attrStr, "PC");

        if (!member.getIsAdmin()) {
            String tempStr = "&nbsp;";
            if (displayConfig.isNameAndDateNotInline()) {
                tempStr += attrStr;
            }
            if (!(displayConfig.isHideInscriber() && hasSignature)) {
                if (!displayConfig.isNameAndDateNotInline()) {
                    sb.append(tempStr);
                }
                sb.append(userName);
            }
            if (EdocOpinionDisplayEnum.OpinionDateFormatSetEnum.DATETIME.getValue().equals(displayConfig.getShowDateType())) {
                sb.append(tempStr).append(Datetimes.formatDatetimeWithoutSecond(opinion.getCreateTime()));
            } else if (EdocOpinionDisplayEnum.OpinionDateFormatSetEnum.DATE.getValue().equals(displayConfig.getShowDateType())) {
                sb.append(tempStr).append(Datetimes.formatDate(opinion.getCreateTime()));
            }
        }
        if (newLine) {//设置落款换行显示
            sb.append("</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private static String getOpinionUserName(Long userId, String proxyName, OrgManager orgManager, FormOpinionConfig displayConfig,
                                             EdocOpinion edocOpinion, boolean popUserInfo, String attitude, String openFrom) {
        String doUserName = "";
        try {
            V3xOrgMember member = orgManager.getMemberById(userId);
            doUserName = member.getName();

            //只有在文单内才显示签名
            if (edocOpinion.isBound() && EdocOpinionDisplayEnum.OpinionShowNameTypeEnum.SIGN.getValue().equals(displayConfig.getShowNameType())) {//电子签名显示方式
                V3xHtmDocumentSignatManager v3xHtmDocumentSignatManager = (V3xHtmDocumentSignatManager) AppContext.getBean("v3xHtmDocumentSignatManager");
                V3xHtmDocumentSignature vSign = v3xHtmDocumentSignatManager.getBySummaryIdAffairIdAndType(edocOpinion.getEdocSummary().getId(),
                        edocOpinion.getAffairId(),
                        V3xHtmSignatureEnum.HTML_SIGNATURE_EDOC_FLOW_INSCRIBE.getKey());
                if (vSign != null) {
                    String path = SystemEnvironment.getContextPath();
                    if (displayConfig.isNameAndDateNotInline()) {
                        doUserName += (attitude + "&nbsp;");
                    }
                    doUserName = "<IMG alt=\"" + Strings.toHTML(doUserName) + "\" style=\"vertical-align: text-bottom;\" src=\"" + path + "/edocController.do?method=showInscribeSignetPic&id=" + vSign.getId() + "\" >";
                }
            }

            if (member.getIsAdmin()) {
                // 如果是管理员终止，不显示管理员名字及时间
                doUserName = "";
            } else if (popUserInfo) {
                if ("PC".equals(openFrom) && displayConfig.isNameAndDateNotInline()) {
                    doUserName = "<span class='link-blue' onclick='javascript:showV3XMemberCard(\""
                            + userId
                            + "\",parent.window)'>"
                            + attitude
                            + "&nbsp;"
                            + doUserName + "</span>";
                } else {
                    doUserName = "<span class='link-blue' onclick='javascript:showV3XMemberCard(\""
                            + userId
                            + "\",parent.window)'>"
                            + doUserName + "</span>";
                }
            }

            if (!Strings.isBlank(proxyName) && !edocOpinion.getContent().contains(ResourceUtil.getString("govdoc.edoc.bidding"))) {
                doUserName += ResourceBundleUtil
                        .getString(
                                "com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                                "edoc.opinion.proxy", proxyName);
            }
            //处理人姓名单行显示
            if ("PC".equals(openFrom) && displayConfig.isNameAndDateNotInline()) {
                doUserName = "<div>" + doUserName + "</div>";
            }
        } catch (Exception e) {
            LOGGER.error("取公文单显示的时候的人名 抛出异常", e);
        }
        return doUserName;
    }

    private static String replaceStr2Blank(String src, String defualt) {

        StringBuilder ret = new StringBuilder();

        if (Strings.isBlank(src) && defualt != null) {
            ret.append(defualt);
        } else {
            for (int i = 0; i < src.length(); i++) {
                char c = src.charAt(i);
                if ((int) c > 0 && (int) c < 255) {//普通字符0x00 ~ 0xff
                    ret.append(" ");//半角空格
                } else {
                    ret.append(ResourceUtil.getString("govdoc.space.quan"));//全角空格
                }
            }
        }
        return ret.toString();
    }

    private static V3xOrgMember getMember(Long id, OrgManager orgManager) {
        V3xOrgMember member = new V3xOrgMember();
        try {
            member = orgManager.getMemberById(id);
        } catch (BusinessException e) {
            // TODO Auto-generated catch block
            LOGGER.error("", e);
        }
        return member;
    }
}
