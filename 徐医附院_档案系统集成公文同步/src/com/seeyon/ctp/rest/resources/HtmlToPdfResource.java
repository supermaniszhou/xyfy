package com.seeyon.ctp.rest.resources;

import com.seeyon.apps.ext.DTdocument.util.FormParseTool;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.util.FormParseUtil;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("formHtml")
@Produces({MediaType.APPLICATION_JSON})
public class HtmlToPdfResource extends BaseResource {

    private EdocManager edocManager = (EdocManager) AppContext.getBean("edocManager");
    private EdocFormManager edocFormManager = (EdocFormManager) AppContext.getBean("edocFormManager");

    private static final String EXPORT_EDOC_ALL = "0";
    private static final String EXPORT_EDOC_FORM = "1";
    private static final String EXPORT_EDOC_BODY = "2";

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("productHtml")
    @RestInterfaceAnnotation
    public Response exportEdocFile(Map<String, Object> param) throws BusinessException {
        // 判断rest用户是否是单位公文收发员或者部门公文收发员
        Map<String, Object> map = new HashMap<String, Object>();
        return ok(this.exportFile(param));
    }

    /**
     * 根据公文id及导出类型导出公文到指定目录
     *
     * @param param summaryId     String  |  必填       |  公文ID
     *              folder        String  |  必填       |  输出的目录
     *              exportType    String  |  非必输   |  导出的类型
     *              0-全部；1-文单；2-正文(含花脸)
     *              不输入默认导出全部
     * @return
     */
    private Map<String, Object> exportFile(Map<String, Object> param) {
        String summaryId = (String) param.get("summaryid");
        String folder = (String) param.get("folder");
        String exportType = (String) param.get("exportType");
        List<Long> bodyIds = new ArrayList<Long>();
        List<V3XFile> v3xList = new ArrayList<V3XFile>();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if ((null != summaryId && !"".equals(summaryId)) && (null != folder && !"".equals(folder))) {
                if (null == exportType) {
                    exportType = EXPORT_EDOC_ALL;
                }
                List<Long> fileList = new ArrayList<Long>();
                EdocSummary es = edocManager.getEdocSummaryById(Long.parseLong(summaryId), true);
                if (null != es) {
                    boolean isNewGovdoc = true;
                    if (es.getGovdocType() == 0) {
                        isNewGovdoc = false;
                    }
                    // 根据导出类型导出文单、正文(含花脸)
                    if (EXPORT_EDOC_FORM.equals(exportType)) {
                        if (isNewGovdoc) {
//                            edocFormManager.writeForm2File2(Long.parseLong(summaryId), folder);
                            FormParseTool.writeForm2File2(Long.parseLong(summaryId), folder);
                        } else {
//                            edocFormManager.writeForm2File(Long.parseLong(summaryId), folder);
                            FormParseTool.writeForm2File(Long.parseLong(summaryId), folder);

                        }
                    }
                    if (map.isEmpty()) {
                        map.put("success", true);
                        map.put("msg", ResourceUtil.getString("govdoc.file.output") + folder);
                    }
                    return map;
                } else {
                    throw new BusinessException("公文ID(summaryid)查询不到对应公文");
                }
            } else {
                throw new BusinessException("公文ID(summaryid)及输出目录(folder)均不能为空");
            }
        } catch (Exception e) {
            map.put("success", false);
            map.put("msg", ResourceUtil.getString("govdoc.fail.file.operation") + e.getMessage());
            return map;
        }
    }


}
