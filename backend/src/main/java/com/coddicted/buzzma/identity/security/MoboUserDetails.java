package com.coddicted.buzzma.identity.security;

import com.coddicted.buzzma.identity.entity.UsersEntity;
import com.coddicted.buzzma.identity.entity.UserStatus;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class MoboUserDetails implements UserDetails {

  private final UsersEntity user;

  public MoboUserDetails(UsersEntity user) {
    this.user = user;
  }

  public UsersEntity getUser() {
    return user;
  }

  public UUID getUserId() {
    return user.getId();
  }

  public String[] getRoleNames() {
    return user.getRoles();
  }

  public String getMediatorCode() {
    return user.getMediatorCode();
  }

  public String getParentCode() {
    return user.getParentCode();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    String[] roles = user.getRoles();
    if (roles == null || roles.length == 0) {
      return java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
    return Arrays.stream(roles)
        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
        .collect(Collectors.toList());
  }

  @Override
  public String getPassword() {
    return user.getPasswordHash();
  }

  @Override
  public String getUsername() {
    return user.getId().toString();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return user.getLockoutUntil() == null
        || user.getLockoutUntil().isBefore(java.time.Instant.now());
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return !user.getIsDeleted() && UserStatus.active.equals(user.getStatus());
  }
}
