package com.example.fifa_2026.controller;

import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    // Overzicht van alle teams
    @GetMapping
    public String teams(Model model) {
        model.addAttribute("teams", teamService.getTop10Teams());
        return "teamoverzicht";
    }

}