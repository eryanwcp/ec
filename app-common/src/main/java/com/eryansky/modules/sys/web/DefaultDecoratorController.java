/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.core.security.annotation.PrepareOauth2;
import com.eryansky.core.security.annotation.RequiresUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * SiteMesh3 decorators
 *
 * @author Eryan
 * @date 2024-8-3
 */
@PrepareOauth2(enable = false)
@RequiresUser(required = false)
@Controller
@RequestMapping(value = "/decorators")
public class DefaultDecoratorController extends SimpleController {

    @GetMapping(value = {"default"})
    public ModelAndView _default(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/layouts/default");
        return modelAndView;
    }

    @GetMapping(value = {"default_sys"})
    public ModelAndView default_sys(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/layouts/default");
        return modelAndView;
    }

    @GetMapping(value = {"default_full"})
    public ModelAndView default_full(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/layouts/default_full");
        return modelAndView;
    }

    @GetMapping(value = {"default_mobile"})
    public ModelAndView default_mobile(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/layouts/default_mobile");
        return modelAndView;
    }
}