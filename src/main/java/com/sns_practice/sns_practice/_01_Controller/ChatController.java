package com.sns_practice.sns_practice._01_Controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Log4j2
public class ChatController {

    @GetMapping("/chat")
    public String chatGET(@RequestParam("nickname") String nickName, Model model){

        model.addAttribute("nickname", nickName);
        log.info("@ChatController, chat GET()");

        return "chat";
    }
}