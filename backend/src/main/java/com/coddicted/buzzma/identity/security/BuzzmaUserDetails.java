package com.coddicted.buzzma.identity.security;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class BuzzmaUserDetails implements UserDetails {

  private final BuzzmaUser user;

  public BuzzmaUserDetails(BuzzmaUser user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    final UserRole userRole = user.getRole();
    return List.of(new SimpleGrantedAuthority(userRole.name()));
  }

  @Override
  public String getPassword() {
    return "hashed";
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isEnabled() {
    return !user.getIsDeleted();
  }
}
