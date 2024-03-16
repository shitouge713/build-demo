/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.build.demo.controller;

import com.build.demo.task.ProduceUnevenTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 取消任务执行controller
 */
@Controller
public class TaskController {

    @Autowired
    private ProduceUnevenTask produceUnevenTask;

    @ResponseBody
    @GetMapping("/serialTask")
    public void serialTask() {
        produceUnevenTask.serialTask();
    }

    @ResponseBody
    @GetMapping("/cancel")
    public void cancel() {
        produceUnevenTask.cancelTask();
    }
}
