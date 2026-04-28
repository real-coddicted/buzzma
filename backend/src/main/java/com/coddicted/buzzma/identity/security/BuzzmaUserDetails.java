package com.coddicted.buzzma.identity.security;

import com.coddicted.buzzma.identity.entity.UserRole;
import com.coddicted.buzzma.identity.entity.UsersEntity;
import com.coddicted.buzzma.identity.entity.UserStatus;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class BuzzmaUserDetails implements UserDetails {

  private final UsersEntity user;

  public BuzzmaUserDetails(UsersEntity user) {
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
