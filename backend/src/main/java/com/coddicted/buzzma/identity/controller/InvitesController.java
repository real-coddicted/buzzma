package com.coddicted.buzzma.identity.controller;

import com.coddicted.buzzma.identity.service.InviteService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invites")
@Validated
public class InvitesController {

  private final InviteService service;

  public InvitesController(InviteService service) {
    this.service = service;
  }
}
