package com.ruoyi.web.controller.workflow;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.workflow.domain.dto.FlowProcDefDto;
import com.ruoyi.workflow.domain.dto.FlowSaveXmlVo;
import com.ruoyi.workflow.service.IFlowDefinitionService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 工作流程定义
 * </p>
 *
 * @author XuanXuan
 * @date 2021-04-03
 */
@Slf4j
@Api(tags = "流程定义")
@RestController
@RequestMapping("/workflow/definition")
public class FlowDefinitionController {

    @Autowired
    private IFlowDefinitionService flowDefinitionService;

    @Autowired
    private ISysUserService userService;

    @Resource
    private ISysRoleService sysRoleService;


    @GetMapping(value = "/list")
    @ApiOperation(value = "流程定义列表", response = FlowProcDefDto.class)
    public R list(@ApiParam(value = "当前页码", required = true) @RequestParam Integer pageNum,
                           @ApiParam(value = "每页条数", required = true) @RequestParam Integer pageSize) {
        return R.success(flowDefinitionService.list(pageNum, pageSize));
    }


    @ApiOperation(value = "导入流程文件", notes = "上传bpmn20的xml文件")
    @PostMapping("/import")
    public R importFile(@RequestParam(required = false) String name,
                                 @RequestParam(required = false) String category,
                                 MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            flowDefinitionService.importFile(name, category, in);
        } catch (Exception e) {
            log.error("导入失败:", e);
            return R.success(e.getMessage());
        }

        return R.success("导入成功");
    }


    @ApiOperation(value = "读取xml文件")
    @GetMapping("/readXml/{deployId}")
    public R readXml(@ApiParam(value = "流程定义id") @PathVariable(value = "deployId") String deployId) {
        try {
            return flowDefinitionService.readXml(deployId);
        } catch (Exception e) {
            return R.error("加载xml文件异常");
        }

    }

    @ApiOperation(value = "读取图片文件")
    @GetMapping("/readImage/{deployId}")
    public void readImage(@ApiParam(value = "流程定义id") @PathVariable(value = "deployId") String deployId, HttpServletResponse response) {
        try (OutputStream os = response.getOutputStream()) {
            BufferedImage image = ImageIO.read(flowDefinitionService.readImage(deployId));
            response.setContentType("image/png");
            if (image != null) {
                ImageIO.write(image, "png", os);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @ApiOperation(value = "保存流程设计器内的xml文件")
    @PostMapping("/save")
    public R save(@RequestBody FlowSaveXmlVo vo) {
        try (InputStream in = new ByteArrayInputStream(vo.getXml().getBytes(StandardCharsets.UTF_8))) {
            flowDefinitionService.importFile(vo.getName(), vo.getCategory(), in);
        } catch (Exception e) {
            log.error("导入失败:", e);
            return R.success(e.getMessage());
        }

        return R.success("导入成功");
    }


    @ApiOperation(value = "根据流程定义id启动流程实例")
    @PostMapping("/start/{procDefId}")
    public R start(@ApiParam(value = "流程定义id") @PathVariable(value = "procDefId") String procDefId,
                            @ApiParam(value = "变量集合,json对象") @RequestBody Map<String, Object> variables) {
        return flowDefinitionService.startProcessInstanceById(procDefId, variables);

    }

    @ApiOperation(value = "激活或挂起流程定义")
    @PutMapping(value = "/updateState")
    public R updateState(@ApiParam(value = "1:激活,2:挂起", required = true) @RequestParam Integer state,
                                  @ApiParam(value = "流程部署ID", required = true) @RequestParam String deployId) {
        flowDefinitionService.updateState(state, deployId);
        return R.success();
    }

    @ApiOperation(value = "删除流程")
    @DeleteMapping(value = "/delete")
    public R delete(@ApiParam(value = "流程部署ID", required = true) @RequestParam String deployId) {
        flowDefinitionService.delete(deployId);
        return R.success();
    }

    @ApiOperation(value = "指定流程办理人员列表")
    @GetMapping("/userList")
    public R userList(SysUser user) {
        List<SysUser> list = userService.selectUserList(user);
        return R.success(list);
    }

    @ApiOperation(value = "指定流程办理组列表")
    @GetMapping("/roleList")
    public R roleList(SysRole role) {
        List<SysRole> list = sysRoleService.selectRoleList(role);
        return R.success(list);
    }

}