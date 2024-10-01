package top.xpyvip.bingWallpaper.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Validated
@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class DefaultApiController {

    /**
     * 默认controller控制器，在前端页面上线之前使用
     */
    @GetMapping()
    public String get() throws Exception {
        return "redirect:/image";
    }
}
